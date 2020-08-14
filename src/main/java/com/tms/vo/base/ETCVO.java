package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ETCVO extends SuperVO {
	
	private String pk_etc;
	private String etc_code;
	private String etc_name;
	private String carno;
	private String pk_driver;
	private String buyer;
	private String buy_addr;
	private String memo;
	private UFDouble amount;
	private UFDateTime buy_date;
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
	
	public static final String PK_ETC = "pk_etc";
	public static final String ETC_CODE = "etc_code";
	public static final String ETC_NAME = "etc_name";
	public static final String TS_ETC = "ts_etc";
	

	public String getBuyer() {
		return buyer;
	}

	public void setBuyer(String buyer) {
		this.buyer = buyer;
	}

	public String getBuy_addr() {
		return buy_addr;
	}

	public void setBuy_addr(String buy_addr) {
		this.buy_addr = buy_addr;
	}

	public UFDateTime getBuy_date() {
		return buy_date;
	}

	public void setBuy_date(UFDateTime buy_date) {
		this.buy_date = buy_date;
	}

	public String getPk_etc() {
		return pk_etc;
	}

	public void setPk_etc(String pk_etc) {
		this.pk_etc = pk_etc;
	}

	public String getEtc_code() {
		return etc_code;
	}

	public void setEtc_code(String etc_code) {
		this.etc_code = etc_code;
	}

	public String getEtc_name() {
		return etc_name;
	}

	public void setEtc_name(String etc_name) {
		this.etc_name = etc_name;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getPk_driver() {
		return pk_driver;
	}

	public void setPk_driver(String pk_driver) {
		this.pk_driver = pk_driver;
	}

	public UFDouble getAmount() {
		return amount;
	}

	public void setAmount(UFDouble amount) {
		this.amount = amount;
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

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	public void setLocked_flag(UFBoolean locked_flag) {
		this.locked_flag = locked_flag;
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

	public ETCVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return PK_ETC;
	}

	@Override
	public String getTableName() {
		return TS_ETC;
	}

}
