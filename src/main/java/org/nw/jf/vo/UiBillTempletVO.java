package org.nw.jf.vo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储模板数据的VO，模板数据将被缓存
 * 
 * @author xuqc
 * @date 2010-11-24
 */
public class UiBillTempletVO implements Serializable {
	private static final long serialVersionUID = -4965732220370404465L;
	// 模板主表ts信息
	private String ts;
	// 模板信息
	private List<BillTempletBVO> fieldVOs;
	// 表头表格的字段名称，主要用于定义表头表格的字段顺序，目前模板还不支持表头表格和表头表单的顺序不同
	private String[] HeaderListItemKey;
	// 模板Tab信息
	private List<BillTempletTVO> tabVOs;
	// 模板ID
	private String templateID;
	// 节点编码
	private String funCode;
	// 节点标识
	private String nodeKey;
	// 公司主键
	private String pk_corp;
	// 用户主键
	private String pk_user;
	// freeMarker模板文件
	private String fmTemplate;
	// js命名空间
	private String moduleName;
	// // application将被渲染的tab
	// private String tabId;
	// // true：使用iframe加载app，false：使用autoLoad加载app
	// private boolean standalone;

	private String headerTabCode = ""; // 表头的tabCode，多个tab使用,分隔

	private String firstHeaderTabCode = ""; // 表头的第一个tabcode,会作为表头表格的id

	private String bodyTabCode = ""; // 表体的tabCode，多个tabCode使用,分隔

	private String headerPkField; // 表头的主键

	private boolean headerGridImmediatelyLoad; // 表头是否在渲染后马上加载数据？考虑到存在billId时，直接切换到卡片页，此时不加载表头数据。

	private Map<String, String> childrenPkFieldMap;// 表体的tabCode与pkField的map，用于生成表格时设置pkField

	private String billType; // 单据类型

	private String billId; // 某个单据的ID，当单据ID存在时，直接打开卡片页
	
	private String billIds; // 待打开的单据数组，这里用","分割，当billIds存在时，直接加载列表页，并且加载对应的数据。

	private String pk_checkflow; // 待办事项主键

	/**
	 * 表头和表尾中的所有参照对象，以<itemKey,BillTempletBVO>存储
	 */
	private Map<String, BillTempletBVO> headerRefMap = new HashMap<String, BillTempletBVO>();

	/**
	 * 表头和表尾中所有包括默认值的对象，以<itemKey,defaultValue>存储
	 */
	private Map<String, Object> headerDefaultValueMap = new HashMap<String, Object>();

	/**
	 * 表体中所有包括默认值的对象，以<itemKey,defaultValue>存储
	 */
	private Map<String, Object> bodyDefaultValueMap = new HashMap<String, Object>();

	/**
	 * 是否隐藏表体,当表体的所有字段都隐藏的情况下,隐藏表体页签
	 */
	private boolean hideBodyGrid;

	/**
	 * NC功能注册中定义的与生成单据相关变量
	 */
	private FunRegisterPropertyVO funcRegisterPropertyVO = new FunRegisterPropertyVO();

	/**
	 * 查询模板中定义的锁定
	 */
	private List<Object> lockingItemAry;

	/**
	 * 表头统计行的字段,String[]包括2个值，第一个是数据库中的字段，第二个是模板中的字段<br/>
	 * XXX 表体的统计行使用页面直接统计就好
	 */
	private List<String[]> headerSummaryFieldAry = new ArrayList<String[]>();

	// 卡片显示字段的最大长度，字节数
	private int maxDefaultshownameLength;

	public String[] getHeaderListItemKey() {
		return HeaderListItemKey;
	}

	public void setHeaderListItemKey(String[] headerListItemKey) {
		HeaderListItemKey = headerListItemKey;
	}

	public boolean isHideBodyGrid() {
		return hideBodyGrid;
	}

	public void setHideBodyGrid(boolean hideBodyGrid) {
		this.hideBodyGrid = hideBodyGrid;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public List<BillTempletBVO> getFieldVOs() {
		return fieldVOs;
	}

	public void setFieldVOs(List<BillTempletBVO> fieldVOs) {
		this.fieldVOs = fieldVOs;
	}

	public List<BillTempletTVO> getTabVOs() {
		return tabVOs;
	}

	public void setTabVOs(List<BillTempletTVO> tabVOs) {
		this.tabVOs = tabVOs;
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

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pkCorp) {
		pk_corp = pkCorp;
	}

	public String getPk_user() {
		return pk_user;
	}

	public void setPk_user(String pkUser) {
		pk_user = pkUser;
	}

	public String getFmTemplate() {
		return fmTemplate;
	}

	public void setFmTemplate(String fmTemplate) {
		this.fmTemplate = fmTemplate;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getTemplateID() {
		return templateID;
	}

	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}

	public String getHeaderTabCode() {
		return headerTabCode;
	}

	public void setHeaderTabCode(String headerTabCode) {
		this.headerTabCode = headerTabCode;
	}

	public String getHeaderPkField() {
		return headerPkField;
	}

	public void setHeaderPkField(String headerPkField) {
		this.headerPkField = headerPkField;
	}

	public String getBodyTabCode() {
		return bodyTabCode;
	}

	public void setBodyTabCode(String bodyTabCode) {
		this.bodyTabCode = bodyTabCode;
	}

	public Map<String, String> getChildrenPkFieldMap() {
		return childrenPkFieldMap;
	}

	public void setChildrenPkFieldMap(Map<String, String> childrenPkFieldMap) {
		this.childrenPkFieldMap = childrenPkFieldMap;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	public String getBillId() {
		return billId;
	}

	public void setBillId(String billId) {
		this.billId = billId;
	}
	
	public String getBillIds() {
		return billIds;
	}

	public void setBillIds(String billIds) {
		this.billIds = billIds;
	}

	public boolean isHeaderGridImmediatelyLoad() {
		return headerGridImmediatelyLoad;
	}

	public void setHeaderGridImmediatelyLoad(boolean headerGridImmediatelyLoad) {
		this.headerGridImmediatelyLoad = headerGridImmediatelyLoad;
	}

	public Map<String, BillTempletBVO> getHeaderRefMap() {
		return headerRefMap;
	}

	public void setHeaderRefMap(Map<String, BillTempletBVO> headerRefMap) {
		this.headerRefMap = headerRefMap;
	}

	public Map<String, Object> getHeaderDefaultValueMap() {
		return headerDefaultValueMap;
	}

	public void setHeaderDefaultValueMap(Map<String, Object> headerDefaultValueMap) {
		this.headerDefaultValueMap = headerDefaultValueMap;
	}

	public Map<String, Object> getBodyDefaultValueMap() {
		return bodyDefaultValueMap;
	}

	public void setBodyDefaultValueMap(Map<String, Object> bodyDefaultValueMap) {
		this.bodyDefaultValueMap = bodyDefaultValueMap;
	}

	public FunRegisterPropertyVO getFuncRegisterPropertyVO() {
		return funcRegisterPropertyVO;
	}

	public void setFuncRegisterPropertyVO(FunRegisterPropertyVO funcRegisterPropertyVO) {
		this.funcRegisterPropertyVO = funcRegisterPropertyVO;
	}

	public String getPk_checkflow() {
		return pk_checkflow;
	}

	public void setPk_checkflow(String pk_checkflow) {
		this.pk_checkflow = pk_checkflow;
	}

	public String getFirstHeaderTabCode() {
		return firstHeaderTabCode;
	}

	public void setFirstHeaderTabCode(String firstHeaderTabCode) {
		this.firstHeaderTabCode = firstHeaderTabCode;
	}

	public List<Object> getLockingItemAry() {
		return lockingItemAry;
	}

	public void setLockingItemAry(List<Object> lockingItemAry) {
		this.lockingItemAry = lockingItemAry;
	}

	public List<String[]> getHeaderSummaryFieldAry() {
		return headerSummaryFieldAry;
	}

	public void setHeaderSummaryFieldAry(List<String[]> headerSummaryFieldAry) {
		this.headerSummaryFieldAry = headerSummaryFieldAry;
	}

	public int getMaxDefaultshownameLength() {
		return maxDefaultshownameLength;
	}

	public void setMaxDefaultshownameLength(int maxDefaultshownameLength) {
		this.maxDefaultshownameLength = maxDefaultshownameLength;
	}

	/**
	 * 利用序列化和反序列化进行深度克隆
	 * 
	 * @return
	 */
	public UiBillTempletVO clone() {
		UiBillTempletVO cloneObj = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			oo.writeObject(this);

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(in);
			cloneObj = (UiBillTempletVO) oi.readObject();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return cloneObj;
	}
}
