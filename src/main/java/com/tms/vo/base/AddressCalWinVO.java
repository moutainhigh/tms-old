package com.tms.vo.base;

//yaojiie 2015 11 17 添加作业时间窗VO
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

@SuppressWarnings("serial")
public class AddressCalWinVO extends SuperVO {
	private String pk_address_cal_win;
	private String pk_address;
	private Integer dr;
	private String ts;
	
	private String mon_start;
	private String mon_end;
	private String tue_start;
	private String tue_end;
	private String wed_start;
	private String wed_end;
	private String thu_start;
	private String thu_end;
	private String fir_start;
	private String fir_end;
	private String sat_start;
	private String sat_end;
	private String sun_start;
	private String sun_end;
	
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
	
	public static final String PK_ADDRESS_CAL_WIN = "pk_address_cal_win";
	public static final String PK_ADDRESS = "pk_address";
	
	public static String getPkAddress() {
		return PK_ADDRESS;
	}

	public static String getPkAddressCalWin() {
		return PK_ADDRESS_CAL_WIN;
	}

	public String getPk_address_cal_win() {
		return pk_address_cal_win;
	}

	public void setPk_address_cal_win(String pk_address_cal_win) {
		this.pk_address_cal_win = pk_address_cal_win;
	}

	public String getPk_address() {
		return pk_address;
	}

	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
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

	public String getMon_start() {
		return mon_start;
	}

	public void setMon_start(String mon_start) {
		this.mon_start = mon_start;
	}

	public String getMon_end() {
		return mon_end;
	}

	public void setMon_end(String mon_end) {
		this.mon_end = mon_end;
	}

	public String getTue_start() {
		return tue_start;
	}

	public void setTue_start(String tue_start) {
		this.tue_start = tue_start;
	}

	public String getTue_end() {
		return tue_end;
	}

	public void setTue_end(String tue_end) {
		this.tue_end = tue_end;
	}

	public String getWed_start() {
		return wed_start;
	}

	public void setWed_start(String wed_start) {
		this.wed_start = wed_start;
	}

	public String getWed_end() {
		return wed_end;
	}

	public void setWed_end(String wed_end) {
		this.wed_end = wed_end;
	}

	public String getThu_start() {
		return thu_start;
	}

	public void setThu_start(String thu_start) {
		this.thu_start = thu_start;
	}

	public String getThu_end() {
		return thu_end;
	}

	public void setThu_end(String thu_end) {
		this.thu_end = thu_end;
	}

	public String getFir_start() {
		return fir_start;
	}

	public void setFir_start(String fir_start) {
		this.fir_start = fir_start;
	}

	public String getFir_end() {
		return fir_end;
	}

	public void setFir_end(String fir_end) {
		this.fir_end = fir_end;
	}

	public String getSat_start() {
		return sat_start;
	}

	public void setSat_start(String sat_start) {
		this.sat_start = sat_start;
	}

	public String getSat_end() {
		return sat_end;
	}

	public void setSat_end(String sat_end) {
		this.sat_end = sat_end;
	}

	public String getSun_start() {
		return sun_start;
	}

	public void setSun_start(String sun_start) {
		this.sun_start = sun_start;
	}

	public String getSun_end() {
		return sun_end;
	}

	public void setSun_end(String sun_end) {
		this.sun_end = sun_end;
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

	
	public String getCreate_user() {
		return create_user;
	}

	public void setCreate_user(String create_user) {
		this.create_user = create_user;
	}

	public UFDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(UFDateTime create_time) {
		this.create_time = create_time;
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

	
	public AddressCalWinVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return "pk_address";
	}

	@Override
	public String getPKFieldName() {
		return "pk_address_cal_win";
	}

	@Override
	public String getTableName() {
		return "ts_address_cal_win";
	}

}
