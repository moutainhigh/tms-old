package com.tms.vo.base;

/**
 * ts_supp_addr
 * 
 * @version 1.0
 * @since 1.0
 */
public class SuppAddrVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_supp_addr;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_supplier;

	private java.lang.String pk_address;

	private java.lang.String memo;

	private org.nw.vo.pub.lang.UFBoolean if_default;

	private org.nw.vo.pub.lang.UFBoolean locked_flag;

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

	private org.nw.vo.pub.lang.UFDouble def11;

	private org.nw.vo.pub.lang.UFDouble def12;

	// 扩展的字段，用户可以在客户管理页面中直接维护地址档案
	private String addrcode;
	private String addrname;
	private Integer addr_type;
	private String detail_addr;
	private String contact;
	private String contact_post;
	private String phone;
	private String mobile;
	private String email;
	private String fax;
	private String pk_city;
	private String pk_province;
	private String pk_area;

	public static final String PK_SUPP_ADDR = "pk_supp_addr";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_SUPPLIER = "pk_supplier";
	public static final String PK_ADDRESS = "pk_address";
	public static final String MEMO = "memo";
	public static final String IF_DEFAULT = "if_default";
	public static final String LOCKED_FLAG = "locked_flag";
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
	public static final String DEF11 = "def11";
	public static final String DEF12 = "def12";

	public String getAddrcode() {
		return addrcode;
	}

	public void setAddrcode(String addrcode) {
		this.addrcode = addrcode;
	}

	public String getAddrname() {
		return addrname;
	}

	public void setAddrname(String addrname) {
		this.addrname = addrname;
	}

	public Integer getAddr_type() {
		return addr_type;
	}

	public void setAddr_type(Integer addr_type) {
		this.addr_type = addr_type;
	}

	public String getDetail_addr() {
		return detail_addr;
	}

	public void setDetail_addr(String detail_addr) {
		this.detail_addr = detail_addr;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getContact_post() {
		return contact_post;
	}

	public void setContact_post(String contact_post) {
		this.contact_post = contact_post;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getPk_city() {
		return pk_city;
	}

	public void setPk_city(String pk_city) {
		this.pk_city = pk_city;
	}

	public String getPk_province() {
		return pk_province;
	}

	public void setPk_province(String pk_province) {
		this.pk_province = pk_province;
	}

	public String getPk_area() {
		return pk_area;
	}

	public void setPk_area(String pk_area) {
		this.pk_area = pk_area;
	}

	public java.lang.String getPk_supp_addr() {
		return this.pk_supp_addr;
	}

	public void setPk_supp_addr(java.lang.String value) {
		this.pk_supp_addr = value;
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

	public java.lang.String getPk_supplier() {
		return this.pk_supplier;
	}

	public void setPk_supplier(java.lang.String value) {
		this.pk_supplier = value;
	}

	public java.lang.String getPk_address() {
		return this.pk_address;
	}

	public void setPk_address(java.lang.String value) {
		this.pk_address = value;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_default() {
		return this.if_default;
	}

	public void setIf_default(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_default = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getLocked_flag() {
		return this.locked_flag;
	}

	public void setLocked_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.locked_flag = value;
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

	public java.lang.String getDef6() {
		return this.def6;
	}

	public void setDef6(java.lang.String value) {
		this.def6 = value;
	}

	public java.lang.String getDef7() {
		return this.def7;
	}

	public void setDef7(java.lang.String value) {
		this.def7 = value;
	}

	public java.lang.String getDef8() {
		return this.def8;
	}

	public void setDef8(java.lang.String value) {
		this.def8 = value;
	}

	public java.lang.String getDef9() {
		return this.def9;
	}

	public void setDef9(java.lang.String value) {
		this.def9 = value;
	}

	public java.lang.String getDef10() {
		return this.def10;
	}

	public void setDef10(java.lang.String value) {
		this.def10 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef11() {
		return this.def11;
	}

	public void setDef11(org.nw.vo.pub.lang.UFDouble value) {
		this.def11 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef12() {
		return this.def12;
	}

	public void setDef12(org.nw.vo.pub.lang.UFDouble value) {
		this.def12 = value;
	}

	public String getParentPKFieldName() {
		return PK_SUPPLIER;
	}

	public String getPKFieldName() {
		return PK_SUPP_ADDR;
	}

	public String getTableName() {
		return "ts_supp_addr";
	}
}
