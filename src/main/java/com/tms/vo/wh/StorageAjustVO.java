package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_storage_ajust
 * 
 * @version 1.0
 * @since 1.0
 */
public class StorageAjustVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_storage_ajust;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String vbillno;

	private java.lang.Integer vbillstatus;

	private java.lang.Integer order_type;

	private org.nw.vo.pub.lang.UFDate ajust_date;

	private java.lang.String src_customer;

	private java.lang.String dest_customer;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private org.nw.vo.pub.lang.UFBoolean sync_flag;

	private String pk_corp;

	private UFDate dbilldate;

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

	public static final String PK_STORAGE_AJUST = "pk_storage_ajust";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String VBILLNO = "vbillno";
	public static final String VBILLSTATUS = "vbillstatus";
	public static final String ORDER_TYPE = "order_type";
	public static final String AJUST_DATE = "ajust_date";
	public static final String SRC_CUSTOMER = "src_customer";
	public static final String DEST_CUSTOMER = "dest_customer";
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

	public java.lang.String getPk_storage_ajust() {
		return this.pk_storage_ajust;
	}

	public void setPk_storage_ajust(java.lang.String value) {
		this.pk_storage_ajust = value;
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

	public java.lang.Integer getOrder_type() {
		return this.order_type;
	}

	public void setOrder_type(java.lang.Integer value) {
		this.order_type = value;
	}

	public org.nw.vo.pub.lang.UFDate getAjust_date() {
		return this.ajust_date;
	}

	public void setAjust_date(org.nw.vo.pub.lang.UFDate value) {
		this.ajust_date = value;
	}

	public java.lang.String getSrc_customer() {
		return this.src_customer;
	}

	public void setSrc_customer(java.lang.String value) {
		this.src_customer = value;
	}

	public java.lang.String getDest_customer() {
		return this.dest_customer;
	}

	public void setDest_customer(java.lang.String value) {
		this.dest_customer = value;
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
		return "pk_storage_ajust";
	}

	public String getTableName() {
		return "ts_storage_ajust";
	}
}
