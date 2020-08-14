package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class AssignRuleBVO extends SuperVO {
	private String pk_assign_rule_b;
	private String pk_assign_rule;
	
	private Integer start_addr_type;
	private String start_addr;
	private Integer end_addr_type;
	private String end_addr;
	
	private String pk_trans_type;
	private String pk_customer;
	private Integer urgent_level;
	private String pk_psndoc;
	private String pk_dept;
	private String pk_invoice_type;
	private String pk_supplier;
	private String item_code;
	private UFBoolean if_return;
	private UFBoolean if_customs_official;
	private Integer invoice_goods_type;
	private Integer balatype;
	private String pk_car_type;
	private Integer deli_method;
	private Integer mileage;
	private String pk_carrier;

	private String req_carrier;
	private String req_carr_type;
	private String req_car;
	private String req_car_type;
	private String req_drvi;
	
	private Integer dr;
	private UFDateTime ts;
	
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
	
	private String memo;

	public static final String PK_ASSIGN_RULE = "pk_assign_rule";
	public static final String PK_ASSIGN_RULE_B = "pk_assign_rule_b";
	
	public String getPk_assign_rule_b() {
		return pk_assign_rule_b;
	}

	public void setPk_assign_rule_b(String pk_assign_rule_b) {
		this.pk_assign_rule_b = pk_assign_rule_b;
	}

	public String getPk_assign_rule() {
		return pk_assign_rule;
	}

	public void setPk_assign_rule(String pk_assign_rule) {
		this.pk_assign_rule = pk_assign_rule;
	}

	public Integer getStart_addr_type() {
		return start_addr_type;
	}

	public void setStart_addr_type(Integer start_addr_type) {
		this.start_addr_type = start_addr_type;
	}

	public String getStart_addr() {
		return start_addr;
	}

	public void setStart_addr(String start_addr) {
		this.start_addr = start_addr;
	}

	public Integer getEnd_addr_type() {
		return end_addr_type;
	}

	public void setEnd_addr_type(Integer end_addr_type) {
		this.end_addr_type = end_addr_type;
	}

	public String getEnd_addr() {
		return end_addr;
	}

	public void setEnd_addr(String end_addr) {
		this.end_addr = end_addr;
	}

	public String getPk_trans_type() {
		return pk_trans_type;
	}

	public void setPk_trans_type(String pk_trans_type) {
		this.pk_trans_type = pk_trans_type;
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public Integer getUrgent_level() {
		return urgent_level;
	}

	public void setUrgent_level(Integer urgent_level) {
		this.urgent_level = urgent_level;
	}

	public String getPk_psndoc() {
		return pk_psndoc;
	}

	public void setPk_psndoc(String pk_psndoc) {
		this.pk_psndoc = pk_psndoc;
	}

	public String getPk_dept() {
		return pk_dept;
	}

	public void setPk_dept(String pk_dept) {
		this.pk_dept = pk_dept;
	}

	public String getPk_invoice_type() {
		return pk_invoice_type;
	}

	public void setPk_invoice_type(String pk_invoice_type) {
		this.pk_invoice_type = pk_invoice_type;
	}

	public String getPk_supplier() {
		return pk_supplier;
	}

	public void setPk_supplier(String pk_supplier) {
		this.pk_supplier = pk_supplier;
	}

	public String getItem_code() {
		return item_code;
	}

	public void setItem_code(String item_code) {
		this.item_code = item_code;
	}

	public UFBoolean getIf_return() {
		return if_return;
	}

	public void setIf_return(UFBoolean if_return) {
		this.if_return = if_return;
	}

	public UFBoolean getIf_customs_official() {
		return if_customs_official;
	}

	public void setIf_customs_official(UFBoolean if_customs_official) {
		this.if_customs_official = if_customs_official;
	}

	public Integer getInvoice_goods_type() {
		return invoice_goods_type;
	}

	public void setInvoice_goods_type(Integer invoice_goods_type) {
		this.invoice_goods_type = invoice_goods_type;
	}

	public Integer getBalatype() {
		return balatype;
	}

	public void setBalatype(Integer balatype) {
		this.balatype = balatype;
	}

	public String getPk_car_type() {
		return pk_car_type;
	}

	public void setPk_car_type(String pk_car_type) {
		this.pk_car_type = pk_car_type;
	}

	public Integer getDeli_method() {
		return deli_method;
	}

	public void setDeli_method(Integer deli_method) {
		this.deli_method = deli_method;
	}

	public Integer getMileage() {
		return mileage;
	}

	public void setMileage(Integer mileage) {
		this.mileage = mileage;
	}

	public String getPk_carrier() {
		return pk_carrier;
	}

	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}

	public String getReq_carrier() {
		return req_carrier;
	}

	public void setReq_carrier(String req_carrier) {
		this.req_carrier = req_carrier;
	}

	public String getReq_carr_type() {
		return req_carr_type;
	}

	public void setReq_carr_type(String req_carr_type) {
		this.req_carr_type = req_carr_type;
	}

	public String getReq_car() {
		return req_car;
	}

	public void setReq_car(String req_car) {
		this.req_car = req_car;
	}

	public String getReq_car_type() {
		return req_car_type;
	}

	public void setReq_car_type(String req_car_type) {
		this.req_car_type = req_car_type;
	}

	public String getReq_drvi() {
		return req_drvi;
	}

	public void setReq_drvi(String req_drvi) {
		this.req_drvi = req_drvi;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getParentPKFieldName() {
		return PK_ASSIGN_RULE;
	}
	
	public String getPKFieldName() {
		return PK_ASSIGN_RULE_B;
	}
	
	public String getTableName() {
		return "ts_assign_rule_b";
	}

	public AssignRuleBVO() {
		super();
	}
}
