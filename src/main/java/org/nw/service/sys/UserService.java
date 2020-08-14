package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.sys.UserVO;


public interface UserService extends IToftService {

	/**
	 * 根据用户名读取用户对象
	 * 
	 * @param username
	 */
	public UserVO getByUserCode(String user_code);

	/**
	 * 事务测试方法
	 */
	public void addObject();

	/**
	 * 根据部门返回用户
	 * 
	 * @param pk_dept
	 * @return
	 */
	public List<UserVO> getByPkDept(String pk_dept);

	/**
	 * 更新密码及发送邮件，在重置密码时使用
	 * 
	 * @param usercode
	 * @param newPassword
	 * @param email
	 */
	public void updatePasswordAndSendMail(final String usercode, final String newPassword, final String email);
}
