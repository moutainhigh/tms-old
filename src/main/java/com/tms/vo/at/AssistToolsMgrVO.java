package com.tms.vo.at;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_assist_tools_mgr
 * 
 * @version 1.0
 * @since 1.0
 */
public class AssistToolsMgrVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_assist_tools_mgr;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String vbillno;

	private java.lang.Integer vbillstatus;

	private java.lang.Integer origin;

	private java.lang.String invoice_vbillno;

	private java.lang.String pk_customer;

	private java.lang.String entrust_vbillno;

	private java.lang.String pk_carrier;

	private org.nw.vo.pub.lang.UFBoolean is_deposit;

	private org.nw.vo.pub.lang.UFDouble deposit_amount;

	private org.nw.vo.pub.lang.UFDate grant_date;

	private java.lang.String grant_man;

	private org.nw.vo.pub.lang.UFDate req_return_date;

	private java.lang.String grant_memo;

	private org.nw.vo.pub.lang.UFBoolean is_return_deposit;

	private org.nw.vo.pub.lang.UFDate return_date;

	private java.lang.String return_man;

	private java.lang.String return_memo;

	private java.lang.String pk_corp;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private UFDate dbilldate;
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

	public static final String PK_ASSIST_TOOLS_MGR = "pk_assist_tools_mgr";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String VBILLNO = "vbillno";
	public static final String VBILLSTATUS = "vbillstatus";
	public static final String ORIGIN = "origin";
	public static final String INVOICE_VBILLNO = "invoice_vbillno";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String ENTRUST_VBILLNO = "entrust_vbillno";
	public static final String PK_CARRIER = "pk_carrier";
	public static final String IS_DEPOSIT = "is_deposit";
	public static final String DEPOSIT_AMOUNT = "deposit_amount";
	public static final String GRANT_DATE = "grant_date";
	public static final String GRANT_MAN = "grant_man";
	public static final String REQ_RETURN_DATE = "req_return_date";
	public static final String GRANT_MEMO = "grant_memo";
	public static final String IS_RETURN_DEPOSIT = "is_return_deposit";
	public static final String RETURN_DATE = "return_date";
	public static final String RETURN_MAN = "return_man";
	public static final String RETURN_MEMO = "return_memo";
	public static final String PK_CORP = "pk_corp";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String DBILLDATE = "dbilldate";

	public UFDate getDbilldate() {
		return dbilldate;
	}

	public void setDbilldate(UFDate dbilldate) {
		this.dbilldate = dbilldate;
	}

	public java.lang.String getPk_assist_tools_mgr() {
		return this.pk_assist_tools_mgr;
	}

	public void setPk_assist_tools_mgr(java.lang.String value) {
		this.pk_assist_tools_mgr = value;
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

	public java.lang.Integer getOrigin() {
		return this.origin;
	}

	public void setOrigin(java.lang.Integer value) {
		this.origin = value;
	}

	public java.lang.String getInvoice_vbillno() {
		return this.invoice_vbillno;
	}

	public void setInvoice_vbillno(java.lang.String value) {
		this.invoice_vbillno = value;
	}

	public java.lang.String getPk_customer() {
		return this.pk_customer;
	}

	public void setPk_customer(java.lang.String value) {
		this.pk_customer = value;
	}

	public java.lang.String getEntrust_vbillno() {
		return this.entrust_vbillno;
	}

	public void setEntrust_vbillno(java.lang.String value) {
		this.entrust_vbillno = value;
	}

	public java.lang.String getPk_carrier() {
		return this.pk_carrier;
	}

	public void setPk_carrier(java.lang.String value) {
		this.pk_carrier = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIs_deposit() {
		return this.is_deposit;
	}

	public void setIs_deposit(org.nw.vo.pub.lang.UFBoolean value) {
		this.is_deposit = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDeposit_amount() {
		return this.deposit_amount;
	}

	public void setDeposit_amount(org.nw.vo.pub.lang.UFDouble value) {
		this.deposit_amount = value;
	}

	public org.nw.vo.pub.lang.UFDate getGrant_date() {
		return this.grant_date;
	}

	public void setGrant_date(org.nw.vo.pub.lang.UFDate value) {
		this.grant_date = value;
	}

	public java.lang.String getGrant_man() {
		return this.grant_man;
	}

	public void setGrant_man(java.lang.String value) {
		this.grant_man = value;
	}

	public org.nw.vo.pub.lang.UFDate getReq_return_date() {
		return this.req_return_date;
	}

	public void setReq_return_date(org.nw.vo.pub.lang.UFDate value) {
		this.req_return_date = value;
	}

	public java.lang.String getGrant_memo() {
		return this.grant_memo;
	}

	public void setGrant_memo(java.lang.String value) {
		this.grant_memo = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIs_return_deposit() {
		return this.is_return_deposit;
	}

	public void setIs_return_deposit(org.nw.vo.pub.lang.UFBoolean value) {
		this.is_return_deposit = value;
	}

	public org.nw.vo.pub.lang.UFDate getReturn_date() {
		return this.return_date;
	}

	public void setReturn_date(org.nw.vo.pub.lang.UFDate value) {
		this.return_date = value;
	}

	public java.lang.String getReturn_man() {
		return this.return_man;
	}

	public void setReturn_man(java.lang.String value) {
		this.return_man = value;
	}

	public java.lang.String getReturn_memo() {
		return this.return_memo;
	}

	public void setReturn_memo(java.lang.String value) {
		this.return_memo = value;
	}

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
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

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_assist_tools_mgr";
	}

	public String getTableName() {
		return "ts_assist_tools_mgr";
	}
}
