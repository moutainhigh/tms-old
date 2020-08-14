package com.tms.vo.fleet;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
//yaojiie 2015 12 16 加油管理VO
@SuppressWarnings("serial")
public class RefuelVO extends SuperVO {
	
	private String pk_refuel;
	private String pk_vehicle_pay;
	private String vbillno;
	private Integer vbillstatus;
	private String lot;
	private Integer pay_type;
	private String pk_fuelcard;
	private String carno;
	private String pk_driver;
	private UFDouble refuel_mileage;
	private String refuel_mode;
	private String refuel_station;
	private UFDouble refuel_price;
	private UFDouble amount;
	private UFDouble refuel_qty;
	private UFDateTime refuel_dete;
	private String pk_corp;
	private String memo;
	private UFDouble curr_longitude;
	private UFDouble curr_latitude;
	private String app_detail_addr;
	
	private Integer dr;
	private String ts;
	
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
	
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	
	public static final String PK_REFUEL = "pk_refuel";
	
	public String getPk_refuel() {
		return pk_refuel;
	}

	public void setPk_refuel(String pk_refuel) {
		this.pk_refuel = pk_refuel;
	}

	public String getPk_vehicle_pay() {
		return pk_vehicle_pay;
	}

	public void setPk_vehicle_pay(String pk_vehicle_pay) {
		this.pk_vehicle_pay = pk_vehicle_pay;
	}

	public String getVbillno() {
		return vbillno;
	}

	public void setVbillno(String vbillno) {
		this.vbillno = vbillno;
	}

	public Integer getVbillstatus() {
		return vbillstatus;
	}

	public void setVbillstatus(Integer vbillstatus) {
		this.vbillstatus = vbillstatus;
	}

	public UFDouble getCurr_longitude() {
		return curr_longitude;
	}

	public void setCurr_longitude(UFDouble curr_longitude) {
		this.curr_longitude = curr_longitude;
	}

	public UFDouble getCurr_latitude() {
		return curr_latitude;
	}

	public void setCurr_latitude(UFDouble curr_latitude) {
		this.curr_latitude = curr_latitude;
	}

	public String getApp_detail_addr() {
		return app_detail_addr;
	}

	public void setApp_detail_addr(String app_detail_addr) {
		this.app_detail_addr = app_detail_addr;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public Integer getPay_type() {
		return pay_type;
	}

	public void setPay_type(Integer pay_type) {
		this.pay_type = pay_type;
	}

	public String getPk_fuelcard() {
		return pk_fuelcard;
	}

	public void setPk_fuelcard(String pk_fuelcard) {
		this.pk_fuelcard = pk_fuelcard;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getPk_driver() {
		return pk_driver;
	}

	public void setPk_driver(String pk_driver) {
		this.pk_driver = pk_driver;
	}

	public UFDouble getRefuel_mileage() {
		return refuel_mileage;
	}

	public void setRefuel_mileage(UFDouble refuel_mileage) {
		this.refuel_mileage = refuel_mileage;
	}

	public String getRefuel_mode() {
		return refuel_mode;
	}

	public void setRefuel_mode(String refuel_mode) {
		this.refuel_mode = refuel_mode;
	}

	public String getRefuel_station() {
		return refuel_station;
	}

	public void setRefuel_station(String refuel_station) {
		this.refuel_station = refuel_station;
	}

	public UFDouble getRefuel_price() {
		return refuel_price;
	}

	public void setRefuel_price(UFDouble refuel_price) {
		this.refuel_price = refuel_price;
	}

	public UFDouble getAmount() {
		return amount;
	}

	public void setAmount(UFDouble amount) {
		this.amount = amount;
	}

	public UFDouble getRefuel_qty() {
		return refuel_qty;
	}

	public void setRefuel_qty(UFDouble refuel_qty) {
		this.refuel_qty = refuel_qty;
	}

	public UFDateTime getRefuel_dete() {
		return refuel_dete;
	}

	public void setRefuel_dete(UFDateTime refuel_dete) {
		this.refuel_dete = refuel_dete;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
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

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
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

	public static String getPkRefuel() {
		return PK_REFUEL;
	}

	public RefuelVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return "pk_refuel";
	}

	@Override
	public String getTableName() {
		return "ts_refuel";
	}

}
