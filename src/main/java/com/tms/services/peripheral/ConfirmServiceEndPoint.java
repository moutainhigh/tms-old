package com.tms.services.peripheral;

import java.util.Map;
import org.nw.vo.ParamVO;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.te.EntrustService;


/**
 * @author zhuyj
 * @for 到货服务
 */
@SuppressWarnings("deprecation")
public class ConfirmServiceEndPoint extends ServletEndpointSupport {
	@Autowired
	private EntrustService entrustService;
	
	
	@ResponseBody
	public Map<String,Object> confirm(String userCode, String pks) {
		//初始化用户信息
		ServletContextHolder.setRequest(WebServicesUtils.getHttpServletRequest());
		WebServicesUtils.initLoginEnvironment(userCode);
		
		boolean success = false;
		String msg = "";

		setEntrustService();
		this.entrustService = getEntrustService();
		String[] pk_entrusts = pks.split(",");
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode("t501");
		paramVO.setBillType(BillTypeConst.WTD);
		paramVO.setTemplateID("0001A410000000000JEW");
		paramVO.setTabCode(TabcodeConst.TS_ENTRUST);
		paramVO.setHeaderTabCode(TabcodeConst.TS_ENTRUST);
		paramVO.setBodyTabCode("ts_ent_pack_b,ts_ent_line_b,ts_ent_transbility_b,ts_pay_detail_b,ts_ent_operation_b");
		for(String pk_entrust : pk_entrusts){
			paramVO.setBillId(pk_entrust);
			try {
				this.entrustService.confirm(paramVO);
			} catch (Exception e) {
				success = false;
				return WebServicesUtils.genAjaxResponse(success, e.getMessage(), null);
			}
		}
		success = true;
		msg = "接单成功！";
		return WebServicesUtils.genAjaxResponse(success, msg, null);
	}

	
	public EntrustService getEntrustService() {
		return entrustService;
	}

	public void setEntrustService() {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();  
		this.entrustService = (EntrustService) wac.getBean("entrustService");
	}

	
}
