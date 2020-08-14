package com.tms.service.te.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.exp.CodeImportChecker;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFDateTime;

import com.tms.vo.base.CarTypeVO;
import com.tms.vo.te.EntrustVO;

/**
 * 导入运力信息时的检查器
 * 
 * @author yaojie
 * @date 2015-11-12 下午14:19
 */
public class TransbilityChecker extends CodeImportChecker implements ImportChecker {

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(fieldVO.getItemkey().equals("pk_entrust")) {
			if(value == null || StringUtils.isBlank(value.toString())) {
				throw new BusiException("第[?]委托单号不能为空！",rowNum+"");
			}
		}
		return null;
	}


}
