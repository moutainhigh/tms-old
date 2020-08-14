package com.tms.web.httpEdi.yusen;

import org.nw.exception.ApiException;
import org.nw.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseEdiController {
	
	protected Logger logger = LoggerFactory.getLogger("EDI");
	
	protected String authentication(String uid,String pwd){
		try {
			return HttpUtils.authentication(uid, pwd);
		} catch (ApiException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	

}
