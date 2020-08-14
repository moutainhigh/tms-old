package com.tms.web.cm;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
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
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.utils.SafeCompute;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tms.constants.BillTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.base.CarrService;
import com.tms.service.cm.PayCheckSheetService;
import com.tms.service.cm.PayDetailService;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.PayCheckSheetBVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;

/**
 * 应付明细操作
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/cm/pd")
public class PayDetailController extends AbsBillController {
	public static final String PAY_METHOD = "pay_method";// 数据字典中定义收款方式的关键字
	public static final String PD_UNCONFIRM_TYPE_LIST = "pd_unconfirm_type_list";// 数据字典中定义应收明细反确认类型
	@Autowired
	private PayDetailService payDetailService;

	public PayDetailService getService() {
		return payDetailService;
	}

	@Autowired
	private DataDictService dataDictService;

	@Autowired
	private PayCheckSheetService payCheckSheetService;

	@Autowired
	private CarrService carrService;

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		PayDetailVO parentVO = (PayDetailVO) billVO.getParentVO();
		if(StringUtils.isNotBlank(parentVO.getPk_pay_detail())) {
			// 修改的情况
			PayDetailVO oriVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "pk_pay_detail=?",
					parentVO.getPk_pay_detail());
			if(oriVO.getPay_type().intValue() == PayDetailConst.ORIGIN_TYPE
					&& parentVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("不能将单据类型从[其他类型]改成[原始类型]！");
			}
		}
		if(parentVO.getStatus() == VOStatus.NEW && parentVO.getPay_type().intValue() == PayDetailConst.ORIGIN_TYPE) {
			// 新增单据不能选择原始单据
			throw new BusiException("新增单据的单据类型不能选择[原始单据]！");
		}

		// 2013-8-11 手工分摊金额的总和必须和系统分摊总金额一致
		ExAggPayDetailVO aggVO = (ExAggPayDetailVO) billVO;
		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO(TabcodeConst.TS_PAY_DEVI_B);
		if(cvos != null && cvos.length > 0) {
			UFDouble sysDeviAmount = UFDouble.ZERO_DBL, manDeviAmount = UFDouble.ZERO_DBL;
			for(CircularlyAccessibleValueObject cvo : cvos) {
				PayDeviBVO deviBVO = (PayDeviBVO) cvo;
				if(deviBVO.getStatus() != VOStatus.DELETED) {
					// 2015-3-18
					// 将系统分摊金额重新启用2个小数位数，避免数据库存的时8位，而显示的是2位，在下面的判断中出现金额不相等的情况
					// 目前在计算分摊金额的时候已经处理了，新计算的金额不会有问题，但是历史数据没有修改，这里的会对每一次修改的记录做处理
					deviBVO.setSys_devi_amount(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getSys_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setMan_devi_amount(deviBVO.getMan_devi_amount() == null ? UFDouble.ZERO_DBL : deviBVO
							.getMan_devi_amount().setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP));
					deviBVO.setStatus(VOStatus.UPDATED);

					sysDeviAmount = sysDeviAmount.add(deviBVO.getSys_devi_amount() == null ? UFDouble.ZERO_DBL
							: deviBVO.getSys_devi_amount());
					manDeviAmount = manDeviAmount.add(deviBVO.getMan_devi_amount() == null ? UFDouble.ZERO_DBL
							: deviBVO.getMan_devi_amount());
				}
			}
			if(SafeCompute.sub(sysDeviAmount, manDeviAmount).doubleValue() != 0) {
				throw new BusiException("系统分摊金额不等于手工分摊金额，请重新分配！");
			}
		}
	}

	/**
	 * 返回付款方式，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getPayMethod.json")
	@ResponseBody
	public Map<String, Object> getReceivableMethod(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(PAY_METHOD);
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
	
	
	@RequestMapping(value = "/getLot.json")
	@ResponseBody
	public Map<String, Object> getLot(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("tax_rate");
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
	 * 返回税率，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTaxRate.json")
	@ResponseBody
	public Map<String, Object> getTaxRate(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("tax_rate");
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
	 * 返回税种，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTaxCat.json")
	@ResponseBody
	public Map<String, Object> getTaxCat(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("tax_category");
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
	
	@RequestMapping(value = "/getCheckType.json")
	@ResponseBody
	public Map<String, Object> getCheckType(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("billing_type");
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
	 * 根据承运商返回发票抬头
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCheckHead.json")
	@ResponseBody
	public Map<String, Object> getCheckHead(HttpServletRequest request, HttpServletResponse response) {
		String pk_carrier = request.getParameter("pk_carrier");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(pk_carrier)) {
			logger.warn("承运商参数不能为空！");
			return retMap;
		}
		retMap.put("checkHead", carrService.getCheckHead(pk_carrier));
		return retMap;
	}

	@RequestMapping(value = "/getDefaultCheckType.json")
	@ResponseBody
	public Map<String, Object> getDefaultCheckType(HttpServletRequest request, HttpServletResponse response) {
		String pk_carrier = request.getParameter("pk_carrier");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(pk_carrier)) {
			logger.warn("承运商参数不能为空！");
			return retMap;
		}
		retMap.put("checkType", carrService.getDefaultCheckType(pk_carrier));
		return retMap;
	}
	
	/**
	 * 加载付款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadPayRecord.json")
	@ResponseBody
	public Map<String, Object> loadPayRecord(HttpServletRequest request, HttpServletResponse response) {
		String pk_pay_detail = request.getParameter("pk_pay_detail");
		if(StringUtils.isBlank(pk_pay_detail)) {
			throw new BusiException("请先选择一行应付明细记录！");
		}
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		PaginationVO paginationVO = this.getService().loadPayRecord(pk_pay_detail, paramVO, offset, pageSize);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 将应付明细加入对账单时，根据承运商查询所有新建的对账单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadPayCheckSheet.json")
	@ResponseBody
	public Map<String, Object> loadPayCheckSheet(HttpServletRequest request, HttpServletResponse response) {
		String pk_carrier = request.getParameter("pk_carrier");
		if(StringUtils.isBlank(pk_carrier)) {
			throw new BusiException("加载应付明细对账单时，承运商不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		PaginationVO paginationVO = payCheckSheetService.getByPk_carrier(pk_carrier, paramVO);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 生成对账单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/buildPayCheckSheet.json")
	@ResponseBody
	public Map<String, Object> buildPayCheckSheet(HttpServletRequest request, HttpServletResponse response) {
		String json = request.getParameter(Constants.HEADER);
		ParamVO paramVO = this.getParamVO(request);
		JsonNode header = JacksonUtils.readTree(json);
		List<String> pk_pay_details = new ArrayList<String>();
		if(header.size() > 0) {
			for(int i = 0; i < header.size(); i++){
				JsonNode obj = header.get(i);
				PayCheckSheetBVO childVO = (PayCheckSheetBVO) JacksonUtils.readValue(obj, PayCheckSheetBVO.class);
				pk_pay_details.add(childVO.getPk_pay_detail());
			}
		}
		Map<String,String> procMsg = this.getService().CheckSheetByProc(NWUtils.join(pk_pay_details.toArray(new String[pk_pay_details.size()]), ","));
		String msg = null;
		if(procMsg != null && procMsg.size() > 0){
			msg = procMsg.get("msg");
			String type = procMsg.get("type");
			if(type.equals("1")){
				return this.genAjaxResponse(false, msg, null);
			}
		}
		List<Map<String, Object>> retList = this.getService().buildPayCheckSheet(paramVO, json);
		return this.genAjaxResponse(true, msg, retList);
	}

	/**
	 * 加入对账单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/addToPayCheckSheet.json")
	@ResponseBody
	public Map<String, Object> addToPayCheckSheet(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_pay_detail = request.getParameterValues("pk_pay_detail");
		String pk_pay_check_sheet = request.getParameter("pk_pay_check_sheet");
		if(pk_pay_detail == null || pk_pay_detail.length == 0) {
			throw new BusiException("请先选择要加入对账单的应付明细！");
		}
		if(pk_pay_check_sheet == null) {
			throw new BusiException("请先选择对账单记录！");
		}
		Map<String,String> procMsg = this.getService().CheckSheetByProc(NWUtils.join(pk_pay_detail, ","));
		String msg = null;
		if(procMsg != null && procMsg.size() > 0){
			msg = procMsg.get("msg");
			String type = procMsg.get("type");
			if(type.equals("1")){
				return this.genAjaxResponse(false, msg, null);
			}
		}
		List<Map<String, Object>> retList = this.getService().addToPayCheckSheet(paramVO, pk_pay_check_sheet,
				pk_pay_detail);
		return this.genAjaxResponse(true, msg, retList);
	}

	/**
	 * 付款
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/payable.json")
	@ResponseBody
	public Map<String, Object> payable(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.HEADER);
		Map<String, Object> retMap = this.getService().doPayable(paramVO, json);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.add(retMap);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 全额付款
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/payableAll.json")
	@ResponseBody
	public Map<String, Object> payableAll(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_pay_detail = request.getParameterValues("pk_pay_detail");
		List<Map<String, Object>> retList = this.getService().doPayableAll(paramVO, pk_pay_detail);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 删除付款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deletePayRecord.json")
	@ResponseBody
	public Map<String, Object> deletePayRecord(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_pay_record = request.getParameter("pk_pay_record");
		Map<String, Object> retMap = this.getService().deletePayRecord(paramVO, pk_pay_record);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.add(retMap);
		return genAjaxResponse(true, null, retList);
	}

	/**
	 * 提交按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/commit.json")
	@ResponseBody
	public Map<String, Object> commit(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new RuntimeException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().commit(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量提交时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未提交成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 反提交按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/uncommit.json")
	@ResponseBody
	public Map<String, Object> uncommit(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> retMap = this.getService().uncommit(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量反提交时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未反提交成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}
	
	/**
	 * 重新计算合同金额
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/payDetailComputer.json")
	@ResponseBody
	public Map<String, Object> payDetailComputer(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> retList = this.getService().payDetailComputer(paramVO ,billId);
		return this.genAjaxResponse(true, null, retList);
	}
	/**
	 * 重新计算合同金额
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reComputeMny.json")
	@ResponseBody
	public Map<String, Object> reComputeMny(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				AggregatedValueObject billVO = this.getService().queryBillVO(paramVO);
				retList.add(this.getService().reComputeMny(billVO, paramVO));
			} catch(Exception e) {
				logger.warn("批量重算金额时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据重算金额未成功：<br/>");
			if(num == billId.length) {// 都失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, null, result.toString());
		} else {
			return this.genAjaxResponse(true, null, retList);
		}
	}
	
	
	//yaojiie 2015 12 29 按批次计算金额
	@RequestMapping(value = "/reComputeMnyByLots.json")
	@ResponseBody
	public Map<String, Object> reComputeMnyByLots(HttpServletRequest request, HttpServletResponse response) {
		String[] billIds = request.getParameterValues("billId");
		if(billIds == null || billIds.length == 0 || billIds[0] == null || billIds[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> retList = this.getService().reComputeMnyByLots(paramVO ,billIds);
		return this.genAjaxResponse(true, null, retList);
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
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(PD_UNCONFIRM_TYPE_LIST);
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
	 * 集货段提货段，干线段、配送段委托单重算金额
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/rebuildBySegtype.json")
	@ResponseBody
	public Map<String, Object> rebuildBySegtype(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0) {
			throw new BusiException("请先选择记录");
		}
		int seg_type = Integer.parseInt(request.getParameter("seg_type"));
		this.getService().doPayDetailRebuildBySegtype(paramVO, seg_type, billId);
		return this.genAjaxResponse(true, null, null);
	}
	
	@RequestMapping(value = "/loadPayDetail.json")
	@ResponseBody
	public Map<String, Object> loadPayDetail(HttpServletRequest request, HttpServletResponse response){
		String[] pk_pay_details = request.getParameterValues("pk_pay_detail");
		List<Map<String, Object>> list = this.getService().loadPayDetail(pk_pay_details);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
		
	}
	
	@RequestMapping(value = "/saveLotPay.json")
	@ResponseBody
	public void saveLotPay(HttpServletRequest request, HttpServletResponse response){
		String json = request.getParameter(Constants.APP_POST_DATA);
		String pk = request.getParameter("pk_pay_detail");
		String[] pdpks = pk.split("\\" + Constants.SPLIT_CHAR);
		
		List<PayDetailBVO> detailBVOs = new ArrayList<PayDetailBVO>();
		JsonNode header = JacksonUtils.readTree(json);
		JsonNode headers = header.get(Constants.BODY).get(TabcodeConst.TS_PAY_DETAIL_B).get(Constants.UPDATE);
		for(JsonNode unitHeader : headers){
			PayDetailBVO detailBVO = new PayDetailBVO();
			
			 detailBVO.setPk_expense_type(unitHeader.get("pk_expense_type").getTextValue());
			 detailBVO.setValuation_type(unitHeader.get("valuation_type") == null ? null : unitHeader.get("valuation_type").getValueAsInt());
			 detailBVO.setQuote_type(unitHeader.get("quote_type") == null ? null : unitHeader.get("quote_type").getValueAsInt());
			 detailBVO.setPrice_type(unitHeader.get("price_type") == null ? null : unitHeader.get("price_type").getValueAsInt());
			 detailBVO.setPrice(new UFDouble(unitHeader.get("price") == null ? 0 : unitHeader.get("price").getValueAsDouble()));
			 detailBVO.setAmount(new UFDouble(unitHeader.get("amount").getValueAsDouble()));
			 detailBVO.setMemo(unitHeader.get("memo") == null ? null : unitHeader.get("memo").getTextValue());
			 
			detailBVOs.add(detailBVO);
		}
		
		//这里组织数据
		this.getService().saveLotPay(detailBVOs,pdpks);
		
	}
	
	@RequestMapping(value = "/close.json")
	@ResponseBody
	public Map<String, Object> close(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
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
			throw new BusiException("billId不能为空！");
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
}
