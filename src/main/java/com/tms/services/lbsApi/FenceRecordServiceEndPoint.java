package com.tms.services.lbsApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.json.JacksonUtils;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.tms.BillStatus;
import com.tms.constants.DataDictConst;
import com.tms.constants.TrackingConst;
import com.tms.service.te.TrackingService;
import com.tms.services.peripheral.WebServicesUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;

/**
 * @author XIA
 * @for LBS 同步围栏记录接口
 */
@SuppressWarnings("deprecation")
public class FenceRecordServiceEndPoint extends ServletEndpointSupport {
	@Autowired
	private TrackingService trackingService;
	
	public TrackingService getTrackingService() {
		return trackingService;
	}

	public void setTrackingService() {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
		this.trackingService = (TrackingService) wac.getBean("trackingService");
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> handFenceRecord(Map<String,String> invokeParams){
		String pk_addrs = invokeParams.get("addrs");
		String gps_id = invokeParams.get("gps_id");
		String record = invokeParams.get("record");
		String fence_name = invokeParams.get("fence_name");
		
		boolean success = false;
		String msg = "";
		
		if(StringUtils.isBlank(pk_addrs)){
			msg = "地址不能为空!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		String[] pk_addressS = pk_addrs.split(",");
		if(pk_addressS == null || pk_addressS.length == 0){
			msg = "地址不能为空!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		if(StringUtils.isBlank(record) || JacksonUtils.readValue(record, Map.class) == null){
			msg = "记录不能为空!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		if(StringUtils.isBlank(gps_id)){
			msg = "gps_id不能为空!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, 
				"pk_address in " + NWUtils.buildConditionString(pk_addressS));
		if(addressVOs == null || addressVOs.length == 0){
			if(pk_addressS == null || pk_addressS.length == 0){
				msg = "地址性质不正确!";
				return WebServicesUtils.genAjaxResponse(success, msg, null);
			}
		}
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment("EdiUser");
		//初始化服务类
		setTrackingService();
		this.trackingService = getTrackingService();
		
		//根据GPS_ID和GPS_time 和地址信息，获取订单信息
		Map<String,Object> recordMap = JacksonUtils.readValue(record, Map.class);
		UFDateTime gps_time = new UFDateTime();
		try {
			gps_time = new UFDateTime(String.valueOf(recordMap.get("entry_time")));
		} catch (Exception e) {
			msg = "围栏进出时间不正确!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		
		String sql = "SELECT te.* FROM ts_entrust te With (nolock) "
				+ " LEFT JOIN ts_ent_transbility_b tetb With (nolock) ON tetb.pk_entrust=te.pk_entrust AND  isnull(tetb.dr,0)=0 AND tetb.gps_id IS NOT NULL "
				+ " WHERE isnull(te.dr,0)=0 AND te.lot IS NOT NULL  AND te.req_deli_date >getdate()-15  AND te.req_deli_date <getdate()+15   "
				+ " AND tetb.gps_id =? ";
		List<EntrustVO> entrustVOs = NWDao.getInstance().queryForList(sql, EntrustVO.class, gps_id);
		if(entrustVOs == null || entrustVOs.size() == 0){
			msg = "没有对应的委托单信息!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		List<EntrustVO> needToOperate = getOnOperationEnt(entrustVOs);
		if(needToOperate == null || needToOperate.size() == 0){
			msg = "没有找到需要操作的委托单!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		String[] entPks = new String[needToOperate.size()];
		for(int i=0;i<needToOperate.size();i++){
			entPks[i] = needToOperate.get(i).getPk_entrust();
		}
		EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class,
				"pk_entrust in " + NWUtils.buildConditionString(entPks));
		if(entLineBVOs == null || entLineBVOs.length == 0){
			msg = "委托单节点信息不正确!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		//对entLineBVOs按照委托单分组
		Map<String,List<EntLineBVO>> groupMap = new HashMap<String, List<EntLineBVO>>();
		for(EntLineBVO entLineBVO : entLineBVOs){
			String key = entLineBVO.getPk_entrust();
			List<EntLineBVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<EntLineBVO>();
				groupMap.put(key, voList);
			}
			voList.add(entLineBVO);
		}
		for(String key : groupMap.keySet()){
			List<EntLineBVO> voList = groupMap.get(key);
			if(voList != null && voList.size() > 0){
				for(EntLineBVO entLineBVO : voList){
					for(AddressVO addressVO : addressVOs){
						if(addressVO.getPk_address().equals(entLineBVO.getPk_address())){
							//生成跟踪记录
							for(EntrustVO entrustVO : needToOperate){
								if(entrustVO.getPk_entrust().equals(entLineBVO.getPk_entrust())){
									EntTrackingVO etVO = new EntTrackingVO();
									etVO.setStatus(VOStatus.NEW);
									NWDao.setUuidPrimaryKey(etVO);
									if(Integer.parseInt(String.valueOf(recordMap.get("entry_type"))) == 0){
										etVO.setTracking_status(TrackingConst.ENTRY);
										etVO.setTracking_memo(gps_id + " 驶入围栏" + fence_name);
									}
									if(Integer.parseInt(String.valueOf(recordMap.get("entry_type"))) == 1){
										etVO.setTracking_status(TrackingConst.EXIT);
										etVO.setTracking_memo(gps_id + " 驶离围栏" + fence_name);
									}
									etVO.setTracking_time(gps_time);
									etVO.setPk_corp(entrustVO.getPk_corp());
									etVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
									etVO.setCreate_time(new UFDateTime(new Date()));
									etVO.setEntrust_vbillno(entrustVO.getVbillno());
									etVO.setInvoice_vbillno(entrustVO.getInvoice_vbillno());
									etVO.setTracking_origin(DataDictConst.TRACK_ORIG.LBS.intValue());
									etVO.setSync_flag(UFBoolean.TRUE);
									if(StringUtils.isNotBlank(entrustVO.getInvoice_vbillno())){
										trackingService.saveEntTracking(etVO, entrustVO,
												null, null, entrustVO.getInvoice_vbillno().split(","));
									}else{
										trackingService.saveEntTracking(etVO, entrustVO,
												null, null, null);
									}
									
								}
							}
						}
					}
				}
			}
		}
		success = true;
		msg = "保存成功!";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	private List<EntrustVO> getOnOperationEnt(List<EntrustVO> entrustVOs){
		//对委托单按照批次进行分组
		Map<String,List<EntrustVO>> groupMap = new HashMap<String, List<EntrustVO>>();
		for(EntrustVO entrustVO : entrustVOs){
			String key = entrustVO.getLot();
			List<EntrustVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<EntrustVO>();
				groupMap.put(key, voList);
			}
			voList.add(entrustVO);
		}
		Map<String,UFDateTime> lotAndReq_deli_date = new HashMap<String, UFDateTime>();
		for(String key : groupMap.keySet()){
			List<EntrustVO> voList = groupMap.get(key);
			//判断这个批次下是否全部到货了，如果全部到货则跳过这个批次，它不需要操作
			//如果是部分操作，部分未操作，那就不需要向下排序了，因为肯定是在操作这个单子
			if(voList != null && voList.size() > 0){
				boolean isAllOpening = true;
				for(EntrustVO entrustVO : voList){
					if(entrustVO.getVbillstatus().equals(BillStatus.ENT_ARRIVAL)
							|| entrustVO.getVbillstatus().equals(BillStatus.ENT_VENT)
							|| entrustVO.getVbillstatus().equals(BillStatus.NEW)){
						//这个委托单已经结束了
						isAllOpening = false;
					}else if(entrustVO.getVbillstatus().equals(BillStatus.ENT_CONFIRM)){
					}else if(entrustVO.getVbillstatus().equals(BillStatus.ENT_DELIVERY)){
						//正在执行的委托单
						return voList;
					}
				}
				if(isAllOpening){
					//这个单子全部未操作，需要对时间进行排序
					UFDateTime temp = new UFDateTime(voList.get(0).getReq_deli_date());
					for(EntrustVO entrustVO : voList){
						UFDateTime req_deli_date = new UFDateTime(entrustVO.getReq_deli_date());
						if(req_deli_date.before(temp)){
							temp = req_deli_date;
						}
					}
					lotAndReq_deli_date.put(key, temp);
				}
			}
		}
		
		if(lotAndReq_deli_date.size() > 0){
			String temp = "";
			for(String key : lotAndReq_deli_date.keySet()){
				temp = key;
				break;
			}
			for(String key : lotAndReq_deli_date.keySet()){
				if(lotAndReq_deli_date.get(key).before(lotAndReq_deli_date.get(temp))){
					temp = key;
				}
			}
			return groupMap.get(temp);
		}
		return null;
	}

}
