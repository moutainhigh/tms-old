package org.nw.job;

import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.vo.sys.JobDefVO;
import org.springframework.stereotype.Service;

/**
 * 任务启动器 <BR/>
 * FIXME job的起始日期和结束日期目前的实现方式是错误的，目前只是在系统创建任务时跟当前日期进行比较，
 * 落在当前日期区间内的才创建。实际上应该有另外的县城对现有的任务进行管理，如果已经超过日期了，那么结束这个任务
 * 
 * @author xuqc
 * @date 2015-1-30 下午05:16:02
 */
@Service
public class JobStarterImpl extends AbsJobStarter implements IJobStarter {

	static Logger logger = Logger.getLogger(JobStarterImpl.class);

	public static String serviceID = "jobStarterImpl";

	public String getJobName() {
		return "NW";
	}

	public void createJob() throws Exception {
		String currentDate = DateUtils.getCurrentDate();
		JobDefVO[] jobDefVOs = NWDao .getInstance() .queryForSuperVOArrayByCondition( JobDefVO.class,
						"isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and (begin_date is null or begin_date <=?) and (end_date is null or end_date >=?)",
						currentDate, currentDate);
		if(jobDefVOs != null && jobDefVOs.length > 0) {
			logger.info("总共需要创建" + jobDefVOs.length + "个计划任务！");
			int index = 1;
			for(JobDefVO jobDefVO : jobDefVOs) {
				logger.info("----------------开始创建第" + index + "个计划任务，任务名称：" + jobDefVO.getJob_name()
						+ "--------------------");
				createTask(jobDefVO);
				logger.info("----------------第" + index + "个任务,任务名称：" + jobDefVO.getJob_name()
						+ "创建完成--------------------");
				index++;
			}
		}
	}

}
