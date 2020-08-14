package org.nw.web.portlet;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.service.sys.DocumentService;
import org.nw.utils.NWUtils;
import org.nw.vo.sys.DocumentVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 文档管理，政策文件管理
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:28:07
 */
@Controller
@RequestMapping(value = "/c/doc")
public class DocumentViewController extends AbsToftController {

	@Autowired
	private DocumentService documentService;

	public DocumentService getService() {
		return documentService;
	}

	protected String getFunHelpName(String funCode) {
		return "/default/more/doc.jsp";
	}

	/**
	 * 查询最新的5条记录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getTop5.html")
	public ModelAndView getTop5(HttpServletRequest request, HttpServletResponse response) {
		List<DocumentVO> vos = this.getService().getTop5();
		request.setAttribute("dataList", vos);
		return new ModelAndView("/default/autoLoad/doc.jsp");
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
