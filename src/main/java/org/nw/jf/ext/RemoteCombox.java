package org.nw.jf.ext;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.UiConstants;

/**
 * 使用远程url作为数据源的下拉框，这个对象对应的Ext对象是uftcombo
 * 
 * @author xuqc
 * @date 2013-12-9 下午04:14:52
 */
public class RemoteCombox extends Combox {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2558810753244111795L;

	// 远程请求的url，作为数据源
	// 1、如果url以/开头，如/a/loadData.json，最后请求的路径会自动加上上下文
	// 2、如果不是，则作为一个相对路径
	String dataUrl;
	String params = "";// 查询的初始化参数
	String getByValueUrl;// 如果store还没加载的情况下，需要根据这个url去请求显示值

	public void addParam(String param) {
		if(StringUtils.isNotBlank(param)) {
			if(StringUtils.isNotBlank(this.params)) {
				this.params += ";" + param;// 已改成使用分号分隔
			} else {
				this.params = param;
			}
		}
	}

	public RemoteCombox() {
		xtype = UiConstants.FORM_XTYPE.UFTCOMBO.toString();
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getGetByValueUrl() {
		return getByValueUrl;
	}

	public void setGetByValueUrl(String getByValueUrl) {
		this.getByValueUrl = getByValueUrl;
	}

}
