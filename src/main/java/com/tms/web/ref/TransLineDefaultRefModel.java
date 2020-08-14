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

import com.tms.vo.base.GoodsTypeVO;
import com.tms.vo.base.TransLineVO;

/**
 * 货品类型参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/transLine")
public class TransLineDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { TransLineVO.LINE_CODE, TransLineVO.LINE_NAME, TransLineVO.LINE_TYPE, GoodsTypeVO.MEMO };
	}

	protected String[] getFieldName() {
		return new String[] { "线路编码", "线路名称","类型", "备注" };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, TransLineVO.class);
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
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(TransLineVO.class, offset, pageSize, cond.toString());
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
				"datatype_code->\"trans_line_type\"",
				"pk_data_dict->getcolvalue(nw_data_dict,pk_data_dict,datatype_code,datatype_code)",
				"line_type->getcolvaluemorewithcond(\"nw_data_dict_b\",\"display_name\",\"pk_data_dict\",pk_data_dict,\"value\",line_type,\"\")" };
	}

	public String getPkFieldCode() {
		return TransLineVO.PK_TRANS_LINE;
	}

	public String getCodeFieldCode() {
		return TransLineVO.LINE_CODE;
	}

	public String getNameFieldCode() {
		return TransLineVO.LINE_NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(TransLineVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(TransLineVO.class, pk));
	}

}
