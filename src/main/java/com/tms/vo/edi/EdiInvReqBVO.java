package com.tms.vo.edi;

import org.nw.vo.pub.SuperVO;

/**
 * edi_inv_pack_b
 * 
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class EdiInvReqBVO extends SuperVO {

	private java.lang.String pk_inv_req_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.Integer serialno;

	private java.lang.String pk_invoice;
	
	private java.lang.String req_code;
	
	private java.lang.String req_name;

	private java.lang.Integer req_type;

	private java.lang.String def1;

	private java.lang.String def2;

	private java.lang.String def3;

	private java.lang.String def4;

	private java.lang.String def5;

	private java.lang.String def6;

	private java.lang.String def7;

	private java.lang.String def8;

	private java.lang.String def9;

	private java.lang.String def10;

	private org.nw.vo.pub.lang.UFDouble def11;

	private org.nw.vo.pub.lang.UFDouble def12;

	private org.nw.vo.pub.lang.UFDateTime sync_time;
	private java.lang.String sync_by;

	private org.nw.vo.pub.lang.UFBoolean syncexp_flag;

	private java.lang.String syncexp_memo;

	public static final String PK_INV_REQ_B = "pk_inv_req_b";
	public static final String PK_INVOICE = "pk_invoice";

	public java.lang.String getPk_inv_req_b() {
		return pk_inv_req_b;
	}

	public void setPk_inv_req_b(java.lang.String pk_inv_req_b) {
		this.pk_inv_req_b = pk_inv_req_b;
	}

	public java.lang.Integer getDr() {
		return dr;
	}

	public void setDr(java.lang.Integer dr) {
		this.dr = dr;
	}

	public org.nw.vo.pub.lang.UFDateTime getTs() {
		return ts;
	}

	public void setTs(org.nw.vo.pub.lang.UFDateTime ts) {
		this.ts = ts;
	}

	public java.lang.Integer getSerialno() {
		return serialno;
	}

	public void setSerialno(java.lang.Integer serialno) {
		this.serialno = serialno;
	}

	public java.lang.String getPk_invoice() {
		return pk_invoice;
	}

	public void setPk_invoice(java.lang.String pk_invoice) {
		this.pk_invoice = pk_invoice;
	}

	public java.lang.String getReq_code() {
		return req_code;
	}

	public void setReq_code(java.lang.String req_code) {
		this.req_code = req_code;
	}

	public java.lang.String getReq_name() {
		return req_name;
	}

	public void setReq_name(java.lang.String req_name) {
		this.req_name = req_name;
	}

	public java.lang.Integer getReq_type() {
		return req_type;
	}

	public void setReq_type(java.lang.Integer req_type) {
		this.req_type = req_type;
	}

	public java.lang.String getDef1() {
		return def1;
	}

	public void setDef1(java.lang.String def1) {
		this.def1 = def1;
	}

	public java.lang.String getDef2() {
		return def2;
	}

	public void setDef2(java.lang.String def2) {
		this.def2 = def2;
	}

	public java.lang.String getDef3() {
		return def3;
	}

	public void setDef3(java.lang.String def3) {
		this.def3 = def3;
	}

	public java.lang.String getDef4() {
		return def4;
	}

	public void setDef4(java.lang.String def4) {
		this.def4 = def4;
	}

	public java.lang.String getDef5() {
		return def5;
	}

	public void setDef5(java.lang.String def5) {
		this.def5 = def5;
	}

	public java.lang.String getDef6() {
		return def6;
	}

	public void setDef6(java.lang.String def6) {
		this.def6 = def6;
	}

	public java.lang.String getDef7() {
		return def7;
	}

	public void setDef7(java.lang.String def7) {
		this.def7 = def7;
	}

	public java.lang.String getDef8() {
		return def8;
	}

	public void setDef8(java.lang.String def8) {
		this.def8 = def8;
	}

	public java.lang.String getDef9() {
		return def9;
	}

	public void setDef9(java.lang.String def9) {
		this.def9 = def9;
	}

	public java.lang.String getDef10() {
		return def10;
	}

	public void setDef10(java.lang.String def10) {
		this.def10 = def10;
	}

	public org.nw.vo.pub.lang.UFDouble getDef11() {
		return def11;
	}

	public void setDef11(org.nw.vo.pub.lang.UFDouble def11) {
		this.def11 = def11;
	}

	public org.nw.vo.pub.lang.UFDouble getDef12() {
		return def12;
	}

	public void setDef12(org.nw.vo.pub.lang.UFDouble def12) {
		this.def12 = def12;
	}

	public org.nw.vo.pub.lang.UFDateTime getSync_time() {
		return sync_time;
	}

	public void setSync_time(org.nw.vo.pub.lang.UFDateTime sync_time) {
		this.sync_time = sync_time;
	}

	public java.lang.String getSync_by() {
		return sync_by;
	}

	public void setSync_by(java.lang.String sync_by) {
		this.sync_by = sync_by;
	}

	public org.nw.vo.pub.lang.UFBoolean getSyncexp_flag() {
		return syncexp_flag;
	}

	public void setSyncexp_flag(org.nw.vo.pub.lang.UFBoolean syncexp_flag) {
		this.syncexp_flag = syncexp_flag;
	}

	public java.lang.String getSyncexp_memo() {
		return syncexp_memo;
	}

	public void setSyncexp_memo(java.lang.String syncexp_memo) {
		this.syncexp_memo = syncexp_memo;
	}

	public static String getPkInvReqB() {
		return PK_INV_REQ_B;
	}

	public static String getPkInvoice() {
		return PK_INVOICE;
	}

	public String getParentPKFieldName() {
		return "pk_invoice";
	}

	public String getPKFieldName() {
		return "pk_inv_req_b";
	}

	public String getTableName() {
		return "edi_inv_req_b";
	}
}
