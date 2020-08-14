package com.tms.service.cm.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.BillStatus;
import com.tms.constants.PayDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 应收明细导入批次导入
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class PayDetailLotExcelImporter extends BillExcelImporter {

	public PayDetailLotExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new PayDetailVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		//这个Map里存批次号和这个批次号对应的总费用明细子表信息
		List<String> lots = new ArrayList<String>();
		Map<String,PayDetailBVO[]> lotAndPayDetailBVOs = new HashMap<String, PayDetailBVO[]>();
		for(AggregatedValueObject aggVO : aggVOs){
			PayDetailVO payDetailVO = (PayDetailVO)aggVO.getParentVO();
			ExAggPayDetailVO exAggpayDetailVO = (ExAggPayDetailVO) aggVO;
			PayDetailBVO[] payDetailBVOs = (PayDetailBVO[])exAggpayDetailVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
			lotAndPayDetailBVOs.put(payDetailVO.getVbillno(), payDetailBVOs);
			//因为应付明细模块没有 lot 字段，所以将lot放在vbillno字段，方便使用
			lots.add(payDetailVO.getVbillno());
		}
		String lotsCond = NWUtils.buildConditionString(lots.toArray(new String[lots.size()]));
		//获得批次号对对应的发货单，可能导入的批次号有误，没有对应的委托单所以要对批次号进行验证
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				" vbillstatus <> 24 and lot in " + lotsCond);
		List<String> entrustVbillnos = new ArrayList<String>();
		for(EntrustVO entrustVO : entrustVOs){
			lots.remove(entrustVO.getLot());
			entrustVbillnos.add(entrustVO.getVbillno());
		}
		if(lots.size() > 0){
			String msg = new String();
			for(String lot : lots){
				msg = msg + lot;
			}
			throw new BusiException("批次号[?]没有查询到，或者委托单状态有误，请检查数据！",msg);
		}
		String entrustVbillnoCond = NWUtils.buildConditionString(entrustVbillnos.toArray(new String[entrustVbillnos.size()]));
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				" pay_type = 0 and entrust_vbillno in " + entrustVbillnoCond);
		//对应付明细状态是原始凭证，新建的，进行分摊操作。
		Map<String,List<PayDetailVO>> lotAndPayDetailVOs = new HashMap<String, List<PayDetailVO>>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			for (EntrustVO entrustVO : entrustVOs) {
				if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
					if(payDetailVO.getVbillstatus() != BillStatus.NEW){
						throw new BusiException("批次号[?]对应的应付明细单号[?]的状态不正确，请检查数据！",entrustVO.getLot(),payDetailVO.getVbillno());
					}else{
						List<PayDetailVO> PayDetailVOs = lotAndPayDetailVOs.get(entrustVO.getLot());
						if(PayDetailVOs == null){
							PayDetailVOs = new ArrayList<PayDetailVO>();
							lotAndPayDetailVOs.put(entrustVO.getLot(), PayDetailVOs);
						}
						PayDetailVOs.add(payDetailVO);
					}
				}
			}
		}
		List<String> payDetailPks = new ArrayList<String>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			payDetailPks.add(payDetailVO.getPk_pay_detail());
		}
		String payDetailPksCond = NWUtils.buildConditionString(payDetailPks.toArray(new String[payDetailPks.size()]));
		PayDetailBVO[] oldpayDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				" pk_pay_detail in " + payDetailPksCond);
		
		//将费用分摊到每个应付明细上，并生成应付明细子表信息。
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		List<PayDetailBVO> allNewPayDetailBVOs = new ArrayList<PayDetailBVO>();
		for(String payDetailKey : lotAndPayDetailVOs.keySet()){
			for(String payDetailBKey : lotAndPayDetailBVOs.keySet()){
				if(payDetailKey.equals(payDetailBKey)){//属于同一批次 按计费重进行分摊
					UFDouble allFee_weight_count = UFDouble.ZERO_DBL;
					//yaojiie 2015 12 29 增加一个功能，当所有的计费重都是0的时候，评价分摊。
					int index = 0;
					for(PayDetailVO payDetailVO :lotAndPayDetailVOs.get(payDetailKey)){
						allFee_weight_count = allFee_weight_count.add(payDetailVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : payDetailVO.getFee_weight_count());
						index++;
					}
					for(PayDetailVO payDetailVO :lotAndPayDetailVOs.get(payDetailKey)){
						List<PayDetailBVO> allDetailBVOs = new ArrayList<PayDetailBVO>();
						for(PayDetailBVO excelPayDetailBVO :lotAndPayDetailBVOs.get(payDetailBKey)){
							//if(payDetailVO.getFee_weight_count() != null && payDetailVO.getFee_weight_count() != UFDouble.ZERO_DBL){
								PayDetailBVO newPayDetailBVO = new PayDetailBVO();
								newPayDetailBVO.setStatus(VOStatus.NEW);
								NWDao.setUuidPrimaryKey(newPayDetailBVO);
								newPayDetailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail());
								newPayDetailBVO.setPrice_type(excelPayDetailBVO.getPrice_type());
								newPayDetailBVO.setValuation_type(excelPayDetailBVO.getValuation_type());
								newPayDetailBVO.setPk_expense_type(excelPayDetailBVO.getPk_expense_type());
								newPayDetailBVO.setQuote_type(excelPayDetailBVO.getQuote_type());
								newPayDetailBVO.setPrice(excelPayDetailBVO.getPrice());
								if(!allFee_weight_count.equals(UFDouble.ZERO_DBL)){
									if(payDetailVO.getFee_weight_count() == null ){
										newPayDetailBVO.setAmount(UFDouble.ZERO_DBL);
									}else{
										newPayDetailBVO.setAmount(excelPayDetailBVO.getAmount().multiply(payDetailVO.getFee_weight_count().div(allFee_weight_count)));
									}
								}else{
									newPayDetailBVO.setAmount(excelPayDetailBVO.getAmount().multiply(1.0/index));
								}
								
								newPayDetailBVO.setMemo(excelPayDetailBVO.getMemo());
								allNewPayDetailBVOs.add(newPayDetailBVO);
								allDetailBVOs.add(newPayDetailBVO);
								VOs.add(newPayDetailBVO);
							//}
						}
						for(PayDetailBVO payDetailBVO : oldpayDetailBVOs){
							if(payDetailBVO.getPk_pay_detail().equals(payDetailVO.getPk_pay_detail())){
								allDetailBVOs.add(payDetailBVO);
							}
						}
						payDetailVO.setStatus(VOStatus.UPDATED);
						VOs.add(payDetailVO);
						// 重新计算应付对账的金额 对账单明细实际上不存储金额，只是关联应付明细
						CMUtils.processExtenal(payDetailVO, allDetailBVOs);
					}
				}
			}
		}
		List<PayDeviBVO> payDeviBVOs = this.getPayDeviBVOs(entrustVbillnos, payDetailVOs, allNewPayDetailBVOs);
		VOs.addAll(payDeviBVOs);
		NWDao.getInstance().saveOrUpdate(VOs);
		
		//计算金额利润
		String entCond = NWUtils.buildConditionString(entrustVbillnos.toArray(new String[entrustVbillnos.size()]));
		String sql = "SELECT DISTINCT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH (NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
				+ " LEFT JOIN ts_entrust ent WITH (NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
				+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
				+ " AND ent.vbillno IN " 
				+ entCond;

		List<InvoiceVO> invoiceVOs =  NWDao.getInstance().queryForList(sql, InvoiceVO.class);
		CMUtils.totalCostComput(invoiceVOs);
		
	}
	
	@SuppressWarnings("unchecked")
	private List<PayDeviBVO> getPayDeviBVOs (List<String> entrustVbillnos , PayDetailVO[] payDetailVOs , List<PayDetailBVO> newPayDetailBVOs){
		String entCond = NWUtils.buildConditionString(entrustVbillnos.toArray(new String[entrustVbillnos.size()]));
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class," vbillno in "+ entCond);
		List<String> pk_entrusts = new ArrayList<String>();
		if(entrustVOs != null && entrustVOs.length > 0){
			for(EntrustVO entrustVO : entrustVOs){
				pk_entrusts.add(entrustVO.getPk_entrust());
			}
		}
		
		String pkEntCond = NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()]));
		EntSegBVO[] entSegBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntSegBVO.class, "pk_entrust in " + pkEntCond);
		
		SegmentVO[] segmentVOs = null;
		if(entSegBVOs != null && entSegBVOs.length > 0){
			// 得到所有运段
			String segCond = getSegmentCond(entSegBVOs);
			
			segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + segCond);
			
		}
		List<Map<String,Object>> paramMaps = new ArrayList<Map<String,Object>>();
		
		for (EntrustVO entrustVO : entrustVOs) {
			
			Map<String, UFDouble> fee_weight_countMap = new HashMap<String, UFDouble>();
			Map<String, UFDouble> volume_countMap = new HashMap<String, UFDouble>();
			
			Map<String, Object> map = new HashMap<String, Object>();
			for (EntSegBVO entSegBVO : entSegBVOs) {
				for (SegmentVO segmentVO : segmentVOs) {
					if (entrustVO.getPk_entrust().equals(entSegBVO.getPk_entrust())
							&& segmentVO.getPk_segment().equals(entSegBVO.getPk_segment())) {
						String invoice_vbillno = segmentVO.getInvoice_vbillno();
						UFDouble fee_weight_count = fee_weight_countMap.get(invoice_vbillno);
						if (fee_weight_count == null) {
							fee_weight_countMap.put(invoice_vbillno, segmentVO.getFee_weight_count() == null
									? UFDouble.ZERO_DBL : segmentVO.getFee_weight_count());
						} else {
							fee_weight_countMap.put(invoice_vbillno,
									fee_weight_count.add(segmentVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL
											: segmentVO.getFee_weight_count()));
						}
						UFDouble volume_count = volume_countMap.get(invoice_vbillno);
						if (volume_count == null) {
							volume_countMap.put(invoice_vbillno, segmentVO.getVolume_count() == null ? UFDouble.ZERO_DBL
									: segmentVO.getVolume_count());
						} else {
							volume_countMap.put(invoice_vbillno, volume_count.add(segmentVO.getVolume_count() == null
									? UFDouble.ZERO_DBL : segmentVO.getVolume_count()));
						}
					}
				}
			}
			map.put("entVO", entrustVO);
			map.put("fee_weight_countMap", fee_weight_countMap);
			map.put("volume_countMap", volume_countMap);

			List<PayDetailBVO> payDetailBVOs = new ArrayList<PayDetailBVO>();
			for (PayDetailVO payDetailVO : payDetailVOs) {
				for (PayDetailBVO payDetailBVO : newPayDetailBVOs) {
					if (entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())
							&& payDetailVO.getPk_pay_detail().equals(payDetailBVO.getPk_pay_detail())) {
						payDetailBVOs.add(payDetailBVO);
					}
				}
			}
			map.put("PayDetailBVOs", payDetailBVOs);
			paramMaps.add(map);
		}

		List<PayDeviBVO> payDeviBVOsList = new ArrayList<PayDeviBVO>();
		for (Map<String, Object> map : paramMaps) {
			EntrustVO entVO = (EntrustVO) map.get("entVO");
			Map<String, UFDouble> fee_weight_countMap1 = (Map<String, UFDouble>) map.get("fee_weight_countMap");
			Map<String, UFDouble> volume_countMap1 = (Map<String, UFDouble>) map.get("volume_countMap");
			List<PayDetailBVO> PayDetailBVOsList = (List<PayDetailBVO>) map.get("PayDetailBVOs");
			PayDetailBVO[] PayDetailBVOs = PayDetailBVOsList.toArray(new PayDetailBVO[PayDetailBVOsList.size()]);

			List<PayDeviBVO> payDeviBVOs = this.getPayDeviBVOs(entVO, fee_weight_countMap1, volume_countMap1,
					PayDetailBVOs);
			payDeviBVOsList.addAll(payDeviBVOs);
		}
		return payDeviBVOsList;
	}
	
	private List<PayDeviBVO> getPayDeviBVOs(EntrustVO entVO, Map<String, UFDouble> fee_weight_countMap,
			Map<String, UFDouble> volume_countMap, PayDetailBVO[] detailBVOs) {
		
		//yaojiie 2015 12 22 
		UFDouble allFee_weight_count = UFDouble.ZERO_DBL;
		UFDouble allVolume_count = UFDouble.ZERO_DBL;
		for (String key : fee_weight_countMap.keySet()) {
			allFee_weight_count = allFee_weight_count.add(fee_weight_countMap.get(key) == null ? UFDouble.ZERO_DBL : fee_weight_countMap.get(key));
		}
		for (String key : volume_countMap.keySet()) {
			allVolume_count = allVolume_count.add(volume_countMap.get(key) == null ? UFDouble.ZERO_DBL : volume_countMap.get(key));
		}
		
		String payDeviType = ParameterHelper.getPayDeviType();// 得到分摊类型
		String payDeviDimension = ParameterHelper.getPayDeviDimension();// 得到分摊维度
		boolean allDeviValueIsZero = true;// 如果分摊数量都为0，那么平均分摊
		List<PayDeviBVO> allDeviBVOs = new ArrayList<PayDeviBVO>();
		for(PayDetailBVO detailBVO : detailBVOs) {
			List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();// 每个费用的分摊费用明细
			for(String key : fee_weight_countMap.keySet()) {// 这里循环fee_weight_countMap和循环volume_countMap是一样的
				InvoiceVO invVO = NWDao.getInstance().queryByCondition(InvoiceVO.class,
						new String[] { "pk_invoice", "vbillno" }, "vbillno=?", key);
				if(PayDetailConst.PAY_DEVI_DIMENSION.INVOICE.equals(payDeviDimension)) {
					PayDeviBVO deviBVO = new PayDeviBVO();
					deviBVO.setPk_entrust(entVO.getPk_entrust());
					deviBVO.setPk_invoice(invVO.getPk_invoice());
					deviBVO.setPk_carrier(entVO.getPk_carrier());
					deviBVO.setPk_car_type(entVO.getPk_car_type());
					deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
					if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
					} else {
						if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
								|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
							double fee_weight_count = fee_weight_countMap.get(key).doubleValue();
							if(allFee_weight_count.doubleValue() == 0) {
								deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviBVO.setSys_devi_amount(detailBVO.getAmount().div(allFee_weight_count)
										.multiply(fee_weight_count));
							}
							if(fee_weight_count != 0) {
								allDeviValueIsZero = false;
							}
						} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
							// 按体积进行分摊，得到当前发货单的总体积
							double volume_count = volume_countMap.get(key).doubleValue();
							if(allVolume_count.doubleValue() == 0) {
								deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviBVO.setSys_devi_amount(detailBVO.getAmount().div(allVolume_count)
										.multiply(volume_count));
							}
							if(volume_count != 0) {
								allDeviValueIsZero = false;
							}
						}
					}
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());// 手工分摊金额：默认等于系统分摊金额
					deviBVO.setPk_pay_detail(detailBVO.getPk_pay_detail());
					deviBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(deviBVO);
					deviBVOs.add(deviBVO);
				} else if(PayDetailConst.PAY_DEVI_DIMENSION.DETAIL.equals(payDeviDimension)) {
					// FIXME 这里是否应该使用运段的货品明细？
					InvPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
							"pk_invoice=?", invVO.getPk_invoice());
					if(packBVOs == null || packBVOs.length == 0) {
						// 没有货品信息，不需要分摊
						continue;
					}
					TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
							entVO.getPk_trans_type());
					if(typeVO == null) {
						throw new BusiException("当前的运输方式已经被删除，pk[?]！",entVO.getPk_trans_type());
					}
					if(typeVO.getRate() == null) {
						typeVO.setRate(UFDouble.ZERO_DBL);// 没有定义相应的换算比率，默认为0
					}
					for(InvPackBVO packBVO : packBVOs) {
						PayDeviBVO deviBVO = new PayDeviBVO();
						deviBVO.setPk_entrust(entVO.getPk_entrust());
						deviBVO.setPk_invoice(invVO.getPk_invoice());
						deviBVO.setInvoice_serialno(packBVO.getSerialno());
						deviBVO.setPk_carrier(entVO.getPk_carrier());
						deviBVO.setPk_car_type(entVO.getPk_car_type());
						deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
						if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
							// 总金额为0，不需要分摊
						} else {
							if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
									|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
								// 按重量分摊
								// 计算当前这个发货单的总计费重
								UFDouble volume = packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume();
								UFDouble weight = packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight();
								double fee_weight_count = weight.doubleValue();
								double fee = volume.multiply(typeVO.getRate()).doubleValue();
								if(fee > weight.doubleValue()) {
									fee_weight_count = fee;
								}

								if(allFee_weight_count.doubleValue() == 0) {
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailBVO.getAmount().div(allFee_weight_count)
											.multiply(fee_weight_count));
								}
								if(fee_weight_count != 0) {
									allDeviValueIsZero = false;
								}
							} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
								// 按体积进行分摊，这里按照行分摊，必须读取行的体积
								double volume_count = packBVO.getVolume() == null ? 0 : packBVO.getVolume()
										.doubleValue();
								if(allVolume_count.doubleValue() == 0) {
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailBVO.getAmount().div(allVolume_count)
											.multiply(volume_count));
								}
								if(volume_count != 0) {
									allDeviValueIsZero = false;
								}
							}
						}
						// 2015-3-18 对分摊费用设置四舍五入
						deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
								.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
						deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());// 手工分摊金额：默认等于系统分摊金额
						deviBVO.setPk_pay_detail(detailBVO.getPk_pay_detail());
						deviBVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(deviBVO);
						deviBVOs.add(deviBVO);
					}
				} else {
					throw new BusiException("系统参数中，设置的[pay_devi_dimension]参数不支持，当前值是[?]！",payDeviDimension);
				}
			}
			if(allDeviValueIsZero) {
				// 平均分摊
				for(PayDeviBVO deviBVO : deviBVOs) {
					//当手工删除费用时，getAmount 会为NULL，这次做下NULL 的判断。2015-11-1 Jonathan
					//deviBVO.setSys_devi_amount(detailBVO.getAmount().div(deviBVOs.size()));
					deviBVO.setSys_devi_amount((detailBVO.getAmount()== null ? UFDouble.ZERO_DBL : detailBVO.getAmount()).div(deviBVOs.size()));
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());
				}
			}
			allDeviBVOs.addAll(deviBVOs);
		}
		return allDeviBVOs;
	}
	
	private String getSegmentCond(EntSegBVO[] entSegBVOs) {
		if(entSegBVOs == null || entSegBVOs.length == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i = 0; i < entSegBVOs.length; i++) {
			buf.append("'");
			buf.append(entSegBVOs[i].getPk_segment());
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}
}
