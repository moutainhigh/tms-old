/*
 * @(#)SqlSupportVO.java 1.0 2003-7-25
 *
 * Copyright 2005 UFIDA Software Co. Ltd. All rights reserved.
 * UFIDA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.nw.vo.pub;

/**
 * 数据库表中的字段名与VO属性名的对应关系VO类。
 * 
 * 
 */
public class SqlSupportVO extends ValueObject {
	// 数据库表中的字段名
	private String sqlSelectField = null;

	// VO属性名
	private String voAttributeName = null;

	/**
	 * 根据数据库表中的字段名和VO属性名构造。
	 * 
	 * @param sqlSelectField
	 *            数据库表中的字段名
	 * @param voAttributeName
	 *            VO属性名
	 */
	public SqlSupportVO(String sqlSelectField, String voAttributeName) {
		super();
		this.sqlSelectField = sqlSelectField;
		this.voAttributeName = voAttributeName;
	}

	/**
	 * 返回对象的显示名称。
	 * <p>
	 * 创建日期：(2001-2-15 14:18:08)
	 * 
	 * @return 显示名称。
	 */
	public String getEntityName() {
		return null;
	}

	/**
	 * 获得数据库表中的字段名。
	 * <p>
	 * 创建日期：(2003-7-25 14:16:58)
	 * 
	 * @return 数据库表中的字段名
	 */
	public String getSqlSelectField() {
		return sqlSelectField;
	}

	/**
	 * 获得与数据库表中的字段名相对应的VO属性名。
	 * 
	 * 
	 * @return VO属性名
	 */
	public String getVoAttributeName() {
		return voAttributeName;
	}

	/**
	 * 设置数据库表中的字段名。
	 * 
	 * @param newSqlSelectField
	 *            数据库表中的字段名
	 */
	public void setSqlSelectField(String newSqlSelectField) {
		sqlSelectField = newSqlSelectField;
	}

	/**
	 * 设置与数据库表中的字段名相对应的VO属性名。
	 * 
	 * @param newVoAttributeName
	 *            VO属性名
	 */
	public void setVoAttributeName(String newVoAttributeName) {
		voAttributeName = newVoAttributeName;
	}

	/**
	 * 验证对象各属性之间的数据逻辑正确性。
	 * 
	 * @throws ValidationException
	 *             验证失败。
	 */
	public void validate() throws ValidationException {
	}
}
