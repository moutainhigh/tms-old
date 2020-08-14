package org.nw.vo.pub;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * <p>
 * 包装代表业务含义的一组数据，如凭证、科目等，负责在系统各层 之间传递业务数据。
 * <p>
 * 实体类同时必须实现每个需要持久化属性的setXXX和getXXX方法。 在set方法中对属性进行验证，验证失败抛出ValidationException异
 * 常。
 * <p>
 * 为每一个属性添加一个静态的属性描述对象（FieldObject），并 为每个FieldObject实现一个get方法，在get方法中实例化加入对应属
 * 性的描述信息。FieldObject中描述了对应属性的取值范围、名称、标签 等特征。
 * 
 * <p>
 * 
 */
public abstract class ValueObject implements Cloneable, java.io.Serializable {
	private static final long serialVersionUID = -2144910174446136305L;
	/** 判断此记录是否为脏 */
	private boolean m_isDirty = false;

	/**
	 * ValueObject 构造子注解。
	 */
	public ValueObject() {
		super();
	}

	/**
	 * 克隆一个完全相同的VO对象(前层复制)。
	 * 
	 * 创建日期：(2001-3-7 11:34:51)
	 * 
	 * @return nc.vo.pub.ValueObject
	 */
	public Object clone() {

		Object o = null;
		try {
			o = super.clone();
		} catch(CloneNotSupportedException e) {
			System.out.println("clone not supported!");
		}
		return o;
	}

	/**
	 * 返回数值对象的显示名称。
	 * 
	 * 创建日期：(2001-2-15 14:18:08)
	 * 
	 * @return java.lang.String 返回数值对象的显示名称。
	 */
	public abstract String getEntityName();

	/**
	 * 返回对象标识，用来唯一定位对象。
	 * 
	 * 创建日期：(2001-2-15 9:43:38)
	 * 
	 * @return nc.vo.pub.PrimaryKey
	 */
	public String getPrimaryKey() throws BusinessException {

		throw new BusinessException("Method getPrimaryKey() is not realized in class " + getClass().getName());
	}

	/**
	 * 处理异常。
	 * 
	 * 创建日期：(2001-2-28 10:33:37)
	 * 
	 * @param e
	 *            java.lang.Throwable
	 */
	protected static void handleException(Throwable e) {
		e.printStackTrace();
	}

	/**
	 * 是否为脏数据 创建日期：(2001-8-8 12:44:04)
	 * 
	 * @since V1.00
	 * @return boolean
	 */
	@JsonIgnore
	public boolean isDirty() {
		return m_isDirty;
	}

	/**
	 * 设置是否为脏数据 创建日期：(2001-8-8 12:43:23)
	 * 
	 * @since V1.00
	 * @param isDirty
	 *            boolean
	 */
	public void setDirty(boolean isDirty) {
		m_isDirty = isDirty;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-26 9:05:32)
	 * 
	 * @param key
	 *            java.lang.String
	 */
	public void setPrimaryKey(String key) throws BusinessException {

		throw new BusinessException("Method setPrimaryKey() is not realized in class " + getClass().getName());
	}

	/**
	 * 验证对象各属性之间的数据逻辑正确性。
	 * 
	 * 创建日期：(2001-2-15 11:47:35)
	 * 
	 * @exception org.nw.vo.pub.ValidationException
	 *                如果验证失败，抛出 ValidationException，对错误进行解释。
	 */
	public abstract void validate() throws ValidationException;
}
