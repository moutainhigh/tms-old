package org.nw.web.sys.activiti;

import org.nw.service.sys.activiti.impl.TodoTaskServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 待办
 * 
 * @author xuqc
 *
 */
@Controller
@RequestMapping(value = "/activiti/todotask")
public class TodoTaskController extends AbsActivitiController {

	@Override
	public TodoTaskServiceImpl getService() {
		return new TodoTaskServiceImpl();
	}

}
