package com.tms.web.te;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.json.JacksonUtils;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.EquipTrackingService;
import com.tms.vo.te.VehicleViewVO;

@Controller
@RequestMapping(value = "/te/et")
public class EquipTrackingController extends AbsBillController {

	@Autowired
	private EquipTrackingService equipTrackingService;

	public EquipTrackingService getService() {
		return equipTrackingService;
	}
	private boolean split = true;//4等分为true 9 等分为false
	
	@RequestMapping(value = "/loadQueryEquipData.json")
	@ResponseBody
	public String loadQueryEquipData(HttpServletRequest request, HttpServletResponse response)throws UnsupportedEncodingException {
		String keyword = request.getParameter("keyword");
		if(StringUtils.isBlank(keyword)){
			return null;
		}
		keyword = new String(keyword.getBytes("ISO8859-1"), "UTF-8");
		// 1.获取当前登录用户所有的车辆信息，过滤权限
		VehicleViewVO[] vehicleViewVOs = this.getService()
				.getVehicleViewVOsByUserAndKeyWord(WebUtils.getLoginInfo().getPk_user(), keyword);
		if (vehicleViewVOs == null || vehicleViewVOs.length == 0) {
			return null;
		}
		String[] gps_ids = new String[vehicleViewVOs.length];
		int index = 0;
		for (VehicleViewVO vehicleViewVO : vehicleViewVOs) {
			if (StringUtils.isNotBlank(vehicleViewVO.getGps_id())) {
				gps_ids[index] = vehicleViewVO.getGps_id();
				index++;
			}
		}
		if (gps_ids.length == 0) {
			return null;
		}
		List<TrackVO> trackVOs = this.getService().getLBSByGps_ids(gps_ids);
		if (trackVOs == null || trackVOs.size() == 0) {
			logger.info("没有从LBS里获取到数据，请保证接口和数据正确性！");
			return null;
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for (TrackVO trackVO : trackVOs) {
			for (VehicleViewVO vehicleViewVO : vehicleViewVOs) {
				if (vehicleViewVO.getGps_id().equals(trackVO.getGpsid())) {
					// 组织信息
					Map<String, Object> retMap = new HashMap<String, Object>();
					retMap.put("equipcode", vehicleViewVO.getCarno());
					retMap.put("photo", vehicleViewVO.getPhoto() == null ? "car" : vehicleViewVO.getPhoto());
					retMap.put("speed", trackVO.getSpeed());
					retMap.put("icon", trackVO.getIcon());
					retMap.put("speed_status", trackVO.getSpeed_status());
					retMap.put("road_name", trackVO.getRoad_name() == null ? "&nbsp;" : trackVO.getRoad_name());
					retMap.put("place_name", trackVO.getPlace_name() == null ? "&nbsp;" : trackVO.getPlace_name());
					retMap.put("carrier",
							vehicleViewVO.getCarr_name() == null ? "&nbsp;" : vehicleViewVO.getCarr_name());
					retMap.put("driver",
							vehicleViewVO.getDriver_name() == null ? "&nbsp;" : vehicleViewVO.getDriver_name());
					retMap.put("mobile",
							vehicleViewVO.getDriver_mobile() == null ? "&nbsp;" : vehicleViewVO.getDriver_mobile());
					retMap.put("customs", vehicleViewVO.getCustoms());
					retMap.put("dangerous_veh", vehicleViewVO.getDangerous_veh());
					retMap.put("tonnage", vehicleViewVO.getTonnage());
					retMap.put("equip_type", vehicleViewVO.getCar_type());
					retMap.put("equip_status", vehicleViewVO.getCar_status());
					retMap.put("ent_status", vehicleViewVO.getEnt_status());
					retMap.put("distance", trackVO.getDistance());
					retMap.put("gps_time", trackVO.getGps_time());
					retMap.put("latitude", trackVO.getLatitude());
					retMap.put("longitude", trackVO.getLongitude());
					retMap.put("memo", trackVO.getMemo());
					retList.add(retMap);
					break;
				}
			}
		}
		return JacksonUtils.writeValueAsString(retList);
	}
	
	
	@RequestMapping(value = "/loadEquipData.json")
	@ResponseBody
	public String loadEquipData(HttpServletRequest request, HttpServletResponse response) {
		String sw_lat = request.getParameter("sw_lat");
		String sw_lng = request.getParameter("sw_lng");
		String ne_lat = request.getParameter("ne_lat");
		String ne_lng = request.getParameter("ne_lng");
		String level = request.getParameter("level");
		//1.获取当前登录用户所有的车辆信息，过滤权限
		VehicleViewVO[] vehicleViewVOs = this.getService().getVehicleViewVOsByUserAndKeyWord(WebUtils.getLoginInfo().getPk_user(),null);
		if(vehicleViewVOs == null || vehicleViewVOs.length == 0){
			return null;
		}
		String[] gps_ids = new String[vehicleViewVOs.length];
		int index = 0;
		for(VehicleViewVO vehicleViewVO : vehicleViewVOs){
			if(StringUtils.isNotBlank(vehicleViewVO.getGps_id())){
				gps_ids[index] = vehicleViewVO.getGps_id();
				index++;
			}
		}
		if( gps_ids.length == 0 ){
			return null;
		}
		List<TrackVO> trackVOs = this.getService().getLBSByGps_ids(gps_ids);
		if(trackVOs == null || trackVOs.size() == 0){
			logger.info("没有从LBS里获取到数据，请保证接口和数据正确性！");
			return null;
		}
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		double _ne_lng = (ne_lng == null ? 142.647637 : Double.parseDouble(ne_lng));
		double _sw_lng = (ne_lng == null ? 76.123012 : Double.parseDouble(sw_lng));
		double _ne_lat = (ne_lng == null ? 50.021039 : Double.parseDouble(ne_lat));
		double _sw_lat = (ne_lng == null ? 18.436794 : Double.parseDouble(sw_lat));
		for(TrackVO trackVO : trackVOs){
			//先将不符合条件的数据过滤掉
			double longitude = 0;
			double latitude = 0;
			try {
				longitude = Double.parseDouble(trackVO.getLongitude());
				latitude = Double.parseDouble(trackVO.getLatitude());
			} catch (Exception e) {
				logger.info("经纬度获取失败：lng："+trackVO.getLongitude()+",lat"+trackVO.getLatitude());
				continue;
			}
			
			if(longitude <= _ne_lng && longitude >= _sw_lng
					&& latitude <= _ne_lat && latitude >= _sw_lat){
				for(VehicleViewVO vehicleViewVO : vehicleViewVOs){
					if(vehicleViewVO.getGps_id().equals(trackVO.getGpsid())){
						//组织信息
						Map<String,Object> retMap = new HashMap<String, Object>();
						retMap.put("equipcode", vehicleViewVO.getCarno());
						retMap.put("photo", vehicleViewVO.getPhoto() == null ? "car":vehicleViewVO.getPhoto());
						retMap.put("speed", trackVO.getSpeed());
						retMap.put("icon", trackVO.getIcon());
						retMap.put("speed_status", trackVO.getSpeed_status());
						retMap.put("road_name", trackVO.getRoad_name() == null ? "&nbsp;":trackVO.getRoad_name());
						retMap.put("place_name", trackVO.getPlace_name() == null ? "&nbsp;":trackVO.getPlace_name());
						retMap.put("carrier", vehicleViewVO.getCarr_name() == null ? "&nbsp;":vehicleViewVO.getCarr_name());
						retMap.put("driver", vehicleViewVO.getDriver_name() == null ? "&nbsp;":vehicleViewVO.getDriver_name());
						retMap.put("mobile", vehicleViewVO.getDriver_mobile() == null ? "&nbsp;":vehicleViewVO.getDriver_mobile());
						retMap.put("customs", vehicleViewVO.getCustoms());
						retMap.put("dangerous_veh", vehicleViewVO.getDangerous_veh());
						retMap.put("tonnage", vehicleViewVO.getTonnage());
						retMap.put("equip_type", vehicleViewVO.getCar_type());
						retMap.put("equip_status", vehicleViewVO.getCar_status());
						retMap.put("ent_status", vehicleViewVO.getEnt_status());
						retMap.put("distance", trackVO.getDistance());
						retMap.put("gps_time", trackVO.getGps_time());
						retMap.put("latitude", trackVO.getLatitude());
						retMap.put("longitude", trackVO.getLongitude());
						retMap.put("memo", trackVO.getMemo());
						retList.add(retMap);
						break;
					}
				}
			}
		}
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("info", retList);
		if(StringUtils.isBlank(level)){
			//初次传入level为空
			if(retList.size() >= 20){
				List<Map<String, Object>> roughInfoList = convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, retList,split);
				jsonMap.put("roughInfo", roughInfoList);
			}
		}else{
			if(retList.size() >= 20 && Integer.parseInt(level) < 11 ){
				List<Map<String, Object>> roughInfoList = convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, retList,split);
				jsonMap.put("roughInfo", roughInfoList);
			}
		}
		return JacksonUtils.writeValueAsString(jsonMap);
	}
	
	@RequestMapping(value = "/loadEquipDataByLock.json")
	@ResponseBody
	public String loadEquipDataByLock(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String equipcodes = request.getParameter("equipcodes");
		if(StringUtils.isBlank(equipcodes)){
			return null;
		}
		String[] equipcodeArrs = (new String(equipcodes.getBytes("ISO8859-1"), "UTF-8")).split(",");
		if(equipcodeArrs == null || equipcodeArrs.length == 0){
			return null;
		}
		//1.获取当前登录用户所有的车辆信息，过滤权限
		VehicleViewVO[] vehicleViewVOs = this.getService().getVehicleViewVOsByEquipcodes(equipcodeArrs);
		if(vehicleViewVOs == null || vehicleViewVOs.length == 0){
			return null;
		}
		String[] gps_ids = new String[vehicleViewVOs.length];
		int index = 0;
		for(VehicleViewVO vehicleViewVO : vehicleViewVOs){
			if(StringUtils.isNotBlank(vehicleViewVO.getGps_id())){
				gps_ids[index] = vehicleViewVO.getGps_id();
				index++;
			}
		}
		if( gps_ids.length == 0 ){
			return null;
		}
		List<TrackVO> trackVOs = this.getService().getLBSByGps_ids(gps_ids);
		if(trackVOs == null || trackVOs.size() == 0){
			logger.info("没有从LBS里获取到数据，请保证接口和数据正确性！");
			return null;
		}
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		for(TrackVO trackVO : trackVOs){
			for(VehicleViewVO vehicleViewVO : vehicleViewVOs){
				if(vehicleViewVO.getGps_id().equals(trackVO.getGpsid())){
					//组织信息
					Map<String,Object> retMap = new HashMap<String, Object>();
					retMap.put("equipcode", vehicleViewVO.getCarno());
					retMap.put("photo", vehicleViewVO.getPhoto() == null ? "car":vehicleViewVO.getPhoto());
					retMap.put("speed", trackVO.getSpeed());
					retMap.put("icon", trackVO.getIcon());
					retMap.put("speed_status", trackVO.getSpeed_status());
					retMap.put("road_name", trackVO.getRoad_name() == null ? "&nbsp;":trackVO.getRoad_name());
					retMap.put("place_name", trackVO.getPlace_name() == null ? "&nbsp;":trackVO.getPlace_name());
					retMap.put("carrier", vehicleViewVO.getCarr_name() == null ? "&nbsp;":vehicleViewVO.getCarr_name());
					retMap.put("driver", vehicleViewVO.getDriver_name() == null ? "&nbsp;":vehicleViewVO.getDriver_name());
					retMap.put("mobile", vehicleViewVO.getDriver_mobile() == null ? "&nbsp;":vehicleViewVO.getDriver_mobile());
					retMap.put("customs", vehicleViewVO.getCustoms());
					retMap.put("dangerous_veh", vehicleViewVO.getDangerous_veh());
					retMap.put("tonnage", vehicleViewVO.getTonnage());
					retMap.put("equip_type", vehicleViewVO.getCar_type());
					retMap.put("equip_status", vehicleViewVO.getCar_status());
					retMap.put("ent_status", vehicleViewVO.getEnt_status());
					retMap.put("distance", trackVO.getDistance());
					retMap.put("gps_time", trackVO.getGps_time());
					retMap.put("latitude", trackVO.getLatitude());
					retMap.put("longitude", trackVO.getLongitude());
					retMap.put("memo", trackVO.getMemo());
					retList.add(retMap);
					break;
				}
			}
		}
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("info", retList);
		return JacksonUtils.writeValueAsString(jsonMap);
	}
	

	
	public List<Map<String, Object>> convertToRoughInfoEquipData(double _ne_lng,double _sw_lng,double _ne_lat,double _sw_lat,List<Map<String, Object>> retList,Boolean split){
		if(retList == null || retList.size() == 0){
			return null;
		} 
		if(split){
			return convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, retList);
		}
		List<Map<String, Object>> northwest = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> midwest = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> southwest = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> midnorth = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> mid = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> midsouth = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> northeast = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> mideast = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> southeast = new ArrayList<Map<String,Object>>();
		//对查询到的结果进行分组
		for(Map<String, Object> retMap : retList){
			double longitude = 0;
			double latitude = 0;	
			try {
				//检查数据是否合格
				longitude =  Double.parseDouble(retMap.get("longitude").toString());
				latitude =  Double.parseDouble(retMap.get("latitude").toString());
			} catch (Exception e) {
				logger.info("设备："+ retMap.get("equipcode") + "GPS信息有误！");
				continue;
			}
			//东北角
			if(longitude <= _ne_lng && longitude > ((_ne_lng - _sw_lng)/3*2 + _sw_lng)
					&& latitude <= _ne_lat && latitude >(_ne_lat - _sw_lat)/3*2 + _sw_lat){
				northeast.add(retMap);
			}
			//中东部
			if(longitude <= _ne_lng && longitude > ((_ne_lng - _sw_lng)/3*2 + _sw_lng)
					&& latitude <= ((_ne_lat - _sw_lat)/3*2 + _sw_lat) && latitude > ((_ne_lat - _sw_lat)/3 + _sw_lat)){
				mideast.add(retMap);
			}
			//东南角
			if(longitude <= _ne_lng && longitude > ((_ne_lng - _sw_lng)/3*2 + _sw_lng)
					&& latitude > _sw_lat && latitude <= ((_ne_lat - _sw_lat)/3 + _sw_lat)){
				southeast.add(retMap);
			}
			//西南角
			if(longitude > _sw_lng && longitude <= ((_ne_lng - _sw_lng)/3 + _sw_lng)
					&& latitude > _sw_lat && latitude <= ((_ne_lat - _sw_lat)/3 + _sw_lat)){
				southwest.add(retMap);
			}
			//中西部
			if(longitude > _sw_lng && longitude <= ((_ne_lng - _sw_lng)/3 + _sw_lng)
					&& latitude >((_ne_lat - _sw_lat)/3 + _sw_lat) && latitude <= ((_ne_lat - _sw_lat)/3*2 + _sw_lat)){
				midwest.add(retMap);
			}
			//西北角
			if(longitude > _sw_lng && longitude <= ((_ne_lng - _sw_lng)/3 + _sw_lng)
					&& latitude <= _ne_lat && latitude > ((_ne_lat - _sw_lat)/3*2 + _sw_lat)){
				northwest.add(retMap);
			}
			//中北部
			if(longitude > ((_ne_lng - _sw_lng)/3 + _sw_lng) && longitude <= ((_ne_lng - _sw_lng)/3*2 + _sw_lng)
					&& latitude <= _ne_lat && latitude > ((_ne_lat - _sw_lat)/3*2 + _sw_lat)){
				midnorth.add(retMap);
			}
			//中部
			if(longitude > ((_ne_lng - _sw_lng)/3 + _sw_lng) && longitude <= ((_ne_lng - _sw_lng)/3*2 + _sw_lng)
					&& latitude <= ((_ne_lat - _sw_lat)/3*2 + _sw_lat) && latitude > ((_ne_lat - _sw_lat)/3 + _sw_lat)){
				mid.add(retMap);
			}
			//中南部
			if(longitude > ((_ne_lng - _sw_lng)/3 + _sw_lng) && longitude <= ((_ne_lng - _sw_lng)/3*2 + _sw_lng)
					&& latitude <= ((_ne_lat - _sw_lat)/3*2 + _sw_lat) && latitude > _sw_lat){
				midsouth.add(retMap);
			}
		}
		//将结果放在同一个集合里，方便一起处理
		List<List<Map<String, Object>>> allList = new ArrayList<List<Map<String,Object>>>();
		
		allList.add(northwest);
		allList.add(midwest);
		allList.add(southwest);
		allList.add(midnorth);
		allList.add(mid);
		allList.add(midsouth);
		allList.add(northeast);
		allList.add(mideast);
		allList.add(southeast);
		
		//设置区域中心点和返回信息
		List<Map<String, Object>> roughInfoList = new ArrayList<Map<String,Object>>();
		for(List<Map<String, Object>> list : allList){
			if(list.size() > 0){
				double lat = 0;
				double lng = 0;
				for(Map<String, Object> retMap : list){
					lat += Double.parseDouble(retMap.get("latitude").toString());
					lng += Double.parseDouble(retMap.get("longitude").toString());
				}
				//中心点坐标
				lat = lat/list.size();
				lng = lng/list.size();
				Map<String, Object> roughInfo = new HashMap<String, Object>();
				roughInfo.put("num", list.size()+"");
				roughInfo.put("point_lat", lat+"");
				roughInfo.put("point_lng", lng+"");
				roughInfoList.add(roughInfo);
			}
		}
		return roughInfoList;
	} 
	
	public List<Map<String, Object>> convertToRoughInfoEquipData(double _ne_lng,double _sw_lng,double _ne_lat,double _sw_lat,List<Map<String, Object>> retList){
		if(retList == null || retList.size() == 0){
			return null;
		} 
		List<Map<String, Object>> northwest = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> southwest = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> northeast = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> southeast = new ArrayList<Map<String,Object>>();
		//对查询到的结果进行分组
		for(Map<String, Object> retMap : retList){
			double longitude = 0;
			double latitude = 0;	
			try {
				//检查数据是否合格
				longitude =  Double.parseDouble(retMap.get("longitude").toString());
				latitude =  Double.parseDouble(retMap.get("latitude").toString());
			} catch (Exception e) {
				logger.info("设备："+ retMap.get("equipcode") + "GPS信息有误！");
				continue;
			}
			//东北角
			if(longitude <= _ne_lng && longitude > (_ne_lng + _sw_lng)/2
					&& latitude <= _ne_lat && latitude >(_ne_lat + _sw_lat)/2){
				northeast.add(retMap);
			}
			//东南角
			if(longitude <= _ne_lng && longitude > (_ne_lng + _sw_lng)/2
					&& latitude > _sw_lat && latitude <=(_ne_lat + _sw_lat)/2){
				southeast.add(retMap);
			}
			//西南角
			if(longitude > _sw_lng && longitude <= (_ne_lng + _sw_lng)/2
					&& latitude > _sw_lat && latitude <=(_ne_lat + _sw_lat)/2){
				southwest.add(retMap);
			}
			//西北角
			if(longitude > _sw_lng && longitude <= (_ne_lng + _sw_lng)/2
					&& latitude <= _ne_lat && latitude >(_ne_lat + _sw_lat)/2){
				northwest.add(retMap);
			}
		}
		//将结果放在同一个集合里，方便一起处理
		List<List<Map<String, Object>>> allList = new ArrayList<List<Map<String,Object>>>();
		allList.add(northeast);
		allList.add(southeast);
		allList.add(southwest);
		allList.add(northwest);
		//设置区域中心点和返回信息
		List<Map<String, Object>> roughInfoList = new ArrayList<Map<String,Object>>();
		for(List<Map<String, Object>> list : allList){
			if(list.size() > 0){
				double lat = 0;
				double lng = 0;
				for(Map<String, Object> retMap : list){
					lat += Double.parseDouble(retMap.get("latitude").toString());
					lng += Double.parseDouble(retMap.get("longitude").toString());
				}
				//中心点坐标
				lat = lat/list.size();
				lng = lng/list.size();
				Map<String, Object> roughInfo = new HashMap<String, Object>();
				roughInfo.put("num", list.size()+"");
				roughInfo.put("point_lat", lat+"");
				roughInfo.put("point_lng", lng+"");
				roughInfoList.add(roughInfo);
			}
		}
		return roughInfoList;
	} 
	
	@RequestMapping(value = "/loadEquipLotsOrdersInfo.json")
	@ResponseBody
	public Map<String, Object> loadEquipInfo(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		String equipcode = request.getParameter("equipcode");
		if(StringUtils.isBlank(equipcode)){
			return null;
		}
		equipcode = new String(equipcode.getBytes("ISO8859-1"), "UTF-8");
		return this.getService().loadEquipInfo(equipcode);
	} 
	
	@RequestMapping(value = "/getRefreshInterval.json")
	@ResponseBody
	public Integer getRefreshInterval(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		return this.getService().getRefreshInterval();
	} 
	
}
