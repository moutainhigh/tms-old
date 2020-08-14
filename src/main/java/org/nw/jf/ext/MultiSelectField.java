package org.nw.jf.ext;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 多选择框参照，详见MultiSelect.js中的定义
 * 
 * @author xuqc
 * @date 2010-11-29
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MultiSelectField extends Field {

	private static final long serialVersionUID = 2147562169458880870L;

	protected Boolean allowBlank;
	/**
	 * 选项集合
	 */
	private List<Checkbox> items;

	public MultiSelectField() {
		xtype = UiConstants.FORM_XTYPE.MULTISELECTFIELD.toString();
	}

	/**
	 * @return the items
	 */
	public List<Checkbox> getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<Checkbox> items) {
		this.items = items;
	}

	public Boolean getAllowBlank() {
		return allowBlank;
	}

	public void setAllowBlank(Boolean allowBlank) {
		this.allowBlank = allowBlank;
	}

}
