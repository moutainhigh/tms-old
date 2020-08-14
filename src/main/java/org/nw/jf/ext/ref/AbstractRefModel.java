package org.nw.jf.ext.ref;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;
import org.nw.jf.ext.ref.win.AbstractRefWindow;
import org.nw.jf.web.AbsRefController;

/**
 * 参照是一个特殊的类，包括model的特性也包括controller的特性。由于java的extends只支持单继承。
 * 让该类继承AbstractBaseConstroller,对于field的特性，则直接拷贝到该类。 参照对象对应的model 一个参照主要包含3种数据
 * valueField:用于存储最终的数据值，一般是表的主键 codeField:当鼠标双击后，参照显示的值 textField,参照失去焦点后显示的值
 * 注意：1、只有声明了@JsonProperty的字段才会被序列化 值为null的属性不再被输出。
 * 2、这个类声明了一些参照所用到的字段，但是不需要被继承，子类只继承BaseRefModel中的方法
 * 
 * @author xuqc
 * @date 2010-8-31
 */
@JsonAutoDetect(JsonMethod.NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public abstract class AbstractRefModel extends AbsRefController implements Serializable {
	private static final long serialVersionUID = 8417756019292153060L;
	// 将当前的model传入

	@JsonProperty
	protected String refName;// 参照名称
	@JsonProperty
	protected Boolean isMulti; // 是否多选
	@JsonProperty
	protected Boolean isBodyMulti; // 是否表体多选，表体的多选需要自己写业务操作
	@JsonProperty
	protected Boolean commonUse; // 是否显示我的常用tab

	@JsonProperty
	protected Boolean isRemoteFilter;// 是否后台过虑

	/************ textField的属性 开始 *********************/
	@JsonProperty
	protected String renderTo;
	@JsonProperty
	protected String applyTo;// 区别于renderTo，如果是多选参照，这里的值如：pk_cycle,pk_cycle_name
	@JsonProperty
	protected Boolean selectOnFocus;
	@JsonProperty
	protected Boolean allowBlank;
	@JsonProperty
	protected String xtype = UiConstants.FORM_XTYPE.HEADERREFFIELD.toString();
	@JsonProperty
	protected String id;
	@JsonProperty
	protected String name;
	@JsonProperty
	protected String fieldLabel;
	@JsonProperty
	protected String labelStyle;
	@JsonProperty
	protected String itemCls; // field外层div的css
	@JsonProperty
	protected Integer width = UiConstants.HEADERGRID_DEFAULT_WIDTH;
	// @JsonProperty
	// protected Boolean disabled;
	@JsonProperty
	protected Boolean readOnly;
	@JsonProperty
	protected Boolean newlineflag; // 布局到下一行

	public Boolean getNewlineflag() {
		return newlineflag;
	}

	public void setNewlineflag(Boolean newlineflag) {
		this.newlineflag = newlineflag;
	}

	/**
	 * 是否可修订的标志，修订是指在修订按钮后，该field是否可编辑。 只有为该标识为true的field才能编辑
	 */
	@JsonProperty
	protected Boolean reviseflag;
	@JsonProperty
	protected Integer colspan;
	@JsonProperty
	protected Boolean hidden;
	@JsonProperty
	protected Boolean hideLabel;
	@JsonProperty
	protected String value;
	// 模板中【自定义项一】配置的js脚本，用于监听Field的change事件
	// 需要注意的是脚本不能使用双引号，js脚本可以使用单引号代替
	@JsonProperty
	protected String script;

	@JsonProperty
	protected String beforeEditScript;

	@JsonProperty
	protected String vtype;
	/************ textField的属性 结束 *********************/

	/**
	 * 查询模板的参照根据改值返回code\pk\name<br/>
	 * 0：code 1：name 2：pk
	 */
	@JsonProperty
	protected Integer returnType;

	/**
	 * 模板中设置的参照域所参考的pk域，必须在读取模板时实例化
	 */
	@JsonProperty
	protected String idcolname;

	/**
	 * 参照所在的单据模板ID 注意是单据模板，对于查询模板，不需要查询公式了
	 */
	@JsonProperty
	protected String pkBilltemplet;

	/**
	 * 单据模板中定义该参照的主键，必须在读取单据模板时实例化 注意是单据模板，对于查询模板，不需要查询公式了
	 */
	@JsonProperty
	protected String pkBilltempletB;

	/**
	 * 是否包含需要执行的编辑公式，前台会检验这个变量，如果为true则发送请求，否则不发送
	 */
	@JsonProperty
	protected Boolean hasEditformula;

	/**
	 * 参照的编辑公式,一般在模板中已经设置，如果参照作为一个单独的组件使用，则需要在子类赋值
	 */
	@JsonProperty
	protected String editformula;

	/**
	 * 参照的显示公式,一般在模板中已经设置，如果参照作为一个单独的组件使用，则需要在子类赋值
	 */
	@JsonProperty
	protected String loadformula;// 参照的显示公式

	/**
	 * 执行编辑公式的URL，一般不需要被覆盖(在子类重新赋值)，除非自己写了方法
	 */
	@JsonProperty
	protected String editFormulaUrl = "execFormula.json";

	/**
	 * 参照窗口,必须在子类实例化
	 */
	@JsonProperty
	protected AbstractRefWindow refWindow;
	@JsonProperty
	protected String pkField;
	@JsonProperty
	protected String codeField;
	@JsonProperty
	protected String nameField;

	// 助记码，不需要序列化
	protected String[] mnecode;

	// 助记码的查询方式，like和=号查询,默认是等号查询
	private String[] mnecodeSearchType;

	/**
	 * 能否支持直接录入
	 */
	@JsonProperty
	protected Boolean fillinable;
	/**
	 * 鼠标移开的时候显示code，一般显示name
	 */
	@JsonProperty
	protected Boolean showCodeOnBlur;

	/**
	 * 鼠标移入的时候是否显示code，特殊情况显示name
	 */
	@JsonProperty
	protected Boolean showCodeOnFocus;

	/**
	 * 当参照获得焦点时,根据主键返回VO，必须在子类重新赋值
	 */
	@JsonProperty
	protected String getByPkUrl;
	/**
	 * 当参照失去焦点时，先根据code查询VO，并设置到页面上，再执行编辑公式，必须在子类重新赋值
	 */
	@JsonProperty
	protected String getByCodeUrl;

	/**
	 * @return the idcolname
	 */
	public String getIdcolname() {
		return idcolname;
	}

	/**
	 * @param idcolname
	 *            the idcolname to set
	 */
	public void setIdcolname(String idcolname) {
		this.idcolname = idcolname;
	}

	/**
	 * @return the pkBilltemplet
	 */
	public String getPkBilltemplet() {
		return pkBilltemplet;
	}

	/**
	 * @param pkBilltemplet
	 *            the pkBilltemplet to set
	 */
	public void setPkBilltemplet(String pkBilltemplet) {
		this.pkBilltemplet = pkBilltemplet;
	}

	/**
	 * @return the pkBilltempletB
	 */
	public String getPkBilltempletB() {
		return pkBilltempletB;
	}

	/**
	 * @param pkBilltempletB
	 *            the pkBilltempletB to set
	 */
	public void setPkBilltempletB(String pkBilltempletB) {
		this.pkBilltempletB = pkBilltempletB;
	}

	public Boolean getHasEditformula() {
		return hasEditformula;
	}

	public void setHasEditformula(Boolean hasEditformula) {
		this.hasEditformula = hasEditformula;
	}

	/**
	 * @return the editformula
	 */
	public String getEditformula() {
		return editformula;
	}

	/**
	 * @param editformula
	 *            the editformula to set
	 */
	public void setEditformula(String editformula) {
		this.editformula = editformula;
	}

	/**
	 * @return the loadformula
	 */
	public String getLoadformula() {
		return loadformula;
	}

	/**
	 * @param loadformula
	 *            the loadformula to set
	 */
	public void setLoadformula(String loadformula) {
		this.loadformula = loadformula;
	}

	/**
	 * @return the editFormulaUrl
	 */
	public String getEditFormulaUrl() {
		return editFormulaUrl;
	}

	/**
	 * @param editFormulaUrl
	 *            the editFormulaUrl to set
	 */
	public void setEditFormulaUrl(String editFormulaUrl) {
		this.editFormulaUrl = editFormulaUrl;
	}

	/**
	 * @return the pkField
	 */
	public String getPkField() {
		return pkField;
	}

	/**
	 * @param pkField
	 *            the pkField to set
	 */
	public void setPkField(String pkField) {
		this.pkField = pkField;
	}

	/**
	 * @return the codeField
	 */
	public String getCodeField() {
		return codeField;
	}

	public String getNameField() {
		return nameField;
	}

	public void setNameField(String nameField) {
		this.nameField = nameField;
	}

	/**
	 * @param codeField
	 *            the codeField to set
	 */
	public void setCodeField(String codeField) {
		this.codeField = codeField;
	}

	public String getGetByPkUrl() {
		return getByPkUrl;
	}

	public void setGetByPkUrl(String getByPkUrl) {
		this.getByPkUrl = getByPkUrl;
	}

	public String getGetByCodeUrl() {
		return getByCodeUrl;
	}

	public void setGetByCodeUrl(String getByCodeUrl) {
		this.getByCodeUrl = getByCodeUrl;
	}

	/**
	 * @param refWindow
	 *            the refWindow to set
	 */
	public void setRefWindow(AbstractRefWindow refWindow) {
		this.refWindow = refWindow;
	}

	public Boolean getSelectOnFocus() {
		return selectOnFocus;
	}

	public void setSelectOnFocus(Boolean selectOnFocus) {
		this.selectOnFocus = selectOnFocus;
	}

	public Boolean getAllowBlank() {
		return allowBlank;
	}

	public void setAllowBlank(Boolean allowBlank) {
		this.allowBlank = allowBlank;
	}

	/**
	 * @return the xtype
	 */
	public String getXtype() {
		return xtype;
	}

	/**
	 * @param xtype
	 *            the xtype to set
	 */
	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the fieldLabel
	 */
	public String getFieldLabel() {
		return fieldLabel;
	}

	/**
	 * @param fieldLabel
	 *            the fieldLabel to set
	 */
	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	/**
	 * @return the labelStyle
	 */
	public String getLabelStyle() {
		return labelStyle;
	}

	/**
	 * @param labelStyle
	 *            the labelStyle to set
	 */
	public void setLabelStyle(String labelStyle) {
		this.labelStyle = labelStyle;
	}

	/**
	 * @return the itemCls
	 */
	public String getItemCls() {
		return itemCls;
	}

	/**
	 * @param itemCls
	 *            the itemCls to set
	 */
	public void setItemCls(String itemCls) {
		this.itemCls = itemCls;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getReviseflag() {
		return reviseflag;
	}

	public void setReviseflag(Boolean reviseflag) {
		this.reviseflag = reviseflag;
	}

	/**
	 * @return the colspan
	 */
	public Integer getColspan() {
		return colspan;
	}

	/**
	 * @param colspan
	 *            the colspan to set
	 */
	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getHideLabel() {
		return hideLabel;
	}

	public void setHideLabel(Boolean hideLabel) {
		this.hideLabel = hideLabel;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script
	 *            the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * @return the vtype
	 */
	public String getVtype() {
		return vtype;
	}

	/**
	 * @param vtype
	 *            the vtype to set
	 */
	public void setVtype(String vtype) {
		this.vtype = vtype;
	}

	public String getRenderTo() {
		return renderTo;
	}

	public void setRenderTo(String renderTo) {
		this.renderTo = renderTo;
	}

	/**
	 * @return the beforeEditScript
	 */
	public String getBeforeEditScript() {
		return beforeEditScript;
	}

	/**
	 * @param beforeEditScript
	 *            the beforeEditScript to set
	 */
	public void setBeforeEditScript(String beforeEditScript) {
		this.beforeEditScript = beforeEditScript;
	}

	public String[] getMnecode() {
		return mnecode;
	}

	public void setMnecode(String[] mnecode) {
		this.mnecode = mnecode;
	}

	public Integer getReturnType() {
		return returnType;
	}

	public void setReturnType(Integer returnType) {
		this.returnType = returnType;
	}

	/**
	 * 返回助记码查询符号，只有两种值 = 和like<br/>
	 * 若返回null，则默认都是等号<br/>
	 * 如：<br/>
	 * return new String[]{"like","="};
	 * 
	 * @return
	 * @author xuqc
	 * @date 2011-12-26
	 * 
	 */
	public String[] getMnecodeSearchType() {
		return mnecodeSearchType;
	}

	public void setMnecodeSearchType(String[] mnecodeSearchType) {
		this.mnecodeSearchType = mnecodeSearchType;
	}

	public void setShowCodeOnBlur(Boolean showCodeOnBlur) {
		this.showCodeOnBlur = showCodeOnBlur;
	}

	public Boolean isShowCodeOnBlur() {
		return showCodeOnBlur;
	}

	public void setFillinable(Boolean fillinable) {
		this.fillinable = fillinable;
	}

	public void setShowCodeOnFocus(Boolean showCodeOnFocus) {
		this.showCodeOnFocus = showCodeOnFocus;
	}

	public Boolean isShowCodeOnFocus() {
		return showCodeOnFocus;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public Boolean getIsMulti() {
		return isMulti;
	}

	public void setIsMulti(Boolean isMulti) {
		this.isMulti = isMulti;
	}

	public Boolean getIsBodyMulti() {
		return isBodyMulti;
	}

	public void setIsBodyMulti(Boolean isBodyMulti) {
		this.isBodyMulti = isBodyMulti;
	}

	public String getApplyTo() {
		return applyTo;
	}

	public void setApplyTo(String applyTo) {
		this.applyTo = applyTo;
	}

	public Boolean getCommonUse() {
		return commonUse;
	}

	public void setCommonUse(Boolean commonUse) {
		this.commonUse = commonUse;
	}

	public final Boolean getIsRemoteFilter() {
		return isRemoteFilter;
	}

	public final void setIsRemoteFilter(Boolean isRemoteFilter) {
		this.isRemoteFilter = isRemoteFilter;
	}
}
