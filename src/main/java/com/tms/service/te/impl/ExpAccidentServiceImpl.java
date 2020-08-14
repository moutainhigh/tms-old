package com.tms.service.te.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.utils.BillnoHelper;
import org.nw.utils.ParameterHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.te.ExpAccidentService;
import com.tms.service.tp.impl.PZUtils;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 异常事故处理
 * 
 * @author xuqc
 * @date 2013-5-5 下午12:04:08
 */
@Service
public class ExpAccidentServiceImpl extends TMSAbsBillServiceImpl implements ExpAccidentService {

	@Autowired
	private ExpenseTypeService expenseTypeService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ExpAccidentVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ExpAccidentVO.PK_EXP_ACCIDENT);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.YCSG;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("invoice_vbillno")) {
					fieldVO.setRenderer("invoice_vbillnoRenderer");
					// 过滤条件
					fieldVO.setUserdefine3("entrust_vbillno:${Ext.getCmp('entrust_vbillno').getValue()}");
				} else if(fieldVO.getItemkey().equals("entrust_vbillno")) {
					fieldVO.setRenderer("entrust_vbillnoRenderer");
					fieldVO.setUserdefine3("invoice_vbillno:${Ext.getCmp('invoice_vbillno').getValue()}");
				}
			}
		}
		return templetVO;
	}

	public String getCustomerByInvoice_vbillno(String invoice_vbillno) {
		if(StringUtils.isBlank(invoice_vbillno)) {
			return null;
		}
		String sql = "select pk_customer from ts_invoice where vbillno=? and isnull(dr,0)=0";
		return NWDao.getInstance().queryForObject(sql, String.class, invoice_vbillno);
	}

	public String getCarrierByEntrust_vbillno(String entrust_vbillno) {
		if(StringUtils.isBlank(entrust_vbillno)) {
			return null;
		}
		String sql = "select pk_carrier from ts_entrust where vbillno=? and isnull(dr,0)=0";
		return NWDao.getInstance().queryForObject(sql, String.class, entrust_vbillno);
	}

	public String getByInvoice_vbillno(String invoice_vbillno) {
		if(StringUtils.isNotBlank(invoice_vbillno)) {
			String sql = "select vbillno from ts_exp_accident where isnull(dr,0)=0 and invoice_vbillno=?";
			List<String> vbillnoList = NWDao.getInstance().queryForList(sql, String.class, invoice_vbillno);
			if(vbillnoList != null && vbillnoList.size() > 0) {
				StringBuffer buf = new StringBuffer();
				for(String vbillno : vbillnoList) {
					buf.append(vbillno);
					buf.append("|");
				}
				return buf.substring(0, buf.length() - 1);
			}
		}
		return null;
	}

	public String getByEntrust_vbillno(String entrust_vbillno) {
		if(StringUtils.isNotBlank(entrust_vbillno)) {
			String sql = "select vbillno from ts_exp_accident where isnull(dr,0)=0 and entrust_vbillno=?";
			List<String> vbillnoList = NWDao.getInstance().queryForList(sql, String.class, entrust_vbillno);
			if(vbillnoList != null && vbillnoList.size() > 0) {
				StringBuffer buf = new StringBuffer();
				for(String vbillno : vbillnoList) {
					buf.append(vbillno);
					buf.append("|");
				}
				return buf.substring(0, buf.length() - 1);
			}
		}
		return null;
	}

	public void addExpAccident(ExpAccidentVO eaVO) {
		logger.info("登记异常事故...");
		if(eaVO == null) {
			return;
		}
		if(eaVO.getDbilldate() == null) {
			eaVO.setDbilldate(new UFDate());
		}
		eaVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		eaVO.setCreate_time(new UFDateTime(new Date()));
		eaVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.EXP_ACCIDENT_CODE);
		setCodeField(eaVO, paramVO);
		eaVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(eaVO);
		NWDao.getInstance().saveOrUpdate(eaVO);
	}

	protected Integer getConfirmStatus() {
		return BillStatus.EA_WHANDLE;
	}

	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		ExpAccidentVO parentVO = (ExpAccidentVO) billVO.getParentVO();
		// 如果此时的状态是待处理和处理中，那么此时的保存实际上是处理
		if(parentVO.getVbillstatus().intValue() == BillStatus.EA_WHANDLE) {
			// 更新状态为处理中
			parentVO.setVbillstatus(BillStatus.EA_HANDLING);
		}
		// 未赔金额等于索赔金额减去已赔金额
		if(parentVO.getCust_claimant_amount() != null) {
			UFDouble cust_claimant_amount = parentVO.getCust_claimant_amount();
			UFDouble cust_claimanted_amount = parentVO.getCust_claimanted_amount() == null ? new UFDouble(0) : parentVO
					.getCust_claimanted_amount();
			parentVO.setCust_unclaimant_amount(cust_claimant_amount.sub(cust_claimanted_amount));
		}
		if(parentVO.getCarr_claimant_amount() != null) {
			UFDouble carr_claimant_amount = parentVO.getCarr_claimant_amount();
			UFDouble carr_claimanted_amount = parentVO.getCarr_claimanted_amount() == null ? new UFDouble(0) : parentVO
					.getCarr_claimanted_amount();
			parentVO.setCarr_unclaimant_amount(carr_claimant_amount.sub(carr_claimanted_amount));
		}

		billVO = super.save(billVO, paramVO);// 保存异常事故
		if(parentVO.getVbillstatus().intValue() == BillStatus.EA_WHANDLE
				|| parentVO.getVbillstatus().intValue() == BillStatus.EA_HANDLING) {
			// 根据发货单，客户，应收明细的类型查询应收明细，如果存在记录
			ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class,
					"invoice_vbillno=? and pk_customer=? and rece_type=?", parentVO.getInvoice_vbillno(),
					parentVO.getPk_customer(), ReceiveDetailConst.CUST_CLAIMANT_TYPE);
			// 勾选客户是否索赔时，向应收明细插入一条记录
			if(parentVO.getCust_claimant_flag().booleanValue()) {
				ReceDetailBVO detailVO = null;
				if(rdVO == null) {
					rdVO = new ReceiveDetailVO();
					rdVO.setVbillstatus(BillStatus.NEW);
					rdVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(rdVO);

					// 插入明细
					detailVO = new ReceDetailBVO();
					String typeCode = ExpenseTypeConst.ET20;
					ExpenseTypeVO typeVO = (ExpenseTypeVO) expenseTypeService.getByCode(typeCode);
					if(typeVO == null) {
						throw new BusiException("费用类型表必须存在一条类型编码是[?]的记录！",typeCode);
					}
					detailVO.setPk_expense_type(typeVO.getPk_expense_type());
					detailVO.setValuation_type(ValuationTypeConst.TICKET);
					detailVO.setPrice_type(PriceTypeConst.REGULAR_PRICE);
					detailVO.setStatus(VOStatus.NEW);
					detailVO.setPk_receive_detail(rdVO.getPk_receive_detail());

					NWDao.setUuidPrimaryKey(detailVO);
				} else {
					rdVO.setStatus(VOStatus.UPDATED);

					// 读取明细记录
					detailVO = dao.queryByCondition(ReceDetailBVO.class, "pk_receive_detail=?",
							rdVO.getPk_receive_detail());
					detailVO.setStatus(VOStatus.UPDATED);
				}
				if(rdVO.getDbilldate() == null) {
					rdVO.setDbilldate(new UFDate());
				}
				rdVO.setRelationid(parentVO.getPk_exp_accident());// 设置关联id，当已赔金额改变时可以同步改变异常事故的金额
				rdVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YSMX));
				rdVO.setRece_type(ReceiveDetailConst.CUST_CLAIMANT_TYPE);
				rdVO.setInvoice_vbillno(parentVO.getInvoice_vbillno());
				rdVO.setPk_customer(parentVO.getPk_customer());
				rdVO.setBala_customer(parentVO.getPk_customer());
				rdVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());// 默认月结
				rdVO.setCost_amount(new UFDouble(0 - (parentVO.getCust_claimant_amount()
						 == null ? 0 : parentVO.getCust_claimant_amount().doubleValue())));
				rdVO.setUngot_amount(new UFDouble(0  - (parentVO.getCust_claimant_amount()
						 == null ? 0 : parentVO.getCust_claimant_amount().doubleValue())));
				rdVO.setCurrency(ParameterHelper.getCurrency());
				rdVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				rdVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				rdVO.setCreate_time(new UFDateTime(new Date()));
				dao.saveOrUpdate(rdVO);
				// 更新明细记录
				detailVO.setAmount((new UFDouble(0 - (parentVO.getCust_claimant_amount()
						 == null ? 0 : parentVO.getCust_claimant_amount().doubleValue()))));
				dao.saveOrUpdate(detailVO);
			} else {
				// 检查是否存在对应的应收明细，若存在，删除该记录,如果该记录的状态不是新建，那么提示不能修改该值
				if(rdVO != null) {
					dao.delete(rdVO);
					ReceDetailBVO detailVO = dao.queryByCondition(ReceDetailBVO.class, "pk_receive_detail=?",
							rdVO.getPk_receive_detail());
					dao.delete(detailVO);
				}
			}
			// 根据委托单，承运商，应付明细的类型查询应付明细，如果存在记录
			PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "entrust_vbillno=? and pk_carrier=? and pay_type=?",
					parentVO.getEntrust_vbillno(), parentVO.getPk_carrier(), PayDetailConst.CARR_CLAIMANT_TYPE);
			// 勾选承运商是否索赔时，想应付明细插入一条记录
			if(parentVO.getCarr_claimant_flag().booleanValue()) {
				PayDetailBVO detailVO = null;
				if(pdVO == null && StringUtils.isNotBlank(parentVO.getEntrust_vbillno())) {
					pdVO = new PayDetailVO();
					pdVO.setVbillstatus(BillStatus.NEW);
					pdVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(pdVO);

					// 插入明细
					detailVO = new PayDetailBVO();
					String typeCode = ExpenseTypeConst.ET30;
					ExpenseTypeVO typeVO = (ExpenseTypeVO) expenseTypeService.getByCode(typeCode);
					if(typeVO == null) {
						throw new BusiException("费用类型表必须存在一条类型编码是[?]的记录！",typeCode);
					}
					detailVO.setPk_expense_type(typeVO.getPk_expense_type());
					detailVO.setValuation_type(ValuationTypeConst.TICKET);
					detailVO.setPrice_type(PriceTypeConst.REGULAR_PRICE);
					detailVO.setStatus(VOStatus.NEW);
					detailVO.setPk_pay_detail(pdVO.getPk_pay_detail());
					NWDao.setUuidPrimaryKey(detailVO);
				} else {
					pdVO.setStatus(VOStatus.UPDATED);

					// 读取明细记录
					detailVO = dao.queryByCondition(PayDetailBVO.class, "pk_pay_detail=?", pdVO.getPk_pay_detail());
					detailVO.setStatus(VOStatus.UPDATED);
				}
				if(pdVO.getDbilldate() == null) {
					pdVO.setDbilldate(new UFDate());
				}
				pdVO.setRelationid(parentVO.getPk_exp_accident());// 设置关联id，当已赔金额改变时可以同步改变异常事故的金额
				pdVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
				pdVO.setPay_type(PayDetailConst.CARR_CLAIMANT_TYPE);
				pdVO.setEntrust_vbillno(parentVO.getEntrust_vbillno());
				pdVO.setPk_carrier(parentVO.getPk_carrier());
				pdVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());// 默认月结
				pdVO.setCost_amount((new UFDouble(0 - (parentVO.getCarr_claimant_amount()
						 == null ? 0 : parentVO.getCarr_claimant_amount().doubleValue()))));
				pdVO.setUngot_amount((new UFDouble(0 - (parentVO.getCarr_claimant_amount()
						 == null ? 0 : parentVO.getCarr_claimant_amount().doubleValue()))));
				pdVO.setCurrency(ParameterHelper.getCurrency());
				pdVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				pdVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				pdVO.setCreate_time(new UFDateTime(new Date()));
				dao.saveOrUpdate(pdVO);
				// 更新明细记录
				detailVO.setAmount((new UFDouble(0 - (parentVO.getCarr_claimant_amount()
						 == null ? 0 : parentVO.getCarr_claimant_amount().doubleValue()))));
				dao.saveOrUpdate(detailVO);

				// 对承运商索赔进行成本分摊
				EntrustVO entVO = dao.queryByCondition(EntrustVO.class, "vbillno=?", parentVO.getEntrust_vbillno());
				if(StringUtils.isNotBlank(parentVO.getInvoice_vbillno())) {
					// 选择了一个委托单，将费用都分摊到该发货单
					InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "vbillno=?", parentVO.getInvoice_vbillno());
					PayDeviBVO deviVO = new PayDeviBVO();
					deviVO.setPk_entrust(entVO.getPk_entrust());
					deviVO.setPk_invoice(invVO.getPk_invoice());
					deviVO.setPk_carrier(parentVO.getPk_carrier());
					deviVO.setPk_car_type(entVO.getPk_car_type());
					deviVO.setPk_expense_type(detailVO.getPk_expense_type());
					deviVO.setSys_devi_amount(pdVO.getCost_amount());
					deviVO.setMan_devi_amount(deviVO.getSys_devi_amount());
					deviVO.setPk_pay_detail(pdVO.getPk_pay_detail());
					deviVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(deviVO);
					dao.saveOrUpdate(deviVO);
				} else {
					// 没有选择委托单，那么分摊到委托单的所有发货单中
					// 1、根据委托单查询发货单
					List<InvoiceVO> invVOs = dao
							.queryForList(
									"select * from ts_invoice where isnull(dr,0)=0 and pk_invoice in (select pk_invoice from ts_ent_inv_b where isnull(dr,0)=0 and pk_entrust=?)",
									InvoiceVO.class, entVO.getPk_entrust());
					List<SegmentVO> segVOs = dao
							.queryForList(
									"select * from ts_segment where isnull(dr,0)=0 and pk_segment in (select pk_segment from ts_ent_seg_b where isnull(dr,0)=0 and pk_entrust=?)",
									SegmentVO.class, entVO.getPk_entrust());
					if(invVOs != null) {
						List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entVO,
								invVOs.toArray(new InvoiceVO[invVOs.size()]),
								segVOs.toArray(new SegmentVO[segVOs.size()]), new PayDetailBVO[] { detailVO });
						dao.saveOrUpdate(deviBVOs.toArray(new PayDeviBVO[deviBVOs.size()]));
					}
				}
			} else {
				// 检查是否存在对应的应付明细，若存在，删除该记录,如果该记录的状态不是新建，那么提示不能修改该值
				if(pdVO != null) {
					dao.delete(pdVO);
					PayDetailBVO detailVO = dao.queryByCondition(PayDetailBVO.class, "pk_pay_detail=?",
							pdVO.getPk_pay_detail());
					dao.delete(detailVO);
				}
			}
		}
		return billVO;
	}

	public Map<String, Object> doRevocation(ParamVO paramVO) {
		logger.info("异常事故撤销处理，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.EA_HANDLING != billStatus && BillStatus.EA_HANDLED != billStatus) {
				throw new BusiException("只有[处理中、已处理]状态的单据才能进行撤销！");
			}
		}
		ExpAccidentVO eaVO = (ExpAccidentVO) parentVO;

		// 没有勾选客户是否索赔，承运商是否索赔，进行撤销处理时提示不能进行撤销
		if(!((eaVO.getCust_claimant_flag() != null && eaVO.getCust_claimant_flag().booleanValue()) || (eaVO
				.getCarr_claimant_flag() != null && eaVO.getCarr_claimant_flag().booleanValue()))) {
			throw new BusiException("没有勾选客户是否索赔和承运商是否索赔，不能进行撤销！");
		}

		// 判断对应的应收明细和应付明细是否还是新建状态，如果是，那么同时删除
		if(StringUtils.isNotBlank(eaVO.getInvoice_vbillno())) {
			ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class,
					"invoice_vbillno=? and pk_customer=? and rece_type=?", eaVO.getInvoice_vbillno(), eaVO.getPk_customer(),
					ReceiveDetailConst.CUST_CLAIMANT_TYPE);
			if(rdVO != null) {
				if(rdVO.getVbillstatus().intValue() == BillStatus.NEW) {
					dao.delete(rdVO);
					ReceDetailBVO detailVO = dao.queryByCondition(ReceDetailBVO.class, "pk_receive_detail=?",
							rdVO.getPk_receive_detail());
					dao.delete(detailVO);
				} else {
					throw new BusiException("客户索赔对应的应收明细不是新建状态,不能撤销处理！");
				}
			}
		}
		PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "entrust_vbillno=? and pk_carrier=? and pay_type=?",
				eaVO.getEntrust_vbillno(), eaVO.getPk_carrier(), PayDetailConst.CARR_CLAIMANT_TYPE);
		if(pdVO != null) {
			if(pdVO.getVbillstatus().intValue() == BillStatus.NEW) {
				dao.delete(pdVO);
				PayDetailBVO detailVO = dao.queryByCondition(PayDetailBVO.class, "pk_pay_detail=?",
						pdVO.getPk_pay_detail());
				dao.delete(detailVO);
			} else {
				throw new BusiException("承运商索赔对应的应付明细不是新建状态,不能撤销处理！");
			}
		}

		// 清空已处理的数据
		eaVO.setReason_type(null);
		eaVO.setReason_memo(null);
		eaVO.setPsndoc_flag(null);
		eaVO.setPk_psndoc(null);
		eaVO.setCustomer_flag(null);
		eaVO.setCarrier_flag(null);
		eaVO.setCorrect_measure(null);
		eaVO.setLongterm_measure(null);
		eaVO.setCust_claimant_flag(null);
		eaVO.setCust_claimant_amount(null);
		eaVO.setCarr_claimant_flag(null);
		eaVO.setCarr_claimant_amount(null);
		eaVO.setExp_level(null);
		eaVO.setLevel_memo(null);

		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.EA_WHANDLE); // 设置成待处理
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	public Map<String, Object> doFinish(ParamVO paramVO) {
		logger.info("异常事故结案，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.EA_HANDLING != billStatus) {
				throw new BusiException("只有[处理中]状态的单据才能进行结案！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.EA_HANDLED); // 设置成已处理
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	public Map<String, Object> doUnfinish(ParamVO paramVO) {
		logger.info("异常事故撤销结案，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.EA_HANDLED != billStatus) {
				throw new BusiException("只有[已处理]状态的单据才能进行撤销结案！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.EA_HANDLING);
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	public Map<String, Object> doClose(ParamVO paramVO) {
		logger.info("关闭异常事故，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.EA_CLOSED);
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

}
