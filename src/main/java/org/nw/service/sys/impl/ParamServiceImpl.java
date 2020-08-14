package org.nw.service.sys.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.ParamService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ParameterVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 系统参数操作类
 * 
 * @author xuqc
 * 
 */
@Service
public class ParamServiceImpl extends AbsToftServiceImpl implements ParamService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ParameterVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ParameterVO.PK_PARAMETER);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_parameter");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_parameter");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject[] vos = billVO.getChildrenVO();
		if(vos != null && vos.length > 0) {
			for(CircularlyAccessibleValueObject vo : vos) {
				ParameterVO parameterVO = (ParameterVO) vo;
				if(parameterVO.getStatus() == VOStatus.DELETED && parameterVO.getType() != null
						&& parameterVO.getType().intValue() == 0) {
					throw new BusiException("平台级参数不能删除！");
				}
				if(parameterVO.getStatus() == VOStatus.DELETED || parameterVO.getStatus() == VOStatus.UPDATED) {
					String currentCorp = WebUtils.getLoginInfo().getPk_corp();
					if(currentCorp != Constants.SYSTEM_CODE && !currentCorp.equals(parameterVO.getPk_corp())) {
						throw new BusiException("只能修改本公司的参数！");
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyDefaultValues(ParamVO paramVO, Map<String, Object> headerMap) {
		Map<String, Object> bodyMap = super.getBodyDefaultValues(paramVO, headerMap);
		Map<String, Object> oneTabMap = (Map<String, Object>) bodyMap.get("nw_parameter");
		if(oneTabMap == null) {
			oneTabMap = new HashMap<String, Object>();
			bodyMap.put("nw_parameter", oneTabMap);
		}
		oneTabMap.put("pk_corp", WebUtils.getLoginInfo().getPk_corp());
		oneTabMap.put("type", Constants.PRODUCT_PARAM);
		return bodyMap;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			return " order by type, create_time";
		}
		return orderBy;
	}

	/**
	 * 只能看到当前公司的数据
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and " + cond;
		}
		String corpCond = CorpHelper.getCurrentCorpWithGroup();
		if(StringUtils.isNotBlank(corpCond)) {
			fCond += " and " + corpCond;
		}
		return fCond;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.nw.service.impl.AbsToftServiceImpl#buildOperatorColumn(org.nw.jf.
	 * vo.UiBillTempletVO)
	 */
	protected void buildOperatorColumn(UiBillTempletVO uiBillTempletVO) {

	}
}
