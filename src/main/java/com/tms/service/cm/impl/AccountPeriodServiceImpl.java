package com.tms.service.cm.impl;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.service.cm.AccountPeriodService;
import com.tms.vo.cm.AccountPeriodBVO;
import com.tms.vo.cm.AccountPeriodVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:49:35
 */
@Service
public class AccountPeriodServiceImpl extends AbsToftServiceImpl implements AccountPeriodService {
	
	
	private final int COMMIT = 0;
	
	private final int UNCOMMIT = 1;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AccountPeriodVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AccountPeriodVO.PK_ACCOUNT_PERIOD);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, AccountPeriodBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, AccountPeriodBVO.PK_ACCOUNT_PERIOD);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, AccountPeriodBVO.TS_ACCOUNT_PERIOD_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, AccountPeriodBVO.TS_ACCOUNT_PERIOD_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}
	
	public String getCodeFieldCode() {
		return "code";
	}
	
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		AccountPeriodVO parentVO = (AccountPeriodVO) billVO.getParentVO();
		if(parentVO.getStatus() != VOStatus.NEW){
			return;
		}
		String account_year = parentVO.getAccount_year();
		boolean isLeapYear = UFDateTime.isLeapYear(Integer.parseInt(account_year));
		//生成明细表
		AccountPeriodBVO[] childrenVO = new AccountPeriodBVO[12];
		for(int i=1; i<=12; i++){
			AccountPeriodBVO childVO = new AccountPeriodBVO();
			childVO.setStatus(VOStatus.NEW);
			childVO.setPeriod_year(account_year);
			childVO.setPeriod_month(i);
			String period_code_b = "";
			if(i<10){
				period_code_b = account_year + "-0" + i;
			}else{
				period_code_b = account_year + "-" + i;
			}
			childVO.setPeriod_code_b(period_code_b);
			childVO.setStart_date(new UFDateTime(period_code_b + "-01 00:00:00"));
			if(i==2){
				if(isLeapYear){//闰年
					childVO.setEnd_date(new UFDateTime(period_code_b + "-29 23:59:59"));
				}else{
					childVO.setEnd_date(new UFDateTime(period_code_b + "-28 23:59:59"));
				}
			}else{
				switch (i) {
				case 1:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				case 3:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				case 4:childVO.setEnd_date(new UFDateTime(period_code_b + "-30 23:59:59"));
					break;
				case 5:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				case 6:childVO.setEnd_date(new UFDateTime(period_code_b + "-30 23:59:59"));
					break;
				case 7:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				case 8:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				case 9:childVO.setEnd_date(new UFDateTime(period_code_b + "-30 23:59:59"));
					break;
				case 10:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				case 11:childVO.setEnd_date(new UFDateTime(period_code_b + "-30 23:59:59"));
					break;
				case 12:childVO.setEnd_date(new UFDateTime(period_code_b + "-31 23:59:59"));
					break;
				
				}
				
			}
			childrenVO[i-1] = childVO;
		}
		billVO.setChildrenVO(childrenVO);
	}

	public Map<String,String> periodCommit(String id) {
		return processBeforeCommitUnCommitByProc(id,COMMIT);
	}

	public Map<String,String> periodUncommit(String id) {
		return processBeforeCommitUnCommitByProc(id,UNCOMMIT);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private  Map<String,String> processBeforeCommitUnCommitByProc(String id,int type) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		final Map<String,String> returnMsgs = new HashMap<String,String>();
		// 存储过程名称
		final String PROC_NAME = "ts_account_period_proc";
		final String TYPE = type + "";
		final String PK = id;
		final String PK_USER = WebUtils.getLoginInfo().getPk_user();
		final String EMPTY = "";

		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, PK);
					cs.setString(2, TYPE);
					cs.setString(3, PK_USER);
					cs.setString(4, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						returnMsgs.put("msg",rs.getString(1));
						returnMsgs.put("type",rs.getString(2));
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			logger.info(e.getMessage());
			// 出现错误返回空接可以了
			return null;
		}
		if (returnMsgs.size() > 0) {
			return returnMsgs;
		}
		return null;
	}
	

}
