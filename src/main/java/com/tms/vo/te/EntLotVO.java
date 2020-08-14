package com.tms.vo.te;

import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;

/**
 * ts_entrustlot
 * 
 * @version 1.0
 * @since 1.0
 */
public class EntLotVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String pk_entrust_lot;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String lot;

	private java.lang.Integer seg_type;

	private UFBoolean iffx;

	private java.lang.Integer num_count;

	private org.nw.vo.pub.lang.UFDouble weight_count;

	private org.nw.vo.pub.lang.UFDouble volume_count;

	private org.nw.vo.pub.lang.UFDouble fee_weight_count;

	private java.lang.String pk_delivery;

	private java.lang.String deli_city;

	private java.lang.String pk_arrival;

	private java.lang.String arri_city;

	private java.lang.String pk_trans_type;

	private String req_deli_date;

	private java.lang.String bala_customer;

	private java.lang.String create_user;

	private org.nw.vo.pub.lang.UFDateTime create_time;

	private java.lang.String modify_user;

	private org.nw.vo.pub.lang.UFDateTime modify_time;

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

	private Integer vbillstatus;

	private UFDate dbilldate;

	private String pk_corp;
	
	private String pk_entrust;
	private UFDouble temp;	
	private UFDouble low_temp;
	private UFDouble hight_temp;
	private UFDouble speed_limit;
	private String pz_line;
	private Integer pz_mileage;

	public static final String PK_ENTRUSTLOT = "pk_entrust_lot";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String LOT = "lot";
	public static final String SEG_TYPE = "seg_type";
	public static final String NUM_COUNT = "num_count";
	public static final String WEIGHT_COUNT = "weight_count";
	public static final String VOLUME_COUNT = "volume_count";
	public static final String FEE_WEIGHT_COUNT = "fee_weight_count";
	public static final String PK_DELIVERY = "pk_delivery";
	public static final String DELI_CITY = "deli_city";
	public static final String PK_ARRIVAL = "pk_arrival";
	public static final String ARRI_CITY = "arri_city";
	public static final String PK_TRANS_TYPE = "pk_trans_type";
	public static final String REQ_DELI_DATE = "req_deli_date";
	public static final String BALA_CUSTOMER = "bala_customer";
	public static final String CREATE_USER = "create_user";
	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String DEF1 = "def1";
	public static final String DEF2 = "def2";
	public static final String DEF3 = "def3";
	public static final String DEF4 = "def4";
	public static final String DEF5 = "def5";
	public static final String DEF6 = "def6";
	public static final String DEF7 = "def7";
	public static final String DEF8 = "def8";
	public static final String DEF9 = "def9";
	public static final String DEF10 = "def10";
	public static final String DEF11 = "def11";
	public static final String DEF12 = "def12";
	
	
	public String getPz_line() {
		return pz_line;
	}

	public void setPz_line(String pz_line) {
		this.pz_line = pz_line;
	}

	public Integer getPz_mileage() {
		return pz_mileage;
	}

	public void setPz_mileage(Integer pz_mileage) {
		this.pz_mileage = pz_mileage;
	}

	public UFDouble getSpeed_limit() {
		return speed_limit;
	}

	public void setSpeed_limit(UFDouble speed_limit) {
		this.speed_limit = speed_limit;
	}

	public UFDouble getTemp() {
		return temp;
	}

	public void setTemp(UFDouble temp) {
		this.temp = temp;
	}

	public UFDouble getLow_temp() {
		return low_temp;
	}

	public void setLow_temp(UFDouble low_temp) {
		this.low_temp = low_temp;
	}

	public UFDouble getHight_temp() {
		return hight_temp;
	}

	public void setHight_temp(UFDouble hight_temp) {
		this.hight_temp = hight_temp;
	}

	public String getPk_entrust() {
		return pk_entrust;
	}

	public void setPk_entrust(String pk_entrust) {
		this.pk_entrust = pk_entrust;
	}

	//yaojiie 2016 1 3 添加字段
	private UFBoolean is_append;
	
	private UFBoolean edi_flag;
	private String edi_msg;
	
	public String getEdi_msg() {
		return edi_msg;
	}

	public void setEdi_msg(String edi_msg) {
		this.edi_msg = edi_msg;
	}

	public UFBoolean getEdi_flag() {
		return edi_flag;
	}

	public void setEdi_flag(UFBoolean edi_flag) {
		this.edi_flag = edi_flag;
	}

	public UFBoolean getIs_append() {
		return is_append;
	}

	public void setIs_append(UFBoolean is_append) {
		this.is_append = is_append;
	}
	
	public java.lang.String getPk_entrust_lot() {
		return pk_entrust_lot;
	}

	public void setPk_entrust_lot(java.lang.String pk_entrust_lot) {
		this.pk_entrust_lot = pk_entrust_lot;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public UFBoolean getIffx() {
		return iffx;
	}

	public void setIffx(UFBoolean iffx) {
		this.iffx = iffx;
	}

	public Integer getVbillstatus() {
		return vbillstatus;
	}

	public void setVbillstatus(Integer vbillstatus) {
		this.vbillstatus = vbillstatus;
	}

	public UFDate getDbilldate() {
		return dbilldate;
	}

	public void setDbilldate(UFDate dbilldate) {
		this.dbilldate = dbilldate;
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

	public java.lang.Integer getSeg_type() {
		return this.seg_type;
	}

	public void setSeg_type(java.lang.Integer value) {
		this.seg_type = value;
	}

	public java.lang.Integer getNum_count() {
		return this.num_count;
	}

	public void setNum_count(java.lang.Integer value) {
		this.num_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getWeight_count() {
		return this.weight_count;
	}

	public void setWeight_count(org.nw.vo.pub.lang.UFDouble value) {
		this.weight_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getVolume_count() {
		return this.volume_count;
	}

	public void setVolume_count(org.nw.vo.pub.lang.UFDouble value) {
		this.volume_count = value;
	}

	public org.nw.vo.pub.lang.UFDouble getFee_weight_count() {
		return this.fee_weight_count;
	}

	public void setFee_weight_count(org.nw.vo.pub.lang.UFDouble value) {
		this.fee_weight_count = value;
	}

	public java.lang.String getPk_delivery() {
		return this.pk_delivery;
	}

	public void setPk_delivery(java.lang.String value) {
		this.pk_delivery = value;
	}

	public java.lang.String getDeli_city() {
		return this.deli_city;
	}

	public void setDeli_city(java.lang.String value) {
		this.deli_city = value;
	}

	public java.lang.String getPk_arrival() {
		return this.pk_arrival;
	}

	public void setPk_arrival(java.lang.String value) {
		this.pk_arrival = value;
	}

	public java.lang.String getArri_city() {
		return this.arri_city;
	}

	public void setArri_city(java.lang.String value) {
		this.arri_city = value;
	}

	public java.lang.String getPk_trans_type() {
		return this.pk_trans_type;
	}

	public void setPk_trans_type(java.lang.String value) {
		this.pk_trans_type = value;
	}

	public String getReq_deli_date() {
		return req_deli_date;
	}

	public void setReq_deli_date(String req_deli_date) {
		this.req_deli_date = req_deli_date;
	}

	public java.lang.String getBala_customer() {
		return this.bala_customer;
	}

	public void setBala_customer(java.lang.String value) {
		this.bala_customer = value;
	}

	public java.lang.String getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(java.lang.String value) {
		this.create_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.create_time = value;
	}

	public java.lang.String getModify_user() {
		return this.modify_user;
	}

	public void setModify_user(java.lang.String value) {
		this.modify_user = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getModify_time() {
		return this.modify_time;
	}

	public void setModify_time(org.nw.vo.pub.lang.UFDateTime value) {
		this.modify_time = value;
	}

	public java.lang.String getDef1() {
		return this.def1;
	}

	public void setDef1(java.lang.String value) {
		this.def1 = value;
	}

	public java.lang.String getDef2() {
		return this.def2;
	}

	public void setDef2(java.lang.String value) {
		this.def2 = value;
	}

	public java.lang.String getDef3() {
		return this.def3;
	}

	public void setDef3(java.lang.String value) {
		this.def3 = value;
	}

	public java.lang.String getDef4() {
		return this.def4;
	}

	public void setDef4(java.lang.String value) {
		this.def4 = value;
	}

	public java.lang.String getDef5() {
		return this.def5;
	}

	public void setDef5(java.lang.String value) {
		this.def5 = value;
	}

	public java.lang.String getDef6() {
		return this.def6;
	}

	public void setDef6(java.lang.String value) {
		this.def6 = value;
	}

	public java.lang.String getDef7() {
		return this.def7;
	}

	public void setDef7(java.lang.String value) {
		this.def7 = value;
	}

	public java.lang.String getDef8() {
		return this.def8;
	}

	public void setDef8(java.lang.String value) {
		this.def8 = value;
	}

	public java.lang.String getDef9() {
		return this.def9;
	}

	public void setDef9(java.lang.String value) {
		this.def9 = value;
	}

	public java.lang.String getDef10() {
		return this.def10;
	}

	public void setDef10(java.lang.String value) {
		this.def10 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef11() {
		return this.def11;
	}

	public void setDef11(org.nw.vo.pub.lang.UFDouble value) {
		this.def11 = value;
	}

	public org.nw.vo.pub.lang.UFDouble getDef12() {
		return this.def12;
	}

	public void setDef12(org.nw.vo.pub.lang.UFDouble value) {
		this.def12 = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_entrust_lot";
	}

	public String getTableName() {
		return "ts_entrust_lot";
	}
	
	public EntLotVO() {
		super();
	}
}
