package com.tms.web.ref;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.GoodsPackcorpVO;

/**
 * 货品包装单位参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/packcorp")
public class GoodsPackcorpDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { GoodsPackcorpVO.CODE, GoodsPackcorpVO.NAME, GoodsPackcorpVO.MEMO };
	}

	protected String[] getFieldName() {
		return new String[] { "编码", "名称", "备注" };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, GoodsPackcorpVO.class);
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
		String pk_goods = request.getParameter("pk_goods");
		PaginationVO page = null;
		if(StringUtils.isNotBlank(pk_goods)) {
			// 存在货品的参数，那么优先选择货品下的包装明细
			String sql = "select * from ts_goods_packcorp WITH(NOLOCK) where isnull(dr,0)=0 and "
					+ "pk_goods_packcorp in (select pk_goods_packcorp from ts_goods_pack_rela WITH(NOLOCK) where pk_goods=?)";
			sql += " and " + cond.toString();
			page = baseRefService.queryBySqlForPagination(GoodsPackcorpVO.class, sql, offset, pageSize, pk_goods);
		} else {
			page = baseRefService.queryForPagination(GoodsPackcorpVO.class, offset, pageSize, cond.toString());
		}
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return GoodsPackcorpVO.PK_GOODS_PACKCORP;
	}

	public String getCodeFieldCode() {
		return GoodsPackcorpVO.CODE;
	}

	public String getNameFieldCode() {
		return GoodsPackcorpVO.NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(GoodsPackcorpVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(GoodsPackcorpVO.class, pk));
	}

}
