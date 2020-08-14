package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;

/**
 */
@SuppressWarnings("serial")
public class PowerWorkbenchVO extends SuperVO {
	private String pk_power_workbench;
	private String pk_workbench;
	private String pk_role;
	private UFDateTime ts;
	private Integer dr;
	
	
	public String getPk_power_workbench() {
		return pk_power_workbench;
	}
	public void setPk_power_workbench(String pk_power_workbench) {
		this.pk_power_workbench = pk_power_workbench;
	}
	public String getPk_workbench() {
		return pk_workbench;
	}
	public void setPk_workbench(String pk_workbench) {
		this.pk_workbench = pk_workbench;
	}
	public String getPk_role() {
		return pk_role;
	}
	public void setPk_role(String pk_role) {
		this.pk_role = pk_role;
	}
	public UFDateTime getTs() {
		return ts;
	}
	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}
	public Integer getDr() {
		return dr;
	}
	public void setDr(Integer dr) {
		this.dr = dr;
	}
	@Override
	public String getParentPKFieldName() {
		return null;
	}
	@Override
	public String getPKFieldName() {
		return "pk_power_workbench";
	}
	@Override
	public String getTableName() {
		return "nw_power_workbench";
	}

	
}
