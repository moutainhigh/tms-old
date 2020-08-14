package com.tms.service.job.edi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.JobDefVO;

import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntrustVO;
import org.apache.log4j.Logger;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.types.Schema;
import java.text.ParseException;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * 裕络发货单跟踪信息导出，调用对方web service，将数据导出给第三方系统
 * 
 * @author songf
 * @Date 2015年11月14日 下午15:34:28
 *
 */
public class ExportInvoiceTrackingJobService implements IJobService {
	static Logger logger = Logger.getLogger(ExportInvoiceTrackingJobService.class);

	@SuppressWarnings("rawtypes")
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出发货单跟踪信息-------------------");
		if (jobDefVO == null) {
			return;
		}
		logger.info("判断是否需要同步数据");
		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");

		// 记录提供同步成功与失败
		List<String> listSuccesDeliPKInvoice = new ArrayList<String>();
		List<String> listFailedDeliPKInvoice = new ArrayList<String>();

		logger.info("开始进行发货单提货数据同步操作。");

		// 同步提货信息
		syncDeliArrivedData(jobDefVO, listSuccesDeliPKInvoice, listFailedDeliPKInvoice);

		logger.info("发货单提货数据同步操作结束。");

		// 更新统计信息及状态
		afterDeliArrivedSyncData(jobDefVO, listSuccesDeliPKInvoice, listFailedDeliPKInvoice);

		logger.info("-------------------导出发货单跟踪信息任务执行完毕-------------------");
	}

	/**
	 * 同步提货到货信息 songf 2015-11-18
	 * 
	 * @param jobDefVO
	 *            执行任务
	 * @param listSucces
	 *            同步成功的主键列表
	 * @param listFialed
	 *            同步失败的主键列表
	 */
	private void syncDeliArrivedData(JobDefVO jobDefVO, List<String> listSucces, List<String> listFialed) {
		// 查询发货单提供信息
		String sql = "";

		sql = "SELECT TOP 1000 ts_invoice.pk_invoice,ts_invoice.cust_orderno AS ID,ts_invoice.cust_orderno AS SHIPMENT_GID,deli_detail_addr AS SRC_STOP_ADDRESS,"
				+ " act_deli_date AS SRC_STOP_D_DATE ,act_deli_date AS SRC_STOP_A_DATE,CAST('' as varchar(50)) SRC_REMARK,"
				+ " arri_detail_addr  DEST_STOP_ADDRESS,act_arri_date DEST_STOP_D_DATE,act_arri_date DEST_STOP_A_DATE,CAST('' as varchar(50)) DEST_REMARK"
				+ " from ts_invoice where act_deli_date is not null and ts_invoice.vbillstatus in(3,4,5,6)  AND  isnull(ts_invoice.def10,'N') <>'Y' "
				+ " AND ts_invoice.dr=0 AND ts_invoice.pk_customer='f53be58b7d3f445c8586e4aefb08b317' ";

		// 开始执行时间
		Calendar start = Calendar.getInstance();
		String strPkInvoice = "";
		try {
			// webservice路径
			String endpoint = jobDefVO.getUrl();

			// 指定调用接口名称及命名空间
			String operationName = "ArrivalShipmentAllStop";
			String targetNamespace = "http://tempuri.org/";

			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(endpoint);

			// 解决错误：服务器未能识别 HTTP 头 SOAPAction 的值
			call.setUseSOAPAction(true);
			call.setSOAPActionURI("http://tempuri.org/ArrivalShipmentAllStop");

			// WSDL里面描述的接口名称
			call.setOperationName(new QName(targetNamespace, operationName));

			// 设置方法参数
			call.addParameter(new QName(targetNamespace, "signData"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName(targetNamespace, "usergid"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);

			// 设置返回类型
			call.setReturnType(org.apache.axis.encoding.XMLType.XSD_SCHEMA);

			// 获取数据
			List<HashMap> mapInvoiceTackingList = NWDao.getInstance().queryForList(sql, HashMap.class);
			logger.info("共查询到" + mapInvoiceTackingList.size() + "条提货信息");
			if(mapInvoiceTackingList.size() == 0){
				return;
			}
			if (mapInvoiceTackingList != null && mapInvoiceTackingList.size() > 0) {
				for (int i = 0; i < mapInvoiceTackingList.size(); i++) {
					HashMap payRowMap = mapInvoiceTackingList.get(i);
					String strID = String.valueOf(payRowMap.get("id"));
					strPkInvoice = String.valueOf(payRowMap.get("pk_invoice"));
					String strShipmentID = String.valueOf(payRowMap.get("shipment_gid"));
					String strSrcStopAdderss = String.valueOf(payRowMap.get("src_stop_address"));
					String strSrcStopADate = String.valueOf(payRowMap.get("src_stop_a_date"));
					String strSrcStopDDate = String.valueOf(payRowMap.get("src_stop_d_date"));
					String strSrcRemark = String.valueOf(payRowMap.get("src_remark"));
					String strDestStopAddress = String.valueOf(payRowMap.get("dest_stop_address"));
					String strDestStopADate = String.valueOf(payRowMap.get("dest_stop_a_date"));
					String strDestStopDDate = String.valueOf(payRowMap.get("dest_stop_d_date"));
					String strDestRemark = String.valueOf(payRowMap.get("dest_remark"));
					
					logger.info("委托单" + strShipmentID + "strSrcStopADate:" +strSrcStopADate+ "strSrcStopDDate:" +strSrcStopDDate+ "strDestStopADate:" +strDestStopADate+ "strDestStopDDate:" +strDestStopDDate);

					Object[] opTransArgs = new Object[] { "[{\"ID\":\"" + strID + "\",\"SHIPMENT_GID\":\""
							+ strShipmentID + "\",\"SRC_STOP_ADDRESS\":\"" + strSrcStopAdderss + "\","
							+ "\"SRC_STOP_A_DATE\":\"" + strSrcStopADate + "\",\"SRC_STOP_D_DATE\":\"" + strSrcStopDDate
							+ "\",\"SRC_REMARK\":\"" + strSrcRemark + "\"," + "\"DEST_STOP_ADDRESS\":\""
							+ strDestStopAddress + "\",\"DEST_STOP_A_DATE\":\"" + strDestStopADate
							+ "\",\"DEST_STOP_D_DATE\":\"" + strDestStopDDate + "\"," + "\"DEST_REMARK\":\""
							+ strDestRemark + "\"}]", "DS.ADMIN" };

					// 调用ws方法
					Object result = call.invoke(opTransArgs);

					Schema schema = (Schema) result;
					MessageElement[] msgele = schema.get_any();

					// 截取返回值的信息，判断是否成功
					String strResultDeatil = msgele[2].getAsString();
					int iStart = strResultDeatil.indexOf("<ns1:IsSucc>");
					int iStop = strResultDeatil.indexOf("</ns1:IsSucc>");
					String strReturn = strResultDeatil.substring(iStart + 12, iStop);

					//判断是否成功 
					boolean isSuccess = Boolean.parseBoolean(strReturn);
		
					// 返回成功加入成功列表，否则返回失败列表
					if (isSuccess) {
						if (!listSucces.contains(strPkInvoice)) {
							listSucces.add(strPkInvoice);
						}
					} else {
						//获取失败原因，加入失败列表
						int iMsgStart = strResultDeatil.indexOf("<ns1:Descr>");
						int iMsgStop = strResultDeatil.indexOf("</ns1:Descr>");
						String strMsgReturn = strResultDeatil.substring(iMsgStart + 11, iMsgStop);

						//获取错误信息，暂时不作处理
						strMsgReturn = StringEscapeUtils.unescapeHtml(strMsgReturn);
						if (!listFialed.contains(strPkInvoice)) {
							listFialed.add(strPkInvoice);
						}
					}
				}
			}
		} catch (Exception e) {
			// 记录失败的ID
			if (!listFialed.contains(strPkInvoice)) {
				listFialed.add(strPkInvoice);
			}
			e.printStackTrace();
		}
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFialed.size() + "条数据。");

	}

	/**
	 * 判断任务是否同步数据 songf 2015-11-14
	 * 
	 * @param jobDefVO
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
	 * 同步提货到货后处理 songf 2015-11-18
	 * 
	 * @param jobDefVO
	 *            执行任务
	 * @param listSucces
	 *            同步成功的主键列表
	 * @param listFialed
	 *            同步失败的主键列表
	 */
	private void afterDeliArrivedSyncData(final JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed) {
		try {
			
			if(listSucces.size() == 0 && listFailed.size() == 0){
				return;
			}
			
			List<SuperVO> updateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_invoice表的发送状态
			if (listSucces.size() > 0) {
				InvoiceVO[] invVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
						"pk_invoice in " + NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (invVOSuccs != null && invVOSuccs.length != 0) {
					for (InvoiceVO invVOSucc : invVOSuccs) {
						
						// 更新提货状态为已同步
						invVOSucc.setDef10("Y");
						invVOSucc.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						updateSuccessList.add(invVOSucc);
					}
				}
			}
			// 保存数据
			if(updateSuccessList!=null&&updateSuccessList.size()>0)
			{
			    NWDao.getInstance().saveOrUpdate(updateSuccessList);
			}

			List<SuperVO> updateFailedList = new ArrayList<SuperVO>();
			
			// 更新失败后的处理信息
			if (listFailed.size() > 0) {
				InvoiceVO[] invVOFaileds = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
						"pk_invoice in " + NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (invVOFaileds != null && invVOFaileds.length != 0) {
					for (InvoiceVO invVOFailed : invVOFaileds) {
						invVOFailed.setStatus(VOStatus.UPDATED);

						// 更新提货状态为已同步
						invVOFailed.setDef10("N");

						// 保存更新的状态
						updateFailedList.add(invVOFailed);
					}
				}
			}
			// 保存数据
			if (updateFailedList != null && updateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(updateFailedList);
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
			lSyncFailedRecord +=  (long)listFailed.size();
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
