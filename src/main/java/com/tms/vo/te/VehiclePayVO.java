package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class VehiclePayVO extends SuperVO {
	
	private String pk_vehicle_pay;
	private String vehicle_pay_type;
	private String lot;
	private UFDouble kilometre;
	private UFDouble days;
	private UFDouble amount;
	private String pk_etc;
	private String pk_fuelcard;
	private UFDouble refuel_qty;
	private UFDouble refuel_amount;
	private Integer check_type;
	private Integer pay_type;
	private String memo;
	private Integer dr;
	private UFDateTime ts;
	
	private String def1;
	private String def2;
	private String def3;
	private String def4;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private String def9;
	private String def10;
	private UFDouble def11;
	private UFDouble def12;
	
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	

	public String getPk_vehicle_pay() {
		return pk_vehicle_pay;
	}

	public void setPk_vehicle_pay(String pk_vehicle_pay) {
		this.pk_vehicle_pay = pk_vehicle_pay;
	}


	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getVehicle_pay_type() {
		return vehicle_pay_type;
	}

	public void setVehicle_pay_type(String vehicle_pay_type) {
		this.vehicle_pay_type = vehicle_pay_type;
	}

	public UFDouble getKilometre() {
		return kilometre;
	}

	public void setKilometre(UFDouble kilometre) {
		this.kilometre = kilometre;
	}

	public UFDouble getAmount() {
		return amount;
	}

	public UFDouble getDays() {
		return days;
	}

	public void setDays(UFDouble days) {
		this.days = days;
	}

	public void setAmount(UFDouble amount) {
		this.amount = amount;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getPk_etc() {
		return pk_etc;
	}

	public void setPk_etc(String pk_etc) {
		this.pk_etc = pk_etc;
	}

	public String getPk_fuelcard() {
		return pk_fuelcard;
	}

	public void setPk_fuelcard(String pk_fuelcard) {
		this.pk_fuelcard = pk_fuelcard;
	}

	public UFDouble getRefuel_qty() {
		return refuel_qty;
	}

	public void setRefuel_qty(UFDouble refuel_qty) {
		this.refuel_qty = refuel_qty;
	}

	public UFDouble getRefuel_amount() {
		return refuel_amount;
	}

	public void setRefuel_amount(UFDouble refuel_amount) {
		this.refuel_amount = refuel_amount;
	}

	public Integer getCheck_type() {
		return check_type;
	}

	public void setCheck_type(Integer check_type) {
		this.check_type = check_type;
	}

	public Integer getPay_type() {
		return pay_type;
	}

	public void setPay_type(Integer pay_type) {
		this.pay_type = pay_type;
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

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public String getDef4() {
		return def4;
	}

	public void setDef4(String def4) {
		this.def4 = def4;
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

	public String getParentPKFieldName() {
		return null;
	}
	
	public String getPKFieldName() {
		return "pk_vehicle_pay";
	}
	
	public String getTableName() {
		return "ts_vehicle_pay";
	}
	
	public VehiclePayVO() {
		super();
	}
}
