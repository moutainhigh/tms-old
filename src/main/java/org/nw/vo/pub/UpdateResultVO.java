/*
 * @(#)UpdateResultVO.java 1.0 2003-8-12
 *
 * Copyright 2005 UFIDA Software Co. Ltd. All rights reserved.
 * UFIDA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.nw.vo.pub;

import org.nw.vo.pub.lang.UFDateTime;

/**
 * 用于{@link nc.bs.pub.SuperDMO}的<code>insert</code>，<code>insertArray</code>
 * 方法的返回值。
 * 
 */
public class UpdateResultVO extends ValueObject {
	private String[] pks = null;

	private UFDateTime ts = null;

	public UpdateResultVO() {
		super();
	}

	/**
	 * 返回对象的显示名称。
	 * <p>
	 * 创建日期：(2001-2-15 14:18:08)
	 * 
	 * @return 对象的显示名称。
	 */
	public String getEntityName() {
		return null;
	}

	/**
	 * 主键字符串数组。
	 * <p>
	 * 创建日期：(2003-8-12 10:03:11)
	 * 
	 * @return 主键字符串数组
	 */
	public String[] getPks() {
		return pks;
	}

	/**
	 * 时间戳。
	 * <p>
	 * 创建日期：(2003-8-12 10:03:11)
	 * 
	 * @return 时间戳
	 */
	public UFDateTime getTs() {
		return ts;
	}

	/**
	 * 设置主键字符串数组。
	 * <p>
	 * 创建日期：(2003-8-12 10:03:11)
	 * 
	 * @param newPks
	 *            主键字符串数组
	 */
	public void setPks(java.lang.String[] newPks) {
		pks = newPks;
	}

	/**
	 * 设置时间戳。
	 * <p>
	 * 创建日期：(2003-8-12 10:03:11)
	 * 
	 * @param newTs
	 *            时间戳
	 */

	public void setTs(org.nw.vo.pub.lang.UFDateTime newTs) {
		ts = newTs;
	}

	/**
	 * 验证对象各属性之间的数据逻辑正确性。
	 * 
	 * @throws ValidationException
	 *             验证失败
	 */
	public void validate() throws ValidationException {
	}
}
