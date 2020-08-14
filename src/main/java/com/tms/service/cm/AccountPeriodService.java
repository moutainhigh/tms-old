package com.tms.service.cm;

import java.util.Map;

import org.nw.service.IToftService;

/**
 * 账期
 * @author muyun
 *
 */
public interface AccountPeriodService extends IToftService {
	
	public Map<String,String> periodCommit(String id);
	
	public Map<String,String> periodUncommit(String id);

}
