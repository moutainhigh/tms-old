package org.nw.utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.redis.RedisDao;

import com.tms.vo.base.AreaVO;

/**
 * 区域查找，主要查找区域信息
 * 
 * @author XIA
 * @date 2016 9 23
 */
public class AreaHelper {
	

	/**
	 * 返回当前区域和子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static List<AreaVO> getCurrentAreaVOWithChildren(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area=?"
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a,"
				+ " ts_area b WITH(NOLOCK)"
				+ " WHERE a.pk_area=b.parent_id AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0"
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK)";
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class, pk_area);
		return result;
	}
	
	/**
	 * 返回当前区域和子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static List<AreaVO> getCurrentAreaVOWithChildren(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area in" + NWUtils.buildConditionString(pk_areas)
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a,"
				+ " ts_area b WITH(NOLOCK)"
				+ " WHERE a.pk_area=b.parent_id AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0"
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK)";
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class);
		return result;
	}
	
	/**
	 * 返回当前区域和子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static List<AreaVO> getCurrentAreaVOWithChildren(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return new ArrayList<AreaVO>();
		}
		return getCurrentAreaVOWithChildren(listToArr(pk_areas));
	}
	
	/**
	 * 返回子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static List<AreaVO> getChildrenAreaVOs(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area =? "
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a, "
				+ " ts_area b WITH(NOLOCK) "
				+ " WHERE a.pk_area=b.parent_id  AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK) AND　pk_area　<>? ";
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class, pk_area,pk_area);
		return result;
	}
	
	/**
	 * 返回子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static List<AreaVO> getChildrenAreaVOs(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area in " + NWUtils.buildConditionString(pk_areas)
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a, "
				+ " ts_area b WITH(NOLOCK) "
				+ " WHERE a.pk_area=b.parent_id  AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK) AND　pk_area　not in " + NWUtils.buildConditionString(pk_areas) ;
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class);
		return result;
	}
	
	/**
	 * 返回子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static List<AreaVO> getChildrenAreaVOs(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return new ArrayList<AreaVO>();
		}
		return getChildrenAreaVOs(listToArr(pk_areas));
	}
	
	/**
	 * 返回当前区域和父集区域
	 * 
	 * @return
	 */
	public static List<AreaVO> getCurrentAreaVOWithParents(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return new ArrayList<AreaVO>();
		}
		return RedisDao.getInstance().getCurrentAreaVOWithParents(pk_area);
//		String sql = "WITH tab AS "
//				+ "("
//				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area=? "
//				+ " UNION ALL"
//				+ " SELECT b.* "
//				+ " FROM"
//				+ " tab a,"
//				+ " ts_area b WITH(NOLOCK)"
//				+ " WHERE a.parent_id=b.pk_area AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
//				+ ")"
//				+ "SELECT * FROM tab WITH(NOLOCK)";
//		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class, pk_area);
//		return result;
	}
	
	/**
	 * 返回当前区域和父集区域
	 * 
	 * @return
	 */
	public static List<AreaVO> getCurrentAreaVOWithParents(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area in " + NWUtils.buildConditionString(pk_areas)
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a,"
				+ " ts_area b WITH(NOLOCK)"
				+ " WHERE a.parent_id=b.pk_area AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK)";
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class);
		return result;
	}
	
	/**
	 * 返回当前区域和父集区域
	 * 
	 * @return
	 */
	public static List<AreaVO> getCurrentAreaVOWithParents(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return new ArrayList<AreaVO>();
		}
		return getCurrentAreaVOWithParents(listToArr(pk_areas));
	}
	
	/**
	 * 返回父集区域
	 * 
	 * @return
	 */
	public static List<AreaVO> getParentAreaVOs(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area =? "
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a, "
				+ " ts_area b WITH(NOLOCK) "
				+ " WHERE a.parent_id=b.pk_area AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK) AND　pk_area　<>? ";
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class, pk_area,pk_area);
		return result;
	}
	
	/**
	 * 返回父集区域
	 * 
	 * @return
	 */
	public static List<AreaVO> getParentAreaVOs(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return new ArrayList<AreaVO>();
		}
		String sql = "WITH tab AS "
				+ "("
				+ "SELECT * FROM ts_area WITH(NOLOCK) WHERE pk_area in " + NWUtils.buildConditionString(pk_areas)
				+ " UNION ALL"
				+ " SELECT b.* "
				+ " FROM"
				+ " tab a, "
				+ " ts_area b WITH(NOLOCK) "
				+ " WHERE a.parent_id=b.pk_area AND isnull(a.dr,0)=0 AND isnull(b.dr,0)=0 "
				+ ")"
				+ "SELECT * FROM tab WITH(NOLOCK) AND　pk_area　not in " + NWUtils.buildConditionString(pk_areas);
		List<AreaVO> result = NWDao.getInstance().queryForList(sql, AreaVO.class);
		return result;
	}
	
	/**
	 * 返回父集区域
	 * 
	 * @return
	 */
	public static List<AreaVO> getParentAreaVOs(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return new ArrayList<AreaVO>();
		}
		return getParentAreaVOs(listToArr(pk_areas));
	}

	
	/**
	 * 返回当前区域和子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static String getCurrentAreasWithChildren(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithChildren(pk_area);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			sb.append("'");
			sb.append(areaVO.getPk_area());
			sb.append("',");
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回当前区域和子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static String getCurrentAreasWithChildren(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithChildren(pk_areas);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			sb.append("'");
			sb.append(areaVO.getPk_area());
			sb.append("',");
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回当前区域和子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static String getCurrentAreasWithChildren(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return "('')";
		}
		return getCurrentAreasWithChildren(listToArr(pk_areas));
	}
	
	/**
	 * 返回子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static String getChildrenAreas(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithChildren(pk_area);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			if(!areaVO.getPk_area().equals(pk_area)){
				sb.append("'");
				sb.append(areaVO.getPk_area());
				sb.append("',");
			}
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static String getChildrenAreas(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithChildren(pk_areas);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			for(String pk_area : pk_areas){
				if(!areaVO.getPk_area().equals(pk_area)){
					sb.append("'");
					sb.append(areaVO.getPk_area());
					sb.append("',");
				}
			}
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回子集区域
	 * 返回值可能很多，慎用
	 * @return
	 */
	public static String getChildrenAreas(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return "('')";
		}
		return getChildrenAreas(listToArr(pk_areas));
	}

	
	/**
	 * 返回当前区域和父集区域
	 * 
	 * @return
	 */
	public static String getCurrentAreaWithParents(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithParents(pk_area);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			sb.append("'");
			sb.append(areaVO.getPk_area());
			sb.append("',");
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回当前区域和父集区域
	 * 
	 * @return
	 */
	public static String getCurrentAreaWithParents(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithParents(pk_areas);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			sb.append("'");
			sb.append(areaVO.getPk_area());
			sb.append("',");
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回当前区域和父集区域
	 * 
	 * @return
	 */
	public static String getCurrentAreaWithParents(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return "('')";
		}
		return getCurrentAreaWithParents(listToArr(pk_areas));
	}
	
	/**
	 * 返回父集区域
	 * 
	 * @return
	 */
	public static String getParentAreas(String pk_area) {
		if(StringUtils.isBlank(pk_area)){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithParents(pk_area);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			if(!areaVO.getPk_area().equals(pk_area)){
				sb.append("'");
				sb.append(areaVO.getPk_area());
				sb.append("',");
			}
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回父集区域
	 * 
	 * @return
	 */
	public static String getParentAreas(String[] pk_areas) {
		if(pk_areas == null || pk_areas.length == 0){
			return "('')";
		}
		List<AreaVO> resultList = getCurrentAreaVOWithParents(pk_areas);
		StringBuffer sb = new StringBuffer();
		for(AreaVO areaVO : resultList) {
			for(String pk_area : pk_areas){
				if(!areaVO.getPk_area().equals(pk_area)){
					sb.append("'");
					sb.append(areaVO.getPk_area());
					sb.append("',");
				}
			}
		}
		String areaCond = null;
		if(sb.length() > 0) {
			areaCond = sb.substring(0, sb.length() - 1);
		}
		return "(" + areaCond + ")";
	}
	
	/**
	 * 返回父集区域
	 * 
	 * @return
	 */
	public static String getParentAreas(List<String> pk_areas) {
		if(pk_areas == null || pk_areas.size() == 0){
			return "('')";
		}
		return getParentAreas(listToArr(pk_areas));
	}
	
	private static String[] listToArr(List<String> list){
		if(list == null || list.size() == 0){
			return null;
		}
		return list.toArray(new String[list.size()]);
	}
	
}
