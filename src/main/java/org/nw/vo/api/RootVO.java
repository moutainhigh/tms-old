package org.nw.vo.api;

import java.util.List;

import org.nw.Global;

/**
 * 开放接口返回的vo，最后将这个vo转成xml格式
 * 
 * @author xuqc
 * @date 2015-1-24 下午05:03:47
 */
public class RootVO {
	private boolean result = true; // 返回的结果是否成功
	private String msg; // 如果返回失败，那么错误信息是什么
	private String source = Global.productName;// 标识从什么地方来(什么系统)的数据，比如LBS,TMS
	// 这里可以放各种vo
	private List dataset;// 返回成功的话，那么返回的数据集合

	public RootVO() {

	}

	public RootVO(List dataset) {
		this.result = true;
		this.dataset = dataset;
	}

	public RootVO(String msg) {
		this.result = false;
		this.msg = msg;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List getDataset() {
		return dataset;
	}

	public void setDataset(List dataset) {
		this.dataset = dataset;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
