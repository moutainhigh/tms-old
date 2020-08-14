package com.tms.web.cm;

import java.io.IOException;
import java.io.OutputStream;
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
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.service.sys.DataDictService;
import org.nw.utils.NWUtils;
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
import com.tms.service.cm.ReceCheckSheetService;
import com.tms.vo.cm.ReceCheckSheetVO;

/**
 * 应收对账
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
@Controller
@RequestMapping(value = "/cm/rcs")
public class ReceCheckSheetController extends AbsBillController {
	public static final String RCS_UNCONFIRM_TYPE_LIST = "rcs_unconfirm_type_list";// 数据字典中定义应收明细反确认类型

	@Autowired
	private DataDictService dataDictService;
	
	@Autowired
	private ReceCheckSheetService receCheckSheetService;

	public ReceCheckSheetService getService() {
		return receCheckSheetService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ReceCheckSheetVO parentVO = (ReceCheckSheetVO) billVO.getParentVO();
		if(parentVO.getVbillstatus() != BillStatus.NEW) {
			// 只有新建状态的对账单才可以修改
			throw new BusiException("只有[新建]状态的对账单才可以修改！");
		}
	}

	/**
	 * 收款
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/receivable.json")
	@ResponseBody
	public Map<String, Object> receivable(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.HEADER);
		Map<String, Object> retMap = this.getService().doReceivable(paramVO, json);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.add(retMap);
		return this.genAjaxResponse(true, null, retList);
	}
	
	@RequestMapping(value = "/receCheckSheetInvoice.json")
	@ResponseBody
	public Map<String, Object> receCheckSheetInvoice(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.HEADER);
		this.getService().receCheckSheetInvoice(paramVO, json);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 加载收款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadReceCheckSheetRecord.json")
	@ResponseBody
	public Map<String, Object> loadReceCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) {
		String pk_rece_check_sheet = request.getParameter("pk_rece_check_sheet");
		if(StringUtils.isBlank(pk_rece_check_sheet)) {
			throw new BusiException("请先选择一行应收对账记录！");
		}
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		PaginationVO paginationVO = this.getService().loadReceCheckSheetRecord(pk_rece_check_sheet, paramVO, offset,
				pageSize);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 导出收款记录
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/exportReceCheckSheetRecord.do")
	public void exportReceCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		ParamVO paramVO = this.getParamVO(request);
		HSSFWorkbook workbook = null;
		workbook = this.getService().exportReceCheckSheetRecord(paramVO, Constants.DEFAULT_OFFSET_WITH_NOPAGING,
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
	 * 删除收款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deleteReceCheckSheetRecord.json")
	@ResponseBody
	public Map<String, Object> deleteReceCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_rece_check_sheet_record = request.getParameter("pk_rece_check_sheet_record");
		Map<String, Object> retMap = this.getService().deleteReceCheckSheetRecord(paramVO, pk_rece_check_sheet_record);
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
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(RCS_UNCONFIRM_TYPE_LIST);
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
	
	// yaojie 2015-11-10 添加集货应收明细导出功能
	@RequestMapping(value = "/exportPickupReceCheckSheetSheet.do")
	public void exportPickupReceCheckSheetRecord(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		ParamVO paramVO = this.getParamVO(request);
		
		//此处取到前台选中的所有key的值
		String[] rkeys = request.getParameterValues("key");
		//将获取的pk_rece_check_sheet 转化成 pk_receive_detail，调用应收明细的导出功能。
		String cond = NWUtils.buildConditionString(rkeys);
		String sql = "select pk_receive_detail from ts_rece_check_sheet_b where isnull(dr,0)=0 and pk_rece_check_sheet in "+cond;
		List<String> keys = NWDao.getInstance().queryForList(sql, String.class);
		HSSFWorkbook workbook = null;
		workbook = this.getService().exportPickupReceCheckSheetRecord(paramVO,keys.toArray(new String[rkeys.length]),"正常");
		
		//如果没有创建workBook就不导出
		if(workbook == null){
			return;
		}
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
	
	@RequestMapping(value = "/getReceCheckSheetCheckType.json")
	@ResponseBody
	public Map<String, Object> getReceCheckSheetCheckType(HttpServletRequest request, HttpServletResponse response) {
		String checkType = request.getParameter("check_type");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(checkType)) {
			return retMap;
		}
		retMap.put("checkType", this.getService().getReceCheckSheetCheckType(checkType));
		return retMap;
	}
	
	
	@RequestMapping(value = "/getReceCheckSheetCheckCorp.json")
	@ResponseBody
	public Map<String, Object> getReceCheckSheetCheckCorp(HttpServletRequest request, HttpServletResponse response) {
		String checkCorp = request.getParameter("check_corp");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(checkCorp)) {
			return retMap;
		}
		retMap.put("checkCorp", this.getService().getReceCheckSheetCheckCorp(checkCorp));
		return retMap;
	}
	
	@RequestMapping(value = "/getReceCheckSheetTaxtCat.json")
	@ResponseBody
	public Map<String, Object> getReceCheckSheetTaxtCat(HttpServletRequest request, HttpServletResponse response) {
		String taxtCat = request.getParameter("check_tax_cat");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(taxtCat)) {
			return retMap;
		}
		retMap.put("taxtCat", this.getService().getReceCheckSheetTaxtCat(taxtCat));
		return retMap;
	}
	
	@RequestMapping(value = "/getReceCheckSheetCheckTaxtRate.json")
	@ResponseBody
	public Map<String, Object> getReceCheckSheetCheckTaxtRate(HttpServletRequest request, HttpServletResponse response) {
		String taxtRate = request.getParameter("check_tax_rate");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(taxtRate)) {
			return retMap;
		}
		retMap.put("taxtRate", this.getService().getReceCheckSheetCheckTaxtRate(taxtRate));
		return retMap;
	}
	
	
	@RequestMapping(value = "/getReceCheckSheetCheckHead.json")
	@ResponseBody
	public Map<String, Object> getReceCheckSheetCheckHead(HttpServletRequest request, HttpServletResponse response) {
		String vbillno = request.getParameter("vbillno");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(vbillno)) {
			return retMap;
		}
		retMap.put("checkHead", this.getService().getReceCheckSheetCheckHead(vbillno));
		return retMap;
	}
	
	
}
