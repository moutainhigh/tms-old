package com.nw.test;

import org.nw.vo.pub.lang.UFDouble;

/**
 * 
 * @author xuqc
 * @date 2014-4-25 下午09:56:13
 */
public class JDKTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UFDouble a = new UFDouble(24.5);
		UFDouble b = new UFDouble(5);

		System.out.println(a.div(b).setScale(0, UFDouble.ROUND_FLOOR));
		// String s = "我们我们我们";
		// try {
		// System.out.println(s.getBytes("iso8859-1").length);
		// } catch(UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// for(int i = 0; i < 100; i++) {
		// System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
		// }
	}

}
