package org.nw.service.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.utils.CorpHelper;
import org.nw.vo.ParamVO;


/**
 * 基本的service，区别于基础数据的BaseDataServiceImpl，单据的BillServiceImpl
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:42:15
 */
public abstract class AbsCommonServiceImpl extends AbsToftServiceImpl {

	/**
	 * 只能看到当前公司的数据
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and " + cond;
		}
		if(!paramVO.isBody()) {
			String corpCond = CorpHelper.getCurrentCorp();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and " + corpCond;
			}
		}
		return fCond;
	}

}
