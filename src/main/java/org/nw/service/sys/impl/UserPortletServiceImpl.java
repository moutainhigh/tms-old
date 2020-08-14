package org.nw.service.sys.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.CacheUtils;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.UserPortletService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.UserPortletVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 用户门户配置
 * 
 * @author xuqc
 * @date 2013-11-4 下午05:23:06
 * @deprecated
 */
@Service
public class UserPortletServiceImpl extends AbsToftServiceImpl implements UserPortletService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, UserPortletVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, UserPortletVO.PK_USER_PORTLET);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_user_portlet");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_user_portlet");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		StringBuffer condBuf = new StringBuffer(" pk_corp='" + WebUtils.getLoginInfo().getPk_corp() + "' ");
		condBuf.append(" and ").append(" pk_user='" + WebUtils.getLoginInfo().getPk_user() + "'");
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			condBuf.append(" and ");
			condBuf.append(cond);
		}
		return condBuf.toString();
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = "order by column_index,display_order";
		}
		return orderBy;
	}

	public UserPortletVO[] getUserPortlet(String pk_user) {
		UserPortletVO[] upVOs;
		// 配置信息不会经常改变，使用缓存
		if(CacheUtils.isUseCache()) {
			upVOs = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(UserPortletVO.class,
					"pk_user=? and pk_corp=? order by display_order", pk_user, WebUtils.getLoginInfo().getPk_corp());
		} else {
			upVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(UserPortletVO.class,
					"pk_user=? and pk_corp=? order by display_order", pk_user, WebUtils.getLoginInfo().getPk_corp());
		}
		if(upVOs == null || upVOs.length == 0) {
			// 个人没有定义门户信息，使用管理员定义的默认门户
			upVOs = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(UserPortletVO.class,
					"pk_user=? and pk_corp=? order by display_order", Constants.SYSTEM_CODE, Constants.SYSTEM_CODE);
		}
		return upVOs;
	}
}
