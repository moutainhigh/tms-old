package com.nw.test;

import java.util.Map;

import org.nw.vo.ParamVO;

import com.tms.service.cm.ReceCheckSheetService;

public class ReceCheckSheetServiceTest extends BaseTestCase {

	private ReceCheckSheetService receCheckSheetService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		receCheckSheetService = (ReceCheckSheetService) appContext.getBean("receCheckSheetServiceImpl");
	}

	/**
	 * 测试事务是否回滚
	 */
	@SuppressWarnings("unchecked")
	public void testConfirm() {
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode("t405");
		paramVO.setTemplateID("0001A4100000000012LV");
		paramVO.setBillId("7fcb618dea124ec5be9226d125e3bb06");
		Map<String, Object> map = receCheckSheetService.confirm(paramVO);
		Map<String, Object> header = (Map<String, Object>) map.get("HEADER");
		System.out.println("状态：" + header.get("vbillstatus"));
	}

	public void testUnconfirm() {
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode("t405");
		paramVO.setTemplateID("0001A4100000000012LV");
		paramVO.setBillId("7fcb618dea124ec5be9226d125e3bb06");
		// receCheckSheetService.unconfirm(paramVO);
	}
}
