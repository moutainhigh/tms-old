/*
 * 创建日期 2005-9-15
 *
 * 
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package org.nw.vo.pub;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 反射帮助类
 * 
 * 缓存结构：key：class名称，value:Map(key:0(get),value(Map(key:propertyName,value:method
 * ))
 * 
 * @nopublish
 * 
 */
public class FieldHelper {
	private static HashMap<String, Map<String, Field>> cache = new HashMap<String, Map<String, Field>>();

	private static FieldHelper bhelp = new FieldHelper();

	private Map<String, Field[]> declaredMap = new HashMap<String, Field[]>();

	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private FieldHelper() {
	}

	public static FieldHelper getInstance() {
		return bhelp;
	}

	private Map<String, Field> cacheField(Class beanCls) {
		String key = beanCls.getName();
		Map<String, Field> cFields = cache.get(key);
		if(cFields == null) {
			rwl.readLock().unlock();
			rwl.writeLock().lock();
			try {
				cFields = cache.get(key);
				if(cFields == null) {
					cFields = new HashMap<String, Field>();
					Field[] fields = getField(beanCls);
					for(Field field : fields) {
						if(field.getName().startsWith("m_"))
							cFields.put(field.getName().substring(2).toLowerCase(), field);
						else
							cFields.put(field.getName().toLowerCase(), field);
					}
				}

				cache.put(key, cFields);

			} finally {
				rwl.readLock().lock();
				rwl.writeLock().unlock();
			}
		}
		return cFields;
	}

	public Field[] getCacheFields(Class c) {
		Map<String, Field> cField = null;
		rwl.readLock().lock();
		try {
			cField = cacheField(c);
		} finally {
			rwl.readLock().unlock();
		}

		return cField.values().toArray(new Field[cField.size()]);
	}

	public Field getField(Class c, String propName) {
		Map<String, Field> cField = null;
		rwl.readLock().lock();
		try {
			cField = cacheField(c);
		} finally {
			rwl.readLock().unlock();
		}
		return cField.get(propName);
	}

	private Field[] getField(Class c) {
		Field[] f = getSerialField(c);
		Class cl = c.getSuperclass();
		while(cl != Object.class) {
			Field[] f1 = getSerialField(cl);
			if(f1 != null) {
				Field[] tf = f;
				f = new Field[tf.length + f1.length];
				shallowCopy(tf, f, 0);
				shallowCopy(f1, f, tf.length);
			}
			cl = cl.getSuperclass();
		}
		return f;
	}

	private void shallowCopy(Field[] src, Field[] dest, int from) {
		for(int i = 0; i < src.length; i++) {
			dest[from + i] = src[i];
		}
	}

	private Field[] getSerialField(Class c) {
		Field[] f = declaredMap.get(c.getName());
		if(f == null) {
			List<Field> al = new ArrayList<Field>();
			f = c.getDeclaredFields();
			for(Field field : f) {

				if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
					continue;
				else {
					field.setAccessible(true);
					al.add(field);
				}
			}
			f = al.toArray(new Field[al.size()]);
			declaredMap.put(c.getName().intern(), f);
		}
		return f;
	}

}
