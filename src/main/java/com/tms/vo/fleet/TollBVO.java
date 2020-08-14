package com.tms.vo.fleet;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
//yaojiie 2015 12 16 路桥费管理BVO
@SuppressWarnings("serial")
public class TollBVO extends SuperVO {
	
	private String pk_toll_b;
	private String pk_toll;
	
	private Integer pay_type;
	private String pk_expense_type;
	private String pk_vehicle_pay;
	private UFDouble amount;
	private UFDateTime payment_time;
	private UFDateTime regi_time;
	private String regi_person;
	private UFDouble curr_longitude;
	private UFDouble curr_latitude;
	private String app_detail_addr;
	
	private String pk_arrival;
	private String pk_delivery;
	private UFDouble arri_mileage;
	private UFDouble deli_mileage;
	
	private String memo;
	
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
	
	public static final String PK_TOLL = "pk_toll";
	public static final String PK_TOLL_B = "pk_toll_b";

	public String getPk_toll_b() {
		return pk_toll_b;
	}

	public void setPk_toll_b(String pk_toll_b) {
		this.pk_toll_b = pk_toll_b;
	}

	public String getPk_toll() {
		return pk_toll;
	}

	public void setPk_toll(String pk_toll) {
		this.pk_toll = pk_toll;
	}

	public Integer getPay_type() {
		return pay_type;
	}

	public void setPay_type(Integer pay_type) {
		this.pay_type = pay_type;
	}

	public String getPk_vehicle_pay() {
		return pk_vehicle_pay;
	}

	public void setPk_vehicle_pay(String pk_vehicle_pay) {
		this.pk_vehicle_pay = pk_vehicle_pay;
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

	public String getPk_expense_type() {
		return pk_expense_type;
	}

	public void setPk_expense_type(String pk_expense_type) {
		this.pk_expense_type = pk_expense_type;
	}

	public UFDouble getAmount() {
		return amount;
	}

	public void setAmount(UFDouble amount) {
		this.amount = amount;
	}

	public UFDateTime getPayment_time() {
		return payment_time;
	}

	public void setPayment_time(UFDateTime payment_time) {
		this.payment_time = payment_time;
	}

	public UFDateTime getRegi_time() {
		return regi_time;
	}

	public void setRegi_time(UFDateTime regi_time) {
		this.regi_time = regi_time;
	}

	public String getRegi_person() {
		return regi_person;
	}

	public void setRegi_person(String regi_person) {
		this.regi_person = regi_person;
	}

	public String getPk_arrival() {
		return pk_arrival;
	}

	public void setPk_arrival(String pk_arrival) {
		this.pk_arrival = pk_arrival;
	}

	public String getPk_delivery() {
		return pk_delivery;
	}

	public void setPk_delivery(String pk_delivery) {
		this.pk_delivery = pk_delivery;
	}

	public UFDouble getArri_mileage() {
		return arri_mileage;
	}

	public void setArri_mileage(UFDouble arri_mileage) {
		this.arri_mileage = arri_mileage;
	}

	public UFDouble getDeli_mileage() {
		return deli_mileage;
	}

	public void setDeli_mileage(UFDouble deli_mileage) {
		this.deli_mileage = deli_mileage;
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

	public static String getPkToll() {
		return PK_TOLL;
	}

	public static String getPkTollB() {
		return PK_TOLL_B;
	}

	public TollBVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return "pk_toll";
	}

	@Override
	public String getPKFieldName() {
		return "pk_toll_b";
	}

	@Override
	public String getTableName() {
		return "ts_toll_b";
	}

}
