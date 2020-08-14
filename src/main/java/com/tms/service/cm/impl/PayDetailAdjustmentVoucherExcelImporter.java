package com.tms.service.cm.impl;

import java.io.File;
import java.util.ArrayList;
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
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;

import com.tms.BillStatus;
import com.tms.constants.PayDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.cm.PayDetailService;
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

/**
 * 应收明细调整凭证导入
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class PayDetailAdjustmentVoucherExcelImporter extends BillExcelImporter {
	
	private PayDetailService payDetailService = SpringContextHolder.getBean("payDetailServiceImpl");

	public PayDetailAdjustmentVoucherExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new PayDetailVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		check(aggVOs);
		Map<String,List<AggregatedValueObject>> groupMap = new HashMap<String, List<AggregatedValueObject>>();
		for(AggregatedValueObject aggVO : aggVOs){
			String key = (String) aggVO.getParentVO().getAttributeValue("entrust_vbillno");
			if(StringUtils.isBlank(key)){
				key = (String) aggVO.getParentVO().getAttributeValue("lot");
				if(StringUtils.isBlank(key)){
					key = Math.random() + "";//随便搞一个随机数做key就可以了。
				}
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
			ExAggPayDetailVO billVO = new ExAggPayDetailVO();
			PayDetailVO parentVO = (PayDetailVO) volist.get(0).getParentVO();
			parentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			parentVO.setVbillstatus(BillStatus.NEW);
			billVO.setParentVO(parentVO);
			PayDetailBVO[] detailBVOs = new PayDetailBVO[volist.size()];
			int index = 0;
			for(AggregatedValueObject aggVO : volist){
				PayDetailBVO detailBVO = (PayDetailBVO) ((ExAggPayDetailVO) aggVO).getTableVO(TabcodeConst.TS_PAY_DETAIL_B)[0];
				detailBVOs[index] = detailBVO;
				index++;
			}
			billVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, detailBVOs);
			payDetailService.save(billVO, paramVO);
		}
	}
	
	public void check(List<AggregatedValueObject> aggVOs){
		Set<String> lots = new HashSet<String>();
		Set<String> entrust_vbillnos = new HashSet<String>();
		for(AggregatedValueObject aggVO : aggVOs){
			PayDetailVO payDetailVO = (PayDetailVO)aggVO.getParentVO();
			//统计批次号和委托单号
			if(StringUtils.isNotBlank(payDetailVO.getLot())){
				lots.add(payDetailVO.getLot());
			}
			if(StringUtils.isNotBlank(payDetailVO.getEntrust_vbillno())){
				entrust_vbillnos.add(payDetailVO.getEntrust_vbillno());
			}
			
			
		}
		EntLotVO[] entLotVOs = null;
		if(lots.size() > 0){
			String lotsCond = NWUtils.buildConditionString(lots.toArray(new String[lots.size()]));
			entLotVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLotVO.class, "lot in " +  lotsCond);
			if(entLotVOs == null){
				throw new BusiException("批次号[?]维护错误！",lotsCond);
			}else{
				if(entLotVOs.length != lots.size()){
					for(String lot : lots){
						boolean exist = false;
						for(EntLotVO entLotVO : entLotVOs){
							if(entLotVO.getLot().equals(lot)){
								exist = true;
								break;
							}
						}
						if(!exist){
							throw new BusiException("批次号[?]维护错误！",lot);
						}
					}
				}
			}
		}
		EntrustVO[] entrustVOs = null;
		if(entrust_vbillnos.size() > 0){
			String entrustVbillnosCond = NWUtils.buildConditionString(entrust_vbillnos.toArray(new String[entrust_vbillnos.size()]));
			entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "vbillno in " +  entrustVbillnosCond);
			if(entrustVOs == null){
				throw new BusiException("委托单号[?]维护错误！",entrustVbillnosCond);
			}else{
				if(entrustVOs.length != entrust_vbillnos.size()){
					for(String entrust_vbillno : entrust_vbillnos){
						boolean exist = false;
						for(EntrustVO entrustVO : entrustVOs){
							if(entrustVO.getVbillno().equals(entrust_vbillno)){
								exist = true;
								break;
							}
						}
						if(!exist){
							throw new BusiException("委托单号[?]维护错误！",entrust_vbillno);
						}
					}
				}
			}
		}
		//严重批次号和委托单号是否一致
		for(AggregatedValueObject aggVO : aggVOs){
			PayDetailVO payDetailVO = (PayDetailVO)aggVO.getParentVO();
			if(StringUtils.isNotBlank(payDetailVO.getEntrust_vbillno())
					&& StringUtils.isNotBlank(payDetailVO.getLot())){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
						if(!entrustVO.getVbillno().equals(payDetailVO.getLot())){
							throw new BusiException("委托单号[?],和批次号[?]维护错误！",payDetailVO.getEntrust_vbillno(),payDetailVO.getLot());
						}else{
							break;
						}
					}
				}
			}
			if(StringUtils.isNotBlank(payDetailVO.getPk_carrier())
					&& StringUtils.isNotBlank(payDetailVO.getEntrust_vbillno())){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillno().equals(payDetailVO.getEntrust_vbillno())){
						if(!entrustVO.getPk_carrier().equals(payDetailVO.getPk_carrier())){
							throw new BusiException("委托单号[?],和承运商[?]维护错误！",payDetailVO.getEntrust_vbillno(),payDetailVO.getLot());
						}else{
							break;
						}
					}
				}
			}
			if(StringUtils.isBlank(payDetailVO.getPk_carrier())
					&& StringUtils.isBlank(payDetailVO.getEntrust_vbillno())
					&& StringUtils.isBlank(payDetailVO.getLot())){
				throw new BusiException("批次号，委托单号,和承运商不能同时为空！",payDetailVO.getEntrust_vbillno(),payDetailVO.getLot());
			}
		}
	}
}
