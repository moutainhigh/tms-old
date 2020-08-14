package org.nw.web.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.service.IToftService;
import org.nw.service.sys.FunService;
import org.nw.service.sys.RoleService;
import org.nw.service.sys.TempletDistService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.TempletDistVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 模板分配
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/templetdist")
public class TempletDistController extends AbsToftController {

	@Autowired
	private TempletDistService templetDistService;

	@Autowired
	private FunService funService;

	@Autowired
	private RoleService roleService;

	public IToftService getService() {
		// 这里事务使用spring自动管理，service必须从springbean管理器中取得
		return templetDistService;
	}

	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = getParamVO(request);
		return new ModelAndView(getFunHelpName(paramVO.getFunCode()));
	}

	/**
	 * 查询当前用户授权的菜单
	 * 
	 * @param request
	 * @param response
	 * @param parentFunCode
	 *            父级节点编码
	 * @return
	 */
	@RequestMapping(value = "/getFunTree.json")
	@ResponseBody
	public TreeVO[] getFunTree(HttpServletRequest request, HttpServletResponse response) {
		TreeVO[] funTree = funService.getFunTree(WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo()
				.getPk_user(), Constants.SYSTEM_CODE, true);
		if(funTree == null) {
			// 返回一个null对象，js会报错
			funTree = new TreeVO[0];
		}
		return funTree;
	}

	/**
	 * 返回当前用户所在公司的角色树，实际上角色是没有上下级关系的，都作为叶子节点
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getRoleTree.json")
	@ResponseBody
	public List<TreeVO> getRoleTree(HttpServletRequest request, HttpServletResponse response) {
		List<TreeVO> roleTree = roleService.getRoleTree();
		if(roleTree == null) {
			roleTree = new ArrayList<TreeVO>();
		}
		return roleTree;
	}

	/**
	 * 查询可分配的模板，包括单据模板和查询模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadTemplet.json")
	@ResponseBody
	public Map<String, Object> loadTemplet(HttpServletRequest request, HttpServletResponse response) {
		String pk_fun = request.getParameter("pk_fun");
		if(StringUtils.isBlank(pk_fun)) {
			throw new BusiException("请先选择功能菜单！");
		}
		return genAjaxResponse(true, null, templetDistService.loadTemplet(pk_fun));
	}

	/**
	 * 查询已经分配的模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadTempletDist.json")
	@ResponseBody
	public Map<String, Object> loadTempletDist(HttpServletRequest request, HttpServletResponse response) {
		String pk_fun = request.getParameter("pk_fun");
		String pk_role = request.getParameter("pk_role");
		if(StringUtils.isBlank(pk_fun)) {
			throw new BusiException("请先选择功能菜单！");
		}
		if(StringUtils.isBlank(pk_role)) {
			throw new BusiException("请先选择一个角色！");
		}
		return this.genAjaxResponse(true, null, templetDistService.loadTempletDist(pk_fun, pk_role));
	}

	/**
	 * 保存模板分配
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/saveTempletDist.json")
	@ResponseBody
	public Map<String, Object> saveTempletDist(HttpServletRequest request, HttpServletResponse response) {
		String pk_fun = request.getParameter("pk_fun");
		String pk_role = request.getParameter("pk_role");
		if(StringUtils.isBlank(pk_fun) || StringUtils.isBlank(pk_role)) {
			throw new BusiException("请先选择功能节点和角色！");
		}
		String sTempletAry = request.getParameter("templetAry");
		if(StringUtils.isBlank(sTempletAry)) {
			return genAjaxResponse(true, null, null);
		}
		JsonNode templetAry = JacksonUtils.readTree(sTempletAry);
		List<TempletDistVO> distVOs = new ArrayList<TempletDistVO>();
		for(int i = 0; i < templetAry.size(); i++) {
			TempletDistVO templetDistVO = JacksonUtils.readValue(templetAry.get(i), TempletDistVO.class);
			distVOs.add(templetDistVO);
		}
		templetDistService.saveTempletDist(distVOs, pk_fun, pk_role);
		return genAjaxResponse(true, null, null);
	}
}
