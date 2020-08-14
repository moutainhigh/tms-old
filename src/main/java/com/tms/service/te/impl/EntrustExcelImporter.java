package com.tms.service.te.impl;

import org.nw.exp.BillExcelImporter;
import org.nw.exp.ExcelImporter;
import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.vo.te.EntTransbilityBVO;

/**
 * 异常跟踪导入
 * 
 * @author xuqc
 * @date 2013-11-26 下午04:14:16
 */
public class EntrustExcelImporter extends BillExcelImporter {

	public EntrustExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	protected SuperVO getParentVO() {
		return new EntTransbilityBVO();
	}
}
