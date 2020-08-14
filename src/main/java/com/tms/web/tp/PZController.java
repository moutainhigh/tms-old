package com.tms.web.tp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.utils.FormulaHelper;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.base.CarService;
import com.tms.service.base.CarrService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.service.te.EntrustService;
import com.tms.service.tp.PZService;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 调度配载-配载页面
 * 
 * @author xuqc
 * @date 2012-9-12 下午09:36:54
 */
@Controller
@RequestMapping(value = "/tp/pz")
public class PZController extends AbsBillController {

	@Autowired
	private PZService pZService;

	@Autowired
	private EntrustService entrustService;
	
	@Autowired
	private CarService carService;
	
	@Autowired
	private CarrService carrService;

	@Autowired
	private ExpenseTypeService expenseTypeService;

	public PZService getService() {
		return pZService;
	}

	/**
	 * 配载操作，打开配载窗口
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = getParamVO(request);
		// 获取模板数据
		String templetID = this.getService().getBillTemplateID(paramVO);
		if(StringUtils.isBlank(templetID)) {
			throw new BusiException("没有分配单据模板！");
		}
		UiBillTempletVO templetVO = this.getService().getBillTempletVO(templetID);
		// 设置表体主键
		setChildrenPkFieldMap(templetVO, this.getService().getBillInfo());
		templetVO.setFunCode(paramVO.getFunCode());
		templetVO.setNodeKey(paramVO.getNodeKey());
		request.setAttribute(Constants.TEMPLETVO, templetVO);
		// 页面需要使用该moduleName
		request.setAttribute(Constants.MODULENAME, templetVO.getModuleName());

		// 读取运段明细在功能注册的url,设置到segPackUrl中
		String segPackUrl = this.getService().getFunVOByFunCode(FunConst.SEG_PACK_CODE).getClass_name();
		if(segPackUrl.indexOf("?") == -1) {
			segPackUrl += "?";
		} else {
			segPackUrl += "&";
		}
		segPackUrl += "funCode=" + FunConst.SEG_PACK_CODE;
		request.setAttribute("segPackUrl", segPackUrl); // 设置到前台

		Map<String, Object> headerMap = new HashMap<String, Object>();
		// 运段参数
		String[] pk_segment = request.getParameterValues("pk_segment");
		if(pk_segment != null && pk_segment.length > 0) {
			// 读取运段的备注信息、总件数、总重量、总体积,总体积重，总计费重
			double pack_num_count = 0, num_count = 0, weight_count = 0, volume_count = 0, fee_weight_count = 0, volume_weight_count = 0;
			StringBuffer memoBuf = new StringBuffer();
			SegmentVO[] segVOs = this.getService().querySegmentByPKs(pk_segment);
			if(segVOs != null) {
				for(SegmentVO segVO : segVOs) {
					if(StringUtils.isNotBlank(segVO.getMemo())) {
						memoBuf.append(segVO.getMemo() == null ? "" : segVO.getMemo()).append(Constants.SPLIT_CHAR);
					}
					pack_num_count += segVO.getPack_num_count() == null ? 0 : segVO.getPack_num_count().doubleValue();
					num_count += segVO.getNum_count() == null ? 0 : segVO.getNum_count();
					weight_count += segVO.getWeight_count() == null ? 0 : segVO.getWeight_count().doubleValue();
					volume_count += segVO.getVolume_count() == null ? 0 : segVO.getVolume_count().doubleValue();
					fee_weight_count += segVO.getFee_weight_count() == null ? 0 : segVO.getFee_weight_count()
							.doubleValue();
					volume_weight_count += segVO.getVolume_weight_count() == null ? 0 : segVO.getVolume_weight_count()
							.doubleValue();
				}
			}
			headerMap.put("pk_trans_type", segVOs[0].getPk_trans_type());// 默认第一个运段的运输方式
			headerMap.put("pack_num_count", pack_num_count);
			headerMap.put("num_count", num_count);
			headerMap.put("weight_count", weight_count);
			headerMap.put("volume_count", volume_count);
			headerMap.put("fee_weight_count", fee_weight_count);
			headerMap.put("volume_weight_count", volume_weight_count);
			if(memoBuf.toString().endsWith(Constants.SPLIT_CHAR)) {
				headerMap.put("memo", memoBuf.substring(0, memoBuf.length() - 1));
			} else {
				headerMap.put("memo", memoBuf.toString());
			}
		}
		headerMap.put("balatype", 0); // 结算方式默认月结
		// 如果选择了车辆
		String pk_car = request.getParameter("pk_car");
		String pk_carrier = request.getParameter("pk_carrier");
		String req_deli_date = request.getParameter("req_deli_date");
		String req_arri_date = request.getParameter("req_arri_date");
		headerMap.put("req_deli_date", req_deli_date); 
		headerMap.put("req_arri_date", req_arri_date); 
		if(StringUtils.isNotBlank(pk_carrier)){
		//	CarrierVO carrierVO = carrService.getByPrimaryKey(CarrierVO.class, pk_carrier);
			headerMap.put("pk_carrier",pk_carrier);
		}
		
		if(StringUtils.isNotBlank(pk_car)) {
			// 根据车辆读取，承运商、车辆类型、驾驶员、车牌号、身份证号
			String[] pk_cars = pk_car.split(",");
			CarVO carVO = carService.getByPrimaryKey(CarVO.class, pk_cars[0]);
			List<SuperVO> carVOs = new ArrayList<SuperVO>();
			carVOs.add(carVO);
			// 执行下公式
			List<Map<String, Object>> retList = FormulaHelper.execFormulaForSuperVO(carVOs, getCarFormulas(), false);
			Map<String, Object> retMap = retList.get(0);
			if(StringUtils.isNotBlank(carVO.getPk_driver())){
				DriverVO driverVO = NWDao.getInstance().queryByCondition(DriverVO.class, "pk_driver=?", carVO.getPk_driver());
				if(driverVO !=null){
					retMap.put("pk_driver", driverVO.getDriver_code());
					retMap.put("driver_name", driverVO.getPk_driver());
					retMap.put("driver_mobile", driverVO.getMobile());
					retMap.put("certificate_id", driverVO.getId());
				}
			}
			headerMap.putAll(retMap);
		}
		request.setAttribute("headerMap", JacksonUtils.writeValueAsString(headerMap));
		if(paramVO.getFunCode().equals(FunConst.SEG_PZ_CODE)){
			return new ModelAndView("/busi/tp/pz.jsp");
		}else{
			return new ModelAndView("/busi/tp/BatchPZ.jsp");
		}
		
	}

	private String[] getCarFormulas() {
		return new String[] { "carno,pk_carrier,pk_car_type,pk_driver->getcolsvalue(ts_car,carno,pk_carrier,pk_car_type,pk_driver,pk_car,pk_car)" };
	}
	
	
	

	/**
	 * 配载页面-根据运段pk加载运段
	 */
	@RequestMapping(value = "/loadSegmentByPKs.json")
	@ResponseBody
	public Map<String, Object> loadSegmentByPKs(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		List<Map<String, Object>> retList = this.getService().loadSegmentByPKs(pk_segment);
		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(retList);
		return this.genAjaxResponse(true, null, pageVO);
	}

	/**
	 * 配载页面-根据运段pk加载路线信息
	 */
	@RequestMapping(value = "/loadLineInfo.json")
	@ResponseBody
	public Map<String, Object> loadLineInfo(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		List<Map<String, Object>> retList = this.getService().loadLineInfo(pk_segment);
		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(retList);
		return this.genAjaxResponse(true, null, pageVO);
	}

	/**
	 * 加载运力信息
	 */
	@RequestMapping(value = "/loadEntTransbilityB.json")
	@ResponseBody
	public Map<String, Object> loadEntTransbilityB(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		List<Map<String, Object>> retList = this.getService().loadTransbilityB(pk_segment);
		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(retList);
		return this.genAjaxResponse(true, null, pageVO);
	}

	/**
	 * 配载页面-根据承运商、运输方式、路线信息匹配合同，并算出费用明细
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/refreshPayDetail.json")
	@ResponseBody
	public Map<String, Object> refreshPayDetail(HttpServletRequest request, HttpServletResponse response) {
		double pack_num_count = 0;
		try {
			pack_num_count = Double.parseDouble(request.getParameter("pack_num_count"));// 总数量
		} catch(Exception e) {

		}
		int num_count = Integer.parseInt(request.getParameter("num_count"));// 总件数，不可能出现异常啊
		double fee_weight_count = Double.parseDouble(request.getParameter("fee_weight_count"));// 总计费重
		double weight_count = Double.parseDouble(request.getParameter("weight_count"));// 总重量
		double volume_count = Double.parseDouble(request.getParameter("volume_count"));// 总体积
		int node_count = Integer.parseInt(request.getParameter("node_count"));// 节点数，不可能出现异常啊
		String pk_carrier = request.getParameter("pk_carrier"); // 承运商
		String pk_trans_type = request.getParameter("pk_trans_type"); // 运输方式
		String start_addr = request.getParameter("start_addr"); // 起始地址
		String start_city = request.getParameter("start_city"); // 起始城市
		String end_addr = request.getParameter("end_addr"); // 目的地址
		String end_city = request.getParameter("end_city"); // 目的城市
		String[] pk_car_type = request.getParameterValues("pk_car_type"); // 车辆类型
		String pk_corp = request.getParameter("pk_corp");
		String req_arri_date = request.getParameter("req_arri_date");
		if(StringUtils.isBlank(pk_carrier) || StringUtils.isBlank(pk_trans_type) || StringUtils.isBlank(start_addr)
				|| StringUtils.isBlank(end_addr)) {
			throw new BusiException("匹配合同时，承运商、运输方式、起始地址、目的地址都不能为空！");
		}
		String[] invoiceVbillnoAry = request.getParameterValues("invoiceVbillnoAry");
		//根据发货单单号查询发货单的信息
		InvoiceVO invoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "vbillno = ?", invoiceVbillnoAry[0]);
		int deli_node_count = InvoiceUtils.getDeliNodeCount(invoiceVbillnoAry);
		List<Map<String, Object>> retList = this.getService().loadPayDetail(pack_num_count, num_count,
				fee_weight_count, weight_count, volume_count, node_count, deli_node_count,null, pk_carrier, pk_trans_type,
				start_addr, end_addr, start_city, end_city, pk_car_type, pk_corp, req_arri_date,invoiceVO.getUrgent_level(),
				invoiceVO.getItem_code(),invoiceVO.getPk_trans_line(),invoiceVO.getIf_return());
		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(retList == null ? new ArrayList<Map<String, Object>>() : retList); // 这里返回一个空数组，这样页面会将上次的值清空
		return this.genAjaxResponse(true, null, pageVO);
	}

	protected boolean checkBillStatusBeforeSave() {
		return false;
	}

	/**
	 * 批量配载
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/batchSave.json")
	@ResponseBody
	public Map<String, Object> batchSave(PZHeaderVO headerVO, HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] vbillnoAry = request.getParameterValues("vbillno");
		this.getService().processBatchSave(headerVO, vbillnoAry, paramVO);
		return this.genAjaxResponse(true, null, null);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ExAggEntrustVO aggVO = (ExAggEntrustVO) billVO;
		PayDetailBVO[] detailBVOs = (PayDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_PAY_DETAIL_B);
		if(detailBVOs != null && detailBVOs.length > 0) {
			int count = 0;// 运费记录
			String transFeeCode = ExpenseTypeConst.ET10;
			if(StringUtils.isNotBlank(transFeeCode)) {
				for(PayDetailBVO detailBVO : detailBVOs) {
					if(detailBVO.getStatus() == VOStatus.DELETED) {
						continue;
					}
					String pk_expense_type = detailBVO.getPk_expense_type();
					ExpenseTypeVO etVO = expenseTypeService.getByPrimaryKey(ExpenseTypeVO.class, pk_expense_type);
					if(etVO == null) {
						logger.error("主键为[" + pk_expense_type + "]的费用类型已经不存在！");
						throw new BusiException("主键为[?]的费用类型已经不存在！",pk_expense_type);
					}
					if(etVO.getCode().equals(transFeeCode)) {
						count++;
					}
					if(count > 1) {
						throw new BusiException("费用类型为[运费]的费用明细只能有一条！");
					}
				}
			}
		}

		// 校验所选运段对应的发货单不能重复
		List<String> invoiceVbillnoAry = new ArrayList<String>();
		boolean ifMergeSameInvoice = ParameterHelper.getIfMergeSameInvoice();
		SegmentVO[] segVOs = (SegmentVO[]) aggVO.getTableVO(TabcodeConst.TS_SEGMENT); // 运段信息
		if(segVOs != null) {
			for(SegmentVO segVO : segVOs) {
				if(invoiceVbillnoAry.contains(segVO.getInvoice_vbillno()) && !ifMergeSameInvoice) {
					throw new BusiException("相同发货单的运段不能一起配载！");
				} else {
					invoiceVbillnoAry.add(segVO.getInvoice_vbillno());
				}
			}
		}
	}
	
	/**
	 * 保存动作，可能是新增、修改
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/save.json")
	@ResponseBody
	public Map<String, Object> save(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeSave(billVO, paramVO);
		Map<String,Object> entMap = this.getService().save(billVO, paramVO, "");
		boolean autoConfirm = ParameterHelper.getEntrustAutoConfirm();
		if(autoConfirm){
			for(String key : entMap.keySet()){
				ExAggEntrustVO aggEntrustVO = (ExAggEntrustVO) entMap.get(key);
				EntrustVO parentVO = (EntrustVO) aggEntrustVO.getParentVO();
				List<SuperVO> toBeUpdate = entrustService.cashToPay(parentVO);
				if(toBeUpdate != null && toBeUpdate.size() > 0){
					NWDao.getInstance().saveOrUpdate(toBeUpdate);
				}
				entrustService.generateInvoice(aggEntrustVO);
			}
		}
		return this.genAjaxResponse(true, null,null);
	}
	
}
