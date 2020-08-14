package org.nw.mail.res;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.nw.utils.Resources;
import org.nw.web.utils.WebUtils;

/**
 * 邮件模板
 * 
 * @author aimer.xu
 * 
 */
public class ResetPasswordMailTemplate {

	String subject; // 邮件标题
	String content; // 邮件内容
	boolean isHtml; // 是否以html格式发送，或者以文本格式发送
	String signature; // 签名

	public void initResetMail(String usercode, String randomPass) {
		try {
			InputStream is = null;
			String filePath = "mail/reset_password.html";
			File file = new File(WebUtils.getClientConfigPath() + File.separator + filePath);
			if(file.exists()) {
				is = new FileInputStream(file);
			}
			if(is == null) {
				is = Resources.getResourceAsStream(filePath);
			}
			InputStreamReader in = new InputStreamReader(is, "utf-8");
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(in);
			String str = br.readLine();
			while(str != null) {
				sb.append(str);
				str = br.readLine();
			}
			br.close();
			in.close();
			this.content = sb.toString();
			int begin = content.indexOf("<title>");
			int end = content.indexOf("</title>");
			this.subject = content.substring(begin + 7, end);
		} catch(IOException e) {
			e.printStackTrace();
		}
		content = content.replaceAll("\\$usercode", usercode);
		content = content.replaceAll("\\$randomPass", randomPass);
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

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

}
