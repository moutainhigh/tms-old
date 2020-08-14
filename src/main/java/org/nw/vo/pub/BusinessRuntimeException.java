package org.nw.vo.pub;

/**
 * 
 * This runtime exception is designed for business module
 */
public class BusinessRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BusinessRuntimeException() {
		super();
	}

	public BusinessRuntimeException(String msg) {
		super(msg);
	}

	public BusinessRuntimeException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
