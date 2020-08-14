package org.nw.web.sys.activiti;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.service.sys.activiti.DeploymentService;
import org.nw.service.sys.activiti.impl.DeploymentServiceImpl;
import org.nw.vo.ParamVO;
import org.nw.vo.sys.activiti.DeploymentVO;
import org.nw.web.AbsToftController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 流程部署
 * 
 * @author xuqc
 *
 */
@Controller
@RequestMapping(value = "/activiti/deployment")
public class DeploymentController extends AbsToftController {

	@Override
	public DeploymentService getService() {
		return new DeploymentServiceImpl();
	}

	/**
	 * 启动流程
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/startFlow.json")
	@ResponseBody
	public Map<String, Object> startFlow(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		if(StringUtils.isBlank(paramVO.getBillId())) {
			throw new BusiException("请先选择要启动的流程！");
		}
		ProcessInstance processInstance = this.getService().doStartFlow(paramVO);
		if(processInstance == null) {
			return this.genAjaxResponse(false, "流程启动失败！", null);
		}
		return this.genAjaxResponse(true, "流程已启动，流程实例ID：" + processInstance.getId(), null);
	}

	/**
	 * 部署流程
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/deploy.do")
	public void deploy(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> fileAry = mRequest.getFiles("userfile");
		Map<String, InputStream> inMap = new HashMap<String, InputStream>();
		for(MultipartFile file : fileAry) {
			String fileName = file.getOriginalFilename();
			if(StringUtils.isBlank(fileName)) {
				continue;
			}
			inMap.put(fileName, file.getInputStream());
		}
		Set<String> keys = inMap.keySet();
		if(keys.size() == 0) {
			throw new JsonException("您没有发布任何文件！");
		}
		this.getService().doDeploy(inMap);
		this.writeHtmlStream(response, "{'msg':'流程发布成功!','success':'true'}");
	}

	/**
	 * 预览流程图
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/preview.do")
	public void preview(HttpServletRequest request, HttpServletResponse response) {
		String deployment_id = request.getParameter("deployment_id");
		if(StringUtils.isBlank(deployment_id)) {
			throw new BusiException("预览流程图时ID参数不能为空！");
		}
		DeploymentVO vo = this.getService().getByPrimaryKey(DeploymentVO.class, deployment_id);
		if(vo == null) {
			throw new BusiException("流程发布记录已经不存在！");
		}
		try {
			response.setContentType("image/png");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			this.getService().preview(vo, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！", e.getMessage());
		}
	}
}
