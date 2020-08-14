package org.nw.basic.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 类反射工具类 继承自spring
 * 
 * @author fangw
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ReflectionUtils {
	private static final Log log = LogFactory.getLog(ReflectionUtils.class);

	/**
	 * 取得参数对象中的公共属性
	 * 
	 * @param obj
	 * @param fieldname
	 * @return
	 * @throws Exception
	 */
	public static Object getProperty(Object obj, String fieldname) throws Exception {
		Object result = null;
		Class objClass = obj.getClass();
		Field field = objClass.getField(fieldname);
		result = field.get(obj);
		return result;
	}

	/**
	 * 获得某类的静态属性
	 * 
	 * @param className
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public static Object getStaticProperty(String className, String fieldName) throws Exception {
		Class cls = Class.forName(className);
		Field field = cls.getField(fieldName);
		Object provalue = field.get(cls);
		return provalue;
	}

	/**
	 * 获取参数对象的属性值
	 * 
	 * @param obj
	 * @param propertyName
	 * @return
	 * @throws Exception
	 */
	public static Object getPrivatePropertyValue(Object obj, String propertyName) throws Exception {
		Class cls = obj.getClass();
		Field field = cls.getDeclaredField(propertyName);
		field.setAccessible(true);
		Object retvalue = field.get(obj);
		return retvalue;
	}

	/**
	 * 执行某对象的方法
	 * 
	 * @param owner
	 * @param methodName
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static Object invokeMethod(Object owner, String methodName) throws Exception {
		Class cls = owner.getClass();
		Method method = cls.getMethod(methodName);
		return method.invoke(owner);
	}

	/**
	 * 获得一个类的示例，即创建一个对象<br>
	 * 调用示例：<br>
	 * Object vpp=reflection.newInstance("test.web.example.PP",new
	 * Object[]{"peidw","广州"});
	 * 
	 * @param className
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static Object newInstance(String className, Object[] args) throws Exception {
		Class clss = Class.forName(className);

		Class[] argclass = new Class[args.length];
		for(int i = 0, j = argclass.length; i < j; i++) {
			argclass[i] = args[i].getClass();
		}
		java.lang.reflect.Constructor cons = clss.getConstructor(argclass);
		return cons.newInstance(args);
	}

	/**
	 * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
	 */
	public static Object getFieldValue(final Object object, final String fieldName) {
		Field field = getDeclaredField(object, fieldName);

		if(field == null) {
			// FIXME XUQC 2013-3-20
			return null;
			// throw new IllegalArgumentException("Could not find field [" +
			// fieldName + "] on target [" + object + "]");
		}

		makeAccessible(field);

		Object result = null;
		try {
			result = field.get(object);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数.
	 */
	public static void setFieldValue(final Object object, final String fieldName, final Object value) {
		Field field = getDeclaredField(object, fieldName);

		if(field == null) {
			// FIXME XUQC 2013-3-20
			return;
			// throw new IllegalArgumentException("Could not find field [" +
			// fieldName + "] on target [" + object + "]");
		}

		makeAccessible(field);

		try {
			field.set(object, value);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 直接调用对象方法, 支持private/protected修饰符的方法（这里强制设置为可访问）
	 * 注：这里虽然parameterTypes可以通过parameters来获取到，但并不完全准确，
	 * 特别是像boolean和Boolean这种类型时会出错，所以这里强制让调用者指定
	 */
	public static Object invokeMethod(final Object object, final String methodName, final Class<?>[] parameterTypes,
			final Object[] parameters) {
		Method method = getDeclaredMethod(object, methodName, parameterTypes);
		if(method == null) {
			throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");
		}

		method.setAccessible(true);

		try {
			return method.invoke(object, parameters);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 执行静态类的方法
	 * 
	 * @param className
	 * @param methodName
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static Object invokeStaticMethod(String className, String methodName, Object[] args) {
		try {
			Class cls = Class.forName(className);
			return invokeStaticMethod(cls, methodName, args);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object invokeStaticMethod(Class clazz, String methodName, Object[] args) {
		try {
			Class[] argclass = new Class[args.length];
			for(int i = 0, j = argclass.length; i < j; i++) {
				argclass[i] = args[i].getClass();
			}
			Method method = clazz.getMethod(methodName, argclass);
			return method.invoke(null, args);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object invokeStaticMethod(String className, String methodName) {
		try {
			Class cls = Class.forName(className);
			return invokeStaticMethod(cls, methodName);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object invokeStaticMethod(Class clazz, String methodName) {
		try {
			Method method = clazz.getMethod(methodName);
			return method.invoke(null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 循环向上转型, 获取对象的DeclaredField. 如向上转型到Object仍无法找到, 返回null.
	 */
	public static Field getDeclaredField(final Object object, final String fieldName) {
		Assert.notNull(object, "object不能为空");
		Assert.hasText(fieldName, "fieldName");
		for(Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch(NoSuchFieldException e) {
				// Field不在当前类定义,继续向上转型
			}
		}
		return null;
	}

	/**
	 * 强行设置Field可访问.
	 */
	public static void makeAccessible(final Field field) {
		if(!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	/**
	 * 循环向上转型,获取对象的DeclaredMethod. 如向上转型到Object仍无法找到, 返回null.
	 */
	public static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {
		Assert.notNull(object, "object不能为空");

		for(Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				return superClass.getDeclaredMethod(methodName, parameterTypes);
			} catch(NoSuchMethodException e) {
				// Method不在当前类定义,继续向上转型
			}
		}
		return null;
	}

	public static Method getDeclaredMethod(Object object, String methodName) {
		Assert.notNull(object, "object不能为空");

		for(Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				return superClass.getDeclaredMethod(methodName);
			} catch(NoSuchMethodException e) {
				// Method不在当前类定义,继续向上转型
			}
		}
		return null;
	}

	/**
	 * 通过反射,获得Class定义中声明的父类的泛型参数的类型. 如无法找到, 返回Object.class. eg. public UserDao
	 * extends HibernateDao<User>
	 * 
	 * @param clazz
	 *            The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be
	 *         determined
	 */
	public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
		return getSuperClassGenricType(clazz, 0);
	}

	/**
	 * 通过反射,获得定义Class时声明的父类的泛型参数的类型. 如无法找到, 返回Object.class. 如public UserDao
	 * extends HibernateDao<User,Long>
	 * 
	 * @param clazz
	 *            clazz The class to introspect
	 * @param index
	 *            the Index of the generic ddeclaration,start from 0.
	 * @return the index generic declaration, or Object.class if cannot be
	 *         determined
	 */
	public static Class getSuperClassGenricType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();

		if(!(genType instanceof ParameterizedType)) {
			log.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if(index >= params.length || index < 0) {
			log.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		if(!(params[index] instanceof Class)) {
			log.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}

	/**
	 * 提取集合中的对象的属性(通过getter函数), 组合成List.
	 * 
	 * @param collection
	 *            来源集合.
	 * @param propertyName
	 *            要提取的属性名.
	 */
	// public static List convertElementPropertyToList(final Collection
	// collection, final String propertyName) {
	// List list = new ArrayList();
	//
	// try {
	// for(Object obj : collection) {
	// list.add(PropertyUtils.getProperty(obj, propertyName));
	// }
	// } catch(Exception e) {
	// throw new RuntimeException(e);
	// }
	//
	// return list;
	// }

	/**
	 * 提取集合中的对象的属性(通过getter函数), 组合成由分割符分隔的字符串.
	 * 
	 * @param collection
	 *            来源集合.
	 * @param propertyName
	 *            要提取的属性名.
	 * @param separator
	 *            分隔符.
	 */
	// public static String convertElementPropertyToString(final Collection
	// collection, final String propertyName,
	// final String separator) {
	// List list = convertElementPropertyToList(collection, propertyName);
	// return StringUtils.join(list, separator);
	// }

	/**
	 * 转换字符串类型到clazz的property类型的值.
	 * 
	 * @param value
	 *            待转换的字符串
	 * @param clazz
	 *            提供类型信息的Class
	 * @param propertyName
	 *            提供类型信息的Class的属性.
	 */
	// public static Object convertValue(Object value, Class<?> toType) {
	// try {
	// DateConverter dc = new DateConverter();
	// dc.setUseLocaleFormat(true);
	// dc.setPatterns(new String[] { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss" });
	// ConvertUtils.register(dc, Date.class);
	// return ConvertUtils.convert(value, toType);
	// } catch(Exception e) {
	// throw new RuntimeException(e);
	// }
	// }

	/**
	 * Get all declared fields on the leaf class and all superclasses. Leaf
	 * class fields are included first.
	 */
	public static Field[] getAllDeclaredFields(Class leafClass) throws IllegalArgumentException {
		final List list = new ArrayList(32);
		doWithFields(leafClass, new FieldCallback() {
			public void doWith(Field field) {
				list.add(field);
			}
		});
		return (Field[]) list.toArray(new Field[list.size()]);
	}

	/**
	 * Get all declared fields on the leaf class and all superclasses. Leaf
	 * class fields are included first.
	 */
	public static Field[] getAllDeclaredFields(Class leafClass, FieldFilter ff) throws IllegalArgumentException {
		final List list = new ArrayList(32);
		doWithFields(leafClass, new FieldCallback() {
			public void doWith(Field field) {
				list.add(field);
			}
		}, ff);
		return (Field[]) list.toArray(new Field[list.size()]);
	}

	/**
	 * Attempt to find a {@link Field field} on the supplied {@link Class} with
	 * the supplied <code>name</code>. Searches all superclasses up to
	 * {@link Object}.
	 * 
	 * @param clazz
	 *            the class to introspect
	 * @param name
	 *            the name of the field
	 * @return the corresponding Field object, or <code>null</code> if not found
	 */
	public static Field findField(Class<?> clazz, String name) {
		return findField(clazz, name, null);
	}

	/**
	 * Attempt to find a {@link Field field} on the supplied {@link Class} with
	 * the supplied <code>name</code> and/or {@link Class type}. Searches all
	 * superclasses up to {@link Object}.
	 * 
	 * @param clazz
	 *            the class to introspect
	 * @param name
	 *            the name of the field (may be <code>null</code> if type is
	 *            specified)
	 * @param type
	 *            the type of the field (may be <code>null</code> if name is
	 *            specified)
	 * @return the corresponding Field object, or <code>null</code> if not found
	 */
	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.isTrue(name != null || type != null, "Either name or type of the field must be specified");
		Class<?> searchType = clazz;
		while(!Object.class.equals(searchType) && searchType != null) {
			Field[] fields = searchType.getDeclaredFields();
			for(Field field : fields) {
				if((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	/**
	 * Set the field represented by the supplied {@link Field field object} on
	 * the specified {@link Object target object} to the specified
	 * <code>value</code>. In accordance with {@link Field#set(Object, Object)}
	 * semantics, the new value is automatically unwrapped if the underlying
	 * field has a primitive type.
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException(Exception)}.
	 * 
	 * @param field
	 *            the field to set
	 * @param target
	 *            the target object on which to set the field
	 * @param value
	 *            the value to set; may be <code>null</code>
	 */
	public static void setField(Field field, Object target, Object value) {
		try {
			field.set(target, value);
		} catch(IllegalAccessException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
					+ ex.getMessage());
		}
	}

	/**
	 * Get the field represented by the supplied {@link Field field object} on
	 * the specified {@link Object target object}. In accordance with
	 * {@link Field#get(Object)} semantics, the returned value is automatically
	 * wrapped if the underlying field has a primitive type.
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException(Exception)}.
	 * 
	 * @param field
	 *            the field to get
	 * @param target
	 *            the target object from which to get the field
	 * @return the field's current value
	 */
	public static Object getField(Field field, Object target) {
		try {
			return field.get(target);
		} catch(IllegalAccessException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
					+ ex.getMessage());
		}
	}

	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied
	 * name and no parameters. Searches all superclasses up to
	 * <code>Object</code>.
	 * <p>
	 * Returns <code>null</code> if no {@link Method} can be found.
	 * 
	 * @param clazz
	 *            the class to introspect
	 * @param name
	 *            the name of the method
	 * @return the Method object, or <code>null</code> if none found
	 */
	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, new Class[0]);
	}

	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied
	 * name and parameter types. Searches all superclasses up to
	 * <code>Object</code>.
	 * <p>
	 * Returns <code>null</code> if no {@link Method} can be found.
	 * 
	 * @param clazz
	 *            the class to introspect
	 * @param name
	 *            the name of the method
	 * @param paramTypes
	 *            the parameter types of the method (may be <code>null</code> to
	 *            indicate any signature)
	 * @return the Method object, or <code>null</code> if none found
	 */
	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(name, "Method name must not be null");
		Class<?> searchType = clazz;
		while(searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for(Method method : methods) {
				if(name.equals(method.getName())
						&& (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with no arguments. The target object can be <code>null</code> when
	 * invoking a static {@link Method}.
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException}.
	 * 
	 * @param method
	 *            the method to invoke
	 * @param target
	 *            the target object to invoke the method on
	 * @return the invocation result, if any
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, new Object[0]);
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with the supplied arguments. The target object can be <code>null</code>
	 * when invoking a static {@link Method}.
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException}.
	 * 
	 * @param method
	 *            the method to invoke
	 * @param target
	 *            the target object to invoke the method on
	 * @param args
	 *            the invocation arguments (may be <code>null</code>)
	 * @return the invocation result, if any
	 */
	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			return method.invoke(target, args);
		} catch(Exception ex) {
			handleReflectionException(ex);
		}
		throw new IllegalStateException("Should never get here");
	}

	/**
	 * Invoke the specified JDBC API {@link Method} against the supplied target
	 * object with no arguments.
	 * 
	 * @param method
	 *            the method to invoke
	 * @param target
	 *            the target object to invoke the method on
	 * @return the invocation result, if any
	 * @throws SQLException
	 *             the JDBC API SQLException to rethrow (if any)
	 * @see #invokeJdbcMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeJdbcMethod(Method method, Object target) throws SQLException {
		return invokeJdbcMethod(method, target, new Object[0]);
	}

	/**
	 * Invoke the specified JDBC API {@link Method} against the supplied target
	 * object with the supplied arguments.
	 * 
	 * @param method
	 *            the method to invoke
	 * @param target
	 *            the target object to invoke the method on
	 * @param args
	 *            the invocation arguments (may be <code>null</code>)
	 * @return the invocation result, if any
	 * @throws SQLException
	 *             the JDBC API SQLException to rethrow (if any)
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeJdbcMethod(Method method, Object target, Object... args) throws SQLException {
		try {
			return method.invoke(target, args);
		} catch(IllegalAccessException ex) {
			handleReflectionException(ex);
		} catch(InvocationTargetException ex) {
			if(ex.getTargetException() instanceof SQLException) {
				throw (SQLException) ex.getTargetException();
			}
			handleInvocationTargetException(ex);
		}
		throw new IllegalStateException("Should never get here");
	}

	/**
	 * Handle the given reflection exception. Should only be called if no
	 * checked exception is expected to be thrown by the target method.
	 * <p>
	 * Throws the underlying RuntimeException or Error in case of an
	 * InvocationTargetException with such a root cause. Throws an
	 * IllegalStateException with an appropriate message else.
	 * 
	 * @param ex
	 *            the reflection exception to handle
	 */
	public static void handleReflectionException(Exception ex) {
		if(ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		if(ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		}
		if(ex instanceof InvocationTargetException) {
			handleInvocationTargetException((InvocationTargetException) ex);
		}
		if(ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		handleUnexpectedException(ex);
	}

	/**
	 * Handle the given invocation target exception. Should only be called if no
	 * checked exception is expected to be thrown by the target method.
	 * <p>
	 * Throws the underlying RuntimeException or Error in case of such a root
	 * cause. Throws an IllegalStateException else.
	 * 
	 * @param ex
	 *            the invocation target exception to handle
	 */
	public static void handleInvocationTargetException(InvocationTargetException ex) {
		rethrowRuntimeException(ex.getTargetException());
	}

	/**
	 * Rethrow the given {@link Throwable exception}, which is presumably the
	 * <em>target exception</em> of an {@link InvocationTargetException}. Should
	 * only be called if no checked exception is expected to be thrown by the
	 * target method.
	 * <p>
	 * Rethrows the underlying exception cast to an {@link RuntimeException} or
	 * {@link Error} if appropriate; otherwise, throws an
	 * {@link IllegalStateException}.
	 * 
	 * @param ex
	 *            the exception to rethrow
	 * @throws RuntimeException
	 *             the rethrown exception
	 */
	public static void rethrowRuntimeException(Throwable ex) {
		if(ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		if(ex instanceof Error) {
			throw (Error) ex;
		}
		handleUnexpectedException(ex);
	}

	/**
	 * Rethrow the given {@link Throwable exception}, which is presumably the
	 * <em>target exception</em> of an {@link InvocationTargetException}. Should
	 * only be called if no checked exception is expected to be thrown by the
	 * target method.
	 * <p>
	 * Rethrows the underlying exception cast to an {@link Exception} or
	 * {@link Error} if appropriate; otherwise, throws an
	 * {@link IllegalStateException}.
	 * 
	 * @param ex
	 *            the exception to rethrow
	 * @throws Exception
	 *             the rethrown exception (in case of a checked exception)
	 */
	public static void rethrowException(Throwable ex) throws Exception {
		if(ex instanceof Exception) {
			throw (Exception) ex;
		}
		if(ex instanceof Error) {
			throw (Error) ex;
		}
		handleUnexpectedException(ex);
	}

	/**
	 * Throws an IllegalStateException with the given exception as root cause.
	 * 
	 * @param ex
	 *            the unexpected exception
	 */
	private static void handleUnexpectedException(Throwable ex) {
		throw new IllegalStateException("Unexpected exception thrown", ex);
	}

	/**
	 * Determine whether the given method explicitly declares the given
	 * exception or one of its superclasses, which means that an exception of
	 * that type can be propagated as-is within a reflective invocation.
	 * 
	 * @param method
	 *            the declaring method
	 * @param exceptionType
	 *            the exception to throw
	 * @return <code>true</code> if the exception can be thrown as-is;
	 *         <code>false</code> if it needs to be wrapped
	 */
	public static boolean declaresException(Method method, Class<?> exceptionType) {
		Assert.notNull(method, "Method must not be null");
		Class<?>[] declaredExceptions = method.getExceptionTypes();
		for(Class<?> declaredException : declaredExceptions) {
			if(declaredException.isAssignableFrom(exceptionType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine whether the given field is a "public static final" constant.
	 * 
	 * @param field
	 *            the field to check
	 */
	public static boolean isPublicStaticFinal(Field field) {
		int modifiers = field.getModifiers();
		return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
	}

	/**
	 * Determine whether the given method is an "equals" method.
	 * 
	 * @see java.lang.Object#equals(Object)
	 */
	public static boolean isEqualsMethod(Method method) {
		if(method == null || !method.getName().equals("equals")) {
			return false;
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		return (paramTypes.length == 1 && paramTypes[0] == Object.class);
	}

	/**
	 * Determine whether the given method is a "hashCode" method.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
	}

	/**
	 * Determine whether the given method is a "toString" method.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
	}

	/**
	 * Make the given method accessible, explicitly setting it accessible if
	 * necessary. The <code>setAccessible(true)</code> method is only called
	 * when actually necessary, to avoid unnecessary conflicts with a JVM
	 * SecurityManager (if active).
	 * 
	 * @param method
	 *            the method to make accessible
	 * @see java.lang.reflect.Method#setAccessible
	 */
	public static void makeAccessible(Method method) {
		if((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
				&& !method.isAccessible()) {
			method.setAccessible(true);
		}
	}

	/**
	 * Make the given constructor accessible, explicitly setting it accessible
	 * if necessary. The <code>setAccessible(true)</code> method is only called
	 * when actually necessary, to avoid unnecessary conflicts with a JVM
	 * SecurityManager (if active).
	 * 
	 * @param ctor
	 *            the constructor to make accessible
	 * @see java.lang.reflect.Constructor#setAccessible
	 */
	public static void makeAccessible(Constructor<?> ctor) {
		if((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))
				&& !ctor.isAccessible()) {
			ctor.setAccessible(true);
		}
	}

	/**
	 * Perform the given callback operation on all matching methods of the given
	 * class and superclasses.
	 * <p>
	 * The same named method occurring on subclass and superclass will appear
	 * twice, unless excluded by a {@link MethodFilter}.
	 * 
	 * @param clazz
	 *            class to start looking at
	 * @param mc
	 *            the callback to invoke for each method
	 * @see #doWithMethods(Class, MethodCallback, MethodFilter)
	 */
	public static void doWithMethods(Class<?> clazz, MethodCallback mc) throws IllegalArgumentException {
		doWithMethods(clazz, mc, null);
	}

	/**
	 * Perform the given callback operation on all matching methods of the given
	 * class and superclasses (or given interface and super-interfaces).
	 * <p>
	 * The same named method occurring on subclass and superclass will appear
	 * twice, unless excluded by the specified {@link MethodFilter}.
	 * 
	 * @param clazz
	 *            class to start looking at
	 * @param mc
	 *            the callback to invoke for each method
	 * @param mf
	 *            the filter that determines the methods to apply the callback
	 *            to
	 */
	public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf)
			throws IllegalArgumentException {

		// Keep backing up the inheritance hierarchy.
		Method[] methods = clazz.getDeclaredMethods();
		for(Method method : methods) {
			if(mf != null && !mf.matches(method)) {
				continue;
			}
			try {
				mc.doWith(method);
			} catch(IllegalAccessException ex) {
				throw new IllegalStateException("Shouldn't be illegal to access method '" + method.getName() + "': "
						+ ex);
			}
		}
		if(clazz.getSuperclass() != null) {
			doWithMethods(clazz.getSuperclass(), mc, mf);
		} else if(clazz.isInterface()) {
			for(Class<?> superIfc : clazz.getInterfaces()) {
				doWithMethods(superIfc, mc, mf);
			}
		}
	}

	/**
	 * Get all declared methods on the leaf class and all superclasses. Leaf
	 * class methods are included first.
	 */
	public static Method[] getAllDeclaredMethods(Class<?> leafClass) throws IllegalArgumentException {
		final List<Method> methods = new ArrayList<Method>(32);
		doWithMethods(leafClass, new MethodCallback() {
			public void doWith(Method method) {
				methods.add(method);
			}
		});
		return methods.toArray(new Method[methods.size()]);
	}

	/**
	 * Invoke the given callback on all fields in the target class, going up the
	 * class hierarchy to get all declared fields.
	 * 
	 * @param clazz
	 *            the target class to analyze
	 * @param fc
	 *            the callback to invoke for each field
	 */
	public static void doWithFields(Class<?> clazz, FieldCallback fc) throws IllegalArgumentException {
		doWithFields(clazz, fc, null);
	}

	/**
	 * Invoke the given callback on all fields in the target class, going up the
	 * class hierarchy to get all declared fields.
	 * 
	 * @param clazz
	 *            the target class to analyze
	 * @param fc
	 *            the callback to invoke for each field
	 * @param ff
	 *            the filter that determines the fields to apply the callback to
	 */
	public static void doWithFields(Class<?> clazz, FieldCallback fc, FieldFilter ff) throws IllegalArgumentException {

		// Keep backing up the inheritance hierarchy.
		Class<?> targetClass = clazz;
		do {
			Field[] fields = targetClass.getDeclaredFields();
			for(Field field : fields) {
				// Skip static and final fields.
				if(ff != null && !ff.matches(field)) {
					continue;
				}
				try {
					fc.doWith(field);
				} catch(IllegalAccessException ex) {
					throw new IllegalStateException("Shouldn't be illegal to access field '" + field.getName() + "': "
							+ ex);
				}
			}
			targetClass = targetClass.getSuperclass();
		} while(targetClass != null && targetClass != Object.class);
	}

	/**
	 * Given the source object and the destination, which must be the same class
	 * or a subclass, copy all fields, including inherited fields. Designed to
	 * work on objects with public no-arg constructors.
	 * 
	 * @throws IllegalArgumentException
	 *             if the arguments are incompatible
	 */
	public static void shallowCopyFieldState(final Object src, final Object dest) throws IllegalArgumentException {
		if(src == null) {
			throw new IllegalArgumentException("Source for field copy cannot be null");
		}
		if(dest == null) {
			throw new IllegalArgumentException("Destination for field copy cannot be null");
		}
		if(!src.getClass().isAssignableFrom(dest.getClass())) {
			throw new IllegalArgumentException("Destination class [" + dest.getClass().getName()
					+ "] must be same or subclass as source class [" + src.getClass().getName() + "]");
		}
		doWithFields(src.getClass(), new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				makeAccessible(field);
				Object srcValue = field.get(src);
				field.set(dest, srcValue);
			}
		}, COPYABLE_FIELDS);
	}

	/**
	 * Action to take on each method.
	 */
	public interface MethodCallback {

		/**
		 * Perform an operation using the given method.
		 * 
		 * @param method
		 *            the method to operate on
		 */
		void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
	}

	/**
	 * Callback optionally used to method fields to be operated on by a method
	 * callback.
	 */
	public interface MethodFilter {

		/**
		 * Determine whether the given method matches.
		 * 
		 * @param method
		 *            the method to check
		 */
		boolean matches(Method method);
	}

	/**
	 * Callback interface invoked on each field in the hierarchy.
	 */
	public interface FieldCallback {

		/**
		 * Perform an operation using the given field.
		 * 
		 * @param field
		 *            the field to operate on
		 */
		void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
	}

	/**
	 * Callback optionally used to filter fields to be operated on by a field
	 * callback.
	 */
	public interface FieldFilter {

		/**
		 * Determine whether the given field matches.
		 * 
		 * @param field
		 *            the field to check
		 */
		boolean matches(Field field);
	}

	/**
	 * Pre-built FieldFilter that matches all non-static, non-final fields.
	 */
	public static FieldFilter COPYABLE_FIELDS = new FieldFilter() {

		public boolean matches(Field field) {
			return !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()));
		}
	};

	/**
	 * Pre-built MethodFilter that matches all non-bridge methods.
	 */
	public static MethodFilter NON_BRIDGED_METHODS = new MethodFilter() {

		public boolean matches(Method method) {
			return !method.isBridge();
		}
	};

	/**
	 * Pre-built MethodFilter that matches all non-bridge methods which are not
	 * declared on <code>java.lang.Object</code>.
	 */
	public static MethodFilter USER_DECLARED_METHODS = new MethodFilter() {

		public boolean matches(Method method) {
			return (!method.isBridge() && method.getDeclaringClass() != Object.class);
		}
	};

}
