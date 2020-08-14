package org.nw.jf.ulw;

import org.nw.vo.pub.lang.UFBoolean;

/**
 * 处理ULW的checkbox
 * 
 * @author xuqc
 * @date 2012-6-7
 */
public class CheckboxRenderer implements IRenderer {

	/*
	 * 当v === true || v === 'true' || v=='Y' || v==1 )时，显示“是”，否则显示“否”
	 * 这个是与checkbox.js对应的
	 * 
	 * @see com.uft.webnc.jf.ulw.IRenderer#render(java.lang.Object,
	 * java.lang.String)
	 */
	public Object render(Object value, int datatype, String reftype) {
		if(value != null
				&& ("true".equalsIgnoreCase(value.toString()) || "Y".equalsIgnoreCase(value.toString()) || "1"
						.equalsIgnoreCase(value.toString()))) {
			return "是";
		}
		return "否";
	}

	public static void main(String[] args) {
		System.out.println(UFBoolean.valueOf(null).booleanValue());
		System.out.println(UFBoolean.valueOf(true).booleanValue());
		System.out.println(UFBoolean.valueOf("true").booleanValue());
		System.out.println(UFBoolean.valueOf("Y").booleanValue());
		System.out.println(UFBoolean.valueOf("1").booleanValue());
	}
}
