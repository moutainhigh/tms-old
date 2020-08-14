package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.AggQueryTempletVO;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.QueryTempletVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.QueryTempletService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2012-12-1 下午02:52:51
 */
@Service
public class QueryTempletServiceImpl extends AbsToftServiceImpl implements QueryTempletService {
	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new AggQueryTempletVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, AggQueryTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, QueryTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, QueryTempletVO.PK_TEMPLET);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, AggQueryTempletVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, QueryConditionVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, "pk_templet");
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_query_condition");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_query_condition");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getItemkey().equals("data_type")) {
					fieldVO.setUserdefine1("afterEditData_type(record)");
				}
			}
		}
		return templetVO;
	}

	public String getCodeFieldCode() {
		return QueryTempletVO.MODEL_CODE;
	}

	protected String getParentPkFieldInChild(ParamVO paramVO) {
		return "pk_templet";
	}

	public boolean useUUIDPrimaryKey() {
		return false;
	}

	private String getBodyOrderBy() {
		return " order by if_immobility desc,if_must desc,disp_sequence";
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		if(paramVO.isBody()) {
			return getBodyOrderBy();
		}
		return super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		return ServiceHelper.queryBillVO(this.getBillInfo(), paramVO, new String[] { getBodyOrderBy() });
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		values.put("pk_corp", WebUtils.getLoginInfo().getPk_corp());
		return values;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyDefaultValues(ParamVO paramVO, Map<String, Object> headerMap) {
		Map<String, Object> bodyMap = super.getBodyDefaultValues(paramVO, headerMap);
		Map<String, Object> oneTabMap = (Map<String, Object>) bodyMap.get("nw_query_condition");
		if(oneTabMap == null) {
			oneTabMap = new HashMap<String, Object>();
			bodyMap.put("nw_query_condition", oneTabMap);
		}
		oneTabMap.put("pk_corp", WebUtils.getLoginInfo().getPk_corp());
		return bodyMap;
	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject vo = copyVO.getParentVO();
		vo.setAttributeValue(QueryTempletVO.MODEL_CODE, null);
		vo.setAttributeValue(QueryTempletVO.NODE_CODE, null);
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject[] childVOs = billVO.getChildrenVO();
		if(childVOs != null && childVOs.length > 0) {
			// 判断不能重复
			List<String> fieldCodeAry = new ArrayList<String>();
			for(CircularlyAccessibleValueObject childVO : childVOs) {
				QueryConditionVO condVO = (QueryConditionVO) childVO;
				if(StringUtils.isNotBlank(condVO.getConsult_code()) && condVO.getConsult_code().startsWith("&lt;")) {
					condVO.setConsult_code(NWUtils.unescape(condVO.getConsult_code()));
				}
				if(childVO.getStatus() != VOStatus.DELETED) {
					if(fieldCodeAry.contains(condVO.getField_code())) {
						throw new BusiException("查询条件[?:?]出现重复，这是不允许的！",condVO.getField_name(),condVO.getField_code());
					}
					fieldCodeAry.add(condVO.getField_code());
				}
			}
		}
	}
}
