package com.tms.constants;

/**
 * 计费规则<br/>
 * 对于客户：控制发货单在匹配合同时若出现相同类型的费用明细时系统可取其中的一条费用类型保留。<br/>
 * 对于承运商：控制调度配载、委托单在匹配合同时若出现相同类型的费用明细时系统可取其中的一条费用类型保留。<br/>
 * 
 * @author xuqc
 * @Date 2015年5月17日 上午11:06:09
 *
 */
public class BillingRuleConst {

	public static final int MAX = 1;// 取大

	public static final int MIN = 2;// 取小
}
