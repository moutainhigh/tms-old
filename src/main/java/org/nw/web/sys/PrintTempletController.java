package org.nw.web.sys;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.jf.vo.PrintTempletVO;
import org.nw.service.sys.PrintTempletService;
import org.nw.utils.NWUtils;
import org.nw.vo.TreeVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 打印模板操作类
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
@Controller
@RequestMapping(value = "/sys/pt")
public class PrintTempletController extends AbsToftController {

	@Autowired
	private PrintTempletService printTempletService;

	public PrintTempletService getService() {
		return printTempletService;
	}

	/**
	 * 返回单据类型树，读取nw_fun中的所有单据类型
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getFunCodeTree.json")
	@ResponseBody
	public List<TreeVO> getBillTypeTree(HttpServletRequest request, HttpServletResponse response) {
		return this.getService().getFunCodeTree();
	}

	public String getTreePkField() {
		return "nodecode";
	}

	/**
	 * 上传打印的模板文件
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/upload.do")
	public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(HTML_CONTENT_TYPE);
		String billId = request.getParameter("billId");
		String nodecode = request.getParameter("nodecode");

		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		MultipartFile mFile = mRequest.getFile("file");// 先不支持一次上传多个模板，这里的fileAry肯定只有一个
		if(mFile == null) {
			throw new BusiException("请选择模板文件！");
		}
		String fileName = mFile.getOriginalFilename();
		PrintTempletVO attachVO;
		if(StringUtils.isBlank(billId)) {
			// 如果没有billId，说明是新增一个模板，此时需要选择节点编码
			if(StringUtils.isBlank(nodecode)) {
				throw new BusiException("节点编码不能为空！");
			}
			String vtemplatename = request.getParameter("vtemplatename");// 模板名称
			String vtemplatecode = request.getParameter("vtemplatecode");// 模板编码
			attachVO = new PrintTempletVO();
			attachVO.setPk_print_templet(UUID.randomUUID().toString());
			attachVO.setDr(0);
			attachVO.setTs(new UFDateTime(new Date()));
			if(StringUtils.isBlank(vtemplatename)) {// 如果没有传入,那么使用模板文件名
				int index = fileName.indexOf(".");
				if(index != -1) {
					vtemplatename = fileName.substring(0, index);
				}
			}
			attachVO.setVtemplatename(vtemplatename);
			attachVO.setVtemplatecode(vtemplatecode);
			attachVO.setNodecode(nodecode);
			attachVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			attachVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			attachVO.setCreate_time(new UFDateTime(new Date()));
			attachVO.setStatus(VOStatus.NEW);
		} else {
			// 根据主键查询attachVO
			attachVO = this.getService().getByPrimaryKey(billId);
			attachVO.setStatus(VOStatus.UPDATED);
		}
		attachVO.setFile_name(fileName);
		attachVO.setFile_size(mFile.getSize());
		this.getService().uploadTemplet(attachVO, mFile.getInputStream());
		this.writeHtmlStream(response, "{'msg':'模板上传成功!','success':'true'}");
	}

	/**
	 * 下载打印模板文件,注意下载的是上传的原始文件，而不是编译后的文件
	 */
	@RequestMapping(value = "/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		String pk_print_templet = request.getParameter("pk_print_templet");
		if(StringUtils.isBlank(pk_print_templet)) {
			throw new BusiException("查询打印模板时主键参数不能为空！");
		}
		PrintTempletVO attachVO = null;
		try {
			attachVO = this.getService().getByPrimaryKey(pk_print_templet);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(attachVO == null) {
			throw new BusiException("您查看的文件不存在！");
		}
		try {
			// 设置下载方式
			response.setContentType("application/xml");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			String userAgent = request.getHeader("User-Agent").toLowerCase();
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + NWUtils.getDownloadFileName(attachVO.getFile_name(), userAgent) + "\"");
			this.getService().downloadTemplet(pk_print_templet, response.getOutputStream());
		} catch(Exception e) {
			logger.error(e);
			throw new BusiException("文件下载出错,错误信息[?]！", e.getMessage());
		}
	}
}
