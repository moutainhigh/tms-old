package com.tms.web.httpEdi;

import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.ApiException;
import org.nw.service.IBillService;
import org.nw.utils.BillnoHelper;
import org.nw.utils.CodenoHelper;
import org.nw.utils.HttpUtils;
import org.nw.utils.ParameterHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.web.AbsBillController;
import org.nw.web.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.BillStatus;
import com.tms.constants.AddressConst;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TrackingConst;
import com.tms.constants.TransLineConst;
import com.tms.service.base.AreaService;
import com.tms.service.base.CustService;
import com.tms.service.base.TransTypeService;
import com.tms.service.cm.impl.CMUtils;
import com.tms.service.inv.InvoiceService;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.service.te.TrackingService;
import com.tms.utils.TransLineUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CustBalaVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.base.TransTypeVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.edi.EdiInvoiceVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntLinePackBVO;
import com.tms.vo.te.EntrustVO;

/**
 * RT接口
 * 
 * @author XIA
 * @date 2016 6 13
 */
@Controller
@RequestMapping(value = "/public/httpEdi/rt")
public class RTController extends AbsBillController {
	
	private Logger logger = LoggerFactory.getLogger("EDI");
	
	@Autowired
	private CustService custService;
	
	@Autowired
	private AreaService areaService;
	
	@Autowired
	private TransTypeService transTypeService;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private TrackingService trackingService;
	
	@Override
	public IBillService getService() {
		return null;
	}

	public static final String PK_CORP = "ad21c21cc30c4df8a341bbfb15525ee4";
	public static final String PK_USER = "32e6103e697f44b7ac98477583af49cd";
	public static final String PK_CUST_BALA_RS = "25c2da0b72de4ea7a1eece71f69f02c8";
	public static final String PK_CUST_BALA_SY = "25c2dde4ea7a1ea7a1eece71f69f02c8";

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/importFHD.do")
	@ResponseBody
	public String importFHD(HttpServletRequest request, HttpServletResponse response) {
		
		
		logger.info((new UFDateTime(new Date())).toString() + ":接口开始同步发货单信息!");
		
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		String xmlData = request.getParameter("xml");
		if(StringUtils.isBlank(xmlData)){
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，没有XML文本! ");
			logger.info("--------");
			return transToXML("没有XML文本！", false);
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		InvoiceVO invoiceVO = new InvoiceVO();
		String def1 = "";
		try {
			String xml =  new String(xmlData.getBytes("ISO-8859-1"),"UTF-8");
			//解析传递数据
			Document doc = DocumentHelper.parseText(xml);
			ExAggInvoiceVO billVO = new ExAggInvoiceVO();
			//优先判断DEF1.当不满足条件时，就不再执行了。
			def1 = doc.selectSingleNode("/hrow/def1") == null ? null : doc.selectSingleNode("/hrow/def1").getText();
			if(StringUtils.isBlank(def1)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：def1! ");
				logger.info("--------");
				return transToXML("关键字段为空：def1", false);
			}
			InvoiceVO invoiceVO_db = NWDao.getInstance().queryByCondition(InvoiceVO.class, "def1=?", def1);
			if(invoiceVO_db != null){
				logger.info((new UFDateTime(new Date())).toString() + ":同步成功，"+def1+"：但是这个单子已经存在！ ");
				logger.info("--------");
				return transToXML(def1 + "：同步成功，但是这个单子已经存在！", true);
			}
			invoiceVO.setDef1(def1);
			
			invoiceVO.setStatus(VOStatus.NEW);
			invoiceVO.setPk_corp(PK_CORP);
			invoiceVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.FHD));
			NWDao.setUuidPrimaryKey(invoiceVO);
			Element root = doc.getRootElement();
			List attrList = root.attributes();
			for (int i = 0; i < attrList.size(); i++) {
				 Attribute item = (Attribute)attrList.get(i);
				 if(item.getName().equals("djname")){
					 invoiceVO.setPk_invoice_type(item.getValue());
				 }else if(item.getName().equals("pk_sys_src")){
					 invoiceVO.setDef4(item.getValue());
				 }
			}
			
			String corp = doc.selectSingleNode("/hrow/corp") == null ? null : doc.selectSingleNode("/hrow/corp").getText();
			if(StringUtils.isBlank(corp)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：corp，def1:"+def1  );
				logger.info("--------");
				return transToXML("关键字段为空：corp", false);
			}
			invoiceVO.setDef3(corp);
			String orderno = doc.selectSingleNode("/hrow/orderno") == null ? null : doc.selectSingleNode("/hrow/orderno").getText();
			if(StringUtils.isBlank(orderno)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：orderno，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：orderno", false);
			}
			invoiceVO.setOrderno(orderno);
			String cust_code = doc.selectSingleNode("/hrow/cust_code") == null ? null : doc.selectSingleNode("/hrow/cust_code").getText();
			if(StringUtils.isBlank(cust_code)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：cust_code，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：cust_code", false);
			}
			String cust_name = doc.selectSingleNode("/hrow/cust_name") == null ? null : doc.selectSingleNode("/hrow/cust_name").getText();
			if(StringUtils.isBlank(cust_name)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：cust_name，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：cust_name", false);
			}
			String tax_identify = doc.selectSingleNode("/hrow/tax_identify") == null ? null : doc.selectSingleNode("/hrow/tax_identify").getText();
			if(StringUtils.isBlank(tax_identify)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：tax_identify，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：tax_identify", false);
			}
			String bank = doc.selectSingleNode("/hrow/bank") == null ? null : doc.selectSingleNode("/hrow/bank").getText();
			if(StringUtils.isBlank(bank)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：bank，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：bank", false);
			}
			String account_name = doc.selectSingleNode("/hrow/account_name") == null ? null : doc.selectSingleNode("/hrow/account_name").getText();
			if(StringUtils.isBlank(account_name)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：account_name，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：account_name", false);
			}
			String bank_account = doc.selectSingleNode("/hrow/bank_account") == null ? null : doc.selectSingleNode("/hrow/bank_account").getText();
			if(StringUtils.isBlank(bank_account)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：bank_account，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：bank_account", false);
			}
			String register_addr = doc.selectSingleNode("/hrow/register_addr") == null ? null : doc.selectSingleNode("/hrow/register_addr").getText();
			CustomerVO customerVO = custService.getByCode(cust_code);
			if(customerVO == null){
				customerVO = new CustomerVO();
				customerVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(customerVO);
				customerVO.setCust_code(cust_code);
				customerVO.setCust_name(cust_name);
				customerVO.setPk_corp(PK_CORP);
				customerVO.setTax_identify(tax_identify);
				customerVO.setBank(bank);
				customerVO.setBalatype(String.valueOf(DataDictConst.BALATYPE.MONTH.intValue()));
				customerVO.setAccount_name(account_name);
				customerVO.setBank_account(bank_account);
				customerVO.setRegister_addr(register_addr);
				customerVO.setCreate_time(new UFDateTime(new Date()));
				customerVO.setCreate_user(PK_USER);
				toBeUpdate.add(customerVO);
				CustBalaVO balaVO = new CustBalaVO();
				balaVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(balaVO);
				balaVO.setPk_customer(customerVO.getPk_customer());
				if(corp.equals("4")){
					balaVO.setPk_related_cust(PK_CUST_BALA_SY);
				}else if(corp.equals("1")){
					balaVO.setPk_related_cust(PK_CUST_BALA_RS);
				}
				toBeUpdate.add(balaVO);
			}
			if(corp.equals("4")){
				invoiceVO.setBala_customer(PK_CUST_BALA_SY);
			}else if(corp.equals("1")){
				invoiceVO.setBala_customer(PK_CUST_BALA_RS);
			}
			invoiceVO.setPk_customer(customerVO.getPk_customer());
			String req_deli_date = doc.selectSingleNode("/hrow/req_deli_date") == null ? null : doc.selectSingleNode("/hrow/req_deli_date").getText();
			if(StringUtils.isBlank(req_deli_date)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：req_deli_date，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：req_deli_date", false);
			}
			String req_arri_date = doc.selectSingleNode("/hrow/req_arri_date") == null ? null : doc.selectSingleNode("/hrow/req_arri_date").getText();
			if(StringUtils.isBlank(req_arri_date)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：req_arri_date，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：req_arri_date", false);
			}
			invoiceVO.setReq_deli_date((new UFDateTime(req_deli_date)).toString());
			invoiceVO.setReq_arri_date((new UFDateTime(req_arri_date)).toString());
			String deli_city = doc.selectSingleNode("/hrow/deli_city") == null ? null : doc.selectSingleNode("/hrow/deli_city").getText();
			if(StringUtils.isBlank(deli_city)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：deli_city，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：deli_city", false);
			}
			AreaVO deliAreaVO = areaService.getCityByCode(deli_city);
			if(deliAreaVO == null){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，区域不存在:"+deli_city+"，def1:"+def1 );
				logger.info("--------");
				return transToXML("区域不存在:"+deli_city, false);
			}
			String deli_addr_code = doc.selectSingleNode("/hrow/deli_addr_code") == null ? null : doc.selectSingleNode("/hrow/deli_addr_code").getText();
			String deli_detail_addr = doc.selectSingleNode("/hrow/deli_detail_addr") == null ? null : doc.selectSingleNode("/hrow/deli_detail_addr").getText();
			String deli_contact = doc.selectSingleNode("/hrow/deli_contact") == null ? null : doc.selectSingleNode("/hrow/deli_contact").getText();
			String deli_phone = doc.selectSingleNode("/hrow/deli_phone") == null ? null : doc.selectSingleNode("/hrow/deli_phone").getText();
			String deliAddrSql = "SELECT * FROM ts_address WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N')='N' AND addr_code = ? AND pk_city=? ";
			AddressVO deliAddrVO = NWDao.getInstance().queryForObject(deliAddrSql, AddressVO.class, deli_addr_code,deliAreaVO.getPk_area());
			if(deliAddrVO == null){
				deliAddrVO = new AddressVO();
				deliAddrVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(deliAddrVO);
				if(StringUtils.isBlank(deli_detail_addr)){
					logger.info((new UFDateTime(new Date())).toString() + ":同步失败，地址不存在，关键字段为空：deli_detail_addr，def1:"+def1 );
					logger.info("--------");
					return transToXML("地址不存在，关键字段为空：deli_detail_addr", false);
				}
				deliAddrVO.setAddr_name(deli_detail_addr);
				deliAddrVO.setAddr_code(deli_addr_code);
				deliAddrVO.setDetail_addr(deli_detail_addr);
				deliAddrVO.setContact(deli_contact);
				deliAddrVO.setPhone(deli_phone);
				deliAddrVO.setPk_city(deliAreaVO.getPk_area());
				deliAddrVO.setCreate_time(new UFDateTime(new Date()));
				deliAddrVO.setCreate_user(PK_USER);
				deliAddrVO.setPk_corp(PK_CORP);
				deliAddrVO.setAddr_type(AddressConst.ADDR_TYPE.TYPE1.intValue());
				toBeUpdate.add(deliAddrVO);
			}
			invoiceVO.setPk_delivery(deliAddrVO.getPk_address());
			invoiceVO.setDeli_city(deliAddrVO.getPk_city());
			invoiceVO.setDeli_province(deliAddrVO.getPk_province());
			invoiceVO.setDeli_area(deliAddrVO.getPk_area());
			invoiceVO.setDeli_detail_addr(deli_detail_addr == null ? deliAddrVO.getDetail_addr() : deli_detail_addr);
			invoiceVO.setDeli_contact(deli_contact == null ? deliAddrVO.getContact() : deli_contact);
			invoiceVO.setDeli_phone(deli_phone == null ? deliAddrVO.getPhone() : deli_phone);
			String arri_addr_code = doc.selectSingleNode("/hrow/arri_addr_code") == null ? null : doc.selectSingleNode("/hrow/arri_addr_code").getText();
			if(StringUtils.isBlank(arri_addr_code)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：arri_addr_code!，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：arri_addr_code", false);
			}
			String arri_detail_addr = doc.selectSingleNode("/hrow/arri_detail_addr") == null ? null : doc.selectSingleNode("/hrow/arri_detail_addr").getText();
			if(StringUtils.isBlank(arri_detail_addr)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：arri_detail_addr!，def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：arri_detail_addr", false);
			}
			String arri_contact = doc.selectSingleNode("/hrow/arri_contact") == null ? null : doc.selectSingleNode("/hrow/arri_contact").getText();
			String arri_phone = doc.selectSingleNode("/hrow/arri_phone") == null ? null : doc.selectSingleNode("/hrow/arri_phone").getText();
			AreaVO arriAreaVO = areaService.getCityByCode(arri_addr_code);
			if(arriAreaVO == null){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，区域不存在:"+arri_addr_code+"，def1:"+def1 );
				logger.info("--------");
				return transToXML("区域不存在:"+arri_addr_code, false);
			}
			String arriAddrSql = "SELECT * FROM ts_address WITH(NOLOCK) WHERE isnull(dr,0)=0 AND isnull(locked_flag,'N')='N' AND detail_addr = ? AND pk_city=? ";
			AddressVO arriAddrVO = NWDao.getInstance().queryForObject(arriAddrSql, AddressVO.class, arri_detail_addr,arriAreaVO.getPk_area());
			if(arriAddrVO == null){
				arriAddrVO = new AddressVO();
				arriAddrVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(arriAddrVO);
				arriAddrVO.setAddr_name(arri_detail_addr);
				arriAddrVO.setAddr_code(CodenoHelper.generateCode(FunConst.ADDRESS_FUN_CODE));
				arriAddrVO.setDetail_addr(arri_detail_addr);
				arriAddrVO.setContact(arri_contact);
				arriAddrVO.setPhone(arri_phone);
				arriAddrVO.setPk_city(arriAreaVO.getPk_area());
				arriAddrVO.setCreate_time(new UFDateTime(new Date()));
				arriAddrVO.setCreate_user(PK_USER);
				arriAddrVO.setPk_corp(PK_CORP);
				arriAddrVO.setAddr_type(AddressConst.ADDR_TYPE.TYPE1.intValue());
				toBeUpdate.add(arriAddrVO);
			}
			invoiceVO.setPk_arrival(arriAddrVO.getPk_address());
			invoiceVO.setArri_city(arriAddrVO.getPk_city());
			invoiceVO.setArri_province(arriAddrVO.getPk_province());
			invoiceVO.setArri_area(arriAddrVO.getPk_area());
			invoiceVO.setArri_detail_addr(arri_detail_addr);
			invoiceVO.setArri_contact(arri_contact == null ? arriAddrVO.getContact() : arri_contact);
			invoiceVO.setArri_phone(arri_phone == null ? arriAddrVO.getPhone() : arri_phone);
			String trans_code = doc.selectSingleNode("/hrow/trans_code") == null ? null : doc.selectSingleNode("/hrow/trans_code").getText();
			if(StringUtils.isBlank(trans_code)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：trans_code,def1:"+def1 );
				logger.info("--------");
				return transToXML("关键字段为空：trans_code", false);
			}
			TransTypeVO transTypeVO = transTypeService.getByCode(trans_code);
			if(transTypeVO == null){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，运输方式不存在:"+arri_addr_code+"，def1:"+def1 );
				logger.info("--------");
				return transToXML("运输方式不存在:"+arri_addr_code, false);
			}
			invoiceVO.setPk_trans_type(transTypeVO.getPk_trans_type());
			String memo = doc.selectSingleNode("/hrow/memo") == null ? null : doc.selectSingleNode("/hrow/memo").getText();
			invoiceVO.setMemo(memo);
			String psnname = doc.selectSingleNode("/hrow/psnname") == null ? null : doc.selectSingleNode("/hrow/psnname").getText();
			invoiceVO.setPk_psndoc(psnname);
			
			String def2 = doc.selectSingleNode("/hrow/def2") == null ? null : doc.selectSingleNode("/hrow/def2").getText();
			invoiceVO.setDef2(def2);
			String def5 = doc.selectSingleNode("/hrow/def5") == null ? null : doc.selectSingleNode("/hrow/def5").getText();
			invoiceVO.setDef5(def5);
			String def6 = doc.selectSingleNode("/hrow/def6") == null ? null : doc.selectSingleNode("/hrow/def6").getText();
			invoiceVO.setDef6(def6);
			String def7 = doc.selectSingleNode("/hrow/def7") == null ? null : doc.selectSingleNode("/hrow/def7").getText();
			invoiceVO.setDef7(def7);
			String def8 = doc.selectSingleNode("/hrow/def8") == null ? null : doc.selectSingleNode("/hrow/def8").getText();
			invoiceVO.setDef8(def8);
			String def9 = doc.selectSingleNode("/hrow/def9") == null ? null : doc.selectSingleNode("/hrow/def9").getText();
			invoiceVO.setDef9(def9);
			String def10 = doc.selectSingleNode("/hrow/def10") == null ? null : doc.selectSingleNode("/hrow/def10").getText();
			invoiceVO.setDef10(def10);
			
			invoiceVO.setVbillstatus(BillStatus.NEW);
			invoiceVO.setCreate_time(new UFDateTime(new Date()));
			invoiceVO.setCreate_user(PK_USER);
			invoiceVO.setUrgent_level(0);
			invoiceVO.setIf_return(UFBoolean.FALSE);
			invoiceVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());
			invoiceVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.EDI.intValue());
			billVO.setParentVO(invoiceVO);
			List nodeList = doc.selectNodes("/hrow/brow");
			InvPackBVO[] packBVOs = new InvPackBVO[nodeList.size()];
			
			for(int i = 0; i < nodeList.size(); i++) {
				Node node = (Node) nodeList.get(i);
				InvPackBVO packVO = new InvPackBVO();
				packVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(packVO);
				packVO.setPk_invoice(invoiceVO.getPk_invoice());
				packVO.setSerialno(i*10 + 10);
				String goods_code = node.selectSingleNode("goods_code") == null ? null : node.selectSingleNode("goods_code").getText();
				packVO.setGoods_code(goods_code);
				String goods_name = node.selectSingleNode("goods_name") == null ? null : node.selectSingleNode("goods_name").getText();
				packVO.setGoods_name(goods_name);
				String goods_type = node.selectSingleNode("goods_type") == null ? null : node.selectSingleNode("goods_type").getText();
				packVO.setGoods_type(goods_type);
				String num = node.selectSingleNode("num") == null ? null : node.selectSingleNode("num").getText();
				if(StringUtils.isBlank(num)){
					logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：num，def1:"+def1);
					logger.info("--------");
					return transToXML("关键字段为空：num", false);
				}
				packVO.setNum(Integer.parseInt(num));
				String weight = node.selectSingleNode("weight") == null ? null : node.selectSingleNode("weight").getText();
				if(StringUtils.isBlank(weight)){
					logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：weight，def1:"+def1 );
					logger.info("--------");
					return transToXML("关键字段为空：weight", false);
				}
				packVO.setWeight(new UFDouble(Double.parseDouble(weight)));
				String defb1 = node.selectSingleNode("def1") == null ? null : node.selectSingleNode("def1").getText();
				if(StringUtils.isBlank(defb1)){
					logger.info((new UFDateTime(new Date())).toString() + ":同步失败，子表关键字段为空：def1"+def1 );
					logger.info("--------");
					return transToXML("关键字段为空：def1", false);
				}
				packVO.setDef1(defb1);
				String defb2 = node.selectSingleNode("def2") == null ? null : node.selectSingleNode("def2").getText();
				packVO.setDef2(defb2);
				String defb3 = node.selectSingleNode("def3") == null ? null : node.selectSingleNode("def3").getText();
				packVO.setDef3(defb3);
				String defb4 = node.selectSingleNode("def4") == null ? null : node.selectSingleNode("def4").getText();
				packVO.setDef4(defb4);
				String defb5 = node.selectSingleNode("def5") == null ? null : node.selectSingleNode("def5").getText();
				packVO.setDef5(defb5);
				String defb6 = node.selectSingleNode("def6") == null ? null : node.selectSingleNode("def6").getText();
				packVO.setDef6(defb6);
				String defb7 = node.selectSingleNode("def7") == null ? null : node.selectSingleNode("def7").getText();
				packVO.setDef7(defb7);
				String defb8 = node.selectSingleNode("def8") == null ? null : node.selectSingleNode("def8").getText();
				packVO.setDef8(defb8);
				String defb9 = node.selectSingleNode("def9") == null ? null : node.selectSingleNode("def9").getText();
				packVO.setDef9(defb9);
				String defb10 = node.selectSingleNode("def10") == null ? null : node.selectSingleNode("def10").getText();
				packVO.setDef10(defb10);
				packBVOs[i] = packVO;
				
			}
			billVO.setTableVO(TabcodeConst.TS_INV_PACK_B, packBVOs);
			InvoiceUtils.setHeaderCount(billVO, null, false);
//			ParamVO paramVO = new ParamVO();
//			paramVO.setFunCode(FunConst.INVOICE_CODE);
//			paramVO.getAttr().put("updateAddr", false);
			NWDao.getInstance().saveOrUpdate(toBeUpdate);
			logger.info((new UFDateTime(new Date())).toString() + ":开始写入发货单数据！："+invoiceVO.getVbillno() );
			//invoiceService.save(billVO, paramVO);
			insertLine(invoiceVO);
			saveFHD(billVO);
			NWDao.getInstance().saveOrUpdate(billVO);
			EdiInvoiceVO edInvoiceVO = new EdiInvoiceVO();
			edInvoiceVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(edInvoiceVO);
			edInvoiceVO.setVbillno(invoiceVO.getVbillno());
			edInvoiceVO.setDef1(invoiceVO.getDef1());
			edInvoiceVO.setSyncexp_flag(UFBoolean.TRUE);
			edInvoiceVO.setSync_time(new UFDateTime(new Date()));
			NWDao.getInstance().saveOrUpdate(edInvoiceVO);
			logger.info((new UFDateTime(new Date())).toString() + ":同步成功，本次同步结束！："+invoiceVO.getVbillno() );
			return transToXML(invoiceVO.getDef1(), true);
		} catch (UnsupportedEncodingException e) {
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，编码解析异常"+def1 );
			logger.info("--------");
			return transToXML("编码解析异常！", false);
		} catch (DocumentException e) {
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，文本解析异常"+def1 );
			logger.info("--------");
			return transToXML("文本解析异常！", false);
		} catch (NullPointerException e) {
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，空指针异常"+def1 );
			logger.info("--------");
			return transToXML("空指针异常，请联系开发人员！", false);
		} catch (Exception e) {
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，"+e.getMessage()+"def1"+def1 );
			logger.info("--------");
			return transToXML(e.getMessage(), false);
		}
	}
	
	public void insertLine(InvoiceVO invoiceVO){
		List<TransLineVO> transLineVOs = TransLineUtils.matchTransLine(TransLineConst.PZLX, invoiceVO);
		if (transLineVOs != null && transLineVOs.size() > 0) {
			invoiceVO.setPz_line(transLineVOs.get(0).getPk_trans_line());
			invoiceVO.setPz_mileage(transLineVOs.get(0).getMileage());
		} else {
			TransLineVO transLineVO = TransLineUtils.matchTransLineByArea(TransLineConst.PZLX, invoiceVO);
			if (transLineVO != null) {
				invoiceVO.setPz_line(transLineVO.getPk_trans_line());
				invoiceVO.setPz_mileage(transLineVO.getMileage());
			} else {
				invoiceVO.setPz_line(null);
				invoiceVO.setPz_mileage(null);
			}
		}
	}
	
	public void saveFHD(ExAggInvoiceVO billVO){
		if(billVO == null){
			return;
		}
		InvoiceVO parentVO = (InvoiceVO) billVO.getParentVO();
		parentVO.setTrans_type(DataDictConst.TRANSPORT_TYPE.LD.intValue());
		
		// 保存到应收明细表
		ReceiveDetailVO detailVO = new ReceiveDetailVO();
		detailVO.setStatus(VOStatus.NEW);
		detailVO.setVbillstatus(BillStatus.NEW);
		NWDao.setUuidPrimaryKey(detailVO);
		detailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YSMX)); // 生成应收明细的单据号
		detailVO.setCreate_user(parentVO.getCreate_user());
		detailVO.setPk_corp(parentVO.getPk_corp());
		detailVO.setCreate_time(new UFDateTime(new Date()));
		detailVO.setDbilldate(new UFDate());
		detailVO.setPk_customer(parentVO.getPk_customer());
		detailVO.setBala_customer(parentVO.getBala_customer());
		detailVO.setCurrency(ParameterHelper.getCurrency());
		detailVO.setPk_op_project(parentVO.getPk_op_project());
		detailVO.setBilling_corp(parentVO.getBilling_corp());
		detailVO.setPack_num_count(parentVO.getPack_num_count());
		detailVO.setNum_count(parentVO.getNum_count());
		detailVO.setFee_weight_count(parentVO.getFee_weight_count());
		detailVO.setWeight_count(parentVO.getWeight_count());
		detailVO.setVolume_count(parentVO.getVolume_count());
		detailVO.setCost_amount(parentVO.getCost_amount());
		detailVO.setUngot_amount(parentVO.getCost_amount());// 未收金额等于总金额
		detailVO.setBalatype(parentVO.getBalatype());
		detailVO.setInvoice_vbillno(parentVO.getVbillno()); // 发货单单据号
		detailVO.setMemo(parentVO.getMemo());
		detailVO.setRece_type(ReceiveDetailConst.ORIGIN_TYPE); // 表示这是由发货单生成的应收明细
		detailVO.setMerge_type(ReceiveDetailConst.MERGE_TYPE.UNMERGE.intValue()); // 合并类型
		detailVO.setOrderno(parentVO.getOrderno());
		detailVO.setCust_orderno(parentVO.getCust_orderno());
		detailVO.setAccount_period(new UFDateTime(parentVO.getReq_deli_date()));
		detailVO.setBilling_corp(parentVO.getBilling_corp());
		detailVO.setPk_op_project(parentVO.getPk_op_project());
		NWDao.getInstance().saveOrUpdate(detailVO);
		
		InvTrackingVO itVO = new InvTrackingVO();
		itVO.setTracking_status(TrackingConst.NEW);
		itVO.setTracking_time(new UFDateTime(new Date()));
		itVO.setTracking_memo("新建");
		itVO.setInvoice_vbillno(parentVO.getVbillno());
		itVO.setPk_corp(parentVO.getPk_corp());
		itVO.setCreate_user(parentVO.getCreate_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		NWDao.getInstance().saveOrUpdate(itVO);
	}
	
	@RequestMapping(value = "/deleteFHD.do")
	@ResponseBody
	public String deleteFHD(HttpServletRequest request, HttpServletResponse response){
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		try {
			String cust_code_data = request.getParameter("cust_code");
			if(StringUtils.isBlank(cust_code_data)){
				return transToXML("关键字段为空：cust_code", false);
			}
			String cust_code = new String(cust_code_data.getBytes("ISO-8859-1"),"UTF-8");
			
			String def1_data = request.getParameter("def1");
			if(StringUtils.isBlank(def1_data)){
				return transToXML("关键字段为空：def1", false);
			}
			String def1 = new String(def1_data.getBytes("ISO-8859-1"),"UTF-8");
			
			CustomerVO customerVO = custService.getByCode(cust_code);
			if(customerVO == null){
				return transToXML("客户不存在！", false);
			}
			InvoiceVO invoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "pk_customer=? and def1=?", customerVO.getPk_customer(),def1);
			if(invoiceVO == null){
				return transToXML("订单不存在！", false);
			}
			if(invoiceVO.getVbillstatus() != BillStatus.NEW){
				return transToXML("当前订单已排单或确认，如需撤消，请先在ＴＭＳ进行处理！", false);
			}
			ReceiveDetailVO receVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class, "invoice_vbillno =?", invoiceVO.getVbillno());
			if(receVO == null || receVO.getVbillstatus() != BillStatus.NEW){
				return transToXML("订单应收不正确！", false);
			}
			invoiceService.batchDelete(InvoiceVO.class, new String[]{invoiceVO.getPk_invoice()});		
		} catch (Exception e) {
			return transToXML("删除失败"+e.getMessage(), false);
		}
		return transToXML("删除成功",true);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/importSJFH.do")
	@ResponseBody
	public String importSJFH(HttpServletRequest request, HttpServletResponse response){
		logger.info((new UFDateTime(new Date())).toString() + ":接口开始同步实际发货信息!");
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		String xmlData = request.getParameter("xml");
		if(StringUtils.isBlank(xmlData)){
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，没有XML文本！");
			logger.info("--------");
			return transToXML("没有XML文本！", false);
		}
		String ent_vbillno = "";
		try {
			String xml =  new String(xmlData.getBytes("ISO-8859-1"),"UTF-8");
			Document doc = DocumentHelper.parseText(xml);
			ent_vbillno = doc.selectSingleNode("/hrow/ent_vbillno") == null ? null : doc.selectSingleNode("/hrow/ent_vbillno").getText();
			if(StringUtils.isBlank(ent_vbillno)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：ent_vbillno! ");
				logger.info("--------");
				return transToXML("关键字段为空：ent_vbillno", false);
			}
			String ck_vbillno = doc.selectSingleNode("/hrow/ck_vbillno") == null ? null : doc.selectSingleNode("/hrow/ck_vbillno").getText();
			if(StringUtils.isBlank(ck_vbillno)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：ck_vbillno!  ent_vbillno:"+ent_vbillno );
				logger.info("--------");
				return transToXML("关键字段为空：ck_vbillno", false);
			}
			String amount = doc.selectSingleNode("/hrow/amount") == null ? null : doc.selectSingleNode("/hrow/amount").getText();
			if(StringUtils.isBlank(amount)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：amount!  ent_vbillno:"+ent_vbillno );
				logger.info("--------");
				return transToXML("关键字段为空：amount", false);
			}
			
			String sql = "SELECT ts_ent_line_b.* FROM ts_ent_line_b WITH(NOLOCK) "
					+ " LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_ent_line_b.pk_entrust = ts_entrust.pk_entrust "
					+ " WHERE isnull(ts_ent_line_b.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 "
					+ " and ts_ent_line_b.addr_flag = 'S' and ts_entrust.vbillno=?";
			
			List<EntLineBVO> entLineBVOs = NWDao.getInstance().queryForList(sql, EntLineBVO.class, ent_vbillno);
			if(entLineBVOs == null || entLineBVOs.size() != 1 ){
				logger.info((new UFDateTime(new Date())).toString() + ":同步失败，没有符合条件的委托单!  ent_vbillno:"+ent_vbillno );
				logger.info("--------");
				return transToXML("没有符合条件的委托单！", false);
			}
			if(entLineBVOs.get(0).getArrival_flag()!=null && entLineBVOs.get(0).getArrival_flag().equals(UFBoolean.TRUE)){
				logger.info((new UFDateTime(new Date())).toString() + ":同步成功，但是这个单子已经被提货了，需要检查数据!  ent_vbillno:"+ent_vbillno );
				logger.info("--------");
				return transToXML("同步成功，但是这个单子已经被提货了,ent_vbillno:"+ent_vbillno , true);
			}
			
			EntLinePackBVO[] entLinePackBVOs_db = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLinePackBVO.class, 
					"pk_ent_line_b=?", entLineBVOs.get(0).getPk_ent_line_b());
			List nodeList = doc.selectNodes("/hrow/brow");
			List<EntLinePackBVO> entLinePackBVOs = new ArrayList<EntLinePackBVO>();
			for(EntLinePackBVO entLinePackBVO_db : entLinePackBVOs_db){
				boolean flag = true;
				for(int i = 0; i < nodeList.size(); i++) {
					Node node = (Node) nodeList.get(i);
					String num = node.selectSingleNode("num") == null ? null : node.selectSingleNode("num").getText();
					if(StringUtils.isBlank(num)){
						logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：num!  ent_vbillno:"+ent_vbillno );
						logger.info("--------");
						return transToXML("关键字段为空：num", false);
					}
					String weight = node.selectSingleNode("weight") == null ? null : node.selectSingleNode("weight").getText();
					if(StringUtils.isBlank(weight)){
						logger.info((new UFDateTime(new Date())).toString() + "::同步失败，关键字段为空：weight!  ent_vbillno:"+ent_vbillno );
						logger.info("--------");
						return transToXML("关键字段为空：weight", false);
					}
					String ent_pack_b = node.selectSingleNode("ent_pack_b") == null ? null : node.selectSingleNode("ent_pack_b").getText();
					if(StringUtils.isBlank(ent_pack_b)){
						logger.info((new UFDateTime(new Date())).toString() + ":同步失败，关键字段为空：ent_pack_b!  ent_vbillno:"+ent_vbillno );
						logger.info("--------");
						return transToXML("关键字段为空：ent_pack_b", false);
					}
					if(entLinePackBVO_db.getPk_ent_pack_b().equals(ent_pack_b)){
						EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
						entLinePackBVO.setPk_entrust(entLineBVOs.get(0).getPk_entrust());
						entLinePackBVO.setPk_ent_line_b(entLineBVOs.get(0).getPk_ent_line_b());
						entLinePackBVO.setPk_ent_line_pack_b(entLinePackBVO_db.getPk_ent_line_pack_b());
						entLinePackBVO.setPk_ent_pack_b(ent_pack_b);
						entLinePackBVO.setNum(Integer.parseInt(num));
						entLinePackBVO.setWeight(new UFDouble(Double.parseDouble(weight)));
						entLinePackBVO.setVolume(UFDouble.ZERO_DBL);
						entLinePackBVOs.add(entLinePackBVO);
						flag = false;
						break;
					}
				}
				if(flag){
					EntLinePackBVO entLinePackBVO = new EntLinePackBVO();
					entLinePackBVO.setPk_entrust(entLinePackBVO_db.getPk_entrust());
					entLinePackBVO.setPk_ent_line_b(entLinePackBVO_db.getPk_ent_line_b());
					entLinePackBVO.setPk_ent_pack_b(entLinePackBVO_db.getPk_ent_pack_b());
					entLinePackBVO.setPk_ent_line_pack_b(entLinePackBVO_db.getPk_ent_line_pack_b());
					entLinePackBVO.setNum(0);
					entLinePackBVO.setWeight(UFDouble.ZERO_DBL);
					entLinePackBVOs.add(entLinePackBVO);
				}
			}
			entLineBVOs.get(0).setAct_arri_date((new UFDateTime(new Date())).toString());
			entLineBVOs.get(0).setEntLinePackBVOs(entLinePackBVOs);
			trackingService.confirmArrival(entLineBVOs.get(0),1);
			logger.info((new UFDateTime(new Date())).toString() + ":提货操作执行结束!  ent_vbillno:"+ent_vbillno );
			logger.info("--------");
			doAfterImportSJFH(ent_vbillno, ck_vbillno, amount);
		} catch (Exception e) {
			logger.info((new UFDateTime(new Date())).toString() + ":同步失败，"+e.getMessage()+"  ent_vbillno:"+ent_vbillno );
			logger.info("--------");
			return transToXML(e.getMessage(), false);
		}
		logger.info((new UFDateTime(new Date())).toString() + ":同步成功!  ent_vbillno:"+ent_vbillno );
		logger.info("--------");
		return transToXML("导入成功！", true);
	}
	
	public void doAfterImportSJFH(String ent_vbillno, String ck_vbillno, String amount){
		processAfterImportSJFH(ent_vbillno, ck_vbillno, amount);
		String sql = "SELECT ts_invoice.* FROM ts_invoice WITH(NOLOCK) LEFT JOIN ts_entrust WITH(NOLOCK)"
				+ " ON ts_entrust.invoice_vbillno = ts_invoice.vbillno "
				+ " WHERE isnull(ts_invoice.dr,0)=0 AND isnull(ts_entrust.dr,0)=0 AND ts_entrust.vbillno =?";
		List<InvoiceVO> invoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class, ent_vbillno);
		CMUtils.totalCostComput(invoiceVOs);
	}
	
	public void processAfterImportSJFH(String ent_vbillno, String ck_vbillno, String amount){
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", ent_vbillno);
		entrustVO.setStatus(VOStatus.UPDATED);
		entrustVO.setDef1(ck_vbillno);
		entrustVO.setDef11(new UFDouble(amount));
		toBeUpdate.add(entrustVO);
		if(org.nw.basic.util.StringUtils.isNotBlanks(amount)){
			String sql = "SELECT ts_receive_detail.* FROM ts_receive_detail WITH(NOLOCK) "
					+ " LEFT JOIN ts_invoice WITH(NOLOCK) ON ts_receive_detail.invoice_vbillno = ts_invoice.vbillno "
					+ " LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_entrust.invoice_vbillno = ts_invoice.vbillno "
					+ " WHERE isnull(ts_receive_detail.dr,0)=0  AND isnull(ts_invoice.dr,0)=0  AND isnull(ts_entrust.dr,0)=0  "
					+ " AND ts_entrust.vbillno =?";
			ReceiveDetailVO receiveDetailVO = NWDao.getInstance().queryForObject(sql, ReceiveDetailVO.class, ent_vbillno);
			if(receiveDetailVO != null){
				receiveDetailVO.setStatus(VOStatus.UPDATED);
				receiveDetailVO.setCost_amount(receiveDetailVO.getCost_amount() == null ? new UFDouble(amount)
						: receiveDetailVO.getCost_amount().add(new UFDouble(amount)));
				receiveDetailVO.setUngot_amount(receiveDetailVO.getUngot_amount() == null ? new UFDouble(amount)
						: receiveDetailVO.getUngot_amount().add(new UFDouble(amount)));
				toBeUpdate.add(receiveDetailVO);
				ReceDetailBVO detailBVO = new ReceDetailBVO();
				detailBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(detailBVO);
				detailBVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail());
				detailBVO.setSystem_create(new UFBoolean(true));
				detailBVO.setAmount(new UFDouble(amount));
				detailBVO.setPk_expense_type("4468028d8b6f4fe3b4f163c29ed74245");
				detailBVO.setMemo(ent_vbillno);
				toBeUpdate.add(detailBVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
	}
	
	@RequestMapping(value = "/importDCZT.do")
	@ResponseBody
	public String importDCZT(HttpServletRequest request, HttpServletResponse response){
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		try {
			
			String lotData = request.getParameter("lot");
			if(StringUtils.isBlank(lotData)){
				return transToXML("关键字段为空：lot", false);
			}
			String lot =  new String(lotData.getBytes("ISO-8859-1"),"UTF-8");
			
			String statusData = request.getParameter("status");
			if(StringUtils.isBlank(statusData)){
				return transToXML("关键字段为空：status", false);
			}
			String status =  new String(statusData.getBytes("ISO-8859-1"),"UTF-8");
			
			EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "lot=?", lot);
			if(entrustVOs == null || entrustVOs.length == 0){
				return transToXML("批次号错误！", false);
			}
			for(EntrustVO entVO : entrustVOs){
				entVO.setStatus(VOStatus.UPDATED);
				entVO.setDef4(status);
			}
			NWDao.getInstance().saveOrUpdate(entrustVOs);
		} catch (Exception e) {
			return transToXML(e.getMessage(), false);
		}
		return transToXML("导入成功！", true);
	}
	
	@RequestMapping(value = "/importDYFP.do")
	@ResponseBody
	public String importDYFP(HttpServletRequest request, HttpServletResponse response){
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		String xmlData = request.getParameter("xml");
		if(StringUtils.isBlank(xmlData)){
			return transToXML("没有XML文本！", false);
		}
		
		try {
			String xml =  new String(xmlData.getBytes("ISO-8859-1"),"UTF-8");
			Document doc = DocumentHelper.parseText(xml);
			@SuppressWarnings("rawtypes")
			List nodeList = doc.selectNodes("/hrow/brow");
			List<SuperVO> toBeUpate = new ArrayList<SuperVO>();
			if(nodeList != null && nodeList.size() > 0){
				for(int i=0;i<nodeList.size();i++){
					Node node = (Node) nodeList.get(i);
					String ent_vbillno = node.selectSingleNode("ent_vbillno") == null ? null : node.selectSingleNode("ent_vbillno").getText();
					if(StringUtils.isBlank(ent_vbillno)){
						return transToXML("关键字段为空：ent_vbillno", false);
					}
					
					EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", ent_vbillno);
					if(entrustVO == null){
						return transToXML("委托单号错误:" + ent_vbillno , false);
					}
					entrustVO.setStatus(VOStatus.UPDATED);
					entrustVO.setDef5(UFBoolean.TRUE.toString());
					toBeUpate.add(entrustVO);
				}
				NWDao.getInstance().saveOrUpdate(toBeUpate);
			}
			
		} catch (Exception e) {
			return transToXML(e.getMessage(), false);
		}
		return transToXML("导入成功！", true);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/importGBDD.do")
	@ResponseBody
	public String importGBDD(HttpServletRequest request, HttpServletResponse response){
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		String xmlData = request.getParameter("xml");
		if(StringUtils.isBlank(xmlData)){
			return transToXML("没有XML文本！", false);
		}

		try {
			String xml =  new String(xmlData.getBytes("ISO-8859-1"),"UTF-8");
			Document doc = DocumentHelper.parseText(xml);
			String erpdjlsh = doc.selectSingleNode("/hrow/erpdjlsh") == null ? null : doc.selectSingleNode("/hrow/erpdjlsh").getText();
			if(StringUtils.isBlank(erpdjlsh)){
				return transToXML("关键字段为空：erpdjlsh", false);
			}
			String confirm_memo = doc.selectSingleNode("/hrow/confirm_memo") == null ? null : doc.selectSingleNode("/hrow/confirm_memo").getText();
			String wplsh_0s = "";
			String wplsh_1s = "";
			List nodeList = doc.selectNodes("/hrow/brow");
			for(int i = 0; i < nodeList.size(); i++) {
				Node node = (Node) nodeList.get(i);
				String wplsh = node.selectSingleNode("wplsh") == null ? null : node.selectSingleNode("wplsh").getText();
				if(StringUtils.isBlank(wplsh)){
					return transToXML("关键字段为空：wplsh", false);
				}
				String receipt = node.selectSingleNode("receipt") == null ? null : node.selectSingleNode("receipt").getText();
				if(StringUtils.isBlank(receipt)){
					return transToXML("关键字段为空：receipt", false);
				}
				if(Integer.parseInt(receipt) == 0){
					wplsh_0s +=(wplsh +",");
				}else if(Integer.parseInt(receipt) == 1){
					wplsh_1s +=(wplsh +",");
				}else{
					return transToXML("关键字段：receipt值不正确：" + receipt, false);
				}
			}
			String result = "";
			if(wplsh_0s.length() > 0){
				result = getGBDDData(erpdjlsh, wplsh_0s.substring(0, wplsh_0s.length()-1), "0",confirm_memo);
			}
			if(StringUtils.isNotBlank(result)){
				return transToXML(result, false);
			}
			if(wplsh_1s.length() > 0){
				result = getGBDDData(erpdjlsh, wplsh_1s.substring(0, wplsh_1s.length()-1), "1",confirm_memo);
			}
			if(StringUtils.isNotBlank(result)){
				return transToXML(result, false);
			}
		} catch (Exception e) {
			return transToXML(e.getMessage(), false);
		}
		return transToXML("导入成功！", true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getGBDDData(String erpdjlsh,String wplshs,String receipt, String confirm_memo){
		// 调用存储过程
		// 存储过程名称
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = "ts_edi_close_seg_proc";
		final String DEF1 = erpdjlsh;
		final String DEFB1 = wplshs;
		final String STATUS = receipt;
		final String MEMO = confirm_memo;
		final String EMPTY = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 5;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, DEF1);
					cs.setString(2, DEFB1);
					cs.setString(3, STATUS);
					cs.setString(4, MEMO);
					cs.setString(5, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs != null && rs.next()) {
						RETURN.add(rs.getString(1));
						break;
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(RETURN.size() > 0){
			return RETURN.get(0);
		}else{
			return null;
		}
		
	} 
	
	@RequestMapping(value = "/importQRGB.do")
	@ResponseBody
	public String importQRGB(HttpServletRequest request, HttpServletResponse response){
		String uid = request.getParameter("uid");//用户名
		String pwd = request.getParameter("pwd");//密码
		
		String error = authentication(uid, pwd);
		if(StringUtils.isNotBlank(error)){
			return transToXML(error, false);
		}
		String xmlData = request.getParameter("xml");
		if(StringUtils.isBlank(xmlData)){
			return transToXML("没有XML文本！", false);
		}

		try {
			String xml =  new String(xmlData.getBytes("ISO-8859-1"),"UTF-8");
			Document doc = DocumentHelper.parseText(xml);
			String vbillno = doc.selectSingleNode("/hrow/vbillno") == null ? "" : doc.selectSingleNode("/hrow/vbillno").getText();
			if(StringUtils.isBlank(vbillno)){
				return transToXML("关键字段为空：vbillno", false);
			}
			String receipt = doc.selectSingleNode("/hrow/receipt") == null ? "" : doc.selectSingleNode("/hrow/receipt").getText();
			if(StringUtils.isBlank(receipt)){
				return transToXML("关键字段为空：receipt", false);
			}
			String confirm_man = doc.selectSingleNode("/hrow/confirm_man") == null ? "" : doc.selectSingleNode("/hrow/confirm_man").getText();
			String confirm_date = doc.selectSingleNode("/hrow/confirm_date") == null ? "" : doc.selectSingleNode("/hrow/confirm_date").getText();
			String confirm_memo = doc.selectSingleNode("/hrow/confirm_memo") == null ? "" : doc.selectSingleNode("/hrow/confirm_memo").getText();
			
			String msg = getQRGBData(vbillno, receipt, confirm_man, confirm_date, confirm_memo);
			if(org.nw.basic.util.StringUtils.isNotBlanks(msg)){
				return transToXML(msg, false);
			}
			
		} catch (Exception e) {
			return transToXML(e.getMessage(), false);
		}
		return transToXML("导入成功！", true);
	}
	
	
	public String getQRGBData(String vbillno,String receipt,String confirm_man,String confirm_date, String confirm_memo){
		// 调用存储过程
		// 存储过程名称
		final List<String> RETURN = new ArrayList<String>();
		final String PROC_NAME = "ts_edi_close_result_seg_proc";
		final String BILLNO = vbillno;
		final String RECEIPT = receipt;
		final String MAN = confirm_man;
		final String DATE = confirm_date;
		final String MEMO = confirm_memo;
		final String EMPTY = "";
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, BILLNO);
					cs.setString(2, RECEIPT);
					cs.setString(3, MAN);
					cs.setString(4, DATE);
					cs.setString(5, MEMO);
					cs.setString(6, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback<Object>() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					cs.execute();
					ResultSet rs = cs.getResultSet();
					while (rs != null && rs.next()) {
						RETURN.add(rs.getString(1));
						break;
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(RETURN.size() > 0){
			return RETURN.get(0);
		}else{
			return null;
		}
		
	} 
	
	private String authentication(String uid,String pwd){
		try {
			return HttpUtils.authentication(uid, pwd);
		} catch (ApiException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
	
	private String transToXML(String strReturnMsg,boolean trueOrFalse){
		// 创建message的xml片段
		String body1 = "<result>";
		String body2 = "</result>";
		if(trueOrFalse){
			body1 += "<success>true</success>";
		}else{
			body1 += "<success>false</success>";
		}
		body1 += "<msg>"+strReturnMsg+"</msg>";
		return StringEscapeUtils.unescapeXml(body1 + body2);
	}

	
}
