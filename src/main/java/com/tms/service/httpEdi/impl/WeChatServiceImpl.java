package com.tms.service.httpEdi.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.httpEdi.WeChatService;


/**
 * 
 * @author XIA
 * @date 2016 6 13 
 */

@Service("weChatService")
public class WeChatServiceImpl extends TMSAbsBillServiceImpl implements WeChatService {
	//继承TMSAbsBillServiceImpl 只是为了形式统一，并没有实质关系。
	Logger logger = Logger.getLogger(WeChatServiceImpl.class);
	
	private String viewName = "ts_inv_wechat_view";

	public String getBillType() {
		return null;
	}

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	public List<Map<String, Object>> searchOrders(String cond) {
		String sql = "select * from " + viewName;
		if (StringUtils.isNotBlank(cond)) {
			sql += " where " + cond;
		}
		sql += " order by tracking_time desc ";
		List<Map<String, Object>> result = NWDao.getInstance().queryForList(sql);
		return result;
	}

	public List<Map<String, Object>> searchOrdersCount(String corp_codes, String cust_codes) {
		String sql = "SELECT LEFT(ts_invoice.req_deli_date,7) AS month, "
				+ " convert(varchar(20),CAST(sum(ts_invoice.weight_count)  as decimal(18,2))) AS weight_count , "
				+ " convert(varchar(20),CAST(sum(ts_invoice.num_count)  as int))  AS num_count, "
				+ " convert(varchar(20),CAST(sum(ts_invoice.volume_count)  as decimal(18,2)))  AS volume_count, "
				+ " count(ts_invoice.vbillno) AS Count "
				+ " FROM ts_invoice With(nolock) "
				+ " LEFT JOIN ts_customer With(nolock)     ON ts_invoice.pk_customer = ts_customer.pk_customer "
				+ " AND ts_customer.dr = 0 "
				+ " LEFT JOIN nw_corp With(nolock)    ON nw_corp.pk_corp= ts_invoice.pk_corp "
				+ " WHERE ";
				if(StringUtils.isNotBlank(corp_codes)){
					sql += " nw_corp.corp_code IN (" + corp_codes + ") AND ";
				}
				if(StringUtils.isNotBlank(cust_codes)){
					sql += " ts_customer.cust_code IN (" + cust_codes + ") AND ";
				}
				sql += (" ts_invoice.req_deli_date  BETWEEN DateAdd(Month,-3,CONVERT(char(8),GetDate(),120)+'1') AND  "
				+ " DATEADD(Day,-1,CONVERT(char(8),GetDate(),120)+'1')+1-1.0/3600/24 "
				+ " GROUP BY LEFT(ts_invoice.req_deli_date,7)");
		List<Map<String, Object>> result = NWDao.getInstance().queryForList(sql);
		return result;
	}


	
}
