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
 * 委托单参照
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:46:02
 */
@Controller
@RequestMapping(value = "/ref/common/ent")
public class EntrustDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7855191119317451527L;

	public Boolean isFillinable() {
		return true;
	}

	protected String[] getFieldCode() {
		return new String[] { EntrustVO.VBILLNO, EntrustVO.CUST_ORDERNO, EntrustVO.ORDERNO, "carrier_name",EntrustVO.MEMO};
	}

	protected String[] getFieldName() {
		return new String[] { "委托单号", "客户订单号", "订单号", "承运商" ,"备注"};
	}

	protected String[] getHiddenFieldCode() {
		return new String[] { EntrustVO.PK_CARRIER };
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
			String pk_invoice = NWDao.getInstance().queryForObject("select pk_invoice from ts_invoice where vbillno=?",
					String.class, invoice_vbillno);
			if(StringUtils.isNotBlank(pk_invoice)) {
				cond.append(" and pk_entrust in (select pk_entrust from ts_ent_inv_b WITH(NOLOCK) where pk_invoice='" + pk_invoice
						+ "') ");
			}
		}

		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		PaginationVO paginationVO = baseRefService.queryForPagination(EntrustVO.class, offset, pageSize,
				cond.toString());

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
		return new String[] { "carrier_name->getColValue(ts_carrier,carr_name,pk_carrier,pk_carrier)" };
	}

	public String getPkFieldCode() {
		return EntrustVO.VBILLNO;
	}

	public String getCodeFieldCode() {
		return EntrustVO.VBILLNO;
	}

	public String getNameFieldCode() {
		return EntrustVO.VBILLNO;
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null,NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", code));
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, NWDao.getInstance().queryByCondition(EntrustVO.class, "pk_entrust=?", pk));
	}

}
