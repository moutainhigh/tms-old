package org.nw.jf.vo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nw.vo.sys.ReportTempletBVO;
import org.nw.vo.sys.ReportTempletVO;

/**
 * 存储报表模板数据的VO
 */
public class UiReportTempletVO implements Serializable {
	private static final long serialVersionUID = -2671765656013091735L;
	private String ts;
	private ReportTempletVO reportTempletVO;
	private List<ReportTempletBVO> fieldVOs;
	private String reportTempletId;// 报表模板ID
	private String funCode;
	private String nodeKey;

	// freeMarker模板文件
	private String fmTemplate;
	// js命名空间
	private String moduleName;

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

	// 2015-1-17 是否是动态报表
	private boolean isDynReport;

	public FunRegisterPropertyVO getFuncRegisterPropertyVO() {
		return funcRegisterPropertyVO;
	}

	public void setFuncRegisterPropertyVO(FunRegisterPropertyVO funcRegisterPropertyVO) {
		this.funcRegisterPropertyVO = funcRegisterPropertyVO;
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

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getReportTempletId() {
		return reportTempletId;
	}

	public void setReportTempletId(String reportTempletId) {
		this.reportTempletId = reportTempletId;
	}

	public ReportTempletVO getReportTempletVO() {
		return reportTempletVO;
	}

	public void setReportTempletVO(ReportTempletVO reportTempletVO) {
		this.reportTempletVO = reportTempletVO;
	}

	public List<ReportTempletBVO> getFieldVOs() {
		return fieldVOs;
	}

	public void setFieldVOs(List<ReportTempletBVO> fieldVOs) {
		this.fieldVOs = fieldVOs;
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

	public boolean isDynReport() {
		return isDynReport;
	}

	public void setDynReport(boolean isDynReport) {
		this.isDynReport = isDynReport;
	}

	/**
	 * 利用序列化和反序列化进行深度克隆
	 * 
	 * @return
	 */
	public UiReportTempletVO clone() {
		UiReportTempletVO cloneObj = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			oo.writeObject(this);

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(in);
			cloneObj = (UiReportTempletVO) oi.readObject();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return cloneObj;
	}
}
