/**
 * 
 */
package com.tms.web.inv;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.inv.OrderlotService;

/**
 * 订单批次管理
 * 
 * @author xuqc
 * @Date 2015年6月9日 下午9:09:50
 *
 */
@Controller
@RequestMapping(value = "/inv/orderlot")
public class OrderlotController extends AbsBillController {

	@Autowired
	OrderlotService orderlotService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.web.AbsBillController#getService()
	 */
	@Override
	public OrderlotService getService() {
		return orderlotService;
	}

	/**
	 * 根据现有的发货单重算金额
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/recompute.json")
	@ResponseBody
	public Map<String, Object> recompute(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = getParamVO(request);
		String lot = request.getParameter("lot");
		if(StringUtils.isBlank(lot)) {
			throw new BusiException("批次号参数不能为空！");
		}
		String[] invoice_vbillnoAry = request.getParameterValues("invoice_vbillnoAry");
		return this.genAjaxResponse(true, null, this.getService().doRecompute(paramVO, lot, invoice_vbillnoAry));
	}
	
	@RequestMapping(value = "/show.json")
	@ResponseBody
	public Map<String, Object> show(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		AggregatedValueObject billVO = this.getService().show(paramVO);
		Map<String, Object> result = this.getService().execFormula4Templet(billVO, paramVO, true, true);
		this.getService().processAfterExecFormula(result);
		this.getService().setGoodsInfo(result);
		return this.genAjaxResponse(true, null, result);
	}
	
	@RequestMapping(value = "/save.json")
	@ResponseBody
	public Map<String, Object> save(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeSave(billVO, paramVO);
		billVO = this.getService().save(billVO, paramVO);
		Map<String,Object> result = this.getService().execFormula4Templet(billVO, paramVO, true, false);
		this.getService().setGoodsInfo(result);
		return this.genAjaxResponse(true, null, result);
	}
}
