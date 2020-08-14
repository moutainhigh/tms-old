package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * Ext checkbox
 * 
 * @author xuqc
 * @date 2010-11-29
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Checkbox extends Field {

	private static final long serialVersionUID = -2959942729858251636L;
	private String inputValue = "true";
	private Boolean checked;
	private String boxLabel;

	public Checkbox() {
		xtype = UiConstants.FORM_XTYPE.UFTCHECKBOX.toString();
	}

	public String getInputValue() {
		return inputValue;
	}

	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	/**
	 * @return the boxLabel
	 */
	public String getBoxLabel() {
		return boxLabel;
	}

	/**
	 * @param boxLabel
	 *            the boxLabel to set
	 */
	public void setBoxLabel(String boxLabel) {
		this.boxLabel = boxLabel;
	}

}
