package com.tms.web.base;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.CarrService;
import com.tms.vo.base.CarrRateVO;

/**
 * 承运商
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:24:59
 */
@Controller
@RequestMapping(value = "/base/carr")
public class CarrController extends AbsToftController {

	@Autowired
	private CarrService carrService;

	public CarrService getService() {
		return carrService;
	}

//	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
//		super.checkBeforeSave(billVO, paramVO);
//
//		// 运输方式不能相同
//		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
//		if(cvos != null) {
//			Map<String, Integer> typeMap = new HashMap<String, Integer>();
//			int index = 1;
//			for(CircularlyAccessibleValueObject cvo : cvos) {
//				CarrRateVO rateVO = (CarrRateVO) cvo;
//				if(StringUtils.isBlank(rateVO.getPk_trans_type())) {
//					continue;
//				}
//				Integer row = typeMap.get(rateVO.getPk_trans_type());
//				if(row != null) {
//					throw new BusiException("第[?]行的运输方式和第[?]行的运输方式相同，请重新修改！",index+"",row+"");
//				} else {
//					typeMap.put(rateVO.getPk_trans_type(), index);
//				}
//				index++;
//			}
//		}
//	}
}
