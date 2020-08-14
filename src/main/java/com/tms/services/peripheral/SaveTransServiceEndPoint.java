package com.tms.services.peripheral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.nw.dao.NWDao;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.tms.service.te.TrackingService;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntTransbilityBVO;


/**
 * @author XIA
 * @for 前往节点
 */
@SuppressWarnings("deprecation")
public class SaveTransServiceEndPoint extends ServletEndpointSupport {
	@Autowired
	private TrackingService trackingService;
	
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	public Map<String,Object> saveTrans(Map params) {
		
		boolean success = false;
		String msg = "";

		setTrackingService();
		this.trackingService = getTrackingService();
		
		String lot = String.valueOf(params.get("lot"));
		String pk_address = String.valueOf(params.get("pk_address"));
		String detail_addr = String.valueOf(params.get("detail_addr"));
		String for_arri_date = String.valueOf(params.get("for_arri_date"));
		String memo = String.valueOf(params.get("memo"));
		
		String sql = "SELECT ts_ent_transbility_b.* FROM ts_ent_transbility_b WITH(NOLOCK) "
				+ " LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_ent_transbility_b.pk_entrust = ts_entrust.pk_entrust "
				+ "	WHERE isnull(ts_ent_transbility_b.dr,0)=0 "
				+ "	AND isnull(ts_entrust.dr,0)=0 AND ts_entrust.lot=?";
		List<EntTransbilityBVO> transBVOs = NWDao.getInstance().queryForList(sql, EntTransbilityBVO.class, lot);
		if(transBVOs != null && transBVOs.size() > 0){
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			for(EntTransbilityBVO transBVO : transBVOs){
				transBVO.setForecast_deli_date(new UFDateTime(for_arri_date));
				transBVO.setPk_address(pk_address);
				transBVO.setDetail_addr(detail_addr);
				transBVO.setMemo(memo);
				transBVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(transBVO);
			}
			NWDao.getInstance().saveOrUpdate(toBeUpdate);
		}
		
		success = true;
		msg = "节点记录成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}

	
	public TrackingService getTrackingService() {
		return trackingService;
	}

	public void setTrackingService() {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
		this.trackingService = (TrackingService) wac.getBean("trackingService");
	}

	
}
