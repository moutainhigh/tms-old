package org.nw.vo.sys;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.COLUMN_XTYPE;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.UiConstants.FORM_XTYPE;
import org.nw.jf.ext.Combox;
import org.nw.jf.ext.ListColumn;
import org.nw.jf.ext.RecordType;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.vo.pub.lang.UFBoolean;

/**
 * 报表模板中设置一个字段的属性VO
 * 
 * @version 1.0
 * @since 1.0
 */
public class ReportTempletBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;
	private java.lang.String pk_report_templet_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_report_templet;

	private java.lang.String itemkey;

	private java.lang.String defaultshowname;

	private java.lang.Integer data_type;

	private java.lang.String reftype;

	private java.lang.Integer width;

	private org.nw.vo.pub.lang.UFBoolean lock_flag;

	private org.nw.vo.pub.lang.UFBoolean total_flag;

	private java.lang.String loadformula;

	private java.lang.Integer display_order;

	private java.lang.String options;

	private String idcolname;

	private UFBoolean show_flag;

	/* 扩展属性 */
	private String renderer; // 渲染函数，目前只用于表体

	private String beforeRenderer;// 渲染函数，当存在默认的renderer时，不要设置renderer，设置这个

	private String summaryRenderer;// 统计行的列渲染函数，只用于表格

	private String font_color;// 字体颜色
	private String background_color;// 背景颜色

	public static final String PK_REPORT_TEMPLET_B = "pk_report_templet_b";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_REPORT_TEMPLET = "pk_report_templet";
	public static final String ITEMKEY = "itemkey";
	public static final String DEFAULTSHOWNAME = "defaultshowname";
	public static final String DATA_TYPE = "data_type";
	public static final String REFTYPE = "reftype";
	public static final String WIDTH = "width";
	public static final String LOCK_FLAG = "lock_flag";
	public static final String TOTAL_FLAG = "total_flag";
	public static final String LOADFORMULA = "loadformula";
	public static final String DISPLAY_ORDER = "display_order";
	public static final String OPTIONS = "options";
	public static final String IDCOLNAME = "idcolname";
	public static final String SHOW_FLAG = "show_flag";
	public static final String FONT_COLOR = "font_color";
	public static final String BACKGROUND_COLOR = "background_color";

	public String getFont_color() {
		return font_color;
	}

	public void setFont_color(String font_color) {
		this.font_color = font_color;
	}

	public String getBackground_color() {
		return background_color;
	}

	public void setBackground_color(String background_color) {
		this.background_color = background_color;
	}

	public UFBoolean getShow_flag() {
		return show_flag;
	}

	public void setShow_flag(UFBoolean show_flag) {
		this.show_flag = show_flag;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public String getBeforeRenderer() {
		return beforeRenderer;
	}

	public void setBeforeRenderer(String beforeRenderer) {
		this.beforeRenderer = beforeRenderer;
	}

	public String getSummaryRenderer() {
		return summaryRenderer;
	}

	public void setSummaryRenderer(String summaryRenderer) {
		this.summaryRenderer = summaryRenderer;
	}

	public String getIdcolname() {
		return idcolname;
	}

	public void setIdcolname(String idcolname) {
		this.idcolname = idcolname;
	}

	public java.lang.String getPk_report_templet_b() {
		return this.pk_report_templet_b;
	}

	public void setPk_report_templet_b(java.lang.String value) {
		this.pk_report_templet_b = value;
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

	public java.lang.String getPk_report_templet() {
		return this.pk_report_templet;
	}

	public void setPk_report_templet(java.lang.String value) {
		this.pk_report_templet = value;
	}

	public java.lang.String getItemkey() {
		return this.itemkey;
	}

	public void setItemkey(java.lang.String value) {
		this.itemkey = value;
	}

	public java.lang.String getDefaultshowname() {
		return this.defaultshowname;
	}

	public void setDefaultshowname(java.lang.String value) {
		this.defaultshowname = value;
	}

	public java.lang.Integer getData_type() {
		return this.data_type;
	}

	public void setData_type(java.lang.Integer value) {
		this.data_type = value;
	}

	public java.lang.String getReftype() {
		return this.reftype;
	}

	public void setReftype(java.lang.String value) {
		this.reftype = value;
	}

	public java.lang.Integer getWidth() {
		return this.width;
	}

	public void setWidth(java.lang.Integer value) {
		this.width = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getLock_flag() {
		return this.lock_flag;
	}

	public void setLock_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.lock_flag = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getTotal_flag() {
		return this.total_flag;
	}

	public void setTotal_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.total_flag = value;
	}

	public java.lang.String getLoadformula() {
		return this.loadformula;
	}

	public void setLoadformula(java.lang.String value) {
		this.loadformula = value;
	}

	public java.lang.Integer getDisplay_order() {
		return this.display_order;
	}

	public void setDisplay_order(java.lang.Integer value) {
		this.display_order = value;
	}

	public java.lang.String getOptions() {
		return this.options;
	}

	public void setOptions(java.lang.String value) {
		this.options = value;
	}

	public String getParentPKFieldName() {
		return "pk_report_templet";
	}

	public String getPKFieldName() {
		return "pk_report_templet_b";
	}

	public String getTableName() {
		return "nw_report_templet_b";
	}

	/**
	 * 导出时调用,此时不需要设置defaulshowname居中
	 * 
	 * @param exportflag
	 * @return
	 */
	public ListColumn buildListColumn(boolean exportflag) {
		ListColumn column = new ListColumn();
		column.setDataIndex(this.getItemkey().trim());
		if(this.getLock_flag() != null && this.getLock_flag().booleanValue()) {
			column.setLockflag(true);
		}
		if(this.getTotal_flag() != null && this.getTotal_flag().booleanValue()) {
			column.setSummaryType(UiConstants.SUMMARY_TYPE.SUM.toString());
		}
		if(this.getShow_flag() != null && !this.getShow_flag().booleanValue()) {
			column.setHidden(true);
		}

		if(!exportflag) {
			// 当是数字类型时，让header居中，使用align属性会让表头也居右
			if(DATATYPE.INTEGER.equals(this.getData_type()) || DATATYPE.DECIMAL.equals(this.getData_type())) {
				this.setDefaultshowname("<center>" + this.getDefaultshowname() + "</center>");
			}
		}
		column.setHeader("<span class='uft-grid-header-column'>" + this.getDefaultshowname() + "</span>");
		column.setWidth(this.getWidth());
		setListColumnValue(column);
		column.setRenderer(this.getRenderer()); // 设置渲染效果
		column.setBeforeRenderer(this.getBeforeRenderer());// 设置渲染函数的before函数，改函数在renderer中使用
		column.setSummaryRenderer(this.getSummaryRenderer());// 设置统计行的列渲染函数
		// 设置字体颜色以及背景色
		if(StringUtils.isNotBlank(this.getFont_color())) {// 字体颜色
			column.setCss("color:" + this.getFont_color() + ";");
		}
		if(StringUtils.isNotBlank(this.getBackground_color())) {// 背景颜色
			column.setCss("background-color:" + this.getBackground_color() + ";");
		}
		if(this.getItemkey().startsWith("_")) {// 以下划线开头的列都是功能列，居中对齐
			column.setAlign(UiConstants.ALIGN.CENTER.toString());
		}
		return column;
	}

	/**
	 * 生成表格的列模型
	 * 
	 * @return
	 */
	public ListColumn buildListColumn() {
		return buildListColumn(false);
	}

	/**
	 * 生成表格的列模型
	 * 
	 * @return
	 */
	public String genListColumn() {
		ListColumn column = buildListColumn();
		return column.toString();
	}

	/**
	 * 一些与datatype有关的字段，统一使用该方法设置值 包括xtype,editor,type
	 * 
	 * @param column
	 */
	private void setListColumnValue(ListColumn column) {
		// 是否能够为空
		if(DATATYPE.INTEGER.equals(this.getData_type())) {
			column.setAlign(UiConstants.ALIGN.RIGHT.toString()); // 数字使用右对齐
		} else if(DATATYPE.DECIMAL.equals(this.getData_type())) {
			column.setAlign(UiConstants.ALIGN.RIGHT.toString()); // 数字使用右对齐
			column.setXtype(UiConstants.COLUMN_XTYPE.NUMBERCOLUMN.toString());
			column.setFormat(UiTempletUtils.getNumberFormat(this.getReftype()));
		} else if(DATATYPE.SELECT.equals(this.getData_type())) {
			Combox combox = UIUtils.buildCombox(this);
			column.setEditor(combox.toString());
			column.setXtype(COLUMN_XTYPE.SELECT.toString());
		} else if(DATATYPE.OBJECT.equals(this.getData_type())) {
			if(FORM_XTYPE.MONTHPICKER.toString().equalsIgnoreCase(this.getReftype())) {
				// 若是 monthpicker类型，则使用monthcolumn
				column.setXtype(COLUMN_XTYPE.MONTHCOLUMN.toString());
			} else if(FORM_XTYPE.PERCENTFIELD.toString().equalsIgnoreCase(this.getReftype())) {
				// 若是 percentfield类型，则使用percentcolumn
				column.setXtype(COLUMN_XTYPE.PERCENTCOLUMN.toString());
			}
		} else if(DATATYPE.PASSWORD.equals(this.getData_type())) {
		} else if(DATATYPE.REF.equals(this.getData_type()) || DATATYPE.USERDEFINE.equals(this.getData_type())) {
			// 参照或者自定义档案
			column.setXtype(COLUMN_XTYPE.REF.toString());
			if(UIUtils.isMultiSelect(this.getReftype(), this.getData_type())) {
				// 如果是多选类型
				column.setXtype(COLUMN_XTYPE.MULTISELECTCOLUMN.toString());
			}
		} else if(DATATYPE.TEXTAREA.equals(this.getData_type())) {
		} else if(DATATYPE.CHECKBOX.equals(this.getData_type())) {
		} else if(DATATYPE.DATE.equals(this.getData_type())) {
		} else if(DATATYPE.TIME.equals(this.getData_type())) {
		} else if(DATATYPE.TIMESTAMP.equals(this.getData_type())) {
			column.setXtype(COLUMN_XTYPE.DATETIME.toString());
		} else if(DATATYPE.TEXT.equals(this.getData_type())) {
		}
	}

	/**
	 * 生成Ext grid中的recordType对象
	 * 
	 * @return
	 */
	public RecordType buildRecordType() {
		RecordType record = new RecordType();
		record.setName(this.getItemkey().trim());
		record.setType(UIUtils.getColumnType(this.getData_type().intValue()));
		if(StringUtils.isNotBlank(this.getIdcolname())) {
			record.setSortName(this.getIdcolname());
		}
		return record;
	}

	/**
	 * 生成Ext grid中的recordType对象
	 * 
	 * @return
	 */
	public String genRecordType() {
		return buildRecordType().toString();
	}
}
