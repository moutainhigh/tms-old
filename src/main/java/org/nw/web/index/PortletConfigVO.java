package org.nw.web.index;

import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.PortletVO;

/**
 * portlet配置vo，包括显示到第几列，显示顺序
 * 
 * @author xuqc
 * @date 2014-5-21 下午02:57:48
 */
public class PortletConfigVO extends PortletVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private java.lang.Integer column_index;

	private java.lang.Integer display_order;

	private String fun_name;

	private String class_name;
	private String countSql;

	private Integer num_count = 0;// 根据query_sql查询出来的记录数
	
	private Integer popup_time;
	private UFBoolean if_popup;


	public String getCountSql() {
		return countSql;
	}

	public void setCountSql(String countSql) {
		this.countSql = countSql;
	}

	public Integer getPopup_time() {
		return popup_time;
	}

	public void setPopup_time(Integer popup_time) {
		this.popup_time = popup_time;
	}

	public UFBoolean getIf_popup() {
		return if_popup;
	}

	public void setIf_popup(UFBoolean if_popup) {
		this.if_popup = if_popup;
	}

	public java.lang.Integer getColumn_index() {
		return column_index;
	}

	public void setColumn_index(java.lang.Integer column_index) {
		this.column_index = column_index;
	}

	public java.lang.Integer getDisplay_order() {
		return display_order;
	}

	public void setDisplay_order(java.lang.Integer display_order) {
		this.display_order = display_order;
	}

	public String getFun_name() {
		return fun_name;
	}

	public void setFun_name(String fun_name) {
		this.fun_name = fun_name;
	}

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public Integer getNum_count() {
		return num_count;
	}

	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}

}
