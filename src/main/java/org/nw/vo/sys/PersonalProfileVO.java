package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * 个人配置信息，包括模板配置信息
 * 
 * @author xuqc
 * @date 2012-4-20
 */
public class PersonalProfileVO extends SuperVO {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4620499094298567430L;
	private String def10;
	private String def2;
	private String def1;
	private String def4;
	private String def3;
	private UFDateTime ts;
	private String pk_personal_profile;
	private String vmemo;
	private String def9;
	private String def5;
	private String def6;
	private String def7;
	private String def8;
	private Integer dr;
	private String comp_ids;
	private String field_pks;
	private String node_code;
	private String pk_corp;
	private String pk_template;
	private String pk_user;
	private Integer templatestyle;

	public static final String TEMPLATESTYLE = "templatestyle";
	public static final String NODE_CODE = "node_code";
	public static final String PK_CORP = "pk_corp";
	public static final String PK_TEMPLET = "pk_templet";
	public static final String PK_USER = "pk_user";
	public static final String DEF10 = "def10";
	public static final String DEF2 = "def2";
	public static final String DEF1 = "def1";
	public static final String DEF4 = "def4";
	public static final String DEF3 = "def3";
	public static final String PK_PERSONAL_PROFILE = "pk_personal_profile";
	public static final String VMEMO = "vmemo";
	public static final String DEF9 = "def9";
	public static final String DEF5 = "def5";
	public static final String DEF6 = "def6";
	public static final String DEF7 = "def7";
	public static final String DEF8 = "def8";
	public static final String DATA = "data";

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

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getPk_personal_profile() {
		return pk_personal_profile;
	}

	public void setPk_personal_profile(String pk_personal_profile) {
		this.pk_personal_profile = pk_personal_profile;
	}

	public String getVmemo() {
		return vmemo;
	}

	public void setVmemo(String vmemo) {
		this.vmemo = vmemo;
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

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public String getComp_ids() {
		return comp_ids;
	}

	public void setComp_ids(String comp_ids) {
		this.comp_ids = comp_ids;
	}

	public String getField_pks() {
		return field_pks;
	}

	public void setField_pks(String field_pks) {
		this.field_pks = field_pks;
	}

	public String getNode_code() {
		return node_code;
	}

	public void setNode_code(String node_code) {
		this.node_code = node_code;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getPk_template() {
		return pk_template;
	}

	public void setPk_template(String pk_template) {
		this.pk_template = pk_template;
	}

	public String getPk_user() {
		return pk_user;
	}

	public void setPk_user(String pk_user) {
		this.pk_user = pk_user;
	}

	public Integer getTemplatestyle() {
		return templatestyle;
	}

	public void setTemplatestyle(Integer templatestyle) {
		this.templatestyle = templatestyle;
	}

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {
		return "pk_personal_profile";
	}

	public String getTableName() {
		return "nw_personal_profile";
	}

}
