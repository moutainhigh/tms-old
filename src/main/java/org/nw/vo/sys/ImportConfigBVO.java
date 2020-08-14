package org.nw.vo.sys;

/**
 * nw_import_config_b
 * 
 * @version 1.0
 * @since 1.0
 */
public class ImportConfigBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_import_config_b;

	private java.lang.String pk_import_config;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String field_name;

	private java.lang.String field_code;

	private org.nw.vo.pub.lang.UFBoolean keyfield_flag;

	private org.nw.vo.pub.lang.UFBoolean notnull_flag;

	private java.lang.Integer display_order;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;

	private Integer cell_type;

	public static final String PK_IMPORT_CONFIG_B = "pk_import_config_b";
	public static final String PK_IMPORT_CONFIG = "pk_import_config";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String FIELD_NAME = "field_name";
	public static final String FIELD_CODE = "field_code";
	public static final String KEYFIELD_FLAG = "keyfield_flag";
	public static final String NOTNULL_FLAG = "notnull_flag";
	public static final String DISPLAY_ORDER = "display_order";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String CELL_TYPE = "cell_type";

	public org.nw.vo.pub.lang.UFBoolean getNotnull_flag() {
		return notnull_flag;
	}

	public void setNotnull_flag(org.nw.vo.pub.lang.UFBoolean notnull_flag) {
		this.notnull_flag = notnull_flag;
	}

	public Integer getCell_type() {
		return cell_type;
	}

	public void setCell_type(Integer cell_type) {
		this.cell_type = cell_type;
	}

	public java.lang.String getPk_import_config_b() {
		return this.pk_import_config_b;
	}

	public void setPk_import_config_b(java.lang.String value) {
		this.pk_import_config_b = value;
	}

	public java.lang.String getPk_import_config() {
		return this.pk_import_config;
	}

	public void setPk_import_config(java.lang.String value) {
		this.pk_import_config = value;
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

	public java.lang.String getField_name() {
		return this.field_name;
	}

	public void setField_name(java.lang.String value) {
		this.field_name = value;
	}

	public java.lang.String getField_code() {
		return this.field_code;
	}

	public void setField_code(java.lang.String value) {
		this.field_code = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getKeyfield_flag() {
		return this.keyfield_flag;
	}

	public void setKeyfield_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.keyfield_flag = value;
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
		return "pk_import_config";
	}

	public String getPKFieldName() {
		return "pk_import_config_b";
	}

	public String getTableName() {
		return "nw_import_config_b";
	}
}
