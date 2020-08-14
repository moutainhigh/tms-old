package com.tms.vo.inv;


/**
 * ts_orderlot_inv
 * 
 * @version 1.0
 * @since 1.0
 */
public class OrderlotInvVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

		private java.lang.String pk_orderlot_inv;
	
		private java.lang.Integer dr;
	
		private org.nw.vo.pub.lang.UFDateTime ts;
	
		private java.lang.String lot;
	
		private java.lang.String invoice_vbillno;
	
		private java.lang.String rd_vbillno;
	
		private org.nw.vo.pub.lang.UFDouble fee_weight_count;
	
	public static final String PK_ORDERLOT_INV = "pk_orderlot_inv";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String LOT = "lot";
	public static final String INVOICE_VBILLNO = "invoice_vbillno";
	public static final String RD_VBILLNO = "rd_vbillno";
	public static final String FEE_WEIGHT_COUNT = "fee_weight_count";
	public java.lang.String getPk_orderlot_inv() {
		return this.pk_orderlot_inv;
	}

	public void setPk_orderlot_inv(java.lang.String value) {
		this.pk_orderlot_inv = value;
	}

	public java.lang.Integer getDr() {
		return this.dr;
	}

	public void setDr(java.lang.Integer value) {
		this.dr = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getTs() {
		return this.ts;
	}

	public void setTs(org.nw.vo.pub.lang.UFDateTime value) {
		this.ts = value;
	}

	public java.lang.String getLot() {
		return this.lot;
	}

	public void setLot(java.lang.String value) {
		this.lot = value;
	}

	public java.lang.String getInvoice_vbillno() {
		return this.invoice_vbillno;
	}

	public void setInvoice_vbillno(java.lang.String value) {
		this.invoice_vbillno = value;
	}

	public java.lang.String getRd_vbillno() {
		return this.rd_vbillno;
	}

	public void setRd_vbillno(java.lang.String value) {
		this.rd_vbillno = value;
	}

	public org.nw.vo.pub.lang.UFDouble getFee_weight_count() {
		return this.fee_weight_count;
	}

	public void setFee_weight_count(org.nw.vo.pub.lang.UFDouble value) {
		this.fee_weight_count = value;
	}

	public String getParentPKFieldName() {
		return "lot";
	}

	public String getPKFieldName() {
		return "pk_orderlot_inv";
	}

	public String getTableName() {
		return "ts_orderlot_inv";
	}
}
