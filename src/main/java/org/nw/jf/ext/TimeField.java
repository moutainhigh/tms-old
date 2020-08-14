package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 对应Ext的TimeField
 * 
 * @author xuqc
 * @date 2011-2-27
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TimeField extends TextField {

	/**
	 * 时间格式，更多格式参考TimeField.js
	 */
	public static final String FORMAT_24HOUR = "H:i";

	private static final long serialVersionUID = 8885284824383461651L;

	private String format = FORMAT_24HOUR; // 24小时时间格式

	private String minValue = "8:30"; // 最小值，该值应该在模板中设置，这需要对模板vo扩展
	private String maxValue = "17:30"; // 最大值，该值应该在模板中设置，这需要对模板vo扩展

	public TimeField() {
		xtype = UiConstants.FORM_XTYPE.TIMEFIELD.toString();
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

}
