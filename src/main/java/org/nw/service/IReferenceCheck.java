package org.nw.service;

import org.nw.vo.pub.BusinessException;

/**
 * 关联校验，目前使用的删除档案时的校验
 * 
 * @author xuqc
 * @date 2012-8-14 上午10:47:20
 */
public interface IReferenceCheck {
	/**
	 * 查询tableName中主键字段的值为key的记录是否被引用了
	 * 
	 * @param tableName
	 * @param key
	 * @return ture 如果被引用,否则为false
	 * @throws BusinessException
	 */
	public boolean isReferenced(String tableName, String key);
}
