package com.tms.vo.fleet;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
//yaojiie 2015 12 16 保险管理VO
@SuppressWarnings("serial")
public class InsurVO extends SuperVO {
	
	private String pk_insur;
	private String vbillno;
	private Integer vbillstatus;
	private String carno;
	private String pk_driver;
	
	private Integer insur_corp;

	private UFDateTime insur_time;
	private UFDateTime next_insur_time;
	
	private UFDouble cost_amount;
	private UFDouble coverage;
	
	private String pk_corp;
	private String memo;
	
	private Integer dr;
	private String ts;
	
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
	
	public static final String PK_INSUR = "pk_insur";

	
	

	public String getPk_insur() {
		return pk_insur;
	}

	public void setPk_insur(String pk_insur) {
		this.pk_insur = pk_insur;
	}

	public String getVbillno() {
		return vbillno;
	}

	public void setVbillno(String vbillno) {
		this.vbillno = vbillno;
	}

	public Integer getVbillstatus() {
		return vbillstatus;
	}

	public void setVbillstatus(Integer vbillstatus) {
		this.vbillstatus = vbillstatus;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getPk_driver() {
		return pk_driver;
	}

	public void setPk_driver(String pk_driver) {
		this.pk_driver = pk_driver;
	}

	public Integer getInsur_corp() {
		return insur_corp;
	}

	public void setInsur_corp(Integer insur_corp) {
		this.insur_corp = insur_corp;
	}

	public UFDateTime getInsur_time() {
		return insur_time;
	}

	public void setInsur_time(UFDateTime insur_time) {
		this.insur_time = insur_time;
	}

	public UFDateTime getNext_insur_time() {
		return next_insur_time;
	}

	public void setNext_insur_time(UFDateTime next_insur_time) {
		this.next_insur_time = next_insur_time;
	}

	public UFDouble getCost_amount() {
		return cost_amount;
	}

	public void setCost_amount(UFDouble cost_amount) {
		this.cost_amount = cost_amount;
	}

	public UFDouble getCoverage() {
		return coverage;
	}

	public void setCoverage(UFDouble coverage) {
		this.coverage = coverage;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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

	public static String getPkInsur() {
		return PK_INSUR;
	}

	public InsurVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return "pk_insur";
	}

	@Override
	public String getTableName() {
		return "ts_insur";
	}

}
