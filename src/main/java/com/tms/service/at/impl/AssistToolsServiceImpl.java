package com.tms.service.at.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.at.AssistToolsService;
import com.tms.vo.at.AssistToolsVO;

/**
 * 辅助工具基础信息
 * 
 * @author xuqc
 * @date 2013-6-9 上午09:44:11
 */
@Service
public class AssistToolsServiceImpl extends AbsToftServiceImpl implements AssistToolsService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO parentVO = new VOTableVO();
			parentVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.HEADITEMVO, AssistToolsVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.PKFIELD, "pk_assist_tools");
			parentVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_assist_tools");
			parentVO.setAttributeValue(VOTableVO.VOTABLE, "ts_assist_tools");
			billInfo.setParentVO(parentVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return "code";
	}

}
