package org.nw.service.sys.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.PsndocService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.PsndocVO;
import org.springframework.stereotype.Service;


/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午03:39:14
 */
@Service
public class PsndocServiceImpl extends AbsToftServiceImpl implements PsndocService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, PsndocVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, PsndocVO.PK_PSNDOC);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public PsndocVO getByPsncode(String psncode) {
		return dao.queryByCondition(PsndocVO.class, "psncode=?", psncode);
	}

	public String getCodeFieldCode() {
		return PsndocVO.PSNCODE;
	}

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
			String corpCond = CorpHelper.getCurrentCorpWithChildren();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and " + corpCond;
			}
		}
		return fCond;
	}
}
