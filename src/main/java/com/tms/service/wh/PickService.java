package com.tms.service.wh;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.BillnoHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.vo.wh.LotQtyVO;
import com.tms.vo.wh.LotVO;
import com.tms.vo.wh.OutstorageBVO;
import com.tms.vo.wh.OutstorageVO;
import com.tms.vo.wh.PickDetailVO;

/**
 * 分配
 * 
 * @author xuqc
 * @date 2014-3-7 下午10:31:16
 */
@Service
public class PickService extends AbsToftServiceImpl {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, LotQtyVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, LotQtyVO.PK_LOT_QTY);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_LOT_QTY);
			childVO.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_LOT_QTY);
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		return templetVO;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		StringBuffer condBuf = new StringBuffer("ts_lot_qty.available_num > 0");
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			condBuf.append(" and ");
			condBuf.append(cond);
		}
		return condBuf.toString();
	}

	public PaginationVO getPaginationVO(AggregatedValueObject billInfo, ParamVO paramVO,
			Class<? extends SuperVO> voClass, int offset, int pageSize, String where, String orderBy, Object... values) {
		String sql = "select ts_lot_qty.pk_lot_qty,ts_lot_qty.lot,ts_lot_qty.pk_goods_allocation,ts_lot_qty.lpn,ts_lot_qty.pk_customer,ts_lot_qty.pk_goods,"
				+ "ts_lot_qty.stock_num,ts_lot_qty.available_num,ts_lot_qty.located_num,ts_lot_qty.picked_num,ts_lot_qty.choosed_num,"
				+ "ts_lot_qty.instorage_vbillno,ts_lot.lot_attr1,ts_lot.lot_attr2,ts_lot.lot_attr3,ts_lot.lot_attr4,"
				+ "ts_lot.lot_attr5,ts_lot.lot_attr6,ts_lot.lot_attr7,ts_lot.lot_attr8,ts_lot.lot_attr9,ts_lot.lot_attr10,"
				+ "ts_lot.lot_attr11,ts_lot.produce_date,ts_lot.expire_date from ts_lot_qty inner join ts_lot "
				+ "on ts_lot_qty.lot=ts_lot.lot where isnull(ts_lot_qty.dr,0)=0 and isnull(ts_lot.dr,0)=0 ";
		if(StringUtils.isNotBlank(where)) {
			sql += " and ";
			sql += where;
		}
		if(orderBy != null && orderBy.length() > 0 && orderBy.indexOf(Constants.ORDER_BY) != -1) {
			sql += orderBy;
		}
		PaginationVO paginationVO = dao.queryBySqlWithPaging(sql, HashMap.class, offset, pageSize, values);
		return paginationVO;
	}

	/**
	 * 选择一行出库单明细
	 * 
	 * @param pk_outstorage_b
	 */
	@Transactional
	public void unpick1(String pk_outstorage_b) {
		if(StringUtils.isBlank(pk_outstorage_b)) {
			return;
		}
		// 根据pk_outstorage_b 查询到一条相应的分配明细
		PickDetailVO[] pdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PickDetailVO.class,
				"pk_outstorage_b=?", pk_outstorage_b);
		if(pdVOs == null || pdVOs.length == 0) {
			throw new BusiException("没有找到相应的出库单分配明细，无法取消分配！");
		}
		for(PickDetailVO pdVO : pdVOs) {
			unpick(pdVO.getPk_pick_detail());
		}
	}

	/**
	 * 选择一行分配明细，执行取消分配操作
	 * 
	 * @param pk_pick_detail
	 * @return
	 */
	@Transactional
	public void unpick(String pk_pick_detail) {
		if(StringUtils.isBlank(pk_pick_detail)) {
			return;
		}
		PickDetailVO pdVO = NWDao.getInstance()
				.queryByCondition(PickDetailVO.class, "pk_pick_detail=?", pk_pick_detail);
		if(pdVO == null) {
			throw new BusiException("没有找到相应的分配明细，无法取消分配，明细ID[?]！",pk_pick_detail);
		}
		if(pdVO.getVbillstatus() != BillStatus.NEW) {
			throw new BusiException("分配明细不是[新建]状态，不能取消分配，明细单号[?]！",pdVO.getVbillno());
		}
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "vbillno=?",
				pdVO.getOutstorage_b_vbillno());
		if(childVO == null) {
			throw new BusiException("该分配明细对应的出货明细记录已经被删除，无法取消分配，明细单号[?]！,pdVO.getVbillno()");
		}
		OutstorageVO parentVO = NWDao.getInstance().queryByCondition(OutstorageVO.class, "pk_outstorage=?",
				pdVO.getPk_outstorage());
		if(parentVO == null) {
			throw new BusiException("该出库单已经被删除，无法取消分配，明细单号[?]！",pdVO.getVbillno());
		}
		UFDouble picked_count = pdVO.getPicked_count() == null ? new UFDouble(0) : pdVO.getPicked_count();
		// 1、表头的已分配数量减去表体的已分配数量
		UFDouble header_picked_count = parentVO.getPicked_count() == null ? new UFDouble(0) : parentVO
				.getPicked_count();
		header_picked_count = header_picked_count.sub(picked_count);
		parentVO.setPicked_count(header_picked_count);
		if(header_picked_count.doubleValue() == 0) {
			// 表头的已接收数量为0，那么状态更新为新建
			parentVO.setVbillstatus(BillStatus.NEW);
		} else {
			parentVO.setVbillstatus(BillStatus.OUTSTO_PART_PICK);
		}
		parentVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(parentVO);

		// 2、将明细行已分配数量减去分配明细的已分配数量，同时更改状态为新建
		UFDouble body_picked_count = childVO.getPicked_count() == null ? new UFDouble(0) : childVO.getPicked_count();
		body_picked_count = body_picked_count.sub(picked_count);
		childVO.setPicked_count(body_picked_count);
		if(body_picked_count.doubleValue() == 0) {
			childVO.setVbillstatus(BillStatus.NEW);
		} else {
			childVO.setVbillstatus(BillStatus.OUTSTO_PART_PICK);
		}

		childVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(childVO);

		// 3、根据分配明细中的内部批次号，找到批次QTY表中的相应记录，将对应的记录的“可用数量”加上分配明细中的已分配数量，“分配数量”减去已分配数量
		LotQtyVO lqVO = NWDao.getInstance().queryByCondition(LotQtyVO.class, "lot=?", pdVO.getLot());
		UFDouble available_num = lqVO.getAvailable_num() == null ? new UFDouble(0) : lqVO.getAvailable_num();
		UFDouble picked_num = lqVO.getPicked_num() == null ? new UFDouble(0) : lqVO.getPicked_num();
		available_num = available_num.add(picked_count);
		picked_num = picked_num.sub(picked_count);
		lqVO.setAvailable_num(available_num);
		lqVO.setPicked_num(picked_num);
		lqVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(lqVO);

		// 4、删除分配明细，可能会有多条分配明细
		NWDao.getInstance().delete(pdVO);
	}

	/**
	 * 保存分配数量
	 */
	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		String pk_outstorage_b = ServletContextHolder.getRequest().getParameter("pk_outstorage_b");
		if(StringUtils.isBlank(pk_outstorage_b)) {
			throw new BusiException("执行分配时关键参数丢失！");
		}
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "pk_outstorage_b=?",
				pk_outstorage_b);
		OutstorageVO parentVO = NWDao.getInstance().queryByCondition(OutstorageVO.class, "pk_outstorage=?",
				childVO.getPk_outstorage());
		// 出库单的已分配数量
		UFDouble header_picked_count = parentVO.getPicked_count() == null ? new UFDouble(0) : parentVO
				.getPicked_count();
		// 出库单子表的已分配数量
		UFDouble body_picked_count = childVO.getPicked_count() == null ? new UFDouble(0) : childVO.getPicked_count();
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos != null && cvos.length > 0) {
			for(CircularlyAccessibleValueObject cvo : cvos) {
				LotQtyVO lqVO = (LotQtyVO) cvo;
				if(lqVO.getPick_num() == null || lqVO.getPick_num().doubleValue() == 0) {
					throw new BusiException("分配数量必须大于0，小于等于可用数量！");
				}

				// 1、可用数量=可用数量-分配数量；已分配数量=已分配数量+分配数量
				UFDouble available_num = lqVO.getAvailable_num() == null ? new UFDouble(0) : lqVO.getAvailable_num();
				UFDouble pick_num = lqVO.getPick_num();// 这一次的分配数量
				UFDouble picked_num = lqVO.getPicked_num() == null ? new UFDouble(0) : lqVO.getPicked_num();// 已分配数量
				if(pick_num.doubleValue() > available_num.doubleValue()) {
					throw new BusiException("分配数量必须大于0，小于等于可用数量！");
				}
				picked_num = picked_num.add(pick_num);
				available_num = available_num.sub(pick_num);
				body_picked_count = body_picked_count.add(pick_num);
				header_picked_count = header_picked_count.add(pick_num);
				lqVO.setPicked_num(picked_num);
				lqVO.setAvailable_num(available_num);
				lqVO.setStatus(VOStatus.UPDATED);
				NWDao.getInstance().saveOrUpdate(lqVO);

				// 2、往分配明细插入一条记录
				// 根据lot查询批次管理（ts_lot）表
				LotVO lVO = NWDao.getInstance().queryByCondition(LotVO.class, "lot=?", lqVO.getLot());
				if(lVO == null) {
					throw new BusiException("内部批次号[?]在批次表中没有相应的记录！",lqVO.getLot());
				}
				PickDetailVO pdVO = new PickDetailVO();
				pdVO.setDbilldate(new UFDate());
				pdVO.setPk_outstorage(parentVO.getPk_outstorage());
				pdVO.setPk_outstorage_b(childVO.getPk_outstorage_b());
				pdVO.setVbillno(BillnoHelper.generateBillnoByDefault(BillTypeConst.PICK_DETAIL));
				pdVO.setVbillstatus(BillStatus.NEW);
				pdVO.setOutstorage_vbillno(parentVO.getVbillno());
				pdVO.setOutstorage_b_vbillno(childVO.getVbillno());
				pdVO.setLot(lqVO.getLot());
				pdVO.setPk_customer(lqVO.getPk_customer());
				pdVO.setPk_goods(lqVO.getPk_goods());
				pdVO.setOrder_count(childVO.getOrder_count());
				pdVO.setPicked_count(lqVO.getPick_num());
				pdVO.setLpn(lqVO.getLpn());
				pdVO.setPk_goods_allocation(lqVO.getPk_goods_allocation());
				pdVO.setGoods_prop(lVO.getGoods_prop());
				pdVO.setLot_attr1(lVO.getLot_attr1());
				pdVO.setLot_attr2(lVO.getLot_attr2());
				pdVO.setLot_attr3(lVO.getLot_attr3());
				pdVO.setLot_attr4(lVO.getLot_attr4());
				pdVO.setLot_attr5(lVO.getLot_attr5());
				pdVO.setLot_attr6(lVO.getLot_attr6());
				pdVO.setLot_attr7(lVO.getLot_attr7());
				pdVO.setLot_attr8(lVO.getLot_attr8());
				pdVO.setLot_attr9(lVO.getLot_attr9());
				pdVO.setLot_attr10(lVO.getLot_attr10());
				pdVO.setLot_attr11(lVO.getLot_attr11());
				pdVO.setProduce_date(lVO.getProduce_date());
				pdVO.setExpire_date(lVO.getExpire_date());
				pdVO.setPick_user(WebUtils.getLoginInfo().getPk_user());
				pdVO.setPick_date(new UFDate(new Date()));
				pdVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				pdVO.setCreate_time(new UFDateTime(new Date()));
				pdVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				pdVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(pdVO);
				NWDao.getInstance().saveOrUpdate(pdVO);
			}

			// 3、更新出库单子表的状态和已接收数量
			if(body_picked_count.doubleValue() > childVO.getOrder_count().doubleValue()) {
				// 分配数量超过了订单数量
				throw new BusiException("分配数量的和不能大于订单数量！");
			}
			childVO.setPicked_count(body_picked_count);
			// 如果已分配数量=订单数量，那么单据状态改成已分配，否则部分分配
			if(body_picked_count.doubleValue() == childVO.getOrder_count().doubleValue()) {
				childVO.setVbillstatus(BillStatus.OUTSTO_PICKED);
			} else {
				childVO.setVbillstatus(BillStatus.OUTSTO_PART_PICK);
			}
			childVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(childVO);

			// 3、更新出库单主表的状态和已接收数量
			if(header_picked_count.doubleValue() > parentVO.getOrder_count().doubleValue()) {
				// 分配数量超过了订单数量
				throw new BusiException("分配数量的和不能大于订单数量！");
			}
			parentVO.setPicked_count(header_picked_count);
			// 如果已分配数量=订单数量，那么单据状态改成已分配，否则部分分配
			if(header_picked_count.doubleValue() == parentVO.getOrder_count().doubleValue()) {
				parentVO.setVbillstatus(BillStatus.OUTSTO_PICKED);
			} else {
				parentVO.setVbillstatus(BillStatus.OUTSTO_PART_PICK);
			}
			parentVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(parentVO);
		} else {
			throw new BusiException("您没有对任何记录进行分配操作！");
		}
		// 返回出库单子表的已分配数量，这个数量要更新到分配页面的查询框
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("picked_count", childVO.getPicked_count());
		billVO.setChildrenVO(new OutstorageBVO[]{childVO});
		return billVO;
	}

	/**
	 * 自动分配
	 * 
	 * @param pk_outstorage_b
	 */
	@Transactional
	public void doAutopick(String pk_outstorage_b) {
		if(StringUtils.isBlank(pk_outstorage_b)) {
			return;
		}
		OutstorageBVO childVO = NWDao.getInstance().queryByCondition(OutstorageBVO.class, "pk_outstorage_b=?",
				pk_outstorage_b);
		if(childVO == null) {
			throw new BusiException("该出库单明细记录已经被删除，不能进行分配！");
		}
		OutstorageVO parentVO = NWDao.getInstance().queryByCondition(OutstorageVO.class, "pk_outstorage=?",
				childVO.getPk_outstorage());
		if(parentVO == null) {
			throw new BusiException("该出库单已经被删除，不能进行分配，出库单明细号[?]！",childVO.getVbillno());
		}
		// 出库单的已分配数量
		UFDouble header_picked_count = parentVO.getPicked_count() == null ? new UFDouble(0) : parentVO
				.getPicked_count();
		// 当前明细的订单数量，和已分配数量
		UFDouble body_order_count = childVO.getOrder_count() == null ? new UFDouble(0) : childVO.getOrder_count();
		UFDouble body_picked_count = childVO.getPicked_count() == null ? new UFDouble(0) : childVO.getPicked_count();
		UFDouble body_unpick_count = body_order_count.sub(body_picked_count);
		if(body_unpick_count.doubleValue() <= 0) {
			throw new BusiException("该出库单明细记录已经不需要分配，出库单明细号[?]！",childVO.getVbillno());
		}

		String pk_customer = childVO.getPk_customer();// 客户
		String pk_goods = childVO.getPk_goods();// 货品
		String pk_goods_allocation = childVO.getPk_goods_allocation();// 货位
		String lpn = childVO.getLpn();// LPN
		// 根据货品，货位，LPN，查询可用数量大于0的批次QTY
		StringBuffer condBuf = new StringBuffer(" ts_lot_qty.pk_goods=? and ts_lot_qty.pk_customer=? ");
		if(StringUtils.isNotBlank(pk_goods_allocation)) {
			condBuf.append(" and ");
			condBuf.append("ts_lot_qty.pk_goods_allocation='");
			condBuf.append(pk_goods_allocation);
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(lpn)) {
			condBuf.append(" and ");
			condBuf.append("ts_lot_qty.lpn='");
			condBuf.append(lpn);
			condBuf.append("'");
		}
		condBuf.append(" and ts_lot_qty.available_num > 0");// 可用数量大于0的批次Qty
		if(StringUtils.isNotBlank(childVO.getLot_attr1())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr1='");
			condBuf.append(childVO.getLot_attr1());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr2())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr2='");
			condBuf.append(childVO.getLot_attr2());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr3())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr3='");
			condBuf.append(childVO.getLot_attr3());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr4())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr4='");
			condBuf.append(childVO.getLot_attr4());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr5())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr5='");
			condBuf.append(childVO.getLot_attr5());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr6())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr6='");
			condBuf.append(childVO.getLot_attr6());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr7())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr7='");
			condBuf.append(childVO.getLot_attr7());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr8())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr8='");
			condBuf.append(childVO.getLot_attr8());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr9())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr9='");
			condBuf.append(childVO.getLot_attr9());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr10())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr10='");
			condBuf.append(childVO.getLot_attr10());
			condBuf.append("'");
		}
		if(StringUtils.isNotBlank(childVO.getLot_attr11())) {
			condBuf.append(" and ");
			condBuf.append("ts_lot.lot_attr11='");
			condBuf.append(childVO.getLot_attr11());
			condBuf.append("'");
		}
		// 构建查询条件
		String sql = "select ts_lot_qty.* from ts_lot_qty join ts_lot on ts_lot_qty.lot=ts_lot.lot where "
				+ condBuf.toString();
		// LotQtyVO[] lqVOs =
		// NWDao.getInstance().queryForSuperVOArrayByCondition(LotQtyVO.class,
		// condBuf.toString(),
		// pk_goods, pk_customer);
		List<LotQtyVO> lqVOs = NWDao.getInstance().queryForList(sql, LotQtyVO.class, pk_goods, pk_customer);
		if(lqVOs == null || lqVOs.size() == 0) {
			throw new BusiException("没有库存数据，无法分配，出库单明细号[?]！",childVO.getVbillno());
		}
		for(LotQtyVO lqVO : lqVOs) {
			if(body_unpick_count.doubleValue() <= 0) {
				// 如果
				break;
			}
			UFDouble available_num = lqVO.getAvailable_num() == null ? new UFDouble(0) : lqVO.getAvailable_num();// 批次QTY表的可分配数量
			UFDouble picked_num = lqVO.getPicked_num() == null ? new UFDouble(0) : lqVO.getPicked_num();// 批次QTY表的已分配数量
			UFDouble pick_num;// 当前要分配的两
			if(body_unpick_count.doubleValue() <= available_num.doubleValue()) {
				// 这条记录的库存已经足够了
				pick_num = body_unpick_count;
			} else {
				// 一次分配还不够，需要多次进行分配
				pick_num = available_num;
			}
			picked_num = picked_num.add(pick_num);
			available_num = available_num.sub(pick_num);
			body_picked_count = body_picked_count.add(pick_num);
			header_picked_count = header_picked_count.add(pick_num);
			lqVO.setPicked_num(picked_num);
			lqVO.setAvailable_num(available_num);
			lqVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(lqVO);

			// 2、往分配明细插入一条记录
			// 根据lot查询批次管理（ts_lot）表
			LotVO lVO = NWDao.getInstance().queryByCondition(LotVO.class, "lot=?", lqVO.getLot());
			if(lVO == null) {
				throw new BusiException("内部批次号[?]在批次表中没有相应的记录！",lqVO.getLot());
			}
			PickDetailVO pdVO = new PickDetailVO();
			pdVO.setDbilldate(new UFDate());
			pdVO.setPk_outstorage(parentVO.getPk_outstorage());
			pdVO.setPk_outstorage_b(childVO.getPk_outstorage_b());
			pdVO.setVbillno(BillnoHelper.generateBillnoByDefault(BillTypeConst.PICK_DETAIL));
			pdVO.setVbillstatus(BillStatus.NEW);
			pdVO.setOutstorage_vbillno(parentVO.getVbillno());
			pdVO.setOutstorage_b_vbillno(childVO.getVbillno());
			pdVO.setLot(lqVO.getLot());
			pdVO.setPk_customer(lqVO.getPk_customer());
			pdVO.setPk_goods(lqVO.getPk_goods());
			pdVO.setOrder_count(childVO.getOrder_count());
			pdVO.setPicked_count(pick_num);
			pdVO.setLpn(lqVO.getLpn());
			pdVO.setPk_goods_allocation(lqVO.getPk_goods_allocation());
			pdVO.setGoods_prop(lVO.getGoods_prop());
			pdVO.setLot_attr1(lVO.getLot_attr1());
			pdVO.setLot_attr2(lVO.getLot_attr2());
			pdVO.setLot_attr3(lVO.getLot_attr3());
			pdVO.setLot_attr4(lVO.getLot_attr4());
			pdVO.setLot_attr5(lVO.getLot_attr5());
			pdVO.setLot_attr6(lVO.getLot_attr6());
			pdVO.setLot_attr7(lVO.getLot_attr7());
			pdVO.setLot_attr8(lVO.getLot_attr8());
			pdVO.setLot_attr9(lVO.getLot_attr9());
			pdVO.setLot_attr10(lVO.getLot_attr10());
			pdVO.setLot_attr11(lVO.getLot_attr11());
			pdVO.setProduce_date(lVO.getProduce_date());
			pdVO.setExpire_date(lVO.getExpire_date());
			pdVO.setPick_user(WebUtils.getLoginInfo().getPk_user());
			pdVO.setPick_date(new UFDate(new Date()));
			pdVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			pdVO.setCreate_time(new UFDateTime(new Date()));
			pdVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			pdVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(pdVO);
			NWDao.getInstance().saveOrUpdate(pdVO);

			// 待分配数量，如果待分配数量小于0了，那么就不需要继续进行了。
			body_unpick_count = body_unpick_count.sub(pick_num);

			if(childVO.getVbillno().equals("OUTSTO20143260007_1")) {
				throw new BusiException("aaa");
			}
		}

		// 3、更新出库单子表的状态和已接收数量
		if(body_picked_count.doubleValue() > childVO.getOrder_count().doubleValue()) {
			// 分配数量超过了订单数量
			throw new BusiException("分配数量的和不能大于订单数量！");
		}
		childVO.setPicked_count(body_picked_count);
		// 如果已分配数量=订单数量，那么单据状态改成已分配，否则部分分配
		if(body_picked_count.doubleValue() == childVO.getOrder_count().doubleValue()) {
			childVO.setVbillstatus(BillStatus.OUTSTO_PICKED);
		} else {
			childVO.setVbillstatus(BillStatus.OUTSTO_PART_PICK);
		}
		childVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(childVO);

		// 3、更新出库单主表的状态和已接收数量
		if(header_picked_count.doubleValue() > parentVO.getOrder_count().doubleValue()) {
			// 分配数量超过了订单数量
			throw new BusiException("分配数量的和不能大于订单数量！");
		}
		parentVO.setPicked_count(header_picked_count);
		// 如果已分配数量=订单数量，那么单据状态改成已分配，否则部分分配
		if(header_picked_count.doubleValue() == parentVO.getOrder_count().doubleValue()) {
			parentVO.setVbillstatus(BillStatus.OUTSTO_PICKED);
		} else {
			parentVO.setVbillstatus(BillStatus.OUTSTO_PART_PICK);
		}
		parentVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(parentVO);
	}
}
