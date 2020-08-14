
package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * 工作台VO
 * @time 2016 6 26 
 * 
 * @author XIA
 */
@SuppressWarnings("serial")
public class WorkBenchVO extends SuperVO {
	private String pk_workbench;
	private Integer dr;
	private UFDateTime ts;
	private String workbench_code;
	private String workbench_name;
	private String page_name;
	private UFBoolean locked_flag;
	
	private UFDateTime create_time;
	private String create_user;
	private UFDateTime modify_time;
	private String modify_user;
	
	private String def10;
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	
	public String getPk_workbench() {
		return pk_workbench;
	}
	public void setPk_workbench(String pk_workbench) {
		this.pk_workbench = pk_workbench;
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
	public String getWorkbench_code() {
		return workbench_code;
	}
	public void setWorkbench_code(String workbench_code) {
		this.workbench_code = workbench_code;
	}
	public String getWorkbench_name() {
		return workbench_name;
	}
	public void setWorkbench_name(String workbench_name) {
		this.workbench_name = workbench_name;
	}
	public String getPage_name() {
		return page_name;
	}
	public void setPage_name(String page_name) {
		this.page_name = page_name;
	}
	public UFBoolean getLocked_flag() {
		return locked_flag;
	}
	public void setLocked_flag(UFBoolean locked_flag) {
		this.locked_flag = locked_flag;
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
	public String getDef10() {
		return def10;
	}
	public void setDef10(String def10) {
		this.def10 = def10;
	}
	public String getDef2() {
		return def2;
	}
	public void setDef2(String def2) {
		this.def2 = def2;
	}
	public String getDef1() {
		return def1;
	}
	public void setDef1(String def1) {
		this.def1 = def1;
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
	public String getDef9() {
		return def9;
	}
	public void setDef9(String def9) {
		this.def9 = def9;
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
	@Override
	public String getParentPKFieldName() {
		return null;
	}
	@Override
	public String getPKFieldName() {
		return "pk_workbench";
	}
	@Override
	public String getTableName() {
		return "nw_workbench";
	}


}
