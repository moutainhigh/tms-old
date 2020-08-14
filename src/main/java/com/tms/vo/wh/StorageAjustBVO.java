package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_storage_ajust_b
 * 
 * @version 1.0
 * @since 1.0
 */
public class StorageAjustBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_storage_ajust_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_storage_ajust;

	private java.lang.String pk_goods;

	private java.lang.String lot;

	private java.lang.String dest_lot;

	private org.nw.vo.pub.lang.UFDouble ajust_num;

	private java.lang.String pk_goods_allocation;

	private java.lang.String dest_goods_allocation;

	private java.lang.String lpn;

	private java.lang.String dest_lpn;

	private java.lang.Integer goods_prop;

	private java.lang.String lot_attr1;

	private java.lang.String lot_attr2;

	private java.lang.String lot_attr3;

	private java.lang.String lot_attr4;

	private java.lang.String lot_attr5;

	private java.lang.String lot_attr6;

	private java.lang.String lot_attr7;

	private java.lang.String lot_attr8;

	private java.lang.String lot_attr9;

	private java.lang.String lot_attr10;

	private java.lang.String lot_attr11;

	private org.nw.vo.pub.lang.UFDate produce_date;

	private org.nw.vo.pub.lang.UFDate expire_date;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;
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

	public static final String PK_STORAGE_AJUST_B = "pk_storage_ajust_b";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_STORAGE_AJUST = "pk_storage_ajust";
	public static final String PK_GOODS = "pk_goods";
	public static final String LOT = "lot";
	public static final String DEST_LOT = "dest_lot";
	public static final String AJUST_NUM = "ajust_num";
	public static final String PK_GOODS_ALLOCATION = "pk_goods_allocation";
	public static final String DEST_GOODS_ALLOCATION = "dest_goods_allocation";
	public static final String LPN = "lpn";
	public static final String DEST_LPN = "dest_lpn";
	public static final String GOODS_PROP = "goods_prop";
	public static final String LOT_ATTR1 = "lot_attr1";
	public static final String LOT_ATTR2 = "lot_attr2";
	public static final String LOT_ATTR3 = "lot_attr3";
	public static final String LOT_ATTR4 = "lot_attr4";
	public static final String LOT_ATTR5 = "lot_attr5";
	public static final String LOT_ATTR6 = "lot_attr6";
	public static final String LOT_ATTR7 = "lot_attr7";
	public static final String LOT_ATTR8 = "lot_attr8";
	public static final String LOT_ATTR9 = "lot_attr9";
	public static final String LOT_ATTR10 = "lot_attr10";
	public static final String LOT_ATTR11 = "lot_attr11";
	public static final String PRODUCE_DATE = "produce_date";
	public static final String EXPIRE_DATE = "expire_date";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";

	public java.lang.String getLot() {
		return lot;
	}

	public void setLot(java.lang.String lot) {
		this.lot = lot;
	}

	public java.lang.String getPk_goods_allocation() {
		return pk_goods_allocation;
	}

	public void setPk_goods_allocation(java.lang.String pk_goods_allocation) {
		this.pk_goods_allocation = pk_goods_allocation;
	}

	public java.lang.String getLpn() {
		return lpn;
	}

	public void setLpn(java.lang.String lpn) {
		this.lpn = lpn;
	}

	public java.lang.String getPk_storage_ajust_b() {
		return this.pk_storage_ajust_b;
	}

	public void setPk_storage_ajust_b(java.lang.String value) {
		this.pk_storage_ajust_b = value;
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

	public java.lang.String getPk_storage_ajust() {
		return this.pk_storage_ajust;
	}

	public void setPk_storage_ajust(java.lang.String value) {
		this.pk_storage_ajust = value;
	}

	public java.lang.String getPk_goods() {
		return this.pk_goods;
	}

	public void setPk_goods(java.lang.String value) {
		this.pk_goods = value;
	}

	public java.lang.String getDest_lot() {
		return this.dest_lot;
	}

	public void setDest_lot(java.lang.String value) {
		this.dest_lot = value;
	}

	public org.nw.vo.pub.lang.UFDouble getAjust_num() {
		return this.ajust_num;
	}

	public void setAjust_num(org.nw.vo.pub.lang.UFDouble value) {
		this.ajust_num = value;
	}

	public java.lang.String getDest_goods_allocation() {
		return this.dest_goods_allocation;
	}

	public void setDest_goods_allocation(java.lang.String value) {
		this.dest_goods_allocation = value;
	}

	public java.lang.String getDest_lpn() {
		return this.dest_lpn;
	}

	public void setDest_lpn(java.lang.String value) {
		this.dest_lpn = value;
	}

	public java.lang.Integer getGoods_prop() {
		return this.goods_prop;
	}

	public void setGoods_prop(java.lang.Integer value) {
		this.goods_prop = value;
	}

	public java.lang.String getLot_attr1() {
		return this.lot_attr1;
	}

	public void setLot_attr1(java.lang.String value) {
		this.lot_attr1 = value;
	}

	public java.lang.String getLot_attr2() {
		return this.lot_attr2;
	}

	public void setLot_attr2(java.lang.String value) {
		this.lot_attr2 = value;
	}

	public java.lang.String getLot_attr3() {
		return this.lot_attr3;
	}

	public void setLot_attr3(java.lang.String value) {
		this.lot_attr3 = value;
	}

	public java.lang.String getLot_attr4() {
		return this.lot_attr4;
	}

	public void setLot_attr4(java.lang.String value) {
		this.lot_attr4 = value;
	}

	public java.lang.String getLot_attr5() {
		return this.lot_attr5;
	}

	public void setLot_attr5(java.lang.String value) {
		this.lot_attr5 = value;
	}

	public java.lang.String getLot_attr6() {
		return this.lot_attr6;
	}

	public void setLot_attr6(java.lang.String value) {
		this.lot_attr6 = value;
	}

	public java.lang.String getLot_attr7() {
		return this.lot_attr7;
	}

	public void setLot_attr7(java.lang.String value) {
		this.lot_attr7 = value;
	}

	public java.lang.String getLot_attr8() {
		return this.lot_attr8;
	}

	public void setLot_attr8(java.lang.String value) {
		this.lot_attr8 = value;
	}

	public java.lang.String getLot_attr9() {
		return this.lot_attr9;
	}

	public void setLot_attr9(java.lang.String value) {
		this.lot_attr9 = value;
	}

	public java.lang.String getLot_attr10() {
		return this.lot_attr10;
	}

	public void setLot_attr10(java.lang.String value) {
		this.lot_attr10 = value;
	}

	public java.lang.String getLot_attr11() {
		return this.lot_attr11;
	}

	public void setLot_attr11(java.lang.String value) {
		this.lot_attr11 = value;
	}

	public org.nw.vo.pub.lang.UFDate getProduce_date() {
		return this.produce_date;
	}

	public void setProduce_date(org.nw.vo.pub.lang.UFDate value) {
		this.produce_date = value;
	}

	public org.nw.vo.pub.lang.UFDate getExpire_date() {
		return this.expire_date;
	}

	public void setExpire_date(org.nw.vo.pub.lang.UFDate value) {
		this.expire_date = value;
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
		return "pk_storage_ajust";
	}

	public String getPKFieldName() {
		return "pk_storage_ajust_b";
	}

	public String getTableName() {
		return "ts_storage_ajust_b";
	}
}
