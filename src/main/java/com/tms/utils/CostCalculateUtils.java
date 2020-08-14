package com.tms.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;

/**
 * 费用计算工具类
 * @author XIA
 * @date 2016 8 5
 */
public class CostCalculateUtils {
	
	public static final Integer RD = 1;
	public static final Integer PD = 2;
	
	public static final Integer RD_UNIT = 10;//应收正常计算
	
	public static final Integer RD_YUSEN = 11;//日邮应收计算
	
	public static final Integer PD_UNIT = 20;//应付正常计算
	public static final Integer PD_LOT = 21;//应付批次计算
	public static final Integer PD_RT_LOT = 22;//应付RT批次计算
	public static final Integer PD_YUSEN_LOT = 23;//应付RT批次计算
	
	/**
	 * 费用计算的分配类，根据传入的参数决定到底使用哪种计算方式
	 * @param ids
	 * @param type
	 * @param pk_user
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<Integer,List<String>> dispatcher(String[] ids,Integer type,String pk_user){
		if( ids == null || ids.length  == 0 || type == null  ){
			throw new BusiException("费用计算缺少参数！");
		}
		final Map<String,Integer> results = new HashMap<String,Integer>();
		// 存储过程名称
		final String PROC_NAME = "ts_cost_calculation_dispatcher_proc";
		final String IDS = NWUtils.join(ids, ",");
		final String TYPE = type.toString();
		final String USER = pk_user;
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, IDS);
					cs.setString(2, TYPE);
					cs.setString(3, USER);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs != null && rs.next()) {
						results.put(rs.getString(1), Integer.parseInt(rs.getString(2)));
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			throw new BusiException("费用计算方法分配器出现错误[?]！",e.getMessage());
		}
		//将或得到的结果 按照 Map<Integer,List<String>>形式重新组合
		Map<Integer,List<String>> map = new HashMap<Integer,List<String>>();
		for(String key : results.keySet()){
			List<String> idList = map.get(results.get(key));
			if(idList == null){
				idList = new ArrayList<String>();
				map.put(results.get(key), idList);
			}
			idList.add(key);
		}
		return map;
	}
}
