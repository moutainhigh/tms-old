package com.tms.service.cm.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;

import com.tms.BillStatus;
import com.tms.constants.PayDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.cm.PayDetailService;
import com.tms.service.cm.ReceiveDetailService;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.ExAggReceiveDetailVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.PayDeviBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLotVO;
import com.tms.vo.te.EntSegBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.tp.SegmentVO;

import net.sf.jasperreports.web.util.WebUtil;

/**
 * 应收明细调整凭证导入
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class ReceDetailAdjustmentVoucherExcelImporter extends BillExcelImporter {
	
	private ReceiveDetailService receDetailService = SpringContextHolder.getBean("receiveDetailServiceImpl");

	public ReceDetailAdjustmentVoucherExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new ReceiveDetailVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		//对aggVOs 安照发货单号合并
		Map<String,List<AggregatedValueObject>> groupMap = new HashMap<String, List<AggregatedValueObject>>();
		for(AggregatedValueObject aggVO : aggVOs){
			String key = (String) aggVO.getParentVO().getAttributeValue("invoice_vbillno");
			if(StringUtils.isBlank(key)){
				key = Math.random() + "";//随便搞一个随机数做key就可以了。
			}
			List<AggregatedValueObject> volist = groupMap.get(key);
			if(volist == null){
				volist = new ArrayList<AggregatedValueObject>();
				groupMap.put(key, volist);
			}
			volist.add(aggVO);
		}
		for(String key : groupMap.keySet()){
			List<AggregatedValueObject> volist = groupMap.get(key);
			ExAggReceiveDetailVO billVO = new ExAggReceiveDetailVO();
			ReceiveDetailVO parentVO = (ReceiveDetailVO) volist.get(0).getParentVO();
			parentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			parentVO.setVbillstatus(BillStatus.NEW);
			billVO.setParentVO(parentVO);
			ReceDetailBVO[] detailBVOs = new ReceDetailBVO[volist.size()];
			int index = 0;
			for(AggregatedValueObject aggVO : volist){
				ReceDetailBVO detailBVO = (ReceDetailBVO) ((ExAggReceiveDetailVO) aggVO).getTableVO(TabcodeConst.TS_RECE_DETAIL_B)[0];
				detailBVOs[index] = detailBVO;
				index++;
			}
			billVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B, detailBVOs);
			receDetailService.save(billVO, paramVO);
		}
	}
}
