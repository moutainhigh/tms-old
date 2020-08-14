package org.nw.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.ExceptionReference;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.NullNode;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.exp.ExcelImporter;
import org.nw.exp.PrintTempletUtils;
import org.nw.jf.UiConstants;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.BillTempletTVO;
import org.nw.jf.vo.FunRegisterPropertyVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiPrintTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.redis.RedisDao;
import org.nw.service.IToftService;
import org.nw.service.ServiceHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.HttpUtils;
import org.nw.utils.ImageUtil;
import org.nw.utils.NWUtils;
import org.nw.utils.ParameterHelper;
import org.nw.utils.TempletHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.index.LoginRecordVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.PersonalProfileVO;
import org.nw.vo.sys.RefInfoVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.tms.constants.BaiduMapConst;
import com.tms.service.base.GoodsService;
import com.tms.vo.base.GoodsVO;

/**
 * 与业务相关的controller基类
 * 
 * @author xuqc
 * @date 2012-6-15 下午09:51:52
 */
public abstract class AbsToftController extends AbsBaseController {
	/**
	 * 返回service类
	 * 
	 * @return
	 */
	public abstract IToftService getService();
	@Autowired
	private GoodsService goodsService;

	protected ParamVO getParamVO(HttpServletRequest request) {
		ParamVO paramVO = super.getParamVO(request);
		if(StringUtils.isNotBlank(paramVO.getVbillno())) {
			// 根据vbillno设置billId
			paramVO.setBillId(this.getService().getPKByCode(paramVO.getVbillno()));
		}
		return paramVO;
	}

	/**
	 * 进入首页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = getParamVO(request);
		// 获取模板数据
		String templetID = this.getService().getBillTemplateID(paramVO);
		if(StringUtils.isBlank(templetID)) {
			throw new BusiException("没有分配单据模板！");
		}
		//UiBillTempletVO templetVO = this.getService().getBillTempletVO(templetID);
		UiBillTempletVO templetVO = RedisDao.getInstance().getBillTempletVO(templetID, this.getService());
		AggregatedValueObject billInfo = this.getService().getBillInfo();
		// 设置表头主键
		if(billInfo.getParentVO() != null) {
			templetVO.setHeaderPkField(billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString());
		}
		templetVO.setFunCode(paramVO.getFunCode());
		templetVO.setNodeKey(paramVO.getNodeKey());

		// 设置表体主键
		setChildrenPkFieldMap(templetVO, billInfo);
		templetVO.setBillId(paramVO.getBillId());
		templetVO.setBillIds(paramVO.getBillIds());
		// 锁定条件
		templetVO.setLockingItemAry(this.getService().getLockingItemAry(paramVO));

		// 功能注册中定义的与生成单据相关的配置信息
		String sHeaderHeight = request.getParameter(FunRegisterPropertyVO._HEADERHEIGHT);
		try {
			if(sHeaderHeight != null) {
				templetVO.getFuncRegisterPropertyVO().setHeaderHeight(Integer.parseInt(sHeaderHeight));
			}
		} catch(Exception e) {
		}
		templetVO.getFuncRegisterPropertyVO().setHeaderSplit(
				Boolean.valueOf(request.getParameter(FunRegisterPropertyVO._HEADERSPLIT)));
		templetVO.getFuncRegisterPropertyVO().setSimpleUnConfirm(ParameterHelper.getSimpleUnConfirm());
		templetVO.getFuncRegisterPropertyVO().setWaterfallScene(
				Boolean.valueOf(request.getParameter(FunRegisterPropertyVO._WATERFALLSCENE)));
		templetVO.getFuncRegisterPropertyVO().setBodyWaterfallScene(
				Boolean.valueOf(request.getParameter(FunRegisterPropertyVO._BODYWATERFALLSCENE)));
		templetVO.getFuncRegisterPropertyVO().setBtnArray(
				//ServiceHelper.getBtnRegisterAry(paramVO)
				RedisDao.getInstance().getBtnRegisterAry(paramVO, WebUtils.getLoginInfo().getPk_user())
				);
		request.setAttribute(Constants.TEMPLETVO, templetVO);
		// 页面需要使用该moduleName
		request.setAttribute(Constants.MODULENAME, templetVO.getModuleName());
		// 附加数据处理
		this.doIndexExtProcess(request, response, templetVO);
		String fun_help_name = getFunHelpName(paramVO.getFunCode());
		saveLoginRecord(request, paramVO.getFunCode(), fun_help_name);
		return new ModelAndView(fun_help_name);
	}

	/**
	 * 返回ulw列表页的查询条件
	 * 
	 * @param request
	 * @return
	 */
	public String getConditionString(HttpServletRequest request) {
		return null;
	}

	/**
	 * 轻量级列表页
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/ulw_index.html")
	public ModelAndView ulw_index(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		// 分页条件
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		String orderBy = this.getOrderBy(request, null);
		String templateID = this.getService().getBillTemplateID(paramVO);
		paramVO.setTemplateID(templateID);
		UiBillTempletVO templetVO = this.getService().getBillTempletVO(templateID);
		request.setAttribute(Constants.TEMPLETVO, templetVO);
		// 这里只过滤显示的字段，但是包括主键，这个主键很重要，前台需要根据主键进行一些操作
		List<BillTempletBVO> headerListFieldVOs = UiTempletUtils.filterULWFields(templetVO.getFieldVOs(), this
				.getService().getULWImmobilityFields(paramVO));
		paramVO.setTabCode(templetVO.getHeaderTabCode());
		request.setAttribute(Constants.HEADERLISTFIELDVOS, headerListFieldVOs);

		String cond = getConditionString(request);
		// 执行查询，返回分页数据
		PaginationVO paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, cond);
		List items = paginationVO.getItems();
		// 只保留所需要的数据
		paginationVO.setItems(this.getService().processULWData(items, paramVO));
		// 设置数据到前台
		request.setAttribute(Constants.OFFSET_PARAM, offset);
		request.setAttribute(Constants.PAGE_SIZE_PARAM, pageSize);
		request.setAttribute(Constants.PAGINATIONVO, paginationVO);
		boolean showDefaultList = Constants.TRUE.equals(request.getParameter(Constants.SHOWDEFAULTLIST));
		request.setAttribute(Constants.SHOWDEFAULTLIST, showDefaultList);// 设置更多筛选条件的展开状态
		request.setAttribute("headerPkField", templetVO.getHeaderPkField());
		return new ModelAndView(getIndexJsp(request));
	}

	/**
	 * 进入页面的附加处理,to be override （具体controller可覆盖此方法）
	 * 
	 * @param request
	 * @param response
	 */
	protected void doIndexExtProcess(HttpServletRequest request, HttpServletResponse response,
			UiBillTempletVO uiBillTempletVO) {
		// 默认无处理
	}
	
	@SuppressWarnings("unchecked")
	public LoginRecordVO saveLoginRecord(HttpServletRequest request,String fun_code,String fun_help_name){
		LoginRecordVO recordVO = new LoginRecordVO();
		recordVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(recordVO);
		//获取城市信息 
		String msg = "";
		try {
			msg = HttpUtils.get(BaiduMapConst.BAIDU_ADDRESS_LOCATION_URL);
		} catch (Exception e1) {
			logger.info("百度获取地址出错：" + e1.getMessage());
		}
		Map<String,Object> addrmsg = JacksonUtils.readValue(msg, Map.class);
		if(addrmsg == null){
			return null;
		}
		Map<String,Map<String,String>> content = (Map<String, Map<String, String>>) addrmsg.get("content");
		String province = String.valueOf(content.get("address_detail") == null ? "" : content.get("address_detail").get("province"));
		//String city = String.valueOf(content.get("address_detail").get("city"));
		String baidu_x = String.valueOf(content.get("point") == null ? "" : content.get("point").get("x"));
		String baidu_y = String.valueOf(content.get("point") == null ? "" : content.get("point").get("y"));
		recordVO.setProvince(province);
		//recordVO.setCity(city);
		recordVO.setBaidu_x(baidu_x);
		recordVO.setBaidu_y(baidu_y);
//		String ip = request.getHeader("x-forwarded-for");
//		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//			ip = request.getHeader("Proxy-Client-IP");
//		}
//		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//			ip = request.getHeader("WL-Proxy-Client-IP");
//		}
//		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//			ip = request.getRemoteAddr();
//		}
		String ip = request.getParameter("ip");
		String city = "";
		try {
			city = new String(request.getParameter("city").getBytes("ISO-8859-1"),"UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recordVO.setCity(city);
		recordVO.setIp(ip);
		recordVO.setLogin_time(new UFDateTime(new Date()));
		recordVO.setPk_user(WebUtils.getLoginInfo().getPk_user());
		recordVO.setUser_code(WebUtils.getLoginInfo().getUser_code());
		recordVO.setUser_name(WebUtils.getLoginInfo().getUser_name());
		recordVO.setFun_code(fun_code);
		recordVO.setHelp_name(fun_help_name);
		NWDao.getInstance().saveOrUpdate(recordVO);
		return recordVO;
	}

	protected void setChildrenPkFieldMap(UiBillTempletVO vo, AggregatedValueObject billInfoAggVO) {
		// 设置表体主键
		CircularlyAccessibleValueObject[] childrenVOs = billInfoAggVO.getChildrenVO();
		if(childrenVOs != null && childrenVOs.length > 0) {
			// 主子表模式
			Map<String, String> childrenPkFieldMap = new HashMap<String, String>();
			for(int i = 0; i < childrenVOs.length; i++) {
				String headItemVO = childrenVOs[i].getAttributeValue(VOTableVO.HEADITEMVO).toString().trim();
				try {
					Class<?> childClass = Class.forName(headItemVO);
					SuperVO superVO = (SuperVO) childClass.newInstance();
					// 这里用VOTableVO.VOTABLE跟模板中的tabcode对应，而不是VOTableVO.ITEMCODE
					// --fangw.
					String tabcode = childrenVOs[i].getAttributeValue(VOTableVO.VOTABLE).toString();
					if(StringUtils.isBlank(tabcode)) {
						// 对于单表体，itemcode可能为空，此时从模板中读取tabcode
						List<BillTempletTVO> tabVOs = vo.getTabVOs();
						for(BillTempletTVO tabVO : tabVOs) {
							if(tabVO.getPos() == UiConstants.POS[1]) {
								tabcode = tabVO.getTabcode(); // 取得表体的tabcode
								break;
							}
						}
					}
					childrenPkFieldMap.put(tabcode, superVO.getPKFieldName());
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			vo.setChildrenPkFieldMap(childrenPkFieldMap);
		}
	}

	/**
	 * 根据funCode获取FunVO 的帮助文件名
	 */
	protected String getFunHelpName(String funCode) {
		return this.getService().getFunVOByFunCode(funCode).getHelp_name();
	}

	/**
	 * 返回列表页的jsp
	 * 
	 * @param request
	 * @return
	 */
	protected String getIndexJsp(HttpServletRequest request) {
		ParamVO paramVO = this.getParamVO(request);
		return getFunHelpName(paramVO.getFunCode());
	}

	/**
	 * 传统的单据页面不需要这个 返回卡片页的jsp，可能是新增，修改
	 * 
	 * @param request
	 * @return
	 */
	protected String getCardJsp(HttpServletRequest request) {
		return null;
	}

	/**
	 * 返回新增的默认值
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getDefaultValue.json")
	@ResponseBody
	protected Map<String, Object> getDefaultValue(HttpServletRequest request) {
		ParamVO paramVO = this.getParamVO(request);
		return genAjaxResponse(true, null, this.getService().getDefaultValue(paramVO));
	}

	/**
	 * 删除动作，删除是ajax请求
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/delete.json")
	@ResponseBody
	public Map<String, Object> delete(HttpServletRequest request, HttpServletResponse response) {
		String[] pks = request.getParameterValues("billId");
		SuperVO parentVO = (SuperVO) this.getService().getBillInfo().getParentVO();
		try {
			Class parentClass = (Class<? extends SuperVO>) Class.forName(parentVO
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			this.getService().batchDelete(parentClass, pks);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return this.genAjaxResponse(true, null, pks);
	}

	/**
	 * 查看动作，跳转到查看页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/show.json")
	@ResponseBody
	public Map<String, Object> show(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		AggregatedValueObject billVO = this.getService().show(paramVO);
		Map<String, Object> result = this.getService().execFormula4Templet(billVO, paramVO, true, true);
		this.getService().processAfterExecFormula(result);
		return this.genAjaxResponse(true, null, result);
	}

	/**
	 * 返回可更新的子表VO
	 * 
	 * @param tabcode
	 * @param voClass
	 * @param updateArray
	 * @param childrenVOList
	 * @return
	 */
	public List<CircularlyAccessibleValueObject> getUpdateChildrenVOList(String tabcode,
			Class<? extends SuperVO> voClass, JsonNode updateArray, List<CircularlyAccessibleValueObject> childrenVOList) {
		for(int m = 0; m < updateArray.size(); m++) {
			JsonNode updateObj = updateArray.get(m);
			if(updateObj == null || updateObj instanceof NullNode) {
				continue;
			}
			SuperVO toBeUpdate = JacksonUtils.readValue(updateObj, voClass);
			// 如果没有PK值，则是新增的记录；如果有PK值，则为修改的记录
			if(StringUtils.isBlank(toBeUpdate.getPrimaryKey())) {
				toBeUpdate.setStatus(VOStatus.NEW);
				childrenVOList.add(toBeUpdate);
			} else {
				// 遍历，将修改信息直接覆盖到原始的VO上
				for(CircularlyAccessibleValueObject childVO : childrenVOList) {
					if(toBeUpdate.getPrimaryKey().equals(((SuperVO) childVO).getPrimaryKey())) {
						Iterator<String> it = updateObj.getFieldNames();
						while(it.hasNext()) {
							String fieldName = it.next();
							((SuperVO) childVO).setAttributeValue(fieldName, toBeUpdate.getAttributeValue(fieldName));
						}
						childVO.setStatus(VOStatus.UPDATED);
					}
				}

			}
		}
		return childrenVOList;
	}

	/**
	 * 转换聚合vo
	 * 
	 * @param billType
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AggregatedValueObject convertJsonToAggVO(String json) {
		/*
		 * 解决行、列的问题 1.获取pk字段值
		 * 2.如果pk字段不为空，则认为是修改，从数据库获取原billvo(单表体获取的是superVO)，否则创建新vo
		 * this.queryBillVOByBillId(billType, billId); 3.把修改应用到原billvo，然后返回
		 */
		AggregatedValueObject billVO = null;
		AggregatedValueObject billInfo = this.getService().getBillInfo();
		Class<?> parentVOClass = null;

		Map<String, Class<? extends SuperVO>> childrenVOClassMap = new HashMap<String, Class<? extends SuperVO>>();

		try {
			Class<?> aggVOClass = null;
			if(billInfo.getParentVO() != null) {
				parentVOClass = Class.forName(billInfo.getParentVO().getAttributeValue(VOTableVO.HEADITEMVO).toString()
						.trim());
				aggVOClass = Class
						.forName(billInfo.getParentVO().getAttributeValue(VOTableVO.BILLVO).toString().trim());
			}
			if(billInfo.getChildrenVO() != null) {
				for(int i = 0; i < billInfo.getChildrenVO().length; i++) {
					Class<?> childVOClass = Class.forName(billInfo.getChildrenVO()[i]
							.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
					childrenVOClassMap.put(billInfo.getChildrenVO()[i].getAttributeValue(VOTableVO.VOTABLE).toString(),
							(Class<? extends SuperVO>) childVOClass);
				}
				if(aggVOClass == null) {
					// 可能是没有配置表头的VOTable，属于单表体的情况
					aggVOClass = Class.forName(billInfo.getChildrenVO()[0].getAttributeValue(VOTableVO.BILLVO)
							.toString().trim());
				}
			}
			billVO = (AggregatedValueObject) aggVOClass.newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		// 多子表处理，前台必须传递tabCode，而且grid数据必须分离，以便这里识别某个grid的数据对应哪个vo类，进而实现json->object的转换
		/**
		 * 获取parentVO后，取pk，如果为null就是新增，直接保存，如果不为null则是修改，然后做如下处理：
		 * 1.queryBillVOByBillId或queryByCondition获取原始的数据VO；
		 * 2.在JsonNode中取字段名，然后去转化到的相应VO中取值
		 * （不能直接在迭代VO，因为VO里包含了所有的字段，而JsonNode里的字段才是需要修改的字段）；
		 * 3.在原始数据VO中通过setAttributeValue设置修改的字段值； 子表同理
		 */
		JsonNode jn = JacksonUtils.readTree(json);
		JsonNode header = jn.get(Constants.HEADER);
		// rawBillVO用于存放原始的billVO，在修改的时候用
		AggregatedValueObject rawBillVO = null;
		SuperVO parentVO = null;
		if(header != null) {
			parentVO = (SuperVO) JacksonUtils.readValue(header, parentVOClass);
			if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
				parentVO.setStatus(VOStatus.NEW);// 新增主表记录
			} else {
				// 修改单据时，需要获取原始单据主表数据，然后将修改的字段修改到原始的主表VO
				ParamVO paramVO = new ParamVO();
				paramVO.setBillId(parentVO.getPrimaryKey());
				rawBillVO = this.getService().queryBillVO(paramVO);
				SuperVO rawParentVO = (SuperVO) rawBillVO.getParentVO();
				Iterator<String> it = header.getFieldNames();
				while(it.hasNext()) {
					String fieldName = it.next();
					rawParentVO.setAttributeValue(fieldName, parentVO.getAttributeValue(fieldName));
				}
				parentVO = rawParentVO;
				parentVO.setStatus(VOStatus.UPDATED);// 更新主表记录
			}
			billVO.setParentVO(parentVO);
		}

		JsonNode body = jn.get(Constants.BODY);
		if(body != null) {
			if(childrenVOClassMap.isEmpty() && parentVOClass == null) {
				throw new BusiException("JSON转聚合VO时，有子表数据但没有找到子表VO类，可能VOTABLE没有配置！");
			}
			// 如果表头存在，且为单据新增
			if(parentVO != null && StringUtils.isBlank(parentVO.getPrimaryKey())) {
				for(String key : childrenVOClassMap.keySet()) {
					JsonNode tab_body_grid_json = body.get(key); // 每个tab的grid数据
					if(tab_body_grid_json == null || tab_body_grid_json.isNull()) {
						continue;
					}

					JsonNode updateArray = tab_body_grid_json.get(Constants.UPDATE);// 新增和更新的数据都在update里
					Class<? extends SuperVO> voClass = childrenVOClassMap.get(key);
					List<SuperVO> childrenVOList = new ArrayList<SuperVO>();
					if(updateArray != null) {
						for(int m = 0; m < updateArray.size(); m++) {
							JsonNode updateObj = updateArray.get(m);
							SuperVO toBeUpdate = JacksonUtils.readValue(updateObj, voClass);
							toBeUpdate.setStatus(VOStatus.NEW);// 这里只可能有new的数据
							childrenVOList.add(toBeUpdate);
						}
					}

					SuperVO[] voArray = (SuperVO[]) Array.newInstance(voClass, childrenVOList.size());
					if(billVO instanceof IExAggVO) {
						((IExAggVO) billVO).setTableVO(key, childrenVOList.toArray(voArray));
					} else {
						billVO.setChildrenVO(childrenVOList.toArray(voArray));
					}
				}
			}
			// 如果表头存在，且为单据修改
			if(parentVO != null && StringUtils.isNotBlank(parentVO.getPrimaryKey())) {
				for(String key : childrenVOClassMap.keySet()) {
					JsonNode tab_body_grid_json = body.get(key); // 每个tab的grid数据
					if(tab_body_grid_json == null || tab_body_grid_json.isNull()) {
						// shij 没有修改数据时,应该把数据库中的VO设置为当前表体
						if(billVO instanceof IExAggVO) {
							((IExAggVO) billVO).setTableVO(key, ((IExAggVO) rawBillVO).getTableVO(key));
						} else {
							billVO.setChildrenVO(rawBillVO.getChildrenVO());
						}
						continue;
					}

					// 用来存放原始的子表记录
					List<CircularlyAccessibleValueObject> childrenVOList = new ArrayList<CircularlyAccessibleValueObject>();
					// 多子表和单子表的处理
					CircularlyAccessibleValueObject[] childrenVO = null;
					if(billVO instanceof IExAggVO) {
						childrenVO = ((IExAggVO) rawBillVO).getTableVO(key);
					} else {
						childrenVO = rawBillVO.getChildrenVO();
					}
					if(childrenVO != null) {
						childrenVOList.addAll(Arrays.asList(childrenVO));
					}

					Class<? extends SuperVO> voClass = childrenVOClassMap.get(key);
					// 收集待删除的数据，在原始的VO上进行操作
					JsonNode deleteArray = tab_body_grid_json.get(Constants.DELETE);
					if(deleteArray != null) {
						for(int m = 0; m < deleteArray.size(); m++) {
							JsonNode deleteObj = deleteArray.get(m);
							SuperVO toBeDel = JacksonUtils.readValue(deleteObj, voClass);
							for(CircularlyAccessibleValueObject childVO : childrenVOList) {
								if(toBeDel.getPrimaryKey().equals(((SuperVO) childVO).getPrimaryKey())) {
									childVO.setStatus(VOStatus.DELETED);
								}
							}
						}
					}

					// 收集待更新的子表记录，包括新增的记录和修改的记录
					// XXX 对于强调顺序的字段，从前台传入的vo是所有的行记录，
					// 这里的做法应该是把childrenVOList的数据写入toBeUpdate,而不是toBeUpdate写入childrenVOList
					JsonNode updateArray = tab_body_grid_json.get(Constants.UPDATE);
					if(updateArray != null) {
						childrenVOList = getUpdateChildrenVOList(key, voClass, updateArray, childrenVOList);
					}

					SuperVO[] voArray = (SuperVO[]) Array.newInstance(voClass, childrenVOList.size());
					if(billVO instanceof IExAggVO) {
						((IExAggVO) billVO).setTableVO(key, childrenVOList.toArray(voArray));
					} else {
						billVO.setChildrenVO(childrenVOList.toArray(voArray));
					}
				}
			}
			// 如果表头不存在，只有表体
			/*
			 * 这儿的处理方法跟上面有所不同，上面的处理都是在通过主表取得原始BillVO的基础上，取得各个子表的VO数组。这儿因为
			 * 表头不存在，所以无法原始原始的BillVO。同时，因为前台传来的数据只有新增、修改和删除过的，因此这儿无法通过遍历子表记录
			 * PK的方法取得所有原始的VO。因此，这儿简单处理，只将处理过的记录转化为相应状态的VO，返回BillVO。
			 */
			if(header == null) {
				List<SuperVO> childrenVOList = new ArrayList<SuperVO>();
				for(String key : childrenVOClassMap.keySet()) {
					JsonNode tab_body_grid_json = body.get(key); // 每个tab的grid数据
					if(tab_body_grid_json == null || tab_body_grid_json.isNull()) {
						continue;
					}

					Class<? extends SuperVO> voClass = childrenVOClassMap.get(key);
					JsonNode deleteArray = tab_body_grid_json.get(Constants.DELETE);
					if(deleteArray != null) {
						for(int m = 0; m < deleteArray.size(); m++) {
							JsonNode deleteObj = deleteArray.get(m);
							SuperVO toBeDel = JacksonUtils.readValue(deleteObj, voClass);
							toBeDel.setStatus(VOStatus.DELETED);
							childrenVOList.add(toBeDel);
						}
					}

					JsonNode updateArray = tab_body_grid_json.get(Constants.UPDATE);
					if(updateArray != null) {
						for(int m = 0; m < updateArray.size(); m++) {
							JsonNode updateObj = updateArray.get(m);
							SuperVO toBeUpdate = JacksonUtils.readValue(updateObj, voClass);
							if(StringUtils.isBlank(toBeUpdate.getPrimaryKey())) {
								toBeUpdate.setStatus(VOStatus.NEW);
								childrenVOList.add(toBeUpdate);
							} else {
								String where = "";
								where += toBeUpdate.getPKFieldName();
								where += "=?";
								SuperVO rawVO = NWDao.getInstance().queryByCondition(voClass, where,
										toBeUpdate.getPrimaryKey());
								Iterator<String> it = updateObj.getFieldNames();
								while(it.hasNext()) {
									String fieldName = it.next();
									rawVO.setAttributeValue(fieldName, toBeUpdate.getAttributeValue(fieldName));
								}
								rawVO.setStatus(VOStatus.UPDATED);
								childrenVOList.add(rawVO);
							}
						}
					}

					SuperVO[] voArray = (SuperVO[]) Array.newInstance(voClass, childrenVOList.size());
					if(billVO instanceof IExAggVO) {
						((IExAggVO) billVO).setTableVO(key, childrenVOList.toArray(voArray));
					} else {
						billVO.setChildrenVO(childrenVOList.toArray(voArray));
					}
				}
			}
		}

		if(this.isConvertEmptyToNull()) {
			billVO = ServiceHelper.convertEmptyStringToNull(billVO);
		}
		return billVO;
	}

	/**
	 * 是否转换空串为null
	 * 
	 * @return
	 */
	public boolean isConvertEmptyToNull() {
		return true;
	}

	/**
	 * 保存前校验code是否存在，和code的唯一性
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		String codeField = this.getService().getCodeFieldCode();
		if(parentVO != null) {
			if(StringUtils.isNotBlank(codeField)) {
				Object code = parentVO.getAttributeValue(this.getService().getCodeFieldCode());
				if(code != null && StringUtils.isNotBlank(code.toString())) {
					try {
						// XXX 这里到底要不要包括dr=1的数据呢？
						// 合同删除了，结果加入不了相同编码的合同了，删除的数据理论上是不能恢复的。恢复的时候需要看看编码是否已经被占用了。
						SuperVO superVO = null;
						//若是货品管理需要特殊处理
						if(GoodsVO.GOODS_CODE.equals(codeField)){
							String customCode = (String) parentVO.getAttributeValue(GoodsVO.PK_CUSTOMER);
							superVO = goodsService.getByGoodsCodeCustomCode(code.toString(), customCode);
						}else{
							superVO = this.getService().getByCode(code.toString());
						}
						if(StringUtils.isBlank(parentVO.getPrimaryKey())) {							
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
								if(!parentVO.getPrimaryKey().equals(superVO.getPrimaryKey())) {
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
			}
			// 子公司只能修改本公司的数据
			if(!WebUtils.getLoginInfo().getPk_corp().equals(Constants.SYSTEM_CODE)) {
				// 不是集团登陆的
				//List<CorpVO> vos = CorpHelper.getCurrentCorpVOsWithChildren();// 当前公司和子公司
				List<CorpVO> vos = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());
				Object pk_corp = parentVO.getAttributeValue("pk_corp");
				if(pk_corp != null) {
					if(!pk_corp.toString().equals("@@@@")) {
						boolean exist = false;
						for(CorpVO vo : vos) {
							if(pk_corp.toString().equals(vo.getPk_corp())) {
								exist = true;// 可编辑的范围
								break;
							}
						}
						if(!exist) {
							throw new BusiException("您不能修改父级公司的数据");
						}
					}
				}
			}
		}
	}

	/**
	 * 保存动作，可能是新增、修改
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/save.json")
	@ResponseBody
	public Map<String, Object> save(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeSave(billVO, paramVO);
		billVO = this.getService().save(billVO, paramVO);
		Map<String,Object> result = this.getService().execFormula4Templet(billVO, paramVO, true, false);
		return this.genAjaxResponse(true, null, result);
	}

	/**
	 * 加载数据<br>
	 * 注：主表和子表都用这一个方法
	 * 
	 * @param request
	 * @param response
	 * @param funCode
	 * @param templateID
	 * @param tabCode
	 * @param isBody
	 * @return
	 */
	@RequestMapping(value = "/loadData.json")
	@ResponseBody
	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);

		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		String extendCond = null, treePk = null;
		PaginationVO paginationVO = null;

		if(getTreePkField() != null) {
			// 判断是否存在树的情况
			treePk = request.getParameter(getTreePkField());
		}
		if(StringUtils.isNotBlank(treePk)) {
			extendCond = this.getTreePkField() + "=?";
			paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, extendCond, treePk);
		} else {
			paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, null);
		}

		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 若存在左边树，需要继承该方法，返回树的主键字段,如果是编辑树的类型，比如部门管理。也需要继承该方法
	 * 
	 * @return
	 */
	public String getTreePkField() {
		return null;
	}

	/**
	 * 复制主子表 选择一条单据后，点击复制，直接将该单据复制一份，并切换到新增状态
	 * 
	 * @param request
	 * @param response
	 * @param billId
	 */
	@RequestMapping(value = "/copy.json")
	@ResponseBody
	public Map<String, Object> copy(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		AggregatedValueObject billVO = this.getService().copy(paramVO);
		Map<String, Object> map = this.getService().execFormula4Templet(billVO, paramVO);
		return this.genAjaxResponse(true, null, map);
	}

	/********* 与查询模板相关 *************/
	/**
	 * 返回查询模板的所有条件，用于构建查询窗口左边树结构
	 */
	@RequestMapping(value = "loadConds.json")
	@ResponseBody
	public Map<String, Object> loadConds(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String templetID = this.getService().getQueryTemplateID(paramVO);
		if(StringUtils.isBlank(templetID)) {
			throw new BusiException("没有分配查询模板！");
		}
		UiQueryTempletVO vo = this.getService().getQueryTempletVO(templetID);
		if(vo == null || vo.getConditions() == null) {
			throw new BusiException("没有查询模板，或查询模板为空！");
		}
		if(vo.getConditions().size() == 0) {
			throw new BusiException("有查询模板,但模板字段为空！");
		}
		return this.genAjaxResponse(true, null, vo.getConditions());
	}

	/**
	 * 返回查询模板条件的表单域 根据数据类型生成不同的表单域，目前支持的表单域有：字符、整数、小数、日期、逻辑、参照、下拉
	 * 
	 * @param ids
	 *            字段编码和表单表示为组成，用于生成表单域的id，格式如：ycim_investproj_b.vadmindeptid_1;
	 * @param pks
	 *            查询模板VO的主键
	 * @param funCode
	 * @param nodeKey
	 * @return
	 */
	@RequestMapping(value = "getItem.json")
	@ResponseBody
	public Map<String, Object> getItem(HttpServletRequest request, HttpServletResponse response) {
		String cid = request.getParameter("cid");
		String pk = request.getParameter("pk");

		ParamVO paramVO = this.getParamVO(request);
		String templetID = this.getService().getQueryTemplateID(paramVO);
		if(StringUtils.isBlank(templetID)) {
			throw new BusiException("没有分配查询模板！");
		}
		UiQueryTempletVO vo = this.getService().getQueryTempletVO(templetID);
		Object item = UIUtils.getItem(vo, cid, pk);
		return this.genAjaxResponse(true, null, item);
	}

	/**
	 * 返回查询模板默认查询条件的表单域。 根据数据类型生成不同的表单域，目前支持的表单域有：字符、整数、小数、日期、逻辑、参照、下拉
	 * 
	 * @param ids
	 *            字段编码和表单表示为组成，用于生成表单域的id，格式如：ycim_investproj_b.vadmindeptid_1;
	 * @param pks
	 *            查询模板VO的主键
	 * @param funCode
	 * @param nodeKey
	 * @return
	 */
	@RequestMapping(value = "getDefault.json")
	@ResponseBody
	public Map<String, Object> getDefault(HttpServletRequest request, HttpServletResponse response) {
		String[] cids = request.getParameterValues("cids");
		String[] pks = request.getParameterValues("pks");

		ParamVO paramVO = this.getParamVO(request);
		List<Object> items = new ArrayList<Object>();
		String templetID = this.getService().getQueryTemplateID(paramVO);
		if(StringUtils.isBlank(templetID)) {
			throw new BusiException("没有分配查询模板！");
		}
		UiQueryTempletVO vo = this.getService().getQueryTempletVO(templetID);

		PersonalProfileVO profileVO = TempletHelper.getPersonalProfileVO(paramVO.getFunCode(), vo.getTemplateID(),
				UiConstants.TPL_STYLE.QUERY.intValue());
		if(profileVO != null) {
			pks = profileVO.getField_pks().split(Constants.SPLIT_ATTR);
			cids = profileVO.getComp_ids().split(Constants.SPLIT_ATTR);
			if(pks == null || cids == null) {
				return this.genAjaxResponse(true, null, null);
			}
			if(pks.length != cids.length) {
				throw new BusiException("保存的条件中字段和组件id不对应");
			}
		}
		for(int i = 0; i < pks.length; i++) {
			String cid = cids[i];
			String pk = pks[i];
			Object item = UIUtils.getItem(vo, cid, pk);
			items.add(item);
		}

		// 这里条件可能是从个人配置信息表读取的，需要返回到前台
		Map<String, Object> toRet = new HashMap<String, Object>();
		toRet.put("pks", pks);
		toRet.put("cids", cids);
		// 这里pks可能已经改变，需要回传回去
		return this.genAjaxResponse(true, null, items, toRet);
	}

	/**
	 * 保存查询条件
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @author xuqc
	 * @date 2012-4-23
	 */
	@RequestMapping("/saveConds.json")
	@ResponseBody
	public Map<String, Object> saveConds(HttpServletRequest request, HttpServletResponse response) {
		String[] cids = request.getParameterValues("cids");
		String[] pks = request.getParameterValues("pks");

		if(cids == null || cids.length == 0 || pks == null || pks.length == 0) {
			throw new BusiException("没有任何查询条件，不执行保存动作!");
		}
		if(cids.length != pks.length) {
			throw new BusiException("保存的条件中字段和组件id不对应");
		}

		ParamVO paramVO = this.getParamVO(request);
		UiQueryTempletVO vo = this.getService().getQueryTempletVO(this.getService().getQueryTemplateID(paramVO));
		PersonalProfileVO profileVO = TempletHelper.getPersonalProfileVO(paramVO.getFunCode(), vo.getTemplateID(),
				UiConstants.TPL_STYLE.QUERY.intValue());
		if(profileVO == null) {
			profileVO = new PersonalProfileVO();
			profileVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(profileVO);
		} else {
			profileVO.setStatus(VOStatus.UPDATED);
		}
		profileVO.setComp_ids(StringUtils.join(cids, Constants.SPLIT_ATTR));
		profileVO.setField_pks(StringUtils.join(pks, Constants.SPLIT_ATTR));
		profileVO.setPk_user(WebUtils.getLoginInfo().getPk_user());
		profileVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		profileVO.setPk_template(vo.getTemplateID());
		profileVO.setNode_code(paramVO.getFunCode());
		profileVO.setTemplatestyle(UiConstants.TPL_STYLE.QUERY.intValue());
		try {
			this.getService().saveOrUpdate(profileVO);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return this.genAjaxResponse(true, null, null);
	}

	/**
	 * 参照->执行编辑公式 这个URL在权限过滤时将忽略掉，该方法可以被覆盖，但是不推荐使用另外的URL Mapping
	 * 
	 * @param request
	 * @param response
	 * @param vo
	 * @param pkBilltempletB
	 * @return
	 */
	@RequestMapping(value = "/execFormula.json")
	@ResponseBody
	public Map<String, Object> execFormula(HttpServletRequest request, HttpServletResponse response,
			String pkBilltemplet, String pkBilltempletB) {
		// 1.获取编辑公式,这里获取编辑公式统一从TempletHandler中读取，因为定义了转换器，模板数据在读取后会被重新组织
		UiBillTempletVO vo = this.getService().getBillTempletVO(pkBilltemplet);// this.getService().getBillTempletVO4ExecFormula(pkBilltemplet);
		List<BillTempletBVO> fieldVOs = vo.getFieldVOs();
		BillTempletBVO billTempletBVO = null;
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPk_billtemplet_b().equals(pkBilltempletB)) {
				// 定位到该参照的模板信息
				billTempletBVO = fieldVO;
				break;
			}
		}
		// 2.获取数据model
		// 2.1.获取字段类型
		Map<String, Integer> typeMap = new HashMap<String, Integer>();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(billTempletBVO.getTable_code().equals(fieldVO.getTable_code())) {
				typeMap.put(fieldVO.getItemkey(), fieldVO.getDatatype());
			}
		}
		// 2.2.组装数据model
		Map<String, Object> context = new HashMap<String, Object>();
		@SuppressWarnings("rawtypes")
		Enumeration enumeration = request.getParameterNames();
		while(enumeration.hasMoreElements()) {
			String key = enumeration.nextElement().toString();
			Object value = request.getParameter(key);
			if(value != null && value.toString().length() > 0 && typeMap.get(key) != null) {
				if(UiConstants.DATATYPE.INTEGER.intValue() == typeMap.get(key).intValue()) {
					// value值可能是非法的
					try {
						value = Integer.valueOf(value.toString());
					} catch(Exception e) {
						value = null;
					}
				} else if(UiConstants.DATATYPE.DECIMAL.intValue() == typeMap.get(key).intValue()) {
					// value值可能是非法的
					try {
						value = Double.valueOf(value.toString());
					} catch(Exception e) {
						value = null;
					}
				}
			}
			context.put(key, value);
		}
		return this.getService().execEditFormula(billTempletBVO, context);
	}

	/**
	 * 导出excel文件，将列表信息导出，可以是主表，也可以是子表
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/exportExcel.do")
	public void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		ParamVO paramVO = this.getParamVO(request);
		// 返回查询的值
		String extendCond = null;
		String treePk = null;
		HSSFWorkbook workbook = null;
		if(getTreePkField() != null) {
			// 判断是否存在树的情况
			treePk = request.getParameter(getTreePkField());
			extendCond = this.getTreePkField() + "=?";
			workbook = this.getService().export(paramVO, Constants.DEFAULT_OFFSET_WITH_NOPAGING,
					Constants.DEFAULT_PAGESIZE_WITH_NOPAGING, null, extendCond, treePk);
		} else {
			workbook = this.getService().export(paramVO, Constants.DEFAULT_OFFSET_WITH_NOPAGING,
					Constants.DEFAULT_PAGESIZE_WITH_NOPAGING, null, null);
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

	/**
	 * 返回上传文件的那个字段名称
	 * 
	 * @return
	 */
	protected String getUploadField() {
		return null;
	}
	
	/**
	 * 返回作为其他用途的的缩略图上传路径
	 * 
	 * @return
	 */
	protected String getOtherUseURL() {
		return null;
	}

	/**
	 * 无刷新文件上传
	 * <p>
	 * 不能使用@responseBody,response头会自动使用aplication/json，在IE下会提示下载文件
	 * </p>
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/upload.json")
	public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		String uploadField = getUploadField(); // 返回前台上传文件域的字段名称
		if(StringUtils.isBlank(uploadField)) {
			throw new BusiException("子类需要定义上传文件的字段名称！");
		}
		// 根据前台的name名称得到上传的文件
		MultipartFile file = mRequest.getFile(uploadField);
		String uploadDir = Global.uploadDir;
		String funCode = request.getParameter("funCode");
		if(StringUtils.isNotBlank(funCode)) {
			// 如果存在funCode，那么文件放在funCode的文件夹下
			uploadDir = Global.uploadDir + File.separator + funCode;
		}
		uploadDir += File.separator + DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_8CHAR);// 日期文件夹
		File uploadFolder = new File(uploadDir);
		if(!uploadFolder.exists()) {
			uploadFolder.mkdirs();
		}
		// 得到文件名
		String fileName = NWUtils.generateFileName(null);
		File destFile = new File(uploadDir + File.separator + fileName);
		try {
			file.transferTo(destFile);
		} catch(Exception e) {
			logger.error("上传文件错误，" + e.getMessage());
			e.printStackTrace();
			throw new BusiException("上传文件错误[?]！",e.getMessage());
		}
		// 这里返回原始文件名和新的文件名
		HashMap<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("fileName", file.getOriginalFilename());
		dataMap.put("realFileName", fileName);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("msg", "操作成功!");
		retMap.put("success", "true");
		retMap.put("data", dataMap);
		this.writeHtmlStream(response, JacksonUtils.writeValueAsString(retMap));
	}

	/**
	 * 无刷新图片上传，会生成缩略图
	 * <p>
	 * 不能使用@responseBody,response头会自动使用aplication/json，在IE下会提示下载文件
	 * </p>
	 * 
	 * @param request
	 * @param response
	 */
	
	@RequestMapping(value = "/uploadImage.json")
	public void uploadImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		String uploadField = getUploadField(); // 返回前台上传文件域的字段名称
		if(StringUtils.isBlank(uploadField)) {
			throw new BusiException("子类需要定义上传文件的字段名称！");
		}
		// 根据前台的name名称得到上传的文件
		MultipartFile file = mRequest.getFile(uploadField);
		String uploadDir = Global.uploadDir;
		String funCode = request.getParameter("funCode");
		if(StringUtils.isNotBlank(funCode)) {
			// 如果存在funCode，那么文件放在funCode的文件夹下
			uploadDir = Global.uploadDir + File.separator + funCode;
		}
		uploadDir += File.separator + DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_8CHAR);// 日期文件夹
		File uploadFolder = new File(uploadDir);
		if(!uploadFolder.exists()) {
			uploadFolder.mkdirs();
		}
		// 得到文件名
		String fileName = NWUtils.generateFileName(null);
		File destFile = new File(uploadDir + File.separator + fileName);
		try {
			file.transferTo(destFile);
		} catch(Exception e) {
			logger.error("上传文件错误:" + e.getMessage());
			e.printStackTrace();
			throw new BusiException("上传文件错误[?]！",e.getMessage());
		}

		// 生成缩略图
		String thumbDir = uploadDir + File.separator + "s"; // 缩略图路径
		String otherDir = getOtherUseURL();
		File otherFolder = null;
		if(StringUtils.isNotBlank(otherDir)){
			otherFolder = new File(otherDir);
			if(!otherFolder.exists()) {
				otherFolder.mkdirs();
			}
		}
		File thumbFolder = new File(thumbDir);
		if(!thumbFolder.exists()) {
			thumbFolder.mkdirs();
		}
		int width = Global.getIntValue("upload.image.thumb.width");
		int heigth = Global.getIntValue("upload.image.thumb.height");
		try {
			if(otherFolder != null){
				ImageUtil.resize(destFile, new File(otherFolder + File.separator + fileName + ".jpg"), width, heigth, false);
			}
			ImageUtil.resize(destFile, new File(thumbDir + File.separator + fileName), width, heigth, false);
		} catch(IOException e) {
			logger.error("生成缩略图失败，可能不是图片文件，" + e.getMessage());
			e.printStackTrace();
			// 删除上传的文件
			destFile.delete();
			throw new BusiException("生成缩略图失败，可能不是图片文件[?]！",e.getMessage());
		}
		this.writeHtmlStream(response, "{'msg':'操作成功!','success':'true','data':'" + fileName + "'}");
	}

	

	/**
	 * 返回无刷新上传的文件
	 * 
	 * @param request
	 * @param fileName
	 * @return
	 */
	protected File getUploadFile(HttpServletRequest request, String fileName) {
		String downloadDir = Global.uploadDir;
		String funCode = request.getParameter("funCode");
		if(StringUtils.isNotBlank(funCode)) {
			// 如果存在funCode，那么文件放在funCode的文件夹下
			downloadDir = Global.uploadDir + File.separator + funCode;
		}
		String dateStr = fileName.substring(0, 8);// 日期
		downloadDir += File.separator + dateStr;
		String thumb = request.getParameter("thumb"); // 是否请求的是缩略图
		if(StringUtils.isNotBlank(thumb)) {
			downloadDir += File.separator + "s";
		}
		File destFile = new File(downloadDir + File.separator + fileName);
		if(!destFile.exists()) {
			throw new RuntimeException("文件不存在！");
		}
		return destFile;
	}

	/**
	 * 文件下载
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		String fileName = request.getParameter("fileName");
		if(StringUtils.isBlank(fileName)) {
			throw new RuntimeException("文件名称参数不能为空！");
		} else if(fileName.length() < 8) {
			throw new RuntimeException("文件名称不正确！");
		}
		try {
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment;filename=\""
					+ new String(fileName.getBytes(), "ISO-8859-1") + "\"");

			File destFile = getUploadFile(request, fileName);
			/* 创建输出流 */
			ServletOutputStream servletOS = response.getOutputStream();
			FileInputStream inputStream = new FileInputStream(destFile);
			byte[] buf = new byte[1024];
			int readLength;
			while(((readLength = inputStream.read(buf)) != -1)) {
				servletOS.write(buf, 0, readLength);
			}
			inputStream.close();
			servletOS.flush();
			servletOS.close();
		} catch(Exception e) {
			throw new RuntimeException("文件下载出错！", e);
		}
	}

	/**
	 * 显示照片
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/viewImage.do")
	public void viewImage(HttpServletRequest request, HttpServletResponse response) {
		response.reset();
		response.resetBuffer();
		response.setContentType("image/png");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		String fileName = request.getParameter("fileName");
		if(StringUtils.isBlank(fileName)) {
			throw new RuntimeException("文件名称参数不能为空！");
		} else if(fileName.length() < 8) {
			throw new RuntimeException("文件名称不正确！");
		}
		String downloadDir = Global.uploadDir;
		String funCode = request.getParameter("funCode");
		if(StringUtils.isNotBlank(funCode)) {
			downloadDir += File.separator + funCode;
		}
		String dateStr = fileName.substring(0, 8);// 日期
		downloadDir += File.separator + dateStr;
		String thumb = request.getParameter("thumb"); // 是否请求的是缩略图
		if(StringUtils.isNotBlank(thumb)) {
			downloadDir += File.separator + "s";
		}

		File destFile = new File(downloadDir + File.separator + fileName);
		if(!destFile.exists()) {
			throw new RuntimeException("文件不存在！");
		}

		try {
			/* 创建输出流 */
			ServletOutputStream servletOS = response.getOutputStream();
			FileInputStream inputStream = new FileInputStream(destFile);
			byte[] buf = new byte[1024];
			int readLength;
			while(((readLength = inputStream.read(buf)) != -1)) {
				servletOS.write(buf, 0, readLength);
			}
			inputStream.close();
			servletOS.flush();
			servletOS.close();
		} catch(Exception e) {
			logger.error("文件下载出错," + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 点击修改或者切换到卡片页后，根据单据主键查询一条单据
	 */
	@RequestMapping("/prev.json")
	@ResponseBody
	public Map<String, Object> prev(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		if(StringUtils.isBlank(paramVO.getBillId())) {
			throw new RuntimeException("必须先选择一条记录！");
		}
		String orderBy = this.getOrderBy(request, null);
		String extendCond = null, treePk = null;
		if(getTreePkField() != null) {
			// 判断是否存在树的情况
			treePk = request.getParameter(getTreePkField());
		}
		if(StringUtils.isNotBlank(treePk)) {
			if(StringUtils.isNotBlank(extendCond)) {
				extendCond = this.getTreePkField() + "='" + treePk + "' and " + extendCond;
			} else {
				extendCond = this.getTreePkField() + "='" + treePk + "'";
			}
		}
		AggregatedValueObject billVO = this.getService().doPrev(paramVO, orderBy, extendCond);
		Map<String, Object> result = this.getService().execFormula4Templet(billVO, paramVO, true, true);
		this.getService().processAfterExecFormula(result);
		return this.genAjaxResponse(true, null, result);
	}

	/**
	 * 点击修改或者切换到卡片页后，根据单据主键查询一条单据
	 */
	@RequestMapping("/next.json")
	@ResponseBody
	public Map<String, Object> next(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		if(StringUtils.isBlank(paramVO.getBillId())) {
			throw new RuntimeException("必须先选择一条记录！");
		}
		String orderBy = this.getOrderBy(request, null);
		String extendCond = null, treePk = null;
		if(getTreePkField() != null) {
			// 判断是否存在树的情况
			treePk = request.getParameter(getTreePkField());
		}
		if(StringUtils.isNotBlank(treePk)) {
			if(StringUtils.isNotBlank(extendCond)) {
				extendCond = this.getTreePkField() + "='" + treePk + "' and " + extendCond;
			} else {
				extendCond = this.getTreePkField() + "='" + treePk + "'";
			}
		}
		AggregatedValueObject billVO = this.getService().doNext(paramVO, orderBy, extendCond);
		Map<String, Object> result = this.getService().execFormula4Templet(billVO, paramVO, true, true);
		this.getService().processAfterExecFormula(result);
		return this.genAjaxResponse(true, null, result);
	}

	/************ 打印相关 *****************/

	/**
	 * 单据打印（导出pdf）
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/print.do")
	public void print(HttpServletRequest request, HttpServletResponse response) throws IOException {
		printBySql(request,response);
		return;
//		logger.info("开始执行单据打印动作...");
//		Calendar start = Calendar.getInstance();
//		ParamVO paramVO = this.getParamVO(request);
//		if(StringUtils.isBlank(paramVO.getBillId())) {
//			throw new BusiException("请先选择一张单据进行打印！");
//		}
//		/**
//		 * 获取单据模板数据，用于执行公式，获取单据展现的数据
//		 */
//		UiBillTempletVO uiBillTempletVO = this.getService().getBillTempletVO(
//				this.getService().getBillTemplateID(paramVO));
//
//		/**
//		 * 获取打印模板数据，用于画pdf
//		 */
//		UiPrintTempletVO uiPrintTempletVO = this.getService().getPrintTempletVO(
//				this.getService().getPrintTempletID(paramVO));
//		if(uiPrintTempletVO == null) {
//			throw new BusiException("没有分配打印模板！");
//		}
//
//		/**
//		 * 设置输出方式和名称之类的
//		 */
//		// 保存时的文件名
//		String pdfName = uiPrintTempletVO.getPrintTempletVO().getVtemplatename() + ".pdf";
//		String fn = null;
//		try {
//			// 这里主要解决中文文件名乱码问题
//			fn = new String(pdfName.getBytes("GB2312"), "ISO-8859-1");
//		} catch(UnsupportedEncodingException e) {
//			throw new BusiException(e);
//		}
//		// ie中直接打开
//		// response.setHeader("Content-Disposition", "inline;filename=\"" + fn +
//		// "\"");
//		// 以浏览器附件下载方式
//		response.setHeader("Content-Disposition", "inline;filename=\"" + fn + "\"");
//		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
//		response.setHeader("Pragma", "public");
//		response.setDateHeader("Expires", (System.currentTimeMillis() + 1000));
//		response.setContentType("application/pdf");
//		ServletOutputStream out = null;
//		try {
//			out = response.getOutputStream();
//			InputStream in = TempletHelper.getPrintTempletData(uiPrintTempletVO.getPrintTempletId());
//			if(in == null) {
//				throw new BusiException("打印模板文件为空！");
//			}
//			InputStream compileIn = null;
//			try {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				JasperCompileManager.compileReportToStream(in, baos);
//				byte[] ary = baos.toByteArray();
//				compileIn = new ByteArrayInputStream(ary);// 已经编译后的字节流
//			} catch(JRException e) {
//				e.printStackTrace();
//				throw new BusiException("模板文件在编译成jasper文件时出错,错误信息：" + e.getMessage());
//			}
//			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(compileIn);
//			List<Map<String, Object>> context = this.getService().getBillPrintDataSource(paramVO, uiBillTempletVO,
//					uiPrintTempletVO);
//			JRDataSource dataSource = new JRBeanCollectionDataSource(context);
//			JasperPrint print = JasperFillManager.fillReport(jasperReport,
//					this.getService().getBillPrintParameterMap(request, paramVO), dataSource);
//			JasperExportManager.exportReportToPdfStream(print, out);
//		} catch(Exception e) {
//			e.printStackTrace();
//		} finally {
//			logger.info("打印动作执行完毕，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
//			if(out != null) {
//				out.flush();
//				out.close();
//			}
//		}
	}
	
	/**
	 * 单据打印（导出pdf）
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/printBySql.do")
	public void printBySql(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("开始执行单据打印动作...");
		Calendar start = Calendar.getInstance();
		ParamVO paramVO = this.getParamVO(request);
		if(StringUtils.isBlank(paramVO.getBillId())) {
			throw new BusiException("请先选择一张单据进行打印！");
		}
//		/**
//		 * 获取单据模板数据，用于执行公式，获取单据展现的数据
//		 */
//		UiBillTempletVO uiBillTempletVO = this.getService().getBillTempletVO(
//				this.getService().getBillTemplateID(paramVO));

		/**
		 * 获取打印模板数据，用于画pdf
		 */
		UiPrintTempletVO uiPrintTempletVO = this.getService().getPrintTempletVO(
				this.getService().getPrintTempletID(paramVO));
		if(uiPrintTempletVO == null) {
			throw new BusiException("没有分配打印模板！");
		}

		/**
		 * 设置输出方式和名称之类的
		 */
		// 保存时的文件名
		String pdfName = uiPrintTempletVO.getPrintTempletVO().getVtemplatename() + ".pdf";
		String fn = null;
		try {
			// 这里主要解决中文文件名乱码问题
			fn = new String(pdfName.getBytes("GB2312"), "ISO-8859-1");
		} catch(UnsupportedEncodingException e) {
			throw new BusiException(e);
		}
		// ie中直接打开
		// response.setHeader("Content-Disposition", "inline;filename=\"" + fn +
		// "\"");
		// 以浏览器附件下载方式
		response.setHeader("Content-Disposition", "inline;filename=\"" + fn + "\"");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setDateHeader("Expires", (System.currentTimeMillis() + 1000));
		response.setContentType("application/pdf");
		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
			InputStream in = TempletHelper.getPrintTempletData(uiPrintTempletVO.getPrintTempletId());
			if(in == null) {
				throw new BusiException("打印模板文件为空！");
			}
			InputStream compileIn = null;
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JasperCompileManager.compileReportToStream(in, baos);
				byte[] ary = baos.toByteArray();
				compileIn = new ByteArrayInputStream(ary);// 已经编译后的字节流
			} catch(JRException e) {
				e.printStackTrace();
				throw new BusiException("模板文件在编译成jasper文件时出错,错误信息[?]！",e.getMessage());
			}
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(compileIn);
			//用模板的数据源SQL获取数据源
			List context = PrintTempletUtils.getBillPrintDataMapSourceBySql(uiPrintTempletVO.getPrintTempletVO().getDatasourcesql(),paramVO.getBillId());
			JRDataSource dataSource = new JRBeanCollectionDataSource(context);
			JasperPrint print = JasperFillManager.fillReport(jasperReport,
					this.getService().getBillPrintParameterMap(request, paramVO), dataSource);
			JasperExportManager.exportReportToPdfStream(print, out);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("打印动作执行完毕，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
			if(out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 报表的打印
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/reportPrint.do")
	public void reportPrint(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("开始执行报表打印动作...");
		Calendar start = Calendar.getInstance();
		ParamVO paramVO = this.getParamVO(request);
		/**
		 * 获取打印模板
		 */
		UiReportTempletVO templetVO = this.getService().getReportTempletVO(
				this.getService().getReportTempletID(paramVO));

		/**
		 * 获取打印模板数据，用于画pdf
		 */
		UiPrintTempletVO uiPrintTempletVO = this.getService().getPrintTempletVO(
				this.getService().getPrintTempletID(paramVO));
		if(uiPrintTempletVO == null) {
			throw new BusiException("没有分配打印模板！");
		}

		/**
		 * 设置输出方式和名称之类的
		 */
		// 保存时的文件名
		String pdfName = uiPrintTempletVO.getPrintTempletVO().getVtemplatename() + ".pdf";
		String fn = null;
		try {
			// 这里主要解决中文文件名乱码问题
			fn = new String(pdfName.getBytes("GB2312"), "ISO-8859-1");
		} catch(UnsupportedEncodingException e) {
			throw new BusiException(e);
		}
		// ie中直接打开
		// response.setHeader("Content-Disposition", "inline;filename=\"" + fn +
		// "\"");
		// 以浏览器附件下载方式
		response.setHeader("Content-Disposition", "inline;filename=\"" + fn + "\"");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setDateHeader("Expires", (System.currentTimeMillis() + 1000));
		response.setContentType("application/pdf");
		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
			InputStream in = TempletHelper.getPrintTempletData(uiPrintTempletVO.getPrintTempletId());
			if(in == null) {
				throw new BusiException("打印模板文件为空！");
			}
			InputStream compileIn = null;
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JasperCompileManager.compileReportToStream(in, baos);
				byte[] ary = baos.toByteArray();
				compileIn = new ByteArrayInputStream(ary);// 已经编译后的字节流
			} catch(JRException e) {
				e.printStackTrace();
				throw new BusiException("模板文件在编译成jasper文件时出错,错误信息[?]！",e.getMessage());
			}
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(compileIn);
			List<Map<String, Object>> context = this.getService().getReportPrintDataSource(paramVO, templetVO,
					uiPrintTempletVO);
			JRDataSource dataSource = new JRBeanCollectionDataSource(context);
			JasperPrint print = JasperFillManager.fillReport(jasperReport, this.getService()
					.getReportPrintParameterMap(request, paramVO), dataSource);
			JasperExportManager.exportReportToPdfStream(print, out);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("打印动作执行完毕，耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "毫秒。");
			if(out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 返回表格模板信息，在弹出框中使用,funCode和tabCode参数是必须的， 使用单据模板中定义的信息，这样不需要在js中硬编码
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTempletMap.json")
	@ResponseBody
	public Map<String, Object> getTempletMap4Grid(HttpServletRequest request, HttpServletResponse response) {
		String tabCode = request.getParameter("tabCode");
		if(StringUtils.isBlank(tabCode)) {
			throw new BusiException("页签编码不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		String key = paramVO.getFunCode();
		UiBillTempletVO templetVO;
		if(StringUtils.isNotBlank(paramVO.getFunCode())) {
			templetVO = this.getService().getBillTempletVOByFunCode(paramVO);
		} else {
			key = request.getParameter("pk_billtypecode");
			if(StringUtils.isNotBlank(key)) {
				templetVO = this.getService().getBillTempletVOByBilltypecode(key);
			} else {
				throw new BusiException("查询模板时，功能注册编码或者模板编码不能为空！");
			}
		}
		if(templetVO == null) {
			throw new BusiException("根据功能编码或模板编码没有找到相应模板！code:" + key);
		}
		return this.genAjaxResponse(true, null, ServiceHelper.buildTempletMap4Grid(paramVO, templetVO, tabCode));
	}

	/**
	 * 返回模板的表头信息，用于生成表单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTempletMap4Form.json")
	@ResponseBody
	public Map<String, Object> getTempletMap4Form(HttpServletRequest request, HttpServletResponse response) {
		String tabCode = request.getParameter("tabCode");
		if(StringUtils.isBlank(tabCode)) {
			throw new BusiException("页签编码不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		String key = paramVO.getFunCode();
		UiBillTempletVO templetVO;
		if(StringUtils.isNotBlank(paramVO.getFunCode())) {
			templetVO = this.getService().getBillTempletVOByFunCode(paramVO);
		} else {
			key = request.getParameter("pk_billtypecode");
			if(StringUtils.isNotBlank(key)) {
				templetVO = this.getService().getBillTempletVOByBilltypecode(key);
			} else {
				throw new BusiException("查询模板时，功能注册编码或者模板编码不能为空！");
			}
		}
		if(templetVO == null) {
			throw new BusiException("根据功能编码或模板编码没有找到相应模板！code:" + key);
		}
		return this.genAjaxResponse(true, null, ServiceHelper.buildTempletMap4Form(paramVO, templetVO, tabCode));
	}

	/**
	 * 根据pk查询模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTempletMapByPK4Form.json")
	@ResponseBody
	public Map<String, Object> getTempletMapByPK4Form(HttpServletRequest request, HttpServletResponse response) {
		String pk_templet = request.getParameter("pk_templet");
		if(StringUtils.isBlank(pk_templet)) {
			throw new BusiException("模板PK不能为空！");
		}
		ParamVO paramVO = this.getParamVO(request);
		UiBillTempletVO templetVO = this.getService().getBillTempletVO(pk_templet);
		if(templetVO == null) {
			throw new BusiException("根据模板PK没有找到相应模板！PK[?]！",pk_templet);
		}
		return this.genAjaxResponse(true, null, ServiceHelper.buildTempletMap4Form(paramVO, templetVO, null));
	}

	/**
	 * 加载系统可选择的参照
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/loadSystemRef.json")
	@ResponseBody
	public Map<String, Object> loadSystemRef(HttpServletRequest request, HttpServletResponse response) {
		List<RefInfoVO> vos = this.getService().loadSystemRef();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(vos.size());
		for(RefInfoVO vo : vos) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.TEXT, vo.getName());
			map.put(Constants.VALUE, vo.getName());
			results.add(map);
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("records", results);
		return retMap;
	}

	/**
	 * 下载模板文件
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/downloadTemplet.do")
	public void downloadTemplet(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		new ExcelImporter().downladTemplet(request, response, paramVO);
	}

	/**
	 * 导入文件
	 * 
	 * @param mfile
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/import.do")
	public void _import(@RequestParam("file") MultipartFile mfile,
			@RequestParam("pk_import_config") String pk_import_config,
			HttpServletRequest request,
			HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		response.setContentType("text/html");
		String uploadDir = Global.uploadDir;
		File dir = new File(uploadDir + File.separator + "temp");
		if(!dir.exists()) {
			dir.mkdirs(); // 若目录不存在，则创建目录
		}
		String extension = ""; // 文件后缀名
		if(mfile.getOriginalFilename().indexOf(".") != -1) {
			extension = mfile.getOriginalFilename().substring(mfile.getOriginalFilename().lastIndexOf("."));
		}
		File file = new File(dir + File.separator + DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL)
				+ extension);
		try {
			mfile.transferTo(file);// 将文件上传到临时目录
			String strMsg = this.getService().doImport(paramVO, file, pk_import_config);
			// IE8下，不能返回符号，< > &
			String userAgent = request.getHeader("User-Agent").toLowerCase();
			if(userAgent.indexOf("msie 8") != -1) {
				// IE8,去你妈的IE8
				response.setContentType("text/html;charset=UTF-8");
				strMsg = URLEncoder.encode(strMsg, "utf-8");
			} else {
				strMsg = strMsg.replace("\n", "</p><p>"); //
				// 打印到控制台使用\n，打印到页面上使用<BR>
				strMsg = "<p>" + strMsg + "</p>";
			}
			response.getWriter().write("{'success':true,msg:'导入成功！','log':'" + strMsg + "'}");
		} catch(Exception e) {
			e.printStackTrace();
			throw new JsonException(e.getMessage());
		} finally {
			try {
				file.delete();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 点击修改或者切换到卡片页后，根据单据主键查询一条单据
	 */
	@RequestMapping("/getCodeField.json")
	@ResponseBody
	public String getCodeField(HttpServletRequest request, HttpServletResponse response) {
		return this.getService().getCodeFieldCode();
	}
	
	@RequestMapping("/getExceptionMessage.json")
	@ResponseBody
	public String getExceptionMessage(HttpServletRequest request, HttpServletResponse response){
		String zh_CN_Message = request.getParameter("zh_CN_Message");
		ExceptionReference ref = new ExceptionReference();
		if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
			return zh_CN_Message;
		}else{
			if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				String en_US_Message = ref.getEnValue(zh_CN_Message);
				if(StringUtils.isNotBlank(en_US_Message)){
					return en_US_Message;
				}
			}
		}
		return zh_CN_Message;
	}
}
