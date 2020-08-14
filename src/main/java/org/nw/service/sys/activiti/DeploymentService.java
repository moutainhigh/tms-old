package org.nw.service.sys.activiti;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.nw.service.IToftService;
import org.nw.vo.ParamVO;
import org.nw.vo.sys.activiti.DeploymentVO;

public interface DeploymentService extends IToftService {
	/**
	 * 部署流程
	 * 
	 * @param inMap
	 */
	public void doDeploy(Map<String, InputStream> inMap);

	/**
	 * 预览流程图
	 * 
	 * @param vo
	 * @param out
	 * @throws Exception
	 */
	public void preview(DeploymentVO vo, OutputStream out) throws Exception;

	/**
	 * 启动流程
	 * 
	 * @param deployment_id
	 */
	public ProcessInstance doStartFlow(ParamVO paramVO);
}
