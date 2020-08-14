package com.tms.constants;

/**
 * 异常事故常量类
 * 
 * @author xuqc
 * @date 2013-5-7 下午09:01:43
 */
public class ExpAccidentConst {
	/**
	 * 异常事故类型
	 * 
	 * @author xuqc
	 * 
	 */
	public enum ExpAccidentOrgin {
		TRACKING("异常跟踪"), POD("签收"), RECEIPT("回单"), 
		COMPLAINT("客户投诉"), OTHER("其他"),VENT("退单"),KPI("KPI考核");
		private String value;

		private ExpAccidentOrgin(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}
	
	public enum expAccidentType {
		
		QSYC("0"), HDYC("1"), PS("2"),QT("3"),HWDS("4"),
		FYYC("5"), SDYC("6"), CLPM("7"), KPI("8"), FKYC("9"), 
		KFJD("201"), FHDTH("202"),FHDTHDJ("203"),FHDDH("204"),
		FHDDHDJ("205"),QS("206"),QSDJ("207"),HD("208"),HDDJ("209"),
		HDJL("210"),FHDPZ("211"),
		
		WTDTH("212"), WTDTHDJ("213"), WTDDH("214"),WTDDHDJ("215"),
		
		RDQR("216"),PDQR("217");
		
		private String value;

		private expAccidentType(String value) {
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
