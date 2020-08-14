package org.nw.web.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.jf.vo.BillTempletVO;
import org.nw.service.sys.BillTempletService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 模板初始化分配
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/bt")
public class BillTempletController extends AbsToftController {

	@Autowired
	private BillTempletService templetService;

	public BillTempletService getService() {
		return templetService;
	}

	/**
	 * 不需要加载模板等操作，直接跳转到jsp
	 */
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = getParamVO(request);
		return new ModelAndView(getFunHelpName(paramVO.getFunCode()));
	}

	/**
	 * 返回可选的用户表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadUserTable.json")
	@ResponseBody
	public Map<String, Object> loadUserTable(HttpServletRequest request, HttpServletResponse response) {
		String keyword = request.getParameter("keyword");
		List<String> tableNameAry = templetService.selectUserTable(keyword);
		List<Map<String, Object>> results = convertObjectToMap(tableNameAry);
		Map<String, Object> finalMap = new HashMap<String, Object>();
		finalMap.put("records", results);
		return finalMap;
	}

	/**
	 * 根据单据类型编码返回所有的模板数据
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadTempletData.json")
	@ResponseBody
	public Map<String, Object> loadTempletData(HttpServletRequest request, HttpServletResponse response) {
		String pk_billtypecode = request.getParameter("pk_billtypecode");
		if(StringUtils.isBlank(pk_billtypecode)) {
			throw new BusiException("单据模板类型参数不能为空！");
		}
		return this.genAjaxResponse(true, null, this.getService().loadTempletData(pk_billtypecode));
	}

	private List<Map<String, Object>> convertObjectToMap(List<String> tableNameAry) {
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(tableNameAry.size());
		for(int i = 0; i < tableNameAry.size(); i++) {
			String tableName = tableNameAry.get(i);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.TEXT, tableName);
			map.put(Constants.VALUE, tableName);
			results.add(map);
		}
		return results;
	}

	/**
	 * 根据表名称查询所有字段
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/loadTableFields.json")
	@ResponseBody
	public Map<String, Object> loadTableFields(HttpServletRequest request, HttpServletResponse response) {
		String tableName = request.getParameter("tableName");
		List<HashMap> fieldAry = this.getService().loadTableFields(tableName);
		List<String> fields = new ArrayList<String>();
		Map<String, Integer> fieldLengthMap = new HashMap<String, Integer>();
		for(Map map : fieldAry) {
			String itemkey = map.get("name").toString();
			fields.add(itemkey);
			fieldLengthMap.put(tableName + "_" + itemkey, Integer.parseInt(map.get("length").toString()));
		}
		List<Map<String, Object>> results = convertObjectToMap(fields);
		return this.genAjaxResponse(true, null, results, fieldLengthMap);
	}

	/**
	 * 读取模板信息，返回的格式{B:{tabcode1:[{},{}],tabcode2:[{},{}]},T:{tabcode1:{},
	 * tabcode2:{}},tabAry:[]}
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadTempletDesc.json")
	@ResponseBody
	public Map<String, Object> loadTempletDesc(HttpServletRequest request, HttpServletResponse response) {
		String pk_billtemplet = request.getParameter("pk_billtemplet");
		if(StringUtils.isBlank(pk_billtemplet)) {
			throw new BusiException("主键参数pk_billtemplet不能为空！");
		}
		Map<String, Object> retMap = this.getService().loadTempletDesc(pk_billtemplet);
		return this.genAjaxResponse(true, null, retMap);
	}

	/**
	 * 复制模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/copyBillTemplet.json")
	@ResponseBody
	public Map<String, Object> copyTemplet(HttpServletRequest request, HttpServletResponse response) {
		String pk_billtemplet = request.getParameter("pk_billtemplet");
		if(StringUtils.isBlank(pk_billtemplet)) {
			throw new BusiException("请先选择一条待复制的模板！");
		}
		// 这个字段实际上是模板标题，但是web这边当做模板名称，而不使用bill_templetname这个字段
		String bill_templetcaption = request.getParameter("bill_templetcaption");
		if(StringUtils.isBlank(bill_templetcaption)) {
			throw new BusiException("复制模板时模板名称不能为空！");
		}
		String nodecode = request.getParameter("nodecode");// 节点号
		if(StringUtils.isBlank(nodecode)) {
			throw new BusiException("复制模板时节点号不能为空！");
		}
		String pk_billtypecode = request.getParameter("pk_billtypecode");
		BillTempletVO billVO = this.getService().copyBillTemplet(pk_billtemplet, bill_templetcaption, nodecode);
		return this.genAjaxResponse(true, null, billVO, this.getService().loadTempletData(pk_billtypecode));
	}

	/**
	 * 删除单据模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deleteBillTemplet.json")
	@ResponseBody
	public Map<String, Object> deleteBillTemplet(HttpServletRequest request, HttpServletResponse response) {
		String pk_billtemplet = request.getParameter("pk_billtemplet");
		if(StringUtils.isBlank(pk_billtemplet)) {
			throw new BusiException("主键参数pk_billtemplet不能为空！");
		}
		this.getService().deleteBillTemplet(pk_billtemplet);
		return this.genAjaxResponse(true, null, null);
	}

	@RequestMapping(value = "/saveBillTemplet.json")
	@ResponseBody
	public Map<String, Object> saveBillTemplet(HttpServletRequest request, HttpServletResponse response) {
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		Map<String, Object> retMap = this.getService().saveBillTemplet(billVO);
		String pk_billtypecode = request.getParameter("pk_billtypecode");
		return this.genAjaxResponse(true, null, retMap, this.getService().loadTempletData(pk_billtypecode));
	}

	/**
	 * 根据单据模板生成查询模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/buildQueryTemplet.json")
	@ResponseBody
	public Map<String, Object> buildQueryTemplet(HttpServletRequest request, HttpServletResponse response) {
		String pk_billtemplet = request.getParameter("pk_billtemplet");
		String model_name = request.getParameter("model_name");
		String node_code = request.getParameter("node_code");
		if(StringUtils.isBlank(pk_billtemplet) || StringUtils.isBlank(model_name) || StringUtils.isBlank(node_code)) {
			throw new BusiException("参数不完整！");
		}
		boolean bCover = false;
		String cover = request.getParameter("cover");
		if(StringUtils.isNotBlank(cover)) {
			bCover = new UFBoolean(cover).booleanValue();
		}
		this.getService().buildQueryTemplet(pk_billtemplet, model_name, node_code, bCover);
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 根据单据模板生成报表模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/buildReportTemplet.json")
	@ResponseBody
	public Map<String, Object> buildReportTemplet(HttpServletRequest request, HttpServletResponse response) {
		String pk_billtemplet = request.getParameter("pk_billtemplet");
		String vtemplatename = request.getParameter("vtemplatename");
		String nodecode = request.getParameter("nodecode");
		if(StringUtils.isBlank(pk_billtemplet) || StringUtils.isBlank(vtemplatename) || StringUtils.isBlank(nodecode)) {
			throw new BusiException("参数不完整！");
		}
		boolean bCover = false;
		String cover = request.getParameter("cover");
		if(StringUtils.isNotBlank(cover)) {
			bCover = new UFBoolean(cover).booleanValue();
		}
		this.getService().buildReportTemplet(pk_billtemplet, vtemplatename, nodecode, bCover);
		return this.genAjaxResponse(true, null, null);
	}
}
