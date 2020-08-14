package com.tms.service.inv;

import java.util.Map;

import org.nw.service.IBillService;

import com.tms.vo.inv.InvPackRevBVO;
import com.tms.vo.inv.InvRevBVO;

/**
 * 订单修订
 * @author XIA
 *
 */
public interface InvoiceRevService extends IBillService {
	
	public Map<String,Object> examine(InvRevBVO[] invRevBVOs,InvPackRevBVO[] invPackRevBVOs);


}
