package org.nw.jf.ext.ref.win;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * 参照窗口类 目前支持3总模式的窗口 ：1、树结构 2、表格结构 3、左边树、右边表格结构
 * 
 * @author xuqc
 * @date 2011-1-5
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public abstract class AbstractRefWindow implements Serializable {

	private static final long serialVersionUID = -2699620935920215697L;

	// 参照的类型，参考UiConstants.REF_MODEL中的定义，必须在子类赋值
	protected int model = UiConstants.REF_MODEL.TREE.intValue();

	// 对于树结构的窗口，是否只能选择叶子节点
	// 需要将该参数传递到controller中，所以在setLeafflag时会同时将该参数加入param中
	protected boolean leafflag;

	/**
	 * 参照窗口的宽度
	 */
	protected Integer width;

	/**
	 * 参照窗口的高度
	 */
	protected Integer height;

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	protected String params = ""; // 查询参数

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public void addParam(String param) {
		if(StringUtils.isNotBlank(param)) {
			if(StringUtils.isNotBlank(this.params)) {
				this.params += ";" + param;// 已改成使用分号分隔
			} else {
				this.params = param;
			}
		}
	}

	/**
	 * @return the leafflag
	 */
	public boolean isLeafflag() {
		return leafflag;
	}

	/**
	 * @param leafflag
	 *            the leafflag to set
	 */
	public void setLeafflag(boolean leafflag) {
		if(leafflag) {
			this.addParam("leafflag=true"); // 需要将参数传递到controller
		}
		this.leafflag = leafflag;
	}

	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
