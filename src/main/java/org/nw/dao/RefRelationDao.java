package org.nw.dao;

import java.util.List;

import org.nw.vo.sys.RefRelationVO;


/**
 * 关系表的dao
 * 
 * @author xuqc
 * @date 2010-12-3
 */
public class RefRelationDao {

	public boolean isReferenced(String tableName, String key) {
		List<RefRelationVO> relationList = getRelationListByTableName(tableName, false);
		return checkReferenceHelper(tableName, key, relationList);
	}

	/**
	 * 返回注册的关系
	 * 
	 * @param tableName
	 * @param isModifyCheck
	 * @return
	 */
	public List<RefRelationVO> getRelationListByTableName(String tableName, boolean isModifyCheck) {
		String sql = "select referencedtablekey, referencingtablename, referencingtablecolumn, referencingtablename from nw_ref_relation  where referencedtablename =? and isnull(dr,0)=0";
		if(isModifyCheck) {
			sql += " and ismodifycheck = 'Y'";
		}
		return NWDao.getInstance().queryForListWithCache(sql, RefRelationVO.class, tableName);
	}

	private boolean checkReferenceHelper(String tableName, String key, List<RefRelationVO> relationList) {
		// 没有引用该表的情况：
		if(relationList.size() == 0)
			return false;
		for(RefRelationVO relation : relationList) {
			StringBuilder buf = new StringBuilder();
			buf.append("select ");
			buf.append("count(1)");
			buf.append(" from ");
			buf.append(relation.getReferencingtablename());
			buf.append(" WITH(NOLOCK) where ");
			buf.append(relation.getReferencingtablecolumn());
			buf.append("=");
			buf.append("?");
			buf.append(" and isnull(dr,0)=0");

			Integer count = NWDao.getInstance().queryForObject(buf.toString(), Integer.class, key);
			if(count > 0) {
				return true;
			}
		}
		return false;
	}
}
