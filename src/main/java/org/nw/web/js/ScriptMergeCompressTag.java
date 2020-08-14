package org.nw.web.js;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 使用YUIcompressor 压缩JS/Css文件　并压缩多个JS/Css文件到一个文件
 */
public class ScriptMergeCompressTag extends BodyTagSupport {
	private static final long serialVersionUID = 5889107866036330689L;
	private static final Log logger = LogFactory
			.getLog(ScriptMergeCompressTag.class);

	// 该正则表达式用于分析出标签中的JS代码部分
	// 即：<script type="text/javascript" src="/js/test.js"></script>部分
	private static final Pattern SCRIPT_PATTERN = Pattern.compile(
			"<script\\s+[^>]*src=[\"']?([^>\"']*)[\"']?[^>]*><\\/script>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	// 该正则表达式用于分析出标签中的CSS代码部分
	// 即：<link rel="stylesheet" type="text/css" href="style.css"></script>部分
	private static final Pattern CSS_PATTERN = Pattern
			.compile("<link\\s+[^>]*href=[\"']?([^>\"']*)[\"']?[^>]*><\\/link>");

	// 存储：合并后的文件地址->合并前所有JS文件的字节数
	Map<String, Long> fileSizeMap = new HashMap<String, Long>();

	/**
	 * 合并后的JS路径
	 */
	private String jsTo;

	private String cssTo;

	/**
	 * 仅仅合并，不压缩
	 */
	private boolean onlyMerge = false;

	public int doAfterBody() {
		BodyContent body = getBodyContent();
		String content = body.getString();
		JspWriter out = body.getEnclosingWriter();
		try {
			Matcher jsMatcher = SCRIPT_PATTERN.matcher(content);
			String webroot = pageContext.getServletContext().getRealPath("/");
			// 合并后的文件，自动创建父目录
			File jsOutfile = null, cssOutfile = null;
			if (getJsTo() != null) {
				jsOutfile = new File(webroot, getJsTo());
			}
			if (getCssTo() != null) {
				cssOutfile = new File(webroot, getCssTo());
			}
			if (jsOutfile != null && !jsOutfile.getParentFile().exists()) {
				jsOutfile.getParentFile().mkdirs();
			}
			if (cssOutfile != null && !cssOutfile.getParentFile().exists()) {
				cssOutfile.getParentFile().mkdirs();
			}

			if (jsOutfile != null) {
				/**
				 * 处理 JS文件
				 */
				// 用于保存合并前的js文件地址
				List<File> jsFiles = new ArrayList<File>();
				long jsLength = 0;
				// 从标签体中解析出js文件，加入列表中，并计算所有文件的字节数的和
				while (jsMatcher.find()) {
					String path = jsMatcher.group(1);
					File file = new File(webroot, path);
					if (!file.exists()) {
						logger.warn("javascript文件不存在：" + file.getPath());
						continue;
					}
					jsLength += file.length();
					jsFiles.add(file);
				}

				// 如果合并后的文件不存在 或者 内存中没有记录合并的记录 或者
				// 合并记录中存在但长度与重新计算的文件长度不同，则执行合并操作
				if (!jsOutfile.exists()
						|| !fileSizeMap.containsKey(getJsTo())
						|| (fileSizeMap.containsKey(getJsTo()) && fileSizeMap
								.get(getJsTo()) != jsLength)) {
					if (onlyMerge) {
						mergeFile(jsFiles, jsOutfile);
					} else {
						Compressor.compress(jsFiles, jsOutfile);
					}

					fileSizeMap.put(getJsTo(), jsLength);
					logger.debug("文件:" + getJsTo() + " 大小有改变，合成JS文件　．．．");
				} else {
					logger.debug("文件:" + getJsTo() + " 大小没有改变，使用缓存中的地址");
				}
			}

			if (cssOutfile != null) {
				/**
				 * 处理 css文件
				 */
				Matcher cssMatcher = CSS_PATTERN.matcher(content);
				// 用于保存合并前的css文件地址
				List<File> cssFiles = new ArrayList<File>();
				long cssLength = 0;
				while (cssMatcher.find()) {
					String path = cssMatcher.group(1);
					File file = new File(webroot, path);
					if (!file.exists()) {
						logger.warn("css文件不存在：" + file.getPath());
						continue;
					}
					cssLength += file.length();
					cssFiles.add(file);
				}

				// 如果合并后的文件不存在 或者 内存中没有记录合并的记录 或者
				// 合并记录中存在但长度与重新计算的文件长度不同，则执行合并操作
				if (cssOutfile != null
						&& !cssOutfile.exists()
						|| !fileSizeMap.containsKey(getCssTo())
						|| (fileSizeMap.containsKey(getCssTo()) && fileSizeMap
								.get(getCssTo()) != cssLength)) {
					if (onlyMerge) {
						mergeFile(cssFiles, cssOutfile);
					} else
						Compressor.compressCss(cssFiles, cssOutfile);

					fileSizeMap.put(getCssTo(), cssLength);
					logger.debug("文件:" + getCssTo() + " 大小有改变，合成CSS文件　．．．");
				} else {
					logger.debug("文件:" + getCssTo() + " 大小没有改变，使用缓存中的地址");
				}
			}
		} catch (Exception e) {
			// 出错时不做让任何处理，返回原始内容
			try {
				out.println(content);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			logger.warn("合并JS异常", e);
		}
		return SKIP_BODY;
	}

	private void mergeFile(List<File> fileList, File outfile)
			throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outfile);
			for (File file : fileList) {
				byte[] bytes = FileUtils.readFileToByteArray(file);
				out.write(bytes);
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public String getJsTo() {
		return jsTo;
	}

	public void setJsTo(String jsTo) {
		this.jsTo = jsTo;
	}

	public String getCssTo() {
		return cssTo;
	}

	public void setCssTo(String cssTo) {
		this.cssTo = cssTo;
	}

	public boolean isOnlyMerge() {
		return onlyMerge;
	}

	public void setOnlyMerge(boolean onlyMerge) {
		this.onlyMerge = onlyMerge;
	}
}
