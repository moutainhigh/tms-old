package org.nw.service.sys;

import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.sys.BulletinVO;


/**
 * 公告
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:28:34
 */
public interface BulletinService extends IToftService {

	/**
	 * 返回最新的5条公告
	 * 
	 * @return
	 */
	public List<BulletinVO> getTop5();
}
