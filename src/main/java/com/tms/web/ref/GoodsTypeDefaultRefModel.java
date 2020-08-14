package com.tms.web.ref;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.GoodsTypeVO;

/**
 * 货品类型参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/goodstype")
public class GoodsTypeDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { GoodsTypeVO.CODE, GoodsTypeVO.NAME, GoodsTypeVO.MEMO };
	}

	protected String[] getFieldName() {
		return new String[] { "货品类型编码", "货品类型名称", "备注" };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, GoodsTypeVO.class);
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
		PaginationVO page = baseRefService.queryForPagination(GoodsTypeVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return GoodsTypeVO.PK_GOODS_TYPE;
	}

	public String getCodeFieldCode() {
		return GoodsTypeVO.CODE;
	}

	public String getNameFieldCode() {
		return GoodsTypeVO.NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(GoodsTypeVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(GoodsTypeVO.class, pk));
	}

}
