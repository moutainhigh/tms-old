package org.nw.jf.ext.ref.userdefine;

import java.util.List;

import org.nw.jf.UiConstants;


/**
 * 下拉框接口，有些单据的参照想使用下拉框来展现，参照类可以继承该接口
 * 
 * @author xuqc
 * @date 2012-9-27 上午09:15:27
 */
public interface IComboxModel {

	/**
	 * 参照要使用下拉框来展现，默认的下拉框类型是名值对的方式
	 */
	public String DEFAULT_COMBOX_TYPE = UiConstants.COMBOX_TYPE.SX.toString();

	/**
	 * combox的数据源
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-9-27
	 * 
	 */
	public List<String[]> load4Combox();
}
