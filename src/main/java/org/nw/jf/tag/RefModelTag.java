package org.nw.jf.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.ext.ref.BaseRefModel;


/**
 * refModel tag,for easy use in everywhere
 * 
 * @author xuqc
 * @date 2013-3-25
 */
public class RefModelTag extends TagSupport {

	private String variableName;
	private String fieldLabel;

	private String id;

	private String refClazz;

	// a pk vlaue or a string value like {pk:'123',code:'123',name:''}
	private String defaultValue;

	private Boolean isMulti = false;

	private Integer returnType = 2;

	private Integer colspan = 1;

	private String renderTo;

	/**
	 * 
	 */
	private static final long serialVersionUID = -7370365521480774320L;

	public int doEndTag() throws JspException {
		int result = super.doEndTag();
		if(StringUtils.isBlank(refClazz) || StringUtils.isBlank(id)) {
			return result;
		}

		try {
			Class<?> clazz = Class.forName(refClazz);
			StringBuffer sb = new StringBuffer();
			sb.append("<script type='text/javascript'>");
			if(StringUtils.isNotBlank(variableName)) {
				sb.append("var ").append(variableName).append("=");
			}

			BaseRefModel refModel = (BaseRefModel) clazz.newInstance();
			refModel.setId(id);
			refModel.setColspan(colspan);
			refModel.setIsMulti(isMulti);
			refModel.setReturnType(returnType);
			refModel.setValue(defaultValue);
			if(StringUtils.isNotBlank(fieldLabel)) {
				refModel.setFieldLabel(fieldLabel);
			}
			refModel.setRenderTo(renderTo);
			sb.append("Ext.create(" + refModel + ");");
			sb.append("</script>");
			this.pageContext.getOut().write(sb.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public final String getVariableName() {
		return variableName;
	}

	public final void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean getIsMulti() {
		return isMulti;
	}

	public void setIsMulti(Boolean isMulti) {
		this.isMulti = isMulti;
	}

	public Integer getReturnType() {
		return returnType;
	}

	public void setReturnType(Integer returnType) {
		this.returnType = returnType;
	}

	public Integer getColspan() {
		return colspan;
	}

	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRefClazz() {
		return refClazz;
	}

	public void setRefClazz(String refClazz) {
		this.refClazz = refClazz;
	}

	public final String getRenderTo() {
		return renderTo;
	}

	public final void setRenderTo(String renderTo) {
		this.renderTo = renderTo;
	}
}
