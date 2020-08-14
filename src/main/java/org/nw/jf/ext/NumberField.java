package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 数字输入框，包括整数和小数
 * 
 * @author xuqc
 * @date 2010-11-4
 * @version $Revision$
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class NumberField extends TextField {

	private static final long serialVersionUID = -3897682700203272536L;
	private int decimalPrecision = UiConstants.DEFAULT_PRECISION;
	private String style = "text-align: right";

	public NumberField() {
		xtype = UiConstants.FORM_XTYPE.NUMBERFIELD.toString();
	};

	public NumberField(int decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
		xtype = UiConstants.FORM_XTYPE.NUMBERFIELD.toString();
	}

	public int getDecimalPrecision() {
		return decimalPrecision;
	}

	public void setDecimalPrecision(int decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	/**
	 * @return the style
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}

}
