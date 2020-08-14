package org.nw.jf.ext.ref.win;

import java.util.ArrayList;
import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.ext.ListColumn;

/**
 * 表格结构的参照窗口
 * 
 * @author xuqc
 * @date 2011-1-5
 */
public class GridRefWindow extends AbstractRefWindow {

	private static final long serialVersionUID = -536381420034497258L;

	/**
	 * ֻ�ṩһ�����캯������������ѿ�����Ա����ʵ����Ӧ�ı���
	 * 
	 * @param gridDataUrl
	 * @param extGridColumnDescns
	 */
	public GridRefWindow(String gridDataUrl, List<ListColumn> extGridColumnDescns) {
		this.gridDataUrl = gridDataUrl;
		this.extGridColumnDescns = extGridColumnDescns;
		model = UiConstants.REF_MODEL.GRID.intValue();
	}

	/**
	 * 参照窗口包含的grid的数据加载url 当存在表格时，这是必须的
	 */
	protected String gridDataUrl;

	/**
	 * 存储grid中的列定义
	 */
	protected List<ListColumn> extGridColumnDescns = new ArrayList<ListColumn>();

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

}
