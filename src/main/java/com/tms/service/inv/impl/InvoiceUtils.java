package com.tms.service.inv.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.OperateTypeConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransTypeConst;
import com.tms.service.base.CustService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.te.TrackingService;
import com.tms.service.te.impl.EntrustUtils;
import com.tms.service.tp.PZService;
import com.tms.service.tp.impl.PZUtils;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

import net.sf.jasperreports.engine.query.DefaultQueryExecuterFactoryBundle;

/**
 * 发货单相关数据计算工具类
 * 
 * @author xuqc
 * @date 2014-4-27 下午07:44:59
 */
public class InvoiceUtils {


	static Logger logger = Logger.getLogger(InvoiceUtils.class);

	public static void setHeaderCount(AggregatedValueObject billVO, ParamVO paramVO) {
		setHeaderCount(billVO, paramVO, false);
	}
	
	public static void setHeaderCount(InvoiceVO invoiceVO, InvPackBVO[] invPackBVO) {
		setHeaderCount(invoiceVO, invPackBVO, false);
	}

	/**
	 * 查询委托单有几个提货点
	 * 
	 * @param pk_entrust
	 * @return
	 */
	public static int getDeliNodeCount(String pk_entrust) {
		if(StringUtils.isBlank(pk_entrust)) {
			return 0;
		}
		String sql = "select count(distinct pk_delivery) from ts_invoice WITH(NOLOCK) "
				+ "where pk_invoice in (select pk_invoice from ts_ent_inv_b WITH(NOLOCK) where pk_entrust=? and isnull(dr,0)=0) and isnull(dr,0)=0";
		return NWDao.getInstance().queryForObject(sql, Integer.class, pk_entrust);
	}

	/**
	 * 根据发货单号查询有几个提货点，在配载页面使用，其他地方可以根据委托单查询
	 * 
	 * @param vbillnoAry
	 * @return
	 */
	public static int getDeliNodeCount(String[] vbillnoAry) {
		if(vbillnoAry == null || vbillnoAry.length == 0) {
			return 0;
		}
		String cond = NWUtils.buildConditionString(vbillnoAry);
		String sql = "select count(distinct pk_delivery) from ts_invoice " + "where isnull(dr,0)=0 and vbillno in "
				+ cond;
		return NWDao.getInstance().queryForObject(sql, Integer.class);
	}

	/**
	 * 设置表头的计费重和体积重
	 * 
	 * @param billVO
	 * @param paramVO
	 * @param recompute
	 *            是否重新计算重量和体积
	 */
	public static void setHeaderCount(InvoiceVO invoiceVO, InvPackBVO[] invPackBVO, boolean recompute) {
		if(invoiceVO == null) {
			return;
		}
		if(invPackBVO == null||invPackBVO.length==0) {
			return;
		}
		InvoiceVO parentVO = invoiceVO;
		UFDouble pack_num_count = UFDouble.ZERO_DBL;
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		InvPackBVO[] childVOs = invPackBVO;
		if(childVOs != null && childVOs.length > 0) {
			// 根据导入的件数，单位重，单位体积，计算重量、体积
			for(InvPackBVO childVO : childVOs) {
				InvPackBVO ipVO =  childVO;
				if(ipVO.getStatus() != VOStatus.DELETED) {
					Integer num = ipVO.getNum() == null ? 0 : ipVO.getNum();// 件数
					if(recompute) {
						UFDouble unit_weight = ipVO.getUnit_weight() == null ? UFDouble.ZERO_DBL : ipVO
								.getUnit_weight();// 单位重
						UFDouble unit_volume = ipVO.getUnit_volume() == null ? UFDouble.ZERO_DBL : ipVO
								.getUnit_volume();// 单位体积
						ipVO.setWeight(unit_weight.multiply(new UFDouble(num)));
						ipVO.setVolume(unit_volume.multiply(new UFDouble(num)));
					}
					pack_num_count = pack_num_count.add(ipVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : ipVO
							.getPack_num_count());
					num_count += num;
					weight_count = weight_count.add(ipVO.getWeight() == null ? UFDouble.ZERO_DBL : ipVO.getWeight());
					volume_count = volume_count.add(ipVO.getVolume() == null ? UFDouble.ZERO_DBL : ipVO.getVolume());
				}
			}
		}
		parentVO.setPack_num_count(pack_num_count);
		parentVO.setNum_count(num_count);
		parentVO.setWeight_count(weight_count);
		parentVO.setVolume_count(volume_count);
		String pk_trans_type = parentVO.getPk_trans_type();
		if(StringUtils.isNotBlank(pk_trans_type)) {
			CustService custService = SpringContextHolder.getBean("custServiceImpl");
			double rate = custService.getFeeRate(parentVO.getPk_customer(), pk_trans_type, parentVO.getDeli_city(), parentVO.getArri_city()).doubleValue();
			UFDouble volume_weight_count = volume_count.multiply(rate);
			// 体积重
			parentVO.setVolume_weight_count(volume_weight_count);
			UFDouble fee = volume_weight_count; // 总体积/体积重换算比率
			if(fee.doubleValue() < weight_count.doubleValue()) {
				fee = weight_count;
			}
			parentVO.setFee_weight_count(fee);
		}
	}

	
	/**
	 * 设置表头的计费重和体积重
	 * 
	 * @param billVO
	 * @param paramVO
	 * @param recompute
	 *            是否重新计算重量和体积
	 */
	public static void setHeaderCount(AggregatedValueObject billVO, ParamVO paramVO, boolean recompute) {
		if (billVO == null) {
			return;
		}
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		UFDouble pack_num_count = UFDouble.ZERO_DBL;
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO;
		CircularlyAccessibleValueObject[] childVOs = aggVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		if (childVOs != null && childVOs.length > 0) {
			// 根据导入的件数，单位重，单位体积，计算重量、体积
			for (CircularlyAccessibleValueObject childVO : childVOs) {
				InvPackBVO ipVO = (InvPackBVO) childVO;
				if (ipVO.getStatus() != VOStatus.DELETED) {
					Integer num = ipVO.getNum() == null ? 0 : ipVO.getNum();// 件数
					if (recompute) {
						UFDouble unit_weight = ipVO.getUnit_weight() == null ? UFDouble.ZERO_DBL
								: ipVO.getUnit_weight();// 单位重
						UFDouble unit_volume = ipVO.getUnit_volume() == null ? UFDouble.ZERO_DBL
								: ipVO.getUnit_volume();// 单位体积
						ipVO.setWeight(unit_weight.multiply(new UFDouble(num)));
						ipVO.setVolume(unit_volume.multiply(new UFDouble(num)));
					}
					pack_num_count = pack_num_count
							.add(ipVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : ipVO.getPack_num_count());
					num_count += num;
					weight_count = weight_count.add(ipVO.getWeight() == null ? UFDouble.ZERO_DBL : ipVO.getWeight());
					volume_count = volume_count.add(ipVO.getVolume() == null ? UFDouble.ZERO_DBL : ipVO.getVolume());
				}
			}
		}
		parentVO.setPack_num_count(pack_num_count);
		parentVO.setNum_count(num_count);
		parentVO.setWeight_count(weight_count);
		parentVO.setVolume_count(volume_count);

		String pk_trans_type = parentVO.getPk_trans_type();
		if (StringUtils.isNotBlank(pk_trans_type)) {
			TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
					pk_trans_type);
			if (typeVO != null) {
				if (typeVO.getRate() != null) {
					UFDouble volume_weight_count = volume_count.multiply(typeVO.getRate());
					// 体积重
					parentVO.setVolume_weight_count(volume_weight_count);
					UFDouble fee = volume_weight_count; // 总体积/体积重换算比率
					if (fee.doubleValue() < weight_count.doubleValue()) {
						fee = weight_count;
					}
					parentVO.setFee_weight_count(fee);
				}
			}
		}
	}

	// 更新费用明细的金额信息
	public static void setBodyDetailAmount(InvoiceVO parentVO, ReceDetailBVO[] detailBVOs, TransBilityBVO[] tbBVOs) {
		if(parentVO == null || detailBVOs == null || detailBVOs.length == 0) {
			return;
		}

		Integer tb_num = new Integer(0);
		if(tbBVOs == null || tbBVOs.length == 0) {
			for(TransBilityBVO tbBVO : tbBVOs) {
				Integer num = tbBVO.getNum() == null ? new Integer(0) : tbBVO.getNum();
				tb_num += num;
			}
		}

		for(ReceDetailBVO detailBVO : detailBVOs) {
			Integer quote_type = detailBVO.getQuote_type();
			Integer valuation_type = detailBVO.getValuation_type();
			Integer price_type = detailBVO.getPrice_type();
			UFDouble price = detailBVO.getPrice();
			if(valuation_type == null || price == null) {
				return;
			}
			UFDouble amount = UFDouble.ZERO_DBL;
			if(quote_type.intValue() == 0) {// 区间报价
				if(price_type == 0) {// 价格类型=单价
					switch(valuation_type){
					case 0: // 重量
						UFDouble fee_weight_count = parentVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL
								: parentVO.getFee_weight_count();
						amount = fee_weight_count.multiply(price);
						break;
					case 1: // 体积
						UFDouble volume_count = parentVO.getVolume_count() == null ? UFDouble.ZERO_DBL : parentVO
								.getVolume_count();
						amount = volume_count.multiply(price);
						break;
					case 2: // 件数
						Integer num_count = parentVO.getNum_count() == null ? new Integer(0) : parentVO.getNum_count();
						amount = price.multiply(num_count);
						break;
					case 3: // 设备
						amount = price.multiply(tb_num);
						break;
					case 4: // 吨公里
						UFDouble weight_count = parentVO.getWeight_count() == null ? UFDouble.ZERO_DBL : parentVO
								.getWeight_count();
						UFDouble distance = parentVO.getDistance() == null ? UFDouble.ZERO_DBL : parentVO.getDistance();
						amount = (weight_count.div(1000)).multiply(distance).multiply(price);
						break;
					case 6: // 节点 FIXME 后面会用到
						break;
					}
					detailBVO.setAmount(amount);
				}
			} else {
				detailBVO.setPrice(UFDouble.ZERO_DBL);
			}
		}
	}

	// 更新表头的总金额
	public static void setHeaderCostAmount(InvoiceVO parentVO, ReceDetailBVO[] detailBVOs) {
		if(parentVO == null || detailBVOs == null || detailBVOs.length == 0) {
			return;
		}
		UFDouble cost_amount = UFDouble.ZERO_DBL;
		for(ReceDetailBVO detailBVO : detailBVOs) {
			if(detailBVO.getStatus() != VOStatus.DELETED) {
				cost_amount = cost_amount
						.add(detailBVO.getAmount() == null ? UFDouble.ZERO_DBL : detailBVO.getAmount());
			}
		}
		parentVO.setCost_amount(cost_amount);
	}

	/**
	 * 根据发货单的商品信息生成运段的商品信息
	 * 
	 * @param ipBVO
	 * @return
	 */
	public static SegPackBVO convert(InvPackBVO ipBVO) {
		SegPackBVO segPackVO = new SegPackBVO();
		segPackVO.setSerialno(ipBVO.getSerialno());
		segPackVO.setPk_invoice(ipBVO.getPk_invoice());
		segPackVO.setPk_inv_pack_b(ipBVO.getPk_inv_pack_b());
		segPackVO.setPk_goods(ipBVO.getPk_goods());
		segPackVO.setGoods_code(ipBVO.getGoods_code());
		segPackVO.setGoods_name(ipBVO.getGoods_name());
		segPackVO.setPlan_pack_num_count(ipBVO.getPlan_pack_num_count());// 计划数量
		segPackVO.setPack_num_count(ipBVO.getPack_num_count());// 数量
		segPackVO.setPlan_num(ipBVO.getPlan_num());// 计划件数
		segPackVO.setNum(ipBVO.getNum());// 件数
		segPackVO.setPack(ipBVO.getPack());
		segPackVO.setWeight(ipBVO.getWeight());
		segPackVO.setVolume(ipBVO.getVolume());
		segPackVO.setUnit_weight(ipBVO.getUnit_weight());
		segPackVO.setUnit_volume(ipBVO.getUnit_volume());
		segPackVO.setLength(ipBVO.getLength());
		segPackVO.setWidth(ipBVO.getWidth());
		segPackVO.setHeight(ipBVO.getHeight());
		segPackVO.setTrans_note(ipBVO.getTrans_note());
		segPackVO.setLow_temp(ipBVO.getLow_temp());
		segPackVO.setHight_temp(ipBVO.getHight_temp());
		segPackVO.setReference_no(ipBVO.getReference_no());
		segPackVO.setMemo(ipBVO.getMemo());
		
		segPackVO.setDef1(ipBVO.getDef1());
		segPackVO.setDef2(ipBVO.getDef2());
		segPackVO.setDef3(ipBVO.getDef3());
		segPackVO.setDef4(ipBVO.getDef4());
		segPackVO.setDef5(ipBVO.getDef5());
		segPackVO.setDef6(ipBVO.getDef6());
		segPackVO.setDef7(ipBVO.getDef7());
		segPackVO.setDef8(ipBVO.getDef8());
		segPackVO.setDef9(ipBVO.getDef9());
		segPackVO.setDef10(ipBVO.getDef10());
		segPackVO.setDef11(ipBVO.getDef11());
		segPackVO.setDef12(ipBVO.getDef12());
		
		return segPackVO;
	}
	
	public static void SegPackBAndEntCreation(InvPackBVO ipBVO,SegmentVO segmentVO) {
		SegPackBVO segPackVO = new SegPackBVO();
		segPackVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(segPackVO);
		segPackVO.setPk_segment(segmentVO.getPk_segment());
		segPackVO.setSerialno(ipBVO.getSerialno());
		segPackVO.setPk_invoice(ipBVO.getPk_invoice());
		segPackVO.setPk_inv_pack_b(ipBVO.getPk_inv_pack_b());
		segPackVO.setPk_goods(ipBVO.getPk_goods());
		segPackVO.setGoods_code(ipBVO.getGoods_code());
		segPackVO.setGoods_name(ipBVO.getGoods_name());
		segPackVO.setPlan_pack_num_count(ipBVO.getPlan_pack_num_count());// 计划数量
		segPackVO.setPack_num_count(ipBVO.getPack_num_count());// 数量
		segPackVO.setPlan_num(ipBVO.getPlan_num());// 计划件数
		segPackVO.setNum(ipBVO.getNum());// 件数
		segPackVO.setPack(ipBVO.getPack());
		segPackVO.setWeight(ipBVO.getWeight());
		segPackVO.setVolume(ipBVO.getVolume());
		segPackVO.setUnit_weight(ipBVO.getUnit_weight());
		segPackVO.setUnit_volume(ipBVO.getUnit_volume());
		segPackVO.setLength(ipBVO.getLength());
		segPackVO.setWidth(ipBVO.getWidth());
		segPackVO.setHeight(ipBVO.getHeight());
		segPackVO.setTrans_note(ipBVO.getTrans_note());
		segPackVO.setLow_temp(ipBVO.getLow_temp());
		segPackVO.setHight_temp(ipBVO.getHight_temp());
		segPackVO.setReference_no(ipBVO.getReference_no());
		segPackVO.setMemo(ipBVO.getMemo());
		NWDao.getInstance().saveOrUpdate(segPackVO);
		//看看这个运段有没有对应的委托单，如果有，给委托单也生成一个包装明细及节点货品表
		EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "segment_vbillno=?", segmentVO.getVbillno());
		if(entrustVO != null){
			EntPackBVO entPackBVO = new EntPackBVO();
			entPackBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(entPackBVO);
			entPackBVO.setPk_entrust(entrustVO.getPk_entrust());
			entPackBVO.setPk_segment(segmentVO.getPk_segment());
			entPackBVO.setSerialno(segPackVO.getSerialno());
			entPackBVO.setPk_invoice(segPackVO.getPk_invoice());
			entPackBVO.setPk_seg_pack_b(segPackVO.getPk_seg_pack_b());
			entPackBVO.setPk_segment(segPackVO.getPk_segment());
			entPackBVO.setPk_goods(segPackVO.getPk_goods());
			entPackBVO.setGoods_code(segPackVO.getGoods_code());
			entPackBVO.setGoods_name(segPackVO.getGoods_name());
			entPackBVO.setPlan_pack_num_count(segPackVO.getPlan_pack_num_count());// 计划数量
			entPackBVO.setPack_num_count(segPackVO.getPack_num_count());// 数量
			entPackBVO.setPlan_num(segPackVO.getPlan_num());// 计划件数
			entPackBVO.setNum(segPackVO.getNum());// 件数
			entPackBVO.setPack(segPackVO.getPack());
			entPackBVO.setWeight(segPackVO.getWeight());
			entPackBVO.setVolume(segPackVO.getVolume());
			entPackBVO.setUnit_weight(segPackVO.getUnit_weight());
			entPackBVO.setUnit_volume(segPackVO.getUnit_volume());
			entPackBVO.setLength(segPackVO.getLength());
			entPackBVO.setWidth(segPackVO.getWidth());
			entPackBVO.setHeight(segPackVO.getHeight());
			entPackBVO.setTrans_note(segPackVO.getTrans_note());
			entPackBVO.setLow_temp(segPackVO.getLow_temp());
			entPackBVO.setHight_temp(segPackVO.getHight_temp());
			entPackBVO.setReference_no(segPackVO.getReference_no());
			entPackBVO.setMemo(segPackVO.getMemo());
			NWDao.getInstance().saveOrUpdate(entPackBVO);
			
			EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust=?", entrustVO.getPk_entrust());
			if(entLineBVOs != null && entLineBVOs.length > 0){
				for(EntLineBVO entLineBVO : entLineBVOs){
					EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
					entLinePackBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(entLinePackBVO);
					entLinePackBVO.setPk_entrust(entLineBVO.getPk_entrust());
					entLinePackBVO.setPk_ent_line_b(entLinePackBVO.getPk_ent_line_b());
					entLinePackBVO.setPk_ent_pack_b(entPackBVO.getPk_ent_pack_b());
					entLinePackBVO.setSerialno(entPackBVO.getSerialno());
					entLinePackBVO.setPk_goods(entPackBVO.getPk_goods());
					entLinePackBVO.setGoods_code(entPackBVO.getGoods_code());
					entLinePackBVO.setGoods_name(entPackBVO.getGoods_name());
					entLinePackBVO.setPlan_pack_num_count(entPackBVO.getPlan_pack_num_count());// 计划数量
					entLinePackBVO.setPack_num_count(entPackBVO.getPack_num_count());// 数量
					entLinePackBVO.setPlan_num(entPackBVO.getPlan_num());// 计划件数
					entLinePackBVO.setNum(entPackBVO.getNum());// 件数
					entLinePackBVO.setPack(entPackBVO.getPack());
					entLinePackBVO.setWeight(entPackBVO.getWeight());
					entLinePackBVO.setVolume(entPackBVO.getVolume());
					entLinePackBVO.setUnit_weight(entPackBVO.getUnit_weight());
					entLinePackBVO.setUnit_volume(entPackBVO.getUnit_volume());
					entLinePackBVO.setLength(entPackBVO.getLength());
					entLinePackBVO.setWidth(entPackBVO.getWidth());
					entLinePackBVO.setHeight(entPackBVO.getHeight());
					entLinePackBVO.setTrans_note(entPackBVO.getTrans_note());
					entLinePackBVO.setLow_temp(entPackBVO.getLow_temp());
					entLinePackBVO.setHight_temp(entPackBVO.getHight_temp());
					entLinePackBVO.setReference_no(entPackBVO.getReference_no());
					entLinePackBVO.setMemo(entPackBVO.getMemo());
					NWDao.getInstance().saveOrUpdate(entLinePackBVO);
				}
			}
		}
		
	}
	
	public static InvPackBVO invPackCreation(InvLineBVO invLineBVO ){
		NWDao.setUuidPrimaryKey(invLineBVO);
		InvPackBVO invPackBVO = new InvPackBVO();
		invPackBVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(invPackBVO);
		invPackBVO.setPk_invoice(invLineBVO.getPk_invoice());
		invPackBVO.setPk_inv_line_b(invLineBVO.getPk_inv_line_b());
		invPackBVO.setPk_goods(invLineBVO.getPk_goods());
		invPackBVO.setGoods_code(invLineBVO.getGoods_code());
		invPackBVO.setGoods_name(invLineBVO.getGoods_name());
		invPackBVO.setPlan_pack_num_count(invLineBVO.getPlan_pack_num_count());// 计划数量
		invPackBVO.setPack_num_count(invLineBVO.getPack_num_count());// 数量
		invPackBVO.setPlan_num(invLineBVO.getPlan_num());// 计划件数
		invPackBVO.setNum(invLineBVO.getNum());// 件数
		invPackBVO.setPack(invLineBVO.getPack());
		invPackBVO.setWeight(invLineBVO.getWeight());
		invPackBVO.setVolume(invLineBVO.getVolume());
		invPackBVO.setUnit_weight(invLineBVO.getUnit_weight());
		invPackBVO.setUnit_volume(invLineBVO.getUnit_volume());
		invPackBVO.setLength(invLineBVO.getLength());
		invPackBVO.setWidth(invLineBVO.getWidth());
		invPackBVO.setHeight(invLineBVO.getHeight());
		invPackBVO.setTrans_note(invLineBVO.getTrans_note());
		invPackBVO.setLow_temp(invLineBVO.getLow_temp());
		invPackBVO.setHight_temp(invLineBVO.getHight_temp());
		invPackBVO.setReference_no(invLineBVO.getReference_no());
		return invPackBVO;
	}
	
	public static InvPackBVO segPackCreation(InvLineBVO invLineBVO ){
		NWDao.setUuidPrimaryKey(invLineBVO);
		InvPackBVO invPackBVO = new InvPackBVO();
		invPackBVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(invPackBVO);
		invPackBVO.setPk_invoice(invLineBVO.getPk_invoice());
		invPackBVO.setPk_inv_line_b(invLineBVO.getPk_inv_line_b());
		invPackBVO.setPk_goods(invLineBVO.getPk_goods());
		invPackBVO.setGoods_code(invLineBVO.getGoods_code());
		invPackBVO.setGoods_name(invLineBVO.getGoods_name());
		invPackBVO.setPlan_pack_num_count(invLineBVO.getPlan_pack_num_count());// 计划数量
		invPackBVO.setPack_num_count(invLineBVO.getPack_num_count());// 数量
		invPackBVO.setPlan_num(invLineBVO.getPlan_num());// 计划件数
		invPackBVO.setNum(invLineBVO.getNum());// 件数
		invPackBVO.setPack(invLineBVO.getPack());
		invPackBVO.setWeight(invLineBVO.getWeight());
		invPackBVO.setVolume(invLineBVO.getVolume());
		invPackBVO.setUnit_weight(invLineBVO.getUnit_weight());
		invPackBVO.setUnit_volume(invLineBVO.getUnit_volume());
		invPackBVO.setLength(invLineBVO.getLength());
		invPackBVO.setWidth(invLineBVO.getWidth());
		invPackBVO.setHeight(invLineBVO.getHeight());
		invPackBVO.setTrans_note(invLineBVO.getTrans_note());
		invPackBVO.setLow_temp(invLineBVO.getLow_temp());
		invPackBVO.setHight_temp(invLineBVO.getHight_temp());
		invPackBVO.setReference_no(invLineBVO.getReference_no());
		return invPackBVO;
	}
	
	public static SegmentVO SegmentAndEntrustCreation(InvLineBVO invLineBVO ,InvoiceVO invoiceVO){
		if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
			//看看发货单状态
			if(invoiceVO.getVbillstatus() == BillStatus.INV_ARRIVAL
					&& invoiceVO.getVbillstatus() != BillStatus.INV_PART_SIGN
					&& invoiceVO.getVbillstatus() != BillStatus.INV_SIGN
					&& invoiceVO.getVbillstatus() != BillStatus.INV_BACK){
				//这种情况不允许再加节点了。
				throw new BusiException("多提一送业务，单据已经完成运输，不允许新增节点！");
			}
		}
		SegmentVO segVO = new SegmentVO();
		segVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(segVO);
		segVO.setDbilldate(new UFDate());
		segVO.setInvoice_vbillno(invoiceVO.getVbillno());
		segVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YDPZ));
		segVO.setPk_trans_type(invoiceVO.getPk_trans_type());// 运输方式，这个字段在计算总计费重时需要使用
		segVO.setVbillstatus(BillStatus.SEG_WPLAN); // 待计划
		segVO.setSeg_type(SegmentConst.SECTION); // 分段运段
		segVO.setSeg_mark(SegmentConst.SEG_MARK_NORMAL);// 运段标识
		segVO.setMileage(invoiceVO.getMileage());
		segVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		segVO.setCreate_time(new UFDateTime(new Date()));
		segVO.setPk_corp(invoiceVO.getPk_corp());
		segVO.setDeli_method(invoiceVO.getDeli_method());// 派送方式
		segVO.setDistance(invoiceVO.getDistance());
		segVO.setMemo(invoiceVO.getMemo());
		segVO.setDeli_process(invoiceVO.getDeli_process());
		segVO.setArri_process(invoiceVO.getArri_process());
		segVO.setNote(invoiceVO.getNote());
		segVO.setPz_line(invoiceVO.getPz_line());
		segVO.setPz_mileage(invoiceVO.getPz_mileage());
		segVO.setNote(invoiceVO.getNote());
		segVO.setUrgent_level(invoiceVO.getUrgent_level());
		if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
			segVO.setReq_deli_date(invoiceVO.getReq_deli_date());
			segVO.setReq_deli_time(invoiceVO.getReq_deli_time());
			segVO.setPk_delivery(invoiceVO.getPk_delivery());
			segVO.setDeli_city(invoiceVO.getDeli_city());
			segVO.setDeli_province(invoiceVO.getDeli_province());
			segVO.setDeli_area(invoiceVO.getDeli_area());
			segVO.setDeli_detail_addr(invoiceVO.getDeli_detail_addr());
			segVO.setDeli_contact(invoiceVO.getDeli_contact());
			segVO.setDeli_mobile(invoiceVO.getDeli_mobile());
			segVO.setDeli_phone(invoiceVO.getDeli_phone());
			segVO.setDeli_email(invoiceVO.getDeli_mobile());
			
			segVO.setReq_arri_date(invLineBVO.getReq_date_from().toString());
			segVO.setReq_arri_time(invLineBVO.getReq_date_from().toString());
			segVO.setPk_arrival(invLineBVO.getPk_address());
			segVO.setArri_city(invLineBVO.getPk_city());
			segVO.setArri_province(invLineBVO.getPk_province());
			segVO.setArri_area(invLineBVO.getPk_area());
			segVO.setArri_detail_addr(invLineBVO.getDetail_addr());
			segVO.setArri_contact(invLineBVO.getContact());
			segVO.setArri_mobile(invLineBVO.getMobile());
			segVO.setArri_phone(invLineBVO.getPhone());
			segVO.setArri_email(invLineBVO.getEmail());
		}else if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
			segVO.setReq_deli_date(invLineBVO.getReq_date_from().toString());
			segVO.setReq_deli_time(invLineBVO.getReq_date_from().toString());
			segVO.setPk_delivery(invLineBVO.getPk_address());
			segVO.setDeli_city(invLineBVO.getPk_city());
			segVO.setDeli_province(invLineBVO.getPk_province());
			segVO.setDeli_area(invLineBVO.getPk_area());
			segVO.setDeli_detail_addr(invLineBVO.getDetail_addr());
			segVO.setDeli_contact(invLineBVO.getContact());
			segVO.setDeli_mobile(invLineBVO.getMobile());
			segVO.setDeli_phone(invLineBVO.getPhone());
			segVO.setDeli_email(invLineBVO.getEmail());
			
			segVO.setReq_arri_date(invoiceVO.getReq_arri_date());
			segVO.setReq_arri_time(invoiceVO.getReq_deli_time());
			segVO.setPk_arrival(invoiceVO.getPk_arrival());
			segVO.setArri_city(invoiceVO.getArri_city());
			segVO.setArri_province(invoiceVO.getArri_province());
			segVO.setArri_area(invoiceVO.getArri_area());
			segVO.setArri_detail_addr(invoiceVO.getArri_detail_addr());
			segVO.setArri_contact(invoiceVO.getArri_contact());
			segVO.setArri_mobile(invoiceVO.getArri_mobile());
			segVO.setArri_phone(invoiceVO.getArri_phone());
			segVO.setArri_email(invoiceVO.getArri_mobile());
		}
		NWDao.getInstance().saveOrUpdate(segVO);
		//查看这个发货单有没有对应的委托单
		String sql = " SELECT TOP 1 * FROM ts_entrust WITH(NOLOCK) "
				+ " WHERE isnull(dr,0)=0 AND invoice_vbillno=?";
		EntrustVO entrustVO = NWDao.getInstance().queryForObject(sql, EntrustVO.class, invoiceVO.getVbillno());
		if(entrustVO == null){
			//没有委托单
			return segVO;
		}
		//对这个运段进行配载
		PZHeaderVO pzHeaderVO = new PZHeaderVO();
		pzHeaderVO.setPk_carrier(entrustVO.getPk_carrier());
		pzHeaderVO.setPk_trans_type(entrustVO.getPk_trans_type());
		pzHeaderVO.setBalatype(entrustVO.getBalatype());
		pzHeaderVO.setMemo(entrustVO.getMemo());
		pzHeaderVO.setLot(entrustVO.getLot());
		ExAggEntrustVO pzAggVO = new ExAggEntrustVO();
		pzAggVO.setParentVO(pzHeaderVO);
		EntTransbilityBVO[] entTransbilityVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust=?", entrustVO.getPk_entrust());
		pzAggVO.setTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B, entTransbilityVOs);
		pzAggVO.setTableVO(TabcodeConst.TS_SEGMENT, new SegmentVO[]{segVO});
		PZService pZService = (PZService) SpringContextHolder.getBean("PZServiceImpl");
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.SEG_BATCH_PZ_CODE);
		AggregatedValueObject billVO = pZService.save(pzAggVO,paramVO);
		ExAggEntrustVO exAggEntrustVO = (ExAggEntrustVO) billVO;
		if(billVO == null){
			return segVO;
		}
		EntLineBVO[] entLineBVOs = (EntLineBVO[]) exAggEntrustVO.getTableVO(TabcodeConst.TS_ENT_LINE_B);
		if(entLineBVOs == null || entLineBVOs.length == 0){
			return segVO;
		}
		//看看发货单是什么状态
		//先看看类型
		if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
			if(invoiceVO.getVbillstatus() != BillStatus.INV_CONFIRM
					&& invoiceVO.getVbillstatus() != BillStatus.NEW
					&& invoiceVO.getVbillstatus() != BillStatus.INV_CLOSE){
				//要把这个委托的提货提掉
				for(EntLineBVO entLineBVO : entLineBVOs){
					if(entLineBVO.getAddr_flag().equals("S")){
						//看看这个发货单有没有对应的签收单
						PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pk_invoice=?", invoiceVO.getPk_invoice());
						if(podVO != null){
							//删除这个签收信息，直接删除就行。
							NWDao.getInstance().delete(podVO);
						}
						TrackingService trackingService = (TrackingService) SpringContextHolder.getApplicationContext().getBean("trackingServiceImpl");
						trackingService.confirmArrival(entLineBVO, 0);
					}
				}
			}
		}
		return segVO;
	}
	
	/**
	 * 检查订单的运输方式返回数据字典 TRANSPORT_TYPE 对应的值
	 * <br/>
	 * 这个方法不返回null
	 * <br/>
	 * 默认 DataDictConst.TRANSPORT_TYPE.LD.intValue()
	 * 
	 * @param invLineBVOs
	 * @return
	 */
	public static Integer getTransportType(InvLineBVO[] invLineBVOs){
		if(invLineBVOs == null || invLineBVOs.length == 0){
			return DataDictConst.TRANSPORT_TYPE.LD.intValue();
		}
		//对输入的线路信息进行分组，获取pickup和delivery的数据
		Map<Integer,Map<String,List<InvLineBVO>>> groupMap = new HashMap<Integer, Map<String,List<InvLineBVO>>>();
		for(InvLineBVO lineBVO : invLineBVOs){
			//过滤状态是删除的线路
			if(lineBVO.getStatus() == VOStatus.DELETED){
				continue;
			}
			Integer operation = lineBVO.getOperate_type();
			Map<String,List<InvLineBVO>> sameOperationLineVOs = groupMap.get(operation);
			if(sameOperationLineVOs == null){
				sameOperationLineVOs = new HashMap<String, List<InvLineBVO>>();
				groupMap.put(operation, sameOperationLineVOs);
			}
			String addressAndDate = lineBVO.getPk_address() + lineBVO.getReq_date_from();
			List<InvLineBVO> sameAddressLineVOs = sameOperationLineVOs.get(addressAndDate);
			if(sameAddressLineVOs == null){
				sameAddressLineVOs = new ArrayList<InvLineBVO>();
				sameOperationLineVOs.put(addressAndDate, sameAddressLineVOs);
			}
			sameAddressLineVOs.add(lineBVO);
		}
		//节点信息不完整，这时认为是错误的数据
		if(groupMap.get(OperateTypeConst.PICKUP) == null || groupMap.get(OperateTypeConst.PICKUP).size() == 0
				|| groupMap.get(OperateTypeConst.DELIVERY) == null || groupMap.get(OperateTypeConst.DELIVERY).size() == 0){
			return DataDictConst.TRANSPORT_TYPE.ERROR.intValue();
		//提货点和收货点的数量都比1大，milkrun
		}else if(groupMap.get(OperateTypeConst.PICKUP).size() > 1 && groupMap.get(OperateTypeConst.DELIVERY).size() > 1){
			return DataDictConst.TRANSPORT_TYPE.DD.intValue();
		//一提多送
		}else if(groupMap.get(OperateTypeConst.PICKUP).size() == 1 && groupMap.get(OperateTypeConst.DELIVERY).size() > 1){
			return DataDictConst.TRANSPORT_TYPE.YD.intValue();
		//多提一送
		}else if(groupMap.get(OperateTypeConst.PICKUP).size() > 1 && groupMap.get(OperateTypeConst.DELIVERY).size() == 1){
			return DataDictConst.TRANSPORT_TYPE.DY.intValue();
		}
		return DataDictConst.TRANSPORT_TYPE.ERROR.intValue();
	}
	
	
	
	/**
	 * 修改多点运输单
	 * @param billVO
	 * @param paramVO
	 */
	public static void syncMultiPointTransportation(AggregatedValueObject billVO, ParamVO paramVO){
		if(billVO == null){
			return;
		}
		ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) billVO;
		InvoiceVO invoiceVO = (InvoiceVO) aggInvoiceVO.getParentVO();
		InvPackBVO[] invPackBVOs = (InvPackBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		InvLineBVO[] invLineBVOs = (InvLineBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
		
		Integer transportType = getTransportType(invLineBVOs);
		if(transportType == DataDictConst.TRANSPORT_TYPE.ERROR.intValue()){
			throw new BusiException("多点运输单[?],提到货信息不完整，不能确认!",invoiceVO.getVbillno());
		}else if(transportType == DataDictConst.TRANSPORT_TYPE.DD.intValue()){
			throw new BusiException("多点运输单[?],出现多点提送业务,此业务尚不支持!");
		}
		//订单修订
		if(invoiceVO.getVbillstatus() != BillStatus.NEW){
			//如果订单没有运输方式类型，则把运输类型赋值，否则校验运输方式类型是否一致
			if(invoiceVO.getTrans_type() == null ){
				invoiceVO.setTrans_type(transportType);
			}else if(!transportType.equals(invoiceVO.getTrans_type())){
				throw new BusiException("订单修订的时候，运输类型不允许发生改变！");
			}
		}else{
			//新建状态下，允许直接修改运输方式类型
			invoiceVO.setTrans_type(transportType);
		}
		
		
		//转换成list方便操作
		List<InvPackBVO> invPackBVOList = new ArrayList<InvPackBVO>();
		//先将所有包装都认为是要删除的
		for(InvPackBVO invPackBVO : invPackBVOs){
			if(invPackBVO.getStatus() == VOStatus.NEW){
				//如果是复制单据，会出现状态是新建的单子，如果这些单据被删除，会导致主键为空
				NWDao.setUuidPrimaryKey(invPackBVO);
			}
			invPackBVO.setStatus(VOStatus.DELETED);
			invPackBVOList.add(invPackBVO);
		}
		//生成包装信息
		for(InvLineBVO invLineBVO : invLineBVOs){
			//看看是什么业务
			if(transportType == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
				//一提多送 不需要关心提货点
				if(invLineBVO.getOperate_type() == OperateTypeConst.PICKUP){
					continue;
				}
			}else if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
				//多提一送 不需要关心到货点
				if(invLineBVO.getOperate_type() == OperateTypeConst.DELIVERY){
					continue;
				}
			}
			//预先为所有新建的line设置PK，否则在和包装匹配的时候麻烦
			if(StringUtils.isBlank(invLineBVO.getPk_inv_line_b())){
				NWDao.setUuidPrimaryKey(invLineBVO);
			}
			if(StringUtils.isBlank(invLineBVO.getPk_invoice())){
				invLineBVO.setPk_invoice(invoiceVO.getPk_invoice());
			}
			//只有有货品的我们才生成包装信息
			if(StringUtils.isNotBlank(invLineBVO.getGoods_code())){
				if(invLineBVO.getStatus() == VOStatus.NEW){
					//新建,直接生成包装就行。
					//新建一份包装信息
					invPackBVOList.add(invPackCreation(invLineBVO));
				} else if(invLineBVO.getStatus() == VOStatus.UPDATED || invLineBVO.getStatus() == VOStatus.UNCHANGED){
					InvPackBVO invPackBVOtemp = null;
					for(InvPackBVO invPackBVO : invPackBVOs){
						if(invLineBVO.getPk_inv_line_b().equals(invPackBVO.getPk_inv_line_b())){
							invPackBVOtemp = invPackBVO;
							break;
						}
					}
					if(invPackBVOtemp == null){
						if(StringUtils.isNotBlank(invLineBVO.getGoods_code())){
							//新建一份包装信息
							invPackBVOList.add(invPackCreation(invLineBVO));
						}
					}else{
						//存在货品包装货品编码也有，这时候更新一下
						if(StringUtils.isNotBlank(invLineBVO.getGoods_code())){
							invPackBVOtemp.setPk_goods(invLineBVO.getPk_goods());
							invPackBVOtemp.setGoods_code(invLineBVO.getGoods_code());
							invPackBVOtemp.setGoods_name(invLineBVO.getGoods_name());
							invPackBVOtemp.setPlan_pack_num_count(invLineBVO.getPlan_pack_num_count());// 计划数量
							invPackBVOtemp.setPack_num_count(invLineBVO.getPack_num_count());// 数量
							invPackBVOtemp.setPlan_num(invLineBVO.getPlan_num());// 计划件数
							invPackBVOtemp.setNum(invLineBVO.getNum());// 件数
							invPackBVOtemp.setPack(invLineBVO.getPack());
							invPackBVOtemp.setWeight(invLineBVO.getWeight());
							invPackBVOtemp.setVolume(invLineBVO.getVolume());
							invPackBVOtemp.setUnit_weight(invLineBVO.getUnit_weight());
							invPackBVOtemp.setUnit_volume(invLineBVO.getUnit_volume());
							invPackBVOtemp.setLength(invLineBVO.getLength());
							invPackBVOtemp.setWidth(invLineBVO.getWidth());
							invPackBVOtemp.setHeight(invLineBVO.getHeight());
							invPackBVOtemp.setTrans_note(invLineBVO.getTrans_note());
							invPackBVOtemp.setLow_temp(invLineBVO.getLow_temp());
							invPackBVOtemp.setHight_temp(invLineBVO.getHight_temp());
							invPackBVOtemp.setReference_no(invLineBVO.getReference_no());
							//line如果是修改过的，那么pack也跟着修改一下，如果line是未改动的，说明pack也不需要变动。
							//这里更新一下状态就可以了。
							invPackBVOtemp.setStatus(invLineBVO.getStatus());
						}else{
							//不管他就行，会被删除。
						}
					}
				}else if(invLineBVO.getStatus() == VOStatus.DELETED){
					//删除对应的包装信息(上面已经统一删除了)
				} 
			}
		}
		//对于发货单是新建状态，的只需要修改发货单对应的包装和发货单信息即可，对于发货单非新建状态的的需要不仅需要修改包装信息，还要修改对应的运段及委托的信息
		List<String> canDirectDelete = new ArrayList<String>();
		if(invoiceVO.getVbillstatus() != BillStatus.NEW){//订单修订
			for(InvLineBVO invLineBVO : invLineBVOs){
				//看看是什么业务
				if(transportType == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
					//一提多送 不需要关心提货点
					if(invLineBVO.getOperate_type() == OperateTypeConst.PICKUP){
						continue;
					}
				}else if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
					//多提一送 不需要关心到货点
					if(invLineBVO.getOperate_type() == OperateTypeConst.DELIVERY){
						continue;
					}
				}
				//老规矩，只获取有用的线路
				if(invLineBVO.getStatus() == VOStatus.NEW){
					//获取运段
					SegmentVO segmentVO = null;
					if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
						segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, 
								"pk_delivery=? and pk_arrival=? and req_arri_date=? and invoice_vbillno=?",
								invoiceVO.getPk_delivery(),invLineBVO.getPk_address(),invLineBVO.getReq_date_from().toString(),invoiceVO.getVbillno());
					}else if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
						segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, 
								"pk_delivery=? and pk_arrival=? and req_deli_date=? and invoice_vbillno=?",
								invLineBVO.getPk_address(),invoiceVO.getPk_arrival(),invLineBVO.getReq_date_from().toString(),invoiceVO.getVbillno());
					}
					//没有则生成一个
					if(segmentVO == null){
						//判断是否满足创造运段的条件
						if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
							//多提一送，如果发货单到货不允许再加节点
							if(invoiceVO.getVbillstatus() >= BillStatus.INV_ARRIVAL ){
								throw new BusiException("多提一送业务，在发货单已到货情况下，不允许新增节点！");
							}
						}
						segmentVO = SegmentAndEntrustCreation(invLineBVO, invoiceVO);
						//找到那个包装明细
						InvPackBVO tempInvPackBVO = null;
						for (InvPackBVO invPackBVO : invPackBVOList){
							//新建必然对应新建
							if(invPackBVO.getStatus() == VOStatus.NEW){
								if(invLineBVO.getPk_inv_line_b().equals(invPackBVO.getPk_inv_line_b())){
									tempInvPackBVO = invPackBVO;
									break;
								}
							}
						}
						if(tempInvPackBVO != null){
							//创建一个包装
							SegPackBAndEntCreation(tempInvPackBVO,segmentVO);
						}
					}else{
						//如果有，看看有没有包装明细，这时只需要处理包装信息即可
						//找到那个包装明细
						InvPackBVO tempInvPackBVO = null;
						for (InvPackBVO invPackBVO : invPackBVOList){
							//新建必然对应新建
							if(invPackBVO.getStatus() == VOStatus.NEW){
								if(invLineBVO.getPk_inv_line_b().equals(invPackBVO.getPk_inv_line_b())){
									tempInvPackBVO = invPackBVO;
									break;
								}
							}
						}
						if(tempInvPackBVO != null){
							//生成一个包装明细
							if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()
									&& segmentVO.getVbillstatus() >= BillStatus.SEG_DELIVERY){
								throw new BusiException("多提一送业务，在运段[?]已提货情况下，不允许修改节点信息！",segmentVO.getVbillno());
							}
							if(segmentVO.getVbillstatus() >= BillStatus.SEG_ARRIVAL){
								throw new BusiException("运段[?]已到货，不允许修改货品信息！",segmentVO.getVbillno());
							}
							SegPackBAndEntCreation(tempInvPackBVO,segmentVO);
						}
					}
				}else if(invLineBVO.getStatus() == VOStatus.UPDATED){
					//更新包括两个方面 1，修改对应的运段信息,第二，修改包装信息。
					//找出系统原有的运段
					InvLineBVO invLineBVO_db = NWDao.getInstance().queryByCondition(InvLineBVO.class, "pk_inv_line_b=?", invLineBVO.getPk_inv_line_b());
					if(invLineBVO_db == null){
						//没有是不科学的，也是不可能的，这里跳过。
						continue;
					}
					SegmentVO segmentVO = null;
					//找出系统原有的那个运段
					if(transportType == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
						segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_delivery=? and pk_arrival=? and req_arri_date=? and invoice_vbillno=?",
								invoiceVO.getPk_delivery(),invLineBVO_db.getPk_address(),invLineBVO_db.getReq_date_from().toString(),invoiceVO.getVbillno());
					}else if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
						segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_delivery=? and pk_arrival=? and req_deli_date=? and invoice_vbillno=?",
								invLineBVO_db.getPk_address(),invoiceVO.getPk_arrival(),invLineBVO_db.getReq_date_from().toString(),invoiceVO.getVbillno());
					}
					//如果系统原有的那个运段不存在了，可能是存在多行数据对应同一个运段，那么在上一行数据进行操作的时候，就会把系统之前的运段修改掉
					//此时就查不出对应的运段了，那么应该按照最新输入的数据，进行查询，如果查询出结果，那么修改这个运段，否则新增
					if(segmentVO == null){
						if(transportType == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
							segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_delivery=? and pk_arrival=? and req_arri_date=? and invoice_vbillno=?",
									invoiceVO.getPk_delivery(),invLineBVO.getPk_address(),invLineBVO.getReq_date_from().toString(),invoiceVO.getVbillno());
						}else if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
							segmentVO = NWDao.getInstance().queryByCondition(SegmentVO.class, "pk_delivery=? and pk_arrival=? and req_deli_date=? and invoice_vbillno=?",
									invLineBVO.getPk_address(),invoiceVO.getPk_arrival(),invLineBVO.getReq_date_from().toString(),invoiceVO.getVbillno());
						}
					}
					if(segmentVO == null){
						//判断是否满足创造运段的条件
						if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
							//多提一送，如果发货单到货不允许再加节点
							if(invoiceVO.getVbillstatus() >= BillStatus.INV_ARRIVAL ){
								throw new BusiException("多提一送业务，在发货单已到货情况下，不允许新增节点！");
							}
						}
						segmentVO = SegmentAndEntrustCreation(invLineBVO, invoiceVO);
						//找到那个包装明细
						InvPackBVO tempInvPackBVO = null;
						for (InvPackBVO invPackBVO : invPackBVOList){
							if(invPackBVO.getStatus() != VOStatus.DELETED){
								if(invLineBVO.getPk_inv_line_b().equals(invPackBVO.getPk_inv_line_b())){
									tempInvPackBVO = invPackBVO;
									break;
								}
							}
						}
						if(tempInvPackBVO != null){
							//把原来的那个运段里的包装删除掉
							SegPackBVO segPackBVO_db = NWDao.getInstance().queryByCondition(SegPackBVO.class, "pk_inv_pack_b=?", tempInvPackBVO.getPk_inv_pack_b());
							if(segPackBVO_db != null){
								NWDao.getInstance().delete(segPackBVO_db);
							}
							//生成一个包装明细
							SegPackBAndEntCreation(tempInvPackBVO,segmentVO);
						}
					}else{
						if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()
								&& segmentVO.getVbillstatus() >= BillStatus.SEG_DELIVERY){
							throw new BusiException("多提一送业务，在运段[?]已提货情况下，不允许修改节点信息！",segmentVO.getVbillno());
						}
						if(segmentVO.getVbillstatus() >= BillStatus.SEG_ARRIVAL){
							throw new BusiException("运段[?]已到货，不允许修改货品信息！",segmentVO.getVbillno());
						}
						//如果有，修改这个运段的信息
						segmentVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
						segmentVO.setModify_time(new UFDateTime(new Date()));
						if(transportType == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
							segmentVO.setReq_deli_date(invoiceVO.getReq_deli_date());
							segmentVO.setReq_deli_time(invoiceVO.getReq_deli_time());
							segmentVO.setPk_delivery(invoiceVO.getPk_delivery());
							segmentVO.setDeli_city(invoiceVO.getDeli_city());
							segmentVO.setDeli_province(invoiceVO.getDeli_province());
							segmentVO.setDeli_area(invoiceVO.getDeli_area());
							segmentVO.setDeli_detail_addr(invoiceVO.getDeli_detail_addr());
							segmentVO.setDeli_contact(invoiceVO.getDeli_contact());
							segmentVO.setDeli_mobile(invoiceVO.getDeli_mobile());
							segmentVO.setDeli_phone(invoiceVO.getDeli_phone());
							segmentVO.setDeli_email(invoiceVO.getDeli_mobile());
							
							segmentVO.setReq_arri_date(invLineBVO.getReq_date_from().toString());
							segmentVO.setReq_arri_time(invLineBVO.getReq_date_from().toString());
							segmentVO.setPk_arrival(invLineBVO.getPk_address());
							segmentVO.setArri_city(invLineBVO.getPk_city());
							segmentVO.setArri_province(invLineBVO.getPk_province());
							segmentVO.setArri_area(invLineBVO.getPk_area());
							segmentVO.setArri_detail_addr(invLineBVO.getDetail_addr());
							segmentVO.setArri_contact(invLineBVO.getContact());
							segmentVO.setArri_mobile(invLineBVO.getMobile());
							segmentVO.setArri_phone(invLineBVO.getPhone());
							segmentVO.setArri_email(invLineBVO.getEmail());
						}else if(transportType == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
							segmentVO.setReq_deli_date(invLineBVO.getReq_date_from().toString());
							segmentVO.setReq_deli_time(invLineBVO.getReq_date_from().toString());
							segmentVO.setPk_delivery(invLineBVO.getPk_address());
							segmentVO.setDeli_city(invLineBVO.getPk_city());
							segmentVO.setDeli_province(invLineBVO.getPk_province());
							segmentVO.setDeli_area(invLineBVO.getPk_area());
							segmentVO.setDeli_detail_addr(invLineBVO.getDetail_addr());
							segmentVO.setDeli_contact(invLineBVO.getContact());
							segmentVO.setDeli_mobile(invLineBVO.getMobile());
							segmentVO.setDeli_phone(invLineBVO.getPhone());
							segmentVO.setDeli_email(invLineBVO.getEmail());
							
							segmentVO.setReq_arri_date(invoiceVO.getReq_arri_date());
							segmentVO.setReq_arri_time(invoiceVO.getReq_deli_time());
							segmentVO.setPk_arrival(invoiceVO.getPk_arrival());
							segmentVO.setArri_city(invoiceVO.getArri_city());
							segmentVO.setArri_province(invoiceVO.getArri_province());
							segmentVO.setArri_area(invoiceVO.getArri_area());
							segmentVO.setArri_detail_addr(invoiceVO.getArri_detail_addr());
							segmentVO.setArri_contact(invoiceVO.getArri_contact());
							segmentVO.setArri_mobile(invoiceVO.getArri_mobile());
							segmentVO.setArri_phone(invoiceVO.getArri_phone());
							segmentVO.setArri_email(invoiceVO.getArri_mobile());
						}
						segmentVO.setStatus(VOStatus.UPDATED);
						NWDao.getInstance().saveOrUpdate(segmentVO);
						//看看有没有委托单，如果有，一起修改了
						EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "segment_vbillno=?", segmentVO.getVbillno());
						if(entrustVO != null){
							entrustVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
							entrustVO.setModify_time(new UFDateTime(new Date()));
							
							entrustVO.setReq_deli_date(segmentVO.getReq_deli_date());
							entrustVO.setReq_deli_time(segmentVO.getReq_deli_time());
							entrustVO.setPk_delivery(segmentVO.getPk_delivery());
							entrustVO.setDeli_city(segmentVO.getDeli_city());
							entrustVO.setDeli_province(segmentVO.getDeli_province());
							entrustVO.setDeli_area(segmentVO.getDeli_area());
							entrustVO.setDeli_detail_addr(segmentVO.getDeli_detail_addr());
							entrustVO.setDeli_contact(segmentVO.getDeli_contact());
							entrustVO.setDeli_mobile(segmentVO.getDeli_mobile());
							entrustVO.setDeli_phone(segmentVO.getDeli_phone());
							entrustVO.setDeli_email(segmentVO.getDeli_mobile());
							entrustVO.setReq_arri_date(segmentVO.getReq_arri_date());
							entrustVO.setReq_arri_time(segmentVO.getReq_deli_time());
							entrustVO.setPk_arrival(segmentVO.getPk_arrival());
							entrustVO.setArri_city(segmentVO.getArri_city());
							entrustVO.setArri_province(segmentVO.getArri_province());
							entrustVO.setArri_area(segmentVO.getArri_area());
							entrustVO.setArri_detail_addr(segmentVO.getArri_detail_addr());
							entrustVO.setArri_contact(segmentVO.getArri_contact());
							entrustVO.setArri_mobile(segmentVO.getArri_mobile());
							entrustVO.setArri_phone(segmentVO.getArri_phone());
							entrustVO.setArri_email(segmentVO.getArri_mobile());
							entrustVO.setStatus(VOStatus.UPDATED);
							NWDao.getInstance().saveOrUpdate(entrustVO);
							
							//修改线路信息
							EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust=?", entrustVO.getPk_entrust());
							if(entLineBVOs != null && entLineBVOs.length > 0){
								for(EntLineBVO entLineBVO : entLineBVOs){
									if(entLineBVO.getAddr_flag().equals("S")){
										entLineBVO.setPk_address(entrustVO.getPk_delivery());
										entLineBVO.setPk_city(entrustVO.getDeli_city());
										entLineBVO.setPk_province(entrustVO.getDeli_province());
										entLineBVO.setPk_area(entrustVO.getDeli_area());
										entLineBVO.setDetail_addr(entrustVO.getDeli_detail_addr());
										entLineBVO.setContact(entrustVO.getDeli_contact());
										entLineBVO.setMobile(entrustVO.getDeli_mobile());
										entLineBVO.setPhone(entrustVO.getDeli_phone());
										entLineBVO.setEmail(entrustVO.getDeli_email());
										entLineBVO.setReq_arri_date(entrustVO.getReq_deli_date());
									}else if(entLineBVO.getAddr_flag().equals("E")){
										entLineBVO.setPk_address(entrustVO.getPk_arrival());
										entLineBVO.setPk_city(entrustVO.getArri_city());
										entLineBVO.setPk_province(entrustVO.getArri_province());
										entLineBVO.setPk_area(entrustVO.getArri_area());
										entLineBVO.setDetail_addr(entrustVO.getArri_detail_addr());
										entLineBVO.setContact(entrustVO.getArri_contact());
										entLineBVO.setMobile(entrustVO.getArri_mobile());
										entLineBVO.setPhone(entrustVO.getArri_phone());
										entLineBVO.setEmail(entrustVO.getArri_email());
										entLineBVO.setReq_arri_date(entrustVO.getReq_arri_date());
									}
									entLineBVO.setStatus(VOStatus.UPDATED);
									NWDao.getInstance().saveOrUpdate(entLineBVO);
								}
							}
						}
						//找到那个包装明细
						InvPackBVO tempInvPackBVO = null;
						for (InvPackBVO invPackBVO : invPackBVOList){
							if(invPackBVO.getStatus() != VOStatus.DELETED){
								if(invLineBVO.getPk_inv_line_b().equals(invPackBVO.getPk_inv_line_b())){
									tempInvPackBVO = invPackBVO;
									break;
								}
							}
						}
						//看看这个line有没有对应的运段包装
						String sql = "SELECT ts_seg_pack_b.* FROM ts_seg_pack_b WITH(NOLOCK) "
								+ " LEFT JOIN ts_inv_pack_b WITH(NOLOCK) ON ts_inv_pack_b.pk_inv_pack_b = ts_seg_pack_b.pk_inv_pack_b "
								+ " LEFT JOIN ts_inv_line_b WITH(NOLOCK) ON ts_inv_pack_b.pk_inv_line_b = ts_inv_line_b.pk_inv_line_b "
								+ " LEFT JOIN ts_segment WITH(NOLOCK) ON ts_segment.pk_segment = ts_seg_pack_b.pk_segment "
								+ " WHERE isnull(ts_seg_pack_b.dr,0)=0  "
								+ " AND isnull(ts_seg_pack_b.dr,0)=0  "
								+ " AND isnull(ts_seg_pack_b.dr,0)=0  "
								+ " AND ts_inv_line_b.pk_inv_line_b = ?"
								+ " AND ts_segment.pk_segment = ?";
						SegPackBVO segPackBVO_db = NWDao.getInstance().queryForObject(sql, SegPackBVO.class, invLineBVO.getPk_inv_line_b(),segmentVO.getPk_segment());
						if(tempInvPackBVO == null){
							//这次没有包装了
							if(segPackBVO_db == null){
								//原来也没有包装，皆大欢喜，不用管
							}else{
								//删除原来的包装
								segPackBVO_db.setStatus(VOStatus.DELETED);
								NWDao.getInstance().saveOrUpdate(segPackBVO_db);
							}
						}else{
							//产生包装了
							if(segPackBVO_db == null){
								//原来也没有包装，新建一个
								SegPackBAndEntCreation(tempInvPackBVO,segmentVO);
							}else{
								//修改原来的包装
								segPackBVO_db.setPk_goods(tempInvPackBVO.getPk_goods());
								segPackBVO_db.setGoods_code(tempInvPackBVO.getGoods_code());
								segPackBVO_db.setGoods_name(tempInvPackBVO.getGoods_name());
								segPackBVO_db.setPlan_pack_num_count(tempInvPackBVO.getPlan_pack_num_count());// 计划数量
								segPackBVO_db.setPack_num_count(tempInvPackBVO.getPack_num_count());// 数量
								segPackBVO_db.setPlan_num(tempInvPackBVO.getPlan_num());// 计划件数
								segPackBVO_db.setNum(tempInvPackBVO.getNum());// 件数
								segPackBVO_db.setPack(tempInvPackBVO.getPack());
								segPackBVO_db.setWeight(tempInvPackBVO.getWeight());
								segPackBVO_db.setVolume(tempInvPackBVO.getVolume());
								segPackBVO_db.setUnit_weight(tempInvPackBVO.getUnit_weight());
								segPackBVO_db.setUnit_volume(tempInvPackBVO.getUnit_volume());
								segPackBVO_db.setLength(tempInvPackBVO.getLength());
								segPackBVO_db.setWidth(tempInvPackBVO.getWidth());
								segPackBVO_db.setHeight(tempInvPackBVO.getHeight());
								segPackBVO_db.setTrans_note(tempInvPackBVO.getTrans_note());
								segPackBVO_db.setLow_temp(tempInvPackBVO.getLow_temp());
								segPackBVO_db.setHight_temp(tempInvPackBVO.getHight_temp());
								segPackBVO_db.setReference_no(tempInvPackBVO.getReference_no());
								segPackBVO_db.setMemo(tempInvPackBVO.getMemo());
								segPackBVO_db.setStatus(VOStatus.UPDATED);
								NWDao.getInstance().saveOrUpdate(segPackBVO_db);
								//找到委托单包装的线路包装 修改
								EntPackBVO entPackBVO = NWDao.getInstance().queryByCondition(EntPackBVO.class, "pk_seg_pack_b=?", segPackBVO_db.getPk_seg_pack_b());
								if(entPackBVO != null){
									entPackBVO.setPk_goods(segPackBVO_db.getPk_goods());
									entPackBVO.setGoods_code(segPackBVO_db.getGoods_code());
									entPackBVO.setGoods_name(segPackBVO_db.getGoods_name());
									entPackBVO.setPlan_pack_num_count(segPackBVO_db.getPlan_pack_num_count());// 计划数量
									entPackBVO.setPack_num_count(segPackBVO_db.getPack_num_count());// 数量
									entPackBVO.setPlan_num(segPackBVO_db.getPlan_num());// 计划件数
									entPackBVO.setNum(segPackBVO_db.getNum());// 件数
									entPackBVO.setPack(segPackBVO_db.getPack());
									entPackBVO.setWeight(segPackBVO_db.getWeight());
									entPackBVO.setVolume(segPackBVO_db.getVolume());
									entPackBVO.setUnit_weight(segPackBVO_db.getUnit_weight());
									entPackBVO.setUnit_volume(segPackBVO_db.getUnit_volume());
									entPackBVO.setLength(segPackBVO_db.getLength());
									entPackBVO.setWidth(segPackBVO_db.getWidth());
									entPackBVO.setHeight(segPackBVO_db.getHeight());
									entPackBVO.setTrans_note(segPackBVO_db.getTrans_note());
									entPackBVO.setLow_temp(segPackBVO_db.getLow_temp());
									entPackBVO.setHight_temp(segPackBVO_db.getHight_temp());
									entPackBVO.setReference_no(segPackBVO_db.getReference_no());
									entPackBVO.setMemo(segPackBVO_db.getMemo());
									entPackBVO.setStatus(VOStatus.UPDATED);
									NWDao.getInstance().saveOrUpdate(entPackBVO);
									
									EntLinePackBVO[] entLinePackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_ent_pack_b=?", entPackBVO.getPk_ent_pack_b());
									if(entLinePackBVOs != null && entLinePackBVOs.length > 0){
										for(EntLinePackBVO entLinePackBVO : entLinePackBVOs){
											entLinePackBVO.setPk_goods(entPackBVO.getPk_goods());
											entLinePackBVO.setGoods_code(entPackBVO.getGoods_code());
											entLinePackBVO.setGoods_name(entPackBVO.getGoods_name());
											entLinePackBVO.setPlan_pack_num_count(entPackBVO.getPlan_pack_num_count());// 计划数量
											entLinePackBVO.setPack_num_count(entPackBVO.getPack_num_count());// 数量
											entLinePackBVO.setPlan_num(entPackBVO.getPlan_num());// 计划件数
											entLinePackBVO.setNum(entPackBVO.getNum());// 件数
											entLinePackBVO.setPack(entPackBVO.getPack());
											entLinePackBVO.setWeight(entPackBVO.getWeight());
											entLinePackBVO.setVolume(entPackBVO.getVolume());
											entLinePackBVO.setUnit_weight(entPackBVO.getUnit_weight());
											entLinePackBVO.setUnit_volume(entPackBVO.getUnit_volume());
											entLinePackBVO.setLength(entPackBVO.getLength());
											entLinePackBVO.setWidth(entPackBVO.getWidth());
											entLinePackBVO.setHeight(entPackBVO.getHeight());
											entLinePackBVO.setTrans_note(entPackBVO.getTrans_note());
											entLinePackBVO.setLow_temp(entPackBVO.getLow_temp());
											entLinePackBVO.setHight_temp(entPackBVO.getHight_temp());
											entLinePackBVO.setReference_no(entPackBVO.getReference_no());
											entLinePackBVO.setMemo(entPackBVO.getMemo());
											entLinePackBVO.setStatus(VOStatus.UPDATED);
											NWDao.getInstance().saveOrUpdate(entLinePackBVO);
										}
									}
								}
							}
						}
					}
				}else if(invLineBVO.getStatus() == VOStatus.DELETED){
					//删除无关发货单的包装，但是会存在两种情况，第一种，只是删除运段包装，第二种，整个运段的删除
					//删除的特殊处理
					if(canDirectDelete.contains(invLineBVO.getPk_address() + invLineBVO.getReq_date_from())){
						//删除过了，
						continue;
					}
					boolean flag = true;
					for(InvLineBVO lineBVO : invLineBVOs){
						if(lineBVO.getStatus() != VOStatus.DELETED
								&& lineBVO.getPk_address().equals(invLineBVO.getPk_address())
								&& lineBVO.getReq_date_from().equals(invLineBVO.getReq_date_from())){
							//看看是不是完全删除的
							flag = false;
							break;
						}
					}
					if(!flag){
						//删除对应的包装即可
						//看看这个line有没有对应的运段包装
						String sql = "SELECT ts_seg_pack_b.pk_seg_pack_b,ts_segment.vbillstatus,ts_segment.vbillno FROM ts_seg_pack_b WITH(NOLOCK) "
								+ " LEFT JOIN ts_inv_pack_b WITH(NOLOCK) ON ts_inv_pack_b.pk_inv_pack_b = ts_seg_pack_b.pk_inv_pack_b "
								+ " LEFT JOIN ts_inv_line_b WITH(NOLOCK) ON ts_inv_pack_b.pk_inv_line_b = ts_inv_line_b.pk_inv_line_b "
								+ "	LEFT JOIN ts_segment WITH(NOLOCK) ON ts_segment.pk_segment=ts_seg_pack_b.pk_segment"
								+ " WHERE isnull(ts_seg_pack_b.dr,0)=0  "
								+ " AND isnull(ts_seg_pack_b.dr,0)=0  "
								+ " AND isnull(ts_seg_pack_b.dr,0)=0  "
								+ "	AND isnull(ts_segment.dr,0)=0 "
								+ " AND ts_inv_line_b.pk_inv_line_b = ?";
						@SuppressWarnings("unchecked")
						Map<String,Object> segPackBVO_db_info = NWDao.getInstance().queryForObject(sql, HashMap.class, invLineBVO.getPk_inv_line_b());
						if(segPackBVO_db_info != null){
							if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()
									&& Integer.parseInt(segPackBVO_db_info.get("vbillstatus").toString()) >= BillStatus.SEG_DELIVERY){
								throw new BusiException("多提一送业务，在运段[?]已提货情况下，不允许修改节点信息！",segPackBVO_db_info.get("vbillno").toString());
							}
							if(Integer.parseInt(segPackBVO_db_info.get("vbillstatus").toString()) >= BillStatus.SEG_ARRIVAL){
								throw new BusiException("运段[?]已到货，不允许修改货品信息！",segPackBVO_db_info.get("vbillno").toString());
							}
							NWDao.getInstance().deleteByPK(SegPackBVO.class, segPackBVO_db_info.get("pk_seg_pack_b").toString());
							//看看委托单里有没有对应的信息，如果有，也删除掉
							EntPackBVO entPackBVO = NWDao.getInstance().queryByCondition(EntPackBVO.class, "pk_seg_pack_b=?", segPackBVO_db_info.get("pk_seg_pack_b").toString());
							if(entPackBVO != null){
								entPackBVO.setStatus(VOStatus.DELETED);
								NWDao.getInstance().saveOrUpdate(entPackBVO);
								EntLinePackBVO[] entLinePackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_ent_pack_b=?", entPackBVO.getPk_ent_pack_b());
								if(entLinePackBVOs != null && entLinePackBVOs.length > 0){
									for(EntLinePackBVO entLinePackBVO : entLinePackBVOs){
										entLinePackBVO.setStatus(VOStatus.DELETED);
										NWDao.getInstance().saveOrUpdate(entLinePackBVO);
									}
								}
							}
						}
					}else{
						canDirectDelete.add(invLineBVO.getPk_address() + invLineBVO.getReq_date_from());
						
						String sql = "SELECT DISTINCT vbillno,pk_segment,vbillstatus FROM  ( "
								+ " ( "
								+ " SELECT ts_segment.vbillno,ts_segment.pk_segment,ts_segment.vbillstatus "
								+ " FROM ts_inv_line_b WITH(NOLOCK) "
								+ " LEFT JOIN ts_inv_pack_b WITH(NOLOCK) "
								+ " ON ts_inv_pack_b.pk_inv_line_b = ts_inv_line_b.pk_inv_line_b "
								+ " LEFT JOIN ts_seg_pack_b WITH(NOLOCK) "
								+ " ON ts_seg_pack_b.pk_inv_pack_b = ts_inv_pack_b.pk_inv_pack_b "
								+ " LEFT JOIN ts_segment WITH(NOLOCK) "
								+ " ON ts_seg_pack_b.pk_segment = ts_segment.pk_segment "
								+ " WHERE isnull(ts_inv_line_b.dr, 0) = 0 "
								+ " AND isnull(ts_inv_pack_b.dr, 0) = 0 "
								+ " AND isnull(ts_seg_pack_b.dr, 0) = 0 "
								+ " AND isnull(ts_segment.dr, 0) = 0 "
								+ " AND ts_inv_line_b.pk_inv_line_b = '"+ invLineBVO.getPk_inv_line_b() +"' "
								+ " ) "
								+ " UNION  "
								+ " ( "
								+ " SELECT ts_segment.vbillno,ts_segment.pk_segment,ts_segment.vbillstatus "
								+ " FROM ts_inv_line_b WITH(NOLOCK) "
								+ " LEFT JOIN ts_invoice WITH(NOLOCK) "
								+ " ON ts_inv_line_b.pk_invoice = ts_invoice.pk_invoice "
								+ " LEFT JOIN ts_segment WITH(NOLOCK) "
								+ " ON ts_invoice.vbillno = ts_segment.invoice_vbillno AND ts_segment.pk_delivery=ts_inv_line_b.pk_address "
								+ " WHERE isnull(ts_inv_line_b.dr, 0) = 0 "
								+ " AND isnull(ts_invoice.dr, 0) = 0 "
								+ " AND isnull(ts_segment.dr, 0) = 0 "
								+ " AND ts_inv_line_b.pk_inv_line_b = '"+ invLineBVO.getPk_inv_line_b() +"' "
								+ " ) "
								+ " ) table_a WHERE  vbillno IS NOT NULL ";
						
						//删除运段
						SegmentVO segmentVO_db = NWDao.getInstance().queryForObject(sql, SegmentVO.class);
						if(segmentVO_db != null){
							if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()
									&& segmentVO_db.getVbillstatus() >= BillStatus.SEG_DELIVERY){
								throw new BusiException("多提一送业务，在运段[?]已提货情况下，不允许修改节点信息！",segmentVO_db.getVbillno());
							}
							if(segmentVO_db.getVbillstatus() >= BillStatus.SEG_ARRIVAL){
								throw new BusiException("运段[?]已到货，不允许删除！",segmentVO_db.getVbillno());
							}
							segmentVO_db.setStatus(VOStatus.DELETED);
							NWDao.getInstance().saveOrUpdate(segmentVO_db);
							EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "segment_vbillno=?", segmentVO_db.getVbillno());
							if(entrustVO != null){
								entrustVO.setStatus(VOStatus.DELETED);
								NWDao.getInstance().saveOrUpdate(entrustVO);
								//删除应付
								PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "entrust_vbillno=?", entrustVO.getVbillno());
								if(payDetailVOs != null && payDetailVOs.length > 0){
									NWDao.getInstance().delete(payDetailVOs);
								}
							}
						}
					}
				}
			}
		}
		aggInvoiceVO.setTableVO(TabcodeConst.TS_INV_PACK_B, invPackBVOList.toArray(new InvPackBVO[invPackBVOList.size()]));
	}
	
	
	

	/**
	 * 同步更新运段信息
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	public static void syncSegmentUpdater(AggregatedValueObject billVO, ParamVO paramVO) {
		if(billVO == null) {
			return;
		}
		ExAggInvoiceVO exAggVO = (ExAggInvoiceVO) billVO;
		InvoiceVO parentVO = (InvoiceVO) exAggVO.getParentVO();
		logger.info("START----------------" + parentVO.getVbillno() + "------------------");
		logger.info("[XXX发货单:" + parentVO.getVbillno() + "]开始同步更新运段...");
		// 只有不是新增状态的单据才需要同步更新运段
		// 可能做了分段，所以可能有多条运段记录
		SegmentVO[] segVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class, "invoice_vbillno=?",
				parentVO.getVbillno());
		if(segVOs == null || segVOs.length == 0) {
			// 没有对应的运段
			logger.info("[XXX发货单:" + parentVO.getVbillno() + "]发货单没有对应的运段，不需要同步修改...");
			return;
		}
		logger.info("[XXX发货单:" + parentVO.getVbillno() + "]共查询到" + segVOs.length + "条需要同步更新的运段...");

		logger.info("[XXX发货单:" + parentVO.getVbillno() + "]收集发货单中可以同步到运段的包装信息...");
		// 将要更新的货品记录转换成运段中的货品记录
		List<SegPackBVO> spBVOs = new ArrayList<SegPackBVO>();
		CircularlyAccessibleValueObject[] cvos = exAggVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		if(cvos != null && cvos.length > 0) {
			int index = 1;
			for(CircularlyAccessibleValueObject cvo : cvos) {
				InvPackBVO ipBVO = (InvPackBVO) cvo;
				if(ipBVO.getStatus() != VOStatus.DELETED) {
					logger.info("[XXX发货单:" + parentVO.getVbillno() + "]开始收集第" + index + "个包装信息...");
					SegPackBVO spBVO = InvoiceUtils.convert(ipBVO);
					spBVOs.add(spBVO);
					logger.info("[XXX发货单:" + parentVO.getVbillno() + "]完成收集第" + index + "个包装信息...");
					index++;
				}
			}
		}
		logger.info("[XXX发货单:" + parentVO.getVbillno() + "]总共收集了" + spBVOs.size() + "个可以同步到运段的包装信息...");

		int index = 1;
		for(SegmentVO segVO : segVOs) {
			if(segVO.getSeg_type().intValue() == SegmentConst.QUANTITY) {
				// 该运段可能已经做了分量或其他操作，不是分段操作
				throw new BusiException("发货单[?]已做了分量操作，不能同步更新！",parentVO.getVbillno());
			}
			logger.info("[XXX发货单:" + parentVO.getVbillno() + "]开始同步第" + index + "个运段,运段号：" + segVO.getVbillno() + "...");
			// 更新的字段
			segVO.setReq_deli_date(parentVO.getReq_deli_date());
			segVO.setReq_deli_time(parentVO.getReq_deli_time());
			segVO.setReq_arri_date(parentVO.getReq_arri_date());
			segVO.setReq_arri_time(parentVO.getReq_arri_time());
			segVO.setPack_num_count(parentVO.getPack_num_count());
			segVO.setNum_count(parentVO.getNum_count());
			segVO.setWeight_count(parentVO.getWeight_count());
			segVO.setVolume_count(parentVO.getVolume_count());
			segVO.setFee_weight_count(parentVO.getFee_weight_count());
			segVO.setVolume_weight_count(parentVO.getVolume_weight_count());
			segVO.setMemo(parentVO.getMemo());
			if(segVO.getPk_delivery().equals(parentVO.getPk_delivery())){
				segVO.setDeli_area(parentVO.getDeli_area());
				segVO.setDeli_city(parentVO.getDeli_city());
				segVO.setDeli_province(parentVO.getDeli_province());
				segVO.setDeli_contact(parentVO.getDeli_contact());
				segVO.setDeli_detail_addr(parentVO.getDeli_detail_addr());
				segVO.setDeli_email(parentVO.getDeli_email());
				segVO.setDeli_mobile(parentVO.getDeli_mobile());
				segVO.setDeli_phone(parentVO.getDeli_phone());
			}
			if(segVO.getPk_arrival().equals(parentVO.getPk_arrival())){
				segVO.setArri_area(parentVO.getArri_area());
				segVO.setArri_city(parentVO.getArri_city());
				segVO.setArri_province(parentVO.getArri_province());
				segVO.setArri_contact(parentVO.getArri_contact());
				segVO.setArri_detail_addr(parentVO.getArri_detail_addr());
				segVO.setArri_email(parentVO.getArri_email());
				segVO.setArri_mobile(parentVO.getArri_mobile());
				segVO.setArri_phone(parentVO.getArri_phone());
			}
			segVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(segVO);
			logger.info("[XXX发货单:" + parentVO.getVbillno() + "]完成同步第" + index + "个运段,运段号：" + segVO.getVbillno() + "...");

			logger.info("[XXX发货单:" + parentVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",删除运段现有的包装信息...");
			// 删除现有的数据
			SegPackBVO[] oldSpBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegPackBVO.class,
					"pk_segment=?", segVO.getPk_segment());
			if(oldSpBVOs != null && oldSpBVOs.length > 0) {
				logger.info("[XXX发货单:" + parentVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",总共需要删除"
						+ oldSpBVOs.length + "条包装信息...");
				for(SegPackBVO spBVO : oldSpBVOs) {
					spBVO.setStatus(VOStatus.DELETED);
					NWDao.getInstance().saveOrUpdate(spBVO);
				}
			}

			logger.info("[XXX发货单:" + parentVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",保存之前收集的包装信息，共"
					+ spBVOs.size() + "条...");
			// 保存新的包装信息
			for(SegPackBVO spBVO : spBVOs) {
				spBVO.setStatus(VOStatus.NEW);
				spBVO.setPk_seg_pack_b(null);
				NWDao.setUuidPrimaryKey(spBVO);
				spBVO.setPk_segment(segVO.getPk_segment());
				NWDao.getInstance().saveOrUpdate(spBVO);
			}
			logger.info("[XXX发货单:" + parentVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",完成保存之前收集的包装信息，共"
					+ spBVOs.size() + "条...");

			logger.info("[XXX发货单:" + parentVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",开始同步更新委托单...");
			// 更新委托单
			syncEntrustUpdater(parentVO, segVO, spBVOs);
			logger.info("[XXX发货单:" + parentVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",完成同步更新委托单...");

			index++;
		}
		logger.info("[XXX发货单:" + parentVO.getVbillno() + "]完成同步更新运段...");
		logger.info("END----------------" + parentVO.getVbillno() + "------------------");
	}

	/**
	 * 同步更新委托单
	 * 
	 * @param billVO
	 * @param paramVO
	 * 
	 */
	
	public static void syncEntrustUpdater(InvoiceVO invVO, SegmentVO segVO, List<SegPackBVO> spBVOs){
		syncEntrustUpdater( invVO,  segVO, spBVOs ,null);
	}
	
	public static void syncEntrustUpdater(InvoiceVO invVO, SegmentVO segVO, List<SegPackBVO> spBVOs ,String lot) {
		if(segVO == null) {
			return;
		}
		String sql = "select * from ts_entrust where isnull(dr,0)=0 "
				+ "and pk_entrust in (select pk_entrust from ts_ent_seg_b where pk_segment=? and isnull(dr,0)=0)";
		List<EntrustVO> entVOs = NWDao.getInstance().queryForList(sql, EntrustVO.class, segVO.getPk_segment());
		if(entVOs == null || entVOs.size() == 0) {
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",运段没有对应的委托单，不需要同步...");
			return;
		}

		logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",总共需要同步" + entVOs.size()
				+ "条委托单...");
		int index = 1;
		for(EntrustVO entVO : entVOs) {
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",开始同步第" + index
					+ "条委托单，委托单号：" + entVO.getVbillno() + "...");
			// 查询委托单的货品信息
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",查询委托单的包装信息...");
			EntPackBVO[] epBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class, "pk_entrust=?",
					entVO.getPk_entrust());
			List<EntPackBVO> newEpBVOs = new ArrayList<EntPackBVO>();
			if(epBVOs != null && epBVOs.length > 0) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",查询到" + epBVOs.length + "条委托单的包装信息...");
				// 更新委托单的主表信息
				for(EntPackBVO epBVO : epBVOs) {
					// 这里要过滤当前运段的货品,将这个货品删除
					logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
							+ entVO.getVbillno() + ",删除该运段对应的包装信息...");
					if(epBVO.getPk_segment().equals(segVO.getPk_segment())) {
						epBVO.setStatus(VOStatus.DELETED);
						NWDao.getInstance().saveOrUpdate(epBVO);
					} else {
						newEpBVOs.add(epBVO);
					}
				}
			}
			// 添加新的货品到委托单
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",将运段的包装信息同步到委托单...");
			if(spBVOs != null && spBVOs.size() > 0) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",共需要同步" + spBVOs.size() + "条包装信息...");
				for(SegPackBVO spBVO : spBVOs) {
					EntPackBVO epBVO = EntrustUtils.convert(spBVO);
					epBVO.setPk_entrust(entVO.getPk_entrust());
					epBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(epBVO);
					NWDao.getInstance().saveOrUpdate(epBVO);
					newEpBVOs.add(epBVO);
				}
			}
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",发货单表头的订单号，客户订单号同步到委托单...");
			EntrustUtils.setHeader(invVO, entVO);
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
			+ ",发货单的地址信息同步到委托单...");
			EntrustUtils.setAddress(segVO, entVO);
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",将委托单的包装信息合计到表头...");
			EntrustUtils.setHeaderCount(entVO, newEpBVOs);
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",查询委托单对应的应付明细...");
			PayDetailVO pdVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "entrust_vbillno=? and pay_type=?",
					entVO.getVbillno(), PayDetailConst.ORIGIN_TYPE);
			if(pdVO == null) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",没有找到对应的应付明细...");
				return;
			}
			// 匹配合同，重新计算费用明细
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",查找委托单应付明细的费用明细...");
			PayDetailBVO[] pdBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
							"pk_pay_detail=(select pk_pay_detail from ts_pay_detail where isnull(dr,0)=0 and entrust_vbillno=?)",
							entVO.getVbillno());
			List<PayDetailBVO> oldDetailBVOs = new ArrayList<PayDetailBVO>();
			if(pdBVOs != null) {
				oldDetailBVOs = Arrays.asList(pdBVOs);
			}
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",查询委托单的路线信息...");
			EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class,
					"pk_entrust=?", entVO.getPk_entrust());
			if(entLineBVOs == null || entLineBVOs.length == 0) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",没有查询到路线信息...");
			} else {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",共查询到" + entLineBVOs.length + "条路线信息...");
				for(EntLineBVO entLineBVO : entLineBVOs){
					if(entLineBVO.getPk_address().equals(segVO.getPk_delivery())){
						entLineBVO.setPk_area(segVO.getDeli_area());
						entLineBVO.setPk_city(segVO.getDeli_city());
						entLineBVO.setPk_province(segVO.getDeli_province());
						entLineBVO.setContact(segVO.getDeli_contact());
						entLineBVO.setDetail_addr(segVO.getDeli_detail_addr());
						entLineBVO.setEmail(segVO.getDeli_email());
						entLineBVO.setMobile(segVO.getDeli_mobile());
						entLineBVO.setPhone(segVO.getDeli_phone());
					}
					if(entLineBVO.getPk_address().equals(segVO.getPk_arrival())){
						entLineBVO.setPk_area(segVO.getArri_area());
						entLineBVO.setPk_city(segVO.getArri_city());
						entLineBVO.setPk_province(segVO.getArri_province());
						entLineBVO.setContact(segVO.getArri_contact());
						entLineBVO.setDetail_addr(segVO.getArri_detail_addr());
						entLineBVO.setEmail(segVO.getArri_email());
						entLineBVO.setMobile(segVO.getArri_mobile());
						entLineBVO.setPhone(segVO.getArri_phone());
					}
				}
			}
			// 根据路线信息匹配合同，返回费用明细
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",根据路线信息匹配合同，返回费用明细...");

			EntTransbilityBVO[] tbBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
					"pk_entrust=?", entVO.getPk_entrust());

			oldDetailBVOs = EntrustUtils.getPayDetailBVOs(null, entVO, tbBVOs, entLineBVOs, oldDetailBVOs);
			if(oldDetailBVOs == null || oldDetailBVOs.size() == 0) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",没有返回任何费用明细...");
			} else {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",返回" + oldDetailBVOs.size() + "条费用明细...");
			}
			for(PayDetailBVO cvo : oldDetailBVOs) {
				if(cvo.getStatus() == VOStatus.NEW) {
					cvo.setPk_pay_detail(pdVO.getPk_pay_detail()); // 设置主表的主键
					NWDao.setUuidPrimaryKey(cvo);
				}
			}
			pdBVOs = oldDetailBVOs.toArray(new PayDetailBVO[oldDetailBVOs.size()]);

			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",重新计算费用明细的金额信息和委托单的金额...");
			if(pdBVOs != null && pdBVOs.length > 0) {
				EntrustUtils.setBodyDetailAmount(entVO, pdBVOs);
				EntrustUtils.setHeaderCostAmount(entVO, pdBVOs);// 合计委托单总金额
			}
			if(StringUtils.isNotBlank(lot)){
				//更新批次信息
				EntLotVO entLotVO = NWDao.getInstance().queryByCondition(EntLotVO.class, "pk_entrust =?", entVO.getLot());
				if(entLotVO != null){
					entLotVO.setLot(lot);
					entLotVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(entLotVO);
				}
				//重新生成委托单相关信息
				entVO.setLot(lot);
				entVO.setReq_arri_date(segVO.getReq_arri_date());
				entVO.setReq_arri_time(segVO.getReq_arri_time());
				entVO.setReq_deli_date(segVO.getReq_deli_date());
				entVO.setReq_deli_time(segVO.getReq_deli_time());
				entVO.setPk_arrival(segVO.getPk_arrival());
				entVO.setArri_city(segVO.getArri_city());
				entVO.setArri_area(segVO.getArri_area());
				entVO.setArri_province(segVO.getArri_province());
				entVO.setArri_detail_addr(segVO.getArri_detail_addr());
				entVO.setArri_contact(segVO.getArri_contact());
				entVO.setArri_email(segVO.getArri_email());
				entVO.setArri_mobile(segVO.getArri_mobile());
				entVO.setArri_phone(segVO.getArri_phone());
				entVO.setPk_delivery(segVO.getPk_delivery());
				entVO.setDeli_city(segVO.getDeli_city());
				entVO.setDeli_area(segVO.getDeli_area());
				entVO.setDeli_province(segVO.getDeli_province());
				entVO.setDeli_detail_addr(segVO.getDeli_detail_addr());
				entVO.setDeli_contact(segVO.getDeli_contact());
				entVO.setDeli_email(segVO.getDeli_email());
				entVO.setDeli_mobile(segVO.getDeli_mobile());
				entVO.setDeli_phone(segVO.getDeli_phone());
				
				
			}
			
			entVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(entVO);

			// 更新应付明细主表
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",同步更新委托单的应付明细...");
			syncPayDetailUpdater(entVO, oldDetailBVOs);

			if(pdBVOs == null || pdBVOs.length == 0) {
				return;
			}
			// 更新到数据库
			// 这里的pdBVOs每个元素已经设置了状态了，不需要重新设置
			NWDao.getInstance().saveOrUpdate(pdBVOs);

			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",查询应付明细对应的分担费用...");
			// 先删除旧的分摊记录
			PayDeviBVO[] oldDeviVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class,
					"pk_pay_detail=?", pdVO.getPk_pay_detail());
			if(oldDeviVOs != null) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",查询到" + oldDeviVOs.length + "条旧的分摊费用...");
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",将这些分摊费用删除...");
				for(PayDeviBVO oldDeviVO : oldDeviVOs) {
					oldDeviVO.setStatus(VOStatus.DELETED);
					NWDao.getInstance().saveOrUpdate(oldDeviVO);
				}
			} else {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",没有旧的分摊费用...");
			}
			logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号：" + entVO.getVbillno()
					+ ",应付明细：" + pdVO.getVbillno() + ",重新分摊...");
			List<PayDeviBVO> deviVOs = PZUtils.getPayDeviBVOs(entVO, null, null, pdBVOs);
			// 添加新的分摊记录
			if(deviVOs != null) {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",共生成" + deviVOs.size() + "条分摊费用...");
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",保存分摊费用...");
				for(PayDeviBVO deviVO : deviVOs) {
					deviVO.setStatus(VOStatus.NEW);
					NWDao.getInstance().saveOrUpdate(deviVO);
				}
			} else {
				logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",委托单号："
						+ entVO.getVbillno() + ",应付明细：" + pdVO.getVbillno() + ",没有分摊费用...");
			}
			index++;
		}
		logger.info("[XXX发货单:" + invVO.getVbillno() + "],运段号：" + segVO.getVbillno() + ",完成同步" + entVOs.size()
				+ "条委托单...");
	}

	/**
	 * 同步更新应付明细 包括订单号和客户订单号
	 * 
	 * @param entVO
	 * @param detailBVOs
	 *            这个参数主要是为了提供税率、税种等参数
	 */
	public static void syncPayDetailUpdater(EntrustVO entVO, List<PayDetailBVO> detailBVOs) {
		if(entVO == null) {
			return;
		}
		PayDetailVO pdVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "entrust_vbillno=? and pay_type=?",
				entVO.getVbillno(), PayDetailConst.ORIGIN_TYPE);
		if(pdVO == null) {
			return;
		}
		if(pdVO.getVbillstatus().intValue() != BillStatus.NEW) {
			throw new BusiException("应付明细必须[新建]状态才能进行更新,单据号[" + pdVO.getVbillno() + "],委托单号[" + entVO.getVbillno()+"]");
		}
		pdVO.setLot(entVO.getLot());
		pdVO.setOrderno(entVO.getOrderno());
		pdVO.setCust_orderno(entVO.getCust_orderno());
		pdVO.setPack_num_count(entVO.getPack_num_count());
		pdVO.setNum_count(entVO.getNum_count());
		pdVO.setVolume_count(entVO.getVolume_count());
		pdVO.setWeight_count(entVO.getWeight_count());
		pdVO.setFee_weight_count(entVO.getFee_weight_count());
		pdVO.setCost_amount(entVO.getCost_amount());
		pdVO.setUngot_amount(entVO.getCost_amount());// 未付金额默认等于总金额

		// FIXME 取第一行合同明细的税种，税率
		if(detailBVOs != null && detailBVOs.size() > 0) {
			pdVO.setTax_cat(detailBVOs.get(0).getTax_cat());
			pdVO.setTax_rate(detailBVOs.get(0).getTax_rate());
			// FIXME 取第一行合同明细的税种，税率
			pdVO.setTaxmny(CMUtils.getTaxmny(pdVO.getCost_amount(), pdVO.getTax_cat(), pdVO.getTax_rate()));
		}

		pdVO.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(pdVO);
	}
}
