package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * nw_import_config
 * 
 * @version 1.0
 * @since 1.0
 */
public class ImportConfigVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_import_config;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_fun;

	private java.lang.String fun_code;

	private java.lang.String checker;

	private java.lang.String pk_corp;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;

	private java.lang.String templet_file;
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	private String importer;
	

	public String getImporter() {
		return importer;
	}

	public void setImporter(String importer) {
		this.importer = importer;
	}

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

	public static final String PK_IMPORT_CONFIG = "pk_import_config";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_FUN = "pk_fun";
	public static final String FUN_CODE = "fun_code";
	public static final String CHECKER = "checker";
	public static final String PK_CORP = "pk_corp";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String TEMPLET_FILE = "templet_file";

	public java.lang.String getPk_import_config() {
		return this.pk_import_config;
	}

	public void setPk_import_config(java.lang.String value) {
		this.pk_import_config = value;
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
		return this.pk_fun;
	}

	public void setPk_fun(java.lang.String value) {
		this.pk_fun = value;
	}

	public java.lang.String getFun_code() {
		return this.fun_code;
	}

	public void setFun_code(java.lang.String value) {
		this.fun_code = value;
	}

	public java.lang.String getChecker() {
		return this.checker;
	}

	public void setChecker(java.lang.String value) {
		this.checker = value;
	}

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
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

	public java.lang.String getTemplet_file() {
		return this.templet_file;
	}

	public void setTemplet_file(java.lang.String value) {
		this.templet_file = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_import_config";
	}

	public String getTableName() {
		return "nw_import_config";
	}
}
