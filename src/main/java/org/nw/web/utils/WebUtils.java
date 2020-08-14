package org.nw.web.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.NCPasswordUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.basic.util.DESEncoderUtils;
import org.nw.basic.util.HardWareDetectTool;
import org.nw.basic.util.MD5EncoderUtils;

import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.utils.CorpHelper;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.UserVO;
import org.nw.vo.sys.WorkBenchVO;
import org.nw.web.listener.NWClientListener;
import org.nw.web.vo.LoginInfo;
import org.springframework.util.Assert;


/**
 * <b>WEBNC工具类</b><br>
 * 用于获取当前登录信息，以及客户端配置信息<br>
 * 
 * @author fangw
 * @date 2010-10-25
 */
public class WebUtils {
	private static final Log log = LogFactory.getLog(WebUtils.class);
	private static String contextPath = null;
	private static String serverInfo = null;
    private static Map<String, String> loginUserMap = new HashMap<String, String>();
    
	public static String getServerInfo() {
		return serverInfo;
	}

	public static void setServerInfo(String serverInfo) {
		WebUtils.serverInfo = serverInfo;
	}

	public static String getContextPath() {
		return contextPath;
	}

	/**
	 * 保留此方法，是为了简化代码书写
	 * 
	 * @return
	 */
	public static LoginInfo getLoginInfo() {
		if(ServletContextHolder.getRequest() != null) {
			HttpSession session = ServletContextHolder.getRequest().getSession(false);
			if(session != null) {
				Object obj = session.getAttribute(Constants.SESSION_NAME);
				if(obj != null) {
					return (LoginInfo) obj;
				}
			}
		} else {
			log.error("ServletContextHolder获取不到request，请检查ServletContextHolderFilter的配置！");
		}
		return null;
	}

	public static void setContextPath(String contextPath) {
		WebUtils.contextPath = contextPath;
	}

	/**
	 * 清除登录信息
	 */
	public static void clearLoginInfo() {
		if(ServletContextHolder.getRequest() != null) {
			if(ServletContextHolder.getRequest().getSession(false) != null) {
				HttpSession session = ServletContextHolder.getRequest().getSession(true);
				
				if(getLoginInfo() != null){
					String strCode = getLoginInfo().getUser_code();
					loginUserMap.remove(strCode);
				}
			
				session.removeAttribute(Constants.SESSION_NAME);
				session.invalidate();
			}
		} else {
			log.error("注意：ContextHolder获取不到request，请检查ContextHolderFilter的配置！");
		}
	}

	/**
	 * 获取当前用户登录环境,包括用户信息，公司信息，用户所在部门信息
	 * 
	 * @param principal
	 */
	public static void initLoginEnvironment(ServletRequest request) {
		try {
			String user_code = request.getParameter("user_code");
			String loginDate = request.getParameter("loginDate");
			String language = request.getParameter("language");

			LoginInfo loginInfo = new LoginInfo();
			loginInfo.setLoginDate(loginDate == null ? DateUtils.getCurrentDate() : loginDate);
			NWDao dao = NWDao.getInstance();
			// 用户信息
			UserVO userVO = dao.queryByCondition(UserVO.class, "user_code=?", user_code);
			// 用户vo不可能为空，否则不会调用到这边
			loginInfo.setUser_code(userVO.getUser_code());
			loginInfo.setUser_name(userVO.getUser_name());
			loginInfo.setPassword(userVO.getUser_password());
			loginInfo.setPk_user(userVO.getPk_user());
			//2015 11 16 yaojiie 用户类型，客户，承运商
			loginInfo.setUser_type(userVO.getUser_type());
			loginInfo.setPk_customer(userVO.getPk_customer());
			loginInfo.setPk_carrier(userVO.getPk_carrier());
			loginInfo.setPk_address(userVO.getPk_address());
			loginInfo.setLanguage(language);
			// 公司信息
			CorpVO corpVO = dao.queryByCondition(CorpVO.class, "pk_corp=?", userVO.getPk_corp());
			if(corpVO == null) {
				if(userVO.getUser_type().intValue() != Constants.USER_TYPE.ADMIN.intValue()) {
					// 如果不是管理员
					log.warn("用户" + user_code + "所在的公司不存在，或已经被删除！");
				}
			} else {
				loginInfo.setPk_corp(corpVO.getPk_corp());
				loginInfo.setCorp_code(corpVO.getCorp_code());
				loginInfo.setCorp_name(corpVO.getCorp_name());
			}

			// 部门信息
			DeptVO deptVO = dao.queryByCondition(DeptVO.class, "pk_dept=?", userVO.getPk_dept());
			if(deptVO == null) {
				if(userVO.getUser_type().intValue() != Constants.USER_TYPE.ADMIN.intValue()) {
					// 如果不是管理员
					log.warn("用户" + user_code + "所在的部门不存在，或已经被删除！");
				}
			} else {
				loginInfo.setPk_dept(deptVO.getPk_dept());
				loginInfo.setDept_Code(deptVO.getDept_code());
				loginInfo.setDept_name(deptVO.getDept_name());
			}
			String sql = "";
			if(DB_TYPE.SQLSERVER.equals(dao.getDatabaseType())){
				sql = "SELECT nw_workbench.* FROM nw_workbench WITH(NOLOCK) "
						+ " LEFT JOIN nw_power_workbench WITH(NOLOCK) ON nw_workbench.pk_workbench = nw_power_workbench.pk_workbench "
						+ " LEFT JOIN nw_user_role WITH(NOLOCK) ON nw_power_workbench.pk_role = nw_user_role.pk_role  "
						+ " WHERE isnull(nw_workbench.dr,0)=0 AND isnull(nw_power_workbench.dr,0)=0 AND isnull(nw_user_role.dr,0)=0 "
						+ " AND isnull(nw_workbench.locked_flag,'N')='N' AND  nw_user_role.pk_user = ?";
			}else if(DB_TYPE.ORACLE.equals(dao.getDatabaseType())){
				sql = "SELECT nw_workbench.* FROM nw_workbench "
						+ " LEFT JOIN nw_power_workbench ON nw_workbench.pk_workbench = nw_power_workbench.pk_workbench "
						+ " LEFT JOIN nw_user_role ON nw_power_workbench.pk_role = nw_user_role.pk_role  "
						+ " WHERE isnull(nw_workbench.dr,0)=0 AND isnull(nw_power_workbench.dr,0)=0 AND isnull(nw_user_role.dr,0)=0 "
						+ " AND isnull(nw_workbench.locked_flag,'N')='N' AND  nw_user_role.pk_user = ?";
			}
			
			List<WorkBenchVO> workbenchs = NWDao.getInstance().queryForList(sql, WorkBenchVO.class, userVO.getPk_user());
			loginInfo.setWorkbenchs(workbenchs);

			//公司和子公司 yaojiie 2015 12 08
			String corpcond = CorpHelper.getCurrentCorpWithChildren2(userVO.getPk_corp());
			loginInfo.setCurrentCorpWithChildren(corpcond);
			
			addUsers(userVO.getUser_code());
			
			checkLicense();
			/**
			 * 这里必须先放入线程局部变量，否则下面将会用到loginInfo里的信息
			 */
			WebUtils.setLoginInfo(loginInfo);
		} catch(Exception e) {
			log.error("设置登录环境时出错！" + e);
			WebUtils.clearLoginInfo();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 判断条件 mac+cpu+beginTime+endTime+corperationNum+users
	 * songf 2016-01-13
	 */
	public static int checkLicense() {
		// 验证软件是否注册过了，没有注册的软件，登录始终不成功
		// 检查软件的license，查看license是否过期
		// 没有源代码，破解了也没用
		int remainDays = -1;
//		try {
//			InputStream in = WebUtils.class.getResourceAsStream("nw.lic");
//			if(in == null) {
//				// 没有授权文件，抛异常
//				throw new RuntimeException("您使用的软件版本已经过期！错误码：0");
//			} else {
//				BufferedReader br = new BufferedReader(new InputStreamReader(in));
//				String line = null, lic = null;
//				int index = 0;
//				while((line = br.readLine()) != null) {
//					if(index == 0) {
//						lic = line;
//					}
//					index++;
//				}
//				
//				DESEncoderUtils desEncoderUtils = new DESEncoderUtils();
//				String strDecrypt = desEncoderUtils.decrypt(lic);
//				
//				int flag1 = strDecrypt.indexOf("&");
//				String strEncryptHead = strDecrypt.substring(0, flag1);
//				String strBeginTime = strDecrypt.substring(flag1 + 1, flag1 + 11);
//				String strEndTime =  strDecrypt.substring(flag1 + 12, flag1 + 22);
//				int flag2 = strDecrypt.indexOf("^");
//				int flag3 = strDecrypt.indexOf("|");
//			
//			    int iNumber = Integer.parseInt(strDecrypt.substring(flag2+1, flag3));
//			   
//				int iUserCont = Integer.parseInt(strDecrypt.substring(flag3 + 1, strDecrypt.length()));
//				
//				String strMac = HardWareDetectTool.getMACAddress();
//				String strCpuInfo = HardWareDetectTool.getCPUSerial();
//				String strHead  =  "shanghai" + strMac + "runzhi" + strCpuInfo + "keji";
//				String strCurrEncrypt = MD5EncoderUtils.MD5Encrypt(strHead);
//				
//				if(strCurrEncrypt.compareToIgnoreCase(strEncryptHead) != 0){
//					throw new RuntimeException("您使用的软件版本已经过期！错误码：-1");
//				}
//				
//				SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd" );
//				Date dBeginTime = sdf.parse(strBeginTime);
//				Date dEndTime = sdf.parse(strEndTime);
//				Date dCurrent = new Date();
//				
//				
//				if(dBeginTime.after(dCurrent) || dEndTime.before(dCurrent)){
//					throw new RuntimeException("您使用的软件版本已经过期！错误码：-2");
//				}
//				
//				remainDays = new Double(DateUtils.getIntervalDays(dCurrent, dEndTime)).intValue();
//				
//				int iCorpNumber = getCorpNumber();
//				if(iCorpNumber > iNumber){
//					throw new RuntimeException("您使用的软件版本已经过期！错误码：-3");
//				}
//				
//				int iUsers = loginUserMap.size();
//				//if(iUsers >iUserCont ){
//				//	throw new RuntimeException("您使用的软件版本已经超出访问限制！错误码：-4");
//				//}
//			}
//		} catch(Exception ex) {
//			log.error("检查软件许可证时出现异常，异常信息：" + ex.getMessage());
//			ex.printStackTrace();
//			throw new RuntimeException(ex.getMessage());
//		}
		return remainDays;
	}
	/**
	 * 获取公司数
	 */
	public static int getCorpNumber(){
		String strSql = "SELECT pk_corp, dr, ts, corp_code, corp_name, shortname, url, address, zipcode, leaf_flag, fathercorp, corp_level,"
				+ " create_user, create_time, modify_user, modify_time, def1, def2, def3, def4, def5, def6, def7, def8, def9, def10"
				+ " FROM dbo.nw_corp WITH(NOLOCK) WHERE dr = 0";
		
		List<CorpVO> corpVOs = NWDao.getInstance().queryForList(strSql, CorpVO.class);
		if(corpVOs == null || corpVOs.size() == 0){
			return 0;
		}
		return corpVOs.size();
	}
	/**
	 * 增加用户
	 */
	public static void addUsers(String strUserCode){
		//所有的登录信息   
		boolean isExist = false;  
		  
		for (String username : loginUserMap.keySet()) {  
		//判断是否已经保存该登录用户的信息，是否为同一个用户进行重复登录  
		if(!username.equals(strUserCode) || loginUserMap.containsValue(strUserCode)){  
		continue;  
		}  
		isExist = true;  
		break;  
		}  
		  
		if(isExist){  
		//该用户已登录  
		//  
		}else {  
		//该用户没有登录  
		loginUserMap.put(strUserCode, strUserCode);  
		//  
	  }  
	
	}
	/**
	 * 检测软件的许可证 返回值>=0,是试用版本 返回值等于-1，是有许可证版本
	 */
	public static int checkLicense_old() {
		// 验证软件是否注册过了，没有注册的软件，登录始终不成功
		// 检查软件的license，查看license是否过期
		// 没有源代码，破解了也没用
		int remainDays = -1;
		try {
			InputStream in = WebUtils.class.getResourceAsStream("nw.lic");
			if(in == null) {
				// 没有授权文件，是试用版，30天的试用期
				String date = DateUtils.getCurrentDate();
				File trial = new File(WebUtils.class.getResource(".").getPath() + "trial");
				remainDays = 30;
				if(trial.exists()) {
					String firstDate = FileUtils.readFileToString(trial, "utf-8");
					int intervalDays = new Double(DateUtils.getIntervalDays(firstDate, date)).intValue();
					if(intervalDays > remainDays) {
						// 已经过了试用期了
						throw new RuntimeException("您的软件已经过了试用期，请联系销售顾问，非常感谢您的支持！");
					}
					remainDays = remainDays - intervalDays;
				} else {
					// 不存在，首次使用试用版本
					trial.createNewFile();
					FileUtils.writeStringToFile(trial, date, "utf-8");
				}
			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String line = null, lic = null;
				int index = 0;
				while((line = br.readLine()) != null) {
					if(index == 0) {
						lic = line;
					}
					index++;
				}
				if(lic.equals(NCPasswordUtils.encode("daobanzhewuchi"))) {
				} else {
					throw new RuntimeException("您使用的软件版本已经过期！");
				}
			}
		} catch(Exception ex) {
			log.error("检查软件许可证时出现异常，异常信息：" + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
		return remainDays;
	}

	/**
	 * 设置登录信息<br>
	 * 2011-4-8
	 */
	public static void setLoginInfo(LoginInfo loginInfo) {
		if(ServletContextHolder.getRequest() != null) {
			HttpSession session = ServletContextHolder.getRequest().getSession(true);
			session.setAttribute(Constants.SESSION_NAME, loginInfo);
		} else {
			log.error("ContextHolder获取不到request，请检查ContextHolderFilter的配置！");
		}
	}

	/**
	 * 获取当前应用的上下文(非安全方式，因为默认并没有暴露相关接口，所以采用类反射强行获取)
	 * 
	 * @param sce
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public static String getContextPath(ServletContextEvent sce) {
		String ctxPath = null;
		try {
			// tomcat6和was6.1已测试可行，但nc报错，针对nc采用了另外的获取方式
			Object servletContext = sce.getServletContext();
			Class servletContextClass = servletContext.getClass();
			java.lang.reflect.Field contextField = servletContextClass.getDeclaredField("context");
			contextField.setAccessible(true);
			Object context = contextField.get(servletContext);

			Class contextClass = context.getClass();
			java.lang.reflect.Method getContextPath = contextClass.getMethod("getContextPath");
			Object contextPath = getContextPath.invoke(context);

			ctxPath = contextPath.toString();
			log.info("contextPath=" + contextPath);
		} catch(Exception e) {
			try {
				// NC采用这种方式，上面那种会报错
				Object obj = sce.getServletContext().getRequestDispatcher("/");
				Object contextPath = ReflectionUtils.getPrivatePropertyValue(obj, "requestURI");
				ctxPath = contextPath.toString().substring(0, contextPath.toString().length() - 1);
				log.info("contextPath(Maybe running in NC)=" + ctxPath);
			} catch(Exception e1) {
				e1.printStackTrace();
				throw new RuntimeException("获取上下文失败!");
			}
		}
		return ctxPath;
	}

	/**
	 * Return the real path of the given path within the web application, as
	 * provided by the servlet container.
	 * <p>
	 * Prepends a slash if the path does not already start with a slash, and
	 * throws a FileNotFoundException if the path cannot be resolved to a
	 * resource (in contrast to ServletContext's <code>getRealPath</code>, which
	 * returns null).
	 * 
	 * @param servletContext
	 *            the servlet context of the web application
	 * @param path
	 *            the path within the web application
	 * @return the corresponding real path
	 * @throws FileNotFoundException
	 *             if the path cannot be resolved to a resource
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public static String getRealPath(ServletContext servletContext, String path) throws FileNotFoundException {
		Assert.notNull(servletContext, "ServletContext must not be null");
		// Interpret location as relative to the web application root directory.
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		String realPath = servletContext.getRealPath(path);
		if(realPath == null) {
			throw new FileNotFoundException("ServletContext resource [" + path
					+ "] cannot be resolved to absolute file path - " + "web application archive not expanded?");
		}
		return realPath;
	}

	public static String getInitParameter(String name) {
		return NWClientListener.getInitParameters().get(name);
	}

	public static String getInitParameter(String name, String defaultValue) {
		if(NWClientListener.getInitParameters().containsKey(name)
				&& NWClientListener.getInitParameters().get(name) != null) {
			return getInitParameter(name);
		}
		return defaultValue;
	}

	/**
	 * 获取客户端ip，这里处理了二级代理、反向代理等情况
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if(index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}

	/**
	 * 反序列化对象
	 * 
	 * @param input
	 * @return
	 */
	public static Object deserializObject(InputStream input) throws BusinessException {
		try {
			ObjectInputStream responseReader = new ObjectInputStream(input);
			return responseReader.readObject();
		} catch(Exception e) {
			log.error("反序列化对象时报错" + e.getMessage(), e);
			throw new BusinessException("反序列化对象时报错[?]！",e.getMessage());
		}
	}

	/**
	 * 序列化对象
	 * 
	 * @param data
	 * @return
	 */
	public static ByteArrayOutputStream serializObject(Object data) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutStream;
		try {
			objectOutStream = new ObjectOutputStream(outStream);
			objectOutStream.writeObject(data);
			objectOutStream.close();
		} catch(Exception e) {
			log.error("序列化对象时报错" + e.getMessage());
			return null;
		}
		return outStream;
	}

	/**
	 * 读取资源文件的配置信息
	 * 
	 * @param code
	 * @return
	 */
	public static String getMessage(String code) {
		return getMessage(code, null);
	}

	/**
	 * 读取资源文件的配置信息
	 * 
	 * @param code
	 *            资源文件的key
	 * @param args
	 *            参数值
	 * @return
	 */
	public static String getMessage(String code, Object[] args) {
		return SpringContextHolder.getApplicationContext().getMessage(code, args,
				ServletContextHolder.getRequest().getLocale());
	}

	/**
	 * 客户端配置文件的路径
	 * 
	 * @return
	 */
	public static String getClientConfigPath() {
		return WebUtils.class.getClassLoader().getResource("").getPath()+"clientConfig";
	}

	public static String getClientTempPath() {
		return WebUtils.class.getClassLoader().getResource("").getPath()+"tmp";
	}
}
