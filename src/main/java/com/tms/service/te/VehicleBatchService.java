package com.tms.service.te;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;

import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.te.VehiclePayVO;

public interface VehicleBatchService extends IBillService {
	
	public List<Map<String, Object>> getPackRecord(String id);

	public Map<String,Object> authentication(String lot, String card_msg);
	
	public List<Map<String,Object>> loadPayDetail(String lot);
	
	public void saveVBLotPay(List<PayDetailBVO> detailBVOs,String lot);
	
	public List<Map<String,Object>> loadVehiclePay(String lot);
	
	public void saveVehiclePay(List<VehiclePayVO> vehiclePayVOs);
	
	public Map<String,Object> getKilometreAndDays(String lot);
}
