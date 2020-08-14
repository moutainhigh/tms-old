package org.nw.service.sys.impl;

import java.util.List;

import org.nw.basic.util.DateUtils;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.UFunService;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.FunVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 用户自定义菜单处理类
 * 
 * @author xuqc
 * @date 2012-7-8 下午02:16:36
 */
@Service
public class UFunServiceImpl extends AbsToftServiceImpl implements UFunService {

	public AggregatedValueObject getBillInfo() {
		return null;
	}

	public int saveDisplayOrder(List<FunVO> funVOs) {
		if(funVOs == null || funVOs.size() == 0) {
			return 0;
		}
		for(FunVO funVO : funVOs) {
			String sql = "update nw_fun set parent_id=?,display_order=?,ts=?,modify_user=?,modify_time=? where pk_fun=?";
			String current_time = DateUtils.getCurrentDate(DateUtils.DATETIME_FORMAT_HORIZONTAL);
			dao.update(sql, funVO.getParent_id(), funVO.getDisplay_order(), current_time, WebUtils.getLoginInfo()
					.getPk_user(), current_time, funVO.getPk_fun());
		}
		return funVOs.size();
	}

}
