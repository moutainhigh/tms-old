package com.tms.service.te.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;

/**
 * 导入快递信息的检查器
 * 
 * @author yaojie
 * @date 2015-11-12 下午14:19
 */
public class ExpressChecker  implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}
	
	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(fieldVO.getItemkey().equals("pk_entrust")) {
			if(value == null || StringUtils.isBlank(value.toString())) {
				throw new BusiException("第[?]委托单号不能为空!",rowNum+"");
			}
		}
		return null;
	}


}
