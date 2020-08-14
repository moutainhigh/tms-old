package org.nw.web.portlet;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.service.sys.SmsService;
import org.nw.vo.sys.SmsVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 站内信服务
 * 
 * @author xuqc
 * @date 2013-7-1 上午11:30:41
 */
@Controller
@RequestMapping(value = "/c/sms")
public class SmsViewController extends AbsToftController {

	@Autowired
	private SmsService smsService;

	public SmsService getService() {
		return smsService;
	}

	/**
	 * 查询最新的5条记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTop5.html")
	public ModelAndView getTop5(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, Object>> vos = this.getService().getTop5();
		request.setAttribute("dataList", vos);
		return new ModelAndView("/default/autoLoad/sms.jsp");
	}

	/**
	 * 返回当前登陆用户的总消息数
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCount.json")
	@ResponseBody
	public Map<String, Object> getCount(HttpServletRequest request, HttpServletResponse response) {
		return this.genAjaxResponse(true, null, this.getService().getCount());
	}

	/**
	 * 更新已读标记
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateReadFlag.json")
	@ResponseBody
	public Map<String, Object> updateReadFlag(HttpServletRequest request, HttpServletResponse response) {
		String pk_sms = request.getParameter("pk_sms");
		if(StringUtils.isBlank(pk_sms)) {
			throw new BusiException("更新已读标记时主键不能为空！");
		}
		SmsVO vo = this.getService().updateReadFlag(pk_sms);
		return this.genAjaxResponse(true, null, vo);
	}

}
