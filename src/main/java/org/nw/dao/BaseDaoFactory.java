package org.nw.dao;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

/**
 * NC VO的数据库操作类<br>
 * 本类继承自spring的JdbcDaoSupport，可以使用spring的jdbcTemplate所有强大功能<br>
 * 本类已经final，不允许再继承
 * 
 * @author fangw
 */
public class BaseDaoFactory {
	private static ConcurrentHashMap<Object, BaseDao> cacheMap = new ConcurrentHashMap<Object, BaseDao>();

	/**
	 * 根据datasource获取dao实例(实例未缓存)
	 * 
	 * @param ds
	 * @return
	 */
	public static BaseDao getInstance(DataSource ds) {
		if(ds != null && cacheMap.get(ds) != null) {
			return cacheMap.get(ds);
		} else {
			BaseDao dao = new BaseDao(ds);

			if(ds != null) {
				cacheMap.put(ds, dao);
			}
			return dao;
		}
	}

	/**
	 * 根据JNDI名称，获取dao实例(对实例使用了缓存)
	 * 
	 * @param jndiName
	 * @return
	 */
	public static BaseDao getInstance(String jndiName) {
		if(jndiName != null && cacheMap.get(jndiName) != null) {
			return cacheMap.get(jndiName);
		} else {
			BaseDao dao = new BaseDao(jndiName);

			if(jndiName != null) {
				cacheMap.put(jndiName, dao);
			}
			return dao;
		}
	}

}
