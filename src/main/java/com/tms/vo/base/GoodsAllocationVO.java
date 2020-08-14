package com.tms.vo.base;

import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_goods_allocation
 * 
 * @version 1.0
 * @since 1.0
 */
public class GoodsAllocationVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_goods_allocation;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String code;

	private java.lang.String name;

	private java.lang.Integer istatus;

	private org.nw.vo.pub.lang.UFDouble length;

	private org.nw.vo.pub.lang.UFDouble width;

	private org.nw.vo.pub.lang.UFDouble height;

	private org.nw.vo.pub.lang.UFDouble volume;

	private java.lang.Integer useage;

	private java.lang.String store_area;

	private java.lang.Integer cateorage;

	private java.lang.Integer property;

	private java.lang.Integer lpn_num;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;
	private org.nw.vo.pub.lang.UFBoolean locked_flag;

	private String pk_corp;
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

	public static final String PK_GOODS_ALLOCATION = "pk_goods_allocation";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String ISTATUS = "istatus";
	public static final String LENGTH = "length";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String VOLUME = "volume";
	public static final String USEAGE = "useage";
	public static final String STORE_AREA = "store_area";
	public static final String CATEORAGE = "cateorage";
	public static final String PROPERTY = "property";
	public static final String LPN_NUM = "lpn_num";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";

	public java.lang.String getPk_goods_allocation() {
		return this.pk_goods_allocation;
	}

	public void setPk_goods_allocation(java.lang.String value) {
		this.pk_goods_allocation = value;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public org.nw.vo.pub.lang.UFBoolean getLocked_flag() {
		return locked_flag;
	}

	public void setLocked_flag(org.nw.vo.pub.lang.UFBoolean locked_flag) {
		this.locked_flag = locked_flag;
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

	public java.lang.String getCode() {
		return this.code;
	}

	public void setCode(java.lang.String value) {
		this.code = value;
	}

	public java.lang.String getName() {
		return this.name;
	}

	public void setName(java.lang.String value) {
		this.name = value;
	}

	public java.lang.Integer getIstatus() {
		return this.istatus;
	}

	public void setIstatus(java.lang.Integer value) {
		this.istatus = value;
	}

	public org.nw.vo.pub.lang.UFDouble getLength() {
		return this.length;
	}

	public void setLength(org.nw.vo.pub.lang.UFDouble value) {
		this.length = value;
	}

	public org.nw.vo.pub.lang.UFDouble getWidth() {
		return this.width;
	}

	public void setWidth(org.nw.vo.pub.lang.UFDouble value) {
		this.width = value;
	}

	public org.nw.vo.pub.lang.UFDouble getHeight() {
		return this.height;
	}

	public void setHeight(org.nw.vo.pub.lang.UFDouble value) {
		this.height = value;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume() {
		return this.volume;
	}

	public void setVolume(org.nw.vo.pub.lang.UFDouble value) {
		this.volume = value;
	}

	public java.lang.Integer getUseage() {
		return this.useage;
	}

	public void setUseage(java.lang.Integer value) {
		this.useage = value;
	}

	public java.lang.String getStore_area() {
		return this.store_area;
	}

	public void setStore_area(java.lang.String value) {
		this.store_area = value;
	}

	public java.lang.Integer getCateorage() {
		return this.cateorage;
	}

	public void setCateorage(java.lang.Integer value) {
		this.cateorage = value;
	}

	public java.lang.Integer getProperty() {
		return this.property;
	}

	public void setProperty(java.lang.Integer value) {
		this.property = value;
	}

	public java.lang.Integer getLpn_num() {
		return this.lpn_num;
	}

	public void setLpn_num(java.lang.Integer value) {
		this.lpn_num = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.create_time = value;
	}

	public java.lang.String getModify_user() {
		return this.modify_user;
	}

	public void setModify_user(java.lang.String value) {
		this.modify_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getModify_time() {
		return this.modify_time;
	}

	public void setModify_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.modify_time = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_goods_allocation";
	}

	public String getTableName() {
		return "ts_goods_allocation";
	}
}
