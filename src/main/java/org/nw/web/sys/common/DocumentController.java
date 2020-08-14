package org.nw.web.sys.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.service.sys.DocumentService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.DocumentVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 文档管理，政策文件管理
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:28:07
 */
@Controller
@RequestMapping(value = "/common/doc")
public class DocumentController extends AbsToftController {

	@Autowired
	private DocumentService documentService;

	public DocumentService getService() {
		return documentService;
	}

	/**
	 * 上传文档
	 * 
	 * @param request
	 * @param response
	 * @param docVO
	 * @throws Exception
	 */
	// @RequestMapping(value = "/saveDoc.do")
	// public void upload(HttpServletRequest request, HttpServletResponse
	// response, DocumentVO docVO) throws Exception {
	// response.setContentType(HTML_CONTENT_TYPE);
	// MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest)
	// request;
	// List<MultipartFile> fileAry = mRequest.getFiles("userfile");
	// InputStream inputStream = null;
	// if(fileAry != null && fileAry.size() > 0) {
	// MultipartFile file = fileAry.get(0);
	// inputStream = file.getInputStream();
	// String fileName = file.getOriginalFilename();
	// docVO.setFile_name(fileName);
	// docVO.setFile_size(file.getSize());
	// if(StringUtils.isBlank(docVO.getTitle())) {
	// docVO.setTitle(fileName);
	// }
	// }
	// docVO.setPk_document(UUID.randomUUID().toString());
	// docVO.setPost_date(new UFDate(new Date()));
	// docVO.setDr(0);
	// docVO.setTs(new UFDateTime(new Date()));
	// docVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
	// docVO.setCreate_time(new UFDateTime(new Date()));
	// this.getService().upload(docVO, inputStream);
	// this.writeHtmlStream(response, "{'msg':'文件上传成功!','success':'true'}");
	// }

	protected String getUploadField() {
		return "userfile";
	}

	public Map<String, Object> save(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeSave(billVO, paramVO);
		DocumentVO docVO = (DocumentVO) billVO.getParentVO();
		docVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(docVO);
		docVO.setDr(0);
		docVO.setTs(new UFDateTime(new Date()));
		if(StringUtils.isBlank(docVO.getTitle())) {
			docVO.setTitle(docVO.getFile_name());
		}
		InputStream inputStream = null;
		File file = null;
		if(StringUtils.isNotBlank(docVO.getUserfile())) {
			file = this.getUploadFile(request, docVO.getUserfile());
			if(file != null) {
				docVO.setFile_size(file.length());
				try {
					inputStream = new FileInputStream(file);
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		Map<String, Object> retMap = this.getService().upload(paramVO, docVO, inputStream);
		// 保存后,删除临时文件
		if(file != null) {
			file.delete();
		}
		return this.genAjaxResponse(true, null, retMap);
	}

	/**
	 * 下载文件
	 */
	@RequestMapping(value = "/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		String pk_document = request.getParameter("pk_document");
		if(StringUtils.isBlank(pk_document)) {
			throw new BusiException("查看文件时主键参数不能为空！");
		}
		DocumentVO docVO = null;
		try {
			docVO = this.getService().getByPrimaryKey(pk_document);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(docVO == null) {
			throw new BusiException("您查看的文件不存在！");
		}
		docVO = NWDao.getInstance().queryByCondition(DocumentVO.class, "pk_document=?", pk_document);
		try {
			// 设置下载方式
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			String userAgent = request.getHeader("User-Agent").toLowerCase();
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + NWUtils.getDownloadFileName(docVO.getFile_name(), userAgent) + "\"");
			this.getService().download(pk_document, response.getOutputStream());
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
		String pk_document = request.getParameter("pk_document");
		if(StringUtils.isBlank(pk_document)) {
			throw new BusiException("批量下载文件时主键参数不能为空！");
		}
		String[] pkAry = pk_document.split(",");
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
}
