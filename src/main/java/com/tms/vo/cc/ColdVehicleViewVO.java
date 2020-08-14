package com.tms.vo.cc;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ColdVehicleViewVO extends SuperVO {
	
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
	private String lot;
	private String pk_carrier;
	
	private String pk_address;
	private String detail_addr;
	private Integer num_count;
	private UFDouble weight_count;
	private UFDouble volume_count;

	
	
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
	public Integer getNum_count() {
		return num_count;
	}
	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}
	public UFDouble getWeight_count() {
		return weight_count;
	}
	public void setWeight_count(UFDouble weight_count) {
		this.weight_count = weight_count;
	}
	public UFDouble getVolume_count() {
		return volume_count;
	}
	public void setVolume_count(UFDouble volume_count) {
		this.volume_count = volume_count;
	}
	public String getPk_carrier() {
		return pk_carrier;
	}
	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
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
	public String getLot() {
		return lot;
	}
	public void setLot(String lot) {
		this.lot = lot;
	}
	@Override
	public String getParentPKFieldName() {
		return null;
	}
	@Override
	public String getPKFieldName() {
		return "";
	}
	@Override
	public String getTableName() {
		return "ts_cold_vehicle_view";
	}
	
	
}
