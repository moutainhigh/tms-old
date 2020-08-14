package com.tms.web.te;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.constants.Constants;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.te.EntTransbilityService;

/**
 * 委托单的运力信息操作接口
 * 
 * @author xuqc
 * @date 2013-8-25 上午10:38:22
 */
@Controller
@RequestMapping(value = "/te/tb")
public class EntTransbilityController extends AbsToftController {

	@Autowired
	private EntTransbilityService entTransbilityService;

	public EntTransbilityService getService() {
		return entTransbilityService;
	}
	
	@RequestMapping(value = "/saveTransbility.json")
	@ResponseBody
	public void saveTransbility(HttpServletRequest request, HttpServletResponse response){
		String json = request.getParameter(Constants.APP_POST_DATA);
		String pk_entrust = request.getParameter("pk_entrust");
		//这个billVO里并没有parentVO里的信息
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		this.getService().saveTransbility(billVO,pk_entrust);
		
	}

}
