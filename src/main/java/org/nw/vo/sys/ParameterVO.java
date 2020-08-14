/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;

/**
 */
@SuppressWarnings("serial")
public class ParameterVO extends SuperVO {
	private String pk_corp;
	private UFDateTime create_time;
	private String memo;
	private String modify_user;
	private UFDateTime modify_time;
	private Integer type;
	private String param_value;
	private UFDateTime ts;
	private Integer dr;
	private String param_name;
	private String create_user;
	private String pk_parameter;
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

	public static final String PK_CORP = "pk_corp";
	public static final String CREATE_TIME = "create_time";
	public static final String MEMO = "memo";
	public static final String MODIFY_USER = "modify_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String TYPE = "type";
	public static final String PARAM_VALUE = "param_value";
	public static final String PARAM_NAME = "param_name";
	public static final String CREATE_USER = "create_user";
	public static final String PK_PARAMETER = "pk_parameter";

	/**
	 * ����pk_corp��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getPk_corp() {
		return pk_corp;
	}

	/**
	 * ����pk_corp��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newPk_corp
	 *            String
	 */
	public void setPk_corp(String newPk_corp) {
		this.pk_corp = newPk_corp;
	}

	/**
	 * ����memo��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * ����memo��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newMemo
	 *            String
	 */
	public void setMemo(String newMemo) {
		this.memo = newMemo;
	}

	/**
	 * ����modify_user��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getModify_user() {
		return modify_user;
	}

	/**
	 * ����modify_user��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newModify_user
	 *            String
	 */
	public void setModify_user(String newModify_user) {
		this.modify_user = newModify_user;
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

	/**
	 * ����type��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return Integer
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * ����type��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newType
	 *            Integer
	 */
	public void setType(Integer newType) {
		this.type = newType;
	}

	/**
	 * ����param_value��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getParam_value() {
		return param_value;
	}

	/**
	 * ����param_value��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newParam_value
	 *            String
	 */
	public void setParam_value(String newParam_value) {
		this.param_value = newParam_value;
	}

	/**
	 * ����ts��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return UFDateTime
	 */
	public UFDateTime getTs() {
		return ts;
	}

	/**
	 * ����ts��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newTs
	 *            UFDateTime
	 */
	public void setTs(UFDateTime newTs) {
		this.ts = newTs;
	}

	/**
	 * ����dr��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return Integer
	 */
	public Integer getDr() {
		return dr;
	}

	/**
	 * ����dr��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newDr
	 *            Integer
	 */
	public void setDr(Integer newDr) {
		this.dr = newDr;
	}

	/**
	 * ����param_name��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getParam_name() {
		return param_name;
	}

	/**
	 * ����param_name��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newParam_name
	 *            String
	 */
	public void setParam_name(String newParam_name) {
		this.param_name = newParam_name;
	}

	/**
	 * ����create_user��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getCreate_user() {
		return create_user;
	}

	/**
	 * ����create_user��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newCreate_user
	 *            String
	 */
	public void setCreate_user(String newCreate_user) {
		this.create_user = newCreate_user;
	}

	/**
	 * ����pk_parameter��Getter����. ��������:2012-06-16 23:18:01
	 * 
	 * @return String
	 */
	public String getPk_parameter() {
		return pk_parameter;
	}

	/**
	 * ����pk_parameter��Setter����. ��������:2012-06-16 23:18:01
	 * 
	 * @param newPk_parameter
	 *            String
	 */
	public void setPk_parameter(String newPk_parameter) {
		this.pk_parameter = newPk_parameter;
	}

	/**
	 * <p>
	 * ȡ�ø�VO�����ֶ�.
	 * <p>
	 * ��������:2012-06-16 23:18:01
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getParentPKFieldName() {
		return null;
	}

	/**
	 * <p>
	 * ȡ�ñ�����.
	 * <p>
	 * ��������:2012-06-16 23:18:01
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getPKFieldName() {
		return "pk_parameter";
	}

	/**
	 * <p>
	 * ���ر����.
	 * <p>
	 * ��������:2012-06-16 23:18:01
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "nw_parameter";
	}

	/**
	 * ����Ĭ�Ϸ�ʽ����������.
	 * 
	 * ��������:2012-06-16 23:18:01
	 */
	public ParameterVO() {
		super();
	}
}