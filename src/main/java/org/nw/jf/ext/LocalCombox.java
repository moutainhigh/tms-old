package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 用于生成LocalCombox对象，详见combox.js的定义
 * 
 * @author xuqc
 * @date 2010-11-29
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocalCombox extends Combox {

	private static final long serialVersionUID = 3655974548208856382L;

	public LocalCombox() {
		xtype = UiConstants.FORM_XTYPE.LOCALCOMBO.toString();
	}

	private ComboxStore store;

	public ComboxStore getStore() {
		return store;
	}

	public void setStore(ComboxStore store) {
		this.store = store;
	}

}
