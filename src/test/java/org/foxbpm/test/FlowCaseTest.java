package org.foxbpm.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

public class FlowCaseTest extends BaseFlowTestCase {
	ProcessEngine processEngine;// 流程引擎示例
	// ModelService modelService; // 发布流程，对流程定义的操作，所以方法在modelService类
	RuntimeService runtimeService;// 启动流程，对于流程实例的操作，所以方法在runtimeService中，这里有多种方法
	TaskService taskService;// 获取待办任务、点击同意按钮、获取流程实例（流程追踪）等对任务的操作

	String processDefinitionKey = "test01_1";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// init();
	}

	// public void init() {
	// processEngine = ProcessEngineManagement.getDefaultProcessEngine();
	// modelService = processEngine.getModelService();
	// runtimeService = processEngine.getRuntimeService();
	// taskService = processEngine.getTaskService();
	//
	// /*
	// * processDefinitionQuery：流程定义的Query对象。
	// * ProcessInstanceQuery：流程实例的Query对象。 TaskQuery：任务实例的Query对象。
	// * IdentityLinkQuery：任务候选人的Query对象。
	// */
	// // 注：每次查询都重新创建对象，不要缓存
	// ProcessDefinitionQuery processDefinitionQuery =
	// modelService.createProcessDefinitionQuery();
	// ProcessInstanceQuery processInstanceQuery =
	// runtimeService.createProcessInstanceQuery();
	// RunningTrackQuery runningTrackQuery =
	// runtimeService.createRunningTrackQuery();
	// TokenQuery tokenQuery = runtimeService.createTokenQuery();
	// TaskQuery taskQuery = taskService.createTaskQuery();
	// // 通过流程定义编号获取流程图的文件流
	// InputStream stream =
	// modelService.GetFlowGraphicsImgStreamByDefKey(processDefinitionKey);
	// if(stream != null) {
	// System.out.println("success");
	// }
	// }
	//
	// /**
	// * 启动流程
	// */
	// @Test
	// public void testStartFlow() {
	// // ProcessInstance processInstance =
	// // runtimeService.startProcessInstanceByKey(processDefinitionKey);
	//
	// // System.out.println(processInstance.getId());
	// // System.out.println(processInstance.getInitiator());
	// // System.out.println(processInstance.getInstanceStatus());
	// // System.out.println(processInstance.getProcessLocation());
	// // System.out.println(processInstance.getStartAuthor());
	// // System.out.println(processInstance.getSubject());
	//
	// }
	//
	// /**
	// * 读取流程任务
	// */
	// @Test
	// public void testUserTask() {
	// TaskQuery taskQuery = taskService.createTaskQuery();
	// taskQuery.processDefinitionKey(processDefinitionKey);
	// // taskQuery("AIMER");
	// long result = taskQuery.count();
	// System.out.println("任务数：" + result);
	// // taskQuery.businessKey("TESTKEY");
	// List<Task> tasks = taskQuery.list();
	// for(Task task : tasks) {
	// System.out.println("任务ID：" + task.getId());
	// System.out.println("任务业务Key：" + task.getBizKey());
	// }
	// }
	//
	// @Test
	// public void testCommitTask() {
	// // ProcessInstance processInstance =
	// // runtimeService.startProcessInstanceByKey(processDefinitionKey,
	// // "TESTKEY");
	// // System.out.println("流程实例ID：" + processInstance.getId());
	// // Task task =
	// //
	// taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskNotEnd()
	// // .singleResult();
	// //
	// // ExpandTaskCommand expandTaskCommand = new ExpandTaskCommand();
	// // expandTaskCommand.setTaskId(task.getId());
	// // expandTaskCommand.setTaskCommandId("HandleCommand_99");
	// // expandTaskCommand.setCommandType("submit");
	// // expandTaskCommand.setBusinessKey("TESTKEY");
	// // expandTaskCommand.setInitiator("AIMER");
	// // taskService.expandTaskComplete(expandTaskCommand, null);
	// }

}
