package org.nw.service.sys;

import java.util.List;

import org.nw.dao.PaginationVO;
import org.nw.service.IToftService;
import org.nw.vo.sys.NewsVO;


/**
 * 新闻中心接口
 * 
 * @author xuqc
 * @date 2013-7-3 上午10:03:14
 */
public interface NewsService extends IToftService {
	/**
	 * 返回最新的5条新闻
	 * 
	 * @return
	 */
	public List<NewsVO> getTop5();

	/**
	 * 返回查询的VO
	 * 
	 * @return
	 */
	public PaginationVO getPageVO(int offset, int pageSize, String keyword);

	/**
	 * 更新新闻的阅读数
	 * 
	 * @param pk_news
	 * @return
	 */
	public NewsVO updateReadNum(String pk_news);

	/**
	 * 根据主键查询
	 * 
	 * @param pk_news
	 * @return
	 */
	public NewsVO getByPK(String pk_news);

	/**
	 * 保存或者修改
	 * 
	 * @param newsVO
	 * @return
	 */
	public NewsVO saveNews(NewsVO newsVO);

	/**
	 * 根据主键删除
	 * 
	 * @param pkAry
	 */
	public void delete(String[] pkAry);
}
