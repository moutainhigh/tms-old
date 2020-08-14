package org.nw.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.constants.FunRegisterConst;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.utils.WebUtils;

/**
 * TreeVO的工具类，提供了各种VO转换成TreeVO的方法，平台级
 * 
 * @author xuqc
 * @date 2012-6-16 下午02:44:25
 */
public class TreeUtils {

	/**
	 * 一个功能节点VO转TreeVO结构
	 * 
	 * @param funVO
	 * @param isLeaf
	 * @param checked
	 *            是否选中该节点,注意这个变量是Boolean类型,如果为null,表示不生成checkbox框
	 * @return
	 */
	public static TreeVO convertFunVO(FunVO funVO, boolean isLeaf, Boolean checked) {
		if(funVO == null) {
			return null;
		}
		TreeVO treeNode = new TreeVO();
		treeNode.setId(funVO.getPk_fun());
		treeNode.setCode(funVO.getFun_code());
		treeNode.setText(funVO.getFun_name());
		if(WebUtils.getLoginInfo() != null){
			if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				treeNode.setText(funVO.getFun_en_name());	
			}
		}
		
		treeNode.setUrl(funVO.getClass_name());
		if(funVO.getLocked_flag() != null && funVO.getLocked_flag().booleanValue()) {
			treeNode.setLocked(true);
		}
		treeNode.addProperty(FunVO.FUN_CODE, funVO.getFun_code());
		treeNode.addProperty(FunVO.FUN_NAME, funVO.getFun_name());
		treeNode.addProperty(FunVO.PK_FUN, funVO.getPk_fun());
		treeNode.addProperty(FunVO.FUN_PROPERTY, funVO.getFun_property());
		treeNode.setExpanded(false);
		treeNode.setChecked(checked);
		if(funVO.getFun_code().length() == 2) {
			// XXX顶级功能节点都是非叶子节点
			treeNode.setLeaf(false);
		} else {
			treeNode.setLeaf(isLeaf);
		}
		if(funVO.getParent_id() != null || !funVO.getParent_id().equals(Constants.SYSTEM_CODE)) {
			// 只要不是根节点，那么可以拖动，但是还需要在创建tree的时候使用统一的开关enableDD
			treeNode.setDraggable(true);
		}
		return treeNode;
	}

	/**
	 * 同一级别的节点转TreeVO结构
	 * 
	 * @param topLevelVOs
	 *            同一级别的功能节点，没有上下级关系
	 * @param children
	 * @return
	 */
	public static List<TreeVO> convertFunVO(List<FunVO> topLevelVOs, List<FunVO> children, List<String> checkedFun) {
		List<TreeVO> treeNodes = new ArrayList<TreeVO>();
		for(FunVO vo : topLevelVOs) {
			boolean isLeaf;
			if(vo.getFun_property().intValue() == FunRegisterConst.INEXECUTABLE_FUNC_NODE) {
				// 虚功能节点，使用folder展示
				isLeaf = false;
			} else {
				isLeaf = !hasChildFunVO(vo, children);
			}
			TreeVO treeNode = convertFunVO(vo, isLeaf, checkedFun == null ? null : checkedFun.contains(vo.getPk_fun()));
			treeNodes.add(treeNode);
		}
		return treeNodes;
	}

	/**
	 * 将查询出来的部门集合转换成Tree格式
	 * 
	 * @param funVOs
	 * @param parent_id
	 *            父级节点id
	 * @param checkedFun
	 *            有权限的节点,在转换成treeVO时根据这个来判定是否是checked状态
	 * @return
	 */
	public static List<TreeVO> convertFunVOByParent(List<FunVO> funVOs, String parent_id, List<String> checkedFun) {
		if(parent_id == null) {
			parent_id = "";
		}
		List<FunVO> topLevel = new ArrayList<FunVO>();
		for(FunVO vo : funVOs) {
			if(vo.getParent_id() == null) {
				vo.setParent_id("");
			}
			if(parent_id.equals(vo.getParent_id())) {
				topLevel.add(vo);
			}
		}
		List<TreeVO> results = TreeUtils.convertFunVO(topLevel, funVOs, checkedFun);
		TreeUtils.setChildrenFunVOs(topLevel, funVOs, results, checkedFun);
		return results;
	}

	/**
	 * 设置每个treeNode的下级节点
	 * 
	 * @param topNodeList
	 * @param allChilds
	 * @param results
	 */
	private static void setChildrenFunVOs(List<FunVO> parentVOs, List<FunVO> allChildVOs, List<TreeVO> results,
			List<String> checkedFun) {
		for(int i = 0; i < parentVOs.size(); i++) {
			FunVO parentVO = parentVOs.get(i);
			TreeVO treeNode = results.get(i);
			if(StringUtils.isBlank(parentVO.getParent_id())) {
				// 第一级展开
				treeNode.setExpanded(true);
			} else {
				treeNode.setExpanded(false);
			}
			List<FunVO> childrenVOs = new ArrayList<FunVO>();
			for(int j = 0; j < allChildVOs.size(); j++) {
				FunVO childVO = allChildVOs.get(j);
				// 以下条件用来判断是否是子节点
				if(parentVO.getPk_fun().equals(childVO.getParent_id())) {
					childrenVOs.add(childVO);
				}
			}
			if(childrenVOs.size() > 0) {
				List<TreeVO> childrenResults = convertFunVO(childrenVOs, allChildVOs, checkedFun);
				treeNode.setChildren(childrenResults);
				setChildrenFunVOs(childrenVOs, allChildVOs, childrenResults, checkedFun);
			}
		}
	}

	/**
	 * 判断当前功能节点是否包含子节点
	 * 
	 * @param parentVO
	 * @param children
	 * @return
	 */
	private static boolean hasChildFunVO(FunVO parentVO, List<FunVO> children) {
		for(FunVO child : children) {
			if(parentVO.getPk_fun().equals(child.getParent_id())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 公司VO转TreeVO结构
	 * 
	 * @param corpVO
	 * @param isLeaf
	 * @return
	 */
	public static TreeVO convertCorpVO(CorpVO corpVO, boolean isLeaf) {
		if(corpVO == null) {
			return null;
		}
		TreeVO treeNode = new TreeVO();
		treeNode.setId(corpVO.getPk_corp());
		treeNode.setCode(corpVO.getCorp_code());
		treeNode.setText(corpVO.getCorp_name());
		treeNode.setExpanded(false);
		treeNode.setLeaf(isLeaf);
		return treeNode;
	}

	/**
	 * 同一级别的节点转TreeVO结构
	 * 
	 * @param topLevelVOs
	 *            同一级别的功能节点，没有上下级关系
	 * @param children
	 * @return
	 */
	public static List<TreeVO> convertCorpVO(List<CorpVO> topLevelVOs, List<CorpVO> children) {
		List<TreeVO> treeNodes = new ArrayList<TreeVO>();
		for(CorpVO vo : topLevelVOs) {
			boolean isLeaf = !hasChildCorpVO(vo, children);
			TreeVO treeNode = convertCorpVO(vo, isLeaf);
			treeNodes.add(treeNode);
		}
		return treeNodes;
	}

	/**
	 * 将查询出来的部门集合转换成Tree格式
	 * 
	 * @param corpVOs
	 * @param parent_id
	 *            父级节点id
	 * @param rootNode
	 * @return
	 */
	public static List<TreeVO> convertCorpVOByParent(List<CorpVO> corpVOs, String parent_id) {
		if(parent_id == null) {
			parent_id = "";
		}
		List<CorpVO> topLevel = new ArrayList<CorpVO>();
		for(CorpVO vo : corpVOs) {
			if(vo.getFathercorp() == null) {
				vo.setFathercorp("");
			}
			if(parent_id.equals(vo.getFathercorp())) {
				topLevel.add(vo);
			}
		}
		List<TreeVO> results = TreeUtils.convertCorpVO(topLevel, corpVOs);
		TreeUtils.setChildrenCorpVOs(topLevel, corpVOs, results);
		return results;
	}

	/**
	 * 设置每个treeNode的下级节点
	 * 
	 * @param topNodeList
	 * @param allChilds
	 * @param results
	 */
	private static void setChildrenCorpVOs(List<CorpVO> parentVOs, List<CorpVO> allChildVOs, List<TreeVO> results) {
		for(int i = 0; i < parentVOs.size(); i++) {
			CorpVO parentVO = parentVOs.get(i);
			TreeVO treeNode = results.get(i);
			if(StringUtils.isBlank(parentVO.getFathercorp())) {
				// 第一级展开
				treeNode.setExpanded(true);
			} else {
				treeNode.setExpanded(false);
			}
			List<CorpVO> childrenVOs = new ArrayList<CorpVO>();
			for(int j = 0; j < allChildVOs.size(); j++) {
				CorpVO childVO = allChildVOs.get(j);
				// 以下条件用来判断是否是子节点
				if(parentVO.getPk_corp().equals(childVO.getFathercorp())) {
					childrenVOs.add(childVO);
				}
			}
			if(childrenVOs.size() > 0) {
				List<TreeVO> childrenResults = convertCorpVO(childrenVOs, allChildVOs);
				treeNode.setChildren(childrenResults);
				setChildrenCorpVOs(childrenVOs, allChildVOs, childrenResults);
			}
		}
	}

	/**
	 * 判断当前公司是否包含子节点
	 * 
	 * @param parentVO
	 * @param children
	 * @return
	 */
	public static boolean hasChildCorpVO(CorpVO parentVO, List<CorpVO> children) {
		for(CorpVO child : children) {
			if(parentVO.getPk_corp().equals(child.getFathercorp())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 部门VO转TreeVO结构
	 * 
	 * @param deptVO
	 * @param isLeaf
	 * @return
	 */
	public static TreeVO convertDeptVO(DeptVO deptVO, boolean isLeaf) {
		if(deptVO == null) {
			return null;
		}
		TreeVO treeNode = new TreeVO();
		treeNode.setId(deptVO.getPk_dept());
		treeNode.setCode(deptVO.getDept_code());
		treeNode.setText(deptVO.getDept_name());
		treeNode.setExpanded(false);
		treeNode.setLeaf(isLeaf);
		return treeNode;
	}

	/**
	 * 同一级别的节点转TreeVO结构
	 * 
	 * @param topLevelVOs
	 *            同一级别的功能节点，没有上下级关系
	 * @param children
	 * @return
	 */
	public static List<TreeVO> convertDeptVO(List<DeptVO> topLevelVOs, List<DeptVO> children) {
		List<TreeVO> treeNodes = new ArrayList<TreeVO>();
		for(DeptVO vo : topLevelVOs) {
			boolean isLeaf = !hasChildDeptVO(vo, children);
			TreeVO treeNode = convertDeptVO(vo, isLeaf);
			treeNodes.add(treeNode);
		}
		return treeNodes;
	}

	/**
	 * 将查询出来的部门集合转换成Tree格式
	 * 
	 * @param deptVOs
	 * @param parent_id
	 *            父级节点id
	 * @param rootNode
	 * @return
	 */
	public static List<TreeVO> convertDeptVOByParent(List<DeptVO> deptVOs, String parent_id) {
		if(parent_id == null) {
			parent_id = "";
		}
		List<DeptVO> topLevel = new ArrayList<DeptVO>();
		for(DeptVO vo : deptVOs) {
			if(vo.getFatherdept() == null) {
				vo.setFatherdept("");
			}
			if(parent_id.equals(vo.getFatherdept())) {
				topLevel.add(vo);
			}
		}
		List<TreeVO> results = TreeUtils.convertDeptVO(topLevel, deptVOs);
		TreeUtils.setChildrenDeptVOs(topLevel, deptVOs, results);
		return results;
	}

	/**
	 * 设置每个treeNode的下级节点
	 * 
	 * @param topNodeList
	 * @param allChilds
	 * @param results
	 */
	private static void setChildrenDeptVOs(List<DeptVO> parentVOs, List<DeptVO> allChildVOs, List<TreeVO> results) {
		for(int i = 0; i < parentVOs.size(); i++) {
			DeptVO parentVO = parentVOs.get(i);
			TreeVO treeNode = results.get(i);
			if(StringUtils.isBlank(parentVO.getFatherdept())) {
				// 第一级展开
				treeNode.setExpanded(true);
			} else {
				treeNode.setExpanded(false);
			}
			List<DeptVO> childrenVOs = new ArrayList<DeptVO>();
			for(int j = 0; j < allChildVOs.size(); j++) {
				DeptVO childVO = allChildVOs.get(j);
				// 以下条件用来判断是否是子节点
				if(parentVO.getPk_dept().equals(childVO.getFatherdept())) {
					childrenVOs.add(childVO);
				}
			}
			if(childrenVOs.size() > 0) {
				List<TreeVO> childrenResults = convertDeptVO(childrenVOs, allChildVOs);
				treeNode.setChildren(childrenResults);
				setChildrenDeptVOs(childrenVOs, allChildVOs, childrenResults);
			}
		}
	}

	/**
	 * 判断当前功能节点是否包含子节点
	 * 
	 * @param parentVO
	 * @param children
	 * @return
	 */
	private static boolean hasChildDeptVO(DeptVO parentVO, List<DeptVO> children) {
		for(DeptVO child : children) {
			if(parentVO.getPk_dept().equals(child.getFatherdept())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回自定义的根节点
	 * 
	 * @param rootId
	 * @param rootText
	 * @param isLeaf
	 * @param isExpanded
	 * @return
	 * @author xuqc
	 * @date 2012-3-8
	 * 
	 */
	private static TreeVO getRootNode(String rootId, String rootText, boolean isLeaf, boolean isExpanded) {
		TreeVO treeNode = new TreeVO();
		treeNode.setId(rootId);
		treeNode.setText(rootText);
		treeNode.setLeaf(isLeaf);
		treeNode.setExpanded(isExpanded);
		return treeNode;
	}

	/**
	 * 为树增加默认的根节点，默认展开根节点
	 * 
	 * @param rootId
	 * @param rootText
	 * @param list
	 * @return
	 * @author xuqc
	 * @date 2012-3-8
	 * 
	 */
	public static List<TreeVO> addRootNode(String rootId, String rootText, List<TreeVO> list) {
		return addRootNode(rootId, rootText, list, true);
	}

	/**
	 * 为树增加默认的根节点
	 * 
	 * @param rootId
	 * @param rootText
	 * @param list
	 * @param isExpanded
	 * @return
	 * @author xuqc
	 * @date 2012-3-8
	 * 
	 */
	public static List<TreeVO> addRootNode(String rootId, String rootText, List<TreeVO> list, boolean isExpanded) {
		boolean isLeaf = false;
		if(list == null || list.size() == 0) {
			isLeaf = true;
		}
		TreeVO root = getRootNode(rootId, rootText, isLeaf, isExpanded);
		root.setChildren(list);
		List<TreeVO> tree = new ArrayList<TreeVO>();
		tree.add(root);
		return tree;
	}

}
