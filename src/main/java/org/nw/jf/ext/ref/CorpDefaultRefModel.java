package org.nw.jf.ext.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.utils.TreeUtils;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.CorpVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 公司目录参照
 * 
 * @author xuqc
 * @date 2010-10-25
 * @version $Revision$
 */
@Controller
@RequestMapping(value = "/ref/common/corp")
public class CorpDefaultRefModel extends AbstractTreeRefModel {

	private static final long serialVersionUID = 4325986395304065247L;

	/**
	 * 返回公司树
	 * 
	 * @param node
	 *            父节点ID
	 * @return
	 */
	public List<TreeVO> load4Tree(HttpServletRequest request) {
		String whereClause = this.getExtendCond(request);
		String parent_id =Constants.SYSTEM_CODE; 
				//WebUtils.getLoginInfo().getPk_corp();
		List<CorpVO> corpVOs = getCorpVOs(parent_id, whereClause);
		if(corpVOs.size() == 0) {
			return new ArrayList<TreeVO>();
		}
		TreeVO treeVO = TreeUtils.convertCorpVO(corpVOs.get(0), !TreeUtils.hasChildCorpVO(corpVOs.get(0), corpVOs));
		treeVO.setExpanded(true);
		List<TreeVO> children = TreeUtils.convertCorpVOByParent(corpVOs, parent_id);
		treeVO.setChildren(children);
		// 加入parent_id所在的节点作为父节点
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		treeVOs.add(treeVO);
		return treeVOs;
	}

	/**
	 * 根据父级公司查询子公司
	 * 
	 * @param parent_id
	 * @return
	 */
	public List<CorpVO> getCorpVOs(String parent_id, String whereClause) {
		//yaojiie 2015 12 08添加 WITH (NOLOCK)
		String sql = "select * from nw_corp WITH(NOLOCK) where isnull(dr,0)=0";
		if(StringUtils.isNotBlank(whereClause)) {
			sql += " and " + whereClause;
		}
		sql += " order by corp_code";
		List<CorpVO> corpVOs;
		NWDao dao = NWDao.getInstance();
		corpVOs = dao.queryForList(sql, CorpVO.class);

		List<CorpVO> children = new ArrayList<CorpVO>();
		for(CorpVO corpVO : corpVOs) {
			if(corpVO.getPk_corp().equals(parent_id)) {
				children.add(corpVO);
				break;
			}
		}
		setChildren(corpVOs, parent_id, children);
		return children;
	}

	/**
	 * 使用递归返回parent_id的所有子菜单
	 * 
	 * @param corpVOs
	 * @param parent_id
	 * @return
	 */
	private void setChildren(List<CorpVO> corpVOs, String parent_id, List<CorpVO> children) {
		if(parent_id == null) {
			parent_id = "";
		}
		for(int i = 0; i < corpVOs.size(); i++) {
			CorpVO corpVO = corpVOs.get(i);
			String newParent_id = corpVO.getFathercorp();
			if(newParent_id == null) {
				newParent_id = "";
			}
			if(parent_id.equals(newParent_id)) {
				children.add(corpVO);
				setChildren(corpVOs, corpVO.getPk_corp(), children);
			}
		}
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByCode(CorpVO.class, code));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(CorpVO.class, pk));
	}

	public String getPkFieldCode() {
		return CorpVO.PK_CORP;
	}

	public String getCodeFieldCode() {
		return CorpVO.CORP_CODE;
	}

	public String getNameFieldCode() {
		return CorpVO.CORP_NAME;
	}
}
