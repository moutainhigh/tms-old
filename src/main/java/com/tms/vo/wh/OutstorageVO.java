package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;

/**
 * TS_OUTSTORAGE
 * 
 * @version 1.0
 * @since 1.0
 */
public class OutstorageVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_outstorage;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String vbillno;

	private java.lang.Integer vbillstatus;

	private java.lang.String cust_orderno;

	private java.lang.String orderno;

	private java.lang.Integer order_type;

	private java.lang.String pk_customer;

	private String req_deli_date;

	private String req_arri_date;

	private String req_ship_date;

	private String act_ship_date;

	private java.lang.String pk_shiper;

	private java.lang.String province;

	private java.lang.String city;

	private java.lang.String zipcode;

	private java.lang.String phone;

	private java.lang.String contact;

	private java.lang.String address;

	private java.lang.String pk_carrier;

	private java.lang.String pk_driver;

	private java.lang.String carno;

	private org.nw.vo.pub.lang.UFDouble order_count;

	private org.nw.vo.pub.lang.UFDouble picked_count;
	private org.nw.vo.pub.lang.UFDouble shiped_count;

	private org.nw.vo.pub.lang.UFDouble weight_count;

	private org.nw.vo.pub.lang.UFDouble volume_count;

	private java.lang.String memo;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;
	private UFDate dbilldate;
	private String pk_corp;

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	private String def10;
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private UFDouble def11;
	private UFDouble def12;

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

	public String getDef10() {
		return def10;
	}

	public void setDef10(String def10) {
		this.def10 = def10;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
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

	public String getDef9() {
		return def9;
	}

	public void setDef9(String def9) {
		this.def9 = def9;
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

	private org.nw.vo.pub.lang.UFBoolean sync_flag;
	private Integer origin;
	private String service_orderno;
	private String invoice_vbillno;
	private String pk_delivery;
	private String pk_arrival;
	private String pk_trans_type;

	public Integer getOrigin() {
		return origin;
	}

	public void setOrigin(Integer origin) {
		this.origin = origin;
	}

	public String getService_orderno() {
		return service_orderno;
	}

	public void setService_orderno(String service_orderno) {
		this.service_orderno = service_orderno;
	}

	public String getInvoice_vbillno() {
		return invoice_vbillno;
	}

	public void setInvoice_vbillno(String invoice_vbillno) {
		this.invoice_vbillno = invoice_vbillno;
	}

	public String getPk_delivery() {
		return pk_delivery;
	}

	public void setPk_delivery(String pk_delivery) {
		this.pk_delivery = pk_delivery;
	}

	public String getPk_arrival() {
		return pk_arrival;
	}

	public void setPk_arrival(String pk_arrival) {
		this.pk_arrival = pk_arrival;
	}

	public String getPk_trans_type() {
		return pk_trans_type;
	}

	public void setPk_trans_type(String pk_trans_type) {
		this.pk_trans_type = pk_trans_type;
	}

	public static final String PK_OUTSTORAGE = "pk_outstorage";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String VBILLNO = "vbillno";
	public static final String VBILLSTATUS = "vbillstatus";
	public static final String CUST_ORDERNO = "cust_orderno";
	public static final String ORDERNO = "orderno";
	public static final String ORDER_TYPE = "order_type";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String REQ_DELI_DATE = "req_deli_date";
	public static final String REQ_ARRI_DATE = "req_arri_date";
	public static final String REQ_SHIP_DATE = "req_ship_date";
	public static final String ACT_SHIP_DATE = "act_ship_date";
	public static final String PK_SHIPER = "pk_shiper";
	public static final String PROVINCE = "province";
	public static final String CITY = "city";
	public static final String ZIPCODE = "zipcode";
	public static final String PHONE = "phone";
	public static final String CONTACT = "contact";
	public static final String ADDRESS = "address";
	public static final String PK_CARRIER = "pk_carrier";
	public static final String PK_DRIVER = "pk_driver";
	public static final String CARNO = "carno";
	public static final String ORDER_COUNT = "order_count";
	public static final String PICKED_COUNT = "picked_count";
	public static final String SHIPED_COUNT = "shiped_count";
	public static final String WEIGHT_COUNT = "weight_count";
	public static final String VOLUME_COUNT = "volume_count";
	public static final String MEMO = "memo";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String SYNC_FLAG = "sync_flag";
	public static final String DBILLDATE = "dbilldate";

	public UFDate getDbilldate() {
		return dbilldate;
	}

	public void setDbilldate(UFDate dbilldate) {
		this.dbilldate = dbilldate;
	}

	public java.lang.String getPk_outstorage() {
		return this.pk_outstorage;
	}

	public void setPk_outstorage(java.lang.String value) {
		this.pk_outstorage = value;
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

	public java.lang.String getVbillno() {
		return this.vbillno;
	}

	public void setVbillno(java.lang.String value) {
		this.vbillno = value;
	}

	public java.lang.Integer getVbillstatus() {
		return this.vbillstatus;
	}

	public void setVbillstatus(java.lang.Integer value) {
		this.vbillstatus = value;
	}

	public java.lang.String getCust_orderno() {
		return this.cust_orderno;
	}

	public void setCust_orderno(java.lang.String value) {
		this.cust_orderno = value;
	}

	public java.lang.String getOrderno() {
		return this.orderno;
	}

	public void setOrderno(java.lang.String value) {
		this.orderno = value;
	}

	public java.lang.Integer getOrder_type() {
		return this.order_type;
	}

	public void setOrder_type(java.lang.Integer value) {
		this.order_type = value;
	}

	public java.lang.String getPk_customer() {
		return this.pk_customer;
	}

	public void setPk_customer(java.lang.String value) {
		this.pk_customer = value;
	}

	public String getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(String req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public String getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(String req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public String getReq_ship_date() {
		return req_ship_date;
	}

	public void setReq_ship_date(String req_ship_date) {
		this.req_ship_date = req_ship_date;
	}

	public String getAct_ship_date() {
		return act_ship_date;
	}

	public void setAct_ship_date(String act_ship_date) {
		this.act_ship_date = act_ship_date;
	}

	public java.lang.String getPk_shiper() {
		return pk_shiper;
	}

	public void setPk_shiper(java.lang.String pk_shiper) {
		this.pk_shiper = pk_shiper;
	}

	public java.lang.String getProvince() {
		return this.province;
	}

	public void setProvince(java.lang.String value) {
		this.province = value;
	}

	public java.lang.String getCity() {
		return this.city;
	}

	public void setCity(java.lang.String value) {
		this.city = value;
	}

	public java.lang.String getZipcode() {
		return this.zipcode;
	}

	public void setZipcode(java.lang.String value) {
		this.zipcode = value;
	}

	public java.lang.String getPhone() {
		return this.phone;
	}

	public void setPhone(java.lang.String value) {
		this.phone = value;
	}

	public java.lang.String getContact() {
		return this.contact;
	}

	public void setContact(java.lang.String value) {
		this.contact = value;
	}

	public java.lang.String getAddress() {
		return this.address;
	}

	public void setAddress(java.lang.String value) {
		this.address = value;
	}

	public java.lang.String getPk_carrier() {
		return this.pk_carrier;
	}

	public void setPk_carrier(java.lang.String value) {
		this.pk_carrier = value;
	}

	public java.lang.String getPk_driver() {
		return this.pk_driver;
	}

	public void setPk_driver(java.lang.String value) {
		this.pk_driver = value;
	}

	public java.lang.String getCarno() {
		return this.carno;
	}

	public void setCarno(java.lang.String value) {
		this.carno = value;
	}

	public org.nw.vo.pub.lang.UFDouble getOrder_count() {
		return order_count;
	}

	public void setOrder_count(org.nw.vo.pub.lang.UFDouble order_count) {
		this.order_count = order_count;
	}

	public org.nw.vo.pub.lang.UFDouble getPicked_count() {
		return picked_count;
	}

	public void setPicked_count(org.nw.vo.pub.lang.UFDouble picked_count) {
		this.picked_count = picked_count;
	}

	public org.nw.vo.pub.lang.UFDouble getShiped_count() {
		return shiped_count;
	}

	public void setShiped_count(org.nw.vo.pub.lang.UFDouble shiped_count) {
		this.shiped_count = shiped_count;
	}

	public org.nw.vo.pub.lang.UFDouble getWeight_count() {
		return weight_count;
	}

	public void setWeight_count(org.nw.vo.pub.lang.UFDouble weight_count) {
		this.weight_count = weight_count;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume_count() {
		return this.volume_count;
	}

	public void setVolume_count(org.nw.vo.pub.lang.UFDouble value) {
		this.volume_count = value;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.create_time = value;
	}

	public java.lang.String getModify_user() {
		return this.modify_user;
	}

	public void setModify_user(java.lang.String value) {
		this.modify_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getModify_time() {
		return this.modify_time;
	}

	public void setModify_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.modify_time = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getSync_flag() {
		return this.sync_flag;
	}

	public void setSync_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.sync_flag = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_outstorage";
	}

	public String getTableName() {
		return "TS_OUTSTORAGE";
	}
}
