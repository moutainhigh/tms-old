package com.tms.web.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.utils.FormulaHelper;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.OpProjectVO;

/**
 * 利润分享
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/opp")
public class OpProjectDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { "code", "name", "pk_customer", "memo"};
	}

	protected String[] getFieldName() {
		return new String[] {"编码", "名称", "客户", "备注" };
	}


	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, CustomerVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" isnull(locked_flag,'N')='N' ");
		if(StringUtils.isNotBlank(condition)) {
			cond.append(" and ");
			cond.append(condition);
		}
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		String corpCond = CorpHelper.getCurrentCorpWithChildrenAndParent();
		if(StringUtils.isNotBlank(corpCond)) {
			cond.append(" and ");
			cond.append(corpCond);
		}
		String pk_customer = request.getParameter("pk_customer");
		if(StringUtils.isNotBlank(pk_customer) && !"null".equals(pk_customer)) {
			// 根据客户查询结算客户
			cond.append(" and ");
			cond.append("  pk_customer='" + pk_customer + "'");
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(OpProjectVO.class, offset, pageSize, cond.toString());
		List<SuperVO> superVOList = page.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOList.size());
		for(SuperVO vo : superVOList) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		superVOList.clear();

		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, getFormulas(), true);
		page.setItems(list);
		return this.genAjaxResponse(true, null, page);
	}

	public String[] getFormulas() {
		return new String[] {"pk_customer->getcolvalue(ts_customer,cust_name,pk_customer,pk_customer)"};
	}

	public String getPkFieldCode() {
		return OpProjectVO.PK_OP_PROJECT;
	}

	public String getCodeFieldCode() {
		return "code";
	}

	public String getNameFieldCode() {
		return "name";
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(OpProjectVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(OpProjectVO.class, pk));
	}
}
