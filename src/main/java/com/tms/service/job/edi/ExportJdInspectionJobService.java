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
 * 佳达同步协查接口
 * 
 */
public class ExportJdInspectionJobService implements IJobService {

	static Logger logger = Logger.getLogger(ExportJdInspectionJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	@SuppressWarnings({ "unchecked" })
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出协查信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有信息需要同步");

		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		String sql = "SELECT ts_inspection_b.pk_inspection_b,ts_inspection.code,ts_inspection.orderno,ts_inspection_b.fee_code,ts_inspection_b.fee_confirmed, "
				+ " ts_inspection_b.finished_time,ts_inspection_b.approvedby,ts_inspection_b.feedback_result,ts_inspection_b.closed_reason "
				+ " FROM ts_inspection_b "
				+ " LEFT JOIN ts_inspection ON ts_inspection_b.pk_inspection=ts_inspection.pk_inspection "
				+ " ISNULL(ts_inspection_b.edi_flag,'N')='N'  AND ts_inspection_b.dr=0 AND  ts_inspection_b.fee_confirmed='Y' ";
		// 记录成功和失败信息
		List<String> listSucces = new ArrayList<String>();
		List<Map<String, String>> listFailed = new ArrayList<Map<String, String>>();
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
		List<String> updateSqls = new ArrayList<String>();
		for (Map<String, Object> ret : retMsg){
			String assistNo = String.valueOf(ret.get("assistNo"));
			String feeCode = String.valueOf(ret.get("feeCode"));
			UFBoolean success = new UFBoolean(String.valueOf(ret.get("success")));
			String errorMessage = String.valueOf(ret.get("errorMessage"));
			String updateSql = "UPDATE ts_inspection_b SET 	ts_inspection_b.edi_flag='" + success.toString() + "',ts_inspection_b.edi_msg='" + errorMessage + "' "
					+ " FROM ts_inspection  WHERE  ts_inspection.pk_inspection=ts_inspection_b.pk_inspection "
					+ " AND  ts_inspection.code='" + assistNo + "' AND ts_inspection_b.fee_code='" + feeCode + "' ";
			updateSqls.add(updateSql);
		}
		
		if(updateSqls.size() == 0){
			return;
		}else{
			NWDao.getInstance().batchUpdate(updateSqls.toArray(new String[updateSqls.size()]));
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
