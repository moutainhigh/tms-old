package com.tms.vo.inv;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class InvPackRevBVO extends SuperVO {

	private String pk_inv_pack_rev_b;
	private String pk_inv_pack_b;
	private String pk_invoice;
	private Integer dr;
	private UFDateTime ts;
	
	private Integer revb_serialno;
	private Integer packb_serialno;

	private Integer revise_status;
	private Integer revise_type;
	
	private String revise_user;
	private UFDateTime revise_time;
	private String confirm_user;
	private UFDateTime confirm_date;
	private String unconfirm_reason;
	private String memo;
	
	private UFDouble volume;
	private UFDouble hight_temp;
	private UFDouble width;
	private UFDouble length;
	private String pk_goods;
	private String goods_code;
	private String goods_name;
	private String goods_type;
	private UFDouble unit_weight;
	private String reference_no;
	private Integer num;
	private UFDouble height;
	private String pack;
	private UFDouble unit_volume;
	private UFDouble low_temp;
	private UFDouble weight;
	private String trans_note;

	private Integer pod_num;
	private Integer reject_num;
	private Integer damage_num;
	private Integer lost_num;
	private Integer plan_num;
	private UFDouble plan_pack_num_count;
	

	private String def1;
	private String def2;
	private String def3;
	private String def4;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private String def9;
	private String def10;
	private UFDouble def11;
	private UFDouble def12;
	
	

	public String getPk_inv_pack_rev_b() {
		return pk_inv_pack_rev_b;
	}
	public void setPk_inv_pack_rev_b(String pk_inv_pack_rev_b) {
		this.pk_inv_pack_rev_b = pk_inv_pack_rev_b;
	}
	public String getPk_inv_pack_b() {
		return pk_inv_pack_b;
	}
	public void setPk_inv_pack_b(String pk_inv_pack_b) {
		this.pk_inv_pack_b = pk_inv_pack_b;
	}
	public String getPk_invoice() {
		return pk_invoice;
	}
	public void setPk_invoice(String pk_invoice) {
		this.pk_invoice = pk_invoice;
	}
	public Integer getDr() {
		return dr;
	}
	public void setDr(Integer dr) {
		this.dr = dr;
	}
	public UFDateTime getTs() {
		return ts;
	}
	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}
	public Integer getRevb_serialno() {
		return revb_serialno;
	}
	public void setRevb_serialno(Integer revb_serialno) {
		this.revb_serialno = revb_serialno;
	}
	public Integer getPackb_serialno() {
		return packb_serialno;
	}
	public void setPackb_serialno(Integer packb_serialno) {
		this.packb_serialno = packb_serialno;
	}
	public Integer getRevise_status() {
		return revise_status;
	}
	public void setRevise_status(Integer revise_status) {
		this.revise_status = revise_status;
	}
	public Integer getRevise_type() {
		return revise_type;
	}
	public void setRevise_type(Integer revise_type) {
		this.revise_type = revise_type;
	}
	public String getRevise_user() {
		return revise_user;
	}
	public void setRevise_user(String revise_user) {
		this.revise_user = revise_user;
	}
	public UFDateTime getRevise_time() {
		return revise_time;
	}
	public void setRevise_time(UFDateTime revise_time) {
		this.revise_time = revise_time;
	}
	public String getConfirm_user() {
		return confirm_user;
	}
	public void setConfirm_user(String confirm_user) {
		this.confirm_user = confirm_user;
	}
	public UFDateTime getConfirm_date() {
		return confirm_date;
	}
	public void setConfirm_date(UFDateTime confirm_date) {
		this.confirm_date = confirm_date;
	}
	public String getUnconfirm_reason() {
		return unconfirm_reason;
	}
	public void setUnconfirm_reason(String unconfirm_reason) {
		this.unconfirm_reason = unconfirm_reason;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public UFDouble getVolume() {
		return volume;
	}
	public void setVolume(UFDouble volume) {
		this.volume = volume;
	}
	public UFDouble getHight_temp() {
		return hight_temp;
	}
	public void setHight_temp(UFDouble hight_temp) {
		this.hight_temp = hight_temp;
	}
	public UFDouble getWidth() {
		return width;
	}
	public void setWidth(UFDouble width) {
		this.width = width;
	}
	public UFDouble getLength() {
		return length;
	}
	public void setLength(UFDouble length) {
		this.length = length;
	}
	public String getPk_goods() {
		return pk_goods;
	}
	public void setPk_goods(String pk_goods) {
		this.pk_goods = pk_goods;
	}
	public String getGoods_code() {
		return goods_code;
	}
	public void setGoods_code(String goods_code) {
		this.goods_code = goods_code;
	}
	public String getGoods_name() {
		return goods_name;
	}
	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}
	public String getGoods_type() {
		return goods_type;
	}
	public void setGoods_type(String goods_type) {
		this.goods_type = goods_type;
	}
	public UFDouble getUnit_weight() {
		return unit_weight;
	}
	public void setUnit_weight(UFDouble unit_weight) {
		this.unit_weight = unit_weight;
	}
	public String getReference_no() {
		return reference_no;
	}
	public void setReference_no(String reference_no) {
		this.reference_no = reference_no;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
	public UFDouble getHeight() {
		return height;
	}
	public void setHeight(UFDouble height) {
		this.height = height;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public UFDouble getUnit_volume() {
		return unit_volume;
	}
	public void setUnit_volume(UFDouble unit_volume) {
		this.unit_volume = unit_volume;
	}
	public UFDouble getLow_temp() {
		return low_temp;
	}
	public void setLow_temp(UFDouble low_temp) {
		this.low_temp = low_temp;
	}
	public UFDouble getWeight() {
		return weight;
	}
	public void setWeight(UFDouble weight) {
		this.weight = weight;
	}
	public String getTrans_note() {
		return trans_note;
	}
	public void setTrans_note(String trans_note) {
		this.trans_note = trans_note;
	}
	public Integer getPod_num() {
		return pod_num;
	}
	public void setPod_num(Integer pod_num) {
		this.pod_num = pod_num;
	}
	public Integer getReject_num() {
		return reject_num;
	}
	public void setReject_num(Integer reject_num) {
		this.reject_num = reject_num;
	}
	public Integer getDamage_num() {
		return damage_num;
	}
	public void setDamage_num(Integer damage_num) {
		this.damage_num = damage_num;
	}
	public Integer getLost_num() {
		return lost_num;
	}
	public void setLost_num(Integer lost_num) {
		this.lost_num = lost_num;
	}
	public Integer getPlan_num() {
		return plan_num;
	}
	public void setPlan_num(Integer plan_num) {
		this.plan_num = plan_num;
	}
	public UFDouble getPlan_pack_num_count() {
		return plan_pack_num_count;
	}
	public void setPlan_pack_num_count(UFDouble plan_pack_num_count) {
		this.plan_pack_num_count = plan_pack_num_count;
	}
	public String getDef1() {
		return def1;
	}
	public void setDef1(String def1) {
		this.def1 = def1;
	}
	public String getDef2() {
		return def2;
	}
	public void setDef2(String def2) {
		this.def2 = def2;
	}
	public String getDef3() {
		return def3;
	}
	public void setDef3(String def3) {
		this.def3 = def3;
	}
	public String getDef4() {
		return def4;
	}
	public void setDef4(String def4) {
		this.def4 = def4;
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
	public String getDef9() {
		return def9;
	}
	public void setDef9(String def9) {
		this.def9 = def9;
	}
	public String getDef10() {
		return def10;
	}
	public void setDef10(String def10) {
		this.def10 = def10;
	}
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
	public static final String PK_INV_PACK_REV_B = "pk_inv_pack_rev_b";
	public static final String PK_INV_PACK_B = "pk_inv_pack_b";
	public static final String PK_INVOICE = "pk_invoice";
	public static final String TS_INV_PACK_REV_B = "ts_inv_pack_rev_b";
	
	

	@Override
	public String getParentPKFieldName() {
		return PK_INVOICE;
	}
	@Override
	public String getPKFieldName() {
		return PK_INV_PACK_REV_B;
	}
	@Override
	public String getTableName() {
		return TS_INV_PACK_REV_B;
	}

	
}
