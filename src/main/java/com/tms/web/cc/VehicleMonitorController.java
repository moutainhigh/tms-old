package com.tms.web.cc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.json.JacksonUtils;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.service.cc.VehicleMonitorService;
import com.tms.vo.te.EntLotTrackingBVO;

/**
 * 冷链-车辆监控
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:11:10
 */
@Controller
@RequestMapping(value = "/cc/vm")
public class VehicleMonitorController extends AbsToftController {

	@Autowired
	private VehicleMonitorService vehicleMonitorService;

	public VehicleMonitorService getService() {
		return vehicleMonitorService;
	}
	
	
	/**
	 * 进入首页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response){
		ParamVO paramVO = getParamVO(request);
		return new ModelAndView(getFunHelpName(paramVO.getFunCode()));
	}
	
	
	
	@RequestMapping(value = "/loadVehicleData.json")
	@ResponseBody
	public String loadVehicleData(HttpServletRequest request, HttpServletResponse response) {
		String sw_lat = request.getParameter("sw_lat");
		String sw_lng = request.getParameter("sw_lng");
		String ne_lat = request.getParameter("ne_lat");
		String ne_lng = request.getParameter("ne_lng");
		String level = request.getParameter("level");
		
		double _ne_lng = (ne_lng == null ? 142.647637 : Double.parseDouble(ne_lng));
		double _sw_lng = (ne_lng == null ? 76.123012 : Double.parseDouble(sw_lng));
		double _ne_lat = (ne_lng == null ? 50.021039 : Double.parseDouble(ne_lat));
		double _sw_lat = (ne_lng == null ? 18.436794 : Double.parseDouble(sw_lat));
		String sql = "SELECT ent_lot_track_rank.* "
							+ " FROM (	SELECT ts_ent_lot_track_b.* "
							+ " ,Row_Number() OVER ( "
							+ " partition by ts_ent_lot_track_b.carno "
							+ " ORDER BY ts_ent_lot_track_b.track_time DESC) rank "
							+ " FROM ts_ent_lot_track_b With (nolock) "
						  	+ " WHERE isnull(ts_ent_lot_track_b.dr,0)=0 AND isnull (ts_ent_lot_track_b.carno,'')<>'' "
					+ " )ent_lot_track_rank  "
					+ " WHERE ent_lot_track_rank.rank=1 and longitude <? and longitude >? and latitude <? and latitude >? AND track_time>convert(varchar,getdate()-60,120)"
					+ " and " + CorpHelper.getCurrentCorpWithChildren();
		//1.获取车辆信息
		List<EntLotTrackingBVO> lotTrackingBVOs = NWDao.getInstance().queryForList(sql, EntLotTrackingBVO.class, _ne_lng,_sw_lng,_ne_lat,_sw_lat);
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("info", lotTrackingBVOs);
		if(StringUtils.isBlank(level)){
			//初次传入level为空
			if(lotTrackingBVOs.size() >= 20){
				List<Map<String, Object>> roughInfoList = convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, lotTrackingBVOs);
				jsonMap.put("roughInfo", roughInfoList);
			}
		}else{
			if(lotTrackingBVOs.size() >= 20 && Integer.parseInt(level) < 11 ){
				List<Map<String, Object>> roughInfoList = convertToRoughInfoEquipData(_ne_lng, _sw_lng, _ne_lat, _sw_lat, lotTrackingBVOs);
				jsonMap.put("roughInfo", roughInfoList);
			}
		}
		return JacksonUtils.writeValueAsString(jsonMap);
	}
	
	public List<Map<String, Object>> convertToRoughInfoEquipData(double _ne_lng,double _sw_lng,double _ne_lat,double _sw_lat,List<EntLotTrackingBVO> lotTrackingBVOs){
		if(lotTrackingBVOs == null || lotTrackingBVOs.size() == 0){
			return null;
		} 
		List<EntLotTrackingBVO> northwest = new ArrayList<EntLotTrackingBVO>();
		List<EntLotTrackingBVO> southwest = new ArrayList<EntLotTrackingBVO>();
		List<EntLotTrackingBVO> northeast = new ArrayList<EntLotTrackingBVO>();
		List<EntLotTrackingBVO> southeast = new ArrayList<EntLotTrackingBVO>();
		//对查询到的结果进行分组
		for(EntLotTrackingBVO lotTrackingBVO : lotTrackingBVOs){
			double longitude = 0;
			double latitude = 0;	
			try {
				//检查数据是否合格
				longitude = StringUtils.isBlank(lotTrackingBVO.getLongitude()) ? 0 : Double.parseDouble(lotTrackingBVO.getLongitude());
				latitude = StringUtils.isBlank(lotTrackingBVO.getLatitude()) ? 0 : Double.parseDouble(lotTrackingBVO.getLatitude());
			} catch (Exception e) {
				logger.info("设备："+ lotTrackingBVO.getCarno() + "GPS信息有误！");
				continue;
			}
			//东北角
			if(longitude <= _ne_lng && longitude > (_ne_lng + _sw_lng)/2
					&& latitude <= _ne_lat && latitude >(_ne_lat + _sw_lat)/2){
				northeast.add(lotTrackingBVO);
			}
			//东南角
			if(longitude <= _ne_lng && longitude > (_ne_lng + _sw_lng)/2
					&& latitude > _sw_lat && latitude <=(_ne_lat + _sw_lat)/2){
				southeast.add(lotTrackingBVO);
			}
			//西南角
			if(longitude > _sw_lng && longitude <= (_ne_lng + _sw_lng)/2
					&& latitude > _sw_lat && latitude <=(_ne_lat + _sw_lat)/2){
				southwest.add(lotTrackingBVO);
			}
			//西北角
			if(longitude > _sw_lng && longitude <= (_ne_lng + _sw_lng)/2
					&& latitude <= _ne_lat && latitude >(_ne_lat + _sw_lat)/2){
				northwest.add(lotTrackingBVO);
			}
		}
		//将结果放在同一个集合里，方便一起处理
		List<List<EntLotTrackingBVO>> allList = new ArrayList<List<EntLotTrackingBVO>>();
		allList.add(northeast);
		allList.add(southeast);
		allList.add(southwest);
		allList.add(northwest);
		//设置区域中心点和返回信息
		List<Map<String, Object>> roughInfoList = new ArrayList<Map<String,Object>>();
		for(List<EntLotTrackingBVO> list : allList){
			if(list.size() > 0){
				double lat = 0;
				double lng = 0;
				for(EntLotTrackingBVO lotTrackingBVO : list){
					lat += StringUtils.isBlank(lotTrackingBVO.getLatitude()) ? 0 : Double.parseDouble(lotTrackingBVO.getLatitude());
					lng += StringUtils.isBlank(lotTrackingBVO.getLongitude()) ? 0 : Double.parseDouble(lotTrackingBVO.getLongitude());
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
	
	@RequestMapping(value = "/loadQueryVehicleData.json")
	@ResponseBody
	public String loadQueryEquipData(HttpServletRequest request, HttpServletResponse response)throws UnsupportedEncodingException {
		String keyword = request.getParameter("keyword");
		if(StringUtils.isBlank(keyword)){
			return null;
		}
		keyword = new String(keyword.getBytes("ISO8859-1"), "UTF-8");
		//1.获取车辆信息
		
		String sql = "SELECT ent_lot_track_rank.* "
				+ " FROM (	SELECT ts_ent_lot_track_b.* "
				+ " ,Row_Number() OVER ( "
				+ " partition by ts_ent_lot_track_b.carno "
				+ " ORDER BY ts_ent_lot_track_b.track_time DESC) rank "
				+ " FROM ts_ent_lot_track_b With (nolock) "
			  	+ " WHERE isnull(ts_ent_lot_track_b.dr,0)=0 AND isnull (ts_ent_lot_track_b.carno,'')<>'' "
		+ " )ent_lot_track_rank  "
		+ " WHERE ent_lot_track_rank.rank=1 and carno =? AND track_time>convert(varchar,getdate()-60,120)"
		+ " and " + CorpHelper.getCurrentCorpWithChildren();
		List<EntLotTrackingBVO> lotTrackingBVOs = NWDao.getInstance().queryForList(sql, EntLotTrackingBVO.class, keyword);
		return JacksonUtils.writeValueAsString(lotTrackingBVOs);
	}
	
	@RequestMapping(value = "/loadVehicleDataByLock.json")
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
		String sql = "SELECT ent_lot_track_rank.* "
				+ " FROM (	SELECT ts_ent_lot_track_b.* "
				+ " ,Row_Number() OVER ( "
				+ " partition by ts_ent_lot_track_b.carno "
				+ " ORDER BY ts_ent_lot_track_b.track_time DESC) rank "
				+ " FROM ts_ent_lot_track_b With (nolock) "
			  	+ " WHERE isnull(ts_ent_lot_track_b.dr,0)=0 AND isnull (ts_ent_lot_track_b.carno,'')<>'' "
		+ " )ent_lot_track_rank  "
		+ " WHERE ent_lot_track_rank.rank=1 AND track_time>convert(varchar,getdate()-60,120)"
		+ " and carno in "+ NWUtils.buildConditionString(equipcodeArrs) +" and " + CorpHelper.getCurrentCorpWithChildren();
		
		List<EntLotTrackingBVO> lotTrackingBVOs = NWDao.getInstance().queryForList(sql, EntLotTrackingBVO.class);
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("info", lotTrackingBVOs);
		return JacksonUtils.writeValueAsString(jsonMap);
		
	}
	
	@RequestMapping(value = "/loadVehicleLotsOrdersInfo.json")
	@ResponseBody
	public Map<String, Object> loadEquipInfo(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		String equipcode = request.getParameter("equipcode");
		if(StringUtils.isBlank(equipcode)){
			return null;
		}
		equipcode = new String(equipcode.getBytes("ISO8859-1"), "UTF-8");
		return this.getService().loadEquipInfo(equipcode);
	} 

}
