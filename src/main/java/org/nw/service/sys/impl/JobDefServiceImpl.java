package org.nw.service.sys.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.job.IJobService;
import org.nw.job.IJobStarter;
import org.nw.job.JobStarterImpl;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.JobDefService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.stereotype.Service;

/**
 * 任务配置
 * 
 * @author xuqc
 * @date 2012-8-14 下午01:07:44
 */
@Service
public class JobDefServiceImpl extends AbsToftServiceImpl implements JobDefService {
	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, JobDefVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, JobDefVO.PK_JOB_DEF);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return JobDefVO.JOB_CODE;
	}

	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processAfterSave(billVO, paramVO);

		JobDefVO jobDefVO = (JobDefVO) billVO.getParentVO();
		final String taskId = jobDefVO.getPk_job_def();
		final IJobStarter starter = SpringContextHolder.getBean(JobStarterImpl.serviceID);
		starter.cancelTask(taskId);

		if(jobDefVO.getLocked_flag() == null || !jobDefVO.getLocked_flag().booleanValue()) {
			// 比较日期
			if(jobDefVO.getBegin_date() != null) {
				double days = DateUtils.getIntervalDays(jobDefVO.getBegin_date().toDate(), new Date());
				if(days < 0) {
					return;
				}
			}
			if(jobDefVO.getEnd_date() != null) {
				double days = DateUtils.getIntervalDays(new Date(), jobDefVO.getEnd_date().toDate());
				if(days < 0) {
					return;
				}
			}

			// XXX
			// 重新保存后，删除当前的定时任务，并重新创建,这里要使用一个新的线程去做这个事情,避免因为事务没提交的情况下,马上进行查询导致死锁
			Thread thread = new Thread() {
				public void run() {
					starter.createTask(taskId);
				}
			};
			thread.start();
		}
	}

	@SuppressWarnings("rawtypes")
	public String testTask(String pk_job_def) {
		if(StringUtils.isBlank(pk_job_def)) {
			return null;
		}
		JobDefVO jobDefVO = NWDao.getInstance().queryByCondition(JobDefVO.class, "pk_job_def=?", pk_job_def);
		if(jobDefVO == null) {
			throw new BusiException("任务配置记录已经被删除，pk_job_def[?]！",pk_job_def);
		}
		String busi_clazz = jobDefVO.getBusi_clazz();
		if(StringUtils.isBlank(busi_clazz)) {
			logger.error("任务名称[" + jobDefVO.getJob_name() + "]，业务处理类不能为空！");
			throw new BusiException("任务名称[?]，业务处理类不能为空！",jobDefVO.getJob_name());
		}
		try {
			Class busiClazz = Class.forName(busi_clazz);
			IJobService service = (IJobService) busiClazz.newInstance();
			service.before(jobDefVO);
			service.exec(jobDefVO);
			service.after(jobDefVO);
			//兼容低版本jdk
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			throw new BusiException("任务名称[?]，业务处理类[?]无效！",jobDefVO.getJob_name(),busi_clazz);
		} catch(InstantiationException e) {
			e.printStackTrace();
			throw new BusiException("任务名称[?]，业务处理类[?]无效！",jobDefVO.getJob_name(),busi_clazz);
		} catch( IllegalAccessException e) {
			e.printStackTrace();
			throw new BusiException("任务名称[?]，业务处理类[?]无效！",jobDefVO.getJob_name(),busi_clazz);
		} catch(BusiException e) {
			e.printStackTrace();
			throw new BusiException(e.getMessage());
		}
		return null;
	}
}
