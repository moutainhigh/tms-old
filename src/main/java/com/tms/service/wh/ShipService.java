package com.tms.service.wh;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.utils.BillnoHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransactionConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.base.CustService;
import com.tms.vo.wh.ExAggOutstorageVO;
import com.tms.vo.wh.LotQtyVO;
import com.tms.vo.wh.OutstorageBVO;
import com.tms.vo.wh.OutstorageVO;
import com.tms.vo.wh.PickDetailVO;
import com.tms.vo.wh.TransactionVO;

/**
 * 发货
 * 
 * @author xuqc
 * @date 2014-3-7 下午10:31:16
 */
@Service
public class ShipService extends TMSAbsBillServiceImpl {

	@Autowired
	private CustService custService;

	public String getBillType() {
		return BillTypeConst.OUTSTO;
	}

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggOutstorageVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, ExAggOutstorageVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, OutstorageVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, OutstorageVO.PK_OUTSTORAGE);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggOutstorageVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, OutstorageBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, OutstorageBVO.PK_OUTSTORAGE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_OUTSTORAGE_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_OUTSTORAGE_B);

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggOutstorageVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, PickDetailVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, OutstorageBVO.PK_OUTSTORAGE);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_PICK_DETAIL);
			childVO1.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_PICK_DETAIL);

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
					if(fieldVO.getItemkey().equals(OutstorageVO.VBILLSTATUS)) {
						fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
					} else if(fieldVO.getItemkey().equals(OutstorageVO.REQ_SHIP_DATE)) {
						// 要求发货日期
						fieldVO.setBeforeRenderer("req_ship_dateBeforeRenderer");
					}
				}
			}
		}
		// 增加行操作列，包括展开的图标
		BillTempletBVO processor = new BillTempletBVO();
		processor.setItemkey("_processor");
		processor.setDefaultshowname("操作");
		processor.setDatatype(UiConstants.DATATYPE.TEXT.intValue());
		processor.setListflag(Constants.YES);
		processor.setCardflag(Constants.YES);
		processor.setListshowflag(new UFBoolean(true));
		processor.setShowflag(Constants.YES);
		processor.setEditflag(Constants.NO);
		processor.setLockflag(Constants.NO);
		processor.setReviseflag(new UFBoolean(false));
		processor.setTotalflag(Constants.NO);
		processor.setNullflag(Constants.NO);
		processor.setWidth(30);
		processor.setDr(0);
		processor.setPos(UiConstants.POS[1]);
		processor.setTable_code(TabcodeConst.TS_OUTSTORAGE_B);
		processor.setPk_billtemplet(templateID);
		processor.setPk_billtemplet_b(UUID.randomUUID().toString());
		templetVO.getFieldVOs().add(0, processor);
		return templetVO;
	}

	/**
	 * 发货,通过选择出库单明细进行发货<br/>
	 * 1、查找分配明细，分配数量=0，发货数量=分配数量<br/>
	 * 2、查询库存QTY，分配数量=分配数量-分配明细的分配数量，库存数量=库存数量-分配明细的分配数量<br/>
	 * 3、出库单明细，分配数量=分配数量-分配明细的分配数量，发货数量=发货数量+分配明细的分配数量<br/>
	 * 4、出库单，分配数量=分配数量-明细的分配数量，发货数量=发货数量+明细的发货数量<br/>
	 * 5、往交易表插入一条记录
	 * 
	 * @param pk_outstorage_b
	 * @return
	 */
	@Transactional
	public void ship(String pk_outstorage_b) {
		logger.info("发货，pk_outstorage_b" + pk_outstorage_b);
		if(StringUtils.isBlank(pk_outstorage_b)) {
			return;
		}
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "pk_outstorage_b=?",
				pk_outstorage_b);
		if(childVO == null) {
			throw new BusiException("该出库单明细记录已经被删除，不能发货！");
		}
		PickDetailVO[] pdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PickDetailVO.class,
				"pk_outstorage_b=?", pk_outstorage_b);
		if(pdVOs == null || pdVOs.length == 0) {
			throw new BusiException("该出库单明细记录没有对应的分配明细，不需要发货！");
		}
		for(PickDetailVO pdVO : pdVOs) {
			ship1(pdVO.getPk_pick_detail(), false);
		}
	}

	/**
	 * 发货，通过选择分配明细进行发货<br/>
	 * 1、分配明细，分配数量=0，发货数量=分配数量<br/>
	 * 2、查询库存QTY，分配数量=分配数量-分配明细的分配数量，库存数量=库存数量-分配明细的分配数量<br/>
	 * 3、出库单明细，分配数量=分配数量-分配明细的分配数量，发货数量=发货数量+分配明细分配数量<br/>
	 * 4、出库单，分配数量=分配数量-明细的分配数量，发货数量=发货数量+明细的发货数量<br/>
	 * 5、往交易表插入一条记录
	 * 
	 * @param pk_pick_detail
	 * @param throwEx
	 *            是否在分配明细没有可发货的数量时抛出异常，如果选中明细记录进行发货，那么不需要抛出异常，以为一个明细对应多条分配明细
	 */
	@Transactional
	public void ship1(String pk_pick_detail, boolean throwEx) {
		logger.info("通过选择分配明细进行发货，pk_pick_detail" + pk_pick_detail);
		if(StringUtils.isBlank(pk_pick_detail)) {
			return;
		}
		PickDetailVO pdVO = NWDao.getInstance()
				.queryByCondition(PickDetailVO.class, "pk_pick_detail=?", pk_pick_detail);
		if(pdVO == null) {
			throw new BusiException("该分配明细已经被删除，不能发货！");
		}
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "pk_outstorage_b=?",
				pdVO.getPk_outstorage_b());
		if(childVO == null) {
			throw new BusiException("该出库单明细记录已经被删除，不能发货！");
		}
		OutstorageVO parentVO = NWDao.getInstance().queryByCondition(OutstorageVO.class, "pk_outstorage=?",
				childVO.getPk_outstorage());
		if(parentVO == null) {
			throw new BusiException("该出库单已经被删除，不能发货，出库单明细号[?]！",childVO.getVbillno());
		}
		// 主表的分配数量
		UFDouble header_picked_count = parentVO.getPicked_count() == null ? new UFDouble(0) : parentVO
				.getPicked_count();
		// 主表的发货数量
		UFDouble header_shiped_count = parentVO.getShiped_count() == null ? new UFDouble(0) : parentVO
				.getShiped_count();
		// 主表的订单数量
		UFDouble header_order_count = parentVO.getOrder_count() == null ? new UFDouble(0) : parentVO.getOrder_count();

		// 子表的分配数量
		UFDouble body_picked_count = childVO.getPicked_count() == null ? new UFDouble(0) : childVO.getPicked_count();
		// 子表的发货数量
		UFDouble body_shiped_count = childVO.getShiped_count() == null ? new UFDouble(0) : childVO.getShiped_count();
		// 子表的订单数量
		UFDouble body_order_count = childVO.getOrder_count() == null ? new UFDouble(0) : childVO.getOrder_count();

		UFDouble picked_count = pdVO.getPicked_count();
		if(picked_count == null || picked_count.doubleValue() == 0) {
			if(throwEx) {
				throw new BusiException("该分配明细没有的分配数量为0，不能发货！");
			} else {
				return;
			}
		}
		// 1、分配明细，分配数量=0，发货数量=分配数量
		pdVO.setShiped_count(picked_count);
		pdVO.setPicked_count(new UFDouble(0));
		pdVO.setVbillstatus(BillStatus.OUTSTO_SHIPED);// 设置成已发货
		pdVO.setShip_date(new UFDate(new Date()));
		pdVO.setShip_user(WebUtils.getLoginInfo().getPk_user());
		pdVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(pdVO);

		// 2、插入交易表
		TransactionVO tVO = new TransactionVO();
		tVO.setDbilldate(new UFDate());
		tVO.setRelateid(parentVO.getPk_outstorage());
		tVO.setVbillno(BillnoHelper.generateBillnoByDefault(BillTypeConst.TRANS));
		tVO.setTrans_type(TransactionConst.TRANS_TYPE.OUT.toString());// 交易类型为发货
		tVO.setPk_customer(childVO.getPk_customer());
		tVO.setPk_goods(childVO.getPk_goods());
		tVO.setLot(pdVO.getLot());
		tVO.setFrom_loc(childVO.getPk_goods_allocation());
		tVO.setFrom_lpn(childVO.getLpn());
		tVO.setOrder_type(parentVO.getOrder_type());
		tVO.setVstatus(TransactionConst.STATUS.OK.toString());// 实际设置为OK
		tVO.setGoods_prop(childVO.getGoods_prop());
		tVO.setPk_supplier(childVO.getPk_supplier());
		tVO.setLot_attr1(childVO.getLot_attr1());
		tVO.setLot_attr2(childVO.getLot_attr2());
		tVO.setLot_attr3(childVO.getLot_attr3());
		tVO.setLot_attr4(childVO.getLot_attr4());
		tVO.setLot_attr5(childVO.getLot_attr5());
		tVO.setLot_attr6(childVO.getLot_attr6());
		tVO.setLot_attr7(childVO.getLot_attr7());
		tVO.setLot_attr8(childVO.getLot_attr8());
		tVO.setLot_attr9(childVO.getLot_attr9());
		tVO.setLot_attr10(childVO.getLot_attr10());
		tVO.setLot_attr11(childVO.getLot_attr11());
		tVO.setProduce_date(childVO.getProduce_date());
		tVO.setExpire_date(childVO.getExpire_date());
		tVO.setReceipt_date(new UFDate(new Date()));
		tVO.setQuantity(picked_count);// 分配数量
		tVO.setPack(childVO.getPack());
		tVO.setMin_pack(childVO.getMin_pack());
		tVO.setUnit_weight(childVO.getUnit_weight());
		tVO.setUnit_volume(childVO.getUnit_volume());
		tVO.setLength(childVO.getLength());
		tVO.setWidth(childVO.getWidth());
		tVO.setHeight(childVO.getHeight());
		tVO.setTrans_date(new UFDate(new Date()));
		tVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		tVO.setCreate_time(new UFDateTime(new Date()));
		tVO.setRelate_vbillno(parentVO.getVbillno());
		tVO.setRelate_b_vbillno(childVO.getVbillno());
		tVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		tVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(tVO);
		NWDao.getInstance().saveOrUpdate(tVO);

		// 修改批次QTY
		LotQtyVO lqVO = NWDao.getInstance().queryByCondition(LotQtyVO.class, "lot=?", pdVO.getLot());
		if(lqVO == null) {
			throw new BusiException("分配明细记录没有对应的批次QTY记录，内部批次号[?]！",pdVO.getLot());
		}
		UFDouble stock_num = lqVO.getStock_num() == null ? new UFDouble(0) : lqVO.getStock_num();
		UFDouble picked_num = lqVO.getPicked_num() == null ? new UFDouble(0) : lqVO.getPicked_num();
		lqVO.setPicked_num(picked_num.sub(picked_count));
		lqVO.setStock_num(stock_num.sub(picked_count));
		lqVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(lqVO);

		// 3、修改明细表
		body_picked_count = body_picked_count.sub(picked_count);
		body_shiped_count = body_shiped_count.add(picked_count);
		childVO.setPicked_count(body_picked_count);
		childVO.setShiped_count(body_shiped_count);
		if(body_shiped_count.doubleValue() == body_order_count.doubleValue()) {
			childVO.setVbillstatus(BillStatus.OUTSTO_SHIPED);
		} else {
			childVO.setVbillstatus(BillStatus.OUTSTO_PART_SHIP);
		}
		childVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(childVO);

		// 4、修改主表
		header_picked_count = header_picked_count.sub(picked_count);
		header_shiped_count = header_shiped_count.add(picked_count);
		parentVO.setPicked_count(header_picked_count);
		parentVO.setShiped_count(header_shiped_count);
		if(header_shiped_count.doubleValue() == header_order_count.doubleValue()) {
			parentVO.setVbillstatus(BillStatus.OUTSTO_SHIPED);
		} else {
			parentVO.setVbillstatus(BillStatus.OUTSTO_PART_SHIP);
		}
		parentVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(parentVO);
	}

	/**
	 * 取消发货
	 * 
	 * @param paramVO
	 * @return
	 */
	@Transactional
	public void unship(String pk_outstorage_b) {
		logger.info("取消发货，pk_outstorage_b：" + pk_outstorage_b);
		if(StringUtils.isBlank(pk_outstorage_b)) {
			return;
		}
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "pk_outstorage_b=?",
				pk_outstorage_b);
		if(childVO == null) {
			throw new BusiException("该出库单明细记录已经被删除，不能取消发货！");
		}
		OutstorageVO parentVO = NWDao.getInstance().queryByCondition(OutstorageVO.class, "pk_outstorage=?",
				childVO.getPk_outstorage());
		if(parentVO == null) {
			throw new BusiException("该出库单已经被删除，不能取消发货，出库单明细号[?]",childVO.getVbillno());
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
