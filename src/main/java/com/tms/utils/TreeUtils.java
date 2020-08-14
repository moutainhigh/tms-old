package com.tms.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.vo.TreeVO;

import com.tms.vo.base.AreaVO;

/**
 * TreeVO的工具类，提供了各种VO转换成TreeVO的方法，业务级
 * 
 * @author xuqc
 * @date 2013-8-23 下午12:04:43
 */
public class TreeUtils extends org.nw.utils.TreeUtils {
	/**
	 * 区域VO转TreeVO结构
	 * 
	 * @param areaVO
	 * @param isLeaf
	 * @return
	 */
	public static TreeVO convertAreaVO(AreaVO areaVO, boolean isLeaf) {
		if(areaVO == null) {
			return null;
		}
		TreeVO treeNode = new TreeVO();
		treeNode.setId(areaVO.getPk_area());
		treeNode.setCode(areaVO.getCode());
		treeNode.setText(areaVO.getName());
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
	public static List<TreeVO> convertAreaVO(List<AreaVO> topLevelVOs, List<AreaVO> children) {
		List<TreeVO> treeNodes = new ArrayList<TreeVO>();
		for(AreaVO vo : topLevelVOs) {
			boolean isLeaf = !hasChildAreaVO(vo, children);
			TreeVO treeNode = convertAreaVO(vo, isLeaf);
			treeNodes.add(treeNode);
		}
		return treeNodes;
	}

	/**
	 * 将查询出来的部门集合转换成Tree格式
	 * 
	 * @param areaVOs
	 * @param parent_id
	 *            父级节点id
	 * @param rootNode
	 * @return
	 */
	public static List<TreeVO> convertAreaVOByParent(List<AreaVO> areaVOs, String parent_id) {
		if(parent_id == null) {
			parent_id = "";
		}
		List<AreaVO> topLevel = new ArrayList<AreaVO>();
		for(AreaVO vo : areaVOs) {
			if(vo.getParent_id() == null) {
				vo.setParent_id("");
			}
			if(parent_id.equals(vo.getParent_id())) {
				topLevel.add(vo);
			}
		}
		List<TreeVO> results = TreeUtils.convertAreaVO(topLevel, areaVOs);
		TreeUtils.setChildrenAreaVOs(topLevel, areaVOs, results);
		return results;
	}

	/**
	 * 设置每个treeNode的下级节点,这里设置为public，需要调用到
	 * 
	 * @param topNodeList
	 * @param allChilds
	 * @param results
	 */
	public static void setChildrenAreaVOs(List<AreaVO> parentVOs, List<AreaVO> allChildVOs, List<TreeVO> results) {
		for(int i = 0; i < parentVOs.size(); i++) {
			AreaVO parentVO = parentVOs.get(i);
			TreeVO treeNode = results.get(i);
			if(StringUtils.isBlank(parentVO.getParent_id())) {
				// 第一级展开
				treeNode.setExpanded(true);
			} else {
				treeNode.setExpanded(false);
			}
			List<AreaVO> childrenVOs = new ArrayList<AreaVO>();
			for(int j = 0; j < allChildVOs.size(); j++) {
				AreaVO childVO = allChildVOs.get(j);
				// 以下条件用来判断是否是子节点
				if(parentVO.getPk_area().equals(childVO.getParent_id())) {
					childrenVOs.add(childVO);
				}
			}
			if(childrenVOs.size() > 0) {
				List<TreeVO> childrenResults = convertAreaVO(childrenVOs, allChildVOs);
				treeNode.setChildren(childrenResults);
				setChildrenAreaVOs(childrenVOs, allChildVOs, childrenResults);
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
	private static boolean hasChildAreaVO(AreaVO parentVO, List<AreaVO> children) {
		for(AreaVO child : children) {
			if(parentVO.getPk_area().equals(child.getParent_id())) {
				return true;
			}
		}
		return false;
	}
}
