package org.nw.utils;

import org.nw.vo.pub.lang.UFDouble;

/**
 * 此处插入类型说明。 创建日期：(2004-1-27 10:21:56)
 * 
 * @author：宋杰
 */
public class SafeCompute {
	/**
	 * SafeCompute 构造子注解。
	 */
	public SafeCompute() {
		super();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-1-27 10:22:44)
	 * 
	 * @return nc.vo.pub.lang.UFDouble
	 * @param d1
	 *            nc.vo.pub.lang.UFDouble
	 * @param d2
	 *            nc.vo.pub.lang.UFDouble
	 */
	public static UFDouble add(UFDouble d1, UFDouble d2) {
		d1 = (d1 == null) ? new UFDouble(0) : d1;
		d2 = (d2 == null) ? new UFDouble(0) : d2;
		return d1.add(d2);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-1-27 10:22:44)
	 * 
	 * @return nc.vo.pub.lang.UFDouble
	 * @param d1
	 *            nc.vo.pub.lang.UFDouble
	 * @param d2
	 *            nc.vo.pub.lang.UFDouble
	 */
	public static UFDouble div(UFDouble d1, UFDouble d2) {
		d1 = (d1 == null) ? new UFDouble(0) : d1;
		d2 = (d2 == null) ? new UFDouble(0) : d2;
		return d1.div(d2);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-1-27 10:22:44)
	 * 
	 * @return nc.vo.pub.lang.UFDouble
	 * @param d1
	 *            nc.vo.pub.lang.UFDouble
	 * @param d2
	 *            nc.vo.pub.lang.UFDouble
	 */
	public static UFDouble multiply(UFDouble d1, UFDouble d2) {
		d1 = (d1 == null) ? new UFDouble(0) : d1;
		d2 = (d2 == null) ? new UFDouble(0) : d2;
		return d1.multiply(d2);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-1-27 10:22:44)
	 * 
	 * @return nc.vo.pub.lang.UFDouble
	 * @param d1
	 *            nc.vo.pub.lang.UFDouble
	 * @param d2
	 *            nc.vo.pub.lang.UFDouble
	 */
	public static UFDouble sub(UFDouble d1, UFDouble d2) {
		d1 = (d1 == null) ? new UFDouble(0) : d1;
		d2 = (d2 == null) ? new UFDouble(0) : d2;
		return d1.sub(d2);
	}
}
