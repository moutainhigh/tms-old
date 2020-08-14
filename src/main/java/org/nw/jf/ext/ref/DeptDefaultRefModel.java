package org.nw.jf.ext.ref;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.utils.CorpHelper;
import org.nw.utils.TreeUtils;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.DeptVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sun.star.security.CryptographyException;

/**
 * 部门档案参照
 * 
 * @author xuqc
 * @date 2010-10-25
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "/ref/common/dept")
public class DeptDefaultRefModel extends AbstractTreeRefModel {

	private static final long serialVersionUID = 4748153476251550291L;

	public List<TreeVO> load4Tree(HttpServletRequest request) {
		String whereClause = this.getExtendCond(request);
		String pk_corp = request.getParameter("pk_corp");
		if(StringUtils.isBlank(pk_corp)) {
			pk_corp = WebUtils.getLoginInfo().getPk_corp();
		}
		NWDao dao = NWDao.getInstance();
		//yaojiie 2015 12 08添加 WITH (NOLOCK)
		String sql = "select * from nw_dept WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and "+CorpHelper.getCurrentCorpWithChildren();
		if(StringUtils.isNotBlank(whereClause)) {
			sql += " and " + whereClause;
		}
		sql += " order by dept_code";
		List<DeptVO> deptVOs = dao.queryForListWithCache(sql, DeptVO.class);
		List<TreeVO> treeVOs = TreeUtils.convertDeptVOByParent(deptVOs, null);
		return TreeUtils.addRootNode("__root", "部门", treeVOs);
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(DeptVO.class, code, "isnull(locked_flag,'N')='N' and " + CorpHelper.getCurrentCorpWithChildren()));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(DeptVO.class, pk));
	}

	public String getPkFieldCode() {
		return DeptVO.PK_DEPT;
	}

	public String getCodeFieldCode() {
		return DeptVO.DEPT_CODE;
	}

	public String getNameFieldCode() {
		return DeptVO.DEPT_NAME;
	}
}
