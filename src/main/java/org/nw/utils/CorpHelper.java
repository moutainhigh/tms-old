package org.nw.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.redis.RedisDao;
import org.nw.vo.sys.CorpVO;
import org.nw.web.utils.WebUtils;

/**
 * 公司操作类，主要提供公司查询参数
 * 
 * @author xuqc
 * @date 2012-9-7 下午09:15:17
 */
public class CorpHelper {

	public static List<CorpVO> getCorpVOs(String parent_id) {
		String sql = "select * from nw_corp WITH(NOLOCK) where isnull(dr,0)=0 ";
		sql += " order by corp_code asc";
		List<CorpVO> corpVOs = NWDao.getInstance().queryForListWithCache(sql, CorpVO.class);
		List<CorpVO> children = new ArrayList<CorpVO>();
		setChildren(corpVOs, parent_id, children);
		return children;
	}

	/**
	 * 返回当前公司和子公司
	 * 
	 * @return
	 */
	public static List<CorpVO> getCurrentCorpVOsWithChildren() {
		String parent_id = WebUtils.getLoginInfo().getPk_corp();
		CorpVO parentVO = NWDao.getInstance().queryByConditionWithCache(CorpVO.class, "pk_corp=?", parent_id);
		List<CorpVO> allCorpVOs = getCorpVOs(null);// 所有公司
		List<CorpVO> allChildVOs = new ArrayList<CorpVO>();
		allChildVOs.add(parentVO);
		setChildrenCorpVOs(parentVO, allCorpVOs, allChildVOs);
		return allChildVOs;
	}

	/**
	 * 返回当前公司和子公司
	 * 
	 * @return
	 */
	//yaojiie 2015 12 08重构方法，用于登陆时将公司信息和子公司信息，放入SISSION，避免后续动作多次访问数据库。
	public static List<CorpVO> getCurrentCorpVOsWithChildren(String pk_corp) {
		String parent_id = pk_corp;
		CorpVO parentVO = NWDao.getInstance().queryByConditionWithCache(CorpVO.class, "pk_corp=?", parent_id);
		List<CorpVO> allCorpVOs = getCorpVOs(null);// 所有公司
		List<CorpVO> allChildVOs = new ArrayList<CorpVO>();
		allChildVOs.add(parentVO);
		setChildrenCorpVOs(parentVO, allCorpVOs, allChildVOs);
		return allChildVOs;
	}
	
	
	/**
	 * 当前公司和子公司以及父级公司
	 * 
	 * @return
	 */
	public static List<CorpVO> getCurrentCorpVOsWithChildrenAndParent() {
		// 得到所有公司
		String sql = "select * from nw_corp WITH(NOLOCK) where isnull(dr,0)=0 ";
		sql += " order by corp_code asc";
		List<CorpVO> corpVOs = NWDao.getInstance().queryForListWithCache(sql, CorpVO.class);
		List<CorpVO> resultList = new ArrayList<CorpVO>();
		String currentPkCorp = WebUtils.getLoginInfo() == null ? "0001": WebUtils.getLoginInfo().getPk_corp();
		setChildren(corpVOs, currentPkCorp, resultList);
		setParent(corpVOs, currentPkCorp, resultList);
		return resultList;
	}

	/**
	 * 当前公司和子公司以及父级公司
	 * 
	 * @return
	 */
	public static String getCurrentCorpWithChildrenAndParent() {
		List<CorpVO> resultList = getCurrentCorpVOsWithChildrenAndParent();
		StringBuffer sb = new StringBuffer();
		for(CorpVO corpVO : resultList) {
			sb.append("'");
			sb.append(corpVO.getPk_corp());
			sb.append("',");
		}
		String corpCond = null;
		if(sb.length() > 0) {
			corpCond = sb.substring(0, sb.length() - 1);
		}
		return "pk_corp in (" + corpCond + ")";
	}

	/**
	 * 返回当前公司以及子公司
	 * 
	 * @return
	 */
	public static String getCurrentCorpWithChildren2() {
		// 根据当前登录用户的公司，查找所有子公司，不分级次
		//List<CorpVO> corpVOs = getCurrentCorpVOsWithChildren();
		List<CorpVO> corpVOs = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());
		StringBuffer sb = new StringBuffer();
		for(CorpVO corpVO : corpVOs) {
			sb.append("'");
			sb.append(corpVO.getPk_corp());
			sb.append("',");
		}
		String corpCond = null;
		if(sb.length() > 0) {
			corpCond = sb.substring(0, sb.length() - 1);
		}
		return corpCond;
	}

	/**
	 * 返回当前公司以及子公司
	 * 
	 * @return
	 */
	//yaojiie 2015 12 08重构方法，用于登陆时将公司信息和子公司信息，放入SISSION，避免后续动作多次访问数据库。
	public static String getCurrentCorpWithChildren2(String pk_corp) {
		// 根据当前登录用户的公司，查找所有子公司，不分级次
		List<CorpVO> corpVOs = getCurrentCorpVOsWithChildren(pk_corp);
		StringBuffer sb = new StringBuffer();
		for(CorpVO corpVO : corpVOs) {
			sb.append("'");
			sb.append(corpVO.getPk_corp());
			sb.append("',");
		}
		String corpCond = null;
		if(sb.length() > 0) {
			corpCond = sb.substring(0, sb.length() - 1);
		}
		return corpCond;
	}
	
	/**
	 * 当前公司及子公司的条件
	 * 
	 * @param table_prefix
	 * @return
	 */
	public static String getCurrentCorpWithChildren(String table_prefix) {
		// 根据当前登录用户的公司，查找所有子公司，不分级次
		//List<CorpVO> corpVOs = getCurrentCorpVOsWithChildren();
		List<CorpVO> corpVOs = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());
		StringBuffer sb = new StringBuffer();
		for(CorpVO corpVO : corpVOs) {
			sb.append("'");
			sb.append(corpVO.getPk_corp());
			sb.append("',");
		}
		String corpCond = null;
		if(sb.length() > 0) {
			corpCond = sb.substring(0, sb.length() - 1);
		}
		StringBuffer condBuf = new StringBuffer();
		if(StringUtils.isNotBlank(table_prefix)) {
			condBuf.append(table_prefix);
			condBuf.append(".");
		}
		condBuf.append("pk_corp in (").append(corpCond).append(")");
		return condBuf.toString();
	}

	/**
	 * 当前公司及子公司
	 * 
	 * @return
	 */
	public static String getCurrentCorpWithChildren() {
		return getCurrentCorpWithChildren(null);
	}

	/**
	 * 当前公司、子公司、和集团
	 * 
	 * @return
	 */
	public static String getCurrentCorpWithChildrenAndGroup() {
		// 根据当前登录用户的公司，查找所有子公司，不分级次
		//List<CorpVO> corpVOs = getCurrentCorpVOsWithChildren();
		List<CorpVO> corpVOs = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());
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
		return "(pk_corp in (" + corpCond + ") or pk_corp is null)"; // null也是集团的数据
	}

	/**
	 * 当前公司和集团
	 * 
	 * @return
	 */
	public static String getCurrentCorpWithGroup() {
		return "(pk_corp in ('" + WebUtils.getLoginInfo().getPk_corp() + "','" + Constants.SYSTEM_CODE
				+ "') or pk_corp is null)";
	}

	/**
	 * 当前公司
	 * 
	 * @return
	 */
	public static String getCurrentCorp() {
		return "pk_corp='" + WebUtils.getLoginInfo().getPk_corp() + "'";
	}

	/**
	 * 递归，返回所有子节点
	 * 
	 * @param parentVO
	 * @param allCorpVOs
	 * @param allChildVOs
	 */
	private static void setChildrenCorpVOs(CorpVO parentVO, List<CorpVO> allCorpVOs, List<CorpVO> allChildVOs) {
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
	private static void setChildren(List<CorpVO> corpVOs, String parent_id, List<CorpVO> resultList) {
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
				resultList.add(corpVO);
				setChildren(corpVOs, corpVO.getPk_corp(), resultList);
			}
		}
	}

	/**
	 * 使用递归返回parent_id的所有父菜单
	 * 
	 * @param corpVOs
	 * @param currentPkCorp
	 * @return
	 */
	private static void setParent(List<CorpVO> corpVOs, String currentPkCorp, List<CorpVO> resultList) {
		if(currentPkCorp == null) {
			currentPkCorp = "";
		}
		String fatherPkCorp = null;
		for(int i = 0; i < corpVOs.size(); i++) {
			CorpVO corpVO = corpVOs.get(i);
			if(corpVO.getPk_corp().equals(currentPkCorp)) {
				resultList.add(corpVO);
				fatherPkCorp = corpVO.getFathercorp();
				setParent(corpVOs, fatherPkCorp, resultList);
			}
		}
	}
}
