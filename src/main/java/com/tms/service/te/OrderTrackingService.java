package com.tms.service.te;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;
import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.inv.InvView;

public interface OrderTrackingService extends IBillService {
	
	public List<TrackVO> getLBSByGps_ids(String[] gps_ids);
	
	public List<InvView> getOrdersByUserAndKeyWord(String keyword);
	
	//public List<TrackVO> loadTrailsByVbillno(List<String> vbillnos);
	
	public List<Map<String,Object>> convertToRoughInfoEquipData(double _ne_lng,double _sw_lng,double _ne_lat,double _sw_lat,List<Map<String, Object>> retList);
}
