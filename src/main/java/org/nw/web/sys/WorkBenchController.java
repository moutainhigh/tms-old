package org.nw.web.sys;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.service.sys.WorkBenchService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.vo.base.CarrierVO;

/**
 * 工作台
 * 
 * @author XIA
 * @date 2016-6-28 下午06:23:50
 */
@Controller
@RequestMapping(value = "/workBench")
public class WorkBenchController extends AbsToftController {

	@Autowired
	private WorkBenchService workBenchService;

	public WorkBenchService getService() {
		return workBenchService;
	}
	
	/**
	 * 加载货量分析折线图
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadGoodsAmount.json")
	@ResponseBody
	public Map<String, Object> loadGoodsAmount(HttpServletRequest request, HttpServletResponse response) {
		String stratTime = request.getParameter("stratTime");
		String endTime = request.getParameter("endTime");
		String timeId = request.getParameter("timeId");
		if(StringUtils.isBlank(timeId)){
			//如果没有时间，则默认是上周
			timeId = "lastWeek";
		}
		Map<String, Object> result = workBenchService.getGoodsAmountData(stratTime, endTime, timeId);
		return result;
	}
	
	/**
	 * 加载当月和当日的货量信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadDateAmount.json")
	@ResponseBody
	public Map<String, Object> loadDateAmount(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getDateAmountData();
		return result;
	}
	
	/**
	 * 加载当月的货量按照线路统计信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadRouteAnalyze.json")
	@ResponseBody
	public Map<String, Object> loadRouteAnalyze(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getRouteAnalyze();
		return result;
	}
	
	/**
	 * 加载当月的货量按照客户统计信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadCustAnalyze.json")
	@ResponseBody
	public Map<String, Object> loadCustAnalyze(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getCustAnalyze();
		return result;
	}
	
	/**
	 * 加载费用分析折线图
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadChargeAmount.json")
	@ResponseBody
	public Map<String, Object> loadChargeAmount(HttpServletRequest request, HttpServletResponse response) {
		String stratTime = request.getParameter("stratTime");
		String endTime = request.getParameter("endTime");
		String timeId = request.getParameter("timeId");
		if(StringUtils.isBlank(timeId)){
			//如果没有时间，则默认是上周
			timeId = "lastWeek";
		}
		Map<String, Object> result = workBenchService.getChargeAmountData(stratTime, endTime,timeId);
		return result;
	}
	
	/**
	 * 加载当月和当日的费用信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadChargeDateAmount.json") 
	@ResponseBody
	public Map<String, Object> loadChargeDateAmount(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getChargeDateAmount();
		return result;
	}
	
	
	/**
	 * 加载应收统计
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadReceAnalyze.json")
	@ResponseBody
	public Map<String, Object> loadReceAnalyze(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getReceAnalyze();
		return result;
	}
	
	/**
	 * 加载应收统计
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadPayAnalyze.json")
	@ResponseBody
	public Map<String, Object> loadPayAnalyze(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getPayAnalyze();
		return result;
	}
	
	/**
	 * 加载承运商选择列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value = "/loadCarriers.json")
	@ResponseBody
	public List<CarrierVO> loadCarriers(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String keyword = request.getParameter("keyword");
		if(StringUtils.isNotBlank(keyword)){
			keyword = new String(keyword.getBytes("ISO8859-1"), "UTF-8");
		}
		List<CarrierVO> result = workBenchService.getCarriers(keyword);
		return result;
	}
	
	/**
	 * 加载KPI数据
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadKPIAmountData.json")
	@ResponseBody
	public  Map<String, Object> loadKPIAmountData(HttpServletRequest request, HttpServletResponse response) {
		String stratTime = request.getParameter("stratTime");
		String endTime = request.getParameter("endTime");
		String timeId = request.getParameter("timeId");
		if(StringUtils.isBlank(timeId)){
			//如果没有时间，则默认是上周
			timeId = "lastWeek";
		}
		String pk_carrier = request.getParameter("pk_carrier");
		Map<String, Object> result = workBenchService.getKPIAmountData(stratTime, endTime,timeId,pk_carrier);
		return result;
	}
	
	
	/**
	 * 加载KPI上周和上月数据
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadKPIDateAmount.json")
	@ResponseBody
	public  Map<String, Object> loadKPIDateAmount(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getKPIDateAmount();
		return result;
	}
	
	
	/**
	 * 加载上月的KPI按照线路统计信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadKPIRouteAnalyze.json")
	@ResponseBody
	public Map<String, Object> loadKPIRouteAnalyze(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getKPIRouteAnalyze();
		return result;
	}
	
	/**
	 * 加载上月的KPI按照承运商统计信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadKPICarrAnalyze.json")
	@ResponseBody
	public Map<String, Object> loadKPICarrAnalyze(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = workBenchService.getKPICarrAnalyze();
		return result;
	}
	

}
