package org.foxbpm.test;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.log4j.Logger;
import org.junit.Test;

public class FlowStartTest extends BaseFlowTestCase {
	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger(FlowStartTest.class.getName());

	/**
	 * 启动流程
	 */
	@Test
	public void testStartFlow() {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("name", "Activiti");
		// 第一个参数是流程key，第二个参数是busiessKey
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, "INSTO0001",
				variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " " + processInstance.getProcessDefinitionId());
	}

}
