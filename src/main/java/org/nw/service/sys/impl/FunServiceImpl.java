package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.constants.FunRegisterConst;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.FunService;
import org.nw.utils.TreeUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.TempletDistVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 功能菜单service类
 * 
 * @author xuqc
 * @date 2012-6-16 下午06:21:39
 */
@Service
public class FunServiceImpl extends AbsToftServiceImpl implements FunService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, FunVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, FunVO.PK_FUN);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public String getCodeFieldCode() {
		return FunVO.FUN_CODE;
	}

	/**
	 * 多次引用该比较类，使用内部类处理 <br/>
	 * Arrays.sort(funArray, new FunComparator());
	 * 
	 * @author xuqc
	 * @date 2011-5-30
	 */
	class FunComparator implements Comparator<FunVO> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(FunVO fvo1, FunVO fvo2) {
			return fvo1.getFun_code().compareToIgnoreCase(fvo2.getFun_code());
		}
	}

	class TreeVOComparator implements Comparator<TreeVO> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(TreeVO fvo1, TreeVO fvo2) {
			return fvo1.getCode().compareToIgnoreCase(fvo2.getCode());
		}
	}

	public FunVO getFunVOByFunCodeWithNoDr(String fun_code) {
		return NWDao.getInstance().queryForObject("select * from nw_fun WITH(NOLOCK) where fun_code=?", FunVO.class, fun_code);
	}

	public List<TreeVO> getOriginalFunTree(String parent_id) {
		List<FunVO> funVOs = getFunVOs(parent_id, true);
		FunVO[] funAry = funVOs.toArray(new FunVO[funVOs.size()]);
		Arrays.sort(funAry, new FunComparator()); // 根据fun_code重新排序，因为查询的数据已经根据display_order排序了
		List<TreeVO> treeVOs = TreeUtils.convertFunVOByParent(Arrays.asList(funAry), parent_id, null);
		return treeVOs;
	}

	public List<TreeVO> getFunTree(String parent_id) {
		List<FunVO> funVOs = getFunVOs(parent_id, false);
		List<TreeVO> treeVOs = TreeUtils.convertFunVOByParent(funVOs, parent_id, null);
		return treeVOs;
	}

	public List<FunVO> getFunVOs(String parent_id, boolean encludeLockedFlag) {
		String sql = "select * from nw_fun WITH(NOLOCK) where isnull(dr,0)=0 ";
		if(!encludeLockedFlag) {
			sql += " and isnull(locked_flag,'N')='N'";
		}
		sql += " order by display_order asc,fun_code asc";
		List<FunVO> funVOs = dao.queryForList(sql, FunVO.class);
		List<FunVO> children = new ArrayList<FunVO>();
		setChildren(funVOs, parent_id, children);
		return children;
	}

	/**
	 * 该类可能在其他地方使用,如权限过滤器,此时无法使用自动注入的dao
	 */
	public boolean isPowerByUserFun(String pk_user, String pk_fun) {
		String sql = "select 1 from nw_power_fun "
				+ " WITH(NOLOCK) where pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where pk_user=? and isnull(dr,0)=0) "
				+ "and pk_fun=? and isnull(dr,0)=0";
		List<String> funAry = NWDao.getInstance().queryForList(sql, String.class, pk_user, pk_fun);
		if(funAry != null && funAry.size() > 0) {
			return true;
		}
		return false;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0] && fieldVO.getItemkey().equals("fun_property")) {
				// 节点类型
				fieldVO.setUserdefine1("afterChangeFun_property(value,originalValue)");
			}
		}
		return templetVO;
	}

	public FunVO getFunVOByBillType(String bill_type) {
		String strWhere = "(dr=0 or dr is null) and bill_type=?";
		return dao.queryByCondition(FunVO.class, strWhere, bill_type);
	}

	public List<TreeVO> getBillTypeTree() {
		List<FunVO> funVOs = getPowerFunVO(false);
		for(int i = 0; i < funVOs.size(); i++) {
			// 没有定义单据类型，或者没有勾选“使用编码规则”
			if(StringUtils.isBlank(funVOs.get(i).getBill_type()) || funVOs.get(i).getIf_code_rule() == null
					|| !funVOs.get(i).getIf_code_rule().booleanValue()) {
				funVOs.remove(i);
				i--;
			}
		}
		return convertToTreeVO(funVOs);
	}

	private List<TreeVO> convertToTreeVO(List<FunVO> funVOs) {
		if(funVOs == null || funVOs.size() == 0) {
			return null;
		}
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		for(FunVO funVO : funVOs) {
			TreeVO treeNode = new TreeVO();
			treeNode.setId(funVO.getBill_type());
			treeNode.setCode(funVO.getFun_code());
			treeNode.setText(funVO.getBill_type() + " " + funVO.getFun_name());
			treeNode.setLeaf(true);
			treeVOs.add(treeNode);
		}
		return treeVOs;
	}

	public List<TreeVO> getFunTree(String pk_user, String pk_corp, String parent_id) {
		List<FunVO> allFunVOs = getPowerFunVO(false);
		List<FunVO> funVOs = new ArrayList<FunVO>();
		setChildren(allFunVOs, parent_id, funVOs);
		return TreeUtils.convertFunVOByParent(funVOs, parent_id, null);
	}

	public List<TreeVO> getFunTreeWithBtn(String pk_user, String pk_corp, String parent_id) {
		List<FunVO> allFunVOs = getPowerFunVOWithBtn(false);
		List<FunVO> funVOs = new ArrayList<FunVO>();
		setChildren(allFunVOs, parent_id, funVOs);
		return TreeUtils.convertFunVOByParent(funVOs, parent_id, null);
	}

	public TreeVO[] getFunTree(String pk_user, String pk_corp, String parent_id, boolean encludeLockedFlag) {
		List<FunVO> allFunVOs = getPowerFunVO(encludeLockedFlag);
		List<FunVO> funVOs = new ArrayList<FunVO>();
		setChildren(allFunVOs, parent_id, funVOs);
		List<TreeVO> treeVOs = TreeUtils.convertFunVOByParent(funVOs, parent_id, null);
		TreeVO[] treeAry = treeVOs.toArray(new TreeVO[treeVOs.size()]);
		// Arrays.sort(treeAry, new TreeVOComparator());
		return treeAry;
	}

	/**
	 * 返回顶级的功能菜单
	 */
	public FunVO[] getTopLevelFunVOs(String pk_user, String pk_corp) {
		List<FunVO> allFunVOs = getPowerFunVO(false);
		List<FunVO> funVOs = new ArrayList<FunVO>();
		for(int i = 0; i < allFunVOs.size(); i++) {
			if(Constants.SYSTEM_CODE.equals(allFunVOs.get(i).getParent_id())) {
				funVOs.add(allFunVOs.get(i));
			}
		}
		FunVO[] funAry = funVOs.toArray(new FunVO[funVOs.size()]);
		// Arrays.sort(funAry, new FunComparator());
		return funAry;
	}

	/**
	 * 返回所有授权的功能菜单,不包括按钮节点
	 * 
	 * @return
	 */
	public List<FunVO> getPowerFunVO(boolean encludeLockedFlag) {
		if(Constants.SYSTEM_CODE.equals(WebUtils.getLoginInfo().getPk_user())) {
			// 超级管理员登录
			String sql = "select * from nw_fun WITH(NOLOCK) where isnull(dr,0)=0";
			if(!encludeLockedFlag) {
				sql += " and isnull(locked_flag,'N')='N'";
			}
			sql += " and fun_property <> " + FunRegisterConst.POWERFUL_BUTTON
					+ " order by display_order asc,fun_code asc";
			return dao.queryForList(sql, FunVO.class);
		} else {
			String sql = "select * from nw_fun WITH(NOLOCK) where isnull(dr,0)=0";
			if(!encludeLockedFlag) {
				sql += " and isnull(locked_flag,'N')='N'";
			}
			sql += " and pk_fun in (select pk_fun from nw_power_fun WITH(NOLOCK) where isnull(dr,0)=0 "
					+ " and pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where pk_user=? and isnull(dr,0)=0)) and fun_property <> "
					+ FunRegisterConst.POWERFUL_BUTTON + " order by display_order asc,fun_code asc";
			return dao.queryForList(sql, FunVO.class, WebUtils.getLoginInfo().getPk_user());
		}
	}

	public List<FunVO> getPowerFunVOWithBtn(boolean encludeLockedFlag) {
		if(Constants.SYSTEM_CODE.equals(WebUtils.getLoginInfo().getPk_user())) {
			// 超级管理员登录
			String sql = "select * from nw_fun WITH(NOLOCK) where isnull(dr,0)=0";
			if(!encludeLockedFlag) {
				sql += " and isnull(locked_flag,'N')='N'";
			}
			sql += " order by display_order asc,fun_code asc";
			return dao.queryForList(sql, FunVO.class);
		} else {
			String sql = "select * from nw_fun WITH(NOLOCK) where isnull(dr,0)=0";
			if(!encludeLockedFlag) {
				sql += " and isnull(locked_flag,'N')='N'";
			}
			sql += " and pk_fun in (select pk_fun from nw_power_fun WITH(NOLOCK) where isnull(dr,0)=0 "
					+ " and pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where pk_user=? and isnull(dr,0)=0))  order by display_order asc,fun_code asc";
			return dao.queryForList(sql, FunVO.class, WebUtils.getLoginInfo().getPk_user());
		}
	}

	/**
	 * 使用递归返回parent_id的所有子菜单
	 * 
	 * @param allFunVOs
	 * @param parent_id
	 * @return
	 */
	private void setChildren(List<FunVO> funVOs, String parent_id, List<FunVO> children) {
		if(parent_id == null) {
			parent_id = "";
		}
		for(int i = 0; i < funVOs.size(); i++) {
			FunVO funVO = funVOs.get(i);
			String newParent_id = funVO.getParent_id();
			if(newParent_id == null) {
				newParent_id = "";
			}
			if(parent_id.equals(newParent_id)) {
				children.add(funVO);
				setChildren(funVOs, funVO.getPk_fun(), children);
			}
		}
	}

	public List<FunVO> getByIf_code_rule(boolean if_code_rule) {
		String ifCodeRule = "Y";
		if(!if_code_rule) {
			ifCodeRule = "N";
		}
		FunVO[] funVOs = dao.queryForSuperVOArrayByCondition(FunVO.class,
				"isnull(locked_flag,'N')='N' and if_code_rule=? and fun_property=?", ifCodeRule,
				FunRegisterConst.LFW_FUNC_NODE);
		return Arrays.asList(funVOs);
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		FunVO funVO = (FunVO) billVO.getParentVO();
		// 如果节点已经分配了模板，那么不能删除
		TempletDistVO[] tdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(TempletDistVO.class, " pk_fun=?",
				funVO.getPk_fun());
		if(tdVOs != null && tdVOs.length > 0) {
			throw new BusiException("该节点已经分配了模板，不能删除！");
		}
		// 先删除子节点，再删除当前节点
		FunVO[] funVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(FunVO.class, "parent_id=?",
				funVO.getPk_fun());
		if(funVOs != null && funVOs.length > 0) {
			NWDao.getInstance().delete(funVOs);
		}
	}

	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processAfterSave(billVO, paramVO);
		// 如果父节点锁定了，那么子节点也要锁定
		FunVO parentVO = (FunVO) billVO.getParentVO();
		List<FunVO> childVOs = getFunVOs(parentVO.getPk_fun(), true);
		if(childVOs != null && childVOs.size() > 0) {
			for(FunVO childVO : childVOs) {
				if(parentVO.getLocked_flag() != null && parentVO.getLocked_flag().booleanValue()) {
					childVO.setLocked_flag(UFBoolean.TRUE);
				} else {
					childVO.setLocked_flag(UFBoolean.FALSE);
				}
				childVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(childVO);
			}
		}
	}
}
