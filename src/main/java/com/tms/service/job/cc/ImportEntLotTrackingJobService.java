package com.tms.service.job.cc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.JobDefVO;

import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.impl.LBSUtils;
import com.tms.vo.cc.ColdVehicleControlViewVO;
import com.tms.vo.cc.ColdVehicleViewVO;
import com.tms.vo.te.EntLotTrackingBVO;
import com.tms.vo.te.EntLotTrackingHisBVO;

/**
 * @author XIA 2016 5 30
 * 
 * 冷链车次GPS位置获取信息接口
 * 
 */
public class ImportEntLotTrackingJobService implements IJobService {

	static Logger logger = Logger.getLogger(ImportEntLotTrackingJobService.class);

	public void before(JobDefVO jobDefVO) {

	}

	@SuppressWarnings("unchecked")
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导入车辆批次跟踪记录-------------------");
		if (jobDefVO == null) {
			return;
		}

		logger.info("判断是否有信息需要同步");

		if (!isNeedSyncData(jobDefVO)) {
			return;
		}
		logger.info("检测通过，有数据要同步。");
		logger.info("开始查询数据。");
		ColdVehicleViewVO[] cold_vehicles = NWDao.getInstance().queryForSuperVOArrayByCondition(ColdVehicleViewVO.class," 1=1 ");
		if(cold_vehicles == null || cold_vehicles.length == 0){
			logger.info("没有车辆信息，停止同步。");
			return;
		}
		String[] gps_ids = new String[cold_vehicles.length];
		for(int i=0;i<cold_vehicles.length;i++){
			gps_ids[i] = String.valueOf(cold_vehicles[i].getGps_id());
		}

		RootVO rootVO = LBSUtils.getCurrentTrackVO(gps_ids);
		if(rootVO == null || !rootVO.isResult()){
			logger.info("LBS接口没有返回正确数据，停止同步。");
			return;
		}
		List<TrackVO> trackVOs = rootVO.getDataset();
		if(trackVOs == null || trackVOs.size() == 0){
			logger.info("LBS接口没有返回正确数据，停止同步。");
			return;
		}
		
		//批次信息
		ColdVehicleControlViewVO[] cold_controls = NWDao.getInstance().queryForSuperVOArrayByCondition(ColdVehicleControlViewVO.class," 1=1 ");
		//数据库里的批次跟踪信息
		String lotSql = "SELECT ent_lot_track_rank.* FROM (SELECT ts_ent_lot_track_b.*,Row_Number() OVER ( "
							+ " partition by ts_ent_lot_track_b.carno "
							+ " ORDER BY ts_ent_lot_track_b.track_time DESC) rank "
							+ " FROM ts_ent_lot_track_b With (nolock) "
						  	+ " WHERE isnull(ts_ent_lot_track_b.dr,0)=0 AND isnull (ts_ent_lot_track_b.carno,'')<>'' "
					+ " )ent_lot_track_rank  "
					+ " WHERE ent_lot_track_rank.rank=1 AND track_time>convert(varchar,getdate()-60,120)";
		List<EntLotTrackingBVO> lotTrackingBVOs = NWDao.getInstance().queryForList(lotSql, EntLotTrackingBVO.class);
		
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		
		//组装数据
		for(ColdVehicleViewVO cold_vehicle : cold_vehicles){
			boolean needInsert = false;
			EntLotTrackingBVO entLotTrackingBVO = null;
			for(EntLotTrackingBVO lotTrackingBVO : lotTrackingBVOs){
				if((lotTrackingBVO.getCarno() + String.valueOf(lotTrackingBVO.getLot()))
						.equals(cold_vehicle.getCarno() + String.valueOf(cold_vehicle.getLot()))){
					entLotTrackingBVO = lotTrackingBVO;
					entLotTrackingBVO.setStatus(VOStatus.UPDATED);
					break;
				}
			}
			if(entLotTrackingBVO == null){
				entLotTrackingBVO = new EntLotTrackingBVO();
				entLotTrackingBVO.setStatus(VOStatus.NEW);
				entLotTrackingBVO.setCreate_time(new UFDateTime(new Date()));
				NWDao.setUuidPrimaryKey(entLotTrackingBVO);
			}
			convertVehicleToLotTracking(entLotTrackingBVO, cold_vehicle);
			//拼接跟踪信息,没有跟踪信息,跟踪记录也不插入了。
			boolean flag = true;
			for(TrackVO trackVO : trackVOs){
				if(trackVO.getGpsid().equals(cold_vehicle.getGps_id())){
					needInsert = convertTrackVOToLotTracking(entLotTrackingBVO, trackVO);
					flag = false;
					break;
				}
			}
			if (flag) {
				continue;
			}
			//拼接批次信息
			for(ColdVehicleControlViewVO cold_control : cold_controls){
				//车次信息里可能没有lot，需要特殊处理。
				if((cold_vehicle.getCarno() + String.valueOf(cold_vehicle.getLot()))
						.equals(cold_control.getCarno() + cold_control.getLot())){
					convertLotToLotTracking(entLotTrackingBVO, cold_control);
					break;
				}
			}
			toBeUpdate.add(entLotTrackingBVO);
			//生成历史记录
			EntLotTrackingHisBVO hisBVO = null;
			if(needInsert){
				hisBVO = convertToHis(entLotTrackingBVO);
			}else{
				String sql = "";
				if(entLotTrackingBVO.getLot() == null){
					sql = "SELECT TOP 1 * FROM ts_ent_lot_track_his_b WITH(NOLOCK) where isnull(dr,0)=0 and carno=? and isnull(lot,'')='' ORDER BY gps_time DESC ";
					hisBVO = NWDao.getInstance().queryForObject(sql, EntLotTrackingHisBVO.class, entLotTrackingBVO.getCarno());
				}else{
					sql = "SELECT TOP 1 * FROM ts_ent_lot_track_his_b WITH(NOLOCK) where isnull(dr,0)=0 and carno=? and lot=? ORDER BY gps_time DESC ";
					hisBVO = NWDao.getInstance().queryForObject(sql, EntLotTrackingHisBVO.class, 
							entLotTrackingBVO.getCarno(),entLotTrackingBVO.getLot());
				}
				if(hisBVO == null){
					hisBVO = convertToHis(entLotTrackingBVO);
				}else{
					hisBVO.setStatus(VOStatus.UPDATED);
					hisBVO.setGps_time(entLotTrackingBVO.getGps_time());
				}
			}
			toBeUpdate.add(hisBVO);
		}
		
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		
		logger.info("-------------------导入车辆批次跟踪记录结束-------------------");
		
		//如果是间隔同步要修改同步时间
		if(jobDefVO.getExec_type() == 1){
			jobDefVO.setDef1((new UFDateTime(new Date())).toString());
		}
		// 获取任务同步次数,更新数据 def2为同步次数
		if (jobDefVO.getDef2() == null) {
			long syncCount = Long.parseLong(jobDefVO.getDef2() == null ? "0" : jobDefVO.getDef2());
			jobDefVO.setDef2(String.valueOf(syncCount++));
		}
		jobDefVO.setStatus(VOStatus.UPDATED);
		// 保存数据
		NWDao.getInstance().saveOrUpdate(jobDefVO);
	}

	public void after(JobDefVO jobDefVO) {

	}

	// 判断是否需要同步信息
	private boolean isNeedSyncData(final JobDefVO jobDefVO) {

		// 设置要返回的值
		boolean bReturn = false;

		// 执行类型，如果 ExecType =1 是间隔，2是定时
		if (jobDefVO.getExec_type() == 1) {
			UFDateTime lastExecTime = jobDefVO.getDef1() == null ? new UFDateTime("2000-01-01 00:00:00") : new UFDateTime(jobDefVO.getDef1());
			UFDateTime now = new UFDateTime(new Date());
			if(UFDateTime.getSecondsBetween(lastExecTime, now) >= jobDefVO.getJob_interval()) {
				bReturn = true;
			}else{
				logger.info("间隔时间:" + UFDateTime.getSecondsBetween(lastExecTime, now) + " 过短，不需要同步数据!");
			}
		}
		// 定时同步
		else if (jobDefVO.getExec_type() == 2) {
			// 获取指定运行时间，并拆分为时分秒
			String strExecTime = jobDefVO.getExec_time();
			int iSecIndex = strExecTime.lastIndexOf(':');
			int iExecSec = Integer.parseInt(strExecTime.substring(iSecIndex + 1, strExecTime.length()));

			strExecTime = strExecTime.substring(0, iSecIndex);
			int iMinIndex = strExecTime.lastIndexOf(':');
			int iExecHour = Integer.parseInt(strExecTime.substring(0, iMinIndex));
			int iExecMin = Integer.parseInt(strExecTime.substring(iMinIndex + 1, strExecTime.length()));

			// 获取当前时间 如果当前时间-最后一次同步时间 大于间隔时间，就进行一次同步
			Date dCurrDate = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strCurrent = fmt.format(dCurrDate);

			int indexHour = strCurrent.indexOf(':');
			int iCurrHour = Integer.parseInt(strCurrent.substring(indexHour - 2, indexHour));
			int iCurrMin = DateUtils.getMinute();
			int iCurrSec = DateUtils.getSecond();

			if (iCurrHour > iExecHour) {
				bReturn = true;
			} else if (iCurrHour == iExecHour && iCurrMin > iExecMin) {
				bReturn = true;
			} else if (iCurrHour == iExecHour && iCurrMin == iExecMin && iCurrSec > iExecSec) {
				bReturn = true;
			}

		}

		return bReturn;
	}
	
	private void convertVehicleToLotTracking(EntLotTrackingBVO entLotTrackingBVO,ColdVehicleViewVO cold_vehicle){
		entLotTrackingBVO.setCarno(cold_vehicle.getCarno());
		entLotTrackingBVO.setPk_carrier(cold_vehicle.getPk_carrier());
		entLotTrackingBVO.setGps_id(cold_vehicle.getGps_id());
		entLotTrackingBVO.setDriver_name(cold_vehicle.getDriver_name());
		entLotTrackingBVO.setDriver_mobile(cold_vehicle.getDriver_mobile());
		entLotTrackingBVO.setPk_corp(cold_vehicle.getPk_corp());
	}
	
	private boolean convertTrackVOToLotTracking(EntLotTrackingBVO entLotTrackingBVO,TrackVO trackVO){
		//判断经纬度，用来位置信息，当位置相同时，抛出布尔判断
		boolean needInsert = true;
		String latitude = String.valueOf(entLotTrackingBVO.getLatitude());
		String longitude = String.valueOf(entLotTrackingBVO.getLongitude());
		String temp1 = String.valueOf(entLotTrackingBVO.getTemp1());
		String temp2 = String.valueOf(entLotTrackingBVO.getTemp2());
		String temp3 = String.valueOf(entLotTrackingBVO.getTemp3());
		String temp4 = String.valueOf(entLotTrackingBVO.getTemp4());
		String press = String.valueOf(entLotTrackingBVO.getPress());
		String str = latitude + "." + longitude + "." + temp1 +"." + temp2 +"." + temp3 +"." + temp4 + "." + press;
				
		String _latitude = String.valueOf(trackVO.getLatitude());
		String _longitude = String.valueOf(trackVO.getLongitude());
		String _temp1 = String.valueOf(trackVO.getTemp1());
		String _temp2 = String.valueOf(trackVO.getTemp2());
		String _temp3 = String.valueOf(trackVO.getTemp3());
		String _temp4 = String.valueOf(trackVO.getTemp4());
		
		String _press = String.valueOf(trackVO.getPress());
		String _str = _latitude + "." + _longitude + "." + _temp1 + "." + _temp2 + "." + _temp3 + "." + _temp4 + "." + _press;
		
		if(str.equals(_str)){
			needInsert = false;
		}
		entLotTrackingBVO.setLatitude(trackVO.getLatitude());
		entLotTrackingBVO.setLongitude(trackVO.getLongitude());
		entLotTrackingBVO.setSpeed(trackVO.getSpeed() == null ? UFDouble.ZERO_DBL : trackVO.getSpeed());
		entLotTrackingBVO.setSpeed_status(trackVO.getSpeed_status());
		entLotTrackingBVO.setTemp1(trackVO.getTemp1());
		entLotTrackingBVO.setTemp2(trackVO.getTemp2());
		entLotTrackingBVO.setTemp3(trackVO.getTemp3());
		entLotTrackingBVO.setTemp4(trackVO.getTemp4());
		entLotTrackingBVO.setTemp_time1(trackVO.getTemp_time1());
		entLotTrackingBVO.setTemp_time2(trackVO.getTemp_time2());
		entLotTrackingBVO.setTemp_time3(trackVO.getTemp_time3());
		entLotTrackingBVO.setTemp_time4(trackVO.getTemp_time4());
		entLotTrackingBVO.setFuel(trackVO.getFuel());
		entLotTrackingBVO.setPress(trackVO.getPress());
		entLotTrackingBVO.setEvent(trackVO.getEvent());
		entLotTrackingBVO.setDistance(trackVO.getDistance());
		entLotTrackingBVO.setEngine_status(trackVO.getEngine_status());
		entLotTrackingBVO.setCompressor_status(trackVO.getCompressor_status());
		entLotTrackingBVO.setMagnetic(trackVO.getMagnetic());
		entLotTrackingBVO.setFence_code(trackVO.getFence_code());
		entLotTrackingBVO.setFence_name(trackVO.getFence_name());
		entLotTrackingBVO.setEnter_time(trackVO.getEnter_time());
		entLotTrackingBVO.setExit_time(trackVO.getExit_time());
		entLotTrackingBVO.setPlace_name(trackVO.getPlace_name());
		entLotTrackingBVO.setRoad_name(trackVO.getRoad_name());
		entLotTrackingBVO.setTrack_time(new UFDateTime(new Date()));
		entLotTrackingBVO.setGps_time(trackVO.getGps_time());
		entLotTrackingBVO.setGps_latitude(trackVO.getGps_latitude());
		entLotTrackingBVO.setGps_longitude(trackVO.getGps_longitude());
		entLotTrackingBVO.setProtocol_version(trackVO.getProtocol_version());
		entLotTrackingBVO.setGps_accuracy(trackVO.getGps_accuracy());
		entLotTrackingBVO.setHeading(trackVO.getHeading());
		entLotTrackingBVO.setAltitude(trackVO.getAltitude());
		return needInsert;
	}

	private void convertLotToLotTracking(EntLotTrackingBVO entLotTrackingBVO,ColdVehicleControlViewVO cold_control){
		entLotTrackingBVO.setLot(cold_control.getLot());
		entLotTrackingBVO.setLow_temp(cold_control.getLow_temp());
		entLotTrackingBVO.setHight_temp(cold_control.getHight_temp());
		entLotTrackingBVO.setEnt_lot_speed(cold_control.getEnt_lot_speed() == null ? UFDouble.ZERO_DBL : cold_control.getEnt_lot_speed());
		entLotTrackingBVO.setPk_address(cold_control.getPk_address());
		entLotTrackingBVO.setDetail_addr(cold_control.getDetail_addr());
		entLotTrackingBVO.setReq_arri_date(cold_control.getReq_arri_date());
		entLotTrackingBVO.setForecast_deli_date(cold_control.getForecast_deli_date());
		entLotTrackingBVO.setService_status(cold_control.getService_status());
		entLotTrackingBVO.setNum_count(cold_control.getNum_count() == null ? 0 : cold_control.getNum_count());
		entLotTrackingBVO.setWeight_count(cold_control.getWeight_count() == null ? UFDouble.ZERO_DBL : cold_control.getWeight_count());
		entLotTrackingBVO.setVolume_count(cold_control.getVolume_count() == null ? UFDouble.ZERO_DBL : cold_control.getVolume_count());
		if(entLotTrackingBVO.getSpeed() != null 
				&& entLotTrackingBVO.getEnt_lot_speed() != null 
				&&  (entLotTrackingBVO.getSpeed().sub(entLotTrackingBVO.getEnt_lot_speed())).doubleValue() > 0){
			entLotTrackingBVO.setSpeed_status("超速");
		}
		
	}
	
	private EntLotTrackingHisBVO convertToHis(EntLotTrackingBVO entLotTrackingBVO){
		EntLotTrackingHisBVO hisBVO = new EntLotTrackingHisBVO();
		if(entLotTrackingBVO == null){
			return hisBVO;
		}
		//历史表和实际表的数据完全一致
		for(String attr : entLotTrackingBVO.getAttributeNames()){
			if(!attr.equals(entLotTrackingBVO.getPKFieldName())){
				hisBVO.setAttributeValue(attr, entLotTrackingBVO.getAttributeValue(attr));
			}
		}
		
		hisBVO.setStatus(VOStatus.NEW);
		hisBVO.setPk_ent_lot_track_his_b(null);
		hisBVO.setCreate_time(new UFDateTime(new Date()));
		NWDao.setUuidPrimaryKey(hisBVO);
		return hisBVO;
	}
}
