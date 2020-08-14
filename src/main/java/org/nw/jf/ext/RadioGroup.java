package org.nw.jf.ext;

import java.util.ArrayList;
import java.util.List;

import org.nw.jf.UiConstants;

/**
 * 
 * @author xuqc
 * @date 2014-10-21 上午11:28:11
 */
public class RadioGroup extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RadioGroup() {
		xtype = UiConstants.FORM_XTYPE.RADIOGROUP.toString();
	}

	List<Radio> items = new ArrayList<Radio>();

	public List<Radio> getItems() {
		return items;
	}

	public void setItems(List<Radio> items) {
		this.items = items;
	}

}
