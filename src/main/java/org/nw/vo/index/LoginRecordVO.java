package org.nw.vo.index;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

public class LoginRecordVO extends SuperVO {

	private static final long serialVersionUID = 1L;
	private String pk_login_record;
	private Integer dr;
	private UFDateTime ts;
	private String pk_user;
	private String user_code;
	private String user_name;
	private String ip;
	private String province;
	private String city;
	private String baidu_x;
	private String baidu_y;
	private String fun_code;
	private String help_name;
	private UFDateTime login_time;
	
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
	
	
	public String getPk_login_record() {
		return pk_login_record;
	}
	public void setPk_login_record(String pk_login_record) {
		this.pk_login_record = pk_login_record;
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
	public String getPk_user() {
		return pk_user;
	}
	public void setPk_user(String pk_user) {
		this.pk_user = pk_user;
	}
	public String getUser_code() {
		return user_code;
	}
	public void setUser_code(String user_code) {
		this.user_code = user_code;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getBaidu_x() {
		return baidu_x;
	}
	public void setBaidu_x(String baidu_x) {
		this.baidu_x = baidu_x;
	}
	public String getBaidu_y() {
		return baidu_y;
	}
	public void setBaidu_y(String baidu_y) {
		this.baidu_y = baidu_y;
	}
	public String getFun_code() {
		return fun_code;
	}
	public void setFun_code(String fun_code) {
		this.fun_code = fun_code;
	}
	public String getHelp_name() {
		return help_name;
	}
	public void setHelp_name(String help_name) {
		this.help_name = help_name;
	}
	public UFDateTime getLogin_time() {
		return login_time;
	}
	public void setLogin_time(UFDateTime login_time) {
		this.login_time = login_time;
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
	public static final String PK_LOGIN_RECORD = "pk_login_record";
	public static final String TS_LOGIN_RECORD = "ts_login_record";
	
	@Override
	public String getParentPKFieldName() {
		return null;
	}
	@Override
	public String getPKFieldName() {
		return PK_LOGIN_RECORD;
	}
	@Override
	public String getTableName() {
		return TS_LOGIN_RECORD;
	}
	
}
