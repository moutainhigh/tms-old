package com.tms.service.job.edi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.JobDefVO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.pod.PodVO;

import org.apache.log4j.Logger;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.types.Schema;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * 裕络签收回单导出，调用对方web service，将数据导出给对方系统
 * 
 * @author songf
 * @Date 2015年11月14日 下午15:34:28
 *
 */
public class ExportInvoicePodJobService implements IJobService {
	static Logger logger = Logger.getLogger(ExportInvoicePodJobService.class);

	@SuppressWarnings("rawtypes")
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导出发货单签收回单信息-------------------");
		if (jobDefVO == null) {
			return;
		}
	
		logger.info("判断是否需要同步发货单签收数据。");
		// 判断是否需要同步数据
		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		
		logger.info("检测通过，有数据要同步。");

		// 记录提供同步成功与失败
		List<String> listSuccesDeliPKInvoice = new ArrayList<String>();
		List<String> listFailedDeliPKInvoice = new ArrayList<String>();
		
		logger.info("开始进行发货单回单同步操作。");
		
		// 记录提供同步成功与失败
		List<String> listSuccesArriPKInvoice = new ArrayList<String>();
		List<String> listFailedArriPKInvoice = new ArrayList<String>();

		// 同步到货信息
		syncSignPodData(jobDefVO, listSuccesArriPKInvoice, listFailedArriPKInvoice);
		
		logger.info("发货单回单同步操作结束。");
		
		//更新统计信息及状态
		afterSignPodSyncData(jobDefVO, listSuccesArriPKInvoice, listFailedArriPKInvoice);

		logger.info("-------------------导出发货单跟踪信息任务执行完毕-------------------");
	}

	/**
	 * 同步签收回回单信息 songf 2015-11-14
	 * 
	 * @param jobDefVO 执行任务
	 * @param listSucces 同步成功的主键列表
	 * @param listFialed 同步失败的主键列表
	 */
	private void syncSignPodData(JobDefVO jobDefVO, List<String> listSucces, List<String> listFialed) {
		// 定义查询SQL
		String sql = "";

		// 回单
		sql = "SELECT ts_pod.pk_pod,ts_invoice.cust_orderno AS ID,ts_invoice.cust_orderno AS SHIPMENT_GID,ts_pod. pod_date AS SIGN_DATE,ts_pod.pod_man AS SIGN_BY,"
				+ " CAST('' as varchar(50)) SIGN_STATUS ,ts_pod.pod_num_count AS SIGN_COUNT,CAST(0  as INT) RECEIVED_COUNT,CAST(0  as INT) REJECTION_COUNT,"
				+ " CAST(0  as INT) DAMAGE_COUNT,CAST(0  as INT) LOST_COUNT,CAST('' as varchar(50)) SIGN_REMARK, CAST('' as varchar(50)) SIGNED_ID_CARD,"
				+ " ts_pod.pod_book_time RECEIPT_DATE,ts_pod.receipt_man RECEIPT_BY, ts_pod. pod_date ACTUAL_SIGNED_DATE,CAST('' as varchar(50)) POD_RESULT,"
				+ " CAST('' as varchar(50)) SRC_POD, ts_pod.receipt_book_time FAX_D_DATE,ts_pod.pod_num_count AS RECEIPT_COUNT,CAST(0  as INT) IS_CASH_TO_PAY,"
				+ " CAST(0  as INT) PAY_AMOUNT, CAST('' as varchar(50)) RECEIPT_REMARK" + " FROM ts_pod "
				+ " LEFT JOIN ts_invoice ON ts_pod.pk_invoice=ts_invoice.pk_invoice"
				+ " where ts_invoice.vbillstatus in (6) and (ts_invoice.pk_arrival in ('bc647cc46df644db9fcf21df0395e1ae','ce2356b6e7254aca85f0c6a0a93d6dc7')"
				+ " or ts_invoice.pk_delivery in ('bc647cc46df644db9fcf21df0395e1ae','ce2356b6e7254aca85f0c6a0a93d6dc7'))  AND  isnull(ts_pod.def10,'N') <>'Y' AND ts_invoice.cust_orderno = 'ED.SP1505260459'";

		// 开始执行时间
		Calendar start = Calendar.getInstance();
		
		//记录数据主键值
		String strPkPod = "";
		try {
			// webservice路径
			String endpoint = jobDefVO.getUrl();

			// 指定调用接口名称及命名空间
			String operationName = "UpdatePodSignConfirmInfo";
			String targetNamespace = "http://tempuri.org/";

			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(endpoint);

			// 解决错误：服务器未能识别 HTTP 头 SOAPAction 的值
			call.setUseSOAPAction(true);
			call.setSOAPActionURI("http://tempuri.org/UpdatePodSignConfirmInfo");

			// WSDL里面描述的接口名称
			call.setOperationName(new QName(targetNamespace, operationName));

			// 设置方法参数
			call.addParameter(new QName(targetNamespace, "signData"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName(targetNamespace, "usergid"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);

			// 设置返回类型
			call.setReturnType(org.apache.axis.encoding.XMLType.XSD_SCHEMA);

			// 获取数据
			List<HashMap> mapInvoicePodList = NWDao.getInstance().queryForList(sql, HashMap.class);
			if(mapInvoicePodList.size() == 0){
				return;
			}
			logger.info("共查询到" + mapInvoicePodList.size() + "条提货信息");
			if (mapInvoicePodList != null && mapInvoicePodList.size() > 0) {
				for (int i = 0; i < mapInvoicePodList.size(); i++) {
					HashMap payRowMap = mapInvoicePodList.get(i);
					String strid = String.valueOf(payRowMap.get("id"));
					strPkPod = String.valueOf(payRowMap.get("pk_pod"));
					String strShipmentGID = String.valueOf(payRowMap.get("shipment_gid"));
					String strSignDate = String.valueOf(payRowMap.get("sign_date"));
					String strSignBy = String.valueOf(payRowMap.get("sign_by"));
					String strSignStatus = String.valueOf(payRowMap.get("pod_status"));
					String strSignCount = String.valueOf(payRowMap.get("sign_count"));
					String strRecivedCount = String.valueOf(payRowMap.get("received_count"));
					String strRejectionCount = String.valueOf(payRowMap.get("rejection_count"));
					String strDamageCount = String.valueOf(payRowMap.get("damage_count"));
					String strLostCount = String.valueOf(payRowMap.get("lost_count"));
					String strSignRemark = String.valueOf(payRowMap.get("sign_remark"));
					String strSignIDCard = String.valueOf(payRowMap.get("signed_id_card"));
					String strReceiptDate = String.valueOf(payRowMap.get("receipt_date"));
					String strReceiptBy = String.valueOf(payRowMap.get("receipt_by"));
					String strActualSignDate = String.valueOf(payRowMap.get("actual_signed_date"));
					String strPodResult = String.valueOf(payRowMap.get("pod_result"));
					String strSrcPod = String.valueOf(payRowMap.get("src_pod"));
					String strFaxDate = String.valueOf(payRowMap.get("fax_d_date"));
					String strReceiptCount = String.valueOf(payRowMap.get("receipt_count"));

					String strIsCashToPay = String.valueOf(payRowMap.get("is_cash_to_pay"));
					String strPayAmount = String.valueOf(payRowMap.get("pay_amount"));
					String strReceiptRemark = String.valueOf(payRowMap.get("receipt_remark"));

					Object[] opTransArgs = new Object[] { "[{\"ID\":\"" + strid + "\",\"SHIPMENT_GID\":\""
							+ strShipmentGID + "\",\"SIGN_DATE\":\"" + strSignDate + "\"," + "\"SIGN_BY\":\""
							+ strSignBy + "\",\"SIGN_STATUS\":\"" + strSignStatus + "\",\"SIGN_COUNT\":\""
							+ strSignCount + "\"," + "\"RECEIVED_COUNT\":\"" + strRecivedCount
							+ "\",\"REJECTION_COUNT\":\"" + strRejectionCount + "\",\"DAMAGE_COUNT\":\""
							+ strDamageCount + "\"," + "\"LOST_COUNT\":\"" + strLostCount + "\",\"SIGN_REMARK\":\""
							+ strSignRemark + "\",\"SIGN_ID_CARD\":\"" + strSignIDCard + "\",\"RECEIPT_DATE\":\""
							+ strReceiptDate + "\"" + ",\"RECEIPT_BY\":\"" + strReceiptBy + "\",\"ACTUAL_SIGN_DATE\":\""
							+ strActualSignDate + "\",\"POD_RESULT\":\"" + strPodResult + "\",\"SRC_POD\":\""
							+ strSrcPod + "\"" + ",\"FAX_DATE\":\"" + strFaxDate + "\",\"RECEIPT_COUNT\":\""
							+ strReceiptCount + "\",\"IS_CASH_TO_PAY\":\"" + strIsCashToPay + "\",\"PAY_AMOUNT\":\""
							+ strPayAmount + "\"" + ",\"RECEIPT_REMARK\":\"" + strReceiptRemark + "\"}]", "DS.ADMIN" };
					// 调用ws方法
					Object result = call.invoke(opTransArgs);

					Schema schema = (Schema) result;
					MessageElement[] msgele = schema.get_any();

					// 截取返回值的信息，判断是否成功
					String strResultDeatil = msgele[2].getAsString();
					int iStart = strResultDeatil.indexOf("<ns1:IsSucc>");
					int iStop = strResultDeatil.indexOf("</ns1:IsSucc>");
					String strReturn = strResultDeatil.substring(iStart + 12, iStop);
					boolean isSuccess = Boolean.parseBoolean(strReturn);

					// 返回成功加入成功列表，否则返回失败列表
					if (isSuccess) {
						if (!listSucces.contains(strPkPod)) {
							listSucces.add(strPkPod);
						}
					} else {
						//获取失败原因，加入失败列表
						int iMsgStart = strResultDeatil.indexOf("<ns1:Descr>");
						int iMsgStop = strResultDeatil.indexOf("</ns1:Descr>");
						String strMsgReturn = strResultDeatil.substring(iMsgStart + 11, iMsgStop);
						
						//获取错误信息，暂时不作处理
						strMsgReturn = StringEscapeUtils.unescapeHtml(strMsgReturn);
			
						if (!listFialed.contains(strPkPod)) {
							listFialed.add(strPkPod);
						}
					}
				}
			}
		} catch (Exception e) {
			// 记录失败的ID
			if (!listFialed.contains(strPkPod)) {
				listFialed.add(strPkPod);
			}
			e.printStackTrace();
		}
		long interval = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
		logger.info("共执行：" + interval / 1000 + "秒，成功" + listSucces.size() + "条数据，失败" + listFialed.size() + "条数据。");

	}

	/**
	 * 判断任务是否同步数据 songf 2015-11-14
	 * 
	 * @param jobDefVO 执行任务
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
	 * 同步签收回单后处理  songf 2015-11-14
	 * 
	 * @param jobDefVO 执行任务
	 * @param listSucces 同步成功的主键列表
	 * @param listFialed 同步失败的主键列表
	 */
	private void afterSignPodSyncData(final JobDefVO jobDefVO, List<String> listSucces,List<String> listFailed) {
		try {
			
			if(listSucces.size() == 0 && listFailed.size() == 0){
				return;
			}
			List<SuperVO> updateSuccessList = new ArrayList<SuperVO>();
			
			// 先Pod签收成功的数据状态
			if (listSucces.size() > 0) {
				PodVO[] podVOSuccs = NWDao.getInstance().queryForSuperVOArrayByCondition(PodVO.class,
						"pk_pod in " + NWUtils.buildConditionString(listSucces.toArray(new String[listSucces.size()])));
				if (podVOSuccs != null && podVOSuccs.length != 0) {
					for (PodVO podVOSucc : podVOSuccs) {

						// 更新回单状态为已同步
						podVOSucc.setDef9("Y");

						podVOSucc.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						updateSuccessList.add(podVOSucc);
					}
				}
			}
			// 保存数据
			if (updateSuccessList != null && updateSuccessList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(updateSuccessList);
			}

			List<SuperVO> updateFailedList = new ArrayList<SuperVO>();
			
			// 先Pod签收失败的数据状态
			if (listFailed.size() > 0) {
				PodVO[] podVOFaileds = NWDao.getInstance().queryForSuperVOArrayByCondition(PodVO.class,
						"pk_pod in " + NWUtils.buildConditionString(listFailed.toArray(new String[listFailed.size()])));
				if (podVOFaileds != null && podVOFaileds.length != 0) {
					for (PodVO podVOFailed : podVOFaileds) {

						// 更新回单状态为同步失败
						podVOFailed.setDef9("N");
						podVOFailed.setStatus(VOStatus.UPDATED);
						// 保存更新的状态
						updateFailedList.add(podVOFailed);
					}
				}
			}
			// 保存数据
			if (updateFailedList != null && updateFailedList.size() > 0) {
				NWDao.getInstance().saveOrUpdate(updateFailedList);
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
