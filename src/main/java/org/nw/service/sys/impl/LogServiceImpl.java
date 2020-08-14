package org.nw.service.sys.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.LogService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.SystemLogVO;
import org.springframework.stereotype.Service;


/**
 * 日志管理操作类
 * 
 * @author xuqc
 * 
 */
@Service
public class LogServiceImpl extends AbsToftServiceImpl implements LogService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, SystemLogVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, SystemLogVO.PK_SYSTEM_LOG);
		billInfo.setParentVO(vo);
		return billInfo;
	}

}
