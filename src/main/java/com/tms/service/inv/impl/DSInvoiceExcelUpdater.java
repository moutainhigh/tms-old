package com.tms.service.inv.impl;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.BillStatus;
import com.tms.constants.DataDictConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TransTypeConst;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.inv.TransBilityBVO;

/**
 * 发货单集货信息更新
 * 
 * @author xuqc
 * @date 2014-4-9 下午10:17:52
 */
public class DSInvoiceExcelUpdater extends BillExcelImporter {

	public DSInvoiceExcelUpdater(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
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

	public void processAfterResolveOneRow(SuperVO parentVO1, SuperVO childVO1, List<AggregatedValueObject> aggVOs,
			int rowNum) {
		// super.processAfterResolveOneRow(parentVO1, childVO1, aggVOs, rowNum);

		// 大连陆川和长春陆川的德沙更新业务
		Object oVbillno = parentVO1.getAttributeValue(VBILLNO);
		if(oVbillno == null) {
			throw new BusiException("第[?]行的发货单不能为空！",rowNum+"");
		}
		InvoiceVO parentVO = (InvoiceVO) parentVO1;
		InvPackBVO childVO = (InvPackBVO) childVO1;

		// 先从aggVOs中找到具体的某个billVO
		ExAggInvoiceVO currAggVO = null;
		for(AggregatedValueObject aggVO : aggVOs) {
			InvoiceVO newVO = (InvoiceVO) aggVO.getParentVO();
			if(parentVO.getVbillno().equals(newVO.getVbillno())) {
				currAggVO = (ExAggInvoiceVO) aggVO;
				break;
			}
		}
		if(currAggVO == null) {
			InvoiceVO invVO = (InvoiceVO) this.service.getByCode(oVbillno.toString());
			if(invVO == null) {
				throw new BusiException("第[?]行的发货单号已经不存在！",rowNum+"");
			}
			if(invVO.getVbillstatus().intValue() != BillStatus.NEW
					&& invVO.getVbillstatus().intValue() != BillStatus.INV_CONFIRM
					&& invVO.getVbillstatus().intValue() != BillStatus.INV_DELIVERY) {
				throw new BusiException("第[?]行的发货单不是[新增、已确认、已提货]状态，不能进行更新！",rowNum+"");
			}
			paramVO.setBillId(invVO.getPk_invoice());
			currAggVO = (ExAggInvoiceVO) this.service.queryBillVO(paramVO);
			aggVOs.add(currAggVO);
		}

		InvoiceVO invVO = (InvoiceVO) currAggVO.getParentVO();
		// 3、再次导入时发货单号若在系统不存在提示异常；发货人、提货地址、始发地、目的城市若被修改提示异常，不允许导入。

		if(invVO.getPk_customer() == null || !invVO.getPk_customer().equals(parentVO.getPk_customer())) {
			throw new BusiException("不能修改第[?]行的发货人！",rowNum+"");
		} else if(invVO.getDeli_detail_addr() == null
				|| !invVO.getDeli_detail_addr().equals(parentVO.getDeli_detail_addr())) {
			throw new BusiException("不能修改第[?]行的提货地址！",rowNum+"");
		} else if(invVO.getDeli_city() == null || !invVO.getDeli_city().equals(parentVO.getDeli_city())) {
			throw new BusiException("不能修改第[?]行的始发地！",rowNum+"");
		}
		// XXX 这里先不提示,因为目的城市的匹配需要跟导入时候的匹配方式一致，稍微比较麻烦
		// else if(invVO.getArri_city() == null ||
		// !invVO.getArri_city().equals(parentVO.getArri_city())) {
		// throw new BusiException("不能修改第" + rowNum + "行的目的城市！");
		// }

		// 操作人员可修订‘波次号、预计提货日期、送货日期、送货单号、筹措员、零件号（需校验是否系统存在的货品号）、计划托盘件数、计划零件数、实际托盘数、实际零件数、重量、体积、备注'
		invVO.setOrderno(parentVO.getOrderno());
		invVO.setReq_deli_date(parentVO.getReq_deli_date());
		invVO.setReq_arri_date(parentVO.getReq_arri_date());
		invVO.setCust_orderno(parentVO.getCust_orderno());
		invVO.setPk_psndoc(parentVO.getPk_psndoc());
		invVO.setMemo(parentVO.getMemo());
		invVO.setStatus(VOStatus.UPDATED);

		CircularlyAccessibleValueObject[] childVOs = currAggVO.getTableVO(childVO.getTableName());
		for(CircularlyAccessibleValueObject cvo : childVOs) {
			if(cvo.getStatus() != VOStatus.NEW) {
				cvo.setStatus(VOStatus.DELETED);// 删除原有的子表
			}
		}
		// 对导入的这一行数据的表头和表体进行关联处理
		childVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(childVO);
		childVO.setAttributeValue(getParentPkInChild(), parentVO.getPrimaryKey());
		CircularlyAccessibleValueObject[] newChildVOs = (CircularlyAccessibleValueObject[]) Array.newInstance(
				childVO.getClass(), childVOs.length + 1);
		if(childVOs != null && childVOs.length > 0) {
			for(int i = 0; i < childVOs.length; i++) {
				newChildVOs[i] = childVOs[i];
			}
		}
		newChildVOs[newChildVOs.length - 1] = childVO;
		// 多子表
		currAggVO.setTableVO(childVO.getTableName(), newChildVOs);
	}

	public void _import(File file) throws Exception {
		InvoiceServiceImpl service1 = (InvoiceServiceImpl) service;
		List<AggregatedValueObject> aggVOs = resolve(file);
		if(aggVOs != null && aggVOs.size() > 0) {
			for(AggregatedValueObject billVO : aggVOs) {
				processBeforeImport(billVO, paramVO);
				execFormula(billVO);
				paramVO.getAttr().put("updateAddr", false);// 不需要更新地址
				service1.save(billVO, paramVO);// 这里调用发货单公共的逻辑
				processAfterImport(billVO, paramVO);
			}
			logBuf.append("共导入" + importNum + "记录");
			logger.info(logBuf.toString());
		}
	}

	public void setDefaultValue(SuperVO parentVO, SuperVO childVO, int rowNum) {
		super.setDefaultValue(parentVO, childVO, rowNum);
		InvoiceVO invVO = (InvoiceVO) parentVO;
		InvPackBVO ipBVO = (InvPackBVO) childVO;
		// 运输方式，默认是公路整车
		if(StringUtils.isBlank(invVO.getPk_trans_type())) {
			String sql = "select pk_trans_type from ts_trans_type where isnull(dr,0)=0 and name=?";
			String pk_trans_type = NWDao.getInstance().queryForObject(sql, String.class, TransTypeConst.TT_GLZC);
			if(StringUtils.isBlank(pk_trans_type)) {
				throw new BusiException("基础数据中没有维护名称为[?]的运输方式！",TransTypeConst.TT_GLZC);
			}
			invVO.setPk_trans_type(pk_trans_type);
		}
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

	protected void processBeforeImport(AggregatedValueObject billVO, ParamVO paramVO) {
		// super.processBeforeImport(billVO, paramVO);
		// 统计总件数、重量、体积
		ExAggInvoiceVO aggVO = (ExAggInvoiceVO) billVO;
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		// 计算体积重，计费重
		InvoiceUtils.setHeaderCount(billVO, paramVO);

		// 计算表体金额，表头总金额
		ReceDetailBVO[] detailBVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		TransBilityBVO[] tbBVOs = (TransBilityBVO[]) aggVO.getTableVO(TabcodeConst.TS_TRANS_BILITY_B);
		InvoiceUtils.setBodyDetailAmount(parentVO, detailBVOs, tbBVOs);
		InvoiceUtils.setHeaderCostAmount(parentVO, detailBVOs);
	}

	protected void processAfterImport(AggregatedValueObject billVO, ParamVO paramVO) {
		// super.processAfterImport(billVO, paramVO);
		// 同步更新应收明细，这个操作会在service.processBeforeSave中操作
		// 同步更新运段，同时会调用更新委托单、应付明细、分摊费用
		// 在processAfterSave中调用了
		// InvoiceUtils.syncSegmentUpdater(billVO, paramVO);
	}

}
