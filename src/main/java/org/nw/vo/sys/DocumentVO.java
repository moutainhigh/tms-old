package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * nw_document
 * 
 * @version 1.0
 * @since 1.0
 */
public class DocumentVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_document;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String serialno;

	private java.lang.String fileno;

	private java.lang.String post_org;

	private org.nw.vo.pub.lang.UFDate post_date;

	private java.lang.String title;

	private java.lang.String file_name;

	private java.lang.String summary;

	private byte[] contentdata;

	private java.lang.Long file_size;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;
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

	private String userfile;
	private String pk_corp;
	public static final String PK_DOCUMENT = "pk_document";
	public static final String PK_CORP = "pk_corp";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String SERIALNO = "serialno";
	public static final String FILENO = "fileno";
	public static final String POST_ORG = "post_org";
	public static final String POST_DATE = "post_date";
	public static final String TITLE = "title";
	public static final String FILE_NAME = "file_name";
	public static final String SUMMARY = "summary";
	public static final String CONTENTDATA = "contentdata";
	public static final String FILE_SIZE = "file_size";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getUserfile() {
		return userfile;
	}

	public void setUserfile(String userfile) {
		this.userfile = userfile;
	}

	public java.lang.String getPk_document() {
		return pk_document;
	}

	public void setPk_document(java.lang.String pk_document) {
		this.pk_document = pk_document;
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

	public java.lang.String getSerialno() {
		return this.serialno;
	}

	public void setSerialno(java.lang.String value) {
		this.serialno = value;
	}

	public java.lang.String getFileno() {
		return this.fileno;
	}

	public void setFileno(java.lang.String value) {
		this.fileno = value;
	}

	public java.lang.String getPost_org() {
		return this.post_org;
	}

	public void setPost_org(java.lang.String value) {
		this.post_org = value;
	}

	public org.nw.vo.pub.lang.UFDate getPost_date() {
		return this.post_date;
	}

	public void setPost_date(org.nw.vo.pub.lang.UFDate value) {
		this.post_date = value;
	}

	public java.lang.String getTitle() {
		return this.title;
	}

	public void setTitle(java.lang.String value) {
		this.title = value;
	}

	public java.lang.String getFile_name() {
		return this.file_name;
	}

	public void setFile_name(java.lang.String value) {
		this.file_name = value;
	}

	public java.lang.String getSummary() {
		return this.summary;
	}

	public void setSummary(java.lang.String value) {
		this.summary = value;
	}

	public byte[] getContentdata() {
		return this.contentdata;
	}

	public void setContentdata(byte[] value) {
		this.contentdata = value;
	}

	public java.lang.Long getFile_size() {
		return file_size;
	}

	public void setFile_size(java.lang.Long file_size) {
		this.file_size = file_size;
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
		return "pk_document";
	}

	public String getTableName() {
		return "nw_document";
	}
}
