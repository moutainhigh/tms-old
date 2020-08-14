package com.tms.web.ref;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.ETCVO;
import com.tms.vo.base.FuelCardVO;
import com.tms.vo.te.EntrustVO;

/**
 * ETC卡参照
 * 
 * @author yaojiie
 * @date 2015 12 29
 */
@Controller
@RequestMapping(value = "/ref/common/etc")
public class ETCDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7855191119317451527L;

	public Boolean isFillinable() {
		return true;
	}

	protected String[] getFieldCode() {
		return new String[] {"etc_code", "etc_name","memo"};
	}

	protected String[] getFieldName() {
		return new String[] {"编码", "名称" ,"备注"};
	}
	
	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, FuelCardVO.class);
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
		PaginationVO paginationVO = baseRefService.queryForPagination(ETCVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, paginationVO);
	}

	public String getPkFieldCode() {
		return "pk_etc";
	}

	public String getCodeFieldCode() {
		return "etc_code";
	}

	public String getNameFieldCode() {
		return  "etc_name";
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(ETCVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(ETCVO.class, pk));
	}

}
