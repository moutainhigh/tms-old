package com.tms.service.cc;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;
import org.nw.service.IToftService;

import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.te.VehicleViewVO;

public interface VehicleMonitorService extends IBillService {
	
	public List<TrackVO> getLBSByGps_ids(String[] gps_ids);
	
	public VehicleViewVO[] getVehicleViewVOsByUserAndKeyWord(String pk_user,String keyword);
	
	public VehicleViewVO[] getVehicleViewVOsByEquipcodes(String[] equipcodeArrs);
	
	public Map<String, Object> loadEquipInfo(String equipcode);
	
	public Integer getRefreshInterval();
}
