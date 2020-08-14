package org.nw.service.sys.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.RefRelationService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.RefRelationVO;
import org.springframework.stereotype.Service;


/**
 * 
 * @author xuqc
 * @date 2012-8-14 下午01:07:44
 */
@Service
public class RefRelationServiceImpl extends AbsToftServiceImpl implements RefRelationService {
	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, RefRelationVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, RefRelationVO.PK_REF_RELATION);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by referencedtablekey";
		}
		return orderBy;
	}

}
