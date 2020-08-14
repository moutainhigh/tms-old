package com.tms.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.exception.BusiException;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.QuoteTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.base.CarrService;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.service.te.impl.EntrustUtils;
import com.tms.service.tp.impl.PZUtils;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractMatchVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBMatchVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceDetailBMatchVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;

public class CostCalculater {
	
	private ContractService contractService = SpringContextHolder.getBean("contractServiceImpl");
	
	private CarrService carrService = SpringContextHolder.getBean("carrServiceImpl");;
	
	
	private static final String MILKRUN_TRANS_TYPE = "89816b7d4cfe457881425a48fad21cc8";
	
	private static final String ET110_PK = "dd6e3ab545e94cb3be5bc8995e1fad4a"; // 运费
	
	private static final String ET120_PK = "9436f31e58fc44d1981471b4c2d50e95"; // 点位费
	
	private static final String ET40_PK = "b980f421ca4b4f689b261254bcdbe445";  // 保险费
	
	/**
	 * @param billIds 应收明细单据号
	 * @return 应收明细 和 子明细的Map集合
	 */
	public Map<ReceiveDetailVO, List<ReceDetailBVO>> computeReceivable(List<String> billIds){
		if(billIds == null || billIds.size() == 0){
			return null;
		}
		ReceiveDetailVO[] detailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
				"pk_receive_detail in " + NWUtils.buildConditionString(billIds));
		if(detailVOs == null || detailVOs.length == 0){
			throw new BusiException("应收明细不存在！");
		}
		
		List<String> invoice_vbillnos = new ArrayList<String>();
		for(ReceiveDetailVO rdVO : detailVOs){
			if (rdVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应收明细[?]必须是[新建]状态才能进行重算金额！",rdVO.getVbillno());
			}
			if (rdVO.getRece_type().intValue() != ReceiveDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应收明细[?]必须是[原始单据]类型才能进行重算金额！",rdVO.getVbillno());
			}
			if (StringUtils.isBlank(rdVO.getInvoice_vbillno())) {
				throw new BusiException("应收明细[?]没有对应的发货单！",rdVO.getVbillno());
			}
			invoice_vbillnos.add(rdVO.getInvoice_vbillno());
		}
		
		InvoiceVO[] invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
				"vbillno in " + NWUtils.buildConditionString(invoice_vbillnos));
		
		ReceDetailBVO[] allOldDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceDetailBVO.class,
				"pk_receive_detail in " + NWUtils.buildConditionString(billIds));
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		//验证发货单是否存在
		Map<ReceiveDetailVO, List<ReceDetailBVO>> results = new HashMap<ReceiveDetailVO, List<ReceDetailBVO>>();
		for(ReceiveDetailVO rdVO : detailVOs){
			boolean exist = false;
			InvoiceVO invoiceVO = null;
			for(InvoiceVO invVO : invVOs){
				if(invVO.getVbillno().equals(rdVO.getInvoice_vbillno())){
					exist = true;
					invoiceVO = invVO;
					break;
				}
			}
			if(!exist){
				throw new BusiException("应收明细[?]对应的发货单[?]已经不存在！",rdVO.getVbillno(),rdVO.getInvoice_vbillno());
			}
			
			//获取发货单的运力信息
			TransBilityBVO[] tbBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(TransBilityBVO.class,
					" pk_invoice =? ",invoiceVO.getPk_invoice());
			String[] pk_car_type = null;
			if (tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for (int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}
			//这里直接写死milkrun运输方式的pk
			List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
			List<ReceDetailBVO> detailBVOs = null;
			if(invoiceVO.getPk_trans_type().equals(MILKRUN_TRANS_TYPE)){
				detailBVOs = milkRunReceComputer(invoiceVO,rdVO, pk_car_type);
			}else{
				detailBVOs = normalReceComputer(invoiceVO,rdVO, pk_car_type);
			}
			if (detailBVOs != null && detailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for (ReceDetailBVO detailBVO : detailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(detailBVO);
					detailBVO.setPk_receive_detail(rdVO.getPk_receive_detail()); // 设置主表的主键
				}
			}
			
			for(ReceDetailBVO detailBVO : allOldDetailBVOs){
				if(detailBVO.getPk_receive_detail().equals(rdVO.getPk_receive_detail())){
					if(detailBVO.getSystem_create() != null
						&& detailBVO.getSystem_create().equals(UFBoolean.TRUE)){
						detailBVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(detailBVO);
					}else{
						detailBVOs.add(detailBVO);
					}
					
				}
			}
			if (detailBVOs != null && detailBVOs.size() > 0){
				toBeUpdate.addAll(detailBVOs);
			}
			
			// 保存到应收明细表
			rdVO.setStatus(VOStatus.UPDATED);
			//取第一行合同明细的税种，税率
			if (contractBVOs != null && contractBVOs.size() > 0) {
				rdVO.setTax_cat(contractBVOs.get(0).getTax_cat());
				rdVO.setTax_rate(contractBVOs.get(0).getTax_rate());
				// 取第一行合同明细的税种，税率
				rdVO.setTaxmny(CMUtils.getTaxmny(rdVO.getCost_amount(), rdVO.getTax_cat(), rdVO.getTax_rate()));
			}
			toBeUpdate.add(rdVO);// 保存应收明细
			// 如果运力信息存在记录，而费用明细没有计价方式为“设备”的记录，那么需要插入一条费用明细
			// 将根据参数判断是否加上保险费
			boolean autoGenInsurance = ParameterHelper.getBooleanParam(ParameterHelper.AUTO_GEN_INSURANCE);// 读取参数是否自动生成保险单
			if (autoGenInsurance) {
				// 增加一个新的保险费
				if (invoiceVO.getIf_insurance() != null && invoiceVO.getIf_insurance().equals(UFBoolean.TRUE)) {
					ReceDetailBVO insBVO = new ReceDetailBVO();
					insBVO.setPk_expense_type(ET40_PK);
					insBVO.setValuation_type(ValuationTypeConst.TICKET);
					insBVO.setQuote_type(QuoteTypeConst.INTERVAL);
					insBVO.setPrice_type(PriceTypeConst.REGULAR_PRICE);
					insBVO.setAmount(invoiceVO.getInsurance_amount());
					insBVO.setContract_amount(invoiceVO.getInsurance_amount());
					insBVO.setSystem_create(UFBoolean.TRUE);
					insBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(insBVO);
					insBVO.setPk_receive_detail(rdVO.getPk_receive_detail());
					detailBVOs.add(insBVO);
				}
			}
			// 重新统计费用
			CMUtils.processExtenalforComputer(rdVO, detailBVOs.toArray(new ReceDetailBVO[detailBVOs.size()]));
			results.put(rdVO, detailBVOs);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		CMUtils.totalCostComput(Arrays.asList(invVOs));
		return results;
	}
	
	/**
	 * @param billIds 应收明细单据号
	 * @return 应收明细 和 子明细的Map集合
	 */
	public void buildReceiveDetail(ExAggInvoiceVO aggVO){
		
	}
	
	private List<ReceDetailBVO> normalReceComputer(InvoiceVO invoiceVO,ReceiveDetailVO rdVO,String[] pk_car_type){
		// 重新匹配合同，匹配合同后需要
		// 1、如果当前已经存在运费的记录，同时匹配返回的记录中也包括运费的记录，那么将现有的删除，而使用刚刚匹配到的记录代替
		// 2、更新表头的总金额，
		// 3、更新运力信息的单价和金额
		List<ReceDetailBVO> newDetailBVOs = new ArrayList<ReceDetailBVO>();
		ContractMatchVO contractMatchVO = new ContractMatchVO();
		contractMatchVO.setContract_type(ContractConst.CUSTOMER);
		contractMatchVO.setPk_carrierOrBala_customer(invoiceVO.getBala_customer());
		contractMatchVO.setPk_trans_type(invoiceVO.getPk_trans_type());
		contractMatchVO.setStart_addr(invoiceVO.getPk_delivery());
		contractMatchVO.setEnd_addr(invoiceVO.getPk_arrival());
		contractMatchVO.setStart_city(invoiceVO.getDeli_city());
		contractMatchVO.setEnd_city(invoiceVO.getArri_city());
		contractMatchVO.setPk_corp(invoiceVO.getPk_corp());
		contractMatchVO.setReq_arri_date(invoiceVO.getReq_deli_date());
		contractMatchVO.setUrgent_level(invoiceVO.getUrgent_level());
		contractMatchVO.setItem_code(invoiceVO.getItem_code());
		contractMatchVO.setPk_trans_line(invoiceVO.getPk_trans_line());
		contractMatchVO.setIf_return(invoiceVO.getIf_return());
		List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
		if (contractBVOs != null && contractBVOs.size() > 0) {
			InvPackBVO[] invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
					"pk_invoice =?", invoiceVO.getPk_invoice());
			
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			Map<String,List<InvPackBVO>> groupMap = new  HashMap<String,List<InvPackBVO>>();
			//对包装按照pack进行分组
			for(InvPackBVO invPackBVO : invPackBVOs){
				String key = invPackBVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<InvPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<InvPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(invPackBVO);
			}
			if (groupMap.size() > 0) {
				for(String key : groupMap.keySet()){
					PackInfo packInfo = new PackInfo();
					List<InvPackBVO> voList = groupMap.get(key);
					Integer num = 0;
					UFDouble weight = UFDouble.ZERO_DBL;
					UFDouble volume = UFDouble.ZERO_DBL;
					for(InvPackBVO packBVO : voList){
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
			
			ReceDetailBMatchVO receDetailBMatchVO = new ReceDetailBMatchVO();
			receDetailBMatchVO.setBala_customer(rdVO.getBala_customer());
			receDetailBMatchVO.setPack_num_count(rdVO.getPack_num_count());
			receDetailBMatchVO.setNum_count(rdVO.getNum_count());
			receDetailBMatchVO.setFee_weight_count(rdVO.getFee_weight_count());
			receDetailBMatchVO.setWeight_count(rdVO.getWeight_count());
			receDetailBMatchVO.setVolume_count(rdVO.getVolume_count());
			receDetailBMatchVO.setPk_car_types(pk_car_type);
			receDetailBMatchVO.setPk_corp(rdVO.getPk_corp());
			receDetailBMatchVO.setContractBVOs(contractBVOs);
			receDetailBMatchVO.setPackInfos(packInfos);
			
			newDetailBVOs = contractService.buildReceDetailBVO(receDetailBMatchVO);
		}
		return newDetailBVOs;
	}
	
	private List<ReceDetailBVO> milkRunReceComputer(InvoiceVO invoiceVO,ReceiveDetailVO rdVO,String[] pk_car_type){
		List<ReceDetailBVO> newReceDetailBVOs = new ArrayList<ReceDetailBVO>();
		// 获取所有的线路信息
		String accessRule = ParameterHelper.getMilkRunNodeAccessRule();
		String[] accessRules = accessRule.split("\\" + Constants.SPLIT_CHAR);
		String accessRulesCond = NWUtils.buildConditionString(accessRules);
		String invLineBSql = "select * from ts_inv_line_b with(nolock) where isnull(dr,0)=0 "
				+ " and pk_invoice ='"+ invoiceVO.getPk_invoice() +"' and operate_type in "+accessRulesCond;
		List<InvLineBVO> listLineBVOs = NWDao.getInstance().queryForList(invLineBSql,InvLineBVO.class);
		InvLineBVO[] lineBVOs = listLineBVOs.toArray(new InvLineBVO[listLineBVOs.size()]);
		// 对线路信息进行分类
		List<InvLineBVO> points = new ArrayList<InvLineBVO>();
		List<InvLineBVO> bases = new ArrayList<InvLineBVO>();
		
		for (int i = 0; i < lineBVOs.length; i++) {
			if (i == 0) {
				// 第一个点必定是基点
				bases.add(lineBVOs[i]);
			} else {
				if (lineBVOs[i].getPk_address().equals(lineBVOs[i - 1].getPk_address())) {
					continue;
				} else {
					if (lineBVOs[i].getPk_city().equals(lineBVOs[i - 1].getPk_city())) {
						points.add(lineBVOs[i]);
					} else {
						bases.add(lineBVOs[i]);
					}
				}
			}
		}
		
		Map<String, List<InvLineBVO>> groupMap = new HashMap<String, List<InvLineBVO>>();
		for (InvLineBVO invLineBVO : points) {
			String key = invLineBVO.getPk_city();
			List<InvLineBVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<InvLineBVO>();
				groupMap.put(key, voList);
			}
			voList.add(invLineBVO);
		}
		if (pk_car_type == null || pk_car_type.length == 0) {
			throw new BusiException("没有录入车辆信息，请检查数据！");
		}
		
		for (InvLineBVO invLineBVO : bases) {
			ContractMatchVO contractMatchVO = new ContractMatchVO();
			contractMatchVO.setContract_type(ContractConst.CUSTOMER);
			contractMatchVO.setPk_carrierOrBala_customer(invoiceVO.getBala_customer());
			contractMatchVO.setPk_trans_type(invoiceVO.getPk_trans_type());
			contractMatchVO.setStart_addr(invLineBVO.getPk_address());
			contractMatchVO.setStart_city(invLineBVO.getPk_city());
			contractMatchVO.setPk_corp(invoiceVO.getPk_corp());
			contractMatchVO.setReq_arri_date(invoiceVO.getReq_deli_date());
			contractMatchVO.setUrgent_level(invoiceVO.getUrgent_level());
			contractMatchVO.setItem_code(invoiceVO.getItem_code());
			contractMatchVO.setPk_trans_line(invoiceVO.getPk_trans_line());
			contractMatchVO.setIf_return(invoiceVO.getIf_return());
			// 只有一个点说明这个应该是基费 开始匹配合同
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
			if (contractBVOs != null && contractBVOs.size() > 0) {
				for (ContractBVO contractBVO : contractBVOs) {
					// 只有结果为基费的合同才是我们需要的,并且合同设备类型需要相符
					if (contractBVO.getPk_expense_type().equals(ET110_PK)
							&& pk_car_type[0].equals(contractBVO.getEquip_type())) {
						List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
						//只会存在一个合同
						unitContractBVO.add(contractBVO);
						//计算包装，用于匹配费用
						List<PackInfo> packInfos = new ArrayList<PackInfo>();
						PackInfo packInfo = new PackInfo();
						packInfo.setPack(invLineBVO.getPack());
						packInfo.setNum(invLineBVO.getNum());
						packInfo.setWeight(invLineBVO.getWeight());
						packInfo.setVolume(invLineBVO.getVolume());
						packInfos.add(packInfo);
						ReceDetailBMatchVO receDetailBMatchVO = new ReceDetailBMatchVO();
						receDetailBMatchVO.setBala_customer(rdVO.getBala_customer());
						receDetailBMatchVO.setPack_num_count(rdVO.getPack_num_count());
						receDetailBMatchVO.setNum_count(rdVO.getNum_count());
						receDetailBMatchVO.setFee_weight_count(rdVO.getFee_weight_count());
						receDetailBMatchVO.setWeight_count(rdVO.getWeight_count());
						receDetailBMatchVO.setVolume_count(rdVO.getVolume_count());
						receDetailBMatchVO.setPackInfos(packInfos);
						receDetailBMatchVO.setPk_car_types(pk_car_type);
						receDetailBMatchVO.setPk_corp(rdVO.getPk_corp());
						receDetailBMatchVO.setContractBVOs(unitContractBVO);
						
						List<ReceDetailBVO> detailBVOs = contractService.buildReceDetailBVO(receDetailBMatchVO);
						if(detailBVOs == null || detailBVOs.size() == 0){
							continue;
						}
						detailBVOs.get(0).setPrice(detailBVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL : detailBVOs.get(0).getAmount());
						newReceDetailBVOs.addAll(detailBVOs); 
					}
				}
			}
		}
		for (String key : groupMap.keySet()) {
			
			ContractMatchVO contractMatchVO = new ContractMatchVO();
			contractMatchVO.setContract_type(ContractConst.CUSTOMER);
			contractMatchVO.setPk_carrierOrBala_customer(invoiceVO.getBala_customer());
			contractMatchVO.setPk_trans_type(invoiceVO.getPk_trans_type());
			contractMatchVO.setStart_addr(groupMap.get(key).get(0).getPk_city());
			contractMatchVO.setStart_city(groupMap.get(key).get(0).getPk_city());
			contractMatchVO.setPk_corp(invoiceVO.getPk_corp());
			contractMatchVO.setReq_arri_date(invoiceVO.getReq_deli_date());
			contractMatchVO.setUrgent_level(invoiceVO.getUrgent_level());
			contractMatchVO.setItem_code(invoiceVO.getItem_code());
			contractMatchVO.setPk_trans_line(invoiceVO.getPk_trans_line());
			contractMatchVO.setIf_return(invoiceVO.getIf_return());
			
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
			if (contractBVOs != null && contractBVOs.size() > 0) {
				for (ContractBVO contractBVO : contractBVOs) {
					// 只有结果为点位费的合同才是我们需要的,并且合同设备类型需要相符
					if (contractBVO.getPk_expense_type().equals(ET120_PK)
							&& pk_car_type[0].equals(contractBVO.getEquip_type())) {
						List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
						unitContractBVO.add(contractBVO);
						
						ReceDetailBMatchVO receDetailBMatchVO = new ReceDetailBMatchVO();
						receDetailBMatchVO.setBala_customer(rdVO.getBala_customer());
						receDetailBMatchVO.setPack_num_count(rdVO.getPack_num_count());
						receDetailBMatchVO.setNum_count(rdVO.getNum_count());
						receDetailBMatchVO.setFee_weight_count(rdVO.getFee_weight_count());
						receDetailBMatchVO.setWeight_count(rdVO.getWeight_count());
						receDetailBMatchVO.setVolume_count(rdVO.getVolume_count());
						receDetailBMatchVO.setPk_car_types(pk_car_type);
						receDetailBMatchVO.setPk_corp(rdVO.getPk_corp());
						receDetailBMatchVO.setContractBVOs(unitContractBVO);
						
						List<ReceDetailBVO> detailBVOs = contractService.buildReceDetailBVO(receDetailBMatchVO);
						if(detailBVOs == null || detailBVOs.size() == 0){
							continue;
						}
						//只会存在一个合同
						detailBVOs.get(0).setPrice(detailBVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL : detailBVOs.get(0).getAmount());
						detailBVOs.get(0).setAmount(detailBVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL : detailBVOs.get(0).getAmount().multiply(groupMap.get(key).size()));
						detailBVOs.get(0).setContract_amount(detailBVOs.get(0).getAmount());
						newReceDetailBVOs.addAll(detailBVOs); 
					}
				}
			}
		}
		return newReceDetailBVOs;
		
	}
	
	public Map<PayDetailVO, List<PayDetailBVO>> computeUnitPayable(List<String> billIds){
		
		if(billIds == null || billIds.size() == 0){
			return null;
		}
		PayDetailVO[] detailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(billIds));
		if(detailVOs == null || detailVOs.length == 0){
			throw new BusiException("应付明细不存在！");
		}
		List<String> entrust_vbillnos = new ArrayList<String>();
		for(PayDetailVO pdVO : detailVOs){
			if(pdVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应付明细[?]必须是[新建]状态才能进行重算金额！",pdVO.getVbillno());
			}
			if(pdVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！",pdVO.getVbillno());
			}
			if(StringUtils.isBlank(pdVO.getEntrust_vbillno())) {
				throw new BusiException("应付明细[?]没有对应的委托单！",pdVO.getVbillno());
			}
			entrust_vbillnos.add(pdVO.getEntrust_vbillno());
		}
		EntrustVO[] entVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				"vbillno in " + NWUtils.buildConditionString(entrust_vbillnos));
		
		PayDetailBVO[] allOldDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(billIds));
		String lineBSql = "select * from ts_ent_line_b WITH(NOLOCK) "
				+ " left join ts_entrust WITH(NOLOCK) ON ts_entrust.pk_entrust = ts_ent_line_b.pk_entrust "
				+ " WHERE isnull(ts_ent_line_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 "
				+ " AND ts_entrust.vbillno in " + NWUtils.buildConditionString(entrust_vbillnos) + " ORDER BY ts_ent_line_b.serialno asc ";
		List<EntLineBVO> allLineVOs = NWDao.getInstance().queryForList(lineBSql, EntLineBVO.class);
		
		String packBSql = "select * from ts_ent_pack_b WITH(NOLOCK) "
				+ " left join ts_entrust WITH(NOLOCK) ON ts_entrust.pk_entrust = ts_ent_pack_b.pk_entrust "
				+ " WHERE isnull(ts_ent_pack_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 "
				+ " AND ts_entrust.vbillno in " + NWUtils.buildConditionString(entrust_vbillnos);
		List<EntPackBVO> allpackVOs = NWDao.getInstance().queryForList(packBSql, EntPackBVO.class);
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		//验证委托单是否存在
		Map<PayDetailVO, List<PayDetailBVO>> results = new HashMap<PayDetailVO, List<PayDetailBVO>>();
		for(PayDetailVO pdVO : detailVOs){
			boolean exist = false;
			EntrustVO entrustVO = null;
			for(EntrustVO entVO : entVOs){
				if(entVO.getVbillno().equals(pdVO.getEntrust_vbillno())){
					exist = true;
					entrustVO = entVO;
					break;
				}
			}
			if(!exist){
				throw new BusiException("应付明细[?]对应的委托单[?]已经不存在！",pdVO.getVbillno(),pdVO.getEntrust_vbillno());
			}
			
			List<PayDetailBVO> oldDetailBVOs = new ArrayList<PayDetailBVO>();
			for(PayDetailBVO detailBVO : allOldDetailBVOs){
				if(detailBVO.getPk_pay_detail().equals(pdVO.getPk_pay_detail())){
					oldDetailBVOs.add(detailBVO);
				}
			}
			
			List<EntLineBVO> lineBVOs = new ArrayList<EntLineBVO>();
			for(EntLineBVO lineBVO : allLineVOs){
				if(lineBVO.getPk_entrust().equals(entrustVO.getPk_entrust())){
					lineBVOs.add(lineBVO);
				}
			}
			
			lineBVOs = PZUtils.processLineInfo(lineBVOs.toArray(new EntLineBVO[lineBVOs.size()]), true);
			//获取发货单的运力信息
			EntTransbilityBVO[] tbBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
					" pk_entrust =? ",entrustVO.getPk_entrust());
			String[] pk_car_type = null;
			if (tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for (int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}
			List<EntPackBVO> packBVOs = new ArrayList<EntPackBVO>();
			for(EntPackBVO packBVO : allpackVOs){
				if(packBVO.getPk_entrust().equals(entrustVO.getPk_entrust())){
					packBVOs.add(packBVO);
				}
			}
			List<PayDetailBVO> detailBVOs = payDetailBComputer(entrustVO, pk_car_type, lineBVOs, packBVOs);
			
			if (detailBVOs != null && detailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for (PayDetailBVO detailBVO : detailBVOs) {
					if(pdVO.getTax_cat() == null && pdVO.getTax_rate() == null){
						pdVO.setTax_cat(detailBVO.getTax_cat());
						pdVO.setTax_rate(detailBVO.getTax_rate());
					}
					detailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(detailBVO);
					detailBVO.setPk_pay_detail(pdVO.getPk_pay_detail()); // 设置主表的主键
				}
			}
			
			for(PayDetailBVO detailBVO : allOldDetailBVOs){
				if(detailBVO.getPk_pay_detail().equals(pdVO.getPk_pay_detail())){
					if(detailBVO.getSystem_create() != null
						&& detailBVO.getSystem_create().equals(UFBoolean.TRUE)){
						detailBVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(detailBVO);
					}else{
						detailBVOs.add(detailBVO);
					}
					
				}
			}
			if (detailBVOs != null && detailBVOs.size() > 0){
				toBeUpdate.addAll(detailBVOs);
			}
			toBeUpdate.add(pdVO);		
			// 重新计算分摊费用
			List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entrustVO, null, null,
					detailBVOs.toArray(new PayDetailBVO[detailBVOs.size()]));
			toBeUpdate.addAll(deviBVOs);
			NWDao.getInstance().saveOrUpdate(toBeUpdate);
			// 更新总金额
			CMUtils.processExtenalforComputer(pdVO, detailBVOs);
			String invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
					+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
					+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
					+ " AND ent.vbillno =? ";
			if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
				invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei ON inv.pk_invoice = ei.pk_invoice "
						+ " LEFT JOIN ts_entrust ent ON ent.pk_entrust = ei.pk_entrust "
						+ " WHERE nvl(ei.dr,0)=0 AND nvl(ent.dr,0)=0 AND nvl(inv.dr,0)=0 "
						+ " AND ent.vbillno =? ";
			}
			List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(invSql, InvoiceVO.class,entrustVO.getVbillno());
			CMUtils.totalCostComput(invoiceVOs);
			results.put(pdVO, detailBVOs);
		}
		return results;
	}
	
	public Map<PayDetailVO, List<PayDetailBVO>> computeLotPayableWithNoPoint(List<String> billIds){
		if(billIds == null || billIds.size() == 0){
			return null;
		}
		PayDetailVO[] detailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(billIds));
		if(detailVOs == null || detailVOs.length == 0){
			throw new BusiException("应付明细不存在！");
		}
		List<String> entrust_vbillnos = new ArrayList<String>();
		Map<String, List<PayDetailVO>> groupMap = new HashMap<String, List<PayDetailVO>>();
		for (PayDetailVO payDetailVO : detailVOs) {
			if(payDetailVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！",payDetailVO.getVbillno());
			}
			String key = new StringBuffer().append(payDetailVO.getLot()).toString();
			List<PayDetailVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<PayDetailVO>();
				groupMap.put(key, voList);
			}
			voList.add(payDetailVO);
			entrust_vbillnos.add(payDetailVO.getEntrust_vbillno());
		}
		String lotCond = NWUtils.buildConditionString(groupMap.keySet().toArray(new String[groupMap.keySet().size()]));
		EntLotVO[] entLotVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLotVO.class, " lot in " + lotCond);
		
		PayDetailBVO[] allOldDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(billIds));
		
		EntrustVO[] allEntrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				"vbillno in " + NWUtils.buildConditionString(entrust_vbillnos));
		
		String packBSql = "select ts_ent_pack_b.* from ts_ent_pack_b WITH(NOLOCK) "
				+ " LEFT JOIN　ts_entrust WITH(NOLOCK) ON ts_entrust.pk_entrust = ts_ent_pack_b.pk_entrust "
				+ " WHERE isnull(ts_ent_pack_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 "
				+ " AND ts_entrust.vbillno in " + NWUtils.buildConditionString(entrust_vbillnos);
		List<EntPackBVO> allpackVOs = NWDao.getInstance().queryForList(packBSql, EntPackBVO.class);
		
		String transBSql = "SELECT ts_ent_transbility_b.* FROM ts_ent_transbility_b WITH(NOLOCK) "
				+ " LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_entrust.pk_entrust = ts_ent_transbility_b.pk_entrust "
				+ " WHERE isnull(ts_ent_transbility_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 "
				+ " AND ts_entrust.vbillno IN " + NWUtils.buildConditionString(entrust_vbillnos);
		List<EntTransbilityBVO> allTransVOs = NWDao.getInstance().queryForList(transBSql, EntTransbilityBVO.class);
		
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
		for (EntLotVO entLotVO : entLotVOs){
			List<EntrustVO> entrustVOs = new ArrayList<EntrustVO>();
			List<EntPackBVO> entPackBVOs = new ArrayList<EntPackBVO>();
			List<PayDetailVO> payDetailVOs = new ArrayList<PayDetailVO>();
			List<String> pk_car_type = new ArrayList<String>();
			for (PayDetailVO payDetailVO : detailVOs){
				if(payDetailVO.getLot().equals(entLotVO.getLot())){
					if (!payDetailVO.getVbillstatus().equals(BillStatus.NEW)){
						throw new BusiException("批次号[?]中应付明细[?]状态有误！",entLotVO.getLot(),payDetailVO.getVbillno());
					}
					payDetailVOs.add(payDetailVO);
				}
				
			}
			for(EntrustVO entVO : allEntrustVOs){
				if(entVO.getLot().equals(entLotVO.getLot())){
					entrustVOs.add(entVO);
				}
			}
			for(EntPackBVO packVO : allpackVOs){
				if(packVO.getPk_entrust().equals(entrustVOs.get(0).getPk_entrust())){
					entPackBVOs.add(packVO);
				}
			}
			for(EntTransbilityBVO transVO : allTransVOs){
				if(transVO.getPk_entrust().equals(entrustVOs.get(0).getPk_entrust())){
					pk_car_type.add(transVO.getPk_car_type());
				}
			}
			List<PayDetailBVO> detailBVOs = payDetailBLotComputerWithNoPoint(entrustVOs, entPackBVOs, payDetailVOs, pk_car_type.toArray(new String[pk_car_type.size()]));
			for(PayDetailVO pdVO : payDetailVOs){
				for(PayDetailBVO detailBVO : allOldDetailBVOs){
					if(detailBVO.getPk_pay_detail().equals(pdVO.getPk_receive_detail())){
						if(detailBVO.getSystem_create() != null
							&& detailBVO.getSystem_create().equals(UFBoolean.TRUE)){
							detailBVO.setStatus(VOStatus.DELETED);
							toBeUpdate.add(detailBVO);
						}else{
							detailBVOs.add(detailBVO);
						}
					}
				}
				
			}
			entLotVO.setStatus(VOStatus.UPDATED);
			entLotVO.setModify_time(new UFDateTime(new Date()));
			entLotVO.setModify_user("system");
			entLotVO.setDef4("Y");
			toBeUpdate.add(entLotVO);
		}
		
		
		
		return null;
	}
	
	/**
	 * 
	 * @param billIds
	 * @return
	 */
	public Map<PayDetailVO, List<PayDetailBVO>> computeLotPayable(List<String> billIds){
		if(billIds == null || billIds.size() == 0){
			return null;
		}
		PayDetailVO[] detailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(billIds));
		if(detailVOs == null || detailVOs.length == 0){
			throw new BusiException("应付明细不存在！");
		}
		List<String> ent_vbillnos = new ArrayList<String>();
		for(PayDetailVO payDetailVO : detailVOs){
			if(payDetailVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应付明细[?]必须是[新建]状态才能进行重算金额！",payDetailVO.getVbillno());
			}
			if(payDetailVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！",payDetailVO.getVbillno());
			}
			if(StringUtils.isBlank(payDetailVO.getEntrust_vbillno())) {
				throw new BusiException("应付明细[?]没有对应的委托单！",payDetailVO.getVbillno());
			}
			ent_vbillnos.add(payDetailVO.getEntrust_vbillno());
		}
		List<String> pk_entrusts = new ArrayList<String>();
		List<String> pk_pay_details = new ArrayList<String>();
		//判断委托单状态
		EntrustVO[] allEntrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				"vbillno in " + NWUtils.buildConditionString(ent_vbillnos));
		for(PayDetailVO payDetailVO : detailVOs){
			boolean flag = true;
			for(EntrustVO entrustVO : allEntrustVOs){
				if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
					pk_entrusts.add(entrustVO.getPk_entrust());
					flag = false;
					break;
				}
			}
			if(flag){
				throw new BusiException("应付明细[?]对应的委托单[?]已经不存在！",payDetailVO.getVbillno(),payDetailVO.getEntrust_vbillno());
			}
			pk_pay_details.add(payDetailVO.getPk_pay_detail());
		}
		EntTransbilityBVO[] transbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
				"pk_entrust in " + NWUtils.buildConditionString(pk_entrusts));
		
		EntPackBVO[] entPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntPackBVO.class,
				"pk_entrust in " + NWUtils.buildConditionString(pk_entrusts));
		
		PayDetailBVO[] allOldPayDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(pk_pay_details));
		
		//根据应付明细计算出运费
		//对PayDetailVO按照批次分组
		Map<String,List<PayDetailVO>> groupMap = new HashMap<String,List<PayDetailVO>>();
		for(PayDetailVO payDetailVO : detailVOs){
			String key = payDetailVO.getLot();
			List<PayDetailVO> voList = groupMap.get(key);
			if(voList  == null){
				voList = new ArrayList<PayDetailVO>();
				groupMap.put(key, voList);
			}
			voList.add(payDetailVO);
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(String key : groupMap.keySet()){
			//获取一个批次下的所有单据信息，包括委托单，应付，应付明细，包装，运力信息
			List<PayDetailVO> payDetailVOs = groupMap.get(key);
			List<EntrustVO> entrustVOs = new ArrayList<EntrustVO>();
			//这里。肯定会获取到委托单
			for(EntrustVO entrustVO : allEntrustVOs){
				if(entrustVO.getLot().equals(key)){
					entrustVOs.add(entrustVO);
				}
			}
			//同一个批次下，所有的单据的运力信息都是一样的，这里获取第一个单据的运力信息即可
			List<String> pk_car_types = new ArrayList<String>();
			for(EntTransbilityBVO transBVO : transbilityBVOs){
				if(transBVO.getPk_entrust().equals(entrustVOs.get(0).getPk_entrust())){
					pk_car_types.add(transBVO.getPk_car_type());
				}
			}
			//获取此批次下，所有的包装明细
			List<EntPackBVO> ent_packBVOs = new ArrayList<EntPackBVO>();
			for(EntrustVO entrustVO : entrustVOs){
				for(EntPackBVO entPackBVO : entPackBVOs){
					if(entPackBVO.getPk_entrust().equals(entrustVO.getPk_entrust())){
						ent_packBVOs.add(entPackBVO);
					}
				}
			}
			//List<PayDetailBVO> detailBVOs = payDetailBComputer(EntrustVO, String[], List<EntLineBVO>, List<EntPackBVO>);
			
			for(PayDetailVO payDetailVO : groupMap.get(key)){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
						List<EntTransbilityBVO> transBVOs = new ArrayList<EntTransbilityBVO>();
						for(EntTransbilityBVO transbilityBVO : transbilityBVOs){
							if(transbilityBVO.getPk_entrust().equals(entrustVO.getPk_entrust())){
								transBVOs.add(transbilityBVO);
							}
						}
						//批次
						List<PayDetailBVO> oldPayDetailBVOs = new ArrayList<PayDetailBVO>();
						for(PayDetailBVO oldPayDetailBVO : allOldPayDetailBVOs){
							if(oldPayDetailBVO.getPk_pay_detail().equals(payDetailVO.getPk_pay_detail())){
								oldPayDetailBVOs.add(oldPayDetailBVO);
							}
						}
						List<EntPackBVO> packBVOs = new ArrayList<EntPackBVO>();
						for(EntPackBVO entPackBVO : entPackBVOs){
							if(entPackBVO.getPk_entrust().equals(entrustVO.getPk_entrust())){
								packBVOs.add(entPackBVO);
							}
						}
						EntTransbilityBVO[] tbBVOs = transBVOs.toArray(new EntTransbilityBVO[transBVOs.size()]);
						List<PayDetailBVO> detailBVOs = EntrustUtils.getPayDetailBVOs(contractService, entrustVO, tbBVOs,groupMap.get(key).size()-1,oldPayDetailBVOs,payDetailVO);
						List<PayDetailBVO> newPayDetailBVOs = new ArrayList<PayDetailBVO>();
						if(detailBVOs != null && detailBVOs.size() > 0){
//							for(PayDetailBVO detailBVO : detailBVOs) {
//								if(isFirst && detailBVO.getPk_expense_type().equals("9436f31e58fc44d1981471b4c2d50e95")
//										&& detailBVO.getStatus() == VOStatus.NEW){
//									//去除第一个点的点位费
//									isFirst = false;
//									continue;
//								}else{
//									newPayDetailBVOs.add(detailBVO);
//									if(detailBVO.getStatus() == VOStatus.NEW) {
//										detailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail()); // 设置主表的主键
//										NWDao.setUuidPrimaryKey(detailBVO);
//									}
//								}
//							}
						}
						toBeUpdate.addAll(newPayDetailBVOs);
						List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entrustVO, null, null,
								newPayDetailBVOs.toArray(new PayDetailBVO[newPayDetailBVOs.size()]));
						toBeUpdate.addAll(deviBVOs);
						NWDao.getInstance().saveOrUpdate(toBeUpdate);
						toBeUpdate.clear();
						CMUtils.processExtenalforComputer(payDetailVO, newPayDetailBVOs);
						//更新应收明细表头成本信息2016-7-4 XIA
						String invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
								+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
								+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
								+ " AND ent.vbillno =? ";
						if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
							invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei ON inv.pk_invoice = ei.pk_invoice "
									+ " LEFT JOIN ts_entrust ent ON ent.pk_entrust = ei.pk_entrust "
									+ " WHERE nvl(ei.dr,0)=0 AND nvl(ent.dr,0)=0 AND nvl(inv.dr,0)=0 "
									+ " AND ent.vbillno =? ";
						}
						List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(invSql, InvoiceVO.class,entrustVO.getVbillno());
						CMUtils.totalCostComput(invoiceVOs);
					}
				}
			}
		}
		
		
		return null;
	}
	
	
	
	private List<PayDetailBVO> payDetailBComputer(EntrustVO entrustVO ,String[] pk_car_type,List<EntLineBVO> lineBVOs,List<EntPackBVO> packBVOs ){
		List<PayDetailBVO> newDetailBVOs = new ArrayList<PayDetailBVO>();
		ContractMatchVO contractMatchVO = new ContractMatchVO();
		contractMatchVO.setContract_type(ContractConst.CARRIER);
		contractMatchVO.setPk_carrierOrBala_customer(entrustVO.getPk_carrier());
		contractMatchVO.setPk_trans_type(entrustVO.getPk_trans_type());
		contractMatchVO.setStart_addr(entrustVO.getPk_delivery());
		contractMatchVO.setEnd_addr(entrustVO.getPk_arrival());
		contractMatchVO.setStart_city(entrustVO.getDeli_city());
		contractMatchVO.setEnd_city(entrustVO.getArri_city());
		contractMatchVO.setPk_corp(entrustVO.getPk_corp());
		contractMatchVO.setReq_arri_date(entrustVO.getReq_deli_date());
		contractMatchVO.setUrgent_level(entrustVO.getUrgent_level());
		contractMatchVO.setItem_code(entrustVO.getItem_code());
		contractMatchVO.setPk_trans_line(entrustVO.getPk_trans_line());
		contractMatchVO.setIf_return(entrustVO.getIf_return());
		List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
		if(contractBVOs != null && contractBVOs.size() > 0) {
			int node_count = 0;
			if(lineBVOs != null) {
				for(EntLineBVO elBVO : lineBVOs) {
					if(elBVO.getStatus() != VOStatus.DELETED) {
						node_count++;
					}
				}
			}
			// 委托单对应的提货点
			int deli_node_count = InvoiceUtils.getDeliNodeCount(entrustVO.getPk_entrust());
			//FIXME 委托单对应的到货点
			int arri_node_count = 0;
			Map<String, UFDouble> retMap = PZUtils.computeFeeWeightCount(entrustVO.getPk_carrier(),
					entrustVO.getPk_trans_type(), entrustVO.getDeli_city(),entrustVO.getArri_city(),
					entrustVO.getVolume_count(), entrustVO.getWeight_count());
			if(retMap == null) {
				// 不需要重新计算
			} else {
				entrustVO.setFee_weight_count(retMap.get("fee_weight_count"));
				entrustVO.setVolume_weight_count(retMap.get("volume_weight_count"));
			}
			
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
			//对包装按照pack进行分组
			for(EntPackBVO packVO : packBVOs){
				String key = packVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<EntPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<EntPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(packVO);
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
			
			PayDetailBMatchVO payDetailBMatchVO = new PayDetailBMatchVO();
			payDetailBMatchVO.setPk_carrier(entrustVO.getPk_carrier());
			payDetailBMatchVO.setNode_count(node_count);
			payDetailBMatchVO.setDeli_node_count(deli_node_count);
			payDetailBMatchVO.setArri_node_count(arri_node_count);
			payDetailBMatchVO.setPack_num_count(entrustVO.getPack_num_count());
			payDetailBMatchVO.setNum_count(entrustVO.getNum_count());
			payDetailBMatchVO.setFee_weight_count(entrustVO.getFee_weight_count());
			payDetailBMatchVO.setWeight_count(entrustVO.getWeight_count());
			payDetailBMatchVO.setVolume_count(entrustVO.getVolume_count());
			payDetailBMatchVO.setPackInfos(packInfos);
			payDetailBMatchVO.setPk_car_types(pk_car_type);
			payDetailBMatchVO.setPk_corp(entrustVO.getPk_corp());
			payDetailBMatchVO.setContractBVOs(contractBVOs);
			newDetailBVOs = contractService.buildPayDetailBVO(payDetailBMatchVO);
		}
		return newDetailBVOs;
	}
	
	//FIXME 这个方法一次只计算一个批次
	private List<PayDetailBVO> payDetailBLotComputerWithNoPoint(List<EntrustVO> entrustVOs,List<EntPackBVO> packBVOs, List<PayDetailVO> payDetailVOs,String[] pk_car_type){
		if(entrustVOs == null || entrustVOs.size() == 0 || payDetailVOs == null || payDetailVOs.size() == 0){
			return null;
		}
		ContractMatchVO contractMatchVO = new ContractMatchVO();
		contractMatchVO.setContract_type(ContractConst.CARRIER);
		contractMatchVO.setPk_carrierOrBala_customer(entrustVOs.get(0).getPk_carrier());
		contractMatchVO.setPk_trans_type(entrustVOs.get(0).getPk_trans_type());
		// 获取最早提货日期和地址  最晚到货日期和地址
		EntrustVO firstEntrustVO = entrustVOs.get(0);
		EntrustVO lastEntrustVO = entrustVOs.get(0);
		for(EntrustVO entVO : entrustVOs){
			if((new UFDateTime(entVO.getReq_deli_date())).before(new UFDateTime(firstEntrustVO.getReq_deli_date()))){
				firstEntrustVO = entVO;
			}
			if((new UFDateTime(entVO.getReq_arri_date())).after(new UFDateTime(firstEntrustVO.getReq_arri_date()))){
				lastEntrustVO = entVO;
			}
		}
		contractMatchVO.setStart_addr(firstEntrustVO.getPk_delivery());
		contractMatchVO.setStart_city(firstEntrustVO.getDeli_city());
		contractMatchVO.setEnd_addr(lastEntrustVO.getPk_delivery());
		contractMatchVO.setEnd_city(lastEntrustVO.getDeli_city());
		contractMatchVO.setReq_arri_date(lastEntrustVO.getReq_deli_date());
		contractMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
		contractMatchVO.setUrgent_level(entrustVOs.get(0).getUrgent_level());
		contractMatchVO.setItem_code(entrustVOs.get(0).getItem_code());
		contractMatchVO.setPk_trans_line(entrustVOs.get(0).getPk_trans_line());
		contractMatchVO.setIf_return(entrustVOs.get(0).getIf_return());
		
		List<PackInfo> packInfos = new ArrayList<PackInfo>();
		if(packBVOs != null && packBVOs.size() > 0){
			Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
			//对包装按照pack进行分组
			for(EntPackBVO packVO : packBVOs){
				String key = packVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<EntPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<EntPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(packVO);
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
		List<PayDetailBVO> detailBVOs = null; 
		if (entrustVOs.get(0).getPk_trans_type().equals(MILKRUN_TRANS_TYPE)){
			//计算milkrun的应付
		}else{
			// 匹配合同
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);

			PayDetailBMatchVO payDetailBMatchVO = new PayDetailBMatchVO();
			payDetailBMatchVO.setPk_carrier(entrustVOs.get(0).getPk_carrier());
			for(EntrustVO entrustVO : entrustVOs){
				payDetailBMatchVO.setPack_num_count((entrustVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : entrustVO.getPack_num_count()).add(payDetailBMatchVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getPack_num_count()));
				payDetailBMatchVO.setNum_count((entrustVO.getNum_count() == null ? 0 : entrustVO.getNum_count()) + (payDetailBMatchVO.getNum_count()== null ? 0 : payDetailBMatchVO.getNum_count()));
				payDetailBMatchVO.setFee_weight_count((entrustVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : entrustVO.getFee_weight_count()).add(payDetailBMatchVO.getFee_weight_count()== null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getFee_weight_count()));
				payDetailBMatchVO.setWeight_count((entrustVO.getWeight_count() == null ? UFDouble.ZERO_DBL : entrustVO.getWeight_count()).add(payDetailBMatchVO.getWeight_count() == null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getWeight_count()));
				payDetailBMatchVO.setVolume_count((entrustVO.getVolume_count() == null ? UFDouble.ZERO_DBL : entrustVO.getVolume_count()).add(payDetailBMatchVO.getVolume_count() == null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getVolume_count()));
			}
			payDetailBMatchVO.setPackInfos(packInfos);
			payDetailBMatchVO.setPk_car_types(pk_car_type);
			payDetailBMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
			payDetailBMatchVO.setContractBVOs(contractBVOs); 
			
			// 重新计算金额
			detailBVOs = contractService.buildPayDetailBVO(payDetailBMatchVO);
		}
		return detailBVOs;
	}
	
	
	public List<Map<String, Object>> RTComputeLotPayable(List<String> billIds){
		if(billIds == null || billIds.size() == 0) {
			return null;
		}
		
		String unitSql = "SELECT ts_pay_detail.*  "
				+ "FROM ts_pay_detail WITH (NOLOCK) "
				+ "LEFT JOIN ts_entrust  WITH (NOLOCK) ON ts_pay_detail.lot = ts_entrust.lot "
				+ "LEFT JOIN ts_pay_detail tpd2 WITH (NOLOCK) ON ts_entrust.vbillno = tpd2.entrust_vbillno "
				+ "WHERE isnull(ts_pay_detail.dr,0)=0 AND ts_pay_detail.pay_type=0 AND isnull(ts_entrust.dr,0)=0 AND isnull(tpd2.dr,0)=0  "
				+ "AND ts_entrust.vbillstatus <> 24 ";
		String cond = NWUtils.buildConditionString(billIds);
		String sql = unitSql + "AND tpd2.pk_pay_detail in " + cond;
		List<PayDetailVO> payDetailVOs = NWDao.getInstance().queryForList(sql, PayDetailVO.class);
		if(payDetailVOs == null || payDetailVOs.size() == 0){
			throw new BusiException("请选择单据！");
		}
		List<String> ent_vbillnos = new ArrayList<String>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			if(payDetailVO.getVbillstatus() != BillStatus.NEW) {
				throw new BusiException("应付明细[?]必须是[新建]状态才能进行重算金额！",payDetailVO.getVbillno());
			}
			if(payDetailVO.getPay_type().intValue() != PayDetailConst.ORIGIN_TYPE) {
				throw new BusiException("应付明细[?]必须是[原始单据]类型才能进行重算金额！", payDetailVO.getVbillno());
			}
			if(StringUtils.isBlank(payDetailVO.getEntrust_vbillno())) {
				throw new BusiException("应付明细[?]没有对应的委托单！",payDetailVO.getVbillno());
			}
			ent_vbillnos.add(payDetailVO.getEntrust_vbillno());
		}
		List<String> pk_entrusts = new ArrayList<String>();
		List<String> pk_pay_details = new ArrayList<String>();
		//判断委托单状态
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,
				"vbillno in " + NWUtils.buildConditionString(ent_vbillnos));
		for(PayDetailVO payDetailVO : payDetailVOs){
			boolean flag = true;
			for(EntrustVO entrustVO : entrustVOs){
				if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
					pk_entrusts.add(entrustVO.getPk_entrust());
					flag = false;
					break;
				}
			}
			if(flag){
				throw new BusiException("应付明细[?]对应的委托单[?]已经不存在！",payDetailVO.getVbillno(),payDetailVO.getEntrust_vbillno());
			}
			pk_pay_details.add(payDetailVO.getPk_pay_detail());
		}
		EntTransbilityBVO[] transbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
				"pk_entrust in " + NWUtils.buildConditionString(pk_entrusts));
		
		PayDetailBVO[] allOldPayDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				"pk_pay_detail in " + NWUtils.buildConditionString(pk_pay_details));
		
		//根据应付明细计算出运费
		//对PayDetailVO按照批次分组
		Map<String,List<PayDetailVO>> groupMap = new HashMap<String,List<PayDetailVO>>();
		for(PayDetailVO payDetailVO : payDetailVOs){
			String key = payDetailVO.getLot();
			List<PayDetailVO> voList = groupMap.get(key);
			if(voList  == null){
				voList = new ArrayList<PayDetailVO>();
				groupMap.put(key, voList);
			}
			voList.add(payDetailVO);
		}
		List<SuperVO> ToBeUpdate = new ArrayList<SuperVO>();
		for(String key : groupMap.keySet()){
			boolean isFirst = true;
			for(PayDetailVO payDetailVO : groupMap.get(key)){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
						List<EntTransbilityBVO> transBVOs = new ArrayList<EntTransbilityBVO>();
						for(EntTransbilityBVO transbilityBVO : transbilityBVOs){
							if(transbilityBVO.getPk_entrust().equals(transbilityBVO.getPk_entrust())){
								transBVOs.add(transbilityBVO);
							}
						}
						List<PayDetailBVO> oldPayDetailBVOs = new ArrayList<PayDetailBVO>();
						for(PayDetailBVO oldPayDetailBVO : allOldPayDetailBVOs){
							if(oldPayDetailBVO.getPk_pay_detail().equals(payDetailVO.getPk_pay_detail())){
								oldPayDetailBVOs.add(oldPayDetailBVO);
							}
						}
						EntTransbilityBVO[] tbBVOs = transBVOs.toArray(new EntTransbilityBVO[transBVOs.size()]);
						List<PayDetailBVO> detailBVOs = EntrustUtils.getPayDetailBVOs(contractService, entrustVO, tbBVOs,groupMap.get(key).size()-1,oldPayDetailBVOs,payDetailVO);
						List<PayDetailBVO> newPayDetailBVOs = new ArrayList<PayDetailBVO>();
						if(detailBVOs != null && detailBVOs.size() > 0){
							for(PayDetailBVO detailBVO : detailBVOs) {
								if(isFirst && detailBVO.getPk_expense_type().equals("9436f31e58fc44d1981471b4c2d50e95")
										&& detailBVO.getStatus() == VOStatus.NEW){
									//去除第一个点的点位费
									isFirst = false;
									continue;
								}else{
									newPayDetailBVOs.add(detailBVO);
									if(detailBVO.getStatus() == VOStatus.NEW) {
										detailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail()); // 设置主表的主键
										NWDao.setUuidPrimaryKey(detailBVO);
									}
								}
							}
						}
						ToBeUpdate.addAll(newPayDetailBVOs);
						List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entrustVO, null, null,
								newPayDetailBVOs.toArray(new PayDetailBVO[newPayDetailBVOs.size()]));
						ToBeUpdate.addAll(deviBVOs);
						NWDao.getInstance().saveOrUpdate(ToBeUpdate);
						ToBeUpdate.clear();
						CMUtils.processExtenalforComputer(payDetailVO, newPayDetailBVOs);
						//更新应收明细表头成本信息2016-7-4 XIA
						String invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
								+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
								+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
								+ " AND ent.vbillno =? ";
						if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
							invSql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei ON inv.pk_invoice = ei.pk_invoice "
									+ " LEFT JOIN ts_entrust ent ON ent.pk_entrust = ei.pk_entrust "
									+ " WHERE nvl(ei.dr,0)=0 AND nvl(ent.dr,0)=0 AND nvl(inv.dr,0)=0 "
									+ " AND ent.vbillno =? ";
						}
						List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(invSql, InvoiceVO.class,entrustVO.getVbillno());
						CMUtils.totalCostComput(invoiceVOs);
						
						
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 计算一个批次下的应付明细，这里只返回某个应付下的明细，因为同一个批次下的
	 * @param entrustVOs
	 * @param payDetailVOs
	 * @param packBVOs
	 * @param pk_car_type
	 * @return
	 */
	private List<PayDetailBVO> payDetailBLotComputer(List<EntrustVO> entrustVOs, List<PayDetailVO> payDetailVOs,List<EntPackBVO> packBVOs,String[] pk_car_type){
		if(entrustVOs == null || entrustVOs.size() == 0 || payDetailVOs == null || payDetailVOs.size() == 0){
			return null;
		}
		ContractMatchVO contractMatchVO = new ContractMatchVO();
		contractMatchVO.setContract_type(ContractConst.CARRIER);
		contractMatchVO.setPk_carrierOrBala_customer(entrustVOs.get(0).getPk_carrier());
		contractMatchVO.setPk_trans_type(entrustVOs.get(0).getPk_trans_type());
		// 获取最早提货日期和地址  最晚到货日期和地址
		EntrustVO firstEntrustVO = entrustVOs.get(0);
		EntrustVO lastEntrustVO = entrustVOs.get(0);
		for(EntrustVO entVO : entrustVOs){
			if((new UFDateTime(entVO.getReq_deli_date())).before(new UFDateTime(firstEntrustVO.getReq_deli_date()))){
				firstEntrustVO = entVO;
			}
			if((new UFDateTime(entVO.getReq_arri_date())).after(new UFDateTime(firstEntrustVO.getReq_arri_date()))){
				lastEntrustVO = entVO;
			}
		}
		contractMatchVO.setStart_addr(firstEntrustVO.getPk_delivery());
		contractMatchVO.setStart_city(firstEntrustVO.getDeli_city());
		contractMatchVO.setEnd_addr(lastEntrustVO.getPk_delivery());
		contractMatchVO.setEnd_city(lastEntrustVO.getDeli_city());
		contractMatchVO.setReq_arri_date(lastEntrustVO.getReq_deli_date());
		contractMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
		contractMatchVO.setUrgent_level(entrustVOs.get(0).getUrgent_level());
		contractMatchVO.setItem_code(entrustVOs.get(0).getItem_code());
		contractMatchVO.setPk_trans_line(entrustVOs.get(0).getPk_trans_line());
		contractMatchVO.setIf_return(entrustVOs.get(0).getIf_return());
		List<PackInfo> packInfos = new ArrayList<PackInfo>();
		if(packBVOs != null && packBVOs.size() > 0){
			Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
			//对包装按照pack进行分组
			for(EntPackBVO packVO : packBVOs){
				String key = packVO.getPack();
				if(StringUtils.isBlank(key)){
					//没有包装的货品自动过滤
					continue;
				}
				List<EntPackBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<EntPackBVO>();
					groupMap.put(key, voList);
				}
				voList.add(packVO);
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
		List<PayDetailBVO> detailBVOs = null; 
		// 匹配合同
		List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);

		PayDetailBMatchVO payDetailBMatchVO = new PayDetailBMatchVO();
		payDetailBMatchVO.setPk_carrier(entrustVOs.get(0).getPk_carrier());
		for(EntrustVO entrustVO : entrustVOs){
			payDetailBMatchVO.setPack_num_count((entrustVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : entrustVO.getPack_num_count()).add(payDetailBMatchVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getPack_num_count()));
			payDetailBMatchVO.setNum_count((entrustVO.getNum_count() == null ? 0 : entrustVO.getNum_count()) + (payDetailBMatchVO.getNum_count()== null ? 0 : payDetailBMatchVO.getNum_count()));
			payDetailBMatchVO.setFee_weight_count((entrustVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : entrustVO.getFee_weight_count()).add(payDetailBMatchVO.getFee_weight_count()== null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getFee_weight_count()));
			payDetailBMatchVO.setWeight_count((entrustVO.getWeight_count() == null ? UFDouble.ZERO_DBL : entrustVO.getWeight_count()).add(payDetailBMatchVO.getWeight_count() == null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getWeight_count()));
			payDetailBMatchVO.setVolume_count((entrustVO.getVolume_count() == null ? UFDouble.ZERO_DBL : entrustVO.getVolume_count()).add(payDetailBMatchVO.getVolume_count() == null ? UFDouble.ZERO_DBL : payDetailBMatchVO.getVolume_count()));
		}
		payDetailBMatchVO.setPackInfos(packInfos);
		payDetailBMatchVO.setPk_car_types(pk_car_type);
		payDetailBMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
		payDetailBMatchVO.setContractBVOs(contractBVOs); 
						
			// 重新计算金额
		detailBVOs = contractService.buildPayDetailBVO(payDetailBMatchVO);
		return detailBVOs;
	}


	private List<PayDetailBVO> milkRunPayComputer(List<EntrustVO> entrustVOs,List<EntPackBVO> packBVOs, List<PayDetailVO> payDetailVOs,String[] pk_car_type){
		List<String> pk_entrusts = new ArrayList<String>();
		// 如果运输方式为milkrun，只会存在一个批次号，这些所有的委托单都属于同一批次。
		// 对于milkrun运输，多数情况也只会有一个委托单
		for(EntrustVO entVO : entrustVOs ){
			pk_entrusts.add(entVO.getPk_entrust());
		}
		String entCond = NWUtils.buildConditionString(pk_entrusts);
		// 获取所有的线路信息
		String accessRule = ParameterHelper.getMilkRunNodeAccessRule();
		String[] accessRules = accessRule.split("\\" + Constants.SPLIT_CHAR);
		String accessRulesCond = NWUtils.buildConditionString(accessRules);
		String entLineBSql = "select * from ts_ent_line_b with(nolock) where isnull(dr,0)=0 and pk_entrust in"+ entCond +" and operate_type in "+accessRulesCond;
		List<EntLineBVO> listLineBVOs = NWDao.getInstance().queryForList(entLineBSql,EntLineBVO.class);
		EntLineBVO[] lineBVOs = listLineBVOs.toArray(new EntLineBVO[listLineBVOs.size()]);
		// 因为会存在多个委托单的情况，所以要对线路按提货时间进行排序 冒泡排序
		//这里分组时，对于时间完全相同的，并没有处理，但是系统生成线路信息时，已经进行了一定的排序。
		EntLineBVO TempLineBVO = new EntLineBVO();
		for (int i = 0; i < lineBVOs.length - 1; i++) {
			for (int j = 0; j < lineBVOs.length - 1 - i; j++) {
				if (new UFDateTime(lineBVOs[j].getReq_arri_date())
						.before(new UFDateTime(lineBVOs[j + 1].getReq_arri_date()))){
					TempLineBVO = lineBVOs[j];
					lineBVOs[j] = lineBVOs[j + 1];
					lineBVOs[j + 1] = TempLineBVO;
				}
			}
		}
		
		// 排序完毕
		// 对线路信息进行分类
		List<EntLineBVO> points = new ArrayList<EntLineBVO>();
		List<EntLineBVO> bases = new ArrayList<EntLineBVO>();
		for (int i = 0; i < lineBVOs.length; i++) {
			if (i == 0) {
				// 第一个点必定是基点
				bases.add(lineBVOs[i]);
			} else {
				if (lineBVOs[i].getPk_address().equals(lineBVOs[i - 1].getPk_address())) {
					continue;
				} else {
					if (lineBVOs[i].getPk_city().equals(lineBVOs[i - 1].getPk_city())) {
						points.add(lineBVOs[i]);
					} else {
						bases.add(lineBVOs[i]);
					}
				}
			}
		}

		Map<String, List<EntLineBVO>> pointGroupMap = new HashMap<String, List<EntLineBVO>>();
		for (EntLineBVO entLineBVO : points) {
			String pointKey = entLineBVO.getPk_city();
			List<EntLineBVO> voList = pointGroupMap.get(pointKey);
			if (voList == null) {
				voList = new ArrayList<EntLineBVO>();
				pointGroupMap.put(pointKey, voList);
			}
			voList.add(entLineBVO);
		}
		List<PayDetailBVO> payDetailBVOs = new ArrayList<PayDetailBVO>();
		for (EntLineBVO entLineBVO : bases) {
			ContractMatchVO contractMatchVO = new ContractMatchVO();
			contractMatchVO.setContract_type(ContractConst.CARRIER);
			contractMatchVO.setPk_carrierOrBala_customer(entrustVOs.get(0).getPk_carrier());
			contractMatchVO.setPk_trans_type(entrustVOs.get(0).getPk_trans_type());
			contractMatchVO.setStart_addr(entLineBVO.getPk_address());
			contractMatchVO.setStart_city(entLineBVO.getPk_city());
			contractMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
			contractMatchVO.setReq_arri_date(entLineBVO.getReq_arri_date());
			contractMatchVO.setUrgent_level(entrustVOs.get(0).getUrgent_level());
			contractMatchVO.setItem_code(entrustVOs.get(0).getItem_code());
			contractMatchVO.setPk_trans_line(entrustVOs.get(0).getPk_trans_line());
			contractMatchVO.setIf_return(entrustVOs.get(0).getIf_return());
			
			// 只有一个点说明这个应该是基费 开始匹配合同
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
			if (contractBVOs != null && contractBVOs.size() > 0) {
				for (ContractBVO contractBVO : contractBVOs) {
					// 只有结果为基费的合同才是我们需要的,并且合同设备类型需要相符
					if (contractBVO.getPk_expense_type().equals(ET110_PK)
							&& pk_car_type[0].equals(contractBVO.getEquip_type())) {
						List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
						// 只会存在一个合同
						unitContractBVO.add(contractBVO);
						//获取entPackB 
						EntLinePackBVO[] linePackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_ent_line_b=?", entLineBVO.getPk_ent_line_b());
						PayDetailBMatchVO payDetailBMatchVO = new PayDetailBMatchVO();
						payDetailBMatchVO.setPk_carrier(entrustVOs.get(0).getPk_carrier());
						payDetailBMatchVO.setPk_car_types(pk_car_type);
						payDetailBMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
						List<PackInfo> packInfos = new ArrayList<PackInfo>();
						UFDouble pack_num_count = UFDouble.ZERO_DBL;
						Integer num_count = 0;
						UFDouble weight_count = UFDouble.ZERO_DBL;
						UFDouble volume_count = UFDouble.ZERO_DBL;
						if(linePackBVOs != null && linePackBVOs.length > 0){
							Map<String,List<EntLinePackBVO>> groupMap = new  HashMap<String,List<EntLinePackBVO>>();
							//对包装按照pack进行分组
							for(EntLinePackBVO packVO : linePackBVOs){
								String key = packVO.getPack();
								if(StringUtils.isBlank(key)){
									//没有包装的货品自动过滤
									continue;
								}
								List<EntLinePackBVO> voList = groupMap.get(key);
								if(voList == null){
									voList = new ArrayList<EntLinePackBVO>();
									groupMap.put(key, voList);
								}
								voList.add(packVO);
							}
							if(groupMap.size() > 0) {
								for(String key : groupMap.keySet()){
									PackInfo packInfo = new PackInfo();
									List<EntLinePackBVO> voList = groupMap.get(key);
									Integer num = 0;
									UFDouble weight = UFDouble.ZERO_DBL;
									UFDouble volume = UFDouble.ZERO_DBL;
									for(EntLinePackBVO packBVO : voList){
										num = num + (packBVO.getNum() == null ? 0 : packBVO.getNum());
										weight = weight.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
										volume = volume.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
										
										pack_num_count = pack_num_count.add(packBVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : packBVO.getPack_num_count());
										num_count = num_count + (packBVO.getNum() == null ? 0 : packBVO.getNum());;
										weight_count = weight_count.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
										volume_count = volume_count.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
										
									}
									packInfo.setPack(key);
									packInfo.setNum(num);
									packInfo.setWeight(weight);
									packInfo.setVolume(volume);
									packInfos.add(packInfo);
								}
							}
							payDetailBMatchVO.setPackInfos(packInfos);
							payDetailBMatchVO.setPack_num_count(pack_num_count);
							payDetailBMatchVO.setNum_count(num_count);
							UFDouble rate = carrService.getFeeRate(entrustVOs.get(0).getPk_carrier(), entrustVOs.get(0).getPk_trans_type(), entrustVOs.get(0).getDeli_city(), entrustVOs.get(0).getArri_city());
							//payDetailBMatchVO.setFee_weight_count(carrService.getFeeWeightCount(entrustVOs.get(0).getPk_carrier(), entrustVOs.get(0).getPk_trans_type(), weight_count, volume_count));
							UFDouble volume_weight_count = rate.multiply(volume_count == null ? UFDouble.ZERO_DBL : volume_count);
							if(volume_weight_count.sub(weight_count == null ? UFDouble.ZERO_DBL : weight_count).doubleValue() > 0 ){
								payDetailBMatchVO.setFee_weight_count(volume_weight_count);
							}else{
								payDetailBMatchVO.setFee_weight_count(weight_count);
							}
							payDetailBMatchVO.setWeight_count(weight_count);
							payDetailBMatchVO.setVolume_count(volume_count);
							payDetailBMatchVO.setContractBVOs(unitContractBVO);
						}
						
						// 重新计算金额
						List<PayDetailBVO> 	unitpdbVOs = contractService.buildPayDetailBVO(payDetailBMatchVO);
						if(unitpdbVOs == null || unitpdbVOs.size() == 0){
							continue;
						}
						unitpdbVOs.get(0).setPrice(unitpdbVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL
								: unitpdbVOs.get(0).getAmount());
						payDetailBVOs.addAll(unitpdbVOs);
					}
				}
			}
		}
		for (String pointKey : pointGroupMap.keySet()) {
			ContractMatchVO contractMatchVO = new ContractMatchVO();
			contractMatchVO.setContract_type(ContractConst.CARRIER);
			contractMatchVO.setPk_carrierOrBala_customer(entrustVOs.get(0).getPk_carrier());
			contractMatchVO.setPk_trans_type(entrustVOs.get(0).getPk_trans_type());
			contractMatchVO.setStart_addr(pointGroupMap.get(pointKey).get(0).getPk_address());
			contractMatchVO.setStart_city(pointGroupMap.get(pointKey).get(0).getPk_city());
			contractMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
			contractMatchVO.setReq_arri_date(pointGroupMap.get(pointKey).get(0).getReq_arri_date());
			contractMatchVO.setUrgent_level(entrustVOs.get(0).getUrgent_level());
			contractMatchVO.setItem_code(entrustVOs.get(0).getItem_code());
			contractMatchVO.setPk_trans_line(entrustVOs.get(0).getPk_trans_line());
			contractMatchVO.setIf_return(entrustVOs.get(0).getIf_return());
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
			
			if (contractBVOs != null && contractBVOs.size() > 0) {
				for (ContractBVO contractBVO : contractBVOs) {
					// 只有结果为点费的合同才是我们需要的,并且合同设备类型需要相符
					//点位费只按照点位进行匹配费用，不需要关注件重体信息
					if (contractBVO.getPk_expense_type().equals(ET120_PK)
							&& pk_car_type[0].equals(contractBVO.getEquip_type())) {
						// FIXME  点位费的算法都是不一样的，这里都按照到货点点位计算 具体问题需要具体分析。
						List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
						// 只会存在一个合同
						unitContractBVO.add(contractBVO);
						PayDetailBMatchVO payDetailBMatchVO = new PayDetailBMatchVO();
						payDetailBMatchVO.setPk_carrier(entrustVOs.get(0).getPk_carrier());
						payDetailBMatchVO.setPk_car_types(pk_car_type);
						payDetailBMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
						payDetailBMatchVO.setDeli_node_count(pointGroupMap.get(pointKey).size()-1);
						payDetailBMatchVO.setContractBVOs(unitContractBVO);
						// 重新计算金额
						List<PayDetailBVO> 	unitpdbVOs = contractService.buildPayDetailBVO(payDetailBMatchVO);
						if(unitpdbVOs == null || unitpdbVOs.size() == 0){
							continue;
						}
						unitpdbVOs.get(0).setPrice(unitpdbVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL
								: unitpdbVOs.get(0).getAmount());
						unitpdbVOs.get(0).setAmount(unitpdbVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL
								: unitpdbVOs.get(0).getAmount().multiply(pointGroupMap.get(pointKey).size()));
						unitpdbVOs.get(0).setContract_amount(unitpdbVOs.get(0).getAmount());
						payDetailBVOs.addAll(unitpdbVOs);
					}
				}
			}
		}
		return payDetailBVOs;
	}
	
}
