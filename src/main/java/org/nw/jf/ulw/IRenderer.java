package org.nw.jf.ulw;

/**
 * ULW数据的处理器接口
 * 
 * @author xuqc
 * @date 2012-5-11
 */
public interface IRenderer {

	/**
	 * 
	 * @param value
	 *            当前值
	 * @param reftype
	 *            参照类型
	 * @return
	 * @author xuqc
	 * @date 2012-5-11
	 * 
	 */
	public Object render(Object value, int datatype, String reftype);
}
