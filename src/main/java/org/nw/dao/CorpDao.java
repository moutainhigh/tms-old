package org.nw.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.vo.sys.CorpVO;


/**
 * 公司目录参照的dao
 * 
 * @author xuqc
 * @date 2010-12-3
 */
public class CorpDao {
	public static final String ROOT_CORP_ID = "0001";
	public static final String ROOT_CORP_UNITNAME = "集团";
	private static CorpVO root = null;

	/**
	 * 获取根节点
	 * 
	 * @return
	 */
	public CorpVO getRoot() {
		if(root == null) {
			root = new CorpVO();
			root.setPk_corp(ROOT_CORP_ID);
			root.setCorp_code(ROOT_CORP_ID);
			root.setCorp_name(ROOT_CORP_UNITNAME);
			root.setShortname(ROOT_CORP_UNITNAME);
		}
		return root;
	}

	/**
	 * 根据上级公司查询所有子公司
	 * 
	 * @param parentId
	 * @return
	 */
	public List<CorpVO> getChildren(String parentId, String whereClause) {
		List<CorpVO> list = null;

		if(StringUtils.isBlank(parentId)) {
			String sql = "select * from nw_corp  WITH(NOLOCK) where (fathercorp is null or fathercorp='') and isnull(dr,0)=0 ";
			if(StringUtils.isNotBlank(whereClause)) {
				sql += " and " + whereClause;
			}
			sql += " order by corp_code";
			list = NWDao.getInstance().queryForList(sql, CorpVO.class);
		} else {
			String sql = "select * from nw_corp  WITH(NOLOCK) where fathercorp=? and isnull(dr,0)=0";
			if(StringUtils.isNotBlank(whereClause)) {
				sql += " and " + whereClause;
			}
			sql += " order by corp_code";
			list = NWDao.getInstance().queryForList(sql, CorpVO.class, parentId);
		}
		return list;
	}

	/**
	 * 获取子节点数目
	 * 
	 * @param parentId
	 * @return
	 */
	public Long getChildCount(String parentId) {
		String sql = "select count(pk_corp) from nw_corp a  WITH(NOLOCK) where a.fathercorp=? and isnull(a.dr,0)=0";
		return NWDao.getInstance().getJdbcTemplate().queryForLong(sql, parentId);
	}

	/**
	 * 判断某个节点是否有子节点
	 * 
	 * @param menuId
	 * @return
	 */
	public boolean hasChild(String parentId) {
		if(ROOT_CORP_ID.equalsIgnoreCase(parentId)) {
			return true;
		}
		long count = this.getChildCount(parentId);
		return count != 0;
	}
}
