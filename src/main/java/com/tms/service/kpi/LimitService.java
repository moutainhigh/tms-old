package com.tms.service.kpi;

import java.util.HashMap;
import java.util.List;
import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFBoolean;

import com.tms.vo.kpi.LimitVO;

public interface LimitService extends IToftService{
	
	public List<HashMap<String,String>> matchLimit();
	
	@SuppressWarnings("rawtypes")
	public List<HashMap> getWarnByProce(String proce, String pk_user);
	
	public LimitVO getLimitVObycond(Integer type,Integer matter,Integer exp_type,
			String pk_carrier, String pk_customer, UFBoolean if_urgent, String item_code, 
			String goods_type ,String pk_address, String pk_corp);
}
