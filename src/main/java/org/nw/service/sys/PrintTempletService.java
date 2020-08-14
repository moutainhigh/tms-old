package org.nw.service.sys;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nw.jf.vo.PrintTempletVO;
import org.nw.service.IToftService;
import org.nw.vo.TreeVO;


/**
 * 打印模板操作接口
 * 
 * @author xuqc
 * @date 2012-12-1 下午02:52:06
 */
public interface PrintTempletService extends IToftService {

	public List<TreeVO> getFunCodeTree();

	/**
	 * 上传模板文件，只支持jrxml文件
	 * 
	 * @param attachVO
	 * @param in
	 *            上传文件的原始输入流
	 */
	public void uploadTemplet(final PrintTempletVO attachVO, final InputStream in);

	/**
	 * 根据主键查询，不返回内容字段
	 * 
	 * @param <T>
	 * @param primaryKey
	 * @return
	 */
	public PrintTempletVO getByPrimaryKey(String primaryKey);

	/**
	 * 下载打印模板
	 * 
	 * @param pk_print_templet
	 * @param out
	 * @throws Exception
	 */
	public void downloadTemplet(String pk_print_templet, OutputStream out) throws Exception;
}
