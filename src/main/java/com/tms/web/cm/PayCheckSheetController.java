package com.tms.web.cm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.service.sys.DataDictService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.BillStatus;
import com.tms.service.cm.PayCheckSheetService;
import com.tms.vo.cm.PayCheckSheetVO;

/**
 * 应付对账
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
@Controller
@RequestMapping(value = "/cm/pcs")
public class PayCheckSheetController extends AbsBillController {

	public static final String PCS_UNCONFIRM_TYPE_LIST = "pcs_unconfirm_type_list";// 数据字典中定义应收明细反确认类型

	@Autowired
	private DataDictService dataDictService;
	
	@Autowired
	private PayCheckSheetService payCheckSheetService;

	public PayCheckSheetService getService() {
		return payCheckSheetService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		PayCheckSheetVO parentVO = (PayCheckSheetVO) billVO.getParentVO();
		if(parentVO.getVbillstatus() != BillStatus.NEW) {
			// 只有新建状态的对账单才可以修改
			throw new BusiException("只有[新建]状态的对账单才可以修改！");
		}
	}

	/**
	 * 付款
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/payable.json")
	@ResponseBody
	public Map<String, Object> payable(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.HEADER);
		Map<String, Object> retMap = this.getService().doPayable(paramVO, json);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.add(retMap);
		return this.genAjaxResponse(true, null, retList);
	}
	
	
	/**
	 * 付款开票 yaojiie 2015 12 30
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value = "/payCheckSheetInvoice.json")
	@ResponseBody
	public Map<String, Object> payCheckSheetInvoice(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.HEADER);
		this.getService().payCheckSheetInvoice(paramVO, json);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 加载付款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadPayCheckSheetRecord.json")
	@ResponseBody
	public Map<String, Object> loadPayCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) {
		String pk_pay_check_sheet = request.getParameter("pk_pay_check_sheet");
		if(StringUtils.isBlank(pk_pay_check_sheet)) {
			throw new BusiException("请先选择一行应付对账记录！");
		}
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		PaginationVO paginationVO = this.getService().loadPayCheckSheetRecord(pk_pay_check_sheet, paramVO, offset,
				pageSize);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 导出付款记录
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/exportPayCheckSheetRecord.do")
	public void exportReceCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		ParamVO paramVO = this.getParamVO(request);
		HSSFWorkbook workbook = null;
		workbook = this.getService().exportPayCheckSheetRecord(paramVO, Constants.DEFAULT_OFFSET_WITH_NOPAGING,
				Constants.DEFAULT_PAGESIZE_WITH_NOPAGING);
		OutputStream os = response.getOutputStream();
		try {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment;filename=" + filename + ".xls");
			workbook.write(os);
		} catch(Exception e) {
			logger.error("导出excel出错！", e);
		} finally {
			os.flush();
			os.close();
		}
	}

	/**
	 * 删除付款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deletePayCheckSheetRecord.json")
	@ResponseBody
	public Map<String, Object> deletePayCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_pay_check_sheet_record = request.getParameter("pk_pay_check_sheet_record");
		Map<String, Object> retMap = this.getService().deletePayCheckSheetRecord(paramVO, pk_pay_check_sheet_record);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.add(retMap);
		return genAjaxResponse(true, null, retList);
	}
	
	/**
	 * 获取反确认类型列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getUnConfirmTypeList.json")
	@ResponseBody
	public Map<String, Object> getUnConfirmTypeList(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(PCS_UNCONFIRM_TYPE_LIST);
		Map<String, Object> recordsMap = new HashMap<String, Object>();
		if(billVO != null) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null) {
				for(CircularlyAccessibleValueObject cvo : cvos) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(Constants.TEXT, cvo.getAttributeValue(DataDictBVO.DISPLAY_NAME));
					map.put(Constants.VALUE, cvo.getAttributeValue(DataDictBVO.VALUE));
					list.add(map);
				}
				recordsMap.put("records", list);
			}
		}
		return recordsMap;
	}
	
	@RequestMapping(value = "/getPayCheckSheetCheckType.json")
	@ResponseBody
	public Map<String, Object> getPayCheckSheetCheckType(HttpServletRequest request, HttpServletResponse response) {
		String checkType = request.getParameter("check_type");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(checkType)) {
			return retMap;
		}
		retMap.put("checkType", this.getService().getPayCheckSheetCheckType(checkType));
		return retMap;
	}
	
	@RequestMapping(value = "/getPayCheckSheetTaxtCat.json")
	@ResponseBody
	public Map<String, Object> getPayCheckSheetTaxtCat(HttpServletRequest request, HttpServletResponse response) {
		String taxtCat = request.getParameter("check_tax_cat");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(taxtCat)) {
			return retMap;
		}
		retMap.put("taxtCat", this.getService().getPayCheckSheetTaxtCat(taxtCat));
		return retMap;
	}
	
	@RequestMapping(value = "/getPayCheckSheetCheckTaxtRate.json")
	@ResponseBody
	public Map<String, Object> getPayCheckSheetCheckTaxtRate(HttpServletRequest request, HttpServletResponse response) {
		String taxtRate = request.getParameter("check_tax_rate");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(taxtRate)) {
			return retMap;
		}
		retMap.put("taxtRate", this.getService().getPayCheckSheetCheckTaxtRate(taxtRate));
		return retMap;
	}
	
	@RequestMapping(value = "/getPayCheckSheetCheckHead.json")
	@ResponseBody
	public Map<String, Object> getPayCheckSheetCheckHead(HttpServletRequest request, HttpServletResponse response) {
		String vbillno = request.getParameter("vbillno");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(vbillno)) {
			return retMap;
		}
		retMap.put("checkHead", this.getService().getPayCheckSheetCheckHead(vbillno));
		return retMap;
	}
	
}
