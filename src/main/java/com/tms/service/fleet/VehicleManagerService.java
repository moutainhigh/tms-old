package com.tms.service.fleet;

import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;

import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.DriverVO;

public interface VehicleManagerService extends IBillService {
	
	public Map<String, Object> commit(ParamVO paramVO);

	public Map<String, Object> uncommit(ParamVO paramVO);
	
	public Map<String, Object> vmcheck(String id, Integer choice, Integer reason_type, String memo);
	
	public Map<String, Object> vmrecheck(String id);
	
	public CarVO[] getCarno();
	
	public DriverVO[] getDriver();
	
	public AddressVO[] getAddr();
	
	public Map<String, Object> vmsend(String id, String carno, String main_driver, String deputy_drive, String memo);
	
	public Map<String, Object> vmdispatch(String id,String watch,String gps,String fule,String time, String addr,String memo);

	public Map<String, Object> vmreturn(String id,String watch,String gps,String fule,String time, String addr,String memo);
}
