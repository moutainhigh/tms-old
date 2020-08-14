package org.nw.formula;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.nw.dao.BaseDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.vo.pub.SuperVO;

public class FormulaDao extends BaseDao {
	private ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

	public FormulaDao(DataSource ds) {
		super(ds);
	}

	private String constructSql(String sql) {
		if(sql == null) {
			return null;
		}
		String newSql = sql.toLowerCase();
		if(newSql.indexOf("order by") == -1) {// 如果已经包括了order by，不再增加条件了
			if(sql.indexOf("where") == -1) {
				sql += " where isnull(dr,0)=0";
			} else {
				sql += " and isnull(dr,0)=0";
			}
		}
		return sql;
	}

	public <T> List<T> queryForListWithCache(String sql, Class<T> clazz, Object... args) {
		// 这里cachekey要与clearCacheForQueryForListWithCache中一致，否则无法正常清除缓存
		sql = constructSql(sql);
		String cacheKey = this.getCacheKey(
				"com.uft.webnc.formula.FormulaDao.queryForListWithCache(String, Class<T>, Object...)", sql,
				clazz.getName(), args);
		Object cacheValue = cache.get(cacheKey);
		if(cacheValue != null) {
			this.logger.debug("缓存命中,sql=" + sql + ",class=" + clazz.getName());
			this.log(-1, sql, args);
			return (List<T>) cacheValue;
		}

		String transSql = this.getTransSql(sql);
		List<T> list = this.queryForList(transSql, clazz, args);

		if(list != null) {
			cache.put(cacheKey, list);
		}
		return list;
	}

	public List<Map<String, Object>> queryForListWithCache(String sql, Object... args) {
		sql = constructSql(sql);
		String cacheKey = this.getCacheKey("com.uft.webnc.formula.FormulaDao.queryForListWithCache(String, Object...)",
				sql, args);
		Object cacheValue = cache.get(cacheKey);
		if(cacheValue != null) {
			this.logger.debug("缓存命中,sql=" + sql);
			this.log(-1, sql, args);
			return (List<Map<String, Object>>) cacheValue;
		}

		String transSql = this.getTransSql(sql);
		List<Map<String, Object>> list = this.getJdbcTemplate().queryForList(transSql, args);
		if(list != null) {
			cache.put(cacheKey, list);
		}
		return list;
	}

	public <T extends SuperVO> T queryByConditionWithCache(Class<? extends SuperVO> clazz, String strWhere,
			Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T) this.queryByWhereClauseWithCache(clazz, condition, args);
	}

	public <T> T queryForObjectWithCache(String sql, Class<T> clazz, Object... args) {
		sql = constructSql(sql);
		String cacheKey = this.getCacheKey(
				"com.uft.webnc.formula.FormulaDao.queryForObjectWithCache(String, Class<T>, Object...)", sql,
				clazz.getName(), args);
		Object cacheValue = cache.get(cacheKey);
		if(cacheValue != null) {
			this.logger.debug("缓存命中,sql=" + sql + ",class=" + clazz.getName());
			this.log(-1, sql, args);
			return (T) cacheValue;
		}

		String transSql = this.getTransSql(sql);
		Object obj = this.queryForObject(transSql, clazz, args);
		if(obj != null) {
			cache.put(cacheKey, obj);
		}
		return (T) obj;
	}

	public <T extends SuperVO> T queryByWhereClauseWithCache(Class<? extends SuperVO> clazz, String condition,
			Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		return (T) this.queryForObjectWithCache(sql, clazz, args);
	}

	public <T extends SuperVO> T[] queryForSuperVOArrayByConditionWithCache(Class<? extends SuperVO> clazz,
			String strWhere, Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T[]) this.queryForSuperVOArrayByWhereClauseWithCache(clazz, condition, args);
	}

	public <T extends SuperVO> T[] queryForSuperVOArrayByWhereClauseWithCache(Class<? extends SuperVO> clazz,
			String condition, Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		sql = constructSql(sql);
		String cacheKey = this
				.getCacheKey(
						"com.uft.webnc.formula.FormulaDao.queryForSuperVOArrayByWhereClauseWithCache(Class<? extends SuperVO>, String, Object...)",
						sql, clazz.getName(), args);

		Object cacheValue = cache.get(cacheKey);
		if(cacheValue != null) {
			this.logger.debug("缓存命中,sql=" + sql + ",class=" + clazz.getName());
			this.log(-1, sql, args);
			return (T[]) cacheValue;
		}

		List<? extends SuperVO> list = this.queryForList(sql, clazz, args);
		if(list == null) {
			return null;
		}

		T[] voArr = list.toArray((T[]) Array.newInstance(clazz, list.size()));
		if(voArr != null) {
			cache.put(cacheKey, voArr);
		}
		return voArr;
	}

}
