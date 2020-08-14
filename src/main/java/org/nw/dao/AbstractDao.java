package org.nw.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;

import org.nw.constants.Constants;
import org.nw.dao.mapper.NWRowMapper;
import org.nw.dao.sqltranslator.SqlTranslator;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * dao抽象基类
 * 
 * @author fangw
 * @date 2011-4-9
 */
public abstract class AbstractDao extends JdbcDaoSupport {
	public static DB_TYPE DATABASE_TYPE = null;
	StringBuilder sb = new StringBuilder();

	/**
	 * 数据库类型的枚举定义
	 * 
	 * @author fangw
	 */
	public enum DB_TYPE {
		DB2(0), ORACLE(1), SQLSERVER(2), SYBASE(3), INFORMIX(4), HSQL(5), OSCAR(6), MYSQL(7), UNKOWN(-1);
		private Integer value;

		private DB_TYPE(Integer value) {
			this.value = value;
		}

		public boolean equals(Integer value) {
			return this.value.compareTo(value) == 0;
		}

			public String toString() {
			return this.value.toString();
		}
	}

	protected String jndiName;

	public AbstractDao(DataSource ds) {
		super();
		this.setDataSource(ds);

		// 获取数据库类型
		this.getDatabaseType();
	}

	public AbstractDao(String jndiName) {
		super();
		this.jndiName = jndiName;

		// 数据源查找
		if(jndiName != null) {
			try {
				InitialContext ctx = new InitialContext();
				DataSource ds = null;

				/**
				 * 不同应用服务器的不同引用方式下，jndi查找的方式也不太一样，下面将进行3次查找，3次中必须有一次找到
				 */
				if(ds == null) {
					try {
						// java:comp/env/jdbc/ncdb
						ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + jndiName);
					} catch(NameNotFoundException e) {
						// e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(ds == null) {
					try {
						ds = (DataSource) ctx.lookup("java:comp/env/" + jndiName);
					} catch(NameNotFoundException e) {
						// e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(ds == null) {
					try {
						ds = (DataSource) ctx.lookup(jndiName);
					} catch(NameNotFoundException e) {
						// e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}

				if(ds == null) {
					throw new RuntimeException("没有找到数据源:" + jndiName);
				}
				this.setDataSource(ds);
			} catch(Exception e) {
				logger.error("没有找到数据源！", e);
				throw new RuntimeException(e);
			}
		}

		// 获取数据库类型
		this.getDatabaseType();
	}

	public void setDatabaseType(DB_TYPE databaseType) {
		DATABASE_TYPE = databaseType;
	}

	/**
	 * 获取数据库类型
	 * 
	 * @return
	 */
	public Integer getDatabaseType() {
		if(DATABASE_TYPE == null) {
			// 根据connect的信息，自动判断
			Connection conn = null;
			try {
				conn = this.getConnection();
				if(conn == null) {
					throw new RuntimeException("获取数据库连接出错！");
				}

				DatabaseMetaData metaData = conn.getMetaData();
				String productName = metaData.getDatabaseProductName();
				logger.info("DatabaseProductName=" + productName);

				if(productName.startsWith("DB2")) {
					DATABASE_TYPE = DB_TYPE.DB2;
				} else if(productName.startsWith("Oracle")) {
					DATABASE_TYPE = DB_TYPE.ORACLE;
				} else if(productName.startsWith("Microsoft SQL Server")) {
					DATABASE_TYPE = DB_TYPE.SQLSERVER;
				} else if(productName.startsWith("MySQL")) {
					DATABASE_TYPE = DB_TYPE.MYSQL;
				} else {
					logger.warn("不支持的数据库类型：" + DATABASE_TYPE);
					DATABASE_TYPE = DB_TYPE.UNKOWN;
				}
			} catch(Exception e) {
				logger.error("获取数据库类型出错！", e);
				e.printStackTrace();
			} finally {
				if(conn != null) {
					try {
						conn.close();
					} catch(SQLException e) {
						logger.error("数据库连接关闭出错！", e);
					}
				}
			}
		}
		return DATABASE_TYPE.value;
	}

	/**
	 * 当前数据库类型
	 * 
	 * @return
	 */
	public static Integer getCurrentDBType() {
		return DATABASE_TYPE.value;
	}

	public SqlTranslator getSqlTranslator() {
		return new SqlTranslator(this.getDatabaseType());
	}

	public String getTransSql(String sql) {
		try {
			return this.getSqlTranslator().getSql(sql);
		} catch(SQLException e) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("SQL转换处理异常，本操作失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("SQL conversion processing exception, this operation failed!", e);
			}
			throw new RuntimeException("SQL转换处理异常，本操作失败！", e);
		}
	}

	public String[] getTransSql(String[] sqls) {
		try {
			if(sqls != null) {
				for(int i = 0; i < sqls.length; i++) {
					sqls[i] = this.getSqlTranslator().getSql(sqls[i]);
				}
				return sqls;
			}
			return null;
		} catch(SQLException e) {
			throw new RuntimeException("SQL转换处理异常，本操作失败！", e);
		}
	}

	public List<String> getTransSql(List<String> sqlList) {
		try {
			if(sqlList != null) {
				List<String> list = new ArrayList<String>();
				for(String sql : sqlList) {
					list.add(this.getSqlTranslator().getSql(sql));
				}
				return list;
			}
			return null;
		} catch(SQLException e) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("SQL转换处理异常，本操作失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("SQL conversion processing exception, this operation failed!", e);
			}
			throw new RuntimeException("SQL转换处理异常，本操作失败！", e);
		}
	}

	/**
	 * 把数据库字段名，转换为bean属性名
	 * 
	 * @param colName
	 * @return
	 */
	public String convertObjectName(String colName) {
		colName = colName.toLowerCase();
		StringBuffer sb = new StringBuffer();
		boolean flag = false;
		for(int i = 0; i < colName.length(); i++) {
			char c = colName.charAt(i);
			if("_".charAt(0) == c) {
				flag = true;
				continue;
			} else {
				if(flag || i == 0) {
					// 如果上一个是"_",或者是第一个字母,那么转成大写
					if(c >= 97 && c <= 122) {
						c = (char) (c - 32);
					}
				}
				sb.append(c);
				flag = false;
			}
		}
		return sb.toString();
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public void log(Object... args) {
		if(logger.isInfoEnabled()) {
			if(args != null && args.length > 0) {
				sb.setLength(0);
				for(Object obj : args) {
					if(sb.length() > 0) {
						sb.append(",");
					}
					sb.append(obj);
				}
				logger.info(sb);
			}
		}
	}

	public void log(String sql, Object... args) {
		if(logger.isInfoEnabled()) {
			logger.info(sql);
			if(args != null && args.length > 0) {
				sb.setLength(0);
				for(Object obj : args) {
					if(sb.length() > 0) {
						sb.append(",");
					}
					sb.append(obj);
				}
				if(sb.length() > 0) {
					logger.info(sb);
				}
			}
		}
	}

	/**
	 * 根据sql查询，返回具体的VO
	 * 
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param clazz
	 * @return
	 */
	public <T> T queryForObject(final String sql, final Class<T> clazz, final Object... args) {
		long startTs = System.currentTimeMillis();
		String transSql = this.getTransSql(sql);

		this.log(transSql, args);

		T retObj = null;
		try {
			if(clazz.getName().startsWith("java.lang")) {
				// 对于此类对象，不能用UftRowMapper
				retObj = this.getJdbcTemplate().queryForObject(transSql, clazz, args);
			} else {
				retObj = this.getJdbcTemplate().queryForObject(transSql, new NWRowMapper<T>(clazz), args);
			}
		} catch(IncorrectResultSizeDataAccessException e) {
			// spring在查询不到数据时直接抛出了异常，这里只做警告，不再网上抛
			logger.warn("结果集大小不正确，无法转换为" + clazz.getName() + "对象，有些逻辑可能根据该返回值进行判断，所以有时候可以忽略该警告.");
			return null;
		}
		long time = System.currentTimeMillis() - startTs;
		if(time > Constants.printSqlTime) {
			logger.warn("执行时间：" + time + ",sql：" + sql);
		} else {
			// logger.info(sql);
		}
		return retObj;
	}

	/**
	 * 根据sql查询，返回List
	 * 
	 * @param sql
	 * @param args
	 * @param clazz
	 * @return
	 */
	public <T> List<T> queryForList(final String sql, final Class<T> clazz, final Object... args) {
		long startTs = System.currentTimeMillis();
		String transSql = this.getTransSql(sql);

		this.log(transSql, args);

		List<T> retList = null;
		if(clazz.getName().startsWith("java.lang")) {
			// 对于此类对象，不能用UftRowMapper
			retList = this.getJdbcTemplate().queryForList(transSql, clazz, args);
		} else {
			retList = this.getJdbcTemplate().query(transSql, new NWRowMapper<T>(clazz), args);
		}

		long time = System.currentTimeMillis() - startTs;
		if(time > Constants.printSqlTime) {
			logger.warn("执行时间：" + time + ",sql：" + sql);
		} else {
			// logger.info(sql);
		}
		return retList;
	}

	/**
	 * 拼装cache的key
	 */
	protected String getCacheKey(Object... args) {
		StringBuilder sb = new StringBuilder();// "K"
		/**
		 * 这里一定要用当前的jndiName做区分，因为可能指定某个jndiName进行数据库操作，而不是当前帐套对应的数据源
		 */
		sb.append(this.getJndiName() == null ? "JDBC" : this.getJndiName());
		for(Object arg : args) {
			if(arg != null) {
				if(arg instanceof Object[]) {
					for(Object o : (Object[]) arg) {
						if(o instanceof Object[]) {
							sb.append(getCacheKey(o));// 递归获取key
						} else {
							sb.append("_");
							sb.append(o);
						}
					}
				} else {
					sb.append("_");
					sb.append(arg);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 返回最后的order by 子句
	 * 
	 * @param sql
	 * @return
	 */
	public String getOrderBy(String sql) {
		if(sql.toLowerCase().lastIndexOf(" order ") != -1) {
			return sql.substring(sql.toLowerCase().lastIndexOf(" order "));
		}
		return null;
	}

	/**
	 * 根据sql分页查询，并包装为相应vo
	 * 
	 * @param voClass
	 * @param sql
	 * @param offset
	 * @param pageSize
	 * @param args
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public PaginationVO queryBySqlWithPaging(final String sql, final Class<?> voClass, final int offset,
			final int pageSize, final Object... args) {
		long startTs = System.currentTimeMillis();
		String transSql = this.getTransSql(sql);

		// sqlserver不加个别名，会出错
		String orderBy = null;
		if(DB_TYPE.SQLSERVER.equals(this.getDatabaseType())) {
			orderBy = getOrderBy(transSql);
			if(transSql.toLowerCase().lastIndexOf(" order ") != -1) {
				transSql = transSql.substring(0, transSql.toLowerCase().lastIndexOf(" order "));
			}
		}
		String countSql = "SELECT count(1) AS c FROM (" + transSql + ") countSql_";
		this.log(countSql, args);

		long count = this.getJdbcTemplate().queryForLong(countSql, args);

		PaginationVO paginationVO = new PaginationVO((int) count, offset, pageSize);

		if(count == 0) {
			// 如果没有数据，则不进行查询(但这里为了避免业务处理麻烦，空数据也返回了一个list，而不是null)
			paginationVO.setItems(new ArrayList());
			return paginationVO;
		}

		StringBuilder pagingSql = new StringBuilder();

		if(offset == -1 || pageSize == -1) {
			// 如果为DEFAULT_OFFSET_WITH_NOPAGING时，则不分页，而是查询所有数据
			pagingSql.append(transSql);
			if(orderBy != null) {
				pagingSql.append(orderBy);
			}
		} else {
			// 产生分页查询的sql
			if(DB_TYPE.DB2.equals(this.getDatabaseType())) {
				pagingSql.append("SELECT * FROM (SELECT A__.*,rownumber() over() AS ROWID__ FROM (");
				pagingSql.append(transSql);
				pagingSql.append(") AS A__) AS B__ WHERE B__.ROWID__ BETWEEN ");
				pagingSql.append(paginationVO.getStartOffset());
				pagingSql.append(" AND ");
				pagingSql.append(paginationVO.getEndOffset());
			} else if(DB_TYPE.ORACLE.equals(this.getDatabaseType())) {
				pagingSql.append("SELECT * FROM (SELECT A__.*,ROWNUM AS ROWID__ FROM (");
				pagingSql.append(transSql);
				pagingSql.append(") A__) B__ WHERE B__.ROWID__ BETWEEN ");
				pagingSql.append(paginationVO.getStartOffset());
				pagingSql.append(" AND ");
				pagingSql.append(paginationVO.getEndOffset());
			} else if(DB_TYPE.SQLSERVER.equals(this.getDatabaseType())) {
				pagingSql.append("SELECT * FROM (SELECT A__.*,row_number() over (");
				if(orderBy != null && orderBy.length() > 0) {
					pagingSql.append(orderBy);
				} else {
					pagingSql.append("order by getdate()");
				}
				pagingSql.append(") AS ROWID__ FROM (");
				pagingSql.append(transSql);
				pagingSql.append(") A__) B__ WHERE B__.ROWID__ BETWEEN ");
				pagingSql.append(paginationVO.getStartOffset());
				pagingSql.append(" AND ");
				pagingSql.append(paginationVO.getEndOffset());
			} else if(DB_TYPE.MYSQL.equals(this.getDatabaseType())) {
				pagingSql.append("SELECT * FROM (");
				pagingSql.append(transSql);
				pagingSql.append(") as A__ LIMIT ");
				pagingSql.append(paginationVO.getStartOffset() - 1); // mysql的起始记录比其他的少1
				pagingSql.append(",");
				pagingSql.append(paginationVO.getPageSize());
			} else {
				if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
					throw new RuntimeException("尚未支持此数据库的分页,数据库类型为:" + this.getDatabaseType());
				}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
					throw new RuntimeException("This database is not supported for paging:"+ this.getDatabaseType());
				}
				throw new RuntimeException("尚未支持此数据库的分页,数据库类型为:" + this.getDatabaseType());
			}
		}

		List<?> list = null;
		if(voClass == null) {
			list = this.queryForList(pagingSql.toString(), args);
		} else {
			list = this.queryForList(pagingSql.toString(), voClass, args);
		}
		paginationVO.setItems(list);

		long time = System.currentTimeMillis() - startTs;
		if(time > Constants.printSqlTime) {
			logger.warn("执行时间：" + time + ",sql：" + sql);
		} else {
			// logger.info(sql);
		}
		return paginationVO;
	}

	/**
	 * 根据sql分页查询
	 * 
	 * @param sql
	 * @param offset
	 * @param pageSize
	 * @param args
	 * @return
	 */
	public PaginationVO queryBySqlWithPaging(final String sql, final int offset, final int pageSize,
			final Object... args) {
		return this.queryBySqlWithPaging(sql, null, offset, pageSize, args);
	}

	/**
	 * 根据sql，返回list 2011-4-15 注，如果结果集为空，不抛出EmptyResultDataAccessException异常
	 */
	public List<Map<String, Object>> queryForList(final String sql, final Object... args) {
		long startTs = System.currentTimeMillis();
		String transSql = this.getTransSql(sql);

		this.log(transSql, args);

		List<Map<String, Object>> list = null;
		try {
			list = this.getJdbcTemplate().queryForList(transSql, args);
		} catch(EmptyResultDataAccessException e) {
		}

		long time = System.currentTimeMillis() - startTs;
		if(time > Constants.printSqlTime) {
			logger.warn("执行时间：" + time + ",sql：" + sql);
		} else {
			// logger.info(sql);
		}
		return list;
	}

}