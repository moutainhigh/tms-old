/**
 * 
 */
package org.nw.job;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.vo.sys.JobDefVO;

/**
 * 定时器启动器
 * 
 * @author xuqc
 * @Date 2015年6月5日 下午9:45:38
 *
 */
public abstract class AbsJobStarter implements IJobStarter {

	static Logger logger = Logger.getLogger(AbsJobStarter.class);
	// 定时器
	public Timer timer = new Timer();

	public Map<String, NWTimerTask> taskMap = new HashMap<String, NWTimerTask>();// 用来存储平台中正在运行的任务

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobStarter#createTask(java.lang.String)
	 */
	public void createTask(String taskId) {
		if(StringUtils.isBlank(taskId)) {
			return;
		}
		JobDefVO jobDefVO = NWDao.getInstance().queryByCondition(JobDefVO.class, "pk_job_def=?", taskId);
		if(jobDefVO == null) {
			throw new BusiException("任务已经被删除，任务ID[?]！",taskId);
		}
		createTask(jobDefVO);
	}

	/**
	 * 创建任务
	 * 
	 * @param jobDefVO
	 */
	protected void createTask(JobDefVO jobDefVO) {
		Integer execType = jobDefVO.getExec_type();
		if(execType == null) {
			logger.error("任务名称：" + jobDefVO.getJob_name() + "，任务执行方式不能为空！");
			return;
		}
		String busi_clazz = jobDefVO.getBusi_clazz();
		if(StringUtils.isBlank(busi_clazz)) {
			logger.error("任务名称：" + jobDefVO.getJob_name() + "，业务处理类不能为空！");
			return;
		}
		NWTimerTask tt = new NWTimerTask(jobDefVO);
		taskMap.put(tt.getId(), tt);

		if(execType == EXEC_INTERVAL) {
			Integer interval = jobDefVO.getJob_interval();// 这里的单位是秒
			if(interval == null) {
				logger.error("任务名称：" + jobDefVO.getJob_name() + "，间隔时间(秒)不能为空！");
				return;
			}
			timer.schedule(tt, delay, interval * 1000);
		} else if(execType == EXEC_EVERYDAY) {
			String exec_time = jobDefVO.getExec_time();// 多个时间使用逗号分隔，如：8:00:00,23:00:00
			if(StringUtils.isBlank(exec_time)) {
				logger.error("任务名称：" + jobDefVO.getJob_name() + "，执行时间不能为空！");
				return;
			}
			String[] arr = exec_time.split(",");
			for(String one : arr) {// 这里one的格式如：08:00:00
				if(StringUtils.isNotBlank(one)) {
					String[] arr1 = one.split(":");
					if(arr1.length != 3) {
						logger.error("任务名称：" + jobDefVO.getJob_name() + "，执行时间格式不正确！");
						continue;
					}
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr1[0]));
					calendar.set(Calendar.MINUTE, Integer.parseInt(arr1[1]));
					calendar.set(Calendar.SECOND, Integer.parseInt(arr1[2]));
					// 如果设定的时间点在当前时间之前，任务会被马上执行，然后开始按照设定的周期定时执行任务。
					timer.schedule(tt, calendar.getTime(), 24 * 60 * 60 * 1000);// 每天执行
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobStarter#cancelTask(java.lang.String)
	 */
	public void cancelTask(String taskId) {
		if(StringUtils.isBlank(taskId)) {
			return;
		}
		NWTimerTask tt = taskMap.get(taskId);
		if(tt != null) {
			tt.cancel();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobStarter#restartTask(java.lang.String)
	 */
	public void restartTask(String taskId) {
		if(StringUtils.isBlank(taskId)) {
			return;
		}
		cancelTask(taskId);
		createTask(taskId);
	}

	/**
	 * 取消定时器，会取消所有任务
	 */
	public void cancelJob() {
		logger.info("清除" + getJobName() + "的计划任务...");
		if(timer != null) {
			timer.purge();
		}
		if(taskMap != null) {
			for(String key : taskMap.keySet()) {
				NWTimerTask tt = taskMap.get(key);
				tt.cancel();
			}
		}
		taskMap.clear();
	}

	public Timer getTimer() {
		return timer;
	}

	public Map<String, NWTimerTask> getTaskMap() {
		return taskMap;
	}

}
