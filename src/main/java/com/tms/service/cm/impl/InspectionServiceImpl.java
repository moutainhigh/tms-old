package com.tms.service.cm.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.service.cm.InspectionService;
import com.tms.vo.base.CarCapabilityVO;
import com.tms.vo.base.CarMemberVO;
import com.tms.vo.base.CarPartVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.ExAggCarVO;
import com.tms.vo.cm.InspectionBVO;
import com.tms.vo.cm.InspectionVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:49:35
 */
@Service
public class InspectionServiceImpl extends AbsToftServiceImpl implements InspectionService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InspectionVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InspectionVO.PK_INSPECTION);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, InspectionBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, InspectionBVO.PK_INSPECTION);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, InspectionBVO.TS_INSPECTION_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, InspectionBVO.TS_INSPECTION_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

}
