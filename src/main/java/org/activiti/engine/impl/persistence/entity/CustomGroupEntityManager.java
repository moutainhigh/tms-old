/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiDao;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;

/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class CustomGroupEntityManager extends GroupEntityManager {

	public Group createNewGroup(String groupId) {
		return new GroupEntity(groupId);
	}

	public void insertGroup(Group group) {
		throw new ActivitiException("group manager doesn't support creating a new group");
	}

	public void updateGroup(Group updatedGroup) {
		throw new ActivitiException("group manager doesn't support update group");
	}

	public void deleteGroup(String groupId) {
		throw new ActivitiException("group manager doesn't support delete a group");
	}

	public GroupQuery createNewGroupQuery() {
		return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutor());
	}

	private String getGroupQuerySql(GroupQueryImpl query) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT pk_role AS id, role_name AS name,1 AS type from nw_role where isnull(dr,0)=0 ");
		if(query.getId() != null) {
			sb.append(" and pk_role='");
			sb.append(query.getId());
			sb.append("'");
		}
		if(query.getName() != null) {
			sb.append(" and role_name='");
			sb.append(query.getName());
			sb.append("'");
		}
		if(query.getNameLike() != null) {
			sb.append(" and role_name like'%");
			sb.append(query.getNameLike());
			sb.append("%'");
		}
		if(query.getUserId() != null) {
			sb.append(" and pk_role in (select pk_role from nw_user_role where isnull(dr,0)=0 and pk_user='");
			sb.append(query.getUserId());
			sb.append("')");
		}
		return sb.toString();
	}

	public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
		String sql = getGroupQuerySql(query);
		List<GroupEntity> groups = ActivitiDao.getInstance().queryForList(sql, GroupEntity.class);
		List<Group> groupAry = new ArrayList<Group>();
		for(GroupEntity group : groups) {
			groupAry.add(group);
		}
		return groupAry;
	}

	public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
		String sql = getGroupQuerySql(query);
		sql = "SELECT COUNT(*) FROM (" + sql + ") A ";
		return ActivitiDao.getInstance().getJdbcTemplate().queryForObject(sql, Long.class);
	}

	public List<Group> findGroupsByUser(String userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT pk_role AS id, role_name AS name,1 AS type from nw_role where isnull(dr,0)=0 ");
		sb.append(" and pk_role in (select pk_role from nw_user_role where isnull(dr,0)=0 and pk_user='");
		sb.append(userId);
		sb.append("')");
		List<GroupEntity> groups = ActivitiDao.getInstance().queryForList(sb.toString(), GroupEntity.class);
		List<Group> groupAry = new ArrayList<Group>();
		for(GroupEntity group : groups) {
			groupAry.add(group);
		}
		return groupAry;
	}

	private String getNativeQuerySql(Map<String, Object> parameterMap) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT pk_role AS id, role_name AS name,1 AS type from nw_role where isnull(dr,0)=0 ");
		if(parameterMap.get("id") != null) {
			sb.append(" and pk_role='");
			sb.append(parameterMap.get("id"));
			sb.append("'");
		}
		if(parameterMap.get("name") != null) {
			sb.append(" and role_name='");
			sb.append(parameterMap.get("name"));
			sb.append("'");
		}
		if(parameterMap.get("nameLike") != null) {
			sb.append(" and role_name like'%");
			sb.append(parameterMap.get("nameLike"));
			sb.append("%'");
		}
		if(parameterMap.get("userId") != null) {
			sb.append(" and pk_role in (select pk_role from nw_user_role where isnull(dr,0)=0 and pk_user='");
			sb.append(parameterMap.get("userId"));
			sb.append("')");
		}
		if(parameterMap.get("procDefId") != null) {
			sb.append(" and ");
			sb.append("and exists (select ID_ from ACT_RU_IDENTITYLINK where PROC_DEF_ID_ = '"
					+ parameterMap.get("procDefId") + "' and GROUP_ID_=pk_role )");
		}
		return sb.toString();
	}

	public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
		String sql = getNativeQuerySql(parameterMap);
		List<GroupEntity> groups = ActivitiDao.getInstance().queryForList(sql, GroupEntity.class);
		List<Group> groupAry = new ArrayList<Group>();
		for(GroupEntity group : groups) {
			groupAry.add(group);
		}
		return groupAry;
	}

	public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
		String sql = getNativeQuerySql(parameterMap);
		sql = "SELECT COUNT(*) FROM (" + sql + ") A ";
		return ActivitiDao.getInstance().getJdbcTemplate().queryForObject(sql, Long.class);
	}

	@Override
	public boolean isNewGroup(Group group) {
		return ((GroupEntity) group).getRevision() == 0;
	}

}
