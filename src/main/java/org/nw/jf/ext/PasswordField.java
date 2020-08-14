package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * Ext 密码输入框
 * 
 * @author xuqc
 * @date 2010-11-4
 * @version $Revision$
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PasswordField extends TextField {

	private static final long serialVersionUID = 1573784916638736369L;
	private String inputType = "password";
	private String initial;

	public PasswordField() {
		xtype = UiConstants.FORM_XTYPE.PASSWORD.toString();
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public String getInitial() {
		return initial;
	}

	public void setInitial(String initial) {
		this.initial = initial;
	}

}
