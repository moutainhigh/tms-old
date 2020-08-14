package org.nw.deploy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.nw.web.utils.WebUtils;

/**
 * 数据库操作管理类
 * 
 */
public class DB {

	private static Logger logger = Logger.getLogger(DB.class.getName());

	// SQL语句对象
	private Statement stmt;
	// 带参数的Sql语句对象
	private PreparedStatement pstmt;
	// 记录集对象
	private ResultSet rs;

	private Connection conn;

	private String driver;
	private String url;
	private String username;
	private String password;

	public boolean ifInit;

	/**
	 * 从配置文件中读取数据库连接信息
	 */
	public DB() {
		this(false);
	}

	public DB(boolean ifInit) {
		this.ifInit = ifInit;
		Document document = ExportConfig.getDocument(ifInit);
		if(document == null) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("读取配置文件错误，配置文件不存在！");
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Read the configuration file error, the configuration file does not exist!");
			}
			throw new RuntimeException("读取配置文件错误，配置文件不存在！");
		}
		Element root = document.getRootElement();
		driver = root.selectSingleNode("/export/db/driver").getText();
		url = root.selectSingleNode("/export/db/url").getText();
		username = root.selectSingleNode("/export/db/username").getText();
		password = root.selectSingleNode("/export/db/password").getText();
		logger.info("数据库连接信息如下：");
		logger.info("driver:" + driver);
		logger.info("url:" + url);
		logger.info("username:" + username);
		logger.info("password:" + password);
		conn = getConnection();
		if(conn == null) {
			logger.fine("无法得到数据库连接！");
		}
	}

	public Connection getConn() {
		return conn;
	}

	/**
	 * 从指定参数得到一个连接对象
	 * 
	 * @param driver
	 * @param url
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public Connection getConnection() {
		try {
			logger.info("从指定配置中得到一个数据库连接");
			Class.forName(driver);
			return DriverManager.getConnection(url, username, password);
		} catch(ClassNotFoundException ex) {
			logger.info("找不到类驱动类: " + driver);
			ex.printStackTrace();
		} catch(SQLException ex) {
			logger.info("加载类: " + driver + " 时出现 SQLException 异常");
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 是否mysql数据库
	 * 
	 * @return
	 */
	public boolean isMysql() {
		if(url.toLowerCase().startsWith("jdbc:mysql")) {
			return true;
		}
		return false;
	}

	/**
	 * 执行SQL语句操作(更新数据 无参数)
	 * 
	 * @param strSql
	 *            SQL语句
	 * @throws Exception
	 */
	public boolean executeUpdate(String strSql) throws SQLException {
		boolean flag = false;
		stmt = conn.createStatement();
		logger.info("执行SQL语句操作(更新数据 无参数):" + strSql);
		try {
			if(0 < stmt.executeUpdate(strSql)) {
				flag = true;
				conn.commit();
			}
		} catch(SQLException ex) {
			logger.info("执行SQL语句操作(更新数据 无参数):" + strSql + "失败!");
			flag = false;
			conn.rollback();
			throw ex;
		} finally {
			close();
		}
		return flag;

	}

	/**
	 * 执行SQL语句操作(更新数据 有参数)
	 * 
	 * @param strSql
	 *            sql指令
	 * @param prams
	 *            参数列表
	 * @return
	 * @throws SQLException
	 */
	public boolean executeUpdate(String strSql, HashMap<Integer, Object> prams) throws SQLException,
			ClassNotFoundException {
		boolean flag = false;
		try {
			pstmt = conn.prepareStatement(strSql);
			setParamet(pstmt, prams);
			logger.info("执行SQL语句操作(更新数据 有参数):" + strSql);

			if(0 < pstmt.executeUpdate()) {
				flag = true;
				conn.commit();
			}
		} catch(SQLException ex) {
			logger.info("执行SQL语句操作(更新数据 无参数):" + strSql + "失败!");
			flag = false;
			conn.rollback();
			throw ex;
		} catch(ClassNotFoundException ex) {
			logger.info("执行SQL语句操作(更新数据 无参数):" + strSql + "失败! 参数设置类型错误!");
			conn.rollback();
			throw ex;
		} finally {
			close();
		}
		return flag;

	}

	/**
	 * 执行SQL语句操作(查询数据 无参数)
	 * 
	 * @param strSql
	 *            SQL语句
	 * @return 数组对象列表
	 * @throws Exception
	 */
	public ArrayList<HashMap<Object, Object>> executeSql(String strSql) throws Exception {
		try {
			stmt = conn.createStatement();
			logger.info("执行SQL语句操作(查询数据):" + strSql);
			rs = stmt.executeQuery(strSql);
			conn.commit();
			if(null != rs) {
				return convertResultSetToArrayList(rs);
			}
		} finally {
			close();
		}
		return null;
	}

	/**
	 * 执行SQL语句操作(查询数据 有参数)
	 * 
	 * @param strSql
	 *            SQL语句
	 * @param prams
	 *            参数列表
	 * @return 数组对象列表
	 * @throws Exception
	 */
	public ArrayList<HashMap<Object, Object>> executeSql(String strSql, HashMap<Integer, Object> prams)
			throws Exception {
		try {
			pstmt = conn.prepareStatement(strSql);
			setParamet(pstmt, prams);
			logger.info("执行SQL语句操作(查询数据):" + strSql);
			rs = pstmt.executeQuery();
			conn.commit();
			if(null != rs) {
				return convertResultSetToArrayList(rs);
			}
		} finally {
			close();
		}
		return null;
	}

	/**
	 * 执行存储过程(查询数据 无参数)
	 * 
	 * @param procName
	 *            存储过程名称
	 * @return 数组列表对象
	 * @throws Exception
	 */
	public ArrayList<HashMap<Object, Object>> executeProcedureQuery(String procName) throws Exception {
		try {
			String callStr = "{call " + procName + "}";// 构造执行存储过程的sql指令
			CallableStatement cs = conn.prepareCall(callStr);
			logger.info("执行存储过程(查询数据):" + procName);
			rs = cs.executeQuery();
			conn.commit();
			cs.close();
		} finally {
			close();
		}
		return convertResultSetToArrayList(rs);
	}

	/**
	 * 执行存储过程(查询数据,带参数)返回结果集合
	 * 
	 * @param procName
	 *            存储过程名称
	 * @param parameters
	 *            参数对象数组
	 * @param al
	 *            数组列表对象
	 * @return 数组列表对象
	 * @throws Exception
	 */
	public ArrayList<HashMap<Object, Object>> executeProcedureQuery(String procName, Object[] parameters)
			throws Exception {
		ArrayList<HashMap<Object, Object>> procedureInfo = getProcedureInfo(procName);
		try {
			int parameterPoint = 0;
			// 获取存储过程信息列表集合
			// 获取存储过程的完全名称
			String procedureCallName = getProcedureCallName(procName, parameters.length);
			// 初始化 存储过程 执行对象
			CallableStatement cs = conn.prepareCall(procedureCallName);
			// 参数下标变量
			int index = 0;
			// 获取 存储过程信息列表集合的 迭代器 对象
			Iterator<HashMap<Object, Object>> iter = procedureInfo.iterator();
			// 遍历存储过程信息列表集合
			while(iter.hasNext()) {
				HashMap<Object, Object> hm = iter.next();

				parameterPoint++;
				// 如果参数是输入参数 way = 0
				if(hm.get("WAY").equals("0")) {
					// 设置参数到cs
					cs.setObject(parameterPoint, parameters[index]);
					// 参数下标+1
					index++;
				}
			}
			// 释放这个对象,做为第二次使用
			procedureInfo = null;
			logger.info("执行存储过程(查询数据):" + procedureCallName);
			rs = cs.executeQuery();
			conn.commit();
			procedureInfo = convertResultSetToArrayList(rs);
			cs.close();
		} finally {
			close();
		}
		return procedureInfo;

	}

	/**
	 * 关闭数据对象
	 */
	public void close() {
		if(null != rs) {
			try {
				rs.close();
			} catch(SQLException ex) {
				rs = null;
			}
		}
		if(null != stmt) {
			try {
				stmt.close();
			} catch(SQLException ex) {
				stmt = null;
			}
		}
		if(null != pstmt) {
			try {
				pstmt.close();
			} catch(SQLException ex) {
				pstmt = null;
			}
		}
	}

	public void closeConn() {
		if(conn != null) {
			try {
				conn.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置Sql 指令参数
	 * 
	 * @param p_stmt
	 *            PreparedStatement
	 * @param pramets
	 *            HashMap
	 */
	private PreparedStatement setParamet(PreparedStatement p_stmt, HashMap<Integer, Object> pramets)
			throws ClassNotFoundException, SQLException {
		// 如果参数为空
		if(null != pramets) {
			// 如果参数个数为0
			if(0 <= pramets.size()) {
				for(int i = 1; i <= pramets.size(); i++) {
					try {
						// 字符类型 String
						if(pramets.get(i).getClass() == Class.forName("java.lang.String")) {
							p_stmt.setString(i, pramets.get(i).toString());
						}
						// 日期类型 Date
						if(pramets.get(i).getClass() == Class.forName("java.sql.Date")) {
							p_stmt.setDate(i, java.sql.Date.valueOf(pramets.get(i).toString()));
						}
						// 布尔类型 Boolean
						if(pramets.get(i).getClass() == Class.forName("java.lang.Boolean")) {
							p_stmt.setBoolean(i, (Boolean) (pramets.get(i)));
						}
						// 整型 int
						if(pramets.get(i).getClass() == Class.forName("java.lang.Integer")) {
							p_stmt.setInt(i, (Integer) pramets.get(i));
						}
						// 浮点 float
						if(pramets.get(i).getClass() == Class.forName("java.lang.Float")) {
							p_stmt.setFloat(i, (Float) pramets.get(i));
						}
						// 双精度型 double
						if(pramets.get(i).getClass() == Class.forName("java.lang.Double")) {
							p_stmt.setDouble(i, (Double) pramets.get(i));
						}

					} catch(ClassNotFoundException ex) {
						throw ex;
					} catch(SQLException ex) {
						throw ex;
					}
				}
			}
		}
		return p_stmt;
	}

	/**
	 * 转换记录集对象为数组列表对象
	 * 
	 * @param rs
	 *            纪录集合对象
	 * @return 数组列表对象
	 * @throws Exception
	 */
	private ArrayList<HashMap<Object, Object>> convertResultSetToArrayList(ResultSet rs) throws Exception {
		// logger.info("转换记录集对象为数组列表对象");
		// 获取rs 集合信息对象
		ResultSetMetaData rsmd = rs.getMetaData();
		// 创建数组列表集合对象
		ArrayList<HashMap<Object, Object>> tempList = new ArrayList<HashMap<Object, Object>>();
		HashMap<Object, Object> tempHash = null;
		// 填充数组列表集合
		while(rs.next()) {
			// 创建键值对集合对象
			tempHash = new HashMap<Object, Object>();
			for(int i = 0; i < rsmd.getColumnCount(); i++) {
				// 遍历每列数据，以键值形式存在对象tempHash中
				tempHash.put(rsmd.getColumnName(i + 1).toUpperCase(), rs.getString(rsmd.getColumnName(i + 1)));
			}
			// 第一个键值对，存储在tempList列表集合对象中
			tempList.add(tempHash);
		}
		return tempList;// 返回填充完毕的数组列表集合对象
	}

	/**
	 * 从数据库得到存储过程信息
	 * 
	 * @param procName
	 *            存储过程名称
	 * @return 数组列表对象
	 * @throws Exception
	 */
	private ArrayList<HashMap<Object, Object>> getProcedureInfo(String procName) throws Exception {
		return this.executeSql("select Syscolumns.isoutparam as Way,systypes.name as TypeName from sysobjects,syscolumns,systypes where systypes.xtype=syscolumns.xtype and syscolumns.id=sysobjects.id and sysobjects.name='"
						+ procName + "' order by Syscolumns.isoutparam");
	}

	/**
	 * 从数据库得到存储过程参数个数
	 * 
	 * @param procName
	 *            存储过程名称
	 * @return 数组列表对象
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private int getParametersCount(String procName) throws Exception {
		int returnVal = 0;
		for(HashMap<Object, Object> tempHas : this.executeSql("select count(*) as RowsCount from sysobjects,syscolumns,systypes where systypes.xtype=syscolumns.xtype and syscolumns.id=sysobjects.id and sysobjects.name='"
						+ procName + "'")) {
			returnVal = Integer.parseInt(tempHas.get("ROWSCOUNT").toString());
		}
		return returnVal;
	}

	/**
	 * 得到调用存储过程的全名
	 * 
	 * @param procName
	 *            存储过程名称
	 * @return 调用存储过程的全名
	 * @throws Exception
	 */
	private String getProcedureCallName(String procName, int prametCount) throws Exception {
		String procedureCallName = "{call " + procName;
		for(int i = 0; i < prametCount; i++) {
			if(0 == i) {
				procedureCallName = procedureCallName + "(?";
			}
			if(0 != i) {
				procedureCallName = procedureCallName + ",?";
			}
		}
		procedureCallName = procedureCallName + ")}";
		return procedureCallName;
	}

	/**
	 * ALTER TABLE table_name ADD INDEX index_name (column_list) ALTER TABLE
	 * table_name ADD UNIQUE (column_list) ALTER TABLE table_name ADD PRIMARY
	 * KEY (column_list)
	 */
	public String generateDdl(String Tablename) {
		try {
			DatabaseMetaData odmd = conn.getMetaData();
			// 取表名
			String commnt = "";
			String indexu = "";
			ResultSet pkRSet = odmd.getPrimaryKeys(null, null, Tablename);
			ResultSet rscol = odmd.getColumns(null, null, Tablename, null);
			ResultSet inset = odmd.getIndexInfo(null, null, Tablename, false, true);
			String colstr = "";
			while(rscol.next()) {
				String ColumnName = rscol.getString(4);
				String ColumnTypeName = rscol.getString(6);
				String REMARKS = rscol.getString(12);
				if(StringUtils.isNotBlank(REMARKS)) {
					commnt = commnt + "COMMENT ON " + Tablename + " ( " + ColumnName + " IS '" + REMARKS + "' ); \n";
				}
				while(inset.next()) {
					if(inset.getInt(7) == DatabaseMetaData.tableIndexOther) {
						indexu = indexu + "CREATE  UNIQUE  INDEX " + inset.getString(6) + " ON " + inset.getString(5)
								+ "(" + inset.getString(9) + ");\n";
					}
				}

				int displaySize = rscol.getInt(7);
				int scale = rscol.getInt(9);
				// int Precision = displaySize-scale;
				if(StringUtils.isNotBlank(colstr)) {
					colstr = colstr + ",\n";
				}

				if(isMysql()) {
					// 对mysql关键字进行处理
					for(String keyword : MysqlGenerator.keywords) {
						if(keyword.equals(ColumnName.toLowerCase())) {
							ColumnName = "`" + ColumnName + "`";
						}
					}
				}

				colstr = colstr + "\t" + ColumnName + "\t";
				if(StringUtils.indexOf(ColumnTypeName, "IDENTITY") >= 0) {
					colstr = colstr + ColumnTypeName + "(1,1)";
				} else if(StringUtils.equalsIgnoreCase(ColumnTypeName, "TIMESTAMP")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "INT")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "DATETIME")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "LONG")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "DATE")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "TEXT")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "IMAGE")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "BIT")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "NTEXT")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "SMALLINT")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "BIGINT")) {
					colstr = colstr + ColumnTypeName.toUpperCase() + "";
				} else if(StringUtils.equalsIgnoreCase(ColumnTypeName, "DECIMAL")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "NUMBER")
						|| StringUtils.equalsIgnoreCase(ColumnTypeName, "DOUBLE")) {
					if(scale == 0)
						colstr = colstr + ColumnTypeName.toUpperCase() + "(" + displaySize + ")";
					else
						colstr = colstr + ColumnTypeName.toUpperCase() + "(" + displaySize + "," + scale + ")";
				} else {
					colstr = colstr + ColumnTypeName.toUpperCase() + "(" + displaySize + ")";
				}
				String defaultstr = rscol.getString(13);
				if(defaultstr != null)
					colstr = colstr + "\t DEFAULT " + defaultstr;
				if(rscol.getInt(11) == DatabaseMetaData.columnNoNulls) {
					colstr = colstr + "\t NOT NULL";
				} else if(rscol.getInt(11) == DatabaseMetaData.columnNullable) {
					// sql.append("\tnull");
				}
			}
			String pkcolstr = "";
			while(pkRSet.next()) {

				if(StringUtils.isNotBlank(pkcolstr)) {
					pkcolstr = pkcolstr + ",\n";
				} else {
					if(StringUtils.isNotBlank(colstr)) {
						colstr = colstr + ",\n";
					}
				}
				pkcolstr = pkcolstr + "\t CONSTRAINT " + pkRSet.getObject(6) + " PRIMARY KEY (" + pkRSet.getObject(4)
						+ ")";
			}
			StringBuffer sb = new StringBuffer();
			// sb.append("DROP TABLE " + Tablename + " \n ; \n");
			sb.append("CREATE TABLE " + Tablename + "\n(" + colstr + pkcolstr + "\n)");
			sb.append("\n");
			if(isMysql()) {
				sb.append("type = InnoDB;");
			} else {
				sb.append(";");
			}

			sb.append("\n");
			if(StringUtils.isNotBlank(commnt)) {
				sb.append("\n");
				sb.append(commnt);
				sb.append(" ; ");
				sb.append("\n");
			}
			// 索引先不要创建，避免跟主键的索引重复
			// if(StringUtils.isNotBlank(indexu)) {
			// sb.append("\n");
			// sb.append(indexu);
			// sb.append(" ; ");
			// sb.append("\n");
			// }
			return sb.toString();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return null;
	}
}
