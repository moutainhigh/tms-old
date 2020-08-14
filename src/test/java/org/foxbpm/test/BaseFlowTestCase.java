package org.foxbpm.test;

import junit.framework.TestCase;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BaseFlowTestCase extends TestCase {
	protected ApplicationContext appContext;

	protected ProcessEngine processEngine;
	protected RepositoryService repositoryService;
	protected RuntimeService runtimeService;
	protected TaskService taskService;
	protected HistoryService historyService;
	protected ManagementService managementService;

	String processDefinitionKey = "instorageFlow";

	protected void setUp() throws Exception {
		super.setUp();
		appContext = new ClassPathXmlApplicationContext(new String[] { "classpath*:/spring/test-spring*.xml",
				"classpath*:/spring/test-activiti.cfg.xml" });
		assertNotNull("appContext为空", appContext);
		init();
	}

	public void init() {
		processEngine = ProcessEngines.getDefaultProcessEngine();
		runtimeService = processEngine.getRuntimeService();
		taskService = processEngine.getTaskService();
		repositoryService = processEngine.getRepositoryService();
		historyService = processEngine.getHistoryService();
		managementService = processEngine.getManagementService();
		/*
		 * processDefinitionQuery：流程定义的Query对象。
		 * ProcessInstanceQuery：流程实例的Query对象。 TaskQuery：任务实例的Query对象。
		 * IdentityLinkQuery：任务候选人的Query对象。
		 */
		// 注：每次查询都重新创建对象，不要缓存
		// ProcessDefinitionQuery processDefinitionQuery =
		// repositoryService.createProcessDefinitionQuery();
		// ProcessInstanceQuery processInstanceQuery =
		// runtimeService.createProcessInstanceQuery();
		// TaskQuery taskQuery = taskService.createTaskQuery();
	}
}
