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
public class InvoiceImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(fieldVO.getItemkey().equals("goods_code")) {
			if(value == null) {
				throw new BusiException("第[?]行的货品编码不存在！",rowNum+"");
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
			if (fieldVO.getItemkey().equals("plan_num") || fieldVO.getItemkey().equals("num")) {
				// 计划件数,件数，计划数量，数量
				Integer intvalue = (Integer) value;
				if (intvalue.intValue() < 0) {
					throw new BusiException("第[?]计划数量，数量等数据必须大于0！",rowNum+"");
				}
			} else if (fieldVO.getItemkey().equals("unit_weight") || fieldVO.getItemkey().equals("unit_volume")) {
				UFDouble ufd = (UFDouble) value;
				if (ufd.doubleValue() < 0) {
					throw new BusiException("第[?]行重量和体积必须大于0！",rowNum+"");
				}
			}
		}
		return null;
	}
}
