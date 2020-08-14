package org.nw.vo.pub;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * <p>
 * 可在一个循环中依次存、取所有属性的一类特殊VO。
 * <p>
 * 例如：A组件获得B组件传来的一个VO对象后，可通过下面代码获得此VO对象的 所有属性值，而不必事先知道这个VO对象有那些属性：
 * <p>=
 * ========================================
 * <p>
 * String[] keys = vo.getAttributeNames();
 * <p>
 * for ( int i = 0; i < keys.length; i++ ) {
 * <p>
 * <p>
 * Object obj = getAttributeValue(key[i]);
 * <p>
 * ... ...//使用obj
 * <p>
 * <p>=
 * ========================================
 * <p>
 * 这类VO对象可应用在档案、参照等领域。
 * <p>
 * 创建日期：(01-3-20 17:15:05)
 * 
 * @author：Zhao Jijiang
 */
public abstract class CircularlyAccessibleValueObject extends ValueObject {
	private static final long serialVersionUID = -4360103926517671160L;
	private int status = VOStatus.UNCHANGED;

	/**
	 * CircularlyAccessibleValueObject 构造子注解。
	 */
	public CircularlyAccessibleValueObject() {
		super();
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:26:03)
	 * 
	 * @return java.lang.String[]
	 */
	public abstract String[] getAttributeNames();

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:24:29)
	 * 
	 * @param key
	 *            java.lang.String
	 */
	public abstract Object getAttributeValue(String attributeName);

	/**
	 * 此处插入方法说明。 创建日期：(2001-4-11 14:48:58)
	 * 
	 * @return int
	 */
	@JsonIgnore
	public int getStatus() {

		return this.status;
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-3-20 17:24:29)
	 * 
	 * @param key
	 *            java.lang.String
	 */
	public abstract void setAttributeValue(String name, Object value);

	/**
	 * 此处插入方法说明。 创建日期：(2001-4-11 14:48:58)
	 * 
	 * @return int
	 */
	public void setStatus(int status) {

		this.status = status;
	}
}
