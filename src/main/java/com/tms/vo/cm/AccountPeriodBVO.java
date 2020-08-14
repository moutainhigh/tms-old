package com.tms.vo.cm;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class AccountPeriodBVO extends SuperVO {
	
	private String pk_account_period_b;
	private String pk_account_period;
	private Integer dr;
	private UFDateTime ts;
	private String period_code_b;
	private String period_year;
	private Integer period_month;
	private UFDateTime start_date;
	private UFDateTime end_date;
	private Integer account_state;
	private String memo;
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

	public static final String PK_ACCOUNT_PERIOD = "pk_account_period";
	public static final String PK_ACCOUNT_PERIOD_B = "pk_account_period_b";
	public static final String TS_ACCOUNT_PERIOD_B = "ts_account_period_b";

	
	public String getPk_account_period_b() {
		return pk_account_period_b;
	}

	public void setPk_account_period_b(String pk_account_period_b) {
		this.pk_account_period_b = pk_account_period_b;
	}

	public String getPk_account_period() {
		return pk_account_period;
	}

	public void setPk_account_period(String pk_account_period) {
		this.pk_account_period = pk_account_period;
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

	public String getPeriod_code_b() {
		return period_code_b;
	}

	public void setPeriod_code_b(String period_code_b) {
		this.period_code_b = period_code_b;
	}

	public String getPeriod_year() {
		return period_year;
	}

	public void setPeriod_year(String period_year) {
		this.period_year = period_year;
	}

	public Integer getPeriod_month() {
		return period_month;
	}

	public void setPeriod_month(Integer period_month) {
		this.period_month = period_month;
	}

	public UFDateTime getStart_date() {
		return start_date;
	}

	public void setStart_date(UFDateTime start_date) {
		this.start_date = start_date;
	}

	public UFDateTime getEnd_date() {
		return end_date;
	}

	public void setEnd_date(UFDateTime end_date) {
		this.end_date = end_date;
	}

	public Integer getAccount_state() {
		return account_state;
	}

	public void setAccount_state(Integer account_state) {
		this.account_state = account_state;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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
		return PK_ACCOUNT_PERIOD;
	}

	@Override
	public String getPKFieldName() {
		return PK_ACCOUNT_PERIOD_B;
	}

	@Override
	public String getTableName() {
		return TS_ACCOUNT_PERIOD_B;
	}

}
