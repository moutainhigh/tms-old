package com.tms.services.tmsStandardApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.dao.NWDao;
import org.nw.exception.ApiException;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.service.api.AuthenticationService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

import com.tms.service.cm.ContractService;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractVO;

@SuppressWarnings("deprecation")
public class ImportContractEndpoint extends ServletEndpointSupport{
	
	private static String  EDI_USER_CODE = "EdiUser";
	private final static String TRUE = "TRUE";
	private final static String FALSE = "FALSE";
	
	private static	String ediUserSql = "SELECT * FROM nw_user WHERE isnull(dr,0) = 0 AND isnull(locked_flag,'N')='N' AND user_code = ?";
	private static	UserVO ediUserVO = NWDao.getInstance().queryForObject(ediUserSql, UserVO.class, EDI_USER_CODE);
	
	
	private ContractService service;
	private ParamVO paramVO = new ParamVO();
	
	public String importContracts (String uid,String pwd,String jsonContracts) throws ApiException{
		Map<String,List<String>> result = new HashMap<String, List<String>>();
		List<String> success = new ArrayList<String>();
		List<String> failed = new ArrayList<String>();
		
		String authError = checkBeforeImport(uid,pwd);
		if(!authError.isEmpty()){
			failed.add(authError);
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
			 
		}
		
		//空值判断
		if(jsonContracts.isEmpty()){
			failed.add("未提供任何数据，请确认！");
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
			 
		}
		JsonNode contracts = JacksonUtils.readTree(jsonContracts);
		//判断参数有效性
		if(contracts  == null){
			failed.add("输入的参数不是要求的格式，请修改！");
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newContracts = contracts;
//		try {
//			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(contracts, "contractConvert.xml");
//			Object error = convertResult.get(FALSE);
//			if(error != null && StringUtils.isNotBlank(error.toString())){
//				failed.add(error.toString());
//				result.put("failed", failed);
//				return JacksonUtils.writeValueAsString(result);
//			}
//			newContracts = (JsonNode)convertResult.get(TRUE);
//		} catch (Exception e1) {
//			failed.add(e1.getMessage());
//			result.put("failed", failed);
//			return JacksonUtils.writeValueAsString(result);
//		}
		for(JsonNode contract : newContracts ){
			HYBillVO hYContractVO = new HYBillVO();
			String code = contract.get("code") == null ? "" : contract.get("code").getTextValue();
			
			Integer dr = contract.get("dr") == null ? 0 : contract.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				 failed.add("DR字段只能为0或者1");
				 continue;
			}
			//delete
			ContractVO oldContractVO = NWDao.getInstance().queryByCondition(ContractVO.class, "code =? ", code);
			if(dr == 1){
				if(StringUtils.isBlank(code)){
					 failed.add("删除时，code不能为空!");
					 continue;
				}else{
					if(oldContractVO == null){
						failed.add(code +"不存在，无法删除");
						continue;
					}else{
						try {
							//service.batchDelete(ContractVO.class, new String[]{oldContractVO.getPrimaryKey()});
							NWDao.getInstance().delete(oldContractVO);
						} catch (Exception e) {
							failed.add(code +"无法删除:" +e.getMessage());
							continue;
						}
						success.add("删除："+code);
						continue;
					}
				}
			}
			ContractVO contractVO = new ContractVO();
			contractVO.setCode(code);
			String pk_corp = contract.get("pk_corp").getTextValue();
			String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
			String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
			if(StringUtils.isBlank(corp)){
				failed.add(code + ":pk_corp:"+pk_corp+"不正确！");
				continue;
			}else{
				contractVO.setPk_corp(corp);
			}
			String memo =  contract.get("memo") == null ? null : contract.get("memo").getTextValue();
			contractVO.setMemo(memo);
			Integer contract_type = contract.get("contract_type").getValueAsInt();
			if(contract_type == 0){
				String bala_customer = contract.get("bala_customer") == null ? null : contract.get("bala_customer").getTextValue();
				if(StringUtils.isNotBlank(bala_customer)){
					String cust_sql = "SELECT ts_customer.pk_customer FROM ts_customer WITH(nolock) WHERE isnull(ts_customer.dr,0)=0"
							+ " and isnull(ts_customer.locked_flag,'N')='N' and ts_customer.cust_code = ?";
					String customer = NWDao.getInstance().queryForObject(cust_sql, String.class, bala_customer);
					if(StringUtils.isBlank(customer)){
						failed.add(code + ":bala_customer:"+bala_customer+"不正确！");
						continue;
					}else{
						contractVO.setBala_customer(customer);
					}
				}
			}else if(contract_type == 1){
				String pk_carrier = contract.get("pk_carrier") == null ? null : contract.get("pk_carrier").getTextValue();
				if(StringUtils.isNotBlank(pk_carrier)){
					String carrSql = "SELECT pk_carrier FROM ts_carrier WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carr_code = ?";
					String carr =  NWDao.getInstance().queryForObject(carrSql, String.class, pk_carrier);
					if(StringUtils.isBlank(carr)){
						failed.add(code + ":pk_carrier:"+pk_carrier+"不正确！");
						continue;
					}else{
						contractVO.setPk_carrier(carr);
					}
				}
			}
			contractVO.setContract_type(contract_type);
			String currency = contract.get("currency").getTextValue();
			contractVO.setCurrency(currency);
			
			UFDateTime invalid_date = new UFDateTime(contract.get("invalid_date").getTextValue());
			UFDateTime effective_date = new UFDateTime(contract.get("effective_date").getTextValue());
			contractVO.setInvalid_date(invalid_date);
			contractVO.setEffective_date(effective_date);
			
			String contractno = contract.get("contractno") == null ? null :contract.get("contractno").getTextValue();
			contractVO.setContractno(contractno);
			String trans_type = contract.get("trans_type").getTextValue();
			String transtypeSql = "SELECT pk_trans_type FROM ts_trans_type WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code = ?";
			String transtype =  NWDao.getInstance().queryForObject(transtypeSql, String.class, trans_type);
			if(StringUtils.isBlank(transtype)){
				failed.add(code + ":trans_type:"+trans_type+"不正确！");
				continue;
			}else{
				contractVO.setTrans_type(transtype);
			}
			String name = contract.get("name") == null ? null : contract.get("name").getTextValue();
			contractVO.setName(name);
			
			String def10 = contract.get("def10") == null ? null : contract.get("def10").getTextValue();
			contractVO.setDef10(def10);
			String def2 = contract.get("def2") == null ? null : contract.get("def2").getTextValue();
			contractVO.setDef2(def2);
			String def1 = contract.get("def1") == null ? null : contract.get("def1").getTextValue();
			contractVO.setDef1(def1);
			String def4 = contract.get("def4") == null ? null : contract.get("def4").getTextValue();
			contractVO.setDef4(def4);
			String def3 = contract.get("def3") == null ? null : contract.get("def3").getTextValue();
			contractVO.setDef3(def3);
			String def9 = contract.get("def9") == null ? null : contract.get("def9").getTextValue();
			contractVO.setDef9(def9);
			String def5 = contract.get("def5") == null ? null : contract.get("def5").getTextValue();
			contractVO.setDef5(def5);
			String def6 = contract.get("def6") == null ? null : contract.get("def6").getTextValue();
			contractVO.setDef6(def6);
			String def7 = contract.get("def7") == null ? null : contract.get("def7").getTextValue();
			contractVO.setDef7(def7);
			String def8 = contract.get("def8") == null ? null : contract.get("def8").getTextValue();
			contractVO.setDef8(def8);
			UFDouble def11 = contract.get("def11") == null ? null : new UFDouble(contract.get("def11").getTextValue());
			contractVO.setDef11(def11);
			UFDouble def12 = contract.get("def12") == null ? null :  new UFDouble(contract.get("def12").getTextValue());
			contractVO.setDef12(def12);
			

			hYContractVO.setParentVO(contractVO);
			boolean flag = false;
			JsonNode contract_bs = contract.get("contract_b");
			if(contract_bs != null && contract_bs.size() > 0){
				List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
				for(JsonNode contract_b : contract_bs){
					ContractBVO contractBVO = new ContractBVO();
					
					String valuation_type = contract_b.get("valuation_type") == null ? null : contract_b.get("valuation_type").getTextValue();
					if(StringUtils.isNotBlank(valuation_type)){
						if(valuation_type.equals("重量")){
							contractBVO.setValuation_type(0);
						}else if(valuation_type.equals("体积")){
							contractBVO.setValuation_type(1);
						}else if(valuation_type.equals("件数")){
							contractBVO.setValuation_type(2);
						}else if(valuation_type.equals("设备")){
							contractBVO.setValuation_type(3);
						}else if(valuation_type.equals("吨公里")){
							contractBVO.setValuation_type(4);
						}else if(valuation_type.equals("票")){
							contractBVO.setValuation_type(5);
						}else if(valuation_type.equals("节点")){
							contractBVO.setValuation_type(6);
						}else if(valuation_type.equals("数量")){
							contractBVO.setValuation_type(7);
						}else if(valuation_type.equals("车型吨位")){
							contractBVO.setValuation_type(8);
						}else if(valuation_type.equals("提货点")){
							contractBVO.setValuation_type(9);
						}else if(valuation_type.equals("车型吨位+重量")){
							contractBVO.setValuation_type(10);
						}
						if(contractBVO.getValuation_type() == null){
							failed.add(contract_b + ":valuation_type:"+valuation_type+"不正确！");
							flag = true;
							break;
						}
					}
					String equip_type = contract_b.get("equip_type") == null ? null : contract_b.get("equip_type").getTextValue();
					if(StringUtils.isNotBlank(equip_type)){
						String equiptypeSql = "SELECT pk_car_type FROM ts_car_type WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code = ?";
						String equipType =  NWDao.getInstance().queryForObject(equiptypeSql, String.class, equip_type);
						if(StringUtils.isBlank(equipType)){
							failed.add(code + ":equip_type:"+equip_type+"不正确！");
							flag = true;
							break;
						}else{
							contractBVO.setEquip_type(equipType);
						}
					}
					String quote_type = contract_b.get("quote_type") == null ? null : contract_b.get("quote_type").getTextValue();
					if(StringUtils.isNotBlank(quote_type)){
						if(quote_type.equals("区间报价")){
							contractBVO.setQuote_type(0);
						}else if(quote_type.equals("首重报价")){
							contractBVO.setQuote_type(1);
						}
						if(contractBVO.getQuote_type() == null){
							failed.add(contract_b + ":quote_type:"+quote_type+"不正确！");
							flag = true;
							break;
						}
					}
					
					String pk_expense_type = contract_b.get("pk_expense_type") == null ? null : contract_b.get("pk_expense_type").getTextValue();
					if(StringUtils.isNotBlank(pk_expense_type)){
						String expensetypeSql = "SELECT pk_expense_type FROM ts_expense_type WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code = ?";
						String expenseType =  NWDao.getInstance().queryForObject(expensetypeSql, String.class, pk_expense_type);
						if(StringUtils.isBlank(expenseType)){
							failed.add(code + ":pk_expense_type:"+pk_expense_type+"不正确！");
							flag = true;
							break;
						}else{
							contractBVO.setPk_expense_type(expenseType);
						}
					}
					UFDouble lowest_fee = contract_b.get("def10") == null ? null : new UFDouble(contract_b.get("def10").getTextValue());
					contractBVO.setLowest_fee(lowest_fee);
					String price_type = contract_b.get("price_type") == null ? null : contract_b.get("price_type").getTextValue();
					if(StringUtils.isNotBlank(price_type)){
						if(price_type.equals("单价")){
							contractBVO.setPrice_type(0);
						}else if(price_type.equals("固定价格")){
							contractBVO.setPrice_type(1);
						}
						if(contractBVO.getPrice_type() == null){
							failed.add(contract_b + ":price_type:"+price_type+"不正确！");
							flag = true;
							break;
						}
					}
					UFDouble first_weight_price = contract_b.get("first_weight_price") == null ? null : new UFDouble(contract_b.get("first_weight_price").getTextValue());
					contractBVO.setFirst_weight_price(first_weight_price);
					UFDouble first_weight = contract_b.get("first_weight") == null ? null : new UFDouble(contract_b.get("first_weight").getTextValue());
					contractBVO.setFirst_weight(first_weight);
					UFBoolean if_return = contract_b.get("if_return") == null ? null : new UFBoolean(contract_b.get("if_return").getTextValue());
					contractBVO.setIf_return(if_return);
					String urgent_level = contract_b.get("urgent_level") == null ? null : contract_b.get("urgent_level").getTextValue();
					if(StringUtils.isNotBlank(urgent_level)){
						Object realValue = this.getValueByDataDict("urgent_level", urgent_level);
						if(realValue == null){
							failed.add(contract_b + ":urgent_level:"+urgent_level+"不正确！");
							flag = true;
							break;
						}else{
							Integer urgentLevel = (Integer) realValue;
							contractBVO.setUrgent_level(urgentLevel);
						}
					}
					String item_code = contract_b.get("item_code") == null ? null : contract_b.get("item_code").getTextValue();
					contractBVO.setItem_code(item_code);
					String pk_trans_line = contract_b.get("pk_trans_line") == null ? null : contract_b.get("pk_trans_line").getTextValue();
					contractBVO.setPk_trans_line(pk_trans_line);
					Integer start_addr_type = contract_b.get("start_addr_type") == null ? null : contract_b.get("start_addr_type").getValueAsInt();
					if(start_addr_type != null ){
						if(start_addr_type == 0){//城市
							String start_addr = contract_b.get("start_addr") == null ? null : contract_b.get("start_addr").getTextValue();
							String addrSql = "SELECT pk_area FROM ts_area "
									+ " WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N') = 'N' AND name = ?";
							String pk_area =  NWDao.getInstance().queryForObject(addrSql, String.class, start_addr);
							if(StringUtils.isBlank(pk_area)){
								failed.add(code + ":start_addr:"+start_addr+"不正确！");
								flag = true;
								break;
							}else{
								contractBVO.setStart_addr(pk_area);
							}
						}else if(start_addr_type == 1){//地址
							String start_addr = contract_b.get("start_addr") == null ? null : contract_b.get("start_addr").getTextValue();
							String addrSql = "SELECT pk_address FROM ts_address "
									+ " WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N') = 'N' AND addr_name = ?";
							String pk_address =  NWDao.getInstance().queryForObject(addrSql, String.class, start_addr);
							if(StringUtils.isBlank(pk_address)){
								failed.add(code + ":start_addr:"+start_addr+"不正确！");
								flag = true;
								break;
							}else{
								contractBVO.setStart_addr(pk_address);
							}
						}
						contractBVO.setStart_addr_type(start_addr_type);
						
					}
					
					Integer end_addr_type = contract_b.get("end_addr_type") == null ? null : contract_b.get("end_addr_type").getValueAsInt();
					if(end_addr_type != null ){
						if(end_addr_type == 0){//城市
							String end_addr = contract_b.get("end_addr") == null ? null : contract_b.get("end_addr").getTextValue();
							String addrSql = "SELECT pk_area FROM ts_area "
									+ " WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N') = 'N' AND name = ?";
							String pk_area =  NWDao.getInstance().queryForObject(addrSql, String.class, end_addr);
							if(StringUtils.isBlank(pk_area)){
								failed.add(code + ":end_addr:"+end_addr+"不正确！");
								flag = true;
								break;
							}else{
								contractBVO.setEnd_addr(pk_area);
							}
						}else if(end_addr_type == 1){//地址
							String end_addr = contract_b.get("end_addr") == null ? null : contract_b.get("end_addr").getTextValue();
							String addrSql = "SELECT pk_address FROM ts_address "
									+ " WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N') = 'N' AND addr_name = ?";
							String pk_address =  NWDao.getInstance().queryForObject(addrSql, String.class, end_addr);
							if(StringUtils.isBlank(pk_address)){
								failed.add(code + ":end_addr:"+end_addr+"不正确！");
								flag = true;
								break;
							}else{
								contractBVO.setEnd_addr(pk_address);
							}
						}
						contractBVO.setEnd_addr_type(end_addr_type);
					}
					
					UFBoolean if_optional = contract.get("contractno") == null ? UFBoolean.FALSE :new UFBoolean(contract.get("contractno").getTextValue());
					contractBVO.setIf_optional(if_optional);

					
					UFDouble interval1 = contract_b.get("interval1") == null ? null : new UFDouble(contract_b.get("interval1").getTextValue());
					UFDouble price1 = contract_b.get("price1") == null ? null : new UFDouble(contract_b.get("price1").getTextValue());
					contractBVO.setInterval1(interval1);
					contractBVO.setPrice1(price1);
					UFDouble interval2 = contract_b.get("interval2") == null ? null : new UFDouble(contract_b.get("interval2").getTextValue());
					UFDouble price2 = contract_b.get("price2") == null ? null : new UFDouble(contract_b.get("price2").getTextValue());
					contractBVO.setInterval2(interval2);
					contractBVO.setPrice2(price2);
					UFDouble interval3 = contract_b.get("interval3") == null ? null : new UFDouble(contract_b.get("interval3").getTextValue());
					UFDouble price3 = contract_b.get("price3") == null ? null : new UFDouble(contract_b.get("price3").getTextValue());
					contractBVO.setInterval3(interval3);
					contractBVO.setPrice3(price3);
					UFDouble interval4 = contract_b.get("interval4") == null ? null : new UFDouble(contract_b.get("interval4").getTextValue());
					UFDouble price4 = contract_b.get("price4") == null ? null : new UFDouble(contract_b.get("price4").getTextValue());
					contractBVO.setInterval4(interval4);
					contractBVO.setPrice4(price4);
					UFDouble interval5 = contract_b.get("interval5") == null ? null : new UFDouble(contract_b.get("interval5").getTextValue());
					UFDouble price5 = contract_b.get("price5") == null ? null : new UFDouble(contract_b.get("price5").getTextValue());
					contractBVO.setInterval5(interval5);
					contractBVO.setPrice5(price5);
					UFDouble interval6 = contract_b.get("interval6") == null ? null : new UFDouble(contract_b.get("interval6").getTextValue());
					UFDouble price6 = contract_b.get("price6") == null ? null : new UFDouble(contract_b.get("price6").getTextValue());
					contractBVO.setInterval6(interval6);
					contractBVO.setPrice6(price6);
					UFDouble interval7 = contract_b.get("interval7") == null ? null : new UFDouble(contract_b.get("interval7").getTextValue());
					UFDouble price7 = contract_b.get("price7") == null ? null : new UFDouble(contract_b.get("price7").getTextValue());
					contractBVO.setInterval7(interval7);
					contractBVO.setPrice7(price7);
					UFDouble interval8 = contract_b.get("interval8") == null ? null : new UFDouble(contract_b.get("interval8").getTextValue());
					UFDouble price8 = contract_b.get("price8") == null ? null : new UFDouble(contract_b.get("price8").getTextValue());
					contractBVO.setInterval8(interval8);
					contractBVO.setPrice8(price8);
					UFDouble interval9 = contract_b.get("interval9") == null ? null : new UFDouble(contract_b.get("interval9").getTextValue());
					UFDouble price9 = contract_b.get("price9") == null ? null : new UFDouble(contract_b.get("price9").getTextValue());
					contractBVO.setInterval9(interval9);
					contractBVO.setPrice9(price9);
					UFDouble interval10 = contract_b.get("interval10") == null ? null : new UFDouble(contract_b.get("interval10").getTextValue());
					UFDouble price10 = contract_b.get("price10") == null ? null : new UFDouble(contract_b.get("price10").getTextValue());
					contractBVO.setInterval10(interval10);
					contractBVO.setPrice10(price10);
					UFDouble interval11 = contract_b.get("interval11") == null ? null : new UFDouble(contract_b.get("interval11").getTextValue());
					UFDouble price11 = contract_b.get("price11") == null ? null : new UFDouble(contract_b.get("price11").getTextValue());
					contractBVO.setInterval11(interval11);
					contractBVO.setPrice11(price11);
					UFDouble interval12 = contract_b.get("interval12") == null ? null : new UFDouble(contract_b.get("interval12").getTextValue());
					UFDouble price12 = contract_b.get("price12") == null ? null : new UFDouble(contract_b.get("price12").getTextValue());
					contractBVO.setInterval12(interval12);
					contractBVO.setPrice12(price12);
					String b_def10 = contract_b.get("def10") == null ? null : contract_b.get("def10").getTextValue();
					contractBVO.setDef10(b_def10);
					String b_def2 = contract_b.get("def2") == null ? null : contract_b.get("def2").getTextValue();
					contractBVO.setDef2(b_def2);
					String b_def1 = contract_b.get("def1") == null ? null : contract_b.get("def1").getTextValue();
					contractBVO.setDef1(b_def1);
					String b_def4 = contract_b.get("def4") == null ? null : contract_b.get("def4").getTextValue();
					contractBVO.setDef4(b_def4);
					String b_def3 = contract_b.get("def3") == null ? null : contract_b.get("def3").getTextValue();
					contractBVO.setDef3(b_def3);
					String b_def9 = contract_b.get("def9") == null ? null : contract_b.get("def9").getTextValue();
					contractBVO.setDef9(b_def9);
					String b_def5 = contract_b.get("def5") == null ? null : contract_b.get("def5").getTextValue();
					contractBVO.setDef5(b_def5);
					String b_def6 = contract_b.get("def6") == null ? null : contract_b.get("def6").getTextValue();
					contractBVO.setDef6(b_def6);
					String b_def7 = contract_b.get("def7") == null ? null : contract_b.get("def7").getTextValue();
					contractBVO.setDef7(b_def7);
					String b_def8 = contract_b.get("def8") == null ? null : contract_b.get("def8").getTextValue();
					contractBVO.setDef8(b_def8);
					UFDouble b_def11 = contract_b.get("def11") == null ? null : new UFDouble(contract_b.get("def11").getTextValue());
					contractBVO.setDef11(b_def11);
					UFDouble b_def12 = contract_b.get("def12") == null ? null :  new UFDouble(contract_b.get("def12").getTextValue());
					contractBVO.setDef12(b_def12);
					
					
					contractBVO.setStatus(VOStatus.NEW);
					contractBVOs.add(contractBVO);
					
				}
				hYContractVO.setChildrenVO(contractBVOs.toArray(new ContractBVO[contractBVOs.size()]));
			}
			if (flag) {
				continue;
			}
			if(contract_type == 0){
				paramVO.setFunCode("t409");
				paramVO.setTemplateID("1455100297DC0001SfI8");
			}else if(contract_type == 1){
				paramVO.setFunCode("t410");
				paramVO.setTemplateID("1455587067DC0001uqu9");
			}
			
			contractVO.setCreate_time(new UFDateTime(new Date()));
			contractVO.setCreate_user(ediUserVO.getPk_user());
			contractVO.setPk_corp(ediUserVO.getPk_corp());
			contractVO.setStatus(VOStatus.NEW);
			if(oldContractVO != null){
				paramVO.setBillId(oldContractVO.getPrimaryKey());
				try {
					//service.batchDelete(ContractVO.class, new String[]{oldContractVO.getPrimaryKey()});
					NWDao.getInstance().delete(oldContractVO);
					contractVO.setCreate_time(oldContractVO.getCreate_time());
					contractVO.setCreate_user(oldContractVO.getCreate_user());
					contractVO.setPk_corp(oldContractVO.getPk_corp());
				} catch (Exception e) {
					 failed.add(e.getMessage());
				}
			}
			service.save(hYContractVO, paramVO);
			if(oldContractVO != null){
				success.add("更新：" + contractVO.getCode() +","+contractVO.getName());
			}else{
				success.add("新建：" + contractVO.getCode() +","+contractVO.getName());
			}
			
		}
		result.put("failed", failed);
		result.put("success", success);
		return JacksonUtils.writeValueAsString(result);
		
	}

	//导入前的检查
	private String checkBeforeImport(String uid,String pwd) throws ApiException {
		AuthenticationService authenticationService = SpringContextHolder.getBean("authenticationService");
		if (authenticationService == null) {
			throw new BusiException("验证服务没有启动，服务ID:AuthenticationService");
		}
		String authError = authenticationService.auth(uid, pwd);
		
		if(!authError.isEmpty()){
			return authError;
		}
		
		service = (ContractService) SpringContextHolder.getApplicationContext().getBean("contractServiceImpl");
		return "";
	}
	
	private Object getValueByDataDict(String dictName, String value){
		try {
			return Integer.parseInt(value);//如果可以直接转换成Int，说明这本身就是value
		} catch (Exception e) {
			String sql = "SELECT nw_data_dict_b.value AS value FROM nw_data_dict_b LEFT JOIN nw_data_dict ON "
					+ " nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ " WHERE nw_data_dict.datatype_code =? and display_name = ?";
			return NWDao.getInstance().queryForObject(sql, Object.class,dictName, value);
		}
	}
	
}
