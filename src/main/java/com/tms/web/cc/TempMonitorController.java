package com.tms.web.cc;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.cc.TempMonitorService;
import com.tms.vo.te.EntLotTrackingBVO;

/**
 * 冷链-温度监控
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:11:10
 */
@Controller
@RequestMapping(value = "/cc/tm")
public class TempMonitorController extends AbsToftController {

	@Autowired
	private TempMonitorService tempMonitorService;

	public TempMonitorService getService() {
		return tempMonitorService;
	}
	
	@RequestMapping(value = "/getAjaxData.json")
	@ResponseBody
	public Map<String,Object> getAjaxData(HttpServletRequest request, HttpServletResponse response){
		String[] ids = request.getParameterValues("ids");
		EntLotTrackingBVO[] dataList = this.getService().getAjaxData(ids);
		return this.genAjaxResponse(true, null, dataList);
	}
	
	
	
}
