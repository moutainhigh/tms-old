/**
 * 
 */
package org.nw.web.sys;

import org.apache.commons.lang3.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.service.IToftService;
import org.nw.service.sys.PortletPlanService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.PortletPlanVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 组件方案
 * 
 * @author xuqc
 * @Date 2015年5月15日 下午3:26:29
 *
 */
@Controller
@RequestMapping(value = "/portletplan")
public class PortletPlanController extends AbsToftController {

	@Autowired
	private PortletPlanService portletPlanService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.web.AbsToftController#getService()
	 */
	@Override
	public IToftService getService() {
		return portletPlanService;
	}

	@Override
	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		// TODO Auto-generated method stub
		super.checkBeforeSave(billVO, paramVO);
		PortletPlanVO parentVO = (PortletPlanVO) billVO.getParentVO();
		if(parentVO.getIf_default() != null && parentVO.getIf_default().booleanValue()) {
			// 检查是否有多条默认的方案，一个公司只能有一条默认的方案
			String sql = "select count(1) from nw_portlet_plan WITH(NOLOCK) where isnull(if_default,'N')='Y' "
					+ "and isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and pk_corp=?";
			if(StringUtils.isNoneBlank(parentVO.getPk_portlet_plan())) {
				sql += " and pk_portlet_plan <> '" + parentVO.getPk_portlet_plan() + "'";
			}
			Long count = NWDao.getInstance().queryForObject(sql, Long.class, WebUtils.getLoginInfo().getPk_corp());
			if(count != null && count > 0) {
				throw new BusiException("一个公司只能有一条默认方案！");
			}
		}
	}
}
