package org.nw.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * 从前台传入的参数，可能参数比较多，使用VO与Service交互更加清晰
 * 
 * @author xuqc
 * @date 2011-5-26
 */
public class ParamVO implements Cloneable {
	String funCode;
	String nodeKey;
	String billType;
	String billId;
	String billIds;
	String vbillno;// 这个的作用和billId一样，只是有时候不想从vbillno转换成billId
	String pk_checkflow; // 待办事项主键
	String templateID;
	/**
	 * 用于查询单据模板的funCode（用于模板的funcode与当前节点funcode不一致的情况）
	 */
	String templateFunCode;
	String tabCode; // 对于表格加载数据，该变量指示当前执行的tabcode，比如表格的加载数据，tabCode表示当前表格所在地tab
	String headerTabCode; // 表头tabcode，可能包括多个
	String bodyTabCode; // 表体tabcode，也可能包含多个
	boolean isBody;
	String srcFunCode;
	String srcNodeKey;
	String srcBillType;
	boolean ulw; // 是否是ulw页面的请求

	boolean reviseflag;// 是否修订发送的请求

	Map<String, Boolean> bodyPaginationMap = new HashMap<String, Boolean>();// 表体是否分页,值是tabcode:true/false
	// 设置扩展参数
	Map<String, Object> attr = new HashMap<String, Object>();
	
	//yaojiie 2015 12 08 将unconfirm_type更改为Integer
	Integer unconfirmType;//反确认类型
	String unconfirmMemo;//反确认说明
	//yaojiie 2015 12 27添加退单类型和退单说明
	Integer ventType;//反确认类型
	String ventMemo;//反确认说明
	
	public String getBillIds() {
		return billIds;
	}

	public void setBillIds(String billIds) {
		this.billIds = billIds;
	}

	public String getVbillno() {
		return vbillno;
	}

	public void setVbillno(String vbillno) {
		this.vbillno = vbillno;
	}

	/**
	 * @return the billType
	 */
	public String getBillType() {
		return billType;
	}

	/**
	 * @param billType
	 *            the billType to set
	 */
	public void setBillType(String billType) {
		this.billType = billType;
	}

	/**
	 * @return the billId
	 */
	public String getBillId() {
		return billId;
	}

	/**
	 * @param billId
	 *            the billId to set
	 */
	public void setBillId(String billId) {
		this.billId = billId;
	}

	/**
	 * @return the templateID
	 */
	public String getTemplateID() {
		return templateID;
	}

	/**
	 * @param templateID
	 *            the templateID to set
	 */
	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}

	public String getHeaderTabCode() {
		return headerTabCode;
	}

	public void setHeaderTabCode(String headerTabcode) {
		this.headerTabCode = headerTabcode;
	}

	public String getBodyTabCode() {
		return bodyTabCode;
	}

	public void setBodyTabCode(String bodyTabcode) {
		this.bodyTabCode = bodyTabcode;
	}

	public String getTabCode() {
		return tabCode;
	}

	public void setTabCode(String tabCode) {
		this.tabCode = tabCode;
	}

	/**
	 * @return the isBody
	 */
	public boolean isBody() {
		return isBody;
	}

	/**
	 * @param isBody
	 *            the isBody to set
	 */
	public void setBody(boolean isBody) {
		this.isBody = isBody;
	}

	/**
	 * @return the funCode
	 */
	public String getFunCode() {
		return funCode;
	}

	/**
	 * @param funCode
	 *            the funCode to set
	 */
	public void setFunCode(String funCode) {
		this.funCode = funCode;
	}

	/**
	 * @return the nodeKey
	 */
	public String getNodeKey() {
		return nodeKey;
	}

	/**
	 * @param nodeKey
	 *            the nodeKey to set
	 */
	public void setNodeKey(String nodeKey) {
		this.nodeKey = nodeKey;
	}

	public String getSrcFunCode() {
		return srcFunCode;
	}

	public Map<String, Boolean> getBodyPaginationMap() {
		return bodyPaginationMap;
	}

	public void setBodyPaginationMap(Map<String, Boolean> bodyPaginationMap) {
		this.bodyPaginationMap = bodyPaginationMap;
	}

	public void setSrcFunCode(String srcFunCode) {
		this.srcFunCode = srcFunCode;
	}

	public String getSrcNodeKey() {
		return srcNodeKey;
	}

	public void setSrcNodeKey(String srcNodeKey) {
		this.srcNodeKey = srcNodeKey;
	}

	public String getSrcBillType() {
		return srcBillType;
	}

	public void setSrcBillType(String srcBillType) {
		this.srcBillType = srcBillType;
	}

	public String getPk_checkflow() {
		return pk_checkflow;
	}

	public void setPk_checkflow(String pk_checkflow) {
		this.pk_checkflow = pk_checkflow;
	}

	public boolean isUlw() {
		return ulw;
	}

	public void setUlw(boolean ulw) {
		this.ulw = ulw;
	}

	public boolean isReviseflag() {
		return reviseflag;
	}

	public void setReviseflag(boolean reviseflag) {
		this.reviseflag = reviseflag;
	}

	public String toString() {
		return "ParamVO [funCode=" + funCode + ", nodeKey=" + nodeKey + ", billType=" + billType + ", billId=" + billId
				+ ", pk_checkflow=" + pk_checkflow + ", templateID=" + templateID + ", tabCode=" + tabCode
				+ ", headerTabCode=" + headerTabCode + ", bodyTabCode=" + bodyTabCode + ", isBody=" + isBody
				+ ", srcFunCode=" + srcFunCode + ", srcNodeKey=" + srcNodeKey + ", srcBillType=" + srcBillType
				+ ", ulw=" + ulw + "]";
	}

	public ParamVO clone() {
		try {
			return (ParamVO) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getTemplateFunCode() {
		return templateFunCode;
	}

	public void setTemplateFunCode(String templateFunCode) {
		this.templateFunCode = templateFunCode;
	}

	public Map<String, Object> getAttr() {
		return attr;
	}

	public void setAttr(Map<String, Object> attr) {
		this.attr = attr;
	}


	public Integer getUnconfirmType() {
		return unconfirmType;
	}

	public void setUnconfirmType(Integer unconfirmType) {
		this.unconfirmType = unconfirmType;
	}

	public String getUnconfirmMemo() {
		return unconfirmMemo;
	}

	public void setUnconfirmMemo(String unconfirmMemo) {
		this.unconfirmMemo = unconfirmMemo;
	}

	public Integer getVentType() {
		return ventType;
	}

	public void setVentType(Integer ventType) {
		this.ventType = ventType;
	}

	public String getVentMemo() {
		return ventMemo;
	}

	public void setVentMemo(String ventMemo) {
		this.ventMemo = ventMemo;
	}
	
	

	// @Override
	// public ParamVO clone() {
	// try {
	// return (ParamVO) BeanUtils.cloneBean(this);
	// } catch(Exception e) {
	// throw new RuntimeException(e);
	// }
	// }

}
