package com.tms.web.inv;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.inv.InvoiceRevService;
import com.tms.vo.inv.InvPackRevBVO;
import com.tms.vo.inv.InvRevBVO;

/**
 * 订单修订
 * @author XIA
 *
 */
@Controller
@RequestMapping(value = "/inv/rev")
public class InvoiceRevController extends AbsBillController {

	@Autowired
	private InvoiceRevService invoiceRevService;
	
	@Override
	public InvoiceRevService getService() {
		return invoiceRevService;
	}

	@RequestMapping(value="/examine.json")
	@ResponseBody
	public Map<String, Object> examine(HttpServletRequest request, HttpServletResponse response){
		String ts_inv_revise_b = request.getParameter("ts_inv_revise_b");
		String ts_inv_pack_rev_b = request.getParameter("ts_inv_pack_rev_b");
		
		JsonNode ts_inv_revise_bs = JacksonUtils.readTree(ts_inv_revise_b);
		JsonNode ts_inv_pack_rev_bs = JacksonUtils.readTree(ts_inv_pack_rev_b);
		
		if((ts_inv_revise_bs == null || ts_inv_revise_bs.size() == 0)
				&& (ts_inv_pack_rev_bs == null || ts_inv_pack_rev_bs.size() == 0)){
			throw new BusiException("没有发送行参数，请确认是否选中行！");
		}
		
		InvRevBVO[] invRevBVOs = null;
		if(ts_inv_revise_bs != null && ts_inv_revise_bs.size() > 0){
			invRevBVOs = new InvRevBVO[ts_inv_revise_bs.size()];
			for(int i=0;i<ts_inv_revise_bs.size();i++){
				invRevBVOs[i] = JacksonUtils.readValue(ts_inv_revise_bs.get(i), InvRevBVO.class);
			}
		}
		
		InvPackRevBVO[] invPackRevBVOs = null;
		if(ts_inv_pack_rev_bs != null && ts_inv_pack_rev_bs.size() > 0){
			invPackRevBVOs = new InvPackRevBVO[ts_inv_pack_rev_bs.size()];
			for(int i=0;i<ts_inv_pack_rev_bs.size();i++){
				invPackRevBVOs[i] = JacksonUtils.readValue(ts_inv_pack_rev_bs.get(i), InvPackRevBVO.class);
			}
		}
		
		return this.getService().examine(invRevBVOs, invPackRevBVOs);
	}
		
}
