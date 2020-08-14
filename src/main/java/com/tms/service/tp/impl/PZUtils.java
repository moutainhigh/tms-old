package com.tms.service.tp.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.utils.ParameterHelper;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.SpringContextHolder;

import com.tms.constants.AddressConst;
import com.tms.constants.PayDetailConst;
import com.tms.service.base.CarrService;
import com.tms.service.base.TransTypeService;
import com.tms.service.base.impl.TransTypeServiceImpl;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntTransHisBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 配载工具类，主要用来计算单据的金额、计费重、体积重等相关信息
 * 
 * @author xuqc
 * @date 2014-4-29 下午10:14:50
 */
public class PZUtils {

	static Logger logger = Logger.getLogger(PZUtils.class);
	
	private static CarrService carrService = SpringContextHolder.getBean("carrServiceImpl");

	/**
	 * 计算计费重、体积重
	 * 
	 * @param pk_trans_type
	 *            运输方式
	 * @param volume_count
	 *            总体积
	 * @param weight_count
	 *            总重量
	 * @return
	 */
	public static Map<String, UFDouble> computeFeeWeightCount(String pk_carrier, String pk_trans_type,
			String deli_city,String arri_city,
			UFDouble volume_count, UFDouble weight_count) {
		if(StringUtils.isBlank(pk_trans_type) || StringUtils.isBlank(pk_carrier)) {
			return null;
		}
		Map<String, UFDouble> retMap = new HashMap<String, UFDouble>();
		UFDouble rate = carrService.getFeeRate(pk_carrier, 
				pk_trans_type, deli_city,arri_city);
		UFDouble volume_weight_count = UFDouble.ZERO_DBL;
		UFDouble fee_weight_count = UFDouble.ZERO_DBL;
		weight_count=(weight_count == null ? UFDouble.ZERO_DBL : weight_count);
		if(rate != null && rate.doubleValue() != 0){
			volume_weight_count = rate.multiply(volume_count == null ? UFDouble.ZERO_DBL : volume_count);
			if(volume_weight_count.doubleValue() < weight_count.doubleValue()) {
				fee_weight_count = weight_count;
			} else {
				fee_weight_count = volume_weight_count;
			}
		}
		retMap.put("volume_weight_count", volume_weight_count);
		retMap.put("fee_weight_count", fee_weight_count);
		return retMap;
	}

	/**
	 * 委托单计算费用明细的金额
	 * 
	 * @param quote_type
	 *            报价类型
	 * @param valuation_type
	 *            计价方式
	 * @param price_type
	 *            价格类型
	 * @param price
	 *            单价
	 * @param fee_weight_count
	 *            计费重
	 * @param volume_count
	 *            总体积
	 * @param num_count
	 *            设备数
	 * @return
	 */
	public static UFDouble compute(Integer quote_type, Integer valuation_type, Integer price_type, UFDouble price,
			UFDouble fee_weight_count, UFDouble volume_count, Integer num_count) {
		UFDouble amount = UFDouble.ZERO_DBL;
		if(valuation_type == null || price == null || price.doubleValue() == 0) {
			return amount;
		}
		if(quote_type != null && quote_type.intValue() == 0) {// 报价类型=区间报价
			if(price_type != null && price_type == 0) {// 价格类型=单价
				int i_valuation_type = valuation_type.intValue();

				switch(i_valuation_type){
				case 0:
					// 重量
					amount = fee_weight_count.multiply(price);
					break;
				case 1: // 体积
					amount = volume_count.multiply(price);
					break;
				case 2: // 件数
					amount = price.multiply(num_count);
					break;
				case 3: // 设备-这里是和发货单不同的地方，这里只有设备类型(车辆类型)，没有设备数量,不需要计算
					break;
				case 4: // 吨公里-这里是和发货单不同的地方，不需要计算
					break;
				case 6: // 节点 FIXME 后面会用到
					break;
				}
			}
			//增加设备金额计算2015-11-6 jonathan
			else if(price_type != null && price_type == 1 && valuation_type.equals(3)){
				
				amount = price;
			}
			
		}
		return amount;
	}

	/**
	 * 计算分摊费用，当批量配载、修改异常事故时调用
	 * 
	 * @param entVO
	 * @param invVOs
	 * @param segVOs
	 * @param detailBVOs
	 *            待分摊的费用
	 * @return
	 */
	public static List<PayDeviBVO> getPayDeviBVOs(EntrustVO entVO, InvoiceVO[] invVOs, SegmentVO[] segVOs,
			PayDetailBVO[] detailBVOs) {
		logger.info("当批量配载、修改异常事故时调用重新计算分摊费用...");
		List<PayDeviBVO> retList = new ArrayList<PayDeviBVO>();
		if(entVO == null || detailBVOs == null || detailBVOs.length == 0) {
			return retList;
		}
		logger.info("START----------------" + entVO.getVbillno() + "------------------");
		// 读取相关的发货单
		if(invVOs == null) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有传入发货单VOs，查询委托单关联的发货单...");
			invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
					"pk_invoice in (select pk_invoice from ts_ent_inv_b WITH(NOLOCK) where isnull(dr,0)=0 and pk_entrust=?)",
					entVO.getPk_entrust());
			if(invVOs == null || invVOs.length == 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有查询到任何关联的发货单,直接返回...");
				return retList;
			}
		}
		
		if(segVOs == null) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有传入运段VOs，查询委托单关联的运段...");
			segVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"pk_segment in (select pk_segment from ts_ent_seg_b WITH(NOLOCK) where isnull(dr,0)=0 and pk_entrust=?)",
					entVO.getPk_entrust());
			if(segVOs == null || segVOs.length == 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有查询到任何关联的运段,直接返回...");
				return retList;
			}
		}
		
		//yaojiie 2015 12 22 
		UFDouble allFee_weight_count = UFDouble.ZERO_DBL;
		UFDouble allVolume_count = UFDouble.ZERO_DBL;
		for(SegmentVO segVO : segVOs){
			allFee_weight_count = allFee_weight_count.add(segVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : segVO.getFee_weight_count());
			allVolume_count = allVolume_count.add(segVO.getVolume_count() == null ? UFDouble.ZERO_DBL : segVO.getVolume_count());
		}
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]对每个费用明细进行分摊，共需要分摊" + detailBVOs.length + "个费用明细...");
		int index = 1;
		for(PayDetailBVO detailBVO : detailBVOs) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]开始分摊第" + index + "个费用明细,费用类型："
					+ detailBVO.getPk_expense_type() + "...");
			// 删除该应付明细，并且该费用类型的分摊记录
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]查询当前费用类型的旧的分摊记录...");
			PayDeviBVO[] oldDeviBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class,
					"pk_pay_detail=? and pk_expense_type=?", detailBVO.getPk_pay_detail(),
					detailBVO.getPk_expense_type());
			if(oldDeviBVOs != null && oldDeviBVOs.length > 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]删除当前费用类型的旧的分摊记录，共删除" + oldDeviBVOs.length + "条...");
				NWDao.getInstance().delete(oldDeviBVOs);
			}
			if(detailBVO.getStatus() == VOStatus.DELETED) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]第" + index + "条费用明细是删除状态，则不用分摊...");
				continue;
			}
			// 第9步的分摊放到这边执行
			// 按发货单进行分摊
			String payDeviType = ParameterHelper.getPayDeviType();// 得到分摊类型
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊类型：" + payDeviType + "...");
			String payDeviDimension = ParameterHelper.getPayDeviDimension();// 得到分摊维度
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊维度：" + payDeviDimension + "...");
			boolean allDeviValueIsZero = true;// 如果分摊数量都为0，那么平均分摊
			List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
			for(int i = 0; i < invVOs.length; i++) {
				InvoiceVO invVO = invVOs[i];
				if(PayDetailConst.PAY_DEVI_DIMENSION.INVOICE.equals(payDeviDimension)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据发货单维度进行分摊...");
					PayDeviBVO deviVO = new PayDeviBVO();
					deviVO.setPk_entrust(entVO.getPk_entrust());
					deviVO.setPk_invoice(invVO.getPk_invoice());
					deviVO.setPk_carrier(entVO.getPk_carrier());
					deviVO.setPk_car_type(entVO.getPk_car_type());
					deviVO.setPk_expense_type(detailBVO.getPk_expense_type());
					if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]金额为0，分摊金额也为0...");
						// 总金额为0，分摊金额都为0
					} else {
						if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
								|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]根据计费重进行分摊...");
							// 按及计费重分摊
							// 计算当前发货单关联的运段的总计费重
							double fee_weight_count = 0;
							for(SegmentVO segVO : segVOs) {
								if(segVO.getInvoice_vbillno().equals(invVO.getVbillno())) { // 该运段属于该发货单
									//yaojiie 2015 12 15 当没有货品明细时，出现计费重字段为空的情况，会出现空指针错误
									if(segVO.getFee_weight_count() != null){
										fee_weight_count += segVO.getFee_weight_count().doubleValue();
									}
								}
							}
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该发货单关联的运段的总计费重：" + fee_weight_count + "...");
							if(
									allFee_weight_count.doubleValue() == 0) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单关联的运段的总计费重为0，分摊金额也设置为0...");
								deviVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviVO.setSys_devi_amount(detailBVO.getAmount().div(allFee_weight_count)
										.multiply(fee_weight_count));
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]改发货单的分摊金额为:" + deviVO.getSys_devi_amount().doubleValue() + "...");
							}
							if(fee_weight_count != 0) {
								allDeviValueIsZero = false;
							}
						} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]根据体积进行分摊...");
							// 按体积进行分摊，得到当前发货单关联的运段的总体积
							double volume_count = 0;
							for(SegmentVO segVO : segVOs) {
								if(segVO.getInvoice_vbillno().equals(invVO.getVbillno())) { // 该运段属于该发货单
									volume_count += segVO.getVolume_count() == null ? 0 : segVO.getVolume_count()
											.doubleValue();
								}
							}
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该发货单关联的运段的总体积：" + volume_count + "...");
							if(allVolume_count.doubleValue() == 0) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单关联的运段的总体积为0，分摊金额也设置为0...");
								deviVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviVO.setSys_devi_amount(detailBVO.getAmount().div(allVolume_count)
										.multiply(volume_count));
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]改发货单的分摊金额为:" + deviVO.getSys_devi_amount().doubleValue() + "...");
							}
							if(volume_count != 0) {
								allDeviValueIsZero = false;
							}
						}
					}
					// 2015-3-18 对分摊费用设置四舍五入
					deviVO.setSys_devi_amount(deviVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviVO.setMan_devi_amount(deviVO.getSys_devi_amount());// 手工分摊金额：默认等于系统分摊金额
					deviVO.setPk_pay_detail(detailBVO.getPk_pay_detail());
					deviVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(deviVO);
					deviBVOs.add(deviVO);
				} else if(PayDetailConst.PAY_DEVI_DIMENSION.DETAIL.equals(payDeviDimension)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
							+ "]根据发货单货品明细行维度进行分摊...");
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
							+ "]根据发货单查询所有的货品明细行...");
					// FIXME 这里查询的应该是运段的货品明细？
					InvPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
							"pk_invoice=?", invVO.getPk_invoice());
					if(packBVOs == null || packBVOs.length == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]该发货单没有货品明细，不需要分摊...");
						// 没有货品信息，不需要分摊
						continue;
					}
					TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
							entVO.getPk_trans_type());
					if(typeVO == null) {
						throw new BusiException("当前的运输方式已经被删除，pk[?]！",entVO.getPk_trans_type());
					}
					if(typeVO.getRate() == null) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]没有定义相应的换算比率，默认为0...");
						typeVO.setRate(UFDouble.ZERO_DBL);// 没有定义相应的换算比率，默认为0
					}
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]查询到"
							+ packBVOs.length + "条货品明细...");
					for(InvPackBVO packBVO : packBVOs) {
						PayDeviBVO deviBVO = new PayDeviBVO();
						deviBVO.setPk_entrust(entVO.getPk_entrust());
						deviBVO.setPk_invoice(invVO.getPk_invoice());
						deviBVO.setInvoice_serialno(packBVO.getSerialno());
						deviBVO.setPk_carrier(entVO.getPk_carrier());
						deviBVO.setPk_car_type(entVO.getPk_car_type());
						deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
						if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]金额为0，分摊金额也为0...");
							// 总金额为0，不需要分摊
						} else {
							if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
									|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]根据计费重进行分摊...");
								// 按重量分摊
								// 当前货品的计费重
								UFDouble volume = packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume();
								UFDouble weight = packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight();
								double fee_weight_count = weight.doubleValue();
								double fee = volume.multiply(typeVO.getRate()).doubleValue();
								if(fee > weight.doubleValue()) {
									fee_weight_count = fee;
								}
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]当前货品的计费重:" + fee_weight_count + "...");
								if(allFee_weight_count.doubleValue() == 0) {
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的总计费重为0，分摊金额也设置为0...");
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailBVO.getAmount().div(allFee_weight_count)
											.multiply(fee_weight_count));
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
								}
								if(fee_weight_count != 0) {
									allDeviValueIsZero = false;
								}
							} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]根据体积进行分摊...");
								// 按体积进行分摊，得到当前货品的体积
								double volume_count = (packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO
										.getVolume()).doubleValue();
								if(allVolume_count.doubleValue() == 0) {
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的总体积为0，分摊金额也设置为0...");
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailBVO.getAmount().div(allVolume_count)
											.multiply(volume_count));
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
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
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]，以上分摊后，分摊金额都为0，则平均分摊...");
				// 平均分摊
				for(PayDeviBVO deviBVO : deviBVOs) {
					deviBVO.setSys_devi_amount(detailBVO.getAmount().div(deviBVOs.size()));
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());
				}
			}
			retList.addAll(deviBVOs);
			index++;
		}
		return retList;
	}

	public static List<PayDeviBVO> getPayDeviBVOs(EntrustVO entVO, InvoiceVO[] invVOs, SegmentVO[] segVOs,
			PayDetailBVO[] detailBVOs,String strExpenseType) {
		logger.info("当批量配载、修改异常事故时调用重新计算分摊费用...");
		List<PayDeviBVO> retList = new ArrayList<PayDeviBVO>();
		if(entVO == null || detailBVOs == null || detailBVOs.length == 0) {
			return retList;
		}
		logger.info("START----------------" + entVO.getVbillno() + "------------------");
		// 读取相关的发货单
		if(invVOs == null) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有传入发货单VOs，查询委托单关联的发货单...");
			invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
					"pk_invoice in (select pk_invoice from ts_ent_inv_b where isnull(dr,0)=0 and pk_entrust=?)",
					entVO.getPk_entrust());
			if(invVOs == null || invVOs.length == 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有查询到任何关联的发货单,直接返回...");
				return retList;
			}
		}
		if(segVOs == null) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有传入运段VOs，查询委托单关联的运段...");
			segVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"pk_segment in (select pk_segment from ts_ent_seg_b where isnull(dr,0)=0 and pk_entrust=?)",
					entVO.getPk_entrust());
			if(segVOs == null || segVOs.length == 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]没有查询到任何关联的运段,直接返回...");
				return retList;
			}
		}
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]对每个费用明细进行分摊，共需要分摊" + detailBVOs.length + "个费用明细...");
		int index = 1;
		for(PayDetailBVO detailBVO : detailBVOs) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]开始分摊第" + index + "个费用明细,费用类型："
					+ detailBVO.getPk_expense_type() + "...");
			// 删除该应付明细，并且该费用类型的分摊记录
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]查询当前费用类型的旧的分摊记录...");
			PayDeviBVO[] oldDeviBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class,
					"pk_pay_detail=? and pk_expense_type=?", detailBVO.getPk_pay_detail(),
					strExpenseType);
			if(oldDeviBVOs != null && oldDeviBVOs.length > 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]删除当前费用类型的旧的分摊记录，共删除" + oldDeviBVOs.length + "条...");
				NWDao.getInstance().delete(oldDeviBVOs);
			}
			if(detailBVO.getStatus() == VOStatus.DELETED) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]第" + index + "条费用明细是删除状态，则不用分摊...");
				continue;
			}
			// 第9步的分摊放到这边执行
			// 按发货单进行分摊
			String payDeviType = ParameterHelper.getPayDeviType();// 得到分摊类型
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊类型：" + payDeviType + "...");
			String payDeviDimension = ParameterHelper.getPayDeviDimension();// 得到分摊维度
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊维度：" + payDeviDimension + "...");
			boolean allDeviValueIsZero = true;// 如果分摊数量都为0，那么平均分摊
			List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
			for(int i = 0; i < invVOs.length; i++) {
				InvoiceVO invVO = invVOs[i];
				if(PayDetailConst.PAY_DEVI_DIMENSION.INVOICE.equals(payDeviDimension)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据发货单维度进行分摊...");
					PayDeviBVO deviVO = new PayDeviBVO();
					deviVO.setPk_entrust(entVO.getPk_entrust());
					deviVO.setPk_invoice(invVO.getPk_invoice());
					deviVO.setPk_carrier(entVO.getPk_carrier());
					deviVO.setPk_car_type(entVO.getPk_car_type());
					deviVO.setPk_expense_type(detailBVO.getPk_expense_type());
					if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]金额为0，分摊金额也为0...");
						// 总金额为0，分摊金额都为0
					} else {
						if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
								|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]根据计费重进行分摊...");
							// 按及计费重分摊
							// 计算当前发货单关联的运段的总计费重
							double fee_weight_count = 0;
							for(SegmentVO segVO : segVOs) {
								if(segVO.getInvoice_vbillno().equals(invVO.getVbillno())) { // 该运段属于该发货单
									fee_weight_count += segVO.getFee_weight_count().doubleValue();
								}
							}
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该发货单关联的运段的总计费重：" + fee_weight_count + "...");
							if(entVO.getFee_weight_count().doubleValue() == 0) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单关联的运段的总计费重为0，分摊金额也设置为0...");
								deviVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviVO.setSys_devi_amount(detailBVO.getAmount().div(entVO.getFee_weight_count())
										.multiply(fee_weight_count));
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]改发货单的分摊金额为:" + deviVO.getSys_devi_amount().doubleValue() + "...");
							}
							if(fee_weight_count != 0) {
								allDeviValueIsZero = false;
							}
						} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]根据体积进行分摊...");
							// 按体积进行分摊，得到当前发货单关联的运段的总体积
							double volume_count = 0;
							for(SegmentVO segVO : segVOs) {
								if(segVO.getInvoice_vbillno().equals(invVO.getVbillno())) { // 该运段属于该发货单
									volume_count += segVO.getVolume_count() == null ? 0 : segVO.getVolume_count()
											.doubleValue();
								}
							}
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该发货单关联的运段的总体积：" + volume_count + "...");
							if(entVO.getVolume_count().doubleValue() == 0) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单关联的运段的总体积为0，分摊金额也设置为0...");
								deviVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviVO.setSys_devi_amount(detailBVO.getAmount().div(entVO.getVolume_count())
										.multiply(volume_count));
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]改发货单的分摊金额为:" + deviVO.getSys_devi_amount().doubleValue() + "...");
							}
							if(volume_count != 0) {
								allDeviValueIsZero = false;
							}
						}
					}
					// 2015-3-18 对分摊费用设置四舍五入
					deviVO.setSys_devi_amount(deviVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviVO.setMan_devi_amount(deviVO.getSys_devi_amount());// 手工分摊金额：默认等于系统分摊金额
					deviVO.setPk_pay_detail(detailBVO.getPk_pay_detail());
					deviVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(deviVO);
					deviBVOs.add(deviVO);
				} else if(PayDetailConst.PAY_DEVI_DIMENSION.DETAIL.equals(payDeviDimension)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
							+ "]根据发货单货品明细行维度进行分摊...");
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
							+ "]根据发货单查询所有的货品明细行...");
					// FIXME 这里查询的应该是运段的货品明细？
					InvPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
							"pk_invoice=?", invVO.getPk_invoice());
					if(packBVOs == null || packBVOs.length == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]该发货单没有货品明细，不需要分摊...");
						// 没有货品信息，不需要分摊
						continue;
					}
					TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
							entVO.getPk_trans_type());
					if(typeVO == null) {
						throw new BusiException("当前的运输方式已经被删除，pk[?]！",entVO.getPk_trans_type());
					}
					if(typeVO.getRate() == null) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]没有定义相应的换算比率，默认为0...");
						typeVO.setRate(UFDouble.ZERO_DBL);// 没有定义相应的换算比率，默认为0
					}
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]查询到"
							+ packBVOs.length + "条货品明细...");
					for(InvPackBVO packBVO : packBVOs) {
						PayDeviBVO deviBVO = new PayDeviBVO();
						deviBVO.setPk_entrust(entVO.getPk_entrust());
						deviBVO.setPk_invoice(invVO.getPk_invoice());
						deviBVO.setInvoice_serialno(packBVO.getSerialno());
						deviBVO.setPk_carrier(entVO.getPk_carrier());
						deviBVO.setPk_car_type(entVO.getPk_car_type());
						deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
						if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]金额为0，分摊金额也为0...");
							// 总金额为0，不需要分摊
						} else {
							if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
									|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]根据计费重进行分摊...");
								// 按重量分摊
								// 当前货品的计费重
								UFDouble volume = packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume();
								UFDouble weight = packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight();
								double fee_weight_count = weight.doubleValue();
								double fee = volume.multiply(typeVO.getRate()).doubleValue();
								if(fee > weight.doubleValue()) {
									fee_weight_count = fee;
								}
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]当前货品的计费重:" + fee_weight_count + "...");
								if(entVO.getFee_weight_count().doubleValue() == 0) {
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的总计费重为0，分摊金额也设置为0...");
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailBVO.getAmount().div(entVO.getFee_weight_count())
											.multiply(fee_weight_count));
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
								}
								if(fee_weight_count != 0) {
									allDeviValueIsZero = false;
								}
							} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]根据体积进行分摊...");
								// 按体积进行分摊，得到当前货品的体积
								double volume_count = (packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO
										.getVolume()).doubleValue();
								if(entVO.getVolume_count().doubleValue() == 0) {
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的总体积为0，分摊金额也设置为0...");
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailBVO.getAmount().div(entVO.getVolume_count())
											.multiply(volume_count));
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
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
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]，以上分摊后，分摊金额都为0，则平均分摊...");
				// 平均分摊
				for(PayDeviBVO deviBVO : deviBVOs) {
					deviBVO.setSys_devi_amount(detailBVO.getAmount().div(deviBVOs.size()));
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());
				}
			}
			retList.addAll(deviBVOs);
			index++;
		}
		return retList;
	}

	/**
	 * 计算分摊费用，对运段配载时调用
	 * 
	 * @param entVO
	 *            委托单
	 * @param segVO
	 *            运段
	 * @param invVO
	 *            发货单
	 * @param detailBVO
	 *            费用明细
	 * @return
	 */
	public static List<PayDeviBVO> getPayDeviBVO(EntrustVO entVO, InvoiceVO invVO, SegmentVO segVO,
			PayDetailBVO detailBVO) {
		logger.info("当运段配载时调用计算分摊费用...");
		// 删除该应付明细，并且该费用类型的分摊记录
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]查询当前费用类型的旧的分摊记录...");
		PayDeviBVO[] oldDeviBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class,
				"pk_pay_detail=? and pk_expense_type=?", detailBVO.getPk_pay_detail(), detailBVO.getPk_expense_type());
		if(oldDeviBVOs != null && oldDeviBVOs.length > 0) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]查询到" + oldDeviBVOs.length + "条旧的分摊记录,删除...");
			NWDao.getInstance().delete(oldDeviBVOs);
		}
		List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
		if(detailBVO.getStatus() == VOStatus.DELETED) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]费用明细是删除状态，不用分摊...");
			return deviBVOs;
		}
		String payDeviType = ParameterHelper.getPayDeviType();// 得到分摊类型
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊类型：" + payDeviType + "...");
		String payDeviDimension = ParameterHelper.getPayDeviDimension();// 得到分摊维度
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊维度：" + payDeviDimension + "...");
		boolean allDeviValueIsZero = true;// 如果分摊数量都为0，那么平均分摊
		if(PayDetailConst.PAY_DEVI_DIMENSION.INVOICE.equals(payDeviDimension)) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]根据发货单维度进行分摊...");
			PayDeviBVO deviBVO = new PayDeviBVO();
			deviBVO.setPk_entrust(entVO.getPk_entrust());
			deviBVO.setPk_invoice(invVO.getPk_invoice());
			deviBVO.setPk_carrier(entVO.getPk_carrier());
			deviBVO.setPk_car_type(entVO.getPk_car_type());
			deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
			if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]金额为0，分摊金额也为0...");
				// 总金额为0，不需要分摊
			} else {
				if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
						|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据计费重进行分摊...");
					// 按重量分摊
					// 计算当前运段的总计费重
					double fee_weight_count = segVO.getFee_weight_count() == null ? 0 : segVO.getFee_weight_count()
							.doubleValue();
					if(entVO.getFee_weight_count().doubleValue() == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]该发货单关联的运段的总体积为0，分摊金额也设置为0...");
						deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
					} else {
						deviBVO.setSys_devi_amount(detailBVO.getAmount().div(entVO.getFee_weight_count())
								.multiply(fee_weight_count));
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]该发货单的分摊金额为:"
								+ deviBVO.getSys_devi_amount().doubleValue() + "...");
					}
					if(fee_weight_count != 0) {
						allDeviValueIsZero = false;
					}
				} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据体积进行分摊...");
					// 按体积进行分摊，得到当前发货单的总体积
					double volume_count = segVO.getVolume_count() == null ? 0 : segVO.getVolume_count().doubleValue();
					if(invVO.getVolume_count().doubleValue() == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]该发货单关联的运段的总体积为0，分摊金额也设置为0...");
						deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
					} else {
						deviBVO.setSys_devi_amount(detailBVO.getAmount().div(invVO.getVolume_count())
								.multiply(volume_count));
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]该发货单的分摊金额为:"
								+ deviBVO.getSys_devi_amount().doubleValue() + "...");
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
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据发货单货品明细行维度进行分摊...");
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据发货单查询所有的货品明细行...");
			// FIXME 这里查询的应该是运段的货品明细？
			InvPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
					"pk_invoice=?", invVO.getPk_invoice());
			if(packBVOs == null || packBVOs.length == 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]没有货品明细，不需要分摊...");
				// 没有货品信息，不需要分摊
				return deviBVOs;
			}
			TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
					entVO.getPk_trans_type());
			if(typeVO == null) {
				throw new BusiException("当前的运输方式已经被删除，pk[?]！",entVO.getPk_trans_type());
			}
			if(typeVO.getRate() == null) {
				typeVO.setRate(UFDouble.ZERO_DBL);// 没有定义相应的换算比率，默认为0
			}
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]需要分摊到" + packBVOs.length
					+ "个货品明细中...");
			for(InvPackBVO packBVO : packBVOs) {
				PayDeviBVO deviBVO = new PayDeviBVO();
				deviBVO.setPk_entrust(entVO.getPk_entrust());
				deviBVO.setPk_invoice(invVO.getPk_invoice());
				deviBVO.setInvoice_serialno(packBVO.getSerialno());// 当前行号
				deviBVO.setPk_carrier(entVO.getPk_carrier());
				deviBVO.setPk_car_type(entVO.getPk_car_type());
				deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
				if(detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]金额为0，分摊金额也为0...");
					// 总金额为0，不需要分摊
				} else {
					if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
							|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据计费重进行分摊...");
						// 按重量分摊
						// 当前货品的计费重
						UFDouble volume = packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume();
						UFDouble weight = packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight();
						double fee_weight_count = weight.doubleValue();
						double fee = volume.multiply(typeVO.getRate()).doubleValue();
						if(fee > weight.doubleValue()) {
							fee_weight_count = fee;
						}
						if(invVO.getFee_weight_count().doubleValue() == 0) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该货品明细的总计费重为0，分摊金额也设置为0...");
							deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
						} else {
							deviBVO.setSys_devi_amount(detailBVO.getAmount().div(invVO.getFee_weight_count())
									.multiply(fee_weight_count));
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
						}
						if(fee_weight_count != 0) {
							allDeviValueIsZero = false;
						}
					} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]根据体积进行分摊...");
						// 按体积进行分摊，得到当前发货单的总体积
						double volume_count = (packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume())
								.doubleValue();
						if(invVO.getVolume_count().doubleValue() == 0) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该货品明细的总体积为0，分摊金额也设置为0...");
							deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
						} else {
							deviBVO.setSys_devi_amount(detailBVO.getAmount().div(invVO.getVolume_count())
									.multiply(volume_count));
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
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
		if(allDeviValueIsZero) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]，以上分摊后，分摊金额都为0，则平均分摊...");
			// 平均分摊
			for(PayDeviBVO deviVO : deviBVOs) {
				deviVO.setSys_devi_amount(detailBVO.getAmount().div(deviBVOs.size()));
				// 2015-3-18 对分摊费用设置四舍五入
				deviVO.setSys_devi_amount(deviVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviVO
						.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
				deviVO.setMan_devi_amount(deviVO.getSys_devi_amount());
			}
		}
		return deviBVOs;
	}

	/**
	 * 应付明细修改费用时，分摊费用也跟着修改<br/>
	 * fee_weight_countMap和volume_countMap的个数是相同的，并且有相同的key，
	 * 当使用计费重进行分摊时使用fee_weight_countMap，当使用体积分摊时使用volume_countMap
	 * 
	 * @param entVO
	 * @param fee_weight_countMap
	 *            发货单号和计费重的map
	 * @param volume_countMap
	 *            发货单号和体积的map
	 * @param detailBVOs
	 *            待分摊的费用
	 * @return
	 */
	public static List<PayDeviBVO> getPayDeviBVOs2(EntrustVO entVO, Map<String, UFDouble> fee_weight_countMap,
			Map<String, UFDouble> volume_countMap, PayDetailBVO[] detailBVOs) {
		
		// yaojiie 2015 12 22
		UFDouble allFee_weight_count = UFDouble.ZERO_DBL;
		UFDouble allVolume_count = UFDouble.ZERO_DBL;
		for (String key : fee_weight_countMap.keySet()) {
			allFee_weight_count = allFee_weight_count
					.add(fee_weight_countMap.get(key) == null ? UFDouble.ZERO_DBL : fee_weight_countMap.get(key));
		}
		for (String key : volume_countMap.keySet()) {
			allVolume_count = allVolume_count
					.add(volume_countMap.get(key) == null ? UFDouble.ZERO_DBL : volume_countMap.get(key));
		}
		
		logger.info("应付明细修改费用时，重新计算分摊费用...");
		String payDeviType = ParameterHelper.getPayDeviType();// 得到分摊类型
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊类型：" + payDeviType + "...");
		String payDeviDimension = ParameterHelper.getPayDeviDimension();// 得到分摊维度
		logger.info("[XXX委托单:" + entVO.getVbillno() + "]系统分摊维度：" + payDeviDimension + "...");
		boolean allDeviValueIsZero = true;// 如果分摊数量都为0，那么平均分摊
		List<PayDeviBVO> allDeviBVOs = new ArrayList<PayDeviBVO>();
		int index = 1;
		for(PayDetailBVO detailVO : detailBVOs) {
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]开始分摊第" + index + "个费用明细,费用类型："
					+ detailVO.getPk_expense_type() + "...");
			// 删除该应付明细，并且该费用类型的分摊记录
			logger.info("[XXX委托单:" + entVO.getVbillno() + "]查询当前费用类型的旧的分摊记录...");
			PayDeviBVO[] oldDeviBVOs = NWDao.getInstance()
					.queryForSuperVOArrayByCondition(PayDeviBVO.class, "pk_pay_detail=? and pk_expense_type=?",
							detailVO.getPk_pay_detail(), detailVO.getPk_expense_type());
			if(oldDeviBVOs != null && oldDeviBVOs.length > 0) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]查询到" + oldDeviBVOs.length + "条旧的分摊记录,删除...");
				NWDao.getInstance().delete(oldDeviBVOs);
			}
			if(detailVO.getStatus() == VOStatus.DELETED) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]费用明细是删除状态，不用分摊...");
				continue;
			}
			List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();// 每个费用的分摊费用明细
			for(String key : fee_weight_countMap.keySet()) {// 这里循环fee_weight_countMap和循环volume_countMap是一样的
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]根据发货单号查询整个发货单...");
				InvoiceVO invVO = NWDao.getInstance().queryByCondition(InvoiceVO.class,
						new String[] { "pk_invoice", "vbillno" }, "vbillno=?", key);
				if(PayDetailConst.PAY_DEVI_DIMENSION.INVOICE.equals(payDeviDimension)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "],[发货单：" + invVO.getVbillno() + "]根据发货单进行分摊...");
					PayDeviBVO deviBVO = new PayDeviBVO();
					deviBVO.setPk_entrust(entVO.getPk_entrust());
					deviBVO.setPk_invoice(invVO.getPk_invoice());
					deviBVO.setPk_carrier(entVO.getPk_carrier());
					deviBVO.setPk_car_type(entVO.getPk_car_type());
					deviBVO.setPk_expense_type(detailVO.getPk_expense_type());
					if(detailVO.getAmount() == null || detailVO.getAmount().doubleValue() == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]金额为0，分摊金额也为0...");
						// 总金额为0，不需要分摊
					} else {
						if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
								|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]根据计费重进行分摊...");
							// 按重量分摊
							// 计算当前这个发货单的总计费重
							double fee_weight_count = fee_weight_countMap.get(key).doubleValue();
							if(allFee_weight_count.doubleValue() == 0) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单所对应的运段的总计费重为0，分摊金额也设置为0...");
								deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviBVO.setSys_devi_amount(detailVO.getAmount().div(allFee_weight_count)
										.multiply(fee_weight_count));
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
							}
							if(fee_weight_count != 0) {
								allDeviValueIsZero = false;
							}
						} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
							// 按体积进行分摊，得到当前发货单的总体积
							double volume_count = volume_countMap.get(key).doubleValue();
							if(allVolume_count.doubleValue() == 0) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单所对应的运段的总体积为0，分摊金额也设置为0...");
								deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviBVO.setSys_devi_amount(detailVO.getAmount().div(allVolume_count)
										.multiply(volume_count));
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]该发货单的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
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
					deviBVO.setPk_pay_detail(detailVO.getPk_pay_detail());
					deviBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(deviBVO);
					deviBVOs.add(deviBVO);
				} else if(PayDetailConst.PAY_DEVI_DIMENSION.DETAIL.equals(payDeviDimension)) {
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
							+ "]根据发货单货品明细行维度进行分摊...");
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
							+ "]根据发货单查询所有的货品明细行...");
					// FIXME 这里是否应该使用运段的货品明细？
					InvPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
							"pk_invoice=?", invVO.getPk_invoice());
					if(packBVOs == null || packBVOs.length == 0) {
						logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
								+ "]没有货品明细，不需要分摊...");
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
					logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno() + "]需要分摊到"
							+ packBVOs.length + "个货品明细中...");
					for(InvPackBVO packBVO : packBVOs) {
						PayDeviBVO deviBVO = new PayDeviBVO();
						deviBVO.setPk_entrust(entVO.getPk_entrust());
						deviBVO.setPk_invoice(invVO.getPk_invoice());
						deviBVO.setInvoice_serialno(packBVO.getSerialno());
						deviBVO.setPk_carrier(entVO.getPk_carrier());
						deviBVO.setPk_car_type(entVO.getPk_car_type());
						deviBVO.setPk_expense_type(detailVO.getPk_expense_type());
						if(detailVO.getAmount() == null || detailVO.getAmount().doubleValue() == 0) {
							logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
									+ "]金额为0，分摊金额也为0...");
							// 总金额为0，不需要分摊
						} else {
							if(PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
									|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]根据计费重进行分摊...");
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
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的总计费重为0，分摊金额也设置为0...");
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailVO.getAmount().div(allFee_weight_count)
											.multiply(fee_weight_count));
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
								}
								if(fee_weight_count != 0) {
									allDeviValueIsZero = false;
								}
							} else if(PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
								logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
										+ "]根据体积进行分摊...");
								// 按体积进行分摊，这里按照行分摊，必须读取行的体积
								double volume_count = packBVO.getVolume() == null ? 0 : packBVO.getVolume()
										.doubleValue();
								if(allVolume_count.doubleValue() == 0) {
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的总体积为0，分摊金额也设置为0...");
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(detailVO.getAmount().div(allVolume_count)
											.multiply(volume_count));
									logger.info("[XXX委托单:" + entVO.getVbillno() + "]，[发货单：" + invVO.getVbillno()
											+ "]该货品明细的分摊金额为:" + deviBVO.getSys_devi_amount().doubleValue() + "...");
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
						deviBVO.setPk_pay_detail(detailVO.getPk_pay_detail());
						deviBVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(deviBVO);
						deviBVOs.add(deviBVO);
					}
				} else {
					throw new BusiException("系统参数中，设置的[pay_devi_dimension]参数不支持，当前值是[?]！",payDeviDimension);
				}
			}
			if(allDeviValueIsZero) {
				logger.info("[XXX委托单:" + entVO.getVbillno() + "]，以上分摊后，分摊金额都为0，则平均分摊...");
				// 平均分摊
				for(PayDeviBVO deviBVO : deviBVOs) {
					//当手工删除费用时，getAmount 会为NULL，这次做下NULL 的判断。2015-11-1 Jonathan
					//deviBVO.setSys_devi_amount(detailVO.getAmount().div(deviBVOs.size()));
					deviBVO.setSys_devi_amount((detailVO.getAmount()== null ? UFDouble.ZERO_DBL : detailVO.getAmount()).div(deviBVOs.size()));
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());
				}
			}
			allDeviBVOs.addAll(deviBVOs);
			index++;
		}
		return allDeviBVOs;
	}

	/**
	 * 合并地址信息,合并同一分组中相同的地址
	 * 
	 * @param lineVOs
	 *            可能是起始地或者目的地集合，但只会是一种，比如就是起始地
	 * @return
	 */
	public static List<EntLineBVO> groupAndMergeLine(List<EntLineBVO> lineVOs, boolean startLine) {
		// 1、先把起始点根据ID号分组出来，并给每个分组结果的调度单字段处写上对应的调度单号和调度单-起始点\结束点，对于合并的节点，写上合并的调度单号起始点。合并时要求到达时间获取最早的。
		List<String> addressPKs = new LinkedList<String>();
		Map<String, EntLineBVO> lineMap = new HashMap<String, EntLineBVO>();
		for(int i = 0; i < lineVOs.size(); i++) {
			EntLineBVO lineVO = lineVOs.get(i);
			String pk_address = lineVO.getPk_address();
			if(addressPKs.contains(pk_address)) {
				EntLineBVO currentVO = lineMap.get(pk_address);
				String current_req_arri_date = currentVO.getReq_arri_date();// 要求到达时间
				String req_arri_date = lineVO.getReq_arri_date();
				if(startLine) {
					// 取要求到达时间比较早的
					if(StringUtils.isBlank(current_req_arri_date)) {
						currentVO.setReq_arri_date(req_arri_date);
					} else if(StringUtils.isBlank(req_arri_date)) {

					} else {
						long s = DateUtils.getIntervalMillSeconds(current_req_arri_date, req_arri_date);
						if(s < 0) {
							currentVO.setReq_arri_date(req_arri_date);
						}
					}
				} else {
					// 取要求到达时间比较晚的
					if(StringUtils.isBlank(current_req_arri_date)) {
						currentVO.setReq_arri_date(req_arri_date);
					} else if(StringUtils.isBlank(req_arri_date)) {

					} else {
						long s = DateUtils.getIntervalMillSeconds(current_req_arri_date, req_arri_date);
						if(s > 0) {
							currentVO.setReq_arri_date(req_arri_date);
						}
					}
				}
				// 合并运段号
				currentVO.setPk_segment(currentVO.getPk_segment() + Constants.SPLIT_CHAR + lineVO.getPk_segment());
				// 合并起始地和目的地标识
				currentVO.setAddr_flag(currentVO.getAddr_flag() + Constants.SPLIT_CHAR + lineVO.getAddr_flag());
				// 合并后，删除这个地址
				lineVOs.remove(lineVO);
				i--;
			} else {
				addressPKs.add(pk_address);
				lineMap.put(pk_address, lineVO);
			}
		}
		return lineVOs;
	}

	/**
	 * 将起始地和目的地放在一起进行合并操作
	 * 
	 * @param lineVOs
	 *            包括起始地和目的地
	 * @return
	 */
	public static List<EntLineBVO> groupAndMergeLine2(List<EntLineBVO> lineVOs) {
		// 把起始点分组排在目的点分组之前，并根据运段和地址进行二次分组。针对地址相同，但是运段中没有重复的可以再次合并
		if(lineVOs.size() > 2) {
			for(int i = 0; i < lineVOs.size() - 1; i++) {
				EntLineBVO currentOne = lineVOs.get(i);
				EntLineBVO nextOne = lineVOs.get(i + 1);
				if(StringUtils.isBlank(currentOne.getPk_segment())) {
					i++;
					continue;
				}
				if(StringUtils.isBlank(nextOne.getPk_segment())) {
					i++;
					continue;
				}
				if(currentOne.getPk_address().equals(nextOne.getPk_address())) {
					// 地址相同，判断是否是同一个运段，如果是同一个运段，那么不能合并，否则进行合并
					if(!currentOne.getPk_segment().equals(nextOne.getPk_segment())
							|| (currentOne.getPk_segment().equals(nextOne.getPk_segment()) && lineVOs.size() > 2)) {
						// 合并
						String current_req_arri_date = currentOne.getReq_arri_date();// 要求到达时间
						String req_arri_date = nextOne.getReq_arri_date();
						// 取要求到达时间比较早的
						if(StringUtils.isBlank(current_req_arri_date)) {
							nextOne.setReq_arri_date(req_arri_date);
						} else if(StringUtils.isBlank(req_arri_date)) {

						} else {
							long s = DateUtils.getIntervalMillSeconds(current_req_arri_date, req_arri_date);
							if(s < 0) {
								nextOne.setReq_arri_date(req_arri_date);
							}
						}
						// 合并运段号
						nextOne.setPk_segment(currentOne.getPk_segment() + Constants.SPLIT_CHAR
								+ nextOne.getPk_segment());
						// 合并起始地和目的地标识
						nextOne.setAddr_flag(currentOne.getAddr_flag() + Constants.SPLIT_CHAR + nextOne.getAddr_flag());
						// 合并后，删除这个地址
						currentOne.setStatus(VOStatus.DELETED);
					}
				}
			}
		}
		return lineVOs;
	}

	/**
	 * 当调整了顺序以后，校验调整的顺序是否正确 <br/>
	 * 1、第一个节点必须是某个运段的始发地,请调整路线信息的节点顺序!<br/>
	 * 2、最后一个节点必须是某个运段的目的地,请调整路线信息的节点顺序!<br/>
	 * 3、从节点序号1开始判断，对应的调度单字段处的起始点还是目的点，如果是调度单号的起始点，
	 * 那么找这个序号里面是否有调度单号的目的地点，如果有则验证通过
	 * 4、如果没有，则验证后续序号中是否有这个调度单号的目的点。如果有则验证通过。如果没有则提示详细错误信息
	 * 
	 * @param lineVOs
	 */
	public static void checkLineVOs(List<EntLineBVO> lineVOs) {
		if(lineVOs == null || lineVOs.size() == 0) {
			return;
		}
		if(lineVOs.size() >= 2) {
			EntLineBVO firstOne = lineVOs.get(0);
			if(firstOne.getAddr_flag() == null || firstOne.getAddr_flag().indexOf(AddressConst.START_ADDR_FLAG) == -1) {
				throw new BusiException("第一个节点[?]必须是运段的始发地！",firstOne.getAddr_name());
			}
			EntLineBVO lastOne = lineVOs.get(lineVOs.size() - 1);
			if(lastOne.getAddr_flag() == null || lastOne.getAddr_flag().indexOf(AddressConst.END_ADDR_FLAG) == -1) {
				throw new BusiException("最后一个节点[?]必须是运段的目的地！",lastOne.getAddr_name());
			}
			for(int i = 0; i < lineVOs.size() - 1; i++) {
				EntLineBVO currentOne = lineVOs.get(i);
				if(currentOne.getSegment_node() == null || !currentOne.getSegment_node().booleanValue()) {
					// 新增加的节点，不需要判断
					continue;
				}
				// 对于系统已经存在的运段节点，这里的运段和运段的地址标识是一一对应的，但是对于新增加的节点，可能地址标识会为空
				if(StringUtils.isBlank(currentOne.getPk_segment())) {
					// 新增加的节点
					continue;
				}
				String[] current_pk_segmentAry = currentOne.getPk_segment().split(Constants.SPLIT_CHAR);
				String[] current_addr_flagAry = currentOne.getAddr_flag().split(Constants.SPLIT_CHAR);
				for(int j = 0; j < current_pk_segmentAry.length; j++) {
					String pk_segment = current_pk_segmentAry[j];
					String addr_flag = current_addr_flagAry[j];
					boolean find = false;
					if(addr_flag.equals(AddressConst.START_ADDR_FLAG)) {// 起始点
						for(int k = j + 1; k < current_pk_segmentAry.length; k++) {
							if(pk_segment.equals(current_pk_segmentAry[k])
									&& current_addr_flagAry[k].equals(AddressConst.END_ADDR_FLAG)) {
								// 在同一行里面找到了这个运段的目的节点，符合规则
								find = true;
								break;
							}
						}
						if(!find) {
							// 在同一行里面没找到，那么查询下一行的数据
							for(int n = i + 1; n < lineVOs.size(); n++) {
								EntLineBVO nextOne = lineVOs.get(n);
								if(StringUtils.isBlank(nextOne.getPk_segment())) {
									// 新增加的节点
									continue;
								}
								String[] next_pk_segmentAry = nextOne.getPk_segment().split(Constants.SPLIT_CHAR);
								String[] next_addr_flagAry = nextOne.getAddr_flag().split(Constants.SPLIT_CHAR);
								for(int k = 0; k < next_pk_segmentAry.length; k++) {
									if(pk_segment.equals(next_pk_segmentAry[k])
											&& next_addr_flagAry[k].equals(AddressConst.END_ADDR_FLAG)) {
										find = true;
										break;
									}
								}
								if(find) {
									break;
								}
							}
						}
						if(!find) {
							// 都没有找到，那么提示错误信息
							String addr_sql = "select addr_name from ts_address where pk_address=?";
							String addr_name = NWDao.getInstance().queryForObject(addr_sql, String.class,
									currentOne.getPk_address());
							String seg_sql = "select vbillno from ts_segment where pk_segment=?";
							String segment_vbillno = NWDao.getInstance().queryForObject(seg_sql, String.class,
									pk_segment);
							throw new BusiException("运段[?]的起始地[?]必须在目的地的前面！",segment_vbillno,addr_name);
						}
					}
				}
			}
		}
	}

	/**
	 * 返回节点集合的第一个节点
	 * 
	 * @param lineVOs
	 * @return
	 */
	public static EntLineBVO getFirstLineVO(EntLineBVO[] lineVOs) {
		if(lineVOs == null) {
			return null;
		}
		EntLineBVO firstLineVO = null;
		for(int i = 0; i < lineVOs.length; i++) {
			if(firstLineVO == null) {
				if(lineVOs[i].getStatus() != VOStatus.DELETED) {
					firstLineVO = lineVOs[i];
					break;
				}
			}
		}
		return firstLineVO;
	}

	/**
	 * 返回节点集合的最后一个节点
	 * 
	 * @param lineVOs
	 * @return
	 */
	public static EntLineBVO getLastLineVO(EntLineBVO[] lineVOs) {
		if(lineVOs == null) {
			return null;
		}
		EntLineBVO lastLineVO = null;
		for(int i = lineVOs.length - 1; i >= 0; i--) {
			if(lastLineVO == null) {
				if(lineVOs[i].getStatus() != VOStatus.DELETED) {
					lastLineVO = lineVOs[i];
					break;
				}
			}
		}
		return lastLineVO;
	}

	/**
	 * 校验节点信息，分组等，返回委托单最终的节点集合
	 * 
	 * @param lineVOs
	 * @param selfCheck
	 *            是否检测节点顺序
	 * @return
	 */
	public static List<EntLineBVO> processLineInfo(EntLineBVO[] lineVOs, boolean selfCheck) {
		if(lineVOs == null || lineVOs.length == 0) {
			return new ArrayList<EntLineBVO>();
		}
		List<EntLineBVO> lineList = new ArrayList<EntLineBVO>();
		for(EntLineBVO lineVO : lineVOs) {
			lineList.add(lineVO);
		}
		if(selfCheck) {
			PZUtils.checkLineVOs(lineList);
		}
		// 地址校验是成功的，对地址进行分组合并
		lineList = PZUtils.groupAndMergeLine2(lineList);// XXX 删除的对象会标记为delete
		// 给每个节点加入一个序号
		logger.info("-------------------起始地和目的地合并后的地址名称--------------------");
		for(int i = 0; i < lineList.size(); i++) {
			EntLineBVO lineVO = lineList.get(i);
			if(lineVO.getStatus() == VOStatus.DELETED) {
				if(StringUtils.isBlank(lineVO.getPk_ent_line_b())) {
					lineList.remove(lineVO);
					i--;
					continue;
				}
			} else if(lineVO.getStatus() != VOStatus.NEW) {
				lineVO.setStatus(VOStatus.UPDATED);
			}
			int serialno = i * 10 + 10;
			lineVO.setSerialno(serialno);// 设置一个行号
			logger.info("行号：" + serialno + ",地址名称：" + lineVO.getAddr_name());
		}
		// 对于同一个节点包括多个运段的情况，对这个节点的起始点和目的点顺序进行调整。将目的点放到起始点前面
		for(int i = 0; i < lineList.size(); i++) {
			EntLineBVO lineVO = lineList.get(i);
			if(lineVO.getAddr_flag() != null && lineVO.getAddr_flag().length() > 1) {// 只调整包括多个运段的节点
				String[] addr_flagAry = lineVO.getAddr_flag().split(Constants.SPLIT_CHAR);
				String[] pk_segmentAry = lineVO.getPk_segment().split(Constants.SPLIT_CHAR);
				AjustVO[] vos = new AjustVO[addr_flagAry.length];
				for(int j = 0; j < addr_flagAry.length; j++) {
					AjustVO vo = new AjustVO();
					vo.setAddr_flag(addr_flagAry[j]);
					vo.setPk_segment(pk_segmentAry[j]);
					vos[j] = vo;
				}
				Arrays.sort(vos, new AjustVOComparator());
				StringBuffer addrFlagBuf = new StringBuffer();
				StringBuffer pkSegmentBuf = new StringBuffer();
				for(int j = 0; j < vos.length; j++) {
					addrFlagBuf.append(vos[j].getAddr_flag());
					pkSegmentBuf.append(vos[j].getPk_segment());
					if(j < vos.length - 1) {
						addrFlagBuf.append(Constants.SPLIT_CHAR);
						pkSegmentBuf.append(Constants.SPLIT_CHAR);
					}
				}
				lineVO.setAddr_flag(addrFlagBuf.toString());
				lineVO.setPk_segment(pkSegmentBuf.toString());
			}
		}
		return lineList;
	}

	/**
	 * 更改了节点顺序后，同步委托单的提货点和收货点
	 * 
	 * @param entVO
	 * @param firstLineVO
	 * @param lastLineVO
	 */
	public static void syncEntrustDeliAndArri(EntrustVO entVO, EntLineBVO firstLineVO, EntLineBVO lastLineVO) {
		// 提货方
		entVO.setReq_deli_date(firstLineVO.getReq_arri_date());
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
		entVO.setReq_arri_date(lastLineVO.getReq_arri_date());
		entVO.setPk_arrival(lastLineVO.getPk_address());
		entVO.setArri_city(lastLineVO.getPk_city());
		entVO.setArri_province(lastLineVO.getPk_province());
		entVO.setArri_area(lastLineVO.getPk_area());
		entVO.setArri_detail_addr(lastLineVO.getDetail_addr());
		entVO.setArri_contact(lastLineVO.getContact());
		entVO.setArri_phone(lastLineVO.getPhone());
		entVO.setArri_mobile(lastLineVO.getMobile());
		entVO.setArri_email(lastLineVO.getEmail());
	}
	
	//yaojiie 2015 12 29 配载和批量配载时，将运力信息数据放入运力信息历史表中
	public static EntTransHisBVO getEntTransHisBVO(EntTransbilityBVO entTransbilityBVO){
		EntTransHisBVO entTransHisBVO = new EntTransHisBVO();
		
		entTransHisBVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(entTransHisBVO);
		
		entTransHisBVO.setPk_entrust(entTransbilityBVO.getPk_entrust());
		entTransHisBVO.setCarno(entTransbilityBVO.getCarno());
		entTransHisBVO.setPk_driver(entTransbilityBVO.getPk_driver());
		entTransHisBVO.setContainer_no(entTransbilityBVO.getContainer_no());
		entTransHisBVO.setSealing_no(entTransbilityBVO.getSealing_no());
		entTransHisBVO.setForecast_deli_date(entTransbilityBVO.getForecast_deli_date());
		entTransHisBVO.setPk_car_type(entTransbilityBVO.getPk_car_type());
		entTransHisBVO.setNum(entTransbilityBVO.getNum());
		entTransHisBVO.setMemo(entTransbilityBVO.getMemo());
		entTransHisBVO.setGps_id(entTransbilityBVO.getGps_id());
		
		entTransHisBVO.setDef1(entTransbilityBVO.getDef1());
		entTransHisBVO.setDef2(entTransbilityBVO.getDef2());
		entTransHisBVO.setDef3(entTransbilityBVO.getDef3());
		entTransHisBVO.setDef4(entTransbilityBVO.getDef4());
		entTransHisBVO.setDef5(entTransbilityBVO.getDef5());
		entTransHisBVO.setDef6(entTransbilityBVO.getDef6());
		entTransHisBVO.setDef7(entTransbilityBVO.getDef7());
		entTransHisBVO.setDef8(entTransbilityBVO.getDef8());
		entTransHisBVO.setDef9(entTransbilityBVO.getDef9());
		entTransHisBVO.setDef10(entTransbilityBVO.getDef10());
		entTransHisBVO.setDef11(entTransbilityBVO.getDef11());
		entTransHisBVO.setDef12(entTransbilityBVO.getDef12());
		
		return entTransHisBVO;
	}
}
