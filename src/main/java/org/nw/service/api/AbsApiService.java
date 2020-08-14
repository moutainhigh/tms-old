package org.nw.service.api;

import javax.servlet.http.HttpServletRequest;

import org.nw.constants.ApiConstants;
import org.nw.exception.ApiException;
import org.nw.exception.BusiException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 开发接口service抽象类
 * 
 * @author xuqc
 * @date 2015-1-28 下午09:14:01
 */
public abstract class AbsApiService {

	@Autowired
	AuthenticationService authenticationService;

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	/**
	 * 验证用户名和密码是否合法
	 * 
	 * @param uid
	 * @param pwd
	 * @return
	 * @throws BusiException
	 */
	public String auth(HttpServletRequest request) throws ApiException {
		String uid = request.getParameter(ApiConstants.PARAM_USERNAME);
		String pwd = request.getParameter(ApiConstants.PARAM_PASSWORD);
		return authenticationService.auth(uid, pwd);
	}
}
