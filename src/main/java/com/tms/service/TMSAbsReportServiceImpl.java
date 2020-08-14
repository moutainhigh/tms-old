package com.tms.service;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.service.impl.AbsReportServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.utils.QueryHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.WebUtils;

/**
 * tms报表查询抽象类
 * 
 * @author xuqc
 * @date 2014-4-20 上午12:08:19
 */
public abstract class TMSAbsReportServiceImpl extends AbsReportServiceImpl {

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		String logicCond = buildLogicCondition(paramVO, templetVO);
		if(StringUtils.isNotBlank(logicCond)) {
			cond += " and " + logicCond;
		}
		String customerOrCarrier = getCustomerOrCarrierCond(paramVO);
		if(StringUtils.isNotBlank(customerOrCarrier)) {
			cond += " and " + customerOrCarrier;
		}
		return cond;
	}

	/**
	 * 目前这个逻辑条件只在tms中使用
	 */
	@SuppressWarnings("rawtypes")
	public String buildLogicCondition(ParamVO paramVO, UiQueryTempletVO templetVO) {
		Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
		return buildLogicCondition(clazz, templetVO);
	}

	@SuppressWarnings("rawtypes")
	public String buildLogicCondition(Class clazz, UiQueryTempletVO templetVO) {
		try {
			SuperVO superVO = (SuperVO) clazz.newInstance();
			Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
			if(pk_corp != null) {
				String corpCond = CorpHelper.getCurrentCorpWithChildren(superVO.getTableName());
				String logicCond = QueryHelper.getLogicCond(templetVO);
				if(StringUtils.isNotBlank(logicCond)) {
					corpCond += " or (" + logicCond + ")";
				}
				return "(" + corpCond + ")";
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 客户或者承运商登陆时，只能查询对应的客户或者承运商的数据
	 * 
	 * @param paramVO
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected String getCustomerOrCarrierCond(ParamVO paramVO) {
		UserVO userVO = NWDao.getInstance().queryByCondition(UserVO.class, "pk_user=?",
				WebUtils.getLoginInfo().getPk_user());
		Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
		SuperVO superVO = null;
		try {
			superVO = (SuperVO) clazz.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(superVO != null) {
			if(userVO.getUser_type().intValue() == Constants.USER_TYPE.CUSTOMER.intValue()) {
				// 如果是客户登陆
				Field pk_customer = ReflectionUtils.getDeclaredField(superVO, "pk_customer");
				if(pk_customer != null) {
					return " pk_customer='" + userVO.getPk_customer() + "'";
				}
			} else if(userVO.getUser_type().intValue() == Constants.USER_TYPE.CARRIER.intValue()) {
				// 如果是承运商
				Field pk_carrier = ReflectionUtils.getDeclaredField(superVO, "pk_carrier");
				if(pk_carrier != null) {
					return " pk_carrier='" + userVO.getPk_carrier() + "'";
				}
			}
		}
		return null;
	}
}
