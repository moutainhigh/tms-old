package com.tms.service.kpi.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFBoolean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.service.kpi.LimitService;
import com.tms.vo.kpi.LimitVO;

@Service
public class LimitServiceImpl extends AbsToftServiceImpl implements LimitService {

	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, LimitVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, LimitVO.PK_LIMIT);
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_limit");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_limit");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
	
	public String getCodeFieldCode() {
		return "code";
	}
	public LimitVO getLimitVObycond(Integer type,Integer matter,Integer exp_type,
			String pk_carrier, String pk_customer, UFBoolean if_urgent, String item_code, 
			String goods_type ,String pk_address, String pk_corp){
		
		String sql = "select * from ts_limit where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
		if(StringUtils.isBlank(pk_carrier)){
			sql += " and pk_carrier is null ";
		}else{
			sql += " and pk_carrier ='" + pk_carrier + "'";
		}
		
		if(StringUtils.isBlank(pk_customer)){
			sql += " and pk_customer is null ";
		}else{
			sql += " and pk_customer ='" + pk_customer + "'";
		}
		
		if(if_urgent != null && if_urgent.equals(UFBoolean.TRUE)){
			sql += " and if_urgent = 'Y' ";
		}else{
			sql += " and isnull(if_urgent,'N')='N' ";
		}
		
		if(StringUtils.isBlank(item_code)){
			sql += " and item_code is null ";
		}else{
			sql += " and item_code ='" + item_code + "'";
		}
		
		if(StringUtils.isBlank(goods_type)){
			sql += " and goods_type is null ";
		}else{
			sql += " and goods_type ='" + goods_type + "'";
		}
		
		if(StringUtils.isBlank(pk_address)){
			sql += " and pk_address is null ";
		}else{
			sql += " and pk_address ='" + pk_address + "'";
		}
		sql += "and limit_type =? and matter=? and exp_type=?";
		List<LimitVO> limitVOs = NWDao.getInstance().queryForList(sql, LimitVO.class, type, matter, exp_type);
		if(limitVOs == null || limitVOs.size() == 0){
			return null;
		}else{
			return limitVOs.get(0);
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<HashMap<String,String>> matchLimit(){
		logger.info("--------------------开始匹配时效--------------------");
		//这里调用存储过程匹配时效
		//存储过程名称
		final String KPI_LIMIT_PROC = "ts_kpi_limit_proc";
		//这种存储过程调用必须要有参数
		final String EMPTY = "enpty";
		final List<HashMap<String,String>> retList = new ArrayList<HashMap<String,String>>();
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 1;
					String storedProc = DaoHelper.getProcedureCallName(KPI_LIMIT_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						fillResultfSetForLimit(retList, rs);
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
	public static void fillResultfSetForLimit(List<HashMap<String,String>> resutlLists, ResultSet rs) throws SQLException{
		HashMap<String,String> resut = new HashMap();
		resut.put("order_type", rs.getString("order_type"));
		resut.put("exp_type", rs.getString("exp_type"));
		resut.put("invoice_vbillno", rs.getString("invoice_vbillno"));
		
		resut.put("invoice_vbillno", rs.getString("invoice_vbillno"));
		resut.put("pk_customer", rs.getString("pk_customer"));
		resut.put("entrust_vbillno", rs.getString("entrust_vbillno"));
		resut.put("pk_carrier", rs.getString("pk_carrier"));
		
		resut.put("fee_vbillno", rs.getString("fee_vbillno"));
		resut.put("exp_time", rs.getString("exp_time"));
		
		resut.put("req_operate", rs.getString("req_operate"));
		resut.put("act_operate", rs.getString("act_operate"));
		resut.put("memo", rs.getString("memo"));
		resut.put("pk_corp", rs.getString("pk_corp"));
		
		resut.put("def1", rs.getString("def1"));
		resut.put("def2", rs.getString("def2"));
		resut.put("def3", rs.getString("def3"));
		resut.put("def4", rs.getString("def4"));
		resut.put("def5", rs.getString("def5"));
		resut.put("def6", rs.getString("def6"));
		resut.put("def7", rs.getString("def7"));
		resut.put("def8", rs.getString("def8"));
		resut.put("def9", rs.getString("def9"));
		resut.put("def10", rs.getString("def10"));
		resut.put("def11", rs.getString("def11"));
		resut.put("def12", rs.getString("def12"));
		
		resutlLists.add(resut);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<HashMap> getWarnByProce(String proce ,String pk_user){
		final List<HashMap> retList = new ArrayList<HashMap>();
		final String PROCE_NAME = proce;
		final String PK_USER = pk_user;
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					
					String storedProc = DaoHelper.getProcedureCallName(PROCE_NAME, 1);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, PK_USER);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						fillResultSetForProce(retList, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return retList ;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillResultSetForProce(List<HashMap> resutlLists, ResultSet rs) throws SQLException{
		HashMap resut = new HashMap();
		resut.put("pk", rs.getString("pk"));
		resut.put("vbillno", rs.getString("vbillno"));
		resut.put("ts", rs.getString("ts"));
		resut.put("time", rs.getString("time"));
		resutlLists.add(resut);
	}

}
