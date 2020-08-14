package org.nw.web.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.json.JacksonUtils;
import org.nw.service.IToftService;
import org.nw.service.sys.FunService;
import org.nw.service.sys.UFunService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户自定义菜单
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/ufun")
public class UFunController extends AbsToftController {

	@Autowired
	private UFunService uFunService;

	@Autowired
	private FunService funService;

	public IToftService getService() {
		return uFunService;
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
	public List<TreeVO> getFunTree(HttpServletRequest request, HttpServletResponse response) {
		String parent_id = request.getParameter("node");
		String isRoot = request.getParameter("isRoot");
		if(StringUtils.isNotBlank(isRoot) && Boolean.parseBoolean(isRoot)) {
			parent_id = Constants.SYSTEM_CODE;
		} else {
			if(StringUtils.isBlank(parent_id)) {
				parent_id = Constants.SYSTEM_CODE;
			}
		}
		List<TreeVO> funTree = funService.getFunTree(WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo()
				.getPk_user(), parent_id);
		if(funTree == null) {
			// 返回一个null对象，js会报错
			funTree = new ArrayList<TreeVO>();
		}
		return funTree;
	}

	/**
	 * 保存用户自定义菜单的顺序，可能会修改到的是parent_id,display_order
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/saveDisplayOrder.json")
	@ResponseBody
	public Map<String, Object> saveDisplayOrder(HttpServletRequest request, HttpServletResponse response) {
		String sNodeAry = request.getParameter("nodeAry");
		if(StringUtils.isBlank(sNodeAry)) {
			return genAjaxResponse(true, null, null);
		}
		JsonNode nodeAry = JacksonUtils.readTree(sNodeAry);
		List<FunVO> funVOs = new ArrayList<FunVO>();
		for(int i = 0; i < nodeAry.size(); i++) {
			FunVO funVO = JacksonUtils.readValue(nodeAry.get(i), FunVO.class);
			funVOs.add(funVO);
		}
		uFunService.saveDisplayOrder(funVOs);
		return genAjaxResponse(true, null, null);
	}
}
