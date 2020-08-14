package com.tms;

import java.util.HashMap;
import java.util.Map;

/**
 * 单据状态
 * 
 * @author xuqc
 * @date 2012-8-5 下午10:07:41
 */
public class BillStatus extends org.nw.BillStatus {

	// 发货单状态
	public static int INV_CONFIRM = 1; // 已确认
	public static int INV_DELIVERY = 2;// 已提货
	public static int INV_ARRIVAL = 3; // 已到货
	public static int INV_SIGN = 4;// 已签收
	public static int INV_PART_SIGN = 5; // 部分签收
	public static int INV_BACK = 6;// 已回单
	public static int INV_PART_DELIVERY = 7;// 部分提货
	public static int INV_PART_ARRIVAL = 8;// 部分到货
	public static int INV_CLOSE = 9;// 关闭

	public static Map<Integer, String> inv_status_map = new HashMap<Integer, String>();
	static {
		inv_status_map.put(NEW, "新建");
		inv_status_map.put(INV_CONFIRM, "已确认");
		inv_status_map.put(INV_DELIVERY, "已提货");
		inv_status_map.put(INV_ARRIVAL, "已到货");
		inv_status_map.put(INV_SIGN, "已签收");
		inv_status_map.put(INV_BACK, "已回单");
		inv_status_map.put(INV_PART_DELIVERY, "部分提货");
		inv_status_map.put(INV_PART_ARRIVAL, "部分到货");
		inv_status_map.put(INV_CLOSE, "关闭");
	}

	// 运段状态
	public static int SEG_WPLAN = 10; // 待调度 = 新建
	public static int SEG_DISPATCH = 11;// 已调度
	public static int SEG_DELIVERY = 12;// 已提货
	public static int SEG_ARRIVAL = 13; // 已到货
	public static int SEG_CLOSE = 14; // 关闭

	// 委托单状态
	public static int ENT_UNCONFIRM = NEW;// 待确认
	public static int ENT_CONFIRM = 21;// 已确认
	public static int ENT_DELIVERY = 22;// 已提货
	public static int ENT_ARRIVAL = 23; // 已到货
	public static int ENT_VENT = 24; // 变更

	// 应收明细
	public static int RD_CONFIRM = 31;// 已确认
	public static int RD_CHECK = 32;// 已对账
	public static int RD_PART_CAVLOAN = 33; // 部分核销
	public static int RD_CAVLOAN = 34;// 已核销
	public static int RD_CONFIRMING = 35;// 确认中
	public static int RD_CLOSE = 36;// 关闭

	// 应付明细
	public static int PD_CONFIRM = 41;// 已确认
	public static int PD_CHECK = 42;// 已对账
	public static int PD_PART_CAVLOAN = 43; // 部分核销
	public static int PD_CAVLOAN = 44;// 已核销
	public static int PD_CONFIRMING = 45;// 确认中
	public static int PD_CLOSE = 46;// 关闭

	// 应收对账
	public static int RCS_CONFIRM = 51;// 已确认
	public static int RCS_PART_CAVLOAN = 53; // 部分核销
	public static int RCS_CAVLOAN = 54;// 已核销

	// 应付对账
	public static int PCS_CONFIRM = 61;// 已确认
	public static int PCS_PART_CAVLOAN = 63; // 部分核销
	public static int PCS_CAVLOAN = 64;// 已核销

	// 异常事故
	public static int EA_NEW = NEW;//
	public static int EA_WHANDLE = 71;// 待处理
	public static int EA_HANDLING = 72;// 处理中
	public static int EA_HANDLED = 73;// 已结案
	public static int EA_CLOSED = 74;// 已关闭

	// 辅助工具管理
	public static int ATM_OPEN = NEW;// 开放
	public static int ATM_APPROVE = 81;// 已审核
	public static int ATM_GRANT = 82;// 已发放
	public static int ATM_RETURN = 83;// 已回收

	// 入库单状态，入库单子表也可以使用
	public static int INSTO_NEW = NEW;
	public static int INSTO_PART_REC = 92;// 部分收货
	public static int INSTO_ALL_REC = 94;// 收货完成
	public static int INSTO_ADDED = 96;// 上架
	public static int INSTO_CLOSED = 98;// 关闭

	// 出库单状态，出库单子表也可以使用
	public static int OUTSTO_NEW = NEW;
	public static int OUTSTO_PART_PICK = 102;// 部分分配
	public static int OUTSTO_PICKED = 104;// 分配完成
	public static int OUTSTO_PART_SHIP = 106;// 部分发货
	public static int OUTSTO_SHIPED = 108;// 发货完成
	public static int OUTSTO_CLOSED = 110;// 关闭

	// 调整单状态
	public static int AJUST_NEW = NEW;// 新建
	public static int AJUST_CONFIRM = 112;// 确认
	
	
	//yaojiie 2015 12 17 车队管理
	public static int REM_NEW = NEW;// 新建 报账
	public static int REM_CONFIRM = 116;// 确认
	
	public static int TOLL_NEW = NEW;// 新建 路桥费
	public static int TOLL_CONFIRM = 118;// 确认
	
	public static int MAT_NEW = NEW;// 新建 保养
	public static int MAT_CONFIRM = 120;// 确认
	
	public static int INS_NEW = NEW;// 新建 保险
	public static int INS_CONFIRM = 122;// 确认
	
	public static int REP_NEW = NEW;// 新建 维修记录
	public static int REP_CONFIRM = 124;// 确认
	
	public static int REF_NEW = NEW;// 新建 加油
	public static int REF_CONFIRM = 126;// 确认
	
	public static int YCGL_NEW = NEW;// 新建 用车管理
	public static int YCGL_CONFIRMING = 128;// 确认中
	public static int YCGL_CONFIRM = 130;// 确认
	public static int YCGL_REFUSE = 132;// 拒绝
	public static int YCGL_SEND = 134;//已派车
	public static int YCGL_DIS = 136;//出车
	public static int YCGL_RET = 138;//收车
	
	public static int ANN_NEW = NEW;// 新建  年审
	public static int ANN_CONFIRM = 140;// 确认
	
	public static int VIO_NEW = NEW;// 新建 违章
	public static int VIO_CONFIRM = 142;// 确认
	
	public static int SJPX_NEW = NEW;// 新建 司机培训
	public static int SJPX_CONFIRM = 144;// 确认
	
	public static int TYRE_NEW = NEW;// 新建 轮胎
	public static int TYRE_CONFIRM = 146;// 确认
	}
