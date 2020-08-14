package com.tms.web.cm;

import java.util.HashMap;
import java.util.Map;

import com.tms.constants.DataDictConst;
import com.tms.constants.PriceTypeConst;
import com.tms.constants.QuoteTypeConst;
import com.tms.constants.ValuationTypeConst;

/**
 * 合同明细模板定义类 --已没有使用，使用通用的单据导入配置
 * 
 * @author xuqc
 * @date 2012-8-29 下午09:08:22
 */
@Deprecated
public class ContractTempletDefine {

	/**
	 * 模板中的标题和字段名称的对应
	 */
	private static Map<String, String> header = new HashMap<String, String>();

	/**
	 * 地址类型Map
	 */
	private static Map<String, Integer> addrTypeMap = new HashMap<String, Integer>();

	/**
	 * 报价类型Map
	 */
	private static Map<String, Integer> quoteTypeMap = new HashMap<String, Integer>();

	/**
	 * 价格类型Map
	 */
	private static Map<String, Integer> priceTypeMap = new HashMap<String, Integer>();

	/**
	 * 计价方式Map
	 */
	private static Map<String, Integer> valuationTypeMap = new HashMap<String, Integer>();

	public static Map<String, String> getHeaderMap() {
		if(header.size() == 0) {
			header.put("始发区域类型", "start_addr_type");
			header.put("始发区域", "start_addr");
			header.put("目的区域类型", "end_addr_type");
			header.put("目的区域", "end_addr");
			header.put("费用类型", "pk_expense_type");
			header.put("报价类型", "quote_type");
			header.put("价格类型", "price_type");
			header.put("计价方式", "valuation_type");
			header.put("税种", "tax_cat");
			header.put("税率", "tax_rate");
			header.put("设备类型", "equip_type");
			header.put("最低收费", "lowest_fee");
			header.put("首重", "first_weight");
			header.put("首重价格", "first_weight_price");
			header.put("区间1", "interval1");
			header.put("价格1", "price1");
			header.put("区间2", "interval2");
			header.put("价格2", "price2");
			header.put("区间3", "interval3");
			header.put("价格3", "price3");
			header.put("区间4", "interval4");
			header.put("价格4", "price4");
			header.put("区间5", "interval5");
			header.put("价格5", "price5");
			header.put("区间6", "interval6");
			header.put("价格6", "price6");
			header.put("区间7", "interval7");
			header.put("价格7", "price7");
			header.put("区间8", "interval8");
			header.put("价格8", "price8");
			header.put("区间9", "interval9");
			header.put("价格9", "price9");
			header.put("区间10", "interval10");
			header.put("价格10", "price10");
			header.put("区间11", "interval11");
			header.put("价格11", "price11");
			header.put("区间12", "interval12");
			header.put("价格12", "price12");
		}
		return header;
	}

	// 地址类型
	public static Map<String, Integer> getAddrTypeMap() {
		if(addrTypeMap.size() == 0) {
			addrTypeMap.put("城市", DataDictConst.ADDR_TYPE.CITY.intValue());
			addrTypeMap.put("地址", DataDictConst.ADDR_TYPE.ADDR.intValue());
		}
		return addrTypeMap;
	}

	// 计价方式
	public static Map<String, Integer> getValuationTypeMap() {
		if(valuationTypeMap.size() == 0) {
			valuationTypeMap.put("重量", ValuationTypeConst.WEIGHT);
			valuationTypeMap.put("体积", ValuationTypeConst.VOLUME);
			valuationTypeMap.put("件数", ValuationTypeConst.NUM);
			valuationTypeMap.put("件", ValuationTypeConst.NUM);// 冗余一个，免得excel直接写成“件”了
			valuationTypeMap.put("设备", ValuationTypeConst.EQUIP);
			valuationTypeMap.put("顿公里", ValuationTypeConst.TON_KM);
			valuationTypeMap.put("票", ValuationTypeConst.TICKET);
			valuationTypeMap.put("节点", ValuationTypeConst.NODE);
			valuationTypeMap.put("数量", ValuationTypeConst.PACK_NUM);
		}
		return valuationTypeMap;
	}

	// 报价类型
	public static Map<String, Integer> getQuoteTypeMap() {
		if(quoteTypeMap.size() == 0) {
			quoteTypeMap.put("区间报价", QuoteTypeConst.INTERVAL);
			quoteTypeMap.put("首重报价", QuoteTypeConst.FIRST_WEIGHT);
		}
		return quoteTypeMap;
	}

	// 价格类型
	public static Map<String, Integer> getPriceTypeMap() {
		if(priceTypeMap.size() == 0) {
			priceTypeMap.put("单价", PriceTypeConst.UNIT_PRICE);
			priceTypeMap.put("固定价格", PriceTypeConst.REGULAR_PRICE);
		}
		return priceTypeMap;
	}
}
