package org.nw.vo.sys.activiti;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * ACT_RE_DEPLOYMENT
 * 
 * @version 1.0
 * @since 1.0
 */
public class DeploymentVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private java.lang.String id_;

	private java.lang.String name_;

	private java.lang.String category_;

	private java.lang.String tenant_id_;

	private org.nw.vo.pub.lang.UFDateTime deploy_time_;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private String pk_corp;

	public static final String ID_ = "id_";
	public static final String NAME_ = "name_";
	public static final String CATEGORY_ = "category_";
	public static final String TENANT_ID_ = "tenant_id_";
	public static final String DEPLOY_TIME_ = "deploy_time_";

	public java.lang.Integer getDr() {
		return dr;
	}

	public void setDr(java.lang.Integer dr) {
		this.dr = dr;
	}

	public org.nw.vo.pub.lang.UFDateTime getTs() {
		return ts;
	}

	public void setTs(org.nw.vo.pub.lang.UFDateTime ts) {
		this.ts = ts;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public java.lang.String getId_() {
		return this.id_;
	}

	public void setId_(java.lang.String value) {
		this.id_ = value;
	}

	public java.lang.String getName_() {
		return this.name_;
	}

	public void setName_(java.lang.String value) {
		this.name_ = value;
	}

	public java.lang.String getCategory_() {
		return this.category_;
	}

	public void setCategory_(java.lang.String value) {
		this.category_ = value;
	}

	public java.lang.String getTenant_id_() {
		return this.tenant_id_;
	}

	public void setTenant_id_(java.lang.String value) {
		this.tenant_id_ = value;
	}

	public UFDateTime getDeploy_time_() {
		return deploy_time_;
	}

	public void setDeploy_time_(UFDateTime value) {
		this.deploy_time_ = value;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "id_";
	}

	public String getTableName() {
		return "ACT_RE_DEPLOYMENT";
	}
}
