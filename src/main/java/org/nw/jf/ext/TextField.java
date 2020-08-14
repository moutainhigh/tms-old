package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 对应Ext中TextField.js中的定义
 * 
 * @author xuqc
 * @date 2011-3-19
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TextField extends Field {

	private static final long serialVersionUID = -3284235964896044084L;
	protected Boolean selectOnFocus;
	protected Boolean allowBlank;
	// protected Integer minLength=UiConstants.INVALID_LENGTH;
	protected Integer maxLength = UiConstants.DEFAULT_LENGTH;
	protected String vtype;

	public TextField() {
		xtype = UiConstants.FORM_XTYPE.TEXTFIELD.toString();
	}

	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	public Boolean getSelectOnFocus() {
		return selectOnFocus;
	}

	public void setSelectOnFocus(Boolean selectOnFocus) {
		this.selectOnFocus = selectOnFocus;
	}

	public Boolean getAllowBlank() {
		return allowBlank;
	}

	public void setAllowBlank(Boolean allowBlank) {
		this.allowBlank = allowBlank;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public String getVtype() {
		return vtype;
	}

	public void setVtype(String vtype) {
		this.vtype = vtype;
	}

}
