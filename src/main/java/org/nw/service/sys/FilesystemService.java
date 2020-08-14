package org.nw.service.sys;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.sys.FilesystemVO;

/**
 * 单据附件的处理接口
 * 
 * @author xuqc
 * @date 2013-8-12 下午09:55:45
 */
public interface FilesystemService extends IToftService {
	/**
	 * 上传附件
	 * 
	 * @param attachVO
	 * @param in
	 */
	public void upload(final FilesystemVO attachVO, final InputStream in);

	/**
	 * 上传附件，多个文件一起上传
	 * 
	 * @param attachVOs
	 * @param inAry
	 */
	public void upload(final List<FilesystemVO> attachVOs, final List<InputStream> inAry);

	/**
	 * 根据主键返回vo，不包括文件内容字段
	 * 
	 * @param primaryKey
	 * @return
	 */
	public FilesystemVO getByPrimaryKey(String primaryKey) throws Exception;

	/**
	 * 下载单个文件
	 * 
	 * @param pk_filesystem
	 * @return
	 */
	public void download(String pk_filesystem, OutputStream out) throws Exception;

	/**
	 * 打包下载
	 * 
	 * @param pk_filesystem
	 * @param out
	 */
	public void zipDownload(String[] pk_filesystem, OutputStream out) throws Exception;

	/**
	 * 预览单个文件
	 * 
	 * @param pk_filesystem
	 * @return
	 */
	public void preview(String pk_filesystem, OutputStream out) throws Exception;

	public void previewByAspose(String pk_filesystem, OutputStream output) throws Exception;
}
