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

import com.tms.service.job.edi.route.client.RouteClient;
import com.tms.service.job.edi.route.client.RouteVO;
import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.base.AddressCapabilityVO;
import com.tms.vo.base.AddressVO;
import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * 导出地址信息，调用第三方提供的web service，将数据导出给第三方系统
 * 
 * @author songf
 * @Date 2015年11月19日 下午22:42:28
 *
 */
public class ExportAddressJobService implements IJobService {
	static Logger logger = Logger.getLogger(ExportAddressJobService.class);

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出地址信息-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有地址信息需要同步");

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

			logger.info("开始同步地址及能力信息。");
			syncAddressData(jobDefVO, listSucces, listFailed, listcapSucces, listcapFailed);
			logger.info("地址及能力信息同步结束。");

			// 更新统计信息
			afterSyncAddressData(jobDefVO, listSucces, listFailed, listcapSucces, listcapFailed);

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("-------------------导出地址信息任务执行完毕-------------------");
	}

	/**
	 * 同步地址信息 songf 2015-12-14
	 * 
	 * @param jobDefVO
	 *            执行任务
	 * @param listSucces
	 *            同步成功的主键列表
	 * @param listFialed
	 *            同步失败的主键列表
	 * @param listCapSucces
	 *            同步能力的主键列表
	 * @param listCapFailed
	 *            同步能力的主键列表
	 */
	@SuppressWarnings("rawtypes")
	private void syncAddressData(JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed,
			List<String> listCapSucces, List<String> listCapFailed) {
		// soap头
		String strHeader = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cais=\"http://www.ortec.com/CAIS\"><soapenv:Header/>"
				+ "<soapenv:Body><cais:SendMessage><cais:message><![CDATA[<comtec version='2010'>";
		// soap尾
		String strTail = "</comtec>]]></cais:message><cais:commandName>ImportAddress</cais:commandName></cais:SendMessage></soapenv:Body></soapenv:Envelope>";

		// 查询地址信息

		String sql = "SELECT ts_address.addr_code AS id , ts_address.addr_code AS code, "
				+ " ts_address.addr_name AS name,(CASE WHEN ts_address.addr_type = 1 THEN 'Supplier' WHEN ts_address.addr_type = 2 THEN 'Depo' WHEN ts_address.addr_type = 3 THEN 'Main Depot'  END ) AS address_kind,"
				+ " city.name AS city,province.name AS state_name,CAST('CN'  AS varchar(50)) country_code,"
				+ " ts_address.longitude AS givenX,ts_address.latitude AS givenY,"
				+ " (CASE WHEN ts_address.def6 is null then 0 when ts_address.def6 = 'N' then 0 when ts_address.def6 = 'Y' then 1 end ) AS is_depot,ts_address.def2 AS contact_code,ts_address.def3 AS Depot_VWED,"
				+ " ts_address.def4 AS Depot_Operational,ts_address.def5 AS CarrierDepot,ts_address.def5 AS DaysToCarrierDepot,"
				+ " ts_address.def5 AS CostRegion,ts_address.detail_addr AS comment FROM ts_address WITH(NOLOCK) "
				+ " LEFT JOIN ts_area city WITH(NOLOCK) ON ts_address.pk_city=city.pk_area AND city.dr=0 "
				+ " LEFT JOIN ts_area province WITH(NOLOCK) ON ts_address.pk_province=province.pk_area AND city.dr=0 "
				+ " LEFT JOIN ts_area country WITH(NOLOCK) ON ts_address.pk_country=country.pk_area AND country.dr=0 "
				+ " WHERE ts_address.dr=0 and isnull(ts_address.def1,'N') <>'Y' ";

//		// 查询地址能力信息
//		String sqlAddrCapability = " SELECT pk_address_capability,nw_data_dict_b.display_name ,(CASE	WHEN ts_address_capability.value =0 THEN 'required'  WHEN ts_address_capability.value =1 THEN 'available' WHEN ts_address_capability.value =2 THEN 'forbidden' END) AS value ,"
//				+ " ts_address_capability.pk_address "
//				+ " FROM   ts_address_capability"
//				+ " LEFT JOIN nw_data_dict_b ON ts_address_capability.type=nw_data_dict_b.value AND nw_data_dict_b.pk_data_dict='3b8b204410134be4a1b1cd91b7c1c08d'"
//				+ " WHERE ts_address_capability.dr=0 and ts_address_capability.pk_address  in ";

		// 开始执行时间
		Calendar start = Calendar.getInstance();

		// 记录数据主键值
		String strPkAddress = "";
		try {
		
			

			// 获取地址及地址能力信息
			List<HashMap> mapAddressList = NWDao.getInstance().queryForList(sql, HashMap.class);
			logger.info("共查询到" + mapAddressList.size() + "条地址信息需要进行同步操作。");
			if (mapAddressList != null && mapAddressList.size() > 0) {
				for (int i = 0; i < mapAddressList.size(); i++) {
					HashMap addressRowMap = mapAddressList.get(i);
					
					strPkAddress = String.valueOf(addressRowMap.get("id"));
					String strCode = String.valueOf(addressRowMap.get("code"));
					String strName = String.valueOf(addressRowMap.get("name"));
					String strAddressKind = String.valueOf(addressRowMap.get("address_kind"));
					
					String strCity = String.valueOf(addressRowMap.get("city"));
					String strState_name = String.valueOf(addressRowMap.get("state_name"));
					String strCountry_code = String.valueOf(addressRowMap.get("country_code"));
					
					String strGivenX = String.valueOf(addressRowMap.get("givenx"));
					String strGivenY = String.valueOf(addressRowMap.get("giveny"));
					String strIsDepot = String.valueOf(addressRowMap.get("is_depot"));
					String strComment = String.valueOf(addressRowMap.get("comment"));
					String strContact_code = String.valueOf(addressRowMap.get("contact_code"));
					
					String strDepot_VWED = String.valueOf(addressRowMap.get("Depot_VWED"));
					String strDepot_Operational = String.valueOf(addressRowMap.get("Depot_Operational"));
					String strCarrierDepot = String.valueOf(addressRowMap.get("CarrierDepot"));
					String strDaysToCarrierDepot = String.valueOf(addressRowMap.get("DaysToCarrierDepot"));
					String strCostRegion = String.valueOf(addressRowMap.get("CostRegion"));
					
					// 创建xml格式数据--地址信息
					Document document = DocumentHelper.createDocument();
					//Element root = document.addElement("message");
					//Element nodeComtec = root.addElement("comtec");
					Element nodeAddress = document.addElement("address");

					nodeAddress.addElement("id").addText(strCode);
					nodeAddress.addElement("code").addText(strCode);
					nodeAddress.addElement("name").addText(strName);
					
					Element nodeAddress_kind = nodeAddress.addElement("address_kind");
					nodeAddress_kind.addElement("code").addText(strAddressKind);
					
					nodeAddress.addElement("city").addText(strCity);
					nodeAddress.addElement("state_name").addText(strState_name);
					nodeAddress.addElement("country_code").addText(strCountry_code);
					nodeAddress.addElement("given_x").addText(strGivenX);
					nodeAddress.addElement("given_y").addText(strGivenY);
					nodeAddress.addElement("is_depot").addText(strIsDepot);
					nodeAddress.addElement("comment").addText(strComment);
					nodeAddress.addElement("contact_code").addText(strContact_code);

					Element nodeUdfields = nodeAddress.addElement("udfields");
					nodeUdfields.addElement("Depot_VWED").addText(strDepot_VWED);
					nodeUdfields.addElement("Depot_Operational").addText(strDepot_Operational);
					nodeUdfields.addElement("CarrierDepot").addText(strCarrierDepot);
					nodeUdfields.addElement("DaysToCarrierDepot").addText(strDaysToCarrierDepot);
					nodeUdfields.addElement("CostRegion").addText(strCostRegion);


					
					
					// 调用WS服务
					Element rootMessage = document.getRootElement();
					String strMessage = rootMessage.asXML();

					//调用JNI接口
					RouteClient client = new RouteClient();
				    RouteVO routeVO = new RouteVO();
					routeVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(routeVO);
					routeVO.setCommandname("ImportAddress");
					routeVO.setMessage(strMessage);
					NWDao.getInstance().saveOrUpdate(routeVO);
					String returnMsg =client.SendMessage(routeVO.getPrimaryKey(), "yuliuziduan");
					
					if ("TRUE".equalsIgnoreCase(returnMsg)){
						String instersql="INSERT INTO edi_his_route SELECT * FROM edi_route WITH(NOLOCK) WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						String delsql="DELETE edi_route WHERE pk_route='"+routeVO.getPrimaryKey()+"'";
						String updatesql1="UPDATE ts_address SET def1='Y'  WHERE addr_code='"+strCode+"'";
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
			if (!listFailed.contains(strPkAddress)) {
				listFailed.add(strPkAddress);
			}
			e.printStackTrace();
		}
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFailed.size() + "条数据。");

	}

	/**
	 * 判断任务是否同步数据 songf 2015-11-14
	 * 
	 * @param jobDefVO
	 *            同步任务信息
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
	 * 数据同步后处理，更新 ts_address,ts_addr_capability的状态 ，更新 任务的统计信息 songf 2015-12-14
	 * 
	 * @param jobDefVO
	 *            同步任务信息
	 * @param listSucces
	 *            ts_address同步成功明细主键列表
	 * @param listFailed
	 *            ts_address同步失败明细主键列表
	 * @param listcapSucces
	 *            ts_address_capability同步成功明细主键列表
	 * @param listcapFailed
	 *            ts_address_capability同步失败明细主键列表
	 */
	private void afterSyncAddressData(final JobDefVO jobDefVO, List<String> listSucces, List<String> listFailed,
			List<String> listcapSucces, List<String> listcapFailed) {
		try {
			if (listSucces.size() == 0 && listFailed.size() == 0) {
				return;
			}
			List<SuperVO> addressUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_address 将同步成功的明细def1 改为Y
			if (listSucces.size() > 0) {
				AddressVO[] addressVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class,
						"pk_address in " + NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (addressVOSuccs != null && addressVOSuccs.length > 0) {
					for (AddressVO addressVO : addressVOSuccs) {

						// 更新状态为已同步
						addressVO.setDef1("Y");
						addressVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						addressUpdateSuccessList.add(addressVO);
					}
				}
			}
			// 保存数据
			if (addressUpdateSuccessList != null && addressUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(addressUpdateSuccessList);
			}

			List<SuperVO> addressUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_address 将同步失败的明细def1 改为N
			if (listFailed.size() > 0) {
				AddressVO[] addressVOFaileds = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class,
						"pk_address in " + NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (addressVOFaileds != null && addressVOFaileds.length != 0) {
					for (AddressVO addressVO : addressVOFaileds) {

						// 更新状态为同步失败
						addressVO.setDef1("N");
						addressVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						addressUpdateFailedList.add(addressVO);
					}
				}
			}
			// 保存数据
			if (addressUpdateFailedList != null && addressUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(addressUpdateFailedList);
			}

			List<SuperVO> addressCapUpdateSuccessList = new ArrayList<SuperVO>();
			// 先更新ts_address_capability 将同步成功的明细def1 改为Y
			if (listcapSucces.size() > 0) {
				int index = 0;
				for (; index < listcapSucces.size(); index++) {
					String strFailed = listcapSucces.get(index);
					String[] strSuccessArray = strFailed.split(",");
				AddressCapabilityVO[] addresscapVOSuccs = NWDao.getInstance()
						.queryForSuperVOArrayByCondition(AddressCapabilityVO.class, "pk_address_capability in " + NWUtils
								.buildConditionString(strSuccessArray));
				if (addresscapVOSuccs != null && addresscapVOSuccs.length > 0) {
					for (AddressCapabilityVO addresscapVO : addresscapVOSuccs) {

						// 更新状态为已同步
						addresscapVO.setDef1("Y");
						addresscapVO.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						addressCapUpdateSuccessList.add(addresscapVO);
					 }
				   }
				}
			}
			// 保存数据
			if (addressCapUpdateSuccessList != null && addressCapUpdateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(addressCapUpdateSuccessList);
			}

			List<SuperVO> addressCapUpdateFailedList = new ArrayList<SuperVO>();
			// 先更新ts_address 将同步失败的明细def1 改为N
			if (listcapFailed.size() > 0) {
				int index = 0;
				for (; index < listcapFailed.size(); index++) {
					String strFailed = listcapFailed.get(index);
					String[] strFailedArray = strFailed.split(",");
					AddressCapabilityVO[] addresscapVOFaileds = NWDao.getInstance().queryForSuperVOArrayByCondition(
							AddressCapabilityVO.class, "pk_address_capability in " + NWUtils.buildConditionString(strFailedArray));
					if (addresscapVOFaileds != null && addresscapVOFaileds.length != 0) {
						for (AddressCapabilityVO addresscapVO : addresscapVOFaileds) {

							// 更新状态为同步失败
							addresscapVO.setDef1("N");
							addresscapVO.setStatus(VOStatus.UPDATED);
							// 保存更新的状态
							addressCapUpdateFailedList.add(addresscapVO);
						}
					}
				}
			}
			// 保存数据
			if (addressCapUpdateFailedList != null && addressCapUpdateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(addressCapUpdateFailedList);
			}

			// 如果是间隔同步要修改同步时间
			if (jobDefVO.getExec_type() == 1) {
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
			lSyncSuccessRecord += (long) listSucces.size();
			jobDefVO.setDef3(String.valueOf(lSyncSuccessRecord));

			// 获取总同步数据量，更新数据 def4为同步失败条数
			if (jobDefVO.getDef4() == null) {
				jobDefVO.setDef4("0");
			}
			long lSyncFailedRecord = Long.parseLong(jobDefVO.getDef4());
			lSyncFailedRecord += (long) listFailed.size();
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
	@SuppressWarnings({"unused" })
	private List<TrackVO> createHttpTask(final JobDefVO jobDefVO, Map<String, Object> paramMap) {

		return null;
	}

	/**
	 * 创建webservice请求的任务
	 * 
	 * @param otVO
	 * @return
	 */
	@SuppressWarnings("unused")
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
