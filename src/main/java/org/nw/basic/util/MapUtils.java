package org.nw.basic.util;

import java.lang.reflect.Field;
import java.util.Map;

public class MapUtils extends org.apache.commons.collections.MapUtils {
	public static void copyProperties(Map<String, Object> source, Object target) {
		if(source == null) {
			return;
		}

		for(String key : source.keySet()) {
			if(key.startsWith("_")) {
				continue;
			}
			Field field = ReflectionUtils.getDeclaredField(target, key);
			if(field != null) {
				ReflectionUtils.setFieldValue(target, key, source.get(key) == null ? null
						: ((String[]) source.get(key))[0]);
			}
		}
	}
}
