package com.tms.web.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.basic.util.StringUtils;
import org.nw.exception.BusiException;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.base.AddressService;

/**
 * 地址管理
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:11:10
 */
@Controller
@RequestMapping(value = "/base/addr")
public class AddressController extends AbsToftController {

	@Autowired
	private AddressService addressService;

	public AddressService getService() {
		return addressService;
	}
	//yaojiie 2015 11 19 添加经纬度获取功能
	@RequestMapping(value = "/getGeocoderLatitude.json")
	@ResponseBody
	public void getGeocoderLatitude(HttpServletRequest request, HttpServletResponse response){
		String[] pk_addressS = request.getParameterValues("pk_address");
		String errorMesages = this.getService().addLongitude(pk_addressS);
		if(StringUtils.isNotBlank(errorMesages)){
			throw new BusiException(errorMesages);
		}else{
			throw new BusiException("获取成功！");
		}
	} 

}
