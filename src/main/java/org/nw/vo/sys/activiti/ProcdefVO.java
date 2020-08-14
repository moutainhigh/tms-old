package org.nw.vo.sys.activiti;

/**
 * ACT_RE_PROCDEF
 * 
 * @version 1.0
 * @since 1.0
 */
public class ProcdefVO extends org.nw.vo.pub.SuperVO {
	private static final long serialVersionUID = 1L;

	private String id_;

	private java.lang.Integer rev_;

	private String category_;

	private String name_;

	private String key_;

	private java.lang.Integer version_;

	private String deployment_id_;

	private String resource_name_;

	private String dgrm_resource_name_;

	private String description_;

	private Integer has_start_form_key_;

	private Integer has_graphical_notation_;

	private Integer suspension_state_;

	private String tenant_id_;

	private java.lang.Integer dr;

	private org.nw.vo.pub.lang.UFDateTime ts;

	private String pk_corp;

	public static final String ID_ = "id_";
	public static final String REV_ = "rev_";
	public static final String CATEGORY_ = "category_";
	public static final String NAME_ = "name_";
	public static final String KEY_ = "key_";
	public static final String VERSION_ = "version_";
	public static final String DEPLOYMENT_ID_ = "deployment_id_";
	public static final String RESOURCE_NAME_ = "resource_name_";
	public static final String DGRM_RESOURCE_NAME_ = "dgrm_resource_name_";
	public static final String DESCRIPTION_ = "description_";
	public static final String HAS_START_FORM_KEY_ = "has_start_form_key_";
	public static final String HAS_GRAPHICAL_NOTATION_ = "has_graphical_notation_";
	public static final String SUSPENSION_STATE_ = "suspension_state_";
	public static final String TENANT_ID_ = "tenant_id_";

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

	public String getCategory_() {
		return this.category_;
	}

	public void setCategory_(String value) {
		this.category_ = value;
	}

	public String getName_() {
		return this.name_;
	}

	public void setName_(String value) {
		this.name_ = value;
	}

	public String getKey_() {
		return this.key_;
	}

	public void setKey_(String value) {
		this.key_ = value;
	}

	public java.lang.Integer getVersion_() {
		return this.version_;
	}

	public void setVersion_(java.lang.Integer value) {
		this.version_ = value;
	}

	public String getDeployment_id_() {
		return this.deployment_id_;
	}

	public void setDeployment_id_(String value) {
		this.deployment_id_ = value;
	}

	public String getResource_name_() {
		return this.resource_name_;
	}

	public void setResource_name_(String value) {
		this.resource_name_ = value;
	}

	public String getDgrm_resource_name_() {
		return this.dgrm_resource_name_;
	}

	public void setDgrm_resource_name_(String value) {
		this.dgrm_resource_name_ = value;
	}

	public String getDescription_() {
		return this.description_;
	}

	public void setDescription_(String value) {
		this.description_ = value;
	}

	public Integer getHas_start_form_key_() {
		return this.has_start_form_key_;
	}

	public void setHas_start_form_key_(Integer value) {
		this.has_start_form_key_ = value;
	}

	public Integer getHas_graphical_notation_() {
		return this.has_graphical_notation_;
	}

	public void setHas_graphical_notation_(Integer value) {
		this.has_graphical_notation_ = value;
	}

	public Integer getSuspension_state_() {
		return this.suspension_state_;
	}

	public void setSuspension_state_(Integer value) {
		this.suspension_state_ = value;
	}

	public String getTenant_id_() {
		return this.tenant_id_;
	}

	public void setTenant_id_(String value) {
		this.tenant_id_ = value;
	}

	public String getParentPKFieldName() {
		return "DEPLOYMENT_ID_";
	}

	public String getPKFieldName() {
		return "id_";
	}

	public String getTableName() {
		return "ACT_RE_PROCDEF";
	}
}
