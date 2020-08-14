package org.nw.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * 简单邮件（不带附件的邮件）发送器
 */
public class SimpleMailSender {

	/**
	 * 以文本格式发送邮件
	 * 
	 * @param mailInfo
	 *            待发送的邮件的信息
	 */
	public static void sendTextMail(MailSenderInfo mailSenderInfo)
			throws MessagingException {
		// if(mailInfo.isSSL())
		// java.security.Security.addProvider(new
		// com.sun.net.ssl.internal.ssl.Provider());
		// 判断是否需要身份认证
		Properties props = getProperties(mailSenderInfo);
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session.getDefaultInstance(props);
		// 根据session创建一个邮件消息
		Message mailMessage = new MimeMessage(sendMailSession);
		// 创建邮件发送者地址
		Address from = new InternetAddress(mailSenderInfo.getFromAddress());
		// 设置邮件消息的发送者
		mailMessage.setFrom(from);
		// 创建邮件的接收者地址
		for (int j = 0; j < mailSenderInfo.getToAddress().length; j++) {
			String to = mailSenderInfo.getToAddress()[j];
			mailMessage.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to));
		}
		// 设置邮件消息的主题
		mailMessage.setSubject(mailSenderInfo.getSubject());
		// 设置邮件消息发送的时间
		mailMessage.setSentDate(new Date());
		// 设置邮件消息的主要内容
		String mailContent = mailSenderInfo.getContent();
		mailMessage.setText(mailContent);
		// 发送邮件
		// 连接发送服务器
		Transport transport = sendMailSession.getTransport("smtp");
		transport.connect(mailSenderInfo.getMailServerHost(),
				mailSenderInfo.getUserName(), mailSenderInfo.getPassword());
		// 发送信息
		transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
		transport.close();
	}

	/**
	 * 以HTML格式发送邮件
	 * 
	 * @param mailSenderInfo
	 *            待发送的邮件信息
	 */
	public static void sendHtmlMail(MailSenderInfo mailSenderInfo)
			throws MessagingException, UnsupportedEncodingException {
		Properties props = getProperties(mailSenderInfo);
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session.getDefaultInstance(props);
		// 根据session创建一个邮件消息
		Message mailMessage = new MimeMessage(sendMailSession);
		// 创建邮件发送者地址
		mailMessage.setFrom(new InternetAddress(mailSenderInfo.getFromAddress()));
		// 创建邮件的接收者地址
		for (int j = 0; j < mailSenderInfo.getToAddress().length; j++) {
			String to = mailSenderInfo.getToAddress()[j];
			mailMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
		}

		// 设置邮件消息的主题
		mailMessage.setSubject(mailSenderInfo.getSubject());
		// 设置邮件消息发送的时间
		mailMessage.setSentDate(new Date());
		// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
		Multipart multipart = new MimeMultipart();
		// 创建一个包含HTML内容的MimeBodyPart
		BodyPart bodyPart = new MimeBodyPart();
		// 设置HTML内容
		bodyPart.setContent(mailSenderInfo.getContent(),"text/html; charset=utf-8");
		multipart.addBodyPart(bodyPart);
		// 有附件
		if(mailSenderInfo.getAttachs() != null && mailSenderInfo.getAttachs().size() > 0){
			for(File file : mailSenderInfo.getAttachs()){
				 BodyPart attachmentBodyPart = new MimeBodyPart();
				 DataSource source = new FileDataSource(file);
				 attachmentBodyPart.setDataHandler(new DataHandler(source));
				 //MimeUtility.encodeWord可以避免文件名乱码
				 attachmentBodyPart.setFileName(MimeUtility.encodeWord(file.getName()));
				 multipart.addBodyPart(attachmentBodyPart);
			}
		}
//		mailMessage.setContent(multipart);
//		
//		if (mailSenderInfo.getAttachFileNames() != null) {
//			if (mailSenderInfo.getAttachFileNames().length > 0) {
//				for (int i = 0; i < mailSenderInfo.getAttachFileNames().length; i++) {
//					MimeBodyPart mbodypart = new MimeBodyPart();
//					FileDataSource fds = new FileDataSource(mailSenderInfo.getAttachFileNames()[i]);
//					// 得到附件本身并植入BodyPart
//					mbodypart.setDataHandler(new DataHandler(fds));
//					// 得到文件名同样植入BodyPart
//					if (StringUtils.isNotEmpty(fds.getName())) {
//						mbodypart.setFileName(MimeUtility.encodeWord(fds.getName(), "GBK", null));
//					}
//					multipart.addBodyPart(mbodypart);
//				}
//			}
//		}
		// 将MiniMultipart对象设置为邮件内容
		mailMessage.setContent(multipart);
		mailMessage.saveChanges();
		// 连接发送服务器
		Transport transport = sendMailSession.getTransport("smtp");
		transport.connect(mailSenderInfo.getMailServerHost(),
				mailSenderInfo.getUserName(), mailSenderInfo.getPassword());
		// 发送信息
		transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
		transport.close();
	}

	/**
	 * 获得邮件会话属性
	 */
	public static Properties getProperties(MailSenderInfo mailSenderInfo) {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", mailSenderInfo.getMailServerHost());
		props.put("mail.smtp.port", mailSenderInfo.getMailServerPort());
		props.put("mail.smtp.auth", mailSenderInfo.isValidate() ? "true"
				: "false");
		props.put("mail.debug", mailSenderInfo.isDebug() ? "true" : "false");
		props.put("mail.smtp.user", mailSenderInfo.getUserName());
		props.put("mail.smtp.password", mailSenderInfo.getPassword());
		props.put("mail.smtp.from", mailSenderInfo.getFromAddress());
		props.put("mail.smtp.timeout", 30000);
		if (mailSenderInfo.isSSL()) {
			props.put("mail.smtp.socketFactory.port",
					mailSenderInfo.getMailServerPort());
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
		}
		return props;
	}

	public static void main(String[] args) {
		// 这个类主要是设置邮件
		MailSenderInfo mailSenderInfo = new MailSenderInfo("", "", "", "", "",
				true, false);
		mailSenderInfo.setToAddress(new String[] { "24559364@qq.com" });
		mailSenderInfo.setSubject("设置邮箱标题");
		mailSenderInfo.setContent("设置邮箱内容 中国桂花网 <b>是中国最大桂花网站==</b>");
		try {
			// SimpleMailSender.sendTextMail(mailSenderInfo);//发送文体格式
			// 发送html格式
			SimpleMailSender.sendHtmlMail(mailSenderInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
