package com.tms.web.te;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.NullNode;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.exp.ExcelImporter;
import org.nw.json.JacksonUtils;
import org.nw.service.sys.DataDictService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tms.BillStatus;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.base.CarrService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.te.EntrustService;
import com.tms.service.te.impl.EntrustExcelImporter;
import com.tms.services.peripheral.WebServicesUtils;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;

/**
 * 委托单操作
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/te/ent")
public class EntrustController extends AbsBillController {

	public static final String ENT_UNCONFIRM_TYPE_LIST = "ent_unconfirm_type_list";// 数据字典中定义应收明细反确认类型

	public static final String VENT_REASON_TYPE = "vent_reason_type";// 数据字典中定义退单原因类型

	
	@Autowired
	private DataDictService dataDictService;
	
	@Autowired
	private EntrustService entrustService;

	@Autowired
	private CarrService carrService;
	
	@Autowired
	private ExpenseTypeService expenseTypeService;

	public EntrustService getService() {
		return entrustService;
	}

	/**
	 * 根据单个的运输方式读取换算比率
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getFeeRate.json")
	@ResponseBody
	public Map<String, Object> getTransTypeRate(HttpServletRequest request, HttpServletResponse response) {
		String pk_trans_type = request.getParameter("pk_trans_type");
		if(StringUtils.isBlank(pk_trans_type)) {
			throw new BusiException("运输方式不能为空！");
		}
		String pk_carrier = request.getParameter("pk_carrier");
		String deli_city = request.getParameter("deli_city");
		String arri_city = request.getParameter("arri_city");
		UFDouble rate = carrService.getFeeRate(pk_carrier, pk_trans_type, deli_city, arri_city);
		return this.genAjaxResponse(true, null,rate);
	}

	/**
	 * 退单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/vent.json")
	@ResponseBody
	public Map<String, Object> vent(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				AggregatedValueObject billVO = this.getService().vent(paramVO);
				Map<String, Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量退单时存在异常，单据ID：" + id, e);
				num++;
				result.append(ONE_LINE_BILL_MSG.replace("$billId", "").replace("$msg", e.getMessage()));
			}
		}
		
		if(result.length() > 0) {
			result.insert(0, "以下单据未退单成功：<br/>");
			if(num == billId.length) {
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 撤销退单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unvent.json")
	@ResponseBody
	public Map<String, Object> unvent(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id);
			try {
				AggregatedValueObject billVO = this.getService().unvent(paramVO);
				Map<String, Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量撤销退单时存在异常，单据ID：" + id, e);
				num++;
				result.append(ONE_LINE_BILL_MSG.replace("$billId", id).replace("$msg", e.getMessage()));
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未撤销退单成功：<br/>");
			if(num == billId.length) {
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 根据委托单pk加载路线信息，目前用于节点到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadEntLineB.json")
	@ResponseBody
	public Map<String, Object> loadEntLineB(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> list = this.getService().loadEntLineB(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	//yaojiie 2015 12 27 添加此JSON，在节点到货时，显示这个委托单下，对应的路线包装信息
	@RequestMapping(value = "/loadEntLinePackB.json")
	@ResponseBody
	public Map<String, Object> loadEntLinePackB(HttpServletRequest request, HttpServletResponse response) {
		String pk_ent_line_b = request.getParameter("pk_ent_line_b");
		List<Map<String, Object>> list = this.getService().loadEntLinePackB(pk_ent_line_b);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 该方法与配载页面的方法一样<br/>
	 * 配载页面-根据承运商、运输方式、路线信息匹配合同，并算出费用明细
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/refreshPayDetail.json")
	@ResponseBody
	public Map<String, Object> refreshPayDetail(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
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
		String urgent_level = request.getParameter("urgent_level");
		String item_code = request.getParameter("item_code");
		String pk_trans_line = request.getParameter("pk_trans_line");
		UFBoolean if_return = request.getParameter("if_return") == null? null : UFBoolean.valueOf(request.getParameter("if_return"));
		if(StringUtils.isBlank(pk_carrier) || StringUtils.isBlank(pk_trans_type) || StringUtils.isBlank(start_addr)
				|| StringUtils.isBlank(end_addr)) {
			throw new BusiException("匹配合同时，承运商、运输方式、起始地址、目的地址都不能为空！");
		}
		List<Map<String, Object>> retList = this.getService().refreshPayDetail(pk_entrust, pack_num_count, num_count,
				fee_weight_count, weight_count, volume_count, node_count, pk_carrier, pk_trans_type, start_addr,
				end_addr, start_city, end_city, pk_car_type, pk_corp, req_arri_date,Integer.valueOf(urgent_level),item_code,pk_trans_line,if_return);
		PaginationVO pageVO = new PaginationVO();
		pageVO.setItems(retList == null ? new ArrayList<Map<String, Object>>() : retList); // 这里返回一个空数组，这样页面会将上次的值清空
		
		//yaojiie 2015 12 15 计算金额利率等信息。
		String sql  = "select inv.* from ts_ent_inv_b ei WITH(NOLOCK) left join ts_invoice inv WITH(NOLOCK)"
				+ " on ei.pk_invoice = inv.pk_invoice where isnull(ei.dr,0)=0 and isnull(inv.dr,0)=0 and ei.pk_entrust =?";
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, pk_entrust);
		CMUtils.totalCostComput(invoiceVOs);
		
		
		return this.genAjaxResponse(true, null, pageVO);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ExAggEntrustVO aggVO = (ExAggEntrustVO) billVO;
		PayDetailBVO[] detailBVOs = (PayDetailBVO[]) aggVO.getTableVO("ts_pay_detail_b");
		if(detailBVOs != null && detailBVOs.length > 0) {
			int count = 0;// 运费记录
			String transFeeCode = ExpenseTypeConst.ET10;
			if(StringUtils.isNotBlank(transFeeCode)) {
				for(PayDetailBVO detailBVO : detailBVOs) {
					// 不能修改系统生成的费用明细
					if(aggVO.getParentVO().getStatus() == VOStatus.UPDATED) {// 更新
						if(detailBVO.getSystem_create() != null && detailBVO.getSystem_create().booleanValue()) {
							if(detailBVO.getStatus() == VOStatus.DELETED) {
								throw new BusiException("您不能删除系统创建的费用明细!");
							}
						}
					}
					if(detailBVO.getStatus() != VOStatus.DELETED) {
						String pk_expense_type = detailBVO.getPk_expense_type();
						if(StringUtils.isNotBlank(pk_expense_type)) {
							ExpenseTypeVO etVO = expenseTypeService.getByPrimaryKey(ExpenseTypeVO.class,
									pk_expense_type);
							if(etVO.getCode().equals(transFeeCode)) {
								count++;
							}
							if(count > 1) {
								throw new BusiException("费用类型为[运费]的费用明细只能有一条！");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 生成入库单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/buildInstorage.json")
	@ResponseBody
	public Map<String, Object> buildInstorage(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		this.getService().buildInstorage(paramVO);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 生成出库单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/buildOutstorage.json")
	@ResponseBody
	public Map<String, Object> buildOutstorage(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		this.getService().buildOutstorage(paramVO);
		return this.genAjaxResponse(true, null, null);
	}

	public List<CircularlyAccessibleValueObject> getUpdateChildrenVOList(String tabcode,
			Class<? extends SuperVO> voClass, JsonNode updateArray, List<CircularlyAccessibleValueObject> childrenVOList) {
		if(tabcode.equals(TabcodeConst.TS_ENT_LINE_B)) {
			// 路线信息tab,前台传入的是所有的路线节点,这里强调了路线节点的顺序
			List<CircularlyAccessibleValueObject> updateList = new ArrayList<CircularlyAccessibleValueObject>();
			for(int m = 0; m < updateArray.size(); m++) {
				JsonNode updateObj = updateArray.get(m);
				if(updateObj == null || updateObj instanceof NullNode) {
					continue;
				}
				SuperVO toBeUpdate = JacksonUtils.readValue(updateObj, voClass);
				// 如果没有PK值，则是新增的记录；如果有PK值，则为修改的记录
				if(StringUtils.isBlank(toBeUpdate.getPrimaryKey())) {
					toBeUpdate.setStatus(VOStatus.NEW);
				} else {
					toBeUpdate.setStatus(VOStatus.UPDATED);
					// 遍历，将修改信息直接覆盖到原始的VO上
					for(CircularlyAccessibleValueObject childVO : childrenVOList) {
						if(toBeUpdate.getPrimaryKey().equals(((SuperVO) childVO).getPrimaryKey())) {
							Iterator<String> it = updateObj.getFieldNames();
							while(it.hasNext()) {
								String fieldName = it.next();
								((SuperVO) childVO).setAttributeValue(fieldName,
										toBeUpdate.getAttributeValue(fieldName));
							}
							toBeUpdate = (SuperVO) childVO;
						}
					}
				}
				updateList.add(toBeUpdate);
			}
			return updateList;
		}
		return super.getUpdateChildrenVOList(tabcode, voClass, updateArray, childrenVOList);
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
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(ENT_UNCONFIRM_TYPE_LIST);
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
	
	
	@RequestMapping(value = "/getVentReasonTypeList.json")
	@ResponseBody
	public Map<String, Object> getVentReasonTypeList(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(VENT_REASON_TYPE);
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
	

	/**
	 * 发送邮件到承运商的邮箱中
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/sendEntEmail.json")
	@ResponseBody
	public String sendEntEmail(HttpServletRequest request, HttpServletResponse response) {
		String[] ids = request.getParameterValues("ids");
		//调用存储过程获取邮件信息
		return this.getService().sendEntEmail(ids,"t50123");
	}
	
	/**
	 * 点击修订时，检测是否可以对该单据进行修订<br/>
	 * 1、 当委托单状态是：新建,已确认、已提货、已到货状态<br/>
	 * 2、委托单对应应付凭证状态是：新建<br/>
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
		EntrustVO entVO = this.getService().getByPrimaryKey(EntrustVO.class, billId);
		Integer vbillstatus = entVO.getVbillstatus();
		if(vbillstatus != BillStatus.ENT_CONFIRM && vbillstatus != BillStatus.ENT_DELIVERY
				&& vbillstatus != BillStatus.ENT_ARRIVAL && vbillstatus != BillStatus.NEW) {
			throw new BusiException("委托单必须是:新建,已确认、已提货已到货等状态下才能进行修订！");
		}
		// 根据发货单查询应收明细(应收凭证)
		PayDetailVO pdVO = this.getService().getPayDetailVOsByEntrustBillno(entVO.getVbillno());
		if(pdVO != null) {
			if(pdVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("委托单对应的应付凭证状态必须是[新建]才能进行修订,应收凭证单号[?]！",pdVO.getVbillno());
			}
		}
		return this.genAjaxResponse(true, null, "Y");
	}
	
	
	@RequestMapping("/upLoadBase64Image.json")
	@ResponseBody
	public boolean upLoadBase64Image(HttpServletRequest request, HttpServletResponse response) throws IOException, DecoderException {
		
		
		String id = request.getParameter("id");
		String base64 = request.getParameter("base64");
		base64 = base64.replaceAll(" ", "+");
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String certificateDir = webappPath + "certificate";
		String filePath = certificateDir + File.separator + id + ".bmp";
		return WebServicesUtils.convertBase64DataToImage(base64, filePath);
		
	}
	
	/**
	 * 同步运单状态信息
	 * @author XIA
	 * 2016 7 14 
	 * @return
	 */
	@RequestMapping("/syncExpress.json")
	@ResponseBody
	public List<Map<String,Object>> syncExpress(HttpServletRequest request, HttpServletResponse response) {
		String[] ids = request.getParameterValues("ids");
		//调用存储过程获取邮件信息
		List<Map<String,Object>> result = this.getService().syncExpress(ids);
		return result;
		
	}
	
	@RequestMapping("/receipt.json")
	@ResponseBody
	public Map<String, Object> receipt(PodVO podVO,HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] vbillnos = request.getParameterValues("vbillnos");
		if(vbillnos == null || vbillnos.length == 0) {
			throw new BusiException("请先选择要回单的记录！");
		}
		if(podVO == null){
			throw new BusiException("缺少签收信息！");
		}
		if(podVO.getAct_receipt_date() == null){
			throw new BusiException("回单时间不能为空！");
		}
		if(podVO.getAct_receipt_date().after(new UFDate(new Date()))){
			throw new BusiException("回单时间错误！");
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		StringBuffer error = new StringBuffer();
		for(String vbillno : vbillnos){
			try {
				retList.add(this.getService().receipt(vbillno,podVO,paramVO));
			} catch (Exception e) {
				error.append(vbillno).append(":").append(e.getMessage()).append("<br/>");
			}
		}
		if(error.length() == 0 ){
			return this.genAjaxResponse(true, null, retList);
		}else{
			//error.insert(0, "回单成功，但是以下单据存在错误<br/>");
			return this.genAjaxResponse(false, error.toString(), retList);
		}
		
	}
	
	@RequestMapping("/expReceipt.json")
	@ResponseBody
	public Map<String, Object> expReceipt(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_entrust = request.getParameter("pk_entrust");
		if(StringUtils.isBlank(pk_entrust)) {
			throw new BusiException("请先选择要回单的记录！");
		}
		
		String json = request.getParameter(Constants.APP_POST_DATA);
		JsonNode jsonNode = JacksonUtils.readTree(json);
		JsonNode podNode = jsonNode.get("HEADER");
		PodVO podVO = JacksonUtils.readValue(podNode, PodVO.class);
		JsonNode packBs = jsonNode.get("BODY").get("ts_ent_pack_b").get("update");
		List<EntPackBVO> packBVOs = new ArrayList<EntPackBVO>();
		for(JsonNode packB : packBs){
			EntPackBVO packBVO = JacksonUtils.readValue(packB, EntPackBVO.class);
			packBVOs.add(packBVO);
		}
		Map<String, Object> retMap = this.getService().expReceipt(pk_entrust,podVO, paramVO,packBVOs);
		return this.genAjaxResponse(true, null, retMap);
		
	}
	
	@RequestMapping("/unreceipt.json")
	@ResponseBody
	public Map<String, Object> unreceipt(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] vbillnos = request.getParameterValues("vbillnos");
		if(vbillnos == null || vbillnos.length == 0) {
			throw new BusiException("请先选择要撤销回单的记录！");
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		StringBuffer error = new StringBuffer();
		for(String vbillno : vbillnos){
			try {
				retList.add(this.getService().unReceipt(vbillno,paramVO));
			} catch (Exception e) {
				error.append(vbillno).append(":").append(e.getMessage()).append("<br/>");
			}
		}
		if(error.length() == 0 ){
			return this.genAjaxResponse(true, null, retList);
		}else{
			error.insert(0, "撤销回单成功，但是以下单据存在错误<br/>");
			return this.genAjaxResponse(false, error.toString(), retList);
		}
		
	}
	
	@RequestMapping(value = "/loadPackRecord.json")
	@ResponseBody
	public Map<String, Object> loadPackRecord(HttpServletRequest request, HttpServletResponse response){
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> result = this.getService().getPackRecord(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(result);
		return this.genAjaxResponse(true, null, paginationVO);
	}
}
