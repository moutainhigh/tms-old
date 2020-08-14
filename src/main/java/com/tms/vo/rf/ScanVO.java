package com.tms.vo.rf;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;

@SuppressWarnings("serial")
public class ScanVO extends SuperVO {
	
	private String pk_ent_line_b;
	private String lot;
	private String carr_code;
	private String carr_name;
	private String pk_driver;
	private String carno;
	private String addr_code;
	private String addr_name;
	private Integer num;
	private UFBoolean arrival_flag;
	
	public UFBoolean getArrival_flag() {
		return arrival_flag;
	}

	public void setArrival_flag(UFBoolean arrival_flag) {
		this.arrival_flag = arrival_flag;
	}

	public String getPk_ent_line_b() {
		return pk_ent_line_b;
	}

	public void setPk_ent_line_b(String pk_ent_line_b) {
		this.pk_ent_line_b = pk_ent_line_b;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getCarr_code() {
		return carr_code;
	}

	public void setCarr_code(String carr_code) {
		this.carr_code = carr_code;
	}

	public String getCarr_name() {
		return carr_name;
	}

	public void setCarr_name(String carr_name) {
		this.carr_name = carr_name;
	}

	public String getPk_driver() {
		return pk_driver;
	}

	public void setPk_driver(String pk_driver) {
		this.pk_driver = pk_driver;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getAddr_code() {
		return addr_code;
	}

	public void setAddr_code(String addr_code) {
		this.addr_code = addr_code;
	}

	public String getAddr_name() {
		return addr_name;
	}

	public void setAddr_name(String addr_name) {
		this.addr_name = addr_name;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	@Override
	public String getParentPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}

}
