package com.tms.service.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.utils.NWUtils;
import org.nw.utils.ReportUtils;
import org.nw.vo.LinkButtonVO;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ReportTempletBVO;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsDynReportServiceImpl;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvoiceVO;

/**
 * 每日财务报表
 * 
 * @author xuqc
 * @date 2015-1-12 下午02:55:12
 */
@Service
public class DailyFinancialReportServiceImpl extends TMSAbsDynReportServiceImpl {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InvoiceVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InvoiceVO.PK_INVOICE);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public UiReportTempletVO getReportTempletVO(String templateID) {
		UiReportTempletVO templetVO = super.getReportTempletVO(templateID);
		List<ReportTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(ReportTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getItemkey().equals("vbillno")) {
				// 发货单号
				fieldVO.setRenderer("invoice_vbillnoRenderer");
				break;
			}
		}
		return templetVO;
	}

	public boolean addCorrelateQuery(UiReportTempletVO templetVO, ParamVO paramVO) {
		return true;
	}

	public LinkButtonVO[] getCorrelateButtonVOs(ParamVO paramVO) {
		LinkButtonVO btnVO = new LinkButtonVO();
		btnVO.setText("货品明细");
		btnVO.setFun_code("t80414");
		// 如果不定义fun_code，可以直接定义url
		// btnVO.setUrl("/report/invPack/index.html?funCode=t80414");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("vbillno", "vbillno");
		btnVO.setParamMap(paramMap);
		return new LinkButtonVO[] { btnVO };
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processReportAfterExecFormula(ReportVO reportVO, ParamVO paramVO) {
		super.processReportAfterExecFormula(reportVO, paramVO);
		List<Map<String, Object>> list = reportVO.getPageVO().getItems();
		if(list == null || list.size() == 0) {
			return;
		}
		UiReportTempletVO templetVO = reportVO.getTempletVO();

		List<String> vbillnoAry = new ArrayList<String>();
		List<String> pkInvoiceAry = new ArrayList<String>();
		Map<String, Map<String, Object>> valueMap = new HashMap<String, Map<String, Object>>();
		for(Map<String, Object> rowMap : list) {
			String vbillno = String.valueOf(rowMap.get("vbillno"));
			vbillnoAry.add(vbillno);
			valueMap.put(vbillno, rowMap);
			pkInvoiceAry.add(String.valueOf(rowMap.get("pk_invoice")));
		}
		// 联查费用明细,包括“发货单号，费用类型，金额”
		// XXX 对于vbillno和pk_expense_type相同的字段，SQL返回的结果会合并金额
		String condStr = NWUtils.buildConditionString(vbillnoAry.toArray(new String[vbillnoAry.size()]));
		String sql = "select a.invoice_vbillno as vbillno,sum(b.amount) as amount,c.name as expense_type_name "
				+ "from ts_receive_detail a,ts_rece_detail_b b left join ts_expense_type c "
				+ "on b.pk_expense_type=c.pk_expense_type "
				+ "where a.pk_receive_detail=b.pk_receive_detail and isnull(a.dr,0)=0 and isnull(b.dr,0)=0 and a.invoice_vbillno in "
				+ condStr + " group by invoice_vbillno,name";
		logger.info("查询应收明细SQL:" + sql);
		List<HashMap> receDetailList = NWDao.getInstance().queryForList(sql, HashMap.class);
		// 应收费用合计
		logger.info("计算应收明细的合计费用！");
		Map<String, UFDouble> rece_total_amount_map = buildDynCol(receDetailList, templetVO, valueMap, "a", "应收明细");

		// 联查委托单的分摊费用
		condStr = NWUtils.buildConditionString(pkInvoiceAry.toArray(new String[pkInvoiceAry.size()]));
		sql = "SELECT d.name as expense_type_name, a.man_devi_amount as amount, b.vbillno, b.pk_delivery AS inv_delivery, b.pk_arrival  AS inv_arrival, "
				+ "c.pk_delivery as ent_delivery,c.pk_arrival as ent_arrival "
				+ "FROM ts_pay_devi_b a , ts_invoice b, ts_entrust c ,ts_expense_type d "
				+ " WHERE a.pk_invoice in "
				+ condStr
				+ " AND isnull(a.dr,0)=0 and isnull(b.dr,0)=0 and isnull(c.dr,0)=0 "
				+ "and a.pk_invoice=b.pk_invoice and a.pk_entrust=c.pk_entrust and a.pk_expense_type=d.pk_expense_type";
		logger.info("联查分摊费用SQL：" + sql);
		List<HashMap> payDetailList = NWDao.getInstance().queryForList(sql, HashMap.class);
		List<HashMap> deliPayDetailList = new ArrayList<HashMap>();// 提货段
		List<HashMap> otherPayDetailList = new ArrayList<HashMap>();// 干线段
		List<HashMap> arriPayDetailList = new ArrayList<HashMap>();// 到货段
		if(payDetailList != null && payDetailList.size() > 0) {
			for(int i = 0; i < payDetailList.size(); i++) {
				HashMap payRowMap = payDetailList.get(i);
				String inv_delivery = String.valueOf(payRowMap.get("inv_delivery"));
				String inv_arrival = String.valueOf(payRowMap.get("inv_arrival"));
				String ent_delivery = String.valueOf(payRowMap.get("ent_delivery"));
				String ent_arrival = String.valueOf(payRowMap.get("ent_arrival"));
				if(inv_delivery.equals(ent_delivery)) {
					// 提货段
					deliPayDetailList.add(payRowMap);
				} else if(inv_arrival.equals(ent_arrival)) {
					// 到货段
					arriPayDetailList.add(payRowMap);
				} else {
					// 干线段
					otherPayDetailList.add(payRowMap);
				}
			}
		}
		// 如果发货单号和费用类型相同，那么合并
		logger.info("对发货单号和费用类型相同的记录进行合并金额!");
		deliPayDetailList = mergePayRowMap(deliPayDetailList);
		otherPayDetailList = mergePayRowMap(otherPayDetailList);
		arriPayDetailList = mergePayRowMap(arriPayDetailList);

		logger.info("计算提货段费用的合计！");
		Map<String, UFDouble> deli_amount_map = buildDynCol(deliPayDetailList, templetVO, valueMap, "b", "提货段费用");
		//
		logger.info("计算干线段费用的合计！");
		Map<String, UFDouble> other_amount_map = buildDynCol(otherPayDetailList, templetVO, valueMap, "c", "干线段费用");
		logger.info("计算到货段费用的合计！");
		Map<String, UFDouble> arri_amount_map = buildDynCol(arriPayDetailList, templetVO, valueMap, "d", "到货段费用");

		logger.info("计算营业收入！");
		ReportTempletBVO fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] { "e", "营业收入",
				UiConstants.DATATYPE.DECIMAL.intValue() });
		templetVO.getFieldVOs().add(fieldVO);
		logger.info("计算毛利率!");
		fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] { "f", "毛利率",
				UiConstants.DATATYPE.TEXT.intValue() });

		templetVO.getFieldVOs().add(fieldVO);
		for(Map<String, Object> rowMap : list) {
			String vbillno = String.valueOf(rowMap.get("vbillno"));
			UFDouble rece_total = rece_total_amount_map.get(vbillno);
			if(rece_total == null) {
				rece_total = UFDouble.ZERO_DBL;
			}

			UFDouble deli_total = deli_amount_map.get(vbillno);
			if(deli_total == null) {
				deli_total = UFDouble.ZERO_DBL;
			}

			UFDouble other_total = other_amount_map.get(vbillno);
			if(other_total == null) {
				other_total = UFDouble.ZERO_DBL;
			}

			UFDouble arri_total = arri_amount_map.get(vbillno);
			if(arri_total == null) {
				arri_total = UFDouble.ZERO_DBL;
			}

			UFDouble pay_total = deli_total.add(other_total).add(arri_total);// 运费合计
			UFDouble income = rece_total.sub(pay_total);// 营业收入
			rowMap.put("e", income);
			// 毛利率=营业收入/运费合计,使用百分数表示,并保留2位小数
			if(pay_total.doubleValue() == 0) {
				rowMap.put("f", "100%");
			} else {
				double d = income.div(pay_total).doubleValue();
				rowMap.put("f", new BigDecimal(d * 100).setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
			}
		}
	}

	/**
	 * 合并金额
	 * 
	 * @param payDetailList
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<HashMap> mergePayRowMap(List<HashMap> payDetailList) {
		Map<String, HashMap> map1 = new HashMap<String, HashMap>();
		for(HashMap map : payDetailList) {
			String vbillno = String.valueOf(map.get("vbillno"));
			String expense_type_name = String.valueOf(map.get("expense_type_name"));
			UFDouble newAmount = map.get("amount") == null ? UFDouble.ZERO_DBL : new UFDouble(String.valueOf(map
					.get("amount")));
			String key = vbillno + expense_type_name;
			HashMap oriMap = map1.get(key);
			if(oriMap == null) {
				map1.put(key, map);
			} else {
				// 合并
				Object oAmount = oriMap.get("amount");
				if(oAmount == null) {
					oAmount = UFDouble.ZERO_DBL;
				} else {
					oAmount = new UFDouble(String.valueOf(oAmount));
				}
				UFDouble amount = (UFDouble) oAmount;
				amount = amount.add(newAmount);
				oriMap.put("amount", amount);
			}
		}
		List<HashMap> list = new ArrayList<HashMap>();
		list.addAll(map1.values());
		return list;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, UFDouble> buildDynCol(List<HashMap> detailList, UiReportTempletVO templetVO,
			Map<String, Map<String, Object>> valueMap, String fieldCodePrex, String columnGroupName) {
		Map<String, UFDouble> total_amount_map = new HashMap<String, UFDouble>();
		if(detailList != null && detailList.size() > 0) {
			String options = ReportUtils.buildOptions(columnGroupName);
			Map<String, String> expenseTypeMap = new HashMap<String, String>();// 费用类型列表，这个费用类型需要拉平，作为列名
			int index = 0;
			for(int i = 0; i < detailList.size(); i++) {
				HashMap receRowMap = detailList.get(i);
				Object oTypeName = receRowMap.get("expense_type_name");
				if(oTypeName == null) {
					oTypeName = "未分类";
				}
				String typeName = oTypeName.toString();
				if(!expenseTypeMap.keySet().contains(typeName)) {
					String fieldCode = fieldCodePrex + index;
					expenseTypeMap.put(typeName, fieldCode);
					// 增加列
					ReportTempletBVO fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
							fieldCode, typeName, UiConstants.DATATYPE.DECIMAL.intValue() });
					fieldVO.setOptions(options);
					templetVO.getFieldVOs().add(fieldVO);
					index++;
				}
				String vbillno = String.valueOf(receRowMap.get("vbillno"));
				UFDouble amount = UFDouble.ZERO_DBL;
				Object oAmount = receRowMap.get("amount");
				if(oAmount != null) {
					amount = new UFDouble(oAmount.toString());
				}
				// 根据vbillno得到当前的列值
				Map<String, Object> rowMap = valueMap.get(vbillno);
				rowMap.put(expenseTypeMap.get(typeName), amount);

				// 费用小计
				UFDouble total_amount = total_amount_map.get(vbillno);
				if(total_amount == null) {
					total_amount = amount;
				} else {
					total_amount = total_amount.add(amount);
				}
				total_amount_map.put(vbillno, total_amount);
			}
			// 增加合计列
			String countFieldCode = fieldCodePrex + "_count";
			ReportTempletBVO fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					countFieldCode, "合计", UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO);
			for(String key : valueMap.keySet()) {
				valueMap.get(key).put(countFieldCode, total_amount_map.get(key));
			}
		}
		return total_amount_map;
	}
}
