package org.nw.formula.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;
import org.nw.formula.MapContext;

/**
 * iif公式 格式示例：
 * "approvestatus_name->iif(actiontype==\"BIZ\",iif(ischeck==\"Y\",\"已处理\",\"未处理\"),iif(approvestatus==0,\"未处理\",iif(approvestatus==1,\"已处理\",iif(approvestatus==4,\"作废\",approvestatus))))"
 * ,
 * "approveresult_name->iif(approveresult==\"Y\",\"批准\",iif(approveresult==\"R\",\"驳回\",iif(approveresult==\"N\",\"不批准\",approveresult)))"
 * ,
 * 
 * @author fangw
 */
public class Formula_iif extends AbstractFormula {

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

		// 参数修正，如：[ischeck=="Y", "通知(未读), ", "通知"]，这里由于出现了括号，则后一个引号成了独立的
		for(int i = 0; i < argList.size(); i++) {
			Object obj = argList.get(i);
			if(obj != null && obj.equals("\"")) {
				if(i > 0) {
					Object pObj = argList.get(i - 1);
					if(pObj != null && pObj.toString().startsWith("\"")) {
						pObj = pObj.toString().substring(1);
					}
					argList.set(i - 1, pObj);
					argList.remove(i);
					// 重头开始
					i = -1;
					continue;
				}
			}
		}

		for(int i = 0; i < argList.size(); i++) {
			Object obj = argList.get(i);
			if(obj instanceof IFormula) {
				IFormula formula = (IFormula) obj;
				// 如果参数是子公式，则先执行子公式
				argList.set(i, "\"" + formula.getResult(rowContext) + "\"");
			}
		}

		// 有可能条件中的子公式未合并，这里如果超过3个参数，则合并倒数第二个之前的所有参数，应该返回布尔值
		if(argList.size() > 3) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < argList.size() - 2; i++) {
				Object obj = argList.get(i);
				if(obj != null && obj instanceof BigDecimal) {
					sb.append(((BigDecimal) obj).toPlainString());
				} else {
					sb.append(obj);
				}
			}

			JexlEngine engine = new JexlEngine();
			JexlContext context = new MapContext(rowContext);
			Object value = null;
			try {
				Expression expression = engine.createExpression(sb.toString());
				value = expression.evaluate(context);
			} catch(Exception e) {
				this.logger.error("JexlEngine出错，公式内容将作为公式结果:" + sb.toString());
				value = sb.toString();
			}
			while(argList.size() > 3) {
				argList.remove(0);
			}
			argList.set(0, value);
		}

		// 执行公式
		Object result = null;
		String express = argList.get(0).toString();
		// 替换中文变量:当前页数=__pageIndex__,总页数=__totalPages__
		express = express.replace("当前页数", "__pageIndex__");
		express = express.replace("总页数", "__totalPages__");

		JexlEngine engine = new JexlEngine();
		JexlContext context = new MapContext(rowContext);
		Object value = null;
		try {
			Expression expression = engine.createExpression(express);
			value = expression.evaluate(context);
		} catch(Exception e) {
			this.logger.error("JexlEngine出错，公式内容将作为公式结果:" + express);
			value = express;
		}

		if(value instanceof Boolean || (value != null && (value.toString().length() > 0))) {
			Boolean booleanValue = null;
			if(value instanceof Boolean) {
				booleanValue = (Boolean) value;
			} else {
				String str = value.toString().toLowerCase();
				if(str.equals("y")) {
					booleanValue = Boolean.TRUE;
				} else if(str.equals("n")) {
					booleanValue = Boolean.FALSE;
				} else {
					booleanValue = Boolean.parseBoolean(str);
				}
			}

			if(booleanValue) {
				result = this.parseString(argList.get(1), rowContext);
			} else {
				result = this.parseString(argList.get(2), rowContext);
			}
		} else {
			logger.error("公式书写错误:" + this.getFormulaStr() + ",iif表达式返回不是布尔值!");
			return null;
			// throw new RuntimeException("公式书写错误:" + this.getFormulaStr() +
			// ",iif表达式返回不是布尔值!");
		}

		logger.debug("公式执行结果：" + result);
		return result;
	}

	private Object parseString(Object obj, Map<String, Object> rowContext) {
		if(obj instanceof IFormula) {
			// 存在子公式
			IFormula formula = (IFormula) obj;
			logger.debug("发现子公式：" + formula.getFormulaStr());
			// 获取子公式的值
			Object value = formula.getResult(rowContext);
			if(value != null) {
				return value;
			}
		} else {
			if(obj != null) {
				if(this.isString(obj)) {
					String str = obj.toString();
					if(str.startsWith("\"") && str.endsWith("\"")) {
						str = str.substring(1, str.length() - 1);
						return str;
					} else {
						if(str.length() == 0) {
							return str;
						}
						return rowContext.get(str);
					}
				} else {
					JexlEngine engine = new JexlEngine();
					JexlContext context = new MapContext(rowContext);
					Object value = null;
					try {
						Expression expression = engine.createExpression(obj.toString());
						value = expression.evaluate(context);
					} catch(Exception e) {
						this.logger.error("JexlEngine出错，公式内容将作为公式结果:" + obj.toString());
						value = obj.toString();
					}
					return value;
				}
			}
		}
		return null;
	}

	private boolean isString(Object obj) {
		if(obj == null) {
			return false;
		}
		String str = obj.toString();
		if(str.startsWith("\"") && str.endsWith("\"")) {
			str = str.substring(1, str.length() - 1);
			if(!str.contains("\"")) {
				return true;
			}
		} else {
			if(!str.contains("+") && !str.contains("-") && !str.contains("*") && !str.contains("/")) {
				try {
					Double.parseDouble(str.trim());
					return false;
				} catch(NumberFormatException e) {
				}
				return true;
			}
		}
		return false;
	}
}
