package com.tms.service.cm.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;

import com.tms.constants.ContractConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.cm.ContractService;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractMatchVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.ReceDetailBMatchVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;

public class YusenReceHandler {
	
	private ContractService contractService = SpringContextHolder.getBean("contractServiceImpl");
	
	private Logger logger = Logger.getLogger(YusenReceHandler.class);
	
	/**
	 * 返回格式如下 Map<String,String> key : 费用大类对应的小类，用','拼接。 value,这个费用大类对应的的单据
	 * @param ids
	 * @param pk_user
	 * @return 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String[],String> dispatcher(List<String> ids, String pk_user,Integer type){
		if( ids == null || ids.size()  == 0){
			throw new BusiException("费用计算缺少参数！");
		}
		final Map<String[],String> results = new HashMap<String[],String>();
		// 存储过程名称
		final String PROC_NAME = "ts_cost_calculation_proc";
		final String IDS = NWUtils.join(ids, ",");
		final String TYPE = type.toString();
		final String USER = pk_user;
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, IDS);
					cs.setString(2, TYPE);
					cs.setString(3, USER);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs != null && rs.next()) {
						results.put(rs.getString("pk_receive_detail").split(","), rs.getString("parent_type"));
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			throw new BusiException("费用计算方法分配器出现错误[?]！",e.getMessage());
		}
		return results;
	}
	
	public Map<ReceiveDetailVO, List<ReceDetailBVO>> compute(List<String> ids){
		Map<ReceiveDetailVO,List<ReceDetailBVO>> group = new HashMap<ReceiveDetailVO, List<ReceDetailBVO>>();
		if(ids == null || ids.size() == 0){
			return group;
		}
		Map<String[],String> maps = dispatcher(ids, WebUtils.getLoginInfo().getPk_user(),1);
		if(maps == null || maps.size() == 0){
			return group;
		}
		logger.info("----------------处理日邮应收计算，共["+maps.size()+"]组数据------------------");
		List<ReceDetailBVO> results = new ArrayList<ReceDetailBVO>();
		Set<String> allBillIds = new HashSet<String>();
		for(String[] billIds : maps.keySet()){
			if(billIds == null || billIds.length == 0){
				continue;
			}
			allBillIds.addAll(Arrays.asList(billIds));
		}
		ReceiveDetailVO[] allDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class, "pk_receive_detail in " + NWUtils.buildConditionString(allBillIds));
		//获取发货单信息
		String inv_sql = "SELECT ts_invoice.bala_customer,ts_invoice.pk_trans_type,ts_invoice.pk_delivery,ts_invoice.pk_arrival,  "
				+ " ts_invoice.deli_city,ts_invoice.arri_city,ts_invoice.pk_corp,ts_invoice.req_deli_date,ts_invoice.urgent_level, "
				+ " ts_invoice.item_code,ts_invoice.pk_trans_line,ts_invoice.if_return,ts_receive_detail.pk_receive_detail as def1  "
				+ " FROM ts_invoice WITH(NOLOCK)  "
				+ " LEFT JOIN ts_receive_detail WITH(NOLOCK) ON ts_receive_detail.invoice_vbillno = ts_invoice.vbillno  "
				+ " WHERE isnull(ts_invoice.dr,0)=0 AND isnull(ts_receive_detail.dr,0)=0  "
				+ " AND ts_receive_detail.pk_receive_detail IN  " + NWUtils.buildConditionString(allBillIds)
				+ " ORDER BY ts_invoice.req_deli_date ";
		List<InvoiceVO> allInvoiceVOs = NWDao.getInstance().queryForList(inv_sql, InvoiceVO.class);
		logger.info("----------------获取数据完毕------------------");
		for(String[] billIds : maps.keySet()){
			logger.info("----------------数据处理中------------------");
			String parent_type = maps.get(billIds);;
			if(billIds == null || billIds.length == 0){
				continue;
			}
			
			//获取这个批次里，对应的应付明细和委托单
			List<ReceiveDetailVO> receDetailVOs = new ArrayList<ReceiveDetailVO>();
			List<InvoiceVO> invoiceVOs = new ArrayList<InvoiceVO>();
			for(String billId : billIds){
				//获取应付
				for(ReceiveDetailVO receDetailVO : allDetailVOs){
					if(billId.equals(receDetailVO.getPk_receive_detail())){
						receDetailVOs.add(receDetailVO);
						break;
					}
				}
				//获取委托单
				for(InvoiceVO invoiceVO : allInvoiceVOs){
					if(billId.equals(invoiceVO.getDef1())){//pk_rece_detail 寸在def1字段
						invoiceVOs.add(invoiceVO);
						break;
					}
				}
			}
			if(invoiceVOs.size() == 0 || receDetailVOs.size() == 0){
				continue;
			}
			
			//合同匹配
			ContractMatchVO contractMatchVO = new ContractMatchVO();
			contractMatchVO.setContract_type(ContractConst.CUSTOMER);
			contractMatchVO.setPk_carrierOrBala_customer(invoiceVOs.get(0).getBala_customer());
			contractMatchVO.setPk_trans_type(invoiceVOs.get(0).getPk_trans_type());
			contractMatchVO.setStart_addr(invoiceVOs.get(0).getPk_delivery());
			contractMatchVO.setEnd_addr(invoiceVOs.get(invoiceVOs.size()-1).getPk_arrival());
			contractMatchVO.setStart_city(invoiceVOs.get(0).getDeli_city());
			contractMatchVO.setEnd_city(invoiceVOs.get(invoiceVOs.size()-1).getArri_city());
			contractMatchVO.setPk_corp(invoiceVOs.get(0).getPk_corp());
			contractMatchVO.setReq_arri_date(invoiceVOs.get(0).getReq_arri_date());
			contractMatchVO.setUrgent_level(invoiceVOs.get(0).getUrgent_level());
			contractMatchVO.setItem_code(invoiceVOs.get(0).getItem_code());
			contractMatchVO.setPk_trans_line(invoiceVOs.get(0).getPk_trans_line());
			contractMatchVO.setIf_return(invoiceVOs.get(0).getIf_return());
			contractMatchVO.setParent_type(parent_type == null ? 100000 :Integer.parseInt(parent_type));
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
			if(contractBVOs == null || contractBVOs.size() == 0){
				continue;
			}
			Iterator<ContractBVO> it = contractBVOs.iterator();
			//移除不需要的合同明细，顺便看看有没有按包装和设备计费的啊。
			boolean have_pack_valuation_type = false;
			boolean have_equip_valuation_type = false;
			while(it.hasNext()){
				ContractBVO contractBVO = it.next();
				if(contractBVO.getValuation_type().equals(ValuationTypeConst.PACK)){
					have_pack_valuation_type = true;
				}
				if(contractBVO.getValuation_type().equals(ValuationTypeConst.EQUIP)){
					have_equip_valuation_type = true;
				}
			}
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			String[] pk_car_types = null;
			if(contractBVOs == null || contractBVOs.size() == 0){
				continue;
			}
			if(have_pack_valuation_type){
				String pack_sql = "SELECT ts_inv_pack_b.* FROM ts_inv_pack_b  WITH(NOLOCK) "
						+ " LEFT JOIN ts_invoice WITH(NOLOCK) ON ts_inv_pack_b.pk_invoice = ts_invoice.pk_invoice "
						+ " LEFT JOIN ts_receive_detail WITH(NOLOCK) ON ts_receive_detail.invoice_vbillno = ts_invoice.vbillno "
						+ " WHERE isnull(ts_inv_pack_b.dr,0)=0 AND isnull(ts_invoice.dr,0)=0 AND isnull(ts_receive_detail.dr,0)=0  "
						+ " AND ts_receive_detail.pk_receive_detail IN " + NWUtils.buildConditionString(billIds);
				
				List<InvPackBVO> packBVOs = NWDao.getInstance().queryForList(pack_sql, InvPackBVO.class);
				if(packBVOs != null && packBVOs.size() > 0){
					Map<String,List<InvPackBVO>> groupMap = new  HashMap<String,List<InvPackBVO>>();
					//对包装按照pack进行分组
					for(InvPackBVO invPackBVO : packBVOs){
						String pack = invPackBVO.getPack();
						if(StringUtils.isBlank(pack)){
							//没有包装的货品自动过滤
							continue;
						}
						List<InvPackBVO> voList = groupMap.get(pack);
						if(voList == null){
							voList = new ArrayList<InvPackBVO>();
							groupMap.put(pack, voList);
						}
						voList.add(invPackBVO);
					}
					if (groupMap.size() > 0) {
						for(String pack : groupMap.keySet()){
							PackInfo packInfo = new PackInfo();
							List<InvPackBVO> voList = groupMap.get(pack);
							Integer num = 0;
							UFDouble weight = UFDouble.ZERO_DBL;
							UFDouble volume = UFDouble.ZERO_DBL;
							for(InvPackBVO packBVO : voList){
								num = num + (packBVO.getNum() == null ? 0 : packBVO.getNum());
								weight = weight.add(packBVO.getWeight() == null ? UFDouble.ZERO_DBL : packBVO.getWeight());
								volume = volume.add(packBVO.getVolume() == null ? UFDouble.ZERO_DBL : packBVO.getVolume());
							}
							packInfo.setPack(pack);
							packInfo.setNum(num);
							packInfo.setWeight(weight);
							packInfo.setVolume(volume);
							packInfos.add(packInfo);
						}
					}
				}
			}
			if(have_equip_valuation_type){
				String equip_sql = "SELECT distinct ts_trans_bility_b.pk_car_type  "
						+ " FROM ts_receive_detail WITH(NOLOCK) "
						+ " LEFT JOIN ts_invoice WITH(NOLOCK) ON ts_receive_detail.invoice_vbillno=ts_invoice.vbillno AND ts_invoice.dr=0 "
						+ " LEFT JOIN ts_trans_bility_b WITH(NOLOCK) ON ts_invoice.pk_invoice=ts_trans_bility_b.pk_invoice "
						+ " WHERE ts_receive_detail.pk_receive_detail in " + NWUtils.buildConditionString(billIds);
				List<String> pk_car_type = NWDao.getInstance().queryForList(equip_sql, String.class);
				if(pk_car_type != null && pk_car_type.size() > 0){
					pk_car_types = pk_car_type.toArray(new String[pk_car_type.size()]);
				}
			}
			UFDouble pack_num_count = UFDouble.ZERO_DBL;
			Integer num_count = 0;
			UFDouble fee_weight_count = UFDouble.ZERO_DBL;
			UFDouble weight_count = UFDouble.ZERO_DBL;
			UFDouble volume_count = UFDouble.ZERO_DBL;
			for(ReceiveDetailVO detailVO : receDetailVOs){
				pack_num_count = pack_num_count.add(detailVO.getPack_num_count());
				num_count += detailVO.getNum_count() == null ? 0 : detailVO.getNum_count();
				fee_weight_count = fee_weight_count.add(detailVO.getFee_weight_count());
				weight_count = weight_count.add(detailVO.getWeight_count());
				volume_count = volume_count.add(detailVO.getVolume_count());
			}
			
			ReceDetailBMatchVO receDetailBMatchVO = new ReceDetailBMatchVO();
			receDetailBMatchVO.setBala_customer(invoiceVOs.get(0).getBala_customer());
			receDetailBMatchVO.setPack_num_count(pack_num_count);
			receDetailBMatchVO.setNum_count(num_count);
			receDetailBMatchVO.setFee_weight_count(fee_weight_count);
			receDetailBMatchVO.setWeight_count(weight_count);
			receDetailBMatchVO.setVolume_count(volume_count);
			receDetailBMatchVO.setPk_car_types(pk_car_types);
			receDetailBMatchVO.setPk_corp(invoiceVOs.get(0).getPk_corp());
			receDetailBMatchVO.setContractBVOs(contractBVOs);
			receDetailBMatchVO.setPackInfos(packInfos);
			List<ReceDetailBVO> detailBVOs = contractService.buildReceDetailBVO(receDetailBMatchVO);
			//绑定应收主键
			if(detailBVOs == null || detailBVOs.size() == 0){
				continue;
			}
			for(ReceDetailBVO detailBVO : detailBVOs){
				detailBVO.setPk_receive_detail(NWUtils.join(billIds, ","));
				results.add(detailBVO);
			}
			logger.info("----------------完成一条费用的计算------------------");
		}
		logger.info("----------------完成费用生成，开始插入费用------------------");
		ReceDetailBVO[] oldDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceDetailBVO.class,
				"pk_receive_detail IN " + NWUtils.buildConditionString(allBillIds));
		//分摊应收明细 如果有总的计费重，那就按照计费重分摊，否则就按件数分摊，如果只有一个应收就不分摊
		/*
		 * 对每一个应收明细的子信息，都要进行分摊，分摊的维度是这个子信息属于哪些应收
		 * 好在子信息记录了对应的应收的主键数组
		 * 那么在对每个子信息进行处理的时候，根据明细主键数组来获得分摊系数即可
		 * 在对每个子信息都分摊完毕之后，在统一进行主键的绑定操作，将子信息和明细绑定起来
		 * 
		 */
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(ReceiveDetailVO detailVO : allDetailVOs){
			List<ReceDetailBVO> detailBVOs = new ArrayList<ReceDetailBVO>();
			for(ReceDetailBVO detailBVO : results){
				//对应应收的主键值
				String[] billIds = detailBVO.getPk_receive_detail().split(",");
				//获取每个id对应的分摊维度
				Map<String,UFDouble> disMap = computeSharingDimension(billIds, allDetailVOs);
				for(String billId : billIds){
					if(billId.equals(detailVO.getPk_receive_detail())){
						ReceDetailBVO detailBVOTemp = (ReceDetailBVO) detailBVO.clone();
						detailBVOTemp.setPk_rece_detail_b(null);
						detailBVOTemp.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(detailBVOTemp);
						detailBVOTemp.setPk_receive_detail(billId); // 设置主表的主键
						detailBVOTemp.setAmount(detailBVOTemp.getAmount() == null ? UFDouble.ZERO_DBL : detailBVOTemp.getAmount().multiply(disMap.get(billId)));
						detailBVOTemp.setContract_amount(detailBVOTemp.getContract_amount() == null ? UFDouble.ZERO_DBL : detailBVOTemp.getContract_amount().multiply(disMap.get(billId)));
						detailBVOTemp.setContract_cost(detailBVOTemp.getContract_cost() == null ? UFDouble.ZERO_DBL : detailBVOTemp.getContract_cost().multiply(disMap.get(billId)));
						detailBVOs.add(detailBVOTemp);//匹配出来的费用
						toBeUpdate.add(detailBVOTemp);
					}
				}
			}
			for(ReceDetailBVO detailBVO : oldDetailBVOs){
				if(detailBVO.getPk_receive_detail().equals(detailVO.getPk_receive_detail())){
					if(UFBoolean.TRUE.equals(detailBVO.getSystem_create())){
						detailBVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(detailBVO);
					}else{
						detailBVOs.add(detailBVO);//原有的手工维护的费用
					}
				}
			}
			if(detailBVOs != null && detailBVOs.size() > 0){
				detailVO.setTax_cat(detailBVOs.get(0).getTax_cat());
				detailVO.setTax_rate(detailBVOs.get(0).getTax_rate());
			}
			computeHeadAmount(detailVO, detailBVOs);//计算头部金额
			detailVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(detailVO);
			group.put(detailVO, detailBVOs);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate,true);//删除无用的费用明细子信息和保存新的费用明细
		logger.info("----------------开始计算利润------------------");
		//计算利润
		CMUtils.totalCostComput(allInvoiceVOs);
		logger.info("----------------计算完毕------------------");
		return group;
	}
	
	
	public void computeHeadAmount(ReceiveDetailVO rdVO, List<ReceDetailBVO> rdBVOs){
		if (rdBVOs != null && rdBVOs.size() > 0) {
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			for (ReceDetailBVO rdBVO : rdBVOs) {
				cost_amount = cost_amount.add(rdBVO.getAmount()== null ? UFDouble.ZERO_DBL : rdBVO.getAmount());
			}
			rdVO.setCost_amount(cost_amount);
			// 未付金额
			UFDouble got_amount = rdVO.getGot_amount() == null ? UFDouble.ZERO_DBL : rdVO.getGot_amount();
			rdVO.setUngot_amount(rdVO.getCost_amount().sub(got_amount));
			
			rdVO.setTax_cat(rdBVOs.get(0).getTax_cat());
			rdVO.setTax_rate(rdBVOs.get(0).getTax_rate());
			rdVO.setTaxmny(CMUtils.getTaxmny(rdVO.getCost_amount(), rdVO.getTax_cat(), rdVO.getTax_rate()));
		} else {
			rdVO.setCost_amount(UFDouble.ZERO_DBL);
			rdVO.setUngot_amount(UFDouble.ZERO_DBL);
		}
	}
	
	public Map<String,UFDouble> computeSharingDimension(String[] billIds, ReceiveDetailVO[] detailVOs){
		Map<String,UFDouble> map = new HashMap<String, UFDouble>();
		if(billIds.length == 1){
			map.put(billIds[0], UFDouble.ONE_DBL);
			return map;
		}
		UFDouble fee_weight_cost = UFDouble.ZERO_DBL;//总的计费重
		for(String id : billIds){
			for(ReceiveDetailVO detailVO : detailVOs){
				if(id.equals(detailVO.getPk_receive_detail())){
					fee_weight_cost = fee_weight_cost.add(detailVO.getFee_weight_count());
					break;
				}
			}
		}
		for(String id : billIds){
			for(ReceiveDetailVO detailVO : detailVOs){
				if(id.equals(detailVO.getPk_receive_detail())){
					if(fee_weight_cost.doubleValue() == 0){//平均分摊
						UFDouble dis = new UFDouble(1.0/billIds.length);
						map.put(id,dis);
					}else{//按计费重分摊
						UFDouble dis = detailVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : detailVO.getFee_weight_count().div(fee_weight_cost);
						map.put(id,dis);
					}
					break;
				}
			}
		}
		return map;
	}

}
