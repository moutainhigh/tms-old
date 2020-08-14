package com.tms.service.te.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.exp.ExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.DriverVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;

/**
 * 运力信息导入
 * 
 * @author xuqc
 * @date 2013-11-26 下午04:14:16
 */
public class TransbilityExcelImporter extends BillExcelImporter {

	public TransbilityExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	protected SuperVO getParentVO() {
		return new EntTransbilityBVO();
	}
	
	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		List<EntTransbilityBVO> etbBVOs  = new ArrayList<EntTransbilityBVO>();
		if(aggVOs == null || aggVOs.size() == 0){
			throw new BusiException("没有导入信息");
		}
		for(AggregatedValueObject aggVO : aggVOs) {
			EntTransbilityBVO etEntTransbilityBVOs = (EntTransbilityBVO)aggVO.getParentVO();
			etbBVOs.add(etEntTransbilityBVOs);
		}
		saveEntTransbility(etbBVOs);
		
	} 
	
	public void saveEntTransbility(List<EntTransbilityBVO> etbBVOs){
		List<EntTransbilityBVO> oldbVOS = new ArrayList<EntTransbilityBVO>();
		List<SuperVO> updateList = new ArrayList<SuperVO>();
		int rowNum = 1;
		for(EntTransbilityBVO etbVO : etbBVOs){
			if(etbVO.getCarno() == null && etbVO.getPk_car_type() == null){
				throw new BusiException("第[?]行：车牌号和车辆类型不能同时为空！",rowNum+"");
			}
			CarVO carVO = NWDao.getInstance().queryByCondition(CarVO.class, "carno=?", etbVO.getCarno());
			//传进etbVO里的PK_entrust,实际上就EXCEL里的vbillno
			EntrustVO entVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", etbVO.getPk_entrust());
			if(entVO == null){
				throw new BusiException("委托单[?]不存在，请检查数据！" ,etbVO.getPk_entrust());
			}
			EntTransbilityBVO entTransbilityBVO = new EntTransbilityBVO();
			
			entTransbilityBVO.setPk_entrust(entVO.getPk_entrust());
			entTransbilityBVO.setLot(entVO.getLot());
			entTransbilityBVO.setCarno(etbVO.getCarno());
			entTransbilityBVO.setPk_driver(etbVO.getPk_driver());
			//先将driver_name 和driver_mobile添加到VO里，如果存在pk_driver
			//那么再用pk_driver获取对应的name和mobile 如果传入数据为空，就添加到数据库，否则就使用原有的。
			entTransbilityBVO.setDriver_name(etbVO.getDriver_name());
			entTransbilityBVO.setDriver_mobile(etbVO.getDriver_mobile());
			if(StringUtils.isNotBlank(etbVO.getPk_driver())){
				DriverVO driverVO = NWDao.getInstance().queryByCondition(DriverVO.class, "driver_code=?", etbVO.getPk_driver());
				if(StringUtils.isBlank(etbVO.getDriver_name()) && driverVO != null){
					entTransbilityBVO.setDriver_name(driverVO.getDriver_name());
				}
				if(StringUtils.isBlank(etbVO.getDriver_mobile()) && driverVO != null){
					entTransbilityBVO.setDriver_mobile(driverVO.getMobile());
				}
			}
			entTransbilityBVO.setContainer_no(etbVO.getContainer_no());
			entTransbilityBVO.setSealing_no(etbVO.getSealing_no());
			entTransbilityBVO.setForecast_deli_date(etbVO.getForecast_deli_date());
			entTransbilityBVO.setCarno(etbVO.getCarno());
			//传进etbVO里的PK_car_type,实际上就EXCEL里的name
			//业务需求：当车型和车牌号同时为空时  不成立
			//       当车型存在但与数据库里的数据不符合的时候 不成立
			//       当车型不存在的时候，从车牌号获取车型，获取不到的时候  不成立
			if(StringUtils.isBlank(etbVO.getPk_car_type())){
				// 2015 11 13 yaojiie存在车辆类型为空，车牌号存在，但是车牌号对应的数据错误
				if(carVO == null){
					throw new BusiException("第[?]行：车辆类型为空，提供的车牌号错误！",rowNum+"");
				}else
				if(StringUtils.isBlank(carVO.getPk_car_type())){
					throw new BusiException("第[?]行：车辆类型为空，车牌号获取的车辆类型也为空！",rowNum+"");
				}
					entTransbilityBVO.setPk_car_type(carVO.getPk_car_type());
				}else{
					CarTypeVO carTypeVO = NWDao.getInstance().queryByCondition(CarTypeVO.class, "pk_car_type=?",etbVO.getPk_car_type());
					if(carTypeVO == null ){
						throw new BusiException("第[?]行：输入的车辆类型不正确！",rowNum+"");
					}else{
						//etbVO.getPk_car_type() 获取到的是车辆类型名称。
						entTransbilityBVO.setPk_car_type(carTypeVO.getPk_car_type());
					}
				}
			//这里判断GPS号，当Excel里没有传入GPSID时，去车辆信息里找GPSID，车辆信息也没有，则不填。
			if(StringUtils.isBlank(etbVO.getGps_id())){
				if(carVO == null){
					entTransbilityBVO.setGps_id(etbVO.getGps_id());
				}else{
					entTransbilityBVO.setGps_id(carVO.getGps_id());
				}
			}else{
				entTransbilityBVO.setGps_id(etbVO.getGps_id());
			}
			entTransbilityBVO.setMemo(etbVO.getMemo());
			//需要修改订单状态为删除的VO
			EntTransbilityBVO[] bVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
			"pk_entrust =?",entVO.getPk_entrust() );
			if(bVOs != null && bVOs.length > 0){
				for(EntTransbilityBVO bVO : bVOs){
					bVO.setStatus(VOStatus.DELETED);
					oldbVOS.add(bVO);
				}
			}
			if(oldbVOS!=null && oldbVOS.size()>0)	{
				updateList.addAll(oldbVOS);
			}
			//设置身份证号，和行驶证号码
			entTransbilityBVO.setCertificate_id(etbVO.getCertificate_id());
			entTransbilityBVO.setDriving_license(etbVO.getDriving_license());
			entTransbilityBVO.setStatus(VOStatus.NEW);	
			NWDao.setUuidPrimaryKey(entTransbilityBVO);
			updateList.add(entTransbilityBVO);
			rowNum++;
		}
		NWDao.getInstance().saveOrUpdate(updateList);
	
	}
}
