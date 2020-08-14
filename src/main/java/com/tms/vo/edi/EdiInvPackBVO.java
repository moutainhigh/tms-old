package com.tms.vo.edi;

/**
 * edi_inv_pack_b
 * 
 * @version 1.0
 * @since 1.0
 */
public class EdiInvPackBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_inv_pack_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.Integer serialno;

	private java.lang.String pk_invoice;

	private java.lang.String pk_goods;

	private java.lang.String goods_code;

	private java.lang.String goods_name;

	private java.lang.String goods_type;

	private java.lang.Integer plan_num;

	private java.lang.Integer num;

	private java.lang.String pack;

	private java.lang.String min_pack;

	private org.nw.vo.pub.lang.UFDouble plan_pack_num_count;

	private org.nw.vo.pub.lang.UFDouble pack_num_count;

	private org.nw.vo.pub.lang.UFDouble weight;

	private org.nw.vo.pub.lang.UFDouble volume;

	private org.nw.vo.pub.lang.UFDouble unit_weight;

	private org.nw.vo.pub.lang.UFDouble unit_volume;

	private org.nw.vo.pub.lang.UFDouble length;

	private org.nw.vo.pub.lang.UFDouble width;

	private org.nw.vo.pub.lang.UFDouble height;

	private java.lang.String trans_note;

	private org.nw.vo.pub.lang.UFDouble low_temp;

	private org.nw.vo.pub.lang.UFDouble hight_temp;

	private java.lang.String reference_no;

	private java.lang.String memo;

	private java.lang.Integer pod_num;

	private java.lang.Integer reject_num;

	private java.lang.Integer damage_num;

	private java.lang.Integer lost_num;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;

	private java.lang.String def6;

	private java.lang.String def7;

	private java.lang.String def8;

	private java.lang.String def9;

	private java.lang.String def10;

	private org.nw.vo.pub.lang.UFDouble def11;

	private org.nw.vo.pub.lang.UFDouble def12;

	private org.nw.vo.pub.lang.UFDateTime sync_time;

	private org.nw.vo.pub.lang.UFBoolean syncexp_flag;

	private java.lang.String syncexp_memo;

	public static final String PK_INV_PACK_B = "pk_inv_pack_b";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String SERIALNO = "serialno";
	public static final String PK_INVOICE = "pk_invoice";
	public static final String PK_GOODS = "pk_goods";
	public static final String GOODS_CODE = "goods_code";
	public static final String GOODS_NAME = "goods_name";
	public static final String GOODS_TYPE = "goods_type";
	public static final String PLAN_NUM = "plan_num";
	public static final String NUM = "num";
	public static final String PACK = "pack";
	public static final String MIN_PACK = "min_pack";
	public static final String PLAN_PACK_NUM_COUNT = "plan_pack_num_count";
	public static final String PACK_NUM_COUNT = "pack_num_count";
	public static final String WEIGHT = "weight";
	public static final String VOLUME = "volume";
	public static final String UNIT_WEIGHT = "unit_weight";
	public static final String UNIT_VOLUME = "unit_volume";
	public static final String LENGTH = "length";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String TRANS_NOTE = "trans_note";
	public static final String LOW_TEMP = "low_temp";
	public static final String HIGHT_TEMP = "hight_temp";
	public static final String REFERENCE_NO = "reference_no";
	public static final String MEMO = "memo";
	public static final String POD_NUM = "pod_num";
	public static final String REJECT_NUM = "reject_num";
	public static final String DAMAGE_NUM = "damage_num";
	public static final String LOST_NUM = "lost_num";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String DEF6 = "def6";
	public static final String DEF7 = "def7";
	public static final String DEF8 = "def8";
	public static final String DEF9 = "def9";
	public static final String DEF10 = "def10";
	public static final String DEF11 = "def11";
	public static final String DEF12 = "def12";
	public static final String SYNC_TIME = "sync_time";
	public static final String SYNCEXP_FLAG = "syncexp_flag";
	public static final String SYNCEXP_MEMO = "syncexp_memo";

	public java.lang.String getPk_inv_pack_b() {
		return this.pk_inv_pack_b;
	}

	public void setPk_inv_pack_b(java.lang.String value) {
		this.pk_inv_pack_b = value;
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

	public java.lang.Integer getSerialno() {
		return this.serialno;
	}

	public void setSerialno(java.lang.Integer value) {
		this.serialno = value;
	}

	public java.lang.String getPk_invoice() {
		return this.pk_invoice;
	}

	public void setPk_invoice(java.lang.String value) {
		this.pk_invoice = value;
	}

	public java.lang.String getPk_goods() {
		return this.pk_goods;
	}

	public void setPk_goods(java.lang.String value) {
		this.pk_goods = value;
	}

	public java.lang.String getGoods_code() {
		return this.goods_code;
	}

	public void setGoods_code(java.lang.String value) {
		this.goods_code = value;
	}

	public java.lang.String getGoods_name() {
		return this.goods_name;
	}

	public void setGoods_name(java.lang.String value) {
		this.goods_name = value;
	}

	public java.lang.String getGoods_type() {
		return this.goods_type;
	}

	public void setGoods_type(java.lang.String value) {
		this.goods_type = value;
	}

	public java.lang.Integer getPlan_num() {
		return this.plan_num;
	}

	public void setPlan_num(java.lang.Integer value) {
		this.plan_num = value;
	}

	public java.lang.Integer getNum() {
		return this.num;
	}

	public void setNum(java.lang.Integer value) {
		this.num = value;
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

	public org.nw.vo.pub.lang.UFDouble getPlan_pack_num_count() {
		return plan_pack_num_count;
	}

	public void setPlan_pack_num_count(org.nw.vo.pub.lang.UFDouble plan_pack_num_count) {
		this.plan_pack_num_count = plan_pack_num_count;
	}

	public org.nw.vo.pub.lang.UFDouble getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(org.nw.vo.pub.lang.UFDouble pack_num_count) {
		this.pack_num_count = pack_num_count;
	}

	public org.nw.vo.pub.lang.UFDouble getWeight() {
		return this.weight;
	}

	public void setWeight(org.nw.vo.pub.lang.UFDouble value) {
		this.weight = value;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume() {
		return this.volume;
	}

	public void setVolume(org.nw.vo.pub.lang.UFDouble value) {
		this.volume = value;
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

	public java.lang.String getTrans_note() {
		return this.trans_note;
	}

	public void setTrans_note(java.lang.String value) {
		this.trans_note = value;
	}

	public org.nw.vo.pub.lang.UFDouble getLow_temp() {
		return this.low_temp;
	}

	public void setLow_temp(org.nw.vo.pub.lang.UFDouble value) {
		this.low_temp = value;
	}

	public org.nw.vo.pub.lang.UFDouble getHight_temp() {
		return this.hight_temp;
	}

	public void setHight_temp(org.nw.vo.pub.lang.UFDouble value) {
		this.hight_temp = value;
	}

	public java.lang.String getReference_no() {
		return this.reference_no;
	}

	public void setReference_no(java.lang.String value) {
		this.reference_no = value;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
	}

	public java.lang.Integer getPod_num() {
		return this.pod_num;
	}

	public void setPod_num(java.lang.Integer value) {
		this.pod_num = value;
	}

	public java.lang.Integer getReject_num() {
		return this.reject_num;
	}

	public void setReject_num(java.lang.Integer value) {
		this.reject_num = value;
	}

	public java.lang.Integer getDamage_num() {
		return this.damage_num;
	}

	public void setDamage_num(java.lang.Integer value) {
		this.damage_num = value;
	}

	public java.lang.Integer getLost_num() {
		return this.lost_num;
	}

	public void setLost_num(java.lang.Integer value) {
		this.lost_num = value;
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

	public java.lang.String getDef6() {
		return this.def6;
	}

	public void setDef6(java.lang.String value) {
		this.def6 = value;
	}

	public java.lang.String getDef7() {
		return this.def7;
	}

	public void setDef7(java.lang.String value) {
		this.def7 = value;
	}

	public java.lang.String getDef8() {
		return this.def8;
	}

	public void setDef8(java.lang.String value) {
		this.def8 = value;
	}

	public java.lang.String getDef9() {
		return this.def9;
	}

	public void setDef9(java.lang.String value) {
		this.def9 = value;
	}

	public java.lang.String getDef10() {
		return this.def10;
	}

	public void setDef10(java.lang.String value) {
		this.def10 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef11() {
		return this.def11;
	}

	public void setDef11(org.nw.vo.pub.lang.UFDouble value) {
		this.def11 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef12() {
		return this.def12;
	}

	public void setDef12(org.nw.vo.pub.lang.UFDouble value) {
		this.def12 = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getSync_time() {
		return this.sync_time;
	}

	public void setSync_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.sync_time = value;
	}

	public org.nw.vo.pub.lang.UFBoolean getSyncexp_flag() {
		return this.syncexp_flag;
	}

	public void setSyncexp_flag(org.nw.vo.pub.lang.UFBoolean value) {
		this.syncexp_flag = value;
	}

	public java.lang.String getSyncexp_memo() {
		return this.syncexp_memo;
	}

	public void setSyncexp_memo(java.lang.String value) {
		this.syncexp_memo = value;
	}

	public String getParentPKFieldName() {
		return "pk_invoice";
	}

	public String getPKFieldName() {
		return "pk_inv_pack_b";
	}

	public String getTableName() {
		return "edi_inv_pack_b";
	}
}
