package org.nw.service.impl;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.utils.CorpHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;

/**
 * 基础资料Service，需要一些通用的处理
 * 
 * @author xuqc
 * @date 2012-9-7 下午09:06:55
 */
public abstract class AbsBaseDataServiceImpl extends AbsToftServiceImpl {

	/**
	 * 基础资料，可以看到当前公司及其子公司及集团的数据
	 */
	@SuppressWarnings("rawtypes")
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and " + cond;
		}
		if(!paramVO.isBody()) {
			boolean useDefaultCorpCondition = useDefaultCorpCondition(paramVO);
			if(useDefaultCorpCondition) {
				Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
				try {
					SuperVO superVO = (SuperVO) clazz.newInstance();
					Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
					if(pk_corp != null) {
						String corpCond = getCorpCondition(superVO.getTableName(), paramVO, templetVO);
						if(StringUtils.isNotBlank(corpCond)) {
							fCond += " and " + corpCond;
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return fCond;
	}

	/*
	 * 基础数据，默认的规则是，可以使用本公司及其子公司以及父公司的数据
	 * 
	 * @see
	 * org.nw.service.impl.AbsToftServiceImpl#getCorpCondition(org.nw.vo.ParamVO
	 * , org.nw.jf.vo.UiQueryTempletVO)
	 */
	public String getCorpCondition(String tablePrefix, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return CorpHelper.getCurrentCorpWithChildrenAndGroup();
	}

	@SuppressWarnings("unchecked")
	public SuperVO getByCode(String code) {
		AggregatedValueObject billInfo = this.getBillInfo();
		CircularlyAccessibleValueObject parentVO = billInfo.getParentVO();
		if(parentVO == null) {
			// 没有表头，当作单表体处理
			CircularlyAccessibleValueObject[] cvos = billInfo.getChildrenVO();
			if(cvos != null && cvos.length > 0) {
				parentVO = cvos[0];
			}
		}
		Class<? extends SuperVO> parentVOClass = null;
		try {
			parentVOClass = (Class<? extends SuperVO>) Class.forName(parentVO.getAttributeValue(VOTableVO.HEADITEMVO)
					.toString().trim());
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(parentVOClass == null) {
			throw new BusiException("无法实例化parentVO，请检查billInfo配置！");
		}
		String codeFieldCode = this.getCodeFieldCode();
		if(StringUtils.isBlank(codeFieldCode)) {
			throw new BusiException("没有继承getCodeFieldCode方法返回code字段名，无法根据code查询VO！");
		}
		String where = this.getCodeFieldCode() + "=?";
		String corpCond = CorpHelper.getCurrentCorpWithChildrenAndParent();
		if(StringUtils.isNotBlank(corpCond)) {
			where += " and " + corpCond;
		}
		return NWDao.getInstance().queryByCondition(parentVOClass, where, code);
	}
}
