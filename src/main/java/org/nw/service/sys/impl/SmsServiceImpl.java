package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.SmsService;
import org.nw.utils.FormulaHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.SmsVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.constants.TabcodeConst;
import com.tms.vo.attach.AttachmentVO;

/**
 * 站内信处理
 * 
 * @author xuqc
 * @date 2013-7-1 下午02:49:08
 */
@Service
public class SmsServiceImpl extends AbsToftServiceImpl implements SmsService {

	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, SmsVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, "pk_sms");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("billnos")) {
					fieldVO.setRenderer("billnosRenderer");
				}
				if(fieldVO.getItemkey().equals("upload_flag")) {
					fieldVO.setRenderer("attaRenderer");
				}
			}
		}
		return templetVO;
	}

	/**
	 * 只能查询个人的站内信
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String cond = " (receiver is null or receiver='" + WebUtils.getLoginInfo().getPk_user() + "'"
				+ " or sender ='" + WebUtils.getLoginInfo().getPk_user() + "')";
		String where = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(where)) {
			cond += " and " + where;
		}
		return cond;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			return " order by read_flag,post_date desc ";
		}
		return orderBy;
	}

	private String[] getFormulas() {
		return new String[] { "sender_name->getColValue(nw_user,user_name,pk_user,sender)",
				"receiver_name->getColValue(nw_user,user_name,pk_user,receiver)" };
	}

	public List<Map<String, Object>> getTop5() {
		String sql = null;
		if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
			sql = "select * from nw_sms  where ifnull(dr,0)=0 and (receiver is null or receiver=?) order by read_flag,post_date desc limit 0,5";
		} else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 * from nw_sms where isnull(dr,0)=0 and (receiver is null or receiver=?) order by read_flag,post_date desc";
		}else if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 * from nw_sms where rownum < 6 and isnull(dr,0)=0 and (receiver is null or receiver=?) order by read_flag,post_date desc";
		}
		List<SmsVO> list = dao.queryForList(sql, SmsVO.class, WebUtils.getLoginInfo().getPk_user());
		return FormulaHelper.execFormulaForSuperVO(list, getFormulas());
	}

	public Map<String, Object> getByPK(String pk_sms) {
		if(pk_sms == null) {
			return null;
		}
		SmsVO vo = this.getByPrimaryKey(SmsVO.class, pk_sms);
		List<SmsVO> vos = new ArrayList<SmsVO>();
		vos.add(vo);
		List<Map<String, Object>> list = FormulaHelper.execFormulaForSuperVO(vos, getFormulas());
		return list.get(0);
	}
	
	@Override
	public void processBeforeShow(ParamVO paramVO){
		updateReadFlag(paramVO.getBillId());
	}

	public SmsVO updateReadFlag(String pk_sms) {
		if(StringUtils.isBlank(pk_sms)) {
			return null;
		}
		//自己的站内信，不打标记。
		SmsVO vo = NWDao.getInstance().queryByCondition(SmsVO.class, "pk_sms=? and sender <>'" + WebUtils.getLoginInfo().getPk_user() + "'", pk_sms);
		if(vo == null) {
			return null;
		}
		vo.setRead_flag(UFBoolean.TRUE);
		vo.setRead_date(new UFDateTime(new Date()));
		vo.setStatus(VOStatus.UPDATED);
		NWDao.getInstance().saveOrUpdate(vo);
		return vo;
	}

	public void doSend(SmsVO vo,String[] pk_attas) {
		if(vo == null) {
			return;
		}
		String pk = "";
		vo.setPost_date(new UFDateTime(new Date()));
		vo.setSender(WebUtils.getLoginInfo().getPk_user());
		vo.setRead_flag(UFBoolean.FALSE);
		vo.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		if(StringUtils.isNotBlank(vo.getReceiver())) {
			String[] receiverAry = vo.getReceiver().split(",");
			for(String receiver : receiverAry) {
				SmsVO newVO = new SmsVO();
				newVO.setTitle(vo.getTitle());
				newVO.setContent(vo.getContent());
				newVO.setBillids(vo.getBillids());
				newVO.setBillnos(vo.getBillnos());
				newVO.setFun_code(vo.getFun_code());
				newVO.setSender(vo.getSender());
				newVO.setPost_date(vo.getPost_date());
				newVO.setRead_flag(vo.getRead_flag());
				newVO.setUpload_flag(vo.getUpload_flag());
				newVO.setReceiver(receiver);
				newVO.setPk_corp(vo.getPk_corp());
				newVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(newVO);
				pk = newVO.getPk_sms();
				NWDao.getInstance().saveOrUpdate(newVO);
			}
		} else {
			vo.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(vo);
			pk = vo.getPk_sms();
			NWDao.getInstance().saveOrUpdate(vo);
		}
		if(pk_attas != null && pk_attas.length > 0){
			String cond = NWUtils.buildConditionString(pk_attas);
			AttachmentVO[] attachmentVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AttachmentVO.class, " bill_type ='SMS' and pk_attachment in "+  cond);
			if(attachmentVOs != null && attachmentVOs.length > 0){
				for(AttachmentVO attachmentVO : attachmentVOs){
					attachmentVO.setStatus(VOStatus.UPDATED);
					attachmentVO.setPk_billno(pk);
					NWDao.getInstance().saveOrUpdate(attachmentVO);
				}
			}
		}
		
		
	}

	public Integer getCount() {
		String sql = "select count(1) from nw_sms WITH(NOLOCK) where isnull(dr,0)=0 and receiver=? and isnull(read_flag,'N')='N'";
		Integer count = null;
		if(WebUtils.getLoginInfo() != null){
			count = NWDao.getInstance().queryForObject(sql, Integer.class, WebUtils.getLoginInfo().getPk_user());
		}
		return count;
	}

	public String getVbillnosByFunVOAndbillids(String billIds, FunVO funVO) {
		if(StringUtils.isBlank(billIds) || funVO == null){
			return null;
		}
		funVO.getClass_name();
		funVO.getClass();
		//Class.forName(className)
		//table_code
		
		
		// TODO Auto-generated method stub
		return null;
	}
}
