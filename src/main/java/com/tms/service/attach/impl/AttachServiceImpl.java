package com.tms.service.attach.impl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.nw.Global;
import org.nw.apache.tools.zip.ZipEntry;
import org.nw.apache.tools.zip.ZipOutputStream;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.NWUtils;
import org.nw.utils.PropertiesUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.SmsVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.tms.constants.BillTypeConst;
import com.tms.service.attach.AttachService;
import com.tms.vo.attach.AttachmentVO;
import com.tms.vo.cm.PayCheckSheetVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.ReceCheckSheetVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;


@Service
public class AttachServiceImpl extends AbsToftServiceImpl implements AttachService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AttachmentVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AttachmentVO.PK_ATTACHMENT);
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
	 * 不需要加载默认条件
	 */
	public String buildDefaultCond(UiQueryTempletVO queryTempletVO) {
		return "";
	}
	
	@Override
	public String buildImmobilityCond(UiQueryTempletVO queryTempletVO) {
		return "";
	}

	//yaojiie 2015 11 25 通过主键获取VO
	public AttachmentVO getByPrimaryKey(String primaryKey) {
		AttachmentVO attachmentVO = NWDao.getInstance().queryByCondition(AttachmentVO.class, "pk_attachment = ?", primaryKey);
		return attachmentVO;
	}

	//yaojiie 2015 11 25  将文件打包
	private void zipFile(File dir, ZipOutputStream zipOut, String parent) {
		if(!dir.isDirectory()) {
			throw new BusiException("?不是一个文件夹！",dir.getName());
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


	//yaojiie 2015 11 25   使用OpenOffice转化文件
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


	protected String clearFormat(String htmlStr, String docImgPath) {

		return htmlStr;
	}

	public static String getOfficeHome() {
		return getProp().getProperty("office.home").trim();
	}

	//yaojiie 2015 11 23 添加POD模块，签单附件上传功能
	public void uploadAttachment(List<MultipartFile> fileAry, ParamVO paramVO, List<String> pks){
		//VOs 包含需要新建的附件VO和需要修改flag的模块对应的VO 如PodVO InvoiceVO等。
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		String bill_type = paramVO.getBillType();
		String cond = NWUtils.buildConditionString(pks.toArray(new String[pks.size()]));
		if(StringUtils.isNotBlank(bill_type)){
			//对签单和回单的判断
			if(bill_type.equals(BillTypeConst.POD_SIGN) || (bill_type.equals(BillTypeConst.POD_RECEIPT))){
				PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", paramVO.getBillId());
				if(bill_type.equals(BillTypeConst.POD_SIGN)){
					podVO.setSign_flag(UFBoolean.TRUE);
				}else{
					podVO.setReceipt_flag(UFBoolean.TRUE);
				}
				podVO.setStatus(VOStatus.UPDATED);
				VOs.add(podVO);
			}
			//yaojiie 2015 12 08 异常跟踪附件的判断
			if(bill_type.equals(BillTypeConst.TRACKING)){
				EntrustVO[] entrustVOs = dao.queryForSuperVOArrayByCondition(EntrustVO.class, "pk_entrust in "+ cond);
				for(EntrustVO entrustVO : entrustVOs){
					entrustVO.setStatus(VOStatus.UPDATED);
					entrustVO.setUpload_flag(UFBoolean.TRUE);
					VOs.add(entrustVO);
				}
			}
			//yaojiie 2016 1 15 异常附件判断
			if(bill_type.equals(BillTypeConst.EXP)){
				ExpAccidentVO expAccidentVO = dao.queryByCondition(ExpAccidentVO.class, "pk_exp_accident=?", paramVO.getBillId());
				expAccidentVO.setStatus(VOStatus.UPDATED);
				expAccidentVO.setUpload_flag(UFBoolean.TRUE);
				VOs.add(expAccidentVO);
			}
			//应收明细
			if(bill_type.equals(BillTypeConst.RD)){
				ReceiveDetailVO receiveDetailVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?", paramVO.getBillId());
				receiveDetailVO.setStatus(VOStatus.UPDATED);
				receiveDetailVO.setUpload_flag(UFBoolean.TRUE);
				VOs.add(receiveDetailVO);
			}
			//应付明细
			if(bill_type.equals(BillTypeConst.PD)){
				PayDetailVO payDetailVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", paramVO.getBillId());
				payDetailVO.setStatus(VOStatus.UPDATED);
				payDetailVO.setUpload_flag(UFBoolean.TRUE);
				VOs.add(payDetailVO);
			}
			//应收对账
			if(bill_type.equals(BillTypeConst.RCS)){
				ReceCheckSheetVO receCheckSheetVO = dao.queryByCondition(ReceCheckSheetVO.class, "pk_rece_check_sheet=?", paramVO.getBillId());
				receCheckSheetVO.setStatus(VOStatus.UPDATED);
				receCheckSheetVO.setUpload_flag(UFBoolean.TRUE);
				VOs.add(receCheckSheetVO);
			}
			//应付对账
			if(bill_type.equals(BillTypeConst.PCS)){
				PayCheckSheetVO payCheckSheetVO = dao.queryByCondition(PayCheckSheetVO.class, "pk_pay_check_sheet=?", paramVO.getBillId());
				payCheckSheetVO.setStatus(VOStatus.UPDATED);
				payCheckSheetVO.setUpload_flag(UFBoolean.TRUE);
				VOs.add(payCheckSheetVO);
			}
			//站内信
			if(bill_type.equals("SMS")){
				SmsVO smsVO = dao.queryByCondition(SmsVO.class, "pk_sms=?", paramVO.getBillId());
				smsVO.setStatus(VOStatus.UPDATED);
				smsVO.setUpload_flag(UFBoolean.TRUE);
				VOs.add(smsVO);
			}
			
		}
		String pk_corp = WebUtils.getLoginInfo().getPk_corp();
		String create_user = WebUtils.getLoginInfo().getPk_user();
		String uploadDir = Global.uploadDir; 
		File dir = new File(uploadDir + File.separator + paramVO.getBillType() + 
	File.separator + DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_HORIZONTAL));
		if(!dir.exists()) {
			dir.mkdirs(); // 若目录不存在，则创建目录
		}
		String extension = ""; // 文件后缀名
		String fileName = "";//文件名
		String fileUrl = "";//文件路径，但实际存的是路径加文件名
		int index = 1;
		for(MultipartFile mfile : fileAry){
			if(StringUtils.isBlank(mfile.getOriginalFilename())){
				continue;
			}
			if(mfile.getOriginalFilename().indexOf(".") != -1) {
				extension = mfile.getOriginalFilename().substring(mfile.getOriginalFilename().lastIndexOf("."));
			}
			String time_name = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
			fileName = time_name + extension;
			fileUrl = dir + File.separator + fileName;
			File file = new File(fileUrl);
			if(file.exists()){
				fileName = time_name +"-"+ index + extension;
				fileUrl = dir + File.separator + fileName;
				index ++;
				file = new File(fileUrl);
			}else{
				index = 1;
			}
			
			try {
				mfile.transferTo(file);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//文件上传文件夹完毕，下面导入数据库中 当存在多个单据时，每个但均都生成一个附件。
			for(String pk : pks){
				AttachmentVO attachmentVO = new AttachmentVO();
				attachmentVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(attachmentVO);
				attachmentVO.setBill_type(paramVO.getBillType());
				attachmentVO.setPk_billno(pk);
				attachmentVO.setPk_corp(pk_corp);
				attachmentVO.setAttachment_name(fileName);
				attachmentVO.setFile_url(fileUrl);
				attachmentVO.setFile_name(mfile.getOriginalFilename());
				attachmentVO.setCreate_user(create_user);
				attachmentVO.setCreate_time(new UFDateTime(new Date()));
				
				VOs.add(attachmentVO);
			}
			
		}
		dao.saveOrUpdate(VOs);
	}
	
	
	//yaojiie 2015 11 23 添加POD模块，签单附件下载功能
	public void downloadAttachment(AttachmentVO attachmentVO, OutputStream out) throws Exception {
		logger.info("下载附件，pk_attachment" + attachmentVO.getAttachment_name());
		String fileName =attachmentVO.getFile_name();
		if(StringUtils.isBlank(fileName)) {
			logger.warn("您下载的文件已经不存在,pk_attachment=" + attachmentVO.getFile_name());
			throw new BusiException("您下载的文件已经不存在，请刷新页面！");
		}
		if(fileName != null) {
			InputStream in = new FileInputStream(attachmentVO.getFile_url());
			byte[] b = new byte[1024];
			while(in.read(b) != -1) {
					out.write(b);
			}
			out.close();
			in.close();
		}
	}
		
	//yaojiie 2015 11 23 签收回单批量下载
	public void zipAttachmentDownload(String[] pk_attachments, OutputStream out) throws Exception {
		logger.info("打包下载POD附件...");
		String tmpdirPath = Constants.TMPDIR;
		File tmpdir = new File(tmpdirPath);
		if(!tmpdir.exists()) {
			tmpdir.mkdirs();
		}
		for(String pk_attachment : pk_attachments) {
			AttachmentVO attachmentVO = NWDao.getInstance().queryByCondition(AttachmentVO.class, "pk_attachment = ?", pk_attachment);
			if(attachmentVO == null) {
				logger.warn("您下载的文件已经不存在,pk_pod_attach=" + pk_attachment);
				continue;
			}
			String file_name = attachmentVO.getAttachment_name();// 这里取我们存在系统里的文件名称
			File file = new File(tmpdirPath + File.separator + file_name);// 创建一个用于存储数据库文件的临时文件
			FileInputStream fin = null; // 从数据库中读取了inputstream，写入临时文件中
			FileOutputStream fos = null;
			try {
				fin = new FileInputStream(attachmentVO.getFile_url());
				fos = new FileOutputStream(file);
				byte[] b = new byte[1024];
				int readLength;
				while((readLength = fin.read(b)) != -1) {
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
					if(fin != null) {
						fin.close();
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
	
	//yaojiie 2015 11 23 附件预览
	public void previewAttachment(AttachmentVO attachmentVO, OutputStream output) throws Exception {
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String sHtmlDir = webappPath + File.separator + "tmp" + File.separator + "html";
		File htmlDir = new File(sHtmlDir);
		if(!htmlDir.exists()) {
			htmlDir.mkdirs();
		}
		String sHtmlTmpFile = sHtmlDir + File.separator + attachmentVO.getPk_attachment() + File.separator + attachmentVO.getPk_attachment() + ".html";
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
			String fileName = attachmentVO.getFile_name();
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);// 后缀
			File srcFile = new File(sHtmlDir + File.separator + fileName);
			if(fileName != null) {
				InputStream srcInput = new FileInputStream(attachmentVO.getFile_url());
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
						htmlStr = clearFormat(htmlStr, ServletContextHolder.getRequest().getContextPath()+ "/tmp/html/" + attachmentVO.getPk_attachment());// 这里不能使用File.separator,在window下会被忽略，导致目录不正确
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
	
	//yaojiie 2015 11 24 附件删除 不删除本地文件只删除数据库记录
	//yaojiie 2015 11 25 当相关附件全部删除时 将对应模块的Flag也标记为删除了。
	public void deleteAttachment(Class<? extends SuperVO> clazz, String[] primaryKeys) throws Exception {
		logger.info("根据主键删除附件。");
		if(primaryKeys == null || primaryKeys.length == 0) {
			throw new BusiException("删除操作的主键不能为空！");
		}
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		String cond = NWUtils.buildConditionString(primaryKeys);
		AttachmentVO[] attachmentVOs =  NWDao.getInstance().queryForSuperVOArrayByCondition(AttachmentVO.class, "pk_attachment in" + cond);
		String pk_billno = attachmentVOs[0].getPk_billno();
		List<String> pk_entrusts = new ArrayList<String>();
		for(AttachmentVO attachmentVO : attachmentVOs){
			if(pk_entrusts.contains(attachmentVO.getPk_billno())){
				continue;
			}
			pk_entrusts.add(attachmentVO.getPk_billno());
		}
		String bill_type = attachmentVOs[0].getBill_type();
		String sql = "select count(1) from ts_attachment WITH(NOLOCK) where isnull(dr,0)=0 and pk_billno=? and bill_type=?";
		Integer count = NWDao.getInstance().queryForObject(sql, Integer.class, pk_billno,bill_type);
		if(attachmentVOs[0].getBill_type().equals(BillTypeConst.POD_RECEIPT) || attachmentVOs[0].getBill_type().equals(BillTypeConst.POD_SIGN)
				|| attachmentVOs[0].getBill_type().equals(BillTypeConst.EXP)
				|| attachmentVOs[0].getBill_type().equals(BillTypeConst.RD)
				|| attachmentVOs[0].getBill_type().equals(BillTypeConst.PD)
				|| attachmentVOs[0].getBill_type().equals(BillTypeConst.RCS)
				|| attachmentVOs[0].getBill_type().equals(BillTypeConst.PCS)
				|| attachmentVOs[0].getBill_type().equals("SMS")){
			if(count == primaryKeys.length) {
				// 将上传状态更新成为删除
				if(attachmentVOs[0].getBill_type().equals(BillTypeConst.POD_RECEIPT)){
					PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pk_invoice=?", pk_billno);
					podVO.setReceipt_flag(UFBoolean.FALSE);
					podVO.setStatus(VOStatus.UPDATED);
					VOs.add(podVO);
				}else if(attachmentVOs[0].getBill_type().equals(BillTypeConst.POD_SIGN)){
					PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pk_invoice=?", pk_billno);
					podVO.setSign_flag(UFBoolean.FALSE);
					podVO.setStatus(VOStatus.UPDATED);
					VOs.add(podVO);
				}else if(attachmentVOs[0].getBill_type().equals(BillTypeConst.EXP)){
					ExpAccidentVO expAccidentVO = dao.queryByCondition(ExpAccidentVO.class, "pk_exp_accident=?",pk_billno);
					expAccidentVO.setUpload_flag(UFBoolean.FALSE);
					expAccidentVO.setStatus(VOStatus.UPDATED);
					VOs.add(expAccidentVO);
				}else if(attachmentVOs[0].getBill_type().equals(BillTypeConst.RD)){
					ReceiveDetailVO receiveDetailVO = dao.queryByCondition(ReceiveDetailVO.class, "pk_receive_detail=?", pk_billno);
					receiveDetailVO.setUpload_flag(UFBoolean.FALSE);
					receiveDetailVO.setStatus(VOStatus.UPDATED);
					VOs.add(receiveDetailVO);
				}else if(attachmentVOs[0].getBill_type().equals(BillTypeConst.PD)){
					PayDetailVO payDetailVO = dao.queryByCondition(PayDetailVO.class, "pk_pay_detail=?", pk_billno);
					payDetailVO.setUpload_flag(UFBoolean.FALSE);
					payDetailVO.setStatus(VOStatus.UPDATED);
					VOs.add(payDetailVO);
				}else if(attachmentVOs[0].getBill_type().equals(BillTypeConst.RCS)){
					ReceCheckSheetVO receCheckSheetVO = dao.queryByCondition(ReceCheckSheetVO.class, "pk_rece_check_sheet=?", pk_billno);
					receCheckSheetVO.setUpload_flag(UFBoolean.FALSE);
					receCheckSheetVO.setStatus(VOStatus.UPDATED);
					VOs.add(receCheckSheetVO);
				}else if(attachmentVOs[0].getBill_type().equals(BillTypeConst.PCS)){
					PayCheckSheetVO payCheckSheetVO = dao.queryByCondition(PayCheckSheetVO.class, "pk_pay_check_sheet=?", pk_billno);
					payCheckSheetVO.setUpload_flag(UFBoolean.FALSE);
					payCheckSheetVO.setStatus(VOStatus.UPDATED);
					VOs.add(payCheckSheetVO);
				}else if(attachmentVOs[0].getBill_type().equals("SMS")){
					SmsVO smsVO = dao.queryByCondition(SmsVO.class, "pk_sms=?", pk_billno);
					smsVO.setUpload_flag(UFBoolean.FALSE);
					smsVO.setStatus(VOStatus.UPDATED);
					VOs.add(smsVO);
				}
			}
		}
		for(AttachmentVO attachmentVO : attachmentVOs){
			attachmentVO.setStatus(VOStatus.DELETED);
			VOs.add(attachmentVO);
		}
		dao.saveOrUpdate(VOs);
		List<SuperVO> updateVOs = new ArrayList<SuperVO>();
		if(attachmentVOs[0].getBill_type().equals(BillTypeConst.TRACKING)){
			for(String pk_entrust : pk_entrusts){
				AttachmentVO[] entAttachmentVOs = dao.queryForSuperVOArrayByCondition(AttachmentVO.class, "pk_billno=?", pk_entrust);
				if(entAttachmentVOs == null || entAttachmentVOs.length == 0){
					EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "pk_entrust=?", pk_entrust);
					entrustVO.setUpload_flag(UFBoolean.FALSE);
					entrustVO.setStatus(VOStatus.UPDATED);
					updateVOs.add(entrustVO);
				}
			}
		}
		if(updateVOs.size() > 0){
			dao.saveOrUpdate(updateVOs);
		}
		
	}
	public String uploadAtta(MultipartFile file,String pkCorp,String userId,String billType,String billId) throws Exception{
		String fileName = file.getOriginalFilename();
		String extension = "";
		String fileUrl = "";
		String uploadDir = Global.uploadDir; 
		String attaPathKind = BillTypeConst.YCSG.equals(billType)?BillTypeConst.EXP:billType;
		File dir = new File(uploadDir + File.separator + attaPathKind + 
		File.separator + DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_HORIZONTAL));
		if(!dir.exists()) {
			dir.mkdirs(); // 若目录不存在，则创建目录
		}
		if (fileName.indexOf(".") != -1) {
			extension = fileName.substring(fileName.lastIndexOf("."));
		}
		fileName = DateUtils.formatDate(new Date(),
				DateUtils.DATE_TIME_FORMAT_ALL) + extension;
		fileUrl = dir + File.separator + fileName;
		InputStream is = file.getInputStream();
		//写入磁盘
		writeTmpFile(new File(fileUrl),is);
		//文件上传文件夹完毕，下面导入数据库中
		List<SuperVO> updateVOs = new ArrayList<SuperVO>();
		AttachmentVO attachmentVO = new AttachmentVO();
		attachmentVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(attachmentVO);
		attachmentVO.setBill_type(BillTypeConst.EXP);
		attachmentVO.setPk_billno(billId);
		attachmentVO.setPk_corp(pkCorp);
		attachmentVO.setAttachment_name(fileName);
		attachmentVO.setFile_url(fileUrl);
		attachmentVO.setFile_name(fileName);
		attachmentVO.setCreate_user(userId);
		attachmentVO.setCreate_time(new UFDateTime(new Date()));
		updateVOs.add(attachmentVO);
		if(updateVOs.size() > 0){
			dao.saveOrUpdate(updateVOs);
		}
		return attachmentVO.getPk_attachment();
	}
	
	
	public String uploadSmsAttachment(MultipartFile file) throws Exception{
		String fileName = file.getOriginalFilename();
		String extension = "";
		String fileUrl = "";
		String uploadDir = Global.uploadDir; 
		String attaPathKind = "SMS";
		File dir = new File(uploadDir + File.separator + attaPathKind + 
		File.separator + DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_HORIZONTAL));
		if(!dir.exists()) {
			dir.mkdirs(); // 若目录不存在，则创建目录
		}
		if (fileName.indexOf(".") != -1) {
			extension = fileName.substring(fileName.lastIndexOf("."));
		}
		fileName = DateUtils.formatDate(new Date(),
				DateUtils.DATE_TIME_FORMAT_ALL) + extension;
		fileUrl = dir + File.separator + fileName;
		InputStream is = file.getInputStream();
		//写入磁盘
		writeTmpFile(new File(fileUrl),is);
		//文件上传文件夹完毕，下面导入数据库中
		List<SuperVO> updateVOs = new ArrayList<SuperVO>();
		AttachmentVO attachmentVO = new AttachmentVO();
		attachmentVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(attachmentVO);
		attachmentVO.setBill_type("SMS");
		attachmentVO.setPk_billno("");
		attachmentVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		attachmentVO.setAttachment_name(fileName);
		attachmentVO.setFile_url(fileUrl);
		attachmentVO.setFile_name(fileName);
		attachmentVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		attachmentVO.setCreate_time(new UFDateTime(new Date()));
		updateVOs.add(attachmentVO);
		if(updateVOs.size() > 0){
			dao.saveOrUpdate(updateVOs);
		}
		return attachmentVO.getPk_attachment();
	}
	
	
	
}
