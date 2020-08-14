package com.tms.service.cm.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.IBillService;
import org.nw.utils.BillnoHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.PayDetailConst;
import com.tms.constants.ReceiveDetailConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.cm.PayDetailService;
import com.tms.service.cm.ReceiveDetailService;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.ExAggReceiveDetailVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.ReceDetailBVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.cm.WarehouseVO;


public class WarehouselExcelImporter extends BillExcelImporter {

	public WarehouselExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}
	
	protected WarehouseVO getParentVO() {
		return new WarehouseVO();
	}
	
	public List<AggregatedValueObject> resolve(File file) throws Exception {
		if(file == null) {
			throw new BusiException("没有选择要导入的文件！");
		}
		if(service == null) {
			throw new BusiException("导入模板时需要注入Service类！");
		}
		InputStream input = null;
		Workbook wb = null;
		try {
			String sql = "SELECT nw_billtemplet_b.* FROM nw_billtemplet_b WITH(NOLOCK)  "
					+ " LEFT JOIN nw_billtemplet ON nw_billtemplet.pk_billtemplet = nw_billtemplet_b.pk_billtemplet "
					+ " WHERE isnull(nw_billtemplet_b.dr,0)=0 AND isnull(nw_billtemplet.dr,0)=0 "
					+ " AND nw_billtemplet.nodecode = 't413'";
			List<BillTempletBVO> templetBVOs = NWDao.getInstance().queryForList(sql, BillTempletBVO.class);
			Map<String, BillTempletBVO> itemMap = new HashMap<String, BillTempletBVO>();
			for(BillTempletBVO templetBVO : templetBVOs){
				itemMap.put(templetBVO.getItemkey(), templetBVO);
			}
			input = new FileInputStream(file);
			wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(0);
			return resolve(sheet, itemMap);
		} finally {
			try {
				input.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void _import(File file) throws Exception {
		
		PayDetailService payDetailService = SpringContextHolder.getBean("payDetailServiceImpl");
		ReceiveDetailService receDetailService = SpringContextHolder.getBean("receiveDetailServiceImpl");
		
		ParamVO pdParamVO = new ParamVO();
		pdParamVO.setFunCode(FunConst.PAY_DETAIL_CODE);
		ParamVO rdParamVO = new ParamVO();
		rdParamVO.setFunCode(FunConst.RECEIVE_DETAIL_CODE);
		List<AggregatedValueObject> aggVOs = resolve(file);
		//按账期，客户，承运商，将导入明细分为应收和应付两组
		Map<String,List<PayDetailBVO>> pdGroup = new HashMap<String, List<PayDetailBVO>>();
		Map<String,List<ReceDetailBVO>> rdGroup = new HashMap<String, List<ReceDetailBVO>>();
		for(AggregatedValueObject aggVO : aggVOs){
			WarehouseVO warehouseVO = (WarehouseVO) aggVO.getParentVO();
			if(StringUtils.isNotBlank(warehouseVO.getPk_carrier())
					&& warehouseVO.getPay_amount() != null){
				String key = warehouseVO.getAccount_period() + "," +warehouseVO.getPk_carrier();
				List<PayDetailBVO> voList = pdGroup.get(key);
				if(voList == null){
					voList = new ArrayList<PayDetailBVO>();
					pdGroup.put(key, voList);
				}
				PayDetailBVO detailBVO = new PayDetailBVO();
				detailBVO.setAmount(warehouseVO.getPay_amount());
				detailBVO.setPk_expense_type(warehouseVO.getPk_expense_type());
				detailBVO.setMemo(warehouseVO.getMemo());
				voList.add(detailBVO);
			}
			
			if(StringUtils.isNotBlank(warehouseVO.getPk_customer())
					&& warehouseVO.getRece_amount() != null){
				String key = warehouseVO.getAccount_period() + "," +warehouseVO.getPk_customer() + "," +warehouseVO.getBala_customer();
				List<ReceDetailBVO> voList = rdGroup.get(key);
				if(voList == null){
					voList = new ArrayList<ReceDetailBVO>();
					rdGroup.put(key, voList);
				}
				ReceDetailBVO detailBVO = new ReceDetailBVO();
				detailBVO.setAmount(warehouseVO.getRece_amount());
				detailBVO.setPk_expense_type(warehouseVO.getPk_expense_type());
				detailBVO.setMemo(warehouseVO.getMemo());
				voList.add(detailBVO);
			}
		}
		
		if(pdGroup.size() > 0){
			for(String key : pdGroup.keySet()){
				String accountPeriod = key.split(",")[0];
				String pk_carrier = key.split(",")[1];
				UFDouble cost_amount = UFDouble.ZERO_DBL;
				List<PayDetailBVO> voList = pdGroup.get(key);
				PayDetailVO payDetailVO = new PayDetailVO();
				payDetailVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(payDetailVO);
				payDetailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YFMX));
				payDetailVO.setVbillstatus(BillStatus.NEW);
				payDetailVO.setPk_carrier(pk_carrier);
				payDetailVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());
				payDetailVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				payDetailVO.setCreate_time(new UFDateTime(new Date()));
				payDetailVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				payDetailVO.setPay_type(PayDetailConst.PAYABLE_TYPE.DIRECT.intValue());
				payDetailVO.setAccount_period(new UFDateTime(accountPeriod));
				for(PayDetailBVO detailBVO : voList){
					cost_amount = cost_amount.add(detailBVO.getAmount());
					detailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(detailBVO);		
					detailBVO.setPk_pay_detail(payDetailVO.getPk_pay_detail());
				}
				payDetailVO.setCost_amount(cost_amount);
				payDetailVO.setUngot_amount(cost_amount);
				
				ExAggPayDetailVO aggPayDetailVO = new ExAggPayDetailVO();
				aggPayDetailVO.setParentVO(payDetailVO);
				aggPayDetailVO.setTableVO(TabcodeConst.TS_PAY_DETAIL_B, voList.toArray(new PayDetailBVO[voList.size()]));
				payDetailService.save(aggPayDetailVO, pdParamVO);
			}
		}
		
		if(rdGroup.size() > 0){
			for(String key : rdGroup.keySet()){
				String accountPeriod = key.split(",")[0];
				String pk_customer = key.split(",")[1];
				String bala_customer = key.split(",")[2];
				UFDouble cost_amount = UFDouble.ZERO_DBL;
				List<ReceDetailBVO> voList = rdGroup.get(key);
				ReceiveDetailVO receiveDetailVO = new ReceiveDetailVO();
				receiveDetailVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(receiveDetailVO);
				receiveDetailVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YSMX));
				receiveDetailVO.setVbillstatus(BillStatus.NEW);
				receiveDetailVO.setPk_customer(pk_customer);
				receiveDetailVO.setBala_customer(bala_customer);
				receiveDetailVO.setBalatype(DataDictConst.BALATYPE.MONTH.intValue());
				receiveDetailVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				receiveDetailVO.setCreate_time(new UFDateTime(new Date()));
				receiveDetailVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				receiveDetailVO.setRece_type(ReceiveDetailConst.OTHER_TYPE);
				receiveDetailVO.setAccount_period(new UFDateTime(accountPeriod));
				for(ReceDetailBVO detailBVO : voList){
					cost_amount = cost_amount.add(detailBVO.getAmount());
					detailBVO.setStatus(VOStatus.NEW);
					NWDao.setUuidPrimaryKey(detailBVO);		
					detailBVO.setPk_receive_detail(receiveDetailVO.getPk_receive_detail());
				}
				receiveDetailVO.setCost_amount(cost_amount);
				receiveDetailVO.setUngot_amount(cost_amount);
				
				ExAggReceiveDetailVO aggReceDetailVO= new ExAggReceiveDetailVO();
				aggReceDetailVO.setParentVO(receiveDetailVO);
				aggReceDetailVO.setTableVO(TabcodeConst.TS_RECE_DETAIL_B, voList.toArray(new ReceDetailBVO[voList.size()]));
				receDetailService.save(aggReceDetailVO, rdParamVO);
			}
		}
		

	}
	
	
}
