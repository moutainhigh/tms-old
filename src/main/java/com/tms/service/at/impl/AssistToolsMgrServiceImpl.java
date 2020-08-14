package com.tms.service.at.impl;

import org.nw.service.impl.AbsBillServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.constants.BillTypeConst;
import com.tms.service.at.AssistToolsMgrService;
import com.tms.vo.at.AssistToolsMgrBVO;
import com.tms.vo.at.AssistToolsMgrVO;

/**
 * 辅助工具管理处理
 * 
 * @author xuqc
 * @date 2013-8-19 下午11:32:24
 */
@Service
public class AssistToolsMgrServiceImpl extends AbsBillServiceImpl implements AssistToolsMgrService {

	public String getBillType() {
		return BillTypeConst.ATM;
	}

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AssistToolsMgrVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AssistToolsMgrVO.PK_ASSIST_TOOLS_MGR);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, AssistToolsMgrBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, AssistToolsMgrBVO.PK_ASSIST_TOOLS_MGR);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_assist_tools_mgr");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_assist_tools_mgr");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

}
