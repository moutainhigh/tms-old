package org.nw.jf.group;

import java.io.Serializable;

import org.nw.vo.pub.lang.UFBoolean;

/**
 * ��Ӧnw_billtemplet_b�е�options�ֶΣ���(pk�ֶ�,�磺<br/>
 * <root><tab code="demo_product_detail" showflag="Y" listshowflag="Y"
 * mulicolhead="�ϲ������_abc" /></root>
 * 
 * @author xuqc
 * @date 2013-3-26 ����10:06:16
 */
public class OptionsVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7173108027005442818L;
	private String code;
	private UFBoolean showflag;
	private UFBoolean listshowflag;
	private String mulicolhead;
	private String[] mulicolheadAry;

	public final String getCode() {
		return code;
	}

	public final void setCode(String code) {
		this.code = code;
	}

	public final UFBoolean getShowflag() {
		return showflag;
	}

	public final void setShowflag(UFBoolean showflag) {
		this.showflag = showflag;
	}

	public final UFBoolean getListshowflag() {
		return listshowflag;
	}

	public final void setListshowflag(UFBoolean listshowflag) {
		this.listshowflag = listshowflag;
	}

	public final String getMulicolhead() {
		return mulicolhead;
	}

	public final void setMulicolhead(String mulicolhead) {
		this.mulicolhead = mulicolhead;
	}

	public final String[] getMulicolheadAry() {
		return mulicolheadAry;
	}

	public final void setMulicolheadAry(String[] mulicolheadAry) {
		this.mulicolheadAry = mulicolheadAry;
	}

}
