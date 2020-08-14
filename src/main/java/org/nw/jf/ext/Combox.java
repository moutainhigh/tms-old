package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * ExtCombox对象,没有数据源对象，必须被继承
 * 
 * @author xuqc
 * @date 2010-9-27
 * @version $Revision$
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public abstract class Combox extends TextField {

	private static final long serialVersionUID = 58787872043947016L;

	protected String hiddenName;

	// 下拉框能否直接录入
	protected Boolean editable;

	// 是否是number类型的值，如果是，在setValue的时候需要转换成number，否则会出现0.5!=0.50的情况
	protected Boolean numberflag;

	public Combox() {
		xtype = UiConstants.FORM_XTYPE.COMBO.toString();
	}

	public String getHiddenName() {
		return hiddenName;
	}

	public void setHiddenName(String hiddenName) {
		this.hiddenName = hiddenName;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public Boolean getNumberflag() {
		return numberflag;
	}

	public void setNumberflag(Boolean numberflag) {
		this.numberflag = numberflag;
	}
}
