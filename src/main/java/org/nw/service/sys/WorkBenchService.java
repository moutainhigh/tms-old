package org.nw.service.sys;

import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.sys.WorkBenchVO;

import com.tms.vo.base.CarrierVO;

/**
 * 工作台接口
 * 
 * @author XIA
 * @date 2016-6-27 下午06:20:54
 */
public interface WorkBenchService extends IToftService {

	public List<WorkBenchVO> getWorkBenchVOs();
	
	public List<String> getWorkBenchPKsByRole(String pk_role);
	
	public Map<String, Object> getGoodsAmountData(String stratTime, String endTime, String timeId);
	
	public Map<String, Object> getDateAmountData();
	
	public Map<String, Object> getRouteAnalyze();
	
	public Map<String, Object> getCustAnalyze();
	
	public Map<String, Object> getChargeAmountData(String stratTime, String endTime, String timeId);
	
	public Map<String, Object> getChargeDateAmount();
	
	public Map<String, Object> getReceAnalyze();
	
	public Map<String, Object> getPayAnalyze();
	
	public List<CarrierVO> getCarriers(String keyword);
	
	public Map<String, Object> getKPIAmountData(String stratTime, String endTime, String timeId,String pk_carrier);
	
	public Map<String, Object> getKPIDateAmount();
	
	public Map<String, Object> getKPIRouteAnalyze();
	
	public Map<String, Object> getKPICarrAnalyze();

}
