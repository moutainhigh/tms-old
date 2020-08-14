package com.tms.web.rf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.rf.ScanService;
import com.tms.service.te.TrackingService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.rf.EntLineBarBVO;
import com.tms.vo.rf.ScanVO;
import com.tms.vo.te.EntLineBVO;

@Controller
@RequestMapping("/rf/sc")
public class ScanController extends AbsBillController{
	
	
	@Autowired
	private TrackingService trackingService;
	
	@Autowired 
	private ScanService scanService;
	
	public ScanService getService(){
		return scanService;
	}
	
	@RequestMapping(value = "/getScan.json")
	@ResponseBody
	//适用于提货扫描和到货扫描
	public void getScan(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String path =request.getContextPath();
		response.setContentType("text/html;charset=UTF-8"); 
		String lot = request.getParameter("lot");
		String page = request.getParameter("page");
		String nowPage = request.getParameter("nowPage");
		Integer operate_type = Integer.parseInt( request.getParameter("operate_type"));
		//将lot里面的字符转化成大写，并且去掉前后空格
		if(StringUtils.isBlank(lot)){
			response.getWriter().write("<script>alert('批次号不能为空！');</script>");
			response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + nowPage+"'</script>");
		}
		String upperLot = lot.toUpperCase().trim();
		List<ScanVO> results = this.getService().scan(upperLot, operate_type);
		if(results != null && results.size() > 0){
			HttpSession session = request.getSession();
			session.setAttribute("lot",  results.get(0).getLot());
			session.setAttribute("carr_name", results.get(0).getCarr_name());
			session.setAttribute("carno", results.get(0).getCarno());
			
			List<Map<String,String>> resultsList = new ArrayList<Map<String,String>>();
			for(ScanVO deliScanVO : results){
				Map<String,String> result = new HashMap<String, String>();
				result.put("lot", deliScanVO.getLot());
				
				String addr_name = deliScanVO.getAddr_name();
				if(StringUtils.isNotBlank(addr_name) && addr_name.length() > 6){
					addr_name = addr_name.substring(0, 6) + "...";
					result.put("addr_name", addr_name);
				}else{
					result.put("addr_name", deliScanVO.getAddr_name());
				}
				
				result.put("num", deliScanVO.getNum() == null ? "0" : deliScanVO.getNum().toString());
				if(deliScanVO.getArrival_flag() == null || !deliScanVO.getArrival_flag().booleanValue()){
					result.put("arrival_flag", "开放");
				}else{
					result.put("arrival_flag", "完成");
				}
				result.put("pk_ent_line_b", deliScanVO.getPk_ent_line_b());
				resultsList.add(result);
			}
			
			//如果这个批次号下有正确的信息，那么我们还要将这个公司下，所有的发货单为 新建 确认 已提货的所有发货单的条码进行汇总，只允许扫描这些条码
			session.setAttribute("resultsList", resultsList);
			response.sendRedirect(""+request.getContextPath()+"/busi/rf/" + page);
		}else{
			response.getWriter().write("<script>alert('扫描失败，批次号错误！');</script>");
			response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + nowPage+"'</script>");
		}
	}
	
	@RequestMapping(value = "nodeScan.json")
	@ResponseBody
	public void nodeScan(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession();
		session.setAttribute("lot",request.getParameter("lot"));
		session.setAttribute("addr_name",new String(request.getParameter("addr_name").getBytes("ISO-8859-1"),"UTF-8"));
		session.setAttribute("allNum", request.getParameter("num"));
		session.setAttribute("pk_ent_line_b", request.getParameter("pk_ent_line_b"));
		
		//这里需要查询这个批次下这个节点所有的barcode并储藏在session里
		//总条码数量
		EntLineBarBVO[] barBVOs = this.getService().getBarCodeVOS(request.getParameter("lot"),request.getParameter("pk_ent_line_b"));
		session.setAttribute("barBVOs", barBVOs);
		
		//扫描条码数量
		List<String> scanBarcodes = new ArrayList<String>();
		session.setAttribute("scanBarcodes", scanBarcodes);
		
		//异常条码数量
		List<String> abnormalBarCodes = new ArrayList<String>();
		session.setAttribute("abnormalBarCodes", abnormalBarCodes);
		
		response.sendRedirect(request.getContextPath()+"/busi/rf/"+request.getParameter("page"));
	}
	
	@RequestMapping(value = "validation.json")
	@ResponseBody
	public String validation(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession();
		String barcode = request.getQueryString().trim();
		if(barcode.endsWith("%20")){
			barcode = barcode.substring(0, barcode.length()-3);
		}
		 @SuppressWarnings("unchecked")
		 List<String> scanBarcodes = (List<String>) session.getAttribute("scanBarcodes");
		 for(String scanBarcode : scanBarcodes){
			 if(scanBarcode.substring(5).equals(barcode)){
				 //这个条码已经被扫描了
				 return "repeat";
			 }
		 }
		 EntLineBarBVO[] barBVOs = (EntLineBarBVO[]) session.getAttribute("barBVOs");
		 boolean flag = true;
		 for(EntLineBarBVO barBVO : barBVOs){
			 if(barcode.equals(barBVO.getBar_code())){
				 flag = false;
				 //已存在
				 return "normal";
			 }
		 }
		 if(flag){
			 //循环结束都没有找到这个值，这事应该在页面弹出确认框。
			return "abnormal";
		 }
		 return "normal";
	}
	
	@RequestMapping(value = "checkAbnormalBarCodes.json")
	@ResponseBody
	public String checkAbnormalBarCodes(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession();
		String barcode = request.getQueryString().trim().split("&")[0];
		if(barcode.endsWith("%20")){
			barcode = barcode.substring(0, barcode.length()-3);
		}
		@SuppressWarnings("unchecked")
		List<String> abnormalBarCodes = (List<String>) session.getAttribute("abnormalBarCodes");
		for(String abnormalBarCode : abnormalBarCodes){
			if(barcode.equals(abnormalBarCode)){
				//存在
				return "YES";
			};
		}
		return "NO";
	}
	
	@RequestMapping(value = "addToSession.json")
	@ResponseBody
	public void addToSession(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession();
		String barcode = request.getQueryString().trim().split("&")[0];
		int index = barcode.indexOf(",");
		if(index == -1){
			//判断字符串是否是空格结尾
			if(barcode.endsWith("%20")){
				barcode = barcode.substring(0, barcode.length()-3);
			}
			//这个code是存在的
			@SuppressWarnings("unchecked")
			List<String> scanBarcodes = (List<String>) session.getAttribute("scanBarcodes");
			scanBarcodes.add(barcode);
			session.setAttribute("scanBarcodes", scanBarcodes);
			
		}else{
			//异常扫描
			barcode = barcode.substring(0, index);
			if(barcode.endsWith("%20")){
				barcode = barcode.substring(0, barcode.length()-3);
			}
			
			@SuppressWarnings("unchecked")
			List<String> scanBarcodes = (List<String>) session.getAttribute("scanBarcodes");
			scanBarcodes.add(barcode);
			session.setAttribute("scanBarcodes", scanBarcodes);
			
			
			@SuppressWarnings("unchecked")
			List<String> abnormalBarCodes = (List<String>) session.getAttribute("abnormalBarCodes");
			abnormalBarCodes.add(barcode);
			session.setAttribute("abnormalBarCodes", abnormalBarCodes);
			
		}
	}
	
	@RequestMapping(value = "remiveToSession.json")
	@ResponseBody
	public void remiveToSession(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession();
		String barcode = request.getQueryString().trim().split("&")[0];
		if(barcode.endsWith("%20")){
			barcode = barcode.substring(0, barcode.length()-3);
		}
		@SuppressWarnings("unchecked")
		List<String> scanBarcodes = (List<String>) session.getAttribute("scanBarcodes");
		scanBarcodes.remove(barcode);
		session.setAttribute("scanBarcodes", scanBarcodes);
		
		@SuppressWarnings("unchecked")
		List<String> abnormalBarCodes = (List<String>) session.getAttribute("abnormalBarCodes");
		abnormalBarCodes.remove(barcode);
		session.setAttribute("abnormalBarCodes", abnormalBarCodes);
	}
	
	
	@RequestMapping(value = "deliScanConfirm.json")
	@ResponseBody
	public void deliScanConfirm(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//第一步，找到这个节点下所有的条码信息，这些信息需要删除掉
		response.setContentType("text/html;charset=UTF-8"); 
		HttpSession session = request.getSession();
		@SuppressWarnings("unchecked")
		List<String> scanBarcodes = (List<String>) session.getAttribute("scanBarcodes");
		String pk_ent_line_b = (String) session.getAttribute("pk_ent_line_b");
		//将pk_ent_line_bs按逗号分隔开，形成数组。
		String[] pkArrs = pk_ent_line_b.split("\\" + Constants.SPLIT_CHAR);
		if(scanBarcodes != null && scanBarcodes.size() > 0){
			EntLineBarBVO[] barBVOs = (EntLineBarBVO[]) session.getAttribute("barBVOs");
			String lot = (String) session.getAttribute("lot");
			EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_ent_line_b in "
					+ NWUtils.buildConditionString(pkArrs));
			//提货时间
			for(EntLineBVO entLineBVO : entLineBVOs){
				entLineBVO.setAct_arri_date((new UFDateTime(new Date())).toString());
				//提货确认的节点到货方法
				Object[] msgs =  trackingService.confirmArrival(entLineBVO,0);
				if(msgs!= null && msgs.length > 0 && msgs[0] instanceof String){
					response.getWriter().write("<script>alert('"+ msgs[0].toString() +"');</script>");
					response.getWriter().write("<script>location.href='"+request.getContextPath()+"/rfIndex.html'</script>");
				}
			}
			List<EntLineBarBVO> entLineBarBVOs = this.getService().saveBarCodes(barBVOs, scanBarcodes,pkArrs[0],lot);
			//将当前节点的条码信息，更新到下一个节点
			//1,将当前点的条码按pk_ent_line_b分组
			Map<String, List<EntLineBarBVO>> groupMap = new HashMap<String, List<EntLineBarBVO>>();
			for (EntLineBarBVO entLineBarBVO : entLineBarBVOs) {
				String key = entLineBarBVO.getPk_ent_line_b();
				List<EntLineBarBVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<EntLineBarBVO>();
					groupMap.put(key, voList);
				}
				voList.add(entLineBarBVO);
			}
			this.getService().updateNextEntLineBarBVO(groupMap, entLineBVOs);
		}
		@SuppressWarnings("unchecked")
		List<Map<String,String>> resultsList = (List<Map<String, String>>) session.getAttribute("resultsList");
		//将已做完的刚才做掉的那个地址的状态改为完成
		for(Map<String,String> map : resultsList){
			if(pk_ent_line_b.equals(map.get("pk_ent_line_b"))){
				map.put("arrival_flag", "完成");
				break;
			}
		}
		response.setContentType("text/html;charset=UTF-8"); 
		response.getWriter().write("<script>alert('扫描成功');</script>");
		response.getWriter().write("<script>location.href='"+request.getContextPath()+"/rfIndex.html'</script>");
		
	}
	
	
	@RequestMapping(value = "arriScanConfirm.json")
	@ResponseBody
	public void arriScanConfirm(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html;charset=UTF-8"); 
		//第一步，找到这个节点下所有的条码信息，这些信息需要删除掉
		HttpSession session = request.getSession();
		@SuppressWarnings("unchecked")
		List<String> scanBarcodes = (List<String>) session.getAttribute("scanBarcodes");
		String pk_ent_line_b = (String) session.getAttribute("pk_ent_line_b");
		//将pk_ent_line_bs按逗号分隔开，形成数组。
		String[] pkArrs = pk_ent_line_b.split("\\" + Constants.SPLIT_CHAR);
		if(scanBarcodes != null && scanBarcodes.size() > 0){
			EntLineBarBVO[] barBVOs = (EntLineBarBVO[]) session.getAttribute("barBVOs");
			String lot = (String) session.getAttribute("lot");
			EntLineBVO[] entLineBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBVO.class, "pk_ent_line_b in "
					+ NWUtils.buildConditionString(pkArrs));
			for(EntLineBVO entLineBVO : entLineBVOs){
				if(entLineBVO.getSerialno() == 10){
					response.getWriter().write("<script>alert('扫描失败，这个节点不是到货点，不允许到货操作！');</script>");
					response.getWriter().write("<script>location.href='"+request.getContextPath()+"/rfIndex.html'</script>");
					return;
				}
				//这里到货确认时，需要判断这个节点的上一个节点是否已经到货，否则不允许进行到货确认
				EntLineBVO prevEntLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class, "pk_entrust=? and serialno=?", entLineBVO.getPk_entrust(),entLineBVO.getSerialno()-10);
				if(prevEntLineBVO == null || !UFBoolean.TRUE.equals(prevEntLineBVO.getArrival_flag())){
					response.getWriter().write("<script>alert('扫描失败，上一节点未到货，不允许进行到货操作！');</script>");
					response.getWriter().write("<script>location.href='"+request.getContextPath()+"/rfIndex.html'</script>");
					return;
				}
				//到货时间
				entLineBVO.setAct_arri_date((new UFDateTime(new Date())).toString());
				//到货确认的节点到货方法 到货确认
				Object[] msgs =  trackingService.confirmArrival(entLineBVO,0);
				if(msgs!= null && msgs.length > 0 && msgs[0] instanceof String){
					response.getWriter().write("<script>alert('"+ msgs[0].toString() +"');</script>");
					response.getWriter().write("<script>location.href='"+request.getContextPath()+"/rfIndex.html'</script>");
				}
			}
			List<EntLineBarBVO> entLineBarBVOs = this.getService().saveBarCodes(barBVOs, scanBarcodes,pkArrs[0],lot);
			//将当前节点的条码信息，更新到下一个节点
			//1,将当前点的条码按pk_ent_line_b分组
			Map<String, List<EntLineBarBVO>> groupMap = new HashMap<String, List<EntLineBarBVO>>();
			for (EntLineBarBVO entLineBarBVO : entLineBarBVOs) {
				String key = entLineBarBVO.getPk_ent_line_b();
				List<EntLineBarBVO> voList = groupMap.get(key);
				if (voList == null) {
					voList = new ArrayList<EntLineBarBVO>();
					groupMap.put(key, voList);
				}
				voList.add(entLineBarBVO);
			}
			this.getService().updateNextEntLineBarBVO(groupMap, entLineBVOs);
			
			@SuppressWarnings("unchecked")
			List<Map<String,String>> resultsList = (List<Map<String, String>>) session.getAttribute("resultsList");
			//将已做完的刚才做掉的那个地址的状态改为完成
			for(Map<String,String> map : resultsList){
				if(pk_ent_line_b.equals(map.get("pk_ent_line_b"))){
					map.put("arrival_flag", "完成");
					break;
				}
			}
		}
		response.getWriter().write("<script>alert('扫描成功');</script>");
		response.getWriter().write("<script>location.href='"+request.getContextPath()+"/rfIndex.html'</script>");
		
	}
	
	
	@RequestMapping(value = "arriOrLeav.json")
	@ResponseBody
	public void arriOrLeav(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html;charset=UTF-8"); 
		//检查这个批次下，有没有属于当前地址的ＷＴＤ信息，如果有，插入跟踪信息。
		String lot = request.getParameter("lot");
		String type = request.getParameter("type");
		//将lot里面的字符转化成大写，并且去掉前后空格
		if(StringUtils.isBlank(lot)){
			response.getWriter().write("<script>alert('批次号不能为空！');</script>");
			response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + type + ".jsp'</script>");
			return;
		}
		String upperLot = lot.toUpperCase().trim();
		List<ScanVO> results = this.getService().scan(upperLot, null);
		if(results == null || results.size() == 0){
			
			response.getWriter().write("<script>alert('扫描失败，批次号错误！');</script>");
			response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + type + ".jsp'</script>");
			return;
		}
		//获取登录用户的地址信息
		AddressVO addrVO = NWDao.getInstance().queryByCondition(AddressVO.class, "pk_address =?", WebUtils.getLoginInfo().getPk_address());
		if(addrVO == null){
			response.getWriter().write("<script>alert('扫描失败，地址绑定错误！');</script>");
			response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + type + ".jsp'</script>");
			return;
		}
		boolean flag = false;
		for(ScanVO result : results){
			if(addrVO.getAddr_code().equals(result.getAddr_code())){
				Map<String, Object> retMap = this.getService().insertTracking(result,type,addrVO);
				if(retMap == null || retMap.get("msg") == null){
					flag = true;
				}
			}
		}
		if(flag){
			response.getWriter().write("<script>alert('扫描成功！');</script>");
			response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + type + ".jsp'</script>");
			return;
			
			
		}
		response.getWriter().write("<script>alert('扫描失败，该批次没有可操作的订单！');</script>");
		response.getWriter().write("<script>location.href='"+request.getContextPath()+"/busi/rf/" + type + ".jsp'</script>");
		return;
	}
	
	
}
