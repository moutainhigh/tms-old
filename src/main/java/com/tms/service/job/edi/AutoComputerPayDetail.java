/**
 * 
 */
package com.tms.service.job.edi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.nw.BillStatus;
import org.nw.dao.NWDao;
import org.nw.job.IJobService;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.JobDefVO;
import org.springframework.beans.factory.annotation.Autowired;
import com.tms.constants.SegmentConst;
import com.tms.service.cm.PayDetailService;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.te.EntLotVO;

/**
 * 自动按批次计算应付费用
 * 
 * @author lanjian
 * @Date 2015年12月27日 下午9:08:28
 *
 */
public class AutoComputerPayDetail implements IJobService  {

	@Autowired
	private PayDetailService payDetailService;

	Logger logger = Logger.getLogger(this.getClass());

	StringBuffer mess = new StringBuffer();// 每个批次计算时的错误信息
	private ParamVO paramVO;

	/*
	 * 
	 * @see org.nw.job.IJobService#exec(org.nw.vo.sys.JobDefVO)
	 */
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行应付费用计算任务-------------------");
		logger.info("从批次表中获取符合计算的批次单");
		//查询当前日期前30天未计算费用的批次信息
		UFDateTime dCurrDate = new UFDateTime(new Date());
		UFDateTime dStartDate = dCurrDate.getDateBefore(30);
		EntLotVO[] entLotVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLotVO.class,
				"ts > '"+dStartDate.toString()+"' and ts < '" + dCurrDate.toString() + "'");
		
		List<String> lots = new ArrayList<String>();
		if (entLotVOs != null && entLotVOs.length > 0) {
			logger.info("共查询到" + entLotVOs.length + "条发货单");
			for (EntLotVO entLotVO : entLotVOs) {
				lots.add(entLotVO.getLot());
				}
			}
		if(lots != null && lots.size() > 0){
			String lotsCond = NWUtils.buildConditionString(lots.toArray(new String[lots.size()]));
			String sql = "SELECT ts_pay_detail.* , ts_entrust_lot.lot FROM ts_pay_detail WITH(NOLOCK) "
					+ "LEFT JOIN ts_entrust WITH(NOLOCK) ON ts_entrust.vbillno = ts_pay_detail.entrust_vbillno "
					+ "LEFT JOIN ts_entrust_lot WITH(NOLOCK) ON ts_entrust_lot.lot = ts_entrust.lot "
					+ "WHERE isnull(ts_pay_detail.dr,0)=0 AND ts_pay_detail.pay_type=0 AND isnull(ts_entrust.dr,0)=0 AND isnull(ts_entrust_lot.dr,0)=0 AND ts_entrust.vbillstatus <> 24 "
					+ "AND ts_entrust_lot.lot in  " + lotsCond;
			List<PayDetailVO> payDetailVOs = NWDao.getInstance().queryForList(sql, PayDetailVO.class);
			if (payDetailVOs == null || payDetailVOs.size() == 0) {
				logger.info("没有查询到对应的应付");
			} else {
			//对payDetailVOs和entLotVOs进行分组匹配
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>(); // 待更新的VO
			for (EntLotVO entLotVO : entLotVOs){
				logger.info("开始处理批次号：" + entLotVO.getLot());
				mess.setLength(0);
				List<String> billId = new ArrayList<String>();
				for (PayDetailVO payDetailVO : payDetailVOs){
					if(entLotVO.getLot().equals(payDetailVO.getLot())){
						if (payDetailVO.getVbillstatus().equals(BillStatus.NEW)) {
							billId.add(payDetailVO.getPk_pay_detail());
						} else {
							logger.info("批次号" + entLotVO.getLot() + "中应付明细" + payDetailVO.getVbillno() + "状态有误");
							mess.setLength(1);
							break;
						}
					}
				}
				if (mess.length() > 0) {
					break;
				} else {
					payDetailService.doPayDetailRebuildBySegtype(paramVO, SegmentConst.SEG_TYPE_NORMAL, billId.toArray(new String[billId.size()]));
					//修改批次表费用计算标记信息2015-12-28 lanjian
					entLotVO.setStatus(VOStatus.UPDATED);
					entLotVO.setModify_time(new UFDateTime(new Date()));
					entLotVO.setModify_user("system");
					entLotVO.setDef4("Y");
					toBeUpdate.add(entLotVO);
				}
			}
			NWDao.getInstance().saveOrUpdate(toBeUpdate.toArray(new SuperVO[toBeUpdate.size()]));
		}
		}
		logger.info("-------------------应付费用计算任务执行完毕-------------------");
	}

	public void before(JobDefVO jobDefVO) {
		
	}

	public void after(JobDefVO jobDefVO) {
		
	}

}
