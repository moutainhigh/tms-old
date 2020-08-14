package com.tms.service.job.edi;

import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.job.ApiTypeConst;
import org.nw.job.IConverter;
import org.nw.job.IJobService;
import org.nw.utils.HttpUtils;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.JobDefVO;

import com.tms.BillStatus;
import com.tms.constants.TrackingConst;
import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.impl.EntrustUtils;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntTransbilityBVO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.types.Schema;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.text.ParseException;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * 裕络运力信息导出，调用对方web service，将数据导出给第三方系统
 * 
 * @author songf
 * @Date 2015年11月14日 下午15:34:28
 *
 */
public class ExportEntTransbilityInfoJobService implements IJobService {

	static Logger logger = Logger.getLogger(ExportEntTransbilityInfoJobService.class);

	@SuppressWarnings("rawtypes")

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出运力信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有运力信息需要同步");

		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");

		// 查询委托单的运力信息
		String sql = "SELECT distinct ts_ent_transbility_b.pk_ent_trans_bility_b, '裕络' SP_GID ,ts_entrust.CUST_ORDERNO AS SHIPMENT_GID,ts_entrust.CUST_ORDERNO AS  ID,"
				+ " ts_ent_transbility_b.carno AS EQUIPMENT_GID , CAST('' as varchar(50)) EQUIPMENT_TYPE_GID,"
				+ " ts_ent_transbility_b.pk_driver AS DRIVER_GID , CAST('' as varchar(50))  DRIVER_PHONE,"
				+ " CAST('' as varchar(50)) DRIVER_ID_CARD ,CAST('' as varchar(50)) S_LANE_GID " + " FROM ts_entrust "
				+ " LEFT JOIN ts_ent_transbility_b ON ts_entrust.pk_entrust=ts_ent_transbility_b.pk_entrust AND ts_ent_transbility_b.dr=0 "
				+ " WHERE ts_entrust.dr=0  AND ts_entrust.vbillstatus not in (0,24) AND isnull(ts_ent_transbility_b.def10,'N') <>'Y'  "
				+ " AND ts_ent_transbility_b.carno IS NOT null AND ts_entrust.pk_customer='f53be58b7d3f445c8586e4aefb08b317'   ";

		// 记录成功的运力信息和失败的运力信息的明细主键值
		List<String> listSuccesPKEntTransB = new ArrayList<String>();
		List<String> listFailedPKEntTransB = new ArrayList<String>();

		// 开始执行时间
		Calendar start = Calendar.getInstance();

		// 记录主键值
		String strPkEntTransB = "";

		try {

			// webservice路径
			String endpoint = jobDefVO.getUrl();

			// 指定调用接口名称及命名空间
			String operationName = "UpdateShipmentTransInfo";
			String targetNamespace = "http://tempuri.org/";

			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endpoint));

			// 解决错误：服务器未能识别 HTTP 头 SOAPAction 的值
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(targetNamespace + operationName);

			// WSDL里面描述的接口名称
			call.setOperationName(new QName(targetNamespace, operationName));

			// 设置方法参数
			call.addParameter(new QName(targetNamespace, "signData"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName(targetNamespace, "usergid"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);

			// 设置返回类型
			call.setReturnType(org.apache.axis.encoding.XMLType.XSD_SCHEMA);
			call.setProperty(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED, Boolean.FALSE);

			// 获取数据
			List<HashMap> mapEntTransbilityList = NWDao.getInstance().queryForList(sql, HashMap.class);
			if(mapEntTransbilityList.size() == 0){
				return;
			}
			logger.info("共查询到" + mapEntTransbilityList.size() + "条运力信息需要进行导出操作。");
			if (mapEntTransbilityList != null && mapEntTransbilityList.size() > 0) {
				for (int i = 0; i < mapEntTransbilityList.size(); i++) {
					HashMap payRowMap = mapEntTransbilityList.get(i);
					String strid = String.valueOf(payRowMap.get("id"));
					strPkEntTransB = String.valueOf(payRowMap.get("pk_ent_trans_bility_b"));
					String strShipmentGID = String.valueOf(payRowMap.get("shipment_gid"));
					String strSpGID = String.valueOf(payRowMap.get("sp_gid"));
					String strEuqipmentGID = String.valueOf(payRowMap.get("equipment_gid"));
					String strEquipmentTypeGID = String.valueOf(payRowMap.get("equipment_type_gid"));
					String strDriverID = String.valueOf(payRowMap.get("driver_gid"));
					String strDriverPhone = String.valueOf(payRowMap.get("driver_phone"));
					String strDriverIDCard = String.valueOf(payRowMap.get("driver_id_card"));
					String strLaneGID = String.valueOf(payRowMap.get("s_lane_gid"));

					// 准备参数数据
					Object[] opTransArgs = new Object[] { "[{\"ID\":\"" + strid + "\",\"SHIPMENT_GID\":\""
							+ strShipmentGID + "\",\"SP_GID\":\"" + strSpGID + "\"," + "\"EQUIPMENT_GID\":\""
							+ strEuqipmentGID + "\",\"EQUIPMENT_TYPE_GID\":\"" + strEquipmentTypeGID + "\","
							+ "\"DRIVER_ID\":\"" + strDriverID + "\",\"DRIVER_PHONE\":\"" + strDriverPhone + "\","
							+ "\"DRIVER_ID_CARD\":\"" + strDriverIDCard + "\",\"S_LANE_GID\":\"" + strLaneGID + "\"}]",
							"DS.ADMIN" };

					// 调用WS服务
					Object result = call.invoke(opTransArgs);
					Schema schema = (Schema) result;
					MessageElement[] msgele = schema.get_any();

					// 截取返回值的信息，判断是否成功
					String strResultDeatil = msgele[2].getAsString();
					int iStart = strResultDeatil.indexOf("<ns1:IsSucc>");
					int iStop = strResultDeatil.indexOf("</ns1:IsSucc>");
					String strReturn = strResultDeatil.substring(iStart + 12, iStop);
					boolean isSuccess = Boolean.parseBoolean(strReturn);

					// 返回成功加入成功列表，否则返回失败列表
					if (isSuccess) {
						if (!listSuccesPKEntTransB.contains(strPkEntTransB)) {
							listSuccesPKEntTransB.add(strPkEntTransB);
						}
					} else {
						// 获取失败原因，加入失败列表
						int iMsgStart = strResultDeatil.indexOf("<ns1:Descr>");
						int iMsgStop = strResultDeatil.indexOf("</ns1:Descr>");
						String strMsgReturn = strResultDeatil.substring(iMsgStart + 11, iMsgStop);

						//获取错误信息，暂时不作处理
						strMsgReturn = StringEscapeUtils.unescapeHtml(strMsgReturn);

						if (!listFailedPKEntTransB.contains(strPkEntTransB)) {
							listFailedPKEntTransB.add(strPkEntTransB);
						}
					}
				}
			}
		} catch (Exception e) {
			// 记录失败的ID
			if (!listFailedPKEntTransB.contains(strPkEntTransB)) {
				listFailedPKEntTransB.add(strPkEntTransB);
			}
			e.printStackTrace();
		}

		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSuccesPKEntTransB.size() + "条数据，失败"
				+ listFailedPKEntTransB.size() + "条数据。");

		logger.info("进行导出后处理。");

		// 更新统计信息及状态
		afterSyncData(jobDefVO, listSuccesPKEntTransB, listFailedPKEntTransB);

		logger.info("-------------------导出运力信息任务执行完毕-------------------");
	}

	/**
	 * 判断任务是否同步数据 songf 2015-11-14
	 * 
	 * @param jobDefVO
	 *            同步任务信息
	 */
	private boolean isNeedSyncData(final JobDefVO jobDefVO) {

		// 设置要返回的值
		boolean bReturn = false;

		// 执行类型，如果 ExecType =1 是间隔，2是定时
		if (jobDefVO.getExec_type() == 1) {
			Date dateLastSyncDate = DateUtils.parseString(jobDefVO.getDef1());
			Date dataCurrent = new Date();

			long lInterval = dataCurrent.getTime() - dateLastSyncDate.getTime();
			long iIntervalMin = lInterval / (1000);
			// 如果当前时间-最后一次同步时间 大于间隔时间，就进行一次同步
			if (iIntervalMin > jobDefVO.getJob_interval()) {
				bReturn = true;
			}
		}
		// 定时同步
		else if (jobDefVO.getExec_type() == 2) {

			// 获取指定运行时间，并拆分为时分秒
			String strExecTime = jobDefVO.getExec_time();
			int iSecIndex = strExecTime.lastIndexOf(':');
			int iExecSec = Integer.parseInt(strExecTime.substring(iSecIndex + 1, strExecTime.length()));

			strExecTime = strExecTime.substring(0, iSecIndex);
			int iMinIndex = strExecTime.lastIndexOf(':');
			int iExecHour = Integer.parseInt(strExecTime.substring(0, iMinIndex));
			int iExecMin = Integer.parseInt(strExecTime.substring(iMinIndex + 1, strExecTime.length()));

			// 获取当前时间 如果当前时间-最后一次同步时间 大于间隔时间，就进行一次同步
			Date dCurrDate = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strCurrent = fmt.format(dCurrDate);

			int indexHour = strCurrent.indexOf(':');
			int iCurrHour = Integer.parseInt(strCurrent.substring(indexHour - 2, indexHour));
			int iCurrMin = DateUtils.getMinute();
			int iCurrSec = DateUtils.getSecond();

			if (iCurrHour > iExecHour) {
				bReturn = true;
			} else if (iCurrHour == iExecHour && iCurrMin > iExecMin) {
				bReturn = true;
			} else if (iCurrHour == iExecHour && iCurrMin == iExecMin && iCurrSec > iExecSec) {
				bReturn = true;
			}

		}

		return bReturn;
	}

	/**
	 * 数据同步后处理，更新 ts_ent_trans_bility_b的状态 ，更新 任务的统计信息 songf 2015-11-18
	 * 
	 * @param jobDefVO
	 *            同步任务信息
	 * @param listSucces
	 *            同步成功明细主键列表
	 * @param listFailed
	 *            同步失败明细主键列表
	 */
	private void afterSyncData(final JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed) {
		try {
			if(listSucces.size() == 0 && listFailed.size() == 0){
				return;
			}
			List<SuperVO> entTransUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_ent_trans_bility_b 将同步成功的明细def10改为Y'
			if (listSucces.size() > 0) {
				EntTransbilityBVO[] entTrasBVOSuccs = NWDao.getInstance()
						.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_ent_trans_bility_b in "
								+ NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));

				if (entTrasBVOSuccs != null || entTrasBVOSuccs.length != 0) {
					for (EntTransbilityBVO entTransBVOSucc : entTrasBVOSuccs) {

						// 更新状态为已同步
						entTransBVOSucc.setDef10("Y");
						entTransBVOSucc.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						entTransUpdateSuccessList.add(entTransBVOSucc);
					}
				}
			}
			// 保存数据
			if (entTransUpdateSuccessList != null && entTransUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(entTransUpdateSuccessList);
			}
			// 先更新ts_ent_trans_bility_b 将同步失败的明细def10改为N
			List<SuperVO> entTransUpdateFailedList = new ArrayList<SuperVO>();
			if (listFailed.size() > 0) {
				EntTransbilityBVO[] entTrasBVOFaileds = NWDao.getInstance()
						.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_ent_trans_bility_b in "
								+ NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (entTrasBVOFaileds != null && entTrasBVOFaileds.length != 0) {
					for (EntTransbilityBVO entTransBVOFailed : entTrasBVOFaileds) {

						// 更新状态为同步失败
						entTransBVOFailed.setDef10("N");
						entTransBVOFailed.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						entTransUpdateFailedList.add(entTransBVOFailed);
					}
				}
			}
			// 保存数据
			if (entTransUpdateFailedList != null && entTransUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(entTransUpdateFailedList);
			}
			
			//如果是间隔同步要修改同步时间
			if(jobDefVO.getExec_type() == 1){
				Date dCurrDate = new Date();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String strCurrent = fmt.format(dCurrDate);
				jobDefVO.setDef1(strCurrent);
			}
			// 获取任务同步次数,更新数据 def2为同步次数
			if (jobDefVO.getDef2() == null) {
				jobDefVO.setDef2("0");
			}
			int iSyncCount = Integer.parseInt(jobDefVO.getDef2());
			iSyncCount++;
			jobDefVO.setDef2(String.valueOf(iSyncCount));

			// 获取总同步数据量，更新数据 def3为同步成功条数
			if (jobDefVO.getDef3() == null) {
				jobDefVO.setDef3("0");
			}
			long lSyncSuccessRecord = Long.parseLong(jobDefVO.getDef3());
			lSyncSuccessRecord += (long)listSucces.size();
			jobDefVO.setDef3(String.valueOf(lSyncSuccessRecord));

			// 获取总同步数据量，更新数据 def4为同步失败条数
			if (jobDefVO.getDef4()== null) {
				jobDefVO.setDef4("0");
			}
			long lSyncFailedRecord = Long.parseLong(jobDefVO.getDef4());
			lSyncFailedRecord += (long)listFailed.size();
			jobDefVO.setDef4(String.valueOf(lSyncFailedRecord));

			jobDefVO.setStatus(VOStatus.UPDATED);
			
			// 保存数据
			NWDao.getInstance().saveOrUpdate(jobDefVO);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建http请求的定时任务
	 * 
	 * @param otVO
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<TrackVO> createHttpTask(final JobDefVO jobDefVO, Map<String, Object> paramMap) {

		return null;
	}

	/**
	 * 创建webservice请求的任务
	 * 
	 * @param otVO
	 * @return
	 */
	private List<TrackVO> createWebserviceTask(final JobDefVO otVO, Map<String, Object> paramMap) {
		return null;
	}

	public void before(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}

	public void after(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}
}
