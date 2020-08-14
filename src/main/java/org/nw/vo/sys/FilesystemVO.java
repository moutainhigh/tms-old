package org.nw.vo.sys;

/**
 * nw_filesystem
 * 
 * @version 1.0
 * @since 1.0
 */
public class FilesystemVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_filesystem;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String billtype;

	private java.lang.String pk_bill;

	private org.nw.vo.pub.lang.UFBoolean folder_flag;

	private java.lang.Long file_size;

	private byte[] contentdata;

	private java.lang.String md5;

	private java.lang.String parent_id;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String file_name;

	public static final String PK_FILESYSTEM = "pk_filesystem";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String BILLTYPE = "billtype";
	public static final String PK_BILL = "pk_bill";
	public static final String FOLDER_FLAG = "folder_flag";
	public static final String FILE_SIZE = "file_size";
	public static final String FILE_NAME = "file_name";
	public static final String CONTENTDATA = "contentdata";
	public static final String MD5 = "md5";
	public static final String PARENT_ID = "parent_id";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";

	public java.lang.String getPk_filesystem() {
		return this.pk_filesystem;
	}

	public void setPk_filesystem(java.lang.String value) {
		this.pk_filesystem = value;
	}

	public java.lang.String getFile_name() {
		return file_name;
	}

	public void setFile_name(java.lang.String file_name) {
		this.file_name = file_name;
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

	public org.nw.vo.pub.lang.UFBoolean getFolder_flag() {
		return this.folder_flag;
	}

	public void setFolder_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.folder_flag = value;
	}

	public java.lang.Long getFile_size() {
		return file_size;
	}

	public void setFile_size(java.lang.Long file_size) {
		this.file_size = file_size;
	}

	public byte[] getContentdata() {
		return this.contentdata;
	}

	public void setContentdata(byte[] value) {
		this.contentdata = value;
	}

	public java.lang.String getMd5() {
		return this.md5;
	}

	public void setMd5(java.lang.String value) {
		this.md5 = value;
	}

	public java.lang.String getParent_id() {
		return this.parent_id;
	}

	public void setParent_id(java.lang.String value) {
		this.parent_id = value;
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
		return "pk_filesystem";
	}

	public String getTableName() {
		return "nw_filesystem";
	}
}
