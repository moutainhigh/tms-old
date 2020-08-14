package org.nw.web.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.service.sys.UserService;
import org.nw.vo.sys.UserVO;
import org.nw.web.AbsBaseController;
import org.nw.web.utils.DigestPasswordEncoder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.constants.DataDictConst;

/**
 * 登陆控制器,注意这里的url前缀使用/c ,过滤器会检测,如果是以/c开头的,那么不进行登陆检查
 * 
 * @author xuqc
 * 
 */
@Controller
public class LoginController extends AbsBaseController {

	@Autowired
	private UserService userService;

	/**
	 * 转到登录页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/login.html")
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("service", request.getParameter("service"));
		return new ModelAndView("/login.jsp");
	}

	/**
	 * 登录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/login.do")
	public void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String user_code = request.getParameter("user_code");
		String user_password = request.getParameter("user_password");
		String service = request.getParameter("service"); // 登录后跳转的url
		if(StringUtils.isBlank(user_code) || StringUtils.isBlank(user_password)) {
			request.getRequestDispatcher("/login.html").forward(request, response);
			return;
		}
		String loginDate = request.getParameter("loginDate");
		if(StringUtils.isBlank(loginDate)) {
			loginDate = DateUtils.getCurrentDate();
		}
		UserVO userVO = userService.getByUserCode(user_code);
		if(userVO == null) {
			request.setAttribute("errorMsg", "用户不存在或已被删除！");
			request.getRequestDispatcher("/login.html").forward(request, response);
			return;
		}
		if(userVO.getLocked_flag() != null && userVO.getLocked_flag().booleanValue()) {
			request.setAttribute("errorMsg", "该用户已经被锁定！");
			request.getRequestDispatcher("/login.html").forward(request, response);
			return;
		}

		if(userVO.getAble_time() != null) {
			// 判断是否在启用时间内
			if(DateUtils.getIntervalDays(loginDate, userVO.getAble_time().toString()) > 0) {
				request.setAttribute("errorMsg", "该用户还没有启用！");
				request.getRequestDispatcher("/login.html").forward(request, response);
				return;
			}
		}
		if(userVO.getDisable_time() != null) {
			// 判断是否在启用时间内
			if(DateUtils.getIntervalDays(loginDate, userVO.getDisable_time().toString()) < 0) {
				request.setAttribute("errorMsg", "该用户已经失效！");
				request.getRequestDispatcher("/login.html").forward(request, response);
				return;
			}
		}

		DigestPasswordEncoder encoder = new DigestPasswordEncoder();
		String encodePassword = encoder.encodePassword(user_password, null);// 密码加密
		
		// 初始化登录信息
		WebUtils.initLoginEnvironment(request);
		
		if(!encodePassword.equals(userVO.getUser_password())) {
			// 验证不通过
			request.setAttribute("errorMsg", "密码错误！");
			//request.getRequestDispatcher("/login.html").forward(request, response);
			
			if(userVO.getPlatform_type() == null || userVO.getPlatform_type() == DataDictConst.PLATFORM_TYPE.TMS.intValue()){
				request.getRequestDispatcher("/login.html").forward(request, response);
			}else if(userVO.getPlatform_type() == DataDictConst.PLATFORM_TYPE.RF.intValue()){
				request.getRequestDispatcher("/rflogin.jsp").forward(request, response);
			}
			
			return;
		}
		
		if(StringUtils.isBlank(service)) {
			if(userVO.getPlatform_type() == null || userVO.getPlatform_type() == DataDictConst.PLATFORM_TYPE.TMS.intValue()){
				service = "index.html";
			}else if(userVO.getPlatform_type() == DataDictConst.PLATFORM_TYPE.RF.intValue()){
				service = "rfIndex.html";
			}
		}
		response.sendRedirect(service);
	}

	/**
	 * 登录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/ajaxLogin.json")
	@ResponseBody
	public Map<String, Object> ajaxLogin(HttpServletRequest request, HttpServletResponse response) {
		String user_code = request.getParameter("user_code");
		String user_password = request.getParameter("user_password");
		String service = request.getParameter("service"); // 登录后跳转的url
		if(StringUtils.isBlank(user_code) || StringUtils.isBlank(user_password)) {
			return this.genAjaxResponse(false, "用户和密码都不能为空！", null);
		}

		boolean useVerifyCode = Global.getBooleanValue("login.useVerifyCode");
		if(useVerifyCode) {
			String verifyCode = request.getParameter("verify_code");
			if(StringUtils.isBlank(verifyCode)) {
				return this.genAjaxResponse(false, "验证码不能为空！", null);
			}
			Object verifyCode_session = request.getSession().getAttribute("verifyCode");
			if(verifyCode_session == null || !verifyCode_session.equals(verifyCode)) {
				return this.genAjaxResponse(false, "验证码不正确！", null);
			}
		}

		String loginDate = request.getParameter("loginDate");
		if(StringUtils.isBlank(loginDate)) {
			loginDate = DateUtils.getCurrentDate();
		}
		UserVO userVO = userService.getByUserCode(user_code);
		if(userVO == null) {
			return this.genAjaxResponse(false, "用户不存在或已被删除！", null);
		}
		if(userVO.getLocked_flag() != null && userVO.getLocked_flag().booleanValue()) {
			return this.genAjaxResponse(false, "该用户已经被锁定！", null);
		}

		if(userVO.getAble_time() != null) {
			// 判断是否在启用时间内
			if(DateUtils.getIntervalDays(loginDate, userVO.getAble_time().toString()) > 0) {
				return this.genAjaxResponse(false, "该用户还没有启用！", null);
			}
		}
		if(userVO.getDisable_time() != null) {
			// 判断是否在启用时间内
			if(DateUtils.getIntervalDays(loginDate, userVO.getDisable_time().toString()) < 0) {
				return this.genAjaxResponse(false, "该用户已经失效！", null);
			}
		}

		DigestPasswordEncoder encoder = new DigestPasswordEncoder();
		String encodePassword = encoder.encodePassword(user_password, null);// 密码加密
		if(!encodePassword.equals(userVO.getUser_password())) {
			// 验证不通过
			return this.genAjaxResponse(false, "密码错误！", null);
		}

		// 初始化登录信息
		WebUtils.initLoginEnvironment(request);
		Map<String, Object> retMap = new HashMap<String, Object>();// 返回数据
		retMap.put("service", service);
		return this.genAjaxResponse(true, null, retMap);
	}

	/**
	 * 登出
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout.do")
	public ModelAndView doLogout(HttpServletRequest request, HttpServletResponse response) {
		// 销毁登录信息
		WebUtils.clearLoginInfo();
		request.setAttribute("service", request.getParameter("service"));
		return login(request, response);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
