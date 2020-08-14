package org.nw.web.vo;

import java.io.Serializable;
import java.util.List;

import org.nw.vo.sys.WorkBenchVO;

/**
 * web登录信息
 * 
 * @author xuqc
 */
public class LoginInfo implements Serializable {
	private static final long serialVersionUID = -4275035589688495765L;
	private String pk_corp;// 公司pk
	private String corp_code;// 公司编码，即unitCode
	private String corp_name;// 公司名称
	private String pk_user;// 用户pk
	private String user_code;// 用户编码
	private String user_name;// 用户名称
	private String password;// 用户密码
	private String pk_dept;// 用户部门
	private String dept_Code;//部门编码
	private String dept_name;//部门名称
	private String language;// 语言
	private Integer user_type;//用户类型
	private String pk_customer;//客户PK
	private String cust_code;//客户编码
	private String cust_name;//客户名字
	private String pk_carrier;//承运商PK
	private String carr_code;//承运商编码
	private String carr_name;//承运商名字
	private String pk_address;//绑定的地址
	private Integer platform_type;//平台类型
	private String loginDate; // 登录日期
	private String CurrentCorpWithChildren;
	//用户关联的工作台页数
	private List<WorkBenchVO> workbenchs;
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public List<WorkBenchVO> getWorkbenchs() {
		return workbenchs;
	}
	public void setWorkbenchs(List<WorkBenchVO> workbenchs) {
		this.workbenchs = workbenchs;
	}

	public Integer getPlatform_type() {
		return platform_type;
	}

	public void setPlatform_type(Integer platform_type) {
		this.platform_type = platform_type;
	}

	public String getPk_address() {
		return pk_address;
	}

	public void setPk_address(String pk_address) {
		this.pk_address = pk_address;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getCorp_code() {
		return corp_code;
	}

	public void setCorp_code(String corp_code) {
		this.corp_code = corp_code;
	}

	public String getCorp_name() {
		return corp_name;
	}

	public void setCorp_name(String corp_name) {
		this.corp_name = corp_name;
	}

	public String getPk_user() {
		return pk_user;
	}

	public void setPk_user(String pk_user) {
		this.pk_user = pk_user;
	}

	public String getUser_code() {
		return user_code;
	}

	public void setUser_code(String user_code) {
		this.user_code = user_code;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPk_dept() {
		return pk_dept;
	}

	public void setPk_dept(String pk_dept) {
		this.pk_dept = pk_dept;
	}

	public String getDept_Code() {
		return dept_Code;
	}

	public void setDept_Code(String dept_Code) {
		this.dept_Code = dept_Code;
	}

	public String getDept_name() {
		return dept_name;
	}

	public void setDept_name(String dept_name) {
		this.dept_name = dept_name;
	}
	
	public Integer getUser_type() {
		return user_type;
	}

	public void setUser_type(Integer user_type) {
		this.user_type = user_type;
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public String getCust_code() {
		return cust_code;
	}

	public void setCust_code(String cust_code) {
		this.cust_code = cust_code;
	}

	public String getCust_name() {
		return cust_name;
	}

	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}

	public String getPk_carrier() {
		return pk_carrier;
	}

	public void setPk_carrier(String pk_carrier) {
		this.pk_carrier = pk_carrier;
	}

	public String getCarr_code() {
		return carr_code;
	}

	public void setCarr_code(String carr_code) {
		this.carr_code = carr_code;
	}

	public String getCarr_name() {
		return carr_name;
	}

	public void setCarr_name(String carr_name) {
		this.carr_name = carr_name;
	}

	public String getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(String loginDate) {
		this.loginDate = loginDate;
	}

	public String getCurrentCorpWithChildren() {
		return CurrentCorpWithChildren;
	}

	public void setCurrentCorpWithChildren(String currentCorpWithChildren) {
		CurrentCorpWithChildren = currentCorpWithChildren;
	}

}
