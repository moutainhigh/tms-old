package com.tms.service;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.service.impl.AbsDynReportServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.utils.QueryHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;

/**
 * tms动态报表查询抽象类
 * 
 * @author xuqc
 * @date 2014-4-20 上午12:08:19
 */
public abstract class TMSAbsDynReportServiceImpl extends AbsDynReportServiceImpl {

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		String logicCond = buildLogicCondition(paramVO, templetVO);
		if(StringUtils.isNotBlank(logicCond)) {
			cond += " and " + logicCond;
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
}
