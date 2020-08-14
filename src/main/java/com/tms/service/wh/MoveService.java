package com.tms.service.wh;

import java.util.ArrayList;
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
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.BillnoHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.constants.BillTypeConst;
import com.tms.vo.wh.LotQtyVO;
import com.tms.vo.wh.LotVO;

/**
 * 库内移动
 * 
 * @author xuqc
 * @date 2014-3-29 下午01:02:27
 */
@Service
public class MoveService extends AbsToftServiceImpl {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, LotQtyVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, LotQtyVO.PK_LOT_QTY);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_lot_qty");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_lot_qty");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	/**
	 * 库内移动的查询比较特殊，需要关联ts_lot_qty和ts_lot表
	 * 
	 * @return
	 */
	public String getBaseSql() {
		String sql = "select ts_lot_qty.pk_lot_qty,ts_lot_qty.lot,ts_lot_qty.pk_goods_allocation,ts_lot_qty.lpn,ts_lot_qty.pk_customer,ts_lot_qty.pk_goods,"
				+ "ts_lot_qty.stock_num,ts_lot_qty.available_num,ts_lot_qty.located_num,ts_lot_qty.picked_num,ts_lot_qty.choosed_num,"
				+ "ts_lot_qty.instorage_vbillno,ts_lot.lot_attr1,ts_lot.lot_attr2,ts_lot.lot_attr3,ts_lot.lot_attr4,"
				+ "ts_lot.lot_attr5,ts_lot.lot_attr6,ts_lot.lot_attr7,ts_lot.lot_attr8,ts_lot.lot_attr9,ts_lot.lot_attr10,"
				+ "ts_lot.lot_attr11,ts_lot.produce_date,ts_lot.expire_date from ts_lot_qty inner join ts_lot "
				+ "on ts_lot_qty.lot=ts_lot.lot where isnull(ts_lot_qty.dr,0)=0 and isnull(ts_lot.dr,0)=0 ";
		return sql;
	}

	public PaginationVO getPaginationVO(AggregatedValueObject billInfo, ParamVO paramVO,
			Class<? extends SuperVO> voClass, int offset, int pageSize, String where, String orderBy, Object... values) {
		String sql = getBaseSql();
		if(StringUtils.isNotBlank(where)) {
			sql += " and " + where;
		}
		if(orderBy != null && orderBy.length() > 0 && orderBy.indexOf(Constants.ORDER_BY) != -1) {
			sql += orderBy;
		}
		NWDao dao = NWDao.getInstance();
		PaginationVO paginationVO = dao.queryBySqlWithPaging(sql, HashMap.class, offset, pageSize, values);
		return paginationVO;
	}

	/**
	 * 库内移动 1、移动数量必须是小于等于可用数量 2、库存数量=库存数量-移动数量
	 */
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject[] vos = billVO.getChildrenVO();
		LotQtyVO parentVO = (LotQtyVO) vos[0];
		// 可用数量
		UFDouble available_num = parentVO.getAvailable_num() == null ? new UFDouble(0) : parentVO.getAvailable_num();
		if(available_num.doubleValue() == 0) {
			throw new BusiException("可用数量为0，不能进行移动！");
		}
		// 库存数量
		UFDouble stock_num = parentVO.getStock_num() == null ? new UFDouble(0) : parentVO.getStock_num();
		// 移动数量
		UFDouble move_num = parentVO.getMove_num() == null ? new UFDouble(0) : parentVO.getMove_num();
		if(move_num.doubleValue() <= 0 || move_num.doubleValue() > available_num.doubleValue()) {
			throw new BusiException("移动数量必须大于0并且小于等于可用数量！");
		}
		stock_num = stock_num.sub(move_num);
		available_num = available_num.sub(move_num);
		// 重新设置可用数量和库存数量
		parentVO.setStock_num(stock_num);
		parentVO.setAvailable_num(available_num);
		// 这里不需要修改parentVO，在save方法会修改

		String lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.LOT);// 产生一个新的内部批次号
		LotQtyVO newLqVO = (LotQtyVO) parentVO.clone();// 克隆一个新的
		newLqVO.setPk_lot_qty(null);
		newLqVO.setStock_num(move_num);
		newLqVO.setAvailable_num(move_num);
		newLqVO.setLocated_num(new UFDouble(0));
		newLqVO.setPicked_num(new UFDouble(0));
		newLqVO.setChoosed_num(new UFDouble(0));
		newLqVO.setLpn(parentVO.getDest_lpn());
		newLqVO.setPk_goods_allocation(parentVO.getDest_goods_allocation());
		newLqVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		newLqVO.setCreate_time(new UFDateTime(new Date()));
		newLqVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		// 重新设置内部批次号
		newLqVO.setLot(lot);
		newLqVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(newLqVO);
		NWDao.getInstance().saveOrUpdate(newLqVO);

		// 根据内部批次号查询批次表
		LotVO lVO = NWDao.getInstance().queryByCondition(LotVO.class, "lot=?", parentVO.getLot());
		if(lVO == null) {
			throw new BusiException("批次表中没有相应的记录，批次号[?]",parentVO.getLot());
		}
		LotVO newLotVO = (LotVO) lVO.clone();
		newLotVO.setPk_lot(null);
		newLotVO.setLot(lot);// 设置新的内部批次号
		newLotVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		newLotVO.setCreate_time(new UFDateTime(new Date()));
		newLotVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		newLotVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(newLotVO);
		NWDao.getInstance().saveOrUpdate(newLotVO);
	}
}
