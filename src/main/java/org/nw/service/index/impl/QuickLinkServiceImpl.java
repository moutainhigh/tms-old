package org.nw.service.index.impl;

import java.util.List;

import org.nw.exception.BusiException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.index.QuickLinkService;
import org.nw.vo.index.QuickLinkVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.FunVO;
import org.springframework.stereotype.Service;


/**
 * 快捷菜单操作类
 * 
 * @author xuqc
 * @date 2012-7-3 下午10:52:22
 */
@Service
public class QuickLinkServiceImpl extends AbsToftServiceImpl implements QuickLinkService {

	// 不需要模板操作，直接返回空
	public AggregatedValueObject getBillInfo() {
		return null;
	}

	public List<FunVO> getQuickLinks(String pk_user) {
		//yaojiie 2015 12 08添加 WITH(NOLOCK)
		String sql = "select f.pk_fun,f.fun_code,f.class_name,q.display_name as fun_name "
				+ "from nw_fun f WITH(NOLOCK) inner join nw_quick_link q WITH(NOLOCK)  on f.pk_fun=q.pk_fun "
				+ " where q.pk_user=? and isnull(q.dr,0)=0 and isnull(f.locked_flag,'N')='N' and isnull(f.dr,0)=0 order by q.create_time ";
		return dao.queryForList(sql, FunVO.class, pk_user);
	}

	/**
	 * 继承父类，检测当前的快捷菜单是否已经存在
	 */
	public int addSuperVO(SuperVO superVO) {
		QuickLinkVO quickLinkVO = (QuickLinkVO) superVO;
		QuickLinkVO vo = dao.queryByCondition(QuickLinkVO.class, "pk_fun=? and pk_user=?", quickLinkVO.getPk_fun(),
				quickLinkVO.getPk_user());
		if(vo != null) {
			throw new BusiException("该节点已存在快捷方式！");
		}
		return super.addSuperVO(quickLinkVO);
	}

}
