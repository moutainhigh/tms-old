package org.nw.jf.ext.ref;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.vo.sys.PortletVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * portlet组件参照
 * 
 * @author xuqc
 * @date 2014-5-21 下午03:14:33
 */
@Controller
@RequestMapping(value = "/ref/common/portlet")
@SuppressWarnings("serial")
public class PortletDefaultRefModel extends AbstractGridRefModel {

	protected String[] getFieldCode() {
		return new String[] { PortletVO.PORTLET_CODE, PortletVO.PORTLET_NAME, PortletVO.FUN_CODE };
	}

	protected String[] getFieldName() {
		return new String[] { "组件编码", "组件名称", "功能节点" };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, PortletVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" 1=1 ");
		if(StringUtils.isNotBlank(condition)) {
			cond.append(" and ");
			cond.append(condition);
		}
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(PortletVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return PortletVO.PORTLET_CODE;
	}

	public String getCodeFieldCode() {
		return PortletVO.PORTLET_CODE;
	}

	public String getNameFieldCode() {
		return PortletVO.PORTLET_NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByCode(PortletVO.class, code));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(PortletVO.class, pk));
	}

}
