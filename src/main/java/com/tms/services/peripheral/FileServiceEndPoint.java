package com.tms.services.peripheral;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import com.tms.constants.BillTypeConst;
import com.tms.vo.attach.AttachmentVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;

import net.sf.json.JSONNull;

/**
 * @author zhuyj
 * @for 货物跟踪服务接口
 */
@SuppressWarnings("deprecation")
public class FileServiceEndPoint extends ServletEndpointSupport {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> uploadAttachment(String userCode, Map params) {
		boolean success = false;
		String msg = "";
		// 初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		String fileJson = params.get("fileJson").toString();
		if(org.nw.basic.util.StringUtils.isBlank(fileJson)){
			msg = "文件不能为空!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		List<Map<String, String>> fileList = JacksonUtils.readValue(fileJson, ArrayList.class);
		String extension = ""; // 文件后缀名
		String fileUrl = "";// 文件路径，但实际存的是路径加文件名
		// VOs 包含需要新建的附件VO和需要修改flag的模块对应的VO 如PodVO InvoiceVO等。
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		String billType = params.get("billType").toString();
		String funCode = params.get("funCode").toString();
		String lot = params.get("lot") == null ? null : params.get("lot").toString();
		String pk_address = params.get("pk_address") == null ? null : params.get("pk_address").toString();
		String[] pks = params.get("pks").toString().split(",");
		if (pks == null || pks.length == 0) {
			msg = "参数单据号不能为空！";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		ParamVO paramVO = new ParamVO();
		paramVO.setBillType(billType);
		paramVO.setFunCode(funCode);
		for (String pk : pks) {
			paramVO.setBillId(pk);
			String cond = NWUtils.buildConditionString(pks);
			if (StringUtils.isNotBlank(billType)) {
				// 对签单和回单的判断
				if (billType.equals(BillTypeConst.POD_SIGN) || (billType.equals(BillTypeConst.POD_RECEIPT))) {
					PodVO podVO = NWDao.getInstance().queryByCondition(PodVO.class, "pk_invoice=?",paramVO.getBillId());
					if (billType.equals(BillTypeConst.POD_SIGN)) {
						podVO.setSign_flag(UFBoolean.TRUE);
					} else {
						podVO.setReceipt_flag(UFBoolean.TRUE);
					}
					podVO.setStatus(VOStatus.UPDATED);
					VOs.add(podVO);
				}
				// yaojiie 2015 12 08 异常跟踪附件的判断
				if (billType.equals(BillTypeConst.TRACKING)) {
					EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class,"pk_entrust in " + cond);
					for (EntrustVO entrustVO : entrustVOs) {
						entrustVO.setStatus(VOStatus.UPDATED);
						entrustVO.setUpload_flag(UFBoolean.TRUE);
						VOs.add(entrustVO);
					}
				}
				if (billType.equals(BillTypeConst.EXP)) {
					ExpAccidentVO[] expAccidentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ExpAccidentVO.class, " pk_exp_accident in " + cond);
					for (ExpAccidentVO expAccidentVO : expAccidentVOs) {
						expAccidentVO.setStatus(VOStatus.UPDATED);
						expAccidentVO.setUpload_flag(UFBoolean.TRUE);
						VOs.add(expAccidentVO);
					}
				}
			}
		}
		String pk_corp = WebUtils.getLoginInfo().getPk_corp();
		String create_user = WebUtils.getLoginInfo().getPk_user();
		String uploadDir = Global.uploadDir;
		File dir = new File(uploadDir + File.separator + billType + File.separator
				+ DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_HORIZONTAL));
		if (!dir.exists()) {
			dir.mkdirs(); // 若目录不存在，则创建目录
		}
		if (fileList == null || fileList.size() == 0) {
			msg = "文件不能为空!";
			return WebServicesUtils.genAjaxResponse(success, msg, null);
		}
		int index = 1;
		for (Map<String, String> fileMap : fileList) {
			String originalFilename = fileMap.get("fileName") == null ? "" : fileMap.get("fileName").toString();
			String fileData = fileMap.get("fileData");
			if (StringUtils.isBlank(originalFilename)) {
				msg = "文件名不能为空!";
				return WebServicesUtils.genAjaxResponse(success, msg, null);
			}
			if (originalFilename.indexOf(".") != -1) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}
			String time_name = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
			String fileName = time_name + extension;
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
				success = WebServicesUtils.convertBase64DataToImage(fileData, fileUrl);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DecoderException e) {
				e.printStackTrace();
			}
			if (!success) {
				msg = "生成文件失败！";
				return WebServicesUtils.genAjaxResponse(success, msg, null);
			}
			// 文件上传文件夹完毕，下面导入数据库中
			for (String pk : pks) {
				paramVO.setBillId(pk);
				AttachmentVO attachmentVO = new AttachmentVO();
				attachmentVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(attachmentVO);
				attachmentVO.setBill_type(paramVO.getBillType());
				attachmentVO.setPk_billno(paramVO.getBillId());
				attachmentVO.setPk_corp(pk_corp);
				attachmentVO.setAttachment_name(fileName);
				attachmentVO.setFile_url(fileUrl);
				attachmentVO.setFile_name(originalFilename);
				attachmentVO.setDef1(lot);
				attachmentVO.setDef2(pk_address);
				attachmentVO.setCreate_user(create_user);
				attachmentVO.setCreate_time(new UFDateTime(new Date()));
				VOs.add(attachmentVO);
			}

		}
		NWDao.getInstance().saveOrUpdate(VOs);

		msg = "附件上传成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}


}
