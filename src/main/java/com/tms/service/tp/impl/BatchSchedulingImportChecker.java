package com.tms.service.tp.impl;

import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 批量排单导入校验
 * 
 * @author xuqc
 * @date 2014-4-8 下午08:52:06
 */
public class BatchSchedulingImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		
		if(fieldVO.getItemkey().equals("pk_carrier")) {
			if(value == null) {
				throw new BusiException("第[?]行的承运商错误！",rowNum+"");
			}
		}
		
		if(fieldVO.getItemkey().equals("pk_trans_type")) {
			if(value == null) {
				throw new BusiException("第[?]行的运输方式错误！",rowNum+"");
			}
		}

		if(fieldVO.getItemkey().equals("vbillno")) {
			if(value == null) {
				throw new BusiException("第[?]行的运输方式错误！",rowNum+"");
			}
		}
		
		if(fieldVO.getItemkey().equals("vbillstatus")) {
			if(value == null) {
				throw new BusiException("第[?]行批次错误！",rowNum+"");
			}
		}
		return null;
	}
}
