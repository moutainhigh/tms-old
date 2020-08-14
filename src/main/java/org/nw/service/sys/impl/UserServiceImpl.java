package org.nw.service.sys.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.mail.MailSenderInfo;
import org.nw.mail.SimpleMailSender;
import org.nw.mail.res.ResetPasswordMailTemplate;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.RoleService;
import org.nw.service.sys.UserService;
import org.nw.test.TestUtils;
import org.nw.utils.CodenoHelper;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.UserRoleVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.DigestPasswordEncoder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户管理操作类
 * 
 * @author xuqc
 * 
 */
@Service
public class UserServiceImpl extends AbsToftServiceImpl implements UserService {

	private static final String default_user_password = "123456";

	@Autowired
	private RoleService roleService;

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, UserVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, UserVO.PK_USER);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0] && fieldVO.getItemkey().equals("pk_corp")) {
				// 编辑了公司,需要重新加载相应的部门
				fieldVO.setUserdefine1("afterChangePk_corp(value,originalValue)");
			}
		}
		return templetVO;
	}

	public UserVO getByUserCode(String user_code) {
		return dao.queryByCondition(UserVO.class, "user_code=?", user_code);
	}

//	public Map<String, Object> show(ParamVO paramVO) {
//		Map<String, Object> userMap = super.show(paramVO);
//		return userMap;
//	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String where = " user_type<>" + Constants.USER_TYPE.ADMIN.intValue(); // 过滤管理员的帐号
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			where += " and " + cond;
		}
		// 加入公司条件,如果是集体用户，可以看到所有子公司的员工
		if(!WebUtils.getLoginInfo().getPk_corp().equals(Constants.SYSTEM_CODE)) {
			String corpCond = CorpHelper.getCurrentCorp();
			if(StringUtils.isNotBlank(corpCond)) {
				where += " and " + corpCond;
			}
		}

		return where;
	}

	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		UserVO userVO = (UserVO) billVO.getParentVO();
		if(VOStatus.NEW == userVO.getStatus()) {
			// 新增的情况，可以同时设置密码，如果是修改的话，密码不可修改，会提供修改密码按钮
			DigestPasswordEncoder encoder = new DigestPasswordEncoder();
			String password = encoder.encodePassword(default_user_password, null);
			userVO.setUser_password(password);
		}
		billVO = super.save(billVO, paramVO);
		if(StringUtils.isNotBlank(userVO.getSrc_user())) {
			// 如果是从复制单据来的，那么同时复制用户的角色
			List<UserRoleVO> userRoleVOs = roleService.getUserRoleByUser(userVO.getSrc_user());
			for(UserRoleVO userRoleVO : userRoleVOs) {
				userRoleVO.setPk_user_role(null);
				userRoleVO.setPk_user(userVO.getPk_user());
				userRoleVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(userRoleVO);
			}
			dao.saveOrUpdate(userRoleVOs.toArray(new UserRoleVO[userRoleVOs.size()]));
		}
		return billVO;
	}

	public AggregatedValueObject copy(ParamVO paramVO) {
		AggregatedValueObject billVO = super.copy(paramVO);
		return billVO;
	}

	public List<UserVO> getByPkDept(String pk_dept) {
		UserVO[] userVOs = dao.queryForSuperVOArrayByCondition(UserVO.class,
				"isnull(locked_flag,'N')='N' and pk_dept=?", pk_dept);
		return Arrays.asList(userVOs);
	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject parentVO = copyVO.getParentVO();
		parentVO.setAttributeValue("user_code", CodenoHelper.generateCode(paramVO.getFunCode()));
	}

	protected void beforeProcessCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.beforeProcessCopyVO(copyVO, paramVO);
		UserVO parentVO = (UserVO) copyVO.getParentVO();
		parentVO.setSrc_user(parentVO.getPk_user());
		parentVO.setUser_code(CodenoHelper.generateCode(paramVO.getFunCode()));
	}

	public void updatePasswordAndSendMail(String usercode, String newPassword, String email) {
		// 更新密码
		DigestPasswordEncoder encoder = new DigestPasswordEncoder();
		String password = encoder.encodePassword(newPassword, null);
		final String updateSql = "update nw_user set user_password='" + password + "' where user_code='" + usercode
				+ "'";
		// 将发送邮件放在此，是为了统一事务
		MailSenderInfo mailSenderInfo = new MailSenderInfo();
		mailSenderInfo.setToAddress(new String[] { email });
		ResetPasswordMailTemplate resetMail = new ResetPasswordMailTemplate();
		resetMail.initResetMail(usercode, newPassword);
		mailSenderInfo.setSubject(resetMail.getSubject());
		mailSenderInfo.setContent(resetMail.getContent());
		try {
			SimpleMailSender.sendHtmlMail(mailSenderInfo);
		} catch(Throwable e) {// 这里可能包括error
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		dao.getJdbcTemplate().execute(updateSql);
	}

	public void addObject() {
		String code = "0002";
		UserVO userVO = new UserVO();
		userVO.setUser_code(code);
		userVO.setUser_name(code);
		userVO.setStatus(VOStatus.NEW);
		DaoHelper.setUuidPrimaryKey(userVO);
		DeptVO deptVO = new DeptVO();
		deptVO.setDept_code(code);
		deptVO.setDept_name(code);
		deptVO.setStatus(VOStatus.NEW);
		DaoHelper.setUuidPrimaryKey(deptVO);
		dao.saveOrUpdate(userVO);
		dao.saveOrUpdate(deptVO);
		if(true) {
			throw new RuntimeException("事务回滚测试!");
		}
	}

}
