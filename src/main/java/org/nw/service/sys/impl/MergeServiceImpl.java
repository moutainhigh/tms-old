package org.nw.service.sys.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.constants.Constants;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.MergeService;
import org.nw.utils.HttpUtils;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.MergeVO;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2012-10-16 上午09:41:19
 */
@Service
public class MergeServiceImpl extends AbsToftServiceImpl implements MergeService {

	Logger logger = Logger.getLogger(MergeServiceImpl.class);

	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, MergeVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, MergeVO.PK_MERGE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_merge");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_merge");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyDefaultValues(ParamVO paramVO, Map<String, Object> headerMap) {
		Map<String, Object> bodyMap = super.getBodyDefaultValues(paramVO, headerMap);
		Map<String, Object> oneTabMap = (Map<String, Object>) bodyMap.get("nw_merge");
		if(oneTabMap == null) {
			oneTabMap = new HashMap<String, Object>();
			bodyMap.put("nw_merge", oneTabMap);
		}
		oneTabMap.put("type", Constants.PRODUCT_PARAM);
		return bodyMap;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			return " order by type asc, dr desc";
		}
		return orderBy;
	}

	public void execute(String[] pk_merge) {
		if(pk_merge == null || pk_merge.length == 0) {
			return;
		}
		String cond = NWUtils.buildConditionString(pk_merge);
		MergeVO[] mergeVOs = dao.queryForSuperVOArrayByCondition(MergeVO.class, "isnull(dr,0)=0 and pk_merge in "
				+ cond);
		for(MergeVO mergeVO : mergeVOs) {
			String url = mergeVO.getUrl(); // 需要执行的url
			HttpServletRequest request = ServletContextHolder.getRequest();
			url = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + url;
			try {
				logger.info("-----------------------------开始执行压缩，URL：" + url + "-----------------------------");
				String result = HttpUtils.get(url);
				logger.debug(result);
				logger.info("-----------------------------执行完成-----------------------------");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
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
