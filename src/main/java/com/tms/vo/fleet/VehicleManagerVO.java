package com.tms.vo.fleet;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
@SuppressWarnings("serial")
public class VehicleManagerVO extends SuperVO {
	
	private String pk_vehicle_manager;
	private String vbillno;
	private Integer vbillstatus;
	private Integer dr;
	private String ts;
	private String applicant;
	private String appl_dept;
	private Integer appl_reason;
	private String appl_nemo;
	private String appl_car_type;
	private UFDateTime appl_start_time;
	private UFDateTime appl_end_time;
	private String appl_start_addr;
	private String appl_start_city;
	private String appl_start_detail_addr;
	private String appl_end_addr;
	private String appl_end_city;
	private String appl_end_detail_addr;
	private String commit_user;
	private UFDateTime commit_time;
	private String confirm_user;
	private UFDateTime confirm_time;
	private String confirm_memo;
	private String refuse_user;
	private UFDateTime refuse_time;
	private Integer refuse_reason;
	private String refuse_memo;
	private String send_user;
	private UFDateTime send_time;
	private String main_driver;
	private String deputy_drive;
	private String carno;
	private String send_memo;
	private UFDouble dispatch_watch;
	private String dispatch_GPS;
	private UFDouble dispatch_fule;
	private UFDateTime dispatch_time;
	private String dispatch_addr;
	private String dispatch_city;
	private String dispatch_detail_addr;
	private String dispatch_memo;
	private UFDouble return_watch;
	private String return_GPS;
	private UFDouble return_fule;
	private UFDateTime return_time;
	private String return_addr;
	private String return_city;
	private String return_detail_addr;
	private String return_memo;
	private String pk_corp;
	private String memo;
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
	
	public static final String PK_VEHICLE_MANAGER = "pk_vehicle_manager";
	
	public String getAppl_car_type() {
		return appl_car_type;
	}

	public void setAppl_car_type(String appl_car_type) {
		this.appl_car_type = appl_car_type;
	}

	public String getPk_vehicle_manager() {
		return pk_vehicle_manager;
	}

	public void setPk_vehicle_manager(String pk_vehicle_manager) {
		this.pk_vehicle_manager = pk_vehicle_manager;
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

	public String getApplicant() {
		return applicant;
	}

	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}

	public String getAppl_dept() {
		return appl_dept;
	}

	public void setAppl_dept(String appl_dept) {
		this.appl_dept = appl_dept;
	}

	public Integer getAppl_reason() {
		return appl_reason;
	}

	public void setAppl_reason(Integer appl_reason) {
		this.appl_reason = appl_reason;
	}

	public String getAppl_nemo() {
		return appl_nemo;
	}

	public void setAppl_nemo(String appl_nemo) {
		this.appl_nemo = appl_nemo;
	}

	public UFDateTime getAppl_start_time() {
		return appl_start_time;
	}

	public void setAppl_start_time(UFDateTime appl_start_time) {
		this.appl_start_time = appl_start_time;
	}

	public UFDateTime getAppl_end_time() {
		return appl_end_time;
	}

	public void setAppl_end_time(UFDateTime appl_end_time) {
		this.appl_end_time = appl_end_time;
	}

	public String getAppl_start_addr() {
		return appl_start_addr;
	}

	public void setAppl_start_addr(String appl_start_addr) {
		this.appl_start_addr = appl_start_addr;
	}

	public String getAppl_start_city() {
		return appl_start_city;
	}

	public void setAppl_start_city(String appl_start_city) {
		this.appl_start_city = appl_start_city;
	}

	public String getAppl_start_detail_addr() {
		return appl_start_detail_addr;
	}

	public void setAppl_start_detail_addr(String appl_start_detail_addr) {
		this.appl_start_detail_addr = appl_start_detail_addr;
	}

	public String getAppl_end_addr() {
		return appl_end_addr;
	}

	public void setAppl_end_addr(String appl_end_addr) {
		this.appl_end_addr = appl_end_addr;
	}

	public String getAppl_end_city() {
		return appl_end_city;
	}

	public void setAppl_end_city(String appl_end_city) {
		this.appl_end_city = appl_end_city;
	}

	public String getAppl_end_detail_addr() {
		return appl_end_detail_addr;
	}

	public void setAppl_end_detail_addr(String appl_end_detail_addr) {
		this.appl_end_detail_addr = appl_end_detail_addr;
	}

	public String getCommit_user() {
		return commit_user;
	}

	public void setCommit_user(String commit_user) {
		this.commit_user = commit_user;
	}

	public UFDateTime getCommit_time() {
		return commit_time;
	}

	public void setCommit_time(UFDateTime commit_time) {
		this.commit_time = commit_time;
	}

	public String getConfirm_user() {
		return confirm_user;
	}

	public void setConfirm_user(String confirm_user) {
		this.confirm_user = confirm_user;
	}

	public UFDateTime getConfirm_time() {
		return confirm_time;
	}

	public void setConfirm_time(UFDateTime confirm_time) {
		this.confirm_time = confirm_time;
	}

	public String getConfirm_memo() {
		return confirm_memo;
	}

	public void setConfirm_memo(String confirm_memo) {
		this.confirm_memo = confirm_memo;
	}

	public String getRefuse_user() {
		return refuse_user;
	}

	public void setRefuse_user(String refuse_user) {
		this.refuse_user = refuse_user;
	}

	public UFDateTime getRefuse_time() {
		return refuse_time;
	}

	public void setRefuse_time(UFDateTime refuse_time) {
		this.refuse_time = refuse_time;
	}

	public Integer getRefuse_reason() {
		return refuse_reason;
	}

	public void setRefuse_reason(Integer refuse_reason) {
		this.refuse_reason = refuse_reason;
	}

	public String getRefuse_memo() {
		return refuse_memo;
	}

	public void setRefuse_memo(String refuse_memo) {
		this.refuse_memo = refuse_memo;
	}

	public String getSend_user() {
		return send_user;
	}

	public void setSend_user(String send_user) {
		this.send_user = send_user;
	}

	public UFDateTime getSend_time() {
		return send_time;
	}

	public void setSend_time(UFDateTime send_time) {
		this.send_time = send_time;
	}

	public String getMain_driver() {
		return main_driver;
	}

	public void setMain_driver(String main_driver) {
		this.main_driver = main_driver;
	}

	public String getDeputy_drive() {
		return deputy_drive;
	}

	public void setDeputy_drive(String deputy_drive) {
		this.deputy_drive = deputy_drive;
	}

	public String getCarno() {
		return carno;
	}

	public void setCarno(String carno) {
		this.carno = carno;
	}

	public String getSend_memo() {
		return send_memo;
	}

	public void setSend_memo(String send_memo) {
		this.send_memo = send_memo;
	}

	public UFDouble getDispatch_watch() {
		return dispatch_watch;
	}

	public void setDispatch_watch(UFDouble dispatch_watch) {
		this.dispatch_watch = dispatch_watch;
	}

	public String getDispatch_GPS() {
		return dispatch_GPS;
	}

	public void setDispatch_GPS(String dispatch_GPS) {
		this.dispatch_GPS = dispatch_GPS;
	}

	public UFDouble getDispatch_fule() {
		return dispatch_fule;
	}

	public void setDispatch_fule(UFDouble dispatch_fule) {
		this.dispatch_fule = dispatch_fule;
	}

	public UFDateTime getDispatch_time() {
		return dispatch_time;
	}

	public void setDispatch_time(UFDateTime dispatch_time) {
		this.dispatch_time = dispatch_time;
	}

	public String getDispatch_addr() {
		return dispatch_addr;
	}

	public void setDispatch_addr(String dispatch_addr) {
		this.dispatch_addr = dispatch_addr;
	}

	public String getDispatch_city() {
		return dispatch_city;
	}

	public void setDispatch_city(String dispatch_city) {
		this.dispatch_city = dispatch_city;
	}

	public String getDispatch_detail_addr() {
		return dispatch_detail_addr;
	}

	public void setDispatch_detail_addr(String dispatch_detail_addr) {
		this.dispatch_detail_addr = dispatch_detail_addr;
	}

	public String getDispatch_memo() {
		return dispatch_memo;
	}

	public void setDispatch_memo(String dispatch_memo) {
		this.dispatch_memo = dispatch_memo;
	}

	public UFDouble getReturn_watch() {
		return return_watch;
	}

	public void setReturn_watch(UFDouble return_watch) {
		this.return_watch = return_watch;
	}

	public String getReturn_GPS() {
		return return_GPS;
	}

	public void setReturn_GPS(String return_GPS) {
		this.return_GPS = return_GPS;
	}

	public UFDouble getReturn_fule() {
		return return_fule;
	}

	public void setReturn_fule(UFDouble return_fule) {
		this.return_fule = return_fule;
	}

	public UFDateTime getReturn_time() {
		return return_time;
	}

	public void setReturn_time(UFDateTime return_time) {
		this.return_time = return_time;
	}

	public String getReturn_addr() {
		return return_addr;
	}

	public void setReturn_addr(String return_addr) {
		this.return_addr = return_addr;
	}

	public String getReturn_city() {
		return return_city;
	}

	public void setReturn_city(String return_city) {
		this.return_city = return_city;
	}

	public String getReturn_detail_addr() {
		return return_detail_addr;
	}

	public void setReturn_detail_addr(String return_detail_addr) {
		this.return_detail_addr = return_detail_addr;
	}

	public String getReturn_memo() {
		return return_memo;
	}

	public void setReturn_memo(String return_memo) {
		this.return_memo = return_memo;
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

	public static String getPkVehicleManager() {
		return PK_VEHICLE_MANAGER;
	}

	public VehicleManagerVO() {
		super();
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getPKFieldName() {
		return PK_VEHICLE_MANAGER;
	}

	@Override
	public String getTableName() {
		return "ts_vehicle_manager";
	}

}
