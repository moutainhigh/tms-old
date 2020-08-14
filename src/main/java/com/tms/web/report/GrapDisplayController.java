package com.tms.web.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.basic.util.StringUtils;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.sys.CorpVO;
import org.nw.web.AbsReportController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.report.GrapDisplayServiceImpl;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.CustomerVO;

@Controller
@RequestMapping(value = "/report/gd")
public class GrapDisplayController extends AbsReportController {

	@Autowired
	private GrapDisplayServiceImpl GrapDisplayServiceImpl;

	public GrapDisplayServiceImpl getService() {
		return GrapDisplayServiceImpl;
	}
	
	@RequestMapping(value = "/unitCarrData.json")
	@ResponseBody
	public String unitCarrData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String year = null;
		String month = null;
		UFDate date = new UFDate();
		if(date.getMonth() == 1){
			month = "12";
			year = (date.getYear()-1)+"";
		}else{
			month = date.getMonth()+"";
			year = date.getYear()+"";
		}
		String carr_code = null;
		String corp_code = WebUtils.getLoginInfo().getPk_corp();

		List<HashMap<String,String>> data = this.getService().getCarrData(year,month,carr_code,corp_code);
		return JacksonUtils.writeValueAsString(data);
	}	
	
	@RequestMapping(value = "/unitCustData.json")
	@ResponseBody
	public String unitCustData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String year = null;
		String month = null;
		UFDate date = new UFDate();
		if(date.getMonth() == 1){
			month = "12";
			year = (date.getYear()-1)+"";
		}else{
			month = date.getMonth()+"";
			year = date.getYear()+"";
		}
		String cust_code = null;
		String corp_code = WebUtils.getLoginInfo().getPk_corp();

		List<HashMap<String,String>> data = this.getService().getCustData(year,month,cust_code,corp_code);
		return JacksonUtils.writeValueAsString(data);
	}
	
	@RequestMapping(value = "/unitEmpData.json")
	@ResponseBody
	public String unitEmpData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String year = null;
		String month = null;
		UFDate date = new UFDate();
		if(date.getMonth() == 1){
			month = "12";
			year = (date.getYear()-1)+"";
		}else{
			month = date.getMonth()+"";
			year = date.getYear()+"";
		}
		String emp_code = null;
		String corp_code = WebUtils.getLoginInfo().getPk_corp();

		List<HashMap<String,String>> data = this.getService().getEmpData(year,month,emp_code,corp_code);
		return JacksonUtils.writeValueAsString(data);
	}
	
	@RequestMapping(value = "/reloadCarrData.json")
	@ResponseBody
	public String reloadCarrData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String year = null;
		String month = null;
		if(StringUtils.isNotBlank(request.getParameter("year"))){
			year = request.getParameter("year");
		}
		if(StringUtils.isNotBlank(request.getParameter("month"))){
			month = request.getParameter("month");
		}
		String carr_code = null;
		String corp_code = WebUtils.getLoginInfo().getPk_corp();
		if(StringUtils.isNotBlank(request.getParameter("carr"))){
			carr_code = new String(request.getParameter("carr").getBytes("ISO-8859-1"),"UTF-8");
		}
		if(StringUtils.isNotBlank(request.getParameter("corp"))){
			corp_code = new String(request.getParameter("corp").getBytes("ISO-8859-1"),"UTF-8");
		}
		List<HashMap<String,String>> data = this.getService().getCarrData(year,month,carr_code,corp_code);
		return JacksonUtils.writeValueAsString(data);
	}	 
	
	@RequestMapping(value = "/reloadCustData.json")
	@ResponseBody
	public String reloadCustData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String year = null;
		String month = null;
		if(StringUtils.isNotBlank(request.getParameter("year"))){
			year = request.getParameter("year");
		}
		if(StringUtils.isNotBlank(request.getParameter("month"))){
			month = request.getParameter("month");
		}
		String cust_code = null;
		String corp_code = WebUtils.getLoginInfo().getPk_corp();
		if(StringUtils.isNotBlank(request.getParameter("cust"))){
			cust_code = new String(request.getParameter("cust").getBytes("ISO-8859-1"),"UTF-8");
		}
		if(StringUtils.isNotBlank(request.getParameter("corp"))){
			corp_code = new String(request.getParameter("corp").getBytes("ISO-8859-1"),"UTF-8");
		}
		List<HashMap<String,String>> data = this.getService().getCustData(year,month,cust_code,corp_code);
		return JacksonUtils.writeValueAsString(data);
	}	
	
	@RequestMapping(value = "/reloadEmpData.json")
	@ResponseBody
	public String reloadEmpData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String year = null;
		String month = null;
		if(StringUtils.isNotBlank(request.getParameter("year"))){
			year = request.getParameter("year");
		}
		if(StringUtils.isNotBlank(request.getParameter("month"))){
			month = request.getParameter("month");
		}
		String emp_code = null;
		String corp_code = WebUtils.getLoginInfo().getPk_corp();
		if(StringUtils.isNotBlank(request.getParameter("emp"))){
			emp_code = new String(request.getParameter("emp").getBytes("ISO-8859-1"),"UTF-8");
		}
		if(StringUtils.isNotBlank(request.getParameter("corp"))){
			corp_code = new String(request.getParameter("corp").getBytes("ISO-8859-1"),"UTF-8");
		}
		List<HashMap<String,String>> data = this.getService().getCustData(year,month,emp_code,corp_code);
		return JacksonUtils.writeValueAsString(data);
	}	
	
	
	@RequestMapping(value = "/getCarrier.json")
	@ResponseBody
	public String getCarrier(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		CarrierVO[] carrierVOs = this.getService().getCarrierByCorpCond();
		if(carrierVOs != null && carrierVOs.length > 0){
			for(CarrierVO carrierVO : carrierVOs){
				Map<String,String> dataMap = new HashMap<String, String>();
				dataMap.put("name", carrierVO.getCarr_code());
				dataList.add(dataMap);
			}
		}
		return JacksonUtils.writeValueAsString(dataList);
	}
	
	@RequestMapping(value = "/getCustomer.json")
	@ResponseBody
	public String getCustomer(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		CustomerVO[] customerVOs = this.getService().getCustomerByCorpCond();
		if(customerVOs != null && customerVOs.length > 0){
			for(CustomerVO customerVO : customerVOs){
				Map<String,String> dataMap = new HashMap<String, String>();
				dataMap.put("name", customerVO.getCust_code());
				dataList.add(dataMap);
			}
		}
		return JacksonUtils.writeValueAsString(dataList);
	}
	
	
	@RequestMapping(value = "/getCorp.json")
	@ResponseBody
	public String getCorp(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		CorpVO[] corpVOs = this.getService().getCorpByCorpCond();
		if(corpVOs != null && corpVOs.length > 0){
			for(CorpVO corpVO : corpVOs){
				Map<String,String> dataMap = new HashMap<String, String>();
				dataMap.put("name", corpVO.getCorp_code());
				dataList.add(dataMap);
			}
		}
		return JacksonUtils.writeValueAsString(dataList);
	}	
}
