package org.nw.web.sys.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.service.sys.AlarmService;
import org.nw.service.sys.FunService;
import org.nw.vo.sys.AlarmVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 预警信息处理接口,一般不需要进行权限限制
 * 
 * @author xuqc
 * @date 2013-9-19 上午10:32:17
 */
@Controller
@RequestMapping(value = "/common/alarm")
public class AlarmController extends AbsToftController {

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private FunService funService;

	public AlarmService getService() {
		return alarmService;
	}

	/**
	 * 跳转到代办事项页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/goTodo.html")
	public void goTodo(HttpServletRequest request, HttpServletResponse response) {
		String pk_alarm = request.getParameter("pk_alarm");
		if(StringUtils.isBlank(pk_alarm)) {
			throw new BusiException("主键不能为空！");
		}
		AlarmVO alarmVO = this.getService().getByPrimaryKey(AlarmVO.class, pk_alarm);
		// 查询功能节点vo
		FunVO funVO = (FunVO) funService.getByCode(alarmVO.getFun_code());
		String uri = funVO.getClass_name();
		if(StringUtils.isBlank(uri)) {
			throw new BusiException("没有定义请求的URL地址，无法跳转！");
		}
		if(uri.indexOf("?") == -1) {
			uri += "?";
		} else {
			uri += "&";
		}
		uri += "funCode=" + alarmVO.getFun_code();
		uri += "&billId=" + alarmVO.getPk_bill();
		String contextPath = request.getContextPath();
		try {
			// 跳转
			// request.getRequestDispatcher(uri).forward(request, response);
			response.sendRedirect(contextPath + uri);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
