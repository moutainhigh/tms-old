/**
 * 
 */
package com.tms.service.inv.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.ReceiveDetailService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.inv.InvoiceService;
import com.tms.service.inv.OrderlotService;
import com.tms.service.job.cm.MatchVO;
import com.tms.service.job.cm.ReceDetailBuilder;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggOrderlotVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.OrderAssVO;
import com.tms.vo.inv.OrderlotDeviVO;
import com.tms.vo.inv.OrderlotInvVO;
import com.tms.vo.inv.OrderlotRdVO;
import com.tms.vo.inv.OrderlotVO;

/**
 * 订单批次管理
 * 
 * @author xuqc
 * @Date 2015年6月9日 下午9:04:42
 *
 */
@Service
public class OrderlotServiceImpl extends TMSAbsBillServiceImpl implements OrderlotService {
	AggregatedValueObject billInfo;
	
	@Autowired
	InvoiceService invoiceService;
	
	@Autowired
	ReceiveDetailService receiveDetailService;
	
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggOrderlotVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, OrderlotVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, OrderlotVO.LOT);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggOrderlotVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, OrderlotRdVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, OrderlotRdVO.LOT);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_orderlot_rd");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_orderlot_rd");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggOrderlotVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, OrderlotDeviVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, OrderlotDeviVO.LOT);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_orderlot_devi");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_orderlot_devi");

			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggOrderlotVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, OrderlotInvVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, OrderlotInvVO.LOT);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_orderlot_inv");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_orderlot_inv");

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1, childVO2 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.service.impl.AbsBillServiceImpl#getCodeFieldCode()
	 */
	@Override
	public String getCodeFieldCode() {
		return OrderlotVO.LOT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.service.IBillService#getBillType()
	 */
	public String getBillType() {
		return BillTypeConst.ORDERLOT;
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, String orderBy) {
		super.processAfterExecFormula(list, paramVO, orderBy);
		if (list != null) {
			if (paramVO.isBody()) {
				Map<String, List<HashMap>> cacheMap = new HashMap<String, List<HashMap>>();
				for (Map<String, Object> map : list) {
					setGoodsInfo(map, cacheMap);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void setGoodsInfo(Map<String, Object> deviMap, Map<String, List<HashMap>> cacheMap) {
		String sql = "select goods_code,goods_name from ts_inv_pack_b where isnull(dr,0)=0 and pk_invoice=(select pk_invoice from ts_invoice where isnull(dr,0)=0 and vbillno=?)";
		Object invoice_vbillno = deviMap.get("invoice_vbillno");
		if (invoice_vbillno != null) {
			// 根据发货单读取货品信息
			List<HashMap> goodsList = cacheMap.get(invoice_vbillno);
			if (goodsList == null) {
				goodsList = NWDao.getInstance().queryForList(sql, HashMap.class, invoice_vbillno);
				cacheMap.put(invoice_vbillno.toString(), goodsList);
			}
			if (goodsList != null && goodsList.size() > 0) {
				String goods_code = "", goods_name = "";
				int index = 0;
				for (HashMap goodsMap : goodsList) {
					goods_code += goodsMap.get("goods_code") == null ? "" : goodsMap.get("goods_code");
					goods_name += goodsMap.get("goods_name") == null ? "" : goodsMap.get("goods_name");
					if (index != goodsList.size() - 1) {
						goods_code += "/";
						goods_name += "/";
					}
					index++;
				}
				deviMap.put("goods_code", goods_code);
				deviMap.put("goods_name", goods_name);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setGoodsInfo(Map<String, Object> retMap) {
		Map<String, Object> bodyMap = (Map<String, Object>) retMap.get(Constants.BODY);
		if (bodyMap != null) {
			List<Map<String, Object>> deviList = (List<Map<String, Object>>) bodyMap.get(TabcodeConst.TS_ORDERLOT_DEVI);
			if (deviList != null && deviList.size() > 0) {
				Map<String, List<HashMap>> cacheMap = new HashMap<String, List<HashMap>>();
				for (Map<String, Object> deviMap : deviList) {
					setGoodsInfo(deviMap, cacheMap);
				}
				cacheMap.clear();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.service.impl.AbsToftServiceImpl#save(org.nw.vo.pub.
	 * AggregatedValueObject, org.nw.vo.ParamVO)
	 */
	@Override
	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		ExAggOrderlotVO aggVO = (ExAggOrderlotVO) billVO;// 界面上的vo
		OrderlotVO parentVO = (OrderlotVO) aggVO.getParentVO();

		OrderlotDeviVO[] deviVOs = (OrderlotDeviVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_DEVI);
		OrderlotRdVO[] olrVOs = (OrderlotRdVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_RD);
		List<OrderlotDeviVO> deviVOList = new ArrayList<OrderlotDeviVO>();
		Set<String> invoice_vbillnoAry = new HashSet<String>();
		Map<String, InvoiceVO> invCacheMap = new HashMap<String, InvoiceVO>();// invoice_vbillno,InvoiceVO
		Map<String, HYBillVO> rdCacheMap = new HashMap<String, HYBillVO>();// invoice_vbillno,HYBillVO
		Map<String, ReceDetailBVO> rdBCacheMap = new HashMap<String, ReceDetailBVO>();// pk_rece_detail_b,ReceDetailBVO
		Map<String, OrderlotDeviVO> deviCacheMap = new HashMap<String, OrderlotDeviVO>();
		if (deviVOs != null && deviVOs.length > 0) {
			for (OrderlotDeviVO deviVO : deviVOs) {
				String invoice_vbillno = deviVO.getInvoice_vbillno();
				deviVOList.add(deviVO);
				deviCacheMap.put(invoice_vbillno, deviVO);

				invoice_vbillnoAry.add(invoice_vbillno);
				InvoiceVO invVO = invCacheMap.get(invoice_vbillno);
				if (invVO == null) {
					invVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "vbillno=?", invoice_vbillno);
					if (invVO == null) {
						throw new BusiException("发货单[?]已经被删除！",invoice_vbillno);
					}
					if (invVO.getVbillstatus().intValue() != BillStatus.INV_PART_ARRIVAL
							&& invVO.getVbillstatus().intValue() != BillStatus.INV_ARRIVAL) {
						throw new BusiException("发货单[?]必须是[到货]或者[部分到货]状态才能重算费用！",invVO.getVbillno());
					}

					HYBillVO cacheVO = rdCacheMap.get(invoice_vbillno);
					if (cacheVO == null) {
						// 查询对应的应收明细
						ReceiveDetailVO rdVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class,
								"rece_type=? and invoice_vbillno=?", ReceiveDetailConst.ORIGIN_TYPE, invoice_vbillno);
						if (rdVO == null) {
							throw new BusiException("发货单[?]所对应的原始应收明细以及被删除！",invoice_vbillno);
						}
						if (rdVO.getVbillstatus() != BillStatus.NEW) {
							throw new BusiException("应收明细[?]必须是新建状态才能重算费用！",rdVO.getVbillno());
						}
						ReceDetailBVO[] detailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(
								ReceDetailBVO.class, "pk_receive_detail=?", rdVO.getPk_receive_detail());
						HYBillVO rdAggVO = new HYBillVO();
						rdAggVO.setParentVO(rdVO);
						rdAggVO.setChildrenVO(detailBVOs);
						rdCacheMap.put(invoice_vbillno, rdAggVO);

						if (detailBVOs != null) {
							for (ReceDetailBVO detailBVO : detailBVOs) {
								rdBCacheMap.put(detailBVO.getPk_rece_detail_b(), detailBVO);
							}
						}
					}
				}

			}

			if (olrVOs != null && olrVOs.length > 0) {
				for (OrderlotRdVO olrVO : olrVOs) {
					if (olrVO.getStatus() != VOStatus.NEW) {
						if (olrVO.getStatus() == VOStatus.DELETED) {
							// 删除批次应收明细，重新分摊到分摊表中
							for (OrderlotDeviVO deviVO : deviVOs) {
								if (olrVO.getPk_orderlot_rd().equals(deviVO.getPk_orderlot_rd())) {
									// 删除对应的费用分摊
									deviVO.setStatus(VOStatus.DELETED);
									ReceDetailBVO rdBVO = rdBCacheMap.get(deviVO.getPk_receive_detail_b());
									if (rdBVO != null) {
										// 删除对应的费用明细
										rdBVO.setStatus(VOStatus.DELETED);
									}
								}
							}
						} else if (olrVO.getStatus() == VOStatus.UPDATED) {
							// 修改了金额的记录，重新分摊到分摊表中
							for (OrderlotDeviVO deviVO : deviVOs) {
								if (olrVO.getPk_orderlot_rd().equals(deviVO.getPk_orderlot_rd())) {
									UFDouble amount = UFDouble.ZERO_DBL;
									try {// 避免因为被除数为0引起的异常
										amount = olrVO.getAmount().div(parentVO.getFee_weight_count())
												.multiply(deviVO.getFee_weight_count());
									} catch (Exception e) {

									}
									deviVO.setAmount(amount.setScale(2, UFDouble.ROUND_HALF_UP));
									deviVO.setStatus(VOStatus.UPDATED);

									// 更新对应的费用明细
									ReceDetailBVO rdBVO = rdBCacheMap.get(deviVO.getPk_receive_detail_b());
									if (rdBVO != null) {
										rdBVO.setAmount(deviVO.getAmount());
										rdBVO.setStatus(VOStatus.UPDATED);
									}
								}
							}
						}
					} else {
						NWDao.setUuidPrimaryKey(olrVO);// 先设置一个pk
						// 新增加了批次费用，那么为每个发货单增加一行记录
						for (String invoice_vbillno : invoice_vbillnoAry) {
							HYBillVO rdAggVO = rdCacheMap.get(invoice_vbillno);
							ReceiveDetailVO rdVO = (ReceiveDetailVO) rdAggVO.getParentVO();
							OrderlotDeviVO refVO = deviCacheMap.get(invoice_vbillno);

							OrderlotDeviVO newVO = new OrderlotDeviVO();
							newVO.setLot(parentVO.getLot());
							newVO.setPk_orderlot_rd(olrVO.getPk_orderlot_rd());
							newVO.setInvoice_vbillno(invoice_vbillno);
							newVO.setRd_vbillno(refVO.getRd_vbillno());
							newVO.setPk_receive_detail(rdVO.getPk_receive_detail());
							newVO.setValuation_type(olrVO.getValuation_type());
							newVO.setPk_expense_type(olrVO.getPk_expense_type());
							newVO.setNode_count(olrVO.getNode_count());
							newVO.setCar_weight(olrVO.getCar_weight());
							newVO.setCar_num(olrVO.getCar_num());
							// 件数重量，体积，计费重，应该是对应的发货单的数据
							newVO.setNum_count(olrVO.getNum_count());
							newVO.setWeight_count(olrVO.getWeight_count());
							newVO.setVolume_count(olrVO.getVolume_count());
							newVO.setFee_weight_count(deviCacheMap.get(invoice_vbillno).getFee_weight_count());

							// 根据计费重分摊，并且只取两位小数,批次费用表不能输入计费重，分摊到发货单时，去发货单的计费重
							UFDouble amount = UFDouble.ZERO_DBL;
							try {
								amount = olrVO.getAmount().div(parentVO.getFee_weight_count())
										.multiply(newVO.getFee_weight_count());
							} catch (Exception e) {

							}
							newVO.setAmount(amount.setScale(2, UFDouble.ROUND_HALF_UP));
							//
							newVO.setPk_contract_b(null);
							newVO.setSystem_create(UFBoolean.TRUE);
							newVO.setCreate_time(new UFDateTime(new Date()));
							newVO.setStatus(VOStatus.NEW);
							NWDao.setUuidPrimaryKey(newVO);
							deviVOList.add(newVO);

							// 增加费用明细
							ReceDetailBVO newRdBVO = new ReceDetailBVO();
							newRdBVO.setPk_receive_detail(refVO.getPk_receive_detail());
							newRdBVO.setPk_expense_type(olrVO.getPk_expense_type());
							newRdBVO.setValuation_type(olrVO.getValuation_type());
							newRdBVO.setPrice(olrVO.getPrice());
							newRdBVO.setAmount(newVO.getAmount());
							newRdBVO.setContract_amount(newRdBVO.getAmount());
							newRdBVO.setSystem_create(UFBoolean.TRUE);
							newRdBVO.setStatus(VOStatus.NEW);
							NWDao.setUuidPrimaryKey(newRdBVO);
							newVO.setPk_receive_detail_b(newRdBVO.getPk_rece_detail_b());// 注意这里设置下关联

							// 将新创建的vo合并到billVO中
							HYBillVO rdBillVO = rdCacheMap.get(invoice_vbillno);
							ReceDetailBVO[] rdBVOs = (ReceDetailBVO[]) rdBillVO.getChildrenVO();
							ReceDetailBVO[] newRdBVOs;
							if (rdBVOs != null && rdBVOs.length > 0) {
								newRdBVOs = new ReceDetailBVO[rdBVOs.length + 1];
								for (int i = 0; i < rdBVOs.length; i++) {
									newRdBVOs[i] = rdBVOs[i];
								}
								newRdBVOs[newRdBVOs.length - 1] = newRdBVO;
							} else {
								newRdBVOs = new ReceDetailBVO[] { newRdBVO };
							}
							rdBillVO.setChildrenVO(newRdBVOs);
						}
					}
				}
			}
		}

		// 合计到表头
		for (String key : rdCacheMap.keySet()) {
			HYBillVO rdBillVO = rdCacheMap.get(key);
			ReceiveDetailVO rdVO = (ReceiveDetailVO) rdBillVO.getParentVO();
			ReceDetailBVO[] rdBVOs = (ReceDetailBVO[]) rdBillVO.getChildrenVO();
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			if (rdBVOs != null) {
				for (ReceDetailBVO rdBVO : rdBVOs) {
					if (rdBVO.getStatus() != VOStatus.DELETED) {
						cost_amount = cost_amount.add(rdBVO.getAmount());
					}
				}
			}
			rdVO.setCost_amount(cost_amount);
			rdVO.setStatus(VOStatus.UPDATED);

			NWDao.getInstance().saveOrUpdate(rdBillVO);
			//通过公共方法来修改应收明细表头信息JONATHAN 2015-10-26
			CMUtils.processExtenalforComputer(rdVO, rdBVOs);

			InvoiceVO invVO = invCacheMap.get(rdVO.getInvoice_vbillno());
			if (invVO != null) {
				invVO.setCost_amount(rdVO.getCost_amount());
				invVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(invVO);
			}
		}

		aggVO.setTableVO(TabcodeConst.TS_ORDERLOT_DEVI, deviVOList.toArray(new OrderlotDeviVO[deviVOList.size()]));
		billVO = super.save(billVO, paramVO);
		invCacheMap.clear();
		rdCacheMap.clear();
		rdBCacheMap.clear();
		deviCacheMap.clear();
		return billVO;
	}

	/**
	 * 
	 * @param paramVO
	 * @param aggVO
	 * @param invoice_vbillnoAry
	 * @return
	 */
	private Map<String, Object> recompute(ParamVO paramVO, ExAggOrderlotVO aggVO, String[] invoice_vbillnoAry,
			boolean viewOnly) {
		OrderlotVO parentVO = (OrderlotVO) aggVO.getParentVO();
		// 重算前的订单
		OrderlotDeviVO[] deviVOs = (OrderlotDeviVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_DEVI);
		OrderlotInvVO[] oliVOs = (OrderlotInvVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_INV);
		OrderlotRdVO[] olrVOs = (OrderlotRdVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_RD);

		// 删除旧数据
		if (deviVOs != null) {
			NWDao.getInstance().delete(deviVOs);
		}
		if (oliVOs != null) {
			NWDao.getInstance().delete(oliVOs);
			// 更新订单状态为未计算
			List<SuperVO> invoiceVOs = new ArrayList<SuperVO>();
			InvoiceVO invoiceVO = null;
			for (int i = 0; i < oliVOs.length; i++) {
				invoiceVO = invoiceService.getByVbillno(oliVOs[i].getInvoice_vbillno());
				invoiceVO.setDef4(null);
				invoiceVOs.add(invoiceVO);
			}
			try {
				invoiceService.saveOrUpdate(invoiceVOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (olrVOs != null) {
			NWDao.getInstance().delete(olrVOs);
		}
		aggVO.removeTableVO(TabcodeConst.TS_ORDERLOT_DEVI);
		aggVO.removeTableVO(TabcodeConst.TS_ORDERLOT_INV);
		aggVO.removeTableVO(TabcodeConst.TS_ORDERLOT_RD);

		if (deviVOs == null || deviVOs.length == 0) {

			return this.execFormula4Templet(aggVO, paramVO, true, true);
		} else {
			// 找出那些删除的发货单，将他们的应收明细和发货单进行修改
			Set<String> deleteAry = new HashSet<String>();
			for (OrderlotDeviVO deviVO : deviVOs) {
				boolean exist = false;
				if (invoice_vbillnoAry != null) {
					for (String invoice_vbillno : invoice_vbillnoAry) {
						if (invoice_vbillno.equals(deviVO.getInvoice_vbillno())) {
							exist = true;
							break;
						}
					}
				}
				if (!exist) {
					deleteAry.add(deviVO.getInvoice_vbillno());
				}
			}
			for (String deleteID : deleteAry) {
				InvoiceVO invVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "vbillno=?", deleteID);
				if (invVO == null) {
					throw new BusiException("发货单[?]已经被删除！",deleteID);
				}
				ReceiveDetailVO rdVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class,
						"rece_type=? and invoice_vbillno=?", ReceiveDetailConst.ORIGIN_TYPE, deleteID);
				if (rdVO == null) {
					throw new BusiException("发货单[?]所对应的原始应收明细以及被删除！",deleteID);
				}
				if (rdVO.getVbillstatus() != BillStatus.NEW) {
					throw new BusiException("应收明细[?]必须是新建状态才能重算费用！",rdVO.getVbillno());
				}
				ReceDetailBVO[] detailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceDetailBVO.class,
						"pk_receive_detail=?", rdVO.getPk_receive_detail());
				UFDouble amount = UFDouble.ZERO_DBL;
				if (detailBVOs != null) {
					for (ReceDetailBVO detailBVO : detailBVOs) {
						if (detailBVO.getSystem_create() != null && detailBVO.getSystem_create().booleanValue()) {
							// 删除系统创建的费用明细
							NWDao.getInstance().delete(detailBVO);
						} else {
							amount = amount.add(detailBVO.getAmount());
						}
					}
				}
				// 更新应收明细主表
				rdVO.setCost_amount(amount);
				rdVO.setStatus(VOStatus.UPDATED);
				// NWDao.getInstance().saveOrUpdate(rdVO);
				try {
					receiveDetailService.saveOrUpdate(rdVO);
					//通过公共方法来修改应收明细表头信息JONATHAN 2015-10-26
					CMUtils.processExtenalforComputer(rdVO, detailBVOs);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// 更新发货单
				invVO.setCost_amount(amount);
				invVO.setDef4(null);
				invVO.setStatus(VOStatus.UPDATED);
				try {
					invoiceService.saveOrUpdate(invVO);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// NWDao.getInstance().saveOrUpdate(invVO);
			}
		}
		// OrderlotDeviVO firstDeviVO = deviVOs[0];// 第一个
		// 获取未删除的订单
		InvoiceVO[] invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
				"vbillno in " + NWUtils.buildConditionString(invoice_vbillnoAry));
		if (invVOs == null || invVOs.length == 0) {
			return this.execFormula4Templet(aggVO, paramVO, true, true);
		}

		List<MatchVO> matchVOs = new ArrayList<MatchVO>();
		for (int i = 0; i < invVOs.length; i++) {
			InvoiceVO invVO = invVOs[i];
			OrderlotDeviVO tmpDeviVO = null;
			// 未删除的订单 需要匹配删除之前的分摊明细
			for (int j = 0; j < deviVOs.length; j++) {
				if (invVO.getVbillno().equalsIgnoreCase(deviVOs[j].getInvoice_vbillno()))
					tmpDeviVO = deviVOs[j];
			}
			MatchVO matchVO = new MatchVO();
			matchVO.setInvoice_vbillno(invVO.getVbillno());
			matchVO.setPk_trans_type(invVO.getPk_trans_type());
			matchVO.setPk_customer(invVO.getPk_customer());
			matchVO.setBala_customer(invVO.getBala_customer());
			matchVO.setOrderno(invVO.getOrderno());
			matchVO.setPack_num_count(invVO.getPack_num_count());
			matchVO.setNum_count(invVO.getNum_count());
			matchVO.setWeight_count(invVO.getWeight_count());
			matchVO.setVolume_count(invVO.getVolume_count());
			matchVO.setFee_weight_count(invVO.getFee_weight_count());
			matchVO.setPk_corp(invVO.getPk_corp());
			matchVO.setUrgent_level(invVO.getUrgent_level());
			matchVO.setItem_code(invVO.getItem_code());
			matchVO.setPk_trans_line(invVO.getPk_trans_line());
			matchVO.setIf_return(invVO.getIf_return());

			matchVO.setPk_delivery(invVO.getPk_delivery());
			matchVO.setDeli_city(invVO.getDeli_city());
			matchVO.setPk_arrival(invVO.getPk_arrival());
			matchVO.setArri_city(invVO.getArri_city());
			matchVO.setReq_deli_date(invVO.getReq_deli_date());
			matchVO.setReq_arri_date(invVO.getReq_arri_date());
			matchVO.setAct_deli_date(invVO.getAct_deli_date());
			matchVO.setAct_arri_date(invVO.getAct_arri_date());

			matchVO.setRd_vbillno(tmpDeviVO.getRd_vbillno());
			matchVO.setPk_receive_detail(tmpDeviVO.getPk_receive_detail());

			if (StringUtils.isBlank(invVO.getOrderno())) {
				throw new BusiException("发货单[?]的订单号不能为空！",invVO.getVbillno());
			}
			if (invVO.getVbillstatus().intValue() != BillStatus.INV_PART_ARRIVAL
					&& invVO.getVbillstatus().intValue() != BillStatus.INV_ARRIVAL
					&& invVO.getVbillstatus().intValue() != BillStatus.INV_SIGN
					&& invVO.getVbillstatus().intValue() != BillStatus.INV_PART_SIGN
					&& invVO.getVbillstatus().intValue() != BillStatus.INV_BACK) {
				throw new BusiException("发货单[?]必须是[到货]、[签收]、[部分签收]、[已回单]或者[部分到货]状态才能重算费用！",invVO.getVbillno());
			}
			// 读取订单辅助表的信息
			OrderAssVO oaVO = NWDao.getInstance().queryByCondition(OrderAssVO.class, "orderno=?", invVO.getOrderno());
			if (oaVO == null) {
				throw new BusiException("订单辅助表记录已经被删除，订单号[?]！",invVO.getOrderno());
			}
//			matchVO.setPk_delivery_ass(oaVO.getPk_delivery());
//			matchVO.setDeli_city_ass(oaVO.getDeli_city());
//			matchVO.setPk_arrival_ass(oaVO.getPk_arrival());
//			matchVO.setArri_city_ass(oaVO.getArri_city());
//			matchVO.setReq_deli_date_ass(oaVO.getReq_deli_date());
//			matchVO.setReq_arri_date_ass(oaVO.getReq_arri_date());
//			matchVO.setAct_deli_date_ass(oaVO.getAct_deli_date());
//			matchVO.setAct_arri_date_ass(oaVO.getAct_arri_date());
//			matchVO.setPk_supplier_ass(oaVO.getPk_supplier());

			matchVOs.add(matchVO);
		}

		ReceDetailBuilder builder = new ReceDetailBuilder();
		builder.setCurAggVO(aggVO);// 设置当前要修改的批次vo
		builder.setViewOnly(viewOnly);
		builder.before(null);
		if (parentVO.getIffx() != null && parentVO.getIffx().booleanValue()) {
			builder.buildFX(matchVOs);
		} else {
			if (parentVO.getSeg_type() == SegmentConst.SEG_TYPE_THD) {
				builder.buildTHD(matchVOs);
			} else if (parentVO.getSeg_type() == SegmentConst.SEG_TYPE_GXD) {
				builder.buildGXD(matchVOs);
			} else if (parentVO.getSeg_type() == SegmentConst.SEG_TYPE_SHD) {
				builder.buildSHD(matchVOs);
			}
		}
		builder.after(null);
		Map<String, Object> retMap = this.execFormula4Templet(aggVO, paramVO, true, true);
		setGoodsInfo(retMap);
		return retMap;
	}

	public Map<String, Object> doRecompute(ParamVO paramVO, String lot, String[] invoice_vbillnoAry) {
		if (StringUtils.isBlank(lot)) {
			return null;
		}
		paramVO.setBillId(lot);
		ExAggOrderlotVO aggVO = (ExAggOrderlotVO) this.queryBillVO(paramVO);
		if (aggVO == null) {
			throw new BusiException("批次订单记录已经被删除，批次号[?]！",lot);
		}
		return recompute(paramVO, aggVO, invoice_vbillnoAry, false);
	}

}
