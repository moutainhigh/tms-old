package com.tms.web.httpEdi.yusen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.dao.NWDao;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.BillStatus;
import com.tms.vo.cm.PayCheckSheetVO;
import com.tms.vo.cm.ReceCheckSheetVO;

@Controller
@RequestMapping(value = "/public/httpEdi/yusen")
public class YusenEDIController extends BaseEdiController{
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/importRDInvoiceNo.do")
	@ResponseBody
	public String importRDInvoiceNo(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> result = new HashMap<String, Object>();
		logger.info("开始同步应收发票号回传TMS");
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			result.put("success", false);	
			result.put("errorMsg", error);
			logger.info("应收发票号回传TMS同步失败：" + error);
			return JacksonUtils.writeValueAsString(result);
		}
		String jsonDatas = request.getParameter("jsonDatas");
		if(StringUtils.isBlank(jsonDatas)){
			result.put("success", false);	
			result.put("errorMsg", "缺少Json数据");
			logger.info("应收发票号回传TMS同步失败：缺少Json数据");
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode jsonNodes = null;
		try {
			jsonNodes = JacksonUtils.readTree(jsonDatas);
			if(jsonNodes == null || jsonNodes.size() == 0){
				result.put("success", false);	
				result.put("errorMsg", "Json格式不正确");
				logger.info("应收发票号回传TMS同步失败：Json格式不正确");
				return JacksonUtils.writeValueAsString(result);
			}
		} catch (Exception e) {
			result.put("success", false);	
			result.put("errorMsg", e.getMessage());
			logger.info("应收发票号回传TMS同步失败：" + e.getMessage());
			return JacksonUtils.writeValueAsString(result);
		}
		List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
		for(JsonNode jsonNode : jsonNodes){
			Map<String,Object> unitResult = new HashMap<String, Object>();
			Map<String,String> info = JacksonUtils.readValue(jsonNode, HashMap.class);
			String vbillno = info.get("vbillno");
			String check_no = info.get("check_no");
			String got_amount = info.get("got_amount");
			if(StringUtils.isBlank(vbillno)){
				unitResult.put("success", false);
				unitResult.put("errorMsg", "应收单号为空");
				datas.add(unitResult);
				continue;
			}
			if(StringUtils.isBlank(check_no)){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "发票号为空");
				datas.add(unitResult);
				continue;
			}
			if(StringUtils.isBlank(got_amount)){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "金额为空");
				datas.add(unitResult);
				continue;
			}
			ReceCheckSheetVO sheetVO = NWDao.getInstance().queryByCondition(ReceCheckSheetVO.class, "vbillstatus=? AND vbillno=?",BillStatus.RCS_CONFIRM, vbillno);
			if(sheetVO == null){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "没有符合条件的应收");
				datas.add(unitResult);
				continue;
			}else{
				sheetVO.setStatus(VOStatus.UPDATED);
				sheetVO.setCheck_no(check_no);
				sheetVO.setCheck_amount(new UFDouble(got_amount));
				sheetVO.setIf_check(UFBoolean.TRUE);
				NWDao.getInstance().saveOrUpdate(sheetVO);
			}
			unitResult.put("orderno", vbillno);
			unitResult.put("success", true);
			datas.add(unitResult);
		}
		result.put("success", true);	
		result.put("datas", datas);
		return JacksonUtils.writeValueAsString(result);
	}

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/importPDInvoiceNo.do")
	@ResponseBody
	public String importPDInvoiceNo(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> result = new HashMap<String, Object>();
		logger.info("开始同步应付发票号回传TMS");
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			result.put("success", false);	
			result.put("errorMsg", error);
			logger.info("应付发票号回传TMS同步失败：" + error);
			return JacksonUtils.writeValueAsString(result);
		}
		String jsonDatas = request.getParameter("jsonDatas");
		if(StringUtils.isBlank(jsonDatas)){
			result.put("success", false);	
			result.put("errorMsg", "缺少Json数据");
			logger.info("应付发票号回传TMS同步失败：缺少Json数据");
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode jsonNodes = null;
		try {
			jsonNodes = JacksonUtils.readTree(jsonDatas);
			if(jsonNodes == null || jsonNodes.size() == 0){
				result.put("success", false);	
				result.put("errorMsg", "Json格式不正确");
				logger.info("应付发票号回传TMS同步失败：Json格式不正确");
				return JacksonUtils.writeValueAsString(result);
			}
		} catch (Exception e) {
			result.put("success", false);	
			result.put("errorMsg", e.getMessage());
			logger.info("应付发票号回传TMS同步失败：" + e.getMessage());
			return JacksonUtils.writeValueAsString(result);
		}
		List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
		for(JsonNode jsonNode : jsonNodes){
			Map<String,Object> unitResult = new HashMap<String, Object>();
			Map<String,String> info = JacksonUtils.readValue(jsonNode, HashMap.class);
			String vbillno = info.get("vbillno");
			String check_no = info.get("check_no");
			String got_amount = info.get("got_amount");
			if(StringUtils.isBlank(vbillno)){
				unitResult.put("success", false);
				unitResult.put("errorMsg", "应付单号为空");
				datas.add(unitResult);
				continue;
			}
			if(StringUtils.isBlank(check_no)){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "发票号为空");
				datas.add(unitResult);
				continue;
			}
			if(StringUtils.isBlank(got_amount)){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "金额为空");
				datas.add(unitResult);
				continue;
			}
			PayCheckSheetVO sheetVO = NWDao.getInstance().queryByCondition(PayCheckSheetVO.class, "vbillstatus=? AND vbillno=?",BillStatus.PCS_CONFIRM, vbillno);
			if(sheetVO == null){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "没有符合条件的应付");
				datas.add(unitResult);
				continue;
			}else{
				sheetVO.setStatus(VOStatus.UPDATED);
				sheetVO.setCheck_no(check_no);
				sheetVO.setCheck_amount(new UFDouble(got_amount));
				sheetVO.setIf_check(UFBoolean.TRUE);
				NWDao.getInstance().saveOrUpdate(sheetVO);
			}
			unitResult.put("orderno", vbillno);
			unitResult.put("success", true);
			datas.add(unitResult);
		}
		result.put("success", true);	
		result.put("datas", datas);
		return JacksonUtils.writeValueAsString(result);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/revokeRDInvoiceNo.do")
	@ResponseBody
	public String revokeRDInvoiceNo(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> result = new HashMap<String, Object>();
		logger.info("开始同步应收发票号撤销TMS");
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			result.put("success", false);	
			result.put("errorMsg", error);
			logger.info("应收发票号撤销TMS同步失败：" + error);
			return JacksonUtils.writeValueAsString(result);
		}
		String jsonDatas = request.getParameter("jsonDatas");
		if(StringUtils.isBlank(jsonDatas)){
			result.put("success", false);	
			result.put("errorMsg", "缺少Json数据");
			logger.info("应收发票号撤销TMS同步失败：缺少Json数据");
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode jsonNodes = null;
		try {
			jsonNodes = JacksonUtils.readTree(jsonDatas);
			if(jsonNodes == null || jsonNodes.size() == 0){
				result.put("success", false);	
				result.put("errorMsg", "Json格式不正确");
				logger.info("应收发票号撤销TMS同步失败：Json格式不正确");
				return JacksonUtils.writeValueAsString(result);
			}
		} catch (Exception e) {
			result.put("success", false);	
			result.put("errorMsg", e.getMessage());
			logger.info("应收发票号撤销TMS同步失败：" + e.getMessage());
			return JacksonUtils.writeValueAsString(result);
		}
		List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
		for(JsonNode jsonNode : jsonNodes){
			Map<String,Object> unitResult = new HashMap<String, Object>();
			Map<String,String> info = JacksonUtils.readValue(jsonNode, HashMap.class);
			String vbillno = info.get("vbillno");
			String cancel_time = info.get("cancel_time");
			String cancel_memo = info.get("cancel_memo");
			String cancel_user = info.get("cancel_user");
			if(StringUtils.isBlank(vbillno)){
				unitResult.put("success", false);
				unitResult.put("errorMsg", "应收单号为空");
				datas.add(unitResult);
				continue;
			}
			ReceCheckSheetVO sheetVO = NWDao.getInstance().queryByCondition(ReceCheckSheetVO.class, "vbillstatus=? AND vbillno=?",BillStatus.RCS_CONFIRM, vbillno);
			if(sheetVO == null){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "没有符合条件的应收");
				datas.add(unitResult);
				continue;
			}else{
				sheetVO.setStatus(VOStatus.UPDATED);
				sheetVO.setDef1(cancel_time);
				sheetVO.setDef2(cancel_memo);
				sheetVO.setDef3(cancel_user);
				NWDao.getInstance().saveOrUpdate(sheetVO);
			}
			unitResult.put("orderno", vbillno);
			unitResult.put("success", true);
			datas.add(unitResult);
		}
		result.put("success", true);	
		result.put("datas", datas);
		return JacksonUtils.writeValueAsString(result);
	}

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/revokePDInvoiceNo.do")
	@ResponseBody
	public String revokePDInvoiceNo(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> result = new HashMap<String, Object>();
		logger.info("开始同步应付发票号撤销TMS");
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			result.put("success", false);	
			result.put("errorMsg", error);
			logger.info("应付发票号撤销TMS同步失败：" + error);
			return JacksonUtils.writeValueAsString(result);
		}
		String jsonDatas = request.getParameter("jsonDatas");
		if(StringUtils.isBlank(jsonDatas)){
			result.put("success", false);	
			result.put("errorMsg", "缺少Json数据");
			logger.info("应付发票号撤销TMS同步失败：缺少Json数据");
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode jsonNodes = null;
		try {
			jsonNodes = JacksonUtils.readTree(jsonDatas);
			if(jsonNodes == null || jsonNodes.size() == 0){
				result.put("success", false);	
				result.put("errorMsg", "Json格式不正确");
				logger.info("应付发票号撤销TMS同步失败：Json格式不正确");
				return JacksonUtils.writeValueAsString(result);
			}
		} catch (Exception e) {
			result.put("success", false);	
			result.put("errorMsg", e.getMessage());
			logger.info("应付发票号撤销TMS同步失败：" + e.getMessage());
			return JacksonUtils.writeValueAsString(result);
		}
		List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
		for(JsonNode jsonNode : jsonNodes){
			Map<String,Object> unitResult = new HashMap<String, Object>();
			Map<String,String> info = JacksonUtils.readValue(jsonNode, HashMap.class);
			String vbillno = info.get("vbillno");
			String cancel_time = info.get("cancel_time");
			String cancel_memo = info.get("cancel_memo");
			String cancel_user = info.get("cancel_user");
			if(StringUtils.isBlank(vbillno)){
				unitResult.put("success", false);
				unitResult.put("errorMsg", "应付单号为空");
				datas.add(unitResult);
				continue;
			}
			PayCheckSheetVO sheetVO = NWDao.getInstance().queryByCondition(PayCheckSheetVO.class, "vbillstatus=? AND vbillno=?",BillStatus.PCS_CONFIRM, vbillno);
			if(sheetVO == null){
				unitResult.put("orderno", vbillno);
				unitResult.put("success", false);
				unitResult.put("errorMsg", "没有符合条件的应付");
				datas.add(unitResult);
				continue;
			}else{
				sheetVO.setStatus(VOStatus.UPDATED);
				sheetVO.setDef1(cancel_time);
				sheetVO.setDef2(cancel_memo);
				sheetVO.setDef3(cancel_user);
				NWDao.getInstance().saveOrUpdate(sheetVO);
			}
			unitResult.put("orderno", vbillno);
			unitResult.put("success", true);
			datas.add(unitResult);
		}
		result.put("success", true);	
		result.put("datas", datas);
		return JacksonUtils.writeValueAsString(result);
	}
	
	
	
}
