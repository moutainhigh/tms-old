package org.nw.jf.ext.ref;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.vo.BillTempletVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 单据模板参照类
 * 
 * @author xuqc
 * @date 2012-12-1 下午12:49:13
 */
@Controller
@RequestMapping(value = "/ref/common/bt")
public class BillTempletDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3054139497972582042L;

	protected String[] getFieldCode() {
		return new String[] { BillTempletVO.PK_BILLTYPECODE, BillTempletVO.BILL_TEMPLETCAPTION };
	}

	protected String[] getFieldName() {
		return new String[] { "模板编码", "模板类型" };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, BillTempletVO.class);
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
		PaginationVO page = baseRefService.queryForPagination(BillTempletVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return BillTempletVO.PK_BILLTYPECODE;
	}

	public String getCodeFieldCode() {
		return BillTempletVO.PK_BILLTYPECODE;
	}

	public String getNameFieldCode() {
		return BillTempletVO.PK_BILLTYPECODE;
	}

	public Map<String, Object> getByCode(String code) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(BillTempletVO.PK_BILLTYPECODE, code);
		return this.genAjaxResponse(true, null, retMap);
	}

	public Map<String, Object> getByPk(String pk) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put(BillTempletVO.PK_BILLTYPECODE, pk);
		return this.genAjaxResponse(true, null, retMap);
	}

}
