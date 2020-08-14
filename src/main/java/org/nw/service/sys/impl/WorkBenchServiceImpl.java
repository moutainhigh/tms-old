package org.nw.service.sys.impl;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.WorkBenchService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.WorkBenchVO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.vo.base.CarrierVO;

/**
 * 工作台接口实现类
 * 
 * @author XIA
 * @date 2016-6-27 下午06:21:39
 */
@Service
public class WorkBenchServiceImpl extends AbsToftServiceImpl implements WorkBenchService {
	
	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, WorkBenchVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, "pk_workbench");
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public List<WorkBenchVO> getWorkBenchVOs(){
		String sql = "select * from nw_workbench with(nolock) "
				+ "where isnull(dr,0) =0 and isnull(locked_flag,'N')='N'";
		List<WorkBenchVO> workBenchVOs = NWDao.getInstance().queryForList(sql, WorkBenchVO.class);
		return workBenchVOs;
	}

	public List<String> getWorkBenchPKsByRole(String pk_role) {
		String sql = "select pk_workbench from nw_power_workbench with(nolock)  "
				+ "where isnull(dr,0) =0 and pk_role =?";
		List<String> workBenchPKs = NWDao.getInstance().queryForList(sql, String.class,pk_role);
		return workBenchPKs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getGoodsAmountData(String stratTime, String endTime, String timeId) {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_GOODS_AMOUNT_PROC = "ts_work_goods_amount_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12,CorpHelper.getCurrentCorpWithChildren().length()-1);;
		final String time_id = timeId;
		final String start = stratTime;
		final String end  = endTime;
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_GOODS_AMOUNT_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForGoodsAmount(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String,Object> result = new HashMap<String, Object>();
		List<String> dates = new ArrayList<String>();
		List<String> weights = new ArrayList<String>();
		List<String> volumes = new ArrayList<String>();
		List<String> packNums = new ArrayList<String>();
		List<String> orderNums = new ArrayList<String>();
		//格式化数据
		for(Map<String,String> ret : retList){
			dates.add(ret.get("date_time"));
			weights.add(ret.get("weight"));
			volumes.add(ret.get("volume"));
			packNums.add(ret.get("pack_num"));
			orderNums.add(ret.get("order_num"));
		}
		result.put("dates", dates);
		result.put("weights", weights);
		result.put("volumes", volumes);
		result.put("packNums", packNums);
		result.put("orderNums", orderNums);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForGoodsAmount(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("date_time", rs.getString("date_time"));
		resut.put("weight", rs.getString("weight"));
		resut.put("volume", rs.getString("volume"));
		resut.put("pack_num", rs.getString("pack_num"));
		resut.put("order_num", rs.getString("order_num"));
		retList.add(resut);
	}


	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getDateAmountData() {
		// 用于存储查询数据
		final Map<String, String> dayRet = new HashMap<String, String>();
		final Map<String, String> monthRet = new HashMap<String, String>();
		// 存储过程名称
		final String TS_WORK_GOODS_AMOUNT_DATE_PROC = "ts_work_goods_amount_date_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12,
				CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_GOODS_AMOUNT_DATE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet dayRs = cs.getResultSet();
					while (dayRs.next()) {
						fillResultfSetForDayGoods(dayRet, dayRs);
						break;
					}
					if (cs.getMoreResults()) {
						ResultSet monthRs = cs.getResultSet();
						while (monthRs.next()) {
							fillResultfSetForMonthGoods(monthRet, monthRs);
							break;
						}
					}

					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("month", monthRet);
		result.put("day", dayRet);
		return result;
	}
	
	@SuppressWarnings({ })
	public static void fillResultfSetForDayGoods(Map<String,String> dayRet, ResultSet rs) throws SQLException{
		dayRet.put("weight", rs.getString("weight"));
		dayRet.put("volume", rs.getString("volume"));
		dayRet.put("packNum", rs.getString("pack_num"));
		dayRet.put("orderNum", rs.getString("order_num"));
	}
	
	@SuppressWarnings({ })
	public static void fillResultfSetForMonthGoods(Map<String,String> monthRet, ResultSet rs) throws SQLException{
		monthRet.put("weight", rs.getString("weight"));
		monthRet.put("volume", rs.getString("volume"));
		monthRet.put("packNum", rs.getString("pack_num"));
		monthRet.put("orderNum", rs.getString("order_num"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getRouteAnalyze() {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_GOODS_ROUTE_ANALYZE_PROC = "ts_work_goods_route_analyze_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12, CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_GOODS_ROUTE_ANALYZE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForRouteAnalyze(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> routes = new ArrayList<String>();
		List<String> weights = new ArrayList<String>();
		List<String> volumes = new ArrayList<String>();
		List<String> packNums = new ArrayList<String>();
		List<String> orderNums = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			routes.add(ret.get("route"));
			weights.add(ret.get("weight"));
			volumes.add(ret.get("volume"));
			packNums.add(ret.get("pack_num"));
			orderNums.add(ret.get("order_num"));
		}
		result.put("routes", routes);
		result.put("weights", weights);
		result.put("volumes", volumes);
		result.put("packNums", packNums);
		result.put("orderNums", orderNums);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForRouteAnalyze(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("route", rs.getString("route"));
		resut.put("weight", rs.getString("weight"));
		resut.put("volume", rs.getString("volume"));
		resut.put("pack_num", rs.getString("pack_num"));
		resut.put("order_num", rs.getString("order_num"));
		retList.add(resut);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getCustAnalyze() {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		final List<String> custList = new ArrayList<String>();
		// 存储过程名称
		final String TS_WORK_GOODS_CUST_ANALYZE_PROC = "ts_work_goods_cust_analyze_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12,
				CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_GOODS_CUST_ANALYZE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForCustAnalyze(retList, rs);
					}
					if (cs.getMoreResults()) {
						ResultSet monthRs = cs.getResultSet();
						while (monthRs.next()) {
							fillResultfSetForCustAnalyzeCust(custList, monthRs);
						}
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> weights = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> volumes = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> packNums = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> orderNums = new ArrayList<Map<String, Object>>();
		for(Map<String, String> ret : retList){
			Map<String,Object> weight = new HashMap<String, Object>();
			weight.put("value", ret.get("weight"));
			weight.put("name", ret.get("cust"));
			weights.add(weight);
			
			Map<String,Object> volume = new HashMap<String, Object>();
			volume.put("value", ret.get("volume"));
			volume.put("name", ret.get("cust"));
			volumes.add(volume);
			
			Map<String,Object> packNum = new HashMap<String, Object>();
			packNum.put("value", ret.get("pack_num"));
			packNum.put("name", ret.get("cust"));
			packNums.add(packNum);
			
			Map<String,Object> orderNum = new HashMap<String, Object>();
			orderNum.put("value", ret.get("order_num"));
			orderNum.put("name", ret.get("cust"));
			orderNums.add(orderNum);
			
		}
		result.put("custs", custList);
		result.put("weights", weights);
		result.put("volumes", volumes);
		result.put("packNums", packNums);
		result.put("orderNums", orderNums);
		return result;
	}
	
	@SuppressWarnings({ })
	public static void fillResultfSetForCustAnalyzeCust(List<String> custList, ResultSet rs) throws SQLException{
		custList.add(rs.getString("cust"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForCustAnalyze(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("cust", rs.getString("cust"));
		resut.put("weight", rs.getString("weight"));
		resut.put("volume", rs.getString("volume"));
		resut.put("pack_num", rs.getString("pack_num"));
		resut.put("order_num", rs.getString("order_num"));
		retList.add(resut);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getChargeAmountData(String stratTime, String endTime, String timeId) {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_CHARGE_AMOUNT_PROC = "ts_work_charge_amount_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren2().replaceAll("'", "");
		final String time_id = timeId;
		final String start = stratTime;
		final String end = endTime;
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_CHARGE_AMOUNT_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForChargeAmount(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> dates = new ArrayList<String>();
		List<String> reces = new ArrayList<String>();
		List<String> pays = new ArrayList<String>();
		List<String> fees = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			dates.add(ret.get("date_time"));
			reces.add(ret.get("rece"));
			pays.add(ret.get("pay"));
			fees.add(ret.get("fee"));
		}
		result.put("dates", dates);
		result.put("reces", reces);
		result.put("pays", pays);
		result.put("fees", fees);
		return result;
		
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForChargeAmount(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("date_time", rs.getString("date_time"));
		resut.put("rece", rs.getString("rece"));
		resut.put("pay", rs.getString("pay"));
		resut.put("fee", rs.getString("fee"));
		retList.add(resut);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getChargeDateAmount() {
		// 用于存储查询数据
		final Map<String, String> dayRet = new HashMap<String, String>();
		final Map<String, String> monthRet = new HashMap<String, String>();
		// 存储过程名称
		final String TS_WORK_CHARGE_AMOUNT_DATE_PROC = "ts_work_charge_amount_date_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12, CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_CHARGE_AMOUNT_DATE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet dayRs = cs.getResultSet();
					while (dayRs.next()) {
						fillResultfSetForDayCharge(dayRet, dayRs);
					}
					if (cs.getMoreResults()) {
						ResultSet monthRs = cs.getResultSet();
						while (monthRs.next()) {
							fillResultfSetForMonthCharge(monthRet, monthRs);
						}
					}

					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("month", monthRet);
		result.put("day", dayRet);
		return result;
	}

	public static void fillResultfSetForDayCharge(Map<String,String> dayRet, ResultSet rs) throws SQLException{
		dayRet.put("rece", rs.getString("rece"));
		dayRet.put("pay", rs.getString("pay"));
		dayRet.put("fee", rs.getString("fee"));
		dayRet.put("profit", rs.getString("profit"));
	}
	
	public static void fillResultfSetForMonthCharge(Map<String,String> monthRet, ResultSet rs) throws SQLException{
		monthRet.put("rece", rs.getString("rece"));
		monthRet.put("pay", rs.getString("pay"));
		monthRet.put("fee", rs.getString("fee"));
		monthRet.put("profit", rs.getString("profit"));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getReceAnalyze() {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_CHARGE_RECE_ANALYZE_PROC = "ts_work_charge_rece_analyze_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12,
				CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_CHARGE_RECE_ANALYZE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForReceAnalyze(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> carrs = new ArrayList<String>();
		List<String> amounts = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			carrs.add(ret.get("cust"));
			amounts.add(ret.get("amount"));
		}
		result.put("custs", carrs);
		result.put("amounts", amounts);
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForReceAnalyze(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("cust", rs.getString("cust"));
		resut.put("amount", rs.getString("amount"));
		retList.add(resut);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getPayAnalyze() {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_CHARGE_PAY_ANALYZE_PROC = "ts_work_charge_pay_analyze_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12,
				CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_CHARGE_PAY_ANALYZE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForPayAnalyze(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> carrs = new ArrayList<String>();
		List<String> amounts = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			carrs.add(ret.get("carr"));
			amounts.add(ret.get("amount"));
		}
		result.put("carrs", carrs);
		result.put("amounts", amounts);
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForPayAnalyze(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("carr", rs.getString("carr"));
		resut.put("amount", rs.getString("amount"));
		retList.add(resut);
	}

	public List<CarrierVO> getCarriers(String keyword) {
		String cond = CorpHelper.getCurrentCorpWithChildren();
		if(StringUtils.isNotBlank(keyword)){
			cond += " and (carr_name LIKE '%"+keyword+"%' OR carr_code LIKE '%"+keyword+"%' )";
		}
		String sql = "SELECT top 10 * FROM ts_carrier with(nolock) "
				+ " where isnull(dr,0)=0 and isnull(locked_flag,'N')='N'"
				+ " and " + cond;
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "SELECT * FROM ts_carrier "
					+ " where rownum < 11 and isnull(dr,0)=0 and isnull(locked_flag,'N')='N'"
					+ " and " + cond;
		}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "SELECT top 10 * FROM ts_carrier with(nolock) "
					+ " where isnull(dr,0)=0 and isnull(locked_flag,'N')='N'"
					+ " and " + cond;
		}
		List<CarrierVO> carrierVOs = NWDao.getInstance().queryForList(sql, CarrierVO.class);
		return carrierVOs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getKPIAmountData(String stratTime, String endTime, String timeId, String pk_carrier) {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_KPI_AMOUNT_PROC = "ts_work_kpi_amount_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12,
				CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = timeId;
		final String start = stratTime;
		final String end = endTime;
		final String EMPTY1 = pk_carrier;
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_KPI_AMOUNT_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForKPIAmount(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> indexs = new ArrayList<String>();
		List<String> scores = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			indexs.add(ret.get("index"));
			scores.add(ret.get("score"));
		}
		result.put("indexs", indexs);
		result.put("scores", scores);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForKPIAmount(List<HashMap<String, String>> retList, ResultSet rs)
			throws SQLException {
		HashMap<String, String> resut = new HashMap();
		resut.put("index", rs.getString("index"));
		resut.put("score", rs.getString("score"));
		retList.add(resut);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getKPIDateAmount() {
		// 用于存储查询数据
		final Map<String, String> dayRet = new HashMap<String, String>();
		final Map<String, String> monthRet = new HashMap<String, String>();
		// 存储过程名称
		final String TS_WORK_KPI_AMOUNT_DATE_PROC = "ts_work_kpi_amount_date_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12, CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_KPI_AMOUNT_DATE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet dayRs = cs.getResultSet();
					while (dayRs.next()) {
						fillResultfSetForDayKPI(dayRet, dayRs);
					}
					if (cs.getMoreResults()) {
						ResultSet monthRs = cs.getResultSet();
						while (monthRs.next()) {
							fillResultfSetForMonthKPI(monthRet, monthRs);
						}
					}

					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("month", monthRet);
		result.put("day", dayRet);
		return result;
	}

	public static void fillResultfSetForDayKPI(Map<String,String> dayRet, ResultSet rs) throws SQLException{
		dayRet.put("day_top1_name", rs.getString("day_top1_name"));
		dayRet.put("day_top1_score", rs.getString("day_top1_score"));
		dayRet.put("day_top2_name", rs.getString("day_top2_name"));
		dayRet.put("day_top2_score", rs.getString("day_top2_score"));
		dayRet.put("day_top3_name", rs.getString("day_top3_name"));
		dayRet.put("day_top3_score", rs.getString("day_top3_score"));
	}
	
	public static void fillResultfSetForMonthKPI(Map<String,String> monthRet, ResultSet rs) throws SQLException{
		monthRet.put("month_top1_name", rs.getString("month_top1_name"));
		monthRet.put("month_top1_score", rs.getString("month_top1_score"));
		monthRet.put("month_top2_name", rs.getString("month_top2_name"));
		monthRet.put("month_top2_score", rs.getString("month_top2_score"));
		monthRet.put("month_top3_name", rs.getString("month_top3_name"));
		monthRet.put("month_top3_score", rs.getString("month_top3_score"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getKPIRouteAnalyze() {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_KPI_ROUTE_ANALYZE_PROC = "ts_work_kpi_route_analyze_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12, CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_KPI_ROUTE_ANALYZE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForKPIRouteAnalyze(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> routes = new ArrayList<String>();
		List<String> scores = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			routes.add(ret.get("route"));
			scores.add(ret.get("score"));
		}
		result.put("routes", routes);
		result.put("scores", scores);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForKPIRouteAnalyze(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("route", rs.getString("route"));
		resut.put("score", rs.getString("score"));
		retList.add(resut);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getKPICarrAnalyze() {
		// 用于存储查询数据
		final List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();
		// 存储过程名称
		final String TS_WORK_KPI_CARR_ANALYZE_PROC = "ts_work_kpi_carr_analyze_proc";
		final String pk_corp = CorpHelper.getCurrentCorpWithChildren().substring(12, CorpHelper.getCurrentCorpWithChildren().length() - 1);
		final String time_id = "";
		final String start = "";
		final String end = "";
		final String EMPTY1 = "";
		final String EMPTY2 = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(TS_WORK_KPI_CARR_ANALYZE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, pk_corp);
					cs.setString(2, time_id);
					cs.setString(3, start);
					cs.setString(4, end);
					cs.setString(5, EMPTY1);
					cs.setString(6, EMPTY2);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs.next()) {
						fillResultfSetForKPICarrAnalyze(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> carrs = new ArrayList<String>();
		List<String> scores = new ArrayList<String>();
		// 格式化数据
		for (Map<String, String> ret : retList) {
			carrs.add(ret.get("carr"));
			scores.add(ret.get("score"));
		}
		result.put("carrs", carrs);
		result.put("scores", scores);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultfSetForKPICarrAnalyze(List<HashMap<String,String>> retList, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("carr", rs.getString("carr"));
		resut.put("score", rs.getString("score"));
		retList.add(resut);
	}
	
}
