package com.tms.service.httpEdi.impl;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.httpEdi.RTJSService;
import com.tms.vo.cm.ReceCheckSheetVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.te.EntrustVO;


/**
 * 
 * @author XIA
 * @date 2016 6 13 
 */

@Service("rTJSService")
public class RTJSServiceImpl extends TMSAbsBillServiceImpl implements RTJSService {
	//继承TMSAbsBillServiceImpl 只是为了形式统一，并没有实质关系。
	Logger logger = Logger.getLogger(RTJSServiceImpl.class);
	

	public String getBillType() {
		return null;
	}

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	public List<Map<String, Object>> billSearch(Map<String, Object> searchKeys) {
		String sql = "SELECT * FROM (SELECT ts_customer.tax_identify,ts_customer.account_name,ts_customer.register_addr,ts_customer.phone, "
				+ " ts_customer.bank,ts_customer.bank_account,ts_entrust.memo,ts_entrust.check_time AS billing_date,ts_entrust.check_man AS billing_by, "
				+ " ts_entrust.def7 AS billing_no ,ts_entrust.check_time,ts_entrust.vbillno AS entrust_vbillno,'运费' AS expense_type, '0.11' AS tax_rate, "
				+ " ts_entrust.def11 AS amount  "
				+ " FROM ts_entrust WITH(NOLOCK) LEFT JOIN ts_customer WITH(NOLOCK) ON ts_entrust.pk_customer = ts_customer.pk_customer "
				+ " WHERE isnull(ts_customer.dr,0) = 0 and isnull(ts_entrust.dr,0) = 0 and isnull(ts_customer.locked_flag,'N')='N' "
				+ " and ISNULL(ts_entrust.def7,'')<>'' AND ISNULL(ts_entrust.check_no,'')='' "
				+ "	UNION ALL "
				+ " SELECT ts_customer.tax_identify,ts_customer.account_name,ts_customer.register_addr,ts_customer.phone, "
				+ " ts_customer.bank,ts_customer.bank_account,ts_receive_detail.memo,ts_receive_detail.def9 AS billing_date, "
				+ " ts_receive_detail.def8 AS billing_by, ts_receive_detail.def7 AS billing_no ,ts_receive_detail.def9 AS check_time,ts_receive_detail.vbillno AS entrust_vbillno, "
				+ " '运费' AS expense_type, '0.11' AS tax_rate,ts_receive_detail.cost_amount AS amount "
				+ " FROM ts_receive_detail WITH(NOLOCK)  "
				+ " LEFT JOIN ts_customer WITH(NOLOCK) ON ts_receive_detail.bala_customer = ts_customer.pk_customer "
				+ " WHERE isnull(ts_customer.dr,0) = 0 and isnull(ts_receive_detail.dr,0) = 0 and isnull(ts_customer.locked_flag,'N')='N' "
				+ " and ISNULL(ts_receive_detail.def7,'')<>'' AND ISNULL(ts_receive_detail.check_no,'')='' AND ts_receive_detail.rece_type<>0) TABLE_A WHERE 1=1  ";
		StringBuffer cond = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		if(searchKeys.get("strat_date") != null && StringUtils.isNotBlank(searchKeys.get("strat_date").toString())){
			cond.append(" and check_time > ?");
			args.add(searchKeys.get("strat_date"));
		}
		if(searchKeys.get("end_date") != null && StringUtils.isNotBlank(searchKeys.get("end_date").toString())){
			cond.append(" and check_time < ?");
			args.add(searchKeys.get("end_date"));
		}
		if(searchKeys.get("cust_code") != null && StringUtils.isNotBlank(searchKeys.get("cust_code").toString())){
			cond.append(" and account_name =?");
			try {
				args.add(new String(searchKeys.get("cust_code").toString().getBytes("ISO-8859-1"),"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new BusiException("编码解析错误！");
			}
		}
		if(searchKeys.get("billing_no") != null && StringUtils.isNotBlank(searchKeys.get("billing_no").toString())){
			cond.append(" and billing_no =?");
			args.add(searchKeys.get("billing_no"));
		}
		if(cond.length() > 0){
			sql += (cond.toString());
		}
		List<Map<String,Object>> queryResults = NWDao.getInstance().queryForList(sql, args.toArray(new Object[args.size()]));
		if(queryResults == null || queryResults.size() == 0){
			return queryResults;
		}
		//对查询结果按照"billing_no"进行分组。
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();//返回结果
		Map<String,List<Map<String, Object>>> groupMap = new HashMap<String,List<Map<String, Object>>>();
		for(Map<String,Object> queryResult : queryResults){
			String key = String.valueOf(queryResult.get("billing_no"));
			List<Map<String, Object>> mapList  =groupMap.get(key);
			if(mapList == null){
				mapList = new ArrayList<Map<String, Object>>();
				groupMap.put(key, mapList);
			}
			mapList.add(queryResult);
		}
		for(String key : groupMap.keySet()){
			Map<String, Object> resultUnitMap = new HashMap<String, Object>();
			List<Map<String, Object>> mapList  =groupMap.get(key);
			resultUnitMap.put("tax_identify", mapList.get(0).get("tax_identify"));
			resultUnitMap.put("account_name", mapList.get(0).get("account_name"));
			resultUnitMap.put("register_addr", mapList.get(0).get("register_addr"));
			resultUnitMap.put("phone", mapList.get(0).get("phone"));
			resultUnitMap.put("bank", mapList.get(0).get("bank"));
			resultUnitMap.put("bank_account", mapList.get(0).get("bank_account"));
			resultUnitMap.put("memo", mapList.get(0).get("memo"));
			resultUnitMap.put("billing_date", mapList.get(0).get("billing_date"));
			resultUnitMap.put("billing_by", mapList.get(0).get("billing_by"));
			resultUnitMap.put("billing_no", mapList.get(0).get("billing_no"));
			List<Map<String,Object>> result_b = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> mapListUnit : mapList){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("billing_no", mapListUnit.get("billing_no"));
				map.put("entrust_vbillno", mapListUnit.get("entrust_vbillno"));
				map.put("expense_type", mapListUnit.get("expense_type"));
				map.put("tax_rate", mapListUnit.get("tax_rate"));
				map.put("amount", mapListUnit.get("amount"));		
				result_b.add(map);
			}
			resultUnitMap.put("billing_b", result_b);
			result.add(resultUnitMap);
		}
		return result;
	}

	public Map<String, Object> billImport(List<Map<String, String>> jsons) {
		if(jsons == null || jsons.size() == 0){
			return null;
		}
		
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<String> success = new ArrayList<String>();
		List<String> fail = new ArrayList<String>();
		for(Map<String, String> json : jsons){
			String billing_no = json.get("billing_no");
			if(billing_no.startsWith("WTD")){
				EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, 
						"def7 =? ",billing_no);
				if(entrustVOs == null || entrustVOs.length == 0){
					fail.add(billing_no);
				}else{
					for(EntrustVO entrustVO : entrustVOs){
						entrustVO.setCheck_no(json.get("invoice_no"));
						entrustVO.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(entrustVO);
					}
					success.add(billing_no);
				}
			}
			
			if(billing_no.startsWith("YSMX")){
				ReceiveDetailVO[] receDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class, 
						"def7 =? ",billing_no);
				if(receDetailVOs == null || receDetailVOs.length == 0){
					fail.add(billing_no);
				}else{
					for(ReceiveDetailVO receDetailVO : receDetailVOs){
						receDetailVO.setCheck_no(json.get("invoice_no"));
						receDetailVO.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(receDetailVO);
					}
					success.add(billing_no);
				}
			}
			
			if(billing_no.startsWith("YSDZ")){
				ReceCheckSheetVO[] receCheckSheetVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceCheckSheetVO.class, 
						"def7 =? ",billing_no);
				if(receCheckSheetVOs == null || receCheckSheetVOs.length == 0){
					fail.add(billing_no);
				}else{
					for(ReceCheckSheetVO receCheckSheetVO : receCheckSheetVOs){
						receCheckSheetVO.setCheck_no(json.get("invoice_no"));
						receCheckSheetVO.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(receCheckSheetVO);
					}
					success.add(billing_no);
				}
			}
		}
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("success", success);
		result.put("fail", fail);
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return result;
	}

	public Map<String, Object> billDelete(List<Map<String, String>> jsons) {
		if(jsons == null || jsons.size() == 0){
			return null;
		}
		
		String[] billing_nos = new String[jsons.size()];
		for(int i=0;i<jsons.size();i++){
			billing_nos[i] = jsons.get(i).get("billing_no");
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<String> success = new ArrayList<String>();
		List<String> fail = new ArrayList<String>();
		for(Map<String, String> json : jsons){
			String billing_no = json.get("billing_no");
			if(billing_no.startsWith("WTD")){
				EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, 
						"def7 =? ",billing_no);
				if(entrustVOs == null || entrustVOs.length == 0){
					fail.add(billing_no);
				}else{
					for(EntrustVO entrustVO : entrustVOs){
						entrustVO.setCheck_no(null);
						entrustVO.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(entrustVO);
					}
					success.add(billing_no);
				}
			}
			
			if(billing_no.startsWith("YSMX")){
				ReceiveDetailVO[] receDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class, 
						"def7 =? ",billing_no);
				if(receDetailVOs == null || receDetailVOs.length == 0){
					fail.add(billing_no);
				}else{
					for(ReceiveDetailVO receDetailVO : receDetailVOs){
						receDetailVO.setCheck_no(null);
						receDetailVO.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(receDetailVO);
					}
					success.add(billing_no);
				}
			}
			
			if(billing_no.startsWith("YSDZ")){
				ReceCheckSheetVO[] receCheckSheetVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceCheckSheetVO.class, 
						"def7 =? ",billing_no);
				if(receCheckSheetVOs == null || receCheckSheetVOs.length == 0){
					fail.add(billing_no);
				}else{
					for(ReceCheckSheetVO receCheckSheetVO : receCheckSheetVOs){
						receCheckSheetVO.setCheck_no(null);
						receCheckSheetVO.setStatus(VOStatus.UPDATED);
						toBeUpdate.add(receCheckSheetVO);
					}
					success.add(billing_no);
				}
			}
		}
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("success", success);
		result.put("fail", fail);
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return result;
	}
	
}
