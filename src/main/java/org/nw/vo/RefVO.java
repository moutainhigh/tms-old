package org.nw.vo;

import java.io.Serializable;

/**
 * ����VO���������յ������Ԫ�أ�id\name\code
 * 
 * @author xuqc
 * @date 2012-2-7
 */
public class RefVO implements Serializable {

	private static final long serialVersionUID = 8643157656226384164L;
	private String pk;
	private String code;
	private String name;

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * �÷�������RefField.js�е�getValue����
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-2-9
	 * 
	 */
	public String getValue() {
		return this.pk;
	}
}
