package com.tms.constants;

import org.nw.constants.FunRegisterConst;

/**
 * 于业务相关的功能编码，如果是平台的功能编码，参见com.tms.constants.FunRegisterConst
 * 
 * @author xuqc
 * @date 2013-8-23 上午10:00:04
 */
public interface FunConst extends FunRegisterConst {

	// 区域档案的功能编码
	public final static String AREA_FUN_CODE = "t103";

	// 地址管理的功能编码
	public final static String ADDRESS_FUN_CODE = "t10502";
	// 货品管理的功能编码
	public final static String GOODS_FUN_CODE = "t10903";
	// 发货单的功能编码
	public final static String INVOICE_CODE = "t201";
	// 运段的功能编码
	public final static String SEG_CODE = "t303";
	// 运段明细的功能编码
	public final static String SEG_PACK_CODE = "t303";
	// 运输计划中配载的功能编码
	public final static String SEG_PZ_CODE = "t30224";
	// 运输计划中批量配载的功能编码
	public final static String SEG_BATCH_PZ_CODE = "t30226";
	// 费用管理中的应收对账单的功能编码
	public final static String RECE_CHECK_SHEET_CODE = "t405";
	// 费用管理中的应收明细的功能编码
	public final static String RECEIVE_DETAIL_CODE = "t402";

	// 费用管理中的应付对账单的功能编码
	public final static String PAY_CHECK_SHEET_CODE = "t406";
	// 费用管理中的应付明细的功能编码
	public final static String PAY_DETAIL_CODE = "t403";

	// 应收明细收款纪录
	public final static String RECE_RECORD_CODE = "t480";
	// 应收对账收款纪录
	public final static String RECE_CHECK_SHEET_RECORD_CODE = "t481";
	// 应付明细付款纪录
	public final static String PAY_RECORD_CODE = "t490";
	// 应付对账付款纪录
	public final static String PAY_CHECK_SHEET_RECORD_CODE = "t491";
	// 异常事故
	public final static String EXP_ACCIDENT_CODE = "t503";

	// 委托单异常跟踪,注意区分于异常跟踪
	public final static String ENT_TRACKING_CODE = "t510";
	// 委托单功能编码
	public final static String ENTRUST_CODE = "t501";
	
	//委托单运力信息导入
	public final static String ENTRUST_TRANS_CODE = "t511";

	// 车辆档案
	public final static String CAR_CODE = "t10704";
	
	// pod 签单附件
	public final static String SIGN_ATTACH_CODE = "t603";
	
	// pod 回单附件
	public final static String RECEIPT_ATTACH_CODE = "t604";
	
	//yaojiie 2015 11 27 添加自定义导入按钮编码
	public final static String CUSTOM_IMPORT_CODE = "t205";
	
	//yaojiie 2015 12 08 异常跟踪附件
	public final static String TRACK_ATTACH_CODE = "t512";
	
	//yaojiie 2015 12 18 milkrun导入
	public final static String MILKRUN_IMPORT_CODE = "t207";
	
	public final static String COLDCHAIN_IMPORT_CODE = "t208";
	
	//yaojiie 2015 12 28 导入批次费用
	public final static String PAYDETAIL_LOT_IMPORT_CODE = "t4031";
	
	//yaojiie 2016 1 15 异常附件
	public final static String EXP_ATTACH_CODE = "t514";
	
	
	
	//yaojiie 2016 3 3 应收附件
	public final static String RD_ATTACH_CODE = "t4021";
		
	//yaojiie 2016 3 3 应付附件
	public final static String PD_ATTACH_CODE = "t4033";
		
	//yaojiie 2016 3 3 应收对账附件
	public final static String RCS_ATTACH_CODE = "t4051";
		
	//yaojiie 2016 3 3 应付对账附件
	public final static String PCS_ATTACH_CODE = "t4061";
	
	//加油管理
	public final static String FLEET_REFUEL_CODE = "t902";
}
