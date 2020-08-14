package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDouble;

/**
 * TS_OUTSTORAGE_B
 * 
 * @version 1.0
 * @since 1.0
 */
public class OutstorageBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_outstorage_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_outstorage;

	private java.lang.String vbillno;

	private java.lang.Integer vbillstatus;

	private java.lang.String cust_orderno;

	private java.lang.String orderno;

	private java.lang.String pk_customer;

	private org.nw.vo.pub.lang.UFDateTime act_ship_date;

	private java.lang.String pk_goods;

	private UFDouble order_count;

	private UFDouble picked_count;

	private UFDouble shiped_count;

	private java.lang.String lpn;

	private java.lang.String pk_goods_allocation;

	private java.lang.String pack;

	private java.lang.String min_pack;

	private org.nw.vo.pub.lang.UFDouble weight;
	private org.nw.vo.pub.lang.UFDouble volume;

	private org.nw.vo.pub.lang.UFDouble unit_weight;

	private org.nw.vo.pub.lang.UFDouble unit_volume;

	private org.nw.vo.pub.lang.UFDouble length;

	private org.nw.vo.pub.lang.UFDouble width;

	private org.nw.vo.pub.lang.UFDouble height;

	private java.lang.String pk_supplier;

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

	private org.nw.vo.pub.lang.UFBoolean sync_flag;

	public static final String PK_OUTSTORAGE_B = "pk_outstorage_b";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_OUTSTORAGE = "pk_outstorage";
	public static final String VBILLNO = "vbillno";
	public static final String VBILLSTATUS = "vbillstatus";
	public static final String CUST_ORDERNO = "cust_orderno";
	public static final String ORDERNO = "orderno";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String ACT_SHIP_DATE = "act_ship_date";
	public static final String PK_GOODS = "pk_goods";
	public static final String ORDER_COUNT = "order_count";
	public static final String PICKED_COUNT = "picked_count";
	public static final String SHIPED_COUNT = "shiped_count";
	public static final String LPN = "lpn";
	public static final String PK_GOODS_ALLOCATION = "pk_goods_allocation";
	public static final String PACK = "pack";
	public static final String MIN_PACK = "min_pack";
	public static final String UNIT_WEIGHT = "unit_weight";
	public static final String UNIT_VOLUME = "unit_volume";
	public static final String WEIGHT = "weight";
	public static final String VOLUME = "volume";
	public static final String LENGTH = "length";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String PK_SUPPLIER = "pk_supplier";
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
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String SYNC_FLAG = "sync_flag";

	public java.lang.String getPk_outstorage_b() {
		return this.pk_outstorage_b;
	}

	public void setPk_outstorage_b(java.lang.String value) {
		this.pk_outstorage_b = value;
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

	public java.lang.String getPk_outstorage() {
		return this.pk_outstorage;
	}

	public void setPk_outstorage(java.lang.String value) {
		this.pk_outstorage = value;
	}

	public org.nw.vo.pub.lang.UFDouble getWeight() {
		return weight;
	}

	public void setWeight(org.nw.vo.pub.lang.UFDouble weight) {
		this.weight = weight;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume() {
		return volume;
	}

	public void setVolume(org.nw.vo.pub.lang.UFDouble volume) {
		this.volume = volume;
	}

	public java.lang.String getVbillno() {
		return this.vbillno;
	}

	public void setVbillno(java.lang.String value) {
		this.vbillno = value;
	}

	public java.lang.Integer getVbillstatus() {
		return this.vbillstatus;
	}

	public void setVbillstatus(java.lang.Integer value) {
		this.vbillstatus = value;
	}

	public java.lang.String getCust_orderno() {
		return this.cust_orderno;
	}

	public void setCust_orderno(java.lang.String value) {
		this.cust_orderno = value;
	}

	public java.lang.String getOrderno() {
		return this.orderno;
	}

	public void setOrderno(java.lang.String value) {
		this.orderno = value;
	}

	public java.lang.String getPk_customer() {
		return this.pk_customer;
	}

	public void setPk_customer(java.lang.String value) {
		this.pk_customer = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getAct_ship_date() {
		return this.act_ship_date;
	}

	public void setAct_ship_date(org.nw.vo.pub.lang.UFDateTime value) {
		this.act_ship_date = value;
	}

	public java.lang.String getPk_goods() {
		return this.pk_goods;
	}

	public void setPk_goods(java.lang.String value) {
		this.pk_goods = value;
	}

	public UFDouble getOrder_count() {
		return order_count;
	}

	public void setOrder_count(UFDouble order_count) {
		this.order_count = order_count;
	}

	public UFDouble getPicked_count() {
		return picked_count;
	}

	public void setPicked_count(UFDouble picked_count) {
		this.picked_count = picked_count;
	}

	public UFDouble getShiped_count() {
		return shiped_count;
	}

	public void setShiped_count(UFDouble shiped_count) {
		this.shiped_count = shiped_count;
	}

	public java.lang.String getLpn() {
		return this.lpn;
	}

	public void setLpn(java.lang.String value) {
		this.lpn = value;
	}

	public java.lang.String getPk_goods_allocation() {
		return this.pk_goods_allocation;
	}

	public void setPk_goods_allocation(java.lang.String value) {
		this.pk_goods_allocation = value;
	}

	public java.lang.String getPack() {
		return this.pack;
	}

	public void setPack(java.lang.String value) {
		this.pack = value;
	}

	public java.lang.String getMin_pack() {
		return this.min_pack;
	}

	public void setMin_pack(java.lang.String value) {
		this.min_pack = value;
	}

	public org.nw.vo.pub.lang.UFDouble getUnit_weight() {
		return this.unit_weight;
	}

	public void setUnit_weight(org.nw.vo.pub.lang.UFDouble value) {
		this.unit_weight = value;
	}

	public org.nw.vo.pub.lang.UFDouble getUnit_volume() {
		return this.unit_volume;
	}

	public void setUnit_volume(org.nw.vo.pub.lang.UFDouble value) {
		this.unit_volume = value;
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

	public java.lang.String getPk_supplier() {
		return this.pk_supplier;
	}

	public void setPk_supplier(java.lang.String value) {
		this.pk_supplier = value;
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

	public org.nw.vo.pub.lang.UFBoolean getSync_flag() {
		return this.sync_flag;
	}

	public void setSync_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.sync_flag = value;
	}

	public String getParentPKFieldName() {
		return "pk_outstorage";
	}

	public String getPKFieldName() {
		return "pk_outstorage_b";
	}

	public String getTableName() {
		return "TS_OUTSTORAGE_B";
	}
}
