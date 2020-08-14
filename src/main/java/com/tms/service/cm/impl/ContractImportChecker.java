package com.tms.service.cm.impl;

import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;

/**
 * 发货单导入的校验类
 * 
 * @author xuqc
 * @date 2014-4-8 下午08:52:06
 */
public class ContractImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(value == null) {
			if(fieldVO.getItemkey().equals("start_addr_type")) {
				throw new BusiException("第[?]行的[始发区域类型]不能为空！",rowNum+"");
			} else if(fieldVO.getItemkey().equals("end_addr_type")) {
				throw new BusiException("第[?]行的[目的区域类型]不能为空！",rowNum+"");
			}  else if(fieldVO.getItemkey().equals("expense_type_name")) {
				throw new BusiException("第[?]行的[费用类型:?]错误，不是有效的费用类型！",rowNum+"",value.toString());
			} else if(fieldVO.getItemkey().equals("quote_type")) {
				throw new BusiException("第[?]行的[报价类型:?]错误，不是有效的报价类型！",rowNum+"",value.toString());
			} else if(fieldVO.getItemkey().equals("price_type")) {
				throw new BusiException("第[?]行的[价格类型:?]错误，不是有效的价格类型！",rowNum+"",value.toString());
			} else if(fieldVO.getItemkey().equals("valuation_type")) {
				throw new BusiException("第[?]行的[计价方式:?]错误，不是有效的计价方式！",rowNum+"",value.toString());
			}
		}
		return null;
	}
}
