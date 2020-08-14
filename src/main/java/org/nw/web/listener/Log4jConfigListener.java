package org.nw.web.listener;

import java.io.File;
import java.io.FileNotFoundException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.nw.web.utils.WebUtils;
import org.springframework.util.Log4jConfigurer;
import org.springframework.web.util.Log4jWebConfigurer;
import org.springframework.web.util.WebAppRootListener;

/**
 * Bootstrap listener for custom log4j initialization in a web environment.
 * Delegates to {@link Log4jWebConfigurer} (see its javadoc for configuration
 * details). <b>WARNING: Assumes an expanded WAR file</b>, both for loading the
 * configuration file and for writing the log files. If you want to keep your
 * WAR unexpanded or don't need application-specific log files within the WAR
 * directory, don't use log4j setup within the application (thus, don't use
 * Log4jConfigListener or Log4jConfigServlet). Instead, use a global, VM-wide
 * log4j setup (for example, in JBoss) or JDK 1.4's
 * <code>java.util.logging</code> (which is global too).
 * <p>
 * This listener should be registered before ContextLoaderListener in
 * <code>web.xml</code> when using custom log4j initialization.
 * 
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.web.context.ContextLoaderListener
 * @see WebAppRootListener
 */
public class Log4jConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		String path = WebUtils.getClientConfigPath() + File.separator + "log4j.properties";
		try {
			event.getServletContext().log("Initializing log4j from [" + path + "]");
			Log4jConfigurer.initLogging(path);
		} catch(FileNotFoundException ex) {
			throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + ex.getMessage());
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		Log4jWebConfigurer.shutdownLogging(event.getServletContext());
	}

}
