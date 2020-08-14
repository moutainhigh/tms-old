package com.tms.vo.edi;

/**
 * edi_his_invoice
 * 
 * @version 1.0
 * @since 1.0
 */
public class EdiHisInvoiceVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_invoice;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String vbillno;

	private java.lang.String cust_orderno;

	private java.lang.String orderno;

	private java.lang.Integer vbillstatus;

	private java.lang.String pk_customer;

	private java.lang.String bala_customer;

	private java.lang.String pk_trans_type;

	private java.lang.Integer balatype;

	private java.lang.Integer urgent_level;

	private java.lang.String feature;

	private String req_deli_date;

	private java.lang.String req_deli_time;

	private String req_arri_date;

	private java.lang.String req_arri_time;

	private org.nw.vo.pub.lang.UFDateTime order_time;

	private java.lang.String pk_psndoc;

	private java.lang.String pk_dept;

	private java.lang.String contact;

	private java.lang.Integer mileage;

	private java.lang.String pk_invoice_type;

	private java.lang.String pk_service_type;

	private java.lang.String trade_term;

	private java.lang.Integer invoice_origin;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String pk_corp;

	private java.lang.String memo;

	private java.lang.Integer sign_status;

	private java.lang.String sign_man;

	private org.nw.vo.pub.lang.UFDateTime sign_time;

	private java.lang.String sign_memo;

	private java.lang.String pk_delivery;

	private java.lang.String deli_code;

	private java.lang.String deli_city;

	private java.lang.String deli_province;

	private java.lang.String deli_area;

	private java.lang.String deli_detail_addr;

	private java.lang.String deli_contact;

	private java.lang.String deli_mobile;

	private java.lang.String deli_phone;

	private java.lang.String deli_email;

	private java.lang.String arri_code;

	private java.lang.String pk_arrival;

	private java.lang.String arri_city;

	private java.lang.String arri_province;

	private java.lang.String arri_area;

	private java.lang.String arri_detail_addr;

	private java.lang.String arri_contact;

	private java.lang.String arri_mobile;

	private java.lang.String arri_phone;

	private java.lang.String arri_email;

	private java.lang.Integer deli_method;

	private org.nw.vo.pub.lang.UFDouble distance;

	private java.lang.Integer backbill_num;

	private org.nw.vo.pub.lang.UFBoolean if_backbill;

	private org.nw.vo.pub.lang.UFBoolean if_ins_receipt;

	private org.nw.vo.pub.lang.UFDouble receipt_amount;

	private org.nw.vo.pub.lang.UFBoolean if_insurance;

	private org.nw.vo.pub.lang.UFDouble insurance_amount;

	private org.nw.vo.pub.lang.UFDouble amount;

	private java.lang.String insurance_no;

	private org.nw.vo.pub.lang.UFBoolean if_billing;

	private org.nw.vo.pub.lang.UFBoolean if_return;

	private java.lang.String returnbill_no;

	private org.nw.vo.pub.lang.UFBoolean if_customs_official;

	private java.lang.String customs_official_no;

	private java.lang.String cost_center;

	private java.lang.String deli_process;

	private java.lang.String arri_process;

	private java.lang.String note;

	private org.nw.vo.pub.lang.UFDouble pack_num_count;

	private java.lang.Integer num_count;

	private org.nw.vo.pub.lang.UFDouble weight_count;

	private org.nw.vo.pub.lang.UFDouble volume_count;

	private org.nw.vo.pub.lang.UFDouble fee_weight_count;

	private org.nw.vo.pub.lang.UFDouble volume_weight_count;

	private org.nw.vo.pub.lang.UFDouble cost_amount;

	private org.nw.vo.pub.lang.UFDouble arri_pay_amount;

	private java.lang.String pk_carrier;

	private java.lang.Integer tracking_status;

	private org.nw.vo.pub.lang.UFDateTime tracking_time;

	private java.lang.String tracking_memo;

	private org.nw.vo.pub.lang.UFBoolean exp_flag;

	private java.lang.String exp_type;

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

	private org.nw.vo.pub.lang.UFDate dbilldate;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private org.nw.vo.pub.lang.UFDateTime act_deli_date;

	private org.nw.vo.pub.lang.UFDateTime act_arri_date;

	private org.nw.vo.pub.lang.UFDateTime con_arri_date;

	private java.lang.String item_name;

	private java.lang.String item_code;

	private org.nw.vo.pub.lang.UFDateTime sync_time;

	private org.nw.vo.pub.lang.UFBoolean syncexp_flag;

	private java.lang.String syncexp_memo;

	public static final String PK_INVOICE = "pk_invoice";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String VBILLNO = "vbillno";
	public static final String CUST_ORDERNO = "cust_orderno";
	public static final String ORDERNO = "orderno";
	public static final String VBILLSTATUS = "vbillstatus";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String BALA_CUSTOMER = "bala_customer";
	public static final String PK_TRANS_TYPE = "pk_trans_type";
	public static final String BALATYPE = "balatype";
	public static final String URGENT_LEVEL = "urgent_level";
	public static final String FEATURE = "feature";
	public static final String REQ_DELI_DATE = "req_deli_date";
	public static final String REQ_DELI_TIME = "req_deli_time";
	public static final String REQ_ARRI_DATE = "req_arri_date";
	public static final String REQ_ARRI_TIME = "req_arri_time";
	public static final String ORDER_TIME = "order_time";
	public static final String PK_PSNDOC = "pk_psndoc";
	public static final String PK_DEPT = "pk_dept";
	public static final String CONTACT = "contact";
	public static final String MILEAGE = "mileage";
	public static final String PK_INVOICE_TYPE = "pk_invoice_type";
	public static final String PK_SERVICE_TYPE = "pk_service_type";
	public static final String TRADE_TERM = "trade_term";
	public static final String INVOICE_ORIGIN = "invoice_origin";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String PK_CORP = "pk_corp";
	public static final String MEMO = "memo";
	public static final String SIGN_STATUS = "sign_status";
	public static final String SIGN_MAN = "sign_man";
	public static final String SIGN_TIME = "sign_time";
	public static final String SIGN_MEMO = "sign_memo";
	public static final String PK_DELIVERY = "pk_delivery";
	public static final String DELI_CODE = "deli_code";
	public static final String DELI_CITY = "deli_city";
	public static final String DELI_PROVINCE = "deli_province";
	public static final String DELI_AREA = "deli_area";
	public static final String DELI_DETAIL_ADDR = "deli_detail_addr";
	public static final String DELI_CONTACT = "deli_contact";
	public static final String DELI_MOBILE = "deli_mobile";
	public static final String DELI_PHONE = "deli_phone";
	public static final String DELI_EMAIL = "deli_email";
	public static final String ARRI_CODE = "arri_code";
	public static final String PK_ARRIVAL = "pk_arrival";
	public static final String ARRI_CITY = "arri_city";
	public static final String ARRI_PROVINCE = "arri_province";
	public static final String ARRI_AREA = "arri_area";
	public static final String ARRI_DETAIL_ADDR = "arri_detail_addr";
	public static final String ARRI_CONTACT = "arri_contact";
	public static final String ARRI_MOBILE = "arri_mobile";
	public static final String ARRI_PHONE = "arri_phone";
	public static final String ARRI_EMAIL = "arri_email";
	public static final String DELI_METHOD = "deli_method";
	public static final String DISTANCE = "distance";
	public static final String BACKBILL_NUM = "backbill_num";
	public static final String IF_BACKBILL = "if_backbill";
	public static final String IF_INS_RECEIPT = "if_ins_receipt";
	public static final String RECEIPT_AMOUNT = "receipt_amount";
	public static final String IF_INSURANCE = "if_insurance";
	public static final String INSURANCE_AMOUNT = "insurance_amount";
	public static final String AMOUNT = "amount";
	public static final String INSURANCE_NO = "insurance_no";
	public static final String IF_BILLING = "if_billing";
	public static final String IF_RETURN = "if_return";
	public static final String RETURNBILL_NO = "returnbill_no";
	public static final String IF_CUSTOMS_OFFICIAL = "if_customs_official";
	public static final String CUSTOMS_OFFICIAL_NO = "customs_official_no";
	public static final String COST_CENTER = "cost_center";
	public static final String DELI_PROCESS = "deli_process";
	public static final String ARRI_PROCESS = "arri_process";
	public static final String NOTE = "note";
	public static final String PACK_NUM_COUNT = "pack_num_count";
	public static final String NUM_COUNT = "num_count";
	public static final String WEIGHT_COUNT = "weight_count";
	public static final String VOLUME_COUNT = "volume_count";
	public static final String FEE_WEIGHT_COUNT = "fee_weight_count";
	public static final String VOLUME_WEIGHT_COUNT = "volume_weight_count";
	public static final String COST_AMOUNT = "cost_amount";
	public static final String ARRI_PAY_AMOUNT = "arri_pay_amount";
	public static final String PK_CARRIER = "pk_carrier";
	public static final String TRACKING_STATUS = "tracking_status";
	public static final String TRACKING_TIME = "tracking_time";
	public static final String TRACKING_MEMO = "tracking_memo";
	public static final String EXP_FLAG = "exp_flag";
	public static final String EXP_TYPE = "exp_type";
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
	public static final String DBILLDATE = "dbilldate";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String ACT_DELI_DATE = "act_deli_date";
	public static final String ACT_ARRI_DATE = "act_arri_date";
	public static final String CON_ARRI_DATE = "con_arri_date";
	public static final String ITEM_NAME = "item_name";
	public static final String ITEM_CODE = "item_code";
	public static final String SYNC_TIME = "sync_time";
	public static final String SYNCEXP_FLAG = "syncexp_flag";
	public static final String SYNCEXP_MEMO = "syncexp_memo";

	public java.lang.String getPk_invoice() {
		return this.pk_invoice;
	}

	public void setPk_invoice(java.lang.String value) {
		this.pk_invoice = value;
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

	public java.lang.String getVbillno() {
		return this.vbillno;
	}

	public void setVbillno(java.lang.String value) {
		this.vbillno = value;
	}

	public java.lang.String getCust_orderno() {
		return this.cust_orderno;
	}

	public void setCust_orderno(java.lang.String value) {
		this.cust_orderno = value;
	}

	public java.lang.String getOrderno() {
		return this.orderno;
	}

	public void setOrderno(java.lang.String value) {
		this.orderno = value;
	}

	public java.lang.Integer getVbillstatus() {
		return this.vbillstatus;
	}

	public void setVbillstatus(java.lang.Integer value) {
		this.vbillstatus = value;
	}

	public java.lang.String getPk_customer() {
		return this.pk_customer;
	}

	public void setPk_customer(java.lang.String value) {
		this.pk_customer = value;
	}

	public java.lang.String getBala_customer() {
		return this.bala_customer;
	}

	public void setBala_customer(java.lang.String value) {
		this.bala_customer = value;
	}

	public java.lang.String getPk_trans_type() {
		return this.pk_trans_type;
	}

	public void setPk_trans_type(java.lang.String value) {
		this.pk_trans_type = value;
	}

	public java.lang.Integer getBalatype() {
		return this.balatype;
	}

	public void setBalatype(java.lang.Integer value) {
		this.balatype = value;
	}

	public java.lang.Integer getUrgent_level() {
		return this.urgent_level;
	}

	public void setUrgent_level(java.lang.Integer value) {
		this.urgent_level = value;
	}

	public java.lang.String getFeature() {
		return this.feature;
	}

	public void setFeature(java.lang.String value) {
		this.feature = value;
	}

	public String getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(String req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public String getReq_arri_date() {
		return req_arri_date;
	}

	public void setReq_arri_date(String req_arri_date) {
		this.req_arri_date = req_arri_date;
	}

	public java.lang.String getReq_deli_time() {
		return this.req_deli_time;
	}

	public void setReq_deli_time(java.lang.String value) {
		this.req_deli_time = value;
	}

	public java.lang.String getReq_arri_time() {
		return this.req_arri_time;
	}

	public void setReq_arri_time(java.lang.String value) {
		this.req_arri_time = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getOrder_time() {
		return this.order_time;
	}

	public void setOrder_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.order_time = value;
	}

	public java.lang.String getPk_psndoc() {
		return this.pk_psndoc;
	}

	public void setPk_psndoc(java.lang.String value) {
		this.pk_psndoc = value;
	}

	public java.lang.String getPk_dept() {
		return this.pk_dept;
	}

	public void setPk_dept(java.lang.String value) {
		this.pk_dept = value;
	}

	public java.lang.String getContact() {
		return this.contact;
	}

	public void setContact(java.lang.String value) {
		this.contact = value;
	}

	public java.lang.Integer getMileage() {
		return this.mileage;
	}

	public void setMileage(java.lang.Integer value) {
		this.mileage = value;
	}

	public java.lang.String getPk_invoice_type() {
		return this.pk_invoice_type;
	}

	public void setPk_invoice_type(java.lang.String value) {
		this.pk_invoice_type = value;
	}

	public java.lang.String getPk_service_type() {
		return this.pk_service_type;
	}

	public void setPk_service_type(java.lang.String value) {
		this.pk_service_type = value;
	}

	public java.lang.String getTrade_term() {
		return this.trade_term;
	}

	public void setTrade_term(java.lang.String value) {
		this.trade_term = value;
	}

	public java.lang.Integer getInvoice_origin() {
		return this.invoice_origin;
	}

	public void setInvoice_origin(java.lang.Integer value) {
		this.invoice_origin = value;
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

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
	}

	public java.lang.Integer getSign_status() {
		return this.sign_status;
	}

	public void setSign_status(java.lang.Integer value) {
		this.sign_status = value;
	}

	public java.lang.String getSign_man() {
		return this.sign_man;
	}

	public void setSign_man(java.lang.String value) {
		this.sign_man = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getSign_time() {
		return this.sign_time;
	}

	public void setSign_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.sign_time = value;
	}

	public java.lang.String getSign_memo() {
		return this.sign_memo;
	}

	public void setSign_memo(java.lang.String value) {
		this.sign_memo = value;
	}

	public java.lang.String getPk_delivery() {
		return this.pk_delivery;
	}

	public void setPk_delivery(java.lang.String value) {
		this.pk_delivery = value;
	}

	public java.lang.String getDeli_code() {
		return this.deli_code;
	}

	public void setDeli_code(java.lang.String value) {
		this.deli_code = value;
	}

	public java.lang.String getDeli_city() {
		return this.deli_city;
	}

	public void setDeli_city(java.lang.String value) {
		this.deli_city = value;
	}

	public java.lang.String getDeli_province() {
		return this.deli_province;
	}

	public void setDeli_province(java.lang.String value) {
		this.deli_province = value;
	}

	public java.lang.String getDeli_area() {
		return this.deli_area;
	}

	public void setDeli_area(java.lang.String value) {
		this.deli_area = value;
	}

	public java.lang.String getDeli_detail_addr() {
		return this.deli_detail_addr;
	}

	public void setDeli_detail_addr(java.lang.String value) {
		this.deli_detail_addr = value;
	}

	public java.lang.String getDeli_contact() {
		return this.deli_contact;
	}

	public void setDeli_contact(java.lang.String value) {
		this.deli_contact = value;
	}

	public java.lang.String getDeli_mobile() {
		return this.deli_mobile;
	}

	public void setDeli_mobile(java.lang.String value) {
		this.deli_mobile = value;
	}

	public java.lang.String getDeli_phone() {
		return this.deli_phone;
	}

	public void setDeli_phone(java.lang.String value) {
		this.deli_phone = value;
	}

	public java.lang.String getDeli_email() {
		return this.deli_email;
	}

	public void setDeli_email(java.lang.String value) {
		this.deli_email = value;
	}

	public java.lang.String getArri_code() {
		return this.arri_code;
	}

	public void setArri_code(java.lang.String value) {
		this.arri_code = value;
	}

	public java.lang.String getPk_arrival() {
		return this.pk_arrival;
	}

	public void setPk_arrival(java.lang.String value) {
		this.pk_arrival = value;
	}

	public java.lang.String getArri_city() {
		return this.arri_city;
	}

	public void setArri_city(java.lang.String value) {
		this.arri_city = value;
	}

	public java.lang.String getArri_province() {
		return this.arri_province;
	}

	public void setArri_province(java.lang.String value) {
		this.arri_province = value;
	}

	public java.lang.String getArri_area() {
		return this.arri_area;
	}

	public void setArri_area(java.lang.String value) {
		this.arri_area = value;
	}

	public java.lang.String getArri_detail_addr() {
		return this.arri_detail_addr;
	}

	public void setArri_detail_addr(java.lang.String value) {
		this.arri_detail_addr = value;
	}

	public java.lang.String getArri_contact() {
		return this.arri_contact;
	}

	public void setArri_contact(java.lang.String value) {
		this.arri_contact = value;
	}

	public java.lang.String getArri_mobile() {
		return this.arri_mobile;
	}

	public void setArri_mobile(java.lang.String value) {
		this.arri_mobile = value;
	}

	public java.lang.String getArri_phone() {
		return this.arri_phone;
	}

	public void setArri_phone(java.lang.String value) {
		this.arri_phone = value;
	}

	public java.lang.String getArri_email() {
		return this.arri_email;
	}

	public void setArri_email(java.lang.String value) {
		this.arri_email = value;
	}

	public java.lang.Integer getDeli_method() {
		return this.deli_method;
	}

	public void setDeli_method(java.lang.Integer value) {
		this.deli_method = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDistance() {
		return this.distance;
	}

	public void setDistance(org.nw.vo.pub.lang.UFDouble value) {
		this.distance = value;
	}

	public java.lang.Integer getBackbill_num() {
		return this.backbill_num;
	}

	public void setBackbill_num(java.lang.Integer value) {
		this.backbill_num = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_backbill() {
		return this.if_backbill;
	}

	public void setIf_backbill(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_backbill = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_ins_receipt() {
		return this.if_ins_receipt;
	}

	public void setIf_ins_receipt(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_ins_receipt = value;
	}

	public org.nw.vo.pub.lang.UFDouble getReceipt_amount() {
		return this.receipt_amount;
	}

	public void setReceipt_amount(org.nw.vo.pub.lang.UFDouble value) {
		this.receipt_amount = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_insurance() {
		return this.if_insurance;
	}

	public void setIf_insurance(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_insurance = value;
	}

	public org.nw.vo.pub.lang.UFDouble getInsurance_amount() {
		return this.insurance_amount;
	}

	public void setInsurance_amount(org.nw.vo.pub.lang.UFDouble value) {
		this.insurance_amount = value;
	}

	public org.nw.vo.pub.lang.UFDouble getAmount() {
		return this.amount;
	}

	public void setAmount(org.nw.vo.pub.lang.UFDouble value) {
		this.amount = value;
	}

	public java.lang.String getInsurance_no() {
		return this.insurance_no;
	}

	public void setInsurance_no(java.lang.String value) {
		this.insurance_no = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_billing() {
		return this.if_billing;
	}

	public void setIf_billing(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_billing = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_return() {
		return this.if_return;
	}

	public void setIf_return(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_return = value;
	}

	public java.lang.String getReturnbill_no() {
		return this.returnbill_no;
	}

	public void setReturnbill_no(java.lang.String value) {
		this.returnbill_no = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIf_customs_official() {
		return this.if_customs_official;
	}

	public void setIf_customs_official(org.nw.vo.pub.lang.UFBoolean value) {
		this.if_customs_official = value;
	}

	public java.lang.String getCustoms_official_no() {
		return this.customs_official_no;
	}

	public void setCustoms_official_no(java.lang.String value) {
		this.customs_official_no = value;
	}

	public java.lang.String getCost_center() {
		return this.cost_center;
	}

	public void setCost_center(java.lang.String value) {
		this.cost_center = value;
	}

	public java.lang.String getDeli_process() {
		return this.deli_process;
	}

	public void setDeli_process(java.lang.String value) {
		this.deli_process = value;
	}

	public java.lang.String getArri_process() {
		return this.arri_process;
	}

	public void setArri_process(java.lang.String value) {
		this.arri_process = value;
	}

	public java.lang.String getNote() {
		return this.note;
	}

	public void setNote(java.lang.String value) {
		this.note = value;
	}

	public org.nw.vo.pub.lang.UFDouble getPack_num_count() {
		return this.pack_num_count;
	}

	public void setPack_num_count(org.nw.vo.pub.lang.UFDouble value) {
		this.pack_num_count = value;
	}

	public java.lang.Integer getNum_count() {
		return this.num_count;
	}

	public void setNum_count(java.lang.Integer value) {
		this.num_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getWeight_count() {
		return this.weight_count;
	}

	public void setWeight_count(org.nw.vo.pub.lang.UFDouble value) {
		this.weight_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume_count() {
		return this.volume_count;
	}

	public void setVolume_count(org.nw.vo.pub.lang.UFDouble value) {
		this.volume_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getFee_weight_count() {
		return this.fee_weight_count;
	}

	public void setFee_weight_count(org.nw.vo.pub.lang.UFDouble value) {
		this.fee_weight_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume_weight_count() {
		return this.volume_weight_count;
	}

	public void setVolume_weight_count(org.nw.vo.pub.lang.UFDouble value) {
		this.volume_weight_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getCost_amount() {
		return this.cost_amount;
	}

	public void setCost_amount(org.nw.vo.pub.lang.UFDouble value) {
		this.cost_amount = value;
	}

	public org.nw.vo.pub.lang.UFDouble getArri_pay_amount() {
		return this.arri_pay_amount;
	}

	public void setArri_pay_amount(org.nw.vo.pub.lang.UFDouble value) {
		this.arri_pay_amount = value;
	}

	public java.lang.String getPk_carrier() {
		return this.pk_carrier;
	}

	public void setPk_carrier(java.lang.String value) {
		this.pk_carrier = value;
	}

	public java.lang.Integer getTracking_status() {
		return this.tracking_status;
	}

	public void setTracking_status(java.lang.Integer value) {
		this.tracking_status = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getTracking_time() {
		return this.tracking_time;
	}

	public void setTracking_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.tracking_time = value;
	}

	public java.lang.String getTracking_memo() {
		return this.tracking_memo;
	}

	public void setTracking_memo(java.lang.String value) {
		this.tracking_memo = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getExp_flag() {
		return this.exp_flag;
	}

	public void setExp_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.exp_flag = value;
	}

	public java.lang.String getExp_type() {
		return this.exp_type;
	}

	public void setExp_type(java.lang.String value) {
		this.exp_type = value;
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

	public org.nw.vo.pub.lang.UFDate getDbilldate() {
		return this.dbilldate;
	}

	public void setDbilldate(org.nw.vo.pub.lang.UFDate value) {
		this.dbilldate = value;
	}

	public java.lang.String getModify_user() {
		return this.modify_user;
	}

	public void setModify_user(java.lang.String value) {
		this.modify_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getModify_time() {
		return this.modify_time;
	}

	public void setModify_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.modify_time = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getAct_deli_date() {
		return this.act_deli_date;
	}

	public void setAct_deli_date(org.nw.vo.pub.lang.UFDateTime value) {
		this.act_deli_date = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getAct_arri_date() {
		return this.act_arri_date;
	}

	public void setAct_arri_date(org.nw.vo.pub.lang.UFDateTime value) {
		this.act_arri_date = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getCon_arri_date() {
		return this.con_arri_date;
	}

	public void setCon_arri_date(org.nw.vo.pub.lang.UFDateTime value) {
		this.con_arri_date = value;
	}

	public java.lang.String getItem_name() {
		return this.item_name;
	}

	public void setItem_name(java.lang.String value) {
		this.item_name = value;
	}

	public java.lang.String getItem_code() {
		return this.item_code;
	}

	public void setItem_code(java.lang.String value) {
		this.item_code = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getSync_time() {
		return this.sync_time;
	}

	public void setSync_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.sync_time = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getSyncexp_flag() {
		return this.syncexp_flag;
	}

	public void setSyncexp_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.syncexp_flag = value;
	}

	public java.lang.String getSyncexp_memo() {
		return this.syncexp_memo;
	}

	public void setSyncexp_memo(java.lang.String value) {
		this.syncexp_memo = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_invoice";
	}

	public String getTableName() {
		return "edi_his_invoice";
	}
}
