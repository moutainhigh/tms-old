package org.nw.mail;

import java.io.File;
import java.util.List;
import org.nw.Global;

public class MailSenderInfo {
	// 发送邮件的服务器的IP和端口
	private String mailServerHost;
	private String mailServerPort = "25";
	// 邮件发送者的地址
	private String fromAddress;
	// 邮件接收者的地址,可以有多个接收者
	private String[] toAddress;
	// 登陆邮件发送服务器的用户名和密码
	private String userName;
	private String password;
	// 是否需要身份验证
	private boolean validate = true;
	// 邮件主题
	private String subject;
	// 邮件的文本内容
	private String content;
	// 邮件附件的文件名
	private String[] attachFileNames;
	// 邮件附件
	private List<File> attachs;
	
	private boolean isSSL = false;

	private boolean debug = false;

	public MailSenderInfo() {
		mailServerHost = Global.getPropertyValue("mail.smtp.host");
		mailServerPort = Global.getPropertyValue("mail.smtp.port");
		fromAddress = Global.getPropertyValue("mail.smtp.address");
		userName = Global.getPropertyValue("mail.smtp.user");
		password = Global.getPropertyValue("mail.smtp.password");
		isSSL = Global.getBooleanValue("mail.smtp.isSSL");
		debug = Global.getBooleanValue("mail.smtp.debug");
	}

	public MailSenderInfo(String mailServerHost, String mailServerPort, String fromAddress, String userName,
			String password, boolean isSSL, boolean debug) {
		this.mailServerHost = mailServerHost;
		this.mailServerPort = mailServerPort;
		this.fromAddress = fromAddress;
		this.userName = userName;
		this.password = password;
		this.isSSL = isSSL;
		this.debug = debug;
	}

	public String getMailServerHost() {
		return mailServerHost;
	}

	public void setMailServerHost(String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}

	public String getMailServerPort() {
		return mailServerPort;
	}

	public void setMailServerPort(String mailServerPort) {
		this.mailServerPort = mailServerPort;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public String[] getAttachFileNames() {
		return attachFileNames;
	}

	public void setAttachFileNames(String[] fileNames) {
		this.attachFileNames = fileNames;
	}
	
	public List<File> getAttachs() {
		return attachs;
	}

	public void setAttachs(List<File> attachs) {
		this.attachs = attachs;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String[] getToAddress() {
		return toAddress;
	}

	public void setToAddress(String[] toAddress) {
		this.toAddress = toAddress;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String textContent) {
		this.content = textContent;
	}

	/**
	 * @return the isSSL
	 */
	public boolean isSSL() {
		return isSSL;
	}

	/**
	 * TODO
	 * 
	 * @param isSSL
	 *            the isSSL to set
	 */
	public void setSSL(boolean isSSL) {
		this.isSSL = isSSL;
	}
}
