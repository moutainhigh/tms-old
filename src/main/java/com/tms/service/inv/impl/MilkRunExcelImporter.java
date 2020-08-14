package com.tms.service.inv.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IBillService;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;
import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.OperateTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.CustBalaVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;

/**
 * MilkRun导入
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class MilkRunExcelImporter extends BillExcelImporter {
	
	private ContractService contractService = null;
	
	public MilkRunExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new InvoiceVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolveForMultiTable(file);
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		Map<String, List<AggregatedValueObject>> groupMap = new HashMap<String, List<AggregatedValueObject>>();
		for (AggregatedValueObject aggVO : aggVOs) {
			InvoiceVO invoiceVO = (InvoiceVO)aggVO.getParentVO();
			String key = new StringBuffer().append(invoiceVO.getOrderno()).toString();
			List<AggregatedValueObject> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<AggregatedValueObject>();
				groupMap.put(key, voList);
			}
			voList.add(aggVO);
		}
		logger.info("共分成" + groupMap.size() + "组");
		//将导入时保存的信息转换成pk值
		List<String> pk_customers = new ArrayList<String>();
		List<String> pk_carriers = new ArrayList<String>();
		List<String> pk_car_types = new ArrayList<String>();
		List<String> pk_psndocs = new ArrayList<String>();
		List<String> pk_trans_types = new ArrayList<String>();
		List<String> pk_addressS = new ArrayList<String>();
		
		List<InvoiceVO> allInvoiceVOs = new ArrayList<InvoiceVO>();
		List<InvLineBVO> allInvLineBVOs = new ArrayList<InvLineBVO>();
		List<TransBilityBVO> allTransBilityBVOs = new ArrayList<TransBilityBVO>();
		List<InvPackBVO> allInvPackBVOs = new ArrayList<InvPackBVO>();
		
		for(String key : groupMap.keySet()){
			List<AggregatedValueObject> aggvos = groupMap.get(key);
			if (aggvos == null || aggvos.size() == 0) {
				continue;
			}
			
			List<InvLineBVO> invLineBVOs = new ArrayList<InvLineBVO>();
			List<TransBilityBVO> transBilityBVOs = new ArrayList<TransBilityBVO>();
			InvoiceVO invoiceVO = (InvoiceVO)aggvos.get(0).getParentVO();
			ExAggInvoiceVO exAggInvoiceVO = (ExAggInvoiceVO) aggvos.get(0);
			
			//检查订单号,客户订单号唯一性
			boolean orderno_must_unique = ParameterHelper.getBooleanParam("orderno_must_unique");
			if(orderno_must_unique){
				if(StringUtils.isNotBlank(invoiceVO.getOrderno())){
					// 必须唯一
					String sql = "select count(1) from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and orderno=? and pk_corp=?";
					Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, invoiceVO.getOrderno(),
							WebUtils.getLoginInfo().getPk_corp());
					if(count > 0) {
						throw new BusiException("订单号[?]已存在！",invoiceVO.getOrderno());
					}
				}
			}
			
			boolean cust_orderno_must_unique = ParameterHelper.getBooleanParam("cust_orderno_must_unique");
			if(cust_orderno_must_unique){
				if(StringUtils.isNotBlank(invoiceVO.getCust_orderno())){
					// 必须唯一
					String sql = "select count(1) from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and cust_orderno=? and pk_corp=?";
					Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, invoiceVO.getCust_orderno(),
							WebUtils.getLoginInfo().getPk_corp());
					if(count > 0) {
						throw new BusiException("客户订单号[?]已存在！",invoiceVO.getCust_orderno());
					}
				}
			}
			
			
			
			for (AggregatedValueObject aggVO : aggvos) {
				ExAggInvoiceVO tempExAggInvoiceVO = (ExAggInvoiceVO) aggVO;
				InvLineBVO[] invLineBVOArrs = (InvLineBVO[])tempExAggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
				invLineBVOs.addAll(Arrays.asList(invLineBVOArrs));
			
			}
			if(invLineBVOs != null && invLineBVOs.size() > 0){
				invLineBVOs = Arrays.asList(bubble_sort(invLineBVOs.toArray(new InvLineBVO[invLineBVOs.size()]), invLineBVOs.size()));
			}
			//往表头中添加相关信息
			pk_customers.add(invoiceVO.getPk_customer());
			pk_carriers.add(invoiceVO.getPk_carrier());
			pk_psndocs.add(invoiceVO.getPk_psndoc());
			pk_trans_types.add(invoiceVO.getPk_trans_type());
			CustomerVO customerVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "pk_customer=?", invoiceVO.getPk_customer());
			invoiceVO.setBilling_corp(customerVO.getBilling_corp());
			invoiceVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.MKDR.intValue());
			invoiceVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			invoiceVO.setVbillstatus(BillStatus.NEW);
			invoiceVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			invoiceVO.setCreate_time(new UFDateTime(new Date()));
			invoiceVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.FHD)); // 生成发货单的单据号
			invoiceVO.setReq_arri_date(invLineBVOs.get(invLineBVOs.size()-1).getReq_date_from().toString());
			invoiceVO.setReq_arri_time(invLineBVOs.get(invLineBVOs.size()-1).getReq_date_till().toString());
			invoiceVO.setReq_deli_date(invLineBVOs.get(0).getReq_date_from().toString());
			invoiceVO.setReq_deli_time(invLineBVOs.get(0).getReq_date_till().toString());
			invoiceVO.setPk_delivery(invLineBVOs.get(0).getPk_address().toString());
			invoiceVO.setPk_arrival(invLineBVOs.get(invLineBVOs.size()-1).getPk_address().toString());
			allInvoiceVOs.add(invoiceVO);
			VOs.add(invoiceVO);
			//生成InvLineBVO的序列号
			int index = 0;
			for(InvLineBVO invLineBVO : invLineBVOs){
				if(invLineBVO.getSerialno() == null) {
					index += 10;
					invLineBVO.setSerialno(index);
				}
				pk_addressS.add(invLineBVO.getPk_address());
				invLineBVO.setPk_invoice(invoiceVO.getPrimaryKey());
				allInvLineBVOs.add(invLineBVO);
			}
			
			TransBilityBVO[] transBilityBVOArrs = (TransBilityBVO[])(exAggInvoiceVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B));
			transBilityBVOs.addAll(Arrays.asList(transBilityBVOArrs));
			VOs.addAll(transBilityBVOs);
			
			for(TransBilityBVO transBilityBVO : transBilityBVOs){
				allTransBilityBVOs.add(transBilityBVO);
				pk_car_types.add(transBilityBVO.getPk_car_type());
				transBilityBVO.setPk_invoice(invoiceVO.getPrimaryKey());
			}
		}
		//将数据转换成PK
		
		String sql = "select * from ts_cust_bala WITH(NOLOCK) where isnull(locked_flag,'N')='N' and isnull(dr,0)=0 and is_default='Y' and pk_customer in " + NWUtils.buildConditionString(pk_customers.toArray(new String[pk_customers.size()]));
		List<CustBalaVO> custbalaVOs = NWDao.getInstance().queryForList(sql, CustBalaVO.class);
		
		String addressCond = NWUtils.buildConditionString(pk_addressS.toArray(new String[pk_addressS.size()]));
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class,
					" pk_address in " + addressCond);
		
		CarTypeVO[] carTypeVOs = null;
		if(pk_car_types != null && pk_car_types.size() > 0){
			String cartypesCond = NWUtils.buildConditionString(pk_car_types.toArray(new String[pk_car_types.size()]));
			carTypeVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarTypeVO.class,
						" code in " + cartypesCond);
			}
		
		for (InvoiceVO invoiceVO : allInvoiceVOs) {
			if (custbalaVOs != null && custbalaVOs.size() > 0) {
				for (CustBalaVO custBalaVO : custbalaVOs) {
					if (invoiceVO.getPk_customer().equals(custBalaVO.getPk_customer())) {
						invoiceVO.setBala_customer(custBalaVO.getPk_related_cust());
						break;
					}
				}
			}

			if (addressVOs != null && addressVOs.length > 0) {
				Boolean deli_flag = false;
				Boolean arri_flag = false;
				for (AddressVO addressVO : addressVOs) {
					if (invoiceVO.getPk_delivery().equals(addressVO.getPk_address())) {
						invoiceVO.setPk_delivery(addressVO.getPk_address());
						invoiceVO.setDeli_area(addressVO.getPk_area());
						invoiceVO.setDeli_province(addressVO.getPk_province());
						invoiceVO.setDeli_city(addressVO.getPk_city());
						invoiceVO.setDeli_contact(addressVO.getContact());
						invoiceVO.setDeli_mobile(addressVO.getMobile());
						invoiceVO.setDeli_phone(addressVO.getPhone());
						invoiceVO.setDeli_email(addressVO.getEmail());
						invoiceVO.setDeli_detail_addr(addressVO.getDetail_addr());
						deli_flag = true;
					}
					if (invoiceVO.getPk_arrival().equals(addressVO.getPk_address())) {
						invoiceVO.setPk_arrival(addressVO.getPk_address());
						invoiceVO.setArri_area(addressVO.getPk_area());
						invoiceVO.setArri_province(addressVO.getPk_province());
						invoiceVO.setArri_city(addressVO.getPk_city());
						invoiceVO.setArri_contact(addressVO.getContact());
						invoiceVO.setArri_mobile(addressVO.getMobile());
						invoiceVO.setArri_phone(addressVO.getPhone());
						invoiceVO.setArri_email(addressVO.getEmail());
						invoiceVO.setArri_detail_addr(addressVO.getDetail_addr());
						arri_flag = true;
					}
				}
				if (!deli_flag) {
					throw new BusiException("起始地址 [?]没有维护！",invoiceVO.getPk_delivery());
				}
				if (!arri_flag) {
					throw new BusiException("目的地址 [?]没有维护！",invoiceVO.getPk_arrival());
				}
			} else {
				throw new BusiException("地址 [?]没有维护！",invoiceVO.getPk_delivery());
			}
		}
		
		//对allInvLineBVOs分组
		Map<String, List<InvLineBVO>> groupMapInvLineBVOs = new HashMap<String, List<InvLineBVO>>();
		for (InvLineBVO invLineBVO : allInvLineBVOs) {
			String key = new StringBuffer().append(invLineBVO.getPk_invoice()).toString();
			List<InvLineBVO> voList = groupMapInvLineBVOs.get(key);
			if (voList == null) {
				voList = new ArrayList<InvLineBVO>();
				groupMapInvLineBVOs.put(key, voList);
			}
			voList.add(invLineBVO);
		}
		
		for(String key : groupMapInvLineBVOs.keySet()){
			int index = 0;
			for(InvLineBVO invLineBVO : groupMapInvLineBVOs.get(key)){
				Boolean addr_flag = false;
				for(AddressVO addressVO : addressVOs){
					if(invLineBVO.getPk_address().equals(addressVO.getPk_address())){
						invLineBVO.setPk_address(addressVO.getPk_address());
						invLineBVO.setPk_province(addressVO.getPk_province());
						invLineBVO.setPk_city(addressVO.getPk_city());
						invLineBVO.setPk_area (addressVO.getPk_area());
						invLineBVO.setDetail_addr(addressVO.getDetail_addr());
						invLineBVO.setContact (addressVO.getContact());
						invLineBVO.setPhone (addressVO.getPhone());
						invLineBVO.setMobile  (addressVO.getMobile());
						invLineBVO.setEmail (addressVO.getEmail());
						addr_flag = true;
						VOs.add(invLineBVO);
					}
				}
				if(!addr_flag){
					throw new BusiException("地址 [?]没有维护！",invLineBVO.getPk_address());
				}
				//将pickup的信息复制到packBVO里
				if(invLineBVO.getOperate_type() == OperateTypeConst.PICKUP){
					InvPackBVO invpackBVO = new InvPackBVO();
					invpackBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invpackBVO);
					invpackBVO.setPk_invoice(invLineBVO.getPk_invoice());
					invpackBVO.setGoods_code(invLineBVO.getGoods_code());
					invpackBVO.setGoods_name(invLineBVO.getGoods_name());
					invpackBVO.setNum(invLineBVO.getNum());
					invpackBVO.setWeight(invLineBVO.getWeight());
					invpackBVO.setVolume(invLineBVO.getVolume());
					invpackBVO.setUnit_weight(invLineBVO.getUnit_weight());
					invpackBVO.setUnit_volume(invLineBVO.getUnit_volume());
					invpackBVO.setPack(invLineBVO.getPack());
					invpackBVO.setMin_pack(invLineBVO.getMin_pack());
					invpackBVO.setReference_no(invLineBVO.getReference_no());
					invpackBVO.setLength(invLineBVO.getLength());
					invpackBVO.setWidth(invLineBVO.getWidth());
					invpackBVO.setHeight(invLineBVO.getWeight());
					index += 10;
					invpackBVO.setSerialno(index);
					allInvPackBVOs.add(invpackBVO);
					VOs.add(invpackBVO);
				}
			}
			
		}			
		for(TransBilityBVO transBilityBVO : allTransBilityBVOs){
			if(carTypeVOs != null && carTypeVOs.length > 0){
				Boolean carType_flag = false;
				for(CarTypeVO carTypeVO : carTypeVOs){
					if(transBilityBVO.getPk_car_type() != null &&transBilityBVO.getPk_car_type().equals(carTypeVO.getCode())){
						transBilityBVO.setPk_car_type(carTypeVO.getPk_car_type());
						carType_flag = true;
					}
				}
				if(!carType_flag){
					throw new BusiException("车辆类型 [?]没有维护！",transBilityBVO.getPk_car_type());
				}
			}else{
				throw new BusiException("车辆类型 [?]没有维护！",transBilityBVO.getPk_car_type());
			}
		}
			
		for(InvoiceVO invoiceVO : allInvoiceVOs){
			List<InvPackBVO> invPackBVOs = new ArrayList<InvPackBVO>();
			for(InvPackBVO invPackBVO : allInvPackBVOs){
				if(invoiceVO.getPk_invoice().equals(invPackBVO.getPk_invoice())){
					invPackBVOs.add(invPackBVO);
				}
			}
			if(invPackBVOs != null && invPackBVOs.size() > 0){
				// 统计总件数、重量、体积,体积重，计费重
				InvoiceUtils.setHeaderCount(invoiceVO, invPackBVOs.toArray(new InvPackBVO[invPackBVOs.size()]));
			}
			List<TransBilityBVO> tBVOs = new ArrayList<TransBilityBVO>();
			for(TransBilityBVO transBilityBVO : allTransBilityBVOs){
				if(invoiceVO.getPk_invoice().equals(transBilityBVO.getPk_invoice())){
					tBVOs.add(transBilityBVO);
				}
			}
			// 计算表体金额，表头总金额
			List<SuperVO> rdVOAndRdBVO = this.computeReceiveDetail(invoiceVO, invPackBVOs.toArray(new InvPackBVO[invPackBVOs.size()]), tBVOs.toArray(new TransBilityBVO[tBVOs.size()]));
			VOs.addAll(rdVOAndRdBVO);
		}
		NWDao.getInstance().saveOrUpdate(VOs);
		CMUtils.totalCostComput(allInvoiceVOs);
	}
	
	private List<SuperVO> computeReceiveDetail(InvoiceVO invoiceVO, InvPackBVO[] invPackBVO, TransBilityBVO[] tbBVOs){
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		ReceiveDetailVO receiveDetailVO = new ReceiveDetailVO();
		receiveDetailVO.setVbillstatus(BillStatus.NEW);
		receiveDetailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YSMX)); // 生成应收明细的单据号
		if (WebUtils.getLoginInfo() != null) {
			// 如果没有登录信息，可能是通过其他系统导入的形式
			receiveDetailVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			receiveDetailVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		} else {
			receiveDetailVO.setCreate_user(invoiceVO.getCreate_user());
			receiveDetailVO.setPk_corp(invoiceVO.getPk_corp());
		}
		receiveDetailVO.setCreate_time(new UFDateTime(new Date()));
		receiveDetailVO.setCust_orderno(invoiceVO.getCust_orderno());
		receiveDetailVO.setOrderno(invoiceVO.getOrderno());
		receiveDetailVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(receiveDetailVO);
		if (receiveDetailVO.getDbilldate() == null) {
			receiveDetailVO.setDbilldate(new UFDate());
		}
		receiveDetailVO.setPk_customer(invoiceVO.getPk_customer());
		receiveDetailVO.setBala_customer(invoiceVO.getBala_customer());
		receiveDetailVO.setCurrency(ParameterHelper.getCurrency());
		receiveDetailVO.setAccount_period(new UFDateTime(invoiceVO.getReq_deli_date()));
		receiveDetailVO.setBilling_corp(invoiceVO.getBilling_corp());
		receiveDetailVO.setPack_num_count(invoiceVO.getPack_num_count());
		receiveDetailVO.setNum_count(invoiceVO.getNum_count());
		receiveDetailVO.setFee_weight_count(invoiceVO.getFee_weight_count());
		receiveDetailVO.setWeight_count(invoiceVO.getWeight_count());
		receiveDetailVO.setVolume_count(invoiceVO.getVolume_count());
		receiveDetailVO.setCost_amount(invoiceVO.getCost_amount());
		receiveDetailVO.setUngot_amount(invoiceVO.getCost_amount());// 未收金额等于总金额
		receiveDetailVO.setBalatype(invoiceVO.getBalatype());
		receiveDetailVO.setInvoice_vbillno(invoiceVO.getVbillno()); // 发货单单据号
		receiveDetailVO.setMemo(invoiceVO.getMemo());
		receiveDetailVO.setRece_type(ReceiveDetailConst.ORIGIN_TYPE); // 表示这是由发货单生成的应收明细
		receiveDetailVO.setMerge_type(ReceiveDetailConst.MERGE_TYPE.UNMERGE.intValue()); // 合并类型
		List<ReceDetailBVO> receDetailBVOs = new ArrayList<ReceDetailBVO>();
		//匹配合同
		
		contractService = SpringContextHolder.getBean("contractServiceImpl");
		if(contractService == null) {
			throw new BusiException("合同服务没有启动，服务ID：contractServiceImpl");
		}
		
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, invoiceVO.getBala_customer(),
				invoiceVO.getPk_trans_type(), invoiceVO.getPk_delivery(), invoiceVO.getPk_arrival(), invoiceVO.getDeli_city(),
				invoiceVO.getArri_city(), invoiceVO.getPk_corp(), invoiceVO.getReq_arri_date(),
				invoiceVO.getUrgent_level(),invoiceVO.getItem_code(),invoiceVO.getPk_trans_line(),invoiceVO.getIf_return());
		if (contractBVOs != null && contractBVOs.size() > 0) {
			// 匹配到合同
			String[] pk_car_type = null;
			if (tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for (int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			if(invPackBVO != null && invPackBVO.length > 0){
				Map<String,List<InvPackBVO>> groupMap = new  HashMap<String,List<InvPackBVO>>();
				for(InvPackBVO packBVO : invPackBVO){
					String key = packBVO.getPack();
					if(StringUtils.isBlank(key)){
						//没有包装的货品自动过滤
						continue;
					}
					List<InvPackBVO> voList = groupMap.get(key);
					if(voList == null){
						voList = new ArrayList<InvPackBVO>();
						groupMap.put(key, voList);
					}
					voList.add(packBVO);
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
			}
			receDetailBVOs = contractService.buildReceDetailBVO(invoiceVO.getBala_customer(),
					invoiceVO.getPack_num_count() == null ? 0 : invoiceVO.getPack_num_count().doubleValue(),
					receiveDetailVO.getNum_count() == null ? 0 : receiveDetailVO.getNum_count(),
					receiveDetailVO.getFee_weight_count() == null ? 0 : receiveDetailVO.getFee_weight_count().doubleValue(),
					receiveDetailVO.getWeight_count() == null ? 0 : receiveDetailVO.getWeight_count().doubleValue(),
					receiveDetailVO.getVolume_count() == null ? 0 : receiveDetailVO.getVolume_count().doubleValue(),packInfos, pk_car_type,
					invoiceVO.getPk_corp(), contractBVOs);
			if (receDetailBVOs != null && receDetailBVOs.size() > 0) {
				// 将这些匹配到的应收明细标识为新增
				for (ReceDetailBVO detailBVO : receDetailBVOs) {
					detailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(detailBVO);
					detailBVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail());
					detailBVO.setSystem_create(new UFBoolean(true));
					VOs.add(detailBVO);
				}
			}
			receiveDetailVO.setTax_cat(contractBVOs.get(0).getTax_cat());
			receiveDetailVO.setTax_rate(contractBVOs.get(0).getTax_rate());
			//  取第一行合同明细的税种，税率
			receiveDetailVO.setTaxmny(CMUtils.getTaxmny(receiveDetailVO.getCost_amount(), receiveDetailVO.getTax_cat(), receiveDetailVO.getTax_rate()));
		}
		VOs.add(receiveDetailVO);
		return VOs;
	}
	
	private InvLineBVO[] bubble_sort(InvLineBVO[] invLineBVOs, int len){
		//每次从后往前冒一个最小值，且每次能确定一个数在序列中的最终位置
		    for (int i = 0; i < len-1; i++){         //比较n-1次
		        boolean exchange = true;               //冒泡的改进，若在一趟中没有发生逆序，则该序列已有序
		        for (int j = len-1; j >i; j--){    // 每次从后边冒出一个最小值
		            if (invLineBVOs[j].getReq_date_from().before(invLineBVOs[j-1].getReq_date_from())){       //发生逆序，则交换
		                InvLineBVO temp = new InvLineBVO();
		                temp =  invLineBVOs[j];
		                invLineBVOs[j] = invLineBVOs[j - 1];
		                invLineBVOs[j - 1] =  temp;
		                exchange = false;
		            }
		        }
		        if (exchange){
		            return invLineBVOs;
		        }
		    }
		    return invLineBVOs;
		}
}
