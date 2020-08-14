package com.tms.vo.inv;

import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_orderlot_rd
 * 
 * @version 1.0
 * @since 1.0
 */
public class OrderlotRdVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_orderlot_rd;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String lot;

	private java.lang.Integer valuation_type;

	private java.lang.String pk_expense_type;

	private java.lang.Integer node_count;

	private UFDouble car_weight;

	private java.lang.Integer car_num;

	private java.lang.Integer num_count;

	private org.nw.vo.pub.lang.UFDouble weight_count;

	private org.nw.vo.pub.lang.UFDouble volume_count;

	private org.nw.vo.pub.lang.UFDouble fee_weight_count;

	private org.nw.vo.pub.lang.UFDouble price;

	private org.nw.vo.pub.lang.UFDouble amount;
	private org.nw.vo.pub.lang.UFDouble contract_amount;

	private java.lang.String pk_contract_b;

	private org.nw.vo.pub.lang.UFBoolean system_create;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String create_user;

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

	public static final String PK_ORDERLOT_RD = "pk_orderlot_rd";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String LOT = "lot";
	public static final String VALUATION_TYPE = "valuation_type";
	public static final String PK_EXPENSE_TYPE = "pk_expense_type";
	public static final String NODE_COUNT = "node_count";
	public static final String PK_CAR_TYPE = "pk_car_type";
	public static final String CAR_NUM = "car_num";
	public static final String NUM_COUNT = "num_count";
	public static final String WEIGHT_COUNT = "weight_count";
	public static final String VOLUME_COUNT = "volume_count";
	public static final String FEE_WEIGHT_COUNT = "fee_weight_count";
	public static final String PRICE = "price";
	public static final String AMOUNT = "amount";
	public static final String CONTRACT_AMOUNT = "contract_amount";
	public static final String PK_CONTRACT_B = "pk_contract_b";
	public static final String SYSTEM_CREATE = "system_create";
	public static final String MODIFY_TIME = "modify_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String CREATE_TIME = "create_time";
	public static final String CREATE_USER = "create_user";
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

	public org.nw.vo.pub.lang.UFDouble getContract_amount() {
		return contract_amount;
	}

	public void setContract_amount(org.nw.vo.pub.lang.UFDouble contract_amount) {
		this.contract_amount = contract_amount;
	}

	public java.lang.String getPk_orderlot_rd() {
		return this.pk_orderlot_rd;
	}

	public void setPk_orderlot_rd(java.lang.String value) {
		this.pk_orderlot_rd = value;
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

	public java.lang.String getLot() {
		return this.lot;
	}

	public void setLot(java.lang.String value) {
		this.lot = value;
	}

	public java.lang.Integer getValuation_type() {
		return this.valuation_type;
	}

	public void setValuation_type(java.lang.Integer value) {
		this.valuation_type = value;
	}

	public java.lang.String getPk_expense_type() {
		return this.pk_expense_type;
	}

	public void setPk_expense_type(java.lang.String value) {
		this.pk_expense_type = value;
	}

	public java.lang.Integer getNode_count() {
		return this.node_count;
	}

	public void setNode_count(java.lang.Integer value) {
		this.node_count = value;
	}

	public UFDouble getCar_weight() {
		return car_weight;
	}

	public void setCar_weight(UFDouble car_weight) {
		this.car_weight = car_weight;
	}

	public java.lang.Integer getCar_num() {
		return this.car_num;
	}

	public void setCar_num(java.lang.Integer value) {
		this.car_num = value;
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

	public org.nw.vo.pub.lang.UFDouble getPrice() {
		return this.price;
	}

	public void setPrice(org.nw.vo.pub.lang.UFDouble value) {
		this.price = value;
	}

	public org.nw.vo.pub.lang.UFDouble getAmount() {
		return this.amount;
	}

	public void setAmount(org.nw.vo.pub.lang.UFDouble value) {
		this.amount = value;
	}

	public java.lang.String getPk_contract_b() {
		return this.pk_contract_b;
	}

	public void setPk_contract_b(java.lang.String value) {
		this.pk_contract_b = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getSystem_create() {
		return this.system_create;
	}

	public void setSystem_create(org.nw.vo.pub.lang.UFBoolean value) {
		this.system_create = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getModify_time() {
		return this.modify_time;
	}

	public void setModify_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.modify_time = value;
	}

	public java.lang.String getModify_user() {
		return this.modify_user;
	}

	public void setModify_user(java.lang.String value) {
		this.modify_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.create_time = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
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
		return "lot";
	}

	public String getPKFieldName() {
		return "pk_orderlot_rd";
	}

	public String getTableName() {
		return "ts_orderlot_rd";
	}
}
