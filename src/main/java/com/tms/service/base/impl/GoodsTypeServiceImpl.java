package com.tms.service.base.impl;

import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.GoodsTypeService;
import com.tms.vo.base.GoodsTypeVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-23 下午08:41:10
 */
@Service
public class GoodsTypeServiceImpl extends AbsBaseDataServiceImpl implements GoodsTypeService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, GoodsTypeVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, GoodsTypeVO.PK_GOODS_TYPE);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return GoodsTypeVO.CODE;
	}

	public GoodsTypeVO getByCode(String code) {
		return dao.queryByCondition(GoodsTypeVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?",
				code);
	}
}
