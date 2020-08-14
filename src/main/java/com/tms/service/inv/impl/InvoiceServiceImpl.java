package com.tms.service.inv.impl;

import java.io.File;
import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.print.ImageResource;
import org.nw.service.ServiceHelper;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CodenoHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.HYBillVO;
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
import com.tms.constants.AddressConst;
import com.tms.constants.AreaConst;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.OperateTypeConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.QuoteTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TrackingConst;
import com.tms.constants.TransLineConst;
import com.tms.constants.TransTypeConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.AreaService;
import com.tms.service.base.CustService;
import com.tms.service.base.TransLineService;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.inv.InvoiceService;
import com.tms.service.tp.PZService;
import com.tms.service.tp.StowageService;
import com.tms.service.tp.impl.PZUtils;
import com.tms.utils.TransLineUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvReqBVO;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.rf.InvPackBarBVO;
import com.tms.vo.te.EntInvBVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntTransHisBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 发货单操作
 * 
 * @author xuqc
 * @date 2012-8-8 下午09:11:10
 */
@Service
public class InvoiceServiceImpl extends TMSAbsBillServiceImpl implements InvoiceService {

	@Autowired
	private TransLineService transLineService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private ContractService contractService;

	@Autowired
	private PZService pZService;

	@Autowired
	private ExpenseTypeService expenseTypeService;

	@Autowired
	private StowageService stowageService;
	
	@Autowired
	private CustService custService;

	private AggregatedValueObject billInfo;

	/**
	 * 因为表体有个tab是属于没有直接联系的表，故定义一个getRealBillInfo方法
	 */
	// yaojiie 2015 12 18 添加路线表信息
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, ReceDetailBVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, InvoiceVO.PK_INVOICE);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_rece_detail_b");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_rece_detail_b");

			billInfo = getRealBillInfo();
			CircularlyAccessibleValueObject[] childrenVO = billInfo.getChildrenVO();
			CircularlyAccessibleValueObject[] newChildrenVO = new CircularlyAccessibleValueObject[childrenVO.length
					+ 1];
			for (int i = 0; i < childrenVO.length; i++) {
				newChildrenVO[i] = childrenVO[i];
			}
			newChildrenVO[childrenVO.length] = childVO2;
			billInfo.setChildrenVO(newChildrenVO);
		}
		return billInfo;
	}

	// yaojiie 2015 12 18 添加路线表信息
	public AggregatedValueObject getRealBillInfo() {
		AggregatedValueObject billInfo = new ExAggCustVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, InvoiceVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, InvoiceVO.PK_INVOICE);
		billInfo.setParentVO(vo);

		VOTableVO childVO = new VOTableVO();
		childVO.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
		childVO.setAttributeValue(VOTableVO.HEADITEMVO, InvPackBVO.class.getName());
		childVO.setAttributeValue(VOTableVO.PKFIELD, InvPackBVO.PK_INVOICE);
		childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_pack_b");
		childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_pack_b");

		VOTableVO childVO1 = new VOTableVO();
		childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
		childVO1.setAttributeValue(VOTableVO.HEADITEMVO, TransBilityBVO.class.getName());
		childVO1.setAttributeValue(VOTableVO.PKFIELD, TransBilityBVO.PK_INVOICE);
		childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_trans_bility_b");
		childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_trans_bility_b");

		VOTableVO childVO2 = new VOTableVO();
		childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
		childVO2.setAttributeValue(VOTableVO.HEADITEMVO, InvLineBVO.class.getName());
		childVO2.setAttributeValue(VOTableVO.PKFIELD, InvLineBVO.PK_INVOICE);
		childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_line_b");
		childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_line_b");

		VOTableVO childVO3 = new VOTableVO();
		childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
		childVO3.setAttributeValue(VOTableVO.HEADITEMVO, InvReqBVO.class.getName());
		childVO3.setAttributeValue(VOTableVO.PKFIELD, InvReqBVO.PK_INVOICE);
		childVO3.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_req_b");
		childVO3.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_req_b");

		VOTableVO childVO4 = new VOTableVO();
		childVO4.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
		childVO4.setAttributeValue(VOTableVO.HEADITEMVO, InvPackBarBVO.class.getName());
		childVO4.setAttributeValue(VOTableVO.PKFIELD, InvPackBarBVO.PK_INVOICE);
		childVO4.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_pack_bar_b");
		childVO4.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_pack_bar_b");

		CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1, childVO2, childVO3, childVO4 };
		billInfo.setChildrenVO(childrenVO);
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.FHD;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("bala_customer")) {
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
					fieldVO.setUserdefine1("refreshReceDetail()");
					// } else if(fieldVO.getItemkey().equals("tracking_status"))
					// {
					// fieldVO.setRenderer("trackInfoRenderer");
				} else if (fieldVO.getItemkey().equals("pk_op_project")) {
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				}  else if (fieldVO.getItemkey().equals("pk_delivery")) {
					// 提货方
					fieldVO.setUserdefine1("afterChangePk_delivery();getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				} else if (fieldVO.getItemkey().equals("pk_arrival")) {
					// 收货方
					fieldVO.setUserdefine1("afterChangePk_arrival();getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				} else if (fieldVO.getItemkey().equals("deli_city")) {
					fieldVO.setUserdefine1("getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("area_level=5");
				} else if (fieldVO.getItemkey().equals("arri_city")) {
					fieldVO.setUserdefine1("getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("larea_level=5");
				} else if (fieldVO.getItemkey().equals("pk_trans_type")) {
					// 运输方式，需要重新计算总计费重
					fieldVO.setUserdefine1("updateHeaderFeeWeightCount();refreshReceDetail()");
				} else if (fieldVO.getItemkey().equals("deli_contact")) {
					// 提货联系人，参照地址档案联系人，ts_addr_contact
					fieldVO.setUserdefine3("pk_address:${Ext.getCmp('pk_delivery').getValue()}");
				} else if (fieldVO.getItemkey().equals("arri_contact")) {
					// 收货联系人，参照地址档案联系人，ts_addr_contact
					fieldVO.setUserdefine3("pk_address:${Ext.getCmp('pk_arrival').getValue()}");
				} else if (fieldVO.getItemkey().equals("req_deli_date")) {
					// 要求提货日期
					fieldVO.setUserdefine1("afterChangeReq_deli_date(field,value,originalValue)");
					fieldVO.setBeforeRenderer("req_deli_dateBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("req_arri_date")) {
					// 要求收货日期
					fieldVO.setUserdefine1("afterChangeReq_arri_date(field,value,originalValue)");
					fieldVO.setBeforeRenderer("req_arri_dateBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("exp_flag")) {
					// 跟踪信息是否异常
					fieldVO.setBeforeRenderer("exp_flagBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("balatype")) {
					// 结算方式
					fieldVO.setUserdefine1("afterChageBalatype(field,value,originalValue)");
				} else if (fieldVO.getItemkey().equals("urgent_level")) {
					fieldVO.setBeforeRenderer("urgent_levelBeforeRenderer");
				}
			} else if (fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if (fieldVO.getTable_code().equals(TabcodeConst.TS_INV_PACK_B)
						|| fieldVO.getTable_code().equals(TabcodeConst.TS_INV_LINE_B)) {
					if (fieldVO.getItemkey().equals("num")) { // 更新件数时，更新其他信息，及表头统计信息
						fieldVO.setUserdefine1("afterEditNumOfPack(record)");
					}
					if (fieldVO.getItemkey().equals("pack_num_count")) { // 更新数量
						fieldVO.setUserdefine1("afterEditPackNumCount(record)");
					} else if (fieldVO.getItemkey().equals("pack_name")) {
						// 编辑“包装”时，如果货品匹配到，则带出包装的信息
						fieldVO.setUserdefine1("afterEditPack(record)");
					} else if (fieldVO.getItemkey().equals("goods_code")) {
						// 编辑“货品”时，需要将货品的条件加入包装的参照中
						fieldVO.setUserdefine1("afterEditGoodsCode(record)");
					} else if (fieldVO.getItemkey().equals("length") || fieldVO.getItemkey().equals("width")
							|| fieldVO.getItemkey().equals("height")) {
						fieldVO.setUserdefine1("afterEditLengthOrWidthOrHeight(record)");
					} else if (fieldVO.getItemkey().equals("volume")) {
						// 编辑体积
						fieldVO.setUserdefine1("afterEditVolume(record)");
					} else if (fieldVO.getItemkey().equals("weight")) {
						// 编辑重量
						fieldVO.setUserdefine1("afterEditWeight(record)");
					} else if (fieldVO.getItemkey().equals("unit_volume")) {
						// 编辑单位体积
						fieldVO.setUserdefine1("afterEditUnit_volume(record)");
					} else if (fieldVO.getItemkey().equals("unit_weight")) {
						// 编辑单位重量
						fieldVO.setUserdefine1("afterEditUnit_weight(record)");
					} else if (fieldVO.getItemkey().equals("goods_type")) {
						fieldVO.setUsereditflag(Constants.YES);
					} else if (fieldVO.getItemkey().equals("addr_code") || fieldVO.getItemkey().equals("pk_address")) {
						fieldVO.setUserdefine1("afterEditLineAddress(record)");
					}else if (fieldVO.getItemkey().equals("req_date_from")) {
						fieldVO.setUserdefine1("afterEditLineReqDate(record)");
					}
				} else if (fieldVO.getTable_code().equals(TabcodeConst.TS_TRANS_BILITY_B)) {
					if (fieldVO.getItemkey().equals("pk_car_type")) {
						fieldVO.setUserdefine1("refreshReceDetail()");
					} else if (fieldVO.getItemkey().equals("num")) {
						fieldVO.setUserdefine1("afterEditNumOfCar(record)");
					} else if (fieldVO.getItemkey().equals("price")) {
						fieldVO.setUserdefine1("afterEditPriceOfCar(record)");
					}
				} else if (fieldVO.getTable_code().equals(TabcodeConst.TS_RECE_DETAIL_B)) {
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

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		if (paramVO.isBody() && (TabcodeConst.TS_INV_PACK_B.equals(paramVO.getTabCode()) || TabcodeConst.TS_INV_LINE_B.equals(paramVO.getTabCode()))) {
			// 根据行号排序
			return "order by serialno";
		}
		return super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> headerMap = super.getHeaderDefaultValues(paramVO);
		headerMap.put("balatype", 0); // 默认结算方式
		String order_time = new UFDateTime(new Date()).toString();
		order_time = order_time.substring(0, order_time.length() - 3); // 不需要秒
		headerMap.put("order_time", order_time); // 接单时间
		headerMap.put("service_type", 0); // 默认服务方式
		headerMap.put("invoice_origin", 0); // 默认发货单来源
		// 默认时间
		// headerMap.put(InvoiceVO.REQ_DELI_DATE,
		// ParameterHelper.getParamValue("inv_req_deli_date"));
		// headerMap.put(InvoiceVO.REQ_ARRI_DATE,
		// ParameterHelper.getParamValue("inv_req_arri_date"));
		return headerMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyDefaultValues(ParamVO paramVO, Map<String, Object> headerMap) {
		Map<String, Object> valuesMap = super.getBodyDefaultValues(paramVO, headerMap);
		Map<String, Object> tabValuesMap = (Map<String, Object>) valuesMap.get(TabcodeConst.TS_RECE_DETAIL_B);
		tabValuesMap.put("price_type", 0); // 价格类型，默认“区间”，表体的下拉框在设置默认值时有bug，设置的是text
		tabValuesMap.put("quote_type", 0);// 报价类型，默认“单价”
		return valuesMap;
	}

	/**
	 * 1、重新计算总金额 2、更新运力信息的单价和金额
	 */
	private void afterReloadDetailGrid(AggregatedValueObject billVO) {
		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO;
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		ReceDetailBVO[] rdBVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		if (rdBVOs != null && rdBVOs.length > 0) {
			UFDouble cost_amount = new UFDouble(0);
			for (ReceDetailBVO rdBVO : rdBVOs) {
				if (rdBVO.getStatus() != VOStatus.DELETED) {
					cost_amount = cost_amount.add(rdBVO.getAmount());
				}
			}
			parentVO.setCost_amount(cost_amount);

			TransBilityBVO[] tbBVOs = (TransBilityBVO[]) aggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
			if (tbBVOs != null && tbBVOs.length > 0) {
				for (TransBilityBVO tbBVO : tbBVOs) {
					for (ReceDetailBVO rdBVO : rdBVOs) {
						if (rdBVO.getStatus() != VOStatus.DELETED) {
							if (rdBVO.getValuation_type() != null
									&& rdBVO.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
								// 计价方式是设备
								tbBVO.setPrice(rdBVO.getPrice());
								UFDouble price = rdBVO.getPrice() == null ? UFDouble.ZERO_DBL : rdBVO.getPrice();
								tbBVO.setAmount(price.multiply(tbBVO.getNum() == null ? 0 : tbBVO.getNum()));
							}
						}
					}
				}
			}
		}
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		// 对于外部的方法调用此方法时，常常不能给出正确的模板ID等信息，这里让自己重新生成这些信息
		// 但是对于接口同步数据时，没有登录信息会导致下面方法报错，而且接口执行也不需要这个方法。
		if (WebUtils.getLoginInfo() != null) {
			if (StringUtils.isBlank(paramVO.getTemplateID())) {
				String templateID = getBillTemplateID(paramVO);
				paramVO.setTemplateID(templateID);
			}
			if (StringUtils.isBlank(paramVO.getHeaderTabCode())) {
				paramVO.setHeaderTabCode(TabcodeConst.TS_INVOICE);
			}
		}
		super.processBeforeSave(billVO, paramVO);
		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO;
		List<SuperVO> updateList = new ArrayList<SuperVO>();
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		if(StringUtils.isBlank(parentVO.getBilling_corp())){
			CustomerVO customerVO = dao.queryByCondition(CustomerVO.class, "pk_customer=?", parentVO.getPk_customer());
			parentVO.setBilling_corp(customerVO.getBilling_corp());
		}
		
		//生成地址
		// Object updateAddr = paramVO.getAttr().get("updateAddr");
		// if (updateAddr == null || new
		// Boolean(updateAddr.toString()).booleanValue()) {
		// // 判断此时的提货方pk是否存在，如果存在，则更新到ts_address表中，否则添加到ts_address表中
		// if (StringUtils.isNotBlank(parentVO.getPk_delivery())) {
		// // 根据该值到ts_address中匹配，若匹配到，则说明是选择的记录
		// AddressVO addressVO = dao.queryByCondition(AddressVO.class,
		// "pk_address=?", parentVO.getPk_delivery());
		// if (addressVO != null) {
		// // 匹配到了，更新信息
		// addressVO.setStatus(VOStatus.UPDATED);
		// } else {
		// addressVO = new AddressVO();
		// addressVO.setAddr_name(parentVO.getPk_delivery()); // 设置地址名称
		// addressVO.setAddr_code(CodenoHelper.generateCode(FunConst.ADDRESS_FUN_CODE));
		// // 生成地址档案的编码
		// addressVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp()); //yaojiie
		// 2015 12 11 只修改新增情况下的公司信息
		// addressVO.setStatus(VOStatus.NEW);
		// NWDao.setUuidPrimaryKey(addressVO);
		// parentVO.setPk_delivery(addressVO.getPk_address());// 设置到主表的VO
		// // 同时更新到客户关联地址表中
		// CustAddrVO custAddrVO = new CustAddrVO();
		// custAddrVO.setPk_customer(parentVO.getPk_customer());
		// custAddrVO.setPk_address(addressVO.getPk_address());
		// custAddrVO.setStatus(VOStatus.NEW);
		// NWDao.setUuidPrimaryKey(custAddrVO);
		// updateList.add(custAddrVO);
		// }
		// addressVO.setPk_city(parentVO.getDeli_city());
		// addressVO.setPk_province(parentVO.getDeli_province());
		// addressVO.setPk_area(parentVO.getDeli_area());
		// addressVO.setDetail_addr(parentVO.getDeli_detail_addr());
		// addressVO.setContact(parentVO.getDeli_contact());
		// addressVO.setMobile(parentVO.getDeli_mobile());
		// addressVO.setPhone(parentVO.getDeli_phone());
		// addressVO.setEmail(parentVO.getDeli_email());
		// // addressVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		// updateList.add(addressVO);
		// }
		//
		// // 判断此时的收货方pk是否存在，如果存在，则更新到ts_address表中，否则添加到ts_address表中
		// if (StringUtils.isNotBlank(parentVO.getPk_arrival())) {
		// // 根据该值到ts_address中匹配，若匹配到，则说明是选择的记录
		// AddressVO addressVO = dao.queryByCondition(AddressVO.class,
		// "pk_address=?", parentVO.getPk_arrival());
		// if (addressVO != null) {
		// // 匹配到了，更新信息
		// addressVO.setStatus(VOStatus.UPDATED);
		// } else {
		// // 匹配不到，是新增的记录
		// addressVO = new AddressVO();
		// addressVO.setAddr_name(parentVO.getPk_arrival()); // 设置地址名称
		// addressVO.setAddr_code(CodenoHelper.generateCode(FunConst.ADDRESS_FUN_CODE));
		// // 生成地址档案的编码
		// addressVO.setStatus(VOStatus.NEW);
		// NWDao.setUuidPrimaryKey(addressVO);
		// parentVO.setPk_arrival(addressVO.getPk_address());// 设置到主表的VO
		// // 同时更新到客户关联地址表中
		// CustAddrVO custAddrVO = new CustAddrVO();
		// custAddrVO.setPk_customer(parentVO.getPk_customer());
		// custAddrVO.setPk_address(addressVO.getPk_address());
		// custAddrVO.setStatus(VOStatus.NEW);
		// NWDao.setUuidPrimaryKey(custAddrVO);
		// updateList.add(custAddrVO);
		// }
		// addressVO.setPk_city(parentVO.getArri_city());
		// addressVO.setPk_province(parentVO.getArri_province());
		// addressVO.setPk_area(parentVO.getArri_area());
		// addressVO.setDetail_addr(parentVO.getArri_detail_addr());
		// addressVO.setContact(parentVO.getArri_contact());
		// addressVO.setMobile(parentVO.getArri_mobile());
		// addressVO.setPhone(parentVO.getArri_phone());
		// addressVO.setEmail(parentVO.getArri_email());
		// //不需要更新地址所属公司2015-12-31 lanjian
		// //addressVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		// updateList.add(addressVO);
		// }
		// }
		// 货品包装
		InvPackBVO[] invPackBVOs = (InvPackBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		InvLineBVO[] invLineBVOs = (InvLineBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
		boolean flag = true;
		if(invLineBVOs != null && invLineBVOs.length > 0){
			for(InvLineBVO invLineBVO : invLineBVOs){
				if (StringUtils.isBlank(invLineBVO.getGoods_code()) && WebUtils.getLoginInfo() != null) {
					// 输入货品包装，检查是否输入了货品编码，如果没有，则生成一个
					invLineBVO.setGoods_code(CodenoHelper.generateCode(FunConst.GOODS_FUN_CODE));
				}
			}
			//如果所有的线路信息和包装信息都没有修改过，这里的逻辑就不再执行
			for(InvLineBVO invLineBVO : invLineBVOs){
				if(invLineBVO.getStatus() != VOStatus.UNCHANGED){
					InvoiceUtils.syncMultiPointTransportation(billVO, paramVO);
					flag = false;
					break;
				}
			}
		}
		if(flag){
			parentVO.setTrans_type(DataDictConst.TRANSPORT_TYPE.LD.intValue());
		}
		//生成包装信息
		if (invPackBVOs != null && invPackBVOs.length > 0) {
			// 统计总件数、重量、体积,体积重，计费重
			if (WebUtils.getLoginInfo() != null) {// 接口数据，不计算头信息
				InvoiceUtils.setHeaderCount(parentVO, invPackBVOs);
			}
			int index = 0;
			for (InvPackBVO invPackBVO : invPackBVOs) {
				if (StringUtils.isBlank(invPackBVO.getPk_goods()) && StringUtils.isBlank(invPackBVO.getGoods_code())
						&& WebUtils.getLoginInfo() != null) {
					// 输入货品包装，检查是否输入了货品编码，如果没有，则生成一个
					invPackBVO.setGoods_code(CodenoHelper.generateCode(FunConst.GOODS_FUN_CODE));
				}
				// 保存时重置下行号
				if (invPackBVO.getStatus() != VOStatus.DELETED) {
					index += 10;
					invPackBVO.setSerialno(index);
					if (invPackBVO.getStatus() == VOStatus.UNCHANGED) {
						invPackBVO.setStatus(VOStatus.UPDATED);
					}
				}
			}
		}
		//生成线路编码
//		if (invLineBVOs != null && invLineBVOs.length > 0){
//			int index1 = 0;
//			for(InvLineBVO invLineBVO : invLineBVOs){
//				if(invLineBVO.getStatus() != VOStatus.DELETED){
//					index1 += 10;
//					invLineBVO.setSerialno(index1);
//					if (invLineBVO.getStatus() == VOStatus.UNCHANGED) {
//						invLineBVO.setStatus(VOStatus.UPDATED);
//					}
//				}
//			}
//		}
		
		String funCode = paramVO.getFunCode();
		// 匹配费用
		ReceDetailBVO[] oldDetailBVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		List<ContractBVO> contractBVOs = null;
		if (!funCode.equals(FunConst.CUSTOM_IMPORT_CODE)) {
			contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, parentVO.getBala_customer(),
					parentVO.getPk_trans_type(), parentVO.getPk_delivery(), parentVO.getPk_arrival(),
					parentVO.getDeli_city(), parentVO.getArri_city(), parentVO.getPk_corp(),
					parentVO.getReq_arri_date(), parentVO.getUrgent_level(), parentVO.getItem_code(),
					parentVO.getPk_trans_line(), parentVO.getIf_return());

		}
		if (contractBVOs != null && contractBVOs.size() > 0) {
			// 匹配到合同
			TransBilityBVO[] tbBVOs = (TransBilityBVO[]) aggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
			String[] pk_car_type = null;
			if (tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for (int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}
			
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			Map<String, List<InvPackBVO>> groupMap = new HashMap<String, List<InvPackBVO>>();
			// 对包装按照pack进行分组
			for (InvPackBVO invPackBVO : invPackBVOs) {
				String key = invPackBVO.getPack();
				if (StringUtils.isBlank(key)) {
					// 没有包装的货品自动过滤
					continue;
				}
				List<InvPackBVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<InvPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(invPackBVO);
			}
			if (groupMap.size() > 0) {
				for (String key : groupMap.keySet()) {
					PackInfo packInfo = new PackInfo();
					List<InvPackBVO> voList = groupMap.get(key);
					Integer num = 0;
					UFDouble weight = UFDouble.ZERO_DBL;
					UFDouble volume = UFDouble.ZERO_DBL;
					for (InvPackBVO packBVO : voList) {
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
			List<ReceDetailBVO> newDetailBVOs = contractService.buildReceDetailBVO(parentVO.getBala_customer(),
					parentVO.getPack_num_count() == null ? 0 : parentVO.getPack_num_count().doubleValue(),
					parentVO.getNum_count() == null ? 0 : parentVO.getNum_count(),
					parentVO.getFee_weight_count() == null ? 0 : parentVO.getFee_weight_count().doubleValue(),
					parentVO.getWeight_count() == null ? 0 : parentVO.getWeight_count().doubleValue(),
					parentVO.getVolume_count() == null ? 0 : parentVO.getVolume_count().doubleValue(), packInfos,
					pk_car_type, parentVO.getPk_corp(), contractBVOs);
			if (newDetailBVOs != null && newDetailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for (ReceDetailBVO detailBVO : newDetailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
				}

				if (oldDetailBVOs != null && oldDetailBVOs.length > 0) {
					String transFeeCode = ExpenseTypeConst.ET10;// 运费的编码
					boolean hasTransFee = false;
					// 检查匹配的费用明细是否包括运费记录
					for (ReceDetailBVO detailBVO : newDetailBVOs) {
						ExpenseTypeVO etVO = expenseTypeService.getByPrimaryKey(ExpenseTypeVO.class,
								detailBVO.getPk_expense_type());
						if (etVO != null) {
							if (transFeeCode.equalsIgnoreCase(etVO.getCode())) {
								hasTransFee = true;
								break;
							}
						}
					}
					for (ReceDetailBVO oldDetailBVO : oldDetailBVOs) {
						if (oldDetailBVO.getSystem_create() == null
								|| !oldDetailBVO.getSystem_create().booleanValue()) {
							// 非系统创建的费用明细
							if (hasTransFee) {
								// 匹配合同的费用明细已经包括了运费，这里就不加入运费了
								ExpenseTypeVO etVO = expenseTypeService.getByPrimaryKey(ExpenseTypeVO.class,
										oldDetailBVO.getPk_expense_type());
								if (etVO != null) {
									if (transFeeCode.equalsIgnoreCase(etVO.getCode())) {
										// 这个运费记录就不加入了
									} else {
										newDetailBVOs.add(oldDetailBVO);
									}
								}
							} else {
								newDetailBVOs.add(oldDetailBVO);
							}
						} else {
							// 系统创建的费用明细
							// XXX 系统创建的费用明细已经都在detailBVOs这里了，现在界面上的系统费用明细不需要了
							if (oldDetailBVO.getStatus() != VOStatus.NEW) {
								oldDetailBVO.setStatus(VOStatus.DELETED);//
								// 删除原有的系统创建的费用明细
								newDetailBVOs.add(oldDetailBVO);
							}
						}
					}
				}
				// 重新设置新的费用明细
				aggVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B,
						newDetailBVOs.toArray(new ReceDetailBVO[newDetailBVOs.size()]));
				// 更新总金额,及运力信息的单价和金额
				afterReloadDetailGrid(billVO);
			}
		} else {
			// XXX 2014-03-27 如果没有匹配到合同
			if (oldDetailBVOs != null && oldDetailBVOs.length > 0) {
				for (ReceDetailBVO rdBVO : oldDetailBVOs) {
					if (rdBVO.getSystem_create() == null || !rdBVO.getSystem_create().booleanValue()) {
						// 非系统创建的费用明细

					} else {
						// 系统创建的费用明细
						rdBVO.setStatus(VOStatus.DELETED);// 删除原有的系统创建的费用明细
					}
				}
			}
		}

		// 保存到应收明细表
		ReceiveDetailVO detailVO = null;
		if (StringUtils.isNotBlank(parentVO.getPrimaryKey())) {
			// 更新发货单的情况，根据发货单的单据号，读取应收明细
			detailVO = getReceiveDetailVOByInvoiceBillno(parentVO.getVbillno());// 可能发货单没有对应的应收明细
			if (detailVO != null) {
				if (detailVO.getVbillstatus().intValue() != BillStatus.NEW) {
					throw new BusiException("发货单对应的应收明细不是[新建]状态，不能更新发货单！");
				}
				detailVO.setOrderno(parentVO.getOrderno());
				detailVO.setCust_orderno(parentVO.getCust_orderno());
				detailVO.setStatus(VOStatus.UPDATED);
			}
		}
		if (detailVO == null) {
			// 新增发货单的情况
			detailVO = new ReceiveDetailVO();
			detailVO.setStatus(VOStatus.NEW);
			detailVO.setVbillstatus(BillStatus.NEW);
			detailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YSMX)); // 生成应收明细的单据号
			if (WebUtils.getLoginInfo() != null) {
				// 如果没有登录信息，可能是通过其他系统导入的形式
				detailVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				detailVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			} else {
				detailVO.setCreate_user(parentVO.getCreate_user());
				detailVO.setPk_corp(parentVO.getPk_corp());
			}
			detailVO.setCreate_time(new UFDateTime(new Date()));
			NWDao.setUuidPrimaryKey(detailVO);
		}
		if (detailVO.getDbilldate() == null) {
			detailVO.setDbilldate(new UFDate());
		}
		detailVO.setPk_customer(parentVO.getPk_customer());
		detailVO.setBala_customer(parentVO.getBala_customer());
		detailVO.setCurrency(ParameterHelper.getCurrency());
		detailVO.setPk_op_project(parentVO.getPk_op_project());
		detailVO.setBilling_corp(parentVO.getBilling_corp());
		detailVO.setPack_num_count(parentVO.getPack_num_count());
		detailVO.setNum_count(parentVO.getNum_count());
		detailVO.setFee_weight_count(parentVO.getFee_weight_count());
		detailVO.setWeight_count(parentVO.getWeight_count());
		detailVO.setVolume_count(parentVO.getVolume_count());
		detailVO.setCost_amount(parentVO.getCost_amount());
		detailVO.setUngot_amount(parentVO.getCost_amount());// 未收金额等于总金额
		detailVO.setBalatype(parentVO.getBalatype());
		detailVO.setInvoice_vbillno(parentVO.getVbillno()); // 发货单单据号
		detailVO.setMemo(parentVO.getMemo());
		detailVO.setRece_type(ReceiveDetailConst.ORIGIN_TYPE); // 表示这是由发货单生成的应收明细
		detailVO.setMerge_type(ReceiveDetailConst.MERGE_TYPE.UNMERGE.intValue()); // 合并类型
		detailVO.setOrderno(parentVO.getOrderno());
		detailVO.setCust_orderno(parentVO.getCust_orderno());
		// FIXME 取第一行合同明细的税种，税率
		if (contractBVOs != null && contractBVOs.size() > 0) {
			detailVO.setTax_cat(contractBVOs.get(0).getTax_cat());
			detailVO.setTax_rate(contractBVOs.get(0).getTax_rate());
			// FIXME 取第一行合同明细的税种，税率
			detailVO.setTaxmny(
					CMUtils.getTaxmny(detailVO.getCost_amount(), detailVO.getTax_cat(), detailVO.getTax_rate()));
		}
		detailVO.setAccount_period(new UFDateTime(parentVO.getReq_deli_date()));
		detailVO.setBilling_corp(parentVO.getBilling_corp());
		detailVO.setPk_op_project(parentVO.getPk_op_project());
		updateList.add(detailVO);// 保存应收明细

		// 保存费用明细
		List<ReceDetailBVO> allDetailBVOs = new ArrayList<ReceDetailBVO>();
		ReceDetailBVO[] receDetailBVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		if (receDetailBVOs == null) {
			receDetailBVOs = new ReceDetailBVO[0];
		}
		for (ReceDetailBVO receDetailBVO : receDetailBVOs) {
			if (receDetailBVO.getStatus() == VOStatus.NEW) {
				NWDao.setUuidPrimaryKey(receDetailBVO);
			}
			receDetailBVO.setPk_receive_detail(detailVO.getPk_receive_detail()); // 设置主表的主键
			if (receDetailBVO.getPk_rece_detail_b() != null) {
				updateList.add(receDetailBVO);
			}
			allDetailBVOs.add(receDetailBVO);
		}
		aggVO.removeTableVO(TabcodeConst.TS_RECE_DETAIL_B); // 这个VO其实是不属于这个主表的，放在这边主要是维护方便

		/*
		 * // 如果运力信息存在记录，而费用明细没有计价方式为“设备”的记录，那么需要插入一条费用明细 TransBilityBVO[] tbVOs
		 * = (TransBilityBVO[])
		 * aggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B); if (tbVOs != null
		 * && tbVOs.length > 0) { UFDouble amount = new UFDouble(0); for
		 * (TransBilityBVO tbVO : tbVOs) { amount = amount.add(tbVO.getAmount()
		 * == null ? new UFDouble(0) : tbVO.getAmount()); } boolean isExist =
		 * false; for (ReceDetailBVO receDetailBVO : receDetailBVOs) { if
		 * (receDetailBVO.getStatus() != VOStatus.DELETED &&
		 * receDetailBVO.getValuation_type() != null &&
		 * receDetailBVO.getValuation_type().intValue() ==
		 * ValuationTypeConst.EQUIP) { isExist = true; } } if (!isExist) {
		 * ReceDetailBVO tdBVO = new ReceDetailBVO(); SuperVO superVO =
		 * expenseTypeService.getByCode(ExpenseTypeConst.ET10); if (superVO ==
		 * null) { throw new BusiException("不存在费用类型为“运费”的记录，请现在费用类型档案上维护！"); }
		 * ExpenseTypeVO etVO = (ExpenseTypeVO) superVO;
		 * tdBVO.setPk_expense_type(etVO.getPk_expense_type());// 运费
		 * tdBVO.setValuation_type(ValuationTypeConst.EQUIP);// 设备
		 * tdBVO.setQuote_type(QuoteTypeConst.INTERVAL);// 区间报价
		 * tdBVO.setPrice_type(PriceTypeConst.REGULAR_PRICE);// 固定价格
		 * tdBVO.setPrice(tbVOs.length > 1 ? null : tbVOs[0].getPrice());
		 * tdBVO.setAmount(amount);
		 * tdBVO.setPk_receive_detail(detailVO.getPk_receive_detail());
		 * tdBVO.setStatus(VOStatus.NEW); NWDao.setUuidPrimaryKey(tdBVO);
		 * updateList.add(tdBVO); allDetailBVOs.add(tdBVO); } }
		 */

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
					if (receDetailBVOs[i].getPk_rece_detail_b() != null) {
						// 这种情况可能是复制了单据，此时这条保险单还在，但实际上不应该在这边，将其状态设置为删除，但是不要执行真正的删除
						// 因为此时没有主键pk
						updateList.add(receDetailBVOs[i]);
					}
				}
			}

			// 增加一个新的保险费
			if (parentVO.getIf_insurance() != null && parentVO.getIf_insurance().booleanValue()) {
				ReceDetailBVO insBVO = new ReceDetailBVO();
				insBVO.setPk_expense_type(pk_expense_type);
				insBVO.setValuation_type(ValuationTypeConst.TICKET);
				insBVO.setQuote_type(QuoteTypeConst.INTERVAL);
				insBVO.setPrice_type(PriceTypeConst.REGULAR_PRICE);
				insBVO.setAmount(parentVO.getInsurance_amount());
				insBVO.setContract_amount(parentVO.getInsurance_amount());
				insBVO.setSystem_create(UFBoolean.TRUE);
				insBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(insBVO);
				insBVO.setPk_receive_detail(detailVO.getPk_receive_detail());
				updateList.add(insBVO);
				allDetailBVOs.add(insBVO);
			}
		}

		// 重新设置到发货单
		parentVO.setCost_amount(detailVO.getCost_amount());

		// 重新统计费用
		CMUtils.processExtenal(detailVO, allDetailBVOs.toArray(new ReceDetailBVO[allDetailBVOs.size()]));
		dao.saveOrUpdate(updateList);
		if (parentVO.getStatus() == VOStatus.NEW) {
			// 新建时，向发货单的跟踪信息表插入一条记录
			InvTrackingVO itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.NEW);
			itVO.setTracking_time(new UFDateTime(new Date()));
			itVO.setTracking_memo("新建");
			itVO.setInvoice_vbillno(parentVO.getVbillno());
			if (WebUtils.getLoginInfo() == null) {
				itVO.setPk_corp(parentVO.getPk_corp());
				itVO.setCreate_user(parentVO.getCreate_user());
			} else {
				itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			}

			itVO.setCreate_time(new UFDateTime(new Date()));
			itVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(itVO);
			NWDao.getInstance().saveOrUpdate(itVO);
		}
	}

	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		// 对于外部的方法调用此方法时，常常不能给出正确的模板ID等信息，这里让自己重新生成这些信息
		// 但是对于接口同步数据时，没有登录信息会导致下面方法报错，而且接口执行也不需要这个方法。
		if (WebUtils.getLoginInfo() != null) {
			if (StringUtils.isBlank(paramVO.getTemplateID())) {
				String templateID = getBillTemplateID(paramVO);
				paramVO.setTemplateID(templateID);
			}
			if (StringUtils.isBlank(paramVO.getHeaderTabCode())) {
				paramVO.setHeaderTabCode(TabcodeConst.TS_INVOICE);
			}
		}
		super.processAfterSave(billVO, paramVO);
		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO;
		// 获取一个发货单VO的一个list，计算金额利率。
		InvoiceVO invoiceVO = (InvoiceVO) billVO.getParentVO();
		InvLineBVO[] lineBVOs = (InvLineBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
		//重新计算发货单的头部件重体
		InvoiceUtils.setHeaderCount(invoiceVO, (InvPackBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_PACK_B));
		if(lineBVOs == null || lineBVOs.length == 0){
			//只有没有线路信息的时候才进行这个操作。
			InvoiceUtils.syncSegmentUpdater(billVO, paramVO);
		}else{
			//重新统计委托单运段，发货单的头信息。
			SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class, "invoice_vbillno=?", invoiceVO.getVbillno());
			if(segmentVOs != null && segmentVOs.length > 0){
				// 根据运段读取客户， 每次保存的的运输方式和rate都是一样的
				for(SegmentVO segmentVO : segmentVOs){
					SegPackBVO[] segPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment=?", segmentVO.getPk_segment());
					if(segPackBVOs == null || segPackBVOs.length == 0){
						segmentVO.setFee_weight_count(UFDouble.ZERO_DBL);
						segmentVO.setNum_count(0);
						segmentVO.setPack_num_count(UFDouble.ZERO_DBL);
						segmentVO.setVolume_count(UFDouble.ZERO_DBL);
						segmentVO.setWeight_count(UFDouble.ZERO_DBL);
						segmentVO.setVolume_weight_count(UFDouble.ZERO_DBL);
						segmentVO.setStatus(VOStatus.UPDATED);
						NWDao.getInstance().saveOrUpdate(segmentVO);
					}else{
						
						UFDouble rate = custService.getFeeRate(invoiceVO.getPk_customer(), invoiceVO.getPk_trans_type(),segmentVO.getDeli_city(),segmentVO.getArri_city());
						
						Integer num_count = 0;
						UFDouble volume_count = UFDouble.ZERO_DBL;
						UFDouble weight_count = UFDouble.ZERO_DBL;
						for(SegPackBVO packBVO : segPackBVOs){
							num_count = num_count + (packBVO.getNum() == null ? 0 : packBVO.getNum());
							volume_count = volume_count.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
							weight_count = weight_count.add(packBVO.getWeight()== null ? UFDouble.ZERO_DBL : packBVO.getWeight());
						}
						if(rate != null && rate.doubleValue() != 0) {
							UFDouble volume_weight_count = volume_count.multiply(rate);
							UFDouble fee_weight_count = UFDouble.ZERO_DBL;
							if(volume_weight_count.doubleValue() < weight_count.doubleValue()) {
								fee_weight_count = weight_count;
							} else {
								fee_weight_count = volume_weight_count;
							}
							segmentVO.setFee_weight_count(fee_weight_count);
							segmentVO.setVolume_weight_count(volume_weight_count);
						}
						segmentVO.setNum_count(num_count);
						segmentVO.setWeight_count(weight_count);
						segmentVO.setVolume_count(volume_count);
						segmentVO.setStatus(VOStatus.UPDATED);
						NWDao.getInstance().saveOrUpdate(segmentVO);
					}
				}
			}
			EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "invoice_vbillno=?", invoiceVO.getVbillno());
			if(entrustVOs != null && entrustVOs.length > 0){
				UFDouble rate = UFDouble.ONE_DBL;
				TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
						invoiceVO.getPk_trans_type());
				if(typeVO != null){
					if(typeVO.getRate() != null){
						rate = typeVO.getRate();
					}
				}
				for(EntrustVO entrustVO : entrustVOs){
					EntPackBVO[] entPackBVOs =  NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class, "pk_entrust=?", entrustVO.getPk_entrust());
					if(entPackBVOs == null || entPackBVOs.length == 0){
						entrustVO.setFee_weight_count(UFDouble.ZERO_DBL);
						entrustVO.setNum_count(0);
						entrustVO.setPack_num_count(UFDouble.ZERO_DBL);
						entrustVO.setVolume_count(UFDouble.ZERO_DBL);
						entrustVO.setWeight_count(UFDouble.ZERO_DBL);
						entrustVO.setVolume_weight_count(UFDouble.ZERO_DBL);
						entrustVO.setStatus(VOStatus.UPDATED);
						NWDao.getInstance().saveOrUpdate(entrustVO);
					}else{
						Integer num_count = 0;
						UFDouble volume_count = UFDouble.ZERO_DBL;
						UFDouble weight_count = UFDouble.ZERO_DBL;
						for(EntPackBVO packBVO : entPackBVOs){
							num_count = num_count + (packBVO.getNum() == null ? 0 : packBVO.getNum());
							volume_count = volume_count.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
							weight_count = weight_count.add(packBVO.getWeight()== null ? UFDouble.ZERO_DBL : packBVO.getWeight());
						}
						if(rate != null && rate.doubleValue() != 0) {
							UFDouble volume_weight_count = volume_count.multiply(rate);
							UFDouble fee_weight_count = UFDouble.ZERO_DBL;
							if(volume_weight_count.doubleValue() < weight_count.doubleValue()) {
								fee_weight_count = weight_count;
							} else {
								fee_weight_count = volume_weight_count;
							}
							entrustVO.setFee_weight_count(fee_weight_count);
							entrustVO.setVolume_weight_count(volume_weight_count);;
						}
						entrustVO.setNum_count(num_count);
						entrustVO.setWeight_count(weight_count);
						entrustVO.setVolume_count(volume_count);
						entrustVO.setStatus(VOStatus.UPDATED);
						NWDao.getInstance().saveOrUpdate(entrustVO);
					}
				}
			}
		}
		List<InvoiceVO> invoiceVOs = new ArrayList<InvoiceVO>();
		invoiceVOs.add(invoiceVO);
		CMUtils.totalCostComput(invoiceVOs);
		// 插入线路信息
		List<TransLineVO> transLineVOs = TransLineUtils.matchTransLine(TransLineConst.PZLX, invoiceVO);
		if (transLineVOs != null && transLineVOs.size() > 0) {
			invoiceVO.setPz_line(transLineVOs.get(0).getPk_trans_line());
			invoiceVO.setPz_mileage(transLineVOs.get(0).getMileage());
		} else {
			TransLineVO transLineVO = TransLineUtils.matchTransLineByArea(TransLineConst.PZLX, invoiceVO);
			if (transLineVO != null) {
				invoiceVO.setPz_line(transLineVO.getPk_trans_line());
				invoiceVO.setPz_mileage(transLineVO.getMileage());
			} else {
				invoiceVO.setPz_line(null);
				invoiceVO.setPz_mileage(null);
			}
		}
		invoiceVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(invoiceVO);

	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject parentVO = copyVO.getParentVO();
		ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) copyVO;
		parentVO.setAttributeValue("origin", 0); // 来源默认为客服下单
		// yaojiie 2015 12 29 复制时不复制一下信息
		parentVO.setAttributeValue("tracking_status", null);
		parentVO.setAttributeValue("tracking_memo", null);
		parentVO.setAttributeValue("exp_flag", null);
		parentVO.setAttributeValue("exp_type", null);
		parentVO.setAttributeValue("tracking_time", null);
		ReceiveDetailVO[] receiveDetailVOs = new ReceiveDetailVO[0];
		// 移除费用
		aggInvoiceVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B, receiveDetailVOs);
		// aggInvoiceVO.removeTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		// ReceDetailBVO[] receDetailBVOs = (ReceDetailBVO[])
		// aggInvoiceVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		// if(receDetailBVOs != null && receDetailBVOs.length > 0){
		// aggInvoiceVO.removeTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		// }

	}

	public AggregatedValueObject show(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		// 读取费用明细
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		ReceDetailBVO[] detailVOs = getReceDetailBVOsByInvoiceBillno(parentVO.getVbillno());
		((IExAggVO) billVO).setTableVO(TabcodeConst.TS_RECE_DETAIL_B, detailVOs);
		return billVO;
	}

	public InvoiceVO getByVbillno(String vbillno) {
		return dao.queryByCondition(InvoiceVO.class, "vbillno=?", vbillno);
	}

	/**
	 * 根据发货单的单据号查询费用明细记录
	 * 
	 * @param invoiceBillno
	 * @return
	 */
	public ReceDetailBVO[] getReceDetailBVOsByInvoiceBillno(String invoiceBillno) {
		ReceDetailBVO[] detailVOs = dao.queryForSuperVOArrayByCondition(ReceDetailBVO.class,
				"pk_receive_detail=(select pk_receive_detail from ts_receive_detail WITH(NOLOCK)  where isnull(dr,0)=0 and invoice_vbillno=? and rece_type=?)",
				invoiceBillno, ReceiveDetailConst.ORIGIN_TYPE);
		return detailVOs;
	}

	/**
	 * 根据发货单的单据号查询应收明细，注意是type=0的记录
	 * 
	 * @param invoiceBillno
	 * @return
	 */
	public ReceiveDetailVO getReceiveDetailVOByInvoiceBillno(String invoiceBillno) {
		return dao.queryByCondition(ReceiveDetailVO.class, "invoice_vbillno=? and rece_type=?", invoiceBillno,
				ReceiveDetailConst.ORIGIN_TYPE);
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		AggregatedValueObject billVO = ServiceHelper.queryBillVO(this.getRealBillInfo(), paramVO,
				new String[] { " order by serialno",""," order by serialno" });
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		ReceDetailBVO[] detailVOs = getReceDetailBVOsByInvoiceBillno(parentVO.getVbillno());
		((IExAggVO) billVO).setTableVO(TabcodeConst.TS_RECE_DETAIL_B, detailVOs);
		return billVO;
	}

	public Map<String, Object> getMileageAndDistance(String deli_city, String arri_city, String pk_delivery,
			String pk_arrival) {
		Map<String, Object> retMap = getMileageAndDistanceByAddr(pk_delivery, pk_arrival);
		if (retMap != null) {
			return retMap;
		}
		if (StringUtils.isBlank(deli_city) || StringUtils.isBlank(arri_city)) {
			return null;
		}
		TransLineVO condVO = new TransLineVO();
		condVO.setStart_addr(deli_city);
		condVO.setEnd_addr(arri_city);
		condVO.setLine_type(TransLineConst.YSJH);
		TransLineVO lineVO = transLineService.getByObject(condVO);
		if (lineVO == null) {
			// 调换顺序去查询
			// XXX 2013-4-21不需要调换顺序
			// condVO.setStart_addr(arri_city);
			// condVO.setEnd_addr(deli_city);
			// lineVO = transLineService.getByObject(condVO);
			// if(lineVO == null) {
			boolean change = false;
			AreaVO deliAreaVO = areaService.getByPrimaryKey(AreaVO.class, deli_city);
			AreaVO arriAreaVO = areaService.getByPrimaryKey(AreaVO.class, arri_city);
			if (deliAreaVO.getArea_level() != null) {
				if (deliAreaVO.getArea_level().intValue() != AreaConst.CITY_LEVEL) {
					// 如果不是城市，那么肯定是地区，此时需要读取地区的父级，也就是城市
					deliAreaVO = areaService.getByPrimaryKey(AreaVO.class, deliAreaVO.getParent_id());
					deli_city = deliAreaVO.getPk_area();
					change = true;
				}
			}
			if (arriAreaVO.getArea_level() != null) {
				if (arriAreaVO.getArea_level().intValue() != AreaConst.CITY_LEVEL) {
					// 如果不是城市，那么肯定是地区，此时需要读取地区的父级，也就是城市
					arriAreaVO = areaService.getByPrimaryKey(AreaVO.class, arriAreaVO.getParent_id());
					arri_city = arriAreaVO.getPk_area();
					change = true;
				}
			}
			condVO.setStart_addr(deli_city);
			condVO.setEnd_addr(arri_city);
			// 重新去路线表匹配
			if (change) {
				lineVO = transLineService.getByObject(condVO);
				// XXX 2013-4-21不需要调换顺序
				// if(lineVO == null) {
				// // 调换顺序去查询
				// condVO.setStart_addr(arri_city);
				// condVO.setEnd_addr(deli_city);
				// lineVO = transLineService.getByObject(condVO);
				// }
			}
			// }
		}
		if (lineVO == null) {
			return null;
		}
		// 根据城市读取两个城市
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("mileage", lineVO.getMileage());
		resultMap.put("distance", lineVO.getDistance()); // 区间距离
		return resultMap;
	}

	public Map<String, Object> getMileageAndDistanceByAddr(String pk_delivery, String pk_arrival) {
		if (StringUtils.isBlank(pk_delivery) || StringUtils.isBlank(pk_arrival)) {
			return null;
		}
		TransLineVO condVO = new TransLineVO();
		condVO.setStart_addr(pk_delivery);
		condVO.setEnd_addr(pk_arrival);
		condVO.setLine_type(TransLineConst.YSJH);
		TransLineVO lineVO = transLineService.getByObject(condVO);
		// if(lineVO == null) {
		// // 调换顺序去查询
		// condVO.setStart_addr(pk_arrival);
		// condVO.setEnd_addr(pk_delivery);
		// lineVO = transLineService.getByObject(condVO);
		// }
		if (lineVO == null) {
			return null;
		}
		// 根据城市读取两个城市
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("mileage", lineVO.getMileage());
		resultMap.put("distance", lineVO.getDistance()); // 区间距离
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getAddrInfoByPkAddress(String pk_address, String type) {
		// deli_city,deli_province,deli_area,deli_detail_addr,deli_contact,deli_phone,deli_mobile,deli_email->getcolsvalue("ts_address","pk_city","pk_province","pk_area","detail_addr","contact","phone","mobile","email","pk_address",pk_delivery,__nocache);
		String sql;
		if ("0".equals(type)) {
			// 提货方
			sql = "select pk_city as deli_city,pk_province as deli_province,pk_area as deli_area,detail_addr as deli_detail_addr,contact as deli_contact,phone as deli_phone,mobile as deli_mobile,email as deli_email from ts_address WITH(NOLOCK) where pk_address=?";
		} else {
			// 收货方
			sql = "select pk_city as arri_city,pk_province as arri_province,pk_area as arri_area,detail_addr as arri_detail_addr,contact as arri_contact,phone as arri_phone,mobile as arri_mobile,email as arri_email from ts_address WITH(NOLOCK)  where pk_address=?";
		}
		return (Map<String, Object>) dao.queryForObject(sql, HashMap.class, pk_address);
	}

	public void processBeforeConfirm(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeConfirm(billVO, paramVO);
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		logger.info("START----------------" + parentVO.getVbillno() + "------------------");
		logger.info("[XXX发货单：" + parentVO.getVbillno() + "]根据发货单开始构建运段...");
		//多线路订单需要特殊处理将路线信息拆成多个运段
		InvLineBVO[] lineBVOs = (InvLineBVO[]) ((ExAggInvoiceVO)billVO).getTableVO(TabcodeConst.TS_INV_LINE_B);
		InvPackBVO[] packBVOs = (InvPackBVO[]) ((ExAggInvoiceVO)billVO).getTableVO(TabcodeConst.TS_INV_PACK_B);
		if(lineBVOs != null && lineBVOs.length > 0 && !parentVO.getPk_trans_type().equals("89816b7d4cfe457881425a48fad21cc8")){
			//对lineBVOs按照pickup和delivery进行分组(这里只支持一点提多点送，和多点提一点送的业务，对多点提多点送的业务暂不支持!)
			Map<Integer,Map<String,List<InvLineBVO>>> groupMap = new HashMap<Integer, Map<String,List<InvLineBVO>>>();
			for(InvLineBVO lineBVO : lineBVOs){
				Integer operation = lineBVO.getOperate_type();
				Map<String,List<InvLineBVO>> sameOperationLineVOs = groupMap.get(operation);
				if(sameOperationLineVOs == null){
					sameOperationLineVOs = new HashMap<String, List<InvLineBVO>>();
					groupMap.put(operation, sameOperationLineVOs);
				}
				String pk_addressAndDate = lineBVO.getPk_address() + lineBVO.getReq_date_from();
				List<InvLineBVO> sameAddressLineVOs = sameOperationLineVOs.get(pk_addressAndDate);
				if(sameAddressLineVOs == null){
					sameAddressLineVOs = new ArrayList<InvLineBVO>();
					sameOperationLineVOs.put(pk_addressAndDate, sameAddressLineVOs);
				}
				sameAddressLineVOs.add(lineBVO);
			}
			//这里只会有两种操作方式，pickUp和delivery
			if(groupMap.get(OperateTypeConst.PICKUP) == null || groupMap.get(OperateTypeConst.DELIVERY) == null){
				throw new BusiException("多点运输单[?],提到货信息不完整，不能确认!");
			}
			if(groupMap.get(OperateTypeConst.PICKUP).size() > 1 && groupMap.get(OperateTypeConst.DELIVERY).size() >1){
				throw new BusiException("多点运输单[?],出现多点提送业务，此业务尚不支持!");
			}
			//一提多送业务
			List<HYBillVO> segs = new ArrayList<HYBillVO>();
			if(groupMap.get(OperateTypeConst.PICKUP).size() == 1){
				//提货点 直接用发货单上面的提货点来作为提货点就可以了。
				for(String pk_address : groupMap.get(OperateTypeConst.DELIVERY).keySet()){
					List<InvLineBVO> sameAddressLineVOs = groupMap.get(OperateTypeConst.DELIVERY).get(pk_address);
					HYBillVO seg = getSegsByLine(parentVO, sameAddressLineVOs, packBVOs, OperateTypeConst.PICKUP);
					if(seg != null){
						segs.add(seg);
					}
					
				}
			}
			//多提一送业务
			if(groupMap.get(OperateTypeConst.DELIVERY).size() == 1){
				for(String pk_address : groupMap.get(OperateTypeConst.PICKUP).keySet()){
					List<InvLineBVO> sameAddressLineVOs = groupMap.get(OperateTypeConst.PICKUP).get(pk_address);
					HYBillVO seg = getSegsByLine(parentVO, sameAddressLineVOs, packBVOs, OperateTypeConst.DELIVERY);
					if(seg != null){
						segs.add(seg);
					}
				}
			}
			if(segs != null && segs.size() > 0){
				for(HYBillVO seg : segs){
					dao.saveOrUpdate(seg);
				}
			}
		}else{
			//常规业务
			SegmentVO segVO = new SegmentVO();
			segVO.setDbilldate(new UFDate());
			segVO.setInvoice_vbillno(parentVO.getVbillno());
			segVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YDPZ));
			segVO.setPk_trans_type(parentVO.getPk_trans_type());// 运输方式，这个字段在计算总计费重时需要使用
			segVO.setVbillstatus(BillStatus.SEG_WPLAN); // 待计划
			segVO.setSeg_type(SegmentConst.SECTION); // 分段运段
			segVO.setSeg_mark(SegmentConst.SEG_MARK_NORMAL);// 运段标识
			segVO.setReq_deli_date(parentVO.getReq_deli_date());
			segVO.setReq_deli_time(parentVO.getReq_deli_time());
			segVO.setReq_arri_date(parentVO.getReq_arri_date());
			segVO.setReq_arri_time(parentVO.getReq_arri_time());
			segVO.setMileage(parentVO.getMileage());
			segVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			segVO.setCreate_time(new UFDateTime(new Date()));
			segVO.setPk_corp(parentVO.getPk_corp());
			segVO.setPk_delivery(parentVO.getPk_delivery());
			segVO.setDeli_city(parentVO.getDeli_city());
			segVO.setDeli_province(parentVO.getDeli_province());
			segVO.setDeli_area(parentVO.getDeli_area());
			segVO.setDeli_detail_addr(parentVO.getDeli_detail_addr());
			segVO.setDeli_contact(parentVO.getDeli_contact());
			segVO.setDeli_mobile(parentVO.getDeli_mobile());
			segVO.setDeli_phone(parentVO.getDeli_phone());
			segVO.setDeli_email(parentVO.getDeli_email());
			segVO.setPk_arrival(parentVO.getPk_arrival());
			segVO.setArri_city(parentVO.getArri_city());
			segVO.setArri_province(parentVO.getArri_province());
			segVO.setArri_area(parentVO.getArri_area());
			segVO.setArri_detail_addr(parentVO.getArri_detail_addr());
			segVO.setArri_contact(parentVO.getArri_contact());
			segVO.setArri_mobile(parentVO.getArri_mobile());
			segVO.setArri_phone(parentVO.getArri_phone());
			segVO.setArri_email(parentVO.getArri_email());
			segVO.setDeli_method(parentVO.getDeli_method());// 派送方式
			segVO.setDistance(parentVO.getDistance());
			segVO.setPack_num_count(parentVO.getPack_num_count());
			segVO.setNum_count(parentVO.getNum_count());
			segVO.setWeight_count(parentVO.getWeight_count());
			segVO.setVolume_count(parentVO.getVolume_count());
			segVO.setFee_weight_count(parentVO.getFee_weight_count());
			segVO.setVolume_weight_count(parentVO.getVolume_weight_count());
			segVO.setMemo(parentVO.getMemo());

			segVO.setDeli_process(parentVO.getDeli_process());
			segVO.setArri_process(parentVO.getArri_process());
			segVO.setNote(parentVO.getNote());
			segVO.setPz_line(parentVO.getPz_line());
			segVO.setPz_mileage(parentVO.getPz_mileage());
			segVO.setNote(parentVO.getNote());
			segVO.setUrgent_level(parentVO.getUrgent_level());
			
			segVO.setDef1(parentVO.getDef1());
			segVO.setDef2(parentVO.getDef2());
			segVO.setDef3(parentVO.getDef3());
			segVO.setDef4(parentVO.getDef4());
			segVO.setDef5(parentVO.getDef5());
			segVO.setDef6(parentVO.getDef6());
			segVO.setDef7(parentVO.getDef7());
			segVO.setDef8(parentVO.getDef8());
			segVO.setDef9(parentVO.getDef9());
			segVO.setDef10(parentVO.getDef10());
			segVO.setDef11(parentVO.getDef11());
			segVO.setDef12(parentVO.getDef12());
			

			segVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(segVO);
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]运段构建完成，运段号[" + segVO.getVbillno() + "]...");

			// 包装明细
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]将发货单的包装明细转换成运段的包装明细...");
			InvPackBVO[] invPackVOs = (InvPackBVO[]) ((ExAggInvoiceVO) billVO).getTableVO(TabcodeConst.TS_INV_PACK_B);//
			SegPackBVO[] segPackVOs = new SegPackBVO[invPackVOs.length];
			for (int i = 0; i < invPackVOs.length; i++) {
				SegPackBVO segPackVO = InvoiceUtils.convert(invPackVOs[i]);
				segPackVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(segPackVO);
				segPackVO.setPk_segment(segVO.getPk_segment());
				segPackVOs[i] = segPackVO;
				logger.info("[XXX发货单：" + parentVO.getVbillno() + "]转换第" + (i + 1) + "条包装明细，PK："
						+ segPackVO.getPk_seg_pack_b() + "...");
			}
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]将发货单的包装明细转换成运段的包装明细完成...");
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]开始自动分段...");
			// 自动分段
			stowageService.autoDistSection(new SegmentVO[] { segVO }, segPackVOs);
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]完成自动分段...");

			// 保存数据
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]开始保存运段，这里保存的是父级运段，运段号：" + segVO.getVbillno() + "...");
			HYBillVO hyBillVO = new HYBillVO();
			hyBillVO.setParentVO(segVO);
			hyBillVO.setChildrenVO(segPackVOs);
			logger.info("[XXX发货单：" + parentVO.getVbillno() + "]完成保存运段，这里保存的是父级运段，运段号：" + segVO.getVbillno() + "...");
			dao.saveOrUpdate(hyBillVO);
			logger.info("END----------------" + parentVO.getVbillno() + "------------------");
		}

	}

	/**
	 * 通过发货单和路线还有类型，生成运段。根据operation确定到底是提货还是到货
	 * @param invoiceVO
	 * @param lineBVOs
	 * @param packBVOs
	 * @param operation
	 * @param rate
	 * @return
	 */
	public HYBillVO getSegsByLine(InvoiceVO invoiceVO, List<InvLineBVO> lineBVOs,InvPackBVO[] packBVOs, Integer operation){
		if(lineBVOs == null || lineBVOs.size() == 0 || operation == null){
			return null;
		}
		HYBillVO hBillVO = new HYBillVO();
		SegmentVO segVO = new SegmentVO();
		segVO.setDbilldate(new UFDate());
		segVO.setInvoice_vbillno(invoiceVO.getVbillno());
		segVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YDPZ));
		segVO.setPk_trans_type(invoiceVO.getPk_trans_type());// 运输方式，这个字段在计算总计费重时需要使用
		segVO.setVbillstatus(BillStatus.SEG_WPLAN); // 待计划
		segVO.setSeg_type(SegmentConst.SECTION); // 分段运段
		segVO.setSeg_mark(SegmentConst.SEG_MARK_NORMAL);// 运段标识
		segVO.setMileage(invoiceVO.getMileage());
		segVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		segVO.setCreate_time(new UFDateTime(new Date()));
		segVO.setPk_corp(invoiceVO.getPk_corp());
		segVO.setDeli_method(invoiceVO.getDeli_method());// 派送方式
		segVO.setDistance(invoiceVO.getDistance());
		segVO.setMemo(invoiceVO.getMemo());
		segVO.setDeli_process(invoiceVO.getDeli_process());
		segVO.setArri_process(invoiceVO.getArri_process());
		segVO.setNote(invoiceVO.getNote());
		segVO.setPz_line(invoiceVO.getPz_line());
		segVO.setPz_mileage(invoiceVO.getPz_mileage());
		segVO.setNote(invoiceVO.getNote());
		segVO.setUrgent_level(invoiceVO.getUrgent_level());
		List<SegPackBVO> segPackVOs = new ArrayList<SegPackBVO>();
		int i = 0;
		//合并件重体
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		UFDouble pack_num_count = UFDouble.ZERO_DBL;
		for(InvLineBVO lineBVO : lineBVOs){
			if(StringUtils.isNotBlank(lineBVO.getPk_goods())
					|| StringUtils.isNotBlank(lineBVO.getGoods_code())
					|| StringUtils.isNotBlank(lineBVO.getGoods_name())){
				//有货品才生成明细
				SegPackBVO segPackVO = new SegPackBVO();
				segPackVO.setSerialno((i+1) * 10);
				segPackVO.setPk_invoice(lineBVO.getPk_invoice());
				if(packBVOs != null && packBVOs.length > 0){
					for(InvPackBVO packBVO : packBVOs){
						if(lineBVO.getPk_inv_line_b().equals(packBVO.getPk_inv_line_b())){
							segPackVO.setPk_inv_pack_b(packBVO.getPk_inv_pack_b());
							break;
						}
					}
				}
				segPackVO.setPk_goods(lineBVO.getPk_goods());
				segPackVO.setGoods_code(lineBVO.getGoods_code());
				segPackVO.setGoods_name(lineBVO.getGoods_name());
				segPackVO.setPlan_pack_num_count(lineBVO.getPlan_pack_num_count());// 计划数量
				segPackVO.setPack_num_count(lineBVO.getPack_num_count());// 数量
				pack_num_count = pack_num_count.add(lineBVO.getPack_num_count());
				segPackVO.setPlan_num(lineBVO.getPlan_num());// 计划件数
				segPackVO.setNum(lineBVO.getNum());// 件数
				num_count = num_count + (lineBVO.getNum() == null ? 0 : lineBVO.getNum());
				segPackVO.setPack(lineBVO.getPack());
				segPackVO.setWeight(lineBVO.getWeight());
				weight_count = weight_count.add(lineBVO.getWeight());
				segPackVO.setVolume(lineBVO.getVolume());
				volume_count = volume_count.add(lineBVO.getVolume());
				segPackVO.setUnit_weight(lineBVO.getUnit_weight());
				segPackVO.setUnit_volume(lineBVO.getUnit_volume());
				segPackVO.setLength(lineBVO.getLength());
				segPackVO.setWidth(lineBVO.getWidth());
				segPackVO.setHeight(lineBVO.getHeight());
				segPackVO.setTrans_note(lineBVO.getTrans_note());
				segPackVO.setLow_temp(lineBVO.getLow_temp());
				segPackVO.setHight_temp(lineBVO.getHight_temp());
				segPackVO.setReference_no(lineBVO.getReference_no());
				segPackVO.setMemo(lineBVO.getMemo());
				segPackVO.setStatus(VOStatus.NEW);
				segPackVOs.add(segPackVO);
				i++;
			}
			
		}
		
		if(operation == OperateTypeConst.PICKUP){
			segVO.setReq_deli_date(invoiceVO.getReq_deli_date());
			segVO.setReq_deli_time(invoiceVO.getReq_deli_time());
			segVO.setPk_delivery(invoiceVO.getPk_delivery());
			segVO.setDeli_city(invoiceVO.getDeli_city());
			segVO.setDeli_province(invoiceVO.getDeli_province());
			segVO.setDeli_area(invoiceVO.getDeli_area());
			segVO.setDeli_detail_addr(invoiceVO.getDeli_detail_addr());
			segVO.setDeli_contact(invoiceVO.getDeli_contact());
			segVO.setDeli_mobile(invoiceVO.getDeli_mobile());
			segVO.setDeli_phone(invoiceVO.getDeli_phone());
			segVO.setDeli_email(invoiceVO.getDeli_mobile());
			
			segVO.setReq_arri_date(lineBVOs.get(0).getReq_date_from().toString());
			segVO.setReq_arri_time(lineBVOs.get(0).getReq_date_from().toString());
			segVO.setPk_arrival(lineBVOs.get(0).getPk_address());
			segVO.setArri_city(lineBVOs.get(0).getPk_city());
			segVO.setArri_province(lineBVOs.get(0).getPk_province());
			segVO.setArri_area(lineBVOs.get(0).getPk_area());
			segVO.setArri_detail_addr(lineBVOs.get(0).getDetail_addr());
			segVO.setArri_contact(lineBVOs.get(0).getContact());
			segVO.setArri_mobile(lineBVOs.get(0).getMobile());
			segVO.setArri_phone(lineBVOs.get(0).getPhone());
			segVO.setArri_email(lineBVOs.get(0).getEmail());
		}else if(operation == OperateTypeConst.DELIVERY){
			segVO.setReq_deli_date(lineBVOs.get(0).getReq_date_from().toString());
			segVO.setReq_deli_time(lineBVOs.get(0).getReq_date_from().toString());
			segVO.setPk_delivery(lineBVOs.get(0).getPk_address());
			segVO.setDeli_city(lineBVOs.get(0).getPk_city());
			segVO.setDeli_province(lineBVOs.get(0).getPk_province());
			segVO.setDeli_area(lineBVOs.get(0).getPk_area());
			segVO.setDeli_detail_addr(lineBVOs.get(0).getDetail_addr());
			segVO.setDeli_contact(lineBVOs.get(0).getContact());
			segVO.setDeli_mobile(lineBVOs.get(0).getMobile());
			segVO.setDeli_phone(lineBVOs.get(0).getPhone());
			segVO.setDeli_email(lineBVOs.get(0).getEmail());
			
			segVO.setReq_arri_date(invoiceVO.getReq_arri_date());
			segVO.setReq_arri_time(invoiceVO.getReq_deli_time());
			segVO.setPk_arrival(invoiceVO.getPk_arrival());
			segVO.setArri_city(invoiceVO.getArri_city());
			segVO.setArri_province(invoiceVO.getArri_province());
			segVO.setArri_area(invoiceVO.getArri_area());
			segVO.setArri_detail_addr(invoiceVO.getArri_detail_addr());
			segVO.setArri_contact(invoiceVO.getArri_contact());
			segVO.setArri_mobile(invoiceVO.getArri_mobile());
			segVO.setArri_phone(invoiceVO.getArri_phone());
			segVO.setArri_email(invoiceVO.getArri_mobile());
			
		}
		

		UFDouble rate = custService.getFeeRate(invoiceVO.getPk_customer(), invoiceVO.getPk_trans_type(), segVO.getDeli_city(), segVO.getArri_city());
		
		segVO.setPack_num_count(pack_num_count);
		segVO.setNum_count(num_count);
		segVO.setWeight_count(weight_count);
		segVO.setVolume_count(volume_count);
		if(rate != null && rate.doubleValue() != 0){
			UFDouble  fee_weight_count = rate.multiply(volume_count);
			segVO.setVolume_weight_count(fee_weight_count);
			if(fee_weight_count.doubleValue() < weight_count.doubleValue()){
				segVO.setFee_weight_count(weight_count);
			}else{
				segVO.setFee_weight_count(fee_weight_count);
			}
		}else{
			segVO.setFee_weight_count(weight_count);
			segVO.setVolume_weight_count(volume_count);
		}

		segVO.setStatus(VOStatus.NEW);
		hBillVO.setParentVO(segVO);
		if(segPackVOs != null && segPackVOs.size() > 0){
			hBillVO.setChildrenVO(segPackVOs.toArray(new SegPackBVO[segPackVOs.size()]));
		}
		NWDao.setUuidPrimaryKey(hBillVO);
		return hBillVO;
	}
	
	@Override
	public void processBeforeBatchUnconfirm(ParamVO paramVO, SuperVO[] parentVOs) {
		List<String> invoice_vbillnos = new ArrayList<String>();
		List<String> pk_invoices = new ArrayList<String>();
		for (InvoiceVO parentVO : (InvoiceVO[]) parentVOs) {
			if (parentVO.getVbillstatus().intValue() != BillStatus.INV_CONFIRM) {
				throw new BusiException("必须是[确认]状态的单据才能反确认，单据号[?]！", parentVO.getVbillno());
			}
			invoice_vbillnos.add(parentVO.getVbillno());
			pk_invoices.add(parentVO.getPk_invoice());
		}
		SegmentVO[] segmentVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class,
				"invoice_vbillno in " + NWUtils.buildConditionString(invoice_vbillnos));
		// 对运段按照发货单号来分组
		Map<String, List<SegmentVO>> groupMap = new HashMap<String, List<SegmentVO>>();
		for (SegmentVO segVO : segmentVOs) {
			String key = segVO.getInvoice_vbillno();
			List<SegmentVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<SegmentVO>();
				groupMap.put(key, voList);
			}
			voList.add(segVO);
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (String key : groupMap.keySet()) {
			List<SegmentVO> voList = groupMap.get(key);
			if (voList.size() > 1) {
				//如果运段数量>1，有可能是多点提送的订单产生的，这时候，需要允许他反确认。
				for(SegmentVO segVO : voList){
					//有父辈运段，说明是分段或者分量的运段，这时候不允许删除
					if(StringUtils.isNotBlank(segVO.getParent_seg())){
						throw new BusiException("该发货单对应了多条运段，不能反确认，单据号[?]！", key);
					}
				}
			}
			for(SegmentVO segVO : voList){
				if (segVO.getVbillstatus().intValue() != BillStatus.SEG_WPLAN) {
					throw new BusiException("该发货单对应的运段已经配载过了,单据号[?]！", key);
				}
				segVO.setStatus(VOStatus.DELETED);
			}
			toBeUpdate.addAll(voList);
		}
		SegPackBVO[] segPackBVOs =  NWDao.getInstance().queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_invoice in "+ NWUtils.buildConditionString(pk_invoices));
		if(segPackBVOs != null && segPackBVOs.length > 0){
			for(SegPackBVO segPackBVO : segPackBVOs){
				segPackBVO.setStatus(VOStatus.DELETED);
				toBeUpdate.add(segPackBVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
	}

	public void processBeforeUnconfirm(AggregatedValueObject billVO, ParamVO paramVO) throws BusiException {
		super.processBeforeUnconfirm(billVO, paramVO);
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		if (parentVO.getVbillstatus().intValue() != BillStatus.INV_CONFIRM) {
			throw new BusiException("必须是[确认]状态的单据才能反确认，单据号[?]！", parentVO.getVbillno());
		}
		// 查询应收明细,type=0表示是从发货单生成的
		String sql = "select vbillstatus from ts_receive_detail WITH(NOLOCK)  where invoice_vbillno=? and rece_type=0 and isnull(dr,0)=0";
		Integer vbillstatus = dao.queryForObject(sql, Integer.class, parentVO.getVbillno());
		if (vbillstatus == null) {
			logger.info("发货单所对应的应收明细已经被删除,发货单号：" + parentVO.getVbillno());
		}
		// 查询运段，删除对应的运段
		SegmentVO[] children = dao.queryForSuperVOArrayByCondition(SegmentVO.class, "invoice_vbillno=?",
				parentVO.getVbillno());
		if (children != null && children.length > 0) {
			if (children.length > 1) {
				throw new BusiException("该发货单对应了多条运段，不能反确认，单据号[?]！", parentVO.getVbillno());
			}
			SegmentVO segmentVO = children[0];
			// 如果运段已经调度，那么不能进行反确认
			if (segmentVO.getVbillstatus().intValue() != BillStatus.SEG_WPLAN) {
				throw new BusiException("该发货单对应的运段已经配载过了！");
			}
			// 运段子表VO
			SegPackBVO[] segPackVOs = dao.queryForSuperVOArrayByCondition(SegPackBVO.class, "pk_segment=?",
					segmentVO.getPk_segment());
			dao.delete(segPackVOs);
			dao.delete(children);
		}
	}

	/**
	 * 删除发货单的操作，
	 */

	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys) {
		// yaojiie 2016 1 3 当删除发货单时，需要删除这个发货单下的所有子表
		List<SuperVO> allVOs = new ArrayList<SuperVO>();
		for (String primaryKey : primaryKeys) {
			List<SuperVO> VOs = new ArrayList<SuperVO>();
			InvoiceVO parentVO = getByPrimaryKey(InvoiceVO.class, primaryKey);
			if (parentVO.getVbillstatus().intValue() != BillStatus.NEW) {
				throw new BusiException("必须是[新建]状态的单据才能删除，单据号[?]！", parentVO.getVbillno());
			}
			// 删除应收明细
			ReceiveDetailVO[] children = dao.queryForSuperVOArrayByCondition(ReceiveDetailVO.class, "invoice_vbillno=?",
					parentVO.getVbillno());
			List<String> pk_receive_detail = new ArrayList<String>();
			for (ReceiveDetailVO detailVO : children) {

				if (detailVO.getVbillstatus() != BillStatus.NEW) {
					throw new BusiException("发货单所对应的应收明细必须是[新建]状态才能删除发货单！");
				}
				pk_receive_detail.add(detailVO.getPk_receive_detail());
			}
			if (pk_receive_detail.size() > 0) {
				// 删除应收明细子表
				ReceDetailBVO[] detailBVOs = dao.queryForSuperVOArrayByCondition(ReceDetailBVO.class,
						"pk_receive_detail in " + NWUtils
								.buildConditionString(pk_receive_detail.toArray(new String[pk_receive_detail.size()])));
				if (detailBVOs != null && detailBVOs.length > 0) {
					VOs.addAll(Arrays.asList(detailBVOs));
				}
			}
			VOs.addAll(Arrays.asList(children));
			VOs.add(parentVO);
			allVOs.addAll(VOs);
		}
		// 删除发货单
		String primaryCond = NWUtils.buildConditionString(primaryKeys);
		InvLineBVO[] invLineBVOs = dao.queryForSuperVOArrayByCondition(InvLineBVO.class,
				"pk_invoice in " + primaryCond);
		InvPackBVO[] invPackBVOs = dao.queryForSuperVOArrayByCondition(InvPackBVO.class,
				"pk_invoice in " + primaryCond);
		TransBilityBVO[] transBilityBVOs = dao.queryForSuperVOArrayByCondition(TransBilityBVO.class,
				"pk_invoice in " + primaryCond);
		allVOs.addAll(Arrays.asList(invLineBVOs));
		allVOs.addAll(Arrays.asList(invPackBVOs));
		allVOs.addAll(Arrays.asList(transBilityBVOs));
		dao.delete(allVOs);
		return primaryKeys.length;
	}

	@Override
	public SuperVO[] batchConfirm(ParamVO paramVO, String[] ids) {
		logger.info("执行单据批量确认动作！");
		List<SuperVO> parentVOs = new ArrayList<SuperVO>();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		InvoiceVO[] invoiceVOs = getByPrimaryKeys(InvoiceVO.class, ids);
		if (invoiceVOs == null || invoiceVOs.length == 0) {
			throw new BusiException("请选择单据！");
		}
		List<String> vbillnos = new ArrayList<String>();
		boolean haveArriPay = false;
		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (BillStatus.NEW != invoiceVO.getVbillstatus()) {
				throw new BusiException("只有[新建]状态的单据才能进行确认！");
			}
			if (invoiceVO.getBalatype() != null
					&& DataDictConst.BALATYPE.ARRI_PAY.intValue() == invoiceVO.getBalatype()) {
				haveArriPay = true;
				vbillnos.add(invoiceVO.getVbillno());
			}
		}
		if (haveArriPay) {
			ReceiveDetailVO[] receVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
					"invoice_vbillno in " + NWUtils.buildConditionString(vbillnos));
			for (ReceiveDetailVO receVO : receVOs) {
				if (receVO.getCost_amount() == null || receVO.getCost_amount().equals(UFDouble.ZERO_DBL)) {
					throw new BusiException("现金到付类发货单确认时，金额不能为0！<br/>单据号[?]！", receVO.getInvoice_vbillno());
				}
			}
		}
		// 重新获取发货单，因为存储过程会修改发货单
		InvoiceVO[] freshInvoiceVOs = getByPrimaryKeys(InvoiceVO.class, ids);
		for (InvoiceVO inv : freshInvoiceVOs) {
			String procMsg = processBeforeConfirmByProc(inv.getPk_invoice());
			if (StringUtils.isNotBlank(procMsg)) {
				throw new BusiException(procMsg);
			}

			paramVO.setBillId(inv.getPk_invoice());
			ExAggInvoiceVO aggVO = (ExAggInvoiceVO) queryBillVO(paramVO);
			aggVO.setParentVO(inv);

			processBeforeConfirm(aggVO, paramVO);

			// 重新查询
			inv = (InvoiceVO) aggVO.getParentVO();
			inv.setStatus(VOStatus.UPDATED);
			inv.setAttributeValue(getBillStatusField(), BillStatus.INV_CONFIRM); // 设置成已确认
			inv.setConfirm_date((new UFDateTime(new Date())).toString());
			inv.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
			InvTrackingVO itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.CONFIRM);
			itVO.setTracking_time(new UFDateTime(new Date()));
			itVO.setTracking_memo("确认");
			itVO.setInvoice_vbillno(inv.getVbillno());
			itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			itVO.setCreate_time(new UFDateTime(new Date()));
			itVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(itVO);
			parentVOs.add(inv);
			toBeUpdate.add(itVO);
			toBeUpdate.add(inv);

		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return parentVOs.toArray(new SuperVO[parentVOs.size()]);
	}

	public AggregatedValueObject confirm(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		if (parentVO.getVbillstatus().intValue() != BillStatus.NEW) {
			throw new BusiException("只有[新建]状态的单据才能进行确认！");
		}
		if (parentVO.getBalatype() != null && DataDictConst.BALATYPE.ARRI_PAY.intValue() == parentVO.getBalatype()) {
			ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) billVO;
			ReceDetailBVO[] rdBVOs = (ReceDetailBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
			UFDouble amount = UFDouble.ZERO_DBL;
			if (rdBVOs != null && rdBVOs.length > 0) {
				for (ReceDetailBVO rdBVO : rdBVOs) {
					amount = amount.add(rdBVO.getAmount());
				}
			}
			if (UFDouble.ZERO_DBL.equals(amount)) {
				throw new BusiException("现金到付类发货单确认时，金额不能为0！");
			}
		}
		// 这里调用存储过程进行复杂的判断
		String procMsg = processBeforeConfirmByProc(parentVO.getPk_invoice());
		if (StringUtils.isNotBlank(procMsg)) {
			throw new BusiException(procMsg);
		}
		billVO = queryBillVO(paramVO);
		logger.info("[XXX发货单：" + parentVO.getVbillno() + "]开始确认单据...");
		// processBeforeConfirmByProc会修改数据库，所以要重新获取billVO
		processBeforeConfirm(billVO, paramVO);
		parentVO = (InvoiceVO) billVO.getParentVO();
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.INV_CONFIRM); // 设置成已确认
		// 增加确认时间，确认人 2015-11-10 jonathan
		parentVO.setConfirm_date((new UFDateTime(new Date())).toString());
		parentVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());

		dao.saveOrUpdate(billVO);
		logger.info("[XXX发货单：" + parentVO.getVbillno() + "]完成确认单据...");
		// 确认时，向发货单的跟踪信息表插入一条记录
		InvTrackingVO itVO = new InvTrackingVO();
		itVO.setTracking_status(TrackingConst.CONFIRM);
		itVO.setTracking_time(new UFDateTime(new Date()));
		itVO.setTracking_memo("确认");
		itVO.setInvoice_vbillno(parentVO.getVbillno());
		itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		NWDao.getInstance().saveOrUpdate(itVO);
		return billVO;
	}

	@Override
	public SuperVO[] batchUnconfirm(ParamVO paramVO, String[] ids) {
		InvoiceVO[] invoiceVOs = getByPrimaryKeys(InvoiceVO.class, ids);
		List<SuperVO> parentVOs = new ArrayList<SuperVO>();
		processBeforeBatchUnconfirm(paramVO, invoiceVOs);
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<String> invoice_vbillnos = new ArrayList<String>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoiceVO.setStatus(VOStatus.UPDATED);
			invoiceVO.setVbillstatus(BillStatus.NEW);
			invoiceVO.setUnconfirm_date((new UFDateTime(new Date())).toString());
			invoiceVO.setUnconfirm_user(WebUtils.getLoginInfo().getPk_user());
			invoiceVO.setUnconfirm_type(paramVO.getUnconfirmType());
			invoiceVO.setUnconfirm_memo(paramVO.getUnconfirmMemo());
			toBeUpdate.add(invoiceVO);
			parentVOs.add(invoiceVO);
			invoice_vbillnos.add(invoiceVO.getVbillno());
		}
		InvTrackingVO[] invTrackingVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvTrackingVO.class,
				"tracking_status=? and invoice_vbillno in " + NWUtils.buildConditionString(invoice_vbillnos),
				TrackingConst.CONFIRM);
		for (InvTrackingVO irackingVO : invTrackingVOs) {
			irackingVO.setStatus(VOStatus.DELETED);
			toBeUpdate.add(irackingVO);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return parentVOs.toArray(new SuperVO[parentVOs.size()]);
	}

	public AggregatedValueObject unconfirm(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeUnconfirm(billVO, paramVO);
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.NEW); // 设置成新建
		parentVO.setAttributeValue(getUnConfirmTypeField(), paramVO.getUnconfirmType());// 反确认类型
		parentVO.setAttributeValue(getUnConfirmMemoField(), paramVO.getUnconfirmMemo());// 反确认说明
		// 增加确认时间，确认人 2015-11-10 jonathan
		parentVO.setUnconfirm_date((new UFDateTime(new Date())).toString());
		parentVO.setUnconfirm_user(WebUtils.getLoginInfo().getPk_user());

		InvTrackingVO invTrackingVO = dao.queryByCondition(InvTrackingVO.class,
				"invoice_vbillno=? and tracking_status=?", parentVO.getVbillno(), TrackingConst.CONFIRM);
		if (invTrackingVO != null) {
			dao.delete(invTrackingVO);
		}
		dao.saveOrUpdate(billVO);
		return billVO;
	}

	public String getBodyPrintTableCode() {
		return TabcodeConst.TS_INV_PACK_B;
	}

	public List<Map<String, Object>> execFormula4Templet(ReceDetailBVO[] detailBVOs) {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(detailBVOs.length);
		for (SuperVO vo : detailBVOs) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for (String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.INVOICE_CODE);
		paramVO.setBody(true);
		paramVO.setTabCode(TabcodeConst.TS_RECE_DETAIL_B);
		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(this.getBillTemplateID(paramVO));
		return this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(), paramVO.getTabCode().split(","),
				null);
	}

	
	public List<Map<String, Object>> refreshReceDetail(String pk_invoice, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, String bala_customer,
			String pk_trans_type, String start_addr, String end_addr, String start_city, String end_city,
			String[] pk_car_type, String pk_corp, String req_arri_date, Integer urgent_level, String item_code,
			String pk_trans_line, UFBoolean if_return) {
		// 1、根据pk_invoice查询当前已经存在的费用明细
		List<Map<String, Object>> oldReceDetailBVOs = new ArrayList<Map<String, Object>>();
		if (StringUtils.isNotBlank(pk_invoice)) {
			InvoiceVO invoiceVO = this.getByPrimaryKey(InvoiceVO.class, pk_invoice);
			if (invoiceVO != null) {
				ReceDetailBVO[] detailBVOs = getReceDetailBVOsByInvoiceBillno(invoiceVO.getVbillno());
				if (detailBVOs != null && detailBVOs.length > 0) {
					// 执行公式
					oldReceDetailBVOs = execFormula4Templet(detailBVOs);
				}
			}
		}
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, bala_customer,
				pk_trans_type, start_addr, end_addr, start_city, end_city, pk_corp, req_arri_date, urgent_level,
				item_code, pk_trans_line, if_return);
		if (contractBVOs == null || contractBVOs.size() == 0) {
			// 所有情况都匹配不到，崩溃了
			return oldReceDetailBVOs;
		}
		InvPackBVO[] invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?",
				pk_invoice);
		List<PackInfo> packInfos = new ArrayList<PackInfo>();
		Map<String, List<InvPackBVO>> groupMap = new HashMap<String, List<InvPackBVO>>();
		// 对包装按照pack进行分组
		for (InvPackBVO invPackBVO : invPackBVOs) {
			String key = invPackBVO.getPack();
			if (StringUtils.isBlank(key)) {
				// 没有包装的货品自动过滤
				continue;
			}
			List<InvPackBVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<InvPackBVO>();
				groupMap.put(key, voList);
			}
			voList.add(invPackBVO);
		}
		if (groupMap.size() > 0) {
			for (String key : groupMap.keySet()) {
				PackInfo packInfo = new PackInfo();
				List<InvPackBVO> voList = groupMap.get(key);
				Integer num = 0;
				UFDouble weight = UFDouble.ZERO_DBL;
				UFDouble volume = UFDouble.ZERO_DBL;
				for (InvPackBVO packBVO : voList) {
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
		List<ReceDetailBVO> detailBVOs = contractService.buildReceDetailBVO(bala_customer, pack_num_count, num_count,
				fee_weight_count, weight_count, volume_count, packInfos, pk_car_type, pk_corp, contractBVOs);
		if (detailBVOs == null) {
			detailBVOs = new ArrayList<ReceDetailBVO>();
		}
		// 执行公式
		List<Map<String, Object>> newReceDetailBVOs = execFormula4Templet(
				detailBVOs.toArray(new ReceDetailBVO[detailBVOs.size()]));

		// 3、将旧的费用明细中系统创建标示为Y的记录，与新的费用明细进行匹配(根据费用类型进行匹配，费用类型是唯一的)，
		// 如果不存在于新的费用明细中,说明是之前匹配的，删除该记录
		for (Map<String, Object> oldReceDetailBVO : oldReceDetailBVOs) {
			if (oldReceDetailBVO.get(ReceDetailBVO.SYSTEM_CREATE) != null
					&& oldReceDetailBVO.get(ReceDetailBVO.SYSTEM_CREATE).toString().equalsIgnoreCase(Constants.Y)) {
				// 属于系统创建的记录，即从合同匹配而来的记录
				boolean exist = false;
				String pk_expense_type = oldReceDetailBVO.get(ReceDetailBVO.PK_EXPENSE_TYPE).toString();
				if (newReceDetailBVOs != null) {
					for (Map<String, Object> newReceDetail : newReceDetailBVOs) {
						if (pk_expense_type.equals(newReceDetail.get(ReceDetailBVO.PK_EXPENSE_TYPE).toString())) {
							exist = true;
						}
					}
				}
				if (!exist) {
					// 如果不存在，那么移除，会使用新的匹配记录代替
					oldReceDetailBVOs.remove(oldReceDetailBVO);
				}
			}
		}
		// 4、将新的费用明细与旧的费用明细中为系统创建的进行匹配(根据费用类型进行匹配，费用类型是唯一的)，
		// 如果不存在于旧的费用明细中，那么添加该记录
		if (newReceDetailBVOs != null) {
			for (Map<String, Object> newReceDetail : newReceDetailBVOs) {
				String pk_expense_type = newReceDetail.get(ReceDetailBVO.PK_EXPENSE_TYPE).toString();
				boolean exist = false;
				for (Map<String, Object> oldReceDetail : oldReceDetailBVOs) {
					if (pk_expense_type.equals(oldReceDetail.get(ReceDetailBVO.PK_EXPENSE_TYPE).toString())) {
						// 这里不需要再根据是否系统创建来判断了，因为费用类型是唯一的
						exist = true;
					}
				}
				if (!exist) {
					oldReceDetailBVOs.add(newReceDetail);
				}
			}
		}
		return oldReceDetailBVOs;
	}

	public PaginationVO loadReceDetail(String pk_invoice, ParamVO paramVO, int offset, int pageSize) {
		InvoiceVO invoiceVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk_invoice);
		ReceDetailBVO[] detailBVOs = getReceDetailBVOsByInvoiceBillno(invoiceVO.getVbillno());
		List<Map<String, Object>> retList = execFormula4Templet(detailBVOs);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(retList);
		return paginationVO;
	}

	public List<InvoiceVO> getTodayTop5() {
		String corpCond = CorpHelper.getCurrentCorpWithChildren("ts_invoice");
		String sql = "";
		if (DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())) {
			sql = "select * from ts_invoice where rownun<6 and isnull(dr,0)=0 and vbillstatus=? and " + corpCond
					+ " order by create_time desc";
		} else if (DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())) {
			sql = "select top 5 * from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and vbillstatus=? and " + corpCond
					+ " order by create_time desc";
		}
		return NWDao.getInstance().queryForList(sql, InvoiceVO.class, BillStatus.NEW);
	}


	// public String[] getExportAry(ParamVO paramVO) {
	// // 集货信息导出字段
	// return new String[] { "vbillno", "orderno", "req_deli_date",
	// "req_arri_date", "cust_orderno", "pk_psndoc",
	// "pk_customer", "goods_code", "ts_inv_pack_b_plan_num",
	// "ts_inv_pack_b_plan_pack_num_count",
	// "ts_inv_pack_b_num", "ts_inv_pack_b_pack_num_count",
	// "ts_inv_pack_b_unit_weight",
	// "ts_inv_pack_b_unit_volume", "deli_detail_addr", "memo", "deli_city",
	// "pk_arrival" };
	// }
	//
	// public String[] getExportTitleAry(ParamVO paramVO) {
	// // 集货信息导出字段
	// return new String[] { "发货单", "波次号", "预计提货日期", "送货日期", "送货单号", "筹措员",
	// "发货人", "零件号", "计划托盘数", "计划零件数", "实际托盘数",
	// "实际零件数", "重量", "体积", "提货地址", "备注", "始发地", "目的城市" };
	// }
	//
	// /**
	// * 集货信息导出时需要执行的公式
	// *
	// * @param paramVO
	// * @return
	// */
	// private String[] getExportFormulaAry(ParamVO paramVO) {
	// return new String[] {
	// "pk_psndoc->getcolvalue(nw_psndoc,psnname,pk_psndoc,pk_psndoc)",
	// "pk_customer->getcolvalue(ts_customer,cust_name,pk_customer,pk_customer)",
	// "deli_city->getcolvalue(ts_area,name,pk_area,deli_city)",
	// "pk_arrival->getcolvalue(ts_address,addr_name,pk_address,pk_arrival)" };
	// }
	//
	// @SuppressWarnings("unchecked")
	// public HSSFWorkbook export(ParamVO paramVO, int offset, int pageSize,
	// String orderBy, String extendCond,
	// Object... values) {
	// logger.info("执行导出动作...");
	// Calendar start = Calendar.getInstance();
	//
	// StringBuilder sbSQL = new StringBuilder();
	// sbSQL.append(
	// "select
	// vbillno,orderno,req_deli_date,req_arri_date,cust_orderno,pk_psndoc,pk_customer,goods_code,"
	// + "ts_inv_pack_b.plan_num as
	// ts_inv_pack_b_plan_num,ts_inv_pack_b.plan_pack_num_count as
	// ts_inv_pack_b_plan_pack_num_count,"
	// + "ts_inv_pack_b.num as ts_inv_pack_b_num,ts_inv_pack_b.pack_num_count as
	// ts_inv_pack_b_pack_num_count,"
	// + "ts_inv_pack_b.unit_weight as
	// ts_inv_pack_b_unit_weight,ts_inv_pack_b.unit_volume as
	// ts_inv_pack_b_unit_volume,"
	// + "ts_invoice.deli_detail_addr,ts_invoice.memo,deli_city,pk_arrival");
	// sbSQL.append(" from ts_invoice WITH(NOLOCK) inner join ts_inv_pack_b
	// WITH(NOLOCK) on ts_invoice.pk_invoice=ts_inv_pack_b.pk_invoice "
	// + "where isnull(ts_invoice.dr,0)=0 and isnull(ts_inv_pack_b.dr,0)=0 ");
	// if (StringUtils.isNotBlank(extendCond)) {
	// sbSQL.append(" and ");
	// sbSQL.append(extendCond);
	// }
	// String params =
	// ServletContextHolder.getRequest().getParameter(Constants.PUB_QUERY_PARAMETER);
	// /**
	// * 1.首先获取对应的superVO
	// */
	// UiQueryTempletVO queryTempletVO =
	// this.getQueryTempletVO(this.getQueryTemplateID(paramVO));
	// StringBuilder where = new StringBuilder("1=1");
	// String dataWhere = null;
	// dataWhere = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
	// if (StringUtils.isNotBlank(dataWhere)) {
	// where.append(" and ");
	// where.append(dataWhere);
	// }
	//
	// /**
	// * 查询模板的相关信息
	// */
	// if (StringUtils.isBlank(params)) {
	// // 没有经过查询窗口的查询
	// String defaultCond = QueryHelper.getDefaultCond(queryTempletVO);
	// if (StringUtils.isNotBlank(defaultCond)) {
	// where.append(" and ");
	// where.append(defaultCond);
	// }
	// }
	// // 固定条件是必须加上去的，不管前台有没有传入，就算传入了，相同的条件也没关系
	// String immobilityCond = QueryHelper.getImmobilityCond(queryTempletVO);
	// if (StringUtils.isNotBlank(immobilityCond)) {
	// where.append(" and ");
	// where.append(immobilityCond);
	// }
	// sbSQL.append(" and ").append(where.toString());
	//
	// NWDao dao = NWDao.getInstance();
	// PaginationVO paginationVO = dao.queryBySqlWithPaging(sbSQL.toString(),
	// HashMap.class, offset, pageSize, values);
	// List<?> list = paginationVO.getItems();
	// List<Map<String, Object>> context = (List<Map<String, Object>>) list;
	// List<Map<String, Object>> retList = FormulaHelper.execFormula(context,
	// getExportFormulaAry(paramVO), true);
	//
	// POI excel = new POI();
	// if (StringUtils.isNotBlank(getFirstSheetName())) {
	// excel.setFirstSheetName(getFirstSheetName());
	// }
	// HSSFWorkbook wb = excel.buildExcel(getExportAry(paramVO),
	// getExportTitleAry(paramVO), retList);
	// logger.info("导出动作结束，耗时：" + (Calendar.getInstance().getTimeInMillis() -
	// start.getTimeInMillis()) + "毫秒。");
	// return wb;
	// }

	public Map<String, Object> getBillPrintParameterMap(HttpServletRequest request, ParamVO paramVO) {
		Map<String, Object> paramMap = super.getBillPrintParameterMap(request, paramVO);
		// 打钩图片
		String path = ImageResource.getRootPath();
		File file = new File(path + File.separator + "yes.png");
		if (!file.exists()) {
			logger.warn("路径【" + path + "】下不存在yes.png图片！");
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (Exception e) {

		}
		if (fis != null) {
			// 结算方式的图片
			// if(invVO.getBalatype() != null) {
			// String key = "balatype_" + invVO.getBalatype();
			paramMap.put("_balatype", fis);
			// }
		}
		return paramMap;
	}

	// jonathan 2015-11-28
	public List<InvoiceVO> autoDistInvoice(InvoiceVO invoiceVO, List<Map<String, List<AddressVO>>> linenodes) {
		List<InvoiceVO> invoiceVOs = new ArrayList<InvoiceVO>();
		if (invoiceVO == null) {
			logger.info("没有发货单信息，不需要自动拆分...");
			invoiceVOs.add(invoiceVO);
			return invoiceVOs;
		}
		if (linenodes == null || linenodes.size() == 0) {
			logger.info("没有路线信息，不需要自动拆分...");
			invoiceVOs.add(invoiceVO);
			return invoiceVOs;
		}
		List<AddressVO> addrVOs = new ArrayList<AddressVO>();
		String keyChar = "|";
		String key = new String();
		String key1 = new String();
		String key2 = new String();
		// 地址到地址匹配
		// 通用的Key
		key = new StringBuffer().append(invoiceVO.getPk_trans_type()).append(keyChar).append(invoiceVO.getPk_delivery())
				.append(keyChar).append(TransLineConst.ADDR).append(keyChar).append(invoiceVO.getPk_arrival())
				.append(keyChar).append(TransLineConst.ADDR).toString();
		// 当前公司的Key
		key1 = new StringBuffer().append(key).append(keyChar).append(invoiceVO.getPk_corp()).toString();
		// 集团对应的Key
		key2 = new StringBuffer().append(key).append(keyChar).append(Constants.SYSTEM_CODE).toString();
		for (Map<String, List<AddressVO>> linenode : linenodes) {
			// 先从公司中匹配线路
			if (linenode.containsKey(key1)) {
				addrVOs.addAll(linenode.get(key1));
				break;
			}
			// 再从集团中中匹配线路
			if (linenode.containsKey(key2)) {
				addrVOs.addAll(linenode.get(key2));
				break;
			}
		}
		if (addrVOs.size() == 0) {
			// 城市到城市匹配
			key = new StringBuffer().append(invoiceVO.getPk_trans_type()).append(keyChar)
					.append(invoiceVO.getDeli_city()).append(keyChar).append(TransLineConst.CITY).append(keyChar)
					.append(invoiceVO.getArri_city()).append(keyChar).append(TransLineConst.CITY).toString();
			// 当前公司的Key
			key1 = new StringBuffer().append(key).append(keyChar).append(invoiceVO.getPk_corp()).toString();
			// 集团对应的Key
			key2 = new StringBuffer().append(key).append(keyChar).append(Constants.SYSTEM_CODE).toString();
			for (Map<String, List<AddressVO>> linenode : linenodes) {
				// 先从公司中匹配线路
				if (linenode.containsKey(key1)) {
					addrVOs.addAll(linenode.get(key1));
					break;
				}
				// 再从集团中中匹配线路
				if (linenode.containsKey(key2)) {
					addrVOs.addAll(linenode.get(key2));
					break;
				}
			}
		}
		if (addrVOs.size() == 0) {
			// 地址到城市匹配
			key = new StringBuffer().append(invoiceVO.getPk_trans_type()).append(keyChar)
					.append(invoiceVO.getPk_delivery()).append(keyChar).append(TransLineConst.ADDR).append(keyChar)
					.append(invoiceVO.getArri_city()).append(keyChar).append(TransLineConst.CITY).toString();
			// 当前公司的Key
			key1 = new StringBuffer().append(key).append(keyChar).append(invoiceVO.getPk_corp()).toString();
			// 集团对应的Key
			key2 = new StringBuffer().append(key).append(keyChar).append(Constants.SYSTEM_CODE).toString();
			for (Map<String, List<AddressVO>> linenode : linenodes) {
				// 先从公司中匹配线路
				if (linenode.containsKey(key1)) {
					addrVOs.addAll(linenode.get(key1));
					break;
				}
				// 再从集团中中匹配线路
				if (linenode.containsKey(key2)) {
					addrVOs.addAll(linenode.get(key2));
					break;
				}
			}
		}
		if (addrVOs.size() == 0) {
			// 城市到地址匹配
			key = new StringBuffer().append(invoiceVO.getPk_trans_type()).append(keyChar)
					.append(invoiceVO.getDeli_city()).append(keyChar).append(TransLineConst.CITY).append(keyChar)
					.append(invoiceVO.getPk_arrival()).append(keyChar).append(TransLineConst.ADDR).toString();
			// 当前公司的Key
			key1 = new StringBuffer().append(key).append(keyChar).append(invoiceVO.getPk_corp()).toString();
			// 集团对应的Key
			key2 = new StringBuffer().append(key).append(keyChar).append(Constants.SYSTEM_CODE).toString();
			for (Map<String, List<AddressVO>> linenode : linenodes) {
				// 先从公司中匹配线路
				if (linenode.containsKey(key1)) {
					addrVOs.addAll(linenode.get(key1));
					break;
				}
				// 再从集团中中匹配线路
				if (linenode.containsKey(key2)) {
					addrVOs.addAll(linenode.get(key2));
					break;
				}
			}
		}
		if (addrVOs.size() == 0) {
			invoiceVOs.add(invoiceVO);
			return invoiceVOs;
		} else {
			// 根据标准时效算出要求提货时间及要求到货时间
			List<String> req_deli_date = new ArrayList<String>(); // 提货时间
			List<String> req_arri_date = new ArrayList<String>(); // 到货时间
			for (int i = 0; i < addrVOs.size(); i++) {
				AddressVO addrVO = addrVOs.get(i);
				// 到达时间
				String beginDate = null;
				if (req_deli_date.size() == 0) {
					beginDate = invoiceVO.getReq_deli_date() == null ? null : invoiceVO.getReq_deli_date().toString();
				} else {
					beginDate = req_deli_date.get(i - 1);
				}
				if (beginDate != null) {
					Date date = DateUtils.parseString(beginDate);
					if (addrVO.getS_timeline() != null) {
						date = DateUtils.addHour(DateUtils.parseString(beginDate),
								addrVO.getS_timeline().doubleValue());
					}
					String sDate = DateUtils.formatDate(date, DateUtils.DATETIME_FORMAT_HORIZONTAL);
					// 这里到货日期就是下一个节点的提货日期
					req_arri_date.add(sDate);//
					req_deli_date.add(sDate);//
				}
			}
			invoiceVOs = distInvoice(addrVOs, req_deli_date, req_arri_date, invoiceVO);
			return invoiceVOs;
		}
	}

	/**
	 * 执行发货单拆分操作，独立出来，可以复用 jonathan 2015-11-28
	 * 
	 * @param addrVOs
	 *            这里的addrVOs已经包含原
	 * @param invoiceVOs
	 * @return
	 */
	public List<InvoiceVO> distInvoice(List<AddressVO> addrVOs, List<String> req_deli_date, List<String> req_arri_date,
			InvoiceVO invoiceVO) {
		List<InvoiceVO> newInvoiceVOs = new ArrayList<InvoiceVO>();
		if (invoiceVO == null || addrVOs == null || addrVOs.size() == 0 || req_deli_date == null
				|| req_deli_date.size() == 0 || req_arri_date == null || req_arri_date.size() == 0) {
			// 不需要拆分
			logger.info("[XXX]信息不完整，不需要拆分...");
			newInvoiceVOs.add(invoiceVO);
			return newInvoiceVOs;
		}
		List<AddressVO> newAddrVOs = new ArrayList<AddressVO>();
		newAddrVOs.addAll(addrVOs);
		// 将原发货单的提货方和收货方加入这个addrVOs中，这样方便进行算法计算
		AddressVO deliAddrVO = new AddressVO();
		deliAddrVO.setPk_address(invoiceVO.getPk_delivery());
		deliAddrVO.setPk_city(invoiceVO.getDeli_city());
		deliAddrVO.setPk_province(invoiceVO.getDeli_province());
		deliAddrVO.setPk_area(invoiceVO.getDeli_area());
		deliAddrVO.setDetail_addr(invoiceVO.getDeli_detail_addr());
		deliAddrVO.setContact(invoiceVO.getDeli_contact());
		deliAddrVO.setEmail(invoiceVO.getDeli_email());
		deliAddrVO.setMobile(invoiceVO.getDeli_mobile());
		deliAddrVO.setPhone(invoiceVO.getDeli_phone());
		newAddrVOs.add(0, deliAddrVO);

		AddressVO arriAddrVO = new AddressVO();
		arriAddrVO.setPk_address(invoiceVO.getPk_arrival());
		arriAddrVO.setPk_city(invoiceVO.getArri_city());
		arriAddrVO.setPk_province(invoiceVO.getArri_province());
		arriAddrVO.setPk_area(invoiceVO.getArri_area());
		arriAddrVO.setDetail_addr(invoiceVO.getArri_detail_addr());
		arriAddrVO.setContact(invoiceVO.getArri_contact());
		arriAddrVO.setEmail(invoiceVO.getArri_email());
		arriAddrVO.setMobile(invoiceVO.getArri_mobile());
		arriAddrVO.setPhone(invoiceVO.getArri_phone());
		newAddrVOs.add(arriAddrVO);

		// 提货日期和到货日期增加原发货单的提货日期和到货日期
		// TODO 这里要考虑时间的情况
		if (invoiceVO.getReq_deli_date() != null) {
			req_deli_date.add(0, invoiceVO.getReq_deli_date());
		} else {
			req_deli_date.add(0, null);
		}
		if (invoiceVO.getReq_arri_date() != null) {
			req_arri_date.add(invoiceVO.getReq_arri_date());
		} else {
			req_arri_date.add(null);
		}
		for (int j = 0; j < newAddrVOs.size() - 1; j++) {
			AddressVO addrVO = newAddrVOs.get(j);
			AddressVO nextAddrVO = newAddrVOs.get(j + 1);
			InvoiceVO newInvoiceVO = (InvoiceVO) invoiceVO.clone();// 新的发货单
			// 设置提货方，收货方，提货日期，收货日期
			newInvoiceVO.setPk_delivery(addrVO.getPk_address());
			newInvoiceVO.setDeli_city(addrVO.getPk_city());
			newInvoiceVO.setDeli_province(addrVO.getPk_province());
			newInvoiceVO.setDeli_area(addrVO.getPk_area());
			newInvoiceVO.setDeli_detail_addr(addrVO.getDetail_addr());
			newInvoiceVO.setDeli_contact(addrVO.getContact());
			newInvoiceVO.setDeli_email(addrVO.getEmail());
			newInvoiceVO.setDeli_mobile(addrVO.getMobile());
			newInvoiceVO.setDeli_phone(addrVO.getPhone());

			newInvoiceVO.setPk_arrival(nextAddrVO.getPk_address());
			newInvoiceVO.setArri_city(nextAddrVO.getPk_city());
			newInvoiceVO.setArri_province(nextAddrVO.getPk_province());
			newInvoiceVO.setArri_area(nextAddrVO.getPk_area());
			newInvoiceVO.setArri_detail_addr(nextAddrVO.getDetail_addr());
			newInvoiceVO.setArri_contact(nextAddrVO.getContact());
			newInvoiceVO.setArri_email(nextAddrVO.getEmail());
			newInvoiceVO.setArri_mobile(nextAddrVO.getMobile());
			newInvoiceVO.setArri_phone(nextAddrVO.getPhone());
			// 设置提货日期、到货日期
			newInvoiceVO.setReq_deli_date(req_deli_date.get(j));
			newInvoiceVO.setReq_arri_date(req_arri_date.get(j));
			newInvoiceVOs.add(newInvoiceVO);
		}
		return newInvoiceVOs;
	}


	// yaojiie 2015 11 23添加接口方法，milkrun一键生成方法。
	public void milkRun(ParamVO paramVO, String[] pk_invoices) {
		// 第一步，去数据库获取发货单，检查发货单运输方式和单据状态
		if (pk_invoices == null || pk_invoices.length == 0) {
			throw new BusiException("请先选择发货单！");
		}

		// 查询运输方式为milkRun的PK值，用来检查运输方式
		String sql = "select * from ts_trans_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?";
		TransTypeVO transTypeVO = dao.queryForObject(sql, TransTypeVO.class, TransTypeConst.TT_MR);
		String cond = NWUtils.buildConditionString(pk_invoices);
		InvoiceVO[] invoiceVOs = dao.queryForSuperVOArrayByCondition(InvoiceVO.class, " pk_invoice in " + cond);
		List<String> invoice_vbillnos = new ArrayList<String>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (!invoiceVO.getPk_trans_type().equals(transTypeVO.getPk_trans_type())) {
				// 不再根据运输类型判断是否可以MilkRun，而是根据规则判断
				if (!ParameterHelper.getMilkRunLimit()) {
					throw new BusiException("发货单[?]运输方式不是MilkRun，如果需要进行MilkRun操作，请修改系统规则！", invoiceVO.getVbillno());
				}
			}
			if (invoiceVO.getVbillstatus() != BillStatus.INV_CONFIRM) {
				throw new BusiException("发货单[?]只有是[确认]状态的发货单才允许进行MilkRun操作 ，请检查数据！", invoiceVO.getVbillno());
			}
			if (StringUtils.isBlank(invoiceVO.getPk_carrier())) {
				throw new BusiException("发货单[?]必须要有承运商才允许进行MilkRun操作 ，请检查数据！", invoiceVO.getVbillno());
			}
			invoice_vbillnos.add(invoiceVO.getVbillno());
		}
		String vbillnos_cond = NWUtils
				.buildConditionString(invoice_vbillnos.toArray(new String[invoice_vbillnos.size()]));

		TransBilityBVO[] transBilityBVOs = dao.queryForSuperVOArrayByCondition(TransBilityBVO.class,
				" pk_invoice in " + cond);
		InvLineBVO[] allInvLineBVOs = dao.queryForSuperVOArrayByCondition(InvLineBVO.class, " pk_invoice in " + cond);
		// 对invLineBVOs用pk_invoice分组
		Map<String, List<InvLineBVO>> groupMap = new HashMap<String, List<InvLineBVO>>();
		for (InvLineBVO invLineBVO : allInvLineBVOs) {
			String key = new StringBuffer().append(invLineBVO.getPk_invoice()).toString();
			List<InvLineBVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<InvLineBVO>();
				groupMap.put(key, voList);
			}
			voList.add(invLineBVO);
		}

		// 第二步，用发货单大单据号，去运段表中获取相应的运段数据
		SegmentVO[] segmentVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class,
				" invoice_vbillno in " + vbillnos_cond);
		// 第三部，仿照批量配载的操作方式，处理数据
		// 1、组装数据
		// 在发货单确认后，数据将会推送到调度配载 表头信息推送到 ts_segment 包装明细推送到ts_seg_pack_b
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
		for (InvoiceVO invoiceVO : invoiceVOs) {
			PZHeaderVO pZHeaderVO = new PZHeaderVO();
			EntTransbilityBVO entTransbilityVO = new EntTransbilityBVO();
			for (TransBilityBVO transBilityBVO : transBilityBVOs) {
				if (transBilityBVO.getPk_invoice().equals(invoiceVO.getPk_invoice())) {
					// 对于每个milkRun的运力信息来说，只会有一个运力信息
					pZHeaderVO.setCarno(transBilityBVO.getCarno());
					pZHeaderVO.setPk_car_type(transBilityBVO.getPk_car_type());
					pZHeaderVO.setPk_driver(transBilityBVO.getPk_driver());

					entTransbilityVO.setCarno(transBilityBVO.getCarno());
					// entTransbilityVO.setContainer_no(transBilityBVO.getContainer_no());
					entTransbilityVO.setForecast_deli_date(new UFDateTime(invoiceVO.getReq_deli_date()));
					entTransbilityVO.setMemo(transBilityBVO.getMemo());
					entTransbilityVO.setNum(transBilityBVO.getNum());
					entTransbilityVO.setPk_car_type(transBilityBVO.getPk_car_type());
					entTransbilityVO.setPk_driver(transBilityBVO.getPk_driver());
					// 这里取不到Gps_id
					// pZHeaderVO.setGps_id(transBilityBVO.getGps_id);
					break;
				}
			}
			pZHeaderVO.setUrgent_level(invoiceVO.getUrgent_level());
			pZHeaderVO.setItem_code(invoiceVO.getItem_code());
			pZHeaderVO.setPk_trans_line(invoiceVO.getPk_trans_line());
			pZHeaderVO.setIf_return(invoiceVO.getIf_return());
			pZHeaderVO.setPk_trans_type(invoiceVO.getPk_trans_type());
			pZHeaderVO.setPk_carrier(invoiceVO.getPk_carrier());
			pZHeaderVO.setMemo(invoiceVO.getMemo());
			// FIXME
			pZHeaderVO.setStatus(2);
			for (SegmentVO segmentVO : segmentVOs) {
				if (segmentVO.getInvoice_vbillno().equals(invoiceVO.getVbillno())) {
					if (segmentVO.getVbillstatus() != BillStatus.SEG_WPLAN) {
						throw new BusiException("发货单[?]的运段状态 不是待调度，请检查数据！", invoiceVO.getVbillno());
					}

					logger.info("执行配载...");
					// 1、生成委托单主表
					EntrustVO entVO = new EntrustVO();
					entVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(entVO);
					boolean autoConfirm = ParameterHelper.getEntrustAutoConfirm();
					if (autoConfirm) {
						entVO.setVbillstatus(BillStatus.ENT_CONFIRM);
						entVO.setConfirm_date((new UFDateTime(new Date())).toString());
						entVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());

					} else {
						entVO.setVbillstatus(BillStatus.ENT_UNCONFIRM); // 待确认状态
					}
					entVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.WTD));
					entVO.setDbilldate(new UFDate());
					entVO.setPlan_carrier(pZHeaderVO.getPk_carrier());
					entVO.setPlan_carno(pZHeaderVO.getCarno());
					entVO.setPlan_driver(pZHeaderVO.getPk_driver());
					entVO.setPlan_car_type(pZHeaderVO.getPk_car_type());
					entVO.setPlan_trans_type(pZHeaderVO.getPk_trans_type());
					entVO.setPlan_flight(pZHeaderVO.getPk_flight());
					entVO.setUrgent_level(pZHeaderVO.getUrgent_level());
					entVO.setItem_code(pZHeaderVO.getItem_code());
					entVO.setPk_trans_line(pZHeaderVO.getPk_trans_line());
					entVO.setIf_return(pZHeaderVO.getIf_return());

					entVO.setPk_carrier(pZHeaderVO.getPk_carrier());
					entVO.setCarno(pZHeaderVO.getCarno());
					entVO.setGps_id(pZHeaderVO.getGps_id());
					entVO.setPk_driver(pZHeaderVO.getPk_driver());
					entVO.setPk_car_type(pZHeaderVO.getPk_car_type());
					entVO.setPk_trans_type(pZHeaderVO.getPk_trans_type());
					entVO.setPk_flight(pZHeaderVO.getPk_flight());

					entVO.setMemo(pZHeaderVO.getMemo());
					if (pZHeaderVO.getLot() == null || StringUtils.isBlank(pZHeaderVO.getLot())) {
						// 若页面未输入批次号，则在此生成
						String lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.BATORDERLOT);
						pZHeaderVO.setLot(lot);
					}
					entVO.setLot(pZHeaderVO.getLot());
					entVO.setInvoice_vbillno(segmentVO.getInvoice_vbillno());

					entVO.setCust_orderno(invoiceVO.getCust_orderno());
					entVO.setOrderno(invoiceVO.getOrderno());
					entVO.setPk_customer(invoiceVO.getPk_customer());
					entVO.setBalatype(invoiceVO.getBalatype());
					entVO.setSegment_vbillno(segmentVO.getVbillno());

					// 将运段的状态改成已调度
					segmentVO.setVbillstatus(BillStatus.SEG_DISPATCH);
					segmentVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(segmentVO);

					// 读取运段的路线信息,实际上也是发货单的路线信息
					EntLineBVO firstLineVO = new EntLineBVO();
					firstLineVO.setPk_address(segmentVO.getPk_delivery());
					firstLineVO.setPk_city(segmentVO.getDeli_city());
					firstLineVO.setPk_province(segmentVO.getDeli_province());
					firstLineVO.setPk_area(segmentVO.getDeli_area());
					firstLineVO.setDetail_addr(segmentVO.getDeli_detail_addr());
					firstLineVO.setContact(segmentVO.getDeli_contact());
					firstLineVO.setPhone(segmentVO.getDeli_phone());
					firstLineVO.setMobile(segmentVO.getDeli_mobile());
					firstLineVO.setEmail(segmentVO.getDeli_email());
					firstLineVO.setReq_arri_date(segmentVO.getReq_deli_date());
					firstLineVO.setAddr_flag(AddressConst.START_ADDR_FLAG); // 始发地标识
					firstLineVO.setPk_segment(segmentVO.getPk_segment());
					firstLineVO.setSegment_node(UFBoolean.TRUE);// 标记是运段的节点，区分用户自己增加的节点

					EntLineBVO lastLineVO = new EntLineBVO();
					lastLineVO.setPk_address(segmentVO.getPk_arrival());
					lastLineVO.setPk_city(segmentVO.getArri_city());
					lastLineVO.setPk_province(segmentVO.getArri_province());
					lastLineVO.setPk_area(segmentVO.getArri_area());
					lastLineVO.setDetail_addr(segmentVO.getArri_detail_addr());
					lastLineVO.setContact(segmentVO.getArri_contact());
					lastLineVO.setPhone(segmentVO.getArri_phone());
					lastLineVO.setMobile(segmentVO.getArri_mobile());
					lastLineVO.setEmail(segmentVO.getArri_email());
					lastLineVO.setReq_arri_date(segmentVO.getReq_arri_date());
					lastLineVO.setAddr_flag(AddressConst.END_ADDR_FLAG); // 目的地标识
					lastLineVO.setPk_segment(segmentVO.getPk_segment());
					lastLineVO.setSegment_node(UFBoolean.TRUE);

					List<InvLineBVO> invLineBVOs = groupMap.get(invoiceVO.getPk_invoice());
					if (invLineBVOs == null || invLineBVOs.size() == 0) {
						throw new BusiException("发货单[?]没有路线信息，请检查数据！", invoiceVO.getVbillno());
					}
					for (int i = 0; i < invLineBVOs.size(); i++) {
						EntLineBVO lineVO = new EntLineBVO();
						lineVO.setSerialno(invLineBVOs.get(i).getSerialno());
						lineVO.setPk_address(invLineBVOs.get(i).getPk_address());
						lineVO.setPk_city(invLineBVOs.get(i).getPk_city());
						lineVO.setPk_province(invLineBVOs.get(i).getPk_province());
						lineVO.setPk_area(invLineBVOs.get(i).getPk_area());
						lineVO.setDetail_addr(invLineBVOs.get(i).getDetail_addr());
						lineVO.setContact(invLineBVOs.get(i).getContact());
						lineVO.setPhone(invLineBVOs.get(i).getPhone());
						lineVO.setMobile(invLineBVOs.get(i).getMobile());
						lineVO.setEmail(invLineBVOs.get(i).getEmail());
						lineVO.setReq_arri_date(invLineBVOs.get(i).getReq_date_from().toString());
						lineVO.setReq_leav_date(invLineBVOs.get(i).getReq_date_till().toString());
						// yaojiie 2015 12 29 添加往委托单路线表添加操作类型。
						lineVO.setOperate_type(invLineBVOs.get(i).getOperate_type());
						if (i == 0) {
							lineVO.setPk_segment(segmentVO.getPk_segment());
							lineVO.setSegment_node(UFBoolean.TRUE);// 标记是运段的节点，区分用户自己增加的节点
							lineVO.setAddr_flag(AddressConst.START_ADDR_FLAG); // 起始地标识
						} else if (i == invLineBVOs.size() - 1) {
							lineVO.setPk_segment(segmentVO.getPk_segment());
							lineVO.setSegment_node(UFBoolean.TRUE);// 标记是运段的节点，区分用户自己增加的节点
							lineVO.setAddr_flag(AddressConst.END_ADDR_FLAG); // 目的地标识
						}

						// lineVO.setPk_segment(segmentVO.getPk_segment());
						lineVO.setPk_entrust(entVO.getPk_entrust());
						lineVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(lineVO);
						toBeUpdate.add(lineVO);// 更新路线信息表

						// 往EntLinePackBVO里添加数据
						EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
						entLinePackBVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(entLinePackBVO);
						entLinePackBVO.setPk_entrust(entVO.getPk_entrust());
						entLinePackBVO.setPk_ent_line_b(lineVO.getPk_ent_line_b());
						entLinePackBVO.setSerialno(invLineBVOs.get(i).getSerialno());
						entLinePackBVO.setPk_goods(invLineBVOs.get(i).getPk_goods());
						entLinePackBVO.setGoods_code(invLineBVOs.get(i).getGoods_code());
						entLinePackBVO.setGoods_name(invLineBVOs.get(i).getGoods_name());
						entLinePackBVO.setNum(invLineBVOs.get(i).getNum());
						entLinePackBVO.setPack(invLineBVOs.get(i).getPack());
						entLinePackBVO.setWeight(invLineBVOs.get(i).getWeight());
						entLinePackBVO.setVolume(invLineBVOs.get(i).getVolume());
						entLinePackBVO.setUnit_volume(invLineBVOs.get(i).getUnit_volume());
						entLinePackBVO.setLength(invLineBVOs.get(i).getLength());
						entLinePackBVO.setWeight(invLineBVOs.get(i).getWeight());
						entLinePackBVO.setHeight(invLineBVOs.get(i).getHeight());
						entLinePackBVO.setTrans_note(invLineBVOs.get(i).getTrans_note());
						entLinePackBVO.setLow_temp(invLineBVOs.get(i).getLow_temp());
						entLinePackBVO.setHight_temp(invLineBVOs.get(i).getHight_temp());
						entLinePackBVO.setReference_no(invLineBVOs.get(i).getReference_no());
						entLinePackBVO.setMemo(invLineBVOs.get(i).getMemo());
						entLinePackBVO.setPack_num_count(invLineBVOs.get(i).getPack_num_count());
						entLinePackBVO.setPlan_num(invLineBVOs.get(i).getPlan_num());
						entLinePackBVO.setPlan_pack_num_count(invLineBVOs.get(i).getPlan_pack_num_count());
						entLinePackBVO.setDef1(invLineBVOs.get(i).getDef1());
						entLinePackBVO.setDef2(invLineBVOs.get(i).getDef2());
						entLinePackBVO.setDef3(invLineBVOs.get(i).getDef3());
						entLinePackBVO.setDef4(invLineBVOs.get(i).getDef4());
						entLinePackBVO.setDef5(invLineBVOs.get(i).getDef5());
						entLinePackBVO.setDef6(invLineBVOs.get(i).getDef6());
						entLinePackBVO.setDef7(invLineBVOs.get(i).getDef7());
						entLinePackBVO.setDef8(invLineBVOs.get(i).getDef8());
						entLinePackBVO.setDef9(invLineBVOs.get(i).getDef9());
						entLinePackBVO.setDef10(invLineBVOs.get(i).getDef10());
						entLinePackBVO.setDef11(invLineBVOs.get(i).getDef11());
						entLinePackBVO.setDef12(invLineBVOs.get(i).getDef12());
						toBeUpdate.add(entLinePackBVO);

					}
					entVO.setReq_deli_date(firstLineVO.getReq_arri_date());
					entVO.setReq_arri_date(lastLineVO.getReq_arri_date());

					// 提货方
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
					entVO.setPk_arrival(lastLineVO.getPk_address());
					entVO.setArri_city(lastLineVO.getPk_city());
					entVO.setArri_province(lastLineVO.getPk_province());
					entVO.setArri_area(lastLineVO.getPk_area());
					entVO.setArri_detail_addr(lastLineVO.getDetail_addr());
					entVO.setArri_contact(lastLineVO.getContact());
					entVO.setArri_phone(lastLineVO.getPhone());
					entVO.setArri_mobile(lastLineVO.getMobile());
					entVO.setArri_email(lastLineVO.getEmail());

					// 计算运输里程，如果没有，则使用父级的运输里程
					Map<String, Object> mileageAndDistanceMap = this.getMileageAndDistance(firstLineVO.getPk_city(),
							lastLineVO.getPk_city(), firstLineVO.getPk_address(), lastLineVO.getPk_address());
					if (mileageAndDistanceMap != null) {
						Object mileage = mileageAndDistanceMap.get("mileage");
						if (mileage != null) {
							entVO.setMileage(Integer.parseInt(mileage.toString()));
						}
						Object distance = mileageAndDistanceMap.get("distance");
						if (distance != null) {
							entVO.setDistance(new UFDouble(distance.toString()));
						}
					}
					entVO.setPack_num_count(segmentVO.getPack_num_count());
					entVO.setNum_count(segmentVO.getNum_count());
					entVO.setWeight_count(segmentVO.getWeight_count());
					entVO.setVolume_count(segmentVO.getVolume_count());
					entVO.setFee_weight_count(segmentVO.getFee_weight_count());
					entVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
					entVO.setCreate_time(new UFDateTime(new Date()));
					entVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());

					toBeUpdate.add(entVO);

					// 插入批次表信息2015-12-29 lanjian
					EntLotVO entLotVO = new EntLotVO();
					// String lot =
					// BillnoHelper.generateBillnoByDefault(BillTypeConst.BATORDERLOT);
					entLotVO.setLot(entVO.getLot());
					entLotVO.setDbilldate(new UFDate());
					entLotVO.setVbillstatus(BillStatus.NEW);
					entLotVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
					entLotVO.setStatus(VOStatus.NEW);
					entLotVO.setCreate_time(new UFDateTime(new Date()));
					entLotVO.setPk_entrust(entVO.getPk_entrust());
					NWDao.setUuidPrimaryKey(entLotVO);
					toBeUpdate.add(entLotVO);

					// 3、生成货品信息
					SegPackBVO[] sPackVOs = dao.queryForSuperVOArrayByCondition(SegPackBVO.class,
							"isnull(dr,0)=0 and pk_segment=? ", segmentVO.getPk_segment());
					for (int i = 0; i < sPackVOs.length; i++) {
						SegPackBVO sPackVO = sPackVOs[i];
						EntPackBVO ePackVO = new EntPackBVO();
						ePackVO.setPk_invoice(sPackVO.getPk_invoice());
						ePackVO.setPk_segment(sPackVO.getPk_segment());
						ePackVO.setPk_goods(sPackVO.getPk_goods());
						ePackVO.setGoods_code(sPackVO.getGoods_code());
						ePackVO.setGoods_name(sPackVO.getGoods_name());
						ePackVO.setPlan_num(sPackVO.getPlan_num());
						ePackVO.setNum(sPackVO.getNum());
						ePackVO.setPlan_pack_num_count(sPackVO.getPlan_pack_num_count());
						ePackVO.setPack_num_count(sPackVO.getPack_num_count());
						ePackVO.setPack(sPackVO.getPack());
						ePackVO.setWeight(sPackVO.getWeight());
						ePackVO.setVolume(sPackVO.getVolume());
						ePackVO.setUnit_weight(sPackVO.getUnit_weight());
						ePackVO.setUnit_volume(sPackVO.getUnit_volume());
						ePackVO.setLength(sPackVO.getLength());
						ePackVO.setWidth(sPackVO.getWidth());
						ePackVO.setHeight(sPackVO.getHeight());
						ePackVO.setTrans_note(sPackVO.getTrans_note());
						ePackVO.setLow_temp(sPackVO.getLow_temp());
						ePackVO.setHight_temp(sPackVO.getHight_temp());
						ePackVO.setReference_no(sPackVO.getReference_no());
						ePackVO.setMemo(sPackVO.getMemo());
						ePackVO.setPk_entrust(entVO.getPk_entrust());
						ePackVO.setStatus(VOStatus.NEW);
						ePackVO.setSerialno(sPackVO.getSerialno());
						NWDao.setUuidPrimaryKey(ePackVO);
						toBeUpdate.add(ePackVO);// 更新货品信息
					}

					// 4.2 生成委托单的运力信息表 只會有一个运力信息
					EntTransbilityBVO tbVO = new EntTransbilityBVO();
					tbVO.setPk_entrust(entVO.getPk_entrust());
					tbVO.setCarno(entTransbilityVO.getCarno());
					tbVO.setPk_car_type(entTransbilityVO.getPk_car_type());
					tbVO.setPk_driver(entTransbilityVO.getPk_driver());
					tbVO.setGps_id(entTransbilityVO.getGps_id());
					tbVO.setContainer_no(entTransbilityVO.getContainer_no());
					tbVO.setMemo(entTransbilityVO.getMemo());
					tbVO.setSealing_no(entTransbilityVO.getSealing_no());
					tbVO.setForecast_deli_date(entTransbilityVO.getForecast_deli_date());
					tbVO.setNum(entTransbilityVO.getNum() == null ? 1 : entTransbilityVO.getNum());
					tbVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(tbVO);

					EntTransHisBVO entTransHisBVO = PZUtils.getEntTransHisBVO(tbVO);
					toBeUpdate.add(entTransHisBVO);
					toBeUpdate.add(tbVO);

					// 5、生成关联发货单子表
					EntInvBVO entInvBVO = new EntInvBVO();
					entInvBVO.setPk_entrust(entVO.getPk_entrust());
					entInvBVO.setPk_invoice(invoiceVO.getPk_invoice());
					entInvBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(entInvBVO);
					toBeUpdate.add(entInvBVO);// 更新发货单子表
					// 6、生成关联运段子表
					EntSegBVO entSegBVO = new EntSegBVO();
					entSegBVO.setPk_entrust(entVO.getPk_entrust());
					entSegBVO.setPk_segment(segmentVO.getPk_segment());
					entSegBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(entSegBVO);
					toBeUpdate.add(entSegBVO);// 更新运段子表
					// 7、生成应付明细主表
					PayDetailVO payVO = new PayDetailVO();
					payVO.setDbilldate(new UFDate());
					payVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
					// 记录批次号
					payVO.setLot(entVO.getLot());
					// 增加批量配载是否匹配合同规则判断
					List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
					boolean autoMatchContract = ParameterHelper.getBatchPZRule();
					if (autoMatchContract) {
						// 默认带出运费合同的币种(匹配合同)，如果没有维护合同则默认为系统设置中的币种
						contractBVOs = contractService.matchContract(ContractConst.CARRIER, pZHeaderVO.getPk_carrier(),
								pZHeaderVO.getPk_trans_type(), firstLineVO.getPk_address(), lastLineVO.getPk_address(),
								firstLineVO.getPk_city(), lastLineVO.getPk_city(), entVO.getPk_corp(),
								entVO.getReq_arri_date(), entVO.getUrgent_level(), entVO.getItem_code(),
								entVO.getPk_trans_line(), entVO.getIf_return());
						if (contractBVOs == null || contractBVOs.size() == 0) {
							// 没有匹配到合同
							payVO.setCurrency(ParameterHelper.getCurrency());
						} else {
							String pk_contract = contractBVOs.get(0).getPk_contract();
							ContractVO contractVO = this.getByPrimaryKey(ContractVO.class, pk_contract);
							payVO.setCurrency(contractVO.getCurrency());
						}
					}
					payVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
					payVO.setCreate_time(new UFDateTime(new Date()));
					payVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
					payVO.setEntrust_vbillno(entVO.getVbillno());
					payVO.setPk_carrier(pZHeaderVO.getPk_carrier());// 承运商
					payVO.setBalatype(pZHeaderVO.getBalatype());
					payVO.setVbillstatus(BillStatus.NEW);
					payVO.setMemo(entVO.getMemo()); // 默认等于发运单备注
					payVO.setPay_type(PayDetailConst.ORIGIN_TYPE); // 类型：默认为0
					payVO.setMerge_type(PayDetailConst.MERGE_TYPE.UNMERGE.intValue());
					payVO.setCust_orderno(entVO.getCust_orderno());
					payVO.setOrderno(entVO.getOrderno());
					// FIXME 取第一行合同明细的税种，税率
					if (contractBVOs != null && contractBVOs.size() > 0) {
						payVO.setTax_cat(contractBVOs.get(0).getTax_cat());
						payVO.setTax_rate(contractBVOs.get(0).getTax_rate());
						// FIXME 取第一行合同明细的税种，税率
						payVO.setTaxmny(
								CMUtils.getTaxmny(payVO.getCost_amount(), payVO.getTax_cat(), payVO.getTax_rate()));
					}

					payVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(payVO);
					toBeUpdate.add(payVO);

					// 8、生成应付明细子表，如果没有选择计价方式，那么匹配合同，返回明细，否则根据计价方式计算应付明细
					// 2015-05-30根据承运商的体积重换算比，计算计费重
					// TODO 重新计算，计费重、体积重、总金额
					Map<String, UFDouble> retMap = PZUtils.computeFeeWeightCount(entVO.getPk_carrier(),
							pZHeaderVO.getPk_trans_type(), segmentVO.getDeli_city(),segmentVO.getArri_city(),
							segmentVO.getVolume_count(), segmentVO.getWeight_count());
					if (retMap == null) {
						entVO.setFee_weight_count(segmentVO.getFee_weight_count());
						entVO.setVolume_weight_count(segmentVO.getVolume_weight_count());
					} else {
						entVO.setFee_weight_count(retMap.get("fee_weight_count"));
						entVO.setVolume_weight_count(retMap.get("volume_weight_count"));
					}
					if (contractBVOs != null && contractBVOs.size() > 0) {
						// 获取车型 修改只算表头设备类型费用，只要考虑明细费用就可以了。2015-11-6
						List<String> pk_car_types_list = new ArrayList<String>();
						pk_car_types_list.add(entTransbilityVO.getPk_car_type().toString());
						// 增加表头设备
						if (entVO.getPk_car_type() != null) {
							pk_car_types_list.add(entVO.getPk_car_type());
						}
						String[] pk_car_types = null;
						if (pk_car_types_list != null && pk_car_types_list.size() > 0) {

							pk_car_types = (String[]) pk_car_types_list.toArray(new String[pk_car_types_list.size()]);
						}

						List<PackInfo> packInfos = new ArrayList<PackInfo>();
						Map<String, List<SegPackBVO>> packGroupMap = new HashMap<String, List<SegPackBVO>>();
						// 对包装按照pack进行分组
						for (SegPackBVO sPackVO : sPackVOs) {
							String key = sPackVO.getPack();
							if (StringUtils.isBlank(key)) {
								// 没有包装的货品自动过滤
								continue;
							}
							List<SegPackBVO> voList = packGroupMap.get(key);
							if (voList == null) {
								voList = new ArrayList<SegPackBVO>();
								packGroupMap.put(key, voList);
							}
							voList.add(sPackVO);
						}
						if (packGroupMap.size() > 0) {
							for (String key : groupMap.keySet()) {
								PackInfo packInfo = new PackInfo();
								List<SegPackBVO> voList = packGroupMap.get(key);
								Integer num = 0;
								UFDouble weight = UFDouble.ZERO_DBL;
								UFDouble volume = UFDouble.ZERO_DBL;
								for (SegPackBVO packBVO : voList) {
									num = num + (packBVO.getNum() == null ? 0 : packBVO.getNum());
									weight = weight
											.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
									volume = volume
											.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
								}
								packInfo.setPack(key);
								packInfo.setNum(num);
								packInfo.setWeight(weight);
								packInfo.setVolume(volume);
								packInfos.add(packInfo);
							}
						}

						List<PayDetailBVO> detailVOs = contractService.buildPayDetailBVO(entVO.getPk_carrier(),
								segmentVO.getPack_num_count() == null ? 0 : segmentVO.getPack_num_count().doubleValue(),
								segmentVO.getNum_count(), entVO.getFee_weight_count().doubleValue(),
								segmentVO.getWeight_count() == null ? 0 : segmentVO.getWeight_count().doubleValue(),
								segmentVO.getVolume_count() == null ? 0 : segmentVO.getVolume_count().doubleValue(), 2,
								1, packInfos, pk_car_types, entVO.getPk_corp(), entVO.getUrgent_level(),
								entVO.getItem_code(), entVO.getPk_trans_line(), entVO.getIf_return(), contractBVOs);
						if (detailVOs != null && detailVOs.size() > 0) {
							String transFeeCode = ExpenseTypeConst.ET10;// 得到系统定义的运费的编码
							ExpenseTypeVO etVO = (ExpenseTypeVO) expenseTypeService.getByCode(transFeeCode);// 得到运费的vo
							// 2015-05-27 批量配载不再需要选择计价方式了
							// if(headerVO.getValuation_type() == null) {
							// 计算费用类型为运费的费用明细，取大值，加上其他费用类型的费用
							double maxAmount = 0;
							List<PayDetailBVO> toBeDelete = new ArrayList<PayDetailBVO>();
							for (PayDetailBVO detailVO : detailVOs) {
								// 计算的金额
								UFDouble amount = PZUtils.compute(detailVO.getQuote_type(),
										detailVO.getValuation_type(), detailVO.getPrice_type(), detailVO.getPrice(),
										segmentVO.getFee_weight_count(), segmentVO.getVolume_count(),
										segmentVO.getNum_count());
								// detailVO.setAmount(amount);
								// 识别所有运费的费用明细
								if (etVO.getPk_expense_type().equals(detailVO.getPk_expense_type())) {// 匹配到运费
									if (amount.doubleValue() < maxAmount) {
										toBeDelete.add(detailVO);
									}
								}
							}
							detailVOs.removeAll(toBeDelete);
							toBeDelete.clear();

							UFDouble cost_amount = UFDouble.ZERO_DBL;// 金额合计
							for (PayDetailBVO detailVO : detailVOs) {
								cost_amount = cost_amount
										.add(detailVO.getAmount() == null ? UFDouble.ZERO_DBL : detailVO.getAmount());
								detailVO.setPk_pay_detail(payVO.getPk_pay_detail());
								detailVO.setStatus(VOStatus.NEW);
								NWDao.setUuidPrimaryKey(detailVO);
								toBeUpdate.add(detailVO);

								// 第9步的分摊放到这边执行
								// 按发货单进行分摊
								List<PayDeviBVO> deviVOs = PZUtils.getPayDeviBVO(entVO, invoiceVO, segmentVO, detailVO);
								toBeUpdate.addAll(deviVOs);
							}
							entVO.setCost_amount(cost_amount);
							// 9、生成分摊表,见第8步
						}
					}
					// 设置应付明细经计算后的数据
					payVO.setPack_num_count(entVO.getPack_num_count());
					payVO.setNum_count(entVO.getNum_count());
					payVO.setVolume_count(entVO.getVolume_count());
					payVO.setWeight_count(entVO.getWeight_count());
					payVO.setFee_weight_count(entVO.getFee_weight_count());
					payVO.setCost_amount(entVO.getCost_amount());
					payVO.setUngot_amount(entVO.getCost_amount());// 未付金额默认等于总金额
					// 执行数据库操作
					break;
				}
			}
		}
		dao.saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));
		CMUtils.totalCostComput(Arrays.asList(invoiceVOs));
	}

	// yaojiie 2015 11 23添加接口方法，milkrun一键生成方法。
	public void keyStowage(ParamVO paramVO, String[] pk_invoices) {
		// 第一步，去数据库获取发货单，检查发货单运输方式和单据状态
		if (pk_invoices == null || pk_invoices.length == 0) {
			throw new BusiException("请先选择发货单！");
		}
		String cond = NWUtils.buildConditionString(pk_invoices);
		InvoiceVO[] invoiceVOs = dao.queryForSuperVOArrayByCondition(InvoiceVO.class, " pk_invoice in " + cond);

		List<String> invoice_vbillnos = new ArrayList<String>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (invoiceVO.getVbillstatus() != BillStatus.INV_CONFIRM) {
				throw new BusiException("发货单[?],只有是[确认]状态的发货单才允许此操作 ，请检查数据！", invoiceVO.getVbillno());
			}
			if (StringUtils.isBlank(invoiceVO.getPk_carrier())) {
				throw new BusiException("发货单[?]必须要有承运商才允许此操作 ，请检查数据！", invoiceVO.getVbillno());
			}
			invoice_vbillnos.add(invoiceVO.getVbillno());
		}
		String vbillnos_cond = NWUtils
				.buildConditionString(invoice_vbillnos.toArray(new String[invoice_vbillnos.size()]));

		TransBilityBVO[] transBilityBVOs = dao.queryForSuperVOArrayByCondition(TransBilityBVO.class,
				" pk_invoice in " + cond);

		// 第二步，用发货单大单据号，去运段表中获取相应的运段数据
		SegmentVO[] segmentVOs = dao.queryForSuperVOArrayByCondition(SegmentVO.class,
				" invoice_vbillno in " + vbillnos_cond);

		// 对发货单按相同承运商信息进行分组，取其中一个的运力信息就可以。

		Map<String, List<InvoiceVO>> groupMap = new HashMap<String, List<InvoiceVO>>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			String key = new StringBuffer().append(invoiceVO.getPk_carrier()).append("||")
					.append(invoiceVO.getPk_trans_type()).toString();
			List<InvoiceVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<InvoiceVO>();
				groupMap.put(key, voList);
			}
			voList.add(invoiceVO);
		}

		for (String key : groupMap.keySet()) {
			List<InvoiceVO> voList = groupMap.get(key);
			if (voList != null && voList.size() > 0) {
				List<SegmentVO> segVOs = new ArrayList<SegmentVO>();
				for (InvoiceVO invoiceVO : voList) {
					for (SegmentVO segmentVO : segmentVOs) {
						if (segmentVO.getInvoice_vbillno().equals(invoiceVO.getVbillno())) {
							segVOs.add(segmentVO);
						}
					}
				}
				List<EntTransbilityBVO> entTransbilityVOs = new ArrayList<EntTransbilityBVO>();
				for (InvoiceVO invoiceVO : voList) {
					for (TransBilityBVO transBilityBVO : transBilityBVOs) {
						if (invoiceVO.getPk_invoice().equals(transBilityBVO.getPk_invoice())) {
							EntTransbilityBVO entTransbilityBVO = new EntTransbilityBVO();
							entTransbilityBVO.setPk_car_type(transBilityBVO.getPk_car_type());
							entTransbilityBVO.setPk_driver(transBilityBVO.getPk_driver());
							entTransbilityBVO.setCarno(transBilityBVO.getPk_driver());
							entTransbilityBVO.setNum(transBilityBVO.getNum());
							entTransbilityBVO.setMemo(transBilityBVO.getMemo());
							entTransbilityVOs.add(entTransbilityBVO);
						}
					}
					// 取一辆车的运力信息即可
					break;
				}
				PZHeaderVO pzHeaderVO = new PZHeaderVO();
				pzHeaderVO.setPk_carrier(voList.get(0).getPk_carrier());
				pzHeaderVO.setPk_trans_type(voList.get(0).getPk_trans_type());
				pzHeaderVO.setBalatype(voList.get(0).getBalatype());
				pzHeaderVO.setMemo(voList.get(0).getMemo());
				ExAggEntrustVO pzAggVO = new ExAggEntrustVO();
				pzAggVO.setParentVO(pzHeaderVO);
				pzAggVO.setTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B,
						entTransbilityVOs.toArray(new EntTransbilityBVO[entTransbilityVOs.size()]));
				pzAggVO.setTableVO(TabcodeConst.TS_SEGMENT, segVOs.toArray(new SegmentVO[segVOs.size()]));
				pZService.save(pzAggVO, paramVO);
			}

		}

	}

	@Transactional
	public Map<String, Object> close(ParamVO paramVO) {
		logger.info("执行单据关闭动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if (oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if (BillStatus.NEW != billStatus) {
				throw new RuntimeException("只有[新建]状态的单据才能进行关闭！");
			}
		}
		InvoiceVO invoiceVO = (InvoiceVO) parentVO;
		invoiceVO.setStatus(VOStatus.UPDATED);
		invoiceVO.setVbillstatus(BillStatus.INV_CLOSE);
		invoiceVO.setModify_time(new UFDateTime());
		invoiceVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		NWDao.getInstance().saveOrUpdate(invoiceVO);

		// 确认时，向发货单的跟踪信息表插入一条记录
		InvTrackingVO itVO = new InvTrackingVO();
		itVO.setTracking_status(TrackingConst.CLOSE);
		itVO.setTracking_time(new UFDateTime(new Date()));
		itVO.setTracking_memo("关闭");
		itVO.setInvoice_vbillno(invoiceVO.getVbillno());
		itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		NWDao.getInstance().saveOrUpdate(itVO);
		return execFormula4Templet(billVO, paramVO);
	}

	@Transactional
	public Map<String, Object> unclose(ParamVO paramVO) {
		logger.info("执行单据撤销关闭动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if (oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if (BillStatus.INV_CLOSE != billStatus) {
				throw new RuntimeException("只有[关闭]状态的单据才能进行撤销关闭！");
			}
		}
		InvoiceVO invoiceVO = (InvoiceVO) parentVO;
		invoiceVO.setStatus(VOStatus.UPDATED);
		invoiceVO.setVbillstatus(BillStatus.NEW);
		invoiceVO.setModify_time(new UFDateTime());
		invoiceVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		NWDao.getInstance().saveOrUpdate(invoiceVO);
		return execFormula4Templet(billVO, paramVO);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String processBeforeConfirmByProc(String pk_invoices) {
		if (StringUtils.isBlank(pk_invoices)) {
			return null;
		}
		final List<String> returnMsgs = new ArrayList<String>();
		// 存储过程名称
		final String TS_FHD_CHECK_PROC = "ts_fhd_check_proc";
		final String PK = pk_invoices;
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
						returnMsgs.add(rs.getString(1));
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			// e.printStackTrace();
			logger.info(e.getMessage());
			// 出现错误返回空接可以了
			return null;
		}
		if (returnMsgs.size() > 0) {
			return returnMsgs.get(0);
		}
		return null;
	}

}
