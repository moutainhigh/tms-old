package com.tms.web.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.OriflameReportServiceImpl;

@Controller
@RequestMapping(value = "/report/oriflame")
public class OriflameReportController extends AbsReportController {

	@Autowired
	private OriflameReportServiceImpl oriflameReportServiceImpl;

	public OriflameReportServiceImpl getService() {
		return oriflameReportServiceImpl;
	}
	
	
	public Map<String,Object> loadchart1(){
		String sql = "SELECT pk_month,name,value FROM cus_ori_delivery_leadtime";
		Map<String,Object> ret = new HashMap<String, Object>();
		List<Map<String,Object>> result = NWDao.getInstance().queryForList(sql);
		if(result == null || result.size() == 0){
			return null;
		}
		for(Map<String,Object> map : result){
			List<String> legend = new ArrayList<String>();
			if(!legend.contains(String.valueOf(map.get("pk_month")))){
				legend.add(String.valueOf(map.get("pk_month")));
			}
		}
		//将result按照pk_month分组
		Map<String,List<Map<String,Object>>> groupMap = new HashMap<String, List<Map<String,Object>>>();
		for(Map<String,Object> map : result){
			String key = String.valueOf(map.get("pk_month"));
			List<Map<String,Object>> mapList = groupMap.get(key);
			if(mapList == null){
				mapList = new ArrayList<Map<String,Object>>();
				groupMap.put(key, mapList);
			}
			mapList.add(map);
		}
		List<String> legend = new ArrayList<String>();
		for(String key : groupMap.keySet()){
			List<Map<String,Object>> mapList = groupMap.get(key);
			if(mapList != null && mapList.size() > 0){
				for(Map<String,Object> map : mapList){
					legend.add(String.valueOf(map.get("pk_month")));
				}
			}
		}
		
		
		
		return null;
	}
	
	public Map<String,String> loadchart2(){
		return null;
	}
}
