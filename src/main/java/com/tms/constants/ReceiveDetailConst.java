package com.tms.constants;

/**
 * 应收明细常量
 * 
 * @author xuqc
 * @date 2012-8-31 下午01:48:48
 */
public class ReceiveDetailConst {

	// 应收明细类型
	public final static int ORIGIN_TYPE = 0; // 原始类型
	public final static int OTHER_TYPE = 1; // 其他类型
	public final static int CUST_CLAIMANT_TYPE = 2; // 客户索赔

	/**
	 * 合并类型
	 * <p>
	 * 0、未合并 <br/>
	 * 1、父级 <br/>
	 * 2、子级
	 * </p>
	 * 
	 * @author xuqc
	 * 
	 */
	public enum MERGE_TYPE {
		UNMERGE(0), PARENT(1), CHILD(2);

		private MERGE_TYPE(Integer value) {
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

	/**
	 * 收款类型，直接收款，对账收款
	 * 
	 * @author xuqc
	 * 
	 */
	public enum RECEIVABLE_TYPE {
		DIRECT(0), CHECKSHEET(1);

		private RECEIVABLE_TYPE(Integer value) {
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
