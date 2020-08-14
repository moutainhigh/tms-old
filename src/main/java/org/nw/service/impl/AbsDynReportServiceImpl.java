package org.nw.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.jf.ext.GridHeaderVO;
import org.nw.jf.ext.ListColumn;
import org.nw.jf.ext.RecordType;
import org.nw.jf.group.GroupUtils;
import org.nw.jf.group.GroupVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.ReportTempletBVO;

/**
 * 动态报表抽象类
 * 
 * @author xuqc
 * @date 2015-1-14 下午04:40:37
 */
public abstract class AbsDynReportServiceImpl extends AbsReportServiceImpl {

	// FIXME 动态报表不使用分页
	protected Map<String, Object> buildReportSummaryRowMap(UiReportTempletVO templetVO, Class<? extends SuperVO> clazz,
			String where) {
		return null;
	}

	/**
	 * 对于动态报表，将要返回的报表列信息放到GridHeaderVO中
	 */
	public ReportVO loadReportData(ParamVO paramVO, int offset, int pageSize, String orderBy) {
		ReportVO reportVO = super.loadReportData(paramVO, offset, pageSize, orderBy);
		GridHeaderVO headerVO = new GridHeaderVO();
		// 模板的列信息也返回到客户端中
		List<ReportTempletBVO> fieldVOs = reportVO.getTempletVO().getFieldVOs();
		List<ListColumn> columnAry = new ArrayList<ListColumn>();
		List<RecordType> rtVOs = new ArrayList<RecordType>();
		for(ReportTempletBVO fieldVO : fieldVOs) {
			// ListColumn对于renderer相关字段在转成string时做了特殊处理,在使用tag生成js时,将引号去掉,此时的renderer就是jsp中定义的js对象,而不是一个字符串了.
			ListColumn column = fieldVO.buildListColumn(true);
			columnAry.add(column);
			RecordType rtVO = fieldVO.buildRecordType();
			rtVOs.add(rtVO);
		}
		headerVO.setColumnAry(columnAry);
		headerVO.setRecordTypeAry(rtVOs);

		// 多表头信息
		List<List<GroupVO>> groupVOs = GroupUtils.buildGroupHeaderAry(
				GroupUtils.getOptionsAry2(reportVO.getTempletVO().getFieldVOs()), 1);
		headerVO.setGroupVOs(groupVOs);
		reportVO.setHeaderVO(headerVO);
		return reportVO;
	}
}
