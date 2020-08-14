package org.nw.jf.ext.ref;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.ext.ref.win.AbstractRefWindow;
import org.nw.jf.web.BaseRefService;
import org.nw.json.JacksonUtils;
import org.nw.web.utils.WebUtils;

/**
 * 参照是一个特殊的类，包括model的特性也包括controller的特性。由于java的extends只支持单继承。
 * 让该类继承AbstractBaseConstroller,对于field的特性，则直接拷贝到该类。 参照对象对应的model 一个参照主要包含3种数据
 * valueField:用于存储最终的数据值，一般是表的主键 codeField:当鼠标双击后，参照显示的值 textField,参照失去焦点后显示的值
 * 注意：1、只有声明了@JsonProperty的字段才会被序列化 值为null的属性不再被输出 <br/>
 * 2、子类只继承这个类的方法
 * 
 * @author xuqc
 * @date 2010-8-31
 */
@JsonAutoDetect(JsonMethod.NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public abstract class BaseRefModel extends AbstractRefModel {
	private static final long serialVersionUID = 8417756019292153060L;
	// 将当前的model传入
	protected BaseRefService baseRefService = new BaseRefService(this);

	public BaseRefModel() {
		refWindow = this.genRefWindow(); // 注意这里使用gen方法，而不是get方法。get方法返回已经创建的对象
		pkField = this.getPkFieldCode();
		codeField = this.getCodeFieldCode();
		nameField = this.getNameFieldCode();
		getByPkUrl = this.getByPkUrl();
		getByCodeUrl = this.getByCodeUrl();

		// 设置鼠标移开时是否出发blur事件，之类可继承
		fillinable = isFillinable();
		showCodeOnFocus = isShowCodeOnFocus();
		refName = getRefName();
		isBodyMulti = isBodyMulti();
		commonUse = isCommonUse();
		isRemoteFilter = isRemoteFilter();
		isMulti = getIsMulti();// 如果表头的参照希望多选，那么子类可以继承这个方法返回true
	}

	public BaseRefModel(String refName) {
		this.refName = refName;
	}

	/************* 可能需要被继承的类 *********************/
	/**
	 * 表体参照是否可以多选
	 */
	public Boolean isBodyMulti() {
		return isBodyMulti;
	}

	/**
	 * 子类继承，参照是否可以直接录入
	 * 
	 * @return
	 */
	public Boolean isFillinable() {
		return fillinable;
	}

	/**
	 * 子类继承，当参照获得焦点时，是否显示code
	 */
	public Boolean isShowCodeOnFocus() {
		return showCodeOnFocus;
	}

	/**
	 * 子类继承该方法，表示树型参照是否使用远程过滤
	 * 
	 * @return
	 */
	public Boolean isRemoteFilter() {
		return isRemoteFilter;
	}

	/**
	 * 子类继承该方法，是否显示常用页签
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-6-18
	 * 
	 */
	public Boolean isCommonUse() {
		return commonUse;
	}

	/**
	 * 返回参照名称,若继承该类则参照窗口会优先使用，否则从nw_refinfo中读取
	 * 
	 * @return the refName
	 */
	public String getRefName() {
		return refName;
	}

	/**
	 * 用于从档案表中查询数据的一个参数字段，对应NW_DEFDOC表中的pk_defdoclist，在nw_bdinfo中存储该字段的值
	 * 
	 * @return
	 */
	public String getPkDefdef() {
		return "pk_defdoclist";
	}

	/**
	 * 返回主键字段
	 * 
	 * @return
	 */
	public abstract String getPkFieldCode();

	/**
	 * 返回编码所在的字段
	 * 
	 * @return
	 */
	public abstract String getCodeFieldCode();

	/**
	 * 返回name所在的字段
	 * 
	 * @return
	 */
	public abstract String getNameFieldCode();

	/**
	 * 返回参照窗口的配置信息
	 * 
	 * @return
	 */
	public abstract AbstractRefWindow genRefWindow();

	/**
	 * 提供一个public方法，其他地方可以对该变量进行修改
	 * 
	 * @return
	 */
	final public AbstractRefWindow getRefWindow() {
		return this.refWindow;
	}

	/**
	 * 根据pk查询code的url,返回一个默认的Url，之类只需继承getCodeByPk方法即可
	 * 
	 * @return
	 */
	final public String getByPkUrl() {
		return WebUtils.getContextPath() + getRequestMappingValue() + "/getByPk.do";
	}

	/**
	 * 根据code查询pk的url,返回一个默认的Url，之类只需继承getPkByCode方法即可
	 * 
	 * @return
	 */
	final public String getByCodeUrl() {
		return WebUtils.getContextPath() + getRequestMappingValue() + "/getByCode.do";
	}

	/*
	 * 这里使用fastjson进行转换会出错？TODO (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// 对renderer和summaryRenderer的处理比较特殊，转化后前台会把它作为一个对象去处理
		// FIXME
		// 更好的方式可能是使用一个注解，当遇到这两个字符时，转化后不加引号，但是这种方式需要修改到jackson的源代码，注意ListColumn有类似的代码
		String result = JacksonUtils.writeValueAsString(this);
		// 对于renderer属性，不能加入双引号
		int index = result.indexOf("renderer");
		if(index > -1) {
			// 存在renderer属性，将其值的前后引号去掉
			String prefix = result.substring(0, index + 9);// 注意：这里需要包括renderer后面的引号
			String subfix = result.substring(index + 9);
			subfix = subfix.replaceFirst("\"", "").replaceFirst("\"", ""); // 替换第一个和第二个引号
			result = prefix + subfix;
		}
		index = result.indexOf("summaryRenderer");
		if(index > -1) {
			// 存在summaryRenderer属性，将其值的前后引号去掉
			String prefix = result.substring(0, index + 16);// 注意：这里需要包括summaryRenderer后面的引号
			String subfix = result.substring(index + 16);
			subfix = subfix.replaceFirst("\"", "").replaceFirst("\"", ""); // 替换第一个和第二个引号
			result = prefix + subfix;
		}
		return result;
	}
}
