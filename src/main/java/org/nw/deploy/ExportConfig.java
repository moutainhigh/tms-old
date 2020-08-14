package org.nw.deploy;

import java.io.File;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * 读取配置文件
 * 
 * @author xuqc
 * @date 2014-5-19 下午09:00:53
 */
public class ExportConfig {

	private static Logger logger = Logger.getLogger(ExportConfig.class.getName());

	private static String db_properties = "exporter.xml";
	// 初始化系统时导出的配置文件
	private static String init_db_properties = "exporter-init.xml";

	public static Document getDocument(boolean ifInit) {
		String baseFolder = ExportConfig.class.getResource("/").getPath();
		logger.info("根目录：" + baseFolder);
		String config_file_path = baseFolder + "package/" + (ifInit ? init_db_properties : db_properties);
		logger.info("数据库配置文件路径：" + config_file_path);
		File config_file = new File(config_file_path);
		if(!config_file.exists()) {
			String s = "数据库配置文件不存在，文件路径：" + config_file_path;
			logger.finest(s);
			throw new RuntimeException(s);
		}
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(config_file);
		} catch(DocumentException e) {
			logger.finest("解析xml文件错误，错误信息：" + e.getMessage());
			e.printStackTrace();
		}
		return document;
	}
}
