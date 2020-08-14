package org.nw.jf.ext.ref;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.PaginationVO;
import org.nw.utils.CorpHelper;
import org.nw.vo.sys.UserVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/user")
public class UserDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { UserVO.USER_CODE, UserVO.USER_NAME, UserVO.USER_NOTE, UserVO.USER_TYPE };
	}

	protected String[] getFieldName() {
		return new String[] { "用户编码", "用户名称", "用户备注", "用户类型" };
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, UserVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" pk_user<> '" + Constants.SYSTEM_CODE + "' ");
		if(StringUtils.isNotBlank(condition)) {
			cond.append(" and ");
			cond.append(condition);
		}
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		if(StringUtils.isNotBlank(corpCond)) {
			cond.append(" and ");
			cond.append(corpCond);
		}

		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(UserVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return UserVO.PK_USER;
	}

	public String getCodeFieldCode() {
		return UserVO.USER_CODE;
	}

	public String getNameFieldCode() {
		return UserVO.USER_NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByCode(UserVO.class, code));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(UserVO.class, pk));
	}

}
