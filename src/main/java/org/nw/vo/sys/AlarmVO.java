package org.nw.vo.sys;

/**
 * nw_alarm
 * 
 * @version 1.0
 * @since 1.0
 */
public class AlarmVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_alarm;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.Integer type;

	private java.lang.String billtype;

	private java.lang.String pk_bill;

	private java.lang.String fun_code;

	private java.lang.String sender_man;

	private org.nw.vo.pub.lang.UFDateTime sender_date;

	private java.lang.String title;

	private java.lang.String message;

	private java.lang.String deal_man;

	private org.nw.vo.pub.lang.UFDateTime deal_date;

	private org.nw.vo.pub.lang.UFBoolean deal_flag;

	private String pk_corp;

	private String billno;

	private String receiver_man;

	public static final String PK_ALARM = "pk_alarm";
	public static final String RECEIVER_MAN = "receiver_man";
	public static final String PK_CORP = "pk_corp";
	public static final String BILLNO = "billno";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String TYPE = "type";
	public static final String BILLTYPE = "billtype";
	public static final String PK_BILL = "pk_bill";
	public static final String FUN_CODE = "fun_code";
	public static final String SENDER_MAN = "sender_man";
	public static final String SENDER_DATE = "sender_date";
	public static final String TITLE = "title";
	public static final String MESSAGE = "message";
	public static final String DEAL_MAN = "deal_man";
	public static final String DEAL_DATE = "deal_date";
	public static final String DEAL_FLAG = "deal_flag";

	public String getReceiver_man() {
		return receiver_man;
	}

	public void setReceiver_man(String receiver_man) {
		this.receiver_man = receiver_man;
	}

	public String getBillno() {
		return billno;
	}

	public void setBillno(String billno) {
		this.billno = billno;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public java.lang.String getPk_alarm() {
		return this.pk_alarm;
	}

	public void setPk_alarm(java.lang.String value) {
		this.pk_alarm = value;
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

	public java.lang.Integer getType() {
		return this.type;
	}

	public void setType(java.lang.Integer value) {
		this.type = value;
	}

	public java.lang.String getBilltype() {
		return this.billtype;
	}

	public void setBilltype(java.lang.String value) {
		this.billtype = value;
	}

	public java.lang.String getPk_bill() {
		return this.pk_bill;
	}

	public void setPk_bill(java.lang.String value) {
		this.pk_bill = value;
	}

	public java.lang.String getFun_code() {
		return this.fun_code;
	}

	public void setFun_code(java.lang.String value) {
		this.fun_code = value;
	}

	public java.lang.String getSender_man() {
		return this.sender_man;
	}

	public void setSender_man(java.lang.String value) {
		this.sender_man = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getSender_date() {
		return sender_date;
	}

	public void setSender_date(org.nw.vo.pub.lang.UFDateTime sender_date) {
		this.sender_date = sender_date;
	}

	public java.lang.String getTitle() {
		return this.title;
	}

	public void setTitle(java.lang.String value) {
		this.title = value;
	}

	public java.lang.String getMessage() {
		return this.message;
	}

	public void setMessage(java.lang.String value) {
		this.message = value;
	}

	public java.lang.String getDeal_man() {
		return this.deal_man;
	}

	public void setDeal_man(java.lang.String value) {
		this.deal_man = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getDeal_date() {
		return this.deal_date;
	}

	public void setDeal_date(org.nw.vo.pub.lang.UFDateTime value) {
		this.deal_date = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getDeal_flag() {
		return this.deal_flag;
	}

	public void setDeal_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.deal_flag = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_alarm";
	}

	public String getTableName() {
		return "nw_alarm";
	}
}
