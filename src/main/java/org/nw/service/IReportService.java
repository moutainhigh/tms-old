package org.nw.service;

import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;

/**
 * 报表service的接口，所有报表的service需要实现该接口
 * 
 * @author xuqc
 * @date 2013-7-29 下午04:28:51
 */
public interface IReportService extends IToftService {

	/**
	 * 动态报表查询方法
	 * 
	 * @param paramVO
	 * @param offset
	 * @param pageSize
	 * @param orderBy
	 * @return
	 */
	public ReportVO loadReportData(ParamVO paramVO, int offset, int pageSize, String orderBy);

}
