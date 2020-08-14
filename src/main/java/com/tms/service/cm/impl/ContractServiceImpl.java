package com.tms.service.cm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.AreaHelper;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;
import org.springframework.stereotype.Service;

import com.tms.constants.BillingRuleConst;
import com.tms.constants.ContractConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.QuoteTypeConst;
import com.tms.constants.ValuationTypeConst;
import com.tms.service.cm.ContractService;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.cm.CartypeTonnageVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractMatchVO;
import com.tms.vo.cm.ContractVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayDetailBMatchVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.ReceDetailBMatchVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.te.EntOperationBVO;

/**
 * 合同
 * 
 * @author xuqc
 * @date 2012-8-28 上午10:17:22
 */
@Service
public class ContractServiceImpl extends AbsToftServiceImpl implements ContractService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ContractVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ContractVO.PK_CONTRACT);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ContractBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ContractBVO.PK_CONTRACT);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_contract_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_contract_b");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}
	
	private static final String MILKRUN_TRANS_TYPE = "89816b7d4cfe457881425a48fad21cc8";

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals(ContractVO.CONTRACT_TYPE)) {
					// 编辑合同类型
					// fieldVO.setUserdefine1("afterChangeContractType()");
				}
			} else if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getItemkey().equals(ContractBVO.START_ADDR_TYPE)) {
					// 起始区域类型
					fieldVO.setUserdefine1("afterEditStart_addr_type(record)");
				} else if(fieldVO.getItemkey().equals(ContractBVO.END_ADDR_TYPE)) {
					// 目的区域类型
					fieldVO.setUserdefine1("afterEditEnd_addr_type(record)");
				}
			}
		}
		return templetVO;
	}

	public String getCodeFieldCode() {
		return "code";
	}

	public SuperVO getByCode(String code) {
		return dao
				.queryByCondition(ContractVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and code=?", code);
	}
	
	@Override
	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processAfterSave(billVO, paramVO);
		ContractVO contractVO = (ContractVO) billVO.getParentVO();
		if(contractVO.getStatus() == VOStatus.NEW){
			ContractUtils.add((HYBillVO)billVO);
		}else{
			if(UFBoolean.TRUE.equals(contractVO.getLocked_flag())){
				ContractUtils.remove(contractVO.getPk_contract());
			}else{
				ContractUtils.modify((HYBillVO)billVO);
			}
		}
	}
	
	@Override
	protected void processAfterDelete(AggregatedValueObject billVO) {
		super.processAfterDelete(billVO);
		String pk_contract = ((ContractVO)billVO.getParentVO()).getPk_contract();
		ContractUtils.remove(pk_contract);
	}

	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		ContractVO parentVO = (ContractVO) copyVO.getParentVO();
		parentVO.setCode(null);
		parentVO.setName(null);
		parentVO.setContractno(null);
	}

	public int addChildren(List<ContractBVO> superVOs) {
		if(superVOs != null) {
			for(ContractBVO superVO : superVOs) {
				superVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(superVO);
				dao.saveOrUpdate(superVO);
			}
		}
		return superVOs.size();
	}


	/**
	 * 合同比较特殊，一般只能查询本公司及其子公司的单据
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = parseCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and ";
			fCond += cond;
		}

		if(!paramVO.isBody()) {
			String corpCond = CorpHelper.getCurrentCorpWithChildrenAndGroup();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and " + corpCond;
			}
		}
		return fCond;
	}
	
	
	
	
	/**
	 * 合同匹配 会按以下优先级匹配合同 ：
	 * 						0:地址-地址
	 * 						1:地址-城市
	 * 						2:城市-地址
	 * 						3:城市-城市
	 * 当涉及城市匹配的时候，需要匹配到这个城市的单点合同
	 * 
	 * @param matchVO 合同匹配的匹配类
	 * @return
	 */
	public List<ContractBVO> matchContract(ContractMatchVO matchVO){
		logger.info("--------------------开始匹配合同 --------------------");
		long strat =  System.currentTimeMillis();
		logger.info("--------------------"+strat+"--------------------");
		if (true) {
			return ContractUtils.matchContract(matchVO);
		}
		List<ContractBVO> contractBVOs = new ArrayList<ContractBVO>();
		List<ContractBVO> nullAddrContractBVOs = new ArrayList<ContractBVO>();//空地址合同
		logger.info("--------------------开始匹配合同 --------------------");
		if(matchVO == null || matchVO.getContract_type() == null || StringUtils.isBlank(matchVO.getPk_carrierOrBala_customer())
				|| StringUtils.isBlank(matchVO.getPk_corp()) || StringUtils.isBlank(matchVO.getPk_trans_type())){
			logger.info("--------------------匹配合同关键字段为空，匹配失败 --------------------");
			return contractBVOs;
		}
		
		
		String[] corps = new String[] { matchVO.getPk_corp(), Constants.SYSTEM_CODE };
		logger.info("--------------------开始匹配合同，匹配参数如下：--------------------");
		logger.info(new StringBuilder().append("contract_type:").append(matchVO.getContract_type()).append(",")
				.append("pk_carrierOrBala_customer:").append(matchVO.getPk_carrierOrBala_customer()).append(",")
				.append("pk_trans_type:").append(matchVO.getPk_trans_type()).append(",").append("start_addr:").append(matchVO.getStart_addr())
				.append(",").append("end_addr:").append(matchVO.getEnd_addr()).append(",").append("start_city:").append(matchVO.getStart_city())
				.append(",").append("end_city:").append(matchVO.getEnd_city()).append(",").append("pk_corp:").append(matchVO.getPk_corp())
				.append(",").append("req_arri_date:").append(matchVO.getReq_arri_date())
				.append(",").append("urgent_level:").append(matchVO.getUrgent_level())
				.append(",").append("item_code:").append(matchVO.getItem_code())
				.append(",").append("pk_trans_line:").append(matchVO.getPk_trans_line())
				.append(",").append("if_return:").append(matchVO.getIf_return()));
		
		if(StringUtils.isBlank(matchVO.getReq_arri_date())) {
			matchVO.setReq_arri_date(DateUtils.getCurrentDate());
		} else {
			// 只取日期，过滤时间
			String req_arri_date = DateUtils.formatDate(matchVO.getReq_arri_date(), DateUtils.DATEFORMAT_HORIZONTAL);
			matchVO.setReq_arri_date(req_arri_date);
		}
		
		String mayBeNullCond = new String();
		if(StringUtils.isBlank(matchVO.getPk_trans_line())){
			mayBeNullCond += " and pk_trans_line is null ";
		}else{
			mayBeNullCond += " and pk_trans_line = '" + matchVO.getPk_trans_line() + "' " ;
		}
		
		if(StringUtils.isBlank(matchVO.getItem_code())){
			mayBeNullCond += " and item_code is null ";
		}else{
			mayBeNullCond += " and item_code = '" + matchVO.getItem_code() + "' " ;
		}
		
		if(matchVO.getIf_return() == null){
			mayBeNullCond += " and isnull (if_return,'N')='N' ";
		}else{
			mayBeNullCond += " and if_return =  '" + matchVO.getIf_return() + "' ";
		}
		
		if(matchVO.getUrgent_level() == null){
			mayBeNullCond += " and  isnull (urgent_level,0)=0 ";
		}else{
			mayBeNullCond += " and urgent_level =  " + matchVO.getUrgent_level() ;
		}
		
		//提货地址和到货地址为空的合同
		String nullSql = "select * from ts_contract_b WITH (NOLOCK) where isnull(locked_flag,'N')='N' and isnull(dr,0)=0 and start_addr is null and end_addr is null"+ mayBeNullCond;
		//符合条件的合同
		String sql = "select * from ts_contract_b WITH (NOLOCK) where isnull(locked_flag,'N')='N' and  isnull(dr,0)=0 and start_addr_type=? and end_addr_type=? and start_addr=? and end_addr=?"
				+ mayBeNullCond;
		String cond = " and pk_contract in (select pk_contract from ts_contract WITH (NOLOCK) where contract_type=? and ";
		//区分承运商和客户合同
		if(matchVO.getContract_type() == ContractConst.CARRIER) {
			cond += " pk_carrier=? ";
		} else if(matchVO.getContract_type() == ContractConst.CUSTOMER) {
			cond += " bala_customer=? ";
		}
		//组合SQL语句
		cond += " and trans_type=? and isnull(dr,0)=0 and effective_date <='"+matchVO.getReq_arri_date()+"' and "
				+ "invalid_date>'"+matchVO.getReq_arri_date()+"' and pk_corp=? and isnull(locked_flag,'N')='N')";

		//nullSql: 
		nullSql += cond;
		sql += cond;
		logger.info("匹配的基本SQL：" + sql);
		for(String corp : corps) {
			if(StringUtils.isBlank(corp)) {
				continue;
			}
			//milkRun的合同特殊匹配
			if(matchVO.getPk_trans_type().equals(MILKRUN_TRANS_TYPE)){
				//这个匹配方式专门为milkrun业务定制
				String newSql = new String();
				newSql = "select * from ts_contract_b WITH (NOLOCK) where isnull(locked_flag,'N')='N' and isnull(dr,0)=0 and start_addr_type=? and end_addr_type=? and start_addr=? and end_addr is null "
						+  mayBeNullCond + cond;
				contractBVOs = dao.queryForList(newSql, ContractBVO.class, DataDictConst.ADDR_TYPE.CITY.intValue(),
						DataDictConst.ADDR_TYPE.CITY.intValue(), matchVO.getStart_city(), matchVO.getContract_type(),
						matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
				if(contractBVOs != null && contractBVOs.size() > 0){
					break;
				}
				
			}else{
				logger.info("匹配点位合同");
				List<AreaVO> startAreaVOs = new ArrayList<AreaVO>();
				String startAreas = "";
				ContractBVO startPointContractBVO = null;
				if(StringUtils.isNotBlank(matchVO.getStart_city())){
					logger.info("匹配起点点位合同");
					startAreaVOs = AreaHelper.getCurrentAreaVOWithParents(matchVO.getStart_city());
					for(AreaVO areaVO : startAreaVOs) {
						startAreas += "'";
						startAreas += areaVO.getPk_area();
						startAreas += "',";
					}
					if(startAreas.length() > 0) {
						startAreas = "(" + startAreas.substring(0, startAreas.length() - 1) + ")";
					}else{
						startAreas = "('')";
					}
					String sql0 = "select * from ts_contract_b WITH(NOLOCK) where isnull(locked_flag,'N')='N' "
							+ "and isnull(dr,0)=0 and start_addr_type=? and end_addr_type=? "
							+ "and  start_addr in " +startAreas+ " AND  isnull(end_addr,'')='' "
							+ mayBeNullCond + cond;
					List<ContractBVO> pointContractBVOs = dao.queryForList(sql0, ContractBVO.class,
							DataDictConst.ADDR_TYPE.CITY.intValue(), DataDictConst.ADDR_TYPE.CITY.intValue(),
							matchVO.getContract_type(), 
							matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
					if(pointContractBVOs != null && pointContractBVOs.size() > 0){
						for(AreaVO area : startAreaVOs){
							boolean flag = false;
							for(ContractBVO contractBVO : pointContractBVOs){
								if(StringUtils.isBlank(contractBVO.getEnd_addr())
										&& area.getPk_area().equals(contractBVO.getStart_addr())){
									startPointContractBVO = contractBVO;
									logger.info("匹配到提货点合同[起点]：" + contractBVO.getStart_addr());
									flag = true;
									break;
								}
							}
							if(flag){
								break;
							}
						}
					}
				}
				List<AreaVO> endAreaVOs = new ArrayList<AreaVO>();
				String endAreas = "";
				ContractBVO endPointContractBVO = null;
				if(StringUtils.isNotBlank(matchVO.getEnd_city())){
					logger.info("匹配卸货点点点位合同");
					endAreaVOs = AreaHelper.getCurrentAreaVOWithParents(matchVO.getEnd_city());
					for(AreaVO areaVO : endAreaVOs) {
						endAreas += "'";
						endAreas += areaVO.getPk_area();
						endAreas += "',";
					}
					if(endAreas.length() > 0) {
						endAreas = "(" + endAreas.substring(0, endAreas.length() - 1) + ")";
					}else{
						endAreas = "('')";
					}
					
					String sql0 = "select * from ts_contract_b WITH(NOLOCK) where isnull(locked_flag,'N')='N' "
							+ "and isnull(dr,0)=0 and start_addr_type=? and end_addr_type=? "
							+ "and  start_addr in " +endAreas+ " AND  isnull(end_addr,'')='' "
							+ mayBeNullCond + cond;
					List<ContractBVO> pointContractBVOs = dao.queryForList(sql0, ContractBVO.class,
							DataDictConst.ADDR_TYPE.CITY.intValue(), DataDictConst.ADDR_TYPE.CITY.intValue(),
							matchVO.getContract_type(), 
							matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
					if(pointContractBVOs != null && pointContractBVOs.size() > 0){
						for(AreaVO area : startAreaVOs){
							boolean flag = false;
							for(ContractBVO contractBVO : pointContractBVOs){
								if(StringUtils.isBlank(contractBVO.getEnd_addr())
										&& area.getPk_area().equals(contractBVO.getStart_addr())){
									endPointContractBVO = contractBVO;
									logger.info("匹配到卸货点合同[起点]：" + contractBVO.getStart_addr());
									flag = true;
									break;
								}
							}
							if(flag){
								break;
							}
						}
					}
				}
				
				
				logger.info("1、根据提货方地址和到货方地址匹配");
				contractBVOs = dao.queryForList(sql, ContractBVO.class, DataDictConst.ADDR_TYPE.ADDR.intValue(),
						DataDictConst.ADDR_TYPE.ADDR.intValue(), matchVO.getStart_addr(), matchVO.getEnd_addr(), matchVO.getContract_type(),
						matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
				if(contractBVOs == null || contractBVOs.size() == 0){
					logger.info("2、根据提货方地址和到货方城市匹配");
					contractBVOs = dao.queryForList(sql, ContractBVO.class,DataDictConst.ADDR_TYPE.ADDR.intValue(), 
							DataDictConst.ADDR_TYPE.CITY.intValue(),matchVO.getStart_addr(), matchVO.getEnd_city(), matchVO.getContract_type(), 
							matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
				}
				if(contractBVOs == null || contractBVOs.size() == 0){
					logger.info("3、根据提货方城市和到货方地址匹配");
					contractBVOs = dao.queryForList(sql, ContractBVO.class,DataDictConst.ADDR_TYPE.CITY.intValue(), 
							DataDictConst.ADDR_TYPE.ADDR.intValue(),matchVO.getStart_city(), matchVO.getEnd_addr(), matchVO.getContract_type(), 
							matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
					
				}
				if(contractBVOs == null || contractBVOs.size() == 0){
					logger.info("4、根据提货方城市和到货方城市匹配");
					if(StringUtils.isNotBlank(matchVO.getStart_city()) && StringUtils.isNotBlank(matchVO.getEnd_city())){
						String sql0 = "select * from ts_contract_b WITH(NOLOCK) where isnull(locked_flag,'N')='N' "
								+ "and isnull(dr,0)=0 and start_addr_type=? and end_addr_type=? "
								+ "and  start_addr in " +startAreas+ " AND end_addr in " + endAreas 
								+ mayBeNullCond + cond;
						contractBVOs = dao.queryForList(sql0, ContractBVO.class,
								DataDictConst.ADDR_TYPE.CITY.intValue(), DataDictConst.ADDR_TYPE.CITY.intValue(),
								matchVO.getContract_type(), 
								matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
						//取出优先级最高的那个合同
						if(contractBVOs != null && contractBVOs.size() > 0){
							List<ContractBVO> stratAreaLeveLHighest = new ArrayList<ContractBVO>();
							List<ContractBVO> endAreaLeveLHighest = new ArrayList<ContractBVO>();
							for(AreaVO area : startAreaVOs){
								for(ContractBVO contractBVO : contractBVOs){
									//终点为空合同这里不会出现
									if(area.getPk_area().equals(contractBVO.getStart_addr())
											&& StringUtils.isNotBlank(contractBVO.getEnd_addr())){
										stratAreaLeveLHighest.add(contractBVO);
									}
								}
								if(stratAreaLeveLHighest.size() > 0){
									break;//如果匹配到了，更高层级的地址就不在匹配了。
								}
							}
							for(AreaVO area : endAreaVOs){
								for(ContractBVO contractBVO : stratAreaLeveLHighest){
									if(area.getPk_area().equals(contractBVO.getEnd_addr())){
										endAreaLeveLHighest.add(contractBVO);
									}
								}
								if(endAreaLeveLHighest.size() > 0){
									break;//如果匹配到了，更高层级的地址就不在匹配了。
								}
							}
							logger.info("根据提货方城市和到货方城市匹配,匹配到：" + endAreaLeveLHighest.size() + "条合同！");
							contractBVOs = endAreaLeveLHighest;
						}
					}
				}
				if(startPointContractBVO != null){
					contractBVOs.add(startPointContractBVO);
				}
				if(endPointContractBVO != null){
					contractBVOs.add(endPointContractBVO);
				}
				if(contractBVOs != null && contractBVOs.size() > 0) {
					break;
				}
			}
		}
		// 0、合同中没有维护起始地和目的地
 		for(String corp : corps) {
			if(StringUtils.isBlank(corp)) {
				continue;
			}
			logger.info("0、先匹配地址和地址为空的情况，结果将作为其他合同明细的补充");
			nullAddrContractBVOs = dao.queryForList(nullSql, ContractBVO.class,matchVO.getContract_type(), 
					matchVO.getPk_carrierOrBala_customer(), matchVO.getPk_trans_type(), corp);
			if(nullAddrContractBVOs != null && nullAddrContractBVOs.size() > 0) {
				break;
			}
		}
				
		if(nullAddrContractBVOs != null && nullAddrContractBVOs.size() > 0) {
			contractBVOs.addAll(nullAddrContractBVOs);
		}
		long end =  System.currentTimeMillis();
		logger.info("--------------------"+end+","+(end-strat)+"--------------------");
		return contractBVOs;
	}
	
	

	/**
	 * 匹配合同
	 * <p>
	 * FIXME,请确认sql语句的性能
	 * </p>
	 * 
	 * @param contract_type
	 *            ,合同类型，客户合同或者承运商合同
	 * @param pk_carrierOrBala_customer
	 * @param pk_trans_type
	 * @param start_addr
	 * @param end_addr
	 * @param start_city
	 * @param end_city
	 * @return
	 */
	//yaojiie 2016 1 11 合同匹配增加以下字段，紧急程度，项目编码，线路。
	public List<ContractBVO> matchContract(int contract_type, String pk_carrierOrBala_customer, String pk_trans_type,
			String start_addr, String end_addr, String start_city, String end_city, String pk_corp, String req_arri_date,
			Integer urgent_level, String item_code, String pk_trans_line,UFBoolean if_return) {
		ContractMatchVO matchVO = new ContractMatchVO();
		matchVO.setContract_type(contract_type);
		matchVO.setPk_carrierOrBala_customer(pk_carrierOrBala_customer);
		matchVO.setPk_trans_type(pk_trans_type);
		matchVO.setStart_addr(start_addr);
		matchVO.setStart_city(start_city);
		matchVO.setEnd_addr(end_addr);
		matchVO.setEnd_city(end_city);
		matchVO.setPk_corp(pk_corp);
		matchVO.setReq_arri_date(req_arri_date);
		matchVO.setUrgent_level(urgent_level);
		matchVO.setItem_code(item_code);
		matchVO.setPk_trans_line(pk_trans_line);
		matchVO.setIf_return(if_return);
		return matchContract(matchVO);
	}

	
	/**
	 * 根据合同VO生成费用明细，这是关键的方法，需要根据不同的条件计算单价及金额<br/>
	 * 2015-05-17
	 * 发货单在匹配合同时若出现相同类型的费用明细时系统可取其中的一条费用类型保留（根据客户档案中的计费规则）。可以取匹配金额的大值还是小值。默认取大
	 * 
	 * @param num_count
	 *            总件数-用于确定单价以及计算金额
	 * @param fee_weight_count
	 *            总计费重-用于确定单价以及计算金额
	 * @param volume_count
	 *            总体积-用于确定单价以及计算金额
	 * @param pk_car_type
	 *            设备类型-用于确定单价以及计算金额
	 * @param contractBVOs
	 * @return
	 */
	public List<ReceDetailBVO> buildReceDetailBVO(String pk_customer, double _pack_num_count, int _num_count,
			double _fee_weight_count, double _weight_count, double _volume_count, List<PackInfo> packInfos,
			String[] pk_car_type, String pk_corp, List<ContractBVO> contractBVOs) {
		double pack_num_count = _pack_num_count;
		int num_count = _num_count;
		double fee_weight_count = _fee_weight_count;
		double weight_count = _weight_count;
		double volume_count = _volume_count;
		List<ReceDetailBVO> detailBVOs = new ArrayList<ReceDetailBVO>();
		if(contractBVOs != null) {
			Map<String, ReceDetailBVO> typeAmountMap = new HashMap<String, ReceDetailBVO>();
			CustomerVO custVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "pk_customer=?", pk_customer);
			if(custVO == null) {
				throw new BusiException("客户[PK:?]已经被删除，请重新选择！",pk_customer);
			}
			// 返回车型吨位的对照信息，并根据费用类型分组，根据件数大到小排序
			Map<String, List<CartypeTonnageVO>> ctMap = new CartypeTonnageServiceImpl().getCartypeTonnageVOMap(pk_corp,ContractConst.CUSTOMER);
			Map<String, String> expenseTypeCodeMap = new ExpenseTypeServiceImpl().getExpenseTypeCodeMap(pk_corp);

			for(ContractBVO cvo : contractBVOs) {
				// 这里没有根据设备类型去匹配.
				// 如果计价方式是设备，那么判断这时候的设备类型是否一致，如果不一致，那么过滤该条合同明细
				if(cvo.getValuation_type() != null && cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
					if(pk_car_type != null && pk_car_type.length > 0) {
						boolean exist = false;
						for(String carType : pk_car_type) {
							if(carType.equals(cvo.getEquip_type())) {
								exist = true;
								break;
							}
						}
						if(!exist) {// 该设备类型不在我们查找之列，过滤掉
							continue;
						}
					} else {// 没有设备类型，那么过滤掉计价方式为设备的合同明细
						continue;
					}
				}
				// 如果计价方式是包装，那么判断这时候是否有包装信息是否一致，如果不一致，那么过滤该条合同明细
				if(StringUtils.isNotBlank(cvo.getPack())){
					if(packInfos == null || packInfos.size() == 0){
						continue;
					}
					boolean exist = true;
					for(PackInfo info : packInfos){
						if(info.getPack().equals(cvo.getPack())){
							exist = false;
							//一个合同明细只会匹配到一个包装类型，所以只会有一个数量
							//packInfo = info;
							fee_weight_count = info.getWeight().doubleValue();
							num_count = info.getNum();
							volume_count = info.getVolume().doubleValue();
							weight_count = info.getWeight().doubleValue();
							break;
						}
					}
					if (exist) {
						continue;
					}
				}
				
				ReceDetailBVO detailBVO = new ReceDetailBVO(); // 创建费用明细VO
				detailBVO.setPk_expense_type(cvo.getPk_expense_type()); // 费用类型
				detailBVO.setQuote_type(cvo.getQuote_type());// 报价类型
				detailBVO.setPrice_type(cvo.getPrice_type());// 价格类型
				detailBVO.setValuation_type(cvo.getValuation_type());// 计价方式
				detailBVO.setSystem_create(new UFBoolean(true));
				detailBVO.setPk_contract_b(cvo.getPk_contract_b());

				// 将合同明细的税种，税率冗余到这里
				detailBVO.setTax_cat(cvo.getTax_cat());
				detailBVO.setTax_rate(cvo.getTax_rate());

				// 单价、金额，重头戏
				double price = 0, amount = 0,cost = 0,contract_cost = 0;
				String bill_value = "";
				if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.FIRST_WEIGHT) {
					// 首重报价
					if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE
							&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
						// 单价、重量
						double weight = fee_weight_count - cvo.getFirst_weight().doubleValue();// 总计费重-首重
						price = getIntervalPrice(cvo, weight);
						cost = getIntervalCost(cvo, weight);
						contract_cost = cvo.getFirst_weight_cost().doubleValue() + weight * cost;
						amount = cvo.getFirst_weight_price().doubleValue() + weight * price;// =首重价格+（总计费重-首重）×区间价格
						bill_value = fee_weight_count + "";
					} else if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE
							&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
						// 固定价格、重量
						double weight = fee_weight_count - cvo.getFirst_weight().doubleValue();// 总计费重-首重
						price = getIntervalPrice(cvo, weight);
						cost = getIntervalCost(cvo, weight);
						contract_cost = cvo.getFirst_weight_cost().doubleValue() + cost;
						amount = cvo.getFirst_weight_price().doubleValue() + price;// =首重价格+区间价格
						bill_value = fee_weight_count + "";
					}
				} else if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.INTERVAL) {
					// 区间报价
					if(cvo.getPrice_type() != null && cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {
						// 价格类型是单价
						if(cvo.getValuation_type() != null) {
							// 2015-06-04 如果合同明细的费用类型是 提货段运费,则根据重量或者体积计算。
							if(cvo.getPk_expense_type().equals(expenseTypeCodeMap.get(ExpenseTypeConst.ET0010))) {// 提货段运费
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, fee_weight_count);
									amount = fee_weight_count * price;// =总计费重×区间价格
									cost = getIntervalCost(cvo, fee_weight_count);
									contract_cost = fee_weight_count * cost;
									bill_value = fee_weight_count + "";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, volume_count);
									amount = volume_count * price;// =总体积×区间价格
									
									cost = getIntervalCost(cvo, volume_count);
									contract_cost = volume_count * cost;
									bill_value = volume_count + "";
									//计价方式是包装
								}
							} else {
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
									// 计价方式是件数
									price = getIntervalPrice(cvo, num_count);
									amount = num_count * price;// =总件数×区间价格
									
									cost = getIntervalCost(cvo, num_count);
									contract_cost = num_count * cost;
									bill_value = num_count + "";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, fee_weight_count);
									amount = fee_weight_count * price;// =总计费重×区间价格
									
									cost = getIntervalCost(cvo, fee_weight_count);
									contract_cost = fee_weight_count * cost;
									bill_value = fee_weight_count + "";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, volume_count);
									amount = volume_count * price;// =总体积×区间价格
									
									cost = getIntervalCost(cvo, volume_count);
									contract_cost = volume_count * cost;
									bill_value = volume_count + "";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK_NUM) {
									// 计价方式是数量
									price = getIntervalPrice(cvo, pack_num_count);
									amount = pack_num_count * price;// =总数量×区间价格
									
									cost = getIntervalCost(cvo, pack_num_count);
									contract_cost = pack_num_count * cost;
									bill_value = pack_num_count + "";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDW) {
									// 计价方式是车型吨位
									List<CartypeTonnageVO> ctVOs = ctMap.get(cvo.getPk_expense_type());// 找到这个费用类型的车型吨位对照
									List<CXDWResultVO> resultVOs = getCartypeTonnageAmount(num_count, new UFDouble(
											weight_count), ctVOs, cvo);
									// 这里根据匹配到几个车型，就记录几条明细
									for(int i = 0; i < resultVOs.size(); i++) {
										CXDWResultVO resultVO = resultVOs.get(i);
										if(i == 0) {
											detailBVO.setCar_weight(resultVO.getCtVO().getWeight());
											detailBVO.setCarnum(resultVO.getCount());
											price = resultVO.getPrice().doubleValue();
											amount = resultVO.getAmount().doubleValue();
											
											cost = resultVO.getCost().doubleValue();
											contract_cost = resultVO.getContract_cost().doubleValue();
											bill_value = "" + resultVO.getCount();
										} else {
											// 深度拷贝一份
											ReceDetailBVO newVO = (ReceDetailBVO) detailBVO.clone();
											newVO.setPk_receive_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											newVO.setCar_weight(resultVO.getCtVO().getWeight());
											newVO.setCarnum(resultVO.getCount());
											newVO.setPrice(resultVO.getPrice());
											newVO.setAmount(resultVO.getAmount());
											newVO.setCost(resultVO.getCost());
											newVO.setContract_cost(resultVO.getContract_cost());
											newVO.setContract_amount(newVO.getAmount());
											newVO.setBill_value(bill_value);
											detailBVOs.add(newVO);
										}
									}
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.THD) {
									// 计价方式是提货点
									price = cvo.getPrice1().doubleValue();
									amount = price;
									
									cost = cvo.getCost1().doubleValue();
									contract_cost = cost;
									bill_value = "1";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDWLD) {
									// 车型吨位+重量算法
									List<CXDWLDResultVO> resultVOs = getCXDWAndLDAmount(new UFDouble(weight_count), cvo);
									// 这里根据匹配到几个车型，就记录几条明细
									for(int i = 0; i < resultVOs.size(); i++) {
										CXDWLDResultVO resultVO = resultVOs.get(i);
										if(i == 0) {
											if(resultVO.getCar_weight() != null
													&& resultVO.getCar_weight().doubleValue() > 0) {
												detailBVO.setValuation_type(ValuationTypeConst.CXDW);
												detailBVO.setBill_value("" + resultVO.getCount());
											} else {
												detailBVO.setValuation_type(ValuationTypeConst.WEIGHT);
												detailBVO.setBill_value(resultVO.getCar_weight() == null ? "0" : resultVO.getCar_weight().toString());
											}
											detailBVO.setCar_weight(resultVO.getCar_weight());
											detailBVO.setCarnum(resultVO.getCount());
											price = resultVO.getPrice().doubleValue();
											amount = resultVO.getAmount().doubleValue();
											
											cost = resultVO.getCost().doubleValue();
											contract_cost = resultVO.getContract_cost().doubleValue();
										} else {
											// 深度拷贝一份
											ReceDetailBVO newVO = (ReceDetailBVO) detailBVO.clone();
											newVO.setPk_receive_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											if(resultVO.getCar_weight() != null
													&& resultVO.getCar_weight().doubleValue() > 0) {
												newVO.setValuation_type(ValuationTypeConst.CXDW);
												newVO.setBill_value("" + resultVO.getCount());
											} else {
												newVO.setValuation_type(ValuationTypeConst.WEIGHT);
												newVO.setBill_value(resultVO.getCar_weight() == null ? "0" : resultVO.getCar_weight().toString());
											}
											newVO.setCar_weight(resultVO.getCar_weight());
											newVO.setCarnum(resultVO.getCount());
											newVO.setPrice(resultVO.getPrice());
											newVO.setAmount(resultVO.getAmount());
											newVO.setContract_amount(newVO.getAmount());
											newVO.setCost(resultVO.getCost());
											newVO.setContract_cost(resultVO.getContract_cost());
											detailBVOs.add(newVO);
										}
									}
								}
							}
						}
					} else if(cvo.getPrice_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE) {
						// 价格类型是固定价格
						if(cvo.getValuation_type() != null) {
							if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
								// 计价方式是重量
								price = getIntervalPrice(cvo, fee_weight_count);
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, fee_weight_count);
								contract_cost = cost;
								bill_value = "" + fee_weight_count;
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
								// 计价方式是件数
								price = getIntervalPrice(cvo, num_count);
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, num_count);
								contract_cost = cost;
								bill_value = "" + num_count;
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
								// 计价方式是体积
								price = getIntervalPrice(cvo, volume_count);
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, volume_count);
								contract_cost = cost;
								bill_value = "" + volume_count;
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
								// 计价方式是设备
								// 2015-06-09根据设备的个数，生成多少条费用明细
								boolean has = false;
								for(int i = 0; i < pk_car_type.length; i++) {
									if(pk_car_type[i].equals(cvo.getEquip_type())) {
										if(!has) {
											// 第一个费用
											price = cvo.getPrice1() == null ? 0 : cvo.getPrice1().doubleValue();
											amount = price;// =区间价格
											cost = cvo.getCost1() == null ? 0 : cvo.getCost1().doubleValue();
											contract_cost = cost;
											has = true;
											bill_value = cvo.getEquip_type();
										} else {
											// 生成第二个或者第三个费用明细
											ReceDetailBVO newVO = (ReceDetailBVO) detailBVO.clone();
											newVO.setPk_receive_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											newVO.setPrice(cvo.getPrice1());
											newVO.setAmount(newVO.getPrice());
											newVO.setContract_amount(newVO.getAmount());
											newVO.setCost(cvo.getCost1());
											newVO.setContract_cost(newVO.getCost());
											newVO.setBill_value(cvo.getEquip_type());
											detailBVOs.add(newVO);
										}
									}
								}
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TON_KM) {
								// 计价方式是吨公里，暂时不处理
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TICKET) {
								// 计价方式是票
								price = cvo.getPrice1().doubleValue();
								amount = price;// =区间价格
								cost = cvo.getCost1().doubleValue();
								contract_cost = cost;// =区间价格
								bill_value = "1";
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NODE) {
								// 这个先不处理
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK_NUM) {
								// 计价方式是数量
								price = getIntervalPrice(cvo, pack_num_count);
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, pack_num_count);
								contract_cost = cost;// =区间价格
								bill_value = "" + pack_num_count;
							}
						}
					}
				}
				double loest_fee = 0;
				double loest_cost = 0;
				if(cvo.getLowest_fee() != null) {
					loest_fee = cvo.getLowest_fee().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
				}
				if(cvo.getLowest_cost() != null){
					loest_cost = cvo.getLowest_cost().doubleValue();
				}
				amount = loest_fee > amount ? loest_fee : amount;
				contract_cost = loest_cost > contract_cost ? loest_cost : contract_cost;
				detailBVO.setPrice(new UFDouble(price));
				detailBVO.setAmount(new UFDouble(amount));
				detailBVO.setCost(new UFDouble(cost));
				detailBVO.setContract_cost(new UFDouble(contract_cost));
				detailBVO.setBill_value(bill_value);
				detailBVO.setContract_amount(detailBVO.getAmount());

				// 2015-06-04 对于车型吨位和设备，这里不需要取大或者取小
				if(cvo.getValuation_type() == null
						|| (cvo.getValuation_type().intValue() != ValuationTypeConst.CXDW 
						&& cvo.getValuation_type().intValue() != ValuationTypeConst.EQUIP
						//按包装计费的，进行特殊处理
						&& StringUtils.isNotBlank(cvo.getPack()))) {
					// 看看费用类型是否已经存在，如果已经存在，那么看看金额哪个大
					ReceDetailBVO dd = typeAmountMap.get(detailBVO.getPk_expense_type());
					if(dd != null) {
						// 费用类型已经存在，比较大小
						if(custVO.getBilling_rule() != null
								&& custVO.getBilling_rule().intValue() == BillingRuleConst.MIN) {
							// 取小
							if(dd.getAmount().doubleValue() > amount) {
								detailBVOs.remove(dd);
								detailBVOs.add(detailBVO);
								typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							}
						} else {
							// 取大
							if(dd.getAmount().doubleValue() < amount) {
								detailBVOs.remove(dd);
								detailBVOs.add(detailBVO);
								typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							}
						}
					} else {
						typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
						detailBVOs.add(detailBVO);
					}
				} else {
					detailBVOs.add(detailBVO);
				}
			}
		}
		//处理包装费用
		List<ReceDetailBVO> result = new ArrayList<ReceDetailBVO>();
		if(detailBVOs != null && detailBVOs.size() > 0){
			//对费用明细按照费用类型进行分组，按照设备报价和包装报价会出现多行
			//按照设备报价，只会在合同里维护
			Map<String,List<ReceDetailBVO>> groupMap = new HashMap<String,List<ReceDetailBVO>>();
			for(ReceDetailBVO detailBVO : detailBVOs){
				String key = detailBVO.getPk_expense_type();
				List<ReceDetailBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<ReceDetailBVO>();
					groupMap.put(key, voList);
				}
				voList.add(detailBVO);
			}
			for(String key : groupMap.keySet()){
				//相同费用类型的费用明细
				List<ReceDetailBVO> voList = groupMap.get(key);
				if(voList != null){
					if(voList.size() > 1){
						//同一个费用类型会出现多行，说明匹配到多种费用（因为上面的过滤出来，这里只会是设备报价和包装报价会出现多行）
						//对明细按照相同报价类型进行分组
						Map<Integer,List<ReceDetailBVO>> groupMap0 = new HashMap<Integer,List<ReceDetailBVO>>();
						for(ReceDetailBVO detailBVO : voList){
							Integer key0 = detailBVO.getValuation_type();
							List<ReceDetailBVO> voList0 = groupMap0.get(key0);
							if(voList0 == null){
								voList0 = new ArrayList<ReceDetailBVO>();
								groupMap0.put(key0, voList0);
							}
							voList0.add(detailBVO);
						}
						UFDouble amountTemp = UFDouble.ZERO_DBL;
						UFDouble contract_costTemp = UFDouble.ZERO_DBL;
						List<ReceDetailBVO> voList1 = new ArrayList<ReceDetailBVO>();
						for(Integer key0 : groupMap0.keySet()){
							List<ReceDetailBVO> voList0 = groupMap0.get(key0);
							UFDouble amount = UFDouble.ZERO_DBL;
							UFDouble contract_cost = UFDouble.ZERO_DBL;
							if(voList0 != null && voList0.size() > 0){
								for(ReceDetailBVO detailBVO : voList0){
									amount = amount.add(detailBVO.getAmount() == null ? UFDouble.ZERO_DBL : detailBVO.getAmount());
									contract_cost = contract_cost.add(detailBVO.getContract_cost() == null ? UFDouble.ZERO_DBL : detailBVO.getContract_cost());
								}
							}
							if(amount.doubleValue() >= amountTemp.doubleValue()){
								voList1 = voList0;
								amountTemp = amount;
							}
							//以费用为准
							if(contract_cost.doubleValue() >= contract_costTemp.doubleValue()){
								contract_costTemp = contract_cost;
							}
						}
						result.addAll(voList1);
					}else if(voList.size() == 1){
						result.add(voList.get(0));
					}
				}
				
			}
		}
		return result;
	}
	
	
	public List<ReceDetailBVO> buildReceDetailBVO(ReceDetailBMatchVO _matchVO) {
		ReceDetailBMatchVO matchVO = (ReceDetailBMatchVO) _matchVO.clone();
		List<ReceDetailBVO> detailBVOs = new ArrayList<ReceDetailBVO>();
		if(matchVO.getContractBVOs() != null) {
			Map<String, ReceDetailBVO> typeAmountMap = new HashMap<String, ReceDetailBVO>();
			CustomerVO custVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "pk_customer=?", matchVO.getBala_customer());
			if(custVO == null) {
				throw new BusiException("结算客户[PK:?]已经被删除，请重新选择！",matchVO.getBala_customer());
			}
			// 返回车型吨位的对照信息，并根据费用类型分组，根据件数大到小排序
			Map<String, List<CartypeTonnageVO>> ctMap = new CartypeTonnageServiceImpl().getCartypeTonnageVOMap(matchVO.getPk_corp(),ContractConst.CUSTOMER);
			Map<String, String> expenseTypeCodeMap = new ExpenseTypeServiceImpl().getExpenseTypeCodeMap(matchVO.getPk_corp());

			for(ContractBVO cvo : matchVO.getContractBVOs()) {
				// 这里没有根据设备类型去匹配.
				// 如果计价方式是设备，那么判断这时候的设备类型是否一致，如果不一致，那么过滤该条合同明细
				if(cvo.getValuation_type() != null && cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
					if(matchVO.getPk_car_types() != null && matchVO.getPk_car_types().length > 0 ) {
						boolean exist = false;
						for(String carType : matchVO.getPk_car_types()) {
							if(carType.equals(cvo.getEquip_type())) {
								exist = true;
								break;
							}
						}
						if(!exist) {// 该设备类型不在我们查找之列，过滤掉
							continue;
						}
					} else {// 没有设备类型，那么过滤掉计价方式为设备的合同明细
						continue;
					}
				}
				// 如果计价方式是包装，那么判断这时候是否有包装信息是否一致，如果不一致，那么过滤该条合同明细
				Integer packNum = 0;
				if(StringUtils.isNotBlank(cvo.getPack())){
					if(matchVO.getPackInfos() == null || matchVO.getPackInfos().size() == 0){
						continue;
					}
					boolean exist = true;
					for(PackInfo packInfo : matchVO.getPackInfos()){
						if(packInfo.getPack().equals(cvo.getPack())){
							exist = false;
							//一个合同明细只会匹配到一个包装类型，所以只会有一个数量
							matchVO.setFee_weight_count(packInfo.getWeight());
							matchVO.setNum_count(packInfo.getNum());
							matchVO.setVolume_count(packInfo.getVolume());
							matchVO.setWeight_count(packInfo.getWeight());
							break;
						}
					}
					if (exist) {
						continue;
					}
				}
				ReceDetailBVO detailBVO = new ReceDetailBVO(); // 创建费用明细VO
				detailBVO.setPk_expense_type(cvo.getPk_expense_type()); // 费用类型
				detailBVO.setQuote_type(cvo.getQuote_type());// 报价类型
				detailBVO.setPrice_type(cvo.getPrice_type());// 价格类型
				detailBVO.setValuation_type(cvo.getValuation_type());// 计价方式
				detailBVO.setSystem_create(new UFBoolean(true));
				detailBVO.setPk_contract_b(cvo.getPk_contract_b());

				// 将合同明细的税种，税率冗余到这里
				detailBVO.setTax_cat(cvo.getTax_cat());
				detailBVO.setTax_rate(cvo.getTax_rate());

				// 单价、金额，重头戏
				double price = 0, amount = 0, cost = 0, contract_cost = 0;
				String bill_value = "";
				if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.FIRST_WEIGHT) {
					// 首重报价
					if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE
							&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
						// 单价、重量
						double weight = matchVO.getFee_weight_count().doubleValue() - cvo.getFirst_weight().doubleValue();// 总计费重-首重
						price = getIntervalPrice(cvo, weight);
						amount = cvo.getFirst_weight_price().doubleValue() + weight * price;// =首重价格+（总计费重-首重）×区间价格
						cost = getIntervalCost(cvo, weight);
						contract_cost = cvo.getFirst_weight_cost().doubleValue() + weight * cost;
						bill_value = "" + matchVO.getFee_weight_count();
					} else if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE
							&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
						// 固定价格、重量
						double weight = matchVO.getFee_weight_count().doubleValue() - cvo.getFirst_weight().doubleValue();// 总计费重-首重
						price = getIntervalPrice(cvo, weight);
						amount = cvo.getFirst_weight_price().doubleValue() + price;// =首重价格+区间价格
						cost = getIntervalCost(cvo, weight);
						contract_cost = cvo.getFirst_weight_cost().doubleValue() + cost;
						bill_value = "" + matchVO.getFee_weight_count();
					}
				} else if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.INTERVAL) {
					// 区间报价
					if(cvo.getPrice_type() != null && cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {
						// 价格类型是单价
						if(cvo.getValuation_type() != null) {
							// 2015-06-04 如果合同明细的费用类型是 提货段运费,则根据重量或者体积计算。
							if(cvo.getPk_expense_type().equals(expenseTypeCodeMap.get(ExpenseTypeConst.ET0010))) {// 提货段运费
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, matchVO.getFee_weight_count().doubleValue());
									amount = matchVO.getFee_weight_count().doubleValue() * price;// =总计费重×区间价格
									cost = getIntervalCost(cvo, matchVO.getFee_weight_count().doubleValue());
									contract_cost = matchVO.getFee_weight_count().doubleValue() * cost;
									bill_value = "" + matchVO.getFee_weight_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, matchVO.getVolume_count().doubleValue());
									amount = matchVO.getVolume_count().doubleValue() * price;// =总体积×区间价格
									cost = getIntervalCost(cvo, matchVO.getVolume_count().doubleValue());
									contract_cost = matchVO.getVolume_count().doubleValue() * cost;
									bill_value = "" + matchVO.getVolume_count();
									//计价方式是包装
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, matchVO.getNum_count());
									amount = matchVO.getNum_count() * price;// =总体积×区间价格
									cost = getIntervalCost(cvo, matchVO.getNum_count());
									contract_cost = matchVO.getNum_count() * cost;
									bill_value = "" + matchVO.getNum_count();
									//计价方式是包装
								}else  if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK) {
									price = getIntervalPrice(cvo, packNum);
									amount = packNum * price;// =件数×区间价格
									cost = getIntervalCost(cvo, packNum);
									contract_cost = packNum * cost;
									bill_value = "" + packNum;
								}
							} else {
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
									// 计价方式是件数
									price = getIntervalPrice(cvo, matchVO.getNum_count());
									amount = matchVO.getNum_count() * price;// =总件数×区间价格
									cost = getIntervalCost(cvo, matchVO.getNum_count());
									contract_cost = matchVO.getNum_count() * cost;
									bill_value = "" + matchVO.getNum_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, matchVO.getFee_weight_count().doubleValue());
									amount = matchVO.getFee_weight_count().doubleValue() * price;// =总计费重×区间价格
									cost = getIntervalCost(cvo, matchVO.getFee_weight_count().doubleValue());
									contract_cost = matchVO.getFee_weight_count().doubleValue() * cost;
									bill_value = "" + matchVO.getFee_weight_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, matchVO.getVolume_count().doubleValue());
									amount = matchVO.getVolume_count().doubleValue() * price;// =总体积×区间价格
									cost = getIntervalCost(cvo, matchVO.getVolume_count().doubleValue());
									contract_cost = matchVO.getVolume_count().doubleValue() * cost;
									bill_value = "" + matchVO.getVolume_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK_NUM) {
									// 计价方式是数量
									price = getIntervalPrice(cvo, matchVO.getPack_num_count().doubleValue());
									amount = matchVO.getPack_num_count().doubleValue() * price;// =总数量×区间价格
									cost = getIntervalCost(cvo, matchVO.getPack_num_count().doubleValue());
									contract_cost = matchVO.getPack_num_count().doubleValue() * cost;
									bill_value = "" + matchVO.getPack_num_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDW) {
									// 计价方式是车型吨位
									List<CartypeTonnageVO> ctVOs = ctMap.get(cvo.getPk_expense_type());// 找到这个费用类型的车型吨位对照
									List<CXDWResultVO> resultVOs = getCartypeTonnageAmount(matchVO.getNum_count(),matchVO.getWeight_count(), ctVOs, cvo);
									// 这里根据匹配到几个车型，就记录几条明细
									for(int i = 0; i < resultVOs.size(); i++) {
										CXDWResultVO resultVO = resultVOs.get(i);
										if(i == 0) {
											detailBVO.setCar_weight(resultVO.getCtVO().getWeight());
											detailBVO.setCarnum(resultVO.getCount());
											price = resultVO.getPrice().doubleValue();
											amount = resultVO.getAmount().doubleValue();
											cost = resultVO.getCost().doubleValue();
											contract_cost = resultVO.getContract_cost().doubleValue();
											bill_value = "" +resultVO.getCount();
										} else {
											// 深度拷贝一份
											ReceDetailBVO newVO = (ReceDetailBVO) detailBVO.clone();
											newVO.setPk_receive_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											newVO.setCar_weight(resultVO.getCtVO().getWeight());
											newVO.setCarnum(resultVO.getCount());
											newVO.setPrice(resultVO.getPrice());
											newVO.setAmount(resultVO.getAmount());
											newVO.setCost(resultVO.getCost());
											newVO.setContract_cost(resultVO.getContract_cost());
											newVO.setContract_amount(newVO.getAmount());
											newVO.setBill_value(""+resultVO.getCount());
											detailBVOs.add(newVO);
										}
									}
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.THD) {
									// 计价方式是提货点
									price = cvo.getPrice1().doubleValue();
									amount = price;
									cost = cvo.getCost1().doubleValue();
									contract_cost = cost;
									bill_value = "1";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDWLD) {
									// 车型吨位+重量算法
									List<CXDWLDResultVO> resultVOs = getCXDWAndLDAmount(matchVO.getWeight_count(), cvo);
									// 这里根据匹配到几个车型，就记录几条明细
									for(int i = 0; i < resultVOs.size(); i++) {
										CXDWLDResultVO resultVO = resultVOs.get(i);
										if(i == 0) {
											if(resultVO.getCar_weight() != null
													&& resultVO.getCar_weight().doubleValue() > 0) {
												detailBVO.setValuation_type(ValuationTypeConst.CXDW);
												detailBVO.setBill_value(""+resultVO.getCount());
											} else {
												detailBVO.setValuation_type(ValuationTypeConst.WEIGHT);
												detailBVO.setBill_value(resultVO.getCar_weight().doubleValue() +"");
											}
											detailBVO.setCar_weight(resultVO.getCar_weight());
											detailBVO.setCarnum(resultVO.getCount());
											price = resultVO.getPrice().doubleValue();
											amount = resultVO.getAmount().doubleValue();
											cost = resultVO.getCost().doubleValue();
											contract_cost = resultVO.getContract_cost().doubleValue();
											bill_value = "" + resultVO.getCount();
										} else {
											// 深度拷贝一份
											ReceDetailBVO newVO = (ReceDetailBVO) detailBVO.clone();
											newVO.setPk_receive_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											if(resultVO.getCar_weight() != null
													&& resultVO.getCar_weight().doubleValue() > 0) {
												newVO.setValuation_type(ValuationTypeConst.CXDW);
												newVO.setBill_value(""+resultVO.getCount());
											} else {
												newVO.setValuation_type(ValuationTypeConst.WEIGHT);
												newVO.setBill_value(resultVO.getCar_weight().doubleValue() +"");
											}
											newVO.setCar_weight(resultVO.getCar_weight());
											newVO.setCarnum(resultVO.getCount());
											newVO.setPrice(resultVO.getPrice());
											newVO.setAmount(resultVO.getAmount());
											newVO.setCost(resultVO.getCost());
											newVO.setContract_cost(resultVO.getContract_cost());
											newVO.setContract_amount(newVO.getAmount());
											detailBVOs.add(newVO);
										}
									}
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK) {
									// 计价方式是包装
									price = getIntervalPrice(cvo, packNum);
									amount = packNum * price;// =包装数量*单价
									cost = getIntervalCost(cvo, packNum);
									contract_cost = packNum * cost;
									bill_value = "" + packNum;
								}
							}
						}
					} else if(cvo.getPrice_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE) {
						// 价格类型是固定价格
						if(cvo.getValuation_type() != null) {
							if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
								// 计价方式是重量
								price = getIntervalPrice(cvo, matchVO.getFee_weight_count().doubleValue());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getFee_weight_count().doubleValue());
								contract_cost = cost;// =区间价格
								bill_value = "" + matchVO.getFee_weight_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
								// 计价方式是件数
								price = getIntervalPrice(cvo, matchVO.getNum_count());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getNum_count());
								contract_cost = cost;// =区间价格
								bill_value = "" + matchVO.getNum_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
								// 计价方式是体积
								price = getIntervalPrice(cvo, matchVO.getVolume_count().doubleValue());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getVolume_count().doubleValue());
								contract_cost = cost;// =区间价格
								bill_value = "" + matchVO.getVolume_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
								// 计价方式是设备
								// 2015-06-09根据设备的个数，生成多少条费用明细
								boolean has = false;
								for(int i = 0; i < matchVO.getPk_car_types().length; i++) {
									if(matchVO.getPk_car_types()[i].equals(cvo.getEquip_type())) {
										if(!has) {
											// 第一个费用
											price = cvo.getPrice1() == null ? 0 : cvo.getPrice1().doubleValue();
											amount = price;// =区间价格
											bill_value = cvo.getEquip_type();
											has = true;
										} else {
											// 生成第二个或者第三个费用明细
											ReceDetailBVO newVO = (ReceDetailBVO) detailBVO.clone();
											newVO.setPk_receive_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											newVO.setPrice(cvo.getPrice1());
											newVO.setCost(cvo.getCost1());
											newVO.setAmount(newVO.getPrice());
											newVO.setBill_value(cvo.getEquip_type());
											newVO.setContract_cost(newVO.getCost());
											newVO.setContract_amount(newVO.getAmount());
											detailBVOs.add(newVO);
										}
									}
								}
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TON_KM) {
								// 计价方式是吨公里，暂时不处理
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TICKET) {
								// 计价方式是票
								price = cvo.getPrice1().doubleValue();
								amount = price;// =区间价格
								cost = cvo.getCost1().doubleValue();
								contract_cost = cost;// =区间价格
								bill_value = "1";
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NODE) {
								// 这个先不处理
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK_NUM) {
								// 计价方式是数量
								price = getIntervalPrice(cvo, matchVO.getPack_num_count().doubleValue());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getPack_num_count().doubleValue());
								contract_cost = cost;
								bill_value = "" + matchVO.getPack_num_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK) {
								// 计价方式是包装
								price = getIntervalPrice(cvo, packNum);
								amount = price;// =包装数量*单价
								cost = getIntervalCost(cvo, packNum);
								contract_cost = cost;
								bill_value = "" + packNum;
							}
						}
					}
				}
				double loest_fee = 0,loest_cost = 0;
				if(cvo.getLowest_fee() != null) {
					loest_fee = cvo.getLowest_fee().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
				}
				if(cvo.getLowest_cost() != null){
					loest_cost = cvo.getLowest_cost().doubleValue();
				}
				amount = loest_fee > amount ? loest_fee : amount;
				contract_cost = loest_cost > contract_cost ? loest_cost : contract_cost;
				detailBVO.setPrice(new UFDouble(price));
				detailBVO.setCost(new UFDouble(cost));
				detailBVO.setContract_cost(new UFDouble(contract_cost));
				detailBVO.setAmount(new UFDouble(amount));
				detailBVO.setBill_value(bill_value);
				detailBVO.setContract_amount(detailBVO.getAmount());

				// 2015-06-04 对于车型吨位和设备，这里不需要取大或者取小
				if(cvo.getValuation_type() == null
						|| (cvo.getValuation_type().intValue() != ValuationTypeConst.CXDW 
						&& cvo.getValuation_type().intValue() != ValuationTypeConst.EQUIP
						//按包装计费的，进行特殊处理
						&& StringUtils.isNotBlank(cvo.getPack()))) {
					// 看看费用类型是否已经存在，如果已经存在，那么看看金额哪个大
					ReceDetailBVO dd = typeAmountMap.get(detailBVO.getPk_expense_type());
					if(dd != null) {
						// 费用类型已经存在，比较大小
						if(custVO.getBilling_rule() != null
								&& custVO.getBilling_rule().intValue() == BillingRuleConst.MIN) {
							// 取小
							if(dd.getAmount().doubleValue() > amount) {
								detailBVOs.remove(dd);
								detailBVOs.add(detailBVO);
								typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							}
						} else {
							// 取大
							if(dd.getAmount().doubleValue() < amount) {
								detailBVOs.remove(dd);
								detailBVOs.add(detailBVO);
								typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							}
						}
					} else {
						typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
						detailBVOs.add(detailBVO);
					}
				} else {
					detailBVOs.add(detailBVO);
				}
			}
		}
		//处理包装费用
		List<ReceDetailBVO> result = new ArrayList<ReceDetailBVO>();
		if(detailBVOs != null && detailBVOs.size() > 0){
			//对费用明细按照费用类型进行分组，按照设备报价和包装报价会出现多行
			//按照设备报价，只会在合同里维护
			Map<String,List<ReceDetailBVO>> groupMap = new HashMap<String,List<ReceDetailBVO>>();
			for(ReceDetailBVO detailBVO : detailBVOs){
				String key = detailBVO.getPk_expense_type();
				List<ReceDetailBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<ReceDetailBVO>();
					groupMap.put(key, voList);
				}
				voList.add(detailBVO);
			}
			for(String key : groupMap.keySet()){
				//相同费用类型的费用明细
				List<ReceDetailBVO> voList = groupMap.get(key);
				if(voList != null){
					if(voList.size() > 1){
						//同一个费用类型会出现多行，说明匹配到多种费用（因为上面的过滤出来，这里只会是设备报价和包装报价会出现多行）
						//对明细按照相同报价类型进行分组
						Map<Integer,List<ReceDetailBVO>> groupMap0 = new HashMap<Integer,List<ReceDetailBVO>>();
						for(ReceDetailBVO detailBVO : voList){
							Integer key0 = detailBVO.getValuation_type();
							List<ReceDetailBVO> voList0 = groupMap0.get(key0);
							if(voList0 == null){
								voList0 = new ArrayList<ReceDetailBVO>();
								groupMap0.put(key0, voList0);
							}
							voList0.add(detailBVO);
						}
						UFDouble amountTemp = UFDouble.ZERO_DBL;
						List<ReceDetailBVO> voList1 = new ArrayList<ReceDetailBVO>();
						for(Integer key0 : groupMap0.keySet()){
							List<ReceDetailBVO> voList0 = groupMap0.get(key0);
							UFDouble amount = UFDouble.ZERO_DBL;
							if(voList0 != null && voList0.size() > 0){
								for(ReceDetailBVO detailBVO : voList0){
									amount = amount.add(detailBVO.getAmount() == null ? UFDouble.ZERO_DBL : detailBVO.getAmount());
								}
							}
							if(amount.doubleValue() >= amountTemp.doubleValue()){
								voList1 = voList0;
								amountTemp = amount;
							}
						}
						result.addAll(voList1);
					}else if(voList.size() == 1){
						result.add(voList.get(0));
					}
				}
				
			}
		}
		return result;
	}
	
	
	

	/**
	 * 根据合同VO生成费用明细，这是关键的方法，需要根据不同的条件计算单价及金额<br/>
	 * 2015-05-17
	 * 委托单在匹配合同时若出现相同类型的费用明细时系统可取其中的一条费用类型保留（根据承运商的计费规则）。可以取匹配金额的大值还是小值。默认取大
	 * 
	 * @param num_count
	 *            总件数-用于确定单价以及计算金额
	 * @param fee_weight_count
	 *            总计费重-用于确定单价以及计算金额
	 * @param volume_count
	 *            总体积-用于确定单价以及计算金额
	 * @param pk_car_type
	 *            设备类型-用于确定单价以及计算金额
	 * @param contractBVOs
	 * @return
	 */
	
	public List<PayDetailBVO> buildPayDetailBVO(String pk_carrier, double pack_num_count, int num_count,
			double fee_weight_count, double weight_count, double volume_count, int node_count, int deli_node_count,
			List<PackInfo> packInfos,
			String[] pk_car_type, String pk_corp, Integer urgent_level, String item_code, String pk_trans_line,
			UFBoolean if_return, List<ContractBVO> contractBVOs) {
		return buildPayDetailBVO(null, pk_carrier, pack_num_count, num_count, fee_weight_count, weight_count, volume_count, node_count, deli_node_count, packInfos, pk_car_type, pk_corp, urgent_level, item_code, pk_trans_line, if_return, contractBVOs);
	}
	
	//检查可选费用项 如果有可选费用返回费用项
	public void getOptional(List<ContractBVO> contractBVOs,String pk_entrust){
		boolean flag = false;
		for(ContractBVO cvo : contractBVOs) {
			if(cvo.getIf_optional() != null && cvo.getIf_optional().equals(UFBoolean.TRUE)){
				flag = true;
				break;
			}
		}
		EntOperationBVO[] entOperationBVOs = null;
		if(flag){
			if(StringUtils.isNotBlank(pk_entrust)){
				//计算可选费用但是没有委托单。
				//查找这个委托单下的可选费用项。
				entOperationBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntOperationBVO.class, 
						"pk_entrust=?", pk_entrust);
			}
			//同一个费用，可能会对多个费用类型。
			String expenseTypeCond = "(";
			if(entOperationBVOs != null && entOperationBVOs.length > 0){
				for(int i=0;i<entOperationBVOs.length;i++){
					expenseTypeCond += entOperationBVOs[i].getOperation_type() +",";
				}
			}
			if(expenseTypeCond.length() > 1){
				expenseTypeCond = expenseTypeCond.substring(0, expenseTypeCond.length()-1) + ")";
				//获取费用类型
				ExpenseTypeVO[] expenseTypeVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ExpenseTypeVO.class, 
						"operation_type in " + expenseTypeCond);
				if(expenseTypeVOs != null && expenseTypeVOs.length > 0){
					for(EntOperationBVO entOperationBVO : entOperationBVOs){
						for(ExpenseTypeVO expenseTypeVO : expenseTypeVOs){
							if(entOperationBVO.getOperation_type().equals(expenseTypeVO.getOperation_type())){
								entOperationBVO.setPk_expense_type(entOperationBVO.getPk_expense_type() == null ? 
										expenseTypeVO.getPk_expense_type() 
										: entOperationBVO.getPk_expense_type() + "," + expenseTypeVO.getPk_expense_type());
							}
						}
					}
					
				}
			}
		}
		
	}
	
	public List<PayDetailBVO> buildPayDetailBVO(PayDetailBMatchVO _matchVO) {
		PayDetailBMatchVO matchVO = (PayDetailBMatchVO) _matchVO.clone();
		List<PayDetailBVO> detailBVOs = new ArrayList<PayDetailBVO>();
		if(matchVO == null || matchVO.getContractBVOs() == null || matchVO.getContractBVOs().size() == 0){
			return detailBVOs;
		}
		Map<String, PayDetailBVO> typeAmountMap = new HashMap<String, PayDetailBVO>();
		CarrierVO carrVO = NWDao.getInstance().queryByCondition(CarrierVO.class, "pk_carrier=?", matchVO.getPk_carrier());
		if(carrVO == null) {
			throw new BusiException("承运商[PK:?]已经被删除，请重新选择！",matchVO.getPk_carrier());
		}
		// 返回车型吨位的对照信息，并根据费用类型分组，根据件数大到小排序
		Map<String, List<CartypeTonnageVO>> ctMap = new CartypeTonnageServiceImpl().getCartypeTonnageVOMap(matchVO.getPk_corp(),ContractConst.CARRIER);
		Map<String, String> expenseTypeCodeMap = new ExpenseTypeServiceImpl().getExpenseTypeCodeMap(matchVO.getPk_corp());
		for(ContractBVO cvo : matchVO.getContractBVOs()) {
			// 如果计价方式是设备，那么判断这时候的设备类型是否一致，如果不一致，那么过滤该条合同明细
			if(cvo.getValuation_type() != null && cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP){
				String[] pk_car_types = matchVO.getPk_car_types();
				boolean exist = false;
				if(pk_car_types != null && pk_car_types.length > 0 && StringUtils.isNotBlank(pk_car_types[0])){
					for(String carType : pk_car_types){
						if(carType.equals(cvo.getEquip_type())) {
							exist = true;
							break;
						}
					}
				}
				if(!exist) {// 该设备类型不在我们查找之列，过滤掉
					continue;
				}
			}
			// 如果计价方式是包装，那么判断这时候的是否有匹配的包装，如果不一致，那么过滤该条合同明细
			if(cvo.getValuation_type() != null && cvo.getValuation_type().intValue() == ValuationTypeConst.PACK){
//				Map<String,Integer> packNums = matchVO.getPackNums();
//				boolean exist = false;
//				if(packNums != null && packNums.size() > 0){
//					for(String pack : packNums.keySet()){
//						if(StringUtils.isNotBlank(pack) && StringUtils.isNotBlank(cvo.getPack()) && pack.equals(cvo.getPack())){
//							exist = false;
//							break;
//						}
//					}
//				}
//				if (exist) {
//					continue;
//				}
			}
			//或有费用需要先进行判断
			if(cvo.getIf_optional() != null && cvo.getIf_optional().equals(UFBoolean.TRUE)){
				Map<String,List<EntOperationBVO>> operationMaps = matchVO.getOperationMaps();
				if(operationMaps == null || operationMaps.size() == 0){
					continue;
				}
				List<EntOperationBVO> operationVOs = operationMaps.get(cvo.getPk_expense_type());
				if(operationVOs == null || operationVOs.size() == 0){
					continue;
				}
				for(EntOperationBVO operationVO : operationVOs){
					PayDetailBVO detailBVO = new PayDetailBVO(); // 创建费用明细VO
					detailBVO.setPk_expense_type(cvo.getPk_expense_type()); // 费用类型
					detailBVO.setQuote_type(cvo.getQuote_type());// 报价类型
					detailBVO.setPrice_type(cvo.getPrice_type());// 价格类型
					detailBVO.setValuation_type(cvo.getValuation_type());// 计价方式
					detailBVO.setSystem_create(UFBoolean.TRUE);
					detailBVO.setPk_contract_b(cvo.getPk_contract_b());

					// 将合同明细的税种，税率冗余到这里
					detailBVO.setTax_cat(cvo.getTax_cat());
					detailBVO.setTax_rate(cvo.getTax_rate());
					// 单价、金额，重头戏
					double price = 0, amount = 0, cost = 0, contract_cost = 0;
					String memo = "";
					String bill_value = "";
					//或有费用匹配，只匹配区间报价
					//这需要计价方式和费用类型和合同一致才进行匹配
					if(cvo.getQuote_type() != null 
							&& cvo.getQuote_type().intValue() == QuoteTypeConst.INTERVAL
							&& cvo.getValuation_type() != null 
							&& cvo.getValuation_type().equals(operationVO.getValuation_type())){
						// 单价
						if(cvo.getPrice_type() != null && cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {
							//只采用区间报价 都是value * price
							price = getIntervalPrice(cvo, operationVO.getOperation_value().doubleValue());
							amount = operationVO.getOperation_value().doubleValue()  * price;// =或有费用录入的值×区间价格
							cost = getIntervalCost(cvo, operationVO.getOperation_value().doubleValue());
							contract_cost = operationVO.getOperation_value().doubleValue()  * cost;// =或有费用录入的值×区间价格
							bill_value = "" + operationVO.getOperation_value();
							double loest_fee = 0,loest_cost = 0;
							if(cvo.getLowest_fee() != null) {
								loest_fee = cvo.getLowest_fee().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
							}
							if(cvo.getLowest_cost() != null) {
								loest_cost = cvo.getLowest_cost().doubleValue();
							}
							detailBVO.setPrice(new UFDouble(price));
							detailBVO.setCost(new UFDouble(cost));
							detailBVO.setAmount(new UFDouble(loest_fee > amount ? loest_fee : amount));
							detailBVO.setContract_cost(new UFDouble(loest_cost > contract_cost ? loest_cost : contract_cost));
							detailBVO.setBill_value(bill_value);
							detailBVO.setMemo(memo);
							detailBVO.setContract_amount(detailBVO.getAmount());
							detailBVOs.add(detailBVO);//或有费用匹配不需要取大取小，因为需求不一样，直接保存就行。
						}else{
							logger.info("或有费用计算，只支持区间报价的单价类型！");
						}
						
					}
				}
			}else{
				PayDetailBVO detailBVO = new PayDetailBVO(); // 创建费用明细VO
				detailBVO.setPk_expense_type(cvo.getPk_expense_type()); // 费用类型
				detailBVO.setQuote_type(cvo.getQuote_type());// 报价类型
				detailBVO.setPrice_type(cvo.getPrice_type());// 价格类型
				detailBVO.setValuation_type(cvo.getValuation_type());// 计价方式
				detailBVO.setSystem_create(UFBoolean.TRUE);
				detailBVO.setPk_contract_b(cvo.getPk_contract_b());

				// 将合同明细的税种，税率冗余到这里
				detailBVO.setTax_cat(cvo.getTax_cat());
				detailBVO.setTax_rate(cvo.getTax_rate());
				// 单价、金额，重头戏
				double price = 0, amount = 0, cost = 0, contract_cost = 0;
				String memo = "";
				String bill_value = "";
				if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.FIRST_WEIGHT) {
					// 首重报价
					if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE
							&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
						// 单价、重量
						double weight = matchVO.getFee_weight_count().doubleValue() - cvo.getFirst_weight().doubleValue();// 总计费重-首重
						price = getIntervalPrice(cvo, weight);
						amount = cvo.getFirst_weight_price().doubleValue() + weight * price;// =首重价格+（总计费重-首重）×区间价格
						cost = getIntervalCost(cvo, weight);
						contract_cost = cvo.getFirst_weight_cost().doubleValue() + weight * cost;
						bill_value = "" + matchVO.getFee_weight_count();
					} else if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
							&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE
							&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
						// 固定价格、重量
						double weight = matchVO.getFee_weight_count().doubleValue() - cvo.getFirst_weight().doubleValue();// 总计费重-首重
						price = getIntervalPrice(cvo, weight);
						amount = cvo.getFirst_weight_price().doubleValue() + price;// =首重价格+区间价格
						cost = getIntervalCost(cvo, weight);
						contract_cost = cvo.getFirst_weight_cost().doubleValue() + cost;
						bill_value = "" + matchVO.getFee_weight_count();
					}
				} else if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.INTERVAL) {
					// 区间报价
					if(cvo.getPrice_type() != null && cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {
						if(cvo.getValuation_type() != null) {
							// 如果合同明细的费用类型是 提货段运费,则根据重量或者体积计算。
							//FIXME 提货段运费计算，不再需要。
							if(cvo.getPk_expense_type().equals(expenseTypeCodeMap.get(ExpenseTypeConst.ET0010))) {// 提货段运费
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, matchVO.getFee_weight_count().doubleValue());
									amount = matchVO.getFee_weight_count().doubleValue() * price;// =总计费重×区间价格
									cost = getIntervalCost(cvo, matchVO.getFee_weight_count().doubleValue());
									contract_cost = matchVO.getFee_weight_count().doubleValue() * cost;
									bill_value = "" + matchVO.getFee_weight_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, matchVO.getVolume_count().doubleValue());
									amount = matchVO.getVolume_count().doubleValue()* price;// =总体积×区间价格
									cost = getIntervalCost(cvo, matchVO.getVolume_count().doubleValue());
									contract_cost = matchVO.getVolume_count().doubleValue() * cost;
									bill_value = "" + matchVO.getVolume_count();
								}
							} else {
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, matchVO.getFee_weight_count().doubleValue());
									amount = matchVO.getFee_weight_count().doubleValue() * price;// =总计费重×区间价格
									cost = getIntervalCost(cvo, matchVO.getFee_weight_count().doubleValue());
									contract_cost = matchVO.getFee_weight_count().doubleValue() * cost;
									bill_value = "" + matchVO.getFee_weight_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
									// 计价方式是件数
									price = getIntervalPrice(cvo, matchVO.getNum_count());
									amount = matchVO.getNum_count() * price;// =总件数×区间价格
									cost = getIntervalCost(cvo, matchVO.getNum_count());
									contract_cost = matchVO.getNum_count() * cost;
									bill_value = "" + matchVO.getNum_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, matchVO.getVolume_count().doubleValue());
									amount = matchVO.getVolume_count().doubleValue() * price;// =总体积×区间价格
									cost = getIntervalCost(cvo, matchVO.getVolume_count().doubleValue());
									contract_cost = matchVO.getVolume_count().doubleValue() * cost;
									bill_value = "" + matchVO.getVolume_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDW) {
									// 计价方式是车型吨位
									List<CartypeTonnageVO> ctVOs = ctMap.get(cvo.getPk_expense_type());// 找到这个费用类型的车型吨位对照
									List<CXDWResultVO> resultVOs = getCartypeTonnageAmount(matchVO.getNum_count(), matchVO.getWeight_count(), ctVOs, cvo);
									// 这里根据匹配到几个车型，就记录几条明细
									for(int i = 0; i < resultVOs.size(); i++) {
										CXDWResultVO resultVO = resultVOs.get(i);
										if(i == 0) {
											price = resultVO.getPrice().doubleValue();
											amount = resultVO.getAmount().doubleValue();
											cost = resultVO.getCost().doubleValue();
											contract_cost = resultVO.getContract_cost().doubleValue();
											bill_value = "1";
											//增加车型吨位类型到备注上JONATHAN2015-10-29;
											if(resultVO.getCtVO()!=null){
												memo=String.valueOf(resultVO.getCtVO().getWeight().doubleValue());
											}
										} else {
											PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
											newVO.setPk_pay_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											newVO.setPrice(resultVO.getPrice());
											newVO.setCost(resultVO.getCost());
											newVO.setContract_cost(resultVO.getContract_cost());
											newVO.setAmount(resultVO.getAmount());
											newVO.setBill_value("1");
											newVO.setContract_amount(newVO.getAmount());
											//增加车型吨位类型到备注上JONATHAN2015-10-29;
											if(resultVO.getCtVO()!=null){
											newVO.setMemo(String.valueOf(resultVO.getCtVO().getWeight().doubleValue()));
											}
											detailBVOs.add(newVO);
										}
									}
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.THD) {
									// 计价方式是提货点
									price = cvo.getPrice1().doubleValue();
									amount = matchVO.getDeli_node_count() * price;
									cost = cvo.getCost1().doubleValue();
									contract_cost = matchVO.getDeli_node_count() * cost;
									bill_value = "" + matchVO.getDeli_node_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.DHD) {
									// 计价方式是到货点
									price = cvo.getPrice1().doubleValue();
									amount = matchVO.getArri_node_count() * price;
									cost = cvo.getCost1().doubleValue();
									contract_cost = matchVO.getArri_node_count() * cost;
									bill_value = "" + matchVO.getArri_node_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NODE) {
									// 计价方式是节点
									price = cvo.getPrice1().doubleValue();
									amount = matchVO.getNode_count() * price;
									cost = cvo.getCost1().doubleValue();
									contract_cost = matchVO.getNode_count() * cost;
									bill_value = "" + matchVO.getNode_count();
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDWLD) {
									// 车型吨位+重量算法
									List<CXDWLDResultVO> resultVOs = getCXDWAndLDAmount(matchVO.getWeight_count(), cvo);
									// 这里根据匹配到几个车型，就记录几条明细
									for(int i = 0; i < resultVOs.size(); i++) {
										CXDWLDResultVO resultVO = resultVOs.get(i);
										if(i == 0) {
											if(resultVO.getCar_weight() != null
													&& resultVO.getCar_weight().doubleValue() > 0) {
												detailBVO.setValuation_type(ValuationTypeConst.CXDW);
												detailBVO.setBill_value(""+resultVO.getCount());
											} else {
												detailBVO.setValuation_type(ValuationTypeConst.WEIGHT);
												detailBVO.setBill_value(""+resultVO.getCar_weight());;
											}
											price = resultVO.getPrice().doubleValue();
											amount = resultVO.getAmount().doubleValue();
											cost = resultVO.getCost().doubleValue();
											contract_cost = resultVO.getContract_cost().doubleValue();
											memo=resultVO.getCar_weight().toString();
										} else {
											// 深度拷贝一份
											PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
											newVO.setPk_pay_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											if(resultVO.getCar_weight() != null
													&& resultVO.getCar_weight().doubleValue() > 0) {
												newVO.setValuation_type(ValuationTypeConst.CXDW);
												newVO.setBill_value(""+resultVO.getCount());
											} else {
												newVO.setValuation_type(ValuationTypeConst.WEIGHT);
												newVO.setBill_value(""+resultVO.getCar_weight());;
											}
											newVO.setPrice(resultVO.getPrice());
											newVO.setAmount(resultVO.getAmount());
											newVO.setCost(resultVO.getCost());
											newVO.setContract_cost(resultVO.getContract_cost());
											newVO.setContract_amount(newVO.getAmount());
											//增加车型吨位类型到备注上JONATHAN2015-10-29;
											newVO.setMemo(resultVO.getCar_weight().toString());
											detailBVOs.add(newVO);
										}
									}
								}
							}
						}
					} else if(cvo.getPrice_type() != null// 价格类型是固定价格
							&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE) {
						if(cvo.getValuation_type() != null) {
							//对于重量  体积 票数 件数 数量 包装的计价方式 ，固定价格
							if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT ) {
								// 计价方式是重量
								price = getIntervalPrice(cvo, matchVO.getFee_weight_count().doubleValue());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getFee_weight_count().doubleValue());
								contract_cost = cost;
								bill_value = "" + matchVO.getFee_weight_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
								// 计价方式是件数
								price = getIntervalPrice(cvo, matchVO.getNum_count());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getNum_count());
								contract_cost = cost;
								bill_value = "" + matchVO.getNum_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
								// 计价方式是体积
								price = getIntervalPrice(cvo, matchVO.getVolume_count().doubleValue());
								amount = price;// =区间价格
								cost = getIntervalCost(cvo, matchVO.getVolume_count().doubleValue());
								contract_cost = cost;
								bill_value = "" + matchVO.getVolume_count();
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
								// 计价方式是设备
								// 2015-06-09根据设备的个数，生成多少条费用明细
								boolean has = false;
								for(int i = 0; i < matchVO.getPk_car_types().length; i++) {
									if(matchVO.getPk_car_types()[i].equals(cvo.getEquip_type())) {
										if(!has) {
											// 第一个费用
											price = cvo.getPrice1() == null ? 0 : cvo.getPrice1().doubleValue();
											amount = price;// =区间价格
											cost = cvo.getCost1() == null ? 0 : cvo.getCost1().doubleValue();
											contract_cost = cost;
											has = true;
											bill_value = cvo.getEquip_type();
										} else {
											// 生成第二个或者第三个费用明细
											PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
											newVO.setPk_pay_detail(null);
											NWDao.setUuidPrimaryKey(newVO);
											newVO.setPrice(cvo.getPrice1());
											newVO.setCost(cvo.getCost1());
											newVO.setContract_cost(newVO.getCost());
											newVO.setAmount(newVO.getPrice());
											newVO.setContract_amount(newVO.getAmount());
											newVO.setBill_value(cvo.getEquip_type());
											detailBVOs.add(newVO);
										}
									}
								}
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TON_KM) {
								// 计价方式是吨公里，暂时不处理
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TICKET) {
								// 计价方式是票，暂时不处理
							} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK_NUM) {
								// 计价方式是数量
								price = getIntervalPrice(cvo, matchVO.getPack_num_count().doubleValue());
								amount = price;
								cost = getIntervalCost(cvo, matchVO.getPack_num_count().doubleValue());
								contract_cost = cost;
								bill_value = "" + matchVO.getPack_num_count();
							}
						}
					}
				}
				double loest_fee = 0,loest_cost = 0;
				if(cvo.getLowest_fee() != null) {
					loest_fee = cvo.getLowest_fee().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
				}
				if(cvo.getLowest_cost() != null) {
					loest_cost = cvo.getLowest_cost().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
				}
				amount = loest_fee > amount ? loest_fee : amount;
				contract_cost = loest_cost > contract_cost ? loest_cost : contract_cost;
				detailBVO.setPrice(new UFDouble(price));
				detailBVO.setAmount(new UFDouble(amount));
				detailBVO.setCost(new UFDouble(cost));
				detailBVO.setContract_cost(new UFDouble(contract_cost));
				detailBVO.setContract_amount(detailBVO.getAmount());
				detailBVO.setBill_value(bill_value);
				detailBVO.setMemo(memo);
				// 2015-06-04 对于车型吨位和设备，这里不需要取大或者取小
				if(cvo.getValuation_type() == null
						|| (cvo.getValuation_type().intValue() != ValuationTypeConst.CXDW 
							&& cvo.getValuation_type().intValue() != ValuationTypeConst.EQUIP
							&& StringUtils.isNotBlank(cvo.getPack()))) {
					// 看看费用类型是否已经存在，如果已经存在，那么看看金额哪个大
					PayDetailBVO dd = typeAmountMap.get(detailBVO.getPk_expense_type());
					if(dd != null) {
						// 费用类型已经存在，比较大小
						if(carrVO.getBilling_rule() != null
								&& carrVO.getBilling_rule().intValue() == BillingRuleConst.MIN) {
							// 取小
							if(dd.getAmount().doubleValue() > amount) {
								detailBVOs.remove(dd);
								detailBVOs.add(detailBVO);
								typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							}
						} else {
							// 取大
							if(dd.getAmount().doubleValue() < amount) {
								detailBVOs.remove(dd);
								detailBVOs.add(detailBVO);
								typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							}
						}
					} else {
						typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
						detailBVOs.add(detailBVO);
					}
				} else {
					detailBVOs.add(detailBVO);
				}
			}
		}
		
		//处理包装费用
		List<PayDetailBVO> result = new ArrayList<PayDetailBVO>();
		if(detailBVOs != null && detailBVOs.size() > 0){
			//对费用明细按照费用类型进行分组，按照设备报价和包装报价会出现多行
			//按照设备报价，只会在合同里维护
			Map<String,List<PayDetailBVO>> groupMap = new HashMap<String,List<PayDetailBVO>>();
			for(PayDetailBVO detailBVO : detailBVOs){
				String key = detailBVO.getPk_expense_type();
				List<PayDetailBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<PayDetailBVO>();
					groupMap.put(key, voList);
				}
				voList.add(detailBVO);
			}
			for(String key : groupMap.keySet()){
				//相同费用类型的费用明细
				List<PayDetailBVO> voList = groupMap.get(key);
				if(voList != null){
					if(voList.size() > 1){
						//同一个费用类型会出现多行，说明匹配到多种费用（因为上面的过滤出来，这里只会是设备报价和包装报价会出现多行）
						//对明细按照相同报价类型进行分组
						Map<Integer,List<PayDetailBVO>> groupMap0 = new HashMap<Integer,List<PayDetailBVO>>();
						for(PayDetailBVO detailBVO : voList){
							Integer key0 = detailBVO.getValuation_type();
							List<PayDetailBVO> voList0 = groupMap0.get(key0);
							if(voList0 == null){
								voList0 = new ArrayList<PayDetailBVO>();
								groupMap0.put(key0, voList0);
							}
							voList0.add(detailBVO);
						}
						UFDouble amountTemp = UFDouble.ZERO_DBL;
						List<PayDetailBVO> voList1 = new ArrayList<PayDetailBVO>();
						for(Integer key0 : groupMap0.keySet()){
							List<PayDetailBVO> voList0 = groupMap0.get(key0);
							UFDouble amount = UFDouble.ZERO_DBL;
							if(voList0 != null && voList0.size() > 0){
								for(PayDetailBVO detailBVO : voList0){
									amount = amount.add(detailBVO.getAmount() == null ? UFDouble.ZERO_DBL : detailBVO.getAmount());
								}
							}
							if(amount.doubleValue() >= amountTemp.doubleValue()){
								voList1 = voList0;
								amountTemp = amount;
							}
						}
						result.addAll(voList1);
					}else if(voList.size() == 1){
						result.add(voList.get(0));
					}
				}
				
			}
		}
		return result;
	}
	
	public List<PayDetailBVO> buildPayDetailBVO(String strPkEntrust, String pk_carrier, double _pack_num_count, int _num_count,
			double _fee_weight_count, double _weight_count, double _volume_count, int node_count, int deli_node_count,
			List<PackInfo> packInfos,
			String[] pk_car_type, String pk_corp,Integer urgent_level, String item_code, String pk_trans_line,UFBoolean if_return, List<ContractBVO> contractBVOs) {
		double pack_num_count = _pack_num_count;
		int num_count = _num_count;
		double fee_weight_count = _fee_weight_count;
		double weight_count = _weight_count;
		double volume_count = _volume_count;
		List<PayDetailBVO> detailBVOs = new ArrayList<PayDetailBVO>();
		if(contractBVOs != null) {
			Map<String, PayDetailBVO> typeAmountMap = new HashMap<String, PayDetailBVO>();
			CarrierVO carrVO = NWDao.getInstance().queryByCondition(CarrierVO.class, "pk_carrier=?", pk_carrier);
			if(carrVO == null) {
				throw new BusiException("承运商[PK:?]已经被删除，请重新选择！",pk_carrier);
			}
			// 返回车型吨位的对照信息，并根据费用类型分组，根据件数大到小排序
			Map<String, List<CartypeTonnageVO>> ctMap = new CartypeTonnageServiceImpl().getCartypeTonnageVOMap(pk_corp,ContractConst.CARRIER);
			Map<String, String> expenseTypeCodeMap = new ExpenseTypeServiceImpl().getExpenseTypeCodeMap(pk_corp);
			
			//可选费用 
			//检查合同明细里有没有可选费用
			boolean flag = false;
			for(ContractBVO cvo : contractBVOs) {
				if(cvo.getIf_optional() != null && cvo.getIf_optional().equals(UFBoolean.TRUE)){
					flag = true;
					break;
				}
			}
			EntOperationBVO[] entOperationBVOs = null;
			if(flag){
				if(StringUtils.isNotBlank(strPkEntrust)){
					//计算可选费用但是没有委托单。
					//查找这个委托单下的可选费用项。
					entOperationBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntOperationBVO.class, 
							"pk_entrust=?", strPkEntrust);
				}
				//同一个费用，可能会对多个费用类型。
				String expenseTypeCond = "(";
				if(entOperationBVOs != null && entOperationBVOs.length > 0){
					for(int i=0;i<entOperationBVOs.length;i++){
						expenseTypeCond += entOperationBVOs[i].getOperation_type() +",";
					}
				}
				if(expenseTypeCond.length() > 1){
					expenseTypeCond = expenseTypeCond.substring(0, expenseTypeCond.length()-1) + ")";
					//获取费用类型
					ExpenseTypeVO[] expenseTypeVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ExpenseTypeVO.class, 
							"operation_type in " + expenseTypeCond);
					if(expenseTypeVOs != null && expenseTypeVOs.length > 0){
						for(EntOperationBVO entOperationBVO : entOperationBVOs){
							for(ExpenseTypeVO expenseTypeVO : expenseTypeVOs){
								if(entOperationBVO.getOperation_type().equals(expenseTypeVO.getOperation_type())){
									entOperationBVO.setPk_expense_type(entOperationBVO.getPk_expense_type() == null ? 
											expenseTypeVO.getPk_expense_type() 
											: entOperationBVO.getPk_expense_type() + "," + expenseTypeVO.getPk_expense_type());
								}
							}
						}
						
					}
				}
			}
			
			for(ContractBVO cvo : contractBVOs) {
				// 如果计价方式是设备，那么判断这时候的设备类型是否一致，如果不一致，那么过滤该条合同明细
				if(cvo.getValuation_type() != null && cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
					if(pk_car_type != null && pk_car_type.length > 0) {
						boolean exist = false;
						for(String carType : pk_car_type) {
							if(carType.equals(cvo.getEquip_type())) {
								exist = true;
								break;
							}
						}
						if(!exist) {// 该设备类型不在我们查找之列，过滤掉
							continue;
						}
					} else {// 没有设备类型，那么过滤掉计价方式为设备的合同明细
						continue;
					}
				}
				if(StringUtils.isNotBlank(cvo.getPack())){
					if(packInfos == null || packInfos.size() == 0){
						continue;
					}
					boolean exist = true;
					for(PackInfo info : packInfos){
						if(info.getPack().equals(cvo.getPack())){
							exist = false;
							//一个合同明细只会匹配到一个包装类型，所以只会有一个数量
							//packInfo = info;
							fee_weight_count = info.getWeight().doubleValue();
							num_count = info.getNum();
							volume_count = info.getVolume().doubleValue();
							weight_count = info.getWeight().doubleValue();
							break;
						}
					}
					if (exist) {
						continue;
					}
				}
				//进行必有费用和或有费用的判断
				if(cvo.getIf_optional() != null && cvo.getIf_optional().equals(UFBoolean.TRUE)){
					if(entOperationBVOs != null && entOperationBVOs.length > 0){
						for(EntOperationBVO entOperationBVO : entOperationBVOs){
							//一个可选费用项，可能会有多个费用类型
							//但是每个合同明细只能匹配到一个费用类型的
							String[] pk_expense_types = entOperationBVO.getPk_expense_type() == null ? null :entOperationBVO.getPk_expense_type().split(",");
							
							if(pk_expense_types == null || pk_expense_types.length == 0){
								break;
							}
							for(String pk_expense_type : pk_expense_types){
								if(pk_expense_type.equals(cvo.getPk_expense_type())){
									PayDetailBVO detailBVO = new PayDetailBVO(); // 创建费用明细VO
									detailBVO.setPk_expense_type(cvo.getPk_expense_type()); // 费用类型
									detailBVO.setQuote_type(cvo.getQuote_type());// 报价类型
									detailBVO.setPrice_type(cvo.getPrice_type());// 价格类型
									detailBVO.setValuation_type(cvo.getValuation_type());// 计价方式
									detailBVO.setSystem_create(UFBoolean.TRUE);
									detailBVO.setPk_contract_b(cvo.getPk_contract_b());

									// 将合同明细的税种，税率冗余到这里
									detailBVO.setTax_cat(cvo.getTax_cat());
									detailBVO.setTax_rate(cvo.getTax_rate());
									// 单价、金额，重头戏
									double price = 0, amount = 0,cost = 0, contract_cost = 0;
									String memo = "";
									String bill_value = "";
									//或有费用匹配，只匹配区间报价
									//这需要计价方式和合同一致才进行匹配
									if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.INTERVAL
											&& cvo.getValuation_type() != null 
											&& cvo.getValuation_type().equals(entOperationBVO.getValuation_type())) {
										// 区间报价
										if(cvo.getPrice_type() != null && cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {
											//只采用区间报价 都是value * price
											price = getIntervalPrice(cvo, entOperationBVO.getOperation_value().doubleValue());
											amount = entOperationBVO.getOperation_value().doubleValue()  * price;// =总计费重×区间价格
											cost = getIntervalCost(cvo, entOperationBVO.getOperation_value().doubleValue());
											contract_cost = entOperationBVO.getOperation_value().doubleValue()  * cost;
											bill_value = "" + entOperationBVO.getOperation_value();
										} else if(cvo.getPrice_type() != null
												&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE) {
											// 价格类型是固定价格
											// 这里要保证合同计价方式和操作的计价方式保持一致。
											if(cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP
													&& cvo.getValuation_type().equals(entOperationBVO.getValuation_type())) {
												// 计价方式是设备
												boolean has = false;
												for(int i = 0; i < pk_car_type.length; i++) {
													if(pk_car_type[i].equals(cvo.getEquip_type())) {
														if(!has) {
															// 第一个费用
															price = cvo.getPrice1() == null ? 0 : cvo.getPrice1().doubleValue();
															amount = price;// =区间价格
															cost = cvo.getCost1() == null ? 0 : cvo.getCost1().doubleValue();
															contract_cost = cost;
															has = true;
															bill_value = cvo.getEquip_type();
														} else {
															// 生成第二个或者第三个费用明细
															PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
															newVO.setPk_pay_detail(null);
															NWDao.setUuidPrimaryKey(newVO);
															newVO.setPrice(cvo.getPrice1());
															newVO.setCost(cvo.getCost1());
															newVO.setAmount(newVO.getPrice());
															newVO.setBill_value(cvo.getEquip_type());
															newVO.setContract_cost(newVO.getCost());
															newVO.setContract_amount(newVO.getAmount());
															detailBVOs.add(newVO);
														}
													}
												}
											}else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TICKET
													&& cvo.getValuation_type().equals(entOperationBVO.getValuation_type())) {
												// 计价方式是票
												price = cvo.getPrice1().doubleValue();
												amount = price;// =区间价格
												cost = cvo.getCost1().doubleValue();
												contract_cost = cost;
												bill_value = "1";
											} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NODE
													&& cvo.getValuation_type().equals(entOperationBVO.getValuation_type())) {
												// 计价方式是节点
												// 检测区间1是第几个节点，如果是2，那么说明第一个节点免费，假设此时有4个节点，那么总金额就是从价格1到价格3的总和
												// 配载界面的单价=null
												// 配载界面的金额=价格1
												int interval1 = cvo.getInterval1().intValue(); // 区间1
												for(int index = 1; index < node_count - interval1 + 2; index++) {
													try {
														amount += Double.parseDouble(ReflectionUtils.invokeMethod(cvo,
																"getPrice" + index).toString());
														contract_cost += Double.parseDouble(ReflectionUtils.invokeMethod(cvo,
																"getCost" + index).toString());
													} catch(Exception e) {
														e.printStackTrace();
													}
												}
												bill_value = "" + node_count;
											}else{
												price = getIntervalPrice(cvo, entOperationBVO.getOperation_value().doubleValue());
												amount = price;// =区间价格
												cost = getIntervalCost(cvo, entOperationBVO.getOperation_value().doubleValue());
												contract_cost = cost;
												bill_value = "" +  entOperationBVO.getOperation_value();
											}
										}
										double loest_fee = 0,loest_cost = 0;
										if(cvo.getLowest_fee() != null) {
											loest_fee = cvo.getLowest_fee().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
										}
										if(cvo.getLowest_cost() != null) {
											loest_cost = cvo.getLowest_cost().doubleValue();
										}
										detailBVO.setPrice(new UFDouble(price));
										detailBVO.setCost(new UFDouble(cost));
										detailBVO.setContract_cost(new UFDouble(loest_cost > contract_cost ? loest_cost : contract_cost));
										detailBVO.setAmount(new UFDouble(loest_fee > amount ? loest_fee : amount));
										detailBVO.setMemo(memo);
										detailBVO.setContract_amount(detailBVO.getAmount());
										detailBVO.setBill_value(bill_value);
										detailBVOs.add(detailBVO);//或有费用匹配不需要取大取小，因为需求不一样，直接保存就行。
									}
								}
							}
						}
					}
				}else{
					PayDetailBVO detailBVO = new PayDetailBVO(); // 创建费用明细VO
					detailBVO.setPk_expense_type(cvo.getPk_expense_type()); // 费用类型
					detailBVO.setQuote_type(cvo.getQuote_type());// 报价类型
					detailBVO.setPrice_type(cvo.getPrice_type());// 价格类型
					detailBVO.setValuation_type(cvo.getValuation_type());// 计价方式
					detailBVO.setSystem_create(UFBoolean.TRUE);
					detailBVO.setPk_contract_b(cvo.getPk_contract_b());

					// 将合同明细的税种，税率冗余到这里
					detailBVO.setTax_cat(cvo.getTax_cat());
					detailBVO.setTax_rate(cvo.getTax_rate());
					// 单价、金额，重头戏
					double price = 0, amount = 0,cost = 0, contract_cost = 0;
					String memo = "";
					String bill_value = "";
					if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.FIRST_WEIGHT) {
						// 首重报价
						if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
								&& cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE
								&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
							// 单价、重量
							double weight = fee_weight_count - cvo.getFirst_weight().doubleValue();// 总计费重-首重
							price = getIntervalPrice(cvo, weight);
							amount = cvo.getFirst_weight_price().doubleValue() + weight * price;// =首重价格+（总计费重-首重）×区间价格
							cost = getIntervalCost(cvo, weight);
							contract_cost = cvo.getFirst_weight_cost().doubleValue() + weight * cost;
							bill_value = "" + fee_weight_count;
						} else if(cvo.getPrice_type() != null && cvo.getValuation_type() != null
								&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE
								&& cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
							// 固定价格、重量
							double weight = fee_weight_count - cvo.getFirst_weight().doubleValue();// 总计费重-首重
							price = getIntervalPrice(cvo, weight);
							amount = cvo.getFirst_weight_price().doubleValue() + price;// =首重价格+区间价格
							cost = getIntervalCost(cvo, weight);
							contract_cost = cvo.getFirst_weight_cost().doubleValue() + cost;
							bill_value = "" + fee_weight_count;
						}
					} else if(cvo.getQuote_type() != null && cvo.getQuote_type().intValue() == QuoteTypeConst.INTERVAL) {
						// 区间报价
						if(cvo.getPrice_type() != null && cvo.getPrice_type().intValue() == PriceTypeConst.UNIT_PRICE) {
							if(cvo.getValuation_type() != null) {
								// 价格类型是单价
								// 2015-06-04 如果合同明细的费用类型是 提货段运费,则根据重量或者体积计算。
								if(cvo.getPk_expense_type().equals(expenseTypeCodeMap.get(ExpenseTypeConst.ET0010))) {// 提货段运费
									if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
										// 计价方式是重量
										price = getIntervalPrice(cvo, fee_weight_count);
										amount = fee_weight_count * price;// =总计费重×区间价格
										cost = getIntervalCost(cvo, fee_weight_count);
										contract_cost = fee_weight_count * cost;
										bill_value = "" + fee_weight_count;
									} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
										// 计价方式是体积
										price = getIntervalPrice(cvo, volume_count);
										amount = volume_count * price;// =总体积×区间价格
										cost = getIntervalCost(cvo, volume_count);
										contract_cost = volume_count * cost;
										bill_value = "" + volume_count;
									} 
								} else {
									if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
										// 计价方式是重量
										price = getIntervalPrice(cvo, fee_weight_count);
										amount = fee_weight_count * price;// =总计费重×区间价格
										cost = getIntervalCost(cvo, fee_weight_count);
										contract_cost = fee_weight_count * cost;
										bill_value = "" + fee_weight_count;
									} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
										// 计价方式是件数
										price = getIntervalPrice(cvo, num_count);
										amount = num_count * price;// =总件数×区间价格
										cost = getIntervalCost(cvo, num_count);
										contract_cost = num_count * cost;
										bill_value = "" + num_count;
									} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
										// 计价方式是体积
										price = getIntervalPrice(cvo, volume_count);
										amount = volume_count * price;// =总体积×区间价格
										cost = getIntervalCost(cvo, volume_count);
										contract_cost = volume_count * cost;
										bill_value = "" + volume_count;
									}else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDW) {
										// 计价方式是车型吨位
										List<CartypeTonnageVO> ctVOs = ctMap.get(cvo.getPk_expense_type());// 找到这个费用类型的车型吨位对照
										List<CXDWResultVO> resultVOs = getCartypeTonnageAmount(num_count, new UFDouble(
												weight_count), ctVOs, cvo);
										// 这里根据匹配到几个车型，就记录几条明细
										for(int i = 0; i < resultVOs.size(); i++) {
											CXDWResultVO resultVO = resultVOs.get(i);
											if(i == 0) {
												price = resultVO.getPrice().doubleValue();
												amount = resultVO.getAmount().doubleValue();
												cost = resultVO.getCost().doubleValue();
												contract_cost = resultVO.getContract_cost().doubleValue();
												bill_value = "" + resultVO.getCount();
												//增加车型吨位类型到备注上JONATHAN2015-10-29;
												if(resultVO.getCtVO()!=null){
												memo=String.valueOf(resultVO.getCtVO().getWeight().doubleValue());
												}
											} else {
												PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
												newVO.setPk_pay_detail(null);
												NWDao.setUuidPrimaryKey(newVO);
												newVO.setPrice(resultVO.getPrice());
												newVO.setAmount(resultVO.getAmount());
												newVO.setCost(resultVO.getCost());
												newVO.setBill_value(""+resultVO.getCount());
												newVO.setContract_cost(resultVO.getContract_cost());
												newVO.setContract_amount(newVO.getAmount());
												//增加车型吨位类型到备注上JONATHAN2015-10-29;
												if(resultVO.getCtVO()!=null){
												newVO.setMemo(String.valueOf(resultVO.getCtVO().getWeight().doubleValue()));
												}
												detailBVOs.add(newVO);
											}
										}
									} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.THD) {
										// 计价方式是提货点
										price = cvo.getPrice1().doubleValue();
										amount = (deli_node_count - 1) * price;
										cost = cvo.getCost1().doubleValue();
										contract_cost = (deli_node_count - 1) * cost;
										bill_value = "" + (deli_node_count - 1);
									}  else if(cvo.getValuation_type().intValue() == ValuationTypeConst.DHD) {
										// 计价方式是到货点
										price = cvo.getPrice1().doubleValue();
										amount = (deli_node_count - 1) * price;
										cost = cvo.getCost1().doubleValue();
										contract_cost = (deli_node_count - 1) * cost;
										bill_value = "" + (deli_node_count - 1);
									} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.CXDWLD) {
										// 车型吨位+重量算法
										List<CXDWLDResultVO> resultVOs = getCXDWAndLDAmount(new UFDouble(weight_count), cvo);
										// 这里根据匹配到几个车型，就记录几条明细
										for(int i = 0; i < resultVOs.size(); i++) {
											CXDWLDResultVO resultVO = resultVOs.get(i);
											if(i == 0) {
												if(resultVO.getCar_weight() != null
														&& resultVO.getCar_weight().doubleValue() > 0) {
													detailBVO.setValuation_type(ValuationTypeConst.CXDW);
													detailBVO.setBill_value(""+resultVO.getCount());
												} else {
													detailBVO.setValuation_type(ValuationTypeConst.WEIGHT);
													detailBVO.setBill_value(""+resultVO.getCar_weight());
												}
												price = resultVO.getPrice().doubleValue();
												amount = resultVO.getAmount().doubleValue();
												cost = resultVO.getCost().doubleValue();
												contract_cost = resultVO.getContract_cost().doubleValue();
												//增加车型吨位类型到备注上JONATHAN2015-10-29;
												memo=resultVO.getCar_weight().toString();
											} else {
												// 深度拷贝一份
												PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
												newVO.setPk_pay_detail(null);
												NWDao.setUuidPrimaryKey(newVO);
												if(resultVO.getCar_weight() != null
														&& resultVO.getCar_weight().doubleValue() > 0) {
													newVO.setValuation_type(ValuationTypeConst.CXDW);
													newVO.setBill_value(""+resultVO.getCount());
												} else {
													newVO.setValuation_type(ValuationTypeConst.WEIGHT);
													newVO.setBill_value(""+resultVO.getCar_weight());
												}
												newVO.setPrice(resultVO.getPrice());
												newVO.setAmount(resultVO.getAmount());
												newVO.setCost(resultVO.getCost());
												newVO.setContract_cost(resultVO.getContract_cost());
												newVO.setContract_amount(newVO.getAmount());
												//增加车型吨位类型到备注上JONATHAN2015-10-29;
												newVO.setMemo(resultVO.getCar_weight().toString());
												detailBVOs.add(newVO);
											}
										}
									}
								}
							}
						} else if(cvo.getPrice_type() != null
								&& cvo.getPrice_type().intValue() == PriceTypeConst.REGULAR_PRICE) {
							if(cvo.getValuation_type() != null) {
								// 价格类型是固定价格
								if(cvo.getValuation_type().intValue() == ValuationTypeConst.WEIGHT) {
									// 计价方式是重量
									price = getIntervalPrice(cvo, fee_weight_count);
									amount = price;// =区间价格
									cost = getIntervalCost(cvo, fee_weight_count);
									contract_cost = cost;
									bill_value = "" + fee_weight_count;
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NUM) {
									// 计价方式是件数
									price = getIntervalPrice(cvo, num_count);
									amount = price;// =区间价格
									cost = getIntervalCost(cvo, num_count);
									contract_cost = cost;
									bill_value = "" + num_count;
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.VOLUME) {
									// 计价方式是体积
									price = getIntervalPrice(cvo, volume_count);
									amount = price;// =区间价格
									cost = getIntervalCost(cvo, volume_count);
									contract_cost = cost;
									bill_value = "" + volume_count;
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.EQUIP) {
									// 计价方式是设备
									// 2015-06-09根据设备的个数，生成多少条费用明细
									boolean has = false;
									for(int i = 0; i < pk_car_type.length; i++) {
										if(pk_car_type[i].equals(cvo.getEquip_type())) {
											if(!has) {
												// 第一个费用
												price = cvo.getPrice1() == null ? 0 : cvo.getPrice1().doubleValue();
												amount = price;// =区间价格
												cost = cvo.getCost1() == null ? 0 : cvo.getCost1().doubleValue();
												contract_cost = cost;
												bill_value = cvo.getEquip_type();
												has = true;
											} else {
												// 生成第二个或者第三个费用明细
												PayDetailBVO newVO = (PayDetailBVO) detailBVO.clone();
												newVO.setPk_pay_detail(null);
												NWDao.setUuidPrimaryKey(newVO);
												newVO.setPrice(cvo.getPrice1());
												newVO.setAmount(newVO.getPrice());
												newVO.setCost(cvo.getCost1());
												newVO.setContract_cost(newVO.getContract_cost());
												newVO.setContract_amount(newVO.getAmount());
												newVO.setBill_value(cvo.getEquip_type());
												detailBVOs.add(newVO);
											}
										}
									}
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TON_KM) {
									// 计价方式是吨公里，暂时不处理
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.TICKET) {
									// 计价方式是票
									price = cvo.getPrice1().doubleValue();
									amount = price;// =区间价格
									cost = cvo.getCost1().doubleValue();
									contract_cost = cost;
									bill_value = "1";
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.NODE) {
									// 计价方式是节点
									// 检测区间1是第几个节点，如果是2，那么说明第一个节点免费，假设此时有4个节点，那么总金额就是从价格1到价格3的总和
									// 配载界面的单价=null
									// 配载界面的金额=价格1
									int interval1 = cvo.getInterval1().intValue(); // 区间1
									for(int index = 1; index < node_count - interval1 + 2; index++) {
										try {
											amount += Double.parseDouble(ReflectionUtils.invokeMethod(cvo,
													"getPrice" + index).toString());
											contract_cost += Double.parseDouble(ReflectionUtils.invokeMethod(cvo,
													"getCost" + index).toString());
										} catch(Exception e) {
											e.printStackTrace();
										}
									}
									bill_value = ""+node_count;
								} else if(cvo.getValuation_type().intValue() == ValuationTypeConst.PACK_NUM) {
									// 计价方式是数量
								} 
							}
						}
					}
					double loest_fee = 0,loest_cost = 0;
					if(cvo.getLowest_fee() != null) {
						loest_fee = cvo.getLowest_fee().doubleValue(); // 最低收费，计算后的金额都需要跟最低收费进行比较，取大者
					}
					if(cvo.getLowest_cost() != null) {
						loest_cost = cvo.getLowest_cost().doubleValue();
					}
					amount = loest_fee > amount ? loest_fee : amount;
					contract_cost = loest_cost > contract_cost ? loest_cost : contract_cost;
					detailBVO.setPrice(new UFDouble(price));
					detailBVO.setAmount(new UFDouble(amount));
					detailBVO.setCost(new UFDouble(cost));
					detailBVO.setContract_cost(new UFDouble(contract_cost));
					detailBVO.setMemo(memo);
					detailBVO.setContract_amount(detailBVO.getAmount());
					detailBVO.setBill_value(bill_value);
					// 2015-06-04 对于车型吨位和设备，这里不需要取大或者取小
					if(cvo.getValuation_type() == null
							|| (cvo.getValuation_type().intValue() == ValuationTypeConst.CXDW 
								&& cvo.getValuation_type().intValue() != ValuationTypeConst.EQUIP 
								&& StringUtils.isNotBlank(cvo.getPack()))
							) {
						// 看看费用类型是否已经存在，如果已经存在，那么看看金额哪个大
						PayDetailBVO dd = typeAmountMap.get(detailBVO.getPk_expense_type());
						if(dd != null) {
							// 费用类型已经存在，比较大小
							if(carrVO.getBilling_rule() != null
									&& carrVO.getBilling_rule().intValue() == BillingRuleConst.MIN) {
								// 取小
								if(dd.getAmount().doubleValue() > amount) {
									detailBVOs.remove(dd);
									detailBVOs.add(detailBVO);
									typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
								}
							} else {
								// 取大
								if(dd.getAmount().doubleValue() < amount) {
									detailBVOs.remove(dd);
									detailBVOs.add(detailBVO);
									typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
								}
							}
						} else {
							typeAmountMap.put(detailBVO.getPk_expense_type(), detailBVO);
							detailBVOs.add(detailBVO);
						}
					} else {
						detailBVOs.add(detailBVO);
					}
				}
			}
		}
		//处理包装费用
		List<PayDetailBVO> result = new ArrayList<PayDetailBVO>();
		if(detailBVOs != null && detailBVOs.size() > 0){
			//对费用明细按照费用类型进行分组，按照设备报价和包装报价会出现多行
			//按照设备报价，只会在合同里维护
			Map<String,List<PayDetailBVO>> groupMap = new HashMap<String,List<PayDetailBVO>>();
			for(PayDetailBVO detailBVO : detailBVOs){
				String key = detailBVO.getPk_expense_type();
				List<PayDetailBVO> voList = groupMap.get(key);
				if(voList == null){
					voList = new ArrayList<PayDetailBVO>();
					groupMap.put(key, voList);
				}
				voList.add(detailBVO);
			}
			for(String key : groupMap.keySet()){
				//相同费用类型的费用明细
				List<PayDetailBVO> voList = groupMap.get(key);
				if(voList != null){
					if(voList.size() > 1){
						//同一个费用类型会出现多行，说明匹配到多种费用（因为上面的过滤出来，这里只会是设备报价和包装报价会出现多行）
						//对明细按照相同报价类型进行分组
						Map<Integer,List<PayDetailBVO>> groupMap0 = new HashMap<Integer,List<PayDetailBVO>>();
						for(PayDetailBVO detailBVO : voList){
							Integer key0 = detailBVO.getValuation_type();
							List<PayDetailBVO> voList0 = groupMap0.get(key0);
							if(voList0 == null){
								voList0 = new ArrayList<PayDetailBVO>();
								groupMap0.put(key0, voList0);
							}
							voList0.add(detailBVO);
						}
						UFDouble amountTemp = UFDouble.ZERO_DBL;
						List<PayDetailBVO> voList1 = new ArrayList<PayDetailBVO>();
						for(Integer key0 : groupMap0.keySet()){
							List<PayDetailBVO> voList0 = groupMap0.get(key0);
							UFDouble amount = UFDouble.ZERO_DBL;
							if(voList0 != null && voList0.size() > 0){
								for(PayDetailBVO detailBVO : voList0){
									amount = amount.add(detailBVO.getAmount() == null ? UFDouble.ZERO_DBL : detailBVO.getAmount());
								}
							}
							if(amount.doubleValue() >= amountTemp.doubleValue()){
								voList1 = voList0;
								amountTemp = amount;
							}
						}
						result.addAll(voList1);
					}else if(voList.size() == 1){
						result.add(voList.get(0));
					}
				}
				
			}
		}
		return result;
	}

	/**
	 * 根据目标数值，看它处于哪个区间
	 * 
	 * @param cvo
	 * @param target
	 * @return
	 */
	private double getIntervalPrice(ContractBVO cvo, double target) {
		if(target >= 0 && target <= (cvo.getInterval1() == null ? 0 : cvo.getInterval1().doubleValue())) {
			return cvo.getPrice1() == null ? 0 : cvo.getPrice1().doubleValue();
		} else if(target > (cvo.getInterval1() == null ? 0 : cvo.getInterval1().doubleValue())
				&& target <= (cvo.getInterval2() == null ? 0 : cvo.getInterval2().doubleValue())) {
			return cvo.getPrice2() == null ? 0 : cvo.getPrice2().doubleValue();
		} else if(target > (cvo.getInterval2() == null ? 0 : cvo.getInterval2().doubleValue())
				&& target <= (cvo.getInterval3() == null ? 0 : cvo.getInterval3().doubleValue())) {
			return cvo.getPrice3() == null ? 0 : cvo.getPrice3().doubleValue();
		} else if(target > (cvo.getInterval3() == null ? 0 : cvo.getInterval3().doubleValue())
				&& target <= (cvo.getInterval4() == null ? 0 : cvo.getInterval4().doubleValue())) {
			return cvo.getPrice4() == null ? 0 : cvo.getPrice4().doubleValue();
		} else if(target > (cvo.getInterval4() == null ? 0 : cvo.getInterval4().doubleValue())
				&& target <= (cvo.getInterval5() == null ? 0 : cvo.getInterval5().doubleValue())) {
			return cvo.getPrice5() == null ? 0 : cvo.getPrice5().doubleValue();
		} else if(target > (cvo.getInterval5() == null ? 0 : cvo.getInterval5().doubleValue())
				&& target <= (cvo.getInterval6() == null ? 0 : cvo.getInterval6().doubleValue())) {
			return cvo.getPrice6() == null ? 0 : cvo.getPrice6().doubleValue();
		} else if(target > (cvo.getInterval6() == null ? 0 : cvo.getInterval6().doubleValue())
				&& target <= (cvo.getInterval7() == null ? 0 : cvo.getInterval7().doubleValue())) {
			return cvo.getPrice7() == null ? 0 : cvo.getPrice7().doubleValue();
		} else if(target > (cvo.getInterval7() == null ? 0 : cvo.getInterval7().doubleValue())
				&& target <= (cvo.getInterval8() == null ? 0 : cvo.getInterval8().doubleValue())) {
			return cvo.getPrice8() == null ? 0 : cvo.getPrice8().doubleValue();
		} else if(target > (cvo.getInterval8() == null ? 0 : cvo.getInterval8().doubleValue())
				&& target <= (cvo.getInterval8() == null ? 0 : cvo.getInterval8().doubleValue())) {
			return cvo.getPrice9() == null ? 0 : cvo.getPrice9().doubleValue();
		} else if(target > (cvo.getInterval9() == null ? 0 : cvo.getInterval9().doubleValue())
				&& target <= (cvo.getInterval10() == null ? 0 : cvo.getInterval10().doubleValue())) {
			return cvo.getPrice10() == null ? 0 : cvo.getPrice10().doubleValue();
		} else if(target > (cvo.getInterval10() == null ? 0 : cvo.getInterval10().doubleValue())
				&& target <= (cvo.getInterval11() == null ? 0 : cvo.getInterval11().doubleValue())) {
			return cvo.getPrice11() == null ? 0 : cvo.getPrice11().doubleValue();
		} else if(target > (cvo.getInterval11() == null ? 0 : cvo.getInterval11().doubleValue())
				&& target <= (cvo.getInterval12() == null ? 0 : cvo.getInterval12().doubleValue())) {
			return cvo.getPrice12() == null ? 0 : cvo.getPrice12().doubleValue();
		}
		return 0;
	}
	
	/**
	 * 根据目标数值，看它处于哪个区间
	 * 
	 * @param cvo
	 * @param target
	 * @return
	 */
	private double getIntervalCost(ContractBVO cvo, double target) {
		if(target >= 0 && target <= (cvo.getInterval1() == null ? 0 : cvo.getInterval1().doubleValue())) {
			return cvo.getCost1() == null ? 0 : cvo.getCost1().doubleValue();
		} else if(target > (cvo.getInterval1() == null ? 0 : cvo.getInterval1().doubleValue())
				&& target <= (cvo.getInterval2() == null ? 0 : cvo.getInterval2().doubleValue())) {
			return cvo.getCost2() == null ? 0 : cvo.getCost2().doubleValue();
		} else if(target > (cvo.getInterval2() == null ? 0 : cvo.getInterval2().doubleValue())
				&& target <= (cvo.getInterval3() == null ? 0 : cvo.getInterval3().doubleValue())) {
			return cvo.getCost3() == null ? 0 : cvo.getCost3().doubleValue();
		} else if(target > (cvo.getInterval3() == null ? 0 : cvo.getInterval3().doubleValue())
				&& target <= (cvo.getInterval4() == null ? 0 : cvo.getInterval4().doubleValue())) {
			return cvo.getCost4() == null ? 0 : cvo.getCost4().doubleValue();
		} else if(target > (cvo.getInterval4() == null ? 0 : cvo.getInterval4().doubleValue())
				&& target <= (cvo.getInterval5() == null ? 0 : cvo.getInterval5().doubleValue())) {
			return cvo.getCost5() == null ? 0 : cvo.getCost5().doubleValue();
		} else if(target > (cvo.getInterval5() == null ? 0 : cvo.getInterval5().doubleValue())
				&& target <= (cvo.getInterval6() == null ? 0 : cvo.getInterval6().doubleValue())) {
			return cvo.getCost6() == null ? 0 : cvo.getCost6().doubleValue();
		} else if(target > (cvo.getInterval6() == null ? 0 : cvo.getInterval6().doubleValue())
				&& target <= (cvo.getInterval7() == null ? 0 : cvo.getInterval7().doubleValue())) {
			return cvo.getCost7() == null ? 0 : cvo.getCost7().doubleValue();
		} else if(target > (cvo.getInterval7() == null ? 0 : cvo.getInterval7().doubleValue())
				&& target <= (cvo.getInterval8() == null ? 0 : cvo.getInterval8().doubleValue())) {
			return cvo.getCost8() == null ? 0 : cvo.getCost8().doubleValue();
		} else if(target > (cvo.getInterval8() == null ? 0 : cvo.getInterval8().doubleValue())
				&& target <= (cvo.getInterval8() == null ? 0 : cvo.getInterval8().doubleValue())) {
			return cvo.getCost9() == null ? 0 : cvo.getCost9().doubleValue();
		} else if(target > (cvo.getInterval9() == null ? 0 : cvo.getInterval9().doubleValue())
				&& target <= (cvo.getInterval10() == null ? 0 : cvo.getInterval10().doubleValue())) {
			return cvo.getCost10() == null ? 0 : cvo.getCost10().doubleValue();
		} else if(target > (cvo.getInterval10() == null ? 0 : cvo.getInterval10().doubleValue())
				&& target <= (cvo.getInterval11() == null ? 0 : cvo.getInterval11().doubleValue())) {
			return cvo.getCost11() == null ? 0 : cvo.getCost11().doubleValue();
		} else if(target > (cvo.getInterval11() == null ? 0 : cvo.getInterval11().doubleValue())
				&& target <= (cvo.getInterval12() == null ? 0 : cvo.getInterval12().doubleValue())) {
			return cvo.getCost12() == null ? 0 : cvo.getCost12().doubleValue();
		}
		return 0;
	}

	/**
	 * 车型吨位算法，计算金额,参考文档
	 * 
	 * @param num
	 *            件数、托
	 * @param weight
	 *            重量
	 * @param ctVOs
	 *            车型吨位对照 这里已经按照件数从大到小排序
	 * @param contractBVO
	 *            合同明细
	 * @return
	 */
	public List<CXDWResultVO> getCartypeTonnageAmount(Integer num, UFDouble weight, List<CartypeTonnageVO> ctVOs,
			ContractBVO contractBVO) {
		logger.info("使用车型吨位算法，件数：" + num + ",重量：" + weight);
		List<CXDWResultVO> list = new ArrayList<CXDWResultVO>();
		Map<CartypeTonnageVO, CXDWResultVO> cartypeMap = new HashMap<CartypeTonnageVO, CXDWResultVO>();
		if(ctVOs == null || ctVOs.size() == 0 || contractBVO == null || num == null || weight == null) {
			return list;
		}
		UFDouble carWeight = UFDouble.ZERO_DBL;// 车辆能装载的重量
		
		CartypeTonnageVO firstVO = ctVOs.get(0);
		if(num >= firstVO.getNum()) {
			int count = num / firstVO.getNum();
			num = num % firstVO.getNum();

			CXDWResultVO resultVO = cartypeMap.get(firstVO);
			if(resultVO == null) {
				resultVO = new CXDWResultVO();
				resultVO.setCtVO(firstVO);
				resultVO.setPrice(new UFDouble(getIntervalPrice(contractBVO, firstVO.getWeight().doubleValue())));
				resultVO.setCost(new UFDouble(getIntervalCost(contractBVO, firstVO.getWeight().doubleValue())));
				cartypeMap.put(firstVO, resultVO);
				list.add(resultVO);
			}
			resultVO.setCount(resultVO.getCount() + count);
			resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
			resultVO.setContract_cost(resultVO.getCost().multiply(resultVO.getCount()));
			carWeight = carWeight.add(firstVO.getWeight().multiply(resultVO.getCount()));
		}

		for(int i = 0; i < ctVOs.size() - 1; i++) {
			CartypeTonnageVO ctVO = ctVOs.get(i);
			CartypeTonnageVO nextVO = ctVOs.get(i + 1);
			if(num <= ctVO.getNum() && num > nextVO.getNum()) {
				CXDWResultVO resultVO = cartypeMap.get(ctVO);
				if(resultVO == null) {
					resultVO = new CXDWResultVO();
					resultVO.setCtVO(ctVO);
					resultVO.setPrice(new UFDouble(getIntervalPrice(contractBVO, ctVO.getWeight().doubleValue())));
					resultVO.setCost(new UFDouble(getIntervalCost(contractBVO, ctVO.getWeight().doubleValue())));
					cartypeMap.put(ctVO, resultVO);
					list.add(resultVO);
				}
				resultVO.setCount(resultVO.getCount() + 1);
				resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
				resultVO.setContract_cost(resultVO.getCost().multiply(resultVO.getCount()));
				carWeight = carWeight.add(ctVO.getWeight());
				num = num - ctVO.getNum();
			}
		}
		if(num > 0) {
			// 使用最小的车装
			CartypeTonnageVO lastVO = ctVOs.get(ctVOs.size() - 1);// 最后一辆车，也就是最小的车
			CXDWResultVO resultVO = cartypeMap.get(lastVO);
			if(resultVO == null) {
				resultVO = new CXDWResultVO();
				resultVO.setCtVO(lastVO);
				resultVO.setPrice(new UFDouble(getIntervalPrice(contractBVO, lastVO.getWeight().doubleValue())));
				resultVO.setCost(new UFDouble(getIntervalCost(contractBVO, lastVO.getWeight().doubleValue())));
				cartypeMap.put(lastVO, resultVO);
				list.add(resultVO);
			}
			resultVO.setCount(resultVO.getCount() + 1);
			resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
			resultVO.setContract_cost(resultVO.getCost().multiply(resultVO.getCount()));
			carWeight = carWeight.add(lastVO.getWeight());
		}

		if(weight.doubleValue() > carWeight.doubleValue()) {
			// 重量超过了车辆的承载，根据重量进行匹配
			list.clear();
			cartypeMap.clear();

			if(weight.doubleValue() >= firstVO.getWeight().doubleValue()) {
				int count = Integer.parseInt(weight.div(firstVO.getWeight()).setScale(0, UFDouble.ROUND_FLOOR) + "");
				weight = weight.mod(firstVO.getWeight());

				CXDWResultVO resultVO = cartypeMap.get(firstVO);
				if(resultVO == null) {
					resultVO = new CXDWResultVO();
					resultVO.setCtVO(firstVO);
					resultVO.setPrice(new UFDouble(getIntervalPrice(contractBVO, firstVO.getWeight().doubleValue())));
					resultVO.setCost(new UFDouble(getIntervalCost(contractBVO, firstVO.getWeight().doubleValue())));
					cartypeMap.put(firstVO, resultVO);
					list.add(resultVO);
				}
				resultVO.setCount(resultVO.getCount() + count);
				resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
				resultVO.setContract_cost(resultVO.getCost().multiply(resultVO.getCount()));
			}

			for(int i = 0; i < ctVOs.size() - 1; i++) {
				CartypeTonnageVO ctVO = ctVOs.get(i);
				CartypeTonnageVO nextVO = ctVOs.get(i + 1);
				if((weight.doubleValue() <= ctVO.getWeight().doubleValue() && weight.doubleValue() > nextVO.getWeight()
						.doubleValue())) {
					CXDWResultVO resultVO = cartypeMap.get(ctVO);
					if(resultVO == null) {
						resultVO = new CXDWResultVO();
						resultVO.setCtVO(ctVO);
						resultVO.setPrice(new UFDouble(getIntervalPrice(contractBVO, ctVO.getWeight().doubleValue())));
						resultVO.setCost(new UFDouble(getIntervalCost(contractBVO, ctVO.getWeight().doubleValue())));
						cartypeMap.put(ctVO, resultVO);
						list.add(resultVO);
					}
					resultVO.setCount(resultVO.getCount() + 1);
					resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
					resultVO.setContract_cost(resultVO.getCost().multiply(resultVO.getCount()));
					weight = weight.sub(ctVO.getWeight());
				}
			}
			if(weight.doubleValue() > 0) {
				// 使用最小的车装
				CartypeTonnageVO lastVO = ctVOs.get(ctVOs.size() - 1);// 最后一辆车，也就是最小的车
				CXDWResultVO resultVO = cartypeMap.get(lastVO);
				if(resultVO == null) {
					resultVO = new CXDWResultVO();
					resultVO.setCtVO(lastVO);
					resultVO.setPrice(new UFDouble(getIntervalPrice(contractBVO, lastVO.getWeight().doubleValue())));
					resultVO.setCost(new UFDouble(getIntervalCost(contractBVO, lastVO.getWeight().doubleValue())));
					cartypeMap.put(lastVO, resultVO);
					list.add(resultVO);
				}
				resultVO.setCount(resultVO.getCount() + 1);
				resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
				resultVO.setContract_cost(resultVO.getCost().multiply(resultVO.getCount()));
			}
		}
		return list;
	}

	/**
	 * <ul>
	 * <li>车型吨位+重量算法，参考文档</li>
	 * <li>0 <全部零担> 13330 <15t整车> 15000 <15t整车+零担> 24500 <30t整车> 30000
	 * <30t整车+零担></li>
	 * </ul>
	 * 
	 * 
	 * 
	 * @return
	 */
	public List<CXDWLDResultVO> getCXDWAndLDAmount(UFDouble weight, ContractBVO cvo) {
		logger.info("使用车型吨位+重量算法，重量：" + weight);
		List<CXDWLDResultVO> resultVOs = new ArrayList<CXDWLDResultVO>();
		List<UFDouble> quoteList = new ArrayList<UFDouble>();
		UFDouble price = cvo.getPrice1();
		UFDouble cost = cvo.getCost1();
		if(price == null || price.doubleValue() == 0) {
			throw new BusiException("车型吨位+重量计价方式，单价1不能为空，也不能为0！");
		}
		quoteList.add(cvo.getInterval1());
		if(cvo.getPrice2() != null && cvo.getPrice2().doubleValue() != 0) {
			quoteList.add(cvo.getPrice2().div(price));
			quoteList.add(cvo.getInterval2());
			if(cvo.getPrice3() != null && cvo.getPrice3().doubleValue() != 0) {
				quoteList.add(cvo.getPrice3().div(price));
				quoteList.add(cvo.getInterval3());
				if(cvo.getPrice4() != null && cvo.getPrice4().doubleValue() != 0) {
					quoteList.add(cvo.getPrice4().div(price));
					quoteList.add(cvo.getInterval4());
					if(cvo.getPrice5() != null && cvo.getPrice5().doubleValue() != 0) {
						quoteList.add(cvo.getPrice5().div(price));
						quoteList.add(cvo.getInterval5());
						if(cvo.getPrice6() != null && cvo.getPrice6().doubleValue() != 0) {
							quoteList.add(cvo.getPrice6().div(price));
							quoteList.add(cvo.getInterval6());
							if(cvo.getPrice7() != null && cvo.getPrice7().doubleValue() != 0) {
								quoteList.add(cvo.getPrice7().div(price));
								quoteList.add(cvo.getInterval7());
								if(cvo.getPrice8() != null && cvo.getPrice8().doubleValue() != 0) {
									quoteList.add(cvo.getPrice8().div(price));
									quoteList.add(cvo.getInterval8());
									if(cvo.getPrice9() != null && cvo.getPrice9().doubleValue() != 0) {
										quoteList.add(cvo.getPrice9().div(price));
										quoteList.add(cvo.getInterval9());
										if(cvo.getPrice10() != null && cvo.getPrice10().doubleValue() != 0) {
											quoteList.add(cvo.getPrice10().div(price));
											quoteList.add(cvo.getInterval10());
											if(cvo.getPrice11() != null && cvo.getPrice11().doubleValue() != 0) {
												quoteList.add(cvo.getPrice11().div(price));
												quoteList.add(cvo.getInterval11());
												if(cvo.getPrice12() != null && cvo.getPrice12().doubleValue() != 0) {
													quoteList.add(cvo.getPrice12().div(price));
													quoteList.add(cvo.getInterval12());
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		Map<UFDouble, CXDWLDResultVO> map = new HashMap<UFDouble, CXDWLDResultVO>();
		// 假设有5个数据，i的最大值是4，表示第3个区间数据
		UFDouble last = quoteList.get(quoteList.size() - 1);
		if(weight.doubleValue() >= last.doubleValue()) {
			int count = Integer.parseInt(weight.div(last).setScale(0, UFDouble.ROUND_FLOOR) + "");
			if(count > 0) {
				CXDWLDResultVO resultVO = new CXDWLDResultVO();
				String price_method = "getPrice" + (quoteList.size() - 1 - 1);
				String cost_method = "getCost" + (quoteList.size() - 1 - 1);
				UFDouble _price = UFDouble.ZERO_DBL;
				UFDouble _cost = UFDouble.ZERO_DBL;
				try {
					_price = (UFDouble) ReflectionUtils.invokeMethod(cvo, price_method);
					_cost = (UFDouble) ReflectionUtils.invokeMethod(cvo, cost_method);
				} catch(Exception e) {
					e.printStackTrace();
				}
				price_method = "getInterval" + (quoteList.size() - 1 - 1);
				cost_method = "getInterval" + (quoteList.size() - 1 - 1);
				UFDouble _interval = UFDouble.ZERO_DBL;
				try {
					_interval = (UFDouble) ReflectionUtils.invokeMethod(cvo, price_method);
				} catch(Exception e) {
					e.printStackTrace();
				}
				resultVO.setPrice(_price);
				resultVO.setCost(_cost);
				resultVO.setCount(count);
				resultVO.setContract_cost(_cost.multiply(count));
				resultVO.setAmount(_price.multiply(count));
				resultVO.setCar_weight(_interval);
				map.put(_interval, resultVO);
				resultVOs.add(resultVO);
			}
		}

		UFDouble tobe = weight.mod(last);
		int j = quoteList.size()-1;
		for(int i = quoteList.size() - 1; i > 0; i--) {
			if(tobe.doubleValue() <= 0) {
				break;
			}
			UFDouble quote = quoteList.get(i);
			UFDouble nextQuote = quoteList.get(i - 1);
			if(tobe.doubleValue() > nextQuote.doubleValue() && tobe.doubleValue() <= quote.doubleValue()) {
				if(i % 2 == 0) {
					// i是偶数,整车
					String price_method = "";
					String cost_method = "";
					//2015-10-26 jonathan 当i为最后一个数组序号，需要将i 减一获取价格和区间。
					if(i==j){
						price_method = "getPrice" + (i-1);
						cost_method = "getCost" + (i-1);
					}
					else{
						price_method = "getPrice" + (i);
						cost_method = "getCost" + (i);
					}
					UFDouble _price = UFDouble.ZERO_DBL;
					UFDouble _cost = UFDouble.ZERO_DBL;
					try {
						_price = (UFDouble) ReflectionUtils.invokeMethod(cvo, price_method);
						_cost = (UFDouble) ReflectionUtils.invokeMethod(cvo, cost_method);
					} catch(Exception e) {
						e.printStackTrace();
					}
					if(i==j){
						price_method = "getInterval" + (i-1);
						cost_method = "getInterval" + (i-1);
					}
					else{
						price_method = "getInterval" + (i);
						cost_method = "getInterval" + (i);
					}
					UFDouble _interval = UFDouble.ZERO_DBL;
					try {
						_interval = (UFDouble) ReflectionUtils.invokeMethod(cvo, price_method);
					} catch(Exception e) {
						e.printStackTrace();
					}
					CXDWLDResultVO resultVO = map.get(_interval);
					if(resultVO != null) {
						resultVO.setCount(resultVO.getCount() + 1);
						//resultVO.setAmount(resultVO.getPrice().multiply(resultVO.getCount()));
						resultVO.setAmount((resultVO.getPrice() == null ? UFDouble.ZERO_DBL : resultVO.getPrice()).multiply(resultVO.getCount()));
						resultVO.setContract_cost((resultVO.getCost() == null ? UFDouble.ZERO_DBL : resultVO.getCost()).multiply(resultVO.getCount()));
					} 
					else {
						resultVO = new CXDWLDResultVO();
						resultVO.setPrice(_price);
						resultVO.setCost(_cost);
						resultVO.setCount(1);
						resultVO.setAmount(_price);
						resultVO.setContract_cost(_cost);
						resultVO.setCar_weight(_interval);
						map.put(_interval, resultVO);
						resultVOs.add(resultVO);
					}
				} else {
					// 整车+零担，或者零担
					if(nextQuote.doubleValue() == 0) {
						// 零担
						CXDWLDResultVO resultVO = new CXDWLDResultVO();
						resultVO.setPrice(price);
						resultVO.setCost(cost);
						resultVO.setCount(1);
						resultVO.setContract_cost(cost.multiply(tobe));
						resultVO.setAmount(price.multiply(tobe));
						resultVOs.add(resultVO);
					} else {
						String price_method = "getPrice" + (i - 1);
						UFDouble _price = UFDouble.ZERO_DBL;
						String cost_method = "getCost" + (i - 1);
						UFDouble _cost = UFDouble.ZERO_DBL;
						try {
							_price = (UFDouble) ReflectionUtils.invokeMethod(cvo, price_method);
							_cost = (UFDouble) ReflectionUtils.invokeMethod(cvo, cost_method);
						} catch(Exception e) {
							e.printStackTrace();
						}
						price_method = "getInterval" + (i - 1);
						cost_method = "getInterval" + (i - 1);
						UFDouble _interval = UFDouble.ZERO_DBL;
						try {
							_interval = (UFDouble) ReflectionUtils.invokeMethod(cvo, price_method);
						} catch(Exception e) {
							e.printStackTrace();
						}
						// 整车+零担
						CXDWLDResultVO resultVO = new CXDWLDResultVO();
						resultVO.setPrice(_price);
						resultVO.setCost(_cost);
						resultVO.setCount(1);
						resultVO.setAmount(_price);
						resultVO.setContract_cost(_cost);
						resultVO.setCar_weight(_interval);
						resultVOs.add(resultVO);

						// 零担
						CXDWLDResultVO resultVO1 = new CXDWLDResultVO();
						resultVO1.setPrice(price);
						resultVO1.setCost(cost);
						resultVO1.setCount(1);
						resultVO1.setAmount(price.multiply(tobe.mod(nextQuote)));
						resultVO1.setContract_cost(cost.multiply(tobe.mod(nextQuote)));
						resultVOs.add(resultVO1);
					}
				}
			}
		}
		map.clear();
		return resultVOs;
	}

	public boolean addNumberRow(ParamVO paramVO) {
		return false;
	}


}
