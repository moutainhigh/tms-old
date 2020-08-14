/**
 * 
 */
package org.nw.service.sys.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.PortletPlanService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.PortletPlanBVO;
import org.nw.vo.sys.PortletPlanVO;
import org.springframework.stereotype.Service;

/**
 * @author xuqc
 * @Date 2015年5月15日 下午3:23:43
 *
 */
@Service
public class PortletPlanServiceImple extends AbsToftServiceImpl implements PortletPlanService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PortletPlanVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PortletPlanVO.PK_PORTLET_PLAN);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, PortletPlanBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, PortletPlanBVO.PK_PORTLET_PLAN);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_portlet_plan_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_portlet_plan_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	@Override
	public String getCodeFieldCode() {
		return PortletPlanVO.PLAN_CODE;
	}
}
