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
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.ReceiveDetailVO;

/**
 * @author XIA 2016 5 30
 * 
 * 佳达同步应付调整信息接口
 * 
 */
public class ExportJdPDOPJobService implements IJobService {

	static Logger logger = Logger.getLogger(ExportJdPDOPJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	@SuppressWarnings({ "unchecked" })
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出应付调整信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有应付信息需要同步");

		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		String sql = "SELECT top 50 ts_pay_detail.vbillno as entrustOrderNo,ts_entrust.orderno AS OrderNo,'TMS' AS ServiceType,ts_carrier.carr_code AS BillingHeader, "
				+ "ts_pay_detail.confirm_time AS  FinishedTime,ts_expense_type.code AS  FeeCode, "
				+ "ts_pay_detail_b.price AS UnitPrice,cvtc.name AS  VariableName1,ts_pay_detail_b.bill_value AS VariableQuantity1,  "
				+ "ts_pay_detail_b.amount AS Amount,cat.display_name AS BillingType,ts_pay_detail.tax_rate AS BillingTypeFax, "
				+ "ts_pay_detail.currency AS CurrencyCode,cvtc.code AS FeeUnit,cvtc.name AS FeeUnitName,ts_pay_detail_b.memo AS  Remark , "
				+ "father_corp.corp_code AS TenantId, nw_corp.def2 AS DepartmentId ,ts_invoice.item_name AS TeamId,ts_pay_detail.confirm_time AS ConfirmedDate "
				+ "FROM ts_pay_detail WITH(nolock) "
				+ "INNER JOIN ts_entrust WITH(nolock) ON ts_entrust.vbillno = ts_pay_detail.entrust_vbillno AND ts_entrust.dr = 0 AND ts_entrust.edi_flag='Y'  "
				+ "LEFT JOIN nw_corp  WITH(nolock) ON ts_pay_detail.pk_corp=nw_corp.pk_corp "
				+ "LEFT JOIN nw_corp father_corp  WITH(nolock) ON nw_corp.fathercorp=father_corp.pk_corp "
				+ "LEFT JOIN ts_pay_detail_b WITH(nolock) ON ts_pay_detail_b.pk_pay_detail= ts_pay_detail.pk_pay_detail AND ts_pay_detail_b.dr = 0   "
				+ "LEFT JOIN cus_valuation_type_convert cvtc WITH(nolock) ON cvtc.value=ts_pay_detail_b.valuation_type  "
				+ "LEFT JOIN ts_expense_type WITH(nolock) ON ts_pay_detail_b.pk_expense_type=ts_expense_type.pk_expense_type  AND ts_expense_type.dr = 0  "
				+ "LEFT JOIN nw_data_dict_b cat WITH(nolock) ON cat.pk_data_dict='8c6af0da0e524d52b3db7d0cb829d8e3' AND ts_pay_detail.tax_cat=cat.value "
				+ "LEFT JOIN ts_invoice WITH(nolock) ON ts_entrust.invoice_vbillno=ts_invoice.vbillno  "
				+ "LEFT JOIN nw_dept WITH(nolock) ON nw_dept.pk_dept=ts_invoice.pk_dept  "
				+ "LEFT JOIN ts_carrier WITH(nolock) ON ts_carrier.pk_carrier=ts_pay_detail.pk_carrier	"
				+ "WHERE ts_pay_detail.dr !=0   AND ts_pay_detail.vbillstatus='41' AND ts_pay_detail.pay_type='0' AND ISNULL(ts_pay_detail.edi_flag,'N') <>'Y'  ";

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
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				"vbillno in "+ NWUtils.buildConditionString(retbillnos.toArray(new String[retbillnos.size()])));
		if(payDetailVOs == null || payDetailVOs.length == 0){
			logger.info("接口返回数据单号不正确！");
			return;
		}
		for(PayDetailVO payDetailVO : payDetailVOs){
			for(String billno : listSucces){
				if(payDetailVO.getVbillno().equals(billno)){
					payDetailVO.setStatus(VOStatus.UPDATED);
					payDetailVO.setEdi_flag(UFBoolean.TRUE);
				}
			}
			for(Map<String,String> map : listFailed){
				if(payDetailVO.getVbillno().equals(map.get("orderNo"))){
					payDetailVO.setStatus(VOStatus.UPDATED);
					payDetailVO.setEdi_flag(UFBoolean.FALSE);
					payDetailVO.setEdi_msg(map.get("errorMessage"));
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(payDetailVOs);
		
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
