package org.nw.web.sys;

import java.util.List;

import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.service.sys.RefRelationService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.RefRelationVO;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 档案表关联关系操作类
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/refrelation")
public class RefRelationController extends AbsToftController {

	@Autowired
	private RefRelationService refRelationService;

	public RefRelationService getService() {
		return refRelationService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.checkBeforeSave(billVO, paramVO);
		RefRelationVO refVO = (RefRelationVO) billVO.getParentVO();
		String sql = "select 1 from nw_ref_relation WITH(NOLOCK) where referencedtablekey=? and referencedtablename=? and referencingtablecolumn=? and referencingtablename=?";
		List<Integer> list = NWDao.getInstance().queryForList(sql, Integer.class, refVO.getReferencedtablekey(),
				refVO.getReferencedtablename(), refVO.getReferencingtablecolumn(), refVO.getReferencingtablename());
		if(list != null && list.size() > 0) {
			throw new BusiException("该规则已经存在！");
		}
	}

}
