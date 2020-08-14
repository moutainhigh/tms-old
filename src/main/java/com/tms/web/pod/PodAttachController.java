package com.tms.web.pod;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.service.pod.PodAttachService;
import com.tms.vo.pod.PodAttachVO;

/**
 * 上传pod签收单
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
@Controller
@RequestMapping(value = "/pod/at")
public class PodAttachController extends AbsToftController {

	@Autowired
	private PodAttachService podAttachService;

	public PodAttachService getService() {
		return podAttachService;
	}

	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		String pk_invoice = request.getParameter("pk_invoice");
		if(StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("查询POD签收单时需要选择一条发货单！");
		}
		String extendCond = "pk_invoice=?";
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		PaginationVO paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, extendCond,
				pk_invoice);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 上传POD签收单
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/uploadPod.do")
	public void uploadPod(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		String pk_invoice = request.getParameter("pk_invoice");
		if(StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("参数发货单pk不能为空！");
		}
		List<MultipartFile> fileAry = mRequest.getFiles("userfile");
		List<PodAttachVO> attachVOs = new ArrayList<PodAttachVO>();
		List<InputStream> inAry = new ArrayList<InputStream>();
		for(MultipartFile file : fileAry) {
			String fileName = file.getOriginalFilename();
			if(StringUtils.isBlank(fileName)) {
				continue;
			}
			PodAttachVO attachVO = new PodAttachVO();
			attachVO.setPk_invoice(pk_invoice);
			attachVO.setPk_pod_attach(UUID.randomUUID().toString());
			attachVO.setDr(0);
			attachVO.setTs(new UFDateTime(new Date()));
			attachVO.setFile_name(fileName);
			attachVO.setFile_size(file.getSize());
			int index = fileName.indexOf(".");
			if(index != -1 && index != fileName.length() - 1) {
				attachVO.setSuffix(fileName.substring(fileName.lastIndexOf(".") + 1));
			}
			attachVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			attachVO.setCreate_time(new UFDateTime(new Date()));
			attachVOs.add(attachVO);
			inAry.add(file.getInputStream());
		}
		this.getService().uploadPodAttach(attachVOs, inAry);
		this.writeHtmlStream(response, "{'msg':'文件上传成功!','success':'true'}");
	}

	@RequestMapping(value = "/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		String pk_pod_attach = request.getParameter("pk_pod_attach");
		if(StringUtils.isBlank(pk_pod_attach)) {
			throw new BusiException("查看签收单附件时主键参数不能为空！");
		}
		PodAttachVO attachVO = null;
		try {
			attachVO = this.getService().getByPrimaryKey(pk_pod_attach);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(attachVO == null) {
			throw new BusiException("您查看的文件不存在！");
		}
		attachVO = NWDao.getInstance().queryByCondition(PodAttachVO.class, "pk_pod_attach=?", pk_pod_attach);
		try {
			// 设置下载方式
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			String userAgent = request.getHeader("User-Agent").toLowerCase();
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + NWUtils.getDownloadFileName(attachVO.getFile_name(), userAgent) + "\"");
			this.getService().downloadPodAttach(pk_pod_attach, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！",e.getMessage());
		}
	}

	/**
	 * 打包下载
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/zipDownload.do")
	public void zipDownload(HttpServletRequest request, HttpServletResponse response) {
		String pk_pod_attach = request.getParameter("pk_pod_attach");
		if(StringUtils.isBlank(pk_pod_attach)) {
			throw new BusiException("批量下载签收单附件时主键参数不能为空！");
		}
		String[] pkAry = pk_pod_attach.split(",");
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
			this.getService().zipDownload(pkAry, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！",e.getMessage());
		}
	}

	/**
	 * 预览
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/preview.do")
	public void preview(HttpServletRequest request, HttpServletResponse response) {
		String pk_pod_attach = request.getParameter("pk_pod_attach");
		if(StringUtils.isBlank(pk_pod_attach)) {
			throw new BusiException("预览附件时主键参数不能为空！");
		}
		PodAttachVO attachVO = null;
		try {
			attachVO = this.getService().getByPrimaryKey(pk_pod_attach);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(attachVO == null) {
			throw new BusiException("您查看的文件不存在！");
		}
		attachVO = NWDao.getInstance().queryByCondition(PodAttachVO.class, "pk_pod_attach=?", pk_pod_attach);
		try {
			String fileName = attachVO.getFile_name();
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);// 后缀
			if("png".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext)
					|| "bmp".equalsIgnoreCase(ext) || "gif".equalsIgnoreCase(ext)) {
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
			this.getService().preview(pk_pod_attach, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！",e.getMessage());
		}
	}
}
