package com.tms.service.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.cm.ExAggReceiveDetailVO;
import com.tms.vo.cm.ReceiveDetailVO;

/**
 * 应收明细报表的service,不需要定义接口了，当然也可以定义
 * 
 * @author xuqc
 * @date 2013-7-29 下午05:20:09
 */
@Service
public class ReceDetailReportServiceImpl extends TMSAbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggReceiveDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ReceiveDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ReceiveDetailVO.PK_RECEIVE_DETAIL);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
	
//	@SuppressWarnings("unchecked")
//	protected Map<String, Object> buildReportSummaryRowMap(UiReportTempletVO templetVO, Class<? extends SuperVO> clazz,
//			String where) {
//		List<String[]> fieldAry = templetVO.getHeaderSummaryFieldAry();
//		if(fieldAry == null || fieldAry.size() == 0) {
//			return null;
//		}
//		if(clazz == null) {
//			// 没有定义billInfo，重写该方法，进行查询
//			throw new BusiException("没有配置单据信息，请继承该方法，查询合计数据！");
//		}
//		String countSql = DaoHelper.buildSumSelectSql(clazz, where, fieldAry);
//		if(StringUtils.isBlank(countSql)) {
//			return null;
//		}
//		//这个报表使用orderby会出错，所以重写这个方法去掉
//		String newSql = countSql.replaceAll(" order by create_time desc", "");
//		return NWDao.getInstance().queryForObject(newSql, HashMap.class);
//	}
//	
}
