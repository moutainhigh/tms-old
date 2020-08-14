package org.nw.vo.sys.activiti;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * ACT_RU_TASK
 * 
 * @version 1.0
 * @since 1.0
 */
public class TaskVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private String id_;

	private java.lang.Integer rev_;

	private String execution_id_;

	private String proc_inst_id_;

	private String proc_def_id_;

	private String name_;

	private String parent_task_id_;

	private String description_;

	private String task_def_key_;

	private String owner_;

	private String assignee_;

	private String delegation_;

	private java.lang.Integer priority_;

	private org.nw.vo.pub.lang.UFDateTime create_time_;

	private org.nw.vo.pub.lang.UFDateTime due_date_;

	private String category_;

	private java.lang.Integer suspension_state_;

	private String tenant_id_;

	private String form_key_;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private java.lang.String pk_corp;

	public static final String ID_ = "id_";
	public static final String REV_ = "rev_";
	public static final String EXECUTION_ID_ = "execution_id_";
	public static final String PROC_INST_ID_ = "proc_inst_id_";
	public static final String PROC_DEF_ID_ = "proc_def_id_";
	public static final String NAME_ = "name_";
	public static final String PARENT_TASK_ID_ = "parent_task_id_";
	public static final String DESCRIPTION_ = "description_";
	public static final String TASK_DEF_KEY_ = "task_def_key_";
	public static final String OWNER_ = "owner_";
	public static final String ASSIGNEE_ = "assignee_";
	public static final String DELEGATION_ = "delegation_";
	public static final String PRIORITY_ = "priority_";
	public static final String CREATE_TIME_ = "create_time_";
	public static final String DUE_DATE_ = "due_date_";
	public static final String CATEGORY_ = "category_";
	public static final String SUSPENSION_STATE_ = "suspension_state_";
	public static final String TENANT_ID_ = "tenant_id_";
	public static final String FORM_KEY_ = "form_key_";
	public static final String DR = "dr";
	public static final String TS = "ts";
	public static final String PK_CORP = "pk_corp";

	public String getId_() {
		return this.id_;
	}

	public void setId_(String value) {
		this.id_ = value;
	}

	public java.lang.Integer getRev_() {
		return this.rev_;
	}

	public void setRev_(java.lang.Integer value) {
		this.rev_ = value;
	}

	public String getExecution_id_() {
		return this.execution_id_;
	}

	public void setExecution_id_(String value) {
		this.execution_id_ = value;
	}

	public String getProc_inst_id_() {
		return this.proc_inst_id_;
	}

	public void setProc_inst_id_(String value) {
		this.proc_inst_id_ = value;
	}

	public String getProc_def_id_() {
		return this.proc_def_id_;
	}

	public void setProc_def_id_(String value) {
		this.proc_def_id_ = value;
	}

	public String getName_() {
		return this.name_;
	}

	public void setName_(String value) {
		this.name_ = value;
	}

	public String getParent_task_id_() {
		return this.parent_task_id_;
	}

	public void setParent_task_id_(String value) {
		this.parent_task_id_ = value;
	}

	public String getDescription_() {
		return this.description_;
	}

	public void setDescription_(String value) {
		this.description_ = value;
	}

	public String getTask_def_key_() {
		return this.task_def_key_;
	}

	public void setTask_def_key_(String value) {
		this.task_def_key_ = value;
	}

	public String getOwner_() {
		return this.owner_;
	}

	public void setOwner_(String value) {
		this.owner_ = value;
	}

	public String getAssignee_() {
		return this.assignee_;
	}

	public void setAssignee_(String value) {
		this.assignee_ = value;
	}

	public String getDelegation_() {
		return this.delegation_;
	}

	public void setDelegation_(String value) {
		this.delegation_ = value;
	}

	public java.lang.Integer getPriority_() {
		return this.priority_;
	}

	public void setPriority_(java.lang.Integer value) {
		this.priority_ = value;
	}

	public UFDateTime getCreate_time_() {
		return create_time_;
	}

	public void setCreate_time_(UFDateTime value) {
		this.create_time_ = value;
	}

	public UFDateTime getDue_date_() {
		return due_date_;
	}

	public void setDue_date_(UFDateTime value) {
		this.due_date_ = value;
	}

	public String getCategory_() {
		return this.category_;
	}

	public void setCategory_(String value) {
		this.category_ = value;
	}

	public java.lang.Integer getSuspension_state_() {
		return this.suspension_state_;
	}

	public void setSuspension_state_(java.lang.Integer value) {
		this.suspension_state_ = value;
	}

	public String getTenant_id_() {
		return this.tenant_id_;
	}

	public void setTenant_id_(String value) {
		this.tenant_id_ = value;
	}

	public String getForm_key_() {
		return this.form_key_;
	}

	public void setForm_key_(String value) {
		this.form_key_ = value;
	}

	public java.lang.Integer getDr() {
		return this.dr;
	}

	public void setDr(java.lang.Integer value) {
		this.dr = value;
	}

	public org.nw.vo.pub.lang.UFDateTime getTs() {
		return this.ts;
	}

	public void setTs(org.nw.vo.pub.lang.UFDateTime value) {
		this.ts = value;
	}

	public java.lang.String getPk_corp() {
		return this.pk_corp;
	}

	public void setPk_corp(java.lang.String value) {
		this.pk_corp = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "id_";
	}

	public String getTableName() {
		return "ACT_RU_TASK";
	}
}
