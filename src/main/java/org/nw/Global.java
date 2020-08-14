package org.nw;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.nw.utils.PropertiesUtils;
import org.nw.web.utils.WebUtils;

/**
 * 全局配置文件
 * 
 * @author xuqc
 * 
 */
@SuppressWarnings("rawtypes")
public class Global {
	static Logger logger = Logger.getLogger(Global.class.getName());

	private static Properties prop = null;
	private static Properties smtpProp = null;
	private static Properties configProp = null;
	private static String fileName = "/nw.properties";
	private static String smtpFileName = "/smtp.properties";
	private static String resourveVersionFile = "/resource.version";
	private static String configFileName = "/config.properties";
	private static String version = null;

	// 上传文件的根路径
	public static String uploadDir;
	public static String licdir;//yaojiie 2016 1 10 许可证根路径
	public static String edidir;//yaojiie 2016 1 15 接口配置文件根路径
	public static String productCode;
	public static String productName;// 产品名称
	static {
		if(prop == null) {
			prop = PropertiesUtils.loadProperties(Global.class.getResourceAsStream(fileName));
		}
		productCode = getPropertyValue("product.code");
		productName = getPropertyValue("product.name");

		if(configProp == null) {
			String path = WebUtils.getClientConfigPath() + configFileName;
			try {
				File configPropFile = new File(path);
				Global.configProp = PropertiesUtils.loadProperties(new FileInputStream(configPropFile));
			} catch(Exception e) {
				logger.fine("加载config配置文件出错，可能文件不存在，path：" + path);
				e.printStackTrace();
			}
		}
		if(configProp != null) {
			// 将configProp的数据拷贝到prop中
			Set keys = configProp.keySet();
			for(Object key : keys) {
				prop.setProperty(String.valueOf(key), String.valueOf(configProp.get(key)));
			}
		}

		if(smtpProp == null) {
			String path = WebUtils.getClientConfigPath() + smtpFileName;
			try {
				File smtpPropFile = new File(path);
				Global.smtpProp = PropertiesUtils.loadProperties(new FileInputStream(smtpPropFile));
			} catch(Exception e) {
				logger.fine("加载smtp配置文件出错，可能文件不存在，path：" + path);
				e.printStackTrace();
			}
		}
		if(smtpProp != null) {
			// 将smtpProp的数据拷贝到prop中
			Set keys = smtpProp.keySet();
			for(Object key : keys) {
				prop.setProperty(String.valueOf(key), String.valueOf(smtpProp.get(key)));
			}
		}

		uploadDir = File.separator + productCode + getPropertyValue("upload.dir");
		licdir = File.separator + productCode + getPropertyValue("lic.dir");
		edidir = File.separator + productCode + getPropertyValue("edi.dir");
	}

	/**
	 * 加载配置文件
	 * 
	 * @return
	 */
	public static Properties getProp() {
		return prop;
	}

	/**
	 * 根据key返回某个配置项的值
	 * 
	 * @param key
	 * @return
	 */
	public static String getPropertyValue(String key) {
		if(prop == null) {
			prop = getProp();
		}
		try {
			String value = prop.getProperty(key);
			if(value != null) {
				value = value.trim();
			}
			return value;
		} catch(Exception e) {

		}
		return null;
	}

	public static int getIntValue(String key) {
		String value = getPropertyValue(key);
		int iValue = 0;
		try {
			iValue = Integer.parseInt(value);
		} catch(Exception e) {
		}
		return iValue;
	}

	public static boolean getBooleanValue(String key) {
		String value = getPropertyValue(key);
		boolean bValue = false;
		try {
			bValue = Boolean.parseBoolean(value);
		} catch(Exception e) {
		}
		return bValue;
	}

	/**
	 * 返回
	 * 
	 * @return
	 */
	public static String getResourceVersion() {
		if(version == null) {
			StringBuffer sb = new StringBuffer();
			try {
				InputStream in = Global.class.getResourceAsStream(resourveVersionFile);
				int n;
				while((n = in.read()) != -1) {
					sb.append((char) n);
				}
				in.close();
				version = sb.toString();
			} catch(Exception e) {
				e.printStackTrace();
				if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
					throw new RuntimeException("读取资源版本时出错，出错信息：" + e.getMessage());
				}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
					throw new RuntimeException("Error reading resource version, error information:" + e.getMessage());
				}
				throw new RuntimeException("读取资源版本时出错，出错信息：" + e.getMessage());
			}
			if(version == null || version.length() == 0) {
				if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
					throw new RuntimeException("没有读取到资源版本号，请检查配置文件resource.version！");
				}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
					throw new RuntimeException("Did not read the resource version number, please check the configuration file resource.version!");
				}
				throw new RuntimeException("没有读取到资源版本号，请检查配置文件resource.version！");
			}
		}
		return version;
	}
}
