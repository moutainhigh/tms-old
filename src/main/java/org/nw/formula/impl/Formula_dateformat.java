package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.DateUtils;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * dateFormat(date, pattern)用于将时间格式化为期望的字符串
 * 其中date可以是时间字符串,也可以是Date对象,pattern为格式化参数
 * ,yyyy表示年,MM表示月,dd表示天数,HH表示小时,mm表示分钟,ss表示秒.比如dateFormat("2006-07-04 12:12:12",
 * "日期:yyyy-MM-dd HH:mm:ss") 将返回"日期:2006-07-04 12:12:12".
 * 
 * @author fangw
 */
public class Formula_dateformat extends AbstractFormula {

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
				if(!obj.toString().startsWith("\"") && !obj.toString().endsWith("\"")) {
					// 把变量翻译为值
					argList.set(i, rowContext.get(obj));
				}
			}
		}

		// 进行计算
		if(argList.get(0) == null || argList.get(1) == null) {
			return null;
		} else {
			String str = null;
			String fmt = argList.get(1).toString();

			if(argList.get(0) instanceof Date) {
				Date date = (Date) argList.get(0);
				str = DateUtils.formatDate(date, fmt);
			} else {
				str = DateUtils.formatDate(argList.get(0).toString(), fmt);
			}
			return str;
		}
	}

}
