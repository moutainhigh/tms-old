package org.nw.web.sys;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.service.sys.MergeService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 资源压缩
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/merge")
public class MergeController extends AbsToftController {

	@Autowired
	private MergeService mergeService;

	public MergeService getService() {
		return mergeService;
	}

	/**
	 * 执行压缩资源
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/execute.json")
	@ResponseBody
	public Map<String, Object> execute(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_merge = request.getParameterValues("pk_merge");
		if(pk_merge != null && pk_merge.length > 0) {
			this.getService().execute(pk_merge);
		}
		return this.genAjaxResponse(true, null, null);
	}

}
