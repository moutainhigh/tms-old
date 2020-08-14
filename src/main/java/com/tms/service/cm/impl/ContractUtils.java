package com.tms.service.cm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.redis.RedisDao;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tms.constants.ContractConst;
import com.tms.vo.base.AreaVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractMatchVO;
import com.tms.vo.cm.ContractVO;

/**
 * 这个类
 * @author muyun
 *
 */
public class ContractUtils {
	
	public static Logger logger = LoggerFactory.getLogger(ContractUtils.class);
	
	private static final String MILKRUN_TRANS_TYPE = "89816b7d4cfe457881425a48fad21cc8";

	/**
	 *  预加载所有合同信息
	 */
	public static String preLoad(){
		
		ContractVO[] contractVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ContractVO.class, "isnull(locked_flag,'N')='N'");
		
		String returnMsg = "";
		
		if(contractVOs == null || contractVOs.length == 0){
			return "0组信息！";
		}
		List<String> pk_contracts = new ArrayList<String>();
		//组装合同
		Map<String,List<String>> headMap = new HashMap<String, List<String>>();
		for(ContractVO contractVO : contractVOs){
			Integer contract_type =  contractVO.getContract_type();
			String pk_carrierOrBala_customer = "";
			if(ContractConst.CARRIER == contract_type){
				pk_carrierOrBala_customer = contractVO.getPk_carrier();
			}else if(ContractConst.CUSTOMER == contract_type){
				pk_carrierOrBala_customer = contractVO.getBala_customer();
			}
			String key = pk_carrierOrBala_customer +","+ contractVO.getTrans_type();
			List<String> voList = headMap.get(key);
			if(voList == null){
				voList = new ArrayList<String>();
				headMap.put(key, voList);
			}
			String value = contractVO.getPk_contract() + "," + contractVO.getPk_corp() + "," + contractVO.getEffective_date() + "," + contractVO.getInvalid_date();
			voList.add(value);
			pk_contracts.add(contractVO.getPk_contract());
		}
		RedisDao.getInstance().saveContractHeads(headMap);
		returnMsg = headMap.size() +  "组合同头部信息，";
		//处理明细
		ContractBVO[] contractBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ContractBVO.class, "pk_contract in " + NWUtils.buildConditionString(pk_contracts));
		Map<String,List<ContractBVO>> childMap = new HashMap<String, List<ContractBVO>>();
		for(ContractBVO contractBVO : contractBVOs){
			String pk_contract = contractBVO.getPk_contract();
			String start_addr = StringUtils.isBlank(contractBVO.getStart_addr()) ? "null" : contractBVO.getStart_addr();
			String end_addr = StringUtils.isBlank(contractBVO.getEnd_addr()) ? "null" : contractBVO.getEnd_addr();
			Integer urgent_level = contractBVO.getUrgent_level() == null ? 0 : contractBVO.getUrgent_level();
			String item_code = contractBVO.getItem_code() == null ? "null" : contractBVO.getPk_trans_line();
			String pk_trans_line = contractBVO.getPk_trans_line() == null ? "null" : contractBVO.getPk_trans_line();
			UFBoolean if_return = contractBVO.getIf_return() == null ? UFBoolean.FALSE : contractBVO.getIf_return();
			Integer parent_type = contractBVO.getParent_type() == null ? 100000 : contractBVO.getParent_type();//嘘。。。。
			
			String key = pk_contract + start_addr + end_addr + urgent_level + item_code + pk_trans_line + if_return + parent_type;
			List<ContractBVO> voList = childMap.get(key);	
			if(voList == null){
				voList = new ArrayList<ContractBVO>();
				childMap.put(key, voList);
			}
			voList.add(contractBVO);
		}
		if(childMap.size() > 0){
			RedisDao.getInstance().saveContractChilds(childMap);
		}
		returnMsg += (childMap.size() +  "组合同明细信息！");
		return returnMsg;
	}
	
	
	public static List<ContractBVO> matchContract(ContractMatchVO matchVO) {
		logger.info("--------------------开始匹配合同 --------------------");
		long strat =  System.currentTimeMillis();
		List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
		if (matchVO == null || matchVO.getContract_type() == null
				|| StringUtils.isBlank(matchVO.getPk_carrierOrBala_customer())
				|| StringUtils.isBlank(matchVO.getPk_corp()) || StringUtils.isBlank(matchVO.getPk_trans_type())) {
			logger.info("--------------------匹配合同关键字段为空，匹配失败 --------------------");
			return contractBVOs;
		}
		String[] corps = null;
		if (Constants.SYSTEM_CODE.equals(matchVO.getPk_corp())) {
			corps = new String[] { Constants.SYSTEM_CODE };
		} else {
			corps = new String[] { matchVO.getPk_corp(), Constants.SYSTEM_CODE };
		}
		if (StringUtils.isBlank(matchVO.getReq_arri_date())) {
			matchVO.setReq_arri_date(DateUtils.getCurrentDate());
		} else {
			// 只取日期，过滤时间
			String req_arri_date = DateUtils.formatDate(matchVO.getReq_arri_date().replace("T", " "), DateUtils.DATEFORMAT_HORIZONTAL);
			matchVO.setReq_arri_date(req_arri_date);
		}
		String pk_carrierOrBala_customer = matchVO.getPk_carrierOrBala_customer();
		// 根据承运商和计算客户，获取合同头部数据

		List<String> contractHeads = RedisDao.getInstance().getContractHeads(pk_carrierOrBala_customer,
				matchVO.getPk_trans_type());
		if (contractHeads == null || contractHeads.size() == 0) {
			return contractBVOs;
		}
		for (String corp : corps) {
			if (StringUtils.isBlank(corp)) {
				continue;
			}
			for (String contractHead : contractHeads) {
				String pk_contract = contractHead.split(",")[0];
				String pk_corp = contractHead.split(",")[1];
				UFDateTime effective_date = new UFDateTime(contractHead.split(",")[2]);
				UFDateTime invalid_date = new UFDateTime(contractHead.split(",")[3]);
				UFDateTime req_arri_date = new UFDateTime(matchVO.getReq_arri_date());
				if (!effective_date.after(req_arri_date) && !invalid_date.before(req_arri_date)
						&& pk_corp.equals(corp)) {
					String start_addr = StringUtils.isBlank(matchVO.getStart_addr()) ? "null" : matchVO.getStart_addr();
					String end_addr = StringUtils.isBlank(matchVO.getEnd_addr()) ? "null" : matchVO.getEnd_addr();
					String start_city = StringUtils.isBlank(matchVO.getStart_addr()) ? "null" : matchVO.getStart_city();
					String end_city = StringUtils.isBlank(matchVO.getEnd_addr()) ? "null" : matchVO.getEnd_city();
					Integer urgent_level = matchVO.getUrgent_level() == null ? 0 : matchVO.getUrgent_level();
					String item_code = matchVO.getItem_code() == null ? "null" : matchVO.getPk_trans_line();
					String pk_trans_line = matchVO.getPk_trans_line() == null ? "null" : matchVO.getPk_trans_line();
					UFBoolean if_return = matchVO.getIf_return() == null ? UFBoolean.FALSE : matchVO.getIf_return();
					Integer parent_type = matchVO.getParent_type() == null ? 100000 : matchVO.getParent_type();//嘘。。。。
					List<ContractBVO> bVOs = new ArrayList<ContractBVO>();
					//milkrun合同匹配，只支持合同到合同的匹配，特殊处理
					if(matchVO.getPk_trans_type().equals(MILKRUN_TRANS_TYPE)){
						String childKey = pk_contract + start_city + end_city + urgent_level + item_code + pk_trans_line
								+ if_return + parent_type;
						bVOs = RedisDao.getInstance().getContractChilds(childKey);
						
					}else{
						//地址到地址匹配
						String childKey = pk_contract + start_addr + end_addr + urgent_level + item_code + pk_trans_line
								+ if_return + parent_type;
						bVOs = RedisDao.getInstance().getContractChilds(childKey);
						//地址到城市匹配
						if(bVOs.size() == 0){
							if(StringUtils.isNotBlank(end_city)){
								childKey = pk_contract + start_addr + end_city + urgent_level + item_code + pk_trans_line
										+ if_return + parent_type;
								bVOs = RedisDao.getInstance().getContractChilds(childKey);
							}
						}
						//城市到地址匹配
						if(bVOs.size() == 0){
							if(StringUtils.isNotBlank(start_city)){
								childKey = pk_contract + start_city + end_addr + urgent_level + item_code + pk_trans_line
										+ if_return + parent_type;
								bVOs = RedisDao.getInstance().getContractChilds(childKey);
							}
						}
						//城市到城市匹配，支持分层
						List<AreaVO> stratCurrentAreaVOWithParents = RedisDao.getInstance().getCurrentAreaVOWithParents(start_city);
						List<AreaVO> endCurrentAreaVOWithParents = RedisDao.getInstance().getCurrentAreaVOWithParents(end_city);
						if(bVOs.size() == 0){
							if(stratCurrentAreaVOWithParents != null && stratCurrentAreaVOWithParents.size() > 0){
								outer:
								for(AreaVO stratAreaVO : stratCurrentAreaVOWithParents){
									if(endCurrentAreaVOWithParents != null && endCurrentAreaVOWithParents.size() > 0){
										for(AreaVO endAreaVO : endCurrentAreaVOWithParents){
											childKey = pk_contract + stratAreaVO.getPk_area() + endAreaVO.getPk_area() + urgent_level + item_code + pk_trans_line
													+ if_return + parent_type;
											bVOs = RedisDao.getInstance().getContractChilds(childKey);
											if(bVOs != null && bVOs.size() > 0){
												break outer;
											}
										}
									}
									
								}
							}
						}
						//匹配点位费合同
						//匹配提货点点位费合同
						if(stratCurrentAreaVOWithParents != null && stratCurrentAreaVOWithParents.size() > 0){
							for(AreaVO areaVO : stratCurrentAreaVOWithParents){
								childKey = pk_contract + areaVO.getPk_area() + "null" + urgent_level + item_code + pk_trans_line
										+ if_return + parent_type;
								List<ContractBVO> startPointContractBVOs = RedisDao.getInstance().getContractChilds(childKey);
								if(startPointContractBVOs != null && startPointContractBVOs.size() > 0){
									bVOs.addAll(startPointContractBVOs);
									break;
								}
							}
						}
						
						if(endCurrentAreaVOWithParents != null && endCurrentAreaVOWithParents.size() > 0){
							for(AreaVO areaVO : endCurrentAreaVOWithParents){
								childKey = pk_contract + "null" + areaVO.getPk_area() + urgent_level + item_code + pk_trans_line
										+ if_return + parent_type;
								List<ContractBVO> endPointContractBVOs = RedisDao.getInstance().getContractChilds(childKey);
								if(endPointContractBVOs != null && endPointContractBVOs.size() > 0){
									bVOs.addAll(endPointContractBVOs);
									break;
								}
							}
						}
					}
					if (bVOs != null && bVOs.size() > 0) {
						contractBVOs.addAll(bVOs);
					}
				}
			}
		}

		for (String corp : corps) {
			// 获取地址为空的合同
			for (String contractHead : contractHeads) {
				String pk_contract = contractHead.split(",")[0];
				String pk_corp = contractHead.split(",")[1];
				UFDateTime effective_date = new UFDateTime(contractHead.split(",")[2]);
				UFDateTime invalid_date = new UFDateTime(contractHead.split(",")[3]);
				UFDateTime req_arri_date = new UFDateTime(matchVO.getReq_arri_date());
				if (!effective_date.after(req_arri_date) && !invalid_date.before(req_arri_date)
						&& pk_corp.equals(corp)) {
					String start_addr = "null";
					String end_addr = "null";
					Integer urgent_level = matchVO.getUrgent_level() == null ? 0 : matchVO.getUrgent_level();
					String item_code = matchVO.getItem_code() == null ? "null" : matchVO.getPk_trans_line();
					String pk_trans_line = matchVO.getPk_trans_line() == null ? "null" : matchVO.getPk_trans_line();
					UFBoolean if_return = matchVO.getIf_return() == null ? UFBoolean.FALSE : matchVO.getIf_return();
					Integer parent_type = matchVO.getParent_type() == null ? 100000 : matchVO.getParent_type();//嘘。。。。
					String childKey = pk_contract + start_addr + end_addr + urgent_level + item_code + pk_trans_line
							+ if_return + parent_type;
					List<ContractBVO> bVOs = RedisDao.getInstance().getContractChilds(childKey);
					if (bVOs != null && bVOs.size() > 0) {
						contractBVOs.addAll(bVOs);
					}
				}
			}
		}
		long end =  System.currentTimeMillis();
		logger.info("--------------------"+end+","+(end-strat)+"--------------------");
		return contractBVOs;
	}
	
	
	
	/**
	 *  修改对应的合同
	 */
	public static void modify(HYBillVO billVO){
		if(billVO == null || billVO.getParentVO() == null){
			return;
		}
		//这是一个非常困难的问题
		ContractVO contractVO = (ContractVO) billVO.getParentVO();
		ContractBVO[] contractBVOs = (ContractBVO[]) billVO.getChildrenVO();
		String pk_contract = contractVO.getPk_contract();
		String pk_carrierOrBala_customer = "";
		if(contractVO.getContract_type().equals(ContractConst.CARRIER)){
			pk_carrierOrBala_customer = contractVO.getPk_carrier();
		}else if(contractVO.getContract_type().equals(ContractConst.CUSTOMER)){
			contractVO.getBala_customer();
		}
		//处理头部信息
		String key = pk_carrierOrBala_customer +","+ contractVO.getTrans_type();
		String value = contractVO.getPk_contract() + "," + contractVO.getPk_corp() + "," + contractVO.getEffective_date() + "," + contractVO.getInvalid_date();
		List<String> contractHeads = RedisDao.getInstance().getContractHeads(pk_carrierOrBala_customer,contractVO.getTrans_type());
		Map<String,List<String>> headMap = new HashMap<String, List<String>>();
		List<String> voList = new ArrayList<String>();
		voList.add(value);
		if(contractHeads != null && contractHeads.size() > 0){
			voList.addAll(contractHeads);
		}
		headMap.put(key, voList);
		RedisDao.getInstance().addContractHeads(headMap);
		//处理明细
		Map<String,List<ContractBVO>> childMap = new HashMap<String, List<ContractBVO>>();
		if(contractBVOs != null && contractBVOs.length > 0){
			for(ContractBVO contractBVO : contractBVOs){
				String start_addr = StringUtils.isBlank(contractBVO.getStart_addr()) ? "null" : contractBVO.getStart_addr();
				String end_addr = StringUtils.isBlank(contractBVO.getEnd_addr()) ? "null" : contractBVO.getEnd_addr();
				Integer urgent_level = contractBVO.getUrgent_level() == null ? 0 : contractBVO.getUrgent_level();
				String item_code = contractBVO.getItem_code() == null ? "null" : contractBVO.getPk_trans_line();
				String pk_trans_line = contractBVO.getPk_trans_line() == null ? "null" : contractBVO.getPk_trans_line();
				UFBoolean if_return = contractBVO.getIf_return() == null ? UFBoolean.FALSE : contractBVO.getIf_return();
				Integer parent_type = contractBVO.getParent_type() == null ? 100000 : contractBVO.getParent_type();//嘘。。。。
				String key_b = pk_contract + start_addr + end_addr + urgent_level + item_code + pk_trans_line + if_return + parent_type;
				if(contractBVO.getStatus() != VOStatus.DELETED){
					List<ContractBVO> voList_b = childMap.get(key_b);	
					if(voList_b == null){
						voList_b = new ArrayList<ContractBVO>();
						childMap.put(key_b, voList_b);
					}
					voList_b.add(contractBVO);
				}
			}
		}
		//移除原有的
		RedisDao.getInstance().removeChildsByPkContract(pk_contract);
		//保存
		if(childMap.size() > 0){
			RedisDao.getInstance().saveContractChilds(childMap);
		}
		
		
		
	}
	
	public static void modify(List<HYBillVO> billVOs){
		if(billVOs == null || billVOs.size() == 0){
			return;
		}
		for(HYBillVO billVO : billVOs){
			modify(billVO);
		}
	}
	
	public static void modify(HYBillVO[] billVOs){
		if(billVOs == null || billVOs.length == 0){
			return;
		}
		for(HYBillVO billVO : billVOs){
			modify(billVO);
		}
	}

	
	/**
	 *  添加合同
	 */
	public static void add(HYBillVO billVO){
		if(billVO == null || billVO.getParentVO() == null){
			return;
		}
		//这是一个非常困难的问题
		ContractVO contractVO = (ContractVO) billVO.getParentVO();
		ContractBVO[] contractBVOs = (ContractBVO[]) billVO.getChildrenVO();
		String pk_contract = contractVO.getPk_contract();
		String pk_carrierOrBala_customer = "";
		if(contractVO.getContract_type().equals(ContractConst.CARRIER)){
			pk_carrierOrBala_customer = contractVO.getPk_carrier();
		}else if(contractVO.getContract_type().equals(ContractConst.CUSTOMER)){
			contractVO.getBala_customer();
		}
		//处理头部信息
		String key = pk_carrierOrBala_customer +","+ contractVO.getTrans_type();
		String value = contractVO.getPk_contract() + "," + contractVO.getPk_corp() + "," + contractVO.getEffective_date() + "," + contractVO.getInvalid_date();
		List<String> contractHeads = RedisDao.getInstance().getContractHeads(pk_carrierOrBala_customer,contractVO.getTrans_type());
		Map<String,List<String>> headMap = new HashMap<String, List<String>>();
		List<String> voList = new ArrayList<String>();
		voList.add(value);
		if(contractHeads != null && contractHeads.size() > 0){
			voList.addAll(contractHeads);
		}
		headMap.put(key, voList);
		RedisDao.getInstance().addContractHeads(headMap);
		//处理明细
		Map<String,List<ContractBVO>> childMap = new HashMap<String, List<ContractBVO>>();
		if(contractBVOs != null && contractBVOs.length > 0){
			for(ContractBVO contractBVO : contractBVOs){
				String start_addr = StringUtils.isBlank(contractBVO.getStart_addr()) ? "null" : contractBVO.getStart_addr();
				String end_addr = StringUtils.isBlank(contractBVO.getEnd_addr()) ? "null" : contractBVO.getEnd_addr();
				Integer urgent_level = contractBVO.getUrgent_level() == null ? 0 : contractBVO.getUrgent_level();
				String item_code = contractBVO.getItem_code() == null ? "null" : contractBVO.getPk_trans_line();
				String pk_trans_line = contractBVO.getPk_trans_line() == null ? "null" : contractBVO.getPk_trans_line();
				UFBoolean if_return = contractBVO.getIf_return() == null ? UFBoolean.FALSE : contractBVO.getIf_return();
				Integer parent_type = contractBVO.getParent_type() == null ? 100000 : contractBVO.getParent_type();//嘘。。。。
				String key_b = pk_contract + start_addr + end_addr + urgent_level + item_code + pk_trans_line + if_return + parent_type;
				List<ContractBVO> voList_b = childMap.get(key_b);	
				if(voList_b == null){
					voList_b = new ArrayList<ContractBVO>();
					childMap.put(key_b, voList_b);
				}
				voList_b.add(contractBVO);
			}
			//直接存入就好，不会有重复的
			RedisDao.getInstance().saveContractChilds(childMap);
		}
		
	}
	
	public static void add(List<HYBillVO> billVOs){
		if(billVOs == null || billVOs.size() == 0){
			return;
		}
		for(HYBillVO billVO : billVOs){
			add(billVO);
		}
		
		
	}
	
	public static void add(HYBillVO[] billVOs){
		if(billVOs == null || billVOs.length == 0){
			return;
		}
		for(HYBillVO billVO : billVOs){
			add(billVO);
		}
	}
	
	/**
	 *  删除合同
	 */
	public static void remove(String pk_contract){
		remove(new String[]{pk_contract});
	}
	
	public static void remove(List<String> pk_contracts){
		if(pk_contracts == null || pk_contracts.size() == 0){
			return;
		}
		Map<String,List<String>> allContractHeads = RedisDao.getInstance().getAllContractHeads();
		if(allContractHeads == null || allContractHeads.size() == 0){
			return;
		}
		Map<String,List<String>> addContractHeads = new HashMap<String, List<String>>();
		List<String> headkeys = new ArrayList<String>();
		for(Entry<String, List<String>> contractHead : allContractHeads.entrySet()){
			String key = contractHead.getKey();
			List<String> values = contractHead.getValue();
			Iterator<String> it = values.iterator();
			boolean exist = false;//记录这组数据是否需要修改
			while(it.hasNext()){
				if(pk_contracts.contains(it.next().split(",")[0])){
					it.remove();
					exist = true;
				}
			}
			if(exist){
				if(values.size() == 0){//连这个头部都删除
					headkeys.add(key);
				}else{//修改明细
					addContractHeads.put(key, values);
				}
			}
		}
		if(addContractHeads.size() > 0){
			RedisDao.getInstance().addContractHeads(addContractHeads);
		}
		if(headkeys.size() > 0){
			RedisDao.getInstance().removeContractHeads(headkeys);
		}
	}
	
	public static void remove(String[] pk_contracts){
		if(pk_contracts != null && pk_contracts.length > 0){
			remove(Arrays.asList(pk_contracts));
		}
	}
}
