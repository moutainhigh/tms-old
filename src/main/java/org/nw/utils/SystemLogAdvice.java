package org.nw.utils;

import java.util.Date;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.SystemLogVO;
import org.nw.web.utils.WebUtils;

/**
 * 记录操作日志
 * 
 * @author xuqc
 * @date 2012-8-8 下午02:49:27
 * @deprecated 在权限控制中进行记录了，不使用方法拦截器
 */
public class SystemLogAdvice implements MethodInterceptor {
	// 方法名称以这些开头的才记录日志
	private static String[] method_prefix = { "save", "insert", "add", "del", "batchDel", "update", "confirm",
			"unconfirm" };
	private static String[] actionName = { "保存", "插入", "新增", "删除", "批量删除", "更新" };
	private static String[] actionMemo = { "保存一条或多条记录！", "向表中插入一条或多条记录！", "新增一条记录！", "删除一条记录！", "批量删除记录！", "更新一条记录" };

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = invocation.proceed();

		// 只有登录了以后才做记录
		if(WebUtils.getLoginInfo() != null && ParameterHelper.getSystemLog()) {
			String methodName = invocation.getMethod().getName();
			int index = checkMethod(methodName);
			if(index >= 0) {
				// 记录日志
				SystemLogVO logVO = new SystemLogVO();
				logVO.setPk_fun(invocation.getThis().getClass().getName() + "." + methodName);
				logVO.setProcess(actionName[index]);
				logVO.setMemo(actionMemo[index]);
				logVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				logVO.setCreate_time(DateUtils.formatDate(new Date(), DateUtils.DATETIME_FORMAT_HORIZONTAL));
				logVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(logVO);
				NWDao.getInstance().saveOrUpdate(logVO);
			}
		}
		return result;
	}

	/**
	 * 检测当前方法是否包含给定的方法名前缀
	 * 
	 * @param methodName
	 * @return
	 */
	private int checkMethod(String methodName) {
		for(int i = 0; i < method_prefix.length; i++) {
			if(methodName.startsWith(method_prefix[i])) {
				return i;
			}
		}
		return -1;
	}
}
