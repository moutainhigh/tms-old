package org.nw.service.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.nw.exception.BusiException;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * api开放接口的验证服务
 * 
 * @author xuqc
 * @date 2015-1-28 下午08:52:54
 */
@Service
public class AuthenticationService {

	Logger logger = Logger.getLogger(this.getClass());

	public static final String authFile = "auth.xml";
	Map<String, String> userAndPassMap = null;

	private Document getDocument() {
		String config_file_path = WebUtils.getClientConfigPath() + File.separator + "api" + File.separator + authFile;
		// logger.info("配置文件路径：" + config_file_path);
		File config_file = new File(config_file_path);
		if(!config_file.exists()) {
			String s = "api验证文件不存在，文件路径：" + config_file_path;
			logger.error(s);
			throw new RuntimeException(s);
		}
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(config_file);
		} catch(DocumentException e) {
			logger.error("解析xml文件错误，错误信息：" + e.getMessage());
			e.printStackTrace();
		}
		return document;
	}

	// 加载验证的用户名和密码
	public void load() {
		Document doc = getDocument();
		if(doc != null) {
			userAndPassMap = new HashMap<String, String>();
			String username = doc.selectSingleNode("/auth/http/username").getText();
			// 这里是已经加密的密码
			String password = doc.selectSingleNode("/auth/http/password").getText();
			userAndPassMap.put(username, password);
		}
	}

	/**
	 * 验证用户名和密码是否合法
	 * 
	 * @param uid
	 * @param pwd
	 * @return
	 * @throws BusiException
	 */
	public String auth(String uid, String pwd){
		if(StringUtils.isBlank(uid) || StringUtils.isBlank(pwd)) {
			return "用户名或密码不能为空！";
		}
		if(userAndPassMap == null) {
			load();
		}
		String pass = userAndPassMap.get(uid);
		if(pass == null) {
			return "用户名不存在！";
		}
		if(!pass.equals(pwd)) {
			return "密码错误！";
		}
		return "";
	}
}
