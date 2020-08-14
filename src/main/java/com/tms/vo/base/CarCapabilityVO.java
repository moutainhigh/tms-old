package com.tms.vo.base;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

//yaojiie  2015 11 17 添加车辆能力VO
@SuppressWarnings("serial")
public class CarCapabilityVO extends SuperVO {
	
	private String pk_car_capability;
	private String pk_car;
	private Integer dr;
	private String ts;
	
	private Integer capability_type;
	//yaojiie 2015 11 20 增加能力值属性
	private String value; 
	
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
	
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	
	public static final String PK_CAR_CAPABILITY = "pk_car_capability";
	public static final String PK_CAR = "pk_car";
	
	
	

	public String getPk_car_capability() {
		return pk_car_capability;
	}

	public void setPk_car_capability(String pk_car_capability) {
		this.pk_car_capability = pk_car_capability;
	}

	public String getPk_car() {
		return pk_car;
	}

	public void setPk_car(String pk_car) {
		this.pk_car = pk_car;
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

	public Integer getCapability_type() {
		return capability_type;
	}

	public void setCapability_type(Integer capability_type) {
		this.capability_type = capability_type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public UFDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(UFDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	public static String getPkCarCapability() {
		return PK_CAR_CAPABILITY;
	}

	public static String getPkCar() {
		return PK_CAR;
	}

	@Override
	public String getParentPKFieldName() {
		return "pk_car";
	}

	@Override
	public String getPKFieldName() {
		return "pk_car_capability";
	}

	@Override
	public String getTableName() {
		return "ts_car_capability";
	}

}
