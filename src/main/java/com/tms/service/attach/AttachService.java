package com.tms.service.attach;

import java.io.OutputStream;
import java.util.List;

import org.nw.service.IToftService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.springframework.web.multipart.MultipartFile;

import com.tms.vo.attach.AttachmentVO;

/**
 * 附件上传
 * 
 * @author yaojiie
 * @date 2015 11 24 
 */
public interface AttachService extends IToftService {

	
	//yaojiie 2015 11 23 签收回单上传
	public void  uploadAttachment(List<MultipartFile> fileAry, ParamVO paramVO, List<String> pks);
	
	//yaojiie 2015 11 23 签收回单下载
	public void downloadAttachment(AttachmentVO attachmentVO, OutputStream out) throws Exception;

	
	//yaojiie 2015 11 23 签收回单批量下载
	public void zipAttachmentDownload(String[] pk_attachment, OutputStream out) throws Exception;
		
	//yaojiie 2015 11 23 附件预览
	public void previewAttachment(AttachmentVO attachmentVO, OutputStream output) throws Exception;
	
	//yaojiie 2015 11 23 附件删除
	public void deleteAttachment(Class<? extends SuperVO> clazz, String[] primaryKeys) throws Exception;
	/**
	 * 上传货物跟踪异常附件
	 * @author:zhuyj
	 * @for:
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public String uploadAtta(MultipartFile file,String pkCorp,String userId,String billType,String billId) throws Exception;
	
	/**
	 * 专门用于站内信的附件上传
	 * @author XIA
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public String uploadSmsAttachment(MultipartFile file) throws Exception;
}
