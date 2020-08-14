package com.tms.service.cm.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CorpHelper;
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
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.PayDetailService;
import com.tms.service.job.cm.PayDetailBuilder;
import com.tms.service.job.cm.PayDetailMatchVO;
import com.tms.service.te.EntrustService;
import com.tms.service.te.impl.EntrustUtils;
import com.tms.service.tp.impl.PZUtils;
import com.tms.utils.CostCalculateUtils;
import com.tms.utils.CostCalculater;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayCheckSheetBVO;
import com.tms.vo.cm.PayCheckSheetVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.PayRecordVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.OrderAssVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.te.ExpAccidentVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 应付明细
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:30:41
 */
@Service
public class PayDetailServiceImpl extends TMSAbsBillServiceImpl implements PayDetailService {
	@Autowired
	EntrustService entrustService;
	
	@Autowired
	ContractService contractService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggPayDetailVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggPayDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PayDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PayDetailVO.PK_PAY_DETAIL);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggPayDetailVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, PayDetailBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, PayDetailBVO.PK_PAY_DETAIL);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_pay_detail_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_pay_detail_b");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggPayDetailVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, PayDeviBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, PayDeviBVO.PK_PAY_DETAIL);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_pay_devi_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_pay_devi_b");
			
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggPayDetailVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, ExpAccidentVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, "entrust_vbillno");
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_exp_accident");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_exp_accident");

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1, childVO2};
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.YFMX;
	}

	public UiBillTempletVO getBillTempletVOByFunCode(ParamVO paramVO) {
		UiBillTempletVO templetVO = super.getBillTempletVOByFunCode(paramVO);
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
		processor.setPos(UiConstants.POS[1]);
		processor.setTable_code(TabcodeConst.TS_PAY_RECORD);
		processor.setPk_billtemplet(templetVO.getTemplateID());
		processor.setPk_billtemplet_b(UUID.randomUUID().toString());
		templetVO.getFieldVOs().add(0, processor);
		return templetVO;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("bala_customer")) {
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				} else if(fieldVO.getItemkey().equals("num_count")) {
					fieldVO.setUserdefine1("afterChangeNumCount(field,value,originalValue)");
				} else if(fieldVO.getItemkey().equals("fee_weight_count")) {
					fieldVO.setUserdefine1("afterChangeFeeWeightCount(field,value,originalValue)");
				} else if(fieldVO.getItemkey().equals("volume_count")) {
					fieldVO.setUserdefine1("afterChangeVolumeCount(field,value,originalValue)");
				} else if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("invoice_vbillno")) {
					fieldVO.setRenderer("invoice_vbillnoRenderer");
				} else if(fieldVO.getItemkey().equals("entrust_vbillno")) {
					fieldVO.setRenderer("entrust_vbillnoRenderer");
				} else if(fieldVO.getItemkey().equals("cost_amount") || fieldVO.getItemkey().equals("got_amount")
						|| fieldVO.getItemkey().equals("ungot_amount")) {
					fieldVO.setBeforeRenderer("amountBeforeRenderer");
				}
			} else if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getTable_code().equals(TabcodeConst.TS_PAY_DETAIL_B)) {
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

		// List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		// for(int i = 0; i < fieldVOs.size(); i++) {
		// BillTempletBVO fieldVO = fieldVOs.get(i);
		// // 不需要显示收款纪录了，在弹出框中显示
		// if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
		// if(fieldVO.getTable_code().equalsIgnoreCase(TabcodeConst.TS_RECE_RECORD))
		// {
		// fieldVOs.remove(fieldVO);
		// i--;
		// }
		// }
		// }
		// // 不需要显示收款纪录了，在弹出框中显示
		// List<BillTempletTVO> tabVOs = templetVO.getTabVOs();
		// for(int i = 0; i < tabVOs.size(); i++) {
		// BillTempletTVO tabVO = tabVOs.get(i);
		// if(tabVO.getPos().intValue() == UiConstants.POS[1]
		// && tabVO.getTabcode().equalsIgnoreCase(TabcodeConst.TS_RECE_RECORD))
		// {
		// tabVOs.remove(tabVO);
		// i--;
		// }
		// }
		return templetVO;
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		AggregatedValueObject billVO = super.queryBillVO(paramVO);
		ExAggPayDetailVO aggVO = (ExAggPayDetailVO) billVO;
		PayDetailVO pdVO = (PayDetailVO) aggVO.getParentVO();
		EntrustVO entVO = dao.queryByCondition(EntrustVO.class, new String[] { "pk_entrust" }, "vbillno=?",
				pdVO.getEntrust_vbillno());
		if(entVO != null){
			// 根据委托单号查询路线信息
			EntLineBVO[] lineVOs = dao.queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust=?",
					entVO.getPk_entrust());
			aggVO.setTableVO("ts_ent_line_b", lineVOs);
			// 根据委托单号查询运力信息
			EntTransbilityBVO[] tbVOs = dao.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust=?",
					entVO.getPk_entrust());
			aggVO.setTableVO("ts_ent_transbility_b", tbVOs);
			//yaojiie 2016 1 15 增加异常事故
			ExpAccidentVO[] accidentVOs =  dao.queryForSuperVOArrayByCondition(ExpAccidentVO.class, "entrust_vbillno=?",
					pdVO.getEntrust_vbillno());
			((IExAggVO) billVO).setTableVO(TabcodeConst.TS_EXP_ACCIDENT, accidentVOs);
		}
		return billVO;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String parentCond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(parentCond.indexOf("ts_pay_detail.pk_corp") < 0){
			return parentCond;
		}
		String custCond = " or "+ CorpHelper.getCurrentCorpWithChildren().replaceAll("pk_corp", "ts_pay_detail.billing_corp");
		parentCond = parentCond.substring(0, parentCond.length()-1) + custCond + ")";
		return parentCond;
	}
	
	public PayDetailVO[] getByEntrustBillno(String entrustBillno) {
		return dao.queryForSuperVOArrayByCondition(PayDetailVO.class, "entrust_vbillno=?", entrustBillno);
	}

	protected Integer getConfirmStatus() {
		return BillStatus.PD_CONFIRM;
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object type = parentVO.getAttributeValue("pay_type");
		Object vbillstatus = parentVO.getAttributeValue("vbillstatus");
		if(type != null
				&& (Integer.parseInt(type.toString()) == PayDetailConst.ORIGIN_TYPE || Integer
						.parseInt(type.toString()) == PayDetailConst.CARR_CLAIMANT_TYPE)) {
			throw new BusiException("不能删除单据类型为[原始单据、承运商索赔]的单据！");
		}
		if(vbillstatus != null && Integer.parseInt(vbillstatus.toString()) != BillStatus.NEW) {
			throw new BusiException("只能删除单据状态为[新建]的单据！");
		}
	}

	@SuppressWarnings("unchecked")
	public PaginationVO loadPayRecord(String pk_pay_detail, ParamVO paramVO, int offset, int pageSize) {
		PaginationVO paginationVO = dao.queryByConditionWithPaging(PayRecordVO.class, offset, pageSize, "relationid=?",
				pk_pay_detail);
		List<Map<String, Object>> list = execFormula4Templet(paramVO, paginationVO.getItems());
		paginationVO.setItems(list);
		return paginationVO;
	}

	/**
	 * 同步更新已收金额和未收金额
	 * 
	 * @param pdVO
	 */
	private void syncExpAccident(PayDetailVO pdVO) {
		// 2013-8-20，如果是承运商索赔的单据，那么需要同步更新异常事故中的相应的索赔金额，未索赔金额
		if(pdVO.getPay_type().intValue() == PayDetailConst.CARR_CLAIMANT_TYPE) {
			// 找到相应的异常事故的记录
			if(StringUtils.isNotBlank(pdVO.getRelationid())) {
				ExpAccidentVO eaVO = NWDao.getInstance().queryByCondition(ExpAccidentVO.class, "pk_exp_accident=?",
						pdVO.getRelationid());
				if(eaVO != null) {
					eaVO.setCarr_unclaimant_amount(new UFDouble(Math.abs(pdVO.getUngot_amount().doubleValue())));
					eaVO.setCarr_claimanted_amount(new UFDouble(Math.abs(pdVO.getGot_amount().doubleValue())));
					eaVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(eaVO);
				}
			}
		}
	}

	public Map<String, Object> doPayable(ParamVO paramVO, String json) {
		logger.info("执行付款动作...");
		JsonNode header = JacksonUtils.readTree(json);
		PayRecordVO prVO = (PayRecordVO) JacksonUtils.readValue(header, PayRecordVO.class);
		// 更新主表
		PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", prVO.getRelationid());
		if(pdVO == null) {
			throw new BusiException("应付明细已经被删除！");
		}
		if(pdVO.getVbillstatus().intValue() != BillStatus.PD_CONFIRM
				&& pdVO.getVbillstatus().intValue() != BillStatus.PD_PART_CAVLOAN) {
			throw new BusiException("应付明细必须是确认和部分核销状态才能执行付款！");
		}
		// 付款金额
		UFDouble pay_amount = prVO.getPay_amount() == null ? UFDouble.ZERO_DBL : prVO.getPay_amount();
		// 检查付款金额是否大于总金额
		UFDouble cost_amount = pdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : pdVO.getCost_amount();
		// xuqc 2013-11-3如果是承运商索赔，此时的应收金额是负数，收款金额也应该是负数
		if(pdVO.getPay_type().intValue() == PayDetailConst.CARR_CLAIMANT_TYPE) {
			if(pay_amount.doubleValue() > 0) {
				throw new BusiException("承运商索赔时，付款金额不能大于0！");
			}
			if(pay_amount.doubleValue() < cost_amount.doubleValue()) {
				throw new BusiException("承运商索赔时，付款金额不能小于总金额！");
			}
		} else {
			if(pay_amount.doubleValue() < 0) {
				throw new BusiException("付款金额不能小于0！");
			}
			if(pay_amount.doubleValue() > cost_amount.doubleValue()) {
				throw new BusiException("付款金额不能大于总金额！");
			}
		}

		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		// 设置已付金额
		UFDouble got_amount = pdVO.getGot_amount() == null ? UFDouble.ZERO_DBL : pdVO.getGot_amount();
		pdVO.setGot_amount(got_amount.add(pay_amount));
		// 设置未付金额
		pdVO.setUngot_amount(cost_amount.sub(pdVO.getGot_amount()));
		if(pdVO.getUngot_amount().doubleValue() == 0) {
			pdVO.setVbillstatus(BillStatus.PD_CAVLOAN);// 如果所有款项已经付完，则状态置为已核销
		} else {
			pdVO.setVbillstatus(BillStatus.PD_PART_CAVLOAN);// 部分核销
		}

		// 更新应收对账的发票号和发票抬头
		String[] checkArr = CMUtils.getUpdateCheck(pdVO.getCheck_no(), prVO.getCheck_no(), pdVO.getCheck_head(),
				prVO.getCheck_head());
		pdVO.setCheck_no(checkArr[0]);
		pdVO.setCheck_head(checkArr[1]);

		pdVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(pdVO);

		prVO.setPay_type(PayDetailConst.PAYABLE_TYPE.DIRECT.intValue());// 直接收款
		prVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		prVO.setCreate_time(new UFDateTime(new Date()));
		if(prVO.getDbilldate() == null) {
			prVO.setDbilldate(new UFDate());
		}
		prVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(prVO);
		toBeUpdate.add(prVO);
		dao.saveOrUpdate(toBeUpdate);

		syncExpAccident(pdVO);

		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(pdVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}

	public List<Map<String, Object>> doPayableAll(ParamVO paramVO, String[] pk_pay_detail) {
		logger.info("执行全部付款动作，pk_pay_detail：" + pk_pay_detail);
		if(pk_pay_detail == null || pk_pay_detail.length == 0) {
			return null;
		}
		List<SuperVO> list = new ArrayList<SuperVO>();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(String pk : pk_pay_detail) {
			PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", pk);
			if(pdVO == null) {
				throw new BusiException("应付明细已经被删除！");
			}
			if(pdVO.getVbillstatus().intValue() != BillStatus.PD_CONFIRM
					&& pdVO.getVbillstatus().intValue() != BillStatus.PD_PART_CAVLOAN) {
				throw new BusiException("应付明细必须是[确认、部分核销]状态才能执行全额付款！");
			}

			// 插入付款记录
			PayRecordVO prVO = new PayRecordVO();
			prVO.setDbilldate(new UFDate());
			prVO.setVbillno(pdVO.getVbillno());
			prVO.setPay_type(0);// 直接收款
			prVO.setRelationid(pdVO.getPk_pay_detail());
			prVO.setPay_amount(pdVO.getUngot_amount());// 这里不能等于总金额，可能是部分核销的单据进行全额收款
			prVO.setPay_date(new UFDate(new Date()));
			prVO.setPay_man(WebUtils.getLoginInfo().getPk_user());
			prVO.setPay_method(0);// 默认现金付款
			prVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			prVO.setCreate_time(new UFDateTime(new Date()));
			prVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(prVO);
			toBeUpdate.add(prVO);

			// 设置未收款金额
			pdVO.setUngot_amount(UFDouble.ZERO_DBL);
			// 设置已收款金额
			pdVO.setGot_amount(pdVO.getCost_amount());
			pdVO.setVbillstatus(BillStatus.PD_CAVLOAN);// 已核销
			pdVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(pdVO);
			list.add(pdVO);

			syncExpAccident(pdVO);
		}
		dao.saveOrUpdate(toBeUpdate);

		// 执行公式后返回
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO, list);
		list.clear();
		return mapList;
	}

	public Map<String, Object> deletePayRecord(ParamVO paramVO, String pk_pay_record) {
		logger.info("删除付款纪录，pk_pay_record：" + pk_pay_record);
		if(StringUtils.isBlank(pk_pay_record)) {
			throw new BusiException("请先选择付款纪录！");
		}
		PayRecordVO prVO = dao.queryByCondition(PayRecordVO.class, "pk_pay_record=?", pk_pay_record);
		if(prVO == null) {
			logger.error("该付款纪录已经被删除,pk_pay_record:" + pk_pay_record);
			throw new BusiException("该付款纪录已经被删除！");
		}
		if(prVO.getPay_type().intValue() == PayDetailConst.PAYABLE_TYPE.CHECKSHEET.intValue()) {
			logger.error("不能删除付款类型是对账收款的记录,pk_pay_record:" + pk_pay_record);
			throw new BusiException("不能删除付款类型是对账收款的记录！");
		}
		PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", prVO.getRelationid());
		if(pdVO == null) {
			throw new BusiException("该付款纪录所对应的应付明细已经被删除！");
		}
		UFDouble pay_amount = prVO.getPay_amount() == null ? UFDouble.ZERO_DBL : prVO.getPay_amount();
		UFDouble ungot_amount = pdVO.getUngot_amount() == null ? UFDouble.ZERO_DBL : pdVO.getUngot_amount();
		pdVO.setUngot_amount(ungot_amount.add(pay_amount));// 加入到未付款额中
		UFDouble got_amount = pdVO.getGot_amount() == null ? UFDouble.ZERO_DBL : pdVO.getGot_amount();
		pdVO.setGot_amount(got_amount.sub(pay_amount));// 已付款额必须减去该记录的付款额
		UFDouble cost_amount = pdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : pdVO.getCost_amount();
		if(pdVO.getUngot_amount().doubleValue() == cost_amount.doubleValue()) {
			// 如果未收款额和总金额相等，说明该记录完全没有收款。那么将该记录的状态置为已确认
			pdVO.setVbillstatus(BillStatus.PD_CONFIRM);
		} else {
			pdVO.setVbillstatus(BillStatus.PD_PART_CAVLOAN);
		}
		pdVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(pdVO);
		// 删除收款纪录
		dao.delete(prVO);
		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(pdVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}

	public List<Map<String, Object>> buildPayCheckSheet(ParamVO paramVO, String json) {
		logger.info("生成对账单...");
		if(StringUtils.isBlank(json)) {
			throw new BusiException("生成对账单时参数不能为空！");
		}
		List<SuperVO> list = new ArrayList<SuperVO>();
		JsonNode header = JacksonUtils.readTree(json);
		if(header.size() > 0) {
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			String checkType = header.get(0).get("check_type").getValueAsText();
			PayCheckSheetVO parentVO = new PayCheckSheetVO();
			if(StringUtils.isNotBlank(checkType) && !checkType.equals("null")){
				String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b WITH(NOLOCK) "
						+ "LEFT JOIN nw_data_dict WITH(NOLOCK) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
						+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  "
						+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
				if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
					sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b "
							+ "LEFT JOIN nw_data_dict ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
							+ "WHERE nvl(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  "
							+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
				}
				String value = NWDao.getInstance().queryForObject(sql, String.class,checkType,checkType);
				if(!checkType.equals(value)){
					JsonNode newHeader = JacksonUtils.readTree(json.replace(checkType, value)).get(0);
					parentVO = (PayCheckSheetVO) JacksonUtils.readValue(newHeader, PayCheckSheetVO.class);
				}else{
					parentVO = (PayCheckSheetVO) JacksonUtils.readValue(header.get(0), PayCheckSheetVO.class);
				}
			}else{
				parentVO = (PayCheckSheetVO) JacksonUtils.readValue(header.get(0), PayCheckSheetVO.class);
			}
			if(parentVO.getDbilldate() == null) {
				parentVO.setDbilldate(new UFDate());
			}
//			parentVO.setCheck_head(carrService.getCheckHead(parentVO.getPk_carrier()));
			parentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			parentVO.setCreate_time(new UFDateTime(new Date()));
			parentVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			ParamVO newParamVO = new ParamVO();
			newParamVO.setFunCode(FunConst.PAY_CHECK_SHEET_CODE);
			parentVO.setVbillno(null);
			//yaojiie 重新获得单据类型的方法，解决YSDZ和YFDZ显示错误问题因为this.getBillType 总是获得当前IMPL的单据类型问题
			this.setCodeFieldRorBuildPayCheckSheet(parentVO, newParamVO);// 单据号必须重新生成
			parentVO.setVbillstatus(BillStatus.NEW);// 对账单处于新建状态
			parentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(parentVO);
			toBeUpdate.add(parentVO);
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			List<String> pk_pay_details = new ArrayList<String>();
			for(int i = 0; i < header.size(); i++) {
				JsonNode obj = header.get(i);
				PayCheckSheetBVO childVO = (PayCheckSheetBVO) JacksonUtils.readValue(obj, PayCheckSheetBVO.class);
				childVO.setPk_pay_check_sheet(parentVO.getPrimaryKey());

				childVO.setStatus(VOStatus.NEW);
				this.addSuperVO(childVO);
				// 更改应付明细为已对账
				PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?",
						childVO.getPk_pay_detail());
				pk_pay_details.add(pdVO.getPk_pay_detail());
				if(pdVO.getVbillstatus().intValue() != BillStatus.PD_CONFIRM) {
					throw new BusiException("只有确认状态的应付明细才能生成对账单！");
				}
				// 汇总金额
				UFDouble child_cost_amount = pdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : pdVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP);
				cost_amount = cost_amount.add(child_cost_amount);
				pdVO.setVbillstatus(BillStatus.PD_CHECK);
				pdVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(pdVO);
				list.add(pdVO);
			}
			parentVO.setCost_amount(cost_amount.setScale(2, UFDouble.ROUND_HALF_UP));
			parentVO.setUngot_amount(cost_amount.setScale(2, UFDouble.ROUND_HALF_UP));
			dao.saveOrUpdate(toBeUpdate);
		}
		// 执行公式后返回
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO, list);
		list.clear();
		return mapList;
	}

	public List<Map<String, Object>> addToPayCheckSheet(ParamVO paramVO, String pk_pay_check_sheet,
			String[] pk_pay_detail) {
		logger.info("将应付明细添加到对账单中，pk_pay_check_sheet:" + pk_pay_check_sheet);
		if(pk_pay_detail == null || pk_pay_detail.length == 0 || pk_pay_check_sheet == null) {
			return null;
		}
		PayCheckSheetVO pcsVO = dao.queryByCondition(PayCheckSheetVO.class, "pk_pay_check_sheet=?", pk_pay_check_sheet);
		if(pcsVO == null) {
			return null;
		}
		List<SuperVO> list = new ArrayList<SuperVO>();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		UFDouble cost_amount = pcsVO.getCost_amount() == null ? UFDouble.ZERO_DBL : pcsVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP);
		UFDouble ungot_amount = pcsVO.getUngot_amount() == null ? UFDouble.ZERO_DBL : pcsVO.getUngot_amount().setScale(2, UFDouble.ROUND_HALF_UP);
		for(String pk : pk_pay_detail) {
			PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", pk);
			if(pdVO == null) {
				continue;
			}
			if(pdVO.getVbillstatus().intValue() != BillStatus.PD_CONFIRM) {
				throw new BusiException("只有确认状态的应付明细才能加入对账单！");
			}
			// 加入应付对账明细表
			PayCheckSheetBVO childVO = new PayCheckSheetBVO();
			childVO.setPk_pay_check_sheet(pcsVO.getPk_pay_check_sheet());
			childVO.setPk_pay_detail(pdVO.getPk_pay_detail());
			childVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(childVO);
			toBeUpdate.add(childVO);

			// 更新应付明细主表的总金额
			cost_amount = cost_amount.add(pdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : pdVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP));
			ungot_amount = ungot_amount.add(pdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : pdVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP));

			// 修改应付明细为已对账
			pdVO.setVbillstatus(BillStatus.PD_CHECK);
			pdVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(pdVO);
			list.add(pdVO);
		}
		pcsVO.setCost_amount(cost_amount.setScale(2, UFDouble.ROUND_HALF_UP));
		pcsVO.setUngot_amount(ungot_amount.setScale(2, UFDouble.ROUND_HALF_UP));
		pcsVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(pcsVO);
		dao.saveOrUpdate(toBeUpdate);
		// 执行公式后返回
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO, list);
		list.clear();
		return mapList;
	}

	public void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		if(StringUtils.isBlank(paramVO.getTemplateID())){
			String templateID = getBillTemplateID(paramVO);
			paramVO.setTemplateID(templateID);
		}
		if(StringUtils.isBlank(paramVO.getHeaderTabCode())){
			paramVO.setHeaderTabCode(TabcodeConst.TS_PAY_DETAIL);
		} 
		super.processBeforeSave(billVO, paramVO);
		PayDetailVO pdVO = (PayDetailVO) billVO.getParentVO();
		ExAggPayDetailVO aggVO = (ExAggPayDetailVO) billVO;
		// 明细费用
		PayDetailBVO[] detailVOs = (PayDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);

		// 重新计算总金额，未付金额
		List<PayDetailBVO> pdBVOs = new ArrayList<PayDetailBVO>();
		if(detailVOs != null && detailVOs.length > 0){
			for(PayDetailBVO pdBVO : detailVOs) {
				pdBVOs.add(pdBVO);
			}
		}
		CMUtils.processExtenal(pdVO, pdBVOs);
		//如果这个单据绑定了委托单，则取用委托单的公司，否则是登录用户的公司
		if(StringUtils.isBlank(pdVO.getEntrust_vbillno())
				&& StringUtils.isBlank(pdVO.getLot())) {
			// 没有委托单，不需要处理系统分摊费用
			pdVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			if(pdVO.getAccount_period() == null){
				pdVO.setAccount_period(pdVO.getCreate_time() == null ? new UFDateTime(new Date()) : pdVO.getCreate_time());
			}
			return;
		}
		if(StringUtils.isNotBlank(pdVO.getEntrust_vbillno())){
			EntrustVO entVO = dao.queryByCondition(EntrustVO.class, "vbillno=?", pdVO.getEntrust_vbillno());
			List<PayDeviBVO> deviBVOs = payDeviBVOCreation(entVO, aggVO);
			// 重新设置分摊记录
			aggVO.setTableVO(TabcodeConst.TS_PAY_DEVI_B, deviBVOs.toArray(new PayDeviBVO[deviBVOs.size()]));
		}else if(StringUtils.isNotBlank(pdVO.getLot())){
			EntrustVO[] entVOs = dao.queryForSuperVOArrayByCondition(EntrustVO.class, "lot=?", pdVO.getLot());
			//先将这个批次的费用分摊到每个委托单下
			if(entVOs == null || entVOs.length == 0){
				throw new BusiException("应付明细对应的委托单已经被删除！");
			}
			//统计计费重
			UFDouble fee_weight_count = UFDouble.ZERO_DBL;
			for(EntrustVO entVO : entVOs){
				fee_weight_count = fee_weight_count.add(entVO.getFee_weight_count());
			}
			if(fee_weight_count.doubleValue() == UFDouble.ZERO_DBL.doubleValue()){
				PayDetailVO pdVOTemp = (PayDetailVO) pdVO.clone();
				//平均分摊
				pdVO.setCost_amount(pdVO.getCost_amount().multiply(1/entVOs.length));
				//每一个明细的金额都要分摊一下
				PayDetailBVO[] pdBVOsTemp = new PayDetailBVO[pdBVOs.size()];
				int i = 0;
				for(PayDetailBVO pdBVO : pdBVOs){
					pdBVOsTemp[i] = (PayDetailBVO) pdBVO.clone();
					i++;
					pdBVO.setAmount(pdBVO.getAmount().multiply(1/entVOs.length));
				}
				List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
				for(EntrustVO entVO : entVOs){
					aggVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, pdBVOs.toArray(new PayDetailBVO[pdBVOs.size()]));
					deviBVOs.addAll(payDeviBVOCreation(entVO, aggVO));
				}
				//将billvo的信息改回去。
				aggVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, pdBVOsTemp);
				aggVO.setTableVO(TabcodeConst.TS_PAY_DEVI_B, deviBVOs.toArray(new PayDeviBVO[deviBVOs.size()]));
				pdVOTemp.setPk_corp(entVOs[0].getPk_corp());
				aggVO.setParentVO(pdVOTemp);
			}else{
				//按计费重分摊
				List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
				for(EntrustVO entVO : entVOs){
					PayDetailVO pdVOTemp = (PayDetailVO) pdVO.clone();
					pdVOTemp.setCost_amount(pdVO.getCost_amount().multiply((entVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : entVO.getFee_weight_count()).div(fee_weight_count)));
					//每一个明细的金额都要分摊一下
					List<PayDetailBVO> pdBVOsTemp = new ArrayList<PayDetailBVO>();
					for(PayDetailBVO pdBVO : pdBVOs){
						PayDetailBVO pdBVOTemp = (PayDetailBVO) pdBVO.clone();
						pdBVOTemp.setAmount(pdBVO.getAmount().multiply((entVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : entVO.getFee_weight_count()).div(fee_weight_count)));
					}
					aggVO.setParentVO(pdVOTemp);
					aggVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, pdBVOsTemp.toArray(new PayDetailBVO[pdBVOsTemp.size()]));
					deviBVOs.addAll(payDeviBVOCreation(entVO, aggVO));
				}
				//将billvo的信息改回去。
				aggVO.setParentVO(pdVO);
				aggVO.setTableVO(TabcodeConst.TS_PAY_DEVI_B, deviBVOs.toArray(new PayDeviBVO[deviBVOs.size()]));
				aggVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, pdBVOs.toArray(new PayDetailBVO[pdBVOs.size()]));
			}
			
		}
		if(StringUtils.isBlank(pdVO.getPk_corp())){
			pdVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		}
		if(pdVO.getAccount_period() == null){
			pdVO.setAccount_period(pdVO.getCreate_time() == null ? new UFDateTime(new Date()) : pdVO.getCreate_time());
		}
	}
	
	
	
	
	protected List<PayDeviBVO> payDeviBVOCreation(EntrustVO entVO,ExAggPayDetailVO aggVO){
		PayDetailVO pdVO = (PayDetailVO) aggVO.getParentVO();
		// 明细费用
		PayDetailBVO[] detailVOs = (PayDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
		if(entVO == null) {
			logger.error("应付明细对应的委托单已经被删除,entrust_vbillno:" + pdVO.getVbillno());
			throw new BusiException("应付明细对应的委托单已经被删除！");
		}
		//如果这个单据绑定了委托单，则取用委托单的公司，否则是登录用户的公司
		pdVO.setPk_corp(entVO.getPk_corp());
		pdVO.setLot(entVO.getLot());
		EntSegBVO[] entSegBVOs = dao.queryForSuperVOArrayByCondition(EntSegBVO.class, "pk_entrust=?",
				entVO.getPk_entrust());
		if(entSegBVOs == null || entSegBVOs.length == 0) {
			logger.error("委托单没有关联的运段，pk_entrust：" + entVO.getPk_entrust());
			throw new BusiException("委托单没有关联的运段！");
		}
		// 得到所有运段
		String segCond = getSegmentCond(entSegBVOs);
		SegmentVO[] segVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "pk_segment in " + segCond);
		// 统计<发货单号，计费重>,<发货单号，体积>
		Map<String, UFDouble> fee_weight_countMap = new HashMap<String, UFDouble>();
		Map<String, UFDouble> volume_countMap = new HashMap<String, UFDouble>();
		for(SegmentVO segVO : segVOs) {
			String invoice_vbillno = segVO.getInvoice_vbillno();
			UFDouble fee_weight_count = fee_weight_countMap.get(invoice_vbillno);
			if(fee_weight_count == null) {
				fee_weight_countMap.put(invoice_vbillno, segVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL
						: segVO.getFee_weight_count());
			} else {
				fee_weight_countMap.put(invoice_vbillno,
						fee_weight_count.add(segVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : segVO
								.getFee_weight_count()));
			}
			UFDouble volume_count = volume_countMap.get(invoice_vbillno);
			if(volume_count == null) {
				volume_countMap.put(invoice_vbillno,
						segVO.getVolume_count() == null ? UFDouble.ZERO_DBL : segVO.getVolume_count());
			} else {
				volume_countMap
						.put(invoice_vbillno, volume_count.add(segVO.getVolume_count() == null ? UFDouble.ZERO_DBL
								: segVO.getVolume_count()));
			}
		}
		// 分摊明细费用
		boolean reComputeDeviAmount = true;
		// 2015-03-02 界面的总金额和数据库的总金额进行比较，没有改变，那么不需要重新计算分摊费用，这里有bug，目前先这么解决了。
		if(StringUtils.isNotBlank(pdVO.getPk_pay_detail())) {
			double cost_amount = pdVO.getCost_amount() == null ? 0 : pdVO.getCost_amount().doubleValue();
			// 读取当前数据库中存储的总金额
			PayDetailVO oriPdVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "pk_pay_detail=?",
					pdVO.getPk_pay_detail());
			if(oriPdVO != null) {// 有可能记录已经不存在
				double ori_cost_amount = oriPdVO.getCost_amount() == null ? 0 : oriPdVO.getCost_amount().doubleValue();
				if(cost_amount != 0 && ori_cost_amount != 0 && cost_amount == ori_cost_amount) {
					// 不需要重新计算分摊金额了
					reComputeDeviAmount = false;
				}
			}
		}
		List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
		if(reComputeDeviAmount) {
			deviBVOs = PZUtils.getPayDeviBVOs2(entVO, fee_weight_countMap, volume_countMap,detailVOs);
			// 合并到待更新的记录中,考虑到情况是，分摊记录修改了金额，那么会出现在billVO中，状态是更新状态，那么此时需要将该条记录删掉，因为会重新分摊
			// 界面上更新的记录
			PayDeviBVO[] oriDeviVOs = (PayDeviBVO[]) aggVO.getTableVO(TabcodeConst.TS_PAY_DEVI_B);
			if(oriDeviVOs != null && oriDeviVOs.length > 0) {
				for(PayDeviBVO oriDeviVO : oriDeviVOs) {
					oriDeviVO.setStatus(VOStatus.DELETED);
					deviBVOs.add(oriDeviVO);
				}
			}
		}
		return deviBVOs;
	}

	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		if(StringUtils.isBlank(paramVO.getTemplateID())){
			String templateID = getBillTemplateID(paramVO);
			paramVO.setTemplateID(templateID);
		}
		if(StringUtils.isBlank(paramVO.getHeaderTabCode())){
			paramVO.setHeaderTabCode(TabcodeConst.TS_PAY_DETAIL);
		}
		super.processAfterSave(billVO, paramVO);
		
		PayDetailVO payDetailVO = (PayDetailVO)billVO.getParentVO();
		
		String entVbillno = payDetailVO.getEntrust_vbillno();
		if(StringUtils.isBlank(entVbillno)){
			return;
		}
		
		String sql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH (NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
				+ " LEFT JOIN ts_entrust ent WITH (NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
				+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
				+ " AND ent.vbillno =? " ;
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, entVbillno);
		CMUtils.totalCostComput(invoiceVOs);
	}
	
	/**
	 * 返回运段的查询条件片段，为了查询多个运段的货品信息
	 * 
	 * @param segVOs
	 * @return
	 */
	public String getSegmentCond(EntSegBVO[] entSegBVOs) {
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

	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, String orderBy) {
		//yaojiie 2015 12 11 修改执行公式后方法，将大量原本用显示公式执行的显示方法，使用SQL查询，加快查询和导出速度
		super.processAfterExecFormula(list, paramVO, orderBy);
		Set<String> pk_customers = new HashSet<String>();
		Set<String> entrust_vbillnos = new HashSet<String>();
		Set<String> pk_entrusts = new HashSet<String>();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> transbilityResults = new ArrayList<Map<String, Object>>();
		CustomerVO[] customerVOs = null;
		for (Map<String, Object> map : list) {
			Object entrust_vbillno = map.get("entrust_vbillno");
			if (entrust_vbillno != null) {
				entrust_vbillnos.add(entrust_vbillno.toString());
			}
		}
		//查询结果集
		if (entrust_vbillnos != null && entrust_vbillnos.size() > 0) {
			String cond = NWUtils.buildConditionString(entrust_vbillnos.toArray(new String[entrust_vbillnos.size()]));
			String sql = "SELECT ent.pk_entrust,ent.vbillno as entrust_vbillno,ent.invoice_vbillno,ent.cust_orderno,ent.orderno,ent.lot,"
					+ "ent.req_deli_date,ent.req_arri_date,ent.act_deli_date,ent.act_arri_date,ent.est_arri_time,"
					+ "ent.pk_delivery,deli_addr.addr_name AS deli_addr_name,deli_area.name AS deli_city_name,ent.deli_province,ent.deli_area,ent.deli_detail_addr,ent.deli_contact,ent.deli_mobile,ent.deli_phone,ent.deli_email, "
					+ "ent.pk_arrival,arri_addr.addr_name AS arri_addr_name,arri_area.name AS arri_city_name,ent.arri_province,ent.arri_area,ent.arri_detail_addr,ent.arri_contact,ent.arri_mobile,ent.arri_phone,ent.arri_email, "
					+ "ent.vbillstatus AS entrust_vbillstatus,ent.memo AS entrust_memo,ent.pk_customer,tp.name AS trans_type_name,ent.mainno,ent.flightno,ent.flight_time,ent.tracking_status,ent.tracking_memo,ent.distance "
					+ "FROM ts_entrust ent WITH(NOLOCK) "
					+ "LEFT JOIN ts_trans_type tp WITH(NOLOCK) ON ent.pk_trans_type = tp.pk_trans_type AND isnull(tp.locked_flag,'N')='N' and isnull(tp.dr,0)=0 "
					+ "LEFT JOIN ts_address deli_addr WITH(NOLOCK) ON ent.pk_delivery = deli_addr.pk_address AND  isnull(deli_addr.locked_flag,'N')='N' and isnull(deli_addr.dr,0)=0 "
					+ "LEFT JOIN ts_address arri_addr WITH(NOLOCK) ON ent.pk_arrival = arri_addr.pk_address AND  isnull(arri_addr.locked_flag,'N')='N' and isnull(arri_addr.dr,0)=0 "
					+ "LEFT JOIN ts_area deli_area WITH(NOLOCK) ON ent.deli_city = deli_area.pk_area AND isnull(deli_area.locked_flag,'N')='N' and isnull(deli_area.dr,0)=0 "
					+ "LEFT JOIN ts_area arri_area WITH(NOLOCK) ON ent.arri_city = arri_area.pk_area AND isnull(arri_area.locked_flag,'N')='N' and isnull(arri_area.dr,0)=0 "
					+ "WHERE  isnull(ent.dr,0)=0 and ent.vbillno in " + cond;
			if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
				sql = "SELECT ent.pk_entrust,ent.vbillno as entrust_vbillno,ent.invoice_vbillno,ent.cust_orderno,ent.orderno,ent.lot,"
						+ "ent.req_deli_date,ent.req_arri_date,ent.act_deli_date,ent.act_arri_date,ent.est_arri_time,"
						+ "ent.pk_delivery,deli_addr.addr_name AS deli_addr_name,deli_area.name AS deli_city_name,ent.deli_province,ent.deli_area,ent.deli_detail_addr,ent.deli_contact,ent.deli_mobile,ent.deli_phone,ent.deli_email, "
						+ "ent.pk_arrival,arri_addr.addr_name AS arri_addr_name,arri_area.name AS arri_city_name,ent.arri_province,ent.arri_area,ent.arri_detail_addr,ent.arri_contact,ent.arri_mobile,ent.arri_phone,ent.arri_email, "
						+ "ent.vbillstatus AS entrust_vbillstatus,ent.memo AS entrust_memo,ent.pk_customer,tp.name AS trans_type_name,ent.mainno,ent.flightno,ent.flight_time,ent.tracking_status,ent.tracking_memo,ent.distance "
						+ "FROM ts_entrust ent  "
						+ "LEFT JOIN ts_trans_type tp ON ent.pk_trans_type = tp.pk_trans_type AND isnull(tp.locked_flag,'N')='N' and isnull(tp.dr,0)=0 "
						+ "LEFT JOIN ts_address deli_addr ON ent.pk_delivery = deli_addr.pk_address AND  isnull(deli_addr.locked_flag,'N')='N' and isnull(deli_addr.dr,0)=0 "
						+ "LEFT JOIN ts_address arri_addr ON ent.pk_arrival = arri_addr.pk_address AND  isnull(arri_addr.locked_flag,'N')='N' and isnull(arri_addr.dr,0)=0 "
						+ "LEFT JOIN ts_area deli_area ON ent.deli_city = deli_area.pk_area AND isnull(deli_area.locked_flag,'N')='N' and isnull(deli_area.dr,0)=0 "
						+ "LEFT JOIN ts_area arri_area ON ent.arri_city = arri_area.pk_area AND isnull(arri_area.locked_flag,'N')='N' and isnull(arri_area.dr,0)=0 "
						+ "WHERE  isnull(ent.dr,0)=0 and ent.vbillno in " + cond;
			}
			results = NWDao.getInstance().queryForList(sql);

		}	
		
		for (Map<String, Object> result : results) {
			Object pk_customer = result.get("pk_customer");
			if (pk_customer != null) {
				String[] pks = pk_customer.toString().split("\\" + Constants.SPLIT_CHAR);
				pk_customers.addAll(Arrays.asList(pks));
			}
		}
		
		if (pk_customers != null && pk_customers.size() > 0) {
			customerVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CustomerVO.class, " pk_customer in "
					+ NWUtils.buildConditionString(pk_customers.toArray(new String[pk_customers.size()])));
		}
		
		for (Map<String, Object> result : results) {
			Object pk_entrust = result.get("pk_entrust");
			if (pk_entrust != null) {
				String[] pks = pk_entrust.toString().split("\\" + Constants.SPLIT_CHAR);
				pk_entrusts.addAll(Arrays.asList(pks));
			}
		}
		//从关联表中，获得车辆信息
		if (pk_entrusts != null && pk_entrusts.size() > 0) {
			String cond = NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()]));
			String sql = "SELECT teb.pk_entrust,teb.carno,teb.pk_driver,teb.container_no,teb.sealing_no,teb.forecast_deli_date,teb.pk_car_type,ts_car_type.name AS car_type_name,teb.num AS car_num,teb.gps_id "
						+ "FROM ts_ent_transbility_b AS teb WITH (NOLOCK) "
						+ "LEFT JOIN ts_car_type WITH (NOLOCK)  ON teb.pk_car_type = ts_car_type.pk_car_type AND isnull(ts_car_type.locked_flag,'N')='N' and isnull(ts_car_type.dr,0)=0 "
						+ "WHERE isnull(teb.dr,0)=0 and teb.pk_entrust in " + cond;

			transbilityResults = NWDao.getInstance().queryForList(sql);
		}

		//将结果集和原数据进行匹配，并放入原数据集中
		for (Map<String, Object> map : list) {
			for (Map<String, Object> result : results) {
				if (result.get("entrust_vbillno").equals(map.get("entrust_vbillno"))) {
					map.put("invoice_vbillno", result.get("invoice_vbillno"));
					map.put("cust_orderno", result.get("cust_orderno"));
					map.put("orderno", result.get("orderno"));
					map.put("lot", result.get("lot"));
					map.put("req_deli_date", result.get("req_deli_date"));
					map.put("req_arri_date", result.get("req_arri_date"));
					map.put("act_deli_date", result.get("act_deli_date"));
					map.put("act_arri_date", result.get("act_arri_date"));
					map.put("est_arri_time", result.get("est_arri_time"));
					map.put("pk_delivery", result.get("pk_delivery"));
					map.put("deli_addr_name", result.get("deli_addr_name"));
					map.put("deli_city_name", result.get("deli_city_name"));
					map.put("deli_area", result.get("deli_area"));
					map.put("deli_detail_addr", result.get("deli_detail_addr"));
					map.put("deli_contact", result.get("deli_contact"));
					map.put("deli_mobile", result.get("deli_mobile"));
					map.put("deli_phone", result.get("deli_phone"));
					map.put("deli_email", result.get("deli_email"));
					map.put("pk_arrival", result.get("pk_arrival"));
					map.put("arri_addr_name", result.get("arri_addr_name"));
					map.put("arri_city_name", result.get("arri_city_name"));
					map.put("arri_area", result.get("arri_area"));
					map.put("arri_detail_addr", result.get("arri_detail_addr"));
					map.put("arri_contact", result.get("arri_contact"));
					map.put("arri_mobile", result.get("arri_mobile"));
					map.put("arri_phone", result.get("arri_phone"));
					map.put("arri_email", result.get("arri_email"));
					map.put("entrust_vbillstatus", result.get("entrust_vbillstatus"));
					map.put("entrust_memo", result.get("entrust_memo"));
					map.put("trans_type_name", result.get("trans_type_name"));
					map.put("mainno", result.get("mainno"));
					map.put("flightno", result.get("flightno"));
					map.put("flight_time", result.get("flight_time"));
					map.put("tracking_status", result.get("tracking_status"));
					map.put("tracking_memo", result.get("tracking_memo"));
					map.put("distance", result.get("distance"));

					map.put("pk_customer", result.get("pk_customer"));
					//处理会有拼接情况的数据
					Object pk_customer = map.get("pk_customer");
					StringBuffer cust_name = new StringBuffer();
					if (pk_customer != null) {
						String[] pks = pk_customer.toString().split("\\" + Constants.SPLIT_CHAR);
						if (pks != null && pks.length > 0) {
							for (String pk : pks) {
								for (CustomerVO customerVO : customerVOs) {
									if (pk.equals(customerVO.getPk_customer())) {
										cust_name.append(customerVO.getCust_name()).append(Constants.SPLIT_CHAR);
									}
								}
								if (StringUtils.isNotBlank(cust_name.toString())) {
									String cust_name1 = cust_name.substring(0, cust_name.length() - 1);
									map.put("cust_name", cust_name1);
								}
							}
						}
					}

					map.put("pk_entrust", result.get("pk_entrust"));
					Object pk_entrust = map.get("pk_entrust");
					StringBuffer carno = new StringBuffer();
					StringBuffer car_type_name = new StringBuffer();
					StringBuffer pk_driver = new StringBuffer();
					StringBuffer container_no = new StringBuffer();
					StringBuffer sealing_no = new StringBuffer();
					StringBuffer forecast_deli_date = new StringBuffer();
					StringBuffer car_num = new StringBuffer();
					StringBuffer gps_id = new StringBuffer();

					if (pk_entrust != null) {
						if (transbilityResults != null && transbilityResults.size() > 0) {
							for (Map<String, Object> transbilityResult : transbilityResults) {
								// carno
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("carno")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("carno").toString())) {
									carno.append(transbilityResult.get("carno")).append(Constants.SPLIT_CHAR);
								}
								// carType_name
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("car_type_name")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("car_type_name").toString())) {
									car_type_name.append(transbilityResult.get("car_type_name"))
											.append(Constants.SPLIT_CHAR);
								}
								// driver
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("pk_driver")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("pk_driver").toString())) {
									pk_driver.append(transbilityResult.get("pk_driver")).append(Constants.SPLIT_CHAR);
								}
								// container_no
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("container_no")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("container_no").toString())) {
									container_no.append(transbilityResult.get("container_no"))
											.append(Constants.SPLIT_CHAR);
								}
								// sealing_no
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("sealing_no")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("sealing_no").toString())) {
									sealing_no.append(transbilityResult.get("sealing_no")).append(Constants.SPLIT_CHAR);
								}
								// forecast_deli_date
								if (pk_entrust.equals(transbilityResult.get("pk_entrust")) &&transbilityResult.get("forecast_deli_date")!=null&& StringUtils
										.isNotBlank(transbilityResult.get("forecast_deli_date").toString())) {
									forecast_deli_date.append(transbilityResult.get("forecast_deli_date"))
											.append(Constants.SPLIT_CHAR);
								}
								// car_num
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("car_num")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("car_num").toString())) {
									car_num.append(transbilityResult.get("car_num")).append(Constants.SPLIT_CHAR);
								}
								// gps_id
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("gps_id")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("gps_id").toString())) {
									gps_id.append(transbilityResult.get("gps_id")).append(Constants.SPLIT_CHAR);
								}
							}

							if (StringUtils.isNotBlank(carno.toString())) {
								String container = carno.substring(0, carno.length() - 1);
								map.put("carno", container);
							}
							if (StringUtils.isNotBlank(car_type_name.toString())) {
								String container = car_type_name.substring(0, car_type_name.length() - 1);
								map.put("car_type_name", container);
							}
							if (StringUtils.isNotBlank(pk_driver.toString())) {
								String container = pk_driver.substring(0, pk_driver.length() - 1);
								map.put("pk_driver", container);
							}
							if (StringUtils.isNotBlank(container_no.toString())) {
								String container = container_no.substring(0, container_no.length() - 1);
								map.put("container_no", container);
							}

							if (StringUtils.isNotBlank(sealing_no.toString())) {
								String container = sealing_no.substring(0, sealing_no.length() - 1);
								map.put("sealing_no", container);
							}
							if (StringUtils.isNotBlank(forecast_deli_date.toString())) {
								String container = forecast_deli_date.substring(0, forecast_deli_date.length() - 1);
								map.put("forecast_deli_date", container);
							}
							if (StringUtils.isNotBlank(car_num.toString())) {
								String container = car_num.substring(0, car_num.length() - 1);
								map.put("car_num", container);
							}
							if (StringUtils.isNotBlank(gps_id.toString())) {
								String container = gps_id.substring(0, gps_id.length() - 1);
								map.put("gps_id", container);
							}
						}
					}
				}
			}
		}
	}

	public AggregatedValueObject confirm(ParamVO paramVO) {
		logger.info("执行单据确认动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), getConfirmStatus()); // 设置成已确认
		parentVO.setAttributeValue(getConfirmTimeField(), new UFDateTime(new Date()));
		parentVO.setAttributeValue(getConfirmUserField(), WebUtils.getLoginInfo().getPk_user());
		dao.saveOrUpdate(billVO);
		return billVO;
	}
	
	@Override
	public SuperVO[] batchConfirm(ParamVO paramVO, String[] ids) {
		logger.info("执行单据批量确认动作！");
		SuperVO[] parentVOs = getByPrimaryKeys(PayDetailVO.class, ids);
		processBeforeBatchConfirm(paramVO,parentVOs);
		
		String msg = payCheckProc(NWUtils.join(ids, ","), 0);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		
		for(SuperVO parentVO : parentVOs){
			parentVO.setStatus(VOStatus.UPDATED);
			parentVO.setAttributeValue(getBillStatusField(), getConfirmStatus()); // 设置成已确认
			parentVO.setAttributeValue(getConfirmTimeField(), new UFDateTime(new Date()));
			parentVO.setAttributeValue(getConfirmUserField(), WebUtils.getLoginInfo().getPk_user());
		}
		dao.saveOrUpdate(parentVOs);
		return parentVOs;
	}

	/**
	 * 提交后，单据属于确认中状态
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> commit(ParamVO paramVO) {
		logger.info("执行单据提交动作，主键：" + paramVO.getBillId());
		
		String msg = payCheckProc(paramVO.getBillId(), 2);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.NEW != billStatus) {
				throw new RuntimeException("只有[新建]状态的单据才能进行提交！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.PD_CONFIRMING); // 设置成确认中
		parentVO.setAttributeValue(getCommitTimeField(), new UFDateTime(new Date()));
		parentVO.setAttributeValue(getCommitUserField(), WebUtils.getLoginInfo().getPk_user());
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	/**
	 * 提交后，单据属于确认中状态
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> uncommit(ParamVO paramVO) {
		logger.info("执行单据反提交动作，主键：" + paramVO.getBillId());
		
		String msg = payCheckProc(paramVO.getBillId(), 3);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.PD_CONFIRMING != billStatus) {
				throw new RuntimeException("只有[确认中]状态的单据才能进行反提交！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.NEW); // 设置成新建
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	/**
	 * 重新计算合同金额，必须是新建，并且是原始单据类型
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> reComputeMny(AggregatedValueObject billVO, ParamVO paramVO) {
		if(billVO == null) {
			return null;
		}
		ExAggPayDetailVO pdAggVO = (ExAggPayDetailVO) billVO;
		PayDetailVO pdVO = (PayDetailVO) pdAggVO.getParentVO();
		if(pdVO.getVbillstatus() != BillStatus.NEW) {
			throw new BusiException("应付明细[?]必须是[新建]状态才能进行重算金额！",pdVO.getVbillno());
		}
		if(pdVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
			throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！",pdVO.getVbillno());
		}
		if(StringUtils.isBlank(pdVO.getEntrust_vbillno())) {
			throw new BusiException("应付明细[?]没有对应的委托单！",pdVO.getVbillno());
		}

		String sql = "select pk_entrust from ts_entrust where vbillno=?";
		String pk_entrust = NWDao.getInstance().queryForObject(sql, String.class, pdVO.getEntrust_vbillno());
		if(StringUtils.isBlank(pk_entrust)) {
			throw new BusiException("应付明细[?]对应的委托单[?]已经不存在！",pdVO.getVbillno(),pdVO.getEntrust_vbillno());
		}

		ParamVO paramVO1 = new ParamVO();
		paramVO1.setBillId(pk_entrust);

		ExAggEntrustVO entAggVO = (ExAggEntrustVO) entrustService.queryBillVO(paramVO1);
		EntrustVO entVO = (EntrustVO) entAggVO.getParentVO(); // 委托单VO
		

		// 根据委托单号查询应付明细VO，主要是要得到应付明细pk，设置到新增加的应付明细子表中
		List<SuperVO> updateList = new ArrayList<SuperVO>();
		// 保存费用明细
		PayDetailBVO[] pdBVOs = (PayDetailBVO[]) pdAggVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
		List<PayDetailBVO> oldDetailBVOs = new ArrayList<PayDetailBVO>();
		for(PayDetailBVO cvo : pdBVOs) {
			oldDetailBVOs.add(cvo);
		}
		// 匹配合同重新计算费用明细
		EntLineBVO[] lineVOs = (EntLineBVO[]) entAggVO.getTableVO(TabcodeConst.TS_ENT_LINE_B);// 节点数
		List<EntLineBVO> lineList = PZUtils.processLineInfo(lineVOs, true);
		// 重新设置数据
		lineVOs = lineList.toArray(new EntLineBVO[lineList.size()]);

		EntTransbilityBVO[] tbBVOs = (EntTransbilityBVO[]) entAggVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B);// 节点数

		oldDetailBVOs = EntrustUtils.getPayDetailBVOs(contractService, entVO, tbBVOs, lineVOs, oldDetailBVOs, pdVO);
		for(PayDetailBVO cvo : oldDetailBVOs) {
			if(cvo.getStatus() == VOStatus.NEW) {
				cvo.setPk_pay_detail(pdVO.getPk_pay_detail()); // 设置主表的主键
				NWDao.setUuidPrimaryKey(cvo);
			}
		}
		updateList.addAll(oldDetailBVOs);
	
		updateList.add(pdVO);

		// 重新计算分摊费用
		List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entVO, null, null,
				oldDetailBVOs.toArray(new PayDetailBVO[oldDetailBVOs.size()]));
		updateList.addAll(deviBVOs);
		dao.saveOrUpdate(updateList);
		
		// 更新总金额
		CMUtils.processExtenalforComputer(pdVO, oldDetailBVOs);
		//更新应收明细表头成本信息2016-7-4 XIA
		String invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
				+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
				+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
				+ " AND ent.vbillno =? ";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei ON inv.pk_invoice = ei.pk_invoice "
					+ " LEFT JOIN ts_entrust ent ON ent.pk_entrust = ei.pk_entrust "
					+ " WHERE nvl(ei.dr,0)=0 AND nvl(ent.dr,0)=0 AND nvl(inv.dr,0)=0 "
					+ " AND ent.vbillno =? ";
		}
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(invSql, InvoiceVO.class,entVO.getVbillno());
		CMUtils.totalCostComput(invoiceVOs);
		paramVO.setBillId(pdVO.getPk_pay_detail());
		return (Map<String, Object>) execFormula4Templet(queryBillVO(paramVO), paramVO).get("HEADER");
	}
	
	
	/**
	 * <ul>
	 * <li>0、所有的应付明细必须是新建状态，以及是原始类型的</li>
	 * <li>1、所有的委托单必须属于同一种运段类型</li>
	 * <li>2、所有的委托单都必须是已到货或者部分到货</li>
	 * </ul>3、增加整车应付按照批次计算逻辑2015-12-27 lanjian
	 */
	public void doPayDetailRebuildBySegtype(ParamVO paramVO, int seg_type, String[] billId) {
		if(billId == null || billId.length == 0) {
			return;
		}
		//通过委托单号，获取的对应的匹配合同VO对应的集合
		List<PayDetailMatchVO> matchVOs = new ArrayList<PayDetailMatchVO>();
		//根据界面传过来的主键单据号，返回应付明细对象列表
		PayDetailVO[] pdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(billId));
		if(pdVOs == null || pdVOs.length == 0) {
			return;
		}
		//获取应付明细对应的委托单号
		String[] entrustVbillnos = new String[pdVOs.length];
		for(int i=0;i<pdVOs.length;i++){
			if(pdVOs[i].getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应付明细[?]必须是[新建]状态才能重算费用！",pdVOs[i].getVbillno());
			}
			if(pdVOs[i].getPay_type() == null || pdVOs[i].getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始类型]才能重算费用！",pdVOs[i].getVbillno());
			}
			//获取委托单号
			entrustVbillnos[i] = pdVOs[i].getEntrust_vbillno();
			//生成PayDetailMatchVO
			PayDetailMatchVO matchVO = new PayDetailMatchVO();
			matchVO.setEntrust_villno(pdVOs[i].getEntrust_vbillno());
			matchVO.setPd_vbillno(pdVOs[i].getVbillno());
			matchVO.setPk_pay_detail(pdVOs[i].getPk_pay_detail());
			//现在不支持按照数量进行匹配，所以这里就就不加入了。
			//matchVO.setPack_num_count(pdVOs[i].getPack_num_count() == null ? 0 : pdVOs[i].getPack_num_count());
			matchVO.setNum_count(pdVOs[i].getNum_count() == null ? 0 : pdVOs[i].getNum_count());
			matchVO.setWeight_count(pdVOs[i].getWeight_count() == null ? UFDouble.ZERO_DBL : pdVOs[i].getWeight_count());
			matchVO.setFee_weight_count(pdVOs[i].getFee_weight_count() == null ? UFDouble.ZERO_DBL : pdVOs[i].getFee_weight_count());
			matchVO.setVolume_count(pdVOs[i].getVolume_count() == null ? UFDouble.ZERO_DBL : pdVOs[i].getVolume_count());
			matchVOs.add(matchVO);
			
		}
		
		//通过得到的委托单号获取EntrustVO
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				"vbillno in " + NWUtils.buildConditionString(entrustVbillnos));
		if(entrustVOs == null || entrustVOs.length == 0) {
			return;
		}
		
		//通过EntrustVO获取发货单的vbillno
		List<String> invoiceVbillnos = new ArrayList<String>();
		List<String> pk_entrusts = new ArrayList<String>();
		for(EntrustVO entrustVO : entrustVOs) {
			//先判断状态
			if(entrustVO.getVbillstatus().intValue() != BillStatus.ENT_DELIVERY
					&& entrustVO.getVbillstatus().intValue() != BillStatus.ENT_ARRIVAL
					&& seg_type != SegmentConst.SEG_TYPE_NORMAL
					) {
				throw new BusiException("委托单[?]必须是[已提货]、[已到货]状态才能重算费用！",entrustVO.getVbillno());
			}
			
			for(PayDetailMatchVO matchVO : matchVOs){
				if(matchVO.getEntrust_villno().equals(entrustVO.getVbillno())){
					matchVO.setEntrust_villno(entrustVO.getVbillno());
					matchVO.setPk_carrier(entrustVO.getPk_carrier());
					matchVO.setPk_entrust(entrustVO.getPk_entrust());
					matchVO.setPk_trans_type(entrustVO.getPk_trans_type());
					matchVO.setUrgent_level(entrustVO.getUrgent_level());
					matchVO.setItem_code(entrustVO.getItem_code());
					matchVO.setIf_return(entrustVO.getIf_return());
					matchVO.setPk_trans_line(entrustVO.getPk_trans_line());
					matchVO.setPk_delivery(entrustVO.getPk_delivery());
					matchVO.setDeli_city(entrustVO.getDeli_city());
					matchVO.setPk_arrival(entrustVO.getPk_arrival());
					matchVO.setArri_city(entrustVO.getArri_city());
					matchVO.setPk_corp(entrustVO.getPk_corp());
					matchVO.setAct_deli_date(entrustVO.getAct_deli_date());
					matchVO.setAct_arri_date(entrustVO.getAct_arri_date());
					matchVO.setReq_deli_date(entrustVO.getReq_deli_date());
					matchVO.setReq_arri_date(entrustVO.getReq_arri_date());
					matchVO.setLot(entrustVO.getLot());
					matchVO.setInvoice_villno(entrustVO.getInvoice_vbillno());
					//获取发货单号
					String[] billnos = entrustVO.getInvoice_vbillno().split("\\" + Constants.SPLIT_CHAR);
					invoiceVbillnos.addAll(Arrays.asList(billnos));
				}
			}
			pk_entrusts.add(entrustVO.getPk_entrust());
		}
		EntPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class,
				"pk_entrust in " + NWUtils.buildConditionString(pk_entrusts));
		//对packBVOs 按照 pk_entrust 分组
		Map<String,List<EntPackBVO>> groupMap = new HashMap<String,List<EntPackBVO>>();
		if(packBVOs != null && packBVOs.length > 0){
			for(EntPackBVO packBVO : packBVOs){
				String key = packBVO.getPk_entrust();
				List<EntPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<EntPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(packBVO);
			}
		}
		for(PayDetailMatchVO matchVO : matchVOs){
			List<EntPackBVO> voList = groupMap.get(matchVO.getPk_entrust());
			if(voList != null && voList.size() > 0){
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				Map<String,List<EntPackBVO>> packGroupMap = new  HashMap<String,List<EntPackBVO>>();
				
				for(EntPackBVO packBVO : voList){
					String key = packBVO.getPack();
					if(StringUtils.isBlank(key)){
						//没有包装的货品自动过滤
						continue;
					}
					List<EntPackBVO> packVOList = packGroupMap.get(key);
					if(packVOList == null){
						packVOList = new ArrayList<EntPackBVO>();
						packGroupMap.put(key, packVOList);
					}
					packVOList.add(packBVO);
				}
				
				if (packGroupMap.size() > 0) {
					for(String key : packGroupMap.keySet()){
						PackInfo packInfo = new PackInfo();
						List<EntPackBVO> packVOList = packGroupMap.get(key);
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(EntPackBVO packBVO : packVOList){
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
				matchVO.setPackInfos(packInfos);
			}
		}
		
		if (seg_type == SegmentConst.SEG_TYPE_NORMAL) {
			//lanjian 2015-12-27 不需要正常类型需要判断订单类型是否一致。
		} else {
			// 通过vbillno得到发货单列表
			InvoiceVO[] invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, "vbillno in "
					+ NWUtils.buildConditionString(invoiceVbillnos.toArray(new String[invoiceVbillnos.size()])));
			if (invVOs == null || invVOs.length == 0) {
				return;
			}
			// 判断发货单的类型，必须一致，要么都是提货段，都是干线段或是配送段
			String def1 = invVOs[0].getDef1();
			String[] ordernos = new String[invVOs.length];
			for(int i=0;i<invVOs.length;i++){
				ordernos[i] = invVOs[i].getOrderno();
				if((def1 == null && invVOs[i].getDef1() == null)
						|| def1.equals(invVOs[i].getDef1())){
					//通过
				}else{
					throw new BusiException("发货单的订单类型必须完全一样才能重算费用，比如全部是提货段、全部是干线段或全部是配送段！");//def1字段不同，认为不是同一运段，不通过。
				}
				
				// 从发货单获取DEF5，DEF5是订单号。2015-10-31Jonathan
				for (PayDetailMatchVO matchVO : matchVOs){
					//这里只处理发货单和委托单一对一的情况
					if (matchVO.getInvoice_villno().equals(invVOs[i].getVbillno())){
						matchVO.setDef5(invVOs[i].getDef5());
						matchVO.setDef7(invVOs[i].getDef7());
						// 增加客户订单号2015-11-6jonathan
						matchVO.setOrderno(invVOs[i].getOrderno());
					}
				}
			}
			// 从发货单列表得到 orderno列表，判断发货单是不是返箱类型
			OrderAssVO[] oaVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(OrderAssVO.class,
					"orderno in " + NWUtils.buildConditionString(ordernos));
			if (oaVOs == null || oaVOs.length == 0) {
				throw new BusiException("发货单辅助表的数据为空，不能匹配计算费用！");
			}
			//订单辅助表里的订单类型也要保持一直
			String orderType = oaVOs[0].getOrder_type();
			for (OrderAssVO oaVO : oaVOs) {
				if((orderType == null && oaVO.getOrder_type() == null)
						|| orderType.equals(oaVO.getOrder_type())){
					//通过
				}else{
					throw new BusiException("发货单的订单类型必须完全一样才能重算费用，比如全部是返箱！");//def1字段不同，认为不是同一运段，不通过。
				}
			}
		}
		
		PayDetailBuilder builder = new PayDetailBuilder();
		builder.before(null);
		if(seg_type == SegmentConst.SEG_TYPE_THD) {
			builder.buildTHD(matchVOs,entrustVOs);
		} 
		else if(seg_type == SegmentConst.SEG_TYPE_GXD) {
			builder.buildGXD(matchVOs,entrustVOs);
		} else if(seg_type == SegmentConst.SEG_TYPE_SHD) {
			builder.buildSHD(matchVOs,entrustVOs);
		} else if(seg_type == SegmentConst.SEG_TYPE_FX) {
			builder.buildFX(matchVOs,entrustVOs);
		}else if(seg_type == SegmentConst.SEG_TYPE_NORMAL){//增加整车应付按批次计算逻辑2015-12-27 lanjian
			builder.computePayDetail(matchVOs,entrustVOs);
		}
		builder.after(null); 
	}
	
	
	
	@Transactional
	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys){
		//yaojiie 2015 12 15 应付明细已被删除，需要重新计算金额和利润
		String cond = NWUtils.buildConditionString(primaryKeys);
		String sql = "SELECT inv.* FROM ts_ent_inv_b ei WITH (NOLOCK) "
				+ " LEFT JOIN ts_invoice inv WITH (NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
				+ " LEFT JOIN ts_entrust ent WITH (NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
				+ " LEFT JOIN ts_pay_detail pd WITH (NOLOCK) ON pd.entrust_vbillno = ent.vbillno"
				+ " WHERE isnull(ei.dr,0)=0 AND isnull(inv.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(pd.dr,0)=0 AND pd.pk_pay_detail in "
				+ cond;
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class);
		for(String primaryKey : primaryKeys) {
			deleteByPrimaryKey(clazz, primaryKey);
		}
		CMUtils.totalCostComput(invoiceVOs);
		return primaryKeys.length;
	}
	
	private void setCodeFieldRorBuildPayCheckSheet(CircularlyAccessibleValueObject parentVO, ParamVO paramVO) {
		if(StringUtils.isNotBlank(getCodeFieldCode())) {
			// 子类继承了该方法，说明希望使用编码规则
			Object codeObj = parentVO.getAttributeValue(getCodeFieldCode());
			if(codeObj == null || StringUtils.isBlank(codeObj.toString())) {
				// 如果没有录入编码，则按照规则生成一个
				String billno = BillnoHelper.generateBillno(BillTypeConst.YFDZ);
				if(StringUtils.isBlank(billno)) {
					throw new RuntimeException("可能没有定义单据号规则，无法生成编码！");
				}
				SuperVO superVO = this.getByCodeWithNoDr(billno);
				while(superVO != null) {
					// 该订单号已经存在
					billno = BillnoHelper.generateBillno(this.getBillType());
					superVO = this.getByCodeWithNoDr(billno);
				}
				parentVO.setAttributeValue(getCodeFieldCode(), billno);
			}
		}
	}
	public List<Map<String, Object>> payDetailComputer(ParamVO paramVO ,String[] billIds){
		Map<Integer,List<String>> dispatcherResult = CostCalculateUtils.dispatcher(billIds, CostCalculateUtils.PD, WebUtils.getLoginInfo().getPk_user());
		for(Integer key : dispatcherResult.keySet()){
			if(key == CostCalculateUtils.PD_UNIT){
				CostCalculater costCalculater = new CostCalculater();
				Map<PayDetailVO, List<PayDetailBVO>> result = costCalculater.computeUnitPayable(Arrays.asList(billIds));
				List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> datas = execFormula4Templet(paramVO,new ArrayList<SuperVO>(result.keySet()));
				for(Map<String, Object> data : datas) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(Constants.HEADER, data);
					retList.add(map);
				}
				return retList;
			}
			if(key == CostCalculateUtils.PD_LOT){
				return reComputeMnyByLots(paramVO ,billIds);
			}
			if(key == CostCalculateUtils.PD_RT_LOT){
				return reComputeMnyByLots2(dispatcherResult.get(key));
			}
			if(key == CostCalculateUtils.PD_YUSEN_LOT){
				List<String> ids =  dispatcherResult.get(key);
				if(ids != null && ids.size() > 0){
					YusenPayHandler payHandler = new YusenPayHandler();
					payHandler.compute(ids);
				}
			}
			
		}
		return null;
	}
	
	
	public List<Map<String, Object>> reComputeMnyByLots2(List<String> billIds){
		if(billIds == null || billIds.size() == 0) {
			return null;
		}
		
		String unitSql = "SELECT DISTINCT ts_pay_detail.*  "
				+ "FROM ts_pay_detail WITH (NOLOCK) "
				+ "LEFT JOIN ts_entrust  WITH (NOLOCK) ON ts_pay_detail.lot = ts_entrust.lot "
				+ "LEFT JOIN ts_pay_detail tpd2 WITH (NOLOCK) ON ts_entrust.vbillno = tpd2.entrust_vbillno "
				+ "WHERE isnull(ts_pay_detail.dr,0)=0 AND ts_pay_detail.pay_type=0 AND isnull(ts_entrust.dr,0)=0 AND isnull(tpd2.dr,0)=0  "
				+ "AND ts_entrust.vbillstatus <> 24 ";
		String cond = NWUtils.buildConditionString(billIds);
		String sql = unitSql + "AND tpd2.pk_pay_detail in " + cond;
		List<PayDetailVO> payDetailVOs = NWDao.getInstance().queryForList(sql, PayDetailVO.class);
		if(payDetailVOs == null || payDetailVOs.size() == 0){
			throw new BusiException("请选择单据！");
		}
		List<String> ent_vbillnos = new ArrayList<String>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			if(payDetailVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应付明细[?]必须是[新建]状态才能进行重算金额！",payDetailVO.getVbillno());
			}
			if(payDetailVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！",payDetailVO.getVbillno());
			}
			if(StringUtils.isBlank(payDetailVO.getEntrust_vbillno())) {
				throw new BusiException("应付明细[?]没有对应的委托单！",payDetailVO.getVbillno());
			}
			ent_vbillnos.add(payDetailVO.getEntrust_vbillno());
		}
		List<String> pk_entrusts = new ArrayList<String>();
		List<String> pk_pay_details = new ArrayList<String>();
		//判断委托单状态
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				"vbillno in " + NWUtils.buildConditionString(ent_vbillnos));
		for(PayDetailVO payDetailVO : payDetailVOs){
			boolean flag = true;
			for(EntrustVO entrustVO : entrustVOs){
				if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
					pk_entrusts.add(entrustVO.getPk_entrust());
					flag = false;
					break;
				}
			}
			if(flag){
				throw new BusiException("应付明细[?]对应的委托单[?]已经不存在！",payDetailVO.getVbillno(),payDetailVO.getEntrust_vbillno());
			}
			pk_pay_details.add(payDetailVO.getPk_pay_detail());
		}
		EntTransbilityBVO[] transbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
				"pk_entrust in " + NWUtils.buildConditionString(pk_entrusts));
		
		PayDetailBVO[] allOldPayDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(pk_pay_details));
		
		//根据应付明细计算出运费
		//对PayDetailVO按照批次分组
		Map<String,List<PayDetailVO>> groupMap = new HashMap<String,List<PayDetailVO>>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			String key = payDetailVO.getLot();
			List<PayDetailVO> voList = groupMap.get(key);
			if(voList  == null){
				voList = new ArrayList<PayDetailVO>();
				groupMap.put(key, voList);
			}
			voList.add(payDetailVO);
		}
		List<SuperVO> ToBeUpdate = new ArrayList<SuperVO>();
		for(String key : groupMap.keySet()){
			//对应付按照同一个批次下的同一个到货地址进行合并
			List<String> pk_arrivals = new ArrayList<String>();
			List<EntrustVO> entVOs = new ArrayList<EntrustVO>();
			for(PayDetailVO payDetailVO : groupMap.get(key)){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
						entVOs.add(entrustVO);
						break;
					}
				}
			}
			for(EntrustVO entVO : entVOs){
				pk_arrivals.add(entVO.getPk_arrival());
			}
			HashSet<String> hs = new HashSet<String>(pk_arrivals);
			int length = hs.size();
			int i = 1;
			for(PayDetailVO payDetailVO : groupMap.get(key)){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
						List<EntTransbilityBVO> transBVOs = new ArrayList<EntTransbilityBVO>();
						for(EntTransbilityBVO transbilityBVO : transbilityBVOs){
							if(transbilityBVO.getPk_entrust().equals(transbilityBVO.getPk_entrust())){
								transBVOs.add(transbilityBVO);
							}
						}
						List<PayDetailBVO> oldPayDetailBVOs = new ArrayList<PayDetailBVO>();
						for(PayDetailBVO oldPayDetailBVO : allOldPayDetailBVOs){
							if(oldPayDetailBVO.getPk_pay_detail().equals(payDetailVO.getPk_pay_detail())){
								oldPayDetailBVOs.add(oldPayDetailBVO);
							}
						}
						EntTransbilityBVO[] tbBVOs = transBVOs.toArray(new EntTransbilityBVO[transBVOs.size()]);
						//这里计算length个点位费
						List<PayDetailBVO> detailBVOs = EntrustUtils.getPayDetailBVOs(contractService, entrustVO, tbBVOs,2,oldPayDetailBVOs,payDetailVO);
						if(i < length){
							i++;
						}else{
							List<PayDetailBVO> temp = new ArrayList<PayDetailBVO>();
							for(PayDetailBVO detailBVO : detailBVOs){
								if(!detailBVO.getPk_expense_type().equals("9436f31e58fc44d1981471b4c2d50e95")){
									temp.add(detailBVO);
								}
							}
							detailBVOs = temp;
						}
						
						List<PayDetailBVO> newPayDetailBVOs = new ArrayList<PayDetailBVO>();
						if(detailBVOs != null && detailBVOs.size() > 0){
							for(PayDetailBVO detailBVO : detailBVOs) {
								newPayDetailBVOs.add(detailBVO);
								if(detailBVO.getStatus() == VOStatus.NEW) {
									detailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail()); // 设置主表的主键
									NWDao.setUuidPrimaryKey(detailBVO);
								}
							}
						}
						ToBeUpdate.addAll(newPayDetailBVOs);
						List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entrustVO, null, null,
								newPayDetailBVOs.toArray(new PayDetailBVO[newPayDetailBVOs.size()]));
						ToBeUpdate.addAll(deviBVOs);
						List<PayDetailBVO> allDeatilb = new ArrayList<PayDetailBVO>();
						allDeatilb.addAll(newPayDetailBVOs);
						for(PayDetailBVO oldPayDetailBVO :  oldPayDetailBVOs){
							if(oldPayDetailBVO.getSystem_create() != null && oldPayDetailBVO.getSystem_create().equals(UFBoolean.TRUE)){
								oldPayDetailBVO.setStatus(VOStatus.DELETED);
								ToBeUpdate.add(oldPayDetailBVO);
							}
							if(oldPayDetailBVO.getSystem_create() == null || oldPayDetailBVO.getSystem_create().equals(UFBoolean.FALSE)){
								allDeatilb.add(oldPayDetailBVO);
							}
						}
						NWDao.getInstance().saveOrUpdate(ToBeUpdate);
						ToBeUpdate.clear();
						CMUtils.processExtenalforComputer(payDetailVO, allDeatilb);
						//更新应收明细表头成本信息2016-7-4 XIA
						String invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
								+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
								+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
								+ " AND ent.vbillno =? ";
						if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
							invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei ON inv.pk_invoice = ei.pk_invoice "
									+ " LEFT JOIN ts_entrust ent ON ent.pk_entrust = ei.pk_entrust "
									+ " WHERE nvl(ei.dr,0)=0 AND nvl(ent.dr,0)=0 AND nvl(inv.dr,0)=0 "
									+ " AND ent.vbillno =? ";
						}
						List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(invSql, InvoiceVO.class,entrustVO.getVbillno());
						CMUtils.totalCostComput(invoiceVOs);
					}
				}
			}
		}
		return null;
	}

	
	
	public List<Map<String, Object>> reComputeMnyByLots(ParamVO paramVO ,String[] billIds) {
		StringBuffer mess = new StringBuffer();// 每个批次计算时的错误信息
		if(billIds == null || billIds.length == 0) {
			return null;
		}
		String unitSql = "SELECT ts_pay_detail.* , ts_entrust_lot.lot FROM ts_pay_detail WITH (NOLOCK) "
				+ "LEFT JOIN ts_entrust WITH (NOLOCK) ON ts_entrust.vbillno = ts_pay_detail.entrust_vbillno "
				+ "LEFT JOIN ts_entrust_lot WITH (NOLOCK) ON ts_entrust_lot.lot = ts_entrust.lot "
				+ "WHERE isnull(ts_pay_detail.dr,0)=0 AND ts_pay_detail.pay_type=0 AND isnull(ts_entrust.dr,0)=0 AND isnull(ts_entrust_lot.dr,0)=0 "
				+ "AND ts_entrust.vbillstatus <> 24 ";
		String cond = NWUtils.buildConditionString(billIds);
		String sql = unitSql + "AND ts_pay_detail.pk_pay_detail in " + cond;
		
		List<PayDetailVO> payDetailVOs = NWDao.getInstance().queryForList(sql, PayDetailVO.class);
		if(payDetailVOs == null || payDetailVOs.size() == 0){
			throw new BusiException("没有查询到应付明细，请检查数据！");
		}
		//将payDetailVOs以lot号分组，这样可以拿到不重复的Lot号
		logger.info("对应付明细按相同批次号进行分组");
		Map<String, List<PayDetailVO>> groupMap = new HashMap<String, List<PayDetailVO>>();
		for (PayDetailVO payDetailVO : payDetailVOs) {
			if(payDetailVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！",payDetailVO.getVbillno());
			}
			String key = new StringBuffer().append(payDetailVO.getLot()).toString();
			List<PayDetailVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<PayDetailVO>();
				groupMap.put(key, voList);
			}
			voList.add(payDetailVO);
		}
		logger.info("共分成" + groupMap.size() + "组");
		String lotCond = NWUtils.buildConditionString(groupMap.keySet().toArray(new String[groupMap.keySet().size()]));
		String lotSql = unitSql + "AND ts_entrust_lot.lot in " + lotCond;
		EntLotVO[] entLotVOs = dao.queryForSuperVOArrayByCondition(EntLotVO.class, " lot in " + lotCond);
		
		List<PayDetailVO> allPayDetailVOs = dao.queryForList(lotSql,PayDetailVO.class);
		
		if(entLotVOs == null || entLotVOs.length == 0){
			throw new BusiException("没有查询到批次信息，请检查数据！");
		}
		//对payDetailVOs和entLotVOs进行分组匹配
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
		for (EntLotVO entLotVO : entLotVOs){
			logger.info("开始处理批次号：" + entLotVO.getLot());
			mess.setLength(0);
			List<String> billId = new ArrayList<String>();
			for (PayDetailVO payDetailVO : allPayDetailVOs){
				if(entLotVO.getLot().equals(payDetailVO.getLot())){
					if (payDetailVO.getVbillstatus().equals(BillStatus.NEW)) {
						billId.add(payDetailVO.getPk_pay_detail());
					} else {
						logger.info("批次号" + entLotVO.getLot() + "中应付明细" + payDetailVO.getVbillno() + "状态有误");
						mess.setLength(1);
						break;
					}
				}
			}
			if (mess.length() > 0) {
				break;
			} else {
				this.doPayDetailRebuildBySegtype(paramVO, SegmentConst.SEG_TYPE_NORMAL, billId.toArray(new String[billId.size()]));
				//修改批次表费用计算标记信息2015-12-28 lanjian
				entLotVO.setStatus(VOStatus.UPDATED);
				entLotVO.setModify_time(new UFDateTime(new Date()));
				entLotVO.setModify_user("system");
				entLotVO.setDef4("Y");
				toBeUpdate.add(entLotVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));
		toBeUpdate.clear();
		//根据单据号，重新查询对应的应付明细，在前台显示
		SuperVO[] newPayDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, 
				"pk_pay_detail in " + cond);
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO,Arrays.asList(newPayDetailVOs));
		return mapList;
	}
	
	public void saveLotPay(List<PayDetailBVO> detailBVOs,String[] pdpks){
		//组织数据，将输入的应付明细B，分摊到对应到每个应付明细中。
		//第一步，查找对应的应付明细
		if(pdpks == null || pdpks.length == 0){
			return;
		}
		
		String pdCond = NWUtils.buildConditionString(pdpks);
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "pk_pay_detail in " + pdCond);
		PayDetailBVO[] allOldPayDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class, "pk_pay_detail in " + pdCond);
		//当所有的计费重都是0的时候，评价分摊。
		int index = 0;
		UFDouble allFee_weight_count = UFDouble.ZERO_DBL;
		for(PayDetailVO payDetailVO : payDetailVOs){
			allFee_weight_count = allFee_weight_count.add(payDetailVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : payDetailVO.getFee_weight_count());
			index++;
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<String> entrustVbillnos = new ArrayList<String>();
		List<PayDetailBVO> allNewPayDetailVOs = new ArrayList<PayDetailBVO>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			List<PayDetailBVO> allPayDetailVOs = new ArrayList<PayDetailBVO>();
			entrustVbillnos.add(payDetailVO.getEntrust_vbillno());
			for(PayDetailBVO payDetailBVO : detailBVOs){
				PayDetailBVO newPayDetailBVO = new PayDetailBVO();
				newPayDetailBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(newPayDetailBVO);
				newPayDetailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail());
				newPayDetailBVO.setPrice_type(payDetailBVO.getPrice_type());
				newPayDetailBVO.setValuation_type(payDetailBVO.getValuation_type());
				newPayDetailBVO.setPk_expense_type(payDetailBVO.getPk_expense_type());
				newPayDetailBVO.setQuote_type(payDetailBVO.getQuote_type());
				newPayDetailBVO.setPrice(payDetailBVO.getPrice());
				if(!allFee_weight_count.equals(UFDouble.ZERO_DBL)){
					if(payDetailVO.getFee_weight_count() == null ){
						newPayDetailBVO.setAmount(UFDouble.ZERO_DBL);
					}else{
						newPayDetailBVO.setAmount(payDetailBVO.getAmount().multiply(payDetailVO.getFee_weight_count().div(allFee_weight_count)));
					}
				}else{
					newPayDetailBVO.setAmount(payDetailBVO.getAmount().multiply(1.0/index));
				}
				newPayDetailBVO.setMemo(payDetailBVO.getMemo());
				allNewPayDetailVOs.add(newPayDetailBVO);
				allPayDetailVOs.add(newPayDetailBVO);
				toBeUpdate.add(newPayDetailBVO);
			}
			for(PayDetailBVO payDetailBVO : allOldPayDetailVOs){
				if(payDetailBVO.getPk_pay_detail().equals(payDetailVO.getPk_pay_detail())){
					allPayDetailVOs.add(payDetailBVO);
				}
			}
			payDetailVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(payDetailVO);
			// 重新计算应付对账的金额 对账单明细实际上不存储金额，只是关联应付明细
			CMUtils.processExtenal(payDetailVO, allPayDetailVOs);
		}
		List<PayDeviBVO> payDeviBVOs = this.getPayDeviBVOs(entrustVbillnos, payDetailVOs, allNewPayDetailVOs);
		toBeUpdate.addAll(payDeviBVOs);
		NWDao.getInstance().saveOrUpdate(toBeUpdate);

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
	private List<PayDeviBVO> getPayDeviBVOs(List<String> entrustVbillnos, PayDetailVO[] payDetailVOs,
			List<PayDetailBVO> newPayDetailBVOs) {
		String entCond = NWUtils.buildConditionString(entrustVbillnos.toArray(new String[entrustVbillnos.size()]));
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				" vbillno in " + entCond);
		List<String> pk_entrusts = new ArrayList<String>();
		if (entrustVOs != null && entrustVOs.length > 0) {
			for (EntrustVO entrustVO : entrustVOs) {
				pk_entrusts.add(entrustVO.getPk_entrust());
			}
		}

		String pkEntCond = NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()]));
		EntSegBVO[] entSegBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntSegBVO.class,
				"pk_entrust in " + pkEntCond);

		SegmentVO[] segmentVOs = null;
		if (entSegBVOs != null && entSegBVOs.length > 0) {
			// 得到所有运段
			String segCond = getSegmentCond(entSegBVOs);

			segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"pk_segment in " + segCond);

		}
		List<Map<String, Object>> paramMaps = new ArrayList<Map<String, Object>>();

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

		String payDeviType = ParameterHelper.getPayDeviType();// 得到分摊类型
		String payDeviDimension = ParameterHelper.getPayDeviDimension();// 得到分摊维度
		boolean allDeviValueIsZero = true;// 如果分摊数量都为0，那么平均分摊
		List<PayDeviBVO> allDeviBVOs = new ArrayList<PayDeviBVO>();
		for (PayDetailBVO detailBVO : detailBVOs) {
			List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();// 每个费用的分摊费用明细
			for (String key : fee_weight_countMap.keySet()) {// 这里循环fee_weight_countMap和循环volume_countMap是一样的
				InvoiceVO invVO = NWDao.getInstance().queryByCondition(InvoiceVO.class,
						new String[] { "pk_invoice", "vbillno" }, "vbillno=?", key);
				if (PayDetailConst.PAY_DEVI_DIMENSION.INVOICE.equals(payDeviDimension)) {
					PayDeviBVO deviBVO = new PayDeviBVO();
					deviBVO.setPk_entrust(entVO.getPk_entrust());
					deviBVO.setPk_invoice(invVO.getPk_invoice());
					deviBVO.setPk_carrier(entVO.getPk_carrier());
					deviBVO.setPk_car_type(entVO.getPk_car_type());
					deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
					if (detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
					} else {
						if (PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
								|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
							double fee_weight_count = fee_weight_countMap.get(key).doubleValue();
							if (allFee_weight_count.doubleValue() == 0) {
								deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviBVO.setSys_devi_amount(
										detailBVO.getAmount().div(allFee_weight_count).multiply(fee_weight_count));
							}
							if (fee_weight_count != 0) {
								allDeviValueIsZero = false;
							}
						} else if (PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
							// 按体积进行分摊，得到当前发货单的总体积
							double volume_count = volume_countMap.get(key).doubleValue();
							if (allVolume_count.doubleValue() == 0) {
								deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
							} else {
								deviBVO.setSys_devi_amount(
										detailBVO.getAmount().div(allVolume_count).multiply(volume_count));
							}
							if (volume_count != 0) {
								allDeviValueIsZero = false;
							}
						}
					}
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL
							: deviBVO.getSys_devi_amount().setScale(ParameterHelper.getPrecision(),
									UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());// 手工分摊金额：默认等于系统分摊金额
					deviBVO.setPk_pay_detail(detailBVO.getPk_pay_detail());
					deviBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(deviBVO);
					deviBVOs.add(deviBVO);
				} else if (PayDetailConst.PAY_DEVI_DIMENSION.DETAIL.equals(payDeviDimension)) {
					// FIXME 这里是否应该使用运段的货品明细？
					InvPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
							"pk_invoice=?", invVO.getPk_invoice());
					if (packBVOs == null || packBVOs.length == 0) {
						// 没有货品信息，不需要分摊
						continue;
					}
					TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
							entVO.getPk_trans_type());
					if (typeVO == null) {
						throw new BusiException("当前的运输方式已经被删除，pk[?]！",entVO.getPk_trans_type());
					}
					if (typeVO.getRate() == null) {
						typeVO.setRate(UFDouble.ZERO_DBL);// 没有定义相应的换算比率，默认为0
					}
					for (InvPackBVO packBVO : packBVOs) {
						PayDeviBVO deviBVO = new PayDeviBVO();
						deviBVO.setPk_entrust(entVO.getPk_entrust());
						deviBVO.setPk_invoice(invVO.getPk_invoice());
						deviBVO.setInvoice_serialno(packBVO.getSerialno());
						deviBVO.setPk_carrier(entVO.getPk_carrier());
						deviBVO.setPk_car_type(entVO.getPk_car_type());
						deviBVO.setPk_expense_type(detailBVO.getPk_expense_type());
						if (detailBVO.getAmount() == null || detailBVO.getAmount().doubleValue() == 0) {
							// 总金额为0，不需要分摊
						} else {
							if (PayDetailConst.PAY_DEVI_TYPE.WEIGHT.equals(payDeviType)
									|| PayDetailConst.PAY_DEVI_TYPE.FEE_WEIGHT.equals(payDeviType)) {
								// 按重量分摊
								// 计算当前这个发货单的总计费重
								UFDouble volume = packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume();
								UFDouble weight = packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight();
								double fee_weight_count = weight.doubleValue();
								double fee = volume.multiply(typeVO.getRate()).doubleValue();
								if (fee > weight.doubleValue()) {
									fee_weight_count = fee;
								}

								if (allFee_weight_count.doubleValue() == 0) {
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(
											detailBVO.getAmount().div(allFee_weight_count).multiply(fee_weight_count));
								}
								if (fee_weight_count != 0) {
									allDeviValueIsZero = false;
								}
							} else if (PayDetailConst.PAY_DEVI_TYPE.VOLUME.equals(payDeviType)) {
								// 按体积进行分摊，这里按照行分摊，必须读取行的体积
								double volume_count = packBVO.getVolume() == null ? 0
										: packBVO.getVolume().doubleValue();
								if (allVolume_count.doubleValue() == 0) {
									deviBVO.setSys_devi_amount(UFDouble.ZERO_DBL);
								} else {
									deviBVO.setSys_devi_amount(
											detailBVO.getAmount().div(allVolume_count).multiply(volume_count));
								}
								if (volume_count != 0) {
									allDeviValueIsZero = false;
								}
							}
						}
						// 2015-3-18 对分摊费用设置四舍五入
						deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL
								: deviBVO.getSys_devi_amount().setScale(ParameterHelper.getPrecision(),
										UFDouble.ROUND_HALF_UP));
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
			if (allDeviValueIsZero) {
				// 平均分摊
				for (PayDeviBVO deviBVO : deviBVOs) {
					// 当手工删除费用时，getAmount 会为NULL，这次做下NULL 的判断。2015-11-1 Jonathan
					// deviBVO.setSys_devi_amount(detailBVO.getAmount().div(deviBVOs.size()));
					deviBVO.setSys_devi_amount(
							(detailBVO.getAmount() == null ? UFDouble.ZERO_DBL : detailBVO.getAmount())
									.div(deviBVOs.size()));
					// 2015-3-18 对分摊费用设置四舍五入
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL
							: deviBVO.getSys_devi_amount().setScale(ParameterHelper.getPrecision(),
									UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getSys_devi_amount());
				}
			}
			allDeviBVOs.addAll(deviBVOs);
		}
		return allDeviBVOs;
	}

	public List<Map<String, Object>> loadPayDetail(String[] pk_pay_detail) {
		if(pk_pay_detail == null || pk_pay_detail.length == 0){
			throw new BusiException("请选择单据！");
		}
		//查询传入的应付明细对应的批次号，只能是同一个批次。
		String pks = NWUtils.buildConditionString(pk_pay_detail);
		String sql = "select distinct ent.lot from ts_entrust ent with(nolock) left join ts_pay_detail pd on "
				+ "ent.vbillno = pd.entrust_vbillno where isnull(ent.dr,0) = 0 and isnull(pd.dr,0) = 0"
				+ "and pd.pk_pay_detail in " + pks;
		List<String> lots = NWDao.getInstance().queryForList(sql, String.class);
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "pk_pay_detail in " + pks);
		for(PayDetailVO payDetailVO : payDetailVOs){
			if(payDetailVO.getVbillstatus() != BillStatus.NEW){
				throw new BusiException("应付明细状态不是[新建]！");
			}
		}
		if(lots == null || lots.size() == 0){
			throw new BusiException("您选择的应付明细单据没有批次号！");
		}
		if(lots == null || lots.size() > 1){
			throw new BusiException("您选择的应付明细单据有多个批次号，请检查数据！");
		}
		return null;
	}
	
	@Transactional
	public Map<String, Object> close(ParamVO paramVO) {
		logger.info("执行单据关闭动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.NEW != billStatus) {
				throw new RuntimeException("只有[新建]状态的单据才能进行关闭！");
			}
		}
		PayDetailVO detailVO = (PayDetailVO)parentVO;
		detailVO.setStatus(VOStatus.UPDATED);
		detailVO.setVbillstatus(BillStatus.PD_CLOSE);
		detailVO.setModify_time(new UFDateTime());
		detailVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		NWDao.getInstance().saveOrUpdate(detailVO);
		return execFormula4Templet(billVO, paramVO);
	}

	@Transactional
	public Map<String, Object> unclose(ParamVO paramVO) {
		logger.info("执行单据撤销关闭动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.PD_CLOSE != billStatus) {
				throw new RuntimeException("只有[关闭]状态的单据才能进行撤销关闭！");
			}
		}
		PayDetailVO detailVO = (PayDetailVO)parentVO;
		detailVO.setStatus(VOStatus.UPDATED);
		detailVO.setVbillstatus(BillStatus.NEW);
		detailVO.setModify_time(new UFDateTime());
		detailVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		NWDao.getInstance().saveOrUpdate(detailVO);
		return execFormula4Templet(billVO, paramVO);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String,String> CheckSheetByProc(String ids){
		if(StringUtils.isBlank(ids)){
			return null;
		}
		final Map<String,String> returnMsgs = new HashMap<String,String>();
		// 存储过程名称
		final String TS_FHD_CHECK_PROC = "ts_build_pay_check_proc";
		final String PK = ids;
		final String EMPTY = "";
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 2;
					String storedProc = DaoHelper.getProcedureCallName(TS_FHD_CHECK_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, PK);
					cs.setString(2, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						returnMsgs.put("msg",rs.getString(1));
						returnMsgs.put("type",rs.getString(2));
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			logger.info(e.getMessage());
			//出现错误返回空接可以了
			return null;
		}
		return returnMsgs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String doProcByConfirmAndUnConform(String ids,String procName,Integer type){
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = procName;
		final String PK = ids;
		final String TYPE = type.toString();
		final String USER = WebUtils.getLoginInfo() == null ? "" : WebUtils.getLoginInfo().getPk_user();
		final String EMPTY = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, PK);
					cs.setString(2, TYPE);
					cs.setString(3, USER);
					cs.setString(4, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						RETURN.add(rs.getString(1));
						break;
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RETURN.get(0);
	}
	
	
	/**
	 * 在应付明细确认，反确认 ，提交，撤销提交的时候，执行存储过程检查。
	 * @param ids
	 * @param procName
	 * @param type 0确认，1反确认，2提交，3反提交
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String payCheckProc(String ids,Integer type){
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = "ts_yfmx_check_proc";
		final String PK = ids;
		final String TYPE = type.toString();
		final String USER = WebUtils.getLoginInfo() == null ? "" : WebUtils.getLoginInfo().getPk_user();
		final String EMPTY = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, PK);
					cs.setString(2, TYPE);
					cs.setString(3, USER);
					cs.setString(4, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						RETURN.add(rs.getString(1));
						break;
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RETURN.get(0);
	}
	
	@Override
	protected String getConfirmAndUnconfirmProcName() {
		return "ts_yfmx_check_proc";
	}
}
