package org.nw.jf.ulw;

import java.math.BigDecimal;

import org.nw.jf.utils.UiTempletUtils;

/**
 * 处理ULW中的数字类型数据
 * 
 * @author xuqc
 * @date 2012-5-11
 */
public class NumberRenderer implements IRenderer {

	/*
	 * <p>数字类型的数值，返回指定的格式</p>
	 * 
	 * @see com.uft.webnc.service.IULWRenderer#render(java.lang.Object)
	 */
	public Object render(Object value, int datatype, String reftype) {
		if(value == null)
			value = 0;
		// 2013-8-4 xuqc 数字类型不需要再格式化了，避免导出时变成string类型
		int precision = UiTempletUtils.getPrecision(reftype);
		BigDecimal bd = new BigDecimal(value.toString());
		bd = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	public static void main(String[] args) {
		// String format = "#,##0.00";
		// DecimalFormat df = new DecimalFormat(format);
		// System.out.println(df.format(56234200.2));
		// System.out.println(df.format(0.2));
		BigDecimal bd = new BigDecimal(200.1);
		bd = bd.setScale(5, BigDecimal.ROUND_HALF_UP);
		System.out.println(bd.doubleValue());
	}
}
