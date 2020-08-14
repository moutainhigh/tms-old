package com.tms.service.pod;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nw.service.IToftService;

import com.tms.vo.pod.PodAttachVO;

/**
 * POD签收单
 * 
 * @author xuqc
 * @date 2013-4-17 下午09:32:32
 */
public interface PodAttachService extends IToftService {

	/**
	 * 上传pod签收单
	 * 
	 * @param attachVO
	 * @param in
	 */
	public void uploadPodAttach(final PodAttachVO attachVO, final InputStream in);

	/**
	 * 上传pod签收单，多个文件一起上传
	 * 
	 * @param attachVOs
	 * @param inAry
	 */
	public void uploadPodAttach(final List<PodAttachVO> attachVOs, final List<InputStream> inAry);

	/**
	 * 根据主键返回vo，不包括文件内容字段
	 * 
	 * @param primaryKey
	 * @return
	 */
	public PodAttachVO getByPrimaryKey(String primaryKey) throws Exception;

	/**
	 * 下载单个文件
	 * 
	 * @param pk_pod_attach
	 * @return
	 */
	public void downloadPodAttach(String pk_pod_attach, OutputStream out) throws Exception;

	/**
	 * 打包下载
	 * 
	 * @param pk_pod_attach
	 * @param out
	 */
	public void zipDownload(String[] pk_pod_attach, OutputStream out) throws Exception;

	/**
	 * 预览
	 * 
	 * @param pk_pod_attach
	 * @param output
	 * @throws Exception
	 */
	public void preview(String pk_pod_attach, OutputStream output) throws Exception;
}
