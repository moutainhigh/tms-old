package org.nw.web.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.constants.FunRegisterConst;
import org.nw.dao.NWDao;
import org.nw.redis.RedisDao;
import org.nw.service.sys.FunService;
import org.nw.service.sys.impl.FunServiceImpl;
import org.nw.utils.ParameterHelper;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.SystemLogVO;
import org.nw.web.utils.WebUtils;

/**
 * 权限过滤器
 * 
 * @author xuqc
 * 
 */
public class NWPermissionFilter implements Filter {
	private static final Log log = LogFactory.getLog(NWPermissionFilter.class);
	// 用户自定义不过滤的页面，如首页，这些页面与COMMON_RESOURCE_NAME的判断逻辑有些不同
	private static String ignoreCustomizePage = null;
	private static String[] ignoreCustomizePageAry = null;

	// 该类不归spring管理,不能使用自动注入
	private FunService funService = new FunServiceImpl();

	public void init(FilterConfig filterConfig) throws ServletException {
		ignoreCustomizePage = filterConfig.getInitParameter("ignoreCustomizePage");
		if(ignoreCustomizePage != null) {
			ignoreCustomizePageAry = ignoreCustomizePage.split("\\|");
		}
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// 不要用httpRequest.getProtocol()，因为http还是https是可变的，而资源前缀只配置http
		// 若端口为80，则数据库不存储其端口
		int serverPort = request.getServerPort();
		String serverName = "http://" + request.getServerName() + ":" + serverPort;
		if(serverPort == 80) {
			serverName = "http://" + request.getServerName();
		}
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		log.debug("serverName+contextPath=" + (serverName + contextPath));
		log.debug("servletPath=" + servletPath);

		// 1、common开头的url不进行权限检查,以ref/common开头的也不进行检查
		if(servletPath.startsWith("/common/") || servletPath.startsWith("/ref/common/")) {
			log.debug("跳过WEBNC客户端权限检查:" + servletPath);
			chain.doFilter(req, resp);
			return;
		}

		// 2、跳过用户自定义忽略的页面
		if(ignoreCustomizePageAry != null) {
			for(String ignoreCustomizePage : ignoreCustomizePageAry) {
				if(servletPath.startsWith(ignoreCustomizePage)) {
					log.debug("跳过用户自定义忽略的页面.servletPath:" + servletPath);
					chain.doFilter(req, resp);
					return;
				}
			}
		}

		// 3、对于系统管理员,不做权限控制
		// FIXME 也可以给管理员分配所有权限,但是目前超级管理员是隐藏的
		if(WebUtils.getLoginInfo().getPk_user().equals(Constants.SYSTEM_CODE)) {
			log.debug("系统管理员,不做权限控制!");
			chain.doFilter(req, resp);
			return;
		}

		// 4、检测检测是否有权限
		boolean hasPermission = false;
		String funCode = request.getParameter("funCode");
//		String baseSql = "select pk_fun,fun_code,fun_name,class_name,help_name,fun_property,parent_id,isbuttonpower from nw_fun WITH(NOLOCK) "
//				+ " where isnull(dr,0)=0 "; // 这里的查询应该包括锁定的资源
//		String sql = baseSql;
//		if(StringUtils.isNotBlank(funCode)) {
//			sql += " and fun_code='" + funCode + "'";
//		}
//		sql += " and class_name like '" + servletPath + "%'";
//		List<FunVO> funVOs = NWDao.getInstance().queryForListWithCache(sql, FunVO.class);
//		if(funVOs == null || funVOs.size() == 0) {
//			// 不使用funCode查询，因为如果是按钮请求，此时发送的fun_code是父级节点的fun_code
//			sql = baseSql + " and class_name like '" + servletPath + "%'";
//			funVOs = NWDao.getInstance().queryForListWithCache(sql, FunVO.class);
//		}
		List<FunVO> funVOs = RedisDao.getInstance().getFunVOForPermission(funCode, servletPath);
		if(funVOs != null && funVOs.size() > 0) {
			// 1.判断是否存在对应菜单
			if(funVOs.size() > 1) {
				log.warn("发现重复url的菜单，可能会干扰授权:");
				for(FunVO funVO : funVOs) {
					log.warn("funCode:" + funVO.getFun_code() + ",funName:" + funVO.getFun_name());
				}
			}
			// 1.1.判断该菜单是否有权限
			try {
				FunVO funVO = funVOs.get(0);
				if(funVO.getFun_property().intValue() == FunRegisterConst.LFW_FUNC_NODE) {
					// web节点
//					if(funService.isPowerByUserFun(WebUtils.getLoginInfo().getPk_user(), funVO.getPk_fun())) {
//						hasPermission = true;
//					}
					if(RedisDao.getInstance().getIsPowerByUserFun(WebUtils.getLoginInfo().getPk_user(), funVO.getPk_fun())){
						hasPermission = true;
					}
				} else if(funVO.getFun_property().intValue() == FunRegisterConst.POWERFUL_BUTTON) {
					// 按钮节点，只有注册了按钮，并且不给按钮分配权限
					// 得到父级节点
					//FunVO parentVO = funService.getByPrimaryKey(FunVO.class, funVO.getParent_id());
					FunVO parentVO = RedisDao.getInstance().getFunVO("pk_fun", funVO.getParent_id());
					if(parentVO.getIsbuttonpower().booleanValue()) {
						// 父级节点启用了按钮权限
						log.debug("父级节点[" + parentVO.getFun_name() + "]启用了按钮权限.");
						if(funService.isPowerByUserFun(WebUtils.getLoginInfo().getPk_user(), funVO.getPk_fun())) {
							hasPermission = true;
						}
//						if(RedisDao.getInstance().getIsPowerByUserFun(WebUtils.getLoginInfo().getPk_user(), funVO.getPk_fun())){
//							hasPermission = true;
//						}
					} else {
						log.info("父级节点[" + parentVO.getFun_name() + "]没有启用按钮权限，判定父级菜单是否有权限，如果有，则按钮也放行.");
						// 没有启用按钮权限，则判定父级菜单是否有权限，如果有，则按钮也放行
						if(funService.isPowerByUserFun(WebUtils.getLoginInfo().getPk_user(), parentVO.getPk_fun())) {
							hasPermission = true;
						}
//						if(RedisDao.getInstance().getIsPowerByUserFun(WebUtils.getLoginInfo().getPk_user(), funVO.getPk_fun())){
//							hasPermission = true;
//						}
					}

					// 如果是按钮的操作，根据配置是否记录日志
					if(WebUtils.getLoginInfo() != null && ParameterHelper.getSystemLog()) {
						SystemLogVO logVO = new SystemLogVO();
						logVO.setPk_fun(parentVO.getPk_fun());
						logVO.setProcess(funVO.getFun_name());
						logVO.setMemo("URL:" + funVO.getClass_name());
						logVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
						logVO.setCreate_time(DateUtils.formatDate(new Date(), DateUtils.DATETIME_FORMAT_HORIZONTAL));
						logVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(logVO);
						NWDao.getInstance().saveOrUpdate(logVO);
					}
				}
			} catch(Exception e) {
				log.error("权限检验出错！", e);
				if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
					throw new RuntimeException("权限检验出错！", e);
				}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
					throw new RuntimeException("Authority check error！", e);
				}
				throw new RuntimeException("权限检验出错！", e);
			}
		} else {
			// 2.1
			// 2.1.找不到对应菜单，放行！（这里默认使用乐观模式，对于未注册的url，一律放行）
			log.debug("发现未注册成菜单的url，默认放行，如果需要做权限控制，请注册成菜单并授权。");
			log.debug("servletPath=" + servletPath);
			hasPermission = true;
		}

		if(!hasPermission) {
			log.info("您没有访问该资源的权限! URL:" + servletPath);
			if(servletPath.endsWith("json")) {
				// Ajax请求
				String json = "{\"success\":false,\"msg\":\"您没有访问该资源[URL:" + servletPath + "]的权限!\"}";
				this.writeResp(request, response, json);
				return;
			} else {
				String errorMsg = "URL:" + contextPath + servletPath;
				errorMsg = new String(Base64.encodeBase64(errorMsg.getBytes()));
				response.sendRedirect(contextPath + "/forbidden.html?errorMsg=" + errorMsg);
				return;
			}
		}
		log.debug("权限过滤器检查通过:" + request.getRequestURL());

		chain.doFilter(req, resp);
	}

	public void destroy() {
	}

	private void writeResp(HttpServletRequest request, HttpServletResponse response, String json) throws IOException {
		response.setContentType("text/html");
		String encoding = request.getCharacterEncoding();
		if(encoding == null) {
			encoding = "UTF-8";
		}
		ServletOutputStream sos = response.getOutputStream();
		InputStream is = new ByteArrayInputStream(json.getBytes(encoding));
		byte[] buf = new byte[1024];
		int len;
		while((len = is.read(buf)) != -1) {
			sos.write(buf, 0, len);
		}
		is.close();
		sos.flush();
		sos.close();
	}
}
