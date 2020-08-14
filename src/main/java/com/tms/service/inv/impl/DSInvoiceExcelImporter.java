package com.tms.service.inv.impl;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ArrayAnnotationValue;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IBillService;
import org.nw.utils.CorpHelper;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.vo.trade.pub.IExAggVO;

import com.tms.BillStatus;
import com.tms.constants.AddressConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.base.impl.BaseUtils;
import com.tms.service.cm.impl.CMUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;

/**
 * 发货单的集货信息导入
 * 
 * @author xuqc
 * @date 2014-4-9 下午10:17:52
 */
public class DSInvoiceExcelImporter extends BillExcelImporter {
	
	public DSInvoiceExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
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
		if(fieldCode.equals(InvoiceVO.PK_TRANS_TYPE)) {
			RefVO refVO = (RefVO) realValue;// 如果是运输类型编码，此时是一个表体参照，返回的是refVO
			superVO.setAttributeValue(InvoiceVO.PK_TRANS_TYPE, refVO.getPk());
		}

	}

	protected void processBeforeImport(AggregatedValueObject billVO, ParamVO paramVO) {
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		// 德沙导入
		// 一、提货地址+始发地匹配地址库的‘详细地址’与‘城市’得出地址编码填写到提货方；
		// 1、根据始发地从区域档案中读取城市pk
		String sql = "select pk_area from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and name=?";
		List<String> cityAry = NWDao.getInstance().queryForList(sql, String.class, parentVO.getDeli_city());
		if(cityAry == null || cityAry.size() == 0) {
			throw new BusiException("根据始发地[?]没有匹配到城市记录！",parentVO.getDeli_city());
		}
		if(cityAry.size() > 1) {
			throw new BusiException("根据始发地[?]匹配到多条城市记录，基础数据有问题！",parentVO.getDeli_city());
		}
		// 2、匹配到城市了，根据城市和详细地址匹配地址库
		sql = "select * from ts_address WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and addr_type=? and detail_addr=? and pk_city=? and "
				+ CorpHelper.getCurrentCorpWithChildrenAndParent();
		List<AddressVO> addrVOs = NWDao.getInstance().queryForList(sql, AddressVO.class,
				AddressConst.ADDR_TYPE.TYPE1.intValue(), parentVO.getDeli_detail_addr(), cityAry.get(0));
		if(addrVOs == null || addrVOs.size() == 0) {
			throw new BusiException("根据提货地址[?]和始发地[?]没有匹配到地址！",parentVO.getDeli_detail_addr(),parentVO.getDeli_city());
		}
		// 3、匹配到了，将信息填写回发货单
		AddressVO addrVO = addrVOs.get(0);
		parentVO.setPk_delivery(addrVO.getPk_address());
		parentVO.setDeli_city(addrVO.getPk_city());
		parentVO.setDeli_province(addrVO.getPk_province());
		parentVO.setDeli_area(addrVO.getPk_area());
		parentVO.setDeli_contact(addrVO.getContact());
		parentVO.setDeli_phone(addrVO.getPhone());
		parentVO.setDeli_mobile(addrVO.getMobile());
		parentVO.setDeli_email(addrVO.getEmail());

		cityAry.clear();
		// 二、目的城市匹配匹配地址库中的地址编码：记录到收货方
		// 2、匹配到城市了，根据目的城市匹配地址库
		sql = "select * from ts_address WITH(NOLOCK) where isnull(dr,0)=0 and addr_type=? and addr_code=? and isnull(locked_flag,'N')='N' and  "
				+ CorpHelper.getCurrentCorpWithChildrenAndParent();
		List<AddressVO> addrVOs1 = NWDao.getInstance().queryForList(sql, AddressVO.class,
				AddressConst.ADDR_TYPE.TYPE1.intValue(), parentVO.getArri_city());
		if(addrVOs1 == null || addrVOs1.size() == 0) {
			throw new BusiException("根据目的城市[?]没有匹配到地址！",parentVO.getArri_city());
		}
		// 3、匹配到了，将信息填写回发货单
		AddressVO addrVO1 = addrVOs1.get(0);
		parentVO.setPk_arrival(addrVO1.getPk_address());
		parentVO.setArri_city(addrVO1.getPk_city());
		parentVO.setArri_province(addrVO1.getPk_province());
		parentVO.setArri_area(addrVO1.getPk_area());
		parentVO.setArri_contact(addrVO1.getContact());
		parentVO.setArri_phone(addrVO1.getPhone());
		parentVO.setArri_mobile(addrVO1.getMobile());
		parentVO.setArri_email(addrVO1.getEmail());

		// 根据客户匹配默认客户，如果没有
		if(StringUtils.isNotBlank(parentVO.getPk_customer())) {
			sql = "select pk_customer from ts_customer WITH(NOLOCK) where cust_code=? and isnull(locked_flag,'N')='N' and isnull(dr,0)=0 and "
					+ CorpHelper.getCurrentCorpWithChildrenAndParent();
			String pk_customer = NWDao.getInstance().queryForObject(sql, String.class, parentVO.getPk_customer());
			if(StringUtils.isBlank(pk_customer)){
				throw new BusiException("[?]客户不存在！",parentVO.getPk_customer());
			}
			parentVO.setPk_customer(pk_customer);
			
			// 根据客户匹配默认结算客户，如果没有
			if(StringUtils.isNotBlank(pk_customer)) {
				sql = "select pk_related_cust from ts_cust_bala WITH(NOLOCK) where pk_customer=? and isnull(locked_flag,'N')='N' and isnull(dr,0)=0 and is_default='Y' ";
				String pk_cust_bala = NWDao.getInstance().queryForObject(sql, String.class, pk_customer);
				parentVO.setBala_customer(pk_cust_bala);
			}

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

	public void processAfterResolveOneRow(SuperVO parentVO1, SuperVO childVO1, List<AggregatedValueObject> aggVOs,
			int rowNum) {
		// 大连陆川和长春陆川的德沙导入业务
		// 通过‘送货日期、发货人、提货地址、始发地、目的城市’都相同的判断这是一票独立的委托单
		Object oVbillno = parentVO1.getAttributeValue(VBILLNO);
		if(oVbillno == null) {
			// 如果不为空的情况在父类已经做了处理
			InvoiceVO parentVO = (InvoiceVO) parentVO1;
			InvPackBVO childVO = (InvPackBVO) childVO1;
			String req_deli_date = parentVO.getReq_deli_date();// 提货日期
			String req_arri_date = parentVO.getReq_arri_date();// 送货日期
			String pk_customer = parentVO.getPk_customer();// 发货人
			String deli_detail_addr = parentVO.getDeli_detail_addr();// 提货地址
			String deli_city = parentVO.getDeli_city();// 始发地
			String arri_city = parentVO.getArri_city();// 目的城市
			//yaojiie 2015 11 30 添加导入时运输方式判断。
			String pk_trans_type = parentVO.getPk_trans_type();
			
			if(StringUtils.isBlank(req_deli_date) || StringUtils.isBlank(req_arri_date)
					|| StringUtils.isBlank(pk_customer) || StringUtils.isBlank(deli_detail_addr)
					|| StringUtils.isBlank(deli_city) || StringUtils.isBlank(arri_city)) {
				throw new BusiException("第[?]行的预计提货日期、送货日期、发货人、提货地址、始发地、目的城市等关键字段都不能为空！",rowNum+"");
			}

			// 这里多加一个校验，主要为了提示是第几行的问题
			String sql = "select pk_area from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and name=?";
			List<String> cityAry = NWDao.getInstance().queryForList(sql, String.class, parentVO.getDeli_city());
			if(cityAry == null || cityAry.size() == 0) {
				throw new BusiException("根据第[?]行始发地[?]没有匹配到城市记录！",rowNum+"",parentVO.getDeli_city());
			}
			if(cityAry.size() > 1) {
				throw new BusiException("根据第[?]行始发地[?]匹配到多条城市记录，基础数据有问题！",rowNum+"",parentVO.getDeli_city());
			}
			// 2、匹配到城市了，根据城市和详细地址匹配地址库
			sql = "select * from ts_address WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and addr_type=? and detail_addr=? and pk_city=? and "
					+ CorpHelper.getCurrentCorpWithChildrenAndParent();
			List<AddressVO> addrVOs = NWDao.getInstance().queryForList(sql, AddressVO.class,
					AddressConst.ADDR_TYPE.TYPE1.intValue(), parentVO.getDeli_detail_addr(), cityAry.get(0));
			if(addrVOs == null || addrVOs.size() == 0) {
				throw new BusiException("根据第[?]行提货地址[?]和始发地[?]没有匹配到地址！",rowNum+"",parentVO.getDeli_detail_addr(),parentVO.getDeli_city());
			}

			AggregatedValueObject aggVO = null;
			SuperVO currentVO = null;
			for(AggregatedValueObject billVO : aggVOs) {
				InvoiceVO superVO = (InvoiceVO) billVO.getParentVO();
				// yaojiie 2015 12 03 增加系统定义getMergeRule，自定义导入时
				// 是否进行合并默认合并
				if(!ParameterHelper.getMergeRule()){
					break;
				}else if(req_deli_date.equals(superVO.getReq_deli_date()) && req_arri_date.equals(superVO.getReq_arri_date())
						&& pk_customer.equals(superVO.getPk_customer())
						&& deli_detail_addr.equals(superVO.getDeli_detail_addr())
						&& pk_trans_type.equals(superVO.getPk_trans_type())
						&& deli_city.equals(superVO.getDeli_city()) && arri_city.equals(superVO.getArri_city())) {
					// 找到相同的主表vo
					aggVO = billVO;
					currentVO = superVO;
					break;
				}
			}
			if(aggVO != null) {
				// 导入的数据中已经包含了单据号，而且单据号已经存在，那么这里只使用其表体的数据
				// 对导入的这一行数据的表头和表体进行关联处理
				childVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(childVO);
				childVO.setAttributeValue(getParentPkInChild(), currentVO.getPrimaryKey());
				CircularlyAccessibleValueObject[] childVOs = null;
				if(aggVO instanceof IExAggVO) {
					// 多子表
					childVOs = ((IExAggVO) aggVO).getTableVO(childVO.getTableName());// table_name务必和tabcode一致
				} else {
					childVOs = aggVO.getChildrenVO();
				}
				if(childVOs != null && childVOs.length > 0) {
					CircularlyAccessibleValueObject[] newChildVOs = (CircularlyAccessibleValueObject[]) Array
							.newInstance(childVOs[0].getClass(), childVOs.length + 1);
					for(int i = 0; i < childVOs.length; i++) {
						newChildVOs[i] = childVOs[i];
					}
					newChildVOs[newChildVOs.length - 1] = childVO;
					if(aggVO instanceof IExAggVO) {
						// 多子表
						((IExAggVO) aggVO).setTableVO(childVO.getTableName(), newChildVOs);
					} else {
						aggVO.setChildrenVO(newChildVOs);
					}
				}
			} else {
				AggregatedValueObject billVO = getAggVO();
				parentVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(parentVO);
				billVO.setParentVO(parentVO);

				// 对导入的这一行数据的表头和表体进行关联处理
				childVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(childVO);
				childVO.setAttributeValue(getParentPkInChild(), parentVO.getPrimaryKey());
				CircularlyAccessibleValueObject[] vos = (CircularlyAccessibleValueObject[]) Array.newInstance(
						childVO.getClass(), 1);
				vos[0] = childVO;
				if(billVO instanceof IExAggVO) {
					// 多子表
					((IExAggVO) billVO).setTableVO(childVO.getTableName(), vos);
				} else {
					billVO.setChildrenVO(vos);
				}
				aggVOs.add(billVO);
			}
		} else {
			logger.warn("德沙更新时，第" + rowNum + "行不需要填写发货单号！");
			parentVO1.setAttributeValue(VBILLNO, null);
			// throw new BusiException("德沙更新时，第" + rowNum + "行不需要填写发货单号！");
		}

	}

	protected Object getRealValueByTemplet(String value, BillTempletBVO fieldVO, SuperVO superVO) {
		// 德沙导入业务
		// 提货地址+始发地 匹配地址库的‘详细地址’与‘城市’得出地址编码填写到提货方；目的城市匹配地址库，得到地址编码：记录到收货方
		if(fieldVO.getItemkey().equals("deli_detail_addr") || fieldVO.getItemkey().equals("deli_city")
				|| fieldVO.getItemkey().equals("arri_city")) {
			// 提货地址+始发地,目的城市,这里先不要根据模板去匹配。而是在弄完这一行以后再进行匹配
			return value;
		}
		return super.getRealValueByTemplet(value, fieldVO, superVO);
	}

	public void setDefaultValue(SuperVO parentVO, SuperVO childVO, int rowNum) {
		super.setDefaultValue(parentVO, childVO, rowNum);
		InvoiceVO invVO = (InvoiceVO) parentVO;
		InvPackBVO ipBVO = (InvPackBVO) childVO;
		if(invVO.getBalatype() == null) {
			invVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());// 结算方式默认月结
		}
		if(ipBVO.getNum() == null) {
			ipBVO.setNum(0);// 件数默认为0
		}
		if(invVO.getInvoice_origin() == null) {
			invVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.DSDR.intValue());
		}
	}
// yaojiie 2015 12 03 集货导入的导入方法
	public void _import(File file) throws Exception {
		//yaojiie 2015 12 15 添加导入时，对应收明细里总费用的相关计算。
		InvoiceServiceImpl service1 = (InvoiceServiceImpl) service;
		List<AggregatedValueObject> aggVOs = resolve(file);
		//系统设置的规则：是否匹配路线，默认不匹配路线
		boolean autoMatchLine = ParameterHelper.getMatchLineRule();
		//获取登录用户当前公司和集团的所有可用路线的节点地址集合。
		List<Map<String, List<AddressVO>>> linenodes = BaseUtils.getLine();
		if(aggVOs != null && aggVOs.size() > 0) {
			for(AggregatedValueObject billVO : aggVOs) {
				processBeforeImport(billVO, paramVO);
				InvoiceVO invoiceVO = (InvoiceVO) billVO.getParentVO();
				//yaojiie 2015 12 27 增加发货单状态
				invoiceVO.setVbillstatus(BillStatus.NEW);
				//当autoMatchLine不为true时，不进行分段，直接将billVO存入数据库
				if(!autoMatchLine){
					paramVO.getAttr().put("updateAddr", false);// 不需要更新地址
					service1.saveforimport(billVO, paramVO);// 这里调用发货单公共的逻辑
					continue;
				}	
				//当进行分段时
				invoiceVO.setPrimaryKey(null);
				ExAggInvoiceVO aggVO1 = (ExAggInvoiceVO) billVO;
				InvPackBVO[] iPBVOs = (InvPackBVO[]) aggVO1.getTableVO(TabcodeConst.TS_INV_PACK_B);
				//调用分段逻辑
				List<InvoiceVO> invoiceVOs = service1.autoDistInvoice(invoiceVO,linenodes);
				if(invoiceVOs!=null&&invoiceVOs.size()>0){
					for(InvoiceVO invVO:invoiceVOs){
						//yaojiie 2015 12 27 增加发货单状态
						invVO.setVbillstatus(BillStatus.NEW);
						billVO.setParentVO(invVO);
						for(InvPackBVO iPBVO: iPBVOs){
							iPBVO.setPrimaryKey(null);
							iPBVO.setAttributeValue(getParentPkInChild(), null);
						}
						paramVO.getAttr().put("updateAddr", false);// 不需要更新地址
						service1.saveforimport(billVO, paramVO);// 这里调用发货单公共的逻辑
					}
					
				}
			}
			logBuf.append("共导入" + importNum + "记录");
			logger.info(logBuf.toString());
		}
	}
}
