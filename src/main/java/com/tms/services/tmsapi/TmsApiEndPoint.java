package com.tms.services.tmsapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.codehaus.jackson.JsonNode;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nw.json.JacksonUtils;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.SegmentConst;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.service.tp.PZService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.route.CoordinatesVO;
import com.tms.vo.route.VehicleTripsBVO;
import com.tms.vo.route.VehicleTripsVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.ShipMentVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;


@SuppressWarnings("deprecation")
public class TmsApiEndPoint extends ServletEndpointSupport{
	
	 private static String  EDI_USER_CODE = "EdiUser";
	 private static String  VIRTUAL_CARRIER_CODE = "ORD";
	
	 private static	String ediUserSql = "SELECT * FROM nw_user WHERE isnull(dr,0) = 0 AND isnull(locked_flag,'N')='N' AND user_code = ?";
	 private static	UserVO ediUserVO = NWDao.getInstance().queryForObject(ediUserSql, UserVO.class, EDI_USER_CODE);
	
	 private  PZService pZService;
	 
	/**
	 * 导入车辆信息
	 * 
	 * @param jsonVechicleTrips 车辆信息的json
	 * @return
	 */
	@ResponseBody
	public String ImportVehicleTrips(String jsonVechicleTrips) {
		String strReturnMsg = "";
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		//空值判断
		if(jsonVechicleTrips.isEmpty()){
			strReturnMsg = "未提供任何数据，请确认！";
			return strReturnMsg;
		}
		
		JsonNode vehicles = JacksonUtils.readTree(jsonVechicleTrips);
		//判断参数有效性
		if(vehicles  == null){
			strReturnMsg = "输入的参数不是要求的格式，请修改！";
			return strReturnMsg;
		}
		List<String> ids = new ArrayList<String>();
		for(JsonNode vehicle :vehicles ){
			
			//判断主表信息
			strReturnMsg = CheckVehicleInfo(vehicle);
			if(!strReturnMsg.isEmpty()){
			   return strReturnMsg;
			}
			String id = vehicle.get("id").getTextValue();
			String resource_id = vehicle.get("resource_id").getTextValue();
			String start_date = vehicle.get("start_date").getTextValue();
			String end_date = vehicle.get("end_date").getTextValue();
			double total_mileage = vehicle.get("total_mileage") == null ? null : vehicle.get("total_mileage").getValueAsDouble();
			double total_time_consuming = vehicle.get("total_time_consuming") == null ? null : vehicle.get("total_time_consuming").getValueAsDouble();
			double total_transportation_time = vehicle.get("total_transportation_time") == null ? null : vehicle.get("total_transportation_time").getValueAsDouble();
			double total_rest = vehicle.get("total_rest") == null ? null : vehicle.get("total_rest").getValueAsDouble();
			double total_loading = vehicle.get("total_loading") == null ? null : vehicle.get("total_loading").getValueAsDouble();
			Integer total_points = vehicle.get("total_points").getValueAsInt();
			Integer status = vehicle.get("status").getValueAsInt();
			String memo =vehicle.get("memo") == null ? null : vehicle.get("memo").getTextValue();
			
			VehicleTripsVO vehicleTripsVO = new VehicleTripsVO();
			vehicleTripsVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(vehicleTripsVO);
			vehicleTripsVO.setId(id);
			vehicleTripsVO.setResource_id(resource_id);
			vehicleTripsVO.setStart_date(new UFDateTime(start_date));
			vehicleTripsVO.setEnd_date(new UFDateTime(end_date));
			vehicleTripsVO.setTotal_mileage(new UFDouble(total_mileage));
			vehicleTripsVO.setTotal_time_consuming(new UFDouble(total_time_consuming));
			vehicleTripsVO.setTotal_transportation_time(new UFDouble(total_transportation_time));
			vehicleTripsVO.setTotal_rest(new UFDouble(total_rest));
			vehicleTripsVO.setTotal_loading(new UFDouble(total_loading));
			vehicleTripsVO.setTotal_points(total_points);
			vehicleTripsVO.setVbillstatus(status);
			vehicleTripsVO.setMemo(memo);
			vehicleTripsVO.setCreate_time(new UFDateTime(new Date()));
			vehicleTripsVO.setCreate_user(ediUserVO.getPk_user());
			vehicleTripsVO.setPk_corp(ediUserVO.getPk_corp());
			ids.add(id);
			toBeUpdate.add(vehicleTripsVO);
			
			JsonNode trips = vehicle.get("trip");
			for(JsonNode trip : trips){
				//判断子表信息
				if(strReturnMsg.isEmpty()){
					strReturnMsg = CheckTripInfo(trip);
				}
				else{
					continue;
				}
				
				String action_id = trip.get("action_id").getTextValue();
				String action_kind = trip.get("action_kind").getTextValue();
				String start_address = trip.get("start_address").getTextValue();
				String end_address = trip.get("end_address").getTextValue();
				String action_start_date = trip.get("action_start_date").getTextValue();
				String action_end_date = trip.get("action_end_date").getTextValue();
				double mileage =trip.get("mileage") == null? null : trip.get("mileage").getValueAsDouble();
				String pk_segment = trip.get("pk_segment") == null ? null : trip.get("pk_segment").getTextValue();
				String orderno = trip.get("orderno") == null ? null : trip.get("orderno").getTextValue();
				String ord_orderno = trip.get("ord_orderno") == null ? null : trip.get("ord_orderno").getTextValue();
				
				VehicleTripsBVO vehicleTripsBVO = new VehicleTripsBVO();
				vehicleTripsBVO.setStatus(VOStatus.NEW);
				vehicleTripsBVO.setPk_vehicle_trips(vehicleTripsVO.getPk_vehicle_trips());
				NWDao.setUuidPrimaryKey(vehicleTripsBVO);
				vehicleTripsBVO.setAction_id(action_id);
				vehicleTripsBVO.setAction_kind(action_kind);
				vehicleTripsBVO.setStart_address(start_address);
				vehicleTripsBVO.setEnd_address(end_address);
				vehicleTripsBVO.setAction_start_date(new UFDateTime(action_start_date));
				vehicleTripsBVO.setAction_end_date(new UFDateTime(action_end_date));
				vehicleTripsBVO.setMileage(new UFDouble(mileage));
				vehicleTripsBVO.setPk_segment(pk_segment);
				vehicleTripsBVO.setOrderno(orderno);
				vehicleTripsBVO.setOrd_orderno(ord_orderno);
				
				toBeUpdate.add(vehicleTripsBVO);
			}
			
			if(!strReturnMsg.isEmpty()){
				return strReturnMsg;
			}
		}
	
		if(ids.size() > 0){
			String vehicleCond = NWUtils.buildConditionString(ids.toArray(new String[ids.size()]));
			VehicleTripsVO[] oldVehicleTripsVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(VehicleTripsVO.class, " id in " + vehicleCond);
			if(oldVehicleTripsVOs != null && oldVehicleTripsVOs.length > 0){
				String[] pkVehicleTrips = new String[oldVehicleTripsVOs.length];
				for(int i=0;i<oldVehicleTripsVOs.length;i++){
					oldVehicleTripsVOs[i].setStatus(VOStatus.DELETED);
					toBeUpdate.add(oldVehicleTripsVOs[i]);
					pkVehicleTrips[i] = oldVehicleTripsVOs[i].getPk_vehicle_trips();
				}
				String tripsCond = NWUtils.buildConditionString(pkVehicleTrips);
				VehicleTripsBVO[] vehicleTripsBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(VehicleTripsBVO.class, " pk_vehicle_trips in " + tripsCond);
				if(vehicleTripsBVOs != null && vehicleTripsBVOs.length > 0){
					for(VehicleTripsBVO vehicleTripsBVO : vehicleTripsBVOs){
						vehicleTripsBVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(vehicleTripsBVO);
					}
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		strReturnMsg = "导入车辆信息成功！";
		return strReturnMsg;
	}
	
	public String CheckVehicleInfo(JsonNode vehicle)
	{
		String strReturnMsg = "";
		
		JsonNode id = vehicle.get("id");
		JsonNode resource_id = vehicle.get("resource_id");
		JsonNode start_date = vehicle.get("start_date");
		JsonNode end_date = vehicle.get("end_date");
		JsonNode total_mileage = vehicle.get("total_mileage");
		JsonNode total_time_consuming = vehicle.get("total_time_consuming");
		JsonNode total_transportation_time = vehicle.get("total_transportation_time");
		JsonNode total_rest = vehicle.get("total_rest");
		JsonNode total_wait = vehicle.get("total_wait");
		JsonNode total_loading = vehicle.get("total_loading");
		JsonNode total_points = vehicle.get("total_points");
		JsonNode memo = vehicle.get("memo");
		JsonNode status = vehicle.get("status");
		
		if(id == null || resource_id == null|| start_date == null || end_date == null
				|| total_mileage == null || total_time_consuming == null
				|| total_transportation_time == null||total_rest == null|| memo == null
				|| total_wait == null||total_loading == null||total_points == null||status == null){
			strReturnMsg = "主表结构不正确，请确认！";
		}
		
		String strId = vehicle.get("id").getTextValue();
		String strResource_id = vehicle.get("resource_id").getTextValue();
		String strStart_date = vehicle.get("start_date").getTextValue();
		String strEnd_date = vehicle.get("end_date").getTextValue();
		String strTotal_points = vehicle.get("total_points").getTextValue();
		String strStatus = vehicle.get("total_mileage").getTextValue();
	
		if(strId.isEmpty() ||strResource_id.isEmpty()
				||strStart_date.isEmpty()||strEnd_date.isEmpty()
				||strTotal_points.isEmpty()||strStatus.isEmpty()){
					strReturnMsg = "主表 Vehicle必填项为空，请确认！";
				}
		
		return strReturnMsg;
	}
	
	public String CheckTripInfo(JsonNode trip)
	{
		String strReturnMsg = "";
		
		JsonNode node_action_id = trip.get("action_id");
		JsonNode node_action_kind = trip.get("action_kind");
		JsonNode node_start_address = trip.get("start_address");
		JsonNode node_end_address = trip.get("end_address");
		JsonNode node_action_start_date = trip.get("action_start_date");
		JsonNode node_action_end_date = trip.get("action_end_date");
		JsonNode node_mileage = trip.get("mileage");
//		JsonNode node_pk_segment = trip.get("pk_segment");
//		JsonNode node_orderno = trip.get("orderno");
//		JsonNode node_ord_orderno = trip.get("ord_orderno");
		
		
		if(node_action_id == null ||node_action_kind == null
				||node_start_address == null||node_end_address == null
				||node_action_start_date == null||node_action_end_date == null
				||node_mileage == null
//				||node_pk_segment == null
//				||node_orderno == null||node_ord_orderno == null
				){
			strReturnMsg = "子表结构不正确，请确认！";
				}
		
		String action_id = trip.get("action_id").getTextValue();
		String action_kind = trip.get("action_kind").getTextValue();
		String action_start_address = trip.get("start_address").getTextValue();
		String action_end_address = trip.get("end_address").getTextValue();
		String action_start_date = trip.get("action_start_date").getTextValue();
//		String action_end_date = trip.get("action_end_date").getTextValue();
//		String pk_segment = trip.get("pk_segment").getTextValue();
//		String ord_orderno = trip.get("ord_orderno").getTextValue();
		
		if(action_id.isEmpty() ||action_kind.isEmpty() 
				||action_start_address.isEmpty()||action_end_address.isEmpty() 
				||action_start_date.isEmpty()
//				||action_end_date.isEmpty()
//				||pk_segment.isEmpty()||ord_orderno.isEmpty()
				){
			strReturnMsg = "子表 Trip必填项为空，请确认！";
		}

		return strReturnMsg;
	}
	
	
	public String CheckShipmentInfo(JsonNode shipments){
		String strReturnMsg = "";
		List<String> seg_vbillnos = new ArrayList<String>();
		for(JsonNode shipment :shipments ){
			JsonNode ord_lot = shipment.get("id");//批次号
			JsonNode car_type = shipment.get("car_type");//这个批次应该使用的车型
			JsonNode seg_vbillno = shipment.get("pk_segment");//系统原运段号
			JsonNode ord_orderno = shipment.get("ord_orderno");//拆分后的运段号
			JsonNode pk_delivery = shipment.get("pk_delivery");//提货方code
			JsonNode pk_arrival = shipment.get("pk_arrival");//到货方code
			JsonNode req_deli_date = shipment.get("req_deli_date");//提货要求到达时间
			JsonNode req_deli_time = shipment.get("req_deli_time");//提货要求离开时间
			JsonNode req_arri_date = shipment.get("req_arri_date");//到货要求到达时间
			JsonNode req_arri_time = shipment.get("req_arri_time");//到货要求离开时间
			JsonNode num_count = shipment.get("num_count");//托数
			
			JsonNode pack_num_count = shipment.get("pack_num_count");//件
			JsonNode weight_count = shipment.get("weight_count");//重
			JsonNode volume_count = shipment.get("volume_count");//体
			
			
			JsonNode seg_id = shipment.get("seg_id");//拆段，0表示不拆段，1表示第一段，2表示第二段，以此类推
			JsonNode split = shipment.get("split");//是否分量，0表示没有分量，1表示分量
			
			if(ord_lot == null || car_type == null || seg_vbillno == null || ord_orderno == null 
					|| pk_delivery == null || pk_arrival == null || req_deli_date == null 
					|| req_deli_time == null || req_arri_date == null || req_arri_time == null 
					|| num_count == null || pack_num_count == null || weight_count == null || volume_count == null
					|| seg_id == null || split == null){
				strReturnMsg = "数据结构不正确，请确认！";
			}
			
			seg_vbillnos.add(shipment.get("pk_segment").getTextValue());
		}
		SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				" vbillno in " + NWUtils.buildConditionString(seg_vbillnos.toArray(new String[seg_vbillnos.size()])));
		if(segmentVOs != null && segmentVOs.length > 0){
			String msg = "";
			for(String seg_vbillno : seg_vbillnos){
				boolean flag = true;
				for(SegmentVO segmentVO : segmentVOs){
					if(segmentVO.getVbillno().equals(seg_vbillno)
							&& (segmentVO.getVbillstatus() == BillStatus.NEW 
							|| segmentVO.getVbillstatus() == BillStatus.SEG_WPLAN
							|| segmentVO.getVbillstatus() == BillStatus.SEG_DISPATCH)){
						//这个运段在系统是存在且正常的。
						flag = false;
					}
				}
				if(flag){
					//这个运段在系统没有了。
					msg = msg + seg_vbillno +",";
				}
			}
			if(StringUtils.isNotBlank(msg)){
				strReturnMsg = "ID:"+ msg.substring(0, msg.length()-1) +"已被删除或已提货!";
			}
		}else{
			String msg = "";
			for(String seg_vbillno : seg_vbillnos){
				msg = msg + seg_vbillno +",";
			}
			strReturnMsg = "ID:"+ msg.substring(0, msg.length()-1) +"已被删除!";
		}
		return strReturnMsg;
	}
	
	public String ImportShipment(String jsonShipments) {
		String strReturnMsg = "";

		pZService = SpringContextHolder.getBean("PZServiceImpl");
		if (pZService == null) {
			strReturnMsg = "应付明细服务没有启动，服务ID：PZServiceImpl";
			return strReturnMsg = transToXML(strReturnMsg, false);
		}

		// 空值判断
		if (jsonShipments.isEmpty()) {
			strReturnMsg = "未提供任何数据，请确认！";
			return strReturnMsg = transToXML(strReturnMsg, false);
		}

		JsonNode shipments = JacksonUtils.readTree(jsonShipments);
		// 判断参数有效性
		if (shipments == null || shipments.size() == 0) {
			strReturnMsg = "输入的参数不是要求的格式，请修改！";
			return strReturnMsg = transToXML(strReturnMsg, false);
		}

		strReturnMsg = CheckShipmentInfo(shipments);
		if (StringUtils.isNotBlank(strReturnMsg)) {
			return strReturnMsg = transToXML(strReturnMsg, false);
		}

		List<ShipMentVO> shipMentVOs = new ArrayList<ShipMentVO>();
		List<String> seg_vbillnos = new ArrayList<String>();
		List<String> ord_lots = new ArrayList<String>();
		List<String> addr_codes = new ArrayList<String>();
		List<String> car_types = new ArrayList<String>();
		for (JsonNode shipment : shipments) {
			String ord_lot = shipment.get("id").getTextValue();// 批次号
			String car_type = shipment.get("car_type").getTextValue();// 车型
			String seg_vbillno = shipment.get("pk_segment").getTextValue();// 系统原运段号
			String orderno = shipment.get("orderno") == null ? null : shipment.get("orderno").getTextValue();// 客户订单号
			String ord_orderno = shipment.get("ord_orderno").getTextValue();// 拆分后的运段号
			String pk_delivery = shipment.get("pk_delivery").getTextValue();// 提货方code
			String pk_arrival = shipment.get("pk_arrival").getTextValue();// 到货方code
			UFDateTime req_deli_date = new UFDateTime(shipment.get("req_deli_date").getTextValue());// 提货要求到达时间
			UFDateTime req_deli_time = new UFDateTime(shipment.get("req_deli_time").getTextValue());// 提货要求离开时间
			UFDateTime req_arri_date = new UFDateTime(shipment.get("req_arri_date").getTextValue());// 到货要求到达时间
			UFDateTime req_arri_time = new UFDateTime(shipment.get("req_arri_time").getTextValue());// 到货要求离开时间
			Integer num_count = shipment.get("num_count").getValueAsInt();// 托数

			UFDouble pack_num_count = new UFDouble(shipment.get("pack_num_count").getValueAsDouble());// 件
			UFDouble weight_count = new UFDouble(shipment.get("weight_count").getValueAsDouble());// 重
			UFDouble volume_count = new UFDouble(shipment.get("volume_count").getValueAsDouble());// 体

			String memo = shipment.get("memo") == null ? null : shipment.get("memo").getTextValue();// 备注

			Integer seg_id = shipment.get("seg_id").getValueAsInt();// 拆段，0表示不拆段，1表示第一段，2表示第二段，以此类推
			Integer split = shipment.get("split").getValueAsInt();// 是否分量，0表示没有分量，1表示分量

			ShipMentVO shipMentVO = new ShipMentVO();
			shipMentVO.setOrd_lot(ord_lot);
			shipMentVO.setCar_type(car_type);
			shipMentVO.setSeg_vbillno(seg_vbillno);
			shipMentVO.setOrderno(orderno);
			shipMentVO.setOrd_orderno(ord_orderno);
			shipMentVO.setPk_delivery(pk_delivery);
			shipMentVO.setPk_arrival(pk_arrival);
			shipMentVO.setReq_arri_date(req_arri_date);
			shipMentVO.setReq_arri_time(req_arri_time);
			shipMentVO.setReq_deli_date(req_deli_date);
			shipMentVO.setReq_deli_time(req_deli_time);
			shipMentVO.setNum_count(num_count);

			shipMentVO.setPack_num_count(pack_num_count);
			shipMentVO.setWeight_count(weight_count);
			shipMentVO.setVolume_count(volume_count);
			shipMentVO.setMemo(memo);
			shipMentVO.setSeg_id(seg_id);
			shipMentVO.setSplit(split);

			shipMentVOs.add(shipMentVO);
			ord_lots.add(ord_lot);
			seg_vbillnos.add(seg_vbillno);
			addr_codes.add(pk_delivery);
			addr_codes.add(pk_arrival);
			car_types.add(car_type);

		}

		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class,
				" addr_code in " + NWUtils.buildConditionString(addr_codes.toArray(new String[addr_codes.size()])));

		SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				" vbillno in " + NWUtils.buildConditionString(seg_vbillnos.toArray(new String[seg_vbillnos.size()])));

		CarTypeVO[] carTypeVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarTypeVO.class,
				" code in " + NWUtils.buildConditionString(car_types.toArray(new String[car_types.size()])));

		String segPackBSql = "SELECT * FROM ts_seg_pack_b WITH(nolock) "
				+ " LEFT JOIN ts_segment WITH(nolock)  ON ts_seg_pack_b.pk_segment = ts_segment.pk_segment "
				+ " WHERE isnull(ts_seg_pack_b.dr,0) = 0 AND  isnull(ts_segment.dr,0) = 0 "
				+ "AND  ts_segment.vbillno IN "
				+ NWUtils.buildConditionString(seg_vbillnos.toArray(new String[seg_vbillnos.size()]));
		List<SegPackBVO> segPackBVOs = NWDao.getInstance().queryForList(segPackBSql, SegPackBVO.class);

		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<SegmentVO> toBePZ = new ArrayList<SegmentVO>();
		List<SegmentVO> toBeHidden = new ArrayList<SegmentVO>();
		List<SegPackBVO> toBeCreate = new ArrayList<SegPackBVO>();

		// 对shipMentVO 按照原有订单号进行分组，分组后的订单属于同一运段；
		Map<String, List<ShipMentVO>> groupMap = new HashMap<String, List<ShipMentVO>>();
		for (ShipMentVO shipMentVO : shipMentVOs) {
			String key = shipMentVO.getSeg_vbillno();
			List<ShipMentVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<ShipMentVO>();
				groupMap.put(key, voList);
			}
			voList.add(shipMentVO);
		}

		boolean updateFlag = false;
		for (String key : groupMap.keySet()) {
			// 一组信息
			List<ShipMentVO> voList = groupMap.get(key);
			for (SegmentVO segmentVO : segmentVOs) {
				if (segmentVO.getVbillno().equals(voList.get(0).getSeg_vbillno())) {
					if (segmentVO.getSeg_mark() == SegmentConst.SEG_MARK_PARENT
							|| segmentVO.getVbillstatus() == BillStatus.SEG_DISPATCH) {
						// 已被配载的或者成为父级运段的
						if (voList != null && voList.size() > 0) {
							syncByUpdate(voList, addressVOs, segmentVO,carTypeVOs);
							updateFlag = true;
						}
					} else {
						// 只有一个运段，而且分段标志和分量标志都没有，说明这个运段既没有被分段，也没有被分量
						if (voList != null && voList.size() == 1 && voList.get(0).getSeg_id() == 0
								&& voList.get(0).getSplit() == 0) {
							segmentVO.setSeg_mark(SegmentConst.SEG_MARK_NORMAL);
							segmentVO.setSeg_type(SegmentConst.SEG_TYPE_NORMAL);
							segmentVO.setOrd_lot(voList.get(0).getOrd_lot());
							segmentVO.setOrd_orderno(voList.get(0).getOrd_orderno());
							toBePZ.add(segmentVO);
						} else {
							if (voList.size() > 1 || voList.get(0).getSeg_id() != 0 || voList.get(0).getSplit() != 0) {
								// 被分段或者分量了。
								// 只要其中一段的分段标志不是0，说明这个运段被分段了。
								segmentVO.setSeg_mark(SegmentConst.SEG_MARK_PARENT);
								segmentVO.setStatus(VOStatus.UPDATED);
								toBeHidden.add(segmentVO);

								// 对这些信息按照分段标志分组
								Map<Integer, List<ShipMentVO>> sameSectionGroupMap = new HashMap<Integer, List<ShipMentVO>>();
								for (ShipMentVO shipMentVO : voList) {
									Integer seg_id = shipMentVO.getSeg_id();
									List<ShipMentVO> sameSectionVOList = sameSectionGroupMap.get(seg_id);
									if (sameSectionVOList == null) {
										sameSectionVOList = new ArrayList<ShipMentVO>();
										sameSectionGroupMap.put(seg_id, sameSectionVOList);
									}
									sameSectionVOList.add(shipMentVO);
								}
								for (Integer seg_id : sameSectionGroupMap.keySet()) {
									List<ShipMentVO> sameSectionVOList = sameSectionGroupMap.get(seg_id);
									if (sameSectionVOList != null && sameSectionVOList.size() > 0) {
										if (sameSectionVOList.get(0).getSeg_id() == 0) {
											// 这个运段只被分量，没有被分段
											int index = 0;
											for (ShipMentVO shipMentVO : sameSectionVOList) {
												SegmentVO segVO = new SegmentVO();
												segVO.setStatus(VOStatus.NEW);
												segVO.setParent_seg(segmentVO.getPk_segment());
												NWDao.setUuidPrimaryKey(segVO);
												index++;
												if (index < 10) {
													segVO.setVbillno(segmentVO.getVbillno() + "-00" + index);
												} else if (index < 100) {
													segVO.setVbillno(segmentVO.getVbillno() + "-0" + index);
												} else {
													segVO.setVbillno(segmentVO.getVbillno() + "-" + index);
												}
												segVO.setSeg_type(SegmentConst.QUANTITY);
												segVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD);
												segVO.setInvoice_vbillno(segmentVO.getInvoice_vbillno());
												segVO.setVbillstatus(BillStatus.SEG_WPLAN);
												segVO.setOrd_lot(shipMentVO.getOrd_lot());
												segVO.setReq_arri_date(shipMentVO.getReq_arri_date().toString());
												segVO.setReq_arri_time(shipMentVO.getReq_arri_time().toString());
												segVO.setReq_deli_date(shipMentVO.getReq_deli_date().toString());
												segVO.setReq_deli_time(shipMentVO.getReq_deli_time().toString());
												segVO.setMemo(shipMentVO.getMemo());
												for (AddressVO addressVO : addressVOs) {
													if (addressVO.getAddr_code().equals(shipMentVO.getPk_arrival())) {
														segVO.setPk_arrival(addressVO.getPk_address());
														segVO.setArri_city(addressVO.getPk_city());
														segVO.setArri_province(addressVO.getPk_province());
														segVO.setArri_area(addressVO.getPk_area());
														segVO.setArri_detail_addr(addressVO.getDetail_addr());
														break;
													}
												}
												for (AddressVO addressVO : addressVOs) {
													if (addressVO.getAddr_code().equals(shipMentVO.getPk_delivery())) {
														segVO.setPk_delivery(addressVO.getPk_address());
														segVO.setDeli_city(addressVO.getPk_city());
														segVO.setDeli_province(addressVO.getPk_province());
														segVO.setDeli_area(addressVO.getPk_area());
														segVO.setDeli_detail_addr(addressVO.getDetail_addr());
														break;
													}
												}
												segVO.setNum_count(shipMentVO.getNum_count());
												segVO.setWeight_count(shipMentVO.getWeight_count());
												segVO.setVolume_count(shipMentVO.getVolume_count());
												segVO.setPack_num_count(shipMentVO.getPack_num_count());
												segVO.setCreate_user(ediUserVO.getPk_user());
												segVO.setCreate_time(new UFDateTime(new Date()));
												segVO.setPk_corp(segmentVO.getPk_corp());
												segVO.setPk_trans_type(segmentVO.getPk_trans_type());
												segVO.setFee_weight_count(shipMentVO.getWeight_count());
												segVO.setVolume_weight_count(shipMentVO.getVolume_count());
												segVO.setDeli_method(segmentVO.getDeli_method());
												segVO.setDbilldate(segmentVO.getDbilldate());
												segVO.setDelegate_status(segmentVO.getDelegate_status());
												segVO.setDelegate_corp(segmentVO.getDelegate_corp());
												segVO.setDelegate_user(segmentVO.getDelegate_user());
												segVO.setDelegate_time(segmentVO.getDelegate_time());
												segVO.setPk_driver(segmentVO.getPk_driver());
												segVO.setCarno(segmentVO.getCarno());
												segVO.setPk_carrier(segmentVO.getPk_carrier());
												segVO.setIf_vent(segmentVO.getIf_vent());
												segVO.setVent_time(segmentVO.getVent_time());
												segVO.setVent_user(segmentVO.getVent_user());
												segVO.setVent_reason(segmentVO.getVent_reason());
												segVO.setVent_type(segmentVO.getVent_type());
												segVO.setOrd_orderno(shipMentVO.getOrd_orderno());
												toBePZ.add(segVO);

												// 处理子表信息
												for (SegPackBVO packBVO : segPackBVOs) {
													if (packBVO.getPk_segment().equals(segmentVO.getPk_segment())) {
														SegPackBVO newSegPackBVO = packBVO.clone();
														newSegPackBVO.setStatus(VOStatus.NEW);
														newSegPackBVO.setPk_seg_pack_b(null);
														newSegPackBVO.setWeight(segVO.getWeight_count());
														newSegPackBVO.setVolume(segVO.getVolume_count());
														newSegPackBVO.setPack_num_count(segVO.getPack_num_count());
														newSegPackBVO.setNum(segVO.getNum_count());
														NWDao.setUuidPrimaryKey(newSegPackBVO);
														newSegPackBVO.setPk_segment(segVO.getPk_segment());
														toBeCreate.add(newSegPackBVO);
														// 只会有一个明细
														break;
													}
												}
											}
										} else if (sameSectionVOList.size() == 1
												&& sameSectionVOList.get(0).getSplit() == 0) {
											// 这个运段只被分段，没有被分量
											SegmentVO segVO = new SegmentVO();
											segVO.setStatus(VOStatus.NEW);
											segVO.setParent_seg(segmentVO.getPk_segment());
											NWDao.setUuidPrimaryKey(segVO);
											segVO.setVbillno(segmentVO.getVbillno() + "-"
													+ sameSectionVOList.get(0).getSeg_id());
											segVO.setSeg_type(SegmentConst.SECTION);
											segVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD);
											segVO.setInvoice_vbillno(segmentVO.getInvoice_vbillno());
											segVO.setVbillstatus(BillStatus.SEG_WPLAN);
											segVO.setOrd_lot(sameSectionVOList.get(0).getOrd_lot());
											segVO.setReq_arri_date(
													sameSectionVOList.get(0).getReq_arri_date().toString());
											segVO.setReq_arri_time(
													sameSectionVOList.get(0).getReq_arri_time().toString());
											segVO.setReq_deli_date(
													sameSectionVOList.get(0).getReq_deli_date().toString());
											segVO.setReq_deli_time(
													sameSectionVOList.get(0).getReq_deli_time().toString());
											segVO.setMemo(sameSectionVOList.get(0).getMemo());
											for (AddressVO addressVO : addressVOs) {
												if (addressVO.getAddr_code()
														.equals(sameSectionVOList.get(0).getPk_arrival())) {
													segVO.setPk_arrival(addressVO.getPk_address());
													segVO.setArri_city(addressVO.getPk_city());
													segVO.setArri_province(addressVO.getPk_province());
													segVO.setArri_area(addressVO.getPk_area());
													segVO.setArri_detail_addr(addressVO.getDetail_addr());
													break;
												}
											}
											for (AddressVO addressVO : addressVOs) {
												if (addressVO.getAddr_code()
														.equals(sameSectionVOList.get(0).getPk_delivery())) {
													segVO.setPk_delivery(addressVO.getPk_address());
													segVO.setDeli_city(addressVO.getPk_city());
													segVO.setDeli_province(addressVO.getPk_province());
													segVO.setDeli_area(addressVO.getPk_area());
													segVO.setDeli_detail_addr(addressVO.getDetail_addr());
													break;
												}
											}
											// 分段时，件重体不发生变化。
											segVO.setNum_count(segmentVO.getNum_count());
											segVO.setWeight_count(segmentVO.getWeight_count());
											segVO.setVolume_count(segmentVO.getVolume_count());
											segVO.setCreate_user(ediUserVO.getPk_user());
											segVO.setCreate_time(new UFDateTime(new Date()));
											segVO.setPk_corp(segmentVO.getPk_corp());
											segVO.setPk_trans_type(segmentVO.getPk_trans_type());
											segVO.setFee_weight_count(segmentVO.getFee_weight_count());
											segVO.setVolume_weight_count(segmentVO.getVolume_weight_count());
											segVO.setDeli_method(segmentVO.getDeli_method());
											segVO.setPack_num_count(segmentVO.getPack_num_count());
											segVO.setDbilldate(segmentVO.getDbilldate());
											segVO.setDelegate_status(segmentVO.getDelegate_status());
											segVO.setDelegate_corp(segmentVO.getDelegate_corp());
											segVO.setDelegate_user(segmentVO.getDelegate_user());
											segVO.setDelegate_time(segmentVO.getDelegate_time());
											segVO.setPk_driver(segmentVO.getPk_driver());
											segVO.setCarno(segmentVO.getCarno());
											segVO.setPk_carrier(segmentVO.getPk_carrier());
											segVO.setIf_vent(segmentVO.getIf_vent());
											segVO.setVent_time(segmentVO.getVent_time());
											segVO.setVent_user(segmentVO.getVent_user());
											segVO.setVent_reason(segmentVO.getVent_reason());
											segVO.setVent_type(segmentVO.getVent_type());
											segVO.setOrd_orderno(sameSectionVOList.get(0).getOrd_orderno());
											toBePZ.add(segVO);

											// 处理子表信息
											for (SegPackBVO packBVO : segPackBVOs) {
												if (packBVO.getPk_segment().equals(segmentVO.getPk_segment())) {
													// 只分段，不分量情况下不需要处理子表件重体
													SegPackBVO newSegPackBVO = packBVO.clone();
													newSegPackBVO.setStatus(VOStatus.NEW);
													newSegPackBVO.setPk_seg_pack_b(null);
													NWDao.setUuidPrimaryKey(newSegPackBVO);
													newSegPackBVO.setPk_segment(segVO.getPk_segment());
													toBeCreate.add(newSegPackBVO);
													// 只会有一个明细
													break;
												}
											}
										} else {
											// 这个运段被分量，也被分段 先执行一次分段操作，在进行分量
											// 分段
											SegmentVO sectionSegVO = new SegmentVO();
											sectionSegVO.setStatus(VOStatus.NEW);
											sectionSegVO.setParent_seg(segmentVO.getPk_segment());
											NWDao.setUuidPrimaryKey(sectionSegVO);
											sectionSegVO.setVbillno(segmentVO.getVbillno() + "-"
													+ sameSectionVOList.get(0).getSeg_id());
											sectionSegVO.setSeg_type(SegmentConst.SECTION);
											sectionSegVO.setSeg_mark(SegmentConst.SEG_MARK_PARENT);
											sectionSegVO.setInvoice_vbillno(segmentVO.getInvoice_vbillno());
											sectionSegVO.setVbillstatus(BillStatus.SEG_WPLAN);
											sectionSegVO.setOrd_lot(sameSectionVOList.get(0).getOrd_lot());
											sectionSegVO.setReq_arri_date(
													sameSectionVOList.get(0).getReq_arri_date().toString());
											sectionSegVO.setReq_arri_time(
													sameSectionVOList.get(0).getReq_arri_time().toString());
											sectionSegVO.setReq_deli_date(
													sameSectionVOList.get(0).getReq_deli_date().toString());
											sectionSegVO.setReq_deli_time(
													sameSectionVOList.get(0).getReq_deli_time().toString());
											sectionSegVO.setMemo(sameSectionVOList.get(0).getMemo());
											for (AddressVO addressVO : addressVOs) {
												if (addressVO.getAddr_code()
														.equals(sameSectionVOList.get(0).getPk_arrival())) {
													sectionSegVO.setPk_arrival(addressVO.getPk_address());
													sectionSegVO.setArri_city(addressVO.getPk_city());
													sectionSegVO.setArri_province(addressVO.getPk_province());
													sectionSegVO.setArri_area(addressVO.getPk_area());
													sectionSegVO.setArri_detail_addr(addressVO.getDetail_addr());
													break;
												}
											}
											for (AddressVO addressVO : addressVOs) {
												if (addressVO.getAddr_code()
														.equals(sameSectionVOList.get(0).getPk_delivery())) {
													sectionSegVO.setPk_delivery(addressVO.getPk_address());
													sectionSegVO.setDeli_city(addressVO.getPk_city());
													sectionSegVO.setDeli_province(addressVO.getPk_province());
													sectionSegVO.setDeli_area(addressVO.getPk_area());
													sectionSegVO.setDeli_detail_addr(addressVO.getDetail_addr());
													break;
												}
											}
											// 分段时，件重体不发生变化。
											sectionSegVO.setNum_count(segmentVO.getNum_count());
											sectionSegVO.setWeight_count(segmentVO.getWeight_count());
											sectionSegVO.setVolume_count(segmentVO.getVolume_count());
											sectionSegVO.setCreate_user(ediUserVO.getPk_user());
											sectionSegVO.setCreate_time(new UFDateTime(new Date()));
											sectionSegVO.setPk_corp(segmentVO.getPk_corp());
											sectionSegVO.setPk_trans_type(segmentVO.getPk_trans_type());
											sectionSegVO.setFee_weight_count(segmentVO.getFee_weight_count());
											sectionSegVO.setVolume_weight_count(segmentVO.getVolume_weight_count());
											sectionSegVO.setDeli_method(segmentVO.getDeli_method());
											sectionSegVO.setPack_num_count(segmentVO.getPack_num_count());
											sectionSegVO.setDbilldate(segmentVO.getDbilldate());
											sectionSegVO.setDelegate_status(segmentVO.getDelegate_status());
											sectionSegVO.setDelegate_corp(segmentVO.getDelegate_corp());
											sectionSegVO.setDelegate_user(segmentVO.getDelegate_user());
											sectionSegVO.setDelegate_time(segmentVO.getDelegate_time());
											sectionSegVO.setPk_driver(segmentVO.getPk_driver());
											sectionSegVO.setCarno(segmentVO.getCarno());
											sectionSegVO.setPk_carrier(segmentVO.getPk_carrier());
											sectionSegVO.setIf_vent(segmentVO.getIf_vent());
											sectionSegVO.setVent_time(segmentVO.getVent_time());
											sectionSegVO.setVent_user(segmentVO.getVent_user());
											sectionSegVO.setVent_reason(segmentVO.getVent_reason());
											sectionSegVO.setVent_type(segmentVO.getVent_type());
											sectionSegVO.setOrd_orderno(sameSectionVOList.get(0).getOrd_orderno());
											toBeHidden.add(sectionSegVO);

											// 处理子表信息
											for (SegPackBVO packBVO : segPackBVOs) {
												if (packBVO.getPk_segment().equals(segmentVO.getPk_segment())) {
													// 只分段，不分量情况下不需要处理子表件重体
													SegPackBVO newSegPackBVO = packBVO.clone();
													newSegPackBVO.setStatus(VOStatus.NEW);
													newSegPackBVO.setPk_seg_pack_b(null);
													NWDao.setUuidPrimaryKey(newSegPackBVO);
													newSegPackBVO.setPk_segment(sectionSegVO.getPk_segment());
													toBeCreate.add(newSegPackBVO);
													// 只会有一个明细
													break;
												}
											}

											// 分量
											int index = 0;
											for (ShipMentVO shipMentVO : sameSectionVOList) {
												SegmentVO splitSegVO = new SegmentVO();
												splitSegVO.setStatus(VOStatus.NEW);
												splitSegVO.setParent_seg(sectionSegVO.getPk_segment());
												NWDao.setUuidPrimaryKey(splitSegVO);
												index++;
												if (index < 10) {
													splitSegVO.setVbillno(sectionSegVO.getVbillno() + "-00" + index);
												} else if (index < 100) {
													splitSegVO.setVbillno(sectionSegVO.getVbillno() + "-0" + index);
												} else {
													splitSegVO.setVbillno(sectionSegVO.getVbillno() + "-" + index);
												}
												splitSegVO.setSeg_type(SegmentConst.QUANTITY);
												splitSegVO.setSeg_mark(SegmentConst.SEG_MARK_CHILD);
												splitSegVO.setInvoice_vbillno(sectionSegVO.getInvoice_vbillno());
												splitSegVO.setVbillstatus(BillStatus.SEG_WPLAN);
												splitSegVO.setOrd_lot(shipMentVO.getOrd_lot());
												splitSegVO.setReq_arri_date(shipMentVO.getReq_arri_date().toString());
												splitSegVO.setReq_arri_time(shipMentVO.getReq_arri_time().toString());
												splitSegVO.setReq_deli_date(shipMentVO.getReq_deli_date().toString());
												splitSegVO.setReq_deli_time(shipMentVO.getReq_deli_time().toString());
												splitSegVO.setMemo(shipMentVO.getMemo());
												for (AddressVO addressVO : addressVOs) {
													if (addressVO.getAddr_code().equals(shipMentVO.getPk_arrival())) {
														splitSegVO.setPk_arrival(addressVO.getPk_address());
														splitSegVO.setArri_city(addressVO.getPk_city());
														splitSegVO.setArri_province(addressVO.getPk_province());
														splitSegVO.setArri_area(addressVO.getPk_area());
														splitSegVO.setArri_detail_addr(addressVO.getDetail_addr());
														break;
													}
												}
												for (AddressVO addressVO : addressVOs) {
													if (addressVO.getAddr_code().equals(shipMentVO.getPk_delivery())) {
														splitSegVO.setPk_delivery(addressVO.getPk_address());
														splitSegVO.setDeli_city(addressVO.getPk_city());
														splitSegVO.setDeli_province(addressVO.getPk_province());
														splitSegVO.setDeli_area(addressVO.getPk_area());
														splitSegVO.setDeli_detail_addr(addressVO.getDetail_addr());
														break;
													}
												}
												// 分量时，件重体发生变化。
												splitSegVO.setNum_count(shipMentVO.getNum_count());
												splitSegVO.setWeight_count(shipMentVO.getWeight_count());
												splitSegVO.setVolume_count(shipMentVO.getVolume_count());
												splitSegVO.setCreate_user(ediUserVO.getPk_user());
												splitSegVO.setCreate_time(new UFDateTime(new Date()));
												splitSegVO.setPk_corp(sectionSegVO.getPk_corp());
												splitSegVO.setPk_trans_type(sectionSegVO.getPk_trans_type());
												splitSegVO.setFee_weight_count(shipMentVO.getWeight_count());
												splitSegVO.setVolume_weight_count(shipMentVO.getWeight_count());
												splitSegVO.setDeli_method(sectionSegVO.getDeli_method());
												splitSegVO.setPack_num_count(shipMentVO.getPack_num_count());
												splitSegVO.setDbilldate(sectionSegVO.getDbilldate());
												splitSegVO.setDelegate_status(sectionSegVO.getDelegate_status());
												splitSegVO.setDelegate_corp(sectionSegVO.getDelegate_corp());
												splitSegVO.setDelegate_user(sectionSegVO.getDelegate_user());
												splitSegVO.setDelegate_time(sectionSegVO.getDelegate_time());
												splitSegVO.setPk_driver(sectionSegVO.getPk_driver());
												splitSegVO.setCarno(sectionSegVO.getCarno());
												splitSegVO.setPk_carrier(sectionSegVO.getPk_carrier());
												splitSegVO.setIf_vent(sectionSegVO.getIf_vent());
												splitSegVO.setVent_time(sectionSegVO.getVent_time());
												splitSegVO.setVent_user(segmentVO.getVent_user());
												splitSegVO.setVent_reason(sectionSegVO.getVent_reason());
												splitSegVO.setVent_type(sectionSegVO.getVent_type());
												splitSegVO.setOrd_orderno(shipMentVO.getOrd_orderno());
												toBePZ.add(splitSegVO);

												// 处理子表信息
												for (SegPackBVO packBVO : segPackBVOs) {
													if (packBVO.getPk_segment().equals(segmentVO.getPk_segment())) {
														// 只分段，不分量情况下不需要处理子表件重体
														SegPackBVO newSegPackBVO = packBVO.clone();
														newSegPackBVO.setStatus(VOStatus.NEW);
														newSegPackBVO.setPk_seg_pack_b(null);
														newSegPackBVO.setWeight(splitSegVO.getWeight_count());
														newSegPackBVO.setNum(splitSegVO.getNum_count());
														newSegPackBVO.setVolume(splitSegVO.getVolume_count());
														newSegPackBVO.setPack_num_count(splitSegVO.getPack_num_count());
														NWDao.setUuidPrimaryKey(newSegPackBVO);
														newSegPackBVO.setPk_segment(splitSegVO.getPk_segment());
														toBeCreate.add(newSegPackBVO);
														// 只会有一个明细
														break;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		toBeUpdate.addAll(toBeCreate);
		toBeUpdate.addAll(toBeHidden);
		toBeUpdate.addAll(toBePZ);
		// 保存运段信息
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		// 清除保存的信息，方便记载配载的信息
		toBeUpdate.clear();
		// 进行配载操作 承运商信息统一使用虚拟承运商
		CarrierVO ORDCarrVO = NWDao.getInstance().queryByCondition(CarrierVO.class, " carr_code =? ",
				VIRTUAL_CARRIER_CODE);
		// 需要将处理号的运段按照ord批次号进行分组
		if (toBePZ != null && toBePZ.size() > 0) {
			Map<String, List<SegmentVO>> sameLotSegVOGroupMap = new HashMap<String, List<SegmentVO>>();
			for (SegmentVO segmentVO : toBePZ) {
				String ord_lot = segmentVO.getOrd_lot();
				List<SegmentVO> sameLotSegVOs = sameLotSegVOGroupMap.get(ord_lot);
				if (sameLotSegVOs == null) {
					sameLotSegVOs = new ArrayList<SegmentVO>();
					sameLotSegVOGroupMap.put(ord_lot, sameLotSegVOs);
				}
				sameLotSegVOs.add(segmentVO);
			}
			for (String ord_lot : sameLotSegVOGroupMap.keySet()) {
				List<SegmentVO> sameLotSegVOs = sameLotSegVOGroupMap.get(ord_lot);
				if (sameLotSegVOs != null && sameLotSegVOs.size() > 0) {

					PZHeaderVO headerVO = new PZHeaderVO();
					headerVO.setPk_carrier(ORDCarrVO.getPk_carrier());
					headerVO.setPk_trans_type(sameLotSegVOs.get(0).getPk_trans_type());
					boolean flag = false;
					for (ShipMentVO shipMentVO : shipMentVOs) {
						if (sameLotSegVOs.get(0).getOrd_lot().equals(shipMentVO.getOrd_lot())) {
							for (CarTypeVO carTypeVO : carTypeVOs) {
								if (shipMentVO.getCar_type().equals(carTypeVO.getCode())) {
									headerVO.setPk_car_type(carTypeVO.getPk_car_type());
									flag = true;
									break;
								}
							}
						}
						if (flag) {
							break;
						}
					}

					EntLotVO entLotVO = new EntLotVO();
					EntrustVO entrustVO = new EntrustVO();
					ParamVO paramVO = new ParamVO();
					paramVO.setFunCode(FunConst.SEG_BATCH_PZ_CODE);

					String lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.BATORDERLOT);

					entLotVO.setLot(lot);
					headerVO.setLot(lot);
					headerVO.setIs_append(UFBoolean.FALSE);
					entLotVO.setDbilldate(new UFDate());
					entLotVO.setVbillstatus(BillStatus.NEW);
					entLotVO.setPk_corp(sameLotSegVOs.get(0).getPk_corp());
					entLotVO.setDef1(sameLotSegVOs.get(0).getOrd_lot());
					entLotVO.setStatus(VOStatus.NEW);
					entLotVO.setCreate_time(new UFDateTime(new Date()));
					NWDao.setUuidPrimaryKey(entLotVO);
					toBeUpdate.add(entLotVO);
					int serialno = 10;
					for (SegmentVO segVO : sameLotSegVOs) {
						ExAggEntrustVO exAggEntrustVO = pZService.doProcessPZ(headerVO, segVO, null, paramVO,serialno);
						entrustVO = (EntrustVO) exAggEntrustVO.getParentVO();
						serialno += 10;
					}
					toBeUpdate.add(entrustVO);
				}
			}
			// 调用完配载方法后就不需要再保存了。
			strReturnMsg = "导入成功";
			return transToXML(strReturnMsg, true);
		}
		if (updateFlag) {
			strReturnMsg = "导入成功";
			return transToXML(strReturnMsg, true);
		}else{
			strReturnMsg = "没有可以配载的信息！";
			return transToXML(strReturnMsg, false);
		}
		
	}
	
	private void syncByUpdate(List<ShipMentVO> shipMentVOs, AddressVO[] addressVOs, SegmentVO segmentVO,
			CarTypeVO[] carTypeVOs) {
		if (shipMentVOs == null || shipMentVOs.size() == 0) {
			return ;
		}
		// 这个运段已经被配载了，找到他所对应的委托单。
		// 这个订单是需要更新或者删除的，订单。
		List<String> ord_ordernos = new ArrayList<String>();
		List<String> ord_lots = new ArrayList<String>();
		for (ShipMentVO shipMentVO : shipMentVOs) {
			ord_ordernos.add(shipMentVO.getOrd_orderno());
			ord_lots.add(shipMentVO.getOrd_lot());
		}
		String ord_ordernoCond = NWUtils.buildConditionString(ord_ordernos.toArray(new String[ord_ordernos.size()]));
		String ord_lotCond = NWUtils.buildConditionString(ord_lots.toArray(new String[ord_lots.size()]));
		// 系统原有的运段信息。
		SegmentVO[] oldSegmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
				"ord_orderno in " + ord_ordernoCond);
		// 系统的运段包装明细
		String packBSql = " SELECT ts_seg_pack_b.* FROM ts_seg_pack_b WITH(nolock) 	"
				+ " LEFT JOIN ts_segment WITH(nolock) ON ts_segment.pk_segment = ts_seg_pack_b.pk_segment"
				+ " WHERE isnull(ts_segment.dr,0) =0 AND  isnull(ts_segment.dr,0) =0 "
				+ " AND ts_segment.ord_orderno IN " + ord_ordernoCond;
		List<SegPackBVO> oldSegPackBVOs = NWDao.getInstance().queryForList(packBSql, SegPackBVO.class);

		String entSql = " SELECT ts_entrust.* FROM ts_entrust WITH(nolock) "
				+ " LEFT JOIN ts_ent_seg_b WITH(nolock) ON ts_entrust.pk_entrust = ts_ent_seg_b.pk_entrust "
				+ " LEFT JOIN ts_segment WITH(nolock) ON ts_segment.pk_segment = ts_ent_seg_b.pk_segment "
				+ " WHERE isnull(ts_segment.dr,0) =0 AND  isnull(ts_segment.dr,0) =0 "
				+ " AND isnull(ts_ent_seg_b.dr,0) =0 AND ts_segment.ord_orderno IN " + ord_ordernoCond;
		List<EntrustVO> entrustVOs = NWDao.getInstance().queryForList(entSql, EntrustVO.class);
		EntLotVO[] entLotVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLotVO.class, "def1 in "+ ord_lotCond);
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for (ShipMentVO shipMentVO : shipMentVOs) {
			// 修改
			// 找到对应的运段，更改运段信息。
			for (SegmentVO oldSegmentVO : oldSegmentVOs) {
				if (shipMentVO.getOrd_orderno().equals(oldSegmentVO.getOrd_orderno())) {
					//如果运段一提货，则不更新。
					if(oldSegmentVO.getVbillstatus() != BillStatus.SEG_DISPATCH
							&& oldSegmentVO.getVbillstatus() != BillStatus.SEG_WPLAN){
						continue;
					}
					oldSegmentVO.setStatus(VOStatus.UPDATED);
					oldSegmentVO.setOrd_lot(shipMentVO.getOrd_lot());
					oldSegmentVO.setReq_arri_date(shipMentVO.getReq_arri_date().toString());
					oldSegmentVO.setReq_arri_time(shipMentVO.getReq_arri_time().toString());
					oldSegmentVO.setReq_deli_date(shipMentVO.getReq_deli_date().toString());
					oldSegmentVO.setReq_deli_time(shipMentVO.getReq_deli_time().toString());
					oldSegmentVO.setMemo(shipMentVO.getMemo());
					for (AddressVO addressVO : addressVOs) {
						if (addressVO.getAddr_code().equals(shipMentVO.getPk_arrival())) {
							oldSegmentVO.setPk_arrival(addressVO.getPk_address());
							oldSegmentVO.setArri_city(addressVO.getPk_city());
							oldSegmentVO.setArri_province(addressVO.getPk_province());
							oldSegmentVO.setArri_area(addressVO.getPk_area());
							oldSegmentVO.setArri_detail_addr(addressVO.getDetail_addr());
							break;
						}
					}
					for (AddressVO addressVO : addressVOs) {
						if (addressVO.getAddr_code().equals(shipMentVO.getPk_delivery())) {
							oldSegmentVO.setPk_delivery(addressVO.getPk_address());
							oldSegmentVO.setDeli_city(addressVO.getPk_city());
							oldSegmentVO.setDeli_province(addressVO.getPk_province());
							oldSegmentVO.setDeli_area(addressVO.getPk_area());
							oldSegmentVO.setDeli_detail_addr(addressVO.getDetail_addr());
							break;
						}
					}
					oldSegmentVO.setNum_count(shipMentVO.getNum_count());
					oldSegmentVO.setWeight_count(shipMentVO.getWeight_count());
					oldSegmentVO.setVolume_count(shipMentVO.getVolume_count());
					oldSegmentVO.setPack_num_count(shipMentVO.getPack_num_count());
					oldSegmentVO.setModify_user(ediUserVO.getPk_user());
					oldSegmentVO.setModify_time(new UFDateTime(new Date()));
					oldSegmentVO.setFee_weight_count(shipMentVO.getWeight_count());
					oldSegmentVO.setVolume_weight_count(shipMentVO.getVolume_count());
					oldSegmentVO.setOrd_orderno(shipMentVO.getOrd_orderno());
					toBeUpdate.add(oldSegmentVO);
					// 处理子表
					// 处理子表信息
					List<SegPackBVO> oldPackBVOs = new ArrayList<SegPackBVO>();
					for (SegPackBVO oldPackBVO : oldSegPackBVOs) {
						if (oldPackBVO.getPk_segment().equals(oldSegmentVO.getPk_segment())) {
							oldPackBVO.setStatus(VOStatus.UPDATED);
							oldPackBVO.setPk_seg_pack_b(null);
							oldPackBVO.setWeight(oldSegmentVO.getWeight_count());
							oldPackBVO.setVolume(oldSegmentVO.getVolume_count());
							oldPackBVO.setPack_num_count(oldSegmentVO.getPack_num_count());
							oldPackBVO.setNum(oldSegmentVO.getNum_count());
							oldPackBVOs.add(oldPackBVO);
							// 只会有一个明细
							break;
						}
					}
					boolean flag = true;
					if(entLotVOs != null && ord_lotCond.length() > 0){
						for(EntLotVO entLotVO : entLotVOs){
							if(entLotVO.getDef1().equals(shipMentVO.getOrd_lot())){
								// 同步委托单等信息
								InvoiceUtils.syncEntrustUpdater(new InvoiceVO(), oldSegmentVO, oldPackBVOs,entLotVO.getLot());
								flag = false;
								break;
							}
						}
					}
					if(flag){
						InvoiceUtils.syncEntrustUpdater(new InvoiceVO(), oldSegmentVO, oldPackBVOs);
					}
				}
			}
		}
	}

	
	public String transToXML(String strReturnMsg,boolean trueOrFalse){
		// 创建message的xml片段
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("result");
		if(trueOrFalse){
			root.addElement("success").addText("true");
		}else{
			root.addElement("success").addText("false");
		}
		root.addElement("msg").addText(strReturnMsg);
		return document.asXML();
	}
	
	public String ImportCoordinates(String jsonCoordinates){
		
		String strReturnMsg = "";
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		//空值判断
		if(jsonCoordinates.isEmpty()){
			strReturnMsg = "未提供任何数据，请确认！";
			return strReturnMsg;
		}
		
		JsonNode coordinates = JacksonUtils.readTree(jsonCoordinates);
		//判断参数有效性
		if(coordinates  == null){
			strReturnMsg = "输入的参数不是要求的格式，请修改！";
			return strReturnMsg;
		}
		List<String> ids = new ArrayList<String>();
		for(JsonNode coordinate :coordinates ){
			//判断主表信息
			strReturnMsg = CheckCoordinateInfo(coordinate);
			if(!strReturnMsg.isEmpty()){
			   return strReturnMsg;
			}
			
			String id = coordinate.get("id").getTextValue();
			String trival_id = coordinate.get("trival_id").getTextValue();
			String longitude = coordinate.get("longitude").getTextValue();
			String latitude = coordinate.get("latitude").getTextValue();
			String time = coordinate.get("time").getTextValue();
			String city = coordinate.get("city") == null ? null : coordinate.get("city").getTextValue();
			String street_name = coordinate.get("street_name") == null ? null : coordinate.get("street_name").getTextValue();
			String address_name = coordinate.get("address_name") == null ? null : coordinate.get("address_name").getTextValue();
			String zipcode = coordinate.get("zipcode") == null ? null : coordinate.get("zipcode").getTextValue();
			
			CoordinatesVO coordinatesVO = new CoordinatesVO();
			coordinatesVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(coordinatesVO);
			coordinatesVO.setId(id);
			coordinatesVO.setTrival_id(trival_id);
			coordinatesVO.setLatitude(latitude);
			coordinatesVO.setLongitude(longitude);
			coordinatesVO.setTime(new UFDateTime(time));
			coordinatesVO.setCity(city);
			coordinatesVO.setStreet_name(street_name);
			coordinatesVO.setAddress_name(address_name);
			coordinatesVO.setZipcode(zipcode);
			coordinatesVO.setCreate_time(new UFDateTime(new Date()));
			coordinatesVO.setCreate_user(ediUserVO.getPk_user());
			coordinatesVO.setPk_corp(ediUserVO.getPk_corp());
			ids.add(id);
			toBeUpdate.add(coordinatesVO);
		}
		
		if(ids.size() > 0){
			String coordinatesCond = NWUtils.buildConditionString(ids.toArray(new String[ids.size()]));
			CoordinatesVO[] oldCoordinatesVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CoordinatesVO.class, " id in " + coordinatesCond);
			if(oldCoordinatesVOs != null && oldCoordinatesVOs.length > 0){
				for(CoordinatesVO coordinatesVO : oldCoordinatesVOs){
					coordinatesVO.setStatus(VOStatus.DELETED);
					toBeUpdate.add(coordinatesVO);
				}
			}
		}
		
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		strReturnMsg = "导入经纬度信息成功！";
		return strReturnMsg;
		
	}
	
	public String CheckCoordinateInfo(JsonNode coordinate)
	{
		String strReturnMsg = "";
		
		JsonNode id = coordinate.get("id");
		JsonNode trival_id = coordinate.get("trival_id");
		JsonNode longitude = coordinate.get("longitude");
		JsonNode latitude = coordinate.get("latitude");
		JsonNode time = coordinate.get("time");
		JsonNode city = coordinate.get("city");
		JsonNode street_name = coordinate.get("street_name");
		JsonNode address_name = coordinate.get("address_name");
		JsonNode zipcode = coordinate.get("zipcode");
		
		
		
		
		if(id == null || trival_id == null|| longitude == null || latitude == null
				|| time == null || city == null
				|| street_name == null||address_name == null|| zipcode == null){
			strReturnMsg = "主表结构不正确，请确认！";
		}
		
		String strId = coordinate.get("id").getTextValue();
		String strTrival_id = coordinate.get("trival_id").getTextValue();
		String strLongitude = coordinate.get("longitude").getTextValue();
		String strLatitude = coordinate.get("latitude").getTextValue();
		String strTime = coordinate.get("time").getTextValue();
	
		if(strId.isEmpty() ||strTrival_id.isEmpty()
				||strLongitude.isEmpty()||strLatitude.isEmpty()
				||strTime.isEmpty()){
			strReturnMsg = "主表 coordinate必填项为空，请确认！";
				}
		
		return strReturnMsg;
	}
	
	
	
}
