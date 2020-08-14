package org.nw.vo.pub;

/**
 * 数据验证异常类。对不符合业务要求的数据输入抛出这个异常。这属于 EJB规范中的应用级异常。
 * 
 */
public class ValidationException extends BusinessException {
	/**
	 * ValidationException 构造子注解。
	 */
	public ValidationException() {
		super();
	}

	/**
	 * ValidationException 构造子注解。
	 * 
	 * @param s
	 *            java.lang.String
	 */
	public ValidationException(String s) {
		super(s);
	}
}
