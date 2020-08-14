package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * date()求日期date的天数
 * 
 * @author fangw
 */
public class Formula_date extends AbstractFormula {

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

		// 进行计算
		if(argList.get(0) == null) {
			return null;
		} else {
			return new Date();
		}
	}

}
