package org.nw.jf.ext;

import org.nw.jf.UiConstants;

/**
 * 大文本输入框
 * 
 * @author xuqc
 * @date 2012-5-15
 */
public class TextArea extends TextField {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8400416277492957407L;
	private Integer height;

	public TextArea() {
		xtype = UiConstants.FORM_XTYPE.TEXTAREA.toString();
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

}
