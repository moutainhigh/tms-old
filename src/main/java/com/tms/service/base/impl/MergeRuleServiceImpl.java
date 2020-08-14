package com.tms.service.base.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.springframework.stereotype.Service;

import com.tms.constants.DataDictConst;
import com.tms.service.base.MergeRuleService;
import com.tms.vo.base.ExAggFuelCardVO;
import com.tms.vo.base.ExAggMergeRuleVO;
import com.tms.vo.base.MergeRuleBVO;
import com.tms.vo.base.MergeRuleVO;

@Service
public class MergeRuleServiceImpl extends AbsToftServiceImpl implements MergeRuleService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggFuelCardVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggMergeRuleVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, MergeRuleVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, MergeRuleVO.PK_MERGE_RULE);
			billInfo.setParentVO(vo);

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggMergeRuleVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, MergeRuleBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, MergeRuleBVO.PK_MERGE_RULE);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_merge_rule_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_merge_rule_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO1 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return  MergeRuleVO.CODE;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getItemkey().equals("matter_type1")) {
					// 第一个参数类型
					fieldVO.setUserdefine1("afterEditMatter_type1(record)");
				} else if(fieldVO.getItemkey().equals("matter_type2")) {
					// 第二个参数类型
					fieldVO.setUserdefine1("afterEditMatter_type2(record)");
				}
			}
		}
		return templetVO;
	}
	
	@Override
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ExAggMergeRuleVO exAggMergeRuleVO = (ExAggMergeRuleVO)billVO;
		//唯一性验证
		MergeRuleVO ruleVO = (MergeRuleVO) exAggMergeRuleVO.getParentVO();
		if(ruleVO.getEffective_date().after(ruleVO.getInvalid_date())){
			throw new BusiException("规则生效日期与必须在失效日期之前！");
		}
		MergeRuleVO[] oldRuleVOs = null;
		if(ruleVO.getRule_type() == null){
			oldRuleVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(MergeRuleVO.class, 
					" pk_corp =?", ruleVO.getPk_corp());
		}else{
			oldRuleVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(MergeRuleVO.class, 
					" pk_corp =? and rule_type =?", ruleVO.getPk_corp(),ruleVO.getRule_type());
		}
		
		if(oldRuleVOs != null && oldRuleVOs.length > 0){
			for(MergeRuleVO oldRuleVO : oldRuleVOs){
				if(StringUtils.isNotBlank(ruleVO.getPk_merge_rule()) 
						&& ruleVO.getPk_merge_rule().equals(oldRuleVO.getPk_merge_rule())){
					//排除自身
					continue;
				}
				//判断生效时间段有没有重复
				if(oldRuleVO.getInvalid_date().before(ruleVO.getEffective_date())){
					//就的失效日期在新的生效日期之前 
					if(!(oldRuleVO.getEffective_date().before(ruleVO.getInvalid_date()))){
						throw new BusiException("规则生效时间段与[?]重复！",oldRuleVO.getCode());
					}
					
				}
				
				if(oldRuleVO.getInvalid_date().after(ruleVO.getEffective_date())){
					//就的失效日期在新的生效日期之后 
					if(!(oldRuleVO.getEffective_date().after(ruleVO.getInvalid_date()))){
						throw new BusiException("规则生效时间段与[?]重复！",oldRuleVO.getCode());
					}
				}
			}
		}
		MergeRuleBVO[] ruleBVOs = (MergeRuleBVO[]) exAggMergeRuleVO.getTableVO("ts_merge_rule_b");
		if(ruleBVOs == null || ruleBVOs.length == 0){
			return;
		}
		//生成行号
		int index = 0;
		for (MergeRuleBVO ruleBVO : ruleBVOs) {
			if (ruleBVO.getStatus() != VOStatus.DELETED) {
				index += 10;
				ruleBVO.setSerialno(index);
				if (ruleBVO.getStatus() == VOStatus.UNCHANGED) {
					ruleBVO.setStatus(VOStatus.UPDATED);
				}
			}
		}
	}
	
}
