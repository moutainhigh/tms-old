package org.nw.service.sys;

import org.nw.service.IToftService;
import org.nw.vo.sys.PsndocVO;


/**
 * 
 * @author xuqc
 * @date 2012-6-17 下午03:38:52
 */
public interface PsndocService extends IToftService {

	/**
	 * 根据人员编码返回人员VO
	 * 
	 * @param psncode
	 * @return
	 */
	public PsndocVO getByPsncode(String psncode);
}
