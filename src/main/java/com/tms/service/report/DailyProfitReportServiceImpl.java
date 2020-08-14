package com.tms.service.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.utils.CorpHelper;
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
 * 每日利润分析报表
 * 
 * @author yaojiie
 * @date 2015-11-26 下午02:55:12
 */
@Service
public class DailyProfitReportServiceImpl extends TMSAbsDynReportServiceImpl {

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

		//顶单号数组
		List<String> vbillnoAry = new ArrayList<String>();
		//发货单数组
		List<String> pkInvoiceAry = new ArrayList<String>();
		Map<String, Map<String, Object>> valueMap = new HashMap<String, Map<String, Object>>();
		for(Map<String, Object> rowMap : list) {
			String vbillno = String.valueOf(rowMap.get("vbillno"));
			vbillnoAry.add(vbillno);
			valueMap.put(vbillno, rowMap);
			pkInvoiceAry.add(String.valueOf(rowMap.get("pk_invoice")));
		}
		// 联查费用明细,包括“发货单号，费用类型，金额”
		// 对于vbillno和pk_expense_type相同的字段，SQL返回的结果会合并金额
		// yaojiie 2015 11 28 添加查询结果的公司过滤方法
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		String condStr = NWUtils.buildConditionString(vbillnoAry.toArray(new String[vbillnoAry.size()]));
		String sql = "SELECT trd.invoice_vbillno AS  vbillno,ti.cust_orderno ,tcc.cust_name AS tcc_cust_name,tccb.cust_name AS tccbcust_name, "
				+ "de_tad.addr_code,de_area.name AS de_area ,ar_tad.addr_code,ar_area.name AS ar_area ,ti.num_count,ti.weight_count,ti.volume_count,ti.fee_weight_count "
				+ ",sum(CASE tet.name WHEN '运费' THEN trdb.amount ELSE 0 END ) yf_sum,  "
				+ "(sum(trdb.amount)-sum(CASE tet.name WHEN '运费' THEN trdb.amount ELSE 0 END )  ) AS other_sum, "
				+ "sum(trdb.amount) total "
				+ "	from ts_receive_detail trd  "
				+ "	LEFT JOIN ts_rece_detail_b trdb ON trd.pk_receive_detail =trdb.pk_receive_detail "
				+ "	LEFT JOIN ts_expense_type tet on trdb.pk_expense_type=tet.pk_expense_type "
				+ "	LEFT JOIN ts_invoice ti  ON ti.vbillno = trd.invoice_vbillno "
				+ "	LEFT JOIN ts_customer tcc ON  tcc.pk_customer=ti.pk_customer  AND tcc.dr = 0 "
				+ "	LEFT JOIN ts_customer tccb ON  tccb.pk_customer=ti.bala_customer  AND tccb.dr = 0 "
				+ "	LEFT JOIN ts_area de_area ON de_area.pk_area=ti.deli_city  AND de_area.dr = 0 "
				+ "	LEFT JOIN ts_area ar_area ON ar_area.pk_area=ti.arri_city AND ar_area.dr = 0 "
				+ "	LEFT JOIN ts_address de_tad ON de_tad.pk_address=ti.pk_delivery  AND de_tad.dr = 0 "
				+ "	LEFT JOIN ts_address ar_tad ON ar_tad.pk_address=ti.pk_arrival  AND ar_tad.dr = 0 "
				+ "where  isnull(trd.dr,0)=0 and isnull(trdb.dr,0)=0 and isnull(tet.dr,0)=0 and trd.invoice_vbillno in"
				+ condStr + " and trd." + corpCond + " group by trd.invoice_vbillno,ti.num_count,ti.weight_count,ti.volume_count,ti.fee_weight_count, "
				+ "ti.cust_orderno ,tcc.cust_name ,tccb.cust_name ,de_area.name  ,ar_area.name ,de_tad.addr_code,ar_tad.addr_code";
		logger.info("查询应收明细SQL:" + sql);
		//应收明细
		List<HashMap> receDetailList = NWDao.getInstance().queryForList(sql, HashMap.class);
		// 应收费用合计
		logger.info("计算应收明细的合计费用！");
		Map<String, UFDouble> order_mesg_map = buildCol(receDetailList, templetVO, valueMap, "a", "订单信息");
		Map<String, UFDouble> rece_total_amount_map = buildCol(receDetailList, templetVO, valueMap, "b", "应收明细");

	
		//查询委托单对应的费用信息
		// yaojiie 2015 11 28 添加查询结果的公司过滤方法
		condStr = NWUtils.buildConditionString(pkInvoiceAry.toArray(new String[pkInvoiceAry.size()]));
		//tetbrank2 获取委托单对应属于第几承运人的表  ;
		//tetb.rank =1 获取委托单的第一辆车信息
		sql = "SELECT tetbrank2.RANK1,tetb.rank,ti.vbillno tivbillno, te.vbillno tevbillno,de_tad.addr_name de_addr_name ,ar_tad.addr_name  "
				+ "ar_addr_name,tc.carr_name,tetb.carno,tetb.pk_driver, "
				+ "	sum(CASE name WHEN '运费' THEN tpdb.man_devi_amount ELSE 0 END ) yf_sum, "
				+ "(sum(tpdb.man_devi_amount)-sum(CASE name WHEN '运费' THEN tpdb.man_devi_amount ELSE 0 END )  ) AS other_sum, "
				+ " sum(tpdb.man_devi_amount) total "
				+ "	FROM ts_invoice ti "
				+ "LEFT JOIN ts_ent_inv_b teb ON  teb.pk_invoice=ti.pk_invoice AND teb.dr = 0 "
				+ "LEFT JOIN ts_entrust te ON  teb.pk_entrust=te.pk_entrust AND te.dr = 0  "
				+ "LEFT JOIN ts_carrier tc ON te.pk_carrier=tc.pk_carrier  AND tc.dr = 0 "
				+ "LEFT JOIN ts_address de_tad ON de_tad.pk_address=te.pk_delivery  AND de_tad.dr = 0 "
				+ "LEFT JOIN ts_address ar_tad ON ar_tad.pk_address=te.pk_arrival  AND ar_tad.dr = 0 "
				+ " LEFT JOIN (SELECT pk_entrust,carno,pk_driver, Row_Number() OVER (partition by pk_entrust ORDER BY carno asc) rank FROM ts_ent_transbility_b "
				+ ") tetb ON tetb.pk_entrust=te.pk_entrust and tetb.rank =1 "
				+ "	LEFT JOIN ts_pay_devi_b tpdb ON teb.pk_invoice = tpdb.pk_invoice AND teb.pk_entrust = tpdb.pk_entrust and tpdb.dr = 0 "
				+ "LEFT JOIN ts_expense_type tet ON tet.pk_expense_type=tpdb.pk_expense_type and tet.dr = 0 "
				+ "  LEFT JOIN ( "
				+ "SELECT  Row_Number() OVER (partition by tpdb.pk_invoice  ORDER BY tpdb.TS asc)  AS RANK1,  "
				+ "tpdb.pk_invoice,tpdb.pk_entrust "
				+ "FROM ts_ent_inv_b  tpdb "
				+ "LEFT JOIN ts_invoice ti ON  tpdb.pk_invoice=ti.pk_invoice AND ti.dr = 0 "
				+ "LEFT JOIN ts_entrust te ON  tpdb.pk_entrust=te.pk_entrust AND te.dr = 0 "
				+ "where tpdb.dr=0 AND te.dr=0 "
				+ "GROUP BY tpdb.pk_invoice,tpdb.TS,tpdb.pk_entrust "
				+ ") tetbrank2  ON tetbrank2.pk_invoice=teb.pk_invoice  AND tetbrank2.pk_entrust=teb.pk_entrust "
				+ "where ti.dr=0  AND te.dr=0 and ti.pk_invoice in "
				+ condStr + " and ti." + corpCond + " GROUP BY ti.vbillno , te.vbillno,tc.carr_name,de_tad.addr_name,ar_tad.addr_name,tetb.carno,tetb.pk_driver,tetb.rank,tetbrank2.RANK1 ";
	   
		List<HashMap> WTDlist = NWDao.getInstance().queryForList(sql, HashMap.class);
		
		List<HashMap> WTD1PayDetailList = new ArrayList<HashMap>();// WTD1
		List<HashMap> WTD2PayDetailList = new ArrayList<HashMap>();// WTD2
		List<HashMap> WTD3PayDetailList = new ArrayList<HashMap>();// WTD3
		List<HashMap> WTD4PayDetailList = new ArrayList<HashMap>();// WTD4
		List<HashMap> WTD5PayDetailList = new ArrayList<HashMap>();// WTD5
		if(WTDlist != null && WTDlist.size() > 0) {
			for(int i = 0; i < WTDlist.size(); i++) {
				HashMap WTDRowMap = WTDlist.get(i);
				//分5段
				String WTD = String.valueOf(WTDRowMap.get("rank1"));
				if(StringUtils.isNotBlank(WTD)){
					if(WTD.equals("1")){
						WTD1PayDetailList.add(WTDRowMap);
					}else if(WTD.equals("2")){
						WTD2PayDetailList.add(WTDRowMap);
					}
					else if(WTD.equals("3")){
						WTD3PayDetailList.add(WTDRowMap);
					}
					else if(WTD.equals("4")){
						WTD4PayDetailList.add(WTDRowMap);
					}
					else if(WTD.equals("5")){
						WTD5PayDetailList.add(WTDRowMap);
					}
				}
			}
		}

		//计算分段费用
		logger.info("计算成本！");
		Map<String, UFDouble> WTD1_amount_map = buildCol(WTD1PayDetailList, templetVO, valueMap, "c", "第一承运人");
		Map<String, UFDouble> WTD2_amount_map = buildCol(WTD2PayDetailList, templetVO, valueMap, "d", "第二承运人");
		Map<String, UFDouble> WTD3_amount_map = buildCol(WTD3PayDetailList, templetVO, valueMap, "e", "第三承运人");
		Map<String, UFDouble> WTD4_amount_map = buildCol(WTD4PayDetailList, templetVO, valueMap, "f", "第四承运人");
		Map<String, UFDouble> WTD5_amount_map = buildCol(WTD5PayDetailList, templetVO, valueMap, "g", "第五承运人");

		logger.info("计算营业收入！");
		ReportTempletBVO fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] { "h", "营业收入",
				UiConstants.DATATYPE.DECIMAL.intValue() });
		templetVO.getFieldVOs().add(fieldVO);
		
		logger.info("计算总成本！");
		fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] { "i", "总成本",
				UiConstants.DATATYPE.DECIMAL.intValue() });
		templetVO.getFieldVOs().add(fieldVO);
		
		logger.info("计算毛利！");
		fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] { "j", "毛利",
				UiConstants.DATATYPE.DECIMAL.intValue() });
		templetVO.getFieldVOs().add(fieldVO);
		
		logger.info("计算毛利率!");
		fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] { "k", "毛利率",
				UiConstants.DATATYPE.TEXT.intValue() });

		templetVO.getFieldVOs().add(fieldVO);
		for(Map<String, Object> rowMap : list) {
			String vbillno = String.valueOf(rowMap.get("vbillno"));
			UFDouble rece_total = rece_total_amount_map.get(vbillno);
			if(rece_total == null) {
				rece_total = UFDouble.ZERO_DBL;
			}

			UFDouble order_mesg = order_mesg_map.get(vbillno);
			if(order_mesg == null) {
				order_mesg = UFDouble.ZERO_DBL;
			}
			
			
			UFDouble WTD1_total = WTD1_amount_map.get(vbillno);
			if(WTD1_total == null) {
				WTD1_total = UFDouble.ZERO_DBL;
			}
			UFDouble WTD2_total = WTD2_amount_map.get(vbillno);
			if(WTD2_total == null) {
				WTD2_total = UFDouble.ZERO_DBL;
			}
			UFDouble WTD3_total = WTD3_amount_map.get(vbillno);
			if(WTD3_total == null) {
				WTD3_total = UFDouble.ZERO_DBL;
			}
			UFDouble WTD4_total = WTD4_amount_map.get(vbillno);
			if(WTD4_total == null) {
				WTD4_total = UFDouble.ZERO_DBL;
			}
			UFDouble WTD5_total = WTD5_amount_map.get(vbillno);
			if(WTD5_total == null) {
				WTD5_total = UFDouble.ZERO_DBL;
			}

			UFDouble pay_total = WTD1_total.add(WTD2_total).add(WTD3_total).add(WTD4_total).add(WTD5_total);// 运费合计
			rowMap.put("h", rece_total);
			rowMap.put("i", pay_total);
			UFDouble income = rece_total.sub(pay_total);// 营业收入
			rowMap.put("j", income);
			// 毛利率=营业收入/运费合计,使用百分数表示,并保留2位小数
			if(pay_total.doubleValue() == 0) {
				rowMap.put("k", "100%");
			} else {
				double d = income.div(rece_total).doubleValue();
				rowMap.put("k", new BigDecimal(d * 100).setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, UFDouble> buildCol(List<HashMap> detailList, UiReportTempletVO templetVO,
			Map<String, Map<String, Object>> valueMap, String fieldCodePrex, String columnGroupName) {

		Map<String, UFDouble> total_amount_map = new HashMap<String, UFDouble>();
		String options = ReportUtils.buildOptions(columnGroupName);
		if(columnGroupName.equals("订单信息")){
			options = ReportUtils.buildOptions(columnGroupName);
			
			String typeName0 = "客户订单号";
			String fieldCode0 = fieldCodePrex + 0;
			
			String typeName1 = "订单号";
			String fieldCode1 = fieldCodePrex + 1;
			
			String typeName2 = "客户";
			String fieldCode2 = fieldCodePrex + 2;
			
			String typeName3 = "结算客户";
			String fieldCode3 = fieldCodePrex + 3;
			
			String typeName4 = "提货方";
			String fieldCode4 = fieldCodePrex + 4;
			
			String typeName5 = "提货城市";
			String fieldCode5 = fieldCodePrex + 5;
			
			String typeName6 = "收货方";
			String fieldCode6 = fieldCodePrex + 6;
			
			String typeName7 = "收货城市";
			String fieldCode7 = fieldCodePrex + 7;
			
			String typeName8 = "总件数";
			String fieldCode8 = fieldCodePrex + 8;
			
			String typeName9 = "总体积";
			String fieldCode9 = fieldCodePrex + 9;
			
			String typeName10 = "总计费重";
			String fieldCode10 = fieldCodePrex + 10;
			
			ReportTempletBVO fieldVO0 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode0, typeName0, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO0.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO0);
			
			ReportTempletBVO fieldVO1 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode1, typeName1, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO1.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO1);
			
			ReportTempletBVO fieldVO2 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode2, typeName2, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO2.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO2);
			
			ReportTempletBVO fieldVO3 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode3, typeName3, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO3.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO3);
			
			ReportTempletBVO fieldVO4 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode4, typeName4, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO4.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO4);
			
			ReportTempletBVO fieldVO5 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode5, typeName5, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO5.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO5);
			
			ReportTempletBVO fieldVO6 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode6, typeName6, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO6.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO6);
			
			ReportTempletBVO fieldVO7 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode7, typeName7, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO7.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO7);
			
			ReportTempletBVO fieldVO8 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode8, typeName8, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO8.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO8);
			
			ReportTempletBVO fieldVO9 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode9, typeName9, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO9.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO9);
			
			ReportTempletBVO fieldVO10 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode10, typeName10, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO10.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO10);
			
			for(int i = 0; i < detailList.size(); i++){
				Map<String, Object> rowMap = valueMap.get(detailList.get(i).get("vbillno"));
				rowMap.put(fieldCode0, detailList.get(i).get("cust_orderno"));
				rowMap.put(fieldCode1, detailList.get(i).get("vbillno"));
				rowMap.put(fieldCode2, detailList.get(i).get("tcc_cust_name"));
				rowMap.put(fieldCode3, detailList.get(i).get("tccbcust_name"));
				rowMap.put(fieldCode4, detailList.get(i).get("addr_code"));
				rowMap.put(fieldCode5, detailList.get(i).get("de_area"));
				rowMap.put(fieldCode6, detailList.get(i).get("addr_code"));
				rowMap.put(fieldCode7, detailList.get(i).get("ar_area"));
	            rowMap.put(fieldCode8, detailList.get(i).get("num_count") == null ? (UFDouble.ZERO_DBL):detailList.get(i).get("num_count"));
				rowMap.put(fieldCode9, detailList.get(i).get("volume_count") == null ? (UFDouble.ZERO_DBL):detailList.get(i).get("volume_count"));
				rowMap.put(fieldCode10, detailList.get(i).get("fee_weight_count") == null ? (UFDouble.ZERO_DBL):detailList.get(i).get("fee_weight_count"));
				
			}
		}else if(columnGroupName.equals("应收明细")){
			options = ReportUtils.buildOptions(columnGroupName);
			
			String typeName0 = "运费";
			String fieldCode0 = fieldCodePrex + 0;
			
			String typeName1 = "杂费";
			String fieldCode1 = fieldCodePrex + 1;
			
			ReportTempletBVO fieldVO0 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode0, typeName0, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO0.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO0);
			
			ReportTempletBVO fieldVO1 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode1, typeName1, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO1.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO1);
			
			// 增加合计列
			String countFieldCode = fieldCodePrex + "_count";
			ReportTempletBVO fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					countFieldCode, "合计", UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO);
			
			
			
			for(int i = 0; i < detailList.size(); i++){
				Map<String, Object> rowMap = valueMap.get(detailList.get(i).get("vbillno"));
				rowMap.put(fieldCode0, detailList.get(i).get("yf_sum"));
				rowMap.put(fieldCode1, detailList.get(i).get("other_sum") == null ? (UFDouble.ZERO_DBL):detailList.get(i).get("other_sum"));
				rowMap.put(countFieldCode, detailList.get(i).get("total"));
				
				String vbillno = detailList.get(i).get("vbillno").toString(); 
				
				
				UFDouble amount = UFDouble.ZERO_DBL;
				Object oAmount = detailList.get(i).get("total");
				if(oAmount != null) {
					amount = new UFDouble(oAmount.toString());
				}
				// 费用小计
				UFDouble total_amount = total_amount_map.get(vbillno);
				if(total_amount == null) {
					total_amount = amount;
				} else {
					total_amount = total_amount.add(amount);
				}
				total_amount_map.put(vbillno, total_amount);
			}
		}else{
			options = ReportUtils.buildOptions(columnGroupName);
			
			String typeName0 = "委托单号";
			String fieldCode0 = fieldCodePrex + 0;
			
			String typeName1 = "承运商";
			String fieldCode1 = fieldCodePrex + 1;
			
			String typeName2 = "司机姓名";
			String fieldCode2 = fieldCodePrex + 2;
			
			String typeName3 = "车号";
			String fieldCode3 = fieldCodePrex + 3;
			
			String typeName4 = "提货方编码";
			String fieldCode4 = fieldCodePrex + 4;
			
			String typeName5 = "收货方编码";
			String fieldCode5 = fieldCodePrex + 5;
			
			String typeName6 = "运费";
			String fieldCode6 = fieldCodePrex + 6;
			
			String typeName7 = "杂费";
			String fieldCode7 = fieldCodePrex + 7;
			// 增加列
			ReportTempletBVO fieldVO0 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode0, typeName0, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO0.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO0);
			
			ReportTempletBVO fieldVO1 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode1, typeName1, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO1.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO1);
			
			ReportTempletBVO fieldVO2 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode2, typeName2, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO2.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO2);
			
			ReportTempletBVO fieldVO3 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode3, typeName3, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO3.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO3);
			
			ReportTempletBVO fieldVO4 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode4, typeName4, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO4.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO4);
			
			ReportTempletBVO fieldVO5 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode5, typeName5, UiConstants.DATATYPE.TEXT.intValue() });
			fieldVO5.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO5);
			
			ReportTempletBVO fieldVO6 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode6, typeName6, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO6.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO6);
			
			ReportTempletBVO fieldVO7 = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					fieldCode7, typeName7, UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO7.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO7);
			
			// 增加合计列
			String countFieldCode = fieldCodePrex + "_count";
			ReportTempletBVO fieldVO = ReportUtils.buildField(templetVO.getReportTempletId(), new Object[] {
					countFieldCode, "合计", UiConstants.DATATYPE.DECIMAL.intValue() });
			fieldVO.setOptions(options);
			templetVO.getFieldVOs().add(fieldVO);
			
			for(int i = 0; i < detailList.size(); i++) {
				
				Map<String, Object> rowMap = valueMap.get(detailList.get(i).get("tivbillno"));
				rowMap.put(fieldCode0, detailList.get(i).get("tevbillno"));
				rowMap.put(fieldCode1, detailList.get(i).get("carr_name"));
				rowMap.put(fieldCode2, detailList.get(i).get("pk_driver"));
				rowMap.put(fieldCode3, detailList.get(i).get("carno"));
				rowMap.put(fieldCode4, detailList.get(i).get("de_addr_name"));
				rowMap.put(fieldCode5, detailList.get(i).get("ar_addr_name"));
				rowMap.put(fieldCode6, detailList.get(i).get("yf_sum"));
				rowMap.put(fieldCode7, detailList.get(i).get("other_sum"));
				rowMap.put(countFieldCode, detailList.get(i).get("total"));
				
				String tivbillno = detailList.get(i).get("tivbillno").toString(); 
				UFDouble amount = UFDouble.ZERO_DBL;
				Object oAmount = detailList.get(i).get("total");
				if(oAmount != null) {
					amount = new UFDouble(oAmount.toString());
				}
				// 费用小计
				
				String rank1 = detailList.get(i).get("rank1").toString(); 
				if(rank1.equals("1")){
					total_amount_map.put(tivbillno, amount);
					continue;	
				}else if(rank1.equals("2")){
					total_amount_map.put(tivbillno, amount);
					continue;
				}else if(rank1.equals("3")){
					total_amount_map.put(tivbillno, amount);
					continue;
				}else if(rank1.equals("4")){
					total_amount_map.put(tivbillno, amount);
					continue;
				}else if(rank1.equals("5")){
					total_amount_map.put(tivbillno, amount);
					continue;
				}				
				
			}
		}
		return total_amount_map;
	}
}
