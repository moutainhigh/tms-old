/**
 * 
 */
package org.nw.job;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.vo.sys.JobDefVO;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 任务类，创建定时任务时可以创建该实例，扩展一个getId方法，用来唯一标识这个任务，可以通过该id来删除（结束）任务
 * 
 * @author xuqc
 * @Date 2015年5月28日 上午9:43:15
 *
 */
public class NWTimerTask extends TimerTask {

	static Logger logger = Logger.getLogger(NWTimerTask.class);

	public JobDefVO jobDefVO;// 用来创建任务的定义vo
	public String id;// 用来唯一标识这个任务

	public NWTimerTask(JobDefVO defVO) {
		this.jobDefVO = defVO;
		this.id = this.jobDefVO.getPk_job_def();
	}

	public String getId() {
		return id;
	}

	public JobDefVO getJobDefVO() {
		return this.jobDefVO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		String busi_clazz = jobDefVO.getBusi_clazz();
		Class busiClazz = null;
		IJobService service = null;
		try {
			busiClazz = Class.forName(busi_clazz);
			service = (IJobService) busiClazz.newInstance();
		} catch(Exception e) {
			logger.error("任务名称：" + jobDefVO.getJob_name() + "，业务处理类：" + busi_clazz + "无效！");
			e.printStackTrace();
		}
		final IJobService fservice = service;
		final JobDefVO defVO = jobDefVO;
		if(fservice != null) {
			try {
				// 启动事务
				PlatformTransactionManager ptm = new DataSourceTransactionManager(NWDao.getInstance().getDataSource());
				DefaultTransactionDefinition def = new DefaultTransactionDefinition();
				TransactionStatus status = ptm.getTransaction(def);
				try {
					fservice.before(defVO);
					fservice.exec(defVO);
					fservice.after(defVO);
				} catch(Throwable e) {
					ptm.rollback(status);
					throw new RuntimeException(e);
				} finally {
					if(!status.isCompleted()) {
						ptm.commit(status);
					}
				}
			} catch(Exception e) {
				// FIXME 任务执行失败的
				logger.error("任务执行时报错，错误信息：" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
