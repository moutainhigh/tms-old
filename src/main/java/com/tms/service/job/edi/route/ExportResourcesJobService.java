package com.tms.service.job.edi.route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.JobDefVO;
import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.base.CarCapabilityVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.DriverVO;
import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import com.tms.service.job.edi.route.client.RouteClient;
import com.tms.service.job.edi.route.client.RouteVO;

import org.apache.commons.lang.StringEscapeUtils;
/**
 * 导出资料信息，调用第三方提供的web service，将数据导出给第三方系统
 * 
 * @author songf
 * @Date 2015年11月23日 下午11:01:28
 *
 */
public class ExportResourcesJobService implements IJobService {
	static Logger logger = Logger.getLogger(ExportResourcesJobService.class);

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出资料信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有资料信息需要同步");

		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");

		
		try {

			// 记录成功的资料及能力信息和失败的资料及能力信息的明细主键值

			List<String> listSucces = new ArrayList<String>();
			List<String> listFailed = new ArrayList<String>();
			List<String> listcapSucces = new ArrayList<String>();
			List<String> listcapFailed = new ArrayList<String>();
			logger.info("开始同步车辆及能力信息。");
			syncCarData(jobDefVO, listSucces, listFailed,listcapSucces,listcapFailed);
			logger.info("车辆及能力信息同步结束。");
			
			//更新统计信息
			afterSyncCarData(jobDefVO, listSucces, listFailed,listcapSucces,listcapFailed);
			
			logger.info("开始同步司机信息。");
			List<String> listDriverSucces = new ArrayList<String>();
			List<String> listDriverFailed = new ArrayList<String>();
			syncDriverData(jobDefVO,listDriverSucces,listDriverFailed);
			logger.info("司机信息同步结束。");
			
			//更新统计信息
			afterSyncDriverData(jobDefVO,listDriverSucces,listDriverFailed);
			
		} catch (Exception e) {
			// 记录失败的ID
			e.printStackTrace();
		}

		logger.info("-------------------导出资料信息任务执行完毕-------------------");
	}
	/**
	 * 同步车辆信息 songf 2015-12-12
	 * 
	 * @param jobDefVO 执行任务
	 * @param listSucces 同步成功的主键列表
	 * @param listFialed 同步失败的主键列表
	 */
	private void syncCarData(JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed, List<String> listCapSucces, List<String> listCapFailed) {
		// soap头
		String strHeader = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:cais=\"http://www.ortec.com/CAIS\"><soap:Header/>"
				+ "<soap:Body><cais:SendMessage xmlns=\"http://www.ortec.com/CAIS\">";
		// soap尾
		String strTail = "<cais:commandName>ImportResource</cais:commandName></cais:SendMessage></soap:Body></soap:Envelope>";

		// 查询车辆信息
		String sqlCar = "SELECT carno AS id,carno AS code,carno AS name,def3 as resoure_tag,(CASE	WHEN ts_car.locked_flag IS NULL  THEN 1 "
				+ " WHEN ts_car.locked_flag  = 'N' THEN 1 WHEN ts_car.locked_flag  = 'Y' THEN 0 END) AS active,"
				+ " def2 AS Home_Base,load_weight AS kg,volume AS m3,def11 AS plts"
				+ " FROM ts_car WITH(NOLOCK) where ts_car.dr = 0 and isnull(ts_car.def1,'N') <>'Y' and ts_car.pk_car='3a3bd7ed4bb34fcb946ee8ae6c57321b' ";

		// 查询车辆能力信息
		String sqlCarCapability = " SELECT pk_car_capability,nw_data_dict_b.display_name ,(CASE	WHEN ts_car_capability.value =0 THEN 'required'  WHEN ts_car_capability.value =1 THEN 'available' WHEN ts_car_capability.value =2 THEN 'forbidden' END) AS value ,"
				+ " ts_car_capability.pk_car "
				+ " FROM   ts_car_capability WITH(NOLOCK) "
				+ " LEFT JOIN nw_data_dict_b WITH(NOLOCK) ON ts_car_capability.capability_type=nw_data_dict_b.value AND nw_data_dict_b.pk_data_dict='3b8b204410134be4a1b1cd91b7c1c08d'"
				+ " WHERE ts_car_capability.dr=0 and ts_car_capability.pk_car  in ";
		// 开始执行时间
		Calendar start = Calendar.getInstance();
		
		// 记录数据主键值
		String strPkCar = "";
		try {
	
		

			// 获取车辆及车辆能力信息
			List<HashMap> mapCarList = NWDao.getInstance().queryForList(sqlCar, HashMap.class);
			logger.info("共查询到" + mapCarList.size() + "条资料信息需要进行同步操作。");
			if (mapCarList != null && mapCarList.size() > 0) {
				for (int i = 0; i < mapCarList.size(); i++) {
					HashMap addressRowMap = mapCarList.get(i);
					String strid = String.valueOf(addressRowMap.get("id"));
					strPkCar = String.valueOf(addressRowMap.get("id"));
					String strCode = String.valueOf(addressRowMap.get("code"));
					String strName = String.valueOf(addressRowMap.get("name"));
					String strResourceTag = String.valueOf(addressRowMap.get("resoure_tag"));
					String strActive = String.valueOf(addressRowMap.get("active"));
					String strHomeBase = String.valueOf(addressRowMap.get("home_base"));
					String strkg = String.valueOf(addressRowMap.get("kg"));
					String strM3 = String.valueOf(addressRowMap.get("m3"));
					String strPlts = String.valueOf(addressRowMap.get("plts"));
					
					// 创建xml格式数据--车辆信息
					Document document = DocumentHelper.createDocument();
					//Element root = document.addElement("message");
					//Element nodeComtec = root.addElement("comtec");

					Element nodeResource = document.addElement("resource");
					nodeResource.addElement("id").addText(strid);
					nodeResource.addElement("code").addText(strCode);
					nodeResource.addElement("name").addText(strName);

					// def3 = 3是Trailer
					if (strResourceTag.equals("3")) {
						nodeResource.addElement("resource_kind_code").addText("Trailer");
						nodeResource.addElement("resource_tag").addText(strResourceTag);
					}
					// def3 = 4是 Vehicle
					else if (strResourceTag.equals("4")) {
						nodeResource.addElement("resource_kind_code").addText("Vehicle");
						nodeResource.addElement("resource_tag").addText(strResourceTag);
					}
					
					nodeResource.addElement("active").addText(strActive);
			
					nodeResource.addElement("Home_Base").addText(strHomeBase);

					Element nodeCapacities = nodeResource.addElement("capacities");
					Element nodeCapacityKg = nodeCapacities.addElement("capacity");
  					nodeCapacityKg.addElement("unit_code").addText("kg");
					nodeCapacityKg.addElement("value").addText(strkg);
					Element nodeCapacityM3 = nodeCapacities.addElement("capacity");
					nodeCapacityM3.addElement("unit_code").addText("m3");
					nodeCapacityM3.addElement("value").addText(strM3);
					Element nodeCapacityPlts = nodeCapacities.addElement("capacity");
					nodeCapacityPlts.addElement("unit_code").addText("plts");
					nodeCapacityPlts.addElement("value").addText(strPlts);

					Element nodeCapabilities = nodeResource.addElement("capabilities");

					// 生成SQL
					sqlCarCapability = sqlCarCapability + "('"+ strid +"')";
					String pk_Car_Capability = "";
					List<HashMap> mapCarCapabilityList = NWDao.getInstance().queryForList(sqlCarCapability,HashMap.class);
					// 获取车辆能力信息
					if (mapCarCapabilityList != null && mapCarCapabilityList.size() > 0) {
						for (int j = 0; j < mapCarCapabilityList.size(); j++) {
							HashMap carCapaRowMap = mapCarCapabilityList.get(j);
							pk_Car_Capability = pk_Car_Capability +","+String.valueOf(carCapaRowMap.get("pk_car_capability"));
							String strTmpPKAddress = String.valueOf(carCapaRowMap.get("pk_car"));
							String strType = String.valueOf(carCapaRowMap.get("display_name"));
							String strValue = String.valueOf(carCapaRowMap.get("value"));

							Element nodeCapability = nodeCapabilities.addElement("capability");
							nodeCapability.addElement("code").addText(strType);
							if (strValue.equals("required")) {
								nodeCapability.addElement("required").addText("true");
							} else if (strValue.equals("forbidden")) {
								nodeCapability.addElement("forbidden").addText("true");
							} else if (strValue.equals("available")) {
								nodeCapability.addElement("available").addText("true");
							}
						}
					}
					
					//去掉第一个字符
					if(!pk_Car_Capability.isEmpty()){
						pk_Car_Capability = pk_Car_Capability.substring(1, pk_Car_Capability.length());
					}
					
					nodeResource.addElement("subcontractor_id").addText("");
					nodeResource.addElement("comment").addText("");

					// 调用WS服务
					Element rootMessage = document.getRootElement();
					String strMessage = rootMessage.asXML();

					//调用JNI接口
					RouteClient client = new RouteClient();
				    RouteVO routeVO = new RouteVO();
					routeVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(routeVO);
					routeVO.setCommandname("ImportResource");
					routeVO.setMessage(strMessage);
					NWDao.getInstance().saveOrUpdate(routeVO);
					String returnMsg =client.SendMessage(routeVO.getPrimaryKey(), "yuliuziduan");
					
					if ("TRUE".equalsIgnoreCase(returnMsg)){
						String instersql="INSERT INTO edi_his_route SELECT * FROM edi_route WITH(NOLOCK) WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						String delsql="DELETE edi_route WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						String updatesql1="UPDATE ts_car SET def1='Y'  WHERE carno='"+strCode+"'";
						NWDao.getInstance().update(instersql);
						NWDao.getInstance().update(delsql);
						NWDao.getInstance().update(updatesql1);
					}else{
						String updatesql="UPDATE edi_route SET syncexp_flag='Y' , syncexp_memo='"+returnMsg+ "' WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						NWDao.getInstance().update(updatesql);
					}
				}
			}
		} catch (Exception e) {
			// 记录失败的ID
			if (!listFailed.contains(strPkCar)) {
				listFailed.add(strPkCar);
			}
			e.printStackTrace();
		}
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");

	}

	/**
	 * 同步司机信息 songf 2015-12-12
	 * 
	 * @param jobDefVO 执行任务
	 * @param listSucces 同步成功的主键列表
	 * @param listFialed 同步失败的主键列表
	 */
	private void syncDriverData(JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed) {
		// soap头
		String strHeader = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:cais=\"http://www.ortec.com/CAIS\"><soap:Header/>"
				+ "<soap:Body><cais:SendMessage xmlns=\"http://www.ortec.com/CAIS\">";
		// soap尾
		String strTail = "<cais:commandName>ImportResource</cais:commandName></cais:SendMessage></soap:Body></soap:Envelope>";

		// 查询司机信息
		String sqlDriver = "SELECT pk_driver AS id,  driver_code AS code, driver_name AS name, "
				+ " locked_flag AS active, def2 AS Home_Base,pk_carrier AS subcontractor_id"
				+ " FROM ts_driver  where ts_driver.dr = 0 and isnull(ts_driver.def1,'N') <>'Y' and ts_driver.pk_driver='73b41019f6114c8fb39e6c33725dd581'";
		
		// 开始执行时间
		Calendar start = Calendar.getInstance();

		// 记录数据主键值
		String strPkDriver = "";
		try {
			// 创建xml格式数据--司机信息
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("message");
			Element nodeComtec = root.addElement("comtec");

			// 获取车辆及车辆能力信息
			List<HashMap> mapDriverList = NWDao.getInstance().queryForList(sqlDriver, HashMap.class);
			logger.info("共查询到" + mapDriverList.size() + "条司机信息需要进行同步操作。");
			if (mapDriverList != null && mapDriverList.size() > 0) {
				for (int i = 0; i < mapDriverList.size(); i++) {
					HashMap driverRowMap = mapDriverList.get(i);
					String strid = String.valueOf(driverRowMap.get("id"));
					strPkDriver = String.valueOf(driverRowMap.get("id"));
					String strCode = String.valueOf(driverRowMap.get("code"));
					String strName = String.valueOf(driverRowMap.get("name"));
					String strDef1 = String.valueOf(driverRowMap.get("resoure_tag"));
					String strSubcontractorID = String.valueOf(driverRowMap.get("subcontractor_id"));
					String strActive = String.valueOf(driverRowMap.get("active"));
					String strHomeBase = String.valueOf(driverRowMap.get("home_base"));
					
					Element nodeResource = nodeComtec.addElement("resource");
					nodeResource.addElement("id").addText(strid);
					nodeResource.addElement("code").addText(strCode);
					nodeResource.addElement("name").addText(strName);
					nodeResource.addElement("resource_kind_code").addText("Driver");
					nodeResource.addElement("resource_tag").addText("1");
					if(strActive.equals("N")){
						nodeResource.addElement("active").addText("0");
					}
					else if(strActive.equals("Y")){
						nodeResource.addElement("active").addText("1");
					}
					nodeResource.addElement("Home_Base").addText(strHomeBase);
					nodeResource.addElement("subcontractor_id").addText(strSubcontractorID);	

					// 调用WS服务
					Element rootMessage = document.getRootElement();
					String strMessage = rootMessage.asXML();

					// 生成message
					String strEnvelope = strHeader + strMessage + strTail;

					RouteClient client = new RouteClient();
					String returnMsg = client.SendMessage(strEnvelope, "ImportResource");
					returnMsg = StringEscapeUtils.unescapeHtml(returnMsg);
					// 截取返回值的信息，判断是否成功
					int iStart = returnMsg.indexOf("success");
			
					// 返回成功加入成功列表，否则返回失败列表
					if(iStart != -1){
						int iStop = returnMsg.indexOf("entity");
						String strReturn = returnMsg.substring(iStart + 9, iStop - 3);
						boolean isSuccess = Boolean.parseBoolean(strReturn);

						// 返回成功加入成功列表，否则返回失败列表
						if (isSuccess) {
							if (!listSucces.contains(strPkDriver)) {
								listSucces.add(strPkDriver);
							}
						} else {
							// 记录错误的ID值
							if (!listFailed.contains(strPkDriver)) {
								listFailed.add(strPkDriver);
							}
						}
					}
					//此时返回的是异常信息
					else
					{
						// 记录错误的ID值
						if (!listFailed.contains(strPkDriver)) {
							listFailed.add(strPkDriver);
						}
					}
				}
			}
		} catch (Exception e) {
			// 记录失败的ID
			if (!listFailed.contains(strPkDriver)) {
				listFailed.add(strPkDriver);
			}
			e.printStackTrace();
		}
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");

	}

	/**
	 * 判断任务是否同步数据 songf 2015-11-14
	 * 
	 * @param jobDefVO 同步任务信息
	 */
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

	/**
	 * 数据同步后处理，更新 ts_car,ts_car_capability的状态 ，更新 任务的统计信息 songf 2015-12-12
	 * 
	 * @param jobDefVO 同步任务信息
	 * @param listSucces ts_car同步成功明细主键列表
	 * @param listFailed ts_car同步失败明细主键列表
	 * @param listcapSucces ts_car_capability同步成功明细主键列表
	 * @param listcapFailed ts_car_capability同步失败明细主键列表
	 */
	private void afterSyncCarData(final JobDefVO jobDefVO, List<String> listSucces,List<String> listFailed, List<String> listcapSucces,List<String> listcapFailed) {
		try {
			if(listSucces.size() == 0 && listFailed.size() == 0){
				return;
			}
			List<SuperVO> carUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_car 将同步成功的明细def1 改为Y
			if (listSucces.size() > 0) {
				CarVO[] carVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarVO.class,
						"pk_car in "
								+ NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (carVOSuccs != null && carVOSuccs.length > 0) {
					for (CarVO carVO : carVOSuccs) {

						// 更新状态为已同步
						carVO.setDef1("Y");
						carVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						carUpdateSuccessList.add(carVO);
					}
				}
			}
			// 保存数据
			if (carUpdateSuccessList != null && carUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(carUpdateSuccessList);
			}
			
			List<SuperVO> carUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_car 将同步失败的明细def1 改为N
			if(listFailed.size() > 0){
				CarVO[] carVOFaileds = NWDao.getInstance()
						.queryForSuperVOArrayByCondition(CarVO.class, "pk_car in "
								+ NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (carVOFaileds != null && carVOFaileds.length != 0) {
					for (CarVO carVO : carVOFaileds) {
						
						// 更新状态为同步失败
						carVO.setDef1("N");
						carVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						carUpdateFailedList.add(carVO);
					}
				}
			}			
			// 保存数据
			if (carUpdateFailedList != null && carUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(carUpdateFailedList);
			}
			
			List<SuperVO> carCapUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_car_capability 将同步成功的明细def1 改为Y
			if (listcapSucces.size() > 0) {
				int index = 0;
				for (; index < listcapSucces.size(); index++) {
					String strFailed = listcapSucces.get(index);
					String[] strSuccessArray = strFailed.split(",");
				CarCapabilityVO[] carcapVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarCapabilityVO.class,
						"pk_car_capability in "
								+ NWUtils.buildConditionString(strSuccessArray));
				if (carcapVOSuccs != null && carcapVOSuccs.length > 0) {
					for (CarCapabilityVO carcapVO : carcapVOSuccs) {

						// 更新状态为已同步
						carcapVO.setDef1("Y");
						carcapVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						carCapUpdateSuccessList.add(carcapVO);
					}
				  }
				}
			}
			// 保存数据
			if (carCapUpdateSuccessList != null && carCapUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(carCapUpdateSuccessList);
			}
			
			List<SuperVO> carCapUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_car 将同步失败的明细def1 改为N
			if(listcapFailed.size() > 0){
				int index = 0;
				for (; index < listcapFailed.size(); index++) {
					String strFailed = listcapFailed.get(index);
					String[] strFailedArray = strFailed.split(",");
				CarCapabilityVO[] carcapVOFaileds = NWDao.getInstance().queryForSuperVOArrayByCondition(CarCapabilityVO.class,
						"pk_car_capability in "
								+ NWUtils.buildConditionString(strFailedArray));
				if (carcapVOFaileds != null && carcapVOFaileds.length > 0) {
					for (CarCapabilityVO carcapVO : carcapVOFaileds) {

						// 更新状态为同步失败
						carcapVO.setDef1("N");
						carcapVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						carCapUpdateFailedList.add(carcapVO);
					}
				  }
				}
			}			
			// 保存数据
			if (carCapUpdateFailedList != null && carCapUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(carCapUpdateFailedList);
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
				jobDefVO.setDef2("0");
			}
			int iSyncCount = Integer.parseInt(jobDefVO.getDef2());
			iSyncCount++;
			jobDefVO.setDef2(String.valueOf(iSyncCount));

			// 获取总同步数据量，更新数据 def3为同步成功条数
			if (jobDefVO.getDef3() == null) {
				jobDefVO.setDef3("0");
			}
			long lSyncSuccessRecord = Long.parseLong(jobDefVO.getDef3());
			lSyncSuccessRecord += (long)listSucces.size();
			jobDefVO.setDef3(String.valueOf(lSyncSuccessRecord));

			// 获取总同步数据量，更新数据 def4为同步失败条数
			if (jobDefVO.getDef4()== null) {
				jobDefVO.setDef4("0");
			}
			long lSyncFailedRecord = Long.parseLong(jobDefVO.getDef4());
			lSyncFailedRecord += (long)listFailed.size();
			jobDefVO.setDef4(String.valueOf(lSyncFailedRecord));

			jobDefVO.setStatus(VOStatus.UPDATED);
			
			// 保存数据
			NWDao.getInstance().saveOrUpdate(jobDefVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数据同步后处理，更新 ts_driver的状态 ，更新 任务的统计信息 songf 2015-12-12
	 * 
	 * @param jobDefVO
	 *            同步任务信息
	 * @param listSucces
	 *            同步成功明细主键列表
	 * @param listFailed
	 *            同步失败明细主键列表
	 */
	private void afterSyncDriverData(final JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed) {
		try {
			if(listSucces.size() == 0 && listFailed.size() == 0){
				return;
			}
			List<SuperVO> driverUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_driver 将同步成功的明细def1 改为Y
			if (listSucces.size() > 0) {
				DriverVO[] driverVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(DriverVO.class,
						"pk_driver in "
								+ NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (driverVOSuccs != null && driverVOSuccs.length > 0) {
					for (DriverVO driverVO : driverVOSuccs) {

						// 更新状态为已同步
						driverVO.setDef1("Y");
						driverVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						driverUpdateSuccessList.add(driverVO);
					}
				}
			}
			// 保存数据
			if (driverUpdateSuccessList != null && driverUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(driverUpdateSuccessList);
			}
			
			List<SuperVO> driverUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_supplier 将同步失败的明细def1 改为N
			if(listFailed.size() > 0){
				DriverVO[] driverVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(DriverVO.class,
						"pk_driver in "
								+ NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (driverVOSuccs != null && driverVOSuccs.length > 0) {
					for (DriverVO driverVO : driverVOSuccs) {

						// 更新状态为已同步
						driverVO.setDef1("Y");
						driverVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						driverUpdateFailedList.add(driverVO);
					}
				}
			}			
			// 保存数据
			if (driverUpdateFailedList != null && driverUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(driverUpdateFailedList);
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
				jobDefVO.setDef2("0");
			}
			int iSyncCount = Integer.parseInt(jobDefVO.getDef2());
			iSyncCount++;
			jobDefVO.setDef2(String.valueOf(iSyncCount));

			// 获取总同步数据量，更新数据 def3为同步成功条数
			if (jobDefVO.getDef3() == null) {
				jobDefVO.setDef3("0");
			}
			long lSyncSuccessRecord = Long.parseLong(jobDefVO.getDef3());
			lSyncSuccessRecord += (long)listSucces.size();
			jobDefVO.setDef3(String.valueOf(lSyncSuccessRecord));

			// 获取总同步数据量，更新数据 def4为同步失败条数
			if (jobDefVO.getDef4()== null) {
				jobDefVO.setDef4("0");
			}
			long lSyncFailedRecord = Long.parseLong(jobDefVO.getDef4());
			lSyncFailedRecord += (long)listFailed.size();
			jobDefVO.setDef4(String.valueOf(lSyncFailedRecord));

			jobDefVO.setStatus(VOStatus.UPDATED);
			
			// 保存数据
			NWDao.getInstance().saveOrUpdate(jobDefVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 创建http请求的定时任务
	 * 
	 * @param otVO
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<TrackVO> createHttpTask(final JobDefVO jobDefVO, Map<String, Object> paramMap) {

		return null;
	}

	/**
	 * 创建webservice请求的任务
	 * 
	 * @param otVO
	 * @return
	 */
	private List<TrackVO> createWebserviceTask(final JobDefVO otVO, Map<String, Object> paramMap) {
		return null;
	}

	public void before(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}

	public void after(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}
}
