package com.tms.vo.wh;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;

/**
 * TS_TRANSACTION
 * 
 * @version 1.0
 * @since 1.0
 */
public class TransactionVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_transaction;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String relateid;

	private java.lang.String vbillno;

	private java.lang.String trans_type;

	private java.lang.String pk_customer;

	private java.lang.String pk_goods;

	private java.lang.String lot;

	private java.lang.String from_loc;

	private java.lang.String from_lpn;

	private java.lang.String to_loc;

	private java.lang.String to_lpn;

	private java.lang.Integer order_type;

	private java.lang.String vstatus;

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

	private org.nw.vo.pub.lang.UFDate receipt_date;

	private org.nw.vo.pub.lang.UFDate ship_date;

	private UFDouble quantity;

	private java.lang.String pack;

	private java.lang.String min_pack;

	private org.nw.vo.pub.lang.UFDouble unit_weight;

	private org.nw.vo.pub.lang.UFDouble unit_volume;

	private org.nw.vo.pub.lang.UFDouble length;

	private org.nw.vo.pub.lang.UFDouble width;

	private org.nw.vo.pub.lang.UFDouble height;

	private org.nw.vo.pub.lang.UFDate trans_date;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

	private org.nw.vo.pub.lang.UFDateTime edi_time;
	private UFDate dbilldate;

	private String relate_vbillno;
	private String relate_b_vbillno;

	private String pk_supplier;

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

	public static final String PK_TRANSACTION = "pk_transaction";
	public static final String PK_SUPPLIER = "pk_supplier";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String RELATEID = "relateid";
	public static final String VBILLNO = "vbillno";
	public static final String TRANS_TYPE = "trans_type";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String PK_GOODS = "pk_goods";
	public static final String LOT = "lot";
	public static final String FROM_LOC = "from_loc";
	public static final String FROM_LPN = "from_lpn";
	public static final String TO_LOC = "to_loc";
	public static final String TO_LPN = "to_lpn";
	public static final String ORDERNO = "orderno";
	public static final String ORDER_TYPE = "order_type";
	public static final String VSTATUS = "vstatus";
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
	public static final String RECEIPT_DATE = "receipt_date";
	public static final String SHIP_DATE = "ship_date";
	public static final String QUANTITY = "quantity";
	public static final String PACK = "pack";
	public static final String MIN_PACK = "min_pack";
	public static final String UNIT_WEIGHT = "unit_weight";
	public static final String UNIT_VOLUME = "unit_volume";
	public static final String LENGTH = "length";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String TRANS_DATE = "trans_date";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String EDI_TIME = "edi_time";
	public static final String RELATE_VBILLNO = "relate_vbillno";
	public static final String RELATE_B_VBILLNO = "relate_b_vbillno";
	public static final String DBILLDATE = "dbilldate";

	public UFDate getDbilldate() {
		return dbilldate;
	}

	public void setDbilldate(UFDate dbilldate) {
		this.dbilldate = dbilldate;
	}

	public java.lang.String getPk_transaction() {
		return this.pk_transaction;
	}

	public void setPk_transaction(java.lang.String value) {
		this.pk_transaction = value;
	}

	public java.lang.String getRelateid() {
		return relateid;
	}

	public void setRelateid(java.lang.String relateid) {
		this.relateid = relateid;
	}

	public String getPk_supplier() {
		return pk_supplier;
	}

	public void setPk_supplier(String pk_supplier) {
		this.pk_supplier = pk_supplier;
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

	public java.lang.String getVbillno() {
		return this.vbillno;
	}

	public void setVbillno(java.lang.String value) {
		this.vbillno = value;
	}

	public java.lang.String getTrans_type() {
		return trans_type;
	}

	public void setTrans_type(java.lang.String trans_type) {
		this.trans_type = trans_type;
	}

	public java.lang.String getVstatus() {
		return vstatus;
	}

	public void setVstatus(java.lang.String vstatus) {
		this.vstatus = vstatus;
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

	public java.lang.String getLot() {
		return this.lot;
	}

	public void setLot(java.lang.String value) {
		this.lot = value;
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

	public java.lang.String getTo_lpn() {
		return this.to_lpn;
	}

	public void setTo_lpn(java.lang.String value) {
		this.to_lpn = value;
	}

	public java.lang.Integer getOrder_type() {
		return order_type;
	}

	public void setOrder_type(java.lang.Integer order_type) {
		this.order_type = order_type;
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

	public org.nw.vo.pub.lang.UFDate getReceipt_date() {
		return this.receipt_date;
	}

	public void setReceipt_date(org.nw.vo.pub.lang.UFDate value) {
		this.receipt_date = value;
	}

	public org.nw.vo.pub.lang.UFDate getShip_date() {
		return this.ship_date;
	}

	public void setShip_date(org.nw.vo.pub.lang.UFDate value) {
		this.ship_date = value;
	}

	public UFDouble getQuantity() {
		return quantity;
	}

	public void setQuantity(UFDouble quantity) {
		this.quantity = quantity;
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

	public org.nw.vo.pub.lang.UFDate getTrans_date() {
		return this.trans_date;
	}

	public void setTrans_date(org.nw.vo.pub.lang.UFDate value) {
		this.trans_date = value;
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

	public org.nw.vo.pub.lang.UFDateTime getEdi_time() {
		return this.edi_time;
	}

	public void setEdi_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.edi_time = value;
	}

	public String getRelate_vbillno() {
		return relate_vbillno;
	}

	public void setRelate_vbillno(String relate_vbillno) {
		this.relate_vbillno = relate_vbillno;
	}

	public String getRelate_b_vbillno() {
		return relate_b_vbillno;
	}

	public void setRelate_b_vbillno(String relate_b_vbillno) {
		this.relate_b_vbillno = relate_b_vbillno;
	}

	public String getParentPKFieldName() {
		return "pk_instorage";
	}

	public String getPKFieldName() {
		return "pk_transaction";
	}

	public String getTableName() {
		return "TS_TRANSACTION";
	}
}
