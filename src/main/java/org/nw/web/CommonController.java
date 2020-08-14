package org.nw.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.nw.Global;
import org.nw.basic.util.Assert;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.FileUtils;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.service.impl.CommonServiceImpl;
import org.nw.vo.ParamVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 包括editor中的文件上传和下载
 * 
 * @author xuqc
 * @date 2012-7-3 下午04:34:14
 */
@Controller
public class CommonController extends AbsBaseController {
	private static final String LEGAL_IMAGE_STR = "gif|jpg|jpeg|png|bmp";
	private static final String LEGAL_ATTACH_STR = "txt|rar|zip|doc|docx|xls|xlsx|ppt|pptx|pdf";
	private static final String LEGAL_FLASH_STR = "swf";
	private static final String LEGAL_MEDIA_STR = "wmv|avi|rm|rmvb|mpeg|mpg|3gp|mov|mp3|mp4";
	private static final Pattern LEGAL_IMAGE_PATTERN = Pattern.compile("^.*?\\.(" + LEGAL_IMAGE_STR + ")$");
	private static final Pattern LEGAL_ATTACH_PATTERN = Pattern.compile("^.*?\\.(" + LEGAL_ATTACH_STR + ")$");
	private static final Pattern LEGAL_FLASH_PATTERN = Pattern.compile("^.*?\\.(" + LEGAL_FLASH_STR + ")$");
	private static final Pattern LEGAL_MEDIA_PATTERN = Pattern.compile("^.*?\\.(" + LEGAL_MEDIA_STR + ")$");

	public CommonServiceImpl getService() {
		return new CommonServiceImpl();
	}

	/**
	 * 返回没有权限的页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/forbidden.html")
	public ModelAndView forbidden(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("errorMsg", request.getParameter("errorMsg"));
		return new ModelAndView("/WEB-INF/jsp/system/forbidden.jsp");
	}

	@RequestMapping(value = "/env.html")
	public ModelAndView env(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/WEB-INF/jsp/system/env.jsp");
	}

	/**
	 * 编辑器中上传附件
	 * 
	 * @param request
	 * @param response
	 * @param imgFile
	 *            这里实际上是附件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/js/editor/uploadAttach.do")
	public void uploadAttach(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(HTML_CONTENT_TYPE);// 必须设置，否则客户端识别不了

		if(!ServletFileUpload.isMultipartContent(request)) {
			throw new JsonException("必须是 multipart/form-data 类型数据！");
		}

		String path = Global.uploadDir + File.separator + "editor" + File.separator + "attach" + File.separator;
		File pathFile = new File(path);
		// 如果不存在目录，则自动创建
		if(!pathFile.exists()) {
			pathFile.mkdirs();
		}

		// 生成一个唯一id，这个id将作为文件名，这里为了避免安全问题等，将不加文件扩展名，至于原始文件名，则有业务代码自己去控制保存
		String fileId = UUID.randomUUID().toString();

		// 创建 DiskFileItemFactory 对象
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// factory.setRepository(tempDir);可以设置缓存目录
		factory.setSizeThreshold(1024 * 1024 * 10);// 超过10M的数据采用临时文件缓存

		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");// 设置普通字段名称和文件字段的文件名所采用的字符集编码
		upload.setFileSizeMax(Global.getIntValue("upload.image.size") * 1024 * 1024);// 单个文件大小,10M
		upload.setSizeMax(-1);// 依次最多上传的文件大小

		List<FileItem> list = null;
		try {
			// 解析request对象 得到一个FileItem对象
			list = upload.parseRequest(request);
		} catch(FileUploadException e) {
			if(e instanceof FileUploadBase.FileSizeLimitExceededException) {
				throw new JsonException("解析上传文件时出错，错误信息：单个文件大小不能超过[?M]",Global.getIntValue("upload.image.size") + "");
			} else {
				throw new JsonException("解析上传文件时出错，错误信息[?]！",e,e.getMessage());
			}
		}
		String dir = request.getParameter("dir");// 上传的类型:附件、flash、音视频
		Pattern pattern;
		String message;
		if(dir.equals("flash")) {
			// 上传flash
			pattern = LEGAL_FLASH_PATTERN;
			message = LEGAL_FLASH_STR;
		} else if(dir.equals("media")) {
			// 上传视频音频
			pattern = LEGAL_MEDIA_PATTERN;
			message = LEGAL_IMAGE_STR;
		} else {
			// 附件上传
			pattern = LEGAL_ATTACH_PATTERN;
			message = LEGAL_ATTACH_STR;
		}
		if(list != null && list.size() > 0) {
			for(FileItem fileItem : list) {
				// 得到文件表单的名称
				String name = fileItem.getFieldName();
				if(!fileItem.isFormField()) {
					if(name.equals("file") || name.equals("imgFile")) {
						// 得到文件表单的值，就是用户本地的文件路径
						String pathStr = fileItem.getName();

						if(pathStr.trim().equals("")) {
							// 如果文件表单为空，则不处理
							continue;
						} else {
							// 判断扩展名
							Matcher matcher = pattern.matcher(pathStr.toLowerCase());
							if(!matcher.find()) {
								throw new JsonException("附件格式必须为[?]！",message);
							}
						}

						/**
						 * 文件处理
						 */
						Map<String, String> result = new HashMap<String, String>();
						try {
							fileItem.write(new File(path + fileId));

							String originalFileName = pathStr.substring(pathStr.lastIndexOf("\\") + 1);

							// 文件大小和文件名称
							logger.info("originalFileName:" + originalFileName + ",size: " + fileItem.getSize()
									+ ",type:" + fileItem.getContentType());

							String accessUrl = request.getContextPath() + getRequestMappingValue()
									+ "/js/editor/downloadAttach.do";
							accessUrl = accessUrl + "?id=" + fileId + "&fileName="
									+ java.net.URLEncoder.encode(fileItem.getName(), "UTF-8");

							result.put("success", "true");
							result.put("fileId", fileId);
							result.put("title", originalFileName);
							result.put("time", DateUtils.getCurrentDate(DateUtils.DATETIME_FORMAT_HORIZONTAL));
							result.put("url", accessUrl);
							this.writeHtmlStream(response, JacksonUtils.writeValueAsString(result));
						} catch(Exception e) {
							logger.error("文件上传出错!", e);
							throw new JsonException("文件上传出错!", e);
						} finally {
							fileItem.delete();
						}
					}
				}
			}
		} else {
			throw new JsonException("没有找到文件！");
		}
	}

	/**
	 * 编辑器中上传附件
	 * 
	 * @param request
	 * @param response
	 * @param file
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/js/editor/uploadImage.do")
	public void uploadImage(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(HTML_CONTENT_TYPE);// 必须设置，否则客户端识别不了

		if(!ServletFileUpload.isMultipartContent(request)) {
			throw new JsonException("必须是 multipart/form-data 类型数据！");
		}

		String path = Global.uploadDir + File.separator + "editor" + File.separator + "image" + File.separator;
		File pathFile = new File(path);
		// 如果不存在目录，则自动创建
		if(!pathFile.exists()) {
			pathFile.mkdirs();
		}

		// 生成一个唯一id，这个id将作为文件名，这里为了避免安全问题等，将不加文件扩展名，至于原始文件名，则有业务代码自己去控制保存
		String fileId = UUID.randomUUID().toString();

		// 创建 DiskFileItemFactory 对象
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// factory.setRepository(tempDir);可以设置缓存目录
		factory.setSizeThreshold(1024 * 1024 * 2);// 超过2M的数据采用临时文件缓存

		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");// 设置普通字段名称和文件字段的文件名所采用的字符集编码
		upload.setFileSizeMax(Global.getIntValue("upload.image.size") * 1024 * 1024);// 单个文件大小,10M
		upload.setSizeMax(-1);// 依次最多上传的文件大小

		List<FileItem> list = null;
		try {
			// 解析request对象 得到一个FileItem对象
			list = upload.parseRequest(request);
		} catch(FileUploadException e) {
			if(e instanceof FileUploadBase.FileSizeLimitExceededException) {
				throw new JsonException("解析上传文件时出错，错误信息：单个文件大小不能超过[?M]",Global.getIntValue("upload.image.size") + "");
			} else {
				throw new JsonException("解析上传文件时出错，错误信息[?]",e,e.getMessage());
			}
		}

		if(list != null && list.size() > 0) {
			for(FileItem fileItem : list) {
				if(!fileItem.isFormField()) {
					// 得到文件表单的名称
					String name = fileItem.getFieldName();
					if(name.equals("file") || name.equals("imgFile")) {
						// 得到文件表单的值，就是用户本地的文件路径
						String pathStr = fileItem.getName();

						if(pathStr.trim().equals("")) {
							// 如果文件表单为空，则不处理
							continue;
						} else {
							// 判断扩展名
							Matcher matcher = LEGAL_IMAGE_PATTERN.matcher(pathStr.toLowerCase());
							if(!matcher.find()) {
								throw new JsonException("图片格式必须为[?]等!",LEGAL_IMAGE_STR);
							}
						}

						/**
						 * 文件处理
						 */
						Map<String, Object> result = new HashMap<String, Object>();
						try {
							fileItem.write(new File(path + fileId));

							String originalFileName = pathStr.substring(pathStr.lastIndexOf("\\") + 1);

							// 文件大小和文件名称
							logger.info("originalFileName:" + originalFileName + ",size: " + fileItem.getSize()
									+ ",type:" + fileItem.getContentType());

							String accessUrl = request.getContextPath() + getRequestMappingValue()
									+ "/js/editor/downloadImage.do";
							accessUrl = accessUrl + "?id=" + fileId + "&fileName="
									+ java.net.URLEncoder.encode(fileItem.getName(), "UTF-8");

							result.put("error", 0);
							result.put("success", "true");
							result.put("fileId", fileId);
							result.put("title", originalFileName);
							result.put("time", DateUtils.getCurrentDate(DateUtils.DATETIME_FORMAT_HORIZONTAL));
							result.put("url", accessUrl);
							this.writeHtmlStream(response, JacksonUtils.writeValueAsString(result));
						} catch(Exception e) {
							logger.error("文件上传出错!", e);
							throw new JsonException("文件上传出错!", e);
						} finally {
							fileItem.delete();
						}
					}
				}
			}
		} else {
			throw new JsonException("没有找到文件！");
		}
	}

	/**
	 * 下载编辑器上传的附件
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            通常使用uuid，这是在文件上传的时候定的
	 * @return
	 */
	@RequestMapping(value = "/js/editor/downloadAttach.do")
	public void downloadAttach(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		String fileName = request.getParameter("fileName");
		try {
			fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String path = Global.uploadDir + File.separator + "editor" + File.separator + "attach" + File.separator;
		doDownload(request, response, path, id, fileName);
	}

	/**
	 * 下载编辑器上传的图片
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            通常使用uuid，这是在文件上传的时候定的
	 * @return
	 */
	@RequestMapping(value = "/js/editor/downloadImage.do")
	public void downloadImage(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		String fileName = request.getParameter("fileName");
		String path = Global.uploadDir + File.separator + "editor" + File.separator + "image" + File.separator;
		doDownload(request, response, path, id, fileName);
	}

	/**
	 * 文件下载动作
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @param fileName
	 * @author xuqc
	 * @date 2011-11-18
	 */
	private void doDownload(HttpServletRequest request, HttpServletResponse response, String path, String id,
			String fileName) {
		Assert.notNull(id);

		if(fileName == null) {
			fileName = id;
		}

		// 设置下载方式
		response.setContentType("application/octet-stream");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		// 文件读取并输出
		File file = new File(path + id);
		if(file.exists()) {
			InputStream inputStream = null;
			try {
				response.setHeader("Content-Disposition", "attachment;filename=\""
						+ new String(fileName.getBytes(), "ISO-8859-1") + "\"");

				inputStream = new FileInputStream(new File(path + id));
				FileUtils.copy(inputStream, response.getOutputStream());
			} catch(Exception e) {
				logger.error("下载文件错误：" + id + "," + fileName, e);
				throw new BusiException("下载文件错误[?],[]", e,id,fileName);
			} finally {
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			throw new BusiException("文件[?],已不存在[]！",fileName,path + id);
		}
	}

	/**
	 * 读取自定义按钮的模板
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/common/loadCustomBtnTemplet.json")
	@ResponseBody
	public Map<String, Object> loadCustomBtnTemplet(HttpServletRequest request, HttpServletResponse response) {
		String pk_fun = request.getParameter("pk_fun");
		if(StringUtils.isBlank(pk_fun)) {
			throw new BusiException("pk_fun参数是必须的！");
		}
		return this.genAjaxResponse(true, null, this.getService().getCustomBtnTemplet(pk_fun));
	}

	/**
	 * 自定义按钮，调用存储过程
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/common/customButton.json")
	@ResponseBody
	public Map<String, Object> customButton(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_fun = request.getParameter("pk_fun");
		if(StringUtils.isBlank(pk_fun)) {
			throw new BusiException("pk_fun参数是必须的！");
		}
		String[] billId = request.getParameterValues("billId");
		if(billId == null || billId.length == 0) {
			throw new BusiException("billId是必须的！");
		}
		LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
		String pk_templet = request.getParameter("pk_templet");
		paramMap.put("returnCode", "");
		if(StringUtils.isBlank(pk_templet)) {
			new CommonServiceImpl().doCustomButton(paramVO, pk_fun, billId, paramMap);
		} else {
			UiBillTempletVO templetVO = this.getService().getBillTempletVO(pk_templet);
			List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
			for(BillTempletBVO fieldVO : fieldVOs) {
				paramMap.put(fieldVO.getItemkey(), request.getParameter(fieldVO.getItemkey()));
			}
			this.getService().doCustomButton(paramVO, pk_fun, billId, paramMap);
		}
		return this.genAjaxResponse(true, null, null);
	}
}
