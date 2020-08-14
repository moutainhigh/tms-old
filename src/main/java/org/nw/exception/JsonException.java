package org.nw.exception;

import org.ExceptionReference;
import org.nw.web.utils.WebUtils;

/**
 * 这种是实际上也业务异常，只是这种异常一定是以json格式返回，而不是跳转到某个错误页面
 * 
 * @author xuqc
 * @date 2013-9-14 下午01:45:14
 */
public class JsonException extends RuntimeException {
	private String msg;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4150390542260541995L;

	/**
	 * Constructor for DataAccessException.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public JsonException(String msg,String... args) {
		msg = getExceptionMessage(msg, args);
		throw new JsonException(msg);
	}
	
	public JsonException(String msg) {
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
	public JsonException(String msg, Throwable cause,String... args) {
		msg = getExceptionMessage(msg, args);
		throw new JsonException(msg,cause);
	}
	
	public JsonException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public JsonException(Throwable cause) {
		super(cause);
	}

	public String getBusiMessage() {
		return msg;
	}
	
	private String getExceptionMessage(String zh_CN_Message,String... args){
		ExceptionReference ref = new ExceptionReference();
		if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
			if(args != null && args.length > 0){
				for(String arg : args){
					zh_CN_Message = zh_CN_Message.replaceFirst("[?]", arg);
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
