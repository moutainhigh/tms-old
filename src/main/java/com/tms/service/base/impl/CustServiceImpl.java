package com.tms.service.base.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.utils.AreaHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDouble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.service.base.CustService;
import com.tms.service.base.TransTypeService;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CustAddrVO;
import com.tms.vo.base.CustBalaVO;
import com.tms.vo.base.CustOpProjectBVO;
import com.tms.vo.base.CustRateVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.base.TransTypeVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-17 下午11:13:33
 */
@Service
public class CustServiceImpl extends AbsBaseDataServiceImpl implements CustService {

	@Autowired
	private TransTypeService transTypeService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggCustVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, CustomerVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, CustomerVO.PK_CUSTOMER);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggCustVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, CustAddrVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, CustAddrVO.PK_CUSTOMER);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_cust_addr");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_cust_addr");

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggCustVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, CustBalaVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, CustBalaVO.PK_CUSTOMER);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_cust_bala");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_cust_bala");

			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggCustVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, CustOpProjectBVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, CustOpProjectBVO.PK_CUSTOMER);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, CustOpProjectBVO.TS_CUST_OP_PROJECT_B);
			childVO2.setAttributeValue(VOTableVO.VOTABLE, CustOpProjectBVO.TS_CUST_OP_PROJECT_B);
			
			VOTableVO childVO3 = new VOTableVO();
			childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggCustVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.HEADITEMVO, CustRateVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.PKFIELD, CustRateVO.PK_CUSTOMER);
			childVO3.setAttributeValue(VOTableVO.ITEMCODE, "ts_cust_rate");
			childVO3.setAttributeValue(VOTableVO.VOTABLE, "ts_cust_rate");

			CircularlyAccessibleValueObject[] childrenVO = { childVO, childVO1, childVO2, childVO3 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("pk_city")) {
					fieldVO.setUserdefine3("area_level=5");
				}
			} else if(fieldVO.getPos().intValue() == UiConstants.POS[1]
					&& fieldVO.getTable_code().equals(ExAggCustVO.TS_CUST_ADDR)) {
				if(fieldVO.getItemkey().equals("city_name")) {
					// 注意，表体的参照是设置到name字段上
					fieldVO.setUserdefine3("area_level=5");
				} else if(fieldVO.getItemkey().equals("addrcode") || fieldVO.getItemkey().equals("addrname")) {
					fieldVO.setUserdefine1("afterEditAddrcodeOrAddrname(record)");
				}
			}
		}
		return templetVO;
	}
//这个方法用于处理当客户绑定地址发生该表时，同步修改地址信息，但是现在不需要使用这个功能。
//	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
//		super.processBeforeSave(billVO, paramVO);
//		logger.info("执行客户档案的保存...");
//		// 多表体，实际上就是多表体
//		ExAggCustVO aggVO = (ExAggCustVO) billVO;
//		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO("ts_cust_addr");
//		List<AddressVO> addrVOs = new ArrayList<AddressVO>();
//		for(CircularlyAccessibleValueObject cvo : cvos) {
//			CustAddrVO custAddrVO = (CustAddrVO) cvo;
//			if(custAddrVO.getStatus() == VOStatus.UNCHANGED) {
//				// 如果数据没有发生改变，不需要做处理
//				continue;
//			}
//			if(StringUtils.isBlank(custAddrVO.getPk_address())) {
//				AddressVO addressVO = new AddressVO();// 创建一个地址VO，需要保存到地址表中
//				// 直接录入的地址
//				if(custAddrVO.getAddrcode() == null || StringUtils.isBlank(custAddrVO.getAddrcode())) {
//					// 没有录入编码,使用自动生成的序列号
//					addressVO.setAddr_code(CodenoHelper.generateCode(paramVO.getFunCode()));
//				} else {
//					addressVO.setAddr_code(custAddrVO.getAddrcode());
//				}
//				addressVO.setAddr_name(custAddrVO.getAddrname());
//				addressVO.setAddr_type(custAddrVO.getAddr_type());
//				addressVO.setDetail_addr(custAddrVO.getDetail_addr());
//				addressVO.setContact(custAddrVO.getContact());
//				addressVO.setContact_post(custAddrVO.getContact_post());
//				addressVO.setPhone(custAddrVO.getPhone());
//				addressVO.setMobile(custAddrVO.getMobile());
//				addressVO.setEmail(custAddrVO.getEmail());
//				addressVO.setFax(custAddrVO.getFax());
//				addressVO.setPk_city(custAddrVO.getPk_city());
//				addressVO.setPk_province(custAddrVO.getPk_province());
//				addressVO.setPk_area(custAddrVO.getPk_area());
//				addressVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
//				addressVO.setLocked_flag(new UFBoolean(false));
//				addressVO.setStatus(VOStatus.NEW);
//				NWDao.setUuidPrimaryKey(addressVO);
//				addrVOs.add(addressVO);
//				custAddrVO.setPk_address(addressVO.getPk_address());
//			} else {
//				// 已经存在地址列，更新地址列
//				AddressVO originalVO = dao
//						.queryByCondition(AddressVO.class, "pk_address=?", custAddrVO.getPk_address());
//				if(originalVO == null) {
//					// 该地址已经被删除了
//					continue;
//				}
//				originalVO.setAddr_name(custAddrVO.getAddrname());
//				originalVO.setAddr_type(custAddrVO.getAddr_type());
//				originalVO.setDetail_addr(custAddrVO.getDetail_addr());
//				originalVO.setContact(custAddrVO.getContact());
//				originalVO.setContact_post(custAddrVO.getContact_post());
//				originalVO.setPhone(custAddrVO.getPhone());
//				originalVO.setMobile(custAddrVO.getMobile());
//				originalVO.setEmail(custAddrVO.getEmail());
//				originalVO.setFax(custAddrVO.getFax());
//				originalVO.setPk_city(custAddrVO.getPk_city());
//				originalVO.setPk_province(custAddrVO.getPk_province());
//				originalVO.setPk_area(custAddrVO.getPk_area());
//				originalVO.setStatus(VOStatus.UPDATED);
//				addrVOs.add(originalVO);
//			}
//		}
//		dao.saveOrUpdate(addrVOs.toArray(new AddressVO[addrVOs.size()]));
//	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ExAggCustVO aggVO = (ExAggCustVO) billVO;
		CustOpProjectBVO[] custOpProjectBVOs = (CustOpProjectBVO[]) aggVO.getTableVO(ExAggCustVO.TS_CUST_OP_PROJECT_B);
		if(custOpProjectBVOs != null && custOpProjectBVOs.length > 0){
			List<String> pk_ops = new ArrayList<String>();
			for(CustOpProjectBVO custOpProjectBVO : custOpProjectBVOs){
				if(pk_ops.contains(custOpProjectBVO.getPk_op_project())){
					throw new BusiException("利润分享不允许重复！");
				}else{
					pk_ops.add(custOpProjectBVO.getPk_op_project());
				}
			}
		}
		
	}
	
	protected void processAfterSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processAfterSave(billVO, paramVO);
		ExAggCustVO aggVO = (ExAggCustVO) billVO;
		// 2015-03-15如果没有结算客户，那么将自己作为默认的结算客户
		CircularlyAccessibleValueObject[] cvos = aggVO.getTableVO("ts_cust_bala");
		if(cvos == null || cvos.length == 0) {
			CustomerVO custVO = (CustomerVO) aggVO.getParentVO();
			CustBalaVO cbVO = new CustBalaVO();
			cbVO.setPk_customer(custVO.getPk_customer());// 放到afterSave中执行，否则这个pk_customer还没有值
			cbVO.setPk_related_cust(custVO.getPk_customer());
			cbVO.setStatus(VOStatus.NEW);
			cbVO.setIs_default(UFBoolean.TRUE);
			NWDao.setUuidPrimaryKey(cbVO);
			NWDao.getInstance().saveOrUpdate(cbVO);
		}
	}

	public String getCodeFieldCode() {
		return "cust_code";
	}

	public CustomerVO getByCode(String code) {
		return dao.queryByCondition(CustomerVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and cust_code=?",
				code);
	}

	public String getCheckHead(String pk_customer) {
		CustomerVO vo = dao.queryByCondition(CustomerVO.class, "pk_customer=?", pk_customer);
		if(vo == null) {
			logger.warn("客户档案已经被删除，pk_customer:" + pk_customer);
			return null;
		}
		return vo.getBilling_payable();
	}

	/**
	 * 这个方法在查询列表数据时，多次被调用，可以使用自定义缓存
	 */
	public String getNameString(String pk_customer) {
		if(StringUtils.isBlank(pk_customer)) {
			return null;
		}
		String[] pks = pk_customer.split("\\" + Constants.SPLIT_CHAR);
		String condString = NWUtils.buildConditionString(pks);
		List<String> retList = NWDao.getInstance().queryForListWithCache(
				"select cust_name from ts_customer WITH(NOLOCK) where pk_customer in " + condString, String.class);
		if(retList != null && retList.size() > 0) {
			StringBuffer nameBuf = new StringBuffer();
			for(String name : retList) {
				nameBuf.append(name);
				nameBuf.append(Constants.SPLIT_CHAR);
			}
			return nameBuf.substring(0, nameBuf.length() - 1);
		}
		return null;
	}

	
	public String getDefaultCheckType(String bala_customer) {
		CustomerVO vo = dao.queryByCondition(CustomerVO.class, "pk_customer=?", bala_customer);
		if(vo == null) {
			logger.warn("结算客户已经被删除，bala_customer:" + bala_customer);
			return null;
		}
		String defaultCheckType = vo.getPk_billing_type();
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b WITH(NOLOCK) "
				+ "LEFT JOIN nw_data_dict WITH(NOLOCK) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='billing_type'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,defaultCheckType);
	}

	public String getDefaultCheckCorp(String bala_customer) {
		CustomerVO vo = dao.queryByCondition(CustomerVO.class, "pk_customer=?", bala_customer);
		if(vo == null) {
			logger.warn("结算客户已经被删除，bala_customer:" + bala_customer);
			return null;
		}
		String defaultCheckType = vo.getPk_billing_type();
		String sql = "SELECT nw_data_dict_b.display_name FROM nw_data_dict_b WITH(NOLOCK) "
				+ "LEFT JOIN nw_data_dict WITH(NOLOCK) ON nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ "WHERE  isnull(nw_data_dict.dr,0)=0 AND nw_data_dict.datatype_code='check_company'  AND nw_data_dict_b.value=? ";
		return NWDao.getInstance().queryForObject(sql, String.class,defaultCheckType);
	}

	public UFDouble getFeeRate(String pk_customer,String pk_trans_type,
			String start_area,String end_area) {
		UFDouble rate = UFDouble.ONE_DBL;
		if(StringUtils.isBlank(pk_trans_type)){
			return rate;
		}
		if(StringUtils.isBlank(pk_customer)){
			TransTypeVO transTypeVO = dao.queryByCondition(TransTypeVO.class, "pk_trans_type=?", pk_trans_type);
			return transTypeVO == null ? rate : (transTypeVO.getRate() == null ? rate : transTypeVO.getRate());
		}
		List<AreaVO> startAreaVOs = AreaHelper.getCurrentAreaVOWithParents(start_area);
		String startAreas = "";
		if(startAreaVOs == null || startAreaVOs.size() == 0){
			startAreas = "('')";
		}else{
			for(AreaVO areaVO : startAreaVOs) {
				startAreas += "'";
				startAreas += areaVO.getPk_area();
				startAreas += "',";
			}
			if(startAreas.length() > 0) {
				startAreas = "(" + startAreas.substring(0, startAreas.length() - 1) + ")";
			}
		}
		List<AreaVO> endAreaVOs = AreaHelper.getCurrentAreaVOWithParents(end_area);
		String endAreas = "";
		if(endAreaVOs == null || endAreaVOs.size() == 0){
			endAreas = "('')";
		}else{
			for(AreaVO areaVO : endAreaVOs) {
				endAreas += "'";
				endAreas += areaVO.getPk_area();
				endAreas += "',";
			}
			if(endAreas.length() > 0) {
				endAreas = "(" + endAreas.substring(0, endAreas.length() - 1) + ")";
			}
		}

		String sql = "SELECT * FROM ts_cust_rate WITH(NOLOCK) "
				+ " WHERE isnull(dr,0)=0 AND pk_customer = ? AND pk_trans_type = ? "
				+ " AND start_area IN " + startAreas+ " AND end_area IN " + endAreas;
		List<CustRateVO> rateVOs = dao.queryForList(sql, CustRateVO.class, pk_customer,pk_trans_type);
		if(rateVOs == null || rateVOs.size() == 0){
			TransTypeVO transTypeVO = dao.queryByCondition(TransTypeVO.class, "pk_trans_type=?", pk_trans_type);
			return transTypeVO == null ? rate : (transTypeVO.getRate() == null ? rate : transTypeVO.getRate());
		}
		//1，找出起始区域等级最高的rate (其实区域可能为空哦)
		AreaVO hightStratArea = null;
		if(startAreaVOs != null && startAreaVOs.size() >= 0){
			for(AreaVO startArea : startAreaVOs){
				if(hightStratArea == null){
					hightStratArea = startArea;
				}else{
					if(startArea.getArea_level() >= hightStratArea.getArea_level()){
						hightStratArea = startArea;
					}
				}
			}
		}
		//取出对应的rate
		List<CustRateVO> stratRates = new ArrayList<CustRateVO>();
		for(CustRateVO rateVO : rateVOs){
			if(hightStratArea == null){
				if(StringUtils.isBlank(rateVO.getStart_area())){
					stratRates.add(rateVO);
				}
			}else{
				if(hightStratArea.getPk_area().equals(rateVO.getStart_area())){
					stratRates.add(rateVO);
				}
			}
		}
		if(stratRates.size() == 1){
			return stratRates.get(0).getRate() == null ? UFDouble.ONE_DBL : stratRates.get(0).getRate();
		}
		//2，找到目的区域，等级最高的rate
		AreaVO hightEndArea = null;
		if(endAreaVOs != null && endAreaVOs.size() >= 0){
			for(AreaVO endArea : endAreaVOs){
				if(hightEndArea == null){
					hightEndArea = endArea;
				}else{
					if(endArea.getArea_level() >= hightEndArea.getArea_level()){
						hightEndArea = endArea;
					}
				}
			}
		}
		for(CustRateVO rateVO : stratRates){
			if(hightEndArea == null){
				if(StringUtils.isBlank(rateVO.getEnd_area())){
					return rateVO.getRate() == null ? UFDouble.ONE_DBL : rateVO.getRate();
				}
			}else{
				if(hightEndArea.getPk_area().equals(rateVO.getEnd_area())){
					return rateVO.getRate() == null ? UFDouble.ONE_DBL : rateVO.getRate();
				}
			}
		}
		return rate;
	}
}
