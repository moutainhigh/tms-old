package com.tms.service.job.rt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.HttpUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.JobDefVO;

import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntrustVO;

/**
 * @author XIA 2016 5 30
 * 
 * 荣通委托单签收接口
 * 
 */
public class ExportPODJobService implements IJobService {

	static Logger logger = Logger.getLogger(ExportPODJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出委托单签收-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有委托单签收信息需要同步");

		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		String sql = "SELECT top 20 ts_entrust.vbillno AS ent_vbillno ,ts_entrust.def1 AS ck_vbillno,ts_entrust.act_deli_date AS pod_date,nw_user.user_name AS pod_man, ts_entrust.act_arri_memo AS memo  "
				+ "FROM ts_entrust WITH(NOLOCK) "
				+ "INNER JOIN ts_invoice WITH(NOLOCK) ON ts_entrust.invoice_vbillno=ts_invoice.vbillno AND ts_invoice.pk_arrival=ts_entrust.pk_arrival AND ts_invoice.dr=0 AND ts_invoice.def4='荣盛ERP' "
				+ "LEFT JOIN nw_user WITH(NOLOCK) ON ts_entrust.act_arri_man=nw_user.pk_user "
				+ "WHERE ts_entrust.vbillstatus='23' AND isnull(ts_entrust.def1,'') !=''  AND isnull(ts_entrust.edi_flag,'N')='N' ";
		//一条一条处理

		// 记录成功和失败信息
		List<String> listSucces = new ArrayList<String>();
		List<Map<String, String>> listFailed = new ArrayList<Map<String, String>>();
		// 开始执行时间
		Calendar start = Calendar.getInstance();
		// webservice路径
		String url = jobDefVO.getUrl();
		String userParam = jobDefVO.getUsername_param();
		String user = jobDefVO.getUsername();
		String passwordParam = jobDefVO.getPassword_param();
		String password = jobDefVO.getPassword();
		List<Map<String, Object>> queryResults = NWDao.getInstance().queryForList(sql);
		if (queryResults == null || queryResults.size() == 0) {
			logger.info("同步失败，没有查询到需要同步的数据！");
			return;
		}
	
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (Map<String, Object> result : queryResults){
			Document document = DocumentHelper.createDocument();
			Element hrow = document.addElement("hrow");
			hrow.addElement("ent_vbillno").addText(String.valueOf(result.get("ent_vbillno")));
			hrow.addElement("ck_vbillno").addText(String.valueOf(result.get("ck_vbillno")));
			hrow.addElement("pod_date").addText(String.valueOf(result.get("pod_date")));
			hrow.addElement("pod_man").addText(String.valueOf(result.get("pod_man")));
			hrow.addElement("memo").addText(String.valueOf(result.get("memo")));
			
			String xmlvalue = document.asXML();
			
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("op", "增加");
			paramMap.put("src", "TMS");
			paramMap.put(userParam, user);
			paramMap.put(passwordParam, password);
			paramMap.put("djname", "签收单");
			paramMap.put("djh", "ToERP");
			paramMap.put("xmlvalue", xmlvalue);
			
			String xmlText = "";
			Document doc = null;
			try {
				xmlText = HttpUtils.post(url,paramMap);
				logger.info("解析接口返回数据！");
				if (StringUtils.isBlank(xmlText)) {
					logger.info("接口没有返回数据！");
					continue;
				}
				xmlText = xmlText.replace("&lt;", "<");
				xmlText = xmlText.replace("&gt;", ">");
				String xmlns = xmlText.substring(45, 82);
				xmlText = xmlText.replace(xmlns, "");
				doc = DocumentHelper.parseText(xmlText);
			} catch (Exception e) {
				logger.info("接口返回数据解析失败，格式不正确！");
				continue;
			}
			
			String errorFlag = doc.selectSingleNode("/string/result/success") == null ? "false" : doc.selectSingleNode("/string/result/success").getText();
			String errormsg = doc.selectSingleNode("/string/result/msg") == null ? null : doc.selectSingleNode("/string/result/msg").getText();
			EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "vbillno=?", String.valueOf(result.get("ent_vbillno")));
			if(Boolean.parseBoolean(errorFlag)){
				for(EntrustVO entrustVO : entrustVOs){
					entrustVO.setStatus(VOStatus.UPDATED);
					entrustVO.setEdi_flag(UFBoolean.TRUE);
					entrustVO.setEdi_msg(errormsg);
					toBeUpdate.add(entrustVO);
				}
			}else{
				logger.info("接口同步数据失败！");
				for(EntrustVO entrustVO : entrustVOs){
					entrustVO.setStatus(VOStatus.UPDATED);
					entrustVO.setEdi_flag(UFBoolean.FALSE);
					entrustVO.setEdi_msg(errormsg);
					toBeUpdate.add(entrustVO);
				}
			}
		}
		
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		//如果是间隔同步要修改同步时间
		if(jobDefVO.getExec_type() == 1){
			Date dCurrDate = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strCurrent = fmt.format(dCurrDate);
			jobDefVO.setDef1(strCurrent);
		}
		// 获取任务同步次数,更新数据 def2为同步次数
		if (jobDefVO.getDef2() == null) {
			long syncCount = Long.parseLong(jobDefVO.getDef2() == null ? "0" : jobDefVO.getDef2());
			jobDefVO.setDef2(String.valueOf(syncCount++));
		}

		// 获取总同步数据量，更新数据 def3为同步成功条数
		if (jobDefVO.getDef3() == null) {
			long syncSucceCount = Long.parseLong(jobDefVO.getDef3() == null ? "0" : jobDefVO.getDef3());
			jobDefVO.setDef3(String.valueOf(syncSucceCount++));
		}

		// 获取总同步数据量，更新数据 def4为同步失败条数
		if (jobDefVO.getDef4() == null) {
			long syncFailCount = Long.parseLong(jobDefVO.getDef4() == null ? "0" : jobDefVO.getDef4());
			jobDefVO.setDef4(String.valueOf(syncFailCount++));
		}

		jobDefVO.setStatus(VOStatus.UPDATED);
		// 保存数据
		NWDao.getInstance().saveOrUpdate(jobDefVO);
		
		
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");
	}

	public void after(JobDefVO jobDefVO) {

	}

	// 判断是否需要同步信息
	private boolean isNeedSyncData(final JobDefVO jobDefVO) {

		// 设置要返回的值
		boolean bReturn = false;

		// 执行类型，如果 ExecType =1 是间隔，2是定时
		if (jobDefVO.getExec_type() == 1) {
			Date dateLastSyncDate = DateUtils.parseString(jobDefVO.getDef1());
			Date dataCurrent = new Date();

			long lInterval = dataCurrent.getTime() - dateLastSyncDate.getTime();
			long iIntervalMin = lInterval / (1000);
			// 如果当前时间-最后一次同步时间 大于间隔时间，就进行一次同步
			if (iIntervalMin > jobDefVO.getJob_interval()) {
				bReturn = true;
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
