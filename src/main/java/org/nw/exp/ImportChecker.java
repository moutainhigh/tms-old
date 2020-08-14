package org.nw.exp;

import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;

/**
 * 验证器,比如导入时需要对编码进行唯一性验证
 * 
 * @author xuqc
 * @date 2013-8-16 下午05:29:29
 */
public interface ImportChecker {

	/**
	 * 注入service类
	 * 
	 * @param service
	 */
	public void setService(IToftService service);

	/**
	 * 检查该字段的值是否有效,返回的string是错误信息
	 * 
	 * @param value
	 * @param fieldVO
	 * @return
	 */
	public String check(Object value, BillTempletBVO fieldVO, int rowNum);
}
