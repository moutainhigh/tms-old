package com.tms.service.base.impl;

import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.CarTypeService;
import com.tms.vo.base.CarTypeVO;

/**
 * 车型档案
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:47:51
 */
@Service
public class CarTypeServiceImpl extends AbsBaseDataServiceImpl implements CarTypeService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, CarTypeVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, CarTypeVO.PK_CAR_TYPE);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return CarTypeVO.CODE;
	}

	public CarTypeVO getByCode(String code) {
		return dao.queryByCondition(CarTypeVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?", code);
	}

	public CarTypeVO getByName(String name) {
		CarTypeVO[] vos = dao.queryForSuperVOArrayByCondition(CarTypeVO.class,
				"isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and name=?", name);
		if(vos != null && vos.length > 0) {
			return vos[0];
		}
		return null;
	}
}
