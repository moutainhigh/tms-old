package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * int(str)
 * 
 * @author fangw
 */
public class Formula_int extends AbstractFormula {

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
				Object value = null;
				if(StringUtils.isDecimal(obj.toString())) {
					// 字符串是数字
					value = obj;
				} else {
					value = rowContext.get(obj);
				}

				if(value != null) {
					return Double.valueOf(value.toString()).intValue();
				}
			}
		}
		return 0;
	}
}