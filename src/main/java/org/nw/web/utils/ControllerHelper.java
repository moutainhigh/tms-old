package org.nw.web.utils;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;

/**
 * Controller的辅助类，为Controller提供从request提取参数的方法。
 * 
 * @author xuqc
 * @date 2011-4-8
 */
public class ControllerHelper {

	/**
	 * 页面发送到参数如： GRID_QUERY_FIELDS :
	 * ["appCode","appName","isDisplay","isDisabled"] GRID_QUERY_KEYWORD : admin
	 * 返回的结果如： (appCode like 'admin%' or appName like 'admin%')
	 * 该函数根据其field类型，返回组装后的sql查询条件 注意fields query是关键字
	 * 
	 * @param request
	 * @return
	 */
	public static String getGridQueryCondition(HttpServletRequest request, Class<?> clazz) {
		return getGridQueryCondition(request, clazz, null);
	}

	/**
	 * 解析Grid查询控件的查询条件，返回查询语句的条件
	 * 
	 * @param request
	 * @param clazz
	 *            查询条件字段所属的类
	 * @param prefix
	 *            是否加上表的前缀
	 * @return
	 */
	public static String getGridQueryCondition(HttpServletRequest request, Class<?> clazz, String prefix) {
		String fields[] = getGridQueryFields(request);
		String keyword = getGridQueryKeyword(request);

		StringBuffer sb = new StringBuffer("(");
		if(fields == null || fields.length == 0 || StringUtils.isBlank(keyword))
			return "";
		if(clazz == null) {
			// 不进行field校验，直接拼装
			// 根据fieldsType判断参数类型
			String[] fieldsType = getGridQueryFieldsType(request);
			// 字段类型全部使用string类型
			for(int i = 0; i < fields.length; i++) {
				if(i > 0)
					sb.append(" or ");
				if(StringUtils.isNotBlank(prefix)) {
					sb.append(prefix + ".");
				}
				sb.append(fields[i]);
				if("int".equalsIgnoreCase(fieldsType[i]) || "float".equalsIgnoreCase(fieldsType[i])) {
					// 数字型
					sb.append("=");
					sb.append(keyword);
				} else {
					sb.append(" like '%"); // 加入完全模糊查询
					sb.append(keyword);
					sb.append("%'");
				}
			}
		} else {
			for(int i = 0; i < fields.length; i++) {
				// 处理xtsOrg.orgName的情况
				Field field;
				if(fields[i].indexOf(".") > 0) {
					String temp = fields[i].substring(0, fields[i].indexOf("."));
					Field nestedField = ReflectionUtils.findField(clazz, temp);
					String f = fields[i].substring(fields[i].indexOf(".") + 1, fields[i].length());
					field = ReflectionUtils.findField(nestedField.getType(), f);
				} else
					field = ReflectionUtils.findField(clazz, fields[i]);
				if(field == null)
					continue;
				if(Number.class.isAssignableFrom(field.getType())) { // number类型
					int value;
					try {
						value = Integer.parseInt(keyword);
					} catch(Exception e) {
						continue;
					}
					if(i > 0 && sb.length() > 1)
						sb.append(" or ");
					if(StringUtils.isNotBlank(prefix)) {
						sb.append(prefix + ".");
					}
					sb.append(fields[i]);
					sb.append(" = ");
					sb.append(value);
				} else {
					if(i > 0 && sb.length() > 1)
						sb.append(" or ");
					sb.append("( ");
					String[] keywordAry = keyword.split(" "); // 如果keyword包括空格，那么作为多个查询条件
					for(int j = 0; j < keywordAry.length; j++) {
						if(StringUtils.isNotBlank(keywordAry[j])) {
							if(StringUtils.isNotBlank(prefix)) {
								sb.append(prefix + ".");
							}
							sb.append(fields[i]);
							sb.append(" like '%");// 使用完全模糊匹配
							sb.append(keywordAry[j]);
							sb.append("%'");
							if(j < keywordAry.length - 1) {
								sb.append(" or ");
							}
						}
					}
					sb.append(" )");
				}
			}
		}
		sb.append(")");
		if(sb.length() <= 2) {// 没有查询条件，只有()
			return null;
		}
		return sb.toString();
	}

	/**
	 * 从request中读Grid查询控件的查询字段的类型，可能是string,float,int,date等
	 * 
	 * @param request
	 * @return
	 */
	public static String[] getGridQueryFieldsType(HttpServletRequest request) {
		String fieldsType[] = null;
		String fieldsTypeStr = request.getParameter(Constants.GRID_QUERY_FIELDS_TYPE);
		if(fieldsTypeStr != null) {
			fieldsTypeStr = fieldsTypeStr.substring(1, fieldsTypeStr.length() - 1); // 过滤前后两个[]
			fieldsType = fieldsTypeStr.split(",");
			for(int i = 0; i < fieldsType.length; i++) {
				fieldsType[i] = fieldsType[i].substring(1, fieldsType[i].length() - 1); // 过滤前后两个"
			}
		}
		return fieldsType;
	}

	/**
	 * 从request中读Grid查询控件的查询字段
	 * 
	 * @param request
	 * @return
	 */
	public static String[] getGridQueryFields(HttpServletRequest request) {
		String fields[] = null;
		String fieldsStr = request.getParameter(Constants.GRID_QUERY_FIELDS);
		if(fieldsStr != null) {
			fieldsStr = fieldsStr.substring(1, fieldsStr.length() - 1); // 过滤前后两个[]
			fields = fieldsStr.split(",");
			for(int i = 0; i < fields.length; i++) {
				fields[i] = fields[i].substring(1, fields[i].length() - 1); // 过滤前后两个"
			}
		}
		return fields;
	}

	/**
	 * 从request中读取Ext Grid查询控件的关键字
	 * 
	 * @param request
	 * @return
	 */
	public static String getGridQueryKeyword(HttpServletRequest request) {
		return request.getParameter(Constants.GRID_QUERY_KEYWORD);
	}

	/**
	 * 根据参数返回order by子句
	 * 
	 * @param request
	 * @return
	 */
	public static String getOrderBy(HttpServletRequest request) {
		return getOrderBy(request, null);
	}

	/**
	 * 判断排序参数是否是类的一个成员变量，是则加入排序，否则忽略
	 * 
	 * @param request
	 * @param clazz
	 * @return
	 */
	public static String getOrderBy(HttpServletRequest request, Class<?> clazz) {
		String dir = request.getParameter(Constants.SORT_DIR_PARAM);
		String field = request.getParameter(Constants.SORT_FIELD_PARAM);
		if(StringUtils.isNotBlank(dir) && StringUtils.isNotBlank(field)) {
			if(clazz != null) {
				Field _field = ReflectionUtils.findField(clazz, field);
				if(_field != null) {
					return " order by " + field + " " + dir;
				}
			} else {
				return " order by " + field + " " + dir;
			}
		}
		//默认为创建时间倒序  yaojiie 2015 12 15
		return "order by create_time desc";
	}
}
