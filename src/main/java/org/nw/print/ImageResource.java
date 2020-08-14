package org.nw.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nw.web.utils.WebUtils;

/**
 * 打印模板中可以使用的图片资源
 * 
 * @author xuqc
 * @date 2014-12-26 上午11:47:30
 */
public class ImageResource {

	Logger logger = Logger.getLogger(ImageResource.class);

	private static String path;
	static {
		path = WebUtils.getClientConfigPath() + File.separator + "print";
	}

	/**
	 * 加载打印所需要的image，打印模板中可以使用
	 * 
	 * @return
	 */
	public Map<String, InputStream> load() {
		Map<String, InputStream> imageMap = new HashMap<String, InputStream>();
		try {
			logger.info("加载打印图片资源，文件路径：" + path);
			File printFolder = new File(path);
			if(printFolder.exists()) {
				// 存在print资源目录
				File[] fileAry = printFolder.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if(name.toLowerCase().endsWith("png")) {
							return true;
						}
						return false;
					}
				});
				if(fileAry != null) {
					logger.info("总共将加载" + fileAry.length + "个文件！");
					for(int i = 0; i < fileAry.length; i++) {
						if(!fileAry[i].exists()) {
							// 不可能不存在
						} else {
							String filename = fileAry[i].getName();
							String key = filename.substring(0, filename.lastIndexOf("."));
							logger.info("加载文件[" + filename + "]...");
							// 设置logo参数
							imageMap.put("_" + key, new FileInputStream(fileAry[i]));
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("加载打印模板中所需要的图片资源时出错，错误信息：" + e.getMessage());
			e.printStackTrace();
		}
		return imageMap;
	}

	/**
	 * 返回打印所需图片的根路径
	 * 
	 * @return
	 */
	public static String getRootPath() {
		return path;
	}
}
