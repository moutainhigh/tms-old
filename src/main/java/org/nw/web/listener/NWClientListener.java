package org.nw.web.listener;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.nw.web.utils.WebUtils;

/**
 * 平台启动监听器，用于实例化资源或者启动定时任务 <br/>
 * 这里还没有启动spring，所以不能使用spring的自动注入，参见web.xml中引用的顺序
 */
public class NWClientListener implements ServletContextListener, HttpSessionListener {
	private static final Map<String, String> initParameters = new HashMap<String, String>();

	public static Map<String, String> getInitParameters() {
		return initParameters;
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent sce) {
		// 获取nc配置
		@SuppressWarnings("rawtypes")
		Enumeration e = sce.getServletContext().getInitParameterNames();
		while(e.hasMoreElements()) {
			String key = e.nextElement().toString();
			initParameters.put(key, sce.getServletContext().getInitParameter(key));
		}

		// 获取服务器信息
		WebUtils.setServerInfo(sce.getServletContext().getServerInfo());

		// 获取当前应用的上下文(非安全方式，因为默认并没有暴露相关接口，所以采用类反射强行获取)
		String ctxPath = WebUtils.getContextPath(sce);
		WebUtils.setContextPath(ctxPath);

		// 检查软件的license，查看license是否过期
		// try {
		// InputStream in = this.getClass().getResourceAsStream("tms.lic");
		// BufferedReader br = new BufferedReader(new InputStreamReader(in));
		// String line = null, lic = null;
		// int index = 0;
		// while((line = br.readLine()) != null) {
		// if(index == 0) {
		// lic = line;
		// }
		// index++;
		// }
		// if(lic.equals(NCPasswordUtils.encode("daobanzhewuchi"))) {
		// } else {
		// throw new RuntimeException("您使用的软件版本未注册！");
		// }
		// } catch(Exception ex) {
		// throw new RuntimeException("您使用的软件版本未注册！");
		// }

	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent sce) {
		// 释放动作,如果需要
	}

	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent se) {
	}

	/**
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent se) {
	}

}
