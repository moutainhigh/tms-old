package org.nw.web.sys.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.service.sys.SmsService;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.SmsVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.service.attach.AttachService;

/**
 * 站内信服务
 * 
 * @author xuqc
 * @date 2013-7-1 上午11:30:41
 */
@Controller
@RequestMapping(value = "/common/sms")
public class SmsController extends AbsToftController {
	
	@Autowired
	private AttachService attachService;

	@Autowired
	private SmsService smsService;

	public SmsService getService() {
		return smsService;
	}
	
	
	/**
	 * 查看动作，跳转到查看页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getFunVO.json")
	@ResponseBody
	public FunVO getFunVO(HttpServletRequest request, HttpServletResponse response) {
		String fun_code = request.getParameter("fun_code");
		if(StringUtils.isBlank(fun_code)) {
			return null;
		}
		return this.getService().getFunVOByFunCode(fun_code);
	}

	/**
	 * 查看动作，跳转到查看页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getByPK.json")
	@ResponseBody
	public Map<String, Object> getByPK(HttpServletRequest request, HttpServletResponse response) {
		String pk_sms = request.getParameter("pk_sms");
		if(StringUtils.isBlank(pk_sms)) {
			throw new BusiException("主键不能为空！");
		}
		this.getService().updateReadFlag(pk_sms);
		return this.genAjaxResponse(true, null, this.getService().getByPK(pk_sms));
	}
	
	/**
	 * 异常事故上传附件
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/uploadSmsAttach.json")
	public void uploadSmsAttach(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		MultipartFile file1 = mRequest.getFile("pk_filesystem1");
		MultipartFile file2 = mRequest.getFile("pk_filesystem2");
		MultipartFile file3 = mRequest.getFile("pk_filesystem3");
		if(file1 == null && file2 == null && file3 == null) {
			throw new BusiException("上传文件不能为空！");
		}
		MultipartFile file = null;
		String id = "";
		if(file1 != null ){
			file = file1;
			id = "pk_filesystem1";
		}
		if(file2 != null ){
			file = file2;
			id = "pk_filesystem2";
		}
		if(file3 != null ){
			file = file3;
			id = "pk_filesystem3";
		}
		String pkAtta = attachService.uploadSmsAttachment(file);
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(id, pkAtta);
		retMap.put("msg", "操作成功!");
		retMap.put("success", "true");
		retMap.put("data", dataMap);
		this.writeHtmlStream(response, JacksonUtils.writeValueAsString(retMap));
	}

	/**
	 * 发送消息
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/send.json")
	@ResponseBody
	public Map<String, Object> send(HttpServletRequest request, HttpServletResponse response) {
		String receiver = request.getParameter("receiver");// 如果是多个receiver使用逗号分开
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String billids = request.getParameter("billids");
		String billnos = request.getParameter("billnos");
		String fun_code = request.getParameter("fun_code");
		String[] pk_attas = request.getParameterValues("pk_attas");
		UFBoolean upload_flag = new UFBoolean(request.getParameter("upload_flag"));
		if(StringUtils.isBlank(title)) {
			throw new BusiException("站内信的标题不能为空！");
		}

		SmsVO vo = new SmsVO();
		vo.setReceiver(receiver);
		vo.setTitle(title);
		vo.setContent(content);
		vo.setBillids(billids);
		vo.setBillnos(billnos);
		vo.setFun_code(fun_code);
		vo.setUpload_flag(upload_flag);
		this.getService().doSend(vo,pk_attas);
		return this.genAjaxResponse(true, null, null);
	}
}
