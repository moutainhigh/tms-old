package org.nw.jf.ext.ref;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.TreeUtils;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.PsndocVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 人员档案参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/psndoc")
public class PsndocDefaultRefModel extends AbstractTreeAndGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { PsndocVO.PSNCODE, PsndocVO.PSNNAME, PsndocVO.ID };
	}

	protected String[] getFieldName() {
		return new String[] { "人员编码", "人员名称", "身份证" };
	}

	protected String getTreePkFieldCode() {
		return PsndocVO.PK_DEPT;
	}

	public List<TreeVO> load4Tree(HttpServletRequest request) {
		String cond = "1=1";
		String whereClause = this.getExtendCond(request);
		if(StringUtils.isNotBlank(whereClause)) {
			cond += " and " + whereClause;
		}
		String pk_corp = request.getParameter("pk_corp");
		if(StringUtils.isBlank(pk_corp)) {
			pk_corp = WebUtils.getLoginInfo().getPk_corp();
		}
		NWDao dao = NWDao.getInstance();
		String sql = "select * from nw_dept where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and pk_corp in " + "(" + CorpHelper.getCurrentCorpWithChildren2(pk_corp) + ")";
		if(StringUtils.isNotBlank(whereClause)) {
			sql += " and " + whereClause;
		}
		sql += " order by dept_code";
		List<DeptVO> deptVOs = dao.queryForListWithCache(sql, DeptVO.class);
		List<TreeVO> treeVOs = TreeUtils.convertDeptVOByParent(deptVOs, null);
		return TreeUtils.addRootNode("__root", "部门", treeVOs);
	}

	private String getWhere() {
		String pk_dept = ServletContextHolder.getRequest().getParameter("pk_dept");
		if(StringUtils.isNotBlank(pk_dept)) {
			return "pk_dept='" + pk_dept + "'";
		}
		return null;
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, PsndocVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" 1=1 ");
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
		String where = getWhere();
		if(StringUtils.isNotBlank(where)) {
			cond.append(" and ");
			cond.append(where);
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO page = baseRefService.queryForPagination(PsndocVO.class, offset, pageSize, cond.toString());
		return this.genAjaxResponse(true, null, page);
	}

	public String getPkFieldCode() {
		return PsndocVO.PK_PSNDOC;
	}

	public String getCodeFieldCode() {
		return PsndocVO.PSNCODE;
	}

	public String getNameFieldCode() {
		return PsndocVO.PSNNAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByCode(PsndocVO.class, code,"isnull(locked_flag,'N')='N' and " + CorpHelper.getCurrentCorpWithChildren()));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(PsndocVO.class, pk));
	}

}
