package org.nw.web.sys;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.utils.UIUtils;
import org.nw.service.sys.QueryTempletService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author xuqc
 * @date 2012-12-1 下午02:53:44
 */
@Controller
@RequestMapping(value = "/qt")
public class QueryTempletController extends AbsToftController {
	@Autowired
	private QueryTempletService queryTempletService;

	public QueryTempletService getService() {
		return queryTempletService;
	}

	/**
	 * 返回参照对象
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getRefModel.json")
	@ResponseBody
	public Map<String, Object> getRefModel(HttpServletRequest request, HttpServletResponse response) {
		String reftype = request.getParameter("reftype");
		if(StringUtils.isBlank(reftype)) {
			throw new BusiException("读取参照类时，参照名称不能为空！");
		}
		BaseRefModel refModel = UIUtils.buildRefModel(reftype, true, true);
		refModel.setIdcolname("value");
		refModel.setShowCodeOnBlur(false);
		return this.genAjaxResponse(true, null, refModel);
	}
}
