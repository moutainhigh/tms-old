package com.tms.service.inv.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;
import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.OperateTypeConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.inv.InvoiceService;
import com.tms.service.tp.PZService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;
import com.tms.vo.tp.PZHeaderVO;
import com.tms.vo.tp.SegPackBVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 多点运输单的更新操作
 * 
 * @author muyun
 * @date 2016 11 14
 */
public class MultiPointTransportationExcelUpdater extends BillExcelImporter {
	
	private InvoiceService invoiceService = SpringContextHolder.getBean("invoiceServiceImpl");
	
	public MultiPointTransportationExcelUpdater(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new InvoiceVO();
	}

	/**
	 * 导入时，主要做一下步骤
	 * 1，合并送货点的货品明细，生成发货单的包装明细。
	 * 
	 */
	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolveForMultiTable(file);
		Set<String> pk_addressS = new HashSet<String>();
		for (AggregatedValueObject aggVO : aggVOs){
			ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) aggVO;
			InvLineBVO[] invLineBVOs = (InvLineBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
			for(InvLineBVO invLineBVO : invLineBVOs ){
				pk_addressS.add(invLineBVO.getPk_address());
			}
		}
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, "pk_address in " + NWUtils.buildConditionString(pk_addressS));
		for (AggregatedValueObject aggVO : aggVOs){
			ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) aggVO;
			InvLineBVO[] invLineBVOs = (InvLineBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
			for(InvLineBVO invLineBVO : invLineBVOs ){
				for(AddressVO addressVO : addressVOs){
					if(addressVO.getPk_address().equals(invLineBVO.getPk_address())){
						invLineBVO.setPk_address(addressVO.getPk_address());
						invLineBVO.setPk_province(addressVO.getPk_province());
						invLineBVO.setPk_city(addressVO.getPk_city());
						invLineBVO.setPk_area (addressVO.getPk_area());
						invLineBVO.setDetail_addr(addressVO.getDetail_addr());
						invLineBVO.setContact (addressVO.getContact());
						invLineBVO.setPhone (addressVO.getPhone());
						invLineBVO.setMobile  (addressVO.getMobile());
						invLineBVO.setEmail (addressVO.getEmail());
					}
				}
			}
		}
		for (AggregatedValueObject aggVO : aggVOs) {
			ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) aggVO;
			InvoiceVO invoiceVO = (InvoiceVO) aggInvoiceVO.getParentVO();
			if(StringUtils.isBlank(invoiceVO.getVbillno())){
				throw new BusiException("订单更新的时候，发货单号是必输字段！");
			}
			String sql = "SELECT pk_invoice FROM ts_invoice WHERE isnull(dr,0)=0 AND vbillno = ?";
			String pk_invoice = NWDao.getInstance().queryForObject(sql, String.class, invoiceVO.getVbillno());
			if(StringUtils.isBlank(pk_invoice)){
				throw new BusiException("发货单号[?]不是有效的发货单号！",invoiceVO.getVbillno());
			}
			paramVO.setBillId(pk_invoice);
			ExAggInvoiceVO aggInvoiceVO_db = (ExAggInvoiceVO) invoiceService.queryBillVO(paramVO);
			InvoiceVO invoiceVO_db = (InvoiceVO) aggInvoiceVO_db.getParentVO();
			//订单更新分为两种，普通订单的更新和多点运输的更新，如果是普通订单的更新那么主要更新货品和头信息的数据即可，而对于多点运输单，更新的难点在于对线路的处理
			if(invoiceVO_db.getTrans_type() == null){
				//这是旧数据，没有运输类型，跳过
				continue;
			}
			if(invoiceVO_db.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()
					|| invoiceVO_db.getTrans_type() == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
				MultiPointTransportationHandler(aggInvoiceVO, aggInvoiceVO_db);
			}
		}
	}
	
	/**
	 * 对于多点运输的处理，主要是处理线路信息
	 * 在页面强制修订的时候，对于某一个线路，如果修改了地址和货品信息，会修改对应的运段及委托单的提到货数据等
	 * 但是在excel导入的时候，如果修改了地址和货品信息，找不到原本对应的那个线路了，只能认为这个线路是新增的而把原来的那个线路给删除掉。
	 * 所以在更新导入的时候需要导入行号字段用来匹配线路
	 * @param aggInvoiceVO
	 * @param aggInvoiceVO_db
	 */
	public void MultiPointTransportationHandler(ExAggInvoiceVO aggInvoiceVO,ExAggInvoiceVO aggInvoiceVO_db){
		
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<String> pk_segments = new ArrayList<String>();
		InvoiceVO invoiceVO_db = (InvoiceVO) aggInvoiceVO_db.getParentVO();
		Integer trans_type = invoiceVO_db.getTrans_type();
		SegmentVO[] segmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(SegmentVO.class, "invoice_vbillno=?", invoiceVO_db.getVbillno());
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "invoice_vbillno=?", invoiceVO_db.getVbillno());
		
		InvLineBVO[] invLineBVOs = (InvLineBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
		InvLineBVO[] invLineBVOs_db = (InvLineBVO[]) aggInvoiceVO_db.getTableVO(TabcodeConst.TS_INV_LINE_B);
		if(invLineBVOs == null || invLineBVOs.length == 0 || invLineBVOs_db == null || invLineBVOs_db.length == 0 ){
			//只要有一个没有线路就跳出
			return;
		}
		if(trans_type.equals(DataDictConst.TRANSPORT_TYPE.YD.intValue())){
			//一提多送，将没有到货的节点和运段，委托单，全部删除。
			if(segmentVOs != null && segmentVOs.length > 0){
				for(SegmentVO segmentVO : segmentVOs){
					if(segmentVO.getVbillstatus() < BillStatus.SEG_ARRIVAL){
						segmentVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(segmentVO);
						pk_segments.add(segmentVO.getPk_segment());
					}
				}
			}
			if(entrustVOs != null && entrustVOs.length > 0){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillstatus() < BillStatus.ENT_ARRIVAL){
						entrustVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(entrustVO);
					}
				}
			}
		}
		if(trans_type.equals(DataDictConst.TRANSPORT_TYPE.DY.intValue())){
			//多提一送，将没有提货的节点和运段，委托单，全部删除。
			if(segmentVOs != null && segmentVOs.length > 0){
				for(SegmentVO segmentVO : segmentVOs){
					if(segmentVO.getVbillstatus() < BillStatus.SEG_DELIVERY){
						segmentVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(segmentVO);
					}
				}
			}
			if(entrustVOs != null && entrustVOs.length > 0){
				for(EntrustVO entrustVO : entrustVOs){
					if(entrustVO.getVbillstatus() < BillStatus.ENT_DELIVERY){
						entrustVO.setStatus(VOStatus.DELETED);
						toBeUpdate.add(entrustVO);
					}
				}
			}
		}
		String sql = "";
		if(invoiceVO_db.getTrans_type().equals(DataDictConst.TRANSPORT_TYPE.YD.intValue())){
			sql = "SELECT * FROM (SELECT ts_inv_line_b.pk_inv_line_b ,ts_segment.pk_segment ,ts_segment.vbillstatus "
					+ " FROM ts_inv_line_b WITH(NOLOCK) "
					+ " LEFT JOIN ts_invoice  WITH(NOLOCK) ON ts_inv_line_b.pk_invoice=ts_invoice.pk_invoice AND ts_invoice.dr=0 "
					+ " LEFT JOIN ts_segment WITH(NOLOCK) ON ts_segment.invoice_vbillno=ts_invoice.vbillno AND ts_segment.dr=0 AND ts_inv_line_b.pk_address=ts_segment.pk_arrival AND ( (ts_segment.parent_seg IS NULL AND  ts_segment.seg_mark=0) OR ts_segment.seg_mark=2 ) "
					+ " WHERE ts_inv_line_b.dr=0 AND ts_inv_line_b.pk_invoice='"+invoiceVO_db.getPk_invoice()+"' ) "
					+ " TABLE_A WHERE pk_segment IS NULL OR vbillstatus=10  OR vbillstatus=11 OR vbillstatus=12";
		}else if(invoiceVO_db.getTrans_type().equals(DataDictConst.TRANSPORT_TYPE.DY.intValue())){
			sql = "SELECT * FROM ( SELECT ts_inv_line_b.pk_inv_line_b ,ts_segment.pk_segment ,ts_segment.vbillstatus  "
					+ " FROM ts_inv_line_b WITH(NOLOCK) "
					+ " LEFT JOIN ts_invoice  WITH(NOLOCK) ON ts_inv_line_b.pk_invoice=ts_invoice.pk_invoice AND ts_invoice.dr=0 "
					+ " LEFT JOIN ts_segment WITH(NOLOCK) ON ts_segment.invoice_vbillno=ts_invoice.vbillno AND ts_segment.dr=0 AND ts_inv_line_b.pk_address=ts_segment.pk_delivery AND ( (ts_segment.parent_seg IS NULL AND  ts_segment.seg_mark=0) OR ts_segment.seg_mark=2 ) "
					+ " WHERE ts_inv_line_b.dr=0 AND ts_inv_line_b.pk_invoice='"+invoiceVO_db.getPk_invoice()+"' ) "
					+ " TABLE_A WHERE pk_segment IS NULL OR vbillstatus=10  OR vbillstatus=11";
		}else{
			throw new BusiException("不支持此类数据的更新");
		}
		
		List<Map<String,Object>> deleted_info = NWDao.getInstance().queryForList(sql);
		List<String> deleted_pk_inv_line_bs = new ArrayList<String>();
		if(deleted_info != null && deleted_info.size() > 0){
			for(Map<String,Object> map : deleted_info){
				deleted_pk_inv_line_bs.add((String) map.get("pk_inv_line_b"));
			}
		}
		// 删除对应的包装
		if(deleted_pk_inv_line_bs != null && deleted_pk_inv_line_bs.size() > 0){
			InvPackBVO[] deleted_invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class,
					"pk_inv_line_b in " + NWUtils.buildConditionString(deleted_pk_inv_line_bs));
			if (deleted_invPackBVOs != null && deleted_invPackBVOs.length > 0) {
				for (InvPackBVO invPackBVO : deleted_invPackBVOs) {
					invPackBVO.setStatus(VOStatus.DELETED);
					toBeUpdate.add(invPackBVO);
				}
			}
		}
		List<InvLineBVO> allLineBVOs = new ArrayList<InvLineBVO>();//所有可用的线路，用来排序
		for(InvLineBVO invLineBVO_db : invLineBVOs_db){
			boolean needDelete = false;
			if(deleted_pk_inv_line_bs != null && deleted_pk_inv_line_bs.size() > 0){
				for(String deleted_pk_inv_line_b : deleted_pk_inv_line_bs){
					if(invLineBVO_db.getPk_inv_line_b().equals(deleted_pk_inv_line_b)){
						invLineBVO_db.setStatus(VOStatus.DELETED);
						toBeUpdate.add(invLineBVO_db);
						needDelete = true;
						break;
					}
				}
			}
			if(!needDelete){
				invLineBVO_db.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(invLineBVO_db);
				allLineBVOs.add(invLineBVO_db);//添加到待排序的列表里
			}
		}
		
		//导入的线路明细，并不是每个明细都是要导入的，对于那些已经做过操作的节点，是不能导入相关信息的，所以要从excel的数据里将它剔除
		List<InvLineBVO> usefulLines = new ArrayList<InvLineBVO>();
		/**
		 *	两类数据需要处理
		 * 	页面导入的，与数据库完全无关的内容
		 * 	页面导入的，数据库被删除的，这类数据实际上是删除后重新生成的，其实类似于更新
		 * 	数据库不要删除的，这时候，传入的数据需要把这部分删除掉
		 * 	
		 */
		for(InvLineBVO invLineBVO : invLineBVOs){
			boolean exist = false;
			for(InvLineBVO invLineBVO_db : invLineBVOs_db){
				if(invLineBVO.getPk_address().equals(invLineBVO_db.getPk_address())){
					exist = true;
					if(invLineBVO_db.getStatus() == VOStatus.DELETED){
						usefulLines.add(invLineBVO);
						invLineBVO.setStatus(VOStatus.NEW);
						toBeUpdate.add(invLineBVO);
						break;
					}else{
						//非删除数据，这部分数据就不要加入了。
						break;
					}
				}
			}
			//数据库没有的数据，这时候也要加入进来。
			if (!exist) {
				usefulLines.add(invLineBVO);
				invLineBVO.setStatus(VOStatus.NEW);
				toBeUpdate.add(invLineBVO);
			}
		}
		
		allLineBVOs.addAll(usefulLines);
		//将可用的线路排序
		allLineBVOs = lineSort(allLineBVOs,invoiceVO_db);
		//更新发货单的收发货地址等信息
		invoiceVO_db.setStatus(VOStatus.UPDATED);
		invoiceVO_db.setReq_deli_date(allLineBVOs.get(0).getReq_date_from().toString());
		invoiceVO_db.setReq_deli_time(allLineBVOs.get(0).getReq_date_till().toString());
		invoiceVO_db.setPk_delivery(allLineBVOs.get(0).getPk_address());
		invoiceVO_db.setDeli_area(allLineBVOs.get(0).getPk_area());
		invoiceVO_db.setDeli_province(allLineBVOs.get(0).getPk_province());
		invoiceVO_db.setDeli_city(allLineBVOs.get(0).getPk_city());
		invoiceVO_db.setDeli_contact(allLineBVOs.get(0).getContact());
		invoiceVO_db.setDeli_mobile(allLineBVOs.get(0).getMobile());
		invoiceVO_db.setDeli_phone(allLineBVOs.get(0).getPhone());
		invoiceVO_db.setDeli_email(allLineBVOs.get(0).getEmail());
		invoiceVO_db.setDeli_detail_addr(allLineBVOs.get(0).getDetail_addr());
		invoiceVO_db.setReq_arri_date(allLineBVOs.get(allLineBVOs.size()-1).getReq_date_from().toString());
		invoiceVO_db.setReq_arri_time(allLineBVOs.get(allLineBVOs.size()-1).getReq_date_till().toString());
		invoiceVO_db.setPk_arrival(allLineBVOs.get(allLineBVOs.size()-1).getPk_address());
		invoiceVO_db.setArri_area(allLineBVOs.get(allLineBVOs.size()-1).getPk_area());
		invoiceVO_db.setArri_province(allLineBVOs.get(allLineBVOs.size()-1).getPk_province());
		invoiceVO_db.setArri_city(allLineBVOs.get(allLineBVOs.size()-1).getPk_city());
		invoiceVO_db.setArri_contact(allLineBVOs.get(allLineBVOs.size()-1).getContact());
		invoiceVO_db.setArri_mobile(allLineBVOs.get(allLineBVOs.size()-1).getMobile());
		invoiceVO_db.setArri_phone(allLineBVOs.get(allLineBVOs.size()-1).getPhone());
		invoiceVO_db.setArri_email(allLineBVOs.get(allLineBVOs.size()-1).getEmail());
		invoiceVO_db.setArri_detail_addr(allLineBVOs.get(allLineBVOs.size()-1).getDetail_addr());
		toBeUpdate.add(invoiceVO_db);
		//对usefulLine按照地址分组
		Map<String,List<InvLineBVO>> sameAddressGroupMap = new HashMap<String, List<InvLineBVO>>();
		for(InvLineBVO usefulLine : usefulLines){
			String key = usefulLine.getPk_address();
			List<InvLineBVO> voList = sameAddressGroupMap.get(key);
			if(voList == null){
				voList = new ArrayList<InvLineBVO>();
				sameAddressGroupMap.put(key, voList);
			}
			voList.add(usefulLine);
		}
		//创建运段委托单等信息
		segmentAndEntrustCreation(sameAddressGroupMap, invoiceVO_db);
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		//重新统计头信息
		InvPackBVO[] invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", invoiceVO_db.getPk_invoice());
		if(invPackBVOs != null && invPackBVOs.length > 0){
			int serialno = 10;
			for(InvPackBVO invPackBVO : invPackBVOs){
				invPackBVO.setStatus(VOStatus.UPDATED);
				invPackBVO.setSerialno(serialno);
				serialno += 10;
			}
		}
		NWDao.getInstance().saveOrUpdate(invPackBVOs);
		invoiceVO_db = NWDao.getInstance().queryByCondition(InvoiceVO.class, "pk_invoice=?", invoiceVO_db.getPk_invoice());
		InvoiceUtils.setHeaderCount(invoiceVO_db, invPackBVOs);
		invoiceVO_db.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(invoiceVO_db);
	}
	
	
	public void segmentAndEntrustCreation(Map<String,List<InvLineBVO>> sameAddressGroupMap,InvoiceVO invoiceVO){
		if(sameAddressGroupMap == null || sameAddressGroupMap.size() == 0 || invoiceVO == null){
			return;
		}
		for(String key : sameAddressGroupMap.keySet()){
			List<InvLineBVO> lineList = sameAddressGroupMap.get(key);
			if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
				//看看发货单状态
				if(invoiceVO.getVbillstatus() == BillStatus.INV_ARRIVAL
						&& invoiceVO.getVbillstatus() != BillStatus.INV_PART_SIGN
						&& invoiceVO.getVbillstatus() != BillStatus.INV_SIGN
						&& invoiceVO.getVbillstatus() != BillStatus.INV_BACK){
					//这种情况不允许再加节点了。
					throw new BusiException("多提一送业务，单据已经完成运输，不允许新增节点！");
				}
			}
			if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
				if(lineList.get(0).getOperate_type().equals(OperateTypeConst.DELIVERY)){
					continue;
				}
			}
			if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
				if(lineList.get(0).getOperate_type().equals(OperateTypeConst.PICKUP)){
					continue;
				}
			}
			//生成包装明细
			List<InvPackBVO> invPackBVOs = new ArrayList<InvPackBVO>();
			for(InvLineBVO line : lineList){
				NWDao.setUuidPrimaryKey(line);
				InvPackBVO invPackBVO = new InvPackBVO();
				invPackBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(invPackBVO);
				invPackBVO.setPk_invoice(line.getPk_invoice());
				invPackBVO.setPk_inv_line_b(line.getPk_inv_line_b());
				invPackBVO.setPk_goods(line.getPk_goods());
				invPackBVO.setGoods_code(line.getGoods_code());
				invPackBVO.setGoods_name(line.getGoods_name());
				invPackBVO.setPlan_pack_num_count(line.getPlan_pack_num_count());// 计划数量
				invPackBVO.setPack_num_count(line.getPack_num_count());// 数量
				invPackBVO.setPlan_num(line.getPlan_num());// 计划件数
				invPackBVO.setNum(line.getNum());// 件数
				invPackBVO.setPack(line.getPack());
				invPackBVO.setWeight(line.getWeight());
				invPackBVO.setVolume(line.getVolume());
				invPackBVO.setUnit_weight(line.getUnit_weight());
				invPackBVO.setUnit_volume(line.getUnit_volume());
				invPackBVO.setLength(line.getLength());
				invPackBVO.setWidth(line.getWidth());
				invPackBVO.setHeight(line.getHeight());
				invPackBVO.setTrans_note(line.getTrans_note());
				invPackBVO.setLow_temp(line.getLow_temp());
				invPackBVO.setHight_temp(line.getHight_temp());
				invPackBVO.setReference_no(line.getReference_no());
				invPackBVOs.add(invPackBVO);
				NWDao.getInstance().saveOrUpdate(invPackBVO);
			}
			
			//检查是否需要生成运段
			if(invoiceVO.getVbillstatus() > BillStatus.NEW){
				//生成运段
				SegmentVO segVO = new SegmentVO();
				segVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(segVO);
				segVO.setDbilldate(new UFDate());
				segVO.setInvoice_vbillno(invoiceVO.getVbillno());
				segVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YDPZ));
				segVO.setPk_trans_type(invoiceVO.getPk_trans_type());// 运输方式，这个字段在计算总计费重时需要使用
				segVO.setVbillstatus(BillStatus.SEG_WPLAN); // 待计划
				segVO.setSeg_type(SegmentConst.SECTION); // 分段运段
				segVO.setSeg_mark(SegmentConst.SEG_MARK_NORMAL);// 运段标识
				segVO.setMileage(invoiceVO.getMileage());
				segVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				segVO.setCreate_time(new UFDateTime(new Date()));
				segVO.setPk_corp(invoiceVO.getPk_corp());
				segVO.setDeli_method(invoiceVO.getDeli_method());// 派送方式
				segVO.setDistance(invoiceVO.getDistance());
				segVO.setMemo(invoiceVO.getMemo());
				segVO.setDeli_process(invoiceVO.getDeli_process());
				segVO.setArri_process(invoiceVO.getArri_process());
				segVO.setNote(invoiceVO.getNote());
				segVO.setPz_line(invoiceVO.getPz_line());
				segVO.setPz_mileage(invoiceVO.getPz_mileage());
				segVO.setNote(invoiceVO.getNote());
				segVO.setUrgent_level(invoiceVO.getUrgent_level());
				if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.YD.intValue()){
					segVO.setReq_deli_date(invoiceVO.getReq_deli_date());
					segVO.setReq_deli_time(invoiceVO.getReq_deli_time());
					segVO.setPk_delivery(invoiceVO.getPk_delivery());
					segVO.setDeli_city(invoiceVO.getDeli_city());
					segVO.setDeli_province(invoiceVO.getDeli_province());
					segVO.setDeli_area(invoiceVO.getDeli_area());
					segVO.setDeli_detail_addr(invoiceVO.getDeli_detail_addr());
					segVO.setDeli_contact(invoiceVO.getDeli_contact());
					segVO.setDeli_mobile(invoiceVO.getDeli_mobile());
					segVO.setDeli_phone(invoiceVO.getDeli_phone());
					segVO.setDeli_email(invoiceVO.getDeli_mobile());
					
					segVO.setReq_arri_date(lineList.get(0).getReq_date_from().toString());
					segVO.setReq_arri_time(lineList.get(0).getReq_date_from().toString());
					segVO.setPk_arrival(lineList.get(0).getPk_address());
					segVO.setArri_city(lineList.get(0).getPk_city());
					segVO.setArri_province(lineList.get(0).getPk_province());
					segVO.setArri_area(lineList.get(0).getPk_area());
					segVO.setArri_detail_addr(lineList.get(0).getDetail_addr());
					segVO.setArri_contact(lineList.get(0).getContact());
					segVO.setArri_mobile(lineList.get(0).getMobile());
					segVO.setArri_phone(lineList.get(0).getPhone());
					segVO.setArri_email(lineList.get(0).getEmail());
				}else if(invoiceVO.getTrans_type() == DataDictConst.TRANSPORT_TYPE.DY.intValue()){
					segVO.setReq_deli_date(lineList.get(0).getReq_date_from().toString());
					segVO.setReq_deli_time(lineList.get(0).getReq_date_from().toString());
					segVO.setPk_delivery(lineList.get(0).getPk_address());
					segVO.setDeli_city(lineList.get(0).getPk_city());
					segVO.setDeli_province(lineList.get(0).getPk_province());
					segVO.setDeli_area(lineList.get(0).getPk_area());
					segVO.setDeli_detail_addr(lineList.get(0).getDetail_addr());
					segVO.setDeli_contact(lineList.get(0).getContact());
					segVO.setDeli_mobile(lineList.get(0).getMobile());
					segVO.setDeli_phone(lineList.get(0).getPhone());
					segVO.setDeli_email(lineList.get(0).getEmail());
					
					segVO.setReq_arri_date(invoiceVO.getReq_arri_date());
					segVO.setReq_arri_time(invoiceVO.getReq_deli_time());
					segVO.setPk_arrival(invoiceVO.getPk_arrival());
					segVO.setArri_city(invoiceVO.getArri_city());
					segVO.setArri_province(invoiceVO.getArri_province());
					segVO.setArri_area(invoiceVO.getArri_area());
					segVO.setArri_detail_addr(invoiceVO.getArri_detail_addr());
					segVO.setArri_contact(invoiceVO.getArri_contact());
					segVO.setArri_mobile(invoiceVO.getArri_mobile());
					segVO.setArri_phone(invoiceVO.getArri_phone());
					segVO.setArri_email(invoiceVO.getArri_mobile());
				}
				NWDao.getInstance().saveOrUpdate(segVO);
				//生成包装信息
				for(InvPackBVO invPackBVO : invPackBVOs){
					SegPackBVO segPackVO = new SegPackBVO();
					segPackVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(segPackVO);
					segPackVO.setPk_segment(segVO.getPk_segment());
					segPackVO.setSerialno(invPackBVO.getSerialno());
					segPackVO.setPk_invoice(invPackBVO.getPk_invoice());
					segPackVO.setPk_inv_pack_b(invPackBVO.getPk_inv_pack_b());
					segPackVO.setPk_goods(invPackBVO.getPk_goods());
					segPackVO.setGoods_code(invPackBVO.getGoods_code());
					segPackVO.setGoods_name(invPackBVO.getGoods_name());
					segPackVO.setPlan_pack_num_count(invPackBVO.getPlan_pack_num_count());// 计划数量
					segPackVO.setPack_num_count(invPackBVO.getPack_num_count());// 数量
					segPackVO.setPlan_num(invPackBVO.getPlan_num());// 计划件数
					segPackVO.setNum(invPackBVO.getNum());// 件数
					segPackVO.setPack(invPackBVO.getPack());
					segPackVO.setWeight(invPackBVO.getWeight());
					segPackVO.setVolume(invPackBVO.getVolume());
					segPackVO.setUnit_weight(invPackBVO.getUnit_weight());
					segPackVO.setUnit_volume(invPackBVO.getUnit_volume());
					segPackVO.setLength(invPackBVO.getLength());
					segPackVO.setWidth(invPackBVO.getWidth());
					segPackVO.setHeight(invPackBVO.getHeight());
					segPackVO.setTrans_note(invPackBVO.getTrans_note());
					segPackVO.setLow_temp(invPackBVO.getLow_temp());
					segPackVO.setHight_temp(invPackBVO.getHight_temp());
					segPackVO.setReference_no(invPackBVO.getReference_no());
					segPackVO.setMemo(invPackBVO.getMemo());
					NWDao.getInstance().saveOrUpdate(segPackVO);
				}
				String sql = "SELECT TOP 1 * FROM ts_entrust WITH(NOLOCK) "
						+ " WHERE isnull(dr,0)=0 AND invoice_vbillno=?";
				EntrustVO entrustVO = NWDao.getInstance().queryForObject(sql, EntrustVO.class, invoiceVO.getVbillno());
				if(entrustVO != null){
					//把这个运段生成委托单并且提货
					//对这个运段进行配载
					PZHeaderVO pzHeaderVO = new PZHeaderVO();
					pzHeaderVO.setPk_carrier(entrustVO.getPk_carrier());
					pzHeaderVO.setPk_trans_type(entrustVO.getPk_trans_type());
					pzHeaderVO.setBalatype(entrustVO.getBalatype());
					pzHeaderVO.setMemo(entrustVO.getMemo());
					pzHeaderVO.setLot(entrustVO.getLot());
					ExAggEntrustVO pzAggVO = new ExAggEntrustVO();
					pzAggVO.setParentVO(pzHeaderVO);
					EntTransbilityBVO[] entTransbilityVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class, "pk_entrust=?", entrustVO.getPk_entrust());
					pzAggVO.setTableVO(TabcodeConst.TS_ENT_TRANSBILITY_B, entTransbilityVOs);
					pzAggVO.setTableVO(TabcodeConst.TS_SEGMENT, new SegmentVO[]{segVO});
					PZService pZService = (PZService) SpringContextHolder.getBean("PZServiceImpl");
					ParamVO paramVO = new ParamVO();
					paramVO.setFunCode(FunConst.SEG_BATCH_PZ_CODE);
					AggregatedValueObject billVO = pZService.save(pzAggVO,paramVO);
					ExAggEntrustVO exAggEntrustVO = (ExAggEntrustVO) billVO;
					EntrustVO parentVO = (EntrustVO) exAggEntrustVO.getParentVO();
					if(invoiceVO.getTrans_type().equals(DataDictConst.TRANSPORT_TYPE.YD.intValue())){
						//一提多送需要设置为已提货，并且搞一条跟踪信息
						parentVO.setVbillstatus(BillStatus.ENT_DELIVERY);
						parentVO.setStatus(VOStatus.UPDATED);
						String trackingSql = "SELECT TOP 1 * FROM ts_ent_tracking WITH(NOLOCK) WHERE isnull(dr,0)=0 AND entrust_vbillno = ? ORDER BY ts DESC ";
						EntTrackingVO trackingVO = NWDao.getInstance().queryForObject(trackingSql, EntTrackingVO.class, entrustVO.getVbillno());
						if(trackingVO != null){
							EntTrackingVO newTrackingVO = (EntTrackingVO) trackingVO.clone();
							newTrackingVO.setStatus(VOStatus.NEW);
							newTrackingVO.setPk_ent_tracking(null);
							NWDao.setUuidPrimaryKey(newTrackingVO);
							newTrackingVO.setEntrust_vbillno(parentVO.getVbillno());
							NWDao.getInstance().saveOrUpdate(newTrackingVO);
							//将提货点提货
							parentVO.setTracking_memo(trackingVO.getTracking_memo());
							EntLineBVO entLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class, "pk_entrust=? and addr_flag='S'", parentVO.getPk_entrust());
							if(entLineBVO != null){
								entLineBVO.setStatus(VOStatus.UPDATED);
								entLineBVO.setArrival_flag(UFBoolean.TRUE);
								entLineBVO.setAct_arri_date(trackingVO.getTracking_time().toString());
								NWDao.getInstance().saveOrUpdate(entLineBVO);
							}
						}
						NWDao.getInstance().saveOrUpdate(parentVO);
					}
				}
			}
		}
	}
	
	
	public List<InvLineBVO> lineSort(List<InvLineBVO> invLineBVOs,InvoiceVO invoiceVO_db){
		if(invLineBVOs == null || invLineBVOs.size() == 0){
			return invLineBVOs;
		}
		//1.安照地址和时间分组,默认同一地址，时间一样
		Map<String,List<InvLineBVO>> groupMap = new HashMap<String, List<InvLineBVO>>();
		for(InvLineBVO invLineBVO : invLineBVOs){
			Integer operate_type = invLineBVO.getOperate_type();
			String req_date_from = invLineBVO.getReq_date_from().toString();
			String pk_address = invLineBVO.getPk_address();
			String key = operate_type + "," + req_date_from + "," + pk_address;
			List<InvLineBVO> lineList = groupMap.get(key);
			if(lineList == null){
				lineList = new ArrayList<InvLineBVO>();
				groupMap.put(key, lineList);
			}
			lineList.add(invLineBVO);
		}
		List<String> keySetArr = new ArrayList<String>(groupMap.keySet());
		Collections.sort(keySetArr);
		//2.对key进行排序
		List<InvLineBVO> result = new ArrayList<InvLineBVO>();
		int serialno = 10;
		for(String key0 : keySetArr){
			List<InvLineBVO> lineList = groupMap.get(key0);
			for(InvLineBVO line : lineList){
				line.setSerialno(serialno);
				line.setPk_invoice(invoiceVO_db.getPk_invoice());
				result.add(line);
				serialno += 10;
			}
		}
		return result;
	}
	
}
