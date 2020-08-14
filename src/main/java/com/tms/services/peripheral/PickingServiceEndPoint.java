package com.tms.services.peripheral;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.tms.service.pod.PodService;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.pod.PodVO;
import com.tms.web.pod.PodController;


/**
 * @author zhuyj
 * @for 货物跟踪服务接口 
 */
@SuppressWarnings("deprecation")
public class PickingServiceEndPoint extends ServletEndpointSupport {
	@Autowired
	private PodService podService;
	@Autowired
	private PodController podController;
	/**
	 * 确认货物签收，单个发货单签收
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	public Map<String,Object> confirmPick(String userCode, Map params) {
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		
		boolean success = false;
		String msg = "";
		Object data = null;

		//初始化POD服务
		setPodService();
		this.podService = getPodService();
		//创建参数对象
		ParamVO paramVO = new ParamVO();
		paramVO.setBillType("FHD");
		paramVO.setBodyTabCode("ts_inv_pack_b");
		paramVO.setFunCode("t601");
		paramVO.setHeaderTabCode("ts_pod");
		paramVO.setTabCode("ts_pod");
		paramVO.setTemplateID("0001A4100000000017ZN");
		//创建POD对象
		String pk_invoice = (String)params.get("pk_invoice");
		PodVO podVO = new PodVO();
		podVO.setDirty(false);
		podVO.setPk_invoice(pk_invoice);
		podVO.setPod_date(new UFDateTime((String)params.get("pickTime")));
		podVO.setPod_man((String)params.get("picker"));
		podVO.setPod_memo((String)params.get("pickMemo"));
		podVO.setDervice_attitude(Integer.valueOf((String)params.get("attitudeScore"))); 
		podVO.setDelivery_speed(Integer.valueOf((String)params.get("deliSpeedScore")));
		podVO.setLogistics_services(Integer.valueOf((String)params.get("waySpeedScore")));
		podVO.setPod_exp(new UFBoolean("N"));
		//获取当前位置信息
		podVO.setCurr_longitude(new UFDouble((String)params.get("currLongitude")));
		podVO.setCurr_latitude(new UFDouble((String)params.get("currLatitude")));
		podVO.setApp_detail_addr((String)params.get("appDetailAddr"));
		String[] pk_invoices = new String[]{pk_invoice};
		if(pk_invoices == null || pk_invoices.length == 0) {
			msg = "没有发送行参数，请确认是否选中行！";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		List<Map<String, Object>> retList = this.podService.doPod(pk_invoices, podVO, paramVO);
		success = true;
		msg = "货物签收成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	/**
	 * 确认货物异常签收，单个发货单签收
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	public Map<String,Object> expPick(String userCode, Map params) {
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		
		boolean success = false;
		String msg = "";
		Object data = null;

		//初始化POD服务
		setPodService();
		setPodController();
		this.podService = getPodService();
		//创建参数对象
		ParamVO paramVO = new ParamVO();
		paramVO.setBillType("FHD");
		paramVO.setBodyTabCode("ts_inv_pack_b");
		paramVO.setFunCode("t601");
		paramVO.setHeaderTabCode("ts_pod");
		paramVO.setTabCode("ts_pod");
		paramVO.setTemplateID("0001A4100000000017ZN");
		//创建POD对象
		String pk_invoice = (String)params.get("pk_invoice");
		String[] pk_invoices = new String[]{pk_invoice};
		if(pk_invoices == null || pk_invoices.length == 0) {
			msg = "没有发送行参数，请确认是否选中行！";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		
		//创建表单对象
		String json = (String)params.get("billVO");
		//将
		JsonNode jn = JacksonUtils.readTree(json);
		JsonNode header = jn.get(Constants.HEADER);
		JsonNode body = jn.get(Constants.BODY).get("ts_inv_pack_b").get("update");
		AggregatedValueObject billVO = new HYBillVO();
		
		PodVO podVO = (PodVO) JacksonUtils.readValue(header, PodVO.class);
		podVO.setStatus(VOStatus.UPDATED);// 更新主表记录
		billVO.setParentVO(podVO);
		if(body != null){
			InvPackBVO[] invPackBVOs = new InvPackBVO[body.size()];
			for(int i=0; i<body.size(); i++){
				InvPackBVO invPackBVO = (InvPackBVO) JacksonUtils.readValue(body.get(i), InvPackBVO.class);
				invPackBVO.setStatus(VOStatus.UPDATED);
				invPackBVOs[i] = invPackBVO;
			}
			billVO.setChildrenVO(invPackBVOs);
		} 
		
		//WebServicesUtils.checkBeforeExpPod(billVO, paramVO);
		Map<String, Object> retMap = this.podService.doExpPod(pk_invoice, billVO, paramVO);
		success = true;
		msg = "货物签收成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	/**
	 * 撤销签收
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	public Map<String, Object> unPick(String userCode, Map params) {
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		
		boolean success = false;
		String msg = "";
		Object data = null;

		//初始化POD服务
		setPodService();
		setPodController();
		this.podService = getPodService();
		//创建参数对象
		ParamVO paramVO = new ParamVO();
		paramVO.setBillType("FHD");
		paramVO.setBodyTabCode("ts_inv_pack_b");
		paramVO.setFunCode("t601");
		paramVO.setHeaderTabCode("ts_pod");
		paramVO.setTabCode("ts_pod");
		paramVO.setTemplateID("0001A4100000000017ZN");
		//创建POD对象
		String pk_invoice = (String)params.get("pk_invoice");
		String[] pk_invoices = new String[]{pk_invoice};
		if(pk_invoices == null || pk_invoices.length == 0) {
			throw new BusiException("请先选择要撤销签收的记录！");
		}
		List<Map<String, Object>> retList = this.podService.doUnpod(pk_invoices, paramVO);
		success = true;
		msg = "撤销签收成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	
	public PodService getPodService() {
		return podService;
	}

	public void setPodService() {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
		this.podService = (PodService) wac.getBean("podService");
	}

	public PodController getPodController() {
		return podController;
	}

	public void setPodController() {
//		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
//		this.podController = (PodController) wac.getBean("podController");
		podController = new PodController();
	}
	
}
