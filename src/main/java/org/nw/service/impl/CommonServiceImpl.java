package org.nw.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.redis.RedisDao;
import org.nw.utils.NWUtils;
import org.nw.utils.TempletHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.sys.BtnTempletDistVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:42:15
 */
@Service
public class CommonServiceImpl {
	Logger logger = Logger.getLogger(this.getClass());

	/**
	 * 自定义按钮
	 * 
	 * @param paramVO
	 * @param pk_fun
	 *            按钮的定义对象
	 * @param billId
	 *            所选择的单据PK
	 * @param paramMap
	 *            弹出的界面的key：value,注意顺序和模板中一致
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doCustomButton(ParamVO paramVO, String pk_fun, String[] billId, LinkedHashMap<String, Object> paramMap) {
		if(StringUtils.isBlank(pk_fun) || billId == null || billId.length == 0) {
			return;
		}
		FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, "pk_fun=?", pk_fun);
		if(funVO == null) {
			throw new BusiException("自定义按钮已经被删除！");
		}
		String proc = funVO.getBill_type();// 对于按钮，单据类型存储要执行的存储过程
		if(StringUtils.isBlank(proc)) {
			throw new BusiException("自定义按钮没有设置要调用的存储过程，请写在[单据类型]项中！");
		}

		final String fProc = proc;
		final LinkedHashMap<String, Object> fParamMap = paramMap;
		final String fBillId = NWUtils.join(billId, ",");
		final String pk_user = WebUtils.getLoginInfo().getPk_user();
		// 用于存储查询数据
		final List<String> retList = new ArrayList<String>();
		// 这里的参数个数必须加1，因为存储过程的第一个参数是billId
		NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
			public CallableStatement createCallableStatement(Connection conn) throws SQLException {
				int count = (fParamMap == null ? 0 : fParamMap.size()) + 2;
				String storedProc = DaoHelper.getProcedureCallName(fProc, count);
				CallableStatement cs = conn.prepareCall(storedProc);
				cs.setString(1, fBillId);
				cs.setString(2, pk_user);
				if(fParamMap != null) {
					int index = 3;
					for(String key : fParamMap.keySet()) {
						cs.setObject(index, fParamMap.get(key));
						index++;
					}
				}
				return cs;
			}
		}, new CallableStatementCallback() {
			public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
				cs.execute();
				ResultSet rs = cs.getResultSet();
				if(rs != null){
					while (rs.next()) {
						retList.add(rs.getString(1));
						break;
					}
				}
				return null;
			}
		});
		if(retList.size() > 0 && StringUtils.isNotBlank(retList.get(0))){
			throw new BusiException(retList.get(0));
		}
	}

	/**
	 * 自定义按钮的模板
	 * 
	 * @param pk_fun
	 */
	public String getCustomBtnTemplet(String pk_fun) {
		if(StringUtils.isBlank(pk_fun)) {
			return null;
		}
		BtnTempletDistVO[] btdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(BtnTempletDistVO.class,
				"pk_fun=?", pk_fun);
		if(btdVOs == null || btdVOs.length == 0) {
			return null;
		}
		if(btdVOs.length > 1) {
			logger.warn("节点[PK:" + pk_fun + "]分配了多个模板！");
		}
		return btdVOs[0].getPk_templet();
	}

	public UiBillTempletVO getBillTempletVO(String pk_billtemplet) {
		if(StringUtils.isBlank(pk_billtemplet)) {
			return null;
		}
		//return TempletHelper.getOriginalBillTempletVO(pk_billtemplet);
		return RedisDao.getInstance().getOriginalBillTempletVO(pk_billtemplet);
	}
}
