package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class MergeRuleBVO extends SuperVO {
	private String pk_merge_rule_b;
	private String pk_merge_rule;
	
	private Integer serialno;
	private Integer matter_type1;
	private String matter1;
	private String matter1_code;
	private Integer matter_type2;
	private String matter2;
	private String matter2_code;
	
	private Integer dr;
	private UFDateTime ts;
	
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
	
	private String memo;
	
	
	
	
	public Integer getSerialno() {
		return serialno;
	}

	public void setSerialno(Integer serialno) {
		this.serialno = serialno;
	}

	public String getMatter1_code() {
		return matter1_code;
	}

	public void setMatter1_code(String matter1_code) {
		this.matter1_code = matter1_code;
	}

	public String getMatter2_code() {
		return matter2_code;
	}

	public void setMatter2_code(String matter2_code) {
		this.matter2_code = matter2_code;
	}

	public String getPk_merge_rule_b() {
		return pk_merge_rule_b;
	}

	public void setPk_merge_rule_b(String pk_merge_rule_b) {
		this.pk_merge_rule_b = pk_merge_rule_b;
	}

	public String getPk_merge_rule() {
		return pk_merge_rule;
	}

	public void setPk_merge_rule(String pk_merge_rule) {
		this.pk_merge_rule = pk_merge_rule;
	}

	public Integer getMatter_type1() {
		return matter_type1;
	}

	public void setMatter_type1(Integer matter_type1) {
		this.matter_type1 = matter_type1;
	}

	public String getMatter1() {
		return matter1;
	}

	public void setMatter1(String matter1) {
		this.matter1 = matter1;
	}

	public Integer getMatter_type2() {
		return matter_type2;
	}

	public void setMatter_type2(Integer matter_type2) {
		this.matter_type2 = matter_type2;
	}

	public String getMatter2() {
		return matter2;
	}

	public void setMatter2(String matter2) {
		this.matter2 = matter2;
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public static final String PK_MERGE_RULE = "pk_merge_rule";
	public static final String PK_MERGE_RULE_B = "pk_merge_rule_b";
	
	
	public String getParentPKFieldName() {
		return PK_MERGE_RULE;
	}
	
	public String getPKFieldName() {
		return PK_MERGE_RULE_B;
	}
	
	public String getTableName() {
		return "ts_merge_rule_b";
	}

	public MergeRuleBVO() {
		super();
	}
}
