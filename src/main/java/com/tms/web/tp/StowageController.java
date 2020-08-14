package com.tms.web.tp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.utils.FormulaHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.constants.FunConst;
import com.tms.service.base.TransLineService;
import com.tms.service.te.EntrustService;
import com.tms.service.tp.StowageService;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 调度配载、运段配载操作
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/tp/sto")
public class StowageController extends AbsBillController {

	@Autowired
	private StowageService stowageService;

	@Autowired
	private TransLineService transLineService;
	
	@Autowired
	private EntrustService entrustService;

	public StowageService getService() {
		return stowageService;
	}
	
	/**
	 * 返回承运商树
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCarrData.json")
	@ResponseBody
	public String getCarrData(HttpServletRequest request, HttpServletResponse response, String parent_id) {
		String[] billIDs = request.getParameterValues("ids");
		List<Map<String, String>> result = stowageService.getCarrData(billIDs);
		return JacksonUtils.writeValueAsString(result);
	}

	/**
	 * 返回路线树
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getLineTree.json")
	@ResponseBody
	public List<TreeVO> getLineTree(HttpServletRequest request, HttpServletResponse response, String parent_id) {
		return stowageService.getLineTree();
	}

	/**
	 * 返回车辆树
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCarData.json")
	@ResponseBody
	public String getCarData(HttpServletRequest request, HttpServletResponse response, String parent_id) {
		String[] carrIds = request.getParameterValues("ids");
		List<Map<String,String>> result =  stowageService.getCarData(carrIds);
		return JacksonUtils.writeValueAsString(result);
	}

	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		// 读取运段明细在功能注册的url,设置到segPackUrl中
		String segPackUrl = this.getService().getFunVOByFunCode(FunConst.SEG_PACK_CODE).getClass_name();
		if(segPackUrl.indexOf("?") == -1) {
			segPackUrl += "?";
		} else {
			segPackUrl += "&";
		}
		segPackUrl += "funCode=" + FunConst.SEG_PACK_CODE;
		request.setAttribute("segPackUrl", segPackUrl); // 设置到前台
		return super.index(request, response);
	}

	/**
	 * 当选择了左边的路线时需要根据路线匹配
	 */
	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		String[] lineIds = request.getParameterValues("lineId");// 路线ID，可能是多 条
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		String params = request.getParameter(Constants.PUB_QUERY_PARAMETER);
//		StringBuffer buf = new StringBuffer();
//		if(lineIds != null){
//			buf.append("pzLine")
//		}
//		if(lineIds != null) {
//			int index = 0;
//			for(String lineId : lineIds) {
//				TransLineVO lineVO = transLineService.getByPrimaryKey(TransLineVO.class, lineId);
//				String condString = this.getService().getCondString(lineVO);
//				if(StringUtils.isNotBlank(condString)) {
//					if(index != 0) {
//						buf.append(" or ");
//					}
//					buf.append("(");
//					buf.append(condString);
//					buf.append(")");
//					index++;
//				}
//			}
//		}
//		if(buf.length() > 0) {
//			buf.insert(0, "(");
//			buf.append(")");
//		}
		PaginationVO paginationVO = null;
		if(lineIds != null && lineIds.length > 0 && org.nw.basic.util.StringUtils.isNotBlanks(lineIds[0])){
			paginationVO = this.getService()
					.loadData(paramVO, offset, pageSize, null, orderBy,"pz_line in " + NWUtils.buildConditionString(lineIds));
		}else{
			paginationVO = this.getService()
					.loadData(paramVO, offset, pageSize, null, orderBy,"1=1");
		}
		
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 根据前台传入的pk值，返回数据，虽然返回PaginationVO，实际上这里是不分页的，一般传入的pk不会多
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadByPKs.json")
	@ResponseBody
	public Map<String, Object> loadByPKs(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		if(pk_segment == null || pk_segment.length == 0) {
			throw new BusiException("请先选择运段！");
		}
		List<SegmentVO> list = this.getService().loadByPKs(pk_segment);
		// 执行下公式
		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>(list.size());
		for(SuperVO vo : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				map.put(key, vo.getAttributeValue(key));
			}
			context.add(map);
		}
		List<Map<String, Object>> retList = FormulaHelper.execFormula(context, getFormulas(), true);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(retList);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 起始地，目的地，起始地城市，目的地城市
	 * 
	 * @return
	 */
	private String[] getFormulas() {
		return new String[] { "pk_delivery_name->getcolvalue(ts_address,addr_name,pk_address,pk_delivery)",
				"pk_arrival_name->getcolvalue(ts_address,addr_name,pk_address,pk_arrival)",
				"deli_city->getcolvalue(ts_area,name,pk_area,deli_city)",
				"arri_city->getcolvalue(ts_area,name,pk_area,arri_city)" };
	}

	/**
	 * 加载运段的产品包装明细，用于分量显示
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadSegPackByParent.json")
	@ResponseBody
	public Map<String, Object> loadSegPackByParent(HttpServletRequest request, HttpServletResponse response) {
		String pk_segment = request.getParameter("pk_segment");
		if(StringUtils.isBlank(pk_segment)) {
			throw new BusiException("请先选择运段！");
		}
		List<Map<String, Object>> list = this.getService().loadSegPackByParent(pk_segment);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 分段界面-编辑地址编码后发送的请求，实际上是执行公式，返回城市，详细地址
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/afterEditAddrCode.json")
	@ResponseBody
	public Map<String, Object> afterEditAddrCode(HttpServletRequest request, HttpServletResponse response) {
		String pk_address = request.getParameter("pk_address");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pk_address", pk_address);
		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		context.add(map);
		String[] formulas = new String[] {
				"addr_name,detail_addr,pk_city->getcolsvalue(ts_address,addr_name,detail_addr,pk_city,pk_address,pk_address)",
				"city_name->getColValue(ts_area, name, pk_area, pk_city)" };
		List<Map<String, Object>> retList = FormulaHelper.execFormula(context, formulas, true);
		return retList.get(0); // 这里实际上只返回一条记录
	}

	/**
	 * 分段操作
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/distSection.json")
	@ResponseBody
	public Map<String, Object> distSection(HttpServletRequest request, HttpServletResponse response) {
		String[] HEADER = request.getParameterValues(Constants.HEADER);
		String[] req_deli_date = request.getParameterValues("req_deli_date");// 要求离开日期
		String[] req_arri_date = request.getParameterValues("req_arri_date"); // 要求到货日期
		//验证传入时间顺序是否正确
		for(int i=0;i<req_deli_date.length-1;i++){
			UFDateTime deli_date = new UFDateTime(req_deli_date[i]);
			UFDateTime arri_date = new UFDateTime(req_arri_date[i+1]);
			if(deli_date.after(arri_date)){
				throw new BusiException("时间顺序有误</br>?</br>?",req_deli_date[i],req_arri_date[i+1]);
			}
		}
		
		if(HEADER == null || HEADER.length == 0) {
			throw new BusiException("请先选择节点！");
		}
		String[] BODY = request.getParameterValues(Constants.BODY);
		if(BODY == null || BODY.length == 0) {
			throw new BusiException("你没有选择任何运段！");
		}
		return this.genAjaxResponse(true, null,
				this.getService().saveDistSection(HEADER, req_deli_date, req_arri_date, BODY));
	}

	/**
	 * 自动分段
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @deprecated 在发货单界面直接自动分段了
	 */
	@RequestMapping(value = "/autoDistSection.json")
	@ResponseBody
	public Map<String, Object> autoDistSection(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		return this.genAjaxResponse(true, null, this.getService().saveAutoDistSection(pk_segment));
	}

	/**
	 * 撤销分段
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/cancelSection.json")
	@ResponseBody
	public Map<String, Object> cancelSection(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		return this.genAjaxResponse(true, null, this.getService().saveCancelSection(pk_segment));
	}

	/**
	 * 分量
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/distQuantity.json")
	@ResponseBody
	public Map<String, Object> distQuantity(HttpServletRequest request, HttpServletResponse response) {
		String pk_segment = request.getParameter("pk_segment");
		String[] pk_seg_pack_b = request.getParameterValues("pk_seg_pack_b");
		String[] sDist_pack_num_count = request.getParameterValues("dist_pack_num_count");
		String[] sDist_num = request.getParameterValues("dist_num");
		String[] sDist_weight = request.getParameterValues("dist_weight");
		String[] sDist_volume = request.getParameterValues("dist_volume");
		// 这4个数组的长度实际上是一样的
		UFDouble[] dist_pack_num_count = new UFDouble[pk_seg_pack_b.length];
		int[] dist_num = new int[pk_seg_pack_b.length];
		UFDouble[] dist_weight = new UFDouble[pk_seg_pack_b.length];
		UFDouble[] dist_volume = new UFDouble[pk_seg_pack_b.length];
		for(int i = 0; i < pk_seg_pack_b.length; i++) {
			dist_pack_num_count[i] = new UFDouble(sDist_pack_num_count[i]);
			dist_num[i] = Integer.parseInt(sDist_num[i]);
			dist_weight[i] = new UFDouble(sDist_weight[i]);
			dist_volume[i] = new UFDouble(sDist_volume[i]);
		}
		return this.genAjaxResponse(
				true,
				null,
				this.getService().saveDistQuantity(pk_segment, pk_seg_pack_b, dist_pack_num_count, dist_num,
						dist_weight, dist_volume));
	}

	/**
	 * 撤销分量
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/cancelQuantity.json")
	@ResponseBody
	public Map<String, Object> cancelQuantity(HttpServletRequest request, HttpServletResponse response) {
		String[] pk_segment = request.getParameterValues("pk_segment");
		return this.genAjaxResponse(true, null, this.getService().saveCancelQuantity(pk_segment));
	}

	/**
	 * 委派
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/delegate.json")
	@ResponseBody
	public Map<String, Object> delegate(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billIds = request.getParameterValues("billId");
		String pk_corp = request.getParameter("pk_corp");
		if(StringUtils.isBlank(pk_corp)) {
			throw new BusiException("请选择要委派的公司！");
		}
		if(billIds == null || billIds.length == 0) {
			throw new BusiException("请选择要委派的运段！");
		}
		List<Map<String, Object>> retList = this.getService().doDelegate(pk_corp, billIds, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 撤销委派
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/cancelDelegate.json")
	@ResponseBody
	public Map<String, Object> cancelDelegate(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billIds = request.getParameterValues("billId");
		if(billIds == null || billIds.length == 0) {
			throw new BusiException("请先选择要撤销委派的运段！");
		}
		List<Map<String, Object>> retList = this.getService().cancelDelegate(billIds, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}
	
	
	//yaojiie 2015 10 16 增加车辆GPS定位功能
	@RequestMapping(value = "/getCurrentTracking.json")
	@ResponseBody
	public RootVO getCurrentTracking(HttpServletRequest request, HttpServletResponse response) {
		String carno = request.getParameter("carno");
		if(StringUtils.isBlank(carno)) {
			throw new BusiException("车牌号参数[carno]是必须的！");
		}
		return this.getService().getCurrentTrackVO(carno);
	}
	
	/**
	 * 同步订单  songf 2015-12-14
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/syncOrders.json")
	@ResponseBody
	public void syncOrders(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billIds = request.getParameterValues("billId");
		if(billIds == null || billIds.length == 0) {
			throw new BusiException("请先选择要同步的订单！");
		}
		this.getService().syncOrders(billIds, paramVO);
		
	}
	
	@RequestMapping(value = "/delegateCarrier.json")
	@ResponseBody
	public Map<String, Object> delegateCarrier(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billIds = request.getParameterValues("ids");
		String carrier = request.getParameter("carrier");
		boolean if_email =  false;
		if(StringUtils.isNotBlank(request.getParameter("if_email"))
				&& request.getParameter("if_email").equals("Y")){
			if_email = true;
		}
		
		if(billIds == null || billIds.length == 0) {
			throw new BusiException("请先选择要委派的订单！");
		}
		if(StringUtils.isBlank(carrier)) {
			throw new BusiException("请先选择承运商！");
		}
		List<Map<String, Object>> retList = this.getService().delegateCarrier(billIds,carrier,paramVO);
		if(if_email){
			entrustService.sendEntEmail(billIds,"t30217");
		}
		return this.genAjaxResponse(true, null, retList);
	}
	
	
	@RequestMapping(value = "/getFullLoadRates.json")
	@ResponseBody
	public Map<String, String> getFullLoadRates(HttpServletRequest request, HttpServletResponse response){
		String[] ids = request.getParameterValues("ids");
		String[] cars = request.getParameterValues("cars");
		return this.getService().getFullLoadRates(ids,cars);
	}
	
	@RequestMapping(value = "/loadProcDatas.json")
	@ResponseBody
	public Map<String, Object> loadProcDatas(HttpServletRequest request, HttpServletResponse response){
		String[] ids = request.getParameterValues("vbillos[]");
		String pk_car = request.getParameter("pk_car");
		return this.getService().getProcDatas(ids,pk_car);
	}
	
	@RequestMapping(value = "/getSegments.json")
	@ResponseBody
	public List<SegmentVO> getSegments(HttpServletRequest request, HttpServletResponse response){
		String[] ids = request.getParameterValues("ids");
		return this.getService().getSegments(ids);
	}
	
	@RequestMapping(value = "/mergeSegments.json")
	@ResponseBody
	public Map<String,Object> mergeSegments(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billIds = request.getParameterValues("billId");
		if(billIds == null || billIds.length == 0) {
			throw new BusiException("请至少选择两条记录！");
		}
		return this.genAjaxResponse(true, null, this.getService().mergeSegments(billIds, paramVO));
		
	}
	
	@RequestMapping(value = "/dispatch.json")
	@ResponseBody
	public Map<String,Object> dispatch(HttpServletRequest request, HttpServletResponse response) {
		String pk_car = request.getParameter("checked_car");
		String pk_carrier = request.getParameter("checked_carrier");
		String[] inv_vbillnos = request.getParameterValues("inv_vbillnos");
		String[] ids = request.getParameterValues("ids");
		if(StringUtils.isBlank(pk_car) && StringUtils.isBlank(pk_carrier)) {
			throw new BusiException("请先选择承运商或者车辆！");
		}
		if(inv_vbillnos == null || inv_vbillnos.length == 0){
			throw new BusiException("请先选择运段！");
		}
		List<SegmentVO> segmentVOs  = this.getService().getSegments(inv_vbillnos);
		if(ids == null || ids.length == 0){
			throw new BusiException("请先选择运段！");
		}
		if(segmentVOs != null && segmentVOs.size() > 0){
			Set<String> pk_segments = new HashSet<String>(Arrays.asList(ids));
			for(SegmentVO segmentVO : segmentVOs ){
				pk_segments.add(segmentVO.getPk_segment());
			}
			ids = pk_segments.toArray(new String[pk_segments.size()]);
		}
		this.getService().dispatch(pk_car, pk_carrier, ids);
		return this.genAjaxResponse(true, null, null);
	}
	
}
