/**
 * 
 */
package org.nw.job;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.stereotype.Service;

/**
 * 定时任务的辅助器，用来监听定时任务是否已经到期，如果到期则停止该任务,目前只判断到天，这个任务在每天
 * 
 * @author xuqc
 * @Date 2015年5月28日 上午10:01:46
 *
 */
@Service
public class JobAssister extends AbsJobStarter implements IJobStarter {
	static Logger logger = Logger.getLogger(JobAssister.class);
	public static String serviceID = "jobAssister";
	// 辅助任务使用自己的定时器，因为不能清楚辅助任务
	public Timer timer = new Timer();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobStarter#getJobName()
	 */
	public String getJobName() {
		return "NW ASSISTER";
	}

	/*
	 * @see org.nw.job.IJobStarter#createJob()
	 */
	public void createJob() throws Exception {

		// 12个小时执行一次
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				logger.info("...辅助线程检查定时任务是否到期，如果定时任务到期则停止任务...");
				logger.info("--------------开始检查--------------");
				IJobStarter starter = SpringContextHolder.getBean(JobStarterImpl.serviceID);
				Map<String, NWTimerTask> taskMap = starter.getTaskMap();// 当前在运行的所有任务集合，除了辅助任务
				if(taskMap != null) {
					for(String key : taskMap.keySet()) {
						NWTimerTask task = taskMap.get(key);
						JobDefVO defVO = task.getJobDefVO();
						// 检查这个任务是否已经过时了，如果已经过时，则停止这个定时任务，注意如果这个任务还在执行当中，那么先等任务执行完毕再停止
						Date endDate = defVO.getEnd_date().toDate();
						Date curDate = new Date();
						double days = DateUtils.getIntervalDays(curDate, endDate);
						if(days <= 0) {
							logger.info("任务[" + defVO.getJob_name() + "]已到期，将被取消");
							// 结束任务
							boolean bol = task.cancel();
							if(bol) {
								logger.info("任务已取消，任务名称：" + defVO.getJob_name());
								taskMap.remove(key);// 从任务列表中移除
							} else {
								logger.error("取消任务时失败，任务名称：" + defVO.getJob_name());
							}
						}
					}
				}
				logger.info("--------------检查结束--------------");
			}
		}, delay, 24 * 60 * 60 * 1000);

	}

	/*
	 * 辅助任务不需要取消
	 * 
	 * @see org.nw.job.IJobStarter#cancelJob()
	 */
	@Override
	public void cancelJob() {
		logger.info("清除" + getJobName() + "的计划任务...");
		if(timer != null) {
			timer.purge();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobStarter#getTimer()
	 */
	@Override
	public Timer getTimer() {
		return timer;
	}

}
