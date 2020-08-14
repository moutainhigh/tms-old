package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFBoolean;

/**
 * nw_portlet
 * 
 * @version 1.0
 * @since 1.0
 */
public class PortletVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_portlet;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String fun_code;

	private java.lang.String portlet_code;

	private java.lang.String portlet_name;

	private java.lang.Integer display_num;

	private java.lang.String title_format;

	private java.lang.String query_sql;
	private String query_where;
	private java.lang.String order_by;

	private java.lang.String buttons;

	private java.lang.String pk_corp;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private java.lang.String modify_user;
	
	private Integer sql_type;

	private String proce;
	
	public static final String PK_PORTLET = "pk_portlet";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String FUN_CODE = "fun_code";
	public static final String PORTLET_CODE = "portlet_code";
	public static final String PORTLET_NAME = "portlet_name";
	public static final String DISPLAY_NUM = "display_num";
	public static final String TITLE_FORMAT = "title_format";
	public static final String QUERY_SQL = "query_sql";
	public static final String QUERY_WHERE = "query_where";
	public static final String BUTTONS = "buttons";
	public static final String PK_CORP = "pk_corp";
	public static final String CREATE_TIME = "create_time";
	public static final String CREATE_USER = "create_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String MODIFY_USER = "modify_user";
	

	public Integer getSql_type() {
		return sql_type;
	}

	public void setSql_type(Integer sql_type) {
		this.sql_type = sql_type;
	}

	public String getProce() {
		return proce;
	}

	public void setProce(String proce) {
		this.proce = proce;
	}

	public String getQuery_where() {
		return query_where;
	}

	public void setQuery_where(String query_where) {
		this.query_where = query_where;
	}

	public java.lang.String getOrder_by() {
		return order_by;
	}

	public void setOrder_by(java.lang.String order_by) {
		this.order_by = order_by;
	}

	public java.lang.String getPk_portlet() {
		return this.pk_portlet;
	}

	public void setPk_portlet(java.lang.String value) {
		this.pk_portlet = value;
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

	public java.lang.String getFun_code() {
		return this.fun_code;
	}

	public void setFun_code(java.lang.String value) {
		this.fun_code = value;
	}

	public java.lang.String getPortlet_code() {
		return portlet_code;
	}

	public void setPortlet_code(java.lang.String portlet_code) {
		this.portlet_code = portlet_code;
	}

	public java.lang.String getPortlet_name() {
		return portlet_name;
	}

	public void setPortlet_name(java.lang.String portlet_name) {
		this.portlet_name = portlet_name;
	}

	public java.lang.Integer getDisplay_num() {
		return this.display_num;
	}

	public void setDisplay_num(java.lang.Integer value) {
		this.display_num = value;
	}

	public java.lang.String getTitle_format() {
		return this.title_format;
	}

	public void setTitle_format(java.lang.String value) {
		this.title_format = value;
	}

	public java.lang.String getQuery_sql() {
		return this.query_sql;
	}

	public void setQuery_sql(java.lang.String value) {
		this.query_sql = value;
	}

	public java.lang.String getButtons() {
		return this.buttons;
	}

	public void setButtons(java.lang.String value) {
		this.buttons = value;
	}

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.create_time = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getModify_time() {
		return this.modify_time;
	}

	public void setModify_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.modify_time = value;
	}

	public java.lang.String getModify_user() {
		return this.modify_user;
	}

	public void setModify_user(java.lang.String value) {
		this.modify_user = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_portlet";
	}

	public String getTableName() {
		return "nw_portlet";
	}
}
