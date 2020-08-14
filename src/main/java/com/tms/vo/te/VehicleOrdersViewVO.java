package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;

@SuppressWarnings("serial")
public class VehicleOrdersViewVO extends SuperVO {
	
	private String carno;
	private String lot;
	private String vbillno;
	private String cust_orderno;
	private String cust_name;
	private String vbillstatus;
	private String req_deli_date;
	private String req_arri_date;
	private String act_deli_date;
	private String act_arri_date;
	private String deli_addr;
	private String arri_addr;
	private String ent_memo;
	
	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getVbillno() {
		return vbillno;
	}

	public void setVbillno(String vbillno) {
		this.vbillno = vbillno;
	}

	public String getCust_orderno() {
		return cust_orderno;
	}

	public void setCust_orderno(String cust_orderno) {
		this.cust_orderno = cust_orderno;
	}

	public String getCust_name() {
		return cust_name;
	}

	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}

	public String getVbillstatus() {
		return vbillstatus;
	}

	public void setVbillstatus(String vbillstatus) {
		this.vbillstatus = vbillstatus;
	}

	public String getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(String req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public String getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(String req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public String getAct_deli_date() {
		return act_deli_date;
	}

	public void setAct_deli_date(String act_deli_date) {
		this.act_deli_date = act_deli_date;
	}

	public String getAct_arri_date() {
		return act_arri_date;
	}

	public void setAct_arri_date(String act_arri_date) {
		this.act_arri_date = act_arri_date;
	}

	public String getDeli_addr() {
		return deli_addr;
	}

	public void setDeli_addr(String deli_addr) {
		this.deli_addr = deli_addr;
	}

	public String getArri_addr() {
		return arri_addr;
	}

	public void setArri_addr(String arri_addr) {
		this.arri_addr = arri_addr;
	}

	public String getEnt_memo() {
		return ent_memo;
	}

	public void setEnt_memo(String ent_memo) {
		this.ent_memo = ent_memo;
	}

	public String getParentPKFieldName() {
		return null;
	}
	
	public String getPKFieldName() {
		return "vbillno";
	}
	
	public String getTableName() {
		return "ts_vehicle_orders_view";
	}
	
	public VehicleOrdersViewVO() {
		super();
	}
}
