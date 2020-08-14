package org.nw.jf.ext;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;

/**
 * Hidden.js
 * 
 * @author xuqc
 * @date 2010-12-14
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class HiddenField extends Field {

	private static final long serialVersionUID = -2913010130146803590L;
	private String id;
	private String name;
	private String value;

	public HiddenField() {
		xtype = UiConstants.FORM_XTYPE.HIDDEN.toString();
	}

	public HiddenField(String id) {
		this(id, id);
	}

	public HiddenField(String id, String name) {
		this.id = id;
		this.name = name;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}
}
