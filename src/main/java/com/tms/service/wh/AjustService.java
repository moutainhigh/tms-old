package com.tms.service.wh;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.utils.BillnoHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransactionConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.vo.wh.LotQtyVO;
import com.tms.vo.wh.LotVO;
import com.tms.vo.wh.StorageAjustBVO;
import com.tms.vo.wh.StorageAjustVO;
import com.tms.vo.wh.TransactionVO;

/**
 * 库内调整
 * 
 * @author xuqc
 * @date 2014-3-29 下午01:03:31
 */
@Service
public class AjustService extends TMSAbsBillServiceImpl {

	public String getBillType() {
		return BillTypeConst.AJUST;
	}

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, StorageAjustVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, StorageAjustVO.PK_STORAGE_AJUST);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, StorageAjustBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, StorageAjustBVO.PK_STORAGE_AJUST);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_STORAGE_AJUST_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_STORAGE_AJUST_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	protected Integer getConfirmStatus() {
		return BillStatus.AJUST_CONFIRM;
	}

	/**
	 * 1、校验调整数量和当前的可用数量(QTY表中的可用数量)<br/>
	 * 2、生成新的QTY和批次表，使用新的内部批次号<br/>
	 * 3、写入交易表<br/>
	 * 4、修改调整单状态<br/>
	 */
	public void processBeforeConfirm(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeConfirm(billVO, paramVO);
		StorageAjustVO parentVO = (StorageAjustVO) billVO.getParentVO();
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos == null || cvos.length == 0) {
			throw new BusiException("没有调整单的明细信息！");
		}
		for(CircularlyAccessibleValueObject cvo : cvos) {
			StorageAjustBVO childVO = (StorageAjustBVO) cvo;
			if(StringUtils.isBlank(childVO.getLot())) {
				throw new BusiException("调整单明细没有内部批次号！");
			}
			LotQtyVO lqVO = NWDao.getInstance().queryByCondition(LotQtyVO.class, "lot=?", childVO.getLot());
			if(lqVO == null) {
				throw new BusiException("调整单明细对应的库存记录已经被删除，内部批次号[?]！",childVO.getLot());
			}
			// 当前库存可用量
			UFDouble available_num = lqVO.getAvailable_num() == null ? new UFDouble(0) : lqVO.getAvailable_num();
			UFDouble ajust_num = childVO.getAjust_num() == null ? new UFDouble(0) : childVO.getAjust_num();
			if(ajust_num.doubleValue() > available_num.doubleValue()) {
				throw new BusiException("调整数量不能大于当前库存量！");
			}
			// 调整现有库存的库存量
			lqVO.setAvailable_num(available_num.sub(ajust_num));
			lqVO.setStock_num(ajust_num);
			lqVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(lqVO);

			// 生成一个内部批次号
			String lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.LOT);
			LotQtyVO newLqVO = new LotQtyVO();
			newLqVO.setLot(lot);
			newLqVO.setPk_goods_allocation(childVO.getPk_goods_allocation());
			newLqVO.setLpn(childVO.getLpn());
			newLqVO.setPk_customer(parentVO.getDest_customer());
			newLqVO.setPk_goods(childVO.getPk_goods());
			newLqVO.setStock_num(ajust_num);
			newLqVO.setAvailable_num(ajust_num);
			newLqVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			newLqVO.setCreate_time(new UFDateTime(new Date()));
			newLqVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			newLqVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(newLqVO);
			NWDao.getInstance().saveOrUpdate(newLqVO);

			LotVO newLotVO = new LotVO();
			newLotVO.setLot(lot);
			newLotVO.setPk_customer(parentVO.getDest_customer());
			newLotVO.setPk_goods(childVO.getPk_goods());
			newLotVO.setGoods_prop(childVO.getGoods_prop());
			newLotVO.setLot_attr1(childVO.getLot_attr1());
			newLotVO.setLot_attr2(childVO.getLot_attr2());
			newLotVO.setLot_attr3(childVO.getLot_attr3());
			newLotVO.setLot_attr4(childVO.getLot_attr4());
			newLotVO.setLot_attr5(childVO.getLot_attr5());
			newLotVO.setLot_attr6(childVO.getLot_attr6());
			newLotVO.setLot_attr7(childVO.getLot_attr7());
			newLotVO.setLot_attr8(childVO.getLot_attr8());
			newLotVO.setLot_attr9(childVO.getLot_attr9());
			newLotVO.setLot_attr10(childVO.getLot_attr10());
			newLotVO.setLot_attr11(childVO.getLot_attr11());
			newLotVO.setProduce_date(childVO.getProduce_date());
			newLotVO.setExpire_date(childVO.getExpire_date());
			newLotVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			newLotVO.setCreate_time(new UFDateTime(new Date()));
			newLotVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			newLotVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(newLotVO);
			NWDao.getInstance().saveOrUpdate(newLotVO);

			// 1.2生成交易VO
			TransactionVO tVO = new TransactionVO();
			tVO.setDbilldate(new UFDate());
			tVO.setRelateid(parentVO.getPk_storage_ajust());
			tVO.setRelate_vbillno(parentVO.getVbillno());
			tVO.setVbillno(BillnoHelper.generateBillnoByDefault(BillTypeConst.TRANS));
			tVO.setTrans_type(TransactionConst.TRANS_TYPE.AJUST.toString());// 交易类型为收货
			tVO.setPk_customer(parentVO.getDest_customer());
			tVO.setPk_goods(childVO.getPk_goods());
			tVO.setLot(lot);
			tVO.setFrom_loc(childVO.getPk_goods_allocation());
			tVO.setFrom_lpn(childVO.getLpn());
			tVO.setTo_loc(childVO.getDest_goods_allocation());
			tVO.setTo_lpn(childVO.getDest_lpn());
			tVO.setOrder_type(parentVO.getOrder_type());
			tVO.setVstatus(TransactionConst.STATUS.OK.toString());// 实际设置为OK
			tVO.setGoods_prop(childVO.getGoods_prop());
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
			tVO.setQuantity(ajust_num);
			tVO.setTrans_date(new UFDate(new Date()));
			tVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			tVO.setCreate_time(new UFDateTime(new Date()));
			tVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			tVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(tVO);
			NWDao.getInstance().saveOrUpdate(tVO);

			childVO.setDest_lot(lot);
		}
	}
}
