package com.tms.service.inv.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.ext.ref.userdefine.IMultiSelect;
import org.nw.jf.utils.DataTypeConverter;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.redis.RedisDao;
import org.nw.utils.NWUtils;
import org.nw.utils.RefUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.constants.FunConst;
import com.tms.constants.RevTypeAndStatus;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.inv.InvoiceRevService;
import com.tms.service.inv.InvoiceService;
import com.tms.vo.inv.ExAggInvoiceRevVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvPackRevBVO;
import com.tms.vo.inv.InvRevBVO;
import com.tms.vo.inv.InvoiceVO;

@Service
public class InvoiceRevServiceImpl extends TMSAbsBillServiceImpl implements InvoiceRevService {

	@Autowired
	private InvoiceService invoiceService;
	
	public String getBillType() {
		return null;
	}
	
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggInvoiceRevVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceRevVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InvoiceVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InvoiceVO.PK_INVOICE);
			billInfo.setParentVO(vo);

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceRevVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, InvRevBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, InvRevBVO.PK_INVOICE);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_revise_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_revise_b");
			
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceRevVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, InvPackBVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, InvPackBVO.PK_INVOICE);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_pack_b");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_pack_b");
			
			VOTableVO childVO3 = new VOTableVO();
			childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggInvoiceRevVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.HEADITEMVO, InvPackRevBVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.PKFIELD, InvPackRevBVO.PK_INVOICE);
			childVO3.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_pack_rev_b");
			childVO3.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_pack_rev_b");
			
			
			
			CircularlyAccessibleValueObject[] childrenVO = {childVO1,childVO2,childVO3};
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("bala_customer")) {
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
					fieldVO.setUserdefine1("refreshReceDetail()");
				} else if (fieldVO.getItemkey().equals("pk_delivery")) {
					// 提货方
					fieldVO.setUserdefine1("afterChangePk_delivery();getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				} else if (fieldVO.getItemkey().equals("pk_arrival")) {
					// 收货方
					fieldVO.setUserdefine1("afterChangePk_arrival();getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("pk_customer:${Ext.getCmp('pk_customer').getValue()}");
				} else if (fieldVO.getItemkey().equals("deli_city")) {
					fieldVO.setUserdefine1("getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("area_level=5");
				} else if (fieldVO.getItemkey().equals("arri_city")) {
					fieldVO.setUserdefine1("getMileageAndDistance();refreshReceDetail()");
					fieldVO.setUserdefine3("larea_level=5");
				} else if (fieldVO.getItemkey().equals("pk_trans_type")) {
					// 运输方式，需要重新计算总计费重
					fieldVO.setUserdefine1("updateHeaderFeeWeightCount();refreshReceDetail()");
				} else if (fieldVO.getItemkey().equals("deli_contact")) {
					// 提货联系人，参照地址档案联系人，ts_addr_contact
					fieldVO.setUserdefine3("pk_address:${Ext.getCmp('pk_delivery').getValue()}");
				} else if (fieldVO.getItemkey().equals("arri_contact")) {
					// 收货联系人，参照地址档案联系人，ts_addr_contact
					fieldVO.setUserdefine3("pk_address:${Ext.getCmp('pk_arrival').getValue()}");
				} else if (fieldVO.getItemkey().equals("req_deli_date")) {
					// 要求提货日期
					fieldVO.setUserdefine1("afterChangeReq_deli_date(field,value,originalValue)");
					fieldVO.setBeforeRenderer("req_deli_dateBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("req_arri_date")) {
					// 要求收货日期
					fieldVO.setUserdefine1("afterChangeReq_arri_date(field,value,originalValue)");
					fieldVO.setBeforeRenderer("req_arri_dateBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("exp_flag")) {
					// 跟踪信息是否异常
					fieldVO.setBeforeRenderer("exp_flagBeforeRenderer");
				} else if (fieldVO.getItemkey().equals("balatype")) {
					// 结算方式
					fieldVO.setUserdefine1("afterChageBalatype(field,value,originalValue)");
				}else if(fieldVO.getItemkey().equals("urgent_level")){
					fieldVO.setBeforeRenderer("urgent_levelBeforeRenderer");
				}
			} else if (fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if (fieldVO.getTable_code().equals(TabcodeConst.TS_INV_PACK_B)) {
					if (fieldVO.getItemkey().equals("num")) { // 更新件数时，更新其他信息，及表头统计信息
						fieldVO.setUserdefine1("afterEditNumOfPack(record)");
					}
					if (fieldVO.getItemkey().equals("pack_num_count")) { // 更新数量
						fieldVO.setUserdefine1("afterEditPackNumCount(record)");
					} else if (fieldVO.getItemkey().equals("pack_name")) {
						// 编辑“包装”时，如果货品匹配到，则带出包装的信息
						fieldVO.setUserdefine1("afterEditPack(record)");
					} else if (fieldVO.getItemkey().equals("goods_code")) {
						// 编辑“货品”时，需要将货品的条件加入包装的参照中
						fieldVO.setUserdefine1("afterEditGoodsCode(record)");
					} else if (fieldVO.getItemkey().equals("length") || fieldVO.getItemkey().equals("width")
							|| fieldVO.getItemkey().equals("height")) {
						fieldVO.setUserdefine1("afterEditLengthOrWidthOrHeight(record)");
					} else if (fieldVO.getItemkey().equals("volume")) {
						// 编辑体积
						fieldVO.setUserdefine1("afterEditVolume(record)");
					} else if (fieldVO.getItemkey().equals("weight")) {
						// 编辑重量
						fieldVO.setUserdefine1("afterEditWeight(record)");
					} else if (fieldVO.getItemkey().equals("unit_volume")) {
						// 编辑单位体积
						fieldVO.setUserdefine1("afterEditUnit_volume(record)");
					} else if (fieldVO.getItemkey().equals("unit_weight")) {
						// 编辑单位重量
						fieldVO.setUserdefine1("afterEditUnit_weight(record)");
					} else if (fieldVO.getItemkey().equals("goods_type")) {
						fieldVO.setUsereditflag(Constants.YES);
					}
				}else if(fieldVO.getTable_code().equals(TabcodeConst.TS_INV_REV_B)){
					if (fieldVO.getItemkey().equals("unconfirm_reason")) {//填写
						fieldVO.setUserdefine1("afterChangeRevUnconfirm_reason(record)");
					}
				}else if(fieldVO.getTable_code().equals(TabcodeConst.TS_INV_PACK_REV_B)){
					if (fieldVO.getItemkey().equals("unconfirm_reason")) {//填写
						fieldVO.setUserdefine1("afterChangePackRevUnconfirm_reason(record)");
					}
				}
			}
		}
		return templetVO;
	}

	
	/**
	 * 重写保存方法，这里保存时不将数据保存到数据库，而是将修改指令保存到指令表里
	 * @author XIA
	 */
	@Override
	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO) {
		if(billVO == null || paramVO == null){
			return null;
		}
		ExAggInvoiceRevVO aggInvoiceRevVO = (ExAggInvoiceRevVO) billVO;
		//获取单据模板 这里默认有登陆用户
		UiBillTempletVO templetVO = this.getBillTempletVOByFunCode(paramVO);
		if(templetVO == null){
			return null ;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		
		InvoiceVO invoiceVO = (InvoiceVO) billVO.getParentVO();
		paramVO.setBillId(invoiceVO.getPrimaryKey());
		//查出数据库原有的billVO
		AggregatedValueObject oldBillVO = this.queryBillVO(paramVO);
		ExAggInvoiceRevVO oldAggInvoiceRevVO = (ExAggInvoiceRevVO) oldBillVO;
		InvoiceVO oldInvoiceVO = (InvoiceVO) oldBillVO.getParentVO();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<InvRevBVO> addRevBVOs = new ArrayList<InvRevBVO>();
		List<InvPackRevBVO> addPackRevBVOs = new ArrayList<InvPackRevBVO>();
		addRevBVOs.addAll(Arrays.asList((InvRevBVO[]) oldAggInvoiceRevVO.getTableVO(TabcodeConst.TS_INV_REV_B)));
		addPackRevBVOs.addAll(Arrays.asList((InvPackRevBVO[]) oldAggInvoiceRevVO.getTableVO(TabcodeConst.TS_INV_PACK_REV_B)));
		//检查主表部分字段的修改情况
		for(String attr : invoiceVO.getAttributeNames()){
			if( (   //1,原有的值和新的值都不为空，但是两者不相等。
					(invoiceVO.getAttributeValue(attr) != null && StringUtils.isNotBlanks(invoiceVO.getAttributeValue(attr).toString()))
						&& (oldInvoiceVO.getAttributeValue(attr) != null && StringUtils.isNotBlanks(oldInvoiceVO.getAttributeValue(attr).toString()))
						&& !oldInvoiceVO.getAttributeValue(attr).toString().equals(invoiceVO.getAttributeValue(attr).toString())
							)||
					//1,新的值为空，原有值不为空
					((invoiceVO.getAttributeValue(attr) == null || StringUtils.isBlank(invoiceVO.getAttributeValue(attr).toString()))
							&&(oldInvoiceVO.getAttributeValue(attr) != null && StringUtils.isNotBlanks(oldInvoiceVO.getAttributeValue(attr).toString()))
							)||
					//1,新的值不为空，原有值为空
					((invoiceVO.getAttributeValue(attr) != null && StringUtils.isNotBlanks(invoiceVO.getAttributeValue(attr).toString()))
							&&(oldInvoiceVO.getAttributeValue(attr) == null || StringUtils.isBlank(oldInvoiceVO.getAttributeValue(attr).toString()))
							)
					){
				//符合条件，判断字段是否加入审计
				for (BillTempletBVO fieldVO : fieldVOs){
					if(fieldVO.getPos().intValue() == UiConstants.POS[0]
							&& fieldVO.getItemkey().equals(attr)
							&& fieldVO.getIf_revise() != null
							&& fieldVO.getIf_revise().equals(UFBoolean.TRUE)){
						InvRevBVO revBVO = new InvRevBVO();
						revBVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(revBVO);
						revBVO.setPk_invoice(invoiceVO.getPrimaryKey());
						revBVO.setRevise_status(RevTypeAndStatus.REV_STATUS_NEW);
						revBVO.setRevise_type(RevTypeAndStatus.REV_TYPE_UPDATE);
						revBVO.setRevise_item(invoiceVO.getTableName()+"."+attr);
						revBVO.setRevise_old(String.valueOf(oldInvoiceVO.getAttributeValue(attr)));
						revBVO.setRevise_new(String.valueOf(invoiceVO.getAttributeValue(attr)));
						revBVO.setItem_reftype(fieldVO.getReftype());
						revBVO.setItem_datatype(fieldVO.getDatatype());
						revBVO.setRevise_user(WebUtils.getLoginInfo().getPk_user());
						revBVO.setRevise_time(new UFDateTime(new Date()));
						toBeUpdate.add(revBVO);
						addRevBVOs.add(revBVO);
					}
				}
			}
		}
		//处理子表信息
		InvPackBVO[] packBVOs = (InvPackBVO[]) aggInvoiceRevVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		//如果packBVOs为空说明不需要进行任何处理
		if(packBVOs != null && packBVOs.length > 0){
			//这里存在4种状态
			// new 新建
			// update 修改
			// delete 删除
			// unchanged 未改变
			for(InvPackBVO packBVO : packBVOs){
				if(packBVO.getStatus() == VOStatus.NEW){
					InvPackRevBVO invPackRevBVO = new InvPackRevBVO();
					invPackRevBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invPackRevBVO);
					NWDao.setUuidPrimaryKey(packBVO);
					invPackRevBVO.setPk_invoice(invoiceVO.getPrimaryKey());
					invPackRevBVO.setRevise_status(RevTypeAndStatus.REV_STATUS_NEW);
					invPackRevBVO.setRevise_type(RevTypeAndStatus.REV_TYPE_NEW);
					invPackRevBVO.setRevise_user(WebUtils.getLoginInfo().getPk_user());
					invPackRevBVO.setRevise_time(new UFDateTime(new Date()));
					for(String attr : packBVO.getAttributeNames()){
						invPackRevBVO.setAttributeValue(attr, packBVO.getAttributeValue(attr));
					}
					invPackRevBVO.setPk_invoice(invoiceVO.getPk_invoice());
					toBeUpdate.add(invPackRevBVO);
					addPackRevBVOs.add(invPackRevBVO);
				}else if(packBVO.getStatus() == VOStatus.DELETED){
					InvPackRevBVO invPackRevBVO = new InvPackRevBVO();
					invPackRevBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invPackRevBVO);
					invPackRevBVO.setPk_invoice(invoiceVO.getPrimaryKey());
					invPackRevBVO.setRevise_status(RevTypeAndStatus.REV_STATUS_NEW);
					invPackRevBVO.setRevise_type(RevTypeAndStatus.REV_TYPE_DELETE);
					invPackRevBVO.setRevise_user(WebUtils.getLoginInfo().getPk_user());
					invPackRevBVO.setRevise_time(new UFDateTime(new Date()));
					//当操作者先修改后删除数据的时候，会出现bug
					for(String attr : packBVO.getAttributeNames()){
						invPackRevBVO.setAttributeValue(attr, packBVO.getAttributeValue(attr));
					}
					toBeUpdate.add(invPackRevBVO);
					addPackRevBVOs.add(invPackRevBVO);
				}else if(packBVO.getStatus() == VOStatus.UPDATED){
					InvPackRevBVO invPackRevBVO = new InvPackRevBVO();
					invPackRevBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(invPackRevBVO);
					invPackRevBVO.setPk_invoice(invoiceVO.getPrimaryKey());
					invPackRevBVO.setRevise_status(RevTypeAndStatus.REV_STATUS_NEW);
					invPackRevBVO.setRevise_type(RevTypeAndStatus.REV_TYPE_UPDATE);
					invPackRevBVO.setRevise_user(WebUtils.getLoginInfo().getPk_user());
					invPackRevBVO.setRevise_time(new UFDateTime(new Date()));
					//当操作者先修改后删除数据的时候，会出现bug
					for(String attr : packBVO.getAttributeNames()){
						invPackRevBVO.setAttributeValue(attr, packBVO.getAttributeValue(attr));
					}
					toBeUpdate.add(invPackRevBVO);
					addPackRevBVOs.add(invPackRevBVO);
				}
			}
		}
		
		aggInvoiceRevVO.setTableVO(TabcodeConst.TS_INV_REV_B, addRevBVOs.toArray(new InvRevBVO[addRevBVOs.size()]));
		aggInvoiceRevVO.setTableVO(TabcodeConst.TS_INV_PACK_REV_B, addPackRevBVOs.toArray(new InvPackRevBVO[addPackRevBVOs.size()]));
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return billVO;
	}

	protected void processInExecFormula(ParamVO paramVO,String bodyTabCode, List<Map<String, Object>> bodyList) {
		if(!bodyTabCode.equals(TabcodeConst.TS_INV_REV_B)
				|| bodyList == null
				|| bodyList.size() == 0){
			return;
		}
		UiBillTempletVO templetVO = this.getBillTempletVOByFunCode(paramVO);
		if(templetVO == null){
			return;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		
		for(Map<String, Object> map : bodyList){
			Object old_realValue = getRealValueByRef(String.valueOf(map.get("revise_old")), 
					String.valueOf(map.get("item_reftype")),
					Integer.parseInt(String.valueOf(map.get("item_datatype"))), new InvRevBVO());
			map.put("revise_old", old_realValue);
			Object new_realValue = getRealValueByRef(String.valueOf(map.get("revise_new")), 
					String.valueOf(map.get("item_reftype")),
					Integer.parseInt(String.valueOf(map.get("item_datatype"))), new InvRevBVO());
			map.put("revise_new", new_realValue);
			String[] item = map.get("revise_item").toString().split("\\.");
			for (BillTempletBVO fieldVO : fieldVOs){
				if(fieldVO.getTable_code().equals(item[0])
						&& fieldVO.getItemkey().equals(item[1])){
					map.put("revise_item",fieldVO.getDefaultshowname());
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected Object getRealValueByRef(String value, String item_reftype, Integer item_datatype, SuperVO superVO) {
		if(StringUtils.isBlank(value)) {
			return null;
		}
		Object realValue = value;
		try {
			if(DATATYPE.SELECT.equals(item_datatype)) {
				// 下拉
				realValue = getSelectValue(value, item_reftype);
			} else if(DATATYPE.REF.equals(item_datatype)) {
				// 参照
				String refclass = DataTypeConverter.getRefClazz(item_reftype, item_datatype);
				BaseRefModel refModel = null;
				Class<?> clazz = Class.forName(refclass);
				Object obj = clazz.newInstance();
				if(obj instanceof IMultiSelect) {
					// 如果是多选类型
					IMultiSelect multiSelect = (IMultiSelect) obj;
					String consult_code = multiSelect.getConsult_code();
					if(StringUtils.isNotBlank(consult_code)) {
						if(value.startsWith("[") && value.endsWith("]")) {
							value = value.substring(1, value.length() - 1);
						}
						String[] valueAry = value.split(",");
						if(valueAry.length > 0) {
							StringBuffer buf = new StringBuffer();
							buf.append("[");
							for(int i = 0; i < valueAry.length; i++) {
								buf.append(UiTempletUtils.getSelectValue(valueAry[i], consult_code));
								if(i != valueAry.length - 1) {
									buf.append("]");
								}
							}
							realValue = buf.toString();
						}
					}
				} else {
					// 参照
					refModel = (BaseRefModel) obj;
					// 这里会根据code以及name去查询,如果返回多个值，不处理
					Map<String, Object> dataMap = refModel.getByPk(value);
					Object dataObj = dataMap.get("data");
					if(dataObj == null) {
						return null;
					}
					if(dataObj instanceof List) {// 如果返回多条记录，这里只取第一条
						List list = (List) dataObj;
						dataObj = list.get(0);
					}
					if(dataObj instanceof SuperVO) {
						SuperVO vo = (SuperVO) dataObj;
						realValue = RefUtils.convert(refModel, vo).getName();// 此时的realValue是一个refVO
					} else {
						Map map = (Map) dataObj;
						realValue = RefUtils.convert(refModel, map).getName();// 此时的realValue是一个refVO
					}
				}
			}
		} catch(Exception e) {
			String error = "值：" + value + "，错误信息：" + e.getMessage();
			logger.error(error);
			throw new BusiException(error);
		}
		return realValue;
	}

	public static Object getSelectValue(String text, String reftype) {
		//List<String[]> list = UiTempletUtils.getSelectValues(reftype); // 这里使用一个没有实际意义的值-1
		List<String[]> list =  RedisDao.getInstance().getSelectValues(reftype);
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				String[] arr = list.get(i); // 这里的arr的长度肯定是2,第一个值是text，第二个值是value
				if(text.trim().equals(arr[1].trim())) {
					String retStr = arr[0];
					return retStr;
				}
			}
		}
		return null;
	}
	
	public Map<String, Object> examine(InvRevBVO[] newInvRevBVOs, InvPackRevBVO[] newInvPackRevBVOs) {
		InvRevBVO[] invRevBVOs = null;
		InvPackRevBVO[] invPackRevBVOs = null;
		if(newInvRevBVOs != null && newInvRevBVOs.length > 0){
			String[] pks = new String[newInvRevBVOs.length];
			for(int i=0;i<newInvRevBVOs.length;i++){
				pks[i] = newInvRevBVOs[i].getPk_inv_revise_b();
			}
			invRevBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvRevBVO.class,
					"pk_inv_revise_b in" + NWUtils.buildConditionString(pks));
			for(InvRevBVO newInvRevBVO : newInvRevBVOs){
				for(InvRevBVO invRevBVO : invRevBVOs){
					if(invRevBVO.getPk_inv_revise_b().equals(newInvRevBVO.getPk_inv_revise_b())){
						invRevBVO.setRevise_status(newInvRevBVO.getRevise_status());
						invRevBVO.setUnconfirm_reason(newInvRevBVO.getUnconfirm_reason());
					}
				}
			}
			
			
		}
		if(newInvPackRevBVOs != null && newInvPackRevBVOs.length > 0){
			String[] pks = new String[newInvPackRevBVOs.length];
			for(int i=0;i<newInvPackRevBVOs.length;i++){
				pks[i] = newInvPackRevBVOs[i].getPk_inv_pack_rev_b();
			}
			invPackRevBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvPackRevBVO.class,
					"pk_inv_pack_rev_b in" + NWUtils.buildConditionString(pks));
			for(InvPackRevBVO newInvPackRevBVO : newInvPackRevBVOs){
				for(InvPackRevBVO invPackRevBVO : invPackRevBVOs){
					if(invPackRevBVO.getPk_inv_pack_rev_b().equals(newInvPackRevBVO.getPk_inv_pack_rev_b())){
						invPackRevBVO.setRevise_status(newInvPackRevBVO.getRevise_status());
						invPackRevBVO.setUnconfirm_reason(newInvPackRevBVO.getUnconfirm_reason());
					}
				}
			}
		}
		
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		String pk_invoice = "";
		if(invRevBVOs != null && invRevBVOs.length > 0){
			pk_invoice = invRevBVOs[0].getPk_invoice();
		}else if(invPackRevBVOs != null && invPackRevBVOs.length > 0){
			pk_invoice = invPackRevBVOs[0].getPk_invoice();
		}
		
		ParamVO paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.INVOICE_CODE);
		paramVO.getAttr().put("updateAddr", false);
		paramVO.setBillId(pk_invoice);
		paramVO.setBodyPaginationMap(new HashMap<String,Boolean>());
		ExAggInvoiceVO billVO = (ExAggInvoiceVO) invoiceService.queryBillVO(paramVO);
		//修改订单头信息
		if(invRevBVOs != null && invRevBVOs.length > 0){
			InvoiceVO invoiceVO = (InvoiceVO) billVO.getParentVO();
			for(InvRevBVO invRevBVO : invRevBVOs){
				if(invRevBVO.getRevise_status() == RevTypeAndStatus.REV_STATUS_AGG){
					invoiceVO.setAttributeValue(invRevBVO.getRevise_item().split("\\.")[1], invRevBVO.getRevise_new());
				}
				invRevBVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(invRevBVO);
			}
			invoiceVO.setStatus(VOStatus.UPDATED);
		}
		InvPackBVO[] invPackBVOs = (InvPackBVO[]) billVO.getTableVO(TabcodeConst.TS_INV_PACK_B);
		//将invPackBVOs转化成List，方便修改。
		List<InvPackBVO> packBVOs = Arrays.asList(invPackBVOs);
		//修改包装信息
		if(invPackRevBVOs != null && invPackRevBVOs.length > 0){
			for(InvPackRevBVO invPackRevBVO : invPackRevBVOs){
				if(invPackRevBVO.getRevise_status() == RevTypeAndStatus.REV_STATUS_AGG){
					if(invPackRevBVO.getRevise_type() == RevTypeAndStatus.REV_TYPE_NEW){
						InvPackBVO packBVO = new InvPackBVO();
						for(String attr : packBVO.getAttributeNames()){
							packBVO.setAttributeValue(attr, invPackRevBVO.getAttributeValue(attr));
						}
						packBVO.setStatus(VOStatus.NEW);
						packBVOs.add(packBVO);
					}else if(invPackRevBVO.getRevise_type() == RevTypeAndStatus.REV_TYPE_UPDATE){
						//找到对应的那个包装明细
						for(InvPackBVO packBVO : packBVOs){
							if(packBVO.getPk_inv_pack_b().equals(invPackRevBVO.getPk_inv_pack_b())){
								for(String attr : packBVO.getAttributeNames()){
									packBVO.setAttributeValue(attr, invPackRevBVO.getAttributeValue(attr));
								}
								packBVO.setStatus(VOStatus.UPDATED);
							}
						}
					}else if(invPackRevBVO.getRevise_type() == RevTypeAndStatus.REV_TYPE_DELETE){
						//找到对应的那个包装明细
						for(InvPackBVO packBVO : packBVOs){
							if(packBVO.getPk_inv_pack_b().equals(invPackRevBVO.getPk_inv_pack_b())){
								packBVO.setStatus(VOStatus.DELETED);
							}
						}
					}
				}
				invPackRevBVO.setStatus(VOStatus.UPDATED);
				toBeUpdate.add(invPackRevBVO);
			}
		}
		billVO.setTableVO(TabcodeConst.TS_INV_PACK_B, packBVOs.toArray(new InvPackBVO[packBVOs.size()]));
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		invoiceService.save(billVO, paramVO);
		return null;
	}
	

}
