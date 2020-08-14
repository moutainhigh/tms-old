package org.nw.basic.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.web.utils.WebUtils;

public class AjaxUtils {

	/**
	 * 页面操作后的返回信息，该信息会被页面捕获，用于操作提示
	 * 
	 * @param result
	 *            操作成功与否
	 * @param msg
	 *            返回操作后的信息，默认操作成功，一般操作失败会抛出异常信息
	 * @param obj
	 *            操作返回的对象，一般是ajax操作后返回的数据对象
	 * @param append
	 *            此字段特定场合用作业务的查询条件 页面可能需要其他的提示信息，如批量操作中,有些记录没有执行成功，此时需要提示用户
	 * @return
	 */
	public static Map<String, Object> genAjaxResponse(boolean result, String msg, Object obj, Object append) {
		Map<String, Object> viewMap = new HashMap<String, Object>();
		viewMap.put("success", result);
		viewMap.put("msg", msg == null ? "操作成功" : msg);
		if(obj instanceof List || obj instanceof Object[]) { // List或者数组
			viewMap.put("datas", obj);
		} else if(obj.getClass().getName().contains("PaginationVO")) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("纯web框架的util不再支持基于主键的分页处理！");
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Pure web framework util are no longer supported on the primary key based paging processing!");
			}
		} else {
			viewMap.put("data", obj);
		}
		if(append != null) {
			viewMap.put("append", append);
		}
		return viewMap;
	}

	/**
	 * 页面操作后的返回信息，该信息会被页面捕获，用于操作提示
	 * 
	 * @param result
	 *            操作成功与否
	 * @param msg
	 *            返回操作后的信息，默认操作成功，一般操作失败会抛出异常信息
	 * @param obj
	 *            操作返回的对象，一般是ajax操作后返回的数据对象
	 * @return
	 */
	public static Map<String, Object> genAjaxResponse(boolean result, String msg, Object obj) {
		return genAjaxResponse(result, msg, obj, null);
	}

}
