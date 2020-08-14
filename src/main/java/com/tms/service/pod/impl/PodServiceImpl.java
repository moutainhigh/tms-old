package com.tms.service.pod.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.QueryHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpAccidentConst;
import com.tms.constants.ExpAccidentConst.ExpAccidentOrgin;
import com.tms.constants.FunConst;
import com.tms.constants.TabcodeConst;
import com.tms.constants.TrackingConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.impl.ReceiveDetailServiceImpl;
import com.tms.service.inv.InvoiceService;
import com.tms.service.pod.PodService;
import com.tms.service.te.ExpAccidentService;
import com.tms.vo.attach.AttachmentVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;

/**
 * 签收回单处理
 * 
 * @author xuqc
 * @date 2013-4-16 下午03:21:54
 */
//zhuyj 对外接口依据beanName获取对象实例 
@Service("podService")
public class PodServiceImpl extends TMSAbsBillServiceImpl implements PodService {

	@Autowired
	private ExpAccidentService expAccidentService;

	@Autowired
	private InvoiceService invoiceService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PodVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PodVO.PK_INVOICE);// 注意ts_pod表是发货单表的附属表，这里使用发货单的主键
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, InvPackBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, InvPackBVO.PK_INVOICE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_inv_pack_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_inv_pack_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	/**
	 * 这里实际上使用的是发货单的单据类型
	 */
	public String getBillType() {
		return BillTypeConst.FHD;
	}

	public UiBillTempletVO getBillTempletVOByFunCode(ParamVO paramVO) {
		UiBillTempletVO templetVO = super.getBillTempletVOByFunCode(paramVO);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getItemkey().equals(InvPackBVO.POD_NUM)
						|| fieldVO.getItemkey().equals(InvPackBVO.REJECT_NUM)
						|| fieldVO.getItemkey().equals(InvPackBVO.DAMAGE_NUM)
						|| fieldVO.getItemkey().equals(InvPackBVO.LOST_NUM)) {
					fieldVO.setEditflag(1);// 设置成可编辑
				}
			}
		}
		return templetVO;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("pod_exp")) {
					fieldVO.setBeforeRenderer("pod_expBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("receipt_exp")) {
					fieldVO.setBeforeRenderer("receipt_expBeforeRenderer");
				} else if(fieldVO.getItemkey().equals("vbillno")) {
					fieldVO.setRenderer("vbillnoRenderer");
				}
			}
		}
		return templetVO;
	}

	@SuppressWarnings("rawtypes")
	protected String getCustomerOrCarrierCond(ParamVO paramVO) {
		UserVO userVO = NWDao.getInstance().queryByCondition(UserVO.class, "pk_user=?",
				WebUtils.getLoginInfo().getPk_user());
		Class clazz = ServiceHelper.getVOClass(this.getBillInfo(), paramVO);
		SuperVO superVO = null;
		try {
			superVO = (SuperVO) clazz.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(superVO != null) {
			// 2015 11 13 yaojiie 判断客户和承运商类型，根据不同的客户和承运商显示相对应的单据
			if(userVO.getUser_type().intValue() == Constants.USER_TYPE.CUSTOMER.intValue()) {
					return " pk_invoice in (select pk_invoice from ts_invoice where isnull(dr,0)=0 and pk_customer='"
							+ userVO.getPk_customer() + "')";
				}
			 	else if(userVO.getUser_type().intValue() == Constants.USER_TYPE.CARRIER.intValue()
			 			&&!paramVO.getTabCode().equals(TabcodeConst.TS_INV_PACK_B)) {
				// 如果是承运商
					return " pod_carrier in ('"+ userVO.getPk_carrier() + "')";
			}
		}
		return null;
	}

	public PodVO afterChangeInvoiceToArrival(InvoiceVO invVO, EntrustVO entVO) {
		if(invVO == null || entVO == null) {
			throw new BusiException("发货单状态更新到已到货时处理出现异常，发货单或者委托单不能为空！");
		}
		//yaojiie 2015 12 15 判断POD状态，当已存在签收单时，不进行签收。
		PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice =?",
				invVO.getPk_invoice());
		if(podVO != null){
			throw new BusiException("发货单已生成签收单！");
		}
		podVO = new PodVO();
		// 从委托单中读取的数据
		podVO.setPod_entrust_vbillno(entVO.getVbillno());// 委托单号
		//通过委托单子表获取司机车辆信息
		EntTransbilityBVO[] entTransbilityBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class, 
				"pk_entrust =? ", entVO.getPk_entrust());
		if(entTransbilityBVOs != null && entTransbilityBVOs.length > 0){
			podVO.setPod_driver(entTransbilityBVOs[0].getPk_driver());// 司机
			podVO.setPod_carno(entTransbilityBVOs[0].getCarno());//车辆
		}
		
		podVO.setPod_carrier(entVO.getPk_carrier());// 承运商
		podVO.setPod_trans_type(entVO.getPk_trans_type());// 运输方式
		// 2015 11 13 yaojiie
		//将委托单的公司放入def1字段，在查找委托公司的时候，查找def1字段
		podVO.setDef1(entVO.getPk_corp());
		// 2015 12 22 yaojiie
		podVO.setCreate_time(new UFDateTime(new Date()));
		podVO.setCreate_user(WebUtils.getLoginInfo() == null ? "32e6103e697f44b7ac98477583af49cd" : WebUtils.getLoginInfo().getPk_user());
		// 从发货单读取的数据
		podVO.setPk_invoice(invVO.getPk_invoice());
		podVO.setPk_corp(invVO.getPk_corp());
		podVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(podVO);
		dao.saveOrUpdate(podVO);
		return podVO;
	}

	public void afterChangeInvoiceToDelivery(InvoiceVO invVO, EntrustVO entVO) {
		if(invVO == null || entVO == null) {
			throw new BusiException("发货单状态从已到货更新到已提货时处理出现异常，发货单或者委托单不能为空！");
		}
		PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", invVO.getPk_invoice());
		if(podVO == null) {
			// 这里其实可能发货单进行了分量，只是部分到货而已，并没有完全到货。所以pod中还不存在记录
			return;
			// logger.error("查询签收单时出错，pk_invoice:" + invVO.getPk_invoice());
			// throw new BusiException("根据发货单查询签收单报错！");
		}
		podVO.setStatus(VOStatus.DELETED);
		dao.saveOrUpdate(podVO);
	}

	public List<Map<String, Object>> doPod(String[] pk_invoice, PodVO newPodVO, ParamVO paramVO) {
		logger.info("发货单签收...");
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("执行签收时需要选择发货单！");
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<AggregatedValueObject> billVOs = new ArrayList<AggregatedValueObject>();
		String[] fGSInvpks = getFGSPks(pk_invoice);
		if(fGSInvpks != null && fGSInvpks.length > 0){
			doPod(fGSInvpks, newPodVO, paramVO);
		}
		for(String pk : pk_invoice) {
			InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk);
			if(invVO.getVbillstatus().intValue() != BillStatus.INV_ARRIVAL) {
				throw new BusiException("只有已到货的发货单才能执行签收！");
			}
			invVO.setVbillstatus(BillStatus.INV_SIGN);// 改成已签收
			invVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(invVO);

			PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk);
			if(podVO == null) {
				throw new BusiException("发货单[?]没有对应的POD签收单！",invVO.getVbillno());
			}
			podVO.setPod_man(newPodVO.getPod_man());
			//yaojiie 2016 1 7 检查签收时间
			if(newPodVO.getPod_date().after(new UFDate())){
				throw new BusiException("签收时间晚于当前时间，请检查数据！");
			}
			podVO.setPod_date(newPodVO.getPod_date());
			podVO.setPod_memo(newPodVO.getPod_memo());
			podVO.setCurr_longitude(newPodVO.getCurr_longitude());
			podVO.setCurr_latitude(newPodVO.getCurr_latitude());
			podVO.setApp_detail_addr(newPodVO.getApp_detail_addr());
			podVO.setPod_exp(UFBoolean.FALSE);// 签收异常为否
			podVO.setPod_book_man(WebUtils.getLoginInfo().getPk_user());
			podVO.setPod_book_time(new UFDateTime(new Date()));
			
			podVO.setDervice_attitude(newPodVO.getDervice_attitude());
			podVO.setDelivery_speed(newPodVO.getDelivery_speed());
			podVO.setLogistics_services(newPodVO.getLogistics_services());
			// 总签收件数置为发货单的总件数
			// 拒收件数、损坏件数、丢失件数置为0
			podVO.setPod_num_count(invVO.getNum_count());
			podVO.setReject_num_count(0);
			podVO.setDamage_num_count(0);
			podVO.setLost_num_count(0);

			podVO.setPod_weight_count(invVO.getWeight_count());
			podVO.setPod_volume_count(invVO.getVolume_count());
			podVO.setPod_volume_weight_count(invVO.getVolume_weight_count());
			podVO.setPod_fee_weight_count(invVO.getFee_weight_count());

			podVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(podVO);

			AggregatedValueObject billVO = new HYBillVO();
			billVO.setParentVO(podVO);

			// 将子表的每条记录的签收件数改成它的件数
			InvPackBVO[] childVOs = dao.queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", pk);
			if(childVOs != null && childVOs.length > 0) {
				for(InvPackBVO childVO : childVOs) {
					childVO.setPod_num(childVO.getNum());
					childVO.setReject_num(0);
					childVO.setDamage_num(0);
					childVO.setLost_num(0);
					childVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(childVO);
				}
			}
			billVO.setChildrenVO(childVOs);
			billVOs.add(billVO);

			// 签收时，向发货单的跟踪信息表插入一条记录
			InvTrackingVO itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.POD);
			itVO.setTracking_time(new UFDateTime(new Date()));
			itVO.setTracking_memo("签收");
			itVO.setInvoice_vbillno(invVO.getVbillno());
			itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			itVO.setCreate_time(new UFDateTime(new Date()));
			itVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(itVO);
			NWDao.getInstance().saveOrUpdate(itVO);
		}
		dao.saveOrUpdate(toBeUpdate);
		// 执行公式后返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for(AggregatedValueObject billVO : billVOs) {
			retList.add(execFormula4Templet(billVO, paramVO));
		}
		billVOs.clear();
		return retList;
	}

	public Map<String, Object> doExpPod(String pk_invoice, AggregatedValueObject billVO, ParamVO paramVO) {
		logger.info("发货单异常签收，pk_invoice：" + pk_invoice);
		if(pk_invoice == null) {
			throw new BusiException("执行异常签收时需要选择发货单！");
		}
		InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk_invoice);
		if(invVO.getVbillstatus().intValue() != BillStatus.INV_ARRIVAL) {
			throw new BusiException("只有已到货的发货单才能执行异常签收！");
		}

		invVO.setVbillstatus(BillStatus.INV_SIGN);// 改成已签收
		invVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(invVO);

		PodVO parentVO = (PodVO) billVO.getParentVO();// 这个parentVO只包括一些要更新的参数
		PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk_invoice);
		podVO.setPod_num_count(parentVO.getPod_num_count());
		podVO.setReject_num_count(parentVO.getReject_num_count());
		podVO.setDamage_num_count(parentVO.getDamage_num_count());
		podVO.setLost_num_count(parentVO.getLost_num_count());
		podVO.setPod_man(parentVO.getPod_man());
		podVO.setPod_date(parentVO.getPod_date());
		podVO.setPod_memo(parentVO.getPod_memo());
		podVO.setPod_exp(UFBoolean.TRUE);// 签收异常为是
		podVO.setPod_exp_type(parentVO.getPod_exp_type());// 异常类型
		podVO.setPod_book_man(WebUtils.getLoginInfo().getPk_user());
		podVO.setPod_book_time(new UFDateTime(new Date()));
		podVO.setPod_weight_count(parentVO.getPod_weight_count());
		podVO.setPod_volume_count(parentVO.getPod_volume_count());
		podVO.setPod_volume_weight_count(parentVO.getPod_volume_weight_count());
		podVO.setPod_fee_weight_count(parentVO.getPod_fee_weight_count());
		podVO.setUpdate_rece_detail(parentVO.getUpdate_rece_detail());
		
		podVO.setDervice_attitude(parentVO.getDervice_attitude());
		podVO.setDelivery_speed(parentVO.getDelivery_speed());
		podVO.setLogistics_services(parentVO.getLogistics_services());
		
		podVO.setCurr_longitude(parentVO.getCurr_longitude());
		podVO.setCurr_latitude(parentVO.getCurr_latitude());
		podVO.setApp_detail_addr(parentVO.getApp_detail_addr());

		podVO.setStatus(VOStatus.UPDATED);
		billVO.setParentVO(podVO);// 重新设置parentVO
		// 在使用json转换后，这里会因为判断表头没有pk，认为是一个新增的记录。
		InvPackBVO[] childVOs = (InvPackBVO[]) billVO.getChildrenVO();
		InvPackBVO[] packBVOs = dao.queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", pk_invoice);
		int index = 0;
		for(InvPackBVO childVO : childVOs) {
			index++;
			int num = childVO.getNum().intValue();// 每行的总件数
			int pod_num = childVO.getPod_num() == null ? 0 : childVO.getPod_num().intValue();// 签收数
			int reject_num = childVO.getReject_num() == null ? 0 : childVO.getReject_num().intValue();// 拒收数
			int damage_num = childVO.getDamage_num() == null ? 0 : childVO.getDamage_num().intValue();// 损坏数
			int lost_num = childVO.getLost_num() == null ? 0 : childVO.getLost_num().intValue();// 丢失数
			if(num != (pod_num + reject_num + damage_num + lost_num)) {
				throw new BusiException("第[?]行签收件数+拒收件数+破损件数+丢失件数必须等于件数！",index+"");
			}
			for(InvPackBVO packBVO : packBVOs){
				if(packBVO.getPk_inv_pack_b().equals(childVO.getPk_inv_pack_b())){
					packBVO.setPod_num(pod_num);
					packBVO.setReject_num(reject_num);
					packBVO.setDamage_num(damage_num);
					packBVO.setLost_num(lost_num);
					packBVO.setStatus(VOStatus.UPDATED);
					break;
				}
			}
		}
		billVO.setChildrenVO(packBVOs);
		dao.saveOrUpdate(billVO);
		Map<String, Object> retMap = execFormula4Templet(billVO, paramVO);

		// 签收时，向发货单的跟踪信息表插入一条记录
		InvTrackingVO itVO = new InvTrackingVO();
		itVO.setTracking_status(TrackingConst.NODE_ARRI);
		itVO.setTracking_time(new UFDateTime(new Date()));
		itVO.setTracking_memo("签收");
		itVO.setInvoice_vbillno(invVO.getVbillno());
		itVO.setExp_flag(UFBoolean.TRUE);// 异常
		itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		NWDao.getInstance().saveOrUpdate(itVO);

		// 生成异常事故
		ExpAccidentVO eaVO = new ExpAccidentVO();
		eaVO.setDbilldate(new UFDate());
		eaVO.setStatus(VOStatus.NEW);
		eaVO.setVbillstatus(BillStatus.NEW);
		eaVO.setInvoice_vbillno(invVO.getVbillno());
		eaVO.setPk_customer(invVO.getPk_customer());
		eaVO.setEntrust_vbillno(podVO.getPod_entrust_vbillno());
		eaVO.setPk_carrier(podVO.getPod_carrier());
		eaVO.setOrigin(ExpAccidentOrgin.POD.toString());
		eaVO.setExp_type(podVO.getPod_exp_type());
		eaVO.setMemo(podVO.getPod_memo());
		eaVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		eaVO.setFb_user(WebUtils.getLoginInfo().getPk_user());
		eaVO.setFb_date(new UFDateTime(new Date()));
		eaVO.setOccur_date(parentVO.getPod_date());
		eaVO.setOccur_addr(parentVO.getApp_detail_addr());
		eaVO.setCreate_time(new UFDateTime(new Date()));
		eaVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		ParamVO paramVO1 = new ParamVO();
		paramVO1.setFunCode(FunConst.EXP_ACCIDENT_CODE);
		expAccidentService.setCodeField(eaVO, paramVO1);
		NWDao.setUuidPrimaryKey(eaVO);
		NWDao.getInstance().saveOrUpdate(eaVO);

		// 当是否更新应收明细复选框勾选时，异常签收时自动将以上签收件数，签收重量，签收体积，签收体积重，签收计费重更新到对应的原始应收明细中，
		// 同时根据合同刷新应收明细费用。如果没有勾选，则不更新应收明细信息。
		if(podVO.getUpdate_rece_detail() != null && podVO.getUpdate_rece_detail().booleanValue()) {
			ReceiveDetailVO rdVO = invoiceService.getReceiveDetailVOByInvoiceBillno(invVO.getVbillno());
			if(rdVO == null) {
				throw new BusiException("发货单[?]对应的原始应收明细被删除了！",invVO.getVbillno());
			}
			if(rdVO.getVbillstatus() == BillStatus.NEW) {
				rdVO.setNum_count(podVO.getPod_num_count());
				rdVO.setVolume_count(podVO.getPod_volume_count());
				rdVO.setWeight_count(podVO.getPod_weight_count());
				rdVO.setFee_weight_count(podVO.getPod_fee_weight_count());
				// 重新计算
				ReceiveDetailServiceImpl rdService = new ReceiveDetailServiceImpl();
				rdService.recomputeHeaderAmount(rdVO);
			}
		}
		
		String[] fGSInvpks = getFGSPks(new String[]{pk_invoice});
		if(fGSInvpks != null && fGSInvpks.length > 0){
			HYBillVO hyBillVO = new HYBillVO();
			hyBillVO.setParentVO(parentVO);
			doExpPod(fGSInvpks[0], billVO, paramVO); 
		}
		return retMap;
	}

	public List<Map<String, Object>> doUnpod(String[] pk_invoice, ParamVO paramVO) {
		logger.info("发货单撤销签收...");
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("撤销签收时需要选择发货单！");
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<AggregatedValueObject> billVOs = new ArrayList<AggregatedValueObject>();
		String[] fGSInvpks = getFGSPks(pk_invoice);
		if(fGSInvpks != null && fGSInvpks.length > 0){
			doUnpod(fGSInvpks, paramVO);
		}
		for(String pk : pk_invoice) {
			InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk);
			if(invVO.getVbillstatus().intValue() != BillStatus.INV_SIGN) {
				throw new BusiException("只有已签收的发货单才能执行撤销！");
			}
			invVO.setVbillstatus(BillStatus.INV_ARRIVAL);// 改成已到货
			invVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(invVO);

			PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk);
			// 如果是异常签收，那么将相应的异常事故删除
			if(podVO.getPod_exp() != null && podVO.getPod_exp().booleanValue()) {
				String sql = "select * from ts_exp_accident where isnull(dr,0)=0 and invoice_vbillno=? and origin=?";
				List<ExpAccidentVO> eaVOs = NWDao.getInstance().queryForList(sql, ExpAccidentVO.class,
						invVO.getVbillno(), ExpAccidentConst.ExpAccidentOrgin.POD.toString());
				if(eaVOs != null && eaVOs.size() > 0) {
					NWDao.getInstance().delete(eaVOs.toArray(new ExpAccidentVO[eaVOs.size()]));
				}
			}

			podVO.setPod_man(null);
			podVO.setPod_date(null);
			podVO.setPod_memo(null);
			podVO.setPod_exp(null);
			podVO.setPod_exp_type(null);
			podVO.setPod_book_man(null);
			podVO.setPod_book_time(null);

			// 签收件数\拒收件数、损坏件数、丢失件数置为0
			podVO.setPod_num_count(0);
			podVO.setReject_num_count(0);
			podVO.setDamage_num_count(0);
			podVO.setLost_num_count(0);

			podVO.setPod_weight_count(null);
			podVO.setPod_volume_count(null);
			podVO.setPod_volume_weight_count(null);
			podVO.setPod_fee_weight_count(null);

			podVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(podVO);

			AggregatedValueObject billVO = new HYBillVO();
			billVO.setParentVO(podVO);

			// 将子表的每条记录的签收件数\拒收件数、损坏件数、丢失件数都置为空
			InvPackBVO[] childVOs = dao.queryForSuperVOArrayByCondition(InvPackBVO.class, "pk_invoice=?", pk);
			if(childVOs != null && childVOs.length > 0) {
				for(InvPackBVO childVO : childVOs) {
					childVO.setPod_num(null);
					childVO.setReject_num(null);
					childVO.setDamage_num(null);
					childVO.setLost_num(null);
					childVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(childVO);
				}
			}
			billVO.setChildrenVO(childVOs);
			billVOs.add(billVO);

			// 删除发货单对应的跟踪记录
			InvTrackingVO itVO = dao.queryByCondition(InvTrackingVO.class, "invoice_vbillno=? and tracking_status=?",
					invVO.getVbillno(), TrackingConst.POD);
			if(itVO != null) {
				dao.delete(itVO);
			}
		}
		dao.saveOrUpdate(toBeUpdate);
		// 执行公式后返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for(AggregatedValueObject billVO : billVOs) {
			retList.add(execFormula4Templet(billVO, paramVO));
		}
		billVOs.clear();
		return retList;
	}

	public List<Map<String, Object>> doReceipt(String[] pk_invoice, PodVO newPodVO, ParamVO paramVO) {
		logger.info("发货单执行回单，pk_invoice:" + pk_invoice);
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("执行回单时需要选择发货单！");
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<AggregatedValueObject> billVOs = new ArrayList<AggregatedValueObject>();
		for(String pk : pk_invoice) {
			InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk);
			if(invVO.getVbillstatus().intValue() != BillStatus.INV_SIGN) {
				throw new BusiException("只有已签收的发货单才能执行回单！");
			}
			invVO.setVbillstatus(BillStatus.INV_BACK);// 改成已回单
			invVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(invVO);

			PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk);
			podVO.setReceipt_man(newPodVO.getReceipt_man());
			podVO.setAct_receipt_date(newPodVO.getAct_receipt_date());
			podVO.setReceipt_memo(newPodVO.getReceipt_memo());
			podVO.setReceipt_exp(UFBoolean.FALSE);// 回单异常为否
			podVO.setReceipt_book_man(WebUtils.getLoginInfo().getPk_user());
			podVO.setReceipt_book_time(new UFDateTime(new Date()));

			podVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(podVO);

			AggregatedValueObject billVO = new HYBillVO();
			billVO.setParentVO(podVO);
			billVOs.add(billVO);
			
			// 回单时，向发货单的跟踪信息表插入一条记录
			InvTrackingVO itVO = new InvTrackingVO();
			itVO.setTracking_status(TrackingConst.RECEIPT);
			itVO.setTracking_time(new UFDateTime(new Date()));
			itVO.setTracking_memo("回单");
			itVO.setInvoice_vbillno(invVO.getVbillno());
			itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			itVO.setCreate_time(new UFDateTime(new Date()));
			itVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(itVO);
			NWDao.getInstance().saveOrUpdate(itVO);
		}
		dao.saveOrUpdate(toBeUpdate);

		// 执行公式后返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for(AggregatedValueObject billVO : billVOs) {
			retList.add(execFormula4Templet(billVO, paramVO));
		}
		billVOs.clear();
		return retList;
	}

	public Map<String, Object> doExpReceipt(String pk_invoice, PodVO newPodVO, ParamVO paramVO) {
		logger.info("发货单异常回单，pk_invoice：" + pk_invoice);
		if(pk_invoice == null) {
			throw new BusiException("执行异常回单时需要选择发货单！");
		}
		AggregatedValueObject billVO = new HYBillVO();
		InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk_invoice);
		if(invVO.getVbillstatus().intValue() != BillStatus.INV_SIGN) {
			throw new BusiException("只有已签收的发货单才能执行异常回单！");
		}
		invVO.setVbillstatus(BillStatus.INV_BACK);// 改成已回单
		invVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(invVO);

		PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk_invoice);
		podVO.setReceipt_man(newPodVO.getReceipt_man());
		podVO.setAct_receipt_date(newPodVO.getAct_receipt_date());
		podVO.setReceipt_memo(newPodVO.getReceipt_memo());
		podVO.setReceipt_exp(UFBoolean.TRUE);// 回单异常为否
		podVO.setReceipt_exp_type(newPodVO.getReceipt_exp_type());// 异常类型
		podVO.setReceipt_book_man(WebUtils.getLoginInfo().getPk_user());
		podVO.setReceipt_book_time(new UFDateTime(new Date()));

		podVO.setStatus(VOStatus.UPDATED);
		dao.saveOrUpdate(podVO);

		billVO.setParentVO(podVO);
		Map<String, Object> retMap = execFormula4Templet(billVO, paramVO);
		
		// 回单时，向发货单的跟踪信息表插入一条记录
		InvTrackingVO itVO = new InvTrackingVO();
		itVO.setTracking_status(TrackingConst.RECEIPT);
		itVO.setTracking_time(new UFDateTime(new Date()));
		itVO.setTracking_memo("回单");
		itVO.setInvoice_vbillno(invVO.getVbillno());
		itVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		itVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		itVO.setCreate_time(new UFDateTime(new Date()));
		itVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(itVO);
		NWDao.getInstance().saveOrUpdate(itVO);

		// 生成异常事故
		ExpAccidentVO eaVO = new ExpAccidentVO();
		eaVO.setDbilldate(new UFDate());
		eaVO.setStatus(VOStatus.NEW);
		eaVO.setVbillstatus(BillStatus.NEW);
		eaVO.setInvoice_vbillno(invVO.getVbillno());
		eaVO.setPk_customer(invVO.getPk_customer());
		eaVO.setEntrust_vbillno(podVO.getPod_entrust_vbillno());
		eaVO.setPk_carrier(podVO.getPod_carrier());
		eaVO.setOrigin(ExpAccidentOrgin.RECEIPT.toString());
		eaVO.setExp_type(newPodVO.getReceipt_exp_type());
		eaVO.setMemo(podVO.getPod_memo());
		if(StringUtils.isBlank(eaVO.getMemo())){
			eaVO.setMemo(podVO.getReceipt_memo());
		}
		eaVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		eaVO.setFb_user(WebUtils.getLoginInfo().getPk_user());
		eaVO.setFb_date(new UFDateTime(new Date()));
		eaVO.setOccur_date(newPodVO.getPod_date());
		eaVO.setOccur_addr(newPodVO.getApp_detail_addr());
		eaVO.setCreate_time(new UFDateTime(new Date()));
		eaVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		ParamVO paramVO1 = new ParamVO();
		paramVO1.setFunCode(FunConst.EXP_ACCIDENT_CODE);
		expAccidentService.setCodeField(eaVO, paramVO1);
		NWDao.setUuidPrimaryKey(eaVO);
		NWDao.getInstance().saveOrUpdate(eaVO);

		return retMap;
	}

	public List<Map<String, Object>> doUnreceipt(String[] pk_invoice, ParamVO paramVO) {
		logger.info("发货单撤销回单，pk_invoice" + pk_invoice);
		if(pk_invoice == null || pk_invoice.length == 0) {
			throw new BusiException("撤销回单时需要选择发货单！");
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<AggregatedValueObject> billVOs = new ArrayList<AggregatedValueObject>();
		for(String pk : pk_invoice) {
			InvoiceVO invVO = dao.queryByCondition(InvoiceVO.class, "pk_invoice=?", pk);
			if(invVO.getVbillstatus().intValue() != BillStatus.INV_BACK) {
				throw new BusiException("只有已回单的发货单才能执行撤销！");
			}
			invVO.setVbillstatus(BillStatus.INV_SIGN);// 改成已签收
			invVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(invVO);

			PodVO podVO = dao.queryByCondition(PodVO.class, "pk_invoice=?", pk);
			// 如果是异常签收，那么将相应的异常事故删除
			if(podVO.getPod_exp() != null && podVO.getPod_exp().booleanValue()) {
				String sql = "select * from ts_exp_accident WITH(NOLOCK) where isnull(dr,0)=0 and invoice_vbillno=? and origin=?";
				List<ExpAccidentVO> eaVOs = NWDao.getInstance().queryForList(sql, ExpAccidentVO.class,
						invVO.getVbillno(), ExpAccidentConst.ExpAccidentOrgin.RECEIPT.toString());
				if(eaVOs != null && eaVOs.size() > 0) {
					NWDao.getInstance().delete(eaVOs.toArray(new ExpAccidentVO[eaVOs.size()]));
				}
			}

			podVO.setReceipt_man(null);
			podVO.setAct_receipt_date(null);
			podVO.setReceipt_memo(null);
			podVO.setReceipt_exp(null);// 回单异常为否
			podVO.setReceipt_exp_type(null);// 异常类型
			podVO.setReceipt_book_man(null);
			podVO.setReceipt_book_time(null);

			AggregatedValueObject billVO = new HYBillVO();
			billVO.setParentVO(podVO);
			billVOs.add(billVO);
			InvTrackingVO itVO = dao.queryByCondition(InvTrackingVO.class, "invoice_vbillno=? and tracking_status=?",
					invVO.getVbillno(), TrackingConst.RECEIPT);
			if(itVO != null) {
				dao.delete(itVO);
			}
		}
		dao.saveOrUpdate(toBeUpdate);
		// 执行公式后返回
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for(AggregatedValueObject billVO : billVOs) {
			retList.add(execFormula4Templet(billVO, paramVO));
		}
		billVOs.clear();
		return retList;
	}

	protected void processBeforeExecFormula(List<SuperVO> superVOList, ParamVO paramVO) {
		super.processBeforeExecFormula(superVOList, paramVO);
		if(superVOList != null && superVOList.size() > 0) {
			String fromExp = ServletContextHolder.getRequest().getParameter("fromExp");
			if("true".equals(fromExp)) {
				// 异常签收弹出框
				for(SuperVO superVO : superVOList) {
					superVO.setAttributeValue("pod_num", superVO.getAttributeValue("num"));// 签收数量默认等于件数
				}
			}
		}

	}
	//YAOJIIE 1 24 根据发货单号查询对应的分公司结算发货单，递归进行签收等动作。
	public String[] getFGSPks(String[] pk_invoice){
		InvoiceVO[] invoiceVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, "pk_invoice in "
				+ NWUtils.buildConditionString(pk_invoice));
		List<String> ordernos = new ArrayList<String>();
		if(invoiceVOs != null && invoiceVOs.length != 0){
			for(InvoiceVO invoiceVO : invoiceVOs){
				if(invoiceVO.getInvoice_origin() != null && DataDictConst.INVOICE_ORIGIN.FGS.intValue() == invoiceVO.getInvoice_origin()){
					ordernos.add(invoiceVO.getOrderno());
				}
			}
			
		}
		if(ordernos != null && ordernos.size() > 0){
			String cond = NWUtils.buildConditionString(ordernos.toArray(new String[ordernos.size()]));
			String sql = "SELECT ts_invoice.* FROM ts_entrust WITH(NOLOCK) "
					+" LEFT JOIN  ts_ent_seg_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_seg_b.pk_entrust "  
					+" LEFT JOIN  ts_segment WITH(NOLOCK) ON ts_segment.pk_segment=ts_ent_seg_b.pk_segment " 
					+" LEFT JOIN  ts_ent_inv_b WITH(NOLOCK) ON ts_entrust.pk_entrust=ts_ent_inv_b.pk_entrust " 
					+" LEFT JOIN  ts_invoice WITH(NOLOCK) ON ts_invoice.pk_invoice=ts_ent_inv_b.pk_invoice " 
					+" WHERE ts_segment.pk_arrival = ts_invoice.pk_arrival "
					+" AND ts_segment.invoice_vbillno=ts_invoice.vbillno  "
					+" AND isnull(ts_entrust.dr,0) = 0 "
					+" AND ts_entrust.vbillstatus !=24 "
					+" AND isnull(ts_ent_seg_b.dr,0) = 0  "
					+" AND isnull(ts_ent_inv_b.dr,0) = 0  "
					+" AND isnull(ts_invoice.dr,0) = 0  "
					+" AND ts_entrust.vbillno in " + cond;
			List<InvoiceVO> initInvoiceVOs = NWDao.getInstance().queryForList(sql, InvoiceVO.class);
			if(initInvoiceVOs != null && initInvoiceVOs.size() > 0){
				List<String> invPks = new ArrayList<String>();
				for(InvoiceVO invoiceVO : initInvoiceVOs){
					invPks.add(invoiceVO.getPk_invoice());
				}
				if(invPks != null && invPks.size() > 0 ){
					return invPks.toArray(new String[invPks.size()]);
				}
			}
		}
		return null;
	}
	/**
	 * 2015 11 13 yaojiie
	 * tms的单据使用的是公司条件+逻辑条件，对于这个单据，另外需要加上委派公司等于当前公司的单据
	 */
	@SuppressWarnings("rawtypes")
	public String buildLogicCondition(Class clazz, UiQueryTempletVO templetVO) {
		try {
			SuperVO superVO = (SuperVO) clazz.newInstance();
			Field pk_corp = ReflectionUtils.getDeclaredField(superVO, "pk_corp");// 如果存在pk_corp字段
			if(pk_corp != null) {
				String corpCond = CorpHelper.getCurrentCorpWithChildren(superVO.getTableName());
				String logicCond = QueryHelper.getLogicCond(templetVO);
				if(StringUtils.isNotBlank(logicCond)) {
					corpCond += " or (" + logicCond + ")";
				}
				// 2015-11-13 def1里存储的是从别的表里获取的 delegate_corp
				corpCond += " or (" + superVO.getTableName() + ".def1='"
						+ WebUtils.getLoginInfo().getPk_corp() + "')";
				return "(" + corpCond + ")";
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String fileupload(MultipartFile mfile, String originalFilename, String vbillnoOrCustOrderno) {
		//根据单据号查询对应的单据
		String sql = "SELECT * FROM ts_pod WITH(nolock) LEFT JOIN ts_invoice WITH(nolock) ON ts_pod.pk_invoice = ts_invoice.pk_invoice "
				+ " WHERE isnull(ts_pod.dr,0) = 0 AND isnull(ts_invoice.dr,0) = 0 AND (ts_invoice.vbillno=? OR ts_invoice.cust_orderno=?)";
		List<PodVO> podVOs = NWDao.getInstance().queryForList(sql, PodVO.class, vbillnoOrCustOrderno,vbillnoOrCustOrderno);
		
		if(podVOs == null || podVOs.size() == 0){
			logger.info("单号：" + vbillnoOrCustOrderno + "没有对应的POD单和发货单！");
			return null;
		}
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		String pk_corp = WebUtils.getLoginInfo().getPk_corp();
		String create_user = WebUtils.getLoginInfo().getPk_user();
		String uploadDir = Global.uploadDir; 
		File dir = new File(uploadDir + File.separator + BillTypeConst.POD_RECEIPT + File.separator + DateUtils.formatDate(new Date(), DateUtils.DATEFORMAT_HORIZONTAL));
		if(!dir.exists()) {
			dir.mkdirs(); // 若目录不存在，则创建目录
		}
		String extension = ""; // 文件后缀名
		String fileName = "";//文件名
		String fileUrl = "";//文件路径，但实际存的是路径加文件名
		if(mfile.getOriginalFilename().indexOf(".") != -1) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		int index = 1;
		for(PodVO podVO : podVOs){
			String time_name = DateUtils.formatDate(new Date(), DateUtils.DATE_TIME_FORMAT_ALL);
			fileName = time_name + extension;
			fileUrl = dir + File.separator + fileName;
			File file = new File(fileUrl);
			if(file.exists()){
				fileUrl = dir + File.separator + time_name +"-"+ index + extension;
				index ++;
				file = new File(fileUrl);
			}else{
				index = 1;
			}
			
			try {
				mfile.transferTo(file);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return e.toString();
			} catch (IOException e) {
				e.printStackTrace();
				return e.toString();
			}
			
			
			
			AttachmentVO attachmentVO = new AttachmentVO();
			attachmentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(attachmentVO);
			attachmentVO.setBill_type(BillTypeConst.POD_RECEIPT);
			attachmentVO.setPk_billno(podVO.getPk_invoice());
			attachmentVO.setPk_corp(pk_corp);
			attachmentVO.setAttachment_name(fileName);
			attachmentVO.setFile_url(fileUrl);
			attachmentVO.setFile_name(mfile.getOriginalFilename());
			attachmentVO.setCreate_user(create_user);
			attachmentVO.setCreate_time(new UFDateTime(new Date()));
			toBeUpdate.add(attachmentVO);
			
			podVO.setReceipt_flag(UFBoolean.TRUE);
			podVO.setStatus(VOStatus.UPDATED);
			toBeUpdate.add(podVO);
		}
		dao.saveOrUpdate(toBeUpdate);
		return "success";
		
	}
}
