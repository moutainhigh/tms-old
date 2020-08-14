package com.tms.service.cm.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.exp.POI;
import org.nw.jf.UiConstants;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.redis.RedisDao;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.TempletHelper;
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
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CustService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.cm.PayCheckSheetService;
import com.tms.vo.cm.ExAggPayCheckSheetVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PayCheckSheetBVO;
import com.tms.vo.cm.PayCheckSheetRecordVO;
import com.tms.vo.cm.PayCheckSheetVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayRecordVO;
import com.tms.vo.cm.ReceCheckSheetVO;

/**
 * 应付对账
 * 
 * @author xuqc
 * @date 2013-3-23 下午07:04:27
 */
@Service
public class PayCheckSheetServiceImpl extends TMSAbsBillServiceImpl implements PayCheckSheetService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggPayCheckSheetVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggPayCheckSheetVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PayCheckSheetVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PayCheckSheetVO.PK_PAY_CHECK_SHEET);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggPayCheckSheetVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, PayCheckSheetBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, PayCheckSheetBVO.PK_PAY_CHECK_SHEET);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_pay_check_sheet_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_pay_check_sheet_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.YFDZ;
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
		processor.setTable_code(TabcodeConst.TS_PAY_CHECK_SHEET_RECORD);
		processor.setPk_billtemplet(templetVO.getTemplateID());
		processor.setPk_billtemplet_b(UUID.randomUUID().toString());
		templetVO.getFieldVOs().add(0, processor);
		return templetVO;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		BillTempletBVO aFieldVO = null;// 其中的一个fieldVO
		int index = 0;// 记录总费用所在的位置的前一个位置
		boolean toIncrease = true;
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("cost_amount") || fieldVO.getItemkey().equals("got_amount")
						|| fieldVO.getItemkey().equals("ungot_amount")) {
					fieldVO.setBeforeRenderer("amountBeforeRenderer");
				}
			} else if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getItemkey().equals("cost_amount")) {// 总费用
					aFieldVO = fieldVO;
					toIncrease = false;
				}
			}
			if(toIncrease) {
				index++;
			}
		}
		// 返回费用类型的所有费用
		List<ExpenseTypeVO> typeVOs = expenseTypeService.getAllExpenseType();
		// 运费的费用类型
		ExpenseTypeVO transFeeTypeVO = null;
		// 将运费的费用类型和其他费用类型区分开
		for(ExpenseTypeVO typeVO : typeVOs) {
			if(typeVO.getCode().equalsIgnoreCase(ExpenseTypeConst.ET10)) {
				transFeeTypeVO = typeVO;
				typeVOs.remove(typeVO);
				break;
			}
		}
		// 增加运费字段的模板
		if( aFieldVO == null){
			
			return templetVO;
			
			
		}
		if(transFeeTypeVO != null) {
			BillTempletBVO newFieldVO = (BillTempletBVO) aFieldVO.clone();
			newFieldVO.setItemkey(transFeeTypeVO.getPk_expense_type());
			newFieldVO.setDefaultshowname(transFeeTypeVO.getName());
			newFieldVO.setPos(UiConstants.POS[1]);
			newFieldVO.setTable_code("ts_pay_check_sheet_b");
			newFieldVO.setShowflag(0);
			newFieldVO.setListshowflag(UFBoolean.FALSE);
			newFieldVO.setDatatype(UiConstants.DATATYPE.DECIMAL.intValue());
			newFieldVO.setDefaultvalue("0");
			newFieldVO.setPk_billtemplet_b(UUID.randomUUID().toString());
			fieldVOs.add(index, newFieldVO);
			index++;
		}
		// 费用明细中定义的其他费用
		for(ExpenseTypeVO typeVO : typeVOs) {
			BillTempletBVO newFieldVO = (BillTempletBVO) aFieldVO.clone();
			newFieldVO.setItemkey(typeVO.getPk_expense_type());
			newFieldVO.setDefaultshowname(typeVO.getName());
			newFieldVO.setPos(UiConstants.POS[1]);
			newFieldVO.setTable_code("ts_pay_check_sheet_b");
			newFieldVO.setShowflag(0);
			newFieldVO.setListshowflag(UFBoolean.FALSE);
			newFieldVO.setDatatype(UiConstants.DATATYPE.DECIMAL.intValue());
			newFieldVO.setDefaultvalue("0");
			newFieldVO.setPk_billtemplet_b(UUID.randomUUID().toString());
			fieldVOs.add(index, newFieldVO);
			index++;
		}
		// 其他费用合计（除了运费外的其他费用）
		BillTempletBVO newFieldVO = (BillTempletBVO) aFieldVO.clone();
		newFieldVO.setItemkey(ExpenseTypeService.OTHER_FEE_CODE);
		newFieldVO.setDefaultshowname(ExpenseTypeService.OTHER_FEE_NAME);
		newFieldVO.setPos(UiConstants.POS[1]);
		newFieldVO.setTable_code("ts_pay_check_sheet_b");
		newFieldVO.setShowflag(0);
		newFieldVO.setListshowflag(UFBoolean.FALSE);
		newFieldVO.setDatatype(UiConstants.DATATYPE.DECIMAL.intValue());
		newFieldVO.setDefaultvalue("0");
		newFieldVO.setPk_billtemplet_b(UUID.randomUUID().toString());
		fieldVOs.add(index, newFieldVO);
		return templetVO;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ExAggPayCheckSheetVO aggVO = (ExAggPayCheckSheetVO) billVO;
		PayCheckSheetVO pcsVO = (PayCheckSheetVO) aggVO.getParentVO();
		if(pcsVO.getVbillstatus().intValue() != BillStatus.NEW) {
			throw new BusiException("必须是[新建]状态的应付明细才能修改！");
		}
		PayCheckSheetBVO[] pcsBVOs = (PayCheckSheetBVO[]) aggVO.getTableVO(TabcodeConst.TS_PAY_CHECK_SHEET_B);
		CMUtils.processExtenal(pcsVO, pcsBVOs);

		// 将删除的应收明细标记为已审核状态
		if(pcsBVOs != null && pcsBVOs.length > 0) {
			List<String> pks = new ArrayList<String>();
			for(PayCheckSheetBVO pcsBVO : pcsBVOs) {
				if(pcsBVO.getStatus() == VOStatus.DELETED) {
					pks.add(pcsBVO.getPk_pay_detail());
				}
			}
			if(pks.size() > 0) {
				String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
				PayDetailVO[] pdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
						"pk_pay_detail in " + cond);
				if(pdVOs != null) {
					for(PayDetailVO pdVO : pdVOs) {
						pdVO.setVbillstatus(BillStatus.PD_CONFIRM);
						pdVO.setStatus(VOStatus.UPDATED);
					}
					NWDao.getInstance().saveOrUpdate(pdVOs);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public PaginationVO loadPayCheckSheetRecord(String pk_pay_check_sheet, ParamVO paramVO, int offset, int pageSize) {
		PaginationVO paginationVO = dao.queryByConditionWithPaging(PayCheckSheetRecordVO.class, offset, pageSize,
				"relationid=?", pk_pay_check_sheet);
		List<SuperVO> superVOs = paginationVO.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOs.size());
		for(SuperVO vo : superVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		//UiBillTempletVO uiBillTempletVO = TempletHelper.getOriginalBillTempletVO(paramVO.getTemplateID());
		UiBillTempletVO uiBillTempletVO = RedisDao.getInstance().getOriginalBillTempletVO(paramVO.getTemplateID());
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(),
				tableCodes, null);
		paginationVO.setItems(list);
		return paginationVO;
	}

	public HSSFWorkbook exportPayCheckSheetRecord(ParamVO paramVO, int offset, int pageSize) {
		String pk_pay_check_sheet = ServletContextHolder.getRequest().getParameter("pk_pay_check_sheet");
		if(StringUtils.isBlank(pk_pay_check_sheet)) {
			throw new BusiException("请先选择一行对账单记录！");
		}
		String templateID = getBillTemplateID(paramVO);
		paramVO.setTemplateID(templateID);
		PaginationVO paginationVO = this.loadPayCheckSheetRecord(pk_pay_check_sheet, paramVO, offset, pageSize);
		List<List<Object>> dataList = processULWData(paginationVO.getItems(), paramVO);
		// 获取单据VO
		UiBillTempletVO templetVO = this.getBillTempletVO(getBillTemplateID(paramVO));
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		fieldVOs = filterULWFields(fieldVOs, null, paramVO);
		String[] titleAry = UiTempletUtils.getDefaultShowname(fieldVOs, paramVO);
		String[] finalTitleAry = new String[titleAry.length + 1];
		finalTitleAry[0] = ""; // 序号列
		for(int i = 0; i < titleAry.length; i++) {
			finalTitleAry[i + 1] = titleAry[i];
		}
		for(int i = 0; i < dataList.size(); i++) {
			// 插入行号列
			List<Object> objs = dataList.get(i);
			objs.add(0, (i + 1));
		}
		POI excel = new POI();
		if(StringUtils.isNotBlank(getFirstSheetName())) {
			excel.setFirstSheetName(getFirstSheetName());
		}
		return excel.buildExcel(finalTitleAry, dataList);
	}

	public PaginationVO getByPk_carrier(String pk_carrier, ParamVO paramVO) {
		if(StringUtils.isBlank(pk_carrier)) {
			return null;
		}
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		//yaojiie 2015 11 25 增加公司判断
		String where = "pk_carrier=? and vbillstatus=? and isnull(dr,0)=0 and "+corpCond;
		PayCheckSheetVO[] sheetVOs = dao.queryForSuperVOArrayByCondition(PayCheckSheetVO.class, where, pk_carrier,
				BillStatus.NEW);

		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());

		/**
		 * 4.转换数据格式，准备执行公式
		 */
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(sheetVOs.length);
		for(SuperVO vo : sheetVOs) {
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
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(),
				tableCodes, null);

		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(list);
		pageVO.setTotalCount(list == null ? 0 : list.size());
		return pageVO;
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object vbillstatus = parentVO.getAttributeValue("vbillstatus");
		if(vbillstatus == null || !vbillstatus.toString().equals(BillStatus.NEW + "")) {
			throw new BusiException("只有新建状态的对账单才能删除！");
		}
	}

	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys){
		// 删除对账单，把对应的应付明细的状态置为已确认
		List<PayDetailVO> toBeUpdate = new ArrayList<PayDetailVO>();
		for(String primaryKey : primaryKeys) {
			ParamVO paramVO = new ParamVO();
			paramVO.setBillId(primaryKey);
			AggregatedValueObject billVO = this.queryBillVO(paramVO);
			ExAggPayCheckSheetVO aggVO = (ExAggPayCheckSheetVO) billVO;
			CircularlyAccessibleValueObject[] vos = aggVO.getTableVO(TabcodeConst.TS_PAY_CHECK_SHEET_B);
			for(CircularlyAccessibleValueObject vo : vos) {
				String pk_pay_detail = vo.getAttributeValue(PayCheckSheetBVO.PK_PAY_DETAIL).toString();
				PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", pk_pay_detail);
				if(pdVO == null) {
					logger.warn("应付对账对应的应付明细已经被删除，pk_pay_detail:" + pk_pay_detail);
				} else {
					pdVO.setVbillstatus(BillStatus.PD_CONFIRM);
					pdVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(pdVO);
				}
			}
		}
		//yaojiie 2016 1 4 删除时，附带删除明细
		String cond = NWUtils.buildConditionString(primaryKeys);
		PayCheckSheetBVO[] payCheckSheetBVOs = dao.queryForSuperVOArrayByCondition(PayCheckSheetBVO.class, "pk_pay_check_sheet in "+ cond);
		dao.delete(payCheckSheetBVOs);
		dao.saveOrUpdate(toBeUpdate.toArray(new PayDetailVO[toBeUpdate.size()]));
		return super.batchDelete(clazz, primaryKeys);
	}

	protected Integer getConfirmStatus() {
		return BillStatus.PCS_CONFIRM;
	}

	public Map<String, Object> doPayable(ParamVO paramVO, String json) {
		logger.info("执行付款动作...");
		JsonNode header = JacksonUtils.readTree(json);
		PayCheckSheetRecordVO pcsrVO = (PayCheckSheetRecordVO) JacksonUtils.readValue(header,
				PayCheckSheetRecordVO.class);
		// 更新主表
		PayCheckSheetVO pcsVO = dao.queryByCondition(PayCheckSheetVO.class, "pk_pay_check_sheet=?",
				pcsrVO.getRelationid());
		if(pcsVO == null) {
			logger.error("对账单已经被删除,pk_pay_check_sheet:" + pcsrVO.getRelationid());
			throw new BusiException("对账单已经被删除！");
		}
		
		if(pcsVO.getVbillstatus().intValue() != BillStatus.PCS_CONFIRM
				&& pcsVO.getVbillstatus().intValue() != BillStatus.PCS_PART_CAVLOAN) {
			throw new BusiException("应付明细必须是确认和部分核销状态才能执行收款！");
		}
		// 付款金额
		UFDouble pay_amount = pcsrVO.getPay_amount() == null ? new UFDouble(0) : pcsrVO.getPay_amount();
		// 检查付款金额是否大于总金额
		UFDouble cost_amount = pcsVO.getCost_amount() == null ? new UFDouble(0) : pcsVO.getCost_amount();
		// 2013-11-3 xuqc 如果是承运商索赔的应付对账，金额是负数的
		// if(pay_amount.doubleValue() < 0) {
		// throw new BusiException("付款金额不能小于等于0!");
		// }
		// if(pay_amount.doubleValue() > cost_amount.doubleValue()) {
		// throw new BusiException("付款金额不能大于总金额");
		// }
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		// 设置已收金额
		UFDouble got_amount = pcsVO.getGot_amount() == null ? new UFDouble(0) : pcsVO.getGot_amount();
		pcsVO.setGot_amount(got_amount.add(pay_amount));
		// 设置未收金额
		pcsVO.setUngot_amount(cost_amount.sub(pcsVO.getGot_amount()));
		if(pcsVO.getUngot_amount().doubleValue() == 0) {
			pcsVO.setVbillstatus(BillStatus.PCS_CAVLOAN);// 如果所有款项已经付完，则状态置为已核销
			// 如果对账单是核销状态，则需要将对账单下的所有应付明细置为核销，同时对应付明细收款记录表插入一条记录
			PayCheckSheetBVO[] childrenVOs = dao.queryForSuperVOArrayByCondition(PayCheckSheetBVO.class,
					"pk_pay_check_sheet=?", pcsVO.getPk_pay_check_sheet());
			for(PayCheckSheetBVO childVO : childrenVOs) {
				PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?",
						childVO.getPk_pay_detail());
				if(pdVO == null) {
					logger.error("应付明细已经被删除,pk_pay_detail:" + childVO.getPk_pay_detail());
					throw new BusiException("应付明细已经被删除！");
				}
				pdVO.setVbillstatus(BillStatus.PD_CAVLOAN);
				pdVO.setGot_amount(pdVO.getCost_amount());
				pdVO.setUngot_amount(new UFDouble(0));
				pdVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(pdVO);

				// 插入应收明细的收款记录
				PayRecordVO rrVO1 = new PayRecordVO();
				rrVO1.setDbilldate(new UFDate());
				rrVO1.setRelationid(pdVO.getPk_pay_detail());// 这里是应付明细的pk
				rrVO1.setVbillno(pdVO.getVbillno());// 对账单号
				rrVO1.setPay_type(PayDetailConst.PAYABLE_TYPE.CHECKSHEET.intValue());// 对账收款
				rrVO1.setPay_amount(pdVO.getCost_amount());
				rrVO1.setPay_date(pcsrVO.getPay_date());
				rrVO1.setPay_man(pcsrVO.getPay_man());
				rrVO1.setPay_method(pcsrVO.getPay_method());
				rrVO1.setCheck_no(pcsrVO.getCheck_no());
				rrVO1.setCheck_head(pcsrVO.getCheck_head());
				rrVO1.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				rrVO1.setCreate_time(new UFDateTime(new Date()));
				rrVO1.setMemo(pcsrVO.getMemo());
				rrVO1.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(rrVO1);
				toBeUpdate.add(rrVO1);
			}
		} else {
			pcsVO.setVbillstatus(BillStatus.PCS_PART_CAVLOAN);// 部分核销
		}

		// 更新应收对账的发票号和发票抬头
//		String[] checkArr = CMUtils.getUpdateCheck(pcsVO.getCheck_no(), pcsrVO.getCheck_no(), pcsVO.getCheck_head(),
//				pcsrVO.getCheck_head());
//		pcsVO.setCheck_no(checkArr[0]);
//		pcsVO.setCheck_head(checkArr[1]);
		pcsVO.setCheck_no(pcsrVO.getCheck_no());
		pcsVO.setCheck_head(pcsrVO.getCheck_head());
		pcsVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(pcsVO);

		pcsrVO.setPay_type(PayDetailConst.PAYABLE_TYPE.CHECKSHEET.intValue());// 直接收款
		pcsrVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		pcsrVO.setCreate_time(new UFDateTime(new Date()));
		pcsrVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(pcsrVO);
		toBeUpdate.add(pcsrVO);
		dao.saveOrUpdate(toBeUpdate);

		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(pcsVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}
	
	//yaojiie 2015 12 30 增加开票方法，当开票时，保存开票信息
	public void payCheckSheetInvoice(ParamVO paramVO, String json){
		logger.info("执行开票动作...");
		JsonNode header = JacksonUtils.readTree(json);
		

		String checkType = header.get("check_type").getValueAsText();
		String taxCat = header.get("check_tax_cat").getValueAsText();
		String TaxRate = header.get("check_tax_rate").getValueAsText();
		
		
		if(StringUtils.isNotBlank(checkType) && !checkType.equals("null")){
			String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b With (nolock) "
					+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  "
					+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
			String value = NWDao.getInstance().queryForObject(sql, String.class,checkType,checkType);
			if(value != null && !checkType.equals(value)){
				json = json.replace(checkType, value);
			}
		}
		
		if(StringUtils.isNotBlank(TaxRate) && !TaxRate.equals("null")){
			String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b With (nolock) "
					+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='tax_rate' "
					+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
			String value = NWDao.getInstance().queryForObject(sql, String.class,TaxRate,TaxRate);
			if(value != null && !TaxRate.equals(value)){
				json = json.replace(TaxRate, value);
			}
		}
		
		if(StringUtils.isNotBlank(taxCat) && !taxCat.equals("null")){
			String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b With (nolock) "
					+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='tax_category' "
					+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
			String value = NWDao.getInstance().queryForObject(sql, String.class,taxCat,taxCat);
			if(value != null && !taxCat.equals(value)){
				json = json.replace(taxCat, value);
			}
		}
		
		JsonNode newHeader = JacksonUtils.readTree(json);
		
		PayCheckSheetVO pcsVO = (PayCheckSheetVO) JacksonUtils.readValue(newHeader,PayCheckSheetVO.class);
		// 更新主表
		PayCheckSheetVO oldPcsVO = dao.queryByCondition(PayCheckSheetVO.class, " vbillno=?",
				pcsVO.getVbillno());
		if(oldPcsVO == null) {
			logger.error("对账单已经被删除:" + pcsVO.getVbillno());
			throw new BusiException("对账单已经被删除!");
		}
		if(oldPcsVO.getVbillstatus().intValue() != BillStatus.PCS_CONFIRM) {
			throw new BusiException("应付对账必须是确认才能执行开票！");
		}
		oldPcsVO.setIf_check(UFBoolean.TRUE);
		oldPcsVO.setCheck_date(pcsVO.getCheck_date());
		
		if(pcsVO.getCheck_amount() == null ){
			throw new BusiException("开票金额不能为空！");
		}else{
			oldPcsVO.setCheck_amount(pcsVO.getCheck_amount());
		}
		
		if(pcsVO.getCheck_type() == null ){
			throw new BusiException("发票类型不能为空！");
		}else{
			oldPcsVO.setCheck_type(pcsVO.getCheck_type());
		}
		
		if(StringUtils.isBlank(pcsVO.getCheck_no())){
			throw new BusiException("发票号不能为空！");
		}else{
			oldPcsVO.setCheck_no(pcsVO.getCheck_no());
		}
		if(StringUtils.isBlank(pcsVO.getCheck_head())){
			throw new BusiException("发票抬头不能为空！");
		}else{
			oldPcsVO.setCheck_head(pcsVO.getCheck_head());
		}
		oldPcsVO.setCheck_man(pcsVO.getCheck_man());
		oldPcsVO.setCheck_tax_rate(pcsVO.getCheck_tax_rate());
		oldPcsVO.setCheck_tax_cat(pcsVO.getCheck_tax_cat());
		oldPcsVO.setCheck_remark(pcsVO.getCheck_remark());
		oldPcsVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(oldPcsVO);
	}

	public Map<String, Object> deletePayCheckSheetRecord(ParamVO paramVO, String pk_pay_check_sheet_record) {
		if(StringUtils.isBlank(pk_pay_check_sheet_record)) {
			throw new BusiException("请先选择付款纪录！");
		}
		PayCheckSheetRecordVO pcsrVO = dao.queryByCondition(PayCheckSheetRecordVO.class, "pk_pay_check_sheet_record=?",
				pk_pay_check_sheet_record);
		if(pcsrVO == null) {
			logger.error("该付款纪录已经被删除,pk_pay_check_sheet_record:" + pk_pay_check_sheet_record);
			throw new BusiException("该付款纪录已经被删除！");
		}
		PayCheckSheetVO pcsVO = dao.queryByCondition(PayCheckSheetVO.class, "pk_pay_check_sheet=?",
				pcsrVO.getRelationid());
		if(pcsVO == null) {
			logger.error("该付款纪录所对应的应付对账已经被删除，pk_pay_check_sheet" + pcsrVO.getRelationid());
			throw new BusiException("该付款纪录所对应的应付对账已经被删除！");
		}
		UFDouble pay_amount = pcsrVO.getPay_amount() == null ? new UFDouble(0) : pcsrVO.getPay_amount();
		UFDouble ungot_amount = pcsVO.getUngot_amount() == null ? new UFDouble(0) : pcsVO.getUngot_amount();
		pcsVO.setUngot_amount(ungot_amount.add(pay_amount));// 加入到未付款额中
		UFDouble got_amount = pcsVO.getGot_amount() == null ? new UFDouble(0) : pcsVO.getGot_amount();
		pcsVO.setGot_amount(got_amount.sub(pay_amount));// 已付款额必须减去该记录的付款额
		UFDouble cost_amount = pcsVO.getCost_amount() == null ? new UFDouble(0) : pcsVO.getCost_amount();
		if(pcsVO.getUngot_amount().doubleValue() == cost_amount.doubleValue()) {
			// 如果未付款额和总金额相等，说明该记录完全没有付款。那么将该记录的状态置为已确认
			pcsVO.setVbillstatus(BillStatus.PCS_CONFIRM);
		} else {
			pcsVO.setVbillstatus(BillStatus.PCS_PART_CAVLOAN);
		}
		pcsVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(pcsVO);
		// 删除对账单所对应的应付明细的收款记录
		PayRecordVO[] prVOs = dao.queryForSuperVOArrayByCondition(PayRecordVO.class, "relationid=? and pay_type=?",
				pcsVO.getPk_pay_check_sheet(), PayDetailConst.PAYABLE_TYPE.CHECKSHEET.intValue());
		dao.delete(prVOs);
		// 将所对应的应付明细的状态更新为已对账
		PayCheckSheetBVO[] childrenVOs = dao.queryForSuperVOArrayByCondition(PayCheckSheetBVO.class,
				"pk_pay_check_sheet=?", pcsVO.getPk_pay_check_sheet());
		for(PayCheckSheetBVO childVO : childrenVOs) {
			PayDetailVO pdVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", childVO.getPk_pay_detail());
			pdVO.setVbillstatus(BillStatus.PD_CHECK);
			pdVO.setGot_amount(new UFDouble(0));
			pdVO.setUngot_amount(pdVO.getCost_amount());
			pdVO.setStatus(VOStatus.UPDATED);
			dao.saveOrUpdate(pdVO);
		}
		// 删除付款纪录
		dao.delete(pcsrVO);
		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(pcsVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}

	/**
	 * 过滤ULW页面的字段，目前使用在导出中，增加一些隐藏的字段到导出的excel中
	 * 
	 * @param list
	 * @param immobilityFields
	 * @param paramVO
	 * @return
	 */
	protected List<BillTempletBVO> filterULWFields(List<BillTempletBVO> list, List<String> immobilityFields,
			ParamVO paramVO) {
		if(paramVO.isBody()) {
			if(immobilityFields == null) {
				immobilityFields = new ArrayList<String>();
				immobilityFields.add("invoice_vbillno");// 发货单号
				immobilityFields.add("pk_customer");// 客户
				List<ExpenseTypeVO> typeVOs = expenseTypeService.getAllExpenseType();
				for(ExpenseTypeVO typeVO : typeVOs) {
					immobilityFields.add(typeVO.getPk_expense_type());
				}
				immobilityFields.add(ExpenseTypeService.OTHER_FEE_CODE);
			}
		}
		return UiTempletUtils.filterULWFields(list, immobilityFields, paramVO);
	}

	@Autowired
	private CustService custService;


	@Autowired
	private ExpenseTypeService expenseTypeService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processBeforeExport(ParamVO paramVO, List dataList) {
		super.processBeforeExport(paramVO, dataList);
		if(paramVO.isBody()) {
			// 返回费用类型的所有费用
			List<ExpenseTypeVO> typeVOs = expenseTypeService.getAllExpenseType();
			// 运费的费用类型
			ExpenseTypeVO transFeeTypeVO = null;
			// 将运费的费用类型和其他费用类型区分开
			for(ExpenseTypeVO typeVO : typeVOs) {
				if(typeVO.getCode().equalsIgnoreCase(ExpenseTypeConst.ET10)) {
					transFeeTypeVO = typeVO;
					typeVOs.remove(typeVO);
					break;
				}
			}

			List<Map<String, Object>> mapList = (List<Map<String, Object>>) dataList;
			for(Map<String, Object> map : mapList) {
				// 处理客户字段
				Object pk_customer = map.get("pk_customer");
				if(pk_customer != null) {
					map.put("pk_customer", custService.getNameString(pk_customer.toString()));
				}
				// 费用类型中定义的费用变成这里的列，并且再增加一个“其他费用合计”（去除“运费”）的其他费用的合计
				Object o_pk_pay_detail = map.get("pk_pay_detail");
				if(o_pk_pay_detail != null) {
					String pk_pay_detail = o_pk_pay_detail.toString();// 应付明细的pk
					// 查询所有费用明细
					PayDetailBVO detailBVOs[] = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
							"pk_pay_detail=? and isnull(dr,0)=0", pk_pay_detail);
					if(detailBVOs != null && detailBVOs.length > 0) {
						double otherFee = 0;
						for(PayDetailBVO detailBVO : detailBVOs) {
							map.put(detailBVO.getPk_expense_type(), detailBVO.getAmount() == null ? 0 : detailBVO
									.getAmount().doubleValue());
							if(transFeeTypeVO == null
									|| !detailBVO.getPk_expense_type().equals(transFeeTypeVO.getPk_expense_type())) {
								otherFee += detailBVO.getAmount() == null ? 0 : detailBVO.getAmount().doubleValue();
							}
						}
						// 其他费用合计
						map.put(ExpenseTypeService.OTHER_FEE_CODE, otherFee);
					}
				}
			}
		}
	}
	
	public String getPayCheckSheetCheckType(String check_type){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,check_type);
	}
	
	public String getPayCheckSheetTaxtCat(String taxtCat){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='tax_category'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,taxtCat);
	}
	
	public String getPayCheckSheetCheckTaxtRate(String taxtRate){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='tax_rate'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,taxtRate.replaceAll("0*$", ""));
	}
	
	public String getPayCheckSheetCheckHead(String vbillno) {
		PayCheckSheetVO checkSheetVO = NWDao.getInstance().queryByCondition(PayCheckSheetVO.class,
				"vbillno = ?", vbillno);
		return checkSheetVO.getCheck_head();
	}
	
	@Override
	protected String getConfirmAndUnconfirmProcName() {
		return "ts_yfdz_check_proc";
	}
	
}
