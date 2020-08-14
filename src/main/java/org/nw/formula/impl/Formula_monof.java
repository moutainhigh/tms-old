package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.DateUtils;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * month(date)得到指定日期内的月份
 * 
 * @author fangw
 */
public class Formula_monof extends AbstractFormula {

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
					new UFDateTime(obj.toString());
				} catch(Exception e) {
					// 如果参数不是子公式，且不能转化为数值，则把变量翻译为值
					argList.set(i, rowContext.get(obj));
				}
			}
		}

		// 进行计算
		if(argList.get(0) == null) {
			return null;
		} else {
			Integer month = 0;
			if(argList.get(0) instanceof UFDate) {
				UFDate date = (UFDate) argList.get(0);
				month = DateUtils.getMonth(date.toDate());
			} else if(argList.get(0) instanceof UFDateTime) {
				UFDateTime date = (UFDateTime) argList.get(0);
				month = DateUtils.getMonth(date.getDate().toDate());
			} else if(argList.get(0) instanceof Date) {
				Date date = (Date) argList.get(0);
				month = DateUtils.getMonth(date);
			} else if(argList.get(0) instanceof String) {
				String date = (String) argList.get(0);
				month = DateUtils.getMonth(date);
			}
			String str = month.toString();
			if(str.length() == 1) {
				str = "0" + str;
			}
			return str;
		}
	}

}