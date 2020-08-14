package com.tms.service.rf;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;

import com.tms.vo.base.AddressVO;
import com.tms.vo.rf.EntLineBarBVO;
import com.tms.vo.rf.ScanVO;
import com.tms.vo.te.EntLineBVO;

public interface ScanService extends IBillService{

	public List<ScanVO> scan(String lot, Integer operate_type);
	
	public EntLineBarBVO[] getBarCodeVOS(String lot, String pk_ent_line_b);
	
	public List<EntLineBarBVO> saveBarCodes(EntLineBarBVO[] barBVOs,List<String> scanBarcodes, String pk_ent_line_b  ,String lot);
	
	public EntLineBVO getNextEntLineBVO(EntLineBVO entLineBVO);
	
	public Map<String, Object> insertTracking(ScanVO result,String type,AddressVO addrVO);
	
	public void updateNextEntLineBarBVO(Map<String, List<EntLineBarBVO>> groupMap, EntLineBVO[] entLineBVOs);
}


