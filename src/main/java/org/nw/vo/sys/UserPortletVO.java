package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * nw_user_portlet
 * 
 * @version 1.0
 * @since 1.0
 * @deprecated
 */
public class UserPortletVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_user_portlet;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_user;

	private java.lang.String portlet_id;

	private java.lang.Integer column_index;

	private java.lang.Integer display_order;

	private java.lang.String create_time;

	private java.lang.String create_user;

	private String pk_corp;
	private UFDateTime modify_time;
	private String modify_user;

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

	public static final String PK_USER_PORTLET = "pk_user_portlet";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_USER = "pk_user";
	public static final String PORTLET_ID = "portlet_id";
	public static final String COLUMN_INDEX = "column_index";
	public static final String DISPLAY_ORDER = "display_order";
	public static final String CREATE_TIME = "create_time";
	public static final String CREATE_USER = "create_user";
	public static final String PK_CORP = "pk_corp";

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public java.lang.String getPk_user_portlet() {
		return this.pk_user_portlet;
	}

	public void setPk_user_portlet(java.lang.String value) {
		this.pk_user_portlet = value;
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

	public java.lang.String getPk_user() {
		return this.pk_user;
	}

	public void setPk_user(java.lang.String value) {
		this.pk_user = value;
	}

	public java.lang.String getPortlet_id() {
		return this.portlet_id;
	}

	public void setPortlet_id(java.lang.String value) {
		this.portlet_id = value;
	}

	public java.lang.Integer getColumn_index() {
		return column_index;
	}

	public void setColumn_index(java.lang.Integer column_index) {
		this.column_index = column_index;
	}

	public java.lang.Integer getDisplay_order() {
		return this.display_order;
	}

	public void setDisplay_order(java.lang.Integer value) {
		this.display_order = value;
	}

	public java.lang.String getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(java.lang.String value) {
		this.create_time = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_user_portlet";
	}

	public String getTableName() {
		return "nw_user_portlet";
	}
}
