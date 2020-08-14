package com.tms.vo.route;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class VehicleTripsBVO extends SuperVO {
	
	private String pk_vehicle_trips_b;
	private String pk_vehicle_trips;
	private String action_id;
	private String action_kind;
	private String start_address;
	private String end_address;
	private UFDateTime action_start_date;
	private UFDateTime action_end_date;
	private UFDouble mileage;

	private String pk_segment;
	private String orderno;
	private String ord_orderno;
	
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
	
	public static final String PK_VEHICLE_TRIPS_B = "pk_vehicle_trips_b";
	public static final String PK_VEHICLE_TRIPS = "pk_vehicle_trips";


	public String getPk_vehicle_trips_b() {
		return pk_vehicle_trips_b;
	}

	public void setPk_vehicle_trips_b(String pk_vehicle_trips_b) {
		this.pk_vehicle_trips_b = pk_vehicle_trips_b;
	}

	public String getPk_vehicle_trips() {
		return pk_vehicle_trips;
	}

	public void setPk_vehicle_trips(String pk_vehicle_trips) {
		this.pk_vehicle_trips = pk_vehicle_trips;
	}

	public String getAction_id() {
		return action_id;
	}

	public void setAction_id(String action_id) {
		this.action_id = action_id;
	}

	public String getAction_kind() {
		return action_kind;
	}

	public void setAction_kind(String action_kind) {
		this.action_kind = action_kind;
	}

	public String getStart_address() {
		return start_address;
	}

	public void setStart_address(String start_address) {
		this.start_address = start_address;
	}

	public String getEnd_address() {
		return end_address;
	}

	public void setEnd_address(String end_address) {
		this.end_address = end_address;
	}

	public UFDateTime getAction_start_date() {
		return action_start_date;
	}

	public void setAction_start_date(UFDateTime action_start_date) {
		this.action_start_date = action_start_date;
	}

	public UFDateTime getAction_end_date() {
		return action_end_date;
	}

	public void setAction_end_date(UFDateTime action_end_date) {
		this.action_end_date = action_end_date;
	}

	public UFDouble getMileage() {
		return mileage;
	}

	public void setMileage(UFDouble mileage) {
		this.mileage = mileage;
	}

	public String getPk_segment() {
		return pk_segment;
	}

	public void setPk_segment(String pk_segment) {
		this.pk_segment = pk_segment;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getOrd_orderno() {
		return ord_orderno;
	}

	public void setOrd_orderno(String ord_orderno) {
		this.ord_orderno = ord_orderno;
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

	@Override
	public String getParentPKFieldName() {
		return "pk_vehicle_trips";
	}

	@Override
	public String getPKFieldName() {
		return "pk_vehicle_trips_b";
	}

	@Override
	public String getTableName() {
		return "ts_vehicle_trips_b";
	}

}
