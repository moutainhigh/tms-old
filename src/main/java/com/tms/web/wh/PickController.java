package com.tms.web.wh;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.service.wh.PickService;
import com.tms.vo.wh.OutstorageBVO;

/**
 * 分配
 * 
 * @author xuqc
 * @date 2014-3-4 下午10:37:28
 */
@Controller
@RequestMapping(value = "/wh/pick")
public class PickController extends AbsToftController {

	@Autowired
	private PickService pickService;

	public PickService getService() {
		return pickService;
	}

	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		String pk_outstorage_b = request.getParameter("pk_outstorage_b");
		if(StringUtils.isBlank(pk_outstorage_b)) {
			throw new BusiException("请先选择出库单明细再进行分配！");
		}
		// 查询出库单明细记录
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "pk_outstorage_b=?",
				pk_outstorage_b);
		request.setAttribute("childVO", JacksonUtils.writeValueAsString(childVO));
		request.setAttribute("pk_outstorage_b", pk_outstorage_b);
		return super.index(request, response);
	}

	/**
	 * 自动分配</br> 系统后台自动根据订单的明细的条件查询出库存可用数量，
	 * 然后根据订单的需求数据和可用数量，自动做分配操作（模拟用户手动操作方式）， 分配系统的可用数量。
	 * 
	 * @param request
	 * @param response
	 * @param billId
	 */
	@RequestMapping(value = "/autopick.json")
	@ResponseBody
	public Map<String, Object> autopick(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_outstorage_b_ary = request.getParameterValues("pk_outstorage_b");
		StringBuilder result = new StringBuilder();
		int num = 0;
		if(pk_outstorage_b_ary != null && pk_outstorage_b_ary.length > 0) {
			for(String pk_outstorage_b : pk_outstorage_b_ary) {
				try {
					this.getService().doAutopick(pk_outstorage_b);
				} catch(Exception e) {
					num++;
					logger.warn("自动分配时存在异常，单据ID：" + pk_outstorage_b, e);
					result.append(e.getMessage());
				}
			}
			if(result.length() > 0) {
				if(num != pk_outstorage_b_ary.length) {
					return this.genAjaxResponse(true, null, null, "自动分配部分成功，部分不成功！");
				} else {
					return this.genAjaxResponse(false, result.toString(), null);
				}
			}
		}
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 取消分配，1、选择一行出库明细记录进行取消分配；2、选择一行分配明细进行取消分配
	 * 
	 * @param request
	 * @param response
	 * @param billId
	 */
	@RequestMapping(value = "/unpick.json")
	@ResponseBody
	public Map<String, Object> unpick(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_outstorage_b_ary = request.getParameterValues("pk_outstorage_b");
		String[] pk_pick_detail_ary = request.getParameterValues("pk_pick_detail");
		StringBuilder result = new StringBuilder();
		int num = 0;
		if(pk_outstorage_b_ary != null && pk_outstorage_b_ary.length > 0) {
			for(String pk_outstorage_b : pk_outstorage_b_ary) {
				try {
					this.getService().unpick1(pk_outstorage_b);
				} catch(Exception e) {
					num++;
					logger.warn("取消分配时存在异常，出库明细ID：" + pk_outstorage_b, e);
					result.append(e.getMessage());
				}
			}
			if(result.length() > 0) {
				if(num != pk_outstorage_b_ary.length) {
					return this.genAjaxResponse(true, null, null, "取消分配部分成功，部分不成功！");
				} else {
					return this.genAjaxResponse(false, result.toString(), null);
				}
			}
		} else if(pk_pick_detail_ary != null && pk_pick_detail_ary.length > 0) {
			for(String pk_pick_detail : pk_pick_detail_ary) {
				try {
					this.getService().unpick(pk_pick_detail);
				} catch(Exception e) {
					num++;
					logger.warn("取消分配时存在异常，分配明细ID：" + pk_pick_detail, e);
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
	
	@RequestMapping(value = "/save.json")
	@ResponseBody
	public Map<String, Object> save(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeSave(billVO, paramVO);
		billVO = this.getService().save(billVO, paramVO);
		OutstorageBVO[] childrenVOs = (OutstorageBVO[]) billVO.getChildrenVO();
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("picked_count", childrenVOs[0].getPicked_count());
		return this.genAjaxResponse(true, null, retMap);
	}
}
