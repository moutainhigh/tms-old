package com.tms.vo.cm;

import org.nw.vo.pub.lang.UFDouble;

/**
 * 车型吨位对照表
 * 
 * @version 1.0
 * @since 1.0
 */
public class CartypeTonnageVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_cartype_tonnage;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_expense_type;

	private UFDouble weight;

	private java.lang.Integer num;

	private java.lang.String pk_corp;

	private java.lang.String memo;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;

	public static final String PK_CARTYPE_TONNAGE = "pk_cartype_tonnage";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_EXPENSE_TYPE = "pk_expense_type";
	public static final String WEIGHT = "weight";
	public static final String NUM = "num";
	public static final String PK_CORP = "pk_corp";
	public static final String MEMO = "memo";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";

	public java.lang.String getPk_cartype_tonnage() {
		return this.pk_cartype_tonnage;
	}

	public void setPk_cartype_tonnage(java.lang.String value) {
		this.pk_cartype_tonnage = value;
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

	public java.lang.String getPk_expense_type() {
		return this.pk_expense_type;
	}

	public void setPk_expense_type(java.lang.String value) {
		this.pk_expense_type = value;
	}

	public UFDouble getWeight() {
		return weight;
	}

	public void setWeight(UFDouble weight) {
		this.weight = weight;
	}

	public java.lang.Integer getNum() {
		return this.num;
	}

	public void setNum(java.lang.Integer value) {
		this.num = value;
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
		return "pk_cartype_tonnage";
	}

	public String getTableName() {
		return "ts_cartype_tonnage";
	}
}
