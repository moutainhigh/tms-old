package org.nw.dao.sqltranslator;

/**
 *模块:	SqlTranslator.java
 *描述:	Sql翻译器的对外接口
 *作者:	cf
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;

public class SqlTranslator implements DBConsts {
	private static final Log log = LogFactory.getLog(SqlTranslator.class);

	/**
	 * SqlTranslator 构造子
	 */
	public SqlTranslator() {
		super();

		initTranslator(SQLSERVER);
		// 缺省设置目标数据库为SQLSERVER
		log.debug("nc.bs.mw.sqltrans.SqlTranslator Over");
	}

	/**
	 * SqlTranslator 构造子
	 */

	public SqlTranslator(int dbType) {
		super();
		m_dbType = dbType;
		initTranslator(dbType);
		// 输入目标数据库类型
		log.debug("nc.bs.mw.sqltrans.SqlTranslator Over");
	}

	/**
	 * 根据目标数据库类型转换SQL语句
	 */

	public String getSql(String srcSql) throws java.sql.SQLException {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSql");

		int destDbType = getDestDbType(); // 判别数据库类型
		// 结果返回
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSql Over");
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			//FIXME 等我学会正则表达式再说。
			srcSql = srcSql.replaceAll("with//(nolock//)", "");
			srcSql = srcSql.replaceAll("WITH//(NOLOCK//)", "");
			srcSql = srcSql.replaceAll("WITH(NOLOCK)", "");
		}
		return getSql(srcSql, destDbType);
	}

	/**
	 * 根据目标数据库类型转换SQL语句
	 */

	public String getSql(String srcSql, int destDbType) throws java.sql.SQLException {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSql");

		// 若目标数据库类型变更，则重新设置
		if(m_trans == null || m_trans.getDestDbType() != destDbType) {
			initTranslator(destDbType);
		}
		// 设置数据库的版本
		((TranslatorObject) m_trans).setDbVersion(m_DbVersion);

		// 结果返回
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSql Over");
		return getResultSql(srcSql);
	}

	/**
	 * 根据目标数据库类型转换SQL异常
	 */

	synchronized public java.sql.SQLException getSqlException(java.sql.SQLException srcExp) {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSqlException");
		// 若不转换，则原样返回
		if(!m_bTranslate)
			return srcExp;
		// 设置被转换异常
		m_trans.setSqlException(srcExp);
		// 取得并返回异常转换结果
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSqlException Over");
		return m_trans.getSqlException();
	}

	/**
	 * 根据目标数据库类型转换SQL异常
	 */

	public java.sql.SQLException getSqlException(java.sql.SQLException srcExp, int destDbType) {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSqlException");
		// 若目标数据库类型变换，则设置变换
		if(m_trans.getDestDbType() != destDbType) {
			initTranslator(destDbType);
		}
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getSqlException Over");
		return getSqlException(srcExp);
	}

	/**
	 * 返回变换时间。 创建日期：(00-6-22 14:16:58)
	 * 
	 * @return long
	 */
	public long getTransTime() {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getTransTime");
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getTransTime Over");
		return m_lTime;
	}

	/**
	 * 根据目标数据库类型,启动不同的翻译器 缺省为sql server
	 **/
	private void initTranslator(int dbType) {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.initTranslator");

		switch(dbType){
		case DB2:
			m_trans = new TranslateToDB2();
			break;
		case ORACLE:
			m_trans = new TranslateToOracle();
			break;
		case SYBASE:
			m_trans = new TranslateToSybase();
			break;
		case SQLSERVER:
			m_trans = new TranslateToSqlServer();
			break;
		case INFORMIX:
			m_trans = new TranslateToInformix();
			break;
		case MYSQL:
			m_trans = new TranslateToMysql();
			break;
		default: // 缺省为SQLSERVER;
			m_trans = new TranslateToSqlServer();
		}
		// System.out.println("hey:----the translator--"+m_trans.getClass().getName());
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.initTranslator Over");
	}

	/**
	 * 设置是否翻译标志。 创建日期：(00-9-17 18:30:18)
	 * 
	 * @param b
	 *            boolean
	 */
	public void setTransFlag(boolean b) {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.setTransFlag");
		m_bTranslate = b;
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.setTransFlag Over");
	}

	private boolean m_bTranslate = true;

	private int m_dbType = -1;

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-16 11:04:26) 功能：通过其他模块接口返回数据库类型，缺省为SQLSERVER;
	 * 
	 * @return int 数据库类型
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	public int getDestDbType() {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getDestDbType");

		if(m_dbType != -1)
			return m_dbType;
		else
			log.debug("nc.bs.mw.sqltrans.SqlTranslator.getDestDbType Over");
		return SQLSERVER;
	}

	/**
	 * 得到根据目标数据库类型转换后的SQL语句
	 */

	synchronized public String getResultSql(String srcSql) throws java.sql.SQLException {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getResultSql");
		// 若不转换，返回原SQL语句
		if(!m_bTranslate)
			return srcSql;
		// 记录当前时间
		m_lTime = System.currentTimeMillis();
		// 取得转换结果
		String sResult;
		try {

			if(srcSql == null)
				return "";

			srcSql = srcSql.trim();
			srcSql = trimPreChars(srcSql);

			m_trans.setSql(srcSql);
			log.info(srcSql);
			sResult = m_trans.getSql();

		} catch(Exception e) {
			throw new java.sql.SQLException(e.getMessage());
		}

		// 计算所用时间
		m_lTime = System.currentTimeMillis() - m_lTime;
		// 返回转换结果
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.getResultSql Over");
		return sResult;
	}

	/**
	 * 根据目标数据库类型转换SQL语句
	 */

	public String trimPreChars(String srcSql) throws java.sql.SQLException {
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.trimPreChars");
		if(srcSql == null || srcSql.length() < 1)
			return "";
		int pos = 0;
		String lineSep = System.getProperty("line.separator");
		while(pos < srcSql.length()
				&& (srcSql.charAt(pos) == ' ' || srcSql.charAt(pos) == '\t' || lineSep.indexOf(srcSql.charAt(pos)) >= 0)) {
			pos++;
		}
		log.debug("nc.bs.mw.sqltrans.SqlTranslator.trimPreChars Over");
		return srcSql.substring(pos);
	}

	// 数据库版本
	private int m_DbVersion = 0;

	// 是否需要翻译标志
	private long m_lTime = 0;// 翻译所需时间

	private ITranslator m_trans = null;// 翻译器

}