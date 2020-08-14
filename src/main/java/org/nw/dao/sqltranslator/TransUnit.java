package org.nw.dao.sqltranslator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 此处插入类型说明。
 * 创建日期：(2001-12-28 18:31:14)
 */
public class TransUnit {
	private static final Log log = LogFactory.getLog(TransUnit.class);
	//private java.lang.String[] asSqlWords = null;
	//private java.lang.String sWords = null;
	private int iOffSet = 0;

	public java.lang.String sql = null;

	public java.lang.String[] sqlArray = null;

	private boolean dontHaveWhere = false;

	/**
	 * TransUnit 构造子注解。
	 */
	public TransUnit() {
		super();
		log.debug("nc.bs.mw.sqltrans.TransUnit Over");
	}

	/**
	 * TransUnit 构造子注解。
	 */
	public TransUnit(String[] newStArray, String newSql, int newOffset) {
		super();
		setSqlArray(newStArray);
		setSql(newSql);
		setIOffSet(newOffset);
		log.debug("nc.bs.mw.sqltrans.TransUnit Over");
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 18:33:44)
	 * 
	 * @return int
	 */
	public int getIOffSet() {
		log.debug("nc.bs.mw.sqltrans.TransUnit.getIOffSet");
		log.debug("nc.bs.mw.sqltrans.TransUnit.getIOffSet Over");
		return iOffSet;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 18:34:26)
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getSql() {
		log.debug("nc.bs.mw.sqltrans.TransUnit.getSql");
		log.debug("nc.bs.mw.sqltrans.TransUnit.getSql Over");
		return sql;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 18:47:53)
	 * 
	 * @return java.lang.String[]
	 */
	public java.lang.String[] getSqlArray() {
		log.debug("nc.bs.mw.sqltrans.TransUnit.getSqlArray");
		log.debug("nc.bs.mw.sqltrans.TransUnit.getSqlArray Over");
		return sqlArray;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 21:26:09)
	 * 
	 * @return boolean
	 */
	public boolean isDontHaveWhere() {
		log.debug("nc.bs.mw.sqltrans.TransUnit.isDontHaveWhere");
		log.debug("nc.bs.mw.sqltrans.TransUnit.isDontHaveWhere Over");
		return dontHaveWhere;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 21:26:09)
	 * 
	 * @param newDontHaveWhere
	 *            boolean
	 */
	public void setDontHaveWhere(boolean newDontHaveWhere) {
		log.debug("nc.bs.mw.sqltrans.TransUnit.setDontHaveWhere");
		dontHaveWhere = newDontHaveWhere;
		log.debug("nc.bs.mw.sqltrans.TransUnit.setDontHaveWhere Over");
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 18:33:44)
	 * 
	 * @param newIOffSet
	 *            int
	 */
	public void setIOffSet(int newIOffSet) {
		log.debug("nc.bs.mw.sqltrans.TransUnit.setIOffSet");
		iOffSet = newIOffSet;
		log.debug("nc.bs.mw.sqltrans.TransUnit.setIOffSet Over");
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 18:34:26)
	 * 
	 * @param newSql
	 *            java.lang.String
	 */
	public void setSql(java.lang.String newSql) {
		log.debug("nc.bs.mw.sqltrans.TransUnit.setSql");
		sql = newSql;
		log.debug("nc.bs.mw.sqltrans.TransUnit.setSql Over");
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2001-12-28 18:47:53)
	 * 
	 * @param newSqlArray
	 *            java.lang.String[]
	 */
	public void setSqlArray(java.lang.String[] newSqlArray) {
		log.debug("nc.bs.mw.sqltrans.TransUnit.setSqlArray");
		sqlArray = newSqlArray;
		log.debug("nc.bs.mw.sqltrans.TransUnit.setSqlArray Over");
	}
}