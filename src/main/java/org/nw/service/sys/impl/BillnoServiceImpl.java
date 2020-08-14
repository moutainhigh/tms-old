package org.nw.service.sys.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.BillnoService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.BillnoRuleVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 单据号规则、单据号
 * 
 * @author xuqc
 * @date 2012-7-7 下午09:47:45
 */
@Service
public class BillnoServiceImpl extends AbsToftServiceImpl implements BillnoService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, BillnoRuleVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, BillnoRuleVO.PK_BILLNO_RULE);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public BillnoRuleVO getByBillType(String bill_type) {
		String where = "isnull(dr,0)=0 and bill_type=? ";
		String corpCond;
		if(WebUtils.getLoginInfo() == null) {
			// 没有登录
			corpCond = " pk_corp='" + Constants.SYSTEM_CODE + "'";
		} else {
			corpCond = CorpHelper.getCurrentCorpWithChildrenAndParent();
		}
		if(StringUtils.isNotBlank(corpCond)) {
			where += " and " + corpCond;
		}
		BillnoRuleVO[] ruleVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(BillnoRuleVO.class, where,
				bill_type);
		if(ruleVOs == null || ruleVOs.length == 0) {
			return null;
		}
		// 这里的排序有点复杂，真正想要的顺序是，自己公司的规则，如果没有，那么上级公司的规则，一直到集团。
		// 在CorpHelper中查询公司的时候，实际上就是根据公司编码进行排序的，保证子公司都排在前面，从而查询的规则也是子公司在前面
		return ruleVOs[0];
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and ";
			fCond += cond;
		}
		// 根据当前登录用户的公司，查找所有子公司，不分级次
		String corpCond = CorpHelper.getCurrentCorpWithGroup();
		if(StringUtils.isNotBlank(corpCond)) {
			fCond += " and " + corpCond;
		}
		return fCond;
	}
}
