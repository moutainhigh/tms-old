package org.nw.basic.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * 提供文件读写操作静态方法的类
 */
public class FileUtils {

	/**
	 * load file to a String with input encoding
	 * 
	 * @param fileName
	 *            要读取的文件名
	 * @param encoding
	 *            解析该文件的编码,与要读取的文件编码相同
	 * @return String 以String返回文件内容
	 */
	public static String loadFile2String(String fileName, String encoding) {
		try {
			StringBuffer sb = new StringBuffer("");
			File file = new File(fileName);
			InputStreamReader in = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader br = new BufferedReader(in);
			String str = br.readLine();
			while(str != null) {
				sb.append(str);
				str = br.readLine();
			}
			br.close();
			in.close();
			return sb.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * load file to a String with default encoding
	 * 
	 * @param fileName
	 *            要读取的文件名
	 * @return String 以String返回文件内容
	 */
	public static String loadFile2String(String fileName) {
		try {
			StringBuffer sb = new StringBuffer("");
			File file = new File(fileName);
			FileInputStream in = new FileInputStream(file);
			int n;
			while((n = in.read()) != -1) {
				sb.append((char) n);
			}
			in.close();
			return sb.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将字符串info写入文件file中
	 * 
	 * @param file
	 * @param info
	 * @param isAppend
	 *            若文件file已经存在,true表示追加该字符串,false表示替换该字符串
	 */
	public static void writerFile(String file, String info, boolean isAppend) {
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(new File(file), isAppend));
			bf.write(info);
			bf.flush();
			bf.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将字符串写入文件,并定义文件的编码
	 * 
	 * @param file
	 *            要写入的文件
	 * @param info
	 *            要写入文件的字符串
	 * @param encoding
	 *            文件的编码
	 */
	public static void writerFile(String file, String info, String encoding) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), encoding);
			out.write(info);
			out.flush();
			out.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将字符串写入文件,并定义文件的编码
	 * 
	 * @param file
	 *            要写入的文件
	 * @param info
	 *            要写入文件的字符串
	 */
	public static void writerFile(String file, String info) {
		writerFile(file, info, "utf-8");
	}

	/**
	 * 新建目录
	 * 
	 * @param path
	 *            待新建目录
	 */
	public static void mkdir(String path) {
		path = path.toString(); // 中文转换
		File myFilePath = new File(path);
		if(!myFilePath.exists()) {
			myFilePath.mkdir();
		}
	}

	/**
	 * 新建一个文件
	 * 
	 * @param file
	 *            待新建的文件
	 */
	public static void newFile(String file) {
		try {
			file = file.toString();
			File myFilePath = new File(file);
			if(!myFilePath.exists())
				myFilePath.createNewFile();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 删除指定文件
	 * 
	 * @param file
	 *            待删除的文件
	 */
	public static void delFile(String file) {
		file = file.toString();
		File myDelFile = new File(file);
		myDelFile.delete();
	}

	/**
	 * 文件拷贝
	 * 
	 * @param sourceFile
	 *            待拷贝的文件
	 * @param destFile
	 *            目标文件
	 */
	public static void copyFile(String sourceFile, String destFile) {
		try {
			//			int bytesum = 0;
			int byteread = 0;
			InputStream is = new FileInputStream(sourceFile);
			FileOutputStream fs = new FileOutputStream(destFile);
			byte[] buffer = new byte[1444];
			while((byteread = is.read(buffer)) != -1) {
				//				bytesum += byteread;
				fs.write(buffer, 0, byteread);
			}
			is.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean move(String srcFile, String destPath) {
		File file = new File(srcFile);
		File dir = new File(destPath);
		return file.renameTo(new File(dir, file.getName()));
	}

	/**
	 * 目录拷贝
	 * 
	 * @param sourceDir
	 *            待拷贝的目录
	 * @param destDir
	 *            目标目录
	 */
	public static void copyDir(String sourceDir, String destDir) {
		try {
			File[] file = (new File(sourceDir)).listFiles();
			for(int i = 0; i < file.length; i++) {
				if(file[i].isFile()) {
					FileInputStream input = new FileInputStream(file[i]);
					FileOutputStream output = new FileOutputStream(destDir + "/" + (file[i].getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 把网页保存成文件
	 * 
	 * @param url
	 *            网页所在的网址如："http://www.163.com"
	 * @param destFile
	 *            网页要保存在本地的文件
	 */
	public static void saveFile(String url, String destFile) {
		URL stdURL = null;
		BufferedReader stdIn = null;
		PrintWriter stdOut = null;
		try {
			stdURL = new URL(url);
		} catch(MalformedURLException e) {
			throw new RuntimeException(e);
		}

		try {
			stdIn = new BufferedReader(new InputStreamReader(stdURL.openStream()));
			stdOut = new PrintWriter(new BufferedWriter(new FileWriter(destFile)));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		/** *把URL指定的页面以流的形式读出，写成指定的文件** */
		try {
			String strHtml = "";
			while((strHtml = stdIn.readLine()) != null) {
				stdOut.println(strHtml);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(stdIn != null)
					stdIn.close();
				if(stdOut != null)
					stdOut.close();
			} catch(Exception e) {

			}
		}
	}

	/**
	 * 直接下载网上的文件
	 * 
	 * @param sourceFile
	 *            远程文件路径,如："http://pimg.163.com/sms/micheal/logo.gif"
	 * @param destFile
	 *            保存到本地的文件
	 */
	public static void downloadFile(String sourceFile, String destFile) {
		try {
			//			int bytesum = 0;
			int byteread = 0;

			URL url = new URL(sourceFile);
			URLConnection conn = url.openConnection();
			InputStream inStream = conn.getInputStream();
			FileOutputStream fs = new FileOutputStream(destFile);

			byte[] buffer = new byte[1444];
			while((byteread = inStream.read(buffer)) != -1) {
				//				bytesum += byteread;
				fs.write(buffer, 0, byteread);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copy the contents of the given InputStream to the given OutputStream.
	 * Closes both streams when done.
	 * 
	 * @param in the stream to copy from
	 * @param out the stream to copy to
	 * @return the number of bytes copied
	 * @throws IOException in case of I/O errors
	 */
	public static int copy(InputStream in, OutputStream out) throws IOException {
		Assert.notNull(in, "No InputStream specified");
		Assert.notNull(out, "No OutputStream specified");
		try {
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		} finally {
			try {
				in.close();
			} catch(IOException ex) {
			}
			try {
				out.close();
			} catch(IOException ex) {
			}
		}
	}

	/**
	 * Copies a file to a new location preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 * 
	 * @param srcFile an existing file to copy, must not be <code>null</code>
	 * @param destFile the new file, must not be <code>null</code>
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File)
	 */
	public static void copyFile(File srcFile, File destFile) throws IOException {
		copyFile(srcFile, destFile, true);
	}

	/**
	 * Copies a file to a new location.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 * 
	 * @param srcFile an existing file to copy, must not be <code>null</code>
	 * @param destFile the new file, must not be <code>null</code>
	 * @param preserveFileDate true if the file date of the copy
	 *            should be the same as the original
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File, boolean)
	 */
	public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		if(srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if(destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if(srcFile.exists() == false) {
			throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		}
		if(srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' exists but is a directory");
		}
		if(srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
			throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
		}
		if(destFile.getParentFile() != null && destFile.getParentFile().exists() == false) {
			if(destFile.getParentFile().mkdirs() == false) {
				throw new IOException("Destination '" + destFile + "' directory cannot be created");
			}
		}
		if(destFile.exists() && destFile.canWrite() == false) {
			throw new IOException("Destination '" + destFile + "' exists but is read-only");
		}
		doCopyFile(srcFile, destFile, preserveFileDate);
	}

	/**
	 * Internal copy file method.
	 * 
	 * @param srcFile the validated source file, must not be <code>null</code>
	 * @param destFile the validated destination file, must not be
	 *            <code>null</code>
	 * @param preserveFileDate whether to preserve the file date
	 * @throws IOException if an error occurs
	 */
	private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		if(destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		FileInputStream input = new FileInputStream(srcFile);
		try {
			FileOutputStream output = new FileOutputStream(destFile);
			try {
				IOUtils.copy(input, output);
			} finally {
				IOUtils.closeQuietly(output);
			}
		} finally {
			IOUtils.closeQuietly(input);
		}

		if(srcFile.length() != destFile.length()) {
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		}
		if(preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}

	//-----------------------------------------------------------------------
	/**
	 * Copies a directory to within another directory preserving the file dates.
	 * <p>
	 * This method copies the source directory and all its contents to a
	 * directory of the same name in the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that those operations will succeed. If the modification
	 * operation fails, no indication is provided.
	 * 
	 * @param srcDir an existing directory to copy, must not be
	 *            <code>null</code>
	 * @param destDir the directory to place the copy in, must not be
	 *            <code>null</code>
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since Commons IO 1.2
	 */
	public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
		if(srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if(srcDir.exists() && srcDir.isDirectory() == false) {
			throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
		}
		if(destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if(destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
		}
		copyDirectory(srcDir, new File(destDir, srcDir.getName()), true);
	}

	/**
	 * Copies a whole directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the specified directory and all its child directories
	 * and files to the specified destination. The destination is the new
	 * location and name of the directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that those operations will succeed. If the modification
	 * operation fails, no indication is provided.
	 * 
	 * @param srcDir an existing directory to copy, must not be
	 *            <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since Commons IO 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir) throws IOException {
		copyDirectory(srcDir, destDir, true);
	}

	/**
	 * Copies a whole directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * <code>true</code> tries to preserve the files' last modified date/times
	 * using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that those operations will succeed. If the modification operation fails,
	 * no indication is provided.
	 * 
	 * @param srcDir an existing directory to copy, must not be
	 *            <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @param preserveFileDate true if the file date of the copy
	 *            should be the same as the original
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since Commons IO 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
		copyDirectory(srcDir, destDir, null, preserveFileDate);
	}

	/**
	 * Copies a filtered directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that those operations will succeed. If the modification
	 * operation fails, no indication is provided.
	 * <h4>Example: Copy directories only</h4>
	 * 
	 * <pre>
	 * // only copy the directory structure
	 * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
	 * </pre>
	 * 
	 * <h4>Example: Copy directories and txt files</h4>
	 * 
	 * <pre>
	 * // Create a filter for &quot;.txt&quot; files
	 * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(&quot;.txt&quot;);
	 * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 * 
	 * // Create a filter for either directories or &quot;.txt&quot; files
	 * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 * 
	 * // Copy using the filter
	 * FileUtils.copyDirectory(srcDir, destDir, filter);
	 * </pre>
	 * 
	 * @param srcDir an existing directory to copy, must not be
	 *            <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @param filter the filter to apply, null means copy all directories and
	 *            files
	 *            should be the same as the original
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since Commons IO 1.4
	 */
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
		copyDirectory(srcDir, destDir, filter, true);
	}

	/**
	 * Copies a filtered directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * <code>true</code> tries to preserve the files' last modified date/times
	 * using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that those operations will succeed. If the modification operation fails,
	 * no indication is provided.
	 * <h4>Example: Copy directories only</h4>
	 * 
	 * <pre>
	 * // only copy the directory structure
	 * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
	 * </pre>
	 * 
	 * <h4>Example: Copy directories and txt files</h4>
	 * 
	 * <pre>
	 * // Create a filter for &quot;.txt&quot; files
	 * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(&quot;.txt&quot;);
	 * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 * 
	 * // Create a filter for either directories or &quot;.txt&quot; files
	 * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 * 
	 * // Copy using the filter
	 * FileUtils.copyDirectory(srcDir, destDir, filter, false);
	 * </pre>
	 * 
	 * @param srcDir an existing directory to copy, must not be
	 *            <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @param filter the filter to apply, null means copy all directories and
	 *            files
	 * @param preserveFileDate true if the file date of the copy
	 *            should be the same as the original
	 * @throws NullPointerException if source or destination is
	 *             <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since Commons IO 1.4
	 */
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate)
			throws IOException {
		if(srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if(destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if(srcDir.exists() == false) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if(srcDir.isDirectory() == false) {
			throw new IOException("Source '" + srcDir + "' exists but is not a directory");
		}
		if(srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
			throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
		}

		// Cater for destination being directory within the source directory (see IO-141)
		List<String> exclusionList = null;
		if(destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
			if(srcFiles != null && srcFiles.length > 0) {
				exclusionList = new ArrayList<String>(srcFiles.length);
				for(File srcFile : srcFiles) {
					File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
	}

	/**
	 * Internal copy directory method.
	 * 
	 * @param srcDir the validated source directory, must not be
	 *            <code>null</code>
	 * @param destDir the validated destination directory, must not be
	 *            <code>null</code>
	 * @param filter the filter to apply, null means copy all directories and
	 *            files
	 * @param preserveFileDate whether to preserve the file date
	 * @param exclusionList List of files and directories to exclude from the
	 *            copy, may be null
	 * @throws IOException if an error occurs
	 * @since Commons IO 1.1
	 */
	private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate,
			List<String> exclusionList) throws IOException {
		// recurse
		File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
		if(files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + srcDir);
		}
		if(destDir.exists()) {
			if(destDir.isDirectory() == false) {
				throw new IOException("Destination '" + destDir + "' exists but is not a directory");
			}
		} else {
			if(destDir.mkdirs() == false) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
		}
		if(destDir.canWrite() == false) {
			throw new IOException("Destination '" + destDir + "' cannot be written to");
		}
		for(File file : files) {
			File copiedFile = new File(destDir, file.getName());
			if(exclusionList == null || !exclusionList.contains(file.getCanonicalPath())) {
				if(file.isDirectory()) {
					doCopyDirectory(file, copiedFile, filter, preserveFileDate, exclusionList);
				} else {
					doCopyFile(file, copiedFile, preserveFileDate);
				}
			}
		}

		// Do this last, as the above has probably affected directory metadata
		if(preserveFileDate) {
			destDir.setLastModified(srcDir.lastModified());
		}
	}

}