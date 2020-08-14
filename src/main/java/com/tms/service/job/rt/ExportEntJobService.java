package com.tms.service.job.rt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.HttpUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.JobDefVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author XIA 2016 5 30
 * 
 *  荣通委托单接口
 * 
 */
public class ExportEntJobService implements IJobService {

	private static Logger logger = LoggerFactory.getLogger("EDI");

	public void before(JobDefVO jobDefVO) {

	}

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出委托单信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有委托单信息需要同步");

		if (!isNeedSyncData(jobDefVO)) {
			logger.info("没有数据要同步。");
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		String sql = "SELECT TABLE_A.idsort,TABLE_A.lot,TABLE_A.def2,TABLE_A.carno,TABLE_A.driver_name,TABLE_A.driver_phone, "
				+ "TABLE_A.load_weight,TABLE_A.chex,TABLE_A.chechang,TABLE_A.req_deli_date,TABLE_A.create_user, "
				+ "TABLE_A.ent_vbillno,TABLE_A.erpdjlsh,TABLE_A.cust_code, isnull(ts_ent_pack_b.num,0) as num, "
				+ "CAST(isnull(ts_ent_pack_b.weight,0) AS VARCHAR) as sl,isnull(ts_ent_pack_b.pk_ent_pack_b,'') AS ent_pack_b, "
				+ "ts_inv_pack_b.def1 AS erpdjbth "
				+ "FROM ( "
				+ "SELECT ROW_NUMBER() OVER (ORDER BY ts_entrust.vbillno) AS idsort,ts_entrust.pk_entrust,ts_entrust_lot.lot, "
				+ "isnull(ts_entrust_lot.def2,'N') as def2,isnull(ts_ent_transbility_b.carno,'') as carno,isnull(ts_ent_transbility_b.driver_name,'') as driver_name, "
				+ "ts_ent_transbility_b.driver_mobile AS driver_phone,  CAST(isnull(ts_car.def3,'1') AS VARCHAR) AS load_weight, "
				+ "isnull (ts_car.def1,'1T') as chex ,isnull(ts_car.def2,'1') as chechang ,LEFT(ts_entrust.req_deli_date ,10) AS req_deli_date ,nw_user.user_name as create_user,  "
				+ " ts_entrust.vbillno as ent_vbillno,ts_invoice.def1 AS erpdjlsh,isnull(ts_customer.cust_code,'') as cust_code  "
				+ "FROM ts_entrust WITH(NOLOCK) "
				+ "LEFT JOIN ts_entrust_lot WITH(NOLOCK)  ON ts_entrust_lot.lot=ts_entrust.lot AND ts_entrust.dr=0 "
				+ "LEFT JOIN ts_ent_transbility_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_transbility_b.pk_entrust AND ts_ent_transbility_b.dr=0 "
				+ "INNER JOIN ts_invoice WITH(NOLOCK) ON ts_entrust.invoice_vbillno=ts_invoice.vbillno AND ts_invoice.pk_delivery=ts_entrust.pk_delivery AND ts_invoice.dr=0 AND ts_invoice.def4='荣盛ERP'   "
				+ "LEFT JOIN ts_customer WITH(NOLOCK) ON ts_invoice.pk_customer=ts_customer.pk_customer AND ts_customer.dr=0 "
				+ "LEFT JOIN ts_car  WITH(NOLOCK) ON ts_ent_transbility_b.carno=ts_car.carno AND ts_car.dr=0 "
				+ "LEFT JOIN nw_user WITH(NOLOCK) ON ts_entrust.create_user=nw_user.pk_user AND nw_user.dr=0 "
				+ "WHERE ts_entrust.dr=0   AND ts_entrust.vbillstatus =21 AND ts_entrust.lot IN ("
				+ "SELECT ts_entrust.lot FROM ts_entrust WITH(NOLOCK) "
				+ "INNER JOIN ts_invoice WITH(NOLOCK) ON ts_entrust.invoice_vbillno=ts_invoice.vbillno AND ts_invoice.pk_delivery=ts_entrust.pk_delivery AND ts_invoice.dr=0 AND ts_invoice.def4='荣盛ERP'  "
				+ "LEFT JOIN ts_entrust_lot  ON ts_entrust.lot=ts_entrust_lot.lot AND ts_entrust_lot.dr=0  "
				+ "WHERE  isnull(ts_entrust_lot.edi_flag,'N')='N' AND ts_entrust.dr=0  "
				+ "GROUP BY ts_entrust.lot 	HAVING	count(DISTINCT ts_entrust.vbillstatus)=1) "
				+ ") TABLE_A "
				+ "LEFT JOIN ts_ent_pack_b WITH(NOLOCK) ON TABLE_A.pk_entrust=ts_ent_pack_b.pk_entrust AND ts_ent_pack_b.dr=0  "
				+ "LEFT JOIN ts_seg_pack_b WITH(NOLOCK) ON ts_ent_pack_b.pk_seg_pack_b=ts_seg_pack_b.pk_seg_pack_b AND ts_seg_pack_b.dr=0 "
				+ "LEFT JOIN ts_inv_pack_b WITH(NOLOCK) ON ts_inv_pack_b.pk_inv_pack_b=ts_seg_pack_b.pk_inv_pack_b AND ts_inv_pack_b.dr=0  ";
		// 记录成功和失败信息

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
		//按照批次分组
		Map<String, List<Map<String, Object>>> groupMap = new HashMap<String, List<Map<String, Object>>>();
		for (Map<String, Object> map : queryResults) {
			String key = String.valueOf(map.get("lot"));
			List<Map<String, Object>> mapList = groupMap.get(key);
			if (mapList == null) {
				mapList = new ArrayList<Map<String, Object>>();
				groupMap.put(key, mapList);
			}
			mapList.add(map);
		}
		logger.info("开始同步数据，一共【"+groupMap.size()+"】条!");
		for(String key : groupMap.keySet()){
			List<Map<String, Object>> mapList = groupMap.get(key);
			if(mapList == null || mapList.size() == 0){
				continue;
			}
			Document document = DocumentHelper.createDocument();
			Element hrow = document.addElement("hrow");
			hrow.addElement("lot").addText(String.valueOf(mapList.get(0).get("lot")));
			hrow.addElement("carno").addText(String.valueOf(mapList.get(0).get("carno")));
			hrow.addElement("driver_name").addText(String.valueOf(mapList.get(0).get("driver_name")));
			hrow.addElement("driver_phone").addText(String.valueOf(mapList.get(0).get("driver_phone")));
			hrow.addElement("load_weight").addText(String.valueOf(mapList.get(0).get("load_weight")));
			hrow.addElement("chex").addText(String.valueOf(mapList.get(0).get("chex")));
			hrow.addElement("chechang").addText(String.valueOf(mapList.get(0).get("chechang")));
			hrow.addElement("req_deli_date").addText(String.valueOf(mapList.get(0).get("req_deli_date")));
			hrow.addElement("create_user").addText(String.valueOf(mapList.get(0).get("create_user")));
			
			for (Map<String, Object> map : mapList){
				Element brow = hrow.addElement("brow");
				brow.addElement("idsort").addText(String.valueOf(map.get("idsort")));
				brow.addElement("ent_vbillno").addText(String.valueOf(map.get("ent_vbillno")));
				brow.addElement("erpdjlsh").addText(String.valueOf(map.get("erpdjlsh")));
				brow.addElement("cust_code").addText(String.valueOf(map.get("cust_code")));
				brow.addElement("num").addText(String.valueOf(map.get("num")));
				brow.addElement("sl").addText(String.valueOf(map.get("sl")));
				brow.addElement("ent_pack_b").addText(String.valueOf(map.get("ent_pack_b")));
				brow.addElement("erpdjbth").addText(String.valueOf(map.get("erpdjbth")));
			}
			
			String xmlValue = document.asXML();
			//ts_entrust_lot.def2判断是否有给ERP同步过,N没有，Y有
			Map<String,Object> paramMap = new HashMap<String,Object>();
			if(queryResults.get(0).get("def2").equals("N")){
				paramMap.put("op", "增加");
			}else if(queryResults.get(0).get("def2").equals("Y")){
				paramMap.put("op", "修改");
			}
			paramMap.put("src", "TMS");
			paramMap.put(userParam, user);
			paramMap.put(passwordParam, password);
			paramMap.put("djname", "调度单");
			paramMap.put("djh", "ToERP");
			paramMap.put("xmlvalue", xmlValue);
			
			String xmlText = "";
			Document doc = null;
			
			try {
				xmlText = HttpUtils.post(url,paramMap);
				logger.info("解析接口返回数据！");
				if (StringUtils.isBlank(xmlText)) {
					logger.info("接口没有返回数据,此条跳过:" + mapList.get(0).get("lot"));
					continue;
				}
				xmlText = xmlText.replace("&lt;", "<");
				xmlText = xmlText.replace("&gt;", ">");
				String xmlns = xmlText.substring(45, 82);
				xmlText = xmlText.replace(xmlns, "");
				doc = DocumentHelper.parseText(xmlText);
			} catch (Exception e) {
				logger.info("接口返回数据解析失败，格式不正确,此条跳过:"+ mapList.get(0).get("lot"));
				continue;
			}
			String errorFlag = doc.selectSingleNode("/string/result/success") == null ? "false" : doc.selectSingleNode("/string/result/success").getText();
			String errormsg = doc.selectSingleNode("/string/result/msg") == null ? "" : doc.selectSingleNode("/string/result/msg").getText();
			String commitSql = "";
			if(Boolean.parseBoolean(errorFlag)){
				//同步成功更新Edi_flag=Y 标准的edi_flg
				commitSql = "UPDATE ts_entrust_lot SET edi_flag = 'Y',edi_msg='"+ errormsg.replaceAll("'", "\"") +"' WHERE lot = '"+String.valueOf(mapList.get(0).get("lot"))+"'";
			}else{
				logger.info("接口同步数据失败！:"+ mapList.get(0).get("lot") + ":" + errormsg);
				commitSql = "UPDATE ts_entrust_lot SET edi_flag = 'N',edi_msg='"+ errormsg.replaceAll("'", "\"") +"' WHERE lot = '"+String.valueOf(mapList.get(0).get("lot"))+"'";
			}
			//强制数据提交
			try {
				Connection connection = NWDao.getInstance().getDataSource().getConnection();
				PreparedStatement ps = connection.prepareStatement(commitSql);
				ps.execute();
				connection.commit();
				connection.close();
				logger.info("同步调度单结束,lot:" +mapList.get(0).get("lot")+ ",edi_msg:" + errormsg.replaceAll("'", "\""));
				logger.info("                                                                                       ");
			} catch (SQLException e) {
				e.printStackTrace();
				logger.info("数据库提交出错：" + e.getMessage());
			}
		}
		
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
		logger.info("共执行：" + interval / 1000 + "秒");
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
