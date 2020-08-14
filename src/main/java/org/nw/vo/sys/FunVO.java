/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package org.nw.vo.sys;

import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * </p>
 * 
 * @author Administrator
 */
@SuppressWarnings("serial")
public class FunVO extends SuperVO {
	private UFDateTime create_time;
	private String modify_user;
	private String pk_fun;
	private Integer fun_property;
	private UFBoolean locked_flag;
	private Integer dr;
	private String class_name;
	private String help_name;
	private UFBoolean isbuttonpower;
	private String create_user;
	private UFDateTime modify_time;
	private String fun_code;
	private UFDateTime ts;
	private String fun_name;
	private String fun_en_name;
	private String parent_id;
	private String bill_type;
	private Integer display_order;
	private UFBoolean if_code_rule;
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

	public String getFun_en_name() {
		return fun_en_name;
	}

	public void setFun_en_name(String fun_en_name) {
		this.fun_en_name = fun_en_name;
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

	private UFBoolean auth = UFBoolean.TRUE;// 扩展字段，对于按钮，标识是否有按钮权限

	public UFBoolean getAuth() {
		return auth;
	}

	public void setAuth(UFBoolean auth) {
		this.auth = auth;
	}

	public static final String CREATE_TIME = "create_time";
	public static final String MODIFY_USER = "modify_user";
	public static final String PK_FUN = "pk_fun";
	public static final String FUN_PROPERTY = "fun_property";
	public static final String LOCKED_FLAG = "locked_flag";
	public static final String CLASS_NAME = "class_name";
	public static final String HELP_NAME = "help_name";
	public static final String ISBUTTONPOWER = "isbuttonpower";
	public static final String CREATE_USER = "create_user";
	public static final String MODIFY_TIME = "modify_time";
	public static final String FUN_CODE = "fun_code";
	public static final String FUN_NAME = "fun_name";
	public static final String PARENT_ID = "parent_id";
	public static final String BILL_TYPE = "bill_type";
	public static final String DISPLAY_ORDER = "display_order";
	public static final String IF_CODE_RULE = "if_code_rule";

	public UFBoolean getIf_code_rule() {
		return if_code_rule;
	}

	public void setIf_code_rule(UFBoolean if_code_rule) {
		this.if_code_rule = if_code_rule;
	}

	/**
	 * ����modify_user��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getModify_user() {
		return modify_user;
	}

	/**
	 * ����modify_user��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newModify_user
	 *            String
	 */
	public void setModify_user(String newModify_user) {
		this.modify_user = newModify_user;
	}

	/**
	 * ����pk_fun��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getPk_fun() {
		return pk_fun;
	}

	/**
	 * ����pk_fun��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newPk_fun
	 *            String
	 */
	public void setPk_fun(String newPk_fun) {
		this.pk_fun = newPk_fun;
	}

	public String getBill_type() {
		return bill_type;
	}

	public void setBill_type(String bill_type) {
		this.bill_type = bill_type;
	}

	/**
	 * ����fun_property��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return Integer
	 */
	public Integer getFun_property() {
		return fun_property;
	}

	/**
	 * ����fun_property��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newFun_property
	 *            Integer
	 */
	public void setFun_property(Integer newFun_property) {
		this.fun_property = newFun_property;
	}

	/**
	 * ����locked_flag��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return UFBoolean
	 */
	public UFBoolean getLocked_flag() {
		return locked_flag;
	}

	/**
	 * ����locked_flag��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newLocked_flag
	 *            UFBoolean
	 */
	public void setLocked_flag(UFBoolean newLocked_flag) {
		this.locked_flag = newLocked_flag;
	}

	/**
	 * ����dr��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return Integer
	 */
	public Integer getDr() {
		return dr;
	}

	/**
	 * ����dr��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newDr
	 *            Integer
	 */
	public void setDr(Integer newDr) {
		this.dr = newDr;
	}

	/**
	 * ����class_name��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getClass_name() {
		return class_name;
	}

	/**
	 * ����class_name��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newClass_name
	 *            String
	 */
	public void setClass_name(String newClass_name) {
		this.class_name = newClass_name;
	}

	/**
	 * ����help_name��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getHelp_name() {
		return help_name;
	}

	/**
	 * ����help_name��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newHelp_name
	 *            String
	 */
	public void setHelp_name(String newHelp_name) {
		this.help_name = newHelp_name;
	}

	/**
	 * ����isbuttonpower��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return UFBoolean
	 */
	public UFBoolean getIsbuttonpower() {
		return isbuttonpower;
	}

	/**
	 * ����isbuttonpower��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newIsbuttonpower
	 *            UFBoolean
	 */
	public void setIsbuttonpower(UFBoolean newIsbuttonpower) {
		this.isbuttonpower = newIsbuttonpower;
	}

	/**
	 * ����create_user��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getCreate_user() {
		return create_user;
	}

	/**
	 * ����create_user��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newCreate_user
	 *            String
	 */
	public void setCreate_user(String newCreate_user) {
		this.create_user = newCreate_user;
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
	 * ����fun_code��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getFun_code() {
		return fun_code;
	}

	/**
	 * ����fun_code��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newFun_code
	 *            String
	 */
	public void setFun_code(String newFun_code) {
		this.fun_code = newFun_code;
	}

	/**
	 * ����ts��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return UFDateTime
	 */
	public UFDateTime getTs() {
		return ts;
	}

	/**
	 * ����ts��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newTs
	 *            UFDateTime
	 */
	public void setTs(UFDateTime newTs) {
		this.ts = newTs;
	}

	/**
	 * ����fun_name��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getFun_name() {
		return fun_name;
	}

	/**
	 * ����fun_name��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newFun_name
	 *            String
	 */
	public void setFun_name(String newFun_name) {
		this.fun_name = newFun_name;
	}

	/**
	 * ����parent_id��Getter����. ��������:2012-06-16 23:16:17
	 * 
	 * @return String
	 */
	public String getParent_id() {
		return parent_id;
	}

	/**
	 * ����parent_id��Setter����. ��������:2012-06-16 23:16:17
	 * 
	 * @param newParent_id
	 *            String
	 */
	public void setParent_id(String newParent_id) {
		this.parent_id = newParent_id;
	}

	public Integer getDisplay_order() {
		return display_order;
	}

	public void setDisplay_order(Integer display_order) {
		this.display_order = display_order;
	}

	/**
	 * <p>
	 * ȡ�ø�VO�����ֶ�.
	 * <p>
	 * ��������:2012-06-16 23:16:17
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
	 * ��������:2012-06-16 23:16:17
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getPKFieldName() {
		return "pk_fun";
	}

	/**
	 * <p>
	 * ���ر����.
	 * <p>
	 * ��������:2012-06-16 23:16:17
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "nw_fun";
	}

	/**
	 * ����Ĭ�Ϸ�ʽ����������.
	 * 
	 * ��������:2012-06-16 23:16:17
	 */
	public FunVO() {
		super();
	}
}