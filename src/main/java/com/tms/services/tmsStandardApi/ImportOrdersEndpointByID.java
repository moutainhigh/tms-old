package com.tms.services.tmsStandardApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.nw.dao.NWDao;
import org.nw.exception.ApiException;
import org.nw.json.JacksonUtils;
import org.nw.service.api.AuthenticationService;
import org.nw.utils.BillnoHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DeptVO;
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
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;

@SuppressWarnings("deprecation")
public class ImportOrdersEndpointByID extends ServletEndpointSupport{
	
	Logger logger = Logger.getLogger("EDI");
	
	private static String  EDI_USER_CODE = "EdiUser";
	private final static String TRUE = "TRUE";
	private final static String FALSE = "FALSE";
	
	private static	String ediUserSql = "SELECT * FROM nw_user WHERE isnull(dr,0) = 0 AND isnull(locked_flag,'N')='N' AND user_code = ?";
	private static	UserVO ediUserVO = NWDao.getInstance().queryForObject(ediUserSql, UserVO.class, EDI_USER_CODE);
	
	
	private InvoiceService service;
	private ParamVO paramVO;
	
	public String importOrders (String uid,String pwd,String jsonOrders) throws ApiException{
		logger.info("开始同步订单数据：" + jsonOrders);
		List<Map<String,Object>> jdResult = new ArrayList<Map<String,Object>>();
		String dateStr = (new UFDateTime(new Date())).toString();
		Map<String,Object> retMap = new HashMap<String, Object>();
		
		String authError = checkBeforeImport(uid,pwd);
		if(!authError.isEmpty()){
			retMap.put("success", FALSE);
			retMap.put("errormessage", authError);
			retMap.put("responseDate", dateStr);
			jdResult.add(retMap);
			return JacksonUtils.writeValueAsString(jdResult);
			 
		}
		//空值判断
		if(jsonOrders.isEmpty()){
			retMap.put("success", FALSE);
			retMap.put("errormessage", "没有获取到数据！");
			retMap.put("responseDate", dateStr);
			jdResult.add(retMap);
			return JacksonUtils.writeValueAsString(jdResult);
			 
		}
		JsonNode orders = JacksonUtils.readTree(jsonOrders);
		//判断参数有效性
		if(orders  == null){
			retMap.put("success", FALSE);
			retMap.put("errormessage", "输入的参数不是要求的Json格式，请修改！");
			retMap.put("responseDate", dateStr);
			jdResult.add(retMap);
			return JacksonUtils.writeValueAsString(jdResult);
		}
		
		for(JsonNode order : orders ){
			
			String SubServiceId = order.get("SubServiceId") == null ? null : order.get("SubServiceId").getTextValue();
			String TenantId = order.get("TenantId") == null ? "0001" : order.get("TenantId").getTextValue();
			String AdvanceOrderId = order.get("AdvanceOrderId") == null ? null : order.get("AdvanceOrderId").getTextValue();
			String Id = order.get("Id") == null ? null : order.get("Id").getTextValue();
			String OrderNo = order.get("OrderNo") == null ? null : order.get("OrderNo").getTextValue();
			String InvoiceNo = order.get("InvoiceNo") == null ? null : order.get("InvoiceNo").getTextValue();
			String TrustCustomerId = order.get("TrustCustomerId") == null ? null : order.get("TrustCustomerId").getTextValue();
			String PickupTime = order.get("PickupTime") == null ? null : order.get("PickupTime").getTextValue();
			String DeliveryTime = order.get("DeliveryTime") == null ? null : order.get("DeliveryTime").getTextValue();
			String PickupAddressId = order.get("PickupAddressId") == null ? null : order.get("PickupAddressId").getTextValue();
			String PickupAddress = order.get("PickupAddress") == null ? null : order.get("PickupAddress").getTextValue();
			String DeliveryAddressId = order.get("DeliveryAddressId") == null ? null : order.get("DeliveryAddressId").getTextValue();
			String DeliveryAddress = order.get("DeliveryAddress") == null ? null : order.get("DeliveryAddress").getTextValue();
			String MainOrderNo = order.get("MainOrderNo") == null ? null : order.get("MainOrderNo").getTextValue();
			Integer Count = order.get("Count") == null ? 0 : Integer.parseInt(order.get("Count").getTextValue());
			UFDouble Weight = order.get("Weight") == null ? UFDouble.ZERO_DBL : new UFDouble(order.get("Weight").getTextValue());
			UFDouble Volume = order.get("Volume") == null ? UFDouble.ZERO_DBL : new UFDouble(order.get("Volume").getTextValue());
			String Cname = order.get("Cname") == null ? null : order.get("Cname").getTextValue();
			String SubServiceCode = order.get("SubServiceCode") == null ? null : order.get("SubServiceCode").getTextValue();
			String ApportionMethod = order.get("ApportionMethod") == null ? null : order.get("ApportionMethod").getTextValue();
			Integer IsUrgent = order.get("IsUrgent") == null ? 0 : UFBoolean.TRUE.equals(new UFBoolean(order.get("IsUrgent").getTextValue())) == true ? 1 : 0;
			String UrgentReason = order.get("UrgentReason") == null ? null : order.get("UrgentReason").getTextValue();
			Integer PaymentType = order.get("PaymentType") == null ? 0 : Integer.parseInt(order.get("PaymentType").getTextValue() == null ? "0" : order.get("PaymentType").getTextValue());
			String FreightFlow = order.get("FreightFlow") == null ? null : order.get("FreightFlow").getTextValue();
			String OrderOperator = order.get("OrderOperator") == null ? null : order.get("OrderOperator").getTextValue();
			String SupervisionType = order.get("SupervisionType") == null ? null : order.get("SupervisionType").getTextValue();
			String OtherCarRequirement = order.get("OtherCarRequirement") == null ? null : order.get("OtherCarRequirement").getTextValue();
			String RoadType = order.get("RoadType") == null ? null : order.get("RoadType").getTextValue();
			String OtherSpecialRequirement = order.get("OtherSpecialRequirement") == null ? null : order.get("OtherSpecialRequirement").getTextValue();
			String OperationFlag = order.get("OperationFlag") == null ? null : order.get("OperationFlag").getTextValue();
			UFDouble TotalMoney = order.get("TotalMoney") == null ? UFDouble.ZERO_DBL : new UFDouble(order.get("TotalMoney").getTextValue());
			String MoneyType = order.get("MoneyType") == null ? null : order.get("MoneyType").getTextValue();
			String OperationTargetId = order.get("OperationTargetId") == null ? null : order.get("OperationTargetId").getTextValue();
			String DepartmentId = order.get("DepartmentId") == null ? null : order.get("DepartmentId").getTextValue();
			String TeamId = order.get("TeamId") == null ? null : order.get("TeamId").getTextValue();
			
			List<SuperVO> toBeDelete = new ArrayList<SuperVO>();
			InvoiceVO invoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "def1=?", SubServiceId);
			if(invoiceVO != null){
				//更新
				invoiceVO.setStatus(VOStatus.UPDATED);
				invoiceVO.setModify_time(new UFDateTime(new Date()));
				invoiceVO.setModify_user(ediUserVO.getPk_user());
				//删除明细 
				InvPackBVO[] oldPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", invoiceVO.getPk_invoice());
				if(oldPackBVOs != null && oldPackBVOs.length > 0){
					toBeDelete.addAll(Arrays.asList(oldPackBVOs));
				}
				InvLineBVO[] oldLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvLineBVO.class, "pk_invoice=?", invoiceVO.getPk_invoice());
				if(oldLineBVOs != null && oldLineBVOs.length > 0){
					toBeDelete.addAll(Arrays.asList(oldLineBVOs));
				}
				TransBilityBVO[] oldTransBilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(TransBilityBVO.class, "pk_invoice=?", invoiceVO.getPk_invoice());
				if(oldTransBilityBVOs != null && oldTransBilityBVOs.length > 0){
					toBeDelete.addAll(Arrays.asList(oldTransBilityBVOs));
				}
			}else{
				invoiceVO = new InvoiceVO();
				invoiceVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(invoiceVO);
				invoiceVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.FHD));
				invoiceVO.setVbillstatus(BillStatus.NEW);
				invoiceVO.setCreate_time(new UFDateTime(new Date()));
				invoiceVO.setCreate_user(ediUserVO.getPk_user());
			}
			invoiceVO.setDef1(SubServiceId);
			invoiceVO.setDef6(AdvanceOrderId);
			invoiceVO.setDef9(Id);
			invoiceVO.setOrderno(OrderNo);
			invoiceVO.setCust_orderno(InvoiceNo);
			invoiceVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.EDI.intValue());
			CustomerVO customerVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "def1=?", TrustCustomerId);
			if(customerVO == null){
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "TrustCustomerId不存在：" + TrustCustomerId);
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				continue;
			}else{
				invoiceVO.setPk_customer(customerVO.getPk_customer());
			}
			invoiceVO.setReq_deli_date((new UFDateTime(PickupTime)).toString());
			invoiceVO.setReq_arri_date((new UFDateTime(DeliveryTime)).toString());
			AddressVO delivery = NWDao.getInstance().queryByCondition(AddressVO.class, "def1=?", PickupAddressId);
			if(delivery == null){
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "PickupAddressId不存在：" + PickupAddressId);
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				continue;
			}else{
				invoiceVO.setPk_delivery(delivery.getPk_address());
				invoiceVO.setDeli_area(delivery.getPk_area());
				invoiceVO.setDeli_city(delivery.getPk_city());
				invoiceVO.setDeli_province(delivery.getPk_province());
				invoiceVO.setDeli_detail_addr(delivery.getDetail_addr());
				invoiceVO.setDeli_contact(delivery.getContact());
				invoiceVO.setDeli_email(delivery.getEmail());
				invoiceVO.setDeli_mobile(delivery.getMobile());
				invoiceVO.setDeli_phone(delivery.getPhone());
				if(StringUtils.isNotBlank(PickupAddress)){
					invoiceVO.setDeli_detail_addr(PickupAddress);
				}
			}
			AddressVO arrival = NWDao.getInstance().queryByCondition(AddressVO.class, "def1=?", DeliveryAddressId);
			if(arrival == null){
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "DeliveryAddressId不存在：" + DeliveryAddressId);
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				continue;
			}else{
				invoiceVO.setPk_arrival(arrival.getPk_address());
				invoiceVO.setArri_area(arrival.getPk_area());
				invoiceVO.setArri_city(arrival.getPk_city());
				invoiceVO.setArri_province(arrival.getPk_province());
				invoiceVO.setArri_detail_addr(arrival.getDetail_addr());
				invoiceVO.setArri_contact(arrival.getContact());
				invoiceVO.setArri_email(arrival.getEmail());
				invoiceVO.setArri_mobile(arrival.getMobile());
				invoiceVO.setArri_phone(arrival.getPhone());
				if(StringUtils.isNotBlank(DeliveryAddress)){
					invoiceVO.setArri_detail_addr(DeliveryAddress);
				}
			}
			invoiceVO.setDef3(MainOrderNo);
			invoiceVO.setNum_count(Count);
			invoiceVO.setWeight_count(Weight);
			invoiceVO.setVolume_count(Volume);
			invoiceVO.setInvoice_goods_type(Cname);
			TransTypeVO transTypeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "def1=?", SubServiceCode);
			if(transTypeVO == null){
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "SubServiceCode不存在：" + SubServiceCode);
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				continue;
			}else{
				invoiceVO.setPk_trans_type(transTypeVO.getPk_trans_type());
			}
			invoiceVO.setDef4(ApportionMethod);
			invoiceVO.setUrgent_level(IsUrgent);
			invoiceVO.setUrgent_reson(UrgentReason);
			invoiceVO.setBalatype(PaymentType);
			invoiceVO.setCargo_flow(FreightFlow);
			invoiceVO.setDef5(OrderOperator);
			invoiceVO.setSupervision_type(SupervisionType);
			invoiceVO.setVehicle_req(OtherCarRequirement);
			invoiceVO.setTransport_req(RoadType);
			invoiceVO.setOther_req(OtherSpecialRequirement);
			invoiceVO.setDef2(OperationFlag);
			invoiceVO.setAmount(TotalMoney);
			invoiceVO.setCurrency(MoneyType);
			DeptVO deptVO = NWDao.getInstance().queryByCondition(DeptVO.class, "def1=?", DepartmentId);
			if(deptVO == null){
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "DepartmentId不存在：" + DepartmentId);
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				continue;
			}else{
				invoiceVO.setPk_dept(deptVO.getPk_dept());
			}
			invoiceVO.setItem_name(TeamId);
			CorpVO corpVO = NWDao.getInstance().queryByCondition(CorpVO.class, "def1=?", OperationTargetId);
			if(corpVO == null){
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "OperationTargetId不存在：" + OperationTargetId);
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				continue;
			}else{
				invoiceVO.setPk_corp(corpVO.getPk_corp());
				if(StringUtils.isBlank(corpVO.getDef3())){
					retMap.put("orderNo", OrderNo);
					retMap.put("invoiceNo", InvoiceNo);
					retMap.put("success", FALSE);
					retMap.put("errormessage", "结算客户不存在：" + OperationTargetId);
					retMap.put("responseDate", dateStr);
					jdResult.add(retMap);
					continue;
				}
				invoiceVO.setBala_customer(corpVO.getDef3());
			}
			JsonNode inv_pack_bs = order.get("inv_pack_b");
			InvPackBVO[] invPackBVOs = null;
			if(inv_pack_bs != null && inv_pack_bs.size() > 0){
				invPackBVOs = new InvPackBVO[inv_pack_bs.size()];
				int index = 0;
				for(JsonNode inv_pack_b : inv_pack_bs){
					Integer Count_b = inv_pack_b.get("Count") == null ? 0 : Integer.parseInt(inv_pack_b.get("Count").getTextValue());
					String PackingType = inv_pack_b.get("PackingType") == null ? null : inv_pack_b.get("PackingType").getTextValue();
					UFDouble X = inv_pack_b.get("X") == null ? null : new UFDouble(inv_pack_b.get("X").getTextValue());
					UFDouble Y = inv_pack_b.get("Y") == null ? null : new UFDouble(inv_pack_b.get("Y").getTextValue());
					UFDouble Z = inv_pack_b.get("Z") == null ? null : new UFDouble(inv_pack_b.get("Z").getTextValue());
					String CanSuperposition = inv_pack_b.get("CanSuperposition") == null ? null : inv_pack_b.get("CanSuperposition").getTextValue();
					String SuperpositionCount = inv_pack_b.get("SuperpositionCount") == null ? null : inv_pack_b.get("SuperpositionCount").getTextValue();
					UFDouble PieceWeight = inv_pack_b.get("PieceWeight") == null ? null : new UFDouble(inv_pack_b.get("PieceWeight").getTextValue());
					UFDouble pack_Volume = inv_pack_b.get("Volume") == null ? null : new UFDouble(inv_pack_b.get("Volume").getTextValue());
					UFDouble pack_Weight = inv_pack_b.get("Weight") == null ? null : new UFDouble(inv_pack_b.get("Weight").getTextValue());
					String IsNonstandard = inv_pack_b.get("IsNonstandard") == null ? null : inv_pack_b.get("IsNonstandard").getTextValue();
					String ProductType = inv_pack_b.get("ProductType") == null ? null : inv_pack_b.get("ProductType").getTextValue();
					String DangerousType = inv_pack_b.get("DangerousType") == null ? null : inv_pack_b.get("DangerousType").getTextValue();
					InvPackBVO invPackBVO = new InvPackBVO();
					invPackBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invPackBVO);
					invPackBVO.setPk_invoice(invoiceVO.getPk_invoice());
					invPackBVO.setNum(Count_b);
					invPackBVO.setWeight(pack_Weight);
					invPackBVO.setVolume(pack_Volume);
					invPackBVO.setPack(PackingType);
					invPackBVO.setLength(X);
					invPackBVO.setWidth(Y);
					invPackBVO.setHeight(Z);
					invPackBVO.setDef1(CanSuperposition);
					invPackBVO.setDef2(SuperpositionCount);
					invPackBVO.setUnit_weight(PieceWeight);
					invPackBVO.setDef3(IsNonstandard);
					invPackBVO.setGoods_type(ProductType);
					invPackBVO.setDef4(DangerousType);
					invPackBVOs[index] = invPackBVO;
					index ++;
				}
			}
			boolean exit = false;
			JsonNode inv_line_bs = order.get("inv_line_b");
			InvLineBVO[] invLineBVOs = null;
			if(inv_line_bs != null && inv_line_bs.size() > 0){
				invLineBVOs = new InvLineBVO[inv_line_bs.size()];
				int index = 0;
				for(JsonNode inv_line_b : inv_line_bs){
					Integer Type = inv_line_b.get("Type") == null ? null : Integer.parseInt(inv_line_b.get("Type").getTextValue());
					String ShippingOrgAddressId = inv_line_b.get("ShippingOrgAddressId") == null ? null : inv_line_b.get("ShippingOrgAddressId").getTextValue();
					Integer line_Count = inv_line_b.get("Count") == null ? null : Integer.parseInt(inv_line_b.get("Count").getTextValue());
					String IsTimeLimit = inv_line_b.get("IsTimeLimit") == null ? null : inv_line_b.get("IsTimeLimit").getTextValue();
					UFDateTime LimitTime = inv_line_b.get("LimitTime") == null ? null : new UFDateTime(inv_line_b.get("LimitTime").getTextValue());
					String CannotInFactoryByCargo = inv_line_b.get("CannotInFactoryByCargo") == null ? null : inv_line_b.get("CannotInFactoryByCargo").getTextValue();
					String NeedPaper = inv_line_b.get("NeedPaper") == null ? null : inv_line_b.get("NeedPaper").getTextValue();
					String SupervisedWarehouse = inv_line_b.get("SupervisedWarehouse") == null ? null : inv_line_b.get("SupervisedWarehouse").getTextValue();
					
					InvLineBVO invLineBVO = new InvLineBVO();
					invLineBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invLineBVO);
					invLineBVO.setPk_invoice(invoiceVO.getPk_invoice());
					invLineBVO.setOperate_type(Type + 1);
					invLineBVO.setPk_address(ShippingOrgAddressId);
					AddressVO address = NWDao.getInstance().queryByCondition(AddressVO.class, "def1=?", PickupAddressId);
					if(address == null){
						retMap.put("orderNo", OrderNo);
						retMap.put("invoiceNo", InvoiceNo);
						retMap.put("success", FALSE);
						retMap.put("errormessage", "ShippingOrgAddressId不存在：" + ShippingOrgAddressId);
						retMap.put("responseDate", dateStr);
						jdResult.add(retMap);
						exit = true;
						break;
					}else{
						invLineBVO.setPk_address(delivery.getPk_address());
						invLineBVO.setPk_area(delivery.getPk_area());
						invLineBVO.setPk_city(delivery.getPk_city());
						invLineBVO.setPk_province(delivery.getPk_province());
						invLineBVO.setDetail_addr(delivery.getDetail_addr());
						invLineBVO.setContact(delivery.getContact());
						invLineBVO.setEmail(delivery.getEmail());
						invLineBVO.setMobile(delivery.getMobile());
						invLineBVO.setPhone(delivery.getPhone());
					}
					invLineBVO.setReq_date_from(LimitTime);
					invLineBVO.setReq_date_till(LimitTime);
					invLineBVO.setNum(line_Count);
					invLineBVO.setDef1(IsTimeLimit);
					invLineBVO.setDef2(CannotInFactoryByCargo);
					invLineBVO.setDef3(NeedPaper);
					invLineBVO.setDef4(SupervisedWarehouse);
					invLineBVO.setSerialno((index + 1)*10);
					invLineBVOs[index] = invLineBVO;
					index ++;
				}
			}
			
//			JsonNode inv_trans_bs = order.get("inv_trans_b");
//			TransBilityBVO[] transBilityBVOs = null;
//			if(inv_trans_bs != null && inv_trans_bs.size() > 0){
//				transBilityBVOs = new TransBilityBVO[inv_trans_bs.size()];
//				int index = 0;
//				for(JsonNode inv_trans_b : inv_trans_bs){
//					String CarType = inv_trans_b.get("CarType") == null ? null : inv_trans_b.get("CarType").getTextValue();
//					Integer CarCount = inv_trans_b.get("CarCount") == null ? 0 : inv_trans_b.get("CarCount").getIntValue();
//					String CarSize = inv_trans_b.get("CarSize") == null ? null : inv_trans_b.get("CarSize").getTextValue();
//					
//					TransBilityBVO transBilityBVO = new TransBilityBVO();
//					transBilityBVO.setStatus(VOStatus.NEW);
//					NWDao.setUuidPrimaryKey(transBilityBVO);
//					transBilityBVO.setPk_invoice(invoiceVO.getPk_invoice());
//					CarTypeVO carTypeVO = NWDao.getInstance().queryByCondition(CarTypeVO.class, "def1=?", CarType);
//					if(carTypeVO == null){
//						retMap.put("orderNo", OrderNo);
//						retMap.put("invoiceNo", InvoiceNo);
//						retMap.put("success", FALSE);
//						retMap.put("errormessage", "CarType不存在：" + CarType);
//						retMap.put("responseDate", dateStr);
//						jdResult.add(retMap);
//						exit = true;
//						break;
//					}else{
//						transBilityBVO.setPk_car_type(carTypeVO.getPk_car_type());
//					}
//
//					transBilityBVO.setNum(CarCount);
//					transBilityBVO.setDef2(CarSize);
//					transBilityBVOs[index] = transBilityBVO;
//					index ++;
//				}
//			}
			if (exit) {
				continue;
			}
			ExAggInvoiceVO aggInvoiceVO = new ExAggInvoiceVO();
			aggInvoiceVO.setParentVO(invoiceVO);
			if(invLineBVOs != null && invLineBVOs.length != 2){
				aggInvoiceVO.setTableVO(TabcodeConst.TS_INV_LINE_B, invLineBVOs);
			}else{
				aggInvoiceVO.setTableVO(TabcodeConst.TS_INV_PACK_B, invPackBVOs);
			}
//			aggInvoiceVO.setTableVO(TabcodeConst.TS_TRANS_BILITY_B, transBilityBVOs);
			try {
				service.save(aggInvoiceVO, paramVO);
			} catch (Exception e) {
				retMap.put("orderNo", OrderNo);
				retMap.put("invoiceNo", InvoiceNo);
				retMap.put("success", FALSE);
				retMap.put("errormessage", "订单不允许保存，SubServiceId：" + SubServiceId + "错误:" + e.getMessage());
				retMap.put("responseDate", dateStr);
				jdResult.add(retMap);
				
				if(invoiceVO.getStatus() == VOStatus.UPDATED){
					String sql = "UPDATE ts_invoice SET memo ='订单修改失败，待修改的件重体 , num_count:"+Count+",weight_count:"+Weight+",volume_count:"+Volume+"' WHERE pk_invoice =?";
					NWDao.getInstance().update(sql, invoiceVO.getPk_invoice());
				}
				continue;
			}
			if(toBeDelete.size() > 0){
				NWDao.getInstance().delete(toBeDelete);//删除旧的明细
			}
			retMap.put("orderNo", OrderNo);
			retMap.put("invoiceNo", InvoiceNo);
			retMap.put("success", TRUE);
			retMap.put("errormessage", "");
			retMap.put("responseDate", dateStr);
			jdResult.add(retMap);
		}
		return JacksonUtils.writeValueAsString(jdResult);
		
	}


	//导入前的检查
	private String checkBeforeImport(String uid,String pwd) throws ApiException {
		AuthenticationService authenticationService = SpringContextHolder.getBean("authenticationService");
		if (authenticationService == null) {
			return "验证服务没有启动，服务ID:AuthenticationService";
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
	
	
	
}
