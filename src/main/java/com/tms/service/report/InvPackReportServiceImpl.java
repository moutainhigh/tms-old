package com.tms.service.report;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.inv.InvPackBVO;

/**
 * 货品明细报表
 * 
 * @author xuqc
 * @date 2015-1-12 下午02:55:12
 */
@Service
public class InvPackReportServiceImpl extends TMSAbsReportServiceImpl {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InvPackBVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InvPackBVO.PK_INV_PACK_B);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		HttpServletRequest request = ServletContextHolder.getRequest();
		String vbillno = request.getParameter("vbillno");
		if(StringUtils.isBlank(vbillno)) {
			throw new BusiException("vbillno参数是必须的！");
		}
		String sql = "select pk_invoice from ts_invoice where vbillno=? and isnull(dr,0)=0";
		String pk_invoice = NWDao.getInstance().queryForObject(sql, String.class, vbillno);
		if(StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("发货单[?]不存在或者已经被删除！",vbillno);
		}
		String condBuf = " pk_invoice='" + pk_invoice + "' ";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			condBuf += " and ";
			condBuf += cond;
		}
		return condBuf;
	}
}
