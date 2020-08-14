package com.tms.service.report;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.CorpVO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.te.ExpAccidentVO;

/*KPI承运商报表总览*/
@Service
public class GrapDisplayServiceImpl extends TMSAbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO parentVO = new VOTableVO();
			parentVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.HEADITEMVO, ExpAccidentVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.PKFIELD, "pk_exp_accident");
			parentVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_exp_accident");
			parentVO.setAttributeValue(VOTableVO.VOTABLE, "ts_exp_accident");
			billInfo.setParentVO(parentVO);
		}
		return billInfo;
	}

	public boolean addCorrelateQuery(UiReportTempletVO templetVO, ParamVO paramVO) {
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<HashMap<String,String>> getCarrData(String year, String month, String carr_code, String corp_code){
		final List<HashMap<String,String>> retList = new ArrayList<HashMap<String,String>>();
		//存储过程名称
		final String KPI_INDEX_CARRIER_PROC = "ts_kpi_index_carrier_proc";
		final String YRAR = year;
		final String MONTH = month;
		final String CARR = carr_code;
		final String CORP = corp_code;
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(KPI_INDEX_CARRIER_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, YRAR);
					cs.setString(2, MONTH);
					cs.setString(3, CARR);
					cs.setString(4, CORP);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						fillResultfSetForGD(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return retList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<HashMap<String,String>> getCustData(String year, String month, String cust_code, String corp_code){
		final List<HashMap<String,String>> retList = new ArrayList<HashMap<String,String>>();
		//存储过程名称
		final String KPI_INDEX_CARRIER_PROC = "ts_kpi_index_customer_proc";
		final String YRAR = year;
		final String MONTH = month;
		final String CARR = cust_code;
		final String CORP = corp_code;
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(KPI_INDEX_CARRIER_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, YRAR);
					cs.setString(2, MONTH);
					cs.setString(3, CARR);
					cs.setString(4, CORP);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						fillResultfSetForGD(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return retList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<HashMap<String,String>> getEmpData(String year, String month, String emp_code, String corp_code){
		final List<HashMap<String,String>> retList = new ArrayList<HashMap<String,String>>();
		//存储过程名称
		final String KPI_INDEX_CARRIER_PROC = "kpi_index_emp_proc";
		final String YRAR = year;
		final String MONTH = month;
		final String CARR = emp_code;
		final String CORP = corp_code;
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 4;
					String storedProc = DaoHelper.getProcedureCallName(KPI_INDEX_CARRIER_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, YRAR);
					cs.setString(2, MONTH);
					cs.setString(3, CARR);
					cs.setString(4, CORP);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						fillResultfSetForGD(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return retList;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForGD(List<HashMap<String,String>> resutlLists, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("code", rs.getString("kpi_index_code"));
		resut.put("name", rs.getString("kpi_index_name"));
		resut.put("exp_count", rs.getString("exp_count"));
		resut.put("count", rs.getString("count"));
		resut.put("percent", rs.getString("exp_percent"));
		resut.put("score", rs.getString("score"));
		resut.put("total_score", rs.getString("total_score"));
		
		resutlLists.add(resut);
	}
	
	public CarrierVO[] getCarrierByCorpCond(){
		CarrierVO[] carrierVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarrierVO.class,CorpHelper.getCurrentCorpWithChildren());
		return carrierVOs;
	}
	
	public CustomerVO[] getCustomerByCorpCond(){
		CustomerVO[] customerVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CustomerVO.class, CorpHelper.getCurrentCorpWithChildren());
		return customerVOs;
	}
	
	public CorpVO[] getCorpByCorpCond(){
		CorpVO[] corpVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CorpVO.class,"pk_corp in (" + CorpHelper.getCurrentCorpWithChildren2()+ ")");
		return corpVOs;
	}
}
