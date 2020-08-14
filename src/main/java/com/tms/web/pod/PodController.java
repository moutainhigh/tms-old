package com.tms.web.pod;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.service.pod.PodService;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.pod.PodVO;

/**
 * 签收回单
 * 
 * @author xuqc
 * @date 2012-8-8 下午10:42:50
 */
//zhuyj 对外接口依据beanName获取对象实例
@Controller("podController")
@RequestMapping(value = "/pod/pod")
public class PodController extends AbsBillController {

	@Autowired
	private PodService podService;

	public PodService getService() {
		return podService;
	}

	/**
	 * 签收
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/pod.json")
	@ResponseBody
	public Map<String, Object> pod(PodVO podVO, HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_invoice = request.getParameterValues("pk_invoice");
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("请先选择要签收的记录！");
		}
		List<Map<String, Object>> retList = this.getService().doPod(pk_invoice, podVO, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 异常签收
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/expPod.json")
	@ResponseBody
	public Map<String, Object> expPod(PodVO podVO, HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_invoice = request.getParameter("pk_invoice");
		if(StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("请先选择要签收的记录！");
		}
		String json = request.getParameter(Constants.APP_POST_DATA);
		AggregatedValueObject billVO = convertJsonToAggVO(json);
		checkBeforeExpPod(billVO, paramVO);
		Map<String, Object> retMap = this.getService().doExpPod(pk_invoice, billVO, paramVO);
		return this.genAjaxResponse(true, null, retMap);
	}

	/**
	 * 检查表体的金额是否计算正确
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	private void checkBeforeExpPod(AggregatedValueObject billVO, ParamVO paramVO) {
		if(billVO.getChildrenVO() != null && billVO.getChildrenVO().length > 0) {
			InvPackBVO[] childVOs = (InvPackBVO[]) billVO.getChildrenVO();
			for(int i = 0; i < childVOs.length; i++) {
				InvPackBVO childVO = childVOs[i];
				int num = childVO.getNum() == null ? 0 : childVO.getNum();
				int pod_num = childVO.getPod_num() == null ? 0 : childVO.getPod_num();
				int damage_num = childVO.getDamage_num() == null ? 0 : childVO.getDamage_num();
				int reject_num = childVO.getReject_num() == null ? 0 : childVO.getReject_num();
				int lost_num = childVO.getLost_num() == null ? 0 : childVO.getLost_num();
				if(num != (pod_num + damage_num + reject_num + lost_num)) {
					throw new BusiException("第[?]行签收件数+拒收件数+破损件数+丢失件数必须等于件数！",(i + 1)+"");
				}
			}
		}
	}

	/**
	 * 撤销签收
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unpod.json")
	@ResponseBody
	public Map<String, Object> unpod(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_invoice = request.getParameterValues("pk_invoice");
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("请先选择要撤销签收的记录！");
		}
		List<Map<String, Object>> retList = this.getService().doUnpod(pk_invoice, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 回单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/receipt.json")
	@ResponseBody
	public Map<String, Object> receipt(PodVO podVO, HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_invoice = request.getParameterValues("pk_invoice");
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("请先选择要回单的记录！");
		}
		List<Map<String, Object>> retList = this.getService().doReceipt(pk_invoice, podVO, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}

	/**
	 * 异常回单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/expReceipt.json")
	@ResponseBody
	public Map<String, Object> expReceipt(PodVO podVO, HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String pk_invoice = request.getParameter("pk_invoice");
		if(StringUtils.isBlank(pk_invoice)) {
			throw new BusiException("请先选择要回单的记录！");
		}
		Map<String, Object> retMap = this.getService().doExpReceipt(pk_invoice, podVO, paramVO);
		return this.genAjaxResponse(true, null, retMap);
	}

	/**
	 * 撤销回单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/unreceipt.json")
	@ResponseBody
	public Map<String, Object> unreceipt(HttpServletRequest request, HttpServletResponse response) {
		ParamVO paramVO = this.getParamVO(request);
		String[] pk_invoice = request.getParameterValues("pk_invoice");
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("请先选择要撤销回单的记录！");
		}
		List<Map<String, Object>> retList = this.getService().doUnreceipt(pk_invoice, paramVO);
		return this.genAjaxResponse(true, null, retList);
	}
	
	@RequestMapping(value = "/fileupload.do")
	@ResponseBody
	public String fileupload(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(HTML_CONTENT_TYPE);
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> fileAry = mRequest.getFiles("file");
		for(MultipartFile file : fileAry) {
			String originalFilename = file.getOriginalFilename();
			if(StringUtils.isBlank(originalFilename)) {
				continue;
			}
			//根据文件名找到对应的POD单据
			String[] temps = originalFilename.split("-");
			String vbillnoOrCustOrderno = "";
			if(temps.length == 1){
				vbillnoOrCustOrderno = temps[0].substring(0, temps[0].lastIndexOf("."));
			}else{
				vbillnoOrCustOrderno = temps[0];
			}
			
			String msg = this.getService().fileupload(file, originalFilename, vbillnoOrCustOrderno);
			if(StringUtils.isNotBlank(msg) && msg.equals("success")){
				return "success";
			}
		}
		throw new BusiException("error");
		//return "error";
	}
}
