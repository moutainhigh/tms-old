package org.nw.exception;

/**
 * 开放接口的异常类，系统根据这个异常类形成开放接口统一的数据格式
 * 
 * @author xuqc
 * @date 2015-1-24 下午04:48:12
 */
public class ApiException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String msg;

	public ApiException() {

	}

	/**
	 * Constructor for DataAccessException.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public ApiException(String msg) {
		super(msg);
		this.msg = msg;
	}

	/**
	 * Constructor for DataAccessException.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the root cause (usually from using a underlying data access
	 *            API such as JDBC)
	 */
	public ApiException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ApiException(Throwable cause) {
		super(cause);
	}

	public String getBusiMessage() {
		return msg;
	}
}
