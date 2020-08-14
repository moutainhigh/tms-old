package org.nw.web.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.service.IToftService;
import org.nw.service.sys.RoleService;
import org.nw.service.sys.UserService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.RoleVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.DigestPasswordEncoder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户操作类
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/user")
public class UserController extends AbsToftController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	public IToftService getService() {
		return userService;
	}

	/**
	 * 修改登录密码
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/editPassword.json")
	@ResponseBody
	public Map<String, Object> editPassword(HttpServletRequest request, HttpServletResponse response, String pk_user,
			String distPassword, String rePassword) {
		if(StringUtils.isBlank(pk_user)) {
			throw new BusiException("请选择一个人进行重置密码!");
		}
		if(!distPassword.equals(rePassword)) {
			throw new BusiException("两次输入的密码不相同!");
		}
		UserVO userVO = userService.getByPrimaryKey(UserVO.class, pk_user);
		if(userVO == null) {
			throw new BusiException("用户不存在!");
		}
		DigestPasswordEncoder encoder = new DigestPasswordEncoder();
		String password = encoder.encodePassword(distPassword, null);
		userVO.setUser_password(password);
		userService.updateByPrimaryKey(userVO);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 根据用户返回用户当前具有的角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadRoleByUser.json")
	@ResponseBody
	public Map<String, Object> loadRoleByUser(HttpServletRequest request, HttpServletResponse response) {
		String pk_user = request.getParameter("pk_user");
		List<RoleVO> roleVOs = roleService.getRoleByUser(pk_user);
		return convertObjectToMap(roleVOs);
	}

	/**
	 * 返回没有授于用户的角色，这些角色是用户所属的组织下的角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadUnAuthorizeRoleByUser.json")
	@ResponseBody
	public Map<String, Object> loadUnAuthorizeRoleByUser(HttpServletRequest request, HttpServletResponse response) {
		String pk_user = request.getParameter("pk_user");
		List<RoleVO> roleVOs = roleService.getUnAuthorizeRoleByUser(pk_user);
		return convertObjectToMap(roleVOs);
	}

	private Map<String, Object> convertObjectToMap(List<RoleVO> roleVOs) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>(roleVOs.size());
		for(int i = 0; i < roleVOs.size(); i++) {
			RoleVO roleVO = roleVOs.get(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("text", roleVO.getRole_code() + " " + roleVO.getRole_name());
			map.put("value", roleVO.getPk_role());
			results.add(map);
		}
		Map<String, Object> finalMap = new HashMap<String, Object>();
		finalMap.put("records", results);
		return finalMap;
	}

	/**
	 * 为用户授予角色，先将用户原有的角色删除
	 * 
	 * @param pkRoles
	 * @param pkUser
	 * @return
	 */
	@RequestMapping("/addRoleToUser.json")
	@ResponseBody
	public Map<String, Object> addRoleToUser(HttpServletRequest request, HttpServletResponse response) {
		String[] pkRoleAry = request.getParameterValues("pk_role");
		String pk_user = request.getParameter("pk_user");
		roleService.addRoleToUser(pk_user, pkRoleAry);
		return this.genAjaxResponse(true, null, null);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object user_code = parentVO.getAttributeValue("user_code");
		if(user_code == null) {
			throw new BusiException("用户编码不能为空！");
		}
		try {
			UserVO userVO = userService.getByUserCode(user_code.toString());
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				// 新增的情况
				if(userVO != null) {
					if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
						throw new RuntimeException("编码已经存在！");
					}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
						throw new RuntimeException("Code already exists！");
					}
					throw new RuntimeException("编码已经存在！");
				}
			} else {
				// 修改的情况
				if(userVO != null) {
					if(!parentVO.getPrimaryKey().equals(userVO.getPk_user())) {
						if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
							throw new RuntimeException("编码已经存在！");
						}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
							throw new RuntimeException("Code already exists！");
						}
						throw new RuntimeException("编码已经存在！");
					}
				}
			}
		} catch(BusinessException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/copy.json")
	@ResponseBody
	public Map<String, Object> copy(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		AggregatedValueObject billVO = this.getService().copy(paramVO);
		Map<String, Object> map = this.getService().execFormula4Templet(billVO, paramVO);
		Map<String, Object> headerMap = (Map<String, Object>) map.get(Constants.HEADER);
		headerMap.put("user_password", null); // 拷贝时将用户密码清空
		return this.genAjaxResponse(true, null, headerMap);
	}

}
