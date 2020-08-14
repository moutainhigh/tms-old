package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.MoneyUtils;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * getChineseCurrency获取中国式的金额大写
 * 
 * @author fangw
 */
public class Formula_getchinesecurrency extends AbstractFormula {

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
				try {
					Double.parseDouble(obj.toString());
				} catch(NumberFormatException e) {
					// 如果参数不是子公式，且不能转化为数值，则把变量翻译为值
					argList.set(i, rowContext.get(obj));
				}
			}
		}

		// 进行计算
		if(argList.get(0) == null) {
			return null;
		} else {
			if(argList.get(0).toString().startsWith("-")) {
				// return "<span style=\"color:red;\">负" +
				// MoneyUtils.toChinese(argList.get(0).toString().substring(1))+"</span>";
				return "负" + MoneyUtils.toChinese(argList.get(0).toString().substring(1));
			} else {
				return MoneyUtils.toChinese(argList.get(0).toString());
			}
		}
	}

}