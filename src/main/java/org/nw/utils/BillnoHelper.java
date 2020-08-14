package org.nw.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.service.sys.BillnoService;
import org.nw.service.sys.impl.BillnoServiceImpl;
import org.nw.vo.sys.BillnoRuleVO;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
/**
 * 单据号工具类，生成单据号
 * 
 * @author xuqc
 * @date 2012-7-7 下午11:55:33
 */
public class BillnoHelper {

	static Logger logger = Logger.getLogger(BillnoHelper.class);

	public static final int serial_no_count = 4;

	//private static final byte[] lock = new byte[0];

	/**
	 * 在没有定义单据号生成规则的情况下，根据单据类型和默认的规则生成单据号
	 * 
	 * @param bill_type
	 * @return
	 */
	public static String generateBillnoByDefault(String bill_type) {
		if(StringUtils.isBlank(bill_type)) {
			return null;
		}
		StringBuffer billno = new StringBuffer();
		billno.append(bill_type);
		billno.append(DateUtils.getYear());
		billno.append(DateUtils.getMonthStr());
		billno.append(DateUtils.getDayStr());
		// 流水号位数
		if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			billno.append(getSqlServerSerialno(bill_type, serial_no_count));
		}else if (DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			billno.append(getOracleSerialno(bill_type, serial_no_count));
		}else{
			billno.append(getSqlServerSerialno(bill_type, serial_no_count));
		}
		
		return billno.toString();
	}

	/**
	 * 根据单据类型返回单据号
	 * 
	 * @param bill_type
	 * @return
	 */
	public static String generateBillno(String bill_type) {
		if(StringUtils.isBlank(bill_type)) {
			return null;
		}
		BillnoService billnoService = new BillnoServiceImpl();
		BillnoRuleVO ruleVO = billnoService.getByBillType(bill_type);
		if(ruleVO == null) {
			throw new BusiException("没有定义单据号生成规则，单据类型[?]！",bill_type);
		}
		StringBuffer billno = new StringBuffer();
		if(ruleVO.getIs_bill_type().booleanValue()) {
			billno.append(bill_type);
		}
		if(ruleVO.getIs_pk_corp().booleanValue()) {
			billno.append(WebUtils.getLoginInfo().getCorp_code()); // 这里使用的是公司编码
		}
		if(ruleVO.getIs_pk_dept().booleanValue()) {
			billno.append(WebUtils.getLoginInfo().getDept_Code());// 这里使用的是部门编码
		}
		if(ruleVO.getIs_year().booleanValue()) {
			billno.append(DateUtils.getYear());
		}
		if(ruleVO.getIs_month().booleanValue()) {
			billno.append(DateUtils.getMonthStr());
		}
		if(ruleVO.getIs_day().booleanValue()) {
			billno.append(DateUtils.getDayStr());
		}
		if(ruleVO.getIs_serial_no().booleanValue()) {
			// 流水号位数
			//判断数据库类型
			if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
				billno.append(getSqlServerSerialno(bill_type, ruleVO.getSerial_no_count()));
			}else if (DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
				billno.append(getOracleSerialno(bill_type, serial_no_count));
			}else{
				billno.append(getSqlServerSerialno(bill_type, serial_no_count));
			}
			
		}
		return billno.toString();
	}

	/**
	 * 返回可用的流水号，如果当前的单据类型已经存在流水号，则返回流水号+1，否则返回1， 返回的数值需要根据位数进行补齐，如0001
	 * 
	 * @param bill_type
	 * @param serial_no_count
	 *            //流水号的位数
	 * @return
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getSqlServerSerialno(String bill_type, int serial_no_count) {
			logger.info("同步块，返回可用的流水号...");
			
			// 生成序列号的存储过程名
			final String sp_billno_seq = "billno_sequence";
			final String final_billType = bill_type;
			final int final_serialNoCount = serial_no_count;
			final String final_pk_corp = (WebUtils.getLoginInfo() == null ? "0001" : WebUtils.getLoginInfo().getPk_corp());
			final List<String> retReturn = new ArrayList<String>();
			
			try {
				NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
					public CallableStatement createCallableStatement(Connection conn) throws SQLException {
						// 设置存储过程参数
						int count = 3;
						String storedProc = DaoHelper.getProcedureCallName(sp_billno_seq, count);
						CallableStatement cs = conn.prepareCall(storedProc);
						cs.setString(1, final_billType);
						cs.setInt(2, final_serialNoCount);
						cs.setString(3, final_pk_corp);
						return cs;
					}
				}, new CallableStatementCallback() {
					public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
						ResultSet rs = cs.executeQuery();
						while (rs.next()) {
							retReturn.add(rs.getString("MaxDicId"));
						}
						cs.close();
						return null;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
			return retReturn.get(0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getOracleSerialno(String bill_type, int serial_no_count) {
			logger.info("同步块，返回可用的流水号...");
			final String procName = "billno_sequence";
			// 生成序列号的存储过程名
			final String final_billType = bill_type;
			final int final_serialNoCount = serial_no_count;
			final String final_pk_corp = (WebUtils.getLoginInfo() == null ? "0001" : WebUtils.getLoginInfo().getPk_corp());
			final int prametCount = 4;
			final List<String> retReturn = new ArrayList<String>();
			try {
				NWDao.getInstance().getJdbcTemplate().execute(DaoHelper.getProcedureCallName(procName,prametCount),new CallableStatementCallback() {
					public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
						cs.setString(1, final_billType);
		                cs.setInt(2, final_serialNoCount);
		                cs.setString(3, final_pk_corp);
		                cs.registerOutParameter(4, Types.VARCHAR);
						// 查询结果集
						cs.execute();
						retReturn.add(cs.getString(4));
						cs.close();
						return null;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
			return retReturn.get(0);
	}
}
