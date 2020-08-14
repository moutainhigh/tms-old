package org.nw.job;

import java.util.Map;
import java.util.Timer;

/**
 * job启动器
 * 
 * @author xuqc
 * @date 2015-1-30 下午05:12:45
 */
public interface IJobStarter {
	// 任务延时
	public long delay = 60 * 1000;

	// 任务执行方式
	public int EXEC_INTERVAL = 1;// 间隔时间执行

	public int EXEC_EVERYDAY = 2;// 每天执行

	/**
	 * 定时任务名称
	 * 
	 * @return
	 */
	public String getJobName();

	/**
	 * 创建定时器
	 * 
	 * @return
	 */
	public void createJob() throws Exception;

	/**
	 * 取消定时器
	 */
	public void cancelJob();

	/**
	 * 创建某个任务
	 * 
	 * @param taskId
	 */
	public void createTask(String taskId);

	/**
	 * 取消某个任务
	 * 
	 * @param taskId
	 */
	public void cancelTask(String taskId);

	/**
	 * 重启某个任务
	 * 
	 * @param taskId
	 */
	public void restartTask(String taskId);

	/**
	 * 返回定时任务集合
	 * 
	 * @return
	 */
	public Map<String, NWTimerTask> getTaskMap();

	/**
	 * 返回定时器
	 * 
	 * @return
	 */
	public Timer getTimer();
}
