package com.tms.service.job.edi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.json.JacksonUtils;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.JobDefVO;

import com.tms.services.tmsStandardApi.HttpRequestUtils;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.te.EntTrackingVO;

/**
 * @author XIA 2016 5 30
 * 
 * 佳达同步跟踪信息接口
 * 
 */
public class ExportJdTrackingJobService implements IJobService {

	static Logger logger = Logger.getLogger(ExportJdTrackingJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	@SuppressWarnings({ "unchecked" })
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出跟踪信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有跟踪信息需要同步");

		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		String sql = "SELECT top 50 ts_inv_tracking.pk_inv_tracking AS vbillno, ts_invoice.orderno AS OrderNo, ts_inv_tracking.tracking_time AS ETC,ts_inv_tracking.tracking_time AS ATC, "
				+ " ts_inv_tracking.tracking_status  AS NodeType,nw_data_dict_b.display_name  AS OperationContent,  "
				+ " 'TMS' AS ServiceType,ts_trans_type.def1 AS SubServiceCode,  "
				+ " '' AS SubServiceId,nw_user.user_code AS Operator,nw_dept.dept_code AS OperationDepartment ,nw_dept.def2 AS Manager,  "
				+ " ts_inv_tracking.exp_memo AS ExceptionContent,ts_inv_tracking.exp_type AS ExceptionType,'' AS ExceptionResponsiblity,  "
				+ " '' AS ExceptionFeeResponsibility,'' AS  ExceptionCreatedTime,'' AS ExceptionClosedTime,'' AS Count, ts_inv_tracking.pk_inv_tracking AS  EntrustOrderNo  "
				+ " FROM ts_inv_tracking WITH(nolock)  "
				+ " LEFT JOIN ts_invoice WITH(nolock) ON ts_invoice.vbillno = ts_inv_tracking.invoice_vbillno AND isnull(ts_invoice.dr,0)=0 AND  ts_invoice.invoice_origin=5  "
				+ " LEFT JOIN ts_entrust WITH(nolock) ON ts_entrust.vbillno = ts_inv_tracking.entrust_vbillno AND ts_entrust.dr = 0   "
				+ " LEFT JOIN ts_trans_type WITH(nolock) ON ts_entrust.pk_trans_type = ts_trans_type.pk_trans_type AND ts_trans_type.dr = 0  "
				+ " LEFT JOIN nw_user WITH(nolock) ON ts_inv_tracking.create_user = nw_user.pk_user AND nw_user.dr = 0  "
				+ " LEFT JOIN nw_dept WITH(nolock) ON nw_user.pk_dept = nw_dept.pk_dept AND nw_dept.dr = 0  "
				+ " LEFT JOIN nw_data_dict_b  WITH(nolock) ON ts_inv_tracking.tracking_status=nw_data_dict_b.value AND nw_data_dict_b.pk_data_dict='57141b8e05534d4eb3750d2f0ab29816'  "
				+ " WHERE ts_inv_tracking.dr=0   AND isnull(ts_inv_tracking.edi_flag,'N')<>'Y'   ";
		// 记录成功和失败信息
		List<String> listSucces = new ArrayList<String>();
		List<Map<String, String>> listFailed = new ArrayList<Map<String, String>>();
		List<String> retbillnos = new ArrayList<String>();

		// 开始执行时间
		Calendar start = Calendar.getInstance();
		// webservice路径
		String url = jobDefVO.getUrl();
		String userParam = jobDefVO.getUsername_param();
		String user = jobDefVO.getUsername();
		String passwordParam = jobDefVO.getPassword_param();
		String password = jobDefVO.getPassword();
		List<Map<String, Object>> queryResult = NWDao.getInstance().queryForList(sql);
		if (queryResult == null || queryResult.size() == 0) {
			logger.info("同步失败，没有查询到需要同步的数据！");
			return;
		}
		String jsonParam = JacksonUtils.writeValueAsString(queryResult);
		logger.info("开始同步数据，共" + queryResult.size() + "条！");
		String retMsgJson = HttpRequestUtils.httpPost(url, userParam, user, passwordParam, password, jsonParam);
		logger.info("解析接口返回数据！");
		if (StringUtils.isBlank(retMsgJson)) {
			logger.info("接口没有返回数据！");
			return;
		}
		List<Map<String, Object>> retMsg = JacksonUtils.readValue(retMsgJson, ArrayList.class);
		if (retMsg == null) {
			logger.info("接口返回数据解析失败，格式不正确！");
		}
		for (Map<String, Object> ret : retMsg) {
			boolean flag = Boolean.parseBoolean(ret.get("success").toString());
			if (flag) {
				listSucces.add((String) ret.get("entrustOrderNo"));
			} else {
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderNo", (String) ret.get("entrustOrderNo"));
				map.put("errorMessage", (String) ret.get("errorMessage"));
				listFailed.add(map);
			}
			retbillnos.add((String) ret.get("entrustOrderNo"));
		}
		if(retbillnos == null || retbillnos.size() == 0){
			logger.info("接口返回数据缺少单号！");
			return;
		}
		InvTrackingVO[] invTrackingVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvTrackingVO.class,
				"pk_inv_tracking in "+ NWUtils.buildConditionString(retbillnos.toArray(new String[retbillnos.size()])));
		if(invTrackingVOs == null || invTrackingVOs.length == 0){
			logger.info("接口返回数据单号不正确！");
			return;
		}
		for(InvTrackingVO invTrackingVO : invTrackingVOs){
			for(String billno : listSucces){
				if(invTrackingVO.getPk_inv_tracking().equals(billno)){
					invTrackingVO.setStatus(VOStatus.UPDATED);
					invTrackingVO.setEdi_flag(UFBoolean.TRUE);
				}
			}
			for(Map<String,String> map : listFailed){
				if(invTrackingVO.getPk_inv_tracking().equals(map.get("orderNo"))){
					invTrackingVO.setStatus(VOStatus.UPDATED);
					invTrackingVO.setEdi_flag(UFBoolean.FALSE);
					invTrackingVO.setEdi_msg(map.get("errorMessage"));
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(invTrackingVOs);
		
		//如果是间隔同步要修改同步时间
		if(jobDefVO.getExec_type() == 1){
			Date dCurrDate = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strCurrent = fmt.format(dCurrDate);
			jobDefVO.setDef1(strCurrent);
		}
		// 获取任务同步次数,更新数据 def2为同步次数
		if (jobDefVO.getDef2() == null) {
			long syncCount = Long.parseLong(jobDefVO.getDef2() == null ? "0" : jobDefVO.getDef2());
			jobDefVO.setDef2(String.valueOf(syncCount++));
		}

		// 获取总同步数据量，更新数据 def3为同步成功条数
		if (jobDefVO.getDef3() == null) {
			long syncSucceCount = Long.parseLong(jobDefVO.getDef3() == null ? "0" : jobDefVO.getDef3());
			jobDefVO.setDef3(String.valueOf(syncSucceCount++));
		}

		// 获取总同步数据量，更新数据 def4为同步失败条数
		if (jobDefVO.getDef4() == null) {
			long syncFailCount = Long.parseLong(jobDefVO.getDef4() == null ? "0" : jobDefVO.getDef4());
			jobDefVO.setDef4(String.valueOf(syncFailCount++));
		}

		jobDefVO.setStatus(VOStatus.UPDATED);
		// 保存数据
		NWDao.getInstance().saveOrUpdate(jobDefVO);
		
		
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");
	}

	public void after(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}

	// 判断是否需要同步信息
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

}
