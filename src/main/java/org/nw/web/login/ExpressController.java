package org.nw.web.login;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.web.AbsBaseController;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 快递查询
 * @author XIA
 * @date 2016-7-17 
 */
@Controller
public class ExpressController extends AbsBaseController {

	/**
	 * 转到查询页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/express.html")
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/express.jsp");
	}

	/**
	 * 从tracking信息里，获取快递信息
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/loadExpress.json")
	@ResponseBody
	public Map<String, Object> loadExpress(HttpServletRequest request, HttpServletResponse response) {
		String billno = request.getParameter("billno");
		String errorMsg = "";
		final List<Map<String,String>> RET_LIST = new ArrayList<Map<String,String>>();
		final String PROC_NAME = "ts_inv_tracking_proc";
		final String ID = billno;
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection con) throws SQLException {
					int count=1;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = con.prepareCall(storedProc);
					cs.setString(1, ID);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					cs.execute();
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						fillResultfSet(RET_LIST, rs);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			errorMsg = e.getMessage();
			e.printStackTrace();
		}
		if(RET_LIST.size() == 0){
			errorMsg = "抱歉，您查询的单号暂时没有查询结果！请检查单号是否正确?";
		}

		if(StringUtils.isBlank(errorMsg)) {
			// 操作成功
			return this.genAjaxResponse(true, null, RET_LIST);
		} else {
			return this.genAjaxResponse(false, errorMsg, null);
		}
	}
	
	public static void fillResultfSet(List<Map<String,String>> resutlLists, ResultSet rs) throws SQLException{
		Map<String,String> map = new HashMap<String, String>();
		map.put("status", rs.getString("status"));
		map.put("tracking_time", rs.getString("tracking_time"));
		map.put("tracking_memo", rs.getString("tracking_memo"));
		resutlLists.add(map);
	}
}
