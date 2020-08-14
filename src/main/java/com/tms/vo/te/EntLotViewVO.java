package com.tms.vo.te;

import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;


@SuppressWarnings("serial")
public class EntLotViewVO extends org.nw.vo.pub.SuperVO {
	
	private String pk_entrust_lot_view;
	private String pk_entrust_lot;
	private Integer vbillstatus;
	private UFDateTime ts;
	private String lot;
	private String pk_corp;
	private UFDateTime create_time;
	private String create_user;
	private UFBoolean is_append;
	private String carr_name;
	private String trans_type;
	private String carno;
	private String car_type;
	private String driver_name;
	private String driver_mobile;
	private String certificate_id;
	private String container_no;
	private String sealing_no;
	private UFDateTime forecast_deli_date;
	private String detail_addr;
	private Integer num_count;
	private UFDouble weight_count;
	private UFDouble volume_count;
	private UFBoolean if_checked;
	private String pz_line;
	private Integer pz_mileage;
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
	
	public static final String PK_ENTRUST_LOT_VIEW = "pk_entrust_lot_view";
	public static final String TS_ENTRUST_LOT_VIEW = "ts_entrust_lot_view";
	
	
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

	public String getPk_entrust_lot_view() {
		return pk_entrust_lot_view;
	}

	public void setPk_entrust_lot_view(String pk_entrust_lot_view) {
		this.pk_entrust_lot_view = pk_entrust_lot_view;
	}

	public String getPk_entrust_lot() {
		return pk_entrust_lot;
	}

	public void setPk_entrust_lot(String pk_entrust_lot) {
		this.pk_entrust_lot = pk_entrust_lot;
	}

	public Integer getVbillstatus() {
		return vbillstatus;
	}

	public void setVbillstatus(Integer vbillstatus) {
		this.vbillstatus = vbillstatus;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
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

	public UFBoolean getIs_append() {
		return is_append;
	}

	public void setIs_append(UFBoolean is_append) {
		this.is_append = is_append;
	}

	public String getCarr_name() {
		return carr_name;
	}

	public void setCarr_name(String carr_name) {
		this.carr_name = carr_name;
	}

	public String getTrans_type() {
		return trans_type;
	}

	public void setTrans_type(String trans_type) {
		this.trans_type = trans_type;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getCar_type() {
		return car_type;
	}

	public void setCar_type(String car_type) {
		this.car_type = car_type;
	}

	public String getDriver_name() {
		return driver_name;
	}

	public void setDriver_name(String driver_name) {
		this.driver_name = driver_name;
	}

	public String getDriver_mobile() {
		return driver_mobile;
	}

	public void setDriver_mobile(String driver_mobile) {
		this.driver_mobile = driver_mobile;
	}

	public String getCertificate_id() {
		return certificate_id;
	}

	public void setCertificate_id(String certificate_id) {
		this.certificate_id = certificate_id;
	}

	public String getContainer_no() {
		return container_no;
	}

	public void setContainer_no(String container_no) {
		this.container_no = container_no;
	}

	public String getSealing_no() {
		return sealing_no;
	}

	public void setSealing_no(String sealing_no) {
		this.sealing_no = sealing_no;
	}

	public UFDateTime getForecast_deli_date() {
		return forecast_deli_date;
	}

	public void setForecast_deli_date(UFDateTime forecast_deli_date) {
		this.forecast_deli_date = forecast_deli_date;
	}

	public String getDetail_addr() {
		return detail_addr;
	}

	public void setDetail_addr(String detail_addr) {
		this.detail_addr = detail_addr;
	}

	public Integer getNum_count() {
		return num_count;
	}

	public void setNum_count(Integer num_count) {
		this.num_count = num_count;
	}

	public UFDouble getWeight_count() {
		return weight_count;
	}

	public void setWeight_count(UFDouble weight_count) {
		this.weight_count = weight_count;
	}

	public UFDouble getVolume_count() {
		return volume_count;
	}

	public void setVolume_count(UFDouble volume_count) {
		this.volume_count = volume_count;
	}

	public UFBoolean getIf_checked() {
		return if_checked;
	}

	public void setIf_checked(UFBoolean if_checked) {
		this.if_checked = if_checked;
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
		return null;
	}

	@Override
	public String getPKFieldName() {
		return PK_ENTRUST_LOT_VIEW;
	}

	@Override
	public String getTableName() {
		return TS_ENTRUST_LOT_VIEW;
	}
	
}
