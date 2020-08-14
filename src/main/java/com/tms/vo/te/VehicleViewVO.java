package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;

@SuppressWarnings("serial")
public class VehicleViewVO extends SuperVO {
	
	private String carno;
	private String car_type;
	private String carr_name;
	private String car_status;
	private String driver_name;
	private String driver_mobile;
	private String photo;
	private String gps_id;
	private String pk_corp;
	private String ent_status;
	private String customs;
	private String dangerous_veh;
	private String tonnage;
	
	private String kpi_point;
	private String bill_num;
	private String exp_num;
	private String deli_rate;
	private String arri_rate;
	private String lost_rate;
	private String brok_rate;
	private String near_num;
	
	public String getKpi_point() {
		return kpi_point;
	}

	public void setKpi_point(String kpi_point) {
		this.kpi_point = kpi_point;
	}

	public String getBill_num() {
		return bill_num;
	}

	public void setBill_num(String bill_num) {
		this.bill_num = bill_num;
	}

	public String getExp_num() {
		return exp_num;
	}

	public void setExp_num(String exp_num) {
		this.exp_num = exp_num;
	}

	public String getDeli_rate() {
		return deli_rate;
	}

	public void setDeli_rate(String deli_rate) {
		this.deli_rate = deli_rate;
	}

	public String getArri_rate() {
		return arri_rate;
	}

	public void setArri_rate(String arri_rate) {
		this.arri_rate = arri_rate;
	}

	public String getLost_rate() {
		return lost_rate;
	}

	public void setLost_rate(String lost_rate) {
		this.lost_rate = lost_rate;
	}

	public String getBrok_rate() {
		return brok_rate;
	}

	public void setBrok_rate(String brok_rate) {
		this.brok_rate = brok_rate;
	}

	public String getNear_num() {
		return near_num;
	}

	public void setNear_num(String near_num) {
		this.near_num = near_num;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getCar_type() {
		return car_type;
	}

	public void setCar_type(String car_type) {
		this.car_type = car_type;
	}

	public String getCarr_name() {
		return carr_name;
	}

	public void setCarr_name(String carr_name) {
		this.carr_name = carr_name;
	}

	public String getCar_status() {
		return car_status;
	}

	public void setCar_status(String car_status) {
		this.car_status = car_status;
	}

	public String getDriver_name() {
		return driver_name;
	}

	public void setDriver_name(String driver_name) {
		this.driver_name = driver_name;
	}

	public String getDriver_mobile() {
		return driver_mobile;
	}

	public void setDriver_mobile(String driver_mobile) {
		this.driver_mobile = driver_mobile;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getGps_id() {
		return gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getEnt_status() {
		return ent_status;
	}

	public void setEnt_status(String ent_status) {
		this.ent_status = ent_status;
	}

	public String getCustoms() {
		return customs;
	}

	public void setCustoms(String customs) {
		this.customs = customs;
	}

	public String getDangerous_veh() {
		return dangerous_veh;
	}

	public void setDangerous_veh(String dangerous_veh) {
		this.dangerous_veh = dangerous_veh;
	}

	public String getTonnage() {
		return tonnage;
	}

	public void setTonnage(String tonnage) {
		this.tonnage = tonnage;
	}

	public String getParentPKFieldName() {
		return null;
	}
	
	public String getPKFieldName() {
		return "carno";
	}
	
	public String getTableName() {
		return "ts_vehicle_view";
	}
	
	public VehicleViewVO() {
		super();
	}
}
