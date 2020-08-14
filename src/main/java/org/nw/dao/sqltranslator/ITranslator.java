package org.nw.dao.sqltranslator;

/**
 * 模块: TranslatorIF.java 
 * 描述: Sql翻译器接口,所有Sql翻译器必须实现该接口 
 * 作者: cf
 */
public interface ITranslator {
	public int getDestDbType();

	public String getSourceSql();

	public String getSql() throws Exception;

	public java.sql.SQLException getSqlException();

	public void setSql(String sql);

	public void setSqlException(java.sql.SQLException e);
}