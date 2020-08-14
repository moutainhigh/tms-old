package org.nw.jf.ext.ref.userdefine;

import java.io.Serializable;

/**
 * 自定义参照<br/>
 * 支持多选的下拉框，NC默认没有支持多选的下拉框，使用自定义参照实现，子类实现该接口<br/>
 * 设置方法<br/>
 * 模板中将【数据类型】设置成参照，【类型设置】中设置成<子类的完整类名><br/>
 * 实现可序列化的接口,可能是要缓存的对象,有些缓存机制需要对象是可序列化的
 * 
 * @author xuqc
 * @date 2011-12-22
 */
public interface IMultiSelect extends Serializable {

	/**
	 * 返回选项值，支持NC的所有下拉框的设置方式<br/>
	 * 如：S,职工代表大会,干部职工代表座谈会,办事公开平台,公司内网<br/>
	 * 更多的方式请查看UiTempletUtils.getSelectValues
	 * 
	 * @return
	 * @author xuqc
	 * @date 2011-12-22
	 * 
	 */
	public String getConsult_code();
}
