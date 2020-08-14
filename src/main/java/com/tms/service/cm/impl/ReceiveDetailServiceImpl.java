package com.tms.service.cm.impl;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.helper.DaoHelper;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.service.ServiceHelper;
import org.springframework.stereotype.Service;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.utils.QueryHelper;
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
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.UserVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.transaction.annotation.Transactional;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.QuoteTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransTypeConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.cm.PayDetailService;
import com.tms.service.cm.ReceiveDetailService;
import com.tms.service.inv.InvoiceService;
import com.tms.service.job.cm.MatchVO;
import com.tms.service.job.cm.ReceDetailBuilder;
import com.tms.utils.CostCalculateUtils;
import com.tms.utils.CostCalculater;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.ExAggReceiveDetailVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceCheckSheetBVO;
import com.tms.vo.cm.ReceCheckSheetVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceRecordVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.OrderAssVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.te.ExpAccidentVO;
import com.tms.vo.tp.SegmentVO;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 应收明细
 * 
 * @author xuqc
 * @date 2012-8-12 上午01:02:07
 */
@Service
public class ReceiveDetailServiceImpl extends TMSAbsBillServiceImpl implements ReceiveDetailService {

	@Autowired
	private ExpenseTypeService expenseTypeService;
	
	@Autowired
	private PayDetailService payDetailService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
		

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, InvPackBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, InvPackBVO.PK_INVOICE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_pack_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_pack_b");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, TransBilityBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, TransBilityBVO.PK_INVOICE);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_trans_bility_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_trans_bility_b");

			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, PayDeviBVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, PayDeviBVO.PK_INVOICE);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_pay_devi_b");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_pay_devi_b");
			
			VOTableVO childVO3 = new VOTableVO();
			childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.HEADITEMVO, ExpAccidentVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.PKFIELD, "invoice_vbillno");
			childVO3.setAttributeValue(VOTableVO.ITEMCODE, "ts_exp_accident");
			childVO3.setAttributeValue(VOTableVO.VOTABLE, "ts_exp_accident");
			
			VOTableVO childVO4 = new VOTableVO();
			childVO4.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
			childVO4.setAttributeValue(VOTableVO.HEADITEMVO, SegmentVO.class.getName());
			childVO4.setAttributeValue(VOTableVO.PKFIELD, "invoice_vbillno");
			childVO4.setAttributeValue(VOTableVO.ITEMCODE, "ts_segment");
			childVO4.setAttributeValue(VOTableVO.VOTABLE, "ts_segment");
			
			billInfo = getRealBillInfo();
			CircularlyAccessibleValueObject[] childrenVO = billInfo.getChildrenVO();
			CircularlyAccessibleValueObject[] newChildrenVO = new CircularlyAccessibleValueObject[childrenVO.length
					+ 5];
			for (int i = 0; i < childrenVO.length; i++) {
				newChildrenVO[i] = childrenVO[i];
			}
			newChildrenVO[childrenVO.length] = childVO;
			newChildrenVO[childrenVO.length + 1] = childVO1;
			newChildrenVO[childrenVO.length + 2] = childVO2;
			newChildrenVO[childrenVO.length + 3] = childVO3;
			newChildrenVO[childrenVO.length + 4] = childVO4;
			billInfo.setChildrenVO(newChildrenVO);
		}
		return billInfo;
	}

	public AggregatedValueObject getRealBillInfo() {
		AggregatedValueObject billInfo = new ExAggReceiveDetailVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, ReceiveDetailVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, ReceiveDetailVO.PK_RECEIVE_DETAIL);
		billInfo.setParentVO(vo);

		VOTableVO childVO = new VOTableVO();
		childVO.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
		childVO.setAttributeValue(VOTableVO.HEADITEMVO, ReceDetailBVO.class.getName());
		childVO.setAttributeValue(VOTableVO.PKFIELD, ReceDetailBVO.PK_RECEIVE_DETAIL);
		childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_rece_detail_b");
		childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_rece_detail_b");

		CircularlyAccessibleValueObject[] childrenVO = { childVO };
		billInfo.setChildrenVO(childrenVO);
		return billInfo;
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
		processor.setTable_code(TabcodeConst.TS_RECE_RECORD);
		processor.setPk_billtemplet(templetVO.getTemplateID());
		processor.setPk_billtemplet_b(UUID.randomUUID().toString());
		templetVO.getFieldVOs().add(0, processor);
		return templetVO;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("bala_customer")) {
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				} else if (fieldVO.getItemkey().equals("num_count")) {
					fieldVO.setUserdefine1("afterChangeNumCount(field,value,originalValue)");
				} else if (fieldVO.getItemkey().equals("fee_weight_count")) {
					fieldVO.setUserdefine1("afterChangeFeeWeightCount(field,value,originalValue)");
				} else if (fieldVO.getItemkey().equals("volume_count")) {
					fieldVO.setUserdefine1("afterChangeVolumeCount(field,value,originalValue)");
				} else if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("invoice_vbillno")) {
					fieldVO.setRenderer("invoice_vbillnoRenderer");
				} else if (fieldVO.getItemkey().equals("cost_amount") || fieldVO.getItemkey().equals("got_amount")
						|| fieldVO.getItemkey().equals("ungot_amount")) {
					fieldVO.setBeforeRenderer("amountBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("lot")) {
					fieldVO.setRenderer("lotRenderer");
				}
			} else if (fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if (fieldVO.getTable_code().equals(TabcodeConst.TS_RECE_DETAIL_B)) {
					if (fieldVO.getItemkey().equals("quote_type") || fieldVO.getItemkey().equals("valuation_type")
							|| fieldVO.getItemkey().equals("price")) {
						fieldVO.setUserdefine1("afterEditQuoteTypeOrValuationTypeOrPrice(record)");
					} else if (fieldVO.getItemkey().equals("expense_type_name")) {
						// 费用类型
						fieldVO.setUserdefine1("afterEditExpenseTypeName(record)");
					} else if (fieldVO.getItemkey().equals("amount")) { // 编辑金额时，更新总金额
						fieldVO.setUserdefine1("updateHeaderCostAmount()");
					}
				}
			}
		}
		return templetVO;
	}

	public String getBillType() {
		return BillTypeConst.YSMX;
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		AggregatedValueObject billVO = ServiceHelper.queryBillVO(this.getRealBillInfo(), paramVO);
		ReceiveDetailVO parentVO = (ReceiveDetailVO) billVO.getParentVO();
		InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, new String[] { "pk_invoice", }, "vbillno=?",
				parentVO.getInvoice_vbillno());
		if (invVO != null) {
			// 根据发货单的单据号，读取运力信息和货品包装明细信息
			InvPackBVO[] packBVOs = dao.queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?",
					invVO.getPk_invoice());
			((IExAggVO) billVO).setTableVO(TabcodeConst.TS_INV_PACK_B, packBVOs);

			TransBilityBVO[] tbBVOs = dao.queryForSuperVOArrayByCondition(TransBilityBVO.class, "pk_invoice=?",
					invVO.getPk_invoice());
			((IExAggVO) billVO).setTableVO(TabcodeConst.TS_TRANS_BILITY_B, tbBVOs);
			//yaojiie 2016 1 15 增加异常事故
			ExpAccidentVO[] accidentVOs =  dao.queryForSuperVOArrayByCondition(ExpAccidentVO.class, "invoice_vbillno=?",
					parentVO.getInvoice_vbillno());
			((IExAggVO) billVO).setTableVO(TabcodeConst.TS_EXP_ACCIDENT, accidentVOs);
			//yaojiie 2016 2 25 增加运段信息
			SegmentVO[] segmentVOs = getSegmentVOsByInvoiceBillno(parentVO.getInvoice_vbillno());
			((IExAggVO) billVO).setTableVO(TabcodeConst.TS_SEGMENT, segmentVOs);
			/**
			 * 处理运力信息中的单价和金额显示.查找费用明细中计价方式是"设备"的记录,<br/>
			 * 如果一条,那么单价就是这条记录的单价,金额=单价*设备数量 <br/>
			 * 如果存在多条，那么单价设置为空，--XXX不会存在多条的情况
			 */
			if (tbBVOs != null && tbBVOs.length != 0) {
				ReceDetailBVO rdbVO = null;
				ExAggReceiveDetailVO aggVO = (ExAggReceiveDetailVO) billVO;
				ReceDetailBVO[] rdbVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
				for (ReceDetailBVO vo : rdbVOs) {
					if (vo.getValuation_type() != null
							&& vo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
						rdbVO = vo;
						break;
					}
				}
				if (rdbVO != null) {
					// 找到了计价方式是设备的记录
					for (TransBilityBVO vo : tbBVOs) {
						UFDouble price = rdbVO.getPrice() == null ? UFDouble.ZERO_DBL : rdbVO.getPrice();
						vo.setPrice(price);
						Integer num = vo.getNum() == null ? 0 : vo.getNum();
						vo.setAmount(price.multiply(num));
					}
				}
			}
			// 如果是原始凭证，根据pk_invoice查询分摊金额
			if (parentVO.getRece_type().intValue() == ReceiveDetailConst.ORIGIN_TYPE) {
				PayDeviBVO[] deviBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class,
						"pk_invoice=? and isnull(dr,0)=0", invVO.getPk_invoice());
				((IExAggVO) billVO).setTableVO(TabcodeConst.TS_PAY_DEVI_B, deviBVOs);
				// 统计成本，毛利，毛利率
				double total_cost = 0, maori = 0, maori_fee = 0;
				for (PayDeviBVO deviBVO : deviBVOs) {
					total_cost += deviBVO.getMan_devi_amount() == null ? 0 : deviBVO.getMan_devi_amount().doubleValue();
				}
				maori = parentVO.getCost_amount() == null ? 0 - total_cost : parentVO.getCost_amount().doubleValue() - total_cost;
				if (parentVO.getCost_amount() == null || parentVO.getCost_amount().doubleValue() == 0) {
					maori_fee = 0;
				} else {
					maori_fee = maori / parentVO.getCost_amount().doubleValue();
				}
				parentVO.setTotal_cost(new UFDouble(total_cost));
				parentVO.setMaori(new UFDouble(maori));
				parentVO.setMaori_fee((new UFDouble(maori_fee).multiply(100).setScale(2, UFDouble.ROUND_HALF_UP)).toString() + "%");
			} else {
				((IExAggVO) billVO).setTableVO(TabcodeConst.TS_PAY_DEVI_B, new PayDeviBVO[] {});
			}
		}

		return billVO;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String parentCond = super.buildLoadDataCondition(params, paramVO, templetVO);
		//将公司部分截掉
		int index = parentCond.indexOf("ts_receive_detail.pk_corp");
		if(index < 0){
			return parentCond;
		}else{
			parentCond = parentCond.substring(0, index);
			parentCond = parentCond + " ( ";
			parentCond = parentCond + CorpHelper.getCurrentCorpWithChildren();
			String custCond = " or "+ CorpHelper.getCurrentCorpWithChildren().replaceAll("pk_corp", "ts_receive_detail.billing_corp");
			parentCond = parentCond + custCond + ")";
		}
		
		
		return parentCond;
	}
	
	public SegmentVO[] getSegmentVOsByInvoiceBillno(String invoiceBillno) {
		String sql = "SELECT DISTINCT ts.* FROM ts_receive_detail trd "
				+" LEFT JOIN ts_segment ts ON ts.invoice_vbillno=trd.invoice_vbillno "
				+" WHERE ts.seg_mark<>1 AND ts.dr=0 AND trd.invoice_vbillno=? ";
		List<SegmentVO> segmentVOArr = dao.queryForList(sql, SegmentVO.class, invoiceBillno);
		if(segmentVOArr != null && segmentVOArr.size() > 0){
			return segmentVOArr.toArray(new SegmentVO[segmentVOArr.size()]);
		}
		return null;
	}
	
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ReceiveDetailVO parentVO = (ReceiveDetailVO) billVO.getParentVO();
		ExAggReceiveDetailVO aggVO = (ExAggReceiveDetailVO) billVO;
		ReceDetailBVO[] childVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		//如果这个单据绑定了，则取用委托单的公司，否则是登录用户的公司
		if(StringUtils.isNotBlank(parentVO.getInvoice_vbillno())){
			InvoiceVO invoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, 
					"vbillno =?", parentVO.getInvoice_vbillno());
			if(invoiceVO == null){
				logger.error("应收明细对应的发货单单已经被删除,entrust_vbillno:" + parentVO.getInvoice_vbillno());
				throw new BusiException("应收明细对应的发货单单已经被删除！");
			}
			parentVO.setPk_corp(invoiceVO.getPk_corp());
		}
		

		List<ReceDetailBVO> allDetailBVOs = new ArrayList<ReceDetailBVO>();
		allDetailBVOs.addAll(Arrays.asList(childVOs));
		// 统计运费费用和其他费用
		CMUtils.processExtenal(parentVO, allDetailBVOs.toArray(new ReceDetailBVO[allDetailBVOs.size()]));
		if(parentVO.getAccount_period() == null){
			parentVO.setAccount_period(parentVO.getCreate_time() == null ? new UFDateTime(new Date()) : parentVO.getCreate_time());
		}
	}

	public ReceiveDetailVO[] getByInvoiceBillno(String invoiceBillno) {
		return dao.queryForSuperVOArrayByCondition(ReceiveDetailVO.class, "invoice_vbillno=?", invoiceBillno);
	}

	/**
	 * 根据发货单的单据号查询费用明细记录
	 * 
	 * @param entrustBillno
	 * @return
	 */
	public ReceDetailBVO[] getReceDetailBVOsByInvoiceBillno(String invoiceBillno) {
		ReceDetailBVO[] detailBVOs = dao.queryForSuperVOArrayByCondition(ReceDetailBVO.class,
				"pk_receive_detail=(select pk_receive_detail from ts_receive_detail WITH(NOLOCK)  where invoice_vbillno=? and rece_type=?)",
				invoiceBillno, ReceiveDetailConst.ORIGIN_TYPE);
		return detailBVOs;
	}

	protected Integer getConfirmStatus() {
		return BillStatus.RD_CONFIRM;
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object type = parentVO.getAttributeValue("rece_type");
		Object vbillstatus = parentVO.getAttributeValue("vbillstatus");
		if (type != null && (Integer.parseInt(type.toString()) == ReceiveDetailConst.ORIGIN_TYPE
				|| Integer.parseInt(type.toString()) == ReceiveDetailConst.CUST_CLAIMANT_TYPE)) {
			throw new BusiException("不能删除单据类型为[原始单据、承运商索赔]的单据！");
		}
		if (vbillstatus != null && Integer.parseInt(vbillstatus.toString()) != BillStatus.NEW) {
			throw new BusiException("只能删除单据状态为[新建]的单据！");
		}
	}

	@SuppressWarnings("unchecked")
	public PaginationVO loadReceRecord(String pk_receive_detail, ParamVO paramVO, int offset, int pageSize) {
		PaginationVO paginationVO = dao.queryByConditionWithPaging(ReceRecordVO.class, offset, pageSize, "relationid=?",
				pk_receive_detail);
		List<Map<String, Object>> list = execFormula4Templet(paramVO, paginationVO.getItems());
		paginationVO.setItems(list);
		return paginationVO;
	}

	public Map<String, Object> doReceivable(ParamVO paramVO, String json) {
		logger.info("执行收款动作...");
		JsonNode header = JacksonUtils.readTree(json);
		ReceRecordVO rrVO = (ReceRecordVO) JacksonUtils.readValue(header, ReceRecordVO.class);
		// 更新主表
		ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?", rrVO.getRelationid());
		if (rdVO == null) {
			throw new BusiException("应收明细已经被删除！");
		}
		if (rdVO.getVbillstatus().intValue() != BillStatus.RD_CONFIRM
				&& rdVO.getVbillstatus().intValue() != BillStatus.RD_PART_CAVLOAN) {
			throw new BusiException("应收明细必须是[确认,部分核销]状态才能执行收款！");
		}
		// 收款金额
		UFDouble receivable_amount = rrVO.getReceivable_amount() == null ? UFDouble.ZERO_DBL
				: rrVO.getReceivable_amount();
		// 检查收款金额是否大于总金额
		UFDouble cost_amount = rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount();
		// xuqc 2013-11-3如果是客户索赔，此时的应收金额是负数，收款金额也应该是负数
		if (rdVO.getRece_type().intValue() == ReceiveDetailConst.CUST_CLAIMANT_TYPE) {
			// 客户索赔
			if (receivable_amount.doubleValue() > 0) {
				throw new BusiException("客户索赔时，收款金额不能大于0！");
			}
			if (receivable_amount.doubleValue() < cost_amount.doubleValue()) {
				throw new BusiException("客户索赔时，收款金额不能小于总金额！");
			}
		} else {
			if (receivable_amount.doubleValue() < 0) {
				throw new BusiException("收款金额不能小于0！");
			}
			if (receivable_amount.doubleValue() > cost_amount.doubleValue()) {
				throw new BusiException("收款金额不能大于总金额！");
			}
		}

		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		// 设置已收金额
		UFDouble got_amount = rdVO.getGot_amount() == null ? UFDouble.ZERO_DBL : rdVO.getGot_amount();
		rdVO.setGot_amount(got_amount.add(receivable_amount));
		// 设置未收金额
		rdVO.setUngot_amount(cost_amount.sub(rdVO.getGot_amount()));
		if (rdVO.getUngot_amount().doubleValue() == 0) {
			rdVO.setVbillstatus(BillStatus.RD_CAVLOAN);// 如果所有款项已经收完，则状态置为已核销
		} else {
			rdVO.setVbillstatus(BillStatus.RD_PART_CAVLOAN);// 部分核销
		}

		// 更新应收对账的发票号和发票抬头
		String[] checkArr = CMUtils.getUpdateCheck(rdVO.getCheck_no(), rrVO.getCheck_no(), rdVO.getCheck_head(),
				rrVO.getCheck_head());
		rdVO.setCheck_no(checkArr[0]);
		rdVO.setCheck_head(checkArr[1]);

		rdVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(rdVO);

		rrVO.setReceivable_type(ReceiveDetailConst.RECEIVABLE_TYPE.DIRECT.intValue());// 直接收款
		rrVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		rrVO.setCreate_time(new UFDateTime(new Date()));
		if (rrVO.getDbilldate() == null) {
			rrVO.setDbilldate(new UFDate());
		}
		rrVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(rrVO);
		toBeUpdate.add(rrVO);
		dao.saveOrUpdate(toBeUpdate);

		syncExpAccident(rdVO);

		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(rdVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}

	/**
	 * 同步更新已收金额和未收金额
	 * 
	 * @param rdVO
	 */
	private void syncExpAccident(ReceiveDetailVO rdVO) {
		// 2013-8-20，如果是客户索赔的单据，那么需要同步更新异常事故中的相应的索赔金额，未索赔金额
		if (rdVO.getRece_type().intValue() == ReceiveDetailConst.CUST_CLAIMANT_TYPE) {
			// 找到相应的异常事故的记录
			if (StringUtils.isNotBlank(rdVO.getRelationid())) {
				ExpAccidentVO eaVO = NWDao.getInstance().queryByCondition(ExpAccidentVO.class, "pk_exp_accident=?",
						rdVO.getRelationid());
				if (eaVO != null) {
					// 对于客户索赔，应收明细是负数，但是异常事故使用正数
					eaVO.setCust_unclaimant_amount(new UFDouble(Math.abs(rdVO.getUngot_amount().doubleValue())));
					eaVO.setCust_claimanted_amount(new UFDouble(Math.abs(rdVO.getGot_amount().doubleValue())));
					eaVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(eaVO);
				}
			}
		}
	}

	public List<Map<String, Object>> doReceivableAll(ParamVO paramVO, String[] pk_receive_detail) {
		logger.info("执行全部收款动作...");
		if (pk_receive_detail == null || pk_receive_detail.length == 0) {
			return null;
		}
		List<SuperVO> list = new ArrayList<SuperVO>();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (String pk : pk_receive_detail) {
			ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?", pk);
			if (rdVO == null) {
				throw new BusiException("应收明细已经被删除！");
			}
			if (rdVO.getVbillstatus().intValue() != BillStatus.RD_CONFIRM
					&& rdVO.getVbillstatus().intValue() != BillStatus.RD_PART_CAVLOAN) {
				throw new BusiException("应收明细必须是[确认,部分核销]状态才能执行全额收款！");
			}

			// 插入收款记录
			ReceRecordVO rrVO = new ReceRecordVO();
			rrVO.setDbilldate(new UFDate());
			rrVO.setVbillno(rdVO.getVbillno());
			rrVO.setReceivable_type(0);// 直接收款
			rrVO.setRelationid(rdVO.getPk_receive_detail());
			rrVO.setReceivable_amount(rdVO.getUngot_amount());// 这里不能等于总金额，可能是部分核销的单据进行全额收款
			rrVO.setReceivable_date(new UFDate(new Date()));
			rrVO.setReceivable_man(WebUtils.getLoginInfo().getPk_user());
			rrVO.setReceivable_method(0);// 默认现金收款
			rrVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			rrVO.setCreate_time(new UFDateTime(new Date()));
			rrVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(rrVO);
			toBeUpdate.add(rrVO);

			// 设置未收款金额
			rdVO.setUngot_amount(UFDouble.ZERO_DBL);
			// 设置已收款金额
			rdVO.setGot_amount(rdVO.getCost_amount());
			rdVO.setVbillstatus(BillStatus.RD_CAVLOAN);// 已核销
			rdVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(rdVO);
			list.add(rdVO);

			syncExpAccident(rdVO);
		}
		dao.saveOrUpdate(toBeUpdate);

		// 执行公式后返回
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO, list);
		list.clear();
		return mapList;
	}

	public Map<String, Object> deleteReceRecord(ParamVO paramVO, String pk_rece_record) {
		logger.info("删除收款纪录，pk_rece_record：" + pk_rece_record);
		if (StringUtils.isBlank(pk_rece_record)) {
			throw new BusiException("请先选择收款纪录！");
		}
		ReceRecordVO rrVO = dao.queryByCondition(ReceRecordVO.class, "pk_rece_record=?", pk_rece_record);
		if (rrVO == null) {
			logger.error("该收款纪录已经被删除,pk_rece_record:" + pk_rece_record);
			throw new BusiException("该收款纪录已经被删除！");
		}
		if (rrVO.getReceivable_type().intValue() == ReceiveDetailConst.RECEIVABLE_TYPE.CHECKSHEET.intValue()) {
			logger.error("不能删除收款类型是对账收款的记录,pk_rece_record:" + pk_rece_record);
			throw new BusiException("不能删除收款类型是对账收款的记录！");
		}
		ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?", rrVO.getRelationid());
		if (rdVO == null) {
			throw new BusiException("该收款纪录所对应的应收明细已经被删除！");
		}
		UFDouble receivable_amount = rrVO.getReceivable_amount() == null ? UFDouble.ZERO_DBL
				: rrVO.getReceivable_amount();
		UFDouble ungot_amount = rdVO.getUngot_amount() == null ? UFDouble.ZERO_DBL : rdVO.getUngot_amount();
		rdVO.setUngot_amount(ungot_amount.add(receivable_amount));// 加入到未收款额中
		UFDouble got_amount = rdVO.getGot_amount() == null ? UFDouble.ZERO_DBL : rdVO.getGot_amount();
		rdVO.setGot_amount(got_amount.sub(receivable_amount));// 已收款额必须减去该记录的收款额
		UFDouble cost_amount = rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount();
		if (rdVO.getUngot_amount().doubleValue() == cost_amount.doubleValue()) {
			// 如果未收款额和总金额相等，说明该记录完全没有收款。那么将该记录的状态置为已确认
			rdVO.setVbillstatus(BillStatus.RD_CONFIRM);
		} else {
			rdVO.setVbillstatus(BillStatus.RD_PART_CAVLOAN);
		}
		rdVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(rdVO);
		// 删除收款纪录
		dao.delete(rrVO);
		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(rdVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}

	public List<Map<String, Object>> buildReceCheckSheet(ParamVO paramVO, String json) {
		logger.info("生成对账单.");
		if (StringUtils.isBlank(json)) {
			throw new BusiException("生成对账单时参数不能为空！");
		}
		List<SuperVO> list = new ArrayList<SuperVO>();
		JsonNode header = JacksonUtils.readTree(json);
		if (header.size() > 0) {
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			
			String checkType = header.get(0).get("check_type").getValueAsText();
			String checkCorp = header.get(0).get("check_corp").getValueAsText();
			ReceCheckSheetVO parentVO = new ReceCheckSheetVO();
			if(StringUtils.isNotBlank(checkType) && !checkType.equals("null")){
				String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b WITH(NOLOCK) "
						+ "LEFT JOIN nw_data_dict WITH(NOLOCK) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
						+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  "
						+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
				String value = NWDao.getInstance().queryForObject(sql, String.class,checkType,checkType);
				if(!checkType.equals(value)){
					json = json.replace(checkType, value);
				}
			}
			
			if(StringUtils.isNotBlank(checkCorp) && !checkCorp.equals("null")){
				String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b WITH(NOLOCK) "
						+ "LEFT JOIN nw_data_dict WITH(NOLOCK) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
						+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='check_company'  "
						+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
				String value = NWDao.getInstance().queryForObject(sql, String.class,checkCorp,checkCorp);
				if(!checkCorp.equals(value)){
					json = json.replace(checkType, value);
				}
			}
			
			JsonNode newHeader = JacksonUtils.readTree(json).get(0);
			parentVO = (ReceCheckSheetVO) JacksonUtils.readValue(newHeader, ReceCheckSheetVO.class);
			if (parentVO.getDbilldate() == null) {
				parentVO.setDbilldate(new UFDate());
			}
//			parentVO.setCheck_head(custService.getCheckHead(parentVO.getBala_customer()));
			parentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			parentVO.setCreate_time(new UFDateTime(new Date()));
			parentVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			ParamVO newParamVO = new ParamVO();
			newParamVO.setFunCode(FunConst.RECE_CHECK_SHEET_CODE);
			parentVO.setVbillno(null);
			//yaojiie 重新获得单据类型的方法，解决YSDZ和YFDZ显示错误问题因为this.getBillType 总是获得当前IMPL的单据类型问题
			this.setCodeFieldRorBuildReceCheckSheet(parentVO, newParamVO);// 单据号必须重新生成
			parentVO.setVbillstatus(BillStatus.NEW);// 对账单处于新建状态
			parentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(parentVO);
			toBeUpdate.add(parentVO);
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			for (int i = 0; i < header.size(); i++) {
				JsonNode obj = header.get(i);
				ReceCheckSheetBVO childVO = (ReceCheckSheetBVO) JacksonUtils.readValue(obj, ReceCheckSheetBVO.class);
				childVO.setPk_rece_check_sheet(parentVO.getPrimaryKey());
				childVO.setStatus(VOStatus.NEW);
				this.addSuperVO(childVO);
				// 更改应收明细为已对账
				ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
						childVO.getPk_receive_detail());
				if (rdVO.getVbillstatus().intValue() != BillStatus.RD_CONFIRM) {
					throw new BusiException("只有[确认]状态的应收明细才能生成对账单！");
				}
				// 汇总金额
				UFDouble child_cost_amount = rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP);
				cost_amount = cost_amount.add(child_cost_amount);
				rdVO.setVbillstatus(BillStatus.RD_CHECK);
				rdVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(rdVO);
				list.add(rdVO);
			}
			parentVO.setCost_amount(cost_amount.setScale(2, UFDouble.ROUND_HALF_UP));
			parentVO.setUngot_amount(cost_amount.setScale(2, UFDouble.ROUND_HALF_UP));// 未收金额等于总金额
			dao.saveOrUpdate(toBeUpdate);
		}
		// 执行公式后返回
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO, list);
		list.clear();
		return mapList;
	}

	public List<Map<String, Object>> addToReceCheckSheet(ParamVO paramVO, String pk_rece_check_sheet,
			String[] pk_receive_detail) {
		logger.info("将应收明细添加到对账单中，pk_rece_check_sheet" + pk_rece_check_sheet);
		if (pk_receive_detail == null || pk_receive_detail.length == 0 || pk_rece_check_sheet == null) {
			return null;
		}
		ReceCheckSheetVO rcsVO = dao.queryByCondition(ReceCheckSheetVO.class, "pk_rece_check_sheet=?",
				pk_rece_check_sheet);
		if (rcsVO == null) {
			return null;
		}
		List<SuperVO> list = new ArrayList<SuperVO>();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		// 对账单的总金额及未收款金额
		UFDouble cost_amount = rcsVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rcsVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP);
		UFDouble ungot_amount = rcsVO.getUngot_amount() == null ? UFDouble.ZERO_DBL : rcsVO.getUngot_amount().setScale(2, UFDouble.ROUND_HALF_UP);
		for (String pk : pk_receive_detail) {
			ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?", pk);
			if (rdVO == null) {
				continue;
			}
			if (rdVO.getVbillstatus().intValue() != BillStatus.RD_CONFIRM) {
				throw new BusiException("只有[确认]状态的应收明细才能加入对账单！");
			}
			// 加入应收对账明细表
			ReceCheckSheetBVO childVO = new ReceCheckSheetBVO();
			childVO.setPk_rece_check_sheet(rcsVO.getPk_rece_check_sheet());
			childVO.setPk_receive_detail(rdVO.getPk_receive_detail());
			childVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(childVO);
			toBeUpdate.add(childVO);

			// 更新应收明细主表的总金额
			cost_amount = cost_amount.add(rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP));
			ungot_amount = ungot_amount.add(rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount().setScale(2, UFDouble.ROUND_HALF_UP));

			// 修改应收明细为已对账
			rdVO.setVbillstatus(BillStatus.RD_CHECK);
			rdVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(rdVO);
			list.add(rdVO);
		}
		rcsVO.setCost_amount(cost_amount.setScale(2, UFDouble.ROUND_HALF_UP));
		rcsVO.setUngot_amount(ungot_amount.setScale(2, UFDouble.ROUND_HALF_UP));
		rcsVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(rcsVO);
		dao.saveOrUpdate(toBeUpdate);
		// 执行公式后返回
		List<Map<String, Object>> mapList = execFormula4Templet(paramVO, list);
		list.clear();
		return mapList;
	}

	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, final String orderBy) {
		//yaojiie 2015 12 11 修改执行公式后方法，将大量原本用显示公式执行的显示方法，使用SQL查询，加快查询和导出速度
		super.processAfterExecFormula(list, paramVO, orderBy);
		Set<String> invoice_vbillnos = new HashSet<String>();
		Set<String> pk_invoices = new HashSet<String>();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> transbilityResults = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : list) {
			Object invoice_vbillno = map.get("invoice_vbillno");
			if (invoice_vbillno != null) {
				invoice_vbillnos.add(invoice_vbillno.toString());
			}
		}
		//查询结果集
		if (invoice_vbillnos != null && invoice_vbillnos.size() > 0) {
			String cond = NWUtils.buildConditionString(invoice_vbillnos.toArray(new String[invoice_vbillnos.size()]));
			String sql = "SELECT inv.vbillno AS invoice_vbillno,inv.cust_orderno,inv.orderno, "
					+ "inv.req_deli_date,inv.req_arri_date,inv.act_deli_date,inv.act_arri_date, "
					+ "inv.pk_delivery,deli_addr.addr_name AS deli_addr_name,deli_area.name AS deli_city_name,inv.deli_province,inv.deli_area,inv.deli_detail_addr,inv.deli_contact,inv.deli_mobile,inv.deli_phone,inv.deli_email, "
					+ "inv.pk_arrival,arri_addr.addr_name AS arri_addr_name,arri_area.name AS arri_city_name,inv.arri_province,inv.arri_area,inv.arri_detail_addr,inv.arri_contact,inv.arri_mobile,inv.arri_phone,inv.arri_email, "
					+ "inv.vbillstatus AS invoice_vbillstatus,inv.memo AS invoice_memo,tp.name AS trans_type_name,inv.deli_method,inv.tracking_status,inv.tracking_memo,inv.distance,inv.if_return,inv.urgent_level,inv.if_customs_official, "
					+ "inv.pk_psndoc,nw_psndoc.psnname,inv.pk_supplier,ts_supplier.supp_name "
					+ "FROM ts_invoice inv WITH(NOLOCK) "
					+ "LEFT JOIN ts_trans_type tp WITH(NOLOCK) ON inv.pk_trans_type = tp.pk_trans_type AND isnull(tp.locked_flag,'N')='N' and isnull(tp.dr,0)=0 " 
					+ "LEFT JOIN ts_address deli_addr WITH(NOLOCK) ON inv.pk_delivery = deli_addr.pk_address AND  isnull(deli_addr.locked_flag,'N')='N' and isnull(deli_addr.dr,0)=0 "
					+ "LEFT JOIN ts_address arri_addr WITH(NOLOCK) ON inv.pk_arrival = arri_addr.pk_address AND  isnull(arri_addr.locked_flag,'N')='N' and isnull(arri_addr.dr,0)=0 "
					+ "LEFT JOIN ts_area deli_area WITH(NOLOCK) ON inv.deli_city = deli_area.pk_area AND isnull(deli_area.locked_flag,'N')='N' and isnull(deli_area.dr,0)=0 "
					+ "LEFT JOIN ts_area arri_area WITH(NOLOCK) ON inv.arri_city = arri_area.pk_area AND isnull(arri_area.locked_flag,'N')='N' and isnull(arri_area.dr,0)=0 "
					+ "LEFT JOIN nw_psndoc WITH(NOLOCK) ON inv.pk_psndoc = nw_psndoc.pk_psndoc AND isnull(nw_psndoc.locked_flag,'N')='N' and isnull(nw_psndoc.dr,0)=0 "
					+ "LEFT JOIN ts_supplier WITH(NOLOCK) ON inv.pk_supplier = ts_supplier.pk_supplier AND isnull(ts_supplier.locked_flag,'N')='N' and isnull(ts_supplier.dr,0)=0 "
					+ "WHERE  isnull(inv.dr,0)=0 AND inv.vbillno in " + cond;
			if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
				sql = "SELECT inv.vbillno AS invoice_vbillno,inv.cust_orderno,inv.orderno, "
						+ "inv.req_deli_date,inv.req_arri_date,inv.act_deli_date,inv.act_arri_date, "
						+ "inv.pk_delivery,deli_addr.addr_name AS deli_addr_name,deli_area.name AS deli_city_name,inv.deli_province,inv.deli_area,inv.deli_detail_addr,inv.deli_contact,inv.deli_mobile,inv.deli_phone,inv.deli_email, "
						+ "inv.pk_arrival,arri_addr.addr_name AS arri_addr_name,arri_area.name AS arri_city_name,inv.arri_province,inv.arri_area,inv.arri_detail_addr,inv.arri_contact,inv.arri_mobile,inv.arri_phone,inv.arri_email, "
						+ "inv.vbillstatus AS invoice_vbillstatus,inv.memo AS invoice_memo,tp.name AS trans_type_name,inv.deli_method,inv.tracking_status,inv.tracking_memo,inv.distance,inv.if_return,inv.urgent_level,inv.if_customs_official, "
						+ "inv.pk_psndoc,nw_psndoc.psnname,inv.pk_supplier,ts_supplier.supp_name "
						+ "FROM ts_invoice inv  "
						+ "LEFT JOIN ts_trans_type tp  ON inv.pk_trans_type = tp.pk_trans_type AND isnull(tp.locked_flag,'N')='N' and isnull(tp.dr,0)=0 " 
						+ "LEFT JOIN ts_address deli_addr ON inv.pk_delivery = deli_addr.pk_address AND  isnull(deli_addr.locked_flag,'N')='N' and isnull(deli_addr.dr,0)=0 "
						+ "LEFT JOIN ts_address arri_addr  ON inv.pk_arrival = arri_addr.pk_address AND  isnull(arri_addr.locked_flag,'N')='N' and isnull(arri_addr.dr,0)=0 "
						+ "LEFT JOIN ts_area deli_area  ON inv.deli_city = deli_area.pk_area AND isnull(deli_area.locked_flag,'N')='N' and isnull(deli_area.dr,0)=0 "
						+ "LEFT JOIN ts_area arri_area  ON inv.arri_city = arri_area.pk_area AND isnull(arri_area.locked_flag,'N')='N' and isnull(arri_area.dr,0)=0 "
						+ "LEFT JOIN nw_psndoc ON inv.pk_psndoc = nw_psndoc.pk_psndoc AND isnull(nw_psndoc.locked_flag,'N')='N' and isnull(nw_psndoc.dr,0)=0 "
						+ "LEFT JOIN ts_supplier ON inv.pk_supplier = ts_supplier.pk_supplier AND isnull(ts_supplier.locked_flag,'N')='N' and isnull(ts_supplier.dr,0)=0 "
						+ "WHERE  isnull(inv.dr,0)=0 AND inv.vbillno in " + cond;
			}
			results = NWDao.getInstance().queryForList(sql);
		}	
		
		for (Map<String, Object> result : results) {
			Object pk_invoice = result.get("pk_invoice");
			if (pk_invoice != null) {
				String[] pks = pk_invoice.toString().split("\\" + Constants.SPLIT_CHAR);
				pk_invoices.addAll(Arrays.asList(pks));
			}
		}
		
		//从关联表中，获得车辆信息
		if (pk_invoices != null && pk_invoices.size() > 0) {
			String cond = NWUtils.buildConditionString(pk_invoices.toArray(new String[pk_invoices.size()]));
			String sql = "SELECT tb.pk_invoice,tb.pk_car_type,ts_car_type.name AS car_type_name,teb.num AS car_num, "
					+ "FROM ts_trans_bility_b AS tb WITH(NOLOCK) "
					+ "LEFT JOIN ts_car_type WITH(NOLOCK)  ON tb.pk_car_type = ts_car_type.pk_car_type AND isnull(ts_car_type.locked_flag,'N')='N' and isnull(ts_car_type.dr,0)=0 "
					+ "WHERE isnull(tb.dr,0)=0 and tb.pk_invoice in " + cond;

			transbilityResults = NWDao.getInstance().queryForList(sql);
		}
		
		//将结果集和原数据进行匹配，并放入原数据集中
		for (Map<String, Object> map : list) {
			for (Map<String, Object> result : results) {
				if (result.get("invoice_vbillno").equals(map.get("invoice_vbillno"))) {
					map.put("cust_orderno", result.get("cust_orderno"));
					map.put("orderno", result.get("orderno"));
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
					map.put("invoice_vbillstatus", result.get("invoice_vbillstatus"));
					map.put("invoice_memo", result.get("invoice_memo"));
					map.put("trans_type_name", result.get("trans_type_name"));
					map.put("tracking_memo", result.get("tracking_memo"));
					map.put("distance", result.get("distance"));
					
					map.put("deli_method", result.get("deli_method"));
					map.put("if_return", result.get("if_return"));
					map.put("urgent_level", result.get("urgent_level"));
					map.put("if_customs_official", result.get("if_customs_official"));
					map.put("pk_psndoc", result.get("pk_psndoc"));
					map.put("psnname", result.get("psnname"));
					map.put("pk_supplier", result.get("pk_supplier"));
					map.put("supp_name", result.get("supp_name"));


					map.put("pk_invoice", result.get("pk_invoice"));
					Object pk_entrust = map.get("pk_invoice");
					StringBuffer car_type_name = new StringBuffer();
					StringBuffer car_num = new StringBuffer();

					//处理会有拼接情况的数据
					if (pk_entrust != null) {
						if (transbilityResults != null && transbilityResults.size() > 0) {
							for (Map<String, Object> transbilityResult : transbilityResults) {
								// carType_name
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("car_type_name")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("car_type_name").toString())) {
									car_type_name.append(transbilityResult.get("car_type_name"))
											.append(Constants.SPLIT_CHAR);
								}
								// car_num
								if (pk_entrust.equals(transbilityResult.get("pk_entrust"))&&transbilityResult.get("car_num")!=null
										&& StringUtils.isNotBlank(transbilityResult.get("car_num").toString())) {
									car_num.append(transbilityResult.get("car_num")).append(Constants.SPLIT_CHAR);
								}
							}
							if (StringUtils.isNotBlank(car_type_name.toString())) {
								String container = car_type_name.substring(0, car_type_name.length() - 1);
								map.put("car_type_name", container);
							}
							if (StringUtils.isNotBlank(car_num.toString())) {
								String container = car_num.substring(0, car_num.length() - 1);
								map.put("car_num", container);
							}
						}
					}
				}
			}
		}
		
		if (StringUtils.isBlank(orderBy)) {
			Collections.sort(list, new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> p1, Map<String, Object> p2) {
					Object v1 = p1.get("req_deli_date");
					Object v2 = p2.get("req_deli_date");
					if (v1 == null) {
						return 0;
					}
					if (v2 == null) {
						return 1;
					}
					UFDateTime dt1 = new UFDateTime(v1.toString());
					UFDateTime dt2 = new UFDateTime(v2.toString());
					return dt1.compareTo(dt2);
				}
			});
		}
	}

	/**
	 * 设置了件数，重量，体积后，重新计算金额
	 * 
	 * @param rdVO
	 */
	public void recomputeHeaderAmount(ReceiveDetailVO rdVO) {
		if (rdVO == null) {
			return;
		}
		if (rdVO.getVbillstatus() != BillStatus.NEW) {
			throw new BusiException("只有[新建]状态的应收明细才能重新计算金额！");
		}
		ReceDetailBVO[] rdBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceDetailBVO.class,
				"pk_receive_detail=?", rdVO.getPk_receive_detail());
		if (rdBVOs != null && rdBVOs.length > 0) {
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			for (int i = 0; i < rdBVOs.length; i++) {
				if (rdBVOs[i].getQuote_type().intValue() == QuoteTypeConst.INTERVAL) {// 区间报价
					if (rdBVOs[i].getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {// 单价
						if (rdBVOs[i].getValuation_type() != null) {
							int valuationType = rdBVOs[i].getValuation_type().intValue();
							UFDouble price = rdBVOs[i].getPrice() == null ? UFDouble.ZERO_DBL : rdBVOs[i].getPrice();
							if (valuationType == ValuationTypeConst.NUM) {
								// 数量
								rdBVOs[i].setAmount(price.multiply(rdVO.getNum_count().intValue()));
							} else if (valuationType == ValuationTypeConst.WEIGHT) {
								// 重量（实际上是计费重）
								rdBVOs[i].setAmount(price.multiply(rdVO.getFee_weight_count()));
							} else if (valuationType == ValuationTypeConst.VOLUME) {
								// 体积
								rdBVOs[i].setAmount(price.multiply(rdVO.getVolume_count()));
							}
							rdBVOs[i].setStatus(VOStatus.UPDATED);
							NWDao.getInstance().saveOrUpdate(rdBVOs[i]);
						}
					}
				}
				cost_amount.add(rdBVOs[i].getAmount());
			}
			rdVO.setCost_amount(cost_amount);
			rdVO.setUngot_amount(cost_amount);
			rdVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(rdVO);
		}
	}
	
	@Override
	public SuperVO[] batchConfirm(ParamVO paramVO, String[] ids) {
		
		String msg = receCheckProc(NWUtils.join(ids, ","), 0);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		
		List<SuperVO> parentVOs = new ArrayList<SuperVO>();
		ReceiveDetailVO[] receVOs = getByPrimaryKeys(ReceiveDetailVO.class, ids);
		//找出分公司结算的单据的明细，处理分公司结算，只需要这部分数据就可以了。
		String sql = "SELECT ts_rece_detail_b.*  "
				+ " FROM ts_rece_detail_b WITH(NOLOCK) "
				+ " LEFT JOIN ts_receive_detail WITH(NOLOCK) ON ts_rece_detail_b.pk_receive_detail = ts_receive_detail.pk_receive_detail "
				+ " LEFT JOIN ts_invoice WITH(NOLOCK) ON ts_invoice.vbillno = ts_receive_detail.invoice_vbillno "
				+ " WHERE isnull(ts_receive_detail.dr,0)=0 AND isnull(ts_invoice.dr,0)=0 AND isnull(ts_rece_detail_b.dr,0)=0  "
				+ " AND ts_invoice.invoice_origin = 3 AND ts_receive_detail.pk_receive_detail IN " + NWUtils.buildConditionString(ids);
		List<ReceDetailBVO> receBVOs = NWDao.getInstance().queryForList(sql, ReceDetailBVO.class);
		//对receBVOs按照pk_receive_detail进行分组
		Map<String,List<ReceDetailBVO>> groupMap = new HashMap<String,List<ReceDetailBVO>>();
		for(ReceDetailBVO receBVO : receBVOs){
			String key = receBVO.getPk_receive_detail();
			List<ReceDetailBVO> voList = groupMap.get(key);
			if(voList == null){
				voList = new ArrayList<ReceDetailBVO>();
				groupMap.put(key, voList);
			}
			voList.add(receBVO);
		}
		if(receVOs == null || receVOs.length == 0){
			throw new RuntimeException("请选择单据！");
		}
		for(ReceiveDetailVO receVO : receVOs){
			if(BillStatus.NEW != receVO.getVbillstatus() && BillStatus.RD_CONFIRMING != receVO.getVbillstatus()){
				throw new RuntimeException("只有[新建]状态的单据才能进行确认！");
			}
			if(receVO.getBalatype() != null && DataDictConst.BALATYPE.ARRI_PAY.intValue() == receVO.getBalatype()){
				if(receVO.getCost_amount() == null || UFDouble.ZERO_DBL.equals(receVO.getCost_amount())){
					throw new BusiException("现金到付类应收明细确认时，金额不能为0！");
				}
			}
			receVO.setStatus(VOStatus.UPDATED);
			receVO.setAttributeValue(getBillStatusField(), getConfirmStatus()); // 设置成已确认
			receVO.setAttributeValue(getConfirmTimeField(), new UFDateTime(new Date()));
			receVO.setAttributeValue(getConfirmUserField(), WebUtils.getLoginInfo().getPk_user());
			ExAggReceiveDetailVO aggVO = new ExAggReceiveDetailVO();
			aggVO.setParentVO(receVO);
			List<ReceDetailBVO> childrenVOs = groupMap.get(receVO.getPk_receive_detail());
			if(childrenVOs != null && childrenVOs.size() > 0){
				aggVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B, childrenVOs.toArray(new ReceDetailBVO[childrenVOs.size()]));
			}
			ExAggPayDetailVO exAggPayDetailVO = this.syncPayDetailByConfirm(aggVO);
			if(exAggPayDetailVO != null){
				ParamVO vo = new ParamVO();
				vo.setFunCode(FunConst.PAY_DETAIL_CODE);
				payDetailService.save(exAggPayDetailVO, vo);
			}
			parentVOs.add(receVO);
		}
		NWDao.getInstance().saveOrUpdate(parentVOs);
		return parentVOs.toArray(new SuperVO[parentVOs.size()]);
	}
	
	/**
	 * 在应收明细确认，反确认 ，提交，撤销提交的时候，执行存储过程检查。
	 * @param ids
	 * @param procName
	 * @param type 0确认，1反确认，2提交，3反提交
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String receCheckProc(String ids,Integer type){
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = "ts_ysmx_check_proc";
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

	public AggregatedValueObject confirm(ParamVO paramVO) {
		logger.info("执行单据确认动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		ReceiveDetailVO parentVO = (ReceiveDetailVO) billVO.getParentVO();
		if(parentVO.getBalatype() != null && DataDictConst.BALATYPE.ARRI_PAY.intValue() == parentVO.getBalatype()){
			ExAggReceiveDetailVO aggReceiveDetailVO = (ExAggReceiveDetailVO)billVO;
			ReceDetailBVO[] rdBVOs = (ReceDetailBVO[])aggReceiveDetailVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
			UFDouble amount = UFDouble.ZERO_DBL;
			if(rdBVOs != null && rdBVOs.length > 0){
				for(ReceDetailBVO rdBVO : rdBVOs){
					amount = amount.add(rdBVO.getAmount() == null ? UFDouble.ZERO_DBL : rdBVO.getAmount());
				}
			}
			if(UFDouble.ZERO_DBL.equals(amount)){
				throw new BusiException("现金到付类应收明细确认时，金额不能为0！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), getConfirmStatus()); // 设置成已确认
		parentVO.setAttributeValue(getConfirmTimeField(), new UFDateTime(new Date()));
		parentVO.setAttributeValue(getConfirmUserField(), WebUtils.getLoginInfo().getPk_user());
		//对于这个应付明细对应的发货单类型如果是分公司结算的，需要找到这个应付对应的应收，将应付的金额更新。
		ExAggReceiveDetailVO aggReceiveDetailVO = (ExAggReceiveDetailVO)billVO;
		ExAggPayDetailVO exAggPayDetailVO = this.syncPayDetailByConfirm(aggReceiveDetailVO);
		if(exAggPayDetailVO != null){
			ParamVO vo = new ParamVO();
			vo.setFunCode(FunConst.PAY_DETAIL_CODE);
			payDetailService.save(exAggPayDetailVO, vo);
		}
		dao.saveOrUpdate(billVO);
		return billVO;
	}
	
	// yaojiie 2016 1 24 分公司结算时当应收明细确认时，会影响对应应付明细。
	public ExAggPayDetailVO syncPayDetailByConfirm(ExAggReceiveDetailVO exAggReceiveDetailVO) {
		ReceiveDetailVO receiveDetailVO = (ReceiveDetailVO) exAggReceiveDetailVO.getParentVO();
		InvoiceVO[] invoiceVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, "vbillno =? ",
				receiveDetailVO.getInvoice_vbillno());
		// 当应收对应的发货单为空时，不需要执行下面的步骤了。
		if (invoiceVOs == null || invoiceVOs.length == 0) {
			return null;
		}
		if (invoiceVOs[0].getInvoice_origin() == null
				|| DataDictConst.INVOICE_ORIGIN.FGS.intValue() != invoiceVOs[0].getInvoice_origin()) {
			return null;
		}
		// 将应付明细更新掉
		ReceDetailBVO[] receDetailBVOs = (ReceDetailBVO[]) exAggReceiveDetailVO
				.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		CustomerVO customerVO = getByPrimaryKey(CustomerVO.class, invoiceVOs[0].getBala_customer());
		ExAggPayDetailVO exAggPayDetailVO = new ExAggPayDetailVO();
		List<PayDetailBVO> payDetailBVOs = new ArrayList<PayDetailBVO>();
		PayDetailVO unitPayDetailVO = new PayDetailVO();

		if (receDetailBVOs != null && receDetailBVOs.length > 0) {
			for (ReceDetailBVO receDetailBVO : receDetailBVOs) {
				PayDetailBVO newPayDetailBVO = new PayDetailBVO();
				newPayDetailBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(newPayDetailBVO);
				newPayDetailBVO.setPk_pay_detail(unitPayDetailVO.getPk_pay_detail());
				newPayDetailBVO.setPk_expense_type(receDetailBVO.getPk_expense_type());
				newPayDetailBVO.setQuote_type(receDetailBVO.getQuote_type());
				newPayDetailBVO.setPrice_type(receDetailBVO.getPrice_type());
				newPayDetailBVO.setValuation_type(receDetailBVO.getValuation_type());
				newPayDetailBVO.setPrice(receDetailBVO.getPrice());
				newPayDetailBVO.setAmount(receDetailBVO.getAmount());
				newPayDetailBVO.setMemo(receDetailBVO.getMemo());
				newPayDetailBVO.setSystem_create(UFBoolean.TRUE);
				newPayDetailBVO.setRate_id(receDetailBVO.getRate_id());
				newPayDetailBVO.setContract_amount(receDetailBVO.getContract_amount());
				newPayDetailBVO.setPk_contract_b(receDetailBVO.getPk_contract_b());

				newPayDetailBVO.setDef1(receDetailBVO.getDef1());
				newPayDetailBVO.setDef2(receDetailBVO.getDef2());
				newPayDetailBVO.setDef3(receDetailBVO.getDef3());
				newPayDetailBVO.setDef4(receDetailBVO.getDef4());
				newPayDetailBVO.setDef5(receDetailBVO.getDef5());
				newPayDetailBVO.setDef6(receDetailBVO.getDef6());
				newPayDetailBVO.setDef7(receDetailBVO.getDef7());
				newPayDetailBVO.setDef8(receDetailBVO.getDef8());
				newPayDetailBVO.setDef9(receDetailBVO.getDef9());
				newPayDetailBVO.setDef10(receDetailBVO.getDef10());
				newPayDetailBVO.setDef11(receDetailBVO.getDef11());
				newPayDetailBVO.setDef12(receDetailBVO.getDef12());
				payDetailBVOs.add(newPayDetailBVO);
			}
		}
		// 原始类型的应收明细需要更新过去，而非原始类型的需要新建一个
		if (receiveDetailVO.getRece_type() == ReceiveDetailConst.ORIGIN_TYPE) {
			// 这个单据是分公司结算产生的，找到对应的委托单，更新其应付明细
			String sql = "select * from ts_pay_detail with(nolock) where isnull(dr,0) = 0 and entrust_vbillno =? "
					+ "and pay_type =? ";
			List<PayDetailVO> payDetailVOs = NWDao.getInstance().queryForList(sql, PayDetailVO.class,
					invoiceVOs[0].getOrderno(), PayDetailConst.ORIGIN_TYPE);
			if (payDetailVOs == null || payDetailVOs.size() == 0) {
				throw new BusiException("应付明细已经被删除！");
			} else if (payDetailVOs.get(0).getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应付明细不是新建状态！");
			}
			unitPayDetailVO = payDetailVOs.get(0);
			unitPayDetailVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail());
			unitPayDetailVO.setStatus(VOStatus.UPDATED);
		} else {
			PayDetailVO payDetailVO = new PayDetailVO();
			payDetailVO.setStatus(VOStatus.NEW);
			payDetailVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail());
			NWDao.setUuidPrimaryKey(payDetailVO);
			payDetailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
			payDetailVO.setVbillstatus(BillStatus.NEW);
			payDetailVO.setPk_carrier(receiveDetailVO.getPk_customer());
			payDetailVO.setEntrust_vbillno(invoiceVOs[0].getOrderno());
			payDetailVO.setBalatype(receiveDetailVO.getBalatype());
			payDetailVO.setCurrency(receiveDetailVO.getCurrency());
			payDetailVO.setCost_amount(receiveDetailVO.getCost_amount());
			payDetailVO.setGot_amount(receiveDetailVO.getGot_amount());
			payDetailVO.setUngot_amount(receiveDetailVO.getUngot_amount());
			payDetailVO.setMemo(receiveDetailVO.getMemo());
			payDetailVO.setMerge_type(receiveDetailVO.getMerge_type());
			payDetailVO.setCreate_time(new UFDateTime(new Date()));
			payDetailVO.setCreate_user(receiveDetailVO.getCreate_user());
			payDetailVO.setPk_corp(customerVO.getBranch_company());
			payDetailVO.setRelationid(receiveDetailVO.getRelationid());
			payDetailVO.setDbilldate(receiveDetailVO.getDbilldate());
			payDetailVO.setPay_type(receiveDetailVO.getRece_type());
			payDetailVO.setDef1(receiveDetailVO.getDef1());
			payDetailVO.setDef2(receiveDetailVO.getDef2());
			payDetailVO.setDef3(receiveDetailVO.getDef3());
			payDetailVO.setDef4(receiveDetailVO.getDef4());
			payDetailVO.setDef5(receiveDetailVO.getDef5());
			payDetailVO.setDef6(receiveDetailVO.getDef6());
			payDetailVO.setDef7(receiveDetailVO.getDef7());
			payDetailVO.setDef8(receiveDetailVO.getDef8());
			payDetailVO.setDef9(receiveDetailVO.getDef9());
			payDetailVO.setDef10(receiveDetailVO.getDef10());
			payDetailVO.setDef11(receiveDetailVO.getDef11());
			payDetailVO.setDef12(receiveDetailVO.getDef12());
			unitPayDetailVO = payDetailVO;
		}
		exAggPayDetailVO.setParentVO(unitPayDetailVO);
		exAggPayDetailVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B,
				payDetailBVOs.toArray(new PayDetailBVO[payDetailBVOs.size()]));
		return exAggPayDetailVO;
	}
	
	@Override
	public void processBeforeBatchUnconfirm(ParamVO paramVO, SuperVO[] parentVOs) {
		super.processBeforeBatchUnconfirm(paramVO, parentVOs);
		for(SuperVO parentVO : parentVOs){
			ReceiveDetailVO receiveDetailVO = (ReceiveDetailVO) parentVO;
			ExAggPayDetailVO exAggPayDetailVO = this.syncPayDetailByUnConfirm(receiveDetailVO);
			if(exAggPayDetailVO != null){
				ParamVO vo = new ParamVO();
				vo.setFunCode(FunConst.PAY_DETAIL_CODE);
				payDetailService.save(exAggPayDetailVO, vo);
			}
		}
	}
	
	// yaojiie 2016 1 24 分公司结算时当应收明细反确认时，会删除 对应应付明细。
	public ExAggPayDetailVO syncPayDetailByUnConfirm(ReceiveDetailVO receiveDetailVO){
		//反确认时，分两种，原始凭证反确认时要调用保存方法，调整凭证反确认需要调用删除方法
		//分公司结算类型等应收明细在进行反确认的时候需要将相应的应付明细删除和更新
		InvoiceVO[] invoiceVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, "vbillno =? ", receiveDetailVO.getInvoice_vbillno());
		if(invoiceVOs == null || invoiceVOs.length == 0){
			return null;
		}
		if(DataDictConst.INVOICE_ORIGIN.FGS.intValue() != invoiceVOs[0].getInvoice_origin()){
			return null;
		}
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "pk_receive_detail =?", receiveDetailVO.getPk_receive_detail());
		if(payDetailVOs == null || payDetailVOs.length == 0 || BillStatus.NEW != payDetailVOs[0].getVbillstatus()){
			throw new BusiException("对应应付明细已被删除或者确认，无法反确认单据！");
		}
		ExAggPayDetailVO aggPayDetailVO = new ExAggPayDetailVO();
		//一个pk_receive_detail只会有一个PayDetailVO
		if(PayDetailConst.ORIGIN_TYPE == payDetailVOs[0].getPay_type()){
			//如果是原始凭证，直接保存一个新的空应付明细即可
			PayDetailBVO[] payDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class, "pk_pay_detail =?", payDetailVOs[0].getPk_pay_detail());
			if(payDetailBVOs != null && payDetailBVOs.length > 0){
				for(PayDetailBVO payDetailBVO : payDetailBVOs){
					payDetailBVO.setStatus(VOStatus.DELETED);
				}
			}
			PayDeviBVO[] payDeviBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class, "pk_pay_detail =?", payDetailVOs[0].getPk_pay_detail());
			if(payDeviBVOs != null && payDeviBVOs.length > 0){
				for(PayDeviBVO payDeviBVO : payDeviBVOs){
					payDeviBVO.setStatus(VOStatus.DELETED);
				}
			}
			aggPayDetailVO.setParentVO(payDetailVOs[0]);
			aggPayDetailVO.setTableVO(TabcodeConst.TS_PAY_DEVI_B, payDeviBVOs);
			aggPayDetailVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, payDetailBVOs);
			return aggPayDetailVO;
		}else{
			//对于调整凭证，直接删除即可。
			String[] primaryKeys = new String[]{payDetailVOs[0].getPk_pay_detail()};
			try {
				payDetailService.batchDelete(PayDetailVO.class, primaryKeys);
			} catch (Exception e) {
				throw new BusiException("删除应付明细或委托单失败，无法反确认单据！");
			}
			return null;
		}
	}

	/**
	 * 提交后，单据属于确认中状态
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> commit(ParamVO paramVO) {
		
		String msg = receCheckProc(paramVO.getBillId(), 2);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		
		logger.info("执行单据提交动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if (oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if (BillStatus.NEW != billStatus) {
				throw new RuntimeException("只有[新建]状态的单据才能进行提交！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.RD_CONFIRMING); // 设置成确认中
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
		
		String msg = receCheckProc(paramVO.getBillId(), 3);
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		
		logger.info("执行单据反提交动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if (oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if (BillStatus.RD_CONFIRMING != billStatus) {
				throw new RuntimeException("只有[确认中]状态的单据才能进行反提交！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.NEW); // 设置成新建
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	@Autowired
	InvoiceService invoiceService;
	@Autowired
	ContractService contractService;
	
	
	public List<Map<String,Object>> reComputeMny(String[] billIds, ParamVO paramVO) {
		Map<Integer, List<String>> dispatcherResult = CostCalculateUtils.dispatcher(billIds, CostCalculateUtils.RD, "");
		CostCalculater calculater = new CostCalculater();
		List<SuperVO> receiveDetailVOs = new ArrayList<SuperVO>();
		for(Integer key : dispatcherResult.keySet()){
			if(key == CostCalculateUtils.RD_UNIT){
				Map<ReceiveDetailVO, List<ReceDetailBVO>> result = calculater.computeReceivable(dispatcherResult.get(key));
				if(result != null && result.size() > 0){
					receiveDetailVOs.addAll(result.keySet());
				}
			}else if(key == CostCalculateUtils.RD_YUSEN){
				List<String> ids =  dispatcherResult.get(key);
				if(ids != null && ids.size() > 0){
					YusenReceHandler receHandler = new YusenReceHandler();
					//Map<ReceiveDetailVO, List<ReceDetailBVO>> map = receHandler.compute(ids);
					receHandler.compute(ids);
				}
			}
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> datas = execFormula4Templet(paramVO, receiveDetailVOs);
		for(Map<String, Object> data : datas) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.HEADER, data);
			retList.add(map);
		}
		return retList;
	}
	
	/**
	 * 重新计算合同金额，必须是新建的应收明细，并且是原始单据
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public Map<String,Object> reComputeMny(AggregatedValueObject billVO, ParamVO paramVO) {
		if (billVO == null) {
			return null;
		}
		ExAggReceiveDetailVO rdAggVO = (ExAggReceiveDetailVO) billVO;
		ReceiveDetailVO rdVO = (ReceiveDetailVO) rdAggVO.getParentVO();
		if (rdVO.getVbillstatus() != BillStatus.NEW) {
			throw new BusiException("应收明细[?]必须是[新建]状态才能进行重算金额！",rdVO.getVbillno());
		}
		if (rdVO.getRece_type().intValue() != ReceiveDetailConst.ORIGIN_TYPE) {
			throw new BusiException("应收明细[?]必须是[原始单据]类型才能进行重算金额！",rdVO.getVbillno());
		}
		if (StringUtils.isBlank(rdVO.getInvoice_vbillno())) {
			throw new BusiException("应收明细[?]没有对应的发货单！",rdVO.getVbillno());
		}

		String sql = "select pk_invoice from ts_invoice where vbillno=?";
		String pk_invoice = NWDao.getInstance().queryForObject(sql, String.class, rdVO.getInvoice_vbillno());
		if (StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("应收明细[?]对应的发货单[?]已经不存在！",rdVO.getVbillno(),rdVO.getInvoice_vbillno());
		}

		ParamVO paramVO1 = new ParamVO();
		paramVO1.setBillId(pk_invoice);
		ExAggInvoiceVO invAggVO = (ExAggInvoiceVO) invoiceService.queryBillVO(paramVO1);
		List<SuperVO> updateList = new ArrayList<SuperVO>();
		InvoiceVO invVO = (InvoiceVO) invAggVO.getParentVO();
		List<ReceDetailBVO> newDetailBVOs = new ArrayList<ReceDetailBVO>();
		
		ReceDetailBVO[] oldDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceDetailBVO.class,
				"pk_receive_detail=?", rdVO.getPk_receive_detail());
		// milkrun业务的导入，这类业务导入时，不使用表头的路线信息进行匹配合同，需要将路线信息里的路线节点做拆分处理进行匹配合同
		// 1,查询运输方式为milkrun的PK值，进行匹配操作
		// 查询运输方式为milkRun的PK值，用来检查运输方式
		String transTypeSql = "select * from ts_trans_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?";
		TransTypeVO transTypeVO = dao.queryForObject(transTypeSql, TransTypeVO.class, TransTypeConst.TT_MR);
		List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
		if (transTypeVO != null && invVO.getPk_trans_type().equals(transTypeVO.getPk_trans_type())) {	
			// 获取所有的线路信息
			String accessRule = ParameterHelper.getMilkRunNodeAccessRule();
			String[] accessRules = accessRule.split("\\" + Constants.SPLIT_CHAR);
			String accessRulesCond = NWUtils.buildConditionString(accessRules);
			String invLineBSql = "select * from ts_inv_line_b with(nolock) where isnull(dr,0)=0 and pk_invoice ='"+ invVO.getPk_invoice() +"' and operate_type in "+accessRulesCond;
			List<InvLineBVO> listLineBVOs = NWDao.getInstance().queryForList(invLineBSql,InvLineBVO.class);
			InvLineBVO[] lineBVOs = listLineBVOs.toArray(new InvLineBVO[listLineBVOs.size()]);
			// 对线路信息进行分类
			List<InvLineBVO> points = new ArrayList<InvLineBVO>();
			List<InvLineBVO> bases = new ArrayList<InvLineBVO>();
			
			for (int i = 0; i < lineBVOs.length; i++) {
				if (i == 0) {
					// 第一个点必定是基点
					bases.add(lineBVOs[i]);
				} else {
					if (lineBVOs[i].getPk_address().equals(lineBVOs[i - 1].getPk_address())) {
						continue;
					} else {
						if (lineBVOs[i].getPk_city().equals(lineBVOs[i - 1].getPk_city())) {
							points.add(lineBVOs[i]);
						} else {
							bases.add(lineBVOs[i]);
						}
					}
				}
			}

			Map<String, List<InvLineBVO>> groupMap = new HashMap<String, List<InvLineBVO>>();
			for (InvLineBVO invLineBVO : points) {
				String key = invLineBVO.getPk_city();
				List<InvLineBVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<InvLineBVO>();
					groupMap.put(key, voList);
				}
				voList.add(invLineBVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			String expenseSql = "select * from ts_expense_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?";
			ExpenseTypeVO baseTypeVO = dao.queryForObject(expenseSql, ExpenseTypeVO.class, ExpenseTypeConst.ET110);
			ExpenseTypeVO pointTypeVO = dao.queryForObject(expenseSql, ExpenseTypeVO.class, ExpenseTypeConst.ET120);

			// 按条件分组完毕 计算基点费用
			TransBilityBVO[] tbBVOs = (TransBilityBVO[]) invAggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
			String[] pk_car_type = null;
			if (tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for (int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}else{
				throw new BusiException("没有录入车辆信息，请检查数据！");
			}

			for (InvLineBVO invLineBVO : bases) {
				// 只有一个点说明这个应该是基费 开始匹配合同
				contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, invVO.getBala_customer(),
						invVO.getPk_trans_type(), invLineBVO.getPk_address(), null, invLineBVO.getPk_city(), null,
						invVO.getPk_corp(), invVO.getReq_arri_date(), invVO.getUrgent_level(), invVO.getItem_code(),
						invVO.getPk_trans_line(),invVO.getIf_return());
				if (contractBVOs != null && contractBVOs.size() > 0) {
					for (ContractBVO contractBVO : contractBVOs) {
						// 只有结果为基费的合同才是我们需要的,并且合同设备类型需要相符
						if (contractBVO.getPk_expense_type().equals(baseTypeVO.getPk_expense_type())
								&& pk_car_type[0].equals(contractBVO.getEquip_type())) {
							List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
							//只会存在一个合同
							unitContractBVO.add(contractBVO);
							//计算包装，用于匹配费用
							List<PackInfo> packInfos = new ArrayList<PackInfo>();
							PackInfo packInfo = new PackInfo();
							packInfo.setPack(invLineBVO.getPack());
							packInfo.setWeight(invLineBVO.getWeight());
							packInfo.setVolume(invLineBVO.getVolume());
							packInfo.setNum(invLineBVO.getNum());
							List<ReceDetailBVO> detailBVOs = contractService.buildReceDetailBVO(invVO.getBala_customer(),
									invVO.getPack_num_count() == null ? 0 : invVO.getPack_num_count().doubleValue(),
									rdVO.getNum_count() == null ? 0 : rdVO.getNum_count(),
									rdVO.getFee_weight_count() == null ? 0 : rdVO.getFee_weight_count().doubleValue(),
									rdVO.getWeight_count() == null ? 0 : rdVO.getWeight_count().doubleValue(),
									rdVO.getVolume_count() == null ? 0 : rdVO.getVolume_count().doubleValue(),packInfos, pk_car_type,
									invVO.getPk_corp(), unitContractBVO);
							if(detailBVOs == null || detailBVOs.size() == 0){
								continue;
							}
							detailBVOs.get(0).setPrice(detailBVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL : detailBVOs.get(0).getAmount());
							newDetailBVOs.addAll(detailBVOs); 
						}
					}
				}
			}
			for (String key : groupMap.keySet()) {
				contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, invVO.getBala_customer(),
						invVO.getPk_trans_type(), groupMap.get(key).get(0).getPk_city(), null,
						groupMap.get(key).get(0).getPk_city(), null, invVO.getPk_corp(), invVO.getReq_arri_date(),
						invVO.getUrgent_level(), invVO.getItem_code(), invVO.getPk_trans_line(),invVO.getIf_return());
				if (contractBVOs != null && contractBVOs.size() > 0) {
					for (ContractBVO contractBVO : contractBVOs) {
						// 只有结果为点位费的合同才是我们需要的,并且合同设备类型需要相符
						if (contractBVO.getPk_expense_type().equals(pointTypeVO.getPk_expense_type())
								&& pk_car_type[0].equals(contractBVO.getEquip_type())) {
							List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
							unitContractBVO.add(contractBVO);
							//FIXME 点位费不用安装包装收费
							List<PackInfo> packInfos = new ArrayList<PackInfo>();
							List<ReceDetailBVO> detailBVOs = contractService.buildReceDetailBVO(invVO.getBala_customer(),
									invVO.getPack_num_count() == null ? 0 : invVO.getPack_num_count().doubleValue(),
									rdVO.getNum_count() == null ? 0 : rdVO.getNum_count(),
									rdVO.getFee_weight_count() == null ? 0 : rdVO.getFee_weight_count().doubleValue(),
									rdVO.getWeight_count() == null ? 0 : rdVO.getWeight_count().doubleValue(),
									rdVO.getVolume_count() == null ? 0 : rdVO.getVolume_count().doubleValue(),packInfos, pk_car_type,
									invVO.getPk_corp(), unitContractBVO);
							if(detailBVOs == null || detailBVOs.size() == 0){
								continue;
							}
							//只会存在一个合同
							detailBVOs.get(0).setPrice(detailBVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL : detailBVOs.get(0).getAmount());
							detailBVOs.get(0).setAmount(detailBVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL : detailBVOs.get(0).getAmount().multiply(groupMap.get(key).size()));
							detailBVOs.get(0).setContract_amount(detailBVOs.get(0).getAmount());
							newDetailBVOs.addAll(detailBVOs); 
						}
					}
				}
			}
			if (newDetailBVOs != null && newDetailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for (ReceDetailBVO detailBVO : newDetailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
				}
				// 重新设置新的费用明细
				invAggVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B,
						newDetailBVOs.toArray(new ReceDetailBVO[newDetailBVOs.size()]));
			}
		} else {
			// 重新匹配合同，匹配合同后需要
			// 1、如果当前已经存在运费的记录，同时匹配返回的记录中也包括运费的记录，那么将现有的删除，而使用刚刚匹配到的记录代替
			// 2、更新表头的总金额，
			// 3、更新运力信息的单价和金额
			contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, invVO.getBala_customer(),
					invVO.getPk_trans_type(), invVO.getPk_delivery(), invVO.getPk_arrival(), invVO.getDeli_city(),
					invVO.getArri_city(), invVO.getPk_corp(), invVO.getReq_arri_date(), invVO.getUrgent_level(),
					invVO.getItem_code(), invVO.getPk_trans_line(),invVO.getIf_return());

			if (contractBVOs != null && contractBVOs.size() > 0) {
				// 匹配到合同
				TransBilityBVO[] tbBVOs = (TransBilityBVO[]) invAggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
				String[] pk_car_type = null;
				if (tbBVOs != null && tbBVOs.length > 0) {
					pk_car_type = new String[tbBVOs.length];
					for (int i = 0; i < tbBVOs.length; i++) {
						pk_car_type[i] = tbBVOs[i].getPk_car_type();
					}
				}
				InvPackBVO[] invPackBVOs = (InvPackBVO[]) invAggVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
				
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				Map<String,List<InvPackBVO>> groupMap = new  HashMap<String,List<InvPackBVO>>();
				//对包装按照pack进行分组
				for(InvPackBVO invPackBVO : invPackBVOs){
					String key = invPackBVO.getPack();
					if(StringUtils.isBlank(key)){
						//没有包装的货品自动过滤
						continue;
					}
					List<InvPackBVO> voList = groupMap.get(key);
					if(voList == null){
						voList = new ArrayList<InvPackBVO>();
						groupMap.put(key, voList);
					}
					voList.add(invPackBVO);
				}
				if (groupMap.size() > 0) {
					for(String key : groupMap.keySet()){
						PackInfo packInfo = new PackInfo();
						List<InvPackBVO> voList = groupMap.get(key);
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(InvPackBVO packBVO : voList){
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
				newDetailBVOs = contractService.buildReceDetailBVO(invVO.getBala_customer(),
						invVO.getPack_num_count() == null ? 0 : invVO.getPack_num_count().doubleValue(),
						rdVO.getNum_count() == null ? 0 : rdVO.getNum_count(),
						rdVO.getFee_weight_count() == null ? 0 : rdVO.getFee_weight_count().doubleValue(),
						rdVO.getWeight_count() == null ? 0 : rdVO.getWeight_count().doubleValue(),
						rdVO.getVolume_count() == null ? 0 : rdVO.getVolume_count().doubleValue(), packInfos,pk_car_type,
						invVO.getPk_corp(), contractBVOs);
				if (newDetailBVOs != null && newDetailBVOs.size() > 0) {
					// 将这些匹配到的应收明细标识为新增
					for (ReceDetailBVO detailBVO : newDetailBVOs) {
						detailBVO.setStatus(VOStatus.NEW);
					}
					// 重新设置新的费用明细
					invAggVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B,
							newDetailBVOs.toArray(new ReceDetailBVO[newDetailBVOs.size()]));
				}
			}
		}

		// 保存费用明细
		List<ReceDetailBVO> allDetailBVOs = new ArrayList<ReceDetailBVO>();
		// 删除旧的费用明细
		// 2014-03-27 如果没有匹配到合同
		if (oldDetailBVOs != null && oldDetailBVOs.length > 0) {
			for (ReceDetailBVO rdBVO : oldDetailBVOs) {
				// 增加判断，只有系统创建的费用明细才删除，手工创建的不删除Jonathan 2015-10-26
				if (rdBVO.getSystem_create() != null && rdBVO.getSystem_create().toString().equals("Y")) {
					rdBVO.setStatus(VOStatus.DELETED);// 删除原有的系统创建的费用明细
					updateList.add(rdBVO);
					allDetailBVOs.add(rdBVO);
				} else {
					// 添加界面上已有的费用明细 Jonathan 2015-10-26
					allDetailBVOs.add(rdBVO);
				}
			}
		}

		// 保存到应收明细表
		rdVO.setStatus(VOStatus.UPDATED);
		// FIXME 取第一行合同明细的税种，税率
		if (contractBVOs != null && contractBVOs.size() > 0) {
			rdVO.setTax_cat(contractBVOs.get(0).getTax_cat());
			rdVO.setTax_rate(contractBVOs.get(0).getTax_rate());
			// FIXME 取第一行合同明细的税种，税率
			rdVO.setTaxmny(CMUtils.getTaxmny(rdVO.getCost_amount(), rdVO.getTax_cat(), rdVO.getTax_rate()));
		}
		updateList.add(rdVO);// 保存应收明细

		ReceDetailBVO[] receDetailBVOs = (ReceDetailBVO[]) invAggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		// 改成合同计算出来的费用明细，当合同未匹配到时，界面应该不传值的，但用以上方法还是会获得数据，造成总金额重复计算。2015-10-26
		// Jonathan
		if (newDetailBVOs != null && newDetailBVOs.size() > 0) {
			for (ReceDetailBVO receDetailBVO : newDetailBVOs) {
				if (receDetailBVO.getStatus() == VOStatus.NEW) {
					NWDao.setUuidPrimaryKey(receDetailBVO);
				}
				receDetailBVO.setPk_receive_detail(rdVO.getPk_receive_detail()); // 设置主表的主键
				updateList.add(receDetailBVO);
				allDetailBVOs.add(receDetailBVO);
			}
		}
		// 如果运力信息存在记录，而费用明细没有计价方式为“设备”的记录，那么需要插入一条费用明细
		// 将根据参数判断是否加上保险费
		boolean autoGenInsurance = ParameterHelper.getBooleanParam(ParameterHelper.AUTO_GEN_INSURANCE);// 读取参数是否自动生成保险单
		if (autoGenInsurance) {
			SuperVO etVO = expenseTypeService.getByCode("ET40");
			if (etVO == null) {
				throw new BusiException("费用类型中没有维护编码为ET40的保险费！");
			}
			String pk_expense_type = etVO.getPrimaryKey();

			for (int i = 0; i < receDetailBVOs.length; i++) {
				if (pk_expense_type.equals(receDetailBVOs[i].getPk_expense_type())) {
					// 已存在保险费，那么设置为删除状态
					receDetailBVOs[i].setStatus(VOStatus.DELETED);
				}
			}

			// 增加一个新的保险费
			if (invVO.getIf_insurance() != null && invVO.getIf_insurance().booleanValue()) {
				ReceDetailBVO insBVO = new ReceDetailBVO();
				insBVO.setPk_expense_type(pk_expense_type);
				insBVO.setValuation_type(ValuationTypeConst.TICKET);
				insBVO.setQuote_type(QuoteTypeConst.INTERVAL);
				insBVO.setPrice_type(PriceTypeConst.REGULAR_PRICE);
				insBVO.setAmount(invVO.getInsurance_amount());
				insBVO.setContract_amount(invVO.getInsurance_amount());
				insBVO.setSystem_create(UFBoolean.TRUE);
				insBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(insBVO);
				insBVO.setPk_receive_detail(rdVO.getPk_receive_detail());
				updateList.add(insBVO);
				allDetailBVOs.add(insBVO);
			}
		}

		// 重新统计费用
		CMUtils.processExtenalforComputer(rdVO, allDetailBVOs.toArray(new ReceDetailBVO[allDetailBVOs.size()]));
		dao.saveOrUpdate(updateList);
		// yaojiie 2015 12 15 添加金额利润分析方法
		List<InvoiceVO> invVOs = new ArrayList<InvoiceVO>();
		invVOs.add(invVO);
		CMUtils.totalCostComput(invVOs);

		paramVO.setBillId(rdVO.getPk_receive_detail());
		return (Map<String, Object>) execFormula4Templet(queryBillVO(paramVO), paramVO).get("HEADER");
	}

	/**
	 * <ul>
	 * <li>0、所有的应收明细必须是新建状态，以及是原始类型的</li>
	 * <li>1、所有的发货单必须属于同一种运段类型</li>
	 * <li>2、所有的发货单都必须是已到货或者部分到货</li>
	 * <li>3、如果不是返箱，那么辅助表的pk_arrival不能为空</li>
	 * <li>4\发货单必须是相同的运输方式和结算客户才能重算费用</li>
	 * <li>5\辅助表必须是相同的提货方，提货城市，收货方，收货城市，实际提货日期，要求到货日期</li>
	 * </ul>
	 * 
	 * 
	 * 
	 */
	public void doRebuildBySegtype(ParamVO paramVO, int seg_type, String[] billId) {
		if (billId == null || billId.length == 0) {
			return;
		}
		ReceiveDetailVO[] rdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
				"pk_receive_detail in " + NWUtils.buildConditionString(billId));
		if (rdVOs == null || rdVOs.length == 0) {
			return;
		}
		List<String> invoiceVbillnoAry = new ArrayList<String>();
		for (ReceiveDetailVO rdVO : rdVOs) {
			if (rdVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应收明细[?]必须是[新建]状态才能重算费用",rdVO.getVbillno());
			}
			if (rdVO.getRece_type() == null || rdVO.getRece_type().intValue() != ReceiveDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应收明细[?]必须是[原始类型]才能重算费用",rdVO.getVbillno());
			}
			invoiceVbillnoAry.add(rdVO.getInvoice_vbillno());
		}
		InvoiceVO[] invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, "vbillno in "
				+ NWUtils.buildConditionString(invoiceVbillnoAry.toArray(new String[invoiceVbillnoAry.size()])));
		if (invVOs == null || invVOs.length == 0) {
			return;
		}
		String s = SegmentConst.segtypeMap.get(seg_type);
		List<String> ordernoAry = new ArrayList<String>();
		String checkKey = null;
		List<MatchVO> matchVOs = new ArrayList<MatchVO>();
		Map<String, List<MatchVO>> ordernoMatchMap = new HashMap<String, List<MatchVO>>();
		Map<String, List<MatchVO>> vbillnoMatchMap = new HashMap<String, List<MatchVO>>();
		for (InvoiceVO invVO : invVOs) {
			if (!String.valueOf(seg_type).equals(invVO.getDef1())) {
				throw new BusiException("发货单[" + invVO.getVbillno() + "]必须是[" + s + "]才能重算费用！");
			}

			ordernoAry.add(invVO.getOrderno());

			if (checkKey == null) {
				checkKey = invVO.getPk_trans_type() + invVO.getBala_customer();
			} else {
				if (!checkKey.equals(invVO.getPk_trans_type() + invVO.getBala_customer())) {
					throw new BusiException("发货单必须是相同的运输方式和结算客户才能重算费用！");
				}
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
			matchVO.setPk_delivery(invVO.getPk_delivery());
			matchVO.setDeli_city(invVO.getDeli_city());
			matchVO.setPk_arrival(invVO.getPk_arrival());
			matchVO.setArri_city(invVO.getArri_city());
			matchVO.setReq_deli_date(invVO.getReq_deli_date());
			matchVO.setReq_arri_date(invVO.getReq_arri_date());
			matchVO.setAct_deli_date(invVO.getAct_deli_date());
			matchVO.setAct_arri_date(invVO.getAct_arri_date());

			// songf 2015-10-19 增加def6赋值
			matchVO.setDef6(invVO.getDef6());
			// yaojiie 2015 12 01 增加供应商字段
			matchVO.setPk_supplier(invVO.getPk_supplier());
			matchVO.setUrgent_level(invVO.getUrgent_level());
			matchVO.setItem_code(invVO.getItem_code());
			matchVO.setPk_trans_line(invVO.getPk_trans_line());
			matchVO.setIf_return(invVO.getIf_return());

			matchVOs.add(matchVO);
			List<MatchVO> mVOs = ordernoMatchMap.get(matchVO.getOrderno());
			if (mVOs == null) {
				mVOs = new ArrayList<MatchVO>();
				ordernoMatchMap.put(matchVO.getOrderno(), mVOs);
			}
			mVOs.add(matchVO);

			List<MatchVO> mVOs1 = vbillnoMatchMap.get(matchVO.getInvoice_vbillno());
			if (mVOs1 == null) {
				mVOs1 = new ArrayList<MatchVO>();
				vbillnoMatchMap.put(matchVO.getInvoice_vbillno(), mVOs1);
			}
			mVOs1.add(matchVO);
		}
		checkKey = null;

		OrderAssVO[] oaVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(OrderAssVO.class,
				"orderno in " + NWUtils.buildConditionString(ordernoAry.toArray(new String[ordernoAry.size()])));
		if (oaVOs == null || oaVOs.length == 0) {
			return;
		}


		for (ReceiveDetailVO rdVO : rdVOs) {
			List<MatchVO> mVOs = vbillnoMatchMap.get(rdVO.getInvoice_vbillno());
			for (MatchVO mVO : mVOs) {
				mVO.setRd_vbillno(rdVO.getVbillno());
				mVO.setPk_receive_detail(rdVO.getPk_receive_detail());
				
				mVO.setPack_num_count(rdVO.getPack_num_count() == null? UFDouble.ZERO_DBL:rdVO.getPack_num_count());
				mVO.setNum_count(rdVO.getNum_count() == null? 0 : rdVO.getNum_count());
				mVO.setWeight_count(rdVO.getWeight_count() == null? UFDouble.ZERO_DBL:rdVO.getWeight_count());
				mVO.setVolume_count(rdVO.getVolume_count() == null? UFDouble.ZERO_DBL:rdVO.getVolume_count());
				mVO.setFee_weight_count(rdVO.getFee_weight_count() == null? UFDouble.ZERO_DBL:rdVO.getFee_weight_count());
			}
		}

		ordernoMatchMap.clear();
		vbillnoMatchMap.clear();

		ReceDetailBuilder builder = new ReceDetailBuilder();
		builder.before(null);
		if (seg_type == SegmentConst.SEG_TYPE_THD) {
			builder.buildTHD(matchVOs);
		} else if (seg_type == SegmentConst.SEG_TYPE_GXD) {
			builder.buildGXD(matchVOs);
		} else if (seg_type == SegmentConst.SEG_TYPE_SHD) {
			builder.buildSHD(matchVOs);
		} else if (seg_type == SegmentConst.SEG_TYPE_FX) {
			builder.buildFX(matchVOs);
		}
		builder.after(null);
	}

	/**
	 * 导出集货应收明细 songf 2015-11-03
	 * 
	 * @param paramVO
	 *            暂时没用到
	 * @param receDetaiPKs
	 *            跨页选中的主键值
	 * @param strOrderType
	 *            类型，是正常，还是返箱
	 * @return
	 */
	public HSSFWorkbook exportPickupReceiveDetailRecord(ParamVO paramVO, String[] receDetaiPKs, String strOrderType) {
		logger.info("导出集货应收明细记录开始...");
		Calendar start = Calendar.getInstance();

		HSSFWorkbook wb = CMUtils.exportReceiveDetailRecord(receDetaiPKs, strOrderType);
		logger.info("导出集货明细语句结束...，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
		return wb;
	}
	
	/**
	 * tms的单据使用的是公司条件+逻辑条件，对于这个单据，另外需要加上委派公司等于当前公司的单据
	 */
	@SuppressWarnings("rawtypes")
	//yaojiie 2015 11 22 修改查询逻辑：解决OR　AND　ｂｕｇ
	public String buildLogicCondition(Class clazz, UiQueryTempletVO templetVO) {
		try {
			UserVO userVO = NWDao.getInstance().queryByCondition(UserVO.class, "pk_user=?",
					WebUtils.getLoginInfo().getPk_user());
			String logicCond = QueryHelper.getLogicCond(templetVO);
			SuperVO superVO = (SuperVO) clazz.newInstance();
			Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
			String corpCond = CorpHelper.getCurrentCorpWithChildren(superVO.getTableName());
			if(pk_corp != null) {
				Field pk_customer = ReflectionUtils.getDeclaredField(superVO, "pk_customer");
				if(pk_customer != null) {
					CorpVO corpVO = NWDao.getInstance().queryByCondition(CorpVO.class, "pk_corp=?",
							userVO.getPk_corp());
					CustomerVO customerVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "cust_code=?",
							corpVO.getCorp_code());
					if(customerVO != null){
						String custCond = "pk_customer='" + customerVO.getPk_customer() + "'";
						corpCond = "(" + corpCond +" or " + custCond + ")";
					}
				}
				if(StringUtils.isNotBlank(logicCond)) {
					corpCond += " and " + logicCond;
				}
			}else if(superVO.getTableName().equals("ts_rece_detail_b")){
				return logicCond;
			}
			
				
			return corpCond;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//yaojiie 2015 12 15 重写processAfterSave方法，方便进行利润金额的计算
	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processAfterSave(billVO, paramVO);
		//获取一个发货单VO的一个list，计算金额利率。
		ReceiveDetailVO receiveDetailVO = (ReceiveDetailVO)billVO.getParentVO();
		List<InvoiceVO> invoiceVOs = new ArrayList<InvoiceVO>();
		String invoiceVbillno = receiveDetailVO.getInvoice_vbillno();
		InvoiceVO[] invoiceVOsArr = dao.queryForSuperVOArrayByCondition(InvoiceVO.class, " vbillno =? ", invoiceVbillno);
		if(invoiceVOsArr == null || invoiceVOsArr.length == 0 || receiveDetailVO.getRece_type() != ReceiveDetailConst.ORIGIN_TYPE){
			//对于没有绑定发货单的调整明细，随便改改就好了。
			receiveDetailVO.setMaori(receiveDetailVO.getCost_amount());
			receiveDetailVO.setMaori_fee("0.00%");
			receiveDetailVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(receiveDetailVO);
		}else{
			invoiceVOs = Arrays.asList(invoiceVOsArr);
			CMUtils.totalCostComput(invoiceVOs);
		}
		
	}
	
	private void setCodeFieldRorBuildReceCheckSheet(CircularlyAccessibleValueObject parentVO, ParamVO paramVO) {
		if(StringUtils.isNotBlank(getCodeFieldCode())) {
			// 子类继承了该方法，说明希望使用编码规则
			Object codeObj = parentVO.getAttributeValue(getCodeFieldCode());
			if(codeObj == null || StringUtils.isBlank(codeObj.toString())) {
				// 如果没有录入编码，则按照规则生成一个
				String billno = BillnoHelper.generateBillno(BillTypeConst.YSDZ);
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
		ReceiveDetailVO detailVO = (ReceiveDetailVO)parentVO;
		detailVO.setStatus(VOStatus.UPDATED);
		detailVO.setVbillstatus(BillStatus.RD_CLOSE);
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
			if(BillStatus.RD_CLOSE != billStatus) {
				throw new RuntimeException("只有[关闭]状态的单据才能进行撤销关闭！");
			}
		}
		ReceiveDetailVO detailVO = (ReceiveDetailVO)parentVO;
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
		final String TS_FHD_CHECK_PROC = "ts_build_rece_check_proc";
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
	
	@Override
	protected String getConfirmAndUnconfirmProcName() {
		return "ts_ysmx_check_proc";
	}
}
