package org.nw.web.sys.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.service.sys.NewsService;
import org.nw.vo.ParamVO;
import org.nw.vo.sys.NewsVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 新闻中心
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:28:07
 */
@Controller
@RequestMapping(value = "/common/news")
public class NewsController extends AbsToftController {

	@Autowired
	private NewsService newsService;

	public NewsService getService() {
		return newsService;
	}

	protected int getPageSize(HttpServletRequest request) {
		int limit = Constants.DEFAULT_PAGESIZE;
		try {
			limit = Integer.parseInt(request.getParameter(Constants.PAGE_SIZE_PARAM));
		} catch(Exception e) {
			limit = 20;
		}
		return limit;
	}

	/**
	 * 新闻管理首页
	 */
	@RequestMapping(value = "/index.html")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		String funCode = request.getParameter("funCode");
		if(StringUtils.isBlank(funCode)) {
			funCode = request.getAttribute("funCode").toString();
		}
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		String keyword = request.getParameter("keyword");
		if(StringUtils.isNotBlank(keyword)) {
			request.setAttribute("keyword", keyword);
		}

		request.setAttribute("funCode", funCode);
		PaginationVO pageVO = this.getService().getPageVO(offset, pageSize, keyword);
		request.setAttribute("newsVOs", pageVO.getItems());
		request.setAttribute("totalCount", pageVO.getTotalCount());
		request.setAttribute("pageHtml", pageVO.getHtml(PaginationVO.script));
		request.setAttribute("PAGE_PARAM_START", pageVO.getStartOffset());
		request.setAttribute("PAGE_PARAM_LIMIT", pageVO.getPageSize());
		return new ModelAndView(getFunHelpName(funCode));
	}

	/**
	 * 新增或编辑
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/edit.html")
	public ModelAndView getByPK(HttpServletRequest request, HttpServletResponse response) {
		String funCode = request.getParameter("funCode");
		request.setAttribute("funCode", funCode);
		String pk_news = request.getParameter("pk_news");
		if(StringUtils.isNotBlank(pk_news)) {
			this.getService().updateReadNum(pk_news);
			NewsVO vo = this.getService().getByPK(pk_news);
			request.setAttribute("newsVO", vo);
		}
		return new ModelAndView("/sys/common/editNews.jsp");
	}

	/**
	 * 编辑新闻，也可能是新增
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/saveNews.do")
	public void saveNews(HttpServletRequest request, HttpServletResponse response, NewsVO newsVO) {
		this.getService().saveNews(newsVO);
		if(StringUtils.isBlank(newsVO.getTitle())) {
			throw new BusiException("新闻标题是必须的！");
		}
		ParamVO paramVO = getParamVO(request);
		request.setAttribute("funCode", paramVO.getFunCode());
		try {
			response.sendRedirect("index.html?funCode=" + paramVO.getFunCode());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/delete.do")
	public ModelAndView deleteNews(HttpServletRequest request, HttpServletResponse response) {
		String pk_news = request.getParameter("pk_news");
		if(pk_news == null || pk_news.length() == 0) {
			throw new RuntimeException("删除新闻时主键参数不能为空！");
		}
		this.getService().delete(pk_news.split(","));
		String funCode = request.getParameter("funCode");
		request.setAttribute("funCode", funCode);
		return index(request, response);
	}
}
