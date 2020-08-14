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
import com.tms.vo.cm.ReceiveDetailVO;

/**
 * @author XIA 2016 5 30
 * 
 * 佳达同步应收信息接口
 * 
 */
public class ExportJdRDJobService implements IJobService {

	static Logger logger = Logger.getLogger(ExportJdRDJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	@SuppressWarnings({ "unchecked" })
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出应收信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有应收信息需要同步");

		// 判断是否需要同步数据,cus_valuation_type_convert为佳达定制化表，获取计价方式对应关系
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		String sql = "SELECT TOP 50  ts_receive_detail.vbillno  as entrustOrderNo,ts_invoice.orderno AS OrderNo,   "
					+ "'TMS' AS ServiceType,ts_customer.def1 AS BillingHeader,ts_receive_detail.confirm_time AS  FinishedTime,   "
					+ "ts_expense_type.code AS FeeCode,ts_rece_detail_b.price AS UnitPrice,cvtc.name AS  VariableName1,   "
					+ "ts_rece_detail_b.bill_value AS VariableQuantity1,  ts_rece_detail_b.amount AS Amount,    "
					+ "cat.display_name AS BillingType,ts_receive_detail.tax_rate AS BillingTypeFax,   "
					+ "ts_receive_detail.currency AS CurrencyCode,cvtc.code AS FeeUnit,'' AS Remark,    "
					+ "father_corp.corp_code AS TenantId,nw_corp.def2 AS DepartmentId ,ts_invoice.item_name AS TeamId,   "
					+ "CASE WHEN start_city.parent_id='7f723f106049b5814b37a73b976306' THEN 2 ELSE 1 END AS BeginRegionType,   "
					+ "CASE WHEN start_city.parent_id='7f723f106049b5814b37a73b976306' THEN  '' ELSE start_city.def1  END AS BeginCityCode,   "
					+ "CASE WHEN start_city.parent_id='7f723f106049b5814b37a73b976306' THEN  start_city.def1 ELSE '' END  AS BeginCustomsCode,   "
					+ "CASE WHEN end_city.parent_id='7f723f106049b5814b37a73b976306' THEN 2 ELSE 1 END  AS EndRegionType,   "
					+ "CASE WHEN end_city.parent_id='7f723f106049b5814b37a73b976306' THEN  '' ELSE end_city.def1 END  AS EndCityCode,   "
					+ "CASE WHEN end_city.parent_id='7f723f106049b5814b37a73b976306' THEN  end_city.def1 ELSE '' END  AS EndCustomsCode,   "
					+ "'' AS QuotationAttributeId,ts_receive_detail.confirm_time AS ConfirmedDate,   "
					+ "ts_receive_detail.num_count,ts_receive_detail.volume_count,ts_receive_detail.fee_weight_count,ts_car_type.code   "
					+ "FROM ts_receive_detail WITH(nolock)   "
					+ "LEFT JOIN ts_rece_detail_b WITH(nolock) ON ts_rece_detail_b.pk_receive_detail = ts_receive_detail.pk_receive_detail AND ts_rece_detail_b.dr = 0   " 
					+ "LEFT JOIN ts_customer  WITH(nolock) ON ts_receive_detail.bala_customer=ts_customer.pk_customer "
					+ "LEFT JOIN nw_corp  WITH(nolock) ON ts_receive_detail.pk_corp=nw_corp.pk_corp "
					+ "LEFT JOIN nw_corp father_corp  WITH(nolock) ON nw_corp.fathercorp=father_corp.pk_corp "
					+ "LEFT JOIN cus_valuation_type_convert cvtc WITH(nolock) ON cvtc.value=ts_rece_detail_b.valuation_type    "
					+ "LEFT JOIN ts_expense_type ON ts_rece_detail_b.pk_expense_type=ts_expense_type.pk_expense_type  AND ts_expense_type.dr = 0   "
					+ "INNER JOIN ts_invoice WITH(nolock) ON ts_invoice.vbillno = ts_receive_detail.invoice_vbillno AND ts_invoice.dr = 0    "
					+ "AND ts_invoice.invoice_origin=5   "
					+ "LEFT JOIN ts_trans_bility_b WITH(nolock) ON ts_trans_bility_b.pk_invoice=ts_invoice.pk_invoice  AND ts_trans_bility_b.dr = 0    "
					+ "LEFT JOIN ts_car_type WITH(nolock) ON ts_car_type.pk_car_type=ts_trans_bility_b.pk_car_type  AND ts_car_type.dr = 0    "
					+ "LEFT JOIN nw_data_dict_b cat WITH(nolock) ON cat.pk_data_dict='8c6af0da0e524d52b3db7d0cb829d8e3' AND ts_receive_detail.tax_cat=cat.value    "
					+ "LEFT JOIN nw_dept WITH(nolock) ON nw_dept.pk_dept=ts_invoice.pk_dept   "
					+ "LEFT JOIN ts_area start_city WITH(nolock) ON  ts_invoice.deli_city=start_city.pk_area   "
					+ "LEFT JOIN ts_area end_city WITH(nolock)  ON  ts_invoice.arri_city =end_city.pk_area   "
					+ "WHERE ts_receive_detail.dr=0   AND ts_receive_detail.vbillstatus='31'    "
					+ "AND ts_receive_detail.rece_type='0' AND ISNULL(ts_receive_detail.edi_flag,'N') <>'Y'   " ;

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

		for (Map<String, Object> temp : queryResult) {
			List<Map<String, Object>> variables = new ArrayList<Map<String,Object>>();
			Map<String, Object> map1 = new HashMap<String, Object>();
			map1.put("variableName", "件数");
			map1.put("variableQuantity", temp.get("num_count"));
			variables.add(map1);
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("variableName","重量");
			map2.put("variableQuantity", temp.get("volume_count"));
			variables.add(map2);
			Map<String, Object> map3 = new HashMap<String, Object>();
			map3.put("variableName","体积");
			map3.put("variableQuantity", temp.get("fee_weight_count"));
			variables.add(map3);
			temp.put("variables", variables);
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
		ReceiveDetailVO[] receiveDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
				"vbillno in "+ NWUtils.buildConditionString(retbillnos.toArray(new String[retbillnos.size()])));
		if(receiveDetailVOs == null || receiveDetailVOs.length == 0){
			logger.info("接口返回数据单号不正确！");
			return;
		}
		for(ReceiveDetailVO receiveDetailVO : receiveDetailVOs){
			for(String billno : listSucces){
				if(receiveDetailVO.getVbillno().equals(billno)){
					receiveDetailVO.setStatus(VOStatus.UPDATED);
					receiveDetailVO.setEdi_flag(UFBoolean.TRUE);
				}
			}
			for(Map<String,String> map : listFailed){
				if(receiveDetailVO.getVbillno().equals(map.get("orderNo"))){
					receiveDetailVO.setStatus(VOStatus.UPDATED);
					receiveDetailVO.setEdi_flag(UFBoolean.FALSE);
					receiveDetailVO.setEdi_msg(map.get("errorMessage"));
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(receiveDetailVOs);
		
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
