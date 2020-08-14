package com.tms.web.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.formula.FormulaParser;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.constants.ContractConst;
import com.tms.vo.cm.ExpenseTypeVO;

/**
 * 费用类型参照，默认值是“应收、应收应付”,创建一个ExpenseTypeDefaultRefModel2，默认值是"应付、应收应付"
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/et")
public class ExpenseTypeDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { ExpenseTypeVO.CODE, ExpenseTypeVO.NAME, "type_name" ,ExpenseTypeVO.MEMO};
	}

	protected String[] getFieldName() {
		return new String[] { "编码", "名称", "类别" ,"备注"};
	}

	protected String[] getHiddenFieldCode() {
		return new String[] { "expense_type" };
	}

	public String getExtendCond(HttpServletRequest request) {
		String where = request.getParameter("_cond");
		if(StringUtils.isBlank(where)) {
			where = " expense_type in (0,2) ";
		}
		String contract_type = request.getParameter("type");
		if(StringUtils.isNotBlank(contract_type)) {
			if(ContractConst.CARRIER == Integer.parseInt(contract_type)) {
				// 承运商
				where = "expense_type in (1,2)";
			} else {
				where = "expense_type in (0,2)";
			}
		}
		return where;
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, ExpenseTypeVO.class);

		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" isnull(locked_flag,'N')='N' ");
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
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

		PaginationVO paginationVO = baseRefService.queryForPagination(ExpenseTypeVO.class, offset, pageSize,
				cond.toString());
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<SuperVO> superVOList = paginationVO.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOList.size());
		for(SuperVO vo : superVOList) {
			Map<String, Object> map = new HashMap<String, Object>();
			for(String key : vo.getAttributeNames()) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		superVOList.clear();

		// 执行公式
		FormulaParser formulaParse = new FormulaParser(NWDao.getInstance().getDataSource());
		formulaParse.setFormulas(getFormulas());
		formulaParse.setContext(mapList);
		formulaParse.setMergeContextToResult(true);//
		List<Map<String, Object>> retList = formulaParse.getResult();

		paginationVO.setItems(retList);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	public String[] getFormulas() {
		return new String[] { "type_name->iif(expense_type==0,\"应收\",iif(expense_type==1,\"应付\",iif(expense_type==2,\"应收应付\",expense_type)))" };
	}

	public String getPkFieldCode() {
		return ExpenseTypeVO.PK_EXPENSE_TYPE;
	}

	public String getCodeFieldCode() {
		return ExpenseTypeVO.CODE;
	}

	public String getNameFieldCode() {
		return ExpenseTypeVO.NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(ExpenseTypeVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(ExpenseTypeVO.class, pk));
	}

}
