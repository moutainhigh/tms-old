package org.nw.service.sys.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.DateUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.BulletinService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.BulletinVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 公告处理
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:29:19
 */
@Service
public class BulletinServiceImpl extends AbsToftServiceImpl implements BulletinService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, BulletinVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, "pk_bulletin");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCorpCondition(String tablePrefix, ParamVO paramVO, UiQueryTempletVO templetVO) {
		return CorpHelper.getCurrentCorp();
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		values.put("publisher", WebUtils.getLoginInfo().getPk_user());
		values.put("post_date", DateUtils.getCurrentDate());
		return values;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by post_date desc";
		}
		return orderBy;
	}

	public List<BulletinVO> getTop5() {
		String sql = "select top 5 * from nw_bulletin WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select * from nw_bulletin where rownum < 6 and isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 * from nw_bulletin WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		}
		List<BulletinVO> list = NWDao.getInstance().queryForList(sql, BulletinVO.class,WebUtils.getLoginInfo().getPk_corp());
		return list;
	}

}
