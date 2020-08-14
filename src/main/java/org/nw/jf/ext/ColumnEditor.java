package org.nw.jf.ext;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;
import org.nw.json.JacksonUtils;

/**
 * GridPanel的Editor属性，也可以使用ExtFormItem,本身就是一个ExtFormItem， 但是没必要那么多的属性，因为增加了io开销
 * 
 * @author xuqc
 * @date 2011-1-4
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ColumnEditor implements Serializable {

	private static final long serialVersionUID = 3453486111620209111L;

	private String xtype;
	private Boolean allowBlank;
	private String vtype;
	private Boolean selectOnFocus;
	private Integer maxLength = UiConstants.DEFAULT_LENGTH;

	public ColumnEditor() {
	};

	public ColumnEditor(String xtype, String vtype, Boolean allowBlank) {
		this.xtype = xtype;
		this.vtype = vtype;
		if(!allowBlank) { // 如果allowBlank是true，则不需要设置值，这样就不会序列化到前台
			this.allowBlank = false;
		}
	}

	public ColumnEditor(String xtype, String vtype) {
		this.xtype = xtype;
		this.vtype = vtype;
	}

	public ColumnEditor(String xtype, Boolean allowBlank) {
		this.xtype = xtype;
		if(!allowBlank) {
			this.allowBlank = false;
		}
	}

	public ColumnEditor(String xtype) {
		this.xtype = xtype;
	}

	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	public Boolean getAllowBlank() {
		return allowBlank;
	}

	public void setAllowBlank(Boolean allowBlank) {
		this.allowBlank = allowBlank;
	}

	public Boolean getSelectOnFocus() {
		return selectOnFocus;
	}

	public void setSelectOnFocus(Boolean selectOnFocus) {
		this.selectOnFocus = selectOnFocus;
	}

	public String getVtype() {
		return vtype;
	}

	public void setVtype(String vtype) {
		this.vtype = vtype;
	}

	/**
	 * @return the maxLength
	 */
	public Integer getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength
	 *            the maxLength to set
	 */
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public String toString() {
		return JacksonUtils.writeValueAsString(this);
	}

}
