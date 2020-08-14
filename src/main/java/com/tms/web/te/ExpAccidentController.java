package com.tms.web.te;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.service.sys.DataDictService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.te.ExpAccidentService;
import com.tms.vo.te.ExpAccidentVO;

/**
 * 异常事故操作
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/te/ea")
public class ExpAccidentController extends AbsBillController {

	public static final String EXP_UNCONFIRM_TYPE_LIST = "exp_unconfirm_type_list";// 数据字典中定义应收明细反确认类型
	@Autowired
	private ExpAccidentService expAccidentService;
	@Autowired
	private DataDictService dataDictService;

	public ExpAccidentService getService() {
		return expAccidentService;
	}

	/**
	 * 根据发货单查询
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCustomerByInvoice_vbillno.json")
	@ResponseBody
	public Map<String, Object> getCustomerByInvoice_vbillno(HttpServletRequest request, HttpServletResponse response) {
		String invoice_vbillno = request.getParameter("invoice_vbillno");
		if(StringUtils.isBlank(invoice_vbillno)) {
			return this.genAjaxResponse(true, null, null);
		}
		String pk_customer = this.getService().getCustomerByInvoice_vbillno(invoice_vbillno);
		return this.genAjaxResponse(true, null, pk_customer);
	}

	/**
	 * 根据委托单查询
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCarrierByEntrust_vbillno.json")
	@ResponseBody
	public Map<String, Object> getCarrierByEntrust_vbillno(HttpServletRequest request, HttpServletResponse response) {
		String entrust_vbillno = request.getParameter("entrust_vbillno");
		if(StringUtils.isBlank(entrust_vbillno)) {
			return this.genAjaxResponse(true, null, null);
		}
		String pk_carrier = this.getService().getCarrierByEntrust_vbillno(entrust_vbillno);
		return this.genAjaxResponse(true, null, pk_carrier);
	}

	/**
	 * 登记异常事故
	 * 
	 * @param eaVO
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/addExpAccident.json")
	@ResponseBody
	public Map<String, Object> addExpAccident(ExpAccidentVO eaVO, HttpServletRequest request,
			HttpServletResponse response) {
		this.getService().addExpAccident(eaVO);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 根据发货单查询
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getByInvoice_vbillno.json")
	@ResponseBody
	public Map<String, Object> getByInvoice_vbillno(HttpServletRequest request, HttpServletResponse response) {
		String invoice_vbillno = request.getParameter("invoice_vbillno");
		if(StringUtils.isBlank(invoice_vbillno)) {
			throw new BusiException("发货单号不能为空！");
		}
		String vbillno = this.getService().getByInvoice_vbillno(invoice_vbillno);
		return this.genAjaxResponse(true, null, vbillno);
	}

	/**
	 * 根据委托单查询
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getByEntrust_vbillno.json")
	@ResponseBody
	public Map<String, Object> getByEntrust_vbillno(HttpServletRequest request, HttpServletResponse response) {
		String entrust_villno = request.getParameter("entrust_vbillno");
		if(StringUtils.isBlank(entrust_villno)) {
			throw new BusiException("委托单号不能为空！");
		}
		String vbillno = this.getService().getByEntrust_vbillno(entrust_villno);
		return this.genAjaxResponse(true, null, vbillno);
	}

	/**
	 * 撤销处理，撤销后，状态更新为待处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/revocation.json")
	@ResponseBody
	public Map<String, Object> revocation(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> retMap = this.getService().doRevocation(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量撤销处理时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未撤销成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 结案，将状态更新为已处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/finish.json")
	@ResponseBody
	public Map<String, Object> finish(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> retMap = this.getService().doFinish(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量结案时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未结案成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 撤销结案，将状态更新为处理中
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unfinish.json")
	@ResponseBody
	public Map<String, Object> unfinish(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> retMap = this.getService().doUnfinish(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量撤销结案时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未撤销结案成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 关闭，将状态更新为已关闭
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/close.json")
	@ResponseBody
	public Map<String, Object> close(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> retMap = this.getService().doClose(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量关闭时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未关闭成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
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
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(EXP_UNCONFIRM_TYPE_LIST);
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

	// 不需要检测，其他状态也可以修改，此时的修改实际上是处理
	protected boolean checkBillStatusBeforeSave() {
		return false;
	}
}
