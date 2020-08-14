package org.nw.jf.ext.ref.win;

import java.util.ArrayList;
import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.ext.ListColumn;

/**
 * 参照窗口 左树右表结构
 * 
 * @author xuqc
 * @date 2011-1-5
 */
public class TreeAndGridRefWindow extends AbstractRefWindow {

	private static final long serialVersionUID = -3522164112780532851L;

	/**
	 * 当参照窗口存在tree与grid时，tree与grid的关联字段
	 */
	protected String treePkField;
	/**
	 * 参照窗口中的tree的数据加载url
	 */
	private String treeDataUrl;

	/**
	 * 参照窗口包含的grid的数据加载url 当存在表格时，这是必须的
	 */
	private String gridDataUrl;

	/**
	 * 存储grid中的列定义
	 */
	private List<ListColumn> extGridColumnDescns = new ArrayList<ListColumn>();

	// 左树右表的参照，左边树的宽度
	private Integer minWidth;

	/**
	 * 
	 * @param treePkField
	 * @param treeDataUrl
	 * @param gridDataUrl
	 * @param extGridColumnDescns
	 */
	public TreeAndGridRefWindow(String treePkField, String treeDataUrl, String gridDataUrl,
			List<ListColumn> extGridColumnDescns) {
		this.treePkField = treePkField;
		this.treeDataUrl = treeDataUrl;
		this.gridDataUrl = gridDataUrl;
		this.extGridColumnDescns = extGridColumnDescns;
		model = UiConstants.REF_MODEL.TREEANDGRID.intValue();
	}

	public String getTreeDataUrl() {
		return treeDataUrl;
	}

	public void setTreeDataUrl(String treeDataUrl) {
		this.treeDataUrl = treeDataUrl;
	}

	public String getGridDataUrl() {
		return gridDataUrl;
	}

	public void setGridDataUrl(String gridDataUrl) {
		this.gridDataUrl = gridDataUrl;
	}

	public List<ListColumn> getExtGridColumnDescns() {
		return extGridColumnDescns;
	}

	public void setExtGridColumnDescns(List<ListColumn> extGridColumnDescns) {
		this.extGridColumnDescns = extGridColumnDescns;
	}

	public String getTreePkField() {
		return treePkField;
	}

	public void setTreePkField(String treePkField) {
		this.treePkField = treePkField;
	}

	public Integer getMinWidth() {
		return minWidth;
	}

	public void setMinWidth(Integer minWidth) {
		this.minWidth = minWidth;
	}

}
