package org.foxbpm.test;

import java.util.List;

import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;

/**
 * 
 * @author xuqc
 *
 */
public class FlowTaskTest extends BaseFlowTestCase {
	/**
	 * 读取流程任务
	 */
	@Test
	public void testUserTask() {
		TaskQuery taskQuery = taskService.createTaskQuery();// 每次查询重新创建一个taskQuery对象，否则查询条件会叠加，并且候选人和组不能同时作为一个条件
		// taskQuery.processDefinitionKey(processDefinitionKey);//
		// 指定某个流程的任务，一般不需要指定
		// List<Task> tasks =
		long result = taskQuery.taskCandidateGroup("management").count();//
		System.out.println("management组任务数：" + result);
		result = taskQuery.taskCandidateGroup("mygroup").count();//
		System.out.println("mygroup组任务数：" + result);
		result = taskQuery.taskCandidateGroup("c4b86b63b4fa40ed83845c96c14fb018").count();//
		System.out.println("c4b86b63b4fa40ed83845c96c14fb018组任务数：" + result);
		// 某个角色的任务待办
		result = taskService.createTaskQuery().taskCandidateUser("test").count();// 某人的任务待办
		System.out.println("test用户的任务数：" + result);
		System.out.println("总任务数：" + taskService.createTaskQuery().count());
		// taskQuery.businessKey("TESTKEY");//根据业务号查询的待办
		List<Task> tasks = taskService.createTaskQuery().list();
		for(Task task : tasks) {
			System.out.println("任务ID：" + task.getId() + ",任务名称：" + task.getName());
			// taskService.complete(task.getId());// 完成任务
			// taskService.unclaim(task.getId());
		}
		result = taskService.createTaskQuery().taskCandidateGroup("mygroup").count();//
		System.out.println("mygroup组任务数：" + result);
	}
}
