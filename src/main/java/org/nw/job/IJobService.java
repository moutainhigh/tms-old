package org.nw.job;

import org.nw.vo.sys.JobDefVO;

/**
 * 任务接口，exec方法定义要执行的内容
 * 
 * @author xuqc
 * @date 2014-11-15 下午07:53:06
 */
public interface IJobService {

	/**
	 * 执行任务前的准备工作
	 * 
	 * @param jobDefVO
	 */
	public void before(JobDefVO jobDefVO);

	/**
	 * 任务定义vo
	 * 
	 * @param jobDefVO
	 */
	public void exec(JobDefVO jobDefVO);

	/**
	 * 执行任务后的清理工作
	 * 
	 * @param jobDefVO
	 */
	public void after(JobDefVO jobDefVO);
}
