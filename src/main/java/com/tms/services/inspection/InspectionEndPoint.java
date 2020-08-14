package com.tms.services.inspection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.nw.dao.NWDao;
import org.nw.exception.ApiException;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.service.api.AuthenticationService;
import org.nw.vo.HYBillVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

import com.tms.service.cm.InspectionService;
import com.tms.vo.cm.InspectionBVO;
import com.tms.vo.cm.InspectionVO;

@SuppressWarnings("deprecation")
public class InspectionEndPoint extends ServletEndpointSupport {
	
	Logger logger = Logger.getLogger("EDI");
	
	InspectionService inspectionService = SpringContextHolder.getBean("inspectionServiceImpl");
	
	// 导入前的检查
	private String checkBeforeImport(String uid, String pwd) throws ApiException {
		AuthenticationService authenticationService = SpringContextHolder.getBean("authenticationService");
		if (authenticationService == null) {
			throw new BusiException("验证服务没有启动，服务ID:AuthenticationService");
		}
		String authError = authenticationService.auth(uid, pwd);

		if (!authError.isEmpty()) {
			return authError;
		}
		return "";
	}

	public String ImportInspection(String uid,String pwd,String jsonInspections) throws ApiException{
		logger.info("开始同步协查数据：" + jsonInspections);
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		String authError = checkBeforeImport(uid,pwd);
		if(!authError.isEmpty()){
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", authError);
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode inspections = JacksonUtils.readTree(jsonInspections);
		if(inspections == null || inspections.size() == 0){
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "没有输入数据！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		//按照code分组
		Map<String,List<JsonNode>> groupMap = new HashMap<String, List<JsonNode>>();
		for (JsonNode inspection : inspections){
			String code = inspection.get("code") == null ? null : inspection.get("code").getTextValue();
			if(StringUtils.isBlank(code)){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "code不能为空");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			List<JsonNode> inspectionInfos = groupMap.get(code);
			if(inspectionInfos == null){
				inspectionInfos = new ArrayList<JsonNode>();
				groupMap.put(code, inspectionInfos);
			}
			inspectionInfos.add(inspection);
		}
		for(String key : groupMap.keySet()){
			List<JsonNode> inspectionInfos = groupMap.get(key);
			InspectionBVO[] inspectionBVOs = new InspectionBVO[inspectionInfos.size()];
			int index = 0;
			for(JsonNode inspection : inspectionInfos){
				InspectionBVO inspectionBVO = new InspectionBVO();
				inspectionBVO.setStatus(VOStatus.NEW);
				inspectionBVO.setFee_code(inspection.get("feeCode") == null ? null : inspection.get("feeCode").getTextValue());
				inspectionBVO.setModifiedvalue(inspection.get("modifiedValue") == null ? null : inspection.get("modifiedValue").getTextValue());
				inspectionBVO.setType(inspection.get("type") == null ? null : inspection.get("type").getTextValue());
				inspectionBVOs[index] = inspectionBVO;
				index ++;
			}
			InspectionVO inspectionVO = new InspectionVO();
			inspectionVO.setStatus(VOStatus.NEW);
			inspectionVO.setCode(key);
			inspectionVO.setOrderno(inspectionInfos.get(0).get("type") == null ? null : inspectionInfos.get(0).get("type").getTextValue());
			inspectionVO.setSubmituser(inspectionInfos.get(0).get("submitUser") == null ? null : inspectionInfos.get(0).get("submitUser").getTextValue());
			inspectionVO.setDirector(inspectionInfos.get(0).get("director") == null ? null : inspectionInfos.get(0).get("director").getTextValue());
			inspectionVO.setOrderno(inspectionInfos.get(0).get("department") == null ? null : inspectionInfos.get(0).get("department").getTextValue());
			inspectionVO.setReason(inspectionInfos.get(0).get("reason") == null ? null : inspectionInfos.get(0).get("reason").getTextValue());
			inspectionVO.setCreate_time(new UFDateTime(new Date()));
			inspectionVO.setCreate_user("32e6103e697f44b7ac98477583af49cd");
			inspectionVO.setPk_corp("0001");
			HYBillVO hyBillVO = new HYBillVO();
			hyBillVO.setParentVO(inspectionVO);
			hyBillVO.setChildrenVO(inspectionBVOs);
			NWDao.setUuidPrimaryKey(hyBillVO);
			NWDao.getInstance().saveOrUpdate(hyBillVO);
		}
		Map<String,Object> resultMap = new HashMap<String, Object>();
		resultMap.put("success", true);
		resultMap.put("errorMessage", null);
		resultMap.put("entityKey", "");
		resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
		resultMap.put("serviceType", "TMS");
		result.add(resultMap);
		return JacksonUtils.writeValueAsString(result);
	}
}
