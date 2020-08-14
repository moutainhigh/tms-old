package com.tms.web.te;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.BillStatus;
import com.tms.constants.FunConst;
import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.OrderTrackingService;
import com.tms.vo.inv.InvView;
import com.tms.vo.inv.InvoiceVO;


@Controller
@RequestMapping(value = "/te/ot")
public class OrderTrackingController extends AbsBillController {

	@Autowired
	private OrderTrackingService orderTrackingService;

	public OrderTrackingService getService() {
		return orderTrackingService;
	}
	
	
	/**
	 * @author XIA
	 *
	 * 这个方法以订单为维度，显示每个订单的详细信息
	 */
	@RequestMapping(value = "/loadQueryOrderData.json")
	@ResponseBody
	public String loadQueryOrderData(HttpServletRequest request, HttpServletResponse response)throws UnsupportedEncodingException {
		String keyword = request.getParameter("keyword");
		if(StringUtils.isBlank(keyword)){
			return null;
		}
		keyword = new String(keyword.getBytes("ISO8859-1"), "UTF-8");
		// 1.获取当前登录用户所有的订单信息，过滤权限
		List<InvView> invViews = this.getService().getOrdersByUserAndKeyWord(keyword);
		if (invViews == null || invViews.size() == 0) {
			return null;
		}
		List<String> gps_ids = new ArrayList<String>();
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		//对发货单进行分组，未提货的订单和到货的订单，使用起始地的经纬度，运输中的订单使用运力信息的经纬度。
		for(InvView invView : invViews){
			Map<String,Object> map = new HashMap<String, Object>();
			String[] attrs = invView.getAttributeNames();
			for(String key : attrs) {
				map.put(key, invView.getAttributeValue(key));
			}
			retList.add(map);
			if(invView.getVbillstatus()!= null 
					&& invView.getVbillstatus().equals("提货")){
				//对于已提货的单子，需要去LBS里查找路线信息
				gps_ids.add(invView.getGps_id());
			}
		}
		List<TrackVO> trackVOs = null;
		if(gps_ids != null && gps_ids.size() > 0){
			trackVOs =  this.getService().getLBSByGps_ids(gps_ids.toArray(new String[gps_ids.size()]));
		}
		//将跟踪信息也写到结果里面 组织数据
		if(trackVOs != null && trackVOs.size() > 0){
			for(Map<String,Object> map : retList){
				boolean flag = true;
				for(TrackVO trackVO : trackVOs){
					if(trackVO.getGpsid().equals(map.get("gps_id"))){
						map.put("longitude", trackVO.getLongitude());
						map.put("latitude", trackVO.getLatitude());
						map.put("trackVO", trackVO);
						flag = false;
						break;
					}
				}
				if (flag) {
					if(String.valueOf(map.get("vbillstatus")).equals("新建")
							|| String.valueOf(map.get("vbillstatus")).equals("确认")
							|| String.valueOf(map.get("vbillstatus")).equals("提货")){
						map.put("longitude", map.get("deli_longitude"));
						map.put("latitude", map.get("deli_latitude"));
					}
					if(String.valueOf(map.get("vbillstatus")).equals("到货")
							|| String.valueOf(map.get("vbillstatus")).equals("签收")
							|| String.valueOf(map.get("vbillstatus")).equals("回单")){
						map.put("longitude", map.get("arri_longitude"));
						map.put("latitude", map.get("arri_latitude"));
					}
				}
			}
		}else{
			//可能存在ＬＢＳ没有任何记录的情况，也要设置经纬度
			for(Map<String,Object> map : retList){
				if(String.valueOf(map.get("vbillstatus")).equals("新建")
						|| String.valueOf(map.get("vbillstatus")).equals("确认")
						|| String.valueOf(map.get("vbillstatus")).equals("提货")){
					map.put("longitude", map.get("deli_longitude"));
					map.put("latitude", map.get("deli_latitude"));
				}
				if(String.valueOf(map.get("vbillstatus")).equals("到货")
						|| String.valueOf(map.get("vbillstatus")).equals("签收")
						|| String.valueOf(map.get("vbillstatus")).equals("回单")){
					map.put("longitude", map.get("arri_longitude"));
					map.put("latitude", map.get("arri_latitude"));
				}
			}
		}
		
		return JacksonUtils.writeValueAsString(retList);
	}
	
	/**
	 * @author XIA
	 *
	 * 这个方法以车辆单为维度，显示每个车辆的详细信息和车辆上的订单信息
	 */
	@RequestMapping(value = "/loadOrderData.json")
	@ResponseBody
	public String loadOrderData(HttpServletRequest request, HttpServletResponse response) {
		String sw_lat = request.getParameter("sw_lat");
		String sw_lng = request.getParameter("sw_lng");
		String ne_lat = request.getParameter("ne_lat");
		String ne_lng = request.getParameter("ne_lng");
		String level = request.getParameter("level");
		//1.获取当前登录用户所有的订单信息，过滤权限
		List<InvView> invViews = this.getService().getOrdersByUserAndKeyWord(null);
		if(invViews == null || invViews.size() == 0){
			return null;
		}
		List<String> gps_ids = new ArrayList<String>();
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		//对发货单进行分组，未提货的订单和到货的订单，使用起始地的经纬度，运输中的订单使用运力信息的经纬度。
		for(InvView invView : invViews){
			Map<String,Object> map = new HashMap<String, Object>();
			String[] attrs = invView.getAttributeNames();
			for(String key : attrs) {
				map.put(key, invView.getAttributeValue(key));
			}
			retList.add(map);
			if(invView.getVbillstatus()!= null 
					&& invView.getVbillstatus().equals("提货")){
				//对于已提货的单子，需要去LBS里查找路线信息
				gps_ids.add(invView.getGps_id());
			}
		}
		List<TrackVO> trackVOs = null;
		if(gps_ids != null && gps_ids.size() > 0){
			trackVOs =  this.getService().getLBSByGps_ids(gps_ids.toArray(new String[gps_ids.size()]));
		}
		//将跟踪信息也写到结果里面 组织数据
		if(trackVOs != null && trackVOs.size() > 0){
			for(Map<String,Object> map : retList){
				boolean flag = true;
				for(TrackVO trackVO : trackVOs){
					if(trackVO.getGpsid().equals(map.get("gps_id"))){
						map.put("longitude", trackVO.getLongitude());
						map.put("latitude", trackVO.getLatitude());
						map.put("trackVO", trackVO);
						flag = false;
						break;
					}
				}
				if (flag) {
					if(String.valueOf(map.get("vbillstatus")).equals("新建")
							|| String.valueOf(map.get("vbillstatus")).equals("确认")
							|| String.valueOf(map.get("vbillstatus")).equals("提货")){
						map.put("longitude", map.get("deli_longitude"));
						map.put("latitude", map.get("deli_latitude"));
					}
					if(String.valueOf(map.get("vbillstatus")).equals("到货")
							|| String.valueOf(map.get("vbillstatus")).equals("签收")
							|| String.valueOf(map.get("vbillstatus")).equals("回单")){
						map.put("longitude", map.get("arri_longitude"));
						map.put("latitude", map.get("arri_latitude"));
					}
				}
			}
		}else{
			for(Map<String,Object> map : retList){
				if(String.valueOf(map.get("vbillstatus")).equals("新建")
						|| String.valueOf(map.get("vbillstatus")).equals("确认")
						|| String.valueOf(map.get("vbillstatus")).equals("提货")){
					map.put("longitude", map.get("deli_longitude"));
					map.put("latitude", map.get("deli_latitude"));
				}
				if(String.valueOf(map.get("vbillstatus")).equals("到货")
						|| String.valueOf(map.get("vbillstatus")).equals("签收")
						|| String.valueOf(map.get("vbillstatus")).equals("回单")){
					map.put("longitude", map.get("arri_longitude"));
					map.put("latitude", map.get("arri_latitude"));
				}
			}
		}
		

	
		double _ne_lng = (ne_lng == null ? 142.647637 : Double.parseDouble(ne_lng));
		double _sw_lng = (ne_lng == null ? 76.123012 : Double.parseDouble(sw_lng));
		double _ne_lat = (ne_lng == null ? 50.021039 : Double.parseDouble(ne_lat));
		double _sw_lat = (ne_lng == null ? 18.436794 : Double.parseDouble(sw_lat));
		List<Map<String,Object>> jsonList = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map : retList){
			//先将不符合条件的数据过滤掉
			double longitude = 0;
			double latitude = 0;
			try {
				longitude = Double.parseDouble(map.get("longitude") == null ? null : map.get("longitude").toString());
				latitude = Double.parseDouble(map.get("latitude") == null ? null : map.get("latitude").toString());
			} catch (Exception e) {
				logger.info("经纬度获取失败：lng:"+map.get("longitude")+",lat:"+map.get("latitude"));
				continue;
			}
			
			if((longitude <= _ne_lng && longitude >= _sw_lng
					&& latitude <= _ne_lat && latitude >= _sw_lat)){
				jsonList.add(map);
			}
		}
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("info", jsonList);
		if(StringUtils.isBlank(level)){
			//初次传入level为空
			if(jsonList.size() >= 20){
				List<Map<String, Object>> roughInfoList = this.getService().convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, jsonList);
				jsonMap.put("roughInfo", roughInfoList);
			}
		}else{
			if(jsonList.size() >= 20 && Integer.parseInt(level) < 11 ){
				List<Map<String, Object>> roughInfoList = this.getService().convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, jsonList);
				jsonMap.put("roughInfo", roughInfoList);
			}
		}
		return JacksonUtils.writeValueAsString(jsonMap);
	}
	
	@RequestMapping(value = "/loadEquipLotsOrdersInfo.json")
	@ResponseBody
	public Map<String, Object> loadEquipLotsOrdersInfo(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		String equipcode = request.getParameter("equipcode");
		if(StringUtils.isBlank(equipcode)){
			return null;
		}
		equipcode = new String(equipcode.getBytes("ISO8859-1"), "UTF-8");
		//return this.getService().loadEquipLotsOrdersInfo(equipcode);
		return null;
	} 
}