package com.tms.web.wh;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.exception.BusiException;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.wh.ShipService;

/**
 * 发货
 * 
 * @author xuqc
 * @date 2014-3-4 下午10:37:28
 */
@Controller
@RequestMapping(value = "/wh/ship")
public class ShipController extends AbsBillController {

	@Autowired
	private ShipService shipService;

	public ShipService getService() {
		return shipService;
	}

	/**
	 * 发货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/ship.json")
	@ResponseBody
	public Map<String, Object> ship(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_outstorage_b_ary = request.getParameterValues("pk_outstorage_b");
		String[] pk_pick_detail_ary = request.getParameterValues("pk_pick_detail");
		StringBuilder result = new StringBuilder();
		int num = 0;
		if(pk_outstorage_b_ary != null && pk_outstorage_b_ary.length > 0) {
			for(String pk_outstorage_b : pk_outstorage_b_ary) {
				try {
					this.getService().ship(pk_outstorage_b);
				} catch(Exception e) {
					num++;
					logger.warn("发货时存在异常，出库明细ID：" + pk_outstorage_b, e);
					result.append(e.getMessage());
				}
			}
			if(result.length() > 0) {
				if(num != pk_outstorage_b_ary.length) {
					return this.genAjaxResponse(true, null, null, "发货部分成功，部分不成功！");
				} else {
					return this.genAjaxResponse(false, result.toString(), null);
				}
			}
		} else if(pk_pick_detail_ary != null && pk_pick_detail_ary.length > 0) {
			for(String pk_pick_detail : pk_pick_detail_ary) {
				try {
					this.getService().ship1(pk_pick_detail, true);
				} catch(Exception e) {
					num++;
					logger.warn("发货时存在异常，分配明细ID：" + pk_pick_detail, e);
					result.append(e.getMessage());
				}
			}
			if(result.length() > 0) {
				if(num != pk_pick_detail_ary.length) {
					return this.genAjaxResponse(true, null, null, result.toString());
				} else {
					return this.genAjaxResponse(false, result.toString(), null);
				}
			}
		} else {
			throw new BusiException("请选择一行明细记录进行取消分配！");
		}
		return this.genAjaxResponse(true, null, null);
	}

}
