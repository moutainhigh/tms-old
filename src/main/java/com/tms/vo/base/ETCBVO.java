package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ETCBVO extends SuperVO {
	
	private String pk_etc_b;
	private String pk_etc;
	private String pk_toll;
	private Integer operation_type;
	
	private String operator;
	private String operat_addr;
	private UFDateTime operat_date;
	private UFDouble amount;
	private UFBoolean system_create;
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
	
	public static final String PK_ETC_B = "pk_etc_b";
	public static final String PK_ETC = "pk_etc";
	public static final String TS_ETC_B = "ts_etc_b";
	

	public String getPk_etc_b() {
		return pk_etc_b;
	}

	public void setPk_etc_b(String pk_etc_b) {
		this.pk_etc_b = pk_etc_b;
	}

	public String getPk_etc() {
		return pk_etc;
	}

	public void setPk_etc(String pk_etc) {
		this.pk_etc = pk_etc;
	}

	public String getPk_toll() {
		return pk_toll;
	}

	public void setPk_toll(String pk_toll) {
		this.pk_toll = pk_toll;
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

	public UFBoolean getSystem_create() {
		return system_create;
	}

	public void setSystem_create(UFBoolean system_create) {
		this.system_create = system_create;
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

	public ETCBVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return PK_ETC;
	}

	@Override
	public String getPKFieldName() {
		return PK_ETC_B;
	}

	@Override
	public String getTableName() {
		return TS_ETC_B;
	}

}
