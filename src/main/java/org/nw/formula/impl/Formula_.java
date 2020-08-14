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
 * 通用公式，如果没有找到任何公式，则用此公式执行，如逻辑判断、四则运算
 * 
 * @author fangw
 */
public class Formula_ extends AbstractFormula {

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
				// 使用了apache的Commons JEXL后，就不需要这样子了
				// try{
				// Double.parseDouble(obj.toString());
				// }catch(NumberFormatException e){
				// // 如果参数不是子公式，且不能转化为数值，则把变量翻译为值
				// argList.set(i, rowModel.get(obj));
				// }
			}
		}

		// 进行计算
		StringBuilder sb = new StringBuilder();
		for(Object obj : argList) {
			if(obj != null && obj instanceof BigDecimal) {
				sb.append(((BigDecimal) obj).toPlainString());
			} else {
				sb.append(obj);
			}
		}

		// 解析表达式
		JexlEngine engine = new JexlEngine();
		JexlContext context = new MapContext(rowContext);
		Expression expression = engine.createExpression(sb.toString());
		return expression.evaluate(context);
	}
}