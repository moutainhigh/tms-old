package com.tms.constants;

/**
 * 数据字典常量
 * <p>
 * 这里定义的常量，在界面上是不能修改的，否则会出现不对应
 * </p>
 * 
 * @author xuqc
 * @date 2012-7-15 下午05:03:25
 */
public class DataDictConst {

	/**
	 * 地址类型，这里定义的地址类型跟数据字典中定义的地址类型还不是同一个。这个是在路线管理中使用的
	 * 
	 * @author xuqc
	 * 
	 */
	public enum ADDR_TYPE {
		CITY(0), ADDR(1),PRIV(2),AREA(3);

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
	/**
	 * 货品种类
	 * @author muyun
	 *
	 */
	public enum INVOICE_GOOD_TYPE {
		CS(1), DG(2),GG(3);

		private INVOICE_GOOD_TYPE(Integer value) {
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
	 * 路线类型
	 * 
	 * @author xuqc
	 * 
	 */
	public enum LINE_TYPE {
		YSLC(1), YSJH(2), XSLX(3), PZLX(4);

		private LINE_TYPE(Integer value) {
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
	 * 数据字典的数据类型，目前支持数字型和字符串
	 * 
	 * @author xuqc
	 * 
	 */
	public enum VALUETYPE {
		NUMBER(0), STRING(1);

		private VALUETYPE(Integer value) {
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
	 * 结算方式,月结,现金到付
	 * 
	 * @author xuqc
	 * 
	 */
	public enum BALATYPE {
		MONTH(0), ARRI_PAY(1);

		private BALATYPE(Integer value) {
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
	 * 发货单来源
	 * 
	 * @author xuqc
	 * 
	 */
	public enum INVOICE_ORIGIN {
		KFXD(0), BZDR(1), DSDR(2),FGS(3),MKDR(4),EDI(5),CC(6);

		private INVOICE_ORIGIN(Integer value) {
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

	//平台类型
	public enum PLATFORM_TYPE {
		TMS(1), RF(2);

		private PLATFORM_TYPE(Integer value) {
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
	 * 订单类型，正常订单、调拨单、手动单、委托单导入
	 * 
	 * @author xuqc
	 * 
	 */
	public enum ORDER_TYPE {
		ZCDD(1), DBD(2), SDD(3), WTDDR(4);

		private ORDER_TYPE(Integer value) {
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

	public enum TRANSPORT_TYPE {
		ERROR(0), LD(1), YD(2), DY(3), DD(4);

		private TRANSPORT_TYPE(Integer value) {
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
	 * 货品属性，好货、冻结、坏货
	 * 
	 * @author xuqc
	 * 
	 */
	public enum GOODS_PROP {
		OK(1), HOLD(2), DAMDAGE(3);

		private GOODS_PROP(Integer value) {
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
	 * 费用类型所属大类
	 * 
	 * @author xuqc
	 * 
	 */
	public enum EXPENSE_PARENTTYPE {
		THD(0), GXD(1), SHD(2);

		private EXPENSE_PARENTTYPE(Integer value) {
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
	
	public enum CARR_TYPE {
		ZY(1), WX(2), GT(3), WL(4), FGS(5);

		private CARR_TYPE(Integer value) {
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
	
	public enum CUST_TYPE {
		JH(1), FJH(2), FGS(3);

		private CUST_TYPE(Integer value) {
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
	
	public enum TRACK_ORIG {
		RF(1), APP(2), TMS(3), LBS(4);

		private TRACK_ORIG(Integer value) {
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
	
	public enum matter_type {
		
		FHDJD(11), FHDTH(12), FHDTHDJ(13),FHDDH(14),FHDDHDJ(15),
		FHDQS(16), FHDQSDJ(17),FHDHD(18),FHDHDDJ(19),FHDHDCJ(20),
		FHDPZ(21),
		
		WTDTH(31), WTDTHDJ(32), WTDDH(33),WTDDHDJ(34),
		
		RDQR(41),
		PDQR(51)
		
		;

		private matter_type(Integer value) {
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
	
	public enum limit_type {
		YWSX(1), CZSX(2);

		private limit_type(Integer value) {
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
	
	public enum KPI_CHECK_TYPE {
		THJS(1), DHJS(2),QSWZ(3), HDJS(4),HDWZ(5), HWPS(6),HWDS(7),KHTS(8);

		private KPI_CHECK_TYPE(Integer value) {
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
	
	public enum MERGE_RULE_PARAMTER_TYPE {
		CUST(1), PSNDOC(2),DEPT(3), IF_RETURN(4),IF_CUST(5), URGENT_LEVEL(6),INV_GOODS_TYPE(7),DELI(8),
		ARRI(9),GOODS_TYPE(10),ITEM_CODE(11), DELI_ORDER(12),TRANS_TYPE(13), DELI_CITY(14),DELI_A(15),
		DELI_P(16),ARRI_CITY(17),ARRI_A(18),ARRI_P(19);
		private MERGE_RULE_PARAMTER_TYPE(Integer value) {
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
