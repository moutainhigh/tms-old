package com.tms.service.job.cm;

import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.utils.SpringContextHolder;

import com.tms.constants.ContractConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.TransTypeConst;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.tp.impl.PZUtils;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;

/**
 * 自动计算发货单的应付明细
 * 
 * @author song
 * @Date 2015年10月25日 下午2:44:54
 *
 */
public class PayDetailBuilder implements IJobService {

	Logger logger = Logger.getLogger(this.getClass());

	private ContractService contractService = null;
	private HashMap<String, Map<String, String>> corpExpenseTypeMap = new HashMap<String, Map<String, String>>();
	private HashMap<String, Map<String, String>> corpExpenseTypeCodeMap = new HashMap<String, Map<String, String>>();
	private String keyChar = "|";

	/**
	 * 对提货段的发货单进行处理
	 */
	public void buildTHD(List<PayDetailMatchVO> allMatchVOs, EntrustVO[] entVOs) {
		logger.info("共查询到" + allMatchVOs.size() + "条相关的应付明细");
		logger.info("对委托单按相同批次号+结算客户+辅助表的相同供应商+辅助表的相同收货方+辅助表的提货方+辅助表的提货城市+辅助表的相同要求到货日期+辅助表的相同实际提货日期，进行分组");
		Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
		for (PayDetailMatchVO matchVO : allMatchVOs) {
			String key = new StringBuffer().append(matchVO.getLot()).append(keyChar).append(matchVO.getPk_corp())
					.toString();
			List<PayDetailMatchVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<PayDetailMatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");

		// 从分组内获取运输方式
		for (String key : groupMap.keySet()) {
			List<PayDetailMatchVO> matchVOs = groupMap.get(key);
			if (matchVOs == null || matchVOs.size() == 0) {
				continue;
			}
			// 判断每个分组的运输类型，是否为（公路零担、公路整车、公路零担+公路整车）
			List<PayDetailMatchVO> glldMatchVOs = new ArrayList<PayDetailMatchVO>();
			List<PayDetailMatchVO> glzcMatchVOs = new ArrayList<PayDetailMatchVO>();
			List<PayDetailMatchVO> glldMatchVOs1 = new ArrayList<PayDetailMatchVO>();
			List<String> listTransType = new ArrayList<String>();
			
			//通过 公路零担、公路整车的code 查询公路零担、公路整车的对应的主键
			TransTypeVO transTypeGLLDVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "code=?",
					TransTypeConst.TT_GLLD);
			TransTypeVO transTypeGLZCVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "code=?",
					TransTypeConst.TT_GLZC);
			for (PayDetailMatchVO matchVO : matchVOs) {
				String strTransType = matchVO.getPk_trans_type();

				// 判断运输方式计数
				if (!listTransType.contains(strTransType)) {
					listTransType.add(strTransType);
				}

				// 公路零担
				if (strTransType.equals(transTypeGLLDVO.getPk_trans_type())) {
					glldMatchVOs.add(matchVO);
					glldMatchVOs1.add(matchVO);
				} else if (strTransType.equals(transTypeGLZCVO.getPk_trans_type())) {
					glzcMatchVOs.add(matchVO);
				}
			}

			// 只有一种运输方式
			if (listTransType.size() == 1) {
				// 通过expenseType的code查询提货费费用类型
				ExpenseTypeVO THFexpenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code=?",
						ExpenseTypeConst.ET0020);
				// 通过expenseType的code查询提货段运费费用类型
				ExpenseTypeVO THDYFexpenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code=?",
						ExpenseTypeConst.ET0010);

				// 公路整车提货费
				if (listTransType.get(0).equals(transTypeGLZCVO.getPk_trans_type())) {
					// 提货费用
					computeTHF(glzcMatchVOs, entVOs, THFexpenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLZC);
					
				}
				

				// 公路零担费用计算(公路零担时，需要计算此费用 )
				if (listTransType.get(0).equals(transTypeGLLDVO.getPk_trans_type())) {
					//提货段运费
					computeTHDYF(glldMatchVOs, entVOs, THDYFexpenseTypeVO.getPk_expense_type());
					// 提货费用
					computeTHF(glldMatchVOs1, entVOs, THFexpenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLLD);
				}
			}
			// 存在两种运输方式，并且运输方式必须是（公路零担+公路整车、公路整车+公路零担）
			else if (listTransType.size() == 2) {
				String strType1 = listTransType.get(0);
				String strType2 = listTransType.get(1);
				if ((strType1.equals(transTypeGLZCVO.getPk_trans_type()) && strType2.equals(transTypeGLLDVO.getPk_trans_type()))
						|| (strType1.equals(transTypeGLLDVO.getPk_trans_type()) && strType2.equals(transTypeGLZCVO.getPk_trans_type()))) {
					// 通过expenseType的code查询提货费费用类型
					ExpenseTypeVO expenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code=?",
							ExpenseTypeConst.ET0020);

					// 公路整车提货费
					computeTHF(glzcMatchVOs, entVOs, expenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLZC);
					// 公路零担提货费
					computeTHF(glldMatchVOs, entVOs, expenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLLD);
				} else {
					throw new BusiException("运输方式必须是[公路零担、公路整车、公路零担+公路整车、公路整车+公路零担]方式！");
				}
			}
		}
	}

	/**
	 * 计算提货段运费
	 * 
	 * @param matchVOs
	 *            应付明细列表
	 * @param evtVOs
	 *            委托单
	 * @param expenseType
	 *            费用类型主键
	 */
	private void computeTHDYF(List<PayDetailMatchVO> matchVOs, EntrustVO[] entVOs, String expenseType) {

		List<PayDetailBVO> newPdbVOs = new ArrayList<PayDetailBVO>();
		for (PayDetailMatchVO matchVO : matchVOs) {
			// 重新匹配合同计算费用明细
			// 重新匹配合同，匹配合同后需要
			// 1、如果当前已经存在运费的记录，同时匹配返回的记录中也包括运费的记录，那么将现有的删除，而使用刚刚匹配到的记录代替
			// 2、更新表头的总金额，
			List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER,
					matchVO.getPk_carrier(), matchVO.getPk_trans_type(), matchVO.getPk_delivery(),
					matchVO.getPk_arrival(), matchVO.getDeli_city(), matchVO.getArri_city(), matchVO.getPk_corp(),
					matchVO.getReq_arri_date(),matchVO.getUrgent_level(),matchVO.getItem_code(),matchVO.getPk_trans_line(),matchVO.getIf_return());
			
			//没有匹配到合同自动跳出，报异常。
			if(contractBVOs==null||contractBVOs.size()==0){
				
				return;
			}

			// 获取与委托单相关联的车辆类型，多辆车
			EntTransbilityBVO[] tbBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
					"pk_entrust=?", matchVO.getPk_entrust());
			String[] pk_car_type = null;
			if (tbBVOs != null && tbBVOs.length > 0) {
				pk_car_type = new String[tbBVOs.length];
				for (int i = 0; i < tbBVOs.length; i++) {
					pk_car_type[i] = tbBVOs[i].getPk_car_type();
				}
			}

			// 重新计算金额
			List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(matchVO.getPk_carrier(),
					matchVO.getPack_num_count(), matchVO.getNum_count(), matchVO.getFee_weight_count().doubleValue(),
					matchVO.getWeight_count().doubleValue(), matchVO.getVolume_count().doubleValue(), 0, 0,matchVO.getPackInfos(), pk_car_type,
					matchVO.getPk_corp(),matchVO.getUrgent_level(),matchVO.getItem_code(),matchVO.getPk_trans_line(),matchVO.getIf_return(), contractBVOs);
			
			//将费用明细对应到应付明细上 2015-10-27
			if (pdbVOs != null && pdbVOs.size() > 0) {
			for (PayDetailBVO pdbVO : pdbVOs) {

				PayDetailBVO pdbVOTmp = new PayDetailBVO();
				pdbVOTmp.setAmount(pdbVO.getAmount());
				pdbVOTmp.setContract_amount(pdbVO.getAmount());
				pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
				pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
				pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
				pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
				pdbVOTmp.setSystem_create(UFBoolean.TRUE);
				pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
				pdbVOTmp.setPrice(pdbVO.getPrice());

				// 将合同明细的税种，税率冗余到这里
				pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
				pdbVOTmp.setTax_rate(pdbVO.getTax_rate());
				
				//增加车型吨位类型到备注上JONATHAN2015-10-29;
				pdbVOTmp.setMemo(pdbVO.getMemo());

				pdbVOTmp.setDr(0);
				pdbVOTmp.setPk_pay_detail(matchVO.getPk_pay_detail());
				pdbVOTmp.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(pdbVOTmp);
				newPdbVOs.add(pdbVOTmp);
				}
			}
		
		}
		
		// 过滤指定类型的应付明细实体类
		List<PayDetailBVO> newPdbVOsForMatch = newPdbVOs;
		
		// 删除系统创建的委托单的费用明细(提货段运费，委托单与发货单一对一关系)
		List<PayDetailBVO> oldPdbVOsForMatch = deletePayDetail(newPdbVOsForMatch, expenseType);
		
		// 更新委托单及费用明细
		updateEntrustAndPd(matchVOs, oldPdbVOsForMatch, newPdbVOsForMatch, entVOs, expenseType);
	}

	/**
	 * 根据费用类型过滤应付费用明细信息
	 * 
	 * @param pdbVOs
	 *            过滤前的应付费用明细
	 * @param expenseType
	 *            费用类型主键
	 * 
	 * @return List<PayDetailBVO> 经过过滤的新应付费用明细
	 */
	private List<PayDetailBVO> getPayDetailBVOByExpenseType(List<PayDetailBVO> pdbVOs, String expenseType) {
		if (pdbVOs == null || pdbVOs.size() == 0) {
			return null;
		}
		List<PayDetailBVO> listPdbVOs = new ArrayList<PayDetailBVO>();
		for (PayDetailBVO pdbVO : pdbVOs) {
			if (pdbVO.getPk_expense_type().equals(expenseType)) {
				listPdbVOs.add(pdbVO);
			}
		}
		return listPdbVOs;
	}

	/**
	 * 计算提货费
	 * 
	 * @param matchVOs
	 *            应付明细列表
	 * @param evtVOs
	 *            委托单
	 * @param expenseType
	 *            费用类型主键
	 * 
	 */
	private void computeTHF(List<PayDetailMatchVO> matchVOs, EntrustVO[] entVOs, String expenseType,
			String strTransType) {
		// 公路零担 过滤条件（批次号+公司+收货地址+提货地址）
		if (strTransType.equals(TransTypeConst.TT_GLLD)) {
			Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
			for (PayDetailMatchVO matchVO : matchVOs) {
				String key = new StringBuffer().append(matchVO.getPk_delivery()).append(keyChar)
						.append(matchVO.getPk_arrival()).toString();
				List<PayDetailMatchVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<PayDetailMatchVO>();
					groupMap.put(key, voList);
				}
				voList.add(matchVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			// 费用重新分摊后的应付费用明细
			List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
			// 从分组内获取运输方式
			for (String key : groupMap.keySet()) {
				List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
				if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
					continue;
				}

				// 合并计算
				Integer pack_num_count = 0;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				String strPk_carrier = "";
				String strPkEntrust = "";
				String strPk_corp = "";
				String strPk_trans_type = "";
				String strPk_delivery = "";
				String strPk_arrival = "";
				String strDeli_city = "";
				String strArri_city = "";
				String strReq_arri_date = "";
				Integer strUrgent_level = 0;
				String strItem_code = "";
				String strPk_trans_line = "";
				UFBoolean strIf_return = UFBoolean.FALSE;
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count
							.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
					weight_count = weight_count
							.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
					volume_count = volume_count
							.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

					strPk_carrier = mVO.getPk_carrier();
					strPkEntrust = mVO.getPk_entrust();
					strPk_corp = mVO.getPk_corp();
					strPk_trans_type = mVO.getPk_trans_type();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
					strReq_arri_date = mVO.getReq_arri_date();
					strUrgent_level = mVO.getUrgent_level();
					strItem_code = mVO.getItem_code();
					strPk_trans_line = mVO.getPk_trans_line();
					strIf_return = mVO.getIf_return();
					List<PackInfo> unitPackInfos = mVO.getPackInfos();
					if(unitPackInfos != null && unitPackInfos.size() > 0){
						packInfos.addAll(unitPackInfos);
					}
				}
				//汇总包装明细
				if(packInfos != null && packInfos.size() > 0){
					Map<String,List<PackInfo>> packGroupMap = new HashMap<String, List<PackInfo>>();
					for(PackInfo info : packInfos){
						String pack = info.getPack();
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						if(packInfoList == null){
							packInfoList = new ArrayList<PackInfo>();
							packGroupMap.put(pack, packInfoList);
						}
						packInfoList.add(info);
					}
					List<PackInfo> temp = new ArrayList<PackInfo>();
					for(String pack : packGroupMap.keySet()){
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						PackInfo packInfo = new PackInfo();
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(PackInfo info : packInfoList){
							num = num + (info.getNum() == null ? 0 : info.getNum());
							weight = weight.add(info.getWeight() == null ? UFDouble.ZERO_DBL : info.getWeight());
							volume = volume.add(info.getVolume() == null ? UFDouble.ZERO_DBL : info.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						temp.add(packInfo);
					}
					packInfos = temp;
				}
				// 获取车型
				String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);
				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date,strUrgent_level,strItem_code,strPk_trans_line,strIf_return);
				// 重新计算金额
				List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(strPk_carrier,
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos, strPk_car_Type, strPk_corp,
						strUrgent_level,strItem_code,strPk_trans_line,strIf_return,
						contractBVOs);
				// 过滤指定类型的应付明细实体类
				List<PayDetailBVO> newPdbVOs = getPayDetailBVOByExpenseType(pdbVOs, expenseType);
				//没有匹配到合同自动跳出，报异常。应该是按照每个分组分别计算合同的，所以放在大的循环内容，只有有一组没有匹配合同 就跳出，报异常。
				if(newPdbVOs==null||newPdbVOs.size()==0){
					return;
				}

				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					for (PayDetailBVO pdbVO : newPdbVOs) {
						PayDetailBVO pdbVOTmp = new PayDetailBVO();
						if(fee_weight_count.equals(UFDouble.ZERO_DBL)){
							pdbVOTmp.setAmount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
							pdbVOTmp.setContract_amount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
						}else{
							pdbVOTmp.setAmount(pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));	
							pdbVOTmp.setContract_amount(
									pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));
						}
						pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
						pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
						pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
						pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
						pdbVOTmp.setSystem_create(UFBoolean.TRUE);
						pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
						pdbVOTmp.setPrice(pdbVO.getPrice());

						// 将合同明细的税种，税率冗余到这里
						pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
						pdbVOTmp.setTax_rate(pdbVO.getTax_rate());

						//增加车型吨位类型到备注上JONATHAN2015-10-29;
						pdbVOTmp.setMemo(pdbVO.getMemo());
						
						pdbVOTmp.setDr(0);
						pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
						pdbVOTmp.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(pdbVOTmp);

						InsertPdbVOs.add(pdbVOTmp);
					}
				}
			}
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, expenseType);
				
				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, expenseType);
			
		} else if (strTransType.equals(TransTypeConst.TT_GLZC)) {
			Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
			// 公路整车 过滤条件 收货城市+提货城市）
			for (PayDetailMatchVO matchVO : matchVOs) {
				String key = new StringBuffer().append(matchVO.getArri_city()).append(keyChar)
						.append(matchVO.getDeli_city()).toString();
				List<PayDetailMatchVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<PayDetailMatchVO>();
					groupMap.put(key, voList);
				}
				voList.add(matchVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			// 从分组内获取运输方式
			UFDouble total_fee_weight_count = UFDouble.ZERO_DBL;
			List<PayDetailBVO> maxPayDetailBVOs = new ArrayList<PayDetailBVO>();
			UFDouble maxAmount = UFDouble.ZERO_DBL;
			// 费用重新分摊后的应付费用明细
			List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
			for (String key : groupMap.keySet()) {
				List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
				if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
					continue;
				}

				// 合并计算
				Integer pack_num_count = 0;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				String strPk_carrier = "";
				String strPkEntrust = "";
				String strPk_corp = "";
				String strPk_trans_type = "";
				String strPk_delivery = "";
				String strPk_arrival = "";
				String strDeli_city = "";
				String strArri_city = "";
				String strReq_arri_date = "";
				Integer strUrgent_level = 0;
				String strItem_code = "";
				String strPk_trans_line = "";
				UFBoolean strIf_return = UFBoolean.FALSE;
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count
							.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
					weight_count = weight_count
							.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
					volume_count = volume_count
							.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

					strPk_carrier = mVO.getPk_carrier();
					strPkEntrust = mVO.getPk_entrust();
					strPk_corp = mVO.getPk_corp();
					strPk_trans_type = mVO.getPk_trans_type();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
					strReq_arri_date = mVO.getReq_arri_date();
					strUrgent_level = mVO.getUrgent_level();
					strItem_code = mVO.getItem_code();
					strPk_trans_line = mVO.getPk_trans_line();
					strIf_return = mVO.getIf_return();
					List<PackInfo> unitPackInfos = mVO.getPackInfos();
					if(unitPackInfos != null && unitPackInfos.size() > 0){
						packInfos.addAll(unitPackInfos);
					}
				}
				//汇总包装明细
				if(packInfos != null && packInfos.size() > 0){
					Map<String,List<PackInfo>> packGroupMap = new HashMap<String, List<PackInfo>>();
					for(PackInfo info : packInfos){
						String pack = info.getPack();
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						if(packInfoList == null){
							packInfoList = new ArrayList<PackInfo>();
							packGroupMap.put(pack, packInfoList);
						}
						packInfoList.add(info);
					}
					List<PackInfo> temp = new ArrayList<PackInfo>();
					for(String pack : packGroupMap.keySet()){
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						PackInfo packInfo = new PackInfo();
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(PackInfo info : packInfoList){
							num = num + (info.getNum() == null ? 0 : info.getNum());
							weight = weight.add(info.getWeight() == null ? UFDouble.ZERO_DBL : info.getWeight());
							volume = volume.add(info.getVolume() == null ? UFDouble.ZERO_DBL : info.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						temp.add(packInfo);
					}
					packInfos = temp;
				}
				//累加计费重。
				total_fee_weight_count = total_fee_weight_count.add(fee_weight_count);
				// 获取车型
				String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);

				//如果车型为空，跳出此次循环。
				if(strPk_car_Type==null||strPk_car_Type.length==0){
					continue;
				}
				
				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date,strUrgent_level,strItem_code,strPk_trans_line,strIf_return );

				// 重新计算金额
				List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(strPk_carrier,
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos, strPk_car_Type, strPk_corp,
						strUrgent_level,strItem_code,strPk_trans_line,strIf_return ,
						contractBVOs);

				// 过滤指定类型的应付明细实体类
				List<PayDetailBVO> newPdbBVOs = getPayDetailBVOByExpenseType(pdbVOs, expenseType);

				//如果车型为空，跳出此次循环。
				if(newPdbBVOs==null||newPdbBVOs.size()==0){
					continue;
				}
				// 判断最大金额
				UFDouble contractAmount = UFDouble.ZERO_DBL;
				for (PayDetailBVO pdbBVO : newPdbBVOs) {
					contractAmount = contractAmount.add(pdbBVO.getAmount());
				}
				if (contractAmount.doubleValue() > maxAmount.doubleValue()) {
					maxAmount = contractAmount;
					maxPayDetailBVOs.clear();
					maxPayDetailBVOs.addAll(newPdbBVOs);
				}

			}
			//当所有分组匹配合同都为匹配到时，报异常，因为公路整车是按不同线路分组，分组后匹配合同比大小，所以只要有一个合同满足，就可以通过，基于该因素，将异常处理代码放在大的FOR 循环外面。
			if(maxPayDetailBVOs==null||maxPayDetailBVOs.size()==0){
				
				throw new BusiException("批次号["+ matchVOs.get(0).getLot()+"]没有匹配到提货段运费合同，请检查！");
			}

			for (PayDetailMatchVO mVO : matchVOs) {

				for (PayDetailBVO pdbVO : maxPayDetailBVOs) {

					PayDetailBVO pdbVOTmp = new PayDetailBVO();
					pdbVOTmp.setAmount(pdbVO.getAmount().div(total_fee_weight_count).multiply(mVO.getFee_weight_count()));
					pdbVOTmp.setContract_amount(
							pdbVO.getAmount().div(total_fee_weight_count).multiply(mVO.getFee_weight_count()));
					pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
					pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
					pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
					pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
					pdbVOTmp.setSystem_create(UFBoolean.TRUE);
					pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
					pdbVOTmp.setPrice(pdbVO.getPrice());
						// 将合同明细的税种，税率冗余到这里
					pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
					pdbVOTmp.setTax_rate(pdbVO.getTax_rate());
					
					//增加车型吨位类型到备注上JONATHAN2015-10-29;
					pdbVOTmp.setMemo(pdbVO.getMemo());

					pdbVOTmp.setDr(0);
					pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
					pdbVOTmp.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(pdbVOTmp);

					InsertPdbVOs.add(pdbVOTmp);
					}
				}
			
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, expenseType);
				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, expenseType);
			
		}
	}

	/**
	 * 对干线段的发货单进行处理
	 */
	public void buildGXD(List<PayDetailMatchVO> allMatchVOs, EntrustVO[] entVOs) {
		logger.info("共查询到" + allMatchVOs.size() + "条相关的应付明细");
		logger.info("对委托单按相同批次号同一个公司进行分组");
		Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
		for (PayDetailMatchVO matchVO : allMatchVOs) {
			String key = new StringBuffer().append(matchVO.getLot()).append(keyChar).append(matchVO.getPk_corp())
					.toString();
			List<PayDetailMatchVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<PayDetailMatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");

		// 从分组内获取运输方式
		for (String key : groupMap.keySet()) {
			List<PayDetailMatchVO> matchVOs = groupMap.get(key);
			if (matchVOs == null || matchVOs.size() == 0) {
				continue;
			}
			// 判断每个分组的运输类型，是否为（公路零担、公路整车）
			List<PayDetailMatchVO> glldMatchVOs = new ArrayList<PayDetailMatchVO>();
			List<PayDetailMatchVO> glzcMatchVOs = new ArrayList<PayDetailMatchVO>();
			List<String> listTransType = new ArrayList<String>();
			
			//通过 公路零担、公路整车的code 查询公路零担、公路整车的对应的主键
			TransTypeVO transTypeGLLDVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "code=?",
					TransTypeConst.TT_GLLD);
			TransTypeVO transTypeGLZCVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "code=?",
					TransTypeConst.TT_GLZC);
			for (PayDetailMatchVO matchVO : matchVOs) {
				String strTransType = matchVO.getPk_trans_type();

				// 判断运输方式计数
				if (!listTransType.contains(strTransType)) {
					listTransType.add(strTransType);
				}

				// 公路零担
				if (strTransType.equals(transTypeGLLDVO.getPk_trans_type())) {
					glldMatchVOs.add(matchVO);
				} else if (strTransType.equals(transTypeGLZCVO.getPk_trans_type())) {
					glzcMatchVOs.add(matchVO);
				}
			}

			// 通过expenseType的code查询干线费费用类型
			ExpenseTypeVO THFexpenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code=?",
						ExpenseTypeConst.ET0030);

			// 公路整车干线费
			if (listTransType.get(0).equals(transTypeGLZCVO.getPk_trans_type())) {
				//干线费用
				computeGXF(glzcMatchVOs, entVOs, THFexpenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLZC);
					
			}	

			// 公路零担费用计算(公路零担时，需要计算此费用 )
			if (listTransType.get(0).equals(transTypeGLLDVO.getPk_trans_type())) {
				// 干线费用
				computeGXF(glldMatchVOs, entVOs, THFexpenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLLD);
			}
			
		}
	}

	
	/**
	 * 计算干线费
	 * 
	 * @param matchVOs
	 *            应付明细列表
	 * @param evtVOs
	 *            委托单
	 * @param expenseType
	 *            费用类型主键
	 * 
	 */
	private void computeGXF(List<PayDetailMatchVO> matchVOs, EntrustVO[] entVOs, String expenseType,
			String strTransType) {
		// 公路零担 过滤条件上海分公司（批次号+订单号+收货地址+提货地址）
		// 公路零担 过滤条件大连分公司（批次号+委托单号）
		if (strTransType.equals(TransTypeConst.TT_GLLD)) {
			//获取公司2015-10-31jonathan
			CorpVO corpvo = NWDao.getInstance().queryByCondition(CorpVO.class, "pk_corp=?",
					 matchVOs.get(0).getPk_corp());
			 String corpCode = corpvo.getCorp_code();
			 String groupkey = "";
			Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
			for (PayDetailMatchVO matchVO : matchVOs) {
				//大连分公司按每个委托单计算费用
				if(corpCode.equals("DLC")){
					groupkey = new StringBuffer().append(matchVO.getArri_city()).append(keyChar)
							.append(matchVO.getDeli_city()).append(keyChar)
							.append(String.valueOf(matchVO.getEntrust_villno())).toString();
				}
				//其它分公司按照同一个订单分组计算。
				else{
				
					groupkey = new StringBuffer().append(matchVO.getArri_city()).append(keyChar)
						.append(matchVO.getDeli_city()).append(keyChar)
						.append(String.valueOf(matchVO.getDef5())).toString();
				}
				List<PayDetailMatchVO> voList = groupMap.get(groupkey);
				if (voList == null) {
					voList = new ArrayList<PayDetailMatchVO>();
					groupMap.put(groupkey, voList);
				}
				voList.add(matchVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			// 费用重新分摊后的应付费用明细
			List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
			// 从分组内获取运输方式
			for (String key : groupMap.keySet()) {
				List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
				if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
					continue;
				}

				// 合并计算
				Integer pack_num_count = 0;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				String strPk_carrier = "";
				String strPkEntrust = "";
				String strPk_corp = "";
				String strPk_trans_type = "";
				String strPk_delivery = "";
				String strPk_arrival = "";
				String strDeli_city = "";
				String strArri_city = "";
				String strReq_arri_date = "";
				Integer strUrgent_level = 0;
				String strItem_code = "";
				String strPk_trans_line = "";
				UFBoolean strIf_return = UFBoolean.FALSE;
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count
							.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
					weight_count = weight_count
							.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
					volume_count = volume_count
							.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

					strPk_carrier = mVO.getPk_carrier();
					strPkEntrust = mVO.getPk_entrust();
					strPk_corp = mVO.getPk_corp();
					strPk_trans_type = mVO.getPk_trans_type();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
					strReq_arri_date = mVO.getReq_arri_date();
					strUrgent_level = mVO.getUrgent_level();
					strItem_code = mVO.getItem_code();
					strPk_trans_line = mVO.getPk_trans_line();
					strIf_return = mVO.getIf_return();
					List<PackInfo> unitPackInfos = mVO.getPackInfos();
					if(unitPackInfos != null && unitPackInfos.size() > 0){
						packInfos.addAll(unitPackInfos);
					}
				}
				//汇总包装明细
				if(packInfos != null && packInfos.size() > 0){
					Map<String,List<PackInfo>> packGroupMap = new HashMap<String, List<PackInfo>>();
					for(PackInfo info : packInfos){
						String pack = info.getPack();
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						if(packInfoList == null){
							packInfoList = new ArrayList<PackInfo>();
							packGroupMap.put(pack, packInfoList);
						}
						packInfoList.add(info);
					}
					List<PackInfo> temp = new ArrayList<PackInfo>();
					for(String pack : packGroupMap.keySet()){
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						PackInfo packInfo = new PackInfo();
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(PackInfo info : packInfoList){
							num = num + (info.getNum() == null ? 0 : info.getNum());
							weight = weight.add(info.getWeight() == null ? UFDouble.ZERO_DBL : info.getWeight());
							volume = volume.add(info.getVolume() == null ? UFDouble.ZERO_DBL : info.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						temp.add(packInfo);
					}
					packInfos = temp;
				}
				// 获取车型
				String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);
				
				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date,strUrgent_level,strItem_code,strPk_trans_line,strIf_return );

				// 重新计算金额
				List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(strPk_carrier,
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos, strPk_car_Type, strPk_corp,
						strUrgent_level,strItem_code,strPk_trans_line,strIf_return ,
						contractBVOs);

				// 过滤指定类型的应付明细实体类
				List<PayDetailBVO> newPdbVOs = getPayDetailBVOByExpenseType(pdbVOs, expenseType);
				//没有匹配到合同自动跳出，报异常。应该是按照每个分组分别计算合同的，所以放在大的循环内容，只有有一组没有匹配合同 就跳出，报异常。
				if(newPdbVOs==null||newPdbVOs.size()==0){
					throw new BusiException("批次号["+ matchVOs.get(0).getLot()+"],应付明细单号["+payDetailmatchVOs.get(0).getPd_vbillno() + "]没有匹配到提货段运费合同，请检查！");
				}
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					for (PayDetailBVO pdbVO : newPdbVOs) {
						PayDetailBVO pdbVOTmp = new PayDetailBVO();
						if(fee_weight_count.equals(UFDouble.ZERO_DBL)){
							pdbVOTmp.setAmount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
							pdbVOTmp.setContract_amount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
						}else{
							pdbVOTmp.setAmount(pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));	
							pdbVOTmp.setContract_amount(
									pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));
						}
						pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
						pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
						pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
						pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
						pdbVOTmp.setSystem_create(UFBoolean.TRUE);
						pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
						pdbVOTmp.setPrice(pdbVO.getPrice());

						// 将合同明细的税种，税率冗余到这里
						pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
						pdbVOTmp.setTax_rate(pdbVO.getTax_rate());

						//增加车型吨位类型到备注上JONATHAN2015-10-29;
						pdbVOTmp.setMemo(pdbVO.getMemo());
						
						pdbVOTmp.setDr(0);
						pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
						pdbVOTmp.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(pdbVOTmp);

						InsertPdbVOs.add(pdbVOTmp);
					}
				}
			}
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, expenseType);
				
				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, expenseType);
			
		} else if (strTransType.equals(TransTypeConst.TT_GLZC)) {
			Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
			// 公路整车 过滤条件 收货城市+提货城市+ 同一订单号）
			for (PayDetailMatchVO matchVO : matchVOs) {
				String key = new StringBuffer().append(matchVO.getArri_city()).append(keyChar)
						.append(matchVO.getDeli_city()).append(keyChar)
						.append(String.valueOf(matchVO.getDef5())).toString();
				List<PayDetailMatchVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<PayDetailMatchVO>();
					groupMap.put(key, voList);
				}
				voList.add(matchVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			// 从分组内获取运输方式
			UFDouble total_fee_weight_count = UFDouble.ZERO_DBL;
			List<PayDetailBVO> maxPayDetailBVOs = new ArrayList<PayDetailBVO>();
			UFDouble maxAmount = UFDouble.ZERO_DBL;
			// 费用重新分摊后的应付费用明细
			List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
			for (String key : groupMap.keySet()) {
				List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
				if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
					continue;
				}

				// 合并计算
				Integer pack_num_count = 0;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				String strPk_carrier = "";
				String strPkEntrust = "";
				String strPk_corp = "";
				String strPk_trans_type = "";
				String strPk_delivery = "";
				String strPk_arrival = "";
				String strDeli_city = "";
				String strArri_city = "";
				String strReq_arri_date = "";
				Integer strUrgent_level = 0;
				String strItem_code = "";
				String strPk_trans_line = "";
				UFBoolean strIf_return = UFBoolean.FALSE;
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count
							.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
					weight_count = weight_count
							.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
					volume_count = volume_count
							.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

					strPk_carrier = mVO.getPk_carrier();
					strPkEntrust = mVO.getPk_entrust();
					strPk_corp = mVO.getPk_corp();
					strPk_trans_type = mVO.getPk_trans_type();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
					strReq_arri_date = mVO.getReq_arri_date();
					strUrgent_level = mVO.getUrgent_level();
					strItem_code = mVO.getItem_code();
					strPk_trans_line = mVO.getPk_trans_line();
					strIf_return = mVO.getIf_return();
					List<PackInfo> unitPackInfos = mVO.getPackInfos();
					if(unitPackInfos != null && unitPackInfos.size() > 0){
						packInfos.addAll(unitPackInfos);
					}
				}
				//累加计费重。
				total_fee_weight_count = total_fee_weight_count.add(fee_weight_count);
				// 获取车型
				String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);

				//如果车型为空，跳出此次循环。
				if(strPk_car_Type==null||strPk_car_Type.length==0){
					continue;
				}
				
				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date,strUrgent_level,strItem_code,strPk_trans_line,strIf_return );

				// 重新计算金额
				List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(strPk_carrier,
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos, strPk_car_Type, strPk_corp,
						strUrgent_level,strItem_code,strPk_trans_line,strIf_return ,
						contractBVOs);

				// 过滤指定类型的应付明细实体类
				List<PayDetailBVO> newPdbBVOs = getPayDetailBVOByExpenseType(pdbVOs, expenseType);

				//如果车型为空，跳出此次循环。
				if(newPdbBVOs==null||newPdbBVOs.size()==0){
					continue;
				}
				// 判断最大金额
				UFDouble contractAmount = UFDouble.ZERO_DBL;
				for (PayDetailBVO pdbBVO : newPdbBVOs) {
					contractAmount = contractAmount.add(pdbBVO.getAmount());
				}
				if (contractAmount.doubleValue() > maxAmount.doubleValue()) {
					maxAmount = contractAmount;
					maxPayDetailBVOs.clear();
					maxPayDetailBVOs.addAll(newPdbBVOs);
				}

			}
			//当所有分组匹配合同都为匹配到时，报异常，因为公路整车是按不同线路分组，分组后匹配合同比大小，所以只要有一个合同满足，就可以通过，基于该因素，将异常处理代码放在大的FOR 循环外面。
			if(maxPayDetailBVOs==null||maxPayDetailBVOs.size()==0){
				
				throw new BusiException("批次号["+ matchVOs.get(0).getLot()+"]没有匹配到提货段运费合同，请检查！");
			}

			for (PayDetailMatchVO mVO : matchVOs) {

				for (PayDetailBVO pdbVO : maxPayDetailBVOs) {

					PayDetailBVO pdbVOTmp = new PayDetailBVO();
					pdbVOTmp.setAmount(pdbVO.getAmount().div(total_fee_weight_count).multiply(mVO.getFee_weight_count()));
					pdbVOTmp.setContract_amount(
							pdbVO.getAmount().div(total_fee_weight_count).multiply(mVO.getFee_weight_count()));
					pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
					pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
					pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
					pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
					pdbVOTmp.setSystem_create(UFBoolean.TRUE);
					pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
					pdbVOTmp.setPrice(pdbVO.getPrice());
						// 将合同明细的税种，税率冗余到这里
					pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
					pdbVOTmp.setTax_rate(pdbVO.getTax_rate());
					
					//增加车型吨位类型到备注上JONATHAN2015-10-29;
					pdbVOTmp.setMemo(pdbVO.getMemo());

					pdbVOTmp.setDr(0);
					pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
					pdbVOTmp.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(pdbVOTmp);

					InsertPdbVOs.add(pdbVOTmp);
					}
				}
			
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, expenseType);

				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, expenseType);
			
		}
	}
	
	
	
	/**
	 * 对送货段的发货单进行处理
	 */
	public void buildSHD(List<PayDetailMatchVO> allMatchVOs, EntrustVO[] entVOs) {
		logger.info("共查询到" + allMatchVOs.size() + "条相关的应付明细");
		logger.info("对委托单按相同批次号同一个公司进行分组");
		Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
		for (PayDetailMatchVO matchVO : allMatchVOs) {
			String key = new StringBuffer().append(matchVO.getLot()).append(keyChar).append(matchVO.getPk_corp())
					.toString();
			List<PayDetailMatchVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<PayDetailMatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");

		// 从分组内获取运输方式
		for (String key : groupMap.keySet()) {
			List<PayDetailMatchVO> matchVOs = groupMap.get(key);
			if (matchVOs == null || matchVOs.size() == 0) {
				continue;
			}
			// 判断每个分组的运输类型，是否为（公路零担、公路整车）
			List<PayDetailMatchVO> glldMatchVOs = new ArrayList<PayDetailMatchVO>();
			List<PayDetailMatchVO> glzcMatchVOs = new ArrayList<PayDetailMatchVO>();
			List<String> listTransType = new ArrayList<String>();
			
			//通过 公路零担、公路整车的code 查询公路零担、公路整车的对应的主键
			TransTypeVO transTypeGLLDVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "code=?",
					TransTypeConst.TT_GLLD);
			TransTypeVO transTypeGLZCVO = NWDao.getInstance().queryByCondition(TransTypeVO.class, "code=?",
					TransTypeConst.TT_GLZC);
			for (PayDetailMatchVO matchVO : matchVOs) {
				String strTransType = matchVO.getPk_trans_type();

				// 判断运输方式计数
				if (!listTransType.contains(strTransType)) {
					listTransType.add(strTransType);
				}

				// 公路零担
				if (strTransType.equals(transTypeGLLDVO.getPk_trans_type())) {
					glldMatchVOs.add(matchVO);
				} else if (strTransType.equals(transTypeGLZCVO.getPk_trans_type())) {
					glzcMatchVOs.add(matchVO);
				}
			}

			// 通过expenseType的code查询送货费费用类型
			ExpenseTypeVO THFexpenseTypeVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "code=?",
						ExpenseTypeConst.ET0040);

			// 公路整车送货费
			if (listTransType.get(0).equals(transTypeGLZCVO.getPk_trans_type())) {
				//送货费用
				computeSHF(glzcMatchVOs, entVOs, THFexpenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLZC);
					
			}	

			// 公路零担费用计算(公路零担时，需要计算此费用 )
			if (listTransType.get(0).equals(transTypeGLLDVO.getPk_trans_type())) {
				// 送货费用
				computeSHF(glldMatchVOs, entVOs, THFexpenseTypeVO.getPk_expense_type(), TransTypeConst.TT_GLLD);
			}
			
		}
	}

	/**
	 * 计算送货费
	 * 
	 * @param matchVOs
	 *            应付明细列表
	 * @param evtVOs
	 *            委托单
	 * @param expenseType
	 *            费用类型主键
	 * 
	 */
	private void computeSHF(List<PayDetailMatchVO> matchVOs, EntrustVO[] entVOs, String expenseType,
			String strTransType) {
		// 公路零担 过滤条件（批次号+订单号+收货地址+提货地址）
		if (strTransType.equals(TransTypeConst.TT_GLLD)) {
			Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
			for (PayDetailMatchVO matchVO : matchVOs) {
				//如果DEF7为Y 代表该委托单不计算费用。
				if(matchVO.getDef7()!=null && matchVO.getDef7().equals("Y")){
					continue;
				}
				String key = new StringBuffer().append(matchVO.getArri_city()).append(keyChar)
						.append(matchVO.getDeli_city()).toString();
				List<PayDetailMatchVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<PayDetailMatchVO>();
					groupMap.put(key, voList);
				}
				voList.add(matchVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			// 费用重新分摊后的应付费用明细
			List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
			// 从分组内获取运输方式
			for (String key : groupMap.keySet()) {
				List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
				if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
					continue;
				}

				// 合并计算
				Integer pack_num_count = 0;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				String strPk_carrier = "";
				String strPkEntrust = "";
				String strPk_corp = "";
				String strPk_trans_type = "";
				String strPk_delivery = "";
				String strPk_arrival = "";
				String strDeli_city = "";
				String strArri_city = "";
				String strReq_arri_date = "";
				Integer strUrgent_level = 0;
				String strItem_code = "";
				String strPk_trans_line = "";
				UFBoolean strIf_return = UFBoolean.FALSE;
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count
							.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
					weight_count = weight_count
							.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
					volume_count = volume_count
							.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

					strPk_carrier = mVO.getPk_carrier();
					strPkEntrust = mVO.getPk_entrust();
					strPk_corp = mVO.getPk_corp();
					strPk_trans_type = mVO.getPk_trans_type();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
					strReq_arri_date = mVO.getReq_arri_date();
					strUrgent_level = mVO.getUrgent_level();
					strItem_code = mVO.getItem_code();
					strPk_trans_line = mVO.getPk_trans_line();
					strIf_return = mVO.getIf_return();
					List<PackInfo> unitPackInfos = mVO.getPackInfos();
					if(unitPackInfos != null && unitPackInfos.size() > 0){
						packInfos.addAll(unitPackInfos);
					}
				}
				//汇总包装明细
				if(packInfos != null && packInfos.size() > 0){
					Map<String,List<PackInfo>> packGroupMap = new HashMap<String, List<PackInfo>>();
					for(PackInfo info : packInfos){
						String pack = info.getPack();
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						if(packInfoList == null){
							packInfoList = new ArrayList<PackInfo>();
							packGroupMap.put(pack, packInfoList);
						}
						packInfoList.add(info);
					}
					List<PackInfo> temp = new ArrayList<PackInfo>();
					for(String pack : packGroupMap.keySet()){
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						PackInfo packInfo = new PackInfo();
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(PackInfo info : packInfoList){
							num = num + (info.getNum() == null ? 0 : info.getNum());
							weight = weight.add(info.getWeight() == null ? UFDouble.ZERO_DBL : info.getWeight());
							volume = volume.add(info.getVolume() == null ? UFDouble.ZERO_DBL : info.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						temp.add(packInfo);
					}
					packInfos = temp;
				}
				// 获取车型
				String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);
				
				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date,strUrgent_level,strItem_code,strPk_trans_line,strIf_return );

				// 重新计算金额
				List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(strPk_carrier,
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos, strPk_car_Type, strPk_corp,
						strUrgent_level,strItem_code,strPk_trans_line,strIf_return ,
						contractBVOs);

				// 过滤指定类型的应付明细实体类
				List<PayDetailBVO> newPdbVOs = getPayDetailBVOByExpenseType(pdbVOs, expenseType);
				
				//没有匹配到合同自动跳出，报异常。应该是按照每个分组分别计算合同的，所以放在大的循环内容，只有有一组没有匹配合同 就跳出，报异常。
				if(newPdbVOs==null||newPdbVOs.size()==0){
				
					throw new BusiException("批次号["+ matchVOs.get(0).getLot()+"],应付明细单号["+payDetailmatchVOs.get(0).getPd_vbillno() + "]没有匹配到提货段运费合同，请检查！");
				}

				for (PayDetailMatchVO mVO : payDetailmatchVOs) {

					for (PayDetailBVO pdbVO : newPdbVOs) {

						PayDetailBVO pdbVOTmp = new PayDetailBVO();
						if(fee_weight_count.equals(UFDouble.ZERO_DBL)){
							pdbVOTmp.setAmount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
							pdbVOTmp.setContract_amount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
						}else{
							pdbVOTmp.setAmount(pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));	
							pdbVOTmp.setContract_amount(
									pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));
						}
						pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
						pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
						pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
						pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
						pdbVOTmp.setSystem_create(UFBoolean.TRUE);
						pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
						pdbVOTmp.setPrice(pdbVO.getPrice());

						// 将合同明细的税种，税率冗余到这里
						pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
						pdbVOTmp.setTax_rate(pdbVO.getTax_rate());

						//增加车型吨位类型到备注上JONATHAN2015-10-29;
						pdbVOTmp.setMemo(pdbVO.getMemo());
						
						pdbVOTmp.setDr(0);
						pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
						pdbVOTmp.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(pdbVOTmp);

						InsertPdbVOs.add(pdbVOTmp);
					}
				}
			}
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, expenseType);
				
				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, expenseType);
			
		} else if (strTransType.equals(TransTypeConst.TT_GLZC)) {
			Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
			// 公路整车 过滤条件 收货城市+提货城市+ 同一订单号）
			for (PayDetailMatchVO matchVO : matchVOs) {
				//如果DEF7为Y 代表该委托单不计算费用。
				if(matchVO.getDef7()!=null && matchVO.getDef7().equals("Y")){
					continue;
				}
				String key = new StringBuffer().append(matchVO.getArri_city()).append(keyChar)
						.append(matchVO.getDeli_city()).toString();
				List<PayDetailMatchVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<PayDetailMatchVO>();
					groupMap.put(key, voList);
				}
				voList.add(matchVO);
			}
			logger.info("共分成" + groupMap.size() + "组");

			// 从分组内获取运输方式
			UFDouble total_fee_weight_count = UFDouble.ZERO_DBL;
			List<PayDetailBVO> maxPayDetailBVOs = new ArrayList<PayDetailBVO>();
			UFDouble maxAmount = UFDouble.ZERO_DBL;
			// 费用重新分摊后的应付费用明细
			List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
			for (String key : groupMap.keySet()) {
				List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
				if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
					continue;
				}

				// 合并计算
				Integer pack_num_count = 0;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				String strPk_carrier = "";
				String strPkEntrust = "";
				String strPk_corp = "";
				String strPk_trans_type = "";
				String strPk_delivery = "";
				String strPk_arrival = "";
				String strDeli_city = "";
				String strArri_city = "";
				String strReq_arri_date = "";
				Integer strUrgent_level = 0;
				String strItem_code = "";
				String strPk_trans_line = "";
				UFBoolean strIf_return = UFBoolean.FALSE;
				List<PackInfo> packInfos = new ArrayList<PackInfo>();
				for (PayDetailMatchVO mVO : payDetailmatchVOs) {
					pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count
							.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
					weight_count = weight_count
							.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
					volume_count = volume_count
							.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

					strPk_carrier = mVO.getPk_carrier();
					strPkEntrust = mVO.getPk_entrust();
					strPk_corp = mVO.getPk_corp();
					strPk_trans_type = mVO.getPk_trans_type();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
					strReq_arri_date = mVO.getReq_arri_date();
					strUrgent_level = mVO.getUrgent_level();
					strItem_code = mVO.getItem_code();
					strPk_trans_line = mVO.getPk_trans_line();
					strIf_return = mVO.getIf_return();
					List<PackInfo> unitPackInfos = mVO.getPackInfos();
					if(unitPackInfos != null && unitPackInfos.size() > 0){
						packInfos.addAll(unitPackInfos);
					}
					
				}
				//汇总包装明细
				if(packInfos != null && packInfos.size() > 0){
					Map<String,List<PackInfo>> packGroupMap = new HashMap<String, List<PackInfo>>();
					for(PackInfo info : packInfos){
						String pack = info.getPack();
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						if(packInfoList == null){
							packInfoList = new ArrayList<PackInfo>();
							packGroupMap.put(pack, packInfoList);
						}
						packInfoList.add(info);
					}
					List<PackInfo> temp = new ArrayList<PackInfo>();
					for(String pack : packGroupMap.keySet()){
						List<PackInfo> packInfoList = packGroupMap.get(pack);
						PackInfo packInfo = new PackInfo();
						Integer num = 0;
						UFDouble weight = UFDouble.ZERO_DBL;
						UFDouble volume = UFDouble.ZERO_DBL;
						for(PackInfo info : packInfoList){
							num = num + (info.getNum() == null ? 0 : info.getNum());
							weight = weight.add(info.getWeight() == null ? UFDouble.ZERO_DBL : info.getWeight());
							volume = volume.add(info.getVolume() == null ? UFDouble.ZERO_DBL : info.getVolume());
						}
						packInfo.setPack(key);
						packInfo.setNum(num);
						packInfo.setWeight(weight);
						packInfo.setVolume(volume);
						temp.add(packInfo);
					}
					packInfos = temp;
				}
				//累加计费重。
				total_fee_weight_count = total_fee_weight_count.add(fee_weight_count);
				// 获取车型
				String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);

				//如果车型为空，跳出此次循环。
				if(strPk_car_Type==null||strPk_car_Type.length==0){
					continue;
				}
				
				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date,strUrgent_level,strItem_code,strPk_trans_line,strIf_return );

				// 重新计算金额
				List<PayDetailBVO> pdbVOs = contractService.buildPayDetailBVO(strPk_carrier,
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos, strPk_car_Type, strPk_corp,
						strUrgent_level,strItem_code,strPk_trans_line,strIf_return ,
						contractBVOs);

				// 过滤指定类型的应付明细实体类
				List<PayDetailBVO> newPdbBVOs = getPayDetailBVOByExpenseType(pdbVOs, expenseType);

				//如果车型为空，跳出此次循环。
				if(newPdbBVOs==null||newPdbBVOs.size()==0){
					continue;
				}
				// 判断最大金额
				UFDouble contractAmount = UFDouble.ZERO_DBL;
				for (PayDetailBVO pdbBVO : newPdbBVOs) {
					contractAmount = contractAmount.add(pdbBVO.getAmount());
				}
				if (contractAmount.doubleValue() > maxAmount.doubleValue()) {
					maxAmount = contractAmount;
					maxPayDetailBVOs.clear();
					maxPayDetailBVOs.addAll(newPdbBVOs);
				}

			}
			//当所有分组匹配合同都为匹配到时，报异常，因为公路整车是按不同线路分组，分组后匹配合同比大小，所以只要有一个合同满足，就可以通过，基于该因素，将异常处理代码放在大的FOR 循环外面。
			if(maxPayDetailBVOs==null||maxPayDetailBVOs.size()==0){
				
				throw new BusiException("批次号["+ matchVOs.get(0).getLot()+"]没有匹配到送货费合同，请检查！");
			}

			for (PayDetailMatchVO mVO : matchVOs) {

				for (PayDetailBVO pdbVO : maxPayDetailBVOs) {

					PayDetailBVO pdbVOTmp = new PayDetailBVO();
					pdbVOTmp.setAmount(pdbVO.getAmount().div(total_fee_weight_count).multiply(mVO.getFee_weight_count()));
					pdbVOTmp.setContract_amount(
							pdbVO.getAmount().div(total_fee_weight_count).multiply(mVO.getFee_weight_count()));
					pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
					pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
					pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
					pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
					pdbVOTmp.setSystem_create(UFBoolean.TRUE);
					pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
					pdbVOTmp.setPrice(pdbVO.getPrice());
						// 将合同明细的税种，税率冗余到这里
					pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
					pdbVOTmp.setTax_rate(pdbVO.getTax_rate());
					
					//增加车型吨位类型到备注上JONATHAN2015-10-29;
					pdbVOTmp.setMemo(pdbVO.getMemo());

					pdbVOTmp.setDr(0);
					pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
					pdbVOTmp.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(pdbVOTmp);

					InsertPdbVOs.add(pdbVOTmp);
					}
				}
			
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, expenseType);

				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, expenseType);
			
		}
	}
	
	public void buildFX(List<PayDetailMatchVO> allMatchVOs, EntrustVO[] entVOs){
		
	} 
	
	/**
	 * 按批次计算费用
	 * 
	 * @param matchVOs
	 *            应付明细列表
	 * @param evtVOs
	 *            委托单
	 * @param expenseType
	 *            费用类型主键
	 * 
	 */
	public void computePayDetail(List<PayDetailMatchVO> matchVOs, EntrustVO[] entVOs) {	
		Map<String, List<PayDetailMatchVO>> groupMap = new HashMap<String, List<PayDetailMatchVO>>();
		// 获取运输方式
		String transTypeSql = "select * from ts_trans_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?";
		TransTypeVO transTypeVO = NWDao.getInstance().queryForObject(transTypeSql, TransTypeVO.class,TransTypeConst.TT_MR);
		for (PayDetailMatchVO matchVO : matchVOs) {
			String groupkey = new StringBuffer().append(matchVO.getLot()).append(keyChar).append(matchVO.getPk_corp())
					.toString();
			List<PayDetailMatchVO> voList = groupMap.get(groupkey);
			if (voList == null) {
				voList = new ArrayList<PayDetailMatchVO>();
				groupMap.put(groupkey, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");

		// 费用重新分摊后的应付费用明细
		List<PayDetailBVO> InsertPdbVOs = new ArrayList<PayDetailBVO>();
		for (String key : groupMap.keySet()) {
			List<PayDetailMatchVO> payDetailmatchVOs = groupMap.get(key);
			if (payDetailmatchVOs == null || payDetailmatchVOs.size() == 0) {
				continue;
			}
			// 合并计算
			Integer pack_num_count = 0;
			Integer num_count = 0;
			UFDouble fee_weight_count = UFDouble.ZERO_DBL;
			UFDouble weight_count = UFDouble.ZERO_DBL;
			UFDouble volume_count = UFDouble.ZERO_DBL;
			String strPk_carrier = "";
			String strPkEntrust = "";
			String strPk_corp = "";
			String strPk_trans_type = "";
			String strPk_delivery = "";
			String strPk_arrival = "";
			String strDeli_city = "";
			String strArri_city = "";
			String strReq_deli_date = "";
			String strReq_arri_date = "";
			Integer strUrgent_level = 0;
			String strItem_code = "";
			String strPk_trans_line = "";
			UFBoolean strIf_return = UFBoolean.FALSE;
			List<String> pk_entrusts = new ArrayList<String>();
			List<PayDetailBVO> pdbVOs = new ArrayList<PayDetailBVO>();
			List<PackInfo> packInfos = new ArrayList<PackInfo>();
			// 获取批次头信息
			for (PayDetailMatchVO mVO : payDetailmatchVOs) {
				pk_entrusts.add(mVO.getPk_entrust());
				pack_num_count = pack_num_count + (mVO.getPack_num_count() == null ? 0 : mVO.getPack_num_count());
				num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
				fee_weight_count = fee_weight_count
						.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO.getFee_weight_count());
				weight_count = weight_count
						.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO.getWeight_count());
				volume_count = volume_count
						.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO.getVolume_count());

				strPk_carrier = mVO.getPk_carrier();
				strPkEntrust = mVO.getPk_entrust();
				strPk_corp = mVO.getPk_corp();
				strPk_trans_type = mVO.getPk_trans_type();
				if (strReq_deli_date == "") {
					strReq_deli_date = mVO.getReq_deli_date();
					strReq_arri_date = mVO.getReq_arri_date();
					strPk_delivery = mVO.getPk_delivery();
					strPk_arrival = mVO.getPk_arrival();
					strDeli_city = mVO.getDeli_city();
					strArri_city = mVO.getArri_city();
				}
				// 获取最早提货日期和地址
				if (strReq_deli_date != ""
						&& DateUtils.getIntervalMillSeconds(strReq_deli_date, mVO.getReq_deli_date()) < 0) {
					strReq_deli_date = mVO.getReq_deli_date();
					strPk_delivery = mVO.getPk_delivery();
					strDeli_city = mVO.getDeli_city();
				}
				// 获取最晚到货日期和地址
				if (strReq_arri_date != ""
						&& DateUtils.getIntervalMillSeconds(strReq_arri_date, mVO.getReq_arri_date()) > 0) {
					strReq_arri_date = mVO.getReq_arri_date();
					strPk_arrival = mVO.getPk_arrival();
					strArri_city = mVO.getArri_city();
				}
				strUrgent_level = mVO.getUrgent_level();
				strItem_code = mVO.getItem_code();
				strPk_trans_line = mVO.getPk_trans_line();
				strIf_return = mVO.getIf_return();
				pk_entrusts.add(mVO.getPk_entrust());
				List<PackInfo> unitPackInfos = mVO.getPackInfos();
				if(unitPackInfos != null && unitPackInfos.size() > 0){
					packInfos.addAll(unitPackInfos);
				}
			}
			//汇总包装明细
			if(packInfos != null && packInfos.size() > 0){
				Map<String,List<PackInfo>> packGroupMap = new HashMap<String, List<PackInfo>>();
				for(PackInfo info : packInfos){
					String pack = info.getPack();
					List<PackInfo> packInfoList = packGroupMap.get(pack);
					if(packInfoList == null){
						packInfoList = new ArrayList<PackInfo>();
						packGroupMap.put(pack, packInfoList);
					}
					packInfoList.add(info);
				}
				List<PackInfo> temp = new ArrayList<PackInfo>();
				for(String pack : packGroupMap.keySet()){
					List<PackInfo> packInfoList = packGroupMap.get(pack);
					PackInfo packInfo = new PackInfo();
					Integer num = 0;
					UFDouble weight = UFDouble.ZERO_DBL;
					UFDouble volume = UFDouble.ZERO_DBL;
					for(PackInfo info : packInfoList){
						num = num + (info.getNum() == null ? 0 : info.getNum());
						weight = weight.add(info.getWeight() == null ? UFDouble.ZERO_DBL : info.getWeight());
						volume = volume.add(info.getVolume() == null ? UFDouble.ZERO_DBL : info.getVolume());
					}
					packInfo.setPack(pack);
					packInfo.setNum(num);
					packInfo.setWeight(weight);
					packInfo.setVolume(volume);
					temp.add(packInfo);
				}
				packInfos = temp;
			}
			// 获取车型
			String[] strPk_car_Type = getPkCarTypeByPkEntrust(strPkEntrust);

			if (transTypeVO != null && payDetailmatchVOs.get(0).getPk_trans_type().equals(transTypeVO.getPk_trans_type())) {
				// 如果运输方式为milkrun，只会存在一个批次号，这些所有的委托单都属于同一批次。
				// 对于milkrun运输，多数情况也只会有一个委托单
				String entCond = NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()]));
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
				logger.info("共分成" + groupMap.size() + "组");

				String expenseSql = "select * from ts_expense_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?";
				ExpenseTypeVO baseTypeVO = NWDao.getInstance().queryForObject(expenseSql, ExpenseTypeVO.class,
						ExpenseTypeConst.ET110);
				ExpenseTypeVO pointTypeVO = NWDao.getInstance().queryForObject(expenseSql, ExpenseTypeVO.class,
						ExpenseTypeConst.ET120);
				
				for (EntLineBVO entLineBVO : bases) {
					// 只有一个点说明这个应该是基费 开始匹配合同
					List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
							strPk_trans_type, entLineBVO.getPk_address(), null, entLineBVO.getPk_city(), null, strPk_corp,
							strReq_arri_date, strUrgent_level, strItem_code, strPk_trans_line,strIf_return );
					if (contractBVOs != null && contractBVOs.size() > 0) {
						for (ContractBVO contractBVO : contractBVOs) {
							// 只有结果为基费的合同才是我们需要的,并且合同设备类型需要相符
							if (contractBVO.getPk_expense_type().equals(baseTypeVO.getPk_expense_type())
									&& strPk_car_Type[0].equals(contractBVO.getEquip_type())) {
								List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
								// 只会存在一个合同
								unitContractBVO.add(contractBVO);
								//获取entPackB 
								EntLinePackBVO[] packBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLinePackBVO.class, "pk_ent_line_b=?", entLineBVO.getPk_ent_line_b());
								List<PackInfo> entPackInfos = new ArrayList<PackInfo>();
								if(packBVOs != null && packBVOs.length > 0){
									Map<String,List<EntLinePackBVO>> entGroupMap = new  HashMap<String,List<EntLinePackBVO>>();
									//对包装按照pack进行分组
									for(EntLinePackBVO linePackBVO : packBVOs){
										String pack = linePackBVO.getPack();
										if(StringUtils.isBlank(pack)){
											//没有包装的货品自动过滤
											continue;
										}
										List<EntLinePackBVO> voList = entGroupMap.get(pack);
										if(voList == null){
											voList = new ArrayList<EntLinePackBVO>();
											entGroupMap.put(pack, voList);
										}
										voList.add(linePackBVO);
									}
									if (groupMap.size() > 0) {
										for(String pack : entGroupMap.keySet()){
											PackInfo packInfo = new PackInfo();
											List<EntLinePackBVO> voList = entGroupMap.get(pack);
											Integer num = 0;
											UFDouble weight = UFDouble.ZERO_DBL;
											UFDouble volume = UFDouble.ZERO_DBL;
											for(EntLinePackBVO packBVO : voList){
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
								// 重新计算金额
								List<PayDetailBVO> 	unitpdbVOs = contractService.buildPayDetailBVO(strPk_carrier, pack_num_count.doubleValue(), num_count,
										fee_weight_count.doubleValue(), weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos,
										strPk_car_Type, strPk_corp, strUrgent_level, strItem_code, strPk_trans_line,strIf_return , unitContractBVO);
								if(unitpdbVOs == null || unitpdbVOs.size() == 0){
									continue;
								}
								unitpdbVOs.get(0).setPrice(unitpdbVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL
										: unitpdbVOs.get(0).getAmount());
								pdbVOs.addAll(unitpdbVOs);
							}
						}
					}
				}
				for (String pointKey : pointGroupMap.keySet()) {
					List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
							strPk_trans_type, pointGroupMap.get(pointKey).get(0).getPk_address(), null, pointGroupMap.get(pointKey).get(0).getPk_city(), null, strPk_corp,
							strReq_arri_date, strUrgent_level, strItem_code, strPk_trans_line,strIf_return );
					if (contractBVOs != null && contractBVOs.size() > 0) {
						for (ContractBVO contractBVO : contractBVOs) {
							// 只有结果为点位费的合同才是我们需要的,并且合同设备类型需要相符
							if (contractBVO.getPk_expense_type().equals(pointTypeVO.getPk_expense_type())
									&& strPk_car_Type[0].equals(contractBVO.getEquip_type())) {
								List<ContractBVO> unitContractBVO = new ArrayList<ContractBVO>();
								unitContractBVO.add(contractBVO);
								List<PayDetailBVO> 	unitpdbVOs = contractService.buildPayDetailBVO(strPk_carrier, pack_num_count.doubleValue(), num_count,
										fee_weight_count.doubleValue(), weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,null,
										strPk_car_Type, strPk_corp, strUrgent_level, strItem_code, strPk_trans_line, strIf_return ,unitContractBVO);
								if(unitpdbVOs == null || unitpdbVOs.size() == 0){
									continue;
								}
								unitpdbVOs.get(0).setPrice(unitpdbVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL
										: unitpdbVOs.get(0).getAmount());
								unitpdbVOs.get(0).setAmount(unitpdbVOs.get(0).getAmount() == null ? UFDouble.ZERO_DBL
										: unitpdbVOs.get(0).getAmount().multiply(pointGroupMap.get(pointKey).size()));
								unitpdbVOs.get(0).setContract_amount(unitpdbVOs.get(0).getAmount());
								pdbVOs.addAll(unitpdbVOs);
							}
						}
					}
				}
			} else {

				// 匹配合同
				List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CARRIER, strPk_carrier,
						strPk_trans_type, strPk_delivery, strPk_arrival, strDeli_city, strArri_city, strPk_corp,
						strReq_arri_date, strUrgent_level, strItem_code, strPk_trans_line,strIf_return );

				// 重新计算金额
				pdbVOs = contractService.buildPayDetailBVO(strPkEntrust,strPk_carrier, pack_num_count.doubleValue(), num_count,
						fee_weight_count.doubleValue(), weight_count.doubleValue(), volume_count.doubleValue(), 0, 0,packInfos,
						strPk_car_Type, strPk_corp, strUrgent_level, strItem_code, strPk_trans_line,strIf_return , contractBVOs);
			}

			// 过滤指定类型的应付明细实体类
			List<PayDetailBVO> newPdbVOs = new ArrayList<PayDetailBVO>();
			if(pdbVOs != null && pdbVOs.size() > 0){
				for(PayDetailBVO pdbVO : pdbVOs){
					if(!pdbVO.getPk_expense_type().equals("9436f31e58fc44d1981471b4c2d50e95")){
						newPdbVOs.add(pdbVO);
					}
				}
			}
			//List<PayDetailBVO> newPdbVOs = pdbVOs;

			// 没有匹配到合同自动跳出，报异常。应该是按照每个分组分别计算合同的，所以放在大的循环内容，只有有一组没有匹配合同 就跳出，报异常。
			if (newPdbVOs == null || newPdbVOs.size() == 0) {

				logger.info("批次号" + matchVOs.get(0).getLot() + ",应付明细单号" + payDetailmatchVOs.get(0).getPd_vbillno()
						+ "没有匹配到提货段运费合同，请检查");
				// throw new BusiException("批次号" + matchVOs.get(0).getLot() +
				// ",应付明细单号"
				// + payDetailmatchVOs.get(0).getPd_vbillno() +
				// "没有匹配到提货段运费合同，请检查");
			} else {
				if(payDetailmatchVOs.size() == 1){//只有一个单子，就不用分摊了。
					for (PayDetailBVO pdbVO : newPdbVOs){
						pdbVO.setPk_pay_detail(payDetailmatchVOs.get(0).getPk_pay_detail());
						pdbVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(pdbVO);
						pdbVO.setSystem_create(UFBoolean.TRUE);
					}
					
					InsertPdbVOs.addAll(newPdbVOs);
				}else {
					for (PayDetailMatchVO mVO : payDetailmatchVOs) {
						for (PayDetailBVO pdbVO : newPdbVOs) {
							PayDetailBVO pdbVOTmp = new PayDetailBVO();
							if (fee_weight_count.equals(UFDouble.ZERO_DBL)) {
								pdbVOTmp.setAmount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
								pdbVOTmp.setContract_amount(pdbVO.getAmount().div(payDetailmatchVOs.size()));
							} else {
								pdbVOTmp.setAmount(
										pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));
								pdbVOTmp.setContract_amount(
										pdbVO.getAmount().div(fee_weight_count).multiply(mVO.getFee_weight_count()));
							}
							pdbVOTmp.setPk_expense_type(pdbVO.getPk_expense_type()); // 费用类型
							pdbVOTmp.setQuote_type(pdbVO.getQuote_type());// 报价类型
							pdbVOTmp.setPrice_type(pdbVO.getPrice_type());// 价格类型
							pdbVOTmp.setValuation_type(pdbVO.getValuation_type());// 计价方式
							pdbVOTmp.setSystem_create(UFBoolean.TRUE);
							pdbVOTmp.setPk_contract_b(pdbVO.getPk_contract_b());
							pdbVOTmp.setPrice(pdbVO.getPrice());
							pdbVOTmp.setBill_value(pdbVO.getBill_value());
							pdbVOTmp.setCost(pdbVO.getCost());
							pdbVOTmp.setContract_cost(pdbVO.getContract_cost());
							// 将合同明细的税种，税率冗余到这里
							pdbVOTmp.setTax_cat(pdbVO.getTax_cat());
							pdbVOTmp.setTax_rate(pdbVO.getTax_rate());

							// 增加车型吨位类型到备注上JONATHAN2015-10-29;
							pdbVOTmp.setMemo(pdbVO.getMemo());

							pdbVOTmp.setDr(0);
							pdbVOTmp.setPk_pay_detail(mVO.getPk_pay_detail());
							pdbVOTmp.setStatus(VOStatus.NEW);
							NWDao.setUuidPrimaryKey(pdbVOTmp);

							InsertPdbVOs.add(pdbVOTmp);
						}
					}
				}
		
				// 删除系统创建的委托单的费用明细
				List<PayDetailBVO> oldPdbVOs = deletePayDetail(InsertPdbVOs, "");

				// 更新委托单及费用明细
				updateEntrustAndPd(matchVOs, oldPdbVOs, InsertPdbVOs, entVOs, "");
			}
		}
	}

	
	
	private String[] getPkCarTypeByPkEntrust(String strPKEntrust) {
		// 获取与委托单相关联的车辆类型，多辆车
		EntTransbilityBVO[] tbBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
				"pk_entrust=?", strPKEntrust);
		String[] pk_car_type = null;
		if (tbBVOs != null && tbBVOs.length > 0) {
			pk_car_type = new String[tbBVOs.length];
			for (int i = 0; i < tbBVOs.length; i++) {
				pk_car_type[i] = tbBVOs[i].getPk_car_type();
			}
		}

		return pk_car_type;
	}

	/**
	 * 删除旧的系统创建的费用明细，这里不真正的删除，只是标记删除状态，后面再删除
	 * 
	 * @param matchVOs
	 */
	private List<PayDetailBVO> deletePayDetail(List<PayDetailBVO> inPdbVOs, String strExpenseType) {
		// 将指定的费用类型明细删除
		logger.info("将相关的委托单的费用明细中为系统创建的删除");
		if(inPdbVOs==null||inPdbVOs.size()==0){
			return null;
		}	
		//增加不指定费用类型情况，当不指定费用类型时，则删除所有相关费用。2015-12-27 lanjian
		PayDetailBVO[] pdBVOs = null;
		if(!StringUtils.isBlank(strExpenseType)){
			pdBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
					"pk_pay_detail in " + getPdPkDetailCond(inPdbVOs) + " and pk_expense_type ='" + strExpenseType +"'");
		}
		else{
			pdBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,
					"pk_pay_detail in " + getPdPkDetailCond(inPdbVOs));
		}	
		for (PayDetailBVO rdBVO : pdBVOs) {
			if (rdBVO.getSystem_create() != null && rdBVO.getSystem_create().booleanValue()) {
				rdBVO.setStatus(VOStatus.DELETED);
			}
		}
		List<PayDetailBVO> listPdbVOs = new ArrayList<PayDetailBVO>();
		for (PayDetailBVO pdbVO : pdBVOs) {
			listPdbVOs.add(pdbVO);
		}

		return listPdbVOs;
	}

	/**
	 * 更新委托单以及更新费用明细主表
	 */
	public void updateEntrustAndPd(List<PayDetailMatchVO> matchVOs, List<PayDetailBVO> oldPdbVOs,
			List<PayDetailBVO> newPdbVOs, EntrustVO[] entVOs, String strExpenseType) {
		Map<String, List<PayDetailBVO>> oldMap = new HashMap<String, List<PayDetailBVO>>();
		Map<String, List<PayDetailBVO>> newMap = new HashMap<String, List<PayDetailBVO>>();
		if (oldPdbVOs != null) {
			
			for (PayDetailBVO oldVO : oldPdbVOs) {
				List<PayDetailBVO> list = oldMap.get(oldVO.getPk_pay_detail());
				if (list == null) {
					list = new ArrayList<PayDetailBVO>();
					oldMap.put(oldVO.getPk_pay_detail(), list);
				}
				list.add(oldVO);
			}
		}
		if (newPdbVOs != null) {
			//NWDao.getInstance().saveOrUpdate(newPdbVOs.toArray(new PayDetailBVO[newPdbVOs.size()]));
			for (PayDetailBVO newVO : newPdbVOs) {
				List<PayDetailBVO> list = newMap.get(newVO.getPk_pay_detail());
				if (list == null) {
					list = new ArrayList<PayDetailBVO>();
					newMap.put(newVO.getPk_pay_detail(), list);
				}
				list.add(newVO);
			}
		}

		// 批量更新 VO
		List<SuperVO> updateList = new ArrayList<SuperVO>();

		// vbillno:entrust_vbillno
		Map<String, EntrustVO> entMap = new HashMap<String, EntrustVO>();
		List<String> entrustVbillnos = new ArrayList<String>();
		for (EntrustVO entVO : entVOs) {
			entMap.put(entVO.getVbillno(), entVO);
			//获取委托单号，用来计算应收表头成本2015-12-31 lanjian
			entrustVbillnos.add(entVO.getVbillno());
		}

		List<PayDetailVO> pdVOs= new  ArrayList<PayDetailVO>();
		for (PayDetailMatchVO matchVO : matchVOs) {
			EntrustVO entVO = entMap.get(matchVO.getEntrust_villno());
			// 得到应付明细主表
			PayDetailVO pdVO = NWDao.getInstance().queryByCondition(PayDetailVO.class, "Pk_pay_detail=?",
					matchVO.getPk_pay_detail());
			List<PayDetailBVO> oldAndNewVOs = new ArrayList<PayDetailBVO>();
			List<PayDetailBVO> oldVOs = oldMap.get(matchVO.getPk_pay_detail());
			List<PayDetailBVO> newVOs = newMap.get(matchVO.getPk_pay_detail());
			
			if(oldVOs != null){
				oldAndNewVOs.addAll(oldVOs);
			}
			else if(newVOs != null){
				oldAndNewVOs.addAll(newVOs);
			}
			
			// 合计金额到表头
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			if (newVOs != null) {
				for (PayDetailBVO newVO : newVOs) {
					pdVO.setTax_cat(newVO.getTax_cat());
					pdVO.setTax_rate(newVO.getTax_rate());
					cost_amount = cost_amount.add(newVO.getAmount());
				}
			}
			if (oldVOs != null) {
				for (PayDetailBVO oldVO : oldVOs) {
					if (oldVO.getStatus() != VOStatus.DELETED) {
						cost_amount = cost_amount.add(oldVO.getAmount());
					}
				}
			}

			logger.info("将相关的委托单的def4置为Y,并合计金额");
			entVO.setCost_amount(cost_amount);
			entVO.setDef4(Constants.Y);
			entVO.setStatus(VOStatus.UPDATED);
			updateList.add(entVO);

			if(newVOs!=null&&newVOs.size()>0){
				// 重新计算分摊费用
				List<PayDeviBVO> deviBVOs = new ArrayList<PayDeviBVO>();
				if(!StringUtils.isBlank(strExpenseType)){
					 deviBVOs = PZUtils.getPayDeviBVOs(entVO, null, null, newVOs.toArray(new PayDetailBVO[newVOs.size()]), strExpenseType);
				}else{
					deviBVOs = PZUtils.getPayDeviBVOs(entVO, null, null, newVOs.toArray(new PayDetailBVO[newVOs.size()]));
				}	
				updateList.addAll(deviBVOs);
			}
			//应付明细主表清单
			pdVOs.add(pdVO);

		} // end for
			// 增加新增费用明细
		
		if(newPdbVOs!=null&&newPdbVOs.size()>0){
			updateList.addAll(newPdbVOs);
		}
		else{
			return;
		}
		
		if(oldPdbVOs!=null&&oldPdbVOs.size()>0){
			updateList.addAll(oldPdbVOs);
		}

		if(updateList!=null&&updateList.size()>0) {
			NWDao.getInstance().saveOrUpdate(updateList);
			logger.info("合并金额到应付明细主表");
			// 更新总金额
			CMUtils.processExtenal(pdVOs);
			//更新应收明细表头成本信息2015-12-31 lanjian
			String entCond = NWUtils.buildConditionString(entrustVbillnos.toArray(new String[entrustVbillnos.size()]));
			String sql = "SELECT inv.* FROM ts_invoice inv LEFT JOIN ts_ent_inv_b ei WITH(NOLOCK) ON inv.pk_invoice = ei.pk_invoice "
					+ " LEFT JOIN ts_entrust ent WITH(NOLOCK) ON ent.pk_entrust = ei.pk_entrust "
					+ " WHERE isnull(ei.dr,0)=0 AND isnull(ent.dr,0)=0 AND isnull(inv.dr,0)=0 "
					+ " AND ent.vbillno in "
					+ entCond ;
			List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class);
			CMUtils.totalCostComput(invoiceVOs);
		}
	}

	/**
	 * 返回公司的费用类型对照，包括公司和集团的并集
	 * 
	 * @param pk_corp
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, String> getExpenseTypeMap(String pk_corp) {
		Map<String, String> expenseTypeMap = corpExpenseTypeMap.get(pk_corp);
		if (expenseTypeMap != null) {
			return expenseTypeMap;
		} else {
			expenseTypeMap = new HashMap<String, String>();
			corpExpenseTypeMap.put(pk_corp, expenseTypeMap);
		}
		// 查询费用类型
		String sql = "select pk_expense_type,parent_type from ts_expense_type where isnull(dr,0)=0 and (pk_corp=? or pk_corp=?)";
		List<HashMap> list = NWDao.getInstance().queryForList(sql, HashMap.class, pk_corp, Constants.SYSTEM_CODE);
		if (list != null) {
			for (HashMap map : list) {
				String pk_expense_type = String.valueOf(map.get("pk_expense_type"));
				String parent_type = String.valueOf(map.get("parent_type"));
				expenseTypeMap.put(pk_expense_type, parent_type);
			}
		}
		return expenseTypeMap;
	}

	/**
	 * 返回公司的费用类型对照，包括公司和集团的并集
	 * 
	 * @param pk_corp
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, String> getExpenseTypeCodeMap(String pk_corp) {
		Map<String, String> expenseTypeCodeMap = corpExpenseTypeCodeMap.get(pk_corp);
		if (expenseTypeCodeMap != null) {
			return expenseTypeCodeMap;
		} else {
			expenseTypeCodeMap = new HashMap<String, String>();
			corpExpenseTypeCodeMap.put(pk_corp, expenseTypeCodeMap);
		}
		// 查询费用类型
		String sql = "select pk_expense_type,code from ts_expense_type where isnull(dr,0)=0 and (pk_corp=? or pk_corp=?)";
		List<HashMap> list = NWDao.getInstance().queryForList(sql, HashMap.class, pk_corp, Constants.SYSTEM_CODE);
		if (list != null) {
			for (HashMap map : list) {
				String pk_expense_type = String.valueOf(map.get("pk_expense_type"));
				String code = String.valueOf(map.get("code"));
				expenseTypeCodeMap.put(pk_expense_type, code);
			}
		}
		return expenseTypeCodeMap;
	}

	// 在应付明细列表中获取应付 pk_pay_detail 2015-10-26
	private String getPdPkDetailCond(List<PayDetailBVO> pdbVOs) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0; i < pdbVOs.size(); i++) {
			buf.append("'");
			buf.append(pdbVOs.get(i).getPk_pay_detail());
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}

	// 在应付明细列表中获取应付 pk_pay_detail 2015-10-26
	private String getPdPkDetailCondFromMatchVO(List<PayDetailMatchVO> matchVOs) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0; i < matchVOs.size(); i++) {
			buf.append("'");
			buf.append(matchVOs.get(i).getPk_pay_detail());
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}

	public void before(JobDefVO jobDefVO) {
		init();
	}
	//初始化ContractService  2015-10-27
	private void init() {
		contractService = SpringContextHolder.getBean("contractServiceImpl");
		if(contractService == null) {
			throw new BusiException("合同服务没有启动，服务ID：contractServiceImpl");
		}
	}
	
	public void after(JobDefVO jobDefVO) {
		corpExpenseTypeMap.clear();
		corpExpenseTypeCodeMap.clear();

	}

	public void exec(JobDefVO jobDefVO) {
	}


}
