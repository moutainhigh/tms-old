package com.tms.service.wh;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransactionConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.vo.wh.ExAggInstorageVO;
import com.tms.vo.wh.InstorageBVO;
import com.tms.vo.wh.InstorageVO;
import com.tms.vo.wh.LotQtyVO;
import com.tms.vo.wh.LotVO;
import com.tms.vo.wh.TransactionVO;

/**
 * 收货
 * 
 * @author xuqc
 * @date 2014-3-7 下午10:31:16
 */
@Service
public class ReceiptService extends TMSAbsBillServiceImpl {

	public String getBillType() {
		return BillTypeConst.INSTO;
	}

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggInstorageVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, ExAggInstorageVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InstorageVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InstorageVO.PK_INSTORAGE);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggInstorageVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, InstorageBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, InstorageBVO.PK_INSTORAGE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_INSTORAGE_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_INSTORAGE_B);

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggInstorageVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, TransactionVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, TransactionVO.RELATEID);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, TabcodeConst.TS_TRANSACTION);
			childVO1.setAttributeValue(VOTableVO.VOTABLE, TabcodeConst.TS_TRANSACTION);

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
					if(fieldVO.getItemkey().equals(InstorageVO.VBILLSTATUS)) {
						fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
					} else if(fieldVO.getItemkey().equals(InstorageVO.EST_ARRI_DATE)) {
						// 预计到货日期
						fieldVO.setBeforeRenderer("est_arri_dateBeforeRenderer");
					}
				}
			}
		}
		return templetVO;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		// 1、表体的接收量合计到表头的总接收量中
		InstorageVO parentVO = (InstorageVO) billVO.getParentVO();
		UFDouble header_accepted_count = parentVO.getAccepted_count() == null ? new UFDouble(0) : parentVO
				.getAccepted_count();
		if(header_accepted_count.doubleValue() == 0) {
			if(parentVO.getAct_arri_date() == null) {
				// 如果表头的接收量为0，说明这是第一次收货，将“实际到货日期”设置为当前日期，以后收货就不需要修改这个日期了
				parentVO.setAct_arri_date(new UFDateTime(new Date()).toString());
			}
		}
		ExAggInstorageVO aggVO = (ExAggInstorageVO) billVO;
		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO(TabcodeConst.TS_INSTORAGE_B);
		List<TransactionVO> tVOs = new ArrayList<TransactionVO>();// 交易数据，需要保存到交易表中
		List<LotQtyVO> lqVOs = new ArrayList<LotQtyVO>();
		List<LotVO> lVOs = new ArrayList<LotVO>();
		if(cvos != null && cvos.length > 0) {
			for(CircularlyAccessibleValueObject cvo : cvos) {
				InstorageBVO childVO = (InstorageBVO) cvo;
				UFDouble body_accepted_count = childVO.getAccepted_count() == null ? new UFDouble(0) : childVO
						.getAccepted_count();// 表体行已接收量

				// 当前的接收量
				UFDouble accept_count = childVO.getAccept_count() == null ? new UFDouble(0) : childVO.getAccept_count();
				header_accepted_count = header_accepted_count.add(accept_count);
				body_accepted_count = body_accepted_count.add(accept_count);
				childVO.setAccepted_count(body_accepted_count);

				double order_count = childVO.getOrder_count() == null ? 0 : childVO.getOrder_count().doubleValue();// 订单量
				double accepted_count = childVO.getAccepted_count() == null ? 0 : childVO.getAccepted_count()
						.doubleValue();// 已接收量
				// 1.1根据订单量和接收量判断状态
				if(childVO.getStatus() != VOStatus.UNCHANGED) {// 这是执行收货的那一行
					InstorageBVO oriChildVO = this.getByPrimaryKey(InstorageBVO.class, childVO.getPk_instorage_b());
					oriChildVO.setAccepted_count(body_accepted_count);
					if(order_count == accepted_count) {
						oriChildVO.setVbillstatus(BillStatus.INSTO_ALL_REC);// 已全部收货
					} else {
						oriChildVO.setVbillstatus(BillStatus.INSTO_PART_REC);// 部分收货
					}
					oriChildVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(oriChildVO);
				}
				childVO.setStatus(VOStatus.UNCHANGED);// 收货的时候除了已接收量不需要修改任何表体的内容

				if(accept_count.doubleValue() != 0) {
					// 生成一个内部批次号，备用
					String lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.LOT);
					// 说明对这个货品进行收货了
					// 1.2生成交易VO
					TransactionVO tVO = new TransactionVO();
					tVO.setDbilldate(new UFDate());
					tVO.setRelateid(parentVO.getPk_instorage());
					tVO.setVbillno(BillnoHelper.generateBillnoByDefault(BillTypeConst.TRANS));
					tVO.setTrans_type(TransactionConst.TRANS_TYPE.IN.toString());// 交易类型为收货
					tVO.setPk_customer(childVO.getPk_customer());
					tVO.setPk_goods(childVO.getPk_goods());
					tVO.setLot(lot);
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
					tVO.setQuantity(accept_count);
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
					tVOs.add(tVO);

					// 1.3生成LotQtyVO
					LotQtyVO lqVO = new LotQtyVO();
					lqVO.setLot(lot);
					lqVO.setPk_goods_allocation(childVO.getPk_goods_allocation());
					lqVO.setLpn(childVO.getLpn());
					lqVO.setPk_customer(childVO.getPk_customer());
					lqVO.setPk_goods(childVO.getPk_goods());
					lqVO.setStock_num(accept_count);
					lqVO.setAvailable_num(accept_count);
					lqVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
					lqVO.setCreate_time(new UFDateTime(new Date()));
					lqVO.setInstorage_vbillno(parentVO.getVbillno());
					lqVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
					lqVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(lqVO);
					lqVOs.add(lqVO);
					// 1.4生成LotVO
					LotVO lVO = new LotVO();
					lVO.setLot(lot);
					lVO.setPk_customer(childVO.getPk_customer());
					lVO.setPk_goods(childVO.getPk_goods());
					lVO.setGoods_prop(childVO.getGoods_prop());
					lVO.setRec_unit_weight(childVO.getUnit_weight());
					lVO.setRec_unit_volume(childVO.getUnit_volume());
					lVO.setRec_length(childVO.getLength());
					lVO.setRec_width(childVO.getWidth());
					lVO.setRec_height(childVO.getHeight());
					lVO.setLot_attr1(childVO.getLot_attr1());
					lVO.setLot_attr2(childVO.getLot_attr2());
					lVO.setLot_attr3(childVO.getLot_attr3());
					lVO.setLot_attr4(childVO.getLot_attr4());
					lVO.setLot_attr5(childVO.getLot_attr5());
					lVO.setLot_attr6(childVO.getLot_attr6());
					lVO.setLot_attr7(childVO.getLot_attr7());
					lVO.setLot_attr8(childVO.getLot_attr8());
					lVO.setLot_attr9(childVO.getLot_attr9());
					lVO.setLot_attr10(childVO.getLot_attr10());
					lVO.setLot_attr11(childVO.getLot_attr11());
					lVO.setProduce_date(childVO.getProduce_date());
					lVO.setExpire_date(childVO.getExpire_date());
					lVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
					lVO.setCreate_time(new UFDateTime(new Date()));
					lVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
					lVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(lVO);
					lVOs.add(lVO);
				}
			}
		}
		parentVO.setAccepted_count(header_accepted_count);
		// 如果总接收量等于总预期量，那么设置为收货完成，否则部分收货
		if(parentVO.getAccepted_count() == null || parentVO.getAccepted_count().doubleValue() == 0) {
			throw new BusiException("接收数量不能为空！");
		}
		if(parentVO.getOrder_count().doubleValue() == parentVO.getAccepted_count().doubleValue()) {
			parentVO.setVbillstatus(BillStatus.INSTO_ALL_REC);
		} else {
			parentVO.setVbillstatus(BillStatus.INSTO_PART_REC);
		}
		// 2、收货后，存储到交易表，每个货品的一次收货对应交易表的一条记录，所以如果同时对2个货品进行收货，那么同时插入交易表应该是2行记录
		NWDao.getInstance().saveOrUpdate(tVOs.toArray(new TransactionVO[tVOs.size()]));
		// 3、存储到ts_lot_qty表中
		NWDao.getInstance().saveOrUpdate(lqVOs.toArray(new LotQtyVO[lqVOs.size()]));
		// 4、存储到批次管理表中
		NWDao.getInstance().saveOrUpdate(lVOs.toArray(new LotVO[lVOs.size()]));
	}

	/**
	 * 对多条单据进行收货处理，检查收货信息是否完整<br/>
	 * 1、接收数量=订单数量-已接收数量<br/>
	 * 2、货位和货品属性不能为空
	 * 
	 * @param paramVO
	 * @return
	 */
	@Transactional
	public AggregatedValueObject receiptAll(ParamVO paramVO) {
		logger.info("对多条单据进行收货处理...");
		AggregatedValueObject billVO = queryBillVO(paramVO);
		InstorageVO parentVO = (InstorageVO) billVO.getParentVO();
		parentVO.setStatus(VOStatus.UPDATED);
		if(parentVO.getVbillstatus().intValue() != BillStatus.NEW
				&& parentVO.getVbillstatus().intValue() != BillStatus.INSTO_PART_REC) {
			throw new BusiException("单据[?]不是[新增]或[部分收货]状态，不能全部收货！",parentVO.getVbillno());
		}

		ExAggInstorageVO aggVO = (ExAggInstorageVO) billVO;
		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO(TabcodeConst.TS_INSTORAGE_B);
		if(cvos == null || cvos.length == 0) {
			throw new BusiException("该单据[?]没有明细信息，不需要进行收货！",parentVO.getVbillno());
		}
		for(int i = 0; i < cvos.length; i++) {
			InstorageBVO childVO = (InstorageBVO) cvos[i];
			if(StringUtils.isBlank(childVO.getPk_goods_allocation())) {
				throw new BusiException("该单据[?]的明细中，存在没有维护货位的行，不能全部收货！",parentVO.getVbillno());
			}
			if(childVO.getGoods_prop() == null || childVO.getGoods_prop().intValue() == 0) {
				throw new BusiException("该单据[?]的明细中，存在没有维护货品属性的行，不能全部收货！",parentVO.getVbillno());
			}
			// 设置接收数量
			UFDouble order_count = childVO.getOrder_count() == null ? new UFDouble(0) : childVO.getOrder_count();
			UFDouble accepted_count = childVO.getAccepted_count() == null ? new UFDouble(0) : childVO
					.getAccepted_count();
			childVO.setAccept_count(order_count.sub(accepted_count));
			childVO.setStatus(VOStatus.UPDATED);
		}
		return this.save(billVO, paramVO);
	}
}
