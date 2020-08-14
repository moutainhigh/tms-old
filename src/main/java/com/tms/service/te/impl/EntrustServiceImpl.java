package com.tms.service.te.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.IOUtils;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.exp.POI;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.mail.MailSenderInfo;
import org.nw.mail.SimpleMailSender;
import org.nw.mail.ent.EntMailTemplate;
import org.nw.service.ServiceHelper;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.ExpressUtils;
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
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TrackingConst;
import com.tms.constants.ExpAccidentConst.ExpAccidentOrgin;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CarService;
import com.tms.service.base.CarTypeService;
import com.tms.service.base.CarrService;
import com.tms.service.base.CustService;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.service.pod.PodService;
import com.tms.service.te.EntrustService;
import com.tms.service.te.ExpAccidentService;
import com.tms.service.te.TrackingService;
import com.tms.service.tp.PZService;
import com.tms.service.tp.impl.PZUtils;
import com.tms.services.peripheral.WebServicesUtils;
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.base.GoodsVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.rf.EntLineBarBVO;
import com.tms.vo.te.EntInvBVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntOperationBVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntTransHisBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.te.ExpAccidentVO;
import com.tms.vo.tp.SegmentVO;
import com.tms.vo.wh.InstorageBVO;
import com.tms.vo.wh.InstorageVO;
import com.tms.vo.wh.OutstorageBVO;
import com.tms.vo.wh.OutstorageVO;

/**
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:26:41
 */
@Service("entrustService")
public class EntrustServiceImpl extends TMSAbsBillServiceImpl implements EntrustService {
	
	Logger logger = Logger.getLogger(EntrustServiceImpl.class);
	
	@Autowired
	private ExpAccidentService expAccidentService;

	@Autowired
	private CarrService carrService;
	
	@Autowired
	private PZService pZService;
	
	@Autowired
	private CarService carService;
	
	@Autowired
	private CarTypeService carTypeService;

	@Autowired
	private ContractService contractService;
	
	@Autowired
	private TrackingService trackingService;
	
	@Autowired
	private PodService podService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, PayDetailBVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, EntLineBVO.PK_ENTRUST);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_pay_detail_b");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_pay_detail_b");

			billInfo = getRealBillInfo();
			CircularlyAccessibleValueObject[] childrenVO = billInfo.getChildrenVO();
			CircularlyAccessibleValueObject[] newChildrenVO = new CircularlyAccessibleValueObject[childrenVO.length + 1];
			for(int i = 0; i < childrenVO.length; i++) {
				newChildrenVO[i] = childrenVO[i];
			}
			newChildrenVO[childrenVO.length] = childVO2;
			billInfo.setChildrenVO(newChildrenVO);
		}
		return billInfo;
	}

	public AggregatedValueObject getRealBillInfo() {
		AggregatedValueObject billInfo = new ExAggEntrustVO();
		VOTableVO vo = new VOTableVO();

		// 由于是档案型，所以这里手工创建billInfo
		vo.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, EntrustVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, EntrustVO.PK_ENTRUST);
		billInfo.setParentVO(vo);

		VOTableVO childVO = new VOTableVO();
		childVO.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
		childVO.setAttributeValue(VOTableVO.HEADITEMVO, EntPackBVO.class.getName());
		childVO.setAttributeValue(VOTableVO.PKFIELD, EntPackBVO.PK_ENTRUST);
		childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_pack_b");
		childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_pack_b");

		VOTableVO childVO1 = new VOTableVO();
		childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
		childVO1.setAttributeValue(VOTableVO.HEADITEMVO, EntLineBVO.class.getName());
		childVO1.setAttributeValue(VOTableVO.PKFIELD, EntLineBVO.PK_ENTRUST);
		childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_line_b");
		childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_line_b");

		VOTableVO childVO2 = new VOTableVO();
		childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
		childVO2.setAttributeValue(VOTableVO.HEADITEMVO, EntTransbilityBVO.class.getName());
		childVO2.setAttributeValue(VOTableVO.PKFIELD, EntTransbilityBVO.PK_ENTRUST);
		childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_transbility_b");
		childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_transbility_b");
		
		VOTableVO childVO3 = new VOTableVO();
		childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
		childVO3.setAttributeValue(VOTableVO.HEADITEMVO, EntLinePackBVO.class.getName());
		childVO3.setAttributeValue(VOTableVO.PKFIELD, EntLinePackBVO.PK_ENTRUST);
		childVO3.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_line_pack_b");
		childVO3.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_line_pack_b");
		
		VOTableVO childVO4 = new VOTableVO();
		childVO4.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
		childVO4.setAttributeValue(VOTableVO.HEADITEMVO, EntOperationBVO.class.getName());
		childVO4.setAttributeValue(VOTableVO.PKFIELD, EntOperationBVO.PK_ENTRUST);
		childVO4.setAttributeValue(VOTableVO.ITEMCODE, EntOperationBVO.TS_ENT_OPERATION_B);
		childVO4.setAttributeValue(VOTableVO.VOTABLE, EntOperationBVO.TS_ENT_OPERATION_B);

		CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1, childVO2, childVO3, childVO4 };
		billInfo.setChildrenVO(childrenVO);
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
				if(fieldVO.getItemkey().equals("carno")) {
					// 车牌号，因为更新车牌号后会更新到承运商，需要重新计算总计费重,并且重新匹配合同、重新计算总金额
					fieldVO.setUserdefine1("refreshPayDetail()");
					// fieldVO.setRenderer("carnoRenderer");//使用下面的carno_name代替
				} else if(fieldVO.getItemkey().equals("carno_name")) {
					fieldVO.setRenderer("carno_nameRenderer");
				} else if(fieldVO.getItemkey().equals("pk_trans_type")) {
					// 运输方式，需要重新计算总计费重,并且重新匹配合同、重新计算总金额
					fieldVO.setUserdefine1("updateHeaderFeeWeightCount();refreshPayDetail()");
				} else if(fieldVO.getItemkey().equals("pk_carrier") || fieldVO.getItemkey().equals("pk_car_type")) {
					// 承运商、运输方式、重新匹配合同、重新计算总金额
					fieldVO.setUserdefine1("refreshPayDetail()");
				} else if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("req_deli_date")) {
					// 要求提货日期
					fieldVO.setBeforeRenderer("req_deli_dateBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("req_arri_date")) {
					// 要求收货日期
					fieldVO.setBeforeRenderer("req_arri_dateBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("invoice_vbillno")) {
					fieldVO.setRenderer("invoice_vbillnoRenderer");
				}
			} else if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getTable_code().equals(TabcodeConst.TS_ENT_PACK_B)) {
					if(fieldVO.getItemkey().equals("num")) { // 更新件数时，更新其他信息，及表头统计信息
						fieldVO.setUserdefine1("afterEditNum(record)");
					} else if(fieldVO.getItemkey().equals("pack_num_count")) { // 更新数量时，更新其他信息，及表头统计信息
						fieldVO.setUserdefine1("afterEditPackNumCount(record)");
					} else if(fieldVO.getItemkey().equals("volume")) {
						// 编辑体积
						fieldVO.setUserdefine1("afterEditVolume(record)");
					} else if(fieldVO.getItemkey().equals("weight")) {
						// 编辑重量
						fieldVO.setUserdefine1("afterEditWeight(record)");
					}
				} else if(fieldVO.getTable_code().equals(TabcodeConst.TS_ENT_LINE_B)) {
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
		return templetVO;
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		// 查询出来的地址按照行号进行排序
		AggregatedValueObject billVO = ServiceHelper.queryBillVO(this.getRealBillInfo(), paramVO, new String[] { "",
				" order by serialno", "", "" });
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		PayDetailBVO[] detailVOs = getPayDetailBVOsByEntrustBillno(parentVO.getVbillno());
		((IExAggVO) billVO).setTableVO(TabcodeConst.TS_PAY_DETAIL_B, detailVOs);
		return billVO;
	} 

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(!paramVO.isBody()) {
			if(StringUtils.isBlank(orderBy)) {
				orderBy = " order by vbillno desc";
			}
		} else {
			if(TabcodeConst.TS_ENT_LINE_B.equals(paramVO.getTabCode())) {
				orderBy = " order by serialno";
			}
		}
		return orderBy;
	}

	/**
	 * 删除委托单，将运段状态改成待计划
	 */
	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys){
		logger.info("删除委托单，primaryKeys：" + primaryKeys);
		// 根据委托单号返回运段ts_ent_seg_b
		String cond = NWUtils.buildConditionString(primaryKeys);
		//yaojiie 2015 12 15 发货单已被删除，需要重新计算金额和利润
		String invsql  = "SELECT inv.* FROM ts_ent_inv_b ei WITH(NOLOCK) LEFT JOIN ts_invoice inv WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
				+ "WHERE isnull(ei.dr,0)=0 AND isnull(inv.dr,0)=0 AND  ei.pk_entrust in " + cond;
		String sql = "select pk_segment from ts_ent_seg_b WITH(NOLOCK) where isnull(dr,0)=0 and pk_entrust in " + cond;
		List<String> pk_segmentAry = dao.queryForList(sql, String.class);
		cond = NWUtils.buildConditionString(pk_segmentAry.toArray(new String[pk_segmentAry.size()]));
		SegmentVO[] vos = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "isnull(dr,0)=0 and pk_segment in "
				+ cond);
		for(SegmentVO vo : vos) {
			vo.setVbillstatus(BillStatus.SEG_WPLAN); // 改成待调度
			vo.setStatus(VOStatus.UPDATED);
		}
		dao.saveOrUpdate(vos);

		// 删除应付明细
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		List<String> pk_entrusts = new ArrayList<String>();
		for(String primaryKey : primaryKeys) {
			EntrustVO parentVO = getByPrimaryKey(EntrustVO.class, primaryKey);
			pk_entrusts.add(parentVO.getPk_entrust());
			if(parentVO.getVbillstatus().intValue() != BillStatus.ENT_UNCONFIRM) {
				throw new BusiException("必须是待确认的单据才能删除！");
			}
			// 删除应付明细
			PayDetailVO[] children = dao.queryForSuperVOArrayByCondition(PayDetailVO.class, "entrust_vbillno=?",
					parentVO.getVbillno());
			for(PayDetailVO detailVO : children) {
				if(detailVO.getVbillstatus() != BillStatus.NEW) {
					throw new BusiException("委托单所对应的应付明细必须是[新建]状态才能删除委托单！");
				}
				// 删除应付明细子表
				PayDetailBVO[] detailBVOs = dao.queryForSuperVOArrayByCondition(PayDetailBVO.class, "pk_pay_detail=?",
						detailVO.getPk_pay_detail());
				dao.delete(detailBVOs);
			}
			dao.delete(children);
			// 删除委托单关联的分摊费用
			PayDeviBVO[] deviBVOs = dao.queryForSuperVOArrayByCondition(PayDeviBVO.class, "pk_entrust=?",
					parentVO.getPk_entrust());
			if(deviBVOs != null && deviBVOs.length > 0) {
				dao.delete(deviBVOs);
			}
			// 删除委托单
			String primaryCond = NWUtils.buildConditionString(primaryKeys);
			EntInvBVO[] EntInvBVO = dao.queryForSuperVOArrayByCondition(EntInvBVO.class, "pk_entrust in " + primaryCond);
			EntLineBVO[] EntLineBVO = dao.queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust in " + primaryCond);
			EntLinePackBVO[] EntLinePackBVO = dao.queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_entrust in " + primaryCond);
			EntPackBVO[] EntPackBVO  = dao.queryForSuperVOArrayByCondition(EntPackBVO.class, "pk_entrust in " + primaryCond);
			EntrustVO[] EntrustVO  = dao.queryForSuperVOArrayByCondition(EntrustVO.class, "pk_entrust in " + primaryCond);
			EntTransbilityBVO[] EntTransbilityBVO  = dao.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust in " + primaryCond);
			EntTransHisBVO[] EntTransHisBVO  = dao.queryForSuperVOArrayByCondition(EntTransHisBVO.class, "pk_entrust in " + primaryCond);
			EntSegBVO[] EntSegBVO  = dao.queryForSuperVOArrayByCondition(EntSegBVO.class, "pk_entrust in " + primaryCond);
			EntLineBarBVO[] barBVOs = dao.queryForSuperVOArrayByCondition(EntLineBarBVO.class, "pk_entrust in " + primaryCond);
			String[]  entVbillnos = new String[EntrustVO.length];
			
			for(EntrustVO entrustVO : EntrustVO){
				int i = 0;
				entVbillnos[i] = entrustVO.getVbillno();
			}
			String entVbillnoConds = NWUtils.buildConditionString(entVbillnos);
			EntTrackingVO[] EntTrackingVO  = dao.queryForSuperVOArrayByCondition(EntTrackingVO.class, "entrust_vbillno in " + entVbillnoConds);
			
			VOs.addAll(Arrays.asList(EntInvBVO));
			VOs.addAll(Arrays.asList(EntLineBVO));
			VOs.addAll(Arrays.asList(EntLinePackBVO));
			VOs.addAll(Arrays.asList(EntPackBVO));
			VOs.addAll(Arrays.asList(EntrustVO));
			VOs.addAll(Arrays.asList(EntTrackingVO));
			VOs.addAll(Arrays.asList(EntTransbilityBVO));
			VOs.addAll(Arrays.asList(EntTransHisBVO));
			VOs.addAll(Arrays.asList(EntSegBVO));
			VOs.addAll(Arrays.asList(barBVOs));
		}
		dao.delete(VOs,true);
	
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(invsql, InvoiceVO.class);
		CMUtils.totalCostComput(invoiceVOs);
		
		//判断是否要删除ent_lot表
		String lotCond = NWUtils.buildConditionString(pk_entrusts);
		String lotSql = "SELECT DISTINCT ts_entrust_lot.* FROM ts_entrust_lot "
				+ "	WHERE lot IN (  "
				+ "SELECT te2.lot  "
				+ "FROM ts_entrust WITH(NOLOCK)  "
				+ "LEFT JOIN ts_entrust te2 WITH(NOLOCK)  ON ts_entrust.lot=te2.lot "
				+ "WHERE  ts_entrust.pk_entrust  IN  " + lotCond 
				+ "GROUP BY te2.lot 	HAVING	count(DISTINCT te2.dr)=1) ";
		List<EntLotVO> lotVOs = NWDao.getInstance().queryForList(lotSql, EntLotVO.class);
		if(lotVOs != null && lotVOs.size() > 0){
			dao.delete(lotVOs.toArray(new EntLotVO[lotVOs.size()]),true);
		}
		return primaryKeys.length;
	}
	
	public List<SuperVO> cashToPay(EntrustVO parentVO){
		String raceDetailBSql = "SELECT ts_rece_detail_b.* FROM ts_entrust WITH(NOLOCK) "
				+ "LEFT JOIN  ts_ent_seg_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_seg_b.pk_entrust  "
				+ "LEFT JOIN  ts_segment WITH(NOLOCK) ON ts_segment.pk_segment=ts_ent_seg_b.pk_segment "
				+ "LEFT JOIN  ts_ent_inv_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_inv_b.pk_entrust "
				+ "LEFT JOIN  ts_invoice WITH(NOLOCK) ON ts_invoice.pk_invoice=ts_ent_inv_b.pk_invoice "
				+ "LEFT JOIN  ts_receive_detail WITH(NOLOCK) ON ts_receive_detail.invoice_vbillno=ts_invoice.vbillno  AND ts_receive_detail.rece_type=0 "
				+ "LEFT JOIN  ts_rece_detail_b WITH(NOLOCK) ON ts_rece_detail_b.pk_receive_detail = ts_receive_detail.pk_receive_detail "
				+ "WHERE ts_segment.pk_arrival = ts_invoice.pk_arrival AND ts_invoice.balatype = 1 AND ts_invoice.vbillstatus='1' "
				+ "AND ts_segment.invoice_vbillno=ts_invoice.vbillno "	+ "	AND ts_entrust.vbillstatus !=24 "			
				+ "AND isnull(ts_entrust.dr,0) = 0 " + "AND isnull(ts_ent_seg_b.dr,0) = 0 "
				+ "AND isnull(ts_ent_inv_b.dr,0) = 0 " + "AND isnull(ts_invoice.dr,0) = 0 "
				+ "AND isnull(ts_receive_detail.dr,0) = 0 " + "AND isnull(ts_rece_detail_b.dr,0) = 0 "
				+"AND ts_entrust.pk_entrust = " + "'"+ parentVO.getPk_entrust() +"'";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			raceDetailBSql = "SELECT ts_rece_detail_b.* FROM ts_entrust  "
					+ "LEFT JOIN  ts_ent_seg_b  ON ts_entrust.pk_entrust=ts_ent_seg_b.pk_entrust  "
					+ "LEFT JOIN  ts_segment  ON ts_segment.pk_segment=ts_ent_seg_b.pk_segment "
					+ "LEFT JOIN  ts_ent_inv_b  ON ts_entrust.pk_entrust=ts_ent_inv_b.pk_entrust "
					+ "LEFT JOIN  ts_invoice  ON ts_invoice.pk_invoice=ts_ent_inv_b.pk_invoice "
					+ "LEFT JOIN  ts_receive_detail  ON ts_receive_detail.invoice_vbillno=ts_invoice.vbillno  AND ts_receive_detail.rece_type=0 "
					+ "LEFT JOIN  ts_rece_detail_b  ON ts_rece_detail_b.pk_receive_detail = ts_receive_detail.pk_receive_detail "
					+ "WHERE ts_segment.pk_arrival = ts_invoice.pk_arrival AND ts_invoice.balatype = 1 AND ts_invoice.vbillstatus='1' "
					+ "AND ts_segment.invoice_vbillno=ts_invoice.vbillno "	+ "	AND ts_entrust.vbillstatus !=24 "			
					+ "AND nvl(ts_entrust.dr,0) = 0 " + "AND nvl(ts_ent_seg_b.dr,0) = 0 "
					+ "AND nvl(ts_ent_inv_b.dr,0) = 0 " + "AND nvl(ts_invoice.dr,0) = 0 "
					+ "AND nvl(ts_receive_detail.dr,0) = 0 " + "AND nvl(ts_rece_detail_b.dr,0) = 0 "
					+"AND ts_entrust.pk_entrust = " + "'"+ parentVO.getPk_entrust() +"'";
		}
		List<ReceDetailBVO> receDetailBVOs = NWDao.getInstance().queryForList(raceDetailBSql, ReceDetailBVO.class);
		if(receDetailBVOs != null && receDetailBVOs.size() > 0){
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			List<String> receivePks = new ArrayList<String>();
			for(ReceDetailBVO receDetailBVO : receDetailBVOs){
				if(!receivePks.contains(receDetailBVO.getPk_receive_detail())){
					receivePks.add(receDetailBVO.getPk_receive_detail());
				}
			}
			String receiveCond = NWUtils.buildConditionString(receivePks.toArray(new String[receivePks.size()]));
			ReceiveDetailVO[] receiveDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class, "pk_receive_detail in "+receiveCond);
			for(ReceiveDetailVO receiveDetailVO : receiveDetailVOs){
				if(BillStatus.RD_CLOSE == receiveDetailVO.getVbillstatus()){
					continue;
				}
				List<ReceDetailBVO> unitReceDetailBVOs = new ArrayList<ReceDetailBVO>();
				for(ReceDetailBVO receDetailBVO : receDetailBVOs){
					if(receDetailBVO.getPk_receive_detail().equals(receiveDetailVO.getPk_receive_detail())){
						unitReceDetailBVOs.add(receDetailBVO);
					}
				}
				receiveDetailVO.setStatus(VOStatus.UPDATED);
				receiveDetailVO.setVbillstatus(BillStatus.RD_CLOSE);
				toBeUpdate.add(receiveDetailVO);
				PayDetailVO payDetailVO = new PayDetailVO();
				payDetailVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(payDetailVO);
				payDetailVO.setCost_amount(UFDouble.ZERO_DBL.sub(receiveDetailVO.getCost_amount()));
				payDetailVO.setUngot_amount(UFDouble.ZERO_DBL.sub(receiveDetailVO.getUngot_amount()));
				payDetailVO.setPay_type(1);
				//记录批次信息
				payDetailVO.setLot(parentVO.getLot());
				payDetailVO.setVbillstatus(BillStatus.NEW);
				payDetailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
				payDetailVO.setPk_carrier(parentVO.getPk_carrier());
				payDetailVO.setEntrust_vbillno(parentVO.getVbillno());
				payDetailVO.setBalatype(receiveDetailVO.getBalatype());
				payDetailVO.setCurrency(receiveDetailVO.getCurrency());
				payDetailVO.setCreate_time(new UFDateTime(new Date()));
				payDetailVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				payDetailVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				toBeUpdate.add(payDetailVO);
				for(ReceDetailBVO receDetailBVO : unitReceDetailBVOs){
					PayDetailBVO payDetailBVO = new PayDetailBVO();
					payDetailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail());
					payDetailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(payDetailBVO);
					payDetailBVO.setPk_expense_type(receDetailBVO.getPk_expense_type());
					payDetailBVO.setQuote_type(receDetailBVO.getQuote_type());
					payDetailBVO.setPrice_type(receDetailBVO.getPrice_type());
					payDetailBVO.setPrice(receDetailBVO.getPrice());
					payDetailBVO.setValuation_type(receDetailBVO.getValuation_type());
					payDetailBVO.setAmount(UFDouble.ZERO_DBL.sub(receDetailBVO.getAmount()));
					payDetailBVO.setMemo(receDetailBVO.getMemo());
					payDetailBVO.setSystem_create(UFBoolean.TRUE);
					toBeUpdate.add(payDetailBVO);
				}
			}
			return toBeUpdate;
		}
		return new ArrayList<SuperVO>();
	}
	
	@Override
	public SuperVO[] batchConfirm(ParamVO paramVO, String[] ids) {
		logger.info("批量确认委托单");
		EntrustVO[] entVOs = getByPrimaryKeys(EntrustVO.class, ids);
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(EntrustVO entVO : entVOs){
			if(entVO.getVbillstatus().intValue() != BillStatus.ENT_UNCONFIRM) {
				throw new BusiException("必须是[未确认]状态的委托单才能进行确认！");
			}
			//处理现金到付业务
			NWDao.getInstance().saveOrUpdate(this.cashToPay(entVO));
			entVO.setStatus(VOStatus.UPDATED);
			entVO.setVbillstatus(BillStatus.ENT_CONFIRM);
			entVO.setConfirm_date((new UFDateTime(new Date())).toString());
			entVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
			paramVO.setBillId(entVO.getPk_entrust());
			AggregatedValueObject billVO = queryBillVO(paramVO);
			ExAggEntrustVO aggEntrustVO = (ExAggEntrustVO)billVO;
			//当委托单属于分公司结算时，自动产生发货单。
			this.generateInvoice(aggEntrustVO);
			String msg = this.doProcByConfirmAndUnConform(entVO.getPk_entrust(), "1");
			if(StringUtils.isNotBlank(msg)){
				throw new BusiException(msg);
			}
			toBeUpdate.add(entVO);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return entVOs;
	}

	public AggregatedValueObject confirm(ParamVO paramVO) {
		logger.info("确认委托单，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		ExAggEntrustVO aggEntrustVO = (ExAggEntrustVO)billVO;
		if(parentVO.getVbillstatus().intValue() != BillStatus.ENT_UNCONFIRM) {
			throw new BusiException("必须是[未确认]状态的委托单才能进行确认！");
		}
		List<SuperVO> toBeUpdate = this.cashToPay(parentVO);
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.ENT_CONFIRM); // 设置成已确认
		//增加确认时间，确认人 2015-11-10 jonathan
		parentVO.setConfirm_date((new UFDateTime(new Date())).toString());
		parentVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
		//当委托单进行确认时，进行判断，如果这个委托单的承运商时分公司结算类型的，那么会自动产生一条发货单到相应分公司，这个发货单的客户是委托公司
		//判断依据是委托单的承运商的类型。
		//YAOJIIE 1 24 当委托单属于分公司结算时，自动产生发货单。
		this.generateInvoice(aggEntrustVO);
		if(toBeUpdate != null && toBeUpdate.size() > 0){
			dao.saveOrUpdate(toBeUpdate);
		}
		String msg = this.doProcByConfirmAndUnConform(parentVO.getPk_entrust(), "1");
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		dao.saveOrUpdate(billVO);
		return billVO;
	}
	
	//YAOJIIE 1 24 当委托单属于分公司结算时，自动产生发货单。
	public void generateInvoice(ExAggEntrustVO aggEntrustVO){
		EntrustVO parentVO = (EntrustVO) aggEntrustVO.getParentVO();
		//1.查询委托单的客户的类型
		CarrierVO[] carrierVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarrierVO.class, "pk_carrier =?", parentVO.getPk_carrier());
		//一个委托单有且只有一个承运商
		if(carrierVOs[0].getCarr_type() == null || DataDictConst.CARR_TYPE.FGS.intValue() != carrierVOs[0].getCarr_type()){
			return;
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		//这个单子是分公司结算类型
		ExAggInvoiceVO aggInvoiceVO = new ExAggInvoiceVO();
		InvoiceVO invoiceVO = new InvoiceVO();
		toBeUpdate.add(invoiceVO);
		aggInvoiceVO.setParentVO(invoiceVO);
		invoiceVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(invoiceVO);
		invoiceVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.FHD));
		invoiceVO.setVbillstatus(BillStatus.NEW);
		invoiceVO.setCust_orderno(parentVO.getCust_orderno());
		invoiceVO.setOrderno(parentVO.getVbillno());
		String sql = "select * from ts_customer with(nolock) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and cust_type = ? and branch_company =?";
		List<CustomerVO> customerVOs = NWDao.getInstance().queryForList(sql, CustomerVO.class, DataDictConst.CUST_TYPE.FGS.intValue(), parentVO.getPk_corp());
		if(customerVOs.size() == 1){
			invoiceVO.setPk_customer(customerVOs.get(0).getPk_customer());
			invoiceVO.setBala_customer(customerVOs.get(0).getPk_customer());
			invoiceVO.setPk_psndoc(customerVOs.get(0).getPk_psndoc());
			invoiceVO.setPk_dept(customerVOs.get(0).getPk_dept());
			if(StringUtils.isNotBlank(customerVOs.get(0).getBalatype())){
				invoiceVO.setBalatype(Integer.valueOf(customerVOs.get(0).getBalatype()));
			}
		}else{
			throw new BusiException("分公司客户不存在！");
		}
		String corpSql = "select * from ts_carrier with(nolock) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and pk_carrier =?";
		List<CarrierVO> CarrierVOs = NWDao.getInstance().queryForList(corpSql, CarrierVO.class, parentVO.getPk_carrier());
		if(CarrierVOs.size() == 1){
			invoiceVO.setPk_corp(CarrierVOs.get(0).getBranch_company());
		}else{
			throw new BusiException();
		}
		invoiceVO.setMemo(parentVO.getMemo());
		invoiceVO.setPk_trans_type(parentVO.getPk_trans_type());
		invoiceVO.setUrgent_level(parentVO.getUrgent_level());
		invoiceVO.setReq_deli_date(parentVO.getReq_deli_date());
		invoiceVO.setReq_deli_time(parentVO.getReq_deli_time());
		invoiceVO.setReq_arri_date(parentVO.getReq_arri_date());
		invoiceVO.setReq_arri_time(parentVO.getReq_arri_time());
		invoiceVO.setOrder_time(new UFDateTime(new Date()));
		invoiceVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.FGS.intValue());
		invoiceVO.setCreate_user(parentVO.getCreate_user());
		invoiceVO.setCreate_time(new UFDateTime(new Date()));
		invoiceVO.setDeli_city(parentVO.getDeli_city());
		invoiceVO.setDeli_area(parentVO.getDeli_area());
		invoiceVO.setDeli_contact(parentVO.getDeli_contact());
		invoiceVO.setDeli_detail_addr(parentVO.getDeli_detail_addr());
		invoiceVO.setDeli_email(parentVO.getDeli_email());
		invoiceVO.setDeli_mobile(parentVO.getDeli_mobile());
		invoiceVO.setDeli_phone(parentVO.getDeli_phone());
		invoiceVO.setDeli_province(parentVO.getDeli_province());
		invoiceVO.setPk_delivery(parentVO.getPk_delivery());
		invoiceVO.setPk_arrival(parentVO.getPk_arrival());
		invoiceVO.setArri_city(parentVO.getArri_city());
		invoiceVO.setArri_province(parentVO.getArri_province());
		invoiceVO.setArri_area(parentVO.getArri_area());
		invoiceVO.setArri_detail_addr(parentVO.getArri_detail_addr());
		invoiceVO.setArri_contact(parentVO.getArri_contact());
		invoiceVO.setArri_mobile(parentVO.getArri_mobile());
		invoiceVO.setArri_email(parentVO.getArri_email());
		invoiceVO.setArri_phone(parentVO.getArri_phone());
		invoiceVO.setDistance(parentVO.getDistance());
		invoiceVO.setIf_return(parentVO.getIf_return());
		invoiceVO.setNum_count(parentVO.getNum_count());
		invoiceVO.setWeight_count(parentVO.getWeight_count());
		invoiceVO.setVolume_count(parentVO.getVolume_count());
		invoiceVO.setFee_weight_count(parentVO.getFee_weight_count());
		invoiceVO.setCost_amount(parentVO.getCost_amount());
		invoiceVO.setVolume_weight_count(parentVO.getVolume_weight_count());
		invoiceVO.setPack_num_count(parentVO.getPack_num_count());
		invoiceVO.setDbilldate(parentVO.getDbilldate());
		invoiceVO.setItem_code(parentVO.getItem_code());
		invoiceVO.setPk_trans_line(parentVO.getPk_trans_line());
		invoiceVO.setPk_invoice_type("4");
		
		invoiceVO.setDef1(parentVO.getDef1());
		invoiceVO.setDef2(parentVO.getDef2());
		invoiceVO.setDef3(parentVO.getDef3());
		invoiceVO.setDef4(parentVO.getDef4());
		invoiceVO.setDef5(parentVO.getDef5());
		invoiceVO.setDef6(parentVO.getDef6());
		invoiceVO.setDef7(parentVO.getDef7());
		invoiceVO.setDef8(parentVO.getDef8());
		invoiceVO.setDef9(parentVO.getDef9());
		invoiceVO.setDef10(parentVO.getDef10());
		invoiceVO.setDef11(parentVO.getDef11());
		invoiceVO.setDef12(parentVO.getDef12());
		
		EntPackBVO[] entPackBVOs = (EntPackBVO[]) aggEntrustVO.getTableVO(TabcodeConst.TS_ENT_PACK_B);
		if(entPackBVOs != null && entPackBVOs.length > 0){
			for(EntPackBVO entPackBVO : entPackBVOs){
				InvPackBVO invPackBVO = new InvPackBVO();
				invPackBVO.setStatus(VOStatus.NEW);
				toBeUpdate.add(invPackBVO);
				invPackBVO.setPk_invoice(invoiceVO.getPk_invoice());
				NWDao.setUuidPrimaryKey(invPackBVO);
				invPackBVO.setSerialno(entPackBVO.getSerialno());
				invPackBVO.setPk_goods(entPackBVO.getPk_goods());
				invPackBVO.setGoods_code(entPackBVO.getGoods_code());
				invPackBVO.setGoods_name(entPackBVO.getGoods_name());
				invPackBVO.setNum(entPackBVO.getNum());
				invPackBVO.setPack(entPackBVO.getPack());
				invPackBVO.setWeight(entPackBVO.getWeight());
				invPackBVO.setVolume(entPackBVO.getVolume());
				invPackBVO.setUnit_volume(entPackBVO.getUnit_volume());
				invPackBVO.setUnit_weight(entPackBVO.getUnit_weight());
				invPackBVO.setLength(entPackBVO.getLength());
				invPackBVO.setWidth(entPackBVO.getWidth());
				invPackBVO.setHeight(entPackBVO.getHeight());
				invPackBVO.setTrans_note(entPackBVO.getTrans_note());
				invPackBVO.setLow_temp(entPackBVO.getLow_temp());
				invPackBVO.setHight_temp(entPackBVO.getHight_temp());
				invPackBVO.setReference_no(entPackBVO.getReference_no());
				invPackBVO.setMemo(entPackBVO.getMemo());
				invPackBVO.setPack_num_count(entPackBVO.getPack_num_count());
				invPackBVO.setPlan_num(entPackBVO.getPlan_num());
				invPackBVO.setPlan_pack_num_count(entPackBVO.getPlan_pack_num_count());
				
				invPackBVO.setDef1(entPackBVO.getDef1());
				invPackBVO.setDef2(entPackBVO.getDef2());
				invPackBVO.setDef3(entPackBVO.getDef3());
				invPackBVO.setDef4(entPackBVO.getDef4());
				invPackBVO.setDef5(entPackBVO.getDef5());
				invPackBVO.setDef6(entPackBVO.getDef6());
				invPackBVO.setDef7(entPackBVO.getDef7());
				invPackBVO.setDef8(entPackBVO.getDef8());
				invPackBVO.setDef9(entPackBVO.getDef9());
				invPackBVO.setDef10(entPackBVO.getDef10());
				invPackBVO.setDef11(entPackBVO.getDef11());
				invPackBVO.setDef12(entPackBVO.getDef12());
			}
		}
		EntTransbilityBVO[] entTransbilityBVOs = (EntTransbilityBVO[]) aggEntrustVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B);
		if(entTransbilityBVOs != null && entTransbilityBVOs.length > 0){
			for(EntTransbilityBVO entTransbilityBVO : entTransbilityBVOs){
				TransBilityBVO transBilityBVO = new TransBilityBVO();
				toBeUpdate.add(transBilityBVO);
				transBilityBVO.setStatus(VOStatus.NEW);
				transBilityBVO.setPk_invoice(invoiceVO.getPk_invoice());
				NWDao.setUuidPrimaryKey(transBilityBVO);
				transBilityBVO.setPk_car_type(entTransbilityBVO.getPk_car_type());
				transBilityBVO.setNum(entTransbilityBVO.getNum());
				transBilityBVO.setMemo(entTransbilityBVO.getMemo());
				
				transBilityBVO.setDef1(entTransbilityBVO.getDef1());
				transBilityBVO.setDef2(entTransbilityBVO.getDef2());
				transBilityBVO.setDef3(entTransbilityBVO.getDef3());
				transBilityBVO.setDef4(entTransbilityBVO.getDef4());
				transBilityBVO.setDef5(entTransbilityBVO.getDef5());
				transBilityBVO.setDef6(entTransbilityBVO.getDef6());
				transBilityBVO.setDef7(entTransbilityBVO.getDef7());
				transBilityBVO.setDef8(entTransbilityBVO.getDef8());
				transBilityBVO.setDef9(entTransbilityBVO.getDef9());
				transBilityBVO.setDef10(entTransbilityBVO.getDef10());
				transBilityBVO.setDef11(entTransbilityBVO.getDef11());
				transBilityBVO.setDef12(entTransbilityBVO.getDef12());
			}
		}
		//利用委托单上的信息匹配合同
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, invoiceVO.getBala_customer(),
				parentVO.getPk_trans_type(), parentVO.getPk_delivery(), parentVO.getPk_arrival(),
				parentVO.getDeli_city(), parentVO.getArri_city(), parentVO.getPk_corp(),
				parentVO.getReq_arri_date(),parentVO.getUrgent_level(),parentVO.getItem_code(),parentVO.getPk_trans_line(),parentVO.getIf_return());
		
		if(contractBVOs != null && contractBVOs.size() > 0){
			String[] pk_car_type = null;
			if (entTransbilityBVOs != null && entTransbilityBVOs.length > 0) {
				pk_car_type = new String[entTransbilityBVOs.length];
				for (int i = 0; i < entTransbilityBVOs.length; i++) {
					pk_car_type[i] = entTransbilityBVOs[i].getPk_car_type();
				}
			}
			
			
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
			//对包装按照pack进行分组
			for(EntPackBVO entPackBVO : entPackBVOs){
				String key = entPackBVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<EntPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<EntPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(entPackBVO);
			}
			if (groupMap.size() > 0) {
				for(String key : groupMap.keySet()){
					PackInfo packInfo = new PackInfo();
					List<EntPackBVO> voList = groupMap.get(key);
					Integer num = 0;
					UFDouble weight = UFDouble.ZERO_DBL;
					UFDouble volume = UFDouble.ZERO_DBL;
					for(EntPackBVO packBVO : voList){
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
			
			List<ReceDetailBVO> receDetailBVOs = contractService.buildReceDetailBVO(invoiceVO.getBala_customer(),
					parentVO.getPack_num_count() == null ? 0 : parentVO.getPack_num_count().doubleValue(),
					parentVO.getNum_count() == null ? 0 : parentVO.getNum_count(),
					parentVO.getFee_weight_count() == null ? 0 : parentVO.getFee_weight_count().doubleValue(),
					parentVO.getWeight_count() == null ? 0 : parentVO.getWeight_count().doubleValue(),
					parentVO.getVolume_count() == null ? 0 : parentVO.getVolume_count().doubleValue(),packInfos, pk_car_type,
					parentVO.getPk_corp(), contractBVOs);
			//r如果利用合同算出了金额
			if (receDetailBVOs != null && receDetailBVOs.size() > 0){
				for (ReceDetailBVO detailBVO : receDetailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
					toBeUpdate.add(detailBVO);
				}
				// 重新设置新的费用明细
				aggInvoiceVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B,receDetailBVOs.toArray(new ReceDetailBVO[receDetailBVOs.size()]));
			}
		}
		//新建一个应收明细
		ReceiveDetailVO receiveDetailVO = new ReceiveDetailVO();
		toBeUpdate.add(receiveDetailVO);
		receiveDetailVO.setStatus(VOStatus.NEW);
		receiveDetailVO.setVbillstatus(BillStatus.NEW);
		receiveDetailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YSMX)); // 生成应收明细的单据号
		if (WebUtils.getLoginInfo() == null) {
			// 如果没有登录信息，可能是通过其他系统导入的形式
			receiveDetailVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			receiveDetailVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		} else {
			receiveDetailVO.setCreate_user(invoiceVO.getCreate_user());
			receiveDetailVO.setPk_corp(CarrierVOs.get(0).getBranch_company());
		}
		receiveDetailVO.setCreate_time(new UFDateTime(new Date()));
		NWDao.setUuidPrimaryKey(receiveDetailVO);
		receiveDetailVO.setDbilldate(new UFDate());
		receiveDetailVO.setPk_customer(invoiceVO.getPk_customer());
		receiveDetailVO.setBala_customer(invoiceVO.getBala_customer());
		receiveDetailVO.setCurrency(ParameterHelper.getCurrency());

		receiveDetailVO.setPack_num_count(invoiceVO.getPack_num_count());
		receiveDetailVO.setNum_count(invoiceVO.getNum_count());
		receiveDetailVO.setFee_weight_count(invoiceVO.getFee_weight_count());
		receiveDetailVO.setWeight_count(invoiceVO.getWeight_count());
		receiveDetailVO.setVolume_count(invoiceVO.getVolume_count());
		receiveDetailVO.setCost_amount(invoiceVO.getCost_amount());
		receiveDetailVO.setUngot_amount(invoiceVO.getCost_amount());// 未收金额等于总金额
		receiveDetailVO.setBalatype(invoiceVO.getBalatype());
		receiveDetailVO.setInvoice_vbillno(invoiceVO.getVbillno()); // 发货单单据号
		receiveDetailVO.setMemo(invoiceVO.getMemo());
		receiveDetailVO.setRece_type(ReceiveDetailConst.ORIGIN_TYPE); // 表示这是由发货单生成的应收明细
		receiveDetailVO.setMerge_type(ReceiveDetailConst.MERGE_TYPE.UNMERGE.intValue()); // 合并类型
		if (contractBVOs != null && contractBVOs.size() > 0) {
			receiveDetailVO.setTax_cat(contractBVOs.get(0).getTax_cat());
			receiveDetailVO.setTax_rate(contractBVOs.get(0).getTax_rate());
			// 取第一行合同明细的税种，税率
			receiveDetailVO.setTaxmny(CMUtils.getTaxmny(receiveDetailVO.getCost_amount(), receiveDetailVO.getTax_cat(),
					receiveDetailVO.getTax_rate()));
		}
		ReceDetailBVO[] receDetailBVOs = (ReceDetailBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);	
		if(receDetailBVOs != null && receDetailBVOs.length > 0){
			for (ReceDetailBVO receDetailBVO : receDetailBVOs) {
				if (receDetailBVO.getStatus() == VOStatus.NEW) {
					NWDao.setUuidPrimaryKey(receDetailBVO);
				}
				receDetailBVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail()); // 设置主表的主键
			}
			// 重新设置到发货单
			parentVO.setCost_amount(receiveDetailVO.getCost_amount());
			// 重新统计费用
			CMUtils.processExtenal(receiveDetailVO,receDetailBVOs);
		}
		aggInvoiceVO.removeTableVO(TabcodeConst.TS_RECE_DETAIL_B); // 这个VO其实是不属于这个主表的，放在这边主要是维护方便
		dao.saveOrUpdate(toBeUpdate);
	}
	

	@Override
	public SuperVO[] batchUnconfirm(ParamVO paramVO, String[] ids) {
		EntrustVO[] entVOs = getByPrimaryKeys(EntrustVO.class, ids);
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(EntrustVO entVO : entVOs){
			if(entVO.getVbillstatus().intValue() != BillStatus.ENT_CONFIRM) {
				throw new BusiException("必须是[已确认]状态的委托单才能进行反确认！委托单号[?]！",entVO.getVbillno());
			}
			entVO.setStatus(VOStatus.UPDATED);
			entVO.setAttributeValue(getBillStatusField(), BillStatus.ENT_UNCONFIRM); // 设置成待确认
			entVO.setAttributeValue(getUnConfirmTypeField(), paramVO.getUnconfirmType());//反确认类型
			entVO.setAttributeValue(getUnConfirmMemoField(), paramVO.getUnconfirmMemo());//反确认说明
			entVO.setUnconfirm_date((new UFDateTime(new Date())).toString());
			entVO.setUnconfirm_user(WebUtils.getLoginInfo().getPk_user());
			String msg = this.doProcByConfirmAndUnConform(entVO.getPk_entrust(), "0");
			if(StringUtils.isNotBlank(msg)){
				throw new BusiException(msg);
			}
			toBeUpdate.add(entVO);
		}
		String sql = "SELECT ts_entrust.vbillno FROM ts_entrust WITH(NOLOCK) "
				+ " LEFT JOIN ts_ent_inv_b WITH(NOLOCK) ON ts_entrust.pk_entrust = ts_ent_inv_b.pk_entrust "
				+ " LEFT JOIN ts_invoice WITH(NOLOCK) ON ts_invoice.pk_invoice = ts_ent_inv_b.pk_invoice "
				+ " WHERE isnull(ts_entrust.dr,0)=0 AND isnull(ts_ent_inv_b.dr,0)=0 AND isnull(ts_invoice.dr,0)=0 "
				+ " AND ts_invoice.vbillstatus IN (4,5) AND ts_entrust.pk_entrust IN " + NWUtils.buildConditionString(ids);
		List<String> podEntrusts = NWDao.getInstance().queryForList(sql, String.class);
		if(podEntrusts != null && podEntrusts.size() > 0){
			throw new BusiException("该委托单包含的发货单已经签收或回单过，不能反确认，委托单号[?]！",podEntrusts.get(0));
		}
		
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return entVOs;
	}
	
	public AggregatedValueObject unconfirm(ParamVO paramVO) {
		logger.info("反确认委托单，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		if(parentVO.getVbillstatus().intValue() != BillStatus.ENT_CONFIRM) {
			throw new BusiException("必须是[已确认]状态的委托单才能进行反确认！");
		}

		// 如果委托单所对应的发货单已经做了签收回单，那么委托单不能反确认
		List<InvoiceVO> invoiceVOs = dao.queryForList(
						"select * from ts_invoice where isnull(dr,0)=0 and pk_invoice in (select pk_invoice from ts_ent_inv_b where isnull(dr,0)=0 and pk_entrust=?)",
						InvoiceVO.class, parentVO.getPk_entrust());
		if(invoiceVOs != null) {
			for(InvoiceVO invVO : invoiceVOs) {
				int status = invVO.getVbillstatus().intValue();
				if(status == BillStatus.INV_SIGN || status == BillStatus.INV_BACK) {
					throw new BusiException("该委托单包含的发货单已经签收或回单过，不能反确认，委托单号[?]！",parentVO.getVbillno());
				}
			}
		}

		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.ENT_UNCONFIRM); // 设置成待确认
		parentVO.setAttributeValue(getUnConfirmTypeField(), paramVO.getUnconfirmType());//反确认类型
		parentVO.setAttributeValue(getUnConfirmMemoField(), paramVO.getUnconfirmMemo());//反确认说明
		//增加确认时间，确认人 2015-11-10 jonathan
		parentVO.setUnconfirm_date((new UFDateTime(new Date())).toString());
		parentVO.setUnconfirm_user(WebUtils.getLoginInfo().getPk_user());
		String msg = this.doProcByConfirmAndUnConform(parentVO.getPk_entrust(), "0");
		if(StringUtils.isNotBlank(msg)){
			throw new BusiException(msg);
		}
		dao.saveOrUpdate(billVO);
		return billVO;
	}
	

	//yaojiie 2015 12 27 修改退单方法，在退单时产生异常事故。
	public AggregatedValueObject vent(ParamVO paramVO) {
		logger.info("退单委托单，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		if(parentVO.getVbillstatus().intValue() != BillStatus.ENT_CONFIRM && parentVO.getVbillstatus().intValue() != BillStatus.ENT_DELIVERY) {
			throw new BusiException("只有 [已确认、已提货]状态 的委托单才能退单，单据号[?]！",parentVO.getVbillno());
		}
		ExAggEntrustVO aggvo = (ExAggEntrustVO)billVO;
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "entrust_vbillno =?", parentVO.getVbillno());
		if(payDetailVOs != null && payDetailVOs.length > 0){
			for(PayDetailVO payDetailVO : payDetailVOs){
				if(payDetailVO.getVbillstatus() != BillStatus.NEW){
					throw new BusiException("退单时，对应的应付明细状态必须是[新建]状态！");
				}
			}
		}
		
		EntLineBVO[] lineBVOs = (EntLineBVO[]) aggvo.getTableVO(TabcodeConst.TS_ENT_LINE_B);
		if(lineBVOs.length != 2){
			throw new BusiException("只有两个节点的委托单才能退单，单据号[?]！",parentVO.getVbillno());
		}
		lineBVOs[0].setAct_arri_date(null);
		lineBVOs[0].setAct_leav_date(null);
		lineBVOs[0].setMemo(null);
		lineBVOs[0].setStatus(VOStatus.UPDATED); // 清空实际到达时间、实际离开时间
		lineBVOs[0].setArrival_flag(new UFBoolean(false));// 标识为未确认
		toBeUpdate.add(lineBVOs[0]);
		
		
		// 根据委托单号返回运段ts_ent_seg_b
		String sql = "select pk_segment from ts_ent_seg_b WITH(NOLOCK) where isnull(dr,0)=0 and pk_entrust=? ";
		List<String> pk_segmentAry = dao.queryForList(sql, String.class, parentVO.getPk_entrust());
	
		String cond = NWUtils.buildConditionString(pk_segmentAry.toArray(new String[pk_segmentAry.size()]));
		SegmentVO[] vos = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "isnull(dr,0)=0 and pk_segment in "
				+ cond);
		for(SegmentVO vo : vos) {
			vo.setVbillstatus(BillStatus.SEG_WPLAN); // 改成待调度
			//yaojiie 2016 1 6 退单时，增加退单信息。
			vo.setIf_vent(UFBoolean.TRUE);
			vo.setVent_time(new UFDateTime(new Date()));
			vo.setVent_user(WebUtils.getLoginInfo().getPk_user());
			vo.setVent_reason(paramVO.getVentMemo());
			vo.setVent_type(paramVO.getVentType());
			vo.setStatus(VOStatus.UPDATED);
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.ENT_VENT); // 设置成退单
		toBeUpdate.add(parentVO);
		toBeUpdate.addAll(Arrays.asList(vos));
		//yaojiie 2015 12 27 当退单时产生异常事故
		ExpAccidentVO expAccidentVO = this.getExpAccidentVO(parentVO,paramVO);
		toBeUpdate.add(expAccidentVO);
		
		//在这里调用分公司结算和现金到付的退单方法，这样删除方法就可以一起执行了。
		List<SuperVO> cashToPay = this.ventCashToPay(new String[]{paramVO.getBillId()});
		List<SuperVO> FGS = this.ventWithFGS(parentVO);
		if(cashToPay != null && cashToPay.size() > 0){
			toBeUpdate.addAll(cashToPay);
		}
		if(FGS != null && FGS.size() > 0){
			toBeUpdate.addAll(FGS);
		}
		dao.saveOrUpdate(toBeUpdate);

		Map<String, Object> retMap = execFormula4Templet(billVO, paramVO);
		return billVO;
	}
	//对于分公司结算类型的单据，在委托单退单的时候需要将其生成的发货单和应收明细一并删除
	//但是这个委托的的应付明细数据是根据其生成的发货单的应收明细产生，当其对应的应收明细状态为新建状态是，这个
	public List<SuperVO> ventWithFGS(EntrustVO entrustVO){
		//yaojiie 2016 1 21 退单时执行分公司结算逻辑
		CarrierVO[] carrierVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarrierVO.class, "pk_carrier =?", entrustVO.getPk_carrier());
		//一个委托单有且只有一个承运商
		if(carrierVOs[0].getCarr_type() != null && DataDictConst.CARR_TYPE.FGS.intValue() == carrierVOs[0].getCarr_type()){
			String sql = "select * from ts_invoice with(nolock) where isnull(dr,0) = 0 "
					+ "and orderno =? and invoice_origin =?";
			List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, entrustVO.getVbillno(),DataDictConst.INVOICE_ORIGIN.FGS.intValue());
			if(invoiceVOs == null || invoiceVOs.size() == 0){
				return null;
			}
			//只会存在一个发货单
			InvoiceVO parentVO = invoiceVOs.get(0);
			List<SuperVO> toBeUpdate = deleteInv(parentVO);
			return toBeUpdate;
		}
		return null;
	}
	
	private List<SuperVO> deleteInv(InvoiceVO parentVO){
		// yaojiie 2016 1 3 当删除发货单时，需要删除这个发货单下的所有子表
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		if (parentVO.getVbillstatus().intValue() != BillStatus.NEW) {
			throw new BusiException("分公司结算时，发货单必须是[新建]状态才能删除，单据号[?]！",parentVO.getVbillno() );
		}
		// 删除应收明细
		ReceiveDetailVO[] children = dao.queryForSuperVOArrayByCondition(ReceiveDetailVO.class, "invoice_vbillno=?",
				parentVO.getVbillno());
		List<String> pk_receive_detail = new ArrayList<String>();
		for (ReceiveDetailVO detailVO : children) {
			if (detailVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("分公司结算时，发货单所对应的应付明细必须是[新建]状态才能删除发货单！");
			}
			pk_receive_detail.add(detailVO.getPk_receive_detail());
			detailVO.setStatus(VOStatus.DELETED);
		}
		// 删除应收明细子表
		ReceDetailBVO[] detailBVOs = dao.queryForSuperVOArrayByCondition(ReceDetailBVO.class, "pk_receive_detail in "
				+ NWUtils.buildConditionString(pk_receive_detail.toArray(new String[pk_receive_detail.size()])));
		if (detailBVOs != null && detailBVOs.length > 0) {
			for(ReceDetailBVO detailBVO : detailBVOs){
				detailBVO.setStatus(VOStatus.DELETED);
			}
			toBeUpdate.addAll(Arrays.asList(detailBVOs));
		}
		toBeUpdate.addAll(Arrays.asList(children));
		parentVO.setStatus(VOStatus.DELETED);
		toBeUpdate.add(parentVO);
		// 删除发货单
		InvLineBVO[] invLineBVOs = dao.queryForSuperVOArrayByCondition(InvLineBVO.class,
				"pk_invoice =? " ,parentVO.getPk_invoice());
		if(invLineBVOs != null && invLineBVOs.length > 0){
			for(InvLineBVO lineBVO : invLineBVOs){
				lineBVO.setStatus(VOStatus.DELETED);
			}
			toBeUpdate.addAll(Arrays.asList(invLineBVOs));
		}
		
		InvPackBVO[] invPackBVOs = dao.queryForSuperVOArrayByCondition(InvPackBVO.class,
				"pk_invoice =? " ,parentVO.getPk_invoice());
		if(invPackBVOs != null && invPackBVOs.length > 0){
			for(InvPackBVO packBVO : invPackBVOs){
				packBVO.setStatus(VOStatus.DELETED);
			}
			toBeUpdate.addAll(Arrays.asList(invPackBVOs));
		}
		
		TransBilityBVO[] transBilityBVOs = dao.queryForSuperVOArrayByCondition(TransBilityBVO.class,
				"pk_invoice =? " ,parentVO.getPk_invoice());
		if(transBilityBVOs != null && transBilityBVOs.length > 0){
			for(TransBilityBVO transBilityBVO : transBilityBVOs){
				transBilityBVO.setStatus(VOStatus.DELETED);
			}
			toBeUpdate.addAll(Arrays.asList(transBilityBVOs));
		}
		
		return toBeUpdate;
	}
	
	public List<SuperVO> ventCashToPay(String[] billId){
		if(billId == null || billId.length == 0){
			return null;
		}
		String entCond = NWUtils.buildConditionString(billId);
		String sql = "select ts_pay_detail.* from ts_pay_detail with(nolock) "
				+ "left join ts_entrust on ts_entrust.vbillno = ts_pay_detail.entrust_vbillno "
				+ " where isnull(ts_entrust.dr,0) = 0 and  isnull(ts_pay_detail.dr,0) = 0 "
				+ "and ts_pay_detail.pay_type =1 "
				+ "and ts_pay_detail.balatype = 1 and ts_entrust.pk_entrust in " + entCond;
		List<PayDetailVO> detailVOs = NWDao.getInstance().queryForList(sql, PayDetailVO.class);
		if(detailVOs != null && detailVOs.size() > 0){
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			for(PayDetailVO detailVO : detailVOs){
				if(BillStatus.NEW == detailVO.getVbillstatus()){
					detailVO.setStatus(VOStatus.DELETED);
					toBeUpdate.add(detailVO);
				}else{
					throw new BusiException("退单时，现金到付调整明细状态必须是新建的[" + detailVO.getVbillno()+"]");
				}
			}
			String receiveDetailSql = "SELECT ts_receive_detail.* FROM ts_entrust WITH(NOLOCK) "
					+ "LEFT JOIN  ts_ent_seg_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_seg_b.pk_entrust  "
					+ "LEFT JOIN  ts_segment WITH(NOLOCK) ON ts_segment.pk_segment=ts_ent_seg_b.pk_segment "
					+ "LEFT JOIN  ts_ent_inv_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_inv_b.pk_entrust "
					+ "LEFT JOIN  ts_invoice WITH(NOLOCK) ON ts_invoice.pk_invoice=ts_ent_inv_b.pk_invoice "
					+ "LEFT JOIN  ts_receive_detail WITH(NOLOCK) ON ts_receive_detail.invoice_vbillno=ts_invoice.vbillno  AND ts_receive_detail.rece_type=0 "
					+ "WHERE ts_segment.pk_arrival = ts_invoice.pk_arrival AND ts_invoice.balatype = 1 AND ts_invoice.vbillstatus='1' "
					+ "AND ts_segment.invoice_vbillno=ts_invoice.vbillno "	
					+ "AND isnull(ts_entrust.dr,0) = 0 " + "AND isnull(ts_ent_seg_b.dr,0) = 0 "
					+ "AND isnull(ts_ent_inv_b.dr,0) = 0 " + "AND isnull(ts_invoice.dr,0) = 0 "
					+ "AND isnull(ts_receive_detail.dr,0) = 0 "
					+"AND ts_entrust.pk_entrust in " + entCond;
			List<ReceiveDetailVO> ReceiveDetailVOs = NWDao.getInstance().queryForList(receiveDetailSql, ReceiveDetailVO.class);
			if(ReceiveDetailVOs != null && ReceiveDetailVOs.size() > 0){
				for(ReceiveDetailVO detailVO : ReceiveDetailVOs){
					if(BillStatus.RD_CLOSE == detailVO.getVbillstatus()){
						detailVO.setStatus(VOStatus.UPDATED);
						detailVO.setModify_time(new UFDateTime(new Date()));
						detailVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
						detailVO.setVbillstatus(BillStatus.NEW);
						toBeUpdate.add(detailVO);
					}
				}
			}
			return toBeUpdate;
		}
		return null;
	}
	
	//委托单退单时，产生异常事故
	public ExpAccidentVO getExpAccidentVO(EntrustVO parentVO ,ParamVO paramVO){
		parentVO.setExp_flag(UFBoolean.TRUE);
		parentVO.setExp_type("9");//异常类型来自数据字典维护的 固定是9.
		parentVO.setVent_type(paramVO.getVentType());
		parentVO.setVent_memo(paramVO.getVentMemo());
		ExpAccidentVO expAccidentVO = new ExpAccidentVO();
		if(expAccidentVO.getDbilldate() == null) {
			expAccidentVO.setDbilldate(new UFDate());
		}
		expAccidentVO.setOrigin(ExpAccidentOrgin.VENT.toString());// /设置异常的来源是“异常跟踪”
		expAccidentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		expAccidentVO.setCreate_time(new UFDateTime(new Date()));
		expAccidentVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		expAccidentVO.setEntrust_vbillno(parentVO.getVbillno());
		expAccidentVO.setInvoice_vbillno(parentVO.getInvoice_vbillno());
		expAccidentVO.setPk_carrier(parentVO.getPk_carrier());
		expAccidentVO.setMemo(paramVO.getVentMemo());
		expAccidentVO.setReason_memo(paramVO.getVentMemo());
		expAccidentVO.setReason_type(paramVO.getVentType().toString());
		expAccidentVO.setExp_type("9");
		expAccidentVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YCSG));
		expAccidentVO.setVbillstatus(BillStatus.NEW);
		expAccidentVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(expAccidentVO);
		return expAccidentVO;
	}
	
	public AggregatedValueObject unvent(ParamVO paramVO) {
		logger.info("撤销退单委托单，billId：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		if(parentVO.getVbillstatus().intValue() != BillStatus.ENT_VENT) {
			throw new BusiException("只有退单的委托单才能撤销，PK[?]！",parentVO.getPk_entrust());
		}
		// 根据委托单号返回运段ts_ent_seg_b
		String sql = "select pk_segment from ts_ent_seg_b WITH(NOLOCK) where isnull(dr,0)=0 and pk_entrust=? ";
		List<String> pk_segmentAry = dao.queryForList(sql, String.class, parentVO.getPk_entrust());
		String cond = NWUtils.buildConditionString(pk_segmentAry.toArray(new String[pk_segmentAry.size()]));
		SegmentVO[] vos = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "isnull(dr,0)=0 and pk_segment in "
				+ cond);
		for(SegmentVO vo : vos) {
			vo.setVbillstatus(BillStatus.SEG_DISPATCH); // 改成已调度
			//yaojiie 2016 1 6 撤销退单时，删除退单信息。
			vo.setIf_vent(UFBoolean.FALSE);
			vo.setVent_time(null);
			vo.setVent_user(null);
			vo.setVent_reason(null);
			vo.setVent_type(null);
			vo.setStatus(VOStatus.UPDATED);
		}
		dao.saveOrUpdate(vos);

		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.ENT_CONFIRM); // 设置成已确认
		//增加确认时间，确认人 2015-11-10 jonathan
		parentVO.setConfirm_date((new UFDateTime(new Date())).toString());
		parentVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
		
		dao.saveOrUpdate(billVO);
		return billVO;
	}

	public List<Map<String, Object>> loadEntLineB(String pk_entrust) {
		if(StringUtils.isBlank(pk_entrust)) {
			return null;
		}
		String unitSql = "SELECT pk_ent_line_b, dr, ts, pk_entrust, serialno, pk_address, pk_city, pk_province, pk_area,"
				+ " detail_addr, contact, mobile, phone, email, memo, req_arri_date, req_leav_date, addr_flag,"
				+ " pk_segment, act_arri_date, act_leav_date, arrival_flag, segment_node, def1, def2, def3, def4, "
				+ " def5, def6, def7, def8, def9, def10, def11, def12, curr_longitude, curr_latitude, mileage, app_detail_addr, "
				+ " CASE operate_type WHEN 0 THEN 'Start' WHEN 1 THEN 'Pickup' WHEN 2 THEN 'Delivery' WHEN 3 THEN 'End' END operate_name,operate_type "
				+ " FROM ts_ent_line_b WHERE dr = 0";
		String sql = unitSql + " AND ts_ent_line_b.pk_entrust = " + "'"+pk_entrust+"'"+" ORDER BY ts_ent_line_b.req_arri_date asc ";
		
		List<EntLineBVO> lineVOs = NWDao.getInstance().queryForList(sql, EntLineBVO.class);
		if(lineVOs == null || lineVOs.size() == 0){
			return null;
		}

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(lineVOs.size());
		for(SuperVO vo : lineVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				
				map.put(key, vo.getAttributeValue(key));
			}
			if(map.get(EntLineBVO.ARRIVAL_FLAG) == null
					|| Constants.N.equals(map.get(EntLineBVO.ARRIVAL_FLAG).toString())) {
				// 如果非到货节点，实际到达时间默认等于要求到达时间，实际离开时间默认等于要求离开时间
				// map.put(EntLineBVO.ACT_ARRI_DATE,
				// map.get(EntLineBVO.REQ_ARRI_DATE));
				// map.put(EntLineBVO.ACT_LEAV_DATE,
				// map.get(EntLineBVO.REQ_LEAV_DATE));
			}
			mapList.add(map);
		}
		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, getEntLineBFormulas(), true);
		return list;
	}
	
	//yaojiie 2015 12 27 添加此方法，根据PK值，加载此单价下所有的路线包装明细
	public List<Map<String, Object>> loadEntLinePackB(String pk_ent_line_b) {
		if(StringUtils.isBlank(pk_ent_line_b)) {
			return null;
		}
		EntLinePackBVO[] entLinePackBVOs = dao.queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_ent_line_b=? order by serialno",pk_ent_line_b);
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(entLinePackBVOs.length);
		for(SuperVO vo : entLinePackBVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		return mapList;
	}

	private String[] getEntLineBFormulas() {
		return new String[] {
				"addr_code,addr_name,pk_city,pk_province,pk_area,detail_addr->getcolsvalue(\"ts_address\",\"addr_code\",\"addr_name\",\"pk_city\",\"pk_province\",\"pk_area\",\"detail_addr\",\"pk_address\",pk_address)",
				"city_name->getColValue(ts_area, name, pk_area, pk_city)",
				"province_name->getColValue(ts_area, name, pk_area, pk_province)",
				"area_name->getColValue(ts_area, name, pk_area, pk_area)" };
	}

	public List<Map<String, Object>> loadEntTransbilityB(String pk_entrust) {
		EntTransbilityBVO[] tbVOs = dao.queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust=?",
				pk_entrust);
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(tbVOs.length);
		for(SuperVO vo : tbVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.ENTRUST_CODE);
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(this.getBillTemplateID(paramVO));
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, true,
				new String[] { TabcodeConst.TS_ENT_TRANSBILITY_B }, null);
		return list;
	}

	public AggregatedValueObject show(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		// 读取费用明细
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		PayDetailBVO[] detailBVOs = getPayDetailBVOsByEntrustBillno(parentVO.getVbillno());
		((IExAggVO) billVO).setTableVO(TabcodeConst.TS_PAY_DETAIL_B, detailBVOs);
		return billVO;
	}

	/**
	 * 根据发货单的单据号查询费用明细记录
	 * 
	 * @param entrustBillno
	 * @return
	 */
	public PayDetailBVO[] getPayDetailBVOsByEntrustBillno(String entrustBillno) {
		PayDetailBVO[] detailBVOs = dao
				.queryForSuperVOArrayByCondition(
						PayDetailBVO.class,
						"pk_pay_detail=(select pk_pay_detail from ts_pay_detail WITH(NOLOCK) where entrust_vbillno=? and pay_type=? and isnull(dr,0)=0)",
						entrustBillno, PayDetailConst.ORIGIN_TYPE);
		return detailBVOs;
	}

	/**
	 * 返回原始类型的应付明细
	 * 
	 * @param entrustBillno
	 * @return
	 */
	public PayDetailVO getPayDetailVOsByEntrustBillno(String entrustBillno) {
		PayDetailVO detailVO = dao.queryByCondition(PayDetailVO.class, "entrust_vbillno=? and pay_type=0", entrustBillno);
		return detailVO;
	}

	/**
	 * 重新匹配合同，刷新费用明细,参考EntrustUtils.getPayDetailBVOs
	 */
	public List<Map<String, Object>> refreshPayDetail(String pk_entrust, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, int node_count, String pk_carrier,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date,Integer urgent_level,String item_code ,
			String pk_trans_line,UFBoolean if_return) {
		if(StringUtils.isBlank(pk_entrust)) {
			throw new BusiException("主键不能为空！");
		}
		int deli_node_count = InvoiceUtils.getDeliNodeCount(pk_entrust);
		// 1、根据pk_entrust查询当前已经存在的费用明细
		EntrustVO entrustVO = this.getByPrimaryKey(EntrustVO.class, pk_entrust);
		PayDetailBVO[] detailBVOs = getPayDetailBVOsByEntrustBillno(entrustVO.getVbillno());
		// 执行公式
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(detailBVOs.length);
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
		List<Map<String, Object>> oldPayDetailList = this.execFormula4Templet(mapList, uiBillTempletVO,
				paramVO.isBody(), paramVO.getTabCode().split(","), null);
		// 2、调用配载的匹配合同的费用明细方法
		EntPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class,
				"pk_entrust =?",pk_entrust);
		List<PackInfo> packInfos = new ArrayList<PackInfo>();
		if(packBVOs != null && packBVOs.length > 0){
			Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
			for(EntPackBVO packVO : packBVOs){
				String key = packVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<EntPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<EntPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(packVO);
			}
			if (groupMap.size() > 0) {
				for(String key : groupMap.keySet()){
					PackInfo packInfo = new PackInfo();
					List<EntPackBVO> voList = groupMap.get(key);
					Integer num = 0;
					UFDouble weight = UFDouble.ZERO_DBL;
					UFDouble volume = UFDouble.ZERO_DBL;
					for(EntPackBVO packBVO : voList){
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
		}
		List<Map<String, Object>> newPayDetailList = pZService.loadPayDetail(pack_num_count, num_count,
				fee_weight_count, weight_count, volume_count, node_count, deli_node_count,packInfos, pk_carrier, pk_trans_type,
				start_addr, end_addr, start_city, end_city, pk_car_type, pk_corp, req_arri_date,urgent_level,item_code,pk_trans_line,if_return);
		// 3、将旧的费用明细中系统创建标示为Y的记录，与新的费用明细进行匹配(根据费用类型进行匹配，费用类型是唯一的)，
		// 如果不存在于新的费用明细中,说明是之前匹配的，删除该记录
		for(Map<String, Object> oldPayDetail : oldPayDetailList) {
			if(oldPayDetail.get(PayDetailBVO.SYSTEM_CREATE) != null
					&& oldPayDetail.get(PayDetailBVO.SYSTEM_CREATE).toString().equalsIgnoreCase(Constants.Y)) {
				// 属于系统创建的记录，即从合同匹配而来的记录
				boolean exist = false;
				String pk_expense_type = oldPayDetail.get(PayDetailBVO.PK_EXPENSE_TYPE).toString();
				if(newPayDetailList != null) {
					for(Map<String, Object> newPayDetail : newPayDetailList) {
						if(pk_expense_type.equals(newPayDetail.get(PayDetailBVO.PK_EXPENSE_TYPE).toString())) {
							exist = true;
						}
					}
				}
				if(!exist) {
					// 如果不存在，那么移除，会使用新的匹配记录代替
					oldPayDetailList.remove(oldPayDetail);
				}
			}
		}
		// 4、将新的费用明细与旧的费用明细中为系统创建的进行匹配(根据费用类型进行匹配，费用类型是唯一的)，
		// 如果不存在于旧的费用明细中，那么添加该记录
		if(newPayDetailList != null) {
			for(Map<String, Object> newPayDetail : newPayDetailList) {
				String pk_expense_type = newPayDetail.get(PayDetailBVO.PK_EXPENSE_TYPE).toString();
				boolean exist = false;
				for(Map<String, Object> oldPayDetail : oldPayDetailList) {
					if(pk_expense_type.equals(oldPayDetail.get(PayDetailBVO.PK_EXPENSE_TYPE).toString())) {
						// 这里不需要再根据是否系统创建来判断了，因为费用类型是唯一的
						exist = true;
					}
				}
				if(!exist) {
					oldPayDetailList.add(newPayDetail);
				}
			}
		}
		return oldPayDetailList;
	}

	/**
	 * 将应付明细子表中新增的记录单独出来处理，主要是新增的记录需要应付明细主表的pk
	 */
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ExAggEntrustVO aggVO = (ExAggEntrustVO) billVO;
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO(); // 委托单VO

		// 根据委托单号查询应付明细VO，主要是要得到应付明细pk，设置到新增加的应付明细子表中
		PayDetailVO pdVO = getPayDetailVOsByEntrustBillno(parentVO.getVbillno());
		if(pdVO == null){
			throw new BusiException("委托单[?],没有应付明细！");
		}
		//当修改委托单承运商时，应付明细承运商也需要跟着改变。
		pdVO.setPk_carrier(parentVO.getPk_carrier());
		pdVO.setNum_count(parentVO.getNum_count());
		pdVO.setWeight_count(parentVO.getWeight_count());
		pdVO.setVolume_count(parentVO.getVolume_count());
		pdVO.setFee_weight_count(parentVO.getFee_weight_count());
		pdVO.setStatus(VOStatus.UPDATED);
		List<SuperVO> updateList = new ArrayList<SuperVO>();
		// 保存费用明细
		PayDetailBVO[] pdBVOs = (PayDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
		List<PayDetailBVO> oldDetailBVOs = new ArrayList<PayDetailBVO>();
		for(PayDetailBVO cvo : pdBVOs) {
			oldDetailBVOs.add(cvo);
		}
		// 匹配合同重新计算费用明细
		EntLineBVO[] lineVOs = (EntLineBVO[]) aggVO.getTableVO(TabcodeConst.TS_ENT_LINE_B);// 节点数
		List<EntLineBVO> lineList = PZUtils.processLineInfo(lineVOs, true);
		// 重新设置数据
		lineVOs = lineList.toArray(new EntLineBVO[lineList.size()]);
		aggVO.setTableVO(TabcodeConst.TS_ENT_LINE_B, lineVOs);

		// 更改节点顺序以后，需要重新将提货方和收货方更新到委托单上面
		EntLineBVO firstLineVO = PZUtils.getFirstLineVO(lineVOs);
		EntLineBVO lastLineVO = PZUtils.getLastLineVO(lineVOs);
		PZUtils.syncEntrustDeliAndArri(parentVO, firstLineVO, lastLineVO);

		EntTransbilityBVO[] tbBVOs = (EntTransbilityBVO[]) aggVO.getTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B);// 运力信息
		if(tbBVOs != null && tbBVOs.length > 0){
			for(EntTransbilityBVO tbBVO : tbBVOs){
				if(tbBVO.getStatus() == VOStatus.NEW){
					tbBVO.setLot(parentVO.getLot());
				}
			}
		}
		oldDetailBVOs = EntrustUtils.getPayDetailBVOs(contractService, parentVO, tbBVOs, lineVOs, oldDetailBVOs);
		for(PayDetailBVO cvo : oldDetailBVOs) {
			if(cvo.getStatus() == VOStatus.NEW) {
				cvo.setPk_pay_detail(pdVO.getPk_pay_detail()); // 设置主表的主键
				NWDao.setUuidPrimaryKey(cvo);
			}
		}
		updateList.addAll(oldDetailBVOs);
		// 更新总金额
		afterReloadDetailGrid(billVO, oldDetailBVOs, pdVO);
		updateList.add(pdVO);

		// 重新计算分摊费用
		List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(parentVO, null, null,
				oldDetailBVOs.toArray(new PayDetailBVO[oldDetailBVOs.size()]));
		updateList.addAll(deviBVOs);
		aggVO.removeTableVO(TabcodeConst.TS_PAY_DETAIL_B); // 这个VO其实是不属于这个主表的，放在这边主要是维护方便
		dao.saveOrUpdate(updateList);
	}

	/**
	 * 1、重新计算总金额 2、更新运力信息的单价和金额，3、重新计算税额
	 */
	private void afterReloadDetailGrid(AggregatedValueObject billVO, List<PayDetailBVO> pdBVOs, PayDetailVO pdVO) {
		EntrustVO parentVO = (EntrustVO) billVO.getParentVO();
		if(pdBVOs != null && pdBVOs.size() > 0) {
			UFDouble cost_amount = new UFDouble(0);
			for(PayDetailBVO pdBVO : pdBVOs) {
				if(pdBVO.getStatus() != VOStatus.DELETED) {
					cost_amount = cost_amount.add(pdBVO.getAmount());
				}
			}
			parentVO.setCost_amount(cost_amount);
			pdVO.setCost_amount(cost_amount);
		} else {
			parentVO.setCost_amount(UFDouble.ZERO_DBL);
			pdVO.setCost_amount(UFDouble.ZERO_DBL);
		}
		// 取第一行合同明细的税种，税率
		if(pdBVOs != null && pdBVOs.size() > 0) {
			pdVO.setTax_cat(pdBVOs.get(0).getTax_cat());
			pdVO.setTax_rate(pdBVOs.get(0).getTax_rate());
			pdVO.setTaxmny(CMUtils.getTaxmny(pdVO.getCost_amount(), pdVO.getTax_cat(), pdVO.getTax_rate()));
		}

		pdVO.setStatus(VOStatus.UPDATED);
	}

	/**
	 * 数据库中存储的客户名称PK使用,分隔，这里需要对数据特殊处理
	 */
	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, String orderBy) {
		super.processAfterExecFormula(list, paramVO, orderBy);
		Set<String> pk_customers = new HashSet<String>();
		Set<String> pk_entrusts = new HashSet<String>();
		CustomerVO[] customerVOs = null;
		EntTransbilityBVO[] entTransbilityBVOs = null;
		for (Map<String, Object> map : list) {
			Object pk_customer = map.get("pk_customer");
			if (pk_customer != null) {
				String[] pks = pk_customer.toString().split("\\" + Constants.SPLIT_CHAR);
				pk_customers.addAll(Arrays.asList(pks));
			}
			Object pk_entrust = map.get("pk_entrust");
			if (pk_entrust != null) {
				pk_entrusts.add(pk_entrust.toString());
			}
		}
		if (pk_customers != null && pk_customers.size() > 0) {
			customerVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CustomerVO.class, " pk_customer in "
					+ NWUtils.buildConditionString(pk_customers.toArray(new String[pk_customers.size()])));
		}
		if (pk_entrusts != null && pk_entrusts.size() > 0) {
			entTransbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
					" pk_entrust in "
							+ NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()])));
		}
		for (Map<String, Object> map : list) {
			Object pk_customer = map.get("pk_customer");
			StringBuffer cust_name = new StringBuffer();
			StringBuffer carno_name = new StringBuffer();
			if (pk_customer != null) {
				String[] pks = pk_customer.toString().split("\\" + Constants.SPLIT_CHAR);
				if (pks != null && pks.length > 0 && customerVOs != null && customerVOs.length > 0) {
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
			Object pk_entrust = map.get("pk_entrust");
			if (pk_entrust != null) {
				if (entTransbilityBVOs != null && entTransbilityBVOs.length > 0) {
					for (EntTransbilityBVO entTransbilityBVO : entTransbilityBVOs) {
						if (pk_entrust.equals(entTransbilityBVO.getPk_entrust())
								&& StringUtils.isNotBlank(entTransbilityBVO.getCarno())) {
							carno_name.append(entTransbilityBVO.getCarno()).append(Constants.SPLIT_CHAR);
						}
					}
					if (StringUtils.isNotBlank(carno_name.toString())) {
						String carno_name1 = carno_name.substring(0, carno_name.length() - 1);
						map.put("carno_name", carno_name1);
					}
				}
			}
		}
	}

	public void buildInstorage(ParamVO paramVO) {
		if(paramVO == null || StringUtils.isBlank(paramVO.getBillId())) {
			throw new BusiException("请选择委托单！");
		}
		AggregatedValueObject billVO = this.queryBillVO(paramVO);
		ExAggEntrustVO aggVO = (ExAggEntrustVO) billVO;
		EntrustVO entVO = (EntrustVO) aggVO.getParentVO();
		InstorageVO oriInsVO = NWDao.getInstance().queryByCondition(InstorageVO.class, "orderno=?", entVO.getVbillno());
		if(oriInsVO != null) {
			throw new BusiException("当前委托单已经生成过入库单，不能重复生成！");
		}
		InstorageVO insVO = new InstorageVO();
		insVO.setDbilldate(new UFDate());
		insVO.setOrderno(entVO.getVbillno());
		insVO.setCust_orderno(entVO.getCust_orderno());
		insVO.setOrder_type(DataDictConst.ORDER_TYPE.WTDDR.intValue());// 默认委托单导入
		insVO.setPk_customer(entVO.getPk_customer());
		insVO.setEst_arri_date(entVO.getReq_arri_date());// 要求到货日期
		insVO.setPk_delivery(entVO.getPk_delivery());
		insVO.setPk_arrival(entVO.getPk_arrival());
		insVO.setPk_carrier(entVO.getPk_carrier());
		insVO.setPk_driver(entVO.getPk_driver());
		insVO.setCarno(entVO.getCarno());
		insVO.setOrder_count(new UFDouble(entVO.getNum_count() == null ? 0 : entVO.getNum_count()));
		insVO.setWeight_count(entVO.getWeight_count());
		insVO.setVolume_count(entVO.getVolume_count());
		insVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.INSTO));
		insVO.setVbillstatus(BillStatus.NEW);
		insVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		insVO.setCreate_time(new UFDateTime(new Date()));
		insVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		insVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(insVO);
		NWDao.getInstance().saveOrUpdate(insVO);

		CircularlyAccessibleValueObject[] childVOs = aggVO.getTableVO(TabcodeConst.TS_ENT_PACK_B);
		if(childVOs != null && childVOs.length > 0) {
			int index = 0;
			for(CircularlyAccessibleValueObject childVO : childVOs) {
				EntPackBVO epBVO = (EntPackBVO) childVO;
				InstorageBVO insBVO = new InstorageBVO();
				insBVO.setPk_instorage(insVO.getPk_instorage());
				insBVO.setVbillno(insVO.getVbillno() + "_" + (index + 1));// 固定这个格式
				insBVO.setVbillstatus(BillStatus.NEW);
				String goods_code = epBVO.getGoods_code();
				if(StringUtils.isBlank(goods_code)) {
					throw new BusiException("货品编码[?]不存在，不能生成入库单！",goods_code);
				}
				GoodsVO goodsVO = NWDao.getInstance().queryByCondition(GoodsVO.class, "goods_code=?", goods_code);
				if(goodsVO == null) {
					throw new BusiException("货品编码[?]不存在，不能生成入库单！",goods_code);
				}
				insBVO.setOrderno(insVO.getOrderno());
				insBVO.setCust_orderno(insVO.getCust_orderno());
				insBVO.setPk_customer(insVO.getPk_customer());
				insBVO.setPk_goods(goodsVO.getPk_goods());
				insBVO.setPack(epBVO.getPack());
				insBVO.setOrder_count(new UFDouble(epBVO.getNum() == null ? 0 : epBVO.getNum().intValue()));// 件数
				insBVO.setWeight(epBVO.getWeight());
				insBVO.setVolume(epBVO.getVolume());
				insBVO.setUnit_weight(epBVO.getUnit_weight());
				insBVO.setUnit_volume(epBVO.getUnit_volume());
				insBVO.setLength(epBVO.getLength());
				insBVO.setWidth(epBVO.getWidth());
				insBVO.setHeight(epBVO.getHeight());
				insBVO.setGoods_prop(DataDictConst.GOODS_PROP.OK.intValue());
				insBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(insBVO);
				NWDao.getInstance().saveOrUpdate(insBVO);
				index++;
			}
		}
		entVO.setInstorage_vbillno(insVO.getVbillno());
		entVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(entVO);
	}

	public void buildOutstorage(ParamVO paramVO) {
		if(paramVO == null || StringUtils.isBlank(paramVO.getBillId())) {
			throw new BusiException("请选择委托单！");
		}
		AggregatedValueObject billVO = this.queryBillVO(paramVO);
		ExAggEntrustVO aggVO = (ExAggEntrustVO) billVO;
		EntrustVO entVO = (EntrustVO) aggVO.getParentVO();
		OutstorageVO oriOusVO = NWDao.getInstance().queryByCondition(OutstorageVO.class, "orderno=?",
				entVO.getVbillno());
		if(oriOusVO != null) {
			throw new BusiException("当前委托单已经生成过出库单，不能重复生成！");
		}
		OutstorageVO osVO = new OutstorageVO();
		osVO.setDbilldate(new UFDate());
		osVO.setOrderno(entVO.getVbillno());
		osVO.setCust_orderno(entVO.getCust_orderno());
		osVO.setOrder_type(DataDictConst.ORDER_TYPE.WTDDR.intValue());// 默认委托单导入
		osVO.setPk_customer(entVO.getPk_customer());
		osVO.setReq_deli_date(entVO.getReq_deli_date());// 要求提货日期
		osVO.setReq_arri_date(entVO.getReq_arri_date());// 要求到货日期
		osVO.setPk_delivery(entVO.getPk_delivery());
		osVO.setPk_arrival(entVO.getPk_arrival());
		osVO.setPk_carrier(entVO.getPk_carrier());
		osVO.setPk_driver(entVO.getPk_driver());
		osVO.setCarno(entVO.getCarno());
		osVO.setOrder_count(new UFDouble(entVO.getNum_count() == null ? 0 : entVO.getNum_count()));
		osVO.setWeight_count(entVO.getWeight_count());
		osVO.setVolume_count(entVO.getVolume_count());
		osVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.OUTSTO));
		osVO.setVbillstatus(BillStatus.NEW);
		osVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		osVO.setCreate_time(new UFDateTime(new Date()));
		osVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		osVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(osVO);
		NWDao.getInstance().saveOrUpdate(osVO);

		CircularlyAccessibleValueObject[] childVOs = aggVO.getTableVO(TabcodeConst.TS_ENT_PACK_B);
		if(childVOs != null && childVOs.length > 0) {
			int index = 0;
			for(CircularlyAccessibleValueObject childVO : childVOs) {
				EntPackBVO epBVO = (EntPackBVO) childVO;
				OutstorageBVO osBVO = new OutstorageBVO();
				osBVO.setPk_outstorage(osVO.getPk_outstorage());
				osBVO.setVbillno(osVO.getVbillno() + "_" + (index + 1));// 固定这个格式
				osBVO.setVbillstatus(BillStatus.NEW);
				String goods_code = epBVO.getGoods_code();
				if(StringUtils.isBlank(goods_code)) {
					throw new BusiException("货品编码[?]不存在，不能生成入库单！",goods_code);
				}
				GoodsVO goodsVO = NWDao.getInstance().queryByCondition(GoodsVO.class, "goods_code=?", goods_code);
				if(goodsVO == null) {
					throw new BusiException("货品编码[?]不存在，不能生成入库单！",goods_code);
				}
				osBVO.setOrderno(osVO.getOrderno());
				osBVO.setCust_orderno(osVO.getCust_orderno());
				osBVO.setPk_customer(osVO.getPk_customer());
				osBVO.setPk_goods(goodsVO.getPk_goods());
				osBVO.setPack(epBVO.getPack());
				osBVO.setOrder_count(new UFDouble(epBVO.getNum() == null ? 0 : epBVO.getNum().intValue()));// 件数
				osBVO.setWeight(epBVO.getWeight());
				osBVO.setVolume(epBVO.getVolume());
				osBVO.setUnit_weight(epBVO.getUnit_weight());
				osBVO.setUnit_volume(epBVO.getUnit_volume());
				osBVO.setLength(epBVO.getLength());
				osBVO.setWidth(epBVO.getWidth());
				osBVO.setHeight(epBVO.getHeight());
				osBVO.setGoods_prop(DataDictConst.GOODS_PROP.OK.intValue());
				osBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(osBVO);
				NWDao.getInstance().saveOrUpdate(osBVO);
				index++;
			}
		}
		entVO.setOutstorage_vbillno(osVO.getVbillno());
		entVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(entVO);
	}

	public List<EntrustVO> getTodayTop5() {
		String corpCond = CorpHelper.getCurrentCorpWithChildren("ts_entrust");
		String sql = "";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select * from ts_entrust where rownum < 6 and isnull(dr,0)=0 and vbillstatus=? and " + corpCond
					+ " order by create_time desc";
		}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 * from ts_entrust WITH(NOLOCK) where isnull(dr,0)=0 and vbillstatus=? and " + corpCond
					+ " order by create_time desc";
		}
		return NWDao.getInstance().queryForList(sql, EntrustVO.class, BillStatus.NEW);
	}

	protected String getBodyPrintTableCode() {
		return TabcodeConst.TS_ENT_PACK_B;
	}

	// @Override
	// public List<Map<String, Object>> getBillPrintDataSource(ParamVO paramVO,
	// UiBillTempletVO uiBillTempletVO,
	// UiPrintTempletVO uiPrintTempletVO) {
	// List<Map<String, Object>> retList = super.getBillPrintDataSource(paramVO,
	// uiBillTempletVO, uiPrintTempletVO);
	// if(retList == null || retList.size() == 0) {
	// return retList;
	// }
	// for(Map<String, Object> rowMap : retList) {
	// // 每行联查发货单号
	// Object create_time = rowMap.get("h_create_time");
	// if(create_time != null) {
	// // 创建日期只取年月日
	// rowMap.put("h_create_time",
	// DateUtils.formatDate(create_time.toString(),
	// DateUtils.DATEFORMAT_HORIZONTAL));
	// }
	// }
	// return retList;
	// }
	
	
	
	/**
	 * 处理导入运力信息
	 * 
	 * @param List<EntTransbilityBVO> etbBVOs
	 * 
	 *  @author yaojie
	 * @date 2015-11-12 下午14:19
	 * 
	 */
	
	public void saveEntTransbility(List<EntTransbilityBVO> etbBVOs){
		List<EntTransbilityBVO> oldbVOS = new ArrayList<EntTransbilityBVO>();
		List<SuperVO> updateList = new ArrayList<SuperVO>();
		int rowNum = 1;
		for(EntTransbilityBVO etbVO : etbBVOs){
			if(etbVO.getCarno() == null && etbVO.getPk_car_type() == null){
				throw new BusiException("第[?]行：车牌号和车辆类型不能同时为空！",rowNum+"");
			}
			CarVO carVO = (CarVO)carService.getByCarno(etbVO.getCarno());
			//传进etbVO里的PK_entrust,实际上就EXCEL里的vbillno
			EntrustVO entVO = (EntrustVO)getByCode(etbVO.getPk_entrust());
			if(entVO == null){
				throw new BusiException("委托单[?]不存在，请检查数据！" ,etbVO.getPk_entrust());
			}
			EntTransbilityBVO entTransbilityBVO = new EntTransbilityBVO();
			
			entTransbilityBVO.setPk_entrust(entVO.getPk_entrust());
			entTransbilityBVO.setLot(entVO.getLot());
			entTransbilityBVO.setCarno(etbVO.getCarno());
			entTransbilityBVO.setPk_driver(etbVO.getPk_driver());
			//先将driver_name 和driver_mobile添加到VO里，如果存在pk_driver
			//那么再用pk_driver获取对应的name和mobile 如果传入数据为空，就添加到数据库，否则就使用原有的。
			entTransbilityBVO.setDriver_name(etbVO.getDriver_name());
			entTransbilityBVO.setDriver_mobile(etbVO.getDriver_mobile());
			if(StringUtils.isNotBlank(etbVO.getPk_driver())){
				DriverVO driverVO = NWDao.getInstance().queryByCondition(DriverVO.class, "driver_code=?", etbVO.getPk_driver());
				if(StringUtils.isBlank(etbVO.getDriver_name()) && driverVO != null){
					entTransbilityBVO.setDriver_name(driverVO.getDriver_name());
				}
				if(StringUtils.isBlank(etbVO.getDriver_mobile()) && driverVO != null){
					entTransbilityBVO.setDriver_mobile(driverVO.getMobile());
				}
			}
			entTransbilityBVO.setContainer_no(etbVO.getContainer_no());
			entTransbilityBVO.setSealing_no(etbVO.getSealing_no());
			entTransbilityBVO.setForecast_deli_date(etbVO.getForecast_deli_date());
			entTransbilityBVO.setCarno(etbVO.getCarno());
			//传进etbVO里的PK_car_type,实际上就EXCEL里的name
			//业务需求：当车型和车牌号同时为空时  不成立
			//       当车型存在但与数据库里的数据不符合的时候 不成立
			//       当车型不存在的时候，从车牌号获取车型，获取不到的时候  不成立
			if(StringUtils.isBlank(etbVO.getPk_car_type())){
				// 2015 11 13 yaojiie存在车辆类型为空，车牌号存在，但是车牌号对应的数据错误
				if(carVO == null){
					throw new BusiException("第[?]行：车辆类型为空，提供的车牌号错误！",rowNum+"");
				}else
				if(StringUtils.isBlank(carVO.getPk_car_type())){
					throw new BusiException("第[?]行：车辆类型为空，车牌号获取的车辆类型也为空！",rowNum+"");
				}
					entTransbilityBVO.setPk_car_type(carVO.getPk_car_type());
				}else{
					CarTypeVO carTypeVO = (CarTypeVO)carTypeService.getByName(etbVO.getPk_car_type());
					if(carTypeVO == null ){
						throw new BusiException("第[?]行：输入的车辆类型不正确！",rowNum+"");
					}else{
						//etbVO.getPk_car_type() 获取到的是车辆类型名称。
						entTransbilityBVO.setPk_car_type(carTypeVO.getPk_car_type());
					}
				}
			//这里判断GPS号，当Excel里没有传入GPSID时，去车辆信息里找GPSID，车辆信息也没有，则不填。
			if(StringUtils.isBlank(etbVO.getGps_id())){
				if(carVO == null){
					entTransbilityBVO.setGps_id(etbVO.getGps_id());
				}else{
					entTransbilityBVO.setGps_id(carVO.getGps_id());
				}
			}else{
				entTransbilityBVO.setGps_id(etbVO.getGps_id());
			}
			entTransbilityBVO.setMemo(etbVO.getMemo());
			//需要修改订单状态为删除的VO
			EntTransbilityBVO[] bVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
			"pk_entrust =?",entVO.getPk_entrust() );
			if(bVOs != null && bVOs.length > 0){
				for(EntTransbilityBVO bVO : bVOs){
					bVO.setStatus(VOStatus.DELETED);
					oldbVOS.add(bVO);
				}
			}
			if(oldbVOS!=null && oldbVOS.size()>0)	{
				updateList.addAll(oldbVOS);
			}
			//设置身份证号，和行驶证号码
			entTransbilityBVO.setCertificate_id(etbVO.getCertificate_id());
			entTransbilityBVO.setDriving_license(etbVO.getDriving_license());
			entTransbilityBVO.setStatus(VOStatus.NEW);	
			NWDao.setUuidPrimaryKey(entTransbilityBVO);
			updateList.add(entTransbilityBVO);
			rowNum++;
		}
		NWDao.getInstance().saveOrUpdate(updateList);
	
	}
	
	
	// 获取邮件发送的数据包括excel数据和html数据
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> getSendEntEmailData(String[] ids,String funcode) {
		if (ids == null || ids.length == 0) {
			return null;
		}
		String pks = NWUtils.join(ids, ",");
		// 用于存储查询数据
		final List<List<String>> excelRetList = new ArrayList<List<String>>();
		final List<Map<String, String>> htmlRetList = new ArrayList<Map<String, String>>();
		final List<String> listTitle = new ArrayList<String>();
		// 存储过程名称
		final String TS_ENT_CARR_EMAIL_PROC =  "ts_email_proc";
		final String ID = pks;
		final String FUNCODE = funcode;
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 2;
					String storedProc = DaoHelper.getProcedureCallName(TS_ENT_CARR_EMAIL_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, ID);
					cs.setString(2, FUNCODE);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet htmlRs = cs.getResultSet();
					while (htmlRs.next()) {
						fillHtmlResultfSetForEntEMail(htmlRetList, htmlRs);
					}
					if (cs.getMoreResults()) {
						ResultSet excelRs = cs.getResultSet();
						while (excelRs.next()) {
							fillExcelResultfSetForEntEMail(excelRetList, listTitle, excelRs);
						}
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("excel", excelRetList);
		result.put("html", htmlRetList);
		result.put("title", listTitle.toArray(new String[listTitle.size()]));
		return result;
	}
	private File upEntEmailExcel(String[] titles,List<List<String>> mapList){
		if(titles == null || mapList == null || titles.length == 0 || mapList.size() == 0){
			return null;
		}
		POI excel = new POI();
		HSSFWorkbook wb = excel.buildExcel(titles, mapList);
		
		String tmpdirPath = Constants.TMPDIR;
		File tmpdir = new File(tmpdirPath);
		if(!tmpdir.exists()) {
			tmpdir.mkdir();
		}
		String extension = ".xls";
		int index = 1;
		String file_name = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		String fileUrl = tmpdirPath + File.separator + file_name + extension;
		File file = new File(fileUrl);// 创建一个用于存储数据库文件的临时文件
		if(file.exists()){
			file_name = file_name +"-"+ index + extension;
			fileUrl = tmpdir + File.separator + file_name;
			index ++;
			file = new File(fileUrl);
		}else{
			index = 1;
		}
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		try {
			wb.write(os);
		} catch (IOException e1) {
			logger.info("导出excel失败！");
			e1.printStackTrace();
		}finally {
			if(os!=null){
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
					logger.info("流关闭失败！");
				}
			}
		}
		return new File(fileUrl);
	}

	@SuppressWarnings({ "unchecked" })
	public String sendEntEmail(String[] ids,String funcode) {
		if(ids == null || ids.length == 0 || StringUtils.isBlank(ids[0])){
			return "请选择单据！";
		}
		Map<String,Object> emailData = getSendEntEmailData(ids,funcode);
		if(emailData == null || emailData.size() == 0 || emailData.get("excel") == null){
			return "没有需要发送的数据！";
		} 
		// 格式化数据  对同一一个邮箱进行分组
		Map<String, List<List<String>>> groupMap = new HashMap<String, List<List<String>>>();
		for (List<String> ret : (List<List<String>>)emailData.get("excel")) {
			String key = String.valueOf(ret.get(0));//第一个字段固定是邮箱
			List<List<String>> mapList = groupMap.get(key);
			if (mapList == null) {
				mapList = new ArrayList<List<String>>();
				groupMap.put(key, mapList);
			}
			mapList.add(ret.subList(2, ret.size()));
		}
		for(Map<String,String> htmlMap : (List<Map<String,String>>)emailData.get("html")){
			List<List<String>> mapList = groupMap.get(htmlMap.get("邮箱"));
			File excelFile = upEntEmailExcel((String[])emailData.get("title"), mapList);
			//发送邮件
			if(NWUtils.validateEmail(htmlMap.get("邮箱")==null?"":htmlMap.get("邮箱"))) {
				try {
					MailSenderInfo mailSenderInfo =  new MailSenderInfo();
					mailSenderInfo.setToAddress(new String[] { htmlMap.get("邮箱") });
					EntMailTemplate entMailTemplate = new EntMailTemplate();
					entMailTemplate.initEntMail(htmlMap,htmlMap.get("页面"));
					mailSenderInfo.setSubject(entMailTemplate.getSubject());
					mailSenderInfo.setContent(entMailTemplate.getContent());
					List<File> attachs = new ArrayList<File>();
					attachs.add(excelFile);
					mailSenderInfo.setAttachs(attachs);
					logger.info("开始发送邮件"+ htmlMap.get("邮箱"));
					SimpleMailSender.sendHtmlMail(mailSenderInfo);
				} catch(Throwable e) {
					continue;
				}
			} 
		}
		return null;
	}
	
	public static void fillExcelResultfSetForEntEMail(List<List<String>> retList,List<String> listTitle, ResultSet rs) throws SQLException{
		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		String[] names = new String[count];
		List<String> temp = new ArrayList<String>();
		for(int i=0;i<count;i++){
			names[i]=metaData.getColumnName(i+1);
			if(listTitle != null && listTitle.size() == 0){
				if(!names[i].equals("邮箱") && !names[i].equals("承运商")){
					temp.add(names[i]);
				}
			}
		}
		if(listTitle != null && listTitle.size() == 0){
			listTitle.addAll(temp);
		}
		List<String> resut = new ArrayList<String>();
		for(String name : names){
			resut.add(rs.getString(name));
		}
		retList.add(resut);
	}
	
	public static void fillHtmlResultfSetForEntEMail(List<Map<String,String>> retList,ResultSet rs) throws SQLException{
		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		String[] names = new String[count];
		for(int i=0;i<count;i++){
			names[i]=metaData.getColumnName(i+1);
		}
		Map<String,String> map = new HashMap<String, String>();
		for(String name : names){
			map.put(name, rs.getString(name));
		}
		retList.add(map);
	}
	
	public File writeTmpFile(File file, InputStream is) throws Exception {
		OutputStream inputFileStream = null;
		try {
			inputFileStream = new FileOutputStream(file);
			IOUtils.copy(is, inputFileStream);
		} finally {
			IOUtils.closeQuietly(inputFileStream);
		}
		return file;
	}
	
	public Map<String,Object> authentication(String lot, String card_msg){
		if(StringUtils.isBlank(lot)){
			throw new BusiException("请选择批次！");
		}
		if(StringUtils.isBlank(card_msg)){
			throw new BusiException("没有获取到身份证信息！");
		}
		Map<String,Object> ret = new HashMap<String,Object>();
		@SuppressWarnings("unchecked")
		Map<String,String> card_Msg_map = JacksonUtils.readValue(card_msg, Map.class);
		String id = card_Msg_map.get("certNumber");
		ret.put("id", id);
		String bornDay = card_Msg_map.get("bornDay");
		ret.put("bornDay", bornDay);
		String certAddress = card_Msg_map.get("certAddress");
		ret.put("certAddress", certAddress);
		String certOrg = card_Msg_map.get("certOrg");
		ret.put("certOrg", certOrg);
		String effDate = card_Msg_map.get("effDate");
		ret.put("effDate", effDate);
		String expDate = card_Msg_map.get("expDate");
		ret.put("expDate", expDate);
		String gender = String.valueOf(card_Msg_map.get("gender"));
		if(gender.equals("1")){
			gender = "男";
		}else{
			gender = "女";
		}
		ret.put("gender", gender);
		String identityPic = card_Msg_map.get("identityPic");
		identityPic = identityPic.replaceAll(" ", "+");
		String nation = card_Msg_map.get("nation");
		ret.put("nation", nation);
		String partyName = card_Msg_map.get("partyName");
		ret.put("partyName", partyName);
		String sql = "SELECT distinct certificate_id,driver_name FROM ts_ent_transbility_b WITH(NOLOCK) WHERE isnull(certificate_id,'') <>''"
				+ " and isnull(driver_name,'') <>'' and isnull(dr,0)=0 and  lot=? ";
		List<Map<String, Object>> certificates = NWDao.getInstance().queryForList(sql,lot);
		
		if(certificates != null && certificates.size() > 0){
			List<String> certificate_ids = new ArrayList<String>();
			for(Map<String, Object> certificate : certificates){
				if(certificate != null && StringUtils.isNotBlank(String.valueOf(certificate.get("certificate_id"))) 
						&& !(String.valueOf(certificate.get("certificate_id")).equals("null"))){
					certificate_ids.add(String.valueOf(certificate.get("certificate_id")));
				}
			}
			//如果缺少身份证号，那么将整个身份证信息清空。
			if(certificate_ids.size() == 0){
				certificates = new ArrayList<Map<String, Object>>();
			}else{
				for(String certificate_id : certificate_ids){
					if(id.trim().equals(certificate_id)){
						//验证通过
						ret.put("checkUrl", "tg.png");
					}
				}
			}
		}
		ret.put("system", certificates);
		if(ret.get("checkUrl") == null){
			ret.put("checkUrl", "btg.png");
		}else if(ret.get("checkUrl").equals("tg.png")){
			//将运力信息的验证信息，记录到这个批次下的运力信息里
			EntTransbilityBVO[] transBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
					"lot=?", lot);
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			for(EntTransbilityBVO transBVO : transBVOs){
				if(transBVO.getCertificate_id() != null && transBVO.getCertificate_id().equals(id)){
					transBVO.setStatus(VOStatus.UPDATED);
					transBVO.setIf_checked(UFBoolean.TRUE);
					toBeUpdate.add(transBVO);
				}
			}
			NWDao.getInstance().saveOrUpdate(toBeUpdate);
		}
		
		//将照片信息传递到服务器
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String certificateDir = webappPath + "certificate";
		String filePath = certificateDir + File.separator + id + ".bmp";
		try {
			WebServicesUtils.convertBase64DataToImage(identityPic, filePath);
		} catch (IOException e) {
			logger.info("身份证图片上传服务器出错，ID：" + id);
		} catch (DecoderException e) {
			logger.info("身份证图片上传服务器编码解析出错，ID：" + id);
		}
		ret.put("photoUrl", id+".bmp");
		return ret;
	}

		public List<Map<String,Object>> syncExpress(String[] ids) {
			if(ids == null || ids.length == 0){
				throw new BusiException("请先选择记录！");
			}
			String entCond = NWUtils.buildConditionString(ids);
			EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "pk_entrust in" + entCond);
			if(entrustVOs == null || entrustVOs.length == 0){
				throw new BusiException("委托单不存在！");
			}
			List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
			for(EntrustVO entrustVO : entrustVOs){
				Map<String,Object> retUnit = new HashMap<String, Object>();
				returnList.add(retUnit);//先将Map放到List里面，避免后面处理起来
				if(entrustVO.getVbillstatus() == BillStatus.ENT_DELIVERY){
					retUnit.put("id", entrustVO.getVbillno());
					String com = entrustVO.getFlightno();
					String nu = entrustVO.getMainno();
					//获取快递信息
					List<Map<String,String>> retList = ExpressUtils.query(com, nu);
					if(retList == null || retList.size() == 0){
						//没有查询到快递信息
						retUnit.put("error", entrustVO.getVbillno() +"没有查询到快递信息!");
						continue;
					}
					retUnit.put("express", retList);
					String sql = "";
					if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
						sql = " SELECT tracking_time FROM ts_ent_tracking "
								+ "  WHERE rownum = 1 and isnull(dr,0)=0 AND entrust_vbillno=? ORDER BY tracking_time DESC ";
					}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
						sql = " SELECT TOP 1 tracking_time FROM ts_ent_tracking WITH(nolock)"
								+ "  WHERE isnull(dr,0)=0 AND entrust_vbillno=? ORDER BY tracking_time DESC ";
					}
					UFDateTime lastTrackingTime = NWDao.getInstance().queryForObject(sql, UFDateTime.class, entrustVO.getVbillno());
					
					try {
						for(Map<String,String> expressUnit : retList){
							UFDateTime expressTime = new UFDateTime(expressUnit.get("time"));
							if(lastTrackingTime == null || lastTrackingTime.before(expressTime)){
								//没有跟踪记录，那么每条快递信息，都要记录到跟踪表里面,有跟踪记录，只要插入跟踪时间之后的信息即可。
								//根据快递信息内容判断，提货，到货，等状态
								String context = expressUnit.get("context");
								if(ExpressUtils.ExpressCorp.JD.equals(com)){
									//jd快递要替换掉签收字段
									context = context.replaceAll("签收", "");
								}
								Integer status = analyzeExpress(context);
								// 快递业务不分段。
								if (status == TrackingConst.ARRI) {
									EntLineBVO prevEntLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class,
											"pk_entrust=? and addr_flag='S'", entrustVO.getPk_entrust());
									if (prevEntLineBVO != null
											&& !prevEntLineBVO.getArrival_flag().equals(UFBoolean.TRUE)) {
										retUnit.put("error", entrustVO.getVbillno() + " 节点：" + prevEntLineBVO.getAddr_name()
												+ "还未到货，不允许进行到货确认");
										continue;
									}

									EntLineBVO entLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class,
											"pk_entrust=? and addr_flag='E'", entrustVO.getPk_entrust());
									entLineBVO.setAct_arri_date(expressUnit.get("time"));
									entLineBVO.setMemo(context);
									trackingService.confirmArrival(entLineBVO,0);
									// 找到签收单，签了它。
									PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pod_entrust_vbillno=?", entrustVO.getVbillno());
									if (podVO != null) {
										podVO.setPod_date(expressTime);
										podVO.setPod_memo(context);
										ParamVO paramVO = new ParamVO();
										paramVO.setTemplateID("0001A4100000000017ZN");
										paramVO.setHeaderTabCode("ts_pod");
										paramVO.setFunCode("t601");
										podService.doPod(new String[] { podVO.getPk_invoice() }, podVO, paramVO);
									}
								} else {// 说明这是在途状态
									EntTrackingVO entTrackingVO = new EntTrackingVO();
									entTrackingVO.setTracking_memo(context);
									entTrackingVO.setTracking_time(expressTime);
									entTrackingVO.setTracking_status(status);
									entTrackingVO.setSync_flag(UFBoolean.TRUE);;
									trackingService.batchSaveEntTracking(entTrackingVO, new String[] { entrustVO.getVbillno() });
								}
							}
						}
					} catch (Exception e) {
						retUnit.put("error",e.getMessage());
						continue;
					}
				} 
			}
			return returnList;
		}
	
	public Integer analyzeExpress(String context){
		
		Integer status = TrackingConst.ONROAD;//默认都是确认状态
		
		final String[] backs = new String[]{"退回","退件","拒签","客户拒收","失败","签单返还","返货单","已送返"};
		
		final String[] hards = new String[]{"未妥投","疑难","不成功","问题","未支付相关费用","地址有误","延期","地址不详","快件作废"};
		
		final String[] sends = new String[]{"派件","待自取","派送","投递","通知客户领取","配送员","再投","自提","delivery","试投","通知成功","收件准备","便利店","准备签收"};
		
		final String[] deliverys = new String[]{"揽件","已收件","已揽收","已收寄","已收取快件","收寄","揽货","发件人处提取","上门取货","取件成功","配送司机收箱"};
		
		final String[] arrivals = new String[]{"签收","已送达","再次光临","已妥投","用户已领取","已经妥投","妥投","Delivered","已拆包","已递送","DELIVERED","已妥投","派送完毕"};
		
		final String[] onRoads = new String[]{"上级站点","发往","正运往","正送往","转运","上一站","到达","分公司","扫描","离开","分拨中心","到.*?市","到.*?自治州","海关",
				"已发出","公司.*?收入","封发","已打包","寄达局","其他快递","试他局","存局","装车","清关","已入库","运输中","准备送往","快件转寄","安检","上车","分拣中心","运输","分拣完成",
				"开始承运","快递分部","已出口开拆","出库","站.*?收货完成","仓.*?收货完成","中转.*?收货完成","装箱","抵达","到.*?分部","转寄中","已进口开拆","转.*?处理","留仓","接货完成",
				"托运","到.*?县","转件"};
		
		for(String arrival : arrivals){
			if(context.indexOf(arrival) != -1){
				status = TrackingConst.ARRI;
				return status;
			}
		}
		return status;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String doProcByConfirmAndUnConform(String pk_entrust,String type){
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = "ts_wtd_check_proc";
		final String PK = pk_entrust;
		final String TYPE = type;
		final String USER = WebUtils.getLoginInfo().getPk_user();
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

	public Map<String, Object> receipt(String vbillno,PodVO podVO_vr,ParamVO paramVO) {
		if(StringUtils.isBlank(vbillno)){
			throw new BusiException("委托单号不能为空！");
		}
		EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", vbillno);
		if(entrustVO == null){
			throw new BusiException("委托单不存在！");
		}
		if(entrustVO.getVbillstatus() != BillStatus.ENT_ARRIVAL){
			throw new BusiException("只有到货的委托单才可以回单！");
		}
		entrustVO.setStatus(VOStatus.UPDATED);
		entrustVO.setIf_receipt(UFBoolean.TRUE);
		entrustVO.setReceipt_man(podVO_vr.getReceipt_man());
		entrustVO.setReceipt_time(podVO_vr.getAct_receipt_date());
		entrustVO.setReceipt_book_man(WebUtils.getLoginInfo().getPk_user());
		entrustVO.setReceipt_book_time(new UFDateTime(new Date()));
		entrustVO.setReceipt_memo(podVO_vr.getReceipt_memo());
		entrustVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(entrustVO);
		AggregatedValueObject billVO = new ExAggEntrustVO();
		billVO.setParentVO(entrustVO);
		
		if(podVO_vr.getReceipt_exp_type() != null){
			InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "vbillno=?", entrustVO.getInvoice_vbillno());
			// 生成异常事故
			ExpAccidentVO eaVO = new ExpAccidentVO();
			eaVO.setDbilldate(new UFDate());
			eaVO.setStatus(VOStatus.NEW);
			eaVO.setVbillstatus(BillStatus.NEW);
			eaVO.setInvoice_vbillno(invVO.getVbillno());
			eaVO.setPk_customer(invVO.getPk_customer());
			eaVO.setEntrust_vbillno(entrustVO.getVbillno());
			eaVO.setPk_carrier(entrustVO.getPk_carrier());
			eaVO.setOrigin(ExpAccidentOrgin.RECEIPT.toString());
			eaVO.setExp_type(podVO_vr.getReceipt_exp_type());
			eaVO.setMemo(podVO_vr.getPod_memo());
			if(StringUtils.isBlank(eaVO.getMemo())){
				eaVO.setMemo(podVO_vr.getReceipt_memo());
			}
			eaVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			eaVO.setFb_user(WebUtils.getLoginInfo().getPk_user());
			eaVO.setFb_date(new UFDateTime(new Date()));
			eaVO.setOccur_date(new UFDateTime(new Date()));
			eaVO.setOccur_addr(podVO_vr.getApp_detail_addr());
			eaVO.setCreate_time(new UFDateTime(new Date()));
			eaVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			ParamVO paramVO1 = new ParamVO();
			paramVO1.setFunCode(FunConst.EXP_ACCIDENT_CODE);
			expAccidentService.setCodeField(eaVO, paramVO1);
			NWDao.setUuidPrimaryKey(eaVO);
			NWDao.getInstance().saveOrUpdate(eaVO);
		}
		
		//判断是否要回单。
		String sql = "SELECT ts_pod.* FROM ts_pod WITH(NOLOCK)  "
				+ " LEFT JOIN ts_ent_inv_b WITH(NOLOCK)  ON ts_pod.pk_invoice = ts_ent_inv_b.pk_invoice  "
				+ " WHERE isnull(ts_pod.dr,0)=0 AND isnull(ts_ent_inv_b.dr,0)=0  "
				+ " AND ts_ent_inv_b.pk_entrust=?";
		PodVO podVO = NWDao.getInstance().queryForObject(sql, PodVO.class, entrustVO.getPk_entrust());
		if(podVO != null){
			PodVO newPodVO = new PodVO();
			newPodVO.setPk_invoice(podVO.getPk_invoice());
			newPodVO.setReceipt_man(podVO_vr.getReceipt_man());
			newPodVO.setReceipt_memo(podVO_vr.getReceipt_memo());
			newPodVO.setAct_receipt_date(podVO_vr.getAct_receipt_date());
			String sql0 = "SELECT COUNT(ts_entrust.vbillno) FROM ts_invoice "
					+ " LEFT JOIN ts_entrust ON ts_invoice.vbillno=ts_entrust.invoice_vbillno "
					+ " AND ts_invoice.pk_arrival=ts_entrust.pk_arrival AND ts_entrust.pk_entrust<> ? "
					+ " WHERE ts_entrust.vbillstatus<>24 AND ts_invoice.pk_invoice=? "
					+ " AND isnull(ts_entrust.if_receipt,'')<>'Y' AND ISNULL(ts_invoice.dr,0)=0 AND ISNULL(ts_entrust.dr,0)=0 ";
			Integer count= NWDao.getInstance().queryForObject(sql0, Integer.class, entrustVO.getPk_entrust(),podVO.getPk_invoice());
			if(count == 0){
				podService.doReceipt(new String[]{podVO.getPk_invoice()}, newPodVO, paramVO);
			}
		}
		return execFormula4Templet(billVO, paramVO);
	}

	public Map<String, Object> expReceipt(String pk_entrust, PodVO podVO, ParamVO paramVO, List<EntPackBVO> packBVOs) {
		if(podVO.getAct_receipt_date().after(new UFDate(new Date()))){
			throw new BusiException("回单时间错误！");
		}
		//先进行委托单货量更新
		paramVO.setBillId(pk_entrust);
		AggregatedValueObject billVO = queryBillVO(paramVO);
		ExAggEntrustVO aggEntrustVO = (ExAggEntrustVO) billVO;
		EntrustVO entrustVO = (EntrustVO) billVO.getParentVO();
		if(entrustVO.getIf_receipt() != null && entrustVO.getIf_receipt().equals(UFBoolean.TRUE)){
			throw new BusiException("委托单已回单！");
		}
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		UFDouble fee_weight_count = UFDouble.ZERO_DBL;
		for(EntPackBVO packBVO : packBVOs){
			num_count += (packBVO.getNum() == null ? 0 : packBVO.getNum());
			volume_count = volume_count.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
			weight_count = weight_count.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
		}
		// 根据运段读取客户
		UFDouble rate = carrService.getFeeRate(entrustVO.getPk_carrier(), entrustVO.getPk_trans_type(),  entrustVO.getDeli_city(), entrustVO.getArri_city());
		UFDouble volume_weight_count = UFDouble.ZERO_DBL;
		if(rate != null && rate.doubleValue() != 0){
			volume_weight_count = rate.multiply(volume_count);
			if(volume_weight_count.doubleValue() < weight_count.doubleValue()){
				fee_weight_count = weight_count;
			}else{
				fee_weight_count = volume_weight_count;
			}
			
		}else{
			fee_weight_count = weight_count;
		}
		entrustVO.setNum_count(num_count);
		entrustVO.setWeight_count(weight_count);
		entrustVO.setVolume_weight_count(volume_weight_count);
		entrustVO.setVolume_count(volume_count);
		entrustVO.setFee_weight_count(fee_weight_count);
		entrustVO.setStatus(VOStatus.UPDATED);
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(EntPackBVO packBVO : packBVOs){
			packBVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(packBVO);
		}
		aggEntrustVO.setTableVO(TabcodeConst.TS_ENT_PACK_B, packBVOs.toArray(new EntPackBVO[packBVOs.size()]));
		save(billVO, paramVO);
		return receipt(entrustVO.getVbillno(), podVO, paramVO);
	}
	
	public Map<String, Object> unReceipt(String vbillno, ParamVO paramVO) {
		if(StringUtils.isBlank(vbillno)){
			throw new BusiException("委托单号不能为空！");
		}
		EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", vbillno);
		if(entrustVO == null){
			throw new BusiException("委托单不存在！");
		}
		if(!entrustVO.getIf_receipt().equals(UFBoolean.TRUE)){
			throw new BusiException("只有已回单的委托单才可以撤销回单！");
		}
		entrustVO.setStatus(VOStatus.UPDATED);
		entrustVO.setIf_receipt(UFBoolean.FALSE);
		entrustVO.setReceipt_man(null);
		entrustVO.setReceipt_time(null);
		entrustVO.setReceipt_book_man(null);
		entrustVO.setReceipt_book_time(null);
		entrustVO.setReceipt_memo(null);
		entrustVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(entrustVO);
		AggregatedValueObject billVO = new ExAggEntrustVO();
		billVO.setParentVO(entrustVO);
		String sql = "SELECT DISTINCT  ts_pod.pk_pod "
				+ " FROM ts_entrust "
				+ " LEFT JOIN ts_invoice  WITH(NOLOCK) ON ts_entrust.invoice_vbillno=ts_invoice.vbillno AND ts_invoice.dr=0 "
				+ " LEFT JOIN ts_segment WITH(NOLOCK) ON ts_invoice.vbillno=ts_segment.invoice_vbillno AND  "
				+ " ((ts_segment.parent_seg IS NULL AND  ts_segment.seg_mark=0) OR ts_segment.seg_mark=2 ) "
				+ " 	AND ts_segment.dr=0 AND ts_segment.pk_arrival=ts_invoice.pk_arrival "
				+ " LEFT JOIN ts_entrust te2 WITH(NOLOCK) ON te2.segment_vbillno=ts_segment.vbillno AND te2.dr=0  "
				+ " LEFT JOIN ts_pod WITH(NOLOCK) ON ts_invoice.pk_invoice = ts_pod.pk_invoice AND ts_pod.dr=0  "
				+ " WHERE ts_entrust.vbillno=? and ts_invoice.vbillstatus = 6";
		List<Map<String,Object>> result = NWDao.getInstance().queryForList(sql, vbillno);
		
		if(result == null || result.size() != 1){
			return execFormula4Templet(billVO, paramVO);
		}
		PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pk_pod=?", String.valueOf(result.get(0).get("pk_pod")));
		if(podVO != null ){
			podService.doUnreceipt(new String[]{podVO.getPk_invoice()}, paramVO);
		}
		return execFormula4Templet(billVO, paramVO);
	}

	public List<Map<String, Object>> getPackRecord(String id) {
		if(StringUtils.isBlank(id)){
			return new ArrayList<Map<String, Object>>();
		}
		String sql = "select * from ts_ent_pack_record_view where pk_entrust=?";
		List<Map<String, Object>> result = NWDao.getInstance().queryForList(sql, id);
		return result;
	}
}
