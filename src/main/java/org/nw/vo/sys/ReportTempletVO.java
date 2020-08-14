package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * nw_report_templet
 * 
 * @version 1.0
 * @since 1.0
 */
public class ReportTempletVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;
	private java.lang.String pk_report_templet;
	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_corp;

	private java.lang.String nodecode;

	private java.lang.String vtemplatecode;

	private java.lang.String vtemplatename;

	private String select_sql;
	private String order_by;

	private java.lang.String memo;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;
	private UFDateTime modify_time;
	private String modify_user;
	
	private String query_where;
	
	public String getQuery_where() {
		return query_where;
	}

	public void setQuery_where(String query_where) {
		this.query_where = query_where;
	}

	public UFDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(UFDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	public static final String PK_REPORT_TEMPLET = "pk_report_templet";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_CORP = "pk_corp";
	public static final String NODECODE = "nodecode";
	public static final String VTEMPLATECODE = "vtemplatecode";
	public static final String VTEMPLATENAME = "vtemplatename";
	public static final String MEMO = "memo";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String SELECT_SQL = "select_sql";
	public static final String ORDER_BY = "order_by";

	public String getSelect_sql() {
		return select_sql;
	}

	public void setSelect_sql(String select_sql) {
		this.select_sql = select_sql;
	}

	public String getOrder_by() {
		return order_by;
	}

	public void setOrder_by(String order_by) {
		this.order_by = order_by;
	}

	public java.lang.String getPk_report_templet() {
		return this.pk_report_templet;
	}

	public void setPk_report_templet(java.lang.String value) {
		this.pk_report_templet = value;
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

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
	}

	public java.lang.String getNodecode() {
		return this.nodecode;
	}

	public void setNodecode(java.lang.String value) {
		this.nodecode = value;
	}

	public java.lang.String getVtemplatecode() {
		return this.vtemplatecode;
	}

	public void setVtemplatecode(java.lang.String value) {
		this.vtemplatecode = value;
	}

	public java.lang.String getVtemplatename() {
		return this.vtemplatename;
	}

	public void setVtemplatename(java.lang.String value) {
		this.vtemplatename = value;
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

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_report_templet";
	}

	public String getTableName() {
		return "nw_report_templet";
	}
}
