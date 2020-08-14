package org.nw.dao.mapper;

import java.sql.ResultSet;

import org.nw.dao.helper.DaoHelper;
import org.springframework.jdbc.core.RowMapper;


/**
 * 为了针对UFDouble、UFBoolean等数据类型进行特殊处理
 * 
 * @author fangw
 * @date 2010-12-6
 */
public class UftRowMapper<T> implements RowMapper<T> {
	private Class<T> clazz;

	public UftRowMapper(Class<T> clazz) {
		super();
		this.clazz = clazz;
	}

	public T mapRow(ResultSet rs, int rowNum) {
		T obj = null;
		try {
			obj = clazz.newInstance();
			DaoHelper.convertResultSet(obj, rs);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return obj;
	}
}
