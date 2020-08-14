package org.nw.vo.sys;

/**
 * nw_portlet_plan
 * 
 * @version 1.0
 * @since 1.0
 */
public class PortletPlanVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_portlet_plan;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String plan_code;

	private java.lang.String plan_name;

	private org.nw.vo.pub.lang.UFBoolean if_default;

	private org.nw.vo.pub.lang.UFBoolean locked_flag;

	private java.lang.String create_time;

	private java.lang.String create_user;

	private java.lang.String pk_corp;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private java.lang.String modify_user;

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

	private String memo;

	public static final String PK_PORTLET_PLAN = "pk_portlet_plan";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PLAN_CODE = "plan_code";
	public static final String PLAN_NAME = "plan_name";
	public static final String IF_DEFAULT = "if_default";
	public static final String LOCKED_FLAG = "locked_flag";
	public static final String CREATE_TIME = "create_time";
	public static final String CREATE_USER = "create_user";
	public static final String PK_CORP = "pk_corp";
	public static final String MODIFY_TIME = "modify_time";
	public static final String MODIFY_USER = "modify_user";
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public java.lang.String getPk_portlet_plan() {
		return this.pk_portlet_plan;
	}

	public void setPk_portlet_plan(java.lang.String value) {
		this.pk_portlet_plan = value;
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

	public java.lang.String getPlan_code() {
		return this.plan_code;
	}

	public void setPlan_code(java.lang.String value) {
		this.plan_code = value;
	}

	public java.lang.String getPlan_name() {
		return this.plan_name;
	}

	public void setPlan_name(java.lang.String value) {
		this.plan_name = value;
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

	public java.lang.String getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(java.lang.String value) {
		this.create_time = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
	}

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
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

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_portlet_plan";
	}

	public String getTableName() {
		return "nw_portlet_plan";
	}
}
