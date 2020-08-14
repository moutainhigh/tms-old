package org.nw.vo.sys;

import org.nw.vo.pub.lang.UFBoolean;

/**
 * nw_portlet_plan_b
 * 
 * @version 1.0
 * @since 1.0
 */
public class PortletPlanBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

		private java.lang.String pk_portlet_plan_b;
	
		private java.lang.String pk_portlet_plan;
	
		private java.lang.Integer dr;
	
		private org.nw.vo.pub.lang.UFDateTime ts;
	
		private java.lang.String portlet_id;
	
		private java.lang.Integer column_index;
	
		private java.lang.Integer display_order;
	
		private java.lang.String def1;
	
		private java.lang.String def2;
	
		private java.lang.String def3;
	
		private java.lang.String def4;
	
		private java.lang.String def5;
		private UFBoolean if_popup;
		private Integer popup_time;
		
		
	public UFBoolean getIf_popup() {
			return if_popup;
		}

		public void setIf_popup(UFBoolean if_popup) {
			this.if_popup = if_popup;
		}

		public Integer getPopup_time() {
			return popup_time;
		}

		public void setPopup_time(Integer popup_time) {
			this.popup_time = popup_time;
		}

	public static final String PK_PORTLET_PLAN_B = "pk_portlet_plan_b";
	public static final String PK_PORTLET_PLAN = "pk_portlet_plan";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PORTLET_ID = "portlet_id";
	public static final String COLUMN_INDEX = "column_index";
	public static final String DISPLAY_ORDER = "display_order";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	
	
	public java.lang.String getPk_portlet_plan_b() {
		return this.pk_portlet_plan_b;
	}

	public void setPk_portlet_plan_b(java.lang.String value) {
		this.pk_portlet_plan_b = value;
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

	public java.lang.String getPortlet_id() {
		return this.portlet_id;
	}

	public void setPortlet_id(java.lang.String value) {
		this.portlet_id = value;
	}

	public java.lang.Integer getColumn_index() {
		return this.column_index;
	}

	public void setColumn_index(java.lang.Integer value) {
		this.column_index = value;
	}

	public java.lang.Integer getDisplay_order() {
		return this.display_order;
	}

	public void setDisplay_order(java.lang.Integer value) {
		this.display_order = value;
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
		return "pk_portlet_plan";
	}

	public String getPKFieldName() {
		return "pk_portlet_plan_b";
	}

	public String getTableName() {
		return "nw_portlet_plan_b";
	}
}
