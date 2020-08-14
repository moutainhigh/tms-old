package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.constants.Constants;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.CorpService;
import org.nw.utils.TreeUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.CorpVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 公司处理类
 * 
 * @author xuqc
 * @date 2012-6-17 下午02:51:48
 */
@Service
public class CorpServiceImpl extends AbsToftServiceImpl implements CorpService {
	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, CorpVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, CorpVO.PK_CORP);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("city")) {
					fieldVO.setUserdefine3("area_level=5");
				}
			}
		}
		return templetVO;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> valueMap = super.getHeaderDefaultValues(paramVO);
		valueMap.put("pk_corp", null);
		valueMap.put("pk_dept", null);
		return valueMap;
	}

	public List<TreeVO> getCorpTree(String parent_id) {
		if(parent_id == null) {
			parent_id = WebUtils.getLoginInfo().getPk_corp();
		}
		List<CorpVO> corpVOs = getCorpVOs(parent_id);
		List<TreeVO> treeVOs = TreeUtils.convertCorpVOByParent(corpVOs, parent_id);
		CorpVO parentVO = getByPrimaryKey(CorpVO.class, parent_id);
		return TreeUtils.addRootNode(parentVO.getPk_corp(), parentVO.getCorp_name(), treeVOs);
	}

	public List<CorpVO> getCorpVOs(String parent_id) {
		String sql = "select * from nw_corp WITH(NOLOCK) where isnull(dr,0)=0 ";
		sql += " order by corp_code asc";
		List<CorpVO> corpVOs = dao.queryForList(sql, CorpVO.class);
		List<CorpVO> children = new ArrayList<CorpVO>();
		setChildren(corpVOs, parent_id, children);
		return children;
	}

	public CorpVO getCorpVOByCorpCode(String corp_code) {
		String strWhere = "(dr=0 or dr is null) and corp_code=?";
		return dao.queryByCondition(CorpVO.class, strWhere, corp_code);
	}

	public List<CorpVO> getCurrentCorpVOs() {
		String parent_id = WebUtils.getLoginInfo().getPk_corp();
		CorpVO parentVO = getByPrimaryKey(CorpVO.class, parent_id);
		List<CorpVO> allCorpVOs = getCorpVOs(null);// 所有公司
		List<CorpVO> allChildVOs = new ArrayList<CorpVO>();
		allChildVOs.add(parentVO);
		setChildrenCorpVOs(parentVO, allCorpVOs, allChildVOs);
		return allChildVOs;
	}

	public String getCurrentCorpCondition() {
		List<CorpVO> corpVOs = getCurrentCorpVOs();
		StringBuffer sb = new StringBuffer("'" + Constants.SYSTEM_CODE + "',");
		for(CorpVO corpVO : corpVOs) {
			sb.append("'");
			sb.append(corpVO.getPk_corp());
			sb.append("',");
		}
		String corpCond = null;
		if(sb.length() > 0) {
			corpCond = sb.substring(0, sb.length() - 1);
		}
		return " pk_corp in (" + corpCond + ")";
	}

	/**
	 * 递归，返回所有子节点
	 * 
	 * @param parentVO
	 * @param allCorpVOs
	 * @param allChildVOs
	 */
	private void setChildrenCorpVOs(CorpVO parentVO, List<CorpVO> allCorpVOs, List<CorpVO> allChildVOs) {
		if(allChildVOs == null) {
			allChildVOs = new ArrayList<CorpVO>();
		}
		for(int j = 0; j < allCorpVOs.size(); j++) {
			CorpVO childVO = allCorpVOs.get(j);
			// 以下条件用来判断是否是子节点
			if(parentVO.getPk_corp().equals(childVO.getFathercorp())) {
				allChildVOs.add(childVO);
				setChildrenCorpVOs(childVO, allCorpVOs, allChildVOs);
			}
		}
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
}
