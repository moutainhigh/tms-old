/*
 * @(#)AggregatedValueObject.java 1.0 2001-3-20
 *
 * Copyright 2005 UFIDA Software Co. Ltd. All rights reserved.
 * UFIDA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.nw.vo.pub;

/**
 * 针对具有一对多关系的母子表设计的聚合VO对象的抽象基类。它的内部包括一个针对母表的母VO对象和针对<em>单个</em>子表的若干个子VO对象。
 * 只适合一母一子关系。一母多子请使用{@link ExtendedAggregatedValueObject}。
 * 
 * 
 * @author Zhao Jijiang
 * @since 2.0
 * @see ExtendedAggregatedValueObject
 */
public abstract class AggregatedValueObject implements java.io.Serializable {
	public AggregatedValueObject() {
		super();
	}

	/**
	 * 获得子表VO对象数组。
	 * 
	 * @return 子表VO对象数组
	 */
	public abstract CircularlyAccessibleValueObject[] getChildrenVO();

	/**
	 * 获得母表VO对象。
	 * 
	 * @return 母表VO对象
	 */
	public abstract CircularlyAccessibleValueObject getParentVO();

	/**
	 * 设置子表VO对象数组。
	 * 
	 * @param children
	 *            子表VO对象数组
	 */
	public abstract void setChildrenVO(CircularlyAccessibleValueObject[] children);

	/**
	 * 设置母表VO对象。
	 * 
	 * @param parent
	 *            母表VO对象
	 */
	public abstract void setParentVO(CircularlyAccessibleValueObject parent);
}
