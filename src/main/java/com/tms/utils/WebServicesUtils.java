package com.tms.utils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class WebServicesUtils {
	@SuppressWarnings("unchecked")
	public static Map<String,String> invokeWS(String wsdlUrl, String nameSpaceUri,String method, Object[] params) {
		Map<String,String> result = null;
		try {
			// 创建调用对象
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			// 调用 getMessage
			call.setOperationName(new QName(nameSpaceUri, method));
			call.setTargetEndpointAddress(new java.net.URL(wsdlUrl));
			result = (Map<String,String>) call.invoke(params);
		} catch (Exception e) {
			e.printStackTrace();
			result = new HashMap<String,String>();
			result.put("failed", e.getMessage());
		}
		return result;
	}
}
