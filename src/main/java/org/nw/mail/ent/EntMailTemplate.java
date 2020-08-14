package org.nw.mail.ent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.nw.utils.Resources;
import org.nw.web.utils.WebUtils;

/**
 * 邮件模板
 * 
 * @author aimer.xu
 * 
 */
public class EntMailTemplate {

	String subject; // 邮件标题
	String content; // 邮件内容
	boolean isHtml; // 是否以html格式发送，或者以文本格式发送
	String signature; // 签名

	public void initEntMail(Map<String,String> replaceFeilds,String fileName) {
		try {
			InputStream is = null;
			String filePath = "mail/" + fileName;
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
		if(replaceFeilds != null && replaceFeilds.size() > 0){
			for(String feild : replaceFeilds.keySet()){
				content = content.replaceAll("\\$" + feild, replaceFeilds.get(feild));
			}
		}
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
