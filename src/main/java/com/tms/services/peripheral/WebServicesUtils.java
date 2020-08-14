package com.tms.services.peripheral;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.codec.Decoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.basic.util.DateUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.CorpVO;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.WebUtils;
import org.nw.web.vo.LoginInfo;

import com.tms.vo.inv.InvPackBVO;


public class WebServicesUtils {
	private static final Log log = LogFactory.getLog(WebServicesUtils.class);
	/**
	 * 获取当前用户登录环境,包括用户信息，公司信息，用户所在部门信息
	 * 
	 * @param principal
	 */
	public static void initLoginEnvironment(String userCode) {
		try {
			LoginInfo loginInfo = new LoginInfo();
			loginInfo.setLoginDate(DateUtils.getCurrentDate());
			NWDao dao = NWDao.getInstance();
			
			UserVO userVO = dao.queryByCondition(UserVO.class, "user_code=?", userCode);
			loginInfo.setUser_code(userVO.getUser_code());
			loginInfo.setUser_name(userVO.getUser_name());
			loginInfo.setPassword(userVO.getUser_password());
			loginInfo.setPk_user(userVO.getPk_user());
			loginInfo.setUser_type(userVO.getUser_type());
			loginInfo.setPk_customer(userVO.getPk_customer());
			loginInfo.setPk_carrier(userVO.getPk_carrier());
			
			// 公司信息
			CorpVO corpVO = dao.queryByCondition(CorpVO.class, "pk_corp=?", userVO.getPk_corp());
			if(corpVO == null) {
				if(userVO.getUser_type().intValue() != Constants.USER_TYPE.ADMIN.intValue()) {
					// 如果不是管理员
					log.warn("用户" + userCode + "所在的公司不存在，或已经被删除！");
				}
			} else {
				loginInfo.setPk_corp(corpVO.getPk_corp());
				loginInfo.setCorp_code(corpVO.getCorp_code());
				loginInfo.setCorp_name(corpVO.getCorp_name());
			}

			// 部门信息
			DeptVO deptVO = dao.queryByCondition(DeptVO.class, "pk_dept=?", userVO.getPk_dept());
			if(deptVO == null) {
				if(userVO.getUser_type().intValue() != Constants.USER_TYPE.ADMIN.intValue()) {
					// 如果不是管理员
					log.warn("用户" + userCode + "所在的部门不存在，或已经被删除！");
				}
			} else {
				loginInfo.setPk_dept(deptVO.getPk_dept());
				loginInfo.setDept_Code(deptVO.getDept_code());
				loginInfo.setDept_name(deptVO.getDept_name());
			}

			WebUtils.checkLicense();
			/**
			 * 这里必须先放入线程局部变量，否则下面将会用到loginInfo里的信息
			 */
			WebUtils.setLoginInfo(loginInfo);
		} catch(Exception e) {
			log.error("设置登录环境时出错！" + e);
			WebUtils.clearLoginInfo();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 页面操作后的返回信息，该信息会被页面捕获，用于操作提示
	 * 
	 * @param result
	 *            操作成功与否
	 * @param msg
	 *            返回操作后的信息，默认操作成功，一般操作失败会抛出异常信息
	 * @param obj
	 *            操作返回的对象，一般是ajax操作后返回的数据对象
	 * @param append
	 *            此字段特定场合用作业务的查询条件 页面可能需要其他的提示信息，如批量操作中,有些记录没有执行成功，此时需要提示用户
	 * @return
	 */
	public static Map<String, Object> genAjaxResponse(boolean result, String msg, Object obj, Object append) {
		Map<String, Object> viewMap = new HashMap<String, Object>();
		viewMap.put("success", result);
		viewMap.put("msg", msg == null ? Constants.FRAME_OPERATE_SUCCESS : msg);
		if(obj instanceof List || obj instanceof Object[]) { // List或者数组
			viewMap.put("datas", obj);
		} else if(obj instanceof PaginationVO) {
			PaginationVO paginationVO = (PaginationVO) obj;
			if(paginationVO.getSummaryRowMap() != null) {
				viewMap.put("summaryRow", paginationVO.getSummaryRowMap());
			}
			viewMap.put("totalRecords", paginationVO.getTotalCount());
			viewMap.put("records", paginationVO.getItems());
		} else if(obj instanceof ReportVO) {// 报表VO
			ReportVO reportVO = (ReportVO) obj;
			PaginationVO pageVO = reportVO.getPageVO();
			viewMap.put("totalRecords", pageVO.getTotalCount());
			viewMap.put("records", pageVO.getItems());
			if(pageVO.getSummaryRowMap() != null) {
				viewMap.put("summaryRow", pageVO.getSummaryRowMap());
			}
			// 对于动态报表，可能需要返回列头信息，页面可以重新刷新列头
			if(reportVO.getHeaderVO() != null) {
				viewMap.put("headerVO", reportVO.getHeaderVO());
			}
		} else {
			viewMap.put("data", obj);
		}
		if(append != null) {
			viewMap.put("append", append);
		}
		return viewMap;
	}

	/**
	 * 页面操作后的返回信息，该信息会被页面捕获，用于操作提示
	 * 
	 * @param result
	 *            操作成功与否
	 * @param msg
	 *            返回操作后的信息，默认操作成功，一般操作失败会抛出异常信息
	 * @param obj
	 *            操作返回的对象，一般是ajax操作后返回的数据对象
	 * @return
	 */
	public static Map<String, Object> genAjaxResponse(boolean result, String msg, Object obj) {
		return genAjaxResponse(result, msg, obj, null);
	}
	
	/**
	 * 获取webservice中的httprequest对象
	 * @author:zhuyj
	 * @for:
	 * @return
	 */
	public static HttpServletRequest getHttpServletRequest(){
	MessageContext mc = MessageContext.getCurrentContext();
	  if (mc == null){
	          System.out.println("无法获取到MessageContext");
	          return null;
	  }
	  HttpServletRequest request = (HttpServletRequest) mc
	          .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
	  return request;
	}
	
	/**
	 * 检查表体的金额是否计算正确
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	public static void checkBeforeExpPod(AggregatedValueObject billVO, ParamVO paramVO) {
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
					throw new BusiException("第[?]行签收件数+拒收件数+破损件数+丢失件数必须等于件数！",(i + 1+""));
				}
			}
		}
	}
	
	/**
	 * 把base64图片数据转为本地图片
	 * @param base64ImgData
	 * @param filePath
	 * @throws IOException
	 * @throws DecoderException 
	 */
	public static boolean convertBase64DataToImage(String base64ImgData,String filePath) throws IOException, DecoderException {
		boolean result = true;
		Decoder decoder = new Base64();
		byte[] b = (byte[]) decoder.decode(base64ImgData.getBytes()); 
		for(int i=0;i<b.length;++i)  
        {  
            if(b[i]<0)  
            {//调整异常数据  
                b[i]+=256;  
            }  
        } 
		//生成jpeg图片  
        OutputStream out = new FileOutputStream(filePath);      
        out.write(b);  
        out.flush();  
        out.close(); 
        return result;
	}
}
