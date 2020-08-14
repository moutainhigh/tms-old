package org.nw.jf.ext;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.json.JacksonUtils;

/**
 * 对应Ext中Field.js中的定义 值为null的属性不再被输出
 * 
 * @author xuqc
 * @date 2010-8-26
 * @version $Revision$
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Field implements Serializable, Cloneable {

	private static final long serialVersionUID = -9053657592002612855L;
	protected String id;
	protected String name;
	protected String fieldLabel;
	protected String labelStyle;
	protected String itemCls; // fielddivcss
	protected String xtype;

	protected Integer width;
	// protected Integer anchor=UiConstants.DEFAULT_ANCHOR;
	// 使用模板中的统一设置，统一管理，同时减少io
	// protected String anchor=UiConstants.DEFAULT_ANCHOR+"%";

	// 使用disabled 代替,disabled时使用getFieldValue无法读取到其值
	// 修改ext-all-debug.js的源码，提交disabled数据，注意ext-all.js也需要修改
	// 不再使用readOnly,因为样式不好，同时可操作性不好
	// 2011-10-27 disabled在IE下的样式没办法改变,fuck IE
	protected Boolean readOnly;
	// protected Boolean disabled;

	/**
	 * 是否可修订的标志，修订是指在修订按钮后，该field是否可编辑。 只有为该标识为true的field才能编辑
	 */
	protected Boolean reviseflag;

	protected Integer colspan;
	protected Boolean hidden;
	protected Boolean hideLabel;
	protected String value;
	// 模板中【自定义项一】配置的js脚本，用于监听Field的change事件
	// 需要注意的是脚本不能使用双引号，js脚本可以使用单引号代替
	protected String script;

	/**
	 * 参照所在的单据模板ID 注意是单据模板，对于查询模板，不需要查询公式了
	 */
	protected String pkBilltemplet;

	/**
	 * 单据模板中定义该参照的主键，必须在读取单据模板时实例化 注意是单据模板，对于查询模板，不需要查询公式了
	 */
	protected String pkBilltempletB;

	/**
	 * 是否包含需要执行的编辑公式，前台会检验这个变量，如果为true则发送请求，否则不发送
	 */
	protected Boolean hasEditformula;

	protected String renderTo;

	protected String applyTo;// 区别于renderTo

	protected Boolean newlineflag;// 是否布局到下一行

	public Boolean getNewlineflag() {
		return newlineflag;
	}

	public void setNewlineflag(Boolean newlineflag) {
		this.newlineflag = newlineflag;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getLabelStyle() {
		return labelStyle;
	}

	public void setLabelStyle(String labelStyle) {
		this.labelStyle = labelStyle;
	}

	public String getItemCls() {
		return itemCls;
	}

	public void setItemCls(String itemCls) {
		this.itemCls = itemCls;
	}

	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	public Integer getWidth() {
		return width;
	}

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

	public Integer getColspan() {
		return colspan;
	}

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getPkBilltemplet() {
		return pkBilltemplet;
	}

	public void setPkBilltemplet(String pkBilltemplet) {
		this.pkBilltemplet = pkBilltemplet;
	}

	public String getPkBilltempletB() {
		return pkBilltempletB;
	}

	public void setPkBilltempletB(String pkBilltempletB) {
		this.pkBilltempletB = pkBilltempletB;
	}

	public Boolean getHasEditformula() {
		return hasEditformula;
	}

	public void setHasEditformula(Boolean hasEditformula) {
		this.hasEditformula = hasEditformula;
	}

	public String getRenderTo() {
		return renderTo;
	}

	public void setRenderTo(String renderTo) {
		this.renderTo = renderTo;
	}

	public String getApplyTo() {
		return applyTo;
	}

	public void setApplyTo(String applyTo) {
		this.applyTo = applyTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return JacksonUtils.writeValueAsString(this);
	}

	public Field clone() {
		try {
			return (Field) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
