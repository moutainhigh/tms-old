package org.nw.service.sys.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.RefinfoService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.RefInfoVO;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2013-8-1 下午05:11:01
 */
@Service
public class RefinfoServiceImpl extends AbsToftServiceImpl implements RefinfoService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, RefInfoVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, "pk_refinfo");
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by code";
		}
		return orderBy;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		values.put("module", "uap");
		values.put("resid", "UPPref-000469");
		values.put("reftype", 1);
		values.put("residpath", "ref");
		return values;
	}

	public String getCodeFieldCode() {
		return "code";
	}

	public boolean useUUIDPrimaryKey() {
		return false;
	}
}
