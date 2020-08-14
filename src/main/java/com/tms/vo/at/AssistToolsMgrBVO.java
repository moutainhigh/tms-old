package com.tms.vo.at;

import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_assist_tools_mgr_b
 * 
 * @version 1.0
 * @since 1.0
 */
public class AssistToolsMgrBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_assist_tools_mgr_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_assist_tools_mgr;

	private java.lang.String pk_assist_tools;

	private java.lang.Integer num;

	private org.nw.vo.pub.lang.UFBoolean is_return;

	private java.lang.Integer grant_num;

	private java.lang.Integer return_num;
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

	public static final String PK_ASSIST_TOOLS_MGR_B = "pk_assist_tools_mgr_b";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_ASSIST_TOOLS_MGR = "pk_assist_tools_mgr";
	public static final String PK_ASSIST_TOOLS = "pk_assist_tools";
	public static final String NUM = "num";
	public static final String IS_RETURN = "is_return";
	public static final String GRANT_NUM = "grant_num";
	public static final String RETURN_NUM = "return_num";

	public java.lang.String getPk_assist_tools_mgr_b() {
		return this.pk_assist_tools_mgr_b;
	}

	public void setPk_assist_tools_mgr_b(java.lang.String value) {
		this.pk_assist_tools_mgr_b = value;
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

	public java.lang.String getPk_assist_tools_mgr() {
		return this.pk_assist_tools_mgr;
	}

	public void setPk_assist_tools_mgr(java.lang.String value) {
		this.pk_assist_tools_mgr = value;
	}

	public java.lang.String getPk_assist_tools() {
		return this.pk_assist_tools;
	}

	public void setPk_assist_tools(java.lang.String value) {
		this.pk_assist_tools = value;
	}

	public java.lang.Integer getNum() {
		return this.num;
	}

	public void setNum(java.lang.Integer value) {
		this.num = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getIs_return() {
		return this.is_return;
	}

	public void setIs_return(org.nw.vo.pub.lang.UFBoolean value) {
		this.is_return = value;
	}

	public java.lang.Integer getGrant_num() {
		return this.grant_num;
	}

	public void setGrant_num(java.lang.Integer value) {
		this.grant_num = value;
	}

	public java.lang.Integer getReturn_num() {
		return this.return_num;
	}

	public void setReturn_num(java.lang.Integer value) {
		this.return_num = value;
	}

	public String getParentPKFieldName() {
		return "pk_assist_tools_mgr";
	}

	public String getPKFieldName() {
		return "pk_assist_tools_mgr_b";
	}

	public String getTableName() {
		return "ts_assist_tools_mgr_b";
	}
}
