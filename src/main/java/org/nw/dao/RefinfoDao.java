package org.nw.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nw.vo.sys.RefInfoVO;
import org.nw.web.utils.WebUtils;

/**
 * nw_refinfo的dao类<br/>
 * 
 * @author xuqc
 * @date 2010-12-10
 */
public class RefinfoDao {

	private static ConcurrentHashMap<String, RefInfoVO> REFINFO_CACHE_MAP = new ConcurrentHashMap<String, RefInfoVO>();

	/**
	 * 仅用于在debug下使用,这里使用一个缓存，加快调试的效率
	 * <p>
	 * 一般单据的参照很多，如果每个都重新查询，速度也是很慢，而且参照有更改一般也是增加一个参照，所以该缓存一般不会影响调试
	 * </p>
	 * 
	 * @param name
	 * @return
	 * @author xuqc
	 * @date 2011-10-18
	 */
	public RefInfoVO getRefinfoVOWhenNocache(String name) {
		RefInfoVO refInfoVO = REFINFO_CACHE_MAP.get(name);
		if(refInfoVO != null) {
			return refInfoVO;
		}
		String sql = "SELECT * FROM NW_REFINFO WITH(NOLOCK) where name=?";
		List<RefInfoVO> vos = NWDao.getInstance().queryForList(sql, RefInfoVO.class, name);
		if(vos.size() == 1) {
			refInfoVO = vos.get(0);
			REFINFO_CACHE_MAP.put(refInfoVO.getName(), refInfoVO);
			return refInfoVO;
		} else {
			if(vos.size() > 1) {
				if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
					throw new RuntimeException("NW_REFINFO表存在重名参照，参照名称：" + name);
				}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
					throw new RuntimeException("NW_REFINFO table are duplicate reference, reference:" + name);
				}
				throw new RuntimeException("NW_REFINFO表存在重名参照，参照名称：" + name);
			}
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("NW_REFINFO表不存在参照：" + name);
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("There is no reference to the NW_REFINFO table:" + name);
			}
			throw new RuntimeException("NW_REFINFO表不存在参照：" + name);
		}
	}

	/**
	 * 返回参照VO
	 * 
	 * @param reftype
	 * @return
	 */
	public RefInfoVO getRefinfoVO(String reftype) {
		if(reftype.indexOf(",") > -1) {
			// nc中的参照有些附加条件会加在参照名称上，如部门档案,code=Y,nl=N
			reftype = reftype.substring(0, reftype.indexOf(","));
		}
		RefInfoVO vo = null;
		// 当没有使用缓存时，将reftype加入查询条件，这样查询较快，便于测试，
		if(CacheUtils.isUseCache()) {
			Map<String, RefInfoVO> ref_map = getNameRefinfoMap();
			vo = ref_map.get(reftype);
		} else {
			vo = this.getRefinfoVOWhenNocache(reftype);
		}
		if(vo == null) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("参照:" + reftype + "还未配置。");
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Reference: "+ reftype +" is not configured");
			}
			throw new RuntimeException("参照:" + reftype + "还未配置。");
		} else {
			return vo;
		}
	}

	/**
	 * 返回所有参照信息
	 * 
	 * @return
	 */
	public Map<String, RefInfoVO> getNameRefinfoMap() {
		String sql = "SELECT * FROM NW_REFINFO WITH(NOLOCK) where isnull(dr,0)=0 order by name";
		List<RefInfoVO> vos = NWDao.getInstance().queryForListWithCache(sql, RefInfoVO.class);
		Map<String, RefInfoVO> map = new LinkedHashMap<String, RefInfoVO>();
		for(RefInfoVO vo : vos) {
			map.put(vo.getName(), vo);
		}
		return map;
	}
}
