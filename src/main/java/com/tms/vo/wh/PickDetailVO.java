package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_pick_detail
 * 
 * @version 1.0
 * @since 1.0
 */
public class PickDetailVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_pick_detail;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private String pk_outstorage;
	private String pk_outstorage_b;

	private java.lang.String vbillno;

	private java.lang.Integer vbillstatus;

	private java.lang.String outstorage_vbillno;

	private java.lang.String outstorage_b_vbillno;

	private java.lang.String lot;

	private java.lang.String pk_customer;

	private java.lang.String pk_goods;

	private org.nw.vo.pub.lang.UFDouble order_count;

	private org.nw.vo.pub.lang.UFDouble picked_count;

	private org.nw.vo.pub.lang.UFDouble shiped_count;

	private java.lang.String from_loc;

	private java.lang.String from_lpn;

	private java.lang.String to_loc;

	private java.lang.String lpn;

	private java.lang.String pk_goods_allocation;

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

	private java.lang.String pick_user;

	private org.nw.vo.pub.lang.UFDate pick_date;

	private java.lang.String ship_user;

	private org.nw.vo.pub.lang.UFDate ship_date;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;
	private String pk_corp;
	private UFDate dbilldate;

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

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

	public static final String PK_PICK_DETAIL = "pk_pick_detail";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_OUTSTORAGE = "pk_outstorage";
	public static final String PK_OUTSTORAGE_B = "pk_outstorage_b";
	public static final String VBILLNO = "vbillno";
	public static final String VBILLSTATUS = "vbillstatus";
	public static final String OUTSTORAGE_VBILLNO = "outstorage_vbillno";
	public static final String OUTSTORAGE_B_VBILLNO = "outstorage_b_vbillno";
	public static final String LOT = "lot";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String PK_GOODS = "pk_goods";
	public static final String ORDER_COUNT = "order_count";
	public static final String PICKED_COUNT = "picked_count";
	public static final String SHIPED_COUNT = "shiped_count";
	public static final String FROM_LOC = "from_loc";
	public static final String FROM_LPN = "from_lpn";
	public static final String TO_LOC = "to_loc";
	public static final String LPN = "lpn";
	public static final String PK_GOODS_ALLOCATION = "pk_goods_allocation";
	public static final String PACK = "pack";
	public static final String MIN_PACK = "min_pack";
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
	public static final String PICK_USER = "pick_user";
	public static final String PICK_DATE = "pick_date";
	public static final String SHIP_USER = "ship_user";
	public static final String SHIP_DATE = "ship_date";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String SYNC_FLAG = "sync_flag";
	public static final String DBILLDATE = "dbilldate";

	public UFDate getDbilldate() {
		return dbilldate;
	}

	public void setDbilldate(UFDate dbilldate) {
		this.dbilldate = dbilldate;
	}

	public String getPk_outstorage_b() {
		return pk_outstorage_b;
	}

	public void setPk_outstorage_b(String pk_outstorage_b) {
		this.pk_outstorage_b = pk_outstorage_b;
	}

	public java.lang.String getPk_pick_detail() {
		return this.pk_pick_detail;
	}

	public void setPk_pick_detail(java.lang.String value) {
		this.pk_pick_detail = value;
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

	public String getPk_outstorage() {
		return pk_outstorage;
	}

	public void setPk_outstorage(String pk_outstorage) {
		this.pk_outstorage = pk_outstorage;
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

	public java.lang.String getOutstorage_vbillno() {
		return this.outstorage_vbillno;
	}

	public void setOutstorage_vbillno(java.lang.String value) {
		this.outstorage_vbillno = value;
	}

	public java.lang.String getOutstorage_b_vbillno() {
		return this.outstorage_b_vbillno;
	}

	public void setOutstorage_b_vbillno(java.lang.String value) {
		this.outstorage_b_vbillno = value;
	}

	public java.lang.String getLot() {
		return this.lot;
	}

	public void setLot(java.lang.String value) {
		this.lot = value;
	}

	public java.lang.String getPk_customer() {
		return this.pk_customer;
	}

	public void setPk_customer(java.lang.String value) {
		this.pk_customer = value;
	}

	public java.lang.String getPk_goods() {
		return this.pk_goods;
	}

	public void setPk_goods(java.lang.String value) {
		this.pk_goods = value;
	}

	public org.nw.vo.pub.lang.UFDouble getOrder_count() {
		return this.order_count;
	}

	public void setOrder_count(org.nw.vo.pub.lang.UFDouble value) {
		this.order_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getPicked_count() {
		return this.picked_count;
	}

	public void setPicked_count(org.nw.vo.pub.lang.UFDouble value) {
		this.picked_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getShiped_count() {
		return this.shiped_count;
	}

	public void setShiped_count(org.nw.vo.pub.lang.UFDouble value) {
		this.shiped_count = value;
	}

	public java.lang.String getFrom_loc() {
		return this.from_loc;
	}

	public void setFrom_loc(java.lang.String value) {
		this.from_loc = value;
	}

	public java.lang.String getFrom_lpn() {
		return this.from_lpn;
	}

	public void setFrom_lpn(java.lang.String value) {
		this.from_lpn = value;
	}

	public java.lang.String getTo_loc() {
		return this.to_loc;
	}

	public void setTo_loc(java.lang.String value) {
		this.to_loc = value;
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

	public java.lang.String getPick_user() {
		return this.pick_user;
	}

	public void setPick_user(java.lang.String value) {
		this.pick_user = value;
	}

	public org.nw.vo.pub.lang.UFDate getPick_date() {
		return this.pick_date;
	}

	public void setPick_date(org.nw.vo.pub.lang.UFDate value) {
		this.pick_date = value;
	}

	public java.lang.String getShip_user() {
		return this.ship_user;
	}

	public void setShip_user(java.lang.String value) {
		this.ship_user = value;
	}

	public org.nw.vo.pub.lang.UFDate getShip_date() {
		return this.ship_date;
	}

	public void setShip_date(org.nw.vo.pub.lang.UFDate value) {
		this.ship_date = value;
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
		return "pk_pick_detail";
	}

	public String getTableName() {
		return "ts_pick_detail";
	}
}
