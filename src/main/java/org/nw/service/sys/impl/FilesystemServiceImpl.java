package org.nw.service.sys.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.nw.apache.tools.zip.ZipEntry;
import org.nw.apache.tools.zip.ZipOutputStream;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.FilesystemService;
import org.nw.utils.PropertiesUtils;
import org.nw.utils.office.OfficeConvertUtil;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.FilesystemVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

/**
 * 
 * @author xuqc
 * @date 2013-8-12 下午09:56:28
 */
@Service
public class FilesystemServiceImpl extends AbsToftServiceImpl implements FilesystemService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, FilesystemVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, FilesystemVO.PK_FILESYSTEM);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			return " order by ts";
		}
		return orderBy;
	}

	/**
	 * 文件表比较大，使用物理删除
	 */
	protected boolean isLogicalDelete() {
		return false;
	}

	public String[] getLoadDataFields() {
		return new String[] { "pk_filesystem", "dr", "ts", "billtype", "pk_bill", "file_name", "file_size",
				"folder_flag", "parent_id", "md5", "create_user", "create_time" };
	}

	public void upload(final FilesystemVO attachVO, final InputStream in) {
		NWDao dao = NWDao.getInstance();
		String sql = "insert into nw_filesystem(pk_filesystem, dr,ts,billtype,pk_bill,folder_flag,file_name,file_size, parent_id,md5,create_user,create_time,contentdata) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		dao.getJdbcTemplate().execute(sql, new PreparedStatementCallback<FilesystemVO>() {
			public FilesystemVO doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ps.setObject(1, attachVO.getPk_filesystem());
				ps.setObject(2, attachVO.getDr());
				ps.setObject(3, attachVO.getTs().toString());
				ps.setObject(4, attachVO.getBilltype());
				ps.setObject(5, attachVO.getPk_bill());
				ps.setObject(6, attachVO.getFolder_flag());
				ps.setObject(7, attachVO.getFile_name());
				ps.setObject(8, attachVO.getFile_size());
				ps.setObject(9, attachVO.getParent_id());
				ps.setObject(10, attachVO.getMd5());
				ps.setObject(11, attachVO.getCreate_user());
				ps.setObject(12, attachVO.getCreate_time().toString());
				ps.setBinaryStream(13, in, attachVO.getFile_size().intValue());
				ps.execute();
				return attachVO;
			}
		});
	}

	public void upload(final List<FilesystemVO> attachVOs, final List<InputStream> inAry) {
		if(attachVOs == null || inAry == null) {
			return;
		}
		for(int i = 0; i < attachVOs.size(); i++) {
			upload(attachVOs.get(i), inAry.get(i));
		}
	}

	public FilesystemVO getByPrimaryKey(String primaryKey) {
		String sql = "select pk_filesystem, dr,ts,billtype,pk_bill,folder_flag,file_name,file_size,parent_id,md5,create_user,create_time "
				+ "from nw_filesystem WITH(NOLOCK) where pk_filesystem=?";
		return NWDao.getInstance().getJdbcTemplate()
				.queryForObject(sql, new String[] { primaryKey }, new FilesystemRowMapper());
	}

	@SuppressWarnings({ "rawtypes" })
	public void download(String pk_filesystem, OutputStream out) throws Exception {
		String sql = "select contentdata from nw_filesystem WITH(NOLOCK) where pk_filesystem=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_filesystem);
		if(retMap == null) {
			logger.warn("您下载的文件已经不存在,pk_filesystem=" + pk_filesystem);
			throw new BusiException("您下载的文件已经不存在，请刷新页面！");
		}
		Object contentdata = retMap.get("contentdata");
		if(contentdata != null) {
			InputStream in = null;
			if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
				in = new ByteArrayInputStream((byte[]) contentdata);
			} else {
				in = (InputStream) contentdata;
			}
			IOUtils.copy(in, out);
		}
	}

	@SuppressWarnings("rawtypes")
	public void zipDownload(String[] pk_filesystem, OutputStream out) {
		String tmpdirPath = Constants.TMPDIR;
		File tmpdir = new File(tmpdirPath);
		if(!tmpdir.exists()) {
			tmpdir.mkdir();
		}
		for(String pk : pk_filesystem) {
			String sql = "select file_name,contentdata from nw_filesystem WITH(NOLOCK) where pk_filesystem=?";
			HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk);
			if(retMap == null) {
				logger.warn("您下载的文件已经不存在,pk_filesystem=" + pk);
				continue;
			}
			String file_name = retMap.get("file_name").toString();// 文件名称
			File file = new File(tmpdirPath + File.separator + file_name);// 创建一个用于存储数据库文件的临时文件
			Object contentdata = retMap.get("contentdata"); // 从数据库中读取了inputstream，写入临时文件中
			InputStream in = null;
			if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
				in = new ByteArrayInputStream((byte[]) contentdata);
			} else {
				in = (InputStream) contentdata;
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				byte[] b = new byte[1024];
				int readLength;
				while((readLength = in.read(b)) != -1) {
					fos.write(b, 0, readLength);
				}
			} catch(Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					if(fos != null) {
						fos.flush();
						fos.close();
					}
					if(in != null) {
						in.close();
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		zipFile(tmpdir, out, "");
		try {
			FileUtils.deleteDirectory(tmpdir);// 删除临时文件
		} catch(IOException e) {
			logger.error("删除临时目录错误：" + e.getMessage());
		}
	}

	/**
	 * 文件夹打包下载
	 * 
	 * @param dir
	 * @param zipOut
	 * @param parent
	 * @author xuqc
	 * @date 2012-10-17
	 * 
	 */
	private void zipFile(File dir, OutputStream out, String parent) {
		if(!dir.isDirectory()) {
			throw new BusiException("[?]不是一个文件夹！",dir.getName());
		}
		ZipOutputStream zipOut = new ZipOutputStream(out);
		zipOut.setEncoding("GBK");
		// 写入压缩流
		try {
			for(File file : dir.listFiles()) {
				if(file.isFile()) {
					// 文件
					ZipEntry zipEntry = new ZipEntry(parent + file.getName());
					zipOut.putNextEntry(zipEntry);
					FileInputStream in = new FileInputStream(file);
					byte[] b = new byte[1024];
					int readLength;
					while((readLength = in.read(b)) != -1) {
						zipOut.write(b, 0, readLength);
					}
					zipOut.closeEntry();
					in.close();
				} else {
					// 目录
					zipFile(file, zipOut, file.getName() + File.separator);
				}
			}
		} catch(Exception e) {
			logger.error("生成压缩文件出错:" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				zipOut.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String configFileName = "/openoffice.properties";

	private static Properties prop = null;

	private static String[] to_convert_ext = new String[] { "doc", "docx", "xls", "xlsx", "ppt", "pptx" };

	static {
		if(prop == null) {
			try {
				String path = WebUtils.getClientConfigPath() + configFileName;
				File configPropFile = new File(path);
				prop = PropertiesUtils.loadProperties(new FileInputStream(configPropFile));
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static Properties getProp() {
		return prop;
	}

	@SuppressWarnings("rawtypes")
	public void previewByAspose(String pk_filesystem, OutputStream output) throws Exception {
		// 通过服务器的openoffice转换
		FilesystemVO attachVO = this.getByPrimaryKey(pk_filesystem);
		if(attachVO == null) {
			throw new BusiException("您预览的文件已经不存在，请刷新页面！");
		}
		String fileName = attachVO.getFile_name();
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1);// 后缀
		String sql = "select contentdata from nw_filesystem WITH(NOLOCK) where pk_filesystem=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_filesystem);
		if(retMap == null) {
			logger.warn("您下载的文件已经不存在,pk_filesystem=" + pk_filesystem);
			throw new BusiException("您下载的文件已经不存在，请刷新页面！");
		}
		Object contentdata = retMap.get("contentdata");
		if(contentdata != null) {
			InputStream srcInput = (InputStream) contentdata;
			boolean flag = false;
			for(String s : to_convert_ext) {
				if(s.equalsIgnoreCase(ext)) {
					flag = true;
					break;
				}
			}
			if(flag) {
				String htmlStr = OfficeConvertUtil.transformWordToHTML(srcInput);
				output.write(htmlStr.getBytes());
			} else {
				IOUtils.copy(srcInput, output);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void preview(String pk_filesystem, OutputStream output) throws Exception {
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String sHtmlDir = webappPath + File.separator + "tmp" + File.separator + "html";
		File htmlDir = new File(sHtmlDir);
		if(!htmlDir.exists()) {
			htmlDir.mkdirs();
		}
		String sHtmlTmpFile = sHtmlDir + File.separator + pk_filesystem + File.separator + pk_filesystem + ".html";
		File htmlTmpFile = new File(sHtmlTmpFile);
		if(htmlTmpFile.exists()) {
			// 已经存在，直接读取
			InputStream input = null;
			try {
				input = new FileInputStream(htmlTmpFile);
				IOUtils.copy(input, output);
			} finally {
				IOUtils.closeQuietly(output);
			}
		} else {
			// 通过服务器的openoffice转换
			FilesystemVO attachVO = this.getByPrimaryKey(pk_filesystem);
			if(attachVO == null) {
				throw new BusiException("您预览的文件已经不存在，请刷新页面！");
			}
			String fileName = attachVO.getFile_name();
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);// 后缀
			File srcFile = new File(sHtmlDir + File.separator + fileName);

			String sql = "select contentdata from nw_filesystem WITH(NOLOCK) where pk_filesystem=?";
			HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_filesystem);
			if(retMap == null) {
				logger.warn("您下载的文件已经不存在,pk_filesystem=" + pk_filesystem);
				throw new BusiException("您下载的文件已经不存在，请刷新页面！");
			}
			Object contentdata = retMap.get("contentdata");
			if(contentdata != null) {
				InputStream srcInput = (InputStream) contentdata;
				boolean flag = false;
				for(String s : to_convert_ext) {
					if(s.equalsIgnoreCase(ext)) {
						flag = true;
						break;
					}
				}
				if(flag) {
					try {
						srcFile = writeTmpFile(srcFile, srcInput);// 写入文件
						covertByOpenOffice(srcFile, htmlTmpFile);
						String htmlStr = FileUtils.readFileToString(htmlTmpFile);
						// 返回经过清洁的html文本,特别是图片
						htmlStr = clearFormat(htmlStr, ServletContextHolder.getRequest().getContextPath()
								+ "/tmp/html/" + pk_filesystem);// 这里不能使用File.separator,在window下会被忽略，导致目录不正确
						FileUtils.writeStringToFile(htmlTmpFile, htmlStr, "utf-8");// 重新回写到文件
						output.write(htmlStr.getBytes());
					} finally {
						if(srcFile != null) {
							srcFile.delete();
						}
					}
				} else {
					// 直接预览
					try {
						IOUtils.copy(srcInput, output);
					} finally {
						IOUtils.closeQuietly(output);
					}
				}
			}
		}
	}

	/**
	 * 使用OpenOffice转化文件
	 * 
	 * @param src
	 * @param dist
	 * @throws Exception
	 */
	public void covertByOpenOffice(File srcFile, File destFile) throws Exception {
		String ip = getProp().getProperty("server.ip").trim();
		int port = 8100;
		try {
			port = Integer.parseInt(getProp().getProperty("server.port").trim());
		} catch(Exception e) {

		}
		OpenOfficeConnection connection = new SocketOpenOfficeConnection(ip, port);
		try {
			connection.connect();
			DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
			converter.convert(srcFile, destFile);
		} catch(Exception e) {
			e.printStackTrace();
			// logger.error("文件在转换成html时出错，错误信息：" + e.getMessage());
			throw new BusiException(e);
		} finally {
			connection.disconnect();
		}
	}

	public File writeTmpFile(File file, InputStream is) throws Exception {
		OutputStream inputFileStream = null;
		try {
			inputFileStream = new FileOutputStream(file);
			IOUtils.copy(is, inputFileStream);
		} finally {
			IOUtils.closeQuietly(inputFileStream);
		}
		return file;
	}

	/**
	 * 
	 * 清除一些不需要的html标记
	 * 
	 * 
	 * 
	 * @param htmlStr
	 * 
	 *            带有复杂html标记的html语句
	 * 
	 * @return 去除了不需要html标记的语句
	 */

	protected String clearFormat(String htmlStr, String docImgPath) {
		// 获取body内容的正则
		// String bodyReg = "<BODY .*</BODY>";
		// Pattern bodyPattern = Pattern.compile(bodyReg);
		// Matcher bodyMatcher = bodyPattern.matcher(htmlStr);
		// if(bodyMatcher.find()) {
		// // 获取BODY内容，并转化BODY标签为DIV
		// htmlStr = bodyMatcher.group().replaceFirst("<BODY",
		// "<DIV").replaceAll("</BODY>", "</DIV>");
		// }
		// logger.info("docImgPath---------------------:" + docImgPath);
		// System.out.println("docImgPath---------------------:" + docImgPath);
		// 调整图片地址
		htmlStr = htmlStr.replaceAll("<IMG SRC=\"", "<IMG SRC=\"" + docImgPath + "/");
		htmlStr = htmlStr.replaceAll("text/html; charset=gb2312", "text/html; charset=utf-8");
		// 把<P></P>转换成</div></div>保留样式
		// content = content.replaceAll("(<P)([^>]*>.*?)(<\\/P>)",
		// "<div$2</div>");
		// 把<P></P>转换成</div></div>并删除样式
		// htmlStr = htmlStr.replaceAll("(<P)([^>]*)(>.*?)(<\\/P>)",
		// "<p$3</p>");
		// // 删除不需要的标签
		// htmlStr = htmlStr.replaceAll(
		// "<[/]?(font|FONT|span|SPAN|xml|XML|del|DEL|ins|INS|meta|META|[ovwxpOVWXP]:\\w+)[^>]*?>",
		// "");
		// // 删除不需要的属性
		// htmlStr = htmlStr
		// .replaceAll(
		// "<([^>]*)(?:lang|LANG|class|CLASS|style|STYLE|size|SIZE|face|FACE|[ovwxpOVWXP]:\\w+)=(?:'[^']*'|\"\"[^\"\"]*\"\"|[^>]+)([^>]*)>",
		// "<$1$2>");

		return htmlStr;
	}

	public static String getOfficeHome() {
		return getProp().getProperty("office.home").trim();
	}

	public static void main(String[] args) {
		File srcFile = new File("/work/soft/2015-03-14bug.docx");
		File destFile = new File("/work/soft/1.html");
		FilesystemServiceImpl service = new FilesystemServiceImpl();
		try {
			service.covertByOpenOffice(srcFile, destFile);
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
