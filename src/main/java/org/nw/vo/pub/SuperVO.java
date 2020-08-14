package org.nw.vo.pub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.jackson.annotate.JsonIgnore;

/*
 * Generally, one SuperVO to one table. One table's field to one one viarable
 * of SuperVO.How to create map between table_field and supervo_viarable is
 * important for this class.Both the interfaces and variables'name rules are
 * all for to create this map. From NC3.0,this class is related to
 * UAP_DDC.Every variable should have access method.If variable has not own
 * special method and want to use general datatype access method,the field
 * property "isIndustrial" in DDC must be true.
 */

/**
 * <ul>
 * <li>子类书写说明：</li>
 * <li>变量格式：variablename ，其中 variablename 必须是小写格式；</li>
 * <li>所有对应数据库的字段的变量，variablename必须是 字段名称 的格式，例如：pk_corp,usename；</li>
 * <li>变量都必须是实体类变量，不能是原始类型；</li>
 * <li>所有对应数据库的字段的变量必须有public类型的setter和getter方法;</li>
 * </ul>
 * 
 * @支持继承结构
 */
public abstract class SuperVO extends org.nw.vo.pub.CircularlyAccessibleValueObject {
	private static final long serialVersionUID = 1386231098909087720L;

	private transient static Map<Class, String[]> map = new HashMap<Class, String[]>();

	private transient static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	/**
	 * SuperVO 构造子注解。
	 */
	public SuperVO() {
		super();
	}

	/**
	 * 根类Object的方法,克隆这个VO对象。 创建日期：(2001-3-14)
	 */
	public Object clone() {

		SuperVO vo = null;
		try {
			vo = (SuperVO) getClass().newInstance();
		} catch(Exception e) {

		}
		String[] fieldNames = getAttributeNames();
		if(fieldNames != null) {
			for(int i = 0; i < fieldNames.length; i++) {
				try {
					vo.setAttributeValue(fieldNames[i], getAttributeValue(fieldNames[i]));
				} catch(Exception ex) {
					continue;
				}
			}
		}
		vo.setDirty(isDirty());
		vo.setStatus(getStatus());
		return vo;

	}

	/**
	 * 创建日期：(2003-8-1 15:20:29)
	 * 
	 * @param obj
	 *            java.lang.Object
	 * @return boolean
	 */
	public boolean equalsContent(Object obj) {
		if(obj == this)
			return true;
		if(obj == null)
			return false;
		if(obj.getClass() != getClass())
			return false;
		return equalsContent((SuperVO) obj, getAttributeNames());
	}

	/**
	 * @return boolean
	 */
	public boolean equalsContent(SuperVO vo, String[] fieldnames) {
		if(fieldnames == null || vo == null)
			return false;

		for(String field : fieldnames) {
			if(!isAttributeEquals(getAttributeValue(field), vo.getAttributeValue(field)))
				return false;
		}
		return true;
	}

	/**
	 * @return java.lang.String[]
	 */
	@JsonIgnore
	public String[] getAttributeNames() {
		rwl.readLock().lock();
		try {
			return getAttributeAry();
		} finally {
			rwl.readLock().unlock();
		}
	}

	private String[] getAttributeAry() {
		String[] arys = map.get(this.getClass());
		if(arys == null) {
			rwl.readLock().unlock();
			rwl.writeLock().lock();
			try {
				arys = map.get(this.getClass());
				if(arys == null) {
					// List<String> al = new ArrayList<String>();
					Set<String> set = new HashSet<String>();
					String[] strAry = BeanHelper.getInstance().getPropertiesAry(this);
					for(String str : strAry) {
						if(getPKFieldName() != null && str.equals("primarykey")) {
							set.add(getPKFieldName());
						} else if(!(str.equals("status") || str.equals("dirty")))
							set.add(str);
					}
					arys = set.toArray(new String[set.size()]);
					map.put(this.getClass(), arys);
				}
			} finally {
				rwl.readLock().lock();
				rwl.writeLock().unlock();
			}
		}
		return arys;
	}

	/**
	 * <p>
	 * 根据一个属性名称字符串该属性的值。
	 * <p>
	 * 创建日期：(2002-11-6)
	 * 
	 * @param key
	 *            java.lang.String
	 */
	public Object getAttributeValue(String attributeName) {
		if(attributeName == null || attributeName.length() == 0)
			return null;
		if(getPKFieldName() != null && attributeName.equals(getPKFieldName()))
			attributeName = "primarykey";
		return BeanHelper.getProperty(this, attributeName);
	}

	/**
	 * This method used to VO with parent_child structure 创建日期：(2002-8-26
	 * 9:52:39)
	 * 
	 * @return primary key of parent
	 */
	public abstract String getParentPKFieldName();

	/**
	 * 创建日期：(2002-8-26 9:52:39)
	 * 
	 * @return primary key of the database_table
	 */
	public abstract String getPKFieldName();

	/**
	 * 创建日期：(2002-8-26 9:52:39)
	 * 
	 * @return tablename of the database_table
	 */
	public abstract String getTableName();

	/**
	 * 判断两个属性是否值相同，支持字符串、UFBoolean、Integer。 创建日期：(2002-11-15 16:27:32)
	 * 
	 * @param attrOld
	 *            java.lang.Object
	 * @param attrNew
	 *            java.lang.Object
	 * @return boolean
	 */
	private boolean isAttributeEquals(Object attrOld, Object attrNew) {
		if(attrOld == attrNew)
			return true;
		if(attrOld == null || attrNew == null) {
			return false;
		}
		return attrOld.equals(attrNew);
	}

	public static void main(String[] s) {
		System.exit(0);
	}

	/**
	 * 创建日期：(01-3-20 17:24:29)
	 */
	public void setAttributeValue(String attributeName, Object value) {
		if(attributeName == null || attributeName.length() == 0)
			return;
		if(getPKFieldName() != null && attributeName.equals(getPKFieldName()))
			attributeName = "primarykey";
		BeanHelper.setProperty(this, attributeName, value);
	}

	public void setPrimaryKey(String key) {
		if(getPKFieldName() == null)
			return;
		BeanHelper.setProperty(this, getPKFieldName().toLowerCase(), key);
	}

	@JsonIgnore
	public String getPrimaryKey() {
		if(getPKFieldName() == null)
			return null;
		return (String) BeanHelper.getProperty(this, getPKFieldName().toLowerCase());
	}

	/*
	 * If child need this method,please override it
	 * 
	 * @see nc.vo.pub.ValueObject#validate()
	 */
	public void validate() throws ValidationException {
	}

	/*
	 * If child need this method,please override it
	 * 
	 * @see nc.vo.pub.ValueObject#getEntityName()
	 */
	public String getEntityName() {
		return null;
	}
}