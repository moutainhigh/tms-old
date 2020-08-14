package org.nw.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.jf.vo.UiReportTempletVO;

/**
 * 动态报表的基础控制类
 * 
 * @author xuqc
 * @date 2013-7-29 下午01:27:23
 */
public abstract class AbsDynReportController extends AbsReportController {

	protected void doIndexExtProcess(HttpServletRequest request, HttpServletResponse response,
			UiReportTempletVO templetVO) {
		super.doIndexExtProcess(request, response, templetVO);
		templetVO.setDynReport(true);
	}

}
