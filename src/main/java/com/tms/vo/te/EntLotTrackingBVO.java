package com.tms.vo.te;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 */
@SuppressWarnings("serial")
public class EntLotTrackingBVO extends SuperVO {
	private String pk_ent_lot_track_b;
	private String lot;
	private String pk_carrier;
	private String carno;
	private String gps_id;
	private String driver_name;	
	private String driver_mobile;
	private String longitude;	
	private String latitude;
	private UFDouble speed;	
	private UFDouble ent_lot_speed;
	private String speed_status;
	private UFDouble temp1;
	private UFDateTime temp_time1;
	private UFDouble temp2;
	private UFDateTime temp_time2;
	private UFDouble temp3;
	private UFDateTime temp_time3;
	private UFDouble temp4;
	private UFDateTime temp_time4;
	private UFDouble low_temp;
	private UFDouble hight_temp;
	private String temp_status;
	private UFDouble fuel;	
	private UFDouble press;
	private String event;
	private UFDouble distance;	
	private String engine_status;
	private String compressor_status;//压缩机状态
	private String magnetic;//门磁
	private String fence_code;//围栏编码
	private String fence_name;
	private UFDateTime enter_time;//驶入围栏时间
	private UFDateTime exit_time;//驶出围栏时间
	private String place_name;	
	private String road_name;
	private UFDateTime track_time;	
	private UFDateTime gps_time;
	private String memo;
	private String pk_corp;
	private UFDateTime create_time;
	
	private String gps_longitude;
	private String gps_latitude;	
	private UFDouble gps_accuracy;
	private String protocol_version;
	private String heading;
	private String altitude;
	private String cust_name;
	private String pk_address;
	private String detail_addr;
	private UFDateTime req_arri_date;
	private UFDateTime forecast_deli_date;
	private String service_status;
	
	private Integer num_count;
	private UFDouble weight_count;
	private UFDouble volume_count;
	
	private Integer dr;
	private UFDateTime ts;
	
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
	
	private Integer exp_count;
	
	public Integer getExp_count() {
		return exp_count;
	}
	public void setExp_count(Integer exp_count) {
		this.exp_count = exp_count;
	}
	public UFDateTime getCreate_time() {
		return create_time;
	}
	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
	}
	public String getPk_ent_lot_track_b() {
		return pk_ent_lot_track_b;
	}
	public void setPk_ent_lot_track_b(String pk_ent_lot_track_b) {
		this.pk_ent_lot_track_b = pk_ent_lot_track_b;
	}
	public String getLot() {
		return lot;
	}
	public void setLot(String lot) {
		this.lot = lot;
	}
	public String getPk_carrier() {
		return pk_carrier;
	}
	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}
	public String getCarno() {
		return carno;
	}
	public void setCarno(String carno) {
		this.carno = carno;
	}
	public String getGps_id() {
		return gps_id;
	}
	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
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
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public UFDouble getSpeed() {
		return speed;
	}
	public void setSpeed(UFDouble speed) {
		this.speed = speed;
	}
	public UFDouble getEnt_lot_speed() {
		return ent_lot_speed;
	}
	public void setEnt_lot_speed(UFDouble ent_lot_speed) {
		this.ent_lot_speed = ent_lot_speed;
	}
	public String getSpeed_status() {
		return speed_status;
	}
	public void setSpeed_status(String speed_status) {
		this.speed_status = speed_status;
	}
	public UFDouble getTemp1() {
		return temp1;
	}
	public void setTemp1(UFDouble temp1) {
		this.temp1 = temp1;
	}
	public UFDateTime getTemp_time1() {
		return temp_time1;
	}
	public void setTemp_time1(UFDateTime temp_time1) {
		this.temp_time1 = temp_time1;
	}
	public UFDouble getTemp2() {
		return temp2;
	}
	public void setTemp2(UFDouble temp2) {
		this.temp2 = temp2;
	}
	public UFDateTime getTemp_time2() {
		return temp_time2;
	}
	public void setTemp_time2(UFDateTime temp_time2) {
		this.temp_time2 = temp_time2;
	}
	public UFDouble getTemp3() {
		return temp3;
	}
	public void setTemp3(UFDouble temp3) {
		this.temp3 = temp3;
	}
	public UFDateTime getTemp_time3() {
		return temp_time3;
	}
	public void setTemp_time3(UFDateTime temp_time3) {
		this.temp_time3 = temp_time3;
	}
	public UFDouble getTemp4() {
		return temp4;
	}
	public void setTemp4(UFDouble temp4) {
		this.temp4 = temp4;
	}
	public UFDateTime getTemp_time4() {
		return temp_time4;
	}
	public void setTemp_time4(UFDateTime temp_time4) {
		this.temp_time4 = temp_time4;
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
	public String getTemp_status() {
		return temp_status;
	}
	public void setTemp_status(String temp_status) {
		this.temp_status = temp_status;
	}
	public UFDouble getFuel() {
		return fuel;
	}
	public void setFuel(UFDouble fuel) {
		this.fuel = fuel;
	}
	public UFDouble getPress() {
		return press;
	}
	public void setPress(UFDouble press) {
		this.press = press;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public UFDouble getDistance() {
		return distance;
	}
	public void setDistance(UFDouble distance) {
		this.distance = distance;
	}
	public String getEngine_status() {
		return engine_status;
	}
	public void setEngine_status(String engine_status) {
		this.engine_status = engine_status;
	}
	public String getCompressor_status() {
		return compressor_status;
	}
	public void setCompressor_status(String compressor_status) {
		this.compressor_status = compressor_status;
	}
	public String getMagnetic() {
		return magnetic;
	}
	public void setMagnetic(String magnetic) {
		this.magnetic = magnetic;
	}
	public String getFence_code() {
		return fence_code;
	}
	public void setFence_code(String fence_code) {
		this.fence_code = fence_code;
	}
	public String getFence_name() {
		return fence_name;
	}
	public void setFence_name(String fence_name) {
		this.fence_name = fence_name;
	}
	public UFDateTime getEnter_time() {
		return enter_time;
	}
	public void setEnter_time(UFDateTime enter_time) {
		this.enter_time = enter_time;
	}
	public UFDateTime getExit_time() {
		return exit_time;
	}
	public void setExit_time(UFDateTime exit_time) {
		this.exit_time = exit_time;
	}
	public String getPlace_name() {
		return place_name;
	}
	public void setPlace_name(String place_name) {
		this.place_name = place_name;
	}
	public String getRoad_name() {
		return road_name;
	}
	public void setRoad_name(String road_name) {
		this.road_name = road_name;
	}
	public UFDateTime getTrack_time() {
		return track_time;
	}
	public void setTrack_time(UFDateTime track_time) {
		this.track_time = track_time;
	}
	public UFDateTime getGps_time() {
		return gps_time;
	}
	public void setGps_time(UFDateTime gps_time) {
		this.gps_time = gps_time;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getPk_corp() {
		return pk_corp;
	}
	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}
	public String getGps_longitude() {
		return gps_longitude;
	}
	public void setGps_longitude(String gps_longitude) {
		this.gps_longitude = gps_longitude;
	}
	public String getGps_latitude() {
		return gps_latitude;
	}
	public void setGps_latitude(String gps_latitude) {
		this.gps_latitude = gps_latitude;
	}
	public UFDouble getGps_accuracy() {
		return gps_accuracy;
	}
	public void setGps_accuracy(UFDouble gps_accuracy) {
		this.gps_accuracy = gps_accuracy;
	}
	public String getProtocol_version() {
		return protocol_version;
	}
	public void setProtocol_version(String protocol_version) {
		this.protocol_version = protocol_version;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public String getAltitude() {
		return altitude;
	}
	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}
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
	public UFDateTime getReq_arri_date() {
		return req_arri_date;
	}
	public void setReq_arri_date(UFDateTime req_arri_date) {
		this.req_arri_date = req_arri_date;
	}
	public UFDateTime getForecast_deli_date() {
		return forecast_deli_date;
	}
	public void setForecast_deli_date(UFDateTime forecast_deli_date) {
		this.forecast_deli_date = forecast_deli_date;
	}
	public String getService_status() {
		return service_status;
	}
	public void setService_status(String service_status) {
		this.service_status = service_status;
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
		return "pk_ent_lot_track_b";
	}
	@Override
	public String getTableName() {
		return "ts_ent_lot_track_b";
	}
	
	
	
}
