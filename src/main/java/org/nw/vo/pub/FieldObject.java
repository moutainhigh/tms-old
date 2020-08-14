package org.nw.vo.pub;

/**
 * 描述实体域的属性
 * 
 * @version 2000/5/20
 */
public abstract class FieldObject {
	private String m_strLabel = null; // 属性标签（字段说明）
	private String m_strName = null; // 属性名称
	private boolean m_bAllowNull = true;

	/**
	 * FieldObject 构造子注释。
	 */
	public FieldObject() {
		super();
	}

	/**
	 * 子类返回域对象描述的类的类型。
	 *
	 * @return java.lang.Class
	 */
	public abstract Class getFieldType();

	/**
	 * 返回域的显示标签，这通常用于Label控件的Text属性，以达到字段名称的统一显示。 对数据库字段，这是该字段的说明。
	 *
	 * @return java.lang.String
	 */
	public java.lang.String getLabel() {
		return m_strLabel;
	}

	/**
	 * 域对象所描述的属性的名称。
	 *
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return m_strName;
	}

	/**
	 * 返回实体对象在该域上对应的值。
	 *
	 * @return java.lang.Object
	 * @param eo
	 *            ierp.pub.baseobj.EntityObject
	 */
	public Object getValue(ValueObject eo) {
		Object val = null;
		try {
			String strMethod = "get" + getName();
			val = eo.getClass().getMethod(strMethod, new Class[0]).invoke(eo, new Object[0]);
		} catch(Throwable e) {
			handleException(e);
		}
		return val;
	}

	/**
 *
 */
	protected void handleException(Throwable exception) {
		exception.printStackTrace();
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-4-17 9:42:46)
	 * 
	 * @return boolean
	 */
	public boolean isAllowNull() {
		return m_bAllowNull;
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-4-17 9:42:46)
	 * 
	 * @param newAllowNull
	 *            boolean
	 */
	public void setAllowNull(boolean newAllowNull) {
		m_bAllowNull = newAllowNull;
	}

	/**
	 * @param newvalue
	 *            java.lang.String
	 * @see getLabel
	 */
	public void setLabel(java.lang.String newvalue) {
		m_strLabel = newvalue;
	}

	/**
	 * @param newvalue
	 *            java.lang.String
	 * @see getName
	 */
	public void setName(java.lang.String newvalue) {
		m_strName = newvalue;
	}

	/**
	 * 设置实体对象在该域上对应的属性值。
	 * 
	 * @param eo
	 *            ierp.pub.baseobj.EntityObject
	 * @param val
	 *            java.lang.Object
	 * @exception ierp.pub.baseobj.ValidateException
	 *                异常描述。
	 */
	public void setValue(ValueObject eo, Object val) throws ValidationException {
		String strMethod = "set" + getName();
		Class[] aryParamTypes = new Class[1];
		Object[] aryParams = new Object[1];

		try {
			aryParamTypes[0] = getFieldType();
			aryParams[0] = val;
			eo.getClass().getMethod(strMethod, aryParamTypes).invoke(eo, aryParams);
		} catch(NoSuchMethodException nsme) {
			handleException(nsme);
		} catch(IllegalAccessException iae) {
			handleException(iae);
		} catch(java.lang.reflect.InvocationTargetException ite) {
			if(ite.getTargetException() instanceof ValidationException) {
				throw (ValidationException) ite.getTargetException();
			} else {
				handleException(ite);
			}
		}
	}

	/**
	 * 验证属性值的正确性。
	 * 
	 * @param o
	 *            java.lang.Object
	 */
	public abstract boolean validate(Object o) throws ValidationException;
}
