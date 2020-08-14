package org.nw.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

/**
 * 该类为关键性配置提供读取和写入服务，所以，必须保证读取和写入成功，否则直接抛出RuntimeException，程序没必要再执行下去
 * 
 * @author fangw
 */
public class PropertiesUtils {

	/**
	 * 读取properties的信息
	 * 
	 * @param filePath
	 */
	public static Properties loadProperties(String path) {
		InputStream in = null;
		try {
			Properties props = new Properties();
			in = new BufferedInputStream(new FileInputStream(path));
			props.load(in);
			return props;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Properties loadProperties(InputStream is) {
		InputStream in = null;
		try {
			Properties props = new Properties();
			in = new BufferedInputStream(is);
			props.load(in);
			return props;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写入属性文件
	 * 
	 * @param path
	 *            属性文件的相对classpath的路径，如果在根，直接传文件名即可
	 * @param parameterName
	 * @param parameterValue
	 */
	public static void writeProperties(String path, String parameterName,
			String parameterValue) {
		InputStream in = null;
		OutputStream os = null;
		try {
			Properties prop = new Properties();
			in = new BufferedInputStream(new FileInputStream(path));
			prop.load(in);
			// 写入新属性
			prop.setProperty(parameterName, parameterValue);
			os = new FileOutputStream(path);
			prop.store(os, "Update '" + parameterName + "' value");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// readValue("config.properties", "test");
		URL url = ClassLoader.getSystemResource("config.properties");

		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(url.getPath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writeProperties("config.properties", "test", "123");
		// readProperties("config.properties");
		System.out.println("OK");
	}
}
