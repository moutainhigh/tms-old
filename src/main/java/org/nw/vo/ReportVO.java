package org.nw.vo;

import java.io.Serializable;

import org.nw.dao.PaginationVO;
import org.nw.jf.ext.GridHeaderVO;
import org.nw.jf.vo.UiReportTempletVO;

/**
 * 报表返回的vo，正常返回PaginationVO，对于动态报表，还包括headerVO，以及报表模板VO
 * 
 * @author xuqc
 * @date 2015-1-20 下午09:20:12
 */
public class ReportVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 对于动态报表，同时返回列头信息
	private GridHeaderVO headerVO;
	private PaginationVO pageVO;// 存储数据
	private UiReportTempletVO templetVO;// 报表模板数据

	public GridHeaderVO getHeaderVO() {
		return headerVO;
	}

	public void setHeaderVO(GridHeaderVO headerVO) {
		this.headerVO = headerVO;
	}

	public PaginationVO getPageVO() {
		return pageVO;
	}

	public void setPageVO(PaginationVO pageVO) {
		this.pageVO = pageVO;
	}

	public UiReportTempletVO getTempletVO() {
		return templetVO;
	}

	public void setTempletVO(UiReportTempletVO templetVO) {
		this.templetVO = templetVO;
	}

}
