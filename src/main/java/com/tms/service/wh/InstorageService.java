package com.tms.service.wh;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CustService;
import com.tms.vo.wh.InstorageBVO;
import com.tms.vo.wh.InstorageVO;
import com.tms.vo.wh.OutstorageBVO;

/**
 * 使用声明式事务,不再定义一个接口了
 * 
 * @author xuqc
 * @date 2014-3-4 下午10:38:21
 */
@Service
public class InstorageService extends TMSAbsBillServiceImpl {

	@Autowired
	private CustService custService;

	public String getBillType() {
		return BillTypeConst.INSTO;
	}

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InstorageVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InstorageVO.PK_INSTORAGE);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, InstorageBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, InstorageBVO.PK_INSTORAGE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_INSTORAGE_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_INSTORAGE_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals(InstorageVO.VBILLSTATUS)) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals(InstorageVO.EST_ARRI_DATE)) {
					// 预计到货日期
					fieldVO.setBeforeRenderer("est_arri_dateBeforeRenderer");
				}
			}
		}
		return templetVO;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		// 设置表体行的行号，实际上也是vbillno和状态
		InstorageVO parentVO = (InstorageVO) billVO.getParentVO();
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos != null && cvos.length > 0) {
			for(int i = 0; i < cvos.length; i++) {
				InstorageBVO childVO = (InstorageBVO) cvos[i];
				if(StringUtils.isBlank(childVO.getPk_instorage_b())) {
					childVO.setVbillno(parentVO.getVbillno() + "_" + (i + 1));// 固定这个格式
					childVO.setVbillstatus(BillStatus.NEW);
				}

				// 子表的客户订单号、订单号、客户号从表头上带下来
				childVO.setCust_orderno(parentVO.getCust_orderno());
				childVO.setOrderno(parentVO.getOrderno());
				childVO.setPk_customer(parentVO.getPk_customer());
				if(childVO.getStatus() == VOStatus.UNCHANGED) {
					childVO.setStatus(VOStatus.UPDATED);
				}
			}
		}
	}

	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, String orderBy) {
		super.processAfterExecFormula(list, paramVO, orderBy);
		for(Map<String, Object> map : list) {
			Object pk_customer = map.get("pk_customer");
			if(pk_customer != null) {
				map.put("cust_name", custService.getNameString(pk_customer.toString()));
			}
		}
	}

	/**
	 * 关闭单据
	 * 
	 * @param paramVO
	 * @return
	 */
	@Transactional
	public Map<String, Object> close(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.INSTO_CLOSED); // 设置成关闭
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject parentVO = copyVO.getParentVO();
		parentVO.setAttributeValue(InstorageVO.ACCEPTED_COUNT, null); // 总的接收数量设置为null
		CircularlyAccessibleValueObject[] childVOs = copyVO.getChildrenVO();
		if(childVOs != null && childVOs.length != 0) {
			for(CircularlyAccessibleValueObject childVO : childVOs) {
				childVO.setAttributeValue(InstorageBVO.ACCEPTED_COUNT, null);
				childVO.setAttributeValue(OutstorageBVO.VBILLNO, null);
				childVO.setAttributeValue(OutstorageBVO.VBILLSTATUS, null);
			}
		}
	}
}
