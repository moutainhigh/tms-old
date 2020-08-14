package org.foxbpm.test;

import java.io.IOException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;

/**
 * 流程部署测试
 * 
 * @author xuqc
 *
 */
public class FlowDeployTest extends BaseFlowTestCase {

	@Test
	public void testDeploy() throws IOException {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment().addClasspathResource("flow/财务审批.bpmn")
				.addClasspathResource("flow/财务审批.png").deploy();
		System.out.println("deploymentId:" + deployment.getId() + ",deploymentName:" + deployment.getName());
	}
}
