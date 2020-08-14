package com.tms.constants;

/**
 * 交易表的交易类型
 * 
 * @author xuqc
 * @date 2014-3-18 下午11:13:55
 */
public class TransactionConst {

	/**
	 * 交易类型：收货,在页面显示IN；发货,在页面显示OUT,
	 * 
	 * @author xuqc
	 * 
	 */
	public enum TRANS_TYPE {
		IN("IN"), OUT("OUT"), AJUST("AJUST");
		private String value;

		private TRANS_TYPE(String value) {
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
	 * 状态：页面上显示OK，HOLD
	 * 
	 * @author xuqc
	 * 
	 */
	public enum STATUS {
		OK("OK"), HOLD("HOLD");
		private String value;

		private STATUS(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}
}
