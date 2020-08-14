package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.AlarmConst;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.NWDao;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.AlarmService;
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
import org.nw.vo.sys.AlarmVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 预警信息处理接口，首页定时查询预警信息，
 * 
 * @author xuqc
 * @date 2013-9-19 上午10:30:26
 */
@Service
public class AlarmServiceImpl extends AbsToftServiceImpl implements AlarmService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AlarmVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AlarmVO.PK_ALARM);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals("billno")) {
					fieldVO.setRenderer("billnoRenderer");
				}
			}
		}
		return templetVO;
	}

	protected boolean isLogicalDelete() {
		return false;
	}

	private String[] getFormulas() {
		return new String[] { "sender_man_name->getcolvalue(nw_user,user_name,pk_user,sender_man)",
				"deal_man_name->getcolvalue(nw_user,user_name,pk_user,deal_man)" };
	}

	public List<Map<String, Object>> getTop5() {
		String sql = "";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select * from nw_alarm WITH(NOLOCK) where rownum < 6 and isnull(dr,0)=0 and alarm_type=? and pk_corp=? and receiver_man=? "
					+ "and isnull(deal_flag,'N')='N' order by sender_date desc";
		}else if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
			sql = "select top 5 * from nw_alarm WITH(NOLOCK) where isnull(dr,0)=0 and alarm_type=? and pk_corp=? and receiver_man=? "
					+ "and isnull(deal_flag,'N')='N' order by sender_date desc";
		}
		List<AlarmVO> list = NWDao.getInstance().queryForList(sql, AlarmVO.class, AlarmConst.TODO,
				WebUtils.getLoginInfo().getPk_corp(), WebUtils.getLoginInfo().getPk_user());
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(list.size());
		for(SuperVO vo : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] attrs = vo.getAttributeNames();
			for(String key : attrs) {
				Object obj = vo.getAttributeValue(key);
				map.put(key, obj);
			}
			mapList.add(map);
		}
		return FormulaHelper.execFormula(mapList, getFormulas(), true);
	}

	public void sendAlarm(AggregatedValueObject billVO, String billtype, String funCode, String[] receiver_man) {
		if(receiver_man == null || receiver_man.length == 0) {
			return;
		}
		SuperVO superVO = (SuperVO) billVO.getParentVO();
		if(superVO == null) {
			return;
		}
		// 发送预警信息
		for(String pk_user : receiver_man) {
			AlarmVO alarmVO = new AlarmVO();
			alarmVO.setType(AlarmConst.TODO);
			alarmVO.setBilltype(billtype);
			alarmVO.setBillno(superVO.getAttributeValue("vbillno").toString());
			alarmVO.setPk_bill(superVO.getPrimaryKey());
			// XXX 这个功能节点是可以进行审核的功能节点
			// 发送的功能节点和审核的功能节点可能是不同的
			alarmVO.setFun_code(funCode);
			alarmVO.setSender_man(WebUtils.getLoginInfo().getPk_user());
			alarmVO.setSender_date(new UFDateTime(new Date()));
			alarmVO.setTitle("请对单据[" + superVO.getAttributeValue("vbillno").toString() + "]进行处理！");
			alarmVO.setMessage("");
			alarmVO.setReceiver_man(pk_user);
			alarmVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			alarmVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(alarmVO);
			NWDao.getInstance().saveOrUpdate(alarmVO);
		}
	}

	public void deleteAlarmByReceiver_man(String pk_bill, String receiver_man) {
		AlarmVO[] alarmVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AlarmVO.class,
				" pk_bill=? and receiver_man=? and isnull(deal_flag,'N')='N' and isnull(dr,0)=0 ", pk_bill,
				receiver_man);
		if(alarmVOs != null && alarmVOs.length > 0) {
			NWDao.getInstance().delete(alarmVOs, false);// 使用物理删除
		}
	}

	public void deleteAlarm(String pk_bill, String sender_man) {
		AlarmVO[] alarmVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AlarmVO.class,
				" pk_bill=? and sender_man=? and isnull(deal_flag,'N')='N' and isnull(dr,0)=0 ", pk_bill, sender_man);
		if(alarmVOs != null && alarmVOs.length > 0) {
			NWDao.getInstance().delete(alarmVOs, false);// 使用物理删除
		}
	}

	public void dealAlarm(String pk_bill, String[] receiver_man) {
		if(pk_bill == null || receiver_man == null || receiver_man.length == 0) {
			return;
		}
		String cond = NWUtils.buildConditionString(receiver_man);
		AlarmVO[] alarmVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AlarmVO.class,
				" pk_bill=? and receiver_man in " + cond + " and isnull(deal_flag,'N')='N' and isnull(dr,0)=0 ",
				pk_bill);
		if(alarmVOs != null && alarmVOs.length > 0) {
			for(AlarmVO alarmVO : alarmVOs) {
				alarmVO.setStatus(VOStatus.UPDATED);
				alarmVO.setDeal_man(WebUtils.getLoginInfo().getPk_user());
				alarmVO.setDeal_date(new UFDateTime(new Date()));
				alarmVO.setDeal_flag(UFBoolean.TRUE);
			}
			NWDao.getInstance().saveOrUpdate(alarmVOs);
		}
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by deal_flag,sender_date";
		}
		return orderBy;
	}
}
