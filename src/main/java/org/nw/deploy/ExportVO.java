package org.nw.deploy;

/**
 * 
 * @author xuqc
 * @date 2014-5-19 下午08:50:45
 */
public class ExportVO {

	/**
	 * 表名称
	 */
	private String tableName;
	/**
	 * 是否导出表结构
	 */
	private boolean ddl;
	/**
	 * 是否导出表数据
	 */
	private boolean data;

	/**
	 * where条件
	 */
	private String where;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isDdl() {
		return ddl;
	}

	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	public boolean isData() {
		return data;
	}

	public void setData(boolean data) {
		this.data = data;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}
}
