package com.tms.vo.te;

import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_ent_trans_his_b
 * yaojiie 2015 12 29 委托单运力信息历史表
 * @version 1.0
 * @since 1.0
 */
public class EntTransHisBVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_ent_trans_his_b;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_entrust;

	private java.lang.String carno;

	private java.lang.String pk_driver;

	private java.lang.String container_no;

	private java.lang.String sealing_no;

	private org.nw.vo.pub.lang.UFDateTime forecast_deli_date;

	private java.lang.String pk_car_type;
	
	private java.lang.String certificate_id;
	
	private java.lang.String driving_license;

	private java.lang.Integer num;

	private java.lang.String memo;

	private String gps_id;
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
	
	private String pk_address;
	private String detail_addr;
	

	public String getPk_address() {
		return pk_address;
	}

	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
	}

	public String getDetail_addr() {
		return detail_addr;
	}

	public void setDetail_addr(String detail_addr) {
		this.detail_addr = detail_addr;
	}

	public java.lang.String getCertificate_id() {
		return certificate_id;
	}

	public void setCertificate_id(java.lang.String certificate_id) {
		this.certificate_id = certificate_id;
	}

	public java.lang.String getDriving_license() {
		return driving_license;
	}

	public void setDriving_license(java.lang.String driving_license) {
		this.driving_license = driving_license;
	}

	public String getGps_id() {
		return gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
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

	public static final String PK_ENT_TRANS_HIS_B = "pk_ent_trans_his_b";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_ENTRUST = "pk_entrust";
	public static final String CARNO = "carno";
	public static final String PK_DRIVER = "pk_driver";
	public static final String CONTAINER_NO = "container_no";
	public static final String SEALING_NO = "sealing_no";
	public static final String FORECAST_DELI_DATE = "forecast_deli_date";
	public static final String PK_CAR_TYPE = "pk_car_type";
	public static final String NUM = "num";
	public static final String MEMO = "memo";


	public java.lang.String getPk_ent_trans_his_b() {
		return pk_ent_trans_his_b;
	}

	public void setPk_ent_trans_his_b(java.lang.String pk_ent_trans_his_b) {
		this.pk_ent_trans_his_b = pk_ent_trans_his_b;
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

	public java.lang.String getPk_entrust() {
		return this.pk_entrust;
	}

	public void setPk_entrust(java.lang.String value) {
		this.pk_entrust = value;
	}

	public java.lang.String getCarno() {
		return this.carno;
	}

	public void setCarno(java.lang.String value) {
		this.carno = value;
	}

	public java.lang.String getPk_driver() {
		return this.pk_driver;
	}

	public void setPk_driver(java.lang.String value) {
		this.pk_driver = value;
	}

	public java.lang.String getContainer_no() {
		return this.container_no;
	}

	public void setContainer_no(java.lang.String value) {
		this.container_no = value;
	}

	public java.lang.String getSealing_no() {
		return this.sealing_no;
	}

	public void setSealing_no(java.lang.String value) {
		this.sealing_no = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getForecast_deli_date() {
		return this.forecast_deli_date;
	}

	public void setForecast_deli_date(org.nw.vo.pub.lang.UFDateTime value) {
		this.forecast_deli_date = value;
	}

	public java.lang.String getPk_car_type() {
		return this.pk_car_type;
	}

	public void setPk_car_type(java.lang.String value) {
		this.pk_car_type = value;
	}

	public java.lang.Integer getNum() {
		return this.num;
	}

	public void setNum(java.lang.Integer value) {
		this.num = value;
	}

	public java.lang.String getMemo() {
		return this.memo;
	}

	public void setMemo(java.lang.String value) {
		this.memo = value;
	}

	public String getParentPKFieldName() {
		return "pk_entrust";
	}

	public String getPKFieldName() {
		return "pk_ent_trans_his_b";
	}

	public String getTableName() {
		return "ts_ent_trans_his_b";
	}
}
