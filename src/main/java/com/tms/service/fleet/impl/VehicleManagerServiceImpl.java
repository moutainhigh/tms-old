package com.tms.service.fleet.impl;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.fleet.VehicleManagerService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.fleet.VehicleManagerVO;

@Service
public class VehicleManagerServiceImpl extends TMSAbsBillServiceImpl implements VehicleManagerService {

	public String getBillType() {
		return BillTypeConst.YCGL;
	}

	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, VehicleManagerVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, VehicleManagerVO.PK_VEHICLE_MANAGER);
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_vehicle_manager");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_vehicle_manager");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				}
			}
		}
		return templetVO;
	}
	/**
	 * 提交后，单据属于确认中状态
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> commit(ParamVO paramVO) {
		logger.info("执行单据提交动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.NEW != billStatus && BillStatus.YCGL_REFUSE != billStatus) {
				throw new RuntimeException("只有[新建]和[拒绝]状态的单据才能进行提交！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue("refuse_reason", null);//清除拒绝信息
		parentVO.setAttributeValue("refuse_time", null);
		parentVO.setAttributeValue("refuse_user", null);
		parentVO.setAttributeValue("refuse_memo", null);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.YCGL_CONFIRMING); // 设置成确认中
		parentVO.setAttributeValue(getCommitTimeField(), new UFDateTime(new Date()));
		parentVO.setAttributeValue(getCommitUserField(), WebUtils.getLoginInfo().getPk_user());
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	/**
	 * 提交后，单据属于确认中状态
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> uncommit(ParamVO paramVO) {
		logger.info("执行单据反提交动作，主键：" + paramVO.getBillId());
		AggregatedValueObject billVO = queryBillVO(paramVO);
		processBeforeConfirm(billVO, paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object oBillStatus = parentVO.getAttributeValue(getBillStatusField());
		if(oBillStatus != null) {
			int billStatus = Integer.parseInt(oBillStatus.toString());
			if(BillStatus.YCGL_CONFIRMING != billStatus) {
				throw new RuntimeException("只有[确认中]状态的单据才能进行反提交！");
			}
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.NEW); // 设置成新建
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}
	
	public Map<String, Object> vmcheck(String id, Integer choice, Integer reason_type, String memo){
		VehicleManagerVO vehicleManagerVO = NWDao.getInstance().queryByPK(VehicleManagerVO.class, id);
		if(vehicleManagerVO == null || vehicleManagerVO.getVbillstatus() != BillStatus.YCGL_CONFIRMING){
			throw new BusiException("只有状态是[确认中]的单据，才可以审核！");
		}
		if(choice == 0){//拒绝
			if(reason_type == null){
				throw new BusiException("拒绝时，必须提供拒绝原因！");
			}
			vehicleManagerVO.setVbillstatus(BillStatus.YCGL_REFUSE);
			vehicleManagerVO.setRefuse_memo(memo);
			vehicleManagerVO.setRefuse_reason(reason_type);
			vehicleManagerVO.setRefuse_time(new UFDateTime(new Date()));
			vehicleManagerVO.setRefuse_user(WebUtils.getLoginInfo().getPk_user());
			vehicleManagerVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(vehicleManagerVO);
		}else if(choice == 1){//同意
			vehicleManagerVO.setVbillstatus(BillStatus.YCGL_CONFIRM);
			vehicleManagerVO.setConfirm_memo(memo);
			vehicleManagerVO.setConfirm_time(new UFDateTime(new Date()));
			vehicleManagerVO.setConfirm_user(WebUtils.getLoginInfo().getPk_user());
			vehicleManagerVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(vehicleManagerVO);
		}
		return null;
	}
	public Map<String, Object> vmrecheck(String id){
		VehicleManagerVO vehicleManagerVO = NWDao.getInstance().queryByPK(VehicleManagerVO.class, id);
		if(vehicleManagerVO == null || !(vehicleManagerVO.getVbillstatus() == BillStatus.YCGL_CONFIRM || vehicleManagerVO.getVbillstatus() == BillStatus.YCGL_REFUSE)){
			throw new BusiException("只有状态是[确认,拒绝]的单据，才可以重新审核！");
		}
		vehicleManagerVO.setVbillstatus(BillStatus.YCGL_CONFIRMING);
		vehicleManagerVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(vehicleManagerVO);
		return null;
	}
	public CarVO[] getCarno() {
		CarVO[] carVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarVO.class, CorpHelper.getCurrentCorpWithChildren());
		return carVOs;
	}
	
	public DriverVO[] getDriver() {
		DriverVO[] driverVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(DriverVO.class, CorpHelper.getCurrentCorpWithChildren());
		return driverVOs;
	}

	public Map<String, Object> vmsend(String id, String carno, String main_driver, String deputy_drive, String memo) {
		VehicleManagerVO vehicleManagerVO = NWDao.getInstance().queryByPK(VehicleManagerVO.class, id);
		if(vehicleManagerVO == null || vehicleManagerVO.getVbillstatus() != BillStatus.YCGL_CONFIRM){
			throw new BusiException("只有状态是[确认]的单据，才可以派车！");
		}
		if(carno == null || main_driver == null){
			throw new BusiException("车牌号和主驾驶都不能为空！");
		}
		vehicleManagerVO.setSend_memo(memo);
		vehicleManagerVO.setSend_time(new UFDateTime(new Date()));
		vehicleManagerVO.setSend_user(WebUtils.getLoginInfo().getPk_user());
		vehicleManagerVO.setCarno(carno);
		vehicleManagerVO.setMain_driver(main_driver);
		vehicleManagerVO.setDeputy_drive(deputy_drive);
		vehicleManagerVO.setVbillstatus(BillStatus.YCGL_SEND);
		vehicleManagerVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(vehicleManagerVO);
		return null;
	}

	public AddressVO[] getAddr() {
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, CorpHelper.getCurrentCorpWithChildren());
		return addressVOs;
	}

	public Map<String, Object> vmdispatch(String id, String watch, String gps, String fule, String time, String addr,
			String memo) {
		VehicleManagerVO vehicleManagerVO = NWDao.getInstance().queryByPK(VehicleManagerVO.class, id);
		if(vehicleManagerVO == null || vehicleManagerVO.getVbillstatus() != BillStatus.YCGL_SEND){
			throw new BusiException("只有状态是[派车中]的单据，才可以出车！");
		}
		vehicleManagerVO.setDispatch_memo(memo);
		vehicleManagerVO.setDispatch_time(new UFDateTime(time.replace("T", " ")));
		vehicleManagerVO.setDispatch_fule(new UFDouble(fule));
		vehicleManagerVO.setDispatch_GPS(gps);
		vehicleManagerVO.setDispatch_watch(new UFDouble(watch));
		AddressVO addressVO = NWDao.getInstance().queryByPK(AddressVO.class, addr);
		if(addressVO != null){
			vehicleManagerVO.setDispatch_addr(addressVO.getPk_address());
			vehicleManagerVO.setDispatch_city(addressVO.getPk_city());
			vehicleManagerVO.setDispatch_detail_addr(addressVO.getDetail_addr());
		}else{
			vehicleManagerVO.setDispatch_detail_addr(addr);
		}
		vehicleManagerVO.setVbillstatus(BillStatus.YCGL_DIS);
		vehicleManagerVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(vehicleManagerVO);
		return null;
	}

	public Map<String, Object> vmreturn(String id, String watch, String gps, String fule, String time, String addr,
			String memo) {
		VehicleManagerVO vehicleManagerVO = NWDao.getInstance().queryByPK(VehicleManagerVO.class, id);
		if(vehicleManagerVO == null || vehicleManagerVO.getVbillstatus() != BillStatus.YCGL_DIS){
			throw new BusiException("只有状态是[出车中]的单据，才可以收车！");
		}
		vehicleManagerVO.setReturn_memo(memo);
		UFDateTime returnTime = new UFDateTime(time.replace("T", " "));
		if(returnTime.after(vehicleManagerVO.getDispatch_time()) && returnTime.before(new UFDateTime(new Date()))){
			vehicleManagerVO.setReturn_time(returnTime);
		}else{
			throw new BusiException("收车时间必须晚于出车时间，而且不晚于当前时间！");
		}
		
		vehicleManagerVO.setReturn_fule(new UFDouble(fule));
		vehicleManagerVO.setReturn_GPS(gps);
		UFDouble returnWatch = new UFDouble(watch);
		if(returnWatch.toDouble() >= vehicleManagerVO.getDispatch_watch().toDouble()){
			vehicleManagerVO.setReturn_watch(returnWatch);
		}else{
			throw new BusiException("收车码表数不能小于出车码表数！");
		}
		
		AddressVO addressVO = NWDao.getInstance().queryByPK(AddressVO.class, addr);
		if(addressVO != null){
			vehicleManagerVO.setReturn_addr(addressVO.getPk_address());
			vehicleManagerVO.setReturn_city(addressVO.getPk_city());
			vehicleManagerVO.setReturn_detail_addr(addressVO.getDetail_addr());
		}else{
			vehicleManagerVO.setReturn_detail_addr(addr);
		}
		vehicleManagerVO.setVbillstatus(BillStatus.YCGL_RET);
		vehicleManagerVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(vehicleManagerVO);
		return null;
	}
	
	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		String[]  fields = copyVO.getParentVO().getAttributeNames();
		if(fields != null && fields.length > 0){
			for(String field : fields){
				if(!(field.equals("applicant") || field.equals("appl_dept") || field.equals("appl_reason") 
						|| field.equals("appl_nemo") || field.equals("appl_car_type") || field.equals("appl_start_time")
						|| field.equals("appl_end_time") || field.equals("appl_start_addr") || field.equals("appl_start_city")
						|| field.equals("appl_start_detail_addr") || field.equals("appl_end_addr") || field.equals("appl_end_city")
						|| field.equals("appl_start_time") || field.equals("appl_end_time")|| field.equals("appl_end_detail_addr") 
						|| field.equals("pk_corp"))){
					copyVO.getParentVO().setAttributeValue(field, null);
				}
			}
			copyVO.getParentVO().setAttributeValue("vbillstatus", BillStatus.NEW);
		}
		
		
	}
}
