package com.tms.vo.cc;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ColdVehicleOrdersViewVO extends SuperVO {
	
	private String lot;
	private String vbillno;
	private String vbillstatus;
	private String carr_name;
	private String carno;
	private String cust_name;
	private String cust_orderno;
	
	private String req_deli_date;
	private String req_arri_date;
	private String act_deli_date;
	private String act_arri_date;
	
	private String deli_addr;
	private String arri_addr;
	private String pk_address;
	private String detail_addr;
	private UFDouble weight_count;
	private Integer num_count;
	private UFDouble volume_count;
	private UFDouble ent_memo;
	
	
	
	public String getPk_address() {
		return pk_address;
	}
	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
	}
	public String getDetail_addr() {
		return detail_addr;
	}
	public void setDetail_addr(String detail_addr) {
		this.detail_addr = detail_addr;
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
	public String getVbillstatus() {
		return vbillstatus;
	}
	public void setVbillstatus(String vbillstatus) {
		this.vbillstatus = vbillstatus;
	}
	public String getCarr_name() {
		return carr_name;
	}
	public void setCarr_name(String carr_name) {
		this.carr_name = carr_name;
	}
	public String getCarno() {
		return carno;
	}
	public void setCarno(String carno) {
		this.carno = carno;
	}
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}
	public String getCust_orderno() {
		return cust_orderno;
	}
	public void setCust_orderno(String cust_orderno) {
		this.cust_orderno = cust_orderno;
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
	public UFDouble getWeight_count() {
		return weight_count;
	}
	public void setWeight_count(UFDouble weight_count) {
		this.weight_count = weight_count;
	}
	public Integer getNum_count() {
		return num_count;
	}
	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}
	public UFDouble getVolume_count() {
		return volume_count;
	}
	public void setVolume_count(UFDouble volume_count) {
		this.volume_count = volume_count;
	}
	public UFDouble getEnt_memo() {
		return ent_memo;
	}
	public void setEnt_memo(UFDouble ent_memo) {
		this.ent_memo = ent_memo;
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}
	@Override
	public String getPKFieldName() {
		return null;
	}
	@Override
	public String getTableName() {
		return "ts_cold_vehicle_orders_view";
	}
	
	
}