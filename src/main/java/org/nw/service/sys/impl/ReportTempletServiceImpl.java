package org.nw.service.sys.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.service.ServiceHelper;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.ReportTempletService;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.ReportTempletBVO;
import org.nw.vo.sys.ReportTempletVO;
import org.springframework.stereotype.Service;

/**
 * 报表模板处理
 * 
 * @author xuqc
 * @date 2013-7-28 下午11:29:52
 */
@Service
public class ReportTempletServiceImpl extends AbsToftServiceImpl implements ReportTempletService {

	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ReportTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ReportTempletVO.PK_REPORT_TEMPLET);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ReportTempletBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ReportTempletBVO.PK_REPORT_TEMPLET);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_report_templet_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_report_templet_b");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return ReportTempletVO.VTEMPLATECODE;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		if(paramVO.isBody()) {
			return getBodyOrderBy();
		}
		return super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
	}

	private String getBodyOrderBy() {
		return " order by display_order";
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		return ServiceHelper.queryBillVO(this.getBillInfo(), paramVO, new String[] { getBodyOrderBy() });
	}

	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		for(CircularlyAccessibleValueObject vo : cvos) {
			ReportTempletBVO fieldVO = (ReportTempletBVO) vo;
			if(StringUtils.isNotBlank(fieldVO.getOptions())) {
				fieldVO.setOptions(NWUtils.unescape(fieldVO.getOptions()));
			}
		}
		return super.save(billVO, paramVO);
	}
}
