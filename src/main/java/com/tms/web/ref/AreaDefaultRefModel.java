package com.tms.web.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.jf.ext.ref.AbstractTreeRefModel;
import org.nw.vo.TreeVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.utils.TreeUtils;
import com.tms.vo.base.AreaVO;

/**
 * 区域参照
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/area")
public class AreaDefaultRefModel extends AbstractTreeRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	public Boolean isRemoteFilter() {
		return true;
	}

	public List<TreeVO> load4Tree(HttpServletRequest request) {
		String cond = "1=1";
		String whereClause = this.getExtendCond(request);

		if(StringUtils.isNotBlank(whereClause)) {
			cond += " and " + whereClause;
		}
		String condFromFilter = getTreeQueryCondition(request, null);

		String isRoot = request.getParameter("isRoot");
		if(StringUtils.isNotBlank(condFromFilter)) {
			// 使用了查询条件，直接根据这个条件查询
			if(StringUtils.isNotBlank(condFromFilter)) {
				cond += " and " + condFromFilter;
			}
		} else {
			if("true".equals(isRoot)) {
				String sLevel = request.getParameter("level");
				if(StringUtils.isNotBlank(sLevel)) {
					cond += " and area_level =" + sLevel;
				} else {
					if(StringUtils.isBlank(condFromFilter)) {
						cond += " and parent_id is null";
					}
				}
			} else {
				String parent_id = request.getParameter("parent_id");
				if(StringUtils.isBlank(parent_id)) {
					parent_id = request.getParameter("node");
				}
				if(StringUtils.isNotBlank(parent_id)) {
					cond += " and parent_id='" + parent_id + "'";
				} else {
					cond += " and parent_id is null";
				}
			}
		}

		NWDao dao = NWDao.getInstance();
		String sql = "select * from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and locked_flag='N' ";
		if(StringUtils.isNotBlank(cond)) {
			sql += " and " + cond;
		}
		sql += " order by display_order,code";
		List<AreaVO> areaVOs = dao.queryForList(sql, AreaVO.class);
		if(areaVOs == null) {
			areaVOs = new ArrayList<AreaVO>();
		}
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		for(AreaVO areaVO : areaVOs) {
			treeVOs.add(TreeUtils.convertAreaVO(areaVO, false));
		}
		return treeVOs;
	}

	public String getPkFieldCode() {
		return AreaVO.PK_AREA;
	}

	public String getCodeFieldCode() {
		return AreaVO.CODE;
	}

	public String getNameFieldCode() {
		return AreaVO.NAME;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByCode(AreaVO.class, code));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(AreaVO.class, pk));
	}

}
