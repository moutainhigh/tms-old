package com.tms.service.cc.impl;


import java.util.List;
import java.util.Map;

import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cc.VehicleMonitorService;
import com.tms.service.job.lbs.TrackVO;
import com.tms.vo.te.VehicleViewVO;

@Service
public class VehicleMonitorServiceImpl extends TMSAbsBillServiceImpl implements VehicleMonitorService {

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	public List<TrackVO> getLBSByGps_ids(String[] gps_ids) {
		return null;
	}

	public VehicleViewVO[] getVehicleViewVOsByUserAndKeyWord(String pk_user, String keyword) {
		return null;
	}

	public VehicleViewVO[] getVehicleViewVOsByEquipcodes(String[] equipcodeArrs) {
		return null;
	}

	public Map<String, Object> loadEquipInfo(String equipcode) {
		return null;
	}

	public Integer getRefreshInterval() {
		return null;
	}

	public String getBillType() {
		return null;
	}


	
	
	

	
}
