/*
 * �������� 2005-9-15
 *
 * 
 * ���� �� ��ѡ�� �� Java �� ������ʽ �� ����ģ��
 */
package org.nw.vo.pub;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 
 * @nopublish
 * 
 */
public class BeanHelper {
	protected static final Object[] NULL_ARGUMENTS = {};

	private static Map<String, Map<Integer, Map<String, Method>>> cache = new HashMap<String, Map<Integer, Map<String, Method>>>();

	private static BeanHelper bhelp = new BeanHelper();

	private final static int GETID = 0;

	private final static int SETID = 1;

	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	public static BeanHelper getInstance() {
		return bhelp;
	}

	private BeanHelper() {
	}

	protected static PropertyDescriptor[] getPropertyDescriptor(Class beanCls) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(beanCls);
			return beanInfo.getPropertyDescriptors();
		} catch(IntrospectionException e) {
			throw new RuntimeException("Failed to instrospect bean: " + beanCls, e);
		}
	}

	public static List<String> getPropertys(Object bean) {
		return Arrays.asList(getInstance().getPropertiesAry(bean));

	}

	public String[] getPropertiesAry(Object bean) {
		Map<Integer, Map<String, Method>> cMethod = null;
		rwl.readLock().lock();
		try {
			cMethod = cacheMethod(bean.getClass());
		} finally {
			rwl.readLock().unlock();
		}
		String[] retProps = cMethod.get(SETID).keySet().toArray(new String[0]);

		return retProps;
	}

	public static Object getProperty(Object bean, String propertyName) {

		try {
			Method method = getInstance().getMethod(bean, propertyName, false);
			if(method == null)
				return null;
			return method.invoke(bean, NULL_ARGUMENTS);
		} catch(Exception e) {
			String errStr = "Failed to get property: " + propertyName;
			throw new RuntimeException(errStr, e);
		}
	}

	public static Method getMethod(Object bean, String propertyName) {
		return getInstance().getMethod(bean, propertyName, true);
	}

	private Method getMethod(Object bean, String propertyName, boolean isSetMethod) {

		Method method = null;
		rwl.readLock().lock();
		Map<Integer, Map<String, Method>> cMethod = null;
		try {
			cMethod = cacheMethod(bean.getClass());
		} finally {
			rwl.readLock().unlock();
		}
		if(isSetMethod)
			method = cMethod.get(SETID).get(propertyName);
		else
			method = cMethod.get(GETID).get(propertyName);

		return method;
	}

	private Map<Integer, Map<String, Method>> cacheMethod(Class beanCls) {
		String key = beanCls.getName();
		Map<Integer, Map<String, Method>> cMethod = cache.get(key);
		if(cMethod == null) {
			rwl.readLock().unlock();
			rwl.writeLock().lock();
			try {
				cMethod = cache.get(key);
				if(cMethod == null) {
					cMethod = new HashMap<Integer, Map<String, Method>>();
					Map<String, Method> getMap = new HashMap<String, Method>();
					Map<String, Method> setMap = new HashMap<String, Method>();
					cMethod.put(GETID, getMap);
					cMethod.put(SETID, setMap);
					cache.put(key, cMethod);
					PropertyDescriptor[] pdescriptor = getPropertyDescriptor(beanCls);
					for(PropertyDescriptor pd : pdescriptor) {
						if(pd.getReadMethod() != null)
							getMap.put(pd.getName().toLowerCase(), pd.getReadMethod());
						if(pd.getWriteMethod() != null)
							setMap.put(pd.getName().toLowerCase(), pd.getWriteMethod());
					}
				}
			} finally {
				rwl.readLock().lock();
				rwl.writeLock().unlock();
			}
		}
		return cMethod;
	}

	public static void invokeMethod(Object bean, Method method, Object value) {
		try {
			if(method == null)
				return;
			Object[] arguments = { value };
			method.invoke(bean, arguments);
		} catch(Exception e) {
			String errStr = "Failed to set property: " + method.getName();
			throw new RuntimeException(errStr, e);
		}
	}

	public static void setProperty(Object bean, String propertyName, Object value) {
		try {
			Method method = getInstance().getMethod(bean, propertyName, true);
			if(method == null)
				return;
			Object[] arguments = { value };
			method.invoke(bean, arguments);
		} catch(Exception e) {
			String errStr = "Failed to set property: " + propertyName + " on bean: " + bean.getClass().getName()
					+ " with value:" + value;
			throw new RuntimeException(errStr, e);
		}
	}

	/*
	 * ��������get�ķ���
	 */
	public Method[] getAllGetMethod(Class beanCls, String[] fieldNames) {

		Method[] methods = null;
		Map<Integer, Map<String, Method>> cMethod = null;
		List<Method> al = new ArrayList<Method>();
		rwl.readLock().lock();
		try {
			cMethod = cacheMethod(beanCls);
		} finally {
			rwl.readLock().unlock();
		}
		Map<String, Method> map = cMethod.get(GETID);

		for(String str : fieldNames) {
			al.add(map.get(str));
		}
		methods = al.toArray(new Method[al.size()]);
		return methods;
	}

}
