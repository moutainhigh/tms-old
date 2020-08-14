package com.nw.test;

import org.nw.dao.helper.DaoHelper;
import org.nw.service.sys.UserService;
import org.nw.test.UserServiceTransaction;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.UserVO;

/**
 * 注意检查test-spring-resource.xml中的数据源配置，要执行该测试用例，需要改成使用dbcp进行连接
 * 
 * @author xuqc
 * 
 */
public class UserServiceTest extends BaseTestCase {

	private UserService userService1;
	private UserServiceTransaction userServiceTransaction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shonetown.store.test.BaseTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		userService1 = (UserService) appContext.getBean("userServiceImpl");
		userServiceTransaction = (UserServiceTransaction) appContext.getBean("userServiceTransaction");
	}

	// public void testGetByUserCode() {
	// UserVO userVO = userService.getByUserCode("0001");
	// Assert.assertNotNull(userVO);
	// }

	public void testTransaction() {
		// userService.addObject();
		// userServiceTransaction.addObject();
		HYBillVO billVO = new HYBillVO();
		String code = "0002";
		UserVO userVO = new UserVO();
		userVO.setUser_code(code);
		userVO.setUser_name(code);
		userVO.setStatus(VOStatus.NEW);
		DaoHelper.setUuidPrimaryKey(userVO);
		billVO.setParentVO(userVO);
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode("t070");
		paramVO.setTemplateID("0001A4100000000000SP");
		userService1.save(billVO, paramVO);
	}
}
