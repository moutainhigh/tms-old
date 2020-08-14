package org.nw.service.sys;

import org.nw.service.IToftService;

/**
 * Excel导入字段定义处理接口
 * 
 * @author xuqc
 * 
 */
public interface ImportColumnService extends IToftService {
	/**
	 * 复制导入字段到另外一个公司
	 * 
	 * @param pk_fun
	 * @param pk_corp
	 * @param dest_pk_corp
	 */
	public void copyColumn(String pk_fun, String pk_corp, String dest_pk_corp);
}
