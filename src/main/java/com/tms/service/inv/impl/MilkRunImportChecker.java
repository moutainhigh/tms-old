package com.tms.service.inv.impl;

import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 发货单导入的校验类
 * 
 * @author xuqc
 * @date 2014-4-8 下午08:52:06
 */
public class MilkRunImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		
		if(fieldVO.getItemkey().equals("pk_customer")) {
			if(value == null) {
				throw new BusiException("第[?]行的客户编码不存在！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("pk_carrier")) {
			if(value == null) {
				throw new BusiException("第[?]行的承运商不存在！",rowNum+"");
			}
		}
		
		if(fieldVO.getItemkey().equals("pk_trans_type")) {
			if(value == null) {
				throw new BusiException("第[?]行的运输方式错误！",rowNum+"");
			}
		}
		
		if(fieldVO.getItemkey().equals("operate_type")) {
			if(value == null) {
				throw new BusiException("第[?]行的操作类型错误！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("pk_address")) {
			if(value == null) {
				throw new BusiException("第[?]行的地址编码不存在！",rowNum+"");
			}
		}
		if(value != null) {
			if(fieldVO.getItemkey().equals("plan_pack_num_count")
					|| fieldVO.getItemkey().equals("pack_num_count")) {
				// 计划件数,件数，计划数量，数量
				UFDouble ufdvalue = (UFDouble) value;
				if (ufdvalue.doubleValue() < 0) {
					throw new BusiException("第[?]行计划件数,件数，等数据必须大于0！",rowNum+"");
				}
			}
			// 数量必须大于0
			if (fieldVO.getItemkey().equals("num")) {
				// 件数，数量
				Integer intvalue = (Integer) value;
				if (intvalue.intValue() < 0) {
					throw new BusiException("第[?]件数等数据必须大于0！",rowNum+"");
				}
			} else if (fieldVO.getItemkey().equals("weight") || fieldVO.getItemkey().equals("volume")) {
				UFDouble ufd = (UFDouble) value;
				if (ufd.doubleValue() < 0) {
					throw new BusiException("第[?]行重量和体积必须大于0！",rowNum+"");
				}
			}
		}
		return null;
	}
}
