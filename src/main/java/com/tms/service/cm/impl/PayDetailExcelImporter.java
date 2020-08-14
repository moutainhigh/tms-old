package com.tms.service.cm.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
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
 * 应收明细导入
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class PayDetailExcelImporter extends BillExcelImporter {

	public PayDetailExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new PayDetailVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		List<PayDetailBVO> newPayDetailBVOs = new ArrayList<PayDetailBVO>();
		List<String> vbillnos = new ArrayList<String>();
		List<PayDetailVO> importPayDetailVOs = new ArrayList<PayDetailVO>();
		for (AggregatedValueObject aggVO : aggVOs) {
			PayDetailVO payDetailVO = (PayDetailVO)aggVO.getParentVO();
			importPayDetailVOs.add(payDetailVO);
			ExAggPayDetailVO exAggpayDetailVO = (ExAggPayDetailVO) aggVO;
			PayDetailBVO[] payDetailBVOs = (PayDetailBVO[])exAggpayDetailVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
			//导入文件中的应付明细BVO
			for(PayDetailBVO payDetailBVO : payDetailBVOs){
				payDetailBVO.setPk_pay_detail(payDetailVO.getVbillno());
				newPayDetailBVOs.add(payDetailBVO);
			}
			
			vbillnos.add(payDetailVO.getVbillno());
		}
		String cond = NWUtils.buildConditionString(vbillnos.toArray(new String[vbillnos.size()]));
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				" vbillno in " + cond);
		if (payDetailVOs == null || payDetailVOs.length == 0) {
			throw new BusiException("没有查询到应付明细单号，请检查数据！");
		}
		//检查单据状态，并将查询到的单据号放入一个List中，和原有的传入的List进行比较。
		List<String> entrustVbillnos = new ArrayList<String>();
		List<String> payDetailVOvbillnos = new ArrayList<String>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			if(payDetailVO.getVbillstatus() != BillStatus.NEW){
				throw new BusiException("单据号[?]状态不是[新建]，不允许导入，请检查数据！",payDetailVO.getVbillno());
			}
			//预留def1-10 字段，导入使用。
			for(PayDetailVO importPayDetailVO : importPayDetailVOs){
				if(importPayDetailVO.getVbillno().equals(payDetailVO.getVbillno())){
					payDetailVO.setDef1(importPayDetailVO.getDef1());
					payDetailVO.setDef2(importPayDetailVO.getDef2());
					payDetailVO.setDef3(importPayDetailVO.getDef3());
					payDetailVO.setDef4(importPayDetailVO.getDef4());
					payDetailVO.setDef5(importPayDetailVO.getDef5());
					payDetailVO.setDef6(importPayDetailVO.getDef6());
					payDetailVO.setDef7(importPayDetailVO.getDef7());
					payDetailVO.setDef8(importPayDetailVO.getDef8());
					payDetailVO.setDef9(importPayDetailVO.getDef9());
					payDetailVO.setDef10(importPayDetailVO.getDef10());
					break;
				}
			}
			payDetailVOvbillnos.add(payDetailVO.getVbillno());
			entrustVbillnos.add(payDetailVO.getEntrust_vbillno());
			
		}
		// 检查导入数据是否与数据库数据一致。
		//比较数据,只有当结果集不一致时，才需要进行比较
		String errormesg = "";
		if(vbillnos.size() != payDetailVOs.length){
			for(String vbillno : vbillnos){
				if(!payDetailVOvbillnos.contains(vbillno)){
					errormesg = errormesg + (vbillno + " ");
				}
			}
			if(StringUtils.isNotBlank(errormesg)){
				throw new BusiException("单据号[?]不存在，请检查数据！",errormesg.substring(0, errormesg.length()-1));
			}
		}
		
		// 获取数据库已有的应付明细BVO
		List<String> pk_pay_details = new ArrayList<String>();
		for (PayDetailVO payDetailVO : payDetailVOs) {
			pk_pay_details.add(payDetailVO.getPk_pay_detail());
		}
		String condOld = NWUtils.buildConditionString(pk_pay_details.toArray(new String[pk_pay_details.size()]));
		PayDetailBVO[] oldpayDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				" pk_pay_detail in " + condOld);
		
		
		// 检查结果一致，并获取到所有的应付明细BVO，进行保存动作。
		// 遍历payDetailVOs，将payDetailVO和payDetailBVO进行匹配
		for (PayDetailVO payDetailVO : payDetailVOs) {
			// 将头VO添加到
			VOs.add(payDetailVO);
			List<PayDetailBVO> allDetailBVOs = new ArrayList<PayDetailBVO>();
			for (PayDetailBVO payDetailBVO : newPayDetailBVOs) {
				// 数据库里查询到的VO的getPk_pay_detail，记录的是PK，而导入的BVO里getPk_pay_detail记录的是单据号
				if (payDetailVO.getVbillno().equals(payDetailBVO.getPk_pay_detail())) {
					allDetailBVOs.add(payDetailBVO);
					VOs.add(payDetailBVO);
					payDetailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail());
					payDetailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(payDetailBVO);
				}
			}
			for (PayDetailBVO payDetailBVO : oldpayDetailBVOs) {
				// 将数据库里已有的明细也加到总明细中，进行金额合计
				if (payDetailVO.getPk_pay_detail().equals(payDetailBVO.getPk_pay_detail())) {
					allDetailBVOs.add(payDetailBVO);
				}
			}
			// 重新计算应付对账的金额 对账单明细实际上不存储金额，只是关联应付明细
			CMUtils.processExtenal(payDetailVO, allDetailBVOs);
		}
		List<PayDeviBVO> payDeviBVOs = this.getPayDeviBVOs(entrustVbillnos, payDetailVOs, newPayDetailBVOs);
		VOs.addAll(payDeviBVOs);
		NWDao.getInstance().saveOrUpdate(VOs);
		
		//计算金额利润
		String entCond = NWUtils.buildConditionString(entrustVbillnos.toArray(new String[entrustVbillnos.size()]));
		String sql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH (NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
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
