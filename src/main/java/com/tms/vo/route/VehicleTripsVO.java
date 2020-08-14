package com.tms.vo.route;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class VehicleTripsVO extends SuperVO {
	
	private String pk_vehicle_trips;
	private String id;
	private String resource_id;
	private UFDateTime start_date;
	private UFDateTime end_date;
	private UFDouble total_mileage;
	private UFDouble total_time_consuming;
	private UFDouble total_transportation_time;
	private UFDouble total_rest;
	private UFDouble total_wait;
	private UFDouble total_loading;
	private Integer vbillstatus;
	private Integer total_points;
	private String memo;
	
	private Integer dr;
	private UFDateTime ts;
	
	private String def1;
	private String def2;
	private String def4;
	private String def3;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private String def9;
	private String def10;
	private UFDouble def11;
	private UFDouble def12;
	
	private String pk_corp;
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	public static final String PK_VEHICLE_TRIPS = "pk_vehicle_trips";

	public String getPk_vehicle_trips() {
		return pk_vehicle_trips;
	}

	public void setPk_vehicle_trips(String pk_vehicle_trips) {
		this.pk_vehicle_trips = pk_vehicle_trips;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResource_id() {
		return resource_id;
	}

	public void setResource_id(String resource_id) {
		this.resource_id = resource_id;
	}

	public UFDateTime getStart_date() {
		return start_date;
	}

	public void setStart_date(UFDateTime start_date) {
		this.start_date = start_date;
	}

	public UFDateTime getEnd_date() {
		return end_date;
	}

	public void setEnd_date(UFDateTime end_date) {
		this.end_date = end_date;
	}

	public UFDouble getTotal_mileage() {
		return total_mileage;
	}

	public void setTotal_mileage(UFDouble total_mileage) {
		this.total_mileage = total_mileage;
	}

	public UFDouble getTotal_time_consuming() {
		return total_time_consuming;
	}

	public void setTotal_time_consuming(UFDouble total_time_consuming) {
		this.total_time_consuming = total_time_consuming;
	}

	public UFDouble getTotal_transportation_time() {
		return total_transportation_time;
	}

	public void setTotal_transportation_time(UFDouble total_transportation_time) {
		this.total_transportation_time = total_transportation_time;
	}

	public UFDouble getTotal_rest() {
		return total_rest;
	}

	public void setTotal_rest(UFDouble total_rest) {
		this.total_rest = total_rest;
	}

	public UFDouble getTotal_wait() {
		return total_wait;
	}

	public void setTotal_wait(UFDouble total_wait) {
		this.total_wait = total_wait;
	}

	public UFDouble getTotal_loading() {
		return total_loading;
	}

	public void setTotal_loading(UFDouble total_loading) {
		this.total_loading = total_loading;
	}

	public Integer getVbillstatus() {
		return vbillstatus;
	}

	public void setVbillstatus(Integer vbillstatus) {
		this.vbillstatus = vbillstatus;
	}

	public Integer getTotal_points() {
		return total_points;
	}

	public void setTotal_points(Integer total_points) {
		this.total_points = total_points;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef4() {
		return def4;
	}

	public void setDef4(String def4) {
		this.def4 = def4;
	}

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public String getDef5() {
		return def5;
	}

	public void setDef5(String def5) {
		this.def5 = def5;
	}

	public String getDef6() {
		return def6;
	}

	public void setDef6(String def6) {
		this.def6 = def6;
	}

	public String getDef7() {
		return def7;
	}

	public void setDef7(String def7) {
		this.def7 = def7;
	}

	public String getDef8() {
		return def8;
	}

	public void setDef8(String def8) {
		this.def8 = def8;
	}

	public String getDef9() {
		return def9;
	}

	public void setDef9(String def9) {
		this.def9 = def9;
	}

	public String getDef10() {
		return def10;
	}

	public void setDef10(String def10) {
		this.def10 = def10;
	}

	public UFDouble getDef11() {
		return def11;
	}

	public void setDef11(UFDouble def11) {
		this.def11 = def11;
	}

	public UFDouble getDef12() {
		return def12;
	}

	public void setDef12(UFDouble def12) {
		this.def12 = def12;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public UFDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
	}

	public String getCreate_user() {
		return create_user;
	}

	public void setCreate_user(String create_user) {
		this.create_user = create_user;
	}

	public UFDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(UFDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return "pk_vehicle_trips";
	}

	@Override
	public String getTableName() {
		return "ts_vehicle_trips";
	}

}
