package com.tms.service.te.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFDateTime;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.EquipTrackingService;
import com.tms.vo.te.VehicleViewVO;


@Service()
public class EquipTrackingServiceImpl extends TMSAbsBillServiceImpl implements EquipTrackingService {

	public String getBillType() {
		return null;
	}

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<TrackVO> getLBSByGps_ids(String[] gps_ids) {
		if(gps_ids == null || gps_ids.length == 0) {
			return null;
		}
		RootVO rootVO = LBSUtils.getCurrentTrackVO(gps_ids);
		List<TrackVO> trackVOs = rootVO.getDataset();
		return trackVOs;
	}

	public VehicleViewVO[] getVehicleViewVOsByUserAndKeyWord(String pk_user,String keyword) {
		if(StringUtils.isBlank(pk_user)){
			return null;
		}
		if(StringUtils.isNotBlank(keyword)){
			keyword = " and (carno like '%"+ keyword +"%' or gps_id like '%"+ keyword +"%')"; 
		}else{
			keyword = "";
		}
		VehicleViewVO[] vehicleViewVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(VehicleViewVO.class, CorpHelper.getCurrentCorpWithChildren()+keyword);
		
		return vehicleViewVOs;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> loadEquipInfo(String equipcode){
		String vehicleFileds = "carr_name,carno,car_type,car_type,car_status,driver_name,driver_mobile,photo,gps_id,pk_corp,ent_status,customs,"
				+ "dangerous_veh,tonnage,kpi_point,bill_num,exp_num,deli_rate,arri_rate,lost_rate,brok_rate,near_num";
		String vehicleSql = "SELECT "+vehicleFileds+" FROM ts_vehicle_view WHERE carno =?";
		Map<String,Object> vehicleInfo = NWDao.getInstance().queryForObject(vehicleSql, HashMap.class, equipcode);
		if(vehicleInfo == null || vehicleInfo.size() == 0 || null == vehicleInfo.get("carno")){
			return null;
		}
		
		//根据车牌号查询委托单信息 
		String orderFileds = "lot,vbillno,vbillstatus,carr_name,carno,cust_name,cust_orderno,req_deli_date,req_arri_date,act_arri_date,act_deli_date,deli_addr,arri_addr,weight_count,num_count,volume_count,ent_memo,kpi";
		String vehicleOrdersSql = "SELECT "+orderFileds+" FROM ts_vehicle_orders_view WHERE carno =?";
		List<HashMap> vehicleOrdersInfos = NWDao.getInstance().queryForList(vehicleOrdersSql, HashMap.class, equipcode);
		if(vehicleOrdersInfos == null || vehicleOrdersInfos.size() == 0){
			return vehicleInfo;
		}
		//将结果按照批次号合并，并取出最早和最晚的时间信息
		Map<String,List<Map<String,Object>>> groupMap = new HashMap<String, List<Map<String,Object>>>();
		for(Map<String,Object> map : vehicleOrdersInfos){
			String key = map.get("lot").toString();
			List<Map<String,Object>> mapList = groupMap.get(key);
			if(mapList == null){
				mapList = new ArrayList<Map<String,Object>>();
				groupMap.put(key, mapList);
			}
			mapList.add(map);
		}
		List<Map<String,Object>> vehiclelotsInfos = new ArrayList<Map<String,Object>>();
		for(String key : groupMap.keySet()){
			List<Map<String,Object>> mapList = groupMap.get(key);
			if(mapList != null && mapList.size() > 0){
				//合并件重体，取最早和最晚时间。
				Integer num_count = 0;
				double weight_count = 0;
				double volume_count = 0;
				UFDateTime earliest = new UFDateTime();
				UFDateTime latest = new UFDateTime();
				String deli_addr = "";
				String arri_addr = "";
				int index = 0;
				for(Map<String,Object> map : mapList){
					if(index == 0){
						earliest = new UFDateTime((map.get("req_deli_date").toString()));
						latest = new UFDateTime((map.get("req_arri_date").toString()));
						deli_addr = map.get("deli_addr")== null ? "" : map.get("deli_addr").toString();
						arri_addr = map.get("arri_addr")== null ? "" : map.get("arri_addr").toString();
					}
					index ++;
					num_count += map.get("num_count") == null ? 0 : Integer.parseInt(map.get("num_count").toString());
					weight_count += map.get("weight_count") == null ? 0 : Double.parseDouble(map.get("weight_count").toString());
					volume_count += map.get("volume_count") == null ? 0 : Double.parseDouble(map.get("volume_count").toString());
					UFDateTime req_deli_date = new UFDateTime((map.get("req_deli_date").toString()));
					UFDateTime req_arri_date = new UFDateTime((map.get("req_arri_date").toString()));
					if(req_deli_date.before(earliest)){
						earliest = req_deli_date;
						deli_addr = map.get("deli_addr") == null ? "" : map.get("deli_addr").toString();
					}
					if(req_arri_date.after(latest)){
						latest = req_arri_date;
						arri_addr = map.get("arri_addr") == null ? "" : map.get("arri_addr").toString();
					}
				}
				Map<String,Object> lotInfo = mapList.get(0);
				lotInfo.put("num_count", num_count);
				lotInfo.put("weight_count", weight_count);
				lotInfo.put("volume_count", volume_count);
				lotInfo.put("req_deli_date", earliest);
				lotInfo.put("req_arri_date", latest);
				lotInfo.put("deli_addr", deli_addr);
				lotInfo.put("arri_addr", arri_addr);
				vehiclelotsInfos.add(lotInfo);
			}
		}
		for(Map<String, Object> vehiclelotsInfo : vehiclelotsInfos){
			List<Map<String, Object>> unitOrders = new ArrayList<Map<String,Object>>();
			for(Map<String, Object> vehicleOrdersInfo : vehicleOrdersInfos){
				//一个车辆下面有多个批次，一个批次下面有多个订单
				if(vehicleOrdersInfo.get("lot").equals(vehiclelotsInfo.get("lot"))){
					//这里直接使用vehicleOrdersInfo会出现嵌套现象，导致内存溢出。 
					Map<String,Object> order = new HashMap<String, Object>();
					order.put("lot", vehicleOrdersInfo.get("lot"));
					order.put("vbillno", vehicleOrdersInfo.get("vbillno"));
					order.put("vbillstatus", vehicleOrdersInfo.get("vbillstatus"));
					order.put("carr_name", vehicleOrdersInfo.get("carr_name"));
					order.put("carno", vehicleOrdersInfo.get("carno"));
					order.put("cust_name", vehicleOrdersInfo.get("cust_name"));
					order.put("cust_orderno", vehicleOrdersInfo.get("cust_orderno"));
					order.put("req_deli_date", vehicleOrdersInfo.get("req_deli_date"));
					order.put("req_arri_date", vehicleOrdersInfo.get("req_arri_date"));
					order.put("act_arri_date", vehicleOrdersInfo.get("act_arri_date"));
					order.put("act_deli_date", vehicleOrdersInfo.get("act_deli_date"));
					order.put("deli_addr", vehicleOrdersInfo.get("deli_addr"));
					order.put("arri_addr", vehicleOrdersInfo.get("arri_addr"));
					order.put("weight_count", vehicleOrdersInfo.get("weight_count"));
					order.put("num_count", vehicleOrdersInfo.get("num_count"));
					order.put("volume_count", vehicleOrdersInfo.get("volume_count"));
					order.put("ent_memo", vehicleOrdersInfo.get("ent_memo"));
					order.put("kpi", vehicleOrdersInfo.get("kpi"));
					unitOrders.add(order);
				}
			}
			vehiclelotsInfo.put("orders", unitOrders);
		}
		vehicleInfo.put("lots", vehiclelotsInfos);
		return vehicleInfo;
		
	}

	public VehicleViewVO[] getVehicleViewVOsByEquipcodes(String[] equipcodeArrs) {
		if(equipcodeArrs == null || equipcodeArrs.length == 0){
			return null;
		}
		String cond = NWUtils.buildConditionString(equipcodeArrs);
		VehicleViewVO[] vehicleViewVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(VehicleViewVO.class,"carno in " + cond);
		return vehicleViewVOs;
	}

	public Integer getRefreshInterval() {
		return ParameterHelper.getIntParam(ParameterHelper.REFRESH_INTERVAL);
	}

	
	
	

	
}
