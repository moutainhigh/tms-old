package org.nw.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiAlertException;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.json.JacksonUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.binder.UFBooleanEditor;
import org.nw.web.binder.UFDateEditor;
import org.nw.web.binder.UFDateTimeEditor;
import org.nw.web.utils.ControllerHelper;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * 所有控制器的基类，提供几个与业务无关的简便的方法
 */
public abstract class AbsBaseController extends MultiActionController {
	protected Log logger = LogFactory.getLog(this.getClass());

	protected static final String HTML_CONTENT_TYPE = "text/html; charset=utf-8";
	protected static final String XML_CONTENT_TYPE = "text/xml; charset=utf-8";
	protected static String UNCONFIRM_TYPE ="unconfirm_type";//反确认类型
	protected static String UNCONFIRM_MEMO ="unconfirm_memo";//反确认说明
	protected static String VENT_TYPE ="vent_type";//反确认说明
	protected static String VENT_MEMO ="vent_memo";//反确认说明

	/**
	 * springmvc绑定器初始化，可以注册自己的editor 注：这里不用于json的场景，只用于原生态web的参数绑定
	 * 
	 * @param binder
	 */
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		// 日期型
		SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATEFORMAT_HORIZONTAL);
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		binder.registerCustomEditor(UFDate.class, new UFDateEditor());

		// 日期时间
		SimpleDateFormat datetimeFormat = new SimpleDateFormat(DateUtils.DATETIME_FORMAT_HORIZONTAL);
		datetimeFormat.setLenient(false);
		binder.registerCustomEditor(UFDateTime.class, new UFDateTimeEditor());

		// UFBooleanEditor
		binder.registerCustomEditor(UFBoolean.class, new UFBooleanEditor());
	}

	/**
	 * 页面操作后的返回信息，该信息会被页面捕获，用于操作提示
	 * 
	 * @param result
	 *            操作成功与否
	 * @param msg
	 *            返回操作后的信息，默认操作成功，一般操作失败会抛出异常信息
	 * @param obj
	 *            操作返回的对象，一般是ajax操作后返回的数据对象
	 * @param append
	 *            此字段特定场合用作业务的查询条件 页面可能需要其他的提示信息，如批量操作中,有些记录没有执行成功，此时需要提示用户
	 * @return
	 */
	protected Map<String, Object> genAjaxResponse(boolean result, String msg, Object obj, Object append) {
		Map<String, Object> viewMap = new HashMap<String, Object>();
		viewMap.put("success", result);
		viewMap.put("msg", msg == null ? Constants.FRAME_OPERATE_SUCCESS : msg);
		if(obj instanceof List || obj instanceof Object[]) { // List或者数组
			viewMap.put("datas", obj);
		} else if(obj instanceof PaginationVO) {
			PaginationVO paginationVO = (PaginationVO) obj;
			if(paginationVO.getSummaryRowMap() != null) {
				viewMap.put("summaryRow", paginationVO.getSummaryRowMap());
			}
			viewMap.put("totalRecords", paginationVO.getTotalCount());
			viewMap.put("records", paginationVO.getItems());
		} else if(obj instanceof ReportVO) {// 报表VO
			ReportVO reportVO = (ReportVO) obj;
			PaginationVO pageVO = reportVO.getPageVO();
			viewMap.put("totalRecords", pageVO.getTotalCount());
			viewMap.put("records", pageVO.getItems());
			if(pageVO.getSummaryRowMap() != null) {
				viewMap.put("summaryRow", pageVO.getSummaryRowMap());
			}
			// 对于动态报表，可能需要返回列头信息，页面可以重新刷新列头
			if(reportVO.getHeaderVO() != null) {
				viewMap.put("headerVO", reportVO.getHeaderVO());
			}
		} else {
			viewMap.put("data", obj);
		}
		if(append != null) {
			viewMap.put("append", append);
		}
		return viewMap;
	}

	/**
	 * 页面操作后的返回信息，该信息会被页面捕获，用于操作提示
	 * 
	 * @param result
	 *            操作成功与否
	 * @param msg
	 *            返回操作后的信息，默认操作成功，一般操作失败会抛出异常信息
	 * @param obj
	 *            操作返回的对象，一般是ajax操作后返回的数据对象
	 * @return
	 */
	protected Map<String, Object> genAjaxResponse(boolean result, String msg, Object obj) {
		return genAjaxResponse(result, msg, obj, null);
	}

	/**
	 * 模式化请求所需要的参数
	 * 
	 * @param request
	 * @return
	 */
	protected ParamVO getParamVO(HttpServletRequest request) {
		ParamVO vo = new ParamVO();
		String json = request.getParameter(Constants.HEADER);
		if(json!=null){
			JsonNode header = JacksonUtils.readTree(json);
			if(header.get(UNCONFIRM_TYPE)!=null){
				// yaojiie 2015 12 05将String转换成Integer
				Integer unconfirmType = header.get(UNCONFIRM_TYPE).getValueAsInt();
				vo.setUnconfirmType(unconfirmType);
			}
			if(header.get(UNCONFIRM_MEMO)!=null){
				String unconfirmMemo = header.get(UNCONFIRM_MEMO).getTextValue();
				vo.setUnconfirmMemo(unconfirmMemo);
			}
			// yaojiie 2015 12 27将将退单类型也放到这里处理
			if(header.get(VENT_TYPE)!=null){
				Integer ventType = header.get(VENT_TYPE).getValueAsInt();
				vo.setVentType(ventType);
			}
			if(header.get(VENT_MEMO)!=null){
				String ventMemo = header.get(VENT_MEMO).getTextValue();
				vo.setVentMemo(ventMemo);
			}
			
		}
		vo.setBillId(request.getParameter("billId"));
		vo.setBillIds(request.getParameter("billIds"));
		// 某些操作会发送这个参数，但是实际上不需要执行查询返回billId，参考com.tms.web.AbsBusiController.getParamVO(HttpServletRequest)
		// 这里参数使用比较不常见的_vbillno
		vo.setVbillno(request.getParameter("_vbillno"));
		vo.setPk_checkflow(request.getParameter("pk_checkflow"));
		vo.setBody(Boolean.valueOf(request.getParameter("isBody")));
		vo.setTabCode(request.getParameter("tabCode"));
		vo.setHeaderTabCode(request.getParameter("headerTabCode"));
		vo.setBodyTabCode(request.getParameter("bodyTabCode"));
		vo.setTemplateID(request.getParameter("templateID"));
		vo.setFunCode(request.getParameter("funCode"));
		vo.setNodeKey(request.getParameter("nodeKey"));
		vo.setBillType(request.getParameter("billType"));
		vo.setTemplateFunCode(request.getParameter("templateFunCode"));
		vo.setReviseflag(Boolean.valueOf(request.getParameter("reviseflag")));
		return vo;
	}

	/**
	 * 从request中读取开始记录数，用于翻页
	 * 
	 * @param request
	 * @return
	 */
	protected int getOffset(HttpServletRequest request) {
		int offset = Constants.DEFAULT_OFFSET;
		try {
			offset = Integer.parseInt(request.getParameter(Constants.OFFSET_PARAM));
		} catch(Exception e) {
			offset = Constants.DEFAULT_OFFSET_WITH_NOPAGING;
		}
		return offset;
	}

	/**
	 * 从request中读取每页记录数，用于分页
	 * 
	 * @param request
	 * @return
	 */
	protected int getPageSize(HttpServletRequest request) {
		int limit = Constants.DEFAULT_PAGESIZE;
		try {
			limit = Integer.parseInt(request.getParameter(Constants.PAGE_SIZE_PARAM));
		} catch(Exception e) {
			limit = Constants.DEFAULT_PAGESIZE_WITH_NOPAGING;
		}
		return limit;
	}

	/**
	 * 页面发送到参数如： GRID_QUERY_FIELDS :
	 * ["appCode","appName","isDisplay","isDisabled"] GRID_QUERY_KEYWORD : admin
	 * 返回的结果如： (appCode like 'admin%' or appName like 'admin%')
	 * 该函数根据其field类型，返回组装后的sql查询条件 注意fields query是关键字
	 * 
	 * @param request
	 * @return
	 */
	protected String getGridQueryCondition(HttpServletRequest request, Class<?> clazz) {
		return ControllerHelper.getGridQueryCondition(request, clazz, null);
	}

	/**
	 * 根据参数返回order by子句, <br/>
	 * XXX 不要继承该方法，直接在service中继承buildLoadDataOrderBy
	 * 
	 * @param request
	 * @return
	 */
	protected final String getOrderBy(HttpServletRequest request, Class<?> clazz) {
		return ControllerHelper.getOrderBy(request, clazz);
	}

	/**
	 * 获取RequestMapping注解的value，既url
	 * 
	 * @return 如果没有则返回空串，以便拼接url时不会出现null
	 */
	public String getRequestMappingValue() {
		Object[] objArr = this.getClass().getDeclaredAnnotations();
		for(Object o : objArr) {
			if(o instanceof RequestMapping) {
				RequestMapping rm = (RequestMapping) o;
				String[] arr = rm.value();
				if(arr.length == 1) {
					return arr[0];
				}
			}
		}
		return "";
	}

	@ExceptionHandler({ Exception.class })
	public ModelAndView handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
		String path = request.getServletPath();
		logger.error("Can't accomplish the request because an unexpected error when access to: " + path, ex);
		return handlerInternalException(path, ex, request, response);
	}

	protected ModelAndView handlerInternalException(String path, Exception ex, HttpServletRequest request,
			HttpServletResponse response) {
		if((ex instanceof JsonException) || (path != null && path.endsWith(".json"))) {
			String template = "{\"success\":false,\"msg\":\"$msg\",\"type\":\"$type\"}";
			try {
				response.setCharacterEncoding("UTF-8");
				String errorMsg = ex.getMessage();
				if(errorMsg != null) {
					errorMsg = errorMsg.replace("\\r", "");
					errorMsg = errorMsg.replace("\\n", "<BR>");
					errorMsg = errorMsg.replace("\r", "");
					errorMsg = errorMsg.replace("\n", "<BR>");
					errorMsg = errorMsg.replace("\"", "\\\\\"");
					if(errorMsg.endsWith("<BR>")) {
						errorMsg = errorMsg.substring(0, errorMsg.length() - 4);
					}
				}
				template = template.replaceAll("\\$msg", (ex != null && errorMsg != null ? errorMsg : "系统发生未知错误！"));
				if(ex instanceof BusiAlertException) {
					template = template.replaceAll("\\$type", "2");// 业务严重错误，页面使用alert提示
				} else if(ex instanceof BusiException) {
					template = template.replaceAll("\\$type", "1");// 业务错误，页面使用警告提示
				} else {
					template = template.replaceAll("\\$type", "0");
				}
				response.getWriter().print(template);
			} catch(Exception e) {
				logger.error("", e);
			}
			return null;
		} else {
			if(request.getAttribute("javax.servlet.error.message") == null) {
				request.setAttribute("javax.servlet.error.message", ex.getMessage());
			}
			if(ex.getCause() != null) {
				request.setAttribute("javax.servlet.error.cause", ex.getCause().getMessage());
			}

			return new ModelAndView("/WEB-INF/jsp/system/error.jsp");
		}
	}

	/**
	 * 输出流 向请求输出响应流
	 * 
	 * @param response
	 * @param xdoc
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	protected void writeHtmlStream(HttpServletResponse response, String htmlStr) throws IOException,
			UnsupportedEncodingException {
		write(response, HTML_CONTENT_TYPE, htmlStr);
	}

	protected void write(HttpServletResponse response, String contentType, String str) throws IOException,
			UnsupportedEncodingException {
		response.reset();
		response.setContentType(contentType);
		/* 创建输出流 */
		ServletOutputStream servletOS = response.getOutputStream();
		InputStream inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
		byte[] buf = new byte[1024];
		int readLength;
		while(((readLength = inputStream.read(buf)) != -1)) {
			servletOS.write(buf, 0, readLength);
		}
		inputStream.close();
		servletOS.flush();
		servletOS.close();
	}
}
