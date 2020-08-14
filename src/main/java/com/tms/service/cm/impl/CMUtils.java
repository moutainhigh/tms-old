package com.tms.service.cm.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.exp.POI;
import org.nw.jf.group.GroupVO;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.constants.ContractConst;
import com.tms.constants.ExpenseTypeConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TaxCategoryConst;
import com.tms.service.cm.ContractService;
import com.tms.service.job.cm.PayDetailMatchVO;
import com.tms.vo.base.AddressVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ExpenseTypeVO;
import com.tms.vo.cm.PackInfo;
import com.tms.vo.cm.PayCheckSheetBVO;
import com.tms.vo.cm.PayCheckSheetVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceCheckSheetBVO;
import com.tms.vo.cm.ReceCheckSheetVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;
import com.tms.vo.te.EntPackBVO;
import com.tms.vo.tp.SegmentVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * 费用明细工具类
 * 
 * @author xuqc
 * @date 2013-4-14 下午01:47:28
 */
public class CMUtils {
	@Autowired
	private static ContractService contractService;

	/**
	 * 重新统一应收明细费用
	 * 
	 * @param parentVO
	 * @param childVOs
	 */
	public static void processExtenal(ReceiveDetailVO parentVO, ReceDetailBVO[] childVOs) {
		UFDouble trans_fee_price = UFDouble.ZERO_DBL;
		UFDouble trans_fee_count = UFDouble.ZERO_DBL;
		UFDouble other_fee_count = UFDouble.ZERO_DBL;
		UFDouble cost_amount = UFDouble.ZERO_DBL;
		if (childVOs != null && childVOs.length > 0) {
			for (ReceDetailBVO childVO : childVOs) {
				if (childVO.getStatus() == VOStatus.DELETED) {
					continue;
				}
				UFDouble amount = childVO.getAmount() == null ? UFDouble.ZERO_DBL : childVO.getAmount();
				cost_amount = cost_amount.add(amount);
				ExpenseTypeVO etVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "pk_expense_type=?",
						childVO.getPk_expense_type());
				if (etVO != null) {
					if (ExpenseTypeConst.ET10.equals(etVO.getCode())) {
						// 是运费
						trans_fee_price = childVO.getPrice();
						trans_fee_count = trans_fee_count.add(amount);
					} else {
						other_fee_count = other_fee_count.add(amount);
					}
				}
			}
			parentVO.setTrans_fee_price(trans_fee_price);
			parentVO.setTrans_fee_count(trans_fee_count);
			parentVO.setOther_fee_count(other_fee_count);

			// 重新设置应收明细
			parentVO.setCost_amount(cost_amount);
			// 未付金额
			UFDouble got_amount = parentVO.getGot_amount() == null ? UFDouble.ZERO_DBL : parentVO.getGot_amount();
			parentVO.setUngot_amount(parentVO.getCost_amount().sub(got_amount));// 未收金额等于总金额
		} else {

			parentVO.setCost_amount(UFDouble.ZERO_DBL);
			parentVO.setUngot_amount(UFDouble.ZERO_DBL);
			parentVO.setOther_fee_count(UFDouble.ZERO_DBL);

		}
		// FIXME 取第一行合同明细的税种，税率
		if (childVOs != null && childVOs.length > 0) {
			parentVO.setTax_cat(childVOs[0].getTax_cat());
			parentVO.setTax_rate(childVOs[0].getTax_rate());
			parentVO.setTaxmny(
					CMUtils.getTaxmny(parentVO.getCost_amount(), parentVO.getTax_cat(), parentVO.getTax_rate()));
		}
		if (parentVO.getStatus() != VOStatus.NEW) {
			parentVO.setStatus(VOStatus.UPDATED);
		}
		
	}
	public static void processExtenalforComputer(ReceiveDetailVO parentVO, List<ReceDetailBVO> childVOs){
		if(childVOs == null || childVOs.size() == 0){
			processExtenalforComputer(parentVO, new ReceDetailBVO[]{});
		}else{
			processExtenalforComputer(parentVO, childVOs.toArray(new ReceDetailBVO[childVOs.size()]));
		}
		
	}
	// 增加计算金额保存数据库功能，Jonathan 2015-10-26
	public static void processExtenalforComputer(ReceiveDetailVO parentVO, ReceDetailBVO[] childVOs) {
		UFDouble trans_fee_price = UFDouble.ZERO_DBL;
		UFDouble trans_fee_count = UFDouble.ZERO_DBL;
		UFDouble other_fee_count = UFDouble.ZERO_DBL;
		UFDouble cost_amount = UFDouble.ZERO_DBL;
		if (childVOs != null && childVOs.length > 0) {
			for (ReceDetailBVO childVO : childVOs) {
				if (childVO.getStatus() == VOStatus.DELETED) {
					continue;
				}
				UFDouble amount = childVO.getAmount() == null ? UFDouble.ZERO_DBL : childVO.getAmount();
				cost_amount = cost_amount.add(amount);
				ExpenseTypeVO etVO = NWDao.getInstance().queryByCondition(ExpenseTypeVO.class, "pk_expense_type=?",
						childVO.getPk_expense_type());
				if (etVO != null) {
					if (ExpenseTypeConst.ET10.equals(etVO.getCode())) {
						// 是运费
						trans_fee_price = childVO.getPrice();
						trans_fee_count = trans_fee_count.add(amount);
					} else {
						other_fee_count = other_fee_count.add(amount);
					}
				}
			}
			parentVO.setTrans_fee_price(trans_fee_price);
			parentVO.setTrans_fee_count(trans_fee_count);
			parentVO.setOther_fee_count(other_fee_count);

			// 重新设置应收明细
			parentVO.setCost_amount(cost_amount);
			// 未付金额
			UFDouble got_amount = parentVO.getGot_amount() == null ? UFDouble.ZERO_DBL : parentVO.getGot_amount();
			parentVO.setUngot_amount(parentVO.getCost_amount().sub(got_amount));// 未收金额等于总金额
		} else {

			parentVO.setCost_amount(UFDouble.ZERO_DBL);
			parentVO.setUngot_amount(UFDouble.ZERO_DBL);
			parentVO.setOther_fee_count(UFDouble.ZERO_DBL);

		}
		// FIXME 取第一行合同明细的税种，税率
		if (childVOs != null && childVOs.length > 0) {
			parentVO.setTax_cat(childVOs[0].getTax_cat());
			parentVO.setTax_rate(childVOs[0].getTax_rate());
			parentVO.setTaxmny(
					CMUtils.getTaxmny(parentVO.getCost_amount(), parentVO.getTax_cat(), parentVO.getTax_rate()));
		}
		if (parentVO != null && childVOs != null && childVOs.length > 0) {
			parentVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(parentVO);
		}

	}

	/**
	 * 1、重新计算总金额 2、重新计算税额,刷新费用，重新计算费用使用，与2015-10-29日添加 JONATHAN
	 */
	public static void processExtenalforComputer(PayDetailVO pdVO, List<PayDetailBVO> pdBVOs) {
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
		if(pdVO!=null){
			pdVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(pdVO);
		}
	
	}
	
	
	/**
	 * 1、重新计算总金额 2、重新计算税额
	 */
	public static void processExtenal(PayDetailVO pdVO, List<PayDetailBVO> pdBVOs) {
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
		// FIXME 取第一行合同明细的税种，税率
		if (pdBVOs != null && pdBVOs.size() > 0) {
			pdVO.setTax_cat(pdBVOs.get(0).getTax_cat());
			pdVO.setTax_rate(pdBVOs.get(0).getTax_rate());
			pdVO.setTaxmny(CMUtils.getTaxmny(pdVO.getCost_amount(), pdVO.getTax_cat(), pdVO.getTax_rate()));
		}
		if (pdVO.getStatus() != VOStatus.NEW) {
			pdVO.setStatus(VOStatus.UPDATED);
		}
	
	}
	
	
	
	

	/**
	 * 1、重新计算总金额 2、重新计算税额 直接获取数据库后台统计应付合计费用
	 */
	public  static void processExtenal(List<PayDetailVO> pdVOs) {
		// 批量更新 VO
		List<PayDetailVO> ReturnPayDetailVOs = new ArrayList<PayDetailVO>();
		if(pdVOs !=null && pdVOs.size()>0){
			for(PayDetailVO pdVO:pdVOs)
			{
				PayDetailBVO[] pdBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailBVO.class,"pk_pay_detail=?" ,
						pdVO.getPk_pay_detail());
				if(pdBVOs != null && pdBVOs.length > 0) {
					UFDouble cost_amount = UFDouble.ZERO_DBL;
					for(PayDetailBVO pdBVO : pdBVOs) {
						cost_amount=cost_amount.add(pdBVO.getAmount()== null ? UFDouble.ZERO_DBL : pdBVO.getAmount());
					}
					pdVO.setCost_amount(cost_amount);
					pdVO.setUngot_amount(cost_amount);
			
				} else {
					pdVO.setCost_amount(UFDouble.ZERO_DBL);
					pdVO.setUngot_amount(UFDouble.ZERO_DBL);
				}
				pdVO.setStatus(VOStatus.UPDATED);
				ReturnPayDetailVOs.add(pdVO);
			}
			 NWDao.getInstance().saveOrUpdate(ReturnPayDetailVOs.toArray(new PayDetailVO[ReturnPayDetailVOs.size()]));
		}
	   
	}

	/**
	 * 重新计算应收对账的金额 对账单明细实际上不存储金额，只是关联应收明细
	 * 
	 * @param parentVO
	 * @param rcsBVOs
	 */
	public static void processExtenal(ReceCheckSheetVO parentVO, ReceCheckSheetBVO[] rcsBVOs) {
		if (parentVO == null) {
			return;
		}
		if (rcsBVOs == null || rcsBVOs.length == 0) {
			parentVO.setCost_amount(UFDouble.ZERO_DBL);
			parentVO.setUngot_amount(UFDouble.ZERO_DBL);
		} else {
			List<String> pks = new ArrayList<String>();
			for (int i = 0; i < rcsBVOs.length; i++) {
				if (rcsBVOs[i].getStatus() != VOStatus.DELETED) {
					pks.add(rcsBVOs[i].getPk_receive_detail());
				}
			}
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			UFDouble ungot_amount = UFDouble.ZERO_DBL;
			if (pks.size() > 0) {
				String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
				ReceiveDetailVO[] rdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
						"pk_receive_detail in " + cond);
				if (rdVOs != null && rdVOs.length > 0) {
					for (ReceiveDetailVO rdVO : rdVOs) {
						cost_amount = cost_amount.add(rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount());
						ungot_amount = ungot_amount.add(rdVO.getUngot_amount() == null ? UFDouble.ZERO_DBL : rdVO.getUngot_amount());
					}
				}
			}
			parentVO.setCost_amount(cost_amount);
			parentVO.setUngot_amount(ungot_amount);
			parentVO.setGot_amount(cost_amount.sub(ungot_amount));
		}

	}

	/**
	 * 重新计算应付对账的金额 对账单明细实际上不存储金额，只是关联应付明细
	 * 
	 * @param parentVO
	 * @param pcsBVOs
	 */
	public static void processExtenal(PayCheckSheetVO parentVO, PayCheckSheetBVO[] pcsBVOs) {
		if (parentVO == null) {
			return;
		}
		if (pcsBVOs == null || pcsBVOs.length == 0) {
			parentVO.setCost_amount(UFDouble.ZERO_DBL);
			parentVO.setUngot_amount(UFDouble.ZERO_DBL);
		} else {
			List<String> pks = new ArrayList<String>();
			for (int i = 0; i < pcsBVOs.length; i++) {
				if (pcsBVOs[i].getStatus() != VOStatus.DELETED) {
					pks.add(pcsBVOs[i].getPk_pay_detail());
				}
			}
			UFDouble cost_amount = UFDouble.ZERO_DBL;
			UFDouble ungot_amount = UFDouble.ZERO_DBL;
			if (pks.size() > 0) {
				String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
				PayDetailVO[] pdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class,
						"pk_pay_detail in " + cond);
				if (pdVOs != null && pdVOs.length > 0) {
					for (PayDetailVO rdVO : pdVOs) {
						cost_amount = cost_amount.add(rdVO.getCost_amount() == null ? UFDouble.ZERO_DBL : rdVO.getCost_amount());
						ungot_amount = ungot_amount.add(rdVO.getUngot_amount() == null ? UFDouble.ZERO_DBL : rdVO.getUngot_amount());
					}
				}
			}
			parentVO.setCost_amount(cost_amount);
			parentVO.setUngot_amount(ungot_amount);
			parentVO.setGot_amount(cost_amount.sub(ungot_amount));
		}

	}

	// 更新发票和发票抬头
	public static String[] getUpdateCheck(String current_check_no, String new_check_no, String current_check_head,
			String new_check_head) {
		// 更新应收对账的发票号和发票抬头
		String[] checkArr = new String[2];
		StringBuffer sb = new StringBuffer("");
		if (StringUtils.isNotBlank(current_check_no)) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(current_check_no);
		}
		if (StringUtils.isNotBlank(new_check_no)) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(new_check_no);
		}
		checkArr[0] = sb.toString();

		sb.setLength(0);

		if (StringUtils.isNotBlank(current_check_head)) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(current_check_head);
		}
		if (StringUtils.isNotBlank(new_check_head)) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(new_check_head);
		}
		checkArr[1] = sb.toString();
		return checkArr;
	}

	/**
	 * 根据税种，税率计算税额 // 增值税-----税金=金额-金额/(1+税率) // 营业税-----税金=金额*税率
	 * 
	 * @param amount
	 * @param tax_cat
	 * @param tax_rate
	 * @return
	 */
	public static UFDouble getTaxmny(UFDouble amount, Integer tax_cat, UFDouble tax_rate) {
		UFDouble taxmny = null;
		if (tax_cat != null && tax_rate != null) {
			if (amount == null) {
				amount = new UFDouble(0);
			}
			if (tax_cat == TaxCategoryConst.ZZS) {
				taxmny = amount.sub(amount.div(tax_rate.add(1)));
			} else if (tax_cat == TaxCategoryConst.YYS) {
				taxmny = amount.multiply(tax_rate);
			}
		}
		// 增加设置系统默认的小数位数
		return taxmny == null ? null : taxmny.setScale(ParameterHelper.getPrecision(), UFDouble.ROUND_HALF_UP);
	}

	
	/**
	 * 导出集货应收明细 songf 2015-11-07
	 * 
	 * @param receDetaiPKs
	 *            跨页选中的主键值
	 * @param strOrderType
	 *            类型，是正常，还是返箱
	 * @return
	 */
	public static HSSFWorkbook exportReceiveDetailRecord(String[] receDetaiPKs, String strOrderType) {
		Calendar start = Calendar.getInstance();
		
		// 导出集货应收明细存储过程名
		final String export_data_tgsinfo = "SP_TGSINFO_FEE";

		// 通过应收明细的衣主键，获取指定主键的应收明细集合
		ReceiveDetailVO[] rdVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class,
				"pk_receive_detail in " + NWUtils.buildConditionString(receDetaiPKs));
		if (rdVOs == null || rdVOs.length == 0) {
			return null;
		}

		// 定义用于拼接存储过程参数的变量
		String strInvVbillno = "";
		String strOrderno = "";
		// 遍历获取发货单号列表
		List<String> invoiceVbillnoAry = new ArrayList<String>();
		for (ReceiveDetailVO rdVO : rdVOs) {
			strInvVbillno += rdVO.getInvoice_vbillno() + ",";
			invoiceVbillnoAry.add(rdVO.getInvoice_vbillno());
		}

		// 通过发货单号获取发货单实体类集合
		InvoiceVO[] invVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, "vbillno in "
				+ NWUtils.buildConditionString(invoiceVbillnoAry.toArray(new String[invoiceVbillnoAry.size()])));
		if (invVOs == null || invVOs.length == 0) {
			return null;
		}

		// 遍历发货单实体集合类，获取订单号列表
		List<String> invoiceOrdernoAry = new ArrayList<String>();
		for (InvoiceVO invVO : invVOs) {
			strOrderno += invVO.getOrderno() + ",";
			invoiceVbillnoAry.add(invVO.getOrderno());
		}

		// 得到存储过程参数
		strInvVbillno = strInvVbillno.substring(0, strInvVbillno.length() - 1);
		strOrderno = strOrderno.substring(0, strOrderno.length() - 1);
		final String strOrdernoParam = strOrderno;
		final String strInvVbillnoParam = strInvVbillno;
		final String strOrderTypeParam = strOrderType;

		// 用于存储查询数据
		final List<List<Object>> retList = new ArrayList<List<Object>>();

		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 3;
					String storedProc = DaoHelper.getProcedureCallName(export_data_tgsinfo, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, strOrdernoParam);
					cs.setString(2, strInvVbillnoParam);
					cs.setString(3, strOrderTypeParam);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {

					// 查询结果集
					int index = 0;
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						index++;
						fillResultSet(retList, rs, index);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		// 创建表头
		List<String> listTitle = new ArrayList<String>();
		List<List<GroupVO>> groupRowAry = new ArrayList<List<GroupVO>>();
		List<GroupVO> groupRow = new ArrayList<GroupVO>();
		makeExcelTitle(listTitle, groupRow);
		groupRowAry.add(groupRow);

		// 调用excel导出方法
		POI excel = new POI();
		HSSFWorkbook wb = excel.buildExcel(listTitle.toArray(new String[listTitle.size()]), retList, groupRowAry);
		return wb;
	}

	/**
	 * 从查询结果集中取出数据 songf 2015-11-07
	 */
	public static void fillResultSet(List<List<Object>> resutlLists, ResultSet rs, int index) throws SQLException {

		List<Object> resultObject = new ArrayList<Object>();
		resultObject.add(index);
		resultObject.add(rs.getString("req_deli_date"));
		resultObject.add(rs.getString("act_deli_date"));
		resultObject.add(rs.getString("th_no"));
		resultObject.add(rs.getString("raise_person"));
		resultObject.add(rs.getString("shipper"));
		resultObject.add(rs.getString("deli_person"));
		resultObject.add(rs.getString("goods_code"));
		resultObject.add(rs.getString("plan_pallet_num"));
		resultObject.add(rs.getString("act_pallet_num"));
		resultObject.add(rs.getString("act_component_num"));
		resultObject.add(rs.getString("pkg_info"));
		resultObject.add(rs.getString("plan_component_num"));
		resultObject.add(rs.getString("weight"));
		resultObject.add(rs.getString("volume"));
		resultObject.add(rs.getString("deli_good_check"));
		resultObject.add(rs.getString("deli_contact"));
		resultObject.add(rs.getString("deli_detail_addr"));
		resultObject.add(rs.getString("memo"));
		resultObject.add(rs.getString("begin_province"));
		resultObject.add(rs.getString("begin_addr"));
		resultObject.add(rs.getString("shd_arri_city"));
		resultObject.add(rs.getString("shd_arri_contact"));
		resultObject.add(rs.getString("trans_type"));
		resultObject.add(rs.getString("plan_arri_days"));
		resultObject.add(rs.getString("deli_car_type"));
		resultObject.add(rs.getString("deli_gps"));
		resultObject.add(rs.getString("carno"));
		resultObject.add(rs.getString("driver_name"));
		resultObject.add(rs.getString("gps"));
		resultObject.add(rs.getString("gxd_carno"));
		resultObject.add(rs.getString("gxd_track_memo"));
		resultObject.add(rs.getString("gxd_act_deli_date"));
		resultObject.add(rs.getString("seal_no"));
		resultObject.add(rs.getString("req_date"));
		resultObject.add(rs.getString("act_arri_date"));
		resultObject.add(rs.getString("islate"));
		resultObject.add(rs.getString("late_reason"));
		resultObject.add(rs.getString("shd_req_arri_date"));
		resultObject.add(rs.getString("shd_act_arri_date"));
		resultObject.add(rs.getString("shd_req_deli_date"));
		resultObject.add(rs.getString("shd_act_deli_date"));
		resultObject.add(rs.getString("shd_islate"));
		resultObject.add(rs.getString("shd_late_reason"));
		resultObject.add(rs.getString("D1"));
		resultObject.add(rs.getString("D2"));
		resultObject.add(rs.getString("D3"));
		resultObject.add(rs.getString("D4"));
		resultObject.add(rs.getString("D5"));
		resultObject.add(rs.getString("tracking_feedback"));
		resultObject.add(rs.getString("weight_count"));
		resultObject.add(rs.getString("volume_count"));
		resultObject.add(rs.getDouble("thd_freight"));
		resultObject.add(rs.getDouble("thd_fee"));
		resultObject.add(rs.getDouble("thd_otheramount"));
		resultObject.add(rs.getString("thd_other_memos"));
		resultObject.add(rs.getDouble("thd_totalamount"));

		resultObject.add(rs.getDouble("gxd_ld"));
		resultObject.add(rs.getDouble("gxd_zc"));
		resultObject.add(rs.getDouble("gxd_otheramount"));
		resultObject.add(rs.getString("gxd_other_memos"));
		resultObject.add(rs.getDouble("gxd_totalamount"));

		resultObject.add(rs.getDouble("shd_fee"));
		resultObject.add(rs.getDouble("shd_otheramount"));
		resultObject.add(rs.getString("shd_other_memos"));
		resultObject.add(rs.getDouble("shd_totalamount"));

		resultObject.add(rs.getDouble("totalamount"));

		// 提货段1
		resultObject.add(rs.getString("thdyf01_carr_code"));
		resultObject.add(rs.getDouble("thdyf01_thf"));
		resultObject.add(rs.getDouble("thdyf01_thdyf"));
		resultObject.add(rs.getDouble("thdyf01_shf"));
		resultObject.add(rs.getDouble("thdyf01_other_fee"));
		resultObject.add(rs.getString("thdyf01_devi_memos"));
		resultObject.add(rs.getDouble("thdyf01_zfy"));
		resultObject.add(rs.getString("thdyf01_pdh"));

		// 提货段2
		resultObject.add(rs.getString("thdyf02_carr_code"));
		resultObject.add(rs.getDouble("thdyf02_thf"));
		resultObject.add(rs.getDouble("thdyf02_thdyf"));
		resultObject.add(rs.getDouble("thdyf02_shf"));
		resultObject.add(rs.getDouble("thdyf02_other_fee"));
		resultObject.add(rs.getString("thdyf02_devi_memos"));
		resultObject.add(rs.getDouble("thdyf02_zfy"));
		resultObject.add(rs.getString("thdyf02_pdh"));

		// 干线段费用分摊
		resultObject.add(rs.getString("gxdyf_carr_code"));
		resultObject.add(rs.getDouble("gxdyf_zc"));
		resultObject.add(rs.getDouble("gxdyf_ld"));
		resultObject.add(rs.getDouble("gxdyf_other_fee"));
		resultObject.add(rs.getString("gxdyf_devi_memos"));
		resultObject.add(rs.getDouble("gxdyf_zfy"));
		resultObject.add(rs.getString("gxdyf_pdh"));

		// 送货段费用分摊
		resultObject.add(rs.getString("shdyf_carr_code"));
		resultObject.add(rs.getDouble("shdyf_shf"));
		resultObject.add(rs.getDouble("shdyf_other_fee"));
		resultObject.add(rs.getString("shdyf_devi_memos"));
		resultObject.add(rs.getDouble("shdyf_zfy"));
		resultObject.add(rs.getString("shdyf_pdh"));

		resultObject.add(rs.getDouble("total_cost"));
		resultObject.add(rs.getDouble("gross"));
		resultObject.add(rs.getString("gross_mar"));

		resutlLists.add(resultObject);
	}

	/**
	 * 生成excel表头--这里是写死的 songf 2015-11-07
	 */
	public static void makeExcelTitle(List<String> listTitle, List<GroupVO> groupRow) {

		// 第一行表头
		GroupVO groupBaseVO = new GroupVO();
		groupBaseVO.setHeader("基本信息");
		groupBaseVO.setColspan(52);
		groupBaseVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupBaseVO);

		// 提货段
		GroupVO groupThdVO = new GroupVO();
		groupThdVO.setHeader("提货段");
		groupThdVO.setColspan(5);
		groupThdVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupThdVO);

		// 干线段
		GroupVO groupGxdVO = new GroupVO();
		groupGxdVO.setHeader("干线");
		groupGxdVO.setColspan(5);
		groupGxdVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupGxdVO);

		// 送货段
		GroupVO groupShdVO = new GroupVO();
		groupShdVO.setHeader("送货段");
		groupShdVO.setColspan(4);
		groupShdVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupShdVO);

		// 总应收费用
		GroupVO groupZysVO = new GroupVO();
		groupZysVO.setHeader("总应收费用");
		groupZysVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupZysVO);

		// 提货段1
		GroupVO groupThd1VO = new GroupVO();
		groupThd1VO.setHeader("提货段1");
		groupThd1VO.setColspan(8);
		groupThd1VO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupThd1VO);

		// 提货段2
		GroupVO groupThd2VO = new GroupVO();
		groupThd2VO.setHeader("提货段2");
		groupThd2VO.setColspan(8);
		groupThd2VO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupThd2VO);

		// 干线段分摊
		GroupVO groupGxd1VO = new GroupVO();
		groupGxd1VO.setHeader("干线");
		groupGxd1VO.setColspan(7);
		groupGxd1VO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupGxd1VO);

		// 送货段分摊
		GroupVO groupShd1VO = new GroupVO();
		groupShd1VO.setHeader("送货段");
		groupShd1VO.setColspan(6);
		groupShd1VO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupShd1VO);

		// 总成本
		GroupVO groupTotalVO = new GroupVO();
		groupTotalVO.setHeader("总成本");
		groupTotalVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupTotalVO);

		// 毛利润
		GroupVO groupGrossVO = new GroupVO();
		groupGrossVO.setHeader("毛利润");
		groupGrossVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupGrossVO);

		// 利润率
		GroupVO groupGrossmarVO = new GroupVO();
		groupGrossmarVO.setHeader("利润率");
		groupGrossmarVO.setAlign(GroupVO.ALIGN[2]);
		groupRow.add(groupGrossmarVO);

		// 第二行表头
		listTitle.add("序号");
		listTitle.add("预计提货日期");
		listTitle.add("实际提货时间");
		listTitle.add("送货单号");
		listTitle.add("筹措员");
		listTitle.add("发货人");
		listTitle.add("发货人");
		listTitle.add("零件号");
		listTitle.add("计划托盘件数");
		listTitle.add("实际托盘数");
		listTitle.add("实际零件数");
		listTitle.add("包装情况");
		listTitle.add("计划零件数");
		listTitle.add("重量");
		listTitle.add("体积");
		listTitle.add("提货检查表");
		listTitle.add("联系人");
		listTitle.add("提货地址");
		listTitle.add("备注");
		listTitle.add("始发省");
		listTitle.add("始发地");
		listTitle.add("目的城市");
		listTitle.add("收货人");
		listTitle.add("运输方式");
		listTitle.add("运输时间(天)");
		listTitle.add("提货车型");
		listTitle.add("提货GPS号");
		listTitle.add("提货车号");
		listTitle.add("司机信息");
		listTitle.add("干线GPS号");
		listTitle.add("干线车号");

		listTitle.add("干线信息");
		listTitle.add("上海发车时间");
		listTitle.add("封号");
		listTitle.add("要求提货时间");
		listTitle.add("实际提货时间");
		listTitle.add("是否延误");
		listTitle.add("延误原因");
		listTitle.add("合同到货日期");
		listTitle.add("送货日期");
		listTitle.add("要求到货时间");
		listTitle.add("实际到货时间");
		listTitle.add("是否延误");
		listTitle.add("延误原因");
		listTitle.add("D1");
		listTitle.add("D2 12:00");
		listTitle.add("D3 12:00");
		listTitle.add("D4 12:00");
		listTitle.add("D5 12:00");
		listTitle.add("在途信息反馈");
		listTitle.add("总重量");
		listTitle.add("总体积");

		listTitle.add("提货段运费");
		listTitle.add("提货段提货费");
		listTitle.add("其他应收款");
		listTitle.add("其他应收款说明");
		listTitle.add("总费用");

		listTitle.add("费用");
		listTitle.add("整车");
		listTitle.add("其他应收款");
		listTitle.add("其他应收款说明");
		listTitle.add("总费用");

		listTitle.add("送货费");
		listTitle.add("其他应收款");
		listTitle.add("其他应收款说明");
		listTitle.add("总费用");

		listTitle.add("总应收费用");

		// 提货段1
		listTitle.add("承运商");
		listTitle.add("提货费");
		listTitle.add("提货段运费");
		listTitle.add("送货费");
		listTitle.add("其他应付款");
		listTitle.add("其他应付款说明");
		listTitle.add("总费用");
		listTitle.add("拼单号");

		// 提货段2
		listTitle.add("承运商");
		listTitle.add("提货费");
		listTitle.add("提货段运费");
		listTitle.add("送货费");
		listTitle.add("其他应付款");
		listTitle.add("其他应付款说明");
		listTitle.add("总费用");
		listTitle.add("拼单号");

		// 干线段
		listTitle.add("承运商");
		listTitle.add("费用");
		listTitle.add("整车");
		listTitle.add("其他应付款");
		listTitle.add("其他应付款说明");
		listTitle.add("总费用");
		listTitle.add("拼单号");

		// 送货段
		listTitle.add("承运商");
		listTitle.add("送货费");
		listTitle.add("其他应付款");
		listTitle.add("其他应付款说明");
		listTitle.add("总费用");
		listTitle.add("拼单号");

		// 送货段
		listTitle.add("总成本");
		listTitle.add("毛利润");
		listTitle.add("利润率");
	}
	
	
	/*
	 * 计算应收明细里面的金额 毛利和毛利率，需要在每一个涉及到应付明细的
	 * 操作执行之后执行一次
	 * */
	//yaojiie 2015 12 15 增加费用合计计算
	public static void  totalCostComput(List<InvoiceVO> invoiceVOs){
		List<String> pk_invoices = new ArrayList<String>();
		if(invoiceVOs == null || invoiceVOs.size() ==0){
			return;
		}
		for(InvoiceVO invoiceVO : invoiceVOs){
			pk_invoices.add(invoiceVO.getPk_invoice());
		}
		String pk_invoiceCond = NWUtils.buildConditionString(pk_invoices.toArray(new String[pk_invoices.size()]));
		//根据InvoiceVO获得单据号，查询应收明细 应收明细和发货单一一对应
		List<String> invoiceVbillno = new ArrayList<String>();
		for(InvoiceVO invoiceVO : invoiceVOs){
			invoiceVbillno.add(invoiceVO.getVbillno());
		}
		String vbillnoCond = NWUtils.buildConditionString(invoiceVbillno.toArray(new String[invoiceVbillno.size()]));
		//根据发货单PK查询所有的分摊明细
		PayDeviBVO[] payDeviBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDeviBVO.class, "pk_invoice in " + pk_invoiceCond);
		ReceiveDetailVO[] receiveDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class, "invoice_vbillno in " + vbillnoCond);
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		
		for(ReceiveDetailVO receiveDetailVO : receiveDetailVOs){
			for(InvoiceVO invoiceVO : invoiceVOs){
				if(receiveDetailVO.getInvoice_vbillno().equals(invoiceVO.getVbillno())){
					//对于一个发乎单，会有多个分摊明细与之对于
					 UFDouble man_devi_amount = UFDouble.ZERO_DBL;
					for(PayDeviBVO payDeviBVO : payDeviBVOs){
						if(invoiceVO.getPk_invoice().equals(payDeviBVO.getPk_invoice())){
							//合计手工分摊金额
							man_devi_amount = man_devi_amount.add(payDeviBVO.getMan_devi_amount() == null ? UFDouble.ZERO_DBL : payDeviBVO.getMan_devi_amount());
						}
					}
					//总金额(成本)
					UFDouble total_cost = man_devi_amount;
					receiveDetailVO.setTotal_cost(total_cost);
					//毛利
					UFDouble cost_amount = receiveDetailVO.getCost_amount() == null ? UFDouble.ZERO_DBL : receiveDetailVO.getCost_amount();
					UFDouble maori = cost_amount.sub(total_cost);
					receiveDetailVO.setMaori(maori);
					//毛利率
					if(receiveDetailVO.getCost_amount() == null || receiveDetailVO.getCost_amount().equals(UFDouble.ZERO_DBL) ){
						UFDouble maori_fee = UFDouble.ZERO_DBL.setScale(2, UFDouble.ROUND_HALF_UP);
						receiveDetailVO.setMaori_fee(maori_fee.toString() + "%");
					}else{
						UFDouble maori_fee = maori.div(receiveDetailVO.getCost_amount());
						maori_fee = maori_fee.multiply(100).setScale(2, UFDouble.ROUND_HALF_UP);
						receiveDetailVO.setMaori_fee(maori_fee.toString() + "%");
					}
					receiveDetailVO.setStatus(VOStatus.UPDATED);
					VOs.add(receiveDetailVO);
					break;
				}
			}
		}
		//将已经有了 金额 毛利 毛利率的应收明细，存入数据库
		NWDao.getInstance().saveOrUpdate(VOs);
	}
	
	public static  List<SuperVO> computeReceiveDetail(InvoiceVO invoiceVO, InvPackBVO[] invPackBVO, TransBilityBVO[] tbBVOs){
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		ReceiveDetailVO receiveDetailVO = new ReceiveDetailVO();
		receiveDetailVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(receiveDetailVO);
		receiveDetailVO.setInvoice_vbillno(invoiceVO.getVbillno());
		VOs.add(receiveDetailVO);
		List<ReceDetailBVO> receDetailBVOs = new ArrayList<ReceDetailBVO>();
		//匹配合同
		List<ContractBVO> contractBVOs = contractService.matchContract(ContractConst.CUSTOMER, invoiceVO.getBala_customer(),
				invoiceVO.getPk_trans_type(), invoiceVO.getPk_delivery(), invoiceVO.getPk_arrival(), invoiceVO.getDeli_city(),
				invoiceVO.getArri_city(), invoiceVO.getPk_corp(), invoiceVO.getReq_arri_date(),invoiceVO.getUrgent_level(),
				invoiceVO.getItem_code(),invoiceVO.getPk_trans_line(),invoiceVO.getIf_return());
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
			Map<String,List<InvPackBVO>> groupMap = new  HashMap<String,List<InvPackBVO>>();
			//对包装按照pack进行分组
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
		return VOs;
	}
	
	// 2015 11 14 yaojiie增加检查导入和保存的信息是否有重复
	public static List<String> splicString(ContractBVO[] contractBVOs) {
		// 定义以下字符串，用来接收需要保存的合同明细的数据
		String start_addr_type = new String();// 始发区域类型
		String start_addr = new String();// 始发区域
		String end_addr_type = new String();// 目的区域类型
		String end_addr = new String();// 目的区域
		String pk_expense_type = new String();// 费用类型
		String quote_type = new String();// 报价类型
		String price_type = new String();// 价格类型
		String valuation_type = new String();// 计价方式
		// yaojiie 2015 11 18 添加设备类型判断
		String equip_type = new String();// 设备类型
		String custom_proc = new String();// 存储过程
		String if_return = new String();// 是否回城

		// yaojiie 2016 1 11 增加匹配字段
		String urgent_level = new String();// 紧急程度
		String item_code = new String();// 项目编码
		String pk_trans_line = new String();// 线路
		
		String pack = new String();// 包装

		String comparisonString = new String();// 比较字符串，用了比较合同明细是否相同

		List<String> comparisonStrings = new ArrayList<String>();

		// 拿到的字段值可能为空，应该先判断是否为空
		for (ContractBVO contractBVO : contractBVOs) {
			// 对于拿到的字段为空的，且为String类型的，不需要使用toString方法，不用判断是否为空
			start_addr_type = contractBVO.getStart_addr_type().toString();
			start_addr = contractBVO.getStart_addr();
			end_addr_type = contractBVO.getEnd_addr_type().toString();
			end_addr = contractBVO.getEnd_addr();
			pk_expense_type = contractBVO.getPk_expense_type();
			quote_type = contractBVO.getQuote_type().toString();
			price_type = contractBVO.getPrice_type().toString();
			valuation_type = contractBVO.getValuation_type().toString();
			equip_type = contractBVO.getEquip_type();
			custom_proc = contractBVO.getCustom_proc();
			item_code = contractBVO.getItem_code();
			pk_trans_line = contractBVO.getPk_trans_line();
			pack = contractBVO.getPack();
			if (contractBVO.getUrgent_level() != null) {
				urgent_level = contractBVO.getUrgent_level().toString();
			} else {
				urgent_level = "0";
			}

			// getIf_return 返回Boolean类型
			if (contractBVO.getIf_return() != null) {
				if_return = contractBVO.getIf_return().toString();
			} else {
				if_return = "N";
			}

			comparisonString = start_addr_type + start_addr + end_addr_type + end_addr + pk_expense_type + quote_type
					+ price_type + valuation_type + equip_type + custom_proc + if_return + urgent_level + item_code
					+ pk_trans_line + pack;
			comparisonStrings.add(comparisonString);

		}
		return comparisonStrings;
	}

		public static void  checkRepeat(List<String> comparisonStrings){
					//查找comparisonStrings中的重复数据
			        //复制一个list
			        List<String> copylist = comparisonStrings;
			        String mesg =new String();
		            List<String> errormessages = new ArrayList<String>();
		            boolean b = false;
			        for (int i = 0; i < comparisonStrings.size()-1; i++) {
			            for (int j = i+1; j < copylist.size(); j++) {
			            	if(comparisonStrings.get(i).equals(copylist.get(j))){
			            		b = true;
			            		mesg = "第" + (i + 1) + "条合同明细和第" + (j + 1) + "条合同明细重复";
			            		errormessages.add(mesg);
			            	}
			            }
			        }
			        if (b) {
			            throw new BusiException(errormessages.toString());
			        }
		}

	
	
}
