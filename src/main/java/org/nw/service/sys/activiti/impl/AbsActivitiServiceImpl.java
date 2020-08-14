package org.nw.service.sys.activiti.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsBillServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.vo.ParamVO;

/**
 * 基础资料Service，需要一些通用的处理
 * 
 * @author xuqc
 * @date 2012-9-7 下午09:06:55
 */
public abstract class AbsActivitiServiceImpl extends AbsBillServiceImpl {

	/**
	 * 基础资料，可以看到当前公司及其子公司及集团的数据
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and " + cond;
		}
		if(!paramVO.isBody()) {
			String corpCond = CorpHelper.getCurrentCorpWithChildrenAndParent();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and (pk_corp is null or " + corpCond + ") ";
			}
		}
		return fCond;
	}

	protected boolean isLogicalDelete() {
		return false;
	}
}
