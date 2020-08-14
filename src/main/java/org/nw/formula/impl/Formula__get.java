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
 * 直接取值运算符 解释： 等同->运算 示例： a->b等同于：a->_get(b)
 * 
 * @author fangw
 */
public class Formula__get extends AbstractFormula {

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

		if(argList.size() != 1) {
			throw new RuntimeException("_get公式只应该有一个参数！");
		}

		for(int i = 0; i < argList.size(); i++) {
			Object obj = argList.get(i);
			if(obj == null) {
				return null;
			}

			if(obj instanceof IFormula) {
				IFormula formula = (IFormula) obj;
				// 如果参数是子公式，则先执行子公式
				argList.set(i, formula.getResult(rowContext));
			} else if(obj instanceof BigDecimal) {
				argList.set(i, ((BigDecimal) obj).toPlainString());
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

		return argList.get(0);
	}

}