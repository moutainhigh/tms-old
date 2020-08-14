package com.tms.service.job.edi.route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.JobDefVO;

import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.base.SupplierVO;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.tms.service.job.edi.route.client.RouteClient;
import com.tms.service.job.edi.route.client.RouteVO;
/**
 * ord同步到达和离开时间
 * @author XIA
 *
 */
public class ExportDeliAndArriJobService implements IJobService {
	static Logger logger = Logger.getLogger(ExportDeliAndArriJobService.class);

	@SuppressWarnings("rawtypes")
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出ord同步到达和离开时间信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有信息需要同步");

		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");

		// soap头
		String strHeader = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cais=\"http://www.ortec.com/CAIS\"><soapenv:Header/>"
				+ "<soapenv:Body><cais:SendMessage><cais:message><![CDATA[<comtec-mobile-communication version='2010'>";
		// soap尾
		String strTail = "</comtec-mobile-communication>]]></cais:message><cais:commandName>ImportRealization</cais:commandName></cais:SendMessage></soapenv:Body></soapenv:Envelope>";

		// 查询同步信息
		String sql = "SELECT ts_ent_line_b.act_arri_date AS starttime,ts_ent_line_b.act_leav_date AS finishtime, "
				+ " 'finished' AS planning_status,ts_vehicle_trips_b.action_id "
				+ " FROM ts_entrust WITH(NOLOCK) "
				+ " LEFT JOIN ts_ent_line_b WITH(NOLOCK) ON ts_entrust.pk_entrust = ts_ent_line_b.pk_entrust "
				+ " LEFT JOIN ts_ent_seg_b ON ts_entrust.pk_entrust = ts_ent_seg_b.pk_entrust "
				+ " LEFT JOIN ts_segment ON ts_segment.pk_segment = ts_ent_seg_b.pk_segment "
				+ " LEFT JOIN ts_vehicle_trips_b ON ts_vehicle_trips_b.pk_segment = ts_segment.vbillno "
				+ " WHERE isnull(ts_ent_line_b.dr,0)=0 AND isnull(ts_vehicle_trips_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0  "
				+ " AND isnull(ts_ent_seg_b.dr,0)=0 "
				+ " AND ts_entrust.vbillstatus IN (22,23) "
				+ " AND ts_ent_line_b.arrival_flag='Y' "
				+ " AND action_id IS NOT NULL "
				+ " AND ts_vehicle_trips_b.action_kind = 'pickup' "
				+ " AND ISNULL(ts_ent_line_b.def1,'N') ='N'";

		// 记录成功的供应商及能力信息和失败的供应商及能力信息的明细主键值
		List<String> listSucces = new ArrayList<String>();
		List<String> listFailed = new ArrayList<String>();

		// 开始执行时间
		Calendar start = Calendar.getInstance();

	
		try {
			// 获取数据
			List<HashMap> mapTrackingList = NWDao.getInstance().queryForList(sql, HashMap.class);
			logger.info("共查询到" + mapTrackingList.size() + "条供应商信息需要进行同步操作。");
			if (mapTrackingList != null && mapTrackingList.size() > 0) {
				// 创建message的xml片段
				Document document = DocumentHelper.createDocument();
				Element actions = document.addElement("actions");
				List<String> pk_ent_line_bs = new ArrayList<String>();
				for(HashMap map : mapTrackingList){
					Element action = actions.addElement("action");
					// 记录主键值
					String pk_ent_line_b = map.get("pk_ent_line_b").toString();
					pk_ent_line_bs.add(pk_ent_line_b);
					String id = map.get("id").toString();
					action.addElement("id").addText(id);
					String planning_status = map.get("planning_status").toString();
					action.addElement("planning_status").addText(planning_status);
					Element realized_times = actions.addElement("realized_times");
					String starttime = map.get("starttime").toString();
					realized_times.addElement("starttime").addText(starttime);
					if(planning_status.equals("finished")){
						String finishtime = map.get("finishtime").toString();
						realized_times.addElement("finishtime").addText(finishtime);
					}
				}
				Element rootMessage = document.getRootElement();
				String strMessage = rootMessage.asXML();
				//调用JNI接口
				RouteClient client = new RouteClient();
			    RouteVO routeVO = new RouteVO();
				routeVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(routeVO);
				routeVO.setCommandname("ImportDeliAndArri");
				routeVO.setMessage(strMessage);
				NWDao.getInstance().saveOrUpdate(routeVO);
				String returnMsg =client.SendMessage(routeVO.getPrimaryKey(), "yuliuziduan");
				
				if ("TRUE".equalsIgnoreCase(returnMsg)){
					String instersql="INSERT INTO edi_his_route SELECT * FROM edi_route WITH(NOLOCK) WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
					String delsql="DELETE edi_route WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
					String updatesql1="UPDATE ts_ent_line_b SET def1='Y'  WHERE pk_ent_line_b in " + NWUtils.buildConditionString(pk_ent_line_bs);
					NWDao.getInstance().update(instersql);
					NWDao.getInstance().update(delsql);
					NWDao.getInstance().update(updatesql1);
				}else{
					String updatesql="UPDATE edi_route SET syncexp_flag='Y' , syncexp_memo='"+returnMsg+ "' WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
					NWDao.getInstance().update(updatesql);
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");

		logger.info("进行导出后处理。");

		// 更新统计信息及状态
		afterSyncData(jobDefVO, listSucces, listFailed);

		logger.info("-------------------导出信息任务执行完毕-------------------");
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
	 * 数据同步后处理，更新 ts_supplier的状态 ，更新 任务的统计信息 songf 2015-11-18
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
			List<SuperVO> supplierUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_supplier 将同步成功的明细def1 改为Y
			if (listSucces.size() > 0) {
				SupplierVO[] supplierVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(SupplierVO.class,
						"pk_supplier in "
								+ NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (supplierVOSuccs != null && supplierVOSuccs.length > 0) {
					for (SupplierVO supplierVOSucc : supplierVOSuccs) {

						// 更新状态为已同步
						supplierVOSucc.setDef1("Y");
						supplierVOSucc.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						supplierUpdateSuccessList.add(supplierVOSucc);
					}
				}
			}
			// 保存数据
			if (supplierUpdateSuccessList != null && supplierUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(supplierUpdateSuccessList);
			}
			
			List<SuperVO> supplierUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_supplier 将同步失败的明细def1 改为N
			if(listFailed.size() > 0){
				SupplierVO[] supplierVOFaileds = NWDao.getInstance()
						.queryForSuperVOArrayByCondition(SupplierVO.class, "pk_supplier in "
								+ NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (supplierVOFaileds != null && supplierVOFaileds.length != 0) {
					for (SupplierVO supplierVOFailed : supplierVOFaileds) {
						
						// 更新状态为同步失败
						supplierVOFailed.setDef1("N");
						supplierVOFailed.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						supplierUpdateFailedList.add(supplierVOFailed);
					}
				}
			}			
			// 保存数据
			if (supplierUpdateFailedList != null && supplierUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(supplierUpdateFailedList);
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
	@SuppressWarnings({ "unused" })
	private List<TrackVO> createHttpTask(final JobDefVO jobDefVO, Map<String, Object> paramMap) {

		return null;
	}

	/**
	 * 创建webservice请求的任务
	 * 
	 * @param otVO
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<TrackVO> createWebserviceTask(final JobDefVO otVO, Map<String, Object> paramMap) {
		return null;
	}

	public void before(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}

	public void after(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}
	
	public String transToXML(String strReturnMsg,boolean trueOrFalse){
		// 创建message的xml片段
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("result");
		if(trueOrFalse){
			root.addElement("success").addText("true");
		}else{
			root.addElement("success").addText("false");
		}
		root.addElement("msg").addText(strReturnMsg);
		return document.asXML();
	}
}
