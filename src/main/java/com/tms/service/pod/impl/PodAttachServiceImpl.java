package com.tms.service.pod.impl;

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
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.PropertiesUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.tms.service.pod.PodAttachService;
import com.tms.vo.pod.PodAttachVO;
import com.tms.vo.pod.PodVO;

/**
 * POD签收单
 * 
 * @author xuqc
 * @date 2013-4-17 下午09:33:13
 */
//zhuyj 对外接口依据beanName获取对象实例
@Service("podAttachService")
public class PodAttachServiceImpl extends AbsToftServiceImpl implements PodAttachService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PodAttachVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PodAttachVO.PK_POD_ATTACH);
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

	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys) {
		logger.info("根据主键删除pod附件。");
		if(primaryKeys == null || primaryKeys.length == 0) {
			throw new BusiException("删除操作的主键不能为空！");
		}
		PodAttachVO attachVO = getByPrimaryKey(primaryKeys[0]);
		String pk_invoice = attachVO.getPk_invoice();
		String sql = "select count(1) from ts_pod_attach WITH(NOLOCK) where pk_invoice=?";
		Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, pk_invoice);
		if(count == primaryKeys.length) {
			// 将上传状态更新成为删除
			PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pk_invoice=?", pk_invoice);
			podVO.setUpload_flag(UFBoolean.FALSE);
			podVO.setStatus(VOStatus.UPDATED);
			NWDao.getInstance().saveOrUpdate(podVO);
		}
		return super.batchDelete(clazz, primaryKeys);
	}

	// 物理删除
	public int deleteByPrimaryKey(Class<? extends SuperVO> clazz, String primaryKey) {
		if(StringUtils.isBlank(primaryKey)) {
			throw new BusiException("删除操作的主键不能为空！");
		}
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			throw new BusiException(e.getMessage());
		}
		String sql = "delete from " + superVO.getTableName() + " where " + superVO.getPKFieldName() + "=?";
		return dao.update(sql, primaryKey);
	}

	public String[] getLoadDataFields() {
		return new String[] { "pk_pod_attach", "dr", "ts", "pk_invoice", "file_name", "suffix", "memo", "create_user",
				"create_time", "file_size" };
	}

	public void uploadPodAttach(final PodAttachVO attachVO, final InputStream in) {
		logger.info("上传POD附件...");
		NWDao dao = NWDao.getInstance();
		String sql = "insert into ts_pod_attach(pk_pod_attach, dr,ts,pk_invoice,file_name,file_size, suffix, memo,create_user,create_time,contentdata) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?)";
		dao.getJdbcTemplate().execute(sql, new PreparedStatementCallback<PodAttachVO>() {
			public PodAttachVO doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ps.setObject(1, attachVO.getPk_pod_attach());
				ps.setObject(2, attachVO.getDr());
				ps.setObject(3, attachVO.getTs().toString());
				ps.setObject(4, attachVO.getPk_invoice());
				ps.setObject(5, attachVO.getFile_name());
				ps.setObject(6, attachVO.getFile_size());
				ps.setObject(7, attachVO.getSuffix());
				ps.setObject(8, attachVO.getMemo());
				ps.setObject(9, attachVO.getCreate_user());
				ps.setObject(10, attachVO.getCreate_time().toString());
				ps.setBinaryStream(11, in, attachVO.getFile_size().intValue());
				ps.execute();
				return attachVO;
			}
		});
	}

	public void uploadPodAttach(final List<PodAttachVO> attachVOs, final List<InputStream> inAry) {
		if(attachVOs == null || inAry == null) {
			return;
		}
		for(int i = 0; i < attachVOs.size(); i++) {
			uploadPodAttach(attachVOs.get(i), inAry.get(i));
		}
		String pk_invoice = attachVOs.get(0).getPk_invoice();
		PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk_invoice);
		podVO.setUpload_flag(UFBoolean.TRUE);
		podVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(podVO);
	}

	public PodAttachVO getByPrimaryKey(String primaryKey) {
		String sql = "select pk_pod_attach, dr,ts,pk_invoice,file_name,file_size, suffix, memo,create_user,create_time "
				+ "from ts_pod_attach where pk_pod_attach=?";
		return NWDao.getInstance().getJdbcTemplate()
				.queryForObject(sql, new String[] { primaryKey }, new PodAttachRowMapper());
	}

	@SuppressWarnings({ "rawtypes" })
	public void downloadPodAttach(String pk_pod_attach, OutputStream out) throws Exception {
		logger.info("下载POD附件，pk_pod_attach" + pk_pod_attach);
		String sql = "select contentdata from ts_pod_attach where pk_pod_attach=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_pod_attach);
		if(retMap == null) {
			logger.warn("您下载的文件已经不存在,pk_pod_attach=" + pk_pod_attach);
			throw new BusiException("您下载的文件已经不存在，请刷新页面！");
		}
		Object contentdata = retMap.get("contentdata");
		if(contentdata != null) {
			InputStream in = (InputStream) contentdata;
			byte[] b = new byte[1024];
			while(in.read(b) != -1) {
				out.write(b);
			}
			out.close();
			in.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public void zipDownload(String[] pk_pod_attach, OutputStream out) {
		logger.info("打包下载POD附件...");
		String tmpdirPath = Constants.TMPDIR;
		File tmpdir = new File(tmpdirPath);
		if(!tmpdir.exists()) {
			tmpdir.mkdir();
		}
		for(String pk : pk_pod_attach) {
			String sql = "select file_name,contentdata from ts_pod_attach where pk_pod_attach=?";
			HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk);
			if(retMap == null) {
				logger.warn("您下载的文件已经不存在,pk_pod_attach=" + pk);
				continue;
			}
			String file_name = retMap.get("file_name").toString();// 文件名称
			File file = new File(tmpdirPath + File.separator + file_name);// 创建一个用于存储数据库文件的临时文件
			InputStream in = (InputStream) retMap.get("contentdata"); // 从数据库中读取了inputstream，写入临时文件中
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
		ZipOutputStream zipOut = new ZipOutputStream(out);
		zipOut.setEncoding("GBK");
		zipFile(tmpdir, zipOut, "");
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
	private void zipFile(File dir, ZipOutputStream zipOut, String parent) {
		if(!dir.isDirectory()) {
			throw new BusiException("[?]不是一个文件夹！",dir.getName());
		}
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

	public void preview(String pk_pod_attach, OutputStream output) throws Exception {
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String sHtmlDir = webappPath + File.separator + "tmp" + File.separator + "html";
		File htmlDir = new File(sHtmlDir);
		if(!htmlDir.exists()) {
			htmlDir.mkdirs();
		}
		String sHtmlTmpFile = sHtmlDir + File.separator + pk_pod_attach + File.separator + pk_pod_attach + ".html";
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
			PodAttachVO attachVO = this.getByPrimaryKey(pk_pod_attach);
			if(attachVO == null) {
				throw new BusiException("您预览的文件已经不存在，请刷新页面！");
			}
			String fileName = attachVO.getFile_name();
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);// 后缀
			File srcFile = new File(sHtmlDir + File.separator + fileName);

			String sql = "select contentdata from ts_pod_attach where pk_pod_attach=?";
			HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_pod_attach);
			if(retMap == null) {
				logger.warn("您下载的文件已经不存在,pk_filesystem=" + pk_pod_attach);
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
								+ "/tmp/html/" + pk_pod_attach);// 这里不能使用File.separator,在window下会被忽略，导致目录不正确
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
}
