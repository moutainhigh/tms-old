package org.nw.vo.sys;

/**
 * nw_role_plan
 * 
 * @version 1.0
 * @since 1.0
 */
public class RolePlanVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_role_plan;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_portlet_plan;

	private java.lang.String pk_role;

	public static final String PK_ROLE_PLAN = "pk_role_plan";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_PORTLET_PLAN = "pk_portlet_plan";
	public static final String PK_ROLE = "pk_role";

	public java.lang.String getPk_role_plan() {
		return this.pk_role_plan;
	}

	public void setPk_role_plan(java.lang.String value) {
		this.pk_role_plan = value;
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

	public java.lang.String getPk_portlet_plan() {
		return this.pk_portlet_plan;
	}

	public void setPk_portlet_plan(java.lang.String value) {
		this.pk_portlet_plan = value;
	}

	public java.lang.String getPk_role() {
		return this.pk_role;
	}

	public void setPk_role(java.lang.String value) {
		this.pk_role = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_role_plan";
	}

	public String getTableName() {
		return "nw_role_plan";
	}
}
