package org.nw.xml;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Reflection Cookbook<br/>
 * eg:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Reflection r = new Reflection(A.class);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Reflection r = new Reflection("com.shonetown.A");<br/>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Reflection {

	/**
	 * 类
	 */
	private Class clazz;

	/**
	 * 实例
	 */
	private Object object;

	/**
	 * default construct method
	 */
	public Reflection() {

	}

	/**
	 * 存储字段名-字段值
	 */
	Map map = new LinkedHashMap();

	/**
	 * 字段名-字段对象
	 */
	Map fieldMap = new LinkedHashMap();

	/**
	 * construct method
	 * 
	 * @param obj
	 */
	public Reflection(Object obj) {
		this.object = obj;
		clazz = obj.getClass();
	}

	public Reflection(Class clazz) {
		try {
			this.object = clazz.newInstance();
		} catch(Exception e) {
		}
		this.clazz = clazz;
	}

	/**
	 * construct method
	 * 
	 * @param className
	 * @throws Exception
	 */
	public Reflection(String className) throws Exception {
		if(className == null)
			clazz = null;
		else
			clazz = Class.forName(className);
		this.object = clazz.newInstance();
	}

	/**
	 * 得到类中所声明的所有属性,包括:public,protected,private,
	 * 
	 * @return List properties name in list
	 * @throws Exception
	 */
	public List getProperties() throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		if(fields == null || fields.length == 0)
			return null;
		List list = new ArrayList();
		AccessibleObject.setAccessible(fields, true);
		for(int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(!field.getName().equals("serialVersionUID"))
				list.add(field.getName());
		}
		if(list.size() < 1)
			return null;
		return list;
	}

	public Map getFieldsWithSuper(Class clazz) throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		for(int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(!field.getName().equals("serialVersionUID"))
				fieldMap.put(field.getName(), field);
		}
		clazz = clazz.getSuperclass();
		if(clazz == null)
			return fieldMap;
		return getFieldsWithSuper(clazz);
	}

	/**
	 * 得到类中所声明的所有属性,包括:public,protected,private,存储在Map中
	 * 
	 * @return Map 存储成员变量名-变量类型
	 * @throws Exception
	 */
	public Map getFieldsMap() throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		if(fields == null || fields.length == 0)
			return null;
		Map map = new LinkedHashMap();
		AccessibleObject.setAccessible(fields, true);
		for(int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(!field.getName().equals("serialVersionUID"))
				map.put(field.getName(), field.getType().getName());
		}
		if(map.size() < 1)
			return null;
		return map;
	}

	public Map getFieldsMapWithSuper(Class clazz) throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		for(int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(!field.getName().equals("serialVersionUID"))
				map.put(field.getName(), field.getType().getName());
		}
		clazz = clazz.getSuperclass();
		if(clazz == null)
			return map;
		return getFieldsMapWithSuper(clazz);
	}

	/**
	 * 根据名称查找类声明的所有属性,包括:public,protected,private
	 * 
	 * @param fieldName
	 * @return 该属性对象
	 * @throws Exception
	 * 
	 */
	public Object getProperty(String fieldName) throws Exception {
		Field field = clazz.getDeclaredField(fieldName);
		return field.get(object);
	}

	/**
	 * 得到类的静态公共属性
	 * 
	 * @param fieldName
	 *            属性名
	 * @return 该属性对象
	 * @throws Exception
	 */
	public Object getStaticProperty(String fieldName) throws Exception {
		Field field = clazz.getDeclaredField(fieldName);
		return field.get(clazz);
	}

	/**
	 * all public method include superclass and interface.
	 * 
	 * @return map
	 */
	public Map getMethods() {
		/**
		 * 方法名-方法对象
		 */
		Map methodMap = new LinkedHashMap();
		Method[] methods = clazz.getMethods();
		for(int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			methodMap.put(method.getName(), method);
		}
		return methodMap;
	}

	/**
	 * 执行对象方法 当所执行的函数的参数包含基本类型的包装类是，必须为所有参数定义其参数类型
	 * 
	 * @param methodName
	 *            方法名
	 * @param args
	 *            参数
	 * @param types
	 *            当函数的参数包含基本类型的包装类时，此参数包含所以参数的类型信息
	 * @return 方法返回值
	 * @throws Exception
	 */
	public Object invoke(String methodName, Object[] args, Class[] types) throws Exception {
		Class[] parameterTypes = null;
		if(types == null)
			parameterTypes = getParameterTypes(args);
		else
			parameterTypes = types;
		Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(object, args);
	}

	/**
	 * 执行类的静态方法 当所执行的函数的参数包含基本类型的包装类是，必须为所有参数定义其参数类型
	 * 
	 * @param methodName
	 *            方法名
	 * @param args
	 *            参数数组
	 * @param types
	 *            当函数的参数包含基本类型的包装类时，此参数包含所以参数的类型信息
	 * @return 执行方法返回的结果
	 * @throws Exception
	 */
	public Object invokeStaticMethod(String methodName, Object[] args, Class[] types) throws Exception {
		Class[] parameterTypes = null;
		if(types == null)
			parameterTypes = getParameterTypes(args);
		else
			parameterTypes = types;
		Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
		return method.invoke(null, args);
	}

	private Class[] getParameterTypes(Object[] args) throws Exception {
		if(args == null) {
			return null;
		}
		Class[] parameterTypes = new Class[args.length];
		for(int i = 0, j = args.length; i < j; i++) {
			if(args[i] instanceof Integer) {
				parameterTypes[i] = Integer.TYPE;
			} else if(args[i] instanceof Byte) {
				parameterTypes[i] = Byte.TYPE;
			} else if(args[i] instanceof Short) {
				parameterTypes[i] = Short.TYPE;
			} else if(args[i] instanceof Float) {
				parameterTypes[i] = Float.TYPE;
			} else if(args[i] instanceof Double) {
				parameterTypes[i] = Double.TYPE;
			} else if(args[i] instanceof Character) {
				parameterTypes[i] = Character.TYPE;
			} else if(args[i] instanceof Long) {
				parameterTypes[i] = Long.TYPE;
			} else if(args[i] instanceof Boolean) {
				parameterTypes[i] = Boolean.TYPE;
			} else {
				parameterTypes[i] = args[i].getClass();
			}
		}
		return parameterTypes;
	}

	/**
	 * 新建实例
	 * 
	 * @param args
	 *            构造函数的参数
	 * @return 新建的实例
	 * @throws Exception
	 */
	public Object newInstance(Object[] args) throws Exception {
		Class[] parameterTypes = getParameterTypes(args);
		Constructor cons = clazz.getConstructor(parameterTypes);
		return cons.newInstance(args);
	}

	/**
	 * 返回指定field的名称
	 * 
	 * @param field
	 * @return String
	 */
	public String getFieldName(Field field) {
		return field.getName();
	}

	/**
	 * 返回指定field的类型
	 * 
	 * @param field
	 * @return String
	 */
	public String getFieldType(Field field) {
		return field.getType().getName();
	}

	/**
	 * 是不是某个类的实例
	 * 
	 * @param obj
	 *            实例
	 * @param cls
	 *            类
	 * @return 如果 obj 是此类的实例，则返回 true
	 */
	public static boolean isInstance(Object obj, Class cls) {
		return cls.isInstance(obj);
	}

	/**
	 * 返回类的全称名称
	 * 
	 * @return String
	 */
	public String getName() {
		return clazz.getName();
	}

	/**
	 * 判断该类型是否是接口
	 * 
	 * @return boolean
	 */
	public boolean isInterface() {
		return clazz.isInterface();
	}

	/**
	 * @return the clazz
	 */
	public Class getClazz() {
		return clazz;
	}

	/**
	 * TODO
	 * 
	 * @param clazz
	 *            the clazz to set
	 */
	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * TODO
	 * 
	 * @param object
	 *            the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	public static boolean isWrapClass(Class clz) {
		try {
			return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
		} catch(Exception e) {
			return false;
		}
	}

}