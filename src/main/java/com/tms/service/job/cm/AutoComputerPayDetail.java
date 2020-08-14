/**
 * 
 */
package com.tms.service.job.cm;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nw.BillStatus;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;

import com.tms.constants.SegmentConst;
import com.tms.service.cm.PayDetailService;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.te.EntLotVO;

/**
 * 自动按批次计算应付费用
 * 
 * @author lanjian
 * @Date 2015年12月27日 下午9:08:28
 *
 */
public class AutoComputerPayDetail implements IJobService  {

	private PayDetailService payDetailService;

	Logger logger = Logger.getLogger(this.getClass());

	StringBuffer mess = new StringBuffer();// 每个批次计算时的错误信息
	private ParamVO paramVO;

	/*
	 * 
	 * @see org.nw.job.IJobService#exec(org.nw.vo.sys.JobDefVO)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行应付费用计算任务-------------------");
		logger.info("从批次表中获取符合计算的批次单");
		// 用于存储查询数据
		final List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		final String TS_ASSIGN_RULE_PROC = "ts_auto_computer_pay_detail";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 0;
					String storedProc = DaoHelper.getProcedureCallName(TS_ASSIGN_RULE_PROC, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while(rs != null && rs.next()){
						Map<String,String> lotAndPkPayMap = new HashMap<String, String>();
						lotAndPkPayMap.put("pk", rs.getString(1));
						lotAndPkPayMap.put("lot", rs.getString(2));
						retList.add(lotAndPkPayMap);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(retList.size() == 0){
			return;
		}
		//对结果按批次分组
		Map<String,List<String>> groupMap = new HashMap<String, List<String>>();
		for(Map<String,String> lotAndPkPayMap : retList){
			String lot = lotAndPkPayMap.get("lot");
			String pk_pay_detail = lotAndPkPayMap.get("pk");
			List<String> pkList = groupMap.get(lot);
			if(pkList == null){
				pkList = new ArrayList<String>();
				groupMap.put(lot, pkList);
			}
			pkList.add(pk_pay_detail);
		}
		for(String key : groupMap.keySet()){
			payDetailService.doPayDetailRebuildBySegtype(paramVO, SegmentConst.SEG_TYPE_NORMAL, groupMap.get(key).toArray(new String[groupMap.get(key).size()]));
		}
		String sql = "UPDATE ts_entrust_lot SET def4 = 'Y' WHERE lot IN " + NWUtils.buildConditionString(new ArrayList<String>(groupMap.keySet()));
		NWDao.getInstance().update(sql);
		logger.info("-------------------应付费用计算任务执行完毕-------------------");
	}

	public void before(JobDefVO jobDefVO) {
		init();
	}

	// 初始化PayDetailServiceImpl 2015-10-27
	private void init() {
		payDetailService = SpringContextHolder.getBean("payDetailServiceImpl");
		if (payDetailService == null) {
			throw new BusiException("应付明细服务没有启动，服务ID：PayDetailServiceImpl");
		}
	}

	public void after(JobDefVO jobDefVO) {
	}

}
