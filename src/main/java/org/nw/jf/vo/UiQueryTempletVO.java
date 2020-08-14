package org.nw.jf.vo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * 查询模板Vo
 * 
 * @author xuqc
 * @date 2010-11-29
 */
public class UiQueryTempletVO implements Serializable {
	private static final long serialVersionUID = -5874892547296326710L;
	private String ts;
	private List<QueryConditionVO> conditions;
	private String templateID;
	private String funCode;
	private String nodeKey;

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public List<QueryConditionVO> getConditions() {
		return conditions;
	}

	public void setConditions(List<QueryConditionVO> conditions) {
		this.conditions = conditions;
	}

	public String getFunCode() {
		return funCode;
	}

	public void setFunCode(String funCode) {
		this.funCode = funCode;
	}

	public String getNodeKey() {
		return nodeKey;
	}

	public void setNodeKey(String nodeKey) {
		this.nodeKey = nodeKey;
	}

	public String getTemplateID() {
		return templateID;
	}

	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}

	/**
	 * 深度克隆
	 */
	public UiQueryTempletVO clone() {
		UiQueryTempletVO cloneObj = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			oo.writeObject(this);

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(in);
			cloneObj = (UiQueryTempletVO) oi.readObject();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return cloneObj;
	}
}
