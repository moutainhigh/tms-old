package com.tms.constants;

/**
 * 单据类型常量
 * 
 * @author xuqc
 * @date 2012-8-14 下午10:30:56
 */
public class BillTypeConst {

	public static final String FHD = "FHD"; // 发货单的单据类型

	public static final String YDPZ = "YDPZ"; // 运段配载的单据类型
	public static final String WTD = "WTD"; // 委托单的单据类型
	public static final String YSMX = "YSMX"; // 应收明细的单据类型
	public static final String YSDZ = "YSDZ"; // 应收对账的单据类型
	public static final String YFMX = "YFMX"; // 应付明细的单据类型
	public static final String YFDZ = "YFDZ"; // 应付对账的单据类型
	public static final String YCSG = "YCSG"; // 异常事故的单据类型
	public static final String ATM = "ATM";// 辅助工具管理

	public static final String INSTO = "INSTO";// 入库单
	public static final String OUTSTO = "OUTSTO";// 出库单
	// 交易表需要生成交易号，这里定义一个单据类型，只是为了生成单据号使用
	public static final String TRANS = "TRANS";
	// 批次表需要批次号，这里定义一个单据类型，只是为了生成单据号使用
	public static final String LOT = "LOT";
	// 分配明细表，定义一个单据类型，只是为了生成单据号使用
	public static final String PICK_DETAIL = "PICK";
	// 库内调整单
	public static final String AJUST = "AJUST";
	// 订单批次表
	public static final String ORDERLOT = "LOT";
	// 订单批次表
	public static final String BATORDERLOT = "BAT";
	// POD  2015-11-24 yaojiie
	public static final String POD = "POD";
	// 签单 主要用于POD附件上传 2015-11-24 yaojiie
	public static final String POD_SIGN = "POD_SIGN";
	// 回单 主要用于POD附件上传 2015-11-24 yaojiie
	public static final String POD_RECEIPT = "POD_RECEIPT";
	// 跟踪附件 主要用于异常跟踪附件上传 2015-12-08 yaojiie
	public static final String TRACKING = "TRACKING";
	//yaojiie 2015 12 16 加油管理单据类型 报账管理单据类型 
	public static final String REF = "REF";
	public static final String REM = "REM";
	//yaojiie 2015 12 16 路桥费管理单据类型 
	public static final String TOLL = "TOLL";
	//yaojiie 2015 12 16 保险费管理单据类型 
	public static final String INS = "INS";
	//yaojiie 2015 12 16 保养管理单据类型 
	public static final String MAT = "MAT";
	//yaojiie 2015 12 16 维修记录管理单据类型 
	public static final String REP = "REP";
	
	//yaojiie 2015 1 15 异常附件
	public static final String EXP = "EXP";
	
	//yaojiie 2016 3 3 应收附件
	public static final String RD = "RD";
	
	//yaojiie 2016 3 3 应付附件
	public static final String PD = "PD";
	
	//yaojiie 2016 3 3 应收对账附件
	public static final String RCS = "RCS";
	
	//yaojiie 2016 3 3 应付对账附件
	public static final String PCS = "PCS";
		
	//yaojiie 2016 3 12 时效管理
	public static final String LIMT = "LIMT";	
    
	//用车管理
	public static final String YCGL = "YCGL";
	
	//年审管理
	public static final String ANN = "ANN";	
		
	//违章管理
	public static final String VIO = "VIO";	
	
	//司机培训管理
	public static final String SJPX = "SJPX";	
		
	//轮胎管理
	public static final String TYRE = "TYRE";		
}
