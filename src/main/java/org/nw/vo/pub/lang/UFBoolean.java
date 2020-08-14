package org.nw.vo.pub.lang;

/**
 * 布尔类型封装 <br>
 */
public final class UFBoolean implements java.io.Serializable, Comparable {

	/**
	 * The <code>Boolean</code> object corresponding to the primitive value
	 * <code>true</code>.
	 */
	public static final UFBoolean TRUE = new UFBoolean(true);

	/**
	 * The <code>Boolean</code> object corresponding to the primitive value
	 * <code>false</code>.
	 */
	public static final UFBoolean FALSE = new UFBoolean(false);

	private static final long serialVersionUID = -2971431361057093474L;

	private boolean value = false;

	// add by xuqc
	/* 增加一个默认构造方法，在json-lib转换时使用 */
	public UFBoolean() {
	}

	/**
	 * 按字符型构造UFBoolean---'Y'和'y'时为true,其它为false。
	 */
	public UFBoolean(char ch) {
		super();
		value = (ch == 'Y' || ch == 'y');
	}

	/**
	 * 按字符串构造UFBoolean---"Y"和"y"时为true,其它为false。
	 */
	public UFBoolean(String val) {
		if(val != null && val.length() > 0
				&& (val.equalsIgnoreCase("true") || val.charAt(0) == 'Y' || val.charAt(0) == 'y'))
			value = true;
		else
			value = false;
	}

	/**
	 * 按布尔型构造UFBoolean。
	 */
	public UFBoolean(boolean b) {
		super();
		value = b;
	}

	/**
	 * 返回对象boolean型值
	 */
	public boolean booleanValue() {
		return value;
	}

	public static UFBoolean valueOf(boolean b) {
		return (b ? TRUE : FALSE);
	}

	public static UFBoolean valueOf(String val) {
		if(val != null && val.length() > 0
				&& (val.equalsIgnoreCase("true") || val.charAt(0) == 'Y' || val.charAt(0) == 'y'))
			return TRUE;
		else
			return FALSE;
	}

	/**
	 * 比较两对象值是否相等(不是比较对象本身是否为同一对象)。
	 */
	public boolean equals(Object obj) {
		if((obj != null) && (obj instanceof UFBoolean)) {
			return value == ((UFBoolean) obj).booleanValue();
		}
		return false;
	}

	/**
	 * 生成接收方的散列代码---和Boolean相同。
	 */
	public int hashCode() {
		return value ? 1231 : 1237;
	}

	/**
	 * 返回表示该对象String型值，true时为"Y",false时为"Y"。
	 */
	public String toString() {
		return value ? "Y" : "N";
	}

	public int compareTo(Object o) {
		if(o == null)
			return 1;
		return toString().compareTo(o.toString());
	}

}