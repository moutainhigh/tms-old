package com.tms.vo.kpi;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class KPIIndexBVO extends SuperVO {
	
	private String pk_kpi_index_b;
	private String pk_kpi_index;
	
	private Integer serialno;
	private Integer code;
	private String exp_type;
	private String exp_reason_type;
	private UFDouble standard_score;
	private UFDouble minimum_score;
	private UFDouble dimension;
	private Integer unit;
	private Integer kpi_each;
	private UFDouble unit_score;
	private UFDouble total_score;
	private String memo;
	private Integer compute_type;
	private Integer dr;
	private UFDateTime ts;
	private UFBoolean in_or_de;
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

	public static final String PK_KPI_INDEX_B = "pk_kpi_index_b";
	public static final String PK_KPI_INDEX = "pk_kpi_index";

	public String getPk_kpi_index_b() {
		return pk_kpi_index_b;
	}

	public void setPk_kpi_index_b(String pk_kpi_index_b) {
		this.pk_kpi_index_b = pk_kpi_index_b;
	}

	public Integer getCompute_type() {
		return compute_type;
	}

	public void setCompute_type(Integer compute_type) {
		this.compute_type = compute_type;
	}

	public String getPk_kpi_index() {
		return pk_kpi_index;
	}

	public Integer getUnit() {
		return unit;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	public Integer getKpi_each() {
		return kpi_each;
	}

	public void setKpi_each(Integer kpi_each) {
		this.kpi_each = kpi_each;
	}

	public void setPk_kpi_index(String pk_kpi_index) {
		this.pk_kpi_index = pk_kpi_index;
	}

	public Integer getSerialno() {
		return serialno;
	}

	public void setSerialno(Integer serialno) {
		this.serialno = serialno;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getExp_type() {
		return exp_type;
	}

	public void setExp_type(String exp_type) {
		this.exp_type = exp_type;
	}

	public String getExp_reason_type() {
		return exp_reason_type;
	}

	public void setExp_reason_type(String exp_reason_type) {
		this.exp_reason_type = exp_reason_type;
	}

	public UFDouble getStandard_score() {
		return standard_score;
	}

	public void setStandard_score(UFDouble standard_score) {
		this.standard_score = standard_score;
	}

	public UFDouble getMinimum_score() {
		return minimum_score;
	}

	public void setMinimum_score(UFDouble minimum_score) {
		this.minimum_score = minimum_score;
	}

	public UFDouble getDimension() {
		return dimension;
	}

	public void setDimension(UFDouble dimension) {
		this.dimension = dimension;
	}

	public UFDouble getUnit_score() {
		return unit_score;
	}

	public void setUnit_score(UFDouble unit_score) {
		this.unit_score = unit_score;
	}

	public UFDouble getTotal_score() {
		return total_score;
	}

	public void setTotal_score(UFDouble total_score) {
		this.total_score = total_score;
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

	public UFBoolean getIn_or_de() {
		return in_or_de;
	}

	public void setIn_or_de(UFBoolean in_or_de) {
		this.in_or_de = in_or_de;
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

	@Override
	public String getParentPKFieldName() {
		return PK_KPI_INDEX;
	}

	@Override
	public String getPKFieldName() {
		return PK_KPI_INDEX_B;
	}

	@Override
	public String getTableName() {
		return "ts_kpi_index_b";
	}

}
