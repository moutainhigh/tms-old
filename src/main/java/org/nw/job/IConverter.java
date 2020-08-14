package org.nw.job;

import org.nw.vo.api.RootVO;

/**
 * 调用webservice或者http请求后返回的数据转换成vo
 * 
 * @author xuqc
 * @date 2014-11-14 下午05:30:01
 */
public interface IConverter {

	/**
	 * 将返回的text转换成vo
	 * 
	 * @param text
	 * @return
	 */
	public RootVO convertResponse(String text);
}
