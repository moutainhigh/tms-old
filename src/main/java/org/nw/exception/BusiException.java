package org.nw.exception;

import org.ExceptionReference;
import org.nw.web.utils.WebUtils;

/**
 * 业务异常，通常需要在页面上进行提示，提示方式分成2种<br/>
 * 1、如果是json请求，URL以json结尾，使用json方式提示<br/>
 * 2、其他请求跳转到error页面 <br/>
 * 3、实际上还有一些是以.do结尾的请求，这些请求可能也需要使用json的格式返回，如无刷新的文件上传,这种情况抛出的请示参考JsonException
 * 
 * @author xuqc
 * @date 2012-9-5 下午03:36:23
 */
public class BusiException extends RuntimeException {
	protected String msg;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4150390542260541995L;

	public BusiException() {

	}
	
	public BusiException(String msg) {
		super(msg);
		this.msg = msg;
	}

	/**
	 * Constructor for DataAccessException.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public BusiException(String msg,String... args) {
		msg = getExceptionMessage(msg, args);
		throw new BusiException(msg);
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
	public BusiException(String msg, Throwable cause,String... args) {
		msg = getExceptionMessage(msg, args);
		throw new BusiException(msg, cause);
	}
	
	public BusiException(String msg,Throwable cause) {
		super(msg, cause);
	}

	public BusiException(Throwable cause) {
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
					if(arg == null){
						arg = "";
					}
					zh_CN_Message = zh_CN_Message.replaceFirst("[?]",arg );
				}
			}
			return zh_CN_Message;
		}else{
			if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				String en_US_Message = ref.getEnValue(zh_CN_Message);
				if(args != null && args.length > 0){
					for(String arg : args){
						if(arg == null){
							arg = "";
						}
						en_US_Message = en_US_Message.replaceFirst("[?]", "[" + arg + "]");
					}
				}
				return en_US_Message;
			}
		}
		return zh_CN_Message;
	}
}
