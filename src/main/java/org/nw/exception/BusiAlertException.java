package org.nw.exception;

import org.ExceptionReference;
import org.nw.web.utils.WebUtils;

/**
 * 使用该异常主要用于弹出alert框，对于特别要提醒用户的信息。alert框会阻碍客户的正常流程，慎重使用
 * 
 * @author xuqc
 * @date 2013-11-19 下午10:08:12
 */
public class BusiAlertException extends BusiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for DataAccessException.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public BusiAlertException(String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public BusiAlertException(String msg,String... args) {
		msg = getExceptionMessage(msg, args);
		throw new BusiAlertException(msg);
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
	public BusiAlertException(String msg, Throwable cause,String... args) {
		msg = getExceptionMessage(msg, args);
		throw new BusiAlertException(msg,cause);
	}
	
	public BusiAlertException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	private String getExceptionMessage(String zh_CN_Message,String... args){
		ExceptionReference ref = new ExceptionReference();
		if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
			if(args != null && args.length > 0){
				for(String arg : args){
					zh_CN_Message = zh_CN_Message.replaceFirst("[?]",  arg );
				}
			}
			return zh_CN_Message;
		}else{
			if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				String en_US_Message = ref.getEnValue(zh_CN_Message);
				if(args != null && args.length > 0){
					for(String arg : args){
						en_US_Message = en_US_Message.replaceFirst("[?]", arg);
					}
				}
				return en_US_Message;
			}
		}
		return zh_CN_Message;
	}
}
