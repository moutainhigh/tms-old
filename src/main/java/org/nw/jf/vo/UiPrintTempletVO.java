package org.nw.jf.vo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 存储打印模板数据的VO
 */
public class UiPrintTempletVO implements Serializable {
	private static final long serialVersionUID = -2671765656013091735L;
	private String ts;
	private PrintTempletVO printTempletVO;
	private String billTempletId;// 单据模板id
	private String printTempletId;// 打印模板ID
	private String funCode;
	private String nodeKey;
	private String billType;

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public PrintTempletVO getPrintTempletVO() {
		return printTempletVO;
	}

	public void setPrintTempletVO(PrintTempletVO printTempletVO) {
		this.printTempletVO = printTempletVO;
	}

	public String getBillTempletId() {
		return billTempletId;
	}

	public void setBillTempletId(String billTempletId) {
		this.billTempletId = billTempletId;
	}

	public String getPrintTempletId() {
		return printTempletId;
	}

	public void setPrintTempletId(String printTempletId) {
		this.printTempletId = printTempletId;
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

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	/**
	 * 利用序列化和反序列化进行深度克隆
	 * 
	 * @return
	 */
	public UiPrintTempletVO clone() {
		UiPrintTempletVO cloneObj = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			oo.writeObject(this);

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(in);
			cloneObj = (UiPrintTempletVO) oi.readObject();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return cloneObj;
	}
}
