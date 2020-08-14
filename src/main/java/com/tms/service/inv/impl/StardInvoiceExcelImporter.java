package com.tms.service.inv.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IBillService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.WebUtils;

import com.tms.BillStatus;
import com.tms.constants.DataDictConst;
import com.tms.constants.TabcodeConst;
import com.tms.vo.base.AddressVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvLineBVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvReqBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;

/**
 * 发货单的导入类
 * 
 * @author xuqc
 * @date 2014-4-9 下午10:17:52
 */
public class StardInvoiceExcelImporter extends BillExcelImporter {

	public StardInvoiceExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	protected void setValueToSuperVO(BillTempletBVO fieldVO, SuperVO superVO, String fieldCode, Object realValue) {
		if(realValue == null) {
			return;
		}
		super.setValueToSuperVO(fieldVO, superVO, fieldCode, realValue);
		if(fieldCode.equals(InvPackBVO.GOODS_CODE)) {
			RefVO refVO = (RefVO) realValue;// 如果是商品编码，此时是一个表体参照，返回的是refVO
			superVO.setAttributeValue(InvPackBVO.GOODS_CODE, refVO.getCode());
			superVO.setAttributeValue(InvPackBVO.GOODS_NAME, refVO.getName());
		}

	}

	//标准导入
	protected void processBeforeImport(AggregatedValueObject billVO, ParamVO paramVO) {
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		if(StringUtils.isNotBlank(parentVO.getVbillno())){
			InvoiceVO oldInvoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "vbillno=?", parentVO.getVbillno());
			if(oldInvoiceVO != null){
				parentVO.setStatus(VOStatus.UPDATED);
				parentVO.setPk_invoice(oldInvoiceVO.getPk_invoice());
				parentVO.setVbillno(oldInvoiceVO.getVbillno());
				//删除包装，重新生成
				InvPackBVO[] oldInvPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", oldInvoiceVO.getPk_invoice());
				if(oldInvPackBVOs != null && oldInvPackBVOs.length > 0){
					NWDao.getInstance().delete(oldInvPackBVOs);
				}
			}
		}
		String sql = "";
		// 一、记录提货方地址;
		// 1、根据提货方pk
		sql = "select * from ts_address where isnull(dr,0)=0 and pk_address=?";
		List<AddressVO> addrVOs = NWDao.getInstance().queryForList(sql, AddressVO.class, parentVO.getPk_delivery());
		if(addrVOs == null || addrVOs.size() == 0) {
			throw new BusiException("根据提货方PK[?]没有匹配到地址记录！",parentVO.getPk_delivery());
		}
		if(addrVOs.size() > 1) {
			throw new BusiException("根据提货方PK[?]匹配到多条地址记录，基础数据有问题！",parentVO.getPk_delivery());
		}

		// 2、匹配到了，将信息填写
		AddressVO addrVO = addrVOs.get(0);
		parentVO.setPk_delivery(addrVO.getPk_address());
		parentVO.setDeli_city(addrVO.getPk_city());
		parentVO.setDeli_province(addrVO.getPk_province());
		parentVO.setDeli_area(addrVO.getPk_area());
		parentVO.setDeli_detail_addr(addrVO.getDetail_addr());
		parentVO.setDeli_contact(addrVO.getContact());
		parentVO.setDeli_phone(addrVO.getPhone());
		parentVO.setDeli_mobile(addrVO.getMobile());
		
		// 二、记录收货方地址
		// // 1、根据收货方pk
		sql = "select * from ts_address where isnull(dr,0)=0 and pk_address=?";
		List<AddressVO> addrVOs1 = NWDao.getInstance().queryForList(sql, AddressVO.class, parentVO.getPk_arrival());
		if(addrVOs1 == null || addrVOs1.size() == 0) {
			throw new BusiException("根据收货方PK[?]没有匹配到地址记录！",parentVO.getPk_arrival());
		}
		if(addrVOs1.size() > 1) {
			throw new BusiException("根据收货方PK[?]匹配到多条地址记录，基础数据有问题！",parentVO.getPk_arrival());
		}
		// 3、匹配到了，将信息填写
		AddressVO addrVO1 = addrVOs1.get(0);
		parentVO.setPk_arrival(addrVO1.getPk_address());
		parentVO.setArri_city(addrVO1.getPk_city());
		parentVO.setArri_province(addrVO1.getPk_province());
		parentVO.setArri_area(addrVO1.getPk_area());
		parentVO.setArri_detail_addr(addrVO1.getDetail_addr());
		parentVO.setArri_contact(addrVO1.getContact());
		parentVO.setArri_phone(addrVO1.getPhone());
		parentVO.setArri_mobile(addrVO1.getMobile());
		
		// 根据客户匹配默认结算客户，如果没有
		if(StringUtils.isNotBlank(parentVO.getPk_customer())) {
			sql = "select pk_related_cust from ts_cust_bala where pk_customer=? and isnull(locked_flag,'N')='N' and is_default='Y'";
			String pk_related_cust = NWDao.getInstance().queryForObject(sql, String.class, parentVO.getPk_customer());
			parentVO.setBala_customer(pk_related_cust);
		}

		super.processBeforeImport(billVO, paramVO);

		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO;
		// 统计总件数、重量、体积,体积重，计费重
		InvoiceUtils.setHeaderCount(billVO, paramVO);
		// 计算表体金额，表头总金额
		ReceDetailBVO[] detailBVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		TransBilityBVO[] tbBVOs = (TransBilityBVO[]) aggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
		InvoiceUtils.setBodyDetailAmount(parentVO, detailBVOs, tbBVOs);
		InvoiceUtils.setHeaderCostAmount(parentVO, detailBVOs);
	}
	
	
//	//标准导入
//	public void processAfterResolveOneRow(SuperVO parentVO1, SuperVO childVO1, List<AggregatedValueObject> aggVOs,
//			int rowNum) {
//		// 通过‘发货人、收货方、收货方、送货日期、运输方式、提货日期’都相同的判断这是一票独立的委托单
//		Object oVbillno = parentVO1.getAttributeValue(VBILLNO);
//		InvoiceVO parentVO = (InvoiceVO) parentVO1;
//		if(oVbillno == null) {
//			// 如果不为空的情况在父类已经做了处理
//			InvPackBVO childVO = (InvPackBVO) childVO1;
//			String pk_customer = parentVO.getPk_customer();// 发货人
//			String pk_trans_type = parentVO.getPk_trans_type();// 运输方式
//			String req_deli_date = parentVO.getReq_deli_date();// 提货日期
//			String req_arri_date = parentVO.getReq_arri_date();// 送货日期
//			String pk_delivery = parentVO.getPk_delivery();// 提货方
//			String pk_arrival = parentVO.getPk_arrival();// 收货方
//			String orderno = parentVO.getOrderno();// 提货方
//			String cust_orderno = parentVO.getCust_orderno();// 收货方
//			
//			String key = new StringBuffer().append(req_deli_date).append(req_arri_date).append(pk_customer)
//					.append(pk_arrival).append(pk_delivery).append(pk_trans_type).append(orderno)
//					.append(cust_orderno).toString();
//			int num = childVO.getNum();//数量
//			UFDouble unit_weight = childVO.getWeight();//重量
//			UFDouble unit_volumn = childVO.getUnit_volume();//体积
//			
//			if(StringUtils.isBlank(req_deli_date)) {
//				throw new BusiException("第[?]行的要求提货日期不能为空！",rowNum+"");
//			}
//			
//			if(StringUtils.isBlank(req_arri_date)) {
//				throw new BusiException("第[?]行的要求到货日期不能为空！",rowNum+"");
//			}
//			
//			
//			if(StringUtils.isBlank(pk_customer)) {
//				throw new BusiException("第[?]行的客户编码不能为空！",rowNum+"");
//			}
//			
//			
//			if(StringUtils.isBlank(pk_trans_type)) {
//				throw new BusiException("第[?]行的运输方式不能为空！",rowNum+"");
//			}
//			
//			if( StringUtils.isBlank(pk_delivery)) {
//				throw new BusiException("第[?]行的提货方不能为空！",rowNum+"");
//			}
//			
//			
//			if(StringUtils.isBlank(pk_arrival)) {
//				throw new BusiException("第[?]行的收货方不能为空！",rowNum+"");
//			}
//			
//			
//			if(StringUtils.isBlank(num+"")) {
//				throw new BusiException("第[?]行的件数不能为空！",rowNum+"");
//			}
//			
//			
//			if(StringUtils.isBlank(unit_volumn+"")) {
//				throw new BusiException("第[?]行的重量不能为空！",rowNum+"");
//			}
//			
//			
//			if(StringUtils.isBlank(unit_weight+"")) {
//				throw new BusiException("第[?]行的体积不能为空！",rowNum+"");
//			}
//			
//			// 这里多加一个校验，主要为了提示是第几行的问题
//			//1.提货方
//			String sql = "select * from ts_address where isnull(dr,0)=0 and pk_address=?";
//			List<AddressVO> addrVOs = NWDao.getInstance().queryForList(sql, AddressVO.class, parentVO.getPk_delivery());
//			if(addrVOs == null || addrVOs.size() == 0) {
//				throw new BusiException("根据提货方PK[?]没有匹配到地址记录！",parentVO.getPk_delivery());
//			}
//			//2.收货方
//			sql = "select * from ts_address where isnull(dr,0)=0 and pk_address=?";
//			List<AddressVO> addrVOs1 = NWDao.getInstance().queryForList(sql, AddressVO.class, parentVO.getPk_arrival());
//			if(addrVOs1 == null || addrVOs1.size() == 0) {
//				throw new BusiException("根据收货方PK[?]没有匹配到地址记录！",parentVO.getPk_arrival());
//			}
//			
//			if(StringUtils.isNotBlank(parentVO.getOrderno())) {
//				boolean orderno_must_unique = ParameterHelper.getBooleanParam("orderno_must_unique");
//				if(orderno_must_unique) {
//					// 必须唯一
//					sql = "select count(1) from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and orderno=? and pk_corp=?";
//					if(parentVO.getStatus() == VOStatus.UPDATED) {
//						sql += " and pk_invoice != '" + parentVO.getPk_invoice() + "'";// 排除自身
//					}
//					Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, parentVO.getOrderno(),
//							WebUtils.getLoginInfo().getPk_corp());
//					if(count > 0) {
//						throw new BusiException("第[?]行的订单号[?]已存在！",rowNum+"",parentVO.getOrderno());
//					}
//				}
//			}
//			if(StringUtils.isNotBlank(parentVO.getCust_orderno())) {
//				boolean cust_orderno_must_unique = ParameterHelper.getBooleanParam("cust_orderno_must_unique");
//				if(cust_orderno_must_unique) {
//					// 必须唯一
//					sql = "select count(1) from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0 and cust_orderno=? and pk_corp=?";
//					if(parentVO.getStatus() == VOStatus.UPDATED) {
//						sql += " and pk_invoice != '" + parentVO.getPk_invoice() + "'";// 排除自身
//					}
//					Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, parentVO.getCust_orderno(),
//							WebUtils.getLoginInfo().getPk_corp());
//					if(count > 0) {
//						throw new BusiException("第[?]行的客户订单号[?]已存在！",rowNum+"",parentVO.getCust_orderno());
//					}
//				}
//			}
//			
//			
//			AggregatedValueObject aggVO = null;
//			SuperVO currentVO = null;
//			for(AggregatedValueObject billVO : aggVOs) {
//				InvoiceVO superVO = (InvoiceVO) billVO.getParentVO();
//				//合并发货单
//				String superVOKey = new StringBuffer().append(superVO.getReq_deli_date()).append(superVO.getReq_arri_date())
//						.append(superVO.getPk_customer()).append(superVO.getPk_arrival()).append(superVO.getPk_delivery())
//						.append(superVO.getPk_trans_type()).append(superVO.getOrderno()).append(superVO.getCust_orderno()).toString();
//				if(	superVOKey.equals(key)) {
//					// 找到相同的主表vo
//					aggVO = billVO;
//					currentVO = superVO;
//					break;
//				}
//			}
//			if(aggVO != null) {
//				// 导入的数据中已经包含了单据号，而且单据号已经存在，那么这里只使用其表体的数据
//				// 对导入的这一行数据的表头和表体进行关联处理
//				childVO.setStatus(VOStatus.NEW);
//				NWDao.setUuidPrimaryKey(childVO);
//				childVO.setAttributeValue(getParentPkInChild(), currentVO.getPrimaryKey());
//				CircularlyAccessibleValueObject[] childVOs = null;
//				if(aggVO instanceof IExAggVO) {
//					// 多子表
//					childVOs = ((IExAggVO) aggVO).getTableVO(childVO.getTableName());// table_name务必和tabcode一致
//				} else {
//					childVOs = aggVO.getChildrenVO();
//				}
//				if(childVOs != null && childVOs.length > 0) {
//					CircularlyAccessibleValueObject[] newChildVOs = (CircularlyAccessibleValueObject[]) Array
//							.newInstance(childVOs[0].getClass(), childVOs.length + 1);
//					for(int i = 0; i < childVOs.length; i++) {
//						newChildVOs[i] = childVOs[i];
//					}
//					newChildVOs[newChildVOs.length - 1] = childVO;
//					if(aggVO instanceof IExAggVO) {
//						// 多子表
//						((IExAggVO) aggVO).setTableVO(childVO.getTableName(), newChildVOs);
//					} else {
//						aggVO.setChildrenVO(newChildVOs);
//					}
//				}
//			} else {
//				AggregatedValueObject billVO = getAggVO();
//				parentVO.setVbillstatus(BillStatus.NEW);
//				parentVO.setStatus(VOStatus.NEW);
//				NWDao.setUuidPrimaryKey(parentVO);
//				billVO.setParentVO(parentVO);
//
//				// 对导入的这一行数据的表头和表体进行关联处理
//				childVO.setStatus(VOStatus.NEW);
//				NWDao.setUuidPrimaryKey(childVO);
//				childVO.setAttributeValue(getParentPkInChild(), parentVO.getPrimaryKey());
//				CircularlyAccessibleValueObject[] vos = (CircularlyAccessibleValueObject[]) Array.newInstance(
//						childVO.getClass(), 1);
//				vos[0] = childVO;
//				if(billVO instanceof IExAggVO) {
//					// 多子表
//					((IExAggVO) billVO).setTableVO(childVO.getTableName(), vos);
//				} else {
//					billVO.setChildrenVO(vos);
//				}
//				aggVOs.add(billVO);
//			}
//		} else {
//			//logger.warn("标准导入更新时，第" + rowNum + "行不需要填写发货单号！");
//			//更新处理
//			parentVO.setAttributeValue(VBILLNO, null);
//		}
//	}
	
	protected Object getRealValueByTemplet(String value, BillTempletBVO fieldVO, SuperVO superVO) {
		//为了提高速度，这3个字段我们手工处理
		if(fieldVO.getItemkey().equals("pk_delivery") 
				|| fieldVO.getItemkey().equals("pk_arrival")
				|| fieldVO.getItemkey().equals("pk_customer")) {
			return value;
		}
		return super.getRealValueByTemplet(value, fieldVO, superVO);
	}

//	public void setDefaultValue(SuperVO parentVO, SuperVO childVO, int rowNum) {
//		super.setDefaultValue(parentVO, childVO, rowNum);
//		InvoiceVO invVO = (InvoiceVO) parentVO;
//		InvPackBVO ipBVO = (InvPackBVO) childVO;
//		// 运输方式，默认是公路整车
//		if(StringUtils.isBlank(invVO.getPk_trans_type())) {
//			String sql = "select pk_trans_type from ts_trans_type where isnull(dr,0)=0 and name=? and pk_corp=?";
//			String pk_trans_type = NWDao.getInstance().queryForObject(sql, String.class, TransTypeConst.TT_GLZC,
//					WebUtils.getLoginInfo().getPk_corp());
//			if(StringUtils.isBlank(pk_trans_type)) {
//				pk_trans_type = NWDao.getInstance().queryForObject(sql, String.class, TransTypeConst.TT_GLZC,
//						Constants.SYSTEM_CODE);
//				if(StringUtils.isBlank(pk_trans_type)) {
//					throw new BusiException("基础数据中没有维护名称为[?]的运输方式！",TransTypeConst.TT_GLZC);
//				}
//			}
//			invVO.setPk_trans_type(pk_trans_type);
//		}
//		if(invVO.getBalatype() == null) {
//			invVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());// 结算方式默认月结
//		}
//		if(ipBVO.getNum() == null) {
//			ipBVO.setNum(0);// 件数默认为0
//		}
//		if(invVO.getInsurance_amount() != null){
//			invVO.setIf_insurance(UFBoolean.TRUE);
//		}
//		if(invVO.getInvoice_origin() == null) {
//			invVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.BZDR.intValue());
//		}
//	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolveForMultiTable(file);
		//解析出来的aggVO有一个特点，有vbillno的单子会被组合成一个单子，这些单子我们要用来更新
		//而没有vbillno得单子，我们用来生成发货单，这里要安照部分字段进行分组整合
		Set<String> pk_addressis = new HashSet<String>();
		Set<String> pk_customers = new HashSet<String>();
		Map<String,ExAggInvoiceVO> groupMap = new HashMap<String, ExAggInvoiceVO>();
		for(AggregatedValueObject aggregatedValueObject : aggVOs){
			ExAggInvoiceVO aggInvoiceVO = (ExAggInvoiceVO) aggregatedValueObject;
			InvoiceVO invoiceVO = (InvoiceVO) aggInvoiceVO.getParentVO();
			pk_addressis.add(invoiceVO.getPk_delivery());
			pk_addressis.add(invoiceVO.getPk_arrival());
			pk_customers.add(invoiceVO.getPk_customer());
			if(StringUtils.isNotBlank(invoiceVO.getVbillno())){
				groupMap.put(invoiceVO.getVbillno(), aggInvoiceVO);
				continue;
			}
			//在checker里面严重以下字段都不为空
			String key = new StringBuffer().append(invoiceVO.getReq_deli_date()).append(invoiceVO.getReq_arri_date())
					.append(invoiceVO.getPk_customer()).append(invoiceVO.getPk_arrival()).append(invoiceVO.getPk_delivery())
					.append(invoiceVO.getPk_trans_type()).append(invoiceVO.getOrderno()).append(invoiceVO.getCust_orderno()).toString();
			ExAggInvoiceVO aggVO = groupMap.get(key);
			if(aggVO == null){
				aggVO = aggInvoiceVO;
				groupMap.put(key, aggVO);
			}else{
				//找出原有的各项明细
				InvPackBVO[] oldInvPackBVOs = (InvPackBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
				InvLineBVO[] oldInvLineBVOs = (InvLineBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
				InvReqBVO[] oldInvReqBVOs = (InvReqBVO[]) aggVO.getTableVO(TabcodeConst.TS_INV_REQ_B);
				TransBilityBVO[] oldBilityBVOs = (TransBilityBVO[]) aggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
				
				InvPackBVO[] invPackBVOs = (InvPackBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
				InvLineBVO[] invLineBVOs = (InvLineBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_LINE_B);
				InvReqBVO[] invReqBVOs = (InvReqBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_INV_REQ_B);
				TransBilityBVO[] bilityBVOs = (TransBilityBVO[]) aggInvoiceVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
				
				List<InvPackBVO> mergeInvPackBVOs = new ArrayList<InvPackBVO>();
				if(oldInvPackBVOs != null && oldInvPackBVOs.length > 0){
					mergeInvPackBVOs.addAll(Arrays.asList(oldInvPackBVOs));
				}
				if(invPackBVOs != null && invPackBVOs.length > 0){
					mergeInvPackBVOs.addAll(Arrays.asList(invPackBVOs));
				}
				
				List<InvLineBVO> mergeInvLineBVOs = new ArrayList<InvLineBVO>();
				if(oldInvLineBVOs != null && oldInvLineBVOs.length > 0){
					mergeInvLineBVOs.addAll(Arrays.asList(oldInvLineBVOs));
				}
				if(invLineBVOs != null && invLineBVOs.length > 0){
					mergeInvLineBVOs.addAll(Arrays.asList(invLineBVOs));
				}
				
				List<InvReqBVO> mergeInvReqBVOs = new ArrayList<InvReqBVO>();
				if(oldInvReqBVOs != null && oldInvReqBVOs.length > 0){
					mergeInvReqBVOs.addAll(Arrays.asList(oldInvReqBVOs));
				}
				if(invReqBVOs != null && invReqBVOs.length > 0){
					mergeInvReqBVOs.addAll(Arrays.asList(invReqBVOs));
				}
				
				List<TransBilityBVO> mergeInvTransBVOs = new ArrayList<TransBilityBVO>();
				if(oldBilityBVOs != null && oldBilityBVOs.length > 0){
					mergeInvTransBVOs.addAll(Arrays.asList(oldBilityBVOs));
				}
				if(bilityBVOs != null && bilityBVOs.length > 0){
					mergeInvTransBVOs.addAll(Arrays.asList(bilityBVOs));
				}
				if(mergeInvPackBVOs.size() > 0){
					aggVO.setTableVO(TabcodeConst.TS_INV_PACK_B, mergeInvPackBVOs.toArray(new InvPackBVO[mergeInvPackBVOs.size()]));
				}
				if(mergeInvLineBVOs.size() > 0){
					aggVO.setTableVO(TabcodeConst.TS_INV_LINE_B, mergeInvLineBVOs.toArray(new InvLineBVO[mergeInvLineBVOs.size()]));
				}
				if(mergeInvReqBVOs.size() > 0){
					aggVO.setTableVO(TabcodeConst.TS_INV_REQ_B, mergeInvReqBVOs.toArray(new InvReqBVO[mergeInvReqBVOs.size()]));
				}
				if(mergeInvTransBVOs.size() > 0){
					aggVO.setTableVO(TabcodeConst.TS_TRANS_BILITY_B, mergeInvTransBVOs.toArray(new TransBilityBVO[mergeInvTransBVOs.size()]));
				}
			}
		}
		//获取地址和客户信息
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, 
				"addr_code in " + NWUtils.buildConditionString(pk_addressis));
		if(addressVOs == null || addressVOs.length == 0){
			throw new BusiException("缺少地址信息！");
		}
		String custSql = "SELECT ts_cust_bala.pk_related_cust ,ts_customer.pk_customer,ts_customer.cust_code "
				+ " FROM ts_cust_bala WITH(NOLOCK)  "
				+ " LEFT JOIN ts_customer  WITH(NOLOCK) ON ts_cust_bala.pk_customer = ts_customer.pk_customer "
				+ " WHERE isnull(ts_customer.dr,0)=0  AND isnull(ts_customer.locked_flag,'N')='N' "
				+ " AND isnull(ts_cust_bala.dr,0)=0  AND isnull(ts_cust_bala.locked_flag,'N')='N' AND ts_cust_bala.is_default='Y' "
				+ " AND ts_customer.cust_code IN " + NWUtils.buildConditionString(pk_customers);
		List<Map<String,Object>> cust_maps = NWDao.getInstance().queryForList(custSql);
		if(cust_maps == null || cust_maps.size() == 0){
			throw new BusiException("缺少客户信息！");
		}
//		//检查订单号，客户订单号是否重复
//		boolean orderno_must_unique = ParameterHelper.getBooleanParam("orderno_must_unique");
//		boolean cust_orderno_must_unique = ParameterHelper.getBooleanParam("cust_orderno_must_unique");
//		List<>
//		if(orderno_must_unique || cust_orderno_must_unique){
//			for(String key : groupMap.keySet()){
//				
//			}
//		}
		
		//分组完毕，导入的时候可能会有很多行，但现在只有有很少行了，因为上面合并过了，生成对应的信息
		for(String key : groupMap.keySet()){
			ExAggInvoiceVO aggInvoiceVO = groupMap.get(key);
			InvoiceVO invoiceVO = (InvoiceVO) aggInvoiceVO.getParentVO();
			if(invoiceVO.getBalatype() == null) {
				invoiceVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());// 结算方式默认月结
			}
			if(invoiceVO.getInsurance_amount() != null){
				invoiceVO.setIf_insurance(UFBoolean.TRUE);
			}
			if(invoiceVO.getInvoice_origin() == null) {
				invoiceVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.BZDR.intValue());
			}
			if(invoiceVO.getVbillstatus() == null){
				//不管导入还是更新，状态都是新建
				invoiceVO.setVbillstatus(BillStatus.NEW);
			}
			if(StringUtils.isBlank(invoiceVO.getPk_corp())){
				invoiceVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			}
			if(StringUtils.isBlank(invoiceVO.getCreate_user())){
				invoiceVO.setCreate_user(WebUtils.getLoginInfo().getPk_corp());
			}
			if(invoiceVO.getCreate_time() == null){
				invoiceVO.setCreate_time(new UFDateTime(new Date()));
			}
			//设置地址信息
			boolean deli_success = false;
			boolean arri_success = false;
			for(AddressVO addressVO : addressVOs){
				if(addressVO.getAddr_code().equals(invoiceVO.getPk_delivery())){
					invoiceVO.setPk_delivery(addressVO.getPk_address());
					invoiceVO.setDeli_city(addressVO.getPk_city());
					invoiceVO.setDeli_province(addressVO.getPk_province());
					invoiceVO.setDeli_area(addressVO.getPk_area());
					invoiceVO.setDeli_detail_addr(addressVO.getDetail_addr());
					invoiceVO.setDeli_contact(addressVO.getContact());
					invoiceVO.setDeli_phone(addressVO.getPhone());
					invoiceVO.setDeli_mobile(addressVO.getMobile());
					deli_success = true;
				}
				if(addressVO.getAddr_code().equals(invoiceVO.getPk_arrival())){
					invoiceVO.setPk_arrival(addressVO.getPk_address());
					invoiceVO.setArri_city(addressVO.getPk_city());
					invoiceVO.setArri_province(addressVO.getPk_province());
					invoiceVO.setArri_area(addressVO.getPk_area());
					invoiceVO.setArri_detail_addr(addressVO.getDetail_addr());
					invoiceVO.setArri_contact(addressVO.getContact());
					invoiceVO.setArri_phone(addressVO.getPhone());
					invoiceVO.setArri_mobile(addressVO.getMobile());
					arri_success = true;
				}
				if(deli_success && arri_success){
					break;
				}
			}
			if(!deli_success){
				throw new BusiException("提货地址[?]不存在！",invoiceVO.getPk_delivery());
			}
			if(!arri_success){
				throw new BusiException("收货地址[?]不存在！",invoiceVO.getPk_arrival());
			}
			boolean cust_success = false;
			for(Map<String,Object> cust_map : cust_maps){
				if(cust_map.get("cust_code").equals(invoiceVO.getPk_customer())){
					invoiceVO.setPk_customer(cust_map.get("pk_customer").toString());
					invoiceVO.setBala_customer(cust_map.get("pk_related_cust") == null ? null : cust_map.get("pk_related_cust").toString());
					cust_success = true;
					break;
				}
			}
			if(!cust_success){
				throw new BusiException("客户[?]不存在！",invoiceVO.getPk_customer());
			}
			//检查是更新还是导入
			if(StringUtils.isNotBlank(invoiceVO.getVbillno())){
				InvoiceVO invoiceVO_db = NWDao.getInstance().queryByCondition(InvoiceVO.class, 
						"vbillno=?", invoiceVO.getVbillno());
				if(invoiceVO_db != null){
					if(invoiceVO_db.getVbillstatus() != BillStatus.NEW){
						throw new BusiException("订单[?]不是[新建],不允许修改！",invoiceVO_db.getVbillno());
					}
					invoiceVO.setPk_invoice(invoiceVO_db.getPk_invoice());
					invoiceVO.setStatus(VOStatus.UPDATED);
					invoiceVO.setModify_time(new UFDateTime(new Date()));
					invoiceVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
					invoiceVO.setCreate_user(invoiceVO_db.getCreate_user());
					invoiceVO.setCreate_time(invoiceVO_db.getCreate_time());
					invoiceVO.setTs(invoiceVO_db.getTs());//将原有的时间戳给新的单据，否则无法保存
					invoiceVO.setDr(0);//需要手动设置
					InvPackBVO[] invPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", invoiceVO_db.getPk_invoice());
					InvLineBVO[] invLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvLineBVO.class, "pk_invoice=?", invoiceVO_db.getPk_invoice());
					InvReqBVO[] invReqBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvReqBVO.class, "pk_invoice=?", invoiceVO_db.getPk_invoice());
					TransBilityBVO[] bilityBVO = NWDao.getInstance().queryForSuperVOArrayByCondition(TransBilityBVO.class, "pk_invoice=?", invoiceVO_db.getPk_invoice());
					NWDao.getInstance().delete(invPackBVOs);
					NWDao.getInstance().delete(invLineBVOs);
					NWDao.getInstance().delete(invReqBVOs);
					NWDao.getInstance().delete(bilityBVO);
				}
			}
			service.save(aggInvoiceVO, paramVO);
		}
	}
}
