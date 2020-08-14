package com.tms.service.base.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.service.base.CarService;
import com.tms.vo.base.CarCapabilityVO;
import com.tms.vo.base.CarMemberVO;
import com.tms.vo.base.CarPartVO;
import com.tms.vo.base.CarPayBVO;
import com.tms.vo.base.CarVO;
import com.tms.vo.base.ExAggCarVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:49:35
 */
@Service
public class CarServiceImpl extends AbsBaseDataServiceImpl implements CarService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCarVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggCarVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, CarVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, CarVO.PK_CAR);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggCarVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, CarPartVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, CarPartVO.PK_CAR);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_car_part");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_car_part");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggCarVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, CarMemberVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, CarMemberVO.PK_CAR);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_car_member");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_car_member");
			
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggCarVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, CarCapabilityVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, CarCapabilityVO.PK_CAR);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_car_capability");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_car_capability");
			
			VOTableVO childVO3 = new VOTableVO();
			childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggCarVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.HEADITEMVO, CarPayBVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.PKFIELD, CarPayBVO.PK_CAR);
			childVO3.setAttributeValue(VOTableVO.ITEMCODE, CarPayBVO.TS_CAR_PAY_B);
			childVO3.setAttributeValue(VOTableVO.VOTABLE, CarPayBVO.TS_CAR_PAY_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1,childVO2,childVO3 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0] && fieldVO.getItemkey().equals("car_prop")) {
				// 表头的车辆性质
				fieldVO.setDefaultvalue("1"); // 默认值是"只有"
			}
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]
					&& (fieldVO.getItemkey().equals("quantity") || fieldVO.getItemkey().equals("price"))) {
				// 编辑数量和单价时，计算总价值
				fieldVO.setUserdefine1("afterEditQuantityOrPrice(record)");
			}
		}
		return templetVO;
	}

	public CarVO getByCarno(String carno) {
		return dao.queryByCondition(CarVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and carno=?", carno);
	}

	public String getCodeFieldCode() {
		return CarVO.CARNO;
	}
	
	@Override
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String superCond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(WebUtils.getLoginInfo().getPk_carrier()) 
				&& paramVO.isBody() == false){
			superCond += " and pk_carrier='" +  WebUtils.getLoginInfo().getPk_carrier() + "'";
		}
		return superCond;
	}
}
