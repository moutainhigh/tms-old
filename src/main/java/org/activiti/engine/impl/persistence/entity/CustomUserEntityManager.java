package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiDao;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.apache.commons.lang3.StringUtils;
import org.nw.dao.NWDao;
import org.springframework.stereotype.Service;

/**
 * 自定义的Activiti用户管理器
 * 
 * @author dragon
 * 
 */

@Service
public class CustomUserEntityManager extends UserEntityManager {

	@Override
	public User createNewUser(String userId) {
		throw new ActivitiException("user manager doesn't support creating a new user");
	}

	@Override
	public void insertUser(User user) {
		throw new ActivitiException("user manager doesn't support insert a new user");
	}

	@Override
	public void updateUser(User updatedUser) {
		throw new ActivitiException("user manager doesn't support update a  user");
	}

	@Override
	public User findUserById(String userId) {
		String sql = "select pk_user as id,0 as revision,user_name as firstName,user_note as lastName,email as email,user_password as password "
				+ "from nw_user where pk_user=?";
		UserEntity user = NWDao.getInstance().getJdbcTemplate().queryForObject(sql, UserEntity.class, userId);
		return user;
	}

	@Override
	public void deleteUser(String userId) {
		throw new ActivitiException("user manager doesn't support delete a user");
	}

	private String getUserQuerySql(UserQueryImpl query) {
		StringBuffer sb = new StringBuffer();
		sb.append("select pk_user as id,0 as revision,user_name as firstName,user_note as lastName,email as email,user_password as password "
				+ "from nw_user where isnull(dr,0)=0");
		if(query.getGroupId() != null) {
			sb.append(" and ");
			sb.append(" pk_user in(select pk_user from nw_user_role where isnull(dr,0)=0 and pk_role='"
					+ query.getGroupId() + "')");
		}
		if(query.getId() != null) {
			sb.append(" and ");
			sb.append(" pk_user='");
			sb.append(query.getId());
			sb.append("'");
		}
		if(query.getFirstName() != null) {
			sb.append(" and ");
			sb.append(" user_name='");
			sb.append(query.getFirstName());
			sb.append("'");
		}
		if(query.getFirstNameLike() != null) {
			sb.append(" and ");
			sb.append(" user_name like '%");
			sb.append(query.getFirstNameLike());
			sb.append("%'");
		}
		if(query.getEmail() != null) {
			sb.append(" and ");
			sb.append(" email='");
			sb.append(query.getEmail());
			sb.append("'");
		}
		if(query.getEmailLike() != null) {
			sb.append(" and ");
			sb.append(" email like '%");
			sb.append(query.getEmailLike());
			sb.append("%'");
		}
		return sb.toString();
	}

	@Override
	public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
		String sql = getUserQuerySql(query);
		List<UserEntity> users = ActivitiDao.getInstance().queryForList(sql, UserEntity.class);
		List<User> userAry = new ArrayList<User>();
		for(UserEntity user : users) {
			userAry.add(user);
		}
		return userAry;
	}

	@Override
	public long findUserCountByQueryCriteria(UserQueryImpl query) {
		String sql = getUserQuerySql(query);
		sql = "SELECT COUNT(*) FROM (" + sql + ") A ";
		return ActivitiDao.getInstance().getJdbcTemplate().queryForObject(sql, Long.class);
	}

	@Override
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

	public UserQuery createNewUserQuery() {
		return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutor());
	}

	@Override
	public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("key", key);
		throw new ActivitiException("not implement yet!");
	}

	@Override
	public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
		throw new ActivitiException("not implement yet!");
	}

	@Override
	public Boolean checkPassword(String userId, String password) {
		throw new ActivitiException("not implement yet!");
	}

	private String getUserNativeQuerySql(Map<String, Object> parameterMap) {
		StringBuffer sb = new StringBuffer();
		sb.append("select pk_user as id,0 as revision,user_name as firstName,user_note as lastName,email as email,user_password as password "
				+ "from nw_user where isnull(dr,0)=0");
		if(parameterMap.get("groupId") != null) {
			sb.append(" and ");
			sb.append(" pk_user in(select pk_user from nw_user_role where isnull(dr,0)=0 and pk_role='"
					+ parameterMap.get("groupId") + "')");
		}
		if(parameterMap.get("id") != null) {
			sb.append(" and ");
			sb.append(" pk_user='");
			sb.append(parameterMap.get("id"));
			sb.append("'");
		}
		if(parameterMap.get("firstName") != null) {
			sb.append(" and ");
			sb.append(" user_name='");
			sb.append(parameterMap.get("firstName"));
			sb.append("'");
		}
		if(parameterMap.get("firstNameLike") != null) {
			sb.append(" and ");
			sb.append(" user_name like '%");
			sb.append(parameterMap.get("firstNameLike"));
			sb.append("%'");
		}
		if(parameterMap.get("email") != null) {
			sb.append(" and ");
			sb.append(" email='");
			sb.append(parameterMap.get("email"));
			sb.append("'");
		}
		if(parameterMap.get("emailLike") != null) {
			sb.append(" and ");
			sb.append(" email like '%");
			sb.append(parameterMap.get("emailLike"));
			sb.append("%'");
		}
		if(parameterMap.get("procDefId") != null) {
			sb.append(" and exists (select ID_ from ACT_RU_IDENTITYLINK where PROC_DEF_ID_ = ? and USER_ID_=pk_user )");
		}
		return sb.toString();
	}

	@Override
	public List<User> findPotentialStarterUsers(String proceDefId) {
		StringBuffer sb = new StringBuffer();
		sb.append("select pk_user as id,0 as revision,user_name as firstName,user_note as lastName,email as email,user_password as password "
				+ "from nw_user where isnull(dr,0)=0");
		if(StringUtils.isNotBlank(proceDefId)) {
			sb.append(" and exists (select ID_ from ACT_RU_IDENTITYLINK where PROC_DEF_ID_ = ? and USER_ID_=pk_user )");
		}
		List<UserEntity> users = ActivitiDao.getInstance().queryForList(sb.toString(), UserEntity.class);
		List<User> userAry = new ArrayList<User>();
		for(UserEntity user : users) {
			userAry.add(user);
		}
		return userAry;
	}

	@Override
	public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
		String sql = getUserNativeQuerySql(parameterMap);
		List<UserEntity> users = ActivitiDao.getInstance().queryForList(sql, UserEntity.class);
		List<User> userAry = new ArrayList<User>();
		for(UserEntity user : users) {
			userAry.add(user);
		}
		return userAry;
	}

	@Override
	public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
		String sql = getUserNativeQuerySql(parameterMap);
		sql = "SELECT COUNT(*) FROM (" + sql + ") A ";
		return ActivitiDao.getInstance().getJdbcTemplate().queryForObject(sql, Long.class);
	}

	@Override
	public boolean isNewUser(User user) {
		return ((UserEntity) user).getRevision() == 0;
	}

	@Override
	public Picture getUserPicture(String userId) {
		UserEntity user = (UserEntity) findUserById(userId);
		return user.getPicture();
	}

	@Override
	public void setUserPicture(String userId, Picture picture) {
		UserEntity user = (UserEntity) findUserById(userId);
		if(user == null) {
			throw new ActivitiObjectNotFoundException("user " + userId + " doesn't exist", User.class);
		}

		user.setPicture(picture);
	}

}
