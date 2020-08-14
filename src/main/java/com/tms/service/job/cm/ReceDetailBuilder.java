/**
 * 
 */
package com.tms.service.job.cm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.job.IJobService;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.utils.SpringContextHolder;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.cm.ContractService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggOrderlotVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.OrderlotDeviVO;
import com.tms.vo.inv.OrderlotInvVO;
import com.tms.vo.inv.OrderlotRdVO;
import com.tms.vo.inv.OrderlotVO;

/**
 * 自动计算发货单的应收明细
 * 
 * @author xuqc
 * @Date 2015年6月3日 下午2:44:54
 *
 */
public class ReceDetailBuilder implements IJobService {

	Logger logger = Logger.getLogger(this.getClass());

	private ContractService contractService = null;
	private ExAggOrderlotVO curAggVO = null;// 如果是修改的情况，此时需要设置curAggVO;
	private boolean viewOnly = false;
	private HashMap<String, Map<String, String>> corpExpenseTypeMap = new HashMap<String, Map<String, String>>();
	private HashMap<String, Map<String, String>> corpExpenseTypeCodeMap = new HashMap<String, Map<String, String>>();
	private String[] supplierAry = new String[] { "白城中一精锻股份有限公司", "长春富奥石川岛增压器有限公司", "一汽铸造有限公司铸造二厂" };
	private List<String> supplierList = null;
	private String keyChar = "|";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobService#before(org.nw.vo.sys.JobDefVO)
	 */
	public void before(JobDefVO jobDefVO) {
		init();
	}

	private void init() {
		contractService = SpringContextHolder.getBean("contractServiceImpl");
		if(contractService == null) {
			throw new BusiException("合同服务没有启动，服务ID：contractServiceImpl");
		}
		String cond = NWUtils.buildConditionString(supplierAry);
		String sql = "select pk_supplier from ts_supplier WITH(NOLOCK)  where isnull(dr,0)=0 and supp_code in " + cond;
		supplierList = NWDao.getInstance().queryForList(sql, String.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobService#exec(org.nw.vo.sys.JobDefVO)
	 */
	public void exec(JobDefVO jobDefVO) {
		
	}




	/**
	 * 对提货段的发货单进行处理
	 */
	public List<ExAggOrderlotVO> buildTHD(List<MatchVO> allMatchVOs) {
		logger.info("共查询到" + allMatchVOs.size() + "条相关的发货单");
//		logger.info("对发货单按相同运输方式+结算客户+辅助表的相同供应商+辅助表的相同收货方+辅助表的提货方+辅助表的提货城市+辅助表的相同要求到货日期+辅助表的相同实际提货日期，进行分组");
		logger.info("对发货单按相同运输方式+结算客户+发货单的相同供应商+发货单的相同收货方+发货单的提货方+发货单的提货城市+发货单的相同要求到货日期+发货单的相同实际提货日期，进行分组");
		Map<String, List<MatchVO>> groupMap = new HashMap<String, List<MatchVO>>();
		for(MatchVO matchVO : allMatchVOs) {
			String key = new StringBuffer().append(matchVO.getPk_trans_type()).append(keyChar)
					.append(matchVO.getBala_customer()).append(keyChar).append(matchVO.getPk_supplier())
					.append(keyChar).append(matchVO.getPk_arrival()).append(keyChar)
					.append(matchVO.getAct_deli_date()).append(keyChar).append(matchVO.getReq_arri_date())
					.append(keyChar).append(matchVO.getPk_delivery()).append(keyChar)
					.append(matchVO.getDeli_city()).append(keyChar).append(matchVO.getPk_corp()).toString();
			List<MatchVO> voList = groupMap.get(key);
			if(voList == null) {
				voList = new ArrayList<MatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");
		List<ExAggOrderlotVO> aggVOs = new ArrayList<ExAggOrderlotVO>();
		for(String key : groupMap.keySet()) {
			List<MatchVO> matchVOs = groupMap.get(key);
			if(matchVOs == null || matchVOs.size() == 0) {
				continue;
			}

			MatchVO matchVO = matchVOs.get(0);
			logger.info("开始匹配合同,匹配参数：" + key);
			List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER,
					matchVO.getBala_customer(), matchVO.getPk_trans_type(), matchVO.getPk_delivery(),
					matchVO.getPk_arrival(), matchVO.getDeli_city(), matchVO.getArri_city(),
					matchVO.getPk_corp(), matchVO.getReq_arri_date(),matchVO.getUrgent_level(),matchVO.getItem_code(),
					matchVO.getPk_trans_line(),matchVO.getIf_return());
			// 更新批次信息，以及匹配后的应收费用明细
			ExAggOrderlotVO aggVO = build(matchVOs, contractBVOs, SegmentConst.SEG_TYPE_THD,
					DataDictConst.EXPENSE_PARENTTYPE.THD.intValue(), false);
			aggVOs.add(aggVO);
		}
		return aggVOs;
	}

	/**
	 * 处理干线段
	 */
	public List<ExAggOrderlotVO> buildGXD(List<MatchVO> allMatchVOs) {
		logger.info("查询需要重新计算应收费用的发货单");
		logger.info("共查询到" + allMatchVOs.size() + "条相关的发货单");
		logger.info("对发货单按相同运输方式+结算客户+辅助表的相同供应商+辅助表的相同收货方+辅助表的提货方+辅助表的提货城市+辅助表的相同要求到货日期+辅助表的相同实际提货日期，进行分组");
		Map<String, List<MatchVO>> groupMap = new HashMap<String, List<MatchVO>>();
		for(int i=0;i<allMatchVOs.size();i++){
			MatchVO matchVO = allMatchVOs.get(i);
			String key = new StringBuffer().append(matchVO.getPk_trans_type()).append(keyChar)
					.append(matchVO.getBala_customer()).append(keyChar).append(matchVO.getPk_supplier())
					.append(keyChar).append(matchVO.getPk_arrival()).append(keyChar)
					.append(matchVO.getAct_deli_date()).append(keyChar).append(matchVO.getReq_arri_date())
					.append(keyChar).append(matchVO.getPk_delivery()).append(keyChar)
					.append(matchVO.getDeli_city()).append(keyChar).append(matchVO.getPk_corp())
					//干线段计算应分开单独计算
					.append(keyChar).append(i)
					.toString();
			List<MatchVO> voList = groupMap.get(key);
			if(voList == null) {
				voList = new ArrayList<MatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");
		List<ExAggOrderlotVO> aggVOs = new ArrayList<ExAggOrderlotVO>();
		for(String key : groupMap.keySet()) {
			List<MatchVO> matchVOs = groupMap.get(key);
			if(matchVOs == null || matchVOs.size() == 0) {
				continue;
			}

			MatchVO matchVO = matchVOs.get(0);
			logger.info("开始匹配合同,匹配参数：" + key);
			List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER,
					matchVO.getBala_customer(), matchVO.getPk_trans_type(), matchVO.getPk_delivery(),
					matchVO.getPk_arrival(), matchVO.getDeli_city(), matchVO.getArri_city(),
					matchVO.getPk_corp(), matchVO.getReq_arri_date(),matchVO.getUrgent_level(),
					matchVO.getItem_code(),matchVO.getPk_trans_line(),matchVO.getIf_return());
			// 更新批次信息，以及匹配后的应收费用明细
			ExAggOrderlotVO aggVO = build(matchVOs, contractBVOs, SegmentConst.SEG_TYPE_GXD,
					DataDictConst.EXPENSE_PARENTTYPE.GXD.intValue(), false);
			aggVOs.add(aggVO);
		}
		return aggVOs;
	}

	/**
	 * 处理送货段的发货单
	 */
	public List<ExAggOrderlotVO> buildSHD(List<MatchVO> allMatchVOs) {
		logger.info("查询需要重新计算应收费用的发货单");
		logger.info("共查询到" + allMatchVOs.size() + "条相关的发货单");
		logger.info("对发货单按相同运输方式+结算客户+辅助表的相同供应商+辅助表的相同收货方+辅助表的提货方+辅助表的提货城市+辅助表的相同要求到货日期+辅助表的相同实际提货日期，进行分组");
		Map<String, List<MatchVO>> groupMap = new HashMap<String, List<MatchVO>>();
		for(MatchVO matchVO : allMatchVOs) {
			
			//songf 2015-10-19修改送货段费用分组逻辑
			/*String key = new StringBuffer().append(matchVO.getPk_trans_type()).append(keyChar)
					.append(matchVO.getBala_customer()).append(keyChar).append(matchVO.getPk_supplier_ass())
					.append(keyChar).append(matchVO.getPk_arrival_ass()).append(keyChar)
					.append(matchVO.getAct_deli_date_ass()).append(keyChar).append(matchVO.getReq_arri_date_ass())
					.append(keyChar).append(matchVO.getPk_delivery_ass()).append(keyChar)
					.append(matchVO.getDeli_city_ass()).append(keyChar).append(matchVO.getPk_corp()).toString();
			*/
		    //修改后的分组逻辑  2015-10-19
			//相同起始地+相同目的地+相同送货优先级（def6）+实际到货时期
			String key = new StringBuffer().append(matchVO.getPk_trans_type()).append(keyChar)
					.append(matchVO.getBala_customer()).append(keyChar).append(matchVO.getPk_delivery())
					.append(keyChar).append(matchVO.getPk_arrival()).append(keyChar).append(DateUtils.formatDate(matchVO.getAct_arri_date(), DateUtils.DATEFORMAT_HORIZONTAL)).append(keyChar).append(matchVO.getPk_corp())
					.append(keyChar).append(matchVO.getDef6())
					.toString();
			List<MatchVO> voList = groupMap.get(key);
			if(voList == null) {
				voList = new ArrayList<MatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");
		List<ExAggOrderlotVO> aggVOs = new ArrayList<ExAggOrderlotVO>();
		for(String key : groupMap.keySet()) {
			List<MatchVO> matchVOs = groupMap.get(key);
			if(matchVOs == null || matchVOs.size() == 0) {
				continue;
			}

			MatchVO matchVO = matchVOs.get(0);
			logger.info("开始匹配合同,匹配参数：" + key);
			
			//songf 2015-10-19修改合同匹配分组逻辑
			/*List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER,
					matchVO.getBala_customer(), matchVO.getPk_trans_type(), matchVO.getPk_delivery_ass(),
					matchVO.getPk_arrival_ass(), matchVO.getDeli_city_ass(), matchVO.getArri_city_ass(),
					matchVO.getPk_corp(), matchVO.getReq_arri_date_ass());
			*/
			
			 //修改后的分组逻辑  2015-10-19
			List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER,
					matchVO.getBala_customer(), matchVO.getPk_trans_type(), matchVO.getPk_delivery(),
					matchVO.getPk_arrival(), matchVO.getDeli_city(), matchVO.getArri_city(), matchVO.getPk_corp(),
					matchVO.getReq_arri_date(),matchVO.getUrgent_level(),matchVO.getItem_code(),
					matchVO.getPk_trans_line(),matchVO.getIf_return());
			
			// 更新批次信息，以及匹配后的应收费用明细
			ExAggOrderlotVO aggVO = build(matchVOs, contractBVOs, SegmentConst.SEG_TYPE_SHD,
					DataDictConst.EXPENSE_PARENTTYPE.SHD.intValue(), false);
			aggVOs.add(aggVO);
		}
		return aggVOs;
	}

	public List<ExAggOrderlotVO> buildFX(List<MatchVO> allMatchVOs) {
		logger.info("查询需要重新计算应收费用的发货单");
		logger.info("共查询到" + allMatchVOs.size() + "条相关的发货单");
		logger.info("对发货单按相同运输方式+结算客户+辅助表的相同供应商+相同收货方+相同要求到货日期+相同实际提货日期，进行分组");
		Map<String, List<MatchVO>> groupMap = new HashMap<String, List<MatchVO>>();
		for(MatchVO matchVO : allMatchVOs) {
			String key = new StringBuffer().append(matchVO.getPk_trans_type()).append(keyChar)
					.append(matchVO.getBala_customer()).append(keyChar).append(matchVO.getPk_delivery())
					.append(keyChar).append(matchVO.getPk_arrival()).append(keyChar).append(matchVO.getAct_deli_date())
					.append(keyChar).append(matchVO.getReq_arri_date()).append(keyChar).append(matchVO.getPk_corp())
					.toString();
			List<MatchVO> voList = groupMap.get(key);
			if(voList == null) {
				voList = new ArrayList<MatchVO>();
				groupMap.put(key, voList);
			}
			voList.add(matchVO);
		}
		logger.info("共分成" + groupMap.size() + "组");
		List<ExAggOrderlotVO> aggVOs = new ArrayList<ExAggOrderlotVO>();
		for(String key : groupMap.keySet()) {
			List<MatchVO> matchVOs = groupMap.get(key);
			if(matchVOs == null || matchVOs.size() == 0) {
				continue;
			}

			MatchVO matchVO = matchVOs.get(0);
			logger.info("开始匹配合同,匹配参数：" + key);
			
			List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER,
					matchVO.getBala_customer(), matchVO.getPk_trans_type(), matchVO.getPk_delivery(),
					matchVO.getPk_arrival(), matchVO.getDeli_city(), matchVO.getArri_city(), matchVO.getPk_corp(),
					matchVO.getReq_arri_date(),matchVO.getUrgent_level(),matchVO.getItem_code(),
					matchVO.getPk_trans_line(),matchVO.getIf_return());
			// 更新批次信息，以及匹配后的应收费用明细
			ExAggOrderlotVO aggVO = build(matchVOs, contractBVOs, SegmentConst.SEG_TYPE_THD,
					DataDictConst.EXPENSE_PARENTTYPE.THD.intValue(), true);
			aggVOs.add(aggVO);
		}
		return aggVOs;
	}

	/**
	 * 删除旧的系统创建的费用明细，这里不真正的删除，只是标记删除状态，后面再删除
	 * 
	 * @param matchVOs
	 */
	private ReceDetailBVO[] deleteReceiveDetail(List<MatchVO> matchVOs) {
		// 删除系统创建的费用明细
		logger.info("将相关的发货单的费用明细中为系统创建的删除");
		ReceDetailBVO[] rdBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(
				ReceDetailBVO.class,
				"pk_receive_detail in (select pk_receive_detail from ts_receive_detail WITH(NOLOCK) where vbillno in "
						+ getRdVbillnoCond(matchVOs) + ")");
		for(ReceDetailBVO rdBVO : rdBVOs) {
			if(rdBVO.getSystem_create() != null && rdBVO.getSystem_create().booleanValue()) {
				rdBVO.setStatus(VOStatus.DELETED);
			}
		}
		return rdBVOs;
	}

	/**
	 * 更新发货单，以及更新费用明细主表
	 */
	public void updateInvoiceAndRd(List<MatchVO> matchVOs, ReceDetailBVO[] oldRdbVOs, List<ReceDetailBVO> newRdbVOs,
			InvoiceVO[] invVOs) {
		Map<String, List<ReceDetailBVO>> oldMap = new HashMap<String, List<ReceDetailBVO>>();
		Map<String, List<ReceDetailBVO>> newMap = new HashMap<String, List<ReceDetailBVO>>();
		if(oldRdbVOs != null) {
			NWDao.getInstance().saveOrUpdate(oldRdbVOs);
			for(ReceDetailBVO oldVO : oldRdbVOs) {
				List<ReceDetailBVO> list = oldMap.get(oldVO.getPk_receive_detail());
				if(list == null) {
					list = new ArrayList<ReceDetailBVO>();
					oldMap.put(oldVO.getPk_receive_detail(), list);
				}
				list.add(oldVO);
			}
		}
		if(newRdbVOs != null) {
			NWDao.getInstance().saveOrUpdate(newRdbVOs.toArray(new ReceDetailBVO[newRdbVOs.size()]));
			for(ReceDetailBVO newVO : newRdbVOs) {
				List<ReceDetailBVO> list = newMap.get(newVO.getPk_receive_detail());
				if(list == null) {
					list = new ArrayList<ReceDetailBVO>();
					newMap.put(newVO.getPk_receive_detail(), list);
				}
				list.add(newVO);
			}
		}

		// vbillno:invoicevo
		Map<String, InvoiceVO> invMap = new HashMap<String, InvoiceVO>();
		for(InvoiceVO invVO : invVOs) {
			invMap.put(invVO.getVbillno(), invVO);
		}

		for(MatchVO matchVO : matchVOs) {
			InvoiceVO invVO = invMap.get(matchVO.getInvoice_vbillno());
			// 得到应收明细主表
			ReceiveDetailVO rdVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
					matchVO.getPk_receive_detail());
			List<ReceDetailBVO> oldVOs = oldMap.get(matchVO.getPk_receive_detail());
			List<ReceDetailBVO> newVOs = newMap.get(matchVO.getPk_receive_detail());

			// 合计金额到表头
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			if(newVOs != null) {
				for(ReceDetailBVO newVO : newVOs) {
					cost_amount = cost_amount.add(newVO.getAmount());
				}
			}
			if(oldVOs != null) {
				for(ReceDetailBVO oldVO : oldVOs) {
					if(oldVO.getStatus() != VOStatus.DELETED) {
						cost_amount = cost_amount.add(oldVO.getAmount());
					}
				}
			}
			logger.info("合并金额到应收明细主表");
			rdVO.setCost_amount(cost_amount);
			rdVO.setUngot_amount(cost_amount);
			rdVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(rdVO);
			
			//调用公共修改应收明细表头信息方法，将以下代码注释掉。2015-10-26 jonathan
			List<ReceDetailBVO> InsertVOs = new ArrayList<ReceDetailBVO>();
			if(oldVOs!=null&&oldVOs.size()>0)
			{
				InsertVOs.addAll(oldVOs);
			}
			if(newVOs!=null&&newVOs.size()>0)
			{
				InsertVOs.addAll(newVOs);
			}
			//调用公共修改应收明细表头信息方法，将以下代码注释掉。2015-10-26 jonathan
			CMUtils.processExtenalforComputer(rdVO,InsertVOs.toArray(new ReceDetailBVO[InsertVOs.size()]));

			logger.info("将相关的发货单的def4置为Y,并合计金额");
			invVO.setCost_amount(cost_amount);
			invVO.setDef4(Constants.Y);
			invVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(invVO);
		}
	}

	private InvoiceVO[] getInvoiceVOs(List<MatchVO> matchVOs) {
		return NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class,
				"vbillno in " + getInvoiceVbillnoCond(matchVOs));
	}

	/**
	 * 更新批次信息，以及匹配后的应收费用明细
	 * 
	 * @param matchVOs
	 * @param contractBVOs
	 */
	private ExAggOrderlotVO build(List<MatchVO> matchVOs, List<ContractBVO> contractBVOs, int seg_type,
			int expense_parenttype, boolean ifFX) {
		// 删除应收明细里面系统创建的费用明细
		ReceDetailBVO[] oldRdbVOs = deleteReceiveDetail(matchVOs);
		InvoiceVO[] invVOs = getInvoiceVOs(matchVOs);
		List<ReceDetailBVO> newRdbVOs = new ArrayList<ReceDetailBVO>();
		logger.info("将分组信息保存到批次表");
		ExAggOrderlotVO aggVO = saveToOrderlot(matchVOs, seg_type, ifFX);// 就算没有匹配到合同明细，也需要记录批次信息

		MatchVO firstVO = matchVOs.get(0);
		if(contractBVOs == null || contractBVOs.size() == 0) {
			logger.info("没有匹配到合同...");
			return null;
		}
		logger.info("匹配到" + contractBVOs.size() + "条合同...");

		if(ifFX) {
			logger.info("筛选出费用类型为干线费和送货费的费用类型");
		} else {
			String s = SegmentConst.segtypeMap.get(seg_type);
			logger.info("筛选出费用类型所属大类为[" + s + "]的合同明细");
		}

		Map<String, String> expenseTypeMap = getExpenseTypeMap(firstVO.getPk_corp());
		Map<String, String> expenseTypeCodeMap = getExpenseTypeCodeMap(firstVO.getPk_corp());
		if(expenseTypeMap.size() == 0 || expenseTypeCodeMap.size() == 0) {
			logger.info("公司[" + firstVO.getPk_corp() + "]没有对应的费用类型");
			return null;
		}
		for(int i = 0; i < contractBVOs.size(); i++) {
			ContractBVO contractBVO = contractBVOs.get(i);
			String pk_expense_type = contractBVO.getPk_expense_type();
			if(StringUtils.isBlank(pk_expense_type)) {
				throw new BusiException("匹配的合同明细没有维护费用类型，明细ID[" + contractBVO.getPk_contract_b()+"]");
			}

			if(ifFX) {
				// 如果是返箱,可能的费用类型是干线费和送货费
				if(ExpenseTypeConst.ET0030.equals(expenseTypeCodeMap.get(pk_expense_type))
						|| ExpenseTypeConst.ET0040.equals(expenseTypeCodeMap.get(pk_expense_type))) {
				} else {
					contractBVOs.remove(i);
					i--;
					continue;
				}
			} else {
				String parent_type = expenseTypeMap.get(pk_expense_type);
				if(String.valueOf(expense_parenttype).equals(parent_type)) {

				} else {
					contractBVOs.remove(i);
					i--;
					continue;
				}

				if(seg_type == SegmentConst.SEG_TYPE_GXD) {
					// 如果是干线段，如果是已知的供应商,只取计价方式为“车型吨位+重量”的合同明细
					if(supplierList != null && supplierList.contains(firstVO.getPk_supplier())) {
						if(contractBVO.getValuation_type() == null
								|| contractBVO.getValuation_type().intValue() != ValuationTypeConst.CXDWLD) {
							contractBVOs.remove(i);
							i--;
							continue;
						}
					}
				}
			}
		}

		logger.info("生成费用明细...");
		List<ReceDetailBVO> detailBVOs = new ArrayList<ReceDetailBVO>();
		if(seg_type == SegmentConst.SEG_TYPE_GXD) {// 干线段
			List<ContractBVO> wOrvCVOs = new ArrayList<ContractBVO>();
			for(int i = 0; i < contractBVOs.size(); i++) {
				ContractBVO contractBVO = contractBVOs.get(i);
				if(contractBVO.getValuation_type() != null
						&& (contractBVO.getValuation_type().intValue() == ValuationTypeConst.WEIGHT || contractBVO
								.getValuation_type().intValue() == ValuationTypeConst.VOLUME)) {
					wOrvCVOs.add(contractBVO);
					contractBVOs.remove(i);
					i--;
				}
			}
			if(wOrvCVOs.size() > 0) {
				// 分开生成明细
				for(MatchVO mVO : matchVOs) {
					List<ReceDetailBVO> rdBVOs = contractService.buildReceDetailBVO(firstVO.getPk_customer(), mVO
							.getPack_num_count().doubleValue(), mVO.getNum_count(), mVO.getFee_weight_count()
							.doubleValue(), mVO.getWeight_count().doubleValue(), mVO.getVolume_count().doubleValue(),mVO.getPackInfos(),
							null, firstVO.getPk_corp(), wOrvCVOs);
					if(rdBVOs != null && rdBVOs.size() > 0) {
						// 直接插入批次费用，批次分摊表
						List<ReceDetailBVO> rdVOs = saveToOrderlotRd(aggVO, rdBVOs, mVO);
						if(rdVOs != null && rdVOs.size() > 0) {
							newRdbVOs.addAll(rdVOs);
						}
					}
				}
			}
			if(contractBVOs.size() > 0) {
				// 合并计算
				UFDouble pack_num_count = UFDouble.ZERO_DBL;
				Integer num_count = 0;
				UFDouble fee_weight_count = UFDouble.ZERO_DBL;
				UFDouble weight_count = UFDouble.ZERO_DBL;
				UFDouble volume_count = UFDouble.ZERO_DBL;
				for(MatchVO mVO : matchVOs) {
					pack_num_count = pack_num_count.add(mVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : mVO
							.getPack_num_count());
					num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
					fee_weight_count = fee_weight_count.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO
							.getFee_weight_count());
					weight_count = weight_count.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO
							.getWeight_count());
					volume_count = volume_count.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO
							.getVolume_count());
				}
				List<ReceDetailBVO> rdBVOs = contractService.buildReceDetailBVO(firstVO.getPk_customer(),
						pack_num_count.doubleValue(), num_count, fee_weight_count.doubleValue(),
						weight_count.doubleValue(), volume_count.doubleValue(),matchVOs.get(0).getPackInfos(), null, firstVO.getPk_corp(),
						contractBVOs);
				if(rdBVOs != null && rdBVOs.size() > 0) {
					detailBVOs.addAll(rdBVOs);
				}
			}
		} else {
			UFDouble pack_num_count = UFDouble.ZERO_DBL;
			Integer num_count = 0;
			UFDouble fee_weight_count = UFDouble.ZERO_DBL;
			UFDouble weight_count = UFDouble.ZERO_DBL;
			UFDouble volume_count = UFDouble.ZERO_DBL;
			for(MatchVO mVO : matchVOs) {
				pack_num_count = pack_num_count.add(mVO.getPack_num_count() == null ? UFDouble.ZERO_DBL : mVO
						.getPack_num_count());
				num_count = num_count + (mVO.getNum_count() == null ? 0 : mVO.getNum_count());
				fee_weight_count = fee_weight_count.add(mVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : mVO
						.getFee_weight_count());
				weight_count = weight_count.add(mVO.getWeight_count() == null ? UFDouble.ZERO_DBL : mVO
						.getWeight_count());
				volume_count = volume_count.add(mVO.getVolume_count() == null ? UFDouble.ZERO_DBL : mVO
						.getVolume_count());
			}
			detailBVOs = contractService.buildReceDetailBVO(firstVO.getPk_customer(), pack_num_count.doubleValue(),
					num_count, fee_weight_count.doubleValue(), weight_count.doubleValue(), volume_count.doubleValue(),matchVOs.get(0).getPackInfos(),
					null, firstVO.getPk_corp(), contractBVOs);
		}

		if(detailBVOs == null || detailBVOs.size() == 0) {
			// logger.info("没有匹配到任何费用明细");
		} else {
			// logger.info("共匹配到" + detailBVOs.size() + "条费用明细");
			List<ReceDetailBVO> rdBVOs = saveToOrderlotRd(aggVO, detailBVOs, matchVOs);
			if(rdBVOs != null && rdBVOs.size() > 0) {
				newRdbVOs.addAll(rdBVOs);
			}
			// 新的匹配的费用明细添加到费用明细中，将原有的系统创建的费用删除
		}
		// 统一更新aggVO;
		if(!viewOnly) {// 传入curAggVO目前是在批次页面的情况，此时不要删除
			NWDao.getInstance().saveOrUpdate(aggVO);
			logger.info("更新发货单主表以及应收明细的主表");
			updateInvoiceAndRd(matchVOs, oldRdbVOs, newRdbVOs, invVOs);
		}
		return aggVO;
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
		if(expenseTypeMap != null) {
			return expenseTypeMap;
		} else {
			expenseTypeMap = new HashMap<String, String>();
			corpExpenseTypeMap.put(pk_corp, expenseTypeMap);
		}
		// 查询费用类型
		String sql = "select pk_expense_type,parent_type from ts_expense_type WITH(NOLOCK) where isnull(dr,0)=0 and (pk_corp=? or pk_corp=?)";
		List<HashMap> list = NWDao.getInstance().queryForList(sql, HashMap.class, pk_corp, Constants.SYSTEM_CODE);
		if(list != null) {
			for(HashMap map : list) {
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
		if(expenseTypeCodeMap != null) {
			return expenseTypeCodeMap;
		} else {
			expenseTypeCodeMap = new HashMap<String, String>();
			corpExpenseTypeCodeMap.put(pk_corp, expenseTypeCodeMap);
		}
		// 查询费用类型
		String sql = "select pk_expense_type,code from ts_expense_type WITH(NOLOCK)  where isnull(dr,0)=0 and (pk_corp=? or pk_corp=?)";
		List<HashMap> list = NWDao.getInstance().queryForList(sql, HashMap.class, pk_corp, Constants.SYSTEM_CODE);
		if(list != null) {
			for(HashMap map : list) {
				String pk_expense_type = String.valueOf(map.get("pk_expense_type"));
				String code = String.valueOf(map.get("code"));
				expenseTypeCodeMap.put(pk_expense_type, code);
			}
		}
		return expenseTypeCodeMap;
	}

	private String getRdVbillnoCond(List<MatchVO> matchVOs) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i = 0; i < matchVOs.size(); i++) {
			buf.append("'");
			buf.append(matchVOs.get(i).getRd_vbillno());
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}

	private String getInvoiceVbillnoCond(List<MatchVO> matchVOs) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i = 0; i < matchVOs.size(); i++) {
			buf.append("'");
			buf.append(matchVOs.get(i).getInvoice_vbillno());
			buf.append("',");
		}
		String cond = buf.substring(0, buf.length() - 1); // 过滤最后一个逗号
		cond += ")";
		return cond;
	}

	/**
	 * 保存单个应收明细的费用明细记录
	 * 
	 * @param olVO
	 * @param oldDetailBVOs
	 *            这里包括此时分组的所有应收明细，所以要挑选出matchVO中的应收明细对应的费用明细
	 * @param detailBVOs
	 * @param matchVO
	 */
	private List<ReceDetailBVO> saveToOrderlotRd(ExAggOrderlotVO aggVO, List<ReceDetailBVO> detailBVOs, MatchVO matchVO) {
		logger.info("插入批次费用表以及批次分摊表...");
		if(detailBVOs == null || detailBVOs.size() == 0) {
			logger.info("没有费用明细，不需要插入");
			return null;
		}
		OrderlotVO olVO = (OrderlotVO) aggVO.getParentVO();
		List<ReceDetailBVO> newVOs = new ArrayList<ReceDetailBVO>();
		List<OrderlotRdVO> orderlotRdVOs = new ArrayList<OrderlotRdVO>();
		List<OrderlotDeviVO> orderlotDeviVOs = new ArrayList<OrderlotDeviVO>();
		for(ReceDetailBVO rdBVO : detailBVOs) {
			OrderlotRdVO vo = new OrderlotRdVO();
			vo.setLot(olVO.getLot());
			vo.setValuation_type(rdBVO.getValuation_type());
			vo.setPk_expense_type(rdBVO.getPk_expense_type());
			vo.setNode_count(1);
			vo.setCar_weight(rdBVO.getCar_weight());
			vo.setCar_num(rdBVO.getCarnum());

			// 件数重量，体积，计费重，应该是对应的发货单的合计数据
			vo.setNum_count(matchVO.getNum_count());
			vo.setWeight_count(matchVO.getWeight_count());
			vo.setVolume_count(matchVO.getVolume_count());
			vo.setFee_weight_count(matchVO.getFee_weight_count());

			vo.setPrice(rdBVO.getPrice());
			vo.setAmount(rdBVO.getAmount());
			vo.setContract_amount(rdBVO.getAmount());
			vo.setPk_contract_b(rdBVO.getPk_contract_b());
			vo.setSystem_create(UFBoolean.TRUE);

			vo.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(vo);
			orderlotRdVOs.add(vo);
			// NWDao.getInstance().saveOrUpdate(vo);

			// 批次分摊表
			OrderlotDeviVO childVO = new OrderlotDeviVO();
			childVO.setLot(olVO.getLot());
			childVO.setPk_orderlot_rd(vo.getPk_orderlot_rd());
			childVO.setInvoice_vbillno(matchVO.getInvoice_vbillno());
			childVO.setRd_vbillno(matchVO.getRd_vbillno());
			childVO.setPk_receive_detail(matchVO.getPk_receive_detail());
			childVO.setValuation_type(rdBVO.getValuation_type());
			childVO.setPk_expense_type(rdBVO.getPk_expense_type());
			childVO.setNode_count(1);
			childVO.setCar_weight(rdBVO.getCar_weight());
			childVO.setCar_num(rdBVO.getCarnum());
			// 件数重量，体积，计费重，应该是对应的发货单的数据
			childVO.setNum_count(matchVO.getNum_count());
			childVO.setWeight_count(matchVO.getWeight_count());
			childVO.setVolume_count(matchVO.getVolume_count());
			childVO.setFee_weight_count(matchVO.getFee_weight_count());
			childVO.setAmount(rdBVO.getAmount().setScale(2, UFDouble.ROUND_HALF_UP));
			//
			childVO.setPk_contract_b(rdBVO.getPk_contract_b());
			childVO.setSystem_create(UFBoolean.TRUE);
			childVO.setCreate_time(new UFDateTime(new Date()));
			childVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(childVO);
			orderlotDeviVOs.add(childVO);

			// 更新费用明细
			ReceDetailBVO newVO = (ReceDetailBVO) rdBVO.clone();
			newVO.setPk_receive_detail(matchVO.getPk_receive_detail());
			newVO.setAmount(childVO.getAmount());
			newVO.setContract_amount(newVO.getAmount());
			newVO.setStatus(VOStatus.NEW);
			newVO.setPk_rece_detail_b(null);
			NWDao.setUuidPrimaryKey(newVO);
			newVOs.add(newVO);

			// 设置批次分摊表和费用明细的关联
			childVO.setPk_receive_detail_b(newVO.getPk_rece_detail_b());
		}
		// 删除旧的数据
		OrderlotRdVO[] oldVOs1 = (OrderlotRdVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_RD);
		if(oldVOs1 != null) {
			for(OrderlotRdVO oldVO : oldVOs1) {
				oldVO.setStatus(VOStatus.DELETED);
				orderlotRdVOs.add(oldVO);
			}
		}
		OrderlotDeviVO[] oldVOs = (OrderlotDeviVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_DEVI);
		if(oldVOs != null) {
			for(OrderlotDeviVO oldVO : oldVOs) {
				oldVO.setStatus(VOStatus.DELETED);
				orderlotDeviVOs.add(oldVO);
			}
		}
		aggVO.setTableVO(TabcodeConst.TS_ORDERLOT_DEVI,
				orderlotDeviVOs.toArray(new OrderlotDeviVO[orderlotDeviVOs.size()]));
		aggVO.setTableVO(TabcodeConst.TS_ORDERLOT_RD, orderlotRdVOs.toArray(new OrderlotRdVO[orderlotRdVOs.size()]));
		return newVOs;
	}

	/**
	 * 保存批次费用表，以及批次分摊表
	 */
	private List<ReceDetailBVO> saveToOrderlotRd(ExAggOrderlotVO aggVO, List<ReceDetailBVO> detailBVOs,
			List<MatchVO> matchVOs) {
		logger.info("插入批次费用表以及批次分摊表...");
		if(detailBVOs == null || detailBVOs.size() == 0) {
			logger.info("没有费用明细，不需要插入");
			return null;
		}
		List<ReceDetailBVO> newVOs = new ArrayList<ReceDetailBVO>();
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		UFDouble fee_weight_count = UFDouble.ZERO_DBL;
		for(MatchVO matchVO : matchVOs) {
			num_count = num_count + (matchVO.getNum_count() == null ? 0 : matchVO.getNum_count());
			weight_count = weight_count.add(matchVO.getWeight_count() == null ? UFDouble.ZERO_DBL : matchVO
					.getWeight_count());
			volume_count = volume_count.add(matchVO.getVolume_count() == null ? UFDouble.ZERO_DBL : matchVO
					.getVolume_count());
			fee_weight_count = fee_weight_count.add(matchVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : matchVO
					.getFee_weight_count());
		}
		OrderlotVO olVO = (OrderlotVO) aggVO.getParentVO();
		List<OrderlotRdVO> orderlotRdVOs = new ArrayList<OrderlotRdVO>();
		List<OrderlotDeviVO> orderlotDeviVOs = new ArrayList<OrderlotDeviVO>();
		for(ReceDetailBVO rdBVO : detailBVOs) {
			OrderlotRdVO vo = new OrderlotRdVO();
			vo.setLot(olVO.getLot());
			vo.setValuation_type(rdBVO.getValuation_type());
			vo.setPk_expense_type(rdBVO.getPk_expense_type());
			vo.setNode_count(1);
			vo.setCar_weight(rdBVO.getCar_weight());
			vo.setCar_num(rdBVO.getCarnum());

			// 件数重量，体积，计费重，应该是对应的发货单的合计数据
			vo.setNum_count(num_count);
			vo.setWeight_count(weight_count);
			vo.setVolume_count(volume_count);
			vo.setFee_weight_count(fee_weight_count);

			vo.setPrice(rdBVO.getPrice());
			vo.setAmount(rdBVO.getAmount());
			vo.setContract_amount(rdBVO.getAmount());
			vo.setPk_contract_b(rdBVO.getPk_contract_b());
			vo.setSystem_create(UFBoolean.TRUE);

			vo.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(vo);
			orderlotRdVOs.add(vo);

			// 批次分摊表
			for(MatchVO matchVO : matchVOs) {
				OrderlotDeviVO childVO = new OrderlotDeviVO();
				childVO.setLot(olVO.getLot());
				childVO.setPk_orderlot_rd(vo.getPk_orderlot_rd());
				childVO.setInvoice_vbillno(matchVO.getInvoice_vbillno());
				childVO.setRd_vbillno(matchVO.getRd_vbillno());
				childVO.setPk_receive_detail(matchVO.getPk_receive_detail());
				childVO.setValuation_type(rdBVO.getValuation_type());
				childVO.setPk_expense_type(rdBVO.getPk_expense_type());
				childVO.setNode_count(1);
				childVO.setCar_weight(rdBVO.getCar_weight());
				childVO.setCar_num(rdBVO.getCarnum());
				// 件数重量，体积，计费重，应该是对应的发货单的数据
				childVO.setNum_count(matchVO.getNum_count());
				childVO.setWeight_count(matchVO.getWeight_count());
				childVO.setVolume_count(matchVO.getVolume_count());
				childVO.setFee_weight_count(matchVO.getFee_weight_count());

				// 根据计费重分摊，并且只取两位小数
				UFDouble amount = rdBVO.getAmount().div(fee_weight_count).multiply(matchVO.getFee_weight_count());
				childVO.setAmount(amount.setScale(2, UFDouble.ROUND_HALF_UP));
				//
				childVO.setPk_contract_b(rdBVO.getPk_contract_b());
				childVO.setSystem_create(UFBoolean.TRUE);
				childVO.setCreate_time(new UFDateTime(new Date()));
				childVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(childVO);
				orderlotDeviVOs.add(childVO);

				// 更新费用明细
				ReceDetailBVO newVO = (ReceDetailBVO) rdBVO.clone();
				newVO.setPk_receive_detail(matchVO.getPk_receive_detail());
				newVO.setAmount(childVO.getAmount());
				newVO.setContract_amount(newVO.getAmount());
				newVO.setStatus(VOStatus.NEW);
				newVO.setPk_rece_detail_b(null);
				NWDao.setUuidPrimaryKey(newVO);
				newVOs.add(newVO);

				// 设置批次分摊表和费用明细的关联
				childVO.setPk_receive_detail_b(newVO.getPk_rece_detail_b());
			}
		}
		aggVO.setTableVO(TabcodeConst.TS_ORDERLOT_DEVI,
				orderlotDeviVOs.toArray(new OrderlotDeviVO[orderlotDeviVOs.size()]));
		aggVO.setTableVO(TabcodeConst.TS_ORDERLOT_RD, orderlotRdVOs.toArray(new OrderlotRdVO[orderlotRdVOs.size()]));
		return newVOs;
	}

	/**
	 * 保存批次表，以及批次订单表
	 * 
	 * @param matchVOs
	 * @param seg_type
	 */
	private ExAggOrderlotVO saveToOrderlot(List<MatchVO> matchVOs, int seg_type, boolean ifFX) {
		if(matchVOs == null || matchVOs.size() == 0) {
			return null;
		}
		MatchVO first = matchVOs.get(0);
		Integer num_count = 0;
		UFDouble weight_count = UFDouble.ZERO_DBL;
		UFDouble volume_count = UFDouble.ZERO_DBL;
		UFDouble fee_weight_count = UFDouble.ZERO_DBL;
		for(MatchVO matchVO : matchVOs) {
			num_count += matchVO.getNum_count() == null ? 0 : matchVO.getNum_count();
			weight_count = weight_count.add(matchVO.getWeight_count() == null ? UFDouble.ZERO_DBL : matchVO
					.getWeight_count());
			volume_count = volume_count.add(matchVO.getVolume_count() == null ? UFDouble.ZERO_DBL : matchVO
					.getVolume_count());
			fee_weight_count = fee_weight_count.add(matchVO.getFee_weight_count() == null ? UFDouble.ZERO_DBL : matchVO
					.getFee_weight_count());
		}

		// 保存到批次表
		ExAggOrderlotVO aggVO;
		OrderlotVO olVO;
		String lot;
		if(curAggVO != null) {
			aggVO = curAggVO;
			olVO = (OrderlotVO) aggVO.getParentVO();
			lot = olVO.getLot();
			olVO.setStatus(VOStatus.UPDATED);
		} else {
			aggVO = new ExAggOrderlotVO();
			olVO = new OrderlotVO();
			lot = BillnoHelper.generateBillnoByDefault(BillTypeConst.ORDERLOT);
			olVO.setLot(lot);
			olVO.setDbilldate(new UFDate());
			olVO.setVbillstatus(BillStatus.NEW);
			olVO.setPk_corp(first.getPk_corp());
			olVO.setStatus(VOStatus.NEW);
			// NWDao.setUuidPrimaryKey(olVO);//因为OrderlotVO和子表关联的外键是lot，在vo中设置主键字段就是lot，这里不能使用这个方式设置真正的主键
			olVO.setPk_orderlot(UUID.randomUUID().toString().replace("-", ""));
			aggVO.setParentVO(olVO);
		}
		olVO.setSeg_type(seg_type);
		if(ifFX) {
			olVO.setIffx(UFBoolean.TRUE);
		} else {
			olVO.setIffx(UFBoolean.FALSE);
		}

		olVO.setNum_count(num_count);
		olVO.setWeight_count(weight_count);
		olVO.setVolume_count(volume_count);
		olVO.setFee_weight_count(fee_weight_count);

		if(ifFX) {
			olVO.setPk_delivery(first.getPk_delivery());
			olVO.setDeli_city(first.getDeli_city());
			olVO.setPk_arrival(first.getPk_arrival());
			olVO.setArri_city(first.getArri_city());
		} else {
			olVO.setPk_delivery(first.getPk_delivery());
			olVO.setDeli_city(first.getDeli_city());
			olVO.setPk_arrival(first.getPk_arrival());
			olVO.setArri_city(first.getArri_city());
		}

		olVO.setPk_trans_type(first.getPk_trans_type());
		olVO.setBala_customer(first.getBala_customer());
		olVO.setReq_deli_date(first.getReq_deli_date());
		olVO.setCreate_time(new UFDateTime(new Date()));

		List<OrderlotInvVO> oliVOs = new ArrayList<OrderlotInvVO>();
		for(MatchVO vo : matchVOs) {
			OrderlotInvVO oliVO = new OrderlotInvVO();
			oliVO.setLot(lot);
			oliVO.setInvoice_vbillno(vo.getInvoice_vbillno());
			oliVO.setFee_weight_count(vo.getFee_weight_count());
			oliVO.setRd_vbillno(vo.getRd_vbillno());

			oliVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(oliVO);
			oliVOs.add(oliVO);
		}
		OrderlotInvVO[] oldVOs = (OrderlotInvVO[]) aggVO.getTableVO(TabcodeConst.TS_ORDERLOT_INV);
		if(oldVOs != null) {
			for(OrderlotInvVO oldVO : oldVOs) {
				oldVO.setStatus(VOStatus.DELETED);
				oliVOs.add(oldVO);
			}
		}
		aggVO.setTableVO(TabcodeConst.TS_ORDERLOT_INV, oliVOs.toArray(new OrderlotInvVO[oliVOs.size()]));
		return aggVO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobService#after(org.nw.vo.sys.JobDefVO)
	 */
	public void after(JobDefVO jobDefVO) {
		corpExpenseTypeMap.clear();
		corpExpenseTypeCodeMap.clear();
		if(supplierList != null) {
			supplierList.clear();
		}
	}

	public void setCurAggVO(ExAggOrderlotVO curAggVO) {
		this.curAggVO = curAggVO;
	}

	public void setViewOnly(boolean viewOnly) {
		this.viewOnly = viewOnly;
	}

}
