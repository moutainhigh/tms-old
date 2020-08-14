package com.tms.vo.inv;

/**
 * ts_order_ass
 * 
 * @version 1.0
 * @since 1.0
 */
public class OrderAssVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_order_ass;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String order_type;

	private java.lang.String orderno;

	private java.lang.String item_name;

	private java.lang.String pk_supplier;

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

	public static final String PK_ORDER_ASS = "pk_order_ass";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String ORDER_TYPE = "order_type";
	public static final String ORDERNO = "orderno";
	public static final String ITEM_NAME = "item_name";
	public static final String PK_SUPPLIER = "pk_supplier";
	public static final String PK_DELIVERY = "pk_delivery";
	public static final String DELI_CITY = "deli_city";
	public static final String PK_ARRIVAL = "pk_arrival";
	public static final String ARRI_CITY = "arri_city";
	public static final String REQ_DELI_DATE = "req_deli_date";
	public static final String ACT_DELI_DATE = "act_deli_date";
	public static final String REQ_ARRI_DATE = "req_arri_date";
	public static final String ACT_ARRI_DATE = "act_arri_date";
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

	// 发货单的相应信息
	private java.lang.String pk_delivery;
	private java.lang.String deli_city;
	private java.lang.String pk_arrival;
	private java.lang.String arri_city;
	private java.lang.String req_deli_date;
	private java.lang.String act_deli_date;
	private java.lang.String req_arri_date;
	private java.lang.String act_arri_date;

	public java.lang.String getPk_order_ass() {
		return this.pk_order_ass;
	}

	public void setPk_order_ass(java.lang.String value) {
		this.pk_order_ass = value;
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

	public java.lang.String getOrder_type() {
		return this.order_type;
	}

	public void setOrder_type(java.lang.String value) {
		this.order_type = value;
	}

	public java.lang.String getOrderno() {
		return this.orderno;
	}

	public void setOrderno(java.lang.String value) {
		this.orderno = value;
	}

	public java.lang.String getItem_name() {
		return this.item_name;
	}

	public void setItem_name(java.lang.String value) {
		this.item_name = value;
	}

	public java.lang.String getPk_supplier() {
		return this.pk_supplier;
	}

	public void setPk_supplier(java.lang.String value) {
		this.pk_supplier = value;
	}

	public java.lang.String getPk_delivery() {
		return this.pk_delivery;
	}

	public void setPk_delivery(java.lang.String value) {
		this.pk_delivery = value;
	}

	public java.lang.String getDeli_city() {
		return this.deli_city;
	}

	public void setDeli_city(java.lang.String value) {
		this.deli_city = value;
	}

	public java.lang.String getPk_arrival() {
		return this.pk_arrival;
	}

	public void setPk_arrival(java.lang.String value) {
		this.pk_arrival = value;
	}

	public java.lang.String getArri_city() {
		return this.arri_city;
	}

	public void setArri_city(java.lang.String value) {
		this.arri_city = value;
	}

	public java.lang.String getReq_deli_date() {
		return this.req_deli_date;
	}

	public void setReq_deli_date(java.lang.String value) {
		this.req_deli_date = value;
	}

	public java.lang.String getAct_deli_date() {
		return this.act_deli_date;
	}

	public void setAct_deli_date(java.lang.String value) {
		this.act_deli_date = value;
	}

	public java.lang.String getReq_arri_date() {
		return this.req_arri_date;
	}

	public void setReq_arri_date(java.lang.String value) {
		this.req_arri_date = value;
	}

	public java.lang.String getAct_arri_date() {
		return this.act_arri_date;
	}

	public void setAct_arri_date(java.lang.String value) {
		this.act_arri_date = value;
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
		return null;
	}

	public String getPKFieldName() {
		return "pk_order_ass";
	}

	public String getTableName() {
		return "ts_order_ass";
	}
}
