package com.tms.service.tp.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import com.tms.BillStatus;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.tp.PZService;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 批量排单导入
 * 
 * @author muyun
 * @date 2016 11 14
 */
public class BatchSchedulingExcelImporter extends BillExcelImporter {
	
	private PZService pzService = SpringContextHolder.getBean("PZServiceImpl");
	
	private ParamVO paramVO;
	
	public BatchSchedulingExcelImporter(ParamVO paramVO, IToftService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
		this.paramVO = paramVO;
	}
	
	protected SuperVO getParentVO() {
		return new PZHeaderVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolveForMultiTable(file);
		//将导入的运段按照批次分组
		Map<Integer,Map<PZHeaderVO,String>> groupMap = new HashMap<Integer, Map<PZHeaderVO,String>>();
		for(AggregatedValueObject aggVO : aggVOs){
			ExAggEntrustVO aggPZVO = (ExAggEntrustVO) aggVO;
			PZHeaderVO headerVO = (PZHeaderVO) aggPZVO.getParentVO();
			SegmentVO segmentVO = (SegmentVO) aggPZVO.getTableVO(TabcodeConst.TS_SEGMENT)[0];
			Integer lotFlag = segmentVO.getVbillstatus();
			//批次分组信息存放在运段的状态字段里
			Map<PZHeaderVO,String> pzInfo = groupMap.get(lotFlag);
			if(pzInfo == null){
				pzInfo = new HashMap<PZHeaderVO, String>();
				pzInfo.put(headerVO, segmentVO.getVbillno());
				groupMap.put(lotFlag, pzInfo);
			}
			//一个批次里面只会有一个头信息
			pzInfo.put(headerVO, segmentVO.getVbillno());
		}
		for(Integer lotFlag : groupMap.keySet()){
			Map<PZHeaderVO,String> pzInfo = groupMap.get(lotFlag);
			List<String> segVbillnos = new ArrayList<String>();
			List<EntTransbilityBVO> entTransbilityBVOs = new ArrayList<EntTransbilityBVO>();
			PZHeaderVO headerVO = new PZHeaderVO();
			//统计车辆信息，以车牌号为准 按照车牌号分组
			for(PZHeaderVO key : pzInfo.keySet()){
				String carno = key.getCarno();
				segVbillnos.add(pzInfo.get(key));
				if(StringUtils.isBlank(headerVO.getPk_carrier())){
					headerVO = key;
					continue;
				}
				//没有车牌号
				if(StringUtils.isNotBlank(carno)
						&& StringUtils.isNotBlank(headerVO.getCarno())
						&& !headerVO.getCarno().equals(carno)){
					//生成一条运力信息
					EntTransbilityBVO entTransbilityBVO = new EntTransbilityBVO();
					entTransbilityBVO.setCarno(headerVO.getCarno());
					entTransbilityBVO.setCertificate_id(headerVO.getCertificate_id());
					entTransbilityBVO.setPk_car_type(headerVO.getPk_car_type());
					entTransbilityBVO.setPk_driver(headerVO.getPk_driver());
					entTransbilityBVO.setDriver_mobile(headerVO.getDriver_mobile());
					entTransbilityBVO.setDriver_name(headerVO.getDriver_name());
					entTransbilityBVO.setMemo(headerVO.getMemo());
					entTransbilityBVOs.add(entTransbilityBVO);
				}
			}
			if(headerVO.getBalatype() == null){
				//看看这个承运商的结算方式是什么
				CarrierVO carrierVO = NWDao.getInstance().queryByCondition(CarrierVO.class, "pk_carrier=?", headerVO.getPk_carrier());
				//这里一般不会出现carrier不存在的情况，但是还是判定一下。
				if(carrierVO == null){
					throw new BusiException("承运商不存在["+ headerVO.getPk_carrier() +"]！");
				}
				headerVO.setBalatype(Integer.parseInt(carrierVO.getBalatype()));
			}
			//获取运段号 未调度 未隐藏
			SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class,
					"vbillno in " + NWUtils.buildConditionString(segVbillnos));
			if(segmentVOs == null || segmentVOs.length == 0){
				throw new BusiException("运段不存在["+NWUtils.buildConditionString(segVbillnos) +"]！");
			}
			for(SegmentVO segmentVO : segmentVOs){
				if(segmentVO.getVbillstatus() != BillStatus.SEG_WPLAN){
					throw new BusiException("运段["+ segmentVO.getVbillno() +"],是非待调度状态,不允许调度！");
				}
				if(segmentVO.getSeg_mark() == SegmentConst.SEG_MARK_PARENT){
					throw new BusiException("运段["+ segmentVO.getVbillno() +"],是父辈运段,不允许调度！");
				}
			}
			ExAggEntrustVO aggPZVO = new ExAggEntrustVO();
			aggPZVO.setParentVO(headerVO);
			aggPZVO.setTableVO(TabcodeConst.TS_SEGMENT, segmentVOs);
			aggPZVO.setTableVO(TabcodeConst.TS_TRANS_BILITY_B, entTransbilityBVOs.toArray(new EntTransbilityBVO[entTransbilityBVOs.size()]));
			pzService.save(aggPZVO, paramVO);
		}
	}
	
	
	
}
