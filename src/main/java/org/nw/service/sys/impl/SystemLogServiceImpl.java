package org.nw.service.sys.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.SystemLogService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.SystemLogVO;
import org.springframework.stereotype.Service;


/**
 * 
 * @author xuqc
 * @date 2012-8-8 下午02:38:19
 */
@Service
public class SystemLogServiceImpl extends AbsToftServiceImpl implements SystemLogService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, SystemLogVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, SystemLogVO.PK_SYSTEM_LOG);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
}
