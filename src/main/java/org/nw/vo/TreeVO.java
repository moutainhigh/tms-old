package org.nw.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * EXT Tree 对象
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TreeVO implements Serializable {

	private static final long serialVersionUID = -388914004365487399L;

	public static final String DEFAULT_ROOT_ID = "__root"; // 树的根节点默认id请统????置为__root
	/************ 以下三个字段是必须的 ********************/
	private String id;
	private String code;
	private String text;
	/****************************************************/

	private String dispCode;
	/**
	 * 一般树的text显示的是code+text，hiddenText显示真正的text，目前用于参照域
	 */
	private String hiddenText;
	private Boolean leaf = false;
	private Boolean disabled = false;
	private Boolean expanded = false;
	private Boolean locked;
	private String cls;
	private String iconCls;
	// private String href;//在页面上使用event.stopEvent();阻止默认的链接
	private String hrefTarget;
	private String hrefPrefix;
	private String listeners;
	private Boolean checked;
	private String url; // 存放链接Url
	private List<TreeVO> children;
	private int type = 0;// 0不使用该属????在tab页中打开??作为js代码执行??弹出新窗??
	private String qtip;
	private String addition;
	private Boolean draggable = false;
	private Boolean hidden = false;
	private Map<String, Object> properties = new HashMap<String, Object>(); // 额外的属性，可以使用node.attributes['properties'].mapKey访问

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public String getAddition() {
		return addition;
	}

	public void setAddition(String addition) {
		this.addition = addition;
	}

	public String getQtip() {
		return qtip;
	}

	public void setQtip(String qtip) {
		this.qtip = qtip;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Boolean getLeaf() {
		return leaf;
	}

	public void setLeaf(Boolean leaf) {
		this.leaf = leaf;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean getExpanded() {
		return expanded;
	}

	public void setExpanded(Boolean expanded) {
		this.expanded = expanded;
	}

	public String getCls() {
		return cls;
	}

	public void setCls(String cls) {
		this.cls = cls;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}

	// public String getHref(){
	// return href;
	// }
	//
	// public void setHref(String href){
	// this.href=href;
	// }

	public void addProperty(String key, Object value) {
		this.getProperties().put(key, value);
	}

	public void removeProperty(String key) {
		getProperties().remove(key);
	}

	public Object getProperty(String key) {
		Object obj = getProperties().get(key);
		if(obj == null) {
			return "";
		}
		return obj;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String getHrefTarget() {
		return hrefTarget;
	}

	public void setHrefTarget(String hrefTarget) {
		this.hrefTarget = hrefTarget;
	}

	public String getListeners() {
		return listeners;
	}

	public void setListeners(String listeners) {
		this.listeners = listeners;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public List<TreeVO> getChildren() {
		return children;
	}

	public void setChildren(List<TreeVO> children) {
		this.children = children;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 添加子节点
	 * 
	 * @param childNode
	 */
	public void add(TreeVO childNode) {
		if(this.children == null) {
			this.children = new ArrayList<TreeVO>(1);
		}
		children.add(childNode);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Boolean getDraggable() {
		return draggable;
	}

	public void setDraggable(Boolean draggable) {
		this.draggable = draggable;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public String getHrefPrefix() {
		return hrefPrefix;
	}

	public void setHrefPrefix(String hrefPrefix) {
		this.hrefPrefix = hrefPrefix;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHiddenText() {
		return hiddenText;
	}

	public void setHiddenText(String hiddenText) {
		this.hiddenText = hiddenText;
	}

	public String getDispCode() {
		return dispCode;
	}

	public void setDispCode(String dispCode) {
		this.dispCode = dispCode;
	}

}
