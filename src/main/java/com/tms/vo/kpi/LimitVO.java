package com.tms.vo.kpi;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class LimitVO extends SuperVO {
	
	private String pk_limit;
	private Integer limit_type;
	private Integer matter;
	private String code;
	private String name;
	private String pk_address;
	private String pk_carrier;
	private String pk_customer;
	private String item_code;
	private UFBoolean if_urgent;
	private String goods_type;
	private Integer exp_type;
	private UFDouble exp_time;
	private UFDouble warn_time;
	
	private Integer dr;
	private String ts;
	private UFBoolean locked_flag;
	private String def1;
	private String def2;
	private String def4;
	private String def3;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private String def9;
	private String def10;
	private UFDouble def11;
	private UFDouble def12;
	
	private String pk_corp;
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	
	public static final String PK_LIMIT = "pk_limit";
	
	public String getCode() {
		return code;
	}

	public Integer getExp_type() {
		return exp_type;
	}

	public void setExp_type(Integer exp_type) {
		this.exp_type = exp_type;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	public void setLocked_flag(UFBoolean locked_flag) {
		this.locked_flag = locked_flag;
	}

	public String getPk_limit() {
		return pk_limit;
	}

	public void setPk_limit(String pk_limit) {
		this.pk_limit = pk_limit;
	}


	public Integer getLimit_type() {
		return limit_type;
	}

	public void setLimit_type(Integer limit_type) {
		this.limit_type = limit_type;
	}

	public Integer getMatter() {
		return matter;
	}

	public void setMatter(Integer matter) {
		this.matter = matter;
	}

	public String getPk_address() {
		return pk_address;
	}

	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
	}

	public String getPk_carrier() {
		return pk_carrier;
	}

	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public String getItem_code() {
		return item_code;
	}

	public void setItem_code(String item_code) {
		this.item_code = item_code;
	}

	public UFBoolean getIf_urgent() {
		return if_urgent;
	}

	public void setIf_urgent(UFBoolean if_urgent) {
		this.if_urgent = if_urgent;
	}

	public String getGoods_type() {
		return goods_type;
	}

	public void setGoods_type(String goods_type) {
		this.goods_type = goods_type;
	}

	public UFDouble getExp_time() {
		return exp_time;
	}

	public void setExp_time(UFDouble exp_time) {
		this.exp_time = exp_time;
	}

	public UFDouble getWarn_time() {
		return warn_time;
	}

	public void setWarn_time(UFDouble warn_time) {
		this.warn_time = warn_time;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
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

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public UFDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
	}

	public String getCreate_user() {
		return create_user;
	}

	public void setCreate_user(String create_user) {
		this.create_user = create_user;
	}

	public UFDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(UFDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return PK_LIMIT;
	}

	@Override
	public String getTableName() {
		return "ts_limit";
	}

}
