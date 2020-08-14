package com.tms.services.jdQuotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.ApiException;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.service.api.AuthenticationService;
import org.nw.utils.CodenoHelper;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

import com.tms.constants.DataDictConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.QuoteTypeConst;
import com.tms.service.cm.ContractService;
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractVO;
import com.tms.vo.cm.ExpenseTypeVO;

@SuppressWarnings("deprecation")
public class JDQuotationEndPoint extends ServletEndpointSupport {
	
	Logger logger = Logger.getLogger("EDI");
	
	private ContractService contractService = (ContractService) SpringContextHolder.getApplicationContext().getBean("contractServiceImpl");
	
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
	
	public String ImportQuotation(String uid,String pwd,String jsonQuotations) throws ApiException{
		logger.info("开始同步报价数据：" + jsonQuotations);
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
		if (jsonQuotations.isEmpty()) {
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", false);
			resultMap.put("errorMessage", "未提供任何数据，请确认！");
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode quotations = JacksonUtils.readTree(jsonQuotations);
		// 判断参数有效性
		if (quotations == null) {
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
		for (JsonNode quotation : quotations){
			String id = quotation.get("Id") == null ? null : quotation.get("Id").getTextValue();
			String code = quotation.get("Code") == null ? null : quotation.get("Code").getTextValue();//报价单单号
			String name = quotation.get("Name") == null ? null : quotation.get("Name").getTextValue();//报价单名字
			String vendorId = quotation.get("VendorId") == null ? null : quotation.get("VendorId").getTextValue();//供应商编号
			String beginDate = quotation.get("BeginDate") == null ? null : quotation.get("BeginDate").getTextValue();//报价起始日期
			String endDate = quotation.get("EndDate") == null ? null : quotation.get("EndDate").getTextValue();//报价到期日期
			String comments = quotation.get("Comments") == null ? null : quotation.get("Comments").getTextValue();//备注
			boolean isInlandTransportation = quotation.get("isInlandTransportation") == null ? false : quotation.get("isInlandTransportation").getBooleanValue();//是否国内运输供方
			if(!isInlandTransportation){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", code +"：不是国内运输供方");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
//			String tenantId = quotation.get("TenantId") == null ? null : quotation.get("TenantId").getTextValue();//公司Id
//			if(StringUtils.isBlank(tenantId)){
//				failed.add("tenantId不能为空，请修改！");
//				result.put("failed", failed);
//				return JacksonUtils.writeValueAsString(result);
//			}
			Integer recordStatus = quotation.get("RecordStatus") == null ? 1 : quotation.get("RecordStatus").getIntValue();//数据状态:0=删除,1=可用
			
			String sql = "SELECT top 1 CASE WHEN carr_type=1 THEN 1 WHEN carr_type=2 THEN 0 ELSE 2 END  AS contract_type, "
					+ " CASE WHEN carr_type=1 THEN pk_carrier END  AS pk_carrier, "
					+ " CASE WHEN carr_type=2 THEN '551db20c88d84506812629a4f3eddd48' END  AS bala_customer "
					+ "FROM ts_carrier WITH(NOLOCK) WHERE def1=?";
			List<Map<String,Object>> contractMsgs = NWDao.getInstance().queryForList(sql, vendorId);
			if(contractMsgs == null || contractMsgs.size() == 0){
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", code +"vendorId错误");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			Map<String,Object> contractMsg = contractMsgs.get(0);
			Integer contract_type = null;
			try {
				contract_type = Integer.parseInt(String.valueOf(contractMsg.get("contract_type")));
				if(contract_type != 0 && contract_type !=1){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", false);
					resultMap.put("errorMessage", code +"供应商编号错误");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
				
			} catch (Exception e) {
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("success", false);
				resultMap.put("errorMessage", code +"供应商编号错误");
				resultMap.put("entityKey", "");
				resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
				resultMap.put("serviceType", "TMS");
				result.add(resultMap);
				continue;
			}
			
			ContractVO[] oldContractVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ContractVO.class, "contractno=?", code);
			if(oldContractVOs != null && oldContractVOs.length > 0){
				String[] primaryKeys = new String[oldContractVOs.length];
				for(int i=0; i<oldContractVOs.length;i++){
					primaryKeys[i] = oldContractVOs[i].getPrimaryKey();
				}
				try {
					contractService.batchDelete(ContractVO.class, primaryKeys);
				} catch (Exception e) {
					if(recordStatus == 0){//记录删除
						Map<String,Object> resultMap = new HashMap<String, Object>();
						resultMap.put("success", false);
						resultMap.put("errorMessage", code +"删除出错：" + e.getMessage());
						resultMap.put("entityKey", "");
						resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
						resultMap.put("serviceType", "TMS");
						result.add(resultMap);
						continue;
					}
					continue;
				}
				if(recordStatus == 0){//记录删除
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", true);
					resultMap.put("errorMessage", "删除："+ code);
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
			}else{
				if(recordStatus == 0){
					Map<String,Object> resultMap = new HashMap<String, Object>();
					resultMap.put("success", true);
					resultMap.put("errorMessage", code +"：不存在，无法删除 ");
					resultMap.put("entityKey", "");
					resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
					resultMap.put("serviceType", "TMS");
					result.add(resultMap);
					continue;
				}
			}
			Map<String,List<ContractVO>> sameProductContracts = new HashMap<String,List<ContractVO>>();
			JsonNode costFees = quotation.get("CostFees");
			if(costFees != null && costFees.size() > 0){
				for(JsonNode costFee : costFees){
					String costId = costFee.get("CostId") == null ? null : costFee.get("CostId").getTextValue();
					String productCode = costFee.get("ProductCode") == null ? null : costFee.get("ProductCode").getTextValue();
					String transTypeSql = "SELECT pk_trans_type FROM ts_trans_type WHERE code =?";
					String pk_trans_type = NWDao.getInstance().queryForObject(transTypeSql, String.class, productCode);
					String feeCode = costFee.get("FeeCode") == null ? null : costFee.get("FeeCode").getTextValue();
					//String expenseSql = "SELECT * FROM ts_expense_type WHERE code = ?";
					ExpenseTypeVO expenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code=?", feeCode);
					String citySql = "SELECT top 1 pk_area FROM ts_area WHERE code =? OR code =?";
					String beginCityCode = costFee.get("BeginCityCode") == null ? null : costFee.get("BeginCityCode").getTextValue();
					String beginCustomsCode = costFee.get("BeginCustomsCode") == null ? null : costFee.get("BeginCustomsCode").getTextValue();
					String beginCity = NWDao.getInstance().queryForObject(citySql, String.class, beginCityCode,beginCustomsCode);
					String endCityCode = costFee.get("EndCityCode") == null ? null : costFee.get("EndCityCode").getTextValue();
					String endCustomsCode = costFee.get("EndCustomsCode") == null ? null : costFee.get("EndCustomsCode").getTextValue();
					String endCity = NWDao.getInstance().queryForObject(citySql, String.class, endCityCode,endCustomsCode);
					List<ContractVO> contractVOs = sameProductContracts.get(pk_trans_type);
					if(contractVOs == null){
						contractVOs = new ArrayList<ContractVO>();
						//判断合同类型，如果是客户合同，需要为每个客户生成一份合同。
						if(contract_type == 0){//客户
							//查看系统的所有客户
							CustomerVO[] customerVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CustomerVO.class, "cust_type=2");
							if(customerVOs == null || customerVOs.length == 0){
								Map<String,Object> resultMap = new HashMap<String, Object>();
								resultMap.put("success", true);
								resultMap.put("errorMessage", code +"系统尚未维护客户信息");
								resultMap.put("entityKey", "");
								resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
								resultMap.put("serviceType", "TMS");
								result.add(resultMap);
								continue;
							}
							for(CustomerVO customerVO : customerVOs){
								ContractVO contractVO = new ContractVO();
								contractVO.setStatus(VOStatus.NEW);
								NWDao.setUuidPrimaryKey(contractVO);
								contractVO.setCode(CodenoHelper.generateCode("t409"));
								contractVO.setCreate_time(new UFDateTime(new Date()));
								contractVO.setCreate_user("32e6103e697f44b7ac98477583af49cd");
								contractVO.setPk_corp("0001");
								contractVO.setName(name);
								contractVO.setContract_type(0);
								contractVO.setBala_customer(customerVO.getPk_customer());
								contractVO.setEffective_date(new UFDateTime(beginDate));
								contractVO.setInvalid_date(new UFDateTime(endDate));
								contractVO.setContractno(code);
								contractVO.setMemo(comments);
								contractVO.setTrans_type(pk_trans_type);
								contractVO.setDef1(id);
								toBeUpdate.add(contractVO);
								contractVOs.add(contractVO);
							}
							sameProductContracts.put(pk_trans_type, contractVOs);
						}
						if(contract_type == 1){//承运商
							ContractVO contractVO = new ContractVO();
							contractVO.setStatus(VOStatus.NEW);
							NWDao.setUuidPrimaryKey(contractVO);
							contractVO.setCode(CodenoHelper.generateCode("t410"));
							contractVO.setCreate_time(new UFDateTime(new Date()));
							contractVO.setCreate_user("32e6103e697f44b7ac98477583af49cd");
							contractVO.setPk_corp("0001");
							contractVO.setName(name);
							contractVO.setContract_type(1);
							contractVO.setPk_carrier(contractMsg.get("pk_carrier") == null ? null : contractMsg.get("pk_carrier").toString());
							contractVO.setEffective_date(new UFDateTime(beginDate));
							contractVO.setInvalid_date(new UFDateTime(endDate));
							contractVO.setContractno(code);
							contractVO.setMemo(comments);
							contractVO.setTrans_type(pk_trans_type);
							contractVO.setDef1(id);
							toBeUpdate.add(contractVO);
							contractVOs.add(contractVO);
							sameProductContracts.put(pk_trans_type, contractVOs);
						}
					}
					
					JsonNode costFeeUnits = costFee.get("CostFeeUnits");
					if(costFeeUnits != null && costFeeUnits.size() > 0){
						for(JsonNode costFeeUnit : costFeeUnits){
							String costFeeUnit_comments = costFeeUnit.get("Comments") == null ? null : costFeeUnit.get("Comments").getTextValue();
							String quotationAttribute = costFeeUnit.get("QuotationAttribute") == null ? null : costFeeUnit.get("QuotationAttribute").getTextValue();
							Integer unitPriceType = costFeeUnit.get("UnitPriceType") == null ? null : costFeeUnit.get("UnitPriceType").getIntValue();
							boolean isReturn = costFeeUnit.get("IsReturn") == null ? null : costFeeUnit.get("IsReturn").getBooleanValue();
							double minPrice = costFeeUnit.get("MinPrice") == null ? null : costFeeUnit.get("MinPrice").getDoubleValue();
							String billingTypeCode = costFeeUnit.get("BillingTypeCode") == null ? null : costFeeUnit.get("BillingTypeCode").getTextValue();//税种
							String billingTypeFax = costFeeUnit.get("BillingTypeFax") == null ? null : String.valueOf(costFeeUnit.get("BillingTypeFax").getDoubleValue());//税率
							if(StringUtils.isNotBlank(billingTypeCode)){
								String codeSql = "SELECT value FROM nw_data_dict_b WITH(NOLOCK) WHERE pk_data_dict='8c6af0da0e524d52b3db7d0cb829d8e3' AND display_name=?";
								billingTypeCode = NWDao.getInstance().queryForObject(codeSql, String.class, billingTypeCode);
							}
							if(StringUtils.isNotBlank(billingTypeFax)){
								String faxSql = "SELECT value FROM nw_data_dict_b WITH(NOLOCK) WHERE pk_data_dict='1527384346364410965457abada9ef77' AND value=?";
								String fax = NWDao.getInstance().queryForObject(faxSql, String.class, billingTypeFax);
								if(StringUtils.isNotBlank(fax)){
									billingTypeFax = fax;
								}
							}
							String pk_car_type = null;
							if(StringUtils.isNotBlank(quotationAttribute)){
								CarTypeVO carTypeVO = NWDao.getInstance().queryByCondition(CarTypeVO.class, "def1=?", quotationAttribute);
								if(carTypeVO != null){
									pk_car_type = carTypeVO.getPk_car_type();
								}
							}
							for(ContractVO contractVO : contractVOs){
								ContractBVO contractBVO = new ContractBVO();
								contractBVO.setStatus(VOStatus.NEW);
								contractBVO.setPk_contract(contractVO.getPk_contract());
								NWDao.setUuidPrimaryKey(contractBVO);
								contractBVO.setStart_addr(beginCity);
								contractBVO.setStart_addr_type(DataDictConst.ADDR_TYPE.CITY.intValue());
								contractBVO.setEnd_addr(endCity);
								contractBVO.setEnd_addr_type(DataDictConst.ADDR_TYPE.CITY.intValue());
								contractBVO.setLowest_fee(new UFDouble(minPrice));
								contractBVO.setIf_return(new UFBoolean(isReturn));
								contractBVO.setEquip_type(pk_car_type);
								if(expenseTypeVO != null){
									contractBVO.setPk_expense_type(expenseTypeVO.getPk_expense_type());
									if("2".equals(expenseTypeVO.getDef2())){
										contractBVO.setIf_optional(UFBoolean.TRUE);
									}
								}
								contractBVO.setQuote_type(QuoteTypeConst.INTERVAL);
								contractBVO.setPrice_type(PriceTypeConst.UNIT_PRICE);
								contractBVO.setValuation_type(unitPriceType);
								contractBVO.setUrgent_level(0);
								contractBVO.setDef1(costId);
								contractBVO.setTax_cat(billingTypeCode == null ? null : Integer.parseInt(billingTypeCode));
								contractBVO.setTax_rate(new UFDouble( billingTypeFax== null ? "0" : billingTypeFax));
								contractBVO.setMemo(costFeeUnit_comments);
								List<Map<String,Double>> intervalPrices = new ArrayList<Map<String,Double>>();
								JsonNode costFeeUnitPrices = costFeeUnit.get("CostFeeUnitPrices");
								if(costFeeUnitPrices != null && costFeeUnitPrices.size() > 0){
									for(JsonNode costFeeUnitPrice : costFeeUnitPrices){
										double endValue = costFeeUnitPrice.get("EndValue") == null ? null : costFeeUnitPrice.get("EndValue").getDoubleValue();
										double unitPrice = costFeeUnitPrice.get("UnitPrice") == null ? null : costFeeUnitPrice.get("UnitPrice").getDoubleValue();
										Map<String,Double> intervalPrice = new HashMap<String,Double>();
										intervalPrice.put("endValue", endValue);
										intervalPrice.put("unitPrice", unitPrice);
										intervalPrices.add(intervalPrice);
									}
								}
								if(intervalPrices.size() > 0){
									//对报价进行排序
									sortIntervalPrices(intervalPrices);
									for(int i=1;i<intervalPrices.size()+1 && i<13;i++){//只记录12个区间
										String interval = "interval" + i;
										String price = "price" + i;
										contractBVO.setAttributeValue(interval, new UFDouble(intervalPrices.get(i-1).get("endValue")));
										contractBVO.setAttributeValue(price, new UFDouble(intervalPrices.get(i-1).get("unitPrice")));
									}
								}
								toBeUpdate.add(contractBVO);
							}
						}
					}
				}
			}
			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", true);
			resultMap.put("errorMessage", code);
			resultMap.put("entityKey", "");
			resultMap.put("responseDate", (new UFDateTime(new Date())).toString());
			resultMap.put("serviceType", "TMS");
			result.add(resultMap);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return JacksonUtils.writeValueAsString(result);
	}
	
	private void sortIntervalPrices(List<Map<String,Double>> intervalPrices){
		if(intervalPrices == null || intervalPrices.size() == 0){
			return;
		}
		//每次从后往前冒一个最小值，且每次能确定一个数在序列中的最终位置		
		for(int i=0;i<intervalPrices.size();i++){//比较n-1次
			boolean exchange = true; //冒泡的改进，若在一趟中没有发生逆序，则该序列已有序
			for (int j = intervalPrices.size()-1; j >i; j--){// 每次从后边冒出一个最小值
				if (intervalPrices.get(j).get("endValue") < intervalPrices.get(j-1).get("endValue")){//发生逆序，则交换
					Map<String,Double> temp = intervalPrices.get(j);
					intervalPrices.set(j, intervalPrices.get(j-1));
					intervalPrices.set(j-1, temp);
		            exchange = false;
				}
			}
			if (exchange) {
				return;
			}
		}
	}
	

}
