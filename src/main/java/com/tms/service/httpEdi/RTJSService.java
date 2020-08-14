package com.tms.service.httpEdi;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;

/**
 * 
 * @author XIA
 * @date 2016 6 13 
 */
public interface RTJSService extends IBillService {

	List<Map<String, Object>> billSearch(Map<String,Object> searchKeys);
	
	Map<String,Object> billImport(List<Map<String,String>> jsons);
	
	Map<String,Object> billDelete(List<Map<String,String>> jsons);

	
}
