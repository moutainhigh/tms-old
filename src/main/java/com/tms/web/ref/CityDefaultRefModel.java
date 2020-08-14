package com.tms.web.ref;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.vo.TreeVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.constants.AreaConst;
import com.tms.utils.TreeUtils;
import com.tms.vo.base.AreaVO;

/**
 * 城市区域参照，实际上是区域档案的一部分，因为比较常用到，用一个单独的参照来表示，方便使用
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/city")
public class CityDefaultRefModel extends AreaDefaultRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4697550166289979740L;

	public List<TreeVO> load4Tree(HttpServletRequest request) {
		String cond = "area_level >=" + AreaConst.CITY_LEVEL;
		String whereClause = this.getExtendCond(request);
		if(StringUtils.isNotBlank(whereClause)) {
			cond += " and " + whereClause;
		}
		NWDao dao = NWDao.getInstance();
		String sql = "select * from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and locked_flag='N' ";
		if(StringUtils.isNotBlank(cond)) {
			sql += " and " + cond;
		}
		sql += " order by display_order,code";
		List<AreaVO> areaVOs = dao.queryForList(sql, AreaVO.class);
		List<TreeVO> treeVOs = null;
		// 从level级开始生成树
		List<AreaVO> topLevelVOs = new ArrayList<AreaVO>();
		for(AreaVO areaVO : areaVOs) {
			if(areaVO.getArea_level().intValue() == AreaConst.CITY_LEVEL) {
				topLevelVOs.add(areaVO);
			}
		}
		treeVOs = TreeUtils.convertAreaVO(topLevelVOs, areaVOs);
		TreeUtils.setChildrenAreaVOs(topLevelVOs, areaVOs, treeVOs);
		return TreeUtils.addRootNode("__root", "区域树", treeVOs);
	}

}
