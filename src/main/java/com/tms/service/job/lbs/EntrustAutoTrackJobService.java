package com.tms.service.job.lbs;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.job.ApiTypeConst;
import org.nw.job.IConverter;
import org.nw.job.IJobService;
import org.nw.utils.HttpUtils;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.JobDefVO;

import com.tms.BillStatus;
import com.tms.constants.TrackingConst;
import com.tms.service.te.impl.EntrustUtils;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;

/**
 * 委托单自动跟踪的业务处理类，系统可以配置改业务类进行自动跟踪
 * 
 * @author xuqc
 * @date 2014-11-12 下午04:38:07
 */
public class EntrustAutoTrackJobService implements IJobService {

	static Logger logger = Logger.getLogger(EntrustAutoTrackJobService.class);

	@SuppressWarnings("rawtypes")
	public void exec(JobDefVO jobDefVO) {
		if(jobDefVO == null) {
			return;
		}
		// 查询已提货的委托单的gpsid
		String sql = "select pk_entrust,gps_id,carno from ts_ent_transbility_b where isnull(dr,0)=0 and "
				+ "pk_entrust in (select pk_entrust from ts_entrust where isnull(dr,0)=0 and vbillstatus=?)";
		List<HashMap> mapList = NWDao.getInstance().queryForList(sql, HashMap.class, BillStatus.ENT_DELIVERY);
		if(mapList != null && mapList.size() > 0) {
			Set<String> gpsIDs = new HashSet<String>();// 不要重复
			for(HashMap map : mapList) {
				if(map.get("gps_id") != null) {
					gpsIDs.add(map.get("gps_id").toString());
				}
			}
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("gpsID", gpsIDs);
			List<TrackVO> vos = null;
			if(ApiTypeConst.HTTP.equalsIgnoreCase(jobDefVO.getApi_type())) {
				vos = createHttpTask(jobDefVO, paramMap);
			} else if(ApiTypeConst.WEBSERVICE.equalsIgnoreCase(jobDefVO.getApi_type())) {
				vos = createWebserviceTask(jobDefVO, paramMap);
			}
			// 自动追加跟踪记录
			if(vos != null && vos.size() > 0) {
				Map<String, TrackVO> trackMap = new HashMap<String, TrackVO>();
				for(TrackVO trackVO : vos) {
					trackMap.put(trackVO.getGpsid(), trackVO);
				}
				for(HashMap map : mapList) {
					String pk_entrust = String.valueOf(map.get("pk_entrust"));
					Object gps_id = map.get("gps_id");
					String carno = String.valueOf(map.get("carno"));
					if(gps_id != null) {
						TrackVO trackVO = trackMap.get(gps_id);
						if(trackVO == null) {
							continue;
						}
						// 更新到委托单的跟踪表
						// 重新读取委托单表，确认此时的状态还是已提货,避免这个过程中已经被修改
						sql = "select * from ts_entrust where isnull(dr,0)=0 and pk_entrust=?";
						EntrustVO entVO = NWDao.getInstance().queryForObject(sql, EntrustVO.class, pk_entrust);
						if(entVO.getVbillstatus().intValue() == BillStatus.ENT_DELIVERY) {
							// 插入委托单跟踪表
							EntTrackingVO etVO = new EntTrackingVO();
							etVO.setTracking_status(TrackingConst.ONROAD);
							etVO.setTracking_time(trackVO.getGps_time());
							etVO.setTracking_memo("车辆[" + carno + "]在 " + trackVO.getPlace_name() + " "
									+ trackVO.getRoad_name());
							etVO.setEntrust_vbillno(entVO.getVbillno());
							// 没有登陆,没有公司数据
							// etVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
							etVO.setCreate_user("系统");
							etVO.setCreate_time(new UFDateTime(new Date()));
							etVO.setInvoice_vbillno(entVO.getInvoice_vbillno());
							etVO.setSync_flag(UFBoolean.TRUE);// 同步到发货单
							// 插入发货单跟踪表
							EntrustUtils.saveEntTracking(etVO, entVO);
						}
					}
				}
			}
		}

	}

	/**
	 * 创建http请求的定时任务
	 * 
	 * @param otVO
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<TrackVO> createHttpTask(final JobDefVO jobDefVO, Map<String, Object> paramMap) {
		if(paramMap == null) {
			paramMap = new HashMap<String, Object>();
		}
		paramMap.put(jobDefVO.getUsername_param(), jobDefVO.getUsername());
		paramMap.put(jobDefVO.getPassword_param(), jobDefVO.getPassword());
		String responseText = null;
		try {
			responseText = HttpUtils.post(jobDefVO.getUrl(), paramMap);
			if(StringUtils.isBlank(responseText)) {
				String msg = "http请求没有返回数据，URL:" + jobDefVO.getUrl() + "，" + jobDefVO.getUsername_param() + "："
						+ jobDefVO.getUsername() + "，" + jobDefVO.getPassword_param() + "：" + jobDefVO.getPassword();
				logger.error(msg);
				throw new BusiException(msg);
			}
			logger.info("http请求返回的数据：" + responseText);
		} catch(Exception e) {
			logger.error("http请求出错，URL:" + jobDefVO.getUrl() + "，" + jobDefVO.getUsername_param() + "："
					+ jobDefVO.getUsername() + "，" + jobDefVO.getPassword_param() + "：" + jobDefVO.getPassword()
					+ "，错误信息：" + e.getMessage());
			e.printStackTrace();
		}
		String convert_clazz = jobDefVO.getConvert_clazz();
		if(StringUtils.isBlank(convert_clazz)) {
			String msg = "任务名称：" + jobDefVO.getJob_name() + "，没有定义业务转换类。";
			logger.error(msg);
			throw new BusiException(msg);
		}
		logger.info("业务转换类：" + convert_clazz);
		IConverter converter = null;
		try {
			Class clazz = Class.forName(convert_clazz);
			converter = (IConverter) clazz.newInstance();
		} catch(Exception e) {
			throw new BusiException("无法实例化业务转换类，类名[?]！",convert_clazz);
		}
		return (List<TrackVO>) converter.convertResponse(responseText).getDataset();	
	}

	/**
	 * 创建webservice请求的任务
	 * 
	 * @param otVO
	 * @return
	 */
	private List<TrackVO> createWebserviceTask(final JobDefVO otVO, Map<String, Object> paramMap) {
		return null;
	}

	public void before(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}

	public void after(JobDefVO jobDefVO) {
		// TODO Auto-generated method stub

	}

}
