package org.nw.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.nw.basic.util.ReflectionUtils;
import org.nw.basic.util.SecurityUtils;
import org.nw.constants.Constants;
import org.nw.jf.UiConstants;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;

/**
 * 查询模板处理类<br>
 * 2011-4-9
 * 
 * @author fangw
 */
public class QueryHelper {
	static Logger logger = Logger.getLogger(QueryHelper.class);

	/**
	 * 根据superVO的值返回查询条件，String类型使用like查询，其他都是用等号查询
	 * 
	 * @param superVO
	 * @param operaCodeMap
	 *            字段名称与操作符的Map,表明该字段用什么操作符进行查询
	 * @return
	 */
	public static String getWhere(SuperVO superVO, Map<String, String> operaCodeMap) {
		if(superVO == null) {
			return null;
		}
		StringBuffer cond = new StringBuffer();
		for(String fieldName : superVO.getAttributeNames()) {
			Object value = superVO.getAttributeValue(fieldName);
			if(value == null || StringUtils.isBlank(value.toString())) {
				continue;
			}

			if(cond.length() != 0) {
				cond.append(" and ");
			}
			Field field = ReflectionUtils.findField(superVO.getClass(), fieldName);
			String condition = "="; // 默认的操作符
			if(Number.class.isAssignableFrom(field.getType())) {
				if(operaCodeMap != null) {
					condition = operaCodeMap.get(fieldName);
				}
				if(condition == null) {
					condition = "=";
				}
				if("Y".equalsIgnoreCase(String.valueOf(value))) {
					value = 1;
				} else if("N".equalsIgnoreCase(String.valueOf(value))) {
					value = 0;
				}
				cond.append(fieldName);
				cond.append(" ");
				cond.append(condition);
				cond.append(" ");
				cond.append(value);
			} else if(Boolean.class.isAssignableFrom(field.getType())) {
				if(operaCodeMap != null) {
					condition = operaCodeMap.get(fieldName);
				}
				cond.append(fieldName);
				cond.append(" ");
				cond.append(condition);
				cond.append(" ");
				if("true".equalsIgnoreCase(String.valueOf(value)) || "Y".equalsIgnoreCase(String.valueOf(value))) {
					cond.append("'Y'");
				} else {
					cond.append("'N'");
				}
			} else {
				condition = "like";
				if(operaCodeMap != null) {
					condition = operaCodeMap.get(fieldName);
				}
				cond.append(fieldName);
				cond.append(" ");
				cond.append(condition);
				cond.append(" '");
				cond.append(value);
				cond.append("%'");
			}
		}
		return cond.toString();
	}

	/**
	 * 根据superVO的值返回查询条件，String类型使用like查询，其他都是用等号查询
	 * 
	 * @param superVO
	 * @return
	 */
	public static String getWhere(SuperVO superVO) {
		return getWhere(superVO, null);
	}

	/**
	 * 解析查询模板的条件,并加入数据权限的条件
	 * 
	 * @param params
	 * @param condVOs
	 *            查询模板中使用的查询条件，这里会根据field_code定位到条件对象
	 * @return
	 * @author xuqc
	 * @date 2012-7-18
	 * 
	 */
	public static String parseCondition(String params, ParamVO paramVO, UiQueryTempletVO queryTempletVO) {
		if(queryTempletVO == null || queryTempletVO.getConditions().size() == 0) {
			return null;
		}
		List<QueryConditionVO> condVOs = queryTempletVO.getConditions();
		StringBuffer cond = new StringBuffer(" 1=1 ");

		/**
		 * 1.处理前台传来的参数
		 */
		if(StringUtils.isNotBlank(params) && !"[]".equals(params)) {
			JsonNode node = JacksonUtils.readTree(params);
			for(int m = 0; m < node.size(); m++) {
				JsonNode child = node.get(m);
				String fieldName = child.get("fieldName").getValueAsText().trim(); // 在设计参照的公式时，经常会在前面加入空格
				String value = child.get("value").getValueAsText().trim();
				String condition = child.get("condition").getValueAsText().trim();
				if(StringUtils.isBlank(value)) {
					continue;
				}
				if(fieldName.endsWith("timeline")) {// tms对timeline（时效）做了特殊处理
					continue;
				}
				if(fieldName.startsWith(Constants.START_DATE_FIELD_PREFIX)) {
					// ulw中的开始日期
					condition = ">=";
					// 2015 11 13 yaojiie
					//当传入的时间是 xxxx-xx-xx时，将开始时间和结束时间，分别加上0和59
					if(value.length() == 10){
						value += " 00:00";
					}
					fieldName = fieldName.substring(Constants.START_DATE_FIELD_PREFIX.length());
				} else if(fieldName.startsWith(Constants.END_DATE_FIELD_PREFIX)) {
					// ulw中的结束日期
					condition = "<=";
					if(value.length() == 10){
						value += " 23:59:59";
					}
					fieldName = fieldName.substring(Constants.END_DATE_FIELD_PREFIX.length());
				}

				QueryConditionVO condVO = UiTempletUtils.getCondVOByField_code(condVOs, fieldName);
				if(condVO == null) {
					continue;
				}
				if(condVO.getIscondition() != null && !condVO.getIscondition().booleanValue()) {
					// 逻辑条件，不要拼到sql中
					continue;
				}

				if(StringUtils.isBlank(condition)) {
					condition = UIUtils.getDefaultOperator(condVO);
				}

				if(cond.length() != 0) {
					cond.append(" and ");
				}

				StringBuffer oneCond = new StringBuffer();

				if(condVO.getData_type().intValue() == UiConstants.DATATYPE.DECIMAL.intValue()
						|| condVO.getData_type().intValue() == UiConstants.DATATYPE.INTEGER.intValue()) {
					// 数字类型
					if(value.startsWith("[") && value.endsWith("]")) {
						if(value.length() > 2) {
							// 处理in查询
							oneCond.append(fieldName);
							if(condition.equals("<>")) {
								// 如果是不等于号
								oneCond.append(" not ");
							}
							value = " in (" + value.substring(1, value.length() - 1) + ")";
							oneCond.append(value);
						}
					} else if(value.indexOf(",") > -1) {
						// 存在逗号，使用in查询
						oneCond.append(fieldName);
						if(condition.equals("<>")) {
							// 如果是不等于号
							oneCond.append(" not ");
						}
						oneCond.append(" in (" + value + ")");
					} else {
						// 页面上使用checkbox的时候，目前我们都统一返回Y&N，但是NC可能存储的是1&0，这里做个转换
						if("Y".equalsIgnoreCase(value)) {
							value = "1";
						} else if("N".equalsIgnoreCase(value)) {
							value = "0";
						}
						oneCond.append(fieldName);
						oneCond.append(" ");
						oneCond.append(SecurityUtils.unescape(condition));
						oneCond.append(" ");
						oneCond.append(value);
					}
				} else if(condVO.getData_type().intValue() == UiConstants.DATATYPE.CHECKBOX.intValue()) {
					// checkbox类型，在NC中会使用N，Y存储
					// FIXME XUQC下拉现在支持多选了
					oneCond.append(fieldName);
					if(value.indexOf(Constants.SPLIT_CHAR) != -1) {
						if(condition.equals("<>")) {
							// 如果是不等于号
							oneCond.append(" not ");
						}
						oneCond.append(" in ");
						String[] arr = value.split(Constants.SPLIT_CHAR);
						for(int i = 0; i < arr.length; i++) {
							if("true".equalsIgnoreCase(arr[i]) || "Y".equalsIgnoreCase(arr[i])) {
								oneCond.append("'Y'");
							} else {
								oneCond.append("'N'");
							}
							if(i != arr.length - 1) {
								oneCond.append(",");
							}
						}
					} else {
						oneCond.append(condition);
						if("true".equalsIgnoreCase(value) || "Y".equalsIgnoreCase(value)) {
							oneCond.append("'Y'");
						} else {
							oneCond.append("'N'");
						}
					}
				} else if(condVO.getData_type().intValue() == UiConstants.DATATYPE.SELECT.intValue()) {
					// 下拉类型，需要根据参照类型的前缀进行判断
					oneCond.append(fieldName);
					String consult_code = condVO.getConsult_code();
					// 存在逗号，使用in查询
					String[] arr1 = value.split(Constants.SPLIT_CHAR);
					String temp = "";
					for(int i = 0; i < arr1.length; i++) {
						if(consult_code.startsWith("I")) {
							// 数字类型的返回值
							temp += arr1[i] + ",";
						} else {
							temp += "'" + arr1[i] + "',";
						}
					}
					if(condition.equals("<>")) {
						// 如果是不等于号
						oneCond.append(" not ");
					}
					oneCond.append(" in (" + temp.substring(0, temp.length() - 1) + ")");
				} else {
					if(value.startsWith("[") && value.endsWith("]")) {
						if(value.length() > 2) {
							// 处理in查询
							oneCond.append(fieldName);
							if(condition.equals("<>")) {
								// 如果是不等于号
								oneCond.append(" not ");
							}
							oneCond.append(" in");
							value = value.substring(1, value.length() - 1);
							String[] array = value.split(Constants.SPLIT_CHAR);
							String valueStr = "";
							for(int i = 0; i < array.length; i++) {
								valueStr += "'" + array[i] + "',";
							}
							// 这里valueStr肯定有值
							valueStr = valueStr.substring(0, valueStr.length() - 1);
							oneCond.append("(" + valueStr + ")");
						}
					} else if(value.indexOf(Constants.SPLIT_CHAR) > -1) {
						// 存在逗号，使用in查询
						oneCond.append(fieldName);
						String[] arr1 = value.split(Constants.SPLIT_CHAR);
						String temp = "";
						for(int i = 0; i < arr1.length; i++) {
							temp += "'" + arr1[i] + "',";
						}
						if(condition.equals("<>")) {
							// 如果是不等于号
							oneCond.append(" not ");
						}
						oneCond.append(" in (" + temp.substring(0, temp.length() - 1) + ")");
					} else {
						oneCond.append(fieldName);
						oneCond.append(" ");
						oneCond.append(SecurityUtils.unescape(condition));
						oneCond.append(" '");
						if(SecurityUtils.unescape(condition).equalsIgnoreCase("like")) {
							oneCond.append("%");
						}
						oneCond.append(value);
						if(SecurityUtils.unescape(condition).equalsIgnoreCase("like")) {
							oneCond.append("%");
						}
						oneCond.append("'");
					}
				}
				if(StringUtils.isBlank(condVO.getInstrumentsql())) {
					cond.append(oneCond);
				} else {
					cond.append(condVO.getInstrumentsql().replaceAll("\\?\\?\\?", oneCond.toString()));
				}
			}
		}

		/**
		 * FIXME 2012-07-19数据权限
		 */
		String dataPowerQueryString = getDataPowerQueryString(paramVO, queryTempletVO);
		if(StringUtils.isNotBlank(dataPowerQueryString)) {
			cond.append(" and ");
			cond.append(dataPowerQueryString);
		}
		return cond.toString();
	}

	/**
	 * 返回数据权限的where子句
	 * 
	 * @param paramVO
	 * @param queryTempletVO
	 * @return
	 * @author xuqc
	 * @date 2012-7-19
	 * 
	 */
	public static String getDataPowerQueryString(ParamVO paramVO, UiQueryTempletVO queryTempletVO) {
		return null;
	}

	/**
	 * 根据条件VO组装成String
	 * 
	 * @param condVOs
	 * @return
	 * @author xuqc
	 * @date 2012-7-18
	 * 
	 */
	private static String getCondString(List<QueryConditionVO> condVOs, String s) {
		if(condVOs == null || condVOs.size() == 0) {
			return null;
		}
		StringBuffer cond = new StringBuffer();
		for(QueryConditionVO condVO : condVOs) {
			// 若锁定该条件,并且默认值是存在的
			String fieldName = condVO.getField_code();

			if(cond.length() != 0) {
				cond.append(" " + s + " ");
			}

			StringBuffer oneCond = new StringBuffer();

			String value = condVO.getValue();
			if(StringUtils.isBlank(value)) {
				continue;
			}
			if(condVO.getData_type().intValue() == UiConstants.DATATYPE.DECIMAL.intValue()
					|| condVO.getData_type().intValue() == UiConstants.DATATYPE.INTEGER.intValue()) {
				if(value.startsWith("[") && value.endsWith("]")) {
					if(value.length() > 2) {
						// 处理in查询
						oneCond.append(fieldName);
						oneCond.append(" in");
						value = "(" + value.substring(1, value.length() - 1) + ")";
						oneCond.append(value);
					}
				} else if(value.indexOf(",") > -1) {
					// 存在冒号，使用in查询
					oneCond.append(fieldName);
					oneCond.append(" in (" + value + ")");
				} else {
					// 页面上使用checkbox的时候，目前我们都统一返回Y&N，但是NC可能存储的是1&0，这里做个转换
					if("Y".equalsIgnoreCase(value)) {
						value = "1";
					} else if("N".equalsIgnoreCase(value)) {
						value = "0";
					}
					// 只有一个值，使用第一个操作符
					oneCond.append(fieldName);
					oneCond.append(" ");
					int index = condVO.getOpera_code().indexOf("@");
					if(index == -1) {
						oneCond.append(condVO.getOpera_code());
					} else {
						// 取第一个操作符
						oneCond.append(condVO.getOpera_code().substring(0, index));
					}
					oneCond.append(" ");
					oneCond.append(value);
				}
			} else if(condVO.getData_type().intValue() == UiConstants.DATATYPE.CHECKBOX.intValue()) {
				// UFBoolean在NC中会使用N，Y存储
				oneCond.append(fieldName);
				String operator = null; // 操作符
				int index = condVO.getOpera_code().indexOf("@");
				if(index == -1) {
					operator = condVO.getOpera_code();
				} else {
					// 取第一个操作符
					operator = condVO.getOpera_code().substring(0, index);
				}
				oneCond.append(operator);
				if("true".equalsIgnoreCase(value) || "Y".equalsIgnoreCase(value)) {
					oneCond.append("'Y'");
				} else {
					oneCond.append("'N'");
				}
			} else if(condVO.getData_type().intValue() == UiConstants.DATATYPE.SELECT.intValue()) {
				// 下拉类型，需要根据参照类型的前缀进行判断
				oneCond.append(fieldName);
				String consult_code = condVO.getConsult_code();
				// 存在逗号，使用in查询
				String[] arr1 = value.split(Constants.SPLIT_CHAR);
				String temp = "";
				for(int i = 0; i < arr1.length; i++) {
					if(consult_code.startsWith("I")) {
						// 数字类型的返回值
						temp += arr1[i] + ",";
					} else {
						temp += "'" + arr1[i] + "',";
					}
				}
				oneCond.append(" in (" + temp.substring(0, temp.length() - 1) + ")");
			} else {
				if(value.startsWith("[") && value.endsWith("]")) {
					if(value.length() > 2) {
						// 处理in查询
						oneCond.append(fieldName);
						oneCond.append(" in");
						value = value.substring(1, value.length() - 1);
						String[] array = value.split(Constants.SPLIT_CHAR);
						String valueStr = "";
						for(int i = 0; i < array.length; i++) {
							valueStr += "'" + array[i] + "',";
						}
						// 这里valueStr肯定有值
						valueStr = valueStr.substring(0, valueStr.length() - 1);
						oneCond.append("(" + valueStr + ")");
					}
				} else if(value.indexOf(",") > -1) {
					// 存在冒号，使用in查询
					oneCond.append(fieldName);
					String[] arr1 = value.split(Constants.SPLIT_CHAR);
					String temp = "";
					for(int i = 0; i < arr1.length; i++) {
						temp += "'" + arr1[i] + "',";
					}
					oneCond.append(" in (" + temp.substring(0, temp.length() - 1) + ")");
				} else {
					oneCond.append(fieldName);
					oneCond.append(" ");
					String operator = null; // 操作符
					int index = condVO.getOpera_code().indexOf("@");
					if(index == -1) {
						operator = condVO.getOpera_code();
					} else {
						// 取第一个操作符
						operator = condVO.getOpera_code().substring(0, index);
					}
					oneCond.append(operator);
					oneCond.append(" '");
					oneCond.append(value);
					if(operator.equalsIgnoreCase("like")) {
						oneCond.append("%");
					}
					oneCond.append("'");
				}
			}
			if(StringUtils.isBlank(condVO.getInstrumentsql())) {
				cond.append(oneCond);
			} else {
				cond.append(condVO.getInstrumentsql().replaceAll("\\?\\?\\?", oneCond.toString()));
			}
		}
		return cond.toString();
	}

	/**
	 * 查询模板的默认查询条件，包括锁定的条件
	 * 
	 * @param queryTempletVO
	 * @return
	 * @author xuqc
	 * @date 2012-7-18
	 * 
	 */
	public static String getDefaultCond(UiQueryTempletVO queryTempletVO) {
		if(queryTempletVO == null) {
			// 可能没有配置查询模板
			return null;
		}
		List<QueryConditionVO> defaultCondVOs = new ArrayList<QueryConditionVO>();
		List<QueryConditionVO> condVOs = queryTempletVO.getConditions();
		for(QueryConditionVO condVO : condVOs) {
			if(!condVO.getIscondition().booleanValue()) {
				// 没有勾选【是否查询条件】，是逻辑条件
				continue;
			}
			// 2013-4-19固定条件不在默认条件里面了。固定条件在后台直接拼接
			if((condVO.getIf_default().booleanValue()) && StringUtils.isNotBlank(condVO.getValue())) {
				defaultCondVOs.add(condVO);
			}
		}
		return getCondString(defaultCondVOs, "and");
	}

	/**
	 * 从查询模板中读取锁定的查询条件，这些条件是必须加入的
	 */
	public static String getImmobilityCond(UiQueryTempletVO queryTempletVO) {
		if(queryTempletVO == null) {
			// 可能没有配置查询模板
			return null;
		}
		List<QueryConditionVO> ImmobilityCondVOs = new ArrayList<QueryConditionVO>();
		List<QueryConditionVO> condVOs = queryTempletVO.getConditions();
		for(QueryConditionVO condVO : condVOs) {
			if(!condVO.getIscondition().booleanValue()) {
				// 没有勾选【是否查询条件】，是逻辑条件
				continue;
			}
			if(condVO.getIf_immobility().booleanValue() && StringUtils.isNotBlank(condVO.getValue())) {
				ImmobilityCondVOs.add(condVO);
			}
		}
		return getCondString(ImmobilityCondVOs, "and");
	}

	/**
	 * 从查询模板中读取锁定的查询条件，这些条件是必须加入的
	 */
	public static String getLogicCond(UiQueryTempletVO queryTempletVO) {
		if(queryTempletVO == null) {
			// 可能没有配置查询模板
			return null;
		}
		List<QueryConditionVO> logicCondVOs = new ArrayList<QueryConditionVO>();
		List<QueryConditionVO> condVOs = queryTempletVO.getConditions();
		for(QueryConditionVO condVO : condVOs) {
			if(!condVO.getIscondition().booleanValue()) {
				logicCondVOs.add(condVO);
			}
		}
		return getCondString(logicCondVOs, "or");
	}

	public static void main(String[] args) {
		String str = "abc ??? def";
		System.out.println(str.replaceAll("\\?\\?\\?", "123"));
	}
}
