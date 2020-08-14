package org.nw.service.sys;

import org.nw.service.IToftService;

/**
 * 资源压缩的操作类接口
 * 
 * @author xuqc
 * 
 */
public interface MergeService extends IToftService {

	/**
	 * 执行压缩动作
	 * 
	 * @param pk_merge
	 */
	public void execute(String[] pk_merge);
}
