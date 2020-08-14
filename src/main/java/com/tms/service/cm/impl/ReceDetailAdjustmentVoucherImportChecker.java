package com.tms.service.cm.impl;

import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;

public class ReceDetailAdjustmentVoucherImportChecker implements ImportChecker{

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(fieldVO.getItemkey().equals("rece_type")){
			if(value == null || value.equals(0)){
				throw new BusiException("第[?]行的[单据类型]不能为空，且不能是[原始单据]！",rowNum+"");
			}
		}
		if(value == null) {
			if(fieldVO.getItemkey().equals("expense_type_name")) {
				throw new BusiException("第[?]行的[费用类型]不能为空！",rowNum+"");
			} else if(fieldVO.getItemkey().equals("amount")) {
				throw new BusiException("第[?]行的[金额]不能为空！",rowNum+"");
			}
		}
		return null;
	}

}
