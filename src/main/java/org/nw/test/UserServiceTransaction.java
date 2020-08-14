package org.nw.test;

import java.util.List;

import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.service.IToftService;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tms.vo.base.AddressVO;

/**
 * 测试使用声明式事务
 * 
 * @author xuqc
 * @date 2013-8-31 下午12:06:34
 */
@Service
public class UserServiceTransaction extends AbsToftServiceImpl implements IToftService {

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	@Transactional
	public void addObject() {
		String sql = "select * from ts_address where addr_type = 1 and detail_addr = '上海市嘉定区泰波路11号' and pk_city = '772593a0487b44d59fe7bb045e2a29b6'";
		List<AddressVO> uv = NWDao.getInstance().queryForList(sql, AddressVO.class);
		List<AddressVO> uv1 = NWDao.getInstance().queryForList(sql, AddressVO.class);
		String code = "0002";
		UserVO userVO = new UserVO();
		userVO.setUser_code(code);
		userVO.setUser_name(code);
		userVO.setStatus(VOStatus.NEW);
		DaoHelper.setUuidPrimaryKey(userVO);
		dao.saveOrUpdate(userVO);

		TestUtils.syncUpdate();

		DeptVO deptVO = new DeptVO();
		deptVO.setDept_code(code);
		deptVO.setDept_name(code);
		deptVO.setStatus(VOStatus.NEW);
		DaoHelper.setUuidPrimaryKey(deptVO);
		// dao.saveOrUpdate(deptVO);

	}
}
