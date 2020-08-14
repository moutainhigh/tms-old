package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

public class APPTrackingVO extends SuperVO {
	
	private String pk_app_track;
	private String mobile;
	private String email;
	private String longitude;
	private String latitude;
	private String mobile_battery;
	private Integer trans_type;
	private String app_version;
	private UFDouble speed;
	private UFDouble distance;
	private String engine;
	private String place_name;
	private String road_name;
	private UFDateTime track_time;
	private UFDateTime gps_time;
	private String pk_fence;
	private String memo;
	private String gps_provider;
	private String pk_corp;
	
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
	
	private String app_longitude;
	private String app_latitude;
	private String protocol_version;
	
	private String serialno;
	private String gps_num;
	private UFDouble gps_accuracy;
	private String heading;
	private String altitude;
	
	public static final String PK_APP_TRACK = "pk_app_track";
	
	public String getPk_app_track() {
		return pk_app_track;
	}

	public void setPk_app_track(String pk_app_track) {
		this.pk_app_track = pk_app_track;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getMobile_battery() {
		return mobile_battery;
	}

	public void setMobile_battery(String mobile_battery) {
		this.mobile_battery = mobile_battery;
	}

	public Integer getTrans_type() {
		return trans_type;
	}

	public void setTrans_type(Integer trans_type) {
		this.trans_type = trans_type;
	}

	public String getApp_version() {
		return app_version;
	}

	public void setApp_version(String app_version) {
		this.app_version = app_version;
	}

	public UFDouble getSpeed() {
		return speed;
	}

	public void setSpeed(UFDouble speed) {
		this.speed = speed;
	}

	public UFDouble getDistance() {
		return distance;
	}

	public void setDistance(UFDouble distance) {
		this.distance = distance;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
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

	public String getPk_fence() {
		return pk_fence;
	}

	public void setPk_fence(String pk_fence) {
		this.pk_fence = pk_fence;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getGps_provider() {
		return gps_provider;
	}

	public void setGps_provider(String gps_provider) {
		this.gps_provider = gps_provider;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
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

	public String getApp_longitude() {
		return app_longitude;
	}

	public void setApp_longitude(String app_longitude) {
		this.app_longitude = app_longitude;
	}

	public String getApp_latitude() {
		return app_latitude;
	}

	public void setApp_latitude(String app_latitude) {
		this.app_latitude = app_latitude;
	}

	public String getProtocol_version() {
		return protocol_version;
	}

	public void setProtocol_version(String protocol_version) {
		this.protocol_version = protocol_version;
	}

	public String getSerialno() {
		return serialno;
	}

	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}

	public String getGps_num() {
		return gps_num;
	}

	public void setGps_num(String gps_num) {
		this.gps_num = gps_num;
	}

	public UFDouble getGps_accuracy() {
		return gps_accuracy;
	}

	public void setGps_accuracy(UFDouble gps_accuracy) {
		this.gps_accuracy = gps_accuracy;
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

	@Override
	public String getParentPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPKFieldName() {
		return "pk_app_track";
	}

	@Override
	public String getTableName() {
		return "ts_app_track";
	}

}
