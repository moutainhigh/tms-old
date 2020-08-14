package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class CustOpProjectBVO extends SuperVO {
	
	private String pk_cust_op_project_b;
	private String pk_customer;
	private String pk_op_project;
	private Integer dr;
	private String ts;
	
	private UFBoolean is_default;
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
	
	public static final String PK_CUST_OP_PROJECT_B = "pk_cust_op_project_b";
	public static final String PK_CUST_OP_PROJECT = "pk_op_project";
	public static final String PK_CUSTOMER = "pk_customer";
	public static final String TS_CUST_OP_PROJECT_B = "ts_cust_op_project_b";



	public String getPk_cust_op_project_b() {
		return pk_cust_op_project_b;
	}

	public void setPk_cust_op_project_b(String pk_cust_op_project_b) {
		this.pk_cust_op_project_b = pk_cust_op_project_b;
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public String getPk_op_project() {
		return pk_op_project;
	}

	public void setPk_op_project(String pk_op_project) {
		this.pk_op_project = pk_op_project;
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

	public UFBoolean getIs_default() {
		return is_default;
	}

	public void setIs_default(UFBoolean is_default) {
		this.is_default = is_default;
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
		return PK_CUSTOMER;
	}

	@Override
	public String getPKFieldName() {
		return PK_CUST_OP_PROJECT_B;
	}

	@Override
	public String getTableName() {
		return TS_CUST_OP_PROJECT_B;
	}

}
