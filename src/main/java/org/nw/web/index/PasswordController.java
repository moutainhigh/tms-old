package org.nw.web.index;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.service.sys.UserService;
import org.nw.vo.index.PasswordVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.UserVO;
import org.nw.web.AbsBaseController;
import org.nw.web.utils.DigestPasswordEncoder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登陆后框架页的密码处理类
 * 
 * @author xuqc
 * @date 2012-6-16 下午04:38:40
 */
@Controller
@RequestMapping(value = "/password")
public class PasswordController extends AbsBaseController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/index/passwordsetting.jsp");
	}

	@RequestMapping(value = "/change.do")
	public ModelAndView processSubmit(HttpServletRequest request, @ModelAttribute("pwd") PasswordVO pwd,
			BindingResult result) {
		if(!pwd.getNewPwd().equals(pwd.getRePwd())) {
			result.rejectValue("rePwd", "confirm_error", "两次输入的密码不相同!");
		}
		UserVO userVO = null;
		try {
			userVO = userService.getByPrimaryKey(UserVO.class, WebUtils.getLoginInfo().getPk_user());
		} catch(Exception e) {
			result.rejectValue("oldPassword", "oldPassword_error", "没有取得当前用户的密码！");
		}

		DigestPasswordEncoder encoder = new DigestPasswordEncoder();
		if(userVO.getUser_password().equals(encoder.encodePassword(pwd.getOldPwd(), null))) {
			userVO.setUser_password(encoder.encodePassword(pwd.getNewPwd(), null));
			userVO.setStatus(VOStatus.UPDATED);
			userService.updateByPrimaryKey(userVO);
		} else {
			result.rejectValue("oldPwd", "old_error", "您输入的凭证有误");
		}
		if(result.hasErrors()) {
			pwd.setNewPwd("");
			pwd.setOldPwd("");
			pwd.setRePwd("");
		} else {
			pwd.setNewPwd("");
			pwd.setOldPwd("");
			pwd.setRePwd("");
			request.setAttribute("result", "true");
		}
		return new ModelAndView("/index/passwordsetting.jsp");
	}
}
