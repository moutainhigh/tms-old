package com.tms.web.cm;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.utils.QueryHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.cm.AccountPeriodService;

/**
 *  账期
 * @author muyun
 *
 */
@Controller
@RequestMapping(value = "/cm/ap")
public class AccountPeriodController extends AbsToftController {

	@Autowired
	private AccountPeriodService accountPeriodService;

	public AccountPeriodService getService() {
		return accountPeriodService;
	}
	
	@RequestMapping(value = "/periodCommit.json")
	@ResponseBody
	public Map<String, Object> periodCommit(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		String pk = request.getParameter("pk");
		Map<String,String> procMsg = this.getService().periodCommit(id);
		String msg = null;
		if(procMsg != null && procMsg.size() > 0){
			msg = procMsg.get("msg");
			String type = procMsg.get("type");
			if(type.equals("1")){
				return this.genAjaxResponse(false, msg, null);
			}
		}
		ParamVO paramVO = getParamVO(request);
		paramVO.setBillId(pk);
		AggregatedValueObject billVO = this.getService().queryBillVO(paramVO);
		return this.genAjaxResponse(true, msg, this.getService().execFormula4Templet(billVO, paramVO));
	}
	
	@RequestMapping(value = "/periodUncommit.json")
	@ResponseBody
	public Map<String, Object> periodUncommit(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		String pk = request.getParameter("pk");
		Map<String,String> procMsg = this.getService().periodUncommit(id);
		String msg = null;
		if(procMsg != null && procMsg.size() > 0){
			msg = procMsg.get("msg");
			String type = procMsg.get("type");
			if(type.equals("1")){
				return this.genAjaxResponse(false, msg, null);
			}
		}
		ParamVO paramVO = getParamVO(request);
		paramVO.setBillId(pk);
		AggregatedValueObject billVO = this.getService().queryBillVO(paramVO);
		return this.genAjaxResponse(true, msg, this.getService().execFormula4Templet(billVO, paramVO));
	}

	
}
