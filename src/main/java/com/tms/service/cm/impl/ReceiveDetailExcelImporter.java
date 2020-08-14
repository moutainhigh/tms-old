package com.tms.service.cm.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.BillStatus;
import com.tms.constants.TabcodeConst;
import com.tms.vo.cm.ExAggReceiveDetailVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvoiceVO;

/**
 * 应收明细导入
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class ReceiveDetailExcelImporter extends BillExcelImporter {

	public ReceiveDetailExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected SuperVO getParentVO() {
		return new ReceiveDetailVO();
	}
	
	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		List<SuperVO> VOs = new ArrayList<SuperVO>();
		List<ReceDetailBVO> newRaceDetailBVOs = new ArrayList<ReceDetailBVO>();
		List<ReceiveDetailVO> importReceiveDetailVOs = new ArrayList<ReceiveDetailVO>();
		List<String> vbillnos = new ArrayList<String>();
		for (AggregatedValueObject aggVO : aggVOs) {
			ReceiveDetailVO receiveDetailVO = (ReceiveDetailVO)aggVO.getParentVO();
			importReceiveDetailVOs.add(receiveDetailVO);
			ExAggReceiveDetailVO exAggReceiveDetailVO = (ExAggReceiveDetailVO) aggVO;
			ReceDetailBVO[] receDetailBVOs = (ReceDetailBVO[])exAggReceiveDetailVO.getTableVO(TabcodeConst.TS_RECE_DETAIL_B);
			//导入文件中的应付明细BVO
			for(ReceDetailBVO receDetailBVO : receDetailBVOs){
				receDetailBVO.setPk_receive_detail(receiveDetailVO.getVbillno());
				newRaceDetailBVOs.add(receDetailBVO);
			}
			vbillnos.add(receiveDetailVO.getVbillno());
		}
		//将list里的重复元素去掉
		String cond = NWUtils.buildConditionString(vbillnos.toArray(new String[vbillnos.size()]));
		//根据导入文件的应收明细单号，查询到的应收明细VO
		ReceiveDetailVO[] receiveDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ReceiveDetailVO.class, " vbillno in " + cond);
		if(receiveDetailVOs ==null || receiveDetailVOs.length == 0){
			throw new BusiException("没有查询到应收明细单号，请检查数据！");
		}
		//检查导入数据是否与数据库数据一致。
		List<String> receiveDetailVOvbillnos = new ArrayList<String>();
		//判断单据状态，并将单据号放到一个List中，方便比较。
		for(ReceiveDetailVO receiveDetailVO : receiveDetailVOs){
			if(receiveDetailVO.getVbillstatus() != BillStatus.NEW){
				throw new BusiException("单据号[?]状态不是新建，不允许导入，请检查数据！",receiveDetailVO.getVbillno());
			}
			for(ReceiveDetailVO importReceiveDetailVO : importReceiveDetailVOs){
				//预留def1-10 字段，导入使用。
				if(importReceiveDetailVO.getVbillno().equals(receiveDetailVO.getVbillno())){
					receiveDetailVO.setDef1(importReceiveDetailVO.getDef1());
					receiveDetailVO.setDef2(importReceiveDetailVO.getDef2());
					receiveDetailVO.setDef3(importReceiveDetailVO.getDef3());
					receiveDetailVO.setDef4(importReceiveDetailVO.getDef4());
					receiveDetailVO.setDef5(importReceiveDetailVO.getDef5());
					receiveDetailVO.setDef6(importReceiveDetailVO.getDef6());
					receiveDetailVO.setDef7(importReceiveDetailVO.getDef7());
					receiveDetailVO.setDef8(importReceiveDetailVO.getDef8());
					receiveDetailVO.setDef9(importReceiveDetailVO.getDef9());
					receiveDetailVO.setDef10(importReceiveDetailVO.getDef10());
					break;
				}
			}
			receiveDetailVOvbillnos.add(receiveDetailVO.getVbillno());
		}
		//比较数据,只有当结果集不一致时，才需要进行比较
		String errormesg = "";
		if(vbillnos.size() != receiveDetailVOs.length){
			for(String vbillno : vbillnos){
				if(!receiveDetailVOvbillnos.contains(vbillno)){
					errormesg = errormesg + (vbillno + " ");
				}
			}
			if(StringUtils.isNotBlank(errormesg)){
				throw new BusiException("单据号[?]不存在，请检查数据！",errormesg.substring(0, errormesg.length()-1));
			}
		}

		//获取数据库已有的应收明细BVO
		List<String> pk_receive_details = new ArrayList<String>();
		for(ReceiveDetailVO receiveDetailVO : receiveDetailVOs){
			pk_receive_details.add(receiveDetailVO.getPk_receive_detail());
		}
		String condOld = NWUtils.buildConditionString(pk_receive_details.toArray(new String[pk_receive_details.size()]));
		ReceDetailBVO[] oldReceDetailBVOs =  NWDao.getInstance().queryForSuperVOArrayByCondition(ReceDetailBVO.class, " pk_receive_detail in " + condOld);
		//检查结果一致，并获取到所有的应收明细BVO，进行保存动作。
		//遍历receiveDetailVOs，将receiveDetailVO和receDetailBVO进行匹配
		//2015 12 15 添加invoiceVbillno，记录应收明细对应的发货单号。
		List<String> invoiceVbillnos = new ArrayList<String>();
		for(ReceiveDetailVO receiveDetailVO : receiveDetailVOs){
			invoiceVbillnos.add(receiveDetailVO.getInvoice_vbillno());
			//将头VO添加到
			VOs.add(receiveDetailVO);
			List<ReceDetailBVO> allDetailBVOs = new ArrayList<ReceDetailBVO>();
			for(ReceDetailBVO receDetailBVO : newRaceDetailBVOs){
				//数据库里查询到的VO的getPk_receive_detail，记录的是PK，而导入的BVO里getPk_receive_detail记录的是单据号
				if(receiveDetailVO.getVbillno().equals(receDetailBVO.getPk_receive_detail())){
					allDetailBVOs.add(receDetailBVO);
					VOs.add(receDetailBVO);
					receDetailBVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail());
					receDetailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(receDetailBVO);
				}
			}
			for(ReceDetailBVO receDetailBVO : oldReceDetailBVOs){
				//将数据库里已有的明细也加到总明细中，进行金额合计
				if(receiveDetailVO.getPk_receive_detail().equals(receDetailBVO.getPk_receive_detail())){
					allDetailBVOs.add(receDetailBVO);
				}
			}
			CMUtils.processExtenal(receiveDetailVO, allDetailBVOs.toArray(new ReceDetailBVO[allDetailBVOs.size()]));
		}
		 NWDao.getInstance().saveOrUpdate(VOs);
		 //计算金额利润
		 String invCond = NWUtils.buildConditionString(invoiceVbillnos.toArray(new String[invoiceVbillnos.size()]));
		 InvoiceVO[] invoiceVOs =  NWDao.getInstance().queryForSuperVOArrayByCondition(InvoiceVO.class, " vbillno in " + invCond);
		 CMUtils.totalCostComput(Arrays.asList(invoiceVOs));
	}
	
}
