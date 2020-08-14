package com.tms.service.base.impl;

import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.GoodsPackcorpService;
import com.tms.vo.base.GoodsPackcorpVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-23 下午08:47:22
 */
@Service
public class GoodsPackcorpServiceImpl extends AbsBaseDataServiceImpl implements GoodsPackcorpService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, GoodsPackcorpVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, GoodsPackcorpVO.PK_GOODS_PACKCORP);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return GoodsPackcorpVO.CODE;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]
					&& (fieldVO.getItemkey().equals("length") || fieldVO.getItemkey().equals("width") || fieldVO
							.getItemkey().equals("height"))) {
				// 长、宽、高，计算体积
				fieldVO.setUserdefine1("afterChangeLengthOrWidthOrHeight()");
			}
		}
		return templetVO;
	}

	public GoodsPackcorpVO getByCode(String code) {
		return dao.queryByCondition(GoodsPackcorpVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?",
				code);
	}
}
