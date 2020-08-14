package com.tms.service.job.portlet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.redis.RedisDao;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.index.PortletConfigVO;


/**
 * @author XIA 2016 5 30
 * 
 * 门户组件定时查询任务
 * 
 */
public class AutoRefreshPortletJobService implements IJobService {

	static Logger logger = Logger.getLogger(AutoRefreshPortletJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行门户组件处理信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有信息需要同步");

		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		Map<String,Integer> userPortletAndTime = RedisDao.getInstance().getUserPortletAndTime();
		if(userPortletAndTime == null || userPortletAndTime.size() == 0){
			logger.info("检测通过，没有数据需要处理。");
			return;
		}
		Map<String,Integer> sqlResult = new HashMap<String, Integer>();
		for(String key : userPortletAndTime.keySet()){
			String pk_portlet = key.split(":")[1];
			String pk_user = key.split(":")[0];
			String userAndPortlet = pk_user + ":" + pk_portlet;
			Integer popupTime = Integer.parseInt(key.split(":")[2]);
			Map<String,Integer> unitUserPortletAndTime = new HashMap<String, Integer>();
			if(popupTime < jobDefVO.getJob_interval() || userPortletAndTime.get(key) * jobDefVO.getJob_interval() > popupTime){
				//提醒时间小于间隔时间，立即执行。
				String countSql = RedisDao.getInstance().getUserPortletAndCountSql(userAndPortlet);
				try {
					Integer count = 0;
					if(sqlResult.get(countSql) != null){
						count = sqlResult.get(countSql);
					}
					count = NWDao.getInstance().queryForObject(countSql, Integer.class);
					PortletConfigVO portletConfigVO = RedisDao.getInstance().getPortlet(pk_portlet);
					if(count != null && count > 0 && portletConfigVO != null){
						//获取到数据了，将数据存入到redis中，方便前台界面读取。
						//webscoket效果更好？
						String portlet_name = portletConfigVO.getPortlet_name();
						portlet_name = portlet_name + "(" + count + ")";
						RedisDao.getInstance().saveUserPortletInfo(pk_user, pk_portlet, portlet_name);
					}
				} catch (Exception e) {
					//查询有误
					continue;
				}
				//执行完毕，执行次数重置
				unitUserPortletAndTime.put(key, 1);
			}else{
				unitUserPortletAndTime.put(key, userPortletAndTime.get(key) + 1);
				
			}
			RedisDao.getInstance().saveUserPortletAndTime(unitUserPortletAndTime);
		}
		
		
		logger.info("-------------------执行门户组件处理信息结束-------------------");
		
		//如果是间隔同步要修改同步时间
		if(jobDefVO.getExec_type() == 1){
			jobDefVO.setDef1((new UFDateTime(new Date())).toString());
		}
		// 获取任务同步次数,更新数据 def2为同步次数
		if (jobDefVO.getDef2() == null) {
			long syncCount = Long.parseLong(jobDefVO.getDef2() == null ? "0" : jobDefVO.getDef2());
			jobDefVO.setDef2(String.valueOf(syncCount++));
		}
		jobDefVO.setStatus(VOStatus.UPDATED);
		// 保存数据
		NWDao.getInstance().saveOrUpdate(jobDefVO);
	}

	public void after(JobDefVO jobDefVO) {

	}

	// 判断是否需要同步信息
	private boolean isNeedSyncData(final JobDefVO jobDefVO) {

		// 设置要返回的值
		boolean bReturn = false;

		// 执行类型，如果 ExecType =1 是间隔，2是定时
		if (jobDefVO.getExec_type() == 1) {
			UFDateTime lastExecTime = jobDefVO.getDef1() == null ? new UFDateTime("2000-01-01 00:00:00") : new UFDateTime(jobDefVO.getDef1());
			UFDateTime now = new UFDateTime(new Date());
			if(UFDateTime.getSecondsBetween(lastExecTime, now) >= jobDefVO.getJob_interval()) {
				bReturn = true;
			}else{
				logger.info("间隔时间:" + UFDateTime.getSecondsBetween(lastExecTime, now) + " 过短，不需要同步数据!");
			}
		}
		// 定时同步
		else if (jobDefVO.getExec_type() == 2) {
			// 获取指定运行时间，并拆分为时分秒
			String strExecTime = jobDefVO.getExec_time();
			int iSecIndex = strExecTime.lastIndexOf(':');
			int iExecSec = Integer.parseInt(strExecTime.substring(iSecIndex + 1, strExecTime.length()));

			strExecTime = strExecTime.substring(0, iSecIndex);
			int iMinIndex = strExecTime.lastIndexOf(':');
			int iExecHour = Integer.parseInt(strExecTime.substring(0, iMinIndex));
			int iExecMin = Integer.parseInt(strExecTime.substring(iMinIndex + 1, strExecTime.length()));

			// 获取当前时间 如果当前时间-最后一次同步时间 大于间隔时间，就进行一次同步
			Date dCurrDate = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strCurrent = fmt.format(dCurrDate);

			int indexHour = strCurrent.indexOf(':');
			int iCurrHour = Integer.parseInt(strCurrent.substring(indexHour - 2, indexHour));
			int iCurrMin = DateUtils.getMinute();
			int iCurrSec = DateUtils.getSecond();

			if (iCurrHour > iExecHour) {
				bReturn = true;
			} else if (iCurrHour == iExecHour && iCurrMin > iExecMin) {
				bReturn = true;
			} else if (iCurrHour == iExecHour && iCurrMin == iExecMin && iCurrSec > iExecSec) {
				bReturn = true;
			}

		}

		return bReturn;
	}
	
	
	
}
