package org.nw.web.sys;

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
import org.nw.service.sys.FilesystemService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.FilesystemVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 单据附件处理
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/fs")
public class FilesystemController extends AbsToftController {

	@Autowired
	private FilesystemService filesystemService;

	public FilesystemService getService() {
		return filesystemService;
	}

	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		String billtype = request.getParameter("billtype");
		String pk_bill = request.getParameter("pk_bill");
		if(StringUtils.isBlank(billtype) || StringUtils.isBlank(pk_bill)) {
			throw new BusiException("查询单据附件时，单据类型和单据PK不能为空！");
		}
		String extendCond = "billtype=? and pk_bill=?";
		ParamVO paramVO = this.getParamVO(request);
		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		PaginationVO paginationVO = this.getService().loadData(paramVO, offset, pageSize, null, orderBy, extendCond,
				billtype, pk_bill);
		return this.genAjaxResponse(true, null, paginationVO);
	}

	/**
	 * 上传单据附件
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/upload.do")
	public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		String billtype = request.getParameter("billtype");
		String pk_bill = request.getParameter("pk_bill");
		if(StringUtils.isBlank(billtype) || StringUtils.isBlank(pk_bill)) {
			throw new BusiException("上传单据附件时，单据类型和单据PK不能为空！");
		}
		List<MultipartFile> fileAry = mRequest.getFiles("userfile");
		List<FilesystemVO> attachVOs = new ArrayList<FilesystemVO>();
		List<InputStream> inAry = new ArrayList<InputStream>();
		for(MultipartFile file : fileAry) {
			String fileName = file.getOriginalFilename();
			if(StringUtils.isBlank(fileName)) {
				continue;
			}
			FilesystemVO attachVO = new FilesystemVO();
			attachVO.setBilltype(billtype);
			attachVO.setPk_bill(pk_bill);
			attachVO.setPk_filesystem(UUID.randomUUID().toString());
			attachVO.setDr(0);
			attachVO.setTs(new UFDateTime(new Date()));
			attachVO.setFile_name(fileName);
			attachVO.setFile_size(file.getSize());
			attachVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			attachVO.setCreate_time(new UFDateTime(new Date()));
			attachVOs.add(attachVO);
			inAry.add(file.getInputStream());
		}
		this.getService().upload(attachVOs, inAry);
		this.writeHtmlStream(response, "{'msg':'文件上传成功!','success':'true'}");
	}

	@RequestMapping(value = "/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		String pk_filesystem = request.getParameter("pk_filesystem");
		if(StringUtils.isBlank(pk_filesystem)) {
			throw new BusiException("查看附件时主键参数不能为空！");
		}
		FilesystemVO attachVO = null;
		try {
			attachVO = this.getService().getByPrimaryKey(pk_filesystem);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(attachVO == null) {
			throw new BusiException("您查看的文件不存在！");
		}
		attachVO = NWDao.getInstance().queryByCondition(FilesystemVO.class, "pk_filesystem=?", pk_filesystem);
		try {
			// 设置下载方式
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			String userAgent = request.getHeader("User-Agent").toLowerCase();
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + NWUtils.getDownloadFileName(attachVO.getFile_name(), userAgent) + "\"");
			this.getService().download(pk_filesystem, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！", e.getMessage());
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
		String pk_filesystem = request.getParameter("pk_filesystem");
		if(StringUtils.isBlank(pk_filesystem)) {
			throw new BusiException("批量下载附件时主键参数不能为空！");
		}
		String[] pkAry = pk_filesystem.split(",");
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
			throw new BusiException("文件下载出错,错误信息[?]！", e.getMessage());
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
		String pk_filesystem = request.getParameter("pk_filesystem");
		if(StringUtils.isBlank(pk_filesystem)) {
			throw new BusiException("预览附件时主键参数不能为空！");
		}
		FilesystemVO attachVO = null;
		try {
			attachVO = this.getService().getByPrimaryKey(pk_filesystem);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(attachVO == null) {
			throw new BusiException("您预览的文件不存在！");
		}
		attachVO = NWDao.getInstance().queryByCondition(FilesystemVO.class, "pk_filesystem=?", pk_filesystem);
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
			this.getService().preview(pk_filesystem, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！", e.getMessage());
		}
	}
}
