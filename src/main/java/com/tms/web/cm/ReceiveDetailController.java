package com.tms.web.cm;

import java.io.File;
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
import org.codehaus.jackson.JsonNode;
import org.nw.BillStatus;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.exp.ExcelImporter;
import org.nw.json.JacksonUtils;
import org.nw.redis.RedisDao;
import org.nw.service.sys.DataDictService;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DataDictBVO;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tms.constants.FunConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.base.CustService;
import com.tms.service.cm.ReceCheckSheetService;
import com.tms.service.cm.ReceiveDetailService;
import com.tms.service.cm.impl.ReceiveDetailExcelImporter;
import com.tms.service.te.impl.EntrustExcelImporter;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.cm.ExAggReceiveDetailVO;
import com.tms.vo.cm.PayCheckSheetBVO;
import com.tms.vo.cm.ReceCheckSheetBVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.te.EntTransbilityBVO;

/**
 * 应收明细
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
@Controller
@RequestMapping(value = "/cm/rd")
public class ReceiveDetailController extends AbsBillController {

	public static final String RECEIVABLE_METHOD = "receivable_method";// 数据字典中定义收款方式的关键字
	public static final String RD_UNCONFIRM_TYPE_LIST = "rd_unconfirm_type_list";// 数据字典中定义应收明细反确认类型

	@Autowired
	private ReceiveDetailService receiveDetailService;
	
	@Autowired
	private DataDictService dataDictService;

	@Autowired
	private ReceCheckSheetService receCheckSheetService;

	@Autowired
	private CustService custService;

	public ReceiveDetailService getService() {
		return receiveDetailService;
	}
// yaojiie 2015 11 20 因为需要添加判断逻辑 登录用户可以看到本公司及子公司，和客户公司为自己公司的数据，并修改，所以重新写一个checkBeforeSave方法
	protected void checkBeforeSaveold(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		ReceiveDetailVO parentVO = (ReceiveDetailVO) billVO.getParentVO();
		if(StringUtils.isNotBlank(parentVO.getPk_receive_detail())) {
			// 修改的情况
			ReceiveDetailVO oriVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
					parentVO.getPk_receive_detail());
			if(oriVO.getRece_type().intValue() == ReceiveDetailConst.ORIGIN_TYPE
					&& parentVO.getRece_type().intValue() != ReceiveDetailConst.ORIGIN_TYPE) {
				throw new BusiException("不能将单据类型从[其他类型]改成[原始类型]！");
			}
		}

		if(parentVO.getStatus() == VOStatus.NEW && parentVO.getRece_type().intValue() == ReceiveDetailConst.ORIGIN_TYPE) {
			// 新增单据不能选择原始单据
			throw new BusiException("新增单据的单据类型不能选择[原始单据]！");
		}
		// 校验金额是否相等
		ExAggReceiveDetailVO aggVO = (ExAggReceiveDetailVO) billVO;
		ReceDetailBVO[] rdbVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
		UFDouble cost_amount = parentVO.getCost_amount() == null ? new UFDouble(0) : parentVO.getCost_amount();
		UFDouble new_cost_amount = new UFDouble(0);
		for(ReceDetailBVO rdbVO : rdbVOs) {
			if(rdbVO.getStatus() != VOStatus.DELETED) {
				new_cost_amount = new_cost_amount.add(rdbVO.getAmount() == null ? new UFDouble(0) : rdbVO.getAmount());
			}
		}
		cost_amount = cost_amount.setScale(2, UFDouble.ROUND_HALF_UP);
		new_cost_amount = new_cost_amount.setScale(2, UFDouble.ROUND_HALF_UP);
		if(cost_amount.doubleValue() != new_cost_amount.doubleValue()) {
			throw new BusiException("表头和表体金额不一致,请检查!");
		}
	}
	
	
	@SuppressWarnings("unused")
	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		//以下内容 来自AbsToftController是最基础的修改判断
		CircularlyAccessibleValueObject parentVO1 = billVO.getParentVO();
		if(parentVO1 != null) {
					try {
						SuperVO superVO = null;
						if(StringUtils.isBlank(parentVO1.getPrimaryKey())) {							
							// 新增的情况
							if(superVO != null) {
								if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
									throw new RuntimeException("单据号或编码已经存在！");
								}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
									throw new RuntimeException("Bill number or code already exists!");
								}
								throw new RuntimeException("单据号或编码已经存在！");
							}
						} else {
							// 修改的情况
							if(superVO != null) {
								if(!parentVO1.getPrimaryKey().equals(superVO.getPrimaryKey())) {
									if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
										throw new RuntimeException("单据号或编码已经存在！");
									}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
										throw new RuntimeException("Bill number or code already exists!");
									}
									throw new RuntimeException("单据号或编码已经存在！");
								}
							}
						}
					} catch(BusinessException e) {
						e.printStackTrace();
					}
				}
			// 子公司只能修改本公司的数据
			if(!WebUtils.getLoginInfo().getPk_corp().equals(Constants.SYSTEM_CODE)) {
				// 不是集团登陆的
				//List<CorpVO> vos = CorpHelper.getCurrentCorpVOsWithChildren();// 当前公司和子公司
				List<CorpVO> vos = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());
				Object pk_corp = parentVO1.getAttributeValue("pk_corp");
				Object pk_customer = parentVO1.getAttributeValue("pk_customer");
				//这里一次性获取了三个VO 通过userVO获得user所在公司的VO，通过corpVO获得客户的VO
//				UserVO userVO = NWDao.getInstance().queryByCondition(UserVO.class, "pk_user=?",
//						WebUtils.getLoginInfo().getPk_user());
//				
				CorpVO corpVO = NWDao.getInstance().queryByCondition(CorpVO.class, "pk_corp=?",
						WebUtils.getLoginInfo().getPk_corp());
//				CustomerVO customerVO = NWDao.getInstance().queryByCondition(CustomerVO.class, "cust_code=?",
//						corpVO.getCorp_code());
				if(pk_corp != null) {
					if(!pk_corp.toString().equals("@@@@")) {
						boolean exist = false;
						for(CorpVO vo : vos) {
							if(pk_corp.toString().equals(vo.getPk_corp())) {
							//if(pk_corp.toString().equals(vo.getPk_corp()) || customerVO.getPk_customer().equals(pk_customer)) {
								exist = true;// 可编辑的范围
								break;
							}
						}
						if(!exist) {
							throw new BusiException("您不能修改父级公司的数据！");
						}
					}
				}
			}
			// 以下内容 来自AbsbillController是最基础的修改判断
			if(checkBillStatusBeforeSave()) {
//				CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
				Object vbillstatus = parentVO1.getAttributeValue(this.getService().getBillStatusField());
				if(vbillstatus == null) {
					throw new BusiException("不能保存单据状态为空的单据！");
				}
				if(!paramVO.isReviseflag()) {
					if(Integer.parseInt(vbillstatus.toString()) != BillStatus.NEW) {
						throw new BusiException("只能编辑单据状态为[新建]的单据！");
					}
				}
			}
			
			//以下内容 来自receivedetialController是原有的判断
			ReceiveDetailVO parentVO = (ReceiveDetailVO) billVO.getParentVO();
			if(StringUtils.isNotBlank(parentVO.getPk_receive_detail())) {
				// 修改的情况
				ReceiveDetailVO oriVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?",
						parentVO.getPk_receive_detail());
				if(oriVO.getRece_type().intValue() == ReceiveDetailConst.ORIGIN_TYPE
						&& parentVO.getRece_type().intValue() != ReceiveDetailConst.ORIGIN_TYPE) {
					throw new BusiException("不能将单据类型从[其他类型]改成[原始类型]");
				}
			}

			if(parentVO.getStatus() == VOStatus.NEW && parentVO.getRece_type().intValue() == ReceiveDetailConst.ORIGIN_TYPE) {
				// 新增单据不能选择原始单据
				throw new BusiException("新增单据的单据类型不能选择[原始单据]");
			}
			// 校验金额是否相等
			ExAggReceiveDetailVO aggVO = (ExAggReceiveDetailVO) billVO;
			ReceDetailBVO[] rdbVOs = (ReceDetailBVO[]) aggVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
			UFDouble cost_amount = parentVO.getCost_amount() == null ? new UFDouble(0) : parentVO.getCost_amount();
			UFDouble new_cost_amount = new UFDouble(0);
			for(ReceDetailBVO rdbVO : rdbVOs) {
				if(rdbVO.getStatus() != VOStatus.DELETED) {
					new_cost_amount = new_cost_amount.add(rdbVO.getAmount() == null ? new UFDouble(0) : rdbVO.getAmount());
				}
			}
			cost_amount = cost_amount.setScale(2, UFDouble.ROUND_HALF_UP);
			new_cost_amount = new_cost_amount.setScale(2, UFDouble.ROUND_HALF_UP);
			if(cost_amount.doubleValue() != new_cost_amount.doubleValue()) {
				throw new BusiException("表头和表体金额不一致,请检查!");
			}
			
			
		}
		
	
	

	/**
	 * 返回还款方式，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getReceivableMethod.json")
	@ResponseBody
	public Map<String, Object> getReceivableMethod(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(RECEIVABLE_METHOD);
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
	
	/**
	 * 返回贴息原因，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getDiscountMemo.json")
	@ResponseBody
	public Map<String, Object> getDiscountMemo(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("discount_memo");
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

	/**
	 * 返回税率，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTaxRate.json")
	@ResponseBody
	public Map<String, Object> getTaxRate(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("tax_rate");
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
	@RequestMapping(value = "/getCheckType.json")
	@ResponseBody
	public Map<String, Object> getCheckType(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("billing_type");
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
	
	@RequestMapping(value = "/getDefaultCheckType.json")
	@ResponseBody
	public Map<String, Object> getDefaultCheckType(HttpServletRequest request, HttpServletResponse response) {
		String bala_customer = request.getParameter("bala_customer");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(bala_customer)) {
			logger.warn("客户参数不能为空！");
			return retMap;
		}
		retMap.put("checkType", custService.getDefaultCheckType(bala_customer));
		return retMap;
	}
	
	@RequestMapping(value = "/getDefaultCheckCorp.json")
	@ResponseBody
	public Map<String, Object> getDefaultCheckCorp(HttpServletRequest request, HttpServletResponse response) {
		String bala_customer = request.getParameter("bala_customer");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(bala_customer)) {
			logger.warn("客户参数不能为空！");
			return retMap;
		}
		retMap.put("checkType", custService.getDefaultCheckCorp(bala_customer));
		return retMap;
	}
	
	
	
	@RequestMapping(value = "/getCheckCorp.json")
	@ResponseBody
	public Map<String, Object> getCheckCorp(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("check_company");
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
	/**
	 * 返回税种，从数据字典中读取
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTaxCat.json")
	@ResponseBody
	public Map<String, Object> getTaxCat(HttpServletRequest request, HttpServletResponse response) {
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode("tax_category");
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
	
	/**
	 * 根据结算客户返回发票抬头
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getCheckHead.json")
	@ResponseBody
	public Map<String, Object> getCheckHead(HttpServletRequest request, HttpServletResponse response) {
		String bala_customer = request.getParameter("bala_customer");
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(bala_customer)) {
			logger.warn("结算客户参数不能为空！");
			return retMap;
		}
		retMap.put("checkHead", custService.getCheckHead(bala_customer));
		return retMap;
	}

	/**
	 * 加载收款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadReceRecord.json")
	@ResponseBody
	public Map<String, Object> loadReceRecord(HttpServletRequest request, HttpServletResponse response) {
		String pk_receive_detail = request.getParameter("pk_receive_detail");
		if(StringUtils.isBlank(pk_receive_detail)) {
			throw new BusiException("请先选择一行应收明细记录！");
		}
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		PaginationVO paginationVO = this.getService().loadReceRecord(pk_receive_detail, paramVO, offset, pageSize);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 将应收明细加入对账单时，根据结算客户查询所有新建的对账单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadReceCheckSheet.json")
	@ResponseBody
	public Map<String, Object> loadReceCheckSheet(HttpServletRequest request, HttpServletResponse response) {
		String bala_customer = request.getParameter("bala_customer");
		if(StringUtils.isBlank(bala_customer)) {
			throw new BusiException("加载应收明细对账单时，结算客户不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		PaginationVO paginationVO = receCheckSheetService.getByBala_customer(bala_customer, paramVO);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 生成对账单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/buildReceCheckSheet.json")
	@ResponseBody
	public Map<String, Object> buildReceCheckSheet(HttpServletRequest request, HttpServletResponse response) {
		String json = request.getParameter(Constants.HEADER);
		ParamVO paramVO = this.getParamVO(request);
		JsonNode header = JacksonUtils.readTree(json);
		List<String> pk_receive_details = new ArrayList<String>();
		if(header.size() > 0) {
			for(int i = 0; i < header.size(); i++){
				JsonNode obj = header.get(i);
				ReceCheckSheetBVO childVO = (ReceCheckSheetBVO) JacksonUtils.readValue(obj, ReceCheckSheetBVO.class);
				pk_receive_details.add(childVO.getPk_receive_detail());
			}
		}
		Map<String,String> procMsg = this.getService().CheckSheetByProc(NWUtils.join(pk_receive_details.toArray(new String[pk_receive_details.size()]), ","));
		String msg = null;
		if(procMsg != null && procMsg.size() > 0){
			msg = procMsg.get("msg");
			String type = procMsg.get("type");
			if(type.equals("1")){
				return this.genAjaxResponse(false, msg, null);
			}
		}
		List<Map<String, Object>> retList = this.getService().buildReceCheckSheet(paramVO, json);
		return this.genAjaxResponse(true, msg, retList);
	}

	/**
	 * 加入对账单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/addToReceCheckSheet.json")
	@ResponseBody
	public Map<String, Object> addToReceCheckSheet(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_receive_detail = request.getParameterValues("pk_receive_detail");
		String pk_rece_check_sheet = request.getParameter("pk_rece_check_sheet");
		if(pk_receive_detail == null || pk_receive_detail.length == 0) {
			throw new BusiException("请先选择要加入对账单的应收明细！");
		}
		if(pk_rece_check_sheet == null) {
			throw new BusiException("请先选择对账单记录！");
		}
		Map<String,String> procMsg = this.getService().CheckSheetByProc(NWUtils.join(pk_receive_detail, ","));
		String msg = null;
		if(procMsg != null && procMsg.size() > 0){
			msg = procMsg.get("msg");
			String type = procMsg.get("type");
			if(type.equals("1")){
				return this.genAjaxResponse(false, msg, null);
			}
		}
		List<Map<String, Object>> retList = this.getService().addToReceCheckSheet(paramVO, pk_rece_check_sheet,
				pk_receive_detail);
		return this.genAjaxResponse(true, msg, retList);
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

	/**
	 * 全额收款
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/receivableAll.json")
	@ResponseBody
	public Map<String, Object> receivableAll(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_receive_detail = request.getParameterValues("pk_receive_detail");
		List<Map<String, Object>> retList = this.getService().doReceivableAll(paramVO, pk_receive_detail);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 删除收款纪录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deleteReceRecord.json")
	@ResponseBody
	public Map<String, Object> deleteReceRecord(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_rece_record = request.getParameter("pk_rece_record");
		Map<String, Object> retMap = this.getService().deleteReceRecord(paramVO, pk_rece_record);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.add(retMap);
		return genAjaxResponse(true, null, retList);
	}

	/**
	 * 提交按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/commit.json")
	@ResponseBody
	public Map<String, Object> commit(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().commit(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量提交时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未提交成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 反提交按钮的处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/uncommit.json")
	@ResponseBody
	public Map<String, Object> uncommit(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder result = new StringBuilder();
		int num = 0;
		for(String id : billId) {
			paramVO.setBillId(id); //
			try {
				Map<String, Object> retMap = this.getService().uncommit(paramVO);
				list.add(retMap);
			} catch(Exception e) {
				logger.warn("批量反提交时存在异常，单据ID：" + id, e);
				num++;
				result.append(e.getMessage());
			}
		}
		if(result.length() > 0) {
			result.insert(0, "以下单据未反提交成功：<br/>");
			if(num == billId.length) {// 都审批失败
				throw new BusiException(result.toString());
			}
			return this.genAjaxResponse(true, null, list, result.toString());
		} else {
			return this.genAjaxResponse(true, null, list);
		}
	}

	/**
	 * 重新计算合同金额
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reComputeMny.json")
	@ResponseBody
	public Map<String, Object> reComputeMny(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		
		List<Map<String, Object>> retList = this.getService().reComputeMny(billId, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 集货发货单重算金额
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/rebuildBySegtype.json")
	@ResponseBody
	public Map<String, Object> rebuildBySegtype(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0) {
			throw new BusiException("请先选择记录");
		}
		int seg_type = Integer.parseInt(request.getParameter("seg_type"));
		this.getService().doRebuildBySegtype(paramVO, seg_type, billId);
		return this.genAjaxResponse(true, null, null);
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
		AggregatedValueObject billVO = dataDictService.getAggVOByDatatypeCode(RD_UNCONFIRM_TYPE_LIST);
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
	
	/**
	 * 集货应收明细导出  songf  2015-11-03
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/exportPickupReceiveDetail.do")
	public void exportPickupReceiveDetailRecord(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		ParamVO paramVO = this.getParamVO(request);
		
		//此处取到前台选中的所有key的值
		String[] keys = request.getParameterValues("key");
		HSSFWorkbook workbook = null;
		workbook = this.getService().exportPickupReceiveDetailRecord(paramVO,keys,"正常");
		
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
	
	@RequestMapping(value = "/close.json")
	@ResponseBody
	public Map<String, Object> close(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(String id : billId){
			paramVO.setBillId(id);
			Map<String, Object> retMap = this.getService().close(paramVO);
			list.add(retMap);
		}
		return this.genAjaxResponse(true, null, list);
	}

	@RequestMapping(value = "/unclose.json")
	@ResponseBody
	public Map<String, Object> unclose(HttpServletRequest request, HttpServletResponse response) {
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0 || billId[0] == null || billId[0].length() == 0) {
			throw new BusiException("billId不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(String id : billId){
			paramVO.setBillId(id);
			Map<String, Object> retMap = this.getService().unclose(paramVO);
			list.add(retMap);
		}
		return this.genAjaxResponse(true, null, list);
	}
	
}
