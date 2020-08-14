package com.tms.web.ref;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.utils.FormulaHelper;
import org.nw.utils.NWUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.GoodsAllocationVO;

/**
 * 货位档案参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/ga")
public class GoodsAllocationDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { GoodsAllocationVO.CODE, GoodsAllocationVO.NAME, "status_name", "useage_name",
				GoodsAllocationVO.STORE_AREA, "cateorage_name", "prop_name" };
	}

	protected String[] getFieldName() {
		return new String[] { "编码", "名称", "状态", "货位用途", "储区", "货位种类", "货位属性" };
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, GoodsAllocationVO.class);
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
		PaginationVO page = baseRefService.queryForPagination(GoodsAllocationVO.class, offset, pageSize,
				cond.toString());
		List<GoodsAllocationVO> list = page.getItems();
		List<Map<String, Object>> mapList = NWUtils.convertVO2Map(list);
		mapList = FormulaHelper.execFormula(mapList, getFormulas(), true);
		page.setItems(mapList);
		// 将int类型的字段翻译成名字
		return this.genAjaxResponse(true, null, page);
	}

	public String[] getFormulas() {
		return new String[] { "status_name->iif(istatus==1,\"OK\",\"HOLD\")",
				"useage_name->iif(useage==1,\"箱拣货货位\",\"件拣货货位\")",
				"cateorage_name->iif(cateorage==1,\"普通货位\",\"流利式货位\")",
				"prop_name->iif(property==1,\"正常\",iif(property==2,\"搁置\",\"破损\"))" };
	}

	public String getPkFieldCode() {
		return GoodsAllocationVO.PK_GOODS_ALLOCATION;
	}

	public String getCodeFieldCode() {
		return GoodsAllocationVO.CODE;
	}

	public String getNameFieldCode() {
		return GoodsAllocationVO.NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(GoodsAllocationVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(GoodsAllocationVO.class, pk));
	}

}
