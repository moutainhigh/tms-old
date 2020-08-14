package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * substring(str,int) substring(str,int,int) 截取字符串
 * 
 * @author xuqc
 */
public class Formula_substring extends AbstractFormula {

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
		if(argList.get(0) != null) {
			if(argList.get(0).toString().startsWith("\"") && argList.get(0).toString().endsWith("\"")) {
				String t = argList.get(0).toString();
				t = t.substring(1);
				t = t.substring(0, t.length() - 1);
				argList.set(0, t);
			} else {
				argList.set(0, rowContext.get(argList.get(0)));
			}
		}
		if(argList.size() == 2) {
			// substring(str,int)的情况
			if(argList.get(0) == null || argList.get(0).toString().length() == 0) {
				return "";
			}
			String t = argList.get(0).toString();
			int index = 0;
			try {
				index = Integer.parseInt(argList.get(1).toString());
			} catch(Exception e) {
				logger.error("公式参数错误，参数值：" + argList.get(1));
				return argList.get(0);
			}
			return t.substring(index);
		} else if(argList.size() == 3) {
			// substring(str,int,int)的情况
			if(argList.get(0) == null || argList.get(0).toString().length() == 0) {
				return "";
			}
			String t = argList.get(0).toString();
			int start = 0;
			try {
				start = Integer.parseInt(argList.get(1).toString());
			} catch(Exception e) {
				logger.error("公式参数错误，参数值：" + argList.get(1));
				return argList.get(0);
			}
			int end = 0;
			try {
				end = Integer.parseInt(argList.get(2).toString());
			} catch(Exception e) {
				logger.error("公式参数错误，参数值：" + argList.get(2));
				return argList.get(0);
			}
			return t.substring(start, end);
		} else {
			logger.error("公式格式不正确，公式：" + this.getFormulaStr());
			return argList.get(0);
		}
	}

}