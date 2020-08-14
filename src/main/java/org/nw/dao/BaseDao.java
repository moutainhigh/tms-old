package org.nw.dao;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.Assert;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.helper.DaoHelper;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.SuperVO;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.uft.webnc.cache.proxy.CacheManagerProxy;

public class BaseDao extends AbstractDao {

	/**
	 * 根据datasource获取dao实例(实例未缓存)
	 * 
	 * @param ds
	 * @return
	 */
	public BaseDao(DataSource ds) {
		super(ds);
	}

	/**
	 * 根据JNDI名称
	 * 
	 * @param jndiName
	 * @return
	 */
	public BaseDao(String jndiName) {
		super(jndiName);
	}

	/**
	 * 返回in查询条件
	 * 
	 * @param field
	 * @param valueArr
	 * @return
	 */
	public static String getInClause(String field, String[] valueArr) {
		Assert.notNull(field);
		if(valueArr == null || valueArr.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < valueArr.length; i++) {
			if(valueArr[i] == null) {
				// 忽略null的值
				continue;
			}
			if(i != 0) {
				sb.append(',');
			}
			sb.append("'" + valueArr[i] + "'");
		}
		return field + " in (" + sb.toString() + ")";
	}

	/**
	 * 设置UUID主键<br>
	 * 2011-4-9
	 */
	public static void setUuidPrimaryKey(AggregatedValueObject billVO) {
		DaoHelper.setUuidPrimaryKey(billVO);
	}

	/**
	 * 设置UUID主键<br>
	 * 2011-4-9
	 */
	public static void setUuidPrimaryKey(SuperVO superVO) {
		DaoHelper.setUuidPrimaryKey(superVO);
	}

	/**
	 * 设置NC主键<br>
	 * 2011-4-9
	 */
	public static void setNCPrimaryKey(AggregatedValueObject billVO) {
		DaoHelper.setNWPrimaryKey(billVO);
	}

	/**
	 * 设置NC主键<br>
	 * 2011-4-9
	 */
	public static void setNCPrimaryKey(SuperVO superVO) {
		DaoHelper.setNWPrimaryKey(superVO);
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param billVO
	 */
	public int[] delete(AggregatedValueObject billVO) {
		return this.delete(billVO, true);
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param billVO
	 * @param isLogicalDelete
	 */
	public int[] delete(AggregatedValueObject billVO, boolean isLogicalDelete) {
		List<String> sqlList = DaoHelper.getDeleteSQL(billVO, isLogicalDelete);
		return this.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param superVO
	 */
	public int delete(SuperVO superVO) {
		return this.delete(superVO, true);
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param superVO
	 * @param isLogicalDelete
	 */
	public int delete(SuperVO superVO, boolean isLogicalDelete) {
		String sql = DaoHelper.getDeleteSQL(superVO, isLogicalDelete);
		return this.update(sql);
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param superVO
	 */
	public int[] delete(SuperVO[] superVOs) {
		return this.delete(superVOs, true);
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param superVO
	 * @param isLogicalDelete
	 */
	public int[] delete(SuperVO[] superVOs, boolean isLogicalDelete) {
		List<String> sqlList = DaoHelper.getDeleteSQL(superVOs, isLogicalDelete);
		return this.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param superVO
	 */
	public int[] delete(List<SuperVO> superVOList) {
		return this.delete(superVOList, true);
	}

	/**
	 * 与saveOrUpdate的status为delete不同，这里的vo的status可能不为delete，但同样进行删除
	 * 
	 * @param superVO
	 * @param isLogicalDelete
	 */
	public int[] delete(List<SuperVO> superVOList, boolean isLogicalDelete) {
		List<String> sqlList = DaoHelper.getDeleteSQL(superVOList, isLogicalDelete);
		return this.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
	}

	/**
	 * 根据pk删除数据(注意，如果没有DR字段，则直接物理删除，如果有dr，则用逻辑删除)
	 * 
	 * @param clazz
	 * @param pk
	 * @return
	 */
	public int deleteByPK(Class<? extends SuperVO> clazz, String pk) {
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		if(ReflectionUtils.getDeclaredField(superVO, "dr") == null) {
			// 物理删除
			return this.deleteByPK(clazz, pk, false);
		} else {
			// 逻辑删除
			return this.deleteByPK(clazz, pk, true);
		}
	}

	public int deleteByPK(Class<? extends SuperVO> clazz, String pk, boolean isLogicalDelete) {
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		StringBuilder sql = new StringBuilder();

		if(!isLogicalDelete) {
			// 物理删除
			sql.append("DELETE FROM ").append(superVO.getTableName()).append(" WHERE ")
					.append(superVO.getPKFieldName()).append("=?");
		} else {
			// 逻辑删除
			sql.append("UPDATE ").append(superVO.getTableName()).append(" set DR=1 WHERE ")
					.append(superVO.getPKFieldName()).append("=?");
		}

		return this.update(sql.toString(), pk);
	}

	/**
	 * 根据pk批量删除数据(注意，如果没有DR字段，则直接物理删除，如果有dr，则用逻辑删除)
	 * 
	 * @param clazz
	 * @param pks
	 * @return
	 */
	public int[] deleteByPKs(Class<? extends SuperVO> clazz, final String[] pks) {
		if(pks == null || pks.length == 0) {
			return null;
		}

		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		if(ReflectionUtils.getDeclaredField(superVO, "dr") == null) {
			// 物理删除
			return this.deleteByPKs(clazz, pks, false);
		} else {
			// 逻辑删除
			return this.deleteByPKs(clazz, pks, true);
		}
	}

	public int[] deleteByPKs(Class<? extends SuperVO> clazz, final String[] pks, boolean isLogicalDelete) {
		if(pks == null || pks.length == 0) {
			return null;
		}

		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		StringBuilder sql = new StringBuilder();

		if(!isLogicalDelete) {
			// 物理删除
			sql.append("DELETE FROM ").append(superVO.getTableName()).append(" WHERE ")
					.append(superVO.getPKFieldName()).append("=?");
		} else {
			// 逻辑删除
			sql.append("UPDATE ").append(superVO.getTableName()).append(" set DR=1 WHERE ")
					.append(superVO.getPKFieldName()).append("=?");
		}

		BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setObject(1, pks[i]);
			}

			public int getBatchSize() {
				return pks.length;
			}
		};

		return this.batchUpdate(sql.toString(), pss);
	}

	/**
	 * 根据条件删除数据(注意，直接物理删除，如果有dr，则用逻辑删除)
	 * 
	 * @param clazz
	 * @param wherestr
	 * @param args
	 * @return
	 */
	public int deleteByClause(Class<? extends SuperVO> clazz, String wherestr, final Object... args) {
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		if(ReflectionUtils.getDeclaredField(superVO, "dr") == null) {
			// 物理删除
			return this.deleteByClause(clazz, wherestr, false, args);
		} else {
			// 逻辑删除
			return this.deleteByClause(clazz, wherestr, true, args);
		}
	}

	/**
	 * 根据条件删除数据(注意，直接物理删除，如果有dr，则用逻辑删除)
	 * 
	 * @param clazz
	 * @param wherestr
	 * @param args
	 * @return
	 */
	public int deleteByClause(Class<? extends SuperVO> clazz, String wherestr, boolean isLogicalDelete,
			final Object... args) {
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		StringBuilder sql = new StringBuilder();

		if(!isLogicalDelete) {
			// 物理删除
			sql.append("DELETE FROM ").append(superVO.getTableName());
		} else {
			// 逻辑删除
			sql.append("UPDATE ").append(superVO.getTableName()).append(" set DR=1");
		}

		if(wherestr != null) {
			wherestr = wherestr.trim();
			if(wherestr.length() > 0) {
				if(wherestr.toUpperCase().startsWith("WHERE")) {
					wherestr = wherestr.substring(5);
				}
				if(wherestr.length() > 0) {
					sql.append(" WHERE ").append(wherestr);
					return this.update(sql.toString(), args);
				}
			}
		}

		return 0;
	}

	/**
	 * 判断单据是否是新增（pk是否为空）
	 * 
	 * @param billVO
	 * @return
	 */
	public boolean isNew(AggregatedValueObject billVO) {
		try {
			return billVO.getParentVO().getPrimaryKey() == null || billVO.getParentVO().getPrimaryKey().length() == 0;
		} catch(BusinessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据PK查询某个vo，这里不做dr的判断，就算为1也查
	 * 
	 * @param clazz
	 * @param pk
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByPK(final Class<? extends SuperVO> clazz, final String pk) {
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			logger.error("创建SuperVO实例失败！", e);
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("创建SuperVO实例失败！", e);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Failed to create a SuperVO instance!", e);
			}
			throw new RuntimeException("创建SuperVO实例失败！", e);
		}

		String where = superVO.getPKFieldName() + "=?";

		// String condition = DaoHelper.getWhereClause(clazz, where);
		return (T) this.queryByWhereClause(clazz, where, pk);
	}

	/**
	 * 查询superVO<br>
	 * 注：如果有dr字段，会自动增加dr=0的处理，如果不需要这个处理，可以调用queryByWhereClause方法<br>
	 * 2011-4-9
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByCondition(final Class<? extends SuperVO> clazz, final String strWhere,
			final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T) this.queryByWhereClause(clazz, condition, args);
	}

	/**
	 * 查询superVO<br>
	 * 注：如果有dr字段，会自动增加dr=0的处理，如果不需要这个处理，可以调用queryByWhereClause方法<br>
	 * 
	 * @param <T>
	 * @param clazz
	 *            返回类
	 * @param fields
	 *            查询的字段值
	 * @param strWhere
	 *            查询条件
	 * @param args
	 *            查询参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByCondition(final Class<? extends SuperVO> clazz, String[] fieldNames,
			final String strWhere, final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T) this.queryByWhereClause(clazz, fieldNames, condition, args);
	}

	/**
	 * 模仿NC，根据条件查询superVO列表-带分页
	 * 
	 * @param clazz
	 * @param strWhere
	 * @param offset
	 * @param pageSize
	 * @param args
	 * @return
	 */
	public PaginationVO queryByConditionWithPaging(final Class<? extends SuperVO> clazz, final int offset,
			final int pageSize, final String strWhere, final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return this.queryByWhereClauseWithPaging(clazz, offset, pageSize, condition, args);
	}

	/**
	 * 模仿NC，根据条件查询superVO列表-带分页 分页方式使用pk定位分页。
	 * 
	 * @param clazz
	 * @param pkField
	 *            主键pk
	 * @param pks
	 *            查询分页的主键pk集合，
	 * @param pageSize
	 * @param strWhere
	 *            查询条件
	 * @param args
	 * @return
	 */
	public PaginationVO queryByConditionWithPaging(final Class<? extends SuperVO> clazz, String pkField,
			final String[] pks, final int pageSize, final String strWhere, final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return this.queryByWhereClauseWithPaging(clazz, pkField, pks, pageSize, condition, args);
	}

	/**
	 * 查询superVO<br>
	 * 2011-4-9
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByWhereClause(final Class<? extends SuperVO> clazz, final String condition,
			final Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		return (T) this.queryForObject(sql, clazz, args);
	}

	/**
	 * 查询superVO<br>
	 * 2011-4-9
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByWhereClause(final Class<? extends SuperVO> clazz, String[] fieldNames,
			final String condition, final Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, fieldNames);
		return (T) this.queryForObject(sql, clazz, args);
	}

	public PaginationVO queryByWhereClauseWithPaging(final Class<? extends SuperVO> clazz, final int offset,
			final int pageSize, final String condition, final Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		return this.queryBySqlWithPaging(sql, clazz, offset, pageSize, args);
	}

	/**
	 * 使用pk定位方式进行分页查询
	 * 
	 * @param clazz
	 *            要查询的VO类
	 * @param condition
	 *            查询条件
	 * @param pageSize
	 *            每页记录数，当第一次查询时需要传入该参数，点击下一页就不再需要传入了。
	 * @param pkField
	 *            主键名称，用于拼接in 查询条件
	 * @param pks
	 *            主键集合，用于拼接in 查询条件，第一次查询不需要传入该参数，以后下一页都需要传入。
	 * @param args
	 *            参数值
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public PaginationVO queryByWhereClauseWithPaging(final Class<? extends SuperVO> clazz, String pkField,
			String[] pks, final int pageSize, final String condition, final Object... args) {
		String sql = null;
		PaginationVO paginationVO = new PaginationVO();
		if(pageSize == -1) {
			// 不进行分页
			sql = DaoHelper.buildSelectSql(clazz, condition, null);
		} else {
			// 分页查询
			if(pks == null || pks.length == 0) {
				sql = DaoHelper.buildSelectSql(clazz, condition, new String[] { pkField });
				List<String> _pks = queryForList(sql, String.class, args);
				if(_pks != null && _pks.size() > 0) {
					int pksize = pageSize;
					if(_pks.size() < pageSize) {
						pksize = _pks.size();
					}
					pks = _pks.subList(0, pksize).toArray(new String[pksize]);
					paginationVO.setPks(_pks);
				}
			}
			String inClause = getInClause(pkField, pks);
			String newCondition = inClause;
			if(StringUtils.isNotBlank(condition)) {
				if(StringUtils.isNotBlank(newCondition)) {
					newCondition += " and ";
				}
				newCondition += condition;
			}
			sql = DaoHelper.buildSelectSql(clazz, newCondition, null);
		}
		List vos = this.queryForList(sql, clazz, args);
		paginationVO.setItems(vos);
		return paginationVO;
	}

	/**
	 * 根据条件查询SuperVO数组（自动加dr=0的条件）
	 * 
	 * @param clazz
	 * @param strWhere
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T[] queryForSuperVOArrayByCondition(final Class<? extends SuperVO> clazz,
			final String strWhere, final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T[]) this.queryForSuperVOArrayByWhereClause(clazz, condition, args);
	}

	/**
	 * 根据条件查询SuperVO数组（不自动加dr=0的条件）
	 * 
	 * @param clazz
	 * @param condition
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T[] queryForSuperVOArrayByWhereClause(final Class<? extends SuperVO> clazz,
			final String condition, final Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		List<T> list = (List<T>) this.queryForList(sql, clazz, args);
		T[] voArr = list.toArray((T[]) Array.newInstance(clazz, list.size()));
		return voArr;
	}

	/**
	 * 保存聚合vo到数据库<br>
	 * 2011-4-9
	 */
	public void saveOrUpdate(AggregatedValueObject billVO) {
		this.saveOrUpdate(billVO, true);
	}

	/**
	 * 保存聚合vo到数据库<br>
	 * 2011-4-9
	 */
	public int[] saveOrUpdate(AggregatedValueObject billVO, boolean isLogicalDelete) {
		// 解析成insert或者update语句
		List<String> sqlList = DaoHelper.getUpdateSQL(this, billVO, isLogicalDelete);
		return this.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
	}

	/**
	 * 保存superVO到数据库<br>
	 * 
	 * @param superVO
	 */
	public void saveOrUpdate(SuperVO superVO) {
		this.saveOrUpdate(superVO, true);
	}

	/**
	 * 保存superVO到数据库<br>
	 * 2011-4-9
	 */
	public int saveOrUpdate(SuperVO superVO, boolean isLogicalDelete) {
		// 解析成insert或者update语句
		String sql = DaoHelper.getUpdateSQL(this, superVO, isLogicalDelete);
		return this.update(sql);
	}

	/**
	 * 批量保存<br>
	 * 2011-4-9
	 */
	public int[] saveOrUpdate(SuperVO[] superVOArray) {
		return this.saveOrUpdate(superVOArray, true);
	}

	/**
	 * 批量保存<br>
	 * 2011-4-9
	 */
	public int[] saveOrUpdate(SuperVO[] superVOArray, boolean isLogicalDelete) {
		// 解析成insert或者update语句
		List<String> sqlList = DaoHelper.getUpdateSQL(this, superVOArray, isLogicalDelete);
		return this.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
	}

	public int[] saveOrUpdate(List<SuperVO> superVOArray) {
		return saveOrUpdate(superVOArray,true);
	}
	
	public int[] saveOrUpdate(List<SuperVO> superVOArray,boolean isCkeck) {
		// 解析成insert或者update语句
		List<String> sqlList = DaoHelper.getUpdateSQL(this, superVOArray, true,isCkeck);
		return this.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
	}

	public int update(String sql) {
		// sql翻译
		String transSql = this.getTransSql(sql);
		// 执行sql
		try {
			logger.info(transSql);
			return getJdbcTemplate().update(transSql);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public int update(String sql, Object... args) {
		// sql翻译
		String transSql = this.getTransSql(sql);

		// 执行sql
		try {
			if(logger.isInfoEnabled()) {
				logger.info(transSql);
				if(args != null) {
					for(Object obj : args) {
						logger.info(obj);
					}
				}
			}
			return getJdbcTemplate().update(transSql, args);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public int[] batchUpdate(String[] sqls) {
		// sql翻译
		String[] transSqls = this.getTransSql(sqls);

		// 执行sql
		if(transSqls != null && transSqls.length > 0) {
			try {
				if(logger.isInfoEnabled()) {
					for(String sql : transSqls) {
						logger.info(sql);
					}
				}
				return getJdbcTemplate().batchUpdate(transSqls);
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) {
		// sql翻译
		String transSql = this.getTransSql(sql);

		// 执行sql
		try {
			if(logger.isInfoEnabled()) {
				logger.info(transSql);
				logger.info(pss);
			}
			return getJdbcTemplate().batchUpdate(transSql, pss);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据sql查询，返回List
	 * 
	 * @param sql
	 * @param args
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForListWithCache(final String sql, final Class<T> clazz, final Object... args) {
		String cacheKey = this.getCacheKey(
				"com.uft.webnc.dao.AbstractDao.queryForListWithCache(String, Class<T>, Object...)", sql,
				clazz.getName(), args);
//		// 判断缓存中是否有
//		Object cacheValue = CacheManagerProxy.getICache().get(cacheKey);
//		if(cacheValue != null) {
//			if(this.logger.isInfoEnabled()) {
//				this.logger.info("缓存命中,sql=" + sql + ",class=" + clazz.getName());
//				this.log(args);
//			}
//			return (List<T>) cacheValue;
//		}

		String transSql = this.getTransSql(sql);
		List<T> list = this.queryForList(transSql, clazz, args);

		if(list != null) {
			// 没有查到结果不缓存
			CacheManagerProxy.getICache().put(cacheKey, list);
		}
		return list;
	}

	/**
	 * 根据sql，返回list(带缓存的) 2011-4-15
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForListWithCache(final String sql, final Object... args) {
		String cacheKey = this.getCacheKey("com.uft.webnc.dao.AbstractDao.queryForListWithCache(String, Object...)",
				sql, args);
		// 判断缓存中是否有
		Object cacheValue = CacheManagerProxy.getICache().get(cacheKey);
		if(cacheValue != null) {
			if(this.logger.isInfoEnabled()) {
				this.logger.info("缓存命中,sql=" + sql);
				this.log(args);
			}
			return (List<Map<String, Object>>) cacheValue;
		}

		String transSql = this.getTransSql(sql);
		List<Map<String, Object>> list = this.getJdbcTemplate().queryForList(transSql, args);
		if(list != null) {
			// 没有查到结果不缓存
			CacheManagerProxy.getICache().put(cacheKey, list);
		}
		return list;
	}

	/**
	 * 查询superVO<br>
	 * 注：如果有dr字段，会自动增加dr=0的处理，如果不需要这个处理，可以调用queryByWhereClauseWithCache方法<br>
	 * 2011-4-9
	 */
	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByConditionWithCache(final Class<? extends SuperVO> clazz, final String strWhere,
			final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T) this.queryByWhereClauseWithCache(clazz, condition, args);
	}

	/**
	 * 根据sql查询，返回具体的VO
	 * 
	 * @param sql
	 * @param args
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryForObjectWithCache(final String sql, final Class<T> clazz, final Object... args) {
		String cacheKey = this.getCacheKey(
				"com.uft.webnc.dao.AbstractDao.queryForObjectWithCache(String, Class<T>, Object...)", sql,
				clazz.getName(), args);
		// 判断缓存中是否有
		Object cacheValue = CacheManagerProxy.getICache().get(cacheKey);
		if(cacheValue != null) {
			if(this.logger.isInfoEnabled()) {
				this.logger.info("缓存命中,sql=" + sql + ",class=" + clazz.getName());
				this.log(args);
			}
			return (T) cacheValue;
		}

		String transSql = this.getTransSql(sql);
		Object obj = this.queryForObject(transSql, clazz, args);
		if(obj != null) {
			// 没有查到结果不缓存
			CacheManagerProxy.getICache().put(cacheKey, obj);
		}
		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T queryByWhereClauseWithCache(final Class<? extends SuperVO> clazz,
			final String condition, final Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		return (T) this.queryForObjectWithCache(sql, clazz, args);
	}

	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T[] queryForSuperVOArrayByConditionWithCache(final Class<? extends SuperVO> clazz,
			final String strWhere, final Object... args) {
		String condition = DaoHelper.getWhereClause(clazz, strWhere);
		return (T[]) this.queryForSuperVOArrayByWhereClauseWithCache(clazz, condition, args);
	}

	@SuppressWarnings("unchecked")
	public <T extends SuperVO> T[] queryForSuperVOArrayByWhereClauseWithCache(final Class<? extends SuperVO> clazz,
			final String condition, final Object... args) {
		String sql = DaoHelper.buildSelectSql(clazz, condition, null);
		String cacheKey = this
				.getCacheKey(
						"com.uft.webnc.dao.NCDao.queryForSuperVOArrayByWhereClauseWithCache(Class<? extends SuperVO>, String, Object...)",
						sql, clazz.getName(), args);

		// 判断缓存中是否存在，如果存在直接返回
		Object cacheValue = CacheManagerProxy.getICache().get(cacheKey);
		if(cacheValue != null) {
			if(this.logger.isInfoEnabled()) {
				this.logger.info("缓存命中,sql=" + sql + ",class=" + clazz.getName());
				this.log(args);
			}
			return (T[]) cacheValue;
		}

		List<? extends SuperVO> list = this.queryForList(sql, clazz, args);
		if(list == null) {
			return null;
		}

		T[] voArr = list.toArray((T[]) Array.newInstance(clazz, list.size()));
		if(voArr != null) {
			// 没有查到结果不缓存
			CacheManagerProxy.getICache().put(cacheKey, voArr);
		}
		return voArr;
	}

	/**
	 * 取上一条记录pk
	 */
	public Object getPreviewPk(final String sql, final String fieldName, final Object fieldValue, final Object... args) {
		// 首先调用sql翻译器
		final String transedSql = getTransSql(sql);
		return getJdbcTemplate().execute(new ConnectionCallback<Object>() {
			public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
				PreparedStatement stmt = connection.prepareStatement(transedSql);
				for(int i = 0; args != null && i < args.length; i++) {
					stmt.setObject(i + 1, args[i]);
				}
				ResultSet rs = stmt.executeQuery();
				if(rs != null) {
					Object previewValue = null;
					while(rs.next()) {
						Object value = rs.getObject(fieldName);
						if(value.equals(fieldValue)) {
							// 定位到
							return previewValue;
						} else {
							previewValue = value;
						}
					}
				}
				return null;
			}
		});
	}

	/**
	 * 取下一条记录pk
	 */
	public Object getNextPk(final String sql, final String fieldName, final Object fieldValue, final Object... args) {
		// 首先调用sql翻译器
		final String transedSql = getTransSql(sql);

		return getJdbcTemplate().execute(new ConnectionCallback<Object>() {
			public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
				PreparedStatement stmt = connection.prepareStatement(transedSql);
				for(int i = 0; args != null && i < args.length; i++) {
					stmt.setObject(i + 1, args[i]);
				}
				ResultSet rs = stmt.executeQuery();
				if(rs != null) {
					while(rs.next()) {
						Object value = rs.getObject(fieldName);
						if(value.equals(fieldValue)) {
							// 定位到
							if(rs.next()) {
								return rs.getObject(fieldName);
							} else {
								return null;
							}
						}
					}
				}
				return null;
			}
		});
	}

	public void query(String sql, RowCallbackHandler rch, Object... args) {
		String transedSql = getTransSql(sql);
		this.getJdbcTemplate().query(transedSql, rch, args);
	}
}
