package org.nw.jf.ext;

import java.util.ArrayList;
import java.util.List;

import org.nw.jf.UiConstants;

/**
 * 
 * @author xuqc
 * @date 2014-10-21 上午11:44:20
 */
public class CheckboxGroup extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CheckboxGroup() {
		xtype = UiConstants.FORM_XTYPE.CHECKBOXGROUP.toString();
	}

	private List<Checkbox> items = new ArrayList<Checkbox>();

	public List<Checkbox> getItems() {
		return items;
	}

	public void setItems(List<Checkbox> items) {
		this.items = items;
	}

}
