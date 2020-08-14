package org.nw.service.impl;

import org.nw.dao.RefRelationDao;
import org.nw.service.IReferenceCheck;

/**
 * 检查档案是否被引用的实现类
 * 
 * @author xuqc
 * @date 2012-8-14 上午10:49:24
 */
public class ReferenceCheckImpl implements IReferenceCheck {

	public boolean isReferenced(String tableName, String key) {
		return new RefRelationDao().isReferenced(tableName, key);
	}

}
