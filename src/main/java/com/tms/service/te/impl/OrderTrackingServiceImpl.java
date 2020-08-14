package com.tms.service.te.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.utils.CorpHelper;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.OrderTrackingService;
import com.tms.vo.inv.InvView;


@Service()
public class OrderTrackingServiceImpl extends TMSAbsBillServiceImpl implements OrderTrackingService {

	public String getBillType() {
		return null;
	}

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<TrackVO> getLBSByGps_ids(String[] gps_ids) {
		if (gps_ids == null || gps_ids.length == 0) {
			return null;
		}
		try {
			RootVO rootVO = LBSUtils.getCurrentTrackVO(gps_ids);
			if (rootVO != null) {
				return rootVO.getDataset();
			}
		} catch (Exception e) {
			logger.info("请求LBS数据出错");
		}
		return null;
	}

	public List<InvView> getOrdersByUserAndKeyWord(String keyword) {
		if(WebUtils.getLoginInfo() == null){
			return null;
		}
		//只抓取一个月之类的数据
		String startDate = (new UFDateTime(new Date())).getDateBefore(30).toString();
		String endDate = (new UFDateTime(new Date())).toString();
		String sql = "select * from ts_inv_view where ";
		if(StringUtils.isNotBlank(keyword)){
			sql += " (vbillno like '%"+ keyword +"%' or cust_orderno like '%"+ keyword +"%' or orderno like '%"+ keyword +"%')"; 
		}else{
			sql += " 1=1 ";
		}
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		sql += " and " + corpCond;
		//客户只能看到自己的数据
		if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_customer())){
			sql = " and ( pk_customer ='"+WebUtils.getLoginInfo().getPk_customer()+"'"; 
		}
		sql += " and create_time BETWEEN ? and ? ";
				
		List<InvView> invViews = NWDao.getInstance().queryForList(sql, InvView.class,startDate,endDate);
		return invViews;
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
	
	

	
}
