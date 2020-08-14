package org.nw.xml;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.pub.lang.UFTime;

/**
 * 根据参数的值以及参数的类型返回具体的参数对象
 * 
 * @author xuqc
 * 
 */
public class ResolvedJavaType {

	private ResolvedJavaType() {

	}

	/**
	 * 读取所有java基本类型的默认值
	 * 
	 * @param type
	 * @return Object
	 */
	public static Object getDefaultResolvedValue(String type) {
		int lastIndex = type.lastIndexOf(".");
		String baseShortName = "";
		boolean isPrimitive = false;
		if(lastIndex == -1) {
			baseShortName = type;
			isPrimitive = true;
		} else {
			baseShortName = type.substring(lastIndex + 1);
			if(baseShortName.equalsIgnoreCase("String")) {
				return "";
			} else if(baseShortName.equalsIgnoreCase("UFBoolean")) {
				return UFBoolean.FALSE;
			} else if(baseShortName.equalsIgnoreCase("UFDouble")) {
				return UFDouble.ZERO_DBL;
			} else if(baseShortName.equalsIgnoreCase("UFDateTime")) {
				return null;
			} else if(baseShortName.equalsIgnoreCase("UFDate")) {
				return null;
			} else if(baseShortName.equalsIgnoreCase("UFTime")) {
				return null;
			} else if(baseShortName.equalsIgnoreCase("Date")) {
				return new Date();
			} else if(baseShortName.equalsIgnoreCase("BigDecimal")) {
				return new BigDecimal(0);
			} else {
				isPrimitive = true;
			}
		}
		if(isPrimitive) {
			if(baseShortName.toLowerCase().startsWith("int")) {
				return 0;
			} else if(baseShortName.toLowerCase().startsWith("long")) {
				return 0l;
			} else if(baseShortName.toLowerCase().startsWith("double")) {
				return 0d;
			} else if(baseShortName.toLowerCase().startsWith("float")) {
				return 0f;
			} else if(baseShortName.toLowerCase().startsWith("boolean")) {
				return false;
			} else if(baseShortName.toLowerCase().startsWith("char")) {
				return '0';
			} else if(baseShortName.toLowerCase().startsWith("short")) {
				return 0;
			} else if(baseShortName.toLowerCase().startsWith("byte")) {
				return '0';
			}
		}
		return "";
	}

	/**
	 * 将String类型的value转换为type类型的value
	 * 
	 * @param type
	 * @param value
	 * @return Object
	 */
	public static Object getResolvedValue(String type, String value) {
		int lastIndex = type.lastIndexOf(".");
		String baseShortName = "";
		boolean isPrimitive = false;
		if(lastIndex == -1) {
			baseShortName = type;
			isPrimitive = true;
		} else {
			baseShortName = type.substring(lastIndex + 1);
			if(baseShortName.equalsIgnoreCase("String")) {
				return value;
			} else if(baseShortName.equalsIgnoreCase("UFBoolean")) {
				if(StringUtils.isBlank(value)) {
					return UFBoolean.FALSE;
				}
				return new UFBoolean(value);
			} else if(baseShortName.equalsIgnoreCase("UFDouble")) {
				if(StringUtils.isBlank(value)) {
					return null;
				}
				return new UFDouble(value);
			} else if(baseShortName.equalsIgnoreCase("UFDateTime")) {
				if(StringUtils.isBlank(value)) {
					return null;
				}
				return new UFDateTime(value);
			} else if(baseShortName.equalsIgnoreCase("UFDate")) {
				if(StringUtils.isBlank(value)) {
					return null;
				}
				return new UFDate(value);
			} else if(baseShortName.equalsIgnoreCase("UFTime")) {
				if(StringUtils.isBlank(value)) {
					return null;
				}
				return new UFTime(value);
			} else if(baseShortName.equalsIgnoreCase("Date")) {
				return DateUtils.parseString(value);
			} else if(baseShortName.equalsIgnoreCase("BigDecimal")) {
				return new BigDecimal(value);
			} else {
				isPrimitive = true;
			}
		}
		if(isPrimitive) {
			if(baseShortName.toLowerCase().startsWith("int")) {
				return TypeTransfer.String2int(value);
			} else if(baseShortName.toLowerCase().startsWith("long")) {
				return TypeTransfer.String2long(value);
			} else if(baseShortName.toLowerCase().startsWith("double")) {
				return TypeTransfer.String2double(value);
			} else if(baseShortName.toLowerCase().startsWith("float")) {
				return TypeTransfer.String2float(value);
			} else if(baseShortName.toLowerCase().startsWith("boolean")) {
				if(value.equalsIgnoreCase("true"))
					return true;
				else
					return false;
			} else if(baseShortName.toLowerCase().startsWith("char")) {
				if(value.length() > 0)
					return value.toCharArray()[0];
				else
					return '0';
			} else if(baseShortName.toLowerCase().startsWith("short")) {
				return TypeTransfer.String2short(value);
			} else if(baseShortName.toLowerCase().startsWith("byte")) {
				return TypeTransfer.String2byte(value);
			}
		}
		return "";
	}
}
