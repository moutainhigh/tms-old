package org.nw.service.sys;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.ParamVO;
import org.nw.vo.sys.DocumentVO;

/**
 * 
 * @author xuqc
 * @date 2013-9-13 上午10:50:05
 */
public interface DocumentService extends IToftService {

	public List<DocumentVO> getTop5();

	/**
	 * 根据主键返回vo，不包括文件内容字段
	 * 
	 * @param primaryKey
	 * @return
	 */
	public DocumentVO getByPrimaryKey(String primaryKey) throws Exception;

	/**
	 * 上传文档
	 * 
	 * @param docVO
	 * @param in
	 */
	public Map<String, Object> upload(ParamVO paramVO, final DocumentVO docVO, final InputStream in);

	/**
	 * 下载单个文件
	 * 
	 * @param pk_filesystem
	 * @return
	 */
	public void download(String pk_document, OutputStream out) throws Exception;

	/**
	 * 打包下载
	 * 
	 * @param pk_document
	 * @param out
	 */
	public void zipDownload(String[] pk_document, OutputStream out) throws Exception;
}
