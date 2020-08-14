package com.tms.service.base.impl;

import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;

/**
 * yaojiie 2015 12 30 客户信息的导入类，只验证客户编码和客户名称是否存在。
 */
public class CustImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		
		if(fieldVO.getItemkey().equals("cust_code")) {
			if(value == null) {
				throw new BusiException("第[?]行的客户编码不存在！", rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("cust_name")) {
			if(value == null) {
				throw new BusiException("第[?]行的客户名称不存在！",rowNum+"");
			}
		}
		return null;
	}
}
