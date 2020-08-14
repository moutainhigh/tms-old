package org.nw.service.sys.activiti.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.dao.PaginationVO;
import org.nw.json.JacksonUtils;
import org.nw.service.sys.RoleService;
import org.nw.service.sys.activiti.TodoTaskService;
import org.nw.service.sys.impl.RoleServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.RoleVO;
import org.nw.vo.sys.activiti.TaskVO;
import org.nw.web.utils.ProcessEngineHelper;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 我的待办
 * 
 * @author xuqc
 *
 */
@Service
public class TodoTaskServiceImpl extends AbsActivitiServiceImpl implements TodoTaskService {

	@Autowired
	private RoleService roleService;

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, TaskVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, TaskVO.ID_);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	private Map<String, String> getTaskCond(String params) {
		Map<String, String> condMap = new HashMap<String, String>();
		if(StringUtils.isNotBlank(params) && !"[]".equals(params)) {
			JsonNode node = JacksonUtils.readTree(params);
			for(int m = 0; m < node.size(); m++) {
				JsonNode child = node.get(m);
				String fieldName = child.get("fieldName").getValueAsText().trim(); // 在设计参照的公式时，经常会在前面加入空格
				String value = child.get("value").getValueAsText().trim();
				if(StringUtils.isBlank(value)) {
					continue;
				}
				if(fieldName.equalsIgnoreCase("ACT_RU_TASK.name_")
						|| fieldName.equalsIgnoreCase("ACT_RU_TASK.description_")) {
					condMap.put(fieldName, value);
				}
			}
		}
		return condMap;
	}

	/**
	 * 这里只查询角色具有的待办，如果只分配给用户的待办呢？
	 */
	@Override
	public PaginationVO loadData(ParamVO paramVO, int offset, int pageSize, String[] pks, String orderBy,
			String extendCond, Object... values) {
		String params = ServletContextHolder.getRequest().getParameter(Constants.PUB_QUERY_PARAMETER);
		Map<String, String> condMap = getTaskCond(params);
		TaskQuery taskQuery = ProcessEngineHelper.getProcessEngine().getTaskService().createTaskQuery();
		String nameLike = condMap.get("ACT_RU_TASK.name_");
		String descriptionLike = condMap.get("ACT_RU_TASK.description_");
		if(StringUtils.isNotBlank(nameLike)) {
			taskQuery.taskNameLike(nameLike);
		}
		if(StringUtils.isNotBlank(descriptionLike)) {
			taskQuery.taskDescriptionLike(descriptionLike);
		}
		// 返回用户的角色
		List<RoleVO> roleAry = new RoleServiceImpl().getRoleByUser(WebUtils.getLoginInfo().getPk_user());
		if(roleAry != null && roleAry.size() > 0) {
			List<String> groupsAry = new ArrayList<String>();
			for(RoleVO vo : roleAry) {
				groupsAry.add(vo.getPk_role());
			}
			// 查找角色下具有的待办
			taskQuery.taskCandidateGroupIn(groupsAry);
		} else {
			// 查询用户
			taskQuery.taskCandidateUser(WebUtils.getLoginInfo().getPk_user());
		}
		taskQuery.orderByTaskCreateTime().desc();
		long totalCount = taskQuery.count();
		PaginationVO pageVO = new PaginationVO((int) totalCount, offset, pageSize);
		List<Task> tasks = taskQuery.listPage(pageVO.getStartOffset() - 1, pageVO.getEndOffset());
		List<TaskVO> taskVOs = new ArrayList<TaskVO>();
		for(Task task : tasks) {
			taskVOs.add(convert(task));
		}
		pageVO.setItems(taskVOs);
		return pageVO;
	}

	private TaskVO convert(Task task) {
		TaskVO taskVO = new TaskVO();
		taskVO.setId_(task.getId());
		taskVO.setExecution_id_(task.getExecutionId());
		taskVO.setProc_def_id_(task.getProcessDefinitionId());
		taskVO.setProc_inst_id_(task.getProcessInstanceId());
		taskVO.setName_(task.getName());
		taskVO.setParent_task_id_(task.getParentTaskId());
		taskVO.setDescription_(task.getDescription());
		taskVO.setTask_def_key_(task.getTaskDefinitionKey());
		taskVO.setOwner_(task.getOwner());
		taskVO.setAssignee_(task.getAssignee());
		taskVO.setPriority_(task.getPriority());
		taskVO.setCreate_time_(new UFDateTime(task.getCreateTime()));
		if(task.getDueDate() != null) {
			taskVO.setDue_date_(new UFDateTime(task.getDueDate()));
		}
		taskVO.setCategory_(task.getCategory());
		taskVO.setForm_key_(task.getFormKey());
		taskVO.setDr(0);
		taskVO.setTs(new UFDateTime(new Date()));
		return taskVO;
	}

	@Override
	public PaginationVO getPaginationVO(AggregatedValueObject billInfo, ParamVO paramVO,
			Class<? extends SuperVO> voClass, int offset, int pageSize, String where, String orderBy, Object... values) {
		NativeTaskQuery taskQuery = ProcessEngineHelper.getProcessEngine().getTaskService().createNativeTaskQuery();
		//yaojiie 2015 12 08添加 WITH (NOLOCK)
		String sql = "SELECT * FROM ACT_RU_TASK WITH(NOLOCK) WHERE 1=1 ";
		if(StringUtils.isNotBlank(where)) {
			sql += where;
		}
		if(StringUtils.isNotBlank(orderBy)) {
			sql += orderBy;
		}
		long totalCount = taskQuery.sql(sql).count();
		PaginationVO pageVO = new PaginationVO((int) totalCount, offset, pageSize);
		List<Task> tasks = taskQuery.sql(sql).listPage(pageVO.getStartOffset(), pageVO.getEndOffset());
		pageVO.setItems(tasks);
		return pageVO;
	}

	public String getBillType() {
		// TODO Auto-generated method stub
		return null;
	}
}
