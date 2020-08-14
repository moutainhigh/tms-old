package com.tms.web.base;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.exp.ExcelImporter;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.web.AbsTreeFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tms.service.base.AreaService;
import com.tms.service.base.impl.AreaExcelImporter;
import com.tms.vo.base.AreaVO;

/**
 * 区域管理
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:11:10
 */
@Controller
@RequestMapping(value = "/base/area")
public class AreaController extends AbsTreeFormController {

	@Autowired
	private AreaService areaService;

	public AreaService getService() {
		return areaService;
	}

	public String getTreePkField() {
		return AreaVO.PK_AREA;
	}

	public String getTreeCodeField() {
		return AreaVO.CODE;
	}

	public String getTreeTextField() {
		return AreaVO.NAME;
	}

	@RequestMapping(value = "/getAreaTree.json")
	@ResponseBody
	public List<TreeVO> getFunTree(HttpServletRequest request, HttpServletResponse response) {
		String parent_id = request.getParameter("parent_id");
		if(StringUtils.isBlank(parent_id)) {
			parent_id = request.getParameter("node");
		}
		String isRoot = request.getParameter("isRoot");
		if(Constants.TRUE.equals(isRoot)) {
			parent_id = null;
		}
		String keyword = request.getParameter(Constants.TREE_QUERY_KEYWORD);
		return this.getService().getAreaTree(parent_id, keyword);
	}

	public Map<String, Object> show(HttpServletRequest request, HttpServletResponse response) {
		String isRoot = request.getParameter("isRoot");
		if(Constants.TRUE.equals(isRoot)) {
			return this.genAjaxResponse(true, null, null);
		}
		return super.show(request, response);
	}

	// FIXME 2013-11-20编码不限制位数了，考虑到导入数据的时候自动生成编码的情况
	// protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO
	// paramVO) {
	// CircularlyAccessibleValueObject cvo = billVO.getParentVO();
	// Object code = cvo.getAttributeValue("code");
	// if(code != null && StringUtils.isNotBlank(code.toString())) {
	// try {
	// AreaVO vo = (AreaVO) this.getService().getByCode(code.toString());
	// if(cvo.getAttributeValue("parent_id") != null) {
	// // 不是根节点
	// // 父级vo
	// AreaVO parentVO = this.getService().getByPrimaryKey(AreaVO.class,
	// cvo.getAttributeValue("parent_id").toString());
	// if(code.toString().length() - parentVO.getCode().length() != 2) {
	// throw new RuntimeException("该级节点的编码长度必须是" + (parentVO.getCode().length()
	// + 2));
	// }
	// } else {
	// // 根节点的情况
	// if(code.toString().length() != 2) {
	// throw new RuntimeException("第一级节点的编码长度必须为2，如01，02");
	// }
	// }
	// if(StringUtils.isBlank(cvo.getPrimaryKey())) {
	// // 新增的情况
	// if(vo != null) {
	// throw new RuntimeException("编码已经存在！");
	// }
	// } else {
	// // 修改的情况
	// if(vo != null) {
	// if(!cvo.getPrimaryKey().equals(vo.getPrimaryKey())) {
	// throw new RuntimeException("编码已经存在！");
	// }
	// }
	// }
	// } catch(BusinessException e) {
	// e.printStackTrace();
	// }
	// }
	// // 设置级次
	// cvo.setAttributeValue("level", (int) code.toString().length() / 2);
	// }

	/**
	 * 导入文件
	 * 
	 * @param mfile
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/import.do")
	public void _import(@RequestParam("file") MultipartFile mfile, HttpServletRequest request,
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
			ExcelImporter importer = new AreaExcelImporter(paramVO, this.getService());
			importer._import(file);
			String strMsg = importer.getLog();
			strMsg = strMsg.replace("\n", "</p><p>"); // 打印到控制台使用\n，打印到页面上使用<BR>
			strMsg = "<p>" + strMsg + "</p>";
			response.getWriter().write("{'success':true,msg:'导入成功！','log':'" + strMsg + "'}");
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				file.delete();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
