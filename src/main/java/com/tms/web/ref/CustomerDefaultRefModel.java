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

/**
 * 客户档案参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/cust")
public class CustomerDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { CustomerVO.CUST_CODE, CustomerVO.CUST_NAME, "cust_type_name", CustomerVO.MEMO };
	}

	protected String[] getFieldName() {
		return new String[] { "客户编码", "客户名称", "客户类型", "备注" };
	}

	protected String[] getHiddenFieldCode() {
		return new String[] { CustomerVO.CUST_TYPE };
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
			cond.append(" pk_customer in (select pk_related_cust from ts_cust_bala WITH(NOLOCK) where pk_customer='" + pk_customer
					+ "') ");
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(CustomerVO.class, offset, pageSize, cond.toString());
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
		return new String[] {
				"datatype_code->\"cust_type\"",
				"pk_data_dict->getcolvalue(nw_data_dict,pk_data_dict,datatype_code,datatype_code)",
				"cust_type_name->getcolvaluemorewithcond(\"nw_data_dict_b\",\"display_name\",\"pk_data_dict\",pk_data_dict,\"value\",cust_type,\"\")" };
	}

	public String getPkFieldCode() {
		return CustomerVO.PK_CUSTOMER;
	}

	public String getCodeFieldCode() {
		return CustomerVO.CUST_CODE;
	}

	public String getNameFieldCode() {
		return CustomerVO.CUST_NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(CustomerVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(CustomerVO.class, pk));
	}
}
