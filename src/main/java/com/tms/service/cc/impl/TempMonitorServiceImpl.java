package com.tms.service.cc.impl;



import java.util.List;

import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cc.TempMonitorService;
import com.tms.vo.te.EntLotTrackingBVO;

@Service
public class TempMonitorServiceImpl extends TMSAbsBillServiceImpl implements TempMonitorService {

	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, EntLotTrackingBVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, "pk_ent_lot_track_b");
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_ent_lot_track_b");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_ent_lot_track_b");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	
	public String getBillType() {
		return null;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public String buildLogicCondition(Class clazz, UiQueryTempletVO templetVO) {
		String superCond = super.buildLogicCondition(clazz, templetVO);
		return superCond + " AND ts_ent_lot_track_b.lot IS NOT NULL ";
	}
	
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("speed_status")) {
					fieldVO.setBeforeRenderer("speed_statusBeforeRenderer");
				}else if (fieldVO.getItemkey().equals("temp_status")) {
					fieldVO.setBeforeRenderer("temp_statusBeforeRenderer");
				}
			}
		}
		return templetVO;
	}


	public EntLotTrackingBVO[] getAjaxData(String[] ids) {
		if(ids == null || ids.length == 0){
			return null;
		}
		EntLotTrackingBVO[] trackingBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLotTrackingBVO.class,
				"pk_ent_lot_track_b in " + NWUtils.buildConditionString(ids));
		return trackingBVOs;
	}

	
}
