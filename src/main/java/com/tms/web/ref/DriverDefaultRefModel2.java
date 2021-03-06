package com.tms.web.ref;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.DriverVO;

/**
 * 司机档案参照，返回司机名称，委托单的司机可能使用的都是司机名称，这样用户也可以直接录入名称，跟车牌号一直
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/driver2")
public class DriverDefaultRefModel2 extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { DriverVO.DRIVER_CODE, DriverVO.DRIVER_NAME, DriverVO.MEMO };
	}

	protected String[] getFieldName() {
		return new String[] { "司机编码", "司机名称", "备注" };
	}

	public Boolean isFillinable() {
		return true;
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, DriverVO.class);
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
		if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_carrier())){
			cond.append(" and ");
			cond.append(" pk_carrier='" +  WebUtils.getLoginInfo().getPk_carrier() + "'");
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(DriverVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return DriverVO.DRIVER_NAME;
	}

	public String getCodeFieldCode() {
		return DriverVO.DRIVER_NAME;
	}

	public String getNameFieldCode() {
		return DriverVO.DRIVER_NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(DriverVO.class, code, " isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(DriverVO.class, pk));
	}

	public String getRefName() {
		return "司机档案";
	}
}
