package com.tms.services.tmsStandardApi;

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
import org.nw.utils.CodenoHelper;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.PsndocVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

import com.tms.service.base.AddressService;
import com.tms.service.base.AreaService;
import com.tms.service.base.CarTypeService;
import com.tms.service.base.CarrService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.base.GoodsPackcorpVO;
import com.tms.vo.cm.ExpenseTypeVO;


@SuppressWarnings("deprecation")
public class ImportBasicDatasEndpoint extends ServletEndpointSupport {
	
	Logger logger = Logger.getLogger("EDI");
	
	private static String  EDI_USER_CODE = "EdiUser";
	private final static String TRUE = "TRUE";
	private final static String FALSE = "FALSE";
	
	private static	String ediUserSql = "SELECT * FROM nw_user WITH(NOLOCK) WHERE isnull(dr,0) = 0 AND isnull(locked_flag,'N')='N' AND user_code = ?";
	private static	UserVO ediUserVO = NWDao.getInstance().queryForObject(ediUserSql, UserVO.class, EDI_USER_CODE);
	
	private AddressService addrService = (AddressService) SpringContextHolder.getApplicationContext().getBean("addressServiceImpl");
	private AreaService areaService = (AreaService) SpringContextHolder.getApplicationContext().getBean("areaServiceImpl");
	private CarrService carrService = (CarrService) SpringContextHolder.getApplicationContext().getBean("carrServiceImpl");
	private CarTypeService carTypeService = (CarTypeService) SpringContextHolder.getApplicationContext().getBean("carTypeServiceImpl");
	
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
	public String ImportCorp(String uid,String pwd,String jsonCorps) throws ApiException{
		logger.info("开始同步公司数据：" + jsonCorps);
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
		// 空值判断
		if (jsonCorps.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode corps = JacksonUtils.readTree(jsonCorps);
		// 判断参数有效性
		if (corps == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newCorps = corps;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(corps, "corpConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newCorps = (JsonNode) convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		
		for (JsonNode corp : newCorps) {
			//判断主表信息
			String errorMsg = CheckCorpInfo(corp);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String def1 =corp.get("def1") == null ? null : corp.get("def1").getTextValue();
			Integer dr = corp.get("dr") == null ? 1 : corp.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String corp_code = corp.get("corp_code").getTextValue();
			String corp_name = corp.get("corp_name").getTextValue();
			String shortname = corp.get("shortname") == null ? null : corp.get("shortname").getTextValue();
			String url = corp.get("url") == null ? null : corp.get("url").getTextValue();
			String address = corp.get("address") == null ? null : corp.get("address").getTextValue();
			String zipcode = corp.get("zipcode") == null ? null : corp.get("zipcode").getTextValue();
			String fathercorp = corp.get("fathercorp") == null ? null : corp.get("fathercorp").getTextValue();
			Integer level = corp.get("level") == null ? null : corp.get("level").getValueAsInt();
			UFBoolean leaf_flag = corp.get("leaf_flag") == null ? null : new UFBoolean(corp.get("leaf_flag").getValueAsText());
			String def2 =corp.get("def2") == null ? null : corp.get("def2").getTextValue();
			String def3 =corp.get("def3") == null ? null : corp.get("def3").getTextValue();
			String def4 =corp.get("def4") == null ? null : corp.get("def4").getTextValue();
			String def5 =corp.get("def5") == null ? null : corp.get("def5").getTextValue();
			String def6 =corp.get("def6") == null ? null : corp.get("def6").getTextValue();
			String def7 =corp.get("def7") == null ? null : corp.get("def7").getTextValue();
			String def8 =corp.get("def8") == null ? null : corp.get("def8").getTextValue();
			String def9 =corp.get("def9") == null ? null : corp.get("def9").getTextValue();
			String def10 =corp.get("def10") == null ? null : corp.get("def10").getTextValue();
			
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			CorpVO oldCorpVO = NWDao.getInstance().queryByCondition(CorpVO.class, "corp_code =? ", corp_code);
			//delete
			if(dr == 0){
				if(oldCorpVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", corp_code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCorpVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldCorpVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldCorpVO != null){
				oldCorpVO.setCorp_code(corp_code);
				oldCorpVO.setCorp_name(corp_name);
				oldCorpVO.setShortname(shortname);
				oldCorpVO.setUrl(url);
				oldCorpVO.setAddress(address);
				oldCorpVO.setZipcode(zipcode);
				oldCorpVO.setLeaf_flag(leaf_flag);
				if(StringUtils.isNotBlank(fathercorp)){
					String fathcorpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
					String fathcorp =  NWDao.getInstance().queryForObject(fathcorpSql, String.class, fathercorp);
					if(StringUtils.isBlank(fathcorp)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", corp_code + ":fathercorp:"+fathercorp+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCorpVO.setFathercorp(fathcorp);
				}
				
				oldCorpVO.setCorp_level(level);
				oldCorpVO.setModify_user(create_user);
				oldCorpVO.setModify_time(create_time);
				oldCorpVO.setDef1(def1);
				oldCorpVO.setDef2(def2);
				oldCorpVO.setDef3(def3);
				oldCorpVO.setDef4(def4);
				oldCorpVO.setDef5(def5);
				oldCorpVO.setDef6(def6);
				oldCorpVO.setDef7(def7);
				oldCorpVO.setDef8(def8);
				oldCorpVO.setDef9(def9);
				oldCorpVO.setDef10(def10);
				oldCorpVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldCorpVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				CorpVO corpVO = new CorpVO();
				corpVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(corpVO);
				corpVO.setCorp_code(corp_code);
				corpVO.setCorp_name(corp_name);
				corpVO.setShortname(shortname);
				corpVO.setUrl(url);
				corpVO.setAddress(address);
				corpVO.setZipcode(zipcode);
				corpVO.setLeaf_flag(leaf_flag);
				if(StringUtils.isNotBlank(fathercorp)){
					String fathcorpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
					String fathcorp =  NWDao.getInstance().queryForObject(fathcorpSql, String.class, fathercorp);
					if(StringUtils.isBlank(fathcorp)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", corp_code + ":fathercorp:"+fathercorp+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					corpVO.setFathercorp(fathcorp);
				}
				corpVO.setCorp_level(level);
				corpVO.setCreate_user(create_user);
				corpVO.setCreate_time(create_time);
				corpVO.setDef1(def1);
				corpVO.setDef2(def2);
				corpVO.setDef3(def3);
				corpVO.setDef4(def4);
				corpVO.setDef5(def5);
				corpVO.setDef6(def6);
				corpVO.setDef7(def7);
				corpVO.setDef8(def8);
				corpVO.setDef9(def9);
				corpVO.setDef10(def10);
				NWDao.getInstance().saveOrUpdate(corpVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	private String CheckCorpInfo(JsonNode corp) {
		JsonNode corp_code = corp.get("corp_code"); //公司编码
		JsonNode corp_name = corp.get("corp_name");//公司名称
		if(corp_code == null || corp_name == null){
			return "主表结构不正确，请确认！";
		}
		String strCorp_code = corp.get("corp_code").getTextValue();
		String strCorp_name = corp.get("corp_name").getTextValue();
		if(strCorp_code == null || strCorp_name == null){
			return "主表 corp必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportCustomer(String uid,String pwd,String jsonCustomers) throws ApiException{
		logger.info("开始同步客户数据：" + jsonCustomers);
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
		// 空值判断
		if (jsonCustomers.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode customers = JacksonUtils.readTree(jsonCustomers);
		// 判断参数有效性
		if (customers == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		
		JsonNode newCustomers = customers;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(customers, "customerConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newCustomers = (JsonNode) convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		
		for (JsonNode customer : newCustomers){
			//判断主表信息
			String errorMsg = CheckCustomerInfo(customer);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			}
			String def1 =customer.get("def1") == null ? null : customer.get("def1").getTextValue();
			Integer dr = customer.get("dr") == null ? 1 : customer.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String cust_code = customer.get("cust_code").getTextValue();
			String cust_name = customer.get("cust_name").getTextValue();
			String cust_type =customer.get("cust_type") == null ? null : customer.get("cust_type").getTextValue();
			String pk_dept =customer.get("pk_dept") == null ? null : customer.get("pk_dept").getTextValue();
			String pk_psndoc =customer.get("pk_psndoc") == null ? null : customer.get("pk_psndoc").getTextValue();
			String pk_province =customer.get("pk_province") == null ? null : customer.get("pk_province").getTextValue();
			String pk_city =customer.get("pk_city") == null ? null : customer.get("pk_city").getTextValue();
			String pk_area =customer.get("pk_area") == null ? null : customer.get("pk_area").getTextValue();
			String contact =customer.get("contact") == null ? null : customer.get("contact").getTextValue();
			String contact_post =customer.get("contact_post") == null ? null : customer.get("contact_post").getTextValue();
			String phone =customer.get("phone") == null ? null : customer.get("phone").getTextValue();
			String mobile =customer.get("mobile") == null ? null : customer.get("mobile").getTextValue();
			String email =customer.get("email") == null ? null : customer.get("email").getTextValue();
			String fax =customer.get("fax") == null ? null : customer.get("fax").getTextValue();
			String address =customer.get("address") == null ? null : customer.get("address").getTextValue();
			String zipcode =customer.get("zipcode") == null ? null : customer.get("zipcode").getTextValue();
			UFDouble discount_rate = customer.get("discount_rate") == null ? null : new UFDouble(customer.get("discount_rate").getValueAsDouble());
			Integer account_period = customer.get("account_period") == null ? null : customer.get("account_period").getValueAsInt();
			Integer acc_period_ahead = customer.get("acc_period_ahead") == null ? null : customer.get("acc_period_ahead").getValueAsInt();
			UFDate billing_date =customer.get("billing_date") == null ? null : new UFDate(customer.get("billing_date").getTextValue());
			Integer billing_ahead = customer.get("billing_ahead") == null ? null : customer.get("billing_ahead").getValueAsInt();
			UFDouble credit_amount = customer.get("credit_amount") == null ? null : new UFDouble(customer.get("credit_amount").getValueAsDouble());
			String tax_identify =customer.get("tax_identify") == null ? null : customer.get("tax_identify").getTextValue();
			String pk_billing_type =customer.get("pk_billing_type") == null ? null : customer.get("pk_billing_type").getTextValue();
			String billing_payable =customer.get("billing_payable") == null ? null : customer.get("billing_payable").getTextValue();
			String bank =customer.get("bank") == null ? null : customer.get("bank").getTextValue();
			String account_name =customer.get("account_name") == null ? null : customer.get("account_name").getTextValue();
			String bank_account =customer.get("bank_account") == null ? null : customer.get("bank_account").getTextValue();
			String register_addr =customer.get("register_addr") == null ? null : customer.get("register_addr").getTextValue();
			String legal_represent =customer.get("legal_represent") == null ? null : customer.get("legal_represent").getTextValue();
			UFDouble register_capital = customer.get("register_capital") == null ? null : new UFDouble(customer.get("register_capital").getValueAsDouble());
			String website =customer.get("website") == null ? null : customer.get("website").getTextValue();
			String memo =customer.get("memo") == null ? null : customer.get("memo").getTextValue();
			String pk_corp =customer.get("pk_corp") == null ? null : customer.get("pk_corp").getTextValue();
			String psncontact =customer.get("psncontact") == null ? null : customer.get("psncontact").getTextValue();
			String balatype =customer.get("balatype") == null ? null : customer.get("balatype").getTextValue();
			Integer billing_rule =customer.get("billing_rule") == null ? 1 : customer.get("billing_rule").getValueAsInt();
			String branch_company =customer.get("branch_company") == null ? null : customer.get("branch_company").getTextValue();
			
			String def2 =customer.get("def2") == null ? null : customer.get("def2").getTextValue();
			String def3 =customer.get("def3") == null ? null : customer.get("def3").getTextValue();
			String def4 =customer.get("def4") == null ? null : customer.get("def4").getTextValue();
			String def5 =customer.get("def5") == null ? null : customer.get("def5").getTextValue();
			String def6 =customer.get("def6") == null ? null : customer.get("def6").getTextValue();
			String def7 =customer.get("def7") == null ? null : customer.get("def7").getTextValue();
			String def8 =customer.get("def8") == null ? null : customer.get("def8").getTextValue();
			String def9 =customer.get("def9") == null ? null : customer.get("def9").getTextValue();
			String def10 =customer.get("def10") == null ? null : customer.get("def10").getTextValue();
			UFDouble def11 =customer.get("def11") == null ? null : new UFDouble(customer.get("def11").getValueAsDouble());
			UFDouble def12 =customer.get("def12") == null ? null : new UFDouble(customer.get("def12").getValueAsDouble());
			
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			CustomerVO oldCustomerVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "cust_code =? ", cust_code);
			//delete
			if(dr == 0){
				if(oldCustomerVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", cust_code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCustomerVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldCustomerVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldCustomerVO != null){
				oldCustomerVO.setStatus(VOStatus.UPDATED);
				oldCustomerVO.setCust_code(cust_code);
				oldCustomerVO.setCust_name(cust_name);
				if(StringUtils.isNotBlank(cust_type)){
					Object realValue = this.getValueByDataDict("cust_type", cust_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":cust_type:"+cust_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setCust_type(Integer.parseInt(realValue.toString()));
				}
				if(StringUtils.isNotBlank(pk_dept)){
					String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and def1 = ?";
					String dept =  NWDao.getInstance().queryForObject(deptSql, String.class, pk_dept);
					if(StringUtils.isBlank(dept)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_dept:"+pk_dept+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setPk_dept(dept);
				}
				if(StringUtils.isNotBlank(pk_psndoc)){
					String psndocSql = "SELECT pk_psndoc FROM nw_psndoc WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String psndoc =  NWDao.getInstance().queryForObject(psndocSql, String.class, pk_psndoc);
					if(StringUtils.isBlank(psndoc)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_psndoc:"+pk_psndoc+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setPk_psndoc(psndoc);
				}
				
				if(StringUtils.isNotBlank(pk_province)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_province);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_province:"+pk_province+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setPk_province(area);
				}
				if(StringUtils.isNotBlank(pk_city)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_city);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_city:"+pk_city+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setPk_city(area);
				}
				if(StringUtils.isNotBlank(pk_area)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_area);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_area:"+pk_area+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setPk_area(area);
				}
				oldCustomerVO.setContact(contact);
				oldCustomerVO.setContact_post(contact_post);
				oldCustomerVO.setPhone(phone);
				oldCustomerVO.setMobile(mobile);
				oldCustomerVO.setEmail(email);
				oldCustomerVO.setFax(fax);
				oldCustomerVO.setAddress(address);
				oldCustomerVO.setZipcode(zipcode);
				oldCustomerVO.setDiscount_rate(discount_rate);
				oldCustomerVO.setAccount_period(account_period);
				oldCustomerVO.setAcc_period_ahead(acc_period_ahead);
				oldCustomerVO.setBilling_date(billing_date);
				oldCustomerVO.setBilling_ahead(billing_ahead);
				oldCustomerVO.setCredit_amount(credit_amount);
				oldCustomerVO.setTax_identify(tax_identify);
				if(StringUtils.isNotBlank(pk_billing_type)){
					Object realValue = this.getValueByDataDict("billing_type", pk_billing_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_billing_type:"+pk_billing_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setPk_billing_type(realValue.toString());
				}
				oldCustomerVO.setBilling_payable(billing_payable);
				oldCustomerVO.setBank(bank);
				oldCustomerVO.setAccount_name(account_name);
				oldCustomerVO.setBank_account(bank_account);
				oldCustomerVO.setRegister_addr(register_addr);
				oldCustomerVO.setLegal_represent(legal_represent);
				oldCustomerVO.setRegister_addr(register_addr);
				oldCustomerVO.setRegister_capital(register_capital);
				oldCustomerVO.setWebsite(website);
				oldCustomerVO.setMemo(memo);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", cust_code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCustomerVO.setPk_corp(corp);
				oldCustomerVO.setPsncontact(psncontact);
				if(StringUtils.isNotBlank(balatype)){
					Object realValue = this.getValueByDataDict("balatype", balatype);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":balatype:"+balatype+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setBalatype(realValue.toString());
				}
				oldCustomerVO.setModify_time(create_time);
				oldCustomerVO.setModify_user(create_user);
				if(StringUtils.isNotBlank(branch_company)){
					String branchCompanySql =  "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
					String branchCompany =  NWDao.getInstance().queryForObject(branchCompanySql, String.class, branch_company);
					if(StringUtils.isBlank(branchCompany)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":branch_company:"+branch_company+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCustomerVO.setBranch_company(branch_company);
				}
				
				oldCustomerVO.setBilling_rule(billing_rule);
				oldCustomerVO.setDef1(def1);
				oldCustomerVO.setDef2(def2);
				oldCustomerVO.setDef3(def3);
				oldCustomerVO.setDef4(def4);
				oldCustomerVO.setDef5(def5);
				oldCustomerVO.setDef6(def6);
				oldCustomerVO.setDef7(def7);
				oldCustomerVO.setDef8(def8);
				oldCustomerVO.setDef9(def9);
				oldCustomerVO.setDef10(def10);
				oldCustomerVO.setDef11(def11);
				oldCustomerVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(oldCustomerVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				CustomerVO customerVO = new CustomerVO();
				customerVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(customerVO);
				customerVO.setCust_code(cust_code);
				customerVO.setCust_name(cust_name);
				if(StringUtils.isNotBlank(cust_type)){
					Object realValue = this.getValueByDataDict("cust_type", cust_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":cust_type:"+cust_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setCust_type(Integer.parseInt(realValue.toString()));
				}
				if(StringUtils.isNotBlank(pk_dept)){
					String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and def1 = ?";
					String dept =  NWDao.getInstance().queryForObject(deptSql, String.class, pk_dept);
					if(StringUtils.isBlank(dept)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_dept:"+pk_dept+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setPk_dept(dept);
				}
				if(StringUtils.isNotBlank(pk_psndoc)){
					String psndocSql = "SELECT pk_psndoc FROM nw_psndoc WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String psndoc =  NWDao.getInstance().queryForObject(psndocSql, String.class, pk_psndoc);
					if(StringUtils.isBlank(psndoc)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_psndoc:"+pk_psndoc+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setPk_psndoc(psndoc);
				}
				
				if(StringUtils.isNotBlank(pk_province)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and name = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_province);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_province:"+pk_province+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setPk_province(area);
				}
				if(StringUtils.isNotBlank(pk_city)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and name = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_city);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_city:"+pk_city+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setPk_city(area);
				}
				if(StringUtils.isNotBlank(pk_area)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and name = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_area);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_area:"+pk_area+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setPk_area(area);
				}
				customerVO.setContact(contact);
				customerVO.setContact_post(contact_post);
				customerVO.setPhone(phone);
				customerVO.setMobile(mobile);
				customerVO.setEmail(email);
				customerVO.setFax(fax);
				customerVO.setAddress(address);
				customerVO.setZipcode(zipcode);
				customerVO.setDiscount_rate(discount_rate);
				customerVO.setAccount_period(account_period);
				customerVO.setAcc_period_ahead(acc_period_ahead);
				customerVO.setBilling_date(billing_date);
				customerVO.setBilling_ahead(billing_ahead);
				customerVO.setCredit_amount(credit_amount);
				customerVO.setTax_identify(tax_identify);
				if(StringUtils.isNotBlank(pk_billing_type)){
					Object realValue = this.getValueByDataDict("billing_type", pk_billing_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":pk_billing_type:"+pk_billing_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setPk_billing_type(realValue.toString());
				}
				customerVO.setBilling_payable(billing_payable);
				customerVO.setBank(bank);
				customerVO.setAccount_name(account_name);
				customerVO.setBank_account(bank_account);
				customerVO.setRegister_addr(register_addr);
				customerVO.setLegal_represent(legal_represent);
				customerVO.setRegister_addr(register_addr);
				customerVO.setRegister_capital(register_capital);
				customerVO.setWebsite(website);
				customerVO.setMemo(memo);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", cust_code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				customerVO.setPk_corp(corp);
				customerVO.setPsncontact(psncontact);
				if(StringUtils.isNotBlank(balatype)){
					Object realValue = this.getValueByDataDict("balatype", balatype);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":balatype:"+balatype+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setBalatype(realValue.toString());
				}
				customerVO.setCreate_time(create_time);
				customerVO.setCreate_user(create_user);
				if(StringUtils.isNotBlank(branch_company)){
					String branchCompanySql =  "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
					String branchCompany =  NWDao.getInstance().queryForObject(branchCompanySql, String.class, branch_company);
					if(StringUtils.isBlank(branchCompany)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", cust_code + ":branch_company:"+branch_company+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					customerVO.setBranch_company(branch_company);
				}
				
				customerVO.setBilling_rule(billing_rule);
				
				customerVO.setDef1(def1);
				customerVO.setDef2(def2);
				customerVO.setDef3(def3);
				customerVO.setDef4(def4);
				customerVO.setDef5(def5);
				customerVO.setDef6(def6);
				customerVO.setDef7(def7);
				customerVO.setDef8(def8);
				customerVO.setDef9(def9);
				customerVO.setDef10(def10);
				customerVO.setDef11(def11);
				customerVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(customerVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
		}
		return JacksonUtils.writeValueAsString(result);
	}
	private String CheckCustomerInfo(JsonNode customer) {
		JsonNode cust_code = customer.get("cust_code"); //客户编码
		JsonNode cust_name = customer.get("cust_name");//客户名称
		JsonNode pk_corp = customer.get("pk_corp");//公司名称
		if(cust_code == null || cust_name == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strCust_code = customer.get("cust_code").getTextValue();
		String strCust_name = customer.get("cust_name").getTextValue();
		String strPk_corp = customer.get("pk_corp").getTextValue();
		if(strCust_code == null || strCust_name == null || strPk_corp == null){
			return "主表 customer必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportAddress2(String uid,String pwd,String jsonAddress) throws ApiException{
		logger.info("开始同步地址数据：" + jsonAddress);
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
		// 空值判断
		if (jsonAddress.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode addressS = JacksonUtils.readTree(jsonAddress);
		// 判断参数有效性
		if (addressS == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (JsonNode address : addressS){
			String Id = address.get("Id") == null ? null : address.get("Id").getTextValue();
			String Name = address.get("Name") == null ? "" : address.get("Name").getTextValue();
			if(StringUtils.isBlank(Name) ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "Name:不能为空！");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String ThreeCode = address.get("ThreeCode") == null ? "" : address.get("ThreeCode").getTextValue();
			if(StringUtils.isBlank(ThreeCode) ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "ThreeCode:不能为空！");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			Integer RecordStatus = address.get("RecordStatus") == null ? 1 : address.get("RecordStatus").getValueAsInt();
			if(RecordStatus != null && RecordStatus!=1 && RecordStatus!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "RecordStatus：不正确");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			AddressVO oldAddressVO = NWDao.getInstance().queryByCondition(AddressVO.class, "addr_code =? ", ThreeCode);
			
			if(RecordStatus == 0){
				if(oldAddressVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", ThreeCode +"：不存在，无法删除");
					resultMap.put("entityKey", Id);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldAddressVO.setStatus(VOStatus.DELETED);
				try {
					addrService.deleteByPrimaryKey(AddressVO.class, oldAddressVO.getPrimaryKey());
				} catch (Exception e) {
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", e.getMessage());
					resultMap.put("entityKey", Id);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String CountryName = address.get("CountryName") == null ? null : address.get("CountryName").getTextValue();
			String CountryCode = address.get("CountryCode") == null ? null : address.get("CountryCode").getTextValue();
			String EnglishName = address.get("EnglishName") == null ? null : address.get("EnglishName").getTextValue();
			
			
			if(oldAddressVO != null){
				oldAddressVO.setAddr_code(ThreeCode);
				oldAddressVO.setAddr_name(Name);
				oldAddressVO.setDef1(Id);
				oldAddressVO.setDef2(EnglishName);
				oldAddressVO.setDef3(CountryName);
				oldAddressVO.setDef4(CountryCode);
				oldAddressVO.setPk_corp("0001");
				oldAddressVO.setModify_time(new UFDateTime(new Date()));
				oldAddressVO.setModify_user(ediUserVO.getPk_user());
				oldAddressVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(oldAddressVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			}else{
				//new
				AddressVO addressVO = new AddressVO();
				addressVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(addressVO);
				addressVO.setAddr_code(ThreeCode);
				addressVO.setAddr_name(Name);
				addressVO.setDef1(Id);
				addressVO.setDef2(EnglishName);
				addressVO.setDef3(CountryName);
				addressVO.setDef4(CountryCode);
				addressVO.setPk_corp("0001");
				addressVO.setCreate_time(new UFDateTime(new Date()));
				addressVO.setCreate_user(ediUserVO.getPk_user());
				toBeUpdate.add(addressVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return JacksonUtils.writeValueAsString(result);
	}
	
	
	
	
	public String ImportAddress(String uid,String pwd,String jsonAddress) throws ApiException{
		logger.info("开始同步地址数据：" + jsonAddress);
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
		// 空值判断
		if (jsonAddress.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode addressS = JacksonUtils.readTree(jsonAddress);
		// 判断参数有效性
		if (addressS == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode address : addressS) {
			Map<String,Object> resultMap = saveAddress(address);
			result.add(resultMap);
		}
		return JacksonUtils.writeValueAsString(result);	
	}
	
	public Map<String,Object> saveAddress(JsonNode address){
		String Id =  address.get("Id").getTextValue();
		String OrgName =  address.get("OrgName").getTextValue();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		JsonNode bodys = address.get("Addresses");
		if(bodys != null && bodys.size() > 0){
			for(JsonNode body : bodys){
				String bodyID = body.get("Id").getTextValue();
				String Country = body.get("Country").getTextValue();
				String Province = body.get("Province").getTextValue();
				String City = body.get("City").getTextValue();
				String Area = body.get("Area").getTextValue();
				String Address = body.get("Address").getTextValue();
				String Contact = body.get("Contact") == null ? null : body.get("Contact").getTextValue();
				String PhoneNumber = body.get("PhoneNumber") == null ? null : body.get("PhoneNumber").getTextValue();
				Integer RecordStatus =  body.get("RecordStatus").getIntValue();
				AddressVO oldAddressVO = NWDao.getInstance().queryByCondition(AddressVO.class, "def1=?", bodyID);
				if(RecordStatus == 0){//删除
					if(oldAddressVO != null){
						oldAddressVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(oldAddressVO);
					}
					//没有也无所谓，结果不存在即可。
					continue;
				}
				//Province
				AreaVO province = NWDao.getInstance().queryByCondition(AreaVO.class, "def1=?", Province);
				if(province == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", "Province:"+Province+"，不存在！");
					resultMap.put("entityKey", bodyID);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					return resultMap;
				}else{
					Province = province.getPk_area();
				}
				//City
				AreaVO city = NWDao.getInstance().queryByCondition(AreaVO.class, "def1=?", City);
				if(city == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", "City:"+City+"，不存在！");
					resultMap.put("entityKey", bodyID);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					return resultMap;
				}else{
					City = city.getPk_area();
				}
				//Area
				AreaVO area = NWDao.getInstance().queryByCondition(AreaVO.class, "def1=?", Area);
				if(area == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", "Area:"+Area+"，不存在！");
					resultMap.put("entityKey", bodyID);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					return resultMap;
				}else{
					Area = area.getPk_area();
				}
				if(oldAddressVO == null){
					oldAddressVO = new AddressVO();
					oldAddressVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(oldAddressVO);
					oldAddressVO.setAddr_code(CodenoHelper.generateCode("t10502"));
					oldAddressVO.setCreate_time(new UFDateTime(new Date()));
					oldAddressVO.setCreate_user(ediUserVO.getPk_user());
				}else{
					oldAddressVO.setStatus(VOStatus.UPDATED);
					oldAddressVO.setModify_time(new UFDateTime(new Date()));
					oldAddressVO.setModify_user(ediUserVO.getPk_user());
				}
				oldAddressVO.setAddr_name(OrgName);
				oldAddressVO.setDef1(bodyID);
				oldAddressVO.setPk_country(Country);
				oldAddressVO.setPk_province(Province);
				oldAddressVO.setPk_city(City);
				oldAddressVO.setPk_area(Area);
				oldAddressVO.setDetail_addr(Address);
				oldAddressVO.setContact(Contact);
				oldAddressVO.setPhone(PhoneNumber);
				oldAddressVO.setPk_corp("0001");
				oldAddressVO.setAddr_type(1);//收发货地址
				toBeUpdate.add(oldAddressVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		Map<String,Object> resultMap = new HashMap<String, Object>();
		resultMap.put("success", true);
		resultMap.put("errorMessage", "");
		resultMap.put("entityKey", Id);
		resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
		resultMap.put("serviceType", "TMS");
		return resultMap;
	}

	
	
	public String ImportCarrier(String uid,String pwd,String jsonCarriers) throws ApiException{
		logger.info("开始同步承运商数据：" + jsonCarriers);
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
		// 空值判断
		if (jsonCarriers.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode carriers = JacksonUtils.readTree(jsonCarriers);
		// 判断参数有效性
		if (carriers == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newCarriers = carriers;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(carriers, "carrierConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newCarriers = (JsonNode) convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode carrier : newCarriers){
			//判断主表信息
			String errorMsg = CheckCarrierInfo(carrier);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String def1 =carrier.get("def1") == null ? null : carrier.get("def1").getTextValue();
			Integer dr = carrier.get("dr") == null ? 1 : carrier.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String carr_code = carrier.get("carr_code").getTextValue();
			String carr_name = carrier.get("carr_name").getTextValue();
			Integer carr_type =carrier.get("carr_type") == null ? null : carrier.get("carr_type").getIntValue();
			String priority =carrier.get("priority") == null ? null : carrier.get("priority").getTextValue();
			String pk_province =carrier.get("pk_province") == null ? null : carrier.get("pk_province").getTextValue();
			String pk_city =carrier.get("pk_city") == null ? null : carrier.get("pk_city").getTextValue();
			String pk_area =carrier.get("pk_area") == null ? null : carrier.get("pk_area").getTextValue();
			String pk_customer =carrier.get("pk_customer") == null ? null : carrier.get("pk_customer").getTextValue();
			String contact =carrier.get("contact") == null ? null : carrier.get("contact").getTextValue();
			String contact_post =carrier.get("contact_post") == null ? null : carrier.get("contact_post").getTextValue();
			String phone =carrier.get("phone") == null ? null : carrier.get("phone").getTextValue();
			String mobile =carrier.get("mobile") == null ? null : carrier.get("mobile").getTextValue();
			String email =carrier.get("email") == null ? null : carrier.get("email").getTextValue();
			String fax =carrier.get("fax") == null ? null : carrier.get("fax").getTextValue();
			String address =carrier.get("address") == null ? null : carrier.get("address").getTextValue();
			String zipcode =carrier.get("zipcode") == null ? null : carrier.get("zipcode").getTextValue();
			UFDouble discount_rate = carrier.get("discount_rate") == null ? null : new UFDouble(carrier.get("discount_rate").getValueAsDouble());
			Integer account_period = carrier.get("account_period") == null ? null : carrier.get("account_period").getValueAsInt();
			Integer acc_period_ahead = carrier.get("acc_period_ahead") == null ? null : carrier.get("acc_period_ahead").getValueAsInt();
			UFDate billing_date =carrier.get("billing_date") == null ? null : new UFDate(carrier.get("billing_date").getTextValue());
			Integer billing_ahead = carrier.get("billing_ahead") == null ? null : carrier.get("billing_ahead").getValueAsInt();
			UFDouble credit_amount = carrier.get("credit_amount") == null ? null : new UFDouble(carrier.get("credit_amount").getValueAsDouble());
			String tax_identify =carrier.get("tax_identify") == null ? null : carrier.get("tax_identify").getTextValue();
			String pk_billing_type =carrier.get("pk_billing_type") == null ? null : carrier.get("pk_billing_type").getTextValue();
			String billing_payable =carrier.get("billing_payable") == null ? null : carrier.get("billing_payable").getTextValue();
			String bank =carrier.get("bank") == null ? null : carrier.get("bank").getTextValue();
			String account_name =carrier.get("account_name") == null ? null : carrier.get("account_name").getTextValue();
			String bank_account =carrier.get("bank_account") == null ? null : carrier.get("bank_account").getTextValue();
			String register_addr =carrier.get("register_addr") == null ? null : carrier.get("register_addr").getTextValue();
			String legal_represent =carrier.get("legal_represent") == null ? null : carrier.get("legal_represent").getTextValue();
			UFDouble register_capital = carrier.get("register_capital") == null ? null : new UFDouble(carrier.get("register_capital").getValueAsDouble());
			String website =carrier.get("website") == null ? null : carrier.get("website").getTextValue();
			String memo =carrier.get("memo") == null ? null : carrier.get("memo").getTextValue();
			String pk_corp =carrier.get("pk_corp") == null ? null : carrier.get("pk_corp").getTextValue();
			String balatype =carrier.get("balatype") == null ? null : carrier.get("balatype").getTextValue();
			Integer billing_rule =carrier.get("billing_rule") == null ? 1 : carrier.get("billing_rule").getValueAsInt();
			String branch_company =carrier.get("branch_company") == null ? null : carrier.get("branch_company").getTextValue();
			
			String def2 =carrier.get("def2") == null ? null : carrier.get("def2").getTextValue();
			String def3 =carrier.get("def3") == null ? null : carrier.get("def3").getTextValue();
			String def4 =carrier.get("def4") == null ? null : carrier.get("def4").getTextValue();
			String def5 =carrier.get("def5") == null ? null : (new UFBoolean(carrier.get("def5").getBooleanValue())).toString();
			String def6 =carrier.get("def6") == null ? null : carrier.get("def6").getTextValue();
			String def7 =carrier.get("def7") == null ? null : carrier.get("def7").getTextValue();
			String def8 =carrier.get("def8") == null ? null : carrier.get("def8").getTextValue();
			String def9 =carrier.get("def9") == null ? null : carrier.get("def9").getTextValue();
			String def10 =carrier.get("def10") == null ? null : carrier.get("def10").getTextValue();
			UFDouble def11 =carrier.get("def11") == null ? null : new UFDouble(carrier.get("def11").getValueAsDouble());
			UFDouble def12 =carrier.get("def12") == null ? null : new UFDouble(carrier.get("def12").getValueAsDouble());
			
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			CarrierVO oldCarrierVO = NWDao.getInstance().queryByCondition(CarrierVO.class, "carr_code =? ", carr_code);
			//delete
			if(dr == 0){
				if(oldCarrierVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", carr_code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCarrierVO.setStatus(VOStatus.DELETED);
				try {
					carrService.deleteByPrimaryKey(CarrierVO.class, oldCarrierVO.getPrimaryKey());
				} catch (Exception e) {
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", e.getMessage());
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldCarrierVO != null){
				oldCarrierVO.setStatus(VOStatus.UPDATED);
				oldCarrierVO.setCarr_code(carr_code);
				oldCarrierVO.setCarr_name(carr_name);
				oldCarrierVO.setCarr_type(carr_type);
				if(StringUtils.isNotBlank(priority)){
					Object realValue = this.getValueByDataDict("carr_priority", priority);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":priority:"+priority+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setPriority(Integer.parseInt(realValue.toString()));
				}
				if(StringUtils.isNotBlank(pk_province)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_province);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_province:"+pk_province+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setPk_province(area);
				}
				if(StringUtils.isNotBlank(pk_city)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_city);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_city:"+pk_city+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setPk_city(area);
				}
				if(StringUtils.isNotBlank(pk_area)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_area);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_area:"+pk_area+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setPk_area(area);
				}
				if(StringUtils.isNotBlank(pk_customer)){
					String custSql = "SELECT pk_customer FROM ts_customer WITH(nolock) WHERE isnull(dr,0)=0 and cust_code = ?";
					String cust =  NWDao.getInstance().queryForObject(custSql, String.class, pk_customer);
					if(StringUtils.isBlank(cust)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_customer:"+pk_customer+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setPk_customer(cust);
				}
				oldCarrierVO.setContact(contact);
				oldCarrierVO.setContact_post(contact_post);
				oldCarrierVO.setPhone(phone);
				oldCarrierVO.setMobile(mobile);
				oldCarrierVO.setEmail(email);
				oldCarrierVO.setFax(fax);
				oldCarrierVO.setAddress(address);
				oldCarrierVO.setZipcode(zipcode);
				oldCarrierVO.setDiscount_rate(discount_rate);
				oldCarrierVO.setAccount_period(account_period);
				oldCarrierVO.setAcc_period_ahead(acc_period_ahead);
				oldCarrierVO.setBilling_date(billing_date);
				oldCarrierVO.setBilling_ahead(billing_ahead);
				oldCarrierVO.setCredit_amount(credit_amount);
				oldCarrierVO.setTax_identify(tax_identify);
				if(StringUtils.isNotBlank(pk_billing_type)){
					Object realValue = this.getValueByDataDict("billing_type", pk_billing_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_billing_type:"+pk_billing_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setPk_billing_type(realValue.toString());
				}
				oldCarrierVO.setBilling_payable(billing_payable);
				oldCarrierVO.setBank(bank);
				oldCarrierVO.setAccount_name(account_name);
				oldCarrierVO.setBank_account(bank_account);
				oldCarrierVO.setRegister_addr(register_addr);
				oldCarrierVO.setLegal_represent(legal_represent);
				oldCarrierVO.setRegister_addr(register_addr);
				oldCarrierVO.setRegister_capital(register_capital);
				oldCarrierVO.setWebsite(website);
				oldCarrierVO.setMemo(memo);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", carr_code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCarrierVO.setPk_corp(corp);
				if(StringUtils.isNotBlank(balatype)){
					Object realValue = this.getValueByDataDict("balatype", balatype);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":balatype:"+balatype+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setBalatype(realValue.toString());
				}
				oldCarrierVO.setModify_time(create_time);
				oldCarrierVO.setModify_user(create_user);
				if(StringUtils.isNotBlank(branch_company)){
					String branchCompanySql =  "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
					String branchCompany =  NWDao.getInstance().queryForObject(branchCompanySql, String.class, branch_company);
					if(StringUtils.isBlank(branchCompany)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":branch_company:"+branch_company+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarrierVO.setBranch_company(branch_company);
				}
				oldCarrierVO.setBilling_rule(billing_rule);
				oldCarrierVO.setDef1(def1);
				oldCarrierVO.setDef2(def2);
				oldCarrierVO.setDef3(def3);
				oldCarrierVO.setDef4(def4);
				oldCarrierVO.setDef5(def5);
				oldCarrierVO.setDef6(def6);
				oldCarrierVO.setDef7(def7);
				oldCarrierVO.setDef8(def8);
				oldCarrierVO.setDef9(def9);
				oldCarrierVO.setDef10(def10);
				oldCarrierVO.setDef11(def11);
				oldCarrierVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(oldCarrierVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				CarrierVO carrierVO = new CarrierVO();
				carrierVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(carrierVO);
				carrierVO.setCarr_code(carr_code);
				carrierVO.setCarr_name(carr_name);
				carrierVO.setCarr_type(carr_type);
				if(StringUtils.isNotBlank(pk_province)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_province);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_province:"+pk_province+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carrierVO.setPk_province(area);
				}
				if(StringUtils.isNotBlank(pk_city)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_city);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_city:"+pk_city+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carrierVO.setPk_city(area);
				}
				if(StringUtils.isNotBlank(pk_area)){
					String areaSql = "SELECT pk_area FROM ts_area WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String area =  NWDao.getInstance().queryForObject(areaSql, String.class, pk_area);
					if(StringUtils.isBlank(area)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_area:"+pk_area+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carrierVO.setPk_area(area);
				}
				carrierVO.setContact(contact);
				carrierVO.setContact_post(contact_post);
				carrierVO.setPhone(phone);
				carrierVO.setMobile(mobile);
				carrierVO.setEmail(email);
				carrierVO.setFax(fax);
				carrierVO.setAddress(address);
				carrierVO.setZipcode(zipcode);
				carrierVO.setDiscount_rate(discount_rate);
				carrierVO.setAccount_period(account_period);
				carrierVO.setAcc_period_ahead(acc_period_ahead);
				carrierVO.setBilling_date(billing_date);
				carrierVO.setBilling_ahead(billing_ahead);
				carrierVO.setCredit_amount(credit_amount);
				carrierVO.setTax_identify(tax_identify);
				if(StringUtils.isNotBlank(pk_billing_type)){
					Object realValue = this.getValueByDataDict("billing_type", pk_billing_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":pk_billing_type:"+pk_billing_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carrierVO.setPk_billing_type(realValue.toString());
				}
				carrierVO.setBilling_payable(billing_payable);
				carrierVO.setBank(bank);
				carrierVO.setAccount_name(account_name);
				carrierVO.setBank_account(bank_account);
				carrierVO.setRegister_addr(register_addr);
				carrierVO.setLegal_represent(legal_represent);
				carrierVO.setRegister_addr(register_addr);
				carrierVO.setRegister_capital(register_capital);
				carrierVO.setWebsite(website);
				carrierVO.setMemo(memo);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", carr_code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				carrierVO.setPk_corp(corp);
				if(StringUtils.isNotBlank(balatype)){
					Object realValue = this.getValueByDataDict("balatype", balatype);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":balatype:"+balatype+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carrierVO.setBalatype(realValue.toString());
				}
				carrierVO.setCreate_time(create_time);
				carrierVO.setCreate_user(create_user);
				if(StringUtils.isNotBlank(branch_company)){
					String branchCompanySql =  "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
					String branchCompany =  NWDao.getInstance().queryForObject(branchCompanySql, String.class, branch_company);
					if(StringUtils.isBlank(branchCompany)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", carr_code + ":branch_company:"+branch_company+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carrierVO.setBranch_company(branch_company);
				}
				
				carrierVO.setBilling_rule(billing_rule);
				carrierVO.setDef1(def1);
				carrierVO.setDef2(def2);
				carrierVO.setDef3(def3);
				carrierVO.setDef4(def4);
				carrierVO.setDef5(def5);
				carrierVO.setDef6(def6);
				carrierVO.setDef7(def7);
				carrierVO.setDef8(def8);
				carrierVO.setDef9(def9);
				carrierVO.setDef10(def10);
				carrierVO.setDef11(def11);
				carrierVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(carrierVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
		}
		return JacksonUtils.writeValueAsString(result);
	}
	
	
	
	private String CheckCarrierInfo(JsonNode carrier) {
		JsonNode carr_code = carrier.get("carr_code"); //承运商编码
		JsonNode carr_name = carrier.get("carr_name");//承运商名称
		JsonNode pk_corp = carrier.get("pk_corp");//公司名称
		if(carr_code == null || carr_name == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strCarr_code = carrier.get("carr_code").getTextValue();
		String strCarr_name = carrier.get("carr_name").getTextValue();
		String strPk_corp = carrier.get("pk_corp").getTextValue();
		if(strCarr_code == null || strCarr_name == null || strPk_corp == null){
			return "主表 carrier必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportCar(String uid,String pwd,String jsonCars) throws ApiException{
		logger.info("开始同步车辆数据：" + jsonCars);
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
		// 空值判断
		if (jsonCars.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode cars = JacksonUtils.readTree(jsonCars);
		// 判断参数有效性
		if (cars == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newCars = cars;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(cars, "carConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newCars = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode car : newCars) {
			//判断主表信息
			String errorMsg = CheckCarInfo(car);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String def1 =car.get("def1") == null ? null : car.get("def1").getTextValue();
			Integer dr = car.get("dr") == null ? 1 : car.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String carno = car.get("carno").getTextValue();
			String pk_car_type = car.get("pk_car_type") == null ? null : car.get("pk_car_type").getTextValue();
			Integer car_prop = car.get("car_prop") == null ? null : car.get("car_prop").getIntValue();
			String pk_carrier = car.get("pk_carrier") == null ? null : car.get("pk_carrier").getTextValue();
			String priority = car.get("priority") == null ? null : car.get("priority").getTextValue();
			Integer car_status = car.get("car_status") == null ? null : car.get("car_status").getValueAsInt();
			String trailer = car.get("trailer") == null ? null : car.get("trailer").getTextValue();
			String tank = car.get("tank") == null ? null : car.get("tank").getTextValue();
			String pk_driver = car.get("pk_driver") == null ? null : car.get("pk_driver").getTextValue();
			String driver_mobile = car.get("driver_mobile") == null ? null : car.get("driver_mobile").getTextValue();
			
			UFDouble length = car.get("length") == null ? null : new UFDouble(car.get("length").getValueAsDouble());
			UFDouble width = car.get("width") == null ? null : new UFDouble(car.get("width").getValueAsDouble());
			UFDouble height = car.get("height") == null ? null : new UFDouble(car.get("height").getValueAsDouble());
			UFDouble volume = car.get("volume") == null ? null : new UFDouble(car.get("volume").getValueAsDouble());
			UFDouble e_fc_per_km = car.get("e_fc_per_km") == null ? null : new UFDouble(car.get("e_fc_per_km").getValueAsDouble());
			UFDouble l_fc_per_km = car.get("l_fc_per_km") == null ? null : new UFDouble(car.get("l_fc_per_km").getValueAsDouble());
			UFDouble v_fc_per_km = car.get("v_fc_per_km") == null ? null : new UFDouble(car.get("v_fc_per_km").getValueAsDouble());
			UFDouble mileage = car.get("mileage") == null ? null : new UFDouble(car.get("mileage").getValueAsDouble());
			UFDouble gps_mileage = car.get("gps_mileage") == null ? null : new UFDouble(car.get("gps_mileage").getValueAsDouble());
			
			String note = car.get("note") == null ? null : car.get("note").getTextValue();
			String photo = car.get("photo") == null ? null : car.get("photo").getTextValue();
			String manufacturer = car.get("manufacturer") == null ? null : car.get("manufacturer").getTextValue();
			UFDate output_date =car.get("output_date") == null ? null : new UFDate(car.get("output_date").getTextValue());
			String brand = car.get("brand") == null ? null : car.get("brand").getTextValue();
			String engine_no = car.get("engine_no") == null ? null : car.get("engine_no").getTextValue();
			String chassis_no = car.get("chassis_no") == null ? null : car.get("chassis_no").getTextValue();
			String frame_no = car.get("frame_no") == null ? null : car.get("frame_no").getTextValue();
			UFDate lic_date =car.get("lic_date") == null ? null : new UFDate(car.get("lic_date").getTextValue());
			UFDate filing_date =car.get("filing_date") == null ? null : new UFDate(car.get("filing_date").getTextValue());
			UFDate buy_date =car.get("buy_date") == null ? null : new UFDate(car.get("buy_date").getTextValue());
			String legal_owner = car.get("legal_owner") == null ? null : car.get("legal_owner").getTextValue();
			UFDouble buy_amount = car.get("buy_amount") == null ? null : new UFDouble(car.get("buy_amount").getValueAsDouble());
			Integer legal_service_date = car.get("legal_service_date") == null ? null : car.get("legal_service_date").getValueAsInt();
			Integer corp_service_date = car.get("corp_service_date") == null ? null : car.get("corp_service_date").getValueAsInt();
			Integer legal_service_mileage = car.get("legal_service_mileage") == null ? null : car.get("legal_service_mileage").getValueAsInt();
			Integer corp_service_mileage = car.get("corp_service_mileage") == null ? null : car.get("corp_service_mileage").getValueAsInt();
			String actual_owner = car.get("actual_owner") == null ? null : car.get("actual_owner").getTextValue();
			String pk_corp = car.get("pk_corp").getTextValue();
			String memo = car.get("memo") == null ? null : car.get("memo").getTextValue();
			UFDouble load_weight = car.get("load_weight") == null ? null : new UFDouble(car.get("load_weight").getValueAsDouble());
			String gps_id = car.get("gps_id") == null ? null : car.get("gps_id").getTextValue();
			UFBoolean customs = car.get("customs") == null ? null : new UFBoolean(car.get("customs").getValueAsText());
			UFBoolean dangerous_veh = car.get("dangerous_veh") == null ? null : new UFBoolean(car.get("dangerous_veh").getValueAsText());
			String tonnage = car.get("tonnage") == null ? null : car.get("tonnage").getTextValue();
			String White_card_number = car.get("White_card_number") == null ? null : car.get("White_card_number").getTextValue();
			String driving_number = car.get("driving_number") == null ? null : car.get("driving_number").getTextValue();
			UFDouble outside_length = car.get("outside_length") == null ? null : new UFDouble(car.get("outside_length").getValueAsDouble());
			UFDouble outside_width = car.get("outside_width") == null ? null : new UFDouble(car.get("outside_width").getValueAsDouble());
			UFDouble outside_height = car.get("outside_height") == null ? null : new UFDouble(car.get("outside_height").getValueAsDouble());
			UFDateTime annual_date =car.get("annual_date") == null ? null : new UFDateTime(car.get("annual_date").getTextValue());
			UFBoolean if_white = car.get("if_white") == null ? null : new UFBoolean(car.get("if_white").getValueAsText());
			Integer supervision_type = car.get("supervision_type") == null ? null : car.get("supervision_type").getValueAsInt();
			Integer gps_lev = car.get("gps_lev") == null ? null : car.get("gps_lev").getValueAsInt();
			
			String def2 =car.get("def2") == null ? null : car.get("def2").getTextValue();
			String def3 =car.get("def3") == null ? null : car.get("def3").getTextValue();
			String def4 =car.get("def4") == null ? null : car.get("def4").getTextValue();
			String def5 =car.get("def5") == null ? null : car.get("def5").getTextValue();
			String def6 =car.get("def6") == null ? null : car.get("def6").getTextValue();
			String def7 =car.get("def7") == null ? null : car.get("def7").getTextValue();
			String def8 =car.get("def8") == null ? null : car.get("def8").getTextValue();
			String def9 =car.get("def9") == null ? null : car.get("def9").getTextValue();
			String def10 =car.get("def10") == null ? null : car.get("def10").getTextValue();
			UFDouble def11 = car.get("def11") == null ? null : new UFDouble(car.get("def11").getValueAsDouble());
			UFDouble def12 = car.get("def12") == null ? null : new UFDouble(car.get("def12").getValueAsDouble());
			
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			CarVO oldCarVO = NWDao.getInstance().queryByCondition(CarVO.class, "carno =? ", carno);
			//delete
			if(dr == 0){
				if(oldCarVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", carno +"不存在，无法删除");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCarVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldCarVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldCarVO != null){
				if(StringUtils.isNotBlank(pk_car_type)){
					String carTypeSql =  "SELECT pk_car_type FROM ts_car_type WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String carType =  NWDao.getInstance().queryForObject(carTypeSql, String.class, pk_car_type);
					if(StringUtils.isBlank(carType)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":pk_car_type:"+pk_car_type+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarVO.setPk_car_type(carType);
				}
				oldCarVO.setCarno(carno);
//				if(StringUtils.isNotBlank(car_prop)){
//					Object realValue = this.getValueByDataDict("car_prop", car_prop);
//					if(realValue == null){
//						Map<String,Object> resultMap = new HashMap<String, Object>();
//						resultMap.put("success", false);
//						resultMap.put("errorMessage",carno + ":car_prop:"+car_prop+"不正确！");
//						resultMap.put("entityKey", "");
//						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
//						resultMap.put("serviceType", "TMS");
//						result.add(resultMap);
//						continue;
//					}
//					oldCarVO.setCar_prop(Integer.parseInt(realValue.toString()));
//				}
				oldCarVO.setCar_prop(car_prop);
				if(StringUtils.isNotBlank(pk_carrier)){
					String carrierSql =  "SELECT pk_carrier FROM ts_carrier WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String carrier =  NWDao.getInstance().queryForObject(carrierSql, String.class, pk_carrier);
					if(StringUtils.isBlank(carrier)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":pk_carrier:"+pk_carrier+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarVO.setPk_carrier(carrier);
				}
				if(StringUtils.isNotBlank(priority)){
					Object realValue = this.getValueByDataDict("car_priority", priority);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":priority:"+priority+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarVO.setPriority(Integer.parseInt(realValue.toString()));
				}
				oldCarVO.setCar_status(car_status);
				
//				if(StringUtils.isNotBlank(trailer)){
//					String trailerSql =  "SELECT pk_car FROM ts_car WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carno = ?";
//					String trai =  NWDao.getInstance().queryForObject(trailerSql, String.class, trailer);
//					if(StringUtils.isBlank(trai)){
//						Map<String,Object> resultMap = new HashMap<String, Object>();
//						resultMap.put("success", false);
//						resultMap.put("errorMessage",carno + ":trailer:"+trailer+"不正确！");
//						resultMap.put("entityKey", "");
//						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
//						resultMap.put("serviceType", "TMS");
//						result.add(resultMap);
//						continue;
//					}
//					oldCarVO.setTrailer(trai);
//				}
				oldCarVO.setTrailer(trailer);
				
				if(StringUtils.isNotBlank(tank)){
					String tankSql =  "SELECT pk_car FROM ts_car WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carno = ?";
					String tank1 =  NWDao.getInstance().queryForObject(tankSql, String.class, tank);
					if(StringUtils.isBlank(tank1)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":tank:"+tank+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarVO.setTank(tank1);
				}
				
				if(StringUtils.isNotBlank(pk_driver)){
					String driverSql =  "SELECT pk_driver FROM ts_driver WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and def1 = ?";
					String driver =  NWDao.getInstance().queryForObject(driverSql, String.class, pk_driver);
					if(StringUtils.isBlank(driver)){
						oldCarVO.setPk_driver(pk_driver);
					}else{
						oldCarVO.setPk_driver(driver);
					}
					
				}
				oldCarVO.setDriver_mobile(driver_mobile);
				oldCarVO.setLength(length);
				oldCarVO.setWidth(width);
				oldCarVO.setHeight(height);
				oldCarVO.setVolume(volume);
				oldCarVO.setE_fc_per_km(e_fc_per_km);
				oldCarVO.setL_fc_per_km(l_fc_per_km);
				oldCarVO.setV_fc_per_km(v_fc_per_km);
				oldCarVO.setMileage(mileage);
				oldCarVO.setGps_mileage(gps_mileage);
				oldCarVO.setModify_time(create_time);
				oldCarVO.setModify_user(create_user);
				oldCarVO.setNote(note);
				oldCarVO.setPhoto(photo);
				oldCarVO.setManufacturer(manufacturer);
				oldCarVO.setOutput_date(output_date);
				oldCarVO.setBrand(brand);
				oldCarVO.setEngine_no(engine_no);
				oldCarVO.setChassis_no(chassis_no);
				oldCarVO.setFrame_no(frame_no);
				oldCarVO.setLic_date(lic_date);
				oldCarVO.setFiling_date(filing_date);
				oldCarVO.setBuy_date(buy_date);
				oldCarVO.setLegal_owner(legal_owner);
				oldCarVO.setBuy_amount(buy_amount);
				oldCarVO.setLegal_service_date(legal_service_date);
				oldCarVO.setCorp_service_date(corp_service_date);
				oldCarVO.setLegal_service_mileage(legal_service_mileage);
				oldCarVO.setCorp_service_mileage(corp_service_mileage);
				oldCarVO.setActual_owner(actual_owner);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",carno + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCarVO.setPk_corp(corp);
				oldCarVO.setMemo(memo);
				oldCarVO.setLoad_weight(load_weight);
				oldCarVO.setGps_id(gps_id);
				oldCarVO.setCustoms(customs);
				oldCarVO.setDangerous_veh(dangerous_veh);
				oldCarVO.setTonnage(tonnage);
				oldCarVO.setWhite_card_number(White_card_number);
				oldCarVO.setDriving_number(driving_number);
				oldCarVO.setOutside_height(outside_height);
				oldCarVO.setOutside_length(outside_length);
				oldCarVO.setOutside_width(outside_width);
				oldCarVO.setAnnual_date(annual_date);
				oldCarVO.setIf_white(if_white);
				oldCarVO.setSupervision_type(supervision_type);
				oldCarVO.setGps_lev(gps_lev);
				oldCarVO.setDef1(def1);
				oldCarVO.setDef2(def2);
				oldCarVO.setDef3(def3);
				oldCarVO.setDef4(def4);
				oldCarVO.setDef5(def5);
				oldCarVO.setDef6(def6);
				oldCarVO.setDef7(def7);
				oldCarVO.setDef8(def8);
				oldCarVO.setDef9(def9);
				oldCarVO.setDef10(def10);
				oldCarVO.setDef11(def11);
				oldCarVO.setDef12(def12);
				oldCarVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldCarVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				CarVO carVO = new CarVO();
				carVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(carVO);
				
				if(StringUtils.isNotBlank(pk_car_type)){
					String carTypeSql =  "SELECT pk_car_type FROM ts_car_type WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String carType =  NWDao.getInstance().queryForObject(carTypeSql, String.class, pk_car_type);
					if(StringUtils.isBlank(carType)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":pk_car_type:"+pk_car_type+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carVO.setPk_car_type(carType);
				}
				carVO.setCarno(carno);
//				if(StringUtils.isNotBlank(car_prop)){
//					Object realValue = this.getValueByDataDict("car_prop", car_prop);
//					if(realValue == null){
//						Map<String,Object> resultMap = new HashMap<String, Object>();
//						resultMap.put("success", false);
//						resultMap.put("errorMessage",carno + ":car_prop:"+car_prop+"不正确！");
//						resultMap.put("entityKey", "");
//						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
//						resultMap.put("serviceType", "TMS");
//						result.add(resultMap);
//						continue;
//					}
//					carVO.setCar_prop(Integer.parseInt(realValue.toString()));
//				}
				carVO.setCar_prop(car_prop);
				if(StringUtils.isNotBlank(pk_carrier)){
					String carrierSql =  "SELECT pk_carrier FROM ts_carrier WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String carrier =  NWDao.getInstance().queryForObject(carrierSql, String.class, pk_carrier);
					if(StringUtils.isBlank(carrier)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":pk_carrier:"+pk_carrier+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carVO.setPk_carrier(carrier);
				}
				if(StringUtils.isNotBlank(priority)){
					Object realValue = this.getValueByDataDict("car_priority", priority);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":priority:"+priority+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carVO.setPriority(Integer.parseInt(realValue.toString()));
				}
				carVO.setCar_status(car_status);
				if(StringUtils.isNotBlank(trailer)){
					String trailerSql =  "SELECT pk_car FROM ts_car WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carno = ?";
					String trai =  NWDao.getInstance().queryForObject(trailerSql, String.class, trailer);
					if(StringUtils.isBlank(trai)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":trailer:"+trailer+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carVO.setTrailer(trai);
				}
				
				
				if(StringUtils.isNotBlank(tank)){
					String tankSql =  "SELECT pk_car FROM ts_car WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carno = ?";
					String tank1 =  NWDao.getInstance().queryForObject(tankSql, String.class, tank);
					if(StringUtils.isBlank(tank1)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",carno + ":tank:"+tank+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carVO.setTank(tank1);
				}
				
				if(StringUtils.isNotBlank(pk_driver)){
					String driverSql =  "SELECT pk_driver FROM ts_driver WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and def1 = ?";
					String driver =  NWDao.getInstance().queryForObject(driverSql, String.class, pk_driver);
					if(StringUtils.isBlank(driver)){
						carVO.setPk_driver(pk_driver);
					}else{
						carVO.setPk_driver(driver);
					}
				}
				carVO.setDriver_mobile(driver_mobile);
				carVO.setLength(length);
				carVO.setWidth(width);
				carVO.setHeight(height);
				carVO.setVolume(volume);
				carVO.setE_fc_per_km(e_fc_per_km);
				carVO.setL_fc_per_km(l_fc_per_km);
				carVO.setV_fc_per_km(v_fc_per_km);
				carVO.setMileage(mileage);
				carVO.setGps_mileage(gps_mileage);
				carVO.setNote(note);
				carVO.setPhoto(photo);
				carVO.setManufacturer(manufacturer);
				carVO.setOutput_date(output_date);
				carVO.setBrand(brand);
				carVO.setEngine_no(engine_no);
				carVO.setChassis_no(chassis_no);
				carVO.setFrame_no(frame_no);
				carVO.setLic_date(lic_date);
				carVO.setFiling_date(filing_date);
				carVO.setBuy_date(buy_date);
				carVO.setLegal_owner(legal_owner);
				carVO.setBuy_amount(buy_amount);
				carVO.setLegal_service_date(legal_service_date);
				carVO.setCorp_service_date(corp_service_date);
				carVO.setLegal_service_mileage(legal_service_mileage);
				carVO.setCorp_service_mileage(corp_service_mileage);
				carVO.setActual_owner(actual_owner);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",carno + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				carVO.setPk_corp(corp);
				carVO.setMemo(memo);
				carVO.setLoad_weight(load_weight);
				carVO.setGps_id(gps_id);
				carVO.setCustoms(customs);
				carVO.setDangerous_veh(dangerous_veh);
				carVO.setTonnage(tonnage);
				carVO.setWhite_card_number(White_card_number);
				carVO.setDriving_number(driving_number);
				carVO.setOutside_height(outside_height);
				carVO.setOutside_length(outside_length);
				carVO.setOutside_width(outside_width);
				carVO.setAnnual_date(annual_date);
				carVO.setIf_white(if_white);
				carVO.setSupervision_type(supervision_type);
				carVO.setGps_lev(gps_lev);
				carVO.setCreate_user(create_user);
				carVO.setCreate_time(create_time);
				carVO.setDef1(def1);
				carVO.setDef2(def2);
				carVO.setDef3(def3);
				carVO.setDef4(def4);
				carVO.setDef5(def5);
				carVO.setDef6(def6);
				carVO.setDef7(def7);
				carVO.setDef8(def8);
				carVO.setDef9(def9);
				carVO.setDef10(def10);
				carVO.setDef11(def11);
				carVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(carVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	
	private String CheckCarInfo(JsonNode car) {
		JsonNode carno = car.get("carno");
		JsonNode pk_corp = car.get("pk_corp");//公司编码
		if(carno == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strCarno = car.get("carno").getTextValue();
		String strPk_corp = car.get("pk_corp").getTextValue();
		if(strCarno == null || strPk_corp == null){
			return "主表 carrier必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportDriver(String uid,String pwd,String jsonDrivers) throws ApiException{
		logger.info("开始同步司机数据：" + jsonDrivers);
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
		// 空值判断
		if (jsonDrivers.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode drivers = JacksonUtils.readTree(jsonDrivers);
		// 判断参数有效性
		if (drivers == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);	
		}
		JsonNode newDrivers = drivers;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(drivers, "driverConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newDrivers = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode driver : newDrivers) {
			String def1 =driver.get("def1") == null ? null : driver.get("def1").getTextValue();
			Integer dr = driver.get("dr") == null ? 1 : driver.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String driver_name = driver.get("driver_name") == null ? null : driver.get("driver_name").getTextValue();
			if(driver_name == null){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "name不能为空!");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String pk_carrier = driver.get("pk_carrier") == null ? null : driver.get("pk_carrier").getTextValue();
			String sex = driver.get("sex") == null ? null : driver.get("sex").getTextValue();
			Integer age = driver.get("age") == null ? null : driver.get("age").getValueAsInt();
			String id = driver.get("id") == null ? null : driver.get("id").getTextValue();
			String phone = driver.get("phone") == null ? null : driver.get("phone").getTextValue();
			String mobile = driver.get("mobile") == null ? null : driver.get("mobile").getTextValue();
			String driver_lic = driver.get("driver_lic") == null ? null : driver.get("driver_lic").getTextValue();
			UFDateTime lic_time = driver.get("lic_time") == null ? null : new UFDateTime(driver.get("lic_time").getTextValue());
			String lic_type = driver.get("lic_type") == null ? null : driver.get("lic_type").getTextValue();
			UFDate lic_approve_time = driver.get("lic_approve_time") == null ? null : new UFDate(driver.get("lic_approve_time").getTextValue());
			Integer driver_age = driver.get("driver_age") == null ? null : driver.get("driver_age").getValueAsInt();
			String pk_corp = driver.get("pk_corp").getTextValue();
			String addr = driver.get("addr") == null ? null : driver.get("addr").getTextValue();
			String photo = driver.get("photo") == null ? null : driver.get("photo").getTextValue();
			String memo = driver.get("memo") == null ? null : driver.get("memo").getTextValue();
			
			String def2 =driver.get("def2") == null ? null : driver.get("def2").getTextValue();
			String def3 =driver.get("def3") == null ? null : driver.get("def3").getTextValue();
			String def4 =driver.get("def4") == null ? null : driver.get("def4").getTextValue();
			String def5 =driver.get("def5") == null ? null : driver.get("def5").getTextValue();
			String def6 =driver.get("def6") == null ? null : driver.get("def6").getTextValue();
			String def7 =driver.get("def7") == null ? null : driver.get("def7").getTextValue();
			String def8 =driver.get("def8") == null ? null : driver.get("def8").getTextValue();
			String def9 =driver.get("def9") == null ? null : driver.get("def9").getTextValue();
			String def10 =driver.get("def10") == null ? null : driver.get("def10").getTextValue();
			UFDouble def11 = driver.get("def11") == null ? null : new UFDouble(driver.get("def11").getValueAsDouble());
			UFDouble def12 = driver.get("def12") == null ? null : new UFDouble(driver.get("def12").getValueAsDouble());
			
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			DriverVO oldDriverVO = NWDao.getInstance().queryByCondition(DriverVO.class, "def1 =? ", def1);
			//delete
			if(dr == 0){
				if(oldDriverVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", true);
					resultMap.put("errorMessage", "");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldDriverVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldDriverVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldDriverVO != null){
				oldDriverVO.setDriver_name(driver_name);
				if(StringUtils.isNotBlank(pk_carrier)){
					String carrierSql =  "SELECT pk_carrier FROM ts_carrier WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String carrier =  NWDao.getInstance().queryForObject(carrierSql, String.class, pk_carrier);
					if(StringUtils.isBlank(carrier)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", def1 + ":pk_carrier:"+pk_carrier+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldDriverVO.setPk_carrier(carrier);
				}
				oldDriverVO.setSex(sex);
				oldDriverVO.setAge(age);
				oldDriverVO.setId(id);
				oldDriverVO.setPhone(phone);
				oldDriverVO.setMobile(mobile);
				oldDriverVO.setDriver_lic(driver_lic);
				oldDriverVO.setLic_time(lic_time == null ? null : lic_time.toString());
				oldDriverVO.setLic_type(lic_type);
				oldDriverVO.setLic_approve_time(lic_approve_time);
				oldDriverVO.setDriver_age(driver_age);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", def1 + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldDriverVO.setPk_corp(corp);
				oldDriverVO.setAddr(addr);
				oldDriverVO.setPhoto(photo);
				oldDriverVO.setMemo(memo);
				oldDriverVO.setModify_user(create_user);
				oldDriverVO.setModify_time(create_time);
				oldDriverVO.setDef1(def1);
				oldDriverVO.setDef2(def2);
				oldDriverVO.setDef3(def3);
				oldDriverVO.setDef4(def4);
				oldDriverVO.setDef5(def5);
				oldDriverVO.setDef6(def6);
				oldDriverVO.setDef7(def7);
				oldDriverVO.setDef8(def8);
				oldDriverVO.setDef9(def9);
				oldDriverVO.setDef10(def10);
				oldDriverVO.setDef11(def11);
				oldDriverVO.setDef12(def12);
				oldDriverVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldDriverVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				DriverVO driverVO = new DriverVO();
				driverVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(driverVO);
				driverVO.setDriver_code(CodenoHelper.generateCode("t10702"));
				driverVO.setDriver_name(driver_name);
				if(StringUtils.isNotBlank(pk_carrier)){
					String carrierSql =  "SELECT pk_carrier FROM ts_carrier WITH(nolock) WHERE isnull(dr,0)=0 and def1 = ?";
					String carrier =  NWDao.getInstance().queryForObject(carrierSql, String.class, pk_carrier);
					if(StringUtils.isBlank(carrier)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", def1 + ":pk_carrier:"+pk_carrier+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					driverVO.setPk_carrier(carrier);
				}
				driverVO.setSex(sex);
				driverVO.setAge(age);
				driverVO.setId(id);
				driverVO.setPhone(phone);
				driverVO.setMobile(mobile);
				driverVO.setDriver_lic(driver_lic);
				driverVO.setLic_time(lic_time == null ? null : lic_time.toString());
				driverVO.setLic_type(lic_type);
				driverVO.setLic_approve_time(lic_approve_time);
				driverVO.setDriver_age(driver_age);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", def1 + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				driverVO.setPk_corp(corp);
				driverVO.setAddr(addr);
				driverVO.setPhoto(photo);
				driverVO.setMemo(memo);
				driverVO.setCreate_user(create_user);
				driverVO.setCreate_time(create_time);
				driverVO.setDef1(def1);
				driverVO.setDef2(def2);
				driverVO.setDef3(def3);
				driverVO.setDef4(def4);
				driverVO.setDef5(def5);
				driverVO.setDef6(def6);
				driverVO.setDef7(def7);
				driverVO.setDef8(def8);
				driverVO.setDef9(def9);
				driverVO.setDef10(def10);
				driverVO.setDef11(def11);
				driverVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(driverVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	
	
	public String ImportPsndoc(String uid,String pwd,String jsonPsndocs) throws ApiException{
		logger.info("开始同步业务员数据：" + jsonPsndocs);
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
		// 空值判断
		if (jsonPsndocs.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode psndocs = JacksonUtils.readTree(jsonPsndocs);
		// 判断参数有效性
		if (psndocs == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);	
		}
		JsonNode newPsndocs = psndocs;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(psndocs, "psndocConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newPsndocs = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode psndoc : newPsndocs) {
			//判断主表信息
			String errorMsg = CheckPsndocInfo(psndoc);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;			
			}
			String def1 =psndoc.get("def1") == null ? null : psndoc.get("def1").getTextValue();
			Integer dr = psndoc.get("dr") == null ? 1 : psndoc.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String psncode = psndoc.get("psncode").getTextValue();
			String psnname = psndoc.get("psnname").getTextValue();
			String pk_corp = psndoc.get("pk_corp").getTextValue();
			String pk_dept = psndoc.get("pk_dept") == null ? null : psndoc.get("pk_dept").getTextValue();
			String sex = psndoc.get("sex") == null ? null : psndoc.get("sex").getTextValue();
			String id = psndoc.get("id") == null ? null : psndoc.get("id").getTextValue();
			UFDateTime birthdate = psndoc.get("birthdate") == null ? null : new UFDateTime(psndoc.get("birthdate").getTextValue());
			String email = psndoc.get("email") == null ? null : psndoc.get("email").getTextValue();
			String homephone = psndoc.get("homephone") == null ? null : psndoc.get("homephone").getTextValue();
			String mobile = psndoc.get("mobile") == null ? null : psndoc.get("mobile").getTextValue();
			String officephone = psndoc.get("officephone") == null ? null : psndoc.get("officephone").getTextValue();
			String zipcode = psndoc.get("zipcode") == null ? null : psndoc.get("zipcode").getTextValue();
			String addr = psndoc.get("addr") == null ? null : psndoc.get("addr").getTextValue();
			String relationship = psndoc.get("relationship") == null ? null : psndoc.get("relationship").getTextValue();
			Integer marriage = psndoc.get("marriage") == null ? null : psndoc.get("marriage").getValueAsInt();
			String job_num = psndoc.get("job_num") == null ? null : psndoc.get("job_num").getTextValue();
			Integer work_status = psndoc.get("work_status") == null ? null : psndoc.get("work_status").getValueAsInt();
			String degree = psndoc.get("degree") == null ? null : psndoc.get("degree").getTextValue();
			String post = psndoc.get("post") == null ? null : psndoc.get("post").getTextValue();
			UFDate join_time = psndoc.get("join_time") == null ? null : new UFDate(psndoc.get("join_time").getTextValue());
			UFDate leave_time = psndoc.get("leave_time") == null ? null : new UFDate(psndoc.get("leave_time").getTextValue());
			String hometown = psndoc.get("hometown") == null ? null : psndoc.get("hometown").getTextValue();
			String driver_lic = psndoc.get("driver_lic") == null ? null : psndoc.get("driver_lic").getTextValue();
			UFDate lic_time = psndoc.get("lic_time") == null ? null : new UFDate(psndoc.get("lic_time").getTextValue());
			String lic_type = psndoc.get("lic_type") == null ? null : psndoc.get("lic_type").getTextValue();
			UFDate lic_approve_time = psndoc.get("lic_approve_time") == null ? null : new UFDate(psndoc.get("lic_approve_time").getTextValue());
			String photo = psndoc.get("photo") == null ? null : psndoc.get("photo").getTextValue();
			
			String def2 =psndoc.get("def2") == null ? null : psndoc.get("def2").getTextValue();
			String def3 =psndoc.get("def3") == null ? null : psndoc.get("def3").getTextValue();
			String def4 =psndoc.get("def4") == null ? null : psndoc.get("def4").getTextValue();
			String def5 =psndoc.get("def5") == null ? null : psndoc.get("def5").getTextValue();
			String def6 =psndoc.get("def6") == null ? null : psndoc.get("def6").getTextValue();
			String def7 =psndoc.get("def7") == null ? null : psndoc.get("def7").getTextValue();
			String def8 =psndoc.get("def8") == null ? null : psndoc.get("def8").getTextValue();
			String def9 =psndoc.get("def9") == null ? null : psndoc.get("def9").getTextValue();
			String def10 =psndoc.get("def10") == null ? null : psndoc.get("def10").getTextValue();
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			PsndocVO oldPsndocVO = NWDao.getInstance().queryByCondition(PsndocVO.class, "psncode =? ", psncode);
			//delete
			if(dr == 0){
				if(oldPsndocVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", psncode +"不存在，无法删除");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldPsndocVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldPsndocVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldPsndocVO != null){
				oldPsndocVO.setPsncode(psncode);
				oldPsndocVO.setPsnname(psnname);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", psncode + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldPsndocVO.setPk_corp(corp);
				if(StringUtils.isNotBlank(pk_dept)){
					String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) LEFT JOIN nw_corp WITH(nolock) ON nw_dept.pk_corp = nw_corp.pk_corp "
							+ " WHERE isnull(nw_dept.dr,0)=0 and isnull(nw_corp.dr,0)=0 and isnull(nw_dept.locked_flag,'N')='N' "
							+ " AND nw_dept.pk_corp =? AND nw_dept.dept_name = ?";
					String dept =  NWDao.getInstance().queryForObject(deptSql, String.class, corp, pk_dept);
					if(StringUtils.isBlank(pk_dept)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", psncode + ":pk_dept:"+pk_dept+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldPsndocVO.setPk_dept(dept);
				}
				if(StringUtils.isNotBlank(sex)){
					if(sex.equals("男")){
						oldPsndocVO.setSex(1);
					}else if(sex.equals("女")){
						oldPsndocVO.setSex(0);
					}else{
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", psncode + ":sex:"+sex+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
				}
				oldPsndocVO.setId(id);
				oldPsndocVO.setBirthdate(birthdate == null ? null : birthdate.toString());
				oldPsndocVO.setEmail(email);
				oldPsndocVO.setHomephone(homephone);
				oldPsndocVO.setMobile(mobile);
				oldPsndocVO.setOfficephone(officephone);
				oldPsndocVO.setZipcode(zipcode);
				oldPsndocVO.setAddr(addr);
				oldPsndocVO.setRelationship(relationship);
				oldPsndocVO.setMarriage(marriage);
				oldPsndocVO.setJob_num(job_num);
				oldPsndocVO.setWork_status(work_status);
				oldPsndocVO.setDegree(degree);
				if(StringUtils.isNotBlank(post)){
					Object realValue = this.getValueByDataDict("psn_post", post);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", psncode + ":post:"+post+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldPsndocVO.setPost(realValue.toString());
				}
				oldPsndocVO.setJoin_time(join_time);
				oldPsndocVO.setLeave_time(leave_time);
				oldPsndocVO.setHometown(hometown);
				oldPsndocVO.setDriver_lic(driver_lic);
				oldPsndocVO.setLic_time(lic_time);
				oldPsndocVO.setLic_type(lic_type);
				oldPsndocVO.setLic_approve_time(lic_approve_time);
				oldPsndocVO.setPhoto(photo);
				oldPsndocVO.setModify_user(create_user);
				oldPsndocVO.setModify_time(create_time);
				oldPsndocVO.setDef1(def1);
				oldPsndocVO.setDef2(def2);
				oldPsndocVO.setDef3(def3);
				oldPsndocVO.setDef4(def4);
				oldPsndocVO.setDef5(def5);
				oldPsndocVO.setDef6(def6);
				oldPsndocVO.setDef7(def7);
				oldPsndocVO.setDef8(def8);
				oldPsndocVO.setDef9(def9);
				oldPsndocVO.setDef10(def10);
				oldPsndocVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldPsndocVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				PsndocVO psndocVO = new PsndocVO();
				psndocVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(psndocVO);
				psndocVO.setPsncode(psncode);
				psndocVO.setPsnname(psnname);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", psncode + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				psndocVO.setPk_corp(corp);
				if(StringUtils.isNotBlank(pk_dept)){
					String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) LEFT JOIN nw_corp WITH(nolock) ON nw_dept.pk_corp = nw_corp.pk_corp "
							+ " WHERE isnull(nw_dept.dr,0)=0 and isnull(nw_corp.dr,0)=0 and isnull(nw_dept.locked_flag,'N')='N' "
							+ " AND nw_dept.pk_corp =? AND nw_dept.dept_name = ?";
					String dept =  NWDao.getInstance().queryForObject(deptSql, String.class, corp, pk_dept);
					if(StringUtils.isBlank(pk_dept)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", psncode + ":pk_dept:"+pk_dept+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					psndocVO.setPk_dept(dept);
				}
				if(StringUtils.isNotBlank(sex)){
					if(sex.equals("男")){
						psndocVO.setSex(1);
					}else if(sex.equals("女")){
						psndocVO.setSex(0);
					}else{
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", psncode + ":sex:"+sex+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
				}
				psndocVO.setId(id);
				psndocVO.setBirthdate(birthdate == null ? null : birthdate.toString());
				psndocVO.setEmail(email);
				psndocVO.setHomephone(homephone);
				psndocVO.setMobile(mobile);
				psndocVO.setOfficephone(officephone);
				psndocVO.setZipcode(zipcode);
				psndocVO.setAddr(addr);
				psndocVO.setRelationship(relationship);
				psndocVO.setMarriage(marriage);
				psndocVO.setJob_num(job_num);
				psndocVO.setWork_status(work_status);
				psndocVO.setDegree(degree);
				if(StringUtils.isNotBlank(post)){
					Object realValue = this.getValueByDataDict("psn_post", post);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", psncode + ":post:"+post+"不正确！");
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					psndocVO.setPost(realValue.toString());
				}
				psndocVO.setJoin_time(join_time);
				psndocVO.setLeave_time(leave_time);
				psndocVO.setHometown(hometown);
				psndocVO.setDriver_lic(driver_lic);
				psndocVO.setLic_time(lic_time);
				psndocVO.setLic_type(lic_type);
				psndocVO.setLic_approve_time(lic_approve_time);
				psndocVO.setPhoto(photo);
				psndocVO.setCreate_user(create_user);
				psndocVO.setCreate_time(create_time);
				psndocVO.setDef1(def1);
				psndocVO.setDef2(def2);
				psndocVO.setDef3(def3);
				psndocVO.setDef4(def4);
				psndocVO.setDef5(def5);
				psndocVO.setDef6(def6);
				psndocVO.setDef7(def7);
				psndocVO.setDef8(def8);
				psndocVO.setDef9(def9);
				psndocVO.setDef10(def10);
				NWDao.getInstance().saveOrUpdate(psndocVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	private String CheckPsndocInfo(JsonNode psndoc) {
		JsonNode psncode = psndoc.get("psncode"); //编码
		JsonNode psnname = psndoc.get("psnname");//名称
		JsonNode pk_corp = psndoc.get("pk_corp");//公司
		if(psncode == null || psnname == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strPsncode = psndoc.get("psncode").getTextValue();
		String strPsnname = psndoc.get("psnname").getTextValue();
		String strPk_corp = psndoc.get("pk_corp").getTextValue();
		if(strPsncode == null || strPsnname == null || strPk_corp == null){
			return "主表 corp必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportGoodsPack(String uid,String pwd,String jsonGoodsPacks) throws ApiException{
		logger.info("开始同步货品包装数据：" + jsonGoodsPacks);
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
		// 空值判断
		if (jsonGoodsPacks.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode goodsPacks = JacksonUtils.readTree(jsonGoodsPacks);
		// 判断参数有效性
		if (goodsPacks == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newGoodsPacks = goodsPacks;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(goodsPacks, "goodsPackConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newGoodsPacks = (JsonNode) convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage",e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode goodsPack : newGoodsPacks) {
			//判断主表信息
			String errorMsg = CheckGoodsPackInfo(goodsPack);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage",errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
			String def1 =goodsPack.get("def1") == null ? null : goodsPack.get("def1").getTextValue();
			Integer dr = goodsPack.get("dr") == null ? 1 : goodsPack.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage","如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
			
			String code = goodsPack.get("code").getTextValue();
			String name = goodsPack.get("name").getTextValue();
			String pk_corp = goodsPack.get("pk_corp").getTextValue();
			UFDouble length =goodsPack.get("length") == null ? null : new UFDouble(goodsPack.get("length").getValueAsDouble());
			UFDouble width =goodsPack.get("width") == null ? null : new UFDouble(goodsPack.get("width").getValueAsDouble());
			UFDouble height =goodsPack.get("height") == null ? null : new UFDouble(goodsPack.get("height").getValueAsDouble());
			UFDouble weight =goodsPack.get("weight") == null ? null : new UFDouble(goodsPack.get("weight").getValueAsDouble());
			UFDouble volume =goodsPack.get("volume") == null ? null : new UFDouble(goodsPack.get("volume").getValueAsDouble());
			String memo = goodsPack.get("memo") == null ? null : goodsPack.get("memo").getTextValue();
			
			String def2 =goodsPack.get("def2") == null ? null : goodsPack.get("def2").getTextValue();
			String def3 =goodsPack.get("def3") == null ? null : goodsPack.get("def3").getTextValue();
			String def4 =goodsPack.get("def4") == null ? null : goodsPack.get("def4").getTextValue();
			String def5 =goodsPack.get("def5") == null ? null : goodsPack.get("def5").getTextValue();
			String def6 =goodsPack.get("def6") == null ? null : goodsPack.get("def6").getTextValue();
			String def7 =goodsPack.get("def7") == null ? null : goodsPack.get("def7").getTextValue();
			String def8 =goodsPack.get("def8") == null ? null : goodsPack.get("def8").getTextValue();
			String def9 =goodsPack.get("def9") == null ? null : goodsPack.get("def9").getTextValue();
			String def10 =goodsPack.get("def10") == null ? null : goodsPack.get("def10").getTextValue();
			UFDouble def11 =goodsPack.get("def11") == null ? null : new UFDouble(goodsPack.get("def11").getValueAsDouble());
			UFDouble def12 =goodsPack.get("def12") == null ? null : new UFDouble(goodsPack.get("def12").getValueAsDouble());
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			GoodsPackcorpVO oldGoodsPackcorpVO = NWDao.getInstance().queryByCondition(GoodsPackcorpVO.class, "code =? ", code);
			//delete
			if(dr == 0){
				if(oldGoodsPackcorpVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
				    continue;
				}
				oldGoodsPackcorpVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldGoodsPackcorpVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
			//update
			if(oldGoodsPackcorpVO != null){
				oldGoodsPackcorpVO.setCode(code);
				oldGoodsPackcorpVO.setName(name);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
				    continue;
				}
				oldGoodsPackcorpVO.setPk_corp(corp);
				oldGoodsPackcorpVO.setMemo(memo);
				oldGoodsPackcorpVO.setLength(length);
				oldGoodsPackcorpVO.setWeight(weight);
				oldGoodsPackcorpVO.setWidth(width);
				oldGoodsPackcorpVO.setHeight(height);
				oldGoodsPackcorpVO.setVolume(volume);
				oldGoodsPackcorpVO.setCreate_time(create_time);
				oldGoodsPackcorpVO.setCreate_user(create_user);
				oldGoodsPackcorpVO.setDef1(def1);
				oldGoodsPackcorpVO.setDef2(def2);
				oldGoodsPackcorpVO.setDef3(def3);
				oldGoodsPackcorpVO.setDef4(def4);
				oldGoodsPackcorpVO.setDef5(def5);
				oldGoodsPackcorpVO.setDef6(def6);
				oldGoodsPackcorpVO.setDef7(def7);
				oldGoodsPackcorpVO.setDef8(def8);
				oldGoodsPackcorpVO.setDef9(def9);
				oldGoodsPackcorpVO.setDef10(def10);
				oldGoodsPackcorpVO.setDef11(def11);
				oldGoodsPackcorpVO.setDef12(def12);
				oldGoodsPackcorpVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldGoodsPackcorpVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}else{
				//new
				GoodsPackcorpVO goodsPackcorpVO = new GoodsPackcorpVO();
				goodsPackcorpVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(goodsPackcorpVO);
				goodsPackcorpVO.setCode(code);
				goodsPackcorpVO.setName(name);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
				    continue;
				}
				goodsPackcorpVO.setPk_corp(corp);
				goodsPackcorpVO.setMemo(memo);
				goodsPackcorpVO.setLength(length);
				goodsPackcorpVO.setWeight(weight);
				goodsPackcorpVO.setWidth(width);
				goodsPackcorpVO.setHeight(height);
				goodsPackcorpVO.setVolume(volume);
				goodsPackcorpVO.setModify_time(create_time);
				goodsPackcorpVO.setModify_user(create_user);
				goodsPackcorpVO.setDef1(def1);
				goodsPackcorpVO.setDef2(def2);
				goodsPackcorpVO.setDef3(def3);
				goodsPackcorpVO.setDef4(def4);
				goodsPackcorpVO.setDef5(def5);
				goodsPackcorpVO.setDef6(def6);
				goodsPackcorpVO.setDef7(def7);
				goodsPackcorpVO.setDef8(def8);
				goodsPackcorpVO.setDef9(def9);
				goodsPackcorpVO.setDef10(def10);
				goodsPackcorpVO.setDef11(def11);
				goodsPackcorpVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(goodsPackcorpVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	private String CheckGoodsPackInfo(JsonNode goodsPack) {
		JsonNode code = goodsPack.get("code"); //编码
		JsonNode name = goodsPack.get("name");//名称
		JsonNode pk_corp = goodsPack.get("pk_corp");//公司名称
		if(code == null || name == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strCode = goodsPack.get("code").getTextValue();
		String strName = goodsPack.get("name").getTextValue();
		String strPk_corp = goodsPack.get("pk_corp").getTextValue();
		if(strCode == null || strName == null || strPk_corp == null){
			return "主表 goodsPack必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportDept(String uid,String pwd,String jsonDepts) throws ApiException{
		logger.info("开始同步部门数据：" + jsonDepts);
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
		// 空值判断
		if (jsonDepts.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode depts = JacksonUtils.readTree(jsonDepts);
		// 判断参数有效性
		if (depts == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newDepts = depts;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(depts, "deptConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newDepts = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode dept : newDepts) {
			//判断主表信息
			String errorMsg = CheckDeptInfo(dept);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
			String def1 =dept.get("def1") == null ? null : dept.get("def1").getTextValue();
			Integer dr = dept.get("dr") == null ? 1 : dept.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
			
			String dept_code = dept.get("dept_code").getTextValue();
			String dept_name = dept.get("dept_name").getTextValue();
			String pk_corp = dept.get("pk_corp").getTextValue();
			String fatherdept = dept.get("fatherdept") == null ? null : dept.get("fatherdept").getTextValue();
			
			String def2 =dept.get("def2") == null ? null : dept.get("def2").getTextValue();
			String def3 =dept.get("def3") == null ? null : dept.get("def3").getTextValue();
			String def4 =dept.get("def4") == null ? null : dept.get("def4").getTextValue();
			String def5 =dept.get("def5") == null ? null : dept.get("def5").getTextValue();
			String def6 =dept.get("def6") == null ? null : dept.get("def6").getTextValue();
			String def7 =dept.get("def7") == null ? null : dept.get("def7").getTextValue();
			String def8 =dept.get("def8") == null ? null : dept.get("def8").getTextValue();
			String def9 =dept.get("def9") == null ? null : dept.get("def9").getTextValue();
			String def10 =dept.get("def10") == null ? null : dept.get("def10").getTextValue();
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			DeptVO oldDeptVO = NWDao.getInstance().queryByCondition(DeptVO.class, "dept_code =? ", dept_code);
			//delete
			if(dr == 0){
				if(oldDeptVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", dept_code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
				    continue;
				}
				oldDeptVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldDeptVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
			//update
			if(oldDeptVO != null){
				oldDeptVO.setDept_code(dept_code);
				oldDeptVO.setDept_name(dept_name);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", dept_code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
				    continue;
				}
				oldDeptVO.setPk_corp(corp);
				if(StringUtils.isNotBlank(fatherdept)){
					String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and dept_code = ?";
					String pk_fatherdept =  NWDao.getInstance().queryForObject(deptSql, String.class, fatherdept);
					if(StringUtils.isBlank(pk_fatherdept)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", dept_code + ":fatherdept:"+fatherdept+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
					    continue;
					}
					oldDeptVO.setFatherdept(pk_fatherdept);
				}
				oldDeptVO.setModify_time(create_time);
				oldDeptVO.setModify_user(create_user);
				oldDeptVO.setDef1(def1);
				oldDeptVO.setDef2(def2);
				oldDeptVO.setDef3(def3);
				oldDeptVO.setDef4(def4);
				oldDeptVO.setDef5(def5);
				oldDeptVO.setDef6(def6);
				oldDeptVO.setDef7(def7);
				oldDeptVO.setDef8(def8);
				oldDeptVO.setDef9(def9);
				oldDeptVO.setDef10(def10);
				oldDeptVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldDeptVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}else{
				//new
				DeptVO deptVO = new DeptVO();
				deptVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(deptVO);
				deptVO.setDept_code(dept_code);
				deptVO.setDept_name(dept_name);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", dept_code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
				    continue;
				}
				deptVO.setPk_corp(corp);
				
				if(StringUtils.isNotBlank(fatherdept)){
					String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and dept_code = ?";
					String pk_fatherdept =  NWDao.getInstance().queryForObject(deptSql, String.class, fatherdept);
					if(StringUtils.isBlank(pk_fatherdept)){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", dept_code + ":fatherdept:"+fatherdept+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
					    continue;
					}
					deptVO.setFatherdept(pk_fatherdept);
				}
				deptVO.setCreate_time(create_time);
				deptVO.setCreate_user(create_user);
				deptVO.setDef1(def1);
				deptVO.setDef2(def2);
				deptVO.setDef3(def3);
				deptVO.setDef4(def4);
				deptVO.setDef5(def5);
				deptVO.setDef6(def6);
				deptVO.setDef7(def7);
				deptVO.setDef8(def8);
				deptVO.setDef9(def9);
				deptVO.setDef10(def10);
				NWDao.getInstance().saveOrUpdate(deptVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
			    continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	private String CheckDeptInfo(JsonNode dept) {
		JsonNode dept_code = dept.get("dept_code"); //部门编码
		JsonNode dept_name = dept.get("dept_name");//部门名称
		JsonNode pk_corp = dept.get("pk_corp");//公司名称
		if(dept_code == null || dept_name == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strDept_code = dept.get("dept_code").getTextValue();
		String strDept_name = dept.get("dept_name").getTextValue();
		String strPk_corp = dept.get("pk_corp").getTextValue();
		if(strDept_code == null || strDept_name == null|| strPk_corp == null){
			return "主表 corp必填项为空，请确认！";
		}
		return "";
	}
	public String ImportExpenseType(String uid,String pwd,String jsonExpenseTypes) throws ApiException{
		logger.info("开始同步费用类型数据：" + jsonExpenseTypes);
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
		// 空值判断
		if (jsonExpenseTypes.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode expenseTypes = JacksonUtils.readTree(jsonExpenseTypes);
		// 判断参数有效性
		if (expenseTypes == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newExpenseTypes = expenseTypes;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(expenseTypes, "expenseTypeConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newExpenseTypes = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode expenseType : newExpenseTypes) {
			//判断主表信息
			String errorMsg = CheckExpenseTypeInfo(expenseType);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			String def1 =expenseType.get("def1") == null ? null : expenseType.get("def1").getTextValue();
			Integer dr = expenseType.get("dr") == null ? 1 : expenseType.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String code = expenseType.get("code").getTextValue();
			String name = expenseType.get("name").getTextValue();
			Integer type = expenseType.get("type").getValueAsInt();
			String pk_corp = expenseType.get("pk_corp").getTextValue();
			String memo = expenseType.get("memo") == null ? null : expenseType.get("memo").getTextValue();
			String parent_type = expenseType.get("parent_type") == null ? null : expenseType.get("parent_type").getTextValue();
			UFBoolean if_preset = expenseType.get("if_preset") == null ? null : new UFBoolean(expenseType.get("if_preset").getValueAsText());
			
			String def2 =expenseType.get("def2") == null ? null : String.valueOf(expenseType.get("def2").getIntValue());
			String def3 =expenseType.get("def3") == null ? null : expenseType.get("def3").getTextValue();
			String def4 =expenseType.get("def4") == null ? null : expenseType.get("def4").getTextValue();
			String def5 =expenseType.get("def5") == null ? null : expenseType.get("def5").getTextValue();
			String def6 =expenseType.get("def6") == null ? null : expenseType.get("def6").getTextValue();
			String def7 =expenseType.get("def7") == null ? null : expenseType.get("def7").getTextValue();
			String def8 =expenseType.get("def8") == null ? null : expenseType.get("def8").getTextValue();
			String def9 =expenseType.get("def9") == null ? null : expenseType.get("def9").getTextValue();
			String def10 =expenseType.get("def10") == null ? null : expenseType.get("def10").getTextValue();
			UFDouble def11 =expenseType.get("def11") == null ? null : new UFDouble(expenseType.get("def11").getValueAsDouble());
			UFDouble def12 =expenseType.get("def12") == null ? null : new UFDouble(expenseType.get("def12").getValueAsDouble());
			
			ExpenseTypeVO oldExpenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code =? ", code);
			//delete
			if(dr == 0){
				if(oldExpenseTypeVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldExpenseTypeVO.setStatus(VOStatus.DELETED);
				NWDao.getInstance().saveOrUpdate(oldExpenseTypeVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldExpenseTypeVO != null){
				oldExpenseTypeVO.setCode(code);
				oldExpenseTypeVO.setName(name);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldExpenseTypeVO.setPk_corp(corp);
				oldExpenseTypeVO.setExpense_type(type);
				oldExpenseTypeVO.setMemo(memo);
				oldExpenseTypeVO.setIf_preset(if_preset);
				if(StringUtils.isNotBlank(parent_type)){
					Object realValue = this.getValueByDataDict("expense_parenttype", parent_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", code + ":parent_type:"+parent_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldExpenseTypeVO.setParent_type(Integer.parseInt(realValue.toString()));
				}

				oldExpenseTypeVO.setDef1(def1);
				oldExpenseTypeVO.setDef2(def2);
				oldExpenseTypeVO.setDef3(def3);
				oldExpenseTypeVO.setDef4(def4);
				oldExpenseTypeVO.setDef5(def5);
				oldExpenseTypeVO.setDef6(def6);
				oldExpenseTypeVO.setDef7(def7);
				oldExpenseTypeVO.setDef8(def8);
				oldExpenseTypeVO.setDef9(def9);
				oldExpenseTypeVO.setDef10(def10);
				oldExpenseTypeVO.setDef11(def11);
				oldExpenseTypeVO.setDef12(def12);
				oldExpenseTypeVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldExpenseTypeVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				ExpenseTypeVO expenseTypeVO = new ExpenseTypeVO();
				expenseTypeVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(expenseTypeVO);
				expenseTypeVO.setCode(code);
				expenseTypeVO.setName(name);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				expenseTypeVO.setPk_corp(corp);
				expenseTypeVO.setExpense_type(type);
				expenseTypeVO.setMemo(memo);
				expenseTypeVO.setIf_preset(if_preset);
				if(StringUtils.isNotBlank(parent_type)){
					Object realValue = this.getValueByDataDict("expense_parenttype", parent_type);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", code + ":parent_type:"+parent_type+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					expenseTypeVO.setParent_type(Integer.parseInt(realValue.toString()));
				}

				expenseTypeVO.setDef1(def1);
				expenseTypeVO.setDef2(def2);
				expenseTypeVO.setDef3(def3);
				expenseTypeVO.setDef4(def4);
				expenseTypeVO.setDef5(def5);
				expenseTypeVO.setDef6(def6);
				expenseTypeVO.setDef7(def7);
				expenseTypeVO.setDef8(def8);
				expenseTypeVO.setDef9(def9);
				expenseTypeVO.setDef10(def10);
				expenseTypeVO.setDef11(def11);
				expenseTypeVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(expenseTypeVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage", "");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	private String CheckExpenseTypeInfo(JsonNode expenseType) {
		JsonNode code = expenseType.get("code"); //编码
		JsonNode name = expenseType.get("name");//名称
		JsonNode type = expenseType.get("type"); //类型
		JsonNode pk_corp = expenseType.get("pk_corp");//公司编码
		if(code == null || name == null || type == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strCode = expenseType.get("code").getTextValue();
		String strName = expenseType.get("name").getTextValue();
		String strType = expenseType.get("type") == null ? "" : expenseType.get("type").getIntValue() + "" ;
		String strPk_corp = expenseType.get("pk_corp").getTextValue();
		if(strCode == null || strName == null || strType == null || strPk_corp == null){
			return "主表 corp必填项为空，请确认！";
		}
		return "";
	}
	
	public String ImportCarType(String uid,String pwd,String jsonCarTypes) throws ApiException{
		logger.info("开始同步车辆类型数据：" + jsonCarTypes);
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
		// 空值判断
		if (jsonCarTypes.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode carTypes = JacksonUtils.readTree(jsonCarTypes);
		// 判断参数有效性
		if (carTypes == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newCarTypes = carTypes;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(carTypes, "carTypeConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", error.toString());
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			newCarTypes = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage",e1.getMessage());
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		for (JsonNode carType : newCarTypes) {
			//判断主表信息
			String errorMsg = CheckCarTypeInfo(carType);
			if(!errorMsg.isEmpty()){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage",errorMsg);
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				return JacksonUtils.writeValueAsString(result);
			}
			String def1 =carType.get("def1") == null ? null : carType.get("def1").getTextValue();
			Integer dr = carType.get("dr") == null ? 1 : carType.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage","如果传入DR字段，DR字段只能为0或者1");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			String code = carType.get("code").getTextValue();
			String name = carType.get("name").getTextValue();
			String pk_corp = carType.get("pk_corp").getTextValue();
			String use_of = carType.get("use_of") == null ? null : carType.get("use_of").getTextValue();
			UFDouble use_load = carType.get("use_load") == null ? null : new UFDouble(carType.get("use_load").getValueAsDouble());
			UFDouble limit_load = carType.get("limit_load") == null ? null : new UFDouble(carType.get("limit_load").getValueAsDouble());
			UFDouble length = carType.get("length") == null ? null : new UFDouble(carType.get("length").getValueAsDouble());
			UFDouble width = carType.get("width") == null ? null : new UFDouble(carType.get("width").getValueAsDouble());
			UFDouble height = carType.get("height") == null ? null : new UFDouble(carType.get("height").getValueAsDouble());
			UFDouble volume = carType.get("volume") == null ? null : new UFDouble(carType.get("volume").getValueAsDouble());
			String memo = carType.get("memo") == null ? null : carType.get("memo").getTextValue();
			
			String def2 =carType.get("def2") == null ? null : carType.get("def2").getTextValue();
			String def3 =carType.get("def3") == null ? null : carType.get("def3").getTextValue();
			String def4 =carType.get("def4") == null ? null : carType.get("def4").getTextValue();
			String def5 =carType.get("def5") == null ? null : carType.get("def5").getTextValue();
			String def6 =carType.get("def6") == null ? null : carType.get("def6").getTextValue();
			String def7 =carType.get("def7") == null ? null : carType.get("def7").getTextValue();
			String def8 =carType.get("def8") == null ? null : carType.get("def8").getTextValue();
			String def9 =carType.get("def9") == null ? null : carType.get("def9").getTextValue();
			String def10 =carType.get("def10") == null ? null : carType.get("def10").getTextValue();
			UFDouble def11 = carType.get("def11") == null ? null : new UFDouble(carType.get("def11").getValueAsDouble());
			UFDouble def12 = carType.get("def12") == null ? null : new UFDouble(carType.get("def12").getValueAsDouble());
			
			String create_user = ediUserVO.getPk_user();
			UFDateTime create_time = new UFDateTime(new Date());
			
			CarTypeVO oldCarTypeVO = NWDao.getInstance().queryByCondition(CarTypeVO.class, "code =? ", code);
			//delete
			if(dr == 0){
				if(oldCarTypeVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code +"不存在，无法删除");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCarTypeVO.setStatus(VOStatus.DELETED);
				try {
					carTypeService.deleteByPrimaryKey(CarTypeVO.class, oldCarTypeVO.getPrimaryKey());
				} catch (Exception e) {
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",e.getMessage());
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			//update
			if(oldCarTypeVO != null){
				oldCarTypeVO.setCode(code);
				oldCarTypeVO.setName(name);
				if(StringUtils.isNotBlank(use_of)){
					Object realValue = this.getValueByDataDict("car_type_use_of", use_of);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",code + ":use_of:"+use_of+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					oldCarTypeVO.setUse_of(Integer.parseInt(realValue.toString()));
				}
				oldCarTypeVO.setUse_load(use_load);
				oldCarTypeVO.setLimit_load(limit_load);
				oldCarTypeVO.setLength(length);
				oldCarTypeVO.setWidth(width);
				oldCarTypeVO.setHeight(height);
				oldCarTypeVO.setVolume(volume);
				oldCarTypeVO.setModify_time(create_time);
				oldCarTypeVO.setModify_user(create_user);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldCarTypeVO.setPk_corp(corp);
				oldCarTypeVO.setMemo(memo);
				oldCarTypeVO.setDef1(def1);
				oldCarTypeVO.setDef2(def2);
				oldCarTypeVO.setDef3(def3);
				oldCarTypeVO.setDef4(def4);
				oldCarTypeVO.setDef5(def5);
				oldCarTypeVO.setDef6(def6);
				oldCarTypeVO.setDef7(def7);
				oldCarTypeVO.setDef8(def8);
				oldCarTypeVO.setDef9(def9);
				oldCarTypeVO.setDef10(def10);
				oldCarTypeVO.setDef11(def11);
				oldCarTypeVO.setDef12(def12);
				oldCarTypeVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(oldCarTypeVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}else{
				//new
				CarTypeVO carTypeVO = new CarTypeVO();
				carTypeVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(carTypeVO);
				carTypeVO.setCode(code);
				carTypeVO.setName(name);
				if(StringUtils.isNotBlank(use_of)){
					Object realValue = this.getValueByDataDict("car_type_use_of", use_of);
					if(realValue == null){
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage",code + ":use_of:"+use_of+"不正确！");
						resultMap.put("entityKey", def1);
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					carTypeVO.setUse_of(Integer.parseInt(realValue.toString()));
				}
				carTypeVO.setUse_load(use_load);
				carTypeVO.setLimit_load(limit_load);
				carTypeVO.setLength(length);
				carTypeVO.setWidth(width);
				carTypeVO.setHeight(height);
				carTypeVO.setVolume(volume);
				carTypeVO.setCreate_time(create_time);
				carTypeVO.setCreate_user(create_user);
				String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
				String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
				if(StringUtils.isBlank(corp)){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code + ":pk_corp:"+pk_corp+"不正确！");
					resultMap.put("entityKey", def1);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				carTypeVO.setPk_corp(corp);
				carTypeVO.setMemo(memo);
				carTypeVO.setDef1(def1);
				carTypeVO.setDef2(def2);
				carTypeVO.setDef3(def3);
				carTypeVO.setDef4(def4);
				carTypeVO.setDef5(def5);
				carTypeVO.setDef6(def6);
				carTypeVO.setDef7(def7);
				carTypeVO.setDef8(def8);
				carTypeVO.setDef9(def9);
				carTypeVO.setDef10(def10);
				carTypeVO.setDef11(def11);
				carTypeVO.setDef12(def12);
				NWDao.getInstance().saveOrUpdate(carTypeVO);
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", def1);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
		}
		return JacksonUtils.writeValueAsString(result);
	}
	
	private String CheckCarTypeInfo(JsonNode carType) {
		JsonNode code = carType.get("code");
		JsonNode name = carType.get("name");
		JsonNode pk_corp = carType.get("pk_corp");//公司编码
		if(code == null || name == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		String strCode = carType.get("code").getTextValue();
		String strName = carType.get("name").getTextValue();
		String strPk_corp = carType.get("pk_corp").getTextValue();
		if(strCode == null || strName == null || strPk_corp == null){
			return "主表 carrier必填项为空，请确认！";
		}
		return "";
	}
	
	private Object getValueByDataDict(String dictName, String value){
		try {
			return Integer.parseInt(value);//如果可以直接转换成Int，说明这本身就是value
		} catch (Exception e) {
			String sql = "SELECT nw_data_dict_b.value AS value FROM nw_data_dict_b WITH(NOLOCK)"
					+ " LEFT JOIN nw_data_dict WITH(NOLOCK) ON "
					+ " nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ " WHERE nw_data_dict.datatype_code =? and display_name = ?";
			return NWDao.getInstance().queryForObject(sql, Object.class,dictName, value);
		}
	}
	
	
	public String ImportArea(String uid,String pwd,String jsonArea) throws ApiException{
		logger.info("开始同步区域数据：" + jsonArea);
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
		// 空值判断
		if (jsonArea.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode areas = JacksonUtils.readTree(jsonArea);
		// 判断参数有效性
		if (areas == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (JsonNode area : areas){
			String id = area.get("id") == null ? null : area.get("id").getTextValue();
			String code = area.get("code") == null ? "" : area.get("code").getTextValue();
			if(StringUtils.isBlank(code) ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "code:不能为空！");
				resultMap.put("entityKey", id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String CityName = area.get("CityName") == null ? "" : area.get("CityName").getTextValue();
			if(StringUtils.isBlank(CityName) ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "CityName:不能为空！");
				resultMap.put("entityKey", id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			Integer RecordStatus = area.get("RecordStatus") == null ? 1 : area.get("RecordStatus").getValueAsInt();
			if(RecordStatus != null && RecordStatus!=1 && RecordStatus!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "RecordStatus：不正确");
				resultMap.put("entityKey", id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			AreaVO oldAreaVO = NWDao.getInstance().queryByCondition(AreaVO.class, "code =? ", code);
			
			if(RecordStatus == 0){
				if(oldAreaVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",code +"：不存在，无法删除");
					resultMap.put("entityKey", id);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldAreaVO.setStatus(VOStatus.DELETED);
				try {
					areaService.deleteByPrimaryKey(AddressVO.class, oldAreaVO.getPrimaryKey());
				} catch (Exception e) {
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage",e.getMessage());
					resultMap.put("entityKey", id);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			
			String CountryName = area.get("CountryName") == null ? null : area.get("CountryName").getTextValue();
			String CountryCode = area.get("CountryCode") == null ? null : area.get("CountryCode").getTextValue();
			
			if(oldAreaVO != null){
				oldAreaVO.setCode(code);
				oldAreaVO.setName(CityName);
				oldAreaVO.setDef1(id);
				oldAreaVO.setDef2(CountryName);
				oldAreaVO.setDef3(CountryCode);
				oldAreaVO.setModify_time(new UFDateTime(new Date()));
				oldAreaVO.setModify_user(ediUserVO.getPk_user());
				oldAreaVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(oldAreaVO);
			}else{
				//new
				AreaVO areaVO = new AreaVO();
				areaVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(areaVO);
				areaVO.setCode(code);
				areaVO.setName(CityName);
				areaVO.setDef1(id);
				areaVO.setDef2(CountryName);
				areaVO.setDef3(CountryCode);
				areaVO.setPk_corp("0001");
				areaVO.setCreate_time(new UFDateTime(new Date()));
				areaVO.setCreate_user(ediUserVO.getPk_user());
				toBeUpdate.add(areaVO);
			}
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", true);
			resultMap.put("errorMessage","");
			resultMap.put("entityKey", id);
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			continue;
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return JacksonUtils.writeValueAsString(result);
	}
	
	public String ImportArea2(String uid,String pwd,String jsonArea) throws ApiException{
		logger.info("开始同步区域数据：" + jsonArea);
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
		// 空值判断
		if (jsonArea.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode areas = JacksonUtils.readTree(jsonArea);
		// 判断参数有效性
		if (areas == null) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "输入的参数不是要求的格式，请修改！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (JsonNode area : areas){
			String Id = area.get("Id") == null ? null : area.get("Id").getTextValue();
			String code = area.get("code") == null ? "" : area.get("code").getTextValue();
			if(StringUtils.isBlank(code) ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "code:不能为空！");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			String ShotName = area.get("ShotName") == null ? "" : area.get("ShotName").getTextValue();
			if(StringUtils.isBlank(ShotName) ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "ShotName:不能为空！");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			Integer RecordStatus = area.get("RecordStatus") == null ? 0 : area.get("RecordStatus").getValueAsInt();
			if(RecordStatus != null && RecordStatus!=1 && RecordStatus!=0 ){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", "RecordStatus：不正确");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			AreaVO oldAreaVO = NWDao.getInstance().queryByCondition(AreaVO.class, "code =? ", code);
			
			if(RecordStatus == 1){
				if(oldAreaVO == null){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", code +"：不存在，无法删除");
					resultMap.put("entityKey", Id);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				oldAreaVO.setStatus(VOStatus.DELETED);
				try {
					areaService.deleteByPrimaryKey(AddressVO.class, oldAreaVO.getPrimaryKey());
				} catch (Exception e) {
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", e.getMessage());
					resultMap.put("entityKey", Id);
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", true);
				resultMap.put("errorMessage","");
				resultMap.put("entityKey", Id);
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			
			String name = area.get("name") == null ? null : area.get("name").getTextValue();
			
			if(oldAreaVO != null){
				oldAreaVO.setCode(code);
				oldAreaVO.setName(ShotName);
				oldAreaVO.setDef1(Id);
				oldAreaVO.setDef3(name);
				oldAreaVO.setModify_time(new UFDateTime(new Date()));
				oldAreaVO.setModify_user(ediUserVO.getPk_user());
				oldAreaVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(oldAreaVO);
			}else{
				//new
				AreaVO areaVO = new AreaVO();
				areaVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(areaVO);
				areaVO.setCode(code);
				areaVO.setName(ShotName);
				areaVO.setDef1(Id);
				areaVO.setDef3(name);
				areaVO.setPk_corp("0001");
				areaVO.setCreate_time(new UFDateTime(new Date()));
				areaVO.setCreate_user(ediUserVO.getPk_user());
				toBeUpdate.add(areaVO);
			}
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", true);
			resultMap.put("errorMessage", "");
			resultMap.put("entityKey", Id);
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			continue;
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return JacksonUtils.writeValueAsString(result);
	}
	
	
}
