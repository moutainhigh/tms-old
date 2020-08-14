package com.tms.web.fleet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.basic.util.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.service.sys.DataDictService;
import org.nw.utils.FormulaHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.fleet.VehicleManagerService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.DriverVO;

@Controller
@RequestMapping("/fleet/vm")
public class VehicleManagerController extends AbsBillController {
	
	@Autowired
	private DataDictService dataDictService; 
	
	@Autowired
	private VehicleManagerService vehicleManagerService;
	
	@Override
	public VehicleManagerService getService() {
		return vehicleManagerService;
	}

	/**
	 * 提交按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/commit.json")
	@ResponseBody
	public Map<String, Object> commit(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new RuntimeException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().commit(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量提交时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未提交成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new RuntimeException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 反提交按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/uncommit.json")
	@ResponseBody
	public Map<String, Object> uncommit(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new RuntimeException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().uncommit(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量反提交时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未反提交成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new RuntimeException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}
	
	@RequestMapping(value = "/getChoice.json")
	@ResponseBody
	public Map<String, Object> getChoice(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put(Constants.TEXT, "同意");
		map2.put(Constants.VALUE, 1);
		list.add(map2);
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put(Constants.TEXT, "拒绝");
		map1.put(Constants.VALUE, 0);
		list.add(map1);
		recordsMap.put("records", list);
		return recordsMap;
	}
	
	@RequestMapping(value = "/getReasonType.json")
	@ResponseBody
	public Map<String, Object> getReasonType(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("vm_refuse_reason_type");
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		if(billVO != null) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null) {
				for(CircularlyAccessibleValueObject cvo : cvos) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(Constants.TEXT, cvo.getAttributeValue(DataDictBVO.DISPLAY_NAME));
					map.put(Constants.VALUE, cvo.getAttributeValue(DataDictBVO.VALUE));
					list.add(map);
				}
				recordsMap.put("records", list);
			}
		}
		return recordsMap;
	}
	
	@RequestMapping(value = "/getCarno.json")
	@ResponseBody
	public Map<String, Object> getCarno(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		CarVO[] carVOs = this.getService().getCarno();
		if(carVOs != null && carVOs.length != 0){
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(CarVO carVO : carVOs){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(Constants.TEXT, carVO.getCarno());
				map.put(Constants.VALUE, carVO.getPk_car());
				list.add(map);
			}
			recordsMap.put("records", list);
		}
		return recordsMap;
	}
	
	@RequestMapping(value = "/getDriver.json")
	@ResponseBody
	public Map<String, Object> getDriver(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		DriverVO[] driverVOs = this.getService().getDriver();
		if(driverVOs != null && driverVOs.length != 0){
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(DriverVO driverVO : driverVOs){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(Constants.TEXT, driverVO.getDriver_code());
				map.put(Constants.VALUE, driverVO.getPk_driver());
				list.add(map);
			}
			recordsMap.put("records", list);
		}
		return recordsMap;
	}
	
	@RequestMapping(value = "/getAddr.json")
	@ResponseBody
	public Map<String, Object> getAddr(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		 AddressVO[] addressVOs = this.getService().getAddr();
		if(addressVOs != null && addressVOs.length != 0){
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(AddressVO addressVO : addressVOs){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(Constants.TEXT, addressVO.getAddr_code());
				map.put(Constants.VALUE, addressVO.getPk_address());
				list.add(map);
			}
			recordsMap.put("records", list);
		}
		return recordsMap;
	}
	
	@RequestMapping(value = "/vmcheck.json")
	@ResponseBody
	public Map<String, Object> vmcheck(HttpServletRequest request, HttpServletResponse response) {
		String[] billIds = request.getParameterValues("ids");
		String choice = request.getParameter("choice");
		String reason_type = request.getParameter("reason_type");
		String memo = request.getParameter("memo");
		if(billIds == null || billIds.length == 0 || billIds[0] == null || billIds[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billIds) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().vmcheck(id, Integer.parseInt(choice),
						(StringUtils.isBlank(reason_type)? null : Integer.parseInt(reason_type)),memo);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量审核时存在异常，单据ID：" + id, e);
				num++;
				result.append(ONE_LINE_BILL_MSG.replace("$billId", "").replace("$msg", e.getMessage()));
			}
		}
		
		if(result.length() > 0) {
			result.insert(0, "以下单据未审核成功：<br/>");
			if(num == billIds.length) {
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}
	
	@RequestMapping(value = "/vmrecheck.json")
	@ResponseBody
	public Map<String, Object> vmrecheck(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new RuntimeException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().vmrecheck(id);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量重审时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new RuntimeException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}
	
	@RequestMapping(value = "/vmsend.json")
	@ResponseBody
	public Map<String, Object> vmsend(HttpServletRequest request, HttpServletResponse response) {
		String[] billIds = request.getParameterValues("ids");
		String carno = request.getParameter("carno");
		String main_driver = request.getParameter("main_driver");
		String deputy_drive = request.getParameter("deputy_drive");
		String memo = request.getParameter("memo");
		if(billIds == null || billIds.length == 0 || billIds[0] == null || billIds[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		for(String billId :  billIds){
			try {
				Map<String, Object> retMap = this.getService().vmsend(billId,carno,main_driver,deputy_drive,memo);
				list.add(retMap);
			} catch (Exception e) {
				result.append(ONE_LINE_BILL_MSG.replace("$billId", "").replace("$msg", e.getMessage()));
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未派车成功：<br/>");
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
		
	}
	
	@RequestMapping(value = "/vmdispatch.json")
	@ResponseBody
	public Map<String, Object> vmdispatch(HttpServletRequest request, HttpServletResponse response) {
		String[] billIds = request.getParameterValues("ids");
		String watch = request.getParameter("mileage");
		String gps = request.getParameter("gps");
		String fule = request.getParameter("fule");
		String time = request.getParameter("dispatch_time");
		String addr = request.getParameter("addr");
		String memo = request.getParameter("memo");
		if(billIds == null || billIds.length == 0 || billIds[0] == null || billIds[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		Map<String, Object> retMap = this.getService().vmdispatch(billIds[0],watch,gps,fule,time,addr,memo);
		return this.genAjaxResponse(true, null, retMap);
	}
	
	@RequestMapping(value = "/vmreturn.json")
	@ResponseBody
	public Map<String, Object> vmreturn(HttpServletRequest request, HttpServletResponse response) {
		String[] billIds = request.getParameterValues("ids");
		String watch = request.getParameter("mileage");
		String gps = request.getParameter("gps");
		String fule = request.getParameter("fule");
		String time = request.getParameter("return_time");
		String addr = request.getParameter("addr");
		String memo = request.getParameter("memo");
		if(billIds == null || billIds.length == 0 || billIds[0] == null || billIds[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		Map<String, Object> retMap = this.getService().vmreturn(billIds[0],watch,gps,fule,time,addr,memo);
		return this.genAjaxResponse(true, null, retMap);
	}
	
	@RequestMapping(value = "/afterEditCarno.json")
	@ResponseBody
	public Map<String, Object> afterEditCarno(HttpServletRequest request, HttpServletResponse response) {
		String pk_car = request.getParameter("pk_car");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pk_car", pk_car);
		List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
		context.add(map);
		String[] formulas = new String[] {
				"pk_driver->getcolsvalue(ts_car,pk_carrier,pk_car,pk_car)",
				"driver_name->getColValue(ts_driver, driver_name, pk_driver, pk_driver)" };
		List<Map<String, Object>> retList = FormulaHelper.execFormula(context, formulas, true);
		return retList.get(0); // 这里实际上只返回一条记录
	}
	
	
}
