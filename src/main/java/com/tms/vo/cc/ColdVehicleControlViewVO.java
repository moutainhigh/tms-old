package com.tms.vo.cc;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ColdVehicleControlViewVO extends SuperVO {
	
	private String lot;
	private String carno;
	private UFDouble low_temp;
	private UFDouble hight_temp;
	private UFDouble ent_lot_speed;
	private String cust_name;
	private String pk_address;
	private String detail_addr;
	private UFDateTime req_arri_date;
	private UFDateTime forecast_deli_date;
	private String service_status;
	
	private Integer num_count;
	private UFDouble weight_count;
	private UFDouble volume_count;
	
	
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
	public String getLot() {
		return lot;
	}
	public void setLot(String lot) {
		this.lot = lot;
	}
	public String getCarno() {
		return carno;
	}
	public void setCarno(String carno) {
		this.carno = carno;
	}
	public UFDouble getLow_temp() {
		return low_temp;
	}
	public void setLow_temp(UFDouble low_temp) {
		this.low_temp = low_temp;
	}
	public UFDouble getHight_temp() {
		return hight_temp;
	}
	public void setHight_temp(UFDouble hight_temp) {
		this.hight_temp = hight_temp;
	}
	public UFDouble getEnt_lot_speed() {
		return ent_lot_speed;
	}
	public void setEnt_lot_speed(UFDouble ent_lot_speed) {
		this.ent_lot_speed = ent_lot_speed;
	}
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}
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
	public UFDateTime getReq_arri_date() {
		return req_arri_date;
	}
	public void setReq_arri_date(UFDateTime req_arri_date) {
		this.req_arri_date = req_arri_date;
	}
	public UFDateTime getForecast_deli_date() {
		return forecast_deli_date;
	}
	public void setForecast_deli_date(UFDateTime forecast_deli_date) {
		this.forecast_deli_date = forecast_deli_date;
	}
	public String getService_status() {
		return service_status;
	}
	public void setService_status(String service_status) {
		this.service_status = service_status;
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
		return "ts_cold_vehicle_control_view";
	}
	
	
}