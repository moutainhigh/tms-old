package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.DeptService;
import org.nw.utils.CorpHelper;
import org.nw.utils.TreeUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.DeptVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;


/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午03:23:33
 */
@Service
public class DeptServiceImpl extends AbsToftServiceImpl implements DeptService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, DeptVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, DeptVO.PK_DEPT);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> valueMap = super.getHeaderDefaultValues(paramVO);
		valueMap.put("pk_corp", WebUtils.getLoginInfo().getPk_corp());
		valueMap.put("pk_dept", WebUtils.getLoginInfo().getPk_dept());
		return valueMap;
	}

	public List<TreeVO> getDeptTree(String parent_id) {
		List<DeptVO> deptVOs = getDeptVOs(parent_id);
		List<TreeVO> treeVOs = TreeUtils.convertDeptVOByParent(deptVOs, parent_id);
		return treeVOs;
	}

	public List<DeptVO> getDeptVOs(String parent_id) {
		String sql = "select * from nw_dept WITH(NOLOCK)where isnull(dr,0)=0 and isnull(locked_flag,'N')='N'"
				+ " and pk_corp in ( "+CorpHelper.getCurrentCorpWithChildren2(WebUtils.getLoginInfo().getPk_corp())+")";
		sql += " order by dept_code asc";
		List<DeptVO> deptVOs = dao.queryForList(sql, DeptVO.class);
		List<DeptVO> children = new ArrayList<DeptVO>();
		setChildren(deptVOs, parent_id, children);
		return children;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String where = super.buildLoadDataCondition(params, paramVO, templetVO);
		// 加入公司条件
		String corpCond = CorpHelper.getCurrentCorp();
		if(StringUtils.isNotBlank(where)) {
			where = corpCond + " and " + where;
		}
		return where;
	}

	public DeptVO getByDept_code(String dept_code) {
		return dao.queryByCondition(DeptVO.class, "dept_code=?", dept_code);
	}

	public List<DeptVO> getByPkCorp(String pk_corp) {
		DeptVO[] deptVOs = dao.queryForSuperVOArrayByCondition(DeptVO.class,
				"isnull(locked_flag,'N')='N' and pk_corp=?", pk_corp);
		return Arrays.asList(deptVOs);
	}

	/**
	 * 使用递归返回parent_id的所有子菜单
	 * 
	 * @param deptVOs
	 * @param parent_id
	 * @return
	 */
	private void setChildren(List<DeptVO> deptVOs, String parent_id, List<DeptVO> children) {
		if(parent_id == null) {
			parent_id = "";
		}
		for(int i = 0; i < deptVOs.size(); i++) {
			DeptVO deptVO = deptVOs.get(i);
			String newParent_id = deptVO.getFatherdept();
			if(newParent_id == null) {
				newParent_id = "";
			}
			if(parent_id.equals(newParent_id)) {
				children.add(deptVO);
				setChildren(deptVOs, deptVO.getPk_dept(), children);
			}
		}
	}
}
