package org.nw.service.sys.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.PortletService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.PortletVO;
import org.springframework.stereotype.Service;

/**
 * 门户组件定义
 * 
 * @author xuqc
 * @date 2014-5-12 下午09:56:47
 */
@Service
public class PortletServiceImpl extends AbsToftServiceImpl implements PortletService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO parentVO = new VOTableVO();
			parentVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.HEADITEMVO, PortletVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.PKFIELD, PortletVO.PK_PORTLET);
			parentVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_portlet");
			parentVO.setAttributeValue(VOTableVO.VOTABLE, "nw_portlet");
			billInfo.setParentVO(parentVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return PortletVO.PORTLET_CODE;
	}
}
