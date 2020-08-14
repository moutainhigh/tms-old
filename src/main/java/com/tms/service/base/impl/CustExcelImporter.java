package com.tms.service.base.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.ExcelImporter;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.WebUtils;

import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CustAddrVO;
import com.tms.vo.base.CustBalaVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.ExAggCustVO;


/**
 * yaojiie 2015 12 30 客户信息导入
 */
public class CustExcelImporter extends ExcelImporter {
	
	public CustExcelImporter(ParamVO paramVO, IToftService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolveForMultiTable(file);
		List<String> custCodes = new ArrayList<String>();
		List<String> addressCodes = new ArrayList<String>();
		List<CustomerVO> customerVOs = new ArrayList<CustomerVO>();
		List<CustAddrVO[]> custAddrVOsList = new ArrayList<CustAddrVO[]>();
		for(AggregatedValueObject aggvo : aggVOs){
			CustomerVO customerVO = (CustomerVO) aggvo.getParentVO();
			custCodes.add(customerVO.getCust_code());
			customerVOs.add(customerVO);
			ExAggCustVO exAggCustVO = (ExAggCustVO)aggvo;
			//pk_address 位置放入了addrcode
			CustAddrVO[] custAddrVOs = (CustAddrVO[]) exAggCustVO.getTableVO("ts_cust_addr");
			custAddrVOsList.add(custAddrVOs);
			for(CustAddrVO custAddrVO : custAddrVOs){
				addressCodes.add(custAddrVO.getPk_address());
			}
			
		}
		//将地址Code转化为地址的VO
		AddressVO[] oldAddressVOs = null;
		if(addressCodes != null && addressCodes.size() > 0){
			String addressCond = NWUtils.buildConditionString(addressCodes.toArray(new String[addressCodes.size()]));
			oldAddressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, "addr_code in " + addressCond);
		}
		//拿到所有的地址和客户信息 在数据库里根据客户编码查找客户信息
		CustomerVO[] oldCustomerVOs = null;
		List<CustAddrVO> oldCustAddrVOs = new ArrayList<CustAddrVO>();
		if(custCodes != null && custCodes.size() > 0){
			String custCond = NWUtils.buildConditionString(custCodes.toArray(new String[custCodes.size()]));
			oldCustomerVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CustomerVO.class, "cust_code in " + custCond);
			String sql = "SELECT ts_cust_addr.* FROM ts_cust_addr LEFT JOIN ts_customer ON ts_cust_addr.pk_customer = ts_customer.pk_customer "
						+ " WHERE isnull(ts_cust_addr.dr,0) = 0 AND isnull(ts_cust_addr.locked_flag,'N') = 'N' "
						+ " AND isnull(ts_customer.dr,0) = 0 AND isnull(ts_customer.locked_flag,'N') = 'N' "
						+ " AND ts_customer.cust_code  IN " + custCond;
			oldCustAddrVOs = NWDao.getInstance().queryForList(sql,CustAddrVO.class);
		}
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		//第一步：将导入的客户信息和数据库中的客户信息做比对，确定哪些该新建，哪些该更新。
		for(CustomerVO customerVO : customerVOs){
			boolean flag = true;
			for(CustomerVO oldCustomerVO : oldCustomerVOs){
				//取出系统原有的客户信息里的客户地址信息，在导入时，如果存在重复的需要处理
				if(customerVO.getCust_code().equals(oldCustomerVO.getCust_code())){
					//数据库已经有了这个客户信息，不需要操作。
					flag = false;
				}
			}
			if(flag){
				//yaojiie 2016 1 4 默认结算客户
				customerVO.setStatus(VOStatus.NEW);
				//yaojiie 2016 2 16 增加公司 创建人创建时间信息
				if(StringUtils.isBlank(customerVO.getPk_corp())){
					customerVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				}
				customerVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				customerVO.setCreate_time(new UFDateTime(new Date()));
				CustBalaVO custBalaVO = new CustBalaVO();
				custBalaVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(custBalaVO);
				custBalaVO.setPk_customer(customerVO.getPk_customer());
				custBalaVO.setPk_related_cust(customerVO.getPk_customer());
				custBalaVO.setIs_default(UFBoolean.TRUE);
				VOs.add(customerVO);
				VOs.add(custBalaVO);
			}
		}
		//第二部验证地址信息的正确性,并将地址的code变成PK值
		for(CustAddrVO[] custAddrVOs : custAddrVOsList){
			for(CustAddrVO custAddrVO : custAddrVOs){
				boolean flag = true;
				for(AddressVO addressVO : oldAddressVOs){
					if(addressVO.getAddr_code().equals(custAddrVO.getPk_address())){
						custAddrVO.setPk_address(addressVO.getPk_address());
						flag = false;
						break;
					}
				}
				//yaojiie 2016 1 4 可能存在地址为空的情况，这时允许存在。
				if(flag && StringUtils.isNotBlank(custAddrVO.getPk_address())){
					throw new BusiException("地址[?]不正确，请检查数据！",custAddrVO.getPk_address());
				}
			}
		}
		//第三部，在验证完所有信息之后，保存地址和客户信息，此时需要将数据库已有客户信息的那部分地址提出来，以为需要重新赋予PK值。
		List<CustAddrVO> allCustAddrVOs = new ArrayList<CustAddrVO>();
		for(AggregatedValueObject aggvo : aggVOs){
			CustomerVO customerVO = (CustomerVO) aggvo.getParentVO();
			ExAggCustVO exAggCustVO = (ExAggCustVO)aggvo;
			CustAddrVO[] custAddrVOs = (CustAddrVO[]) exAggCustVO.getTableVO("ts_cust_addr");
			for(CustomerVO oldCustomerVO : oldCustomerVOs){
				if(oldCustomerVO.getCust_code().equals(customerVO.getCust_code())){
					customerVO.setPk_customer(oldCustomerVO.getPk_customer());
					for(CustAddrVO custAddrVO : custAddrVOs){
						custAddrVO.setPk_customer(oldCustomerVO.getPk_customer());
					}
				}
			}
			allCustAddrVOs.addAll(Arrays.asList(custAddrVOs));
		}
		//yaojiie 2016 1 4 去掉为空的地址信息
		List<CustAddrVO> notEmptyCustAddrVOs = new ArrayList<CustAddrVO>();
		for(CustAddrVO custAddrVO : allCustAddrVOs){
			if(StringUtils.isNotBlank(custAddrVO.getPk_address())){
				notEmptyCustAddrVOs.add(custAddrVO);
			}
		}
		//对导入的信息进行分组，这样可以去除掉相同的导入信息
		Map<String, List<CustAddrVO>> groupMap = new HashMap<String, List<CustAddrVO>>();
		for (CustAddrVO custAddrVO : notEmptyCustAddrVOs) {
			String key = new StringBuffer().append(custAddrVO.getPk_customer()).append(custAddrVO.getPk_address())
					.toString();
			List<CustAddrVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<CustAddrVO>();
				groupMap.put(key, voList);
			}
			voList.add(custAddrVO);
		}
		for(String key : groupMap.keySet()){
			CustAddrVO custAddrVO = groupMap.get(key).get(0);
			boolean flag = true;
			for(CustAddrVO oldCustAddrVO : oldCustAddrVOs){
				if(custAddrVO.getPk_customer().equals(oldCustAddrVO.getPk_customer())
						&& custAddrVO.getPk_address().equals(oldCustAddrVO.getPk_address())){
					flag = false;
					break;
				}
			}
			if(flag){
				custAddrVO.setStatus(VOStatus.NEW);
				VOs.add(custAddrVO);
			}
		}
		NWDao.getInstance().saveOrUpdate(VOs);
		logBuf.append("共导入" + importNum + "记录");
		logger.info(logBuf.toString());
	}
}
