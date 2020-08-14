package org.nw.jf.ulw;

import org.nw.jf.UiConstants;

/**
 * 对模板中的时间戳做处理
 * 
 * @author xuqc
 * @date 2013-4-24 下午11:12:06
 */
public class DateTimeRenderer implements IRenderer {

	public Object render(Object value, int datatype, String reftype) {
		// 对时间戳类型做特殊处理，主要是提货日期和收货日期
		if(datatype == UiConstants.DATATYPE.TIMESTAMP.intValue()) {
			if(value != null && value.toString().length() > 16) {
				value = value.toString().substring(0, 16);
			}
		}
		return value;
	}

}
