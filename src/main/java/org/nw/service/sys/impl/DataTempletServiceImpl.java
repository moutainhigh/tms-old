package org.nw.service.sys.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.DataTempletService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.DataTempletVO;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2012-11-21 下午07:39:56
 */
@Service
public class DataTempletServiceImpl extends AbsToftServiceImpl implements DataTempletService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, DataTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, DataTempletVO.PK_DATATEMPLET);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
}
