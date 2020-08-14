package com.tms.services.tmsStandardApi;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.inv.InvoiceService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.GoodsPackcorpVO;
import com.tms.vo.base.GoodsVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvReqBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;

@SuppressWarnings("deprecation")
public class ImportOrdersEndpoint extends ServletEndpointSupport{
	
	private static String  EDI_USER_CODE = "EdiUser";
	private final static String TRUE = "TRUE";
	private final static String FALSE = "FALSE";
	
	private static	String ediUserSql = "SELECT * FROM nw_user WHERE isnull(dr,0) = 0 AND isnull(locked_flag,'N')='N' AND user_code = ?";
	private static	UserVO ediUserVO = NWDao.getInstance().queryForObject(ediUserSql, UserVO.class, EDI_USER_CODE);
	
	
	private InvoiceService service;
	private ParamVO paramVO;
	
	public String importOrders (String uorderno,String pwd,String jsonOrders) throws ApiException{
		//HttpRequestUtils.httpPost("http://127.0.0.1:8081/tms/services/TmsStandardBasicDatasServices", JSONObject.fromObject("{ 'orderNo': 'DD00012','vehicleorderno': '7bf0026f-36e0-4c37-816b-7444ee77837c','driverorderno': '1aaf2c4a-1d6c-454a-b8ec-0b22041d510a','forwarderorderno': 'c6781db8-7f74-49e5-a61c-4cd0e89e7501','addressorderno': '90126c35-f53e-460b-8c82-5b7b64fb9175','regionorderno': '0bf88757-ae5c-4700-8c05-2deb8e1abc30','count': 7,'entrustOrderNo': 'sample string 8'}"));
		Map<String,List<String>> result = new HashMap<String, List<String>>();
		List<String> success = new ArrayList<String>();
		List<String> failed = new ArrayList<String>();
		
		String authError = checkBeforeImport(uorderno,pwd);
		if(!authError.isEmpty()){
			failed.add(authError);
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
			 
		}
		
		//空值判断
		if(jsonOrders.isEmpty()){
			//return "未提供任何数据，请确认！";
			failed.add("未提供任何数据，请确认！");
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
			 
		}
		JsonNode orders = JacksonUtils.readTree(jsonOrders);
		//判断参数有效性
		if(orders  == null){
			failed.add("输入的参数不是要求的格式，请修改！");
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
		}
		JsonNode newOrders = orders;
		try {
			Map<String,Object> convertResult = FieldConvertUtils.convertByXML(orders, "orderConvert.xml");
			Object error = convertResult.get(FALSE);
			if(error != null && StringUtils.isNotBlank(error.toString())){
				failed.add(error.toString());
				result.put("failed", failed);
				return JacksonUtils.writeValueAsString(result);
			}
			newOrders = (JsonNode)convertResult.get(TRUE);
		} catch (Exception e1) {
			failed.add(e1.getMessage());
			result.put("failed", failed);
			return JacksonUtils.writeValueAsString(result);
		}
		for(JsonNode order : newOrders ){
			
			ExAggInvoiceVO exAggInvoiceVO = new ExAggInvoiceVO();
			//判断主表信息
			String errorMsg = CheckOrderInfo(order);
			if(!errorMsg.isEmpty()){
			   failed.add(errorMsg);
			   continue;
			}
			
			String orderno = order.get("orderno").getTextValue();
			
			Integer dr = order.get("dr") == null ? 0 : order.get("dr").getValueAsInt();
			if(dr != null && dr!=1 && dr!=0 ){
				 failed.add("如果传入DR字段，DR字段只能为0或者1");
				 continue;
			}
			
			InvoiceVO oldInvoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "orderno =? ", orderno);
			//delete
			if(dr == 1){
				if(oldInvoiceVO == null){
					failed.add(orderno +"不存在，无法删除");
					 continue;
				}
				try {
					service.batchDelete(InvoiceVO.class, new String[]{oldInvoiceVO.getPrimaryKey()});
				} catch (Exception e) {
					failed.add(orderno +"无法删除:" +e.getMessage());
					continue;
				}
				success.add("删除："+orderno);
				continue;
			}
			
			String pk_customer = order.get("pk_customer").getTextValue();
			String custSql = "SELECT ts_customer.pk_customer,ts_cust_bala.pk_related_cust "
					+ " FROM ts_customer WITH(nolock) LEFT JOIN ts_cust_bala WITH(nolock) "
					+ " ON ts_customer.pk_customer = ts_cust_bala.pk_customer "
					+ " WHERE isnull(ts_customer.dr,0)=0 and isnull(ts_cust_bala.dr,0)=0 "
					+ "and isnull(ts_customer.locked_flag,'N')='N' and isnull(ts_cust_bala.locked_flag,'N')='N' and ts_customer.cust_code = ?";
			@SuppressWarnings("unchecked")
			HashMap<String, String> customerMap =  NWDao.getInstance().queryForObject(custSql, HashMap.class, pk_customer);
			if(customerMap == null || StringUtils.isBlank(customerMap.get("pk_customer"))){
				failed.add(orderno + ":pk_customer:"+order.get("pk_customer").getTextValue()+"不正确！");
				 continue;
			}
			pk_customer = customerMap.get("pk_customer");
			String bala_customer = customerMap.get("pk_related_cust");
			String pk_trans_type = order.get("pk_trans_type").getTextValue();
			String transtypeSql = "SELECT pk_trans_type FROM ts_trans_type WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code = ?";
			String transtype =  NWDao.getInstance().queryForObject(transtypeSql, String.class, pk_trans_type);
			if(StringUtils.isBlank(transtype)){
				failed.add(orderno + ":pk_trans_type:"+order.get("pk_trans_type").getTextValue()+"不正确！");
				continue;
			}
			pk_trans_type = transtype;
			UFDateTime req_deli_date = new UFDateTime(order.get("req_deli_date").getTextValue());
			UFDateTime req_arri_date = new UFDateTime(order.get("req_arri_date").getTextValue());
			String pk_delivery = order.get("pk_delivery").getTextValue();
			String pk_arrival = order.get("pk_arrival").getTextValue();
			
			String vbillno =order.get("vbillno") == null ? null : order.get("vbillno").getTextValue();
			String cust_orderno =order.get("cust_orderno") == null ? null : order.get("cust_orderno").getTextValue();
			String feature =order.get("feature") == null ? null : order.get("feature").getTextValue();
			if( order.get("feature") != null){
				Object realValue = this.getValueByDataDict("goods_feature", order.get("feature").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":feature:"+order.get("feature").getTextValue()+"不正确！");
					continue;
				}
				feature = (String) realValue;
			}
			
			String pk_psndoc = null ;
			if(order.get("pk_psndoc") != null){
				String psndocSql = "SELECT pk_psndoc FROM nw_psndoc WITH(nolock) WHERE isnull(dr,0)=0 and psnname = ?";
				String psndoc =  NWDao.getInstance().queryForObject(psndocSql, String.class, order.get("pk_psndoc").getTextValue());
				if(StringUtils.isBlank(psndoc)){
					 failed.add(orderno + ":pk_psndoc:"+order.get("pk_psndoc").getTextValue()+"不正确！");
					 continue;
				}
				pk_psndoc = psndoc;
			}
			UFDateTime req_deli_time =order.get("req_deli_time") == null ? null : new UFDateTime(order.get("req_deli_time").getTextValue());
			UFDateTime req_arri_time =order.get("req_arri_time") == null ? null : new UFDateTime(order.get("req_arri_time").getTextValue());
			UFDateTime order_time =order.get("order_time") == null ? null : new UFDateTime(order.get("order_time").getTextValue());
			String pk_dept = null ;
			if(order.get("pk_dept") != null){
				String deptSql = "SELECT pk_dept FROM nw_dept WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and dept_name = ?";
				String dept =  NWDao.getInstance().queryForObject(deptSql, String.class, order.get("pk_dept").getTextValue());
				if(StringUtils.isBlank(dept)){
					failed.add(orderno + ":pk_dept:"+order.get("pk_dept").getTextValue()+"不正确！");
					continue;
				}
				pk_dept = dept;
			}
			String contact =order.get("contact") == null ? null : order.get("contact").getTextValue();
			String pk_invoice_type = null;
			if( order.get("pk_invoice_type") != null){
				Object realValue = this.getValueByDataDict("invoice_type", order.get("pk_invoice_type").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":pk_invoice_type:"+order.get("pk_invoice_type").getTextValue()+"不正确！");
					continue;
				}
				pk_invoice_type = (String) realValue;
			}
			
			String pk_service_type = null;
			if( order.get("pk_service_type") != null){
				Object realValue = this.getValueByDataDict("service_type", order.get("pk_service_type").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":pk_service_type:"+order.get("pk_service_type").getTextValue()+"不正确！");
					continue;
				}
				pk_invoice_type = (String) realValue;
			}
			
			String trade_term =order.get("trade_term") == null ? null : order.get("trade_term").getTextValue();
			String pk_corp = order.get("pk_corp").getTextValue();
			String corpSql = "SELECT pk_corp FROM nw_corp WITH(nolock) WHERE isnull(dr,0)=0 and corp_code = ?";
			String corp =  NWDao.getInstance().queryForObject(corpSql, String.class, pk_corp);
			if(StringUtils.isBlank(corp)){
				failed.add(orderno + ":pk_corp:"+order.get("pk_corp").getTextValue()+"不正确！");
				continue;
			}
			pk_corp = corp;
			String memo =order.get("memo") == null ? null : order.get("memo").getTextValue();
			String deli_contact =order.get("deli_contact") == null ? null : order.get("deli_contact").getTextValue();
			String deli_mobile =order.get("deli_mobile") == null ? null : order.get("deli_mobile").getTextValue();
			String deli_phone =order.get("deli_phone") == null ? null : order.get("deli_phone").getTextValue();
			String deli_email =order.get("deli_email") == null ? null : order.get("deli_email").getTextValue();
			String arri_contact =order.get("arri_contact") == null ? null : order.get("arri_contact").getTextValue();
			String arri_mobile =order.get("arri_mobile") == null ? null : order.get("arri_mobile").getTextValue();
			String arri_phone =order.get("arri_phone") == null ? null : order.get("arri_phone").getTextValue();
			String arri_email =order.get("arri_email") == null ? null : order.get("arri_email").getTextValue();
			String insurance_no =order.get("insurance_no") == null ? null : order.get("insurance_no").getTextValue();
			String returnbill_no =order.get("returnbill_no") == null ? null : order.get("returnbill_no").getTextValue();
			String customs_official_no =order.get("customs_official_no") == null ? null : order.get("customs_official_no").getTextValue();
			String cost_center =order.get("cost_center") == null ? null : order.get("cost_center").getTextValue();
			String deli_process =order.get("deli_process") == null ? null : order.get("deli_process").getTextValue();
			String arri_process =order.get("arri_process") == null ? null : order.get("arri_process").getTextValue();
			String note =order.get("note") == null ? null : order.get("note").getTextValue();
			String pk_carrier = null;
			if(order.get("pk_carrier") != null){
				String carrSql = "SELECT pk_carrier FROM ts_carrier WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carr_code = ?";
				String carr =  NWDao.getInstance().queryForObject(carrSql, String.class, order.get("pk_carrier").getTextValue());
				if(StringUtils.isBlank(carr)){
					failed.add(orderno + ":pk_carrier:"+order.get("pk_carrier").getTextValue()+"不正确！");
					continue;
				}
				pk_carrier = carr;
			}
			String urgent_reson =order.get("urgent_reson") == null ? null : order.get("urgent_reson").getTextValue();
			String cargo_flow =order.get("cargo_flow") == null ? null : order.get("cargo_flow").getTextValue();
			String supervision_type =order.get("supervision_type") == null ? null : order.get("supervision_type").getTextValue();
			String vehicle_req =order.get("vehicle_req") == null ? null : order.get("vehicle_req").getTextValue();
			String transport_req =order.get("transport_req") == null ? null : order.get("transport_req").getTextValue();
			String other_req =order.get("other_req") == null ? null : order.get("other_req").getTextValue();
			String currency =order.get("currency") == null ? null : order.get("currency").getTextValue();
			String def1 =order.get("def1") == null ? null : order.get("def1").getTextValue();
			String def2 =order.get("def2") == null ? null : order.get("def2").getTextValue();
			String def3 =order.get("def3") == null ? null : order.get("def3").getTextValue();
			String def4 =order.get("def4") == null ? null : order.get("def4").getTextValue();
			String def5 =order.get("def5") == null ? null : order.get("def5").getTextValue();
			String def6 =order.get("def6") == null ? null : order.get("def6").getTextValue();
			String def7 =order.get("def7") == null ? null : order.get("def7").getTextValue();
			String def8 =order.get("def8") == null ? null : order.get("def8").getTextValue();
			String def9 =order.get("def9") == null ? null : order.get("def9").getTextValue();
			String def10 =order.get("def10") == null ? null : order.get("def10").getTextValue();
			UFDateTime con_arri_date =order.get("con_arri_date") == null ? null : new UFDateTime(order.get("con_arri_date").getTextValue());
			String item_name =order.get("item_name") == null ? null : order.get("item_name").getTextValue();
			String item_code =order.get("item_code") == null ? null : order.get("item_code").getTextValue();
			String pk_supplier = null;
			if(order.get("pk_supplier") != null){
				String suppSql = "SELECT pk_supplier FROM ts_supplier WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and supp_code = ?";
				String supp =  NWDao.getInstance().queryForObject(suppSql, String.class, order.get("pk_supplier").getTextValue());
				if(StringUtils.isBlank(supp)){
					failed.add(orderno + ":pk_supplier:"+order.get("pk_supplier").getTextValue()+"不正确！");
					continue;
				}
				pk_supplier = supp;
			}
			
			String pk_trans_line =order.get("pk_trans_line") == null ? null : order.get("pk_trans_line").getTextValue();
			if(order.get("pk_trans_line") != null){
				String lineSql = "SELECT pk_trans_line FROM ts_trans_line WITH(nolock) WHERE isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and line_code = ?";
				String line =  NWDao.getInstance().queryForObject(lineSql, String.class, order.get("pk_trans_line").getTextValue());
				if(StringUtils.isBlank(line)){
					failed.add(orderno + ":pk_trans_line:"+order.get("pk_trans_line").getTextValue()+"不正确！");
					continue;
				}
				pk_trans_line = line;
			}
			
			Integer urgent_level = order.get("urgent_level") == null ? null : order.get("urgent_level").getValueAsInt();
			if( order.get("urgent_level") != null){
				Object realValue = this.getValueByDataDict("urgent_level", order.get("urgent_level").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":urgent_level:"+order.get("urgent_level").getTextValue()+"不正确！");
					continue;
				}
				urgent_level = Integer.parseInt(realValue.toString());
			}
			
			Integer mileage = null ;
			if( order.get("mileage") != null){
				Object realValue = this.getValueByDataDict("mileage", order.get("mileage").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":mileage:"+order.get("mileage").getTextValue()+"不正确！");
					continue;
				}
				mileage = Integer.parseInt(realValue.toString());
			}
			Integer num_count = order.get("num_count") == null ? null : order.get("num_count").getValueAsInt();
			
			Integer balatype = null ;
			if( order.get("balatype") != null){
				Object realValue = this.getValueByDataDict("balatype", order.get("balatype").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":balatype:"+order.get("balatype").getTextValue()+"不正确！");
					continue;
				}
				balatype = Integer.parseInt(realValue.toString());
			}
			Integer deli_method =  null;
			if( order.get("deli_method") != null){
				Object realValue = this.getValueByDataDict("delivery_method", order.get("deli_method").getTextValue());
				if(realValue == null){
					failed.add(orderno + ":deli_method:"+order.get("deli_method").getTextValue()+"不正确！");
					continue;
				}
				deli_method = Integer.parseInt(realValue.toString());
			}
			Integer backbill_num = order.get("backbill_num") == null ? null : order.get("backbill_num").getValueAsInt();
			String invoice_goods_type =  order.get("invoice_goods_type") == null ? null : order.get("invoice_goods_type").getTextValue() ;
			
			UFDouble distance = order.get("distance") == null ? null : new UFDouble(order.get("distance").getValueAsDouble());
			UFDouble receipt_amount = order.get("receipt_amount") == null ? null : new UFDouble(order.get("receipt_amount").getValueAsDouble());
			UFDouble insurance_amount = order.get("insurance_amount") == null ? null : new UFDouble(order.get("insurance_amount").getValueAsDouble());
			UFDouble amount = order.get("amount") == null ? null : new UFDouble(order.get("amount").getValueAsDouble());
			UFDouble weight_count = order.get("weight_count") == null ? null : new UFDouble(order.get("weight_count").getValueAsDouble());
			UFDouble volume_count = order.get("volume_count") == null ? null : new UFDouble(order.get("volume_count").getValueAsDouble());
			UFDouble fee_weight_count = order.get("fee_weight_count") == null ? null : new UFDouble(order.get("fee_weight_count").getValueAsDouble());
			UFDouble cost_amount = order.get("cost_amount") == null ? null : new UFDouble(order.get("cost_amount").getValueAsDouble());
			UFDouble volume_weight_count = order.get("volume_weight_count") == null ? null : new UFDouble(order.get("volume_weight_count").getValueAsDouble());
			UFDouble arri_pay_amount = order.get("arri_pay_amount") == null ? null : new UFDouble(order.get("arri_pay_amount").getValueAsDouble());
			UFDouble def11 =order.get("def11") == null ? null : new UFDouble(order.get("def11").getValueAsDouble());
			UFDouble def12 =order.get("def12") == null ? null : new UFDouble(order.get("def12").getValueAsDouble());
			UFDouble pack_num_count = order.get("pack_num_count") == null ? null : new UFDouble(order.get("pack_num_count").getValueAsDouble());
			
			UFBoolean if_backbill = order.get("if_backbill") == null ? null : new UFBoolean(order.get("if_backbill").getValueAsText());
			UFBoolean if_ins_receipt = order.get("if_ins_receipt") == null ? null : new UFBoolean(order.get("if_ins_receipt").getValueAsText());
			UFBoolean if_insurance = order.get("if_insurance") == null ? null : new UFBoolean(order.get("if_insurance").getValueAsText());
			UFBoolean if_billing = order.get("if_billing") == null ? null : new UFBoolean(order.get("if_billing").getValueAsText());
			UFBoolean if_return = order.get("if_return") == null ? null : new UFBoolean(order.get("if_return").getValueAsText());
			UFBoolean if_customs_official = order.get("if_customs_official") == null ? null : new UFBoolean(order.get("if_customs_official").getValueAsText());
			
			InvoiceVO invoiceVO = new InvoiceVO();
			
			invoiceVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(invoiceVO);
			invoiceVO.setVbillno(vbillno == null ? BillnoHelper.generateBillno(BillTypeConst.FHD) : vbillno);
			invoiceVO.setPk_customer(pk_customer);
			invoiceVO.setBala_customer(bala_customer);
			invoiceVO.setPk_trans_type(pk_trans_type);
			invoiceVO.setReq_deli_date(req_deli_date == null ? null : req_deli_date.toString());
			invoiceVO.setReq_arri_date(req_arri_date == null ? null : req_arri_date.toString());
			invoiceVO.setReq_deli_time(req_deli_time == null ? null : req_deli_time.toString());
			invoiceVO.setReq_arri_time(req_arri_time == null ? null : req_arri_time.toString());
			invoiceVO.setCust_orderno(cust_orderno);
			invoiceVO.setOrderno(orderno);
			invoiceVO.setVbillstatus(BillStatus.NEW);
			invoiceVO.setUrgent_level(urgent_level);
			invoiceVO.setFeature(feature);
			invoiceVO.setOrder_time(order_time);
			invoiceVO.setPk_psndoc(pk_psndoc);
			invoiceVO.setPk_dept(pk_dept);
			invoiceVO.setContact(contact);
			invoiceVO.setMileage(mileage);
			invoiceVO.setPk_invoice_type(pk_invoice_type);
			invoiceVO.setPk_service_type(pk_service_type);
			invoiceVO.setTrade_term(trade_term);
			invoiceVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.EDI.intValue());
			invoiceVO.setCreate_time(new UFDateTime(new Date()));
			invoiceVO.setCreate_user(ediUserVO.getPk_user());
			invoiceVO.setPk_corp(pk_corp);
			invoiceVO.setMemo(memo);
			invoiceVO.setPk_delivery(pk_delivery);
			invoiceVO.setDeli_contact(deli_contact);
			invoiceVO.setDeli_mobile(deli_mobile);
			invoiceVO.setDeli_phone(deli_phone);
			invoiceVO.setDeli_email(deli_email);
			invoiceVO.setPk_arrival(pk_arrival);
			invoiceVO.setArri_contact(arri_contact);
			invoiceVO.setArri_mobile(arri_mobile);
			invoiceVO.setArri_phone(arri_phone);
			invoiceVO.setArri_email(arri_email);
			invoiceVO.setDistance(distance);
			invoiceVO.setIf_backbill(if_backbill);
			invoiceVO.setIf_ins_receipt(if_ins_receipt);
			invoiceVO.setReceipt_amount(receipt_amount);
			invoiceVO.setIf_insurance(if_insurance);
			invoiceVO.setInsurance_amount(insurance_amount);
			invoiceVO.setAmount(amount);
			invoiceVO.setInsurance_no(insurance_no);
			invoiceVO.setIf_billing(if_billing);
			invoiceVO.setIf_return(if_return);
			invoiceVO.setReturnbill_no(returnbill_no);
			invoiceVO.setIf_customs_official(if_customs_official);
			invoiceVO.setCustoms_official_no(customs_official_no);
			invoiceVO.setCost_center(cost_center);
			invoiceVO.setDeli_process(deli_process);
			invoiceVO.setArri_process(arri_process);
			invoiceVO.setNote(note);
			invoiceVO.setNum_count(num_count);
			invoiceVO.setWeight_count(weight_count);
			invoiceVO.setVolume_count(volume_count);
			invoiceVO.setFee_weight_count(fee_weight_count);
			invoiceVO.setCost_amount(cost_amount);
			invoiceVO.setPk_carrier(pk_carrier);
			invoiceVO.setBalatype(balatype);
			invoiceVO.setVolume_weight_count(volume_weight_count);
			invoiceVO.setDeli_method(deli_method);
			invoiceVO.setArri_pay_amount(arri_pay_amount);
			invoiceVO.setDef1(def1);
			invoiceVO.setDef2(def2);
			invoiceVO.setDef3(def3);
			invoiceVO.setDef4(def4);
			invoiceVO.setDef5(def5);
			invoiceVO.setDef6(def6);
			invoiceVO.setDef7(def7);
			invoiceVO.setDef8(def8);
			invoiceVO.setDef9(def9);
			invoiceVO.setDef10(def10);
			invoiceVO.setDef11(def11);
			invoiceVO.setDef12(def12);
			invoiceVO.setBackbill_num(backbill_num);
			invoiceVO.setPack_num_count(pack_num_count);
			invoiceVO.setCon_arri_date(con_arri_date == null ? null : con_arri_date.toString());
			invoiceVO.setItem_name(item_name);
			invoiceVO.setItem_code(item_code);
			invoiceVO.setPk_supplier(pk_supplier);
			invoiceVO.setPk_trans_line(pk_trans_line);
			invoiceVO.setInvoice_goods_type(invoice_goods_type);
			invoiceVO.setUrgent_reson(urgent_reson);
			invoiceVO.setCargo_flow(cargo_flow);
			invoiceVO.setSupervision_type(supervision_type);
			invoiceVO.setVehicle_req(vehicle_req);
			invoiceVO.setTransport_req(transport_req);
			invoiceVO.setOther_req(other_req);
			invoiceVO.setCurrency(currency);
			
			exAggInvoiceVO.setParentVO(invoiceVO);
			boolean flag = false;
			JsonNode inv_pack_bs = order.get("inv_pack_b");
			if(inv_pack_bs != null && inv_pack_bs.size() > 0){
				int serialno = 0;
				List<InvPackBVO> invPackBVOs = new ArrayList<InvPackBVO>();
				for(JsonNode inv_pack_b : inv_pack_bs){
					
					String packBErrorMsg =  CheckInvPackB(inv_pack_b);
					if(!packBErrorMsg.isEmpty()){
						 failed.add(packBErrorMsg);
						 flag = true;
						 break;
					}
					serialno += 10;
					String goods_code =inv_pack_b.get("goods_code") == null ? null : inv_pack_b.get("goods_code").getTextValue();
					String goods_name =inv_pack_b.get("goods_name") == null ? null : inv_pack_b.get("goods_name").getTextValue();
					String pack = null;
					if(inv_pack_b.get("pack") != null){
						GoodsPackcorpVO goodsPackVO = NWDao.getInstance().queryByCondition(GoodsPackcorpVO.class, " code =? and pk_corp =?", inv_pack_b.get("pack").getTextValue(),invoiceVO.getPk_corp());
						if(goodsPackVO == null){
							 failed.add(orderno + ":pack:"+inv_pack_b.get("pack").getTextValue()+"不正确！");
							 flag = true;
							 break;
						}
						pack = goodsPackVO.getPk_goods_packcorp();
					}
					String trans_note =inv_pack_b.get("trans_note") == null ? null : inv_pack_b.get("trans_note").getTextValue();
					String reference_no =inv_pack_b.get("reference_no") == null ? null : inv_pack_b.get("reference_no").getTextValue();
					String packbMemo =inv_pack_b.get("memo") == null ? null : inv_pack_b.get("memo").getTextValue();
					String min_pack =inv_pack_b.get("min_pack") == null ? null : inv_pack_b.get("min_pack").getTextValue();
					String goods_type =inv_pack_b.get("goods_type") == null ? null : inv_pack_b.get("goods_type").getTextValue();
					String packbDef1 =inv_pack_b.get("def1") == null ? null : inv_pack_b.get("def1").getTextValue();
					String packbDef2 =inv_pack_b.get("def2") == null ? null : inv_pack_b.get("def2").getTextValue();
					String packbDef3 =inv_pack_b.get("def3") == null ? null : inv_pack_b.get("def3").getTextValue();
					String packbDef4 =inv_pack_b.get("def4") == null ? null : inv_pack_b.get("def4").getTextValue();
					String packbDef5 =inv_pack_b.get("def5") == null ? null : inv_pack_b.get("def5").getTextValue();
					String packbDef6 =inv_pack_b.get("def6") == null ? null : inv_pack_b.get("def6").getTextValue();
					String packbDef7 =inv_pack_b.get("def7") == null ? null : inv_pack_b.get("def7").getTextValue();
					String packbDef8 =inv_pack_b.get("def8") == null ? null : inv_pack_b.get("def8").getTextValue();
					String packbDef9 =inv_pack_b.get("def9") == null ? null : inv_pack_b.get("def9").getTextValue();
					String packbDef10 =inv_pack_b.get("def10") == null ? null : inv_pack_b.get("def10").getTextValue();
					
					Integer num = inv_pack_b.get("num") == null ? null : inv_pack_b.get("num").getValueAsInt();
					Integer pod_num = inv_pack_b.get("pod_num") == null ? null : inv_pack_b.get("pod_num").getValueAsInt();
					Integer reject_num = inv_pack_b.get("reject_num") == null ? null : inv_pack_b.get("reject_num").getValueAsInt();
					Integer damage_num = inv_pack_b.get("damage_num") == null ? null : inv_pack_b.get("damage_num").getValueAsInt();
					Integer lost_num = inv_pack_b.get("lost_num") == null ? null : inv_pack_b.get("lost_num").getValueAsInt();
					Integer plan_num = inv_pack_b.get("plan_num") == null ? null : inv_pack_b.get("plan_num").getValueAsInt();
					
					
					UFDouble weight = inv_pack_b.get("weight") == null ? null : new UFDouble(inv_pack_b.get("weight").getValueAsDouble());
					UFDouble volume = inv_pack_b.get("volume") == null ? null : new UFDouble(inv_pack_b.get("volume").getValueAsDouble());
					UFDouble unit_weight = inv_pack_b.get("unit_weight") == null ? null : new UFDouble(inv_pack_b.get("unit_weight").getValueAsDouble());
					UFDouble unit_volume = inv_pack_b.get("unit_volume") == null ? null : new UFDouble(inv_pack_b.get("unit_volume").getValueAsDouble());
					UFDouble length = inv_pack_b.get("length") == null ? null : new UFDouble(inv_pack_b.get("length").getValueAsDouble());
					UFDouble wordernoth = inv_pack_b.get("wordernoth") == null ? null : new UFDouble(inv_pack_b.get("wordernoth").getValueAsDouble());
					UFDouble height = inv_pack_b.get("height") == null ? null : new UFDouble(inv_pack_b.get("height").getValueAsDouble());
					UFDouble low_temp = inv_pack_b.get("low_temp") == null ? null : new UFDouble(inv_pack_b.get("low_temp").getValueAsDouble());
					UFDouble hight_temp = inv_pack_b.get("hight_temp") == null ? null : new UFDouble(inv_pack_b.get("hight_temp").getValueAsDouble());
					UFDouble packBPack_num_count = inv_pack_b.get("pack_num_count") == null ? null : new UFDouble(inv_pack_b.get("pack_num_count").getValueAsDouble());
					UFDouble plan_pack_num_count = inv_pack_b.get("plan_pack_num_count") == null ? null : new UFDouble(inv_pack_b.get("plan_pack_num_count").getValueAsDouble());
					UFDouble packbDef11 =inv_pack_b.get("def11") == null ? null : new UFDouble(inv_pack_b.get("def11").getValueAsDouble());
					UFDouble packbDef12 =inv_pack_b.get("def12") == null ? null : new UFDouble(inv_pack_b.get("def12").getValueAsDouble());
					
					InvPackBVO invPackBVO = new InvPackBVO();
					invPackBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invPackBVO);
					invPackBVO.setPk_invoice(invoiceVO.getPk_invoice());
					invPackBVO.setSerialno(serialno);
					invPackBVO.setGoods_code(goods_code);
					invPackBVO.setGoods_name(goods_name);
					invPackBVO.setNum(num);
					invPackBVO.setPack(pack);
					invPackBVO.setWeight(weight);
					invPackBVO.setVolume(volume);
					invPackBVO.setUnit_volume(unit_volume);
					invPackBVO.setUnit_weight(unit_weight);
					invPackBVO.setLength(length);
					invPackBVO.setWidth(wordernoth);
					invPackBVO.setHeight(height);
					invPackBVO.setTrans_note(trans_note);
					invPackBVO.setLow_temp(low_temp);
					invPackBVO.setHight_temp(hight_temp);
					invPackBVO.setReference_no(reference_no);
					invPackBVO.setMemo(packbMemo);
					invPackBVO.setPod_num(pod_num);
					invPackBVO.setReject_num(reject_num);
					invPackBVO.setDamage_num(damage_num);
					invPackBVO.setLost_num(lost_num);
					invPackBVO.setMin_pack(min_pack);
					invPackBVO.setPack_num_count(packBPack_num_count);
					invPackBVO.setGoods_type(goods_type);
					invPackBVO.setPlan_num(plan_num);
					invPackBVO.setPlan_pack_num_count(plan_pack_num_count);
					invPackBVO.setDef1(packbDef1);
					invPackBVO.setDef2(packbDef2);
					invPackBVO.setDef3(packbDef3);
					invPackBVO.setDef4(packbDef4);
					invPackBVO.setDef5(packbDef5);
					invPackBVO.setDef6(packbDef6);
					invPackBVO.setDef7(packbDef7);
					invPackBVO.setDef8(packbDef8);
					invPackBVO.setDef9(packbDef9);
					invPackBVO.setDef10(packbDef10);
					invPackBVO.setDef11(packbDef11);
					invPackBVO.setDef12(packbDef12);
					invPackBVOs.add(invPackBVO);
					
				}
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_INV_PACK_B, invPackBVOs.toArray(new InvPackBVO[invPackBVOs.size()]));
			}
			if (flag) {
				continue;
			}
			
			JsonNode inv_trans_bs = order.get("inv_trans_b");
			if(inv_trans_bs != null && inv_trans_bs.size() > 0){
				List<TransBilityBVO> invTransBVOs = new ArrayList<TransBilityBVO>();
				for(JsonNode inv_trans_b : inv_trans_bs){
					String transBErrorMsg = CheckInvTransB(inv_trans_b);
					if(!transBErrorMsg.isEmpty()){
						 failed.add(transBErrorMsg);
						 flag = true;
						 break;
					}
					String pk_car_type = inv_trans_b.get("pk_car_type").getTextValue();
					String carTypeSql = "SELECT pk_car_type FROM ts_car_type WITH(nolock) WHERE isnull(dr,0)=0 and code = ?";
					String car_type =  NWDao.getInstance().queryForObject(carTypeSql, String.class, pk_car_type);
					if(StringUtils.isBlank(car_type)){
						 failed.add(orderno + ":pk_car_type:"+inv_trans_b.get("pk_car_type").getTextValue()+"不正确！");
						 flag = true;
						 break;
					}
					pk_car_type = car_type;
					
					String transBmemo =inv_trans_b.get("memo") == null ? null : inv_trans_b.get("memo").getTextValue();
					String carno =inv_trans_b.get("carno") == null ? null : inv_trans_b.get("carno").getTextValue();
					String pk_driver =inv_trans_b.get("pk_driver") == null ? null : inv_trans_b.get("pk_driver").getTextValue();
					String transBDef1 =inv_trans_b.get("def1") == null ? null : inv_trans_b.get("def1").getTextValue();
					String transBDef2 =inv_trans_b.get("def2") == null ? null : inv_trans_b.get("def2").getTextValue();
					String transBDef3 =inv_trans_b.get("def3") == null ? null : inv_trans_b.get("def3").getTextValue();
					String transBDef4 =inv_trans_b.get("def4") == null ? null : inv_trans_b.get("def4").getTextValue();
					String transBDef5 =inv_trans_b.get("def5") == null ? null : inv_trans_b.get("def5").getTextValue();
					String transBDef6 =inv_trans_b.get("def6") == null ? null : inv_trans_b.get("def6").getTextValue();
					String transBDef7 =inv_trans_b.get("def7") == null ? null : inv_trans_b.get("def7").getTextValue();
					String transBDef8 =inv_trans_b.get("def8") == null ? null : inv_trans_b.get("def8").getTextValue();
					String transBDef9 =inv_trans_b.get("def9") == null ? null : inv_trans_b.get("def9").getTextValue();
					String transBDef10 =inv_trans_b.get("def10") == null ? null : inv_trans_b.get("def10").getTextValue();
					
					UFDouble price =inv_trans_b.get("price") == null ? null : new UFDouble(inv_trans_b.get("price").getValueAsDouble());
					UFDouble transBAmount =inv_trans_b.get("amount") == null ? null : new UFDouble(inv_trans_b.get("amount").getValueAsDouble());
					UFDouble transBDef11 =inv_trans_b.get("def11") == null ? null : new UFDouble(inv_trans_b.get("def11").getValueAsDouble());
					UFDouble transBDef12 =inv_trans_b.get("def12") == null ? null : new UFDouble(inv_trans_b.get("def12").getValueAsDouble());
					
					Integer num = inv_trans_b.get("num") == null ? null : inv_trans_b.get("num").getValueAsInt();
					
					TransBilityBVO invTransBVO = new TransBilityBVO();
					invTransBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invTransBVO);
					invTransBVO.setPk_invoice(invoiceVO.getPk_invoice());
					invTransBVO.setPk_car_type(pk_car_type);
					invTransBVO.setNum(num);
					invTransBVO.setMemo(transBmemo);
					invTransBVO.setPrice(price);
					invTransBVO.setAmount(transBAmount);
					invTransBVO.setCarno(carno);
					invTransBVO.setPk_driver(pk_driver);
					invTransBVO.setDef1(transBDef1);
					invTransBVO.setDef2(transBDef2);
					invTransBVO.setDef3(transBDef3);
					invTransBVO.setDef4(transBDef4);
					invTransBVO.setDef5(transBDef5);
					invTransBVO.setDef6(transBDef6);
					invTransBVO.setDef7(transBDef7);
					invTransBVO.setDef8(transBDef8);
					invTransBVO.setDef9(transBDef9);
					invTransBVO.setDef10(transBDef10);
					invTransBVO.setDef11(transBDef11);
					invTransBVO.setDef12(transBDef12);
					
					invTransBVOs.add(invTransBVO);
				}
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_TRANS_BILITY_B, invTransBVOs.toArray(new TransBilityBVO[invTransBVOs.size()]));
			}
			if (flag) {
				continue;
			}
			JsonNode inv_line_bs = order.get("inv_line_b");
			if(inv_line_bs != null && inv_line_bs.size() > 0){
				int serialno = 0;
				List<InvLineBVO> invLineBVOs = new ArrayList<InvLineBVO>();
				for(JsonNode inv_line_b : inv_line_bs){
					String lineBErrorMsg =CheckInvLineB(inv_line_b);
					if(!lineBErrorMsg.isEmpty()){
						 failed.add(lineBErrorMsg);
						 flag = true;
						 break;
					}
					serialno += 10;
					String pk_address =inv_line_b.get("pk_address") == null ? null : inv_line_b.get("pk_address").getTextValue();
					String LineBcontact =inv_line_b.get("contact") == null ? null : inv_line_b.get("contact").getTextValue();
					String phone =inv_line_b.get("phone") == null ? null : inv_line_b.get("phone").getTextValue();
					String mobile =inv_line_b.get("mobile") == null ? null : inv_line_b.get("mobile").getTextValue();
					String email =inv_line_b.get("email") == null ? null : inv_line_b.get("email").getTextValue();
					UFDateTime req_date_from = new UFDateTime(inv_line_b.get("req_date_from").getTextValue());
					UFDateTime req_date_till = new UFDateTime(inv_line_b.get("req_date_till").getTextValue());
					
					String goods_code =inv_line_b.get("goods_code") == null ? null : inv_line_b.get("goods_code").getTextValue();
					String goods_name =inv_line_b.get("goods_name") == null ? null : inv_line_b.get("goods_name").getTextValue();
					String pack = null ;
					if(inv_line_b.get("pack") != null){
						GoodsPackcorpVO goodsPackVO = NWDao.getInstance().queryByCondition(GoodsVO.class, " code =? and pk_corp =?", inv_line_b.get("pack").getTextValue(),invoiceVO.getPk_corp());
						if(goodsPackVO == null){
							 failed.add(orderno + ":pack:"+inv_line_b.get("pack").getTextValue()+"不正确！");
							 flag = true;
							 break;
						}
						pack = goodsPackVO.getPk_goods_packcorp();
					}
					String trans_note =inv_line_b.get("trans_note") == null ? null : inv_line_b.get("trans_note").getTextValue();
					String reference_no =inv_line_b.get("reference_no") == null ? null : inv_line_b.get("reference_no").getTextValue();
					String LineBMemo =inv_line_b.get("memo") == null ? null : inv_line_b.get("memo").getTextValue();
					String min_pack =inv_line_b.get("min_pack") == null ? null : inv_line_b.get("min_pack").getTextValue();
					String goods_type =inv_line_b.get("goods_type") == null ? null : inv_line_b.get("goods_type").getTextValue();
					String LineBDef1 =inv_line_b.get("def1") == null ? null : inv_line_b.get("def1").getTextValue();
					String LineBDef2 =inv_line_b.get("def2") == null ? null : inv_line_b.get("def2").getTextValue();
					String LineBDef3 =inv_line_b.get("def3") == null ? null : inv_line_b.get("def3").getTextValue();
					String LineBDef4 =inv_line_b.get("def4") == null ? null : inv_line_b.get("def4").getTextValue();
					String LineBDef5 =inv_line_b.get("def5") == null ? null : inv_line_b.get("def5").getTextValue();
					String LineBDef6 =inv_line_b.get("def6") == null ? null : inv_line_b.get("def6").getTextValue();
					String LineBDef7 =inv_line_b.get("def7") == null ? null : inv_line_b.get("def7").getTextValue();
					String LineBDef8 =inv_line_b.get("def8") == null ? null : inv_line_b.get("def8").getTextValue();
					String LineBDef9 =inv_line_b.get("def9") == null ? null : inv_line_b.get("def9").getTextValue();
					String LineBDef10 =inv_line_b.get("def10") == null ? null : inv_line_b.get("def10").getTextValue();
					
					
					Object realValue = this.getValueByDataDict("operate_type", inv_line_b.get("operate_type").getTextValue());
					if(realValue == null){
						failed.add(orderno + ":operate_type:"+inv_line_b.get("operate_type").getTextValue()+"不正确！");
						continue;
					}
					Integer operate_type = Integer.parseInt(realValue.toString());
					
					
					Integer num = inv_line_b.get("num") == null ? null : inv_line_b.get("num").getValueAsInt();
					Integer pod_num = inv_line_b.get("pod_num") == null ? null : inv_line_b.get("pod_num").getValueAsInt();
					Integer reject_num = inv_line_b.get("reject_num") == null ? null : inv_line_b.get("reject_num").getValueAsInt();
					Integer damage_num = inv_line_b.get("damage_num") == null ? null : inv_line_b.get("damage_num").getValueAsInt();
					Integer lost_num = inv_line_b.get("lost_num") == null ? null : inv_line_b.get("lost_num").getValueAsInt();
					Integer plan_num = inv_line_b.get("plan_num") == null ? null : inv_line_b.get("plan_num").getValueAsInt();
					
					
					UFDouble weight = inv_line_b.get("weight") == null ? null : new UFDouble(inv_line_b.get("weight").getValueAsDouble());
					UFDouble volume = inv_line_b.get("volume") == null ? null : new UFDouble(inv_line_b.get("volume").getValueAsDouble());
					UFDouble unit_weight = inv_line_b.get("unit_weight") == null ? null : new UFDouble(inv_line_b.get("unit_weight").getValueAsDouble());
					UFDouble unit_volume = inv_line_b.get("unit_volume") == null ? null : new UFDouble(inv_line_b.get("unit_volume").getValueAsDouble());
					UFDouble length = inv_line_b.get("length") == null ? null : new UFDouble(inv_line_b.get("length").getValueAsDouble());
					UFDouble wordernoth = inv_line_b.get("wordernoth") == null ? null : new UFDouble(inv_line_b.get("wordernoth").getValueAsDouble());
					UFDouble height = inv_line_b.get("height") == null ? null : new UFDouble(inv_line_b.get("height").getValueAsDouble());
					UFDouble low_temp = inv_line_b.get("low_temp") == null ? null : new UFDouble(inv_line_b.get("low_temp").getValueAsDouble());
					UFDouble hight_temp = inv_line_b.get("hight_temp") == null ? null : new UFDouble(inv_line_b.get("hight_temp").getValueAsDouble());
					UFDouble LineBPack_num_count = inv_line_b.get("pack_num_count") == null ? null : new UFDouble(inv_line_b.get("pack_num_count").getValueAsDouble());
					UFDouble plan_pack_num_count = inv_line_b.get("plan_pack_num_count") == null ? null : new UFDouble(inv_line_b.get("plan_pack_num_count").getValueAsDouble());
					UFDouble LineBDef11 =inv_line_b.get("def11") == null ? null : new UFDouble(inv_line_b.get("def11").getValueAsDouble());
					UFDouble LineBDef12 =inv_line_b.get("def12") == null ? null : new UFDouble(inv_line_b.get("def12").getValueAsDouble());
					
					InvLineBVO invLineBVO = new InvLineBVO();
					invLineBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invLineBVO);
					invLineBVO.setPk_invoice(invoiceVO.getPk_invoice());
					invLineBVO.setSerialno(serialno);
					invLineBVO.setPk_address(pk_address);;
					invLineBVO.setContact(LineBcontact);
					invLineBVO.setPhone(phone);
					invLineBVO.setMobile(mobile);
					invLineBVO.setEmail(email);
					invLineBVO.setOperate_type(operate_type);
					invLineBVO.setReq_date_from(req_date_from);
					invLineBVO.setReq_date_till(req_date_till);
					invLineBVO.setGoods_code(goods_code);
					invLineBVO.setGoods_name(goods_name);
					invLineBVO.setNum(num);
					invLineBVO.setPack(pack);
					invLineBVO.setWeight(weight);
					invLineBVO.setVolume(volume);
					invLineBVO.setUnit_volume(unit_volume);
					invLineBVO.setUnit_weight(unit_weight);
					invLineBVO.setLength(length);
					invLineBVO.setWidth(wordernoth);
					invLineBVO.setHeight(height);
					invLineBVO.setTrans_note(trans_note);
					invLineBVO.setLow_temp(low_temp);
					invLineBVO.setHight_temp(hight_temp);
					invLineBVO.setReference_no(reference_no);
					invLineBVO.setMemo(LineBMemo);
					invLineBVO.setPod_num(pod_num);
					invLineBVO.setReject_num(reject_num);
					invLineBVO.setDamage_num(damage_num);
					invLineBVO.setLost_num(lost_num);
					invLineBVO.setMin_pack(min_pack);
					invLineBVO.setPack_num_count(LineBPack_num_count);
					invLineBVO.setGoods_type(goods_type);
					invLineBVO.setPlan_num(plan_num);
					invLineBVO.setPlan_pack_num_count(plan_pack_num_count);
					invLineBVO.setDef1(LineBDef1);
					invLineBVO.setDef2(LineBDef2);
					invLineBVO.setDef3(LineBDef3);
					invLineBVO.setDef4(LineBDef4);
					invLineBVO.setDef5(LineBDef5);
					invLineBVO.setDef6(LineBDef6);
					invLineBVO.setDef7(LineBDef7);
					invLineBVO.setDef8(LineBDef8);
					invLineBVO.setDef9(LineBDef9);
					invLineBVO.setDef10(LineBDef10);
					invLineBVO.setDef11(LineBDef11);
					invLineBVO.setDef12(LineBDef12);
					
					invLineBVOs.add(invLineBVO);
				}
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_INV_LINE_B, invLineBVOs.toArray(new InvLineBVO[invLineBVOs.size()]));
			}
			if (flag) {
				continue;
			}
			JsonNode inv_req_bs = order.get("inv_req_b");
			if(inv_req_bs != null && inv_req_bs.size() > 0){
				List<InvReqBVO> invReqBVOs = new ArrayList<InvReqBVO>();
				int serialno = 0;
				for(JsonNode inv_req_b : inv_req_bs){
					String orderBErrorMsg = CheckInvReqB(inv_req_b);
					if(!orderBErrorMsg.isEmpty()){
						 failed.add(orderBErrorMsg);
						 flag = true;
						 break;
					}
					serialno += 10;
					String req_code =inv_req_b.get("req_code") == null ? null : inv_req_b.get("req_code").getTextValue();
					String req_name =inv_req_b.get("req_name") == null ? null : inv_req_b.get("req_name").getTextValue();
					String syncexp_memo =inv_req_b.get("syncexp_memo") == null ? null : inv_req_b.get("syncexp_memo").getTextValue();
					String reqBDef1 =inv_req_b.get("def1") == null ? null : inv_req_b.get("def1").getTextValue();
					String reqBDef2 =inv_req_b.get("def2") == null ? null : inv_req_b.get("def2").getTextValue();
					String reqBDef3 =inv_req_b.get("def3") == null ? null : inv_req_b.get("def3").getTextValue();
					String reqBDef4 =inv_req_b.get("def4") == null ? null : inv_req_b.get("def4").getTextValue();
					String reqBDef5 =inv_req_b.get("def5") == null ? null : inv_req_b.get("def5").getTextValue();
					String reqBDef6 =inv_req_b.get("def6") == null ? null : inv_req_b.get("def6").getTextValue();
					String reqBDef7 =inv_req_b.get("def7") == null ? null : inv_req_b.get("def7").getTextValue();
					String reqBDef8 =inv_req_b.get("def8") == null ? null : inv_req_b.get("def8").getTextValue();
					String reqBDef9 =inv_req_b.get("def9") == null ? null : inv_req_b.get("def9").getTextValue();
					String reqBDef10 =inv_req_b.get("def10") == null ? null : inv_req_b.get("def10").getTextValue();
					
					Integer req_type = null;
					if( order.get("req_type") != null){
						Object realValue = this.getValueByDataDict("order_type", order.get("req_type").getTextValue());
						if(realValue == null){
							failed.add(orderno + ":req_type:"+order.get("req_type").getTextValue()+"不正确！");
							flag = true;
							break;
						}
						req_type = (Integer) realValue;
					}
					UFDouble reqBDef11 =inv_req_b.get("def11") == null ? null : new UFDouble(inv_req_b.get("def11").getValueAsDouble());
					UFDouble reqBDef12 =inv_req_b.get("def12") == null ? null : new UFDouble(inv_req_b.get("def12").getValueAsDouble());
					
					
					InvReqBVO invReqBVO = new  InvReqBVO();
					invReqBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invReqBVO);
					invReqBVO.setPk_invoice(invoiceVO.getPk_invoice());
					invReqBVO.setSerialno(serialno);
					invReqBVO.setReq_code(req_code);
					invReqBVO.setReq_name(req_name);
					invReqBVO.setReq_type(req_type);
					invReqBVO.setSyncexp_memo(syncexp_memo);
					invReqBVO.setDef1(reqBDef1);
					invReqBVO.setDef2(reqBDef2);
					invReqBVO.setDef3(reqBDef3);
					invReqBVO.setDef4(reqBDef4);
					invReqBVO.setDef5(reqBDef5);
					invReqBVO.setDef6(reqBDef6);
					invReqBVO.setDef7(reqBDef7);
					invReqBVO.setDef8(reqBDef8);
					invReqBVO.setDef9(reqBDef9);
					invReqBVO.setDef10(reqBDef10);
					invReqBVO.setDef11(reqBDef11);
					invReqBVO.setDef12(reqBDef12);
					
					invReqBVOs.add(invReqBVO);
				}
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_INV_REQ_B, invReqBVOs.toArray(new InvReqBVO[invReqBVOs.size()]));
			}
			if (flag) {
				continue;
			}
			String conMsg = convertBeforeSave(exAggInvoiceVO,orderno);
			if(!conMsg.isEmpty()){
				 failed.add(conMsg);
				 continue;
			}
			if(oldInvoiceVO != null){
				paramVO.setBillId(oldInvoiceVO.getPrimaryKey());
				ExAggInvoiceVO oldInvAggVO = (ExAggInvoiceVO) service.queryBillVO(paramVO);
				invoiceVO.setPrimaryKey(oldInvoiceVO.getPrimaryKey());
				InvPackBVO[] oldPackBVOs = (InvPackBVO[]) oldInvAggVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
				InvLineBVO[] oldLineBVOs =  (InvLineBVO[]) oldInvAggVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
				TransBilityBVO[] oldTranBVOs = (TransBilityBVO[]) oldInvAggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
				InvReqBVO[] oldReqBVOs =  (InvReqBVO[]) oldInvAggVO.getTableVO(TabcodeConst.TS_INV_REQ_B);
				
				InvPackBVO[] packBVOs = (InvPackBVO[]) exAggInvoiceVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
				InvLineBVO[] lineBVOs =  (InvLineBVO[]) exAggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
				TransBilityBVO[] tranBVOs = (TransBilityBVO[]) exAggInvoiceVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
				InvReqBVO[] reqBVOs =  (InvReqBVO[]) exAggInvoiceVO.getTableVO(TabcodeConst.TS_INV_REQ_B);
				List<InvPackBVO> allInvPackBVOs = new ArrayList<InvPackBVO>();
				if(oldPackBVOs != null && oldPackBVOs.length > 0){
					for(InvPackBVO invPackBVO : oldPackBVOs){
						invPackBVO.setStatus(VOStatus.DELETED);
					}
					allInvPackBVOs.addAll(Arrays.asList(oldPackBVOs));
				}
				if(packBVOs != null && packBVOs.length > 0){
					allInvPackBVOs.addAll(Arrays.asList(packBVOs));
				}
				List<InvLineBVO> allInvLineBVOs = new ArrayList<InvLineBVO>();
				if(oldLineBVOs != null && oldLineBVOs.length > 0){
					for(InvLineBVO invLineBVO : oldLineBVOs){
						invLineBVO.setStatus(VOStatus.DELETED);
					}
					allInvLineBVOs.addAll(Arrays.asList(oldLineBVOs));
				}
				if(lineBVOs != null && lineBVOs.length > 0){
					allInvLineBVOs.addAll(Arrays.asList(lineBVOs));
				}
				List<TransBilityBVO> allInvTransBVOs = new ArrayList<TransBilityBVO>();
				if(oldTranBVOs != null && oldTranBVOs.length > 0){
					for(TransBilityBVO oldtranBVOs : oldTranBVOs){
						oldtranBVOs.setStatus(VOStatus.DELETED);
					}
					allInvTransBVOs.addAll(Arrays.asList(oldTranBVOs));
				}
				if(tranBVOs != null && tranBVOs.length > 0){
					allInvTransBVOs.addAll(Arrays.asList(tranBVOs));
				}
				List<InvReqBVO> allInvReqBVOs = new ArrayList<InvReqBVO>();
				if(oldReqBVOs != null && oldReqBVOs.length > 0){
					for(InvReqBVO oldReqBVO : oldReqBVOs){
						oldReqBVO.setStatus(VOStatus.DELETED);
					}
					allInvReqBVOs.addAll(Arrays.asList(oldReqBVOs));
				}
				if(reqBVOs != null && reqBVOs.length > 0){
					allInvReqBVOs.addAll(Arrays.asList(reqBVOs));
				}
				invoiceVO.setStatus(VOStatus.UPDATED);
				invoiceVO.setVbillno(oldInvoiceVO.getVbillno());
				exAggInvoiceVO.setParentVO(invoiceVO);
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_INV_PACK_B, allInvPackBVOs.toArray(new InvPackBVO[allInvPackBVOs.size()]));
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_INV_LINE_B, allInvLineBVOs.toArray(new InvLineBVO[allInvLineBVOs.size()]));
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_TRANS_BILITY_B, allInvTransBVOs.toArray(new TransBilityBVO[allInvTransBVOs.size()]));
				exAggInvoiceVO.setTableVO(TabcodeConst.TS_INV_REQ_B, allInvReqBVOs.toArray(new InvReqBVO[allInvReqBVOs.size()]));
			}
			service.save(exAggInvoiceVO, paramVO);
			if(oldInvoiceVO != null){
				success.add("更新：" + orderno +","+invoiceVO.getVbillno());
			}else{
				success.add("新建：" + orderno +","+invoiceVO.getVbillno());
			}
			
		}
		result.put("failed", failed);
		result.put("success", success);
		return JacksonUtils.writeValueAsString(result);
		
	}

	private String CheckOrderInfo(JsonNode order) {
		JsonNode orderno = order.get("orderno"); //订单号
		JsonNode pk_customer = order.get("pk_customer"); //客户
		JsonNode pk_trans_type = order.get("pk_trans_type");//运输方式
		JsonNode req_deli_date = order.get("req_deli_date");//要求提货时间
		JsonNode req_arri_date = order.get("req_arri_date");//要求到货时间
		JsonNode pk_delivery = order.get("pk_delivery");//提货方
		JsonNode pk_arrival = order.get("pk_arrival");//收货方
		JsonNode pk_corp = order.get("pk_corp");//公司
	
		if(orderno == null || pk_customer == null || pk_trans_type == null || req_deli_date == null
				|| req_arri_date == null || pk_delivery == null || pk_arrival == null || pk_corp == null){
			return "主表结构不正确，请确认！";
		}
		
		String strorderno = order.get("orderno").getTextValue();
		String strPk_customer = order.get("pk_customer").getTextValue();
		String strPk_trans_type = order.get("pk_trans_type").getTextValue();
		UFDateTime req_deli_dateTime = new UFDateTime(order.get("req_deli_date").getTextValue());
		UFDateTime req_arri_dateTime = new UFDateTime(order.get("req_arri_date").getTextValue());
		String strPk_delivery = order.get("pk_delivery").getTextValue();
		String strPk_arrival = order.get("pk_arrival").getTextValue();
		String strPk_corp = order.get("pk_corp").getTextValue();
		
		if(strorderno == null || strPk_customer == null || strPk_trans_type == null || req_deli_dateTime == null
				|| req_arri_dateTime == null || strPk_delivery == null || strPk_arrival == null|| strPk_corp == null){
			return "主表 Order必填项为空，请确认！";
		}
		return "";
	}
	
	private String CheckInvPackB(JsonNode inv_pack_b) {
		return "";
	}
	
	
	private String CheckInvTransB(JsonNode inv_trans_b) {
		JsonNode pk_car_type = inv_trans_b.get("pk_car_type"); //车辆类型
		if(pk_car_type == null ){
			return "子表Trans结构不正确，请确认！";
		}
		String strPk_car_type = inv_trans_b.get("pk_car_type").getTextValue();
		if(strPk_car_type == null){
			return "子表 Trans必填项为空，请确认！";
		}
		return "";
	}

	private String CheckInvLineB(JsonNode inv_line_b) {
		JsonNode operate_type = inv_line_b.get("operate_type"); //操作类型
		JsonNode req_date_from = inv_line_b.get("req_date_from"); //要求到达时间
		JsonNode req_date_till = inv_line_b.get("req_date_till"); //要求离开时间
		
		if(operate_type == null || req_date_from == null|| req_date_till == null){
			return "子表Line结构不正确，请确认！";
		}
		
		Integer operate_typeInt = inv_line_b.get("operate_type").getValueAsInt();
		UFDateTime req_date_fromTime = new UFDateTime(inv_line_b.get("req_date_from").getTextValue());
		UFDateTime req_date_tillTime = new UFDateTime(inv_line_b.get("req_date_till").getTextValue());
		if(operate_typeInt == null || req_date_fromTime == null|| req_date_tillTime == null){
			return "子表 Line必填项为空，请确认！";
		}
		
		return "";
	}

	private String CheckInvReqB(JsonNode inv_req_b) {
		return "";
	}

	//执行保存前的字段转换
	private String convertBeforeSave(ExAggInvoiceVO exAggInvoiceVO,String orderno) {
		
		if(exAggInvoiceVO == null){
			return orderno + ":导入信息为空";
		}
		List<String> addrs = new ArrayList<String>();
		InvoiceVO invoiceVO = (InvoiceVO) exAggInvoiceVO.getParentVO();
		InvPackBVO[] invPackBVOs = (InvPackBVO[]) exAggInvoiceVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		InvLineBVO[] invLineBVOs =  (InvLineBVO[]) exAggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
		if(invoiceVO == null){
			return orderno + ":主表信息为空";
		}
		addrs.add(invoiceVO.getPk_delivery());
		addrs.add(invoiceVO.getPk_arrival());
		
		if(invLineBVOs != null && invLineBVOs.length > 0){
			for(InvLineBVO invLineBVO : invLineBVOs){
				if(StringUtils.isNotBlank(invLineBVO.getPk_address())){
					addrs.add(invLineBVO.getPk_address());
				}
			}
		}
		String addressCond = NWUtils.buildConditionString(addrs.toArray(new String[addrs.size()]));
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, " addr_code in " + addressCond);
		if(addressVOs == null || addressVOs.length == 0){
			return orderno + ":地址信息不正确";
		}
		//处理地址信息
		boolean flag = true;
		for(AddressVO addressVO : addressVOs){
			if(invoiceVO.getPk_delivery().equals(addressVO.getAddr_code())){
				invoiceVO.setPk_delivery(addressVO.getPk_address());
				invoiceVO.setDeli_city(addressVO.getPk_city());
				invoiceVO.setDeli_province(addressVO.getPk_province());
				invoiceVO.setDeli_area(addressVO.getPk_area());
				invoiceVO.setDeli_detail_addr(addressVO.getDetail_addr());
				if(StringUtils.isBlank(invoiceVO.getDeli_contact())){
					invoiceVO.setDeli_contact(addressVO.getContact());
				}
				if(StringUtils.isBlank(invoiceVO.getDeli_phone())){
					invoiceVO.setDeli_phone(addressVO.getPhone());
				}
				if(StringUtils.isBlank(invoiceVO.getDeli_mobile())){
					invoiceVO.setDeli_mobile(addressVO.getMobile());
				}
				if(StringUtils.isBlank(invoiceVO.getDeli_email())){
					invoiceVO.setDeli_email(addressVO.getEmail());
				}
				flag = false;
				break;
			}
		}
		if (flag) {
			return orderno + ":提货地址信息不正确"+invoiceVO.getPk_delivery();
		}
		
		flag = true;
		for(AddressVO addressVO : addressVOs){
			if(invoiceVO.getPk_arrival().equals(addressVO.getAddr_code())){
				invoiceVO.setPk_arrival(addressVO.getPk_address());
				invoiceVO.setArri_city(addressVO.getPk_city());
				invoiceVO.setArri_province(addressVO.getPk_province());
				invoiceVO.setArri_area(addressVO.getPk_area());
				invoiceVO.setArri_detail_addr(addressVO.getDetail_addr());
				if(StringUtils.isBlank(invoiceVO.getArri_contact())){
					invoiceVO.setArri_contact(addressVO.getContact());
				}
				if(StringUtils.isBlank(invoiceVO.getArri_phone())){
					invoiceVO.setArri_phone(addressVO.getPhone());
				}
				if(StringUtils.isBlank(invoiceVO.getArri_mobile())){
					invoiceVO.setArri_mobile(addressVO.getMobile());
				}
				if(StringUtils.isBlank(invoiceVO.getArri_email())){
					invoiceVO.setArri_email(addressVO.getEmail());
				}
				flag = false;
				break;
			}
		}
		if (flag) {
			return orderno + ":收货地址信息不正确"+invoiceVO.getPk_arrival();
		}
		
		if(invLineBVOs != null && invLineBVOs.length > 0){
			for(InvLineBVO invLineBVO : invLineBVOs){
				if(StringUtils.isNotBlank(invLineBVO.getPk_address())){
					flag = true;
					for(AddressVO addressVO : addressVOs){
						if(invLineBVO.getPk_address().equals(addressVO.getAddr_code())){
							invLineBVO.setPk_address(addressVO.getPk_address());
							invLineBVO.setPk_city(addressVO.getPk_city());
							invLineBVO.setPk_province(addressVO.getPk_province());
							invLineBVO.setPk_area(addressVO.getPk_area());
							invLineBVO.setDetail_addr(addressVO.getDetail_addr());
							if(StringUtils.isBlank(invLineBVO.getContact())){
								invLineBVO.setContact(addressVO.getContact());
							}
							if(StringUtils.isBlank(invLineBVO.getPhone())){
								invLineBVO.setPhone(addressVO.getPhone());
							}
							if(StringUtils.isBlank(invLineBVO.getMobile())){
								invLineBVO.setMobile(addressVO.getMobile());
							}
							if(StringUtils.isBlank(invLineBVO.getEmail())){
								invLineBVO.setEmail(addressVO.getEmail());
							}
							flag = false;
							break;
						}
					}
					if (flag) {
						return orderno + ":路线表地址信息不正确"+invoiceVO.getPk_arrival();
					}
					
				}
			}
		}
		
		//处理货品信息
		List<String> goodsCodes = new ArrayList<String>();
		if(invPackBVOs != null && invPackBVOs.length > 0){
			for(InvPackBVO invPackBVO : invPackBVOs){
				if(StringUtils.isNotBlank(invPackBVO.getGoods_code())){
					goodsCodes.add(invPackBVO.getGoods_code());
				}
			}
		}
		if(invLineBVOs != null && invLineBVOs.length > 0){
			for(InvLineBVO invLineBVO : invLineBVOs){
				if(StringUtils.isNotBlank(invLineBVO.getGoods_code())){
					goodsCodes.add(invLineBVO.getGoods_code());
				}
			}
		}
		if(goodsCodes.size() > 0){
			String goodsCodesCond = NWUtils.buildConditionString(goodsCodes.toArray(new String[goodsCodes.size()]));
			GoodsVO[] goodsVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(GoodsVO.class, " goods_code in " + goodsCodesCond +" and pk_customer =?",invoiceVO.getPk_customer());
			if(goodsVOs != null && goodsVOs.length > 0){
				for(GoodsVO goodsVO : goodsVOs){
					if(invPackBVOs != null && invPackBVOs.length > 0){
						for(InvPackBVO invPackBVO : invPackBVOs){
							if(StringUtils.isNotBlank(invPackBVO.getGoods_code()) && invPackBVO.getGoods_code().equals(goodsVO.getGoods_code())){
								invPackBVO.setPk_goods(goodsVO.getPk_goods());
								invPackBVO.setGoods_name(goodsVO.getGoods_name());
								invPackBVO.setGoods_type(goodsVO.getPk_goods_type());
							}
						}
					}
					if(invLineBVOs != null && invLineBVOs.length > 0){
						for(InvLineBVO invLineBVO : invLineBVOs){
							if(StringUtils.isNotBlank(invLineBVO.getGoods_code()) && invLineBVO.getGoods_code().equals(goodsVO.getGoods_code())){
								invLineBVO.setPk_goods(goodsVO.getPk_goods());
								invLineBVO.setGoods_name(goodsVO.getGoods_name());
								invLineBVO.setGoods_type(goodsVO.getPk_goods_type());
							}
						}
					}
					
				}
			}
		}
		return "";
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
		
		service = (InvoiceService) SpringContextHolder.getApplicationContext().getBean("invoiceServiceImpl");
		paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.INVOICE_CODE);
		paramVO.getAttr().put("updateAddr", false);// 地址和编码从中间表中传入，已经会做处理，比如地址不存在的话会自动加入。不要在processBeforeSave中处理，否则地址编码不好处理

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
