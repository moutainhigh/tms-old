package com.tms.service.te.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiAlertException;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import com.tms.BillStatus;
import com.tms.service.te.TrackingService;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;

/**
 * 委托单快递信息导入
 * 
 * @author XIA
 * @date 2016 7 15
 */
public class ExpressExcelImporter extends BillExcelImporter {
	
	private TrackingService trackingService = SpringContextHolder.getBean("trackingService");;
	
	
	public ExpressExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new EntrustVO();
	}

	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolveForMultiTable(file);//这种解析方式支持跨表，处理子表信息即可。
		if(aggVOs == null || aggVOs.size() == 0){
			throw new BusiException("没有需要导入的信息！");
		}
		String[] billnos = new String[aggVOs.size()];
		EntrustVO[] entrustVOs = new EntrustVO[aggVOs.size()];//这里只处理委托单表，如果要处理多表，使用ExAggEntrustVO即可。
		for(int i=0;i<aggVOs.size();i++){
			ExAggEntrustVO aggEntrustVO = (ExAggEntrustVO) aggVOs.get(i);
			EntrustVO entrustVO = (EntrustVO) aggEntrustVO.getParentVO();
			billnos[i] = entrustVO.getVbillno();
			entrustVOs[i] = entrustVO;
		}
		String cond = NWUtils.buildConditionString(billnos);
		
		EntrustVO[] oldEntrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "vbillno in " + cond);
		
		if(oldEntrustVOs == null || oldEntrustVOs.length == 0){
			throw new BusiException("委托单不存在！");
		}
		String errsg = "";
		for(EntrustVO entrustVO : entrustVOs){
			boolean flag = true;
			for(EntrustVO oldEntrustVO : oldEntrustVOs){
				if(oldEntrustVO.getVbillno().equals(entrustVO.getVbillno())){
					//将导入字段不为空的值复制到系统原有的委托单里。
					flag = false;
					String[] attrs = entrustVO.getAttributeNames();
					for(String attr : attrs){
						if( !attr.equals(entrustVO.getPKFieldName()) && !attr.equals("def10")){
							if(StringUtils.isNotBlank(entrustVO.getAttributeValue(attr) == null ? "":entrustVO.getAttributeValue(attr).toString())){
								oldEntrustVO.setAttributeValue(attr, entrustVO.getAttributeValue(attr));
							}
						}
					}
					//先将导入数据保存
					oldEntrustVO.setStatus(VOStatus.UPDATED);
					NWDao.getInstance().saveOrUpdate(oldEntrustVO);
					if(UFBoolean.TRUE.equals(new UFBoolean(entrustVO.getDef10()))){
						if(oldEntrustVO.getVbillstatus() != BillStatus.ENT_CONFIRM){
							errsg += entrustVO.getVbillno() + "状态不正确！</br>";
							continue;
						}
						//进行提货确认动作
						EntLineBVO entLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class, "pk_entrust=? and addr_flag='S'", oldEntrustVO.getPk_entrust());
						if(entLineBVO == null){
							errsg += entrustVO.getVbillno() + "节点不存在！</br>";
							continue;
						}
						entLineBVO.setAct_arri_date(entrustVO.getAct_deli_date());
						entLineBVO.setMemo(entrustVO.getMemo());
						try {
							trackingService.confirmArrival(entLineBVO,0);
						} catch (Exception e) {
							errsg += entrustVO.getVbillno() + e.getMessage() +"</br>";
							continue;
						}
					}
				}
			}
			if(flag){
				errsg += entrustVO.getVbillno() + "已被删除！</br>";
			}
		}
		if(StringUtils.isNotBlank(errsg)){
			//errsg = "导入成功,以下单据存在错误！</br>" + errsg;
			throw new BusiException(errsg);
		}
		
	}
	
	
}
