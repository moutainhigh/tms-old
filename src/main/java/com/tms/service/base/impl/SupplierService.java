package com.tms.service.base.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.CodenoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.vo.base.AddressVO;
import com.tms.vo.base.SuppAddrVO;
import com.tms.vo.base.SupplierVO;

/**
 * 供应商
 * 
 * @author xuqc
 * @date 2013-12-29 上午11:04:26
 */
@Service
public class SupplierService extends AbsToftServiceImpl {

	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, SupplierVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, SupplierVO.PK_SUPPLIER);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, SuppAddrVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, SuppAddrVO.PK_SUPPLIER);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_supp_addr");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_supp_addr");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
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
			}
		}
		return templetVO;
	}

	public String getCodeFieldCode() {
		return "supp_code";
	}

	public SupplierVO getByCode(String code) {
		return dao.queryByCondition(SupplierVO.class, "isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and supp_code=?",
				code);
	}

	public String getCheckHead(String pk_supplier) {
		SupplierVO vo = dao.queryByCondition(SupplierVO.class, "pk_supplier=?", pk_supplier);
		if(vo == null) {
			logger.warn("供应商档案已经被删除，pk_supplier:" + pk_supplier);
			return null;
		}
		return vo.getBilling_payable();
	}

	public String getNameString(String pk_supplier) {
		if(StringUtils.isBlank(pk_supplier)) {
			return null;
		}
		String[] pks = pk_supplier.split("\\" + Constants.SPLIT_CHAR);
		String condString = NWUtils.buildConditionString(pks);
		List<String> retList = NWDao.getInstance().queryForList(
				"select supp_name from ts_supplier where pk_supplier in " + condString, String.class);
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

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		logger.info("执行客户档案的保存...");
		// 多表体，实际上就是多表体
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		List<AddressVO> addrVOs = new ArrayList<AddressVO>();
		for(CircularlyAccessibleValueObject cvo : cvos) {
			SuppAddrVO suppAddrVO = (SuppAddrVO) cvo;
			if(suppAddrVO.getStatus() == VOStatus.UNCHANGED) {
				// 如果数据没有发生改变，不需要做处理
				continue;
			}
			if(StringUtils.isBlank(suppAddrVO.getPk_address())) {
				AddressVO addressVO = new AddressVO();// 创建一个地址VO，需要保存到地址表中
				// 直接录入的地址
				if(suppAddrVO.getAddrcode() == null || StringUtils.isBlank(suppAddrVO.getAddrcode())) {
					// 没有录入编码,使用自动生成的序列号
					addressVO.setAddr_code(CodenoHelper.generateCode(paramVO.getFunCode()));
				} else {
					addressVO.setAddr_code(suppAddrVO.getAddrcode());
				}
				addressVO.setAddr_name(suppAddrVO.getAddrname());
				addressVO.setAddr_type(suppAddrVO.getAddr_type());
				addressVO.setDetail_addr(suppAddrVO.getDetail_addr());
				addressVO.setContact(suppAddrVO.getContact());
				addressVO.setContact_post(suppAddrVO.getContact_post());
				addressVO.setPhone(suppAddrVO.getPhone());
				addressVO.setMobile(suppAddrVO.getMobile());
				addressVO.setEmail(suppAddrVO.getEmail());
				addressVO.setFax(suppAddrVO.getFax());
				addressVO.setPk_city(suppAddrVO.getPk_city());
				addressVO.setPk_province(suppAddrVO.getPk_province());
				addressVO.setPk_area(suppAddrVO.getPk_area());
				addressVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				addressVO.setLocked_flag(new UFBoolean(false));
				addressVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(addressVO);
				addrVOs.add(addressVO);
				suppAddrVO.setPk_address(addressVO.getPk_address());
			} else {
				// 已经存在地址列，更新地址列
				AddressVO originalVO = dao
						.queryByCondition(AddressVO.class, "pk_address=?", suppAddrVO.getPk_address());
				if(originalVO == null) {
					// 该地址已经被删除了
					continue;
				}
				originalVO.setAddr_name(suppAddrVO.getAddrname());
				originalVO.setAddr_type(suppAddrVO.getAddr_type());
				originalVO.setDetail_addr(suppAddrVO.getDetail_addr());
				originalVO.setContact(suppAddrVO.getContact());
				originalVO.setContact_post(suppAddrVO.getContact_post());
				originalVO.setPhone(suppAddrVO.getPhone());
				originalVO.setMobile(suppAddrVO.getMobile());
				originalVO.setEmail(suppAddrVO.getEmail());
				originalVO.setFax(suppAddrVO.getFax());
				originalVO.setPk_city(suppAddrVO.getPk_city());
				originalVO.setPk_province(suppAddrVO.getPk_province());
				originalVO.setPk_area(suppAddrVO.getPk_area());
				originalVO.setStatus(VOStatus.UPDATED);
				addrVOs.add(originalVO);
			}
		}
		dao.saveOrUpdate(addrVOs.toArray(new AddressVO[addrVOs.size()]));
	}
}
