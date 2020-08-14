package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class ExrateBVO extends SuperVO {
	private String pk_exrate_b;
	private String pk_exrate;
	private Integer dr;
	private UFDateTime ts;
	private String exrate_month;
	private UFDouble rate;
	private String memo;
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

	public static final String PK_EXTRATE_B = "pk_exrate_b";
	public static final String PK_EXTRATE = "pk_exrate";
	public static final String TS_EXTRATE_B = "ts_exrate_b";
	

	
	public String getPk_exrate_b() {
		return pk_exrate_b;
	}
	public void setPk_exrate_b(String pk_exrate_b) {
		this.pk_exrate_b = pk_exrate_b;
	}
	public String getPk_exrate() {
		return pk_exrate;
	}
	public void setPk_exrate(String pk_exrate) {
		this.pk_exrate = pk_exrate;
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
	public String getExrate_month() {
		return exrate_month;
	}
	public void setExrate_month(String exrate_month) {
		this.exrate_month = exrate_month;
	}
	public UFDouble getRate() {
		return rate;
	}
	public void setRate(UFDouble rate) {
		this.rate = rate;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
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
		return PK_EXTRATE;
	}
	@Override
	public String getPKFieldName() {
		return PK_EXTRATE_B;
	}
	@Override
	public String getTableName() {
		return TS_EXTRATE_B;
	}

	
}
