package com.tms.service.wh;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
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
import com.tms.vo.wh.OutstorageBVO;
import com.tms.vo.wh.OutstorageVO;

/**
 * 出库单
 * 
 * @author xuqc
 * @date 2014-3-4 下午10:38:21
 */
@Service
public class OutstorageService extends TMSAbsBillServiceImpl {

	@Autowired
	private CustService custService;

	public String getBillType() {
		return BillTypeConst.OUTSTO;
	}

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, OutstorageVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, OutstorageVO.PK_OUTSTORAGE);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, OutstorageBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, OutstorageBVO.PK_OUTSTORAGE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_OUTSTORAGE_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_OUTSTORAGE_B);

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
				if(fieldVO.getItemkey().equals(OutstorageVO.VBILLSTATUS)) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals(OutstorageVO.REQ_SHIP_DATE)) {
					// 要求发货日期
					fieldVO.setBeforeRenderer("req_ship_dateBeforeRenderer");
				}
			}
		}
		return templetVO;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		// 设置表体行的行号，实际上也是vbillno和状态
		OutstorageVO parentVO = (OutstorageVO) billVO.getParentVO();
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos != null && cvos.length > 0) {
			for(int i = 0; i < cvos.length; i++) {
				OutstorageBVO childVO = (OutstorageBVO) cvos[i];
				if(StringUtils.isBlank(childVO.getPk_outstorage_b())) {
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

	/**
	 * 关闭单据
	 * 
	 * @param paramVO
	 * @return
	 */
	@Transactional
	public Map<String, Object> close(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		OutstorageVO parentVO = (OutstorageVO) billVO.getParentVO();
		if(parentVO.getPicked_count() != null && parentVO.getPicked_count().doubleValue() != 0) {
			throw new BusiException("该出库单的已分配数量不为0，不能关闭！");
		}
		parentVO.setStatus(VOStatus.UPDATED);
		parentVO.setAttributeValue(getBillStatusField(), BillStatus.OUTSTO_CLOSED); // 设置成关闭
		dao.saveOrUpdate(billVO);
		return execFormula4Templet(billVO, paramVO);
	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject parentVO = copyVO.getParentVO();
		parentVO.setAttributeValue(OutstorageVO.PICKED_COUNT, null); // 表头的总分配数量
		parentVO.setAttributeValue(OutstorageVO.SHIPED_COUNT, null); // 表头的总发货数量
		CircularlyAccessibleValueObject[] childVOs = copyVO.getChildrenVO();
		if(childVOs != null && childVOs.length != 0) {
			for(CircularlyAccessibleValueObject childVO : childVOs) {
				childVO.setAttributeValue(OutstorageBVO.PICKED_COUNT, null);// 分配数量
				childVO.setAttributeValue(OutstorageBVO.SHIPED_COUNT, null);// 发货数量
				childVO.setAttributeValue(OutstorageBVO.VBILLNO, null);
				childVO.setAttributeValue(OutstorageBVO.VBILLSTATUS, null);
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
}
