package org.nw.deploy;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Update by internetroot on 2014-09-06.
 */
public class MysqlGenerator {
	private static Logger logger = Logger.getLogger(MysqlGenerator.class.getName());

	private static String insert = "INSERT INTO";// 插入sql
	private static String values = "VALUES";// values关键字
	private static List<String> insertList = new ArrayList<String>();// 全局存放insertsql文件的数据

	/**
	 * 获取列名和列值
	 *
	 * @param sm
	 * @param selectSqlList
	 * @param rs
	 * @return
	 * @throws java.sql.SQLException
	 */
	public static List<String> getColumnNameAndColumeValue(Statement sm, String selectSql, String tableName) {
		List<String> sqlList = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = sm.executeQuery(selectSql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()) {
				StringBuffer ColumnName = new StringBuffer();
				StringBuffer ColumnValue = new StringBuffer();
				for(int i = 1; i <= columnCount; i++) {
					String value = rs.getString(i);
					if(i == columnCount) {
						ColumnName.append(rsmd.getColumnName(i));
						if(Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i)
								|| Types.LONGVARCHAR == rsmd.getColumnType(i)) {
							if(value == null) {
								ColumnValue.append("null");
							} else {
								ColumnValue.append("'").append(value).append("'");
							}
						} else if(Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i)
								|| Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i)
								|| Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i)
								|| Types.DECIMAL == rsmd.getColumnType(i)) {
							if(value == null) {
								ColumnValue.append("null");
							} else {
								ColumnValue.append(value);
							}
						} else if(Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i)
								|| Types.TIMESTAMP == rsmd.getColumnType(i)) {
							if(value == null) {
								ColumnValue.append("null");
							} else {
								ColumnValue.append("timestamp'").append(value).append("'");
							}
						} else {
							if(value == null) {
								ColumnValue.append("null");
							} else {
								ColumnValue.append(value);
							}
						}
					} else {
						ColumnName.append(rsmd.getColumnName(i) + ",");
						if(Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i)
								|| Types.LONGVARCHAR == rsmd.getColumnType(i)) {
							if(value == null) {
								ColumnValue.append("null,");
							} else {
								ColumnValue.append("'").append(value).append("',");
							}
						} else if(Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i)
								|| Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i)
								|| Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i)
								|| Types.DECIMAL == rsmd.getColumnType(i)) {
							if(value == null) {
								ColumnValue.append("null,");
							} else {
								ColumnValue.append(value).append(",");
							}
						} else if(Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i)
								|| Types.TIMESTAMP == rsmd.getColumnType(i)) {
							if(value == null) {
								ColumnValue.append("null,");
							} else {
								ColumnValue.append("timestamp'").append(value).append("',");
							}
						} else {
							if(value == null) {
								ColumnValue.append("null,");
							} else {
								ColumnValue.append(value).append(",");
							}
						}
					}
				}
				// System.out.println(ColumnName.toString());
				// System.out.println(ColumnValue.toString());
				String insertSql = buildInsertSQL(tableName, ColumnName, ColumnValue);
				sqlList.add(insertSql);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.info("导出表【" + tableName + "】的数据时出现异常，异常信息：" + e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return sqlList;
	}

	public static String[] keywords = new String[] { "interval", "level", "position", "describe", "module" };

	/**
	 * 拼装insertsql 放到全局list里面
	 *
	 * @param ColumnName
	 * @param ColumnValue
	 */
	private static String buildInsertSQL(String TableName, StringBuffer ColumnName, StringBuffer ColumnValue) {
		String cn = ColumnName.toString();
		for(String keyword : keywords) {
			if(keyword.equals(cn.toLowerCase())) {
				cn = "`" + keyword + "`";
			}
		}
		StringBuffer insertSQL = new StringBuffer();
		insertSQL.append(insert).append(" ").append(TableName).append("(").append(cn).append(")").append(values)
				.append("(").append(ColumnValue.toString()).append(");");
		insertList.add(insertSQL.toString());
		return insertSQL.toString();
	}
}
