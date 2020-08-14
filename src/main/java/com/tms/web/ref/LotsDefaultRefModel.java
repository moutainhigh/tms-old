package com.tms.web.ref;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nw.basic.util.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.vo.base.GoodsVO;
import com.tms.vo.te.EntrustVO;

/**
 * 批次参照
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:46:02
 */
@Controller
@RequestMapping(value = "/ref/common/lot")
public class LotsDefaultRefModel extends AbstractGridRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7855191119317451527L;

	public Boolean isFillinable() {
		return true;
	}

	protected String[] getFieldCode() {
		return new String[] {"lot","carno", "pk_driver", EntrustVO.REQ_DELI_DATE, EntrustVO.REQ_ARRI_DATE,"cust_orderno", "orderno" , "pack_num_count", "num_count", "weight_count", "volume_count", "zps"};
	}

	protected String[] getFieldName() {
		return new String[] {"批次号",  "车牌号",  "司机", "要求提货日期", "要求到货日期", "客户订单号","订单号", "总件数", "总数量", "总重量", "总体积","发货单数量"};
	}

	public Map<String, Object> load4Grid(HttpServletRequest request) {
		//得到前台关键字
		String strKeyWord =  request.getParameter(Constants.GRID_QUERY_KEYWORD);
		StringBuffer cond = new StringBuffer(" and  1=1 ");
		String condition = "";
		
		//拼接条件
		if(StringUtils.isNotBlank(strKeyWord)) {
			strKeyWord = "'%" + strKeyWord +"%'";
			condition = " and ( ( telbmin.lot like "+strKeyWord+" ) or ( telbmin.req_deli_date like "+strKeyWord+" ) or ( telbmax.req_arri_date like "+strKeyWord+" ) "
					+ " or ( te2.carno like "+strKeyWord+" )or ( te2.pk_driver like  "+strKeyWord+"  ) or ( te2.cust_orderno like  "+strKeyWord+"  )"
					+ " or ( te2.orderno like  "+strKeyWord+" ))  ";
			cond.append(condition);
		}
		
		String sql = "SELECT telbmin.req_deli_date,telbmax.req_arri_date,te2.carno, te2.pk_driver,te2.cust_orderno, te2.orderno ,tel.lot,tel.pk_corp, count(te.vbillno) AS zps,sum(te.pack_num_count) AS pack_num_count,sum(te.num_count) AS num_count,sum(te.weight_count) AS weight_count  ,sum(te.volume_count) AS volume_count "
				+ "FROM ts_entrust_lot tel WITH(NOLOCK) LEFT JOIN  ts_entrust te WITH(NOLOCK) ON tel.lot= te.lot AND te.dr=0 AND te.vbillstatus <>'24' "
				+ "LEFT JOIN  ts_entrust te2 WITH(NOLOCK) ON tel.pk_entrust= te2.pk_entrust AND te2.dr=0 "
				+ "LEFT JOIN  ( SELECT ts_entrust.lot,MAX(ts_ent_line_b.req_arri_date) AS req_arri_date FROM ts_ent_line_b WITH(NOLOCK)  LEFT JOIN  ts_entrust WITH(NOLOCK) ON ts_entrust.pk_entrust= ts_ent_line_b.pk_entrust WHERE ts_ent_line_b.dr=0  GROUP BY ts_entrust.lot)	telbmax ON telbmax.lot=tel.lot 	   "
				+ "LEFT JOIN  ( SELECT ts_entrust.lot,Min(ts_ent_line_b.req_arri_date) AS req_deli_date FROM ts_ent_line_b WITH(NOLOCK)  LEFT JOIN  ts_entrust WITH(NOLOCK) ON ts_entrust.pk_entrust= ts_ent_line_b.pk_entrust WHERE ts_ent_line_b.dr=0  GROUP BY ts_entrust.lot)	telbmin ON telbmin.lot=tel.lot 	"
				+ " WHERE tel.dr=0  AND  getdate() -7< telbmin.req_deli_date ";
		
		String strGroupBY = "GROUP BY tel.lot,tel.pk_corp, "
					+"te2.carno,te2.pk_driver, "
					+"telbmax.req_arri_date,telbmin.req_deli_date,te2.cust_orderno,te2.orderno ";
					//+"ORDER BY telbmin.req_deli_date DESC";

		sql = sql  + cond.toString();
		sql = sql + strGroupBY.toString();
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		
		@SuppressWarnings("rawtypes")
		PaginationVO paginationVO = NWDao.getInstance().queryBySqlWithPaging(sql, offset, pageSize, null);
		return this.genAjaxResponse(true, null, paginationVO);
	}


	public String getPkFieldCode() {
		return "lot";
	}

	public String getCodeFieldCode() {
		return  "lot";
	}

	public String getNameFieldCode() {
		return  "lot";
	}

	public Map<String, Object> getByCode(String code) {
		return this.genAjaxResponse(true, null, code);
	}

	public Map<String, Object> getByPk(String pk) {
		return this.genAjaxResponse(true, null, pk);
	}

}
