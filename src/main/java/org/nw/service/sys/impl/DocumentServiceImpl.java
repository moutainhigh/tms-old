package org.nw.service.sys.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.nw.apache.tools.zip.ZipEntry;
import org.nw.apache.tools.zip.ZipOutputStream;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.DocumentService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.DocumentVO;
import org.nw.web.utils.WebUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

/**
 * 文档管理，政策文件管理
 * 
 * @author xuqc
 * @date 2013-9-13 上午10:50:33
 */
@Service
public class DocumentServiceImpl extends AbsToftServiceImpl implements DocumentService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, DocumentVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, DocumentVO.PK_DOCUMENT);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public List<DocumentVO> getTop5() {
		String[] fieldAry = getLoadDataFields();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < fieldAry.length; i++) {
			sb.append(fieldAry[i]);
			if(i != fieldAry.length - 1) {
				sb.append(", ");
			}
		}
		String sql = "select top 5 " + sb.toString()
				+ " from nw_document WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select " + sb.toString()
			+ " from nw_document where rownum < 6 and isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 " + sb.toString()
			+ " from nw_document WITH(NOLOCK) where isnull(dr,0)=0 and pk_corp=? order by post_date desc";
		}
		List<DocumentVO> list = NWDao.getInstance().queryForList(sql, DocumentVO.class,
				WebUtils.getLoginInfo().getPk_corp());
		return list;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		values.put(DocumentVO.POST_DATE, DateUtils.getCurrentDate());
		return values;
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = " pk_corp='" + WebUtils.getLoginInfo().getPk_corp() + "' ";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and ";
			fCond += cond;
		}
		return fCond;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by post_date desc";
		}
		return orderBy;
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		AggregatedValueObject aggVO = new HYBillVO();
		DocumentVO docVO = NWDao.getInstance().queryByCondition(DocumentVO.class, getLoadDataFields(), "pk_document=?",
				paramVO.getBillId());
		aggVO.setParentVO(docVO);
		return aggVO;
	}

	public String[] getLoadDataFields() {
		return new String[] { DocumentVO.PK_DOCUMENT, DocumentVO.DR, DocumentVO.TS, DocumentVO.CREATE_TIME,
				DocumentVO.CREATE_USER, DocumentVO.DEF1, DocumentVO.DEF2, DocumentVO.DEF3, DocumentVO.DEF4,
				DocumentVO.DEF5, DocumentVO.FILE_NAME, DocumentVO.FILE_SIZE, DocumentVO.FILENO, DocumentVO.POST_DATE,
				DocumentVO.POST_ORG, DocumentVO.SERIALNO, DocumentVO.SUMMARY, DocumentVO.TITLE, DocumentVO.PK_CORP };
	}

	public DocumentVO getByPrimaryKey(String primaryKey) throws Exception {
		String[] fieldAry = getLoadDataFields();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < fieldAry.length; i++) {
			sb.append(fieldAry[i]);
			if(i != fieldAry.length - 1) {
				sb.append(", ");
			}
		}
		String sql = "select " + sb.toString() + " from nw_document WITH(NOLOCK) where pk_document=?";
		return NWDao.getInstance().getJdbcTemplate()
				.queryForObject(sql, new String[] { primaryKey }, new DocumentRowMapper());
	}

	public Map<String, Object> upload(ParamVO paramVO, final DocumentVO docVO, final InputStream in) {
		NWDao dao = NWDao.getInstance();
		String sql = "insert into nw_document(pk_document, dr,ts,title,summary,serialno,post_org,post_date, "
				+ "fileno,file_size,file_name,def1,def2,def3,def4,def5,create_user,create_time,pk_corp,contentdata) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		dao.getJdbcTemplate().execute(sql, new PreparedStatementCallback<DocumentVO>() {
			public DocumentVO doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ps.setObject(1, docVO.getPk_document());
				ps.setObject(2, docVO.getDr());
				ps.setObject(3, docVO.getTs().toString());
				ps.setObject(4, docVO.getTitle());
				ps.setObject(5, docVO.getSummary());
				ps.setObject(6, docVO.getSerialno());
				ps.setObject(7, docVO.getPost_org());
				ps.setObject(8, docVO.getPost_date().toString());
				ps.setObject(9, docVO.getFileno());
				ps.setObject(10, docVO.getFile_size());
				ps.setObject(11, docVO.getFile_name());
				ps.setObject(12, docVO.getDef1());
				ps.setObject(13, docVO.getDef2());
				ps.setObject(14, docVO.getDef3());
				ps.setObject(15, docVO.getDef4());
				ps.setObject(16, docVO.getDef5());
				ps.setObject(17, docVO.getCreate_user());
				ps.setObject(18, docVO.getCreate_time().toString());
				ps.setObject(19, docVO.getPk_corp());
				ps.setBinaryStream(20, in, docVO.getFile_size().intValue());
				ps.execute();
				return docVO;
			}
		});
		HYBillVO billVO = new HYBillVO();
		billVO.setParentVO(docVO);
		return this.execFormula4Templet(billVO, paramVO);
	}

	@SuppressWarnings("rawtypes")
	public void download(String pk_document, OutputStream out) throws Exception {
		String sql = "select contentdata from nw_document WITH(NOLOCK) where pk_document=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_document);
		if(retMap == null) {
			logger.warn("您下载的文件已经不存在,pk_document=" + pk_document);
			throw new BusiException("您下载的文件已经不存在，请刷新页面！");
		}
		Object contentdata = retMap.get("contentdata");
		if(contentdata != null) {
			InputStream in = (InputStream) contentdata;
			IOUtils.copy(in, out);
		}
	}

	@SuppressWarnings("rawtypes")
	public void zipDownload(String[] pk_document, OutputStream out) throws Exception {
		String tmpdirPath = Constants.TMPDIR;
		File tmpdir = new File(tmpdirPath);
		if(!tmpdir.exists()) {
			tmpdir.mkdir();
		}
		for(String pk : pk_document) {
			String sql = "select file_name,contentdata from nw_document WITH(NOLOCK) where pk_document=?";
			HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk);
			if(retMap == null) {
				logger.warn("您下载的文件已经不存在,pk_document=" + pk);
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
		ZipOutputStream zipOut = new ZipOutputStream(out);
		zipOut.setEncoding("GBK");
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
}
