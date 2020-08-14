package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 用于生成TranCombox对象，详见combox.js的定义
 * 
 * @author xuqc
 * @date 2010-9-27
 * @version $Revision$
 * @deprecated 页面不再使用这个来渲染了，改成使用LocalCombox
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TranCombox extends Combox {

	private static final long serialVersionUID = -8123915894976723913L;
	private String transform;

	public TranCombox() {
		xtype = UiConstants.FORM_XTYPE.TRANCOMBO.toString();
	}

	public String getTransform() {
		return transform;
	}

	public void setTransform(String transform) {
		this.transform = transform;
	}

}
