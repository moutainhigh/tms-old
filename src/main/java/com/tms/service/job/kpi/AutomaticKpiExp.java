package com.tms.service.job.kpi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.job.IJobService;
import org.nw.utils.BillnoHelper;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.JobDefVO;
import org.nw.vo.sys.UserVO;
import org.nw.web.utils.SpringContextHolder;
import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.ExpAccidentConst.ExpAccidentOrgin;
import com.tms.service.kpi.LimitService;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;

public class AutomaticKpiExp implements IJobService {
	
	private LimitService limitService;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	private static String KPI_USER_CODE = "KPIUser";
	private static String kpiUserSql = "SELECT * FROM nw_user WHERE isnull(dr,0) = 0 AND isnull(locked_flag,'N')='N' AND user_code = ?";
	private static UserVO kpiUserVO = NWDao.getInstance().queryForObject(kpiUserSql, UserVO.class, KPI_USER_CODE);

	public void before(JobDefVO jobDefVO) {
		if(kpiUserVO == null){
			logger.info("-------------------初始化KPI用户失败，请创建KPI用户-------------------");
			return;
		}
		limitService = SpringContextHolder.getBean("limitServiceImpl");
		if (limitService == null) {
			throw new BusiException("时效匹配服务没有启动，服务ID：LimitServiceImpl");
		}
	}

	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行委托单KPI计算任务-------------------");
		List<HashMap<String,String>> retList = limitService.matchLimit();
		if(retList == null || retList.size() == 0){
			return;
		}
		
		logger.info("共查询到" +  retList.size() + "条单据");
		for(HashMap<String,String> result : retList){
			//产生异常事故
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			ExpAccidentVO accidentVO  = new ExpAccidentVO();
			accidentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(accidentVO);
			accidentVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YCSG));
			accidentVO.setVbillstatus(BillStatus.NEW);
			accidentVO.setOrigin(ExpAccidentOrgin.KPI.toString());
			accidentVO.setInvoice_vbillno(result.get("invoice_vbillno"));
			accidentVO.setPk_customer(result.get("pk_customer"));
			accidentVO.setEntrust_vbillno(result.get("entrust_vbillno"));
			accidentVO.setPk_carrier(result.get("pk_carrier"));
			accidentVO.setExp_type(result.get("exp_type"));
			accidentVO.setPk_corp(result.get("pk_corp"));
			accidentVO.setFb_user(kpiUserVO.getPk_user());
			accidentVO.setFb_date(new UFDateTime(new Date()));
			accidentVO.setCreate_time(new UFDateTime(new Date()));
			accidentVO.setCreate_user(kpiUserVO.getPk_user());
			accidentVO.setReq_operate_time(new UFDateTime(result.get("req_operate")));
			accidentVO.setAct_operate_time(new UFDateTime(result.get("act_operate")));
			accidentVO.setExp_time(new UFDouble(result.get("exp_time")));
			accidentVO.setOccur_date(new UFDateTime(result.get("act_operate")));
			accidentVO.setOccur_addr(result.get("occur_addr"));
			accidentVO.setMemo(result.get("memo"));
			toBeUpdate.add(accidentVO);
			if(result.get("order_type").equals("FHD")){
				InvoiceVO invoiceVO = NWDao.getInstance().queryByCondition(InvoiceVO.class, "vbillno=?", result.get("invoice_vbillno"));
				if(invoiceVO == null){
					logger.info("发货单"+result.get("invoice_vbillno")+"不存在，KPI计算失败！");
					continue;
				}else{
					invoiceVO.setKpi_flag(UFBoolean.TRUE);
					invoiceVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(invoiceVO);
					NWDao.getInstance().saveOrUpdate(toBeUpdate);
				}
			}else if(result.get("order_type").equals("WTD")){
				EntrustVO entrustVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", result.get("entrust_vbillno"));
				if(entrustVO == null){
					logger.info("委托单"+result.get("entrust_vbillno")+"不存在，KPI计算失败！");
					continue;
				}else{
					entrustVO.setKpi_flag(UFBoolean.TRUE);
					entrustVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(entrustVO);
					NWDao.getInstance().saveOrUpdate(toBeUpdate);
				}
			}else if(result.get("order_type").equals("YSMX")){
				ReceiveDetailVO receiveDetailVO = NWDao.getInstance().queryByCondition(ReceiveDetailVO.class, "invoice_vbillno=?", result.get("invoice_vbillno"));
				if(receiveDetailVO == null){
					logger.info("发货单"+result.get("invoice_vbillno")+"的应收明细不存在，KPI计算失败！");
					continue;
				}else{
					receiveDetailVO.setKpi_flag(UFBoolean.TRUE);
					receiveDetailVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(receiveDetailVO);
					NWDao.getInstance().saveOrUpdate(toBeUpdate);
				}
			}else if(result.get("order_type").equals("YFMX")){
				PayDetailVO payDetailVO= NWDao.getInstance().queryByCondition(PayDetailVO.class, "entrust_vbillno=?", result.get("entrust_vbillno"));
				if(payDetailVO == null){
					logger.info("发货单"+result.get("invoice_vbillno")+"的应付明细不存在，KPI计算失败！");
					continue;
				}else{
					payDetailVO.setKpi_flag(UFBoolean.TRUE);
					payDetailVO.setStatus(VOStatus.UPDATED);
					toBeUpdate.add(payDetailVO);
					NWDao.getInstance().saveOrUpdate(toBeUpdate);
				}
			}
			
		}
		

	}

	public void after(JobDefVO jobDefVO) {

	}
	
	
}
