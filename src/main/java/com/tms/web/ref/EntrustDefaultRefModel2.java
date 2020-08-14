package com.tms.web.ref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.formula.FormulaParser;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.te.EntrustVO;

/**
 * 委托单参照2
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:46:02
 */
@Controller
@RequestMapping(value = "/ref/common/ent2")
public class EntrustDefaultRefModel2 extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7855191119317451527L;

	public Boolean isFillinable() {
		return true;
	}

	protected String[] getFieldCode() {
		return new String[] { EntrustVO.LOT,EntrustVO.CARNO,EntrustVO.PK_DRIVER, "cust_name", "carrier_name","delivery_name","arrival_name","arri_city_name","req_deli_date","req_arri_date",
				EntrustVO.VBILLNO, EntrustVO.CUST_ORDERNO, EntrustVO.ORDERNO, EntrustVO.MEMO};
	}

	protected String[] getFieldName() {
		return new String[] {"批次号","车牌号","司机", "客户", "承运商" ,"提货方","收货方","收货城市","要求提货日期","要求到货日期", "委托单号", "客户订单号", "订单号","备注"};
	}

	protected String[] getHiddenFieldCode() {
		return new String[] { EntrustVO.PK_CARRIER ,EntrustVO.PK_DELIVERY,EntrustVO.PK_ARRIVAL};
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> load4Grid(HttpServletRequest request) {
		String condition = getGridQueryCondition(request, EntrustVO.class);
		String whereClause = this.getExtendCond(request);
		StringBuffer cond = new StringBuffer(" isnull(dr,0)=0 ");
		if(StringUtils.isNotBlank(condition)) {
			cond.append(" and ");
			cond.append(condition);
		}
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		String corpCond = CorpHelper.getCurrentCorpWithChildren();
		if(StringUtils.isNotBlank(corpCond)) {
			cond.append(" and ");
			cond.append(corpCond);
		}
		String invoice_vbillno = request.getParameter("invoice_vbillno");
		if(StringUtils.isNotBlank(invoice_vbillno)) {
			String pk_invoice = NWDao.getInstance().queryForObject("select pk_invoice from ts_invoice WITH(NOLOCK)  where vbillno=?",
					String.class, invoice_vbillno);
			if(StringUtils.isNotBlank(pk_invoice)) {
				cond.append(" and pk_entrust in (select pk_entrust from ts_ent_inv_b  WITH(NOLOCK)  where pk_invoice='" + pk_invoice
						+ "') ");
			}
		}

		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO paginationVO = baseRefService.queryForPagination(EntrustVO.class, offset, pageSize,
				cond.toString() + " order by lot desc ");

		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<SuperVO> superVOList = paginationVO.getItems();
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(superVOList.size());
		for(SuperVO vo : superVOList) {
			Map<String, Object> map = new HashMap<String, Object>();
			for(String key : vo.getAttributeNames()) {
				map.put(key, vo.getAttributeValue(key));
			}
			mapList.add(map);
		}
		superVOList.clear();

		// 执行公式
		FormulaParser formulaParse = new FormulaParser(NWDao.getInstance().getDataSource());
		formulaParse.setFormulas(getFormulas());
		formulaParse.setContext(mapList);
		formulaParse.setMergeContextToResult(true);//
		List<Map<String, Object>> retList = formulaParse.getResult();

		paginationVO.setItems(retList);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	public String[] getFormulas() {
		return new String[] { "carrier_name->getColValue(ts_carrier,carr_name,pk_carrier,pk_carrier)",
							  "cust_name->getColValue(ts_customer, cust_name, pk_customer, pk_customer)",
							  "delivery_name->getColValue(ts_address, addr_name, pk_address, pk_delivery)",
							  "arrival_name->getColValue(ts_address, addr_name, pk_address, pk_arrival)",
							  "arri_city_name->getColValue(ts_area,name, pk_area, arri_city)"};
	}

	public String getPkFieldCode() {
		return EntrustVO.LOT;
	}

	public String getCodeFieldCode() {
		return EntrustVO.LOT;
	}

	public String getNameFieldCode() {
		return EntrustVO.LOT;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,
				baseRefService.getVOByCode(EntrustVO.class, code, "isnull(locked_flag,'N')='N'"));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, baseRefService.getVOByPk(EntrustVO.class, pk));
	}

}
