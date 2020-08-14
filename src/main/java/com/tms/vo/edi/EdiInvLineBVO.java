package com.tms.vo.edi;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class EdiInvLineBVO extends SuperVO {
	
	private String pk_inv_line_b;
	private String pk_invoice;
	private Integer serialno;
	private String pk_address;
	private String pk_province;
	private String pk_city;
	private String pk_area;
	private String detail_addr;
	private String contact;
	private String phone;
	private String mobile;
	private String email;
	private Integer operate_type;
	private UFDateTime req_date_from;
	private UFDateTime req_date_till;
	
	private String pk_goods;
	private String goods_code;
	private String goods_name;
	private String goods_type;
	private Integer num;
	private Integer plan_num;
	private UFDouble plan_pack_num_count;
	private String min_pack;
	private UFDouble pack_num_count;
	
	private String pack;	
	private UFDouble weight;
	private UFDouble volume;
	private UFDouble unit_weight;
	private UFDouble unit_volume;
	
	private UFDouble length;
	private UFDouble width;
	private UFDouble height;
	
	private String trans_note;
	private UFDouble low_temp;
	private UFDouble hight_temp;
	private String reference_no;
	
	private Integer pod_num;
	private Integer reject_num;
	private Integer damage_num;
	private Integer lost_num;
	
	private String memo;
	
	private Integer dr;
	private UFDateTime ts;
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
	
	private org.nw.vo.pub.lang.UFDateTime sync_time;

	private org.nw.vo.pub.lang.UFBoolean syncexp_flag;

	private java.lang.String syncexp_memo;
	
	
	
	public org.nw.vo.pub.lang.UFDateTime getSync_time() {
		return sync_time;
	}

	public void setSync_time(org.nw.vo.pub.lang.UFDateTime sync_time) {
		this.sync_time = sync_time;
	}

	public org.nw.vo.pub.lang.UFBoolean getSyncexp_flag() {
		return syncexp_flag;
	}

	public void setSyncexp_flag(org.nw.vo.pub.lang.UFBoolean syncexp_flag) {
		this.syncexp_flag = syncexp_flag;
	}

	public java.lang.String getSyncexp_memo() {
		return syncexp_memo;
	}

	public void setSyncexp_memo(java.lang.String syncexp_memo) {
		this.syncexp_memo = syncexp_memo;
	}

	public static final String PK_INV_LINE_B = "pk_inv_line_b";
	public static final String PK_INVOICE = "pk_invoice";
	
	public String getPk_inv_line_b() {
		return pk_inv_line_b;
	}

	public void setPk_inv_line_b(String pk_inv_line_b) {
		this.pk_inv_line_b = pk_inv_line_b;
	}

	public String getPk_invoice() {
		return pk_invoice;
	}

	public void setPk_invoice(String pk_invoice) {
		this.pk_invoice = pk_invoice;
	}

	public Integer getSerialno() {
		return serialno;
	}

	public void setSerialno(Integer serialno) {
		this.serialno = serialno;
	}

	public String getPk_address() {
		return pk_address;
	}

	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
	}

	public String getPk_province() {
		return pk_province;
	}

	public void setPk_province(String pk_province) {
		this.pk_province = pk_province;
	}

	public String getPk_city() {
		return pk_city;
	}

	public void setPk_city(String pk_city) {
		this.pk_city = pk_city;
	}

	public String getPk_area() {
		return pk_area;
	}

	public void setPk_area(String pk_area) {
		this.pk_area = pk_area;
	}

	public String getDetail_addr() {
		return detail_addr;
	}

	public void setDetail_addr(String detail_addr) {
		this.detail_addr = detail_addr;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public Integer getOperate_type() {
		return operate_type;
	}

	public void setOperate_type(Integer operate_type) {
		this.operate_type = operate_type;
	}

	public UFDateTime getReq_date_from() {
		return req_date_from;
	}

	public void setReq_date_from(UFDateTime req_date_from) {
		this.req_date_from = req_date_from;
	}

	public UFDateTime getReq_date_till() {
		return req_date_till;
	}

	public void setReq_date_till(UFDateTime req_date_till) {
		this.req_date_till = req_date_till;
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

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
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

	public String getMin_pack() {
		return min_pack;
	}

	public void setMin_pack(String min_pack) {
		this.min_pack = min_pack;
	}

	public UFDouble getPack_num_count() {
		return pack_num_count;
	}

	public void setPack_num_count(UFDouble pack_num_count) {
		this.pack_num_count = pack_num_count;
	}

	public String getPack() {
		return pack;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}

	public UFDouble getWeight() {
		return weight;
	}

	public void setWeight(UFDouble weight) {
		this.weight = weight;
	}

	public UFDouble getVolume() {
		return volume;
	}

	public void setVolume(UFDouble volume) {
		this.volume = volume;
	}

	public UFDouble getUnit_weight() {
		return unit_weight;
	}

	public void setUnit_weight(UFDouble unit_weight) {
		this.unit_weight = unit_weight;
	}

	public UFDouble getUnit_volume() {
		return unit_volume;
	}

	public void setUnit_volume(UFDouble unit_volume) {
		this.unit_volume = unit_volume;
	}

	public UFDouble getLength() {
		return length;
	}

	public void setLength(UFDouble length) {
		this.length = length;
	}

	public UFDouble getWidth() {
		return width;
	}

	public void setWidth(UFDouble width) {
		this.width = width;
	}

	public UFDouble getHeight() {
		return height;
	}

	public void setHeight(UFDouble height) {
		this.height = height;
	}

	public String getTrans_note() {
		return trans_note;
	}

	public void setTrans_note(String trans_note) {
		this.trans_note = trans_note;
	}

	public UFDouble getLow_temp() {
		return low_temp;
	}

	public void setLow_temp(UFDouble low_temp) {
		this.low_temp = low_temp;
	}

	public UFDouble getHight_temp() {
		return hight_temp;
	}

	public void setHight_temp(UFDouble hight_temp) {
		this.hight_temp = hight_temp;
	}

	public String getReference_no() {
		return reference_no;
	}

	public void setReference_no(String reference_no) {
		this.reference_no = reference_no;
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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

	public static String getPkInvLineB() {
		return PK_INV_LINE_B;
	}

	public static String getPkInvoice() {
		return PK_INVOICE;
	}

	public java.lang.String getParentPKFieldName() {
		return "pk_invoice";
	}

	public java.lang.String getPKFieldName() {
		return "pk_inv_line_b";
	}

	public java.lang.String getTableName() {
		return "edi_inv_line_b";
	}
	
	public EdiInvLineBVO() {
		super();
	}
}
