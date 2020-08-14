package com.tms.services.peripheral;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.utils.BillnoHelper;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.UserVO;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.vo.base.ETCBVO;
import com.tms.vo.base.ETCVO;
import com.tms.vo.base.FuelCardBVO;
import com.tms.vo.base.FuelCardVO;
import com.tms.vo.fleet.RefuelVO;
import com.tms.vo.fleet.TollBVO;
import com.tms.vo.fleet.TollVO;
import com.tms.vo.te.EntTransbilityBVO;


/**
 * @author XIA
 * @for 加油和路桥费
 */
@SuppressWarnings("deprecation")
public class RefuelingAndTollServiceEndPoint extends ServletEndpointSupport {
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	public Map<String,Object> refueling(Map params) {
		
		boolean success = false;
		String msg = "";
		
		String lot = String.valueOf(params.get("lot"));
		String gas_station = String.valueOf(params.get("gas_station"));
		String price = String.valueOf(params.get("price"));
		String amount = String.valueOf(params.get("amount"));
		String quantity = String.valueOf(params.get("quantity"));
		String gas_type = String.valueOf(params.get("gas_type"));
		String pay_type = String.valueOf(params.get("pay_type"));
		String pk_fuelcard = String.valueOf(params.get("pk_fuelcard"));
		String memo = String.valueOf(params.get("memo"));
		String curr_longitude = String.valueOf(params.get("curr_longitude"));
		String curr_latitude = String.valueOf(params.get("curr_latitude"));
		String app_detail_addr = String.valueOf(params.get("app_detail_addr"));
		String loginId = String.valueOf(params.get("loginId"));
		
		
		RefuelVO refuelVO = new RefuelVO();
		refuelVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(refuelVO);
		if(StringUtils.isBlank(lot)){
			//未绑定车次，那只能是现金支付
			refuelVO.setPay_type(0);
		}else{
			refuelVO.setLot(lot);
			if(pay_type.equals("card_pay")){//加油卡结算
				refuelVO.setPay_type(1);
				FuelCardVO fuelCardVO = NWDao.getInstance().queryByCondition(FuelCardVO.class, "pk_fuelcard=?", pk_fuelcard);
				if(fuelCardVO == null){
					success = false;
					msg = "加油卡信息错误";
					return WebServicesUtils.genAjaxResponse(success, msg, null);
				}
				if(fuelCardVO.getAmount() != null && fuelCardVO.getAmount().doubleValue() < (
						amount == null ? 0 : Double.parseDouble(amount))){
					success = false;
					msg = "加油卡金额不足";
					return WebServicesUtils.genAjaxResponse(success, msg, null);
				}
				fuelCardVO.setAmount(fuelCardVO.getAmount().sub(new UFDouble(amount)));
				fuelCardVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(fuelCardVO);
				EntTransbilityBVO[] transbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "lot=?", lot);
				refuelVO.setPk_fuelcard(pk_fuelcard);
				refuelVO.setCarno(transbilityBVOs[0].getCarno());
				//插入一条加油明细记录
				FuelCardBVO cardBVO = new FuelCardBVO();
				cardBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(cardBVO);
				cardBVO.setPk_fuelcard(pk_fuelcard);
				cardBVO.setOperat_addr(gas_station);
				cardBVO.setOperat_date(new UFDateTime(new Date()));
				cardBVO.setOperation_type(0);
				cardBVO.setOperator(loginId);
				cardBVO.setPk_refuel(refuelVO.getPk_refuel());
				cardBVO.setAmount(new UFDouble(amount));
				cardBVO.setMemo(memo);
				NWDao.getInstance().saveOrUpdate(cardBVO);
			}else if(pay_type.equals("cash_pay")){
				refuelVO.setPay_type(0);
			}
		}
		//插入一条加油记录
		UserVO userVO = NWDao.getInstance().queryForObject("SELECT * FROM nw_user WITH(NOLOCK)  WHERE isnull(dr,0)=0 AND user_code = ?",
				UserVO.class, loginId);
		refuelVO.setPk_driver(userVO.getPk_user());
		refuelVO.setAmount(new UFDouble(amount));
		refuelVO.setCreate_time(new UFDateTime(new Date()));
		refuelVO.setCreate_user(loginId);
		refuelVO.setPk_corp(userVO.getPk_corp());
		refuelVO.setRefuel_dete(new UFDateTime(new Date()));
		refuelVO.setRefuel_mode(gas_type);
		refuelVO.setRefuel_price(new UFDouble(price));
		refuelVO.setRefuel_qty(new UFDouble(quantity));
		refuelVO.setRefuel_station(gas_station);
		refuelVO.setMemo(memo);
		refuelVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.REF));
		refuelVO.setVbillstatus(BillStatus.NEW);
		refuelVO.setCurr_latitude(new UFDouble(curr_latitude));
		refuelVO.setCurr_longitude(new UFDouble(curr_longitude));
		refuelVO.setApp_detail_addr(app_detail_addr);
		NWDao.getInstance().saveOrUpdate(refuelVO);
		success = true;
		msg = "加油记录成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
	
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	public Map<String,Object> toll(Map params) {
		
		boolean success = false;
		String msg = "";
		
		String lot = String.valueOf(params.get("lot"));
		String station_entry = String.valueOf(params.get("station_entry"));
		String station_exit = String.valueOf(params.get("station_exit"));
		String amount = String.valueOf(params.get("amount"));
		String pay_type = String.valueOf(params.get("pay_type"));
		String pk_etc = String.valueOf(params.get("pk_etc"));
		String memo = String.valueOf(params.get("memo"));
		String curr_longitude = String.valueOf(params.get("curr_longitude"));
		String curr_latitude = String.valueOf(params.get("curr_latitude"));
		String app_detail_addr = String.valueOf(params.get("app_detail_addr"));
		String loginId = String.valueOf(params.get("loginId"));
		
		UserVO userVO = NWDao.getInstance().queryForObject("SELECT * FROM nw_user WITH(NOLOCK)  WHERE isnull(dr,0)=0 AND user_code = ?",
				UserVO.class, loginId);
		if(userVO == null){
			success = false;
			msg = "用户错误!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		
		//没有Etc相关信息，生成一个路桥费记录即可
		TollVO tollVO = null;
		if(StringUtils.isBlank(lot) || "cash_pay".equals(pay_type)){
			tollVO = new TollVO();
			tollVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(tollVO);
			tollVO.setPk_etc(pk_etc);
			tollVO.setCost_amount(UFDouble.ZERO_DBL);
			tollVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.TOLL));
			tollVO.setVbillstatus(BillStatus.NEW);
			tollVO.setPk_driver(userVO.getPk_user());
			tollVO.setLot(lot);
			tollVO.setPk_corp(userVO.getPk_corp());
			tollVO.setCreate_time(new UFDateTime(new Date()));
			tollVO.setCreate_user(userVO.getPk_user());
		}else{
			//获取路桥费和加油卡信息
			ETCVO etcVO = NWDao.getInstance().queryByCondition(ETCVO.class, "pk_etc=?", pk_etc);
			if(etcVO == null){
				success = false;
				msg = "etc卡信息错误";
				return WebServicesUtils.genAjaxResponse(success, msg, null);
			}
			if(etcVO.getAmount() != null && etcVO.getAmount().doubleValue() < (
					amount == null ? 0 : Double.parseDouble(amount))){
				success = false;
				msg = "etc金额不足";
				return WebServicesUtils.genAjaxResponse(success, msg, null);
			}
			etcVO.setAmount(etcVO.getAmount().sub(new UFDouble(amount)));
			etcVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(etcVO);
			EntTransbilityBVO[] transbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "lot=?", lot);
			tollVO = NWDao.getInstance().queryByCondition(TollVO.class, "pk_etc=? and lot=?", pk_etc,lot);
			if(tollVO == null){
				tollVO = new TollVO();
				tollVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(tollVO);
				tollVO.setPk_etc(pk_etc);
				tollVO.setCost_amount(UFDouble.ZERO_DBL);
				tollVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.TOLL));
				tollVO.setVbillstatus(BillStatus.NEW);
				tollVO.setPk_driver(transbilityBVOs[0].getPk_driver());
				tollVO.setCarno(transbilityBVOs[0].getCarno());
				tollVO.setLot(lot);
				tollVO.setPk_corp(userVO.getPk_corp());
				tollVO.setCreate_time(new UFDateTime(new Date()));
				tollVO.setCreate_user(userVO.getPk_user());
			}else{
				tollVO.setStatus(VOStatus.UPDATED);
				tollVO.setModify_time(new UFDateTime(new Date()));
				tollVO.setModify_user(userVO.getPk_user());
			}
			
			//插入一条etc明细记录
			ETCBVO etcbvo = new ETCBVO();
			etcbvo.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(etcbvo);
			etcbvo.setPk_etc(pk_etc);
			etcbvo.setOperat_addr(app_detail_addr);
			etcbvo.setOperat_date(new UFDateTime(new Date()));
			etcbvo.setOperation_type(0);
			etcbvo.setOperator(loginId);
			etcbvo.setPk_toll(tollVO.getPk_toll());
			etcbvo.setAmount(new UFDouble(amount));
			etcbvo.setMemo(memo);
			NWDao.getInstance().saveOrUpdate(etcbvo);
		}
		tollVO.setCost_amount(tollVO.getCost_amount().add(new UFDouble(amount)));
		NWDao.getInstance().saveOrUpdate(tollVO);
		
		TollBVO tollBVO = new TollBVO();
		tollBVO.setStatus(VOStatus.NEW);
		tollBVO.setPk_toll(tollVO.getPk_toll());
		NWDao.setUuidPrimaryKey(tollBVO);
		tollBVO.setAmount(new UFDouble(amount));
		tollBVO.setPayment_time(new UFDateTime(new Date()));
		tollBVO.setPay_type(0);
		tollBVO.setRegi_time(new UFDateTime(new Date()));
		tollBVO.setRegi_person(userVO.getPk_user());
		tollBVO.setPk_delivery(station_entry);
		tollBVO.setPk_arrival(station_exit);
		tollBVO.setMemo(memo);
		tollBVO.setCurr_latitude(new UFDouble(curr_latitude));
		tollBVO.setCurr_longitude(new UFDouble(curr_longitude));
		tollBVO.setApp_detail_addr(app_detail_addr);
		NWDao.getInstance().saveOrUpdate(tollBVO);
		success = true;
		msg = "路桥费记录成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}
}
