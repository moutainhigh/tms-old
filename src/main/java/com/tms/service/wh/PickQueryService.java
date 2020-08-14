package com.tms.service.wh;

import org.nw.service.impl.AbsReportServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.vo.wh.PickDetailVO;

/**
 * 拣货查询
 * 
 * @author xuqc
 * @date 2014-3-31 下午11:34:34
 */
@Service
public class PickQueryService extends AbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PickDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PickDetailVO.PK_PICK_DETAIL);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
}
