package org.nw.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nw.BillStatus;
import org.nw.exception.BusiException;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 单据类的controller，
 * 
 * @author xuqc
 * @date 2012-7-7 上午10:49:13
 */
public abstract class AbsBillController extends AbsToftController {
	protected static String ONE_LINE_BILL_MSG = "单据ID：$billId，原因：$msg.<br/>";

	/**
	 * 抽象方法，获取单据的service实例
	 * ，由具体模块的controller实现该方法（这里覆盖了AbsBusiController中的此方法，因为返回的接口不同）<br>
	 * 一般每个模块都有自己的service，并由spring统一管理，并注入进来，当然也可以不用spring的注入，只要自己实现该方法，
	 * 并返回一个实例即可
	 * 
	 * @return
	 */
	public abstract IBillService getService();
	
	/**
	 * 确认按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/batchConfirm.json")
	@ResponseBody
	public Map<String, Object> batchConfirm(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		SuperVO[] superVOs = this.getService().batchConfirm(paramVO, billId);
		List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
		if(superVOs != null && superVOs.length == 1){
			//只有一条数据
			paramVO.setBillId(superVOs[0].getPrimaryKey());
			AggregatedValueObject billVO = this.getService().queryBillVO(paramVO);
			Map<String, Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
			results.add(retMap);
		}else{
			List<Map<String, Object>>  retMaps = this.getService().execFormula4Templet(paramVO,  Arrays.asList(superVOs));
			//将数据放到header上面。前台js需要这样的结构。
			for(Map<String, Object> retMap :retMaps ){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("HEADER", retMap);
				results.add(map);
			}
		}
		return this.genAjaxResponse(true, null,results);
	}

	/**
	 * 确认按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/confirm.json")
	@ResponseBody
	public Map<String, Object> confirm(HttpServletRequest request, HttpServletResponse response) {
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
				AggregatedValueObject billVO = this.getService().confirm(paramVO);
				Map<String, Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量确认时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未确认成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}
	
	/**
	 * 反确认按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/batchUnconfirm.json")
	@ResponseBody
	public Map<String, Object> batchUnconfirm(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		SuperVO[] superVOs = this.getService().batchUnconfirm(paramVO, billId);
		List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
		if(superVOs != null && superVOs.length == 1){
			//只有一条数据
			paramVO.setBillId(superVOs[0].getPrimaryKey());
			AggregatedValueObject billVO = this.getService().queryBillVO(paramVO);
			Map<String, Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
			results.add(retMap);
		}else{
			List<Map<String, Object>>  retMaps = this.getService().execFormula4Templet(paramVO,  Arrays.asList(superVOs));
			//将数据放到header上面。前台js需要这样的结构。
			for(Map<String, Object> retMap :retMaps ){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("HEADER", retMap);
				results.add(map);
			}
		}
		return this.genAjaxResponse(true, null,results);
	}

	/**
	 * 反确认按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unconfirm.json")
	@ResponseBody
	public Map<String, Object> unconfirm(HttpServletRequest request, HttpServletResponse response) {
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
				AggregatedValueObject billVO = this.getService().unconfirm(paramVO);
				Map<String, Object> retMap = this.getService().execFormula4Templet(billVO, paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量反确认时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未反确认成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new RuntimeException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	protected boolean checkBillStatusBeforeSave() {
		return true;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		if(checkBillStatusBeforeSave()) {
			CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
			Object vbillstatus = parentVO.getAttributeValue(this.getService().getBillStatusField());
			if(vbillstatus == null) {
				throw new BusiException("不能保存单据状态为空的单据！");
			}
			if(!paramVO.isReviseflag()) {
				if(Integer.parseInt(vbillstatus.toString()) != BillStatus.NEW
					&& Integer.parseInt(vbillstatus.toString()) != com.tms.BillStatus.YCGL_REFUSE) {
					throw new BusiException("只能编辑单据状态为[新建]的单据！");
				}
			}
		}
	}

	/**
	 * 审核
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/approve.json")
	@ResponseBody
	public Map<String, Object> approve(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		this.getService().approve(paramVO);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 审核
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unapprove.json")
	@ResponseBody
	public Map<String, Object> unapprove(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		this.getService().unapprove(paramVO);
		return this.genAjaxResponse(true, null, null);
	}
}
