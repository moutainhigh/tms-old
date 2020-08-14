package org.nw.jf.ext;

import java.util.ArrayList;
import java.util.List;

/**
 * 下拉框的数据源对象
 * <ul>
 * <li>var ${"_" + field.genUniqueId() + "_"} = new Ext.data.SimpleStore({</li>
 * <li>fields :['text', 'value'],</li>
 * <li>data : [['是', '1'], ['否', '0']]</li>
 * <li>);</li>
 * </ul>
 * 
 * @author xuqc
 * @date 2012-9-27 下午03:42:30
 */
public class ComboxStore {

	public List<String> fields = new ArrayList<String>();

	public List<String[]> data;

	public String xtype = "arraystore"; // 使用数组作为数据源

	public ComboxStore(List<String[]> data) {
		// 加入关键字
		fields.add("text");
		fields.add("value");
		this.data = data;
		if(this.data == null) {
			this.data = new ArrayList<String[]>();
		}
	}
}
