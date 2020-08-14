package com.tms.web.te;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.exp.ExcelImporter;
import org.nw.json.JacksonUtils;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.FilesystemVO;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.FunConst;
import com.tms.service.attach.AttachService;
import com.tms.service.te.EntrustService;
import com.tms.service.te.TrackingService;
import com.tms.service.te.impl.TrackingExcelImporter;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntOperationBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;
import org.nw.vo.pub.lang.UFDouble;
/**
 * 异常跟踪操作
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/te/tracking")
public class TrackingController extends AbsBillController {

	@Autowired
	private TrackingService trackingService;

	@Autowired
	private EntrustService entrustService;

	@Autowired
	private AttachService attachService;
	
	public TrackingService getService() {
		return trackingService;
	}

	/**
	 * 根据委托单pk加载路线信息，目前用于节点到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadEntLineB.json")
	@ResponseBody
	public Map<String, Object> loadEntLineB(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> list = this.getService().loadEntLineB(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	/**
	 * 根据委托单pk加载路线信息，只加载第一个节点
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadDeliEntLineB.json")
	@ResponseBody
	public Map<String, Object> loadDeliEntLineB(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> list = this.getService().loadDeliEntLineB(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	@RequestMapping(value = "/loadArriEntLineB.json")
	@ResponseBody
	public Map<String, Object> loadArriEntLineB(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> list = this.getService().loadArriEntLineB(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	//yaojiie 2015 12 27 添加此JSON，在节点到货时，显示这个委托单下，对应的路线包装信息
	@RequestMapping(value = "/loadEntLinePackB.json")
	@ResponseBody
	public Map<String, Object> loadEntLinePackB(HttpServletRequest request, HttpServletResponse response) {
		String pk_ent_line_b = request.getParameter("pk_ent_line_b");
		List<Map<String, Object>> list = this.getService().loadEntLinePackB(pk_ent_line_b);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	

	/**
	 * 根据委托单pk加载运力信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadEntTransbilityB.json")
	@ResponseBody
	public Map<String, Object> loadEntTransbilityB(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> list = entrustService.loadEntTransbilityB(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
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

	/**
	 * 确认节点到货,支持多个节点一起到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/confirmArrival.json")
	@ResponseBody
	public Map<String, Object> confirmArrival(HttpServletRequest request, HttpServletResponse response) {
		String appPostData = request.getParameter(Constants.APP_POST_DATA);
		String lineGoodsData = request.getParameter("LINE_GOODS_DATA");
		if(StringUtils.isBlank(appPostData)) {
			throw new BusiException("没有发送行参数，请确认是否选中行！");
		}
		JsonNode jn = JacksonUtils.readTree(appPostData);
		//当lineGoodsData == null 时，表示批量节点到货，不获取数据。
		JsonNode jLineGoodsData = null;
		if(lineGoodsData != null){
			jLineGoodsData = JacksonUtils.readTree(lineGoodsData);
		}
		
		// 1、确认需要保证顺序执行，绝对不能出现后面的节点确认成功，而前面的节点没有成功，这里使用时间来保证顺序
		// 2、这里可能出现的情况是，前面的节点确认成功了。并且因为是一个事务里面的，所以提交成功了，而后面的节点没有确认成功，这种情况可以出现
		List<EntLineBVO> entLineBVOs = new ArrayList<EntLineBVO>();
		String psql = "select vbillno from ts_segment where isnull(dr,0)=0 and pk_segment=?";
		for(int i = 0; i < jn.size(); i++) {
			EntLineBVO entLineBVO = JacksonUtils.readValue(jn.get(i), EntLineBVO.class);
			String segment_vbillno = NWDao.getInstance().queryForObject(psql, String.class, entLineBVO.getPk_segment());
			entLineBVO.setSegment_vbillno(segment_vbillno);
			entLineBVOs.add(entLineBVO);
		}
		
		//从Json中获取界面件重体信息
		if(lineGoodsData != null){
			for(EntLineBVO vo :entLineBVOs ){
				List<EntLinePackBVO> entLinePackBVOs = new ArrayList<EntLinePackBVO>();
				for(int i = 0;i < jLineGoodsData.size();i++){
					EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
					 String pk_ent_line_pack_b = jLineGoodsData.get(i).get("pk_ent_line_pack_b").getTextValue();
					 String pk_ent_line_b = jLineGoodsData.get(i).get("pk_ent_line_b").getTextValue();
					 int num = jLineGoodsData.get(i).get("num").getValueAsInt();
					 double weight = jLineGoodsData.get(i).get("weight").getValueAsDouble();
					 double volume = jLineGoodsData.get(i).get("volume").getValueAsDouble();
					 entLinePackBVO.setPk_ent_line_pack_b(pk_ent_line_pack_b);
					 entLinePackBVO.setPk_ent_line_b(pk_ent_line_b);
					 entLinePackBVO.setNum(num);
					 entLinePackBVO.setWeight(new UFDouble(weight));
					 entLinePackBVO.setVolume(new UFDouble(volume));
				
					if(entLinePackBVO.getPk_ent_line_b().equals(vo.getPk_ent_line_b())){
						entLinePackBVOs.add(entLinePackBVO);
					}
				}
				if(entLinePackBVOs.size() > 0){
					//将界面货品信息赋值给界面的线路信息
					vo.setEntLinePackBVOs(entLinePackBVOs);
				}	
			}
		}
	
		
		List<Object[]> objAry = new LinkedList<Object[]>();
		// 根据运单号进行排序
		EntLineBVO[] arr = entLineBVOs.toArray(new EntLineBVO[entLineBVOs.size()]);
		Arrays.sort(arr, new EntLineBVOComparator());
		for(EntLineBVO vo : arr) {
			try {
				objAry.add(this.getService().confirmArrival(vo,0));
			} catch(Exception e) {
				if(objAry.size() == 0) {
					throw new BusiException(e.getMessage());
				}
				return this.genAjaxResponse(true, null, objAry, e.getMessage());
			}
		}
		return this.genAjaxResponse(true, null, objAry);
	}
	
	/**
	 * 提货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/delivery.json")
	@ResponseBody
	public Map<String, Object> delivery(HttpServletRequest request, HttpServletResponse response) {
		String appPostData = request.getParameter(Constants.APP_POST_DATA);
		String lineGoodsData = request.getParameter("LINE_GOODS_DATA");
		if(StringUtils.isBlank(appPostData)) {
			throw new BusiException("没有发送行参数，请确认是否选中行！");
		}
		JsonNode jn = JacksonUtils.readTree(appPostData);
		//当lineGoodsData == null 时，表示批量节点到货，不获取数据。
		JsonNode jLineGoodsData = null;
		if(lineGoodsData != null){
			jLineGoodsData = JacksonUtils.readTree(lineGoodsData);
		}
		
		// 1、确认需要保证顺序执行，绝对不能出现后面的节点确认成功，而前面的节点没有成功，这里使用时间来保证顺序
		// 2、这里可能出现的情况是，前面的节点确认成功了。并且因为是一个事务里面的，所以提交成功了，而后面的节点没有确认成功，这种情况可以出现
		List<EntLineBVO> entLineBVOs = new ArrayList<EntLineBVO>();
		String psql = "select vbillno from ts_segment where isnull(dr,0)=0 and pk_segment=?";
		for(int i = 0; i < jn.size(); i++) {
			EntLineBVO entLineBVO = JacksonUtils.readValue(jn.get(i), EntLineBVO.class);
			String segment_vbillno = NWDao.getInstance().queryForObject(psql, String.class, entLineBVO.getPk_segment());
			entLineBVO.setSegment_vbillno(segment_vbillno);
			entLineBVOs.add(entLineBVO);
		}
		
		//从Json中获取界面件重体信息
		if(lineGoodsData != null){
			for(EntLineBVO vo :entLineBVOs ){
				List<EntLinePackBVO> entLinePackBVOs = new ArrayList<EntLinePackBVO>();
				for(int i = 0;i < jLineGoodsData.size();i++){
					EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
					 String pk_ent_line_pack_b = jLineGoodsData.get(i).get("pk_ent_line_pack_b").getTextValue();
					 String pk_ent_line_b = jLineGoodsData.get(i).get("pk_ent_line_b").getTextValue();
					 String pk_ent_pack_b = jLineGoodsData.get(i).get("pk_ent_pack_b").getTextValue();
					 String pk_entrust = jLineGoodsData.get(i).get("pk_entrust").getTextValue();
					 int num = jLineGoodsData.get(i).get("num").getValueAsInt();
					 double weight = jLineGoodsData.get(i).get("weight").getValueAsDouble();
					 double volume = jLineGoodsData.get(i).get("volume").getValueAsDouble();
					 entLinePackBVO.setPk_ent_line_pack_b(pk_ent_line_pack_b);
					 entLinePackBVO.setPk_ent_line_b(pk_ent_line_b);
					 entLinePackBVO.setPk_ent_pack_b(pk_ent_pack_b);
					 entLinePackBVO.setPk_entrust(pk_entrust);
					 entLinePackBVO.setNum(num);
					 entLinePackBVO.setWeight(new UFDouble(weight));
					 entLinePackBVO.setVolume(new UFDouble(volume));
				
					if(entLinePackBVO.getPk_ent_line_b().equals(vo.getPk_ent_line_b())){
						entLinePackBVOs.add(entLinePackBVO);
					}
				}
				if(entLinePackBVOs.size() > 0){
					//将界面货品信息赋值给界面的线路信息
					vo.setEntLinePackBVOs(entLinePackBVOs);
				}	
			}
		}
	
		
		List<Object[]> objAry = new LinkedList<Object[]>();
		// 根据运单号进行排序
		EntLineBVO[] arr = entLineBVOs.toArray(new EntLineBVO[entLineBVOs.size()]);
		Arrays.sort(arr, new EntLineBVOComparator());
		for(EntLineBVO vo : arr) {
			try {
				objAry.add(this.getService().confirmArrival(vo,1));
			} catch(Exception e) {
				if(objAry.size() == 0) {
					throw new BusiException(e.getMessage());
				}
				return this.genAjaxResponse(true, null, objAry, e.getMessage());
			}
		}
		return this.genAjaxResponse(true, null, objAry);
	}

	/**
	 * 提货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/arrival.json")
	@ResponseBody
	public Map<String, Object> arrival(HttpServletRequest request, HttpServletResponse response) {
		String appPostData = request.getParameter(Constants.APP_POST_DATA);
		String lineGoodsData = request.getParameter("LINE_GOODS_DATA");
		if(StringUtils.isBlank(appPostData)) {
			throw new BusiException("没有发送行参数，请确认是否选中行！");
		}
		JsonNode jn = JacksonUtils.readTree(appPostData);
		//当lineGoodsData == null 时，表示批量节点到货，不获取数据。
		JsonNode jLineGoodsData = null;
		if(lineGoodsData != null){
			jLineGoodsData = JacksonUtils.readTree(lineGoodsData);
		}
		
		// 1、确认需要保证顺序执行，绝对不能出现后面的节点确认成功，而前面的节点没有成功，这里使用时间来保证顺序
		// 2、这里可能出现的情况是，前面的节点确认成功了。并且因为是一个事务里面的，所以提交成功了，而后面的节点没有确认成功，这种情况可以出现
		List<EntLineBVO> entLineBVOs = new ArrayList<EntLineBVO>();
		String psql = "select vbillno from ts_segment where isnull(dr,0)=0 and pk_segment=?";
		for(int i = 0; i < jn.size(); i++) {
			EntLineBVO entLineBVO = JacksonUtils.readValue(jn.get(i), EntLineBVO.class);
			String segment_vbillno = NWDao.getInstance().queryForObject(psql, String.class, entLineBVO.getPk_segment());
			entLineBVO.setSegment_vbillno(segment_vbillno);
			entLineBVOs.add(entLineBVO);
		}
		EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "pk_entrust=?", entLineBVOs.get(0).getPk_entrust());
		if(entrustVO.getVbillstatus() != BillStatus.ENT_DELIVERY){
			throw new BusiException("只有状态为[已提货]的委托单，才能进行到货！");
		}
		//从Json中获取界面件重体信息
		if(lineGoodsData != null){
			for(EntLineBVO vo :entLineBVOs ){
				List<EntLinePackBVO> entLinePackBVOs = new ArrayList<EntLinePackBVO>();
				for(int i = 0;i < jLineGoodsData.size();i++){
					EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
					 String pk_ent_line_pack_b = jLineGoodsData.get(i).get("pk_ent_line_pack_b").getTextValue();
					 String pk_ent_line_b = jLineGoodsData.get(i).get("pk_ent_line_b").getTextValue();
					 String pk_ent_pack_b = jLineGoodsData.get(i).get("pk_ent_pack_b").getTextValue();
					 String pk_entrust = jLineGoodsData.get(i).get("pk_entrust").getTextValue();
					 int num = jLineGoodsData.get(i).get("num").getValueAsInt();
					 double weight = jLineGoodsData.get(i).get("weight").getValueAsDouble();
					 double volume = jLineGoodsData.get(i).get("volume").getValueAsDouble();
					 entLinePackBVO.setPk_ent_line_pack_b(pk_ent_line_pack_b);
					 entLinePackBVO.setPk_ent_line_b(pk_ent_line_b);
					 entLinePackBVO.setPk_ent_pack_b(pk_ent_pack_b);
					 entLinePackBVO.setPk_entrust(pk_entrust);
					 entLinePackBVO.setNum(num);
					 entLinePackBVO.setWeight(new UFDouble(weight));
					 entLinePackBVO.setVolume(new UFDouble(volume));
				
					if(entLinePackBVO.getPk_ent_line_b().equals(vo.getPk_ent_line_b())){
						entLinePackBVOs.add(entLinePackBVO);
					}
				}
				if(entLinePackBVOs.size() > 0){
					//将界面货品信息赋值给界面的线路信息
					vo.setEntLinePackBVOs(entLinePackBVOs);
				}	
			}
		}
	
		
		List<Object[]> objAry = new LinkedList<Object[]>();
		// 根据运单号进行排序
		EntLineBVO[] arr = entLineBVOs.toArray(new EntLineBVO[entLineBVOs.size()]);
		Arrays.sort(arr, new EntLineBVOComparator());
		for(EntLineBVO vo : arr) {
			objAry.add(this.getService().confirmArrival(vo,2));
//			try {
//				objAry.add(this.getService().confirmArrival(vo,2));
//			} catch(Exception e) {
//				if(objAry.size() == 0) {
//					throw new BusiException(e.getMessage());
//				}
//				return this.genAjaxResponse(true, null, objAry, e.getMessage());
//			}
		}
		return this.genAjaxResponse(true, null, objAry);
	}
	/**
	 * 反确认节点到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unconfirmArrival.json")
	@ResponseBody
	public Map<String, Object> unconfirmArrival(HttpServletRequest request, HttpServletResponse response) {
		String appPostData = request.getParameter(Constants.APP_POST_DATA);
		if(StringUtils.isBlank(appPostData)) {
			throw new BusiException("没有发送行参数，请确认是否选中行！");
		}
		JsonNode jn = JacksonUtils.readTree(appPostData);
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
				objAry.add(this.getService().unconfirmArrival(arr[i]));
			} catch(Exception e) {
				if(objAry.size() == 0) {
					throw new BusiException(e.getMessage());
				}
				return this.genAjaxResponse(true, null, objAry, e.getMessage());
			}
		}
		return this.genAjaxResponse(true, null, objAry);
	}
	
	/**
	 * 撤销到货
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unArrival.json")
	@ResponseBody
	public Map<String, Object> unArrival(HttpServletRequest request, HttpServletResponse response) {
		String appPostData = request.getParameter(Constants.APP_POST_DATA);
		if(StringUtils.isBlank(appPostData)) {
			throw new BusiException("没有发送行参数，请确认是否选中行！");
		}
		JsonNode jn = JacksonUtils.readTree(appPostData);
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
				objAry.add(this.getService().unArrival(arr[i]));
			} catch(Exception e) {
				if(objAry.size() == 0) {
					throw new BusiException(e.getMessage());
				}
				return this.genAjaxResponse(true, null, objAry, e.getMessage());
			}
		}
		return this.genAjaxResponse(true, null, objAry);
	}


	/**
	 * 根据委托单主键查询跟踪记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadEntTracking.json")
	@ResponseBody
	public Map<String, Object> loadEntTracking(HttpServletRequest request, HttpServletResponse response) {
		String entrust_vbillno = request.getParameter("entrust_vbillno");
		if(StringUtils.isBlank(entrust_vbillno)) {
			throw new BusiException("entrust_vbillno参数不能为空！");
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO pageVO = this.getService().loadEntTracking(entrust_vbillno, offset, pageSize);
		return this.genAjaxResponse(true, null, pageVO);
	}

	/**
	 * 删除委托单跟踪记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deleteEntTracking.json")
	@ResponseBody
	public Map<String, Object> deleteEntTracking(HttpServletRequest request, HttpServletResponse response) {
		String pk_ent_tracking = request.getParameter("pk_ent_tracking");
		String last_pk = request.getParameter("last_pk"); // 上一条跟踪记录，可以为空
		if(StringUtils.isBlank(pk_ent_tracking)) {
			throw new BusiException("删除委托单跟踪记录时，主键不能为空！");
		}
		this.getService().deleteEntTracking(pk_ent_tracking, last_pk);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 保存跟踪记录的时候，需要根据情况记录异常信息，或者更新到委托单主表的信息
	 * 
	 * @param etVO
	 * @param entVO
	 * @param eaVO
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/saveEntTracking.json")
	@ResponseBody
	public Map<String, Object> saveEntTracking(EntTrackingVO etVO, ExpAccidentVO eaVO, EntrustVO entVO,
			HttpServletRequest request, HttpServletResponse response) {
		String pk_filesystem1 = request.getParameter("pk_filesystem1");
		String pk_filesystem2 = request.getParameter("pk_filesystem2");
		String pk_filesystem3 = request.getParameter("pk_filesystem3");
		String[] pk_filesystems = new String[]{pk_filesystem1,pk_filesystem2,pk_filesystem3};
		etVO.setExp_type(eaVO.getExp_type());// 跟踪信息的异常类型等于异常表中的异常类型
		if(StringUtils.isBlank(etVO.getEntrust_vbillno())) {
			throw new BusiException("委托单号不能为空！");
		}
		String[] invoiceVbillnoAry = request.getParameterValues("invoiceVbillnoAry");
		EntrustVO oriEntVO = (EntrustVO) entrustService.getByCode(etVO.getEntrust_vbillno());
		// 前台并没有传入vbillstatus字段，而且前台传的vbillno也不是委托单的，所以这里从后台得到，重新设置
		entVO.setVbillno(oriEntVO.getVbillno());
		entVO.setVbillstatus(oriEntVO.getVbillstatus());
		Map<String, Object> retMap = this.getService().saveEntTracking(etVO, entVO, eaVO, pk_filesystems,
				invoiceVbillnoAry);
		return this.genAjaxResponse(true, null, retMap);
	}

	/**
	 * 异常事故上传附件
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/uploadAttach.json")
	public void uploadAttach(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		MultipartFile file1 = mRequest.getFile("pk_filesystem1");
		MultipartFile file2 = mRequest.getFile("pk_filesystem2");
		MultipartFile file3 = mRequest.getFile("pk_filesystem3");
		if(file1 == null && file2 == null && file3 == null) {
			throw new BusiException("上传文件不能为空！");
		}
		MultipartFile file = null;
		String id = "";
		if(file1 != null ){
			file = file1;
			id = "pk_filesystem1";
		}
		if(file2 != null ){
			file = file2;
			id = "pk_filesystem2";
		}
		if(file3 != null ){
			file = file3;
			id = "pk_filesystem3";
		}
		String pk_corp = WebUtils.getLoginInfo().getPk_corp();
		String create_user = WebUtils.getLoginInfo().getPk_user();
		String pkAtta = attachService.uploadAtta(file,pk_corp,create_user,BillTypeConst.YCSG,"");
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(id, pkAtta);
		retMap.put("msg", "操作成功!");
		retMap.put("success", "true");
		retMap.put("data", dataMap);
		this.writeHtmlStream(response, JacksonUtils.writeValueAsString(retMap));
	}

	/**
	 * 保存跟踪记录的时候，需要根据情况记录异常信息，或者更新到委托单主表的信息 区分于saveEntTracking，这个方法适合表单直接提交
	 * 
	 * @param etVO
	 * @param entVO
	 * @param eaVO
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/saveEntTracking2.do")
	public void saveEntTracking2(EntTrackingVO etVO, EntrustVO entVO, ExpAccidentVO eaVO, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> fileAry = mRequest.getFiles("userfile");
		List<FilesystemVO> attachVOs = new ArrayList<FilesystemVO>();
		List<InputStream> inAry = new ArrayList<InputStream>();
		for(MultipartFile file : fileAry) {
			String fileName = file.getOriginalFilename();
			if(StringUtils.isBlank(fileName)) {
				continue;
			}
			FilesystemVO attachVO = new FilesystemVO();
			attachVO.setPk_filesystem(UUID.randomUUID().toString());
			attachVO.setDr(0);
			attachVO.setTs(new UFDateTime(new Date()));
			attachVO.setFile_name(fileName);
			attachVO.setFile_size(file.getSize());
			attachVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			attachVO.setCreate_time(new UFDateTime(new Date()));
			attachVOs.add(attachVO);
			inAry.add(file.getInputStream());
		}
		etVO.setExp_type(eaVO.getExp_type());// 跟踪信息的异常类型等于异常表中的异常类型
		Map<String, Object> retMap = this.getService().saveEntTracking2(etVO, entVO, eaVO, attachVOs, inAry);
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("msg", "操作成功！");
		dataMap.put("success", "true");
		dataMap.put("data", retMap);
		this.writeHtmlStream(response, JacksonUtils.writeValueAsString(retMap));
	}

	/**
	 * 批量保存跟踪记录
	 * 
	 * @param etVO
	 * @param entVO
	 * @param eaVO
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/batchSaveEntTracking.json")
	@ResponseBody
	public Map<String, Object> batchSaveEntTracking(EntTrackingVO etVO, HttpServletRequest request,
			HttpServletResponse response) {
		String[] vbillnoAry = request.getParameterValues("vbillno");
		Map<String, Object> retMap = this.getService().batchSaveEntTracking(etVO, vbillnoAry);
		return this.genAjaxResponse(true, null, retMap);
	}
	//yaojiie 2016 1 6 提货到货确认
	@RequestMapping(value = "/deliConfirm.json") 
	@ResponseBody
	public Map<String, Object> deliConfirm(HttpServletRequest request, HttpServletResponse response) {
		
		String[] pk_entrusts = request.getParameterValues("ids");
		String confirm_date= request.getParameter("confirm_date");
		String confirm_memo= request.getParameter("confirm_memo");
		String cond = NWUtils.buildConditionString(pk_entrusts);
		EntLineBVO[] lineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust in" + cond);
		
		for(EntLineBVO lineBVO : lineBVOs){
			if(lineBVO.getSerialno() == 10){
				lineBVO.setAct_arri_date(confirm_date);
				lineBVO.setMemo(confirm_memo);
				this.getService().confirmArrival(lineBVO,0);
			}
		}
		
		return this.genAjaxResponse(true, null, lineBVOs);
	}
	
	@RequestMapping(value = "/arriConfirm.json")
	@ResponseBody
	public Map<String, Object> arriConfirm(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_entrusts = request.getParameterValues("ids");
		String confirm_date= request.getParameter("confirm_date");
		String confirm_memo= request.getParameter("confirm_memo");
		String cond = NWUtils.buildConditionString(pk_entrusts);
		EntLineBVO[] lineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust in" + cond +" order by serialno ");
		//对entLineBVOs按委托单分组
		Map<String, List<EntLineBVO>> groupMap = new HashMap<String, List<EntLineBVO>>();
		for (EntLineBVO lineBVO : lineBVOs) {
			String key = new StringBuffer().append(lineBVO.getPk_entrust()).toString();
			List<EntLineBVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<EntLineBVO>();
				groupMap.put(key, voList);
			}
			voList.add(lineBVO);
		}
		
		for(String key : groupMap.keySet()){
			List<EntLineBVO> entLineBVOs = groupMap.get(key);
			int serialno = 0;
			int index = 0;
			for(int i = 0;i<entLineBVOs.size();i++){
				if(entLineBVOs.get(i).getSerialno()>serialno){
					serialno = entLineBVOs.get(i).getSerialno();
					index=i;
				}
			}
			if(entLineBVOs.get(index-1).getArrival_flag() != null && entLineBVOs.get(index-1).getArrival_flag().equals(UFBoolean.TRUE)){
				entLineBVOs.get(index).setAct_arri_date(confirm_date);
				entLineBVOs.get(index).setMemo(confirm_memo);
				this.getService().confirmArrival(entLineBVOs.get(index),0);
			}else{
				throw new BusiException("上一个节点[?]还未到货，不允许进行到货确认！",entLineBVOs.get(index-1).getAddr_name());
			}
			
		}
		return this.genAjaxResponse(true, null, lineBVOs);
	}
	
	

	/**
	 * 将委托单的提货方和收货方标注到地图,使用线连接,形成推荐的路径
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getVirtualTrack.json")
	@ResponseBody
	public Map<String, Object> getVirtualTrack(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		if(StringUtils.isBlank(pk_entrust)) {
			throw new BusiException("委托单参数[pk_entrust]是必须的！");
		}
		EntrustVO entVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "pk_entrust=?", pk_entrust);
		if(entVO == null) {
			throw new BusiException("委托单已经不存在,或被删除,pk_entrust[?]！",pk_entrust);
		}
		String[] addrs = new String[] { entVO.getDeli_detail_addr(), entVO.getArri_detail_addr() };
		return this.genAjaxResponse(true, null, addrs);
	}

	/**
	 * 从LBS中读取跟踪位置信息
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/getGpsTrackVOs.json")
	@ResponseBody
	public RootVO getGpsTrackVOs(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		if(StringUtils.isBlank(pk_entrust)) {
			throw new BusiException("委托单参数[pk_entrust]是必须的！");
		}
		String gps_id = request.getParameter("gps_id");
		if(StringUtils.isBlank(gps_id)) {
			throw new BusiException("GPS号参数[gps_id]是必须的！");
		}
		return this.getService().getTrackVOs(pk_entrust, gps_id);
	}

	/**
	 * 返回车辆的当前位置信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCurrentTracking.json")
	@ResponseBody
	public RootVO getCurrentTracking(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		if(StringUtils.isBlank(pk_entrust)) {
			throw new BusiException("委托单参数[pk_entrust]是必须的！");
		}
		String carno = request.getParameter("carno");
		String pk_driver = request.getParameter("pk_driver");
		if(StringUtils.isBlank(carno) && StringUtils.isBlank(pk_driver)) {
			throw new BusiException("车牌号参数[carno]或者司机参数[pk_driver]是必须的！");
		}
		return this.getService().getCurrentTrackVO(pk_entrust, carno,pk_driver);
	}

	@RequestMapping(value = "/loadTrackingInvoice.json")
	@ResponseBody
	public Map<String, Object> loadTrackingInvoice(HttpServletRequest request, HttpServletResponse response) {
		String entrust_vbillno = request.getParameter("entrust_vbillno");
		if(StringUtils.isBlank(entrust_vbillno)) {
			throw new BusiException("委托单号的参数不能为空，参数名称[entrust_vbillno]！");
		}
		List<Map<String, Object>> list = this.getService().loadTrackingInvoice(entrust_vbillno);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	@RequestMapping(value = "/saveOperation.json")
	@ResponseBody
	public void saveOperation(HttpServletRequest request, HttpServletResponse response){
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		String pk_entrust = request.getParameter("pk_entrust");
		//这个billVO里并没有parentVO里的信息
		JsonNode updates = JacksonUtils.readTree(json).get(Constants.BODY).get("ts_ent_operation_b").get("update");
		EntOperationBVO[] entOperationBVOs = null;
		if(updates != null && updates.size() > 0){
			entOperationBVOs = new EntOperationBVO[updates.size()];
			for(int i=0; i<updates.size();i++){
				entOperationBVOs[i] = JacksonUtils.readValue(updates.get(i), EntOperationBVO.class);
			}
		}
		this.getService().saveOperation(entOperationBVOs,pk_entrust,paramVO);
		
	}
	

	/**
	 * 根据委托单pk加载运力信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadEntOperationB.json")
	@ResponseBody
	public Map<String, Object> loadEntOperationB(HttpServletRequest request, HttpServletResponse response) {
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> list = this.getService().loadEntOperationB(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}

}
