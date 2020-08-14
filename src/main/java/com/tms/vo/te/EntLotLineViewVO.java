package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;

@SuppressWarnings("serial")
public class EntLotLineViewVO extends SuperVO {
	
	
	private String lot;
	private String detail_addr;
	private String contact;
	private String mobile;
	private String phone;
	private UFBoolean arrival_flag;
	private Integer operate_type;
	private UFDateTime req_arri_date;
	private UFDateTime act_arri_date;
	private UFDateTime req_leav_date;
	private UFDateTime act_leav_date;
	
	public static final String LOT = "lot";
	public static final String TS_ENT_LOT_LINE_VIEW = "ts_ent_lot_line_view";

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getDetail_addr() {
		return detail_addr;
	}

	public void setDetail_addr(String detail_addr) {
		this.detail_addr = detail_addr;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UFBoolean getArrival_flag() {
		return arrival_flag;
	}

	public void setArrival_flag(UFBoolean arrival_flag) {
		this.arrival_flag = arrival_flag;
	}

	public Integer getOperate_type() {
		return operate_type;
	}

	public void setOperate_type(Integer operate_type) {
		this.operate_type = operate_type;
	}

	public UFDateTime getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(UFDateTime req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public UFDateTime getAct_arri_date() {
		return act_arri_date;
	}

	public void setAct_arri_date(UFDateTime act_arri_date) {
		this.act_arri_date = act_arri_date;
	}

	public UFDateTime getReq_leav_date() {
		return req_leav_date;
	}

	public void setReq_leav_date(UFDateTime req_leav_date) {
		this.req_leav_date = req_leav_date;
	}

	public UFDateTime getAct_leav_date() {
		return act_leav_date;
	}

	public void setAct_leav_date(UFDateTime act_leav_date) {
		this.act_leav_date = act_leav_date;
	}

	public String getParentPKFieldName() {
		return "lot";
	}
	
	public String getPKFieldName() {
		return "lot";
	}
	
	public String getTableName() {
		return "ts_ent_lot_line_view";
	}
	
	public EntLotLineViewVO() {
		super();
	}
}
