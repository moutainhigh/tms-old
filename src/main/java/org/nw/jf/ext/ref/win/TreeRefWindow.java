package org.nw.jf.ext.ref.win;

import org.nw.jf.UiConstants;

/**
 * 参照窗口：树结构
 * 
 * @author xuqc
 * @date 2011-1-5
 */
public class TreeRefWindow extends AbstractRefWindow {

	private static final long serialVersionUID = 5922674222198759498L;

	/**
	 * @param treeDataUrl
	 */
	public TreeRefWindow(String treeDataUrl) {
		this.treeDataUrl = treeDataUrl;
		model = UiConstants.REF_MODEL.TREE.intValue();
	}

	/**
	 * 参照窗口中的tree的数据加载url
	 */
	protected String treeDataUrl;

	public String getTreeDataUrl() {
		return treeDataUrl;
	}

	public void setTreeDataUrl(String treeDataUrl) {
		this.treeDataUrl = treeDataUrl;
	}

}
