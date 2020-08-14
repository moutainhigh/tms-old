package org.nw.service.sys.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.NewsService;
import org.nw.utils.CorpHelper;
import org.nw.utils.FormulaHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.sys.NewsVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 新闻中心
 * 
 * @author xuqc
 * @date 2013-7-3 上午10:03:32
 */
@Service
public class NewsServiceImpl extends AbsToftServiceImpl implements NewsService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, NewsVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, "pk_news");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCorpCondition(String tablePrefix, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return CorpHelper.getCurrentCorp();
	}

	public List<NewsVO> getTop5() {
		String sql = "select top 5 * from nw_news WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select * from nw_news where rownum < 6 and isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 * from nw_news WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		}
		List<NewsVO> list = NWDao.getInstance().queryForList(sql, NewsVO.class, WebUtils.getLoginInfo().getPk_corp());
		return list;
	}

	public String[] getFormulas() {
		return new String[] { "publisher_name->getColValue(nw_user,user_name,pk_user,publisher)" };
	}

	@SuppressWarnings("unchecked")
	public PaginationVO getPageVO(int offset, int pageSize, String keyword) {
		String cond = "pk_corp=? order by post_date desc";
		if(StringUtils.isNotBlank(keyword)) {
			cond = "(title like '%" + keyword + "%' or tags like '%" + keyword + "%' or source like '%" + keyword
					+ "%') and " + cond;
		}
		PaginationVO paginationVO = NWDao.getInstance().queryByConditionWithPaging(NewsVO.class, offset, pageSize,
				cond, WebUtils.getLoginInfo().getPk_corp());
		List<Map<String, Object>> retList = FormulaHelper.execFormulaForSuperVO(paginationVO.getItems(), getFormulas());
		paginationVO.setItems(retList);
		return paginationVO;
	}

	public NewsVO getByPK(String pk_news) {
		return this.getByPrimaryKey(NewsVO.class, pk_news);
	}

	public NewsVO updateReadNum(String pk_news) {
		if(StringUtils.isBlank(pk_news)) {
			return null;
		}
		NewsVO vo = NWDao.getInstance().queryByCondition(NewsVO.class, "pk_news=?", pk_news);
		if(vo == null) {
			return null;
		}
		vo.setRead_num(vo.getRead_num().intValue() + 1);
		vo.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(vo);
		return vo;
	}

	public NewsVO saveNews(NewsVO newsVO) {
		if(newsVO == null) {
			return null;
		}
		newsVO.setPublisher(WebUtils.getLoginInfo().getPk_user());
		newsVO.setPost_date(new UFDate(new Date()));
		newsVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		if(StringUtils.isBlank(newsVO.getPk_news())) {
			newsVO.setRead_num(0);
			newsVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(newsVO);
		} else {
			newsVO.setStatus(VOStatus.UPDATED);
		}
		try {
			this.saveOrUpdate(newsVO);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return newsVO;
	}

	public void delete(String[] pkAry) {
		if(pkAry == null || pkAry.length == 0) {
			return;
		}
		for(String pk : pkAry) {
			NWDao.getInstance().deleteByPK(NewsVO.class, pk);
		}
	}
}
