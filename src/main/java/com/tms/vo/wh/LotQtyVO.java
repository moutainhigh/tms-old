package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDouble;

/**
 * TS_LOT_QTY
 * 
 * @version 1.0
 * @since 1.0
 */
public class LotQtyVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_lot_qty;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String lot;

	private java.lang.String pk_goods_allocation;

	private java.lang.String lpn;

	private java.lang.String pk_customer;

	private java.lang.String pk_goods;

	private org.nw.vo.pub.lang.UFDouble stock_num;

	private org.nw.vo.pub.lang.UFDouble available_num;

	private org.nw.vo.pub.lang.UFDouble located_num;

	private org.nw.vo.pub.lang.UFDouble choosed_num;

	private org.nw.vo.pub.lang.UFDouble picked_num;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private String instorage_vbillno;

	private UFDouble pick_num;// 扩展的字段，分配页面写入的分配数量

	private UFDouble move_num;// 扩展字段，库内移动页面写入的移动数量
	private String dest_lpn;// 扩展字段，库内移动页面写入的目标LPN
	private String dest_goods_allocation;// 扩展字段，库内移动页面写入的目标货位

	private String pk_corp;

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

	public static final String PK_LOT_QTY = "pk_lot_qty";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String LOT = "lot";
	public static final String PK_GOODS_ALLOCATION = "pk_goods_allocation";
	public static final String LPN = "lpn";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String PK_GOODS = "pk_goods";
	public static final String STOCK_NUM = "stock_num";
	public static final String AVAILABLE_NUM = "available_num";
	public static final String LOCATED_NUM = "located_num";
	public static final String CHOOSED_NUM = "choosed_num";
	public static final String PICKED_NUM = "picked_num";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String INSTORAGE_VBILLNO = "instorage_vbillno";

	public String getDest_lpn() {
		return dest_lpn;
	}

	public void setDest_lpn(String dest_lpn) {
		this.dest_lpn = dest_lpn;
	}

	public String getDest_goods_allocation() {
		return dest_goods_allocation;
	}

	public void setDest_goods_allocation(String dest_goods_allocation) {
		this.dest_goods_allocation = dest_goods_allocation;
	}

	public UFDouble getMove_num() {
		return move_num;
	}

	public void setMove_num(UFDouble move_num) {
		this.move_num = move_num;
	}

	public UFDouble getPick_num() {
		return pick_num;
	}

	public void setPick_num(UFDouble pick_num) {
		this.pick_num = pick_num;
	}

	public String getInstorage_vbillno() {
		return instorage_vbillno;
	}

	public void setInstorage_vbillno(String instorage_vbillno) {
		this.instorage_vbillno = instorage_vbillno;
	}

	public java.lang.String getPk_lot_qty() {
		return this.pk_lot_qty;
	}

	public void setPk_lot_qty(java.lang.String value) {
		this.pk_lot_qty = value;
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

	public java.lang.String getLot() {
		return this.lot;
	}

	public void setLot(java.lang.String value) {
		this.lot = value;
	}

	public java.lang.String getPk_goods_allocation() {
		return this.pk_goods_allocation;
	}

	public void setPk_goods_allocation(java.lang.String value) {
		this.pk_goods_allocation = value;
	}

	public java.lang.String getLpn() {
		return this.lpn;
	}

	public void setLpn(java.lang.String value) {
		this.lpn = value;
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

	public org.nw.vo.pub.lang.UFDouble getStock_num() {
		return this.stock_num;
	}

	public void setStock_num(org.nw.vo.pub.lang.UFDouble value) {
		this.stock_num = value;
	}

	public org.nw.vo.pub.lang.UFDouble getAvailable_num() {
		return this.available_num;
	}

	public void setAvailable_num(org.nw.vo.pub.lang.UFDouble value) {
		this.available_num = value;
	}

	public org.nw.vo.pub.lang.UFDouble getLocated_num() {
		return this.located_num;
	}

	public void setLocated_num(org.nw.vo.pub.lang.UFDouble value) {
		this.located_num = value;
	}

	public org.nw.vo.pub.lang.UFDouble getChoosed_num() {
		return choosed_num;
	}

	public void setChoosed_num(org.nw.vo.pub.lang.UFDouble choosed_num) {
		this.choosed_num = choosed_num;
	}

	public org.nw.vo.pub.lang.UFDouble getPicked_num() {
		return this.picked_num;
	}

	public void setPicked_num(org.nw.vo.pub.lang.UFDouble value) {
		this.picked_num = value;
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
		return "pk_lot_qty";
	}

	public String getTableName() {
		return "TS_LOT_QTY";
	}
}
