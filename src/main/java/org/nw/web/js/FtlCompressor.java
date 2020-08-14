package org.nw.web.js;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.FileUtils;

/**
 * 压缩ftl文件中的空格、换行、注释
 * 
 * @author xuqc
 * @date 2011-3-17
 */
public class FtlCompressor {

	public static final String ENCODE = "UTF-8";

	/**
	 * 输出文件，编码为UTF-8 用记事本另存为：fileContent 全部为英文则为ansi 包含中文则为UTF-8
	 * 
	 * @param content
	 *            要输出的文件内容
	 * @param comspec
	 *            全路径名
	 */
	private static void writeFile(String content, String comspec) {
		try {
			FileOutputStream fos = new FileOutputStream(comspec);
			Writer out = new OutputStreamWriter(fos, ENCODE);
			out.write(content);
			System.out.println("成功输出文件：" + comspec);
			out.close();
			fos.close();
		} catch(IOException e) {
			System.out.println("写文件操作出错！");
			e.printStackTrace();
		}
	}

	/**
	 * 读取文件内容
	 * 
	 * @param filePath
	 * @return String
	 */
	private static String readFile(String filePath) {
		StringBuilder sb = new StringBuilder();
		try {
			File file = new File(filePath);
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), ENCODE);
			BufferedReader reader = new BufferedReader(read);
			String s = reader.readLine();
			while(s != null) {
				sb.append(s);
				sb.append(" ");
				s = reader.readLine();
			}
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	/**
	 * 替换空格、换行、tab
	 * 
	 * @param content
	 * @return
	 */
	public static String compress(String content) {
		// 引用文件修改
		content = content.replaceAll("bill-marco.ftl", "bill-marco-bin.ftl");
		content = content.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "");
		// 当前的ftl模板有一个html注释在body中，如果移出该注释会出现js错误，所以不移出该注释
		// content = removeHtmlNote(content);
		content = removeFreeMarkerNote(content);
		content = content.replaceAll("  ", ""); // 去除空格，一个空格不能去除
		return content;
	}

	/**
	 * 去除注释
	 * 
	 * @param content
	 * @return
	 * @author xuqc
	 * @date 2011-9-26
	 * 
	 */
	private static String removeNote(String content, String notePrefix, String noteSuffix) {
		StringBuffer sb = new StringBuffer();
		String remain = content;
		int prefixIndex = remain.indexOf(notePrefix);
		while(prefixIndex != -1) {
			sb.append(remain.substring(0, prefixIndex));
			remain = remain.substring(prefixIndex + 4);
			int suffixIndex = remain.indexOf(noteSuffix);
			if(suffixIndex > -1) {
				remain = remain.substring(suffixIndex + 3);
			} else {
				sb.append(notePrefix);
			}
			prefixIndex = remain.indexOf(notePrefix);
		}
		sb.append(remain);
		return sb.toString();
	}

	/**
	 * 移除html注释
	 * 
	 * @param content
	 * @return
	 * @author xuqc
	 * @date 2012-3-27
	 * 
	 */
	public static String removeHtmlNote(String content) {
		String notePrefix = "<!--";
		String noteSuffix = "-->";
		return removeNote(content, notePrefix, noteSuffix);
	}

	/**
	 * 移除FreeMarker注释
	 * 
	 * @param content
	 * @return
	 * @author xuqc
	 * @date 2012-3-27
	 * 
	 */
	public static String removeFreeMarkerNote(String content) {
		String notePrefix = "<#--";
		String noteSuffix = "-->";
		return removeNote(content, notePrefix, noteSuffix);
	}

	public static void compress(String from, String to) {
		String content = readFile(from);
		if(StringUtils.isNotBlank(content)) {
			writeFile(compress(content), to);
		}
	}

	public static void main(String[] args) {
		String projectPath = System.getProperty("user.dir");
		String targetPath = projectPath + File.separator + "target" + File.separator + "classes" + File.separator
				+ "template" + File.separator;
		String sourcePath = projectPath + File.separator + "src" + File.separator + "main" + File.separator
				+ "resources" + File.separator + "template" + File.separator;
		String[] from = new String[] { "bill-marco.ftl", "bill.ftl", "report.ftl" };
		String[] to = new String[] { "bill-marco-bin.ftl", "bill-bin.ftl", "report-bin.ftl" };
		for(int i = 0; i < from.length; i++) {
			String fromFile = sourcePath + from[i];
			String toFile = sourcePath + to[i];
			System.out.println("开始压缩文件：[" + fromFile + "],目标文件：[" + toFile + "]...");
			FtlCompressor.compress(fromFile, toFile);
			// 将执行后的目录拷贝到maven的编译目录
			System.out.println("开始拷贝文件：[" + toFile + "]到目标文件：[" + targetPath + to[i] + "]...");
			FileUtils.copyFile(toFile, targetPath + to[i]);
		}

	}
}
