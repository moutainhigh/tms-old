package org.nw.formula;

import java.util.List;
import java.util.Map;

/**
 * 公式接口 公式抽象类实现了该接口，则所有公式都具有次接口
 * 
 * @author fangw
 */
public interface IFormula {
	/**
	 * 添加到参数列表
	 * 
	 * @param args
	 *            这里添加的可能是一个字符串型参数，也可能是一个IFromula型公式(子公式场景)
	 */
	public void addArgs(Object args);
	

	/**
	 * 获取公式名称 注：解析并不需要，目前只是为了调试需要
	 * 
	 * @return
	 */
	public String getFormulaName();

	/**
	 * 获取公式字符串 注：解析并不需要，目前只是为了调试需要
	 * 
	 * @return
	 */
	public String getFormulaStr();

	/**
	 * 获取公式输出参数名称 即：->前面的那个参数名称
	 * 
	 * @return
	 */
	public String getOutArgName();

	/**
	 * 获取公式执行结果 可以是List<? extends
	 * CircularlyAccessibleValueObject>,也可以是List<Map<String,Object>>
	 * 
	 * @param context
	 * @return
	 */
	public Object getResult(Map<String, Object> context);

	/**
	 * 设置公式字符串 注：解析并不需要，目前只是为了调试需要
	 * 
	 * @param formulaStr
	 */
	public void setFormulaStr(String formulaStr);

	/**
	 * 输出参数名称
	 * 
	 * @param outArgName
	 */
	public void setOutArgName(String outArgName);

	public FormulaDao getDao();

	public void setDao(FormulaDao dao);

	//yaojiie 2015 12 08 添加获取所有结果的接口
	public void getAllResults(List<Map<String,Object>> contexts,IFormula formula);
	
}
