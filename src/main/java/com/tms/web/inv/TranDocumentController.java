package com.tms.web.inv;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.exp.ExcelImporter;
import org.nw.service.sys.DataDictService;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.base.CustService;
import com.tms.service.base.GoodsService;
import com.tms.service.base.TransLineService;
import com.tms.service.cm.ContractService;
import com.tms.service.inv.InvoiceService;
import com.tms.service.te.EntrustService;
import com.tms.vo.base.GoodsPackRelaVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 发货单
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
@Controller
@RequestMapping(value = "/inv/td")
public class TranDocumentController extends AbsBillController {

	public static final String INV_UNCONFIRM_TYPE_LIST = "inv_unconfirm_type_list";// 数据字典中定义应收明细反确认类型

	@Autowired
	private DataDictService dataDictService;
	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private TransLineService transLineService;

	@Autowired
	private CustService custService;

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private EntrustService entrustService;

	public InvoiceService getService() {
		return invoiceService;
	}

	@RequestMapping(value = "/loadAddrInfoByPkAddress.json")
	@ResponseBody
	public Map<String, Object> loadAddrInfoByPkAddress(HttpServletRequest request, HttpServletResponse response) {
		String pk_address = request.getParameter("pk_address");
		String type = request.getParameter("type");
		if(StringUtils.isBlank(pk_address)) {
			return genAjaxResponse(true, null, null);
		}
		return this.genAjaxResponse(true, null, this.invoiceService.getAddrInfoByPkAddress(pk_address, type));
	}

	/**
	 * 根据条件返回运输里程和区间距离
	 */
	@RequestMapping(value = "/getMileageAndDistance.json")
	@ResponseBody
	public Map<String, Object> getMileageAndDistance(HttpServletRequest request, HttpServletResponse response) {
		String deli_city = request.getParameter("deli_city");
		String arri_city = request.getParameter("arri_city");
		String pk_delivery = request.getParameter("pk_delivery");
		String pk_arrival = request.getParameter("pk_arrival");
		return this.genAjaxResponse(true, null,
				this.getService().getMileageAndDistance(deli_city, arri_city, pk_delivery, pk_arrival));
	}

	/**
	 * 根据条件计算要求到货日期
	 */
	@RequestMapping(value = "/computeReq_deli_date.json")
	@ResponseBody
	public Map<String, Object> computeReq_deli_date(HttpServletRequest request, HttpServletResponse response) {
		String req_deli_date = request.getParameter("req_deli_date");
		String deli_city = request.getParameter("deli_city");
		String arri_city = request.getParameter("arri_city");
		String pk_trans_type = request.getParameter("pk_trans_type");
		TransLineVO condVO = new TransLineVO();
		condVO.setStart_addr(deli_city);
		condVO.setEnd_addr(arri_city);
		condVO.setPk_trans_type(pk_trans_type);
		TransLineVO lineVO = transLineService.getByObject(condVO);
		if(lineVO == null) {
			return this.genAjaxResponse(true, null, null);
		}
		double s_timeline = lineVO.getS_timeline().doubleValue(); // 返回标准时效
		Date req_arri_date = DateUtils.addHour(DateUtils.parseString(req_deli_date), s_timeline);
		return this.genAjaxResponse(true, null, DateUtils.formatDate(req_arri_date));
	}


	/**
	 * 读取货品关联的包装单位的信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getGoodsPackcorpInfo.json")
	@ResponseBody
	public Map<String, Object> getGoodsPackcorpInfo(HttpServletRequest request, HttpServletResponse response) {
		String pk_goods = request.getParameter("pk_goods");
		String pk_goods_packcorp = request.getParameter("pk_goods_packcorp");
		if(StringUtils.isBlank(pk_goods) || StringUtils.isBlank(pk_goods_packcorp)) {
			throw new BusiException("货品和货品对应的包装明细不能为空");
		}
		GoodsPackRelaVO vo = goodsService.getGoodsPackRelaVO(pk_goods, pk_goods_packcorp);
		return this.genAjaxResponse(true, null, vo);
	}

	/**
	 * 保存时校验单据的金额计算是否正确
	 * 
	 * @param billVO
	 * @return
	 */
	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO; // 强制转换，转换不成功，说明billInfo配置错误
		InvoiceVO parentVO = (InvoiceVO) aggVO.getParentVO();
		InvPackBVO[] packVOs = (InvPackBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_PACK_B); // 包装明细VOs
		ReceDetailBVO[] detailVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B); // 应收明细子表VOs

		int precision = ParameterHelper.getPrecision();
		// 得到包装中的总重量，总体积,总件数
		UFDouble weight_count = new UFDouble(0);
		UFDouble volume_count = new UFDouble(0);
		UFDouble pack_num_count = new UFDouble(0);
		int num_count = 0;
		for(InvPackBVO packVO : packVOs) {
			if(packVO.getStatus() != VOStatus.DELETED) {// 如果是删除的记录不应该统计进去
				if(packVO.getNum() != null) {
					num_count += packVO.getNum().intValue();
				}
				if(packVO.getWeight() != null) {
					weight_count = weight_count.add(packVO.getWeight());
				}
				if(packVO.getVolume() != null) {
					volume_count = volume_count.add(packVO.getVolume());
				}
				if(packVO.getPack_num_count() != null) {
					pack_num_count = pack_num_count.add(packVO.getPack_num_count());
				}
			}
		}
		if(parentVO.getPack_num_count() != null
				&& parentVO.getPack_num_count().doubleValue() != NWUtils.getRoundValue(pack_num_count.doubleValue(),
						precision)) {
			throw new BusiException("总数量计算不正确,请检查表单！");
		}
		if(parentVO.getNum_count() != null && parentVO.getNum_count().intValue() != num_count) {
			throw new BusiException("总件数计算不正确,请检查表单！");
		}
		if(parentVO.getWeight_count() != null
				&& parentVO.getWeight_count().doubleValue() != NWUtils.getRoundValue(weight_count.doubleValue(),
						precision)) {
			throw new BusiException("总重量计算不正确,请检查表单！");
		}
		if(parentVO.getVolume_count() != null
				&& parentVO.getVolume_count().doubleValue() != NWUtils.getRoundValue(volume_count.doubleValue(),
						precision)) {
			throw new BusiException("总体积计算不正确,请检查表单！");
		}
		UFDouble cost_amount = new UFDouble(0);// 总金额
		for(ReceDetailBVO detailVO : detailVOs) {
			if(detailVO.getStatus() != VOStatus.DELETED) {
				if(detailVO.getAmount() != null) {// 金额可能为空
					cost_amount = cost_amount.add(detailVO.getAmount());
				}
			}
		}
		// if(parentVO.getCost_amount() != null
		// && parentVO.getCost_amount().doubleValue() !=
		// NWUtils.getRoundValue(cost_amount.doubleValue(),
		// precision)) {
		// throw new BusiException("总金额计算不正确,请检查表单！");
		// }
		// 校验总计费重
		UFDouble rate = custService.getFeeRate(parentVO.getPk_customer(), parentVO.getPk_trans_type(), parentVO.getDeli_city(), parentVO.getArri_city());
		double fee = volume_count.doubleValue() * rate.doubleValue(); // 总体积/体积重换算比率
		if(fee < weight_count.doubleValue()) {
			fee = weight_count.doubleValue();
		}
		if(parentVO.getFee_weight_count() != null
				&& parentVO.getFee_weight_count().doubleValue() != NWUtils.getRoundValue(fee, precision)) {
			throw new BusiException("总计费重计算不正确,请检查表单！");
		}

		// 根据参数，检查orderno，cust_orderno是否必须唯一
		if(StringUtils.isNotBlank(parentVO.getOrderno())) {
			boolean orderno_must_unique = ParameterHelper.getBooleanParam("orderno_must_unique");
			if(orderno_must_unique) {
				// 必须唯一
				String sql = "select count(1) from ts_invoice with(nolock) where isnull(dr,0)=0 and orderno=? and pk_corp=?";
				if(parentVO.getStatus() == VOStatus.UPDATED) {
					sql += " and pk_invoice != '" + parentVO.getPk_invoice() + "'";// 排除自身
				}
				Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, parentVO.getOrderno(),
						WebUtils.getLoginInfo().getPk_corp());
				if(count > 0) {
					throw new BusiException("订单号已经被使用，请使用其他订单号！");
				}
			}
		}
		if(StringUtils.isNotBlank(parentVO.getCust_orderno())) {
			boolean cust_orderno_must_unique = ParameterHelper.getBooleanParam("cust_orderno_must_unique");
			if(cust_orderno_must_unique) {
				// 必须唯一
				String sql = "select count(1) from ts_invoice with(nolock) where isnull(dr,0)=0 and cust_orderno=? and pk_corp=?";
				if(parentVO.getStatus() == VOStatus.UPDATED) {
					sql += " and pk_invoice != '" + parentVO.getPk_invoice() + "'";// 排除自身
				}
				Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, parentVO.getCust_orderno(),
						WebUtils.getLoginInfo().getPk_corp());
				if(count > 0) {
					throw new BusiException("客户订单号已经被使用，请使用其他客户订单号！");
				}
			}
		}
	}

	/**
	 * 该方法与配载页面的方法一样<br/>
	 * 配载页面-根据承运商、运输方式、路线信息匹配合同，并算出费用明细
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/refreshReceDetail.json")
	@ResponseBody
	public Map<String, Object> refreshReceDetail(HttpServletRequest request, HttpServletResponse response) {
		String pk_invoice = request.getParameter("pk_invoice");
		double pack_num_count = 0;
		try {
			pack_num_count = Double.parseDouble(request.getParameter("pack_num_count"));// 总数量
		} catch(Exception e) {

		}
		int num_count = Integer.parseInt(request.getParameter("num_count"));// 总件数，不可能出现异常啊
		double fee_weight_count = Double.parseDouble(request.getParameter("fee_weight_count"));// 总计费重
		double weight_count = Double.parseDouble(request.getParameter("weight_count"));// 总重量
		double volume_count = Double.parseDouble(request.getParameter("volume_count"));// 总体积
		String bala_customer = request.getParameter("bala_customer"); // 结算客户
		String pk_trans_type = request.getParameter("pk_trans_type"); // 运输方式
		String pk_delivery = request.getParameter("pk_delivery"); // 提货方
		String deli_city = request.getParameter("deli_city"); // 提货城市
		String pk_arrival = request.getParameter("pk_arrival"); // 收货方
		String arri_city = request.getParameter("arri_city"); // 收货城市
		String[] pk_car_type = request.getParameterValues("pk_car_type"); // 车辆类型
		String pk_corp = request.getParameter("pk_corp");
		String req_arri_date = request.getParameter("req_arri_date");
		String urgent_level = request.getParameter("urgent_level");
		String item_code = request.getParameter("item_code");
		String pk_trans_line = request.getParameter("pk_trans_line");
		UFBoolean if_return = request.getParameter("if_return") == null? null : UFBoolean.valueOf(request.getParameter("if_return"));
		if(StringUtils.isBlank(bala_customer) || StringUtils.isBlank(pk_trans_type) || StringUtils.isBlank(pk_delivery)
				|| StringUtils.isBlank(pk_arrival)) {
			throw new BusiException("匹配合同时，结算客户、运输方式、起始地址、目的地址都不能为空！");
		}
		List<Map<String, Object>> retList = this.getService().refreshReceDetail(pk_invoice, pack_num_count, num_count,
				fee_weight_count, weight_count, volume_count, bala_customer, pk_trans_type, pk_delivery, pk_arrival,
				deli_city, arri_city, pk_car_type, pk_corp, req_arri_date,Integer.valueOf(urgent_level),item_code,pk_trans_line,if_return);
		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(retList == null ? new ArrayList<Map<String, Object>>() : retList); // 这里返回一个空数组，这样页面会将上次的值清空
		return this.genAjaxResponse(true, null, pageVO);
	}

	/**
	 * 根据发货单查询应收明细
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @deprecated 已经使用refreshReceDetail,发货单需要匹配合同
	 */
	@RequestMapping(value = "/loadReceDetail.json")
	@ResponseBody
	public Map<String, Object> loadReceDetail(HttpServletRequest request, HttpServletResponse response) {
		String pk_invoice = request.getParameter("pk_invoice");
		if(StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("加载表体数据时，主键不能为空!");
		}
		ParamVO paramVO = this.getParamVO(request);
		int offset = this.getOffset(request);
		int pageSize = this.getPageSize(request);
		PaginationVO pageVO = this.getService().loadReceDetail(pk_invoice, paramVO, offset, pageSize);
		return this.genAjaxResponse(true, null, pageVO);
	}

	/**
	 * 返回最近的5条待提交的发货单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTodayTop5.html")
	@ResponseBody
	public ModelAndView getTodayTop5(HttpServletRequest request, HttpServletResponse response) {
		List<InvoiceVO> invVOs = this.getService().getTodayTop5();
		request.setAttribute("dataList", invVOs);
		return new ModelAndView("/default/inv.jsp");
	}


	/**
	 * 点击修订时，检测是否可以对该单据进行修订<br/>
	 * 1、 当发货单状态是：已确认、已提货、已到货状态<br/>
	 * 2、 发货单对应应收凭证状态是：新建；如果发货单已经生成了应付凭证：所有应付凭证状态是：新建。 <br/>
	 * 3、 发货单对应调度计划处没有做分量操作 <br/>
	 * 4、 这三个验证通过后操作人员才可以修改数据，如果有一个没有验证通过，详细提示具体错误原因。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/checkBeforeRevise.json")
	@ResponseBody
	public Map<String, Object> checkBeforeRevise(HttpServletRequest request, HttpServletResponse response) {
		String billId = request.getParameter("billId");
		if(StringUtils.isBlank(billId)) {
			throw new BusiException("请先选择记录进行修订！");
		}
		InvoiceVO invVO = this.getService().getByPrimaryKey(InvoiceVO.class, billId);
		Integer vbillstatus = invVO.getVbillstatus();
		if(vbillstatus != BillStatus.INV_CONFIRM && vbillstatus != BillStatus.INV_DELIVERY
				&& vbillstatus != BillStatus.INV_ARRIVAL && vbillstatus != BillStatus.INV_PART_DELIVERY
				&& vbillstatus != BillStatus.INV_PART_ARRIVAL) {
			throw new BusiException("发货单必须是[已确认、已提货、部分提货、部分到货、已到货]状态下才能进行修订！");
		}
		// 根据发货单查询应收明细(应收凭证)
		ReceiveDetailVO rdVO = this.getService().getReceiveDetailVOByInvoiceBillno(invVO.getVbillno());
		if(rdVO != null) {
			if(rdVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("发货单对应的应收凭证状态必须是[新建]才能进行修订,应收凭证单号[?]！",rdVO.getVbillno());
			}
		}
		// 查询调度计划
		SegmentVO[] segVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class, "invoice_vbillno=?",
				invVO.getVbillno());
		if(segVOs != null && segVOs.length > 0) {
			for(SegmentVO segVO : segVOs) {
				if(segVO.getSeg_type() == SegmentConst.QUANTITY) {// 分量
					throw new BusiException("发货单对应的调度计划[调度单号:?]已做了分量操作，发货单不能修订！",segVO.getVbillno());
				}
			}
		}
		// 查找发货单对应的委托单
		List<EntrustVO> entVOs = NWDao
				.getInstance()
				.queryForList(
						"select * from ts_entrust where isnull(dr,0)=0 "
								+ "and pk_entrust in (select pk_entrust from ts_ent_inv_b where pk_invoice=? and isnull(dr,0)=0)",
						EntrustVO.class, invVO.getPk_invoice());
		if(entVOs != null && entVOs.size() > 0) {
			for(EntrustVO entVO : entVOs) {
				PayDetailVO pdVO = entrustService.getPayDetailVOsByEntrustBillno(entVO.getVbillno());
				if(pdVO != null) {
					if(pdVO.getVbillstatus() != BillStatus.NEW) {
						throw new BusiException("发货单对应的应付凭证状态必须是[新建]才能进行修订，应付凭证单号[?]！",pdVO.getVbillno());
					}
				}
			}
		}
		return this.genAjaxResponse(true, null, "Y");
	}
	
	/**
	 * 获取反确认类型列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getUnConfirmTypeList.json")
	@ResponseBody
	public Map<String, Object> getUnConfirmTypeList(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(INV_UNCONFIRM_TYPE_LIST);
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		if(billVO != null) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null) {
				for(CircularlyAccessibleValueObject cvo : cvos) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(Constants.TEXT, cvo.getAttributeValue(DataDictBVO.DISPLAY_NAME));
					map.put(Constants.VALUE, cvo.getAttributeValue(DataDictBVO.VALUE));
					list.add(map);
				}
				recordsMap.put("records", list);
			}
		}
		return recordsMap;
	}
		
		//一键milkrun yaojiie 2015 12 23
		@RequestMapping(value = "/milkRun.json")
		public void milkRun(HttpServletRequest request, HttpServletResponse response){
			ParamVO paramVO = this.getParamVO(request);
			//因为要调用批量配载的方法，所以先将模板编码设置为T305
			paramVO.setFunCode(FunConst.SEG_BATCH_PZ_CODE);
			paramVO.setBillType(BillTypeConst.WTD);
			String[] pk_invoices = request.getParameterValues("pk_invoice");
			this.getService().milkRun(paramVO, pk_invoices);
		}
		
		//一键配載 yaojiie 2016 2 24 一键配载
		@RequestMapping(value = "/keyStowage.json")
		public void keyStowage(HttpServletRequest request, HttpServletResponse response){
			ParamVO paramVO = this.getParamVO(request);
			//因为要调用批量配载的方法，所以先将模板编码设置为T305
			paramVO.setFunCode(FunConst.SEG_BATCH_PZ_CODE);
			paramVO.setBillType(BillTypeConst.WTD);
			String[] pk_invoices = request.getParameterValues("pk_invoice");
			this.getService().keyStowage(paramVO, pk_invoices);
		}
		
		@RequestMapping(value = "/close.json")
		@ResponseBody
		public Map<String, Object> close(HttpServletRequest request, HttpServletResponse response) {
			String[] billId = request.getParameterValues("billId");
			if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
				throw new RuntimeException("billId不能为空！");
			}
			ParamVO paramVO = this.getParamVO(request);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(String id : billId){
				paramVO.setBillId(id);
				Map<String, Object> retMap = this.getService().close(paramVO);
				list.add(retMap);
			}
			return this.genAjaxResponse(true, null, list);
		}

		@RequestMapping(value = "/unclose.json")
		@ResponseBody
		public Map<String, Object> unclose(HttpServletRequest request, HttpServletResponse response) {
			String[] billId = request.getParameterValues("billId");
			if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
				throw new RuntimeException("billId不能为空！");
			}
			ParamVO paramVO = this.getParamVO(request);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(String id : billId){
				paramVO.setBillId(id);
				Map<String, Object> retMap = this.getService().unclose(paramVO);
				list.add(retMap);
			}
			return this.genAjaxResponse(true, null, list);
		}
		
		
		public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
			// 获取订单跟踪url
			String trackingUrl = "/inv/ot/index";
			request.setAttribute("trackingUrl", trackingUrl); // 设置到前台
			return super.index(request, response);
		}
		
}
