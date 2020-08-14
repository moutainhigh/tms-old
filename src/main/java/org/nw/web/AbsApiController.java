package org.nw.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Node;
import org.nw.vo.api.RootVO;
import org.nw.xml.BeanXmlMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 开放接口定义的controller基类，定义当访问出现异常时所返回的数据格式，以及访问成功时返回的数据格式
 * 
 * @author xuqc
 * @date 2015-1-24 下午04:45:34
 */
public abstract class AbsApiController extends AbsBaseController {

	/**
	 * 返回的数据格式如：</br>
	 * <root><result></result><dataSet><data></data></dataSet><msg></msg></root>
	 * result的值包括：true/false <br/>
	 * msg是在result为false的情况下，返回的异常信息，dataSet是在reulst为true的情况下的数据集合信息
	 * 
	 * @param path
	 * @param ex
	 * @param request
	 * @param response
	 * @return
	 */
	protected ModelAndView handlerInternalException(String path, Exception ex, HttpServletRequest request,
			HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(XML_CONTENT_TYPE);
		String errorMsg = ex.getMessage();
		errorMsg = ex != null && errorMsg != null ? errorMsg : "系统发生未知错误！";
		RootVO rootVO = new RootVO(errorMsg);
		try {
			BeanXmlMapping mapping = new BeanXmlMapping(rootVO);
			mapping.setParentNode("root");
			Node node = mapping.getNodeFromBean();
			response.getWriter().print(node.asXML());
		} catch(Exception e) {
			logger.error("", e);
		}
		return null;
	}
}
