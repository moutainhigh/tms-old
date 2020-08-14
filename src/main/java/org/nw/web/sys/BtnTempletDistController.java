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
import org.nw.service.sys.BtnTempletDistService;
import org.nw.service.sys.FunService;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.BtnTempletDistVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 按钮模板分配
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/btntempletdist")
public class BtnTempletDistController extends AbsToftController {

	@Autowired
	private BtnTempletDistService btnTempletDistService;

	@Autowired
	private FunService funService;

	public IToftService getService() {
		return btnTempletDistService;
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
		List<TreeVO> funTree = funService.getFunTreeWithBtn(WebUtils.getLoginInfo().getPk_corp(), WebUtils
				.getLoginInfo().getPk_user(), Constants.SYSTEM_CODE);
		if(funTree == null) {
			// 返回一个null对象，js会报错
			funTree = new ArrayList<TreeVO>();
		}
		return funTree;
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
		return genAjaxResponse(true, null, btnTempletDistService.loadTemplet(pk_fun));
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
		if(StringUtils.isBlank(pk_fun)) {
			throw new BusiException("请先选择功能菜单！");
		}
		return this.genAjaxResponse(true, null, btnTempletDistService.loadTempletDist(pk_fun));
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
		if(StringUtils.isBlank(pk_fun)) {
			throw new BusiException("请先选择功能节点！");
		}
		String sTempletAry = request.getParameter("templetAry");
		if(StringUtils.isBlank(sTempletAry)) {
			return genAjaxResponse(true, null, null);
		}
		JsonNode templetAry = JacksonUtils.readTree(sTempletAry);
		List<BtnTempletDistVO> distVOs = new ArrayList<BtnTempletDistVO>();
		for(int i = 0; i < templetAry.size(); i++) {
			BtnTempletDistVO templetDistVO = JacksonUtils.readValue(templetAry.get(i), BtnTempletDistVO.class);
			distVOs.add(templetDistVO);
		}
		btnTempletDistService.saveTempletDist(distVOs, pk_fun);
		return genAjaxResponse(true, null, null);
	}
}
