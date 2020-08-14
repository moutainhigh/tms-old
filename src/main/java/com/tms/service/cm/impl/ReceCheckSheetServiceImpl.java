package com.tms.service.cm.impl;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.ReceCheckSheetService;
import com.tms.vo.cm.ExAggReceCheckSheetVO;
import com.tms.vo.cm.PayCheckSheetBVO;
import com.tms.vo.cm.PayCheckSheetVO;
import com.tms.vo.cm.ReceCheckSheetBVO;
import com.tms.vo.cm.ReceCheckSheetRecordVO;
import com.tms.vo.cm.ReceCheckSheetVO;
import com.tms.vo.cm.ReceRecordVO;
import com.tms.vo.cm.ReceiveDetailVO;

/**
 * 应收对账
 * 
 * @author xuqc
 * @date 2013-3-23 下午07:04:27
 */
@Service
public class ReceCheckSheetServiceImpl extends TMSAbsBillServiceImpl implements ReceCheckSheetService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggReceCheckSheetVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggReceCheckSheetVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ReceCheckSheetVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ReceCheckSheetVO.PK_RECE_CHECK_SHEET);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggReceCheckSheetVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ReceCheckSheetBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ReceCheckSheetBVO.PK_RECE_CHECK_SHEET);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_rece_check_sheet_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_rece_check_sheet_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.YSDZ;
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
		processor.setTable_code(TabcodeConst.TS_RECE_CHECK_SHEET_RECORD);
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
				if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("cost_amount") || fieldVO.getItemkey().equals("got_amount")
						|| fieldVO.getItemkey().equals("ungot_amount")) {
					fieldVO.setBeforeRenderer("amountBeforeRenderer");
				}
			}
		}
		return templetVO;
	}

	public boolean isCompatibleMode() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public PaginationVO loadReceCheckSheetRecord(String pk_rece_check_sheet, ParamVO paramVO, int offset, int pageSize) {
		PaginationVO paginationVO = dao.queryByConditionWithPaging(ReceCheckSheetRecordVO.class, offset, pageSize,
				"relationid=?", pk_rece_check_sheet);
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

	public HSSFWorkbook exportReceCheckSheetRecord(ParamVO paramVO, int offset, int pageSize) {
		logger.info("导出收款记录开始...");
		Calendar start = Calendar.getInstance();
		String pk_rece_check_sheet = ServletContextHolder.getRequest().getParameter("pk_rece_check_sheet");
		if(StringUtils.isBlank(pk_rece_check_sheet)) {
			throw new BusiException("请先选择一行对账单记录！");
		}
		String templateID = getBillTemplateID(paramVO);
		paramVO.setTemplateID(templateID);
		PaginationVO paginationVO = this.loadReceCheckSheetRecord(pk_rece_check_sheet, paramVO, offset, pageSize);
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
		HSSFWorkbook wb = excel.buildExcel(finalTitleAry, dataList);
		logger.info("导出收款记录结束，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
		return wb;
	}

	public PaginationVO getByBala_customer(String bala_customer, ParamVO paramVO) {
		if(StringUtils.isBlank(bala_customer)) {
			return null;
		}
		//yaojiie 2015 11 25 增加公司判断
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		String where = "bala_customer=? and vbillstatus=? and isnull(dr,0)=0 and "+corpCond;
		ReceCheckSheetVO[] sheetVOs = dao.queryForSuperVOArrayByCondition(ReceCheckSheetVO.class, where, bala_customer,
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

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ExAggReceCheckSheetVO aggVO = (ExAggReceCheckSheetVO) billVO;
		ReceCheckSheetVO rcsVO = (ReceCheckSheetVO) aggVO.getParentVO();
		if(rcsVO.getVbillstatus().intValue() != BillStatus.NEW) {
			throw new BusiException("必须是[新建]状态的应付明细才能修改！");
		}
		ReceCheckSheetBVO[] rcsBVOs = (ReceCheckSheetBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_CHECK_SHEET_B);
		CMUtils.processExtenal(rcsVO, rcsBVOs);

		// 将删除的应收明细标记为已审核状态
		if(rcsBVOs != null && rcsBVOs.length > 0) {
			List<String> pks = new ArrayList<String>();
			for(ReceCheckSheetBVO rcsBVO : rcsBVOs) {
				if(rcsBVO.getStatus() == VOStatus.DELETED) {
					pks.add(rcsBVO.getPk_receive_detail());
				}
			}
			if(pks.size() > 0) {
				String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
				ReceiveDetailVO[] rdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
						"pk_receive_detail in " + cond);
				if(rdVOs != null) {
					for(ReceiveDetailVO rdVO : rdVOs) {
						rdVO.setVbillstatus(BillStatus.RD_CONFIRM);
						rdVO.setStatus(VOStatus.UPDATED);
					}
					NWDao.getInstance().saveOrUpdate(rdVOs);
				}
			}
		}
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object vbillstatus = parentVO.getAttributeValue("vbillstatus");
		if(vbillstatus == null || !vbillstatus.toString().equals(BillStatus.NEW + "")) {
			throw new BusiException("只有[新建]状态的对账单才能删除！");
		}
	}

	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys){
		// 删除对账单，把对应的应收明细的状态置为已确认
		List<ReceiveDetailVO> toBeUpdate = new ArrayList<ReceiveDetailVO>();
		for(String primaryKey : primaryKeys) {
			ParamVO paramVO = new ParamVO();
			paramVO.setBillId(primaryKey);
			AggregatedValueObject billVO = this.queryBillVO(paramVO);
			ExAggReceCheckSheetVO aggVO = (ExAggReceCheckSheetVO) billVO;
			CircularlyAccessibleValueObject[] vos = aggVO.getTableVO(TabcodeConst.TS_RECE_CHECK_SHEET_B);
			for(CircularlyAccessibleValueObject vo : vos) {
				String pk_receive_detail = vo.getAttributeValue(ReceCheckSheetBVO.PK_RECEIVE_DETAIL).toString();
				ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
						pk_receive_detail);
				if(rdVO == null) {
					logger.warn("应收对账对应的应收明细已经被删除，pk_receive_detail:" + pk_receive_detail);
				} else {
					rdVO.setVbillstatus(BillStatus.RD_CONFIRM);
					rdVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(rdVO);
				}
			}
		}
		//yaojiie 2016 1 4 删除时，附带删除明细
		String cond = NWUtils.buildConditionString(primaryKeys);
		ReceCheckSheetBVO[] receCheckSheetBVOs = dao.queryForSuperVOArrayByCondition(ReceCheckSheetBVO.class, "pk_rece_check_sheet in "+ cond);
		dao.delete(receCheckSheetBVOs);
		dao.saveOrUpdate(toBeUpdate.toArray(new ReceiveDetailVO[toBeUpdate.size()]));
		return super.batchDelete(clazz, primaryKeys);
	}

	protected Integer getConfirmStatus() {
		return BillStatus.RCS_CONFIRM;
	}

	public Map<String, Object> doReceivable(ParamVO paramVO, String json) {
		logger.info("执行收款动作...");
		JsonNode header = JacksonUtils.readTree(json);
		ReceCheckSheetRecordVO rcsrVO = (ReceCheckSheetRecordVO) JacksonUtils.readValue(header,
				ReceCheckSheetRecordVO.class);
		// 更新主表
		ReceCheckSheetVO rcsVO = dao.queryByCondition(ReceCheckSheetVO.class, "pk_rece_check_sheet=?",
				rcsrVO.getRelationid());
		if(rcsVO == null) {
			logger.error("对账单已经被删除,pk_rece_check_sheet:" + rcsrVO.getRelationid());
			throw new BusiException("对账单已经被删除!");
		}
		if(rcsVO.getVbillstatus().intValue() != BillStatus.RCS_CONFIRM
				&& rcsVO.getVbillstatus().intValue() != BillStatus.RCS_PART_CAVLOAN) {
			throw new BusiException("应收明细必须是确认和部分核销状态才能执行收款！");
		}
		// 收款金额
		UFDouble receivable_amount = rcsrVO.getReceivable_amount() == null ? new UFDouble(0) : rcsrVO
				.getReceivable_amount();
		// 检查收款金额是否大于总金额
		UFDouble cost_amount = rcsVO.getCost_amount() == null ? new UFDouble(0) : rcsVO.getCost_amount();
		// 2013-11-3 xuqc 如果是客户索赔的应收对账，金额是负数的
		// if(receivable_amount.doubleValue() < 0) {
		// throw new BusiException("收款金额不能小于0!");
		// }
		// if(receivable_amount.doubleValue() > cost_amount.doubleValue()) {
		// throw new BusiException("收款金额不能大于总金额");
		// }
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		// 设置已收金额
		UFDouble got_amount = rcsVO.getGot_amount() == null ? new UFDouble(0) : rcsVO.getGot_amount();
		rcsVO.setGot_amount(got_amount.add(receivable_amount));
		// 设置未收金额
		rcsVO.setUngot_amount(cost_amount.sub(rcsVO.getGot_amount()));
		if(rcsVO.getUngot_amount().doubleValue() == 0) {
			rcsVO.setVbillstatus(BillStatus.RCS_CAVLOAN);// 如果所有款项已经收完，则状态置为已核销
			// 如果对账单是核销状态，则需要将对账单下的所有应收明细置为核销，同时对应收明细收款记录表插入一条记录
			ReceCheckSheetBVO[] childrenVOs = dao.queryForSuperVOArrayByCondition(ReceCheckSheetBVO.class,
					"pk_rece_check_sheet=?", rcsVO.getPk_rece_check_sheet());
			for(ReceCheckSheetBVO childVO : childrenVOs) {
				ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
						childVO.getPk_receive_detail());
				if(rdVO == null) {
					logger.error("应收明细已经被删除,pk_receive_detail:" + childVO.getPk_receive_detail());
					throw new BusiException("应收明细已经被删除！");
				}
				rdVO.setVbillstatus(BillStatus.RD_CAVLOAN);
				rdVO.setGot_amount(rdVO.getCost_amount());
				rdVO.setUngot_amount(new UFDouble(0));
				rdVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(rdVO);

				// 插入应收明细的收款记录
				ReceRecordVO rrVO1 = new ReceRecordVO();
				rrVO1.setDbilldate(new UFDate());
				rrVO1.setRelationid(rdVO.getPk_receive_detail());// 这里是应收明细的pk
				rrVO1.setVbillno(rdVO.getVbillno());// 对账单号
				rrVO1.setReceivable_type(ReceiveDetailConst.RECEIVABLE_TYPE.CHECKSHEET.intValue());// 对账收款
				rrVO1.setReceivable_amount(rdVO.getCost_amount());
				rrVO1.setReceivable_date(rcsrVO.getReceivable_date());
				rrVO1.setReceivable_man(rcsrVO.getReceivable_man());
				rrVO1.setReceivable_method(rcsrVO.getReceivable_method());
				rrVO1.setCheck_no(rcsrVO.getCheck_no());
				rrVO1.setCheck_head(rcsrVO.getCheck_head());
				rrVO1.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				rrVO1.setCreate_time(new UFDateTime(new Date()));
				rrVO1.setMemo(rcsrVO.getMemo());
				rrVO1.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(rrVO1);
				toBeUpdate.add(rrVO1);
			}
		} else {
			rcsVO.setVbillstatus(BillStatus.RCS_PART_CAVLOAN);// 部分核销
		}
		// 更新应收对账的发票号和发票抬头
//		String[] checkArr = CMUtils.getUpdateCheck(rcsVO.getCheck_no(), rcsrVO.getCheck_no(), rcsVO.getCheck_head(),
//				rcsrVO.getCheck_head());
//		rcsVO.setCheck_no(checkArr[0]);
//		rcsVO.setCheck_head(checkArr[1]);
		rcsVO.setCheck_no(rcsrVO.getCheck_no());
		rcsVO.setCheck_head(rcsrVO.getCheck_head());

		rcsVO.setStatus(VOStatus.UPDATED);
		toBeUpdate.add(rcsVO);

		rcsrVO.setReceivable_type(ReceiveDetailConst.RECEIVABLE_TYPE.CHECKSHEET.intValue());// 直接收款
		rcsrVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		rcsrVO.setCreate_time(new UFDateTime(new Date()));
		rcsrVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(rcsrVO);
		toBeUpdate.add(rcsrVO);
		dao.saveOrUpdate(toBeUpdate);

		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(rcsVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}
	
	// yaojiie 2015 12 30 增加开票方法，当开票时，保存开票信息
	public void receCheckSheetInvoice(ParamVO paramVO, String json) {
		logger.info("执行开票动作...");
		JsonNode header = JacksonUtils.readTree(json);

		String checkType = header.get("check_type").getValueAsText();
		String checkCorp = header.get("check_corp").getValueAsText();
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
		
		if(StringUtils.isNotBlank(checkCorp) && !checkCorp.equals("null")){
			String sql = "SELECT nw_data_dict_b.value FROM nw_data_dict_b With (nolock) "
					+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='check_company'  "
					+ "AND ( nw_data_dict_b.value=? or nw_data_dict_b.display_name = ?)";
			String value = NWDao.getInstance().queryForObject(sql, String.class,checkCorp,checkCorp);
			if(value != null && !checkCorp.equals(value)){
				json = json.replace(checkCorp, value);
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
		
		ReceCheckSheetVO rcsVO = (ReceCheckSheetVO) JacksonUtils.readValue(newHeader, ReceCheckSheetVO.class);
		// 更新主表
		ReceCheckSheetVO oldrcsVO = dao.queryByCondition(ReceCheckSheetVO.class, " vbillno=?", rcsVO.getVbillno());
		if (oldrcsVO == null) {
			logger.error("对账单已经被删除:" + rcsVO.getVbillno());
			throw new BusiException("对账单已经被删除！");
		}
		if (oldrcsVO.getVbillstatus().intValue() != BillStatus.RCS_CONFIRM) {
			throw new BusiException("应收对账必须是[确认]状态才能执行开票！");
		}
		oldrcsVO.setIf_check(UFBoolean.TRUE);
		oldrcsVO.setCheck_date(rcsVO.getCheck_date());

		if (rcsVO.getCheck_amount() == null) {
			throw new BusiException("开票金额不能为空！");
		} else {
			oldrcsVO.setCheck_amount(rcsVO.getCheck_amount());
		}
		
		if (rcsVO.getCheck_type() == null) {
			throw new BusiException("发票类型不能为空！");
		} else {
			oldrcsVO.setCheck_type(rcsVO.getCheck_type());
		}
		
		if (rcsVO.getCheck_corp() == null) {
			throw new BusiException("开票公司不能为空！");
		} else {
			oldrcsVO.setCheck_corp(rcsVO.getCheck_corp());
		}
		
		if (StringUtils.isBlank(rcsVO.getCheck_no())) {
			throw new BusiException("发票号不能为空！");
		} else {
			oldrcsVO.setCheck_no(rcsVO.getCheck_no());
		}
		if (StringUtils.isBlank(rcsVO.getCheck_head())) {
			throw new BusiException("发票抬头不能为空！");
		} else {
			oldrcsVO.setCheck_head(rcsVO.getCheck_head());
		}
		oldrcsVO.setCheck_man(rcsVO.getCheck_man());
		oldrcsVO.setCheck_tax_rate(rcsVO.getCheck_tax_rate());
		oldrcsVO.setCheck_tax_cat(rcsVO.getCheck_tax_cat());
		oldrcsVO.setCheck_remark(rcsVO.getCheck_remark());
		oldrcsVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(oldrcsVO);
	}

	public Map<String, Object> deleteReceCheckSheetRecord(ParamVO paramVO, String pk_rece_check_sheet_record) {
		logger.info("删除收款纪录，pk_rece_check_sheet_record：" + pk_rece_check_sheet_record);
		if(StringUtils.isBlank(pk_rece_check_sheet_record)) {
			throw new BusiException("请先选择收款纪录！");
		}
		ReceCheckSheetRecordVO rcsrVO = dao.queryByCondition(ReceCheckSheetRecordVO.class,
				"pk_rece_check_sheet_record=?", pk_rece_check_sheet_record);
		if(rcsrVO == null) {
			logger.error("该收款纪录已经被删除,pk_rece_check_sheet_record:" + pk_rece_check_sheet_record);
			throw new BusiException("该收款纪录已经被删除！");
		}
		ReceCheckSheetVO rcsVO = dao.queryByCondition(ReceCheckSheetVO.class, "pk_rece_check_sheet=?",
				rcsrVO.getRelationid());
		if(rcsVO == null) {
			logger.error("该收款纪录所对应的应收对账已经被删除，pk_rece_check_sheet" + rcsrVO.getRelationid());
			throw new BusiException("该收款纪录所对应的应收对账已经被删除！");
		}
		UFDouble receivable_amount = rcsrVO.getReceivable_amount() == null ? new UFDouble(0) : rcsrVO
				.getReceivable_amount();
		UFDouble ungot_amount = rcsVO.getUngot_amount() == null ? new UFDouble(0) : rcsVO.getUngot_amount();
		rcsVO.setUngot_amount(ungot_amount.add(receivable_amount));// 加入到未收款额中
		UFDouble got_amount = rcsVO.getGot_amount() == null ? new UFDouble(0) : rcsVO.getGot_amount();
		rcsVO.setGot_amount(got_amount.sub(receivable_amount));// 已收款额必须减去该记录的收款额
		UFDouble cost_amount = rcsVO.getCost_amount() == null ? new UFDouble(0) : rcsVO.getCost_amount();
		if(rcsVO.getUngot_amount().doubleValue() == cost_amount.doubleValue()) {
			// 如果未收款额和总金额相等，说明该记录完全没有收款。那么将该记录的状态置为已确认
			rcsVO.setVbillstatus(BillStatus.RCS_CONFIRM);
		} else {
			rcsVO.setVbillstatus(BillStatus.RCS_PART_CAVLOAN);
		}
		rcsVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(rcsVO);
		// 删除对账单所对应的应收明细的收款记录
		ReceRecordVO[] rrVOs = dao.queryForSuperVOArrayByCondition(ReceRecordVO.class,
				"relationid=? and receivable_type=?", rcsVO.getPk_rece_check_sheet(),
				ReceiveDetailConst.RECEIVABLE_TYPE.CHECKSHEET.intValue());
		dao.delete(rrVOs);
		// 将所对应的应收明细的状态更新为已对账
		ReceCheckSheetBVO[] childrenVOs = dao.queryForSuperVOArrayByCondition(ReceCheckSheetBVO.class,
				"pk_rece_check_sheet=?", rcsVO.getPk_rece_check_sheet());
		for(ReceCheckSheetBVO childVO : childrenVOs) {
			ReceiveDetailVO rdVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
					childVO.getPk_receive_detail());
			rdVO.setVbillstatus(BillStatus.RD_CHECK);
			rdVO.setGot_amount(new UFDouble(0));
			rdVO.setUngot_amount(rdVO.getCost_amount());
			rdVO.setStatus(VOStatus.UPDATED);
			dao.saveOrUpdate(rdVO);
		}
		// 删除收款纪录
		dao.delete(rcsrVO);
		// 执行公式
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		return execFormula4Templet(rcsVO, uiBillTempletVO, paramVO.isBody(), tableCodes, null);
	}
	
	
	// yaojie 2015-11-10 添加集货应收明细导出功能
	public HSSFWorkbook exportPickupReceCheckSheetRecord(ParamVO paramVO, String[] receDetaiPKs, String strOrderType) {
		logger.info("导出集货应收明细记录开始...");
		Calendar start = Calendar.getInstance();

		HSSFWorkbook wb = CMUtils.exportReceiveDetailRecord(receDetaiPKs, strOrderType);
		logger.info("导出集货明细语句结束...，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
		return wb;
	}
	
	public String getReceCheckSheetCheckType(String check_type){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,check_type);
	}
	
	public String getReceCheckSheetCheckCorp(String checkCorp){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='check_company'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,checkCorp);
	}
	
	public String getReceCheckSheetTaxtCat(String taxtCat){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='tax_category'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,taxtCat);
	}
	
	public String getReceCheckSheetCheckTaxtRate(String taxtRate){
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b With (nolock) "
				+ "LEFT JOIN nw_data_dict With (nolock) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='tax_rate'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,taxtRate.replaceAll("0*$", ""));
	}

	public String getReceCheckSheetCheckHead(String vbillno) {
		ReceCheckSheetVO checkSheetVO = NWDao.getInstance().queryByCondition(ReceCheckSheetVO.class,
				"vbillno = ?", vbillno);
		return checkSheetVO.getCheck_head();
	}
	
	@Override
	protected String getConfirmAndUnconfirmProcName() {
		return "ts_ysdz_check_proc";
	}

}
