package com.tms.web.attach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.SuperVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.constants.BillTypeConst;
import com.tms.constants.FunConst;
import com.tms.service.attach.AttachService;
import com.tms.vo.attach.AttachmentVO;

import net.sf.jasperreports.crosstabs.fill.calculation.ArbitraryRankComparator;

/**
 * 附件管理
 * 
 * @author yaojiie
 * @date 2015-11-24 下午14:42:50
 */
@Controller
@RequestMapping(value = "/attach/attach")
public class AttachController extends AbsToftController {

	@Autowired
	private AttachService attachService;

	public AttachService getService() {
		return attachService;
	}

	//yaojiie 2015 11 24 加载数据
	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		//String funCode = paramVO.getFunCode();
		String funCode = request.getParameter("funCode");
		String pk_bill = request.getParameter("pk_bill");
		List<String> pks = new ArrayList<String>(); 
		String bill_type = request.getParameter("billtype");
		String extendCond = "pk_billno=? and bill_type=? ";
	
		//签单和回单
		if(funCode.equals(FunConst.SIGN_ATTACH_CODE)){
			pks.add(request.getParameter("pk_invoice"));
			request.getParameter("pk_invoice");
			bill_type = BillTypeConst.POD_SIGN;
		}else if(funCode.equals(FunConst.RECEIPT_ATTACH_CODE)){
			pks.add(request.getParameter("pk_invoice"));
			bill_type = BillTypeConst.POD_RECEIPT;
		}
		//异常跟踪
		if(funCode.equals(FunConst.TRACK_ATTACH_CODE)){
			pks.addAll(Arrays.asList(request.getParameterValues("pk_entrust")));
			String pkCond = NWUtils.buildConditionString(request.getParameterValues("pk_entrust"));
			extendCond = "bill_type=? and pk_billno in "+ pkCond;
			bill_type = BillTypeConst.TRACKING;
		}
		//异常附件
		if(funCode.equals(FunConst.EXP_ATTACH_CODE)){
			pks.add(request.getParameter("pk_exp_accident"));
			bill_type = BillTypeConst.EXP;
		}
		
		//应收明细
		if(funCode.equals(FunConst.RD_ATTACH_CODE)){
			pks.add(request.getParameter("pk_receive_detail"));
			bill_type = BillTypeConst.RD;
		}
		//应付明细
		if(funCode.equals(FunConst.PD_ATTACH_CODE)){
			pks.add(request.getParameter("pk_pay_detail"));
			bill_type = BillTypeConst.PD;
		}
		//应收对账
		if(funCode.equals(FunConst.RCS_ATTACH_CODE)){
			pks.add(request.getParameter("pk_rece_check_sheet"));
			bill_type = BillTypeConst.RCS;
		}
		//应付对账
		if(funCode.equals(FunConst.PCS_ATTACH_CODE)){
			pks.add(request.getParameter("pk_pay_check_sheet"));
			bill_type = BillTypeConst.PCS;
		}
		//站內信
		if(funCode.equals(FunConst.SMS_FUN_CODE)){
			pks.add(request.getParameter("pk_sms"));
			bill_type = "SMS";
		}
		
//		//加油管理
//		if(funCode.equals(FunConst.FLEET_REFUEL_CODE)){
//			pks.add(request.getParameter("pk_bill"));
//			bill_type = "REF";
//		}
		
		if((pks == null || pks.size() == 0) && StringUtils.isBlank(pk_bill)) {
			throw new BusiException("查询附件时,需要选择一条单据！");
		}
		if(StringUtils.isBlank(pk_bill)){
			pk_bill = pks.get(0);
		}
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		PaginationVO paginationVO =  null;
		if(bill_type.equals(BillTypeConst.TRACKING)){
			paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, extendCond,bill_type);
		}else{
			paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, extendCond,pk_bill,bill_type);
		}
		
		return this.genAjaxResponse(true, null, paginationVO);
	}


	//yaojiie 2015 11 23 添加签单附件上传功能
	@RequestMapping(value = "/uploadAttachment.do")
	@ResponseBody
	public void uploadAttachment (HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType(HTML_CONTENT_TYPE);
		ParamVO paramVO = this.getParamVO(request);
		List<String> pks = new ArrayList<String>(); 
		String funCode = request.getParameter("funCode");
		String pk_bill = request.getParameter("pk_bill");
		String bill_type = request.getParameter("billtype");
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		if(funCode.equals(FunConst.SIGN_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_invoice"));
			pks.add(request.getParameter("pk_invoice"));
			bill_type = BillTypeConst.POD_SIGN;
		}else if(funCode.equals(FunConst.RECEIPT_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_invoice"));
			pks.add(request.getParameter("pk_invoice"));
			bill_type = BillTypeConst.POD_RECEIPT;
		}
		// 判断异常跟踪附件上传 2015-12-08 yaojiie
		if(funCode.equals(FunConst.TRACK_ATTACH_CODE)){
			String pk = request.getParameter("pk_entrust");
			String[] pksArr = pk.split("\\" + Constants.SPLIT_CHAR);
			pks.addAll(Arrays.asList(pksArr));
			bill_type = BillTypeConst.TRACKING;
		}
		//yaojiie 2016 1 15 异常附件判断
		if(funCode.equals(FunConst.EXP_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_exp_accident"));
			pks.add(request.getParameter("pk_exp_accident"));
			bill_type = BillTypeConst.EXP;
		}
		
		//应收明细
		if(funCode.equals(FunConst.RD_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_receive_detail"));
			pks.add(request.getParameter("pk_receive_detail"));
			bill_type = BillTypeConst.RD;
		}
		//应付明细
		if(funCode.equals(FunConst.PD_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_pay_detail"));
			pks.add(request.getParameter("pk_pay_detail"));
			bill_type = BillTypeConst.PD;
		}
		//应收对账
		if(funCode.equals(FunConst.RCS_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_rece_check_sheet"));
			pks.add(request.getParameter("pk_rece_check_sheet"));
			bill_type = BillTypeConst.RCS;
		}
		//应付对账
		if(funCode.equals(FunConst.PCS_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_pay_check_sheet"));
			pks.add(request.getParameter("pk_pay_check_sheet"));
			bill_type = BillTypeConst.PCS;
		}
		//应付对账
		if(funCode.equals(FunConst.PCS_ATTACH_CODE)){
			paramVO.setBillId(request.getParameter("pk_sms"));
			pks.add(request.getParameter("pk_sms"));
			bill_type = "SMS";
		}
		
		if((pks == null || pks.size() == 0) && StringUtils.isBlank(pk_bill)) {
			throw new BusiException("单据号不能为空！");
		}
		paramVO.setBillType(bill_type);
		if(pks.size() == 0){
			pks.add(pk_bill);
		}
		List<MultipartFile> fileAry = mRequest.getFiles("userfile");
		this.getService().uploadAttachment(fileAry, paramVO,pks);
		this.writeHtmlStream(response, "{'msg':'文件上传成功!','success':'true'}");
	}

	//yaojiie 2015 11 23 签收回单下载
	@RequestMapping(value = "/downloadAttachment.do")
	public void downloadAttachment(HttpServletRequest request, HttpServletResponse response){
		String pk_attachment = request.getParameter("pk_attachment");
		if(StringUtils.isBlank(pk_attachment)) {
			throw new BusiException("查看附件时主键参数不能为空！");
		}
		AttachmentVO attachmentVO = NWDao.getInstance().queryByCondition(AttachmentVO.class, "pk_attachment = ?", pk_attachment);
		if(attachmentVO == null){
			throw new BusiException("您查看的文件不存在！");
		}
		try {
			// 设置下载方式
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			String userAgent = request.getHeader("User-Agent").toLowerCase();
			response.setHeader("Content-Disposition","attachment;filename=\"" + NWUtils.getDownloadFileName(attachmentVO.getAttachment_name(), userAgent) + "\"");
			this.getService().downloadAttachment(attachmentVO, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！",e.getMessage());
		}
	}

	//yaojiie 2015 11 23 签收回单批量下载
	@RequestMapping(value = "/zipAttachmentDownload")
	public void zipAttachmentDownload(HttpServletRequest request, HttpServletResponse response){
		String pk_attachment = request.getParameter("pk_attachment");
		if(StringUtils.isBlank(pk_attachment)) {
			throw new BusiException("批量下载附件时主键参数不能为空！");
		}
		String[] pkAry = pk_attachment.split(",");
		String fileName = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		fileName = fileName + ".zip";
		try {
			// 设置下载方式
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
			// 批量下载
			this.getService().zipAttachmentDownload(pkAry, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！",e.getMessage());
		}
	}
	
	//yaojiie 2015 11 23 附件预览
	@RequestMapping(value = "/previewAttachment.do")
	public void previewAttachment (HttpServletRequest request, HttpServletResponse response){
		String pk_attachment = request.getParameter("pk_attachment");
		if(StringUtils.isBlank(pk_attachment)) {
			throw new BusiException("预览附件时主键参数不能为空！");
		}
		AttachmentVO attachmentVO = NWDao.getInstance().queryByCondition(AttachmentVO.class, "pk_attachment = ?", pk_attachment);
		if(attachmentVO == null){
			throw new BusiException("您查看的文件不存在！");
		}
		try {
			String fileName = attachmentVO.getFile_name();
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);// 后缀
			if("png".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext)|| "bmp".equalsIgnoreCase(ext) || "gif".equalsIgnoreCase(ext)) {
				response.setContentType("image/" + ext);
			} else if("pdf".equalsIgnoreCase(ext)) {
				response.setContentType("application/pdf");
			} else {
				response.setContentType("text/html;utf-8");
			}

			response.setCharacterEncoding("UTF-8");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			this.getService().previewAttachment(attachmentVO, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！",e.getMessage());
		}
	}
	
	
	//yaojiie 2015 11 25 增加删除功能，重写删除方法，避免物理删除
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/deleteAttachment.json")
	@ResponseBody
	public Map<String, Object> deleteAttachment(HttpServletRequest request, HttpServletResponse response) {
		String[] pks = request.getParameterValues("billId");
		SuperVO parentVO = (SuperVO) this.getService().getBillInfo().getParentVO();
		try {
			Class parentClass = (Class<? extends SuperVO>) Class.forName(parentVO.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			this.getService().deleteAttachment(parentClass, pks);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return this.genAjaxResponse(true, null, pks);
	}


}
