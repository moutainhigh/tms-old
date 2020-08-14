package com.tms.service.job.lbs;

import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * 提供远程调用的数据结构的vo类,这里和查询数据库的字段是对应的，而数据库字段都使用小写的
 * 
 * @author xuqc
 * @date 2015-1-28 下午08:43:24
 */
public class TrackVO {

	private String gpsid;// gps设备ID
	private String longitude;// 经度
	private String latitude;// 维度
	private UFDouble speed;// 速度
	private UFDouble distance;// 里程
	private String place_name;// 地名
	private String road_name;// 道路名称
	private UFDateTime gps_time;// gps设备回传时间
	private String memo;// 备注
	private String speed_status;// 状态，在线  离线 超速
	private String icon;
	private String speed_limit;// 速度限制
	private String vbillno;// 单号,在批量调用接口时，需要根据这个字段来将接口信息和获取到的数据绑定。
	
	private String gps_longitude;// 原始经度
	private String gps_latitude;// 原始维度
	
	private String protocol_version;
	private Integer serialno;
	
	private Integer gps_num;
	private UFDouble gps_accuracy;
	private String heading;
	private String altitude;
	
	private String fence_code;
	private String fence_name;
	
	private UFDateTime enter_time;
	private UFDateTime exit_time;
	
	private String engine_status;//引擎状态
	private String compressor_status;//压缩机状态
	private String magnetic;
	private UFDouble fuel;
	private UFDouble press;
	private String event;
	
	private UFDouble temp1;
	private UFDateTime temp_time1;
	private UFDouble temp2;
	private UFDateTime temp_time2;
	private UFDouble temp3;
	private UFDateTime temp_time3;
	private UFDouble temp4;
	private UFDateTime temp_time4;
	
	
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
	public String getGpsid() {
		return gpsid;
	}
	public void setGpsid(String gpsid) {
		this.gpsid = gpsid;
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
	public UFDouble getDistance() {
		return distance;
	}
	public void setDistance(UFDouble distance) {
		this.distance = distance;
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
	public String getSpeed_status() {
		return speed_status;
	}
	public void setSpeed_status(String speed_status) {
		this.speed_status = speed_status;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getSpeed_limit() {
		return speed_limit;
	}
	public void setSpeed_limit(String speed_limit) {
		this.speed_limit = speed_limit;
	}
	public String getVbillno() {
		return vbillno;
	}
	public void setVbillno(String vbillno) {
		this.vbillno = vbillno;
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
	public String getProtocol_version() {
		return protocol_version;
	}
	public void setProtocol_version(String protocol_version) {
		this.protocol_version = protocol_version;
	}
	public Integer getSerialno() {
		return serialno;
	}
	public void setSerialno(Integer serialno) {
		this.serialno = serialno;
	}
	public Integer getGps_num() {
		return gps_num;
	}
	public void setGps_num(Integer gps_num) {
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
	
}
