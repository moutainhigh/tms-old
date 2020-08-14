package org.nw.web.listener;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.nw.job.IJobStarter;
import org.nw.job.JobAssister;
import org.nw.job.JobStarterImpl;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;

import com.tms.service.cm.impl.ContractUtils;

/**
 * 平台的启动监听，用于在系统启动时创建资源，或者执行一些动作
 * 
 * @author xuqc
 * @date 2014-11-15 下午06:52:41
 */
public class NWStartupListener implements ServletContextListener {

	Logger logger = Logger.getLogger(this.getClass().getName());

	public void contextInitialized(ServletContextEvent sce) {
		// 创建定时任务
		IJobStarter starter = SpringContextHolder.getBean(JobStarterImpl.serviceID);
		try {
			starter.createJob();
		} catch(Exception e1) {
			e1.printStackTrace();
		}

		// 创建辅助任务
		IJobStarter assister = SpringContextHolder.getBean(JobAssister.serviceID);
		try {
			assister.createJob();
		} catch(Exception e) {
			e.printStackTrace();
		}
		// 拷贝配置文件
		initConfigurationFiles(sce);
		
		//预加载合同信息
		sce.getServletContext().log("----------------开始预加载合同数据---------------");
		String msg = ContractUtils.preLoad();
		sce.getServletContext().log("----------------预加载合同数据结束，一共加载"+msg+"---------------");
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// 销毁定时任务
		IJobStarter starter = SpringContextHolder.getBean(JobStarterImpl.serviceID);
		starter.cancelJob();

		// 销毁辅助任务
		IJobStarter assister = SpringContextHolder.getBean(JobAssister.serviceID);
		assister.cancelJob();
	}

	/**
	 * copy configuration files without overwrite. & read configuration
	 * 
	 * @param sce
	 */
	private void initConfigurationFiles(ServletContextEvent sce) {
		String srcConfigPath = "/WEB-INF/classes/clientConfig";
		String url = sce.getServletContext().getRealPath(srcConfigPath);
		File src = new File(url);

		String clientConfigPath = WebUtils.getClientConfigPath();
		File dist = new File(clientConfigPath);// 目标文件目录
		logger.info("");
		if(src.listFiles() != null) {
			try {
				copyFile(sce, src, dist);
			} catch(IOException e) {
				if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
					throw new RuntimeException("拷贝配置文件出错，请确保有足够权限！源文件目录=" + srcConfigPath + "，目标文件目录：" + clientConfigPath,
							e);
				}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
					throw new RuntimeException("Error copy configuration file. Please make sure you have sufficient permissions! Source file directory = "+ srcConfigPath +", the target file directory: "+ clientConfigPath,
							e);
				}
				throw new RuntimeException("拷贝配置文件出错，请确保有足够权限！源文件目录=" + srcConfigPath + "，目标文件目录：" + clientConfigPath,
						e);
			}
		} else {
			logger.fine(url + " 目录为空，不需要拷贝！");
		}
	}

	private void copyFile(ServletContextEvent sce, File src, File dist) throws IOException {
		// 先判断一次是否为空，因为在苹果机下有时为null
		if(dist.exists()) {
			for(File file : src.listFiles()) {
				if(file.isFile()) {
					String name = file.getName();
					File temp = new File(dist.getAbsolutePath() + "/" + name);
					if(!temp.exists()) {
						FileUtils.copyFile(file, temp);
						logger.info("配置文件拷贝成功:" + name);
					}
				} else {
					// 目录
					String name = file.getName();
					String distPath = dist.getPath();
					String fiPath = distPath + File.separator + name;
					File dir = new File(fiPath);
					copyFile(sce, file, dir);
				}
			}
		} else {
			dist.mkdirs();
			FileUtils.copyDirectory(src, dist);
		}
	}
}
