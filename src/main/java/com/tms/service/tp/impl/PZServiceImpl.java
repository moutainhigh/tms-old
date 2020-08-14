package com.tms.service.tp.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.utils.BillnoHelper;
import org.nw.utils.FormulaHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.AddressConst;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.OperateTypeConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CarrService;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.inv.InvoiceService;
import com.tms.service.te.impl.EntrustUtils;
import com.tms.service.tp.PZService;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.rf.EntLineBarBVO;
import com.tms.vo.rf.InvPackBarBVO;
import com.tms.vo.te.EntInvBVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntTransHisBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 
 * @author xuqc
 * @date 2012-9-12 下午09:38:04
 */
@Service
public class PZServiceImpl extends TMSAbsBillServiceImpl implements PZService {

	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private ContractService contractService;

	@Autowired
	private ExpenseTypeService expenseTypeService;
	
	@Autowired
	private CarrService carrService;
	

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggEntrustVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PZHeaderVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, EntLineBVO.PK_ENTRUST);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, SegmentVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, "pk_segment");
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_segment");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_segment");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, EntLineBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, EntLineBVO.PK_ENTRUST);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_line_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_line_b");

			VOTableVO childVO3 = new VOTableVO();
			childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.HEADITEMVO, EntTransbilityBVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.PKFIELD, EntTransbilityBVO.PK_ENTRUST);
			childVO3.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_transbility_b");
			childVO3.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_transbility_b");

			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, PayDetailBVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, EntLineBVO.PK_ENTRUST);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_pay_detail_b");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_pay_detail_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1, childVO3, childVO2 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.WTD;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
//				if(fieldVO.getItemkey().equals("carno")) {
//					// 车牌号，因为更新车牌号后会更新到承运商，需要重新计算总计费重,并且重新匹配合同、重新计算总金额
//					fieldVO.setUserdefine1("refreshPayDetail()");
//				} 
				if (fieldVO.getItemkey().equals("carno")) {
					// 更改车牌号，修改司机参照
					fieldVO.setUserdefine1("afterChangeCarno(field,value,originalValue)");
				} else if (fieldVO.getItemkey().equals("pk_carrier")) {
					// 要求承运商，修改司机和车牌号参照
					fieldVO.setUserdefine1("afterChangeCarrier(field,value,originalValue)");
				} else if(fieldVO.getItemkey().equals("pk_trans_type")) {
					// 运输方式，需要重新计算总计费重,并且重新匹配合同、重新计算总金额
					fieldVO.setUserdefine1("updateHeaderFeeWeightCount();refreshPayDetail()");
				} else if(fieldVO.getItemkey().equals("pk_carrier") || fieldVO.getItemkey().equals("pk_car_type")) {
					// 承运商、运输方式、重新匹配合同、重新计算总金额
					fieldVO.setUserdefine1("afterEditPk_carrier(field,value,originalValue);updateHeaderFeeWeightCount();refreshPayDetail();");
				} else if (fieldVO.getItemkey().equals("req_deli_date")) {
					// 要求提货日期
					fieldVO.setUserdefine1("afterChangeReq_deli_date(field,value,originalValue)");
				} else if (fieldVO.getItemkey().equals("req_arri_date")) {
					// 要求收货日期
					fieldVO.setUserdefine1("afterChangeReq_arri_date(field,value,originalValue)");
				}
			} else if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getTable_code().equals(TabcodeConst.TS_ENT_LINE_B)) {
					// 路线信息这个tab
					if(fieldVO.getItemkey().equals("contact")) {
						// 提货联系人，参照地址档案联系人，ts_addr_contact
						fieldVO.setUserdefine3("pk_address:${record.get('pk_address')}");
					} else if(fieldVO.getItemkey().equals("req_leav_date")) {
						// 要求离开时间
						fieldVO.setUserdefine1("afterEditReq_leav_date(record,row)");
					} else if(fieldVO.getItemkey().equals("req_arri_date")) {
						// 要求离开时间
						fieldVO.setUserdefine1("afterEditReq_arri_date(record,row)");
					}
				} else if(fieldVO.getTable_code().equals(TabcodeConst.TS_PAY_DETAIL_B)) {// 费用明细这个tab
					if(fieldVO.getItemkey().equals("quote_type") || fieldVO.getItemkey().equals("valuation_type")
							|| fieldVO.getItemkey().equals("price")) {
						fieldVO.setUserdefine1("afterEditQuoteTypeOrValuationTypeOrPrice(record)");
					} else if(fieldVO.getItemkey().equals("expense_type_name")) {
						// 费用类型
						fieldVO.setUserdefine1("afterEditExpenseTypeName(record)");
					} else if(fieldVO.getItemkey().equals("amount")) { // 编辑金额时，更新总金额
						fieldVO.setUserdefine1("updateHeaderCostAmount()");
					}
				}
			}
		}
		// 增加行操作列，包括展开的图标
		BillTempletBVO processor = new BillTempletBVO();
		processor.setItemkey("_processor");
		processor.setDefaultshowname("操作");
		processor.setDatatype(UiConstants.DATATYPE.TEXT.intValue());
		processor.setListflag(Constants.YES);
		processor.setCardflag(Constants.YES);
		processor.setListshowflag(new UFBoolean(true));
		processor.setShowflag(Constants.YES);
		processor.setEditflag(Constants.NO);
		processor.setLockflag(Constants.NO);
		processor.setReviseflag(new UFBoolean(false));
		processor.setTotalflag(Constants.NO);
		processor.setNullflag(Constants.NO);
		processor.setWidth(30);
		processor.setDr(0);
		processor.setPos(UiConstants.POS[1]); // 这个字段是设置到表体的
		processor.setTable_code(TabcodeConst.TS_SEGMENT);
		processor.setPk_billtemplet(templetVO.getTemplateID());
		processor.setPk_billtemplet_b(UUID.randomUUID().toString());
		templetVO.getFieldVOs().add(0, processor);
		return templetVO;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		values.put("balatype", 0); // 结算方式默认月结
		return values;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyDefaultValues(ParamVO paramVO, Map<String, Object> headerMap) {
		Map<String, Object> valuesMap = super.getBodyDefaultValues(paramVO, headerMap);
		Map<String, Object> tabValuesMap = (Map<String, Object>) valuesMap.get(TabcodeConst.TS_PAY_DETAIL_B);
		
		//判断为空不赋值 songf  2015-11-07
		if(tabValuesMap != null){
			tabValuesMap.put("price_type", 0); // 价格类型，默认“区间”，表体的下拉框在设置默认值时有bug，设置的是text
			tabValuesMap.put("quote_type", 0);// 报价类型，默认“单价”
		}
	
		return valuesMap;
	}

	public SegmentVO[] querySegmentByPKs(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			return null;
		}
		String cond = NWUtils.buildConditionString(pk_segment);
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + cond
				+ " and isnull(dr,0)=0");
		return segVOs;
	}

	public List<Map<String, Object>> loadSegmentByPKs(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			return new ArrayList<Map<String, Object>>();
		}
		String cond = NWUtils.buildConditionString(pk_segment);
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + cond
				+ " and isnull(dr,0)=0");
		/**
		 * 4.转换数据格式，准备执行公式
		 */
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(segVOs.length);
		for(SuperVO vo : segVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}

		/**
		 * 6.执行公式
		 */
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.SEG_PZ_CODE);
		paramVO.setBody(true);
		paramVO.setTabCode(TabcodeConst.TS_SEGMENT);
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(this.getBillTemplateID(paramVO));
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(), paramVO
				.getTabCode().split(","), null);
		return list;
	}

	public List<Map<String, Object>> loadLineInfo(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			return new ArrayList<Map<String, Object>>();
		}
		String cond = NWUtils.buildConditionString(pk_segment);
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + cond
				+ " and isnull(dr,0)=0 order by req_arri_date");
		String addr_sql = "select addr_name from ts_address WITH(NOLOCK) where pk_address=?";
		List<EntLineBVO> startLineVOs = new LinkedList<EntLineBVO>(); // 起始地点集合
		List<EntLineBVO> endLineVOs = new LinkedList<EntLineBVO>(); // 目的地点集合
		for(SegmentVO segVO : segVOs) {
			EntLineBVO startLineVO = new EntLineBVO();
			startLineVO.setPk_address(segVO.getPk_delivery());
			startLineVO.setPk_city(segVO.getDeli_city());
			startLineVO.setPk_province(segVO.getDeli_province());
			startLineVO.setPk_area(segVO.getDeli_area());
			startLineVO.setDetail_addr(segVO.getDeli_detail_addr());
			startLineVO.setContact(segVO.getDeli_contact());
			startLineVO.setPhone(segVO.getDeli_phone());
			startLineVO.setMobile(segVO.getDeli_mobile());
			startLineVO.setEmail(segVO.getDeli_email());
			startLineVO.setReq_arri_date(segVO.getReq_deli_date());
			startLineVO.setAddr_flag(AddressConst.START_ADDR_FLAG); // 始发地标识
			startLineVO.setPk_segment(segVO.getPk_segment());
			startLineVO.setSegment_node(UFBoolean.TRUE);// 标记是运段的节点，区分用户自己增加的节点
			String start_addr_name = NWDao.getInstance().queryForObject(addr_sql, String.class,
					startLineVO.getPk_address());
			startLineVO.setAddr_name(start_addr_name);
			startLineVOs.add(startLineVO);

			EntLineBVO endLineVO = new EntLineBVO();
			endLineVO.setPk_address(segVO.getPk_arrival());
			endLineVO.setPk_city(segVO.getArri_city());
			endLineVO.setPk_province(segVO.getArri_province());
			endLineVO.setPk_area(segVO.getArri_area());
			endLineVO.setDetail_addr(segVO.getArri_detail_addr());
			endLineVO.setContact(segVO.getArri_contact());
			endLineVO.setPhone(segVO.getArri_phone());
			endLineVO.setMobile(segVO.getArri_mobile());
			endLineVO.setEmail(segVO.getArri_email());
			endLineVO.setReq_arri_date(segVO.getReq_arri_date());
			endLineVO.setAddr_flag(AddressConst.END_ADDR_FLAG); // 目的地标识
			endLineVO.setPk_segment(segVO.getPk_segment());
			endLineVO.setSegment_node(UFBoolean.TRUE);
			String end_addr_name = NWDao.getInstance()
					.queryForObject(addr_sql, String.class, endLineVO.getPk_address());
			endLineVO.setAddr_name(end_addr_name);
			endLineVOs.add(endLineVO);
			logger.info("------------------运段号：" + segVO.getVbillno() + " 的起始地和目的地------------------");
			logger.info("起始地：" + startLineVO.getAddr_name());
			logger.info("目的地：" + endLineVO.getAddr_name());
		}

		// 起始地和目的地分别进行分组并合并
		startLineVOs = PZUtils.groupAndMergeLine(startLineVOs, true);
		logger.info("-------------------起始地合并后的地址名称--------------------");
		for(EntLineBVO lineVO : startLineVOs) {
			logger.info(lineVO.getAddr_name());
		}
		endLineVOs = PZUtils.groupAndMergeLine(endLineVOs, false);
		logger.info("-------------------目的地合并后的地址名称--------------------");
		for(EntLineBVO lineVO : endLineVOs) {
			logger.info(lineVO.getAddr_name());
		}
		// 对起始点和目的点进行分组合并。
		startLineVOs.addAll(endLineVOs);
		List<EntLineBVO> lineVOs = PZUtils.processLineInfo(startLineVOs.toArray(new EntLineBVO[startLineVOs.size()]),
				false);

		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(lineVOs.size());
		for(SuperVO vo : lineVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}

		/**
		 * 6.执行公式
		 */
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.SEG_PZ_CODE);
		paramVO.setBody(true);
		paramVO.setTabCode(TabcodeConst.TS_ENT_LINE_B);
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(this.getBillTemplateID(paramVO));
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(), paramVO
				.getTabCode().split(","), null);
		return list;
	}

	public List<Map<String, Object>> loadTransbilityB(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			return new ArrayList<Map<String, Object>>();
		}
		String cond = NWUtils.buildConditionString(pk_segment);
		String sql = "select invoice_vbillno from ts_segment WITH(NOLOCK) where pk_segment in " + cond;
		List<String> invoice_vbillno_ary = NWDao.getInstance().queryForList(sql, String.class);// 读取所有发货单
		// 根据发货单号读取运力信息
		cond = NWUtils.buildConditionString(invoice_vbillno_ary.toArray(new String[invoice_vbillno_ary.size()]));
		sql = "select pk_invoice from ts_invoice WITH(NOLOCK) where vbillno in " + cond;
		List<String> pk_invoice_ary = NWDao.getInstance().queryForList(sql, String.class);
		cond = NWUtils.buildConditionString(pk_invoice_ary.toArray(new String[pk_invoice_ary.size()]));
		sql = "select * from ts_trans_bility_b WITH(NOLOCK) where pk_invoice in " + cond;
		TransBilityBVO[] superVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(TransBilityBVO.class,
				"pk_invoice in " + cond);
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOs.length);
		for(SuperVO vo : superVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		return FormulaHelper.execFormula(mapList, getFormulas(), true);
	}

	private String[] getFormulas() {
		return new String[] { "car_type_name->getColValue(ts_car_type, name, pk_car_type, pk_car_type)" };
	}

	/**
	 * 比较器,按照要求到达日期从小到大排序. 使用日期排序无法确定运段的前后
	 * 
	 * @author xuqc
	 * @deprecated
	 * 
	 */
	class EntLineBVOComparator implements Comparator<EntLineBVO> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(EntLineBVO lineVO1, EntLineBVO lineVO2) {
			if(lineVO1.getReq_arri_date() == null && lineVO2.getReq_arri_date() != null) {
				return -1;
			} else if(lineVO1.getReq_arri_date() == null && lineVO2.getReq_arri_date() == null) {
				return 0;
			} else if(lineVO2.getReq_arri_date() == null) {
				return 1;
			} else if(lineVO1.getReq_arri_date() != null && lineVO2.getReq_arri_date() != null) {
				return lineVO1.getReq_arri_date().compareToIgnoreCase(lineVO2.getReq_arri_date());
			}
			return 0;
		}
	}

	public List<InvoiceVO> getInvoiceVOBySegmentPKs(String[] pk_segment) {
		if(pk_segment == null || pk_segment.length == 0) {
			return null;
		}
		String cond = NWUtils.buildConditionString(pk_segment);
		String sql = "select * from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 "
				+ "and vbillno in (select invoice_vbillno from ts_segment WITH(NOLOCK) where pk_segment in " + cond + ")";
		return dao.queryForList(sql, InvoiceVO.class);
	}

	/**
	 * 匹配合同，返回应付明细子表
	 * 
	 * @param num_count
	 * @param fee_weight_count
	 * @param volume_count
	 * @param node_count
	 * @param pk_carrier
	 * @param pk_trans_type
	 * @param start_addr
	 * @param end_addr
	 * @param start_city
	 * @param end_city
	 * @param pk_car_type
	 * @return
	 */
	private List<PayDetailBVO> loadPayDetailBVOs(double pack_num_count, int num_count, double fee_weight_count,
			double weight_count, double volume_count, int node_count, int deli_node_count,List<PackInfo> packInfos, String pk_carrier,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date, Integer urgent_level, String item_code,
			String pk_trans_line,UFBoolean if_return) {
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, pk_carrier,
				pk_trans_type, start_addr, end_addr, start_city, end_city, pk_corp, req_arri_date,urgent_level,item_code,pk_trans_line,if_return);
		if(contractBVOs == null || contractBVOs.size() == 0) {
			// 所有情况都匹配不到，崩溃了
			return null;
		}
		// 匹配到了，不容易啊
		// 开始计算金额吧，痛苦才刚刚开始，请参考《合同明细金额的计算方式》一书
		List<PayDetailBVO> detailBVOs = contractService.buildPayDetailBVO(pk_carrier, pack_num_count, num_count,
				fee_weight_count, weight_count, volume_count, node_count, deli_node_count, packInfos, pk_car_type, pk_corp,
				urgent_level, item_code,  pk_trans_line,if_return, contractBVOs);
		return detailBVOs;
	}

	public List<Map<String, Object>> loadPayDetail(double pack_num_count, int num_count, double fee_weight_count,
			double weight_count, double volume_count, int node_count, int deli_node_count,List<PackInfo> packInfos, String pk_carrier,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date,Integer urgent_level, String item_code,
			String pk_trans_line,UFBoolean if_return) {
		List<PayDetailBVO> detailBVOs = loadPayDetailBVOs(pack_num_count, num_count, fee_weight_count, weight_count,
				volume_count, node_count, deli_node_count,packInfos, pk_carrier, pk_trans_type, start_addr, end_addr, start_city,
				end_city, pk_car_type, pk_corp, req_arri_date,urgent_level,item_code,pk_trans_line,if_return);
		if(detailBVOs == null || detailBVOs.size() == 0) {
			// 匹配不到
			return new ArrayList<Map<String, Object>>();
		}
		/**
		 * 6.执行公式
		 */
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(detailBVOs.size());
		for(SuperVO vo : detailBVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.SEG_PZ_CODE);
		paramVO.setBody(true);
		paramVO.setTabCode(TabcodeConst.TS_PAY_DETAIL_B);
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(this.getBillTemplateID(paramVO));
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(), paramVO
				.getTabCode().split(","), null);
		return list;
	}
	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		Map<String, Object> retMap = save(billVO, paramVO,null);
		for(String key : retMap.keySet()){
			return (AggregatedValueObject) retMap.get(key);
		}
		return null;
	}
	public Map<String, Object> save(AggregatedValueObject billVO, ParamVO paramVO,String placeholder ) {
		// 实际上是pzVO，要转成委托单聚合VO
		ExAggEntrustVO pzAggVO = (ExAggEntrustVO) billVO;
		//增加pzHeaderVO 为了批量配载使用2015-11-6 Jonathan
		PZHeaderVO pzHeaderVO = (PZHeaderVO) billVO.getParentVO();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
		//修改批次表信息2015-12-28 lanjian
		EntLotVO entLotVO = new EntLotVO();
		EntrustVO entrustVO = new EntrustVO();
		int serialno = 0;
		if(pzHeaderVO.getLot() != null){
			EntLotVO oldEntLotVO = NWDao.getInstance().queryByCondition(EntLotVO.class, " lot=? ", pzHeaderVO.getLot());
			String serialno_sql = "SELECT max(isnull(serialno,0)) FROM ts_entrust WHERE lot = ?";
			serialno = dao.queryForObject(serialno_sql, Integer.class, pzHeaderVO.getLot());
			if(oldEntLotVO != null){
				//yaojiie 2016 1 6 当输入了批次号，并且批次号在数据库存在委托单信息时，将委托单里的运力信息，也复制到这个单据里
				EntrustVO[] entrustVOs = dao.queryForSuperVOArrayByCondition(EntrustVO.class, "lot=?", oldEntLotVO.getLot());
				if(entrustVOs != null && entrustVOs.length != 0){
					EntTransbilityBVO[] entTransbilityBVOs = dao.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust=?", entrustVOs[0].getPk_entrust());
					for(EntTransbilityBVO entTransbilityBVO : entTransbilityBVOs){
						entTransbilityBVO.setPk_entrust(null);
						entTransbilityBVO.setPk_ent_trans_bility_b(null);
					}
					pzHeaderVO.setPk_carrier(entrustVOs[0].getPk_carrier());
					pzHeaderVO.setPk_trans_type(entrustVOs[0].getPk_trans_type());
					pzHeaderVO.setBalatype(entrustVOs[0].getBalatype());
					pzHeaderVO.setCarno(null);
					pzHeaderVO.setPk_driver(null);
					pzHeaderVO.setPk_car_type(null);
					pzHeaderVO.setGps_id(null);
					pzAggVO.setTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B, entTransbilityBVOs);
				}
				oldEntLotVO.setStatus(VOStatus.UPDATED);
				oldEntLotVO.setModify_time(new UFDateTime(new Date()));
				oldEntLotVO.setModify_user("system");
				oldEntLotVO.setDef4("M");
				oldEntLotVO.setEdi_flag(UFBoolean.FALSE);
				oldEntLotVO.setIs_append(UFBoolean.TRUE);
				pzHeaderVO.setIs_append(UFBoolean.TRUE);
				toBeUpdate.add(oldEntLotVO);
			}else{
				entLotVO.setLot(pzHeaderVO.getLot());
				pzHeaderVO.setIs_append(UFBoolean.FALSE);
				entLotVO.setDbilldate(new UFDate());
				entLotVO.setEdi_flag(UFBoolean.FALSE);
				entLotVO.setVbillstatus(BillStatus.NEW);
				entLotVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				entLotVO.setStatus(VOStatus.NEW);
				entLotVO.setCreate_time(new UFDateTime(new Date()));
				entLotVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				NWDao.setUuidPrimaryKey(entLotVO);
				toBeUpdate.add(entLotVO);	
			}
		}else{
			String lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.BATORDERLOT);
			entLotVO.setLot(lot);
			pzHeaderVO.setLot(lot);
			pzHeaderVO.setIs_append(UFBoolean.FALSE);
			entLotVO.setDbilldate(new UFDate());
			entLotVO.setEdi_flag(UFBoolean.FALSE);
			entLotVO.setVbillstatus(BillStatus.NEW);
			entLotVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			entLotVO.setStatus(VOStatus.NEW);
			entLotVO.setCreate_time(new UFDateTime(new Date()));
			entLotVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			NWDao.setUuidPrimaryKey(entLotVO);
			toBeUpdate.add(entLotVO);
		}	
		Map<String,Object> entMap = new HashMap<String, Object>();
		if(paramVO.getFunCode().equals(FunConst.SEG_PZ_CODE)){
			// 校验地址顺序
			EntLineBVO[] lineVOs = (EntLineBVO[]) pzAggVO.getTableVO(TabcodeConst.TS_ENT_LINE_B);
			List<EntLineBVO> lineList = PZUtils.processLineInfo(lineVOs, true);
			// 重新设置
			pzAggVO.setTableVO(TabcodeConst.TS_ENT_LINE_B, lineList.toArray(new EntLineBVO[lineList.size()]));
			ExAggEntrustVO exAggEntrustVO = processPZ(pzAggVO, paramVO);
			entrustVO = (EntrustVO) exAggEntrustVO.getParentVO();
			EntPackBVO[] packBVOs = (EntPackBVO[]) exAggEntrustVO.getTableVO(TabcodeConst.TS_ENT_PACK_B);
			setTempToEntLot(entLotVO, packBVOs);
			EntTransbilityBVO[] entTransbilityBVOs = (EntTransbilityBVO[]) exAggEntrustVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B);
			setSpeedLimitToEntLot(entLotVO, entTransbilityBVOs);
			entMap.put(entrustVO.getPk_entrust(), exAggEntrustVO);
		}else if(paramVO.getFunCode().equals(FunConst.SEG_BATCH_PZ_CODE)){
			//增加segVOs 为了批量配载使用2015-11-6 Jonathan
			SegmentVO[] segVOs = (SegmentVO[]) pzAggVO.getTableVO(TabcodeConst.TS_SEGMENT);
			//增加entTransbilityVOs 为了批量配载使用2015-11-6 Jonathan
			EntTransbilityBVO[] entTransbilityVOs = (EntTransbilityBVO[]) pzAggVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B);
			if(segVOs == null || segVOs.length == 0) {
				return null;
			}	
			List<EntPackBVO> packBVOList = new ArrayList<EntPackBVO>();
			EntTransbilityBVO[] entTransbilityBVOs = null;
			for(SegmentVO segVO: segVOs){
				// 只有待调度的单据才能进行配载
				// 2015-10-27 修改原模式，直接获取数据库后台获取segVO
				SegmentVO newsegVO = dao.queryByCondition(SegmentVO.class, "vbillno=? ",segVO.getVbillno());
				if(newsegVO == null) {
					throw new BusiException("单据不存在，请重新选择！");
				}	
				if(newsegVO.getVbillstatus().intValue() == BillStatus.SEG_WPLAN) {
					newsegVO.setReq_deli_date(segVO.getReq_deli_date());
					newsegVO.setReq_deli_time(segVO.getReq_deli_time());
					newsegVO.setReq_arri_date(segVO.getReq_arri_date());
					newsegVO.setReq_arri_time(segVO.getReq_arri_time());
					serialno += 10;
					ExAggEntrustVO exAggEntrustVO = doProcessPZ(pzHeaderVO, newsegVO, entTransbilityVOs,paramVO,serialno);
					entrustVO = (EntrustVO) exAggEntrustVO.getParentVO();
					EntPackBVO[] packBVOs = (EntPackBVO[]) exAggEntrustVO.getTableVO(TabcodeConst.TS_ENT_PACK_B);
					if(entTransbilityBVOs == null){
						entTransbilityBVOs = (EntTransbilityBVO[]) exAggEntrustVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B);
					}
					if(packBVOs != null && packBVOs.length > 0){
						packBVOList.addAll(Arrays.asList(packBVOs));
					}
					entMap.put(entrustVO.getPk_entrust(), exAggEntrustVO);
				}else{
					throw new BusiException("单据已被调度，请重新选择！");
				}
			}
			
			setSpeedLimitToEntLot(entLotVO, entTransbilityBVOs);
			setTempToEntLot(entLotVO, packBVOList.toArray(new EntPackBVO[packBVOList.size()]));
		}
		//yaojiie 2016 2 22 生成lot是，随机记录一个委托单信息。
		entLotVO.setPk_entrust(entrustVO.getPk_entrust());
		dao.saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));	
		return entMap;
	}

	//将批量配载和配载放到同一个方法中，所以该方法暂时不用，但不删除，先放着2015-11-6 Jonathan
	public void processBatchSave(PZHeaderVO headerVO, String[] vbillnoAry, ParamVO paramVO) {
		if(vbillnoAry == null || vbillnoAry.length == 0) {
			return;
		}
		// 2015-10-27 修改原模式，直接获取数据库后台获取segVO
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "vbillno in " + NWUtils.buildConditionString(vbillnoAry));
		if(segVOs == null || segVOs.length == 0) {
			return;
		}	
		for(SegmentVO segVO: segVOs){
		// 只有待调度的单据才能进行配载
			if(segVO.getVbillstatus().intValue() == BillStatus.SEG_WPLAN) {
				//doProcessPZ(headerVO, segVO, paramVO);
			}
		}

	}

	/**
	 * 处理配载 <li>1、生成委托单</li> <li>2、生成应付明细</li> <li>3、更新运段</li>
	 * 
	 * @param pzAggVO
	 * @return
	 */
	private ExAggEntrustVO processPZ(ExAggEntrustVO pzAggVO, ParamVO paramVO) {
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
		PZHeaderVO headerVO = (PZHeaderVO) pzAggVO.getParentVO(); // 表头
		
		CarrierVO[] carrierVOs =  NWDao.getInstance().queryForSuperVOArrayByCondition(CarrierVO.class, "pk_carrier =?", headerVO.getPk_carrier());
		if(carrierVOs[0].getCarr_type() != null && DataDictConst.CARR_TYPE.FGS.intValue() == carrierVOs[0].getCarr_type()){
			throw new BusiException("分公司结算单据不能使用配载，请使用批量配载！");
		}
		SegmentVO[] segVOs = (SegmentVO[]) pzAggVO.getTableVO(TabcodeConst.TS_SEGMENT); // 运段信息
		// 这里的运段信息不是完整的，重新加载一次
		String segCond = getSegmentCond(segVOs);
		segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + segCond);
		for(SegmentVO segVO : segVOs) {
			if(segVO.getVbillstatus().intValue() != BillStatus.SEG_WPLAN) {
				throw new BusiException("必须是[待调度]状态的运段才能执行配载!");
			}
		}

		// 1、生成委托单主表
		EntrustVO entVO = new EntrustVO();
		boolean autoConfirm = ParameterHelper.getEntrustAutoConfirm();
		if(autoConfirm) {
			entVO.setVbillstatus(BillStatus.ENT_CONFIRM);
			//增加确认时间，确认人 2015-11-10 jonathan
			entVO.setConfirm_date((new UFDateTime(new Date())).toString());
			entVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
	
		} else {
			entVO.setVbillstatus(BillStatus.ENT_UNCONFIRM); // 待确认状态
		}
		setCodeField(entVO, paramVO); // 设置单据号
		entVO.setDbilldate(new UFDate());
		entVO.setIs_append(headerVO.getIs_append());
		entVO.setPlan_carrier(headerVO.getPk_carrier());
		entVO.setPlan_carno(headerVO.getCarno());
		entVO.setPlan_driver(headerVO.getPk_driver());
		entVO.setPlan_car_type(headerVO.getPk_car_type());
		entVO.setPlan_trans_type(headerVO.getPk_trans_type());
		entVO.setPlan_flight(headerVO.getPk_flight());

		entVO.setPk_carrier(headerVO.getPk_carrier());
		entVO.setCarno(headerVO.getCarno());
		entVO.setGps_id(headerVO.getGps_id());
		entVO.setPk_driver(headerVO.getPk_driver());
		//headerVO.getDriver_name() 这里的driver_name其实是pk_driver
		//但是这里要存的是name，如果你看不懂，去问经理。
		if(StringUtils.isNotBlank(headerVO.getDriver_name())){
			DriverVO driverVO = NWDao.getInstance().queryByCondition(DriverVO.class, "pk_driver =?", headerVO.getDriver_name());
			if(driverVO != null){
				entVO.setDriver_name(driverVO.getDriver_name());
			}else{
				entVO.setDriver_name(headerVO.getDriver_name());
			}
		}
		
		entVO.setDriver_mobile(headerVO.getDriver_mobile());
		entVO.setPk_car_type(headerVO.getPk_car_type());
		entVO.setPk_trans_type(headerVO.getPk_trans_type());
		entVO.setPk_flight(headerVO.getPk_flight());
		
		entVO.setBalatype(headerVO.getBalatype());
		entVO.setMemo(headerVO.getMemo());
		StringBuffer invoice_vbillno_buf = new StringBuffer();
		StringBuffer cust_orderno_buf = new StringBuffer();
		StringBuffer orderno_buf = new StringBuffer();
		StringBuffer pk_customer_buf = new StringBuffer();
		StringBuffer segment_vbillno_buf = new StringBuffer();
		// 收集运段对应的发货单，注意，一个发货单可以对应多个运段
		List<InvoiceVO> invoiceVOs = new ArrayList<InvoiceVO>();
		List<String> pk_invoiceAry = new ArrayList<String>(); // 用于检测是否是同一条发货单，用完马上销毁
		
		InvoiceVO invO = dao.queryByCondition(InvoiceVO.class, "vbillno=?", segVOs[0].getInvoice_vbillno());
		entVO.setUrgent_level(invO.getUrgent_level());
		entVO.setItem_code(invO.getItem_code());
		entVO.setPk_trans_line(invO.getPk_trans_line());
		entVO.setIf_return(invO.getIf_return());
		
		
		entVO.setDef1(headerVO.getDef1());
		entVO.setDef2(headerVO.getDef2());
		entVO.setDef3(headerVO.getDef3());
		entVO.setDef4(headerVO.getDef4());
		entVO.setDef5(headerVO.getDef5());
		entVO.setDef6(headerVO.getDef6());
		entVO.setDef7(headerVO.getDef7());
		entVO.setDef8(headerVO.getDef8());
		entVO.setDef9(headerVO.getDef9());
		entVO.setDef10(headerVO.getDef10());
		entVO.setDef11(headerVO.getDef11());
		entVO.setDef12(headerVO.getDef12());
		
		// 2、收集多个发货单的信息，并更新运段表
		for(SegmentVO segVO : segVOs) {
			if(segment_vbillno_buf.length() > 0) {
				segment_vbillno_buf.append(Constants.SPLIT_CHAR);
			}
			segment_vbillno_buf.append(segVO.getVbillno());
			// 根据发货单号查询发货单,如果多个运段对应同一个发货单号，此时也是用分隔符隔开，不进行合并
			InvoiceVO invoiceVO = dao.queryByCondition(InvoiceVO.class, "vbillno=?", segVO.getInvoice_vbillno());
			if(invoiceVO == null) {
				throw new BusiException("单据已经不存在，单据号[?]！",segVO.getInvoice_vbillno());
			}
			if(!pk_invoiceAry.contains(invoiceVO.getPk_invoice())) {
				invoiceVOs.add(invoiceVO);
				pk_invoiceAry.add(invoiceVO.getPk_invoice());
			}

			if(invoice_vbillno_buf.length() > 0) {
				invoice_vbillno_buf.append(Constants.SPLIT_CHAR);
			}
			invoice_vbillno_buf.append(segVO.getInvoice_vbillno());

			if(StringUtils.isNotBlank(invoiceVO.getCust_orderno())) {
				if(cust_orderno_buf.length() > 0) {
					cust_orderno_buf.append(Constants.SPLIT_CHAR);
				}
				cust_orderno_buf.append(invoiceVO.getCust_orderno());
			}

			if(StringUtils.isNotBlank(invoiceVO.getOrderno())) {
				if(orderno_buf.length() > 0) {
					orderno_buf.append(Constants.SPLIT_CHAR);
				}
				orderno_buf.append(invoiceVO.getOrderno());
			}

			if(StringUtils.isNotBlank(invoiceVO.getPk_customer())) {
				if(pk_customer_buf.length() > 0) {
					pk_customer_buf.append(Constants.SPLIT_CHAR);
				}
				pk_customer_buf.append(invoiceVO.getPk_customer());
			}
			// 将运段的状态改成已调度
			segVO.setVbillstatus(BillStatus.SEG_DISPATCH);
			segVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(segVO);
		}
		entVO.setInvoice_vbillno(invoice_vbillno_buf.toString());
		entVO.setSegment_vbillno(segment_vbillno_buf.toString());
		entVO.setCust_orderno(cust_orderno_buf.toString());
		entVO.setOrderno(orderno_buf.toString());
		entVO.setPk_customer(pk_customer_buf.toString());
		
		// 销毁
		pk_invoiceAry.clear();

		EntLineBVO[] lineVOs = (EntLineBVO[]) pzAggVO.getTableVO(TabcodeConst.TS_ENT_LINE_B); // 路线信息
		if(lineVOs == null || lineVOs.length == 0) {
			throw new BusiException("配载时路线信息还没有加载完全，请重新点击[保存]即可！");
		}
		EntLineBVO firstLineVO = PZUtils.getFirstLineVO(lineVOs);
		firstLineVO.setOperate_type(OperateTypeConst.PICKUP);
		EntLineBVO lastLineVO = PZUtils.getLastLineVO(lineVOs);
		lastLineVO.setOperate_type(OperateTypeConst.DELIVERY);
		PZUtils.syncEntrustDeliAndArri(entVO, firstLineVO, lastLineVO);
		entVO.setLot(headerVO.getLot());
		// 计算运输里程，如果没有，则使用父级的运输里程
		Map<String, Object> mileageAndDistanceMap = invoiceService.getMileageAndDistance(firstLineVO.getPk_city(),
				lastLineVO.getPk_city(), firstLineVO.getPk_address(), lastLineVO.getPk_address());
		if(mileageAndDistanceMap != null) {
			Object mileage = mileageAndDistanceMap.get("mileage");
			if(mileage != null) {
				entVO.setMileage(Integer.parseInt(mileage.toString()));
			}
			Object distance = mileageAndDistanceMap.get("distance");
			if(distance != null) {
				entVO.setDistance(new UFDouble(distance.toString()));
			}
		}
		entVO.setPack_num_count(headerVO.getPack_num_count());
		entVO.setNum_count(headerVO.getNum_count());
		entVO.setWeight_count(headerVO.getWeight_count());
		entVO.setVolume_count(headerVO.getVolume_count());
		entVO.setFee_weight_count(headerVO.getFee_weight_count());
		entVO.setVolume_weight_count(headerVO.getVolume_weight_count());
		entVO.setCost_amount(headerVO.getCost_amount());
		entVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		entVO.setCreate_time(new UFDateTime(new Date()));
		entVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		entVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(entVO);
		toBeUpdate.add(entVO);

		// 3、生成货品信息
		List<EntPackBVO> entPackBVOs = new ArrayList<EntPackBVO>();
		SegPackBVO[] sPackVOs = dao.queryForSuperVOArrayByCondition(SegPackBVO.class,
				"isnull(dr,0)=0 and pk_segment in " + segCond);
		int index = 0;
		for(int i = 0; i < sPackVOs.length; i++) {
			SegPackBVO sPackVO = sPackVOs[i];
			EntPackBVO ePackVO = EntrustUtils.convert(sPackVO);
			if(ePackVO.getSerialno() == null) {
				index += 10;
				ePackVO.setSerialno(index);
			}

			ePackVO.setPk_entrust(entVO.getPk_entrust());
			ePackVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(ePackVO);
			toBeUpdate.add(ePackVO);// 更新货品信息
			entPackBVOs.add(ePackVO);
		}
		// 4、生成路线信息，从billVO可以直接得到
		for(EntLineBVO lineVO : lineVOs) {
			lineVO.setPk_entrust(entVO.getPk_entrust());
			lineVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(lineVO);
			toBeUpdate.add(lineVO);// 更新路线信息表
		}
		
		//yaojiie 2015 12 24 
		//调用往辅助表 ent_line_pack_b加数据。
		List<SuperVO> entLinePackBVOs =  addToLinePack(Arrays.asList(lineVOs) , entPackBVOs);
		if(entLinePackBVOs != null && entLinePackBVOs.size() > 0){
			toBeUpdate.addAll(entLinePackBVOs);
		}
		
		// 4.5 生成委托单的运力信息表
		EntTransbilityBVO[] tbVOs = (EntTransbilityBVO[]) pzAggVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B); // 运力信息
		for(EntTransbilityBVO tbVO : tbVOs) {
			tbVO.setPk_entrust(entVO.getPk_entrust());
			tbVO.setLot(entVO.getLot());
			tbVO.setCertificate_id(headerVO.getCertificate_id());
			tbVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(tbVO);
			EntTransHisBVO entTransHisBVO = PZUtils.getEntTransHisBVO(tbVO);
			toBeUpdate.add(entTransHisBVO);
			toBeUpdate.add(tbVO);
		}
		// 4.5.1 将表头的运力信息添加到表体的运力信d
		if(StringUtils.isNotBlank(entVO.getCarno()) || StringUtils.isNotBlank(entVO.getPk_driver())
				|| StringUtils.isNotBlank(entVO.getPk_car_type())) {
			EntTransbilityBVO tbVO = new EntTransbilityBVO();
			tbVO.setPk_entrust(entVO.getPk_entrust());
			tbVO.setLot(entVO.getLot());
			tbVO.setCertificate_id(headerVO.getCertificate_id());
			tbVO.setCarno(entVO.getCarno());
			tbVO.setPk_car_type(entVO.getPk_car_type());
			tbVO.setPk_driver(entVO.getPk_driver());
			tbVO.setGps_id(entVO.getGps_id());
			tbVO.setStatus(VOStatus.NEW);
			tbVO.setDriver_name(entVO.getDriver_name());
			tbVO.setDriver_mobile(entVO.getDriver_mobile());
			NWDao.setUuidPrimaryKey(tbVO);
			EntTransHisBVO entTransHisBVO = PZUtils.getEntTransHisBVO(tbVO);
			toBeUpdate.add(entTransHisBVO);
			toBeUpdate.add(tbVO);
		}

		// 5、生成关联发货单子表
		for(int i = 0; i < invoiceVOs.size(); i++) {
			EntInvBVO entInvBVO = new EntInvBVO();
			entInvBVO.setPk_entrust(entVO.getPk_entrust());
			entInvBVO.setPk_invoice(invoiceVOs.get(i).getPk_invoice());
			entInvBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(entInvBVO);
			toBeUpdate.add(entInvBVO);// 更新发货单子表
		}
		// 6、生成关联运段子表
		for(int i = 0; i < segVOs.length; i++) {
			EntSegBVO entSegBVO = new EntSegBVO();
			entSegBVO.setPk_entrust(entVO.getPk_entrust());
			entSegBVO.setPk_segment(segVOs[i].getPk_segment());
			entSegBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(entSegBVO);
			toBeUpdate.add(entSegBVO);// 更新运段子表
		}
		// 7、生成应付明细主表
		PayDetailVO payVO = new PayDetailVO();
		//记录批次号
		payVO.setLot(entVO.getLot());
		payVO.setDbilldate(new UFDate());
		payVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
		// 默认带出运费合同的币种(匹配合同)，如果没有维护合同则默认为系统设置中的币种
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, headerVO.getPk_carrier(),
				headerVO.getPk_trans_type(), firstLineVO.getPk_address(), lastLineVO.getPk_address(),
				firstLineVO.getPk_city(), lastLineVO.getPk_city(), entVO.getPk_corp(), entVO.getReq_arri_date(),
				entVO.getUrgent_level(),entVO.getItem_code(),entVO.getPk_trans_line(),entVO.getIf_return());
		if(contractBVOs == null || contractBVOs.size() == 0) {
			// 没有匹配到合同
			payVO.setCurrency(ParameterHelper.getCurrency());
		} else {
			String pk_contract = contractBVOs.get(0).getPk_contract();
			ContractVO contractVO = this.getByPrimaryKey(ContractVO.class, pk_contract);
			payVO.setCurrency(contractVO.getCurrency());
		}

		payVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		payVO.setCreate_time(new UFDateTime(new Date()));
		payVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		payVO.setPack_num_count(headerVO.getPack_num_count());
		payVO.setNum_count(headerVO.getNum_count());
		payVO.setVolume_count(headerVO.getVolume_count());
		payVO.setWeight_count(headerVO.getWeight_count());
		payVO.setFee_weight_count(headerVO.getFee_weight_count());
		payVO.setCost_amount(headerVO.getCost_amount());
		payVO.setUngot_amount(headerVO.getCost_amount());// 未付金额默认等于总金额
		payVO.setEntrust_vbillno(entVO.getVbillno());
		payVO.setPk_carrier(headerVO.getPk_carrier());// 承运商
		payVO.setBalatype(headerVO.getBalatype());
		payVO.setVbillstatus(BillStatus.NEW);
		payVO.setMemo(entVO.getMemo()); // 默认等于发运单备注
		payVO.setPay_type(PayDetailConst.ORIGIN_TYPE); // 类型：默认为0
		payVO.setMerge_type(PayDetailConst.MERGE_TYPE.UNMERGE.intValue());
		payVO.setCust_orderno(entVO.getCust_orderno());
		payVO.setOrderno(entVO.getOrderno());
		payVO.setAccount_period(new UFDateTime(entVO.getReq_deli_date()));
		// FIXME 取第一行合同明细的税种，税率
		if(contractBVOs != null && contractBVOs.size() > 0) {
			payVO.setTax_cat(contractBVOs.get(0).getTax_cat());
			payVO.setTax_rate(contractBVOs.get(0).getTax_rate());
			// FIXME 取第一行合同明细的税种，税率
			payVO.setTaxmny(CMUtils.getTaxmny(payVO.getCost_amount(), payVO.getTax_cat(), payVO.getTax_rate()));
		}

		payVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(payVO);
		toBeUpdate.add(payVO);
		// 8、生成应付明细子表，从billVO中直接读取
		PayDetailBVO[] detailVOs = (PayDetailBVO[]) pzAggVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
		if(detailVOs != null) {
			for(PayDetailBVO detailVO : detailVOs) {
				detailVO.setPk_pay_detail(payVO.getPk_pay_detail());
				detailVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(detailVO);
				toBeUpdate.add(detailVO);
			}
		}
		// 读取费用分摊VO
		List<PayDeviBVO> deviVOs = PZUtils.getPayDeviBVOs(entVO, invoiceVOs.toArray(new InvoiceVO[invoiceVOs.size()]),
				segVOs, detailVOs);
		if(deviVOs != null && deviVOs.size() > 0) {
			toBeUpdate.addAll(deviVOs);
		}
		// 9、生成分摊表,见第8步

		// 执行数据库操作
		dao.saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));
		//yaojiie 2015 12 15 在配载生成相关单据信息时，计算金额和利润。
		CMUtils.totalCostComput(invoiceVOs);
		ExAggEntrustVO aggEntrustVO = new ExAggEntrustVO();
		aggEntrustVO.setParentVO(entVO);
		aggEntrustVO.setTableVO(TabcodeConst.TS_ENT_PACK_B, entPackBVOs.toArray(new EntPackBVO[entPackBVOs.size()]));
		aggEntrustVO.setTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B, tbVOs);
		return aggEntrustVO;
	}

	/**
	 * 执行配载，在批量配载时使用
	 */
	public ExAggEntrustVO doProcessPZ(PZHeaderVO headerVO, SegmentVO segVO, EntTransbilityBVO[] entTransbilityVOs,ParamVO paramVO,int ent_serialno) {
		logger.info("执行配载...");
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
		// 1、生成委托单主表
		EntrustVO entVO = new EntrustVO();
		boolean autoConfirm = ParameterHelper.getEntrustAutoConfirm();
		if(autoConfirm) {
			entVO.setVbillstatus(BillStatus.ENT_CONFIRM);
			//增加确认时间，确认人 2015-11-10 jonathan
			entVO.setConfirm_date((new UFDateTime(new Date())).toString());
			if(WebUtils.getLoginInfo() == null){
				entVO.setConfirm_user(segVO.getCreate_user());
			}else{
				entVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
			}
		} else {
			entVO.setVbillstatus(BillStatus.ENT_UNCONFIRM); // 待确认状态
		}
		entVO.setSerialno(ent_serialno);
		entVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.WTD)); // 设置单据号
		entVO.setIs_append(headerVO.getIs_append());
		entVO.setDbilldate(new UFDate());
		entVO.setPlan_carrier(headerVO.getPk_carrier());
		entVO.setPlan_carno(headerVO.getCarno());
		entVO.setPlan_driver(headerVO.getPk_driver());
		entVO.setPlan_car_type(headerVO.getPk_car_type());
		entVO.setPlan_trans_type(headerVO.getPk_trans_type());
		entVO.setPlan_flight(headerVO.getPk_flight());
		
		entVO.setPk_carrier(headerVO.getPk_carrier());
		entVO.setCarno(headerVO.getCarno());
		entVO.setGps_id(headerVO.getGps_id());
		entVO.setPk_driver(headerVO.getPk_driver());
		// headerVO.getDriver_name() 这里的driver_name其实是pk_driver
		// 但是这里要存的是name，如果你看不懂，去问经理。
		if (StringUtils.isNotBlank(headerVO.getDriver_name())) {
			DriverVO driverVO = NWDao.getInstance().queryByCondition(DriverVO.class, "pk_driver =?",headerVO.getDriver_name());
			if(driverVO != null){
				entVO.setDriver_name(driverVO.getDriver_name());
			}else{
				entVO.setDriver_name(headerVO.getDriver_name());
			}
		}
		entVO.setDriver_mobile(headerVO.getDriver_mobile());
		entVO.setPk_car_type(headerVO.getPk_car_type());
		entVO.setPk_trans_type(headerVO.getPk_trans_type());
		entVO.setPk_flight(headerVO.getPk_flight());

		entVO.setBalatype(headerVO.getBalatype());
		entVO.setMemo(headerVO.getMemo());
		entVO.setLot(headerVO.getLot());
		entVO.setInvoice_vbillno(segVO.getInvoice_vbillno());
		entVO.setPz_line(segVO.getPz_line());
		entVO.setPz_mileage(segVO.getMileage());
		// 根据发货单号查询发货单,如果多个运段对应同一个发货单号，此时也是用分隔符隔开，不进行合并
		InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "vbillno=?", segVO.getInvoice_vbillno());
		if(invVO == null) {
			throw new BusiException("单据已经不存在，单据号[?]！",segVO.getInvoice_vbillno());
		}

		entVO.setCust_orderno(invVO.getCust_orderno());
		entVO.setOrderno(invVO.getOrderno());
		entVO.setPk_customer(invVO.getPk_customer());
		
		entVO.setUrgent_level(invVO.getUrgent_level());
		entVO.setItem_code(invVO.getItem_code());
		entVO.setPk_trans_line(invVO.getPk_trans_line());
		entVO.setIf_return(invVO.getIf_return());

		entVO.setSegment_vbillno(segVO.getVbillno());

		
		
		entVO.setDef1(headerVO.getDef1());
		entVO.setDef2(headerVO.getDef2());
		entVO.setDef3(headerVO.getDef3());
		entVO.setDef4(headerVO.getDef4());
		entVO.setDef5(headerVO.getDef5());
		entVO.setDef6(headerVO.getDef6());
		entVO.setDef7(headerVO.getDef7());
		entVO.setDef8(headerVO.getDef8());
		entVO.setDef9(headerVO.getDef9());
		entVO.setDef10(headerVO.getDef10());
		entVO.setDef11(headerVO.getDef11());
		entVO.setDef12(headerVO.getDef12());
		
		
		// 将运段的状态改成已调度
		segVO.setVbillstatus(BillStatus.SEG_DISPATCH);
		segVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(segVO);

		List<EntLineBVO> lineVOs = new LinkedList<EntLineBVO>();
		// 读取运段的路线信息,实际上也是发货单的路线信息
		EntLineBVO firstLineVO = new EntLineBVO();
		firstLineVO.setPk_address(segVO.getPk_delivery());
		firstLineVO.setPk_city(segVO.getDeli_city());
		firstLineVO.setPk_province(segVO.getDeli_province());
		firstLineVO.setPk_area(segVO.getDeli_area());
		firstLineVO.setDetail_addr(segVO.getDeli_detail_addr());
		firstLineVO.setContact(segVO.getDeli_contact());
		firstLineVO.setPhone(segVO.getDeli_phone());
		firstLineVO.setMobile(segVO.getDeli_mobile());
		firstLineVO.setEmail(segVO.getDeli_email());
		firstLineVO.setReq_arri_date(segVO.getReq_deli_date());
		firstLineVO.setAddr_flag(AddressConst.START_ADDR_FLAG); // 始发地标识
		firstLineVO.setPk_segment(segVO.getPk_segment());
		firstLineVO.setSegment_node(UFBoolean.TRUE);// 标记是运段的节点，区分用户自己增加的节点
		firstLineVO.setOperate_type(OperateTypeConst.PICKUP);
		lineVOs.add(firstLineVO);

		EntLineBVO lastLineVO = new EntLineBVO();
		lastLineVO.setPk_address(segVO.getPk_arrival());
		lastLineVO.setPk_city(segVO.getArri_city());
		lastLineVO.setPk_province(segVO.getArri_province());
		lastLineVO.setPk_area(segVO.getArri_area());
		lastLineVO.setDetail_addr(segVO.getArri_detail_addr());
		lastLineVO.setContact(segVO.getArri_contact());
		lastLineVO.setPhone(segVO.getArri_phone());
		lastLineVO.setMobile(segVO.getArri_mobile());
		lastLineVO.setEmail(segVO.getArri_email());
		lastLineVO.setReq_arri_date(segVO.getReq_arri_date());
		lastLineVO.setAddr_flag(AddressConst.END_ADDR_FLAG); // 目的地标识
		lastLineVO.setPk_segment(segVO.getPk_segment());
		lastLineVO.setSegment_node(UFBoolean.TRUE);
		lastLineVO.setOperate_type(OperateTypeConst.DELIVERY);
		lineVOs.add(lastLineVO);

		// 设置序号
		int serialno = 10;
		for(EntLineBVO lineVO : lineVOs) {
			lineVO.setSerialno(serialno);
			serialno += 10;
		}

		entVO.setReq_deli_date(firstLineVO.getReq_arri_date());
		entVO.setReq_arri_date(lastLineVO.getReq_arri_date());

		// 提货方
		entVO.setPk_delivery(firstLineVO.getPk_address());
		entVO.setDeli_city(firstLineVO.getPk_city());
		entVO.setDeli_province(firstLineVO.getPk_province());
		entVO.setDeli_area(firstLineVO.getPk_area());
		entVO.setDeli_detail_addr(firstLineVO.getDetail_addr());
		entVO.setDeli_contact(firstLineVO.getContact());
		entVO.setDeli_phone(firstLineVO.getPhone());
		entVO.setDeli_mobile(firstLineVO.getMobile());
		entVO.setDeli_email(firstLineVO.getEmail());
		// 到货方
		entVO.setPk_arrival(lastLineVO.getPk_address());
		entVO.setArri_city(lastLineVO.getPk_city());
		entVO.setArri_province(lastLineVO.getPk_province());
		entVO.setArri_area(lastLineVO.getPk_area());
		entVO.setArri_detail_addr(lastLineVO.getDetail_addr());
		entVO.setArri_contact(lastLineVO.getContact());
		entVO.setArri_phone(lastLineVO.getPhone());
		entVO.setArri_mobile(lastLineVO.getMobile());
		entVO.setArri_email(lastLineVO.getEmail());

		// 计算运输里程，如果没有，则使用父级的运输里程
		Map<String, Object> mileageAndDistanceMap = invoiceService.getMileageAndDistance(firstLineVO.getPk_city(),
				lastLineVO.getPk_city(), firstLineVO.getPk_address(), lastLineVO.getPk_address());
		if(mileageAndDistanceMap != null) {
			Object mileage = mileageAndDistanceMap.get("mileage");
			if(mileage != null) {
				entVO.setMileage(Integer.parseInt(mileage.toString()));
			}
			Object distance = mileageAndDistanceMap.get("distance");
			if(distance != null) {
				entVO.setDistance(new UFDouble(distance.toString()));
			}
		}
		entVO.setPack_num_count(segVO.getPack_num_count());
		entVO.setNum_count(segVO.getNum_count());
		entVO.setWeight_count(segVO.getWeight_count());
		entVO.setVolume_count(segVO.getVolume_count());
		
		if(WebUtils.getLoginInfo() == null){
			entVO.setCreate_user(segVO.getCreate_user());
			entVO.setPk_corp(segVO.getPk_corp());
		}else{
			entVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			entVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		}
		
		entVO.setCreate_time(new UFDateTime(new Date()));
		
		entVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(entVO);
		toBeUpdate.add(entVO);

		// 3、生成货品信息
		List<EntPackBVO> entPackBVOs = new ArrayList<EntPackBVO>();
		SegPackBVO[] sPackVOs = dao.queryForSuperVOArrayByCondition(SegPackBVO.class,
				"isnull(dr,0)=0 and pk_segment=? ", segVO.getPk_segment());
		int index = 0;
		for(int i = 0; i < sPackVOs.length; i++) {
			SegPackBVO sPackVO = sPackVOs[i];
			EntPackBVO ePackVO = new EntPackBVO();
			ePackVO.setPk_invoice(sPackVO.getPk_invoice());
			ePackVO.setPk_segment(sPackVO.getPk_segment());
			ePackVO.setPk_goods(sPackVO.getPk_goods());
			ePackVO.setGoods_code(sPackVO.getGoods_code());
			ePackVO.setGoods_name(sPackVO.getGoods_name());
			ePackVO.setPlan_num(sPackVO.getPlan_num());
			ePackVO.setNum(sPackVO.getNum());
			ePackVO.setPlan_pack_num_count(sPackVO.getPlan_pack_num_count());
			ePackVO.setPack_num_count(sPackVO.getPack_num_count());
			ePackVO.setPack(sPackVO.getPack());
			ePackVO.setWeight(sPackVO.getWeight());
			ePackVO.setVolume(sPackVO.getVolume());
			ePackVO.setUnit_weight(sPackVO.getUnit_weight());
			ePackVO.setUnit_volume(sPackVO.getUnit_volume());
			ePackVO.setLength(sPackVO.getLength());
			ePackVO.setWidth(sPackVO.getWidth());
			ePackVO.setHeight(sPackVO.getHeight());
			ePackVO.setTrans_note(sPackVO.getTrans_note());
			ePackVO.setLow_temp(sPackVO.getLow_temp());
			ePackVO.setHight_temp(sPackVO.getHight_temp());
			ePackVO.setReference_no(sPackVO.getReference_no());
			ePackVO.setMemo(sPackVO.getMemo());
			ePackVO.setPk_entrust(entVO.getPk_entrust());
			ePackVO.setPk_seg_pack_b(sPackVO.getPk_seg_pack_b());
			
			ePackVO.setDef1(sPackVO.getDef1());
			ePackVO.setDef2(sPackVO.getDef2());
			ePackVO.setDef3(sPackVO.getDef3());
			ePackVO.setDef4(sPackVO.getDef4());
			ePackVO.setDef5(sPackVO.getDef5());
			ePackVO.setDef6(sPackVO.getDef6());
			ePackVO.setDef7(sPackVO.getDef7());
			ePackVO.setDef8(sPackVO.getDef8());
			ePackVO.setDef9(sPackVO.getDef9());
			ePackVO.setDef10(sPackVO.getDef10());
			ePackVO.setDef11(sPackVO.getDef11());
			ePackVO.setDef12(sPackVO.getDef12());
			
			ePackVO.setStatus(VOStatus.NEW);
			if(ePackVO.getSerialno() == null) {
				index += 10;
				ePackVO.setSerialno(index);
			}
			NWDao.setUuidPrimaryKey(ePackVO);
			toBeUpdate.add(ePackVO);// 更新货品信息
			entPackBVOs.add(ePackVO);
		}
		// 4、生成路线信息
		//yaojiie 2016 1 29从发货单条码表里获得条码信息 生成委托单条码信息
		InvPackBarBVO[] invPackBarBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBarBVO.class, "pk_invoice =?", invVO.getPk_invoice());
		for(EntLineBVO lineVO : lineVOs) {
			lineVO.setPk_entrust(entVO.getPk_entrust());
			lineVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(lineVO);
			toBeUpdate.add(lineVO);// 更新路线信息表
			if(invPackBarBVOs != null && invPackBarBVOs.length > 0){
				for(InvPackBarBVO invPackBarBVO : invPackBarBVOs){
					EntLineBarBVO entLineBarBVO = new EntLineBarBVO();
					entLineBarBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(entLineBarBVO);
					entLineBarBVO.setPk_entrust(lineVO.getPk_entrust());
					entLineBarBVO.setPk_ent_line_b(lineVO.getPk_ent_line_b());
					entLineBarBVO.setLot(entVO.getLot());
					entLineBarBVO.setPk_goods(invPackBarBVO.getPk_goods());
					entLineBarBVO.setGoods_code(invPackBarBVO.getGoods_code());
					entLineBarBVO.setGoods_name(invPackBarBVO.getGoods_name());
					entLineBarBVO.setBar_code(invPackBarBVO.getBar_code());
					entLineBarBVO.setPk_address(lineVO.getPk_address());
						
					entLineBarBVO.setDef1(invPackBarBVO.getDef1());
					entLineBarBVO.setDef2(invPackBarBVO.getDef2());
					entLineBarBVO.setDef3(invPackBarBVO.getDef3());
					entLineBarBVO.setDef4(invPackBarBVO.getDef4());
					entLineBarBVO.setDef5(invPackBarBVO.getDef5());
					entLineBarBVO.setDef6(invPackBarBVO.getDef6());
					entLineBarBVO.setDef7(invPackBarBVO.getDef7());
					entLineBarBVO.setDef8(invPackBarBVO.getDef8());
					entLineBarBVO.setDef9(invPackBarBVO.getDef9());
					entLineBarBVO.setDef10(invPackBarBVO.getDef10());
					entLineBarBVO.setDef11(invPackBarBVO.getDef11());
					entLineBarBVO.setDef12(invPackBarBVO.getDef12());
					toBeUpdate.add(entLineBarBVO);
				}
			
			}
		}
		//yaojiie 2015 12 24 
		//调用往辅助表 ent_line_pack_b加数据。
		List<SuperVO> entLinePackBVOs =  addToLinePack(lineVOs , entPackBVOs);
		if(entLinePackBVOs != null && entLinePackBVOs.size() > 0){
			toBeUpdate.addAll(entLinePackBVOs);
		}
		
		// 4.2 生成委托单的运力信息表
		List<EntTransbilityBVO> transbilityBVOs = new ArrayList<EntTransbilityBVO>();
		if(entTransbilityVOs!=null&&entTransbilityVOs.length>0){
			for(EntTransbilityBVO tbVO1 : entTransbilityVOs) {
				EntTransbilityBVO tbVO = new EntTransbilityBVO();
				tbVO.setPk_entrust(entVO.getPk_entrust());
				tbVO.setLot(entVO.getLot());
				tbVO.setCertificate_id(headerVO.getCertificate_id());
				tbVO.setCarno(tbVO1.getCarno());
				tbVO.setPk_car_type(tbVO1.getPk_car_type());
				tbVO.setPk_driver(tbVO1.getPk_driver());
				tbVO.setDriver_name(tbVO1.getDriver_name());
				tbVO.setDriver_mobile(tbVO1.getDriver_mobile());
				tbVO.setGps_id(tbVO1.getGps_id());
				tbVO.setContainer_no(tbVO1.getContainer_no());
				tbVO.setMemo(tbVO1.getMemo());
				tbVO.setSealing_no(tbVO1.getSealing_no());
				tbVO.setForecast_deli_date(tbVO1.getForecast_deli_date());
				tbVO.setNum(tbVO1.getNum()==null ? 1:tbVO1.getNum());
				tbVO.setDef1(tbVO1.getDef1());
				tbVO.setDef2(tbVO1.getDef2());
				tbVO.setDef3(tbVO1.getDef3());
				tbVO.setDef4(tbVO1.getDef4());
				tbVO.setDef5(tbVO1.getDef5());
				tbVO.setDef6(tbVO1.getDef6());
				tbVO.setDef7(tbVO1.getDef7());
				tbVO.setDef8(tbVO1.getDef8());
				tbVO.setDef9(tbVO1.getDef9());
				tbVO.setDef10(tbVO1.getDef10());
				tbVO.setDef11(tbVO1.getDef11());
				tbVO.setDef12(tbVO1.getDef12());
				EntTransHisBVO entTransHisBVO = PZUtils.getEntTransHisBVO(tbVO1);
				tbVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(tbVO);
				transbilityBVOs.add(tbVO);
				toBeUpdate.add(tbVO);
				toBeUpdate.add(entTransHisBVO);
			}
		}
		
		// 4.1 将表头的运力信息添加到表体的运力信息
		if(StringUtils.isNotBlank(entVO.getCarno()) || StringUtils.isNotBlank(entVO.getPk_car_type())
				|| StringUtils.isNotBlank(entVO.getPk_driver())) {
			EntTransbilityBVO tbVO = new EntTransbilityBVO();
			tbVO.setPk_entrust(entVO.getPk_entrust());
			tbVO.setLot(entVO.getLot());
			tbVO.setCertificate_id(headerVO.getCertificate_id());
			tbVO.setCarno(entVO.getCarno());
			tbVO.setPk_car_type(entVO.getPk_car_type());
			tbVO.setPk_driver(entVO.getPk_driver());
			tbVO.setGps_id(entVO.getGps_id());
			tbVO.setDriver_name(entVO.getDriver_name());
			tbVO.setDriver_mobile(entVO.getDriver_mobile());
			tbVO.setNum(1);
			tbVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(tbVO);
			EntTransHisBVO entTransHisBVO = PZUtils.getEntTransHisBVO(tbVO);
			toBeUpdate.add(tbVO);
			toBeUpdate.add(entTransHisBVO);
		}
		
		// 5、生成关联发货单子表
		EntInvBVO entInvBVO = new EntInvBVO();
		entInvBVO.setPk_entrust(entVO.getPk_entrust());
		entInvBVO.setPk_invoice(invVO.getPk_invoice());
		entInvBVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(entInvBVO);
		toBeUpdate.add(entInvBVO);// 更新发货单子表
		// 6、生成关联运段子表
		EntSegBVO entSegBVO = new EntSegBVO();
		entSegBVO.setPk_entrust(entVO.getPk_entrust());
		entSegBVO.setPk_segment(segVO.getPk_segment());
		entSegBVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(entSegBVO);
		toBeUpdate.add(entSegBVO);// 更新运段子表
		// 7、生成应付明细主表
		PayDetailVO payVO = new PayDetailVO();
		payVO.setDbilldate(new UFDate());
		payVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
		
		//增加批量配载是否匹配合同规则判断。2015-11-11 jonathan
		List<ContractBVO> contractBVOs= new ArrayList<ContractBVO>();
		boolean autoMatchContract = ParameterHelper.getBatchPZRule();
		if(autoMatchContract) {
		// 默认带出运费合同的币种(匹配合同)，如果没有维护合同则默认为系统设置中的币种
			contractBVOs = contractService.matchContract(ContractConst.CARRIER, headerVO.getPk_carrier(),
					headerVO.getPk_trans_type(), firstLineVO.getPk_address(), lastLineVO.getPk_address(),
					firstLineVO.getPk_city(), lastLineVO.getPk_city(), entVO.getPk_corp(), entVO.getReq_arri_date(),
					entVO.getUrgent_level(),entVO.getItem_code(),entVO.getPk_trans_line(),entVO.getIf_return());
			if(contractBVOs == null || contractBVOs.size() == 0) {
				// 没有匹配到合同
				payVO.setCurrency(ParameterHelper.getCurrency());
			} else {
				String pk_contract = contractBVOs.get(0).getPk_contract();
				ContractVO contractVO = this.getByPrimaryKey(ContractVO.class, pk_contract);
				payVO.setCurrency(contractVO.getCurrency());
			}
		}
		if (WebUtils.getLoginInfo() == null) {
			payVO.setCreate_user(segVO.getCreate_user());
			payVO.setPk_corp(segVO.getPk_corp());
		}else{
			payVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			payVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		}
		
		payVO.setCreate_time(new UFDateTime(new Date()));
		//记录批次号
		payVO.setLot(entVO.getLot());
		payVO.setEntrust_vbillno(entVO.getVbillno());
		payVO.setPk_carrier(headerVO.getPk_carrier());// 承运商
		payVO.setBalatype(headerVO.getBalatype());
		payVO.setVbillstatus(BillStatus.NEW);
		payVO.setMemo(entVO.getMemo()); // 默认等于发运单备注
		payVO.setPay_type(PayDetailConst.ORIGIN_TYPE); // 类型：默认为0
		payVO.setMerge_type(PayDetailConst.MERGE_TYPE.UNMERGE.intValue());
		payVO.setCust_orderno(entVO.getCust_orderno());
		payVO.setOrderno(entVO.getOrderno());
		payVO.setAccount_period(new UFDateTime(entVO.getReq_deli_date()));
		// FIXME 取第一行合同明细的税种，税率
		if(contractBVOs != null && contractBVOs.size() > 0) {
			payVO.setTax_cat(contractBVOs.get(0).getTax_cat());
			payVO.setTax_rate(contractBVOs.get(0).getTax_rate());
			// FIXME 取第一行合同明细的税种，税率
			payVO.setTaxmny(CMUtils.getTaxmny(payVO.getCost_amount(), payVO.getTax_cat(), payVO.getTax_rate()));
		}

		payVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(payVO);
		toBeUpdate.add(payVO);

		// 8、生成应付明细子表，如果没有选择计价方式，那么匹配合同，返回明细，否则根据计价方式计算应付明细
		// 2015-05-30根据承运商的体积重换算比，计算计费重
		// TODO 重新计算，计费重、体积重、总金额
		Map<String, UFDouble> retMap = PZUtils.computeFeeWeightCount(entVO.getPk_carrier(),
				headerVO.getPk_trans_type(), 
				segVO.getDeli_city(),segVO.getArri_city(),
				segVO.getVolume_count(), segVO.getWeight_count());
		if(retMap == null) {
			entVO.setFee_weight_count(segVO.getFee_weight_count());
			entVO.setVolume_weight_count(segVO.getVolume_weight_count());
		} else {
			entVO.setFee_weight_count(retMap.get("fee_weight_count"));
			entVO.setVolume_weight_count(retMap.get("volume_weight_count"));
		}
		if(contractBVOs != null && contractBVOs.size() > 0) {
			//获取车型 修改只算表头设备类型费用，只要考虑明细费用就可以了。2015-11-6 
			List<String> pk_car_types_list = new ArrayList<String>();
			if(entTransbilityVOs!=null&&entTransbilityVOs.length>0){
				for(EntTransbilityBVO etansb:entTransbilityVOs){
					pk_car_types_list.add(etansb.getPk_car_type().toString());
				}
			}
			//增加表头设备
			if(entVO.getPk_car_type()!=null){
				pk_car_types_list.add(entVO.getPk_car_type());
			}
			String[] pk_car_types = null;
			if(pk_car_types_list != null && pk_car_types_list.size() > 0) {
				
				 pk_car_types = (String[])pk_car_types_list.toArray(new String[pk_car_types_list.size()]);
			}
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			Map<String,List<SegPackBVO>> groupMap = new HashMap<String,List<SegPackBVO>>();
			//对包装按照pack进行分组
			for(SegPackBVO sPackVO : sPackVOs){
				String key = sPackVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<SegPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<SegPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(sPackVO);
			}
			if (groupMap.size() > 0) {
				for(String key : groupMap.keySet()){
					PackInfo packInfo = new PackInfo();
					List<SegPackBVO> voList = groupMap.get(key);
					Integer num = 0;
					UFDouble weight = UFDouble.ZERO_DBL;
					UFDouble volume = UFDouble.ZERO_DBL;
					for(SegPackBVO packBVO : voList){
						num = num + (packBVO.getNum() == null ? 0 : packBVO.getNum());
						weight = weight.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
						volume = volume.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
					}
					packInfo.setPack(key);
					packInfo.setNum(num);
					packInfo.setWeight(weight);
					packInfo.setVolume(volume);
					packInfos.add(packInfo);
				}
			}
			
			List<PayDetailBVO> detailVOs = contractService.buildPayDetailBVO(entVO.getPk_carrier(), segVO
					.getPack_num_count() == null ? 0 : segVO.getPack_num_count().doubleValue(), segVO.getNum_count(),
					entVO.getFee_weight_count().doubleValue(), segVO.getWeight_count() == null ? 0 : segVO
							.getWeight_count().doubleValue(), segVO.getVolume_count() == null ? 0 : segVO
							.getVolume_count().doubleValue(), 2, 1,packInfos,pk_car_types, entVO
							.getPk_corp(), entVO.getUrgent_level(),entVO.getItem_code(),entVO.getPk_trans_line(),
							entVO.getIf_return(),contractBVOs);
			if(detailVOs != null && detailVOs.size() > 0) {
				String transFeeCode = ExpenseTypeConst.ET10;// 得到系统定义的运费的编码
				ExpenseTypeVO etVO = (ExpenseTypeVO) expenseTypeService.getByCode(transFeeCode);// 得到运费的vo
				// 2015-05-27 批量配载不再需要选择计价方式了
				// if(headerVO.getValuation_type() == null) {
				// 计算费用类型为运费的费用明细，取大值，加上其他费用类型的费用
				double maxAmount = 0;
				List<PayDetailBVO> toBeDelete = new ArrayList<PayDetailBVO>();
				for(PayDetailBVO detailVO : detailVOs) {
					// 计算的金额
					UFDouble amount = PZUtils.compute(detailVO.getQuote_type(), detailVO.getValuation_type(),
							detailVO.getPrice_type(), detailVO.getPrice(), segVO.getFee_weight_count(),
							segVO.getVolume_count(), segVO.getNum_count());
					//detailVO.setAmount(amount);
					// 识别所有运费的费用明细
					if(etVO.getPk_expense_type().equals(detailVO.getPk_expense_type())) {// 匹配到运费
						if(amount.doubleValue() < maxAmount) {
							toBeDelete.add(detailVO);
						}
					}
				}
				detailVOs.removeAll(toBeDelete);
				toBeDelete.clear();

				UFDouble cost_amount = UFDouble.ZERO_DBL;// 金额合计
				for(PayDetailBVO detailVO : detailVOs) {
					cost_amount = cost_amount.add(detailVO.getAmount() == null ? UFDouble.ZERO_DBL : detailVO
							.getAmount());
					detailVO.setPk_pay_detail(payVO.getPk_pay_detail());
					detailVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(detailVO);
					toBeUpdate.add(detailVO);

					// 第9步的分摊放到这边执行
					// 按发货单进行分摊
					List<PayDeviBVO> deviVOs = PZUtils.getPayDeviBVO(entVO, invVO, segVO, detailVO);
					toBeUpdate.addAll(deviVOs);
				}
				entVO.setCost_amount(cost_amount);
				// 9、生成分摊表,见第8步
			}
		}
		//设置应付明细经计算后的数据
		payVO.setPack_num_count(entVO.getPack_num_count());
		payVO.setNum_count(entVO.getNum_count());
		payVO.setVolume_count(entVO.getVolume_count());
		payVO.setWeight_count(entVO.getWeight_count());
		payVO.setFee_weight_count(entVO.getFee_weight_count());
		payVO.setCost_amount(entVO.getCost_amount());
		payVO.setUngot_amount(entVO.getCost_amount());// 未付金额默认等于总金额
		// 执行数据库操作
		dao.saveOrUpdate(toBeUpdate);
		
		//yaojiie 	2015 12 15 因为在批量配载中，没有发货单相关信息。
		List<InvoiceVO> invoiceVOs = new ArrayList<InvoiceVO>();
		invoiceVOs.add(invVO);
		CMUtils.totalCostComput(invoiceVOs);
		ExAggEntrustVO aggEntrustVO = new ExAggEntrustVO();
		aggEntrustVO.setParentVO(entVO);
		aggEntrustVO.setTableVO(TabcodeConst.TS_ENT_PACK_B, entPackBVOs.toArray(new EntPackBVO[entPackBVOs.size()]));
		aggEntrustVO.setTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B, transbilityBVOs.toArray(new EntTransbilityBVO[transbilityBVOs.size()]));
		return aggEntrustVO;
	}

	/**
	 * 返回运段的查询条件片段，为了查询多个运段的货品信息
	 * 
	 * @param segVOs
	 * @return
	 */
	private String getSegmentCond(SegmentVO[] segVOs) {
		if(segVOs == null || segVOs.length == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i = 0; i < segVOs.length; i++) {
			buf.append("'");
			buf.append(segVOs[i].getPk_segment());
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}


	//往辅助表加数据。
	public List<SuperVO> addToLinePack(List<EntLineBVO> entlineBVOs ,List<EntPackBVO> entPackBVOs){
		if(entlineBVOs == null || entlineBVOs.size() == 0){
			return null ;
		}
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		if(entPackBVOs != null && entPackBVOs.size() > 0 ){
			for(EntLineBVO entLineBVO : entlineBVOs){
				for(EntPackBVO entPackBVO : entPackBVOs){
					//如果这个路线信息和包装明细属于同一个委托单，则生成一个EntLinePackBVO
					if(entLineBVO.getPk_entrust().equals(entPackBVO.getPk_entrust()) && entLineBVO.getSegment_node() == UFBoolean.TRUE){
						EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
						entLinePackBVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(entLinePackBVO);
						entLinePackBVO.setPk_entrust(entLineBVO.getPk_entrust());
						entLinePackBVO.setPk_ent_line_b(entLineBVO.getPk_ent_line_b());
						entLinePackBVO.setPk_ent_pack_b(entPackBVO.getPk_ent_pack_b());
						entLinePackBVO.setSerialno(entPackBVO.getSerialno());
						entLinePackBVO.setPk_goods(entPackBVO.getPk_goods());
						entLinePackBVO.setGoods_code(entPackBVO.getGoods_code());
						entLinePackBVO.setGoods_name(entPackBVO.getGoods_name());
						entLinePackBVO.setNum(entPackBVO.getNum());
						entLinePackBVO.setPack(entPackBVO.getPack());
						entLinePackBVO.setWeight(entPackBVO.getWeight());
						entLinePackBVO.setUnit_weight(entPackBVO.getUnit_weight());
						entLinePackBVO.setVolume(entPackBVO.getVolume());
						entLinePackBVO.setUnit_volume(entPackBVO.getUnit_volume());
						entLinePackBVO.setLength(entPackBVO.getLength());
						entLinePackBVO.setWeight(entPackBVO.getWeight());
						entLinePackBVO.setHeight(entPackBVO.getHeight());
						entLinePackBVO.setTrans_note(entPackBVO.getTrans_note());
						entLinePackBVO.setLow_temp(entPackBVO.getLow_temp());
						entLinePackBVO.setHight_temp(entPackBVO.getHight_temp());
						entLinePackBVO.setReference_no(entPackBVO.getReference_no());
						entLinePackBVO.setMemo(entPackBVO.getMemo());
						entLinePackBVO.setPack_num_count(entPackBVO.getPack_num_count());
						entLinePackBVO.setPlan_num(entPackBVO.getPlan_num());
						entLinePackBVO.setPlan_pack_num_count(entPackBVO.getPlan_pack_num_count());
						entLinePackBVO.setDef1(entPackBVO.getDef1());
						entLinePackBVO.setDef2(entPackBVO.getDef2());
						entLinePackBVO.setDef3(entPackBVO.getDef3());
						entLinePackBVO.setDef4(entPackBVO.getDef4());
						entLinePackBVO.setDef5(entPackBVO.getDef5());
						entLinePackBVO.setDef6(entPackBVO.getDef6());
						entLinePackBVO.setDef7(entPackBVO.getDef7());
						entLinePackBVO.setDef8(entPackBVO.getDef8());
						entLinePackBVO.setDef9(entPackBVO.getDef9());
						entLinePackBVO.setDef10(entPackBVO.getDef10());
						entLinePackBVO.setDef11(entPackBVO.getDef11());
						entLinePackBVO.setDef12(entPackBVO.getDef12());
						VOs.add(entLinePackBVO);
					}
				}
			}
		}
		return VOs;
	}
	
	public void setTempToEntLot(EntLotVO entLotVO , EntPackBVO[] entPackBVOs){
		if(entLotVO == null || entPackBVOs == null || entPackBVOs.length == 0){
			return;
		}
		UFDouble low_temp = null;
		UFDouble hight_temp = null;
		for(EntPackBVO packB : entPackBVOs){
			if(packB.getLow_temp() != null){
				if(low_temp == null){
					low_temp = packB.getLow_temp();
				}else{
					if(packB.getLow_temp().doubleValue() - low_temp.doubleValue() > 0){
						low_temp = packB.getLow_temp();
					}
				}
			}
			if(packB.getHight_temp() != null){
				if(hight_temp == null){
					hight_temp = packB.getHight_temp();
				}else{
					if(packB.getHight_temp().doubleValue() - low_temp.doubleValue() < 0){
						hight_temp = packB.getHight_temp();
					}
				}
			}
		}
		entLotVO.setHight_temp(hight_temp);
		entLotVO.setLow_temp(low_temp);
	}
	
	public void setSpeedLimitToEntLot(EntLotVO entLotVO , EntTransbilityBVO[] entTransbilityBVOs){
		if(entLotVO == null || entTransbilityBVOs == null || entTransbilityBVOs.length == 0){
			return;
		}
		List<String> carnos = new ArrayList<String>();
		for(EntTransbilityBVO transBVO : entTransbilityBVOs){
			if(StringUtils.isNotBlank(transBVO.getCarno())){
				carnos.add(transBVO.getCarno());
			}
		}
		if(carnos.size() == 0){
			return;
		}
		CarVO[] carVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarVO.class, "carno in" + NWUtils.buildConditionString(carnos));
		if(carVOs == null || carVOs.length == 0){
			return;
		}
		UFDouble speedLimit = null;
		for(CarVO car : carVOs){
			if(car.getSpeed_limit() != null){
				if(speedLimit == null){
					speedLimit = car.getSpeed_limit();
				}else{
					//去最小
					if(car.getSpeed_limit().doubleValue() - speedLimit.doubleValue() < 0){
						speedLimit = car.getSpeed_limit();
					}
				}
			}
		}
		entLotVO.setSpeed_limit(speedLimit);
	}
}
