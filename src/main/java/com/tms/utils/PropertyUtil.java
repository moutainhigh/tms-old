package com.tms.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {
	
	/**
	 * 加载属性配置文件，这里是加载配置信息
	 */
	public static Properties configs;
	
	static{
	InputStream inputStreamOfConfigs = PropertyUtil.class.getClassLoader().getResourceAsStream("configs.properties");
	   configs = new Properties();
	   try{
		   configs.load(inputStreamOfConfigs);
	   } catch (IOException e1){
	    e1.printStackTrace();
	   }
	}
	
	public static String getConfigsValue(String key){
		return (String) configs.get(key);
	}
}
