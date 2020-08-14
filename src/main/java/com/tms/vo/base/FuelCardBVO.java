package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class FuelCardBVO extends SuperVO {
	
	private String pk_fuelcard_b;
	private String pk_fuelcard;
	private String pk_refuel;
	private Integer operation_type;
	
	private String operator;
	private String operat_addr;
	private UFDateTime operat_date;
	private UFDouble amount;
	
	private String memo;
	private UFBoolean system_create;
	
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
	
	public static final String PK_FUELCARD_B = "pk_fuelcard_b";
	public static final String PK_FUELCARD = "pk_fuelcard";
	
	public String getPk_fuelcard_b() {
		return pk_fuelcard_b;
	}

	public void setPk_fuelcard_b(String pk_fuelcard_b) {
		this.pk_fuelcard_b = pk_fuelcard_b;
	}

	public String getPk_fuelcard() {
		return pk_fuelcard;
	}

	public void setPk_fuelcard(String pk_fuelcard) {
		this.pk_fuelcard = pk_fuelcard;
	}

	public String getPk_refuel() {
		return pk_refuel;
	}

	public void setPk_refuel(String pk_refuel) {
		this.pk_refuel = pk_refuel;
	}

	public Integer getOperation_type() {
		return operation_type;
	}

	public void setOperation_type(Integer operation_type) {
		this.operation_type = operation_type;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperat_addr() {
		return operat_addr;
	}

	public void setOperat_addr(String operat_addr) {
		this.operat_addr = operat_addr;
	}

	public UFDateTime getOperat_date() {
		return operat_date;
	}

	public void setOperat_date(UFDateTime operat_date) {
		this.operat_date = operat_date;
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

	public UFBoolean getSystem_create() {
		return system_create;
	}

	public void setSystem_create(UFBoolean system_create) {
		this.system_create = system_create;
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

	public static String getPkFuelcardB() {
		return PK_FUELCARD_B;
	}

	public static String getPkFuelcard() {
		return PK_FUELCARD;
	}

	public FuelCardBVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return "pk_fuelcard";
	}

	@Override
	public String getPKFieldName() {
		return "pk_fuelcard_b";
	}

	@Override
	public String getTableName() {
		return "ts_fuelcard_b";
	}

}
