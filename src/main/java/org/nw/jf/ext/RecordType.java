package org.nw.jf.ext;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.json.JacksonUtils;

/**
 * Ext grid中定义recordType
 * 
 * @author xuqc
 * @date 2011-1-7
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RecordType implements Serializable {

	private static final long serialVersionUID = -4136242840885893753L;
	private String name;
	private String sortName;
	private String value; // 该列的默认值，在模板中设置
	// private String mapping;
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public String getMapping(){
	// return mapping;
	// }
	//
	// public void setMapping(String mapping){
	// this.mapping=mapping;
	// }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return JacksonUtils.writeValueAsString(this);
	}
}
