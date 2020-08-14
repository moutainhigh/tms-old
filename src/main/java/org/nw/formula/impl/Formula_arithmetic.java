package org.nw.formula.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.nw.basic.util.StringUtils;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;
import org.nw.formula.MapContext;

/**
 * arithmetic四则运算公式 解释： 可以支持加减乘除运算，这里其实相当于增加了一个arithmetic()函数 注意:
 * 不支持子公式，test->a+getColValue(aa,bb,cc,dd)是不支持的，
 * 可以写成：b->getColValue(aa,bb,cc,dd);test->a+b
 * 示例：test->a+b*c/d等同于：test->arithmetic(a+b*c/d)
 * 20110824注：：四则运算支持字符串运算，如果是字符型，自动以字符串拼接方式
 * 
 * @author fangw
 */
public class Formula_arithmetic extends AbstractFormula {

	/**
	 * 获取公式执行结果
	 * 
	 * @param rowContext
	 * @return
	 */
	public Object getResult(Map<String, Object> rowContext) {
		logger.debug("开始执行公式：" + this.getFormulaStr());

		List<Object> argList = new ArrayList<Object>(this.getArgs().size());
		argList.addAll(this.getArgs());

		for(int i = 0; i < argList.size(); i++) {
			Object obj = argList.get(i);
			if(obj instanceof IFormula) {
				IFormula formula = (IFormula) obj;
				// 如果参数是子公式，则先执行子公式
				argList.set(i, formula.getResult(rowContext));
			} else if(obj instanceof Character) {
				// 如果是操作符，则不作处理
			} else {
				String t = obj == null ? "" : obj.toString();
				if(t.startsWith("\"") && t.endsWith("\"")) {
					t = t.substring(1);
					t = t.substring(0, t.length() - 1);
					argList.set(i, t);
				} else {
					// 如果参数不是子公式，且不能转化为数值，则把变量翻译为值
					// argList.set(i, rowContext.get(obj));
					JexlEngine engine = new JexlEngine();
					JexlContext context = new MapContext(rowContext);
					Expression expression = engine.createExpression(obj.toString());
					argList.set(i, expression.evaluate(context));
				}
			}
		}

		// 进行计算
		StringBuilder sb = new StringBuilder();
		for(Object obj : argList) {
			if(obj != null && obj instanceof BigDecimal) {
				sb.append(((BigDecimal) obj).toPlainString());
			} else {
				if(rowContext.get(obj) != null) {
					if(rowContext.get(obj) != null && rowContext.get(obj) instanceof BigDecimal) {
						sb.append(((BigDecimal) rowContext.get(obj)).toPlainString());
					} else {
						if(StringUtils.isAsciiPrintable(rowContext.get(obj).toString())) {
							sb.append(rowContext.get(obj));
						} else {
							String str = rowContext.get(obj).toString();
							if(!str.startsWith("\"") && !str.endsWith("\"")) {
								str = "\"" + str + "\"";
							}
							sb.append(str);
						}
					}
				} else {
					if(obj == null) {
						sb.append(obj);
					} else {
						if(StringUtils.isAsciiPrintable(obj.toString())) {
							if(obj instanceof String && !StringUtils.isDecimal(obj.toString())
									&& !obj.toString().contains("_")) {
								// 如果是字符串，且可能包含+，-，*，/等字符，则认为是字符串常量
								String str = obj.toString();
								str = str.replace("+", "&a;");
								str = str.replace("-", "&b;");
								str = str.replace("*", "&c;");
								str = str.replace("/", "&d;");
								if(!str.startsWith("\"") && !str.endsWith("\"")) {
									str = "\"" + str + "\"";
								}
								sb.append(str);
							} else {
								sb.append(obj);
							}
						} else {
							String str = obj.toString();
							if(!str.startsWith("\"") && !str.endsWith("\"")) {
								str = "\"" + str + "\"";
							}
							sb.append(str);
						}
					}
				}
			}
		}

		// 解析表达式
		JexlEngine engine = new JexlEngine();
		JexlContext context = new MapContext(rowContext);
		Object value = null;
		String strExpression = sb.toString();
		try {
			if(strExpression.contains("\"\"") || strExpression.contains("\" \"")) {
				strExpression = strExpression.replace("\"\"", "\"&null;\"");
				strExpression = strExpression.replace("\" \"", "\"&nbsp;\"");
				Expression expression = engine.createExpression(strExpression);
				value = expression.evaluate(context);
				String strValue = value == null ? "" : value.toString();
				strValue = strValue.replace("&null;", "");
				if(strValue.equals("&nbsp;")) {
					// 如果公式都是空，则直接返回空串，而不是一个空格
					strValue = "";
				} else {
					strValue = strValue.replace("&nbsp;", " ");
					strValue = strValue.replace("&a;", "+");
					strValue = strValue.replace("&b;", "-");
					strValue = strValue.replace("&c;", "*");
					strValue = strValue.replace("&d;", "/");
				}
				value = strValue;
			} else {
				Expression expression = engine.createExpression(strExpression);
				value = expression.evaluate(context);
			}
		} catch(Exception e) {
			this.logger.error("JexlEngine出错，将做第一次尝试解析，公式内容:" + sb.toString());

			// 尝试
			String temp = sb.toString().replace("\"", "");
			try {
				Expression expression1 = new JexlEngine().createExpression(temp);
				value = expression1.evaluate(context);
				this.logger.error("尝试成功，公式结果:" + value);
			} catch(Exception e1) {
				this.logger.error("JexlEngine再次出错，将做为字符串拼接，公式内容:" + temp);
				if(!temp.contains("-") && !temp.contains("*") && !temp.contains("/")) {
					temp = temp.replace("+", "");
					this.logger.error("字符串拼接结果:" + temp);
					return temp;
				}
			}
		}

		return value;
	}

}