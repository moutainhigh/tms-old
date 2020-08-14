package org.nw.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.jf.vo.FunRegisterPropertyVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.redis.RedisDao;
import org.nw.service.IReportService;
import org.nw.service.ServiceHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.web.utils.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 报表模板的基础控制类
 * 
 * @author xuqc
 * @date 2013-7-29 下午01:27:23
 */
public abstract class AbsReportController extends AbsToftController {

	public abstract IReportService getService();

	/**
	 * 报表节点首页的处理类，在菜单管理中，将对应文件名注册成report.html
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = getParamVO(request);
		// 获取模板数据
		String templetID = this.getService().getReportTempletID(paramVO);
		if(StringUtils.isBlank(templetID)) {
			throw new BusiException("没有分配报表模板！");
		}
		UiReportTempletVO templetVO = this.getService().getReportTempletVO(templetID);
		templetVO.setFunCode(paramVO.getFunCode());
		templetVO.setNodeKey(paramVO.getNodeKey());
		// 锁定条件
		templetVO.setLockingItemAry(this.getService().getLockingItemAry(paramVO));

		// 功能注册中定义的与生成单据相关的配置信息
		String sHeaderHeight = request.getParameter(FunRegisterPropertyVO._HEADERHEIGHT);
		try {
			if(sHeaderHeight != null) {
				templetVO.getFuncRegisterPropertyVO().setHeaderHeight(Integer.parseInt(sHeaderHeight));
			}
		} catch(Exception e) {
		}
		templetVO.getFuncRegisterPropertyVO().setHeaderSplit(
				Boolean.valueOf(request.getParameter(FunRegisterPropertyVO._HEADERSPLIT)));
		templetVO.getFuncRegisterPropertyVO().setWaterfallScene(
				Boolean.valueOf(request.getParameter(FunRegisterPropertyVO._WATERFALLSCENE)));
		templetVO.getFuncRegisterPropertyVO().setBtnArray(
				//ServiceHelper.getBtnRegisterAry(paramVO)
				RedisDao.getInstance().getBtnRegisterAry(paramVO, WebUtils.getLoginInfo().getPk_user())
				);
		request.setAttribute(Constants.TEMPLETVO, templetVO);
		// 页面需要使用该moduleName
		request.setAttribute(Constants.MODULENAME, templetVO.getModuleName());
		// 附加数据处理
		this.doIndexExtProcess(request, response, templetVO);
		return new ModelAndView(getFunHelpName(paramVO.getFunCode()));
	}

	/**
	 * 进入页面的附加处理,to be override （具体controller可覆盖此方法）
	 * 
	 * @param request
	 * @param response
	 */
	protected void doIndexExtProcess(HttpServletRequest request, HttpServletResponse response,
			UiReportTempletVO templetVO) {
		// 默认无处理
	}

	/**
	 * 加载数据<br>
	 * 注：主表和子表都用这一个方法
	 * 
	 * @param request
	 * @param response
	 * @param funCode
	 * @param templateID
	 * @param tabCode
	 * @param isBody
	 * @return
	 */
	@RequestMapping(value = "/loadData.json")
	@ResponseBody
	public Map<String, Object> loadData(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);

		int pageSize = getPageSize(request);
		int offset = getOffset(request);
		String orderBy = this.getOrderBy(request, null);
		ReportVO reportVO = this.getService().loadReportData(paramVO, offset, pageSize, orderBy);

		return this.genAjaxResponse(true, null, reportVO);
	}

	/**
	 * 导出excel文件，将列表信息导出，可以是主表，也可以是子表
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
		ParamVO paramVO = this.getParamVO(request);
		// 返回查询的值
		HSSFWorkbook workbook = this.getService().export(paramVO, Constants.DEFAULT_OFFSET_WITH_NOPAGING,
				Constants.DEFAULT_PAGESIZE_WITH_NOPAGING, null, null);
		OutputStream os = response.getOutputStream();
		try {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment;filename=" + filename + ".xls");
			workbook.write(os);
		} catch(Exception e) {
			logger.error("导出excel出错！", e);
		} finally {
			os.flush();
			os.close();
		}
	}
}
