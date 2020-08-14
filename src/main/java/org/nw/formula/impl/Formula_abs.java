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
 * abs(num)求数num的绝对值
 * 
 * @author fangw
 */
public class Formula_abs extends AbstractFormula {

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
			}
		}

		// 返回结果
		Object obj = argList.get(0);
		if(obj != null) {
			if(obj.toString().length() > 0) {
				JexlEngine engine = new JexlEngine();
				JexlContext context = new MapContext(rowContext);
				Expression expression = engine.createExpression(obj.toString());
				Object value = expression.evaluate(context);
				if(value != null) {
					return Math.abs(Double.parseDouble(value.toString()));
				}
			}
		}
		return 0;
	}

}