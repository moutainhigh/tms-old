package com.tms.vo.base;

/**
 * ts_cust_rate
 * 
 * @version 1.0
 * @since 1.0
 */
public class CustRateVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_cust_rate;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_customer;

	private java.lang.String pk_trans_type;

	private org.nw.vo.pub.lang.UFDouble rate;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;

	private java.lang.String def6;

	private java.lang.String def7;

	private java.lang.String def8;

	private java.lang.String def9;

	private java.lang.String def10;

	private org.nw.vo.pub.lang.UFDouble def11;

	private org.nw.vo.pub.lang.UFDouble def12;
	
	private java.lang.String start_area;
	private java.lang.String end_area;

	public static final String PK_CUST_FEE = "pk_cust_rate";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String PK_TRANS_TYPE = "pk_trans_type";
	public static final String RATE = "rate";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String DEF6 = "def6";
	public static final String DEF7 = "def7";
	public static final String DEF8 = "def8";
	public static final String DEF9 = "def9";
	public static final String DEF10 = "def10";
	public static final String DEF11 = "def11";
	public static final String DEF12 = "def12";

	public java.lang.String getPk_cust_rate() {
		return pk_cust_rate;
	}

	public void setPk_cust_rate(java.lang.String pk_cust_rate) {
		this.pk_cust_rate = pk_cust_rate;
	}

	

	public java.lang.String getStart_area() {
		return start_area;
	}

	public void setStart_area(java.lang.String start_area) {
		this.start_area = start_area;
	}

	public java.lang.String getEnd_area() {
		return end_area;
	}

	public void setEnd_area(java.lang.String end_area) {
		this.end_area = end_area;
	}

	public java.lang.Integer getDr() {
		return this.dr;
	}

	public void setDr(java.lang.Integer value) {
		this.dr = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getTs() {
		return this.ts;
	}

	public void setTs(org.nw.vo.pub.lang.UFDateTime value) {
		this.ts = value;
	}

	public java.lang.String getPk_customer() {
		return this.pk_customer;
	}

	public void setPk_customer(java.lang.String value) {
		this.pk_customer = value;
	}

	public java.lang.String getPk_trans_type() {
		return this.pk_trans_type;
	}

	public void setPk_trans_type(java.lang.String value) {
		this.pk_trans_type = value;
	}

	public org.nw.vo.pub.lang.UFDouble getRate() {
		return this.rate;
	}

	public void setRate(org.nw.vo.pub.lang.UFDouble value) {
		this.rate = value;
	}

	public java.lang.String getDef1() {
		return this.def1;
	}

	public void setDef1(java.lang.String value) {
		this.def1 = value;
	}

	public java.lang.String getDef2() {
		return this.def2;
	}

	public void setDef2(java.lang.String value) {
		this.def2 = value;
	}

	public java.lang.String getDef3() {
		return this.def3;
	}

	public void setDef3(java.lang.String value) {
		this.def3 = value;
	}

	public java.lang.String getDef4() {
		return this.def4;
	}

	public void setDef4(java.lang.String value) {
		this.def4 = value;
	}

	public java.lang.String getDef5() {
		return this.def5;
	}

	public void setDef5(java.lang.String value) {
		this.def5 = value;
	}

	public java.lang.String getDef6() {
		return this.def6;
	}

	public void setDef6(java.lang.String value) {
		this.def6 = value;
	}

	public java.lang.String getDef7() {
		return this.def7;
	}

	public void setDef7(java.lang.String value) {
		this.def7 = value;
	}

	public java.lang.String getDef8() {
		return this.def8;
	}

	public void setDef8(java.lang.String value) {
		this.def8 = value;
	}

	public java.lang.String getDef9() {
		return this.def9;
	}

	public void setDef9(java.lang.String value) {
		this.def9 = value;
	}

	public java.lang.String getDef10() {
		return this.def10;
	}

	public void setDef10(java.lang.String value) {
		this.def10 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef11() {
		return this.def11;
	}

	public void setDef11(org.nw.vo.pub.lang.UFDouble value) {
		this.def11 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef12() {
		return this.def12;
	}

	public void setDef12(org.nw.vo.pub.lang.UFDouble value) {
		this.def12 = value;
	}

	public String getParentPKFieldName() {
		return "pk_customer";
	}

	public String getPKFieldName() {
		return "pk_cust_rate";
	}

	public String getTableName() {
		return "ts_cust_rate";
	}
}
