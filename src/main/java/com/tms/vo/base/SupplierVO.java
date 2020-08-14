package com.tms.vo.base;

import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_supplier
 * 
 * @version 1.0
 * @since 1.0
 */
public class SupplierVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_supplier;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String supp_code;

	private java.lang.String supp_name;

	private java.lang.Integer supp_type;

	private java.lang.String pk_dept;

	private java.lang.String pk_psndoc;

	private java.lang.String psncontact;

	private java.lang.String pk_province;

	private java.lang.String pk_city;

	private java.lang.String pk_area;

	private java.lang.String contact;

	private java.lang.String contact_post;

	private java.lang.String phone;

	private java.lang.String mobile;

	private java.lang.String email;

	private java.lang.String fax;

	private java.lang.String address;

	private java.lang.String zipcode;

	private java.lang.Integer balatype;

	private org.nw.vo.pub.lang.UFDouble discount_rate;

	private java.lang.Integer account_period;

	private java.lang.Integer acc_period_ahead;

	private org.nw.vo.pub.lang.UFDate billing_date;

	private java.lang.Integer billing_ahead;

	private org.nw.vo.pub.lang.UFDouble credit_amount;

	private java.lang.String tax_identify;

	private java.lang.String pk_billing_type;

	private java.lang.String billing_payable;

	private java.lang.String bank;

	private java.lang.String account_name;

	private java.lang.String bank_account;

	private java.lang.String register_addr;

	private java.lang.String legal_represent;

	private org.nw.vo.pub.lang.UFDouble register_capital;

	private java.lang.String website;

	private java.lang.String pk_corp;

	private org.nw.vo.pub.lang.UFBoolean locked_flag;

	private java.lang.String memo;
	private String def10;
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private UFDouble def11;
	private UFDouble def12;
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

	public UFDouble getDef11() {
		return def11;
	}

	public void setDef11(UFDouble def11) {
		this.def11 = def11;
	}

	public UFDouble getDef12() {
		return def12;
	}

	public void setDef12(UFDouble def12) {
		this.def12 = def12;
	}

	public String getDef10() {
		return def10;
	}

	public void setDef10(String def10) {
		this.def10 = def10;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	public String getDef4() {
		return def4;
	}

	public void setDef4(String def4) {
		this.def4 = def4;
	}

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public String getDef9() {
		return def9;
	}

	public void setDef9(String def9) {
		this.def9 = def9;
	}

	public String getDef5() {
		return def5;
	}

	public void setDef5(String def5) {
		this.def5 = def5;
	}

	public String getDef6() {
		return def6;
	}

	public void setDef6(String def6) {
		this.def6 = def6;
	}

	public String getDef7() {
		return def7;
	}

	public void setDef7(String def7) {
		this.def7 = def7;
	}

	public String getDef8() {
		return def8;
	}

	public void setDef8(String def8) {
		this.def8 = def8;
	}

	public static final String PK_SUPPLIER = "pk_supplier";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String SUPP_CODE = "supp_code";
	public static final String SUPP_NAME = "supp_name";
	public static final String SUPP_TYPE = "supp_type";
	public static final String PK_DEPT = "pk_dept";
	public static final String PK_PSNDOC = "pk_psndoc";
	public static final String PSNCONTACT = "psncontact";
	public static final String PK_PROVINCE = "pk_province";
	public static final String PK_CITY = "pk_city";
	public static final String PK_AREA = "pk_area";
	public static final String CONTACT = "contact";
	public static final String CONTACT_POST = "contact_post";
	public static final String PHONE = "phone";
	public static final String MOBILE = "mobile";
	public static final String EMAIL = "email";
	public static final String FAX = "fax";
	public static final String ADDRESS = "address";
	public static final String ZIPCODE = "zipcode";
	public static final String BALATYPE = "balatype";
	public static final String DISCOUNT_RATE = "discount_rate";
	public static final String ACCOUNT_PERIOD = "account_period";
	public static final String ACC_PERIOD_AHEAD = "acc_period_ahead";
	public static final String BILLING_DATE = "billing_date";
	public static final String BILLING_AHEAD = "billing_ahead";
	public static final String CREDIT_AMOUNT = "credit_amount";
	public static final String TAX_IDENTIFY = "tax_identify";
	public static final String PK_BILLING_TYPE = "pk_billing_type";
	public static final String BILLING_PAYABLE = "billing_payable";
	public static final String BANK = "bank";
	public static final String ACCOUNT_NAME = "account_name";
	public static final String BANK_ACCOUNT = "bank_account";
	public static final String REGISTER_ADDR = "register_addr";
	public static final String LEGAL_REPRESENT = "legal_represent";
	public static final String REGISTER_CAPITAL = "register_capital";
	public static final String WEBSITE = "website";
	public static final String PK_CORP = "pk_corp";
	public static final String LOCKED_FLAG = "locked_flag";
	public static final String MEMO = "memo";

	public java.lang.String getPk_supplier() {
		return this.pk_supplier;
	}

	public void setPk_supplier(java.lang.String value) {
		this.pk_supplier = value;
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

	public java.lang.String getSupp_code() {
		return this.supp_code;
	}

	public void setSupp_code(java.lang.String value) {
		this.supp_code = value;
	}

	public java.lang.String getSupp_name() {
		return this.supp_name;
	}

	public void setSupp_name(java.lang.String value) {
		this.supp_name = value;
	}

	public java.lang.Integer getSupp_type() {
		return this.supp_type;
	}

	public void setSupp_type(java.lang.Integer value) {
		this.supp_type = value;
	}

	public java.lang.String getPk_dept() {
		return this.pk_dept;
	}

	public void setPk_dept(java.lang.String value) {
		this.pk_dept = value;
	}

	public java.lang.String getPk_psndoc() {
		return this.pk_psndoc;
	}

	public void setPk_psndoc(java.lang.String value) {
		this.pk_psndoc = value;
	}

	public java.lang.String getPsncontact() {
		return this.psncontact;
	}

	public void setPsncontact(java.lang.String value) {
		this.psncontact = value;
	}

	public java.lang.String getPk_province() {
		return this.pk_province;
	}

	public void setPk_province(java.lang.String value) {
		this.pk_province = value;
	}

	public java.lang.String getPk_city() {
		return this.pk_city;
	}

	public void setPk_city(java.lang.String value) {
		this.pk_city = value;
	}

	public java.lang.String getPk_area() {
		return this.pk_area;
	}

	public void setPk_area(java.lang.String value) {
		this.pk_area = value;
	}

	public java.lang.String getContact() {
		return this.contact;
	}

	public void setContact(java.lang.String value) {
		this.contact = value;
	}

	public java.lang.String getContact_post() {
		return this.contact_post;
	}

	public void setContact_post(java.lang.String value) {
		this.contact_post = value;
	}

	public java.lang.String getPhone() {
		return this.phone;
	}

	public void setPhone(java.lang.String value) {
		this.phone = value;
	}

	public java.lang.String getMobile() {
		return this.mobile;
	}

	public void setMobile(java.lang.String value) {
		this.mobile = value;
	}

	public java.lang.String getEmail() {
		return this.email;
	}

	public void setEmail(java.lang.String value) {
		this.email = value;
	}

	public java.lang.String getFax() {
		return this.fax;
	}

	public void setFax(java.lang.String value) {
		this.fax = value;
	}

	public java.lang.String getAddress() {
		return this.address;
	}

	public void setAddress(java.lang.String value) {
		this.address = value;
	}

	public java.lang.String getZipcode() {
		return this.zipcode;
	}

	public void setZipcode(java.lang.String value) {
		this.zipcode = value;
	}

	public java.lang.Integer getBalatype() {
		return this.balatype;
	}

	public void setBalatype(java.lang.Integer value) {
		this.balatype = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDiscount_rate() {
		return this.discount_rate;
	}

	public void setDiscount_rate(org.nw.vo.pub.lang.UFDouble value) {
		this.discount_rate = value;
	}

	public java.lang.Integer getAccount_period() {
		return this.account_period;
	}

	public void setAccount_period(java.lang.Integer value) {
		this.account_period = value;
	}

	public java.lang.Integer getAcc_period_ahead() {
		return this.acc_period_ahead;
	}

	public void setAcc_period_ahead(java.lang.Integer value) {
		this.acc_period_ahead = value;
	}

	public org.nw.vo.pub.lang.UFDate getBilling_date() {
		return this.billing_date;
	}

	public void setBilling_date(org.nw.vo.pub.lang.UFDate value) {
		this.billing_date = value;
	}

	public java.lang.Integer getBilling_ahead() {
		return this.billing_ahead;
	}

	public void setBilling_ahead(java.lang.Integer value) {
		this.billing_ahead = value;
	}

	public org.nw.vo.pub.lang.UFDouble getCredit_amount() {
		return this.credit_amount;
	}

	public void setCredit_amount(org.nw.vo.pub.lang.UFDouble value) {
		this.credit_amount = value;
	}

	public java.lang.String getTax_identify() {
		return this.tax_identify;
	}

	public void setTax_identify(java.lang.String value) {
		this.tax_identify = value;
	}

	public java.lang.String getPk_billing_type() {
		return this.pk_billing_type;
	}

	public void setPk_billing_type(java.lang.String value) {
		this.pk_billing_type = value;
	}

	public java.lang.String getBilling_payable() {
		return this.billing_payable;
	}

	public void setBilling_payable(java.lang.String value) {
		this.billing_payable = value;
	}

	public java.lang.String getBank() {
		return this.bank;
	}

	public void setBank(java.lang.String value) {
		this.bank = value;
	}

	public java.lang.String getAccount_name() {
		return this.account_name;
	}

	public void setAccount_name(java.lang.String value) {
		this.account_name = value;
	}

	public java.lang.String getBank_account() {
		return this.bank_account;
	}

	public void setBank_account(java.lang.String value) {
		this.bank_account = value;
	}

	public java.lang.String getRegister_addr() {
		return this.register_addr;
	}

	public void setRegister_addr(java.lang.String value) {
		this.register_addr = value;
	}

	public java.lang.String getLegal_represent() {
		return this.legal_represent;
	}

	public void setLegal_represent(java.lang.String value) {
		this.legal_represent = value;
	}

	public org.nw.vo.pub.lang.UFDouble getRegister_capital() {
		return this.register_capital;
	}

	public void setRegister_capital(org.nw.vo.pub.lang.UFDouble value) {
		this.register_capital = value;
	}

	public java.lang.String getWebsite() {
		return this.website;
	}

	public void setWebsite(java.lang.String value) {
		this.website = value;
	}

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getLocked_flag() {
		return this.locked_flag;
	}

	public void setLocked_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.locked_flag = value;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_supplier";
	}

	public String getTableName() {
		return "ts_supplier";
	}
}
