package com.tms.services.peripheral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.nw.dao.NWDao;
import org.nw.json.JacksonUtils;
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


/**
 * @author zhuyj
 * @for 到货服务
 */
@SuppressWarnings("deprecation")
public class ArrivalServiceEndPoint extends ServletEndpointSupport {
	@Autowired
	private TrackingService trackingService;
	
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	public Map<String,Object> arrival(String userCode, Map params) {
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		
		boolean success = false;
		String msg = "";

		setTrackingService();
		this.trackingService = getTrackingService();
		
		String pk_entlineB = String.valueOf(params.get("pk_ent_line_b"));
		EntLineBVO entLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class, "pk_ent_line_b=?", pk_entlineB);
		String memo = String.valueOf(params.get("memo"));
		String act_arri_date = String.valueOf(params.get("arri_date"));
		entLineBVO.setMemo(memo);
		entLineBVO.setAct_arri_date(act_arri_date);
		entLineBVO.setCurr_longitude(new UFDouble(String.valueOf(params.get("currLongitude"))));
		entLineBVO.setCurr_latitude(new UFDouble(String.valueOf(params.get("currLatitude"))));
		entLineBVO.setApp_detail_addr(String.valueOf(params.get("appDetailAddr")));
		String goodsInfos = String.valueOf(params.get("goodsInfos"));
		JsonNode goodsInfo = JacksonUtils.readTree(goodsInfos);
		List<EntLinePackBVO> entLinePackBVOs = new ArrayList<EntLinePackBVO>();
		for(JsonNode goods : goodsInfo){
			EntLinePackBVO entLinePackBVO = JacksonUtils.readValue(goods, EntLinePackBVO.class);
			String[] pks = entLinePackBVO.getPk_ent_line_pack_b().split(",");
			entLinePackBVO.setPk_ent_line_pack_b(pks[0]);
			entLinePackBVO.setPk_ent_pack_b(pks[1]);
			entLinePackBVO.setPk_ent_line_b(pks[2]);
			entLinePackBVO.setPk_entrust(pks[3]);
			Integer num = goods.get("pod_num").getValueAsInt();
			UFDouble weight = new UFDouble(goods.get("weight").toString());
			UFDouble volume = new UFDouble(goods.get("volume").toString());
			entLinePackBVO.setNum(num);
			entLinePackBVO.setWeight(weight);
			entLinePackBVO.setVolume(volume);
			entLinePackBVOs.add(entLinePackBVO);
		}
		entLineBVO.setEntLinePackBVOs(entLinePackBVOs);
		try {
			this.trackingService.confirmArrival(entLineBVO, 2);
		} catch (Exception e) {
			success = false;
			return WebServicesUtils.genAjaxResponse(success, e.getMessage(), null);
		}
		success = true;
		msg = "货物签收成功！";
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
