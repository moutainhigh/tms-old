package com.tms.web.httpEdi;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.ApiException;
import org.nw.json.JacksonUtils;
import org.nw.utils.HttpUtils;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.tms.service.httpEdi.WeChatService;

/**
 * 微信HTTP接口
 * 
 * @author XIA
 * @date 2016 6 13
 */
@Controller
@RequestMapping(value = "/public/httpEdi/weChat")
public class WeChatController extends AbsBillController {
	
	private static final String SPLITCHAR = "\\|";

	@Autowired
	private WeChatService weChatService;

	public WeChatService getService() {
		return weChatService;
	}


	/**
	 * 配载页面-根据运段pk加载运段
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/searchOrders.json")
	@ResponseBody
	public Map<String, Object> searchOrders(HttpServletRequest request, HttpServletResponse response) {
		
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return this.genAjaxResponse(true, null, error);
		}

		//以param形式打包传递数据
		String param = request.getParameter("param");//打包传递JSON 参数
		String cond = "";
		if(StringUtils.isNotBlank(param)){
			Map<String, String> paramMap = new HashMap<String, String>();
			try {
				
				paramMap = JacksonUtils.readValue(new String(param.getBytes("iso-8859-1"),"UTF-8"), Map.class);
			} catch (UnsupportedEncodingException e) {
				return this.genAjaxResponse(true, null, "'param'解码出错 :" + e.getMessage());
			}
			if(paramMap == null){
				return this.genAjaxResponse(true, null, "'param'解析出错，没有解析到参数，请检查数据！");
			}
			cond = getViewLoadDataCondition(paramMap);
			
		}
		List<Map<String, Object>> retList = this.getService().searchOrders(cond);
		return this.genAjaxResponse(true, null, retList);
	}
	
	/**
	 * 接受两种数据类型，String Integer
	 * @author XIA
	 * @param paramMap
	 * @return
	 */
	private String getViewLoadDataCondition(Map<String,String> paramMap){
		if(paramMap == null || paramMap.size() == 0 ){
			return "";
		}
		String cond = "";
		for(String key : paramMap.keySet()){
			if(StringUtils.isNotBlank(paramMap.get(key))){
				String[] values = paramMap.get(key).split(SPLITCHAR);
				//对于提货和到货时间，需要特殊处理。
				if("start_deli_date".equalsIgnoreCase(key)){
					cond += "req_deli_date >= '" + values[0]+ "' and ";
				}else if("end_deli_date".equalsIgnoreCase(key)){
					cond += "req_deli_date <= '" + values[0]+ "' and ";
				}else{
					if(values.length == 1){
						cond += (key + " = '" + paramMap.get(key) + "' and ");
					}else{
						cond += (key + " in( ");
						for(String value : values){
							cond += ("'"+value+"',");
						}
						cond = cond.substring(0,cond.length()-1) +  ") and ";
					}
				}
			}
		}
		if(cond.length() > 0){
			cond = cond.substring(0, cond.length()-4);
		}
		return cond;
	}
	
	/**
	 * 配载页面-根据运段pk加载运段
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/searchOrdersCount.json")
	@ResponseBody
	public Map<String, Object> searchOrdersCount(HttpServletRequest request, HttpServletResponse response) {
		
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return this.genAjaxResponse(true, null, error);
		}
		//以param形式打包传递数据
		String param = request.getParameter("param");//打包传递JSON 参数
		String corp_codes = "";
		String cust_codes = "";
		if(StringUtils.isNotBlank(param)){
			Map<String, String> paramMap = new HashMap<String, String>();
			try {
				paramMap = JacksonUtils.readValue(new String(param.getBytes("iso-8859-1"),"UTF-8"), Map.class);
			} catch (UnsupportedEncodingException e) {
				return this.genAjaxResponse(true, null, "'param'解码出错 :" + e.getMessage());
			}
			if(paramMap == null){
				return this.genAjaxResponse(true, null, "'param'解析出错，没有解析到参数，请检查数据！");
			}
			if(StringUtils.isNotBlank(paramMap.get("cust_code"))){
				String[] cust_codeArr = paramMap.get("cust_code").split(SPLITCHAR);
				if(cust_codeArr != null && cust_codeArr.length > 0){
					for(String cust_code : cust_codeArr){
						cust_codes +=( "'" + cust_code + "'," );
					}
					cust_codes = cust_codes.substring(0, cust_codes.length()-1);
				}
			}
 			if(StringUtils.isNotBlank(paramMap.get("pk_corp"))){
				String[] corp_codeArr = paramMap.get("pk_corp").split(SPLITCHAR);
				if(corp_codeArr != null && corp_codeArr.length > 0){
					for(String corp_code : corp_codeArr){
						corp_codes += ( "'" + corp_code + "'," );
					}
					corp_codes = corp_codes.substring(0, corp_codes.length()-1);
				}
			}
		}
		List<Map<String, Object>> retList = this.getService().searchOrdersCount(corp_codes, cust_codes);
		return this.genAjaxResponse(true, null, retList);
	}
	
	
	private String authentication(String uid,String pwd){
		try {
			return HttpUtils.authentication(uid, pwd);
		} catch (ApiException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
}
