package com.tms.service.cc;



import org.nw.service.IBillService;

import com.tms.vo.te.EntLotTrackingBVO;

public interface TempMonitorService extends IBillService {
	
	EntLotTrackingBVO[] getAjaxData(String[] ids);
	
}
