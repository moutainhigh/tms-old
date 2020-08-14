package org.nw.web.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.service.sys.UserService;
import org.nw.utils.NWUtils;
import org.nw.vo.sys.UserVO;
import org.nw.web.AbsBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author xuqc
 * @date 2012-8-1 下午05:37:29
 */
@Controller
public class ResetPasswordController extends AbsBaseController {
	@Autowired
	private UserService userService;

	/**
	 * 转到登录页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/resetPwd.html")
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/resetPwd.jsp");
	}

	/**
	 * 发送一封已修改了密码的邮件到客户的邮箱中
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/resetPassword.json")
	@ResponseBody
	public Map<String, Object> send_reset(HttpServletRequest request, HttpServletResponse response) {
		String usercode = request.getParameter("username"); // 注意放在页面上的是username
		String verifyCode = request.getParameter("verifyCode");
		String errorMsg = "";
		// 校验验证码
		if(verifyCode == null || verifyCode.length() == 0) {
			errorMsg = "验证码不能为空！";
		} else {
			Object verifyCode_session = request.getSession().getAttribute("verifyCode");
			if(verifyCode_session != null && verifyCode_session.equals(verifyCode)) {
				UserVO userVO = userService.getByUserCode(usercode);
				if(userVO == null) {
					errorMsg = "您提起申请的用户是无效的！";
				} else {
					String email = userVO.getEmail();
					if(StringUtils.isNotBlank(email)) {
						if(NWUtils.validateEmail(email)) {
							String random_pass = NWUtils.getRandomPass(6);
							try {
								// 更新密码及发送邮件
								userService.updatePasswordAndSendMail(usercode, random_pass, email);
							} catch(Throwable e) {
								e.printStackTrace();
								errorMsg = "邮件发送失败，原因：" + e.getMessage();
							}
						} else {
							errorMsg = "您的邮箱格式不正确！";
						}
					} else {
						errorMsg = "您的账号未维护邮箱！";
					}
				}
			} else {
				errorMsg = "验证码不正确！";
			}
		}

		if(StringUtils.isBlank(errorMsg)) {
			// 操作成功
			String succMsg = "您重置密码请求已成功，请检查您的邮箱获取新密码！";
			return this.genAjaxResponse(true, succMsg, null);
		} else {
			return this.genAjaxResponse(false, errorMsg, null);
		}
	}

	public String getPrefixURL(HttpServletRequest request) {
		String schme = request.getScheme();
		String serverName = request.getServerName();
		String port = request.getServerPort() + "";
		String headStr = schme + "://" + serverName + ":" + port + "/";
		return headStr;
	}
}
