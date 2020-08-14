package com.tms.vo.kpi;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class KPIIndexVO extends SuperVO {
	
	private String pk_kpi_index;
	private String code;
	private String name;
	private Integer index_type;
	private String memo;
	private Integer dr;
	private UFDateTime ts;
	private UFBoolean locked_flag;
	private String kpi_year;
	private UFDateTime start_time;
	private UFDateTime end_time;
	private Integer score;
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

	public static final String PK_KPI_INDEX = "pk_kpi_index";
	

	public Integer getIndex_type() {
		return index_type;
	}

	public void setIndex_type(Integer index_type) {
		this.index_type = index_type;
	}

	public String getPk_kpi_index() {
		return pk_kpi_index;
	}

	public void setPk_kpi_index(String pk_kpi_index) {
		this.pk_kpi_index = pk_kpi_index;
	}

	public String getCode() {
		return code;
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

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
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

	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	public void setLocked_flag(UFBoolean locked_flag) {
		this.locked_flag = locked_flag;
	}

	public String getKpi_year() {
		return kpi_year;
	}

	public void setKpi_year(String kpi_year) {
		this.kpi_year = kpi_year;
	}

	public UFDateTime getStart_time() {
		return start_time;
	}

	public void setStart_time(UFDateTime start_time) {
		this.start_time = start_time;
	}

	public UFDateTime getEnd_time() {
		return end_time;
	}

	public void setEnd_time(UFDateTime end_time) {
		this.end_time = end_time;
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
		return PK_KPI_INDEX;
	}

	@Override
	public String getTableName() {
		return "ts_kpi_index";
	}

}
