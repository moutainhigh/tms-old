package org.nw.web.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.service.IToftService;
import org.nw.service.sys.RoleService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.PortletPlanVO;
import org.nw.vo.sys.RoleVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 角色管理
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/role")
public class RoleController extends AbsToftController {

	@Autowired
	private RoleService roleService;

	public IToftService getService() {
		return roleService;
	}

	/**
	 * 返回权限树，如果有权限的节点是checked状态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadPowerFun.json")
	@ResponseBody
	public List<TreeVO> loadPowerFun(HttpServletRequest request, HttpServletResponse response) {
		String pk_role = request.getParameter("pk_role");
		if(StringUtils.isBlank(pk_role)) {
			throw new BusiException("请先选择一个角色!");
		}
		// XXX 20150508 权限分配是一次加载所有节点
		String isRoot = request.getParameter("isRoot");
		if(!"true".equals(isRoot)) {
			return null;
		}
		return roleService.loadPowerFunTree(pk_role);
	}

	@RequestMapping(value = "/savePowerFun.json")
	@ResponseBody
	public Map<String, Object> savePowerFun(HttpServletRequest request, HttpServletResponse response) {
		String pk_role = request.getParameter("pk_role");
		if(StringUtils.isBlank(pk_role)) {
			throw new BusiException("请先选择一个角色!");
		}
		String[] pkFunAry = request.getParameterValues("pk_fun");
		roleService.savePowerFun(pk_role, pkFunAry);
		return this.genAjaxResponse(true, null, null);
	}
	
	/**
	 * 返回权限树，如果有权限的节点是checked状态
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadWorkBenchPower.json")
	@ResponseBody
	public List<TreeVO> loadWorkBenchPlan(HttpServletRequest request, HttpServletResponse response) {
		String pk_role = request.getParameter("pk_role");
		if(StringUtils.isBlank(pk_role)) {
			throw new BusiException("请先选择一个角色!");
		}
		// XXX 20150508 权限分配是一次加载所有节点
		String isRoot = request.getParameter("isRoot");
		if(!"true".equals(isRoot)) {
			return null;
		}
		//这里预设几个图形
		return roleService.loadWorkBenchPowerTree(pk_role);
	}
	
	@RequestMapping(value = "/saveWorkBenchPower.json")
	@ResponseBody
	public Map<String, Object> saveWorkBenchPower(HttpServletRequest request, HttpServletResponse response) {
		String pk_role = request.getParameter("pk_role");
		if(StringUtils.isBlank(pk_role)) {
			throw new BusiException("请先选择一个角色!");
		}
		String[] pk_WorkBenchAry = request.getParameterValues("pk_workbench");
		roleService.saveWorkBenchPower(pk_role, pk_WorkBenchAry);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 根据角色返回角色当前具有的方案
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadPlanByRole.json")
	@ResponseBody
	public Map<String, Object> loadRoleByUser(HttpServletRequest request, HttpServletResponse response) {
		String pk_role = request.getParameter("pk_role");
		List<PortletPlanVO> planVOs = roleService.getPlanByRole(pk_role);
		return convertObjectToMap(planVOs);
	}

	/**
	 * 返回没有授于用户的角色，这些角色是用户所属的组织下的角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadUnAuthorizePlanByRole.json")
	@ResponseBody
	public Map<String, Object> loadUnAuthorizeRoleByUser(HttpServletRequest request, HttpServletResponse response) {
		String pk_role = request.getParameter("pk_role");
		List<PortletPlanVO> planVOs = roleService.getUnAuthorizePlanByRole(pk_role);
		return convertObjectToMap(planVOs);
	}

	private Map<String, Object> convertObjectToMap(List<PortletPlanVO> planVOs) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>(planVOs.size());
		for(int i = 0; i < planVOs.size(); i++) {
			PortletPlanVO planVO = planVOs.get(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("text", planVO.getPlan_code() + " " + planVO.getPlan_name());
			map.put("value", planVO.getPk_portlet_plan());
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
	@RequestMapping("/addPlanToRole.json")
	@ResponseBody
	public Map<String, Object> addRoleToUser(HttpServletRequest request, HttpServletResponse response) {
		String[] pkPlanAry = request.getParameterValues("pk_plan");
		String pk_role = request.getParameter("pk_role");
		roleService.addPlanToRole(pk_role, pkPlanAry);
		return this.genAjaxResponse(true, null, null);
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object role_code = parentVO.getAttributeValue("role_code");
		if(role_code == null) {
			throw new BusiException("编码不能为空！");
		}
		try {
			RoleVO roleVO = roleService.getByRole_code(role_code.toString());
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				// 新增的情况
				if(roleVO != null) {
					if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
						throw new RuntimeException("编码已经存在！");
					}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
						throw new RuntimeException("Code already exists！");
					}
					throw new RuntimeException("编码已经存在！");
				}
			} else {
				// 修改的情况
				if(roleVO != null) {
					if(!parentVO.getPrimaryKey().equals(roleVO.getPk_role())) {
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
}
