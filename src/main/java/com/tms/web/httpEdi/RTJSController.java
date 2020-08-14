package com.tms.web.httpEdi;

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
import com.tms.service.httpEdi.RTJSService;

/**
 * 微信HTTP接口
 * 
 * @author XIA
 * @date 2016 6 13
 */
@Controller
@RequestMapping(value = "/public/httpEdi/rtjs")
public class RTJSController extends AbsBillController {
	
	
	@Autowired
	private RTJSService rTjsService;
	
	public RTJSService getService() {
		return rTjsService;
	}

	public static final String PK_CORP = "ad21c21cc30c4df8a341bbfb15525ee4";
	public static final String PK_USER = "32e6103e697f44b7ac98477583af49cd";
	public static final String PK_CUST_BALA = "25c2da0b72de4ea7a1eece71f69f02c8";

	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/billSearch.do")
	@ResponseBody
	public Map<String,Object> billSearch(HttpServletRequest request, HttpServletResponse response) {
		
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return this.genAjaxResponse(false, null, error);
		}
		String jsonData = request.getParameter("jsonParam");
		Map<String,Object> searchKeys =  new HashMap<String,Object>();
		if(StringUtils.isNotBlank(jsonData)){
			try {
				searchKeys =  JacksonUtils.readValue(jsonData, Map.class);
			} catch (Exception e) {
				return this.genAjaxResponse(false, null, "json格式错误！");
			}
		}
		List<Map<String,Object>> result = this.getService().billSearch(searchKeys);
		return this.genAjaxResponse(true, null, result);
	}

	
	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/billImport.do")
	@ResponseBody
	public Map<String,Object> billImport(HttpServletRequest request, HttpServletResponse response) {
		
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return this.genAjaxResponse(false, null, error);
		}
		String jsonData = request.getParameter("jsonParam");
		if(StringUtils.isBlank(jsonData)){
			return this.genAjaxResponse(false, null, "没有json数据！");
		}
		
		List<Map<String,String>> jsons =  JacksonUtils.readValue(jsonData, List.class);
		Map<String,Object> result = this.getService().billImport(jsons);
		return this.genAjaxResponse(true, null, result);
	}
	
	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/billDelete.do")
	@ResponseBody
	public Map<String,Object> billDelete(HttpServletRequest request, HttpServletResponse response) {
		
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return this.genAjaxResponse(false, null, error);
		}
		String jsonData = request.getParameter("jsonParam");
		if(StringUtils.isBlank(jsonData)){
			return this.genAjaxResponse(false, null, "没有json数据！");
		}
		
		List<Map<String,String>> jsons =  JacksonUtils.readValue(jsonData, List.class);
		Map<String,Object> result = this.getService().billDelete(jsons);
		return this.genAjaxResponse(true, null, result);
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
