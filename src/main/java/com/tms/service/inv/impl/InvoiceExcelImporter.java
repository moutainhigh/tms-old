package com.tms.service.inv.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.constants.DataDictConst;
import com.tms.constants.TransTypeConst;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;

/**
 * 发货单标准导入类
 * 
 * @author xuqc
 * @date 2014-4-9 下午10:17:52
 */
public class InvoiceExcelImporter extends BillExcelImporter {

	public InvoiceExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
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

	protected void processBeforeImport(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeImport(billVO, paramVO);
		// 统计总件数、重量、体积,体积重，计费重
		InvoiceUtils.setHeaderCount(billVO, paramVO);
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
		if(invVO.getInsurance_amount() != null){
			invVO.setIf_insurance(UFBoolean.TRUE);
		}
		if(invVO.getBalatype() == null) {
			invVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());// 结算方式默认月结
		}
		if(ipBVO.getNum() == null) {
			ipBVO.setNum(0);// 件数默认为0
		}
		if(invVO.getInvoice_origin() == null) {
			invVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.BZDR.intValue());
		}
	}
}
