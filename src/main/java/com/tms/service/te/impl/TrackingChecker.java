package com.tms.service.te.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.exp.CodeImportChecker;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;

import com.tms.vo.te.EntTrackingVO;

/**
 * 导入异常跟踪时的检查器
 * 
 * @author xuqc
 * @date 2013-8-23 下午06:45:07
 */
public class TrackingChecker extends CodeImportChecker implements ImportChecker {

	public String check(Object value, BillTempletBVO fieldVO) {
		if(fieldVO.getItemkey().equals(EntTrackingVO.ENTRUST_VBILLNO)) {
			if(value == null || StringUtils.isBlank(value.toString())) {
				return "委托单号不能为空!";
			}
		}
		return null;
	}

}
