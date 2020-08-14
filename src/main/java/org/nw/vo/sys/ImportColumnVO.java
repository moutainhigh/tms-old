package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * nw_import_column
 * 
 * @version 1.0
 * @since 1.0
 */
public class ImportColumnVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_import_column;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_fun;

	private String fun_code;
	
	private String templet_fun_code;

	private java.lang.String field_name;

	private java.lang.String field_code;

	private org.nw.vo.pub.lang.UFBoolean keyfield_flag;

	private java.lang.String checker;
	private Integer display_order;

	private String pk_corp;
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;

	public UFDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
	}

	public String getCreate_user() {
		return create_user;
	}

	public void setCreate_user(String create_user) {
		this.create_user = create_user;
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

	public static final String PK_IMPORT_COLUMN = "pk_import_column";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_FUN = "pk_fun";
	public static final String FUN_CODE = "fun_code";
	public static final String FIELD_NAME = "field_name";
	public static final String FIELD_CODE = "field_code";
	public static final String KEYFIELD_FLAG = "keyfield_flag";
	public static final String CHECKER = "checker";
	public static final String DISPLAY_ORDER = "display_order";

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public Integer getDisplay_order() {
		return display_order;
	}

	public void setDisplay_order(Integer display_order) {
		this.display_order = display_order;
	}

	public String getFun_code() {
		return fun_code;
	}

	public void setFun_code(String fun_code) {
		this.fun_code = fun_code;
	}

	public String getTemplet_fun_code() {
		return templet_fun_code;
	}

	public void setTemplet_fun_code(String templet_fun_code) {
		this.templet_fun_code = templet_fun_code;
	}

	public java.lang.String getPk_import_column() {
		return this.pk_import_column;
	}

	public void setPk_import_column(java.lang.String value) {
		this.pk_import_column = value;
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

	public java.lang.String getPk_fun() {
		return pk_fun;
	}

	public void setPk_fun(java.lang.String pk_fun) {
		this.pk_fun = pk_fun;
	}

	public java.lang.String getField_name() {
		return this.field_name;
	}

	public void setField_name(java.lang.String value) {
		this.field_name = value;
	}

	public java.lang.String getField_code() {
		return this.field_code;
	}

	public void setField_code(java.lang.String value) {
		this.field_code = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getKeyfield_flag() {
		return this.keyfield_flag;
	}

	public void setKeyfield_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.keyfield_flag = value;
	}

	public java.lang.String getChecker() {
		return this.checker;
	}

	public void setChecker(java.lang.String value) {
		this.checker = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_import_column";
	}

	public String getTableName() {
		return "nw_import_column";
	}
}
