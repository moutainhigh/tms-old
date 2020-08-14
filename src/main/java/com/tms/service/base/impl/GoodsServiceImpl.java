package com.tms.service.base.impl;

import java.util.List;

import org.nw.basic.util.StringUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.GoodsService;
import com.tms.vo.base.GoodsPackRelaVO;
import com.tms.vo.base.GoodsVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-23 下午09:07:57
 */
@Service
public class GoodsServiceImpl extends AbsBaseDataServiceImpl implements GoodsService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, GoodsVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, GoodsVO.PK_GOODS);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, GoodsPackRelaVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, GoodsPackRelaVO.PK_GOODS);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_goods_pack_rela");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_goods_pack_rela");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return GoodsVO.GOODS_CODE;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if((fieldVO.getItemkey().equals("length") || fieldVO.getItemkey().equals("width") || fieldVO
						.getItemkey().equals("height"))) {
					// 长、宽、高，计算体积
					fieldVO.setUserdefine1("afterChangeLengthOrWidthOrHeight()");
				}
			}
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if((fieldVO.getItemkey().equals("length") || fieldVO.getItemkey().equals("width") || fieldVO
						.getItemkey().equals("height"))) {
					// 长、宽、高，计算体积
					fieldVO.setUserdefine1("afterEditLengthOrWidthOrHeight(record)");
				} else if(fieldVO.getItemkey().equals("goods_packcorp_name")) {
					// 编辑货品包装后，需要判定是否已经存在该包装了
					fieldVO.setUserdefine1("afterEditGoodsPackcorpName(record)");
				}
			}

		}
		return templetVO;
	}

	public GoodsVO getByCode(String code) {
		return dao.queryByCondition(GoodsVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and goods_code=?",
				code);
	}

	public GoodsPackRelaVO getGoodsPackRelaVO(String pk_goods, String pk_goods_packcorp) {
		GoodsPackRelaVO[] vos = dao.queryForSuperVOArrayByCondition(GoodsPackRelaVO.class,
				"pk_goods=? and pk_goods_packcorp=?", pk_goods, pk_goods_packcorp);
		if(vos.length > 0) {
			return vos[0];
		}
		return null;
	}
	
	public GoodsVO getByGoodsCodeCustomCode(String goodsCode,String customCode) {
		if(StringUtils.isBlank(customCode)){
			return dao.queryByCondition(GoodsVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and goods_code=? and pk_customer is null ",
					goodsCode );
		}else{
			return dao.queryByCondition(GoodsVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and goods_code=? and pk_customer=?",
					goodsCode,customCode);
		}
		
	}
}
