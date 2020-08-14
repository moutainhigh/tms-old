package com.tms.services.peripheral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.bpmn.data.Data;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.BillStatus;
import org.nw.dao.NWDao;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.pub.lang.UFTime;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.tms.constants.TrackingConst;
import com.tms.service.te.EntrustService;
import com.tms.service.te.TrackingService;
import com.tms.vo.base.APPTrackingVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;


/**
 * @author zhuyj
 * @for 货物跟踪服务接口 
 */
@SuppressWarnings("deprecation")
public class TrackingServiceEndPoint extends ServletEndpointSupport {
	@Autowired
	private TrackingService trackingService;
	@Autowired
	private EntrustService entrustService;
	/**
	 * 确认节点到货,支持多个节点一起到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	public Map<String,Object> confirmArrival(String userCode, String entLineBs) {
		//初始化服务类
		setTrackingService();
		this.trackingService = getTrackingService();
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		boolean success = false;
		String msg = "";
		Object data = null;
		if(StringUtils.isBlank(entLineBs)) {
//			throw new BusiException("没有发送行参数，请确认是否选中行！");
			msg = "没有发送行参数，请确认是否选中行！";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		JsonNode jn = JacksonUtils.readTree(entLineBs);
		// 1、确认需要保证顺序执行，绝对不能出现后面的节点确认成功，而前面的节点没有成功，这里使用时间来保证顺序
		// 2、这里可能出现的情况是，前面的节点确认成功了。并且因为是一个事务里面的，所以提交成功了，而后面的节点没有确认成功，这种情况可以出现
		List<EntLineBVO> entLineBVOs = new ArrayList<EntLineBVO>();
		String psql = "select vbillno from ts_segment where isnull(dr,0)=0 and pk_segment=?";
		String getEntLineBVO = "SELECT * FROM ts_ent_line_b where isnull(dr,0)=0 and pk_ent_line_b = ?";
		for(int i = 0; i < jn.size(); i++) {
			EntLineBVO entLineBVO = NWDao.getInstance().queryForObject(getEntLineBVO, EntLineBVO.class, jn.get(i).get("pk_ent_line_b").getTextValue());
//					JacksonUtils.readValue(jn.get(i).toString(), EntLineBVO.class);
					entLineBVO.setAct_arri_date(jn.get(i).get("act_arri_date").getTextValue());
					entLineBVO.setAct_leav_date(jn.get(i).get("act_leav_date").getTextValue());
					try {
						entLineBVO.setMileage(new UFDouble(jn.get(i).get("mileage").getTextValue()));
					} catch (Exception e) {
						entLineBVO.setMileage(UFDouble.ZERO_DBL);
					}
					
					entLineBVO.setCurr_longitude(new UFDouble(jn.get(i).get("curr_longitude").getTextValue()));
					entLineBVO.setCurr_latitude(new UFDouble(jn.get(i).get("curr_latitude").getTextValue()));
					entLineBVO.setApp_detail_addr(jn.get(i).get("app_detail_addr").getTextValue());
					entLineBVO.setMemo(jn.get(i).get("memo").getTextValue());
			String segment_vbillno = NWDao.getInstance().queryForObject(psql, String.class, entLineBVO.getPk_segment());
			entLineBVO.setSegment_vbillno(segment_vbillno);
			entLineBVOs.add(entLineBVO);
		}
		List<Object[]> objAry = new LinkedList<Object[]>();
		// 根据运单号进行排序
		EntLineBVO[] arr = entLineBVOs.toArray(new EntLineBVO[entLineBVOs.size()]);
		Arrays.sort(arr, new EntLineBVOComparator());
		for(EntLineBVO vo : arr) {
			try {
				objAry.add(trackingService.confirmArrival(vo,0));
			} catch(Exception e) {
				if(objAry.size() == 0) {
//					throw new BusiException(e.getMessage());
					msg = e.getMessage();
					return WebServicesUtils.genAjaxResponse(success, msg, null);
				}
			}
		}
		success = true;
		msg = "节点到货成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	/**
	 * 确认节点到货,支持多个节点一起到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	public Map<String, Object> unConfirmArrival(String userCode, String entLineBs) {
		//初始化服务类
		setTrackingService();
		this.trackingService = getTrackingService();
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		boolean success = false;
		String msg = "";
		Object data = null;
		if(StringUtils.isBlank(entLineBs)) {
//			throw new BusiException("没有发送行参数，请确认是否选中行！");
			msg = "没有发送行参数，请确认是否选中行！";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		JsonNode jn = JacksonUtils.readTree(entLineBs);
		// 1、反确认需要保证按倒序执行，不能出现前面的反确认成功了，而后面的节点没有反确认成功的情况，使用时间判断来保证这点
		// 2、这里可能出现的情况是，前面的节点反确认成功了。并且因为是一个事务里面的，所以提交成功了，而后面的节点没有反确认成功
		List<EntLineBVO> entLineBVOs = new ArrayList<EntLineBVO>();
		String psql = "select vbillno from ts_segment where isnull(dr,0)=0 and pk_segment=?";
		for(int i = 0; i < jn.size(); i++) {
			EntLineBVO entLineBVO = JacksonUtils.readValue(jn.get(i), EntLineBVO.class);
			String segment_vbillno = NWDao.getInstance().queryForObject(psql, String.class, entLineBVO.getPk_segment());
			entLineBVO.setSegment_vbillno(segment_vbillno);
			entLineBVOs.add(entLineBVO);
		}
		List<Object[]> objAry = new LinkedList<Object[]>();
		// 根据运单号进行排序
		EntLineBVO[] arr = entLineBVOs.toArray(new EntLineBVO[entLineBVOs.size()]);
		Arrays.sort(arr, new EntLineBVOComparator());
		// 需要根据倒序反确认
		for(int i = arr.length - 1; i >= 0; i--) {
			try {
				objAry.add(trackingService.unconfirmArrival(arr[i]));
			} catch(Exception e) {
				if(objAry.size() == 0) {
//					throw new BusiException(e.getMessage());
					msg = e.getMessage();
					return WebServicesUtils.genAjaxResponse(success, msg, null);
				}
			}
		}
		success = true;
		msg = "取消节点到货成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	
	/**
	 * 根据行号正向排序
	 * 
	 * @author xuqc
	 * 
	 */
	class EntLineBVOComparator implements Comparator<EntLineBVO> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(EntLineBVO lineVO1, EntLineBVO lineVO2) {
			if(lineVO1.getSerialno() != null && lineVO2.getSerialno() != null) {
				return lineVO1.getSerialno().compareTo(lineVO2.getSerialno());
			} else {
				if(StringUtils.isBlank(lineVO1.getSegment_vbillno())
						|| StringUtils.isBlank(lineVO2.getSegment_vbillno())) {
					return 0;
				}
				if(lineVO1.getSegment_vbillno().equals(lineVO2.getSegment_vbillno())) {
					// 如果是相同运段，那么使用实际到达时间来判断
					String act_arri_date1 = lineVO1.getAct_arri_date();
					String act_arri_date2 = lineVO2.getAct_arri_date();
					if(StringUtils.isBlank(act_arri_date1) || StringUtils.isBlank(act_arri_date2)) {
						return 0;
					}
					return act_arri_date1.compareTo(act_arri_date2);
				}
				return lineVO1.getSegment_vbillno().compareTo(lineVO2.getSegment_vbillno());
			}
		}
	}

	public TrackingService getTrackingService() {
		return trackingService;
	}

	public void setTrackingService() {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
		this.trackingService = (TrackingService) wac.getBean("trackingService");
	}
	
	public EntrustService getEntrustService() {
		return entrustService;
	}

	public void setEntrustService() {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
		this.entrustService = (EntrustService) wac.getBean("entrustService");
	}

	/**
	 * 异常反馈
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@ResponseBody
	public Map<String,Object> expFeedBack(String userCode, Map exps) {
		//初始化服务类
		setEntrustService();
		setTrackingService();
		this.entrustService = getEntrustService();
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		boolean success = false;
		String msg = "";
		Object [] pkEntrusts = (Object [])exps.get("pkEntrusts");
		String expTypes =  exps.get("expTypes").toString();
		String expMemo =  exps.get("expMemo").toString();
		//获取当前位置信息
		String currLongitude = (String)exps.get("currLongitude");
		String currLatitude = (String)exps.get("currLatitude");
		String appDetailAddr = (String)exps.get("appDetailAddr");
		String occur_date = (String)exps.get("occur_date");
		for(Object pkEntrust :pkEntrusts){
			EntrustVO ent = this.entrustService.getByPrimaryKey(EntrustVO.class,(String)pkEntrust);
			/**
			 * 创建货物跟踪对象
			 */
			EntTrackingVO etVO = new EntTrackingVO();
			//设置委托单号
			etVO.setEntrust_vbillno(ent.getVbillno());
			etVO.setExp_flag(UFBoolean.TRUE);
			etVO.setDirty(false);
			etVO.setSync_flag(UFBoolean.TRUE);
			etVO.setTracking_status(TrackingConst.ONROAD);
			Date date = new Date();
			etVO.setTracking_time(new UFDateTime(date.getTime()));
			//设置当前位置信息
			etVO.setCurr_longitude(new UFDouble(currLongitude));
			etVO.setCurr_latitude(new UFDouble(currLatitude));
			etVO.setApp_detail_addr(appDetailAddr);
			/**
			 * 创建异常对象
			 */
			ExpAccidentVO eaVO = new ExpAccidentVO();
			eaVO.setEntrust_vbillno(ent.getVbillno());
			eaVO.setDirty(false);
			eaVO.setMemo(expMemo);
			eaVO.setOrigin("异常跟踪");
			eaVO.setPk_carrier(ent.getPk_carrier());
			eaVO.setStatus(BillStatus.NEW);
			eaVO.setExp_type(expTypes);
			eaVO.setVbillstatus(BillStatus.NEW);
			eaVO.setOccur_date(new UFDateTime(occur_date));
			eaVO.setOccur_addr(appDetailAddr);
			
			/**
			 * 创建委托单对象
			 */
			EntrustVO entVO = new EntrustVO();
			entVO.setExp_flag(UFBoolean.TRUE);
			entVO.setMemo(expMemo);
			entVO.setPk_carrier(ent.getPk_carrier());
			entVO.setStatus(BillStatus.NEW);
			entVO.setTracking_status(TrackingConst.ONROAD);
			entVO.setTracking_time(etVO.getTracking_time());
			entVO.setVbillstatus(BillStatus.NEW);
			
			etVO.setExp_type(eaVO.getExp_type());// 跟踪信息的异常类型等于异常表中的异常类型
			etVO.setExp_type(expTypes);
			String invoiceNOs = ent.getInvoice_vbillno();
			String[] invoiceVbillnoAry = invoiceNOs.split("[,]");
			EntrustVO oriEntVO = (EntrustVO) entrustService.getByCode(etVO.getEntrust_vbillno());
			// 前台并没有传入vbillstatus字段，而且前台传的vbillno也不是委托单的，所以这里从后台得到，重新设置
			entVO.setVbillno(oriEntVO.getVbillno());
			entVO.setVbillstatus(oriEntVO.getVbillstatus());
			Map<String, Object> retMap = this.trackingService.saveEntTracking(etVO, entVO, eaVO, new String[]{},
					invoiceVbillnoAry);
		}
		success = true;
		msg = "异常反馈成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	
	/**
	 * 跟踪终端位置
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	public Map<String,Object> tracking(String userCode, Map params) {
		//初始化服务类
		setTrackingService();
		this.trackingService = getTrackingService();
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		boolean success = false;
		String msg = "";
		Object data = null;
		APPTrackingVO appTrackingVO = new APPTrackingVO();
		appTrackingVO.setMobile(userCode);
		appTrackingVO.setPk_corp(String.valueOf(params.get("pkCorp")==null?"":params.get("pkCorp")));
		appTrackingVO.setApp_longitude(String.valueOf(params.get("currLongitude")==null?"":params.get("currLongitude")));
		appTrackingVO.setApp_latitude(String.valueOf(params.get("currLatitude")==null?"":params.get("currLatitude")));
		appTrackingVO.setLongitude(String.valueOf(params.get("longitude")==null?"":params.get("longitude")));
		appTrackingVO.setLatitude(String.valueOf(params.get("latitude")==null?"":params.get("latitude")));
		appTrackingVO.setApp_version(String.valueOf(params.get("appVersion")==null?"":params.get("appVersion")));
		appTrackingVO.setSpeed(new UFDouble(String.valueOf(params.get("speed")==null?"0.0":params.get("speed"))));
		appTrackingVO.setDistance(new UFDouble(String.valueOf(params.get("distance")==null?"0.0":params.get("distance"))));
		appTrackingVO.setRoad_name(String.valueOf(params.get("roadName")==null?"":params.get("roadName")));
		appTrackingVO.setPlace_name(String.valueOf(params.get("placeName")==null?"":params.get("placeName")));
		if(params.get("trackTime")==null){
			appTrackingVO.setTrack_time(new UFDateTime(new Date()));
		}else{
			appTrackingVO.setTrack_time(new UFDateTime( String.valueOf(params.get("trackTime"))));
		}
		if(params.get("gpsTime")==null){
			appTrackingVO.setGps_time(new UFDateTime(new Date()));
		}else{
			appTrackingVO.setGps_time(new UFDateTime( String.valueOf(params.get("gpsTime"))));
		}
		appTrackingVO.setSerialno(String.valueOf(params.get("phoneNo")==null?"":params.get("phoneNo")));
		//标记VO状态为新增
		appTrackingVO.setStatus(VOStatus.NEW);
		//设置主键
		NWDao.setUuidPrimaryKey(appTrackingVO);
		this.trackingService.confirmTracking(appTrackingVO);
		success = true;
		msg = "APP位置上传成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
}
