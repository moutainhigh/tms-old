package org.nw.web.js;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * js，css脚本压缩类，该类需要读取loginInfo中的信息，故需要加入过滤链
 * 
 */
@Controller
@RequestMapping("/js")
public class ScriptController {

	/**
	 * 压缩轻量级js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeUlw.do")
	public ModelAndView mergeUlw(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/mergeUlw.jsp");
	}

	/**
	 * 压缩js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/merge.do")
	public ModelAndView merge(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/merge.jsp");
	}

	/**
	 * 压缩首页js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeIndex.do")
	public ModelAndView mergeIndex(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/mergeIndex.jsp");
	}

	/**
	 * 压缩桌面界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeDefault.do")
	public ModelAndView mergeDefault(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/mergeDefault.jsp");
	}

	/**
	 * 压缩单据模板初始化界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeBill.do")
	public ModelAndView mergeBill(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/mergeBill.jsp");
	}

	// //////////////////////压缩业务页面的js///////////////////////////////////
	/**
	 * 压缩运段配载界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeSto.do")
	public ModelAndView mergeSto(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/busi/mergeSto.jsp");
	}

	/**
	 * 压缩委托单界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeEnt.do")
	public ModelAndView mergeEnt(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/busi/mergeEnt.jsp");
	}

	/**
	 * 压缩应收明细界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeRece.do")
	public ModelAndView mergeRece(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/busi/mergeRece.jsp");
	}

	/**
	 * 压缩应付明细界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergePay.do")
	public ModelAndView mergePay(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/busi/mergePay.jsp");
	}

	/**
	 * 压缩签收回单界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergePod.do")
	public ModelAndView mergePod(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/busi/mergePod.jsp");
	}

	/**
	 * 压缩异常跟踪界面的js
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mergeTracking.do")
	public ModelAndView mergeTracking(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/busi/mergeTracking.jsp");
	}
}
