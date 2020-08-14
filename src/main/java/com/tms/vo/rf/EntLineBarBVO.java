package com.tms.vo.rf;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class EntLineBarBVO extends SuperVO {
	
	private String pk_ent_line_bar_b;
	private String pk_ent_line_b;
	private String pk_entrust;
	private String pk_address;
	private String lot;
	
	private String pk_goods;
	private String goods_code;
	private String goods_name;
	private String bar_code;
	
	private UFBoolean exp_flag;
	
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
	
	public static final String PK_ENT_LINE_BAR_B = "pk_ent_line_bar_b";
	public static final String PK_ENT_LINE_B = "pk_ent_line_b";
	public static final String PK_ENTRUST = "pk_entrust";
	
	public String getPk_address() {
		return pk_address;
	}

	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
	}

	public String getPk_ent_line_bar_b() {
		return pk_ent_line_bar_b;
	}

	public void setPk_ent_line_bar_b(String pk_ent_line_bar_b) {
		this.pk_ent_line_bar_b = pk_ent_line_bar_b;
	}

	public String getPk_ent_line_b() {
		return pk_ent_line_b;
	}

	public void setPk_ent_line_b(String pk_ent_line_b) {
		this.pk_ent_line_b = pk_ent_line_b;
	}

	public String getPk_entrust() {
		return pk_entrust;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public void setPk_entrust(String pk_entrust) {
		this.pk_entrust = pk_entrust;
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

	public String getBar_code() {
		return bar_code;
	}

	public void setBar_code(String bar_code) {
		this.bar_code = bar_code;
	}

	public UFBoolean getExp_flag() {
		return exp_flag;
	}

	public void setExp_flag(UFBoolean exp_flag) {
		this.exp_flag = exp_flag;
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

	@Override
	public String getParentPKFieldName() {
		return "pk_ent_line_b";
	}

	@Override
	public String getPKFieldName() {
		return "pk_ent_line_bar_b";
	}

	@Override
	public String getTableName() {
		return "ts_ent_line_bar_b";
	}

}
