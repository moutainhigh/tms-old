package com.tms.vo.te;

import org.nw.annotation.NotDBField;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 */
@SuppressWarnings("serial")
public class EntOperationBVO extends SuperVO {
	private String pk_entrust;
	private String pk_ent_operation_b;
	private Integer dr;
	private UFDateTime ts;
	
	@NotDBField
	private String pk_expense_type;//费用类型PK字段
	
	private Integer operation_type;
	private Integer valuation_type;
	private UFDouble operation_value;
	private UFDateTime operation_time;
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
	
	public static final String TS_ENT_OPERATION_B = "ts_ent_operation_b";
	public static final String PK_ENTRUST = "pk_entrust";
	public static final String PK_ENT_OPERATION_B = "pk_ent_operation_b";
	
	
	public String getPk_entrust() {
		return pk_entrust;
	}
	public void setPk_entrust(String pk_entrust) {
		this.pk_entrust = pk_entrust;
	}
	public String getPk_ent_operation_b() {
		return pk_ent_operation_b;
	}
	public void setPk_ent_operation_b(String pk_ent_operation_b) {
		this.pk_ent_operation_b = pk_ent_operation_b;
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
	public String getPk_expense_type() {
		return pk_expense_type;
	}
	public void setPk_expense_type(String pk_expense_type) {
		this.pk_expense_type = pk_expense_type;
	}
	public Integer getOperation_type() {
		return operation_type;
	}
	public void setOperation_type(Integer operation_type) {
		this.operation_type = operation_type;
	}
	public Integer getValuation_type() {
		return valuation_type;
	}
	public void setValuation_type(Integer valuation_type) {
		this.valuation_type = valuation_type;
	}
	public UFDouble getOperation_value() {
		return operation_value;
	}
	public void setOperation_value(UFDouble operation_value) {
		this.operation_value = operation_value;
	}
	public UFDateTime getOperation_time() {
		return operation_time;
	}
	public void setOperation_time(UFDateTime operation_time) {
		this.operation_time = operation_time;
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
		return PK_ENTRUST;
	}
	@Override
	public String getPKFieldName() {
		return PK_ENT_OPERATION_B;
	}
	@Override
	public String getTableName() {
		return TS_ENT_OPERATION_B;
	}

	
}
