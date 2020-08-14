package com.tms.service.inv.impl;

import org.nw.exception.BusiException;
import org.nw.exp.ImportChecker;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 发货单导入的校验类
 * 
 * @author xuqc
 * @date 2014-4-8 下午08:52:06
 */
public class StardImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}
	
	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(fieldVO.getItemkey().equals("pk_customer")) {
			if(value == null) {
				System.out.println(value);
				throw new BusiException("第[?]行的客户编码不能为空！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("pk_trans_type")) {
			if(value == null) {
				throw new BusiException("第[?]行的运输方式不能为空！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("req_deli_date")) {
			if(value == null) {
				throw new BusiException("第[?]行的要求提货日期不能为空！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("req_arri_date")) {
			if(value == null) {
				throw new BusiException("第[?]行的要求到货日期不能为空！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("pk_delivery")) {
			if(value == null) {
				throw new BusiException("第[?]行的提货方不能为空！",rowNum+"");
			}
		}
		if(fieldVO.getItemkey().equals("pk_arrival")) {
			if(value == null) {
				throw new BusiException("第[?]行的收货方不能为空！",rowNum+"");
			}
		}
		if(value != null) {
			// 数量必须大于0
			if(fieldVO.getItemkey().equals("plan_num") || fieldVO.getItemkey().equals("num")) {
				// 计划件数,件数，计划数量，数量
				Integer val = (Integer) value;
				if(val.intValue() < 0) {
					throw new BusiException("第[?]行计划件数、件数、等数据必须大于0！",rowNum+"");
				}
			} else if(fieldVO.getItemkey().equals("unit_weight") || fieldVO.getItemkey().equals("unit_volume")) {
				UFDouble ufd = (UFDouble) value;
				if(ufd.doubleValue() < 0) {
					throw new BusiException("第[?]行重量和体积必须大于0！",rowNum+"");
				}
			} else if(fieldVO.getItemkey().equals("plan_pack_num_count") || fieldVO.getItemkey().equals("pack_num_count")){
				UFDouble ufd = (UFDouble) value;
				if(ufd.doubleValue() < 0) {
					throw new BusiException("第[?]计划数量、数量等数据必须大于0！",rowNum+"");
				}
			}
		}
		return null;
	}
}
