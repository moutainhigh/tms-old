package com.tms.constants;

/**
 * 应付明细常量
 * 
 * @author xuqc
 * @date 2012-8-31 下午01:48:48
 */
public class PayDetailConst {

	// 应付明细类型
	public final static int ORIGIN_TYPE = 0; // 原始类型
	public final static int OTHER_TYPE = 1; // 其他类型
	public final static int CARR_CLAIMANT_TYPE = 2; // 承运商索赔

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
	 * 分摊类型
	 * 
	 * @author xuqc
	 * 
	 */
	public enum PAY_DEVI_TYPE {
		FEE_WEIGHT("计费重"), WEIGHT("重量"), VOLUME("体积");
		private String value;

		private PAY_DEVI_TYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 应付明细的分摊维度
	 * 
	 * @author xuqc
	 * 
	 */
	public enum PAY_DEVI_DIMENSION {
		INVOICE("发货单"), DETAIL("发货单明细行");
		private String value;

		private PAY_DEVI_DIMENSION(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 付款类型，直接付款，对账付款
	 * 
	 * @author xuqc
	 * 
	 */
	public enum PAYABLE_TYPE {
		DIRECT(0), CHECKSHEET(1);

		private PAYABLE_TYPE(Integer value) {
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
