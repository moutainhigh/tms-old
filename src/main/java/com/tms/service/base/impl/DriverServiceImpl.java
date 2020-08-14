package com.tms.service.base.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.service.base.DriverService;
import com.tms.vo.base.CarrRateVO;
import com.tms.vo.base.DriverPayBVO;
import com.tms.vo.base.DriverVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:46:37
 */
@Service
public class DriverServiceImpl extends AbsBaseDataServiceImpl implements DriverService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, DriverVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, DriverVO.PK_DRIVER);
			billInfo.setParentVO(vo);
			
//			VOTableVO childVO = new VOTableVO();
//			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
//			childVO.setAttributeValue(VOTableVO.HEADITEMVO, DriverPayBVO.class.getName());
//			childVO.setAttributeValue(VOTableVO.PKFIELD, DriverPayBVO.PK_DRIVER);
//			childVO.setAttributeValue(VOTableVO.ITEMCODE, DriverPayBVO.TS_DRIVER_PAY_B);
//			childVO.setAttributeValue(VOTableVO.VOTABLE,  DriverPayBVO.TS_DRIVER_PAY_B);
//			
//			CircularlyAccessibleValueObject[] childrenVO = { childVO };
//			billInfo.setChildrenVO(childrenVO);
			
			
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return DriverVO.DRIVER_CODE;
	}

	public DriverVO getByCode(String code) {
		return dao.queryByCondition(DriverVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and driver_code=?",
				code);
	}
	
	@Override
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String superCond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_carrier())){
			superCond += " and pk_carrier='" +  WebUtils.getLoginInfo().getPk_carrier() + "'";
		}
		return superCond;
	}
}
