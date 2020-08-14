package org.nw.web.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.redis.RedisDao;
import org.nw.service.sys.FunService;
import org.nw.utils.VariableHelper;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.PortletVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.AbsBaseController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.tms.service.kpi.LimitService;

/**
 * 默认首页(portal页)的操作类
 * 
 * @author xuqc
 * 
 */
@Controller
public class DefaultController extends AbsBaseController {

	@Autowired
	FunService funService;
	
	@Autowired
	LimitService limitService;

	// 这个是作为变量放到查询出来的待办的map中的，前台可以通过这个key得到title
	// 比如：发货单[{vbillno}]待提交，查询后，将vbillno替换后，放入map中，以_title作为key
	public static String TITLE = "_title";
	
	public static String PK = "pk";

	/**
	 * 首页的默认打开页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/default.html")
	public ModelAndView defaultGo(HttpServletRequest request, HttpServletResponse response) {
		// 读取用户自定义的portlet
		List<PortletConfigVO> pcVOs = getPortletConfig(WebUtils.getLoginInfo().getPk_user(), WebUtils.getLoginInfo().getPk_corp());
		if(pcVOs == null) {
			pcVOs = new ArrayList<PortletConfigVO>();
		}
		request.setAttribute("pcVOs", JacksonUtils.writeValueAsString(pcVOs));

		return new ModelAndView("/default/index.jsp");
	}
	
	/**
	 * 订单数量工作台
	 * @author XIA
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/orderWorkBench.html")
	public ModelAndView orderWorkBench(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/default/autoLoad/orderWorkBench.jsp");
	}
	
	/**
	 * 费用展示工作台
	 * @author XIA
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/chargeWorkBench.html")
	public ModelAndView chargeWorkBench(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/default/autoLoad/chargeWorkBench.jsp");
	}
	
	/**
	 * kpi展示工作台
	 * @author XIA
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/kpiWorkBench.html")
	public ModelAndView kpiWorkBench(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/default/autoLoad/kpiWorkBench.jsp");
	}
	
	
	

	/**
	 * 查询最新的待办记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/getLatestTodo.html")
	public ModelAndView getLatestTodo(HttpServletRequest request, HttpServletResponse response) {
		String pk_portlet = request.getParameter("pk_portlet");
		if(StringUtils.isBlank(pk_portlet)) {
			throw new BusiException("加载待办数据时，主键参数不能为空！");
		}
		PortletVO vo = NWDao.getInstance().queryByCondition(PortletVO.class, "pk_portlet=?", pk_portlet);
		if(vo == null) {
			throw new BusiException("组件已经被删除，请重新刷新页面！");
		}
		List<HashMap> list = new ArrayList<HashMap>();
		if(StringUtils.isBlank(vo.getQuery_sql()) && StringUtils.isBlank(vo.getProce())) {
			throw new BusiException("请先定义组建的查询语句，组件编码是[?]！",vo.getPortlet_code());
		}
		StringBuffer billIds = new StringBuffer();
		//存储过程方式
		if(StringUtils.isNotBlank(vo.getProce())){
			//list =  limitService.getWarnByProce(vo.getProce(),WebUtils.getLoginInfo().getPk_user());
			HashMap aa = new HashMap();
			aa.put("pk", "0348fdf09acd49fd862d42fa0c72a0c0");
			aa.put("vbillno", "FHD20150602007");
			aa.put("ts", "2015-12-11 23:17:24");
			aa.put("time", "10");
			list = Arrays.asList(new HashMap[]{aa});
			
		} else if (StringUtils.isNotBlank(vo.getQuery_sql())){//sql语句方式
			if(vo.getQuery_sql() != null && vo.getQuery_sql().length() > 5) {
				String sql = VariableHelper.resolve(this.getTopQuerySql(vo), null);
				list = NWDao.getInstance().queryForList(sql, HashMap.class);
				String pkSql = VariableHelper.resolve(this.getPkQuerySql(vo), null);
				List<String> pks = NWDao.getInstance().queryForList(pkSql, String.class);
				if(pks != null && pks.size() > 0){
					for(String pk : pks){
						billIds.append(pk + ",");
					}
					if(billIds.length() > 0){
						billIds = billIds.delete(billIds.length()-1, billIds.length());
					}
				}
			}
		}
		request.setAttribute("billIds", billIds);
		for(HashMap map : list) {
			String title = VariableHelper.resolve(vo.getTitle_format(), map);
			map.put(TITLE, title);
			
		}
		
		request.setAttribute("dataList", list);

		// 返回功能节点对应的url
		// String sql =
		// "select fun_code,fun_name,class_name from nw_fun where isnull(dr,0)=0 and fun_code=?";
		FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, "fun_code=?", vo.getFun_code());
		if(funVO == null) {
			throw new BusiException("该功能节点已经被删除或锁定，功能编码[?]！",vo.getFun_code());
		}
		request.setAttribute("funVO", funVO);
		request.setAttribute("pk_portlet", pk_portlet);
		return new ModelAndView("/default/autoLoad/portlet.jsp");
	}

	/**
	 * 这里要区分数据库类型
	 * 
	 * @param vo
	 * @return
	 */
	private String getTopQuerySql(PortletVO vo) {
		if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("select ").append(" A.* from (");
			sqlBuf.append(vo.getQuery_sql());
			if(StringUtils.isNotBlank(vo.getQuery_where())) {
				if(vo.getQuery_sql().indexOf("where") == -1) {
					sqlBuf.append(" where ");
				} else {
					sqlBuf.append(" and ");
				}
				sqlBuf.append(vo.getQuery_where());
			}
			sqlBuf.append(") A ");
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				sqlBuf.append(" ");
				sqlBuf.append(vo.getOrder_by());
			}
			sqlBuf.append(" LIMIT 0,").append(vo.getDisplay_num());
			return sqlBuf.toString();
		} else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())) {
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("select top ").append(vo.getDisplay_num()).append(" A.* from (");
			sqlBuf.append(vo.getQuery_sql());
			if(StringUtils.isNotBlank(vo.getQuery_where())) {
				if(vo.getQuery_sql().indexOf("where") == -1) {
					sqlBuf.append(" where ");
				} else {
					sqlBuf.append(" and ");
				}
				sqlBuf.append(vo.getQuery_where());
			}
			sqlBuf.append(") A ");
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				sqlBuf.append(" ");
				sqlBuf.append(vo.getOrder_by());
			}
			return sqlBuf.toString();
		}  else if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())) {
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("select ").append(" A.* from (");
			sqlBuf.append(vo.getQuery_sql());
			if(StringUtils.isNotBlank(vo.getQuery_where())) {
				if(vo.getQuery_sql().indexOf("where") == -1) {
					sqlBuf.append(" where ");
				} else {
					sqlBuf.append(" and ");
				}
				sqlBuf.append(vo.getQuery_where());
			}
			sqlBuf.append(") A where rownum < ").append(vo.getDisplay_num());
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				sqlBuf.append(" ");
				sqlBuf.append(vo.getOrder_by());
			}
			return sqlBuf.toString();
		} else {
			logger.warn("不支持的数据库类型：" + NWDao.getCurrentDBType());
			return null;
		}
	}
	
	/**
	 * 这里要区分数据库类型
	 * 
	 * @param vo
	 * @return
	 */
	private String getPkQuerySql(PortletVO vo) {
		if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("select ").append(" A.pk from (");
			if(StringUtils.isNotBlank(vo.getQuery_where())) {
				if(vo.getQuery_sql().indexOf("where") == -1) {
					sqlBuf.append(" where ");
				} else {
					sqlBuf.append(" and ");
				}
				sqlBuf.append(vo.getQuery_where());
			}
			sqlBuf.append(") A ");
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				sqlBuf.append(" ");
				sqlBuf.append(vo.getOrder_by());
			}
			sqlBuf.append(" LIMIT 0,").append(vo.getDisplay_num());
			return sqlBuf.toString();
		} else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())) {
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("select ").append(" A.pk from (");
			sqlBuf.append(vo.getQuery_sql());
			if(StringUtils.isNotBlank(vo.getQuery_where())) {
				if(vo.getQuery_sql().indexOf("where") == -1) {
					sqlBuf.append(" where ");
				} else {
					sqlBuf.append(" and ");
				}
				sqlBuf.append(vo.getQuery_where());
			}
			sqlBuf.append(") A ");
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				sqlBuf.append(" ");
				sqlBuf.append(vo.getOrder_by());
			}
			return sqlBuf.toString();
		} else if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())) {
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("select ").append(" A.pk from (");
			sqlBuf.append(vo.getQuery_sql());
			if(StringUtils.isNotBlank(vo.getQuery_where())) {
				if(vo.getQuery_sql().indexOf("where") == -1) {
					sqlBuf.append(" where ");
				} else {
					sqlBuf.append(" and ");
				}
				sqlBuf.append(vo.getQuery_where());
			}
			sqlBuf.append(") A ");
			if(StringUtils.isNotBlank(vo.getOrder_by())) {
				sqlBuf.append(" ");
				sqlBuf.append(vo.getOrder_by());
			}
			return sqlBuf.toString();
		} else {
			logger.warn("不支持的数据库类型：" + NWDao.getCurrentDBType());
			return null;
		}
	}

	/**
	 * 返回用户的门户配置，这里要注意，如果功能节点已经被锁定了。那么需要过滤这个节点
	 * 
	 * @param pk_user
	 * @param pk_corp
	 * @return
	 */
	public List<PortletConfigVO> getPortletConfig(String pk_user, String pk_corp) {
		if(StringUtils.isBlank(pk_user) || StringUtils.isBlank(pk_corp)) {
			return null;
		}
		UserVO userVO = NWDao.getInstance().queryByCondition(UserVO.class, "pk_user=?", pk_user);
		// 查询用户的方案
		String sql = "select pk_portlet_plan from nw_role_plan WITH(NOLOCK) where isnull(dr,0)=0 and "
				+ "pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where pk_user=? and isnull(dr,0)=0)";
		List<String> pkPlanAry = NWDao.getInstance().queryForList(sql, String.class, pk_user);
		if(pkPlanAry == null || pkPlanAry.size() == 0) {
			// 使用当前公司默认的方案
			sql = "select pk_portlet_plan from nw_portlet_plan WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and isnull(if_default,'N')='Y' and pk_corp=?";
			pkPlanAry = NWDao.getInstance().queryForList(sql, String.class, pk_corp);
		}
		if(pkPlanAry == null || pkPlanAry.size() == 0) {
			logger.warn("用户[" + userVO.getUser_code() + "]没有分配门户方案，所在的公司也没有默认的门户方案！");
			return null;
		}
		if(pkPlanAry.size() > 1) {
			logger.warn("用户[" + userVO.getUser_code() + "]存在" + pkPlanAry.size() + "条门户方案！");
		}
		String pk_plan = pkPlanAry.get(0);// 取第一条即可
		sql = "select a.*,b.column_index,b.display_order,b.if_popup,b.popup_time,c.fun_name,c.class_name from nw_portlet_plan_b b WITH(NOLOCK) "
				+ "left join nw_portlet a WITH(NOLOCK) on b.portlet_id=a.portlet_code left join nw_fun c WITH(NOLOCK) on c.fun_code=a.fun_code "
				+ "where isnull(c.dr,0)=0 and isnull(c.locked_flag,'N')='N' and isnull(a.dr,0)=0 and isnull(b.dr,0)=0  "
				+ "and b.pk_portlet_plan=? order by display_order";
		List<PortletConfigVO> list = NWDao.getInstance().queryForList(sql, PortletConfigVO.class, pk_plan);
		if(list == null || list.size() == 0) {
			logger.warn("用户[" + userVO.getUser_code() + "]没有分配门户方案,可能方案被删除或者被锁定！");
			return null;
		}
		// 所有有权限的菜单
		List<FunVO> funVOs = funService.getPowerFunVO(false);
		List<String> funCodeAry = new ArrayList<String>();
		for(FunVO funVO : funVOs) {
			funCodeAry.add(funVO.getFun_code());
		}
		// 只显示有权限的组件
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				PortletConfigVO pcVO = list.get(i);
				if(!funCodeAry.contains(pcVO.getFun_code())) {
					list.remove(pcVO);
					i--;
				}
			}
		}
		// 查询count数据
		Map<String,String> userPortletAndCountSql = new HashMap<String, String>();
		Map<String,Integer> userPortletAndTime = new HashMap<String, Integer>();
		for(int i = 0; i < list.size(); i++) {
			PortletConfigVO vo = list.get(i);
			if(StringUtils.isBlank(vo.getQuery_sql())) {
				continue;
			}
			try {// 可能定义的sql有错
				if(vo.getQuery_sql() != null && vo.getQuery_sql().length() > 5) {
					StringBuffer sqlBuf = new StringBuffer();
					sqlBuf.append("select count(1) from (");
					sqlBuf.append(vo.getQuery_sql());
					if(StringUtils.isNotBlank(vo.getQuery_where())) {
						if(vo.getQuery_sql().indexOf("where") == -1) {
							sqlBuf.append(" where ");
						} else {
							sqlBuf.append(" and ");
						}
						sqlBuf.append(vo.getQuery_where());
					}
					sqlBuf.append(") as A ");
					String countSql = VariableHelper.resolve(sqlBuf.toString(), null);
					Integer count = NWDao.getInstance().queryForObject(countSql, Integer.class);
					vo.setNum_count(count);
					vo.setCountSql(countSql);
					userPortletAndCountSql.put(pk_user + ":" + vo.getPk_portlet(), countSql);
					if(vo.getPopup_time() != null){
						userPortletAndTime.put(pk_user + ":" + vo.getPk_portlet() + ":" + vo.getPopup_time(), 1);
					}
				}
			} catch(Exception e) {
				//移除有错误的组件
				userPortletAndCountSql.remove(vo.getPk_portlet());
				list.remove(vo);
				i--;
				logger.error(e.getMessage(), e);
			}
		}
		//将信息存入redis
		RedisDao.getInstance().saveUserPortletAndCountSql(userPortletAndCountSql);
		RedisDao.getInstance().saveUserPortletAndTime(userPortletAndTime);
		for(PortletConfigVO vo:list) {
			RedisDao.getInstance().savePortlet(vo);
		}
		return list;
	}
}
