package org.nw.utils.office;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;

/**
 * Aspose转换工具类
 * 
 */
public class OfficeConvertUtil {

	static Logger logger = Logger.getLogger(OfficeConvertUtil.class);

	/**
	 * 获取license
	 * 
	 * @return
	 */
	public static boolean getLicense() {
		boolean result = false;
		InputStream is = OfficeConvertUtil.class.getClassLoader().getResourceAsStream(
				"org/nw/utils/office/Aspose.Words.lic");
		com.aspose.words.License aposeLic = new com.aspose.words.License();
		try {
			aposeLic.setLicense(is);
			result = true;
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 把word转为html，并返回word的内容
	 * 
	 * @param inStream
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static String transformWordToHTML(InputStream inStream) throws Exception {
		String imageFolderPath = getFolderPath();
		File file = new File(imageFolderPath);
		if(!file.exists()) {
			file.mkdir();
		}
		imageFolderPath += File.separator + "wordimages";
		file = new File(imageFolderPath);
		if(!file.exists()) {
			file.mkdir();
		}
		imageFolderPath += File.separator + WebUtils.getLoginInfo().getPk_user();
		String imageFolderAliaPath = imageFolderPath + File.separator + "alia";
		return transformWordToHTML(inStream, imageFolderPath, imageFolderAliaPath);

	}

	/**
	 * 把excel转为html，并返回html的内容
	 * 
	 * @param inStream
	 * @param request
	 * @return
	 * @throws Exception
	 */
	// public static String transformExcelToHTML(InputStream inStream) throws
	// Exception {
	// String outputFile = getFolderPath();
	// outputFile = System.currentTimeMillis() + ".html";
	// transformExcelToHTML(inStream, outputFile);
	// return FileUtils.loadFile2String(outputFile);
	// }

	/**
	 * 用Aspose把文档转为html
	 * 
	 * @param inputFile
	 * @param outputFile
	 *            void
	 * @author wangjb
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @date 2014年1月7日
	 */
	public static void transformDocmentToHTML(String inputFile, String outputFile) throws FileNotFoundException,
			IOException {
		OfficeConvertUtil.getLicense();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(inputFile);
			out = new FileOutputStream(outputFile);
			com.aspose.words.Document doc = new com.aspose.words.Document(in);
			doc.save(out, com.aspose.words.SaveFormat.HTML);
		} catch(Exception e1) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("转换文件出错：" + e1.getMessage());
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Error in file conversion：" + e1.getMessage());
			}
			throw new RuntimeException("转换文件出错：" + e1.getMessage());
		} finally {
			try {
				if(out != null) {
					out.close();
				}
				if(in != null) {
					in.close();
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 将文件转化为HTML
	 * 
	 * @param filePath
	 *            文件名称
	 * @param inStream
	 *            文件流
	 * @param imageFolderPath
	 *            生成word文档时的图片临时存放地址
	 * @param imageFolderAliaPath
	 *            生成word文档时的图片临时存放地址
	 * @param voperatorid
	 *            操作员,作为临时文件夹路径的一部分,可以随便录一个
	 * @return HTML
	 * @throws Exception
	 * @author lankc
	 * @date 2014-1-8 下午3:48:57
	 */
	public static String transformDocmentToHTML(String filePath, InputStream inStream, String imageFolderPath,
			String imageFolderAliaPath, String voperatorid) throws Exception {
		// 1.获取License
		if(!OfficeConvertUtil.getLicense()) {
			logger.error("获取org/nw/utils/office/Aspose.Words.lic异常.");
			return "";
		}
		// 2.调用插件处理
		ByteArrayOutputStream outByteAryStream = new ByteArrayOutputStream();
		try {
			com.aspose.words.Document doc = new com.aspose.words.Document(inStream);
			File imagesDir = new File(imageFolderPath, voperatorid);
			// The folder specified needs to exist and should be empty.
			if(imagesDir.exists()) {
				deleteDirectory(imagesDir);
			}
			imagesDir.mkdir();
			// Set an option to export form fields as plain text, not as HTML
			// input elements.
			com.aspose.words.HtmlSaveOptions options = new com.aspose.words.HtmlSaveOptions(
					com.aspose.words.SaveFormat.HTML);
			options.setExportTextInputFormFieldAsText(true);
			options.setImagesFolder(imagesDir.getPath());
			options.setImagesFolderAlias(imageFolderAliaPath + File.separator + voperatorid);
			// options.setEncoding(Charset.forName("utf-8"));
			doc.save(outByteAryStream, options);
			return new String(outByteAryStream.toByteArray(), "utf-8");
		} catch(Exception e) {
			logger.error("转换文件出错.", e);
			throw e;
		} finally {
			if(outByteAryStream != null) {
				outByteAryStream.close();
			}
			if(inStream != null) {
				inStream.close();
			}
		}
	}

	public static String transformWordToHTML(InputStream inStream, String imageFolderPath, String imageFolderAliaPath)
			throws Exception {
		// 1.获取License
		AsposeLicense.getInstance().setWordsLicense();
		// 2.调用插件处理
		ByteArrayOutputStream outByteAryStream = new ByteArrayOutputStream();
		try {
			com.aspose.words.Document doc = new com.aspose.words.Document(inStream);
			File imagesDir = new File(imageFolderPath);
			// The folder specified needs to exist and should be empty.
			if(imagesDir.exists()) {
				deleteDirectory(imagesDir);
			}
			imagesDir.mkdir();
			// Set an option to export form fields as plain text, not as HTML
			// input elements.
			com.aspose.words.HtmlSaveOptions options = new com.aspose.words.HtmlSaveOptions(
					com.aspose.words.SaveFormat.HTML);
			options.setExportTextInputFormFieldAsText(true);
			options.setImagesFolder(imagesDir.getPath());
			options.setImagesFolderAlias(imageFolderAliaPath);
			// options.setEncoding(Charset.forName("utf-8"));
			doc.save(outByteAryStream, options);
			return new String(outByteAryStream.toByteArray(), "utf-8");
		} catch(Exception e) {
			logger.error("转换文件出错.", e);
			throw e;
		} finally {
			if(outByteAryStream != null) {
				outByteAryStream.close();
			}
			if(inStream != null) {
				inStream.close();
			}
		}
	}

	/**
	 * 把excle转为html
	 * 
	 * @param inStream
	 *            excel的文件流
	 * @param outputFile
	 *            生成的html地址
	 * @return
	 * @throws Exception
	 */
	// public static void transformExcelToHTML(InputStream inStream, String
	// outputFile) throws Exception {
	// // 2.调用插件处理
	// try {
	// AsposeLicense.getInstance().setCellsLicense();
	// com.aspose.cells.Workbook workbook = new
	// com.aspose.cells.Workbook(inStream);
	// com.aspose.cells.HtmlSaveOptions options = new
	// com.aspose.cells.HtmlSaveOptions(
	// com.aspose.cells.SaveFormat.HTML);
	// workbook.save(outputFile, options);
	// } catch(Exception e) {
	// throw e;
	// } finally {
	// if(inStream != null) {
	// inStream.close();
	// }
	// }
	// }

	// FIXME by sunyp 2015-03-04 ppt的转换依赖1.6的jar包，代码编译基于1.5会报错。这个方法现在也没人用注释掉。
	// public static void transformPPTToHTML(InputStream inStream, String
	// outputFile) throws Exception {
	// // 2.调用插件处理
	// try {
	// AsposeLicense.getInstance().setPPTLicense();
	// com.aspose.slides.Presentation ppt = new
	// com.aspose.slides.Presentation(inStream);
	// com.aspose.slides.HtmlOptions htmlOpt = new
	// com.aspose.slides.HtmlOptions();
	// ppt.save(outputFile, com.aspose.slides.SaveFormat.Html, htmlOpt);
	// } catch (Exception e) {
	// throw e;
	// } finally {
	// if (inStream != null) {
	// inStream.close();
	// }
	// }
	// }

	public static String getFolderPath() {
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String sHtmlDir = webappPath + "tmp" + File.separator + "html";
		return sHtmlDir;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param dirFile
	 *            被删除目录
	 * @author lankc
	 * @date 2014-1-7 下午4:03:38
	 */
	private static void deleteDirectory(File dirFile) {
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for(int i = 0; i < files.length; i++) {
			// 删除子文件
			if(files[i].exists() && files[i].isFile()) {
				files[i].delete();
			}
		}
		dirFile.delete();
	}
}
