package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;


@SuppressWarnings("serial")
public class CarPayBVO extends SuperVO {
	
	private String pk_car_pay_b;
	private String pk_car;
	private Integer dr;
	private String ts;
	
	private Integer check_type;
	private String pay_type; 
	private UFDouble amount;   
	private String memo;   
	
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
	
	public static final String PK_CAR_PAY_B = "pk_car_pay_b";
	public static final String PK_CAR = "pk_car";
	public static final String TS_CAR_PAY_B = "ts_car_pay_b";


	

	public String getPk_car_pay_b() {
		return pk_car_pay_b;
	}

	public void setPk_car_pay_b(String pk_car_pay_b) {
		this.pk_car_pay_b = pk_car_pay_b;
	}

	public String getPk_car() {
		return pk_car;
	}

	public void setPk_car(String pk_car) {
		this.pk_car = pk_car;
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

	public Integer getCheck_type() {
		return check_type;
	}

	public void setCheck_type(Integer check_type) {
		this.check_type = check_type;
	}

	public String getPay_type() {
		return pay_type;
	}

	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}

	public UFDouble getAmount() {
		return amount;
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
		return PK_CAR;
	}

	@Override
	public String getPKFieldName() {
		return PK_CAR_PAY_B;
	}

	@Override
	public String getTableName() {
		return TS_CAR_PAY_B;
	}

}
