package org.nw.web.sys;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.service.sys.JobDefService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 任务配置
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/jobdef")
public class JobDefController extends AbsToftController {

	@Autowired
	private JobDefService jobService;

	public JobDefService getService() {
		return jobService;
	}

	/**
	 * 任务测试
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/testTask.json")
	@ResponseBody
	protected Map<String, Object> testTask(HttpServletRequest request) {
		String pk_job_def = request.getParameter("pk_job_def");
		if(StringUtils.isBlank(pk_job_def)) {
			throw new BusiException("请选择一行记录！");
		}
		String result = this.getService().testTask(pk_job_def);
		return this.genAjaxResponse(true, null, result);
	}
}
