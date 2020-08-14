package com.tms.constants;

/**
 * 地址档案常量类
 * 
 * @author xuqc
 * @date 2012-10-12 下午03:49:30
 */
public class AddressConst {

	// 始发地的标识
	public final static String START_ADDR_FLAG = "S";
	// 目的地的标识
	public final static String END_ADDR_FLAG = "E";

	/**
	 * 数据类型,BLOCK表示占位块
	 * 
	 * @author xuqc
	 * @date 2010-9-20
	 * @version $Revision$
	 */
	public enum ADDR_TYPE {
		TYPE1(1), TYPE2(2), TYPE3(3);

		private ADDR_TYPE(Integer value) {
			this.value = value;
		}

		public int intValue() {
			return this.value;
		}

		public boolean equals(Integer value) {
			if(this.value == value)
				return true;
			return false;
		}

		private int value;
	}
}
