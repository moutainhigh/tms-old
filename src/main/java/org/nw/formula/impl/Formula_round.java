package org.nw.formula.impl;

import java.text.DecimalFormat;
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
 * round(double num,int index)对num保留index位小数（四舍五入）
 * 
 * @author fangw
 */
public class Formula_round extends AbstractFormula {

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

				boolean grouping = false;
				Object value1 = null;
				try {
					if(StringUtils.isDecimal(obj.toString().replace(",", ""))) {
						value1 = Double.parseDouble(obj.toString().replace(",", ""));
						if(obj.toString().contains(",")) {
							grouping = true;
						}
					}
				} catch(NumberFormatException e) {
					logger.warn("转为double错误，可能包含复合公式，下面将进行其它尝试，本错误将被忽略！", e);
				}
				if(value1 == null) {
					// 使用apache公式解析器进行解析
					Expression expression = engine.createExpression(obj.toString());
					value1 = expression.evaluate(context);
					if(value1 == null) {
						// 值如果还为空直接返回，不用再算了
						return null;
					}
					if(value1.toString().contains(",")) {
						grouping = true;
					}
				}

				Expression expression = engine.createExpression(argList.get(1).toString());
				Object value2 = expression.evaluate(context);

				Double num = Double.parseDouble(value1.toString().replace(",", ""));
				Integer index = Integer.parseInt(value2.toString());

				DecimalFormat df = new DecimalFormat();
				df.setMinimumFractionDigits(index);
				df.setMaximumFractionDigits(index);
				df.setGroupingUsed(grouping);

				return df.format(num);
			}
		}
		return 0;
	}

}