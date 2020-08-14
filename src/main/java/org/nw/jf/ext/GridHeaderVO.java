package org.nw.jf.ext;

import java.io.Serializable;
import java.util.List;

import org.nw.jf.group.GroupVO;

/**
 * 表格的表头vo，对于动态的表格，需要在加载数据时同时更新表头信息。此vo包括了要更新表头vo所需要的对象
 * 
 * @author xuqc
 * @date 2015-1-15 下午03:09:14
 */
public class GridHeaderVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<ListColumn> columnAry;// 列头定义

	private List<RecordType> recordTypeAry;

	List<List<GroupVO>> groupVOs;// 分组表头的定义信息

	public List<ListColumn> getColumnAry() {
		return columnAry;
	}

	public void setColumnAry(List<ListColumn> columnAry) {
		this.columnAry = columnAry;
	}

	public List<List<GroupVO>> getGroupVOs() {
		return groupVOs;
	}

	public void setGroupVOs(List<List<GroupVO>> groupVOs) {
		this.groupVOs = groupVOs;
	}

	public List<RecordType> getRecordTypeAry() {
		return recordTypeAry;
	}

	public void setRecordTypeAry(List<RecordType> recordTypeAry) {
		this.recordTypeAry = recordTypeAry;
	}

}
