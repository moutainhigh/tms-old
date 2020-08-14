package org.nw.formula.impl;

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
 * toString
 * 
 * @author fangw
 */
public class Formula_tostring extends AbstractFormula {

	/**
	 * 获取公式执行结果
	 * 
	 * @param rowContext
	 * @return
	 */
	public Object getResult(Map<String, Object> rowContext) {
		logger.debug("开始执行公式：" + this.getFormulaStr());

		List<Object> argList = new ArrayList<Object>(this.getArgs().size());
		argList.addAll(this.getArgs());// 不能直接用getArgs(),因为不能直接改变它，否则会出问题

		for(int i = 0; i < argList.size(); i++) {
			Object obj = argList.get(i);
			if(obj instanceof IFormula) {
				IFormula formula = (IFormula) obj;
				// 如果参数是子公式，则先执行子公式
				argList.set(i, formula.getResult(rowContext));
			} else {
				String t = obj == null ? "" : obj.toString().trim();
				if(t.startsWith("\"") && t.endsWith("\"")) {
					t = t.substring(1);
					t = t.substring(0, t.length() - 1);
					argList.set(i, t);
				} else {
					// 如果参数不是子公式，且不能转化为数值，则把变量翻译为值
					if(t.contains("+") || t.contains("+") || t.contains("+") || t.contains("+") || t.contains("(")
							|| t.contains(")")) {
						JexlEngine engine = new JexlEngine();
						JexlContext context = new MapContext(rowContext);
						Expression expression = engine.createExpression(t);
						argList.set(i, expression.evaluate(context));
					} else {
						argList.set(i, rowContext.get(t));
					}
				}
			}
		}

		// 进行计算
		if(argList.get(0) == null) {
			return null;
		} else {
			return String.valueOf(argList.get(0));
		}
	}

}