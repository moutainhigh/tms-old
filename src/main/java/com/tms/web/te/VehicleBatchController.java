package com.tms.web.te;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.utils.FormulaHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tms.constants.FunConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.fleet.AnnualService;
import com.tms.service.te.VehicleBatchService;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.te.VehiclePayVO;
import com.tms.vo.tp.SegmentVO;

@Controller
@RequestMapping("/te/vb")
public class VehicleBatchController extends AbsBillController {
	
	@Autowired
	private VehicleBatchService vehicleBatchService;
	@Override
	public VehicleBatchService getService() {
		return vehicleBatchService;
	}
	

	@RequestMapping(value = "/loadPackRecord.json")
	@ResponseBody
	public Map<String, Object> loadPackRecord(HttpServletRequest request, HttpServletResponse response){
		String pk_entrust = request.getParameter("pk_entrust");
		List<Map<String, Object>> result = this.getService().getPackRecord(pk_entrust);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(result);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	@RequestMapping("/authentication.json")
	@ResponseBody
	public Map<String,Object> authentication(HttpServletRequest request, HttpServletResponse response){
		String lot = request.getParameter("lot");
		String card_msg = request.getParameter("card_msg");
		return this.getService().authentication(lot, card_msg);
	}
	
	
	public void viewImage(HttpServletRequest request, HttpServletResponse response) {
		response.reset();
		response.resetBuffer();
		response.setContentType("image/png");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		String fileName = request.getParameter("fileName");
		if(StringUtils.isBlank(fileName)) {
			throw new RuntimeException("文件名称参数不能为空！");
		}
		String path = request.getSession().getServletContext().getRealPath("/") + "certificate" + File.separator + fileName;
		File destFile = new File(path);
		if(!destFile.exists()) {
			throw new RuntimeException("未能找到文件！");
		}
		try {
			/* 创建输出流 */
			ServletOutputStream servletOS = response.getOutputStream();
			FileInputStream inputStream = new FileInputStream(destFile);
			byte[] buf = new byte[1024];
			int readLength;
			while(((readLength = inputStream.read(buf)) != -1)) {
				servletOS.write(buf, 0, readLength); 
			}
			inputStream.close();
			servletOS.flush();
			servletOS.close();
		} catch(Exception e) {
			logger.error("文件下载出错," + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	@RequestMapping(value = "/loadPayDetail.json")
	@ResponseBody
	public Map<String,Object> loadPayDetail(HttpServletRequest request, HttpServletResponse response){
		String lot = request.getParameter("lot");
		List<Map<String,Object>> mapList = this.getService().loadPayDetail(lot);
		if(mapList == null || mapList.size() == 0){
			return this.genAjaxResponse(true, null, null);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.PAY_DETAIL_CODE);
		UiBillTempletVO uiBillTempletVO = this.getService().getBillTempletVO(this.getService().getBillTemplateID(paramVO));
		List<Map<String,Object>> list = this.getService().execFormula4Templet(mapList, uiBillTempletVO, true,new String[] { TabcodeConst.TS_PAY_DETAIL_B }, null);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
	}
	
	@RequestMapping(value = "/loadVehiclePay.json")
	@ResponseBody
	public Map<String,Object> loadVehiclePay(HttpServletRequest request, HttpServletResponse response){
		String lot = request.getParameter("lot");
		List<Map<String,Object>> mapList = this.getService().loadVehiclePay(lot);
		if(mapList == null || mapList.size() == 0){
			return this.genAjaxResponse(true, null, null);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode("t50608");
		UiBillTempletVO uiBillTempletVO = this.getService().getBillTempletVO(this.getService().getBillTemplateID(paramVO));
		List<Map<String,Object>> list = this.getService().execFormula4Templet(mapList, uiBillTempletVO, true,new String[] { TabcodeConst.TS_PAY_DETAIL_B }, null);
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return this.genAjaxResponse(true, null, paginationVO);
		
	}
	
	@RequestMapping("/saveVBLotPay.json")
	@ResponseBody
	public void saveVBLotPay(HttpServletRequest request, HttpServletResponse response){
		String json = request.getParameter(Constants.APP_POST_DATA);
		String lot = request.getParameter("lot");
		List<PayDetailBVO> detailBVOs = new ArrayList<PayDetailBVO>();
		JsonNode header = JacksonUtils.readTree(json);
		JsonNode headers = header.get(Constants.BODY).get(TabcodeConst.TS_PAY_DETAIL_B).get(Constants.UPDATE);
		for(JsonNode unitHeader : headers){
			PayDetailBVO detailBVO = new PayDetailBVO();
			 detailBVO.setPk_expense_type(unitHeader.get("pk_expense_type").getTextValue());
			 detailBVO.setValuation_type(unitHeader.get("valuation_type") == null ? null : unitHeader.get("valuation_type").getValueAsInt());
			 detailBVO.setQuote_type(unitHeader.get("quote_type") == null ? null : unitHeader.get("quote_type").getValueAsInt());
			 detailBVO.setPrice_type(unitHeader.get("price_type") == null ? null : unitHeader.get("price_type").getValueAsInt());
			 detailBVO.setPrice(new UFDouble(unitHeader.get("price") == null ? 0 : unitHeader.get("price").getValueAsDouble()));
			 detailBVO.setAmount(new UFDouble(unitHeader.get("amount").getValueAsDouble()));
			 detailBVO.setMemo(unitHeader.get("memo") == null ? null : unitHeader.get("memo").getTextValue());
			detailBVOs.add(detailBVO);
		}
		//这里组织数据
		this.getService().saveVBLotPay(detailBVOs,lot);
	}
	
	@RequestMapping("/saveVehiclePay.json")
	@ResponseBody
	public void saveVehiclePay(HttpServletRequest request, HttpServletResponse response){
		String json = request.getParameter(Constants.APP_POST_DATA);
		String lot = request.getParameter("lot");
		String kilometre = request.getParameter("kilometre");
		String days = request.getParameter("days");
		List<VehiclePayVO> vehiclePayVOs = new ArrayList<VehiclePayVO>();
		JsonNode header = JacksonUtils.readTree(json);
		JsonNode headers = header.get(Constants.BODY).get("ts_vehicle_pay").get(Constants.UPDATE);
		for(JsonNode unitHeader : headers){
			VehiclePayVO vehiclePayVO = JacksonUtils.readValue(unitHeader, VehiclePayVO.class);
			vehiclePayVO.setLot(lot);
			vehiclePayVO.setKilometre(new UFDouble(kilometre));
			vehiclePayVO.setDays(new UFDouble(days));
			vehiclePayVOs.add(vehiclePayVO);
		}
		//这里组织数据
		this.getService().saveVehiclePay(vehiclePayVOs);
	}

	@RequestMapping("/getKilometreAndDays.json")
	@ResponseBody
	public Map<String,Object> getKilometreAndDays(HttpServletRequest request, HttpServletResponse response){
		String lot = request.getParameter("lot");
		return this.getService().getKilometreAndDays(lot);
	}
}
