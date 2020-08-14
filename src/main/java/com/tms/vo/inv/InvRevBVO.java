package com.tms.vo.inv;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class InvRevBVO extends SuperVO {

	private String pk_inv_revise_b;
	private Integer dr;
	private UFDateTime ts;
	private Integer serialno;
	private String pk_invoice;
	private Integer revise_status;
	private Integer revise_type;
	private String revise_item;
	private String revise_old;
	private String revise_new;
	private String item_reftype;
	private Integer item_datatype;
	private String revise_user;
	private UFDateTime revise_time;
	private String confirm_user;
	private UFDateTime confirm_date;
	private String unconfirm_reason;
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

	public static final String PK_INV_REV_B = "pk_inv_revise_b";
	public static final String PK_INVOICE = "pk_invoice";
	public static final String TS_INV_REVISE_B = "ts_inv_revise_b";
	
	
	
	public String getPk_inv_revise_b() {
		return pk_inv_revise_b;
	}
	public void setPk_inv_revise_b(String pk_inv_revise_b) {
		this.pk_inv_revise_b = pk_inv_revise_b;
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
	public Integer getSerialno() {
		return serialno;
	}
	public void setSerialno(Integer serialno) {
		this.serialno = serialno;
	}
	public String getPk_invoice() {
		return pk_invoice;
	}
	public void setPk_invoice(String pk_invoice) {
		this.pk_invoice = pk_invoice;
	}
	public Integer getRevise_status() {
		return revise_status;
	}
	public void setRevise_status(Integer revise_status) {
		this.revise_status = revise_status;
	}
	public Integer getRevise_type() {
		return revise_type;
	}
	public void setRevise_type(Integer revise_type) {
		this.revise_type = revise_type;
	}
	public String getRevise_item() {
		return revise_item;
	}
	public void setRevise_item(String revise_item) {
		this.revise_item = revise_item;
	}
	public String getRevise_old() {
		return revise_old;
	}
	public void setRevise_old(String revise_old) {
		this.revise_old = revise_old;
	}
	public String getRevise_new() {
		return revise_new;
	}
	public void setRevise_new(String revise_new) {
		this.revise_new = revise_new;
	}
	public String getItem_reftype() {
		return item_reftype;
	}
	public void setItem_reftype(String item_reftype) {
		this.item_reftype = item_reftype;
	}
	public String getUnconfirm_reason() {
		return unconfirm_reason;
	}
	public void setUnconfirm_reason(String unconfirm_reason) {
		this.unconfirm_reason = unconfirm_reason;
	}
	public Integer getItem_datatype() {
		return item_datatype;
	}
	public void setItem_datatype(Integer item_datatype) {
		this.item_datatype = item_datatype;
	}
	public String getRevise_user() {
		return revise_user;
	}
	public void setRevise_user(String revise_user) {
		this.revise_user = revise_user;
	}
	public UFDateTime getRevise_time() {
		return revise_time;
	}
	public void setRevise_time(UFDateTime revise_time) {
		this.revise_time = revise_time;
	}
	public String getConfirm_user() {
		return confirm_user;
	}
	public void setConfirm_user(String confirm_user) {
		this.confirm_user = confirm_user;
	}
	public UFDateTime getConfirm_date() {
		return confirm_date;
	}
	public void setConfirm_date(UFDateTime confirm_date) {
		this.confirm_date = confirm_date;
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
		return PK_INVOICE;
	}
	@Override
	public String getPKFieldName() {
		return PK_INV_REV_B;
	}
	@Override
	public String getTableName() {
		return TS_INV_REVISE_B;
	}

	
}
