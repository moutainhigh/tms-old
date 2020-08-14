package com.tms.service.base.impl;

import java.util.HashMap;
import java.util.Map;

import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFDouble;
import org.springframework.stereotype.Service;

import com.tms.service.base.TransTypeService;
import com.tms.vo.base.TransTypeVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:53:46
 */
@Service
public class TransTypeServiceImpl extends AbsBaseDataServiceImpl implements TransTypeService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, TransTypeVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, TransTypeVO.PK_TRANS_TYPE);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return TransTypeVO.CODE;
	}

	public TransTypeVO getByCode(String code) {
		return dao.queryByCondition(TransTypeVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?",
				code);
	}

	// 运输方式和体积重换算比率一般不会改变
	public TransTypeVO[] getAllTransTypeVOs() {
		return dao.queryForSuperVOArrayByConditionWithCache(TransTypeVO.class, "isnull(locked_flag,'N')='N'");
	}


}
