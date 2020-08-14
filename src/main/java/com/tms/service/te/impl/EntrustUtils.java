package com.tms.service.te.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.SpringContextHolder;
import com.tms.constants.ContractConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.ExpenseTypeService;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.service.tp.impl.PZUtils;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 委托单相关数据计算类
 * 
 * @author xuqc
 * @date 2014-4-29 下午09:28:09
 */
public class EntrustUtils {

	static Logger logger = Logger.getLogger(EntrustUtils.class);
	
	
	public static void setAddress(SegmentVO parentVO, EntrustVO entVO){
		entVO.setPk_delivery(parentVO.getPk_delivery());
		entVO.setDeli_area(parentVO.getDeli_area());
		entVO.setDeli_city(parentVO.getDeli_city());
		entVO.setDeli_province(parentVO.getDeli_province());
		entVO.setDeli_detail_addr(parentVO.getDeli_detail_addr());
		entVO.setDeli_contact(parentVO.getDeli_contact());
		entVO.setDeli_email(parentVO.getDeli_email());
		entVO.setDeli_mobile(parentVO.getDeli_mobile());
		entVO.setDeli_phone(parentVO.getDeli_phone());
		
		entVO.setPk_arrival(parentVO.getPk_arrival());
		entVO.setArri_area(parentVO.getArri_area());
		entVO.setArri_city(parentVO.getArri_city());
		entVO.setArri_province(parentVO.getArri_province());
		entVO.setArri_detail_addr(parentVO.getArri_detail_addr());
		entVO.setArri_contact(parentVO.getArri_contact());
		entVO.setArri_email(parentVO.getArri_email());
		entVO.setArri_mobile(parentVO.getArri_mobile());
		entVO.setArri_phone(parentVO.getArri_phone());
	}
	
	
	
	/**
	 * 更新表头的订单号，客户订单号 项目号 紧急程度 是否回程（一般只有强制修订时会调用）
	 * 
	 * @param invVO
	 * @param entVO
	 */
	public static void setHeader(InvoiceVO parentVO, EntrustVO entVO) {
		if(parentVO == null || entVO == null) {
			return;
		}
		String sql = "select * from ts_invoice where isnull(dr,0)=0 and "
				+ "pk_invoice in (select pk_invoice from ts_ent_inv_b where pk_entrust=? and isnull(dr,0)=0)";
		// 委托单关联的所有发货单
		List<InvoiceVO> invVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, entVO.getPk_entrust());
		StringBuffer cust_ordernoBuf = new StringBuffer(parentVO.getCust_orderno() == null ? ""
				: parentVO.getCust_orderno());
		StringBuffer ordernoBuf = new StringBuffer(parentVO.getOrderno() == null ? "" : parentVO.getOrderno());
		StringBuffer item_codeBuf = new StringBuffer(parentVO.getItem_code() == null ? "": parentVO.getItem_code());
		UFBoolean if_return =parentVO.getIf_return();
		Integer urgent_level = parentVO.getUrgent_level();
		
		for(InvoiceVO invVO : invVOs) {
			if(!invVO.getPk_invoice().equals(parentVO.getPk_invoice())) {
				if(cust_ordernoBuf.length() > 0) {
					cust_ordernoBuf.append(",");
				}
				if(ordernoBuf.length() > 0) {
					ordernoBuf.append(",");
				}
				if(item_codeBuf.length() > 0) {
					item_codeBuf.append(",");
				}
				cust_ordernoBuf.append(invVO.getCust_orderno());
				ordernoBuf.append(invVO.getCust_orderno());
				item_codeBuf.append(invVO.getItem_code());
			}
		}
		entVO.setUrgent_level(urgent_level);
		entVO.setIf_return(if_return);
		entVO.setCust_orderno(cust_ordernoBuf.toString());
		entVO.setItem_code(item_codeBuf.toString());
		entVO.setOrderno(ordernoBuf.toString());
	}

	/**
	 * 设置表头的相关数据，包括总件数、总重量、总体积、总计费重、总体积重
	 * 
	 * @param parentVO
	 * @param epBVOs
	 */
	public static void setHeaderCount(EntrustVO parentVO, List<EntPackBVO> epBVOs) {
		if(parentVO == null || epBVOs == null) {
			return;
		}

		UFDouble pack_num_count = new UFDouble(0);
		Integer num_count = new Integer(0);
		UFDouble weight_count = new UFDouble(0);
		UFDouble volume_count = new UFDouble(0);
		for(EntPackBVO epBVO : epBVOs) {
			if(epBVO.getStatus() != VOStatus.DELETED) {
				pack_num_count = pack_num_count.add(epBVO.getPack_num_count() == null ? new UFDouble(0) : epBVO
						.getPack_num_count());
				num_count += epBVO.getNum() == null ? 0 : epBVO.getNum();
				weight_count = weight_count.add(epBVO.getWeight() == null ? new UFDouble(0) : epBVO.getWeight());
				volume_count = volume_count.add(epBVO.getVolume() == null ? new UFDouble(0) : epBVO.getVolume());
			}
		}
		parentVO.setPack_num_count(pack_num_count);
		parentVO.setNum_count(num_count);
		parentVO.setWeight_count(weight_count);
		parentVO.setVolume_count(volume_count);

		String pk_trans_type = parentVO.getPk_trans_type();
		if(StringUtils.isNotBlank(pk_trans_type)) {
			TransTypeVO typeVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "pk_trans_type=?",
					pk_trans_type);
			if(typeVO != null) {
				if(typeVO.getRate() != null) {
					UFDouble volume_weight_count = volume_count.multiply(typeVO.getRate());
					// 体积重
					parentVO.setVolume_weight_count(volume_weight_count);
					UFDouble fee = volume_weight_count; // 总体积/体积重换算比率
					if(fee.doubleValue() < weight_count.doubleValue()) {
						fee = weight_count;
					}
					parentVO.setFee_weight_count(fee);
				}
			}
		}
	}

	// 更新费用明细的金额信息
	public static void setBodyDetailAmount(EntrustVO parentVO, PayDetailBVO[] pdBVOs) {
		if(parentVO == null || pdBVOs == null || pdBVOs.length == 0) {
			return;
		}
		for(PayDetailBVO pdBVO : pdBVOs) {
			Integer quote_type = pdBVO.getQuote_type();
			Integer valuation_type = pdBVO.getValuation_type();
			Integer price_type = pdBVO.getPrice_type();
			UFDouble price = pdBVO.getPrice();
			if(valuation_type == null || price == null) {
				return;
			}
			UFDouble amount = new UFDouble(0);
			if(quote_type.intValue() == 0) {// 区间报价
				if(price_type == 0) {// 价格类型=单价
					switch(valuation_type){
					case 0: // 重量
						UFDouble fee_weight_count = parentVO.getFee_weight_count() == null ? new UFDouble(0) : parentVO
								.getFee_weight_count();
						amount = fee_weight_count.multiply(price);
						break;
					case 1: // 体积
						UFDouble volume_count = parentVO.getVolume_count() == null ? new UFDouble(0) : parentVO
								.getVolume_count();
						amount = volume_count.multiply(price);
						break;
					case 2: // 件数
						Integer num_count = parentVO.getNum_count() == null ? new Integer(0) : parentVO.getNum_count();
						amount = price.multiply(num_count);
						break;
					case 3: // 设备
						break;
					case 4: // 吨公里
						break;
					case 6: // 节点 FIXME 后面会用到
						break;
					}
					pdBVO.setAmount(amount);
				}
			} else {
				pdBVO.setPrice(new UFDouble(0));
			}
		}
	}

	// 更新表头的总金额
	public static void setHeaderCostAmount(EntrustVO parentVO, PayDetailBVO[] pdBVOs) {
		if(parentVO == null || pdBVOs == null || pdBVOs.length == 0) {
			return;
		}
		UFDouble cost_amount = new UFDouble(0);
		if(pdBVOs != null && pdBVOs.length > 0) {
			for(PayDetailBVO pdBVO : pdBVOs) {
				if(pdBVO.getStatus() != VOStatus.DELETED) {
					cost_amount = cost_amount.add(pdBVO.getAmount() == null ? new UFDouble(0) : pdBVO.getAmount());
				}
			}
		}
		parentVO.setCost_amount(cost_amount);
	}

	/**
	 * 将运段的商品信息转换成委托单的商品信息
	 * 
	 * @param sPackVO
	 * @return
	 */
	public static EntPackBVO convert(SegPackBVO sPackVO) {
		EntPackBVO ePackVO = new EntPackBVO();
		ePackVO.setPk_invoice(sPackVO.getPk_invoice());
		ePackVO.setPk_segment(sPackVO.getPk_segment());
		ePackVO.setPk_goods(sPackVO.getPk_goods());
		ePackVO.setGoods_code(sPackVO.getGoods_code());
		ePackVO.setGoods_name(sPackVO.getGoods_name());
		ePackVO.setPlan_num(sPackVO.getPlan_num());
		ePackVO.setNum(sPackVO.getNum());
		ePackVO.setPlan_pack_num_count(sPackVO.getPlan_pack_num_count());
		ePackVO.setPack_num_count(sPackVO.getPack_num_count());
		ePackVO.setPack(sPackVO.getPack());
		ePackVO.setWeight(sPackVO.getWeight());
		ePackVO.setVolume(sPackVO.getVolume());
		ePackVO.setUnit_weight(sPackVO.getUnit_weight());
		ePackVO.setUnit_volume(sPackVO.getUnit_volume());
		ePackVO.setLength(sPackVO.getLength());
		ePackVO.setWidth(sPackVO.getWidth());
		ePackVO.setHeight(sPackVO.getHeight());
		ePackVO.setTrans_note(sPackVO.getTrans_note());
		ePackVO.setLow_temp(sPackVO.getLow_temp());
		ePackVO.setHight_temp(sPackVO.getHight_temp());
		ePackVO.setReference_no(sPackVO.getReference_no());
		ePackVO.setMemo(sPackVO.getMemo());
		ePackVO.setPk_seg_pack_b(sPackVO.getPk_seg_pack_b());
		return ePackVO;
	}

	/**
	 * 将运段的商品信息转换成委托单的商品信息
	 * 
	 * @param sPackVO
	 * @return
	 */
	public static SegPackBVO convert(EntPackBVO packBVO) {
		SegPackBVO segPackBVO = new SegPackBVO();
		segPackBVO.setPk_invoice(packBVO.getPk_invoice());
		segPackBVO.setPk_segment(packBVO.getPk_segment());
		segPackBVO.setPk_goods(packBVO.getPk_goods());
		segPackBVO.setGoods_code(packBVO.getGoods_code());
		segPackBVO.setGoods_name(packBVO.getGoods_name());
		segPackBVO.setPlan_num(packBVO.getPlan_num());
		segPackBVO.setNum(packBVO.getNum());
		segPackBVO.setPlan_pack_num_count(packBVO.getPlan_pack_num_count());
		segPackBVO.setPack_num_count(packBVO.getPack_num_count());
		segPackBVO.setPack(packBVO.getPack());
		segPackBVO.setWeight(packBVO.getWeight());
		segPackBVO.setVolume(packBVO.getVolume());
		segPackBVO.setUnit_weight(packBVO.getUnit_weight());
		segPackBVO.setUnit_volume(packBVO.getUnit_volume());
		segPackBVO.setLength(packBVO.getLength());
		segPackBVO.setWidth(packBVO.getWidth());
		segPackBVO.setHeight(packBVO.getHeight());
		segPackBVO.setTrans_note(packBVO.getTrans_note());
		segPackBVO.setLow_temp(packBVO.getLow_temp());
		segPackBVO.setHight_temp(packBVO.getHight_temp());
		segPackBVO.setReference_no(packBVO.getReference_no());
		segPackBVO.setMemo(packBVO.getMemo());
		return segPackBVO;
	}
	
	public static SegPackBVO convert(EntPackBVO packBVO,SegPackBVO oldSegPackBVO) {
		SegPackBVO segPackBVO = new SegPackBVO();
		segPackBVO.setPk_invoice(packBVO.getPk_invoice());
		segPackBVO.setPk_segment(packBVO.getPk_segment());
		segPackBVO.setPk_goods(packBVO.getPk_goods());
		segPackBVO.setGoods_code(packBVO.getGoods_code());
		segPackBVO.setGoods_name(packBVO.getGoods_name());
		segPackBVO.setPlan_num(packBVO.getPlan_num());
		if(packBVO.getNum() != null && oldSegPackBVO.getNum() != null){
			segPackBVO.setNum(oldSegPackBVO.getNum() - packBVO.getNum());
		}else{
			segPackBVO.setNum(oldSegPackBVO.getNum());
		}
		segPackBVO.setPlan_pack_num_count(packBVO.getPlan_pack_num_count());
		if(packBVO.getPack_num_count() != null && oldSegPackBVO.getPack_num_count() != null){
			segPackBVO.setPack_num_count(oldSegPackBVO.getPack_num_count().sub(packBVO.getPack_num_count()));
		}else{
			segPackBVO.setPack_num_count(oldSegPackBVO.getPack_num_count());
		}
		segPackBVO.setPack(packBVO.getPack());
		segPackBVO.setWeight(packBVO.getWeight());
		segPackBVO.setVolume(packBVO.getVolume());
		segPackBVO.setUnit_weight(packBVO.getUnit_weight());
		segPackBVO.setUnit_volume(packBVO.getUnit_volume());
		segPackBVO.setLength(packBVO.getLength());
		segPackBVO.setWidth(packBVO.getWidth());
		segPackBVO.setHeight(packBVO.getHeight());
		segPackBVO.setTrans_note(packBVO.getTrans_note());
		segPackBVO.setLow_temp(packBVO.getLow_temp());
		segPackBVO.setHight_temp(packBVO.getHight_temp());
		segPackBVO.setReference_no(packBVO.getReference_no());
		segPackBVO.setMemo(packBVO.getMemo());
		return segPackBVO;
	}
	
	public static SegPackBVO convert(EntPackBVO packBVO,EntPackBVO oldEntPackBVO) {
		SegPackBVO segPackBVO = new SegPackBVO();
		segPackBVO.setPk_invoice(packBVO.getPk_invoice());
		segPackBVO.setPk_segment(packBVO.getPk_segment());
		segPackBVO.setPk_goods(packBVO.getPk_goods());
		segPackBVO.setGoods_code(packBVO.getGoods_code());
		segPackBVO.setGoods_name(packBVO.getGoods_name());
		segPackBVO.setPlan_num(packBVO.getPlan_num());
		if(packBVO.getNum() != null && oldEntPackBVO.getNum() != null){
			segPackBVO.setNum(oldEntPackBVO.getNum() - packBVO.getNum());
		}else{
			segPackBVO.setNum(oldEntPackBVO.getNum());
		}
		segPackBVO.setPlan_pack_num_count(packBVO.getPlan_pack_num_count());
		segPackBVO.setPack(packBVO.getPack());
		segPackBVO.setWeight(packBVO.getUnit_weight() == null ? UFDouble.ZERO_DBL : packBVO.getUnit_weight().multiply(segPackBVO.getNum()));
		segPackBVO.setVolume(packBVO.getUnit_volume() == null ? UFDouble.ZERO_DBL : packBVO.getUnit_volume().multiply(segPackBVO.getNum()));
		segPackBVO.setUnit_weight(packBVO.getUnit_weight());
		segPackBVO.setUnit_volume(packBVO.getUnit_volume());
		segPackBVO.setLength(packBVO.getLength());
		segPackBVO.setWidth(packBVO.getWidth());
		segPackBVO.setHeight(packBVO.getHeight());
		segPackBVO.setTrans_note(packBVO.getTrans_note());
		segPackBVO.setLow_temp(packBVO.getLow_temp());
		segPackBVO.setHight_temp(packBVO.getHight_temp());
		segPackBVO.setReference_no(packBVO.getReference_no());
		segPackBVO.setMemo(packBVO.getMemo());
		return segPackBVO;
	}
	
	
	/**
	 * 重新匹配合同返回费用明细，参考EntrustService.refreshPayDetail的逻辑
	 * 
	 * @param contractService
	 * @param parentVO
	 * @param lineVOs
	 * @param oldDetailBVOs
	 * @return
	 */
	public static List<PayDetailBVO> getPayDetailBVOs(ContractService contractService, EntrustVO parentVO,
			EntTransbilityBVO[] tbBVOs, EntLineBVO[] lineVOs, List<PayDetailBVO> oldDetailBVOs) {
		return getPayDetailBVOs(contractService, parentVO, tbBVOs, lineVOs, oldDetailBVOs, null);
	}

	/**
	 * 重新匹配合同返回费用明细，参考EntrustService.refreshPayDetail的逻辑<br/>
	 * 如果传入了pdVO，那么有些参数使用这个对象的参数，使用在应付明细重新匹配合同的情况
	 * 
	 * @param contractService
	 * @param parentVO
	 * @param lineVOs
	 * @param oldDetailBVOs
	 * @return
	 */
	public static List<PayDetailBVO> getPayDetailBVOs(ContractService contractService, EntrustVO parentVO,
			EntTransbilityBVO[] tbBVOs, EntLineBVO[] lineVOs, List<PayDetailBVO> oldDetailBVOs, PayDetailVO pdVO) {
		if(contractService == null) {
			// 有些地方调用没有实例化contractService
			contractService = SpringContextHolder.getBean("contractServiceImpl");
		}
		//ExpenseTypeService expenseTypeService = SpringContextHolder.getBean("expenseTypeServiceImpl");
		// 重新匹配合同计算费用明细
		// 重新匹配合同，匹配合同后需要
		// 1、如果当前已经存在运费的记录，同时匹配返回的记录中也包括运费的记录，那么将现有的删除，而使用刚刚匹配到的记录代替
		// 2、更新表头的总金额，
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, parentVO.getPk_carrier(),
				parentVO.getPk_trans_type(), parentVO.getPk_delivery(), parentVO.getPk_arrival(),
				parentVO.getDeli_city(), parentVO.getArri_city(), parentVO.getPk_corp(), parentVO.getReq_arri_date(),
				parentVO.getUrgent_level(),parentVO.getItem_code(),parentVO.getPk_trans_line(),parentVO.getIf_return());
		if(contractBVOs != null && contractBVOs.size() > 0) {
			// 匹配到合同
			int node_count = 0;
			if(lineVOs == null) {
				// 没有传入，根据委托单去查询
				lineVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_entrust=?",
						parentVO.getPk_entrust());
			}
			if(lineVOs != null) {
				for(EntLineBVO elBVO : lineVOs) {
					if(elBVO.getStatus() != VOStatus.DELETED) {
						node_count++;
					}
				}
			}
			// 委托单对应的提货点
			int deli_node_count = InvoiceUtils.getDeliNodeCount(parentVO.getPk_entrust());

			int num_count;
			UFDouble volume_count, weight_count;
			if(pdVO != null) {
				num_count = pdVO.getNum_count() == null ? 0 : pdVO.getNum_count().intValue();
				volume_count = pdVO.getVolume_count() == null ? UFDouble.ZERO_DBL : pdVO.getVolume_count();
				weight_count = pdVO.getWeight_count() == null ? UFDouble.ZERO_DBL : pdVO.getWeight_count();
			} else {
				num_count = parentVO.getNum_count() == null ? 0 : parentVO.getNum_count().intValue();
				volume_count = parentVO.getVolume_count() == null ? UFDouble.ZERO_DBL : parentVO.getVolume_count();
				weight_count = parentVO.getWeight_count() == null ? UFDouble.ZERO_DBL : parentVO.getWeight_count();
			}

			// 重新计算体积重换算比
			// TODO 重新计算，计费重、体积重、总金额
			Map<String, UFDouble> retMap = PZUtils.computeFeeWeightCount(parentVO.getPk_carrier(),
					parentVO.getPk_trans_type(), 
					parentVO.getDeli_city(),parentVO.getArri_city(),volume_count, parentVO.getWeight_count());
			if(retMap == null) {
				// 不需要重新计算
			} else {
				parentVO.setFee_weight_count(retMap.get("fee_weight_count"));
				parentVO.setVolume_weight_count(retMap.get("volume_weight_count"));
			}

			String[] pk_car_type = null;
			if(tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for(int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}
			EntPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class,
					"pk_entrust =?",parentVO.getPk_entrust());
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			if(packBVOs != null && packBVOs.length > 0){
				Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
				//对包装按照pack进行分组
				for(EntPackBVO packBVO : packBVOs){
					String key = packBVO.getPack();
					if(StringUtils.isBlank(key)){
						//没有包装的货品自动过滤
						continue;
					}
					List<EntPackBVO> voList = groupMap.get(key);
					if(voList == null){
						voList = new ArrayList<EntPackBVO>();
						groupMap.put(key, voList);
					}
					voList.add(packBVO);
				}
				if (groupMap.size() > 0) {
					for(String key : groupMap.keySet()){
						PackInfo packInfo = new PackInfo();
						List<EntPackBVO> voList = groupMap.get(key);
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(EntPackBVO packBVO : voList){
							num = num + (packBVO.getNum() == null ? 0 : packBVO.getNum());
							weight = weight.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
							volume = volume.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						packInfos.add(packInfo);
					}
				}
			}
			List<PayDetailBVO> newDetailBVOs = contractService.buildPayDetailBVO(parentVO.getPk_carrier(),
					parentVO.getPack_num_count() == null ? 0 : parentVO.getPack_num_count().doubleValue(), num_count,
					parentVO.getFee_weight_count().doubleValue(), weight_count.doubleValue(),
					volume_count.doubleValue(), node_count, deli_node_count,packInfos, pk_car_type, parentVO.getPk_corp(),
					parentVO.getUrgent_level(), parentVO.getItem_code(),parentVO.getPk_trans_line(),parentVO.getIf_return(), contractBVOs);
			// 3、将旧的费用明细中系统创建标示为Y的记录，与新的费用明细进行匹配(根据费用类型进行匹配，费用类型是唯一的)，
			// 如果不存在于新的费用明细中,说明是之前匹配的，删除该记录
			if(newDetailBVOs != null && newDetailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for(PayDetailBVO detailBVO : newDetailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
				}

				if(oldDetailBVOs != null && oldDetailBVOs.size() > 0) {
					for(PayDetailBVO oldDetailBVO : oldDetailBVOs) {
						if(oldDetailBVO.getSystem_create() == null || !oldDetailBVO.getSystem_create().booleanValue()) {
						} else {
							if(oldDetailBVO.getStatus() != VOStatus.NEW) {
								oldDetailBVO.setStatus(VOStatus.DELETED);//
								// 删除原有的系统创建的费用明细
								newDetailBVOs.add(oldDetailBVO);
							}
						}
					}
				}
				oldDetailBVOs = newDetailBVOs;
			}
		} else {
			// XXX 2014-03-27 如果没有匹配到合同
			if(oldDetailBVOs != null && oldDetailBVOs.size() > 0) {
				for(PayDetailBVO oldDetailBVO : oldDetailBVOs) {
					if(oldDetailBVO.getSystem_create() == null || !oldDetailBVO.getSystem_create().booleanValue()) {
						// 非系统创建的费用明细

					} else {
						// 系统创建的费用明细
						oldDetailBVO.setStatus(VOStatus.DELETED);// 删除原有的系统创建的费用明细
					}
				}
			}
		}
		return oldDetailBVOs;
	}

	public static List<PayDetailBVO> getPayDetailBVOs(ContractService contractService, EntrustVO parentVO,
			EntTransbilityBVO[] tbBVOs, Integer node_count, List<PayDetailBVO> oldDetailBVOs, PayDetailVO pdVO) {
		if(contractService == null) {
			// 有些地方调用没有实例化contractService
			contractService = SpringContextHolder.getBean("contractServiceImpl");
		}
		//ExpenseTypeService expenseTypeService = SpringContextHolder.getBean("expenseTypeServiceImpl");
		// 重新匹配合同计算费用明细
		// 重新匹配合同，匹配合同后需要
		// 1、如果当前已经存在运费的记录，同时匹配返回的记录中也包括运费的记录，那么将现有的删除，而使用刚刚匹配到的记录代替
		// 2、更新表头的总金额，
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, parentVO.getPk_carrier(),
				parentVO.getPk_trans_type(), parentVO.getPk_delivery(), parentVO.getPk_arrival(),
				parentVO.getDeli_city(), parentVO.getArri_city(), parentVO.getPk_corp(), parentVO.getReq_arri_date(),
				parentVO.getUrgent_level(),parentVO.getItem_code(),parentVO.getPk_trans_line(),parentVO.getIf_return());
		if(contractBVOs != null && contractBVOs.size() > 0) {
			int num_count;
			UFDouble volume_count, weight_count;
			if(pdVO != null) {
				num_count = pdVO.getNum_count() == null ? 0 : pdVO.getNum_count().intValue();
				volume_count = pdVO.getVolume_count() == null ? UFDouble.ZERO_DBL : pdVO.getVolume_count();
				weight_count = pdVO.getWeight_count() == null ? UFDouble.ZERO_DBL : pdVO.getWeight_count();
			} else {
				num_count = parentVO.getNum_count() == null ? 0 : parentVO.getNum_count().intValue();
				volume_count = parentVO.getVolume_count() == null ? UFDouble.ZERO_DBL : parentVO.getVolume_count();
				weight_count = parentVO.getWeight_count() == null ? UFDouble.ZERO_DBL : parentVO.getWeight_count();
			}

			// 重新计算体积重换算比
			// TODO 重新计算，计费重、体积重、总金额
			Map<String, UFDouble> retMap = PZUtils.computeFeeWeightCount(parentVO.getPk_carrier(),
					parentVO.getPk_trans_type(),
					parentVO.getDeli_city(),parentVO.getArri_city(),
					volume_count, parentVO.getWeight_count());
			if(retMap == null) {
				// 不需要重新计算
			} else {
				parentVO.setFee_weight_count(retMap.get("fee_weight_count"));
				parentVO.setVolume_weight_count(retMap.get("volume_weight_count"));
			}

			String[] pk_car_type = null;
			if(tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for(int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}
			EntPackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class,
					"pk_entrust =?",parentVO.getPk_entrust());
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			if(packBVOs != null && packBVOs.length > 0){
				Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
				//对包装按照pack进行分组
				for(EntPackBVO packBVO : packBVOs){
					String key = packBVO.getPack();
					if(StringUtils.isBlank(key)){
						//没有包装的货品自动过滤
						continue;
					}
					List<EntPackBVO> voList = groupMap.get(key);
					if(voList == null){
						voList = new ArrayList<EntPackBVO>();
						groupMap.put(key, voList);
					}
					voList.add(packBVO);
				}
				if (groupMap.size() > 0) {
					for(String key : groupMap.keySet()){
						PackInfo packInfo = new PackInfo();
						List<EntPackBVO> voList = groupMap.get(key);
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(EntPackBVO packBVO : voList){
							num = num + (packBVO.getNum() == null ? 0 : packBVO.getNum());
							weight = weight.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
							volume = volume.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						packInfos.add(packInfo);
					}
				}
			}
			List<PayDetailBVO> newDetailBVOs = contractService.buildPayDetailBVO(parentVO.getPk_carrier(),
					parentVO.getPack_num_count() == null ? 0 : parentVO.getPack_num_count().doubleValue(), num_count,
					parentVO.getFee_weight_count().doubleValue(), weight_count.doubleValue(),
					volume_count.doubleValue(), node_count, node_count,packInfos, pk_car_type, parentVO.getPk_corp(),
					parentVO.getUrgent_level(), parentVO.getItem_code(),parentVO.getPk_trans_line(),parentVO.getIf_return(), contractBVOs);
			// 3、将旧的费用明细中系统创建标示为Y的记录，与新的费用明细进行匹配(根据费用类型进行匹配，费用类型是唯一的)，
			// 如果不存在于新的费用明细中,说明是之前匹配的，删除该记录
			if(newDetailBVOs != null && newDetailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for(PayDetailBVO detailBVO : newDetailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
				}

				if(oldDetailBVOs != null && oldDetailBVOs.size() > 0) {
					for(PayDetailBVO oldDetailBVO : oldDetailBVOs) {
						if(oldDetailBVO.getSystem_create() == null || !oldDetailBVO.getSystem_create().booleanValue()) {
						} else {
							if(oldDetailBVO.getStatus() != VOStatus.NEW) {
								oldDetailBVO.setStatus(VOStatus.DELETED);//
								// 删除原有的系统创建的费用明细
								newDetailBVOs.add(oldDetailBVO);
							}
						}
					}
				}
				oldDetailBVOs = newDetailBVOs;
			}
		} else {
			// XXX 2014-03-27 如果没有匹配到合同
			if(oldDetailBVOs != null && oldDetailBVOs.size() > 0) {
				for(PayDetailBVO oldDetailBVO : oldDetailBVOs) {
					if(oldDetailBVO.getSystem_create() == null || !oldDetailBVO.getSystem_create().booleanValue()) {
						// 非系统创建的费用明细

					} else {
						// 系统创建的费用明细
						oldDetailBVO.setStatus(VOStatus.DELETED);// 删除原有的系统创建的费用明细
					}
				}
			}
		}
		return oldDetailBVOs;
	}

	
	/**
	 * 同步委托单的跟踪异常信息,将最新的跟踪信息写入委托单表，为了显示上的方便
	 * 
	 * @param etVO
	 */
	public static void syncEntrustTrackingInfo(EntTrackingVO etVO, EntrustVO entVO, boolean buildMemo) {
		if(etVO == null) {// 可能是删除了跟踪信息后同步到委托单
			entVO.setTracking_time(null);
			entVO.setTracking_status(null);
			entVO.setTracking_memo(null);
			entVO.setExp_flag(null);
			entVO.setExp_type(null);
		} else {
			entVO.setTracking_time(etVO.getTracking_time());
			entVO.setTracking_status(etVO.getTracking_status());
			entVO.setEst_arri_time(etVO.getEst_arri_time());

			// XXX 2013-7-10,增加一个跟踪时间字段后，不再需要组合这个跟踪信息了
			// if(buildMemo) {
			// entVO.setTracking_memo(etVO.getTracking_time().toString() +
			// etVO.getTracking_memo()
			// + getTrackingStatusName(entVO.getTracking_status() + ""));//
			// 跟踪时间+跟踪信息+跟踪状态
			// } else {
			entVO.setTracking_memo(etVO.getTracking_memo());
			// }

			if((entVO.getExp_flag() != null && entVO.getExp_flag().booleanValue())
					|| (etVO.getExp_flag() != null && etVO.getExp_flag().booleanValue())) {
				entVO.setExp_flag(UFBoolean.TRUE);
			} else {
				entVO.setExp_flag(UFBoolean.FALSE);
			}
			String exp_type = entVO.getExp_type() == null ? "" : entVO.getExp_type();
			if(StringUtils.isNotBlank(etVO.getExp_type())) {
				if(StringUtils.isNotBlank(exp_type)) {
					exp_type += ",";
				}
				String[] arr = etVO.getExp_type().split(",");
				for(String one : arr) {
					if(exp_type.indexOf(one) == -1) {
						exp_type += etVO.getExp_type();
						exp_type += ",";
					}
				}
			}
			if(exp_type.endsWith(",")) {
				exp_type = exp_type.substring(0, exp_type.length() - 1);
			}
			entVO.setExp_type(exp_type);
		}
	}

	/**
	 * 同步发货单的跟踪异常信息,将最新的跟踪信息写入发货单表，为了显示上的方便
	 * 
	 * @param etVO
	 * @param invVO
	 */
	public static void syncInvoiceTrackingInfo(EntTrackingVO etVO, InvoiceVO invVO, boolean buildMemo) {
		if(etVO == null) {
			invVO.setTracking_time(null);
			invVO.setTracking_status(null);
			invVO.setTracking_memo(null);
			invVO.setExp_flag(null);
			invVO.setExp_type(null);
		} else {
			invVO.setTracking_time(etVO.getTracking_time());
			invVO.setTracking_status(etVO.getTracking_status());
			// XXX 2013-7-10,增加一个跟踪时间字段后，不再需要组合这个跟踪信息了
			// if(buildMemo) {
			// invVO.setTracking_memo(etVO.getTracking_time().toString() +
			// etVO.getTracking_memo()
			// + BillStatus.inv_status_map.get(invVO.getVbillstatus()));//
			// 跟踪时间+跟踪信息+发货单状态
			// } else {
			invVO.setTracking_memo(etVO.getTracking_memo());
			// }

			if((invVO.getExp_flag() != null && invVO.getExp_flag().booleanValue())
					|| (etVO.getExp_flag() != null && etVO.getExp_flag().booleanValue())) {
				invVO.setExp_flag(UFBoolean.TRUE);
			} else {
				invVO.setExp_flag(UFBoolean.FALSE);
			}
			String exp_type = invVO.getExp_type() == null ? "" : invVO.getExp_type();
			if(StringUtils.isNotBlank(etVO.getExp_type())) {
				if(StringUtils.isNotBlank(exp_type)) {
					exp_type += ",";
				}
				String[] arr = etVO.getExp_type().split(",");
				for(String one : arr) {
					if(exp_type.indexOf(one) == -1) {
						exp_type += etVO.getExp_type();
						exp_type += ",";
					}
				}
			}
			if(exp_type.endsWith(",")) {
				exp_type = exp_type.substring(0, exp_type.length() - 1);
			}
			invVO.setExp_type(exp_type);
		}
	}

	/**
	 * 保存一条跟踪信息,同步到发货单
	 * 
	 * @param etVO
	 * @param entVO
	 */
	public static void saveEntTracking(EntTrackingVO etVO, EntrustVO entVO) {
		logger.info("保存一条委托单跟踪信息...");
		if(etVO == null) {
			return;
		}
		etVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(etVO);

		NWDao dao = NWDao.getInstance();
		dao.saveOrUpdate(etVO);

		syncEntrustTrackingInfo(etVO, entVO, true);
		dao.saveOrUpdate(entVO);

		// 如果勾选了同步客户跟踪信息
		if(etVO.getSync_flag() != null && etVO.getSync_flag().booleanValue()) {
			List<InvoiceVO> invVOs = null;
			if(StringUtils.isNotBlank(etVO.getInvoice_vbillno())) {
				// 同时向发货单插入一条跟踪记录
				String[] invoice_vbillno_ary = etVO.getInvoice_vbillno().split("\\|");
				String cond = NWUtils.buildConditionString(invoice_vbillno_ary);
				invVOs = dao.queryForList("select * from ts_invoice where isnull(dr,0)=0 and vbillno in" + cond,
						InvoiceVO.class);
			} else {
				// 同时向委托单关联的所有发货单插入跟踪记录
				String sql = "select * from ts_invoice where pk_invoice in "
						+ "(select pk_invoice from ts_ent_inv_b where pk_entrust=(select pk_entrust from ts_entrust where vbillno=?))";
				invVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, etVO.getEntrust_vbillno());
			}
			for(InvoiceVO invVO : invVOs) {
				InvTrackingVO itVO = new InvTrackingVO();
				itVO.setInvoice_vbillno(invVO.getVbillno());
				itVO.setEntrust_vbillno(etVO.getEntrust_vbillno());
				itVO.setTracking_status(etVO.getTracking_status());
				itVO.setTracking_time(etVO.getTracking_time());
				itVO.setTracking_memo(etVO.getTracking_memo());
				itVO.setExp_flag(etVO.getExp_flag());
				itVO.setExp_type(etVO.getExp_type());
				itVO.setExp_memo(etVO.getExp_memo());
				itVO.setPk_corp(etVO.getPk_corp());
				itVO.setCreate_user(etVO.getCreate_user());
				itVO.setCreate_time(etVO.getCreate_time());
				itVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(itVO);
				NWDao.getInstance().saveOrUpdate(itVO);

				syncInvoiceTrackingInfo(etVO, invVO, false);
				invVO.setStatus(VOStatus.UPDATED);
				dao.saveOrUpdate(invVO);
			}
		}
	}

	/**
	 * 根据委托单查询车牌号，并使用逗号连接
	 * 
	 * @param pk_entrust
	 * @return
	 */
	public static String getCarno(String pk_entrust) {
		if(StringUtils.isBlank(pk_entrust)) {
			return "";
		}
		String sql = "select carno from ts_ent_transbility_b WITH(NOLOCK) where pk_entrust=? and isnull(dr,0)=0";
		List<String> carnoList = NWDao.getInstance().queryForList(sql, String.class, pk_entrust);
		if(carnoList == null || carnoList.size() == 0) {
			return "";
		}
		return NWUtils.join(carnoList.toArray(new String[carnoList.size()]), ",");
	
	}
	
	
	
	
	
	//根据委托单信息，获取费用
	
	
	
	
	
	
}
