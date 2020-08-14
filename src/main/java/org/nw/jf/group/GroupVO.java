package org.nw.jf.group;

import java.io.Serializable;

import org.nw.json.JacksonUtils;


/**
 * 表头分组ＶＯ
 * 
 * @author xuqc
 * @date 2013-3-26 上午10:01:25
 */
public class GroupVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1456683552629553344L;

	public static final String ALIGN[] = new String[] { "left", "center", "right" };

	private String header;// 默认空白
	private Integer colspan;
	private String align;// 默认居中

	public final String getHeader() {
		return header;
	}

	public final void setHeader(String header) {
		this.header = header;
	}

	public Integer getColspan() {
		return colspan;
	}

	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}

	public final String getAlign() {
		return align;
	}

	public final void setAlign(String align) {
		this.align = align;
	}

	public String toString() {
		return JacksonUtils.writeValueAsString(this);
	}
}
