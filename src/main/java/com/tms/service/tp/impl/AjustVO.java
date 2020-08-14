package com.tms.service.tp.impl;

/**
 * 针对地址和运段排序的调整VO
 * 
 * @author xuqc
 * @date 2014-10-11 上午12:27:18
 */
public class AjustVO {
	String addr_flag;
	String pk_segment;

	public String getAddr_flag() {
		return addr_flag;
	}

	public void setAddr_flag(String addr_flag) {
		this.addr_flag = addr_flag;
	}

	public String getPk_segment() {
		return pk_segment;
	}

	public void setPk_segment(String pk_segment) {
		this.pk_segment = pk_segment;
	}
}
