package org.nw.formula;

import org.nw.exception.BusiException;

public class FormulaUtils {

	/**
	 * 判断是否四则运算
	 * 
	 * @param formula
	 * @return
	 */
	public static boolean isArithmetic(final String formula) {
		if(formula.contains("+") || formula.contains("-") || formula.contains("*") || formula.contains("/")) {
			// 包含四则运算,arithmetic
			if(formula.indexOf('(') != -1 && formula.indexOf('(') > 0) {
				// 如：iif(indcode==null, null, indcode+" "+indname)
				// 判断整个是否是个公式，否则认为是四则运算（也可能包含子公式）

				// 判断是否是公式：1、左括号之前的必须是公式名，2、右括号必须在最后
				String str = formula.substring(0, formula.indexOf('('));
				if(!str.contains("+") && !str.contains("-") && !str.contains("*") && !str.contains("/")) {
					// 如：getcolvalue(syys_contractbill,
					// vcontractcode,pk_contractbill,pk_contractbill) + ""
					// +getcolvalue(syys_contractbill, vcontractname,
					// pk_contractbill,pk_contractbill)

					// 第一个括号对应的右括号，如果是最后一个字符，则是公式，不是四则运算
					int _start = formula.indexOf('(');// 起始位置
					int _end = -1;// 待定位
					int count = 1;// 左括号的个数，初始为1，意义为有一个左括号
					// 定位右括号结束位置
					for(int index = _start + 1; index < formula.length(); index++) {
						if(formula.charAt(index) == '(') {
							count++;
						} else if(formula.charAt(index) == ')') {
							count--;
							_end = index;
						}
						if(count == 0) {
							break;
						}
					}

					if(count != 0) {
						throw new BusiException("公式解析错误[?]！",formula);
					}

					if((_end + 1) == formula.length()) {
						return false;// 不是四则运算
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 解析函数名称 如：iif(indcode==null, null, indcode+" "+indname)
	 * 
	 * @param formula
	 * @return
	 */
	public static String getFormulaName(final String formula) {
		String retStr = null;
		if(isArithmetic(formula)) {
			retStr = "arithmetic";
		} else {
			if(formula.indexOf("(") != -1) {
				retStr = formula.substring(0, formula.indexOf("("));
			} else {
				retStr = "_get";
			}
		}
		return retStr;
	}

}
