package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * ls_operate_type
 * 
 * @version 1.0
 * @since 1.0
 */
public class JobDefVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_job_def;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String job_code;

	private java.lang.String job_name;

	private org.nw.vo.pub.lang.UFDate begin_date;

	private org.nw.vo.pub.lang.UFDate end_date;

	private java.lang.Integer job_interval;

	// 2015-06-03 执行类型，执行时间
	private String exec_time;
	private Integer exec_type;

	private java.lang.String busi_clazz;
	private String convert_clazz;
	private java.lang.String url;
	private String api_type;

	private String username_param;
	private String password_param;
	private String url_method;

	private java.lang.String username;

	private java.lang.String password;

	private java.lang.String memo;

	private java.lang.String pk_corp;

	private UFDateTime create_time;
	private String modify_user;
	private String create_user;
	private UFDateTime modify_time;

	private UFBoolean locked_flag;

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

	public static final String PK_JOB_DEF = "pk_job_def";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String JOB_CODE = "job_code";
	public static final String JOB_NAME = "job_name";
	public static final String BEGIN_DATE = "begin_date";
	public static final String END_DATE = "end_date";
	public static final String INTERVAL = "interval";
	public static final String REPEAT_TIME = "repeat_time";
	public static final String BUSI_CLAZZ = "busi_clazz";
	public static final String CONVERT_CLAZZ = "convert_clazz";
	public static final String MEMO = "memo";
	public static final String PK_CORP = "pk_corp";
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
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String CREATE_USER = "create_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String LOCKED_FLAG = "locked_flag";

	public java.lang.String getUrl() {
		return url;
	}

	public void setUrl(java.lang.String url) {
		this.url = url;
	}

	public String getApi_type() {
		return api_type;
	}

	public void setApi_type(String api_type) {
		this.api_type = api_type;
	}

	public String getUsername_param() {
		return username_param;
	}

	public void setUsername_param(String username_param) {
		this.username_param = username_param;
	}

	public String getPassword_param() {
		return password_param;
	}

	public void setPassword_param(String password_param) {
		this.password_param = password_param;
	}

	public String getUrl_method() {
		return url_method;
	}

	public void setUrl_method(String url_method) {
		this.url_method = url_method;
	}

	public java.lang.String getUsername() {
		return username;
	}

	public void setUsername(java.lang.String username) {
		this.username = username;
	}

	public java.lang.String getPassword() {
		return password;
	}

	public void setPassword(java.lang.String password) {
		this.password = password;
	}

	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	public void setLocked_flag(UFBoolean locked_flag) {
		this.locked_flag = locked_flag;
	}

	public UFDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
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

	public String getConvert_clazz() {
		return convert_clazz;
	}

	public void setConvert_clazz(String convert_clazz) {
		this.convert_clazz = convert_clazz;
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

	public org.nw.vo.pub.lang.UFDate getBegin_date() {
		return this.begin_date;
	}

	public void setBegin_date(org.nw.vo.pub.lang.UFDate value) {
		this.begin_date = value;
	}

	public org.nw.vo.pub.lang.UFDate getEnd_date() {
		return this.end_date;
	}

	public void setEnd_date(org.nw.vo.pub.lang.UFDate value) {
		this.end_date = value;
	}

	public java.lang.Integer getJob_interval() {
		return job_interval;
	}

	public void setJob_interval(java.lang.Integer job_interval) {
		this.job_interval = job_interval;
	}

	public String getExec_time() {
		return exec_time;
	}

	public void setExec_time(String exec_time) {
		this.exec_time = exec_time;
	}

	public Integer getExec_type() {
		return exec_type;
	}

	public void setExec_type(Integer exec_type) {
		this.exec_type = exec_type;
	}

	public java.lang.String getPk_job_def() {
		return pk_job_def;
	}

	public void setPk_job_def(java.lang.String pk_job_def) {
		this.pk_job_def = pk_job_def;
	}

	public java.lang.String getJob_code() {
		return job_code;
	}

	public void setJob_code(java.lang.String job_code) {
		this.job_code = job_code;
	}

	public java.lang.String getJob_name() {
		return job_name;
	}

	public void setJob_name(java.lang.String job_name) {
		this.job_name = job_name;
	}

	public java.lang.String getBusi_clazz() {
		return busi_clazz;
	}

	public void setBusi_clazz(java.lang.String busi_clazz) {
		this.busi_clazz = busi_clazz;
	}

	public java.lang.String getDef6() {
		return def6;
	}

	public void setDef6(java.lang.String def6) {
		this.def6 = def6;
	}

	public java.lang.String getDef7() {
		return def7;
	}

	public void setDef7(java.lang.String def7) {
		this.def7 = def7;
	}

	public java.lang.String getDef8() {
		return def8;
	}

	public void setDef8(java.lang.String def8) {
		this.def8 = def8;
	}

	public java.lang.String getDef9() {
		return def9;
	}

	public void setDef9(java.lang.String def9) {
		this.def9 = def9;
	}

	public java.lang.String getDef10() {
		return def10;
	}

	public void setDef10(java.lang.String def10) {
		this.def10 = def10;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
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

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_job_def";
	}

	public String getTableName() {
		return "nw_job_def";
	}
}
