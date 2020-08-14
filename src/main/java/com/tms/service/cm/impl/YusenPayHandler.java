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
import com.tms.service.tp.impl.PZUtils;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractMatchVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBMatchVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntrustVO;

public class YusenPayHandler {
	
	private ContractService contractService = SpringContextHolder.getBean("contractServiceImpl");
	
	private Logger logger = Logger.getLogger(YusenPayHandler.class);
	
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
						results.put(rs.getString("pk_pay_detail").split(","), rs.getString("parent_type"));
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
	
	public Map<PayDetailVO, List<PayDetailBVO>> compute(List<String> ids){
		Map<PayDetailVO,List<PayDetailBVO>> group = new HashMap<PayDetailVO, List<PayDetailBVO>>();
		if(ids == null || ids.size() == 0){
			return group;
		}
		Map<String[],String> maps = dispatcher(ids, WebUtils.getLoginInfo().getPk_user(),2);
		if(maps == null || maps.size() == 0){
			return group;
		}
		logger.info("----------------处理日邮应付计算，共["+maps.size()+"]组数据------------------");
		List<PayDetailBVO> results = new ArrayList<PayDetailBVO>();
		Set<String> allBillIds = new HashSet<String>();
		Set<String> equipBillIds = new HashSet<String>();//为了方便处理和数据库效率，每组数据只取一个作为车型。 
		for(String[] billIds : maps.keySet()){
			if(billIds == null || billIds.length == 0){
				continue;
			}
			allBillIds.addAll(Arrays.asList(billIds));
			equipBillIds.add(billIds[0]);
		}
		PayDetailVO[] allDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "pk_pay_detail in " + NWUtils.buildConditionString(allBillIds));
		//获取委托单信息
		String ent_sql = "SELECT ts_entrust.pk_carrier,ts_entrust.pk_trans_type,ts_entrust.pk_delivery,ts_entrust.pk_arrival,  "
				+ " ts_entrust.deli_city,ts_entrust.arri_city,ts_entrust.pk_corp,ts_entrust.req_deli_date,ts_entrust.urgent_level, "
				+ " ts_entrust.item_code,ts_entrust.pk_trans_line,ts_entrust.IF_return,ts_pay_detail.pk_pay_detail as def1  "
				+ " FROM ts_entrust WITH(NOLOCK)  "
				+ " LEFT JOIN ts_pay_detail WITH(NOLOCK) ON ts_pay_detail.entrust_vbillno = ts_entrust.vbillno  "
				+ " WHERE isnull(ts_entrust.dr,0)=0 AND isnull(ts_pay_detail.dr,0)=0  "
				+ " AND ts_pay_detail.pk_pay_detail IN  " + NWUtils.buildConditionString(allBillIds)
				+ " ORDER BY ts_entrust.req_deli_date ";
		List<EntrustVO> allEntrustVOs = NWDao.getInstance().queryForList(ent_sql, EntrustVO.class);
		//获取设备信息
		String equip_sql = "SELECT ts_ent_transbility_b.pk_car_type, ts_pay_detail.pk_pay_detail "
				+ " FROM ts_pay_detail WITH(NOLOCK) "
				+ " INNER JOIN ts_entrust WITH(NOLOCK) ON ts_pay_detail.entrust_vbillno=ts_entrust.vbillno AND ts_entrust.dr=0 "
				+ " INNER JOIN ts_ent_transbility_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_transbility_b.pk_entrust AND ts_ent_transbility_b.dr=0 "
				+ " WHERE ts_pay_detail.pk_pay_detail in "  + NWUtils.buildConditionString(equipBillIds);
		List<Map<String,Object>> carTypeMaps = NWDao.getInstance().queryForList(equip_sql);
		logger.info("----------------获取数据完毕------------------");
		for(String[] billIds : maps.keySet()){
			logger.info("----------------数据处理中------------------");
			String parent_type = maps.get(billIds);
			if(billIds == null || billIds.length == 0){
				continue;
			}
			//获取这个批次里，对应的应付明细和委托单
			List<PayDetailVO> payDetailVOs = new ArrayList<PayDetailVO>();
			List<EntrustVO> entrustVOs = new ArrayList<EntrustVO>();
			for(String billId : billIds){
				//获取应付
				for(PayDetailVO payDetailVO : allDetailVOs){
					if(billId.equals(payDetailVO.getPk_pay_detail())){
						payDetailVOs.add(payDetailVO);
						break;
					}
				}
				//获取委托单
				for(EntrustVO entrustVO : allEntrustVOs){
					if(billId.equals(entrustVO.getDef1())){//pk_pay_detail 寸在def1字段
						entrustVOs.add(entrustVO);
						break;
					}
				}
			}
			if(entrustVOs.size() == 0 || payDetailVOs.size() == 0){
				continue;
			}
			//合同匹配
			ContractMatchVO contractMatchVO = new ContractMatchVO();
			contractMatchVO.setContract_type(ContractConst.CARRIER);
			contractMatchVO.setPk_carrierOrBala_customer(entrustVOs.get(0).getPk_carrier());
			contractMatchVO.setPk_trans_type(entrustVOs.get(0).getPk_trans_type());
			contractMatchVO.setStart_addr(entrustVOs.get(0).getPk_delivery());
			contractMatchVO.setEnd_addr(entrustVOs.get(entrustVOs.size()-1).getPk_arrival());
			contractMatchVO.setStart_city(entrustVOs.get(0).getDeli_city());
			contractMatchVO.setEnd_city(entrustVOs.get(entrustVOs.size()-1).getArri_city());
			contractMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
			contractMatchVO.setReq_arri_date(entrustVOs.get(0).getReq_arri_date());
			contractMatchVO.setUrgent_level(entrustVOs.get(0).getUrgent_level());
			contractMatchVO.setItem_code(entrustVOs.get(0).getItem_code());
			contractMatchVO.setPk_trans_line(entrustVOs.get(0).getPk_trans_line());
			contractMatchVO.setIf_return(entrustVOs.get(0).getIf_return());
			contractMatchVO.setParent_type(parent_type == null ? 100000 : Integer.parseInt(parent_type));
			List<ContractBVO> contractBVOs = contractService.matchContract(contractMatchVO);
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			List<String> pk_car_types = new ArrayList<String>();
			if(contractBVOs != null && contractBVOs.size() > 0){
				//获取车辆信息
				String billId = billIds[0];
				for(ContractBVO contractBVO : contractBVOs){
					if(contractBVO.getValuation_type().equals(ValuationTypeConst.EQUIP)){
						for(Map<String,Object> carTypeMap : carTypeMaps){
							if(billId.equals(String.valueOf(carTypeMap.get("pk_pay_detail")))){
								pk_car_types.add(String.valueOf(carTypeMap.get("pk_car_type")));
							}
						}
						break;
					}
				}
				//看看有没有按包装和设备计费的啊。
				for(ContractBVO contractBVO : contractBVOs){
					if(contractBVO.getValuation_type().equals(ValuationTypeConst.PACK)){
						String pack_sql = " SELECT ts_ent_pack_b.* FROM ts_ent_pack_b  WITH(NOLOCK)  "
								+ " LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_ent_pack_b.pk_entrust = ts_entrust.pk_entrust  "
								+ " LEFT JOIN ts_pay_detail WITH(NOLOCK) ON ts_pay_detail.entrust_vbillno = ts_entrust.vbillno  "
								+ " WHERE isnull(ts_ent_pack_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 AND isnull(ts_pay_detail.dr,0)=0   "
								+ " AND ts_pay_detail.pk_pay_detail IN  " + NWUtils.buildConditionString(billIds);
						List<EntPackBVO> packBVOs = NWDao.getInstance().queryForList(pack_sql, EntPackBVO.class);
						if(packBVOs != null && packBVOs.size() > 0){
							Map<String,List<EntPackBVO>> groupMap = new  HashMap<String,List<EntPackBVO>>();
							//对包装按照pack进行分组
							for(EntPackBVO entPackBVO : packBVOs){
								String pack = entPackBVO.getPack();
								if(StringUtils.isBlank(pack)){
									//没有包装的货品自动过滤
									continue;
								}
								List<EntPackBVO> voList = groupMap.get(pack);
								if(voList == null){
									voList = new ArrayList<EntPackBVO>();
									groupMap.put(pack, voList);
								}
								voList.add(entPackBVO);
							}
							if (groupMap.size() > 0) {
								for(String pack : groupMap.keySet()){
									PackInfo packInfo = new PackInfo();
									List<EntPackBVO> voList = groupMap.get(pack);
									Integer num = 0;
									UFDouble weight = UFDouble.ZERO_DBL;
									UFDouble volume = UFDouble.ZERO_DBL;
									for(EntPackBVO packBVO : voList){
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
						break;
					}
				}
			}
			UFDouble pack_num_count = UFDouble.ZERO_DBL;
			Integer num_count = 0;
			UFDouble fee_weight_count = UFDouble.ZERO_DBL;
			UFDouble weight_count = UFDouble.ZERO_DBL;
			UFDouble volume_count = UFDouble.ZERO_DBL;
			for(PayDetailVO detailVO : payDetailVOs){
				pack_num_count = pack_num_count.add(detailVO.getPack_num_count());
				num_count += detailVO.getNum_count() == null ? 0 : detailVO.getNum_count();
				fee_weight_count = fee_weight_count.add(detailVO.getFee_weight_count());
				weight_count = weight_count.add(detailVO.getWeight_count());
				volume_count = volume_count.add(detailVO.getVolume_count());
			}
			
			PayDetailBMatchVO payDetailBMatchVO = new PayDetailBMatchVO();
			payDetailBMatchVO.setPk_carrier(entrustVOs.get(0).getPk_carrier());
			payDetailBMatchVO.setPack_num_count(pack_num_count);
			payDetailBMatchVO.setNum_count(num_count);
			payDetailBMatchVO.setFee_weight_count(fee_weight_count);
			payDetailBMatchVO.setWeight_count(weight_count);
			payDetailBMatchVO.setVolume_count(volume_count);
			payDetailBMatchVO.setPk_car_types(pk_car_types.size() == 0 ? new String[]{} : pk_car_types.toArray(new String[pk_car_types.size()]));
			payDetailBMatchVO.setPk_corp(entrustVOs.get(0).getPk_corp());
			payDetailBMatchVO.setContractBVOs(contractBVOs);
			payDetailBMatchVO.setPackInfos(packInfos);
			List<PayDetailBVO> detailBVOs = contractService.buildPayDetailBVO(payDetailBMatchVO);
			//绑定应收主键
			if(detailBVOs == null || detailBVOs.size() == 0){
				continue;
			}
			for(PayDetailBVO detailBVO : detailBVOs){
				detailBVO.setPk_pay_detail(NWUtils.join(billIds, ","));
				results.add(detailBVO);
			}
			logger.info("----------------完成一条费用的计算------------------");
		}
		logger.info("----------------完成费用生成，开始插入费用------------------");
		PayDetailBVO[] oldDetailBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
				"pk_pay_detail IN " + NWUtils.buildConditionString(allBillIds));
		//分摊应收明细 如果有总的计费重，那就按照计费重分摊，否则就按件数分摊，如果只有一个应收就不分摊
		/*
		 * 对每一个应收明细的子信息，都要进行分摊，分摊的维度是这个子信息属于哪些应收
		 * 好在子信息记录了对应的应收的主键数组
		 * 那么在对每个子信息进行处理的时候，根据明细主键数组来获得分摊系数即可
		 * 在对每个子信息都分摊完毕之后，在统一进行主键的绑定操作，将子信息和明细绑定起来
		 * 
		 */
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		for(PayDetailVO detailVO : allDetailVOs){
			List<PayDetailBVO> detailBVOs = new ArrayList<PayDetailBVO>();
			for(PayDetailBVO detailBVO : results){
				//对应应收的主键值
				String[] billIds = detailBVO.getPk_pay_detail().split(",");
				//获取每个id对应的分摊维度
				Map<String,UFDouble> disMap = computeSharingDimension(billIds, allDetailVOs);
				for(String billId : billIds){
					if(billId.equals(detailVO.getPk_pay_detail())){
						PayDetailBVO detailBVOTemp = (PayDetailBVO) detailBVO.clone();
						detailBVOTemp.setPk_pay_detail_b(null);
						detailBVOTemp.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(detailBVOTemp);
						detailBVOTemp.setPk_pay_detail(billId); // 设置主表的主键
						detailBVOTemp.setAmount(detailBVOTemp.getAmount() == null ? UFDouble.ZERO_DBL : detailBVOTemp.getAmount().multiply(disMap.get(billId)));
						detailBVOTemp.setContract_amount(detailBVOTemp.getContract_amount() == null ? UFDouble.ZERO_DBL : detailBVOTemp.getContract_amount().multiply(disMap.get(billId)));
						detailBVOTemp.setContract_cost(detailBVOTemp.getContract_cost() == null ? UFDouble.ZERO_DBL : detailBVOTemp.getContract_cost().multiply(disMap.get(billId)));
						detailBVOs.add(detailBVOTemp);//匹配出来的费用
						toBeUpdate.add(detailBVOTemp);
					}
				}
			}
			for(PayDetailBVO detailBVO : oldDetailBVOs){
				if(detailBVO.getPk_pay_detail().equals(detailVO.getPk_pay_detail())){
					if(UFBoolean.TRUE.equals(detailBVO.getSystem_create())){
						detailBVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(detailBVO);
					}else{
						detailBVOs.add(detailBVO);//原有的手工维护的费用
					}
				}
			}
			EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", detailVO.getEntrust_vbillno());
			List<PayDeviBVO> deviBVOs = PZUtils.getPayDeviBVOs(entrustVO,null,null, detailBVOs.toArray(new PayDetailBVO[detailBVOs.size()]));
			if( deviBVOs!= null && deviBVOs.size() > 0){
				for(PayDeviBVO deviBVO : deviBVOs){
					toBeUpdate.add(deviBVO);
				}
			}
			computeHeadAmount(detailVO, detailBVOs);//计算头部金额
			//ji算成本利润
			detailVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(detailVO);
			group.put(detailVO, detailBVOs);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate,true);//删除无用的费用明细子信息和保存新的费用明细
		logger.info("----------------开始计算成本利润------------------");
		//计算金额利润
		String inv_sql = "SELECT ts_invoice.pk_invoice,ts_invoice.vbillno FROM ts_pay_detail WITH(NOLOCK) "
				+ " LEFT JOIN ts_entrust  WITH(NOLOCK) ON ts_pay_detail.entrust_vbillno =ts_entrust.vbillno AND ts_entrust.dr=0 "
				+ " LEFT JOIN ts_invoice WITH(NOLOCK) ON ts_entrust.invoice_vbillno =ts_invoice.vbillno AND ts_invoice.dr=0 "
				+ " WHERE ts_pay_detail.pk_pay_detail in "  + NWUtils.buildConditionString(allBillIds);
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(inv_sql, InvoiceVO.class);
		CMUtils.totalCostComput(invoiceVOs);
		logger.info("----------------处理完毕------------------");
		return group;
	}
	
	public void computeHeadAmount(PayDetailVO pdVO, List<PayDetailBVO> pdBVOs){
		if (pdBVOs != null && pdBVOs.size() > 0) {
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			for (PayDetailBVO pdBVO : pdBVOs) {
				if (pdBVO.getStatus() != VOStatus.DELETED) {
					cost_amount = cost_amount.add(pdBVO.getAmount()== null ? UFDouble.ZERO_DBL : pdBVO.getAmount());
				}
			}
			pdVO.setCost_amount(cost_amount);
			// 未付金额
			UFDouble got_amount = pdVO.getGot_amount() == null ? UFDouble.ZERO_DBL : pdVO.getGot_amount();
			pdVO.setUngot_amount(pdVO.getCost_amount().sub(got_amount));
		} else {
			pdVO.setCost_amount(UFDouble.ZERO_DBL);
			pdVO.setUngot_amount(UFDouble.ZERO_DBL);
		}
		if (pdBVOs != null && pdBVOs.size() > 0) {
			pdVO.setTax_cat(pdBVOs.get(0).getTax_cat());
			pdVO.setTax_rate(pdBVOs.get(0).getTax_rate());
			pdVO.setTaxmny(CMUtils.getTaxmny(pdVO.getCost_amount(), pdVO.getTax_cat(), pdVO.getTax_rate()));
		}
	}
	
	
	public Map<String,UFDouble> computeSharingDimension(String[] billIds, PayDetailVO[] detailVOs){
		Map<String,UFDouble> map = new HashMap<String, UFDouble>();
		if(billIds.length == 1){
			map.put(billIds[0], UFDouble.ONE_DBL);
			return map;
		}
		UFDouble fee_weight_cost = UFDouble.ZERO_DBL;//总的计费重
		for(String id : billIds){
			for(PayDetailVO detailVO : detailVOs){
				if(id.equals(detailVO.getPk_pay_detail())){
					fee_weight_cost = fee_weight_cost.add(detailVO.getFee_weight_count());
					break;
				}
			}
		}
		for(String id : billIds){
			for(PayDetailVO detailVO : detailVOs){
				if(id.equals(detailVO.getPk_pay_detail())){
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
